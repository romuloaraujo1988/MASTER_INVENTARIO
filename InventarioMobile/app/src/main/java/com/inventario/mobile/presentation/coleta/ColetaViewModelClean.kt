package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.presentation.state.ColetaState
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
 */
@HiltViewModel
class ColetaViewModelClean @Inject constructor(
    private val buscarPatrimonioUseCase: BuscarPatrimonioUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val syncScheduler: com.inventario.mobile.sync.SyncScheduler
) : ViewModel() {
    
    private val _state = MutableStateFlow<ColetaState>(ColetaState.Idle)
    val state: StateFlow<ColetaState> = _state.asStateFlow()
    
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
}
