package com.inventario.mobile.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalPatrimonios: Int = 0,
    val pendingCollections: Int = 0,
    val pendingSync: Int = 0,
    val userName: String = "",
    val userProfile: String = "",
    val lastSyncTime: String? = null,
    val errorMessage: String? = null,
    val dashboardStats: com.inventario.mobile.data.remote.dto.DashboardStatsDto? = null,
    val coletasEvolucao: List<com.inventario.mobile.data.remote.dto.ColetasPorDiaDto> = emptyList(),
    val isLoadingGrafico: Boolean = false,
    val graficoError: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    // private val database: InventarioDatabase
    private val preferencesManager: PreferencesManager

    init {
        try {
            android.util.Log.d("DashboardViewModel", "Inicializando DashboardViewModel")
            // database = InventarioDatabase.getDatabase(application)
            android.util.Log.d("DashboardViewModel", "Database inicializado com sucesso")
            preferencesManager = PreferencesManager(application)
            android.util.Log.d("DashboardViewModel", "PreferencesManager inicializado com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("DashboardViewModel", "Erro ao inicializar DashboardViewModel", e)
            throw e
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                android.util.Log.d("DashboardViewModel", "Iniciando carregamento dos dados do dashboard")
                
                // Buscar dados da API
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
                android.util.Log.d("DashboardViewModel", "ApiService obtido, fazendo chamada para getDashboardStats()")
                
                val response = withContext(Dispatchers.IO) {
                    try {
                        apiService.getDashboardStats()
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Exceção ao chamar getDashboardStats", e)
                        throw e
                    }
                }
                
                android.util.Log.d("DashboardViewModel", "Resposta recebida: isSuccessful=${response.isSuccessful}, code=${response.code()}")
                
                var totalPatrimonios = 0
                var pendingCollections = 0
                var pendingSync = 0
                var dashboardStats: com.inventario.mobile.data.remote.dto.DashboardStatsDto? = null
                
                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("DashboardViewModel", "Body: success=${body?.success}, message=${body?.message}, data=${body?.data}")
                    
                    if (body?.success == true && body.data != null) {
                        dashboardStats = body.data
                        totalPatrimonios = dashboardStats.totalPatrimonios
                        pendingCollections = dashboardStats.patrimoniosPendentes
                        pendingSync = 0 // TODO: Implementar contagem de sincronização pendente
                        
                        android.util.Log.d("DashboardViewModel", "✓ Estatísticas carregadas com sucesso:")
                        android.util.Log.d("DashboardViewModel", "  - Total: $totalPatrimonios")
                        android.util.Log.d("DashboardViewModel", "  - Coletados: ${dashboardStats.patrimoniosColetados}")
                        android.util.Log.d("DashboardViewModel", "  - Pendentes: $pendingCollections")
                        android.util.Log.d("DashboardViewModel", "  - Divergências: ${dashboardStats.divergencias}")
                        android.util.Log.d("DashboardViewModel", "  - Coletores: ${dashboardStats.coletoresAtivos}")
                        android.util.Log.d("DashboardViewModel", "  - Percentual: ${dashboardStats.percentualConclusao}%")
                    } else {
                        android.util.Log.w("DashboardViewModel", "✗ Resposta com success=false ou data=null: ${body?.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("DashboardViewModel", "✗ Erro HTTP ao buscar estatísticas:")
                    android.util.Log.e("DashboardViewModel", "  - Code: ${response.code()}")
                    android.util.Log.e("DashboardViewModel", "  - Message: ${response.message()}")
                    android.util.Log.e("DashboardViewModel", "  - ErrorBody: $errorBody")
                }
                
                android.util.Log.d("DashboardViewModel", "Buscando informações do usuário")
                // Buscar informações do usuário
                val userName = try {
                    preferencesManager.getUserName() ?: "Usuário"
                } catch (e: Exception) {
                    android.util.Log.w("DashboardViewModel", "Erro ao obter nome do usuário", e)
                    "Usuário"
                }
                
                val userProfile = try {
                    preferencesManager.getUserProfile() ?: "Coletor"
                } catch (e: Exception) {
                    android.util.Log.w("DashboardViewModel", "Erro ao obter perfil do usuário", e)
                    "Coletor"
                }
                
                android.util.Log.d("DashboardViewModel", "Buscando última sincronização")
                // Buscar última sincronização
                val lastSyncTimestamp = try {
                    preferencesManager.getLastSyncTime()
                } catch (e: Exception) {
                    android.util.Log.w("DashboardViewModel", "Erro ao obter última sincronização", e)
                    0L
                }
                
                val lastSyncTime = if (lastSyncTimestamp > 0) {
                    dateFormatter.format(Date(lastSyncTimestamp))
                } else {
                    "Nunca"
                }
                
                android.util.Log.d("DashboardViewModel", "Atualizando UI state")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalPatrimonios = totalPatrimonios,
                    pendingCollections = pendingCollections,
                    pendingSync = pendingSync,
                    userName = userName,
                    userProfile = userProfile,
                    lastSyncTime = lastSyncTime,
                    dashboardStats = dashboardStats
                )
                
                android.util.Log.d("DashboardViewModel", "Dados do dashboard carregados com sucesso")
                
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Erro ao carregar dados do dashboard", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar dados: ${e.message}"
                )
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
        // TODO: Descomentar após resolver dependência MPAndroidChart
        // loadColetasEvolucao()
    }
    
    /* TODO: Descomentar após resolver dependência MPAndroidChart
    fun loadColetasEvolucao(dias: Int = 7) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGrafico = true, graficoError = null)
            
            try {
                android.util.Log.d("DashboardViewModel", "Carregando evolução de coletas dos últimos $dias dias")
                
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
                
                val response = withContext(Dispatchers.IO) {
                    apiService.getColetasEvolucao(dias)
                }
                
                android.util.Log.d("DashboardViewModel", "Resposta evolução: isSuccessful=${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val evolucao = body.data ?: emptyList()
                        android.util.Log.d("DashboardViewModel", "Evolução carregada: ${evolucao.size} registros")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoadingGrafico = false,
                            coletasEvolucao = evolucao,
                            graficoError = null
                        )
                    } else {
                        android.util.Log.w("DashboardViewModel", "Resposta com success=false: ${body?.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoadingGrafico = false,
                            graficoError = body?.message ?: "Erro ao carregar evolução"
                        )
                    }
                } else {
                    android.util.Log.w("DashboardViewModel", "Erro HTTP: ${response.code}")
                    _uiState.value = _uiState.value.copy(
                        isLoadingGrafico = false,
                        graficoError = "Erro ao carregar evolução"
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Erro ao carregar evolução de coletas", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingGrafico = false,
                    graficoError = "Erro: ${e.message}"
                )
            }
        }
    }
    */
}