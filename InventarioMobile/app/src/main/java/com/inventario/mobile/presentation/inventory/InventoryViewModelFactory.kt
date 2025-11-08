package com.inventario.mobile.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository

class InventoryViewModelFactory(
    private val inventarioRepository: InventarioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(inventarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}