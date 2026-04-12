package com.clex.android.data

import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object ClexBackendConfig {
    const val apiBaseUrl = "https://clex.in"
    const val chainApiBaseUrl = "https://clex.in"
    const val vaultApiBaseUrl = "https://clex.in/vault/api"
    const val vaultSecretRevealBaseUrl = "https://clex.in/vault/secret"
    const val driveAndroidReturnUrl = "https://clex.in/oauth/android"
}

data class ChainStats(
    val totalSessions: Int,
    val totalChains: Int,
    val completedSessions: Int
)

data class ChainSession(
    val id: String,
    val route: String,
    val status: String,
    val durationMs: Long?,
    val recordHash: String,
    val startedAt: Long,
    val fileCount: Int
)

data class ChainFileMeta(
    val category: String,
    val type: String,
    val size: Long,
    val hash: String?,
)

data class CreatedChainSession(
    val sessionId: String,
    val ledgerIndex: Int,
)

data class ChainEvent(
    val id: Int,
    val status: String,
    val timestamp: Long,
)

data class ChainSessionDetail(
    val id: String,
    val senderChainId: String,
    val receiverChainId: String?,
    val route: String,
    val status: String,
    val durationMs: Long?,
    val startedAt: Long,
    val completedAt: Long?,
    val ledgerIndex: Int,
    val previousHash: String?,
    val recordHash: String,
    val files: JSONArray,
    val events: List<ChainEvent>,
)

data class ChainFeed(
    val stats: ChainStats,
    val sessions: List<ChainSession>
)

data class SecretPolicy(
    val viewOnce: Boolean = false,
    val timedView: Boolean = false,
    val noSelect: Boolean = false,
    val tabSwitchLock: Boolean = false,
    val devtoolsGuard: Boolean = false,
    val screenshotGuard: Boolean = false,
    val memoryOnly: Boolean = true,
    val viewWindowSeconds: Int = 0
)

data class CreatedSecret(
    val id: String,
    val expiresAt: Long,
    val policy: SecretPolicy,
    val linkUrl: String,
    val accessCode: String
)

data class SecretStatus(
    val exists: Boolean,
    val alreadyOpened: Boolean,
    val openedAt: Long?,
    val expiresAt: Long?,
    val policy: SecretPolicy
)

data class RevealedSecret(
    val encryptedPayload: String,
    val iv: String,
    val createdAt: Long,
    val expiresAt: Long,
    val policy: SecretPolicy,
)

data class VaultBackupNote(
    val id: String,
    val titleBlob: VaultEncryptedBlob,
    val bodyBlob: VaultEncryptedBlob,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String>,
    val folderId: String?,
    val isPinned: Boolean,
    val attachmentIds: List<String>,
)

data class VaultBackupFolder(
    val id: String,
    val name: String,
    val parentId: String?,
    val createdAt: Long,
    val sortOrder: Int,
)

data class VaultBackupSnapshot(
    val schemaVersion: Int,
    val savedAt: Long,
    val notes: List<VaultBackupNote>,
    val folders: List<VaultBackupFolder>,
)

data class VaultBackupEnvelope(
    val roomId: String,
    val fingerprint: String,
    val noteCount: Int,
    val folderCount: Int,
    val updatedAt: Long,
    val snapshot: VaultEncryptedBlob,
)

data class VaultAccountDevice(
    val id: String,
    val name: String,
    val lastSeen: Long,
    val pairedAt: Long,
    val roomId: String,
    val fingerprint: String,
)

data class DriveUploadItem(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
)

data class DriveUploadResult(
    val folderId: String,
    val folderName: String,
    val webViewLink: String,
)

private data class EncryptedSecretPayload(
    val encryptedPayload: String,
    val iv: String,
    val keyB64: String
)

object ClexChainApi {
    suspend fun registerChainId(chainId: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().put("chain_id", chainId)
            httpPostJson("${ClexBackendConfig.chainApiBaseUrl}/chain/register", payload.toString())
        }.isSuccess
    }

    suspend fun createSession(
        senderChainId: String,
        route: String,
        files: List<ChainFileMeta>,
    ): CreatedChainSession? = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject()
                .put("sender_chain_id", senderChainId)
                .put("route", route)
                .put(
                    "files",
                    JSONArray().apply {
                        files.forEach { file ->
                            put(
                                JSONObject()
                                    .put("category", file.category)
                                    .put("type", file.type)
                                    .put("size", file.size)
                                    .put("hash", file.hash)
                            )
                        }
                    }
                )

            val response = httpPostJson("${ClexBackendConfig.chainApiBaseUrl}/chain/session", payload.toString())
            CreatedChainSession(
                sessionId = response.optString("session_id"),
                ledgerIndex = response.optInt("ledger_index"),
            ).takeIf { it.sessionId.isNotBlank() }
        }.getOrNull()
    }

    suspend fun appendEvent(
        sessionId: String,
        status: String,
        receiverChainId: String? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().put("status", status).apply {
                if (!receiverChainId.isNullOrBlank()) {
                    put("receiver_chain_id", receiverChainId)
                }
            }
            httpPostJson(
                "${ClexBackendConfig.chainApiBaseUrl}/chain/session/${Uri.encode(sessionId)}/event",
                payload.toString(),
            )
        }.isSuccess
    }

    fun hashBytes(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(bytes)
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun fileCategory(mimeType: String): String = when {
        mimeType.startsWith("image/") -> "image"
        mimeType == "application/pdf" -> "pdf"
        mimeType.startsWith("video/") -> "video"
        mimeType.startsWith("audio/") -> "audio"
        mimeType.contains("zip") || mimeType.contains("archive") || mimeType.contains("tar") || mimeType.contains("gzip") -> "archive"
        mimeType.contains("word") || mimeType.contains("document") || mimeType.contains("spreadsheet") || mimeType.contains("presentation") || mimeType.startsWith("text/") -> "document"
        else -> "other"
    }

    suspend fun fetchFeed(limit: Int = 5): ChainFeed = withContext(Dispatchers.IO) {
        val statsJson = httpGetJson("${ClexBackendConfig.chainApiBaseUrl}/chain/stats")
        val explorerJson = httpGetJson(
            "${ClexBackendConfig.chainApiBaseUrl}/chain/explorer?page=1&limit=$limit"
        )

        val stats = ChainStats(
            totalSessions = statsJson.optInt("total_sessions", 0),
            totalChains = statsJson.optInt("total_chains", 0),
            completedSessions = statsJson.optInt("completed_sessions", 0)
        )

        val sessions = explorerJson.optJSONArray("sessions")
            ?.toObjectList { item ->
                val files = item.optJSONArray("files")
                ChainSession(
                    id = item.optString("id"),
                    route = item.optString("route"),
                    status = item.optString("status"),
                    durationMs = item.optLongOrNull("duration_ms"),
                    recordHash = item.optString("record_hash"),
                    startedAt = item.optLong("started_at"),
                    fileCount = files?.length() ?: 0
                )
            }
            .orEmpty()

        ChainFeed(stats = stats, sessions = sessions)
    }

    suspend fun fetchSessionDetail(sessionId: String): ChainSessionDetail = withContext(Dispatchers.IO) {
        val payload = httpGetJson("${ClexBackendConfig.chainApiBaseUrl}/chain/session/${Uri.encode(sessionId)}")
        ChainSessionDetail(
            id = payload.optString("id"),
            senderChainId = payload.optString("sender_chain_id"),
            receiverChainId = payload.optString("receiver_chain_id").takeIf { it.isNotBlank() },
            route = payload.optString("route"),
            status = payload.optString("status"),
            durationMs = payload.optLongOrNull("duration_ms"),
            startedAt = payload.optLong("started_at"),
            completedAt = payload.optLongOrNull("completed_at"),
            ledgerIndex = payload.optInt("ledger_index"),
            previousHash = payload.optString("previous_hash").takeIf { it.isNotBlank() },
            recordHash = payload.optString("record_hash"),
            files = payload.optJSONArray("files") ?: JSONArray(),
            events = payload.optJSONArray("events")
                ?.let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            val item = array.optJSONObject(index) ?: continue
                            add(
                                ChainEvent(
                                    id = item.optInt("id"),
                                    status = item.optString("status"),
                                    timestamp = item.optLong("ts"),
                                )
                            )
                        }
                    }
                }
                .orEmpty(),
        )
    }
}

object ClexVaultApi {
    suspend fun createSecret(
        plaintext: String,
        ttlSeconds: Int,
        policy: SecretPolicy
    ): CreatedSecret = withContext(Dispatchers.IO) {
        val encrypted = encryptSecret(plaintext)
        val payload = JSONObject()
            .put("encryptedPayload", encrypted.encryptedPayload)
            .put("iv", encrypted.iv)
            .put("ttlSeconds", ttlSeconds)
            .put("policy", policy.toJson())

        val response = httpPostJson("${ClexBackendConfig.vaultApiBaseUrl}/secret", payload.toString())
        val id = response.optString("id")
        if (id.isBlank()) {
            throw IOException("Vault returned an invalid secret id.")
        }

        val appliedPolicy = response.optJSONObject("policy")?.toSecretPolicy() ?: policy
        CreatedSecret(
            id = id,
            expiresAt = response.optLong("expiresAt", System.currentTimeMillis() + ttlSeconds * 1000L),
            policy = appliedPolicy,
            linkUrl = buildSecretLink(id, encrypted.keyB64),
            accessCode = buildSecretAccessCode(id, encrypted.keyB64)
        )
    }

    suspend fun fetchSecretStatus(id: String): SecretStatus = withContext(Dispatchers.IO) {
        val response = httpGetJson("${ClexBackendConfig.vaultApiBaseUrl}/secret/${id.trim()}/status")
        SecretStatus(
            exists = response.optBoolean("exists", false),
            alreadyOpened = response.optBoolean("alreadyOpened", false),
            openedAt = response.optLongOrNull("openedAt"),
            expiresAt = response.optLongOrNull("expiresAt"),
            policy = response.optJSONObject("policy")?.toSecretPolicy() ?: SecretPolicy()
        )
    }

    suspend fun revealSecret(id: String): RevealedSecret = withContext(Dispatchers.IO) {
        val response = httpGetJson("${ClexBackendConfig.vaultApiBaseUrl}/secret/${id.trim()}")
        RevealedSecret(
            encryptedPayload = response.optString("encryptedPayload"),
            iv = response.optString("iv"),
            createdAt = response.optLong("createdAt"),
            expiresAt = response.optLong("expiresAt"),
            policy = response.optJSONObject("policy")?.toSecretPolicy() ?: SecretPolicy(),
        )
    }

    suspend fun pushBackup(
        roomId: String,
        fingerprint: String,
        snapshot: VaultEncryptedBlob,
        noteCount: Int,
        folderCount: Int,
        updatedAt: Long,
    ) = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("fingerprint", fingerprint)
            .put("noteCount", noteCount)
            .put("folderCount", folderCount)
            .put("updatedAt", updatedAt)
            .put(
                "snapshot",
                JSONObject()
                    .put("ciphertextB64", snapshot.ciphertextB64)
                    .put("ivB64", snapshot.ivB64)
            )

        httpJsonRequest(
            url = "${ClexBackendConfig.vaultApiBaseUrl}/backup/${Uri.encode(roomId)}",
            method = "PUT",
            body = payload.toString(),
        )
    }

    suspend fun fetchBackup(roomId: String): VaultBackupEnvelope? = withContext(Dispatchers.IO) {
        val payload = httpJsonRequest(
            url = "${ClexBackendConfig.vaultApiBaseUrl}/backup/${Uri.encode(roomId)}",
            method = "GET",
            allowNotFound = true,
        ) ?: return@withContext null

        VaultBackupEnvelope(
            roomId = payload.optString("roomId", roomId),
            fingerprint = payload.optString("fingerprint"),
            noteCount = payload.optInt("noteCount", 0),
            folderCount = payload.optInt("folderCount", 0),
            updatedAt = payload.optLong("updatedAt"),
            snapshot = VaultEncryptedBlob(
                ciphertextB64 = payload.optJSONObject("snapshot")?.optString("ciphertextB64").orEmpty(),
                ivB64 = payload.optJSONObject("snapshot")?.optString("ivB64").orEmpty(),
            ),
        )
    }

    suspend fun upsertAccountDevice(uid: String, device: VaultAccountDevice) = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("id", device.id)
            .put("name", device.name)
            .put("lastSeen", device.lastSeen)
            .put("pairedAt", device.pairedAt)
            .put("roomId", device.roomId)
            .put("fingerprint", device.fingerprint)

        httpJsonRequest(
            url = "${ClexBackendConfig.vaultApiBaseUrl}/devices",
            method = "POST",
            body = payload.toString(),
            headers = mapOf("X-Vault-UID" to uid),
        )
    }

    suspend fun fetchAccountDevices(uid: String): List<VaultAccountDevice> = withContext(Dispatchers.IO) {
        val payload = httpJsonRequest(
            url = "${ClexBackendConfig.vaultApiBaseUrl}/devices",
            method = "GET",
            headers = mapOf("X-Vault-UID" to uid),
        )
        payload?.optJSONArray("devices")
            ?.let { devices ->
                buildList {
                    for (index in 0 until devices.length()) {
                        val item = devices.optJSONObject(index) ?: continue
                        add(
                            VaultAccountDevice(
                                id = item.optString("id"),
                                name = item.optString("name"),
                                lastSeen = item.optLong("lastSeen"),
                                pairedAt = item.optLong("pairedAt"),
                                roomId = item.optString("roomId"),
                                fingerprint = item.optString("fingerprint"),
                            )
                        )
                    }
                }
            }
            .orEmpty()
    }

    suspend fun removeAccountDevice(uid: String, deviceId: String) = withContext(Dispatchers.IO) {
        httpJsonRequest(
            url = "${ClexBackendConfig.vaultApiBaseUrl}/devices/${Uri.encode(deviceId)}",
            method = "DELETE",
            headers = mapOf("X-Vault-UID" to uid),
        )
    }
}

object ClexDriveApi {
    private val http = OkHttpClient()
    private const val DRIVE_ROOT_FOLDER_NAME = "Clex Uploads"
    private const val DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files"
    private const val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"

    fun buildAndroidGoogleAuthUrl(): String {
        val encodedReturnTo = URLEncoder.encode(
            ClexBackendConfig.driveAndroidReturnUrl,
            StandardCharsets.UTF_8.name(),
        )
        return "${ClexBackendConfig.apiBaseUrl}/api/auth/google?return_to=$encodedReturnTo"
    }

    suspend fun pickupDriveToken(sessionId: String): DriveSession? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ClexBackendConfig.apiBaseUrl}/api/auth/gdrive/token?session_id=${Uri.encode(sessionId)}")
            .get()
            .build()

        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return@withContext null

            val payload = JSONObject(body)
            val token = payload.optString("token").takeIf { it.isNotBlank() } ?: return@withContext null
            val userJson = payload.optJSONObject("user") ?: return@withContext null
            val sub = userJson.optString("sub").takeIf { it.isNotBlank() } ?: return@withContext null

            DriveSession(
                token = token,
                user = DriveUserPayload(
                    sub = sub,
                    email = userJson.optString("email").takeIf { it.isNotBlank() },
                    displayName = userJson.optString("displayName").takeIf { it.isNotBlank() },
                    picture = userJson.optString("picture").takeIf { it.isNotBlank() },
                ),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    suspend fun uploadToDrive(
        items: List<DriveUploadItem>,
        accessToken: String,
        onProgress: (Int) -> Unit = {},
    ): DriveUploadResult = withContext(Dispatchers.IO) {
        require(items.isNotEmpty()) { "No files selected for Google Drive upload." }

        onProgress(6)
        val rootFolderId = ensureDriveRootFolder(accessToken)
        onProgress(18)

        val folderName = driveSessionFolderName()
        val folderId = createDriveFolder(folderName, accessToken, rootFolderId)
        val folderLink = "https://drive.google.com/drive/folders/$folderId"
        onProgress(28)

        items.forEachIndexed { index, item ->
            val fileId = uploadFileResumable(item, accessToken, folderId)
            setAnyoneWithLinkPermission(fileId, accessToken)
            val progress = 28 + (((index + 1).toDouble() / items.size.toDouble()) * 58.0).roundToInt()
            onProgress(progress.coerceAtMost(88))
        }

        if (items.size > 1) {
            setAnyoneWithLinkPermission(folderId, accessToken)
        }

        onProgress(100)
        DriveUploadResult(
            folderId = folderId,
            folderName = folderName,
            webViewLink = folderLink,
        )
    }

    private fun ensureDriveRootFolder(accessToken: String): String {
        return findDriveFolderIdByName(DRIVE_ROOT_FOLDER_NAME, accessToken)
            ?: createDriveFolder(DRIVE_ROOT_FOLDER_NAME, accessToken, null)
    }

    private fun findDriveFolderIdByName(
        name: String,
        accessToken: String,
        parentId: String? = null,
    ): String? {
        val queryParts = buildList {
            add("name='${escapeDriveQueryValue(name)}'")
            add("mimeType='application/vnd.google-apps.folder'")
            add("trashed=false")
            if (parentId != null) add("'${escapeDriveQueryValue(parentId)}' in parents")
        }
        val encodedQuery = URLEncoder.encode(queryParts.joinToString(" and "), StandardCharsets.UTF_8.name())
        val request = Request.Builder()
            .url("$DRIVE_FILES_URL?q=$encodedQuery&fields=files(id,name)&pageSize=1&spaces=drive")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Drive folder lookup failed: ${response.code} ${response.message}")
            }
            val payload = JSONObject(response.body?.string().orEmpty().ifBlank { "{}" })
            payload.optJSONArray("files")
                ?.optJSONObject(0)
                ?.optString("id")
                ?.takeIf { it.isNotBlank() }
        }
    }

    private fun createDriveFolder(name: String, accessToken: String, parentId: String?): String {
        val body = JSONObject()
            .put("name", name)
            .put("mimeType", "application/vnd.google-apps.folder")
            .apply {
                if (parentId != null) {
                    put("parents", JSONArray().put(parentId))
                }
            }

        val request = Request.Builder()
            .url(DRIVE_FILES_URL)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Drive folder creation failed: ${response.code} ${response.message}")
            }
            JSONObject(response.body?.string().orEmpty().ifBlank { "{}" })
                .optString("id")
                .takeIf { it.isNotBlank() }
                ?: throw IOException("Drive did not return a folder id.")
        }
    }

    private fun uploadFileResumable(
        item: DriveUploadItem,
        accessToken: String,
        parentId: String,
    ): String {
        val metadata = JSONObject()
            .put("name", item.name)
            .put("mimeType", item.mimeType)
            .put("parents", JSONArray().put(parentId))
            .toString()

        val startRequest = Request.Builder()
            .url("$DRIVE_UPLOAD_URL?uploadType=resumable&fields=id")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json; charset=UTF-8")
            .header("X-Upload-Content-Type", item.mimeType)
            .header("X-Upload-Content-Length", item.bytes.size.toString())
            .post(metadata.toRequestBody("application/json; charset=UTF-8".toMediaType()))
            .build()

        val uploadUrl = http.newCall(startRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Drive upload session failed: ${response.code} ${response.message}")
            }
            response.header("Location")
                ?: throw IOException("Drive upload session did not return an upload URL.")
        }

        val uploadRequest = Request.Builder()
            .url(uploadUrl)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", item.mimeType)
            .put(item.bytes.toRequestBody(item.mimeType.toMediaType()))
            .build()

        return http.newCall(uploadRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Drive upload failed: ${response.code} ${response.message}")
            }
            JSONObject(response.body?.string().orEmpty().ifBlank { "{}" })
                .optString("id")
                .takeIf { it.isNotBlank() }
                ?: throw IOException("Drive did not return a file id.")
        }
    }

    private fun setAnyoneWithLinkPermission(itemId: String, accessToken: String) {
        if (itemId.isBlank()) return

        val request = Request.Builder()
            .url("$DRIVE_FILES_URL/$itemId/permissions")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .post("""{"role":"reader","type":"anyone"}""".toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 409) {
                throw IOException("Drive permission update failed: ${response.code} ${response.message}")
            }
        }
    }

    private fun driveSessionFolderName(date: LocalDateTime = LocalDateTime.now()): String {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
    }

    private fun escapeDriveQueryValue(value: String): String {
        return value.replace("\\", "\\\\").replace("'", "\\'")
    }
}

private fun SecretPolicy.toJson(): JSONObject = JSONObject()
    .put("viewOnce", viewOnce)
    .put("timedView", timedView)
    .put("noSelect", noSelect)
    .put("tabSwitchLock", tabSwitchLock)
    .put("devtoolsGuard", devtoolsGuard)
    .put("screenshotGuard", screenshotGuard)
    .put("memoryOnly", memoryOnly)
    .put("viewWindowSeconds", viewWindowSeconds)

private fun JSONObject.toSecretPolicy(): SecretPolicy = SecretPolicy(
    viewOnce = optBoolean("viewOnce", false),
    timedView = optBoolean("timedView", false),
    noSelect = optBoolean("noSelect", false),
    tabSwitchLock = optBoolean("tabSwitchLock", false),
    devtoolsGuard = optBoolean("devtoolsGuard", false),
    screenshotGuard = optBoolean("screenshotGuard", false),
    memoryOnly = optBoolean("memoryOnly", true),
    viewWindowSeconds = optInt("viewWindowSeconds", 0)
)

private fun buildSecretLink(id: String, keyB64: String): String {
    val encodedKey = Uri.encode(keyB64)
    return "${ClexBackendConfig.vaultSecretRevealBaseUrl}?id=${Uri.encode(id)}#key=$encodedKey"
}

private fun buildSecretAccessCode(id: String, keyB64: String): String {
    return "${id.lowercase()}.${toUrlSafeBase64(keyB64)}"
}

private fun toUrlSafeBase64(raw: String): String {
    return raw.replace("+", "-").replace("/", "_").trimEnd('=')
}

private fun encryptSecret(plaintext: String): EncryptedSecretPayload {
    val secureRandom = SecureRandom()
    val keyBytes = ByteArray(32)
    val ivBytes = ByteArray(12)
    secureRandom.nextBytes(keyBytes)
    secureRandom.nextBytes(ivBytes)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val secretKey = SecretKeySpec(keyBytes, "AES")
    val spec = GCMParameterSpec(128, ivBytes)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

    val encryptedBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
    return EncryptedSecretPayload(
        encryptedPayload = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
        iv = Base64.encodeToString(ivBytes, Base64.NO_WRAP),
        keyB64 = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
    )
}

private fun JSONArray.toObjectList(transform: (JSONObject) -> ChainSession): List<ChainSession> {
    val items = mutableListOf<ChainSession>()
    for (index in 0 until length()) {
        val value = optJSONObject(index) ?: continue
        items += transform(value)
    }
    return items
}

private fun JSONObject.optLongOrNull(key: String): Long? {
    if (!has(key) || isNull(key)) return null
    return optLong(key)
}

private fun httpGetJson(url: String): JSONObject = httpJsonRequest(url, "GET")
    ?: throw IOException("Request failed without a response body.")

private fun httpPostJson(url: String, body: String): JSONObject = httpJsonRequest(url, "POST", body)
    ?: throw IOException("Request failed without a response body.")

private fun httpJsonRequest(
    url: String,
    method: String,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
    allowNotFound: Boolean = false,
): JSONObject? {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = method
        connectTimeout = 10_000
        readTimeout = 15_000
        doOutput = body != null
        if (body != null) {
            setRequestProperty("Content-Type", "application/json")
        }
        setRequestProperty("Accept", "application/json")
        headers.forEach { (key, value) -> setRequestProperty(key, value) }
    }

    if (body != null) {
        connection.outputStream.use { output ->
            output.write(body.toByteArray(StandardCharsets.UTF_8))
        }
    }

    return connection.useJsonResponse(allowNotFound = allowNotFound)
}

private fun HttpURLConnection.useJsonResponse(allowNotFound: Boolean = false): JSONObject? {
    return try {
        val code = responseCode
        val body = (if (code in 200..299) inputStream else errorStream)
            ?.bufferedReader(StandardCharsets.UTF_8)
            ?.use { it.readText() }
            .orEmpty()

        if (allowNotFound && code == 404) {
            return null
        }

        if (code !in 200..299) {
            val message = runCatching { JSONObject(body).optString("error") }.getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Request failed ($code)"
            throw IOException(message)
        }

        if (body.isBlank()) JSONObject() else JSONObject(body)
    } finally {
        disconnect()
    }
}
