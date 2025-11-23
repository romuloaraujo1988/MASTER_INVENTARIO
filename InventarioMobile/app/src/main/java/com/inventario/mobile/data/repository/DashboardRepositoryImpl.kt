package com.inventario.mobile.data.repository

import android.util.Log
import com.inventario.mobile.data.mapper.DashboardMapper
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.domain.model.*
import com.inventario.mobile.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Implementação do DashboardRepository
 * Coordena fontes de dados (remote-first)
 */
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: DashboardMapper
) : DashboardRepository {
    
    companion object {
        private const val TAG = "DashboardRepositoryImpl"
    }
    
    override suspend fun buscarEstatisticas(inventarioId: Int?): Result<com.inventario.mobile.domain.model.DashboardStats> {
        return try {
            Log.d(TAG, "═══ BUSCAR ESTATÍSTICAS ═══")
            Log.d(TAG, "Inventário ID: $inventarioId")
            
            val response = if (inventarioId != null) {
                Log.d(TAG, "Chamando: getDashboardStatsWithInventario($inventarioId)")
                apiService.getDashboardStatsWithInventario(inventarioId)
            } else {
                Log.d(TAG, "Chamando: getDashboardStats()")
                apiService.getDashboardStats()
            }
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            Log.d(TAG, "Response Body: ${response.body()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "API Response Success: ${apiResponse.success}")
                Log.d(TAG, "API Response Message: ${apiResponse.message}")
                Log.d(TAG, "API Response Data: ${apiResponse.data}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    Log.d(TAG, "DTO: totalPatrimonios=${dto.totalPatrimonios}, coletados=${dto.patrimoniosColetados}, pendentes=${dto.patrimoniosPendentes}")
                    
                    val stats = mapper.toDomain(dto)
                    Log.d(TAG, "Stats mapeados: ${stats.percentualConclusao}% (${stats.totalColetados}/${stats.totalPatrimonios})")
                    Log.d(TAG, "═══ SUCESSO ═══")
                    Result.success(stats)
                } else {
                    val error = "API retornou erro: ${apiResponse.message}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } else {
                val error = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "═══ ERRO ═══", e)
            Log.e(TAG, "Tipo: ${e.javaClass.simpleName}")
            Log.e(TAG, "Mensagem: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    override suspend fun buscarEstatisticasLocais(inventarioId: Int?): Result<com.inventario.mobile.domain.model.DashboardStats> {
        return try {
            Log.d(TAG, "═══ BUSCAR ESTATÍSTICAS LOCAIS (OFFLINE) ═══")
            Log.d(TAG, "Inventário ID: $inventarioId")
            
            // TODO: Implementar busca no banco local (Room)
            // Por enquanto, retornar estatísticas vazias
            Log.w(TAG, "⚠️ Busca local ainda não implementada - retornando dados vazios")
            
            val emptyStats = com.inventario.mobile.domain.model.DashboardStats(
                totalPatrimonios = 0,
                totalColetados = 0,
                totalPendentes = 0,
                percentualConclusao = 0.0,
                coletoresAtivos = 0,
                divergencias = 0,
                valorTotal = 0.0,
                inventarioId = inventarioId,
                inventarioNome = null,
                coletasHoje = 0,
                coletasSemana = 0,
                coletasMes = 0,
                tempoMedioColeta = 0.0,
                isOfflineData = true
            )
            
            Log.d(TAG, "═══ RETORNANDO DADOS VAZIOS (OFFLINE) ═══")
            Result.success(emptyStats)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar estatísticas locais", e)
            Result.failure(e)
        }
    }

    
    override suspend fun buscarEvolucaoColetas(
        inventarioId: Int?,
        dias: Int
    ): Result<List<EvolucaoColeta>> {
        return try {
            Log.d(TAG, "Buscando evolução ($dias dias)...")
            
            val response = apiService.getColetasEvolucao(dias = dias)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val evolucaoMap = apiResponse.data["evolucao"] as? Map<*, *>
                    
                    if (evolucaoMap != null) {
                        val evolucaoList = mapper.evolucaoMapToDomain(evolucaoMap)
                        Log.d(TAG, "Evolução: ${evolucaoList.size} dias")
                        Result.success(evolucaoList)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Erro"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarTopItens(inventarioId: Int?, limit: Int): Result<List<TopItem>> {
        Log.w(TAG, "buscarTopItens: não implementado")
        return Result.success(emptyList())
    }
    
    override suspend fun buscarDistribuicaoPorSala(inventarioId: Int?, limit: Int): Result<List<DistribuicaoSala>> {
        Log.w(TAG, "buscarDistribuicaoPorSala: não implementado")
        return Result.success(emptyList())
    }
    
    override suspend fun buscarEstatisticasPorStatus(inventarioId: Int?): Result<List<EstatisticaStatus>> {
        Log.w(TAG, "buscarEstatisticasPorStatus: não implementado")
        return Result.success(emptyList())
    }
}
