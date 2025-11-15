package com.inventario.mobile.presentation.sala

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.usecase.BuscarSalasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel Clean para seleção de salas
 * Usa Clean Architecture + MVVM + Hilt + Paging 3
 */
@HiltViewModel
class SalaSelectionViewModelClean @Inject constructor(
    private val buscarSalasUseCase: BuscarSalasUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SalaSelectionState>(SalaSelectionState.Idle)
    val state: StateFlow<SalaSelectionState> = _state.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    companion object {
        private const val TAG = "SalaSelectionVMClean"
    }

    /**
     * Busca salas com paginação
     * Retorna Flow de PagingData para uso com PagingDataAdapter
     */
    fun buscarSalas(query: String = ""): Flow<PagingData<Sala>> {
        Log.d(TAG, "buscarSalas: query='$query'")
        _searchQuery.value = query
        
        return buscarSalasUseCase(query)
            .cachedIn(viewModelScope) // Cache para sobreviver a mudanças de configuração
    }
    
    /**
     * Atualiza query de busca
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Limpa estado de erro
     */
    fun clearError() {
        if (_state.value is SalaSelectionState.Error) {
            _state.value = SalaSelectionState.Idle
        }
    }
}

/**
 * Estados possíveis da tela de seleção de salas
 */
sealed class SalaSelectionState {
    object Idle : SalaSelectionState()
    object Loading : SalaSelectionState()
    data class Success(val message: String) : SalaSelectionState()
    data class Error(val message: String) : SalaSelectionState()
}
