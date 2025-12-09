package com.inventario.mobile.domain.repository

import android.content.Intent
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import java.io.File

/**
 * Interface do repositório para geração e gerenciamento de PDFs
 */
interface PdfRepository {
    
    /**
     * Gera um PDF com a lista de patrimônios
     * 
     * @param patrimonios Lista de patrimônios a incluir no PDF
     * @param sala Sala dos patrimônios
     * @param filter Filtro aplicado (TODOS, COLETADOS, NAO_COLETADOS)
     * @param isOffline Se está em modo offline
     * @return Result com ExportResult em caso de sucesso
     */
    suspend fun generatePdf(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        isOffline: Boolean
    ): Result<ExportResult>
    
    /**
     * Retorna o diretório de exportação de PDFs
     */
    fun getExportDirectory(): File
    
    /**
     * Cria um Intent para compartilhar o PDF
     * 
     * @param filePath Caminho do arquivo PDF
     * @return Intent configurado para compartilhamento
     */
    fun sharePdf(filePath: String): Intent
    
    /**
     * Cria um Intent para abrir o PDF
     * 
     * @param filePath Caminho do arquivo PDF
     * @return Intent configurado para visualização
     */
    fun openPdf(filePath: String): Intent
}
