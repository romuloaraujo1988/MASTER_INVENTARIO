package com.inventario.mobile.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.model.EvolucaoColeta
import com.inventario.mobile.domain.repository.DashboardRepository
import com.inventario.mobile.domain.usecase.BuscarEstatisticasDashboardUseCase
import com.inventario.mobile.domain.usecase.BuscarEvolucaoColetasUseCase
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Clean para Dashboard
 * Usa Clean Architecture com Use Cases e injeta a INTERFACE DashboardRepository
 * (Req 1.6, 5.4) — nunca a implementação concreta.
 *
 * v2.4: Adicionado suporte a Flow reativo para estatísticas.
 * v2.20.6: Método `observarEstatisticasHibridas` não cria mais coroutine órfã
 *          — o Fragment é o único collector.
 * v3.0 (task 6.1): Introduzido `fonteEstatisticas: StateFlow<DashboardStats>` como
 *          FONTE ÚNICA de verdade para os KPIs do dashboard (Req 2.1, 2.2, 2.4).
 *          - Construída via `combine(inventarioIdFlow, refreshTrigger)` +
 *            `distinctUntilChanged` + `flatMapLatest { dashboardRepository.observarEstatisticasHibridas(it) }`
 *          - `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`
 *            preserva o último valor por 5 s após o último subscriber (Req 2.2, 6.1–6.3).
 *          - `refresh()` dispara nova emissão via `refreshTrigger.tryEmit(Unit)` (Req 2.5, 2.6).
 *          - `catch` faz fallback para `buscarEstatisticasLocais` mantendo `isOfflineData = true`
 *            (Req 2.9, 3.5, 4.1).
 *          - O método `observarEstatisticasHibridas(inventarioId)` continua existindo como
 *            `@Deprecated` apenas para não quebrar o `DashboardFragment` antes que a task 7.1
 *            migre-o para `viewModel.fonteEstatisticas`. Após a task 7.1, esse método pode
 *            ser removido — Req 2.4 é satisfeito quando nenhum consumidor o coleta como Flow
 *            distinto.
 *          - `loadDashboardData`, `loadColetasEvolucao`, `refreshData` permanecem para outras
 *            áreas da UI (loading/erro e gráfico de evolução), conforme design.
 */
@HiltViewModel
class DashboardViewModelClean @Inject constructor(
    private val buscarEstatisticasDashboardUseCase: BuscarEstatisticasDashboardUseCase,
    private val buscarEvolucaoColetasUseCase: BuscarEvolucaoColetasUseCase,
    private val dashboardRepository: DashboardRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModelClean"
    }

    private val _uiState = MutableStateFlow(DashboardUiStateClean())
    val uiState: StateFlow<DashboardUiStateClean> = _uiState.asStateFlow()

    // ========================================
    // 🔄 FONTE ÚNICA DE VERDADE (task 6.1)
    // ========================================

    /**
     * Inventário ativo observado como `StateFlow<Int?>` para propagar mudanças
     * no id ativo. Inicializado com o valor atual do `PreferencesManager`.
     *
     * Hoje o id é um snapshot constante durante o ciclo de vida do ViewModel;
     * expor como `StateFlow` já deixa a topologia preparada para reagir a
     * trocas de inventário no futuro, sem recriar a pipeline.
     */
    private val inventarioIdFlow: StateFlow<Int?> =
        MutableStateFlow(preferencesManager.getInventarioAtivoId())

    /**
     * Disparador manual de refresh — pull-to-refresh e invalidações de cache.
     *
     * - `replay = 1` garante que novos subscribers recebam a última emissão e
     *   que `combine` emita imediatamente na primeira inscrição.
     * - `onBufferOverflow = DROP_OLDEST` + `extraBufferCapacity = 0` preserva
     *   apenas o trigger mais recente, atendendo Req 2.6 (deduplicação de
     *   refresh concorrente) quando combinado com `flatMapLatest` a jusante.
     * - A emissão inicial `tryEmit(Unit)` garante que a pipeline comece ativa
     *   assim que o primeiro subscriber chegar.
     */
    private val refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).apply { tryEmit(Unit) }

    /**
     * `fonteEstatisticas` — única origem de KPIs consumida pela UI (Req 2.1).
     *
     * Pipeline:
     * 1. `combine(inventarioIdFlow, refreshTrigger)` — recomputa sempre que o
     *    id mudar ou `refresh()` for chamado.
     * 2. `distinctUntilChanged()` — evita recomputar para o mesmo id sem refresh.
     * 3. `flatMapLatest { dashboardRepository.observarEstatisticasHibridas(it) }`
     *    — cancela o upstream anterior ao receber novo trigger/id (Req 2.6).
     * 4. `catch` — se o Flow upstream falhar inesperadamente, emite um fallback
     *    offline calculado por `buscarEstatisticasLocais` com `isOfflineData = true`
     *    (Req 2.9). Observação: `observarEstatisticasHibridas` já implementa seu
     *    próprio fallback interno; este `catch` é uma rede de segurança para
     *    exceções fora do contrato.
     * 5. `stateIn(WhileSubscribed(5000))` — mantém o upstream por 5 s após o
     *    último subscriber, permitindo rotação de tela sem nova chamada HTTP
     *    (Req 2.2, 6.1–6.3).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val fonteEstatisticas: StateFlow<DashboardStats> =
        combine(inventarioIdFlow, refreshTrigger) { invId, _ ->
            // Empacota invId + marca-tempo do trigger para NÃO ser filtrado pelo
            // distinctUntilChanged quando o usuário faz pull-to-refresh e o
            // invId não mudou. Bug-fix v2.20.1: antes usávamos só `invId` e o
            // distinctUntilChanged engolia refreshes subsequentes, o que
            // impedia a primeira chamada HTTP a `api/mobile/dashboard/stats`
            // em alguns cenários de inicialização.
            Pair(invId, System.nanoTime())
        }
            .distinctUntilChanged()
            .onEach { (invId, stamp) ->
                Log.d(TAG, "🔄 FonteEstatisticas: disparando upstream (invId=$invId, stamp=$stamp)")
            }
            .flatMapLatest { (invId, _) ->
                Log.d(TAG, "🔄 FonteEstatisticas: flatMapLatest → observarEstatisticasHibridas($invId)")
                dashboardRepository.observarEstatisticasHibridas(invId)
            }
            .onEach { stats ->
                Log.d(
                    TAG,
                    "🔄 FonteEstatisticas: emissão upstream (total=${stats.totalPatrimonios}, coletados=${stats.totalColetados}, offline=${stats.isOfflineData})"
                )
            }
            .catch { e ->
                Log.w(TAG, "fonteEstatisticas: falha inesperada, caindo para fallback offline", e)
                val invId = inventarioIdFlow.value
                val fallback = dashboardRepository
                    .buscarEstatisticasLocais(invId)
                    .getOrElse { DashboardStats.empty(invId, isOfflineData = true) }
                // Marca como offline mas MANTÉM os dados da última sincronização
                // bem-sucedida (vindos do CacheServerStats via buscarEstatisticasLocais).
                // Bug 08/05/2026: antes emitíamos `fallback.copy(isOfflineData = true)`
                // que é correto; agora apenas garantimos que o log torna claro o motivo.
                Log.d(TAG, "fonteEstatisticas.catch: emitindo fallback offline — total=${fallback.totalPatrimonios}, coletados=${fallback.totalColetados}")
                emit(fallback.copy(isOfflineData = true))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = DashboardStats.empty(preferencesManager.getInventarioAtivoId())
            )

    /**
     * Dispara um refresh manual da `fonteEstatisticas` (Req 2.5).
     * Pull-to-refresh do Fragment deve chamar este método.
     *
     * Graças a `flatMapLatest`, múltiplas chamadas concorrentes cancelam o
     * upstream anterior, evitando chamadas HTTP concorrentes (Req 2.6).
     */
    fun refresh() {
        Log.d(TAG, "🔄 refresh() chamado — emitindo novo trigger")
        refreshTrigger.tryEmit(Unit)
    }

    init {
        // Bug-fix v2.20.1: o tryEmit(Unit) no refreshTrigger (durante construção
        // do campo) pode ser consumido antes que qualquer subscriber anexe.
        // Emitir novamente após o init garante que a pipeline dispare assim que
        // o Fragment começar a coletar `fonteEstatisticas`.
        refreshTrigger.tryEmit(Unit)
        Log.d(TAG, "DashboardViewModelClean init: refreshTrigger re-emitido")
    }

    // ========================================
    // UI Secundária: loading/erro e gráfico de evolução
    // ========================================

    /**
     * Carrega dados do dashboard via Use Case.
     *
     * `fonteEstatisticas` é a fonte primária dos KPIs; este método continua
     * alimentando `_uiState.dashboardStats/isLoading/error`, que algumas áreas
     * da UI ainda consomem (mensagens de erro, indicador de loading inicial).
     */
    fun loadDashboardData(inventarioId: Int? = null) {
        viewModelScope.launch {
            Log.d(TAG, "🟢 loadDashboardData() INICIADO — inventarioId=$inventarioId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "🟢 Chamando buscarEstatisticasDashboardUseCase($inventarioId)...")
                val result = buscarEstatisticasDashboardUseCase(inventarioId)
                Log.d(TAG, "🟢 UseCase retornou: isSuccess=${result.isSuccess}")
                result.fold(
                    onSuccess = { stats ->
                        Log.d(
                            TAG,
                            "🟢 loadDashboardData SUCCESS: total=${stats.totalPatrimonios} coletados=${stats.totalColetados} offline=${stats.isOfflineData}"
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dashboardStats = stats,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "🔴 loadDashboardData FAILURE", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "🔴 loadDashboardData EXCEPTION (fora do Result)", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Carrega evolução de coletas (gráfico de linha).
     * Mantido separado de `fonteEstatisticas` conforme design.
     */
    fun loadColetasEvolucao(inventarioId: Int? = null, dias: Int = 30) {
        viewModelScope.launch {
            Log.d(TAG, "Carregando evolução de coletas (últimos $dias dias)...")
            _uiState.value = _uiState.value.copy(isLoadingGrafico = true, graficoError = null)

            buscarEvolucaoColetasUseCase(inventarioId, dias).fold(
                onSuccess = { evolucao ->
                    Log.d(TAG, "Evolução carregada: ${evolucao.size} dias")
                    _uiState.value = _uiState.value.copy(
                        isLoadingGrafico = false,
                        coletasEvolucao = evolucao,
                        graficoError = null
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao carregar evolução", error)
                    _uiState.value = _uiState.value.copy(
                        isLoadingGrafico = false,
                        graficoError = error.message
                    )
                }
            )
        }
    }

    /**
     * Atualiza todos os dados (KPIs via `loadDashboardData` e gráfico via
     * `loadColetasEvolucao`). Mantido para compatibilidade com o Fragment
     * atual; após a task 7.1, o Fragment usará `refresh()` para os KPIs.
     */
    fun refreshData(inventarioId: Int? = null) {
        loadDashboardData(inventarioId)
        loadColetasEvolucao(inventarioId)
    }

    // ========================================
    // 🔄 MÉTODOS DEPRECATED (removidos após task 7.1)
    // ========================================

    /**
     * @deprecated Use [fonteEstatisticas]. Mantido temporariamente para não
     * quebrar o `DashboardFragment` antes que a task 7.1 migre o Fragment para
     * coletar apenas `fonteEstatisticas`. Após a task 7.1 este método pode
     * ser removido, satisfazendo Req 2.4 (nenhum Flow distinto exposto).
     */
    @Deprecated(
        message = "Use fonteEstatisticas (StateFlow). Este método será removido após a task 7.1.",
        replaceWith = ReplaceWith("fonteEstatisticas")
    )
    fun observarEstatisticasHibridas(inventarioId: Int?): Flow<DashboardStats> {
        return dashboardRepository.observarEstatisticasHibridas(inventarioId)
    }
}

/**
 * Estado da UI do Dashboard (Clean)
 */
data class DashboardUiStateClean(
    val isLoading: Boolean = false,
    val isLoadingGrafico: Boolean = false,
    val dashboardStats: DashboardStats? = null,
    val coletasEvolucao: List<EvolucaoColeta> = emptyList(),
    val error: String? = null,
    val graficoError: String? = null
)
