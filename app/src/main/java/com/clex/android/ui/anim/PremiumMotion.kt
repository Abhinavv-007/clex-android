package com.clex.android.ui.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.CxAnim
import com.clex.android.ui.theme.CxMotion
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════
//  CLEX — Premium Motion
//  3D tilt, magnet, infinite marquee, elastic press,
//  spring reveal — building blocks for cinematic UI
// ═══════════════════════════════════════════════════

// ── 3D TILT ───────────────────────────────────────
// Drag/tap to tilt. Snaps back with spring. Simulates
// physical card hover on premium hardware.

fun Modifier.premiumTilt(
    maxDegrees: Float = CxMotion.tiltMaxDeg,
    scaleOnPress: Float = 1.03f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var sizePx by remember { mutableStateOf(Offset.Zero) }

    val reset: () -> Unit = {
        scope.launch {
            rotX.animateTo(
                0f,
                spring(
                    stiffness = CxAnim.Springs.stiffnessPanel,
                    dampingRatio = CxAnim.Springs.dampingPanel
                )
            )
        }
        scope.launch {
            rotY.animateTo(
                0f,
                spring(
                    stiffness = CxAnim.Springs.stiffnessPanel,
                    dampingRatio = CxAnim.Springs.dampingPanel
                )
            )
        }
        scope.launch {
            scale.animateTo(
                1f,
                spring(
                    stiffness = CxAnim.Springs.stiffnessPress,
                    dampingRatio = CxAnim.Springs.dampingPress
                )
            )
        }
    }

    this
        .pointerInput(Unit) {
            sizePx = Offset(size.width.toFloat(), size.height.toFloat())
            detectDragGestures(
                onDragStart = { offset ->
                    scope.launch {
                        scale.animateTo(scaleOnPress, spring(stiffness = 800f))
                    }
                    val nx = (offset.x / sizePx.x) * 2f - 1f
                    val ny = (offset.y / sizePx.y) * 2f - 1f
                    scope.launch { rotY.snapTo(nx * maxDegrees) }
                    scope.launch { rotX.snapTo(-ny * maxDegrees) }
                },
                onDrag = { change, _ ->
                    val nx = (change.position.x / sizePx.x) * 2f - 1f
                    val ny = (change.position.y / sizePx.y) * 2f - 1f
                    scope.launch { rotY.snapTo(nx * maxDegrees) }
                    scope.launch { rotX.snapTo(-ny * maxDegrees) }
                },
                onDragEnd = { reset() },
                onDragCancel = { reset() }
            )
        }
        .graphicsLayer {
            rotationX = rotX.value
            rotationY = rotY.value
            scaleX = scale.value
            scaleY = scale.value
            cameraDistance = 14f * density
            transformOrigin = TransformOrigin.Center
        }
}

// ── ELASTIC PRESS ─────────────────────────────────
// Tap-to-squish then spring back. Use on buttons +
// interactive tiles.

fun Modifier.elasticPress(
    pressScale: Float = 0.94f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    scope.launch {
                        scale.animateTo(
                            pressScale,
                            spring(
                                stiffness = CxAnim.Springs.stiffnessPress,
                                dampingRatio = CxAnim.Springs.dampingPress
                            )
                        )
                    }
                    val released = tryAwaitRelease()
                    scope.launch {
                        scale.animateTo(
                            1f,
                            spring(
                                stiffness = 600f,
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        )
                    }
                    if (released) onClick()
                }
            )
        }
}

// ── PARALLAX FLOAT ────────────────────────────────
// Infinite drift — element floats up/down in a slow
// sine. Stagger with delayFrac for natural chorus.

fun Modifier.parallaxFloat(
    amplitude: Dp = 8.dp,
    durationMs: Int = CxAnim.floatDuration,
    delayFrac: Float = 0f
): Modifier = composed {
    val density = LocalDensity.current
    val ampPx = with(density) { amplitude.toPx() }
    val transition = rememberInfiniteTransition(label = "parallaxFloat")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing)
        ),
        label = "floatPhase"
    )
    val offsetY = kotlin.math.sin(phase + delayFrac * Math.PI.toFloat() * 2f) * ampPx
    this.offset { IntOffset(0, offsetY.roundToInt()) }
}

// ── MAGNET PULL ───────────────────────────────────
// Element attracts toward a target offset when the
// condition flips. Spring-based.

@Composable
fun rememberMagnet(
    target: Offset,
    stiffness: Float = 280f,
    damping: Float = 0.6f
): Pair<Float, Float> {
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }
    LaunchedEffect(target) {
        launch {
            x.animateTo(
                target.x,
                spring(stiffness = stiffness, dampingRatio = damping)
            )
        }
        launch {
            y.animateTo(
                target.y,
                spring(stiffness = stiffness, dampingRatio = damping)
            )
        }
    }
    return x.value to y.value
}

// ── INFINITE MARQUEE ──────────────────────────────
// Seamless horizontal ticker for premium brand marks,
// stats, or status lines.
// Fix: wraps content in Row (items side-by-side, not
// overlapping), measures actual content width for
// seamless looping — not container width.

@Composable
fun InfiniteMarquee(
    modifier: Modifier = Modifier,
    durationMs: Int = CxMotion.tickerSpeed,
    reverse: Boolean = false,
    content: @Composable () -> Unit
) {
    // Measured width of a single content copy
    var contentWidthPx by remember { mutableIntStateOf(0) }

    val transition = rememberInfiniteTransition(label = "marquee")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marqueeProgress"
    )

    val w = contentWidthPx.takeIf { it > 0 } ?: 1
    val dir = if (reverse) 1f else -1f
    val offsetPx = (dir * progress * w).roundToInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier.offset { IntOffset(offsetPx, 0) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First copy — measure its width
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.onGloballyPositioned { coords ->
                    val w = coords.size.width
                    if (w > 0 && contentWidthPx != w) contentWidthPx = w
                }
            ) { content() }

            // Two more copies to ensure seamless fill regardless of screen width
            Row(verticalAlignment = Alignment.CenterVertically) { content() }
            Row(verticalAlignment = Alignment.CenterVertically) { content() }
        }
    }
}

// ── REVEAL FROM BELOW ─────────────────────────────
// Slide + fade on first composition. Used for hero
// text, cards. Stagger via initialDelayMs.

@Composable
fun rememberRevealFromBelow(
    initialDelayMs: Long = 0,
    distanceDp: Dp = 24.dp,
    key: Any? = Unit
): Pair<Float, Float> {
    val translate = remember(key) { Animatable(1f) }
    val alpha = remember(key) { Animatable(0f) }
    val density = LocalDensity.current
    val distPx = with(density) { distanceDp.toPx() }
    LaunchedEffect(key) {
        if (initialDelayMs > 0) kotlinx.coroutines.delay(initialDelayMs)
        launch {
            translate.animateTo(
                0f,
                spring(
                    stiffness = CxAnim.Springs.stiffnessPanel,
                    dampingRatio = CxAnim.Springs.dampingPanel
                )
            )
        }
        launch {
            alpha.animateTo(1f, tween(CxAnim.durationNormal))
        }
    }
    return (translate.value * distPx) to alpha.value
}

// ── COUNT-UP ANIMATION ────────────────────────────
// Drive a numeric tween from 0 → target. Use for
// hero stats.

@Composable
fun rememberCountUp(
    target: Int,
    durationMs: Int = 1400,
    startDelayMs: Long = 200
): Int {
    var value by remember(target) { mutableStateOf(0) }
    LaunchedEffect(target) {
        if (startDelayMs > 0) kotlinx.coroutines.delay(startDelayMs)
        val start = System.nanoTime()
        while (true) {
            val elapsed = (System.nanoTime() - start) / 1_000_000f
            val frac = (elapsed / durationMs).coerceIn(0f, 1f)
            val eased = 1f - (1f - frac) * (1f - frac) * (1f - frac)
            value = (eased * target).roundToInt()
            if (frac >= 1f) break
            withFrameNanos { }
        }
        value = target
    }
    return value
}

// ── SHAKE ─────────────────────────────────────────
// Error / attention-grab horizontal jitter.

fun Modifier.shakeOn(
    trigger: Any?,
    amplitudeDp: Dp = 6.dp,
    durationMs: Int = 420
): Modifier = composed {
    val density = LocalDensity.current
    val ampPx = with(density) { amplitudeDp.toPx() }
    var offsetX by remember { mutableStateOf(0f) }
    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        val start = System.nanoTime()
        while (true) {
            val elapsed = (System.nanoTime() - start) / 1_000_000f
            val t = (elapsed / durationMs).coerceIn(0f, 1f)
            val decay = 1f - t
            offsetX = (kotlin.math.sin(t * Math.PI.toFloat() * 8f)) * ampPx * decay
            if (t >= 1f) {
                offsetX = 0f
                break
            }
            withFrameNanos { }
        }
    }
    this.offset { IntOffset(offsetX.roundToInt(), 0) }
}

// ── HOVER LIFT ────────────────────────────────────
// Non-gesture based "idle breathe" scale — makes
// cards feel alive. Stagger via delayFrac.

fun Modifier.idleBreathe(
    min: Float = 0.995f,
    max: Float = 1.01f,
    durationMs: Int = 4200,
    delayFrac: Float = 0f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "breathe")
    val raw by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing)
        ),
        label = "breathePhase"
    )
    val phase = kotlin.math.sin(raw + delayFrac * Math.PI.toFloat() * 2f)
    val midpoint = (min + max) / 2f
    val halfRange = (max - min) / 2f
    val scale = midpoint + phase * halfRange
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ── ROTATING CONIC ────────────────────────────────
// Slow-rotation utility for loading rings and badges.

fun Modifier.slowRotate(
    durationMs: Int = CxAnim.rotateDuration,
    clockwise: Boolean = true
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "slowRotate")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (clockwise) 360f else -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing)
        ),
        label = "rotateAngle"
    )
    this.graphicsLayer { rotationZ = angle }
}

// ── MAGNETIC HOVER ────────────────────────────────
// On drag, shift a small amount toward finger and
// spring back on release. Lighter than full tilt.

fun Modifier.magneticHover(
    rangeDp: Dp = 12.dp
): Modifier = composed {
    val density = LocalDensity.current
    val rangePx = with(density) { rangeDp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    this
        .pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, _ ->
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val dx = ((change.position.x - cx) / cx).coerceIn(-1f, 1f)
                    val dy = ((change.position.y - cy) / cy).coerceIn(-1f, 1f)
                    scope.launch { offsetX.snapTo(dx * rangePx) }
                    scope.launch { offsetY.snapTo(dy * rangePx) }
                },
                onDragEnd = {
                    scope.launch { offsetX.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.5f)) }
                    scope.launch { offsetY.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.5f)) }
                },
                onDragCancel = {
                    scope.launch { offsetX.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.5f)) }
                    scope.launch { offsetY.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.5f)) }
                }
            )
        }
        .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
}

@Suppress("unused")
private fun Offset.magnitude(): Float = kotlin.math.sqrt(x * x + y * y)

@Suppress("unused")
private fun Float.absClamp(max: Float): Float = if (abs(this) > max) max * (if (this >= 0) 1 else -1) else this
