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

    private var currentPage = 0
    private val pageSize = 10
    private var isLoadingMore = false
    private var hasMorePages = true

    companion object {
        private const val TAG = "SalaSelectionViewModel"
    }

    fun loadSalas() {
        Log.d(TAG, "loadSalas: Carregando salas (página 0)")
        currentPage = 0
        hasMorePages = true
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                loadSalasPage(0)
            } catch (e: Exception) {
                Log.e(TAG, "loadSalas: Erro ao carregar salas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar salas: ${e.message}"
                )
            }
        }
    }
    
    fun loadNextPage() {
        if (isLoadingMore || !hasMorePages) {
            Log.d(TAG, "loadNextPage: Ignorando (isLoadingMore=$isLoadingMore, hasMorePages=$hasMorePages)")
            return
        }
        
        viewModelScope.launch {
            Log.d(TAG, "loadNextPage: Carregando página ${currentPage + 1}")
            isLoadingMore = true
            
            try {
                loadSalasPage(currentPage + 1)
            } catch (e: Exception) {
                Log.e(TAG, "loadNextPage: Erro ao carregar próxima página", e)
                isLoadingMore = false
            }
        }
    }
    
    private suspend fun loadSalasPage(page: Int) {
        Log.d(TAG, "loadSalasPage: Carregando página $page com tamanho $pageSize")
        
        // Buscar salas da API com paginação
        val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
        val response = withContext(Dispatchers.IO) {
            apiService.getSalasPaginadas(page, pageSize)
        }
        
        Log.d(TAG, "loadSalasPage: Response code: ${response.code()}")
        Log.d(TAG, "loadSalasPage: Response successful: ${response.isSuccessful}")
        
        // Verificar se a resposta foi bem-sucedida
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Erro ao buscar salas: ${response.message()}")
        }
        
        val apiResponse = response.body()!!
        Log.d(TAG, "loadSalasPage: API Response success: ${apiResponse.success}")
        
        if (!apiResponse.success || apiResponse.data == null) {
            throw Exception(apiResponse.message ?: "Erro desconhecido")
        }
        
        val salasDto = apiResponse.data
        Log.d(TAG, "loadSalasPage: Recebidas ${salasDto.size} salas do servidor")
        
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
        
        // Combinar com salas existentes se for página > 0
        val todasSalas = if (page == 0) {
            salas
        } else {
            _uiState.value.salas + salas
        }
        
        currentPage = page
        // Se recebeu menos salas que o tamanho da página, não há mais páginas
        hasMorePages = salas.size >= pageSize
        isLoadingMore = false
        
        Log.d(TAG, "loadSalasPage: ${salas.size} salas carregadas (total acumulado: ${todasSalas.size})")
        Log.d(TAG, "loadSalasPage: HasMorePages: $hasMorePages (recebeu ${salas.size} de $pageSize)")
        
        _uiState.value = _uiState.value.copy(
            salas = todasSalas,
            isLoading = false,
            errorMessage = null
        )
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