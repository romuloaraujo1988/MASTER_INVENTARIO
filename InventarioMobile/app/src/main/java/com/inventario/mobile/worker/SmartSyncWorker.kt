package com.inventario.mobile.worker

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.utils.NetworkMonitor
import com.inventario.mobile.utils.PreferencesManager

/**
 * Worker para sincronização inteligente em background
 * 
 * Sincroniza automaticamente quando:
 * - Tem conexão de rede (WiFi ou dados móveis)
 * - Bateria está acima de 20%
 * - Auto-sync está habilitado
 */
class SmartSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "SmartSyncWorker"
        const val WORK_NAME = "smart_sync"
        private const val MIN_BATTERY_LEVEL = 20
    }
    
    private val preferencesManager = PreferencesManager(context)
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "SmartSyncWorker iniciado")
            
            // Verificar se auto-sync está habilitado
            if (!preferencesManager.isAutoSyncEnabled()) {
                Log.d(TAG, "Auto-sync desabilitado, pulando sincronização")
                return Result.success()
            }
            
            // Verificar condições para sincronização
            if (!shouldSync()) {
                Log.d(TAG, "Condições não atendidas, reagendando")
                return Result.retry()
            }
            
            Log.d(TAG, "Condições atendidas, iniciando sincronização...")
            
            // Executar sincronização
            val success = performSync()
            
            if (success) {
                Log.d(TAG, "Sincronização concluída com sucesso")
                
                // Atualizar timestamp da última sincronização
                preferencesManager.saveLastSyncTime(System.currentTimeMillis())
                
                Result.success()
            } else {
                Log.w(TAG, "Sincronização falhou")
                
                // Tentar novamente se não excedeu o limite de tentativas
                if (runAttemptCount < 3) {
                    Log.d(TAG, "Tentativa ${runAttemptCount + 1}/3, reagendando")
                    Result.retry()
                } else {
                    Log.e(TAG, "Limite de tentativas excedido")
                    Result.failure()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização inteligente", e)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    /**
     * Verifica se as condições para sincronização estão atendidas
     */
    private fun shouldSync(): Boolean {
        // 1. Verificar conexão de rede
        val networkMonitor = NetworkMonitor(applicationContext)
        val hasNetwork = networkMonitor.isConnected()
        
        if (!hasNetwork) {
            Log.d(TAG, "Sem conexão de rede")
            return false
        }
        
        // 2. Verificar se é WiFi only
        if (preferencesManager.isWifiOnlySyncEnabled()) {
            val isWifi = networkMonitor.isWifiConnected()
            if (!isWifi) {
                Log.d(TAG, "WiFi only habilitado, mas não está em WiFi")
                return false
            }
        }
        
        // 3. Verificar nível de bateria
        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        
        if (batteryLevel < MIN_BATTERY_LEVEL) {
            Log.d(TAG, "Bateria baixa: $batteryLevel%")
            return false
        }
        
        Log.d(TAG, "Todas as condições atendidas:")
        Log.d(TAG, "- Rede: OK")
        Log.d(TAG, "- WiFi: ${if (preferencesManager.isWifiOnlySyncEnabled()) "Requerido e OK" else "Não requerido"}")
        Log.d(TAG, "- Bateria: $batteryLevel%")
        
        return true
    }
    
    /**
     * Executa a sincronização
     */
    private suspend fun performSync(): Boolean {
        return try {
            // TODO: Implementar lógica de sincronização real
            // Por enquanto, apenas simula sucesso
            
            Log.d(TAG, "Executando sincronização...")
            
            // Aqui você chamaria seu repository de sincronização
            // Exemplo:
            // val syncRepository = SyncRepository(...)
            // val result = syncRepository.syncAll()
            // return result.isSuccess
            
            // Simulação de sincronização
            kotlinx.coroutines.delay(1000)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar sincronização", e)
            false
        }
    }
}
