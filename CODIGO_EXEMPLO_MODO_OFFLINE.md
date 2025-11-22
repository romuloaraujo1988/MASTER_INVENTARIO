# 💻 Código Exemplo - Modo Offline

**Data:** 22/11/2025  
**Objetivo:** Código pronto para implementação

---

## 📦 **Dependências (build.gradle)**

```gradle
dependencies {
    // Biometria
    implementation "androidx.biometric:biometric:1.2.0-alpha05"
    
    // Criptografia
    implementation "androidx.security:security-crypto:1.1.0-alpha06"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    
    // Lifecycle
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
    
    // Hilt (se ainda não tiver)
    implementation "com.google.dagger:hilt-android:2.48"
    kapt "com.google.dagger:hilt-compiler:2.48"
}
```

---

## 🔐 **1. BiometricAuthManager.kt**

```kotlin
package com.inventario.mobile.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthManager(private val context: Context) {
    
    /**
     * Verifica se biometria está disponível no dispositivo
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Autentica usando biometria
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Desbloqueio Biométrico",
        subtitle: String = "Use sua digital ou face para continuar",
        negativeButtonText: String = "Usar PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            })
        
        biometricPrompt.authenticate(promptInfo)
    }
}
```

---

## 🔢 **2. PinManager.kt**

```kotlin
package com.inventario.mobile.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class PinManager(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Salva PIN (criptografado)
     */
    fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        encryptedPrefs.edit().putString(KEY_PIN, hashedPin).apply()
    }
    
    /**
     * Valida PIN
     */
    fun validatePin(pin: String): Boolean {
        val savedHash = encryptedPrefs.getString(KEY_PIN, null) ?: return false
        return hashPin(pin) == savedHash
    }
    
    /**
     * Verifica se tem PIN configurado
     */
    fun hasPin(): Boolean {
        return encryptedPrefs.contains(KEY_PIN)
    }
    
    /**
     * Remove PIN
     */
    fun clearPin() {
        encryptedPrefs.edit().remove(KEY_PIN).apply()
    }
    
    /**
     * Hash do PIN (SHA-256)
     */
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    companion object {
        private const val KEY_PIN = "user_pin"
    }
}
```

---

## 🔑 **3. SessionManager.kt**

```kotlin
package com.inventario.mobile.security

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Verifica se está logado
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Salva sessão
     */
    fun saveSession(
        userId: Int,
        username: String,
        token: String,
        refreshToken: String? = null
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_TOKEN, token)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            putLong(KEY_LAST_ACCESS, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Atualiza último acesso
     */
    fun updateLastAccess() {
        prefs.edit().putLong(KEY_LAST_ACCESS, System.currentTimeMillis()).apply()
    }
    
    /**
     * Verifica se sessão expirou (7 dias offline)
     */
    fun isSessionExpired(): Boolean {
        val loginTime = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
        val currentTime = System.currentTimeMillis()
        val daysSinceLogin = TimeUnit.MILLISECONDS.toDays(currentTime - loginTime)
        return daysSinceLogin > MAX_OFFLINE_DAYS
    }
    
    /**
     * Verifica se deve bloquear por inatividade (5 minutos)
     */
    fun shouldLockByInactivity(): Boolean {
        val lastAccess = prefs.getLong(KEY_LAST_ACCESS, 0)
        val currentTime = System.currentTimeMillis()
        val minutesSinceAccess = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastAccess)
        return minutesSinceAccess > INACTIVITY_TIMEOUT_MINUTES
    }
    
    /**
     * Obtém dados do usuário
     */
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""
    fun getRefreshToken(): String = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
    
    /**
     * Limpa sessão
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    
    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_LAST_ACCESS = "last_access"
        
        private const val MAX_OFFLINE_DAYS = 7L
        private const val INACTIVITY_TIMEOUT_MINUTES = 5L
    }
}
```

