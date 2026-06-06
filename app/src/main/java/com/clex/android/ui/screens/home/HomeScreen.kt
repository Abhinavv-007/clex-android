package com.clex.android.ui.screens.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.ui.components.BrandMark
import com.clex.android.ui.components.CxIcon
import com.clex.android.ui.components.CxIconType
import com.clex.android.ui.components.HRule
import com.clex.android.ui.components.HeaderIconButton
import com.clex.android.ui.components.LedgerRow
import com.clex.android.ui.components.PressableCard
import com.clex.android.ui.components.SectionLabel
import com.clex.android.ui.components.StatusDot
import com.clex.android.ui.components.pressable
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

// ═══════════════════════════════════════════════════
//  Home — v1.12 Ledger landing.
//  Brand mark + edge header + numbered ledger sections.
//  No glass, no mesh, no blob bento. Paper, ink, accent.
// ═══════════════════════════════════════════════════

@Composable
fun HomeScreen(
    onNavigateToWorkspace: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToChain: () -> Unit,
    onNavigateToHelp: () -> Unit,
) {
    val colors = CxTheme.colors
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
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = CxSpacing.screenHorizontal,
                        vertical = CxSpacing.md,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(size = 28.dp, color = colors.textPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Clex",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontFamily = CxTypography.fontDisplay,
                        fontWeight = FontWeight.W700,
                        letterSpacing = (-0.4).sp,
                    )
                }
                HeaderIconButton(
                    icon = CxIconType.QUESTION,
                    onClick = onNavigateToHelp,
                )
            }

            HRule(padded = false)

            Spacer(Modifier.height(40.dp))

            // ── Hero ──
            Hero(onPrimary = onNavigateToWorkspace)

            Spacer(Modifier.height(48.dp))

            // ── Section 01 — Surfaces ──
            SectionLabel(text = "Surfaces", number = "01")
            Spacer(Modifier.height(14.dp))

            LedgerRow(
                label = "Workspace",
                description = "Pick a file. Queue. Route.",
                leadingIcon = CxIconType.SEND,
                onClick = onNavigateToWorkspace,
            )
            HRule()
            LedgerRow(
                label = "Vault",
                description = "Encrypted notes & secrets, on-device.",
                leadingIcon = CxIconType.LOCK,
                onClick = onNavigateToVault,
            )
            HRule()
            LedgerRow(
                label = "Chain",
                description = "Inspect routing path & ledger.",
                leadingIcon = CxIconType.CHAIN,
                onClick = onNavigateToChain,
            )

            Spacer(Modifier.height(48.dp))

            // ── Section 02 — Status ──
            SectionLabel(text = "Status", number = "02")
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
                        StatusDot(color = colors.success)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "All systems",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontFamily = CxTypography.fontBody,
                            fontWeight = FontWeight.W500,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "OPERATIONAL",
                            color = colors.success,
                            fontSize = 11.sp,
                            fontFamily = CxTypography.fontDisplay,
                            fontWeight = FontWeight.W700,
                            letterSpacing = 1.5.sp,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    StatusLine("Routing", "Direct → Local → Cloud")
                    Spacer(Modifier.height(8.dp))
                    StatusLine("Encryption", "AES-256-GCM, on-device")
                    Spacer(Modifier.height(8.dp))
                    StatusLine("Telemetry", "Off")
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Section 03 — Help ──
            SectionLabel(text = "Help", number = "03")
            Spacer(Modifier.height(14.dp))
            LedgerRow(
                label = "Walkthrough & FAQ",
                description = "Get to the relevant bit fast.",
                leadingIcon = CxIconType.QUESTION,
                onClick = onNavigateToHelp,
            )
            HRule()
        }
    }
}

@Composable
private fun Hero(onPrimary: () -> Unit) {
    val colors = CxTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal),
    ) {
        Text(
            text = "FILE ROUTING",
            color = colors.textTertiary,
            fontSize = 11.sp,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = FontWeight.W600,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "One drop.",
            color = colors.textPrimary,
            fontSize = 56.sp,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = FontWeight.W700,
            letterSpacing = (-1.5).sp,
            lineHeight = 60.sp,
        )
        Text(
            text = "Every route",
            color = colors.textPrimary,
            fontSize = 56.sp,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = FontWeight.W700,
            letterSpacing = (-1.5).sp,
            lineHeight = 60.sp,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "covered",
                color = colors.textPrimary,
                fontSize = 56.sp,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = FontWeight.W700,
                letterSpacing = (-1.5).sp,
                lineHeight = 60.sp,
            )
            Text(
                text = ".",
                color = colors.accent,
                fontSize = 56.sp,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = FontWeight.W700,
                lineHeight = 60.sp,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Drop, prepare, share. Clex routes through whichever path lands fastest — direct, local, or cloud — end-to-end encrypted.",
            color = colors.textSecondary,
            fontSize = 15.sp,
            fontFamily = CxTypography.fontBody,
            fontWeight = FontWeight.W400,
            lineHeight = 23.sp,
        )
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.textPrimary)
                    .pressable(onClick = onPrimary)
                    .padding(horizontal = 22.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Open workspace",
                        color = colors.textInverse,
                        fontSize = 14.sp,
                        fontFamily = CxTypography.fontDisplay,
                        fontWeight = FontWeight.W600,
                    )
                    Spacer(Modifier.width(10.dp))
                    CxIcon(
                        icon = CxIconType.ARROW_RIGHT,
                        size = 16.dp,
                        color = colors.textInverse,
                        strokeWidth = 1.6.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    val colors = CxTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = colors.textTertiary,
            fontSize = 13.sp,
            fontFamily = CxTypography.fontBody,
            fontWeight = FontWeight.W400,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontFamily = CxTypography.fontMono,
            fontWeight = FontWeight.W500,
        )
    }
}
