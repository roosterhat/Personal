#pragma once

#include <NimBLEDevice.h>

#define SERVICE_UUID      "53c3b368-70eb-4855-be6d-dfb7ce0cc4e3"
#define COMMAND_UUID      "9ea191cb-0fe6-4ad9-adfd-cf86f43a0624"
#define STATUS_UUID       "37dc8fa3-f61d-4861-8845-59c3dba144c5"
#define ORIENTATION_UUID  "c29ba4df-2240-4c86-96e5-fa24472a6b19"
#define SYSTEMSTATUS_UUID "c6d117fb-bc44-494b-afac-35fa40341e52"
#define SERIALCOMM_UUID   "f2dfe67b-99bf-4cd7-8fd6-60c122694db9"
#define MTU 128
#define DEVICE_NAME "Astral Compass"

inline NimBLECharacteristic *commandHandler, *statusHandler, *orientationHandler, *systemStatusHandler, *serialCommHandler;
inline NimBLEServer *server;
inline NimBLEService *service;
volatile inline bool BLEConnected;
volatile inline bool BLESubscribed;
volatile inline uint16_t BLEMtu = 23;

void BLEInit();
void transmitStatus(char* buffer, int size);
void transmitOrientation(char* buffer, int size);
void transmitSystemStatus(char* buffer, int size);
void transmitSerialComm(char* buffer, int size);