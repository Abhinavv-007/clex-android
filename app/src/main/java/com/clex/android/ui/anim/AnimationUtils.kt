package com.clex.android.ui.anim

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.CxAnim
import com.clex.android.ui.theme.CxColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════
//  CLEX — Animation Utilities
//  Neo-Brutalist motion: physical, snappy, alive
// ═══════════════════════════════════════════════════

// ── Spring Specs ────────────────────────────────────

object CxSpringSpecs {
    fun <T> snap() = spring<T>(
        stiffness = CxAnim.Springs.stiffnessSnap,
        dampingRatio = CxAnim.Springs.dampingSnap
    )

    fun <T> panel() = spring<T>(
        stiffness = CxAnim.Springs.stiffnessPanel,
        dampingRatio = CxAnim.Springs.dampingPanel
    )

    fun <T> bounce() = spring<T>(
        stiffness = CxAnim.Springs.stiffnessBounce,
        dampingRatio = CxAnim.Springs.dampingBounce
    )

    fun <T> gentle() = spring<T>(
        stiffness = CxAnim.Springs.stiffnessGentle,
        dampingRatio = CxAnim.Springs.dampingGentle
    )

    fun <T> slam() = spring<T>(
        stiffness = CxAnim.Springs.stiffnessSlam,
        dampingRatio = CxAnim.Springs.dampingSlam
    )

    fun <T> press() = spring<T>(
        stiffness = CxAnim.Springs.stiffnessPress,
        dampingRatio = CxAnim.Springs.dampingPress
    )
}

// ── Entrance Animations ─────────────────────────────

@Composable
fun RevealFromBottom(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMs: Long = 0,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var shouldShow by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs)
            shouldShow = true
        } else {
            shouldShow = false
        }
    }

    AnimatedVisibility(
        visible = shouldShow,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = CxSpringSpecs.panel()
        ) + fadeIn(animationSpec = tween(CxAnim.durationNormal)),
        exit = slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec = tween(CxAnim.durationFast)
        ) + fadeOut(animationSpec = tween(CxAnim.durationFast)),
        content = content
    )
}

@Composable
fun StableRevealFromBottom(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMs: Long = 0,
    initialOffsetY: Int = 18,
    content: @Composable () -> Unit
) {
    var shouldShow by remember(visible, delayMs) { mutableStateOf(false) }

    LaunchedEffect(visible, delayMs) {
        if (visible) {
            shouldShow = false
            delay(delayMs)
            shouldShow = true
        } else {
            shouldShow = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (shouldShow) 1f else 0f,
        animationSpec = tween(CxAnim.durationNormal, easing = EaseOutCubic),
        label = "stableRevealAlpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (shouldShow) 0.dp else initialOffsetY.dp,
        animationSpec = tween(CxAnim.durationNormal, easing = EaseOutCubic),
        label = "stableRevealOffset"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .alpha(alpha)
    ) {
        content()
    }
}

@Composable
fun SlamIn(
    visible: Boolean,
    delayMs: Long = 0,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var shouldShow by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs)
            shouldShow = true
        } else {
            shouldShow = false
        }
    }

    AnimatedVisibility(
        visible = shouldShow,
        enter = scaleIn(
            initialScale = 1.15f,
            animationSpec = CxSpringSpecs.slam()
        ) + fadeIn(animationSpec = tween(CxAnim.durationFast)),
        exit = scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(CxAnim.durationFast)
        ) + fadeOut(),
        content = content
    )
}

@Composable
fun StampIn(
    visible: Boolean,
    delayMs: Long = 0,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var shouldShow by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs)
            shouldShow = true
        } else {
            shouldShow = false
        }
    }

    AnimatedVisibility(
        visible = shouldShow,
        enter = scaleIn(
            initialScale = 2.0f,
            animationSpec = CxSpringSpecs.snap()
        ) + fadeIn(animationSpec = tween(100)),
        exit = fadeOut(animationSpec = tween(CxAnim.durationFast)),
        content = content
    )
}

// ── Idle Animations ─────────────────────────────────

fun Modifier.floatingIdle(
    amplitude: Float = 10f,
    duration: Int = CxAnim.floatDuration
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(duration / 2, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    this.offset { IntOffset(0, offsetY.roundToInt()) }
}

fun Modifier.pulseGlow(
    color: Color = CxColors.accent,
    duration: Int = CxAnim.pulseDuration
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration / 2, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    this.drawBehind {
        drawRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(-8.dp.toPx(), -8.dp.toPx()),
            size = Size(size.width + 16.dp.toPx(), size.height + 16.dp.toPx())
        )
    }
}

// ── Border Draw Animation ───────────────────────────

fun Modifier.borderDrawIn(
    visible: Boolean,
    color: Color = CxColors.borderBold,
    strokeWidth: Float = 3f,
    duration: Int = CxAnim.durationSlow
): Modifier = composed {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(duration, easing = LinearEasing),
        label = "borderDraw"
    )
    this.drawBehind {
        val perimeter = 2 * (size.width + size.height)
        val drawnLength = perimeter * progress

        val stroke = Stroke(width = strokeWidth)
        var remaining = drawnLength

        // Top edge
        val topLen = minOf(remaining, size.width)
        if (topLen > 0) {
            drawLine(color, Offset(0f, 0f), Offset(topLen, 0f), strokeWidth)
            remaining -= topLen
        }
        // Right edge
        val rightLen = minOf(remaining, size.height)
        if (rightLen > 0) {
            drawLine(color, Offset(size.width, 0f), Offset(size.width, rightLen), strokeWidth)
            remaining -= rightLen
        }
        // Bottom edge
        val bottomLen = minOf(remaining, size.width)
        if (bottomLen > 0) {
            drawLine(color, Offset(size.width, size.height), Offset(size.width - bottomLen, size.height), strokeWidth)
            remaining -= bottomLen
        }
        // Left edge
        val leftLen = minOf(remaining, size.height)
        if (leftLen > 0) {
            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - leftLen), strokeWidth)
        }
    }
}

// ── Stagger Helper ──────────────────────────────────

@Composable
fun StaggeredColumn(
    itemCount: Int,
    staggerDelayMs: Long = CxAnim.staggerDelay,
    content: @Composable (index: Int, visible: Boolean) -> Unit
) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }

    repeat(itemCount) { index ->
        var itemVisible by remember { mutableStateOf(false) }
        LaunchedEffect(triggered) {
            if (triggered) {
                delay(index * staggerDelayMs)
                itemVisible = true
            }
        }
        content(index, itemVisible)
    }
}

@Composable
fun rememberEntryVisibility(
    trigger: Any? = Unit,
    delayMs: Long = 80L
): Boolean {
    var visible by remember(trigger) { mutableStateOf(false) }
    LaunchedEffect(trigger) {
        visible = false
        delay(delayMs)
        visible = true
    }
    return visible
}

// ── Screen Transition Specs ─────────────────────────

object CxTransitions {
    // Snappy tween-based transitions — no bouncy springs that cause multi-jump
    private val enterDuration = 260
    private val exitDuration = 200

    val screenEnter = slideInHorizontally(
        initialOffsetX = { it / 4 },
        animationSpec = tween(enterDuration, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(enterDuration))

    val screenExit = slideOutHorizontally(
        targetOffsetX = { -it / 6 },
        animationSpec = tween(exitDuration, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(exitDuration))

    val screenPopEnter = slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(enterDuration, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(enterDuration))

    val screenPopExit = slideOutHorizontally(
        targetOffsetX = { it / 6 },
        animationSpec = tween(exitDuration, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(exitDuration))

    // ── Bottom-nav tab swap (v1.9.13) ──
    // Slight scale-in + fade for the entering tab, slight scale-out + fade
    // for the exiting tab. Synced with the web frontend's bottom-nav swap.
    val tabEnter = scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(200, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(200))

    val tabExit = scaleOut(
        targetScale = 1.05f,
        animationSpec = tween(160, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(160))
}
