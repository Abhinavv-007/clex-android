package com.clex.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  BrutalistAccordion
//  FAQ-style collapsible with + / − toggle icon
// ═══════════════════════════════════════════════════

@Composable
fun BrutalistAccordion(
    question: String,
    modifier: Modifier = Modifier,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = CxTheme.colors
    val view = LocalView.current
    var expanded by remember { mutableStateOf(defaultExpanded) }

    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(CxAnim.durationNormal),
        label = "accordionIcon"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = CxBorders.medium, color = colors.borderBold)
            .background(colors.bgCard)
    ) {
        // Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    CxHaptics.press(view)
                    expanded = !expanded
                }
                .padding(horizontal = CxSpacing.md, vertical = CxSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MonoText(
                text = question,
                fontSize = CxTypography.textBase,
                fontWeight = CxTypography.weightBold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            Spacer(Modifier.width(CxSpacing.md))
            MonoText(
                text = "+",
                fontSize = CxTypography.text2xl,
                color = colors.accent,
                modifier = Modifier.rotate(iconRotation)
            )
        }

        // Content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(CxAnim.durationNormal)
            ) + fadeIn(animationSpec = tween(CxAnim.durationNormal)),
            exit = shrinkVertically(
                animationSpec = tween(CxAnim.durationNormal)
            ) + fadeOut(animationSpec = tween(CxAnim.durationFast))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSecondary)
                    .padding(CxSpacing.md)
            ) {
                content()
            }
        }
    }
}
