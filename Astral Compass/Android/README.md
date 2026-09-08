# Arm Control (Android BLE skeleton)

A minimal but working skeleton for controlling a 2-axis (pan/tilt) robotic arm from
an Android phone over Bluetooth LE, talking to an ESP32.

## What's included

- **Connect flow** (`ui/ConnectScreen.kt`): requests BLE permissions, scans for
  nearby devices (filtered by name prefix `ESP32-ARM`), lists them, and connects
  on tap.
- **Control flow** (`ui/ControlScreen.kt`): a 2D pan/tilt touchpad — drag anywhere
  in the square to set pan (X) and tilt (Y), 0–180°. Includes a "Center arm" button
  and shows any status string the ESP32 sends back.
- **BLE plumbing** (`ble/BleManager.kt`, `ble/BleConstants.kt`): GATT connect,
  service/characteristic discovery, throttled writes, optional notification
  subscription for telemetry back from the arm.
- **ViewModel** (`ArmControlViewModel.kt`): holds connection/scan state as
  `StateFlow`s and throttles outgoing commands to ~20/sec so the touchpad feels
  smooth without flooding the BLE link.

This is a skeleton, not a finished product — swap in your real UUIDs, wire
protocol, and error handling as needed.

## Opening the project

Open the `ArmControlApp/` folder directly in Android Studio (Koala or newer).
It will generate the Gradle wrapper automatically on first sync. Minimum SDK is
26 (Android 8.0), which covers virtually all BLE-capable phones.

## Wire protocol (change this to match your firmware)

The app currently sends plain ASCII commands with no response required:

```
P090T045\n
```

`P` + 3-digit pan angle, `T` + 3-digit tilt angle, newline-terminated. This is
deliberately simple to eyeball in a serial monitor. If you'd rather send raw
bytes (e.g. `[0xAA, panByte, tiltByte, 0x55]`) or JSON, change
`BleManager.sendPanTilt()` — that's the only place the encoding lives.

## UUIDs to match

Edit `ble/BleConstants.kt` to match your ESP32 sketch:

```kotlin
val SERVICE_UUID = UUID.fromString("...")
val COMMAND_CHARACTERISTIC_UUID = UUID.fromString("...")   // app writes here
val STATUS_CHARACTERISTIC_UUID = UUID.fromString("...")    // ESP32 notifies here (optional)
```

Generate your own UUIDs (e.g. `uuidgen` or any online v4 UUID generator) rather
than reusing the placeholders, to avoid clashing with other BLE peripherals.

## Matching ESP32 (Arduino) sketch

This uses the built-in `BLEDevice` library (comes with the ESP32 Arduino core).

```cpp
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <ESP32Servo.h>

#define SERVICE_UUID           "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define COMMAND_CHAR_UUID      "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define STATUS_CHAR_UUID       "0ac47a4e-0e5a-4a2c-9c1f-0a1b2c3d4e5f"

Servo panServo;
Servo tiltServo;
BLECharacteristic *statusChar;

class CommandCallback : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *characteristic) {
    String value = characteristic->getValue().c_str();
    // Expected format: "P090T045\n"
    int pIndex = value.indexOf('P');
    int tIndex = value.indexOf('T');
    if (pIndex == -1 || tIndex == -1) return;

    int pan = value.substring(pIndex + 1, tIndex).toInt();
    int tilt = value.substring(tIndex + 1).toInt();
    pan = constrain(pan, 0, 180);
    tilt = constrain(tilt, 0, 180);

    panServo.write(pan);
    tiltServo.write(tilt);

    String status = "OK P" + String(pan) + "T" + String(tilt);
    statusChar->setValue(status.c_str());
    statusChar->notify();
  }
};

void setup() {
  Serial.begin(115200);

  panServo.attach(18);
  tiltServo.attach(19);
  panServo.write(90);
  tiltServo.write(90);

  BLEDevice::init("ESP32-ARM-01"); // must start with DEVICE_NAME_PREFIX in the app
  BLEServer *server = BLEDevice::createServer();
  BLEService *service = server->createService(SERVICE_UUID);

  BLECharacteristic *commandChar = service->createCharacteristic(
      COMMAND_CHAR_UUID,
      BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR
  );
  commandChar->setCallbacks(new CommandCallback());

  statusChar = service->createCharacteristic(
      STATUS_CHAR_UUID,
      BLECharacteristic::PROPERTY_NOTIFY
  );
  statusChar->addDescriptor(new BLE2902());

  service->start();
  server->getAdvertising()->start();

  Serial.println("BLE arm controller ready, advertising as ESP32-ARM-01");
}

void loop() {
  // Nothing needed here - all work happens in the BLE write callback.
  delay(20);
}
```

Wire the two servos to whatever GPIO pins you used above (18/19 here), and power
them from a supply that can handle the stall current — don't run servos off the
ESP32's onboard 3.3V regulator.

## Things you'll likely want to add next

- Reconnect-on-drop logic (the app currently just returns to the connect screen).
- MTU negotiation if you move to a richer binary protocol.
- Bonding/pairing if you want the link encrypted.
- Persisting the last-connected device address to skip re-scanning.
