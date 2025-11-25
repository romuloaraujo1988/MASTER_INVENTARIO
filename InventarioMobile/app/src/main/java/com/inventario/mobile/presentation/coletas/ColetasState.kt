package com.inventario.mobile.presentation.coletas

import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.model.StatusFiltro

/**
 * Estados possíveis da tela de visualização de coletas
 * Sealed class para estados mutuamente exclusivos
 * 
 * @see Requirements 7.1, 7.2, 7.3
 */
sealed class ColetasState {
    
    /**
     * Estado inicial - aguardando ação
     */
    object Idle : ColetasState()
    
    /**
     * Estado de carregamento - buscando dados
     */
    object Loading : ColetasState()
    
    /**
     * Estado de sucesso - dados carregados
     * 
     * @param coletas Lista de coletas filtradas
     * @param totalColetados Total de patrimônios coletados
     * @param totalPendentes Total de patrimônios pendentes
     * @param filtroUsuario Se o filtro por usuário está ativo
     * @param filtroSala Sala selecionada para filtro (null = todas)
     * @param filtroStatus Status selecionado para filtro
     * @param salasDisponiveis Lista de salas disponíveis para filtro
     * @param coletasAgrupadas Coletas agrupadas por sala (quando visualização agrupada está ativa)
     * @param visualizacaoAgrupada Se a visualização agrupada está ativa
     */
    data class Success(
        val coletas: List<Coleta>,
        val totalColetados: Int,
        val totalPendentes: Int,
        val filtroUsuario: Boolean = true,
        val filtroSala: String? = null,
        val filtroStatus: StatusFiltro = StatusFiltro.TODOS,
        val salasDisponiveis: List<String> = emptyList(),
        val coletasAgrupadas: Map<String, List<Coleta>> = emptyMap(),
        val visualizacaoAgrupada: Boolean = false
    ) : ColetasState()
    
    /**
     * Estado de erro - falha ao carregar dados
     * 
     * @param message Mensagem de erro para exibir ao usuário
     */
    data class Error(val message: String) : ColetasState()
}
