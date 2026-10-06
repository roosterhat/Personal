package com.example.armcontrol.models

data class SystemState(
    var heapUsage: Float,
    var totalTime: Long,
    var clockSpeed: Int,
    var taskStates: MutableMap<String, TaskState>,
    var coreUtil: MutableMap<Int, TaskState>
)
