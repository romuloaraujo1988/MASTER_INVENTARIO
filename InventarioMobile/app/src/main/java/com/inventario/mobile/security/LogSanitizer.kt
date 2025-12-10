package com.inventario.mobile.security

import android.util.Log
import com.inventario.mobile.BuildConfig

/**
 * Sanitizador de logs para proteger dados sensiveis.
 * Remove ou ofusca tokens, senhas, emails e outros dados pessoais.
 * 
 * Feature: android-security-hardening
 * Validates: Requirements 3.1, 3.2, 3.3
 */
object LogSanitizer {
    
    private const val TAG = "LogSanitizer"
    private const val MASK = "***"
    
    // Regex para detectar tokens JWT (formato: xxx.xxx.xxx)
    private val JWT_REGEX = Regex("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+")
    
    // Regex para detectar emails
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    
    // Regex para detectar senhas em logs (password=xxx, senha=xxx, etc)
    private val PASSWORD_REGEX = Regex("(password|senha|pwd|pass)[=:][^\\s,}]+", RegexOption.IGNORE_CASE)
    
    // Regex para detectar tokens em logs (token=xxx, access_token=xxx, etc)
    private val TOKEN_REGEX = Regex("(token|access_token|refresh_token|bearer)[=:][^\\s,}]+", RegexOption.IGNORE_CASE)
    
    // Regex para detectar CPF (xxx.xxx.xxx-xx ou xxxxxxxxxxx)
    private val CPF_REGEX = Regex("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}")
    
    // Regex para detectar telefone
    private val PHONE_REGEX = Regex("\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}")
    
    /**
     * Sanitiza uma mensagem removendo dados sensiveis.
     * 
     * @param message Mensagem a sanitizar
     * @return Mensagem com dados sensiveis ofuscados
     */
    fun sanitize(message: String): String {
        var result = message
        
        // Ofuscar tokens JWT
        result = JWT_REGEX.replace(result) { "[JWT_TOKEN]" }
        
        // Ofuscar emails
        result = EMAIL_REGEX.replace(result) { match ->
            val email = match.value
            val atIndex = email.indexOf('@')
            if (atIndex > 2) {
                email.substring(0, 2) + MASK + email.substring(atIndex)
            } else {
                MASK + email.substring(atIndex)
            }
        }
        
        // Ofuscar senhas
        result = PASSWORD_REGEX.replace(result) { match ->
            val parts = match.value.split(Regex("[=:]"), 2)
            if (parts.size == 2) parts[0] + "=" + MASK else MASK
        }
        
        // Ofuscar tokens
        result = TOKEN_REGEX.replace(result) { match ->
            val parts = match.value.split(Regex("[=:]"), 2)
            if (parts.size == 2) parts[0] + "=" + MASK else MASK
        }
        
        // Ofuscar CPF
        result = CPF_REGEX.replace(result) { "[CPF]" }
        
        // Ofuscar telefone
        result = PHONE_REGEX.replace(result) { "[PHONE]" }
        
        return result
    }
    
    /**
     * Sanitiza um mapa de dados.
     * 
     * @param data Mapa com dados a sanitizar
     * @return Mapa com valores sensiveis ofuscados
     */
    fun sanitizeMap(data: Map<String, Any?>): Map<String, Any?> {
        val sensitiveKeys = setOf(
            "password", "senha", "pwd", "pass",
            "token", "access_token", "refresh_token", "jwt",
            "cpf", "email", "phone", "telefone",
            "secret", "key", "api_key", "apikey"
        )
        
        return data.mapValues { (key, value) ->
            if (sensitiveKeys.any { key.lowercase().contains(it) }) {
                MASK
            } else if (value is String) {
                sanitize(value)
            } else {
                value
            }
        }
    }
    
    /**
     * Verifica se e uma build de debug.
     * 
     * @return true se for debug build
     */
    fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }
    
    /**
     * Loga mensagem apenas em builds de debug.
     * Em release, nao loga nada.
     * 
     * @param tag Tag do log
     * @param message Mensagem a logar
     * @param level Nivel do log (DEBUG, INFO, WARN, ERROR)
     */
    fun log(tag: String, message: String, level: LogLevel = LogLevel.DEBUG) {
        if (!isDebugBuild()) return
        
        val sanitizedMessage = sanitize(message)
        
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, sanitizedMessage)
            LogLevel.INFO -> Log.i(tag, sanitizedMessage)
            LogLevel.WARN -> Log.w(tag, sanitizedMessage)
            LogLevel.ERROR -> Log.e(tag, sanitizedMessage)
        }
    }
    
    /**
     * Loga mensagem de debug sanitizada.
     */
    fun d(tag: String, message: String) = log(tag, message, LogLevel.DEBUG)
    
    /**
     * Loga mensagem de info sanitizada.
     */
    fun i(tag: String, message: String) = log(tag, message, LogLevel.INFO)
    
    /**
     * Loga mensagem de warning sanitizada.
     */
    fun w(tag: String, message: String) = log(tag, message, LogLevel.WARN)
    
    /**
     * Loga mensagem de erro sanitizada.
     */
    fun e(tag: String, message: String) = log(tag, message, LogLevel.ERROR)
}

/**
 * Niveis de log suportados.
 */
enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}