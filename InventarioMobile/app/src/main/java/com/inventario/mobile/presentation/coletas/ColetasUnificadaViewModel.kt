package com.inventario.mobile.presentation.coletas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.migration.ColetaMigration
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.model.StatusFiltro
import com.inventario.mobile.domain.usecase.AgruparColetasPorSalaUseCase
import com.inventario.mobile.domain.usecase.BuscarColetasComFallbackUseCase
import com.inventario.mobile.domain.usecase.BuscarSalasComColetasUseCase
import com.inventario.mobile.domain.usecase.ExcluirColetaPendenteUseCase
import com.inventario.mobile.domain.usecase.FiltrarColetasUseCase
import com.inventario.mobile.domain.usecase.ObterUsuarioAtualUseCase
import com.inventario.mobile.domain.usecase.ReenviarColetaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel unificado para a Tela_Coletas_Unificada.
 *
 * Funde a lógica de [ColetasViewModelClean] (ColetasActivityClean) e
 * [com.inventario.mobile.presentation.coleta.CollectionViewViewModelClean]
 * (CollectionViewActivity), eliminando a duplicação e concentrando todas as
 * funcionalidades em um único ViewModel.
 *
 * Funcionalidades:
 * - Carregamento offline-first com fallback automático (BuscarColetasComFallbackUseCase)
 * - Migração de coletas antigas (ColetaMigration)
 * - Filtro por texto (numeroPatrimonio / descricaoPatrimonio)
 * - Filtro por status (TODOS, COLETADOS, PENDENTES, SEM_ETIQUETA)
 * - Filtro por usuário logado ("Minhas Coletas")
 * - Filtro por sala
 * - Visualização agrupada por sala
 * - Reenvio de coleta pendente
 * - Exclusão de coleta pendente
 * - Contadores de total e pendentes
 *
 * Requisitos: 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12, 4.6, 4.7, 4.8
 */
@HiltViewModel
class ColetasUnificadaViewModel @Inject constructor(
    private val buscarColetasComFallbackUseCase: BuscarColetasComFallbackUseCase,
    private val filtrarColetasUseCase: FiltrarColetasUseCase,
    private val agruparColetasPorSalaUseCase: AgruparColetasPorSalaUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase,
    private val buscarSalasComColetasUseCase: BuscarSalasComColetasUseCase,
    private val reenviarColetaUseCase: ReenviarColetaUseCase,
    private val excluirColetaPendenteUseCase: ExcluirColetaPendenteUseCase,
    private val coletaMigration: ColetaMigration
) : ViewModel() {

    companion object {
        private const val TAG = "ColetasUnificadaVM"
    }

    // ─── Estado da UI ────────────────────────────────────────────────────────

    private val _state = MutableStateFlow<ColetasUnificadaState>(ColetasUnificadaState.Idle)
    val state: StateFlow<ColetasUnificadaState> = _state.asStateFlow()

    // ─── Cache interno ───────────────────────────────────────────────────────

    /** Todas as coletas carregadas (antes de qualquer filtro). */
    private var todasColetas: List<Coleta> = emptyList()

    /** Salas obtidas do servidor (para o spinner de filtro). */
    private var salasDoServidor: List<String> = emptyList()

    /** Nome do usuário logado (para filtro "Minhas Coletas"). */
    private var nomeUsuarioAtual: String? = null

    /** ID do usuário logado (para filtro por usuarioId). */
    private var idUsuarioAtual: Int? = null

    // ─── Filtros ativos ──────────────────────────────────────────────────────

    /** Texto de busca livre (número de patrimônio ou descrição). Requisito 2.2 */
    private var textoBusca: String = ""

    /** Status selecionado via chips. Requisito 2.3 */
    private var filtroStatus: StatusFiltro = StatusFiltro.TODOS

    /** Indica se o chip "Minhas Coletas" está ativo. Requisito 2.4 */
    private var filtroUsuarioAtivo: Boolean = false

    /** Nome da sala selecionada no spinner (null = todas). Requisito 2.5 */
    private var filtroSala: String? = null

    /** Indica se a visualização agrupada por sala está ativa. Requisito 2.6 */
    private var visualizacaoAgrupadaAtiva: Boolean = false

    // ─── Ações públicas ──────────────────────────────────────────────────────

    /**
     * Carrega (ou recarrega) as coletas.
     *
     * Fluxo:
     * 1. Executa migração de coletas antigas se necessário.
     * 2. Obtém o usuário logado.
     * 3. Busca salas com coletas do servidor (para o spinner).
     * 4. Busca coletas com fallback automático (servidor → local).
     * 5. Aplica os filtros ativos e emite [ColetasUnificadaState.Success].
     *
     * Requisitos: 2.1, 4.6
     */
    fun carregarColetas() {
        viewModelScope.launch {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "CARREGANDO COLETAS")
            _state.value = ColetasUnificadaState.Loading

            try {
                // 1. Migração de coletas antigas
                if (coletaMigration.precisaMigracao()) {
                    Log.d(TAG, "⚠ Coletas antigas precisam de migração, executando...")
                    coletaMigration.migrarColetasAntigas().fold(
                        onSuccess = { result ->
                            Log.d(TAG, "✓ Migração concluída: ${result.atualizadas} coletas atualizadas")
                        },
                        onFailure = { error ->
                            Log.w(TAG, "Erro na migração de coletas antigas: ${error.message}")
                        }
                    )
                }

                // 2. Obter usuário logado
                val usuario = obterUsuarioAtualUseCase()
                nomeUsuarioAtual = usuario?.nome
                idUsuarioAtual = usuario?.id?.toInt()
                Log.d(TAG, "Usuário atual: nome=$nomeUsuarioAtual, id=$idUsuarioAtual")

                // 3. Buscar salas com coletas do servidor (para o spinner de filtro)
                buscarSalasComColetasUseCase().fold(
                    onSuccess = { salas ->
                        Log.d(TAG, "✓ ${salas.size} salas com coletas carregadas do servidor")
                        salasDoServidor = salas
                    },
                    onFailure = { erro ->
                        Log.w(TAG, "⚠ Erro ao buscar salas do servidor, usando fallback local: ${erro.message}")
                        salasDoServidor = emptyList()
                    }
                )

                // 4. Buscar coletas com fallback automático
                buscarColetasComFallbackUseCase().fold(
                    onSuccess = { resultado ->
                        Log.d(TAG, "✓ ${resultado.coletas.size} coletas carregadas (fonte: ${resultado.fonte})")
                        todasColetas = resultado.coletas
                        aplicarFiltrosEAtualizarEstado()
                    },
                    onFailure = { erro ->
                        Log.e(TAG, "✗ Erro ao carregar coletas", erro)
                        _state.value = ColetasUnificadaState.Error(
                            erro.message ?: "Erro desconhecido ao carregar coletas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "✗ Exceção ao carregar coletas", e)
                _state.value = ColetasUnificadaState.Error(
                    e.message ?: "Erro desconhecido ao carregar coletas"
                )
            }

            Log.d(TAG, "═══════════════════════════════════════")
        }
    }

    /**
     * Aplica filtro de busca por texto livre.
     *
     * Filtra coletas cujo [Coleta.numeroPatrimonio] ou [Coleta.descricaoPatrimonio]
     * contém [query] (comparação case-insensitive).
     *
     * @param query Texto de busca. String vazia remove o filtro de texto.
     * Requisito 2.2
     */
    fun filtrarPorTexto(query: String) {
        Log.d(TAG, "filtrarPorTexto: '$query'")
        textoBusca = query
        aplicarFiltrosEAtualizarEstado()
    }

    /**
     * Aplica filtro por status de sincronização.
     *
     * @param status [StatusFiltro.TODOS], [StatusFiltro.COLETADOS],
     *               [StatusFiltro.PENDENTES] ou [StatusFiltro.SEM_ETIQUETA].
     * Requisitos 2.3, 4.7
     */
    fun filtrarPorStatus(status: StatusFiltro) {
        Log.d(TAG, "filtrarPorStatus: $status")
        filtroStatus = status
        aplicarFiltrosEAtualizarEstado()
    }

    /**
     * Ativa ou desativa o filtro "Minhas Coletas".
     *
     * Quando [ativo] é true, exibe apenas coletas do usuário logado.
     *
     * @param ativo true para ativar o filtro, false para desativar.
     * Requisito 2.4
     */
    fun filtrarPorUsuario(ativo: Boolean) {
        Log.d(TAG, "filtrarPorUsuario: $ativo")
        filtroUsuarioAtivo = ativo
        aplicarFiltrosEAtualizarEstado()
    }

    /**
     * Aplica filtro por sala.
     *
     * @param sala Nome da sala selecionada no spinner, ou null para exibir todas.
     * Requisito 2.5
     */
    fun filtrarPorSala(sala: String?) {
        Log.d(TAG, "filtrarPorSala: '${sala ?: "TODAS"}'")
        filtroSala = sala
        aplicarFiltrosEAtualizarEstado()
    }

    /**
     * Reenvia uma coleta pendente ao servidor.
     *
     * Emite [ColetasUnificadaState.ColetaReenviada] em caso de sucesso,
     * ou [ColetasUnificadaState.Error] em caso de falha.
     * Após o reenvio bem-sucedido, recarrega a lista automaticamente.
     *
     * @param coletaId ID da coleta a ser reenviada.
     * Requisito 2.8
     */
    fun reenviarColeta(coletaId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "reenviarColeta: id=$coletaId")
            _state.value = ColetasUnificadaState.Loading

            reenviarColetaUseCase(coletaId).fold(
                onSuccess = {
                    Log.d(TAG, "✅ Coleta $coletaId reenviada com sucesso")
                    _state.value = ColetasUnificadaState.ColetaReenviada("Coleta reenviada com sucesso!")
                    carregarColetas()
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Erro ao reenviar coleta $coletaId", error)
                    _state.value = ColetasUnificadaState.Error(
                        error.message ?: "Erro ao reenviar coleta"
                    )
                }
            )
        }
    }

    /**
     * Exclui uma coleta pendente (não sincronizada).
     *
     * Emite [ColetasUnificadaState.ColetaExcluida] em caso de sucesso,
     * ou [ColetasUnificadaState.Error] em caso de falha (inclusive se a coleta
     * já estiver sincronizada — Requisito 2.10).
     * Após a exclusão bem-sucedida, recarrega a lista automaticamente.
     *
     * @param coletaId ID da coleta a ser excluída.
     * Requisito 2.9
     */
    fun excluirColeta(coletaId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "excluirColeta: id=$coletaId")
            _state.value = ColetasUnificadaState.Loading

            excluirColetaPendenteUseCase(coletaId).fold(
                onSuccess = {
                    Log.d(TAG, "✅ Coleta $coletaId excluída com sucesso")
                    _state.value = ColetasUnificadaState.ColetaExcluida("Coleta excluída com sucesso!")
                    carregarColetas()
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Erro ao excluir coleta $coletaId", error)
                    _state.value = ColetasUnificadaState.Error(
                        error.message ?: "Erro ao excluir coleta"
                    )
                }
            )
        }
    }

    /**
     * Alterna entre visualização plana e visualização agrupada por sala.
     *
     * Quando ativa, [ColetasUnificadaState.Success.coletasAgrupadas] é preenchido
     * com o resultado de [AgruparColetasPorSalaUseCase].
     *
     * Requisito 4.8
     */
    fun toggleVisualizacaoAgrupada() {
        visualizacaoAgrupadaAtiva = !visualizacaoAgrupadaAtiva
        Log.d(TAG, "toggleVisualizacaoAgrupada: $visualizacaoAgrupadaAtiva")
        aplicarFiltrosEAtualizarEstado()
    }

    // ─── Lógica interna ──────────────────────────────────────────────────────

    /**
     * Aplica todos os filtros ativos sobre [todasColetas] e emite um novo
     * [ColetasUnificadaState.Success] com os dados calculados.
     *
     * Requisitos: 2.2–2.12, 4.6–4.8
     */
    private fun aplicarFiltrosEAtualizarEstado() {
        // Aplicar filtros combinados
        val coletasFiltradas = filtrarColetasUseCase(
            coletas = todasColetas,
            usuarioAtual = nomeUsuarioAtual,
            filtroUsuario = filtroUsuarioAtivo,
            filtroSala = filtroSala,
            filtroStatus = filtroStatus,
            textoPesquisa = textoBusca
        )

        // Calcular contadores sobre a lista filtrada (Requisito 2.11)
        val totalColetas = coletasFiltradas.size
        val totalPendentes = coletasFiltradas.count { !it.sincronizado }

        // Salas disponíveis para o spinner: preferir dados do servidor (Requisito 2.5)
        val salasDisponiveis = if (salasDoServidor.isNotEmpty()) {
            Log.d(TAG, "Usando ${salasDoServidor.size} salas do servidor para o spinner")
            salasDoServidor
        } else {
            Log.d(TAG, "Usando salas extraídas das coletas locais para o spinner")
            filtrarColetasUseCase.extrairSalasDisponiveis(todasColetas)
        }

        // Agrupar por sala se a visualização agrupada estiver ativa (Requisito 4.8)
        val coletasAgrupadas: Map<String, List<Coleta>> = if (visualizacaoAgrupadaAtiva) {
            agruparColetasPorSalaUseCase(coletasFiltradas)
        } else {
            emptyMap()
        }

        Log.d(TAG, "Filtros aplicados → ${coletasFiltradas.size} coletas " +
                "(total=$totalColetas, pendentes=$totalPendentes, " +
                "salas=${salasDisponiveis.size}, agrupada=$visualizacaoAgrupadaAtiva)")

        _state.value = ColetasUnificadaState.Success(
            coletas = coletasFiltradas,
            totalColetas = totalColetas,
            totalPendentes = totalPendentes,
            filtroUsuario = filtroUsuarioAtivo,
            filtroSala = filtroSala,
            filtroStatus = filtroStatus,
            salasDisponiveis = salasDisponiveis,
            coletasAgrupadas = coletasAgrupadas,
            visualizacaoAgrupada = visualizacaoAgrupadaAtiva
        )
    }
}
