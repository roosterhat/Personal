package com.example.armcontrol

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.armcontrol.ble.BleManager
import com.example.armcontrol.ble.ConnectionState
import com.example.armcontrol.models.MotorStatusEnum
import com.example.armcontrol.models.Orientation
import com.example.armcontrol.models.Position
import com.example.armcontrol.models.ScannedDevice
import com.example.armcontrol.models.Status
import com.example.armcontrol.models.SystemState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer
import kotlin.math.round

class ArmControlViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleManager(application)

    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val scannedDevices: StateFlow<List<ScannedDevice>> = bleManager.scannedDevices
    val status: StateFlow<Status?> = bleManager.status
    val orientation: StateFlow<Orientation?> = bleManager.orientation
    var systemStatuses: StateFlow<List<SystemState>> = bleManager.systemStatuses
    val position: StateFlow<Position?> = bleManager.position
    val target: StateFlow<Position?> = bleManager.target
    var az: Float = 0f
    var el: Float = 0f

    private var sendJob: Job? = null

    private val timer = fixedRateTimer("setPosition", daemon = false, initialDelay = 50, period = 50) {
        setPosition()
    }

    fun isBluetoothEnabled() = bleManager.isBluetoothEnabled()

    fun startScan() = bleManager.startScan()
    fun stopScan() = bleManager.stopScan()

    fun connect(device: BluetoothDevice) = bleManager.connect(device)

    fun disconnect() = bleManager.disconnect()

    fun setPosition() {
        if (sendJob?.isActive == true || (az == 0f && el == 0f)) return
        sendJob = viewModelScope.launch {
            //Log.d("setPoisition", "$az, $el ${round( az * 10 + (target.value?.azimuth ?: 0f))}, ${round(-el * 10 + (target.value?.elevation ?: 0f))}")
            bleManager.sendTargetCoordinates(
                round( az * 10 + (target.value?.azimuth ?: 0f)) ,
                round(-el * 10 + (target.value?.elevation ?: 0f))
            )
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
            bleManager.setLaser(!(status.value?.LaserEnabled ?: false))
        }
    }

    fun toggleMotors() {
        if (sendJob?.isActive == true || (status.value?.MotorStatus ?: 0) != MotorStatusEnum.IDLE) return
        sendJob = viewModelScope.launch {
            bleManager.setMotor((status.value?.MotorStatus) == MotorStatusEnum.DISABLED)
        }
    }

    fun zero() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.zero()
        }
    }

    fun toggleHoldPosition() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.setHoldPosition(!(status.value?.HoldPosition ?: false))
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.disconnect()
        timer.cancel()
    }
}
