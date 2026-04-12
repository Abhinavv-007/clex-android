package com.clex.android.data.transfer

/**
 * UI-level send route. Direct and Local map 1:1 to TransferMethod.
 * ClexLink resolves to a TransferMethod only after BLE discovery + route negotiation.
 */
enum class SendRoute {
    DIRECT,
    LOCAL,
    CLEX_LINK,
}

/** A Clex device discovered over BLE. */
data class NearbyDevice(
    val id: String,
    val displayName: String,
    val bleAddress: String,
    val rssi: Int = 0,
    val lastSeenMs: Long = System.currentTimeMillis(),
)

/** An invitation sent or received during a Clex Link session. */
data class NearbyInvite(
    val fromDeviceId: String,
    val fromDisplayName: String,
    val toDeviceId: String,
    val fileCount: Int,
    val totalBytes: Long,
    val networkFingerprint: String? = null,
    val routeHint: TransferMethod? = null,
    val timestampMs: Long = System.currentTimeMillis(),
)

/** Top-level state of the nearby BLE session. */
enum class NearbySessionState {
    /** BLE idle — not scanning or advertising. */
    IDLE,
    /** Scanning for nearby Clex devices. */
    SCANNING,
    /** Advertising this device to nearby Clex scanners. */
    ADVERTISING,
    /** Both scanning and advertising simultaneously. */
    DISCOVERING,
    /** Invite sent, waiting for accept/decline. */
    INVITE_PENDING,
    /** Invite received from another device. */
    INVITE_RECEIVED,
    /** Route negotiation in progress after accept. */
    NEGOTIATING,
    /** Route resolved — ready to hand off to transfer layer. */
    RESOLVED,
    /** Session cancelled or timed out. */
    CANCELLED,
    /** BLE not available or permission denied. */
    UNAVAILABLE,
}

/** Result of route negotiation after a Clex Link invite is accepted. */
data class ResolvedTransferRoute(
    val method: TransferMethod,
    val roomCode: String,
    val connectionKind: ConnectionKind,
)
