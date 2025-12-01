package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.repository.PatrimonioRepositoryImpl
import com.inventario.mobile.domain.model.EstatisticasSala
import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case para buscar estatísticas detalhadas de uma sala.
 * Calcula total, coletados, pendentes, percentual, coletados hoje e na semana.
 * 
 * Regra: Contém apenas lógica de negócio, sem dependências Android.
 */
class BuscarEstatisticasSalaUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepositoryImpl,
    private val coletaRepository: ColetaRepository
) {
    
    /**
     * Busca estatísticas detalhadas de uma sala.
     * 
     * @param salaId ID da sala
     * @param salaNome Nome da sala (para exibição)
     * @return Result com estatísticas da sala ou erro
     */
    suspend operator fun invoke(salaId: Int, salaNome: String): Result<EstatisticasSala> {
        // Validar parâmetros
        if (salaId <= 0) {
            return Result.failure(IllegalArgumentException("ID da sala inválido"))
        }
        
        return try {
            // Buscar contagens
            val totalResult = patrimonioRepository.contarPorSala(salaId)
            val coletadosResult = patrimonioRepository.contarColetadosPorSala(salaId)
            
            if (totalResult.isFailure) {
                return Result.failure(totalResult.exceptionOrNull() ?: Exception("Erro ao contar patrimônios"))
            }
            
            if (coletadosResult.isFailure) {
                return Result.failure(coletadosResult.exceptionOrNull() ?: Exception("Erro ao contar coletados"))
            }
            
            val total = totalResult.getOrDefault(0)
            val coletados = coletadosResult.getOrDefault(0)
            
            // Calcular estatísticas temporais (simplificado - pode ser expandido)
            // TODO: Implementar busca de coletados hoje e na semana via ColetaRepository
            val coletadosHoje = 0
            val coletadosSemana = 0
            
            // Criar estatísticas
            val estatisticas = EstatisticasSala.criar(
                salaId = salaId,
                salaNome = salaNome,
                totalPatrimonios = total,
                coletados = coletados,
                coletadosHoje = coletadosHoje,
                coletadosSemana = coletadosSemana
            )
            
            Result.success(estatisticas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
