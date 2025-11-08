package com.inventario.mobile.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Armazenamento seguro usando EncryptedSharedPreferences
 * Criptografa dados sensíveis como tokens e credenciais
 */
class SecureStorage(context: Context) {

    companion object {
        private const val TAG = "SecureStorage"
        private const val PREFS_NAME = "secure_prefs"
        
        // Keys
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_ENCRYPTED_PASSWORD = "encrypted_password"
        private const val KEY_LAST_LOGIN = "last_login"
        
        @Volatile
        private var instance: SecureStorage? = null
        
        fun getInstance(context: Context): SecureStorage {
            return instance ?: synchronized(this) {
                instance ?: SecureStorage(context.applicationContext).also { instance = it }
            }
        }
    }

    private val encryptedPrefs: SharedPreferences

    init {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            Log.d(TAG, "SecureStorage inicializado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar SecureStorage", e)
            throw e
        }
    }

    // ========== JWT TOKEN ==========
    
    fun saveJwtToken(token: String) {
        encryptedPrefs.edit().putString(KEY_JWT_TOKEN, token).apply()
        Log.d(TAG, "JWT token salvo")
    }

    fun getJwtToken(): String? {
        return encryptedPrefs.getString(KEY_JWT_TOKEN, null)
    }

    fun clearJwtToken() {
        encryptedPrefs.edit().remove(KEY_JWT_TOKEN).apply()
        Log.d(TAG, "JWT token removido")
    }

    // ========== REFRESH TOKEN ==========
    
    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        Log.d(TAG, "Refresh token salvo")
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // ========== USER INFO ==========
    
    fun saveUserId(userId: Long) {
        encryptedPrefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long {
        return encryptedPrefs.getLong(KEY_USER_ID, -1L)
    }

    fun saveUsername(username: String) {
        encryptedPrefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return encryptedPrefs.getString(KEY_USERNAME, null)
    }

    // ========== BIOMETRIC ==========
    
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        Log.d(TAG, "Biometria ${if (enabled) "habilitada" else "desabilitada"}")
    }

    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Salva senha criptografada para uso com biometria
     * ATENÇÃO: Usar apenas se necessário. Idealmente, não armazenar senha.
     */
    fun saveEncryptedPassword(password: String) {
        encryptedPrefs.edit().putString(KEY_ENCRYPTED_PASSWORD, password).apply()
        Log.d(TAG, "Senha criptografada salva")
    }

    fun getEncryptedPassword(): String? {
        return encryptedPrefs.getString(KEY_ENCRYPTED_PASSWORD, null)
    }

    fun clearEncryptedPassword() {
        encryptedPrefs.edit().remove(KEY_ENCRYPTED_PASSWORD).apply()
        Log.d(TAG, "Senha criptografada removida")
    }

    // ========== SESSION ==========
    
    fun saveLastLogin(timestamp: Long = System.currentTimeMillis()) {
        encryptedPrefs.edit().putLong(KEY_LAST_LOGIN, timestamp).apply()
    }

    fun getLastLogin(): Long {
        return encryptedPrefs.getLong(KEY_LAST_LOGIN, 0L)
    }

    // ========== CLEAR ALL ==========
    
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        Log.d(TAG, "Todos os dados seguros foram removidos")
    }

    fun clearSession() {
        clearJwtToken()
        encryptedPrefs.edit().remove(KEY_REFRESH_TOKEN).apply()
        Log.d(TAG, "Sessão limpa")
    }

    // ========== HELPERS ==========
    
    fun hasValidSession(): Boolean {
        return getJwtToken() != null && getUserId() != -1L
    }

    fun canUseBiometric(): Boolean {
        return isBiometricEnabled() && 
               getUsername() != null && 
               getEncryptedPassword() != null
    }
}
