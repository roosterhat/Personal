package com.example.armcontrol.ble

import java.util.UUID

/**
 * These UUIDs MUST match whatever you define in your ESP32 BLE sketch.
 * The values below are just placeholders (commonly seen in ESP32 BLE examples) -
 * swap them for your own generated UUIDs.
 */
object BleConstants {

    val SERVICE_UUID: UUID = UUID.fromString("53c3b368-70eb-4855-be6d-dfb7ce0cc4e3")
    val COMMAND_UUID: UUID = UUID.fromString("9ea191cb-0fe6-4ad9-adfd-cf86f43a0624")
    val STATUS_UUID: UUID = UUID.fromString("37dc8fa3-f61d-4861-8845-59c3dba144c5")
    val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val DEVICE_NAME_PREFIX: String = "Astral-Compass"
}
