package com.inventario.mobile.domain.model

/**
 * Domain Model para patrimônio com informações de coleta
 * Usado na listagem de resultados da busca rápida
 * 
 * @see Requirements 2.1
 */
data class PatrimonioComColeta(
    val id: Long,
    val numero: String,
    val descricao: String,
    val salaNome: String?,
    val responsavelNome: String?,
    val coletado: Boolean,
    val coletadoPor: String?,
    val dataColeta: Long?,
    val temDivergencia: Boolean
) {
    /**
     * Retorna status formatado para exibição
     */
    fun getStatusFormatado(): String {
        return if (coletado) "Coletado" else "Pendente"
    }
    
    /**
     * Retorna ícone de status
     */
    fun getStatusIcone(): String {
        return when {
            temDivergencia -> "⚠️"
            coletado -> "✅"
            else -> "⏳"
        }
    }
    
    /**
     * Verifica se corresponde ao termo de busca
     */
    fun correspondeAoBusca(query: String): Boolean {
        val queryLower = query.lowercase()
        return numero.lowercase().contains(queryLower) ||
               descricao.lowercase().contains(queryLower) ||
               salaNome?.lowercase()?.contains(queryLower) == true
    }
}
