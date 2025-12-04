package com.inventario.mobile.domain.model

/**
 * Enum para filtros de busca de patrimônios
 * 
 * @see Requirements 3.1, 3.2, 3.3, 3.4
 */
enum class SearchFilter {
    /** Todos os patrimônios */
    ALL,
    
    /** Apenas patrimônios já coletados */
    COLETADOS,
    
    /** Apenas patrimônios pendentes (não coletados) */
    PENDENTES,
    
    /** Apenas patrimônios com divergência de localização */
    DIVERGENCIAS
}
