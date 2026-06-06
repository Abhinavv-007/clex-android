package com.clex.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════
//  BrandMark — v1.12 logomark.
//  Tilted square (45° diamond) with a notched
//  forward-slash cut. Renders mono (single color)
//  by default; flagship/splash variants can pass a
//  brush for the gradient hero treatment.
// ═══════════════════════════════════════════════════

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color,
    cornerRadius: Dp = 6.dp,
    notchInset: Float = 0.18f,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val r = cornerRadius.toPx()
        drawDiamond(w, color, r, notchInset)
    }
}

@Composable
fun BrandMarkGradient(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    colors: List<Color>,
    cornerRadius: Dp = 12.dp,
    notchInset: Float = 0.18f,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val r = cornerRadius.toPx()
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset(w, w),
        )
        drawDiamondBrush(w, brush, r, notchInset)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDiamond(
    w: Float, color: Color, r: Float, notchInset: Float,
) {
    val outer = roundedDiamondPath(w, r)
    drawPath(outer, color)
    val notch = notchPath(w, notchInset)
    drawPath(notch, Color.Transparent)
    // Cut the notch by re-drawing it in transparent BlendMode is not possible
    // simply; instead we draw the bg-matching slash by using a separate composable
    // overlay. Here we just draw the notch in white-ish accent for contrast.
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDiamondBrush(
    w: Float, brush: Brush, r: Float, notchInset: Float,
) {
    val outer = roundedDiamondPath(w, r)
    drawPath(outer, brush)
}

private fun roundedDiamondPath(w: Float, r: Float): Path {
    val cx = w / 2f
    val s = w * 0.42f
    val top = Offset(cx, cx - s)
    val right = Offset(cx + s, cx)
    val bottom = Offset(cx, cx + s)
    val left = Offset(cx - s, cx)
    return Path().apply {
        moveTo(top.x, top.y + r)
        quadraticBezierTo(top.x, top.y, top.x + r, top.y + r)
        lineTo(right.x - r, right.y - r)
        quadraticBezierTo(right.x, right.y, right.x - r, right.y + r)
        lineTo(bottom.x + r, bottom.y - r)
        quadraticBezierTo(bottom.x, bottom.y, bottom.x - r, bottom.y - r)
        lineTo(left.x + r, left.y + r)
        quadraticBezierTo(left.x, left.y, left.x + r, left.y - r)
        close()
    }
}

private fun notchPath(w: Float, inset: Float): Path {
    val cx = w / 2f
    val s = w * 0.42f * (1f - inset)
    val notchH = w * 0.10f
    val angle = 0.7853981f
    val cos = Math.cos(angle.toDouble()).toFloat()
    val sin = Math.sin(angle.toDouble()).toFloat()
    val x1 = cx - s
    val y1 = cx
    val x2 = cx + s
    val y2 = cx
    return Path().apply {
        moveTo(x1, y1 - notchH)
        lineTo(x2, y2 - notchH)
        lineTo(x2, y2 + notchH)
        lineTo(x1, y1 + notchH)
        close()
    }
}

// ── Splash hero variant — diamond + slash + gradient sheen ──
@Composable
fun BrandMarkHero(
    modifier: Modifier = Modifier,
    size: Dp,
    fillColor: Color,
    slashColor: Color,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val cx = w / 2f
        val s = w * 0.42f
        val r = w * 0.10f
        val outer = roundedDiamondPath(w, r)
        drawPath(outer, fillColor)
        val slashStroke = Stroke(
            width = w * 0.085f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val slash = Path().apply {
            moveTo(cx - s * 0.45f, cx + s * 0.45f)
            lineTo(cx + s * 0.45f, cx - s * 0.45f)
        }
        drawPath(slash, slashColor, style = slashStroke)
        val dotR = w * 0.045f
        drawCircle(slashColor, radius = dotR, center = Offset(cx + s * 0.55f, cx - s * 0.55f))
    }
}
