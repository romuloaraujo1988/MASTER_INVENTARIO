package com.inventario.mobile.sync

import com.inventario.mobile.data.local.dao.SyncLogDao
import com.inventario.mobile.data.local.entity.SyncLogEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logger para histórico de sincronizações
 * Registra execuções de sincronização automática no banco local
 */
@Singleton
class SyncLogger @Inject constructor(
    private val syncLogDao: SyncLogDao
) {
    
    private var currentLogId: Long = 0
    private var startTime: Long = 0
    
    /**
     * Registra início de sincronização
     */
    suspend fun logStart(type: SyncType) {
        startTime = System.currentTimeMillis()
        
        val log = SyncLogEntity(
            tipo = type.name,
            dataHora = startTime,
            status = SyncStatus.IN_PROGRESS.name,
            mensagem = "Iniciando sincronização ${type.displayName}..."
        )
        
        currentLogId = syncLogDao.inserir(log)
    }
    
    /**
     * Registra sucesso da sincronização
     */
    suspend fun logSuccess(stats: SyncStats) {
        val duration = System.currentTimeMillis() - startTime
        
        val log = SyncLogEntity(
            id = currentLogId,
            tipo = SyncType.AUTO.name,
            dataHora = System.currentTimeMillis(),
            status = SyncStatus.SUCCESS.name,
            mensagem = "${stats.synced} coletas sincronizadas${if (stats.failed > 0) ", ${stats.failed} falhas" else ""}",
            coletasSincronizadas = stats.synced,
            coletasFalhadas = stats.failed,
            duracao = duration
        )
        
        syncLogDao.inserir(log)
        
        // Manter apenas últimas 50 entradas
        syncLogDao.limparAntigos(50)
    }
    
    /**
     * Registra erro na sincronização
     */
    suspend fun logError(error: Throwable) {
        val duration = System.currentTimeMillis() - startTime
        
        val log = SyncLogEntity(
            id = currentLogId,
            tipo = SyncType.AUTO.name,
            dataHora = System.currentTimeMillis(),
            status = SyncStatus.ERROR.name,
            mensagem = error.message ?: "Erro desconhecido",
            stackTrace = error.stackTraceToString(),
            duracao = duration
        )
        
        syncLogDao.inserir(log)
    }
    
    /**
     * Busca últimos logs
     */
    suspend fun getRecentLogs(limit: Int = 50): List<SyncLogEntity> {
        return syncLogDao.buscarUltimos(limit)
    }
    
    /**
     * Busca estatísticas de sincronização
     */
    suspend fun getStats(): SyncLogStats {
        return SyncLogStats(
            total = syncLogDao.contar(),
            sucessos = syncLogDao.contarSucessos(),
            erros = syncLogDao.contarErros(),
            duracaoMedia = syncLogDao.duracaoMedia() ?: 0
        )
    }
}

/**
 * Tipos de sincronização
 */
enum class SyncType(val displayName: String) {
    AUTO("automática"),
    MANUAL("manual"),
    BY_COUNT("por contador")
}

/**
 * Status de sincronização
 */
enum class SyncStatus {
    IN_PROGRESS,
    SUCCESS,
    ERROR
}

/**
 * Estatísticas de sincronização
 */
data class SyncStats(
    val synced: Int,
    val failed: Int
)

/**
 * Estatísticas de logs
 */
data class SyncLogStats(
    val total: Int,
    val sucessos: Int,
    val erros: Int,
    val duracaoMedia: Long
)
