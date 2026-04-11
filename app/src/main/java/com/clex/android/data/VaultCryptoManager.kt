package com.clex.android.data

import android.content.Context
import android.os.Build
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class VaultEncryptedBlob(
    val ciphertextB64: String,
    val ivB64: String,
)

enum class VaultKeySource {
    LOCAL,
    SAME_ACCOUNT,
}

data class VaultMasterKey(
    val rawKey: ByteArray,
    val fingerprint: String,
    val roomId: String,
    val source: VaultKeySource,
    val accountId: String? = null,
)

object VaultCryptoManager {
    private const val PREFS_NAME = "clex_vault_crypto"
    private const val KEY_LOCAL_MASTER_KEY = "vault_local_master_key"
    private const val KEY_DEVICE_FINGERPRINT = "vault_device_fingerprint"
    private const val HKDF_SALT = "clex-vault-v1"
    private const val HKDF_INFO = "vault-master-key-v1"

    fun getOrCreateLocalMasterKey(context: Context): VaultMasterKey {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_LOCAL_MASTER_KEY, null)
        val raw = if (stored.isNullOrBlank()) {
            ByteArray(32).also { SecureRandom().nextBytes(it) }.also {
                prefs.edit()
                    .putString(KEY_LOCAL_MASTER_KEY, Base64.encodeToString(it, Base64.NO_WRAP))
                    .apply()
            }
        } else {
            Base64.decode(stored, Base64.DEFAULT)
        }
        return buildMasterKey(raw, VaultKeySource.LOCAL)
    }

    fun storeLocalMasterKey(context: Context, rawKey: ByteArray): VaultMasterKey {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LOCAL_MASTER_KEY, Base64.encodeToString(rawKey, Base64.NO_WRAP))
            .apply()
        return buildMasterKey(rawKey, VaultKeySource.LOCAL)
    }

    fun rotateLocalMasterKey(context: Context): VaultMasterKey {
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return storeLocalMasterKey(context, raw)
    }

    fun deriveSameAccountKey(accountId: String): VaultMasterKey {
        val ikm = accountId.toByteArray(StandardCharsets.UTF_8)
        val prk = hmacSha256(HKDF_SALT.toByteArray(StandardCharsets.UTF_8), ikm)
        val raw = hkdfExpand(prk, HKDF_INFO.toByteArray(StandardCharsets.UTF_8), 32)
        return buildMasterKey(
            rawKey = raw,
            source = VaultKeySource.SAME_ACCOUNT,
            accountId = accountId,
        )
    }

    fun encryptText(plaintext: String, masterKey: VaultMasterKey): VaultEncryptedBlob {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(masterKey.rawKey, "AES"), GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        return VaultEncryptedBlob(
            ciphertextB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP),
            ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP),
        )
    }

    fun decryptText(blob: VaultEncryptedBlob, masterKey: VaultMasterKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(blob.ivB64, Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(masterKey.rawKey, "AES"), GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(Base64.decode(blob.ciphertextB64, Base64.DEFAULT))
        return String(decrypted, StandardCharsets.UTF_8)
    }

    fun decryptSharedSecret(encryptedPayloadB64: String, ivB64: String, keyB64: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val rawKey = Base64.decode(keyB64, Base64.DEFAULT)
        val iv = Base64.decode(ivB64, Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(rawKey, "AES"), GCMParameterSpec(128, iv))
        val plaintext = cipher.doFinal(Base64.decode(encryptedPayloadB64, Base64.DEFAULT))
        return String(plaintext, StandardCharsets.UTF_8)
    }

    fun exportKeyAsJson(masterKey: VaultMasterKey): String {
        val bytes = masterKey.rawKey.joinToString(",") { it.toUByte().toString() }
        return """
            {
              "version": 1,
              "keyBytes": [$bytes],
              "fingerprint": "${masterKey.fingerprint}",
              "exportedAt": "${java.time.Instant.now()}"
            }
        """.trimIndent()
    }

    fun importKeyFromJson(context: Context, jsonStr: String): VaultMasterKey {
        val json = JSONObject(jsonStr)
        require(json.optInt("version") == 1) { "Invalid key backup version." }
        val keyBytes = json.optJSONArray("keyBytes") ?: error("Key backup is missing keyBytes.")
        require(keyBytes.length() == 32) { "Key backup must contain 32 key bytes." }
        val raw = ByteArray(32) { index -> keyBytes.optInt(index).toByte() }
        return storeLocalMasterKey(context, raw)
    }

    fun getDeviceFingerprint(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_DEVICE_FINGERPRINT, null)
        if (!stored.isNullOrBlank()) return stored

        val fingerprint = ByteArray(8)
            .also { SecureRandom().nextBytes(it) }
            .joinToString("") { byte -> "%02x".format(byte) }

        prefs.edit().putString(KEY_DEVICE_FINGERPRINT, fingerprint).apply()
        return fingerprint
    }

    fun detectDeviceName(): String {
        val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
        val model = Build.MODEL?.trim().orEmpty()
        val version = Build.VERSION.RELEASE?.trim().orEmpty()
        return listOf(manufacturer, model)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Android device" } + if (version.isNotBlank()) " · Android $version" else ""
    }

    private fun buildMasterKey(
        rawKey: ByteArray,
        source: VaultKeySource,
        accountId: String? = null,
    ): VaultMasterKey {
        val hash = MessageDigest.getInstance("SHA-256").digest(rawKey)
        val hashHex = hash.joinToString("") { byte -> "%02x".format(byte) }
        return VaultMasterKey(
            rawKey = rawKey,
            fingerprint = hashHex.take(8).uppercase(),
            roomId = hashHex.take(32),
            source = source,
            accountId = accountId,
        )
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        var output = ByteArray(0)
        var previous = ByteArray(0)
        var counter = 1

        while (output.size < length) {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(prk, "HmacSHA256"))
            mac.update(previous)
            mac.update(info)
            mac.update(counter.toByte())
            previous = mac.doFinal()
            output += previous
            counter += 1
        }

        return output.copyOf(length)
    }
}
