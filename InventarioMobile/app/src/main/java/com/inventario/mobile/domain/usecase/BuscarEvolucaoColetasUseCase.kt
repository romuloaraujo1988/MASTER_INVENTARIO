package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.EvolucaoColeta
import com.inventario.mobile.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use Case: Buscar evolução de coletas por dia
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class BuscarEvolucaoColetasUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param dias Quantidade de dias para buscar (padrão: 30)
     * @return Result com lista de EvolucaoColeta ou erro
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
            
            // Buscar evolução do repository
            val result = dashboardRepository.buscarEvolucaoColetas(inventarioId, dias)
            
            // Aplicar regras de negócio adicionais
            result.map { evolucao ->
                // Ordenar por data
                evolucao.sortedBy { it.data }
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao buscar evolução de coletas: ${e.message}", e))
        }
    }
}
