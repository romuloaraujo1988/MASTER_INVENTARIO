package com.inventario.mobile.presentation.historico

import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.repository.EstatisticasHistorico

/**
 * Estados da tela de histórico de scans
 * 
 * @since v2.11.0
 */
sealed class HistoricoScansState {
    
    /** Estado inicial/ocioso */
    object Idle : HistoricoScansState()
    
    /** Carregando dados */
    object Loading : HistoricoScansState()
    
    /** Dados carregados com sucesso */
    data class Success(
        val historico: List<HistoricoScan>,
        val estatisticas: EstatisticasHistorico? = null
    ) : HistoricoScansState()
    
    /** Erro ao carregar */
    data class Error(val message: String) : HistoricoScansState()
    
    /** Lista vazia */
    object Empty : HistoricoScansState()
}

/**
 * Filtros disponíveis para o histórico
 */
enum class FiltroHistorico {
    TODOS,      // Todos os registros
    COLETADOS,  // Apenas os que resultaram em coleta
    CONSULTAS   // Apenas consultas (sem coleta)
}
