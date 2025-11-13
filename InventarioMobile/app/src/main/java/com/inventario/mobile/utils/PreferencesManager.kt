package com.inventario.mobile.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerenciador de preferências compartilhadas
 */
class PreferencesManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "inventario_mobile_prefs"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
    
    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return prefs.getLong(key, defaultValue)
    }
    
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    fun putFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }
    
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return prefs.getFloat(key, defaultValue)
    }
    
    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
    
    fun clear() {
        prefs.edit().clear().apply()
    }
    
    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
    
    // Métodos específicos para sala atual
    fun setCurrentSalaId(salaId: Int) {
        putInt("current_sala_id", salaId)
    }
    
    fun getCurrentSalaId(): Int {
        return getInt("current_sala_id", 0)
    }
    
    fun setCurrentSalaNome(salaNome: String) {
        putString("current_sala_nome", salaNome)
    }
    
    fun getCurrentSalaNome(): String {
        return getString("current_sala_nome", "")
    }
    
    // Método para limpar dados de sessão
    fun clearSessionData() {
        remove("current_sala_id")
        remove("current_sala_nome")
    }
    
    // Métodos de autenticação e tokens
    fun getAccessToken(): String? = getString("access_token", "").takeIf { it.isNotEmpty() }
    fun isTokenValid(): Boolean = getBoolean("token_valid", false)
    fun getDeviceId(): String? = getString("device_id", "").takeIf { it.isNotEmpty() }
    fun saveDeviceId(deviceId: String) = putString("device_id", deviceId)
    
    // Métodos de dispositivo
    fun getDispositivoId(): Int = getInt("dispositivo_id", 0)
    fun saveDispositivoId(dispositivoId: Int) = putInt("dispositivo_id", dispositivoId)
    fun getDispositivoStatus(): String = getString("dispositivo_status", "PENDENTE")
    fun saveDispositivoStatus(status: String) = putString("dispositivo_status", status)
    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        putString("access_token", accessToken)
        putString("refresh_token", refreshToken)
        putLong("token_expires_at", System.currentTimeMillis() + (expiresIn * 1000))
        putBoolean("token_valid", true)
    }
    fun saveUserData(usuario: Any) {
        // Implementação simplificada - pode ser expandida conforme necessário
        putBoolean("user_logged_in", true)
        
        // Salvar nome e perfil do usuário
        try {
            val usuarioClass = usuario::class.java
            val nomeField = usuarioClass.getDeclaredField("nome")
            nomeField.isAccessible = true
            val nome = nomeField.get(usuario) as? String
            if (nome != null) {
                putString("user_name", nome)
            }
            
            val perfilField = usuarioClass.getDeclaredField("perfil")
            perfilField.isAccessible = true
            val perfil = perfilField.get(usuario) as? String
            if (perfil != null) {
                putString("user_profile", perfil)
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Erro ao salvar dados do usuário", e)
        }
    }
    fun isLoggedIn(): Boolean = getBoolean("user_logged_in", false)
    
    // Métodos de usuário
    fun getUserName(): String = getString("user_name", "")
    fun getUserProfile(): String = getString("user_profile", "")
    
    // Métodos de sincronização
    fun getLastSyncTime(): Long = getLong("last_sync_time", 0L)
    fun saveLastSyncTime(time: Long) = putLong("last_sync_time", time)
    fun isAutoSyncEnabled(): Boolean = getBoolean("auto_sync_enabled", false)
    fun setAutoSyncEnabled(enabled: Boolean) = putBoolean("auto_sync_enabled", enabled)
    fun getSyncInterval(): Int = getInt("sync_interval", 30)
    fun setSyncInterval(interval: Int) = putInt("sync_interval", interval)
    fun isWifiOnlySyncEnabled(): Boolean = getBoolean("wifi_only_sync", true)
    fun setWifiOnlySyncEnabled(enabled: Boolean) = putBoolean("wifi_only_sync", enabled)
    
    // ===== SINCRONIZAÇÃO DELTA (INCREMENTAL) =====
    
    /**
     * Obtém o timestamp da última sincronização para um inventário específico
     */
    fun getLastSyncTimestamp(inventarioId: Long): Long {
        return getLong("last_sync_timestamp_$inventarioId", 0L)
    }
    
    /**
     * Salva o timestamp da última sincronização para um inventário específico
     */
    fun setLastSyncTimestamp(inventarioId: Long, timestamp: Long) {
        putLong("last_sync_timestamp_$inventarioId", timestamp)
    }
    
    /**
     * Obtém o timestamp da última sincronização de patrimônios
     */
    fun getLastPatrimonioSyncTimestamp(): Long {
        return getLong("last_patrimonio_sync_timestamp", 0L)
    }
    
    /**
     * Salva o timestamp da última sincronização de patrimônios
     */
    fun setLastPatrimonioSyncTimestamp(timestamp: Long) {
        putLong("last_patrimonio_sync_timestamp", timestamp)
    }
    
    /**
     * Obtém o timestamp da última sincronização de salas
     */
    fun getLastSalaSyncTimestamp(): Long {
        return getLong("last_sala_sync_timestamp", 0L)
    }
    
    /**
     * Salva o timestamp da última sincronização de salas
     */
    fun setLastSalaSyncTimestamp(timestamp: Long) {
        putLong("last_sala_sync_timestamp", timestamp)
    }
    
    /**
     * Limpa todos os timestamps de sincronização
     */
    fun clearSyncTimestamps() {
        // Limpar timestamps de inventários
        val keys = prefs.all.keys.filter { it.startsWith("last_sync_timestamp_") }
        val editor = prefs.edit()
        keys.forEach { editor.remove(it) }
        
        // Limpar outros timestamps
        editor.remove("last_patrimonio_sync_timestamp")
        editor.remove("last_sala_sync_timestamp")
        editor.apply()
    }
    
    // Métodos de contagem de coletas
    fun shouldSyncByCollectionCount(): Boolean = getBoolean("sync_by_collection_count", false)
    fun resetCollectionCount() = putInt("collection_count", 0)
    fun incrementCollectionCount() {
        val current = getInt("collection_count", 0)
        putInt("collection_count", current + 1)
    }
    fun getCollectionCount(): Int = getInt("collection_count", 0)
    fun isAutoSyncByCountEnabled(): Boolean = getBoolean("auto_sync_by_count", false)
    fun setAutoSyncByCountEnabled(enabled: Boolean) = putBoolean("auto_sync_by_count", enabled)
    fun getSyncCollectionInterval(): Int = getInt("sync_collection_interval", 10)
    fun setSyncCollectionInterval(interval: Int) = putInt("sync_collection_interval", interval)
    
    // Métodos de servidor
    fun setServerUrl(url: String) = putString("server_url", url)
    fun getServerUrl(): String? = getString("server_url", "").takeIf { it.isNotEmpty() }
    
    // ===== NOVAS CONFIGURAÇÕES DE SINCRONIZAÇÃO =====
    
    /**
     * Verifica se sincronização apenas via Wi-Fi está habilitada
     */
    fun isWifiOnlyEnabled(): Boolean = getBoolean("wifi_only_sync", true)
    
    /**
     * Define se sincronização deve ser apenas via Wi-Fi
     */
    fun setWifiOnlyEnabled(enabled: Boolean) = putBoolean("wifi_only_sync", enabled)
    
    /**
     * Verifica se modo economia de bateria está habilitado
     */
    fun isBatterySaverEnabled(): Boolean = getBoolean("battery_saver_enabled", false)
    
    /**
     * Define se modo economia de bateria está habilitado
     */
    fun setBatterySaverEnabled(enabled: Boolean) = putBoolean("battery_saver_enabled", enabled)
    
    /**
     * Obtém timestamp da última sincronização bem-sucedida
     */
    fun getLastSyncTimestamp(): Long = getLong("last_sync_timestamp", 0L)
    
    /**
     * Salva timestamp da última sincronização bem-sucedida
     */
    fun setLastSyncTimestamp(timestamp: Long) = putLong("last_sync_timestamp", timestamp)
    
    /**
     * Define contador de coletas
     */
    fun setCollectionCount(count: Int) = putInt("collection_count", count)
}
