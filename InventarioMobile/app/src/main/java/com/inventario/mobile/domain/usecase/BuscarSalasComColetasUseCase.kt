package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.remote.api.ApiService
import javax.inject.Inject

/**
 * Use Case: Buscar salas que possuem coletas registradas
 * 
 * Busca do servidor todas as salas onde há coletas,
 * útil para popular o filtro de salas na tela de itens coletados.
 */
class BuscarSalasComColetasUseCase @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "BuscarSalasComColetasUC"
    }
    
    /**
     * Busca todas as salas que possuem coletas
     * 
     * @param inventarioId ID do inventário (opcional, usa o ativo se não informado)
     * @return Result com lista de nomes de salas ou erro
     */
    suspend operator fun invoke(inventarioId: Int? = null): Result<List<String>> {
        return try {
            Log.d(TAG, "Buscando salas com coletas do servidor (inventário: $inventarioId)")
            
            val response = apiService.buscarSalasComColetas(inventarioId)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    // Extrair nomes das salas da resposta
                    val salas = apiResponse.data.mapNotNull { salaMap ->
                        // O servidor retorna Map com "nome" ou "nomeSala"
                        (salaMap["nome"] as? String) 
                            ?: (salaMap["nomeSala"] as? String)
                            ?: (salaMap["localizacaoEncontrada"] as? String)
                    }.filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    
                    Log.d(TAG, "✓ ${salas.size} salas com coletas encontradas")
                    Result.success(salas)
                } else {
                    Log.w(TAG, "API retornou success=false ou data=null")
                    Result.failure(Exception("Erro ao buscar salas com coletas"))
                }
            } else {
                Log.e(TAG, "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar salas com coletas", e)
            Result.failure(e)
        }
    }
}
