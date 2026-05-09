package com.inventario.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.inventario.mobile.data.model.CacheServerStats
import com.inventario.mobile.domain.model.DashboardStats

/**
 * Gerenciador de preferências compartilhadas
 */
class PreferencesManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "inventario_secure_prefs", // Novo nome isola dos dados antigos puramente em texto
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        android.util.Log.e("PreferencesManager", "Falha ao abrir EncryptedSharedPreferences, recriando...", e)
        // Se a Keystore corromper, precisamos apagar o arquivo problemático
        val prefsFile = java.io.File(context.applicationInfo.dataDir, "shared_prefs/inventario_secure_prefs.xml")
        if (prefsFile.exists()) prefsFile.delete()
        
        EncryptedSharedPreferences.create(
            context,
            "inventario_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    init {
        // Remove arquivo de preferências antigo (plaintext) por fins de segurança
        val oldPrefsFile = java.io.File(context.applicationInfo.dataDir, "shared_prefs/inventario_mobile_prefs.xml")
        if (oldPrefsFile.exists()) {
            oldPrefsFile.delete()
            android.util.Log.d("PreferencesManager", "Arquivo antigo de SharedPreferences apagado.")
        }
    }
    
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
        val now = System.currentTimeMillis()
        putString("access_token", accessToken)
        putString("refresh_token", refreshToken)
        putLong("token_expires_at", now + (expiresIn * 1000))
        putBoolean("token_valid", true)
        
        // Salvar timestamp de login apenas se for um novo login (não renovação)
        // Se já existe um login_timestamp recente (menos de 1 hora), não sobrescrever
        val existingLoginTimestamp = getLong("login_timestamp", 0L)
        val oneHour = 60 * 60 * 1000L
        if (existingLoginTimestamp == 0L || (now - existingLoginTimestamp) > oneHour) {
            putLong("login_timestamp", now)
            android.util.Log.d("PreferencesManager", "✓ Novo login registrado em: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(now))}")
        }
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
     * Obtém timestamp do último login bem-sucedido
     */
    fun getLoginTimestamp(): Long {
        return getLong("login_timestamp", 0L)
    }
    
    /**
     * Salva timestamp do login
     */
    fun setLoginTimestamp(timestamp: Long) {
        putLong("login_timestamp", timestamp)
    }
    
    /**
     * Obtém tempo restante do token em formato legível
     */
    fun getTokenTimeRemaining(): String {
        val expiresAt = getLong("token_expires_at", 0L)
        val now = System.currentTimeMillis()
        val remaining = expiresAt - now
        
        if (remaining <= 0) return "Expirado"
        
        val hours = remaining / (60 * 60 * 1000)
        val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
        
        return when {
            hours > 24 -> "${hours / 24} dias"
            hours > 0 -> "${hours}h ${minutes}min"
            else -> "${minutes} minutos"
        }
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
    
    // ===== VIBRAÇÃO AO COLETAR =====
    
    /**
     * Verifica se a vibração ao coletar está habilitada
     * @return true se habilitada (padrão: true)
     */
    fun isVibrationOnCollectionEnabled(): Boolean {
        return getBoolean("vibration_on_collection", true)
    }
    
    /**
     * Habilita ou desabilita vibração ao coletar
     * @param enabled true para habilitar, false para desabilitar
     */
    fun setVibrationOnCollectionEnabled(enabled: Boolean) {
        putBoolean("vibration_on_collection", enabled)
        android.util.Log.d("PreferencesManager", "Vibração ao coletar ${if (enabled) "HABILITADA" else "DESABILITADA"}")
    }
    
    // ===== ESTADO FIXO PARA COLETA RÁPIDA =====
    
    /**
     * Verifica se fixar o estado da coleta está habilitado
     */
    fun isEstadoFixoEnabled(): Boolean {
        return getBoolean("estado_fixo_enabled", false)
    }
    
    /**
     * Habilita ou desabilita opção de estado fixo na coleta
     */
    fun setEstadoFixoEnabled(enabled: Boolean) {
        putBoolean("estado_fixo_enabled", enabled)
        android.util.Log.d("PreferencesManager", "Estado fixo da coleta ${if (enabled) "HABILITADO" else "DESABILITADO"}")
    }

    /**
     * Obtém o valor do estado fixo da coleta
     */
    fun getEstadoFixo(): String? {
        return getString("estado_fixo_valor", "").takeIf { it.isNotEmpty() }
    }

    /**
     * Define o valor do estado fixo da coleta
     */
    fun setEstadoFixo(estado: String?) {
        if (estado != null) {
            putString("estado_fixo_valor", estado)
            android.util.Log.d("PreferencesManager", "Estado fixo salvo: $estado")
        } else {
            remove("estado_fixo_valor")
            android.util.Log.d("PreferencesManager", "Estado fixo removido")
        }
    }
    
    // ===== FOTO OPCIONAL NA COLETA (v2.11) =====
    
    /**
     * Verifica se a foto opcional está habilitada
     * @return true se habilitada (padrão: true)
     */
    fun isPhotoOnCollectionEnabled(): Boolean {
        return getBoolean("photo_on_collection_enabled", true)
    }
    
    /**
     * Habilita ou desabilita foto opcional na coleta
     */
    fun setPhotoOnCollectionEnabled(enabled: Boolean) {
        putBoolean("photo_on_collection_enabled", enabled)
        android.util.Log.d("PreferencesManager", "Foto na coleta ${if (enabled) "HABILITADA" else "DESABILITADA"}")
    }
    
    /**
     * Verifica se deve sincronizar fotos apenas em Wi-Fi
     * @return true se apenas Wi-Fi (padrão: true)
     */
    fun isPhotoSyncWifiOnly(): Boolean {
        return getBoolean("photo_sync_wifi_only", true)
    }
    
    /**
     * Define se deve sincronizar fotos apenas em Wi-Fi
     */
    fun setPhotoSyncWifiOnly(wifiOnly: Boolean) {
        putBoolean("photo_sync_wifi_only", wifiOnly)
    }
    
    /**
     * Obtém qualidade de compressão de foto (30-100)
     * @return qualidade JPEG (padrão: 65)
     */
    fun getPhotoQuality(): Int {
        return getInt("photo_quality", 65)
    }
    
    /**
     * Define qualidade de compressão de foto
     */
    fun setPhotoQuality(quality: Int) {
        putInt("photo_quality", quality.coerceIn(30, 100))
    }
    
    // ===== DARK MODE =====
    
    companion object {
        private const val PREFS_NAME = "inventario_mobile_prefs"
        
        // Constantes para Dark Mode
        const val THEME_MODE_SYSTEM = 0  // Seguir sistema
        const val THEME_MODE_LIGHT = 1   // Sempre claro
        const val THEME_MODE_DARK = 2    // Sempre escuro

        // ===== Cache de estatísticas do servidor (dashboard-refactor-clean, Req 3.6, 3.8, 3.9) =====
        // Chaves usadas pelos métodos saveCacheServerStats / getCacheServerStats /
        // getCacheServerStatsTimestamp / clearCacheServerStats. Persistidas no
        // EncryptedSharedPreferences único deste PreferencesManager.
        private const val KEY_CACHE_SERVER_TOTAL_PATRIMONIOS = "cache_server_total_patrimonios"
        private const val KEY_CACHE_SERVER_TOTAL_COLETADOS = "cache_server_total_coletados"
        private const val KEY_CACHE_SERVER_DIVERGENCIAS = "cache_server_divergencias"
        private const val KEY_CACHE_SERVER_COLETORES_ATIVOS = "cache_server_coletores_ativos"
        private const val KEY_CACHE_SERVER_VALOR_TOTAL = "cache_server_valor_total"
        private const val KEY_CACHE_SERVER_INVENTARIO_NOME = "cache_server_inventario_nome"
        private const val KEY_CACHE_SERVER_INVENTARIO_ID = "cache_server_inventario_id"
        private const val KEY_CACHE_SERVER_TIMESTAMP = "cache_server_timestamp"

        // Sentinel value para inventarioId ausente (nullable no domínio).
        private const val CACHE_INVENTARIO_ID_NULL_SENTINEL = -1
    }
    
    /**
     * Obtém o modo de tema atual
     * @return THEME_MODE_SYSTEM (0), THEME_MODE_LIGHT (1) ou THEME_MODE_DARK (2)
     */
    fun getThemeMode(): Int {
        return getInt("theme_mode", THEME_MODE_SYSTEM)
    }
    
    /**
     * Define o modo de tema
     * @param mode THEME_MODE_SYSTEM (0), THEME_MODE_LIGHT (1) ou THEME_MODE_DARK (2)
     */
    fun setThemeMode(mode: Int) {
        putInt("theme_mode", mode)
        android.util.Log.d("PreferencesManager", "Tema alterado para: ${getThemeModeName(mode)}")
    }
    
    /**
     * Verifica se Dark Mode está habilitado
     */
    fun isDarkModeEnabled(): Boolean {
        return getThemeMode() == THEME_MODE_DARK
    }
    
    /**
     * Habilita ou desabilita Dark Mode diretamente
     * @param enabled true para Dark Mode, false para Light Mode
     */
    fun setDarkModeEnabled(enabled: Boolean) {
        setThemeMode(if (enabled) THEME_MODE_DARK else THEME_MODE_LIGHT)
    }
    
    /**
     * Verifica se está usando tema do sistema
     */
    fun isSystemTheme(): Boolean {
        return getThemeMode() == THEME_MODE_SYSTEM
    }
    
    /**
     * Obtém nome legível do modo de tema
     */
    fun getThemeModeName(mode: Int = getThemeMode()): String {
        return when (mode) {
            THEME_MODE_SYSTEM -> "Automático (Sistema)"
            THEME_MODE_LIGHT -> "Claro"
            THEME_MODE_DARK -> "Escuro"
            else -> "Desconhecido"
        }
    }
    
    // ===== SINCRONIZAÇÃO DE FOTOS DE REFERÊNCIA (v2.9) =====
    
    /**
     * Obtém timestamp da última sincronização de fotos de referência
     */
    fun getLastFotoReferenciaSyncTimestamp(): Long {
        return getLong("last_foto_referencia_sync_timestamp", 0L)
    }
    
    /**
     * Salva timestamp da última sincronização de fotos de referência
     */
    fun setLastFotoReferenciaSyncTimestamp(timestamp: Long) {
        putLong("last_foto_referencia_sync_timestamp", timestamp)
        android.util.Log.d("PreferencesManager", "Timestamp de sync de fotos atualizado: $timestamp")
    }
    
    /**
     * Verifica se a sincronização de fotos está habilitada
     */
    fun isFotoReferenciaSyncEnabled(): Boolean {
        return getBoolean("foto_referencia_sync_enabled", true)
    }
    
    /**
     * Habilita ou desabilita sincronização de fotos de referência
     */
    fun setFotoReferenciaSyncEnabled(enabled: Boolean) {
        putBoolean("foto_referencia_sync_enabled", enabled)
        android.util.Log.d("PreferencesManager", "Sync de fotos ${if (enabled) "HABILITADO" else "DESABILITADO"}")
    }
    
    /**
     * Obtém limite de armazenamento de fotos em MB
     */
    fun getFotoReferenciaStorageLimitMB(): Int {
        return getInt("foto_referencia_storage_limit_mb", 100)
    }
    
    /**
     * Define limite de armazenamento de fotos em MB
     */
    fun setFotoReferenciaStorageLimitMB(limitMB: Int) {
        putInt("foto_referencia_storage_limit_mb", limitMB.coerceIn(10, 500))
    }
    
    /**
     * Limpa dados de sincronização de fotos de referência
     */
    fun clearFotoReferenciaSync() {
        remove("last_foto_referencia_sync_timestamp")
        android.util.Log.d("PreferencesManager", "Dados de sync de fotos limpos")
    }

    // ==========================================================================
    // Cache do DashboardStats vindo do servidor (dashboard-refactor-clean)
    // Requisitos: 3.6, 3.8, 3.9
    //
    // Contrato central: enquanto nunca houve uma sincronização bem-sucedida do
    // endpoint base de estatísticas (`api/mobile/dashboard/stats*`),
    // `getCacheServerStats()` retorna `null`. Esse `null` é o sinal usado por
    // `DashboardRepositoryImpl.buscarEstatisticasLocais` para cair no fallback
    // de Room (Req 3.9). Após o primeiro save, o timestamp passa a ser > 0 e
    // o cache é reconstruído campo a campo.
    // ==========================================================================

    /**
     * Persiste o snapshot mais recente de estatísticas do servidor no
     * `EncryptedSharedPreferences`, registrando também o timestamp UTC de
     * sincronização em [KEY_CACHE_SERVER_TIMESTAMP].
     *
     * Campos nullable ([DashboardStats.inventarioNome],
     * [DashboardStats.inventarioId]) são gravados respectivamente como
     * `putString(key, null)` e como o sentinel [CACHE_INVENTARIO_ID_NULL_SENTINEL]
     * (-1), seguindo o padrão usado pelos demais métodos do arquivo para
     * representar ausência sem criar chaves com semântica ambígua.
     *
     * Requisitos: 3.6, 3.8.
     */
    fun saveCacheServerStats(stats: DashboardStats) {
        prefs.edit().apply {
            putInt(KEY_CACHE_SERVER_TOTAL_PATRIMONIOS, stats.totalPatrimonios)
            putInt(KEY_CACHE_SERVER_TOTAL_COLETADOS, stats.totalColetados)
            putInt(KEY_CACHE_SERVER_DIVERGENCIAS, stats.divergencias)
            putInt(KEY_CACHE_SERVER_COLETORES_ATIVOS, stats.coletoresAtivos)
            putFloat(KEY_CACHE_SERVER_VALOR_TOTAL, stats.valorTotal.toFloat())
            // inventarioNome é nullable: grava null explicitamente para permitir
            // getString(key, null) recuperar a ausência.
            putString(KEY_CACHE_SERVER_INVENTARIO_NOME, stats.inventarioNome)
            // inventarioId é nullable: usa sentinel -1 já que putInt não aceita null.
            putInt(
                KEY_CACHE_SERVER_INVENTARIO_ID,
                stats.inventarioId ?: CACHE_INVENTARIO_ID_NULL_SENTINEL
            )
            putLong(KEY_CACHE_SERVER_TIMESTAMP, System.currentTimeMillis())
        }.apply()
    }

    /**
     * Recupera o último snapshot de estatísticas do servidor persistido,
     * ou `null` quando ainda não houve nenhuma sincronização bem-sucedida.
     *
     * A detecção de "nunca sincronizou" é feita via
     * `cache_server_timestamp == 0L`. Qualquer valor diferente de 0 significa
     * que [saveCacheServerStats] já foi chamado ao menos uma vez e o cache
     * deve ser reconstruído campo a campo. `inventarioId` retorna `null`
     * quando o valor armazenado é [CACHE_INVENTARIO_ID_NULL_SENTINEL];
     * `inventarioNome` retorna `null` quando armazenado como `null` (ou
     * ausente).
     *
     * Esse contrato de `null` é essencial para o fallback offline do
     * `DashboardRepositoryImpl.buscarEstatisticasLocais` (Req 3.6, 3.9).
     */
    fun getCacheServerStats(): CacheServerStats? {
        val ts = prefs.getLong(KEY_CACHE_SERVER_TIMESTAMP, 0L)
        if (ts == 0L) return null

        val storedInventarioId = prefs.getInt(
            KEY_CACHE_SERVER_INVENTARIO_ID,
            CACHE_INVENTARIO_ID_NULL_SENTINEL
        )
        val inventarioId = if (storedInventarioId == CACHE_INVENTARIO_ID_NULL_SENTINEL) {
            null
        } else {
            storedInventarioId
        }

        return CacheServerStats(
            totalPatrimonios = prefs.getInt(KEY_CACHE_SERVER_TOTAL_PATRIMONIOS, 0),
            totalColetados = prefs.getInt(KEY_CACHE_SERVER_TOTAL_COLETADOS, 0),
            divergencias = prefs.getInt(KEY_CACHE_SERVER_DIVERGENCIAS, 0),
            coletoresAtivos = prefs.getInt(KEY_CACHE_SERVER_COLETORES_ATIVOS, 0),
            valorTotal = prefs.getFloat(KEY_CACHE_SERVER_VALOR_TOTAL, 0f).toDouble(),
            inventarioNome = prefs.getString(KEY_CACHE_SERVER_INVENTARIO_NOME, null),
            inventarioId = inventarioId,
            timestamp = ts
        )
    }

    /**
     * Retorna o timestamp (ms UTC) da última sincronização bem-sucedida do
     * servidor ou `0L` quando o cache ainda não foi populado — mesmo sentinel
     * usado por [getCacheServerStats] para retornar `null`.
     *
     * Requisitos: 3.6.
     */
    fun getCacheServerStatsTimestamp(): Long {
        return prefs.getLong(KEY_CACHE_SERVER_TIMESTAMP, 0L)
    }

    /**
     * Remove todas as chaves `cache_server_*` do [EncryptedSharedPreferences].
     * Após esta chamada, [getCacheServerStats] volta a retornar `null` e
     * [getCacheServerStatsTimestamp] volta a retornar `0L`, reproduzindo o
     * estado inicial "nunca sincronizou" exigido pelos Req 3.6 e 3.9.
     */
    fun clearCacheServerStats() {
        prefs.edit()
            .remove(KEY_CACHE_SERVER_TOTAL_PATRIMONIOS)
            .remove(KEY_CACHE_SERVER_TOTAL_COLETADOS)
            .remove(KEY_CACHE_SERVER_DIVERGENCIAS)
            .remove(KEY_CACHE_SERVER_COLETORES_ATIVOS)
            .remove(KEY_CACHE_SERVER_VALOR_TOTAL)
            .remove(KEY_CACHE_SERVER_INVENTARIO_NOME)
            .remove(KEY_CACHE_SERVER_INVENTARIO_ID)
            .remove(KEY_CACHE_SERVER_TIMESTAMP)
            .apply()
    }
}
