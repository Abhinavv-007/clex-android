package com.clex.android.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.AppRelease
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.anim.RevealFromBottom
import com.clex.android.ui.anim.rememberEntryVisibility
import com.clex.android.ui.components.BodyText
import com.clex.android.ui.components.BrandLogoImage
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.components.PageMark
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  CLEX — Settings Screen
//  Global app settings:
//    - Appearance (dark/light theme)
//    - About (version, FAQ)
// ═══════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    onNavigateToHelp: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToChangelog: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {}
) {
    val colors = CxTheme.colors
    val isDark = ThemeManager.isDark
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val screenVisible = rememberEntryVisibility("settings")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        MeshGradientBackground(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.12f),
            accentStrength = 0.08f
        )
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.12f),
            particleCount = 12,
            connectDistance = 80f,
            color = colors.accent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(colors.bgPrimary)
                    .padding(
                        horizontal = CxSpacing.screenHorizontal,
                        vertical = CxSpacing.md
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PageMark(glyph = "⚙", title = "SETTINGS")
                MonoText(
                    text = "V${AppRelease.versionName}",
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary,
                    letterSpacing = CxTypography.textXs * 0.15
                )
            }

            // Subtle separator
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.borderSubtle)
            )

            Column(
                modifier = Modifier.padding(
                    horizontal = CxSpacing.screenHorizontal,
                    vertical = CxSpacing.xl
                ),
                verticalArrangement = Arrangement.spacedBy(CxSpacing.xxl)
            ) {

                // ── APPEARANCE ──
                RevealFromBottom(visible = screenVisible, delayMs = 100) {
                    Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                        SectionLabel(text = "Appearance")

                        ThemeToggleRow(
                            isDark = isDark,
                            onToggle = {
                                CxHaptics.snap(view)
                                ThemeManager.toggle()
                            }
                        )
                    }
                }

                // ── ABOUT ──
                RevealFromBottom(visible = screenVisible, delayMs = 220) {
                    Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                        SectionLabel(text = "About")

                        SettingsRow(
                            icon = "⟳",
                            title = "CHANGELOG",
                            subtitle = "${AppRelease.changelog.size} releases · ${AppRelease.changelog.last().version} → ${AppRelease.versionName}",
                            trailingIcon = "→",
                            onClick = onNavigateToChangelog
                        )

                        SettingsRow(
                            icon = "?",
                            title = "HELP & FAQ",
                            subtitle = "How Clex works",
                            trailingIcon = "→",
                            onClick = onNavigateToHelp
                        )

                        SettingsRow(
                            icon = "◆",
                            title = "PRIVACY",
                            subtitle = "Zero knowledge · No server storage",
                            trailingIcon = "→",
                            onClick = onNavigateToPrivacy
                        )

                        SettingsRow(
                            icon = "◉",
                            title = "ABOUT THE DEVELOPER",
                            subtitle = "Links, email, and release contact",
                            trailingIcon = "→",
                            onClick = onNavigateToDeveloper
                        )
                    }
                }

                // ── DATA ──
                RevealFromBottom(visible = screenVisible, delayMs = 340) {
                    Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                        SectionLabel(text = "Data")

                        val context = LocalContext.current
                        SettingsRow(
                            icon = "⊗",
                            title = "CLEAR CACHE",
                            subtitle = "Remove temporary files",
                            trailingIcon = null,
                            onClick = {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                            }
                        )

                        SettingsRow(
                            icon = "⟳",
                            title = "REPLAY ONBOARDING",
                            subtitle = "Show the intro walkthrough again",
                            trailingIcon = "→",
                            onClick = {
                                context.getSharedPreferences("clex_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("has_seen_onboarding", false)
                                    .apply()
                                Toast.makeText(context, "Restart the app to see onboarding", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // ── APP IDENTITY ──
                RevealFromBottom(visible = screenVisible, delayMs = 460) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BrandLogoImage(size = 56.dp)
                        BodyText(
                            text = "DROP  ·  PREPARE  ·  SHARE",
                            fontSize = CxTypography.textXs,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        BodyText(
                            text = "Version ${AppRelease.versionName}",
                            fontSize = CxTypography.textXs,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

// ── THEME TOGGLE ROW ─────────────────────────────

@Composable
private fun ThemeToggleRow(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val colors = CxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.76f else 0.92f))
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
        ) {
            MonoText(
                text = if (isDark) "☾" else "☀",
                fontSize = CxTypography.text2xl,
                color = colors.accent
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MonoText(
                    text = "THEME",
                    fontSize = CxTypography.textSm,
                    fontWeight = CxTypography.weightBold,
                    color = colors.textPrimary
                )
                BodyText(
                    text = if (isDark) "Dark Mode" else "Light Mode",
                    fontSize = CxTypography.textXs
                )
            }
        }

        // Toggle pill
        ThemeTogglePill(isDark = isDark)
    }
}

@Composable
private fun ThemeTogglePill(isDark: Boolean) {
    val colors = CxTheme.colors
    val knobOffset by animateDpAsState(
        targetValue = if (isDark) 24.dp else 2.dp,
        animationSpec = tween(200),
        label = "toggleKnob"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (isDark) colors.accent else colors.bgTertiary)
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset, y = 3.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDark) CxColors.pureBlack else colors.textSecondary)
        )
    }
}

// ── SETTINGS ROW ─────────────────────────────────

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    subtitle: String,
    trailingIcon: String?,
    onClick: () -> Unit
) {
    val colors = CxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.76f else 0.92f))
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
        ) {
            MonoText(
                text = icon,
                fontSize = CxTypography.textXl,
                color = colors.accent
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MonoText(
                    text = title,
                    fontSize = CxTypography.textSm,
                    fontWeight = CxTypography.weightBold,
                    color = colors.textPrimary
                )
                BodyText(
                    text = subtitle,
                    fontSize = CxTypography.textXs
                )
            }
        }
        if (trailingIcon != null) {
            MonoText(
                text = trailingIcon,
                fontSize = CxTypography.textLg,
                color = colors.textTertiary
            )
        }
    }
}
