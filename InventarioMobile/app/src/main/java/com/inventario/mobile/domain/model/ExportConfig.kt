package com.inventario.mobile.domain.model

/**
 * Configuração para exportação de relatório PDF
 */
data class ExportConfig(
    val sala: Sala,
    val filter: ExportFilter,
    val inventarioId: Int? = null,
    val isOffline: Boolean = false
)
