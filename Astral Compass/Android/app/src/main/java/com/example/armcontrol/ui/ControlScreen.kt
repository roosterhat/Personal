package com.example.armcontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.armcontrol.ArmControlViewModel
import com.example.armcontrol.ble.ConnectionState
import androidx.compose.foundation.Canvas


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(viewModel: ArmControlViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val status by viewModel.lastStatus.collectAsState()

    var handleX by remember { mutableFloatStateOf(0f) }
    var handleY by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arm Control") },
                actions = {
                    TextButton(onClick = { viewModel.disconnect() }) { Text("Disconnect") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val connectedName = (connectionState as? ConnectionState.Connected)?.deviceName
            Text(
                text = connectedName?.let { "Connected: $it" } ?: "Disconnected",
                style = MaterialTheme.typography.bodyMedium
            )
            status?.let {
                Text("Arm status: $it", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Text("Pan: ${viewModel.pan}°   Tilt: ${viewModel.tilt}°", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(24.dp))

            PanTiltPad(
                x = handleX,
                y = handleY,
                onDrag = { x, y ->
                    handleX = x
                    handleY = y
                    val pan = (x - 0.5f) * 2 + viewModel.pan
                    val tilt = (y - 0.5f) * -2 + viewModel.tilt
                    viewModel.setPosition(pan, tilt)
                },
                onReset = {
                    handleX = 0.5f
                    handleY = 0.5f
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = { viewModel.home() }) {
                Text("Home")
            }
            Button(onClick = { viewModel.toggleLaser() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(if (viewModel.laser) 0xff009b14 else 0xffff0000),
                    contentColor = Color.White
                ))
            {
                Text("Laser")
            }
            Button(onClick = { viewModel.toggleMotors() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(if (viewModel.motors) 0xff009b14 else 0xffff0000),
                contentColor = Color.White
            )) {
                Text("Motors")
            }
        }
    }
}

@Composable
private fun PanTiltPad(
    x: Float,
    y: Float,
    onDrag: (fx: Float, fy: Float) -> Unit,
    onReset: () -> Unit
) {
    val padSize = 280.dp

    Box(
        modifier = Modifier
            .size(padSize)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                radius = 28f,
                center = handleCenter
            )
            drawCircle(
                color = Color.White,
                radius = 28f,
                center = handleCenter,
                style = Stroke(width = 4f)
            )
        }
    }
}
