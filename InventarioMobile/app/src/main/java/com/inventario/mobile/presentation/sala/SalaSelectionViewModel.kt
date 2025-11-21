package com.inventario.mobile.presentation.sala

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.cache.SalaCache
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

    fun loadSalas(forceRefresh: Boolean = false) {
        Log.d(TAG, "loadSalas: Carregando TODAS as salas (forceRefresh=$forceRefresh)")
        
        // Verificar se há cache válido e não é refresh forçado
        if (!forceRefresh && SalaCache.isValid()) {
            val cachedSalas = SalaCache.getSalas()
            if (cachedSalas != null) {
                Log.d(TAG, "loadSalas: Usando ${cachedSalas.size} salas do cache")
                _uiState.value = _uiState.value.copy(
                    salas = cachedSalas,
                    isLoading = false,
                    errorMessage = null
                )
                return
            }
        }
        
        // Carregar TODAS as salas de uma vez
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    errorMessage = null,
                    salas = emptyList()
                )
                
                // Carregar TODAS as salas
                loadAllSalas()
                
            } catch (e: Exception) {
                Log.e(TAG, "loadSalas: Erro ao carregar salas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar salas: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Carrega TODAS as salas de uma vez (sem paginação)
     */
    private suspend fun loadAllSalas() {
        Log.d(TAG, "loadAllSalas: Carregando TODAS as salas de uma vez")
        
        try {
            val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
            val response = withContext(Dispatchers.IO) {
                // Usar endpoint sem paginação
                apiService.getSalasWithResponse()
            }
            
            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Erro ao buscar salas: ${response.message()}")
            }
            
            val apiResponse = response.body()!!
            if (!apiResponse.success || apiResponse.data == null) {
                throw Exception(apiResponse.message ?: "Erro desconhecido")
            }
            
            val salasDto = apiResponse.data
            Log.d(TAG, "loadAllSalas: Recebidas ${salasDto.size} salas do servidor")
            
            // Converter DTO para modelo de domínio
            val salas = salasDto.map { dto ->
                Sala(
                    id = dto.id.toLong(),
                    nome = dto.nome,
                    codigo = dto.codigo,
                    descricao = dto.descricao?.takeIf { it != dto.nome } ?: "",
                    setorId = dto.setorIdFinal.toLong(),
                    ativo = dto.ativa ?: dto.ativo,
                    sincronizado = true,
                    dataCriacao = System.currentTimeMillis(),
                    dataAtualizacao = System.currentTimeMillis(),
                    servidorId = dto.id.toLong()
                )
            }
            
            // Salvar no cache
            SalaCache.setSalas(salas)
            
            _uiState.value = _uiState.value.copy(
                salas = salas,
                isLoading = false,
                errorMessage = null
            )
            
            Log.d(TAG, "loadAllSalas: ✅ ${salas.size} salas carregadas com sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "loadAllSalas: Erro ao carregar salas", e)
            throw e
        }
    }

    fun refreshSalas() {
        Log.d(TAG, "refreshSalas: Forçando atualização da lista de salas")
        SalaCache.clear() // Limpar cache ao fazer refresh manual
        loadSalas(forceRefresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Não limpar o cache aqui - queremos que persista durante a sessão do app
        Log.d(TAG, "onCleared: ViewModel destruído (cache mantido)")
    }
}

data class SalaSelectionUiState(
    val salas: List<Sala> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
)
