package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import javax.inject.Inject

/**
 * Use Case para buscar patrimônios de uma sala para exportação
 */
class BuscarPatrimoniosPorSalaParaExportacaoUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    /**
     * Busca patrimônios de uma sala aplicando o filtro de exportação
     * 
     * @param salaId ID da sala
     * @param filter Filtro de exportação (TODOS, COLETADOS, NAO_COLETADOS)
     * @return Result com lista de patrimônios filtrados
     */
    suspend operator fun invoke(salaId: Int, filter: ExportFilter): Result<List<Patrimonio>> {
        return try {
            val coletadoFilter: Boolean? = when (filter) {
                ExportFilter.TODOS -> null
                ExportFilter.COLETADOS -> true
                ExportFilter.NAO_COLETADOS -> false
            }
            
            // Buscar todos os patrimônios da sala (sem paginação para exportação)
            val result = patrimonioRepository.buscarPorSala(
                salaId = salaId,
                coletado = coletadoFilter,
                page = 0,
                pageSize = Int.MAX_VALUE // Buscar todos para exportação
            )
            
            result.map { patrimonios ->
                patrimonios.sortedBy { it.numeroPatrimonio }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        /**
         * Aplica o filtro de exportação em uma lista de patrimônios
         * Útil para testes e filtragem local
         */
        fun applyFilter(patrimonios: List<Patrimonio>, filter: ExportFilter): List<Patrimonio> {
            return when (filter) {
                ExportFilter.TODOS -> patrimonios
                ExportFilter.COLETADOS -> patrimonios.filter { it.coletado }
                ExportFilter.NAO_COLETADOS -> patrimonios.filter { !it.coletado }
            }
        }
    }
}
