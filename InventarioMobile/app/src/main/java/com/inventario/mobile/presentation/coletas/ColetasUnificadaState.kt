package com.inventario.mobile.presentation.coletas

import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.model.StatusFiltro

/**
 * Estado unificado da Tela_Coletas_Unificada.
 *
 * Funde os estados de [ColetasState] (ColetasActivityClean) e
 * [com.inventario.mobile.presentation.state.CollectionViewState] (CollectionViewActivity),
 * eliminando a duplicação e concentrando todas as funcionalidades em um único estado.
 *
 * Funcionalidades cobertas:
 * - Busca por texto (numeroPatrimonio / descricaoPatrimonio)
 * - Chips de filtro por status (Todos, Coletados, Pendentes, Sem Etiqueta)
 * - Chip "Minhas Coletas" (filtroUsuario)
 * - Spinner de filtro por sala (filtroSala)
 * - Visualização agrupada por sala (coletasAgrupadas / visualizacaoAgrupada)
 * - Menu de contexto long-click: Reenviar / Excluir coleta pendente
 * - Contadores de total e pendentes
 * - Pull-to-refresh
 *
 * Requisitos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12
 */
sealed class ColetasUnificadaState {

    /**
     * Estado inicial — aguardando a primeira ação do usuário ou do ciclo de vida.
     */
    object Idle : ColetasUnificadaState()

    /**
     * Estado de carregamento — dados sendo buscados (local ou remoto).
     */
    object Loading : ColetasUnificadaState()

    /**
     * Estado de sucesso — dados carregados e filtros aplicados.
     *
     * @param coletas Lista de coletas após aplicação de todos os filtros ativos.
     * @param totalColetas Total de coletas na lista filtrada (Requisito 2.11).
     * @param totalPendentes Total de coletas pendentes de sincronização na lista filtrada (Requisito 2.11).
     * @param filtroUsuario Indica se o filtro "Minhas Coletas" está ativo (Requisito 2.4).
     * @param filtroSala Nome da sala selecionada no spinner, ou null para todas as salas (Requisito 2.5).
     * @param filtroStatus Status selecionado via chips: TODOS, COLETADOS, PENDENTES ou SEM_ETIQUETA (Requisito 2.3).
     * @param salasDisponiveis Lista de nomes de salas disponíveis para o spinner de filtro (Requisito 2.5).
     * @param coletasAgrupadas Mapa de sala → lista de coletas, usado quando [visualizacaoAgrupada] é true (Requisito 2.6).
     * @param visualizacaoAgrupada Indica se a lista deve ser exibida agrupada por sala (Requisito 2.6).
     */
    data class Success(
        val coletas: List<Coleta>,
        val totalColetas: Int,
        val totalPendentes: Int,
        val filtroUsuario: Boolean = false,
        val filtroSala: String? = null,
        val filtroStatus: StatusFiltro = StatusFiltro.TODOS,
        val salasDisponiveis: List<String> = emptyList(),
        val coletasAgrupadas: Map<String, List<Coleta>> = emptyMap(),
        val visualizacaoAgrupada: Boolean = false
    ) : ColetasUnificadaState()

    /**
     * Estado de erro — falha ao carregar ou processar dados.
     *
     * @param message Mensagem descritiva do erro para exibição ao usuário.
     */
    data class Error(val message: String) : ColetasUnificadaState()

    /**
     * Estado transitório emitido após o reenvio bem-sucedido de uma coleta pendente (Requisito 2.8).
     *
     * A Activity deve exibir [message] via Toast/Snackbar e, em seguida,
     * recarregar a lista para refletir o novo status da coleta.
     *
     * @param message Mensagem de confirmação do reenvio.
     */
    data class ColetaReenviada(val message: String) : ColetasUnificadaState()

    /**
     * Estado transitório emitido após a exclusão bem-sucedida de uma coleta pendente (Requisito 2.9).
     *
     * A Activity deve exibir [message] via Toast/Snackbar e, em seguida,
     * recarregar a lista para refletir a remoção da coleta.
     *
     * @param message Mensagem de confirmação da exclusão.
     */
    data class ColetaExcluida(val message: String) : ColetasUnificadaState()
}
