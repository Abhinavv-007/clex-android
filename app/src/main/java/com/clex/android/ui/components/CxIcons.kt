package com.clex.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════
//  CxIcons — v1.12 Ledger icon set.
//  24dp grid, 1.5dp stroke, round caps for legibility,
//  square geometric primitives (no decorative flourish).
// ═══════════════════════════════════════════════════

@Composable
fun CxIcon(
    icon: CxIconType,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color,
    strokeWidth: Dp = 1.5.dp,
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
            CxIconType.CHEVRON_LEFT -> drawChevronLeft(w, h, color, stroke)
            CxIconType.ARROW_RIGHT -> drawArrowRight(w, h, color, stroke)
            CxIconType.ARROW_UP_RIGHT -> drawArrowUpRight(w, h, color, stroke)
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
            CxIconType.BOLT -> drawBolt(w, h, color, stroke)
            CxIconType.FILE -> drawFile(w, h, color, stroke)
            CxIconType.FOLDER -> drawFolder(w, h, color, stroke)
            CxIconType.KEY -> drawKey(w, h, color, stroke)
            CxIconType.EYE -> drawEye(w, h, color, stroke)
            CxIconType.EYE_OFF -> drawEyeOff(w, h, color, stroke)
            CxIconType.COPY -> drawCopy(w, h, color, stroke)
            CxIconType.TRASH -> drawTrash(w, h, color, stroke)
            CxIconType.CHECK -> drawCheck(w, h, color, stroke)
            CxIconType.DOT -> drawDot(w, h, color, stroke)
            CxIconType.GRID -> drawGrid(w, h, color, stroke)
            CxIconType.INBOX -> drawInbox(w, h, color, stroke)
            CxIconType.SEND -> drawSend(w, h, color, stroke)
            CxIconType.RADIO -> drawRadio(w, h, color, stroke)
        }
    }
}

enum class CxIconType {
    HOME, VAULT, CHAIN, SETTINGS,
    MENU, CLOSE, CHEVRON_RIGHT, CHEVRON_LEFT,
    ARROW_RIGHT, ARROW_UP_RIGHT, PLUS,
    SHARE, MOON, SUN, QUESTION,
    LOCK, LINK, UPLOAD, DOWNLOAD,
    SPARKLE, SHIELD, CLOUD, NOTE,
    BOLT, FILE, FOLDER, KEY,
    EYE, EYE_OFF, COPY, TRASH,
    CHECK, DOT, GRID, INBOX,
    SEND, RADIO,
}

// All paths normalized to a 24x24 viewBox, then scaled to (w, h).

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHome(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(4f * sx, 11f * sy)
        lineTo(12f * sx, 4f * sy)
        lineTo(20f * sx, 11f * sy)
        lineTo(20f * sx, 20f * sy)
        lineTo(4f * sx, 20f * sy)
        close()
    }
    drawPath(p, c, style = s)
    drawLine(c, Offset(10f * sx, 20f * sy), Offset(10f * sx, 14f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(14f * sx, 20f * sy), Offset(14f * sx, 14f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(10f * sx, 14f * sy), Offset(14f * sx, 14f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVault(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val body = Path().apply {
        moveTo(4f * sx, 10f * sy)
        lineTo(20f * sx, 10f * sy)
        lineTo(20f * sx, 20f * sy)
        lineTo(4f * sx, 20f * sy)
        close()
    }
    drawPath(body, c, style = s)
    val shackle = Path().apply {
        moveTo(8f * sx, 10f * sy)
        lineTo(8f * sx, 7f * sy)
        cubicTo(8f * sx, 4.8f * sy, 9.8f * sx, 3f * sy, 12f * sx, 3f * sy)
        cubicTo(14.2f * sx, 3f * sy, 16f * sx, 4.8f * sy, 16f * sx, 7f * sy)
        lineTo(16f * sx, 10f * sy)
    }
    drawPath(shackle, c, style = s)
    drawCircle(c, radius = 1.2f * sx, center = Offset(12f * sx, 14f * sy))
    drawLine(c, Offset(12f * sx, 15f * sy), Offset(12f * sx, 17.5f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChain(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val left = Path().apply {
        moveTo(10f * sx, 14f * sy)
        lineTo(7f * sx, 17f * sy)
        cubicTo(5.5f * sx, 18.5f * sy, 3.5f * sx, 18.5f * sy, 2f * sx, 17f * sy)
        cubicTo(0.5f * sx, 15.5f * sy, 0.5f * sx, 13.5f * sy, 2f * sx, 12f * sy)
        lineTo(5f * sx, 9f * sy)
    }
    drawPath(left, c, style = s)
    val right = Path().apply {
        moveTo(14f * sx, 10f * sy)
        lineTo(17f * sx, 7f * sy)
        cubicTo(18.5f * sx, 5.5f * sy, 20.5f * sx, 5.5f * sy, 22f * sx, 7f * sy)
        cubicTo(23.5f * sx, 8.5f * sy, 23.5f * sx, 10.5f * sy, 22f * sx, 12f * sy)
        lineTo(19f * sx, 15f * sy)
    }
    drawPath(right, c, style = s)
    drawLine(c, Offset(9f * sx, 15f * sy), Offset(15f * sx, 9f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSettings(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(4f * sx, 7f * sy), Offset(20f * sx, 7f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(4f * sx, 12f * sy), Offset(20f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(4f * sx, 17f * sy), Offset(20f * sx, 17f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawCircle(c, radius = 1.8f * sx, center = Offset(9f * sx, 7f * sy))
    drawCircle(c, radius = 1.8f * sx, center = Offset(15f * sx, 12f * sy))
    drawCircle(c, radius = 1.8f * sx, center = Offset(8f * sx, 17f * sy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMenu(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(5f * sx, 7f * sy), Offset(19f * sx, 7f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(5f * sx, 12f * sy), Offset(19f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(5f * sx, 17f * sy), Offset(13f * sx, 17f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
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
        moveTo(10f * sx, 6f * sy)
        lineTo(16f * sx, 12f * sy)
        lineTo(10f * sx, 18f * sy)
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChevronLeft(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(14f * sx, 6f * sy)
        lineTo(8f * sx, 12f * sy)
        lineTo(14f * sx, 18f * sy)
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowRight(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(4f * sx, 12f * sy), Offset(20f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val arrow = Path().apply {
        moveTo(14f * sx, 6f * sy)
        lineTo(20f * sx, 12f * sy)
        lineTo(14f * sx, 18f * sy)
    }
    drawPath(arrow, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowUpRight(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(6f * sx, 18f * sy), Offset(18f * sx, 6f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val arrow = Path().apply {
        moveTo(8f * sx, 6f * sy)
        lineTo(18f * sx, 6f * sy)
        lineTo(18f * sx, 16f * sy)
    }
    drawPath(arrow, c, style = s)
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
    drawLine(c, Offset(12f * sx, 4f * sy), Offset(12f * sx, 15f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val arrow = Path().apply {
        moveTo(8f * sx, 8f * sy)
        lineTo(12f * sx, 4f * sy)
        lineTo(16f * sx, 8f * sy)
    }
    drawPath(arrow, c, style = s)
    val tray = Path().apply {
        moveTo(5f * sx, 14f * sy)
        lineTo(5f * sx, 20f * sy)
        lineTo(19f * sx, 20f * sy)
        lineTo(19f * sx, 14f * sy)
    }
    drawPath(tray, c, style = s)
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBolt(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(13f * sx, 3f * sy)
        lineTo(5f * sx, 13f * sy)
        lineTo(11f * sx, 13f * sy)
        lineTo(11f * sx, 21f * sy)
        lineTo(19f * sx, 11f * sy)
        lineTo(13f * sx, 11f * sy)
        close()
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFile(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(6f * sx, 3f * sy)
        lineTo(14f * sx, 3f * sy)
        lineTo(19f * sx, 8f * sy)
        lineTo(19f * sx, 21f * sy)
        lineTo(6f * sx, 21f * sy)
        close()
    }
    drawPath(p, c, style = s)
    val fold = Path().apply {
        moveTo(14f * sx, 3f * sy)
        lineTo(14f * sx, 8f * sy)
        lineTo(19f * sx, 8f * sy)
    }
    drawPath(fold, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFolder(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(3f * sx, 7f * sy)
        lineTo(10f * sx, 7f * sy)
        lineTo(12f * sx, 9f * sy)
        lineTo(21f * sx, 9f * sy)
        lineTo(21f * sx, 19f * sy)
        lineTo(3f * sx, 19f * sy)
        close()
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKey(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawCircle(c, radius = 4f * sx, center = Offset(8f * sx, 12f * sy), style = s)
    drawLine(c, Offset(11.5f * sx, 12f * sy), Offset(21f * sx, 12f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(17f * sx, 12f * sy), Offset(17f * sx, 16f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(20f * sx, 12f * sy), Offset(20f * sx, 15f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEye(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(2f * sx, 12f * sy)
        cubicTo(4f * sx, 7f * sy, 8f * sx, 5f * sy, 12f * sx, 5f * sy)
        cubicTo(16f * sx, 5f * sy, 20f * sx, 7f * sy, 22f * sx, 12f * sy)
        cubicTo(20f * sx, 17f * sy, 16f * sx, 19f * sy, 12f * sx, 19f * sy)
        cubicTo(8f * sx, 19f * sy, 4f * sx, 17f * sy, 2f * sx, 12f * sy)
        close()
    }
    drawPath(p, c, style = s)
    drawCircle(c, radius = 3f * sx, center = Offset(12f * sx, 12f * sy), style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEyeOff(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(2f * sx, 12f * sy)
        cubicTo(4f * sx, 7f * sy, 8f * sx, 5f * sy, 12f * sx, 5f * sy)
        cubicTo(16f * sx, 5f * sy, 20f * sx, 7f * sy, 22f * sx, 12f * sy)
        cubicTo(20f * sx, 17f * sy, 16f * sx, 19f * sy, 12f * sx, 19f * sy)
        cubicTo(8f * sx, 19f * sy, 4f * sx, 17f * sy, 2f * sx, 12f * sy)
        close()
    }
    drawPath(p, c, style = s)
    drawLine(c, Offset(4f * sx, 4f * sy), Offset(20f * sx, 20f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCopy(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val back = Path().apply {
        moveTo(8f * sx, 3f * sy)
        lineTo(20f * sx, 3f * sy)
        lineTo(20f * sx, 16f * sy)
        lineTo(8f * sx, 16f * sy)
        close()
    }
    drawPath(back, c, style = s)
    val front = Path().apply {
        moveTo(4f * sx, 8f * sy)
        lineTo(16f * sx, 8f * sy)
        lineTo(16f * sx, 21f * sy)
        lineTo(4f * sx, 21f * sy)
        close()
    }
    drawPath(front, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrash(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawLine(c, Offset(3f * sx, 6f * sy), Offset(21f * sx, 6f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val lid = Path().apply {
        moveTo(9f * sx, 6f * sy)
        lineTo(9f * sx, 4f * sy)
        lineTo(15f * sx, 4f * sy)
        lineTo(15f * sx, 6f * sy)
    }
    drawPath(lid, c, style = s)
    val body = Path().apply {
        moveTo(5f * sx, 7f * sy)
        lineTo(7f * sx, 21f * sy)
        lineTo(17f * sx, 21f * sy)
        lineTo(19f * sx, 7f * sy)
    }
    drawPath(body, c, style = s)
    drawLine(c, Offset(10f * sx, 11f * sy), Offset(10f * sx, 17f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(14f * sx, 11f * sy), Offset(14f * sx, 17f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCheck(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(5f * sx, 12f * sy)
        lineTo(10f * sx, 17f * sy)
        lineTo(20f * sx, 7f * sy)
    }
    drawPath(p, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDot(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawCircle(c, radius = 3.5f * sx, center = Offset(12f * sx, 12f * sy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val q1 = Path().apply { moveTo(4f * sx, 4f * sy); lineTo(10f * sx, 4f * sy); lineTo(10f * sx, 10f * sy); lineTo(4f * sx, 10f * sy); close() }
    val q2 = Path().apply { moveTo(14f * sx, 4f * sy); lineTo(20f * sx, 4f * sy); lineTo(20f * sx, 10f * sy); lineTo(14f * sx, 10f * sy); close() }
    val q3 = Path().apply { moveTo(4f * sx, 14f * sy); lineTo(10f * sx, 14f * sy); lineTo(10f * sx, 20f * sy); lineTo(4f * sx, 20f * sy); close() }
    val q4 = Path().apply { moveTo(14f * sx, 14f * sy); lineTo(20f * sx, 14f * sy); lineTo(20f * sx, 20f * sy); lineTo(14f * sx, 20f * sy); close() }
    drawPath(q1, c, style = s); drawPath(q2, c, style = s); drawPath(q3, c, style = s); drawPath(q4, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInbox(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(3f * sx, 13f * sy)
        lineTo(6f * sx, 5f * sy)
        lineTo(18f * sx, 5f * sy)
        lineTo(21f * sx, 13f * sy)
        lineTo(21f * sx, 19f * sy)
        lineTo(3f * sx, 19f * sy)
        close()
    }
    drawPath(p, c, style = s)
    drawLine(c, Offset(3f * sx, 13f * sy), Offset(8f * sx, 13f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    drawLine(c, Offset(16f * sx, 13f * sy), Offset(21f * sx, 13f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
    val v = Path().apply {
        moveTo(8f * sx, 13f * sy)
        lineTo(10f * sx, 15f * sy)
        lineTo(14f * sx, 15f * sy)
        lineTo(16f * sx, 13f * sy)
    }
    drawPath(v, c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSend(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    val p = Path().apply {
        moveTo(3f * sx, 12f * sy)
        lineTo(21f * sx, 4f * sy)
        lineTo(13f * sx, 21f * sy)
        lineTo(11f * sx, 13f * sy)
        close()
    }
    drawPath(p, c, style = s)
    drawLine(c, Offset(11f * sx, 13f * sy), Offset(21f * sx, 4f * sy), strokeWidth = s.width, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRadio(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val sx = w / 24f; val sy = h / 24f
    drawCircle(c, radius = 1.6f * sx, center = Offset(12f * sx, 12f * sy))
    drawCircle(c, radius = 4f * sx, center = Offset(12f * sx, 12f * sy), style = s)
    drawCircle(c, radius = 8f * sx, center = Offset(12f * sx, 12f * sy), style = s)
}
