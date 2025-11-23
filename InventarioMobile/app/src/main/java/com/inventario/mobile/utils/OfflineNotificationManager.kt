package com.inventario.mobile.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.inventario.mobile.R
import com.inventario.mobile.presentation.sync.SyncActivity

/**
 * Gerenciador de notificações para modo offline
 */
class OfflineNotificationManager(private val context: Context) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    companion object {
        private const val CHANNEL_ID = "offline_mode_channel"
        private const val CHANNEL_NAME = "Modo Offline"
        private const val CHANNEL_DESCRIPTION = "Notificações sobre modo offline e sincronização"
        
        private const val NOTIFICATION_ID_OFFLINE = 1001
        private const val NOTIFICATION_ID_SYNC_NEEDED = 1002
        private const val NOTIFICATION_ID_SYNC_SUCCESS = 1003
        
        @Volatile
        private var instance: OfflineNotificationManager? = null
        
        fun getInstance(context: Context): OfflineNotificationManager {
            return instance ?: synchronized(this) {
                instance ?: OfflineNotificationManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Cria canal de notificação (Android 8+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Mostra notificação de modo offline
     */
    fun showOfflineModeNotification(hasLocalData: Boolean) {
        val message = if (hasLocalData) {
            "Trabalhando em modo offline. Seus dados serão sincronizados quando a conexão for restabelecida."
        } else {
            "Sem conexão e sem dados locais. Conecte-se à internet para sincronizar."
        }
        
        val intent = if (!hasLocalData) {
            // Se não tem dados, abrir tela de sincronização
            Intent(context, SyncActivity::class.java)
        } else {
            // Se tem dados, abrir app normalmente
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.inventario.mobile.R.drawable.ic_offline)
            .setContentTitle("📴 Modo Offline")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_OFFLINE, notification)
    }
    
    /**
     * Mostra notificação pedindo sincronização
     */
    fun showSyncNeededNotification() {
        val intent = Intent(context, SyncActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.inventario.mobile.R.drawable.ic_sync)
            .setContentTitle("⚠️ Sincronização Necessária")
            .setContentText("Você está sem dados locais. Toque para sincronizar.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Conecte-se à internet e sincronize os dados para trabalhar offline."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                com.inventario.mobile.R.drawable.ic_sync,
                "Sincronizar",
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_SYNC_NEEDED, notification)
    }
    
    /**
     * Mostra notificação de sincronização bem-sucedida
     */
    fun showSyncSuccessNotification(itemsSynced: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.inventario.mobile.R.drawable.ic_check)
            .setContentTitle("✅ Sincronização Concluída")
            .setContentText("$itemsSynced itens sincronizados com sucesso")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000) // Auto-dismiss após 5 segundos
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_SYNC_SUCCESS, notification)
    }
    
    /**
     * Mostra notificação de conexão restaurada
     */
    fun showConnectionRestoredNotification(hasPendingSync: Boolean) {
        val message = if (hasPendingSync) {
            "Conexão restaurada! Você tem dados pendentes para sincronizar."
        } else {
            "Conexão restaurada! Você está online novamente."
        }
        
        val intent = if (hasPendingSync) {
            Intent(context, SyncActivity::class.java)
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.inventario.mobile.R.drawable.ic_online)
            .setContentTitle("🌐 Conexão Restaurada")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        if (hasPendingSync) {
            builder.addAction(
                com.inventario.mobile.R.drawable.ic_sync,
                "Sincronizar Agora",
                pendingIntent
            )
        }
        
        notificationManager.notify(NOTIFICATION_ID_OFFLINE, builder.build())
    }
    
    /**
     * Cancela notificação de modo offline
     */
    fun cancelOfflineNotification() {
        notificationManager.cancel(NOTIFICATION_ID_OFFLINE)
    }
    
    /**
     * Cancela notificação de sincronização necessária
     */
    fun cancelSyncNeededNotification() {
        notificationManager.cancel(NOTIFICATION_ID_SYNC_NEEDED)
    }
    
    /**
     * Cancela todas as notificações
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
