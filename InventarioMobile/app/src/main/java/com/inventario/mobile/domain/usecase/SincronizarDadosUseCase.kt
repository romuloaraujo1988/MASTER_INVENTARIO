package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.SincronizacaoRepository
import javax.inject.Inject

/**
 * Use Case: Sincronizar dados offline
 * Baixa patrimônios, salas e responsáveis para o banco local
 */
class SincronizarDadosUseCase @Inject constructor(
    private val sincronizacaoRepository: SincronizacaoRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return sincronizacaoRepository.sincronizarTodosDados()
    }
}
