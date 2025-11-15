package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Sincronizar coletas pendentes com o servidor
 * 
 * Regras de negócio:
 * - Busca todas as coletas não sincronizadas
 * - Envia em lote para o servidor
 * - Marca como sincronizadas em caso de sucesso
 * - Registra erro em caso de falha
 * - Retorna quantidade de coletas sincronizadas
 */
class SincronizarColetasPendentesUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    /**
     * Executa sincronização de coletas pendentes
     * 
     * @return Result com quantidade de coletas sincronizadas ou erro
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            // 1. Verificar se há coletas pendentes
            val coletasPendentes = coletaRepository.getColetasNaoSincronizadas()
            
            if (coletasPendentes.isEmpty()) {
                return Result.success(0)
            }
            
            // 2. Sincronizar coletas
            val quantidadeSincronizada = coletaRepository.sincronizarColetasPendentes()
            
            // 3. Verificar resultado
            if (quantidadeSincronizada < 0) {
                Result.failure(Exception("Erro ao sincronizar coletas"))
            } else {
                Result.success(quantidadeSincronizada)
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
