package com.inventario.mobile.data.export

import android.content.Context
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.Sala
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Valida que o XLSX gerado é estruturalmente correto (ZIP + entradas obrigatórias
 * do OOXML) e contém os dados esperados, sem abrir o Excel.
 *
 * BUGFIX (v2.20.3): versão anterior gerava TSV com extensão `.xls`. Este teste
 * protege contra regressões — qualquer modificação que quebre a estrutura OOXML
 * vai falhar aqui antes de chegar ao usuário.
 */
class ExcelGeneratorStructureTest : StringSpec({

    val context = mockk<Context>(relaxed = true)
    val generator = ExcelGenerator(context)

    val sala = Sala(
        id = 1L,
        nome = "Laboratório de Informática",
        codigo = "LAB-01",
        setorId = 1L
    )

    val patrimonios = listOf(
        Patrimonio(
            id = 1,
            numeroPatrimonio = "PAT-1001",
            descricao = "Monitor LG 24\"",
            estado = "BOM",
            idSetor = 1,
            idSala = 1,
            qrCode = "QR001",
            coletado = true,
            dataColetaFormatada = "06/05/2026 14:30",
            nomeResponsavel = "João da Silva"
        ),
        Patrimonio(
            id = 2,
            numeroPatrimonio = "PAT-1002",
            descricao = "Teclado & Mouse <USB>",  // caracteres que precisam de escape XML
            estado = "OCIOSO",
            idSetor = 1,
            idSala = 1,
            qrCode = "QR002",
            coletado = false,
            nomeResponsavel = null
        )
    )

    fun gerarArquivo(filter: ExportFilter = ExportFilter.TODOS, isOffline: Boolean = false): File {
        val tempFile = File.createTempFile("test-xlsx-", ".xlsx")
        tempFile.deleteOnExit()
        generator.generate(
            patrimonios = patrimonios,
            sala = sala,
            filter = filter,
            isOffline = isOffline,
            outputFile = tempFile
        )
        return tempFile
    }

    "XLSX — arquivo gerado é um ZIP válido" {
        val file = gerarArquivo()
        file.exists() shouldBe true
        file.length() shouldBe file.length() // não é zero
        (file.length() > 0L) shouldBe true

        // Assinatura do ZIP: "PK\u0003\u0004" nos primeiros 4 bytes
        val header = file.inputStream().use { it.readNBytes(4) }
        header[0].toInt() and 0xFF shouldBe 0x50 // 'P'
        header[1].toInt() and 0xFF shouldBe 0x4B // 'K'
        header[2].toInt() and 0xFF shouldBe 0x03
        header[3].toInt() and 0xFF shouldBe 0x04
    }

    "XLSX — contém todas as entradas OOXML obrigatórias" {
        val file = gerarArquivo()
        ZipFile(file).use { zip ->
            val nomes = zip.entries().toList().map(ZipEntry::getName)
            nomes shouldContainAll listOf(
                "[Content_Types].xml",
                "_rels/.rels",
                "xl/workbook.xml",
                "xl/_rels/workbook.xml.rels",
                "xl/styles.xml",
                "xl/sharedStrings.xml",
                "xl/worksheets/sheet1.xml"
            )
        }
    }

    "XLSX — [Content_Types].xml declara worksheet e styles" {
        val file = gerarArquivo()
        val content = lerEntrada(file, "[Content_Types].xml")
        content shouldContain "spreadsheetml.sheet.main+xml"
        content shouldContain "spreadsheetml.worksheet+xml"
        content shouldContain "spreadsheetml.styles+xml"
        content shouldContain "spreadsheetml.sharedStrings+xml"
    }

    "XLSX — sharedStrings.xml escapa caracteres XML corretamente" {
        val file = gerarArquivo()
        val content = lerEntrada(file, "xl/sharedStrings.xml")
        // "Teclado & Mouse <USB>" deve aparecer escapado
        content shouldContain "Teclado &amp; Mouse &lt;USB&gt;"
        // Nomes dos patrimônios também
        content shouldContain "PAT-1001"
        content shouldContain "PAT-1002"
    }

    "XLSX — cabeçalho inclui colunas de auditoria no filtro TODOS" {
        val file = gerarArquivo(filter = ExportFilter.TODOS)
        val content = lerEntrada(file, "xl/sharedStrings.xml")
        // v2.20.4: colunas de auditoria foram adicionadas
        content shouldContain "Nº Patrimônio"
        content shouldContain "Descrição"
        content shouldContain "Estado"
        content shouldContain "Status" // Status só no filtro TODOS
        content shouldContain "Data Coleta"
        content shouldContain "Coletado por"
        content shouldContain "Localização encontrada"
        content shouldContain "Estado encontrado"
        content shouldContain "Responsável"
    }

    "XLSX — coluna Status é omitida quando filtro = COLETADOS" {
        val file = gerarArquivo(filter = ExportFilter.COLETADOS)
        val sheetContent = lerEntrada(file, "xl/worksheets/sheet1.xml")
        // v2.20.5: sem coluna Status no filtro COLETADOS (redundante com cabeçalho)
        // Contar colunas via declaração <cols> — deve ter 8 em vez de 9.
        val colsCount = Regex("""<col\s""").findAll(sheetContent).count()
        colsCount shouldBe 8
    }

    "XLSX — coluna Status é omitida quando filtro = NAO_COLETADOS" {
        val file = gerarArquivo(filter = ExportFilter.NAO_COLETADOS)
        val sheetContent = lerEntrada(file, "xl/worksheets/sheet1.xml")
        val colsCount = Regex("""<col\s""").findAll(sheetContent).count()
        colsCount shouldBe 8
    }

    "XLSX — coluna Status permanece quando filtro = TODOS" {
        val file = gerarArquivo(filter = ExportFilter.TODOS)
        val sheetContent = lerEntrada(file, "xl/worksheets/sheet1.xml")
        val colsCount = Regex("""<col\s""").findAll(sheetContent).count()
        colsCount shouldBe 9
    }

    "XLSX — worksheet contém referências de células A1 válidas" {
        val file = gerarArquivo()
        val content = lerEntrada(file, "xl/worksheets/sheet1.xml")
        content shouldContain """<row r="1">"""
        content shouldContain """<c r="A1""""
        // Deve ter o elemento sheetData obrigatório
        content shouldContain "<sheetData>"
        content shouldContain "</sheetData>"
        // Declaração de dimension
        content shouldContain """<dimension ref="A1:"""
    }

    "XLSX — modo offline inclui linha de aviso" {
        val file = gerarArquivo(isOffline = true)
        val content = lerEntrada(file, "xl/sharedStrings.xml")
        content shouldContain "OFFLINE"
    }

    "XLSX — filtro COLETADOS gera linha de resumo apropriada" {
        val file = gerarArquivo(filter = ExportFilter.COLETADOS)
        val content = lerEntrada(file, "xl/sharedStrings.xml")
        content shouldContain "Total de Itens Coletados:"
    }

    "XLSX — extensão do arquivo de teste termina com .xlsx" {
        val file = gerarArquivo()
        file.extension shouldBe "xlsx"
    }

    "XLSX — styles.xml contém cellXfs com 7 estilos" {
        val file = gerarArquivo()
        val content = lerEntrada(file, "xl/styles.xml")
        content shouldContain """<cellXfs count="7">"""
        // Verifica que estilos de cor para sucesso/pendente existem
        content shouldContain "FF2E7D32" // verde sucesso
        content shouldContain "FFEF6C00" // laranja pendente
    }
})

private fun lerEntrada(zipFile: File, entryName: String): String {
    ZipFile(zipFile).use { zip ->
        val entry = zip.getEntry(entryName) ?: error("Entrada não encontrada: $entryName")
        return zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).readText()
    }
}
