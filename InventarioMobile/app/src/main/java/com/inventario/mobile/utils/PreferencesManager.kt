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
    
    // ========================================
    // Sala Fixada (v2.9) - Permite fixar uma sala para coleta rápida
    // ========================================
    
    /**
     * Fixa uma sala para aparecer sempre no topo ou ser a única visível
     */
    fun setSalaFixada(salaId: Long, salaNome: String) {
        putLong("sala_fixada_id", salaId)
        putString("sala_fixada_nome", salaNome)
        android.util.Log.d("PreferencesManager", "✓ Sala fixada: $salaNome (ID: $salaId)")
    }
    
    /**
     * Obtém o ID da sala fixada (0 se nenhuma)
     */
    fun getSalaFixadaId(): Long {
        return getLong("sala_fixada_id", 0L)
    }
    
    /**
     * Obtém o nome da sala fixada
     */
    fun getSalaFixadaNome(): String {
        return getString("sala_fixada_nome", "")
    }
    
    /**
     * Verifica se há uma sala fixada
     */
    fun hasSalaFixada(): Boolean {
        return getSalaFixadaId() > 0
    }
    
    /**
     * Remove a sala fixada (volta a mostrar todas)
     */
    fun clearSalaFixada() {
        remove("sala_fixada_id")
        remove("sala_fixada_nome")
        android.util.Log.d("PreferencesManager", "✓ Sala fixada removida")
    }
    
    // Métodos de autenticação e tokens
    fun getAccessToken(): String? = getString("access_token", "").takeIf { it.isNotEmpty() }
    fun isTokenValid(): Boolean = getBoolean("token_valid", false)
    fun getDeviceId(): String? = getString("device_id", "").takeIf { it.isNotEmpty() }
    fun saveDeviceId(deviceId: String) = putString("device_id", deviceId)
    
    // Métodos de dispositivo
    fun getDispositivoId(): Int = getInt("dispositivo_id", 0)
    fun saveDispositivoId(dispositivoId: Int) = putInt("dispositivo_id", dispositivoId)
    
    // Métodos de inventário
    // IMPORTANTE: Default é 0 (não 1!) para evitar usar ID inexistente
    // O ID correto deve ser obtido via getInventarioAtivoId() que é salvo no login
    @Deprecated("Use getInventarioAtivoId() que é salvo automaticamente no login")
    fun getInventarioId(): Int = getInt("inventario_id", 0)
    fun saveInventarioId(inventarioId: Int) = putInt("inventario_id", inventarioId)
    
    // Métodos de modo offline forçado
    fun setForceOfflineMode(enabled: Boolean) {
        putBoolean("force_offline_mode", enabled)
        android.util.Log.d("PreferencesManager", "Modo offline forçado ${if (enabled) "ATIVADO" else "DESATIVADO"}")
    }
    
    fun isForceOfflineMode(): Boolean {
        return getBoolean("force_offline_mode", false)
    }
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
    
    // ========== MÉTODOS PARA LOGIN OFFLINE COM BIOMETRIA ==========
    
    /**
     * Verifica se a biometria está habilitada para login
     */
    fun isBiometricEnabled(): Boolean {
        return getBoolean("biometric_enabled", false)
    }
    
    /**
     * Habilita ou desabilita login por biometria
     */
    fun setBiometricEnabled(enabled: Boolean) {
        putBoolean("biometric_enabled", enabled)
        android.util.Log.d("PreferencesManager", "Login por biometria ${if (enabled) "HABILITADO" else "DESABILITADO"}")
    }
    
    /**
     * Verifica se há um usuário salvo localmente
     */
    fun hasUserSavedLocally(): Boolean {
        return getString("saved_username", "").isNotEmpty()
    }
    
    /**
     * Obtém o username do usuário salvo
     */
    fun getSavedUsername(): String? {
        return getString("saved_username", "").takeIf { it.isNotEmpty() }
    }
    
    /**
     * Salva o username do usuário
     */
    fun setSavedUsername(username: String) {
        putString("saved_username", username)
        android.util.Log.d("PreferencesManager", "Username salvo: $username")
    }
    
    /**
     * Obtém o nome completo do usuário salvo
     */
    fun getSavedUserFullName(): String? {
        return getString("saved_user_full_name", "").takeIf { it.isNotEmpty() }
    }
    
    /**
     * Salva o nome completo do usuário
     */
    fun setSavedUserFullName(fullName: String) {
        putString("saved_user_full_name", fullName)
        android.util.Log.d("PreferencesManager", "Nome completo salvo: $fullName")
    }
    
    /**
     * Limpa todos os dados do usuário salvo
     */
    fun clearSavedUser() {
        remove("saved_username")
        remove("saved_user_full_name")
        remove("biometric_enabled")
        remove("access_token")
        remove("refresh_token")
        remove("token_expires_at")
        remove("token_valid")
        remove("user_logged_in")
        remove("user_name")
        remove("user_profile")
        android.util.Log.d("PreferencesManager", "Dados do usuário limpos")
    }
    
    /**
     * Salva dados completos do usuário para login offline
     */
    fun saveUserForOfflineLogin(username: String, fullName: String, accessToken: String) {
        setSavedUsername(username)
        setSavedUserFullName(fullName)
        putString("access_token", accessToken)
        putBoolean("user_logged_in", true)
        android.util.Log.d("PreferencesManager", "Usuário salvo para login offline: $username")
    }
    
    /**
     * Salva o último usuário que fez login (para preencher automaticamente)
     */
    fun saveLastLoginUsername(username: String) {
        putString("last_login_username", username)
        android.util.Log.d("PreferencesManager", "Último usuário salvo: $username")
    }
    
    /**
     * Obtém o último usuário que fez login
     */
    fun getLastLoginUsername(): String? {
        return getString("last_login_username", "").takeIf { it.isNotEmpty() }
    }
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
    
    // ===== MÉTODOS DE INVENTÁRIO ATIVO =====
    
    /**
     * Salva dados do inventário ativo
     */
    fun saveInventarioAtivo(id: Int, nome: String, status: String = "EM_ANDAMENTO") {
        putInt("inventario_ativo_id", id)
        putString("inventario_ativo_nome", nome)
        putString("inventario_ativo_status", status)
        putLong("inventario_ativo_timestamp", System.currentTimeMillis())
        android.util.Log.d("PreferencesManager", "Inventário ativo salvo: ID=$id, Nome=$nome")
    }
    
    /**
     * Obtém ID do inventário ativo
     */
    fun getInventarioAtivoId(): Int? {
        val id = getInt("inventario_ativo_id", 0)
        return if (id > 0) id else null
    }
    
    /**
     * Obtém nome do inventário ativo
     */
    fun getInventarioAtivoNome(): String? {
        return getString("inventario_ativo_nome", "").takeIf { it.isNotEmpty() }
    }
    
    /**
     * Obtém status do inventário ativo
     */
    fun getInventarioAtivoStatus(): String? {
        return getString("inventario_ativo_status", "").takeIf { it.isNotEmpty() }
    }
    
    /**
     * Verifica se há inventário ativo salvo
     */
    fun hasInventarioAtivo(): Boolean {
        return getInventarioAtivoId() != null
    }
    
    /**
     * Limpa dados do inventário ativo
     */
    fun clearInventarioAtivo() {
        remove("inventario_ativo_id")
        remove("inventario_ativo_nome")
        remove("inventario_ativo_status")
        remove("inventario_ativo_timestamp")
        android.util.Log.d("PreferencesManager", "Inventário ativo limpo")
    }
    
    /**
     * Salva estatísticas do inventário
     */
    fun saveInventarioEstatisticas(
        totalPatrimonios: Int,
        totalColetados: Int,
        percentualConclusao: Double
    ) {
        putInt("inventario_total_patrimonios", totalPatrimonios)
        putInt("inventario_total_coletados", totalColetados)
        putFloat("inventario_percentual_conclusao", percentualConclusao.toFloat())
    }
    
    /**
     * Obtém total de patrimônios do inventário
     */
    fun getInventarioTotalPatrimonios(): Int {
        return getInt("inventario_total_patrimonios", 0)
    }
    
    /**
     * Obtém total de patrimônios coletados
     */
    fun getInventarioTotalColetados(): Int {
        return getInt("inventario_total_coletados", 0)
    }
    
    /**
     * Obtém percentual de conclusão do inventário
     */
    fun getInventarioPercentualConclusao(): Float {
        return getFloat("inventario_percentual_conclusao", 0f)
    }
    
    // ===== MÉTODOS DE REFRESH TOKEN =====
    
    /**
     * Obtém refresh token
     */
    fun getRefreshToken(): String? {
        return getString("refresh_token", "").takeIf { it.isNotEmpty() }
    }
    
    /**
     * Verifica se o token está próximo de expirar (menos de 5 minutos)
     */
    fun isTokenExpiringSoon(): Boolean {
        val expiresAt = getLong("token_expires_at", 0L)
        val now = System.currentTimeMillis()
        val fiveMinutes = 5 * 60 * 1000L
        return (expiresAt - now) < fiveMinutes
    }
    
    /**
     * Verifica se o token expirou
     */
    fun isTokenExpired(): Boolean {
        val expiresAt = getLong("token_expires_at", 0L)
        return System.currentTimeMillis() >= expiresAt
    }
    
    /**
     * Obtém ID do usuário
     */
    fun getUserId(): Int? {
        val id = getInt("user_id", 0)
        return if (id > 0) id else null
    }
    
    /**
     * Salva ID do usuário
     */
    fun saveUserId(userId: Int) {
        putInt("user_id", userId)
    }
}
