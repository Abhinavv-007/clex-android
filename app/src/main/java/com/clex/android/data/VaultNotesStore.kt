package com.clex.android.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class VaultLocalNote(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val path: String,
    val tags: List<String> = emptyList(),
)

data class VaultSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val lastRemoteBackupAt: Long? = null,
    val error: String? = null,
    val backupEnabled: Boolean = true,
    val sameAccountVaultEnabled: Boolean = false,
    val accountEmail: String? = null,
    val keyFingerprint: String? = null,
    val roomId: String? = null,
    val keySource: VaultKeySource = VaultKeySource.LOCAL,
    val deviceCount: Int = 0,
)

private data class StoredEncryptedNote(
    val id: String,
    val titleBlob: VaultEncryptedBlob,
    val bodyBlob: VaultEncryptedBlob,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String> = emptyList(),
    val folderId: String? = null,
    val isPinned: Boolean = false,
    val attachmentIds: List<String> = emptyList(),
)

class VaultNotesStore private constructor(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notesDir = File(context.filesDir, "vault-notes").also { it.mkdirs() }
    private val driveAuthStore = DriveAuthStore.get(context)
    private val preferencesStore = VaultPreferencesStore.get(context)

    private val _notes = MutableStateFlow<List<VaultLocalNote>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _syncState = MutableStateFlow(VaultSyncStatus())
    private val _accountDevices = MutableStateFlow<List<VaultAccountDevice>>(emptyList())

    private var activeKey: VaultMasterKey? = null
    private var syncJob: Job? = null

    val notes: StateFlow<List<VaultLocalNote>> = _notes.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val syncState: StateFlow<VaultSyncStatus> = _syncState.asStateFlow()
    val accountDevices: StateFlow<List<VaultAccountDevice>> = _accountDevices.asStateFlow()

    init {
        scope.launch {
            combine(preferencesStore.settings, driveAuthStore.session) { prefs, session ->
                prefs to session
            }.collectLatest { (prefs, session) ->
                ensureKeyBound(prefs, session)
                loadInternal()
                syncNowInternal(forcePull = true)
            }
        }
    }

    fun load() {
        scope.launch { loadInternal() }
    }

    fun saveNote(noteId: String?, title: String, body: String, onSaved: (() -> Unit)? = null) {
        scope.launch {
            val key = ensureKeyBound()
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val normalizedTitle = title.trim().ifBlank { "Untitled note" }
                val targetId = noteId ?: "note-$now"
                val targetFile = File(notesDir, "$targetId.json")
                val existingRecord = readStoredRecord(targetFile, key)
                val createdAt = existingRecord?.createdAt ?: now

                val nextRecord = StoredEncryptedNote(
                    id = targetId,
                    titleBlob = VaultCryptoManager.encryptText(normalizedTitle, key),
                    bodyBlob = VaultCryptoManager.encryptText(body.trimEnd(), key),
                    createdAt = createdAt,
                    updatedAt = now,
                    tags = existingRecord?.tags.orEmpty(),
                    folderId = existingRecord?.folderId,
                    isPinned = existingRecord?.isPinned ?: false,
                    attachmentIds = existingRecord?.attachmentIds.orEmpty(),
                )

                writeStoredRecord(targetFile, nextRecord)
            }
            loadInternal()
            scheduleSync()
            onSaved?.invoke()
        }
    }

    fun deleteNote(note: VaultLocalNote, onDeleted: (() -> Unit)? = null) {
        scope.launch {
            withContext(Dispatchers.IO) {
                File(note.path).delete()
            }
            _notes.update { current -> current.filterNot { it.id == note.id } }
            scheduleSync()
            onDeleted?.invoke()
        }
    }

    fun clearAll(onComplete: (() -> Unit)? = null) {
        scope.launch {
            withContext(Dispatchers.IO) {
                notesDir.listFiles()?.forEach { file ->
                    if (file.extension == "json") file.delete()
                }
            }
            _notes.value = emptyList()
            scheduleSync()
            onComplete?.invoke()
        }
    }

    fun syncNow() {
        scope.launch { syncNowInternal(forcePull = false) }
    }

    fun restoreFromCloud() {
        scope.launch { syncNowInternal(forcePull = true) }
    }

    suspend fun exportNotesAsJson(): String = withContext(Dispatchers.IO) {
        val payload = JSONArray()
        _notes.value.forEach { note ->
            payload.put(
                JSONObject()
                    .put("id", note.id)
                    .put("title", note.title)
                    .put("body", note.body)
                    .put("createdAt", note.createdAt)
                    .put("updatedAt", note.updatedAt)
                    .put("tags", JSONArray(note.tags))
            )
        }
        payload.toString(2)
    }

    fun exportRecoveryKey(): String {
        val key = activeKey ?: ensureKeyBoundSync()
        return VaultCryptoManager.exportKeyAsJson(key)
    }

    fun rotateLocalKey(): Result<String> {
        val prefs = preferencesStore.settings.value
        if (prefs.sameAccountVaultEnabled) {
            return Result.failure(IllegalStateException("Disable Same-Account Vault before rotating the local recovery key."))
        }

        return runCatching {
            val current = ensureKeyBoundSync()
            val next = VaultCryptoManager.rotateLocalMasterKey(context)
            migrateRecordsBlocking(current, next)
            activeKey = next
            scope.launch {
                loadInternal()
                syncNowInternal(forcePull = false)
            }
            next.fingerprint
        }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun scheduleSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            delay(800)
            syncNowInternal(forcePull = false)
        }
    }

    private suspend fun ensureKeyBound(
        prefs: VaultPreferences = preferencesStore.settings.value,
        session: DriveSession? = driveAuthStore.session.value,
    ): VaultMasterKey {
        val desired = if (prefs.sameAccountVaultEnabled && !session?.user?.sub.isNullOrBlank()) {
            VaultCryptoManager.deriveSameAccountKey(session!!.user.sub)
        } else {
            VaultCryptoManager.getOrCreateLocalMasterKey(context)
        }

        val current = activeKey
        if (current == null) {
            activeKey = desired
            _syncState.update {
                it.copy(
                    backupEnabled = prefs.cloudBackupEnabled,
                    sameAccountVaultEnabled = prefs.sameAccountVaultEnabled,
                    accountEmail = session?.user?.email ?: session?.user?.displayName,
                    keyFingerprint = desired.fingerprint,
                    roomId = desired.roomId,
                    keySource = desired.source,
                )
            }
            return desired
        }

        if (current.fingerprint != desired.fingerprint) {
            migrateRecords(current, desired)
        }

        activeKey = desired
        _syncState.update {
            it.copy(
                backupEnabled = prefs.cloudBackupEnabled,
                sameAccountVaultEnabled = prefs.sameAccountVaultEnabled,
                accountEmail = session?.user?.email ?: session?.user?.displayName,
                keyFingerprint = desired.fingerprint,
                roomId = desired.roomId,
                keySource = desired.source,
            )
        }
        return desired
    }

    private fun ensureKeyBoundSync(): VaultMasterKey {
        return activeKey ?: if (
            preferencesStore.settings.value.sameAccountVaultEnabled &&
            !driveAuthStore.session.value?.user?.sub.isNullOrBlank()
        ) {
            VaultCryptoManager.deriveSameAccountKey(driveAuthStore.session.value!!.user.sub)
        } else {
            VaultCryptoManager.getOrCreateLocalMasterKey(context)
        }.also { activeKey = it }
    }

    private suspend fun loadInternal() {
        val key = activeKey ?: return
        _isLoading.value = true
        val nextNotes = withContext(Dispatchers.IO) {
            notesDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.mapNotNull { file ->
                    val record = readStoredRecord(file, key) ?: return@mapNotNull null
                    runCatching {
                        VaultLocalNote(
                            id = record.id,
                            title = VaultCryptoManager.decryptText(record.titleBlob, key),
                            body = VaultCryptoManager.decryptText(record.bodyBlob, key),
                            createdAt = record.createdAt,
                            updatedAt = record.updatedAt,
                            path = file.absolutePath,
                            tags = record.tags,
                        )
                    }.getOrNull()
                }
                ?.sortedByDescending { it.updatedAt }
                ?: emptyList()
        }
        _notes.value = nextNotes
        _isLoading.value = false
    }

    private suspend fun syncNowInternal(forcePull: Boolean) {
        val prefs = preferencesStore.settings.value
        val session = driveAuthStore.session.value
        val key = ensureKeyBound(prefs, session)

        _syncState.update {
            it.copy(
                isSyncing = true,
                error = null,
                backupEnabled = prefs.cloudBackupEnabled,
                sameAccountVaultEnabled = prefs.sameAccountVaultEnabled,
                accountEmail = session?.user?.email ?: session?.user?.displayName,
                keyFingerprint = key.fingerprint,
                roomId = key.roomId,
                keySource = key.source,
            )
        }

        if (session == null) {
            _accountDevices.value = emptyList()
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    error = if (prefs.sameAccountVaultEnabled || prefs.cloudBackupEnabled) {
                        "Sign in to Google Drive to sync Vault across devices."
                    } else null,
                    deviceCount = 0,
                )
            }
            return
        }

        runCatching {
            val uid = session.user.sub
            val localRecords = readAllStoredRecords(key)
            val remoteEnvelope = if (prefs.cloudBackupEnabled) {
                ClexVaultApi.fetchBackup(key.roomId)
            } else {
                null
            }

            val mergedRecords = if (remoteEnvelope != null && remoteEnvelope.snapshot.ciphertextB64.isNotBlank()) {
                val remoteSnapshot = decryptBackupSnapshot(remoteEnvelope, key)
                val merged = mergeNotes(localRecords, remoteSnapshot.notes)
                if (forcePull || merged.size != localRecords.size || merged.any { mergedNote ->
                        val local = localRecords.firstOrNull { it.id == mergedNote.id }
                        local == null || local.updatedAt != mergedNote.updatedAt
                    }
                ) {
                    writeAllStoredRecords(merged)
                }
                merged
            } else {
                localRecords
            }

            if (prefs.cloudBackupEnabled) {
                pushBackupSnapshot(key, mergedRecords)
            }

            val device = VaultAccountDevice(
                id = VaultCryptoManager.getDeviceFingerprint(context),
                name = VaultCryptoManager.detectDeviceName(),
                lastSeen = System.currentTimeMillis(),
                pairedAt = System.currentTimeMillis(),
                roomId = key.roomId,
                fingerprint = key.fingerprint,
            )
            ClexVaultApi.upsertAccountDevice(uid, device)
            val devices = ClexVaultApi.fetchAccountDevices(uid)
            _accountDevices.value = devices
            loadInternal()

            remoteEnvelope?.updatedAt
        }.onSuccess { remoteUpdatedAt ->
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncedAt = System.currentTimeMillis(),
                    lastRemoteBackupAt = remoteUpdatedAt ?: it.lastRemoteBackupAt,
                    error = null,
                    deviceCount = _accountDevices.value.size,
                )
            }
        }.onFailure { error ->
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    error = error.message ?: "Vault sync failed.",
                    deviceCount = _accountDevices.value.size,
                )
            }
        }
    }

    private suspend fun pushBackupSnapshot(
        key: VaultMasterKey,
        records: List<StoredEncryptedNote>,
    ) {
        val snapshot = JSONObject()
            .put("schemaVersion", 1)
            .put("savedAt", System.currentTimeMillis())
            .put(
                "notes",
                JSONArray().apply {
                    records.forEach { note ->
                        put(
                            JSONObject()
                                .put("id", note.id)
                                .put(
                                    "titleBlob",
                                    JSONObject()
                                        .put("ciphertextB64", note.titleBlob.ciphertextB64)
                                        .put("ivB64", note.titleBlob.ivB64)
                                )
                                .put(
                                    "bodyBlob",
                                    JSONObject()
                                        .put("ciphertextB64", note.bodyBlob.ciphertextB64)
                                        .put("ivB64", note.bodyBlob.ivB64)
                                )
                                .put("createdAt", note.createdAt)
                                .put("updatedAt", note.updatedAt)
                                .put("tags", JSONArray(note.tags))
                                .put("folderId", note.folderId)
                                .put("isPinned", note.isPinned)
                                .put("attachmentIds", JSONArray(note.attachmentIds))
                        )
                    }
                }
            )
            .put("folders", JSONArray())

        val encryptedSnapshot = VaultCryptoManager.encryptText(snapshot.toString(), key)
        ClexVaultApi.pushBackup(
            roomId = key.roomId,
            fingerprint = key.fingerprint,
            snapshot = encryptedSnapshot,
            noteCount = records.size,
            folderCount = 0,
            updatedAt = System.currentTimeMillis(),
        )
    }

    private fun decryptBackupSnapshot(
        envelope: VaultBackupEnvelope,
        key: VaultMasterKey,
    ): VaultBackupSnapshot {
        val decrypted = VaultCryptoManager.decryptText(envelope.snapshot, key)
        val json = JSONObject(decrypted)
        val notesArray = json.optJSONArray("notes") ?: JSONArray()
        val folderArray = json.optJSONArray("folders") ?: JSONArray()

        val notes = buildList {
            for (index in 0 until notesArray.length()) {
                val item = notesArray.optJSONObject(index) ?: continue
                add(item.toStoredEncryptedNote())
            }
        }
        val folders = buildList {
            for (index in 0 until folderArray.length()) {
                val item = folderArray.optJSONObject(index) ?: continue
                add(
                    VaultBackupFolder(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        parentId = item.optString("parentId").takeIf { it.isNotBlank() },
                        createdAt = item.optLong("createdAt"),
                        sortOrder = item.optInt("sortOrder"),
                    )
                )
            }
        }

        return VaultBackupSnapshot(
            schemaVersion = json.optInt("schemaVersion", 1),
            savedAt = json.optLong("savedAt", envelope.updatedAt),
            notes = notes.map { record ->
                VaultBackupNote(
                    id = record.id,
                    titleBlob = record.titleBlob,
                    bodyBlob = record.bodyBlob,
                    createdAt = record.createdAt,
                    updatedAt = record.updatedAt,
                    tags = record.tags,
                    folderId = record.folderId,
                    isPinned = record.isPinned,
                    attachmentIds = record.attachmentIds,
                )
            },
            folders = folders,
        )
    }

    private suspend fun migrateRecords(fromKey: VaultMasterKey, toKey: VaultMasterKey) {
        withContext(Dispatchers.IO) {
            migrateRecordsBlocking(fromKey, toKey)
        }
    }

    private fun migrateRecordsBlocking(fromKey: VaultMasterKey, toKey: VaultMasterKey) {
            val files = notesDir.listFiles()?.filter { it.extension == "json" }.orEmpty()
            files.forEach { file ->
                val currentRecord = readStoredRecord(file, fromKey) ?: return@forEach
                val title = VaultCryptoManager.decryptText(currentRecord.titleBlob, fromKey)
                val body = VaultCryptoManager.decryptText(currentRecord.bodyBlob, fromKey)

                val migrated = currentRecord.copy(
                    titleBlob = VaultCryptoManager.encryptText(title, toKey),
                    bodyBlob = VaultCryptoManager.encryptText(body, toKey),
                )
                writeStoredRecord(file, migrated)
            }
    }

    private suspend fun readAllStoredRecords(key: VaultMasterKey): List<StoredEncryptedNote> = withContext(Dispatchers.IO) {
        notesDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file -> readStoredRecord(file, key) }
            ?.sortedByDescending { it.updatedAt }
            ?: emptyList()
    }

    private suspend fun writeAllStoredRecords(records: List<StoredEncryptedNote>) = withContext(Dispatchers.IO) {
        notesDir.listFiles()?.forEach { file ->
            if (file.extension == "json") file.delete()
        }
        records.forEach { record ->
            writeStoredRecord(File(notesDir, "${record.id}.json"), record)
        }
    }

    private fun mergeNotes(
        local: List<StoredEncryptedNote>,
        remote: List<VaultBackupNote>,
    ): List<StoredEncryptedNote> {
        val merged = LinkedHashMap<String, StoredEncryptedNote>()

        local.forEach { record ->
            merged[record.id] = record
        }

        remote.forEach { note ->
            val next = StoredEncryptedNote(
                id = note.id,
                titleBlob = note.titleBlob,
                bodyBlob = note.bodyBlob,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                tags = note.tags,
                folderId = note.folderId,
                isPinned = note.isPinned,
                attachmentIds = note.attachmentIds,
            )
            val current = merged[note.id]
            if (current == null || note.updatedAt >= current.updatedAt) {
                merged[note.id] = next
            }
        }

        return merged.values.sortedByDescending { it.updatedAt }
    }

    private fun readStoredRecord(file: File, keyForLegacy: VaultMasterKey): StoredEncryptedNote? {
        if (!file.exists()) return null
        return runCatching {
            val payload = JSONObject(file.readText())
            if (payload.has("titleBlob") && payload.has("bodyBlob")) {
                payload.toStoredEncryptedNote()
            } else {
                migrateLegacyRecord(file, payload, keyForLegacy)
            }
        }.getOrNull()
    }

    private fun migrateLegacyRecord(
        file: File,
        payload: JSONObject,
        key: VaultMasterKey,
    ): StoredEncryptedNote {
        val createdAt = payload.optLong("createdAt").takeIf { it > 0L } ?: System.currentTimeMillis()
        val updatedAt = payload.optLong("updatedAt").takeIf { it > 0L } ?: createdAt
        val record = StoredEncryptedNote(
            id = payload.optString("id").ifBlank { file.nameWithoutExtension },
            titleBlob = VaultCryptoManager.encryptText(payload.optString("title", "Untitled note"), key),
            bodyBlob = VaultCryptoManager.encryptText(payload.optString("body", ""), key),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
        writeStoredRecord(file, record)
        return record
    }

    private fun writeStoredRecord(file: File, record: StoredEncryptedNote) {
        val payload = JSONObject()
            .put("id", record.id)
            .put(
                "titleBlob",
                JSONObject()
                    .put("ciphertextB64", record.titleBlob.ciphertextB64)
                    .put("ivB64", record.titleBlob.ivB64)
            )
            .put(
                "bodyBlob",
                JSONObject()
                    .put("ciphertextB64", record.bodyBlob.ciphertextB64)
                    .put("ivB64", record.bodyBlob.ivB64)
            )
            .put("createdAt", record.createdAt)
            .put("updatedAt", record.updatedAt)
            .put("tags", JSONArray(record.tags))
            .put("folderId", record.folderId)
            .put("isPinned", record.isPinned)
            .put("attachmentIds", JSONArray(record.attachmentIds))

        file.writeText(payload.toString())
    }

    private fun JSONObject.toStoredEncryptedNote(): StoredEncryptedNote {
        return StoredEncryptedNote(
            id = optString("id"),
            titleBlob = optJSONObject("titleBlob")!!.toEncryptedBlob(),
            bodyBlob = optJSONObject("bodyBlob")!!.toEncryptedBlob(),
            createdAt = optLong("createdAt"),
            updatedAt = optLong("updatedAt"),
            tags = optJSONArray("tags").toStringList(),
            folderId = optString("folderId").takeIf { it.isNotBlank() },
            isPinned = optBoolean("isPinned", false),
            attachmentIds = optJSONArray("attachmentIds").toStringList(),
        )
    }

    private fun JSONObject.toEncryptedBlob(): VaultEncryptedBlob {
        return VaultEncryptedBlob(
            ciphertextB64 = optString("ciphertextB64"),
            ivB64 = optString("ivB64"),
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val value = optString(index)
                if (value.isNotBlank()) add(value)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: VaultNotesStore? = null

        fun get(context: Context): VaultNotesStore {
            val existing = instance
            if (existing != null) return existing

            return synchronized(this) {
                instance ?: VaultNotesStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
