package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.sync.SyncStats
import javax.inject.Inject

/**
 * Use Case: Sincronizar coletas pendentes com o servidor
 * Retorna estatísticas de sincronização (sucessos e falhas)
 */
class SincronizarColetasPendentesUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(): Result<SyncStats> {
        return try {
            // Por enquanto, retorna apenas quantidade sincronizada
            // TODO: Atualizar repository para retornar SyncStats completo
            val quantidadeSincronizada = coletaRepository.sincronizarColetasPendentes()
            Result.success(SyncStats(synced = quantidadeSincronizada, failed = 0))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
