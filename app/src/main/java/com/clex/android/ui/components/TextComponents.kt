package com.clex.android.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.clex.android.ui.theme.*

// ═══════════════════════════════════════════════════
//  CLEX — Typography Components
//  Monospace headings, geometric body, raw labels
// ═══════════════════════════════════════════════════

@Composable
fun HeroTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textPrimary,
    fontSize: TextUnit = CxTypography.text5xl
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBold,
        lineHeight = fontSize * 0.9,
        letterSpacing = fontSize * -0.06
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
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBold,
        lineHeight = fontSize * 0.92,
        letterSpacing = fontSize * -0.05
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
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBold,
        lineHeight = fontSize * 0.92,
        letterSpacing = fontSize * -0.045
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
        lineHeight = fontSize * 1.6,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

@Composable
fun MonoText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CxTheme.colors.textPrimary,
    fontSize: TextUnit = CxTypography.textSm,
    fontWeight: FontWeight = CxTypography.weightBold,
    letterSpacing: TextUnit = CxTypography.textXs * 0.1,
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
    color: Color = CxTheme.colors.accent
) {
    MonoText(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = CxTypography.textXs,
        letterSpacing = CxTypography.textXs * 0.2
    )
}

@Composable
fun AccentNumber(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = CxTypography.text6xl
) {
    Text(
        text = text,
        modifier = modifier,
        color = CxTheme.colors.accent,
        fontSize = fontSize,
        fontFamily = CxTypography.fontDisplay,
        fontWeight = CxTypography.weightBold,
        lineHeight = fontSize * 0.85
    )
}
