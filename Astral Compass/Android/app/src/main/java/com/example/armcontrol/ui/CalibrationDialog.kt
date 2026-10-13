package com.example.armcontrol.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.armcontrol.models.CalStatusEnum
import com.example.armcontrol.models.Orientation
import com.example.armcontrol.models.Status
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val CalGreen = Color(0xFF2E7D32)
private val SliceCount = 12

@Composable
fun CalibrationDialog(
    state: Status,
    orientation: Orientation,
    onStartGyro: () -> Unit,
    onStartMag: () -> Unit,
    onResetMag: () -> Unit,
    onCancelMag: () -> Unit,
    onClose: () -> Unit,
) {
    val magActive = state.magCalStatus == CalStatusEnum.RUNNING
    val close: () -> Unit = {
        if (magActive) onCancelMag()
        onClose()
    }

    Dialog(
        onDismissRequest = close,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = !magActive,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (magActive) "Magnetometer calibration" else "Calibration",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = close) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Right padding on the content to balance the smaller end padding of the header.
                Box(Modifier.padding(end = 12.dp)) {
                    if (magActive) {
                        MagCalContent(state, orientation, onResetMag, onCancelMag)
                    } else {
                        StepList(state, onStartGyro, onStartMag)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepList(
    state: Status,
    onStartGyro: () -> Unit,
    onStartMag: () -> Unit,
) {
    val busy = state.gyroCalStatus == CalStatusEnum.RUNNING || state.magCalStatus == CalStatusEnum.RUNNING

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        CalStepButton(
            label = "Gyro calibration",
            status = state.gyroCalStatus,
            enabled = !busy,
            onClick = onStartGyro
        )
        CalStepButton(
            label = "Magnetometer calibration",
            status = state.magCalStatus,
            enabled = !busy && state.gyroCalStatus == CalStatusEnum.CALIBRATED,
            onClick = onStartMag
        )
    }
}

@Composable
private fun CalStepButton(
    label: String,
    status: CalStatusEnum,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val done = status == CalStatusEnum.CALIBRATED
    val running = status == CalStatusEnum.RUNNING

    val container = if (done) CalGreen else MaterialTheme.colorScheme.surfaceVariant
    val content = if (done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    // A running button is disabled (it can't be pressed) but should still look fully "on".
    val dim = if (enabled || running) 1f else 0.5f

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container.copy(alpha = dim),
            disabledContentColor = content.copy(alpha = dim)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        StatusBadge(status = status, tint = content.copy(alpha = dim))
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusBadge(status: CalStatusEnum, tint: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f))
            .then(
                if (status == CalStatusEnum.INIT) Modifier.border(2.dp, tint.copy(alpha = 0.5f), CircleShape)
                else Modifier
            )
    ) {
        when (status) {
            CalStatusEnum.INIT -> Unit
            CalStatusEnum.RUNNING -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = tint
            )
            CalStatusEnum.CALIBRATED -> Icon(
                Icons.Default.Check,
                contentDescription = "Done",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MagCalContent(
    state: Status,
    orientation: Orientation,
    onReset: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text(
            "Keep the gimbal still and slowly turn the whole device in a level circle until every segment turns green.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        YawDial(
            yawDeg = -orientation.RPY[2],
            measured = state.magCalSteps,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
        )

        Spacer(Modifier.height(12.dp))
        val yaw = -(((orientation.RPY[2] % 360f) + 360f) % 360f).roundToInt() % 360
        Text(
            text = "$yaw°   ·   ${state.magCalSteps.count({ it })} / $SliceCount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }
        }
    }
}

/**
 * Circular yaw display. The ring is split into [sliceCount] equal segments (slice 0 starts at
 * yaw 0, i.e. straight up, and they run clockwise). Segments in [measured] are green, the one
 * the yaw is currently inside is tinted, and a line from the centre shows the yaw itself.
 */
@Composable
fun YawDial(
    yawDeg: Float,
    measured: List<Boolean>,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 28.dp,
    gapDeg: Float = 2f,
) {
    val idleColor = MaterialTheme.colorScheme.outlineVariant
    val currentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val lineColor = MaterialTheme.colorScheme.primary
    val tickColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val n = SliceCount
        val step = 360f / n
        val stroke = ringWidth.toPx()
        val margin = 10.dp.toPx()
        val radius = size.minDimension / 3f - stroke / 2f - margin
        val inner = radius - stroke / 2f
        val outer = radius + stroke / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        val yaw = ((yawDeg % 360f) + 360f) % 360f
        val current = (yaw / step).toInt().coerceIn(0, n - 1)
        val gap = minOf(gapDeg, step * 0.5f)

        // Canvas angles start at 3 o'clock; yaw 0 is 12 o'clock, so subtract 90.
        for (i in 0 until n) {
            val color = when {
                measured[n - i - 1] -> CalGreen
                i == current -> currentColor
                else -> idleColor
            }
            drawArc(
                color = color,
                startAngle = i * step - 90f + gap / 2f,
                sweepAngle = step - gap,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }

        // North notch above the ring.
        drawLine(
            color = tickColor,
            start = Offset(center.x, center.y - outer - 2.dp.toPx()),
            end = Offset(center.x, center.y - outer - 8.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Yaw line from the centre to the outer edge of the ring.
        val rad = Math.toRadians(yaw.toDouble())
        val start = Offset(
            center.x + (sin(rad) * inner).toFloat(),
            center.y - (cos(rad) * inner).toFloat()
        )
        val end = Offset(
            center.x + (sin(rad) * outer).toFloat(),
            center.y - (cos(rad) * outer).toFloat()
        )
        drawLine(lineColor, start, end, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(lineColor, radius = 6.dp.toPx(), center = center)
    }
}