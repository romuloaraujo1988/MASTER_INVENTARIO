package com.inventario.mobile.presentation.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.repository.EstatisticasHistorico
import com.inventario.mobile.domain.usecase.BuscarHistoricoScansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para tela de histórico de scans
 * 
 * @since v2.11.0
 */
@HiltViewModel
class HistoricoScansViewModel @Inject constructor(
    private val buscarHistoricoUseCase: BuscarHistoricoScansUseCase,
    private val preferencesManager: com.inventario.mobile.utils.PreferencesManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "HistoricoScansVM"
        private const val LIMITE_PADRAO = 50
    }
    
    private val _state = MutableStateFlow<HistoricoScansState>(HistoricoScansState.Idle)
    val state: StateFlow<HistoricoScansState> = _state.asStateFlow()
    
    private val _filtroAtual = MutableStateFlow(FiltroHistorico.TODOS)
    val filtroAtual: StateFlow<FiltroHistorico> = _filtroAtual.asStateFlow()
    
    private val _estatisticas = MutableStateFlow<EstatisticasHistorico?>(null)
    val estatisticas: StateFlow<EstatisticasHistorico?> = _estatisticas.asStateFlow()
    
    init {
        carregarHistorico()
        observarHistorico()
    }
    
    /**
     * Carrega histórico inicial
     */
    fun carregarHistorico() {
        viewModelScope.launch {
            _state.value = HistoricoScansState.Loading
            
            try {
                // Carregar estatísticas
                buscarHistoricoUseCase.getEstatisticasHoje().onSuccess { stats ->
                    _estatisticas.value = stats
                }
                
                // Carregar histórico baseado no filtro
                val resultado = when (_filtroAtual.value) {
                    FiltroHistorico.TODOS -> buscarHistoricoUseCase(LIMITE_PADRAO)
                    FiltroHistorico.COLETADOS -> buscarHistoricoUseCase.buscarColetados(LIMITE_PADRAO)
                    FiltroHistorico.CONSULTAS -> buscarHistoricoUseCase.buscarConsultas(LIMITE_PADRAO)
                }
                
                resultado.fold(
                    onSuccess = { historico ->
                        if (historico.isEmpty()) {
                            _state.value = HistoricoScansState.Empty
                        } else {
                            _state.value = HistoricoScansState.Success(
                                historico = historico,
                                estatisticas = _estatisticas.value
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.value = HistoricoScansState.Error(
                            error.message ?: "Erro ao carregar histórico"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Erro ao carregar histórico", e)
                _state.value = HistoricoScansState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Observa histórico em tempo real
     */
    private fun observarHistorico() {
        viewModelScope.launch {
            buscarHistoricoUseCase.observar(LIMITE_PADRAO)
                .catch { e ->
                    android.util.Log.e(TAG, "Erro ao observar histórico", e)
                }
                .collect { historico ->
                    // Atualizar apenas se não estiver em loading
                    if (_state.value !is HistoricoScansState.Loading) {
                        if (historico.isEmpty()) {
                            _state.value = HistoricoScansState.Empty
                        } else {
                            _state.value = HistoricoScansState.Success(
                                historico = historico,
                                estatisticas = _estatisticas.value
                            )
                        }
                    }
                }
        }
    }
    
    /**
     * Altera o filtro e recarrega
     */
    fun setFiltro(filtro: FiltroHistorico) {
        if (_filtroAtual.value != filtro) {
            _filtroAtual.value = filtro
            carregarHistorico()
        }
    }
    
    /**
     * Limpa todo o histórico (tabela `historico_scan`).
     *
     * Não afeta o contador de coletas do scanner nem o último patrimônio
     * coletado — esses são controlados pelo PreferencesManager e podem ser
     * resetados separadamente via [resetarContadorSessao] para quem quiser
     * começar uma nova sessão de coleta limpa.
     */
    fun limparHistorico() {
        viewModelScope.launch {
            _state.value = HistoricoScansState.Loading
            
            buscarHistoricoUseCase.limparHistorico().fold(
                onSuccess = {
                    _state.value = HistoricoScansState.Empty
                    _estatisticas.value = EstatisticasHistorico(0, 0, 0)
                },
                onFailure = { error ->
                    _state.value = HistoricoScansState.Error(
                        error.message ?: "Erro ao limpar histórico"
                    )
                }
            )
        }
    }

    /**
     * Reseta o contador de coletas da sessão e o último patrimônio coletado.
     * Útil para começar uma nova sessão de coleta sem precisar sair do app.
     * Isso NÃO apaga coletas salvas no banco — só os indicadores de sessão
     * exibidos na tela do scanner.
     */
    fun resetarContadorSessao() {
        preferencesManager.resetCollectionCount()
        // `resetCollectionCount()` já limpa o último patrimônio internamente.
        android.util.Log.d(TAG, "Contador de sessão resetado")
    }

    /**
     * Limpeza completa: histórico + contador + último coletado.
     * Oferecido como opção "Tudo" no dialog de limpeza.
     */
    fun limparTudo() {
        resetarContadorSessao()
        limparHistorico()
    }
    
    /**
     * Atualiza estatísticas
     */
    fun atualizarEstatisticas() {
        viewModelScope.launch {
            buscarHistoricoUseCase.getEstatisticasHoje().onSuccess { stats ->
                _estatisticas.value = stats
            }
        }
    }
}
