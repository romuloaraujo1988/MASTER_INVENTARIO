package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.TopItem
import com.inventario.mobile.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use Case: Buscar top itens mais coletados
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class BuscarTopItensUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param limit Quantidade máxima de itens (padrão: 10)
     * @return Result com lista de TopItem ou erro
     */
    suspend operator fun invoke(
        inventarioId: Int? = null,
        limit: Int = 10
    ): Result<List<TopItem>> {
        return try {
            // Validações de negócio
            if (limit <= 0) {
                return Result.failure(Exception("Limite deve ser maior que zero"))
            }
            
            if (limit > 100) {
                return Result.failure(Exception("Limite não pode ser maior que 100"))
            }
            
            // Buscar top itens do repository
            dashboardRepository.buscarTopItens(inventarioId, limit)
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao buscar top itens: ${e.message}", e))
        }
    }
}
