package com.inventario.mobile.presentation.scanner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val inventarioRepository: InventarioRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    
    init {
        loadColetasCount()
    }
    
    fun searchPatrimonio(patrimonioId: Long, codigo: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    statusMessage = "Buscando patrimônio...",
                    errorMessage = null
                )
                
                val result = inventarioRepository.findPatrimonioByNumero(codigo) // TODO: Implementar findPatrimonioById
                
                result.fold(
                    onSuccess = { patrimonio ->
                        if (patrimonio != null) {
                            Log.d("ScannerViewModel", "Patrimônio encontrado - ID: ${patrimonio.id}, Número: ${patrimonio.numeroPatrimonio}")
                            
                            // TODO: Implementar verificação de sincronização
                            val jaColetado = false // Stub temporário
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = if (jaColetado) 
                                    "Patrimônio ${patrimonio.numeroPatrimonio} já foi coletado" 
                                else 
                                    "Patrimônio encontrado: ${patrimonio.numeroPatrimonio}",
                                scanResult = ScanResult(
                                    qrContent = "PATRIMONIO:${patrimonio.id}:${patrimonio.numeroPatrimonio}",
                                    patrimonioId = patrimonio.id,
                                    patrimonioCodigo = patrimonio.numeroPatrimonio,
                                    patrimonio = patrimonio,
                                    jaColetado = jaColetado,
                                    coletadoPor = patrimonio.coletadoPor,
                                    dataColetaFormatada = patrimonio.dataColetaFormatada
                                )
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Patrimônio não encontrado",
                                errorMessage = "Patrimônio com ID $patrimonioId ou código $codigo não foi encontrado"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Erro na busca",
                            errorMessage = "Erro ao buscar patrimônio: ${exception.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Erro na busca",
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun searchPatrimonioByCodigo(codigo: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    statusMessage = "Buscando patrimônio por código...",
                    errorMessage = null
                )
                
                if (codigo.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Código inválido",
                        errorMessage = "Código não pode estar vazio"
                    )
                    return@launch
                }
                
                val result = inventarioRepository.findPatrimonioByNumero(codigo)
                
                result.fold(
                    onSuccess = { patrimonio ->
                        if (patrimonio != null) {
                            Log.d("ScannerViewModel", "Patrimônio encontrado por código - ID: ${patrimonio.id}, Número: ${patrimonio.numeroPatrimonio}")
                            
                            // TODO: Implementar verificação de sincronização
                            val jaColetado = false // Stub temporário
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = if (jaColetado) 
                                    "Patrimônio $codigo já foi coletado" 
                                else 
                                    "Patrimônio encontrado: $codigo",
                                scanResult = ScanResult(
                                    qrContent = "PATRIMONIO:${patrimonio.id}:$codigo",
                                    patrimonioId = patrimonio.id,
                                    patrimonioCodigo = codigo,
                                    patrimonio = patrimonio,
                                    jaColetado = jaColetado
                                )
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Patrimônio não encontrado",
                                errorMessage = "Patrimônio com código $codigo não foi encontrado"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Erro na busca",
                            errorMessage = "Erro ao buscar patrimônio: ${exception.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Erro na busca",
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }
    
    private fun loadColetasCount() {
        viewModelScope.launch {
            try {
                val coletas = inventarioRepository.getColetas()
                _uiState.value = _uiState.value.copy(
                    totalColetas = coletas.size
                )
            } catch (e: Exception) {
                // Silently fail for count
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearScanResult() {
        _uiState.value = _uiState.value.copy(
            scanResult = null,
            statusMessage = "Pronto para escanear",
            errorMessage = null
        )
    }
    
    /**
     * Verifica se deve executar sincronização automática baseada no contador de coletas
     */
    private fun checkAutoSyncByCount() {
        if (preferencesManager.shouldSyncByCollectionCount()) {
            viewModelScope.launch {
                try {
                    // Executar sincronização
                    val syncResult = Result.success(0) // TODO: Implementar sincronizarDados
                    
                    syncResult.fold(
                        onSuccess = {
                            // Resetar contador após sincronização bem-sucedida
                            preferencesManager.resetCollectionCount()
                            
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Sincronização automática realizada com sucesso!"
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Erro na sincronização automática: ${exception.message}"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Erro na sincronização automática: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun coletarPatrimonio(patrimonioId: Long, salaId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ScannerViewModel", "Iniciando coleta - Patrimônio ID: $patrimonioId, Sala ID: $salaId")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    statusMessage = "Coletando patrimônio...",
                    errorMessage = null
                )
                
                // TODO: Buscar patrimônio antes de coletar
                val result = Result.failure<com.inventario.mobile.data.model.Coleta>(Exception("Método não implementado"))
                
                result.fold(
                    onSuccess = { coleta ->
                        Log.d("ScannerViewModel", "Coleta realizada com sucesso - Coleta ID: ${coleta.patrimonioId}, Status: ${coleta.status}")
                        
                        // Incrementar contador de coletas
                        preferencesManager.incrementCollectionCount()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Patrimônio coletado com sucesso!",
                            scanResult = _uiState.value.scanResult?.copy(jaColetado = true)
                        )
                        
                        // Atualizar contador de coletas
                        loadColetasCount()
                        
                        // Verificar se deve sincronizar automaticamente
                        checkAutoSyncByCount()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Erro ao coletar",
                            errorMessage = "Erro ao coletar patrimônio: ${exception.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Erro ao coletar",
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun coletarPatrimonioComEstado(patrimonioId: Long, salaNome: String, estadoEncontrado: String) {
        viewModelScope.launch {
            try {
                Log.d("ScannerViewModel", "Iniciando coleta - Patrimônio ID: $patrimonioId, Sala: $salaNome, Estado: $estadoEncontrado")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    statusMessage = "Coletando patrimônio...",
                    errorMessage = null
                )
                
                // Usar o patrimônio que já está no scanResult
                val patrimonio = _uiState.value.scanResult?.patrimonio
                
                if (patrimonio != null) {
                    val result = inventarioRepository.coletarPatrimonioComSala(
                        patrimonio = patrimonio,
                        salaNome = salaNome,
                        estadoEncontrado = estadoEncontrado
                    )
                    
                    result.fold(
                        onSuccess = { coleta ->
                            Log.d("ScannerViewModel", "Coleta realizada com sucesso - Coleta ID: ${coleta.patrimonioId}")
                            
                            // Incrementar contador de coletas
                            preferencesManager.incrementCollectionCount()
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Patrimônio ${patrimonio.numeroPatrimonio} coletado com sucesso!",
                                scanResult = _uiState.value.scanResult?.copy(jaColetado = true)
                            )
                            
                            // Atualizar contador de coletas
                            loadColetasCount()
                            
                            // Verificar se deve sincronizar automaticamente
                            checkAutoSyncByCount()
                        },
                        onFailure = { exception ->
                            Log.e("ScannerViewModel", "Erro ao coletar patrimônio", exception)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Erro ao coletar",
                                errorMessage = "Erro ao coletar patrimônio: ${exception.message}"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Erro ao coletar",
                        errorMessage = "Patrimônio não encontrado. Escaneie novamente."
                    )
                }
                
            } catch (e: Exception) {
                Log.e("ScannerViewModel", "Erro inesperado ao coletar", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Erro ao coletar",
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }
}

data class ScannerUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "Iniciando scanner...",
    val errorMessage: String? = null,
    val scanResult: ScanResult? = null,
    val totalColetas: Int = 0
)

data class ScanResult(
    val qrContent: String,
    val patrimonioId: Long,
    val patrimonioCodigo: String,
    val patrimonio: Patrimonio? = null,
    val jaColetado: Boolean = false,
    val coletadoPor: String? = null,
    val dataColetaFormatada: String? = null
)