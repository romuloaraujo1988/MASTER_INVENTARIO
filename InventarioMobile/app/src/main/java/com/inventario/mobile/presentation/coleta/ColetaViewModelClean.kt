package com.inventario.mobile.presentation.coleta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.presentation.state.ColetaState
import com.inventario.mobile.utils.MetricsHelper
import com.inventario.mobile.utils.ScanMetrics
import com.inventario.mobile.utils.ScanMetricsTracker
import com.inventario.mobile.utils.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel refatorado para coleta de patrimônios
 * Segue Clean Architecture + MVVM
 * 
 * - Usa Use Cases para lógica de negócio
 * - Gerencia estado da UI
 * - Validações no Use Case (não aqui)
 * 
 * v2.1: Adicionado tracking de métricas para análise de performance
 * v2.10: Adicionado feedback tátil (vibração) ao coletar
 */
@HiltViewModel
class ColetaViewModelClean @Inject constructor(
    private val buscarPatrimonioUseCase: BuscarPatrimonioUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val syncScheduler: com.inventario.mobile.sync.SyncScheduler,
    private val vibrationHelper: VibrationHelper
) : ViewModel() {
    
    companion object {
        private const val TAG = "ColetaViewModelClean"
    }
    
    private val _state = MutableStateFlow<ColetaState>(ColetaState.Idle)
    val state: StateFlow<ColetaState> = _state.asStateFlow()
    
    // ========== TRACKING DE MÉTRICAS (v2.1) ==========
    private var inicioColeta: Long = 0
    private var inicioScan: Long = 0
    private var fimScan: Long = 0
    private var inicioPreenchimento: Long = 0
    private var metodoColeta: String = MetricsHelper.METODO_QR_CODE
    private var scanMetrics: ScanMetrics? = null
    
    /**
     * Registra uma coleta de patrimônio
     * 
     * @param numeroPatrimonio Número do patrimônio (QR Code ou manual)
     * @param localizacaoAtual Localização onde foi encontrado
     * @param observacoes Observações (opcional)
     * @param latitude Latitude GPS (opcional)
     * @param longitude Longitude GPS (opcional)
     * @param idUsuario ID do usuário coletor
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
     * Busca patrimônio por número (para validação antes da coleta)
     */
    fun buscarPatrimonio(numero: String) {
        viewModelScope.launch {
            _state.value = ColetaState.Loading
            
            buscarPatrimonioUseCase(numero).fold(
                onSuccess = { patrimonio ->
                    // Patrimônio encontrado, pode prosseguir
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
     * Define patrimônio escaneado (para compatibilidade com scanner)
     */
    fun setPatrimonio(patrimonioId: Long, patrimonioCodigo: String) {
        // TODO: Implementar se necessário armazenar estado do patrimônio
        // Por enquanto, apenas log
        android.util.Log.d("ColetaViewModelClean", "Patrimônio definido: ID=$patrimonioId, Código=$patrimonioCodigo")
    }
    
    /**
     * Limpa o estado
     */
    fun limparEstado() {
        _state.value = ColetaState.Idle
    }
    
    // ========== MÉTODOS DE TRACKING DE MÉTRICAS (v2.1) ==========
    
    /**
     * Inicia tracking de tempo da coleta
     * Chamar no onCreate da Activity
     */
    fun iniciarColeta() {
        inicioColeta = System.currentTimeMillis()
        Log.d(TAG, "Coleta iniciada: timestamp=$inicioColeta")
    }
    
    /**
     * Inicia tracking de tempo do scan
     * Chamar ao abrir o scanner
     */
    fun iniciarScan() {
        inicioScan = System.currentTimeMillis()
        Log.d(TAG, "Scan iniciado: timestamp=$inicioScan")
    }
    
    /**
     * Finaliza tracking de tempo do scan
     * Chamar após scan bem-sucedido
     */
    fun finalizarScan() {
        fimScan = System.currentTimeMillis()
        Log.d(TAG, "Scan finalizado: timestamp=$fimScan")
    }
    
    /**
     * Inicia tracking de tempo de preenchimento
     * Chamar ao mostrar formulário de coleta
     */
    fun iniciarPreenchimento() {
        inicioPreenchimento = System.currentTimeMillis()
        Log.d(TAG, "Preenchimento iniciado: timestamp=$inicioPreenchimento")
    }
    
    /**
     * Define método de coleta
     * Chamar ao selecionar método (QR, Barcode, Manual, etc)
     */
    fun setMetodoColeta(metodo: String) {
        metodoColeta = metodo
        Log.d(TAG, "Método de coleta definido: $metodo")
    }
    
    /**
     * Define métricas do scan (vindo do ScanMetricsTracker)
     * Chamar após scan bem-sucedido
     */
    fun setScanMetrics(metrics: ScanMetrics) {
        scanMetrics = metrics
        metodoColeta = metrics.tipoScan // Atualizar método baseado no tipo de scan
        Log.d(TAG, "Métricas de scan definidas: $metrics")
    }
    
    /**
     * Calcula métricas de tempo
     */
    private fun calcularMetricasTempo(): Triple<Int, Int?, Int?> {
        val tempoTotal = if (inicioColeta > 0) {
            MetricsHelper.calcularTempoSegundos(inicioColeta)
        } else 0
        
        val tempoScan = if (inicioScan > 0 && fimScan > 0) {
            ((fimScan - inicioScan) / 1000).toInt()
        } else scanMetrics?.tempoSegundos
        
        val tempoPreenchimento = if (inicioPreenchimento > 0) {
            MetricsHelper.calcularTempoSegundos(inicioPreenchimento)
        } else null
        
        return Triple(tempoTotal, tempoScan, tempoPreenchimento)
    }
    
    /**
     * Registra coleta COM métricas
     * Versão atualizada que inclui tracking de performance
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
                
                // Calcular métricas de tempo
                val (tempoTotal, tempoScan, tempoPreenchimento) = calcularMetricasTempo()
                
                // Calcular horário
                val (hora, dia, periodo) = MetricsHelper.calcularHorario()
                
                // Obter métricas de scan
                val tipoScan = scanMetrics?.tipoScan
                val tentativasScan = scanMetrics?.tentativas ?: 1
                val errosScan = scanMetrics?.erros ?: 0
                val qualidadeEtiqueta = scanMetrics?.qualidadeEtiqueta
                
                // Log das métricas
                val resumo = MetricsHelper.criarResumoMetricas(
                    tempoTotal, tempoScan, tempoPreenchimento, metodoColeta, periodo
                )
                Log.d(TAG, resumo)
                
                // TODO: Passar métricas para o Use Case
                // Por enquanto, registrar normalmente
                registrarColetaUseCase(
                    numeroPatrimonio = numeroPatrimonio,
                    localizacaoAtual = localizacaoAtual,
                    observacoes = observacoes,
                    latitude = latitude,
                    longitude = longitude,
                    idUsuario = idUsuario
                    // TODO: Adicionar parâmetros de métricas quando Use Case for atualizado
                ).fold(
                    onSuccess = { coleta ->
                        _state.value = ColetaState.Success(coleta)
                        syncScheduler.incrementCollectionCount()
                        
                        // v2.10: Vibrar ao coletar (se habilitado nas configurações)
                        vibrationHelper.vibrateOnCollection()
                        
                        // Resetar métricas para próxima coleta
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
     * Reseta métricas para próxima coleta
     */
    private fun resetarMetricas() {
        inicioColeta = 0
        inicioScan = 0
        fimScan = 0
        inicioPreenchimento = 0
        metodoColeta = MetricsHelper.METODO_QR_CODE
        scanMetrics = null
        Log.d(TAG, "Métricas resetadas")
    }
    
    /**
     * Retorna resumo das métricas atuais (para debug/testes)
     */
    fun getMetricasResumo(): String {
        val (tempoTotal, tempoScan, tempoPreenchimento) = calcularMetricasTempo()
        val (_, _, periodo) = MetricsHelper.calcularHorario()
        
        return MetricsHelper.criarResumoMetricas(
            tempoTotal, tempoScan, tempoPreenchimento, metodoColeta, periodo
        )
    }
}
