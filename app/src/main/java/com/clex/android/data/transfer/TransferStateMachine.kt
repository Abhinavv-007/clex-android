package com.clex.android.data.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TransferStateMachine {
    private val _state = MutableStateFlow(TransferUiState())
    val state: StateFlow<TransferUiState> = _state.asStateFlow()

    fun setState(newState: TransferState) {
        _state.update {
            it.copy(
                state = newState,
                shareExpiresAtMillis = when (newState) {
                    TransferState.PREPARING,
                    TransferState.WAITING_PEER -> it.shareExpiresAtMillis
                    else -> null
                },
                error = if (newState == TransferState.FAILED) it.error else null,
            )
        }
    }

    fun setMethod(method: TransferMethod) {
        _state.update {
            it.copy(
                method = method,
                state = TransferState.IDLE,
                shareExpiresAtMillis = null,
                error = null,
                nearby = false,
                connectionKind = ConnectionKind.UNKNOWN,
                diagnosticCode = null,
                currentFile = null,
                receivedFiles = emptyList(),
            )
        }
    }

    fun setRoomCode(roomCode: String) {
        _state.update { it.copy(roomCode = roomCode.trim().uppercase()) }
    }

    fun setShareExpiry(durationMillis: Long) {
        val endAt = System.currentTimeMillis() + durationMillis.coerceAtLeast(1L)
        _state.update { it.copy(shareExpiresAtMillis = endAt) }
    }

    fun setProgress(bytesSent: Long, bytesTotal: Long) {
        val pct = if (bytesTotal > 0) {
            ((bytesSent.toDouble() / bytesTotal.toDouble()) * 100.0).coerceIn(0.0, 100.0).toInt()
        } else {
            0
        }
        _state.update { it.copy(bytesSent = bytesSent, bytesTotal = bytesTotal, progress = pct) }
    }

    fun setSpeed(speedBps: Long) {
        _state.update { it.copy(speedBps = speedBps) }
    }

    fun setCurrentFile(file: TransferFilePreview?) {
        _state.update { it.copy(currentFile = file) }
    }

    fun setConnectionKind(kind: ConnectionKind) {
        _state.update { it.copy(connectionKind = kind, nearby = kind == ConnectionKind.LAN) }
    }

    fun setError(error: String, diagnosticCode: String? = null) {
        _state.update {
            it.copy(
                state = TransferState.FAILED,
                shareExpiresAtMillis = null,
                error = error,
                currentFile = null,
                diagnosticCode = diagnosticCode ?: it.diagnosticCode,
            )
        }
    }

    fun expirePendingShare(error: String, diagnosticCode: String? = null) {
        _state.update {
            it.copy(
                state = TransferState.FAILED,
                roomCode = generateRoomCode(),
                shareExpiresAtMillis = null,
                error = error,
                currentFile = null,
                diagnosticCode = diagnosticCode ?: it.diagnosticCode,
            )
        }
    }

    fun addReceivedFile(file: ReceivedFile) {
        _state.update { current ->
            current.copy(
                receivedFiles = current.receivedFiles.filterNot { it.id == file.id } + file
            )
        }
    }

    fun clearReceivedFiles() {
        _state.update { it.copy(receivedFiles = emptyList()) }
    }

    fun reset(roomCode: String = generateRoomCode()) {
        _state.value = TransferUiState(roomCode = roomCode.trim().uppercase())
    }
}
