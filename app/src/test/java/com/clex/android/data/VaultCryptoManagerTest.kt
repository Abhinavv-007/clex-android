package com.clex.android.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class VaultCryptoManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getOrCreateLocalMasterKey_generatesAndPersists() {
        val key1 = VaultCryptoManager.getOrCreateLocalMasterKey(context)
        assertNotNull(key1.rawKey)
        assertEquals(32, key1.rawKey.size)
        assertEquals(VaultKeySource.LOCAL, key1.source)

        val key2 = VaultCryptoManager.getOrCreateLocalMasterKey(context)
        assertArrayEquals(key1.rawKey, key2.rawKey)
        assertEquals(key1.fingerprint, key2.fingerprint)
        assertEquals(key1.roomId, key2.roomId)
    }

    @Test
    fun rotateLocalMasterKey_generatesNewKeyAndOverwrites() {
        val key1 = VaultCryptoManager.getOrCreateLocalMasterKey(context)
        val key2 = VaultCryptoManager.rotateLocalMasterKey(context)

        assertEquals(32, key2.rawKey.size)
        assertNotEquals(key1.rawKey.toList(), key2.rawKey.toList())
        assertNotEquals(key1.fingerprint, key2.fingerprint)

        val key3 = VaultCryptoManager.getOrCreateLocalMasterKey(context)
        assertArrayEquals(key2.rawKey, key3.rawKey)
    }

    @Test
    fun deriveSameAccountKey_isDeterministic() {
        val accountId = "user123@example.com"
        val key1 = VaultCryptoManager.deriveSameAccountKey(accountId)

        assertEquals(32, key1.rawKey.size)
        assertEquals(VaultKeySource.SAME_ACCOUNT, key1.source)
        assertEquals(accountId, key1.accountId)

        val key2 = VaultCryptoManager.deriveSameAccountKey(accountId)
        assertArrayEquals(key1.rawKey, key2.rawKey)
    }

    @Test
    fun deriveSameAccountKey_differentAccountsYieldDifferentKeys() {
        val key1 = VaultCryptoManager.deriveSameAccountKey("user1@example.com")
        val key2 = VaultCryptoManager.deriveSameAccountKey("user2@example.com")

        assertNotEquals(key1.rawKey.toList(), key2.rawKey.toList())
    }

    @Test
    fun encryptAndDecryptText_roundtrip() {
        val masterKey = VaultCryptoManager.getOrCreateLocalMasterKey(context)
        val plaintext = "Hello, World!"

        val blob = VaultCryptoManager.encryptText(plaintext, masterKey)
        assertNotNull(blob.ciphertextB64)
        assertNotNull(blob.ivB64)

        val decrypted = VaultCryptoManager.decryptText(blob, masterKey)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun decryptSharedSecret_decryptsProperly() {
        // Shared secret decryption operates on base64 strings directly rather than VaultMasterKey
        val plaintext = "Secret123"
        val masterKey = VaultCryptoManager.getOrCreateLocalMasterKey(context)
        val keyB64 = android.util.Base64.encodeToString(masterKey.rawKey, android.util.Base64.NO_WRAP)

        val blob = VaultCryptoManager.encryptText(plaintext, masterKey)

        val decrypted = VaultCryptoManager.decryptSharedSecret(
            encryptedPayloadB64 = blob.ciphertextB64,
            ivB64 = blob.ivB64,
            keyB64 = keyB64
        )
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun exportAndImportKey_roundtrip() {
        val key1 = VaultCryptoManager.getOrCreateLocalMasterKey(context)

        val json = VaultCryptoManager.exportKeyAsJson(key1)
        assertTrue(json.contains(key1.fingerprint))
        assertTrue(json.contains("\"version\": 1"))

        val key2 = VaultCryptoManager.importKeyFromJson(context, json)
        assertArrayEquals(key1.rawKey, key2.rawKey)
        assertEquals(key1.fingerprint, key2.fingerprint)
        assertEquals(key1.roomId, key2.roomId)
        assertEquals(VaultKeySource.LOCAL, key2.source)
    }

    @Test(expected = IllegalArgumentException::class)
    fun importKeyFromJson_failsOnInvalidVersion() {
        val invalidJson = """
            {
              "version": 2,
              "keyBytes": [1,2,3],
              "fingerprint": "1234",
              "exportedAt": "now"
            }
        """.trimIndent()
        VaultCryptoManager.importKeyFromJson(context, invalidJson)
    }

    @Test
    fun getDeviceFingerprint_generatesAndPersists() {
        val fp1 = VaultCryptoManager.getDeviceFingerprint(context)
        assertEquals(16, fp1.length) // 8 bytes in hex

        val fp2 = VaultCryptoManager.getDeviceFingerprint(context)
        assertEquals(fp1, fp2)
    }

    @Test
    fun detectDeviceName_returnsNonBlankString() {
        val name = VaultCryptoManager.detectDeviceName()
        assertTrue(name.isNotBlank())
    }
}
