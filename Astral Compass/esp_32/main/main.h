#pragma once

#include <Arduino.h>

#define LED_R 18
#define LED_G 19
#define LED_B 21
#define LED_STATUS 2
#define S1_EN 12
#define S1_DIR 26
#define S1_STEP 15
#define S2_EN 13
#define S2_DIR 25
#define S2_STEP 4
#define ACC_SCL 22
#define ACC_SDA 23
#define LASER 5
#define LM1 14
#define LM2 27
#define SERIAL_TX 17
#define SERIAL_RX 16


enum class m_Status {
    INIT, PAIRING, PAIRED, TRACKING, IDLE, CAL
};

volatile extern m_Status status;
extern float northOffset;

void UpdateStatus(m_Status s);
void BlinkStatusLED();
void Calibrate(void *pvParameters);

class SharedReader {
public: 
    SharedReader(HardwareSerial& s)
        : serial(s), index(0), length(10), serialMutex(xSemaphoreCreateMutex())
    {
        buffer = new String[length]();
    }

    ~SharedReader() {
        delete[] buffer;
    }

    String read(int &i) {        
        if (xSemaphoreTake(serialMutex, pdMS_TO_TICKS(100)) != pdTRUE) {
            return "";
        }

        String value;
        if(i == -1)
            i = index;

        if(i == index) {
            value = serial.readStringUntil('\n');
            if(value.length() == 0) {
                xSemaphoreGive(serialMutex);
                return value;
            }

            buffer[index] = value;
            index = ++index % length;
        }
        
        value = buffer[i];
        i = ++i % length;
        xSemaphoreGive(serialMutex);
        return value;
    }
private:
    int index, length;
    String* buffer;
    HardwareSerial& serial;
    QueueHandle_t serialMutex;
};