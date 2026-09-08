package com.example.armcontrol

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.armcontrol.ble.BleManager
import com.example.armcontrol.ble.ConnectionState
import com.example.armcontrol.ble.ScannedDevice
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArmControlViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleManager(application)

    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val scannedDevices: StateFlow<List<ScannedDevice>> = bleManager.scannedDevices
    val lastStatus: StateFlow<String?> = bleManager.lastStatus

    var pan: Float = 0F
    var tilt: Float = 0F
    var laser: Boolean = false
    var motors: Boolean = false

    private var sendJob: Job? = null

    fun isBluetoothEnabled() = bleManager.isBluetoothEnabled()

    fun startScan() = bleManager.startScan()
    fun stopScan() = bleManager.stopScan()

    fun connect(device: BluetoothDevice) = bleManager.connect(device)

    fun disconnect() = bleManager.disconnect()

    /**
     * Called continuously while the user drags the pan/tilt pad. Throttled to ~20 updates/sec
     * so we don't flood the BLE link, while still feeling responsive.
     */
    fun setPosition(newPan: Float, newTilt: Float) {
        pan = newPan % 360;
        tilt = newTilt.coerceIn(0F, 90F)

        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.sendTargetCoordinates(pan, tilt)
            delay(50)
        }
    }

    fun home() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.home()
        }
    }

    fun toggleLaser() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.setLaser(!laser)
        }
    }

    fun toggleMotors() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.setMotor(!motors)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.disconnect()
    }
}
