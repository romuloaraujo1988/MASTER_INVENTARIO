package com.inventario.mobile.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.inventario.mobile.R

/**
 * Utilitários para funcionalidades de notificações
 */
object NotificationUtils {
    
    // IDs dos canais de notificação
    const val CHANNEL_SYNC = "sync_channel"
    const val CHANNEL_COLETA = "coleta_channel"
    const val CHANNEL_ERRO = "erro_channel"
    const val CHANNEL_GERAL = "geral_channel"
    
    // IDs das notificações
    const val NOTIFICATION_SYNC_ID = 1001
    const val NOTIFICATION_COLETA_ID = 1002
    const val NOTIFICATION_ERRO_ID = 1003
    const val NOTIFICATION_BACKUP_ID = 1004
    const val NOTIFICATION_UPDATE_ID = 1005
    
    /**
     * Cria todos os canais de notificação
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal de sincronização
            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sincronização",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações sobre sincronização de dados"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            
            // Canal de coleta
            val coletaChannel = NotificationChannel(
                CHANNEL_COLETA,
                "Coleta de Patrimônio",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações sobre coleta de patrimônio"
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.primary)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal de erro
            val erroChannel = NotificationChannel(
                CHANNEL_ERRO,
                "Erros e Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações sobre erros e alertas importantes"
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.error)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal geral
            val geralChannel = NotificationChannel(
                CHANNEL_GERAL,
                "Geral",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações gerais do aplicativo"
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.primary)
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannels(listOf(
                syncChannel, coletaChannel, erroChannel, geralChannel
            ))
        }
    }
    
    /**
     * Verifica se as notificações estão habilitadas
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Verifica se um canal específico está habilitado
     */
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return areNotificationsEnabled(context)
    }
    
    /**
     * Mostra notificação de sincronização em progresso
     */
    fun showSyncProgressNotification(
        context: Context,
        progress: Int,
        maxProgress: Int,
        message: String = "Sincronizando dados..."
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setContentTitle("Sincronização")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync)
            .setProgress(maxProgress, progress, false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_SYNC_ID, notification)
    }
    
    /**
     * Mostra notificação de sincronização concluída
     */
    fun showSyncCompletedNotification(
        context: Context,
        success: Boolean,
        itemsCount: Int = 0,
        clickIntent: Intent? = null
    ) {
        val title = if (success) "Sincronização concluída" else "Erro na sincronização"
        val message = if (success) {
            if (itemsCount > 0) "$itemsCount itens sincronizados" else "Dados atualizados"
        } else {
            "Falha ao sincronizar dados"
        }
        
        val icon = if (success) R.drawable.ic_sync_done else R.drawable.ic_sync_error
        val channelId = if (success) CHANNEL_SYNC else CHANNEL_ERRO
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setPriority(if (success) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_SYNC_ID, builder.build())
    }
    
    /**
     * Remove notificação de sincronização
     */
    fun dismissSyncNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_SYNC_ID)
    }
    
    /**
     * Mostra notificação de coleta realizada
     */
    fun showColetaNotification(
        context: Context,
        patrimonioId: String,
        patrimonioDescricao: String,
        clickIntent: Intent? = null
    ) {
        val title = "Patrimônio coletado"
        val message = "$patrimonioId - $patrimonioDescricao"
        
        val builder = NotificationCompat.Builder(context, CHANNEL_COLETA)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_COLETA_ID, builder.build())
    }
    
    /**
     * Mostra notificação de erro
     */
    fun showErrorNotification(
        context: Context,
        title: String,
        message: String,
        clickIntent: Intent? = null,
        actionText: String? = null,
        actionIntent: Intent? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ERRO)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 300, 200, 300))
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        if (actionText != null && actionIntent != null) {
            val actionPendingIntent = PendingIntent.getActivity(
                context,
                1,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_action, actionText, actionPendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ERRO_ID, builder.build())
    }
    
    /**
     * Mostra notificação de backup
     */
    fun showBackupNotification(
        context: Context,
        success: Boolean,
        filePath: String? = null,
        clickIntent: Intent? = null
    ) {
        val title = if (success) "Backup concluído" else "Erro no backup"
        val message = if (success) {
            "Backup salvo${if (filePath != null) " em $filePath" else ""}"
        } else {
            "Falha ao criar backup"
        }
        
        val icon = if (success) R.drawable.ic_backup_done else R.drawable.ic_backup_error
        val channelId = if (success) CHANNEL_GERAL else CHANNEL_ERRO
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setPriority(if (success) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH)
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_BACKUP_ID, builder.build())
    }
    
    /**
     * Mostra notificação de atualização disponível
     */
    fun showUpdateAvailableNotification(
        context: Context,
        version: String,
        downloadIntent: Intent? = null
    ) {
        val title = "Atualização disponível"
        val message = "Nova versão $version disponível para download"
        
        val builder = NotificationCompat.Builder(context, CHANNEL_GERAL)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_update)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        downloadIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
            builder.addAction(R.drawable.ic_download, "Baixar", pendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_UPDATE_ID, builder.build())
    }
    
    /**
     * Mostra notificação personalizada
     */
    fun showCustomNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        iconResId: Int = R.drawable.ic_notification,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        autoCancel: Boolean = true,
        ongoing: Boolean = false,
        sound: Boolean = true,
        vibration: Boolean = true,
        largeIcon: Bitmap? = null,
        clickIntent: Intent? = null,
        actions: List<NotificationAction> = emptyList()
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(iconResId)
            .setPriority(priority)
            .setAutoCancel(autoCancel)
            .setOngoing(ongoing)
        
        if (sound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
        
        if (vibration) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        }
        
        largeIcon?.let {
            builder.setLargeIcon(it)
        }
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        actions.forEachIndexed { index, action ->
            val actionPendingIntent = PendingIntent.getActivity(
                context,
                index + 1,
                action.intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(action.iconResId, action.title, actionPendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(notificationId, builder.build())
    }
    
    /**
     * Mostra notificação expandida com texto longo
     */
    fun showBigTextNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        shortText: String,
        longText: String,
        iconResId: Int = R.drawable.ic_notification,
        clickIntent: Intent? = null
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(shortText)
            .setSmallIcon(iconResId)
            .setStyle(NotificationCompat.BigTextStyle().bigText(longText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(notificationId, builder.build())
    }
    
    /**
     * Mostra notificação com imagem
     */
    fun showBigPictureNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        bigPicture: Bitmap,
        iconResId: Int = R.drawable.ic_notification,
        clickIntent: Intent? = null
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(iconResId)
            .setLargeIcon(bigPicture)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bigPicture)
                    .bigLargeIcon(null as Bitmap?) // Remove large icon when expanded
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        clickIntent?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        
        NotificationManagerCompat.from(context)
            .notify(notificationId, builder.build())
    }
    
    /**
     * Mostra notificação de progresso indeterminado
     */
    fun showIndeterminateProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }
    
    /**
     * Cancela notificação específica
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancela todas as notificações
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
    
    /**
     * Obtém ícone padrão do app como bitmap
     */
    fun getAppIconBitmap(context: Context): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cria bitmap a partir de resource
     */
    fun createBitmapFromResource(context: Context, resourceId: Int): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, resourceId)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Verifica se uma notificação está ativa
     */
    fun isNotificationActive(context: Context, notificationId: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val activeNotifications = notificationManager.activeNotifications
            return activeNotifications.any { it.id == notificationId }
        }
        return false
    }
    
    /**
     * Obtém contagem de notificações ativas
     */
    fun getActiveNotificationsCount(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.activeNotifications.size
        } else {
            0
        }
    }
    
    /**
     * Data class para ações de notificação
     */
    data class NotificationAction(
        val title: String,
        val iconResId: Int,
        val intent: Intent
    )
    
    /**
     * Configurações de notificação
     */
    data class NotificationConfig(
        val channelId: String,
        val title: String,
        val message: String,
        val iconResId: Int = R.drawable.ic_notification,
        val priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        val autoCancel: Boolean = true,
        val ongoing: Boolean = false,
        val sound: Boolean = true,
        val vibration: Boolean = true,
        val largeIcon: Bitmap? = null,
        val clickIntent: Intent? = null,
        val actions: List<NotificationAction> = emptyList()
    )
    
    /**
     * Builder para notificações personalizadas
     */
    class NotificationBuilder(private val context: Context) {
        private var config = NotificationConfig(CHANNEL_GERAL, "", "")
        
        fun setChannel(channelId: String) = apply { config = config.copy(channelId = channelId) }
        fun setTitle(title: String) = apply { config = config.copy(title = title) }
        fun setMessage(message: String) = apply { config = config.copy(message = message) }
        fun setIcon(iconResId: Int) = apply { config = config.copy(iconResId = iconResId) }
        fun setPriority(priority: Int) = apply { config = config.copy(priority = priority) }
        fun setAutoCancel(autoCancel: Boolean) = apply { config = config.copy(autoCancel = autoCancel) }
        fun setOngoing(ongoing: Boolean) = apply { config = config.copy(ongoing = ongoing) }
        fun setSound(sound: Boolean) = apply { config = config.copy(sound = sound) }
        fun setVibration(vibration: Boolean) = apply { config = config.copy(vibration = vibration) }
        fun setLargeIcon(largeIcon: Bitmap?) = apply { config = config.copy(largeIcon = largeIcon) }
        fun setClickIntent(intent: Intent?) = apply { config = config.copy(clickIntent = intent) }
        fun addAction(action: NotificationAction) = apply { 
            config = config.copy(actions = config.actions + action) 
        }
        
        fun show(notificationId: Int) {
            showCustomNotification(
                context = context,
                notificationId = notificationId,
                channelId = config.channelId,
                title = config.title,
                message = config.message,
                iconResId = config.iconResId,
                priority = config.priority,
                autoCancel = config.autoCancel,
                ongoing = config.ongoing,
                sound = config.sound,
                vibration = config.vibration,
                largeIcon = config.largeIcon,
                clickIntent = config.clickIntent,
                actions = config.actions
            )
        }
    }
    
    /**
     * Mostra notificação de sincronização bem-sucedida
     * Usado pelo ColetaSyncWorker
     */
    fun showSyncSuccess(context: Context, stats: com.inventario.mobile.sync.SyncStats) {
        val title = "Sincronização concluída"
        val message = if (stats.failed > 0) {
            "${stats.synced} coletas sincronizadas, ${stats.failed} falhas"
        } else {
            "${stats.synced} coletas sincronizadas com sucesso"
        }
        
        showSyncCompletedNotification(
            context = context,
            success = true,
            itemsCount = stats.synced
        )
    }
    
    /**
     * Mostra notificação de erro na sincronização
     * Usado pelo ColetaSyncWorker
     */
    fun showSyncError(context: Context, error: com.inventario.mobile.sync.SyncError) {
        val title = "Erro na sincronização"
        val message = when (error) {
            is com.inventario.mobile.sync.SyncError.NetworkError -> error.message
            is com.inventario.mobile.sync.SyncError.ServerError -> error.message
            is com.inventario.mobile.sync.SyncError.TimeoutError -> error.message
            is com.inventario.mobile.sync.SyncError.AuthError -> error.message
            is com.inventario.mobile.sync.SyncError.ValidationError -> error.message
            is com.inventario.mobile.sync.SyncError.UnknownError -> error.cause?.message ?: "Erro desconhecido"
        }
        
        // Criar intent para tentar novamente (abre Settings)
        val retryIntent = Intent(context, com.inventario.mobile.presentation.settings.SettingsActivity::class.java)
        
        showErrorNotification(
            context = context,
            title = title,
            message = message,
            clickIntent = retryIntent,
            actionText = "Tentar novamente",
            actionIntent = retryIntent
        )
    }
}