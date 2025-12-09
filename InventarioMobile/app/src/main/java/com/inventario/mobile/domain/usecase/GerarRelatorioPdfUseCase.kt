package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.ExportConfig
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.domain.repository.PdfRepository
import javax.inject.Inject

/**
 * Use Case para gerar relatório PDF de patrimônios
 */
class GerarRelatorioPdfUseCase @Inject constructor(
    private val pdfRepository: PdfRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    /**
     * Gera um relatório PDF com os patrimônios da sala selecionada
     * 
     * @param config Configuração de exportação (sala, filtro, offline)
     * @return Result com ExportResult em caso de sucesso
     */
    suspend operator fun invoke(config: ExportConfig): Result<ExportResult> {
        return try {
            // 1. Determinar filtro de coleta
            val coletadoFilter: Boolean? = when (config.filter) {
                ExportFilter.TODOS -> null
                ExportFilter.COLETADOS -> true
                ExportFilter.NAO_COLETADOS -> false
            }
            
            // 2. Buscar patrimônios da sala com filtro
            val patrimoniosResult = patrimonioRepository.buscarPorSala(
                salaId = config.sala.id.toInt(),
                coletado = coletadoFilter,
                page = 0,
                pageSize = Int.MAX_VALUE // Buscar todos para exportação
            )
            
            if (patrimoniosResult.isFailure) {
                return Result.failure(
                    patrimoniosResult.exceptionOrNull() 
                        ?: Exception("Erro ao buscar patrimônios")
                )
            }
            
            val patrimonios = patrimoniosResult.getOrNull() ?: emptyList()
            
            // 3. Verificar se há dados para exportar
            if (patrimonios.isEmpty()) {
                return Result.failure(NoDataException("Nenhum patrimônio encontrado com os filtros selecionados"))
            }
            
            // 4. Gerar PDF
            pdfRepository.generatePdf(
                patrimonios = patrimonios,
                sala = config.sala,
                filter = config.filter,
                isOffline = config.isOffline
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Exceção para quando não há dados para exportar
 */
class NoDataException(message: String) : Exception(message)
