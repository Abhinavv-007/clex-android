package com.clex.android.data.transfer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ═══════════════════════════════════════════════════
//  CLEX — Android Sharesheet Helper
//
//  Outbound: share files from Clex → Android apps
//    (Quick Share, WhatsApp, Gmail, Drive, etc.)
//  Inbound: receive files/text from external apps → Clex
//
//  This is SEPARATE from Clex Link.
//  Clex does not control native Quick Share device lists.
// ═══════════════════════════════════════════════════

private const val FILE_PROVIDER_AUTHORITY = "com.clex.android.fileprovider"

object SharesheetHelper {

    // ── Outbound: share single file to Android apps ──

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun shareFileUri(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    // ── Outbound: share multiple files to Android apps ──

    fun shareMultipleFiles(context: Context, files: List<File>, mimeType: String = "*/*") {
        if (files.isEmpty()) return
        if (files.size == 1) {
            shareFile(context, files.first(), mimeType)
            return
        }
        val uris = ArrayList<Uri>(files.size)
        for (file in files) {
            uris.add(FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file))
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun shareMultipleUris(context: Context, uris: List<Uri>, mimeType: String = "*/*") {
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            shareFileUri(context, uris.first(), mimeType)
            return
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    // ── Outbound: share text to Android apps ──

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    suspend fun shareWorkspaceFiles(context: Context, files: List<WorkspaceSelectedFile>) {
        if (files.isEmpty()) return

        val stagedFiles = withContext(Dispatchers.IO) {
            val shareDir = File(context.cacheDir, "clex-shares").apply { mkdirs() }
            files.mapIndexedNotNull { index, file ->
                runCatching {
                    val safeName = file.name.ifBlank { "clex-file-$index" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
                    val stagedFile = File(shareDir, "${System.currentTimeMillis()}_${index}_$safeName")
                    context.contentResolver.openInputStream(file.uri)?.use { input ->
                        stagedFile.outputStream().use { output -> input.copyTo(output) }
                    } ?: return@runCatching null
                    stagedFile
                }.getOrNull()
            }
        }

        if (stagedFiles.isEmpty()) return
        val commonMimeType = files.map { it.mimeType }.distinct().singleOrNull() ?: "*/*"
        shareMultipleFiles(context, stagedFiles, commonMimeType)
    }

    // ── Inbound: extract files/text from incoming share intent ──

    data class InboundShare(
        val uris: List<Uri>,
        val text: String?,
    )

    fun extractInboundShare(intent: Intent): InboundShare? {
        val action = intent.action ?: return null

        return when (action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (uri != null || text != null) {
                    InboundShare(
                        uris = listOfNotNull(uri),
                        text = text,
                    )
                } else null
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (!uris.isNullOrEmpty()) {
                    InboundShare(
                        uris = uris,
                        text = null,
                    )
                } else null
            }

            else -> null
        }
    }
}
