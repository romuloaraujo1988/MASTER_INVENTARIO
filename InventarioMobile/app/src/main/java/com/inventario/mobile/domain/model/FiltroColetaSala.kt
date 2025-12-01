package com.inventario.mobile.domain.model

/**
 * Enum para filtros de coleta na aba "Por Sala".
 * 
 * Nota: Usa nome diferente de FiltroColeta (em ColetasViewModel) para evitar conflito.
 * FiltroColeta existente tem valores: TODOS, COM_ETIQUETA, SEM_ETIQUETA
 * Este enum é específico para filtrar por status de coleta na visualização por sala.
 */
enum class FiltroColetaSala {
    /**
     * Exibe todos os patrimônios da sala (coletados e não coletados)
     */
    TODOS,
    
    /**
     * Exibe apenas patrimônios já coletados no inventário atual
     */
    COLETADOS,
    
    /**
     * Exibe apenas patrimônios ainda não coletados (pendentes)
     */
    NAO_COLETADOS;
    
    /**
     * Retorna o valor booleano correspondente para filtro no banco de dados
     * null = todos, true = coletados, false = não coletados
     */
    fun toBoolean(): Boolean? = when (this) {
        TODOS -> null
        COLETADOS -> true
        NAO_COLETADOS -> false
    }
    
    /**
     * Retorna o texto de exibição do filtro
     */
    fun toDisplayText(): String = when (this) {
        TODOS -> "Todos"
        COLETADOS -> "Coletados"
        NAO_COLETADOS -> "Não Coletados"
    }
    
    companion object {
        /**
         * Cria um FiltroColetaSala a partir de um valor booleano
         */
        fun fromBoolean(coletado: Boolean?): FiltroColetaSala = when (coletado) {
            null -> TODOS
            true -> COLETADOS
            false -> NAO_COLETADOS
        }
    }
}
