package com.example.armcontrol.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
public fun PanTiltPad(
    x: Float,
    y: Float,
    onDrag: (fx: Float, fy: Float) -> Unit,
    onReset: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(280.dp)
            .pointerInput(Unit, {
                detectDragGestures(
                    onDragEnd = onReset,
                    onDragCancel = onReset,
                    onDrag = { change, _ ->
                        change.consume()
                        val fx = (change.position.x / size.width).coerceIn(0f, 1f)
                        val fy = (change.position.y / size.height).coerceIn(0f, 1f)
                        onDrag(fx, fy)
                    }
                )
            })
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFE7E0EC),
                radius = size.width / 2,
                center = center
            )
            drawCircle(
                color = Color.Gray,
                radius = size.width / 2,
                center = center,
                style = Stroke(width = 5f)
            )

            // Crosshair guide lines
            drawLine(
                color = Color.Gray,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2f
            )

            // Handle position
            val handleCenter = Offset(
                x = x * size.width,
                y = y * size.height
            )
            drawCircle(
                color = Color(0xFF3F51B5),
                radius = 50f,
                center = handleCenter
            )
            drawCircle(
                color = Color.White,
                radius = 50f,
                center = handleCenter,
                style = Stroke(width = 4f)
            )
        }
    }
}
