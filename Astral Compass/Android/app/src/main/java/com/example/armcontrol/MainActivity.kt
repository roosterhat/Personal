package com.example.armcontrol

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.armcontrol.ble.ConnectionState
import com.example.armcontrol.ui.ConnectScreen
import com.example.armcontrol.ui.ControlScreen
import com.example.armcontrol.ui.theme.ArmControlTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ArmControlViewModel by viewModels()

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var permissionsGranted by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result -> permissionsGranted = result.values.all { it } }

            val enableBtLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { /* user response to the "turn on Bluetooth" prompt */ }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(requiredPermissions())
            }

            ArmControlTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!permissionsGranted) {
                        PermissionRequestScreen(
                            onRetry = { permissionLauncher.launch(requiredPermissions()) }
                        )
                    } else {
                        if (!viewModel.isBluetoothEnabled()) {
                            LaunchedEffect(Unit) {
                                enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            }
                        }
                        AppNavHost(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bluetooth permission is needed to find and control the arm.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Grant permission") }
    }
}

@Composable
private fun AppNavHost(viewModel: ArmControlViewModel) {
    val navController = rememberNavController()
    val connectionState by viewModel.connectionState.collectAsState()

    // Automatically move to the control screen once connected, and back to
    // the connect screen if we ever drop the connection.
    LaunchedEffect(connectionState) {
        when (connectionState) {
            is ConnectionState.Connected -> navController.navigate("control") {
                popUpTo("connect") { inclusive = false }
            }
            is ConnectionState.Disconnected -> navController.navigate("connect") {
                popUpTo("control") { inclusive = true }
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = "connect") {
        composable("connect") { ConnectScreen(viewModel) }
        composable("control") { ControlScreen(viewModel) }
    }
}
