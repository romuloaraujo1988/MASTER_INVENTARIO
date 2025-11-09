package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Use Case: Buscar patrimônios não coletados por descrição
 * Para coleta sem etiqueta
 */
class BuscarPatrimoniosPorDescricaoUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(descricao: String): Result<List<Patrimonio>> {
        return try {
            if (descricao.isBlank()) {
                return Result.failure(Exception("Descrição é obrigatória"))
            }
            
            val patrimonios = patrimonioRepository.buscarPorDescricaoNaoColetados(descricao)
            Result.success(patrimonios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
