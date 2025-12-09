package com.inventario.mobile.presentation.export

import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Sala as DomainSala

/**
 * Estados da tela de exportação de PDF
 */
sealed class ExportPdfState {
    
    /**
     * Estado inicial - aguardando ação do usuário
     */
    object Idle : ExportPdfState()
    
    /**
     * Carregando lista de salas
     */
    object LoadingSalas : ExportPdfState()
    
    /**
     * Salas carregadas com sucesso
     */
    data class SalasLoaded(val salas: List<DomainSala>) : ExportPdfState()
    
    /**
     * Gerando PDF
     */
    data class Generating(val message: String) : ExportPdfState()
    
    /**
     * PDF gerado com sucesso
     */
    data class Success(val result: ExportResult) : ExportPdfState()
    
    /**
     * Erro durante operação
     */
    data class Error(val message: String) : ExportPdfState()
    
    /**
     * Nenhum dado encontrado para exportar
     */
    data class NoData(val message: String) : ExportPdfState()
}
