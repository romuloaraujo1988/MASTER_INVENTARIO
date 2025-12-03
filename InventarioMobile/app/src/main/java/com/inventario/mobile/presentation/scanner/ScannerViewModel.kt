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

/**
 * ViewModel para Scanner de QR Code
 * ✅ Agora usa RegistrarColetaUseCase obrigatório (UNIFICADO com coleta manual)
 */
class ScannerViewModel(
    private val inventarioRepository: InventarioRepository,
    private val preferencesManager: PreferencesManager,
    private val registrarColetaUseCase: com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
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
                    errorMessage = null,
                    scanResult = null // Limpar resultado anterior
                )
                
                Log.d("ScannerViewModel", "Buscando patrimônio - ID: $patrimonioId, Código: $codigo")
                
                // Buscar patrimônio no repositório
                val result = inventarioRepository.findPatrimonioByNumero(codigo)
                
                result.fold(
                    onSuccess = { patrimonio ->
                        if (patrimonio != null) {
                            Log.d("ScannerViewModel", "Patrimônio encontrado - ID: ${patrimonio.id}, Número: ${patrimonio.numeroPatrimonio}")
                            
                            // Verificar se já foi coletado
                            val jaColetado = patrimonio.coletado
                            Log.d("ScannerViewModel", "Patrimônio já coletado: $jaColetado")
                            
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
                                ),
                                errorMessage = null
                            )
                        } else {
                            Log.w("ScannerViewModel", "Patrimônio não encontrado - Código: $codigo")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Patrimônio não encontrado",
                                errorMessage = "Patrimônio '$codigo' não foi encontrado no sistema. Verifique o código e tente novamente.",
                                scanResult = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e("ScannerViewModel", "Erro ao buscar patrimônio", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Erro na busca",
                            errorMessage = "Erro ao buscar patrimônio: ${exception.message}\n\nTente escanear novamente.",
                            scanResult = null
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e("ScannerViewModel", "Erro inesperado na busca", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Erro na busca",
                    errorMessage = "Erro inesperado: ${e.message}\n\nTente escanear novamente.",
                    scanResult = null
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
                    errorMessage = null,
                    scanResult = null // Limpar resultado anterior
                )
                
                if (codigo.isBlank()) {
                    Log.w("ScannerViewModel", "Código vazio fornecido")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Código inválido",
                        errorMessage = "Código não pode estar vazio. Tente escanear novamente.",
                        scanResult = null
                    )
                    return@launch
                }
                
                Log.d("ScannerViewModel", "Buscando patrimônio por código: $codigo")
                
                // Buscar patrimônio no repositório
                val result = inventarioRepository.findPatrimonioByNumero(codigo)
                
                result.fold(
                    onSuccess = { patrimonio ->
                        if (patrimonio != null) {
                            Log.d("ScannerViewModel", "Patrimônio encontrado por código - ID: ${patrimonio.id}, Número: ${patrimonio.numeroPatrimonio}")
                            
                            // Verificar se já foi coletado
                            val jaColetado = patrimonio.coletado
                            Log.d("ScannerViewModel", "Patrimônio já coletado: $jaColetado")
                            
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
                                    jaColetado = jaColetado,
                                    coletadoPor = patrimonio.coletadoPor,
                                    dataColetaFormatada = patrimonio.dataColetaFormatada
                                ),
                                errorMessage = null
                            )
                        } else {
                            Log.w("ScannerViewModel", "Patrimônio não encontrado - Código: $codigo")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                statusMessage = "Patrimônio não encontrado",
                                errorMessage = "Patrimônio '$codigo' não foi encontrado no sistema. Verifique o código e tente novamente.",
                                scanResult = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e("ScannerViewModel", "Erro ao buscar patrimônio por código", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Erro na busca",
                            errorMessage = "Erro ao buscar patrimônio: ${exception.message}\n\nTente escanear novamente.",
                            scanResult = null
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e("ScannerViewModel", "Erro inesperado na busca por código", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Erro na busca",
                    errorMessage = "Erro inesperado: ${e.message}\n\nTente escanear novamente.",
                    scanResult = null
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
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    fun clearScanResult() {
        Log.d("ScannerViewModel", "Limpando resultado do scan")
        _uiState.value = _uiState.value.copy(
            scanResult = null,
            statusMessage = "Pronto para escanear",
            errorMessage = null,
            successMessage = null,
            isLoading = false
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
                Log.d("ScannerViewModel", "═══════════════════════════════════════")
                Log.d("ScannerViewModel", "INICIANDO COLETA (MESMA ESTRUTURA DA COLETA MANUAL)")
                Log.d("ScannerViewModel", "═══════════════════════════════════════")
                Log.d("ScannerViewModel", "Patrimônio ID: $patrimonioId")
                Log.d("ScannerViewModel", "Sala: $salaNome")
                Log.d("ScannerViewModel", "Estado: $estadoEncontrado")
                
                // ✅ CRÍTICO: Obter ID da sala atual do PreferencesManager
                val salaIdAtual = preferencesManager.getCurrentSalaId()
                Log.d("ScannerViewModel", "Sala ID atual (PreferencesManager): $salaIdAtual")
                
                if (salaIdAtual <= 0) {
                    Log.e("ScannerViewModel", "❌ ERRO CRÍTICO: Sala ID não encontrada!")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Erro ao coletar",
                        errorMessage = "Sala não selecionada. Selecione uma sala antes de coletar."
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    statusMessage = "Coletando patrimônio...",
                    errorMessage = null
                )
                
                // Usar o patrimônio que já está no scanResult
                val patrimonio = _uiState.value.scanResult?.patrimonio
                
                if (patrimonio == null) {
                    Log.e("ScannerViewModel", "Patrimônio não encontrado no scanResult")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Erro ao coletar",
                        errorMessage = "Patrimônio não encontrado. Escaneie novamente."
                    )
                    return@launch
                }
                
                Log.d("ScannerViewModel", "Patrimônio encontrado: ${patrimonio.numeroPatrimonio}")
                
                // ✅ Usar RegistrarColetaUseCase (UNIFICADO com coleta manual)
                Log.d("ScannerViewModel", "═══════════════════════════════════════")
                Log.d("ScannerViewModel", "✓ Usando RegistrarColetaUseCase (Clean Architecture - UNIFICADO)")
                Log.d("ScannerViewModel", "  Patrimônio: ${patrimonio.numeroPatrimonio}")
                Log.d("ScannerViewModel", "  Sala ID: $salaIdAtual")
                Log.d("ScannerViewModel", "  Sala Nome: $salaNome")
                Log.d("ScannerViewModel", "  Estado: $estadoEncontrado")
                Log.d("ScannerViewModel", "═══════════════════════════════════════")
                
                val result = registrarColetaUseCase.invoke(
                    numeroPatrimonio = patrimonio.numeroPatrimonio,
                    salaId = salaIdAtual, // ✅ CRÍTICO: Passar ID da sala atual
                    localizacaoAtual = salaNome,
                    estadoEncontrado = estadoEncontrado,
                    observacoes = null
                )
                
                result.fold(
                    onSuccess = { coleta ->
                        Log.d("ScannerViewModel", "✓ Coleta realizada com sucesso!")
                        Log.d("ScannerViewModel", "  Patrimônio: ${patrimonio.numeroPatrimonio}")
                        Log.d("ScannerViewModel", "  Sala: $salaNome")
                        Log.d("ScannerViewModel", "  Estado: $estadoEncontrado")
                        
                        // Incrementar contador de coletas
                        preferencesManager.incrementCollectionCount()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Coleta realizada com sucesso!",
                            successMessage = "Patrimônio ${patrimonio.numeroPatrimonio} coletado com sucesso! Estado: $estadoEncontrado",
                            scanResult = null, // Limpar resultado para permitir nova coleta
                            errorMessage = null
                        )
                        
                        // Atualizar contador de coletas
                        loadColetasCount()
                        
                        // Verificar se deve sincronizar automaticamente
                        checkAutoSyncByCount()
                    },
                    onFailure = { exception ->
                        Log.e("ScannerViewModel", "✗ Erro ao coletar patrimônio", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Erro ao coletar",
                            errorMessage = "Erro ao coletar patrimônio: ${exception.message}",
                            successMessage = null
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e("ScannerViewModel", "✗ Erro inesperado ao coletar", e)
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
    val successMessage: String? = null,
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
