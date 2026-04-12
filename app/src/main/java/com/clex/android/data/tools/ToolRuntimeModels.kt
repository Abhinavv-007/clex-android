package com.clex.android.data.tools

import android.net.Uri

enum class WorkspaceToolId(
    val webId: String,
    val displayTitle: String,
) {
    IMAGE_COMPRESS("image-compress", "IMAGE COMPRESS"),
    IMAGE_CONVERT("image-convert", "FORMAT CONVERT"),
    IMAGE_TO_WEBP("image-to-webp", "IMAGE → WEBP"),
    IMAGE_TO_JPEG("image-to-jpeg", "IMAGE → JPEG"),
    IMAGE_TO_PNG("image-to-png", "IMAGE → PNG"),
    PDF_MERGE("pdf-merge", "PDF MERGE"),
    PDF_SPLIT("pdf-split", "PDF SPLIT"),
    PDF_TO_IMAGE("pdf-to-image", "PDF → IMAGE"),
    WORD_TO_PDF("word-to-pdf", "DOCX → PDF"),
    ZIP("zip", "ZIP BUNDLE"),
    SMART_CHAIN("smart-chain", "SMART CHAIN"),
}

data class WorkspaceToolFile(
    val id: String,
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String,
)

data class WorkspaceToolSuggestion(
    val toolId: String,
    val label: String,
    val description: String,
)

data class WorkspaceToolResult(
    val toolId: WorkspaceToolId,
    val inputFileIds: List<String>,
    val outputBytes: ByteArray? = null,
    val outputName: String? = null,
    val outputType: String? = null,
    val completedAt: Long = System.currentTimeMillis(),
    val suggestions: List<WorkspaceToolSuggestion> = emptyList(),
    val note: String? = null,
)

data class WorkspaceToolsUiState(
    val activeTool: WorkspaceToolId? = null,
    val isProcessing: Boolean = false,
    val progress: Int = 0,
    val result: WorkspaceToolResult? = null,
    val error: String? = null,
)
