package com.clex.android.data.transfer

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class ClientMessage {
    data class Offer(val sdp: String) : ClientMessage()
    data class Answer(val sdp: String) : ClientMessage()
    data class Ice(val candidate: IceCandidate) : ClientMessage()
    data object Ping : ClientMessage()
}

data class IceCandidate(
    val candidate: String,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val usernameFragment: String? = null,
)

sealed class ServerMessage {
    data class Joined(val role: String, val mode: String) : ServerMessage()
    data class PeerJoined(val mode: String) : ServerMessage()
    data class Offer(val sdp: String) : ServerMessage()
    data class Answer(val sdp: String) : ServerMessage()
    data class Ice(val candidate: IceCandidate) : ServerMessage()
    data object PeerLeft : ServerMessage()
    data class Error(val code: String, val message: String? = null) : ServerMessage()
    data object Pong : ServerMessage()
    data class Unknown(val raw: String) : ServerMessage()
}

class SignalingClient(
    baseUrl: String,
    roomCode: String,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
) {
    private val roomUrl = "${baseUrl.trimEnd('/')}/room/${roomCode.trim().uppercase()}"
    private val eventChannel = Channel<ServerMessage>(Channel.UNLIMITED)
    val events: Flow<ServerMessage> = eventChannel.receiveAsFlow()

    private var webSocket: WebSocket? = null
    private var stopPing: (() -> Unit)? = null

    suspend fun connect(role: String, method: TransferMethod): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)
        val request = Request.Builder()
            .url("$roomUrl?role=$role&mode=${method.webValue}")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                startPing(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = parseServerMessage(text)
                if (message is ServerMessage.Joined) {
                    resultChannel.trySend(Result.success(Unit))
                }
                eventChannel.trySend(message)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                resultChannel.trySend(Result.failure(t))
                eventChannel.trySend(ServerMessage.Error("WS_ERROR", t.message))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                stopPing()
                webSocket.close(1000, null)
                eventChannel.trySend(ServerMessage.PeerLeft)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                stopPing()
                eventChannel.trySend(ServerMessage.PeerLeft)
            }
        }

        webSocket = okHttpClient.newWebSocket(request, listener)
        return resultChannel.receive()
    }

    fun send(message: ClientMessage) {
        webSocket?.send(encodeClientMessage(message))
    }

    fun disconnect() {
        stopPing()
        webSocket?.close(1000, "client disconnect")
        webSocket = null
    }

    private fun startPing(webSocket: WebSocket) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                webSocket.send("""{"type":"ping"}""")
                handler.postDelayed(this, 25_000L)
            }
        }
        stopPing = { handler.removeCallbacks(runnable) }
        handler.postDelayed(runnable, 25_000L)
    }

    private fun stopPing() {
        stopPing?.invoke()
        stopPing = null
    }

    private fun encodeClientMessage(message: ClientMessage): String = when (message) {
        is ClientMessage.Offer -> """{"type":"offer","sdp":${JSONObject.quote(message.sdp)}}"""
        is ClientMessage.Answer -> """{"type":"answer","sdp":${JSONObject.quote(message.sdp)}}"""
        is ClientMessage.Ice -> buildString {
            append("""{"type":"ice","candidate":{"candidate":""")
            append(JSONObject.quote(message.candidate.candidate))
            message.candidate.sdpMid?.let { append(""","sdpMid":${JSONObject.quote(it)}""") }
            message.candidate.sdpMLineIndex?.let { append(""","sdpMLineIndex":$it""") }
            message.candidate.usernameFragment?.let { append(""","usernameFragment":${JSONObject.quote(it)}""") }
            append("}}")
        }

        ClientMessage.Ping -> """{"type":"ping"}"""
    }

    private fun parseServerMessage(text: String): ServerMessage {
        return try {
            val obj = JSONObject(text)
            when (obj.getString("type")) {
                "joined" -> ServerMessage.Joined(
                    role = obj.optString("role"),
                    mode = obj.optString("mode"),
                )

                "peer_joined" -> ServerMessage.PeerJoined(mode = obj.optString("mode"))
                "offer" -> ServerMessage.Offer(sdp = obj.getString("sdp"))
                "answer" -> ServerMessage.Answer(sdp = obj.getString("sdp"))
                "ice" -> {
                    val candidate = obj.getJSONObject("candidate")
                    ServerMessage.Ice(
                        IceCandidate(
                            candidate = candidate.getString("candidate"),
                            sdpMid = candidate.optString("sdpMid").takeIf { it.isNotBlank() },
                            sdpMLineIndex = if (candidate.has("sdpMLineIndex")) candidate.getInt("sdpMLineIndex") else null,
                            usernameFragment = candidate.optString("usernameFragment").takeIf { it.isNotBlank() },
                        )
                    )
                }

                "peer_left" -> ServerMessage.PeerLeft
                "error" -> ServerMessage.Error(
                    code = obj.optString("code", "UNKNOWN"),
                    message = obj.optString("message").takeIf { it.isNotBlank() },
                )

                "pong" -> ServerMessage.Pong
                else -> ServerMessage.Unknown(text)
            }
        } catch (_: Exception) {
            ServerMessage.Unknown(text)
        }
    }
}
