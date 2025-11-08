package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository

class CollectionViewViewModelFactory(
    private val inventarioRepository: InventarioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectionViewViewModel::class.java)) {
            return CollectionViewViewModel(inventarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
