package com.clex.android.data.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ImageCompressTool {
    data class CompressResult(val bytes: ByteArray, val mimeType: String)

    suspend fun compress(
        inputBytes: ByteArray,
        maxSizeMB: Double = 1.0,
        quality: Double = 0.8,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<CompressResult> = withContext(Dispatchers.Default) {
        runCatching {
            onProgress?.invoke(10)
            val bitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)
                ?: error("Could not decode image")

            onProgress?.invoke(40)
            val maxBytes = (maxSizeMB * 1024 * 1024).toLong()
            val androidQuality = (quality * 100).toInt().coerceIn(1, 100)

            var currentQuality = androidQuality
            var outputBytes: ByteArray
            do {
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, out)
                outputBytes = out.toByteArray()
                currentQuality = (currentQuality * 0.85).toInt().coerceAtLeast(10)
            } while (outputBytes.size > maxBytes && currentQuality > 10)

            bitmap.recycle()
            onProgress?.invoke(100)
            CompressResult(outputBytes, "image/jpeg")
        }
    }
}

object ImageConvertTool {
    enum class TargetFormat(val mimeType: String, val extension: String) {
        JPEG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        WEBP("image/webp", "webp"),
    }

    data class ConvertResult(val bytes: ByteArray, val mimeType: String, val extension: String)

    suspend fun convert(
        inputBytes: ByteArray,
        target: TargetFormat = TargetFormat.WEBP,
        quality: Int = 85,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<ConvertResult> = withContext(Dispatchers.Default) {
        runCatching {
            onProgress?.invoke(10)
            val bitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)
                ?: error("Could not decode image")

            onProgress?.invoke(50)
            val compressFormat = when (target) {
                TargetFormat.JPEG -> Bitmap.CompressFormat.JPEG
                TargetFormat.PNG -> Bitmap.CompressFormat.PNG
                TargetFormat.WEBP -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }

            val out = ByteArrayOutputStream()
            bitmap.compress(compressFormat, quality, out)
            bitmap.recycle()
            onProgress?.invoke(100)

            ConvertResult(out.toByteArray(), target.mimeType, target.extension)
        }
    }
}

object ZipTool {
    data class ZipInput(val name: String, val bytes: ByteArray)

    suspend fun zip(
        files: List<ZipInput>,
        onProgress: ((Int) -> Unit)? = null,
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            require(files.isNotEmpty()) { "Select at least one file to bundle." }
            val out = ByteArrayOutputStream()
            val zip = ZipOutputStream(out)

            files.forEachIndexed { index, file ->
                val entry = ZipEntry(file.name)
                zip.putNextEntry(entry)
                zip.write(file.bytes)
                zip.closeEntry()
                onProgress?.invoke(((index + 1).toDouble() / files.size * 100).toInt())
            }

            zip.close()
            out.toByteArray()
        }
    }
}
