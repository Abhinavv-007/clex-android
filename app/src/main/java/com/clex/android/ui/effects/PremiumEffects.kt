package com.clex.android.ui.effects

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxPremium
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════
//  CLEX — Premium Effects Library
//  Cinematic layer for splash / hero / landing / vault
//  Pure Compose Canvas — no external libs
// ═══════════════════════════════════════════════════

// ── 1. MESH GRADIENT BACKGROUND ─────────────────────
//  Slowly animated 4-blob radial gradient. Gives the
//  whole screen an "alive" premium depth feel.

@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        CxPremium.meshA,
        CxPremium.meshB,
        CxPremium.meshC,
        CxPremium.meshD,
    ),
    speed: Int = 18_000,
    accentColor: Color = CxPremium.neonLime,
    accentStrength: Float = 0.22f,
) {
    val transition = rememberInfiniteTransition(label = "mesh")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(speed, easing = LinearEasing),
        ),
        label = "meshPhase",
    )

    Box(
        modifier = modifier
            .background(colors.first())
            .drawBehind {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind

                // 4 drifting blob centers
                val centers = listOf(
                    Offset(
                        w * (0.25f + 0.10f * cos(phase)),
                        h * (0.30f + 0.08f * sin(phase * 1.3f)),
                    ),
                    Offset(
                        w * (0.75f + 0.10f * cos(phase * 1.1f + 1f)),
                        h * (0.35f + 0.10f * sin(phase * 0.9f)),
                    ),
                    Offset(
                        w * (0.35f + 0.12f * sin(phase * 0.8f)),
                        h * (0.75f + 0.10f * cos(phase * 1.2f)),
                    ),
                    Offset(
                        w * (0.80f + 0.08f * cos(phase * 0.7f)),
                        h * (0.80f + 0.10f * sin(phase * 1.4f + 2f)),
                    ),
                )
                val palette = listOf(
                    colors.getOrElse(1) { colors.last() },
                    colors.getOrElse(2) { colors.last() },
                    colors.getOrElse(3) { colors.last() },
                    accentColor.copy(alpha = accentStrength),
                )
                centers.forEachIndexed { index, center ->
                    val radius = minOf(w, h) * (0.55f + 0.08f * sin(phase + index))
                    if (radius > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    palette[index].copy(alpha = 0.65f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = radius,
                            ),
                            radius = radius,
                            center = center,
                            blendMode = BlendMode.Plus,
                        )
                    }
                }
            },
    )
}

// ── 2. AURORA BANDS ─────────────────────────────────
//  Three softly drifting horizontal aurora bands.
//  Great for above the hero.

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    band1: Color = CxPremium.auroraLime,
    band2: Color = CxPremium.auroraCyan,
    band3: Color = CxPremium.auroraViolet,
    speed: Int = 12_000,
) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(speed, easing = LinearEasing)),
        label = "auroraPhase",
    )

    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@drawBehind
            val bands = listOf(
                Triple(band1, 0.22f, phase),
                Triple(band2, 0.48f, phase * 1.3f + 2f),
                Triple(band3, 0.72f, phase * 0.8f + 4f),
            )
            bands.forEach { (color, yFrac, p) ->
                val centerY = h * (yFrac + 0.06f * sin(p))
                val radius = w * (0.85f + 0.05f * sin(p * 1.4f))
                if (radius > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color, Color.Transparent),
                            center = Offset(w / 2f + w * 0.12f * cos(p), centerY),
                            radius = radius,
                        ),
                        radius = radius,
                        center = Offset(w / 2f + w * 0.12f * cos(p), centerY),
                        blendMode = BlendMode.Plus,
                    )
                }
            }
        },
    )
}

// ── 3. PARTICLE FIELD ───────────────────────────────
//  Floating dots with line-connections between nearby
//  particles. "constellation" style background.

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val alpha: Float,
)

@Composable
fun ParticleField(
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    color: Color = CxPremium.neonLime,
    connectDistance: Float = 140f,
    seed: Long = 42L,
) {
    val rng = remember(seed) { Random(seed) }
    val particles = remember {
        List(particleCount) {
            Particle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                vx = (rng.nextFloat() - 0.5f) * 0.00025f,
                vy = (rng.nextFloat() - 0.5f) * 0.00025f,
                radius = 1.4f + rng.nextFloat() * 2.2f,
                alpha = 0.4f + rng.nextFloat() * 0.5f,
            )
        }.toMutableList()
    }

    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) last = now
                val dt = ((now - last) / 1_000_000f).coerceAtMost(32f)
                last = now
                particles.forEach { p ->
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    if (p.x < 0f || p.x > 1f) p.vx = -p.vx
                    if (p.y < 0f || p.y > 1f) p.vy = -p.vy
                }
                tick = now
            }
        }
    }

    Box(
        modifier = modifier.drawBehind {
            @Suppress("UNUSED_EXPRESSION") tick
            val w = size.width
            val h = size.height
            // lines between close particles
            for (i in particles.indices) {
                val a = particles[i]
                for (j in (i + 1) until particles.size) {
                    val b = particles[j]
                    val dx = (a.x - b.x) * w
                    val dy = (a.y - b.y) * h
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist < connectDistance) {
                        val t = 1f - (dist / connectDistance)
                        drawLine(
                            color = color.copy(alpha = 0.22f * t),
                            start = Offset(a.x * w, a.y * h),
                            end = Offset(b.x * w, b.y * h),
                            strokeWidth = 1f,
                        )
                    }
                }
            }
            particles.forEach { p ->
                drawCircle(
                    color = color.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(p.x * w, p.y * h),
                )
            }
        },
    )
}

// ── 4. MATRIX RAIN ──────────────────────────────────
//  Vertical falling glyphs in columns. Colored lime.

private data class MatrixColumn(
    var y: Float,
    val speed: Float,
    val chars: MutableList<Char>,
    val maxLen: Int,
)

@Composable
fun MatrixRain(
    modifier: Modifier = Modifier,
    color: Color = CxPremium.neonLime,
    density: Float = 0.8f,
    fontSize: TextUnit = 14.sp,
    alpha: Float = 0.75f,
) {
    val charset = remember {
        ("01アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン").toCharArray()
    }
    val rng = remember { Random(91L) }
    var columns by remember { mutableStateOf<List<MatrixColumn>>(emptyList()) }
    var canvasH by remember { mutableStateOf(0f) }
    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) last = now
                val dt = (now - last) / 1_000_000f
                last = now
                columns.forEach { col ->
                    col.y += col.speed * dt * 0.055f
                    if (col.y > canvasH + 40f) {
                        col.y = -rng.nextFloat() * canvasH * 0.6f
                        while (col.chars.size < col.maxLen) col.chars.add(charset[rng.nextInt(charset.size)])
                    }
                    if (rng.nextFloat() < 0.08f && col.chars.isNotEmpty()) {
                        col.chars[rng.nextInt(col.chars.size)] = charset[rng.nextInt(charset.size)]
                    }
                }
                tick = now
            }
        }
    }

    Box(
        modifier = modifier.drawWithCache {
            val charW = fontSize.toPx() * 0.8f
            val charH = fontSize.toPx() * 1.15f
            val columnCount = (size.width / charW * density).toInt().coerceAtLeast(6)
            if (columns.size != columnCount || canvasH != size.height) {
                canvasH = size.height
                columns = List(columnCount) {
                    val maxLen = 6 + rng.nextInt(14)
                    MatrixColumn(
                        y = -rng.nextFloat() * size.height,
                        speed = 28f + rng.nextFloat() * 55f,
                        chars = MutableList(maxLen) { charset[rng.nextInt(charset.size)] },
                        maxLen = maxLen,
                    )
                }
            }
            onDrawBehind {
                @Suppress("UNUSED_EXPRESSION") tick
                columns.forEachIndexed { index, col ->
                    val x = index * charW + charW * 0.5f
                    col.chars.forEachIndexed { ci, c ->
                        val y = col.y - ci * charH
                        if (y in -charH..size.height) {
                            val fade = 1f - (ci.toFloat() / col.maxLen).coerceIn(0f, 1f)
                            val head = ci == 0
                            val col2 = if (head) Color.White else color
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    this.color = col2.copy(alpha = alpha * fade).toArgb()
                                    this.textSize = fontSize.toPx()
                                    this.isAntiAlias = true
                                    this.typeface = android.graphics.Typeface.MONOSPACE
                                }
                                drawText(c.toString(), x, y, paint)
                            }
                        }
                    }
                }
            }
        },
    )
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)

// ── 5. GLITCH TEXT ──────────────────────────────────
//  Text with RGB-split glitch offsets + occasional
//  scrambled character swaps. Perfect for hero titles.

@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp,
    fontWeight: FontWeight = FontWeight.Black,
    color: Color = CxColors.textPrimary,
    glitchA: Color = CxPremium.neonMagenta,
    glitchB: Color = CxPremium.neonCyan,
    intensity: Float = 1f,
    fontFamily: FontFamily = FontFamily.Monospace,
) {
    val transition = rememberInfiniteTransition(label = "glitch")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "glitchT",
    )

    // scrambled variant changes every ~220ms
    var scrambleTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(220L)
            scrambleTick++
        }
    }
    val scrambled by remember(text, scrambleTick) {
        derivedStateOf {
            val pool = "!@#\$%^&*/\\{}[]01"
            buildString {
                text.forEach { ch ->
                    if (ch != ' ' && ch != '\n' && Random.nextFloat() < 0.04f * intensity) {
                        append(pool[Random.nextInt(pool.length)])
                    } else append(ch)
                }
            }
        }
    }

    val offA = (sin(t * 2f * PI.toFloat()) * 4f * intensity)
    val offB = (cos(t * 2f * PI.toFloat() * 1.4f) * 4f * intensity)

    Box(modifier = modifier) {
        val style = TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            lineHeight = fontSize * 0.92f,
            letterSpacing = (-0.04).em(fontSize),
        )
        // ghost layer A
        Text(
            text = scrambled,
            style = style,
            color = glitchA.copy(alpha = 0.55f),
            modifier = Modifier.drawWithContent {
                translate(offA, -offA / 2f) { this@drawWithContent.drawContent() }
            },
        )
        // ghost layer B
        Text(
            text = scrambled,
            style = style,
            color = glitchB.copy(alpha = 0.55f),
            modifier = Modifier.drawWithContent {
                translate(-offB, offB / 2f) { this@drawWithContent.drawContent() }
            },
        )
        // main
        Text(
            text = text,
            style = style,
            color = color,
        )
    }
}

private fun Double.em(font: TextUnit): TextUnit = (font.value * this).toFloat().sp
private inline fun DrawScope.translate(
    dx: Float,
    dy: Float,
    block: DrawScope.() -> Unit,
) {
    drawContext.transform.translate(dx, dy)
    block()
    drawContext.transform.translate(-dx, -dy)
}

// ── 6. SHIMMER MODIFIER ─────────────────────────────
//  Diagonal highlight sweep — used on cards / buttons.

fun Modifier.premiumShimmer(
    color: Color = Color.White.copy(alpha = 0.14f),
    durationMs: Int = 2200,
    angleDeg: Float = 18f,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val t by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerT",
    )
    this.drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        val x = t * w
        val stripe = w * 0.22f
        val rad = Math.toRadians(angleDeg.toDouble())
        val offX = (stripe * sin(rad)).toFloat()
        val brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, color, Color.Transparent),
            start = Offset(x - stripe / 2f, 0f),
            end = Offset(x + stripe / 2f + offX, h),
        )
        drawRect(brush = brush, blendMode = BlendMode.Plus)
    }
}

// ── 7. NOISE GRAIN OVERLAY ──────────────────────────
//  Static film-grain. Cheap, but adds texture.

@Composable
fun NoiseGrainOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.05f,
    density: Int = 1800,
) {
    val rng = remember { Random(17L) }
    val dots = remember {
        List(density) { Offset(rng.nextFloat(), rng.nextFloat()) to (rng.nextFloat() * alpha) }
    }
    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height
            dots.forEach { (p, a) ->
                drawCircle(
                    color = Color.White.copy(alpha = a),
                    radius = 0.8f,
                    center = Offset(p.x * w, p.y * h),
                )
            }
        },
    )
}

// ── 8. SCAN LINE SWEEP ──────────────────────────────
//  Horizontal CRT-style line moving top→bottom.

@Composable
fun ScanLineSweep(
    modifier: Modifier = Modifier,
    color: Color = CxPremium.neonLime,
    speed: Int = 3200,
) {
    val transition = rememberInfiniteTransition(label = "scan")
    val y by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(speed, easing = LinearEasing)),
        label = "scanY",
    )
    Box(
        modifier = modifier.drawBehind {
            val yy = size.height * y
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.08f),
                        color.copy(alpha = 0.22f),
                        color.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    startY = yy - 40f,
                    endY = yy + 40f,
                ),
                topLeft = Offset(0f, yy - 40f),
                size = Size(size.width, 80f),
            )
        },
    )
}

// ── 9. RADIAL GLOW SPOT ─────────────────────────────

@Composable
fun RadialGlowSpot(
    modifier: Modifier = Modifier,
    color: Color = CxPremium.neonLime,
    centerFracX: Float = 0.5f,
    centerFracY: Float = 0.5f,
    radiusFrac: Float = 0.7f,
    intensity: Float = 0.7f,
) {
    Box(
        modifier = modifier.drawBehind {
            val cx = size.width * centerFracX
            val cy = size.height * centerFracY
            val r = minOf(size.width, size.height) * radiusFrac
            if (r > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = intensity), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )
            }
        },
    )
}

// ── 10. PULSING RING ────────────────────────────────
//  Concentric rings expanding outward — "radar ping".

@Composable
fun PulsingRings(
    modifier: Modifier = Modifier,
    color: Color = CxPremium.neonLime,
    ringCount: Int = 3,
    durationMs: Int = 2800,
    maxRadius: Float = 140f,
) {
    val transition = rememberInfiniteTransition(label = "ring")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMs, easing = LinearEasing)),
        label = "ringT",
    )
    Box(
        modifier = modifier
            .size((maxRadius * 2).dp)
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                for (i in 0 until ringCount) {
                    val offset = (i.toFloat() / ringCount)
                    val localT = ((t + offset) % 1f)
                    val r = localT * maxRadius
                    val alpha = (1f - localT).pow(1.4f) * 0.65f
                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.4f),
                    )
                }
            },
    )
}

// ── 11. WAVEFORM STRIP ──────────────────────────────
//  Horizontal audio-like bars — decorative "live data".

@Composable
fun Waveform(
    modifier: Modifier = Modifier,
    color: Color = CxPremium.neonLime,
    barCount: Int = 28,
    speed: Int = 1200,
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(speed, easing = LinearEasing)),
        label = "wavePhase",
    )
    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height
            val barW = w / (barCount * 1.6f)
            val gap = (w - barW * barCount) / (barCount + 1)
            for (i in 0 until barCount) {
                val p = phase + i * 0.32f
                val amp = (0.4f + 0.6f * (sin(p) * 0.5f + 0.5f)).coerceIn(0.15f, 1f)
                val bh = h * amp
                val x = gap + i * (barW + gap)
                val y = (h - bh) / 2f
                drawRect(
                    color = color.copy(alpha = 0.25f + 0.55f * amp),
                    topLeft = Offset(x, y),
                    size = Size(barW, bh),
                )
            }
        },
    )
}

// ── 12. CRT VIGNETTE + BORDER ───────────────────────

@Composable
fun VignetteOverlay(
    modifier: Modifier = Modifier,
    strength: Float = 0.55f,
) {
    Box(
        modifier = modifier.drawBehind {
            val radius = maxOf(size.width, size.height) * 0.75f
            if (radius > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = strength)),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = radius,
                    ),
                )
            }
        },
    )
}
