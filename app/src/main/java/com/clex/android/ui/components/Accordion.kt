package com.clex.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalView
import com.clex.android.ui.anim.CxHaptics
import com.clex.android.ui.anim.CxSpringSpecs
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

    // v1.9.13 — chevron rotates with a spring instead of a flat tween, so the
    // icon "settles" with the rest of the card rather than easing in straight.
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = CxSpringSpecs.bounce(),
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

        // Content — v1.9.13 spring expand for content slide so the panel
        // settles instead of linearly sliding open. Fade still runs on a
        // tween for legibility.
        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically(
                animationSpec = spring(
                    stiffness = CxAnim.Springs.stiffnessPanel,
                    dampingRatio = CxAnim.Springs.dampingPanel,
                ),
                initialOffsetY = { -it / 6 },
            ) + expandVertically(
                animationSpec = spring(
                    stiffness = CxAnim.Springs.stiffnessPanel,
                    dampingRatio = CxAnim.Springs.dampingPanel,
                ),
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
