package com.inventario.mobile.data.audit

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.local.dao.LogColetaDao
import com.inventario.mobile.data.local.entity.AcaoLog
import com.inventario.mobile.data.local.entity.LogColetaEntity
import com.inventario.mobile.network.NetworkQualityMonitor
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço de auditoria para rastreamento de operações
 * 
 * CRÍTICO: Registra todas as operações importantes para:
 * - Investigação de problemas
 * - Recuperação de dados
 * - Análise de performance
 * - Compliance e auditoria
 * 
 * v2.2: Implementação completa de auditoria
 */
@Singleton
class AuditService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logDao: LogColetaDao,
    private val preferencesManager: PreferencesManager,
    private val networkQualityMonitor: NetworkQualityMonitor
) {
    
    companion object {
        private const val TAG = "AuditService"
    }
    
    /**
     * Registra criação de coleta
     */
    suspend fun registrarColetaCriada(
        coletaId: Long,
        numeroPatrimonio: String,
        sucesso: Boolean = true,
        erro: String? = null
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = AcaoLog.CRIADA,
            detalhes = "Patrimônio: $numeroPatrimonio",
            sucesso = sucesso,
            mensagemErro = erro
        )
    }
    
    /**
     * Registra validação de coleta
     */
    suspend fun registrarValidacao(
        coletaId: Long,
        sucesso: Boolean,
        erro: String? = null
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = if (sucesso) AcaoLog.VALIDADA else AcaoLog.VALIDACAO_FALHOU,
            detalhes = if (sucesso) "Validação passou" else "Validação falhou: $erro",
            sucesso = sucesso,
            mensagemErro = erro
        )
    }
    
    /**
     * Registra sincronização bem-sucedida
     */
    suspend fun registrarSincronizacao(
        coletaId: Long,
        servidorId: Long? = null
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = AcaoLog.SINCRONIZADA,
            detalhes = "Servidor ID: $servidorId",
            sucesso = true
        )
    }
    
    /**
     * Registra erro de sincronização
     */
    suspend fun registrarErroSincronizacao(
        coletaId: Long,
        erro: String,
        tentativa: Int
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = AcaoLog.ERRO_SYNC,
            detalhes = "Tentativa $tentativa",
            sucesso = false,
            mensagemErro = erro
        )
    }
    
    /**
     * Registra tentativa de sincronização
     */
    suspend fun registrarTentativaSincronizacao(
        coletaId: Long,
        tentativa: Int
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = AcaoLog.TENTATIVA_SYNC,
            detalhes = "Tentativa $tentativa",
            sucesso = true
        )
    }
    
    /**
     * Registra detecção de duplicata
     */
    suspend fun registrarDuplicataDetectada(
        coletaId: Long,
        numeroPatrimonio: String,
        coletaAnteriorId: Long
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = AcaoLog.DUPLICATA_DETECTADA,
            detalhes = "Patrimônio $numeroPatrimonio já coletado (ID: $coletaAnteriorId)",
            sucesso = false
        )
    }
    
    /**
     * Registra criação de backup
     */
    suspend fun registrarBackup(
        quantidadeColetas: Int,
        tamanhoBytes: Long
    ) {
        registrarLog(
            coletaId = 0, // Não relacionado a uma coleta específica
            acao = AcaoLog.BACKUP_CRIADO,
            detalhes = "$quantidadeColetas coletas, $tamanhoBytes bytes",
            sucesso = true
        )
    }
    
    /**
     * Registra exportação de dados
     */
    suspend fun registrarExportacao(
        coletaId: Long,
        formato: String
    ) {
        registrarLog(
            coletaId = coletaId,
            acao = AcaoLog.EXPORTADA,
            detalhes = "Formato: $formato",
            sucesso = true
        )
    }
    
    /**
     * Registra restauração de backup
     */
    suspend fun registrarRestauracao(
        quantidadeColetas: Int,
        sucesso: Boolean,
        erro: String? = null
    ) {
        registrarLog(
            coletaId = 0,
            acao = AcaoLog.RESTAURADA,
            detalhes = "$quantidadeColetas coletas restauradas",
            sucesso = sucesso,
            mensagemErro = erro
        )
    }
    
    /**
     * Busca histórico de uma coleta
     */
    suspend fun buscarHistoricoColeta(coletaId: Long): List<LogColetaEntity> {
        return logDao.buscarPorColeta(coletaId)
    }
    
    /**
     * Busca erros recentes
     */
    suspend fun buscarErrosRecentes(limit: Int = 50): List<LogColetaEntity> {
        return logDao.buscarErros()
    }
    
    /**
     * Limpa logs antigos (mais de 90 dias)
     */
    suspend fun limparLogsAntigos(): Int {
        val noventaDiasAtras = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        val removidos = logDao.limparAntigos(noventaDiasAtras)
        
        Log.d(TAG, "✓ $removidos logs antigos removidos")
        return removidos
    }
    
    // ========================================
    // Métodos Privados
    // ========================================
    
    private suspend fun registrarLog(
        coletaId: Long,
        acao: String,
        detalhes: String?,
        sucesso: Boolean,
        mensagemErro: String? = null
    ) {
        try {
            val userId = getUserId()
            val userName = preferencesManager.getUserName() ?: "Desconhecido"
            val networkQuality = networkQualityMonitor.networkQuality.value
            
            val log = LogColetaEntity(
                coletaId = coletaId,
                acao = acao,
                timestamp = System.currentTimeMillis(),
                detalhes = detalhes,
                usuarioId = userId,
                nomeUsuario = userName,
                deviceId = android.os.Build.MODEL,
                appVersion = getAppVersion(),
                tipoRede = networkQuality.name,
                qualidadeRede = networkQuality.name,
                sucesso = sucesso,
                mensagemErro = mensagemErro
            )
            
            logDao.inserir(log)
            
            Log.d(TAG, "✓ Log registrado: $acao - Coleta $coletaId - Sucesso: $sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Erro ao registrar log", e)
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
    
    private fun getUserId(): Int {
        return preferencesManager.getUserId() ?: 0
    }
}
