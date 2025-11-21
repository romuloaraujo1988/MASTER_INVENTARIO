package com.inventario.mobile.security

import android.content.Context
import android.util.Log
import com.inventario.mobile.utils.PreferencesManager
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Gerenciador de autenticação por PIN
 * Permite login offline usando PIN de 4 dígitos
 */
class PinAuthManager(private val context: Context) {
    
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val LOCK_DURATION_MS = 30 * 60 * 1000L // 30 minutos
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256
    }
    
    /**
     * Verifica se PIN está habilitado
     */
    fun isPinEnabled(): Boolean {
        return preferencesManager.getBoolean("pin_enabled", false)
    }
    
    /**
     * Cria um novo PIN
     */
    fun createPin(pin: String): Boolean {
        return try {
            // Validar PIN
            if (!isValidPin(pin)) {
                Log.w("PinAuthManager", "PIN inválido")
                return false
            }
            
            // Gerar salt aleatório
            val salt = generateSalt()
            
            // Hash do PIN
            val hashedPin = hashPin(pin, salt)
            
            // Salvar
            preferencesManager.putString("pin_hash", hashedPin)
            preferencesManager.putString("pin_salt", salt)
            preferencesManager.putBoolean("pin_enabled", true)
            preferencesManager.putInt("pin_attempts", MAX_ATTEMPTS)
            
            Log.d("PinAuthManager", "PIN criado com sucesso")
            true
        } catch (e: Exception) {
            Log.e("PinAuthManager", "Erro ao criar PIN", e)
            false
        }
    }
    
    /**
     * Valida PIN
     */
    fun validatePin(pin: String): Boolean {
        return try {
            // Verificar se conta está bloqueada
            if (isAccountLocked()) {
                Log.w("PinAuthManager", "Conta bloqueada")
                return false
            }
            
            // Obter hash e salt salvos
            val savedHash = preferencesManager.getString("pin_hash", "")
            val salt = preferencesManager.getString("pin_salt", "")
            
            if (savedHash.isEmpty() || salt.isEmpty()) {
                Log.w("PinAuthManager", "PIN não configurado")
                return false
            }
            
            // Hash do PIN digitado
            val inputHash = hashPin(pin, salt)
            
            // Comparar
            if (inputHash == savedHash) {
                // PIN correto - resetar tentativas
                preferencesManager.putInt("pin_attempts", MAX_ATTEMPTS)
                preferencesManager.remove("pin_lock_until")
                Log.d("PinAuthManager", "PIN validado com sucesso")
                return true
            } else {
                // PIN incorreto - decrementar tentativas
                val attempts = getRemainingAttempts() - 1
                preferencesManager.putInt("pin_attempts", attempts)
                
                if (attempts <= 0) {
                    lockAccount()
                }
                
                Log.w("PinAuthManager", "PIN incorreto. Tentativas restantes: $attempts")
                return false
            }
        } catch (e: Exception) {
            Log.e("PinAuthManager", "Erro ao validar PIN", e)
            false
        }
    }
    
    /**
     * Altera PIN
     */
    fun changePin(oldPin: String, newPin: String): Boolean {
        return try {
            // Validar PIN antigo
            if (!validatePin(oldPin)) {
                Log.w("PinAuthManager", "PIN antigo incorreto")
                return false
            }
            
            // Criar novo PIN
            createPin(newPin)
        } catch (e: Exception) {
            Log.e("PinAuthManager", "Erro ao alterar PIN", e)
            false
        }
    }
    
    /**
     * Remove PIN
     */
    fun clearPin() {
        preferencesManager.remove("pin_hash")
        preferencesManager.remove("pin_salt")
        preferencesManager.putBoolean("pin_enabled", false)
        preferencesManager.putInt("pin_attempts", MAX_ATTEMPTS)
        preferencesManager.remove("pin_lock_until")
        Log.d("PinAuthManager", "PIN removido")
    }
    
    /**
     * Obtém tentativas restantes
     */
    fun getRemainingAttempts(): Int {
        return preferencesManager.getInt("pin_attempts", MAX_ATTEMPTS)
    }
    
    /**
     * Bloqueia conta temporariamente
     */
    fun lockAccount() {
        val lockUntil = System.currentTimeMillis() + LOCK_DURATION_MS
        preferencesManager.putLong("pin_lock_until", lockUntil)
        Log.w("PinAuthManager", "Conta bloqueada até: $lockUntil")
    }
    
    /**
     * Verifica se conta está bloqueada
     */
    fun isAccountLocked(): Boolean {
        val lockUntil = preferencesManager.getLong("pin_lock_until", 0L)
        val now = System.currentTimeMillis()
        
        if (lockUntil > now) {
            return true
        } else if (lockUntil > 0) {
            // Desbloqueou - resetar tentativas
            preferencesManager.remove("pin_lock_until")
            preferencesManager.putInt("pin_attempts", MAX_ATTEMPTS)
        }
        
        return false
    }
    
    /**
     * Obtém tempo restante de bloqueio (em minutos)
     */
    fun getLockTimeRemaining(): Int {
        val lockUntil = preferencesManager.getLong("pin_lock_until", 0L)
        val now = System.currentTimeMillis()
        val remaining = lockUntil - now
        
        return if (remaining > 0) {
            (remaining / 60000).toInt() + 1 // minutos
        } else {
            0
        }
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    private fun isValidPin(pin: String): Boolean {
        // PIN deve ter exatamente 4 dígitos
        return pin.length == 4 && pin.all { it.isDigit() }
    }
    
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }
    
    private fun hashPin(pin: String, salt: String): String {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            Base64.getDecoder().decode(salt),
            ITERATIONS,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }
}
