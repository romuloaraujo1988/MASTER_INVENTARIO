package com.inventario.mobile.presentation.statistics

import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Sala

/**
 * Estados da tela de exportação
 */
sealed class ExportState {
    
    /**
     * Estado inicial - aguardando ação do usuário
     */
    object Idle : ExportState()
    
    /**
     * Carregando lista de salas
     */
    object LoadingSalas : ExportState()
    
    /**
     * Salas carregadas com sucesso
     */
    data class SalasLoaded(val salas: List<Sala>) : ExportState()
    
    /**
     * Gerando relatório
     */
    data class Generating(val message: String, val progress: Int = 0) : ExportState()
    
    /**
     * Relatório gerado com sucesso
     */
    data class Success(val result: ExportResult) : ExportState()
    
    /**
     * Erro durante operação
     */
    data class Error(val message: String) : ExportState()
    
    /**
     * Nenhum dado encontrado para exportar
     */
    data class NoData(val message: String) : ExportState()
}
