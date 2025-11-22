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
     * ✅ CORREÇÃO OFFLINE-FIRST: Busca do SQLite PRIMEIRO
     * Carrega TODAS as salas de uma vez (sem paginação)
     */
    private suspend fun loadAllSalas() {
        Log.d(TAG, "loadAllSalas: Iniciando carregamento OFFLINE-FIRST")
        
        try {
            // ========================================
            // PASSO 1: Buscar do banco local (Room) PRIMEIRO
            // ========================================
            Log.d(TAG, "📱 PASSO 1: Buscando salas do SQLite local...")
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(getApplication())
            val salaDao = database.salaDao()
            val salasEntity = salaDao.buscarTodas()
            
            if (salasEntity.isNotEmpty()) {
                Log.d(TAG, "✅ ${salasEntity.size} salas encontradas no banco local")
                
                // Converter Entity para Domain Model
                val salas = salasEntity.map { entity ->
                    Sala(
                        id = entity.id.toLong(),
                        nome = entity.nome,
                        codigo = entity.id.toString(), // Usar ID como código
                        descricao = entity.nomeSetor, // Usar nome do setor como descrição
                        ativo = entity.ativa,
                        setorId = entity.idSetor?.toLong() ?: 0L,
                        sincronizado = true,
                        dataCriacao = entity.dataUltimaAtualizacao,
                        dataAtualizacao = entity.dataUltimaAtualizacao,
                        servidorId = entity.id.toLong()
                    )
                }
                
                // Salvar no cache
                SalaCache.setSalas(salas)
                
                _uiState.value = _uiState.value.copy(
                    salas = salas,
                    isLoading = false,
                    errorMessage = null
                )
                
                Log.d(TAG, "✅ Salas carregadas do SQLite e exibidas")
                
                // ✅ Tentar atualizar do servidor em background (não bloqueia UI)
                tryUpdateFromServerInBackground()
                return
            }
            
            // ========================================
            // PASSO 2: Se SQLite vazio, buscar da API
            // ========================================
            Log.d(TAG, "⚠️ Nenhuma sala no banco local, buscando da API...")
            loadFromApi()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar do SQLite: ${e.message}", e)
            
            // ✅ Tentar API como fallback
            loadFromApi()
        }
    }
    
    /**
     * Busca salas da API e salva no SQLite
     */
    private suspend fun loadFromApi() {
        try {
            Log.d(TAG, "🌐 Buscando salas da API...")
            
            val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
            val response = withContext(Dispatchers.IO) {
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
            Log.d(TAG, "✅ ${salasDto.size} salas recebidas do servidor")
            
            // ========================================
            // Salvar no SQLite para próxima vez
            // ========================================
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(getApplication())
            val salaDao = database.salaDao()
            
            val entities = salasDto.map { dto ->
                com.inventario.mobile.data.local.entity.SalaEntity(
                    id = dto.id,
                    nome = dto.nome,
                    idSetor = dto.setorIdFinal,
                    nomeSetor = null, // Campo não disponível no DTO
                    ativa = dto.ativa ?: dto.ativo,
                    dataUltimaAtualizacao = System.currentTimeMillis()
                )
            }
            
            salaDao.inserirTodas(entities)
            Log.d(TAG, "💾 ${entities.size} salas salvas no SQLite")
            
            // Converter DTO para modelo de domínio
            val salas = salasDto.map { dto ->
                Sala(
                    id = dto.id.toLong(),
                    nome = dto.nome,
                    codigo = dto.codigo ?: "",
                    descricao = dto.descricao?.takeIf { it != dto.nome },
                    ativo = dto.ativa ?: dto.ativo,
                    setorId = dto.setorIdFinal.toLong(),
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
            
            Log.d(TAG, "✅ ${salas.size} salas carregadas da API e salvas")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar da API: ${e.message}", e)
            
            // ========================================
            // FALLBACK FINAL: Buscar do SQLite mesmo com erro
            // ========================================
            tryLoadFromLocalDatabaseAsFallback()
        }
    }
    
    /**
     * Fallback final: buscar do SQLite mesmo com erro de rede
     */
    private suspend fun tryLoadFromLocalDatabaseAsFallback() {
        try {
            Log.d(TAG, "🔄 Tentando fallback para SQLite...")
            
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(getApplication())
            val salaDao = database.salaDao()
            val salasEntity = salaDao.buscarTodas()
            
            if (salasEntity.isNotEmpty()) {
                Log.d(TAG, "✅ Usando ${salasEntity.size} salas do cache local (modo offline)")
                
                val salas = salasEntity.map { entity ->
                    Sala(
                        id = entity.id.toLong(),
                        nome = entity.nome,
                        codigo = entity.id.toString(),
                        descricao = entity.nomeSetor,
                        ativo = entity.ativa,
                        setorId = entity.idSetor?.toLong() ?: 0L,
                        sincronizado = true,
                        dataCriacao = entity.dataUltimaAtualizacao,
                        dataAtualizacao = entity.dataUltimaAtualizacao,
                        servidorId = entity.id.toLong()
                    )
                }
                
                // Salvar no cache
                SalaCache.setSalas(salas)
                
                _uiState.value = _uiState.value.copy(
                    salas = salas,
                    isLoading = false,
                    errorMessage = "📡 Modo offline - Mostrando dados locais"
                )
            } else {
                Log.e(TAG, "❌ Sem dados locais disponíveis")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sem conexão e sem dados locais. Conecte-se à internet para sincronizar."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar do SQLite como fallback: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro ao carregar dados: ${e.message}"
            )
        }
    }
    
    /**
     * Atualiza do servidor em background (não bloqueia UI)
     */
    private fun tryUpdateFromServerInBackground() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Atualizando salas do servidor em background...")
                loadFromApi()
            } catch (e: Exception) {
                Log.d(TAG, "ℹ️ Não foi possível atualizar do servidor (modo offline)")
            }
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
