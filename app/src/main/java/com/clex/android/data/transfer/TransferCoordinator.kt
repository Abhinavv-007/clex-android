package com.clex.android.data.transfer

import android.content.Context
import com.clex.android.BuildConfig
import com.clex.android.data.ChainFileMeta
import com.clex.android.data.ChainIdentityStore
import com.clex.android.data.ClexChainApi
import com.clex.android.data.displayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.PeerConnectionFactory
import java.io.File
import java.util.UUID

private val SIGNALING_BASE_URL: String = BuildConfig.SIGNALING_BASE_URL
private const val SHARE_WAIT_TIMEOUT_MS = 60_000L

class TransferCoordinator(
    private val context: Context,
    private val stateMachine: TransferStateMachine,
    private val factory: PeerConnectionFactory,
) {
    private var activeTransfer: WebRtcTransfer? = null

    suspend fun startSender(
        roomCode: String,
        method: TransferMethod,
        files: List<TransferPayloadFile>,
    ) {
        destroy()
        val transfer = WebRtcTransfer(
            signalingBaseUrl = SIGNALING_BASE_URL,
            roomCode = roomCode,
            role = "sender",
            method = method,
            localChainId = ChainIdentityStore.getOrCreate(context),
            stateMachine = stateMachine,
            factory = factory,
            tempDir = File(context.cacheDir, "clex-transfer"),
        )
        transfer.prepareFiles(
            files.map {
                it.bytes to TransferFilePreview(
                    id = it.id,
                    name = it.name,
                    mimeType = it.mimeType,
                    size = it.bytes.size.toLong(),
                )
            }
        )
        activeTransfer = transfer
        transfer.initSender()
    }

    suspend fun startReceiver(roomCode: String, method: TransferMethod) {
        destroy()
        val transfer = WebRtcTransfer(
            signalingBaseUrl = SIGNALING_BASE_URL,
            roomCode = roomCode,
            role = "receiver",
            method = method,
            localChainId = ChainIdentityStore.getOrCreate(context),
            stateMachine = stateMachine,
            factory = factory,
            tempDir = File(context.cacheDir, "clex-transfer"),
        )
        activeTransfer = transfer
        transfer.initReceiver()
    }

    fun destroy() {
        activeTransfer?.destroy()
        activeTransfer = null
    }
}

private object PeerConnectionFactoryHolder {
    @Volatile
    private var factory: PeerConnectionFactory? = null

    fun get(context: Context): PeerConnectionFactory {
        val existing = factory
        if (existing != null) return existing

        return synchronized(this) {
            val cached = factory
            if (cached != null) {
                cached
            } else {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                        .createInitializationOptions()
                )
                PeerConnectionFactory.builder().createPeerConnectionFactory().also {
                    factory = it
                }
            }
        }
    }
}

/**
 * Tiny façade that lets the Application class warm the WebRTC native libs +
 * factory on a background thread during cold start. Calling `warm` outside
 * the workspace flow is safe because [PeerConnectionFactoryHolder.get]
 * already de-duplicates initialisation.
 */
object PeerConnectionFactoryWarmer {
    fun warm(context: Context) {
        PeerConnectionFactoryHolder.get(context.applicationContext)
    }
}

class WorkspaceSenderController(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val stateMachine = TransferStateMachine()
    private val coordinator = TransferCoordinator(
        context = context,
        stateMachine = stateMachine,
        factory = PeerConnectionFactoryHolder.get(context),
    )
    private val _files = MutableStateFlow<List<WorkspaceSelectedFile>>(emptyList())
    private var activeChainSessionId: String? = null
    private var lastObservedTransferState: TransferState = TransferState.IDLE
    private var lastAppendedChainStatus: String? = null
    private var lastReportedPeerChainId: String? = null

    val files: StateFlow<List<WorkspaceSelectedFile>> = _files.asStateFlow()
    val transferState: StateFlow<TransferUiState> = stateMachine.state

    init {
        scope.launch {
            transferState.collectLatest { state ->
                handleChainStateUpdate(state)
            }
        }
    }

    fun setMethod(method: TransferMethod) {
        stateMachine.setMethod(method)
    }

    fun setRoomCode(roomCode: String) {
        val normalizedCode = roomCode.trim().uppercase()
        if (isValidRoomCode(normalizedCode)) {
            stateMachine.setRoomCode(normalizedCode)
        }
    }

    fun addFiles(uris: List<android.net.Uri>) {
        scope.launch {
            val newEntries = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> uri.toWorkspaceFile(context) }
            }

            _files.update { current ->
                val existing = current.map { Triple(it.name, it.size, it.mimeType) }.toSet()
                current + newEntries.filter { Triple(it.name, it.size, it.mimeType) !in existing }
            }
        }
    }

    fun removeFile(id: String) {
        _files.update { current -> current.filterNot { it.id == id } }
        if (_files.value.isEmpty()) {
            resetTransfer()
        }
    }

    fun clearFiles() {
        _files.value = emptyList()
        resetTransfer()
    }

    fun startTransfer(roomCode: String = transferState.value.roomCode, overrideMethod: TransferMethod? = null) {
        scope.launch {
            val selectedFiles = _files.value
            val normalizedCode = roomCode.trim().uppercase()
            val method = overrideMethod ?: transferState.value.method
            if (selectedFiles.isEmpty()) {
                stateMachine.setError("Add at least one file before starting the transfer.", "no_files")
                return@launch
            }

            if (!isValidRoomCode(normalizedCode)) {
                stateMachine.setError("Generate a valid transfer code before starting the transfer.", "invalid_room_code")
                return@launch
            }

            stateMachine.setMethod(method)
            stateMachine.setRoomCode(normalizedCode)
            stateMachine.setState(TransferState.PREPARING)
            val payload = withContext(Dispatchers.IO) {
                selectedFiles.mapNotNull { file ->
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(file.uri)?.use { it.readBytes() }
                            ?: return@mapNotNull null
                        TransferPayloadFile(
                            id = file.id,
                            name = file.name,
                            mimeType = file.mimeType,
                            bytes = bytes,
                        )
                    }.getOrNull()
                }
            }

            if (payload.isEmpty()) {
                stateMachine.setError("The selected files could not be read on this device.", "read_failed")
                return@launch
            }

            prepareChainSession(method, payload)

            coordinator.startSender(
                roomCode = normalizedCode,
                method = method,
                files = payload,
            )
            if (stateMachine.state.value.state == TransferState.PREPARING ||
                stateMachine.state.value.state == TransferState.WAITING_PEER ||
                stateMachine.state.value.state == TransferState.CONNECTING
            ) {
                stateMachine.setShareExpiry(SHARE_WAIT_TIMEOUT_MS)
            }
        }
    }

    fun resetTransfer() {
        val method = transferState.value.method
        coordinator.destroy()
        stateMachine.reset()
        if (method != TransferMethod.WEBRTC) {
            stateMachine.setMethod(method)
        }
    }

    fun dispose() {
        coordinator.destroy()
        scope.cancel()
    }

    fun expirePendingTransfer() {
        coordinator.destroy()
        stateMachine.expirePendingShare(
            error = "This receive link expired after 1 minute. Start the transfer again to generate a new code.",
            diagnosticCode = "share_expired"
        )
    }

    private suspend fun prepareChainSession(method: TransferMethod, payload: List<TransferPayloadFile>) {
        val senderChainId = ChainIdentityStore.getOrCreate(context)
        ClexChainApi.registerChainId(senderChainId)

        val chainFiles = withContext(Dispatchers.Default) {
            payload.map { file ->
                ChainFileMeta(
                    category = ClexChainApi.fileCategory(file.mimeType),
                    type = file.mimeType,
                    size = file.bytes.size.toLong(),
                    hash = ClexChainApi.hashBytes(file.bytes),
                )
            }
        }

        val createdSession = ClexChainApi.createSession(
            senderChainId = senderChainId,
            route = method.webValue,
            files = chainFiles,
        )

        activeChainSessionId = createdSession?.sessionId
        lastAppendedChainStatus = null
        lastReportedPeerChainId = null

        createdSession?.sessionId?.let { sessionId ->
            if (ClexChainApi.appendEvent(sessionId, "waiting_peer")) {
                lastAppendedChainStatus = "waiting_peer"
            }
        }
    }

    private suspend fun handleChainStateUpdate(state: TransferUiState) {
        val previousState = lastObservedTransferState
        lastObservedTransferState = state.state

        val sessionId = activeChainSessionId
        if (sessionId == null) {
            return
        }

        val nextStatus = when (state.state) {
            TransferState.WAITING_PEER -> "waiting_peer"
            TransferState.CONNECTING -> "connecting"
            TransferState.TRANSFERRING -> "transferring"
            TransferState.COMPLETE -> "completed"
            TransferState.FAILED -> "failed"
            TransferState.IDLE -> if (previousState != TransferState.IDLE) "cancelled" else null
            TransferState.PREPARING -> null
        }

        if (nextStatus == null) {
            return
        }

        val peerChainId = state.peerChainId?.takeIf { it.matches(Regex("^[0-9a-f]{32}$")) }
        val shouldAppend = nextStatus != lastAppendedChainStatus ||
            (peerChainId != null && peerChainId != lastReportedPeerChainId)

        if (!shouldAppend) {
            return
        }

        if (ClexChainApi.appendEvent(sessionId, nextStatus, peerChainId)) {
            lastAppendedChainStatus = nextStatus
            if (peerChainId != null) {
                lastReportedPeerChainId = peerChainId
            }

            if (nextStatus in setOf("completed", "failed", "cancelled")) {
                activeChainSessionId = null
                lastAppendedChainStatus = null
                lastReportedPeerChainId = null
            }
        }
    }
}

class WorkspaceReceiverController(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val stateMachine = TransferStateMachine()
    private val coordinator = TransferCoordinator(
        context = context,
        stateMachine = stateMachine,
        factory = PeerConnectionFactoryHolder.get(context),
    )
    private val _saveMessage = MutableStateFlow<String?>(null)
    private var lastSavedTransferKey: String? = null

    val transferState: StateFlow<TransferUiState> = stateMachine.state
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    fun setMethod(method: TransferMethod) {
        stateMachine.setMethod(method)
    }

    init {
        scope.launch {
            transferState.collectLatest { state ->
                if (state.state != TransferState.COMPLETE || state.receivedFiles.isEmpty()) return@collectLatest

                val transferKey = state.receivedFiles.joinToString("|") { it.id }
                if (transferKey == lastSavedTransferKey) return@collectLatest

                lastSavedTransferKey = transferKey
                persistReceivedFiles(state.receivedFiles)
            }
        }
    }

    fun connect(roomCode: String) {
        val normalizedCode = roomCode.trim().uppercase()
        val method = transferState.value.method
        if (!isValidRoomCode(normalizedCode)) {
            stateMachine.setError("Enter a valid 6-character room code.", "invalid_code")
            return
        }

        cleanupReceivedFiles(transferState.value.receivedFiles)
        coordinator.destroy()
        _saveMessage.value = null
        lastSavedTransferKey = null
        stateMachine.reset(roomCode = normalizedCode)
        stateMachine.setMethod(method)

        scope.launch {
            coordinator.startReceiver(
                roomCode = normalizedCode,
                method = method,
            )
        }
    }

    fun reset() {
        val method = transferState.value.method
        cleanupReceivedFiles(transferState.value.receivedFiles)
        coordinator.destroy()
        stateMachine.reset()
        if (method != TransferMethod.WEBRTC) {
            stateMachine.setMethod(method)
        }
        _saveMessage.value = null
        lastSavedTransferKey = null
    }

    fun saveAgain() {
        val files = transferState.value.receivedFiles
        if (files.isEmpty()) return

        scope.launch {
            persistReceivedFiles(files)
        }
    }

    fun dispose() {
        cleanupReceivedFiles(transferState.value.receivedFiles)
        coordinator.destroy()
        scope.cancel()
    }

    private suspend fun persistReceivedFiles(files: List<ReceivedFile>) {
        _saveMessage.value = "Saving files to Downloads/Clex Received…"
        runCatching {
            withContext(Dispatchers.IO) {
                files.forEach { file ->
                    saveToDownloads(file.name, file.mimeType, File(file.tempFilePath))
                }
            }
        }.onSuccess {
            _saveMessage.value = "Saved ${files.size} file${if (files.size == 1) "" else "s"} to Downloads/Clex Received."
        }.onFailure { error ->
            _saveMessage.value = error.message ?: "Could not save the received files."
        }
    }

    private fun saveToDownloads(displayName: String, mimeType: String, sourceFile: File) {
        require(sourceFile.exists()) { "The received file is no longer available to save." }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    "${android.os.Environment.DIRECTORY_DOWNLOADS}/Clex Received",
                )
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create a Downloads entry.")

            runCatching {
                sourceFile.inputStream().use { input ->
                    resolver.openOutputStream(uri)?.use { output -> input.copyTo(output) }
                        ?: error("Could not open the Downloads output stream.")
                }
                val publish = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                }
                resolver.update(uri, publish, null, null)
            }.getOrElse { error ->
                resolver.delete(uri, null, null)
                throw error
            }
            return
        }

        val downloadsRoot = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads-fallback")
        val clexDir = File(downloadsRoot, "Clex Received").also { it.mkdirs() }
        sourceFile.inputStream().use { input ->
            File(clexDir, displayName).outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun cleanupReceivedFiles(files: List<ReceivedFile>) {
        files.forEach { file ->
            runCatching { File(file.tempFilePath).delete() }
        }
    }
}

private fun android.net.Uri.toWorkspaceFile(context: Context): WorkspaceSelectedFile? {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(this) ?: "application/octet-stream"
    val name = displayName(context) ?: lastPathSegment ?: "file"
    val size = contentResolver.openFileDescriptor(this, "r")?.use { descriptor ->
        descriptor.statSize
    } ?: 0L

    return WorkspaceSelectedFile(
        id = UUID.randomUUID().toString(),
        uri = this,
        name = name,
        size = size,
        mimeType = mimeType,
    )
}

