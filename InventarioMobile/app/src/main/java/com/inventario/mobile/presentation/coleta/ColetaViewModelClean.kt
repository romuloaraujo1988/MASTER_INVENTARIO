package com.inventario.mobile.presentation.coleta

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.presentation.state.ColetaState
import com.inventario.mobile.utils.MetricsHelper
import com.inventario.mobile.utils.ScanMetrics
import com.inventario.mobile.utils.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel refatorado para coleta de patrimônios.
 *
 * - Segue Clean Architecture + MVVM
 * - Usa Use Cases para lógica de negócio
 * - Gerencia estado da UI
 * - Validações no Use Case (não aqui)
 *
 * ## Histórico
 * - **v2.1:** tracking de métricas para análise de performance.
 * - **v2.10:** feedback tátil (vibração) ao coletar.
 * - **v2.24 (AL-06, correcoes-seguranca maio/2026):**
 *   1. Marcos de tempo migrados de `System.currentTimeMillis()` (wall-clock,
 *      corrompe com mudança de fuso ou sync NTP) para
 *      [SystemClock.elapsedRealtime] (monotônico).
 *   2. Marcos persistidos em [SavedStateHandle] para sobreviver a
 *      ProcessDeath. Sem isso, o Android podia matar o processo durante a
 *      coleta e ao retornar `inicioColeta=0` gerava métrica zerada.
 *   3. Guard `isInitialized` evita que a Activity reinicie o cronômetro
 *      após restauração de estado (seria zerar o tempo válido).
 *
 * **Observação:** os marcos em `elapsedRealtime()` são relativos ao último
 * boot do dispositivo. Se o usuário reiniciou o aparelho entre a abertura
 * da tela e o envio da coleta, o cálculo do tempo retornaria negativo —
 * blindamos com `coerceAtLeast(0)` no [MetricsHelper].
 */
@HiltViewModel
class ColetaViewModelClean @Inject constructor(
    private val buscarPatrimonioUseCase: BuscarPatrimonioUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val syncScheduler: com.inventario.mobile.sync.SyncScheduler,
    private val vibrationHelper: VibrationHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "ColetaViewModelClean"

        // Chaves do SavedStateHandle — os marcos de tempo são preservados
        // durante ProcessDeath para garantir continuidade da métrica.
        private const val KEY_INICIO_COLETA = "ml_inicio_coleta"
        private const val KEY_INICIO_SCAN = "ml_inicio_scan"
        private const val KEY_FIM_SCAN = "ml_fim_scan"
        private const val KEY_INICIO_PREENCH = "ml_inicio_preench"
        private const val KEY_METODO_COLETA = "ml_metodo_coleta"
    }

    private val _state = MutableStateFlow<ColetaState>(ColetaState.Idle)
    val state: StateFlow<ColetaState> = _state.asStateFlow()

    // ========== TRACKING DE MÉTRICAS (v2.1) ==========
    //
    // AL-06: ao invés de `private var` em memória, todos os marcos são
    // acessados via SavedStateHandle para sobreviver a ProcessDeath.
    // Os getters/setters mascaram a leitura do handle para manter o código
    // consumidor inalterado.

    private var inicioColeta: Long
        get() = savedStateHandle[KEY_INICIO_COLETA] ?: 0L
        set(value) { savedStateHandle[KEY_INICIO_COLETA] = value }

    private var inicioScan: Long
        get() = savedStateHandle[KEY_INICIO_SCAN] ?: 0L
        set(value) { savedStateHandle[KEY_INICIO_SCAN] = value }

    private var fimScan: Long
        get() = savedStateHandle[KEY_FIM_SCAN] ?: 0L
        set(value) { savedStateHandle[KEY_FIM_SCAN] = value }

    private var inicioPreenchimento: Long
        get() = savedStateHandle[KEY_INICIO_PREENCH] ?: 0L
        set(value) { savedStateHandle[KEY_INICIO_PREENCH] = value }

    // O nome interno `_metodoColeta` evita colisão de JVM signature com
    // o método público `setMetodoColeta(String)` — ambos gerariam a mesma
    // assinatura `setMetodoColeta(Ljava/lang/String;)V`.
    private var _metodoColeta: String
        get() = savedStateHandle[KEY_METODO_COLETA] ?: MetricsHelper.METODO_QR_CODE
        set(value) { savedStateHandle[KEY_METODO_COLETA] = value }

    // ScanMetrics é transitório: se o processo morrer durante o scan, a
    // métrica específica do scanner é reiniciada (aceitável, já que o
    // usuário precisaria refazer o scan mesmo assim).
    private var scanMetrics: ScanMetrics? = null

    /**
     * Registra uma coleta de patrimônio.
     */
    fun registrarColeta(
        numeroPatrimonio: String,
        localizacaoAtual: String?,
        observacoes: String?,
        latitude: Double?,
        longitude: Double?,
        idUsuario: Long
    ) {
        viewModelScope.launch {
            _state.value = ColetaState.Loading

            registrarColetaUseCase(
                numeroPatrimonio = numeroPatrimonio,
                localizacaoAtual = localizacaoAtual,
                observacoes = observacoes,
                latitude = latitude,
                longitude = longitude,
                idUsuario = idUsuario
            ).fold(
                onSuccess = { coleta ->
                    _state.value = ColetaState.Success(coleta)

                    // Incrementar contador de coletas para sincronização automática
                    syncScheduler.incrementCollectionCount()

                    // v2.10: Vibrar ao coletar (se habilitado nas configurações)
                    vibrationHelper.vibrateOnCollection()
                },
                onFailure = { error ->
                    _state.value = ColetaState.Error(
                        error.message ?: "Erro ao registrar coleta"
                    )
                }
            )
        }
    }

    /**
     * Busca patrimônio por número (para validação antes da coleta).
     */
    fun buscarPatrimonio(numero: String) {
        viewModelScope.launch {
            _state.value = ColetaState.Loading

            buscarPatrimonioUseCase(numero).fold(
                onSuccess = { _ ->
                    _state.value = ColetaState.Idle
                },
                onFailure = { error ->
                    _state.value = ColetaState.Error(
                        error.message ?: "Patrimônio não encontrado"
                    )
                }
            )
        }
    }

    /**
     * Define patrimônio escaneado (para compatibilidade com scanner).
     */
    fun setPatrimonio(patrimonioId: Long, patrimonioCodigo: String) {
        Log.d(TAG, "Patrimônio definido: ID=$patrimonioId, Código=$patrimonioCodigo")
    }

    /**
     * Limpa o estado.
     */
    fun limparEstado() {
        _state.value = ColetaState.Idle
    }

    // ========== MÉTODOS DE TRACKING DE MÉTRICAS (v2.1 + AL-06) ==========

    /**
     * Inicia tracking de tempo da coleta.
     *
     * AL-06: idempotente — se já houver marco (restauração de
     * `SavedStateHandle`), o marco anterior é preservado. Assim, uma
     * Activity recriada após mudança de config ou ProcessDeath não zera
     * o tempo válido.
     */
    fun iniciarColeta() {
        if (inicioColeta > 0) {
            Log.d(TAG, "Coleta já iniciada (restaurada): elapsed=$inicioColeta")
            return
        }
        inicioColeta = SystemClock.elapsedRealtime()
        Log.d(TAG, "Coleta iniciada: elapsed=$inicioColeta")
    }

    /**
     * Inicia tracking de tempo do scan.
     */
    fun iniciarScan() {
        inicioScan = SystemClock.elapsedRealtime()
        fimScan = 0L
        Log.d(TAG, "Scan iniciado: elapsed=$inicioScan")
    }

    /**
     * Finaliza tracking de tempo do scan.
     */
    fun finalizarScan() {
        fimScan = SystemClock.elapsedRealtime()
        Log.d(TAG, "Scan finalizado: elapsed=$fimScan")
    }

    /**
     * Inicia tracking de tempo de preenchimento.
     */
    fun iniciarPreenchimento() {
        if (inicioPreenchimento > 0) {
            Log.d(TAG, "Preenchimento já iniciado (restaurado): elapsed=$inicioPreenchimento")
            return
        }
        inicioPreenchimento = SystemClock.elapsedRealtime()
        Log.d(TAG, "Preenchimento iniciado: elapsed=$inicioPreenchimento")
    }

    /**
     * Define método de coleta.
     */
    fun setMetodoColeta(metodo: String) {
        _metodoColeta = metodo
        Log.d(TAG, "Método de coleta definido: $metodo")
    }

    /**
     * Define métricas do scan (vindo do ScanMetricsTracker).
     */
    fun setScanMetrics(metrics: ScanMetrics) {
        scanMetrics = metrics
        _metodoColeta = metrics.tipoScan
        Log.d(TAG, "Métricas de scan definidas: $metrics")
    }

    /**
     * Calcula métricas de tempo.
     *
     * AL-06: usa [MetricsHelper.calcularTempoElapsedSegundos], que consome
     * marcos de `SystemClock.elapsedRealtime()` com proteção contra
     * diferenças negativas.
     */
    private fun calcularMetricasTempo(): Triple<Int, Int?, Int?> {
        val tempoTotal = MetricsHelper.calcularTempoElapsedSegundos(inicioColeta)

        val tempoScan = if (inicioScan > 0 && fimScan > 0) {
            ((fimScan - inicioScan).coerceAtLeast(0) / 1000).toInt()
        } else {
            scanMetrics?.tempoSegundos
        }

        val tempoPreenchimento = if (inicioPreenchimento > 0) {
            MetricsHelper.calcularTempoElapsedSegundos(inicioPreenchimento)
        } else null

        return Triple(tempoTotal, tempoScan, tempoPreenchimento)
    }

    /**
     * Registra coleta COM métricas.
     */
    fun registrarColetaComMetricas(
        numeroPatrimonio: String,
        localizacaoAtual: String?,
        observacoes: String?,
        latitude: Double?,
        longitude: Double?,
        idUsuario: Long
    ) {
        viewModelScope.launch {
            try {
                _state.value = ColetaState.Loading

                val (tempoTotal, tempoScan, tempoPreenchimento) = calcularMetricasTempo()
                val (_, _, periodo) = MetricsHelper.calcularHorario()

                val tipoScan = scanMetrics?.tipoScan

                val resumo = MetricsHelper.criarResumoMetricas(
                    tempoTotal, tempoScan, tempoPreenchimento, _metodoColeta, periodo
                )
                Log.d(TAG, resumo)

                registrarColetaUseCase(
                    numeroPatrimonio = numeroPatrimonio,
                    localizacaoAtual = localizacaoAtual,
                    observacoes = observacoes,
                    latitude = latitude,
                    longitude = longitude,
                    idUsuario = idUsuario,
                    tempoColetaSegundos = tempoTotal,
                    tempoScanSegundos = tempoScan,
                    tempoPreenchimentoSegundos = tempoPreenchimento,
                    metodoColeta = _metodoColeta,
                    tipoScan = tipoScan
                ).fold(
                    onSuccess = { coleta ->
                        _state.value = ColetaState.Success(coleta)
                        syncScheduler.incrementCollectionCount()
                        vibrationHelper.vibrateOnCollection()

                        // Só resetar métricas após confirmação do Use Case —
                        // em caso de falha preservamos os marcos para retry.
                        resetarMetricas()
                    },
                    onFailure = { error ->
                        _state.value = ColetaState.Error(
                            error.message ?: "Erro ao registrar coleta"
                        )
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao registrar coleta com métricas", e)
                _state.value = ColetaState.Error("Erro ao registrar coleta: ${e.message}")
            }
        }
    }

    /**
     * Reseta métricas para próxima coleta.
     */
    private fun resetarMetricas() {
        inicioColeta = 0L
        inicioScan = 0L
        fimScan = 0L
        inicioPreenchimento = 0L
        _metodoColeta = MetricsHelper.METODO_QR_CODE
        scanMetrics = null
        Log.d(TAG, "Métricas resetadas")
    }

    /**
     * Retorna resumo das métricas atuais (para debug/testes).
     */
    fun getMetricasResumo(): String {
        val (tempoTotal, tempoScan, tempoPreenchimento) = calcularMetricasTempo()
        val (_, _, periodo) = MetricsHelper.calcularHorario()

        return MetricsHelper.criarResumoMetricas(
            tempoTotal, tempoScan, tempoPreenchimento, _metodoColeta, periodo
        )
    }
}
