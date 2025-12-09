package com.inventario.mobile.domain.model

/**
 * Filtros disponíveis para exportação de patrimônios
 */
enum class ExportFilter(val displayName: String) {
    TODOS("Todos"),
    COLETADOS("Coletados"),
    NAO_COLETADOS("Não Coletados");
    
    companion object {
        fun fromDisplayName(name: String): ExportFilter {
            return values().find { it.displayName == name } ?: TODOS
        }
    }
}
