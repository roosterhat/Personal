package com.example.armcontrol.models

enum class MotorStatusEnum {
    INIT, HOMING, IDLE, MOVING, DISABLED
}

enum class SystemStatusEnum {
    INIT, PAIRING, PAIRED, TRACKING, IDLE, CAL
}

data class Status (
    val LaserEnabled: Boolean,
    val MotorStatus: MotorStatusEnum,
    val HoldPosition: Boolean,
    val SerialReady: Boolean,
    val LimitSwitch: List<Boolean>,
    val IMU_hz: Int,
    val CAM_hz: Int,
    val SystemStatus: SystemStatusEnum
)