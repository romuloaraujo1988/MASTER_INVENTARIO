package com.inventario.mobile.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utilitários para validação de dados
 */
object ValidationUtils {
    
    // Padrões de validação
    private val CODIGO_PATRIMONIO_PATTERN = Pattern.compile("^[A-Z0-9]{6,20}$")
    private val NUMERO_SERIE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{3,50}$")
    private val TELEFONE_PATTERN = Pattern.compile("^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$")
    private val CPF_PATTERN = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$")
    private val CNPJ_PATTERN = Pattern.compile("^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$")
    
    /**
     * Valida se string não é nula nem vazia
     */
    fun isNotEmpty(value: String?): Boolean {
        return !value.isNullOrBlank()
    }
    
    /**
     * Valida se string tem tamanho mínimo
     */
    fun hasMinLength(value: String?, minLength: Int): Boolean {
        return value != null && value.length >= minLength
    }
    
    /**
     * Valida se string tem tamanho máximo
     */
    fun hasMaxLength(value: String?, maxLength: Int): Boolean {
        return value == null || value.length <= maxLength
    }
    
    /**
     * Valida se string está dentro do range de tamanho
     */
    fun hasValidLength(value: String?, minLength: Int, maxLength: Int): Boolean {
        return hasMinLength(value, minLength) && hasMaxLength(value, maxLength)
    }
    
    /**
     * Valida email
     */
    fun isValidEmail(email: String?): Boolean {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Valida URL
     */
    fun isValidUrl(url: String?): Boolean {
        return url != null && Patterns.WEB_URL.matcher(url).matches()
    }
    
    /**
     * Valida telefone brasileiro
     */
    fun isValidPhone(phone: String?): Boolean {
        return phone != null && TELEFONE_PATTERN.matcher(phone.replace("\\s", "")).matches()
    }
    
    /**
     * Valida CPF
     */
    fun isValidCPF(cpf: String?): Boolean {
        if (cpf == null) return false
        
        val cleanCpf = cpf.replace("[^\\d]".toRegex(), "")
        
        // Verifica se tem 11 dígitos
        if (cleanCpf.length != 11) return false
        
        // Verifica se todos os dígitos são iguais
        if (cleanCpf.all { it == cleanCpf[0] }) return false
        
        // Valida primeiro dígito verificador
        var sum = 0
        for (i in 0..8) {
            sum += (cleanCpf[i].toString().toInt() * (10 - i))
        }
        var remainder = sum % 11
        val firstDigit = if (remainder < 2) 0 else 11 - remainder
        
        if (cleanCpf[9].toString().toInt() != firstDigit) return false
        
        // Valida segundo dígito verificador
        sum = 0
        for (i in 0..9) {
            sum += (cleanCpf[i].toString().toInt() * (11 - i))
        }
        remainder = sum % 11
        val secondDigit = if (remainder < 2) 0 else 11 - remainder
        
        return cleanCpf[10].toString().toInt() == secondDigit
    }
    
    /**
     * Valida CNPJ
     */
    fun isValidCNPJ(cnpj: String?): Boolean {
        if (cnpj == null) return false
        
        val cleanCnpj = cnpj.replace("[^\\d]".toRegex(), "")
        
        // Verifica se tem 14 dígitos
        if (cleanCnpj.length != 14) return false
        
        // Verifica se todos os dígitos são iguais
        if (cleanCnpj.all { it == cleanCnpj[0] }) return false
        
        // Valida primeiro dígito verificador
        val weights1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        var sum = 0
        for (i in 0..11) {
            sum += cleanCnpj[i].toString().toInt() * weights1[i]
        }
        var remainder = sum % 11
        val firstDigit = if (remainder < 2) 0 else 11 - remainder
        
        if (cleanCnpj[12].toString().toInt() != firstDigit) return false
        
        // Valida segundo dígito verificador
        val weights2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        sum = 0
        for (i in 0..12) {
            sum += cleanCnpj[i].toString().toInt() * weights2[i]
        }
        remainder = sum % 11
        val secondDigit = if (remainder < 2) 0 else 11 - remainder
        
        return cleanCnpj[13].toString().toInt() == secondDigit
    }
    
    /**
     * Valida código de patrimônio
     */
    fun isValidPatrimonioCodigo(codigo: String?): Boolean {
        return codigo != null && CODIGO_PATRIMONIO_PATTERN.matcher(codigo).matches()
    }
    
    /**
     * Valida número de série
     */
    fun isValidNumeroSerie(numeroSerie: String?): Boolean {
        return numeroSerie != null && NUMERO_SERIE_PATTERN.matcher(numeroSerie).matches()
    }
    
    /**
     * Valida valor monetário
     */
    fun isValidMonetaryValue(value: String?): Boolean {
        if (value == null) return false
        
        return try {
            val cleanValue = value.replace("[^\\d,.-]".toRegex(), "")
                .replace(",", ".")
            val doubleValue = cleanValue.toDouble()
            doubleValue >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Valida número inteiro positivo
     */
    fun isValidPositiveInteger(value: String?): Boolean {
        if (value == null) return false
        
        return try {
            val intValue = value.toInt()
            intValue > 0
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Valida número decimal positivo
     */
    fun isValidPositiveDecimal(value: String?): Boolean {
        if (value == null) return false
        
        return try {
            val doubleValue = value.replace(",", ".").toDouble()
            doubleValue > 0
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Valida coordenada de latitude
     */
    fun isValidLatitude(latitude: Double?): Boolean {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0
    }
    
    /**
     * Valida coordenada de longitude
     */
    fun isValidLongitude(longitude: Double?): Boolean {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0
    }
    
    /**
     * Valida coordenadas GPS
     */
    fun isValidGpsCoordinates(latitude: Double?, longitude: Double?): Boolean {
        return isValidLatitude(latitude) && isValidLongitude(longitude)
    }
    
    /**
     * Valida senha
     */
    fun isValidPassword(password: String?): Boolean {
        if (password == null) return false
        
        // Mínimo 6 caracteres
        if (password.length < 6) return false
        
        // Pelo menos uma letra
        if (!password.any { it.isLetter() }) return false
        
        // Pelo menos um número
        if (!password.any { it.isDigit() }) return false
        
        return true
    }
    
    /**
     * Valida senha forte
     */
    fun isStrongPassword(password: String?): Boolean {
        if (password == null) return false
        
        // Mínimo 8 caracteres
        if (password.length < 8) return false
        
        // Pelo menos uma letra minúscula
        if (!password.any { it.isLowerCase() }) return false
        
        // Pelo menos uma letra maiúscula
        if (!password.any { it.isUpperCase() }) return false
        
        // Pelo menos um número
        if (!password.any { it.isDigit() }) return false
        
        // Pelo menos um caractere especial
        if (!password.any { !it.isLetterOrDigit() }) return false
        
        return true
    }
    
    /**
     * Valida nome de usuário
     */
    fun isValidUsername(username: String?): Boolean {
        if (username == null) return false
        
        // Entre 3 e 30 caracteres
        if (username.length < 3 || username.length > 30) return false
        
        // Apenas letras, números, underscore e hífen
        val pattern = Pattern.compile("^[a-zA-Z0-9_-]+$")
        return pattern.matcher(username).matches()
    }
    
    /**
     * Valida nome completo
     */
    fun isValidFullName(name: String?): Boolean {
        if (name == null) return false
        
        // Pelo menos 2 caracteres
        if (name.trim().length < 2) return false
        
        // Pelo menos um espaço (nome e sobrenome)
        if (!name.trim().contains(" ")) return false
        
        // Apenas letras, espaços e alguns caracteres especiais
        val pattern = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]+$")
        return pattern.matcher(name.trim()).matches()
    }
    
    /**
     * Valida código QR
     */
    fun isValidQRCode(qrCode: String?): Boolean {
        return isNotEmpty(qrCode) && hasValidLength(qrCode, 1, 500)
    }
    
    /**
     * Valida ID (número positivo)
     */
    fun isValidId(id: Long?): Boolean {
        return id != null && id > 0
    }
    
    /**
     * Valida lista não vazia
     */
    fun <T> isNotEmptyList(list: List<T>?): Boolean {
        return list != null && list.isNotEmpty()
    }
    
    /**
     * Valida data no formato string
     */
    fun isValidDateString(dateString: String?, format: String = "dd/MM/yyyy"): Boolean {
        if (dateString == null) return false
        return DateUtils.isValidDate(dateString, format)
    }
    
    /**
     * Valida extensão de arquivo
     */
    fun isValidFileExtension(fileName: String?, allowedExtensions: List<String>): Boolean {
        if (fileName == null) return false
        
        val extension = FileUtils.getFileExtension(fileName).lowercase()
        return extension in allowedExtensions.map { it.lowercase() }
    }
    
    /**
     * Valida tamanho de arquivo
     */
    fun isValidFileSize(fileSizeBytes: Long, maxSizeMB: Int): Boolean {
        val maxSizeBytes = maxSizeMB * 1024 * 1024L
        return fileSizeBytes <= maxSizeBytes
    }
    
    /**
     * Valida versão do app
     */
    fun isValidAppVersion(version: String?): Boolean {
        if (version == null) return false
        
        val pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+$")
        return pattern.matcher(version).matches()
    }
    
    /**
     * Valida token JWT (formato básico)
     */
    fun isValidJwtToken(token: String?): Boolean {
        if (token == null) return false
        
        val parts = token.split(".")
        return parts.size == 3 && parts.all { it.isNotEmpty() }
    }
    
    /**
     * Valida IP address
     */
    fun isValidIpAddress(ip: String?): Boolean {
        return ip != null && Patterns.IP_ADDRESS.matcher(ip).matches()
    }
    
    /**
     * Valida porta de rede
     */
    fun isValidPort(port: Int?): Boolean {
        return port != null && port in 1..65535
    }
    
    /**
     * Valida MAC address
     */
    fun isValidMacAddress(mac: String?): Boolean {
        if (mac == null) return false
        
        val pattern = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        return pattern.matcher(mac).matches()
    }
    
    /**
     * Valida UUID
     */
    fun isValidUUID(uuid: String?): Boolean {
        if (uuid == null) return false
        
        val pattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        )
        return pattern.matcher(uuid).matches()
    }
    
    /**
     * Valida código de barras
     */
    fun isValidBarcode(barcode: String?): Boolean {
        if (barcode == null) return false
        
        // Aceita códigos de 8, 12, 13 ou 14 dígitos
        val pattern = Pattern.compile("^\\d{8}$|^\\d{12}$|^\\d{13}$|^\\d{14}$")
        return pattern.matcher(barcode).matches()
    }
    
    /**
     * Valida código de cores hexadecimal
     */
    fun isValidHexColor(color: String?): Boolean {
        if (color == null) return false
        
        val pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
        return pattern.matcher(color).matches()
    }
    
    /**
     * Valida range numérico
     */
    fun isInRange(value: Int?, min: Int, max: Int): Boolean {
        return value != null && value in min..max
    }
    
    /**
     * Valida range decimal
     */
    fun isInRange(value: Double?, min: Double, max: Double): Boolean {
        return value != null && value in min..max
    }
    
    /**
     * Resultado de validação com mensagem
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * Valida patrimônio completo
     */
    fun validatePatrimonio(
        codigo: String?,
        descricao: String?,
        valor: String?
    ): ValidationResult {
        when {
            !isValidPatrimonioCodigo(codigo) -> {
                return ValidationResult(false, "Código de patrimônio inválido")
            }
            !hasValidLength(descricao, 3, 200) -> {
                return ValidationResult(false, "Descrição deve ter entre 3 e 200 caracteres")
            }
            !isValidMonetaryValue(valor) -> {
                return ValidationResult(false, "Valor monetário inválido")
            }
            else -> return ValidationResult(true)
        }
    }
    
    /**
     * Valida dados de usuário
     */
    fun validateUser(
        nome: String?,
        email: String?,
        login: String?
    ): ValidationResult {
        when {
            !hasValidLength(nome, 2, 100) -> {
                return ValidationResult(false, "Nome deve ter entre 2 e 100 caracteres")
            }
            !isValidEmail(email) -> {
                return ValidationResult(false, "Email inválido")
            }
            !isValidUsername(login) -> {
                return ValidationResult(false, "Login inválido")
            }
            else -> return ValidationResult(true)
        }
    }
    
    /**
     * Valida dados de login
     */
    fun validateLogin(
        username: String?,
        password: String?
    ): ValidationResult {
        when {
            !isNotEmpty(username) -> {
                return ValidationResult(false, "Usuário é obrigatório")
            }
            !isNotEmpty(password) -> {
                return ValidationResult(false, "Senha é obrigatória")
            }
            else -> return ValidationResult(true)
        }
    }
}
