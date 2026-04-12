package com.clex.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clex.android.R
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography

@Composable
fun BrandLogoImage(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Image(
        painter = painterResource(id = R.drawable.clex_app_logo),
        contentDescription = "Clex logo",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
    )
}

@Composable
fun PageMark(
    glyph: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.76f else 0.92f))
                .border(
                    1.dp,
                    colors.accent.copy(alpha = if (colors.isDark) 0.32f else 0.48f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            MonoText(
                text = glyph,
                fontSize = CxTypography.textLg,
                fontWeight = CxTypography.weightBold,
                color = colors.accent
            )
        }
        Spacer(Modifier.width(CxSpacing.sm))
        MonoText(
            text = title,
            fontSize = CxTypography.textLg,
            fontWeight = CxTypography.weightBold,
            color = colors.textPrimary,
            letterSpacing = CxTypography.textXs * 0.1
        )
    }
}

@Composable
fun TopBarStatusChip(
    text: String,
    accentColor: Color = CxTheme.colors.accent,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
) {
    val colors = CxTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.bgCard.copy(alpha = if (colors.isDark) 0.74f else 0.92f))
            .border(
                1.dp,
                accentColor.copy(alpha = if (colors.isDark) 0.34f else 0.52f),
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
        }
        MonoText(
            text = text.uppercase(),
            fontSize = CxTypography.textXs,
            color = accentColor,
            letterSpacing = CxTypography.textXs * 0.1
        )
    }
}
