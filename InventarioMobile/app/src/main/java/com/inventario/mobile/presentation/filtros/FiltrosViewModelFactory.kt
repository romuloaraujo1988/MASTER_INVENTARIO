package com.inventario.mobile.presentation.filtros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository

class FiltrosViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FiltrosViewModel::class.java)) {
            return FiltrosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}