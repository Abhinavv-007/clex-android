package com.clex.android.ui.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

fun Modifier.physicalPress(
    pressedScale: Float = 0.92f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val hapticView = rememberHapticView()
    val scaleAnim = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    this
        .scale(scaleAnim.value)
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                CxHaptics.press(hapticView)
                scope.launch {
                    scaleAnim.snapTo(pressedScale)
                }

                val up = waitForUpOrCancellation()
                if (up != null) {
                    CxHaptics.snap(hapticView)
                    onClick?.invoke()
                }

                scope.launch {
                    scaleAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = CxSpringSpecs.bounce()
                    )
                }
            }
        }
}

fun Modifier.shakeEffect(
    isShaking: Boolean,
    onShakeComplete: () -> Unit
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -30f at 50
                    30f at 100
                    -20f at 150
                    20f at 200
                    -10f at 250
                    10f at 300
                    0f at 400
                }
            )
            onShakeComplete()
        }
    }

    this.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}
