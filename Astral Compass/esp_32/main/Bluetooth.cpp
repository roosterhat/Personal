#include <main.h>
#include <Bluetooth.h>
#include <Motion.h>
#include <LED.h>
#include <regex>
#include <Utils.h>

SemaphoreHandle_t bleMutex = xSemaphoreCreateMutex();

void serialCommMonitor(void *pvParameters);

void BlinkOnPaired(void *pvParameters) {
  SetLEDs((int[]){ 0, 0, 100 }, 100, 100);
  vTaskDelay(pdMS_TO_TICKS(500));
  UpdateStatus(m_Status::IDLE);
  vTaskDelete(NULL);
}

class ServerCallbacks : public NimBLEServerCallbacks {
  void onConnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo) override {
    Serial.println("BLE Connected");
    xTaskCreate(BlinkOnPaired, "BlinkOnPaired", 1024, NULL, 1, NULL);
    BLEConnected = true;
  }

  void onDisconnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo, int reason) override {
    Serial.println("BLE Disconnected");
    UpdateStatus(m_Status::PAIRING);
    BLEConnected = false;
    BLESubscribed = false;
    BLEMtu = 23;
    digitalWrite(LASER, LOW);
    SetHoldPosition(false);
    SetMotorsEnabled(false);

    NimBLEDevice::startAdvertising();
  }

  void onMTUChange(uint16_t mtu, NimBLEConnInfo& connInfo) override {
    BLEMtu = mtu;
    char buffer[32];
    snprintf(buffer, 32, "BLE MTU: %i", mtu);
    serialPrintln(buffer);
  }
};

class SerialCharCallbacks : public NimBLECharacteristicCallbacks {
  void onSubscribe(NimBLECharacteristic* c, NimBLEConnInfo& connInfo, uint16_t subValue) override {
    BLESubscribed = (subValue & 0x01);   // 1 = notify, 2 = indicate, 0 = unsubscribed
  }
};

class CommandCallback : public BLECharacteristicCallbacks {  
  std::regex coordinatesPattern{R"(\w (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?))"};
  std::regex valuePattern{R"(\w (-?\d+(?:\.\d+)?))"};
  std::regex enablePattern{R"(\w (\d))"};

  void onWrite(NimBLECharacteristic* characteristic, NimBLEConnInfo& connInfo) override {
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
        xTaskCreate(Home, "Home", 2048, NULL, 5, NULL);
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
        xTaskCreate(Calibrate, "Calibrate", 2048, NULL, 5, NULL);
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

    Serial.println(command.c_str());
  }
};

void BLEInit() {
  NimBLEDevice::init(DEVICE_NAME);
  NimBLEDevice::setMTU(MTU);

  server = NimBLEDevice::createServer();
  server->setCallbacks(new ServerCallbacks());
  //server->updateConnParams(connHandle, minInterval, maxInterval, latency, timeout)

  service = server->createService(SERVICE_UUID);

  commandHandler = service->createCharacteristic(COMMAND_UUID, NIMBLE_PROPERTY::WRITE);
  commandHandler->setCallbacks(new CommandCallback());

  statusHandler       = service->createCharacteristic(STATUS_UUID,       NIMBLE_PROPERTY::NOTIFY);
  orientationHandler  = service->createCharacteristic(ORIENTATION_UUID,  NIMBLE_PROPERTY::NOTIFY);
  systemStatusHandler = service->createCharacteristic(SYSTEMSTATUS_UUID, NIMBLE_PROPERTY::NOTIFY);
  serialCommHandler   = service->createCharacteristic(SERIALCOMM_UUID,   NIMBLE_PROPERTY::NOTIFY);

  serialCommHandler->setCallbacks(new SerialCharCallbacks());

  service->start();

  NimBLEAdvertising* adv = NimBLEDevice::getAdvertising();
  adv->setName(DEVICE_NAME);
  adv->addServiceUUID(SERVICE_UUID);
  adv->enableScanResponse(true);
  adv->start();

  xTaskCreate(serialCommMonitor, "serialCommMonitor", 4096, NULL, 9, NULL);

  UpdateStatus(m_Status::PAIRING);
}

bool safeNotifyWorker(NimBLECharacteristic* handler, char* buffer, int size) {
  if (!BLESubscribed) return false;

  bool ok = false;  
  for (int attempt = 0; attempt < 10; attempt++) {    
    xSemaphoreTake(bleMutex, portMAX_DELAY);
    handler->setValue((uint8_t*)buffer, size);
    ok = handler->notify();    
    xSemaphoreGive(bleMutex);

    if (ok) break;
    vTaskDelay(pdMS_TO_TICKS(5));
  }
  
  return ok;
}

bool safeNotify(NimBLECharacteristic* handler, char* buffer, int size) {
  bool ok = true;
  int chunk = max(1, (int)BLEMtu - 3);
  for(int i = 0; i < size; i += chunk)
    ok &= safeNotifyWorker(handler, buffer + i, min(chunk, size - i));

  return ok;
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

void serialCommMonitor(void *pvParameters) {
  char * data;

  while (true) {
    if (BLESubscribed && xQueueReceive(bleSerialCommQueue, &data, portMAX_DELAY) == pdTRUE) {
      safeNotify(serialCommHandler, data, strlen(data));
      delete[] data;
    }    

    vTaskDelay(1);
  }
}