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

    private var currentPage = 0
    private val pageSize = 20 // 20 itens por página
    private var isLoadingMore = false
    private var hasMorePages = true
    private var allSalasLoaded = false

    companion object {
        private const val TAG = "SalaSelectionViewModel"
    }

    fun loadSalas(forceRefresh: Boolean = false) {
        Log.d(TAG, "loadSalas: Carregando salas (forceRefresh=$forceRefresh)")
        
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
                
                // Atualizar estado de paginação baseado no cache
                currentPage = (cachedSalas.size / pageSize)
                hasMorePages = SalaCache.hasMorePages()
                allSalasLoaded = !hasMorePages
                
                return
            }
        }
        
        // Se não há cache válido ou é refresh forçado, carregar do servidor
        currentPage = 0
        hasMorePages = true
        allSalasLoaded = false
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    errorMessage = null,
                    salas = emptyList()
                )
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
        // Verificar se já está carregando ou se não há mais páginas
        if (isLoadingMore || !hasMorePages || allSalasLoaded) {
            Log.d(TAG, "loadNextPage: Ignorando (isLoadingMore=$isLoadingMore, hasMorePages=$hasMorePages, allLoaded=$allSalasLoaded)")
            return
        }
        
        Log.d(TAG, "loadNextPage: Iniciando carregamento da página ${currentPage + 1}")
        isLoadingMore = true
        
        // Atualizar UI para mostrar loading
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        
        viewModelScope.launch {
            try {
                loadSalasPage(currentPage + 1)
            } catch (e: Exception) {
                Log.e(TAG, "loadNextPage: Erro ao carregar próxima página", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = "Erro ao carregar mais salas: ${e.message}"
                )
            } finally {
                isLoadingMore = false
            }
        }
    }
    
    private suspend fun loadSalasPage(page: Int) {
        Log.d(TAG, "loadSalasPage: Carregando página $page com tamanho $pageSize")
        
        try {
            // Buscar salas da API com paginação
            val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
            val response = withContext(Dispatchers.IO) {
                apiService.getSalasPaginadas(page, pageSize)
            }
            
            Log.d(TAG, "loadSalasPage: Response code: ${response.code()}")
            
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
            
            // Se não recebeu nenhuma sala, marcar como todas carregadas
            if (salasDto.isEmpty()) {
                Log.d(TAG, "loadSalasPage: Nenhuma sala recebida, todas já foram carregadas")
                allSalasLoaded = true
                hasMorePages = false
                isLoadingMore = false
                
                // Atualizar apenas o estado de loading
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false
                )
                return
            }
            
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
                // Primeira página: substituir cache
                SalaCache.setSalas(salas)
                salas
            } else {
                // Páginas seguintes: adicionar ao cache
                SalaCache.addSalas(salas)
                _uiState.value.salas + salas
            }
            
            // Atualizar página atual
            currentPage = page
            
            // Se recebeu menos salas que o tamanho da página, não há mais páginas
            if (salas.size < pageSize) {
                Log.d(TAG, "loadSalasPage: Última página alcançada (recebeu ${salas.size} de $pageSize)")
                hasMorePages = false
                allSalasLoaded = true
            } else {
                hasMorePages = true
            }
            
            // Atualizar informações de paginação no cache
            SalaCache.setLastPageSize(salas.size, pageSize)
            
            isLoadingMore = false
            
            Log.d(TAG, "loadSalasPage: ${salas.size} salas carregadas (total: ${todasSalas.size})")
            Log.d(TAG, "loadSalasPage: HasMorePages: $hasMorePages, AllLoaded: $allSalasLoaded")
            Log.d(TAG, "loadSalasPage: Cache atualizado com ${SalaCache.size()} salas")
            
            _uiState.value = _uiState.value.copy(
                salas = todasSalas,
                isLoading = false,
                isLoadingMore = false,
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "loadSalasPage: Erro ao carregar página $page", e)
            isLoadingMore = false
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