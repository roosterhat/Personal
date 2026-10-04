package com.example.armcontrol

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.armcontrol.ble.ConnectionState
import com.example.armcontrol.ui.ConnectScreen
import com.example.armcontrol.ui.ControlScreen
import com.example.armcontrol.ui.LoadingScreen
import com.example.armcontrol.ui.theme.ArmControlTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ArmControlViewModel by viewModels()

    private fun requiredPermissions(): Array<String> =
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }.toTypedArray()

    private fun granted(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermission(): Boolean {
        return granted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                granted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun hasAllPermissions(): Boolean {
        val bluetoothOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                (granted(Manifest.permission.BLUETOOTH_SCAN) && granted(Manifest.permission.BLUETOOTH_CONNECT))
        return bluetoothOk && hasLocationPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var permissionsGranted by remember { mutableStateOf(false) }
            var btEnabled by remember { mutableStateOf(viewModel.isBluetoothEnabled()) }
            var splashDone by rememberSaveable { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsGranted = hasAllPermissions() }

            val enableBtLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { btEnabled = viewModel.isBluetoothEnabled() }   // re-check after the dialog closes

            // Ask for permissions once at startup
            LaunchedEffect(Unit) {
                permissionLauncher.launch(requiredPermissions())
            }

            // Ask to enable Bluetooth once permissions are granted and it's still off
            LaunchedEffect(permissionsGranted) {
                if (permissionsGranted && !btEnabled) {
                    enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
                if (hasLocationPermission()) viewModel.refreshLocation()
            }

            ArmControlTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        !permissionsGranted -> PermissionRequestScreen(
                            onRetry = { permissionLauncher.launch(requiredPermissions()) }
                        )
                        !btEnabled -> PermissionRequestScreen(
                            onRetry = { enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
                        )
                        !splashDone -> {
                            val progress by viewModel.ephemerisProgress.collectAsState()
                            LoadingScreen(
                                progress = progress,
                                onFinished = { splashDone = true },
                                onRetry = { viewModel.refreshEphemeris() }
                            )
                        }
                        else -> AppNavHost(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bluetooth and location permission are required")
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