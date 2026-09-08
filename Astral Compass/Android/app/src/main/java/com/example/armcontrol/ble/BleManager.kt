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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Scanning : ConnectionState()
    data class Connecting(val deviceName: String) : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Failed(val message: String) : ConnectionState()
}

data class ScannedDevice(
    val device: BluetoothDevice,
    val name: String,
    val rssi: Int
)

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner get() = adapter?.bluetoothLeScanner

    private var gatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val _lastStatus = MutableStateFlow<String?>(null)
    val lastStatus: StateFlow<String?> = _lastStatus.asStateFlow()

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled ?: false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            val name = result.device.name ?: result.scanRecord?.deviceName ?: return
            if (!name.startsWith(BleConstants.DEVICE_NAME_PREFIX)) return

            val current = _scannedDevices.value.toMutableList()
            val idx = current.indexOfFirst { it.device.address == result.device.address }
            val entry = ScannedDevice(result.device, name, result.rssi)
            if (idx >= 0) current[idx] = entry else current.add(entry)
            _scannedDevices.value = current
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

        val filters = listOf(ScanFilter.Builder().setServiceUuid(null).build())
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(filters, settings, scanCallback)

        // Auto-stop after 10s so we don't drain the battery scanning forever.
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
                    g.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    commandCharacteristic = null
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }

        fun initNotificationCharacteristic(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            g.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(BleConstants.CLIENT_CHARACTERISTIC_CONFIG_UUID)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    g.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(descriptor)
                }
            }
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

            service.getCharacteristic(BleConstants.STATUS_UUID)?.let { c ->
                initNotificationCharacteristic(g, c)
            }

            _connectionState.value = ConnectionState.Connected(g.device.name ?: g.device.address)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            when(characteristic.uuid) {
                BleConstants.STATUS_UUID -> {
                    _lastStatus.value = String(value, Charsets.UTF_8)
                }
            }
        }

        // Pre-API 33 callback (deprecated but still called on older devices)
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            when(characteristic.uuid) {
                BleConstants.STATUS_UUID -> {
                    _lastStatus.value = String(characteristic.value ?: return, Charsets.UTF_8)
                }
            }
        }
    }

    fun sendTargetCoordinates(pan: Float, tilt: Float) {
        sendCommand(String.format(Locale.US, "C %.2f %.2f", pan, tilt))
    }

    fun home() {
        sendCommand("H")
    }

    fun setLaser(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "L %i", enabled))
    }

    fun setMotor(enabled: Boolean) {
        sendCommand(String.format(Locale.US, "E %i", enabled))
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
