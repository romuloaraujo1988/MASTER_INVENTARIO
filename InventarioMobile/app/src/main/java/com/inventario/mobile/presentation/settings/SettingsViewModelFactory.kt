package com.inventario.mobile.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.utils.PreferencesManager

class SettingsViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val repository: InventarioRepository,
    private val syncScheduler: com.inventario.mobile.sync.SyncScheduler
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferencesManager, repository, syncScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
