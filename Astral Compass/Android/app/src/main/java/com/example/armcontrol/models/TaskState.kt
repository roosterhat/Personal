package com.example.armcontrol.models

data class TaskState (
    val name: String,
    val cpuTime: Long,
    val priority: Int,
    val state: Int,
    val stackSize: Int,
    val stackUsed: Int,
    var utilization: Float
)