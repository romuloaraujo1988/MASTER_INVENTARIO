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
            Log.d(TAG, "Buscando estatísticas...")
            
            val response = apiService.getDashboardStats()
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    val stats = mapper.toDomain(dto)
                    
                    Log.d(TAG, "Estatísticas carregadas: ${stats.percentualConclusao}%")
                    Result.success(stats)
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
