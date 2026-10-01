#pragma once

#include <FastAccelStepper.h>

#define AZ_MOD 616.0
#define EL_MOD 87.0


struct LimitSwitch {
 bool status;
 int pin;
 long lastEvent;
};

enum class StepperStatus {
    INIT, HOMING, IDLE, MOVING, S_DISABLED
};

extern LimitSwitch switches[];
extern float M_target[];
extern StepperStatus stepperStatus;
extern bool M_hold;
extern FastAccelStepper *stepper_AZ, *stepper_EL;

void InitSteppers();
void InitInterrupts();
void Home();
void PointTo(float az, float el);
void MoveTo(float az, float el);
void SetMotorsEnabled(bool enabled);
void SetHoldPosition(bool enabled);
void Halt();