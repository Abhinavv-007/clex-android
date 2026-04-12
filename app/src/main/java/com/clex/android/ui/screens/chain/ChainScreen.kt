package com.clex.android.ui.screens.chain

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.data.ChainSessionDetail
import com.clex.android.data.ChainStats
import com.clex.android.data.ClexChainApi
import com.clex.android.data.ChainSession
import com.clex.android.ui.anim.*
import com.clex.android.ui.components.*
import com.clex.android.ui.effects.MeshGradientBackground
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray

// ═══════════════════════════════════════════════════
//  CLEX — Chain Screen
//  Smooth single-page layout:
//    1. Hero with live stats
//    2. How Chain Works — compact grid
//    3. Pipeline Builder — tool chaining preview
//    4. Public Ledger — live transfer log with detail
// ═══════════════════════════════════════════════════

@Composable
fun ChainScreen() {
    val colors = CxTheme.colors
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val screenVisible = rememberEntryVisibility("chain")
    var refreshNonce by remember { mutableIntStateOf(0) }
    var chainFeedState by remember { mutableStateOf(ChainFeedUiState(isLoading = true)) }
    var selectedEntry by remember { mutableStateOf<LedgerEntry?>(null) }
    var selectedEntryDetail by remember { mutableStateOf<ChainSessionDetail?>(null) }
    var detailLoadingId by remember { mutableStateOf<String?>(null) }
    var detailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshNonce) {
        while (isActive) {
            val cachedState = chainFeedState
            chainFeedState = chainFeedState.copy(
                isLoading = chainFeedState.entries.isEmpty(),
                error = null
            )

            chainFeedState = try {
                val feed = ClexChainApi.fetchFeed(limit = 20)
                ChainFeedUiState(
                    isLoading = false,
                    stats = feed.stats,
                    entries = feed.sessions.map { it.toLedgerEntry() },
                    lastUpdatedAt = System.currentTimeMillis()
                )
            } catch (error: Exception) {
                cachedState.copy(
                    isLoading = false,
                    error = error.message ?: "Could not reach the chain API."
                )
            }

            delay(30_000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        MeshGradientBackground(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.14f),
            accentStrength = 0.08f
        )
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.08f),
            particleCount = 18,
            connectDistance = 90f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ChainTopBar()
            ChainHero(
                stats = chainFeedState.stats,
                visible = screenVisible,
                baseDelay = 40L
            )
            HowChainWorks(
                visible = screenVisible,
                baseDelay = 220L
            )
            PipelineBuilder(
                visible = screenVisible,
                baseDelay = 420L
            )
            PublicLedger(
                feedState = chainFeedState,
                onRetry = { refreshNonce += 1 },
                visible = screenVisible,
                baseDelay = 680L,
                onEntryClick = { entry ->
                    selectedEntry = entry
                    selectedEntryDetail = null
                    detailError = null
                    detailLoadingId = entry.sessionId
                    scope.launch {
                        runCatching {
                            ClexChainApi.fetchSessionDetail(entry.sessionId)
                        }.onSuccess { detail ->
                            selectedEntryDetail = detail
                        }.onFailure { error ->
                            detailError = error.message ?: "Could not load the session details."
                        }
                        detailLoadingId = null
                    }
                }
            )
            VaultBoundary(
                visible = screenVisible,
                baseDelay = 980L
            )
            Spacer(Modifier.height(120.dp))
        }
    }

    // Detail dialog for ledger entry
    selectedEntry?.let { entry ->
        LedgerEntryDetailDialog(
            entry = entry,
            detail = selectedEntryDetail?.takeIf { it.id == entry.sessionId },
            isLoading = detailLoadingId == entry.sessionId,
            error = detailError,
            onDismiss = {
                selectedEntry = null
                selectedEntryDetail = null
                detailError = null
                detailLoadingId = null
            }
        )
    }
}

@Composable
private fun ChainTopBar() {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(colors.bgPrimary)
            .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PageMark(glyph = "⌗", title = "CHAIN")
        TopBarStatusChip(text = "PUBLIC LEDGER", accentColor = colors.accent, showDot = true)
    }
}

// ── HERO ────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChainHero(
    stats: ChainStats?,
    visible: Boolean,
    baseDelay: Long
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPrimary)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(top = CxSpacing.xl, bottom = CxSpacing.xxl)
    ) {
        StableRevealFromBottom(visible = visible, delayMs = baseDelay) {
            NeonTag(text = "PUBLIC LEDGER", accent = colors.accent)
        }
        Spacer(Modifier.height(CxSpacing.md))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 70L) {
            HeroTitle(
                text = "TRANSFER\nCHAIN",
                fontSize = CxTypography.text5xl
            )
        }
        Spacer(Modifier.height(CxSpacing.md))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 140L) {
            BodyText(
                text = "Every Clex transfer is logged to a public, hash-chained ledger. No filenames. No file contents. Just anonymous session metadata.",
                fontSize = CxTypography.textSm
            )
        }
        Spacer(Modifier.height(CxSpacing.lg))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 210L) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)
            ) {
                listOf("APPEND-ONLY", "SHA-256", "NO PII", "CLOUDFLARE D1").forEach {
                    BrutalistBadge(text = it)
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.lg))

        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 280L) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.md)
            ) {
                ChainHeroStatCard(
                    label = "LIVE SESSIONS",
                    value = stats?.totalSessions ?: 0,
                    modifier = Modifier.weight(1f),
                    accent = colors.accent
                )
                ChainHeroStatCard(
                    label = "CHAINS",
                    value = stats?.totalChains ?: 0,
                    modifier = Modifier.weight(1f),
                    accent = CxColors.success
                )
            }
        }

        Spacer(Modifier.height(CxSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.xs)) {
            listOf(
                "No filenames stored",
                "No file contents stored",
                "No IP addresses logged",
                "Chain ID generated locally",
                "Publicly verifiable hashes"
            ).forEachIndexed { index, item ->
                StableRevealFromBottom(visible = visible, delayMs = baseDelay + 340L + index * 45L) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MonoText(text = "✓", color = colors.accent, fontSize = CxTypography.textSm)
                        Spacer(Modifier.width(CxSpacing.sm))
                        MonoText(
                            text = item.uppercase(),
                            fontSize = CxTypography.textXs,
                            color = colors.textTertiary,
                            letterSpacing = CxTypography.textXs * 0.1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainHeroStatCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    accent: Color
) {
    val colors = CxTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.76f else 0.92f))
            .border(1.dp, accent.copy(alpha = if (colors.isDark) 0.24f else 0.34f), RoundedCornerShape(20.dp))
            .padding(CxSpacing.md)
    ) {
        MonoText(
            text = label,
            fontSize = CxTypography.textXs,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(CxSpacing.sm))
        MonoText(
            text = value.toString(),
            fontSize = CxTypography.text4xl,
            fontWeight = CxTypography.weightBold,
            color = accent
        )
    }
}

// ── HOW CHAIN WORKS ─────────────────────────────

@Composable
private fun HowChainWorks(
    visible: Boolean,
    baseDelay: Long
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSecondary)
            .padding(horizontal = CxSpacing.screenHorizontal, vertical = CxSpacing.xl)
    ) {
        StableRevealFromBottom(visible = visible, delayMs = baseDelay) {
            MonoText(
                text = "HOW IT WORKS",
                fontSize = CxTypography.textXs,
                fontWeight = CxTypography.weightBold,
                color = colors.textTertiary,
                letterSpacing = CxTypography.textXs * 0.2
            )
        }
        Spacer(Modifier.height(CxSpacing.md))

        val steps = listOf(
            Triple("01", "LOCAL ID", "Random 32-hex chain ID generated in browser."),
            Triple("02", "SESSION LOG", "Ledger entry with route, types, SHA-256 hashes."),
            Triple("03", "HASH CHAIN", "Each record includes previous record hash."),
            Triple("04", "PUBLIC VIEW", "Anyone can browse the full ledger.")
        )

        // 2x2 grid — clean, no shadows
        Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)) {
            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
                ) {
                    for (colIndex in 0..1) {
                        val index = rowIndex * 2 + colIndex
                        val (num, title, desc) = steps[index]
                        StableRevealFromBottom(
                            visible = visible,
                            modifier = Modifier.weight(1f),
                            delayMs = baseDelay + 90L + index * 55L
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(colors.bgCard)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
                                    .padding(CxSpacing.md)
                            ) {
                                MonoText(
                                    text = num,
                                    fontSize = CxTypography.textXl,
                                    fontWeight = CxTypography.weightBold,
                                    color = colors.accent.copy(alpha = 0.45f)
                                )
                                Spacer(Modifier.height(CxSpacing.xs))
                                MonoText(
                                    text = title,
                                    fontSize = CxTypography.textXs,
                                    fontWeight = CxTypography.weightBold,
                                    color = colors.textPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                BodyText(text = desc, fontSize = CxTypography.textXs)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── PIPELINE BUILDER ────────────────────────────

@Composable
private fun PipelineBuilder(
    visible: Boolean,
    baseDelay: Long
) {
    val colors = CxTheme.colors
    val chainPresets = listOf(
        ChainPreset(
            title = "IMAGE → COMPRESS → CONVERT → SHARE",
            steps = listOf("DROP", "COMPRESS", "CONVERT", "SHARE")
        ),
        ChainPreset(
            title = "PDF → MERGE → COMPRESS → ZIP → SHARE",
            steps = listOf("DROP", "MERGE", "COMPRESS", "ZIP", "SHARE")
        ),
        ChainPreset(
            title = "DOCX → PDF → SPLIT → SHARE",
            steps = listOf("DROP", "PDF", "SPLIT", "SHARE")
        )
    )
    var selectedChainIndex by remember { mutableIntStateOf(0) }
    var selectedStepIndex by remember { mutableIntStateOf(0) }
    val selectedChain = chainPresets[selectedChainIndex]
    val steps = selectedChain.steps.mapIndexed { index, label ->
        PipelineStepUi(
            number = index + 1,
            title = label,
            description = label.toChainStepDescription(),
            status = when {
                index < selectedStepIndex -> StepStatus.COMPLETE
                index == selectedStepIndex -> StepStatus.ACTIVE
                else -> StepStatus.IDLE
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.xl)
    ) {
        StableRevealFromBottom(visible = visible, delayMs = baseDelay) {
            SectionLabel(text = "Tool Chaining")
        }
        Spacer(Modifier.height(CxSpacing.md))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 70L) {
            SectionTitle(text = "ONE THING LEADS\nTO THE NEXT")
        }
        Spacer(Modifier.height(CxSpacing.sm))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 130L) {
            BodyText(text = "Connect operations so your workflow flows naturally. No re-upload between tools.")
        }

        Spacer(Modifier.height(CxSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)) {
            steps.forEachIndexed { index, step ->
                StableRevealFromBottom(visible = visible, delayMs = baseDelay + 220L + index * 65L) {
                    ChainFlowCard(
                        step = step,
                        onClick = { selectedStepIndex = index }
                    )
                }
            }
        }

        Spacer(Modifier.height(CxSpacing.lg))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 540L) {
            MonoText(
                text = "POPULAR CHAINS",
                fontSize = CxTypography.textXs,
                fontWeight = CxTypography.weightBold,
                color = colors.textTertiary,
                letterSpacing = CxTypography.textXs * 0.2
            )
        }
        Spacer(Modifier.height(CxSpacing.sm))

        chainPresets.forEachIndexed { index, chain ->
            StableRevealFromBottom(visible = visible, delayMs = baseDelay + 610L + index * 55L) {
                PopularChainRow(
                    title = chain.title,
                    selected = index == selectedChainIndex,
                    onClick = {
                        selectedChainIndex = index
                        selectedStepIndex = 0
                    }
                )
            }
            if (index < chainPresets.lastIndex) {
                Spacer(Modifier.height(CxSpacing.xs))
            }
        }
    }
}

@Composable
private fun ChainFlowCard(
    step: PipelineStepUi,
    onClick: () -> Unit,
) {
    val colors = CxTheme.colors
    val accent = when (step.status) {
        StepStatus.COMPLETE -> CxColors.success
        StepStatus.ACTIVE -> colors.accent
        StepStatus.ERROR -> CxColors.error
        StepStatus.IDLE -> colors.textTertiary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (step.status == StepStatus.ACTIVE) colors.accent.copy(alpha = if (colors.isDark) 0.14f else 0.12f)
                else colors.bgCard.copy(alpha = if (colors.isDark) 0.76f else 0.92f)
            )
            .border(1.dp, if (step.status == StepStatus.ACTIVE) accent.copy(alpha = 0.65f) else colors.borderSubtle, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MonoText(
                text = String.format("%02d", step.number),
                fontSize = CxTypography.textXs,
                color = accent
            )
            MonoText(
                text = step.title,
                fontSize = CxTypography.textBase,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary
            )
            BodyText(
                text = step.description,
                fontSize = CxTypography.textXs,
                color = colors.textSecondary
            )
        }
        MonoText(
            text = when (step.status) {
                StepStatus.COMPLETE -> "READY"
                StepStatus.ACTIVE -> "ACTIVE"
                StepStatus.ERROR -> "ISSUE"
                StepStatus.IDLE -> "STEP ${step.number}"
            },
            fontSize = CxTypography.textXs,
            color = accent
        )
    }
}

@Composable
private fun PopularChainRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = CxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) colors.accent.copy(alpha = if (colors.isDark) 0.14f else 0.10f)
                else colors.bgCard.copy(alpha = if (colors.isDark) 0.76f else 0.92f)
            )
            .border(
                1.dp,
                if (selected) colors.accent.copy(alpha = 0.6f) else colors.borderSubtle,
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonoText(
            text = if (selected) "●" else "⟳",
            fontSize = CxTypography.textLg,
            color = if (selected) colors.accent else colors.textTertiary
        )
        Spacer(Modifier.width(CxSpacing.sm))
        MonoText(
            text = title,
            fontSize = CxTypography.textXs,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(CxSpacing.sm))
        MonoText(
            text = if (selected) "OPEN" else "VIEW",
            fontSize = CxTypography.textXs,
            color = colors.accent
        )
    }
}

private data class ChainPreset(
    val title: String,
    val steps: List<String>,
)

private data class PipelineStepUi(
    val number: Int,
    val title: String,
    val description: String,
    val status: StepStatus
)

private fun String.toChainStepDescription(): String {
    return when (uppercase()) {
        "DROP" -> "Bring files into the workspace without uploading them anywhere first."
        "COMPRESS" -> "Reduce payload size before sending so the transfer starts faster and finishes cleaner."
        "CONVERT" -> "Switch to a more efficient format before sharing or chaining into the next tool."
        "SHARE" -> "Open the final direct transfer route and hand off the processed files."
        "MERGE" -> "Combine multiple PDF documents into one output before any later compression or sharing."
        "ZIP" -> "Bundle multiple outputs into one archive so the receiver gets a single clean package."
        "PDF" -> "Convert the document into a portable PDF stage before splitting or sharing."
        "SPLIT" -> "Break the document into separate pages or smaller units for easier downstream routing."
        else -> "This step is part of the selected chain and can feed directly into the next action."
    }
}

// ── PUBLIC LEDGER ────────────────────────────────

@Composable
private fun PublicLedger(
    feedState: ChainFeedUiState,
    onRetry: () -> Unit,
    visible: Boolean,
    baseDelay: Long,
    onEntryClick: (LedgerEntry) -> Unit
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSecondary)
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.xl)
    ) {
        StableRevealFromBottom(visible = visible, delayMs = baseDelay) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(text = "LIVE LEDGER")
                MonoText(
                    text = "AUTO-REFRESH 30S",
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary
                )
            }
        }

        Spacer(Modifier.height(CxSpacing.lg))

        feedState.stats?.let { stats ->
            StableRevealFromBottom(visible = visible, delayMs = baseDelay + 80L) {
                LedgerStatsRow(stats = stats)
            }
            Spacer(Modifier.height(CxSpacing.md))
        }

        if (feedState.error != null) {
            StableRevealFromBottom(visible = visible, delayMs = baseDelay + 140L) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CxColors.warning.copy(alpha = 0.4f))
                        .background(colors.bgCard)
                        .padding(CxSpacing.md)
                ) {
                    MonoText(
                        text = "CHAIN API ISSUE",
                        fontSize = CxTypography.textSm,
                        fontWeight = CxTypography.weightBold,
                        color = CxColors.warning
                    )
                    Spacer(Modifier.height(CxSpacing.xs))
                    BodyText(text = feedState.error, fontSize = CxTypography.textXs)
                    Spacer(Modifier.height(CxSpacing.sm))
                    BrutalistButton(
                        text = "RETRY",
                        onClick = onRetry,
                        variant = ButtonVariant.SECONDARY,
                        size = ButtonSize.SMALL
                    )
                }
            }
            Spacer(Modifier.height(CxSpacing.md))
        }

        when {
            feedState.isLoading && feedState.entries.isEmpty() -> {
                StableRevealFromBottom(visible = visible, delayMs = baseDelay + 180L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderColor)
                            .background(colors.bgCard)
                            .padding(CxSpacing.md)
                    ) {
                        MonoText(
                            text = "LOADING LIVE LEDGER...",
                            fontSize = CxTypography.textSm,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        BrutalistProgressBar(
                            progress = 0.55f,
                            accentColor = colors.accent,
                            showLabel = false
                        )
                    }
                }
            }

            feedState.entries.isEmpty() -> {
                StableRevealFromBottom(visible = visible, delayMs = baseDelay + 180L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderColor)
                            .background(colors.bgCard)
                            .padding(CxSpacing.md)
                    ) {
                        MonoText(
                            text = "NO LIVE SESSIONS YET",
                            fontSize = CxTypography.textSm,
                            fontWeight = CxTypography.weightBold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(CxSpacing.xs))
                        BodyText(
                            text = "The chain worker is reachable but has not returned any transfer sessions yet.",
                            fontSize = CxTypography.textXs
                        )
                    }
                }
            }

            else -> {
                var expanded by remember { mutableStateOf(false) }
                val displayedEntries = if (expanded) feedState.entries else feedState.entries.take(6)

                displayedEntries.forEachIndexed { index, entry ->
                    StableRevealFromBottom(visible = visible, delayMs = baseDelay + 180L + index * 55L) {
                        LedgerRow(entry, onClick = { onEntryClick(entry) })
                    }
                    if (index < displayedEntries.lastIndex) {
                        Spacer(Modifier.height(CxSpacing.xs))
                    }
                }

                if (feedState.entries.size > 6) {
                    Spacer(Modifier.height(CxSpacing.md))
                    StableRevealFromBottom(visible = visible, delayMs = baseDelay + 520L) {
                        LedgerExpandButton(
                            text = if (expanded) "SHOW LESS" else "SHOW ALL ${feedState.entries.size} SESSIONS",
                            onClick = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerExpandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.accent.copy(alpha = if (colors.isDark) 0.12f else 0.10f))
            .border(1.dp, colors.accent.copy(alpha = 0.42f), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        MonoText(
            text = text,
            fontSize = CxTypography.textSm,
            fontWeight = CxTypography.weightBold,
            color = colors.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LedgerStatsRow(stats: ChainStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        LedgerStatCard(
            label = "SESSIONS",
            value = stats.totalSessions.toString(),
            modifier = Modifier.weight(1f)
        )
        LedgerStatCard(
            label = "CHAINS",
            value = stats.totalChains.toString(),
            modifier = Modifier.weight(1f)
        )
        LedgerStatCard(
            label = "DONE",
            value = stats.completedSessions.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LedgerStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = CxTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
            .padding(CxSpacing.md)
    ) {
        MonoText(
            text = label,
            fontSize = CxTypography.textXs,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(CxSpacing.xs))
        MonoText(
            text = value,
            fontSize = CxTypography.text2xl,
            fontWeight = CxTypography.weightBold,
            color = colors.accent
        )
    }
}

data class LedgerEntry(
    val sessionId: String,
    val hash: String,
    val route: String,
    val duration: String,
    val files: String,
    val status: String,
    val statusColor: Color
)

private data class ChainFeedUiState(
    val isLoading: Boolean,
    val stats: ChainStats? = null,
    val entries: List<LedgerEntry> = emptyList(),
    val error: String? = null,
    val lastUpdatedAt: Long? = null
)

private fun ChainSession.toLedgerEntry(): LedgerEntry {
    return LedgerEntry(
        sessionId = id,
        hash = recordHash.take(8).ifBlank { id.take(8) },
        route = route.toRouteLabel(),
        duration = durationMs.toDurationLabel(status),
        files = fileCount.toFilesLabel(),
        status = status.toStatusLabel(),
        statusColor = status.toStatusColor()
    )
}

private fun String.toRouteLabel(): String {
    return when (lowercase()) {
        "webrtc" -> "P2P DIRECT"
        "local" -> "LOCAL NETWORK"
        "drive" -> "GOOGLE DRIVE"
        else -> uppercase()
    }
}

private fun Long?.toDurationLabel(status: String): String {
    if (this == null || this <= 0L) {
        return when (status.lowercase()) {
            "completed" -> "0.0s"
            "failed", "cancelled", "abandoned" -> "FAILED"
            else -> "IN FLIGHT"
        }
    }

    val seconds = this / 1000f
    return String.format("%.1fs", seconds)
}

private fun Int.toFilesLabel(): String {
    return if (this == 1) "1 FILE" else "$this FILES"
}

private fun String.toStatusLabel(): String {
    return when (lowercase()) {
        "registered" -> "REGISTERED"
        "waiting_peer" -> "WAITING"
        "connecting" -> "CONNECTING"
        "transferring" -> "TRANSFERRING"
        "completed" -> "SUCCESS"
        "failed" -> "FAILED"
        "cancelled" -> "CANCELLED"
        "abandoned" -> "ABANDONED"
        else -> uppercase()
    }
}

private fun String.toStatusColor(): Color {
    return when (lowercase()) {
        "completed" -> CxColors.success
        "failed", "cancelled", "abandoned" -> CxColors.error
        "waiting_peer", "connecting", "transferring", "registered" -> CxColors.warning
        else -> CxColors.warning
    }
}

private data class ChainFileDetail(
    val category: String,
    val type: String,
    val size: Long,
    val hash: String?,
)

private fun JSONArray.toChainFileDetails(): List<ChainFileDetail> {
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                ChainFileDetail(
                    category = item.optString("category").ifBlank { "other" },
                    type = item.optString("type").ifBlank { "application/octet-stream" },
                    size = item.optLong("size", 0L),
                    hash = item.optString("hash").takeIf { it.isNotBlank() },
                )
            )
        }
    }
}

private fun formatLedgerBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    return if (unitIndex == 0) {
        "${value.toLong()} ${units[unitIndex]}"
    } else {
        String.format("%.1f %s", value, units[unitIndex])
    }
}

@Composable
private fun LedgerRow(entry: LedgerEntry, onClick: () -> Unit = {}) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = CxSpacing.md, vertical = CxSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoText(
                text = "#${entry.hash}",
                fontSize = CxTypography.textXs,
                color = colors.accent,
                letterSpacing = CxTypography.textXs * 0.05
            )
            BrutalistBadge(
                text = entry.status,
                accentColor = entry.statusColor,
                showDot = entry.status == "TRANSFERRING" || entry.status == "CONNECTING"
            )
        }
        Spacer(Modifier.height(CxSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MonoText(text = entry.route, fontSize = CxTypography.textXs, color = colors.textSecondary)
            MonoText(text = "${entry.duration}  ·  ${entry.files}", fontSize = CxTypography.textXs, color = colors.textTertiary)
        }
    }
}

@Composable
private fun LedgerEntryDetailDialog(
    entry: LedgerEntry,
    detail: ChainSessionDetail?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
) {
    val colors = CxTheme.colors
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(CxBorders.thin, colors.borderColor)
                .background(colors.bgPrimary)
                .padding(CxSpacing.cardPadding)
        ) {
            MonoText(
                text = "SESSION #${entry.hash}",
                fontSize = CxTypography.textLg,
                fontWeight = CxTypography.weightBold,
                color = colors.accent
            )
            Spacer(Modifier.height(CxSpacing.lg))

            val baseRows = listOf(
                "STATUS" to entry.status,
                "ROUTE" to entry.route,
                "DURATION" to entry.duration,
                "FILES" to entry.files,
                "HASH" to entry.hash
            )

            baseRows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CxSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoText(text = label, fontSize = CxTypography.textXs, color = colors.textTertiary)
                    MonoText(
                        text = value,
                        fontSize = CxTypography.textXs,
                        fontWeight = CxTypography.weightBold,
                        color = if (label == "STATUS") entry.statusColor else colors.textPrimary
                    )
                }
            }

            when {
                isLoading -> {
                    Spacer(Modifier.height(CxSpacing.md))
                    BrutalistProgressBar(progress = 0.4f, accentColor = colors.accent, showLabel = false)
                }

                error != null -> {
                    Spacer(Modifier.height(CxSpacing.md))
                    BodyText(text = error, fontSize = CxTypography.textXs, color = CxColors.warning)
                }

                detail != null -> {
                    Spacer(Modifier.height(CxSpacing.md))
                    listOf(
                        "SENDER" to detail.senderChainId.takeLast(8),
                        "RECEIVER" to (detail.receiverChainId?.takeLast(8) ?: "PENDING"),
                        "LEDGER INDEX" to detail.ledgerIndex.toString(),
                        "EVENTS" to detail.events.size.toString(),
                    ).forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = CxSpacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MonoText(text = label, fontSize = CxTypography.textXs, color = colors.textTertiary)
                            MonoText(text = value, fontSize = CxTypography.textXs, color = colors.textSecondary)
                        }
                    }

                    val parsedFiles = detail.files.toChainFileDetails()
                    if (parsedFiles.isNotEmpty()) {
                        Spacer(Modifier.height(CxSpacing.md))
                        MonoText(
                            text = "FILES",
                            fontSize = CxTypography.textXs,
                            fontWeight = CxTypography.weightBold,
                            color = colors.accent
                        )
                        Spacer(Modifier.height(CxSpacing.sm))
                        parsedFiles.forEachIndexed { index, file ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.bgCard)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                                    .padding(CxSpacing.md)
                            ) {
                                MonoText(
                                    text = file.category.uppercase(),
                                    fontSize = CxTypography.textXs,
                                    fontWeight = CxTypography.weightBold,
                                    color = colors.accent
                                )
                                Spacer(Modifier.height(CxSpacing.xs))
                                BodyText(
                                    text = "${file.type}  ·  ${formatLedgerBytes(file.size)}",
                                    fontSize = CxTypography.textXs
                                )
                                Spacer(Modifier.height(CxSpacing.xs))
                                MonoText(
                                    text = file.hash?.take(12) ?: "HASH PENDING",
                                    fontSize = CxTypography.textXs,
                                    color = if (file.hash == null) colors.textTertiary else colors.textSecondary
                                )
                            }
                            if (index < parsedFiles.lastIndex) {
                                Spacer(Modifier.height(CxSpacing.sm))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(CxSpacing.lg))
            BrutalistButton(
                text = "CLOSE",
                onClick = onDismiss,
                variant = ButtonVariant.SECONDARY,
                size = ButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── VAULT BOUNDARY ──────────────────────────────

@Composable
private fun VaultBoundary(
    visible: Boolean,
    baseDelay: Long
) {
    val colors = CxTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CxSpacing.screenHorizontal)
            .padding(vertical = CxSpacing.xl)
    ) {
        StableRevealFromBottom(visible = visible, delayMs = baseDelay) {
            SectionLabel(text = "Vault Boundary")
        }
        Spacer(Modifier.height(CxSpacing.md))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 70L) {
            SectionTitle(text = "VAULT STAYS PRIVATE")
        }
        Spacer(Modifier.height(CxSpacing.sm))
        StableRevealFromBottom(visible = visible, delayMs = baseDelay + 130L) {
            BodyText(text = "The public Transfer Chain is for workspace transfer metadata only. Vault notes, secret links, and timed Drive-share files stay outside the public ledger.")
        }

        Spacer(Modifier.height(CxSpacing.lg))

        listOf(
            "NOT ON CHAIN" to "Encrypted notes, secret-link content, and timed Drive-share files stay outside the ledger.",
            "WHAT CHAIN RECORDS" to "Anonymous transfer-session metadata: route choice, timing, file-class info, hash-linked status.",
            "WHY THE SPLIT" to "Vault is for private drafts and secrets. Chain is for public verifiability."
        ).forEachIndexed { index, (title, desc) ->
            StableRevealFromBottom(visible = visible, delayMs = baseDelay + 210L + index * 60L) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.bgCard)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
                        .padding(CxSpacing.md)
                ) {
                    MonoText(
                        text = title,
                        fontSize = CxTypography.textSm,
                        fontWeight = CxTypography.weightBold,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(CxSpacing.xs))
                    BodyText(text = desc, fontSize = CxTypography.textXs)
                }
            }
            if (index < 2) Spacer(Modifier.height(CxSpacing.sm))
        }
    }
}
