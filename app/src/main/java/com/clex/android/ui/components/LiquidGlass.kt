package com.clex.android.ui.components

import android.os.Build
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxGlass
import com.clex.android.ui.theme.CxRadius
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════
//  LIQUID GLASS — iOS 18 / visionOS-inspired surfaces
//  Translucent glass with frost tint, inner specular
//  highlight, outer ink ring, drop shadow.
// ═══════════════════════════════════════════════════

// ── ANIMATED MESH GRADIENT BACKGROUND ──────────────
// Slow-drifting pastel mesh that sits behind glass cards.
// Lavender, peach, mint, blue, pink, yellow blobs orbiting
// a center, blended with ambient fog for depth.

@Composable
fun LiquidMeshBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
) {
    val colors = CxTheme.colors
    val transition = rememberInfiniteTransition(label = "mesh")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(28_000, easing = LinearEasing)
        ),
        label = "meshPhase"
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(42_000, easing = LinearEasing)
        ),
        label = "meshDrift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base wash
        drawRect(
            brush = Brush.verticalGradient(
                if (colors.isDark) listOf(
                    Color(0xFF0A0A12),
                    Color(0xFF14121D),
                    Color(0xFF0A0A12),
                ) else listOf(
                    CxColors.cream,
                    CxColors.creamSoft,
                    CxColors.cream,
                )
            ),
            size = Size(w, h),
        )

        // Floating blobs — each orbits a different center
        data class Blob(val color: Color, val cx: Float, val cy: Float, val r: Float, val ph: Float)
        val baseAlpha = if (colors.isDark) 0.42f else 0.55f
        val blobs = listOf(
            Blob(CxGlass.meshLavender.copy(alpha = baseAlpha * intensity), 0.18f, 0.22f, 0.55f, 0f),
            Blob(CxGlass.meshPeach.copy(alpha = baseAlpha * intensity), 0.82f, 0.18f, 0.5f, 1.4f),
            Blob(CxGlass.meshMint.copy(alpha = (baseAlpha - 0.05f) * intensity), 0.78f, 0.78f, 0.6f, 2.7f),
            Blob(CxGlass.meshBlue.copy(alpha = (baseAlpha - 0.08f) * intensity), 0.22f, 0.82f, 0.5f, 4.1f),
            Blob(CxGlass.meshPink.copy(alpha = (baseAlpha - 0.12f) * intensity), 0.5f, 0.5f, 0.45f, 5.2f),
        )

        blobs.forEach { b ->
            val orbitR = 0.08f
            val cx = (b.cx + cos(phase + b.ph) * orbitR) * w
            val cy = (b.cy + sin(phase + b.ph) * orbitR + sin(drift + b.ph) * 0.04f) * h
            val radius = b.r * w * 0.9f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(b.color, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                center = Offset(cx, cy),
                radius = radius,
            )
        }
    }
}

// ── LIQUID GLASS SURFACE ────────────────────────────
// Frosted translucent panel. Use as the canonical card
// across the app. RenderEffect blur on API 31+ for the
// iOS-quality glass; gradient fallback below.

fun Modifier.liquidGlass(
    cornerRadius: Dp = CxRadius.lg,
    frostAlpha: Float = 1f,
    isDark: Boolean,
    ringStroke: Dp = CxGlass.ringMed,
    showShadow: Boolean = true,
): Modifier = this
    .then(
        if (showShadow) Modifier.drawBehind {
            val r = cornerRadius.toPx()
            drawRoundRect(
                color = if (isDark) CxGlass.dropShadowDark else CxGlass.dropShadowLight,
                topLeft = Offset(0f, 8.dp.toPx()),
                size = size,
                cornerRadius = CornerRadius(r, r),
            )
        } else Modifier
    )
    .clip(RoundedCornerShape(cornerRadius))
    // Body fill — frost tint over whatever's behind. Without RenderEffect
    // it's a flat translucent fill; with it, the content beneath blurs.
    .background(
        if (isDark) CxGlass.frostDark.copy(alpha = CxGlass.frostDark.alpha * frostAlpha)
        else CxGlass.frostLight.copy(alpha = CxGlass.frostLight.alpha * frostAlpha)
    )
    // Inner specular sweep — top-left highlight, bottom-right shadow
    .drawBehind {
        val r = cornerRadius.toPx()
        // Top edge highlight
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    if (isDark) CxGlass.innerHighlightDark else CxGlass.innerHighlightLight,
                    Color.Transparent,
                ),
                start = Offset(0f, 0f),
                end = Offset(0f, size.height * 0.45f),
            ),
            cornerRadius = CornerRadius(r, r),
        )
        // Bottom inner shadow
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    if (isDark) CxGlass.innerShadowDark else CxGlass.innerShadowLight,
                ),
                start = Offset(0f, size.height * 0.55f),
                end = Offset(0f, size.height),
            ),
            cornerRadius = CornerRadius(r, r),
        )
    }
    .border(
        width = ringStroke,
        brush = Brush.linearGradient(
            colors = if (isDark) listOf(
                Color(0x80FFFFFF),
                Color(0x33FFFFFF),
                Color(0x14FFFFFF),
            ) else listOf(
                Color(0xFFFFFFFF),
                Color(0xFFE3D9C0),
                CxColors.ink.copy(alpha = 0.18f),
            )
        ),
        shape = RoundedCornerShape(cornerRadius),
    )

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CxRadius.lg,
    padding: Dp = CxSpacing.cardPadding,
    showShadow: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = CxTheme.colors
    Box(
        modifier = modifier
            .liquidGlass(
                cornerRadius = cornerRadius,
                isDark = colors.isDark,
                showShadow = showShadow,
            )
            .padding(padding),
        content = content,
    )
}

// ── KICKER CHIP ─────────────────────────────────────
// Cream/dark capsule with lavender→peach gradient dot
// + Geist ExtraBold uppercase label. Same as web.

@Composable
fun KickerChip(
    text: String,
    modifier: Modifier = Modifier,
    dotColors: List<Color> = listOf(CxColors.lavender, CxColors.peach2),
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (colors.isDark) CxGlass.frostDarkSoft else CxColors.creamSoft
            )
            .border(
                1.5.dp,
                if (colors.isDark) Color(0x66FFFFFF) else CxColors.ink,
                RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    Brush.linearGradient(dotColors),
                    shape = CircleShape,
                )
        )
        Text(
            text = text.uppercase(),
            fontSize = CxTypography.textXs,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = CxTypography.weightExtrabold,
            letterSpacing = CxTypography.textXs * 0.14,
            color = if (colors.isDark) colors.textPrimary else CxColors.ink,
        )
    }
}

// ── CURSIVE ACCENT WORD ─────────────────────────────
// Pacifico, brand gradient brush. Drop into headlines.

@Composable
fun CursiveAccent(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = CxTypography.text4xl,
) {
    val colors = CxTheme.colors
    val gradient = Brush.linearGradient(
        colors = if (colors.isDark) listOf(
            CxColors.cursiveStartDark,
            CxColors.cursiveMidDark,
            CxColors.cursiveEndDark,
        ) else listOf(
            CxColors.cursiveStart,
            CxColors.cursiveMid1,
            CxColors.cursiveMid2,
            CxColors.cursiveEnd,
        )
    )
    Text(
        text = text,
        fontSize = fontSize,
        fontFamily = CxTypography.fontCursive,
        fontWeight = CxTypography.weightRegular,
        textAlign = TextAlign.Center,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            brush = gradient,
        )
    )
}

// ── GRADIENT PILL CTA ───────────────────────────────
// Lavender→peach2→mint pill, ink stroke, hard offset shadow.

@Composable
fun LiquidPillButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val colors = CxTheme.colors
    Box(
        modifier = modifier
            .drawBehind {
                val r = 999f
                drawRoundRect(
                    color = if (colors.isDark) Color(0xCC000000) else CxColors.ink,
                    topLeft = Offset(0f, 6.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(r, r),
                )
            }
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CxColors.lavender,
                        CxColors.peach2,
                        CxColors.mint,
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = CxColors.ink,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = CxTypography.textBase,
            color = CxColors.ink,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = CxTypography.weightExtrabold,
            letterSpacing = CxTypography.textBase * -0.01,
        )
    }
}

// ── BENTO TILE ──────────────────────────────────────
// Square-ish glass tile with kicker, big stat, hint.

@Composable
fun BentoTile(
    kicker: String,
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
    accentColors: List<Color> = listOf(CxColors.lavender, CxColors.peach2),
    onClick: (() -> Unit)? = null,
) {
    LiquidGlassCard(
        modifier = modifier
            .let { if (onClick != null) it.then(Modifier.background(Color.Transparent)) else it },
        cornerRadius = CxRadius.lg,
        padding = 20.dp,
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            KickerChip(text = kicker, dotColors = accentColors)
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = CxTypography.text2xl,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = CxTypography.weightExtrabold,
                color = CxTheme.colors.textPrimary,
                lineHeight = CxTypography.text2xl * 1.15,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = hint,
                fontSize = CxTypography.textSm,
                fontFamily = CxTypography.fontBody,
                fontWeight = CxTypography.weightMedium,
                color = CxTheme.colors.textTertiary,
                lineHeight = CxTypography.textSm * 1.4,
            )
        }
    }
}

// ── FLOATING PILL NAV ───────────────────────────────
// Free-floating bottom bar matching the iOS reference:
// 999dp pill, large blur, active tab gets bright icon + label.

@Composable
fun LiquidPillNavBar(
    items: List<NavTab>,
    selectedRoute: String?,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (colors.isDark) CxGlass.frostDark
                else CxGlass.frostLight
            )
            .drawBehind {
                // Top-edge specular
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            if (colors.isDark) Color(0x40FFFFFF) else Color(0xFFFFFFFF),
                            Color.Transparent,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height * 0.55f),
                    ),
                    cornerRadius = CornerRadius(999f, 999f),
                )
            }
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = if (colors.isDark) listOf(
                        Color(0x99FFFFFF),
                        Color(0x33FFFFFF),
                    ) else listOf(
                        Color(0xFFFFFFFF),
                        CxColors.ink.copy(alpha = 0.22f),
                    )
                ),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { tab ->
            val selected = tab.route == selectedRoute
            LiquidNavTab(
                tab = tab,
                selected = selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

data class NavTab(
    val route: String,
    val label: String,
    val icon: String,
)

@Composable
private fun LiquidNavTab(
    tab: NavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CxTheme.colors
    val activeColor = if (colors.isDark) Color(0xFFB5DCFF) else Color(0xFF15A3D8)
    val idleColor = colors.textTertiary
    val interaction = androidx.compose.runtime.remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(
                if (selected) Modifier.background(
                    if (colors.isDark) Color(0x33FFFFFF) else Color(0xCCFFFFFF)
                ) else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = tab.icon,
            fontSize = CxTypography.textXl,
            color = if (selected) activeColor else idleColor,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontSize = CxTypography.textXs,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (selected) activeColor else idleColor,
            letterSpacing = CxTypography.textXs * 0.04,
        )
    }
}

// Bridge for clickable inside Liquid components — internal helper removed

// ── MASCOT BADGE ────────────────────────────────────
// Cute floating creature with idle bob, used in code
// blocks and empty states.

@Composable
fun MascotBadge(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    label: String? = "hi · I'm Clex",
) {
    val transition = rememberInfiniteTransition(label = "mascot")
    val bob by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val blink by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "blink",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { translationY = bob.dp.toPx() },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val cx = w / 2f
                val cy = h / 2f

                // Soft halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CxColors.lavender.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = w * 0.7f,
                    ),
                    center = Offset(cx, cy),
                    radius = w * 0.7f,
                )
                // Body
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(CxColors.peach, CxColors.lavender),
                        start = Offset(0f, 0f),
                        end = Offset(w, h),
                    ),
                    center = Offset(cx, cy),
                    radius = w * 0.36f,
                )
                // Body stroke
                drawCircle(
                    color = CxColors.ink,
                    center = Offset(cx, cy),
                    radius = w * 0.36f,
                    style = Stroke(width = 2.dp.toPx()),
                )
                // Eyes (blink)
                val eyeY = cy - h * 0.04f
                val eyeR = w * 0.045f
                val eyeOpen = if (blink in 0.95f..1f || blink in 0f..0.02f) 0.2f else 1f
                drawOval(
                    color = CxColors.ink,
                    topLeft = Offset(cx - w * 0.13f - eyeR, eyeY - eyeR * eyeOpen),
                    size = Size(eyeR * 2f, eyeR * 2f * eyeOpen),
                )
                drawOval(
                    color = CxColors.ink,
                    topLeft = Offset(cx + w * 0.13f - eyeR, eyeY - eyeR * eyeOpen),
                    size = Size(eyeR * 2f, eyeR * 2f * eyeOpen),
                )
                // Cheek blush
                drawCircle(
                    color = CxColors.peach3.copy(alpha = 0.6f),
                    center = Offset(cx - w * 0.18f, cy + h * 0.05f),
                    radius = w * 0.05f,
                )
                drawCircle(
                    color = CxColors.peach3.copy(alpha = 0.6f),
                    center = Offset(cx + w * 0.18f, cy + h * 0.05f),
                    radius = w * 0.05f,
                )
            }
        }
        if (label != null) {
            Column {
                Text(
                    text = label,
                    fontSize = CxTypography.textSm,
                    fontFamily = CxTypography.fontCursive,
                    color = CxTheme.colors.textPrimary,
                )
            }
        }
    }
}
