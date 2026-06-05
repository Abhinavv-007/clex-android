package com.clex.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  CLEX — Typography Components
//  Synced to clex.in: Geist display, mixed-case titles,
//  Pacifico cursive accent, JetBrains Mono for codes.
// ═══════════════════════════════════════════════════

@Composable
fun HeroTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textPrimary,
    fontSize: TextUnit = CxTypography.text5xl
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightExtrabold,
        lineHeight = fontSize * 0.98,
        letterSpacing = fontSize * -0.045
    )
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textPrimary,
    fontSize: TextUnit = CxTypography.text3xl
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightExtrabold,
        lineHeight = fontSize * 1.0,
        letterSpacing = fontSize * -0.04
    )
}

@Composable
fun CardTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textPrimary,
    fontSize: TextUnit = CxTypography.text2xl
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBold,
        lineHeight = fontSize * 1.05,
        letterSpacing = fontSize * -0.03
    )
}

/**
 * Cursive accent text (Pacifico). Used for the website's italic-accent words
 * like "stay private", "without the mess". Renders with a brand gradient brush.
 */
@Composable
fun CursiveAccent(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = CxTypography.text3xl
) {
    val brush = Brush.linearGradient(
        colors = if (CxTheme.colors.isDark)
            listOf(CxColors.cursiveStartDark, CxColors.cursiveMidDark, CxColors.cursiveEndDark)
        else
            listOf(CxColors.cursiveStart, CxColors.cursiveMid1, CxColors.cursiveMid2, CxColors.cursiveEnd)
    )
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontFamily = CxTypography.fontCursive,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal,
        lineHeight = fontSize * 1.3,
        style = androidx.compose.ui.text.TextStyle(brush = brush)
    )
}

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textSecondary,
    fontSize: TextUnit = CxTypography.textBase,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontBody,
        fontWeight = CxTypography.weightRegular,
        lineHeight = fontSize * 1.55,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

/**
 * Mono renderer — JetBrains Mono now (was system Monospace). Used for codes,
 * room IDs, API keys, status numbers. Drops uppercase forcing — caller decides.
 */
@Composable
fun MonoText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textPrimary,
    fontSize: TextUnit = CxTypography.textSm,
    fontWeight: FontWeight = CxTypography.weightSemibold,
    letterSpacing: TextUnit = CxTypography.textXs * 0.05,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontMono,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textTertiary
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = CxTypography.textXs,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBold,
        letterSpacing = CxTypography.textXs * 0.14
    )
}

@Composable
fun AccentNumber(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = CxTypography.text6xl
) {
    val brush = Brush.linearGradient(
        colors = listOf(CxColors.accent, CxColors.accentSecondary)
    )
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBlack,
        lineHeight = fontSize * 0.9,
        style = androidx.compose.ui.text.TextStyle(brush = brush)
    )
}

