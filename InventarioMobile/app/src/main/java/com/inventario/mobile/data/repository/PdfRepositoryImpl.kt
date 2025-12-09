package com.inventario.mobile.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.inventario.mobile.data.pdf.PdfGenerator
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.PdfRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de PDF
 */
@Singleton
class PdfRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfGenerator: PdfGenerator
) : PdfRepository {
    
    companion object {
        private const val EXPORT_DIR = "exports"
        private const val FILE_PROVIDER_AUTHORITY = "com.inventario.mobile.fileprovider"
        private const val PDF_MIME_TYPE = "application/pdf"
    }
    
    override suspend fun generatePdf(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        isOffline: Boolean
    ): Result<ExportResult> {
        return try {
            // Criar diretório de exportação se não existir
            val exportDir = getExportDirectory()
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            // Gerar nome do arquivo
            val fileName = generateFileName(sala, filter)
            val outputFile = File(exportDir, fileName)
            
            // Gerar PDF
            val summary = pdfGenerator.generate(
                patrimonios = patrimonios,
                sala = sala,
                filter = filter,
                isOffline = isOffline,
                outputFile = outputFile
            )
            
            // Criar resultado
            val result = ExportResult(
                filePath = outputFile.absolutePath,
                fileName = fileName,
                totalItems = summary.totalItems,
                coletados = summary.coletados,
                naoColetados = summary.naoColetados,
                percentualColeta = summary.percentualColeta
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getExportDirectory(): File {
        return File(context.getExternalFilesDir(null), EXPORT_DIR)
    }
    
    override fun sharePdf(filePath: String): Intent {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        
        return Intent(Intent.ACTION_SEND).apply {
            type = PDF_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Inventário - ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    override fun openPdf(filePath: String): Intent {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, PDF_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    /**
     * Gera nome do arquivo baseado na sala e filtro
     */
    private fun generateFileName(sala: Sala, filter: ExportFilter): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        
        // Sanitizar nome da sala (remover caracteres especiais)
        val salaNome = sala.nome
            .replace(Regex("[^a-zA-Z0-9]"), "_")
            .take(20)
        
        val filterSuffix = when (filter) {
            ExportFilter.TODOS -> "todos"
            ExportFilter.COLETADOS -> "coletados"
            ExportFilter.NAO_COLETADOS -> "nao_coletados"
        }
        
        return "inventario_${salaNome}_${filterSuffix}_$timestamp.pdf"
    }
}
