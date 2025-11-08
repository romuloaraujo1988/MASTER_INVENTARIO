package com.inventario.mobile.presentation.descricao

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DescricaoSelectionViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DescricaoSelectionViewModel::class.java)) {
            return DescricaoSelectionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
