package com.clex.android.data.transfer

import android.net.Uri
import java.security.SecureRandom

enum class TransferState {
    IDLE,
    PREPARING,
    WAITING_PEER,
    CONNECTING,
    TRANSFERRING,
    COMPLETE,
    FAILED,
}

enum class TransferMethod(val webValue: String) {
    WEBRTC("webrtc"),
    LOCAL("local"),
}

enum class ConnectionKind {
    LAN,
    INTERNET,
    UNKNOWN,
}

data class TransferFilePreview(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long,
)

data class ReceivedFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReceivedFile) return false
        return id == other.id && name == other.name && mimeType == other.mimeType && size == other.size
    }

    override fun hashCode(): Int = id.hashCode()
}

data class TransferUiState(
    val state: TransferState = TransferState.IDLE,
    val method: TransferMethod = TransferMethod.WEBRTC,
    val roomCode: String = generateRoomCode(),
    val shareExpiresAtMillis: Long? = null,
    val progress: Int = 0,
    val bytesSent: Long = 0L,
    val bytesTotal: Long = 0L,
    val speedBps: Long = 0L,
    val nearby: Boolean = false,
    val connectionKind: ConnectionKind = ConnectionKind.UNKNOWN,
    val diagnosticCode: String? = null,
    val error: String? = null,
    val currentFile: TransferFilePreview? = null,
    val receivedFiles: List<ReceivedFile> = emptyList(),
)

data class WorkspaceSelectedFile(
    val id: String,
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String,
)

data class TransferPayloadFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
)

private val roomCodeRandom = SecureRandom()
private const val ROOM_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

fun generateRoomCode(length: Int = 6): String {
    val bytes = ByteArray(length)
    roomCodeRandom.nextBytes(bytes)
    return buildString(length) {
        bytes.forEach { byte ->
            append(ROOM_CODE_CHARS[(byte.toInt() and 0xFF) % ROOM_CODE_CHARS.length])
        }
    }
}

fun isValidRoomCode(code: String): Boolean = code.trim().uppercase().matches(Regex("^[A-Z0-9]{6}$"))

fun buildReceiveLink(roomCode: String, method: TransferMethod): String {
    val normalizedCode = roomCode.trim().uppercase()
    return "https://clex.in/receive?code=$normalizedCode&mode=${method.webValue}"
}
