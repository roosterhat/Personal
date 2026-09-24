package com.example.armcontrol.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun OrientationIndicator3D(
    roll: Float,
    pitch: Float,
    yaw: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val labelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val axisLength = size.minDimension / 2.8f // slightly shorter to leave room for labels

        drawCircle(
            color = Color.Gray.copy(alpha = 0.3f),
            radius = axisLength,
            center = center,
            style = Stroke(width = 1.5f)
        )

        val axes = listOf(
            Triple(1f, 0f, 0f) to Color(0xFFE53935) to "X",
            Triple(0f, 1f, 0f) to Color(0xFF43A047) to "Y",
            Triple(0f, 0f, 1f) to Color(0xFF1E88E5) to "Z"
        )

        val projected = axes.map { (vecColor, label) ->
            val (vec, color) = vecColor
            val (rx, ry, rz) = rotateXYZ(vec.first, vec.second, vec.third, roll, pitch, yaw)
            val tip = Offset(center.x + rx * axisLength, center.y - rz * axisLength)
            Triple(tip, ry, Pair(color, label))   // ry is now the depth component
        }.sortedBy { it.second }

        projected.forEach { (tip, depth, colorLabel) ->
            val (color, label) = colorLabel
            val alpha = 0.35f + 0.65f * ((depth + 1f) / 2f).coerceIn(0f, 1f)

            drawLine(
                color = color.copy(alpha = alpha),
                start = center,
                end = tip,
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = color.copy(alpha = alpha), radius = 6f, center = tip)

            // Push the label a bit further out past the tip so it doesn't overlap the dot
            val dx = tip.x - center.x
            val dy = tip.y - center.y
            val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val labelOffset = Offset(
                tip.x + (dx / len) * 16f,
                tip.y + (dy / len) * 16f
            )

            drawContext.canvas.nativeCanvas.apply {
                labelPaint.color = android.graphics.Color.argb(
                    (alpha * 255).toInt(),
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                )
                // Baseline offset so the label is vertically centered on the point, not sitting above it
                val textHeight = labelPaint.descent() - labelPaint.ascent()
                val baselineY = labelOffset.y + textHeight / 2f - labelPaint.descent()
                drawText(label, labelOffset.x, baselineY, labelPaint)
            }
        }
    }
}

private fun rotateXYZ(
    x: Float, y: Float, z: Float,
    rollDeg: Float, pitchDeg: Float, yawDeg: Float
): Triple<Float, Float, Float> {
    val roll = Math.toRadians(rollDeg.toDouble())
    val pitch = Math.toRadians(pitchDeg.toDouble())
    val yaw = Math.toRadians(yawDeg.toDouble())

    val y1 = y * cos(roll) - z * sin(roll)
    val z1 = y * sin(roll) + z * cos(roll)
    val x1 = x.toDouble()

    val x2 = x1 * cos(pitch) + z1 * sin(pitch)
    val z2 = -x1 * sin(pitch) + z1 * cos(pitch)
    val y2 = y1

    val x3 = x2 * cos(yaw) - y2 * sin(yaw)
    val y3 = x2 * sin(yaw) + y2 * cos(yaw)
    val z3 = z2

    return Triple(x3.toFloat(), y3.toFloat(), z3.toFloat())
}