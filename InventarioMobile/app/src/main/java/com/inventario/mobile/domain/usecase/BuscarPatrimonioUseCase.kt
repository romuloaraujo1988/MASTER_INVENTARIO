package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Use Case: Buscar patrimônio por número
 */
class BuscarPatrimonioUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(numero: String): Result<Patrimonio> {
        return try {
            if (numero.isBlank()) {
                return Result.failure(Exception("Número do patrimônio é obrigatório"))
            }
            
            val patrimonio = patrimonioRepository.buscarPorNumero(numero)
                ?: return Result.failure(Exception("Patrimônio não encontrado"))
            
            Result.success(patrimonio)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
