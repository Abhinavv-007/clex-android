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
import com.clex.android.ui.components.CursiveAccent
import com.clex.android.ui.components.LiquidMeshBackground
import com.clex.android.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

// ═══════════════════════════════════════════════════
//  Splash — liquid glass cinematic intro.
//  Mesh gradient floor → logo bloom → cursive tagline
//  → handoff. Mirrors clex.in hero opening.
// ═══════════════════════════════════════════════════

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    val colors = CxTheme.colors

    var phaseMesh by remember { mutableStateOf(false) }
    var phaseLogo by remember { mutableStateOf(false) }
    var phaseCursive by remember { mutableStateOf(false) }
    var phaseTag by remember { mutableStateOf(false) }
    var phaseRing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80); phaseMesh = true
        delay(220); phaseLogo = true
        delay(420); phaseCursive = true
        delay(280); phaseTag = true
        delay(220); phaseRing = true
        delay(700); onComplete()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (phaseLogo) 1f else 0.6f,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.55f),
        label = "logoScale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (phaseLogo) 1f else 0f,
        animationSpec = tween(280),
        label = "logoAlpha",
    )
    val cursiveAlpha by animateFloatAsState(
        targetValue = if (phaseCursive) 1f else 0f,
        animationSpec = tween(420),
        label = "cursiveAlpha",
    )
    val cursiveLift by animateFloatAsState(
        targetValue = if (phaseCursive) 0f else 14f,
        animationSpec = tween(420, easing = EaseOutCubic),
        label = "cursiveLift",
    )
    val tagAlpha by animateFloatAsState(
        targetValue = if (phaseTag) 1f else 0f,
        animationSpec = tween(380),
        label = "tagAlpha",
    )
    val ringScale by animateFloatAsState(
        targetValue = if (phaseRing) 4.5f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "ringScale",
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (phaseRing) 0f else 0.55f,
        animationSpec = tween(700),
        label = "ringAlpha",
    )

    val infinite = rememberInfiniteTransition(label = "splashIdle")
    val breathe by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "breathe",
    )
    val breatheScale = if (phaseLogo) 1f + 0.012f * sin(breathe) else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (colors.isDark) CxColors.bgPrimary else CxColors.cream),
        contentAlignment = Alignment.Center,
    ) {
        // Layer 0 — mesh gradient floor
        if (phaseMesh) {
            LiquidMeshBackground(
                modifier = Modifier.matchParentSize(),
                intensity = 1.1f,
            )
        }

        // Layer 1 — radial bloom under logo
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = minOf(size.width, size.height) * 0.55f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CxColors.lavender.copy(alpha = 0.32f * logoAlpha),
                                CxColors.peach.copy(alpha = 0.22f * logoAlpha),
                                Color.Transparent,
                            ),
                            center = Offset(cx, cy),
                            radius = r,
                        ),
                        center = Offset(cx, cy),
                        radius = r,
                        blendMode = BlendMode.Plus,
                    )
                }
        )

        // Layer 2 — ring burst on handoff
        if (phaseRing) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = ringScale
                        scaleY = ringScale
                        alpha = ringAlpha
                    }
                    .drawBehind {
                        drawCircle(
                            color = CxColors.lavender,
                            radius = size.minDimension / 2f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                        )
                    }
            )
        }

        // Layer 3 — content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(
                    id = if (colors.isDark) R.drawable.clex_logo_light
                    else R.drawable.clex_logo_dark
                ),
                contentDescription = "Clex",
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale * breatheScale)
                    .alpha(logoAlpha),
            )

            Spacer(Modifier.height(18.dp))

            // Cursive accent — "made for you"
            Box(
                modifier = Modifier
                    .alpha(cursiveAlpha)
                    .graphicsLayer { translationY = cursiveLift },
            ) {
                CursiveAccent(
                    text = "made for you.",
                    fontSize = CxTypography.text2xl,
                )
            }

            Spacer(Modifier.height(8.dp))

            androidx.compose.material3.Text(
                text = "drop · prepare · share",
                fontSize = CxTypography.textSm,
                fontFamily = CxTypography.fontDisplay,
                fontWeight = CxTypography.weightMedium,
                color = colors.textTertiary,
                letterSpacing = CxTypography.textSm * 0.18,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(tagAlpha),
            )
        }

        // Version badge
        androidx.compose.material3.Text(
            text = "v${AppRelease.versionName}",
            fontSize = CxTypography.textXs,
            fontFamily = CxTypography.fontDisplay,
            fontWeight = CxTypography.weightMedium,
            color = colors.textTertiary.copy(alpha = tagAlpha * 0.7f),
            letterSpacing = CxTypography.textXs * 0.2,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
    }
}
