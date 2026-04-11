package com.clex.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  BrutalistProgressBar
//  Chunky, segmented or smooth, hard borders
// ═══════════════════════════════════════════════════

@Composable
fun BrutalistProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = CxTheme.colors.accent,
    showLabel: Boolean = true,
    segments: Int = 0  // 0 = smooth, >0 = segmented
) {
    val colors = CxTheme.colors
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            stiffness = CxAnim.Springs.stiffnessPanel,
            dampingRatio = CxAnim.Springs.dampingPanel
        ),
        label = "progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoText(
                    text = "PROGRESS",
                    fontSize = CxTypography.textXs,
                    color = colors.textTertiary
                )
                MonoText(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = CxTypography.textXs,
                    color = accentColor
                )
            }
            Spacer(Modifier.height(CxSpacing.sm))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .border(CxBorders.medium, colors.borderBold)
                .background(colors.bgInput)
        ) {
            if (segments > 0) {
                // Segmented bar
                Row(modifier = Modifier.fillMaxSize()) {
                    val filledSegments = (animatedProgress * segments).toInt()
                    repeat(segments) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .then(
                                    if (i < segments - 1) Modifier.border(
                                        width = 1.dp,
                                        color = colors.borderColor
                                    ) else Modifier
                                )
                                .background(
                                    if (i < filledSegments) accentColor
                                    else Color.Transparent
                                )
                        )
                    }
                }
            } else {
                // Smooth bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(accentColor)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  StatusDot — colored dot with optional pulse
// ═══════════════════════════════════════════════════

@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 10.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (pulsing) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .alpha(pulseAlpha)
            .background(color, CircleShape)
            .border(1.dp, CxColors.pureBlack, CircleShape)
    )
}

// ═══════════════════════════════════════════════════
//  RadarBackground — animated scanning visual
// ═══════════════════════════════════════════════════

@Composable
fun RadarBackground(
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.accent,
    sweepDuration: Int = CxAnim.radarSweepDuration
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(sweepDuration, easing = LinearEasing)
        ),
        label = "sweep"
    )

    Box(
        modifier = modifier.drawBehind {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(centerX, centerY)

            // Draw concentric circles
            for (i in 1..4) {
                drawCircle(
                    color = color.copy(alpha = 0.08f),
                    radius = radius * (i / 4f),
                    center = Offset(centerX, centerY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            }

            // Draw sweep line
            val rad = Math.toRadians(sweepAngle.toDouble())
            val endX = centerX + radius * kotlin.math.cos(rad).toFloat()
            val endY = centerY + radius * kotlin.math.sin(rad).toFloat()
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
    )
}

// ═══════════════════════════════════════════════════
//  ScanLine — horizontal line that sweeps vertically
// ═══════════════════════════════════════════════════

@Composable
fun ScanLineOverlay(
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.accent,
    duration: Int = CxAnim.scanLineDuration
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val positionFrac by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing)
        ),
        label = "scanPos"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val y = size.height * positionFrac
                drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx()
                )
            }
    )
}

// ═══════════════════════════════════════════════════
//  AnimatedCheckmark — Path drawing animation
// ═══════════════════════════════════════════════════

@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    color: Color = CxColors.success,
    strokeWidth: androidx.compose.ui.unit.Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "checkPulse")
    var isDrawn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        isDrawn = true
    }

    val drawAlpha by animateFloatAsState(
        targetValue = if (isDrawn) 1f else 0f,
        animationSpec = spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium,
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
        ),
        label = "drawAlpha"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .background(color = color.copy(alpha = 0.1f * drawAlpha), shape = CircleShape)
            .border(2.dp, color.copy(alpha = drawAlpha), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(32.dp)
                .alpha(drawAlpha)
                .offset(y = ((1f - drawAlpha) * 10).dp)
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.45f, size.height * 0.75f)
                lineTo(size.width * 0.85f, size.height * 0.25f)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )
        }
    }
}
