package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.repository.DashboardRepository
import kotlinx.coroutines.CancellationException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Use Case: Buscar estatísticas do dashboard
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 * 
 * v2.0: Fallback gracioso - retorna dados locais se servidor inacessível
 */
class BuscarEstatisticasDashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    
    companion object {
        private const val TAG = "BuscarEstatisticasDashboardUseCase"
    }
    
    /**
     * Executa o caso de uso
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @return Result com DashboardStats ou erro
     * 
     * Estratégia de Fallback:
     * 1. Tenta buscar do servidor
     * 2. Se falhar por erro de rede, retorna dados locais
     * 3. Se não houver dados locais, retorna estatísticas vazias
     */
    suspend operator fun invoke(inventarioId: Int? = null): Result<DashboardStats> {
        return try {
            Log.d(TAG, "Buscando estatísticas do dashboard...")
            
            // Tentar buscar estatísticas do repository (servidor ou local)
            val result = dashboardRepository.buscarEstatisticas(inventarioId)
            
            if (result.isSuccess) {
                Log.d(TAG, "✓ Estatísticas obtidas com sucesso")
                result
            } else {
                // Se falhou, verificar se é erro de rede
                val error = result.exceptionOrNull()
                handleError(error, inventarioId)
            }
            
        } catch (e: CancellationException) {
            // Job foi cancelado (normal durante navegação/lifecycle)
            Log.d(TAG, "ℹ️ Job cancelado - retornando dados locais")
            
            // Tentar buscar dados locais sem propagar o cancelamento
            try {
                val localStats = dashboardRepository.buscarEstatisticasLocais(inventarioId)
                if (localStats.isSuccess) {
                    localStats
                } else {
                    Result.success(createEmptyStats())
                }
            } catch (e: Exception) {
                Result.success(createEmptyStats())
            }
            
        } catch (e: Exception) {
            // Verificar se é erro de rede (não logar como erro)
            val isNetworkError = e is UnknownHostException || 
                                e is SocketTimeoutException ||
                                e.message?.contains("failed to connect", ignoreCase = true) == true ||
                                e.message?.contains("Job was cancelled", ignoreCase = true) == true
            
            if (isNetworkError) {
                Log.i(TAG, "ℹ️ Sem conexão com servidor - usando dados locais")
            } else {
                Log.e(TAG, "❌ Exceção ao buscar estatísticas", e)
            }
            
            handleError(e, inventarioId)
        }
    }
    
    /**
     * Trata erros com fallback gracioso
     */
    private suspend fun handleError(error: Throwable?, inventarioId: Int?): Result<DashboardStats> {
        // Verificar se é erro de rede ou cancelamento
        val isNetworkError = error is UnknownHostException || 
                            error is SocketTimeoutException ||
                            error is CancellationException ||
                            error?.message?.contains("failed to connect", ignoreCase = true) == true ||
                            error?.message?.contains("Job was cancelled", ignoreCase = true) == true
        
        return if (isNetworkError) {
            Log.w(TAG, "⚠️ Erro de rede/cancelamento detectado - usando fallback offline")
            
            // Tentar buscar dados locais
            try {
                val localStats = dashboardRepository.buscarEstatisticasLocais(inventarioId)
                
                if (localStats.isSuccess) {
                    Log.d(TAG, "✓ Estatísticas locais obtidas com sucesso")
                    localStats
                } else {
                    // Se não houver dados locais, retornar estatísticas vazias
                    Log.w(TAG, "⚠️ Sem dados locais - retornando estatísticas vazias")
                    Result.success(createEmptyStats())
                }
            } catch (e: CancellationException) {
                // Cancelamento durante busca local - retornar vazio
                Log.d(TAG, "ℹ️ Cancelado durante busca local - retornando vazio")
                Result.success(createEmptyStats())
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar dados locais", e)
                // Retornar estatísticas vazias ao invés de falhar
                Result.success(createEmptyStats())
            }
        } else {
            // Erro não relacionado a rede - propagar
            Log.e(TAG, "❌ Erro não relacionado a rede: ${error?.message}")
            Result.failure(Exception("Erro ao buscar estatísticas: ${error?.message}", error))
        }
    }
    
    /**
     * Cria estatísticas vazias para quando não há dados disponíveis
     */
    private fun createEmptyStats(): DashboardStats {
        return DashboardStats(
            totalPatrimonios = 0,
            totalColetados = 0,
            totalPendentes = 0,
            percentualConclusao = 0.0,
            coletasHoje = 0,
            coletasSemana = 0,
            coletasMes = 0,
            tempoMedioColeta = 0.0,
            isOfflineData = true  // Flag indicando que são dados offline/vazios
        )
    }
}
