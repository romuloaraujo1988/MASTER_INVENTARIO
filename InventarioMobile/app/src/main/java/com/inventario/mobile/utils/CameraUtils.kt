package com.inventario.mobile.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitários para funcionalidades de câmera e captura de fotos
 */
object CameraUtils {
    
    private const val JPEG_QUALITY_HIGH = 90
    private const val JPEG_QUALITY_MEDIUM = 70
    private const val JPEG_QUALITY_LOW = 50
    
    private const val MAX_IMAGE_SIZE_HIGH = 1920
    private const val MAX_IMAGE_SIZE_MEDIUM = 1280
    private const val MAX_IMAGE_SIZE_LOW = 640
    
    /**
     * Verifica se o dispositivo tem câmera
     */
    fun hasCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    
    /**
     * Verifica se o dispositivo tem câmera frontal
     */
    fun hasFrontCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }
    
    /**
     * Verifica se o dispositivo tem flash
     */
    fun hasFlash(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }
    
    /**
     * Verifica se tem permissão de câmera
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Verifica se pode usar a câmera
     */
    fun canUseCamera(context: Context): Boolean {
        return try {
            val hasBasicCamera = hasCamera(context)
            val hasPermission = hasCameraPermission(context)
            val hasCameraIntent = isCameraIntentAvailable(context)
            
            android.util.Log.d("CameraUtils", "Camera check - hasBasicCamera: $hasBasicCamera, hasPermission: $hasPermission, hasCameraIntent: $hasCameraIntent")
            
            // Verificação adicional: tentar acessar o CameraManager
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            val cameraManagerAvailable = cameraManager?.let { manager ->
                try {
                    val cameraIds = manager.cameraIdList
                    android.util.Log.d("CameraUtils", "CameraManager found ${cameraIds.size} cameras")
                    cameraIds.isNotEmpty()
                } catch (e: Exception) {
                    android.util.Log.e("CameraUtils", "Error accessing CameraManager", e)
                    false
                }
            } ?: false
            
            val result = hasBasicCamera && hasPermission && hasCameraIntent && cameraManagerAvailable
            android.util.Log.d("CameraUtils", "Final camera availability result: $result")
            
            result
        } catch (e: Exception) {
            android.util.Log.e("CameraUtils", "Error checking camera availability", e)
            false
        }
    }
    
    /**
     * Cria arquivo temporário para foto
     */
    fun createImageFile(context: Context, prefix: String = "IMG"): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "${prefix}_${timeStamp}_"
        val storageDir = FileUtils.getPhotosDirectory(context)
        
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }
    
    /**
     * Cria URI para arquivo de foto usando FileProvider
     */
    fun createImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * Cria intent para captura de foto
     */
    fun createCameraIntent(context: Context, outputFile: File): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoUri = createImageUri(context, outputFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        return intent
    }
    
    /**
     * Verifica se existe app de câmera disponível
     */
    fun isCameraIntentAvailable(context: Context): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return intent.resolveActivity(context.packageManager) != null
    }
    
    /**
     * Obtém orientação da imagem a partir do EXIF
     */
    fun getImageOrientation(imagePath: String): Int {
        return try {
            val exif = ExifInterface(imagePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            0
        }
    }
    
    /**
     * Rotaciona bitmap baseado na orientação EXIF
     */
    fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        if (orientation == 0) return bitmap
        
        val matrix = Matrix()
        matrix.postRotate(orientation.toFloat())
        
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }
    
    /**
     * Corrige orientação da imagem
     */
    fun correctImageOrientation(imagePath: String): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeFile(imagePath) ?: return null
            val orientation = getImageOrientation(imagePath)
            rotateBitmap(bitmap, orientation)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Redimensiona bitmap mantendo proporção
     */
    fun resizeBitmap(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int,
        filter: Boolean = true
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val ratio = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return try {
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, filter)
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }
    
    /**
     * Comprime e salva bitmap como JPEG
     */
    fun compressBitmap(
        bitmap: Bitmap,
        outputFile: File,
        quality: Int = JPEG_QUALITY_MEDIUM
    ): Boolean {
        return try {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Processa imagem capturada (corrige orientação, redimensiona e comprime)
     */
    fun processImage(
        inputPath: String,
        outputPath: String,
        quality: ImageQuality = ImageQuality.MEDIUM
    ): Boolean {
        return try {
            // Carrega e corrige orientação
            val bitmap = correctImageOrientation(inputPath) ?: return false
            
            // Redimensiona baseado na qualidade
            val maxSize = when (quality) {
                ImageQuality.HIGH -> MAX_IMAGE_SIZE_HIGH
                ImageQuality.MEDIUM -> MAX_IMAGE_SIZE_MEDIUM
                ImageQuality.LOW -> MAX_IMAGE_SIZE_LOW
            }
            
            val resizedBitmap = resizeBitmap(bitmap, maxSize, maxSize)
            
            // Comprime e salva
            val jpegQuality = when (quality) {
                ImageQuality.HIGH -> JPEG_QUALITY_HIGH
                ImageQuality.MEDIUM -> JPEG_QUALITY_MEDIUM
                ImageQuality.LOW -> JPEG_QUALITY_LOW
            }
            
            val result = compressBitmap(resizedBitmap, File(outputPath), jpegQuality)
            
            // Libera memória
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            bitmap.recycle()
            
            result
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtém informações da câmera
     */
    fun getCameraInfo(context: Context): List<CameraInfo> {
        val cameraInfoList = mutableListOf<CameraInfo>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                
                for (cameraId in cameraManager.cameraIdList) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    val facingString = when (facing) {
                        CameraCharacteristics.LENS_FACING_FRONT -> "Frontal"
                        CameraCharacteristics.LENS_FACING_BACK -> "Traseira"
                        CameraCharacteristics.LENS_FACING_EXTERNAL -> "Externa"
                        else -> "Desconhecida"
                    }
                    
                    val supportedSizes = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                    )?.getOutputSizes(android.graphics.ImageFormat.JPEG) ?: emptyArray()
                    
                    val maxSize = supportedSizes.maxByOrNull { it.width * it.height }
                    
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    
                    cameraInfoList.add(
                        CameraInfo(
                            id = cameraId,
                            facing = facingString,
                            maxResolution = maxSize,
                            hasFlash = hasFlash,
                            supportedSizes = supportedSizes.toList()
                        )
                    )
                }
            } catch (e: CameraAccessException) {
                // Erro ao acessar câmera
            }
        }
        
        return cameraInfoList
    }
    
    /**
     * Obtém tamanho recomendado para preview
     */
    fun getOptimalPreviewSize(
        supportedSizes: List<Size>,
        targetWidth: Int,
        targetHeight: Int
    ): Size? {
        val targetRatio = targetWidth.toDouble() / targetHeight
        
        return supportedSizes
            .filter { size ->
                val ratio = size.width.toDouble() / size.height
                kotlin.math.abs(ratio - targetRatio) < 0.1
            }
            .minByOrNull { size ->
                kotlin.math.abs(size.width - targetWidth) + kotlin.math.abs(size.height - targetHeight)
            }
            ?: supportedSizes.minByOrNull { size ->
                kotlin.math.abs(size.width - targetWidth) + kotlin.math.abs(size.height - targetHeight)
            }
    }
    
    /**
     * Calcula tamanho da imagem em bytes
     */
    fun calculateImageSize(width: Int, height: Int, quality: ImageQuality): Long {
        val pixels = width * height
        val bytesPerPixel = when (quality) {
            ImageQuality.HIGH -> 3.0
            ImageQuality.MEDIUM -> 2.0
            ImageQuality.LOW -> 1.0
        }
        return (pixels * bytesPerPixel).toLong()
    }
    
    /**
     * Verifica se há espaço suficiente para salvar a imagem
     */
    fun hasEnoughSpace(
        context: Context,
        width: Int,
        height: Int,
        quality: ImageQuality
    ): Boolean {
        val requiredSpace = calculateImageSize(width, height, quality)
        val availableSpace = FileUtils.getAvailableSpace(context)
        return availableSpace > requiredSpace * 2 // Margem de segurança
    }
    
    /**
     * Cria nome único para arquivo de foto
     */
    fun generatePhotoFileName(
        prefix: String = "foto",
        patrimonioId: Long? = null,
        includeTimestamp: Boolean = true
    ): String {
        val timestamp = if (includeTimestamp) {
            "_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
        } else {
            ""
        }
        
        val patrimonioSuffix = patrimonioId?.let { "_pat_$it" } ?: ""
        
        return "${prefix}${patrimonioSuffix}${timestamp}.jpg"
    }
    
    /**
     * Obtém metadados da imagem
     */
    fun getImageMetadata(imagePath: String): ImageMetadata? {
        return try {
            val file = File(imagePath)
            if (!file.exists()) return null
            
            val exif = ExifInterface(imagePath)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            ImageMetadata(
                width = options.outWidth,
                height = options.outHeight,
                size = file.length(),
                orientation = getImageOrientation(imagePath),
                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME),
                latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.toDoubleOrNull(),
                longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)?.toDoubleOrNull(),
                make = exif.getAttribute(ExifInterface.TAG_MAKE),
                model = exif.getAttribute(ExifInterface.TAG_MODEL),
                flash = exif.getAttribute(ExifInterface.TAG_FLASH),
                focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
                iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Remove metadados EXIF da imagem (para privacidade)
     */
    fun removeExifData(imagePath: String): Boolean {
        return try {
            val exif = ExifInterface(imagePath)
            
            // Remove dados de localização
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, null)
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null)
            
            // Remove dados do dispositivo
            exif.setAttribute(ExifInterface.TAG_MAKE, null)
            exif.setAttribute(ExifInterface.TAG_MODEL, null)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, null)
            
            exif.saveAttributes()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Adiciona marca d'água à imagem
     */
    fun addWatermark(
        originalBitmap: Bitmap,
        watermarkText: String,
        textSize: Float = 24f,
        alpha: Int = 128
    ): Bitmap {
        val result = originalBitmap.copy(originalBitmap.config, true)
        val canvas = android.graphics.Canvas(result)
        
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            this.textSize = textSize
            this.alpha = alpha
            isAntiAlias = true
            setShadowLayer(2f, 2f, 2f, android.graphics.Color.BLACK)
        }
        
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(watermarkText, 0, watermarkText.length, textBounds)
        
        val x = result.width - textBounds.width() - 20f
        val y = result.height - 20f
        
        canvas.drawText(watermarkText, x, y, paint)
        
        return result
    }
    
    /**
     * Enums e Data Classes
     */
    enum class ImageQuality {
        HIGH, MEDIUM, LOW
    }
    
    data class CameraInfo(
        val id: String,
        val facing: String,
        val maxResolution: Size?,
        val hasFlash: Boolean,
        val supportedSizes: List<Size>
    )
    
    data class ImageMetadata(
        val width: Int,
        val height: Int,
        val size: Long,
        val orientation: Int,
        val dateTime: String?,
        val latitude: Double?,
        val longitude: Double?,
        val make: String?,
        val model: String?,
        val flash: String?,
        val focalLength: String?,
        val iso: String?
    ) {
        fun hasLocation(): Boolean = latitude != null && longitude != null
        
        fun getResolutionString(): String = "${width}x${height}"
        
        fun getSizeString(): String = FileUtils.getReadableFileSize(size)
        
        fun getOrientationString(): String = when (orientation) {
            0 -> "Normal"
            90 -> "Rotacionada 90°"
            180 -> "Rotacionada 180°"
            270 -> "Rotacionada 270°"
            else -> "Desconhecida"
        }
    }
    
    data class PhotoCaptureResult(
        val success: Boolean,
        val filePath: String?,
        val error: String?
    )
    
    /**
     * Configurações para captura de foto
     */
    data class PhotoCaptureConfig(
        val quality: ImageQuality = ImageQuality.MEDIUM,
        val addWatermark: Boolean = false,
        val watermarkText: String = "",
        val removeExif: Boolean = true,
        val maxWidth: Int = 0,
        val maxHeight: Int = 0
    )
    
    /**
     * Processa foto capturada com configurações específicas
     */
    fun processPhotoWithConfig(
        inputPath: String,
        outputPath: String,
        config: PhotoCaptureConfig
    ): PhotoCaptureResult {
        return try {
            // Carrega e corrige orientação
            var bitmap = correctImageOrientation(inputPath)
                ?: return PhotoCaptureResult(false, null, "Erro ao carregar imagem")
            
            // Redimensiona se especificado
            if (config.maxWidth > 0 && config.maxHeight > 0) {
                val resized = resizeBitmap(bitmap, config.maxWidth, config.maxHeight)
                if (resized != bitmap) {
                    bitmap.recycle()
                    bitmap = resized
                }
            } else {
                // Redimensiona baseado na qualidade
                val maxSize = when (config.quality) {
                    ImageQuality.HIGH -> MAX_IMAGE_SIZE_HIGH
                    ImageQuality.MEDIUM -> MAX_IMAGE_SIZE_MEDIUM
                    ImageQuality.LOW -> MAX_IMAGE_SIZE_LOW
                }
                val resized = resizeBitmap(bitmap, maxSize, maxSize)
                if (resized != bitmap) {
                    bitmap.recycle()
                    bitmap = resized
                }
            }
            
            // Adiciona marca d'água se solicitado
            if (config.addWatermark && config.watermarkText.isNotEmpty()) {
                val watermarked = addWatermark(bitmap, config.watermarkText)
                bitmap.recycle()
                bitmap = watermarked
            }
            
            // Comprime e salva
            val jpegQuality = when (config.quality) {
                ImageQuality.HIGH -> JPEG_QUALITY_HIGH
                ImageQuality.MEDIUM -> JPEG_QUALITY_MEDIUM
                ImageQuality.LOW -> JPEG_QUALITY_LOW
            }
            
            val saved = compressBitmap(bitmap, File(outputPath), jpegQuality)
            bitmap.recycle()
            
            if (!saved) {
                return PhotoCaptureResult(false, null, "Erro ao salvar imagem")
            }
            
            // Remove dados EXIF se solicitado
            if (config.removeExif) {
                removeExifData(outputPath)
            }
            
            PhotoCaptureResult(true, outputPath, null)
            
        } catch (e: Exception) {
            PhotoCaptureResult(false, null, "Erro no processamento: ${e.message}")
        }
    }
}
