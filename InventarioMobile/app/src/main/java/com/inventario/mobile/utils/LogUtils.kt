package com.inventario.mobile.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Utilitários para funcionalidades de logging e depuração
 */
object LogUtils {
    
    private const val TAG_PREFIX = "InventarioApp"
    private const val LOG_FILE_NAME = "app_logs.txt"
    private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    private const val MAX_LOG_FILES = 3
    private const val LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private var isLoggingEnabled = true
    private var logLevel = LogLevel.DEBUG
    private var fileLoggingEnabled = false
    
    /**
     * Níveis de log
     */
    enum class LogLevel(val priority: Int) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT)
    }
    
    /**
     * Entrada de log
     */
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null,
        val threadName: String = Thread.currentThread().name
    )
    
    /**
     * Configurações de log
     */
    data class LogConfig(
        val enabled: Boolean = true,
        val level: LogLevel = LogLevel.DEBUG,
        val fileLoggingEnabled: Boolean = false,
        val maxFileSize: Long = MAX_LOG_FILE_SIZE.toLong(),
        val maxFiles: Int = MAX_LOG_FILES,
        val includeStackTrace: Boolean = true,
        val includeThreadName: Boolean = true
    )
    
    /**
     * Inicializa o sistema de logging
     */
    fun initialize(config: LogConfig = LogConfig()) {
        isLoggingEnabled = config.enabled
        logLevel = config.level
        fileLoggingEnabled = config.fileLoggingEnabled
    }
    
    /**
     * Habilita ou desabilita o logging
     */
    fun setLoggingEnabled(enabled: Boolean) {
        isLoggingEnabled = enabled
    }
    
    /**
     * Define o nível mínimo de log
     */
    fun setLogLevel(level: LogLevel) {
        logLevel = level
    }
    
    /**
     * Habilita ou desabilita o logging em arquivo
     */
    fun setFileLoggingEnabled(enabled: Boolean) {
        fileLoggingEnabled = enabled
    }
    
    /**
     * Log verbose
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.VERBOSE, tag, message, throwable)
    }
    
    /**
     * Log debug
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, message, throwable)
    }
    
    /**
     * Log info
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, message, throwable)
    }
    
    /**
     * Log warning
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, tag, message, throwable)
    }
    
    /**
     * Log error
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
    
    /**
     * Log assert
     */
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ASSERT, tag, message, throwable)
    }
    
    /**
     * Log com nível específico
     */
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (!isLoggingEnabled || level.priority < logLevel.priority) {
            return
        }
        
        val fullTag = "$TAG_PREFIX:$tag"
        val fullMessage = buildString {
            append(message)
            if (throwable != null) {
                append("\n")
                append(getStackTraceString(throwable))
            }
        }
        
        // Log no Logcat
        when (level) {
            LogLevel.VERBOSE -> Log.v(fullTag, fullMessage)
            LogLevel.DEBUG -> Log.d(fullTag, fullMessage)
            LogLevel.INFO -> Log.i(fullTag, fullMessage)
            LogLevel.WARN -> Log.w(fullTag, fullMessage)
            LogLevel.ERROR -> Log.e(fullTag, fullMessage)
            LogLevel.ASSERT -> Log.wtf(fullTag, fullMessage)
        }
        
        // Adicionar à fila para logging em arquivo
        if (fileLoggingEnabled) {
            val logEntry = LogEntry(
                timestamp = System.currentTimeMillis(),
                level = level,
                tag = fullTag,
                message = fullMessage,
                throwable = throwable
            )
            logQueue.offer(logEntry)
        }
    }
    
    /**
     * Obtém stack trace como string
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    /**
     * Escreve logs em arquivo
     */
    suspend fun flushLogsToFile(context: Context) = withContext(Dispatchers.IO) {
        if (!fileLoggingEnabled || logQueue.isEmpty()) {
            return@withContext
        }
        
        try {
            val logFile = getLogFile(context)
            val dateFormat = SimpleDateFormat(LOG_DATE_FORMAT, Locale.getDefault())
            
            logFile.appendText(buildString {
                while (logQueue.isNotEmpty()) {
                    val entry = logQueue.poll() ?: break
                    val timestamp = dateFormat.format(Date(entry.timestamp))
                    append("[$timestamp] [${entry.level.name}] [${entry.threadName}] ${entry.tag}: ${entry.message}\n")
                }
            })
            
            // Verificar tamanho do arquivo e rotacionar se necessário
            if (logFile.length() > MAX_LOG_FILE_SIZE) {
                rotateLogFiles(context)
            }
            
        } catch (e: Exception) {
            Log.e(TAG_PREFIX, "Erro ao escrever logs em arquivo", e)
        }
    }
    
    /**
     * Obtém o arquivo de log atual
     */
    private fun getLogFile(context: Context): File {
        val logDir = File(context.filesDir, "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        return File(logDir, LOG_FILE_NAME)
    }
    
    /**
     * Rotaciona arquivos de log
     */
    private fun rotateLogFiles(context: Context) {
        try {
            val logDir = File(context.filesDir, "logs")
            val currentFile = File(logDir, LOG_FILE_NAME)
            
            // Mover arquivos existentes
            for (i in MAX_LOG_FILES - 1 downTo 1) {
                val oldFile = File(logDir, "$LOG_FILE_NAME.$i")
                val newFile = File(logDir, "$LOG_FILE_NAME.${i + 1}")
                
                if (oldFile.exists()) {
                    if (i == MAX_LOG_FILES - 1) {
                        oldFile.delete() // Deletar o mais antigo
                    } else {
                        oldFile.renameTo(newFile)
                    }
                }
            }
            
            // Mover arquivo atual
            if (currentFile.exists()) {
                val backupFile = File(logDir, "$LOG_FILE_NAME.1")
                currentFile.renameTo(backupFile)
            }
            
        } catch (e: Exception) {
            Log.e(TAG_PREFIX, "Erro ao rotacionar arquivos de log", e)
        }
    }
    
    /**
     * Obtém todos os arquivos de log
     */
    fun getLogFiles(context: Context): List<File> {
        val logDir = File(context.filesDir, "logs")
        if (!logDir.exists()) {
            return emptyList()
        }
        
        return logDir.listFiles { file ->
            file.name.startsWith(LOG_FILE_NAME)
        }?.sortedBy { it.name } ?: emptyList()
    }
    
    /**
     * Lê conteúdo de um arquivo de log
     */
    suspend fun readLogFile(file: File): String = withContext(Dispatchers.IO) {
        try {
            file.readText()
        } catch (e: Exception) {
            "Erro ao ler arquivo de log: ${e.message}"
        }
    }
    
    /**
     * Obtém logs recentes
     */
    suspend fun getRecentLogs(context: Context, maxLines: Int = 1000): String = withContext(Dispatchers.IO) {
        try {
            val logFile = getLogFile(context)
            if (!logFile.exists()) {
                return@withContext "Nenhum log encontrado"
            }
            
            val lines = logFile.readLines()
            val recentLines = if (lines.size > maxLines) {
                lines.takeLast(maxLines)
            } else {
                lines
            }
            
            recentLines.joinToString("\n")
        } catch (e: Exception) {
            "Erro ao ler logs: ${e.message}"
        }
    }
    
    /**
     * Filtra logs por nível
     */
    suspend fun getLogsByLevel(
        context: Context,
        level: LogLevel,
        maxLines: Int = 1000
    ): String = withContext(Dispatchers.IO) {
        try {
            val allLogs = getRecentLogs(context, maxLines * 2) // Buscar mais para filtrar
            val filteredLines = allLogs.lines().filter { line ->
                line.contains("[${level.name}]")
            }.take(maxLines)
            
            filteredLines.joinToString("\n")
        } catch (e: Exception) {
            "Erro ao filtrar logs: ${e.message}"
        }
    }
    
    /**
     * Busca logs por texto
     */
    suspend fun searchLogs(
        context: Context,
        searchText: String,
        maxLines: Int = 1000
    ): String = withContext(Dispatchers.IO) {
        try {
            val allLogs = getRecentLogs(context, maxLines * 2)
            val filteredLines = allLogs.lines().filter { line ->
                line.contains(searchText, ignoreCase = true)
            }.take(maxLines)
            
            filteredLines.joinToString("\n")
        } catch (e: Exception) {
            "Erro ao buscar logs: ${e.message}"
        }
    }
    
    /**
     * Limpa todos os arquivos de log
     */
    suspend fun clearLogs(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val logFiles = getLogFiles(context)
            var success = true
            
            for (file in logFiles) {
                if (!file.delete()) {
                    success = false
                }
            }
            
            // Limpar fila de logs
            logQueue.clear()
            
            success
        } catch (e: Exception) {
            Log.e(TAG_PREFIX, "Erro ao limpar logs", e)
            false
        }
    }
    
    /**
     * Obtém estatísticas dos logs
     */
    suspend fun getLogStatistics(context: Context): LogStatistics = withContext(Dispatchers.IO) {
        try {
            val logFiles = getLogFiles(context)
            var totalSize = 0L
            var totalLines = 0
            val levelCounts = mutableMapOf<LogLevel, Int>()
            
            for (file in logFiles) {
                totalSize += file.length()
                val lines = file.readLines()
                totalLines += lines.size
                
                // Contar por nível
                for (line in lines) {
                    for (level in LogLevel.values()) {
                        if (line.contains("[${level.name}]")) {
                            levelCounts[level] = levelCounts.getOrDefault(level, 0) + 1
                            break
                        }
                    }
                }
            }
            
            LogStatistics(
                totalFiles = logFiles.size,
                totalSize = totalSize,
                totalLines = totalLines,
                levelCounts = levelCounts,
                queueSize = logQueue.size
            )
        } catch (e: Exception) {
            LogStatistics()
        }
    }
    
    /**
     * Exporta logs para arquivo
     */
    suspend fun exportLogs(
        context: Context,
        outputPath: String,
        includeSystemInfo: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()
            
            outputFile.bufferedWriter().use { writer ->
                // Informações do sistema
                if (includeSystemInfo) {
                    writer.write("=== INFORMAÇÕES DO SISTEMA ===\n")
                    writer.write("Data/Hora: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                    writer.write("Versão do Android: ${android.os.Build.VERSION.RELEASE}\n")
                    writer.write("Modelo do dispositivo: ${android.os.Build.MODEL}\n")
                    writer.write("Fabricante: ${android.os.Build.MANUFACTURER}\n")
                    writer.write("\n=== LOGS ===\n")
                }
                
                // Escrever logs de todos os arquivos
                val logFiles = getLogFiles(context)
                for (file in logFiles.reversed()) { // Mais recente primeiro
                    writer.write("\n--- ${file.name} ---\n")
                    writer.write(file.readText())
                    writer.write("\n")
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG_PREFIX, "Erro ao exportar logs", e)
            false
        }
    }
    
    /**
     * Cria relatório de crash
     */
    fun createCrashReport(
        context: Context,
        throwable: Throwable,
        additionalInfo: Map<String, String> = emptyMap()
    ): String {
        return buildString {
            append("=== RELATÓRIO DE CRASH ===\n")
            append("Data/Hora: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
            append("Versão do Android: ${android.os.Build.VERSION.RELEASE}\n")
            append("Modelo do dispositivo: ${android.os.Build.MODEL}\n")
            append("Fabricante: ${android.os.Build.MANUFACTURER}\n")
            append("Thread: ${Thread.currentThread().name}\n")
            append("\n")
            
            // Informações adicionais
            if (additionalInfo.isNotEmpty()) {
                append("=== INFORMAÇÕES ADICIONAIS ===\n")
                for ((key, value) in additionalInfo) {
                    append("$key: $value\n")
                }
                append("\n")
            }
            
            // Stack trace
            append("=== STACK TRACE ===\n")
            append(getStackTraceString(throwable))
        }
    }
    
    /**
     * Salva relatório de crash
     */
    suspend fun saveCrashReport(
        context: Context,
        throwable: Throwable,
        additionalInfo: Map<String, String> = emptyMap()
    ): String? = withContext(Dispatchers.IO) {
        try {
            val crashDir = File(context.filesDir, "crashes")
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val crashFile = File(crashDir, "crash_$timestamp.txt")
            
            val report = createCrashReport(context, throwable, additionalInfo)
            crashFile.writeText(report)
            
            // Log do crash
            e("CrashReport", "Crash salvo em: ${crashFile.absolutePath}", throwable)
            
            crashFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG_PREFIX, "Erro ao salvar relatório de crash", e)
            null
        }
    }
    
    /**
     * Obtém relatórios de crash
     */
    fun getCrashReports(context: Context): List<File> {
        val crashDir = File(context.filesDir, "crashes")
        if (!crashDir.exists()) {
            return emptyList()
        }
        
        return crashDir.listFiles { file ->
            file.name.startsWith("crash_") && file.name.endsWith(".txt")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Estatísticas de log
     */
    data class LogStatistics(
        val totalFiles: Int = 0,
        val totalSize: Long = 0L,
        val totalLines: Int = 0,
        val levelCounts: Map<LogLevel, Int> = emptyMap(),
        val queueSize: Int = 0
    )
    
    /**
     * Extensões para facilitar o uso
     */
    inline fun <reified T> T.logV(message: String, throwable: Throwable? = null) {
        v(T::class.java.simpleName, message, throwable)
    }
    
    inline fun <reified T> T.logD(message: String, throwable: Throwable? = null) {
        d(T::class.java.simpleName, message, throwable)
    }
    
    inline fun <reified T> T.logI(message: String, throwable: Throwable? = null) {
        i(T::class.java.simpleName, message, throwable)
    }
    
    inline fun <reified T> T.logW(message: String, throwable: Throwable? = null) {
        w(T::class.java.simpleName, message, throwable)
    }
    
    inline fun <reified T> T.logE(message: String, throwable: Throwable? = null) {
        e(T::class.java.simpleName, message, throwable)
    }
    
    inline fun <reified T> T.logWtf(message: String, throwable: Throwable? = null) {
        wtf(T::class.java.simpleName, message, throwable)
    }
}