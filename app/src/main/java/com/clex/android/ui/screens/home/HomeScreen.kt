package com.clex.android.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import com.clex.android.ui.anim.*
import com.clex.android.ui.components.*
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.effects.VignetteOverlay
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  CLEX — Home Screen
//  The landing page of the app:
//    Hero → Marquee → Product Story → Vault → Routing → Features → Trust → CTA
//  Matches the web homepage architecture, mobile-native
// ═══════════════════════════════════════════════════

@Composable
fun HomeScreen(
    onNavigateToWorkspace: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToChain: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val screenVisible = rememberEntryVisibility("home")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── HERO SECTION ──
            HeroSection(
                visible = screenVisible,
                onWorkspace = onNavigateToWorkspace,
                onVault = onNavigateToVault,
                onHelp = onNavigateToHelp
            )

            // ── MARQUEE STRIP ──
            RevealFromBottom(visible = screenVisible, delayMs = 380) {
                MarqueeStrip()
            }

            // ── PRODUCT STORY: Drop → Prepare → Share ──
            ProductStorySection(visible = screenVisible)

            // ── VAULT SECTION ──
            VaultSection(visible = screenVisible, onNavigate = onNavigateToVault)

            // ── ROUTING PANEL ──
            RoutingPanel(visible = screenVisible)

            // ── FEATURES GRID ──
            FeaturesGrid(visible = screenVisible)

            // ── TRUST STRIP ──
            TrustStrip(visible = screenVisible)

            // ── FINAL CTA ──
            FinalCta(onWorkspace = onNavigateToWorkspace)

            Spacer(Modifier.height(120.dp))  // Bottom nav clearance
        }

        // Global vignette
        VignetteOverlay(
            modifier = Modifier
                .matchParentSize()
                .alpha(if (colors.isDark) 0.3f else 0.12f),
            strength = if (colors.isDark) 0.4f else 0.16f
        )
    }
}

// ── HERO ──────────────────────────────────────────

@Composable
private fun HeroSection(
    visible: Boolean,
    onWorkspace: () -> Unit,
    onVault: () -> Unit,
    onHelp: () -> Unit = {}
) {
    val colors = CxTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPrimary)
    ) {
        MeshGradientBackground(
            modifier = Modifier
                .matchParentSize()
                .alpha(if (colors.isDark) 0.34f else 0.18f),
            accentStrength = 0.14f
        )
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(if (colors.isDark) 0.18f else 0.08f),
            particleCount = 28,
            connectDistance = 110f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CxSpacing.screenHorizontal)
                .statusBarsPadding()
                .padding(top = CxSpacing.xxxl, bottom = CxSpacing.xxl)
        ) {
            RevealFromBottom(visible = visible, delayMs = 0) {
                NeonTag(text = "PRIVACY-FIRST FILE MOVEMENT", accent = colors.accent)
            }

            Spacer(Modifier.height(CxSpacing.xl))

            RevealFromBottom(visible = visible, delayMs = 100) {
                HeroTitle(
                    text = "DROP\nPREPARE\nSHARE",
                    fontSize = CxTypography.text6xl,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.height(CxSpacing.lg))

            RevealFromBottom(visible = visible, delayMs = 200) {
                BodyText(
                    text = "Transform files in-browser, then share through the fastest route available. No server. No upload. Just movement.",
                    color = colors.textSecondary,
                    fontSize = CxTypography.textLg
                )
            }

            Spacer(Modifier.height(CxSpacing.xl))

            RevealFromBottom(visible = visible, delayMs = 300) {
                Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                    GlowButton(
                        text = "OPEN WORKSPACE →",
                        onClick = onWorkspace,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)) {
                        BrutalistButton(
                            text = "EXPLORE VAULT",
                            onClick = onVault,
                            variant = ButtonVariant.SECONDARY,
                            size = ButtonSize.MEDIUM,
                            modifier = Modifier.weight(1f)
                        )
                        BrutalistButton(
                            text = "HOW IT WORKS",
                            onClick = onHelp,
                            variant = ButtonVariant.GHOST,
                            size = ButtonSize.MEDIUM,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(CxSpacing.lg))

            RevealFromBottom(visible = visible, delayMs = 360) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
                ) {
                    LiveStatTile(
                        label = "Routes",
                        value = 3,
                        modifier = Modifier.weight(1f),
                        accent = colors.accent,
                        live = true
                    )
                    LiveStatTile(
                        label = "Toolchain",
                        value = 12,
                        modifier = Modifier.weight(1f),
                        accent = CxColors.success,
                        live = false
                    )
                }
            }
        }
    }
}

// ── MARQUEE STRIP ────────────────────────────────

@Composable
private fun MarqueeStrip() {
    val colors = CxTheme.colors

    val items = listOf(
        "P2P ENCRYPTED",
        "ZERO CLOUDS",
        "LOCAL NETWORK",
        "ZERO KNOWLEDGE",
        "NO TRACKING",
        "DIRECT TRANSFER",
        "CROSS PLATFORM"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accent)
            .border(
                width = CxBorders.heavy,
                color = CxColors.pureBlack
            )
            .padding(vertical = CxSpacing.md)
    ) {
        InfiniteMarquee(
            durationMs = 18000,
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = CxSpacing.lg)
                ) {
                    MonoText(
                        text = "◆",
                        fontSize = CxTypography.textXs,
                        fontWeight = CxTypography.weightBold,
                        color = CxColors.pureBlack,
                        letterSpacing = CxTypography.textXs * 0.1
                    )
                    MonoText(
                        text = item,
                        fontSize = CxTypography.textXs,
                        color = CxColors.pureBlack,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(start = CxSpacing.sm)
                    )
                }
            }
        }
    }
}

// ── PRODUCT STORY ────────────────────────────────

@Composable
private fun ProductStorySection(visible: Boolean) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.sectionGap)
    ) {
        RevealFromBottom(visible = visible, delayMs = 400) {
            SectionLabel(text = "How It Works")
        }
        Spacer(Modifier.height(CxSpacing.lg))
        RevealFromBottom(visible = visible, delayMs = 500) {
            SectionTitle(text = "ONE WORKSPACE\nTHREE MOVES")
        }
        Spacer(Modifier.height(CxSpacing.xl))

        // Three step cards
        val steps = listOf(
            Triple("01", "DROP", "Drag files into your workspace. Images, PDFs, documents — anything. They stay in your device's memory."),
            Triple("02", "PREPARE", "Use built-in tools. Compress images, merge PDFs, convert formats. Chain operations together."),
            Triple("03", "SHARE", "Hit share. Clex finds the fastest route. Direct P2P → Local Network → Google Drive fallback.")
        )

        steps.forEachIndexed { index, (num, title, desc) ->
            RevealFromBottom(visible = visible, delayMs = 600L + index * CxAnim.staggerDelay) {
                StepCard(number = num, title = title, description = desc)
            }
            if (index < steps.lastIndex) {
                Spacer(Modifier.height(CxSpacing.md))
            }
        }
    }
}

@Composable
private fun StepCard(number: String, title: String, description: String) {
    val colors = CxTheme.colors

    BrutalistCard {
        Row(verticalAlignment = Alignment.Top) {
            AccentNumber(
                text = number,
                fontSize = CxTypography.text4xl
            )
            Spacer(Modifier.width(CxSpacing.lg))
            Column {
                CardTitle(text = title)
                Spacer(Modifier.height(CxSpacing.sm))
                BodyText(text = description, fontSize = CxTypography.textSm)
            }
        }
    }
}

// ── VAULT SECTION ────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VaultSection(visible: Boolean, onNavigate: () -> Unit) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSecondary)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.sectionGap)
    ) {
        RevealFromBottom(visible = visible, delayMs = 800) {
            SectionLabel(text = "Vault")
        }
        Spacer(Modifier.height(CxSpacing.lg))
        RevealFromBottom(visible = visible, delayMs = 900) {
            SectionTitle(text = "LOCAL-FIRST\nNOTES WITH\nTRUSTED SYNC")
        }
        Spacer(Modifier.height(CxSpacing.lg))

        RevealFromBottom(visible = visible, delayMs = 1000) {
            BodyText(
                text = "Encrypted notes, secret links with QR handoffs, and timed Drive-share sessions. Everything private, everything controlled."
            )
        }

        Spacer(Modifier.height(CxSpacing.xl))

        // Vault feature chips
        RevealFromBottom(visible = visible, delayMs = 1100) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)
            ) {
                listOf("ENCRYPTED", "OFFLINE", "SYNC", "SECRET LINKS").forEach {
                    BrutalistBadge(text = it)
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.xl))

        RevealFromBottom(visible = visible, delayMs = 1200) {
            BrutalistButton(
                text = "EXPLORE VAULT →",
                onClick = onNavigate,
                variant = ButtonVariant.PRIMARY,
                size = ButtonSize.MEDIUM
            )
        }
    }
}

// ── ROUTING PANEL ────────────────────────────────

@Composable
private fun RoutingPanel(visible: Boolean) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.sectionGap)
    ) {
        RevealFromBottom(visible = visible, delayMs = 1300) {
            SectionLabel(text = "Delivery Engine")
        }
        Spacer(Modifier.height(CxSpacing.lg))
        RevealFromBottom(visible = visible, delayMs = 1400) {
            SectionTitle(text = "THREE ROUTES\nONE DECISION")
        }
        Spacer(Modifier.height(CxSpacing.xl))

        // Route cards
        val routes = listOf(
            RouteData("DIRECT P2P", "WebRTC browser-to-browser. No server, no relay.", CxColors.accent, "ACTIVE", true),
            RouteData("LOCAL NETWORK", "Same Wi-Fi detection. LAN-speed transfer.", CxColors.accentTertiary, "SCANNING", false),
            RouteData("GOOGLE DRIVE", "Fallback relay. Your Drive account, not ours.", CxColors.accentSecondary, "STANDBY", false)
        )

        routes.forEachIndexed { index, route ->
            RevealFromBottom(visible = visible, delayMs = 1500L + index * CxAnim.staggerDelay) {
                RouteCard(route)
            }
            if (index < routes.lastIndex) {
                Spacer(Modifier.height(CxSpacing.md))
            }
        }
    }
}

data class RouteData(
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color,
    val status: String,
    val isPrimary: Boolean
)

@Composable
private fun RouteCard(route: RouteData) {
    val colors = CxTheme.colors

    // Shadow padding to prevent overlap with neighbors
    Box(modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = colors.shadowColor,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = Size(this.size.width, this.size.height)
                    )
                }
                .border(
                    CxBorders.thick,
                    if (route.isPrimary) route.color else colors.borderColor
                )
                .background(
                    if (route.isPrimary) colors.accentMuted else colors.bgCard
                )
                .padding(CxSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(color = route.color, pulsing = route.isPrimary)
            Spacer(Modifier.width(CxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                MonoText(
                    text = route.title,
                    fontSize = CxTypography.textBase,
                    fontWeight = CxTypography.weightBold,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                BodyText(
                    text = route.description,
                    fontSize = CxTypography.textSm
                )
            }
            Spacer(Modifier.width(CxSpacing.md))
            BrutalistBadge(
                text = route.status,
                accentColor = route.color,
                showDot = route.isPrimary
            )
        }
    }
}

// ── FEATURES GRID ────────────────────────────────

@Composable
private fun FeaturesGrid(visible: Boolean) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSecondary)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.sectionGap)
    ) {
        RevealFromBottom(visible = visible, delayMs = 1800) {
            SectionLabel(text = "Built-In Tools")
        }
        Spacer(Modifier.height(CxSpacing.lg))
        RevealFromBottom(visible = visible, delayMs = 1900) {
            SectionTitle(text = "EVERYTHING\nIN ONE PLACE")
        }
        Spacer(Modifier.height(CxSpacing.xl))

        val tools = listOf(
            ToolData("⇩", "IMAGE TOOLS", "Compress up to 90%. JPEG, PNG, WebP.", CxColors.toolCyan),
            ToolData("⊕", "PDF OPS", "Merge, split, extract pages.", CxColors.toolAmber),
            ToolData("⬡", "DOCX → PDF", "Word to PDF in seconds. Client-side.", CxColors.toolPurple),
            ToolData("⊞", "ZIP BUNDLE", "Package any set of files.", CxColors.toolGreen),
            ToolData("⟳", "SMART CHAIN", "Connect operations. Zero context switches.", CxColors.toolRed),
            ToolData("◎", "OFFLINE READY", "All tools work without internet.", CxColors.toolGreen)
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useSingleColumn = maxWidth < 420.dp

            if (useSingleColumn) {
                tools.forEachIndexed { index, tool ->
                    RevealFromBottom(
                        visible = visible,
                        delayMs = 2000L + index * CxAnim.staggerDelay
                    ) {
                        ToolCard(
                            tool = tool,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (index < tools.lastIndex) {
                        Spacer(Modifier.height(CxSpacing.md))
                    }
                }
            } else {
                tools.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
                    ) {
                        row.forEach { tool ->
                            RevealFromBottom(
                                modifier = Modifier.weight(1f),
                                visible = visible,
                                delayMs = 2000L + rowIndex * CxAnim.staggerDelay
                            ) {
                                ToolCard(tool, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (row.size < 2) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    if (rowIndex < tools.chunked(2).lastIndex) {
                        Spacer(Modifier.height(CxSpacing.md))
                    }
                }
            }
        }
    }
}

data class ToolData(
    val icon: String,
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color
)

@Composable
private fun ToolCard(tool: ToolData, modifier: Modifier = Modifier) {
    val colors = CxTheme.colors

    // Shadow padding to prevent overlap
    Box(modifier = modifier.padding(bottom = 3.dp, end = 3.dp)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 168.dp)
            .drawBehind {
                drawRect(
                    color = colors.shadowColor,
                    topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(this.size.width, this.size.height)
                )
            }
            .border(CxBorders.thin, colors.borderColor)
            .background(colors.bgCard)
            .padding(CxSpacing.md)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(CxBorders.thin, colors.borderSubtle)
                .background(colors.bgSecondary),
            contentAlignment = Alignment.Center
        ) {
            MonoText(
                text = tool.icon,
                fontSize = CxTypography.text2xl,
                color = tool.color,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(CxSpacing.md))

        MonoText(
            text = tool.title,
            fontSize = CxTypography.textSm,
            fontWeight = CxTypography.weightBold,
            color = colors.textPrimary
        )

        Spacer(Modifier.height(CxSpacing.xs))

        BodyText(
            text = tool.description,
            fontSize = CxTypography.textSm,
            maxLines = 3
        )
    }
    }
}

// ── TRUST STRIP ──────────────────────────────────

@Composable
private fun TrustStrip(visible: Boolean) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.sectionGap)
    ) {
        RevealFromBottom(visible = visible, delayMs = 2200) {
            SectionLabel(text = "Trust")
        }
        Spacer(Modifier.height(CxSpacing.lg))
        RevealFromBottom(visible = visible, delayMs = 2300) {
            SectionTitle(text = "ZERO KNOWLEDGE\nBY DESIGN")
        }
        Spacer(Modifier.height(CxSpacing.xl))

        val trustItems = listOf(
            "NO SERVER STORAGE" to "Files exist only in browser memory during transfer.",
            "NO ACCOUNT REQUIRED" to "Open a link and go. Google auth only for Drive fallback.",
            "END-TO-END ENCRYPTED" to "WebRTC connections encrypted by default via DTLS.",
            "OFFLINE CAPABLE" to "Preparation tools work without internet after first load.",
            "OPEN TRANSFER CHAIN" to "Anonymous, hash-chained public ledger. No PII."
        )

        trustItems.forEachIndexed { index, (title, desc) ->
            RevealFromBottom(
                visible = visible,
                delayMs = 2400L + index * CxAnim.staggerDelay
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CxSpacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    MonoText(
                        text = "✓",
                        color = colors.accent,
                        fontSize = CxTypography.textLg,
                        fontWeight = CxTypography.weightBold
                    )
                    Spacer(Modifier.width(CxSpacing.md))
                    Column {
                        MonoText(
                            text = title,
                            fontSize = CxTypography.textSm,
                            fontWeight = CxTypography.weightBold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        BodyText(text = desc, fontSize = CxTypography.textSm)
                    }
                }
            }
        }
    }
}

// ── FINAL CTA ────────────────────────────────────

@Composable
private fun FinalCta(onWorkspace: () -> Unit) {
    val colors = CxTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accent)
            .border(width = CxBorders.heavy, color = CxColors.pureBlack)
    ) {
        // Subtle radial glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CxColors.pureBlack.copy(alpha = 0.12f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        ),
                        radius = size.minDimension * 0.6f
                    )
                }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CxSpacing.screenHorizontal)
                .padding(vertical = CxSpacing.sectionGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeroTitle(
                text = "READY TO\nMOVE FILES?",
                color = CxColors.pureBlack,
                fontSize = CxTypography.text4xl
            )
            Spacer(Modifier.height(CxSpacing.lg))
            BodyText(
                text = "No download. No install. No signup.",
                color = CxColors.pureBlack.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(CxSpacing.xl))
            BrutalistButton(
                text = "OPEN WORKSPACE →",
                onClick = onWorkspace,
                variant = ButtonVariant.SECONDARY,
                size = ButtonSize.LARGE
            )
        }
    }
}
