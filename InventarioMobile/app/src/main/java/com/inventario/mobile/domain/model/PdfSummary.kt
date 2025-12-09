package com.inventario.mobile.domain.model

/**
 * Resumo/Somatório do relatório PDF
 */
data class PdfSummary(
    val totalItems: Int,
    val coletados: Int,
    val naoColetados: Int,
    val percentualColeta: Double
) {
    companion object {
        /**
         * Calcula o resumo a partir de uma lista de patrimônios
         */
        fun fromPatrimonios(patrimonios: List<Patrimonio>): PdfSummary {
            val total = patrimonios.size
            val coletados = patrimonios.count { it.coletado }
            val naoColetados = total - coletados
            val percentual = if (total > 0) (coletados.toDouble() / total) * 100 else 0.0
            
            return PdfSummary(
                totalItems = total,
                coletados = coletados,
                naoColetados = naoColetados,
                percentualColeta = percentual
            )
        }
    }
    
    /**
     * Valida a consistência do resumo
     */
    fun isConsistent(): Boolean {
        return totalItems == coletados + naoColetados
    }
}
