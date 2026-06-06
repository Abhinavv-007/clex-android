package com.clex.android.ui.screens.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.AppRelease
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.components.BrandMark
import com.clex.android.ui.components.ClexMotion
import com.clex.android.ui.components.CxIcon
import com.clex.android.ui.components.CxIconType
import com.clex.android.ui.components.HRule
import com.clex.android.ui.components.LedgerRow
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.components.pressable
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import com.clex.android.ui.theme.ThemeManager

// ═══════════════════════════════════════════════════
//  Settings — v1.12 Ledger shell.
// ═══════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    onNavigateToHelp: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToChangelog: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {},
) {
    val colors = CxTheme.colors
    val isDark = ThemeManager.isDark
    val view = LocalView.current
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 130.dp),
        ) {
            // ── Header ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        start = CxSpacing.screenHorizontal,
                        end = CxSpacing.screenHorizontal,
                        top = CxSpacing.lg,
                        bottom = CxSpacing.md,
                    ),
            ) {
                Text(
                    text = "SETTINGS",
                    color = colors.textTertiary,
                    fontSize = 11.sp,
                    fontFamily = CxTypography.fontDisplay,
                    fontWeight = FontWeight.W600,
                    letterSpacing = 1.6.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Preferences",
                    color = colors.textPrimary,
                    fontSize = 32.sp,
                    fontFamily = CxTypography.fontDisplay,
                    fontWeight = FontWeight.W700,
                    letterSpacing = (-0.5).sp,
                )
            }

            HRule(padded = false)

            Spacer(Modifier.height(32.dp))

            // ── Section 01 — Appearance ──
            SectionLabel(text = "Appearance", number = "01")
            Spacer(Modifier.height(14.dp))
            ThemeRow(isDark = isDark) {
                CxHaptics.snap(view)
                ThemeManager.toggle()
            }

            Spacer(Modifier.height(40.dp))

            // ── Section 02 — Help & info ──
            SectionLabel(text = "Help & info", number = "02")
            Spacer(Modifier.height(14.dp))
            LedgerRow(
                label = "Walkthrough & FAQ",
                description = "Quick answers + onboarding tour",
                leadingIcon = CxIconType.QUESTION,
                onClick = onNavigateToHelp,
            )
            HRule()
            LedgerRow(
                label = "Privacy",
                description = "How Clex handles your data",
                leadingIcon = CxIconType.SHIELD,
                onClick = onNavigateToPrivacy,
            )
            HRule()
            LedgerRow(
                label = "Changelog",
                description = "What changed across versions",
                leadingIcon = CxIconType.SPARKLE,
                onClick = onNavigateToChangelog,
            )
            HRule()
            LedgerRow(
                label = "Developer",
                description = "API keys, fingerprint, raw routes",
                leadingIcon = CxIconType.KEY,
                onClick = onNavigateToDeveloper,
            )

            Spacer(Modifier.height(40.dp))

            // ── Section 03 — About ──
            SectionLabel(text = "About", number = "03")
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = CxSpacing.screenHorizontal)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderColor, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(size = 32.dp, color = colors.textPrimary)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Clex",
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontFamily = CxTypography.fontDisplay,
                                fontWeight = FontWeight.W700,
                                letterSpacing = (-0.3).sp,
                            )
                            Text(
                                text = "File routing for everywhere",
                                color = colors.textTertiary,
                                fontSize = 12.sp,
                                fontFamily = CxTypography.fontBody,
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    LabelValue("Version", AppRelease.versionName)
                    Spacer(Modifier.height(10.dp))
                    LabelValue("Build", AppRelease.versionCode.toString())
                    Spacer(Modifier.height(10.dp))
                    LabelValue("Channel", "Public")
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(isDark: Boolean, onToggle: () -> Unit) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onClick = onToggle)
            .padding(horizontal = CxSpacing.screenHorizontal, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.bgSecondary),
            contentAlignment = Alignment.Center,
        ) {
            CxIcon(
                icon = if (isDark) CxIconType.MOON else CxIconType.SUN,
                size = 18.dp,
                color = colors.textPrimary,
                strokeWidth = 1.5.dp,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Theme",
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontFamily = CxTypography.fontBody,
                fontWeight = FontWeight.W500,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isDark) "Dark mode" else "Light mode",
                color = colors.textTertiary,
                fontSize = 13.sp,
                fontFamily = CxTypography.fontBody,
            )
        }
        ThemeKnob(isDark = isDark)
    }
}

@Composable
private fun ThemeKnob(isDark: Boolean) {
    val colors = CxTheme.colors
    val knob by animateDpAsState(
        targetValue = if (isDark) 26.dp else 4.dp,
        animationSpec = ClexMotion.defaultSpring(),
        label = "knob",
    )
    Box(
        modifier = Modifier
            .width(54.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (isDark) colors.textPrimary else colors.bgSecondary)
            .border(1.dp, colors.borderColor, RoundedCornerShape(999.dp)),
    ) {
        Box(
            modifier = Modifier
                .offset(x = knob, y = 3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isDark) colors.bgPrimary else colors.textPrimary),
        )
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
            color = colors.textTertiary,
            fontSize = 13.sp,
            fontFamily = CxTypography.fontBody,
            fontWeight = FontWeight.W400,
        )
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontFamily = CxTypography.fontMono,
            fontWeight = FontWeight.W500,
        )
    }
}
