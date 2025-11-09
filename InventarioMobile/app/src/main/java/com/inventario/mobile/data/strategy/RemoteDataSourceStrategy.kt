package com.inventario.mobile.data.strategy

import android.content.Context
import android.util.Log
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.model.Patrimonio
import com.inventario.mobile.model.Sala
import com.inventario.mobile.utils.NetworkUtils

/**
 * Estratégia para buscar dados do servidor remoto (API)
 */
class RemoteDataSourceStrategy(
    private val context: Context,
    private val patrimonioApi: PatrimonioApi,
    private val salaApi: SalaApi
) : DataSourceStrategy {
    
    companion object {
        private const val TAG = "RemoteDataSource"
    }
    
    override suspend fun isAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }
    
    override suspend fun getPatrimonios(): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios do servidor...")
            
            val response = patrimonioApi.listarPatrimonios()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val patrimonios = response.body()?.data ?: emptyList()
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios obtidos do servidor")
                Result.success(patrimonios)
            } else {
                val error = "Erro na API: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios do servidor", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getPatrimonioPorNumero(numero: String): Result<Patrimonio> {
        return try {
            Log.d(TAG, "Buscando patrimônio $numero do servidor...")
            
            val response = patrimonioApi.buscarPorNumero(numero)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val patrimonio = response.body()?.data
                if (patrimonio != null) {
                    Log.d(TAG, "✓ Patrimônio encontrado no servidor")
                    Result.success(patrimonio)
                } else {
                    Result.failure(Exception("Patrimônio não encontrado"))
                }
            } else {
                val error = "Erro na API: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônio do servidor", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getSalas(): Result<List<Sala>> {
        return try {
            Log.d(TAG, "Buscando salas do servidor...")
            
            val response = salaApi.listarSalas()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val salas = response.body()?.data ?: emptyList()
                Log.d(TAG, "✓ ${salas.size} salas obtidas do servidor")
                Result.success(salas)
            } else {
                val error = "Erro na API: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar salas do servidor", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getSalaPorId(id: Int): Result<Sala> {
        return try {
            // Buscar todas e filtrar (ou criar endpoint específico)
            val result = getSalas()
            
            if (result.isSuccess) {
                val sala = result.getOrNull()?.find { it.id == id }
                if (sala != null) {
                    Result.success(sala)
                } else {
                    Result.failure(Exception("Sala não encontrada"))
                }
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar sala do servidor", e)
            Result.failure(e)
        }
    }
    
    override fun getSourceType(): DataSourceType = DataSourceType.REMOTE
}
