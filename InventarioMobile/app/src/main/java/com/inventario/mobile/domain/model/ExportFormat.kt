package com.inventario.mobile.domain.model

/**
 * Formatos de exportação disponíveis.
 *
 * **v2.20.3 (07/05/2026):** EXCEL migrado para XLSX real (OOXML) em vez de TSV
 * com extensão `.xls`. Antes o Excel mostrava "O formato do arquivo e a extensão
 * não correspondem" e apps Android recusavam abrir. Agora o arquivo é um XLSX
 * válido — abre limpo em Excel, Google Sheets, LibreOffice, WPS Office.
 */
enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    PDF("PDF", "pdf", "application/pdf"),
    EXCEL(
        "Excel",
        "xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ),
    CSV("CSV", "csv", "text/csv");

    companion object {
        fun fromDisplayName(name: String): ExportFormat {
            return values().find { it.displayName == name } ?: PDF
        }
    }
}
