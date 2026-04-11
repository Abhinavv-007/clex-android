package com.clex.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  ChainStep — single step in a tool chain pipeline
//  Shows: number, title, status, connecting line
// ═══════════════════════════════════════════════════

enum class StepStatus { IDLE, ACTIVE, COMPLETE, ERROR }

@Composable
fun ChainStep(
    number: Int,
    title: String,
    status: StepStatus,
    modifier: Modifier = Modifier,
    isLast: Boolean = false
) {
    val colors = CxTheme.colors
    val statusColor = when (status) {
        StepStatus.IDLE -> colors.borderColor
        StepStatus.ACTIVE -> colors.accent
        StepStatus.COMPLETE -> CxColors.success
        StepStatus.ERROR -> CxColors.error
    }

    val infiniteTransition = rememberInfiniteTransition(label = "chain$number")
    val activeGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (status == StepStatus.ACTIVE) 0.3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(CxAnim.pulseDuration / 2, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step block
        Box(
            modifier = Modifier
                .drawBehind {
                    drawRect(
                        color = statusColor.copy(alpha = activeGlow),
                        topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                        size = Size(
                            this.size.width + 8.dp.toPx(),
                            this.size.height + 8.dp.toPx()
                        )
                    )
                }
                .drawBehind {
                    drawRect(
                        color = colors.shadowColor,
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = Size(this.size.width, this.size.height)
                    )
                }
                .border(CxBorders.medium, statusColor)
                .background(
                    if (status == StepStatus.ACTIVE) colors.accentMuted
                    else colors.bgCard
                )
                .padding(horizontal = CxSpacing.lg, vertical = CxSpacing.md)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MonoText(
                    text = String.format("%02d", number),
                    fontSize = CxTypography.textXs,
                    color = statusColor,
                    letterSpacing = CxTypography.textXs * 0.1
                )
                Spacer(Modifier.height(4.dp))
                MonoText(
                    text = title.uppercase(),
                    fontSize = CxTypography.textSm,
                    color = colors.textPrimary,
                    letterSpacing = CxTypography.textXs * 0.05
                )
            }
        }

        // Connector
        if (!isLast) {
            Row(
                modifier = Modifier.padding(horizontal = CxSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonoText(
                    text = "→",
                    fontSize = CxTypography.textXl,
                    color = colors.accent
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  ChainPipeline — horizontal scrollable chain view
// ═══════════════════════════════════════════════════

data class ChainStepData(
    val number: Int,
    val title: String,
    val status: StepStatus = StepStatus.IDLE
)

@Composable
fun ChainPipeline(
    steps: List<ChainStepData>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CxSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        steps.forEachIndexed { index, step ->
            ChainStep(
                number = step.number,
                title = step.title,
                status = step.status,
                isLast = index == steps.lastIndex
            )
        }
    }
}
