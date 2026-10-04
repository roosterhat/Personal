package com.example.armcontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.armcontrol.ArmControlViewModel
import com.example.armcontrol.models.MotorStatusEnum
import java.util.Locale
import kotlin.math.*


private fun fmt(v: Float): String = String.format(Locale.US, "%+6.2f", v)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(viewModel: ArmControlViewModel) {
    val status by viewModel.status.collectAsState()
    val orientation by viewModel.orientation.collectAsState()
    val systemStatues by viewModel.systemStatuses.collectAsState()
    val objects by viewModel.objects.collectAsState()
    val credentials by viewModel.savedCredentials.collectAsState()
    val starImportState by viewModel.starImport.collectAsState()

    var handleX by remember { mutableFloatStateOf(0.5f) }
    var handleY by remember { mutableFloatStateOf(0.5f) }

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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Target(${(viewModel.target.value?.azimuth ?: 0f)}°, ${(viewModel.target.value?.elevation ?: 0f)}°), Actual(${(viewModel.position.value?.azimuth ?: 0f)}°, ${(viewModel.position.value?.elevation ?: 0f)}°)",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(24.dp))

                PanTiltPad(
                    x = handleX,
                    y = handleY,
                    onDrag = { _x, _y ->
                        val r = 0.5f
                        var x = _x - r
                        var y = _y - r

                        val rat = min(r / hypot(x, y), 1f)

                        x *= rat
                        y *= rat

                        handleX = r + x
                        handleY = r + y

                        viewModel.az = x
                        viewModel.el = y
                    },
                    onReset = {
                        handleX = 0.5f
                        handleY = 0.5f
                        viewModel.az = 0f
                        viewModel.el = 0f
                    }
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.width(280.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.home() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Home")
                        }
                        Button(
                            onClick = { viewModel.zero() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Zero")
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.toggleMotors() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    if ((status?.MotorStatus ?: MotorStatusEnum.INIT) != MotorStatusEnum.DISABLED ) 0xff009b14 else 0xffff0000
                                ),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Motors")
                        }
                        Button(
                            onClick = { viewModel.toggleHoldPosition() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    if (status?.HoldPosition ?: false) 0xff009b14 else 0xffff0000
                                ),
                                contentColor = Color.White
                            ),
                        ) {
                            Text("Hold")
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.toggleLaser() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    if (status?.LaserEnabled ?: false ) 0xff009b14 else 0xffff0000
                                ),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Laser")
                        }
                    }
                }
            }
            SlideUpTabPanel(
                tabs = listOf(
                    BottomTabItem(
                        label = "Telemetry",
                        icon = Icons.Outlined.ScreenRotation,
                        content = {
                            Column(modifier = Modifier.align(Alignment.BottomCenter),
                                horizontalAlignment = Alignment.CenterHorizontally)
                            {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface (
                                        color = Color.White,
                                        modifier = Modifier.padding(8.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        OrientationIndicator3D(
                                            roll = -(orientation?.RPY?.get(1) ?: 0f),
                                            pitch = -(orientation?.RPY?.get(0) ?: 0f),
                                            yaw = -(orientation?.RPY?.get(2) ?: 0f),
                                            modifier = Modifier.size(150.dp)
                                        )
                                    }

                                    val rows = buildList {
                                        status?.let {
                                            add("Status" to "${it.SystemStatus}")
                                            add("Motors" to "${it.MotorStatus}")
                                            add("Serial" to if(it.SerialReady) "CONNECTED" else "ERROR")
                                            add("LM" to "(${it.LimitSwitch[0]}, ${it.LimitSwitch[1]})")
                                            add("IMU" to "${it.IMU_hz}hz")
                                            add("CAM" to "${it.CAM_hz}hz")
                                        }
                                        orientation?.let {
                                            add("RPY" to "(${fmt(it.RPY[0])}, ${fmt(it.RPY[1])}, ${fmt(it.RPY[2])})")
                                            add("Bias" to "(${fmt(it.bias[0])}, ${fmt(it.bias[1])}, ${fmt(it.bias[2])})")
                                            add("Gyro" to "(${fmt(it.gyro[0])}, ${fmt(it.gyro[1])}, ${fmt(it.gyro[2])})")
                                            add("Acc" to "(${fmt(it.acceleration[0])}, ${fmt(it.acceleration[1])}, ${fmt(it.acceleration[2])})")
                                            add("Rot" to "(${fmt(it.rotationEstimate[0])}, ${fmt(it.rotationEstimate[1])}, ${fmt(it.rotationEstimate[2])})")
                                            add("Track" to "${it.rotationInlinerCount}, ${String.format(Locale.US, "%.3f", it.rotationResidualRMS)}, ${String.format(Locale.US, "%.3f", it.rotation_dt)}s")
                                        }
                                    }

                                    TelemetryTable(rows = rows, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    ),
                    BottomTabItem(
                        label = "Threads",
                        icon = Icons.Outlined.Memory,
                        scrollable = false,
                        height = 460.dp,
                        content = { TaskMonitorPanel(systemStatues) }
                    ),
                    BottomTabItem(
                        label = "Track",
                        icon = Icons.Outlined.GpsFixed,
                        scrollable = false,
                        height = 460.dp,
                        content = { CelestialSearchContent(objects, { viewModel.setTrackObject(it) }, viewModel.currentTrack) }
                    ),
                    BottomTabItem(
                        label = "Cal",
                        icon = Icons.Outlined.Build,
                        content = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { viewModel.calibrate() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xff009b14),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Calibrate")
                                    }
                                    Button(
                                        onClick = { viewModel.reset() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xffff0000),
                                            contentColor = Color.White
                                        ),
                                    ) {
                                        Text("Reset")
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { viewModel.home() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                    ) {
                                        Text("Home")
                                    }
                                    Button(
                                        onClick = { viewModel.zero() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                    ) {
                                        Text("Zero")
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { viewModel.setPos(359.99f, 0f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                    ) {
                                        Text("AZ 360")
                                    }
                                    Button(
                                        onClick = { viewModel.setPos(0f, 90f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                    ) {
                                        Text("EL 90")
                                    }
                                }
                            }
                        }
                    ),
                    BottomTabItem(
                        label = "Settings",
                        icon = Icons.Outlined.Settings,
                        scrollable = false,
                        height = 700.dp,
                        content = { SettingsContent(
                            credentials,
                            { viewModel.saveCredentials(it) },
                            { viewModel.removeCredentials() },
                            starImportState,
                            { viewModel.importStarFile(it) },
                            { viewModel.clearCacheAndReload() }
                        )}
                    ),
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

