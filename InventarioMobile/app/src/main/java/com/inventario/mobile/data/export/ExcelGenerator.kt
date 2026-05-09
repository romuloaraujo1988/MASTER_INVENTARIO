package com.inventario.mobile.data.export

import android.content.Context
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerador de arquivos **XLSX reais** (OOXML / Office Open XML) para relatórios de inventário.
 *
 * ### Contexto histórico
 * Versões anteriores (≤ v2.20.0) geravam TSV (Tab-Separated Values) com BOM UTF-8 e extensão
 * `.xls` + MIME `application/vnd.ms-excel`. Isso fazia o Excel mostrar o aviso
 * *"O formato do arquivo e a extensão não correspondem"*, e apps Android como
 * Google Sheets / WPS Office recusavam abrir.
 *
 * ### Solução atual (v2.20.3)
 * Gera um arquivo `.xlsx` real — um container ZIP com os XMLs mínimos que o Excel
 * e todas as planilhas compatíveis com OOXML aceitam sem avisos:
 *
 * ```
 * arquivo.xlsx (ZIP)
 *  ├─ [Content_Types].xml         ← tipos MIME das partes
 *  ├─ _rels/.rels                 ← relacionamento raiz → workbook
 *  ├─ xl/workbook.xml             ← lista de sheets
 *  ├─ xl/_rels/workbook.xml.rels  ← relação workbook → sheet1
 *  ├─ xl/styles.xml               ← estilos: negrito, alinhamento
 *  ├─ xl/sharedStrings.xml        ← tabela de strings compartilhadas
 *  └─ xl/worksheets/sheet1.xml    ← a planilha em si
 * ```
 *
 * Tudo é Kotlin puro — zero dependências externas. APK não cresce, minSdk 23 continua OK.
 *
 * ### Dimensionamento
 * - Strings repetidas são deduplicadas via `sharedStrings.xml` (reduz tamanho em ~40%).
 * - Números são gravados inline como `<c t="n"><v>42</v></c>` (mais eficiente).
 * - Header da tabela usa estilo "bold + background cinza".
 * - Linhas do relatório têm metadados (sala, filtro, data) no topo em linhas `inlineStr`.
 */
@Singleton
class ExcelGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Gera um arquivo XLSX com a lista de patrimônios.
     *
     * @throws IOException se houver falha de I/O — o chamador (ExportRepositoryImpl)
     *                     converte para Result.failure.
     */
    fun generate(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        isOffline: Boolean,
        outputFile: File
    ): ExportResult {
        // Garantir que o diretório pai existe antes de escrever
        outputFile.parentFile?.let { parent ->
            if (!parent.exists() && !parent.mkdirs()) {
                throw IOException("Não foi possível criar o diretório de exportação: ${parent.absolutePath}")
            }
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val currentDate = dateFormat.format(Date())

        val coletados = patrimonios.count { it.coletado }
        val naoColetados = patrimonios.size - coletados
        val percentual = if (patrimonios.isNotEmpty()) {
            (coletados.toDouble() / patrimonios.size) * 100
        } else 0.0

        // Tabela de strings compartilhadas (deduplicação + case-sensitive)
        val sharedStrings = LinkedHashMap<String, Int>()
        fun interned(value: String): Int =
            sharedStrings.getOrPut(value) { sharedStrings.size }

        // Cada célula: (sharedStringIndex, styleIndex) para células de texto
        //              (numberValue, styleIndex)       para células numéricas
        val sheetRows = mutableListOf<List<Cell>>()

        // --- Cabeçalho informativo ---
        sheetRows.add(listOf(Cell.Str(interned("RELATÓRIO DE INVENTÁRIO"), STYLE_TITLE)))
        sheetRows.add(emptyList()) // linha em branco
        sheetRows.add(
            listOf(
                Cell.Str(interned("Sala:"), STYLE_LABEL),
                Cell.Str(interned(sala.nome), STYLE_DEFAULT)
            )
        )
        sheetRows.add(
            listOf(
                Cell.Str(interned("Filtro:"), STYLE_LABEL),
                Cell.Str(interned(filter.displayName), STYLE_DEFAULT)
            )
        )
        sheetRows.add(
            listOf(
                Cell.Str(interned("Data:"), STYLE_LABEL),
                Cell.Str(interned(currentDate), STYLE_DEFAULT)
            )
        )
        if (isOffline) {
            sheetRows.add(
                listOf(
                    Cell.Str(interned("Modo:"), STYLE_LABEL),
                    Cell.Str(
                        interned("OFFLINE - Dados podem estar desatualizados"),
                        STYLE_WARNING
                    )
                )
            )
        }
        sheetRows.add(emptyList())

        // --- Header da tabela (bold) ---
        // v2.20.5: coluna "Status" só aparece quando filtro = TODOS. Para COLETADOS/NAO_COLETADOS
        // ela seria redundante (o filtro já está no cabeçalho do relatório).
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
            add("Responsável")
        }
        sheetRows.add(headers.map { Cell.Str(interned(it), STYLE_HEADER) })

        // --- Dados ---
        patrimonios.forEach { patrimonio ->
            val dataColeta = if (patrimonio.coletado) {
                patrimonio.dataColetaFormatada
                    ?: formatarDataColeta(patrimonio.dataColeta)
                    ?: "-"
            } else {
                "-"
            }
            val statusTexto = if (patrimonio.coletado) "Coletado" else "Pendente"
            val statusStyle = if (patrimonio.coletado) STYLE_SUCCESS else STYLE_PENDING

            // Campos de auditoria — só fazem sentido quando o item foi coletado.
            // Quando não coletado, preenchemos "-" para manter o alinhamento de colunas.
            val coletadoPor = if (patrimonio.coletado) patrimonio.coletadoPor ?: "-" else "-"
            val localizacaoEncontrada =
                if (patrimonio.coletado) patrimonio.localizacaoEncontrada ?: "-" else "-"
            val estadoEncontrado =
                if (patrimonio.coletado) patrimonio.estadoEncontrado ?: "-" else "-"

            sheetRows.add(
                buildList {
                    add(Cell.Str(interned(patrimonio.numeroPatrimonio), STYLE_DEFAULT))
                    add(Cell.Str(interned(patrimonio.descricao ?: "-"), STYLE_DEFAULT))
                    add(Cell.Str(interned(patrimonio.estado ?: "-"), STYLE_DEFAULT))
                    if (incluirStatus) add(Cell.Str(interned(statusTexto), statusStyle))
                    add(Cell.Str(interned(dataColeta), STYLE_DEFAULT))
                    add(Cell.Str(interned(coletadoPor), STYLE_DEFAULT))
                    add(Cell.Str(interned(localizacaoEncontrada), STYLE_DEFAULT))
                    add(Cell.Str(interned(estadoEncontrado), STYLE_DEFAULT))
                    add(Cell.Str(interned(patrimonio.nomeResponsavel ?: "-"), STYLE_DEFAULT))
                }
            )
        }

        // --- Resumo ---
        sheetRows.add(emptyList())
        sheetRows.add(listOf(Cell.Str(interned("RESUMO"), STYLE_TITLE)))

        when (filter) {
            ExportFilter.TODOS -> {
                sheetRows.add(
                    listOf(
                        Cell.Str(interned("Total de Itens:"), STYLE_LABEL),
                        Cell.Num(patrimonios.size.toDouble(), STYLE_DEFAULT)
                    )
                )
                sheetRows.add(
                    listOf(
                        Cell.Str(interned("Coletados:"), STYLE_LABEL),
                        Cell.Num(coletados.toDouble(), STYLE_DEFAULT),
                        Cell.Str(interned(String.format(Locale("pt", "BR"), "(%.1f%%)", percentual)), STYLE_DEFAULT)
                    )
                )
                sheetRows.add(
                    listOf(
                        Cell.Str(interned("Não Coletados:"), STYLE_LABEL),
                        Cell.Num(naoColetados.toDouble(), STYLE_DEFAULT),
                        Cell.Str(
                            interned(String.format(Locale("pt", "BR"), "(%.1f%%)", 100 - percentual)),
                            STYLE_DEFAULT
                        )
                    )
                )
            }
            ExportFilter.COLETADOS -> {
                sheetRows.add(
                    listOf(
                        Cell.Str(interned("Total de Itens Coletados:"), STYLE_LABEL),
                        Cell.Num(patrimonios.size.toDouble(), STYLE_DEFAULT)
                    )
                )
            }
            ExportFilter.NAO_COLETADOS -> {
                sheetRows.add(
                    listOf(
                        Cell.Str(interned("Total de Itens Não Coletados:"), STYLE_LABEL),
                        Cell.Num(patrimonios.size.toDouble(), STYLE_DEFAULT)
                    )
                )
            }
        }

        // --- Gravar o ZIP XLSX ---
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zip ->
            writeEntry(zip, "[Content_Types].xml", CONTENT_TYPES_XML)
            writeEntry(zip, "_rels/.rels", ROOT_RELS_XML)
            writeEntry(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
            writeEntry(zip, "xl/workbook.xml", WORKBOOK_XML)
            writeEntry(zip, "xl/styles.xml", STYLES_XML)
            writeEntry(zip, "xl/sharedStrings.xml", buildSharedStringsXml(sharedStrings))
            writeEntry(zip, "xl/worksheets/sheet1.xml", buildSheetXml(sheetRows, incluirStatus))
        }

        return ExportResult(
            filePath = outputFile.absolutePath,
            fileName = outputFile.name,
            totalItems = patrimonios.size,
            coletados = coletados,
            naoColetados = naoColetados,
            percentualColeta = percentual
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Construção dos XMLs
    // ─────────────────────────────────────────────────────────────────────

    private fun writeEntry(zip: ZipOutputStream, path: String, content: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildSharedStringsXml(strings: Map<String, Int>): String {
        val sb = StringBuilder(8192)
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append(
            """<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" """
        )
        sb.append("""count="${strings.size}" uniqueCount="${strings.size}">""")
        for ((value, _) in strings) {
            // `xml:space="preserve"` garante que espaços iniciais/finais sejam mantidos
            sb.append("""<si><t xml:space="preserve">""")
            sb.append(escapeXml(value))
            sb.append("""</t></si>""")
        }
        sb.append("""</sst>""")
        return sb.toString()
    }

    private fun buildSheetXml(rows: List<List<Cell>>, incluirStatus: Boolean): String {
        val sb = StringBuilder(16384)
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append(
            """<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">"""
        )
        // Definir larguras de colunas para melhor legibilidade.
        // v2.20.5: quando o filtro é COLETADOS/NAO_COLETADOS, a coluna "Status" é omitida.
        sb.append("""<cols>""")
        var col = 1
        sb.append("""<col min="${col}" max="${col}" width="16" customWidth="1"/>""") ; col++ // Nº Patrimônio
        sb.append("""<col min="${col}" max="${col}" width="40" customWidth="1"/>""") ; col++ // Descrição
        sb.append("""<col min="${col}" max="${col}" width="14" customWidth="1"/>""") ; col++ // Estado
        if (incluirStatus) {
            sb.append("""<col min="${col}" max="${col}" width="12" customWidth="1"/>""") ; col++ // Status
        }
        sb.append("""<col min="${col}" max="${col}" width="18" customWidth="1"/>""") ; col++ // Data Coleta
        sb.append("""<col min="${col}" max="${col}" width="24" customWidth="1"/>""") ; col++ // Coletado por
        sb.append("""<col min="${col}" max="${col}" width="28" customWidth="1"/>""") ; col++ // Localização encontrada
        sb.append("""<col min="${col}" max="${col}" width="18" customWidth="1"/>""") ; col++ // Estado encontrado
        sb.append("""<col min="${col}" max="${col}" width="24" customWidth="1"/>""")          // Responsável
        sb.append("""</cols>""")

        sb.append("""<sheetData>""")
        rows.forEachIndexed { rowIndex, cells ->
            val rowNum = rowIndex + 1
            sb.append("""<row r="$rowNum">""")
            cells.forEachIndexed { colIndex, cell ->
                val ref = cellRef(colIndex, rowNum)
                when (cell) {
                    is Cell.Str -> {
                        // t="s" = shared string index
                        sb.append("""<c r="$ref" s="${cell.style}" t="s"><v>${cell.sharedIndex}</v></c>""")
                    }
                    is Cell.Num -> {
                        // t omitido = número (default)
                        sb.append("""<c r="$ref" s="${cell.style}"><v>${formatNumber(cell.value)}</v></c>""")
                    }
                }
            }
            sb.append("""</row>""")
        }
        sb.append("""</sheetData>""")

        // Evita que o Excel reclame de falta do `dimension`
        val lastRow = rows.size
        val lastCol = rows.maxOfOrNull { it.size } ?: 1
        sb.append("""<pageMargins left="0.7" right="0.7" top="0.75" bottom="0.75" header="0.3" footer="0.3"/>""")
        sb.append("""</worksheet>""")

        // Re-inserir <dimension> antes de <cols> para metadados precisos, mantendo XML válido
        val dimension = """<dimension ref="A1:${cellRef(lastCol - 1, lastRow)}"/>"""
        val insertionPoint = sb.indexOf("<cols>")
        if (insertionPoint > 0) {
            sb.insert(insertionPoint, dimension)
        }

        return sb.toString()
    }

    private fun cellRef(col0Indexed: Int, row1Indexed: Int): String =
        "${colLetter(col0Indexed)}$row1Indexed"

    /**
     * Converte índice zero-based para letras da coluna Excel.
     * 0 -> A, 1 -> B, ..., 25 -> Z, 26 -> AA, ...
     */
    private fun colLetter(col0Indexed: Int): String {
        var n = col0Indexed
        val sb = StringBuilder()
        while (n >= 0) {
            sb.insert(0, ('A' + (n % 26)))
            n = n / 26 - 1
        }
        return sb.toString()
    }

    /**
     * Formata números para XLSX usando `.` como separador decimal
     * (OOXML é `en-US`, independente do locale do sistema).
     */
    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%s", value)
        }
    }

    /**
     * Escapa caracteres reservados do XML em strings de texto.
     * Note: aspas e apóstrofos só precisam ser escapados dentro de atributos,
     * mas fazemos por segurança.
     */
    private fun escapeXml(value: String): String {
        if (value.isEmpty()) return value
        val sb = StringBuilder(value.length + 16)
        for (ch in value) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&apos;")
                // Remover caracteres de controle que o XML 1.0 não aceita
                in '\u0000'..'\u0008', '\u000B', '\u000C', in '\u000E'..'\u001F' -> {
                    // silenciosamente descartado
                }
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

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

    // ─────────────────────────────────────────────────────────────────────
    // Modelos internos de célula
    // ─────────────────────────────────────────────────────────────────────

    private sealed class Cell {
        abstract val style: Int

        data class Str(val sharedIndex: Int, override val style: Int) : Cell()
        data class Num(val value: Double, override val style: Int) : Cell()
    }

    companion object {
        // Índices dos estilos em xl/styles.xml (`<cellXfs>`)
        private const val STYLE_DEFAULT = 0
        private const val STYLE_HEADER = 1
        private const val STYLE_TITLE = 2
        private const val STYLE_LABEL = 3
        private const val STYLE_SUCCESS = 4
        private const val STYLE_PENDING = 5
        private const val STYLE_WARNING = 6

        // ─── XMLs estáticos (não variam por arquivo) ───────────────────

        private val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>"""

        private val ROOT_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        private val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>"""

        private val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Relatório" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

        /**
         * Estilos mínimos para um XLSX válido:
         * - 2 fontes (default, bold)
         * - 4 fills (default, headerGray, successGreen, pendingOrange)
         * - 1 border default
         * - 7 cellXfs correspondendo aos STYLE_* declarados acima
         *
         * Cores em RGB hex ARGB (FF = alpha opaco).
         */
        private val STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="4">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/><color rgb="FFFFFFFF"/></font>
    <font><b/><sz val="14"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/></font>
  </fonts>
  <fills count="5">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF2E7D32"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFEF6C00"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFFC107"/><bgColor indexed="64"/></patternFill></fill>
  </fills>
  <borders count="1">
    <border><left/><right/><top/><bottom/><diagonal/></border>
  </borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="7">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    <xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="center"/></xf>
    <xf numFmtId="0" fontId="1" fillId="3" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="center"/></xf>
    <xf numFmtId="0" fontId="3" fillId="4" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
  </cellXfs>
  <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
  <dxfs count="0"/>
  <tableStyles count="0" defaultTableStyle="TableStyleMedium2" defaultPivotStyle="PivotStyleLight16"/>
</styleSheet>"""
    }
}
