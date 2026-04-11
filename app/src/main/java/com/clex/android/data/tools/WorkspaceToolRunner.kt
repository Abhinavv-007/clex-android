package com.clex.android.data.tools

import android.content.Context
import android.provider.OpenableColumns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WorkspaceToolRunner(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(WorkspaceToolsUiState())
    val state: StateFlow<WorkspaceToolsUiState> = _state.asStateFlow()

    fun runTool(toolId: WorkspaceToolId, uris: List<android.net.Uri>) {
        scope.launch {
            val files = withContext(Dispatchers.IO) { uris.mapNotNull { it.toWorkspaceToolFile(context) } }
            if (files.isEmpty()) {
                _state.value = WorkspaceToolsUiState(error = "No files were selected.")
                return@launch
            }

            _state.value = WorkspaceToolsUiState(activeTool = toolId, isProcessing = true, progress = 0)

            runCatching {
                execute(toolId, files)
            }.onSuccess { result ->
                _state.value = WorkspaceToolsUiState(result = result)
            }.onFailure { error ->
                _state.value = WorkspaceToolsUiState(error = error.message ?: "Tool run failed.")
            }
        }
    }

    fun clearResult() {
        _state.value = WorkspaceToolsUiState()
    }

    private fun setProgress(value: Int) {
        _state.update { it.copy(progress = value.coerceIn(0, 100)) }
    }

    private suspend fun execute(
        toolId: WorkspaceToolId,
        files: List<WorkspaceToolFile>,
    ): WorkspaceToolResult {
        return when (toolId) {
            WorkspaceToolId.IMAGE_COMPRESS -> {
                val file = files.first()
                val inputBytes = readBytes(file)
                val result = ImageCompressTool.compress(inputBytes, onProgress = ::setProgress).getOrThrow()
                val outputName = file.name.replace(Regex("\\.[^.]+$"), "_compressed.jpg")
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = listOf(file.id),
                    outputBytes = result.bytes,
                    outputName = outputName,
                    outputType = result.mimeType,
                    suggestions = ToolChainSuggestions.getSuggestions(result.mimeType, 1)
                )
            }

            WorkspaceToolId.IMAGE_CONVERT -> {
                val file = files.first()
                val inputBytes = readBytes(file)
                val result = ImageConvertTool.convert(
                    inputBytes = inputBytes,
                    target = ImageConvertTool.TargetFormat.WEBP,
                    onProgress = ::setProgress
                ).getOrThrow()
                val outputName = swapExtension(file.name, result.extension)
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = listOf(file.id),
                    outputBytes = result.bytes,
                    outputName = outputName,
                    outputType = result.mimeType,
                    suggestions = ToolChainSuggestions.getSuggestions(result.mimeType, 1)
                )
            }

            WorkspaceToolId.PDF_MERGE -> {
                val pdfFiles = files.filter { it.mimeType == "application/pdf" || it.name.endsWith(".pdf", true) }
                require(pdfFiles.size >= 2) { "Select at least 2 PDF files to merge." }
                val pdfBytes = pdfFiles.map { readBytes(it) }
                val result = PdfMergeTool.merge(context, pdfBytes, onProgress = ::setProgress).getOrThrow()
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = pdfFiles.map { it.id },
                    outputBytes = result,
                    outputName = "merged.pdf",
                    outputType = "application/pdf",
                    suggestions = ToolChainSuggestions.getSuggestions("application/pdf", 1)
                )
            }

            WorkspaceToolId.PDF_SPLIT -> {
                val file = requirePdf(files.first())
                val inputBytes = readBytes(file)
                val baseName = file.name.replace(Regex("\\.pdf$", RegexOption.IGNORE_CASE), "")
                val pages = PdfSplitTool.splitByPage(context, inputBytes, baseName, ::setProgress).getOrThrow()
                val zip = ZipTool.zip(
                    files = pages.map { ZipTool.ZipInput(it.name, it.bytes) },
                    onProgress = ::setProgress
                ).getOrThrow()
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = listOf(file.id),
                    outputBytes = zip,
                    outputName = "${baseName}_pages.zip",
                    outputType = "application/zip",
                    suggestions = ToolChainSuggestions.getSuggestions("application/zip", 1)
                )
            }

            WorkspaceToolId.PDF_TO_IMAGE -> {
                val file = requirePdf(files.first())
                val inputBytes = readBytes(file)
                val baseName = file.name.replace(Regex("\\.pdf$", RegexOption.IGNORE_CASE), "")
                val images = PdfToImageTool.renderToImages(inputBytes, baseName, onProgress = ::setProgress).getOrThrow()
                if (images.size == 1) {
                    WorkspaceToolResult(
                        toolId = toolId,
                        inputFileIds = listOf(file.id),
                        outputBytes = images.first().bytes,
                        outputName = images.first().name,
                        outputType = "image/jpeg",
                        suggestions = ToolChainSuggestions.getSuggestions("image/jpeg", 1)
                    )
                } else {
                    val zip = ZipTool.zip(
                        files = images.map { ZipTool.ZipInput(it.name, it.bytes) },
                        onProgress = ::setProgress
                    ).getOrThrow()
                    WorkspaceToolResult(
                        toolId = toolId,
                        inputFileIds = listOf(file.id),
                        outputBytes = zip,
                        outputName = "${baseName}_images.zip",
                        outputType = "application/zip",
                        suggestions = ToolChainSuggestions.getSuggestions("application/zip", 1)
                    )
                }
            }

            WorkspaceToolId.WORD_TO_PDF -> {
                val file = requireDocx(files.first())
                val inputBytes = readBytes(file)
                val result = WordToPdfTool.convert(context, inputBytes, onProgress = ::setProgress).getOrThrow()
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = listOf(file.id),
                    outputBytes = result,
                    outputName = file.name.replace(Regex("\\.(doc|docx)$", RegexOption.IGNORE_CASE), ".pdf"),
                    outputType = "application/pdf",
                    suggestions = ToolChainSuggestions.getSuggestions("application/pdf", 1)
                )
            }

            WorkspaceToolId.ZIP -> {
                val zip = ZipTool.zip(
                    files = files.map { ZipTool.ZipInput(it.name, readBytes(it)) },
                    onProgress = ::setProgress
                ).getOrThrow()
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = files.map { it.id },
                    outputBytes = zip,
                    outputName = "clex-files.zip",
                    outputType = "application/zip",
                    suggestions = ToolChainSuggestions.getSuggestions("application/zip", 1)
                )
            }

            WorkspaceToolId.SMART_CHAIN -> {
                val primaryMime = files.first().mimeType.ifBlank { "application/octet-stream" }
                WorkspaceToolResult(
                    toolId = toolId,
                    inputFileIds = files.map { it.id },
                    suggestions = ToolChainSuggestions.getSuggestions(primaryMime, files.size),
                    note = "Smart Chain analyzed your selection and suggested the next best actions."
                )
            }
        }
    }

    private suspend fun readBytes(file: WorkspaceToolFile): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(file.uri)?.use { it.readBytes() }
            ?: error("Could not read ${file.name}.")
    }

    private fun requirePdf(file: WorkspaceToolFile): WorkspaceToolFile {
        require(file.mimeType == "application/pdf" || file.name.endsWith(".pdf", true)) {
            "Select a PDF file."
        }
        return file
    }

    private fun requireDocx(file: WorkspaceToolFile): WorkspaceToolFile {
        require(
            file.mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                file.name.endsWith(".docx", true)
        ) { "Select a DOCX file." }
        return file
    }

    private fun swapExtension(fileName: String, newExt: String): String {
        val base = fileName.replace(Regex("\\.[^.]+$"), "")
        return "$base.$newExt"
    }
}

private fun android.net.Uri.toWorkspaceToolFile(context: Context): WorkspaceToolFile? {
    val mimeType = context.contentResolver.getType(this) ?: "application/octet-stream"
    val name = displayName(context) ?: lastPathSegment ?: "file"
    val size = context.contentResolver.openFileDescriptor(this, "r")?.use { it.statSize } ?: 0L
    return WorkspaceToolFile(
        id = UUID.randomUUID().toString(),
        uri = this,
        name = name,
        size = size,
        mimeType = mimeType,
    )
}

private fun android.net.Uri.displayName(context: Context): String? {
    if (scheme != "content") return path?.substringAfterLast('/')
    val cursor = context.contentResolver.query(this, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return it.getString(index)
        }
    }
    return path?.substringAfterLast('/')
}
