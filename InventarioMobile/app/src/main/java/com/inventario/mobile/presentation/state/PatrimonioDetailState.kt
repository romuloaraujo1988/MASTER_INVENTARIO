package com.inventario.mobile.presentation.state

import com.inventario.mobile.domain.model.PatrimonioDetalhe

/**
 * Estados possíveis da tela de detalhes do patrimônio
 * Regra: Sealed class para estados mutuamente exclusivos
 * 
 * @see Requirements 2.2
 */
sealed class PatrimonioDetailState {
    
    /**
     * Estado de carregamento - buscando detalhes
     */
    object Loading : PatrimonioDetailState()
    
    /**
     * Estado de sucesso - detalhes carregados
     * @param patrimonio Detalhes completos do patrimônio
     */
    data class Success(val patrimonio: PatrimonioDetalhe) : PatrimonioDetailState()
    
    /**
     * Estado de erro - falha ao carregar detalhes
     * @param message Mensagem de erro
     */
    data class Error(val message: String) : PatrimonioDetailState()
}
