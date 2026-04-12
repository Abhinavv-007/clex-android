package com.clex.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clex.android.data.AppLinkStore
import com.clex.android.data.ClexDriveApi
import com.clex.android.data.DriveAuthStore
import com.clex.android.data.PendingReceiveLink
import com.clex.android.data.PendingSecretLink
import com.clex.android.data.transfer.SharesheetHelper
import com.clex.android.data.transfer.TransferMethod
import com.clex.android.navigation.*
import com.clex.android.ui.theme.ClexTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxTheme
import kotlinx.coroutines.launch

private const val CLEX_PREFS = "clex_prefs"
private const val HAS_SEEN_ONBOARDING_KEY = "has_seen_onboarding"
private const val DRIVE_INTENT_HANDLED_KEY = "drive_intent_handled"

// ═══════════════════════════════════════════════════
//  CLEX — MainActivity
//  Entry point. Sets up:
//    - Edge-to-edge
//    - System bar colors
//    - Navigation controller
//    - Bottom nav shell
//    - Theme provider
// ═══════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    private val driveAuthStore by lazy {
        DriveAuthStore.get(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        com.clex.android.ui.theme.ThemeManager.init(applicationContext)

        setContent {
            ClexTheme {
                ClexApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val targetIntent = intent ?: return
        if (targetIntent.getBooleanExtra(DRIVE_INTENT_HANDLED_KEY, false)) return

        // Handle inbound share from external apps (ACTION_SEND / ACTION_SEND_MULTIPLE)
        if (targetIntent.action == Intent.ACTION_SEND || targetIntent.action == Intent.ACTION_SEND_MULTIPLE) {
            handleInboundShareIntent(targetIntent)
            return
        }

        val data = targetIntent.data ?: return
        if (data.host != "clex.in") return

        when {
            data.path == "/oauth/android" -> handleDriveAuthIntent(targetIntent, data)
            data.path == "/receive" -> handleReceiveLinkIntent(targetIntent, data)
            data.path?.startsWith("/vault/secret") == true -> handleSecretLinkIntent(targetIntent, data)
        }
    }

    private fun handleDriveAuthIntent(targetIntent: Intent, data: android.net.Uri) {
        if (targetIntent.getBooleanExtra(DRIVE_INTENT_HANDLED_KEY, false)) return

        val sessionId = data.getQueryParameter("session_id")?.trim().orEmpty()
        if (sessionId.isBlank()) return

        targetIntent.putExtra(DRIVE_INTENT_HANDLED_KEY, true)
        lifecycleScope.launch {
            val session = runCatching {
                ClexDriveApi.pickupDriveToken(sessionId)
            }.getOrNull()

            if (session != null) {
                driveAuthStore.persist(session)
                Toast.makeText(
                    this@MainActivity,
                    "Google Drive connected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Google Drive sign-in could not be completed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleReceiveLinkIntent(targetIntent: Intent, data: android.net.Uri) {
        val code = data.getQueryParameter("code")?.trim()?.uppercase().orEmpty()
        if (!com.clex.android.data.transfer.isValidRoomCode(code)) return

        val method = when (data.getQueryParameter("mode")?.trim()?.lowercase()) {
            TransferMethod.LOCAL.webValue -> TransferMethod.LOCAL
            else -> TransferMethod.WEBRTC
        }

        targetIntent.putExtra(DRIVE_INTENT_HANDLED_KEY, true)
        AppLinkStore.queueReceiveLink(
            PendingReceiveLink(
                roomCode = code,
                method = method,
            )
        )
    }

    private fun handleInboundShareIntent(targetIntent: Intent) {
        if (targetIntent.getBooleanExtra(DRIVE_INTENT_HANDLED_KEY, false)) return
        val share = SharesheetHelper.extractInboundShare(targetIntent) ?: return
        targetIntent.putExtra(DRIVE_INTENT_HANDLED_KEY, true)

        if (share.uris.isNotEmpty()) {
            AppLinkStore.queueInboundShare(share.uris)
        }
    }

    private fun handleSecretLinkIntent(targetIntent: Intent, data: android.net.Uri) {
        val secretId = data.getQueryParameter("id")
            ?: data.pathSegments.takeIf { it.size >= 3 && it[0] == "vault" && it[1] == "secret" }?.get(2)
            ?: return
        val fragment = data.fragment.orEmpty()
        val keyB64 = fragment.substringAfter("key=", "").takeIf { it.isNotBlank() }
            ?: data.getQueryParameter("key")
            ?: return

        targetIntent.putExtra(DRIVE_INTENT_HANDLED_KEY, true)
        AppLinkStore.queueSecretLink(
            PendingSecretLink(
                secretId = secretId.trim().lowercase(),
                keyB64 = android.net.Uri.decode(keyB64),
            )
        )
    }
}

@Composable
fun ClexApp() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(CLEX_PREFS, Context.MODE_PRIVATE)
    }
    val treatUpgradeAsSeen = remember(context) {
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.lastUpdateTime > packageInfo.firstInstallTime
        }.getOrDefault(false)
    }
    var hasSeenOnboarding by remember {
        mutableStateOf(
            prefs.getBoolean(HAS_SEEN_ONBOARDING_KEY, treatUpgradeAsSeen)
        )
    }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pendingSecretLink by AppLinkStore.pendingSecretLink.collectAsState()
    val pendingReceiveLink by AppLinkStore.pendingReceiveLink.collectAsState()
    val pendingInboundShare by AppLinkStore.pendingInboundShare.collectAsState()

    // System bars — react to theme changes
    val systemUiController = rememberSystemUiController()
    val isDark = com.clex.android.ui.theme.ThemeManager.isDark
    val colors = CxTheme.colors
    LaunchedEffect(isDark) {
        systemUiController.setSystemBarsColor(
            color = if (isDark) CxColors.bgPrimary else CxColors.lightBgPrimary,
            darkIcons = !isDark
        )
    }

    // Routes that show bottom nav
    val showBottomNav = currentRoute in listOf(
        Screen.Workspace.route,
        Screen.Vault.route,
        Screen.Chain.route,
        Screen.Settings.route
    )

    LaunchedEffect(pendingSecretLink) {
        if (pendingSecretLink != null && currentRoute != Screen.SecretReveal.route) {
            navController.navigate(Screen.SecretReveal.route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingReceiveLink) {
        if (pendingReceiveLink != null && currentRoute != Screen.Workspace.route) {
            navController.navigate(Screen.Workspace.route) {
                launchSingleTop = true
            }
        }
    }

    // Navigate to Workspace send tab when files shared from external apps
    LaunchedEffect(pendingInboundShare) {
        if (pendingInboundShare != null && currentRoute != Screen.Workspace.route) {
            navController.navigate(Screen.Workspace.route) {
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        AppNavHost(
            navController = navController,
            hasSeenOnboarding = hasSeenOnboarding,
            onOnboardingComplete = {
                if (!hasSeenOnboarding) {
                    prefs.edit()
                        .putBoolean(HAS_SEEN_ONBOARDING_KEY, true)
                        .apply()
                    hasSeenOnboarding = true
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )

        // Bottom Nav — overlaid at bottom
        if (showBottomNav) {
            Box(modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)) {
                CxBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { item ->
                        navController.navigate(item.route) {
                            popUpTo(Screen.Workspace.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
