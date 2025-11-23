package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.domain.model.EvolucaoColeta
import com.inventario.mobile.domain.repository.DashboardRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Use Case: Buscar evolução de coletas por dia
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 * 
 * v2.0: Fallback gracioso - retorna lista vazia se servidor inacessível
 */
class BuscarEvolucaoColetasUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    
    companion object {
        private const val TAG = "BuscarEvolucaoColetasUseCase"
    }
    
    /**
     * Executa o caso de uso
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param dias Quantidade de dias para buscar (padrão: 30)
     * @return Result com lista de EvolucaoColeta ou erro
     * 
     * Estratégia de Fallback:
     * 1. Tenta buscar do servidor
     * 2. Se falhar por erro de rede, retorna lista vazia
     */
    suspend operator fun invoke(
        inventarioId: Int? = null,
        dias: Int = 30
    ): Result<List<EvolucaoColeta>> {
        return try {
            // Validações de negócio
            if (dias <= 0) {
                return Result.failure(Exception("Quantidade de dias deve ser maior que zero"))
            }
            
            if (dias > 365) {
                return Result.failure(Exception("Quantidade de dias não pode ser maior que 365"))
            }
            
            Log.d(TAG, "Buscando evolução de coletas ($dias dias)...")
            
            // Buscar evolução do repository
            val result = dashboardRepository.buscarEvolucaoColetas(inventarioId, dias)
            
            if (result.isSuccess) {
                // Aplicar regras de negócio adicionais
                result.map { evolucao ->
                    // Ordenar por data
                    evolucao.sortedBy { it.data }
                }
            } else {
                // Se falhou, verificar se é erro de rede
                val error = result.exceptionOrNull()
                handleError(error)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao buscar evolução", e)
            handleError(e)
        }
    }
    
    /**
     * Trata erros com fallback gracioso
     */
    private fun handleError(error: Throwable?): Result<List<EvolucaoColeta>> {
        val isNetworkError = error is UnknownHostException || 
                            error is SocketTimeoutException ||
                            error?.message?.contains("failed to connect", ignoreCase = true) == true
        
        return if (isNetworkError) {
            Log.w(TAG, "⚠️ Erro de rede detectado - retornando lista vazia")
            // Retornar lista vazia ao invés de falhar
            Result.success(emptyList())
        } else {
            // Erro não relacionado a rede - propagar
            Log.e(TAG, "❌ Erro não relacionado a rede: ${error?.message}")
            Result.failure(Exception("Erro ao buscar evolução de coletas: ${error?.message}", error))
        }
    }
}
