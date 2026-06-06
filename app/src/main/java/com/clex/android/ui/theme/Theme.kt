package com.clex.android.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════
//  CLEX — Theme Provider
//  Wraps Neo-Brutalist tokens into a Compose theme
//  Supports runtime dark↔light toggle
// ═══════════════════════════════════════════════════

// ── Global theme state ─────────────────────────────
object ThemeManager {
    var isDark by mutableStateOf(false)
        internal set

    private var prefs: android.content.SharedPreferences? = null
    private const val PREF_KEY = "theme_is_dark"

    fun init(context: android.content.Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("clex_prefs", android.content.Context.MODE_PRIVATE)
        isDark = prefs?.getBoolean(PREF_KEY, false) ?: false
    }

    fun toggle() {
        isDark = !isDark
        prefs?.edit()?.putBoolean(PREF_KEY, isDark)?.apply()
    }
}

@Immutable
data class CxColorScheme(
    val bgPrimary: Color,
    val bgSecondary: Color,
    val bgTertiary: Color,
    val bgCard: Color,
    val bgCardHover: Color,
    val bgElevated: Color,
    val bgInput: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textInverse: Color,
    val accent: Color,
    val accentHover: Color,
    val accentMuted: Color,
    val accentSecondary: Color,
    val accentTertiary: Color,
    val borderColor: Color,
    val borderBold: Color,
    val borderSubtle: Color,
    val shadowColor: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val isDark: Boolean
)

val DarkCxColors = CxColorScheme(
    bgPrimary = CxColors.bgPrimary,
    bgSecondary = CxColors.bgSecondary,
    bgTertiary = CxColors.bgTertiary,
    bgCard = CxColors.bgCard,
    bgCardHover = CxColors.bgCardHover,
    bgElevated = CxColors.bgElevated,
    bgInput = CxColors.bgInput,
    textPrimary = CxColors.textPrimary,
    textSecondary = CxColors.textSecondary,
    textTertiary = CxColors.textTertiary,
    textInverse = CxColors.textInverse,
    accent = CxColors.accent,
    accentHover = CxColors.accentHover,
    accentMuted = CxColors.accentMuted,
    accentSecondary = CxColors.accentSecondary,
    accentTertiary = CxColors.accentTertiary,
    borderColor = CxColors.borderColor,
    borderBold = CxColors.borderBold,
    borderSubtle = CxColors.borderSubtle,
    shadowColor = CxColors.shadowColor,
    error = CxColors.error,
    success = CxColors.success,
    warning = CxColors.warning,
    isDark = true
)

val LightCxColors = CxColorScheme(
    bgPrimary = CxColors.lightBgPrimary,
    bgSecondary = CxColors.lightBgSecondary,
    bgTertiary = CxColors.lightBgTertiary,
    bgCard = CxColors.lightBgCard,
    bgCardHover = CxColors.lightBgCardHover,
    bgElevated = CxColors.lightBgElevated,
    bgInput = CxColors.lightBgInput,
    textPrimary = CxColors.lightTextPrimary,
    textSecondary = CxColors.lightTextSecondary,
    textTertiary = CxColors.lightTextTertiary,
    textInverse = CxColors.lightTextInverse,
    accent = Color(0xFF5B3FC0),              // deep accent text on cream
    accentHover = Color(0xFF8B5CF6),
    accentMuted = Color(0x335B3FC0),
    accentSecondary = CxColors.accentSecondary,
    accentTertiary = Color(0xFF15A3D8),       // saturated cyan for light bg
    borderColor = CxColors.lightBorderColor,
    borderBold = CxColors.lightBorderBold,
    borderSubtle = CxColors.lightBorderSubtle,
    shadowColor = CxColors.lightShadowColor,
    error = CxColors.error,
    success = CxColors.success,
    warning = CxColors.warning,
    isDark = false
)

val LocalCxColors = staticCompositionLocalOf { LightCxColors }

object CxTheme {
    val colors: CxColorScheme
        @Composable get() = LocalCxColors.current
}

@Composable
fun ClexTheme(
    darkTheme: Boolean = ThemeManager.isDark,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkCxColors else LightCxColors

    CompositionLocalProvider(
        LocalCxColors provides colorScheme,
        content = content
    )
}
