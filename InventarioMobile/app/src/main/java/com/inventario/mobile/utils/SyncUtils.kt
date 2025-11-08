package com.inventario.mobile.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utilitários para funcionalidades de sincronização
 */
object SyncUtils {
    
    private const val PREF_LAST_SYNC = "last_sync_timestamp"
    private const val PREF_SYNC_STATUS = "sync_status"
    private const val PREF_SYNC_PROGRESS = "sync_progress"
    private const val PREF_SYNC_ERROR = "sync_error"
    private const val PREF_SYNC_STATS = "sync_stats"
    private const val PREF_AUTO_SYNC_ENABLED = "auto_sync_enabled"
    private const val PREF_SYNC_INTERVAL = "sync_interval_minutes"
    private const val PREF_WIFI_ONLY_SYNC = "wifi_only_sync"
    private const val PREF_SYNC_CONFLICTS = "sync_conflicts"
    
    // Intervalos de sincronização em minutos
    const val SYNC_INTERVAL_15_MIN = 15
    const val SYNC_INTERVAL_30_MIN = 30
    const val SYNC_INTERVAL_1_HOUR = 60
    const val SYNC_INTERVAL_2_HOURS = 120
    const val SYNC_INTERVAL_4_HOURS = 240
    const val SYNC_INTERVAL_8_HOURS = 480
    const val SYNC_INTERVAL_12_HOURS = 720
    const val SYNC_INTERVAL_24_HOURS = 1440
    
    /**
     * Obtém o timestamp da última sincronização
     */
    fun getLastSyncTimestamp(context: Context): Long {
        val prefsManager = PreferencesManager(context)
        return prefsManager.getLastSyncTime()
    }
    
    /**
     * Define o timestamp da última sincronização
     */
    fun setLastSyncTimestamp(context: Context, timestamp: Long = System.currentTimeMillis()) {
        val prefsManager = PreferencesManager(context)
        prefsManager.saveLastSyncTime(timestamp)
    }
    
    /**
     * Obtém a data da última sincronização formatada
     */
    fun getLastSyncDate(context: Context): String? {
        val timestamp = getLastSyncTimestamp(context)
        return if (timestamp > 0) {
            DateUtils.formatDateTime(Date(timestamp))
        } else {
            null
        }
    }
    
    /**
     * Obtém o tempo desde a última sincronização
     */
    fun getTimeSinceLastSync(context: Context): Long {
        val lastSync = getLastSyncTimestamp(context)
        return if (lastSync > 0) {
            System.currentTimeMillis() - lastSync
        } else {
            Long.MAX_VALUE
        }
    }
    
    /**
     * Obtém o tempo desde a última sincronização formatado
     */
    fun getTimeSinceLastSyncFormatted(context: Context): String {
        val timeDiff = getTimeSinceLastSync(context)
        
        if (timeDiff == Long.MAX_VALUE) {
            return "Nunca sincronizado"
        }
        
        return DateUtils.formatDuration(timeDiff)
    }
    
    /**
     * Verifica se é necessário sincronizar
     */
    fun needsSync(context: Context): Boolean {
        if (!isAutoSyncEnabled(context)) {
            return false
        }
        
        val timeSinceLastSync = getTimeSinceLastSync(context)
        val syncInterval = getSyncInterval(context)
        val intervalMillis = TimeUnit.MINUTES.toMillis(syncInterval.toLong())
        
        return timeSinceLastSync >= intervalMillis
    }
    
    /**
     * Verifica se pode sincronizar agora (considerando Wi-Fi)
     */
    fun canSyncNow(context: Context): Boolean {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return false
        }
        
        if (isWifiOnlySync(context) && !NetworkUtils.isWifiConnected(context)) {
            return false
        }
        
        return true
    }
    
    /**
     * Obtém o status atual da sincronização
     */
    fun getSyncStatus(context: Context): SyncStatus {
        // Simplificado - usando SharedPreferences diretamente
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        val statusName = prefs.getString(PREF_SYNC_STATUS, SyncStatus.IDLE.name) ?: SyncStatus.IDLE.name
        return try {
            SyncStatus.valueOf(statusName)
        } catch (e: IllegalArgumentException) {
            SyncStatus.IDLE
        }
    }
    
    /**
     * Define o status da sincronização
     */
    fun setSyncStatus(context: Context, status: SyncStatus) {
        // Simplificado - usando SharedPreferences diretamente
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SYNC_STATUS, status.name).apply()
    }
    
    /**
     * Obtém o progresso da sincronização
     */
    fun getSyncProgress(context: Context): SyncProgress {
        // Simplificado - retorna objeto padrão
        return SyncProgress()
    }
    
    /**
     * Define o progresso da sincronização
     */
    fun setSyncProgress(context: Context, progress: SyncProgress) {
        // Simplificado - não faz nada por enquanto
    }
    
    /**
     * Obtém o último erro de sincronização
     */
    fun getLastSyncError(context: Context): String? {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        val error = prefs.getString(PREF_SYNC_ERROR, "") ?: ""
        return if (error.isNotEmpty()) error else null
    }
    
    /**
     * Define o último erro de sincronização
     */
    fun setLastSyncError(context: Context, error: String?) {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SYNC_ERROR, error ?: "").apply()
    }
    
    /**
     * Limpa o último erro de sincronização
     */
    fun clearLastSyncError(context: Context) {
        setLastSyncError(context, null)
    }
    
    /**
     * Obtém as estatísticas da sincronização
     */
    fun getSyncStats(context: Context): SyncStats {
        // Simplificado - retorna objeto padrão
        return SyncStats()
    }
    
    /**
     * Define as estatísticas da sincronização
     */
    fun setSyncStats(context: Context, stats: SyncStats) {
        // Simplificado - não faz nada por enquanto
    }
    
    /**
     * Verifica se a sincronização automática está habilitada
     */
    fun isAutoSyncEnabled(context: Context): Boolean {
        val prefsManager = PreferencesManager(context)
        return prefsManager.isAutoSyncEnabled()
    }
    
    /**
     * Define se a sincronização automática está habilitada
     */
    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        val prefsManager = PreferencesManager(context)
        prefsManager.setAutoSyncEnabled(enabled)
    }
    
    /**
     * Obtém o intervalo de sincronização em minutos
     */
    fun getSyncInterval(context: Context): Int {
        val prefsManager = PreferencesManager(context)
        return prefsManager.getSyncInterval()
    }
    
    /**
     * Define o intervalo de sincronização em minutos
     */
    fun setSyncInterval(context: Context, intervalMinutes: Int) {
        val prefsManager = PreferencesManager(context)
        prefsManager.setSyncInterval(intervalMinutes)
    }
    
    /**
     * Verifica se a sincronização deve ocorrer apenas via Wi-Fi
     */
    fun isWifiOnlySync(context: Context): Boolean {
        val prefsManager = PreferencesManager(context)
        return prefsManager.isWifiOnlySyncEnabled()
    }
    
    /**
     * Define se a sincronização deve ocorrer apenas via Wi-Fi
     */
    fun setWifiOnlySync(context: Context, wifiOnly: Boolean) {
        val prefsManager = PreferencesManager(context)
        prefsManager.setWifiOnlySyncEnabled(wifiOnly)
    }
    
    /**
     * Obtém os conflitos de sincronização
     */
    fun getSyncConflicts(context: Context): List<SyncConflict> {
        // Retorna lista vazia por enquanto - implementação simplificada
        return emptyList()
    }
    
    /**
     * Define os conflitos de sincronização
     */
    fun setSyncConflicts(context: Context, conflicts: List<SyncConflict>) {
        // Implementação simplificada - não faz nada por enquanto
    }
    
    /**
     * Adiciona um conflito de sincronização
     */
    fun addSyncConflict(context: Context, conflict: SyncConflict) {
        val conflicts = getSyncConflicts(context).toMutableList()
        conflicts.add(conflict)
        setSyncConflicts(context, conflicts)
    }
    
    /**
     * Remove um conflito de sincronização
     */
    fun removeSyncConflict(context: Context, conflictId: String) {
        val conflicts = getSyncConflicts(context).filter { it.id != conflictId }
        setSyncConflicts(context, conflicts)
    }
    
    /**
     * Limpa todos os conflitos de sincronização
     */
    fun clearSyncConflicts(context: Context) {
        setSyncConflicts(context, emptyList())
    }
    
    /**
     * Verifica se há conflitos de sincronização
     */
    fun hasSyncConflicts(context: Context): Boolean {
        return getSyncConflicts(context).isNotEmpty()
    }
    
    /**
     * Obtém o próximo horário de sincronização
     */
    fun getNextSyncTime(context: Context): Long {
        val lastSync = getLastSyncTimestamp(context)
        val interval = getSyncInterval(context)
        val intervalMillis = TimeUnit.MINUTES.toMillis(interval.toLong())
        
        return if (lastSync > 0) {
            lastSync + intervalMillis
        } else {
            System.currentTimeMillis()
        }
    }
    
    /**
     * Obtém o próximo horário de sincronização formatado
     */
    fun getNextSyncTimeFormatted(context: Context): String {
        val nextSync = getNextSyncTime(context)
        return DateUtils.formatDateTime(Date(nextSync))
    }
    
    /**
     * Calcula o tempo até a próxima sincronização
     */
    fun getTimeUntilNextSync(context: Context): Long {
        val nextSync = getNextSyncTime(context)
        val now = System.currentTimeMillis()
        return maxOf(0L, nextSync - now)
    }
    
    /**
     * Obtém o tempo até a próxima sincronização formatado
     */
    fun getTimeUntilNextSyncFormatted(context: Context): String {
        val timeUntil = getTimeUntilNextSync(context)
        return if (timeUntil > 0) {
            DateUtils.formatDuration(timeUntil)
        } else {
            "Agora"
        }
    }
    
    /**
     * Verifica se a sincronização está em progresso
     */
    fun isSyncInProgress(context: Context): Boolean {
        return getSyncStatus(context) in listOf(
            SyncStatus.SYNCING,
            SyncStatus.UPLOADING,
            SyncStatus.DOWNLOADING
        )
    }
    
    /**
     * Verifica se houve erro na última sincronização
     */
    fun hasLastSyncError(context: Context): Boolean {
        return getLastSyncError(context) != null
    }
    
    /**
     * Obtém informações resumidas da sincronização
     */
    fun getSyncInfo(context: Context): SyncInfo {
        return SyncInfo(
            lastSyncTimestamp = getLastSyncTimestamp(context),
            lastSyncDate = getLastSyncDate(context),
            timeSinceLastSync = getTimeSinceLastSync(context),
            status = getSyncStatus(context),
            progress = getSyncProgress(context),
            lastError = getLastSyncError(context),
            stats = getSyncStats(context),
            autoSyncEnabled = isAutoSyncEnabled(context),
            syncInterval = getSyncInterval(context),
            wifiOnlySync = isWifiOnlySync(context),
            conflicts = getSyncConflicts(context),
            nextSyncTime = getNextSyncTime(context),
            needsSync = needsSync(context),
            canSyncNow = canSyncNow(context)
        )
    }
    
    /**
     * Reseta todas as configurações de sincronização
     */
    fun resetSyncSettings(context: Context) {
        val prefsManager = PreferencesManager(context)
        // Limpa dados de sessão que incluem configurações de sync
        prefsManager.clearSessionData()
        
        // Manter configurações do usuário
        // PreferencesManager.remove(context, PREF_AUTO_SYNC_ENABLED)
        // PreferencesManager.remove(context, PREF_SYNC_INTERVAL)
        // PreferencesManager.remove(context, PREF_WIFI_ONLY_SYNC)
    }
    
    /**
     * Obtém lista de intervalos de sincronização disponíveis
     */
    fun getAvailableSyncIntervals(): List<SyncInterval> {
        return listOf(
            SyncInterval(SYNC_INTERVAL_15_MIN, "15 minutos"),
            SyncInterval(SYNC_INTERVAL_30_MIN, "30 minutos"),
            SyncInterval(SYNC_INTERVAL_1_HOUR, "1 hora"),
            SyncInterval(SYNC_INTERVAL_2_HOURS, "2 horas"),
            SyncInterval(SYNC_INTERVAL_4_HOURS, "4 horas"),
            SyncInterval(SYNC_INTERVAL_8_HOURS, "8 horas"),
            SyncInterval(SYNC_INTERVAL_12_HOURS, "12 horas"),
            SyncInterval(SYNC_INTERVAL_24_HOURS, "24 horas")
        )
    }
    
    /**
     * Obtém o nome do intervalo de sincronização
     */
    fun getSyncIntervalName(intervalMinutes: Int): String {
        return getAvailableSyncIntervals()
            .find { it.minutes == intervalMinutes }
            ?.name ?: "$intervalMinutes minutos"
    }
    
    /**
     * Valida configurações de sincronização
     */
    fun validateSyncSettings(context: Context): SyncValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Verificar conectividade
        if (!NetworkUtils.isNetworkAvailable(context)) {
            errors.add("Sem conexão com a internet")
        }
        
        // Verificar Wi-Fi se necessário
        if (isWifiOnlySync(context) && !NetworkUtils.isWifiConnected(context)) {
            warnings.add("Sincronização configurada apenas para Wi-Fi, mas não está conectado")
        }
        
        // Verificar intervalo
        val interval = getSyncInterval(context)
        if (interval < SYNC_INTERVAL_15_MIN) {
            warnings.add("Intervalo de sincronização muito baixo pode consumir muita bateria")
        }
        
        // Verificar espaço em disco
        val freeSpace = FileUtils.getAvailableSpace(context)
        if (freeSpace < 100 * 1024 * 1024) { // 100MB
            warnings.add("Pouco espaço em disco disponível")
        }
        
        return SyncValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    // Enums e Data Classes
    
    enum class SyncStatus {
        IDLE,
        SYNCING,
        UPLOADING,
        DOWNLOADING,
        COMPLETED,
        ERROR,
        CANCELLED
    }
    
    enum class ConflictResolution {
        LOCAL_WINS,
        REMOTE_WINS,
        MERGE,
        MANUAL
    }
    
    data class SyncProgress(
        val currentStep: String = "",
        val totalSteps: Int = 0,
        val currentStepIndex: Int = 0,
        val itemsProcessed: Int = 0,
        val totalItems: Int = 0,
        val bytesTransferred: Long = 0L,
        val totalBytes: Long = 0L,
        val startTime: Long = 0L,
        val estimatedTimeRemaining: Long = 0L
    ) {
        val progressPercentage: Int
            get() = if (totalItems > 0) {
                (itemsProcessed * 100 / totalItems)
            } else 0
        
        val stepProgressPercentage: Int
            get() = if (totalSteps > 0) {
                (currentStepIndex * 100 / totalSteps)
            } else 0
        
        val bytesProgressPercentage: Int
            get() = if (totalBytes > 0) {
                (bytesTransferred * 100 / totalBytes).toInt()
            } else 0
    }
    
    data class SyncStats(
        val totalSyncs: Int = 0,
        val successfulSyncs: Int = 0,
        val failedSyncs: Int = 0,
        val lastSyncDuration: Long = 0L,
        val averageSyncDuration: Long = 0L,
        val totalDataTransferred: Long = 0L,
        val itemsSynced: Int = 0,
        val conflictsResolved: Int = 0
    ) {
        val successRate: Double
            get() = if (totalSyncs > 0) {
                successfulSyncs.toDouble() / totalSyncs * 100
            } else 0.0
    }
    
    data class SyncConflict(
        val id: String,
        val entityType: String,
        val entityId: String,
        val localData: Map<String, Any?>,
        val remoteData: Map<String, Any?>,
        val conflictFields: List<String>,
        val timestamp: Long,
        val resolution: ConflictResolution? = null
    )
    
    data class SyncInterval(
        val minutes: Int,
        val name: String
    )
    
    data class SyncInfo(
        val lastSyncTimestamp: Long,
        val lastSyncDate: String?,
        val timeSinceLastSync: Long,
        val status: SyncStatus,
        val progress: SyncProgress,
        val lastError: String?,
        val stats: SyncStats,
        val autoSyncEnabled: Boolean,
        val syncInterval: Int,
        val wifiOnlySync: Boolean,
        val conflicts: List<SyncConflict>,
        val nextSyncTime: Long,
        val needsSync: Boolean,
        val canSyncNow: Boolean
    )
    
    data class SyncValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val warnings: List<String>
    )
}