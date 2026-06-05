package com.clex.android.navigation

// ═══════════════════════════════════════════════════
//  CLEX — Navigation Routes & Bottom Nav Config
// ═══════════════════════════════════════════════════

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Workspace : Screen("workspace")
    data object Vault : Screen("vault")
    data object SecretReveal : Screen("vault-secret-reveal")
    data object Chain : Screen("chain")
    data object Settings : Screen("settings")
    data object HelpFaq : Screen("help")
    data object Privacy : Screen("privacy")
    data object Changelog : Screen("changelog")
    data object Developer : Screen("developer")
}

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: String
) {
    HOME(Screen.Workspace.route, "Home", "⌂"),
    VAULT(Screen.Vault.route, "Vault", "◈"),
    CHAIN(Screen.Chain.route, "Chain", "⟐"),
    SETTINGS(Screen.Settings.route, "Settings", "⊙");
}
