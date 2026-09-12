#include <Arduino.h>
#include <esp_camera.h>
#include <cam.h>
#include <opticalTracker.h>
#include <regex>

HardwareSerial mainSerial(2);
SharedReader serialReader(mainSerial);
OpticalRotationTracker tracker;
QueueHandle_t serialMutex = xSemaphoreCreateMutex();
QueueHandle_t blinkQueue = xQueueCreate(1, sizeof(int));
volatile float gyroIntegratedThetaX = 0, gyroIntegratedThetaY = 0, gyroIntegratedThetaZ = 0;
volatile bool serialReady = false;
volatile int64_t lastMainMessage;

bool writeToSerial(const char* str, bool force = false);

void setup() {
  Serial.begin(115200);
  Serial.println("\nSerial Connected");

  pinMode(LED_STATUS, OUTPUT);
  digitalWrite(LED_STATUS, HIGH);

  camera_config_t config = {
      .pin_pwdn       = -1,
      .pin_reset      = -1,
      .pin_xclk       = 10,
      .pin_sccb_sda   = 40,
      .pin_sccb_scl   = 39,
      .pin_d7         = 48,
      .pin_d6         = 11,
      .pin_d5         = 12,
      .pin_d4         = 14,
      .pin_d3         = 16,
      .pin_d2         = 18,
      .pin_d1         = 17,
      .pin_d0         = 15,
      .pin_vsync      = 38,
      .pin_href       = 47,
      .pin_pclk       = 13,

      .xclk_freq_hz   = 20000000,
      .pixel_format   = PIXFORMAT_GRAYSCALE, // The pixel format of the image: PIXFORMAT_ + YUV422|GRAYSCALE|RGB565|JPEG
      .frame_size     = FRAMESIZE_QQVGA, // The resolution size of the image: FRAMESIZE_ + QVGA|CIF|VGA|SVGA|XGA|SXGA|UXGA
      .fb_count       = 2,
      .fb_location    = CAMERA_FB_IN_PSRAM,            
      .grab_mode      = CAMERA_GRAB_LATEST
  };

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  Serial.println("Camera Initialized");

  xTaskCreate(SerialConnectionMonitor, "SerialConnectionMonitor", 8192, NULL, 1, NULL);  
  xTaskCreate(SerialMonitor, "SerialMonitor", 8192, NULL, 1, NULL);  
  xTaskCreate(TrackerLoop, "TrackerLoop", 16384, NULL, 1, NULL);  
  xTaskCreate(ProcessStatusLED, "ProcessStatusLED", 1024, NULL, 1, NULL);
  Serial.println("Threads Initialized");
}

void loop() {
  delay(1000);  
}

void Reset() {
  gyroIntegratedThetaX = 0, gyroIntegratedThetaY = 0, gyroIntegratedThetaZ = 0;
  tracker.init();
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

void TrackerLoop(void *pvParameters) {
  tracker.init();

  while (true) {
    vTaskDelay(1);    

    if(!serialReady) continue;

    tracker.setGyroPrediction(gyroIntegratedThetaX, gyroIntegratedThetaY, gyroIntegratedThetaZ);
    RotationEstimate est = tracker.update();
    
    if (est.valid) {
      writeToSerialf("U %.5f %.5f %.5f %.4f %d %.5f", est.dtheta_x, est.dtheta_y, est.dtheta_z, est.dt_seconds, est.inlier_count, est.residual_rms);
      //Serial.printf("dtheta=[%.5f, %.5f, %.5f] rad  dt=%.4f s  inliers=%d  rms=%.5f\n", est.dtheta_x, est.dtheta_y, est.dtheta_z, est.dt_seconds, est.inlier_count, est.residual_rms);      
    } else {
      writeToSerial("E Insufficient tracks");
      //Serial.println("Insufficient tracks");
    }
  }
}

void SerialConnectionMonitor(void *pvParameters) {
  String command;
  bool acknowledged;

  mainSerial.begin(115200, SERIAL_8N1, SERIAL_RX, SERIAL_TX, false, 1000);
  Serial.println("Serial: connection opened");
  Serial.println("Serial: waiting for host");

  while (true) {
    if(esp_timer_get_time() > lastMainMessage + 1e6) {
      int index = -1;
      writeToSerial("P");

      acknowledged = false;      
      for(int i = 0; i < 1000; i++) {      
        if (mainSerial.available() > 0) {          
          command = serialReader.read(index);

          if(command.startsWith("ACK")) {
            acknowledged = true;
            lastMainMessage = esp_timer_get_time();
            break;
          }
        }        
        vTaskDelay(1); 
      }

      if(!acknowledged) {
        serialReady = false;
        Serial.println("Serial: connection lost");
      }
    }

    vTaskDelay(pdMS_TO_TICKS(1000)); 
  }
}

void SerialMonitor(void *pvParameters) {
  int index = 0;
  String command;
  std::cmatch matches;
  std::regex gyroPattern("G (\d+\.\d+) (\d+\.\d+) (\d+\.\d+)");   

  while (true) {
    while (!mainSerial.available()) vTaskDelay(1);
    command = serialReader.read(index);
    lastMainMessage = esp_timer_get_time();

    BlinkStatusLED();

    switch(command[0]) {
      case 'I':
        serialReady = false;
        writeToSerial("ACK", true);
        Serial.println("Serial: connection established");
        serialReady = true;
        Reset();
        break;
      case 'P':
        writeToSerial("ACK");
        break;
      case 'G':
        if (std::regex_search(command.c_str(), matches, gyroPattern)) {
          gyroIntegratedThetaX = stof(matches[1].str());
          gyroIntegratedThetaY = stof(matches[2].str()); 
          gyroIntegratedThetaZ = stof(matches[3].str());          
        }
        break;
    }
  }
}

bool writeToSerial(const char* str, bool force) {
  if(!(serialReady || force)) return false;

  if (xSemaphoreTake(serialMutex, pdMS_TO_TICKS(100)) != pdTRUE) {
    return false;
  }

  mainSerial.println(str);
  xSemaphoreGive(serialMutex);
  BlinkStatusLED();
  return true;
}

bool writeToSerialf(const char * format, ...) { 
  if(!serialReady) return false;

  if (xSemaphoreTake(serialMutex, pdMS_TO_TICKS(100)) != pdTRUE) {
    return false;
  }

  va_list args;
  va_start(args, format);

  mainSerial.vprintf(format, args);
  mainSerial.println();
  va_end(args);
  xSemaphoreGive(serialMutex);
  BlinkStatusLED();
  return true;
}
