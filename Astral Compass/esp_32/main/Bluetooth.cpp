#include <main.h>
#include <Bluetooth.h>
#include <Motion.h>
#include <LED.h>
#include <regex>
#include <Utils.h>

SemaphoreHandle_t bleMutex = xSemaphoreCreateMutex();
static QueueHandle_t bleCmdQueue = xQueueCreate(8, sizeof(char[64]));

void serialCommMonitor(void *pvParameters);
void bleMessageHandler(void *pvParameters);

void BlinkOnPaired(void *pvParameters) {
  SetLEDs((int[]){ 0, 0, 100 }, 100, 100);
  vTaskDelay(pdMS_TO_TICKS(500));
  UpdateStatus(m_Status::IDLE);
  vTaskDelete(NULL);
}

class ServerCallbacks : public NimBLEServerCallbacks {
  void onConnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo) override {
    Serial.println("BLE Connected");
    taskCreateTracked(BlinkOnPaired, "BlinkOnPaired", 1024, NULL, 1, NULL);
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
  void onWrite(NimBLECharacteristic* characteristic, NimBLEConnInfo& connInfo) override {
    NimBLEAttValue v = characteristic->getValue();
    char msg[64] = {0};
    memcpy(msg, v.data(), min((size_t)63, (size_t)v.size()));
    xQueueSend(bleCmdQueue, msg, 0);
  }  
};

void BLEInit() {
  NimBLEDevice::init(DEVICE_NAME);
  NimBLEDevice::setMTU(MTU);

  server = NimBLEDevice::createServer();
  server->setCallbacks(new ServerCallbacks());

  service = server->createService(SERVICE_UUID);

  commandHandler = service->createCharacteristic(COMMAND_UUID, NIMBLE_PROPERTY::WRITE);
  commandHandler->setCallbacks(new CommandCallback());

  statusHandler       = service->createCharacteristic(STATUS_UUID,       NIMBLE_PROPERTY::NOTIFY);
  orientationHandler  = service->createCharacteristic(ORIENTATION_UUID,  NIMBLE_PROPERTY::NOTIFY);
  systemStatusHandler = service->createCharacteristic(SYSTEMSTATUS_UUID, NIMBLE_PROPERTY::NOTIFY);
  frameHandler        = service->createCharacteristic(FRAME_UUID,        NIMBLE_PROPERTY::NOTIFY);
  serialCommHandler   = service->createCharacteristic(SERIALCOMM_UUID,   NIMBLE_PROPERTY::NOTIFY);

  systemStatusHandler->indicate();
  serialCommHandler->setCallbacks(new SerialCharCallbacks());

  service->start();

  NimBLEAdvertising* adv = NimBLEDevice::getAdvertising();
  adv->setName(DEVICE_NAME);
  adv->addServiceUUID(SERVICE_UUID);
  adv->enableScanResponse(true);
  adv->start();

  taskCreateTracked(serialCommMonitor, "serialCommMonitor", 4096, NULL, 9, NULL);
  taskCreateTracked(bleMessageHandler, "bleMessageHandler", 8192, NULL, 5, NULL);

  UpdateStatus(m_Status::PAIRING);
}

void bleMessageHandler(void * pvParameters) {
  char msg[64];
  std::cmatch matches;
  std::regex coordinatesPattern{R"(\w (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?))"};
  std::regex valuePattern{R"(\w (-?\d+(?:\.\d+)?))"};
  std::regex enablePattern{R"(\w (\d))"};
  std::regex calPattern{R"(\w (\d)?)"};
  std::regex digitPattern{R"((-?\d+(?:\.\d+)?))"};

  while(true) {   
    if (!xQueueReceive(bleCmdQueue, msg, portMAX_DELAY) || strlen(msg) == 0)
      continue;

    BlinkStatusLED();

    switch(msg[0]) {
      case 'D':
        if (status != m_Status::TRACKING && status != m_Status::CAL &&  std::regex_search(msg, matches, coordinatesPattern)) {
          float az = M_target[0] + stof(matches[1].str());
          float el = M_target[1] + stof(matches[2].str());
          boundAZEL(az, el);
          PointTo(az, el);
        }
        break;
      case 'A':
        if (status != m_Status::TRACKING && status != m_Status::CAL && std::regex_search(msg, matches, coordinatesPattern)) {
          float az = stof(matches[1].str());
          float el = stof(matches[2].str());
          PointTo(az, el);
        }
        break;
      case 'T':
        if (status != m_Status::CAL && std::regex_search(msg, matches, coordinatesPattern)) {
          if(status != m_Status::TRACKING)
            UpdateStatus(m_Status::TRACKING);
          float az = stof(matches[1].str()) - northOffset;
          float el = stof(matches[2].str());
          PointTo(az, el);
        }
        break;
      case 'L':
        if (std::regex_search(msg, matches, enablePattern)) {
          digitalWrite(LASER, stoi(matches[1].str()) ? HIGH : LOW);
        }
        break;
      case 'E':
        if (std::regex_search(msg, matches, enablePattern)) {
          SetMotorsEnabled((bool)stoi(matches[1].str()));
        }
        break;
      case 'H':
        taskCreateTracked(Home, "Home", 2048, NULL, 5, NULL);
        break;
      case 'Z':
        M_target[0] = 0; M_target[1] = 0;
        MoveTo(0,0);
        break;
      case 'P':
        if (std::regex_search(msg, matches, enablePattern)) {
          SetHoldPosition((bool)stoi(matches[1].str()));
        }
        break;
      case 'R':
        ESP.restart();
        break;
      case 'C':
        if (std::regex_search(msg, matches, calPattern)) {
          int calType = stoi(matches[1].str());
          std::cmatch argMatches[4];
          int argCount = regex_searchAll(msg + matches.length(), argMatches, 4, digitPattern);
          if(argCount > 0) {
            int arg = stoi(argMatches[0].str());

            switch(calType) {
              case 1:
                switch(arg) {
                  case 0:
                    bz = stof(argMatches[1].str());
                    declination = stof(argMatches[2].str());
                    taskCreateTracked(Calibrate, "Calibrate", 8192, (void *)calType, 5, NULL);
                    break;
                  case 1:
                    cancelMagCalibration();
                    break;
                }
                break;
              case 2:
               switch(arg) {
                case 0:
                  cancelFocalCalibration();
               }
               break;
            }
          }
          else {
            taskCreateTracked(Calibrate, "Calibrate", 8192, (void *)calType, 5, NULL);
          }
        }        
        break;
      case 'S':
        UpdateStatus(m_Status::IDLE);
        break;
      case 'B':
        if (std::regex_search(msg, matches, valuePattern)) {
          SetBrightness(stof(matches[1].str()));
        }
        break;
      case 'F':
        startFrameProcessing();
        break;
    }

    Serial.println(msg);

    vTaskDelay(1);
  }
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

void transmitFrame(char* buffer, int size) { 
  safeNotify(frameHandler, buffer, size);
}

void transmitFrameChunk(char* buffer, int index, int size) {
  const int payload = max(1, (int)BLEMtu - 4);
  uint8_t pkt[payload + 1];
  for (int i = 0; i * payload < size; i++) {
    int s = min(payload, size - i * payload);
    pkt[0] = (i << 5) | index;
    memcpy(pkt + 1, buffer + i * payload, s);
    while (!safeNotifyWorker(frameHandler, (char *)pkt, s + 1)) vTaskDelay(1);
    vTaskDelay(10);
  }
}
