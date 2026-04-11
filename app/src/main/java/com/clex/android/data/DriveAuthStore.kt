package com.clex.android.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DriveUserPayload(
    val sub: String,
    val email: String? = null,
    val displayName: String? = null,
    val picture: String? = null,
)

data class DriveSession(
    val token: String,
    val user: DriveUserPayload,
    val updatedAt: Long,
)

class DriveAuthStore private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(readSession())

    val session: StateFlow<DriveSession?> = _session.asStateFlow()

    fun persist(session: DriveSession) {
        prefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_SUB, session.user.sub)
            .putString(KEY_EMAIL, session.user.email)
            .putString(KEY_NAME, session.user.displayName)
            .putString(KEY_PICTURE, session.user.picture)
            .putLong(KEY_UPDATED_AT, session.updatedAt)
            .apply()
        _session.value = session
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_SUB)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_PICTURE)
            .remove(KEY_UPDATED_AT)
            .apply()
        _session.value = null
    }

    private fun readSession(): DriveSession? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        return DriveSession(
            token = token,
            user = DriveUserPayload(
                sub = prefs.getString(KEY_SUB, "").orEmpty(),
                email = prefs.getString(KEY_EMAIL, null),
                displayName = prefs.getString(KEY_NAME, null),
                picture = prefs.getString(KEY_PICTURE, null),
            ),
            updatedAt = prefs.getLong(KEY_UPDATED_AT, 0L),
        )
    }

    companion object {
        private const val PREFS_NAME = "clex_drive"
        private const val KEY_TOKEN = "drive-token"
        private const val KEY_SUB = "drive-user-sub"
        private const val KEY_EMAIL = "drive-user-email"
        private const val KEY_NAME = "drive-user-name"
        private const val KEY_PICTURE = "drive-user-picture"
        private const val KEY_UPDATED_AT = "drive-updated-at"

        @Volatile
        private var instance: DriveAuthStore? = null

        fun get(context: Context): DriveAuthStore {
            val existing = instance
            if (existing != null) return existing

            return synchronized(this) {
                instance ?: DriveAuthStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
