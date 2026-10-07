package com.example.armcontrol

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.armcontrol.ble.BleManager
import com.example.armcontrol.ble.ConnectionState
import com.example.armcontrol.ephemeris.Credentials
import com.example.armcontrol.ephemeris.CredentialsStore
import com.example.armcontrol.ephemeris.EphemerisEntry
import com.example.armcontrol.ephemeris.EphemerisRepository
import com.example.armcontrol.ephemeris.InitProgress
import com.example.armcontrol.ephemeris.LocationSource
import com.example.armcontrol.ephemeris.Observer
import com.example.armcontrol.ephemeris.Pointing
import com.example.armcontrol.models.MotorStatusEnum
import com.example.armcontrol.models.Orientation
import com.example.armcontrol.models.Position
import com.example.armcontrol.models.ScannedDevice
import com.example.armcontrol.models.Status
import com.example.armcontrol.models.SystemState
import com.example.armcontrol.ui.StarImportState
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.round

class ArmControlViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleManager(application)
    private val credentialStore = CredentialsStore(application)
    private val ephemeris = EphemerisRepository(application, credentialStore)
    private val location = LocationSource(application)
    private var sendJob: Job? = null
    private var controlJob: Job? = null


    val ephemerisProgress: StateFlow<InitProgress> = ephemeris.progress
    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val scannedDevices: StateFlow<List<ScannedDevice>> = bleManager.scannedDevices
    val status: StateFlow<Status?> = bleManager.status
    val orientation: StateFlow<Orientation?> = bleManager.orientation
    var systemStatuses: StateFlow<List<SystemState>> = bleManager.systemStatuses
    val position: StateFlow<Position?> = bleManager.position
    val target: StateFlow<Position?> = bleManager.target
    val objects: StateFlow<List<EphemerisEntry>> = ephemeris.entries
    private val _observer = MutableStateFlow<Observer?>(null)
    val observer: StateFlow<Observer?> = _observer.asStateFlow()
    val serialComm: StateFlow<String> = bleManager.serialComm

    var isCalibrating by mutableStateOf(false)
        private set
    var isHoming by mutableStateOf(false)
        private set

    val savedCredentials = MutableStateFlow(credentialStore.load())
    val starImport = MutableStateFlow<StarImportState>(StarImportState.Idle)

    var az: Float = 0f
    var el: Float = 0f
    var brightness: Float = 0.5f
    var currentTrack: EphemerisEntry? = null
    var pendingTrack: EphemerisEntry? = null


    init {
        viewModelScope.launch {
            bleManager.connectionState.collect { state ->
                controlJob?.cancel()
                controlJob = null
                if (state is ConnectionState.Connected) {
                    controlJob = launch { runControlLoops() }
                }
            }
        }
        refreshEphemeris()
    }

    private suspend fun runControlLoops() = coroutineScope {
        launch {
            delay(50)
            while (isActive) {
                setPosition()
                delay(50)
            }
        }
        launch {
            delay(200)
            while (isActive) {
                trackObject()
                delay(200)
            }
        }
    }

    fun saveCredentials(c: Credentials) {
        credentialStore.save(c)
        savedCredentials.value = c
    }
    fun removeCredentials() {
        credentialStore.clear()
        savedCredentials.value = null
    }

    fun refreshEphemeris() {
        viewModelScope.launch { ephemeris.refresh() }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            location.currentObserver()?.let { _observer.value = it }
        }
    }

    fun isBluetoothEnabled() = bleManager.isBluetoothEnabled()

    fun startScan() = bleManager.startScan()
    fun stopScan() = bleManager.stopScan()

    fun connect(device: BluetoothDevice) {
        bleManager.connect(device)
        resetValues()
    }

    fun disconnect() {
        bleManager.disconnect()
        resetValues()
    }

    fun resetValues() {
        currentTrack = null
        az = 0f
        el = 0f
    }

    fun setPosition() {
        if (sendJob?.isActive == true || (az == 0f && el == 0f) || currentTrack != null) return
        sendJob = viewModelScope.launch {
            bleManager.sendDeltaCoordinates(round(az * 10), round(-el * 5))
        }
    }

    fun trackObject() {
        if (sendJob?.isActive == true || currentTrack == null || observer.value == null) return

        sendJob = viewModelScope.launch {
            val azEl = Pointing.azEl(currentTrack!!, observer.value!!)
            bleManager.sendTrackingCoordinates(azEl.azimuthDeg.toFloat(), azEl.elevationDeg.toFloat())
        }
    }

    fun setTrackObject(obj: EphemerisEntry, force: Boolean = false) {
        if(!(status.value?.Calibrated ?: false) && !force) {
            pendingTrack = obj
        }
        else if(obj == currentTrack) {
            stopTracking()
        }
        else {
            currentTrack = obj
            pendingTrack = null
            sendJob = viewModelScope.launch {
                bleManager.setHoldPosition(true)
            }
        }
    }

    fun home() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            isHoming = true
            bleManager.home()
            delay(500)
            while (status.value?.MotorStatus == MotorStatusEnum.HOMING)
                delay(100)
            isHoming = false
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

    fun setPos(az: Float, el: Float) {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            bleManager.sendTargetCoordinates(az, el)
        }
    }

    fun reset() {
        sendJob = viewModelScope.launch {
            bleManager.resetDevice()
        }
    }

    fun calibrate() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            isCalibrating = true
            bleManager.calibrate()
            delay(500)
            while (!(status.value?.Calibrated ?: false))
                delay(100)
            isCalibrating = false
        }
    }

    fun calibrateAndTrack(obj: EphemerisEntry) {
        viewModelScope.launch {
            calibrate()
            isCalibrating = true
            while (isCalibrating)
                delay(100)
            setTrackObject(obj)
        }
    }

    fun stopTracking() {
        if (sendJob?.isActive == true) return

        currentTrack = null
        sendJob = viewModelScope.launch {
            bleManager.stopTrack()
            bleManager.setHoldPosition(false)
        }
    }

    fun setBrightnessValue(value: Float) {
        if (sendJob?.isActive == true) return

        brightness = value
        sendJob = viewModelScope.launch {
            bleManager.setBrightness(brightness)
        }
    }

    fun importStarFile(uri: Uri) = viewModelScope.launch {
        val name = ephemeris.displayName(uri) ?: "catalog.csv"
        starImport.value = StarImportState.Importing(name, null)
        starImport.value = try {
            val n = ephemeris.importStarCatalog(uri) { p ->
                starImport.value = StarImportState.Importing(name, p)
            }
            StarImportState.Done(name, n)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            StarImportState.Failed(name, e.message ?: "Import failed")
        }
    }

    fun clearCacheAndReload() = viewModelScope.launch {
        ephemeris.clearCache()
        ephemeris.refresh(force = true)
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
