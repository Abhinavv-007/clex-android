package com.clex.android.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════
//  Coachmark — first-launch tutorial overlay.
//  Liquid glass tooltip on dim scrim. Persisted in
//  shared prefs so each tip shows at most once.
// ═══════════════════════════════════════════════════

object CoachmarkPrefs {
    private const val FILE = "clex_coachmarks"
    fun seen(ctx: Context, id: String): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(id, false)
    fun mark(ctx: Context, id: String) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(id, true).apply()
    }
    fun resetAll(ctx: Context) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

data class CoachmarkStep(
    val id: String,
    val kicker: String,
    val title: String,
    val body: String,
    val cta: String = "Got it",
)

@Composable
fun CoachmarkOverlay(
    steps: List<CoachmarkStep>,
    modifier: Modifier = Modifier,
    showDelayMs: Long = 600,
) {
    val ctx = LocalContext.current
    val unseen = remember { steps.filter { !CoachmarkPrefs.seen(ctx, it.id) } }
    if (unseen.isEmpty()) return

    var index by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        delay(showDelayMs)
        index = 0
    }

    val visible = index in unseen.indices
    val current = unseen.getOrNull(index)

    AnimatedVisibility(
        visible = visible && current != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = { /* swallow */ },
                ),
            contentAlignment = Alignment.Center,
        ) {
            current?.let { step ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.95f),
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp),
                        cornerRadius = CxRadius.lg,
                        padding = 26.dp,
                    ) {
                        Column {
                            KickerChip(text = step.kicker)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = step.title,
                                fontSize = CxTypography.text2xl,
                                fontFamily = CxTypography.fontDisplay,
                                fontWeight = CxTypography.weightExtrabold,
                                color = CxTheme.colors.textPrimary,
                                lineHeight = CxTypography.text2xl * 1.15,
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = step.body,
                                fontSize = CxTypography.textBase,
                                fontFamily = CxTypography.fontBody,
                                fontWeight = CxTypography.weightMedium,
                                color = CxTheme.colors.textSecondary,
                                lineHeight = CxTypography.textBase * 1.55,
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${index + 1} / ${unseen.size}",
                                    fontSize = CxTypography.textXs,
                                    fontFamily = CxTypography.fontDisplay,
                                    fontWeight = CxTypography.weightExtrabold,
                                    color = CxTheme.colors.textTertiary,
                                    letterSpacing = CxTypography.textXs * 0.18,
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(CxColors.lavender, CxColors.peach2)
                                            )
                                        )
                                        .clickable {
                                            CoachmarkPrefs.mark(ctx, step.id)
                                            index = if (index < unseen.lastIndex) index + 1 else -1
                                        }
                                        .padding(horizontal = 22.dp, vertical = 12.dp),
                                ) {
                                    Text(
                                        text = if (index < unseen.lastIndex) "Next" else step.cta,
                                        fontSize = CxTypography.textSm,
                                        fontFamily = CxTypography.fontDisplay,
                                        fontWeight = CxTypography.weightExtrabold,
                                        color = CxColors.ink,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val DefaultCoachmarks = listOf(
    CoachmarkStep(
        id = "v1100_welcome",
        kicker = "welcome",
        title = "You're in the new Clex",
        body = "Liquid glass everywhere. Mesh gradients drift behind every screen, fonts and accents match clex.in pixel for pixel.",
    ),
    CoachmarkStep(
        id = "v1100_nav",
        kicker = "navigate",
        title = "Floating pill nav",
        body = "Bottom dock floats free instead of sticking to the edge. Tap any tab — Home, Vault, Chain, Settings.",
    ),
    CoachmarkStep(
        id = "v1100_workspace",
        kicker = "drop",
        title = "Workspace is the engine",
        body = "Drop a file, prepare it, then ship it through whichever route lands fastest. Direct, local, or cloud — Clex picks.",
    ),
    CoachmarkStep(
        id = "v1100_vault",
        kicker = "vault",
        title = "Hide secrets locally",
        body = "Notes, keys and recovery codes get encrypted on-device before anything else happens. The cipher never leaves your phone.",
    ),
)
