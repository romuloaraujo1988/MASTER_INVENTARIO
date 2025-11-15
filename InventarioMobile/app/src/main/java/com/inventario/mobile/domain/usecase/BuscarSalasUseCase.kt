package com.inventario.mobile.domain.usecase

import androidx.paging.PagingData
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.SalaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case: Buscar salas com paginação
 * 
 * Retorna Flow de PagingData para uso com Paging 3
 */
class BuscarSalasUseCase @Inject constructor(
    private val salaRepository: SalaRepository
) {
    /**
     * Busca salas com paginação
     * 
     * @param query Termo de busca (vazio para listar todas)
     * @return Flow de PagingData com salas
     */
    operator fun invoke(query: String): Flow<PagingData<Sala>> {
        return salaRepository.getSalasPaginadas(query)
    }
}
