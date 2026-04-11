package com.clex.android.data.tools

object ToolChainSuggestions {
    fun getSuggestions(outputMime: String, outputCount: Int): List<WorkspaceToolSuggestion> {
        val suggestions = mutableListOf<WorkspaceToolSuggestion>()

        when {
            outputMime == "application/pdf" -> {
                if (outputCount == 1) {
                    suggestions += WorkspaceToolSuggestion("pdf-split", "Split PDF", "Separate into individual pages")
                    suggestions += WorkspaceToolSuggestion("pdf-to-image", "Export as images", "Convert pages to JPG or PNG")
                }
                if (outputCount > 1) {
                    suggestions += WorkspaceToolSuggestion("pdf-merge", "Merge PDFs", "Combine into one document")
                }
                suggestions += WorkspaceToolSuggestion("zip", "Package as ZIP", "Bundle for easy sharing")
                suggestions += WorkspaceToolSuggestion("share", "Share now", "Send directly or upload to Drive")
            }

            outputMime.startsWith("image/") -> {
                suggestions += WorkspaceToolSuggestion("image-compress", "Compress image", "Reduce file size")
                suggestions += WorkspaceToolSuggestion("image-convert", "Convert format", "Change to PNG or WebP")
                if (outputCount > 1) {
                    suggestions += WorkspaceToolSuggestion("zip", "Zip all images", "Bundle into a single archive")
                }
                suggestions += WorkspaceToolSuggestion("share", "Share now", "Send directly or upload to Drive")
            }

            outputMime == "application/zip" || outputMime == "application/x-zip-compressed" -> {
                suggestions += WorkspaceToolSuggestion("share", "Share now", "Send directly or upload to Drive")
            }

            outputMime == "application/msword" ||
            outputMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                suggestions += WorkspaceToolSuggestion("word-to-pdf", "Convert to PDF", "Universal format for sharing")
                suggestions += WorkspaceToolSuggestion("share", "Share now", "Send directly or upload to Drive")
            }

            else -> {
                suggestions += WorkspaceToolSuggestion("zip", "Package as ZIP", "Bundle for easy sharing")
                suggestions += WorkspaceToolSuggestion("share", "Share now", "Send directly or upload to Drive")
            }
        }

        return suggestions
    }
}
