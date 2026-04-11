package com.clex.android.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clex.android.ui.theme.CxBorders
import com.clex.android.ui.theme.CxColors
import com.clex.android.ui.theme.CxSpacing
import com.clex.android.ui.theme.CxTheme
import com.clex.android.ui.theme.CxTypography
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

// ═══════════════════════════════════════════════════
//  QRCodeImage
//  Generates a QR code bitmap via ZXing and displays
//  it as a Compose Image. Themed to match Clex design.
// ═══════════════════════════════════════════════════

@Composable
fun QRCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    foreground: Color = CxTheme.colors.textPrimary,
    background: Color = CxTheme.colors.bgCard
) {
    val bitmap = remember(content, foreground, background) {
        generateQrBitmap(content, foreground, background, pixels = 512)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR code",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback: show placeholder if ZXing fails
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .border(CxBorders.thin, foreground.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                MonoText(
                    text = "QR",
                    fontSize = CxTypography.textSm,
                    color = foreground.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// ── Framed QR (with accent border + label) ────────

@Composable
fun FramedQRCode(
    content: String,
    label: String = "SCAN TO RECEIVE",
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val colors = CxTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CxSpacing.sm)
    ) {
        MonoText(
            text = label,
            fontSize = CxTypography.textXs,
            color = colors.textTertiary,
            letterSpacing = CxTypography.textXs * 0.15
        )

        Box(
            modifier = Modifier
                .border(CxBorders.medium, colors.accent)
                .background(CxColors.white)  // QR always needs white bg for scanner compat
                .padding(CxSpacing.sm)
        ) {
            QRCodeImage(
                content = content,
                size = size,
                foreground = CxColors.pureBlack,
                background = CxColors.white
            )
        }
    }
}

// ── Bitmap Generator ──────────────────────────────

private fun generateQrBitmap(
    content: String,
    foreground: Color,
    background: Color,
    pixels: Int = 512
): Bitmap? {
    if (content.isBlank()) return null
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val writer = QRCodeWriter()
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, pixels, pixels, hints)
        val fgArgb = foreground.toAndroidArgb()
        val bgArgb = background.toAndroidArgb()

        val bmp = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888)
        for (x in 0 until pixels) {
            for (y in 0 until pixels) {
                bmp.setPixel(x, y, if (matrix[x, y]) fgArgb else bgArgb)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}

private fun Color.toAndroidArgb(): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val r = (red   * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue  * 255).toInt().coerceIn(0, 255)
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}
