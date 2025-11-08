package com.inventario.mobile.presentation.descricao

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DescricaoItem(
    val descricao: String,
    val quantidade: Int
)

data class DescricaoSelectionUiState(
    val isLoading: Boolean = false,
    val descricoes: List<DescricaoItem> = emptyList(),
    val errorMessage: String? = null
)

class DescricaoSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DescricaoSelectionUiState())
    val uiState: StateFlow<DescricaoSelectionUiState> = _uiState.asStateFlow()

    fun loadDescricoes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
                val response = withContext(Dispatchers.IO) {
                    apiService.getDescricoes()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data ?: emptyList()
                    val descricoes = data.map { item ->
                        DescricaoItem(
                            descricao = item["descricao"] as? String ?: "",
                            quantidade = (item["quantidade"] as? Double)?.toInt() ?: 0
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        descricoes = descricoes
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar descrições"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DescricaoViewModel", "Erro ao carregar descrições", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro: ${e.message}"
                )
            }
        }
    }

    fun searchDescricoes(termo: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
                val response = withContext(Dispatchers.IO) {
                    apiService.searchDescricoes(termo)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data ?: emptyList()
                    val descricoes = data.map { item ->
                        DescricaoItem(
                            descricao = item["descricao"] as? String ?: "",
                            quantidade = (item["quantidade"] as? Double)?.toInt() ?: 0
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        descricoes = descricoes
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Erro ao buscar descrições"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DescricaoViewModel", "Erro ao buscar descrições", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro: ${e.message}"
                )
            }
        }
    }
}
