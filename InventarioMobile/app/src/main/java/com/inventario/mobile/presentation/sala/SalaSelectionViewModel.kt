package com.inventario.mobile.presentation.sala

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.Sala
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SalaSelectionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SalaSelectionUiState())
    val uiState: StateFlow<SalaSelectionUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SalaSelectionViewModel"
    }

    fun loadSalas() {
        Log.d(TAG, "loadSalas: Carregando salas")
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // Buscar salas da API
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
                val response = withContext(Dispatchers.IO) {
                    apiService.getSalasWithResponse()
                }
                
                // Verificar se a resposta foi bem-sucedida
                if (!response.isSuccessful || response.body() == null) {
                    throw Exception("Erro ao buscar salas: ${response.message()}")
                }
                
                val apiResponse = response.body()!!
                if (!apiResponse.success || apiResponse.data == null) {
                    throw Exception(apiResponse.message ?: "Erro desconhecido")
                }
                
                val salasDto = apiResponse.data
                
                // Converter DTO para modelo de domínio
                val salas = salasDto.map { dto ->
                    Sala(
                        id = dto.id.toLong(),
                        nome = dto.nome,
                        codigo = dto.codigo,
                        descricao = dto.descricao ?: "",
                        setorId = dto.setorIdFinal.toLong(),
                        ativo = dto.ativa ?: dto.ativo,
                        sincronizado = true,
                        dataCriacao = System.currentTimeMillis(),
                        dataAtualizacao = System.currentTimeMillis(),
                        servidorId = dto.id.toLong()
                    )
                }
                
                Log.d(TAG, "loadSalas: ${salas.size} salas carregadas")
                _uiState.value = _uiState.value.copy(
                    salas = salas,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadSalas: Erro ao carregar salas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar salas: ${e.message}"
                )
            }
        }
    }

    fun refreshSalas() {
        Log.d(TAG, "refreshSalas: Atualizando lista de salas")
        loadSalas()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class SalaSelectionUiState(
    val salas: List<Sala> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)