package com.inventario.mobile.data.export

import android.content.Context
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerador de arquivos CSV para relatórios de inventário
 */
@Singleton
class CsvGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val SEPARATOR = ";"
        private const val LINE_BREAK = "\n"
    }
    
    /**
     * Gera um arquivo CSV com a lista de patrimônios
     * 
     * @param patrimonios Lista de patrimônios
     * @param sala Sala dos patrimônios
     * @param filter Filtro aplicado
     * @param isOffline Se está em modo offline
     * @param outputFile Arquivo de saída
     * @return ExportResult com estatísticas
     */
    fun generate(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        isOffline: Boolean,
        outputFile: File
    ): ExportResult {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val currentDate = dateFormat.format(Date())
        
        FileWriter(outputFile).use { writer ->
            // Cabeçalho com informações
            writer.append("# RELATÓRIO DE INVENTÁRIO$LINE_BREAK")
            writer.append("# Sala: ${sala.nome}$LINE_BREAK")
            writer.append("# Filtro: ${filter.displayName}$LINE_BREAK")
            writer.append("# Data: $currentDate$LINE_BREAK")
            if (isOffline) {
                writer.append("# MODO OFFLINE - Dados podem estar desatualizados$LINE_BREAK")
            }
            writer.append(LINE_BREAK)
            
            // Cabeçalho da tabela
            val headers = listOf(
                "Nº Patrimônio",
                "Descrição",
                "Estado",
                "Status",
                "Data Coleta",
                "Sala",
                "Responsável"
            )
            writer.append(headers.joinToString(SEPARATOR))
            writer.append(LINE_BREAK)
            
            // Dados
            patrimonios.forEach { patrimonio ->
                val row = listOf(
                    patrimonio.numeroPatrimonio,
                    escapeCSV(patrimonio.descricao ?: "-"),
                    patrimonio.estado ?: "-",
                    if (patrimonio.coletado) "Coletado" else "Pendente",
                    if (patrimonio.coletado) patrimonio.dataColetaFormatada ?: patrimonio.dataColeta ?: "-" else "-",
                    escapeCSV(sala.nome),
                    escapeCSV(patrimonio.nomeResponsavel ?: "-")
                )
                writer.append(row.joinToString(SEPARATOR))
                writer.append(LINE_BREAK)
            }
            
            // Resumo
            writer.append(LINE_BREAK)
            writer.append("# RESUMO$LINE_BREAK")
            
            val coletados = patrimonios.count { it.coletado }
            val naoColetados = patrimonios.size - coletados
            val percentual = if (patrimonios.isNotEmpty()) {
                (coletados.toDouble() / patrimonios.size) * 100
            } else 0.0
            
            when (filter) {
                ExportFilter.TODOS -> {
                    writer.append("# Total de Itens: ${patrimonios.size}$LINE_BREAK")
                    writer.append("# Coletados: $coletados (${String.format("%.1f", percentual)}%)$LINE_BREAK")
                    writer.append("# Não Coletados: $naoColetados (${String.format("%.1f", 100 - percentual)}%)$LINE_BREAK")
                }
                ExportFilter.COLETADOS -> {
                    writer.append("# Total de Itens Coletados: ${patrimonios.size}$LINE_BREAK")
                }
                ExportFilter.NAO_COLETADOS -> {
                    writer.append("# Total de Itens Não Coletados: ${patrimonios.size}$LINE_BREAK")
                }
            }
        }
        
        // Calcular estatísticas
        val coletados = patrimonios.count { it.coletado }
        val naoColetados = patrimonios.size - coletados
        val percentual = if (patrimonios.isNotEmpty()) {
            (coletados.toDouble() / patrimonios.size) * 100
        } else 0.0
        
        return ExportResult(
            filePath = outputFile.absolutePath,
            fileName = outputFile.name,
            totalItems = patrimonios.size,
            coletados = coletados,
            naoColetados = naoColetados,
            percentualColeta = percentual
        )
    }
    
    /**
     * Escapa caracteres especiais para CSV
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(SEPARATOR) || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
