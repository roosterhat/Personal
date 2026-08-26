#include <main.h>
#include <LED.h>
#include <Adafruit_ICM20948.h>
#include <Adafruit_Sensor.h>
#include <math.h>
#include <SensorFusionEKF.h>
#include <Motion.h>
#include <regex>
#include <BluetoothSerial.h>

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

volatile int64_t previousTick = 0, currentTick = 1, lastICMPoll, lastGyroStateUpdate;
volatile bool serialReady = false, BTSerialReady = false;
double vel[] = {0,0,0}, pos[] = {0,0,0}, angle[] = {0,0,0}, northOffset = 0;
volatile Status status = Status::INIT;
Adafruit_ICM20948 ICM;
sensors_event_t accel, gyro, temp, mag;
HardwareSerial camSerial(2);
BluetoothSerial BTSerial;
SensorFusionEKF fusion;
RotationEstimate* currentEstimate = nullptr;

void setup() {
  ledcAttach(LED_R, 5000, 8);
  ledcAttach(LED_G, 5000, 8);
  ledcAttach(LED_B, 5000, 8);
  pinMode(LM1, INPUT_PULLUP);
  pinMode(LM2, INPUT_PULLUP);
  pinMode(LASER, OUTPUT);

  digitalWrite(LASER, LOW);

  Serial.begin(115200);
  Serial.println("\nSerial Connected");

  UpdateStatus(Status::INIT);
  UpdateLEDs();

  SearchForICM();
  ICM.setAccelRange(ICM20948_ACCEL_RANGE_2_G);
  ICM.setGyroRange(ICM20948_GYRO_RANGE_2000_DPS);
  ICM.setMagDataRate(AK09916_MAG_DATARATE_100_HZ);
  Serial.println("ICM Initialized");

  InitInterrupts();
  Serial.println("Interrupts Initialized");

  InitSteppers();
  Serial.println("Steppers Initialized");

  camSerial.begin(38400, SERIAL_8N1, SERIAL_RX, SERIAL_TX, false, 1000);
  Serial.println("CamSerial: Connection opened");   

  xTaskCreate(SerialMonitor, "SerialMonitor", 1024, NULL, 5, NULL);  
  xTaskCreate(SerialConnectionMonitor, "SerialConnectionMonitor", 1024, NULL, 1, NULL);
  xTaskCreate(BluetoothMonitor, "SerialConnectionMonitor", 4096, NULL, 5, NULL);
  xTaskCreate(ProcessICMUpdates, "ProcessICMUpdates", 4096, NULL, 10, NULL);
  xTaskCreate(StepperLoop, "StepperLoop", 4096, NULL, 9, NULL);
  xTaskCreate(ProcessLEDs, "ProcessLEDs", 1024, NULL, 1, NULL);
  Serial.println("Threads Initialized");  
}

void loop() {
  previousTick = currentTick;
  currentTick = esp_timer_get_time();
}

void BluetoothMonitor(void *pvParameters) {
  BTSerial.begin("Astral Compass");
  Serial.println("Bluetooth: Initialized");
  UpdateStatus(Status::PAIRING);    

  while (true) {
    if(BTSerial.connected()) {
      if(!BTSerialReady) {       
        Serial.println("Bluetooth: Paired");
        BTSerialReady = true;
        UpdateStatus(Status::PAIRED);
        vTaskDelay(pdMS_TO_TICKS(250));
        UpdateStatus(Status::IDLE);
      }

      if(BTSerial.available()) {
        String command = BTSerial.readString();

        switch(command[0]) {
          case 'P':
            BTSerial.print("ACK");
            break;
        }
      }
    }
    else if(BTSerialReady) {
      Serial.println("Bluetooth: Disconnected");
      BTSerialReady = false;
      UpdateStatus(Status::PAIRING);
    }    
  }
}

void SerialConnectionMonitor(void *pvParameters) {
  while (true) {
    if(serialReady) {
      if(!writeToSerial("P")) {
        serialReady = false;
        Serial.println("CamSerial: Connection unresponsive");
      }
    }
    else {
      while(camSerial.availableForWrite() == 0) {}
      camSerial.print("I");
      Serial.println("CamSerial: Waiting for client");

      while (true) {
        while (camSerial.available() == 0){} 
        String command = camSerial.readString();

        if(command == "ACK") {
          Serial.println("CamSerial: Connection established");
          serialReady = true;
          break;
        }
      }      
    }

    vTaskDelay(pdMS_TO_TICKS(100));
  }
}

void SerialMonitor(void *pvParameters) {
  std::cmatch matches;
  std::regex camPattern("U (\d+\.\d+) (\d+\.\d+) (\d+\.\d+) (\d+\.\d+) (\d+) (\d+\.\d+)");    

  while (true) {
    while (camSerial.available() == 0){}
    String command = camSerial.readString();

    switch(command[0]) {
      case 'P':
        writeToSerial("ACK");
        break;
      case 'U':
        if (std::regex_search(command.c_str(), matches, camPattern)) {
          *currentEstimate = RotationEstimate {
            .dtheta_x = stof(matches[1].str()),
            .dtheta_y = stof(matches[2].str()),
            .dtheta_z = stof(matches[3].str()),
            .dt_seconds = stof(matches[4].str()),
            .inlier_count = stof(matches[5].str()),
            .residual_rms = stof(matches[6].str()),
          };
        }
        break;
    }
  }
}

void ProcessLEDs(void *pvParameters) {
  while(true) {
    UpdateLEDs();
    vTaskDelay(pdMS_TO_TICKS(1));
  }
}

void ProcessICMUpdates(void *pvParameters) {
  fusion.init();

  while(true) {
    int64_t start = currentTick;
    ICM.getEvent(&accel, &gyro, &temp, &mag);
    double dt = pdTICKS_TO_MS(currentTick - lastICMPoll);
    lastICMPoll = currentTick;

    if(currentEstimate != nullptr) {
      fusion.updateVision(*currentEstimate);
      currentEstimate = 0;
    }

    fusion.predict(Vector3(gyro.gyro.v), dt);
    fusion.updateAccel(Vector3(accel.acceleration.v));

    Vector3 rpy = fusion.getEulerRPY_deg();
    Vector3 b = fusion.getGyroBias();

    lastGyroStateUpdate = currentTick;
    writeToSerialf("G %2.2f %2.2f %2.2f", rpy.x, rpy.y, rpy.z);

    Serial.printf("RPY: [%.2f, %.2f, %.2f] deg   bias: [%.5f, %.5f, %.5f] rad/s\n", rpy.x, rpy.y, rpy.z, b.x, b.y, b.z);

    vTaskDelay(fmax(pdMS_TO_TICKS(1) - (currentTick - start), 1));
  }
}

void UpdateStatus(Status s) {
  status = s;

  switch (status) {
    case Status::INIT:
      SetLEDs((int[]){ 255, 0, 0 }, 1, 0);
      break;
    case Status::PAIRING:
      SetLEDs((int[]){ 0, 0, 100 }, 500, 500);
      break;
    case Status::PAIRED:
      SetLEDs((int[]){ 0, 0, 100 }, 100, 100);
      break;
    case Status::TRACKING:
      SetLEDs((int[]){ 0, 90, 0 }, 1, 0);
      break;
    case Status::IDLE:
      SetLEDs((int[]){ 180, 90, 0 }, 1, 0);
      break;
    default:
      break;
  }
}

void SearchForICM() {
  Serial.println("Searching for ICM...");

  Wire.begin();
  byte error, address;
  for(address = 1; address < 127; address++ ) {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();
    if (error == 0) {
      Serial.print("I2C device found at address 0x");
      Serial.println(address, 16);

      if(ICM.begin_I2C(address)) {
        Serial.println("Paired ICM");
        return;
      }
    }
  }
  while(1) delay(10);
}

bool writeToSerial(const char str[]) {
  if(!serialReady) return false;

  for(int i = 0; i < 100; i++) {
    if(camSerial.availableForWrite() > 0) {
      camSerial.print(str);
      return true;
    }
  }

  return false;
}

bool writeToSerialf(const char * format, ...) { 
  if(!serialReady) return false;

  va_list args;
  for(int i = 0; i < 100; i++) {
    if(camSerial.availableForWrite() > 0) {
      camSerial.printf(format, args);
      return true;
    }
  }

  return false;
}
