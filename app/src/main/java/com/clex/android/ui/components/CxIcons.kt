package com.clex.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════
//  CxIcons — hand-rolled vector glyphs.
//  24dp, 1.8dp stroke, rounded caps/joins. Pastel
//  cinematic look matching clex.in.
// ═══════════════════════════════════════════════════

@Composable
fun CxIcon(
    icon: CxIconType,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color,
    strokeWidth: Dp = 1.8.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = strokeWidth.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            CxIconType.HOME -> drawHome(w, h, color, stroke)
            CxIconType.VAULT -> drawVault(w, h, color, stroke)
            CxIconType.CHAIN -> drawChain(w, h, color, stroke)
            CxIconType.SETTINGS -> drawSettings(w, h, color, stroke)
            CxIconType.MENU -> drawMenu(w, h, color, stroke)
            CxIconType.CLOSE -> drawClose(w, h, color, stroke)
            CxIconType.CHEVRON_RIGHT -> drawChevronRight(w, h, color, stroke)
            CxIconType.PLUS -> drawPlus(w, h, color, stroke)
            CxIconType.SHARE -> drawShare(w, h, color, stroke)
            CxIconType.MOON -> drawMoon(w, h, color, stroke)
            CxIconType.SUN -> drawSun(w, h, color, stroke)
            CxIconType.QUESTION -> drawQuestion(w, h, color, stroke)
            CxIconType.LOCK -> drawLock(w, h, color, stroke)
            CxIconType.LINK -> drawLink(w, h, color, stroke)
            CxIconType.UPLOAD -> drawUpload(w, h, color, stroke)
            CxIconType.DOWNLOAD -> drawDownload(w, h, color, stroke)
            CxIconType.SPARKLE -> drawSparkle(w, h, color, stroke)
            CxIconType.SHIELD -> drawShield(w, h, color, stroke)
            CxIconType.CLOUD -> drawCloud(w, h, color, stroke)
            CxIconType.NOTE -> drawNote(w, h, color, stroke)
        }
    }
}

enum class CxIconType {
    HOME, VAULT, CHAIN, SETTINGS,
    MENU, CLOSE, CHEVRON_RIGHT, PLUS,
    SHARE, MOON, SUN, QUESTION,
    LOCK, LINK, UPLOAD, DOWNLOAD,
    SPARKLE, SHIELD, CLOUD, NOTE,
}

// All paths normalized to a 24x24 viewBox, then scaled to (w, h).

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHome(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(3f * sx, 11f * sy)
        lineTo(12f * sx, 3f * sy)
        lineTo(21f * sx, 11f * sy)
        lineTo(21f * sx, 20f * sy)
        lineTo(15f * sx, 20f * sy)
        lineTo(15f * sx, 14f * sy)
        lineTo(9f * sx, 14f * sy)
        lineTo(9f * sx, 20f * sy)
        lineTo(3f * sx, 20f * sy)
        close()
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVault(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    // Body box
    val body = Path().apply {
        moveTo(4f * sx, 10f * sy)
        lineTo(20f * sx, 10f * sy)
        lineTo(20f * sx, 21f * sy)
        lineTo(4f * sx, 21f * sy)
        close()
    }
    drawPath(body, c, style = s)
    // Shackle
    val shackle = Path().apply {
        moveTo(8f * sx, 10f * sy)
        lineTo(8f * sx, 7f * sy)
        cubicTo(
            8f * sx, 4.8f * sy, 9.8f * sx, 3f * sy, 12f * sx, 3f * sy,
        )
        cubicTo(
            14.2f * sx, 3f * sy, 16f * sx, 4.8f * sy, 16f * sx, 7f * sy,
        )
        lineTo(16f * sx, 10f * sy)
    }
    drawPath(shackle, c, style = s)
    // Keyhole dot
    drawCircle(c, radius = 1.4f * sx, center = Offset(12f * sx, 14.5f * sy))
    // Keyhole stem
    drawLine(c, Offset(12f * sx, 15.5f * sy), Offset(12f * sx, 18f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChain(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    // Two interlocked rounded rects rotated 45°
    val left = Path().apply {
        moveTo(8f * sx, 12f * sy)
        cubicTo(8f * sx, 8.5f * sy, 11f * sx, 5.5f * sy, 14.5f * sx, 5.5f * sy)
        cubicTo(18f * sx, 5.5f * sy, 21f * sx, 8.5f * sy, 21f * sx, 12f * sy)
    }
    drawPath(left, c, style = s)
    val right = Path().apply {
        moveTo(16f * sx, 12f * sy)
        cubicTo(16f * sx, 15.5f * sy, 13f * sx, 18.5f * sy, 9.5f * sx, 18.5f * sy)
        cubicTo(6f * sx, 18.5f * sy, 3f * sx, 15.5f * sy, 3f * sx, 12f * sy)
    }
    drawPath(right, c, style = s)
    drawLine(c, Offset(10f * sx, 12f * sy), Offset(14f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSettings(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val cx = 12f * sx; val cy = 12f * sy
    // Outer gear teeth — 8 spokes
    for (i in 0 until 8) {
        val a = Math.toRadians((i * 45).toDouble())
        val r1 = 7.5f * sx
        val r2 = 10f * sx
        drawLine(
            c,
            Offset(cx + (r1 * Math.cos(a)).toFloat(), cy + (r1 * Math.sin(a)).toFloat()),
            Offset(cx + (r2 * Math.cos(a)).toFloat(), cy + (r2 * Math.sin(a)).toFloat()),
            strokeWidth = s.width,
            cap = StrokeCap.Round,
        )
    }
    drawCircle(c, radius = 5.5f * sx, center = Offset(cx, cy), style = s)
    drawCircle(c, radius = 1.8f * sx, center = Offset(cx, cy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMenu(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(4f * sx, 7f * sy), Offset(20f * sx, 7f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(4f * sx, 12f * sy), Offset(20f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(4f * sx, 17f * sy), Offset(14f * sx, 17f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClose(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(6f * sx, 6f * sy), Offset(18f * sx, 18f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(18f * sx, 6f * sy), Offset(6f * sx, 18f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChevronRight(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(9f * sx, 6f * sy)
        lineTo(15f * sx, 12f * sy)
        lineTo(9f * sx, 18f * sy)
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlus(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(12f * sx, 5f * sy), Offset(12f * sx, 19f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(5f * sx, 12f * sy), Offset(19f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShare(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawCircle(c, radius = 2.4f * sx, center = Offset(6f * sx, 12f * sy), style = s)
    drawCircle(c, radius = 2.4f * sx, center = Offset(18f * sx, 6f * sy), style = s)
    drawCircle(c, radius = 2.4f * sx, center = Offset(18f * sx, 18f * sy), style = s)
    drawLine(c, Offset(8.2f * sx, 11f * sy), Offset(15.8f * sx, 7f * sy), strokeWidth = s.width)
    drawLine(c, Offset(8.2f * sx, 13f * sy), Offset(15.8f * sx, 17f * sy), strokeWidth = s.width)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMoon(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(20f * sx, 14f * sy)
        cubicTo(19f * sx, 17.5f * sy, 15.5f * sx, 20f * sy, 12f * sx, 20f * sy)
        cubicTo(7.6f * sx, 20f * sy, 4f * sx, 16.4f * sy, 4f * sx, 12f * sy)
        cubicTo(4f * sx, 8.5f * sy, 6.5f * sx, 5f * sy, 10f * sx, 4f * sy)
        cubicTo(8.5f * sx, 8f * sy, 10f * sx, 13f * sy, 14f * sx, 14.5f * sy)
        cubicTo(16f * sx, 15.2f * sy, 18.5f * sx, 14.8f * sy, 20f * sx, 14f * sy)
        close()
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSun(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val cx = 12f * sx; val cy = 12f * sy
    drawCircle(c, radius = 4f * sx, center = Offset(cx, cy), style = s)
    for (i in 0 until 8) {
        val a = Math.toRadians((i * 45).toDouble())
        val r1 = 6.5f * sx
        val r2 = 9f * sx
        drawLine(
            c,
            Offset(cx + (r1 * Math.cos(a)).toFloat(), cy + (r1 * Math.sin(a)).toFloat()),
            Offset(cx + (r2 * Math.cos(a)).toFloat(), cy + (r2 * Math.sin(a)).toFloat()),
            strokeWidth = s.width,
            cap = StrokeCap.Round,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawQuestion(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawCircle(c, radius = 9f * sx, center = Offset(12f * sx, 12f * sy), style = s)
    val q = Path().apply {
        moveTo(9f * sx, 10f * sy)
        cubicTo(9f * sx, 8f * sy, 10.5f * sx, 7f * sy, 12f * sx, 7f * sy)
        cubicTo(13.5f * sx, 7f * sy, 15f * sx, 8f * sy, 15f * sx, 10f * sy)
        cubicTo(15f * sx, 12f * sy, 12f * sx, 12f * sy, 12f * sx, 14f * sy)
    }
    drawPath(q, c, style = s)
    drawCircle(c, radius = 0.9f * sx, center = Offset(12f * sx, 17f * sy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLock(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    drawVault(w, h, c, s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLink(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val a = Path().apply {
        moveTo(10f * sx, 14f * sy)
        cubicTo(8f * sx, 16f * sy, 5f * sx, 16f * sy, 3f * sx, 14f * sy)
        cubicTo(1f * sx, 12f * sy, 1f * sx, 9f * sy, 3f * sx, 7f * sy)
        lineTo(7f * sx, 3f * sy)
        cubicTo(9f * sx, 1f * sy, 12f * sx, 1f * sy, 14f * sx, 3f * sy)
    }
    drawPath(a, c, style = s)
    val b = Path().apply {
        moveTo(14f * sx, 10f * sy)
        cubicTo(16f * sx, 8f * sy, 19f * sx, 8f * sy, 21f * sx, 10f * sy)
        cubicTo(23f * sx, 12f * sy, 23f * sx, 15f * sy, 21f * sx, 17f * sy)
        lineTo(17f * sx, 21f * sy)
        cubicTo(15f * sx, 23f * sy, 12f * sx, 23f * sy, 10f * sx, 21f * sy)
    }
    drawPath(b, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUpload(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(12f * sx, 4f * sy), Offset(12f * sx, 16f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val arrow = Path().apply {
        moveTo(7f * sx, 9f * sy)
        lineTo(12f * sx, 4f * sy)
        lineTo(17f * sx, 9f * sy)
    }
    drawPath(arrow, c, style = s)
    drawLine(c, Offset(4f * sx, 20f * sy), Offset(20f * sx, 20f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDownload(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(12f * sx, 4f * sy), Offset(12f * sx, 16f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val arrow = Path().apply {
        moveTo(7f * sx, 11f * sy)
        lineTo(12f * sx, 16f * sy)
        lineTo(17f * sx, 11f * sy)
    }
    drawPath(arrow, c, style = s)
    drawLine(c, Offset(4f * sx, 20f * sy), Offset(20f * sx, 20f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(12f * sx, 3f * sy)
        lineTo(13.5f * sx, 10.5f * sy)
        lineTo(21f * sx, 12f * sy)
        lineTo(13.5f * sx, 13.5f * sy)
        lineTo(12f * sx, 21f * sy)
        lineTo(10.5f * sx, 13.5f * sy)
        lineTo(3f * sx, 12f * sy)
        lineTo(10.5f * sx, 10.5f * sy)
        close()
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShield(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(12f * sx, 3f * sy)
        lineTo(20f * sx, 6f * sy)
        lineTo(20f * sx, 12f * sy)
        cubicTo(20f * sx, 16.5f * sy, 16.5f * sx, 20f * sy, 12f * sx, 21f * sy)
        cubicTo(7.5f * sx, 20f * sy, 4f * sx, 16.5f * sy, 4f * sx, 12f * sy)
        lineTo(4f * sx, 6f * sy)
        close()
    }
    drawPath(p, c, style = s)
    val tick = Path().apply {
        moveTo(8.5f * sx, 12f * sy)
        lineTo(11f * sx, 14.5f * sy)
        lineTo(15.5f * sx, 9.5f * sy)
    }
    drawPath(tick, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(7f * sx, 17f * sy)
        cubicTo(4f * sx, 17f * sy, 2f * sx, 15f * sy, 2f * sx, 12.5f * sy)
        cubicTo(2f * sx, 10f * sy, 4f * sx, 8f * sy, 6.5f * sx, 8f * sy)
        cubicTo(7f * sx, 5.5f * sy, 9.5f * sx, 4f * sy, 12f * sx, 4f * sy)
        cubicTo(15f * sx, 4f * sy, 17.5f * sx, 6f * sy, 18f * sx, 9f * sy)
        cubicTo(20.5f * sx, 9f * sy, 22f * sx, 11f * sy, 22f * sx, 13f * sy)
        cubicTo(22f * sx, 15.2f * sy, 20f * sx, 17f * sy, 17.5f * sx, 17f * sy)
        close()
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNote(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(5f * sx, 4f * sy)
        lineTo(15f * sx, 4f * sy)
        lineTo(19f * sx, 8f * sy)
        lineTo(19f * sx, 20f * sy)
        lineTo(5f * sx, 20f * sy)
        close()
    }
    drawPath(p, c, style = s)
    drawLine(c, Offset(8f * sx, 11f * sy), Offset(16f * sx, 11f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(8f * sx, 14f * sy), Offset(16f * sx, 14f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(8f * sx, 17f * sy), Offset(13f * sx, 17f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}
