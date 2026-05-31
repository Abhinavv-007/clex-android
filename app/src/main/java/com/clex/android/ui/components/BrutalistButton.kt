package com.clex.android.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.anim.CxSpringSpecs
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  BrutalistButton
//  Hard offset shadow, physical depression on press
//  No ripple. No Material press states.
// ═══════════════════════════════════════════════════

enum class ButtonVariant { PRIMARY, SECONDARY, GHOST }
enum class ButtonSize { SMALL, MEDIUM, LARGE }

@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ButtonSize = ButtonSize.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colors = CxTheme.colors
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Shadow offset animates to collapse on press
    val shadowOffset by animateDpAsState(
        targetValue = if (isPressed) 1.dp else when (size) {
            ButtonSize.SMALL -> 3.dp
            ButtonSize.MEDIUM -> 5.dp
            ButtonSize.LARGE -> 5.dp
        },
        animationSpec = CxSpringSpecs.press(),
        label = "shadowOffset"
    )

    val translateX by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = CxSpringSpecs.press(),
        label = "translateX"
    )
    val translateY by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = CxSpringSpecs.press(),
        label = "translateY"
    )

    // Variant colors
    val bgColor = when (variant) {
        ButtonVariant.PRIMARY -> colors.accent
        ButtonVariant.SECONDARY -> colors.bgCard
        ButtonVariant.GHOST -> Color.Transparent
    }
    val textColor = when (variant) {
        ButtonVariant.PRIMARY -> CxColors.pureBlack
        ButtonVariant.SECONDARY -> colors.textPrimary
        ButtonVariant.GHOST -> colors.textPrimary
    }
    val borderColor = when (variant) {
        ButtonVariant.PRIMARY -> if (colors.isDark) CxColors.pureBlack else colors.accent.copy(alpha = 0.75f)
        ButtonVariant.SECONDARY -> if (colors.isDark) colors.borderBold else colors.borderColor
        ButtonVariant.GHOST -> colors.borderColor
    }
    val shadowColor = when (variant) {
        ButtonVariant.PRIMARY -> if (colors.isDark) CxColors.pureBlack else colors.accent.copy(alpha = 0.26f)
        ButtonVariant.SECONDARY -> colors.shadowColor
        ButtonVariant.GHOST -> Color.Transparent
    }

    // Size
    val paddingH = when (size) {
        ButtonSize.SMALL -> 16.dp
        ButtonSize.MEDIUM -> 24.dp
        ButtonSize.LARGE -> 32.dp
    }
    val paddingV = when (size) {
        ButtonSize.SMALL -> 10.dp
        ButtonSize.MEDIUM -> 14.dp
        ButtonSize.LARGE -> 18.dp
    }
    val fontSize = when (size) {
        ButtonSize.SMALL -> CxTypography.textXs
        ButtonSize.MEDIUM -> CxTypography.textSm
        ButtonSize.LARGE -> CxTypography.textBase
    }
    val borderWidth = when (size) {
        ButtonSize.SMALL -> CxBorders.medium
        ButtonSize.MEDIUM -> CxBorders.thick
        ButtonSize.LARGE -> CxBorders.thick
    }

    // Press-glow (v1.9.13) — pulses a 16dp radial neonLime glow behind the
    // button as the press fires, then fades out. Stays subtle on GHOST.
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.30f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 200 else 320),
        label = "buttonGlowAlpha"
    )
    val glowColor = when (variant) {
        ButtonVariant.PRIMARY -> CxPremium.neonLime
        ButtonVariant.SECONDARY -> colors.accent
        ButtonVariant.GHOST -> colors.accent
    }
    val glowSpread = with(androidx.compose.ui.platform.LocalDensity.current) { 16.dp.toPx() }

    Box(
        modifier = modifier
            .offset(x = translateX, y = translateY)
            .drawBehind {
                // Press glow — radial halo around the button. Note: `size`
                // (the ButtonSize function param) shadows DrawScope.size in
                // this lambda, so we read this.size explicitly.
                if (glowAlpha > 0.001f) {
                    val cw = this.size.width
                    val ch = this.size.height
                    val cx = cw / 2f
                    val cy = ch / 2f
                    val r = (maxOf(cw, ch) / 2f) + glowSpread
                    if (r > 0f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = glowAlpha),
                                    Color.Transparent,
                                ),
                                center = Offset(cx, cy),
                                radius = r,
                            ),
                            topLeft = Offset(-glowSpread, -glowSpread),
                            size = Size(cw + glowSpread * 2f, ch + glowSpread * 2f),
                        )
                    }
                }
                // Hard shadow behind
                if (variant != ButtonVariant.GHOST) {
                    drawRect(
                        color = shadowColor,
                        topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                        size = Size(this.size.width, this.size.height)
                    )
                }
            }
            .border(borderWidth, borderColor)
            .background(if (enabled) bgColor else colors.bgTertiary)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    CxHaptics.press(view)
                    onClick()
                }
            )
            .padding(horizontal = paddingH, vertical = paddingV)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(Modifier.width(CxSpacing.sm))

            MonoText(
                text = text.uppercase(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = if (enabled) textColor else colors.textTertiary,
                letterSpacing = CxTypography.textXs * 0.05f,
                textAlign = TextAlign.Center
            )

            if (trailingIcon != null) Spacer(Modifier.width(CxSpacing.sm))
            trailingIcon?.invoke()
        }
    }
}
