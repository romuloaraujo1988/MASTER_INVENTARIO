package com.inventario.mobile.presentation.consulta

import com.inventario.mobile.domain.model.PatrimonioDetalhe

/**
 * Estados da UI de detalhes de patrimônio
 * 
 * Sealed class para garantir type-safety
 */
sealed class DetalheState {
    /**
     * Estado inicial - aguardando ação do usuário
     */
    object Idle : DetalheState()
    
    /**
     * Estado de carregamento - buscando detalhes
     * 
     * @param patrimonioId ID do patrimônio sendo carregado
     */
    data class Loading(val patrimonioId: Int) : DetalheState()
    
    /**
     * Estado de sucesso - detalhes carregados
     * 
     * @param detalhe Detalhes completos do patrimônio
     */
    data class Success(val detalhe: PatrimonioDetalhe) : DetalheState() {
        val temDivergencias: Boolean get() = detalhe.temDivergencias()
        val foiColetado: Boolean get() = detalhe.foiColetado()
        val temFoto: Boolean get() = detalhe.temFoto()
    }
    
    /**
     * Estado de erro - falha ao carregar detalhes
     * 
     * @param message Mensagem de erro
     * @param patrimonioId ID do patrimônio que falhou
     */
    data class Error(
        val message: String,
        val patrimonioId: Int
    ) : DetalheState()
}
