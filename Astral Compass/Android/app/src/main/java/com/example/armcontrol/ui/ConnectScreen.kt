package com.example.armcontrol.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.armcontrol.ArmControlViewModel
import com.example.armcontrol.ble.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(viewModel: ArmControlViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val devices by viewModel.scannedDevices.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Connect to Astral Compass") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            StatusBanner(connectionState)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.startScan() },
                enabled = connectionState !is ConnectionState.Scanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (connectionState is ConnectionState.Scanning) "Scanning..." else "Scan for devices")
            }

            Spacer(Modifier.height(16.dp))

            if (devices.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No devices found",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(devices, key = { it.device.address }) { scanned ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.connect(scanned.device) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(scanned.name, style = MaterialTheme.typography.titleMedium)
                                    Text(scanned.device.address, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${scanned.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(state: ConnectionState) {
    val text = when (state) {
        is ConnectionState.Disconnected -> "Not connected"
        is ConnectionState.Scanning -> "Scanning for nearby devices..."
        is ConnectionState.Connecting -> "Connecting to ${state.deviceName}..."
        is ConnectionState.Connected -> "Connected to ${state.deviceName}"
        is ConnectionState.Failed -> "Error: ${state.message}"
    }
    Text(text, style = MaterialTheme.typography.bodyLarge)
}
