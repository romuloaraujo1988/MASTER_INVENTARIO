package com.inventario.mobile.domain.model

/**
 * Formatos de exportação disponíveis
 */
enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    PDF("PDF", "pdf", "application/pdf"),
    EXCEL("Excel", "xls", "application/vnd.ms-excel"), // TSV com extensão .xls abre no Excel
    CSV("CSV", "csv", "text/csv");
    
    companion object {
        fun fromDisplayName(name: String): ExportFormat {
            return values().find { it.displayName == name } ?: PDF
        }
    }
}
