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
    /**
     * Scanning is active but BLE peripheral / advertise is unsupported on
     * this device, so it can still see other Clex phones and initiate
     * invites against them, but is invisible to peers.
     */
    SCAN_ONLY,
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

/**
 * Which side of a Clex Link handshake this device is acting as. Used by the
 * workspace UI to gate sender-vs-receiver `LaunchedEffect`s on `resolvedRoute`
 * so that during a tab Crossfade only the correct controller starts a
 * transfer (preventing a double-start when both tabs are momentarily
 * composed).
 */
enum class NearbyRole {
    SENDER,
    RECEIVER,
}
