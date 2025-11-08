package com.inventario.mobile.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.inventario.mobile.R
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.remote.api.ApiClient
import com.inventario.mobile.data.sync.SyncManager
import com.inventario.mobile.data.sync.SyncResult
import com.inventario.mobile.presentation.offline.OfflineManagerActivity
import com.inventario.mobile.utils.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker para sincronização em background
 * Executa periodicamente ou quando solicitado
 */
class SyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "sync_worker"
        const val NOTIFICATION_CHANNEL_ID = "sync_channel"
        const val NOTIFICATION_ID = 1001
        
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Verificar se há conexão
            val networkMonitor = NetworkMonitor(context)
            if (!networkMonitor.isCurrentlyOnline()) {
                return@withContext Result.retry()
            }
            
            // Verificar se deve sincronizar apenas via WiFi
            val syncOnWifiOnly = getSyncOnWifiOnlyPreference()
            if (syncOnWifiOnly && networkMonitor.getConnectionType() != NetworkMonitor.ConnectionType.WIFI) {
                return@withContext Result.retry()
            }
            
            // Criar notificação de progresso
            setForeground(createForegroundInfo("Sincronizando dados..."))
            
            // Executar sincronização
            val database = InventarioDatabase.getDatabase(context)
            val apiService = ApiClient.getApiService(context)
            val syncManager = SyncManager.getInstance(context, database, apiService)
            
            val result = syncManager.syncPendingData()
            
            // Processar resultado
            when (result) {
                is SyncResult.Success -> {
                    if (result.success > 0) {
                        showSuccessNotification(result)
                    }
                    Result.success()
                }
                
                is SyncResult.Error -> {
                    showErrorNotification(result.exception.message ?: "Erro desconhecido")
                    Result.retry()
                }
                
                SyncResult.NoConnection -> {
                    Result.retry()
                }
                
                SyncResult.AlreadySyncing -> {
                    Result.success()
                }
            }
            
        } catch (e: Exception) {
            showErrorNotification(e.message ?: "Erro desconhecido")
            Result.retry()
        }
    }

    /**
     * Cria informações para execução em foreground
     */
    private fun createForegroundInfo(message: String): ForegroundInfo {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sincronização")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
        
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    /**
     * Mostra notificação de sucesso
     */
    private fun showSuccessNotification(result: SyncResult.Success) {
        if (!getNotificationsEnabledPreference()) return
        
        createNotificationChannel()
        
        val intent = Intent(context, OfflineManagerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val message = buildString {
            append("${result.success} itens sincronizados")
            if (result.errors > 0) {
                append(", ${result.errors} erros")
            }
        }
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sincronização concluída")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync_done)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Mostra notificação de erro
     */
    private fun showErrorNotification(message: String) {
        if (!getNotificationsEnabledPreference()) return
        
        createNotificationChannel()
        
        val intent = Intent(context, OfflineManagerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Erro na sincronização")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync_error)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cria canal de notificação
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sincronização",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de sincronização de dados"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Obtém preferência de sincronização apenas via WiFi
     */
    private fun getSyncOnWifiOnlyPreference(): Boolean {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("sync_wifi_only", true)
    }

    /**
     * Obtém preferência de notificações
     */
    private fun getNotificationsEnabledPreference(): Boolean {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
    }
}
