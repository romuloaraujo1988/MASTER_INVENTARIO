package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use Case: Buscar estatísticas do dashboard
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class BuscarEstatisticasDashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @return Result com DashboardStats ou erro
     */
    suspend operator fun invoke(inventarioId: Int? = null): Result<DashboardStats> {
        return try {
            // Validações de negócio (se necessário)
            
            // Buscar estatísticas do repository
            val result = dashboardRepository.buscarEstatisticas(inventarioId)
            
            // Aplicar regras de negócio adicionais (se necessário)
            result.map { stats ->
                // Exemplo: Adicionar validações ou transformações
                stats
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao buscar estatísticas: ${e.message}", e))
        }
    }
}
