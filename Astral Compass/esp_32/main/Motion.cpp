#include <main.h>
#include <Motion.h>

FastAccelStepperEngine engine = FastAccelStepperEngine();
FastAccelStepper *stepper_AZ = NULL, *stepper_EL = NULL;
LimitSwitch switches[2] = { { 0, LM1, 0 }, { 0, LM2, 0 } };
float M_target[2] = { 0, 0 };
StepperStatus stepperStatus = StepperStatus::INIT;
bool M_hold = false;

FastAccelStepper *InitStepper(int step, int dir, int enable, int steps, int accel) {
  auto *stepper = engine.stepperConnectToPin(step);
  if (stepper == NULL) {
    Serial.printf("ERROR: Failed to connect stepper to pin [%i]\n", step);
  } else {
    stepper->setEnablePin(enable);
    stepper->setDirectionPin(dir);
    stepper->setAutoEnable(true);
    stepper->setSpeedInUs(steps);
    stepper->setAcceleration(accel);
  }
  return stepper;
}

void IRAM_ATTR LimitSwitchEvent(void *arg) {
  LimitSwitch &lswitch = switches[(int)(intptr_t)arg];
  TickType_t now = xTaskGetTickCountFromISR();

  if (now - lswitch.lastEvent > pdMS_TO_TICKS(50)) {
    lswitch.lastEvent = now;
    lswitch.status = !((bool)digitalRead(lswitch.pin));
    Serial.printf("limit swtich %i\n", (int)(intptr_t)arg);
  }
}

void InitSteppers() {
  engine.init();
  stepper_AZ = InitStepper(S1_STEP, S1_DIR, S1_EN, 30, 1000000);
  stepper_EL = InitStepper(S2_STEP, S2_DIR, S2_EN, 10, 1000000);
}

void InitInterrupts() {
  attachInterruptArg(LM1, LimitSwitchEvent, (void *)0, CHANGE);
  attachInterruptArg(LM2, LimitSwitchEvent, (void *)1, CHANGE);

  switches[0].status = !((bool)digitalRead(LM1));
  switches[1].status = !((bool)digitalRead(LM2));
}

void Home() {
  stepperStatus = StepperStatus::HOMING;
  PointTo(0, 0);
}

void PointTo(float az, float el) {
  if(stepperStatus == StepperStatus::HOMING || status == Status::CAL) return;

  M_target[0] = fmod(fmod(round(az * 100) / 100, 360.0f) + 360, 360.0f);
  M_target[1] = max(min(round(el * 100) / 100, 90.0f), 0.0f);
}

void MoveTo(float az, float el) {
  if(stepperStatus == StepperStatus::HOMING || status == Status::CAL) return;

  int32_t pos = stepper_AZ->getCurrentPosition() / AZ_MOD;
  int diff = az - (pos % 360);
  int sign = diff == 0 ? 1 : abs(diff) / diff;
  int nsign = -1 * sign;
  az = min(abs(diff) <= 180 ? diff : (360 - abs(diff)) * nsign, 360) + pos;

  stepper_AZ->moveTo(az * AZ_MOD);
  stepper_EL->moveTo(el * -EL_MOD);
}

void Halt() {
  stepper_AZ->forceStop();
  stepper_EL->forceStop();
}

void SetMotorsEnabled(bool enabled) {
  if(enabled) {
    stepper_AZ->enableOutputs();
    stepper_EL->enableOutputs();
    stepperStatus = StepperStatus::IDLE;
  }
  else {
    stepper_AZ->disableOutputs();
    stepper_EL->disableOutputs();
    stepperStatus = StepperStatus::S_DISABLED;
  }
}

void SetHoldPosition(bool enabled) {
  M_hold = enabled;
}