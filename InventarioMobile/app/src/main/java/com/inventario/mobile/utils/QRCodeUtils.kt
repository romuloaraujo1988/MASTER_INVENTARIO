package com.inventario.mobile.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*

/**
 * Utilitários para geração e leitura de QR Codes
 */
object QRCodeUtils {
    
    /**
     * Gera QR Code como Bitmap
     */
    fun generateQRCode(
        content: String,
        width: Int = 512,
        height: Int = 512,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }
            
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gera QR Code colorido
     */
    fun generateColoredQRCode(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }
            
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decodifica QR Code de um Bitmap
     */
    fun decodeQRCode(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decodifica QR Code com hints personalizados
     */
    fun decodeQRCodeWithHints(bitmap: Bitmap, hints: Map<DecodeHintType, Any>): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap, hints)
            result.text
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gera QR Code para patrimônio
     */
    fun generatePatrimonioQRCode(
        patrimonioId: Long,
        codigo: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        val qrContent = createPatrimonioQRContent(patrimonioId, codigo)
        return generateQRCode(qrContent, width, height)
    }
    
    /**
     * Cria conteúdo do QR Code para patrimônio
     */
    fun createPatrimonioQRContent(patrimonioId: Long, codigo: String): String {
        return "PATRIMONIO:$patrimonioId:$codigo"
    }
    
    /**
     * Decodifica QR Code de patrimônio
     */
    fun decodePatrimonioQRCode(qrContent: String): PatrimonioQRData? {
        return try {
            if (qrContent.startsWith("PATRIMONIO:")) {
                val parts = qrContent.split(":")
                if (parts.size >= 3) {
                    val patrimonioId = parts[1].toLongOrNull()
                    val codigo = parts[2]
                    if (patrimonioId != null) {
                        PatrimonioQRData(patrimonioId, codigo)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                // Tenta interpretar como código simples
                PatrimonioQRData(0, qrContent)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Valida se o conteúdo é um QR Code de patrimônio válido
     */
    fun isValidPatrimonioQRCode(qrContent: String): Boolean {
        return decodePatrimonioQRCode(qrContent) != null
    }
    
    /**
     * Gera QR Code para URL
     */
    fun generateUrlQRCode(
        url: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return if (ValidationUtils.isValidUrl(url)) {
            generateQRCode(url, width, height)
        } else {
            null
        }
    }
    
    /**
     * Gera QR Code para texto
     */
    fun generateTextQRCode(
        text: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return if (text.isNotBlank()) {
            generateQRCode(text, width, height)
        } else {
            null
        }
    }
    
    /**
     * Gera QR Code para contato (vCard)
     */
    fun generateContactQRCode(
        name: String,
        phone: String? = null,
        email: String? = null,
        organization: String? = null,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        val vCard = buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            phone?.let { appendLine("TEL:$it") }
            email?.let { appendLine("EMAIL:$it") }
            organization?.let { appendLine("ORG:$it") }
            appendLine("END:VCARD")
        }
        
        return generateQRCode(vCard, width, height)
    }
    
    /**
     * Gera QR Code para WiFi
     */
    fun generateWiFiQRCode(
        ssid: String,
        password: String,
        security: String = "WPA", // WPA, WEP, nopass
        hidden: Boolean = false,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        val wifiString = "WIFI:T:$security;S:$ssid;P:$password;H:${if (hidden) "true" else "false"};;"
        return generateQRCode(wifiString, width, height)
    }
    
    /**
     * Gera QR Code para localização
     */
    fun generateLocationQRCode(
        latitude: Double,
        longitude: Double,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        val locationString = "geo:$latitude,$longitude"
        return generateQRCode(locationString, width, height)
    }
    
    /**
     * Gera QR Code para SMS
     */
    fun generateSMSQRCode(
        phoneNumber: String,
        message: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        val smsString = "smsto:$phoneNumber:$message"
        return generateQRCode(smsString, width, height)
    }
    
    /**
     * Gera QR Code para email
     */
    fun generateEmailQRCode(
        email: String,
        subject: String? = null,
        body: String? = null,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        val emailString = buildString {
            append("mailto:$email")
            if (!subject.isNullOrBlank() || !body.isNullOrBlank()) {
                append("?")
                if (!subject.isNullOrBlank()) {
                    append("subject=${java.net.URLEncoder.encode(subject, "UTF-8")}")
                }
                if (!body.isNullOrBlank()) {
                    if (!subject.isNullOrBlank()) append("&")
                    append("body=${java.net.URLEncoder.encode(body, "UTF-8")}")
                }
            }
        }
        
        return generateQRCode(emailString, width, height)
    }
    
    /**
     * Detecta o tipo de conteúdo do QR Code
     */
    fun detectQRCodeType(content: String): QRCodeType {
        return when {
            content.startsWith("PATRIMONIO:") -> QRCodeType.PATRIMONIO
            content.startsWith("http://") || content.startsWith("https://") -> QRCodeType.URL
            content.startsWith("mailto:") -> QRCodeType.EMAIL
            content.startsWith("tel:") -> QRCodeType.PHONE
            content.startsWith("smsto:") -> QRCodeType.SMS
            content.startsWith("WIFI:") -> QRCodeType.WIFI
            content.startsWith("geo:") -> QRCodeType.LOCATION
            content.startsWith("BEGIN:VCARD") -> QRCodeType.CONTACT
            ValidationUtils.isValidUrl(content) -> QRCodeType.URL
            ValidationUtils.isValidEmail(content) -> QRCodeType.EMAIL
            ValidationUtils.isValidPhone(content) -> QRCodeType.PHONE
            else -> QRCodeType.TEXT
        }
    }
    
    /**
     * Obtém informações legíveis do QR Code
     */
    fun getQRCodeInfo(content: String): QRCodeInfo {
        val type = detectQRCodeType(content)
        val description = when (type) {
            QRCodeType.PATRIMONIO -> {
                val data = decodePatrimonioQRCode(content)
                "Patrimônio: ${data?.codigo ?: "Código inválido"}"
            }
            QRCodeType.URL -> "URL: $content"
            QRCodeType.EMAIL -> "Email: ${content.removePrefix("mailto:").split("?")[0]}"
            QRCodeType.PHONE -> "Telefone: ${content.removePrefix("tel:")}"
            QRCodeType.SMS -> {
                val parts = content.removePrefix("smsto:").split(":")
                "SMS para: ${parts.getOrNull(0) ?: ""}"
            }
            QRCodeType.WIFI -> "Configuração WiFi"
            QRCodeType.LOCATION -> "Localização GPS"
            QRCodeType.CONTACT -> "Contato (vCard)"
            QRCodeType.TEXT -> "Texto: ${content.take(50)}${if (content.length > 50) "..." else ""}"
        }
        
        return QRCodeInfo(type, content, description)
    }
    
    /**
     * Valida tamanho do conteúdo para QR Code
     */
    fun isValidQRCodeSize(content: String, errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M): Boolean {
        val maxCapacity = when (errorCorrectionLevel) {
            ErrorCorrectionLevel.L -> 2953 // ~7%
            ErrorCorrectionLevel.M -> 2331 // ~15%
            ErrorCorrectionLevel.Q -> 1663 // ~25%
            ErrorCorrectionLevel.H -> 1273 // ~30%
        }
        
        return content.toByteArray(Charsets.UTF_8).size <= maxCapacity
    }
    
    /**
     * Otimiza conteúdo para QR Code
     */
    fun optimizeQRCodeContent(content: String, maxLength: Int = 2000): String {
        return if (content.length > maxLength) {
            content.take(maxLength - 3) + "..."
        } else {
            content
        }
    }
    
    /**
     * Calcula densidade ideal para QR Code
     */
    fun calculateOptimalSize(content: String, minSize: Int = 200, maxSize: Int = 800): Int {
        val contentLength = content.length
        return when {
            contentLength < 50 -> minSize
            contentLength < 200 -> (minSize * 1.5).toInt()
            contentLength < 500 -> (minSize * 2).toInt()
            contentLength < 1000 -> (minSize * 2.5).toInt()
            else -> maxSize
        }.coerceIn(minSize, maxSize)
    }
    
    /**
     * Data class para dados de patrimônio do QR Code
     */
    data class PatrimonioQRData(
        val patrimonioId: Long,
        val codigo: String
    )
    
    /**
     * Data class para informações do QR Code
     */
    data class QRCodeInfo(
        val type: QRCodeType,
        val content: String,
        val description: String
    )
    
    /**
     * Enum para tipos de QR Code
     */
    enum class QRCodeType {
        PATRIMONIO,
        URL,
        EMAIL,
        PHONE,
        SMS,
        WIFI,
        LOCATION,
        CONTACT,
        TEXT
    }
    
    /**
     * Configurações para geração de QR Code
     */
    data class QRCodeConfig(
        val width: Int = 512,
        val height: Int = 512,
        val errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
        val foregroundColor: Int = Color.BLACK,
        val backgroundColor: Int = Color.WHITE,
        val margin: Int = 1
    )
    
    /**
     * Gera QR Code com configurações personalizadas
     */
    fun generateQRCodeWithConfig(content: String, config: QRCodeConfig): Bitmap? {
        return generateColoredQRCode(
            content = content,
            width = config.width,
            height = config.height,
            foregroundColor = config.foregroundColor,
            backgroundColor = config.backgroundColor,
            errorCorrectionLevel = config.errorCorrectionLevel
        )
    }
    
    /**
     * Salva QR Code como arquivo
     */
    fun saveQRCodeToFile(
        content: String,
        filePath: String,
        config: QRCodeConfig = QRCodeConfig()
    ): Boolean {
        val bitmap = generateQRCodeWithConfig(content, config)
        return if (bitmap != null) {
            FileUtils.saveBitmapAsJpeg(bitmap, java.io.File(filePath), 90)
        } else {
            false
        }
    }
}