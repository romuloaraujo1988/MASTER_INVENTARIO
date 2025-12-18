package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.model.TipoAcesso
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de histórico de scans
 * 
 * @since v2.11.0
 */
interface HistoricoScanRepository {
    
    /**
     * Registra um novo acesso a patrimônio no histórico
     */
    suspend fun registrarAcesso(
        numeroPatrimonio: String,
        descricao: String?,
        nomeSala: String?,
        salaId: Int?,
        tipoAcesso: TipoAcesso,
        estadoPatrimonio: String? = null,
        jaEstaColetado: Boolean = false
    ): Long
    
    /**
     * Marca o último acesso a um patrimônio como coletado
     */
    suspend fun marcarComoColetado(numeroPatrimonio: String)
    
    /**
     * Busca os últimos N registros do histórico
     */
    suspend fun buscarUltimos(limite: Int = 20): List<HistoricoScan>
    
    /**
     * Observa os últimos registros em tempo real
     */
    fun observarUltimos(limite: Int = 20): Flow<List<HistoricoScan>>
    
    /**
     * Busca registros de hoje
     */
    suspend fun buscarDeHoje(): List<HistoricoScan>
    
    /**
     * Retorna estatísticas de hoje
     */
    suspend fun getEstatisticasHoje(): EstatisticasHistorico
    
    /**
     * Limpa registros antigos, mantendo apenas os últimos N
     */
    suspend fun limparAntigos(manter: Int = 50)
    
    /**
     * Limpa todo o histórico
     */
    suspend fun limparTudo()
    
    /**
     * Busca registros que foram coletados
     */
    suspend fun buscarColetados(limite: Int = 20): List<HistoricoScan>
    
    /**
     * Busca registros que foram apenas consultados
     */
    suspend fun buscarApenasConsultados(limite: Int = 20): List<HistoricoScan>
}

/**
 * Estatísticas do histórico
 */
data class EstatisticasHistorico(
    val totalScansHoje: Int,
    val totalColetasHoje: Int,
    val totalConsultasHoje: Int
) {
    val taxaConversao: Float
        get() = if (totalScansHoje > 0) {
            (totalColetasHoje.toFloat() / totalScansHoje) * 100
        } else 0f
}
