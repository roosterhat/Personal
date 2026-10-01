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
import com.example.armcontrol.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val status: StateFlow<Status?> = _status.asStateFlow()
    val orientation: StateFlow<Orientation> = _orientation.asStateFlow()
    val systemStatuses: StateFlow<List<SystemState>> = _systemStatuses.asStateFlow()
    val position: StateFlow<Position> = _position.asStateFlow()
    val target: StateFlow<Position> = _target.asStateFlow()

    private val statusPattern = Regex("""(\d+)""")
    private val orientationPattern = Regex("""(-?\d+(?:\.\d+)?)""")
    private val systemStatusPattern = Regex("""(T) ([\w\s\-\.]+) (-?\d+(?:\.\d+)?) (\d+) (\d+) (\d+)|(M) (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?)""")
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

            _connectionState.value = ConnectionState.Connected(g.device.name ?: g.device.address)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            val data = String(value, Charsets.UTF_8)
            //Log.d("BleManager", data)

            when(characteristic.uuid) {
                BleConstants.STATE_UUID -> {
                    val matches = statusPattern.findAll(data).toList()
                    if (matches.size == 8) {
                        _status.value = Status(
                            matches[0].value == "1",
                            MotorStatusEnum.entries.getOrNull(matches[1].value.toInt()) ?: MotorStatusEnum.INIT,
                            matches[2].value == "1",
                            matches[3].value == "1",
                            listOf(matches[4].value == "1", matches[5].value == "1"),
                            matches[6].value.toInt(),
                            matches[7].value.toInt()
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
                    if (matches != null) {
                        val id = matches.groups[1]?.value ?: matches.groups[7]?.value
                        when(id) {
                            "M" -> {
                                _systemStatus.heapUsage = matches.groups[8]?.value?.toFloat() ?: 0f
                                _systemStatus.totalTime = matches.groups[9]?.value?.toLong() ?: 0
                                _systemStatus.clockSpeed = matches.groups[10]?.value?.toInt() ?: 0

                                _systemStatuses.value = _systemStatuses.value.drop(max(_systemStatuses.value.size - 59, 0)) + _systemStatus

                                _systemStatus = SystemState(0f, 0, 0, mutableMapOf(), mutableMapOf())
                            }

                            "T" -> {
                                val name = matches.groups[2]?.value ?: ""
                                if(name.contains("IDLE")) {
                                    val coreNum = name.last().digitToInt()
                                    _systemStatus.coreIDLE[coreNum] = matches.groups[3]?.value?.toFloat() ?: 0f
                                }
                                else if(name != "") {
                                    _systemStatus.taskStates[name] = TaskState(
                                        name,
                                        matches.groups[3]?.value?.toFloat() ?: 0f,
                                        matches.groups[4]?.value?.toInt() ?: 0,
                                        matches.groups[5]?.value?.toInt() ?: 0,
                                        matches.groups[6]?.value?.toInt() ?: 0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun sendDeltaCoordinates(pan: Float, tilt: Float) {
        sendCommand(String.format(Locale.US, "D %.2f %.2f", pan, tilt))
    }

    fun sendTargetCoordinates(pan: Float, tilt: Float) {
        sendCommand(String.format(Locale.US, "T %.2f %.2f", pan, tilt))
    }

    fun home() {
        sendCommand("H")
    }

    fun setLaser(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "L %d", if(enabled) 1 else 0))
    }

    fun setMotor(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "E %d", if(enabled) 1 else 0))
    }

    fun zero() {
        sendCommand(String.format(Locale.US, "Z"))
    }

    fun resetDevice() {
        sendCommand(String.format(Locale.US, "R"))
        disconnect()
    }

    fun calibrate() {
        sendCommand(String.format(Locale.US, "C"))
    }

    fun setHoldPosition(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "P %d", if(enabled) 1 else 0))
    }

    private fun sendCommand(command: String) {
        val characteristic = commandCharacteristic ?: return
        val g = gatt ?: return

        val payload = command.toByteArray(Charsets.UTF_8)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(characteristic, payload, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        } else {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            @Suppress("DEPRECATION")
            characteristic.value = payload
            @Suppress("DEPRECATION")
            g.writeCharacteristic(characteristic)
        }
    }
}
