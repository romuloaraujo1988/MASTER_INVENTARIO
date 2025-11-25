package com.inventario.mobile.domain.model

/**
 * Enum para filtro de status de coletas
 * Usado para filtrar patrimônios por status de coleta
 * 
 * @see Requirements 4.1, 4.2, 4.3
 */
enum class StatusFiltro {
    /**
     * Exibe todos os patrimônios (coletados e pendentes)
     */
    TODOS,
    
    /**
     * Exibe apenas patrimônios que já foram coletados
     */
    COLETADOS,
    
    /**
     * Exibe apenas patrimônios pendentes de coleta
     */
    PENDENTES,
    
    /**
     * Exibe apenas coletas sem etiqueta (por descrição)
     * Coletas onde numeroPatrimonio está vazio e descricaoPatrimonio está preenchido
     */
    SEM_ETIQUETA
}
