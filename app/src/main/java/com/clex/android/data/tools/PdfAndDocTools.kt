package com.clex.android.data.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

object PdfMergeTool {
    suspend fun merge(
        context: Context,
        pdfs: List<ByteArray>,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            PDFBoxResourceLoader.init(context.applicationContext)
            require(pdfs.isNotEmpty()) { "At least one PDF required." }

            val destination = PDDocument()
            try {
                pdfs.forEachIndexed { index, pdfBytes ->
                    val src = PDDocument.load(pdfBytes)
                    try {
                        src.pages.forEach { page -> destination.importPage(page) }
                    } finally {
                        src.close()
                    }
                    onProgress?.invoke(((index + 1).toDouble() / pdfs.size * 90).toInt())
                }

                val out = ByteArrayOutputStream()
                destination.save(out)
                onProgress?.invoke(100)
                out.toByteArray()
            } finally {
                destination.close()
            }
        }
    }
}

object PdfSplitTool {
    data class SplitResult(
        val bytes: ByteArray,
        val name: String,
    )

    suspend fun splitByPage(
        context: Context,
        pdfBytes: ByteArray,
        baseName: String,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<List<SplitResult>> = withContext(Dispatchers.IO) {
        runCatching {
            PDFBoxResourceLoader.init(context.applicationContext)
            val src = PDDocument.load(pdfBytes)
            val total = src.numberOfPages
            val results = mutableListOf<SplitResult>()

            try {
                for (i in 0 until total) {
                    val doc = PDDocument()
                    try {
                        doc.importPage(src.pages[i])
                        val out = ByteArrayOutputStream()
                        doc.save(out)
                        results += SplitResult(
                            bytes = out.toByteArray(),
                            name = "${baseName}_page${i + 1}.pdf"
                        )
                    } finally {
                        doc.close()
                    }
                    onProgress?.invoke(((i + 1).toDouble() / total * 100).toInt())
                }
            } finally {
                src.close()
            }

            results
        }
    }
}

object PdfToImageTool {
    data class PageImage(
        val bytes: ByteArray,
        val name: String,
    )

    suspend fun renderToImages(
        pdfBytes: ByteArray,
        baseName: String,
        dpi: Int = 150,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<List<PageImage>> = withContext(Dispatchers.IO) {
        runCatching {
            val tempFile = File.createTempFile("clex_pdf_", ".pdf")
            try {
                tempFile.writeBytes(pdfBytes)
                val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                val total = renderer.pageCount
                val results = mutableListOf<PageImage>()

                try {
                    for (i in 0 until total) {
                        val page = renderer.openPage(i)
                        val scaleFactor = dpi / 72f
                        val width = (page.width * scaleFactor).toInt()
                        val height = (page.height * scaleFactor).toInt()

                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        val out = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        bitmap.recycle()

                        results += PageImage(
                            bytes = out.toByteArray(),
                            name = "${baseName}_p${i + 1}.jpg"
                        )
                        onProgress?.invoke(((i + 1).toDouble() / total * 100).toInt())
                    }
                } finally {
                    renderer.close()
                    fd.close()
                }

                results
            } finally {
                tempFile.delete()
            }
        }
    }
}

object WordToPdfTool {
    suspend fun convert(
        context: Context,
        docxBytes: ByteArray,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            PDFBoxResourceLoader.init(context.applicationContext)
            onProgress?.invoke(10)

            val document = XWPFDocument(ByteArrayInputStream(docxBytes))
            onProgress?.invoke(40)
            val paragraphs = document.paragraphs.map { it.text }
            document.close()
            onProgress?.invoke(60)

            val pdf = PDDocument()
            try {
                val pageSize = PDRectangle.A4
                val margin = 50f
                val lineHeight = 14f
                val font = PDType1Font.HELVETICA
                val fontSize = 11f
                val usableWidth = pageSize.width - 2 * margin

                var currentPage: PDPage? = null
                var stream: PDPageContentStream? = null
                var yPosition = pageSize.height - margin

                fun newPage() {
                    stream?.endText()
                    stream?.close()
                    currentPage = PDPage(pageSize)
                    pdf.addPage(currentPage)
                    stream = PDPageContentStream(pdf, currentPage!!)
                    stream?.beginText()
                    stream?.setFont(font, fontSize)
                    stream?.newLineAtOffset(margin, pageSize.height - margin - lineHeight)
                    yPosition = pageSize.height - margin - lineHeight
                }

                newPage()

                for (paragraph in paragraphs) {
                    val lines = wrapText(paragraph.ifBlank { " " }, font, fontSize, usableWidth)
                    for (line in lines) {
                        if (yPosition < margin + lineHeight) {
                            newPage()
                        }
                        stream?.showText(line.take(200))
                        stream?.newLineAtOffset(0f, -lineHeight)
                        yPosition -= lineHeight
                    }
                }

                stream?.endText()
                stream?.close()
                onProgress?.invoke(90)

                val out = ByteArrayOutputStream()
                pdf.save(out)
                onProgress?.invoke(100)
                out.toByteArray()
            } finally {
                pdf.close()
            }
        }
    }

    private fun wrapText(text: String, font: PDType1Font, fontSize: Float, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val width = runCatching { font.getStringWidth(testLine) / 1000 * fontSize }
                .getOrElse { testLine.length * fontSize * 0.5f }
            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines += currentLine.toString()
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) lines += currentLine.toString()
        return lines.ifEmpty { listOf("") }
    }
}
