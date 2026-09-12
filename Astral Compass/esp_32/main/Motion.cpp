#include <main.h>
#include <Motion.h>
#include <FastAccelStepper.h>

FastAccelStepperEngine engine = FastAccelStepperEngine();
FastAccelStepper *stepper_AZ = NULL, *stepper_EL = NULL;
LimitSwitch switches[2] = { { 0, LM1, 0 }, { 0, LM2, 0 } };
float target[2] = { 0, 0 }, MotionPosition[2] = { 0, 0 }, orientation[3] = { 0, 0, 0 };
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
    Serial.printf("limit swtich %i\n", (int)(intptr_t)arg);
  }
}

void InitSteppers() {
  engine.init();
  stepper_AZ = InitStepper(S1_STEP, S1_DIR, S1_EN, 30, 100000);
  stepper_EL = InitStepper(S2_STEP, S2_DIR, S2_EN, 10, 100000);
}

void InitInterrupts() {
  attachInterruptArg(LM1, LimitSwitchEvent, (void *)0, CHANGE);
  attachInterruptArg(LM2, LimitSwitchEvent, (void *)1, CHANGE);

  switches[0].status = !((bool)digitalRead(LM1));
  switches[1].status = !((bool)digitalRead(LM2));
}

void StepperLoop(void *pvParameters) {
  while (true) {
    //Serial.println("Step");
    //stepper_EL->move(10000, true);
    //vTaskDelay(pdMS_TO_TICKS(500));
    //stepper_EL->move(-10000, true);
    
    //stepper_AZ->runForward();
    vTaskDelay(pdMS_TO_TICKS(500));
  }
}

void Home() {
  stepperStatus = StepperStatus::HOMING;
  PointTo(0, 0);
}

void PointTo(float az, float el) {
  target[0] = az;
  target[1] = el;
}

void UpdateOrientation(float angles[]) {
  std::copy(angles, angles + 3, orientation);
}

void SetEnabled(bool enabled) {
  if(enabled) {
    stepper_AZ->enableOutputs();
    stepper_EL->enableOutputs();
  }
  else {
    stepper_AZ->disableOutputs();
    stepper_EL->disableOutputs();
  }
}