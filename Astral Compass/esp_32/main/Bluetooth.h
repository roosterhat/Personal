#pragma once

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID          "53c3b368-70eb-4855-be6d-dfb7ce0cc4e3"
#define COMMAND_UUID          "9ea191cb-0fe6-4ad9-adfd-cf86f43a0624"
#define STATUS_UUID           "37dc8fa3-f61d-4861-8845-59c3dba144c5"

inline BLECharacteristic *commandHandler, *statusHandler;
inline BLEServer *server;
inline BLEService *service;
inline bool BLEConnected;

void BLEInit();
void transmitStatus(String& value);
void transmitStatus(char* buffer, int size);