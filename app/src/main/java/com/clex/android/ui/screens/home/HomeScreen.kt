package com.clex.android.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.R
import com.clex.android.ui.components.*
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  Home — liquid glass cinematic landing.
//  Hero → bento → mascot CTA. Mirrors clex.in.
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
            .background(if (colors.isDark) CxColors.bgPrimary else CxColors.cream),
    ) {
        LiquidMeshBackground(modifier = Modifier.matchParentSize(), intensity = 0.85f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 18.dp)
                .padding(top = 32.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Hero ──
            HomeHero(
                onPrimary = onNavigateToWorkspace,
                onSecondary = onNavigateToHelp,
            )

            // ── Bento grid ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BentoTile(
                    kicker = "drop",
                    title = "Open\nWorkspace",
                    hint = "Pick a file, queue an action, route it on.",
                    accentColors = listOf(CxColors.lavender, CxColors.peach2),
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .clickable { onNavigateToWorkspace() },
                )
                BentoTile(
                    kicker = "vault",
                    title = "Hide a\nsecret",
                    hint = "Encrypted notes, keys, codes — local only.",
                    accentColors = listOf(CxColors.peach, CxColors.yellow),
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .clickable { onNavigateToVault() },
                )
            }

            BentoTile(
                kicker = "chain · routing",
                title = "Send by the best route",
                hint = "Direct, local, then cloud — Clex picks whichever lands fastest, end-to-end encrypted.",
                accentColors = listOf(CxColors.mint, CxColors.blue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { onNavigateToChain() },
            )

            // ── Mascot panel ──
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = CxRadius.lg,
                padding = 22.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    MascotBadge(size = 56.dp, label = null)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Need a hand?",
                            fontSize = CxTypography.textLg,
                            fontFamily = CxTypography.fontDisplay,
                            fontWeight = CxTypography.weightExtrabold,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = "Tap into the help & FAQ — short, no fluff.",
                            fontSize = CxTypography.textSm,
                            fontFamily = CxTypography.fontBody,
                            fontWeight = CxTypography.weightMedium,
                            color = colors.textTertiary,
                            lineHeight = CxTypography.textSm * 1.45,
                        )
                    }
                    SmallPill(
                        text = "Help",
                        onClick = onNavigateToHelp,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHero(
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    val colors = CxTheme.colors
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CxRadius.lg,
        padding = 24.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                KickerChip(text = "clex · file flow")
                Image(
                    painter = painterResource(
                        id = if (colors.isDark) R.drawable.clex_logo_light
                        else R.drawable.clex_logo_dark
                    ),
                    contentDescription = "Clex logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "One drop.",
                fontSize = CxTypography.text5xl,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = CxTypography.weightExtrabold,
                color = colors.textPrimary,
                lineHeight = CxTypography.text5xl * 1.05,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Every ",
                    fontSize = CxTypography.text5xl,
                    fontFamily = CxTypography.fontDisplay,
                    fontWeight = CxTypography.weightExtrabold,
                    color = colors.textPrimary,
                    lineHeight = CxTypography.text5xl * 1.05,
                )
                CursiveAccent(text = "route", fontSize = CxTypography.text5xl)
            }
            Text(
                text = "covered.",
                fontSize = CxTypography.text5xl,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = CxTypography.weightExtrabold,
                color = colors.textPrimary,
                lineHeight = CxTypography.text5xl * 1.05,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Drop, prepare, share. Clex hands off through whichever route lands fastest — direct, local, or cloud.",
                fontSize = CxTypography.textBase,
                fontFamily = CxTypography.fontBody,
                fontWeight = CxTypography.weightMedium,
                color = colors.textSecondary,
                lineHeight = CxTypography.textBase * 1.55,
            )

            Spacer(Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.clickable { onPrimary() }) {
                    LiquidPillButton(text = "Open Workspace")
                }
                Box(modifier = Modifier.clickable { onSecondary() }) {
                    GhostPill(text = "How it works")
                }
            }
        }
    }
}

@Composable
private fun GhostPill(text: String) {
    val colors = CxTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (colors.isDark) Color(0x33FFFFFF) else Color(0x14000000))
            .padding(horizontal = 22.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            fontSize = CxTypography.textBase,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = CxTypography.weightSemibold,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun SmallPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.linearGradient(
                    listOf(CxColors.lavender, CxColors.peach2)
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            fontSize = CxTypography.textSm,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = CxTypography.weightExtrabold,
            color = CxColors.ink,
        )
    }
}
