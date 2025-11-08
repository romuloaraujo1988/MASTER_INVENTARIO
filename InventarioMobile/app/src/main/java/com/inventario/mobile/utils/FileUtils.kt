package com.inventario.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.text.DecimalFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utilitários para manipulação de arquivos
 */
object FileUtils {
    
    private const val TAG = "FileUtils"
    private const val PHOTOS_DIR = "Inventario/Photos"
    private const val BACKUP_DIR = "Inventario/Backup"
    private const val TEMP_DIR = "Inventario/Temp"
    private const val LOGS_DIR = "Inventario/Logs"
    
    /**
     * Obtém diretório de fotos do app
     */
    fun getPhotosDirectory(context: Context): File {
        val photosDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Inventario")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        return photosDir
    }
    
    /**
     * Obtém diretório de backup
     */
    fun getBackupDirectory(context: Context): File {
        val backupDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Backup")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }
    
    /**
     * Obtém diretório temporário
     */
    fun getTempDirectory(context: Context): File {
        val tempDir = File(context.cacheDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return tempDir
    }
    
    /**
     * Obtém diretório de logs
     */
    fun getLogsDirectory(context: Context): File {
        val logsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Logs")
        if (!logsDir.exists()) {
            logsDir.mkdirs()
        }
        return logsDir
    }
    
    /**
     * Gera nome único para arquivo de foto
     */
    fun generatePhotoFileName(patrimonioId: Long? = null): String {
        val timestamp = DateUtils.formatForFileName(Date())
        return if (patrimonioId != null) {
            "patrimonio_${patrimonioId}_${timestamp}.jpg"
        } else {
            "foto_${timestamp}.jpg"
        }
    }
    
    /**
     * Gera nome único para arquivo de backup
     */
    fun generateBackupFileName(): String {
        val timestamp = DateUtils.formatForFileName(Date())
        return "backup_inventario_${timestamp}.db"
    }
    
    /**
     * Salva bitmap como arquivo JPEG
     */
    fun saveBitmapAsJpeg(bitmap: Bitmap, file: File, quality: Int = 85): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar bitmap: ${e.message}", e)
            false
        }
    }
    
    /**
     * Carrega bitmap de arquivo
     */
    fun loadBitmapFromFile(file: File): Bitmap? {
        return try {
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar bitmap: ${e.message}", e)
            null
        }
    }
    
    /**
     * Redimensiona bitmap mantendo proporção
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
    
    /**
     * Corrige orientação da imagem baseada no EXIF
     */
    fun correctImageOrientation(bitmap: Bitmap, imagePath: String): Bitmap {
        return try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }
            
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao corrigir orientação: ${e.message}", e)
            bitmap
        }
    }
    
    /**
     * Comprime imagem para tamanho específico
     */
    fun compressImage(inputPath: String, outputPath: String, maxSizeKB: Int): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputPath) ?: return false
            val correctedBitmap = correctImageOrientation(bitmap, inputPath)
            
            var quality = 100
            val outputFile = File(outputPath)
            
            do {
                val stream = ByteArrayOutputStream()
                correctedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                val sizeKB = stream.size() / 1024
                
                if (sizeKB <= maxSizeKB || quality <= 10) {
                    FileOutputStream(outputFile).use { fos ->
                        fos.write(stream.toByteArray())
                    }
                    break
                }
                
                quality -= 10
            } while (true)
            
            if (bitmap != correctedBitmap) {
                bitmap.recycle()
            }
            correctedBitmap.recycle()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao comprimir imagem: ${e.message}", e)
            false
        }
    }
    
    /**
     * Obtém tamanho do arquivo em formato legível
     */
    fun getReadableFileSize(file: File): String {
        return getReadableFileSize(file.length())
    }
    
    /**
     * Converte bytes para formato legível
     */
    fun getReadableFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        
        return DecimalFormat("#,##0.#")
            .format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
    
    /**
     * Copia arquivo
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            destination.parentFile?.mkdirs()
            
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao copiar arquivo: ${e.message}", e)
            false
        }
    }
    
    /**
     * Move arquivo
     */
    fun moveFile(source: File, destination: File): Boolean {
        return try {
            if (copyFile(source, destination)) {
                source.delete()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mover arquivo: ${e.message}", e)
            false
        }
    }
    
    /**
     * Deleta arquivo ou diretório recursivamente
     */
    fun deleteRecursively(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    deleteRecursively(child)
                }
            }
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar arquivo: ${e.message}", e)
            false
        }
    }
    
    /**
     * Limpa diretório temporário
     */
    fun clearTempDirectory(context: Context): Boolean {
        val tempDir = getTempDirectory(context)
        return deleteRecursively(tempDir) && tempDir.mkdirs()
    }
    
    /**
     * Limpa cache de imagens antigas
     */
    fun clearOldImages(context: Context, daysOld: Int = 30): Int {
        val photosDir = getPhotosDirectory(context)
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        var deletedCount = 0
        
        photosDir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoffTime) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        
        return deletedCount
    }
    
    /**
     * Obtém espaço livre em disco
     */
    fun getAvailableSpace(context: Context): Long {
        return context.getExternalFilesDir(null)?.freeSpace ?: 0L
    }
    
    /**
     * Verifica se há espaço suficiente
     */
    fun hasEnoughSpace(context: Context, requiredBytes: Long): Boolean {
        return getAvailableSpace(context) > requiredBytes
    }
    
    /**
     * Obtém extensão do arquivo
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }
    
    /**
     * Obtém nome do arquivo sem extensão
     */
    fun getFileNameWithoutExtension(fileName: String): String {
        return fileName.substringBeforeLast('.')
    }
    
    /**
     * Verifica se arquivo é imagem
     */
    fun isImageFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
    
    /**
     * Obtém MIME type do arquivo
     */
    fun getMimeType(fileName: String): String {
        return when (getFileExtension(fileName).lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "zip" -> "application/zip"
            "db" -> "application/x-sqlite3"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Cria arquivo de log
     */
    fun writeLog(context: Context, tag: String, message: String) {
        try {
            val logsDir = getLogsDirectory(context)
            val logFile = File(logsDir, "${tag}_${DateUtils.formatDateForApi(Date())}.log")
            
            val timestamp = DateUtils.formatDateTime(Date())
            val logEntry = "[$timestamp] $message\n"
            
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao escrever log: ${e.message}", e)
        }
    }
    
    /**
     * Lê conteúdo de arquivo texto
     */
    fun readTextFile(file: File): String? {
        return try {
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao ler arquivo: ${e.message}", e)
            null
        }
    }
    
    /**
     * Escreve conteúdo em arquivo texto
     */
    fun writeTextFile(file: File, content: String): Boolean {
        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao escrever arquivo: ${e.message}", e)
            false
        }
    }
    
    /**
     * Obtém URI do arquivo para compartilhamento
     */
    fun getFileUri(context: Context, file: File): Uri? {
        return try {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter URI do arquivo: ${e.message}", e)
            null
        }
    }
    
    /**
     * Lista arquivos em diretório com filtro
     */
    fun listFiles(
        directory: File,
        extension: String? = null,
        recursive: Boolean = false
    ): List<File> {
        val files = mutableListOf<File>()
        
        if (!directory.exists() || !directory.isDirectory) {
            return files
        }
        
        directory.listFiles()?.forEach { file ->
            when {
                file.isFile -> {
                    if (extension == null || getFileExtension(file.name).equals(extension, true)) {
                        files.add(file)
                    }
                }
                file.isDirectory && recursive -> {
                    files.addAll(listFiles(file, extension, recursive))
                }
            }
        }
        
        return files.sortedBy { it.name }
    }
    
    /**
     * Calcula tamanho total de diretório
     */
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
        
        return size
    }
    
    /**
     * Verifica se arquivo está sendo usado
     */
    fun isFileInUse(file: File): Boolean {
        return try {
            if (!file.exists()) return false
            
            FileInputStream(file).use {
                // Se conseguir abrir, não está em uso
                false
            }
        } catch (e: Exception) {
            // Se não conseguir abrir, provavelmente está em uso
            true
        }
    }
    
    /**
     * Cria arquivo temporário único
     */
    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        val tempDir = getTempDirectory(context)
        return File.createTempFile(prefix, suffix, tempDir)
    }
    
    /**
     * Valida nome de arquivo
     */
    fun isValidFileName(fileName: String): Boolean {
        if (fileName.isBlank()) return false
        
        val invalidChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        return !fileName.any { it in invalidChars }
    }
    
    /**
     * Sanitiza nome de arquivo
     */
    fun sanitizeFileName(fileName: String): String {
        val invalidChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        return fileName.map { if (it in invalidChars) '_' else it }.joinToString("")
    }
}