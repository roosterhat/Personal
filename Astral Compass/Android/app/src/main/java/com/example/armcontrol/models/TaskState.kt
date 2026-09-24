package com.example.armcontrol.models

data class TaskState (
    val name: String,
    val utilization: Float,
    val priority: Int,
    val state: Int,
    val stackHighWaterMark: Int
)