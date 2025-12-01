package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.SalaComProgresso
import com.inventario.mobile.domain.repository.SalaRepository
import javax.inject.Inject

/**
 * Use Case para buscar todas as salas com informações de progresso de coleta.
 * Retorna lista ordenada por nome.
 * 
 * Regra: Contém apenas lógica de negócio, sem dependências Android.
 */
class BuscarSalasComProgressoUseCase @Inject constructor(
    private val salaRepository: SalaRepository
) {
    
    /**
     * Busca todas as salas com progresso de coleta.
     * 
     * @return Result com lista de salas com progresso ou erro
     */
    suspend operator fun invoke(): Result<List<SalaComProgresso>> {
        return try {
            val result = salaRepository.buscarComProgresso()
            
            if (result.isSuccess) {
                // Ordenar por nome (já deve vir ordenado do DAO, mas garantir)
                val salas = result.getOrDefault(emptyList())
                    .sortedBy { it.nome.lowercase() }
                
                Result.success(salas)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca salas com progresso filtradas por query de busca.
     * 
     * @param query Termo de busca (nome ou número)
     * @return Result com lista filtrada de salas com progresso ou erro
     */
    suspend fun buscarComFiltro(query: String): Result<List<SalaComProgresso>> {
        return try {
            val result = salaRepository.buscarComProgresso()
            
            if (result.isSuccess) {
                val salas = result.getOrDefault(emptyList())
                
                // Filtrar por query se não estiver vazia
                val salasFiltradas = if (query.isBlank()) {
                    salas
                } else {
                    val queryLower = query.lowercase()
                    salas.filter { sala ->
                        sala.nome.lowercase().contains(queryLower) ||
                        sala.numero?.lowercase()?.contains(queryLower) == true ||
                        sala.id.toString().contains(queryLower)
                    }
                }
                
                // Ordenar por nome
                Result.success(salasFiltradas.sortedBy { it.nome.lowercase() })
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca salas com progresso ordenadas por percentual de coleta.
     * 
     * @param crescente Se true, ordena do menor para maior percentual
     * @return Result com lista ordenada de salas com progresso ou erro
     */
    suspend fun buscarOrdenadasPorProgresso(crescente: Boolean = true): Result<List<SalaComProgresso>> {
        return try {
            val result = salaRepository.buscarComProgresso()
            
            if (result.isSuccess) {
                val salas = result.getOrDefault(emptyList())
                
                val salasOrdenadas = if (crescente) {
                    salas.sortedBy { it.percentualColeta }
                } else {
                    salas.sortedByDescending { it.percentualColeta }
                }
                
                Result.success(salasOrdenadas)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
