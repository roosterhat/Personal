package com.example.armcontrol.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.armcontrol.FrameProcessor
import com.example.armcontrol.models.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.math.max

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Scanning : ConnectionState()
    data class Connecting(val deviceName: String) : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Failed(val message: String) : ConnectionState()
}

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner get() = adapter?.bluetoothLeScanner

    private var gatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingDescriptorWrites = ArrayDeque<() -> Unit>()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val _status = MutableStateFlow<Status?>(null)
    private val _orientation = MutableStateFlow<Orientation>(Orientation(listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), 0f, 0, 0f))
    private val _systemStatuses = MutableStateFlow<List<SystemState>>(emptyList())
    private var _systemStatus = SystemState(0f, 0, 0, mutableMapOf(), mutableMapOf())
    private val _position = MutableStateFlow<Position>(Position(0f, 0f))
    private val _target = MutableStateFlow<Position>(Position(0f, 0f))
    private val _serialComm = MutableStateFlow<String>("")
    private val writeMutex = Mutex()
    private var pendingWrite: CompletableDeferred<Boolean>? = null
    val status: StateFlow<Status?> = _status.asStateFlow()
    val orientation: StateFlow<Orientation> = _orientation.asStateFlow()
    val systemStatuses: StateFlow<List<SystemState>> = _systemStatuses.asStateFlow()
    val position: StateFlow<Position> = _position.asStateFlow()
    val target: StateFlow<Position> = _target.asStateFlow()
    val serialComm: StateFlow<String> = _serialComm.asStateFlow()
    var frameProcessor: FrameProcessor = FrameProcessor()

    private val statusPattern = Regex("""(\d+)""")
    private val orientationPattern = Regex("""(-?\d+(?:\.\d+)?)""")
    private val systemStatusPattern = Regex("""(T) ([\w\s\-\.]+) (-?\d+(?:\.\d+)?) (\d+) (\d+) (\d+)|(M) (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?)""")
    private val framePattern = Regex("""F (\d+) (\d+)""")
    private var rawTaskData: String = ""
    private var MTU: Int = 0
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled ?: false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            val name = result.device.name ?: result.scanRecord?.deviceName ?: return
            if (!name.startsWith(BleConstants.DEVICE_NAME_PREFIX)) return

            val devices = _scannedDevices.value.toMutableList()
            val idx = devices.indexOfFirst { it.device.address == result.device.address }
            val entry = ScannedDevice(result.device, name, result.rssi)
            if (idx >= 0) devices[idx] = entry else devices.add(entry)
            _scannedDevices.value = devices
        }

        override fun onScanFailed(errorCode: Int) {
            _connectionState.value = ConnectionState.Failed("Scan failed (code $errorCode)")
        }
    }

    fun startScan() {
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = ConnectionState.Failed("Bluetooth is off")
            return
        }
        _scannedDevices.value = emptyList()
        _connectionState.value = ConnectionState.Scanning

        val filters = listOf<ScanFilter>()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(filters, settings, scanCallback)

        mainHandler.postDelayed({
            if (_connectionState.value == ConnectionState.Scanning) {
                stopScan()
                _connectionState.value = ConnectionState.Disconnected
            }
        }, 10_000)
    }

    fun stopScan() {
        scanner?.stopScan(scanCallback)
    }

    fun connect(device: BluetoothDevice) {
        stopScan()
        _connectionState.value = ConnectionState.Connecting(device.name ?: device.address)
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        _systemStatuses.value = emptyList()

        _status.value = null
        _orientation.value = Orientation(listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), listOf(0f, 0f, 0f), 0f, 0, 0f)
        _systemStatuses.value = emptyList()
        _systemStatus = SystemState(0f, 0, 0, mutableMapOf(), mutableMapOf())
        _position.value = Position(0f, 0f)
        _target.value = Position(0f, 0f)
        _serialComm.value = ""
        frameProcessor.init(0, MTU)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        commandCharacteristic = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    g.requestMtu(128)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    commandCharacteristic = null
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MTU = mtu
                g.discoverServices()
            }
        }

        private fun initNotificationCharacteristic(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            g.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(BleConstants.CLIENT_CHARACTERISTIC_CONFIG_UUID) ?: return

            pendingDescriptorWrites.addLast {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    g.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(descriptor)
                }
            }
            if (pendingDescriptorWrites.size == 1) {
                pendingDescriptorWrites.first().invoke()
            }
        }

        override fun onDescriptorWrite(g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            pendingDescriptorWrites.removeFirstOrNull()
            pendingDescriptorWrites.firstOrNull()?.invoke()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = ConnectionState.Failed("Service discovery failed")
                return
            }
            val service = g.getService(BleConstants.SERVICE_UUID)
            if (service == null) {
                _connectionState.value = ConnectionState.Failed("Incompatible device")
                return
            }
            commandCharacteristic = service.getCharacteristic(BleConstants.COMMAND_UUID)
            if (commandCharacteristic == null) {
                _connectionState.value = ConnectionState.Failed("Command characteristic not found")
                return
            }

            service.getCharacteristic(BleConstants.STATE_UUID)?.let { c -> initNotificationCharacteristic(g, c) }
            service.getCharacteristic(BleConstants.ORIENTATION_UUID)?.let { c -> initNotificationCharacteristic(g, c) }
            service.getCharacteristic(BleConstants.SYSTEM_STATUS_UUID)?.let { c -> initNotificationCharacteristic(g, c) }
            service.getCharacteristic(BleConstants.SERIAL_COMM_UUID)?.let { c -> initNotificationCharacteristic(g, c) }
            service.getCharacteristic(BleConstants.FRAME_UUID)?.let { c -> initNotificationCharacteristic(g, c) }

            _connectionState.value = ConnectionState.Connected(g.device.name ?: g.device.address)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            val data = String(value, Charsets.UTF_8)
            //Log.d("BleManager", data)

            when(characteristic.uuid) {
                BleConstants.STATE_UUID -> {
                    val matches = statusPattern.findAll(data).toList()
                    if (matches.size == 11) {
                        _status.value = Status(
                            matches[0].value == "1",
                            MotorStatusEnum.entries.getOrNull(matches[1].value.toInt()) ?: MotorStatusEnum.INIT,
                            matches[2].value == "1",
                            matches[3].value == "1",
                            listOf(matches[4].value == "1", matches[5].value == "1"),
                            matches[6].value.toInt(),
                            matches[7].value.toInt(),
                            SystemStatusEnum.entries.getOrNull(matches[8].value.toInt()) ?: SystemStatusEnum.INIT,
                            matches[9].value.toInt(),
                            matches[10].value == "1",
                        )
                    }
                }

                BleConstants.ORIENTATION_UUID -> {
                    // Log.d("BleManager", data)
                    val matches = orientationPattern.findAll(data).toList()
                    if (matches.size == 24) {
                        val rows = matches.map { it.value.toFloat() }.chunked(3)
                        _orientation.value = Orientation(rows[0], rows[1], rows[2], rows[3], rows[4], rows[5][0], rows[5][1].toInt(), rows[5][2])
                        _target.value = Position(rows[6][0], rows[6][1])
                        _position.value = Position(rows[7][0], rows[7][1])
                    }
                }

                BleConstants.SYSTEM_STATUS_UUID -> {
                    val matches = systemStatusPattern.find(data)

                    if(matches != null && matches.groups[7]?.value == "M") {
                        for(line in rawTaskData.split('\n')) {
                            val lineMatch = systemStatusPattern.find(line)

                            if(lineMatch != null) {
                                val name = lineMatch.groups[2]?.value ?: ""

                                if (name.contains("IDLE")) {
                                    val coreNum = name.last().digitToInt()
                                    _systemStatus.coreUtil[coreNum] = TaskState(
                                        name,
                                        lineMatch.groups[3]?.value?.toLong() ?: 0,
                                        lineMatch.groups[4]?.value?.toInt() ?: 0,
                                        lineMatch.groups[5]?.value?.toInt() ?: 0,
                                        lineMatch.groups[6]?.value?.toInt() ?: 0,
                                        0f
                                    )
                                } else if (name != "") {
                                    _systemStatus.taskStates[name] = TaskState(
                                        name,
                                        lineMatch.groups[3]?.value?.toLong() ?: 0,
                                        lineMatch.groups[4]?.value?.toInt() ?: 0,
                                        lineMatch.groups[5]?.value?.toInt() ?: 0,
                                        lineMatch.groups[6]?.value?.toInt() ?: 0,
                                        0f
                                    )
                                }
                            }
                        }
                        rawTaskData = ""

                        _systemStatus.heapUsage = matches.groups[8]?.value?.toFloat() ?: 0f
                        _systemStatus.totalTime = matches.groups[9]?.value?.toLong() ?: 0
                        _systemStatus.clockSpeed = matches.groups[10]?.value?.toInt() ?: 0

                        val previousSystemStatus =
                            if (_systemStatuses.value.isNotEmpty()) _systemStatuses.value.last() else null
                        val dt = _systemStatus.totalTime - (previousSystemStatus?.totalTime ?: 0)
                        for (core in _systemStatus.coreUtil) {
                            val previous = previousSystemStatus?.coreUtil[core.key]
                            core.value.utilization = 1f - (core.value.cpuTime - (previous?.cpuTime ?: 0)).toFloat() / dt
                        }
                        for (task in _systemStatus.taskStates) {
                            val previous = previousSystemStatus?.taskStates[task.key]
                            task.value.utilization = (task.value.cpuTime - (previous?.cpuTime ?: 0)).toFloat() / dt
                        }

                        _systemStatuses.value = _systemStatuses.value.drop(max(_systemStatuses.value.size - 60, 0)) + _systemStatus

                        _systemStatus = SystemState(0f, 0, 0, mutableMapOf(), mutableMapOf())
                    }
                    else {
                        rawTaskData += data
                    }
                }

                BleConstants.SERIAL_COMM_UUID -> {
                    _serialComm.value = _serialComm.value + data
                }

                BleConstants.FRAME_UUID -> {
                    val matches = framePattern.find(data)
                    if(matches != null) {
                        frameProcessor.init(matches.groups[2]?.value?.toInt() ?: 0, MTU)
                    }
                    else {
                        frameProcessor.ingest(value)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, c: BluetoothGattCharacteristic, status: Int) {
            pendingWrite?.complete(status == BluetoothGatt.GATT_SUCCESS)
            pendingWrite = null
        }
    }

    suspend fun sendDeltaCoordinates(pan: Float, tilt: Float) {
        sendCommand(String.format(Locale.US, "D %.2f %.2f", pan, tilt))
    }

    suspend fun sendAbsoluteCoordinates(pan: Float, tilt: Float) {
        sendCommand(String.format(Locale.US, "A %.2f %.2f", pan, tilt))
    }

    suspend fun sendTrackingCoordinates(pan: Float, tilt: Float) {
        sendCommand(String.format(Locale.US, "T %.2f %.2f", pan, tilt))
    }

    suspend fun stopTrack() {
        sendCommand("S")
    }

    suspend fun home() {
        sendCommand("H")
    }

    suspend fun setLaser(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "L %d", if(enabled) 1 else 0))
    }

    suspend fun setMotor(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "E %d", if(enabled) 1 else 0))
    }

    suspend fun zero() {
        sendCommand(String.format(Locale.US, "Z"))
    }

    suspend fun resetDevice() {
        sendCommand(String.format(Locale.US, "R"))
        disconnect()
    }

    suspend fun calibrate() {
        sendCommand(String.format(Locale.US, "C"))
    }

    suspend fun setHoldPosition(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "P %d", if(enabled) 1 else 0))
    }

    suspend fun setBrightness(brightness: Float) {
        sendCommand(String.format(Locale.US, "B %.2f", brightness))
    }

    suspend fun getFrame() {
        sendCommand("F")
    }

    suspend private fun sendCommand(command: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        writeMutex.withLock {
            val characteristic = commandCharacteristic ?: return@withLock
            val g = gatt ?: return@withLock

            val payload = command.toByteArray(Charsets.UTF_8)

            val done = CompletableDeferred<Boolean>()
            pendingWrite = done
            val started = g.writeCharacteristic(
                characteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            if (started != BluetoothStatusCodes.SUCCESS) { pendingWrite = null; return@withLock }
            withTimeoutOrNull(200) { done.await() } ?: false.also { pendingWrite = null }
        }
    }
}
