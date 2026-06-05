package com.clex.android.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

// ═══════════════════════════════════════════════════
//  CLEX — DESIGN TOKENS
//  Neo-Brutalist Design System for Android
//  Extracted from web source + creative expansion
// ═══════════════════════════════════════════════════

// ── COLOR TOKENS ──────────────────────────────────

object CxColors {
    // Dark mode (matches clex.in dark theme)
    val bgPrimary = Color(0xFF0A0A0A)
    val bgSecondary = Color(0xFF14121D)
    val bgTertiary = Color(0xFF1A1822)
    val bgCard = Color(0xFF15131E)
    val bgCardHover = Color(0xFF1F1B2D)
    val bgElevated = Color(0xFF1A1822)
    val bgInput = Color(0xFF111111)

    val textPrimary = Color(0xFFF4EEE0)
    val textSecondary = Color(0xFFCFC6B8)
    val textTertiary = Color(0xFF8A8478)
    val textInverse = Color(0xFF0A0A0A)

    // Pastel-on-dark accents (synced with clex.in)
    val accent = Color(0xFFC4B5FD)            // lavender (primary)
    val accentHover = Color(0xFFD3BBFF)
    val accentMuted = Color(0x33C4B5FD)
    val accentSecondary = Color(0xFFFF8A1F)   // peach/orange
    val accentSecondaryHover = Color(0xFFFFA655)
    val accentTertiary = Color(0xFF7EDC8B)    // mint

    val borderColor = Color(0x33FFFFFF)
    val borderBold = Color(0xFFF4EEE0)
    val borderSubtle = Color(0x14FFFFFF)

    val shadowColor = Color(0xFF000000)

    val surfaceOverlay = Color(0x99000000)

    // Light mode (matches clex.in cream palette)
    val lightBgPrimary = Color(0xFFF6EFDF)
    val lightBgSecondary = Color(0xFFEFE6CF)
    val lightBgTertiary = Color(0xFFEBE2C9)
    val lightBgCard = Color(0xFFFFFFFF)
    val lightBgCardHover = Color(0xFFFFFAEB)
    val lightBgElevated = Color(0xFFFFFFFF)
    val lightBgInput = Color(0xFFFAF5E8)

    val lightTextPrimary = Color(0xFF0E0E0D)
    val lightTextSecondary = Color(0xFF4A4642)
    val lightTextTertiary = Color(0xFF7A7670)
    val lightTextInverse = Color(0xFFFAF5E8)

    val lightBorderColor = Color(0xFFE3D9C0)
    val lightBorderBold = Color(0xFF0E0E0D)
    val lightBorderSubtle = Color(0xFFEBE2C9)

    val lightShadowColor = Color(0xFF0E0E0D)

    // Brand pastel palette
    val cream      = Color(0xFFF6EFDF)
    val creamSoft  = Color(0xFFFAF5E8)
    val creamDeep  = Color(0xFFEFE6CF)
    val lavender   = Color(0xFFC4B5FD)
    val lavender2  = Color(0xFFB9A8FB)
    val lavender3  = Color(0xFFA08AFF)
    val peach      = Color(0xFFFFD0B3)
    val peach2     = Color(0xFFFFBF99)
    val peach3     = Color(0xFFFF9D6E)
    val mint       = Color(0xFFB8E9C4)
    val mint2      = Color(0xFF8FDBA1)
    val pink       = Color(0xFFFFD1DC)
    val yellow     = Color(0xFFFFE27A)
    val yellow2    = Color(0xFFFFD13A)
    val blue       = Color(0xFFB5DCFF)
    val ink        = Color(0xFF0E0E0D)
    val inkSoft    = Color(0xFF2A2A28)

    // Cursive gradient stops (Pacifico accent words)
    val cursiveStart    = Color(0xFF8B5CF6)
    val cursiveMid1     = Color(0xFFD85F8B)
    val cursiveMid2     = Color(0xFFFF8A1F)
    val cursiveEnd      = Color(0xFFFFD46A)
    val cursiveStartDark = Color(0xFFD3BBFF)
    val cursiveMidDark   = Color(0xFFFFB9D6)
    val cursiveEndDark   = Color(0xFFFFD198)

    // Feature-specific accent colors
    val toolCyan = Color(0xFF25B6E8)
    val toolPurple = Color(0xFF8B5CF6)
    val toolAmber = Color(0xFFFF8A1F)
    val toolGreen = Color(0xFF34C759)
    val toolRed = Color(0xFFFF5F57)

    // Status
    val success = Color(0xFF34C759)
    val error = Color(0xFFFF5F57)
    val warning = Color(0xFFFFB13A)
    val info = Color(0xFF25B6E8)

    // Pure
    val black = Color(0xFF000000)
    val white = Color(0xFFFFFFFF)
    val pureBlack = Color(0xFF0E0E0D)
}

// ── TYPOGRAPHY TOKENS ──────────────────────────────

object CxTypography {
    // Font families.
    // - fontDisplay = Geist (system sans-serif fallback locally; downloadable
    //   font provider can be wired later in res/font/ + values/ when needed).
    // - fontBody = same family for unified voice (matches website).
    // - fontMono = JetBrains Mono fallback (system monospace) for codes/keys.
    // - fontCursive = Pacifico fallback (serif italic stand-in).
    val fontDisplay = FontFamily.SansSerif
    val fontBody = FontFamily.SansSerif
    val fontMono = FontFamily.Monospace
    val fontCursive = FontFamily.Cursive

    // Font sizes (mobile-optimized scale)
    val textXs = 11.sp
    val textSm = 13.sp
    val textBase = 15.sp
    val textLg = 17.sp
    val textXl = 19.sp
    val text2xl = 22.sp
    val text3xl = 28.sp
    val text4xl = 34.sp
    val text5xl = 42.sp
    val text6xl = 54.sp
    val text7xl = 68.sp
    val text8xl = 88.sp

    // Weights
    val weightRegular = FontWeight.W400
    val weightMedium = FontWeight.W500
    val weightSemibold = FontWeight.W600
    val weightBold = FontWeight.W700
    val weightExtrabold = FontWeight.W800
    val weightBlack = FontWeight.W900
}

// ── SPACING TOKENS ──────────────────────────────────

object CxSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
    val xxxxl = 96.dp

    // Screen edge padding
    val screenHorizontal = 20.dp
    val screenVertical = 24.dp

    // Section spacing
    val sectionGap = 56.dp
    val cardPadding = 24.dp
    val chipPadding = 12.dp
}

// ── BORDER TOKENS ───────────────────────────────────

object CxBorders {
    val thin = 2.dp
    val medium = 3.dp
    val thick = 4.dp
    val heavy = 5.dp
}

// ── SHADOW TOKENS (hard offsets only) ───────────────

data class HardShadow(val x: Dp, val y: Dp, val color: Color = CxColors.shadowColor)

object CxShadows {
    val sm = HardShadow(3.dp, 3.dp)
    val md = HardShadow(5.dp, 5.dp)
    val lg = HardShadow(8.dp, 8.dp)
    val xl = HardShadow(12.dp, 12.dp)

    // Pressed state — collapsed
    val pressed = HardShadow(1.dp, 1.dp)

    // Accent shadows
    fun accent(size: HardShadow = md) = size.copy(color = CxColors.accent)
}

// ── RADIUS TOKENS ───────────────────────────────────

object CxRadius {
    val none = 0.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val full = 999.dp  // pill shape
}

// ── ANIMATION TOKENS ────────────────────────────────

object CxAnim {
    // Durations (ms)
    const val durationFast = 150
    const val durationNormal = 300
    const val durationSlow = 500
    const val durationStatement = 800
    const val durationCinematic = 1200

    // Spring configs — different feel for different contexts
    object Springs {
        // Snappy UI response — buttons, chips, small elements
        const val stiffnessSnap = 800f
        const val dampingSnap = 0.7f

        // Default panel entrance — micro-app windows
        const val stiffnessPanel = 400f
        const val dampingPanel = 0.72f

        // Bouncy entrance — onboarding cards, success states
        const val stiffnessBounce = 300f
        const val dampingBounce = 0.55f

        // Gentle drift — idle animations, background elements
        const val stiffnessGentle = 150f
        const val dampingGentle = 0.85f

        // Slam — screen transitions, hero elements
        const val stiffnessSlam = 1200f
        const val dampingSlam = 0.65f

        // Physical depression — button press simulation
        const val stiffnessPress = 2000f
        const val dampingPress = 0.9f
    }

    // Stagger delays
    const val staggerDelay = 90L    // ms between staggered children
    const val microDelay = 50L

    // Idle animation durations
    const val floatDuration = 4000
    const val pulseDuration = 3000
    const val rotateDuration = 20000
    const val radarSweepDuration = 3000
    const val typingCursorDuration = 1000
    const val scanLineDuration = 2000
}

// ── Z-INDEX / ELEVATION TOKENS ──────────────────────

object CxElevation {
    val base = 0.dp
    val card = 2.dp
    val dropdown = 4.dp
    val sticky = 6.dp
    val overlay = 8.dp
    val modal = 12.dp
    val toast = 16.dp
}

// ═══════════════════════════════════════════════════
//  PREMIUM TOKENS — "million-dollar app" layer
//  Stacked on top of the brutalist base. These power
//  the cinematic splash, hero glow, mesh gradient
//  backgrounds, aurora/matrix effects, etc.
// ═══════════════════════════════════════════════════

object CxPremium {
    // — Neon glow palette —
    val neonLime = Color(0xFFC8FF00)
    val neonCyan = Color(0xFF00E5FF)
    val neonMagenta = Color(0xFFFF2BD6)
    val neonViolet = Color(0xFF8B5CF6)
    val neonOrange = Color(0xFFFF6A00)
    val neonPeach = Color(0xFFFFB07C)
    val neonMint = Color(0xFF32FFB7)
    val neonGold = Color(0xFFFFD166)
    val neonCoral = Color(0xFFFF4D6D)

    // — Dark layered surfaces (for premium depth) —
    val surface0 = Color(0xFF050505)
    val surface1 = Color(0xFF0B0B0F)
    val surface2 = Color(0xFF111117)
    val surface3 = Color(0xFF171722)
    val surface4 = Color(0xFF1F1F2E)
    val surface5 = Color(0xFF2A2A3A)

    // — Glass overlay tints (for frosted surfaces) —
    val glassLightTint = Color(0x14FFFFFF)
    val glassStrongTint = Color(0x22FFFFFF)
    val glassStroke = Color(0x33FFFFFF)
    val glassStrokeStrong = Color(0x55FFFFFF)

    // — Mesh gradient stops —
    val meshA = Color(0xFF0A0A0F)
    val meshB = Color(0xFF18122B)
    val meshC = Color(0xFF0F2A1D)
    val meshD = Color(0xFF1B1332)

    // — Aurora bands —
    val auroraLime = Color(0x66C8FF00)
    val auroraCyan = Color(0x5500E5FF)
    val auroraViolet = Color(0x558B5CF6)

    // — Gradient presets —
    val heroGradient: List<Color> = listOf(
        Color(0xFF0A0A0A),
        Color(0xFF0E1020),
        Color(0xFF0A0A0A),
    )
    val accentGradient: List<Color> = listOf(
        Color(0xFFC8FF00),
        Color(0xFF32FFB7),
        Color(0xFF00E5FF),
    )
    val fireGradient: List<Color> = listOf(
        Color(0xFFFF2BD6),
        Color(0xFFFF6A00),
        Color(0xFFFFD166),
    )
    val deepGradient: List<Color> = listOf(
        Color(0xFF18122B),
        Color(0xFF2A2A3A),
        Color(0xFF0A0A0F),
    )

    // — Glow specs —
    val glowSmall = 8.dp
    val glowMedium = 16.dp
    val glowLarge = 32.dp
    val glowHero = 56.dp

    // — Parallax depths —
    const val parallaxFar = 0.25f
    const val parallaxMid = 0.55f
    const val parallaxNear = 0.85f
}

// Motion presets tuned for premium feel
object CxMotion {
    const val heroStampMs = 900
    const val heroRevealMs = 1100
    const val particleLifeMs = 6000
    const val tickerSpeed = 28_000 // ms per full loop
    const val shimmerMs = 2200
    const val parallaxSpringStiffness = 220f
    const val tiltMaxDeg = 14f
}
