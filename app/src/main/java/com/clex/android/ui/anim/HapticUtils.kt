package com.clex.android.ui.anim

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

// ═══════════════════════════════════════════════════
//  CLEX — Haptic Patterns
//  Every physical interaction has a tactile response
// ═══════════════════════════════════════════════════

object CxHaptics {

    // Button press — sharp, immediate
    fun press(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    // Connection established — double tap
    fun connect(context: Context) {
        vibrate(context, longArrayOf(0, 30, 60, 30), -1)
    }

    // Transfer success — triumphant pattern
    fun success(context: Context) {
        vibrate(context, longArrayOf(0, 20, 40, 20, 40, 50), -1)
    }

    // Error — hard single buzz
    fun error(context: Context) {
        vibrate(context, longArrayOf(0, 80), -1)
    }

    // Snap to position — tiny tick
    fun snap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    // Stamp — heavy single impact
    fun stamp(context: Context) {
        vibrate(context, longArrayOf(0, 50), -1)
    }

    // Scanning / searching — rhythmic pulse
    fun scanning(context: Context) {
        vibrate(context, longArrayOf(0, 15, 100, 15, 100, 15), -1)
    }

    // Chain step complete — light confirmation
    fun stepComplete(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    // Drag reorder feedback
    fun dragTick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    private fun vibrate(context: Context, pattern: LongArray, repeat: Int) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, repeat)
            }
        }
    }
}

@Composable
fun rememberHapticView(): View = LocalView.current
