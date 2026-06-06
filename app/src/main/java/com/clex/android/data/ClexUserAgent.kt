package com.clex.android.data

import android.content.Context
import android.os.Build
import com.clex.android.AppRelease

// ═══════════════════════════════════════════════════
//  ClexUserAgent — single source of truth for the
//  User-Agent header sent on every API call.
//  Lets the /admin Live Feed → IP History distinguish
//  Android sessions and shows device fingerprint
//  alongside model + Android version.
// ═══════════════════════════════════════════════════
object ClexUserAgent {
    @Volatile
    var value: String? = null
        private set

    fun init(context: Context) {
        if (value != null) return
        val fingerprint = runCatching {
            VaultCryptoManager.getDeviceFingerprint(context.applicationContext)
        }.getOrNull().orEmpty().take(16)
        val model = (Build.MODEL ?: "android").replace(' ', '-')
        val release = Build.VERSION.RELEASE ?: "?"
        val abi = Build.SUPPORTED_ABIS?.firstOrNull() ?: "?"
        value = "Clex-Android/${AppRelease.versionName} " +
                "(${model}; Android $release; abi=$abi; fp=$fingerprint)"
    }
}
