package com.clex.android.ui.screens.splash

import com.clex.android.AppRelease
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clex.android.R
import com.clex.android.ui.components.MonoText
import com.clex.android.ui.effects.ParticleField
import com.clex.android.ui.effects.ScanLineSweep
import com.clex.android.ui.effects.VignetteOverlay
import com.clex.android.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════
//  CLEX — Splash Screen
//  Cinematic 5-phase entrance:
//    1. Deep black + particles fade in
//    2. Radial glow pulses from center
//    3. "CLEX" stamps in at 2.5× scale → snaps to 1×
//    4. Accent underline draws + tagline reveals
//    5. Ring burst + scan line sweep → navigate
// ═══════════════════════════════════════════════════

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    // Phase states — staggered cinematic beats
    var phase0Bg by remember { mutableStateOf(false) }
    var phase1Glow by remember { mutableStateOf(false) }
    var phase2Logo by remember { mutableStateOf(false) }
    var phase3Line by remember { mutableStateOf(false) }
    var phase4Tag by remember { mutableStateOf(false) }
    var phase5Burst by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        phase0Bg = true      // particles + mesh start
        delay(350)
        phase1Glow = true    // center glow bloom
        delay(400)
        phase2Logo = true    // CLEX stamps in
        delay(500)
        phase3Line = true    // underline draws
        delay(300)
        phase4Tag = true     // tagline + version
        delay(200)
        phase5Burst = true   // ring burst
        delay(800)
        onComplete()
    }

    // ── Logo stamp ──
    val logoScale by animateFloatAsState(
        targetValue = if (phase2Logo) 1f else 2.5f,
        animationSpec = spring(
            stiffness = CxAnim.Springs.stiffnessSlam,
            dampingRatio = CxAnim.Springs.dampingSlam
        ),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase2Logo) 1f else 0f,
        animationSpec = tween(80),
        label = "logoAlpha"
    )

    // v1.9.13 — gentle breathe cycle once the logo is settled. Scale drifts
    // 1.00 ↔ 1.02 over a 2.4s cycle so the brand mark feels alive without
    // distracting from the rest of the splash beats.
    val breatheTransition = rememberInfiniteTransition(label = "logoBreathe")
    val breatheRaw by breatheTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "logoBreathePhase"
    )
    val breatheScale = if (phase2Logo) 1f + 0.01f * sin(breatheRaw) else 1f

    // ── Center glow bloom ──
    val glowRadius by animateFloatAsState(
        targetValue = if (phase1Glow) 1f else 0f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "glowRadius"
    )

    // ── Underline draw ──
    val lineWidth by animateFloatAsState(
        targetValue = if (phase3Line) 1f else 0f,
        animationSpec = tween(CxAnim.durationSlow, easing = EaseOut),
        label = "lineWidth"
    )

    // ── Tagline + version fade ──
    val tagAlpha by animateFloatAsState(
        targetValue = if (phase4Tag) 1f else 0f,
        animationSpec = tween(400),
        label = "tagAlpha"
    )

    // ── Background particle opacity ──
    val bgAlpha by animateFloatAsState(
        targetValue = if (phase0Bg) 1f else 0f,
        animationSpec = tween(800),
        label = "bgAlpha"
    )

    // ── Ring burst ──
    val burstScale by animateFloatAsState(
        targetValue = if (phase5Burst) 3.5f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "burstScale"
    )
    val burstAlpha by animateFloatAsState(
        targetValue = if (phase5Burst) 0f else 0.7f,
        animationSpec = tween(900),
        label = "burstAlpha"
    )

    // ── Infinite scan line ──
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "meshPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CxPremium.surface0),
        contentAlignment = Alignment.Center
    ) {
        // ── Layer 0: Animated mesh background ──
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(bgAlpha * 0.5f)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    if (w <= 0f || h <= 0f) return@drawBehind
                    // Slow drifting color blobs
                    val blobs = listOf(
                        Triple(CxPremium.meshB, 0.3f + 0.1f * cos(scanPhase), 0.25f + 0.08f * sin(scanPhase * 1.3f)),
                        Triple(CxPremium.meshC, 0.7f + 0.1f * sin(scanPhase * 0.9f), 0.6f + 0.1f * cos(scanPhase)),
                        Triple(CxPremium.meshD, 0.5f + 0.12f * cos(scanPhase * 1.1f), 0.8f + 0.08f * sin(scanPhase * 0.7f)),
                    )
                    blobs.forEach { (color, fx, fy) ->
                        val r = minOf(w, h) * 0.6f
                        if (r > 0f) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(color.copy(alpha = 0.7f), Color.Transparent),
                                    center = Offset(w * fx, h * fy),
                                    radius = r
                                ),
                                radius = r,
                                center = Offset(w * fx, h * fy),
                                blendMode = BlendMode.Plus
                            )
                        }
                    }
                }
        )

        // ── Layer 1: Particle constellation ──
        ParticleField(
            modifier = Modifier
                .matchParentSize()
                .alpha(bgAlpha * 0.35f),
            particleCount = 45,
            color = CxPremium.neonLime,
            connectDistance = 120f
        )

        // ── Layer 2: Center radial glow ──
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = minOf(size.width, size.height) * 0.5f * glowRadius
                    if (r > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CxPremium.neonLime.copy(alpha = 0.18f * glowRadius),
                                    CxPremium.neonCyan.copy(alpha = 0.06f * glowRadius),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = r
                            ),
                            radius = r,
                            center = Offset(cx, cy),
                            blendMode = BlendMode.Plus
                        )
                    }
                }
        )

        // ── Layer 3: Scan line sweep ──
        ScanLineSweep(
            modifier = Modifier
                .matchParentSize()
                .alpha(bgAlpha * 0.6f),
            speed = 2400
        )

        // ── Layer 4: Ring burst ──
        if (phase5Burst) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = burstScale
                        scaleY = burstScale
                        alpha = burstAlpha
                    }
                    .drawBehind {
                        drawCircle(
                            color = CxPremium.neonLime,
                            radius = size.minDimension / 2f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                    }
            )
        }

        // ── Layer 5: Content ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.clex_app_logo),
                contentDescription = "Clex logo",
                modifier = Modifier
                    .size(190.dp)
                    .scale(logoScale * breatheScale)
                    .alpha(logoAlpha),
            )

            Spacer(Modifier.height(12.dp))

            // Accent underline draw
            Box(
                modifier = Modifier
                    .width(200.dp * lineWidth)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                CxPremium.neonLime.copy(alpha = 0.4f),
                                CxPremium.neonLime,
                                CxPremium.neonCyan,
                                CxPremium.neonLime.copy(alpha = 0.4f),
                            )
                        )
                    )
            )

            Spacer(Modifier.height(CxSpacing.xl))

            // Tagline
            MonoText(
                text = "DROP  ·  PREPARE  ·  SHARE",
                fontSize = CxTypography.textSm,
                color = CxColors.textSecondary,
                modifier = Modifier.alpha(tagAlpha),
                letterSpacing = CxTypography.textSm * 0.25,
                textAlign = TextAlign.Center
            )
        }

        // ── Layer 6: Vignette ──
        VignetteOverlay(
            modifier = Modifier.matchParentSize(),
            strength = 0.6f
        )

        // Version badge — bottom
        MonoText(
            text = "V${AppRelease.versionName}",
            fontSize = CxTypography.textXs,
            color = CxColors.textTertiary.copy(alpha = tagAlpha),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            letterSpacing = CxTypography.textXs * 0.2
        )
    }
}
