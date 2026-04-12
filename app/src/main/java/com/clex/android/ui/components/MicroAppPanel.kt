package com.clex.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxSpringSpecs
import com.clex.android.ui.anim.borderDrawIn
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  MicroAppPanel
//  Windowed tool panel with title bar, border-draw
//  animation, and stacked panel behavior
// ═══════════════════════════════════════════════════

@Composable
fun MicroAppPanel(
    title: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClose: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CxTheme.colors

    var borderVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            borderVisible = true
            kotlinx.coroutines.delay(CxAnim.durationSlow.toLong())
            contentVisible = true
        } else {
            contentVisible = false
            borderVisible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = CxSpringSpecs.panel()
        ),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .borderDrawIn(
                    visible = borderVisible,
                    color = colors.borderBold,
                    strokeWidth = CxBorders.thick.value,
                    duration = CxAnim.durationSlow
                )
                .drawBehind {
                    // Hard shadow
                    drawRect(
                        color = colors.shadowColor,
                        topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                        size = Size(this.size.width, this.size.height)
                    )
                }
                .border(CxBorders.thick, colors.borderBold)
                .background(colors.bgCard)
        ) {
            // ── Title Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgTertiary)
                    .border(
                        width = CxBorders.thick,
                        color = colors.borderBold
                    )
                    .padding(horizontal = CxSpacing.md, vertical = CxSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(
                    text = title.uppercase(),
                    fontSize = CxTypography.textXs,
                    color = colors.textSecondary,
                    letterSpacing = CxTypography.textXs * 0.15
                )

                if (onClose != null) {
                    MonoText(
                        text = "✕",
                        fontSize = CxTypography.textBase,
                        color = colors.textSecondary,
                        modifier = Modifier.clickable { onClose() }
                    )
                }
            }

            // ── Content area with fade-in after border draws ──
            val contentAlpha by animateFloatAsState(
                targetValue = if (contentVisible) 1f else 0f,
                animationSpec = tween(CxAnim.durationNormal),
                label = "panelContent"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha)
                    .padding(CxSpacing.md),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════════════
//  SectionLabel — pill with dot indicator
//  Matches the web's .section__label pattern
// ═══════════════════════════════════════════════════

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(
                width = 1.dp,
                color = colors.accent.copy(alpha = if (colors.isDark) 0.35f else 0.55f),
                shape = RoundedCornerShape(999.dp)
            )
            .background(
                if (colors.isDark) colors.bgCard.copy(alpha = 0.76f)
                else colors.bgCard.copy(alpha = 0.92f)
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    colors.accent,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        Spacer(Modifier.width(8.dp))
        MonoText(
            text = text.uppercase(),
            fontSize = CxTypography.textXs,
            color = colors.accent,
            letterSpacing = CxTypography.textXs * 0.2
        )
    }
}
