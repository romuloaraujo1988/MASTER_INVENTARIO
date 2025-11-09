package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Use Case: Buscar descrições de patrimônios não coletados
 * Para facilitar coleta sem etiqueta
 */
class BuscarDescricoesNaoColetadasUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return try {
            val descricoes = patrimonioRepository.buscarDescricoesNaoColetadas()
            Result.success(descricoes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
