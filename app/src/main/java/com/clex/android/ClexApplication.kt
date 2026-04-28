package com.clex.android

import android.app.Application
import com.clex.android.data.transfer.PeerConnectionFactoryWarmer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Process-wide entry point. Two responsibilities:
 *
 * 1. Pre-warm heavy singletons that the workspace screen would otherwise
 *    initialise lazily on the main thread the first time the user starts a
 *    transfer (PeerConnectionFactory native libs, Drive auth store keys).
 *    Doing this here makes the first transfer feel instant instead of
 *    incurring a ~250ms native-library load on the first tap.
 *
 * 2. Initialise the theme manager so that the very first frame already has
 *    the persisted dark/light mode applied (avoids a one-frame light-mode
 *    flash on cold start).
 *
 * All warm-up work runs on a background dispatcher so it never blocks
 * Application.onCreate.
 */
class ClexApplication : Application() {

    private val warmupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Theme is read on the main thread to avoid a flash on first frame.
        com.clex.android.ui.theme.ThemeManager.init(applicationContext)

        warmupScope.launch {
            // Native WebRTC libraries take ~150–300ms to load; pre-warming on
            // a background thread before the user opens the workspace makes
            // the first transfer feel instant.
            runCatching {
                PeerConnectionFactoryWarmer.warm(applicationContext)
            }
        }
    }
}
