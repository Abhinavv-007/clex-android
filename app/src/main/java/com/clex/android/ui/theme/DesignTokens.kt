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
    // Dark mode (default)
    val bgPrimary = Color(0xFF0A0A0A)
    val bgSecondary = Color(0xFF111111)
    val bgTertiary = Color(0xFF1A1A1A)
    val bgCard = Color(0xFF141414)
    val bgCardHover = Color(0xFF1E1E1E)
    val bgElevated = Color(0xFF1A1A1A)
    val bgInput = Color(0xFF111111)

    val textPrimary = Color(0xFFF0F0E8)
    val textSecondary = Color(0xFF999999)
    val textTertiary = Color(0xFF666666)
    val textInverse = Color(0xFF0A0A0A)

    val accent = Color(0xFFC8FF00)          // Clex Yellow — the brand
    val accentHover = Color(0xFFD4FF33)
    val accentMuted = Color(0x26C8FF00)     // 15% opacity
    val accentSecondary = Color(0xFFFF3D00)  // Red-orange
    val accentSecondaryHover = Color(0xFFFF5722)
    val accentTertiary = Color(0xFF00D4FF)   // Cyan

    val borderColor = Color(0xFF2A2A2A)
    val borderBold = Color(0xFFF0F0E8)
    val borderSubtle = Color(0xFF1E1E1E)

    val shadowColor = Color(0xFF000000)

    val surfaceOverlay = Color(0x99000000)  // 60% black

    // Light mode
    val lightBgPrimary = Color(0xFFF5F0E8)
    val lightBgSecondary = Color(0xFFEBE5D9)
    val lightBgTertiary = Color(0xFFE0D9CC)
    val lightBgCard = Color(0xFFFFFFFF)
    val lightBgCardHover = Color(0xFFFAFAF5)
    val lightBgElevated = Color(0xFFFFFFFF)
    val lightBgInput = Color(0xFFF5F0E8)

    val lightTextPrimary = Color(0xFF0A0A0A)
    val lightTextSecondary = Color(0xFF555555)
    val lightTextTertiary = Color(0xFF888888)
    val lightTextInverse = Color(0xFFF0F0E8)

    val lightBorderColor = Color(0xFFD0C9BA)
    val lightBorderBold = Color(0xFF0A0A0A)
    val lightBorderSubtle = Color(0xFFE0D9CC)

    val lightShadowColor = Color(0xFF0A0A0A)

    // Feature-specific accent colors (from features page)
    val toolCyan = Color(0xFF22D3EE)
    val toolPurple = Color(0xFF9B7FFF)
    val toolAmber = Color(0xFFFFAA00)
    val toolGreen = Color(0xFF00E570)
    val toolRed = Color(0xFFFF4466)

    // Status
    val success = Color(0xFF00E570)
    val error = Color(0xFFFF3D00)
    val warning = Color(0xFFFFAA00)
    val info = Color(0xFF00D4FF)

    // Pure
    val black = Color(0xFF000000)
    val white = Color(0xFFFFFFFF)
    val pureBlack = Color(0xFF0A0A0A)
}

// ── TYPOGRAPHY TOKENS ──────────────────────────────

object CxTypography {
    // We use system monospace + geometric sans as Neo-Brutalist demands
    // Space Mono mapped to monospace system, Inter mapped to sans-serif
    val fontDisplay = FontFamily.Monospace  // Headings — raw, technical
    val fontBody = FontFamily.SansSerif     // Body — clean geometric
    val fontMono = FontFamily.Monospace     // Code, labels, data

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
