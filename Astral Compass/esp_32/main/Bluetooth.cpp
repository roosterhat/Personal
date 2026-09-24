#include <main.h>
#include <Bluetooth.h>
#include <Motion.h>
#include <LED.h>
#include <regex>

SemaphoreHandle_t bleMutex = xSemaphoreCreateMutex();

void BlinkOnPaired(void *pvParameters) {
  SetLEDs((int[]){ 0, 0, 100 }, 100, 100);
  vTaskDelay(pdMS_TO_TICKS(500));
  UpdateStatus(Status::IDLE);
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
      UpdateStatus(Status::PAIRING);  
      BLEConnected = false;
      digitalWrite(LASER, LOW);
      SetHoldPosition(false);
      SetMotorsEnabled(false);

      server->getAdvertising()->start();
    }
};

class CommandCallback : public BLECharacteristicCallbacks {  
  std::regex coordinatesPattern{R"(C (\d+\.\d+) (\d+\.\d+))"};
  std::regex enablePattern{R"(\w (\d))"};

  void onWrite(BLECharacteristic *characteristic) {
    BlinkStatusLED();

    std::cmatch matches;
    String command = characteristic->getValue();

    if (command.length() == 0) return;

    switch(command[0]) {
      case 'C':
        if (std::regex_search(command.c_str(), matches, coordinatesPattern)) {
          PointTo(stof(matches[1].str()), stof(matches[2].str()));
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
        Home();
        break;
      case 'Z':
        ZeroOrientation();
        break;
      case 'P':
        if (std::regex_search(command.c_str(), matches, enablePattern)) {
          SetHoldPosition((bool)stoi(matches[1].str()));
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

    UpdateStatus(Status::PAIRING);    
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