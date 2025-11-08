package com.inventario.mobile.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utilitários para funcionalidades de segurança e criptografia
 */
object SecurityUtils {
    
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val KEY_ALIAS_PREFIX = "inventario_key_"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    private const val PBKDF2_ITERATIONS = 10000
    private const val SALT_LENGTH = 16
    
    /**
     * Gera chave AES no Android Keystore
     */
    fun generateAESKey(alias: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    "$KEY_ALIAS_PREFIX$alias",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
                
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtém chave AES do Android Keystore
     */
    private fun getAESKey(alias: String): SecretKey? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                keyStore.getKey("$KEY_ALIAS_PREFIX$alias", null) as? SecretKey
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Criptografa dados usando AES-GCM
     */
    fun encryptAES(data: String, keyAlias: String): EncryptionResult? {
        return try {
            val key = getAESKey(keyAlias) ?: return null
            
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            
            EncryptionResult(
                encryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT),
                iv = Base64.encodeToString(iv, Base64.DEFAULT)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Descriptografa dados usando AES-GCM
     */
    fun decryptAES(encryptedData: String, iv: String, keyAlias: String): String? {
        return try {
            val key = getAESKey(keyAlias) ?: return null
            
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, Base64.decode(iv, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            
            val decryptedData = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            String(decryptedData, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Criptografia AES simples com senha
     */
    fun encryptWithPassword(data: String, password: String): PasswordEncryptionResult? {
        return try {
            val salt = generateRandomBytes(SALT_LENGTH)
            val key = deriveKeyFromPassword(password, salt)
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            
            PasswordEncryptionResult(
                encryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT),
                salt = Base64.encodeToString(salt, Base64.DEFAULT),
                iv = Base64.encodeToString(iv, Base64.DEFAULT)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Descriptografia AES com senha
     */
    fun decryptWithPassword(
        encryptedData: String,
        password: String,
        salt: String,
        iv: String
    ): String? {
        return try {
            val saltBytes = Base64.decode(salt, Base64.DEFAULT)
            val key = deriveKeyFromPassword(password, saltBytes)
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            
            val decryptedData = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            String(decryptedData, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Deriva chave a partir de senha usando PBKDF2
     */
    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
    
    /**
     * Gera hash SHA-256
     */
    fun generateSHA256Hash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.DEFAULT).trim()
    }
    
    /**
     * Gera hash SHA-256 com salt
     */
    fun generateSHA256HashWithSalt(data: String, salt: String): String {
        return generateSHA256Hash(data + salt)
    }
    
    /**
     * Gera hash MD5 (para compatibilidade)
     */
    fun generateMD5Hash(data: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(data.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Gera bytes aleatórios
     */
    fun generateRandomBytes(length: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }
    
    /**
     * Gera string aleatória
     */
    fun generateRandomString(length: Int, includeSpecialChars: Boolean = false): String {
        val chars = if (includeSpecialChars) {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"
        } else {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        }
        
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * Gera UUID seguro
     */
    fun generateSecureUUID(): String {
        return UUID.randomUUID().toString()
    }
    
    /**
     * Gera token de sessão
     */
    fun generateSessionToken(): String {
        val randomBytes = generateRandomBytes(32)
        return Base64.encodeToString(randomBytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }
    
    /**
     * Valida força da senha
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        val issues = mutableListOf<String>()
        
        // Comprimento
        when {
            password.length < 6 -> issues.add("Muito curta (mínimo 6 caracteres)")
            password.length >= 8 -> score += 1
            password.length >= 12 -> score += 1
        }
        
        // Letras minúsculas
        if (password.any { it.isLowerCase() }) {
            score += 1
        } else {
            issues.add("Deve conter letras minúsculas")
        }
        
        // Letras maiúsculas
        if (password.any { it.isUpperCase() }) {
            score += 1
        } else {
            issues.add("Deve conter letras maiúsculas")
        }
        
        // Números
        if (password.any { it.isDigit() }) {
            score += 1
        } else {
            issues.add("Deve conter números")
        }
        
        // Caracteres especiais
        if (password.any { !it.isLetterOrDigit() }) {
            score += 1
        } else {
            issues.add("Deve conter caracteres especiais")
        }
        
        // Padrões comuns
        val commonPatterns = listOf(
            "123456", "password", "123456789", "12345678", "12345",
            "1234567", "admin", "qwerty", "abc123", "password123"
        )
        
        if (commonPatterns.any { password.lowercase().contains(it) }) {
            score -= 2
            issues.add("Contém padrões comuns")
        }
        
        val strength = when {
            score < 2 -> PasswordStrengthLevel.WEAK
            score < 4 -> PasswordStrengthLevel.MODERATE
            score < 6 -> PasswordStrengthLevel.STRONG
            else -> PasswordStrengthLevel.VERY_STRONG
        }
        
        return PasswordStrength(strength, score, issues)
    }
    
    /**
     * Gera senha segura
     */
    fun generateSecurePassword(
        length: Int = 12,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = true
    ): String {
        var chars = ""
        
        if (includeUppercase) chars += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        if (includeLowercase) chars += "abcdefghijklmnopqrstuvwxyz"
        if (includeNumbers) chars += "0123456789"
        if (includeSpecialChars) chars += "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        if (chars.isEmpty()) return ""
        
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * Verifica integridade de dados usando checksum
     */
    fun calculateChecksum(data: String): String {
        return generateSHA256Hash(data)
    }
    
    /**
     * Verifica integridade de dados
     */
    fun verifyIntegrity(data: String, expectedChecksum: String): Boolean {
        return calculateChecksum(data) == expectedChecksum
    }
    
    /**
     * Sanitiza string para prevenir injeção
     */
    fun sanitizeString(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }
    
    /**
     * Remove caracteres perigosos
     */
    fun removeDangerousChars(input: String): String {
        return input.replace(Regex("[<>\"'&/\\]"), "")
    }
    
    /**
     * Valida token JWT (básico)
     */
    fun isValidJWTFormat(token: String): Boolean {
        val parts = token.split(".")
        return parts.size == 3 && parts.all { it.isNotEmpty() }
    }
    
    /**
     * Extrai payload do JWT (sem validação de assinatura)
     */
    fun extractJWTPayload(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Funcionalidades de biometria
     */
    object BiometricUtils {
        
        fun isBiometricAvailable(context: Context): Boolean {
            val biometricManager = BiometricManager.from(context)
            return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> true
                else -> false
            }
        }
        
        fun getBiometricStatus(context: Context): BiometricStatus {
            val biometricManager = BiometricManager.from(context)
            return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
                else -> BiometricStatus.UNKNOWN_ERROR
            }
        }
        
        fun createBiometricPrompt(
            activity: FragmentActivity,
            title: String,
            subtitle: String,
            description: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ): BiometricPrompt {
            val executor = ContextCompat.getMainExecutor(activity)
            
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }
                    
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Autenticação falhou")
                    }
                })
            
            return biometricPrompt
        }
        
        fun createPromptInfo(
            title: String,
            subtitle: String,
            description: String,
            negativeButtonText: String = "Cancelar"
        ): BiometricPrompt.PromptInfo {
            return BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .build()
        }
    }
    
    /**
     * Enums e Data Classes
     */
    enum class PasswordStrengthLevel {
        WEAK, MODERATE, STRONG, VERY_STRONG
    }
    
    enum class BiometricStatus {
        AVAILABLE, NO_HARDWARE, HARDWARE_UNAVAILABLE, NONE_ENROLLED, UNKNOWN_ERROR
    }
    
    data class EncryptionResult(
        val encryptedData: String,
        val iv: String
    )
    
    data class PasswordEncryptionResult(
        val encryptedData: String,
        val salt: String,
        val iv: String
    )
    
    data class PasswordStrength(
        val level: PasswordStrengthLevel,
        val score: Int,
        val issues: List<String>
    ) {
        fun isAcceptable(): Boolean = level != PasswordStrengthLevel.WEAK
        
        fun getLevelString(): String {
            return when (level) {
                PasswordStrengthLevel.WEAK -> "Fraca"
                PasswordStrengthLevel.MODERATE -> "Moderada"
                PasswordStrengthLevel.STRONG -> "Forte"
                PasswordStrengthLevel.VERY_STRONG -> "Muito Forte"
            }
        }
        
        fun getLevelColor(): Int {
            return when (level) {
                PasswordStrengthLevel.WEAK -> android.graphics.Color.RED
                PasswordStrengthLevel.MODERATE -> android.graphics.Color.parseColor("#FF9800")
                PasswordStrengthLevel.STRONG -> android.graphics.Color.parseColor("#4CAF50")
                PasswordStrengthLevel.VERY_STRONG -> android.graphics.Color.parseColor("#2E7D32")
            }
        }
    }
    
    /**
     * Utilitários para assinatura digital
     */
    object DigitalSignature {
        
        fun generateKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            return keyPairGenerator.generateKeyPair()
        }
        
        fun signData(data: String, privateKey: PrivateKey): String? {
            return try {
                val signature = Signature.getInstance("SHA256withRSA")
                signature.initSign(privateKey)
                signature.update(data.toByteArray(StandardCharsets.UTF_8))
                val signatureBytes = signature.sign()
                Base64.encodeToString(signatureBytes, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        }
        
        fun verifySignature(data: String, signature: String, publicKey: PublicKey): Boolean {
            return try {
                val sig = Signature.getInstance("SHA256withRSA")
                sig.initVerify(publicKey)
                sig.update(data.toByteArray(StandardCharsets.UTF_8))
                val signatureBytes = Base64.decode(signature, Base64.DEFAULT)
                sig.verify(signatureBytes)
            } catch (e: Exception) {
                false
            }
        }
        
        fun keyToString(key: Key): String {
            return Base64.encodeToString(key.encoded, Base64.DEFAULT)
        }
        
        fun stringToPublicKey(keyString: String): PublicKey? {
            return try {
                val keyBytes = Base64.decode(keyString, Base64.DEFAULT)
                val keySpec = X509EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePublic(keySpec)
            } catch (e: Exception) {
                null
            }
        }
        
        fun stringToPrivateKey(keyString: String): PrivateKey? {
            return try {
                val keyBytes = Base64.decode(keyString, Base64.DEFAULT)
                val keySpec = PKCS8EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePrivate(keySpec)
            } catch (e: Exception) {
                null
            }
        }
    }
}