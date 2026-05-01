package com.clex.android.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.clex.android.ui.anim.CxTransitions
import com.clex.android.ui.screens.chain.ChainScreen
import com.clex.android.ui.screens.help.HelpFaqScreen
import com.clex.android.ui.screens.home.HomeScreen
import com.clex.android.ui.screens.onboarding.OnboardingScreen
import com.clex.android.ui.screens.settings.ChangelogScreen
import com.clex.android.ui.screens.settings.DeveloperScreen
import com.clex.android.ui.screens.settings.PrivacyScreen
import com.clex.android.ui.screens.settings.SettingsScreen
import com.clex.android.ui.screens.splash.SplashScreen
import com.clex.android.ui.screens.vault.SecretRevealScreen
import com.clex.android.ui.screens.vault.VaultScreen
import com.clex.android.ui.screens.workspace.WorkspaceScreen
import com.clex.android.ui.theme.CxAnim

// ═══════════════════════════════════════════════════
//  CLEX — App Navigation Host
//  Tab routes use fade-only (no slide jank).
//  Sub-navigation (e.g. HelpFaq push) uses slide.
// ═══════════════════════════════════════════════════

// Bottom-tab routes — fade transitions between these
private val tabRoutes = setOf(
    Screen.Workspace.route,
    Screen.Vault.route,
    Screen.Chain.route,
    Screen.Settings.route,
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean {
    return initialState.destination.route in tabRoutes &&
           targetState.destination.route in tabRoutes
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    hasSeenOnboarding: Boolean,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            if (isTabSwitch()) fadeIn(tween(200))
            else CxTransitions.screenEnter
        },
        exitTransition = {
            if (isTabSwitch()) fadeOut(tween(160))
            else CxTransitions.screenExit
        },
        popEnterTransition = {
            if (isTabSwitch()) fadeIn(tween(200))
            else CxTransitions.screenPopEnter
        },
        popExitTransition = {
            if (isTabSwitch()) fadeOut(tween(160))
            else CxTransitions.screenPopExit
        }
    ) {
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(CxAnim.durationNormal)) }
        ) {
            SplashScreen(
                onComplete = {
                    val nextRoute = if (hasSeenOnboarding) {
                        Screen.Workspace.route
                    } else {
                        Screen.Onboarding.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(tween(CxAnim.durationNormal)) },
            exitTransition = { fadeOut(tween(CxAnim.durationNormal)) }
        ) {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                    navController.navigate(Screen.Workspace.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── BOTTOM NAV TABS ──────────────────────────

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToWorkspace = {
                    navController.navigate(Screen.Workspace.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToVault = {
                    navController.navigate(Screen.Vault.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToChain = {
                    navController.navigate(Screen.Chain.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToHelp = {
                    navController.navigate(Screen.HelpFaq.route)
                }
            )
        }

        composable(Screen.Vault.route) {
            VaultScreen()
        }

        composable(Screen.Chain.route) {
            ChainScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToHelp = {
                    navController.navigate(Screen.HelpFaq.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.Privacy.route)
                },
                onNavigateToChangelog = {
                    navController.navigate(Screen.Changelog.route)
                },
                onNavigateToDeveloper = {
                    navController.navigate(Screen.Developer.route)
                }
            )
        }

        composable(Screen.Workspace.route) {
            WorkspaceScreen()
        }

        // ── PUSH NAVIGATION (slide-in) ───────────────

        composable(Screen.SecretReveal.route) {
            SecretRevealScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HelpFaq.route) {
            HelpFaqScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Changelog.route) {
            ChangelogScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Developer.route) {
            DeveloperScreen(onBack = { navController.popBackStack() })
        }
    }
}
