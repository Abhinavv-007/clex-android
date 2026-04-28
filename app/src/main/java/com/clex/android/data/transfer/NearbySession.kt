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
private const val INSTANCE_ID_KEY = "clex_instance_id"

/**
 * Unassigned-but-stable manufacturer ID used to embed a per-install
 * Clex instance identifier in advertise packets. Receivers use it to
 * suppress self-discovery (the OS reports `02:00:00:00:00:00` for the
 * local Bluetooth MAC since Android 6, so MAC-based self-filtering
 * never works).
 */
private const val CLEX_MANUFACTURER_ID = 0xCE48
private const val INSTANCE_ID_BYTES = 4

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

    /**
     * Which side of the current Clex Link handshake the local device is
     * acting as, if any. SENDER when we initiated by calling [sendInvite],
     * RECEIVER when we accepted an incoming invite via [acceptInvite],
     * `null` while there is no in-flight handshake. The workspace UI uses
     * this to gate sender-vs-receiver `LaunchedEffect`s that listen to
     * [resolvedRoute] so that during a tab Crossfade only the correct
     * controller starts a transfer.
     */
    private val _currentRole = MutableStateFlow<NearbyRole?>(null)
    val currentRole: StateFlow<NearbyRole?> = _currentRole.asStateFlow()

    private var cleanupJob: Job? = null
    private var inviteTimeoutJob: Job? = null
    private var discoveryRequested = false
    // Cached result of peripheralSupported() captured when discovery was last
    // requested. Tracks the device's advertising capability for the lifetime
    // of the discovery session so that fallback transitions back to the
    // "scanning" base state can pick the correct visible-vs-invisible variant
    // (DISCOVERING vs SCAN_ONLY) instead of unconditionally re-asserting
    // DISCOVERING and silently flipping a scan-only phone to "visible".
    private var canAdvertise = false

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private val bleScanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner
    private val bleAdvertiser: BluetoothLeAdvertiser? get() = bluetoothAdapter?.bluetoothLeAdvertiser

    private var gattServer: BluetoothGattServer? = null
    private var serviceAdded = false
    private var advertisePending = false
    private var activeGatt: BluetoothGatt? = null
    private var outboundInvite: NearbyInvite? = null
    private var inboundInviteDevice: BluetoothDevice? = null
    private var responseSubscriberAddress: String? = null
    private var inviteDescriptorReady = false
    private var mtuNegotiated = false
    private val localInstanceId: ByteArray by lazy { ensureInstanceId() }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Whether this device can advertise as a BLE peripheral. Many mid-range
     * Android phones report `bluetoothLeAdvertiser == null` or
     * `isMultipleAdvertisementSupported() == false`, which makes them
     * invisible to peers — they can still scan for and invite phones that
     * do advertise.
     */
    fun peripheralSupported(): Boolean {
        val adapter = bluetoothAdapter ?: return false
        if (!adapter.isEnabled) return false
        if (adapter.bluetoothLeAdvertiser == null) return false
        return runCatching { adapter.isMultipleAdvertisementSupported }.getOrDefault(false)
    }

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
        _currentRole.value = null
        responseSubscriberAddress = null
        inviteDescriptorReady = false

        canAdvertise = peripheralSupported()
        if (canAdvertise) {
            // Open the GATT server first; advertise is deferred until
            // onServiceAdded fires so peers don't connect before the
            // service is actually queryable.
            startGattServer()
        }
        startScan()
        _sessionState.value = scanningBaseState()
        startStaleCleanup()
    }

    /**
     * The "we are scanning, no in-flight invite" base state. Returns
     * [NearbySessionState.DISCOVERING] when the device can also advertise
     * itself, or [NearbySessionState.SCAN_ONLY] when it can only scan.
     * Every fallback transition that returns to "just scanning" must use
     * this rather than a hardcoded `DISCOVERING`, otherwise scan-only phones
     * will silently lose their "invisible" indicator after the first invite,
     * decline, timeout, or peer-disconnect.
     */
    private fun scanningBaseState(): NearbySessionState =
        if (canAdvertise) NearbySessionState.DISCOVERING else NearbySessionState.SCAN_ONLY

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
            _currentRole.value = null
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
                _sessionState.value = scanningBaseState()
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
        _currentRole.value = NearbyRole.SENDER
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
        _currentRole.value = NearbyRole.RECEIVER
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
        _currentRole.value = null
        _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
    }

    /** Cancel a pending outbound invite. */
    fun cancelInvite() {
        inviteTimeoutJob?.cancel()
        disconnectActiveGatt()
        outboundInvite = null
        _currentRole.value = null
        _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
    }

    /** Full teardown — call from dispose. */
    fun destroy() {
        stopDiscovery()
        _sessionState.value = NearbySessionState.IDLE
        _nearbyDevices.value = emptyList()
        _inboundInvite.value = null
        _resolvedRoute.value = null
        _currentRole.value = null
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
        // Scanning is required; advertising is optional and surfaced via
        // peripheralSupported() / NearbySessionState.SCAN_ONLY.
        if (bleScanner == null) return false
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
            .addManufacturerData(CLEX_MANUFACTURER_ID, localInstanceId)
            .setIncludeDeviceName(false)
            .build()
        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertise() {
        advertisePending = false
        runCatching { bleAdvertiser?.stopAdvertising(advertiseCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        if (gattServer != null) return
        val manager = bluetoothManager ?: return
        val server = manager.openGattServer(context, gattServerCallback) ?: return
        gattServer = server
        serviceAdded = false
        advertisePending = true
        // addService is async — onServiceAdded fires startAdvertise so peers
        // can't discover & connect before the service is queryable.
        if (!server.addService(buildGattService())) {
            // queueing failed; fall back to the legacy ordering so we still
            // attempt to advertise.
            advertisePending = false
            startAdvertise()
        }
    }

    private fun stopGattServer() {
        serviceAdded = false
        advertisePending = false
        runCatching { gattServer?.close() }
        gattServer = null
        inboundInviteDevice = null
        responseSubscriberAddress = null
    }

    private fun ensureInstanceId(): ByteArray {
        val prefs = context.getSharedPreferences(CLEX_PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getString(INSTANCE_ID_KEY, null)
        if (stored != null && stored.length == INSTANCE_ID_BYTES * 2) {
            val parsed = ByteArray(INSTANCE_ID_BYTES)
            for (i in 0 until INSTANCE_ID_BYTES) {
                parsed[i] = stored.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            return parsed
        }
        val fresh = ByteArray(INSTANCE_ID_BYTES)
        java.security.SecureRandom().nextBytes(fresh)
        val hex = fresh.joinToString("") { "%02x".format(it.toInt() and 0xff) }
        prefs.edit().putString(INSTANCE_ID_KEY, hex).apply()
        return fresh
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
            // Self-filter: bluetoothAdapter.address returns 02:00:00:00:00:00
            // on Android 6+, so MAC-based comparison never works. We compare
            // against the per-install instance id we publish in manufacturer
            // data instead.
            val advertisedInstanceId = result.scanRecord
                ?.getManufacturerSpecificData(CLEX_MANUFACTURER_ID)
            if (advertisedInstanceId != null && advertisedInstanceId.contentEquals(localInstanceId)) {
                return
            }

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
            when (_sessionState.value) {
                NearbySessionState.DISCOVERING ->
                    // Scan failed but we can still be visible to peers via
                    // our own advertise.
                    _sessionState.value = NearbySessionState.ADVERTISING
                NearbySessionState.SCAN_ONLY ->
                    // No advertising fallback available; the device is
                    // effectively offline for Clex Link.
                    _sessionState.value = NearbySessionState.UNAVAILABLE
                else -> Unit
            }
        }
    }

    // ── Advertise Callback ──────────────────────────

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            advertisePending = false
        }

        override fun onStartFailure(errorCode: Int) {
            advertisePending = false
            // Mark advertising as unavailable for the rest of this discovery
            // session so subsequent fallback transitions via scanningBaseState()
            // also pick SCAN_ONLY rather than reverting to DISCOVERING. Without
            // this, peripheralSupported() returning true at startDiscovery time
            // but startAdvertise failing at runtime (e.g. system resource
            // exhaustion, too many active advertisers) would let the next
            // invite/decline/timeout silently flip the state back to
            // DISCOVERING and hide the "INVISIBLE TO OTHER PHONES" banner.
            canAdvertise = false
            if (_sessionState.value == NearbySessionState.DISCOVERING) {
                _sessionState.value = NearbySessionState.SCAN_ONLY
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
                        _currentRole.value = null
                        _sessionState.value = if (discoveryRequested) {
                            scanningBaseState()
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
                    _currentRole.value = null
                    _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
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
                        _currentRole.value = null
                        _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
                    }
                }
                RESPONSE_STATUS_DECLINED -> {
                    disconnectActiveGatt()
                    _currentRole.value = null
                    _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
                }
            }
        }
    }

    // ── GATT Server / Receiver Side ─────────────────

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onServiceAdded(status: Int, service: BluetoothGattService) {
            if (service.uuid != CLEX_SERVICE_UUID) return
            serviceAdded = status == BluetoothGatt.GATT_SUCCESS
            // Only now is the service queryable; safe to start advertising.
            if (serviceAdded && advertisePending) {
                startAdvertise()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED && inboundInviteDevice?.address == device.address) {
                inboundInviteDevice = null
                responseSubscriberAddress = null
                if (_sessionState.value == NearbySessionState.INVITE_RECEIVED) {
                    _inboundInvite.value = null
                    _currentRole.value = null
                    _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
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
                _currentRole.value = null
                _sessionState.value = if (discoveryRequested) scanningBaseState() else NearbySessionState.IDLE
            }
        }
    }
}
