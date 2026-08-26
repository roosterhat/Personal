#include <main.h>
#include <Motion.h>
#include <FastAccelStepper.h>

FastAccelStepperEngine engine = FastAccelStepperEngine();
FastAccelStepper *stepper_AZ = NULL, *stepper_EL = NULL;
LimitSwitch switches[] = { { 0, LM1, 0 }, { 0, LM2, 0 } };
float target[] = { 0, 0 }, position[] = { 0, 0 }, orientation[] = { 0, 0, 0 };
StepperStatus stepperStatus = StepperStatus::INIT;

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
  }
}

void InitSteppers() {
  engine.init();
  stepper_EL = InitStepper(S1_STEP, S1_DIR, S1_EN, 30, 100000);
  stepper_AZ = InitStepper(S2_STEP, S2_DIR, S2_EN, 30, 100000);
}

void InitInterrupts() {
  attachInterruptArg(LM1, LimitSwitchEvent, (void *)0, CHANGE);
  attachInterruptArg(LM2, LimitSwitchEvent, (void *)1, CHANGE);

  switches[0].status = !((bool)digitalRead(LM1));
  switches[1].status = !((bool)digitalRead(LM2));
}

void Home() {
  stepperStatus = StepperStatus::HOMING;
  Point(0, 0);
}

void Point(float az, float el) {
  target[0] = az;
  target[1] = el;
}

void UpdateOrientation(float angles[]) {
  std::copy(angles, angles + 3, orientation);
}

void StepperLoop(void *pvParameters) {
  while (true) {
    Serial.println("Step");
    //stepper_AZ->move(100000, true);
    stepper_AZ->runForward();
  }
}