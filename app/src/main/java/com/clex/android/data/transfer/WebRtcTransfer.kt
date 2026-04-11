package com.clex.android.data.transfer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.IceCandidate as RtcIceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private const val DATA_CHANNEL_LABEL = "clex-transfer"
private const val CHUNK_SIZE = 64 * 1024
private const val BUFFER_THRESHOLD = 256 * 1024L

class WebRtcTransfer(
    private val signalingBaseUrl: String,
    private val roomCode: String,
    private val role: String,
    private val method: TransferMethod,
    private val stateMachine: TransferStateMachine,
    private val factory: PeerConnectionFactory,
    private val stunServers: List<String> = listOf(
        "stun:stun.l.google.com:19302",
        "stun:stun.cloudflare.com:3478",
    ),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val signaling = SignalingClient(signalingBaseUrl, roomCode)

    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var pendingFiles: List<Pair<ByteArray, TransferFilePreview>> = emptyList()
    private val queuedRemoteIceCandidates = mutableListOf<IceCandidate>()

    private var transferComplete = false
    private var transferFailed = false

    private val assemblyBuffer = ByteArrayOutputStream()
    private var currentReceivingFile: ReceivingFileMeta? = null
    private var totalExpectedBytes = 0L
    private var totalReceivedBytes = 0L

    fun prepareFiles(files: List<Pair<ByteArray, TransferFilePreview>>) {
        pendingFiles = files
    }

    suspend fun initSender() {
        stateMachine.setState(TransferState.PREPARING)
        val connectResult = signaling.connect(role = "sender", method = method)
        if (connectResult.isFailure) {
            stateMachine.setError(connectFailureMessage(connectResult), diagnosticCode = "WS_CONNECT")
            return
        }
        stateMachine.setState(TransferState.WAITING_PEER)
        listenSignaling()
    }

    suspend fun initReceiver() {
        stateMachine.setRoomCode(roomCode)
        stateMachine.setState(TransferState.PREPARING)
        val connectResult = signaling.connect(role = "receiver", method = method)
        if (connectResult.isFailure) {
            stateMachine.setError(connectFailureMessage(connectResult), diagnosticCode = "WS_CONNECT")
            return
        }
        stateMachine.setState(TransferState.WAITING_PEER)
        listenSignaling()
    }

    fun destroy() {
        signaling.disconnect()
        dataChannel?.close()
        peerConnection?.close()
        dataChannel = null
        peerConnection = null
    }

    private fun listenSignaling() {
        scope.launch {
            signaling.events.collect { message ->
                when (message) {
                    is ServerMessage.PeerJoined -> {
                        stateMachine.setState(TransferState.CONNECTING)
                        if (role == "sender") {
                            setupPeerConnection()
                            createAndSendOffer()
                        }
                    }

                    is ServerMessage.Offer -> {
                        if (role == "receiver") {
                            if (peerConnection == null) {
                                setupPeerConnection()
                            }
                            handleRemoteOffer(message.sdp)
                        }
                    }

                    is ServerMessage.Answer -> {
                        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
                            override fun onSetSuccess() {
                                flushQueuedRemoteIceCandidates()
                            }
                        }, SessionDescription(SessionDescription.Type.ANSWER, message.sdp))
                    }

                    is ServerMessage.Ice -> handleRemoteIceCandidate(message.candidate)

                    is ServerMessage.PeerLeft -> {
                        if (!transferComplete && !transferFailed) {
                            stateMachine.setError("Peer disconnected before the transfer could complete.", "peer_left")
                            transferFailed = true
                        }
                    }

                    is ServerMessage.Error -> {
                        stateMachine.setError(message.message ?: defaultSignalingError(message.code), message.code)
                        transferFailed = true
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun setupPeerConnection() {
        val iceServers = if (method == TransferMethod.LOCAL) {
            emptyList()
        } else {
            stunServers.map { url -> PeerConnection.IceServer.builder(url).createIceServer() }
        }

        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = factory.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: RtcIceCandidate) {
                signaling.send(
                    ClientMessage.Ice(
                        IceCandidate(
                            candidate = candidate.sdp,
                            sdpMid = candidate.sdpMid,
                            sdpMLineIndex = candidate.sdpMLineIndex,
                        )
                    )
                )
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED,
                    PeerConnection.IceConnectionState.COMPLETED -> classifyConnectionKind()

                    PeerConnection.IceConnectionState.FAILED,
                    PeerConnection.IceConnectionState.DISCONNECTED -> {
                        if (!transferComplete && !transferFailed) {
                            transferFailed = true
                            stateMachine.setError("WebRTC connection failed.", "ice_failed")
                        }
                    }

                    else -> Unit
                }
            }

            override fun onDataChannel(channel: DataChannel) {
                if (role == "receiver") {
                    dataChannel = channel
                    attachReceiverDataChannelListener(channel)
                }
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) = Unit
            override fun onIceCandidatesRemoved(candidates: Array<out RtcIceCandidate>?) = Unit
            override fun onAddStream(stream: MediaStream?) = Unit
            override fun onRemoveStream(stream: MediaStream?) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
        })

        if (role == "sender") {
            val init = DataChannel.Init().apply { ordered = true }
            dataChannel = peerConnection?.createDataChannel(DATA_CHANNEL_LABEL, init)
            dataChannel?.let(::attachSenderDataChannelListener)
        }
    }

    private fun createAndSendOffer() {
        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                signaling.send(ClientMessage.Offer(sdp.description))
            }
        }, MediaConstraints())
    }

    private fun handleRemoteOffer(sdpString: String) {
        val remoteSdp = SessionDescription(SessionDescription.Type.OFFER, sdpString)
        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                flushQueuedRemoteIceCandidates()
                peerConnection?.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(sdp: SessionDescription) {
                        peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                        signaling.send(ClientMessage.Answer(sdp.description))
                    }
                }, MediaConstraints())
            }
        }, remoteSdp)
    }

    private fun handleRemoteIceCandidate(candidate: IceCandidate) {
        val pc = peerConnection ?: return
        if (pc.remoteDescription == null) {
            queuedRemoteIceCandidates += candidate
            return
        }

        pc.addIceCandidate(
            RtcIceCandidate(
                candidate.sdpMid,
                candidate.sdpMLineIndex ?: 0,
                candidate.candidate,
            )
        )
    }

    private fun flushQueuedRemoteIceCandidates() {
        val pc = peerConnection ?: return
        if (pc.remoteDescription == null || queuedRemoteIceCandidates.isEmpty()) return

        val pending = queuedRemoteIceCandidates.toList()
        queuedRemoteIceCandidates.clear()
        pending.forEach { candidate ->
            pc.addIceCandidate(
                RtcIceCandidate(
                    candidate.sdpMid,
                    candidate.sdpMLineIndex ?: 0,
                    candidate.candidate,
                )
            )
        }
    }

    private fun attachSenderDataChannelListener(channel: DataChannel) {
        channel.registerObserver(object : DataChannel.Observer {
            override fun onStateChange() {
                if (channel.state() == DataChannel.State.OPEN) {
                    stateMachine.setState(TransferState.TRANSFERRING)
                    scope.launch { sendAllFiles(channel) }
                }
            }

            override fun onMessage(buffer: DataChannel.Buffer) = Unit

            override fun onBufferedAmountChange(previousAmount: Long) = Unit
        })
    }

    private suspend fun sendAllFiles(channel: DataChannel) {
        val files = pendingFiles
        val totalBytes = files.sumOf { it.first.size.toLong() }
        var sentBytes = 0L

        for ((bytes, preview) in files) {
            stateMachine.setCurrentFile(preview)

            val startMessage = JSONObject()
                .put("type", "file-start")
                .put("fileId", preview.id)
                .put("name", preview.name)
                .put("mimeType", preview.mimeType)
                .put("totalChunks", (bytes.size + CHUNK_SIZE - 1) / CHUNK_SIZE)
                .put("totalSize", bytes.size)
                .toString()
            channel.send(DataChannel.Buffer(ByteBuffer.wrap(startMessage.toByteArray()), false))

            var offset = 0
            while (offset < bytes.size) {
                while (channel.bufferedAmount() > BUFFER_THRESHOLD) {
                    delay(10)
                }

                val end = minOf(offset + CHUNK_SIZE, bytes.size)
                val chunk = bytes.copyOfRange(offset, end)
                channel.send(DataChannel.Buffer(ByteBuffer.wrap(chunk), true))
                sentBytes += chunk.size.toLong()
                stateMachine.setProgress(sentBytes, totalBytes)
                offset = end
            }

            val endMessage = """{"type":"file-end","fileId":"${preview.id}"}"""
            channel.send(DataChannel.Buffer(ByteBuffer.wrap(endMessage.toByteArray()), false))
        }

        channel.send(DataChannel.Buffer(ByteBuffer.wrap("""{"type":"transfer-complete"}""".toByteArray()), false))
        transferComplete = true
        stateMachine.setCurrentFile(null)
        stateMachine.setState(TransferState.COMPLETE)
    }

    private fun attachReceiverDataChannelListener(channel: DataChannel) {
        stateMachine.setState(TransferState.TRANSFERRING)
        channel.registerObserver(object : DataChannel.Observer {
            override fun onStateChange() = Unit

            override fun onBufferedAmountChange(previousAmount: Long) = Unit

            override fun onMessage(buffer: DataChannel.Buffer) {
                val payload = buffer.data.readRemainingBytes()
                if (buffer.binary) {
                    assemblyBuffer.write(payload)
                    totalReceivedBytes += payload.size.toLong()
                    stateMachine.setProgress(totalReceivedBytes, totalExpectedBytes.coerceAtLeast(totalReceivedBytes))
                } else {
                    handleControlMessage(String(payload, Charsets.UTF_8))
                }
            }
        })
    }

    private fun handleControlMessage(text: String) {
        try {
            val obj = JSONObject(text)
            when (obj.getString("type")) {
                "file-start" -> {
                    assemblyBuffer.reset()
                    val meta = ReceivingFileMeta(
                        id = obj.getString("fileId"),
                        name = obj.getString("name"),
                        mimeType = obj.getString("mimeType"),
                        totalSize = obj.optLong("totalSize", 0L),
                    )
                    currentReceivingFile = meta
                    totalExpectedBytes += meta.totalSize
                    stateMachine.setCurrentFile(
                        TransferFilePreview(
                            id = meta.id,
                            name = meta.name,
                            mimeType = meta.mimeType,
                            size = meta.totalSize,
                        )
                    )
                }

                "file-end" -> {
                    val meta = currentReceivingFile ?: return
                    val bytes = assemblyBuffer.toByteArray()
                    stateMachine.addReceivedFile(
                        ReceivedFile(
                            id = meta.id,
                            name = meta.name,
                            mimeType = meta.mimeType,
                            size = bytes.size.toLong(),
                            bytes = bytes,
                        )
                    )
                    assemblyBuffer.reset()
                    currentReceivingFile = null
                    stateMachine.setCurrentFile(null)
                }

                "transfer-complete" -> {
                    transferComplete = true
                    stateMachine.setCurrentFile(null)
                    stateMachine.setState(TransferState.COMPLETE)
                }
            }
        } catch (_: Exception) {
            // Ignore malformed control payloads so a single bad message does not kill the session.
        }
    }

    private fun classifyConnectionKind() {
        peerConnection?.getStats { report ->
            var nextKind = ConnectionKind.UNKNOWN
            for (stats in report.statsMap.values) {
                if (stats.type != "candidate-pair" || stats.members["state"] != "succeeded") continue

                val localCandidateId = stats.members["localCandidateId"] as? String
                val remoteCandidateId = stats.members["remoteCandidateId"] as? String
                val localType = localCandidateId?.let { report.statsMap[it]?.members?.get("candidateType") as? String }
                val remoteType = remoteCandidateId?.let { report.statsMap[it]?.members?.get("candidateType") as? String }
                val types = listOfNotNull(localType, remoteType)

                nextKind = when {
                    types.any { it == "srflx" } -> ConnectionKind.INTERNET
                    types.isNotEmpty() -> ConnectionKind.LAN
                    else -> ConnectionKind.UNKNOWN
                }
                break
            }

            stateMachine.setConnectionKind(nextKind)
            if (method == TransferMethod.LOCAL && nextKind == ConnectionKind.INTERNET) {
                stateMachine.setError(
                    "Local mode requires both devices on the same Wi-Fi network. Use Direct mode instead.",
                    "LOCAL_MODE_INTERNET",
                )
                transferFailed = true
            }
        }
    }

    private fun connectFailureMessage(result: Result<Unit>): String {
        val detail = result.exceptionOrNull()?.message?.trim().orEmpty()
        return if (detail.isBlank()) {
            "Could not connect to the signaling server."
        } else {
            "Could not connect to the signaling server. $detail"
        }
    }

    private fun defaultSignalingError(code: String): String = when (code) {
        "ROOM_FULL" -> "This transfer code already has both peers connected."
        "NO_PEER" -> "The other device is not connected yet."
        "WS_ERROR" -> "The signaling connection failed."
        else -> "Signaling error: $code"
    }
}

private data class ReceivingFileMeta(
    val id: String,
    val name: String,
    val mimeType: String,
    val totalSize: Long,
)

private open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription) = Unit
    override fun onSetSuccess() = Unit
    override fun onCreateFailure(error: String?) = Unit
    override fun onSetFailure(error: String?) = Unit
}

private fun ByteBuffer.readRemainingBytes(): ByteArray {
    val duplicate = duplicate()
    val bytes = ByteArray(duplicate.remaining())
    duplicate.get(bytes)
    return bytes
}
