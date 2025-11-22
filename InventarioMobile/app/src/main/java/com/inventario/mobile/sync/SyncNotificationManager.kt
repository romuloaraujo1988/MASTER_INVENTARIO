package com.inventario.mobile.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.inventario.mobile.R

/**
 * Gerenciador de notificações de sincronização
 * 
 * Responsável por:
 * - Criar canal de notificações
 * - Mostrar progresso de sincronização
 * - Notificar sucesso/erro
 * - Atualizar notificação em tempo real
 */
class SyncNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "sync_channel"
        private const val CHANNEL_NAME = "Sincronização"
        private const val CHANNEL_DESCRIPTION = "Notificações de sincronização de dados"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    /**
     * Cria canal de notificações (Android 8+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Mostra notificação de sincronização em progresso
     */
    fun showSyncInProgress(current: Int, total: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Sincronizando dados")
            .setContentText("$current de $total itens sincronizados")
            .setProgress(total, current, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Mostra notificação de sincronização concluída com sucesso
     */
    fun showSyncSuccess(totalSynced: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("Sincronização concluída")
            .setContentText("$totalSynced itens sincronizados com sucesso")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Mostra notificação de erro na sincronização
     */
    fun showSyncError(errorMessage: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Erro na sincronização")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Mostra notificação de sincronização parcial
     */
    fun showPartialSync(synced: Int, failed: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Sincronização parcial")
            .setContentText("$synced sincronizados, $failed falharam")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cancela notificação de sincronização
     */
    fun cancelSyncNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
