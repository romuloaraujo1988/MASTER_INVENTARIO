package com.inventario.mobile.sync

import android.content.Context
import com.inventario.mobile.utils.NotificationUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper injetável para notificações de sincronização
 * Wrapper do NotificationUtils para uso com Hilt
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Mostra notificação de sincronização bem-sucedida
     */
    fun showSyncSuccess(stats: SyncStats) {
        NotificationUtils.showSyncSuccess(context, stats)
    }
    
    /**
     * Mostra notificação de erro na sincronização
     */
    fun showSyncError(error: SyncError) {
        NotificationUtils.showSyncError(context, error)
    }
    
    /**
     * Remove notificação de sincronização
     */
    fun dismissSyncNotification() {
        NotificationUtils.dismissSyncNotification(context)
    }
}
