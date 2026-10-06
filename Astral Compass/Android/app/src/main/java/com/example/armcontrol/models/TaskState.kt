package com.example.armcontrol.models

data class TaskState (
    val name: String,
    val cpuTime: Long,
    val priority: Int,
    val state: Int,
    val stackHighWaterMark: Int,
    var utilization: Float
)