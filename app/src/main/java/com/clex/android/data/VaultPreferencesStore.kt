package com.clex.android.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VaultPreferences(
    val defaultSecretExpirySeconds: Int = 24 * 60 * 60,
    val defaultViewOnce: Boolean = true,
    val defaultTimedView: Boolean = false,
    val defaultNoSelect: Boolean = true,
    val defaultTabSwitchLock: Boolean = false,
    val defaultDevtoolsGuard: Boolean = false,
    val defaultScreenshotGuard: Boolean = false,
    val cloudBackupEnabled: Boolean = true,
    val sameAccountVaultEnabled: Boolean = false,
)

class VaultPreferencesStore private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(readSettings())

    val settings: StateFlow<VaultPreferences> = _settings.asStateFlow()

    fun setDefaultSecretExpirySeconds(seconds: Int) {
        update { it.copy(defaultSecretExpirySeconds = seconds.coerceIn(60, 7 * 24 * 60 * 60)) }
    }

    fun setDefaultSecretProtections(
        viewOnce: Boolean,
        timedView: Boolean,
        noSelect: Boolean,
        tabSwitchLock: Boolean,
        devtoolsGuard: Boolean,
        screenshotGuard: Boolean,
    ) {
        update {
            it.copy(
                defaultViewOnce = viewOnce,
                defaultTimedView = timedView,
                defaultNoSelect = noSelect,
                defaultTabSwitchLock = tabSwitchLock,
                defaultDevtoolsGuard = devtoolsGuard,
                defaultScreenshotGuard = screenshotGuard,
            )
        }
    }

    fun setCloudBackupEnabled(enabled: Boolean) {
        update { it.copy(cloudBackupEnabled = enabled) }
    }

    fun setSameAccountVaultEnabled(enabled: Boolean) {
        update { it.copy(sameAccountVaultEnabled = enabled) }
    }

    private fun update(transform: (VaultPreferences) -> VaultPreferences) {
        val next = transform(_settings.value)
        prefs.edit()
            .putInt(KEY_DEFAULT_SECRET_EXPIRY, next.defaultSecretExpirySeconds)
            .putBoolean(KEY_DEFAULT_VIEW_ONCE, next.defaultViewOnce)
            .putBoolean(KEY_DEFAULT_TIMED_VIEW, next.defaultTimedView)
            .putBoolean(KEY_DEFAULT_NO_SELECT, next.defaultNoSelect)
            .putBoolean(KEY_DEFAULT_TAB_SWITCH_LOCK, next.defaultTabSwitchLock)
            .putBoolean(KEY_DEFAULT_DEVTOOLS_GUARD, next.defaultDevtoolsGuard)
            .putBoolean(KEY_DEFAULT_SCREENSHOT_GUARD, next.defaultScreenshotGuard)
            .putBoolean(KEY_CLOUD_BACKUP_ENABLED, next.cloudBackupEnabled)
            .putBoolean(KEY_SAME_ACCOUNT_VAULT_ENABLED, next.sameAccountVaultEnabled)
            .apply()
        _settings.value = next
    }

    private fun readSettings(): VaultPreferences {
        return VaultPreferences(
            defaultSecretExpirySeconds = prefs.getInt(KEY_DEFAULT_SECRET_EXPIRY, 24 * 60 * 60)
                .coerceIn(60, 7 * 24 * 60 * 60),
            defaultViewOnce = prefs.getBoolean(KEY_DEFAULT_VIEW_ONCE, true),
            defaultTimedView = prefs.getBoolean(KEY_DEFAULT_TIMED_VIEW, false),
            defaultNoSelect = prefs.getBoolean(KEY_DEFAULT_NO_SELECT, true),
            defaultTabSwitchLock = prefs.getBoolean(KEY_DEFAULT_TAB_SWITCH_LOCK, false),
            defaultDevtoolsGuard = prefs.getBoolean(KEY_DEFAULT_DEVTOOLS_GUARD, false),
            defaultScreenshotGuard = prefs.getBoolean(KEY_DEFAULT_SCREENSHOT_GUARD, false),
            cloudBackupEnabled = prefs.getBoolean(KEY_CLOUD_BACKUP_ENABLED, true),
            sameAccountVaultEnabled = prefs.getBoolean(KEY_SAME_ACCOUNT_VAULT_ENABLED, false),
        )
    }

    companion object {
        private const val PREFS_NAME = "clex_vault_prefs"
        private const val KEY_DEFAULT_SECRET_EXPIRY = "vault_default_secret_expiry"
        private const val KEY_DEFAULT_VIEW_ONCE = "vault_default_view_once"
        private const val KEY_DEFAULT_TIMED_VIEW = "vault_default_timed_view"
        private const val KEY_DEFAULT_NO_SELECT = "vault_default_no_select"
        private const val KEY_DEFAULT_TAB_SWITCH_LOCK = "vault_default_tab_switch_lock"
        private const val KEY_DEFAULT_DEVTOOLS_GUARD = "vault_default_devtools_guard"
        private const val KEY_DEFAULT_SCREENSHOT_GUARD = "vault_default_screenshot_guard"
        private const val KEY_CLOUD_BACKUP_ENABLED = "vault_cloud_backup_enabled"
        private const val KEY_SAME_ACCOUNT_VAULT_ENABLED = "vault_same_account_enabled"

        @Volatile
        private var instance: VaultPreferencesStore? = null

        fun get(context: Context): VaultPreferencesStore {
            val existing = instance
            if (existing != null) return existing

            return synchronized(this) {
                instance ?: VaultPreferencesStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
