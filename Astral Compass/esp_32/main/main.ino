//Partition Scheme: No FS 4MB

#include <main.h>
#include <LED.h>
#include <Motion.h>
#include <Bluetooth.h>
#include <Adafruit_ICM20948.h>
#include <Adafruit_Sensor.h>
#include <math.h>
#include <SensorFusionEKF.h>
#include <regex>
#include <esp_core_dump.h>
#include <MagCalibrator.h>


#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

volatile int64_t lastICMPoll, lastGyroStateUpdate, lastCamMessage;
volatile bool serialReady = false;
volatile m_Status status = m_Status::INIT;
sensors_event_t accel, gyro, temp, mag;
Adafruit_ICM20948 ICM;
HardwareSerial camSerial(2);
SensorFusionEKF fusion;
QueueHandle_t estimateQueue = xQueueCreate(1, sizeof(RotationEstimate));
QueueHandle_t blinkQueue = xQueueCreate(1, sizeof(int));
QueueHandle_t bleSerialCommQueue = xQueueCreate(128, sizeof(char *));
QueueHandle_t serialMutex = xSemaphoreCreateMutex();
Vector3 RPY, bias, targetPosition, magOrientation, calibratedBias;
int laserStatus, camFPS, IMUhz; 
SharedReader serialReader(camSerial);
RotationEstimate rotationEstimate;
AzEl previousPos;
float previousTarget[] = { 0, 0 };
float northOffset = 0, bz = 0, declination = 0;
m_CalStatus gyroCalStatus = m_CalStatus::INIT, magCalStatus = m_CalStatus::INIT;
int magCalSteps = 0;
int currentChunk = 0, chunkCount = 0, frameSize = 0, totalSent = 0;
static const int chunkSize = (128 - 4) * 4;
TaskHandle_t currentFrameProcessor;
int64_t lastFrame;
NorthEstimator northEstimator(12, 10);
static const char* const ESPResetCodes[] = {"ESP_RST_UNKNOWN", "ESP_RST_POWERON", "ESP_RST_EXT", "ESP_RST_SW", "ESP_RST_PANIC", "ESP_RST_INT_WDT", "ESP_RST_TASK_WDT", "ESP_RST_WDT", "ESP_RST_DEEPSLEEP", "ESP_RST_BROWNOUT", "ESP_RST_SDIO", "ESP_RST_USB", "ESP_RST_JTAG", "ESP_RST_EFUSE", "ESP_RST_PWR_GLITCH", "ESP_RST_CPU_LOCKUP"};
static const char* const exceptionCauseCodes[] = {"IllegalInstruction", "SYSCALL", "InstructionFetchError", "LoadStoreError", "Level1Interrupt", "Alloca", "IntegerDivideByZero", "reserved", "Privileged", "LoadStoreAlignment", "reserved", "reserved", "InstrPIFDataError", "LoadStorePIFDataError", "InstrPIFAddrError", "LoadStorePIFAddrError", "InstTLBMiss", "InstTLBMultiHit", "InstFetchPrivilege", "reserved", "InstFetchProhibited", "reserved", "reserved", "reserved", "LoadStoreTLBMiss", "LoadStoreTLBMultiHit", "LoadStorePrivilege", "reserved", "LoadProhibited", "StoreProhibited"};

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
  digitalWrite(LED_STATUS, LOW);

  Serial.begin(115200);
  checkCrash();
  serialPrintln("\nSerial Connected");

  UpdateStatus(m_Status::INIT);  

  UpdateLEDs();

  SearchForICM();
  ICM.setAccelRange(ICM20948_ACCEL_RANGE_2_G);
  ICM.setGyroRange(ICM20948_GYRO_RANGE_2000_DPS);
  ICM.setMagDataRate(AK09916_MAG_DATARATE_100_HZ);
  serialPrintln("ICM Initialized");

  InitInterrupts();
  serialPrintln("Interrupts Initialized");

  InitSteppers();
  serialPrintln("Steppers Initialized");    

  BLEInit();
  serialPrintln("BLE Initialized");
  
  taskCreateTracked(ProcessICMUpdates, "ProcessICMUpdates", 4096, NULL, 10, NULL);
  taskCreateTracked(StepperLoop, "StepperLoop", 8192, NULL, 9, NULL);
  taskCreateTracked(ProcessLEDs, "ProcessLEDs", 1024, NULL, 1, NULL);
  taskCreateTracked(ProcessStatusLED, "ProcessStatusLED", 1024, NULL, 1, NULL);
  taskCreateTracked(StateMonitor, "StateMonitor", 2048, NULL, 5, NULL);
  taskCreateTracked(OrientationMonitor, "OrientationMonitor", 4096, NULL, 5, NULL);
  taskCreateTracked(SystemMonitor, "SystemMonitor", 2048, NULL, 5, NULL);
  taskCreateTracked(SerialConnectionMonitor, "SerialConnectionMonitor", 4096, NULL, 7, NULL);  
  taskCreateTracked(SerialMonitor, "SerialMonitor", 10240, NULL, 7, NULL);      
  serialPrintln("Threads Initialized");  
}

void loop() {
  delay(1000);
}

void checkCrash() {
  if (esp_core_dump_image_check() != ESP_OK) return;

  esp_core_dump_summary_t* s = (esp_core_dump_summary_t*)malloc(sizeof(*s));
  if (s && esp_core_dump_get_summary(s) == ESP_OK) {
    char buffer[256];
    esp_reset_reason_t r = esp_reset_reason();
    snprintf(buffer, 256, "%s(%i)\nCrashed in task: %s, PC: 0x%08x\ncause: %s(%u), vaddr: 0x%08x", ESPResetCodes[r], r, s->exc_task, s->exc_pc, exceptionCauseCodes[s->ex_info.exc_cause], s->ex_info.exc_cause, s->ex_info.exc_vaddr);
    serialPrintln(buffer);
    serialPrintln("Backtrace:");
    for (int i = 0; i < s->exc_bt_info.depth; i++) {
      snprintf(buffer, 256, " 0x%08x", s->exc_bt_info.bt[i]);
      serialPrintln(buffer);
    }
  }
  free(s);
  esp_core_dump_image_erase();
}

void cancelMagCalibration() {
  magCalStatus = m_CalStatus::INIT;
}

void calibrateMagOffset(void *pvParameters) {
  magCalSteps = 0;
  int magCalStepsMask = (1ull << 12) - 1;
  northEstimator.begin(12, 10);
  northEstimator.setVerticalField(bz);

  while(magCalStatus == m_CalStatus::RUNNING && magCalSteps != magCalStepsMask) {
    int index = northEstimator.addSample(Vector3(-mag.magnetic.v[0], -mag.magnetic.v[1], mag.magnetic.v[2]), RPY);

    if(index >= 0) {
      int mask = 1 << index;
      if(!(magCalSteps & mask) && northEstimator.sliceComplete(index))
        magCalSteps |= mask;
    }

    vTaskDelay(20);
  }

  magCalStatus = m_CalStatus::INIT;
  if(northEstimator.complete()) {
    auto r = northEstimator.estimate();
    if (r.valid) {
      northOffset  = NorthEstimator::trueNorthYaw(r.northYawDeg, declination);
      magCalStatus = m_CalStatus::CALIBRATED;
    }
  }
  
  UpdateStatus(m_Status::IDLE);
  vTaskDelete(NULL);
}

void calibrateGyroBias(void *pvParameters) {
  Vector3 sum(0, 0, 0);
  const int N = 600;
  for (int i = 0; i < N; i++) {
    ICM.getEvent(&accel, &gyro, &temp, &mag);
    sum = sum + Vector3(gyro.gyro.v).remapImuToBody();
    vTaskDelay(5);
  }
  calibratedBias = sum * (1.0f / N);  

  fusion.init();
  fusion.setGyroBias(calibratedBias);
  gyroCalStatus = m_CalStatus::CALIBRATED;
  UpdateStatus(m_Status::IDLE);
  vTaskDelete(NULL);
}

void Calibrate(void *pvParameters) {
  UpdateStatus(m_Status::CAL);
  Halt();
  SetMotorsEnabled(false);
  
  int calType = (int)(intptr_t)pvParameters;
  switch(calType){
    case 0:
      gyroCalStatus = m_CalStatus::RUNNING;
      magCalStatus = m_CalStatus::INIT;
      northOffset = 0;
      taskCreateTracked(calibrateGyroBias, "calibrateGyroBias", 2048, NULL, 5, NULL);      
      break;
    case 1:
      magCalStatus = m_CalStatus::RUNNING;
      taskCreateTracked(calibrateMagOffset, "calibrateMagOffset", 3072, NULL, 5, NULL);
      break;
  }  
  
  vTaskDelete(NULL);
}

void StepperLoop(void *pvParameters) {
  while (true) {
    if(M_hold) {
      AzEl pos = computeRequiredAzEl(fusion.getOrientation(), M_target[0], M_target[1], 0);
      if(pos != previousPos) {
        MoveTo(pos.az, pos.el);

        previousPos = pos;
      }
    }
    else if(M_target[0] != previousTarget[0] || M_target[1] != previousTarget[1]) {
      MoveTo(M_target[0], M_target[1]);

      previousTarget[0] = M_target[0];
      previousTarget[1] = M_target[1];
    }

    if(stepperStatus != StepperStatus::HOMING)
      stepperStatus = stepper_AZ->isRunning() || stepper_EL->isRunning() ? StepperStatus::MOVING : StepperStatus::IDLE;

    vTaskDelay(1);
  }
}

void SystemMonitor(void *pvParameters) {
  while (true) {
    vTaskDelay(pdMS_TO_TICKS(1000));

    String result, temp;
    uint32_t totalTime;
    int taskCount = uxTaskGetNumberOfTasks();
    TaskStatus_t* taskArray = new TaskStatus_t[taskCount]();

    TaskStatus_t task;
    int tasks = uxTaskGetSystemState(taskArray, taskCount, &totalTime);    

    xSemaphoreTake(taskMapMutex, portMAX_DELAY);
    for(int i = 0; i < tasks; i++) {
      task = taskArray[i];
      auto it = taskStackMap.find(task.xHandle);
      uint32_t size = (it != taskStackMap.end()) ? it->second : 0;
      uint32_t used = size ? size - task.usStackHighWaterMark : 0;
      result += "T " + String(task.pcTaskName) + " " + String(task.ulRunTimeCounter) + " " + String(task.uxCurrentPriority) + " " + String(task.eCurrentState) + " " + String(size) + " " + String(used) +"\n";
    }

    if(tasks > 0)
      for (auto it = taskStackMap.begin(); it != taskStackMap.end();) {
        bool alive = false;
        for (int i = 0; i < tasks; i++) 
          if (taskArray[i].xHandle == it->first) { alive = true; break; }
        it = alive ? std::next(it) : taskStackMap.erase(it);
      }
    xSemaphoreGive(taskMapMutex);

    transmitSystemStatus(const_cast<char*>(result.c_str()), result.length());    

    result = "# " + String(1.0f - (float)ESP.getFreeHeap() / ESP.getHeapSize()) + " " + String(totalTime) + " " + String(ESP.getCpuFreqMHz());
    transmitSystemStatus(const_cast<char*>(result.c_str()), result.length());

    delete[] taskArray;
  }
}

void StateMonitor(void *pvParameters) {
  char buffer[128];
  char* statusFormat = "%i %i %i %i %i %i %i %i %i %i %i %i %i";

  while(true) {
    laserStatus = digitalRead(LASER);

    int size = snprintf(buffer, sizeof(buffer), statusFormat, laserStatus, stepperStatus, M_hold, serialReady, switches[0].status, switches[1].status, IMUhz, camFPS, status, (int)northOffset, gyroCalStatus, magCalStatus, magCalSteps);
    transmitStatus(buffer, size);
    vTaskDelay(pdMS_TO_TICKS(200));
  }
}

void OrientationMonitor(void *pvParameters) {
  char buffer[128];
  char* orientationFormat = "%.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %d %.2f %.2f %.2f 0 %.2f %.2f 0";

  while(true) {
    Vector3 orientation = fusion.getEulerYForward_deg();

    int size = snprintf(buffer, sizeof(buffer), orientationFormat, 
      orientation.x, orientation.y, orientation.z, 
      bias.x, bias.y, bias.z, 
      gyro.gyro.v[0], gyro.gyro.v[1], gyro.gyro.v[2], 
      accel.acceleration.v[0], accel.acceleration.v[1], accel.acceleration.v[2],
      rotationEstimate.dtheta_x, rotationEstimate.dtheta_y, rotationEstimate.dtheta_z, 
      rotationEstimate.dt_seconds, rotationEstimate.inlier_count, rotationEstimate.residual_rms,
      M_target[0], M_target[1],
      stepper_AZ->getCurrentPosition() / AZ_MOD, stepper_EL->getCurrentPosition() / -EL_MOD);

    transmitOrientation(buffer, size);
    vTaskDelay(pdMS_TO_TICKS(30));
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
  serialPrintln("CamSerial: Connection opened");  
  serialPrint("CamSerial: Waiting for client");

  while(true) {
    if(!serialReady || esp_timer_get_time() > lastCamMessage + 1e6) {
      int index = -1;
      writeToSerial(serialReady ? "P" : "I", true);
      if(!serialReady)
        serialPrint("...");
      
      acknowledged = false;
      for(int i = 0; i < 1000; i++) {      
        if (camSerial.available()) {
          command = serialReader.read(index);
          
          if(command.startsWith("ACK")) {
            if(!serialReady)
              serialPrintln("\nCamSerial: Connection established");
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
        serialPrintln("CamSerial: Connection lost");
        serialPrint("CamSerial: Waiting for client");
      }
    }
    vTaskDelay(pdMS_TO_TICKS(serialReady ? 100 : 1)); 
  }
}

void SerialMonitor(void *pvParameters) {
  int index = 0;
  String command;
  std::cmatch matches;
  std::regex camPattern{R"(U (-?\d+\.\d+) (-?\d+\.\d+) (-?\d+\.\d+) (\d+\.\d+) (\d+) (\d+\.\d+))"};
  std::regex chunkPattern{R"(C (\d+) (\d+))"};

  while (true) {
    while (!(serialReady && camSerial.available())) vTaskDelay(pdMS_TO_TICKS(1));
    int64_t time = esp_timer_get_time();
    command = serialReader.read(index);
    camFPS = (int)(1 / ((time - lastCamMessage) / 1e6));
    lastCamMessage = time;

    switch(command[0]) {
      case 'P':
        writeToSerial("ACK");
        break;
      case 'U':
        if (std::regex_search(command.c_str(), matches, camPattern)) {
          rotationEstimate = RotationEstimate {
            .dtheta_x = stof(matches[3].str()),
            .dtheta_y = stof(matches[1].str()),
            .dtheta_z = stof(matches[2].str()),
            .dt_seconds = stof(matches[4].str()),
            .inlier_count = stof(matches[5].str()),
            .residual_rms = stof(matches[6].str()),
          };
          
          xQueueOverwrite(estimateQueue, &rotationEstimate);
        }
        break;
      case 'C':
        if (std::regex_search(command.c_str(), matches, chunkPattern)) {
          chunkCount = stoi(matches[1].str());
          frameSize = stoi(matches[2].str());
          currentChunk = 0;
          totalSent = 0;
          taskCreateTracked(ProcessFrameMonitor, "ProcessFrameMonitor", 8192, NULL, 5, NULL);
        }
        break;
      case 'F':
        String *copy = new String(command);
        if(taskCreateTracked(ProcessFrame, "ProcessFrame", 8192, copy, 5, &currentFrameProcessor) != pdPASS) 
         delete copy;
        break;
    }
  }
}

void startFrameProcessing() {
  if(BLEMtu < 70)
    serialPrintln("ProcessFrame Failed: MTU too small");

  if(currentFrameProcessor == 0)
    writeToSerial("F");
}

void ProcessFrame(void *pvParameters) {
  try {
    String *arg = (String *) pvParameters;
    String command = *arg;
    delete arg;

    size_t size;
    uint8_t data[command.length() / 4 * 3 + 3];

    decode(command.c_str() + 1, command.length() - 1, data, size);
    if(size != chunkSize) {
      vTaskDelay(1);
      writeToSerialf("F %i", currentChunk);
      serialPrintln("ProcessFrame Failed: Invalid chunk size");
    }
    else {
      lastFrame = esp_timer_get_time();

      int transmitSize = min((int)size, frameSize - totalSent);
      transmitFrameChunk((char *)data, currentChunk, transmitSize);
      totalSent += transmitSize;

      currentChunk++;
      if(currentChunk < chunkCount)
        writeToSerialf("F %i", currentChunk);
    }
  }
  catch(const std::exception& e) {
    char buffer[128];
    snprintf(buffer, 128, "ProcessFrame Failed: %s", e.what());
    serialPrintln(buffer);
  }

  vTaskDelete(NULL);
}

void ProcessFrameMonitor(void *pvParameters) {
  char buffer[16];
  snprintf(buffer, 16, "F %i %i", chunkSize, frameSize);
  transmitFrame(buffer, 16);

  while(currentChunk < chunkCount) {
    if (currentFrameProcessor == 0) {
      writeToSerialf("F %i", currentChunk);
    }
    // else if((esp_timer_get_time() - lastFrame) > 5e6) {
    //   vTaskDelete(currentFrameProcessor);
    // }
    vTaskDelay(100);
  }

  currentFrameProcessor = NULL;
  vTaskDelete(NULL);
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

    fusion.predict(Vector3(gyro.gyro.v).remapImuToBody(), dt);
    fusion.updateAccel(Vector3(accel.acceleration.v).remapImuToBody());

    RPY = fusion.getEulerRPY_deg();
    bias = fusion.getGyroBias();

    lastGyroStateUpdate = esp_timer_get_time();
    writeToSerialf("G %2.2f %2.2f %2.2f", RPY.x, RPY.y, RPY.z);

    vTaskDelay(pdMS_TO_TICKS(1));
  }
}

void UpdateStatus(m_Status s) {
  status = s;

  switch (status) {
    case m_Status::INIT:
      SetLEDs((int[]){ 255, 0, 0 }, 1, 0);
      break;
    case m_Status::PAIRING:
      SetLEDs((int[]){ 0, 0, 100 }, 500, 500);
      break;
    case m_Status::PAIRED:
      SetLEDs((int[]){ 0, 0, 100 }, 1, 0);
      break;
    case m_Status::TRACKING:
      SetLEDs((int[]){ 0, 90, 0 }, 1, 0);
      break;
    case m_Status::IDLE:
      SetLEDs((int[]){ 180, 90, 0 }, 1, 0);
      break;
    case m_Status::CAL:
      SetLEDs((int[]){ 180, 10, 0 }, 100, 100);
      break;
    default:
      break;
  }
}

void SearchForICM() {
  serialPrintln("Searching for ICM...");

  Wire.begin(ACC_SDA, ACC_SCL);
  byte error, address;
  for(address = 1; address < 127; address++ ) {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();
    if (error == 0) {
      char buffer[64];
      snprintf(buffer, 64, "I2C device found at address 0x%x", address);
      serialPrintln(buffer);

      if(ICM.begin_I2C(address)) {
        serialPrintln("Paired ICM");
        return;
      }
    }
  }
  serialPrint("Unable to find ICM, Halting");
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

void serialPrint(char* data) {
  Serial.print(data);
  char * heapRef = new char[strlen(data) + 1];
  strcpy(heapRef, data);
  if(xQueueSendToBack(bleSerialCommQueue, &heapRef, 10) != pdTRUE)
    delete[] heapRef;
}

void serialPrint(String data) {
  Serial.print(data);
  char * heapRef = new char[data.length() + 1];
  strcpy(heapRef, data.c_str());
  if(xQueueSendToBack(bleSerialCommQueue, &heapRef, 10) != pdTRUE)
    delete[] heapRef;
}

void serialPrintln(char* data) {
  Serial.println(data);
  String d = String(data) + "\n";
  char * heapRef = new char[d.length() + 1];
  strcpy(heapRef, d.c_str());
  if(xQueueSendToBack(bleSerialCommQueue, &heapRef, 10) != pdTRUE)
    delete[] heapRef;
}

void serialPrintln(String data) {
  Serial.println(data);
  String d = data + "\n";
  char * heapRef = new char[d.length() + 1];
  strcpy(heapRef, d.c_str());
  if(xQueueSendToBack(bleSerialCommQueue, &heapRef, 10) != pdTRUE)
    delete[] heapRef;
}
