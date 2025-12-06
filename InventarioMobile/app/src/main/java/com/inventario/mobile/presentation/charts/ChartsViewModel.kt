package com.inventario.mobile.presentation.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gerenciar dados dos gráficos
 */
@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val chartDataProvider: ChartDataProvider
) : ViewModel() {

    private val _progressData = MutableStateFlow<ProgressData?>(null)
    val progressData: StateFlow<ProgressData?> = _progressData.asStateFlow()

    private val _statusData = MutableStateFlow<StatusData?>(null)
    val statusData: StateFlow<StatusData?> = _statusData.asStateFlow()

    private val _evolutionData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val evolutionData: StateFlow<Map<String, Int>> = _evolutionData.asStateFlow()

    private val _topItemsData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val topItemsData: StateFlow<Map<String, Int>> = _topItemsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Carrega todos os dados dos gráficos
     */
    fun loadChartData(idInventario: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Carregar dados em paralelo
                launch { loadProgressData(idInventario) }
                launch { loadStatusData(idInventario) }
                launch { loadEvolutionData(idInventario) }
                launch { loadTopItemsData(idInventario) }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadProgressData(idInventario: Int) {
        try {
            val data = chartDataProvider.getProgressData(idInventario)
            _progressData.value = data
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun loadStatusData(idInventario: Int) {
        try {
            val data = chartDataProvider.getStatusData(idInventario)
            _statusData.value = data
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun loadEvolutionData(idInventario: Int) {
        try {
            val data = chartDataProvider.getEvolutionData(idInventario)
            _evolutionData.value = data
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun loadTopItemsData(idInventario: Int) {
        try {
            val data = chartDataProvider.getTopItemsData(idInventario)
            _topItemsData.value = data
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Recarrega todos os dados
     */
    fun refresh(idInventario: Int) {
        loadChartData(idInventario)
    }
}
