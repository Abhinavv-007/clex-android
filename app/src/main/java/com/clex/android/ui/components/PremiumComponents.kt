package com.clex.android.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.elasticPress
import com.clex.android.ui.anim.idleBreathe
import com.clex.android.ui.anim.rememberCountUp
import com.clex.android.ui.theme.CxBorders
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxPremium
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import kotlin.math.PI
import kotlin.math.sin

// ═══════════════════════════════════════════════════
//  CLEX — Premium Components
//  Glass cards, glow buttons, live stat tiles,
//  ticker strip, count-up counters, mini visualizers
// ═══════════════════════════════════════════════════

// ── GLASS CARD ────────────────────────────────────
// Frosted translucent card with neon stroke + hard
// offset shadow. Core premium surface.

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    cornerRadius: Dp = CxRadius.md,
    borderWidth: Dp = CxBorders.thin,
    showShadow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = CxTheme.colors
    Box(
        modifier = modifier
            .then(
                if (showShadow) Modifier.drawBehind {
                    val offset = 6.dp.toPx()
                    drawRect(
                        color = colors.shadowColor.copy(alpha = 0.85f),
                        topLeft = Offset(offset, offset),
                        size = size
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CxPremium.glassStrongTint,
                        CxPremium.glassLightTint
                    )
                )
            )
            .background(colors.bgCard.copy(alpha = 0.72f))
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.6f),
                        colors.borderBold.copy(alpha = 0.25f),
                        accent.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(CxSpacing.cardPadding)
    ) {
        content()
    }
}

// ── GLOW BUTTON ───────────────────────────────────
// Primary CTA with outer glow halo + inner gradient.
// Use for hero "Get Started", "Connect", etc.

@Composable
fun GlowButton(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    onClick: () -> Unit = {}
) {
    val colors = CxTheme.colors
    val transition = rememberInfiniteTransition(label = "glowBtn")
    val glowPulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(
        modifier = modifier
            .padding(10.dp)
            .drawBehind {
                val pad = 14.dp.toPx() * glowPulse
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.55f * glowPulse),
                            accent.copy(alpha = 0f)
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = (size.width + pad * 2f) / 1.4f
                    ),
                    topLeft = Offset(-pad, -pad),
                    size = Size(size.width + pad * 2f, size.height + pad * 2f),
                    blendMode = BlendMode.Plus
                )
            }
            .clip(RoundedCornerShape(CxRadius.sm))
            .background(
                Brush.linearGradient(
                    colors = listOf(accent, accent.copy(alpha = 0.78f))
                )
            )
            .border(
                width = CxBorders.medium,
                color = colors.borderBold,
                shape = RoundedCornerShape(CxRadius.sm)
            )
            .elasticPress(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        MonoText(
            text = text.uppercase(),
            fontSize = CxTypography.textBase,
            color = CxColors.pureBlack,
            fontWeight = CxTypography.weightBlack
        )
    }
}

// ── LIVE STAT TILE ────────────────────────────────
// Animated stat card: big count-up number + label +
// pulsing indicator. Used across Home + Chain.

@Composable
fun LiveStatTile(
    label: String,
    value: Int,
    unit: String = "",
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    live: Boolean = true,
    delayFrac: Float = 0f
) {
    val colors = CxTheme.colors
    val count = rememberCountUp(target = value, durationMs = 1400, startDelayMs = 300)

    GlassCard(
        modifier = modifier.idleBreathe(delayFrac = delayFrac),
        accent = accent
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
            ) {
                if (live) {
                    StatusDot(color = accent, pulsing = true, size = 8.dp)
                    MonoText(
                        text = "LIVE",
                        fontSize = CxTypography.textXs,
                        color = accent
                    )
                }
                MonoText(
                    text = label.uppercase(),
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HeroTitle(
                    text = count.toString(),
                    fontSize = CxTypography.text4xl,
                    color = colors.textPrimary
                )
                if (unit.isNotEmpty()) {
                    MonoText(
                        text = unit,
                        fontSize = CxTypography.textSm,
                        color = accent,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

// ── GLOW DIVIDER ──────────────────────────────────
// Horizontal gradient line — use for section breaks.

@Composable
fun GlowDivider(
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    height: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.8f),
                        accent,
                        accent.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    )
}

// ── MINI WAVEFORM TILE ────────────────────────────
// Tiny live-bars sparkline — simulates audio/activity.

@Composable
fun MiniWaveform(
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    bars: Int = 18
) {
    val transition = rememberInfiniteTransition(label = "miniWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing)
        ),
        label = "wavePhase"
    )
    Canvas(modifier = modifier.height(32.dp).fillMaxWidth()) {
        val barW = size.width / (bars * 2f)
        for (i in 0 until bars) {
            val t = phase + i * 0.4f
            val mag = (sin(t) * 0.5f + 0.5f) * 0.9f + 0.1f
            val x = i * (barW * 2f) + barW * 0.5f
            val h = size.height * mag
            drawRect(
                color = accent.copy(alpha = 0.5f + mag * 0.5f),
                topLeft = Offset(x, (size.height - h) / 2f),
                size = Size(barW, h)
            )
        }
    }
}

// ── TICKER STRIP ──────────────────────────────────
// Horizontal label chip used inside InfiniteMarquee.

@Composable
fun TickerChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier.padding(horizontal = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(accent)
        )
        MonoText(
            text = text.uppercase(),
            fontSize = CxTypography.textSm,
            color = colors.textSecondary,
            fontWeight = CxTypography.weightSemibold
        )
        MonoText(
            text = "//",
            fontSize = CxTypography.textSm,
            color = colors.textTertiary
        )
    }
}

// ── CIRCULAR PROGRESS RING ────────────────────────
// Arc ring with center label — used for transfer
// progress, secret strength, etc.

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    accent: Color = CxTheme.colors.accent,
    trackColor: Color = CxTheme.colors.borderSubtle,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    val animProgress = progress.coerceIn(0f, 1f)
    val transition = rememberInfiniteTransition(label = "ringGlow")
    val glow by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringGlowPulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        accent.copy(alpha = 0.4f),
                        accent,
                        accent.copy(alpha = 0.9f * glow)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * animProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            // Glow rim
            drawArc(
                color = accent.copy(alpha = 0.35f * glow),
                startAngle = -90f,
                sweepAngle = 360f * animProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke * 1.8f),
                blendMode = BlendMode.Plus
            )
        }
        centerContent()
    }
}

// ── ICON BADGE ────────────────────────────────────
// Large square badge with glow backdrop — used for
// feature-grid tiles.

@Composable
fun IconBadge(
    label: String,
    symbol: String,
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    size: Dp = 72.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                ),
                blendMode = BlendMode.Plus
            )
        }
        // Inner badge
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .background(CxTheme.colors.bgElevated)
                .border(CxBorders.medium, CxTheme.colors.borderBold),
            contentAlignment = Alignment.Center
        ) {
            MonoText(
                text = symbol,
                fontSize = CxTypography.text2xl,
                color = accent,
                fontWeight = CxTypography.weightBlack
            )
        }
    }
}

// ── NEON TAG ──────────────────────────────────────
// Small glowing chip used for labels/tags/statuses.

@Composable
fun NeonTag(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent,
    solid: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CxRadius.full))
            .background(if (solid) accent else accent.copy(alpha = 0.12f))
            .border(
                1.dp,
                accent.copy(alpha = if (solid) 1f else 0.6f),
                RoundedCornerShape(CxRadius.full)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        MonoText(
            text = text.uppercase(),
            fontSize = CxTypography.textXs,
            color = if (solid) CxColors.pureBlack else accent,
            fontWeight = CxTypography.weightBold
        )
    }
}

// ── HERO COUNTER ──────────────────────────────────
// Large count-up stat used on landing + about pages.

@Composable
fun HeroCounter(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    accent: Color = CxTheme.colors.accent
) {
    val animated = rememberCountUp(value, durationMs = 2000, startDelayMs = 400)
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (prefix.isNotEmpty()) {
                HeroTitle(
                    text = prefix,
                    fontSize = CxTypography.text4xl,
                    color = accent
                )
            }
            HeroTitle(
                text = animated.toString(),
                fontSize = CxTypography.text6xl,
                color = CxTheme.colors.textPrimary
            )
            if (suffix.isNotEmpty()) {
                HeroTitle(
                    text = suffix,
                    fontSize = CxTypography.text4xl,
                    color = accent
                )
            }
        }
        MonoText(
            text = label.uppercase(),
            fontSize = CxTypography.textXs,
            color = CxTheme.colors.textTertiary
        )
    }
}

// ── GRADIENT DIVIDER LINE (diagonal) ──────────────
// Angled accent stripe for section headers.

@Composable
fun DiagonalAccent(
    modifier: Modifier = Modifier,
    accent: Color = CxTheme.colors.accent
) {
    Canvas(
        modifier = modifier
            .height(4.dp)
            .fillMaxWidth()
    ) {
        val h = size.height
        val w = size.width
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.3f, 0f)
            lineTo(w, 0f)
            lineTo(w * 0.7f, h)
            close()
        }
        drawPath(path = path, brush = Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.3f))))
    }
}
