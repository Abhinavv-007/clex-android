package com.clex.android.ui.screens.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.clex.android.AppRelease
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.components.CinematicScaffold
import com.clex.android.ui.components.LiquidGlassCard
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import com.clex.android.ui.theme.ThemeManager

// ═══════════════════════════════════════════════════
//  Settings — liquid glass cinematic shell.
//  Theme, About, FAQ, Privacy, Changelog, Developer.
//  Wiring untouched; only chrome re-skinned.
// ═══════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    onNavigateToHelp: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToChangelog: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {}
) {
    val isDark = ThemeManager.isDark
    val view = LocalView.current

    CinematicScaffold(
        kicker = "settings",
        title = "Tune the",
        cursive = "vibe.",
        body = "Theme, support, privacy, build details — everything app-level lives here.",
    ) {
        ThemeRow(isDark = isDark) {
            CxHaptics.snap(view)
            ThemeManager.toggle()
        }
        SectionLabel("App")
        GlassRow("◐", "Help & FAQ", "Quick answers + onboarding tour", onNavigateToHelp)
        GlassRow("◇", "Privacy", "How Clex handles your data", onNavigateToPrivacy)
        GlassRow("⌘", "Changelog", "What changed across versions", onNavigateToChangelog)
        GlassRow("⌬", "Developer", "API keys, fingerprint, raw routes", onNavigateToDeveloper)

        SectionLabel("About")
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), padding = 18.dp) {
            Column {
                LabelValue("Version", AppRelease.versionName)
                Spacer(Modifier.height(8.dp))
                LabelValue("Build", AppRelease.versionCode.toString())
                Spacer(Modifier.height(8.dp))
                LabelValue("Channel", "Public")
            }
        }
    }
}

@Composable
private fun ThemeRow(isDark: Boolean, onToggle: () -> Unit) {
    val colors = CxTheme.colors
    LiquidGlassCard(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, padding = 18.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = if (isDark) "☾" else "☀",
                    fontSize = CxTypography.text2xl,
                    fontFamily = CxTypography.fontDisplay,
                    color = if (colors.isDark) CxColors.lavender else CxColors.peach3,
                )
                Column {
                    Text(
                        text = "Theme",
                        fontSize = CxTypography.textBase,
                        fontFamily = CxTypography.fontDisplay,
                        fontWeight = CxTypography.weightExtrabold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = if (isDark) "Dark mode" else "Light mode",
                        fontSize = CxTypography.textSm,
                        fontFamily = CxTypography.fontBody,
                        color = colors.textTertiary,
                    )
                }
            }
            ThemeKnob(isDark = isDark)
        }
    }
}

@Composable
private fun ThemeKnob(isDark: Boolean) {
    val knob by animateDpAsState(
        targetValue = if (isDark) 26.dp else 4.dp,
        animationSpec = tween(220),
        label = "knob",
    )
    Box(
        modifier = Modifier
            .width(54.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.linearGradient(
                    if (isDark) listOf(CxColors.lavender, CxColors.lavender3)
                    else listOf(CxColors.peach, CxColors.yellow)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = knob, y = 3.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (isDark) CxColors.ink else CxColors.cream)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = CxTheme.colors
    Text(
        text = text.uppercase(),
        fontSize = CxTypography.textXs,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightExtrabold,
        color = colors.textTertiary,
        letterSpacing = CxTypography.textXs * 0.18,
        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
    )
}

@Composable
private fun GlassRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = CxTheme.colors
    LiquidGlassCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }, padding = 18.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = icon,
                    fontSize = CxTypography.textXl,
                    fontFamily = CxTypography.fontDisplay,
                    color = if (colors.isDark) CxColors.lavender else CxColors.lavender3,
                )
                Column {
                    Text(
                        text = title,
                        fontSize = CxTypography.textBase,
                        fontFamily = CxTypography.fontDisplay,
                        fontWeight = CxTypography.weightExtrabold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = subtitle,
                        fontSize = CxTypography.textSm,
                        fontFamily = CxTypography.fontBody,
                        color = colors.textTertiary,
                    )
                }
            }
            Text(
                text = "›",
                fontSize = CxTypography.text2xl,
                fontFamily = CxTypography.fontDisplay,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = CxTypography.textSm,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = CxTypography.weightSemibold,
            color = colors.textTertiary,
        )
        Text(
            text = value,
            fontSize = CxTypography.textSm,
            fontFamily = CxTypography.fontMono,
            color = colors.textPrimary,
        )
    }
}
