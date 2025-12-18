package com.inventario.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper otimizado para captura e compressão de fotos de patrimônios
 * 
 * Estratégia de otimização:
 * - Compressão agressiva: 800x600 max, JPEG 65%
 * - Resultado: ~50-100 KB por foto (vs 3-5 MB original)
 * - Armazenamento em arquivo (não BLOB no banco)
 * - Limpeza automática após sincronização
 * 
 * @author Sistema de Inventário v2.11
 */
@Singleton
class PhotoHelper @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "PhotoHelper"
        
        // Configurações de compressão otimizadas
        const val MAX_WIDTH = 800
        const val MAX_HEIGHT = 600
        const val JPEG_QUALITY = 65
        const val THUMBNAIL_SIZE = 200
        const val THUMBNAIL_QUALITY = 50
        
        // Tamanho máximo em bytes (~100KB)
        const val MAX_FILE_SIZE = 100 * 1024
        
        // Diretório de fotos
        private const val PHOTOS_DIR = "patrimonio_photos"
        private const val THUMBNAILS_DIR = "patrimonio_thumbnails"
        
        // Limite de fotos locais
        const val MAX_LOCAL_PHOTOS = 100
    }
    
    /**
     * Diretório para armazenar fotos
     */
    private val photosDir: File by lazy {
        File(context.filesDir, PHOTOS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Diretório para thumbnails
     */
    private val thumbnailsDir: File by lazy {
        File(context.filesDir, THUMBNAILS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Comprime e salva foto de patrimônio
     * 
     * @param sourceUri URI da foto original (da câmera)
     * @param patrimonioNumero Número do patrimônio para nomear arquivo
     * @return Caminho do arquivo salvo ou null se falhar
     */
    fun compressAndSavePhoto(sourceUri: Uri, patrimonioNumero: String): PhotoResult? {
        return try {
            Log.d(TAG, "Comprimindo foto para patrimônio: $patrimonioNumero")
            
            // Carregar bitmap com opções de memória otimizadas
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            
            // Calcular sample size para reduzir memória
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
            options.inJustDecodeBounds = false
            
            // Carregar bitmap reduzido
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: return null
            
            // Redimensionar para tamanho máximo
            val resizedBitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)
            
            // Gerar nome único
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "foto_${patrimonioNumero}_$timestamp.jpg"
            val photoFile = File(photosDir, fileName)
            
            // Comprimir e salvar
            var quality = JPEG_QUALITY
            var fileSize: Long
            
            do {
                FileOutputStream(photoFile).use { out ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                fileSize = photoFile.length()
                
                // Se ainda muito grande, reduzir qualidade
                if (fileSize > MAX_FILE_SIZE && quality > 30) {
                    quality -= 10
                    Log.d(TAG, "Reduzindo qualidade para $quality (tamanho: ${fileSize / 1024}KB)")
                }
            } while (fileSize > MAX_FILE_SIZE && quality > 30)
            
            // Criar thumbnail
            val thumbnailPath = createThumbnail(resizedBitmap, patrimonioNumero, timestamp)
            
            // Liberar memória
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            bitmap.recycle()
            
            Log.d(TAG, "✓ Foto salva: ${photoFile.absolutePath} (${fileSize / 1024}KB)")
            
            PhotoResult(
                fullPath = photoFile.absolutePath,
                thumbnailPath = thumbnailPath,
                sizeBytes = fileSize,
                width = MAX_WIDTH,
                height = MAX_HEIGHT
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao comprimir foto", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemory ao processar foto", e)
            null
        }
    }
    
    /**
     * Comprime bitmap diretamente (para fotos da câmera)
     */
    fun compressAndSaveBitmap(bitmap: Bitmap, patrimonioNumero: String): PhotoResult? {
        return try {
            val resizedBitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "foto_${patrimonioNumero}_$timestamp.jpg"
            val photoFile = File(photosDir, fileName)
            
            var quality = JPEG_QUALITY
            var fileSize: Long
            
            do {
                FileOutputStream(photoFile).use { out ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                fileSize = photoFile.length()
                
                if (fileSize > MAX_FILE_SIZE && quality > 30) {
                    quality -= 10
                }
            } while (fileSize > MAX_FILE_SIZE && quality > 30)
            
            val thumbnailPath = createThumbnail(resizedBitmap, patrimonioNumero, timestamp)
            
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            
            PhotoResult(
                fullPath = photoFile.absolutePath,
                thumbnailPath = thumbnailPath,
                sizeBytes = fileSize,
                width = MAX_WIDTH,
                height = MAX_HEIGHT
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao comprimir bitmap", e)
            null
        }
    }
    
    /**
     * Converte foto para Base64 (para sincronização)
     * Usa compressão adicional se necessário
     */
    fun photoToBase64(photoPath: String): String? {
        return try {
            val file = File(photoPath)
            if (!file.exists()) return null
            
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter para Base64", e)
            null
        }
    }
    
    /**
     * Salva Base64 como arquivo (para fotos recebidas do servidor)
     */
    fun base64ToPhoto(base64: String, patrimonioNumero: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "foto_${patrimonioNumero}_$timestamp.jpg"
            val photoFile = File(photosDir, fileName)
            
            FileOutputStream(photoFile).use { out ->
                out.write(bytes)
            }
            
            photoFile.absolutePath
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar Base64", e)
            null
        }
    }
    
    /**
     * Carrega thumbnail para exibição rápida
     */
    fun loadThumbnail(thumbnailPath: String?): Bitmap? {
        if (thumbnailPath == null) return null
        
        return try {
            val file = File(thumbnailPath)
            if (!file.exists()) return null
            
            BitmapFactory.decodeFile(thumbnailPath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar thumbnail", e)
            null
        }
    }
    
    /**
     * Deleta foto e thumbnail
     */
    fun deletePhoto(photoPath: String?) {
        if (photoPath == null) return
        
        try {
            val photoFile = File(photoPath)
            if (photoFile.exists()) {
                photoFile.delete()
                Log.d(TAG, "Foto deletada: $photoPath")
            }
            
            // Deletar thumbnail correspondente
            val thumbnailName = photoFile.name.replace("foto_", "thumb_")
            val thumbnailFile = File(thumbnailsDir, thumbnailName)
            if (thumbnailFile.exists()) {
                thumbnailFile.delete()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar foto", e)
        }
    }
    
    /**
     * Limpa fotos antigas (manter apenas últimas MAX_LOCAL_PHOTOS)
     */
    fun cleanupOldPhotos() {
        try {
            val photos = photosDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
            
            if (photos.size > MAX_LOCAL_PHOTOS) {
                val toDelete = photos.drop(MAX_LOCAL_PHOTOS)
                toDelete.forEach { file ->
                    file.delete()
                    Log.d(TAG, "Foto antiga removida: ${file.name}")
                }
                
                // Limpar thumbnails órfãos
                cleanupOrphanThumbnails()
                
                Log.d(TAG, "✓ Limpeza concluída: ${toDelete.size} fotos removidas")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na limpeza de fotos", e)
        }
    }
    
    /**
     * Obtém estatísticas de uso de espaço
     */
    fun getStorageStats(): PhotoStorageStats {
        val photos = photosDir.listFiles() ?: emptyArray()
        val thumbnails = thumbnailsDir.listFiles() ?: emptyArray()
        
        val photosSize = photos.sumOf { it.length() }
        val thumbnailsSize = thumbnails.sumOf { it.length() }
        
        return PhotoStorageStats(
            photoCount = photos.size,
            thumbnailCount = thumbnails.size,
            totalSizeBytes = photosSize + thumbnailsSize,
            photosSizeBytes = photosSize,
            thumbnailsSizeBytes = thumbnailsSize
        )
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private fun createThumbnail(bitmap: Bitmap, patrimonioNumero: String, timestamp: String): String? {
        return try {
            val thumbnail = resizeBitmap(bitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            
            val fileName = "thumb_${patrimonioNumero}_$timestamp.jpg"
            val thumbnailFile = File(thumbnailsDir, fileName)
            
            FileOutputStream(thumbnailFile).use { out ->
                thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, out)
            }
            
            if (thumbnail != bitmap) {
                thumbnail.recycle()
            }
            
            thumbnailFile.absolutePath
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar thumbnail", e)
            null
        }
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
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
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun cleanupOrphanThumbnails() {
        val photoNames = photosDir.listFiles()?.map { it.name.replace("foto_", "") }?.toSet() ?: return
        val thumbnails = thumbnailsDir.listFiles() ?: return
        
        thumbnails.forEach { thumb ->
            val baseName = thumb.name.replace("thumb_", "")
            if (baseName !in photoNames) {
                thumb.delete()
            }
        }
    }
}

/**
 * Resultado da compressão de foto
 */
data class PhotoResult(
    val fullPath: String,
    val thumbnailPath: String?,
    val sizeBytes: Long,
    val width: Int,
    val height: Int
) {
    val sizeKB: Long get() = sizeBytes / 1024
}

/**
 * Estatísticas de armazenamento de fotos
 */
data class PhotoStorageStats(
    val photoCount: Int,
    val thumbnailCount: Int,
    val totalSizeBytes: Long,
    val photosSizeBytes: Long,
    val thumbnailsSizeBytes: Long
) {
    val totalSizeMB: Double get() = totalSizeBytes / (1024.0 * 1024.0)
    val photosSizeMB: Double get() = photosSizeBytes / (1024.0 * 1024.0)
}
