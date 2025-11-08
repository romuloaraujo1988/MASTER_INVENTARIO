package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.model.Coleta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class ManualCollectionViewModel(
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualCollectionUiState())
    val uiState: StateFlow<ManualCollectionUiState> = _uiState.asStateFlow()

    private var salaId: Long = -1L
    private var salaNome: String = ""

    fun setSalaInfo(salaId: Long, salaNome: String) {
        this.salaId = salaId
        this.salaNome = salaNome
        _uiState.value = _uiState.value.copy(
            salaNome = salaNome,
            salaId = salaId
        )
        loadColetasCount()
    }

    fun searchPatrimonio(numeroPatrimonio: String) {
        Log.d("ManualCollectionVM", "searchPatrimonio iniciado com número: '$numeroPatrimonio'")
        
        if (numeroPatrimonio.isBlank()) {
            Log.w("ManualCollectionVM", "Número do patrimônio está em branco")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Digite o número do patrimônio"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ManualCollectionVM", "Iniciando busca no repositório para número: $numeroPatrimonio")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    patrimonio = null
                )

                val result = inventarioRepository.findPatrimonioByNumero(numeroPatrimonio)
                
                Log.d("ManualCollectionVM", "Resultado da busca recebido: ${if (result.isSuccess) "sucesso" else "falha"}")
                
                result.fold(
                    onSuccess = { patrimonio ->
                        Log.d("ManualCollectionVM", "Busca bem-sucedida. Patrimônio encontrado: ${patrimonio != null}")
                        
                        if (patrimonio != null) {
                            Log.d("ManualCollectionVM", "Patrimônio encontrado - ID: ${patrimonio.id}, Número: ${patrimonio.numeroPatrimonio}, Descrição: ${patrimonio.descricao}")
                            
                            // Verificar se já foi coletado
                            val jaColetado = inventarioRepository.isPatrimonioColetado(patrimonio.id)
                            Log.d("ManualCollectionVM", "Patrimônio já coletado: $jaColetado")
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                patrimonio = patrimonio,
                                jaColetado = jaColetado,
                                errorMessage = if (jaColetado) "Patrimônio já foi coletado" else null
                            )
                        } else {
                            Log.w("ManualCollectionVM", "Patrimônio não encontrado para número: $numeroPatrimonio")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Patrimônio não encontrado"
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e("ManualCollectionVM", "Erro na busca do patrimônio", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Erro ao buscar patrimônio: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("ManualCollectionVM", "Erro inesperado na busca", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }

    fun coletarPatrimonio(estadoEncontrado: String, observacoes: String? = null) {
        val patrimonio = _uiState.value.patrimonio
        
        // Validação 1: Verificar se há patrimônio selecionado
        if (patrimonio == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "É necessário pesquisar e selecionar um patrimônio válido antes de realizar a coleta"
            )
            return
        }

        // Validação 2: Verificar se o patrimônio já foi coletado
        if (_uiState.value.jaColetado) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Este patrimônio já foi coletado anteriormente"
            )
            return
        }

        // Validação 3: Verificar se o patrimônio é válido (tem ID e número)
        if (patrimonio.id <= 0 || patrimonio.numeroPatrimonio.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Patrimônio inválido. Realize uma nova pesquisa"
            )
            return
        }

        // Validação 4: Verificar se o estado foi fornecido
        if (estadoEncontrado.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "É necessário selecionar o estado do patrimônio"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val result = inventarioRepository.coletarPatrimonioComSala(
                    patrimonio = patrimonio,
                    salaNome = salaNome,
                    estadoEncontrado = estadoEncontrado,
                    observacoes = observacoes
                )

                result.fold(
                    onSuccess = { coleta ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            coletaRealizada = true,
                            patrimonio = null,
                            jaColetado = false,
                            successMessage = "Patrimônio ${patrimonio.numeroPatrimonio} coletado com sucesso! Estado: $estadoEncontrado"
                        )
                        loadColetasCount()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Erro ao coletar patrimônio: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }

    private fun loadColetasCount() {
        viewModelScope.launch {
            try {
                val coletas = inventarioRepository.getColetasLocal()
                _uiState.value = _uiState.value.copy(
                    totalColetas = coletas.size
                )
            } catch (e: Exception) {
                // Silently fail for count
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            coletaRealizada = false
        )
    }

    fun clearPatrimonio() {
        _uiState.value = _uiState.value.copy(
            patrimonio = null,
            jaColetado = false,
            errorMessage = null,
            successMessage = null
        )
    }
}

data class ManualCollectionUiState(
    val isLoading: Boolean = false,
    val salaId: Long = -1L,
    val salaNome: String = "",
    val patrimonio: Patrimonio? = null,
    val jaColetado: Boolean = false,
    val coletaRealizada: Boolean = false,
    val totalColetas: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)