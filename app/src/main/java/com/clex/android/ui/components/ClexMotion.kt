package com.clex.android.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ═══════════════════════════════════════════════════
//  ClexMotion — v1.12 unified motion language.
//  Single canonical spring, shared-axis page swap,
//  press-scale + haptic, scroll parallax helpers.
// ═══════════════════════════════════════════════════

object ClexMotion {
    // Canonical spring — every interactive surface uses this.
    const val springStiffness = 380f
    const val springDamping = 0.78f

    fun <T> defaultSpring(): SpringSpec<T> = spring(
        dampingRatio = springDamping,
        stiffness = springStiffness,
    )

    // Shared-axis X — used for tab content swaps. Slide 32dp + crossfade.
    val sharedAxisOffset = 32

    // Tween durations.
    const val durationMicro = 120
    const val durationStandard = 240
    const val durationEmphasis = 380
}

// ── Press-scale + haptic — applied to every tappable card / tile ──
fun Modifier.pressable(
    enabled: Boolean = true,
    pressScale: Float = 0.97f,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val onClickState = rememberUpdatedState(onClick)
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressScale else 1f,
        animationSpec = ClexMotion.defaultSpring(),
        label = "press-scale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    pressed = true
                    if (haptic) hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    val released = tryAwaitRelease()
                    pressed = false
                    if (released) onClickState.value()
                },
            )
        }
}

// ── Press-scale only (no click) — used inside lazy lists where the row's
//    own clickable handles the click, but we still want the spring depress. ──
fun Modifier.pressDepress(pressed: Boolean, pressScale: Float = 0.97f): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressScale else 1f,
        animationSpec = ClexMotion.defaultSpring(),
        label = "press-depress",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ── Shared-axis X transition — for AnimatedContent / Crossfade swaps. ──
fun AnimatedContentTransitionScope<*>.sharedAxisX(forward: Boolean = true): ContentTransform {
    val dir = if (forward) 1 else -1
    return slideInHorizontally(
        animationSpec = ClexMotion.defaultSpring(),
        initialOffsetX = { it * ClexMotion.sharedAxisOffset / 100 * dir },
    ) + fadeIn(
        animationSpec = tween(ClexMotion.durationStandard, delayMillis = 40),
    ) togetherWith slideOutHorizontally(
        animationSpec = ClexMotion.defaultSpring(),
        targetOffsetX = { -it * ClexMotion.sharedAxisOffset / 100 * dir },
    ) + fadeOut(
        animationSpec = tween(ClexMotion.durationMicro),
    )
}

// ── Reveal-from-below — for first paint of column children. ──
fun reveal(direction: Int = 1): ContentTransform {
    val dir = direction
    return slideInHorizontally(
        animationSpec = ClexMotion.defaultSpring(),
        initialOffsetX = { it * 8 / 100 * dir },
    ) + fadeIn(
        animationSpec = tween(ClexMotion.durationStandard),
    ) togetherWith fadeOut(
        animationSpec = tween(ClexMotion.durationMicro),
    )
}
