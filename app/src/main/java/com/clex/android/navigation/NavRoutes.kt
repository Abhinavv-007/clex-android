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
}

// Bottom nav items — 4 primary destinations
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: String   // Emoji/text icon — Neo-Brutalist, no Material icons
) {
    HOME(Screen.Workspace.route, "HOME", "⌂"),
    VAULT(Screen.Vault.route, "VAULT", "◈"),
    CHAIN(Screen.Chain.route, "CHAIN", "⟐"),
    SETTINGS(Screen.Settings.route, "SETTINGS", "⊙");
}
