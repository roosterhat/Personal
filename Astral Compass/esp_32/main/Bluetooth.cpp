#include <main.h>
#include <Bluetooth.h>
#include <Motion.h>
#include <regex>

class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      Serial.println("BLE Connected");
      UpdateStatus(Status::PAIRED);  
      BLEConnected = true;
    };
    void onDisconnect(BLEServer* pServer) {
      Serial.println("BLE Disconnected");
      UpdateStatus(Status::PAIRING);  
      BLEConnected = false;
    }
};

class CommandCallback : public BLECharacteristicCallbacks {  
  std::regex coordinatesPattern{R"(C (\d+\.\d+) (\d+\.\d+))"};
  std::regex laserPattern{R"(L (\d))"};
  std::regex enablePattern{R"(E (\d))"};

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
        if (std::regex_search(command.c_str(), matches, laserPattern)) {
          digitalWrite(LASER, stoi(matches[1].str()) ? HIGH : LOW);
        }
        break;
      case 'E':
        if (std::regex_search(command.c_str(), matches, enablePattern)) {
          SetEnabled((bool)stoi(matches[1].str()));
        }
        break;
      case 'H':
        Home();
        break;
    }

    Serial.println(command);
  }
};

void BLEInit() {
    BLEDevice::init("Astral-Compass");
    BLEServer *server = BLEDevice::createServer();
    server->setCallbacks(new ServerCallbacks());

    BLEService *service = server->createService(SERVICE_UUID);

    commandHandler = service->createCharacteristic(COMMAND_UUID, BLECharacteristic::PROPERTY_WRITE);
    commandHandler->setCallbacks(new CommandCallback());

    statusHandler = service->createCharacteristic(STATUS_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    statusHandler->addDescriptor(new BLE2902());

    systemHandler = service->createCharacteristic(SYSTEM_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    systemHandler->addDescriptor(new BLE2902());

    service->start();
    server->getAdvertising()->start();

    UpdateStatus(Status::PAIRING);    
}

void transmitStatus(String& value) {
  if(!BLEConnected) return;

  statusHandler->setValue(value);
  statusHandler->notify();
}

void transmitStatus(char* buffer, int size) {
  if(!BLEConnected) return;

  statusHandler->setValue((uint8_t *) buffer, size);
  statusHandler->notify();
}

void transmitSystemStatus(const char* buffer, int size) {
  if(!BLEConnected) return;

  systemHandler->setValue((uint8_t *) buffer, size);
  systemHandler->notify();
}