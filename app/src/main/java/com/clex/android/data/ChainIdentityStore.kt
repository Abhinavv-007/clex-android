package com.clex.android.data

import android.content.Context
import java.security.SecureRandom

object ChainIdentityStore {
    private const val PREFS = "clex_chain_prefs"
    private const val KEY_CHAIN_ID = "clex-chain-id"

    fun getOrCreate(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_CHAIN_ID, null)?.takeIf { it.matches(Regex("^[0-9a-f]{32}$")) }
        if (existing != null) return existing

        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        val generated = bytes.joinToString("") { "%02x".format(it) }
        prefs.edit().putString(KEY_CHAIN_ID, generated).apply()
        return generated
    }
}
