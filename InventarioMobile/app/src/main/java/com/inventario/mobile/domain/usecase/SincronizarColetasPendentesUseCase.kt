package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Sincronizar coletas pendentes com o servidor
 */
class SincronizarColetasPendentesUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val quantidadeSincronizada = coletaRepository.sincronizarColetasPendentes()
            Result.success(quantidadeSincronizada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
