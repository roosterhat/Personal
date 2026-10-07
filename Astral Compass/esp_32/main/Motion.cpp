#include <main.h>
#include <Motion.h>
#include <Utils.h>

FastAccelStepperEngine engine = FastAccelStepperEngine();
FastAccelStepper *stepper_AZ = NULL, *stepper_EL = NULL;
LimitSwitch switches[2] = { { 0, LM1, 0, stepper_AZ }, { 0, LM2, 0, stepper_EL } };
float M_target[2] = { 0, 0 };
StepperStatus stepperStatus = StepperStatus::INIT;
bool M_hold = false;
SemaphoreHandle_t homingSemaphore = NULL;
QueueHandle_t limitQueue = xQueueCreate(32, sizeof(int));

FastAccelStepper *InitStepper(int step, int dir, int enable, int steps, int accel) {
  auto *stepper = engine.stepperConnectToPin(step);
  if (stepper == NULL) {
    char buffer[64];
    snprintf(buffer, 64, "ERROR: Failed to connect stepper to pin [%i]\n", step);
    serialPrintln(buffer);
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
  int index = (int)(intptr_t)arg;
  LimitSwitch &lswitch = switches[index];
  TickType_t now = xTaskGetTickCountFromISR();

  if (now - lswitch.lastEvent > pdMS_TO_TICKS(50)) {
    lswitch.lastEvent = now;
    lswitch.status = !((bool)digitalRead(lswitch.pin));

    BaseType_t woken = pdFALSE;
    xQueueSendFromISR(limitQueue, &index, &woken);
    if (woken) portYIELD_FROM_ISR();
  }
}

void limitTask(void *pvParameters) {
  int index;
  while (true) {
    if (xQueueReceive(limitQueue, &index, portMAX_DELAY) == pdTRUE) {
      LimitSwitch &lswitch = switches[index];
      if (stepperStatus == StepperStatus::HOMING && lswitch.status)
        lswitch.stepper->forceStop();
      Serial.printf("limit switch: %i\n", index);
    }
    vTaskDelay(1);
  }
}

void InitSteppers() {
  engine.init();
  stepper_AZ = InitStepper(S1_STEP, S1_DIR, S1_EN, 30, 1000000);
  stepper_EL = InitStepper(S2_STEP, S2_DIR, S2_EN, 10, 70000);
  switches[0].stepper = stepper_AZ;
  switches[1].stepper = stepper_EL;

  xTaskCreate(limitTask, "limitTask", 4096, NULL, 10, NULL);
}

void InitInterrupts() {
  attachInterruptArg(LM1, LimitSwitchEvent, (void *)0, CHANGE);
  attachInterruptArg(LM2, LimitSwitchEvent, (void *)1, CHANGE);

  switches[0].status = !((bool)digitalRead(LM1));
  switches[1].status = !((bool)digitalRead(LM2));
}

void HomeAxis_AZ(void *pvParameters) {
  if(!switches[0].status) {
    uint32_t speed = stepper_AZ->getSpeedInUs();
    stepper_AZ->setSpeedInHz(10000);
    stepper_AZ->runForward();

    while(stepper_AZ->isRunning())
      vTaskDelay(1);

    stepper_AZ->move(AZ_MOD, true);
    stepper_AZ->setSpeedInUs(speed);
  }

  xSemaphoreGive(homingSemaphore);
  vTaskDelete(NULL);
}

void HomeAxis_EL(void *pvParameters) {
  if(!switches[1].status) {
    uint32_t speed = stepper_EL->getSpeedInUs();  
    stepper_EL->setSpeedInHz(2000);
    stepper_EL->runForward();

    while(stepper_EL->isRunning())
      vTaskDelay(1);

    stepper_EL->move(EL_MOD, true);
    stepper_EL->setSpeedInUs(speed);
  }

  xSemaphoreGive(homingSemaphore);
  vTaskDelete(NULL);
}

void Home(void *pvParameters) {
  if(stepperStatus == StepperStatus::HOMING || status == m_Status::CAL) {
    vTaskDelete(NULL);
    return;
  }

  stepperStatus = StepperStatus::HOMING;  
  homingSemaphore = xSemaphoreCreateCounting(2, 0);
  xTaskCreate(HomeAxis_AZ, "HomeAxis_AZ", 4096, NULL, 1, NULL);
  xTaskCreate(HomeAxis_EL, "HomeAxis_EL", 4096, NULL, 1, NULL);

  for(int i = 0; i < 2; i++)
    xSemaphoreTake(homingSemaphore, portMAX_DELAY);

  vTaskDelay(1);
  M_target[0] = 0; M_target[1] = 0;
  stepper_AZ->setCurrentPosition(0);
  stepper_EL->setCurrentPosition(0);

  stepperStatus = StepperStatus::IDLE;
  vSemaphoreDelete(homingSemaphore);
  vTaskDelete(NULL);
}

void PointTo(float az, float el) {
  if(stepperStatus == StepperStatus::HOMING || status == m_Status::CAL) return;

  M_target[0] = az;
  M_target[1] = el;
}

void MoveTo(float az, float el) {
  if(stepperStatus == StepperStatus::HOMING || status == m_Status::CAL) return;

  boundAZEL(az, el);

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