package com.clex.android.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxSpringSpecs
import com.clex.android.ui.effects.premiumShimmer
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  BrutalistCard
//  Hard borders, hard shadows, physical press states
// ═══════════════════════════════════════════════════

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentBorder: Boolean = false,
    shadowSize: HardShadow = CxShadows.md,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CxTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor = if (accentBorder) colors.accent else colors.borderBold
    val currentShadowColor = if (accentBorder) colors.accent else shadowSize.color

    val shadowX by animateDpAsState(
        targetValue = if (isPressed) 1.dp else shadowSize.x,
        animationSpec = CxSpringSpecs.press(),
        label = "cardShadowX"
    )
    val shadowY by animateDpAsState(
        targetValue = if (isPressed) 1.dp else shadowSize.y,
        animationSpec = CxSpringSpecs.press(),
        label = "cardShadowY"
    )
    val offsetX by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = CxSpringSpecs.press(),
        label = "cardOffX"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = CxSpringSpecs.press(),
        label = "cardOffY"
    )

    // Shadow padding prevents clipping into neighbors
    Box(modifier = modifier.padding(bottom = shadowSize.y, end = shadowSize.x)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX, y = offsetY)
                .drawBehind {
                    drawRect(
                        color = currentShadowColor,
                        topLeft = Offset(shadowX.toPx(), shadowY.toPx()),
                        size = Size(this.size.width, this.size.height)
                    )
                }
                .border(CxBorders.thick, borderColor)
                .background(colors.bgCard)
                // v1.9.13 — diagonal premium shimmer sweep, kept subtle so it
                // reads as a brand layer rather than a loading state.
                .premiumShimmer(
                    color = Color.White.copy(alpha = 0.08f),
                    durationMs = 2200,
                    angleDeg = 18f,
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                    } else Modifier
                )
                .padding(CxSpacing.cardPadding),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════
//  Badge / Tag — small chip with accent colors
// ═══════════════════════════════════════════════════

@Composable
fun BrutalistBadge(
    text: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    accentColor: Color = CxTheme.colors.accent,
    showDot: Boolean = false
) {
    val colors = CxTheme.colors
    val bgColor = if (filled) accentColor else Color.Transparent
    val textColor = if (filled) CxColors.pureBlack else accentColor
    val bColor = if (filled) CxColors.pureBlack else accentColor

    Row(
        modifier = modifier
            .border(CxBorders.thin, bColor)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, shape = androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(Modifier.width(6.dp))
        }
        MonoText(
            text = text.uppercase(),
            color = textColor,
            fontSize = CxTypography.textXs,
            letterSpacing = CxTypography.textXs * 0.1
        )
    }
}

// ═══════════════════════════════════════════════════
//  TapeStrip — decorative ruled line element
// ═══════════════════════════════════════════════════

@Composable
fun TapeStrip(
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.accent,
    height: androidx.compose.ui.unit.Dp = CxBorders.heavy
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(color)
    )
}

// ═══════════════════════════════════════════════════
//  Divider — hard black line
// ═══════════════════════════════════════════════════

@Composable
fun BrutalistDivider(
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.borderColor,
    thickness: androidx.compose.ui.unit.Dp = CxBorders.medium
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
