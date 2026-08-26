#include <main.h>
#include <esp_timer.h>
#include <LED.h>
#include <Adafruit_ICM20X.h>
#include <Adafruit_ICM20948.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <math.h>
#include <Fusion.h>
#include <Motion.h>
#include <regex>

volatile int64_t previousTick = 0, currentTick = 1, lastICMPoll;
volatile bool serialReady = false;
double vel[] = {0,0,0}, pos[] = {0,0,0}, angle[] = {0,0,0}, northOffset = 0;
volatile Status status = Status::INIT;
Adafruit_ICM20948 ICM;
sensors_event_t accel, gyro, temp, mag;
ImuFusion fusion(0.08);
FusionOutput currentState;
AutoAccelCalibrator autoCal;
AccelCalibration calibration;
HardwareSerial camSerial(2);

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

  xTaskCreate(SerialMonitor, "SerialMonitor", 8192, NULL, 1, NULL);  
  //xTaskCreate(ProcessICMUpdates, "ProcessICMUpdates", 4096, NULL, 1, NULL);
  xTaskCreate(StepperLoop, "StepperLoop", 4096, NULL, 1, NULL);
  xTaskCreate(ProcessLEDs, "ProcessLEDs", 1024, NULL, 1, NULL);
  Serial.println("Threads Initialized");  

  Serial.println("Calibrating...");
  UpdateStatus(Status::PAIRING);    
}

void loop() {
  previousTick = currentTick;
  currentTick = esp_timer_get_time();
}

void SerialMonitor(void *pvParameters) {
  std::cmatch matches;
  std::regex camPattern("U (\d+\.\d+) (\d+\.\d+) (\d+\.\d+) (\d+\.\d+) (\d+) (\d+\.\d+)"); 

  camSerial.begin(38400, SERIAL_8N1, SERIAL_RX, SERIAL_TX, false, 1000);
  Serial.println("Serial: connection opened");

  while(camSerial.availableForWrite() == 0) {}
  camSerial.print("I");
  Serial.println("Serial: waiting for client");

  while (true) {
    while (camSerial.available() == 0){} 
    String command = camSerial.readString();

    if(command == "ACK") {
      Serial.println("Serial: connection established");
      serialReady = true;
      break;
    }
  }

  while (true) {
    while (camSerial.available() == 0){}
    String command = camSerial.readString();

    switch(command[0]) {
      case 'P':
        writeToSerial("ACK");
        break;
      case 'U':
        if (std::regex_search(command.c_str(), matches, camPattern)) {
                  
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
  while(true) {
    if(status == Status::CAL) {
      Calibrate();
    } 
    else {
      UpdateICMData();
    }    
    vTaskDelay(pdMS_TO_TICKS(1));
  }
}

void BlinkOnFaceCalibrated(void *pvParameters) {
  SetLEDs((int[]){ 0, 255, 0 }, 100, 100);
  vTaskDelay(pdMS_TO_TICKS(250));
  UpdateStatus(Status::CAL);
  vTaskDelete(NULL);
}

void Calibrate() {
  ICM.getEvent(&accel, &gyro, &temp, &mag);
  AutoAccelCalibrator::Face f = autoCal.addSample(Vector3(gyro.gyro.v), Vector3(accel.acceleration.v));
  if(f != AutoAccelCalibrator::Face::NONE)
    xTaskCreate(BlinkOnFaceCalibrated, "Blink", 1024, NULL, 1, NULL);

  if(autoCal.ready()) {
    calibration = autoCal.compute();
    fusion.setAccelCalibration(calibration);
    UpdateStatus(Status::IDLE);
    Serial.println("Calibration complete");
    UpdateICMData();
    //Serial.printf("[%2.2f,%2.2f,%2.2f], [%2.2f,%2.2f,%2.2f]\n", calibration.bias.x, calibration.bias.y, calibration.bias.z, calibration.scale.x, calibration.scale.y, calibration.scale.z);
  }
}

void UpdateICMData() {
  ICM.getEvent(&accel, &gyro, &temp, &mag);
  double dt = (currentTick - lastICMPoll) / 1e6;
  lastICMPoll = currentTick;

  currentState = fusion.update(Vector3(gyro.gyro.v), Vector3(accel.acceleration.v), Vector3(mag.magnetic.v), dt);
  Serial.printf("[%2.2f,%2.2f,%2.2f], [%2.2f,%2.2f,%2.2f], %2.2f\n", currentState.position.x, currentState.position.y, currentState.position.z, currentState.angle.x, currentState.angle.y, currentState.angle.z, Vector3(mag.magnetic.v).norm());
}

void UpdateStatus(Status s) {
  status = s;

  switch (status) {
    case Status::INIT:
      SetLEDs((int[]){ 255, 0, 0 }, 1, 0);
      break;
    case Status::CAL:
      SetLEDs((int[]){ 255, 50, 0 }, 250, 250);
      break;
    case Status::PAIRING:
      SetLEDs((int[]){ 0, 0, 100 }, 500, 500);
      break;
    case Status::PAIRED:
      SetLEDs((int[]){ 0, 0, 100 }, 1, 0);
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

void writeToSerial(const char str[]) {
  if(!serialReady) return;

  while(camSerial.availableForWrite() == 0) {}
  camSerial.print(str);
}

void writeToSerialf(const char * format, ...) {
  va_list args;

  if(!serialReady) return;

  while(camSerial.availableForWrite() == 0) {}
  camSerial.printf(format, args);
}
