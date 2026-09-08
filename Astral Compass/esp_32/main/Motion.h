#pragma once

struct LimitSwitch {
 bool status;
 int pin;
 long lastEvent;
};

enum class StepperStatus {
    INIT, HOMING, IDLE, MOVING
};

extern LimitSwitch switches[];
extern float MotionPosition[];
extern StepperStatus stepperStatus;

void InitSteppers();
void InitInterrupts();
void StepperLoop(void *pvParameters);
void Home();
void PointTo(float az, float el);
void UpdateOrientation(float angles[]);
void SetEnabled(bool enabled);