package com.inventario.mobile.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val inventarioRepository: InventarioRepository,
    private val syncScheduler: com.inventario.mobile.sync.SyncScheduler
) : ViewModel() {

    private val _syncResult = MutableLiveData<String>()
    val syncResult: LiveData<String> = _syncResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Auto sync by time
    fun isAutoSyncEnabled(): Boolean = preferencesManager.isAutoSyncEnabled()
    
    fun setAutoSyncEnabled(enabled: Boolean) {
        preferencesManager.setAutoSyncEnabled(enabled)
        
        // Agendar ou cancelar sincronização periódica
        if (enabled) {
            syncScheduler.schedulePeriodicSync()
        } else {
            syncScheduler.cancelPeriodicSync()
        }
    }
    
    fun getSyncInterval(): Int = preferencesManager.getSyncInterval()
    
    fun setSyncInterval(intervalMinutes: Int) {
        preferencesManager.setSyncInterval(intervalMinutes)
        
        // Reagendar sincronização com novo intervalo
        if (isAutoSyncEnabled()) {
            syncScheduler.reschedulePeriodicSync()
        }
    }
    
    // Auto sync by count
    fun isAutoSyncByCountEnabled(): Boolean = preferencesManager.isAutoSyncByCountEnabled()
    
    fun setAutoSyncByCountEnabled(enabled: Boolean) {
        preferencesManager.setAutoSyncByCountEnabled(enabled)
    }
    
    fun getSyncCollectionInterval(): Int = preferencesManager.getSyncCollectionInterval()
    
    fun setSyncCollectionInterval(count: Int) {
        preferencesManager.setSyncCollectionInterval(count)
    }
    
    fun getCollectionCount(): Int = preferencesManager.getCollectionCount()
    
    fun resetCollectionCount() {
        preferencesManager.resetCollectionCount()
    }
    
    // Force offline mode
    fun isForceOfflineMode(): Boolean = preferencesManager.isForceOfflineMode()
    
    fun setForceOfflineMode(enabled: Boolean) {
        preferencesManager.setForceOfflineMode(enabled)
        
        // Se ativar modo offline, cancelar sincronizações agendadas
        if (enabled) {
            syncScheduler.cancelPeriodicSync()
            android.util.Log.d("SettingsViewModel", "Modo offline ativado - sincronizações canceladas")
        } else {
            // Se desativar e auto-sync estiver ativo, reagendar
            if (isAutoSyncEnabled()) {
                syncScheduler.schedulePeriodicSync()
                android.util.Log.d("SettingsViewModel", "Modo offline desativado - sincronizações reagendadas")
            }
        }
    }
    
    // Wi-Fi only sync
    fun isWifiOnlyEnabled(): Boolean = preferencesManager.isWifiOnlySyncEnabled()
    
    fun setWifiOnlyEnabled(enabled: Boolean) {
        preferencesManager.setWifiOnlySyncEnabled(enabled)
        
        // Reagendar sincronização com nova constraint
        if (isAutoSyncEnabled()) {
            syncScheduler.reschedulePeriodicSync()
        }
    }
    
    // Test sync
    fun testSync() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _syncResult.value = "Iniciando sincronização..."
                
                // Executar sincronização imediata via WorkManager
                syncScheduler.syncNow(byCount = false)
                
                _syncResult.value = "Sincronização agendada! Verifique as notificações."
            } catch (e: Exception) {
                _syncResult.value = "Erro ao agendar sincronização: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Helper methods for interval adjustments
    fun increaseSyncInterval() {
        val current = getSyncInterval()
        val newInterval = when {
            current < 60 -> current + 15  // Increase by 15 minutes if less than 1 hour
            current < 240 -> current + 30 // Increase by 30 minutes if less than 4 hours
            else -> current + 60          // Increase by 1 hour
        }
        setSyncInterval(minOf(newInterval, 1440)) // Max 24 hours
    }
    
    fun decreaseSyncInterval() {
        val current = getSyncInterval()
        val newInterval = when {
            current <= 60 -> current - 15  // Decrease by 15 minutes if 1 hour or less
            current <= 240 -> current - 30 // Decrease by 30 minutes if 4 hours or less
            else -> current - 60           // Decrease by 1 hour
        }
        setSyncInterval(maxOf(newInterval, 15)) // Min 15 minutes
    }
    
    fun increaseSyncCount() {
        val current = getSyncCollectionInterval()
        val newCount = current + if (current < 10) 1 else 5
        setSyncCollectionInterval(minOf(newCount, 100)) // Max 100 collections
    }
    
    fun decreaseSyncCount() {
        val current = getSyncCollectionInterval()
        val newCount = current - if (current <= 10) 1 else 5
        setSyncCollectionInterval(maxOf(newCount, 1)) // Min 1 collection
    }
}