package com.example.armcontrol.models

data class Orientation (
    val RPY: List<Float>,
    val bias: List<Float>,
    val gyro: List<Float>,
    val acceleration: List<Float>,
    val rotationEstimate: List<Float>,
    val rotation_dt: Float,
    val rotationInlinerCount: Int,
    val rotationResidualRMS: Float
)