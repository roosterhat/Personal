#include <main.h>
#include <LED.h>
#include <Motion.h>
#include <Bluetooth.h>
#include <Adafruit_ICM20948.h>
#include <Adafruit_Sensor.h>
#include <math.h>
#include <SensorFusionEKF.h>
#include <regex>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

volatile int64_t lastICMPoll, lastGyroStateUpdate, lastCamMessage;
volatile bool serialReady = false;
volatile Status status = Status::INIT;
sensors_event_t accel, gyro, temp, mag;
Adafruit_ICM20948 ICM;
HardwareSerial camSerial(2);
SensorFusionEKF fusion;
QueueHandle_t estimateQueue = xQueueCreate(1, sizeof(RotationEstimate));
QueueHandle_t blinkQueue = xQueueCreate(1, sizeof(int));
Vector3 RPY, bias, targetPosition, initMagOrientation;
double northOffset;
int laserStatus, camFPS, IMUhz; 
SharedReader serialReader(camSerial);
QueueHandle_t serialMutex = xSemaphoreCreateMutex();

bool writeToSerial(const char* str, bool force = false);

void setup() {
  ledcAttach(LED_R, 5000, 8);
  ledcAttach(LED_G, 5000, 8);
  ledcAttach(LED_B, 5000, 8);
  pinMode(LED_STATUS, OUTPUT);
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

  getInitMagOrientaiton();
  Serial.println("Mag Orientation Initialized");

  InitInterrupts();
  Serial.println("Interrupts Initialized");

  InitSteppers();
  Serial.println("Steppers Initialized");    

  BLEInit();
  Serial.println("BLE Initialized");
  
  xTaskCreate(ProcessICMUpdates, "ProcessICMUpdates", 4096, NULL, 10, NULL);
  xTaskCreate(StepperLoop, "StepperLoop", 4096, NULL, 9, NULL);
  xTaskCreate(ProcessLEDs, "ProcessLEDs", 1024, NULL, 1, NULL);
  xTaskCreate(ProcessStatusLED, "ProcessStatusLED", 1024, NULL, 1, NULL);
  xTaskCreate(StateMonitor, "StateMonitor", 2048, NULL, 5, NULL);
  xTaskCreate(OrientationMonitor, "OrientationMonitor", 4096, NULL, 5, NULL);
  xTaskCreate(SystemMonitor, "SystemMonitor", 2048, NULL, 5, NULL);
  xTaskCreate(SerialConnectionMonitor, "SerialConnectionMonitor", 4096, NULL, 7, NULL);  
  xTaskCreate(SerialMonitor, "SerialMonitor", 8192, NULL, 7, NULL);      
  Serial.println("Threads Initialized");  
}

void loop() {
  delay(1000);
}

void getInitMagOrientaiton() {
  for(int i = 0; i < 10; i++) {
    ICM.getEvent(&accel, &gyro, &temp, &mag);
    initMagOrientation = Vector3(mag.magnetic.v);
    delay(10);
  }
}

void SystemMonitor(void *pvParameters) {
  while (true) {
    vTaskDelay(pdMS_TO_TICKS(1000));

    uint32_t totalTime;
    int taskCount = uxTaskGetNumberOfTasks();
    TaskStatus_t* taskArray = new TaskStatus_t[taskCount]();

    int tasks = uxTaskGetSystemState(taskArray, taskCount, &totalTime);

    String result = String(xPortGetFreeHeapSize()) + "," + String(totalTime);
    for(int i = 0; i < tasks; i++) {
      TaskStatus_t task = taskArray[i];
      result += ("," + String(task.pcTaskName) + "," + String(task.ulRunTimeCounter) + "," + String(task.uxCurrentPriority) + "," + String(task.eCurrentState) + "," + String(task.usStackHighWaterMark));
    }

    delete[] taskArray;

    transmitSystemStatus(result.c_str(), result.length());
  }
}

void StateMonitor(void *pvParameters) {
  char buffer[128];
  char* statusFormat = "S %i %i %i %i %i %i";

  while(true) {
    laserStatus = digitalRead(LASER);

    int size = snprintf(buffer, sizeof(buffer), statusFormat, laserStatus, switches[0].status, switches[1].status, serialReady, IMUhz, camFPS);
    transmitStatus(buffer, size);
    vTaskDelay(pdMS_TO_TICKS(200));
  }
}

void OrientationMonitor(void *pvParameters) {
  char buffer[512];
  char* orientationFormat = "O %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f";

  while(true) {
    int size = snprintf(buffer, sizeof(buffer), orientationFormat, RPY.x, RPY.y, RPY.z, bias.x, bias.y, bias.z, gyro.gyro.v[0], gyro.gyro.v[1], gyro.gyro.v[2], accel.acceleration.v[0], accel.acceleration.v[1], accel.acceleration.v[2]);
    transmitStatus(buffer, size);
    vTaskDelay(pdMS_TO_TICKS(10));
  }
}

void BlinkStatusLED() {
  int pvItem;
  xQueueGenericSend(blinkQueue, &pvItem, 0, queueSEND_TO_BACK);
}

void ProcessStatusLED(void *pvParameters) {
  int pvBuffer;
  while(true) {
    if (xQueueReceive(blinkQueue, &pvBuffer, 0) == pdTRUE) {
      digitalWrite(LED_STATUS, HIGH);
      vTaskDelay(1);
      digitalWrite(LED_STATUS, LOW);
    } 
    else {
      vTaskDelay(1);
    }
  }
}

void SerialConnectionMonitor(void *pvParameters) {  
  String command;
  bool acknowledged;

  vTaskDelay(pdMS_TO_TICKS(200));
  camSerial.begin(115200, SERIAL_8N1, SERIAL_RX, SERIAL_TX, false, 1000);
  camSerial.setTimeout(100);
  Serial.println("CamSerial: Connection opened");  
  Serial.print("CamSerial: Waiting for client");

  while(true) {
    if(!serialReady || esp_timer_get_time() > lastCamMessage + 1e6) {
      int index = -1;
      writeToSerial(serialReady ? "P" : "I", true);
      if(!serialReady)
        Serial.print("...");
      
      acknowledged = false;
      for(int i = 0; i < 1000; i++) {      
        if (camSerial.available()) {
          command = serialReader.read(index);
          
          if(command.startsWith("ACK")) {
            if(!serialReady)
              Serial.println("\nCamSerial: Connection established");
            serialReady = true;
            acknowledged = true;
            lastCamMessage = esp_timer_get_time();
            break;
          }
        }     

        vTaskDelay(1); 
      } 

      if(serialReady && !acknowledged) {
        serialReady = false;
        Serial.println("CamSerial: Connection lost");
        Serial.print("CamSerial: Waiting for client");
      }
    }
    vTaskDelay(pdMS_TO_TICKS(serialReady ? 100 : 1)); 
  }
}

void SerialMonitor(void *pvParameters) {
  int index = 0;
  String command;
  std::cmatch matches;
  std::regex camPattern{R"(U (\d+\.\d+) (\d+\.\d+) (\d+\.\d+) (\d+\.\d+) (\d+) (\d+\.\d+))"};   

  while (true) {
    while (!(serialReady && camSerial.available())) vTaskDelay(pdMS_TO_TICKS(1));
    command = serialReader.read(index);
    camFPS = (int)(1 / ((esp_timer_get_time() - lastCamMessage) / 1e6));
    lastCamMessage = esp_timer_get_time();
    //Serial.println(command);

    switch(command[0]) {
      case 'P':
        writeToSerial("ACK");
        break;
      case 'U':
        if (std::regex_search(command.c_str(), matches, camPattern)) {
          RotationEstimate estimate {
            .dtheta_x = stof(matches[1].str()),
            .dtheta_y = stof(matches[2].str()),
            .dtheta_z = stof(matches[3].str()),
            .dt_seconds = stof(matches[4].str()),
            .inlier_count = stof(matches[5].str()),
            .residual_rms = stof(matches[6].str()),
          };

          xQueueOverwrite(estimateQueue, &estimate);
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
  RotationEstimate estimate;

  while(true) {
    int64_t currentTick = esp_timer_get_time();
    ICM.getEvent(&accel, &gyro, &temp, &mag);
    double dt = (currentTick - lastICMPoll) / 1e6;
    IMUhz = (int)(1 / dt);
    lastICMPoll = currentTick;    

    if (xQueueReceive(estimateQueue, &estimate, 0) == pdTRUE) {
        fusion.updateVision(estimate);
    }

    fusion.predict(Vector3(gyro.gyro.v), dt);
    fusion.updateAccel(Vector3(accel.acceleration.v));

    RPY = fusion.getEulerRPY_deg();
    bias = fusion.getGyroBias();

    lastGyroStateUpdate = esp_timer_get_time();
    writeToSerialf("G %2.2f %2.2f %2.2f", RPY.x, RPY.y, RPY.z);

    //Serial.printf("RPY: [%.2f, %.2f, %.2f] deg   bias: [%.5f, %.5f, %.5f] rad/s\n", RPY.x, RPY.y, RPY.z, bias.x, bias.y, bias.z);

    vTaskDelay(pdMS_TO_TICKS(1));
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

  Wire.begin(ACC_SDA, ACC_SCL);
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
  Serial.print("Unable to find ICM, Halting");
  while(1) delay(10);
}

bool writeToSerial(const char* str, bool force) {
  if(!(serialReady || force)) return false;

  if (xSemaphoreTake(serialMutex, pdMS_TO_TICKS(100)) != pdTRUE) {
    return false;
  }

  camSerial.println(str);
  xSemaphoreGive(serialMutex);
  return true;
}

bool writeToSerialf(const char * format, ...) { 
  if(!serialReady) return false;

  if (xSemaphoreTake(serialMutex, pdMS_TO_TICKS(100)) != pdTRUE) {
    return false;
  }

  va_list args;
  va_start(args, format);

  camSerial.vprintf(format, args);
  camSerial.println();
  va_end(args);
  xSemaphoreGive(serialMutex);
  return true;
}
