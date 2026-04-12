package com.clex.android.data

import android.net.Uri
import com.clex.android.data.transfer.TransferMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PendingReceiveLink(
    val roomCode: String,
    val method: TransferMethod,
)

data class PendingSecretLink(
    val secretId: String,
    val keyB64: String,
)

object AppLinkStore {
    private val _pendingReceiveLink = MutableStateFlow<PendingReceiveLink?>(null)
    private val _pendingSecretLink = MutableStateFlow<PendingSecretLink?>(null)
    private val _pendingInboundShare = MutableStateFlow<List<Uri>?>(null)

    val pendingReceiveLink: StateFlow<PendingReceiveLink?> = _pendingReceiveLink.asStateFlow()
    val pendingSecretLink: StateFlow<PendingSecretLink?> = _pendingSecretLink.asStateFlow()
    val pendingInboundShare: StateFlow<List<Uri>?> = _pendingInboundShare.asStateFlow()

    fun queueReceiveLink(link: PendingReceiveLink) {
        _pendingReceiveLink.value = link
    }

    fun consumeReceiveLink(): PendingReceiveLink? {
        val current = _pendingReceiveLink.value
        _pendingReceiveLink.value = null
        return current
    }

    fun queueSecretLink(link: PendingSecretLink) {
        _pendingSecretLink.value = link
    }

    fun consumeSecretLink(): PendingSecretLink? {
        val current = _pendingSecretLink.value
        _pendingSecretLink.value = null
        return current
    }

    fun queueInboundShare(uris: List<Uri>) {
        _pendingInboundShare.value = uris
    }

    fun consumeInboundShare(): List<Uri>? {
        val current = _pendingInboundShare.value
        _pendingInboundShare.value = null
        return current
    }
}
