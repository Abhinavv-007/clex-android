package com.clex.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clex.android.R

// ═══════════════════════════════════════════════════
//  BrandMark — v1.15 logomark.
//  Renders the official artwork (gem-mosaic Clex `C`)
//  from drawable-nodpi/ic_brand_logo.png, with a dark
//  variant in drawable-night-nodpi. All previous
//  composables (BrandMark / BrandMarkGradient /
//  BrandMarkHero) now resolve to the same bitmap so
//  every surface ships the real logo.
// ═══════════════════════════════════════════════════

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Color.Unspecified,
    cornerRadius: Dp = (size.value * 0.22f).dp,
    notchInset: Float = 0f,
) {
    Image(
        painter = painterResource(id = R.drawable.ic_brand_logo),
        contentDescription = "Clex",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius)),
    )
}

@Composable
fun BrandMarkGradient(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    colors: List<Color> = emptyList(),
    cornerRadius: Dp = (size.value * 0.22f).dp,
    notchInset: Float = 0f,
) {
    BrandMark(modifier = modifier, size = size, cornerRadius = cornerRadius)
}

@Composable
fun BrandMarkHero(
    modifier: Modifier = Modifier,
    size: Dp,
    fillColor: Color = Color.Unspecified,
    slashColor: Color = Color.Unspecified,
) {
    BrandMark(
        modifier = modifier,
        size = size,
        cornerRadius = (size.value * 0.22f).dp,
    )
}
