package com.inventario.mobile.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.inventario.mobile.data.export.CsvGenerator
import com.inventario.mobile.data.export.ExcelGenerator
import com.inventario.mobile.data.pdf.PdfGenerator
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.ExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de exportação
 */
@Singleton
class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfGenerator: PdfGenerator,
    private val excelGenerator: ExcelGenerator,
    private val csvGenerator: CsvGenerator
) : ExportRepository {

    companion object {
        private const val EXPORT_DIR = "exports"
    }

    /**
     * Authority do FileProvider derivada de `context.packageName` — evita divergência
     * entre debug/release caso alguém adicione `applicationIdSuffix` no futuro. O manifesto
     * declara `android:authorities="${applicationId}.fileprovider"` (ver AndroidManifest.xml),
     * portanto `packageName + ".fileprovider"` é sempre consistente.
     */
    private val fileProviderAuthority: String
        get() = "${context.packageName}.fileprovider"

    override suspend fun generateReport(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        format: ExportFormat,
        isOffline: Boolean
    ): Result<ExportResult> {
        return try {
            // v2.20.3: validar criação do diretório — antes apenas chamávamos mkdirs()
            // e ignorávamos o retorno, gerando FileNotFoundException opaco se falhava.
            val exportDir = getExportDirectory()
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                return Result.failure(
                    java.io.IOException(
                        "Não foi possível criar o diretório de exportação: ${exportDir.absolutePath}"
                    )
                )
            }

            // Gerar nome do arquivo
            val fileName = generateFileName(sala, filter, format)
            val outputFile = File(exportDir, fileName)
            
            // Gerar arquivo no formato apropriado
            val result = when (format) {
                ExportFormat.PDF -> {
                    val summary = pdfGenerator.generate(
                        patrimonios = patrimonios,
                        sala = sala,
                        filter = filter,
                        isOffline = isOffline,
                        outputFile = outputFile
                    )
                    ExportResult(
                        filePath = outputFile.absolutePath,
                        fileName = fileName,
                        totalItems = summary.totalItems,
                        coletados = summary.coletados,
                        naoColetados = summary.naoColetados,
                        percentualColeta = summary.percentualColeta
                    )
                }
                ExportFormat.EXCEL -> {
                    excelGenerator.generate(
                        patrimonios = patrimonios,
                        sala = sala,
                        filter = filter,
                        isOffline = isOffline,
                        outputFile = outputFile
                    )
                }
                ExportFormat.CSV -> {
                    csvGenerator.generate(
                        patrimonios = patrimonios,
                        sala = sala,
                        filter = filter,
                        isOffline = isOffline,
                        outputFile = outputFile
                    )
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getExportDirectory(): File {
        // getExternalFilesDir pode retornar null se armazenamento externo não estiver disponível
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(baseDir, EXPORT_DIR)
    }
    
    override fun shareFile(filePath: String, format: ExportFormat): Intent {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, fileProviderAuthority, file)

        return Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Inventário - ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun openFile(filePath: String, format: ExportFormat): Intent {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, fileProviderAuthority, file)

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, format.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    override fun listExportedFiles(): List<File> {
        val exportDir = getExportDirectory()
        return if (exportDir.exists()) {
            exportDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    override fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gera nome do arquivo baseado na sala, filtro e formato
     */
    private fun generateFileName(sala: Sala, filter: ExportFilter, format: ExportFormat): String {
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
        
        return "inventario_${salaNome}_${filterSuffix}_$timestamp.${format.extension}"
    }
}
