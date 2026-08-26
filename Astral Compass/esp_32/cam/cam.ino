#include <Arduino.h>
#include <esp_camera.h>
#include <cam.h>
#include <opticalTracker.h>
#include <regex>

HardwareSerial mainSerial(2);
OpticalRotationTracker tracker;
volatile float gyroIntegratedThetaX = 0, gyroIntegratedThetaY = 0, gyroIntegratedThetaZ = 0;
volatile bool serialReady = false;

void setup() {
  Serial.begin(115200);
  Serial.println("\nSerial Connected");

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
      .grab_mode      = CAMERA_GRAB_WHEN_EMPTY
  };

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  Serial.println("Camera Initialized");

  xTaskCreate(SerialMonitor, "SerialMonitor", 8192, NULL, 1, NULL);  
  xTaskCreate(TrackerLoop, "TrackerLoop", 1048576, NULL, 1, NULL);  
}

void loop() {
}

void TrackerLoop(void *pvParameters) {
  tracker.init();

  while (true) {
    tracker.setGyroPrediction(gyroIntegratedThetaX, gyroIntegratedThetaY, gyroIntegratedThetaZ);
    RotationEstimate est = tracker.update();
    
    if (est.valid) {
      writeToSerialf("U %.5f %.5f %.5f %.4f %d %.5f", est.dtheta_x, est.dtheta_y, est.dtheta_z, est.dt_seconds, est.inlier_count, est.residual_rms);
      Serial.printf("dtheta=[%.5f, %.5f, %.5f] rad  dt=%.4f s  inliers=%d  rms=%.5f\n", est.dtheta_x, est.dtheta_y, est.dtheta_z, est.dt_seconds, est.inlier_count, est.residual_rms);      
    } else {
      Serial.println("Insufficient tracks");
    }
  }
}

void SerialMonitor(void *pvParameters) {
  std::cmatch matches;
  std::regex gyroPattern("G (\d+\.\d+) (\d+\.\d+) (\d+\.\d+)"); 

  mainSerial.begin(38400, SERIAL_8N1, SERIAL_RX, SERIAL_TX, false, 1000);
  Serial.println("Serial: connection opened");
  Serial.println("Serial: waiting for host");

  while (true) {
    while (mainSerial.available() == 0){}
    String command = mainSerial.readString();

    switch(command[0]) {
      case 'I':
        while(mainSerial.availableForWrite() == 0) {}
        mainSerial.print("ACK");
        Serial.println("Serial: connection established");
        serialReady = true;
        break;
      case 'P':
        while(mainSerial.availableForWrite() == 0) {}
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

bool writeToSerial(const char str[]) {
  if(!serialReady) return false;

  for(int i = 0; i < 100; i++) {
    if(mainSerial.availableForWrite() > 0) {
      mainSerial.print(str);
      return true;
    }
  }

  return false;
}

bool writeToSerialf(const char * format, ...) { 
  if(!serialReady) return false;

  va_list args;
  for(int i = 0; i < 100; i++) {
    if(mainSerial.availableForWrite() > 0) {
      mainSerial.printf(format, args);
      return true;
    }
  }

  return false;
}
