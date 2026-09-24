#pragma once

struct LimitSwitch {
 bool status;
 int pin;
 long lastEvent;
};

enum class StepperStatus {
    INIT, HOMING, IDLE, MOVING, S_DISABLED
};

extern LimitSwitch switches[];
extern float M_target[], M_position[];
extern StepperStatus stepperStatus;
extern bool M_hold;

void InitSteppers();
void InitInterrupts();
void StepperLoop(void *pvParameters);
void Home();
void PointTo(float az, float el);
void UpdateOrientation(float angles[]);
void SetMotorsEnabled(bool enabled);
void SetHoldPosition(bool enabled);