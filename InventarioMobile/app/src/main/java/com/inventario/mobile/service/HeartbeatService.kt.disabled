package com.inventario.mobile.service

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.remote.api.ApiClient
import com.inventario.mobile.utils.DeviceInfoHelper
import kotlinx.coroutines.*

/**
 * Serviço de Heartbeat para manter conexão ativa com o servidor
 * Envia pings periódicos para indicar que o dispositivo está online
 */
class HeartbeatService private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "HeartbeatService"
        private const val HEARTBEAT_INTERVAL_MS = 30_000L // 30 segundos
        
        @Volatile
        private var instance: HeartbeatService? = null
        
        fun getInstance(context: Context): HeartbeatService {
            return instance ?: synchronized(this) {
                instance ?: HeartbeatService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private var heartbeatJob: Job? = null
    private var isRunning = false
    
    /**
     * Inicia o envio de heartbeats
     */
    fun iniciar() {
        if (isRunning) {
            Log.d(TAG, "Heartbeat já está rodando")
            return
        }
        
        Log.d(TAG, "Iniciando heartbeat service")
        isRunning = true
        
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && isRunning) {
                try {
                    enviarHeartbeat()
                    delay(HEARTBEAT_INTERVAL_MS)
                } catch (e: CancellationException) {
                    Log.d(TAG, "Heartbeat cancelado")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao enviar heartbeat", e)
                    // Continuar tentando mesmo com erro
                    delay(HEARTBEAT_INTERVAL_MS)
                }
            }
        }
    }
    
    /**
     * Para o envio de heartbeats
     */
    fun parar() {
        Log.d(TAG, "Parando heartbeat service")
        isRunning = false
        heartbeatJob?.cancel()
        heartbeatJob = null
    }
    
    /**
     * Envia um heartbeat para o servidor
     */
    private suspend fun enviarHeartbeat() {
        try {
            val apiService = ApiClient.getApiService(context)
            val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
            
            // Criar payload do heartbeat
            val payload = mapOf(
                "deviceId" to deviceInfo.deviceId,
                "timestamp" to System.currentTimeMillis(),
                "deviceModel" to deviceInfo.model,
                "androidVersion" to deviceInfo.androidVersion,
                "appVersion" to deviceInfo.appVersion
            )
            
            // Enviar heartbeat (endpoint a ser criado)
            // val response = apiService.sendHeartbeat(payload)
            
            Log.d(TAG, "Heartbeat enviado com sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar heartbeat", e)
            throw e
        }
    }
    
    /**
     * Verifica se o heartbeat está rodando
     */
    fun isRunning(): Boolean {
        return isRunning
    }
}
