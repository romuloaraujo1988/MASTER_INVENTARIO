package com.inventario.mobile.security

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de tentativas de login para protecao contra forca bruta.
 * Bloqueia o login apos MAX_ATTEMPTS tentativas falhadas por LOCKOUT_DURATION minutos.
 * 
 * Feature: android-security-hardening
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5
 */
class LoginAttemptManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "login_attempts_prefs"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
        private const val KEY_LOCKOUT_END_TIME = "lockout_end_time"
        
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MINUTES = 15L
        
        @Volatile
        private var instance: LoginAttemptManager? = null
        
        fun getInstance(context: Context): LoginAttemptManager {
            return instance ?: synchronized(this) {
                instance ?: LoginAttemptManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Registra uma tentativa de login falhada.
     * Se atingir MAX_ATTEMPTS, inicia o periodo de bloqueio.
     */
    fun recordFailedAttempt() {
        val currentAttempts = getFailedAttemptCount() + 1
        val now = System.currentTimeMillis()
        
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, currentAttempts)
            .putLong(KEY_LAST_ATTEMPT_TIME, now)
            .apply()
        
        // Se atingiu o limite, iniciar bloqueio
        if (currentAttempts >= MAX_ATTEMPTS) {
            val lockoutEnd = now + TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES)
            prefs.edit()
                .putLong(KEY_LOCKOUT_END_TIME, lockoutEnd)
                .apply()
        }
    }
    
    /**
     * Registra um login bem-sucedido.
     * Reseta o contador de tentativas e remove o bloqueio.
     */
    fun recordSuccessfulLogin() {
        reset()
    }
    
    /**
     * Verifica se a conta esta bloqueada.
     * 
     * @return true se bloqueada, false caso contrario
     */
    fun isLocked(): Boolean {
        val lockoutEnd = prefs.getLong(KEY_LOCKOUT_END_TIME, 0L)
        val now = System.currentTimeMillis()
        
        if (lockoutEnd > 0 && now < lockoutEnd) {
            return true
        }
        
        // Se o bloqueio expirou, limpar
        if (lockoutEnd > 0 && now >= lockoutEnd) {
            reset()
        }
        
        return false
    }
    
    /**
     * Obtem o numero de tentativas falhadas.
     * 
     * @return Numero de tentativas falhadas
     */
    fun getFailedAttemptCount(): Int {
        return prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    }
    
    /**
     * Obtem o tempo restante de bloqueio em milissegundos.
     * 
     * @return Tempo restante em ms, ou 0 se nao bloqueado
     */
    fun getRemainingLockoutTime(): Long {
        val lockoutEnd = prefs.getLong(KEY_LOCKOUT_END_TIME, 0L)
        val now = System.currentTimeMillis()
        
        return if (lockoutEnd > now) {
            lockoutEnd - now
        } else {
            0L
        }
    }
    
    /**
     * Obtem o tempo restante de bloqueio formatado.
     * 
     * @return String formatada (ex: "14:30")
     */
    fun getRemainingLockoutTimeFormatted(): String {
        val remaining = getRemainingLockoutTime()
        if (remaining <= 0) return "0:00"
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
        
        return String.format("%d:%02d", minutes, seconds)
    }
    
    /**
     * Reseta o contador de tentativas e remove o bloqueio.
     */
    fun reset() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LAST_ATTEMPT_TIME, 0L)
            .putLong(KEY_LOCKOUT_END_TIME, 0L)
            .apply()
    }
    
    /**
     * Obtem o numero de tentativas restantes antes do bloqueio.
     * 
     * @return Numero de tentativas restantes
     */
    fun getRemainingAttempts(): Int {
        val failed = getFailedAttemptCount()
        return maxOf(0, MAX_ATTEMPTS - failed)
    }
    
    /**
     * Verifica se esta proximo do bloqueio (1 tentativa restante).
     * 
     * @return true se proxima tentativa causara bloqueio
     */
    fun isNearLockout(): Boolean {
        return getRemainingAttempts() == 1
    }
}