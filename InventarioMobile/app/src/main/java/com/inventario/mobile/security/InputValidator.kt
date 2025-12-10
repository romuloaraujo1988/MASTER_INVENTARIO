package com.inventario.mobile.security

import android.util.Patterns

object InputValidator {
    
    private const val MAX_TEXT_LENGTH = 255
    private const val MAX_PATRIMONIO_LENGTH = 10
    private const val MIN_PASSWORD_LENGTH = 8
    
    private val DANGEROUS_CHARS_REGEX = Regex("[<>]")
    private val PATRIMONIO_REGEX = Regex("^[0-9]{1,10}$")
    
    fun validatePatrimonioNumber(numero: String): ValidationResult {
        val trimmed = numero.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Numero obrigatorio")
            trimmed.length > MAX_PATRIMONIO_LENGTH -> ValidationResult.Invalid("Maximo 10 digitos")
            !trimmed.matches(PATRIMONIO_REGEX) -> ValidationResult.Invalid("Apenas digitos")
            else -> ValidationResult.Valid
        }
    }
    
    fun sanitizeText(input: String): String {
        return input.trim().replace(DANGEROUS_CHARS_REGEX, "")
    }
    
    fun truncateToMaxLength(input: String, maxLength: Int = MAX_TEXT_LENGTH): String {
        return if (input.length > maxLength) input.take(maxLength) else input
    }
    
    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Email obrigatorio")
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> ValidationResult.Invalid("Email invalido")
            else -> ValidationResult.Valid
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (password.length < MIN_PASSWORD_LENGTH) errors.add("Minimo 8 caracteres")
        if (!password.any { it.isUpperCase() }) errors.add("Uma maiuscula")
        if (!password.any { it.isDigit() }) errors.add("Um numero")
        if (!password.any { !it.isLetterOrDigit() }) errors.add("Um especial")
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors.joinToString(", "))
    }
    
    fun sanitizeAndTruncate(input: String, maxLength: Int = MAX_TEXT_LENGTH): String {
        return truncateToMaxLength(sanitizeText(input), maxLength)
    }
    
    fun isNumeric(input: String): Boolean = input.all { it.isDigit() }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    fun isValid(): Boolean = this is Valid
    fun getErrorMessage(): String? = (this as? Invalid)?.message
}