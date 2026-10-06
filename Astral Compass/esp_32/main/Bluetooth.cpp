#include <main.h>
#include <Bluetooth.h>
#include <Motion.h>
#include <LED.h>
#include <regex>
#include <Utils.h>

SemaphoreHandle_t bleMutex = xSemaphoreCreateMutex();

void BlinkOnPaired(void *pvParameters) {
  SetLEDs((int[]){ 0, 0, 100 }, 100, 100);
  vTaskDelay(pdMS_TO_TICKS(500));
  UpdateStatus(m_Status::IDLE);
  vTaskDelete(NULL);
}

class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      Serial.println("BLE Connected");
      xTaskCreate(BlinkOnPaired, "BlinkOnPaired", 1024, NULL, 1, NULL);  
      BLEConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      Serial.println("BLE Disconnected");
      UpdateStatus(m_Status::PAIRING);  
      BLEConnected = false;
      digitalWrite(LASER, LOW);
      SetHoldPosition(false);
      SetMotorsEnabled(false);

      server->getAdvertising()->start();
    }
};

class CommandCallback : public BLECharacteristicCallbacks {  
  std::regex coordinatesPattern{R"(\w (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?))"};
  std::regex valuePattern{R"(\w (-?\d+(?:\.\d+)?))"};
  std::regex enablePattern{R"(\w (\d))"};

  void onWrite(BLECharacteristic *characteristic) {
    BlinkStatusLED();

    std::cmatch matches;
    String command = characteristic->getValue();

    if (command.length() == 0) return;

    switch(command[0]) {
      case 'D':
        if (status != m_Status::TRACKING && status != m_Status::CAL &&  std::regex_search(command.c_str(), matches, coordinatesPattern)) {
          float az = M_target[0] + stof(matches[1].str());
          float el = M_target[1] + stof(matches[2].str());
          boundAZEL(az, el);
          PointTo(az, el);
        }
        break;
      case 'F':
        if (status != m_Status::TRACKING && status != m_Status::CAL && std::regex_search(command.c_str(), matches, coordinatesPattern)) {
          float az = stof(matches[1].str());
          float el = stof(matches[2].str());
          PointTo(az, el);
        }
        break;
      case 'T':
        if (status != m_Status::CAL && std::regex_search(command.c_str(), matches, coordinatesPattern)) {
          if(status != m_Status::TRACKING)
            UpdateStatus(m_Status::TRACKING);
          float az = stof(matches[1].str()) + northOffset;
          float el = stof(matches[2].str());
          PointTo(az, el);
        }
        break;
      case 'L':
        if (std::regex_search(command.c_str(), matches, enablePattern)) {
          digitalWrite(LASER, stoi(matches[1].str()) ? HIGH : LOW);
        }
        break;
      case 'E':
        if (std::regex_search(command.c_str(), matches, enablePattern)) {
          SetMotorsEnabled((bool)stoi(matches[1].str()));
        }
        break;
      case 'H':
        xTaskCreate(Home, "Home", 4096, NULL, 5, NULL);
        break;
      case 'Z':
        M_target[0] = 0; M_target[1] = 0;
        MoveTo(0,0);
        break;
      case 'P':
        if (std::regex_search(command.c_str(), matches, enablePattern)) {
          SetHoldPosition((bool)stoi(matches[1].str()));
        }
        break;
      case 'R':
        ESP.restart();
        break;
      case 'C':
        xTaskCreate(Calibrate, "Calibrate", 4096, NULL, 5, NULL);
        break;
      case 'S':
        UpdateStatus(m_Status::IDLE);
        break;
      case 'B':
        if (std::regex_search(command.c_str(), matches, valuePattern)) {
          SetBrightness(stof(matches[1].str()));
        }
        break;
    }

    Serial.println(command);
  }
};

void BLEInit() {
    BLEDevice::init("Astral Compass");
    BLEDevice::setMTU(128);
    BLEServer *server = BLEDevice::createServer();
    server->setCallbacks(new ServerCallbacks());

    BLEService *service = server->createService(SERVICE_UUID);

    commandHandler = service->createCharacteristic(COMMAND_UUID, BLECharacteristic::PROPERTY_WRITE);
    commandHandler->setCallbacks(new CommandCallback());

    statusHandler = service->createCharacteristic(STATUS_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    statusHandler->addDescriptor(new BLE2902());

    orientationHandler = service->createCharacteristic(ORIENTATION_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    orientationHandler->addDescriptor(new BLE2902());

    systemStatusHandler = service->createCharacteristic(SYSTEMSTATUS_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    systemStatusHandler->addDescriptor(new BLE2902());

    service->start();
    server->getAdvertising()->start();

    UpdateStatus(m_Status::PAIRING);    
}

void safeNotify(BLECharacteristic* handler, char* buffer, int size) {
  if(!BLEConnected) return;

  xSemaphoreTake(bleMutex, portMAX_DELAY);
  handler->setValue((uint8_t*) buffer, size);
  handler->notify();
  xSemaphoreGive(bleMutex);
}

void transmitStatus(char* buffer, int size) {
  safeNotify(statusHandler, buffer, size);
}

void transmitOrientation(char* buffer, int size) {
  safeNotify(orientationHandler, buffer, size);
}

void transmitSystemStatus(char* buffer, int size) { 
  safeNotify(systemStatusHandler, buffer, size);
}