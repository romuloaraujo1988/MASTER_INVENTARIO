package com.inventario.mobile.presentation.state

import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.domain.model.EstatisticasSala
import com.inventario.mobile.domain.model.FiltroColetaSala
import com.inventario.mobile.domain.model.SalaComProgresso

/**
 * Estados possíveis da tela de Inventário por Sala.
 * Usa sealed class para estados mutuamente exclusivos.
 */
sealed class InventarioPorSalaState {
    
    /**
     * Estado inicial - aguardando ação do usuário
     */
    object Idle : InventarioPorSalaState()
    
    /**
     * Estado de carregamento
     */
    object Loading : InventarioPorSalaState()
    
    /**
     * Estado quando as salas foram carregadas com sucesso
     * 
     * @property salas Lista completa de salas com progresso
     * @property salasFiltradas Lista de salas filtradas pela busca
     * @property queryBusca Termo de busca atual
     */
    data class SalasCarregadas(
        val salas: List<SalaComProgresso>,
        val salasFiltradas: List<SalaComProgresso> = salas,
        val queryBusca: String = ""
    ) : InventarioPorSalaState()
    
    /**
     * Estado quando uma sala foi selecionada
     * 
     * @property sala Sala selecionada com progresso
     * @property estatisticas Estatísticas detalhadas da sala
     * @property patrimonios Lista de patrimônios da sala
     * @property filtroAtual Filtro de coleta aplicado
     * @property currentPage Página atual da paginação
     * @property hasMorePages Se há mais páginas para carregar
     * @property isLoadingMore Se está carregando mais itens
     */
    data class SalaSelecionada(
        val sala: SalaComProgresso,
        val estatisticas: EstatisticasSala,
        val patrimonios: List<Patrimonio>,
        val filtroAtual: FiltroColetaSala = FiltroColetaSala.TODOS,
        val currentPage: Int = 0,
        val hasMorePages: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : InventarioPorSalaState()
    
    /**
     * Estado de erro
     * 
     * @property message Mensagem de erro para exibição
     */
    data class Error(val message: String) : InventarioPorSalaState()
}
