#pragma once

struct LimitSwitch {
 bool status;
 int pin;
 long lastEvent;
};

enum class StepperStatus {
    INIT, HOMING, IDLE, MOVING
};

void InitSteppers();
void InitInterrupts();
void StepperLoop(void *pvParameters);
void Home();
void Point(float az, float el);
void UpdateOrientation(float angles[]);