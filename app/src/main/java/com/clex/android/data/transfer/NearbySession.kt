package com.clex.android.data.transfer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.LinkAddress
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.Inet4Address
import java.nio.charset.StandardCharsets
import java.util.UUID

// ═══════════════════════════════════════════════════
//  CLEX — Nearby Session (BLE Discovery + Invite Layer)
//
//  BLE is used ONLY for:
//    - Advertising this device
//    - Scanning for nearby Clex devices
//    - Inviting another nearby device
//    - Receiving accept / decline over GATT
//
//  File payloads are NEVER transferred over BLE.
//  After accept, route negotiation resolves to LOCAL or
//  WEBRTC and hands off to existing TransferCoordinator.
// ═══════════════════════════════════════════════════

private val CLEX_SERVICE_UUID: UUID = UUID.fromString("3e8f4f36-0c8e-4e11-90e0-1d35d9ef0e31")
private val CLEX_INVITE_UUID: UUID = UUID.fromString("3e8f4f36-0c8e-4e11-90e0-1d35d9ef0e32")
private val CLEX_RESPONSE_UUID: UUID = UUID.fromString("3e8f4f36-0c8e-4e11-90e0-1d35d9ef0e33")
private val CLIENT_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
private val CLEX_SERVICE_PARCEL_UUID = ParcelUuid(CLEX_SERVICE_UUID)

private const val STALE_DEVICE_TIMEOUT_MS = 15_000L
private const val INVITE_TIMEOUT_MS = 30_000L
private const val SCAN_CLEANUP_INTERVAL_MS = 5_000L

// ATT default MTU is 23 bytes (-> 20-byte useful payload), which truncates the
// invite/response JSON. We negotiate a larger MTU before service discovery so
// a single write/notification carries the full payload.
private const val REQUESTED_ATT_MTU = 247
private const val MIN_USABLE_ATT_MTU = 64

private const val CLEX_PREFS = "clex_prefs"
private const val DEVICE_NAME_KEY = "clex_device_name"

private const val RESPONSE_STATUS_ACCEPTED = "accepted"
private const val RESPONSE_STATUS_DECLINED = "declined"

class NearbySession(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _sessionState = MutableStateFlow(NearbySessionState.IDLE)
    val sessionState: StateFlow<NearbySessionState> = _sessionState.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<NearbyDevice>> = _nearbyDevices.asStateFlow()

    private val _inboundInvite = MutableStateFlow<NearbyInvite?>(null)
    val inboundInvite: StateFlow<NearbyInvite?> = _inboundInvite.asStateFlow()

    private val _resolvedRoute = MutableStateFlow<ResolvedTransferRoute?>(null)
    val resolvedRoute: StateFlow<ResolvedTransferRoute?> = _resolvedRoute.asStateFlow()

    private var cleanupJob: Job? = null
    private var inviteTimeoutJob: Job? = null
    private var discoveryRequested = false

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private val bleScanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner
    private val bleAdvertiser: BluetoothLeAdvertiser? get() = bluetoothAdapter?.bluetoothLeAdvertiser

    private var gattServer: BluetoothGattServer? = null
    private var activeGatt: BluetoothGatt? = null
    private var outboundInvite: NearbyInvite? = null
    private var inboundInviteDevice: BluetoothDevice? = null
    private var responseSubscriberAddress: String? = null
    private var inviteDescriptorReady = false
    private var mtuNegotiated = false

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun missingBlePermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
            ).filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION).filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /** Start BLE discovery (scan + advertise + GATT server). */
    fun startDiscovery() {
        if (!isBleAvailable()) {
            _sessionState.value = NearbySessionState.UNAVAILABLE
            return
        }

        discoveryRequested = true
        stopScan()
        stopAdvertise()
        stopGattServer()
        disconnectActiveGatt()

        _nearbyDevices.value = emptyList()
        _inboundInvite.value = null
        _resolvedRoute.value = null
        responseSubscriberAddress = null
        inviteDescriptorReady = false

        startGattServer()
        startScan()
        startAdvertise()
        _sessionState.value = NearbySessionState.DISCOVERING
        startStaleCleanup()
    }

    /** Stop BLE scanning and advertising. */
    fun stopDiscovery() {
        discoveryRequested = false
        cleanupJob?.cancel()
        cleanupJob = null
        inviteTimeoutJob?.cancel()
        inviteTimeoutJob = null
        stopScan()
        stopAdvertise()
        stopGattServer()
        disconnectActiveGatt()
        _inboundInvite.value = null
        if (_sessionState.value != NearbySessionState.RESOLVED) {
            _sessionState.value = NearbySessionState.IDLE
        }
    }

    /** Sender taps a device → send invite over BLE GATT. */
    @SuppressLint("MissingPermission")
    fun sendInvite(device: NearbyDevice, fileCount: Int, totalBytes: Long) {
        if (!isBleAvailable()) {
            _sessionState.value = NearbySessionState.UNAVAILABLE
            return
        }

        val remoteDevice = runCatching { bluetoothAdapter?.getRemoteDevice(device.bleAddress) }.getOrNull()
            ?: run {
                _sessionState.value = NearbySessionState.DISCOVERING
                return
            }

        outboundInvite = NearbyInvite(
            fromDeviceId = bluetoothAdapter?.address ?: getLocalDeviceName(),
            fromDisplayName = getLocalDeviceName(),
            toDeviceId = device.id,
            fileCount = fileCount,
            totalBytes = totalBytes,
            networkFingerprint = currentWifiFingerprint(),
            routeHint = null,
        )
        _resolvedRoute.value = null
        _sessionState.value = NearbySessionState.INVITE_PENDING
        startInviteTimeout()
        disconnectActiveGatt()
        inviteDescriptorReady = false
        mtuNegotiated = false

        activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            remoteDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            remoteDevice.connectGatt(context, false, gattCallback)
        }
    }

    /** Receiver accepts an inbound invite. */
    fun acceptInvite() {
        val invite = _inboundInvite.value ?: return
        inviteTimeoutJob?.cancel()
        _sessionState.value = NearbySessionState.NEGOTIATING

        scope.launch {
            val resolved = negotiateRoute(invite)
            _resolvedRoute.value = resolved
            notifyInviteResponse(status = RESPONSE_STATUS_ACCEPTED, resolved = resolved)
            _sessionState.value = NearbySessionState.RESOLVED
        }
    }

    /** Receiver declines an inbound invite. */
    fun declineInvite() {
        inviteTimeoutJob?.cancel()
        notifyInviteResponse(status = RESPONSE_STATUS_DECLINED, resolved = null)
        _inboundInvite.value = null
        _resolvedRoute.value = null
        _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
    }

    /** Cancel a pending outbound invite. */
    fun cancelInvite() {
        inviteTimeoutJob?.cancel()
        disconnectActiveGatt()
        outboundInvite = null
        _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
    }

    /** Full teardown — call from dispose. */
    fun destroy() {
        stopDiscovery()
        _sessionState.value = NearbySessionState.IDLE
        _nearbyDevices.value = emptyList()
        _inboundInvite.value = null
        _resolvedRoute.value = null
        outboundInvite = null
        inboundInviteDevice = null
    }

    // ── Route Negotiation ───────────────────────────

    private suspend fun negotiateRoute(invite: NearbyInvite): ResolvedTransferRoute {
        val localFingerprint = currentWifiFingerprint()
        val sameWifi = !localFingerprint.isNullOrBlank() &&
            !invite.networkFingerprint.isNullOrBlank() &&
            localFingerprint == invite.networkFingerprint

        val method = if (sameWifi) TransferMethod.LOCAL else TransferMethod.WEBRTC
        val kind = if (sameWifi) ConnectionKind.LAN else ConnectionKind.INTERNET
        val roomCode = generateRoomCode()
        return ResolvedTransferRoute(
            method = method,
            roomCode = roomCode,
            connectionKind = kind,
        )
    }

    // ── Network Fingerprint ─────────────────────────

    private fun currentWifiFingerprint(): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return null
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

        val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
        val ipv4Address = linkProperties.linkAddresses
            .firstOrNull { it.address is Inet4Address }
            ?.let(::ipv4Fingerprint)
            ?: return null
        val gateway = linkProperties.routes.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress.orEmpty()
        return "$ipv4Address|$gateway"
    }

    private fun ipv4Fingerprint(linkAddress: LinkAddress): String {
        val hostAddress = linkAddress.address.hostAddress ?: return ""
        val prefixLength = linkAddress.prefixLength.coerceIn(0, 32)
        val octets = hostAddress.split(".").mapNotNull { it.toIntOrNull() }.toMutableList()
        if (octets.size != 4) return hostAddress

        val hostBits = 32 - prefixLength
        when {
            hostBits >= 24 -> {
                octets[1] = 0
                octets[2] = 0
                octets[3] = 0
            }
            hostBits >= 16 -> {
                octets[2] = 0
                octets[3] = 0
            }
            hostBits >= 8 -> {
                octets[3] = 0
            }
        }
        return "${octets.joinToString(".")}/$prefixLength"
    }

    // ── BLE Helpers ─────────────────────────────────

    private fun isBleAvailable(): Boolean {
        val adapter = bluetoothAdapter ?: return false
        if (!adapter.isEnabled) return false
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) return false
        if (bleScanner == null || bleAdvertiser == null) return false
        return hasBlePermissions()
    }

    private fun hasBlePermissions(): Boolean = missingBlePermissions().isEmpty()

    @SuppressLint("MissingPermission")
    private fun startScan() {
        val scanner = bleScanner ?: return
        val filter = ScanFilter.Builder()
            .setServiceUuid(CLEX_SERVICE_PARCEL_UUID)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        runCatching { bleScanner?.stopScan(scanCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertise() {
        val advertiser = bleAdvertiser ?: return
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val deviceName = getLocalDeviceName()
        val nameBytes = deviceName.toByteArray(StandardCharsets.UTF_8).take(20).toByteArray()
        val data = AdvertiseData.Builder()
            .addServiceUuid(CLEX_SERVICE_PARCEL_UUID)
            .addServiceData(CLEX_SERVICE_PARCEL_UUID, nameBytes)
            .setIncludeDeviceName(false)
            .build()
        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertise() {
        runCatching { bleAdvertiser?.stopAdvertising(advertiseCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        if (gattServer != null) return
        val manager = bluetoothManager ?: return
        val server = manager.openGattServer(context, gattServerCallback) ?: return
        server.addService(buildGattService())
        gattServer = server
    }

    private fun stopGattServer() {
        runCatching { gattServer?.close() }
        gattServer = null
        inboundInviteDevice = null
        responseSubscriberAddress = null
    }

    @SuppressLint("MissingPermission")
    private fun disconnectActiveGatt() {
        runCatching { activeGatt?.disconnect() }
        runCatching { activeGatt?.close() }
        activeGatt = null
        mtuNegotiated = false
    }

    @SuppressLint("MissingPermission")
    fun getLocalDeviceName(): String {
        val prefs = context.getSharedPreferences(CLEX_PREFS, Context.MODE_PRIVATE)
        val custom = prefs.getString(DEVICE_NAME_KEY, null)
        if (!custom.isNullOrBlank()) return custom
        return bluetoothAdapter?.name
            ?: Build.MODEL.ifBlank { "Clex Device" }
    }

    fun setLocalDeviceName(name: String) {
        context.getSharedPreferences(CLEX_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(DEVICE_NAME_KEY, name.trim().take(30))
            .apply()
    }

    private fun buildGattService(): BluetoothGattService {
        val service = BluetoothGattService(CLEX_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val inviteCharacteristic = BluetoothGattCharacteristic(
            CLEX_INVITE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE,
        )

        val responseCharacteristic = BluetoothGattCharacteristic(
            CLEX_RESPONSE_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ,
        )
        val cccd = BluetoothGattDescriptor(
            CLIENT_CONFIG_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE,
        )
        responseCharacteristic.addDescriptor(cccd)

        service.addCharacteristic(inviteCharacteristic)
        service.addCharacteristic(responseCharacteristic)
        return service
    }

    private fun responseCharacteristic(): BluetoothGattCharacteristic? {
        return gattServer
            ?.getService(CLEX_SERVICE_UUID)
            ?.getCharacteristic(CLEX_RESPONSE_UUID)
    }

    private fun writeInviteIfReady(gatt: BluetoothGatt) {
        if (!inviteDescriptorReady) return
        val payload = outboundInvite ?: return
        val inviteCharacteristic = gatt.getService(CLEX_SERVICE_UUID)
            ?.getCharacteristic(CLEX_INVITE_UUID)
            ?: return
        val bytes = encodeInvite(payload)
        inviteCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        inviteCharacteristic.value = bytes
        gatt.writeCharacteristic(inviteCharacteristic)
    }

    private fun encodeInvite(invite: NearbyInvite): ByteArray {
        return JSONObject()
            .put("fromDeviceId", invite.fromDeviceId)
            .put("fromDisplayName", invite.fromDisplayName)
            .put("toDeviceId", invite.toDeviceId)
            .put("fileCount", invite.fileCount)
            .put("totalBytes", invite.totalBytes)
            .put("networkFingerprint", invite.networkFingerprint ?: JSONObject.NULL)
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
    }

    private fun decodeInvite(bytes: ByteArray, sourceDevice: BluetoothDevice): NearbyInvite? {
        return runCatching {
            val obj = JSONObject(String(bytes, StandardCharsets.UTF_8))
            NearbyInvite(
                fromDeviceId = obj.optString("fromDeviceId", sourceDevice.address),
                fromDisplayName = obj.optString("fromDisplayName", sourceDevice.address),
                toDeviceId = obj.optString("toDeviceId"),
                fileCount = obj.optInt("fileCount", 0),
                totalBytes = obj.optLong("totalBytes", 0L),
                networkFingerprint = obj.optString("networkFingerprint").takeIf { it.isNotBlank() && it != "null" },
                routeHint = null,
            )
        }.getOrNull()
    }

    private fun encodeResponse(status: String, resolved: ResolvedTransferRoute?): ByteArray {
        return JSONObject()
            .put("status", status)
            .put("roomCode", resolved?.roomCode ?: JSONObject.NULL)
            .put("method", resolved?.method?.name ?: JSONObject.NULL)
            .put("connectionKind", resolved?.connectionKind?.name ?: JSONObject.NULL)
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
    }

    private fun decodeResponse(bytes: ByteArray): ResolvedTransferRoute? {
        return runCatching {
            val obj = JSONObject(String(bytes, StandardCharsets.UTF_8))
            when (obj.optString("status")) {
                RESPONSE_STATUS_DECLINED -> null
                RESPONSE_STATUS_ACCEPTED -> {
                    val method = TransferMethod.valueOf(obj.getString("method"))
                    val kind = ConnectionKind.valueOf(obj.getString("connectionKind"))
                    ResolvedTransferRoute(
                        method = method,
                        roomCode = obj.getString("roomCode"),
                        connectionKind = kind,
                    )
                }
                else -> null
            }
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    private fun notifyInviteResponse(status: String, resolved: ResolvedTransferRoute?) {
        val device = inboundInviteDevice ?: return
        val responseChar = responseCharacteristic() ?: return
        if (responseSubscriberAddress != device.address) return
        responseChar.value = encodeResponse(status, resolved)
        gattServer?.notifyCharacteristicChanged(device, responseChar, false)
    }

    // ── Scan Callback ───────────────────────────────

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val localAddress = runCatching { bluetoothAdapter?.address }.getOrNull()
            if (result.device.address == localAddress) return

            val serviceData = result.scanRecord?.getServiceData(CLEX_SERVICE_PARCEL_UUID)
            val name = if (serviceData != null && serviceData.isNotEmpty()) {
                String(serviceData, StandardCharsets.UTF_8)
            } else {
                result.device.name ?: result.device.address
            }
            val device = NearbyDevice(
                id = result.device.address,
                displayName = name,
                bleAddress = result.device.address,
                rssi = result.rssi,
                lastSeenMs = System.currentTimeMillis(),
            )
            _nearbyDevices.update { current ->
                val updated = current.toMutableList()
                val index = updated.indexOfFirst { it.id == device.id }
                if (index >= 0) {
                    updated[index] = device
                } else {
                    updated.add(device)
                }
                updated.sortedByDescending { it.rssi }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            if (_sessionState.value == NearbySessionState.DISCOVERING) {
                _sessionState.value = NearbySessionState.ADVERTISING
            }
        }
    }

    // ── Advertise Callback ──────────────────────────

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) = Unit

        override fun onStartFailure(errorCode: Int) {
            if (_sessionState.value == NearbySessionState.DISCOVERING) {
                _sessionState.value = NearbySessionState.SCANNING
            }
        }
    }

    // ── GATT Client / Sender Side ───────────────────

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // Request a larger ATT MTU before service discovery so the
                    // invite write and response notification carry the full
                    // JSON in a single ATT PDU instead of being truncated to
                    // ~20 bytes by the default 23-byte MTU.
                    val requested = runCatching { gatt.requestMtu(REQUESTED_ATT_MTU) }.getOrDefault(false)
                    if (!requested) {
                        gatt.discoverServices()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (_sessionState.value == NearbySessionState.INVITE_PENDING) {
                        _sessionState.value = if (discoveryRequested) {
                            NearbySessionState.DISCOVERING
                        } else {
                            NearbySessionState.IDLE
                        }
                    }
                    if (activeGatt == gatt) {
                        disconnectActiveGatt()
                    }
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            // Even if MTU negotiation failed (status != GATT_SUCCESS) the link
            // remains usable at the prior MTU; just continue and let the write
            // surface a truncation failure if the JSON is too large.
            mtuNegotiated = status == BluetoothGatt.GATT_SUCCESS && mtu >= MIN_USABLE_ATT_MTU
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            val responseChar = gatt.getService(CLEX_SERVICE_UUID)
                ?.getCharacteristic(CLEX_RESPONSE_UUID)
                ?: return
            val descriptor = responseChar.getDescriptor(CLIENT_CONFIG_UUID) ?: return
            gatt.setCharacteristicNotification(responseChar, true)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (descriptor.uuid != CLIENT_CONFIG_UUID || status != BluetoothGatt.GATT_SUCCESS) return
            inviteDescriptorReady = true
            writeInviteIfReady(gatt)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (characteristic.uuid != CLEX_INVITE_UUID || status != BluetoothGatt.GATT_SUCCESS) {
                if (_sessionState.value == NearbySessionState.INVITE_PENDING) {
                    _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            if (characteristic.uuid != CLEX_RESPONSE_UUID) return
            inviteTimeoutJob?.cancel()
            val responseText = String(characteristic.value ?: ByteArray(0), StandardCharsets.UTF_8)
            val response = decodeResponse(responseText.toByteArray(StandardCharsets.UTF_8))
            val responseJson = runCatching { JSONObject(responseText) }.getOrNull()
            when (responseJson?.optString("status")) {
                RESPONSE_STATUS_ACCEPTED -> {
                    if (response != null) {
                        disconnectActiveGatt()
                        _resolvedRoute.value = response
                        _sessionState.value = NearbySessionState.RESOLVED
                    } else {
                        _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
                    }
                }
                RESPONSE_STATUS_DECLINED -> {
                    disconnectActiveGatt()
                    _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
                }
            }
        }
    }

    // ── GATT Server / Receiver Side ─────────────────

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED && inboundInviteDevice?.address == device.address) {
                inboundInviteDevice = null
                responseSubscriberAddress = null
                if (_sessionState.value == NearbySessionState.INVITE_RECEIVED) {
                    _inboundInvite.value = null
                    _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
                }
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?,
        ) {
            if (descriptor.uuid == CLIENT_CONFIG_UUID) {
                responseSubscriberAddress = if (value?.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == true) {
                    device.address
                } else {
                    null
                }
            }
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?,
        ) {
            if (characteristic.uuid != CLEX_INVITE_UUID || value == null) {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
                }
                return
            }

            val invite = decodeInvite(value, device)
            if (invite == null) {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
                }
                return
            }

            inboundInviteDevice = device
            _resolvedRoute.value = null
            _inboundInvite.value = invite
            _sessionState.value = NearbySessionState.INVITE_RECEIVED
            startInviteTimeout()

            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
        }
    }

    // ── Stale Device Cleanup ────────────────────────

    private fun startStaleCleanup() {
        cleanupJob?.cancel()
        cleanupJob = scope.launch {
            while (discoveryRequested) {
                delay(SCAN_CLEANUP_INTERVAL_MS)
                val cutoff = System.currentTimeMillis() - STALE_DEVICE_TIMEOUT_MS
                _nearbyDevices.update { devices ->
                    devices.filter { it.lastSeenMs >= cutoff }
                }
            }
        }
    }

    // ── Invite Timeout ──────────────────────────────

    private fun startInviteTimeout() {
        inviteTimeoutJob?.cancel()
        inviteTimeoutJob = scope.launch {
            delay(INVITE_TIMEOUT_MS)
            if (_sessionState.value == NearbySessionState.INVITE_PENDING ||
                _sessionState.value == NearbySessionState.INVITE_RECEIVED
            ) {
                disconnectActiveGatt()
                _inboundInvite.value = null
                _sessionState.value = if (discoveryRequested) NearbySessionState.DISCOVERING else NearbySessionState.IDLE
            }
        }
    }
}
