package com.example.armcontrol.models

enum class MotorStatusEnum {
    INIT, HOMING, IDLE, MOVING, DISABLED
}

enum class SystemStatusEnum {
    INIT, PAIRING, PAIRED, TRACKING, IDLE, CAL
}

enum class CalStatusEnum {
    INIT, RUNNING, CALIBRATED
}

data class Status (
    val laserEnabled: Boolean,
    val motorStatus: MotorStatusEnum,
    val holdPosition: Boolean,
    val serialReady: Boolean,
    val limitSwitch: List<Boolean>,
    val IMU_hz: Int,
    val CAM_hz: Int,
    val systemStatus: SystemStatusEnum,
    val northOffset: Int,
    val gyroCalStatus: CalStatusEnum,
    val magCalStatus: CalStatusEnum,
    val magCalSteps: List<Boolean>
)