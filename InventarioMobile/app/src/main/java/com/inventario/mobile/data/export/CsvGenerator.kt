package com.inventario.mobile.data.export

import android.content.Context
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerador de arquivos CSV para relatórios de inventário.
 *
 * ### v2.20.4 (07/05/2026) — Duas correções importantes:
 * 1. **Encoding UTF-8 com BOM**: substitui `FileWriter` (que usava o encoding default do
 *    sistema — CP-1252 no Windows) por `OutputStreamWriter(UTF-8)`. Caracteres acentuados
 *    (`ç`, `ã`, `é`) agora são gravados corretamente. O BOM (`EF BB BF`) ajuda o Excel a
 *    detectar o encoding ao abrir o arquivo.
 * 2. **Data de coleta formatada**: agora usa `dataColetaFormatada` com fallback para
 *    conversão de timestamp numérico, em vez de escrever timestamps crus (`1714392000000`)
 *    quando o campo formatado está null.
 *
 * ### v2.20.4 — Novas colunas de auditoria:
 * Adicionadas para alinhar com XLSX e PDF e dar visibilidade a divergências:
 * - `Coletado por`
 * - `Localização encontrada`
 * - `Estado encontrado`
 *
 * Separador continua sendo `;` (padrão brasileiro para CSV).
 */
@Singleton
class CsvGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val SEPARATOR = ";"
        private const val LINE_BREAK = "\r\n" // CRLF é padrão mais amigável com Excel no Windows

        // BOM UTF-8 como bytes — ajuda o Excel a detectar o encoding automaticamente.
        private val UTF8_BOM_BYTES = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }

    /**
     * Gera um arquivo CSV com a lista de patrimônios.
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

        FileOutputStream(outputFile).use { fos ->
            // BOM UTF-8 como bytes garantidos (antes de abrir o writer de texto)
            fos.write(UTF8_BOM_BYTES)

            BufferedWriter(OutputStreamWriter(fos, Charsets.UTF_8)).use { writer ->
                // Cabeçalho informativo (prefixado com # para Excel ignorar como comentário)
                writer.append("# RELATÓRIO DE INVENTÁRIO$LINE_BREAK")
                writer.append("# Sala: ${sala.nome}$LINE_BREAK")
                writer.append("# Filtro: ${filter.displayName}$LINE_BREAK")
                writer.append("# Data: $currentDate$LINE_BREAK")
                if (isOffline) {
                    writer.append("# MODO OFFLINE - Dados podem estar desatualizados$LINE_BREAK")
                }
                writer.append(LINE_BREAK)

                // Cabeçalho da tabela — mesma ordem do XLSX.
                // v2.20.5: coluna "Status" só aparece quando filtro = TODOS (omissão em
                // COLETADOS/NAO_COLETADOS evita redundância com o cabeçalho do relatório).
                val incluirStatus = filter == ExportFilter.TODOS

                val headers = buildList {
                    add("Nº Patrimônio")
                    add("Descrição")
                    add("Estado")
                    if (incluirStatus) add("Status")
                    add("Data Coleta")
                    add("Coletado por")
                    add("Localização encontrada")
                    add("Estado encontrado")
                    add("Sala")
                    add("Responsável")
                }
                writer.append(headers.joinToString(SEPARATOR))
                writer.append(LINE_BREAK)

                // Dados
                patrimonios.forEach { patrimonio ->
                    val dataColeta = if (patrimonio.coletado) {
                        patrimonio.dataColetaFormatada
                            ?: formatarDataColeta(patrimonio.dataColeta)
                            ?: "-"
                    } else {
                        "-"
                    }

                    val coletadoPor =
                        if (patrimonio.coletado) patrimonio.coletadoPor ?: "-" else "-"
                    val localizacaoEncontrada =
                        if (patrimonio.coletado) patrimonio.localizacaoEncontrada ?: "-" else "-"
                    val estadoEncontrado =
                        if (patrimonio.coletado) patrimonio.estadoEncontrado ?: "-" else "-"

                    val row = buildList {
                        add(escapeCSV(patrimonio.numeroPatrimonio))
                        add(escapeCSV(patrimonio.descricao ?: "-"))
                        add(escapeCSV(patrimonio.estado ?: "-"))
                        if (incluirStatus) {
                            add(if (patrimonio.coletado) "Coletado" else "Pendente")
                        }
                        add(escapeCSV(dataColeta))
                        add(escapeCSV(coletadoPor))
                        add(escapeCSV(localizacaoEncontrada))
                        add(escapeCSV(estadoEncontrado))
                        add(escapeCSV(sala.nome))
                        add(escapeCSV(patrimonio.nomeResponsavel ?: "-"))
                    }
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
                        writer.append(
                            "# Coletados: $coletados " +
                                "(${String.format(Locale("pt", "BR"), "%.1f", percentual)}%)$LINE_BREAK"
                        )
                        writer.append(
                            "# Não Coletados: $naoColetados " +
                                "(${String.format(Locale("pt", "BR"), "%.1f", 100 - percentual)}%)$LINE_BREAK"
                        )
                    }
                    ExportFilter.COLETADOS -> {
                        writer.append("# Total de Itens Coletados: ${patrimonios.size}$LINE_BREAK")
                    }
                    ExportFilter.NAO_COLETADOS -> {
                        writer.append("# Total de Itens Não Coletados: ${patrimonios.size}$LINE_BREAK")
                    }
                }
            }
        }

        // Calcular estatísticas para o ExportResult
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
     * Formata o campo `dataColeta` que pode ser:
     * - Uma string de timestamp numérico (ex: `"1714392000000"`)
     * - Uma string de data já formatada (ex: `"29/04/2026 10:30"`)
     * - `null`
     */
    private fun formatarDataColeta(dataColeta: String?): String? {
        if (dataColeta.isNullOrBlank()) return null
        return try {
            val timestamp = dataColeta.toLong()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            sdf.format(Date(timestamp))
        } catch (e: NumberFormatException) {
            dataColeta
        }
    }

    /**
     * Escapa caracteres especiais para CSV conforme RFC 4180:
     * - Valores contendo separador, aspas ou quebra de linha devem ser envolvidos em aspas.
     * - Aspas internas são escapadas duplicando-as.
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(SEPARATOR) ||
            value.contains("\"") ||
            value.contains("\n") ||
            value.contains("\r")
        ) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
