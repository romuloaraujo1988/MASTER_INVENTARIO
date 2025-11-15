package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.data.local.database.InventarioDatabase
import java.io.File

/**
 * Worker para limpeza automática do banco de dados
 * 
 * Remove:
 * - Coletas sincronizadas com mais de 30 dias
 * - Arquivos de cache antigos
 * - Imagens temporárias antigas
 */
class DatabaseCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "DatabaseCleanup"
        const val WORK_NAME = "database_cleanup"
        
        // 30 dias em milissegundos
        private const val RETENTION_PERIOD_MS = 30L * 24 * 60 * 60 * 1000
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Iniciando limpeza do banco de dados...")
            
            val database = InventarioDatabase.getDatabase(applicationContext)
            val thirtyDaysAgo = System.currentTimeMillis() - RETENTION_PERIOD_MS
            
            // Deletar coletas sincronizadas antigas
            val deletedColetas = database.coletaDao().limparSincronizadasAntigas(thirtyDaysAgo)
            Log.d(TAG, "Deletadas $deletedColetas coletas antigas")
            
            // Limpar cache de arquivos
            val deletedFiles = cleanOldCacheFiles(thirtyDaysAgo)
            Log.d(TAG, "Deletados $deletedFiles arquivos de cache antigos")
            
            // Limpar imagens temporárias
            val deletedImages = cleanOldTempImages(thirtyDaysAgo)
            Log.d(TAG, "Deletadas $deletedImages imagens temporárias antigas")
            
            Log.d(TAG, "Limpeza concluída com sucesso")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na limpeza do banco de dados", e)
            Result.failure()
        }
    }
    
    /**
     * Limpa arquivos de cache antigos
     */
    private fun cleanOldCacheFiles(timestamp: Long): Int {
        var deletedCount = 0
        val cacheDir = applicationContext.cacheDir
        
        cacheDir.listFiles()?.forEach { file ->
            if (file.lastModified() < timestamp) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        
        return deletedCount
    }
    
    /**
     * Limpa imagens temporárias antigas
     */
    private fun cleanOldTempImages(timestamp: Long): Int {
        var deletedCount = 0
        val tempDir = File(applicationContext.filesDir, "temp_images")
        
        if (tempDir.exists()) {
            tempDir.listFiles()?.forEach { file ->
                if (file.lastModified() < timestamp) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
        }
        
        return deletedCount
    }
}
