#pragma once

#define SERIAL_TX  43
#define SERIAL_RX  44
#define LED_STATUS LED_BUILTIN

class SharedReader {
public: 
     SharedReader(HardwareSerial& s)
        : serial(s), index(0), length(32), serialMutex(xSemaphoreCreateMutex())
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