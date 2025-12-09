package com.inventario.mobile.domain.repository

import android.content.Intent
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import java.io.File

/**
 * Interface do repositório para exportação de relatórios
 */
interface ExportRepository {
    
    /**
     * Gera um relatório no formato especificado
     * 
     * @param patrimonios Lista de patrimônios a incluir
     * @param sala Sala dos patrimônios
     * @param filter Filtro aplicado (TODOS, COLETADOS, NAO_COLETADOS)
     * @param format Formato de exportação (PDF, EXCEL, CSV)
     * @param isOffline Se está em modo offline
     * @return Result com ExportResult em caso de sucesso
     */
    suspend fun generateReport(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        format: ExportFormat,
        isOffline: Boolean
    ): Result<ExportResult>
    
    /**
     * Retorna o diretório de exportação
     */
    fun getExportDirectory(): File
    
    /**
     * Cria um Intent para compartilhar o arquivo
     * 
     * @param filePath Caminho do arquivo
     * @param format Formato do arquivo
     * @return Intent configurado para compartilhamento
     */
    fun shareFile(filePath: String, format: ExportFormat): Intent
    
    /**
     * Cria um Intent para abrir o arquivo
     * 
     * @param filePath Caminho do arquivo
     * @param format Formato do arquivo
     * @return Intent configurado para visualização
     */
    fun openFile(filePath: String, format: ExportFormat): Intent
    
    /**
     * Lista arquivos exportados
     * 
     * @return Lista de arquivos no diretório de exportação
     */
    fun listExportedFiles(): List<File>
    
    /**
     * Exclui um arquivo exportado
     * 
     * @param filePath Caminho do arquivo
     * @return true se excluído com sucesso
     */
    fun deleteFile(filePath: String): Boolean
}
