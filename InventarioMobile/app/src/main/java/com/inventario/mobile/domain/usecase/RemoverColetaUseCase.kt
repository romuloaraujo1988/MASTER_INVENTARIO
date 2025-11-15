package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Remover uma coleta
 * 
 * Responsabilidade: Remover uma coleta do repositório
 */
class RemoverColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    /**
     * Remove uma coleta pelo ID
     * 
     * @param id ID da coleta a ser removida
     * @return Result indicando sucesso ou erro
     */
    suspend operator fun invoke(id: Int): Result<Unit> {
        return try {
            // Buscar coleta
            val coleta = coletaRepository.getColetaById(id.toLong())
                ?: return Result.failure(Exception("Coleta não encontrada"))
            
            // Remover coleta
            coletaRepository.deleteColeta(coleta)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
