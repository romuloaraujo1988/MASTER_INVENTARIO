package com.inventario.mobile.domain.model

/**
 * Resultado da exportação de relatório PDF
 */
data class ExportResult(
    val filePath: String,
    val fileName: String,
    val totalItems: Int,
    val coletados: Int,
    val naoColetados: Int,
    val percentualColeta: Double
) {
    /**
     * Verifica se há itens no relatório
     */
    fun hasItems(): Boolean = totalItems > 0
    
    /**
     * Retorna o percentual formatado
     */
    fun percentualFormatado(): String = String.format("%.1f%%", percentualColeta)
}
