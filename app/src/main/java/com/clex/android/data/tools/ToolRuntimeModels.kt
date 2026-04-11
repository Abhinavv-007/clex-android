package com.clex.android.data.tools

import android.net.Uri

enum class WorkspaceToolId(val webId: String) {
    IMAGE_COMPRESS("image-compress"),
    IMAGE_CONVERT("image-convert"),
    PDF_MERGE("pdf-merge"),
    PDF_SPLIT("pdf-split"),
    PDF_TO_IMAGE("pdf-to-image"),
    WORD_TO_PDF("word-to-pdf"),
    ZIP("zip"),
    SMART_CHAIN("smart-chain"),
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
