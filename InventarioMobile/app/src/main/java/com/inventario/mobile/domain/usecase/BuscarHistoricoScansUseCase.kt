package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.repository.EstatisticasHistorico
import com.inventario.mobile.domain.repository.HistoricoScanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case para buscar histórico de scans
 * 
 * @since v2.11.0
 */
class BuscarHistoricoScansUseCase @Inject constructor(
    private val historicoRepository: HistoricoScanRepository
) {
    
    /**
     * Busca os últimos N registros do histórico
     */
    suspend operator fun invoke(limite: Int = 20): Result<List<HistoricoScan>> {
        return try {
            val historico = historicoRepository.buscarUltimos(limite)
            Result.success(historico)
        } catch (e: Exception) {
            android.util.Log.e("BuscarHistoricoUC", "Erro ao buscar histórico", e)
            Result.failure(e)
        }
    }
    
    /**
     * Observa os últimos registros em tempo real
     */
    fun observar(limite: Int = 20): Flow<List<HistoricoScan>> {
        return historicoRepository.observarUltimos(limite)
    }
    
    /**
     * Busca registros de hoje
     */
    suspend fun buscarDeHoje(): Result<List<HistoricoScan>> {
        return try {
            val historico = historicoRepository.buscarDeHoje()
            Result.success(historico)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Retorna estatísticas de hoje
     */
    suspend fun getEstatisticasHoje(): Result<EstatisticasHistorico> {
        return try {
            val stats = historicoRepository.getEstatisticasHoje()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca apenas registros que resultaram em coleta
     */
    suspend fun buscarColetados(limite: Int = 20): Result<List<HistoricoScan>> {
        return try {
            val historico = historicoRepository.buscarColetados(limite)
            Result.success(historico)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca apenas registros de consulta (sem coleta)
     */
    suspend fun buscarConsultas(limite: Int = 20): Result<List<HistoricoScan>> {
        return try {
            val historico = historicoRepository.buscarApenasConsultados(limite)
            Result.success(historico)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Limpa todo o histórico
     */
    suspend fun limparHistorico(): Result<Unit> {
        return try {
            historicoRepository.limparTudo()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
