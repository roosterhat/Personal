#pragma once

#include <Arduino.h>

#define LED_R 18
#define LED_G 19
#define LED_B 21
#define S1_EN 12
#define S1_DIR 2
#define S1_STEP 15
#define S2_EN 13
#define S2_DIR 4
#define S2_STEP 16
#define ACC_SCL 22
#define ACC_SDA 23
#define LASER 5
#define LM1 14
#define LM2 27
#define SERIAL_TX 1
#define SERIAL_RX 3

enum class Status {
    INIT, HOMING, PAIRING, PAIRED, TRACKING, IDLE
};

volatile extern Status status;
volatile extern int64_t currentTick, previousTick; 