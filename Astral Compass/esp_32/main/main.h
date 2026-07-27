/*
LED R     RX0
LED G     D19
LED B     D18
S1 DIR    D15
S1 STEP   D2
S2 DIR    D4
S2 STEP   RX2
ACC SCL   D22
ACC SDA   D21
Laser     D5
LM1       TX0
LM2       D23
*/

#pragma once

#include <Arduino.h>

#define LED_R 3
#define LED_G 19
#define LED_B 18
#define S1_DIR 15
#define S1_STEP 2
#define S2_DIR 4
#define S2_STEP 16
#define ACC_SCL 22
#define ACC_SDA 21
#define LASER 5
#define LM1 35
#define LM2 33

typedef enum {
    INIT, HOMING, CAL, PAIRING, PAIRED, TRACKING, IDLE
} Status;

volatile extern Status status;
volatile extern int64_t currentTick, previousTick;


 