package com.inventario.mobile.presentation.sala

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.usecase.BuscarSalasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * ViewModel para seleção de sala com paginação
 * 
 * Features:
 * - Paginação com Paging 3
 * - Busca em tempo real com debounce
 * - Cache de resultados
 * - Cancelamento automático de buscas anteriores
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SalaViewModelPaging @Inject constructor(
    private val buscarSalasUseCase: BuscarSalasUseCase
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * Flow de PagingData com salas
     * 
     * - Debounce de 300ms para evitar requisições excessivas
     * - distinctUntilChanged para ignorar queries duplicadas
     * - flatMapLatest para cancelar buscas anteriores
     * - cachedIn para manter cache durante rotação
     */
    val salaPagingData: Flow<PagingData<Sala>> = searchQuery
        .debounce(300) // Aguarda 300ms após última digitação
        .distinctUntilChanged() // Ignora valores duplicados
        .flatMapLatest { query ->
            android.util.Log.d(TAG, "Buscando salas: query='$query'")
            buscarSalasUseCase(query)
        }
        .cachedIn(viewModelScope) // Mantém cache durante rotação
    
    /**
     * Define query de busca
     */
    fun setSearchQuery(query: String) {
        android.util.Log.d(TAG, "setSearchQuery: '$query'")
        _searchQuery.value = query
    }
    
    /**
     * Limpa busca
     */
    fun clearSearch() {
        android.util.Log.d(TAG, "clearSearch")
        _searchQuery.value = ""
    }
    
    companion object {
        private const val TAG = "SalaViewModelPaging"
    }
}
