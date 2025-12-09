package com.inventario.mobile.data.pdf

import android.content.Context
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.PdfSummary
import com.inventario.mobile.domain.model.Sala
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerador de PDFs para relatórios de inventário
 */
@Singleton
class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // Cores do tema
        private val HEADER_COLOR = DeviceRgb(0, 100, 0) // Verde escuro
        private val HEADER_TEXT_COLOR = ColorConstants.WHITE
        private val ALTERNATE_ROW_COLOR = DeviceRgb(240, 240, 240) // Cinza claro
        
        // Tamanhos de fonte
        private const val TITLE_FONT_SIZE = 18f
        private const val SUBTITLE_FONT_SIZE = 12f
        private const val TABLE_HEADER_FONT_SIZE = 10f
        private const val TABLE_CONTENT_FONT_SIZE = 9f
        private const val SUMMARY_FONT_SIZE = 11f
        
        // Margens
        private const val MARGIN = 36f
    }
    
    /**
     * Gera um PDF com a lista de patrimônios
     * 
     * @param patrimonios Lista de patrimônios
     * @param sala Sala dos patrimônios
     * @param filter Filtro aplicado
     * @param isOffline Se está em modo offline
     * @param outputFile Arquivo de saída
     * @return PdfSummary com estatísticas
     */
    fun generate(
        patrimonios: List<Patrimonio>,
        sala: Sala,
        filter: ExportFilter,
        isOffline: Boolean,
        outputFile: File
    ): PdfSummary {
        val summary = PdfSummary.fromPatrimonios(patrimonios)
        
        val writer = PdfWriter(outputFile)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)
        
        document.setMargins(MARGIN, MARGIN, MARGIN + 20, MARGIN) // Extra margin for footer
        
        // Adicionar handler para footer com numeração de páginas
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, FooterEventHandler(pdfDoc))
        
        try {
            // Header
            addHeader(document, sala, filter, isOffline)
            
            // Tabela de patrimônios
            addPatrimoniosTable(document, patrimonios)
            
            // Resumo/Somatório
            addSummary(document, summary, filter)
            
        } finally {
            document.close()
        }
        
        return summary
    }
    
    private fun addHeader(
        document: Document,
        sala: Sala,
        filter: ExportFilter,
        isOffline: Boolean
    ) {
        // Tentar adicionar logo
        try {
            val logoStream = context.assets.open("images/logo_ifmt.png")
            val logoBytes = logoStream.readBytes()
            logoStream.close()
            
            val imageData = ImageDataFactory.create(logoBytes)
            val logo = Image(imageData)
            logo.setWidth(100f)
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER)
            document.add(logo)
        } catch (e: Exception) {
            // Logo não encontrado, continuar sem
        }
        
        // Título
        val title = Paragraph("RELATÓRIO DE INVENTÁRIO")
            .setFontSize(TITLE_FONT_SIZE)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10f)
        document.add(title)
        
        // Subtítulo com informações
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val currentDate = dateFormat.format(Date())
        
        val subtitle = Paragraph()
            .add("Sala: ${sala.nome}\n")
            .add("Filtro: ${filter.displayName}\n")
            .add("Data de Geração: $currentDate")
            .setFontSize(SUBTITLE_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10f)
        document.add(subtitle)
        
        // Watermark se offline
        if (isOffline) {
            val offlineWarning = Paragraph("⚠️ MODO OFFLINE - Dados podem estar desatualizados")
                .setFontSize(10f)
                .setFontColor(DeviceRgb(255, 140, 0)) // Laranja
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15f)
            document.add(offlineWarning)
        }
        
        // Linha separadora
        document.add(Paragraph("").setMarginBottom(10f))
    }
    
    private fun addPatrimoniosTable(document: Document, patrimonios: List<Patrimonio>) {
        // Definir larguras das colunas (proporcionais)
        val columnWidths = floatArrayOf(1.5f, 3f, 1f, 1f, 1.5f)
        val table = Table(UnitValue.createPercentArray(columnWidths))
            .useAllAvailableWidth()
        
        // Header da tabela
        addTableHeader(table)
        
        // Dados
        patrimonios.forEachIndexed { index, patrimonio ->
            val isAlternate = index % 2 == 1
            addPatrimonioRow(table, patrimonio, isAlternate)
        }
        
        document.add(table)
    }
    
    private fun addTableHeader(table: Table) {
        val headers = listOf("Nº Patrimônio", "Descrição", "Estado", "Status", "Data Coleta")
        
        headers.forEach { header ->
            val cell = Cell()
                .add(Paragraph(header).setFontSize(TABLE_HEADER_FONT_SIZE).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(HEADER_TEXT_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(5f)
            table.addHeaderCell(cell)
        }
    }
    
    private fun addPatrimonioRow(table: Table, patrimonio: Patrimonio, isAlternate: Boolean) {
        val bgColor = if (isAlternate) ALTERNATE_ROW_COLOR else ColorConstants.WHITE
        
        // Número do Patrimônio
        table.addCell(createCell(patrimonio.numeroPatrimonio, bgColor))
        
        // Descrição (truncar se muito longa)
        val descricao = patrimonio.descricao?.take(50) ?: "-"
        table.addCell(createCell(descricao, bgColor, TextAlignment.LEFT))
        
        // Estado
        table.addCell(createCell(patrimonio.estado ?: "-", bgColor))
        
        // Status de Coleta
        val status = if (patrimonio.coletado) "✓" else "✗"
        val statusColor = if (patrimonio.coletado) DeviceRgb(0, 128, 0) else DeviceRgb(200, 0, 0)
        table.addCell(createCell(status, bgColor).setFontColor(statusColor))
        
        // Data de Coleta
        val dataColeta = if (patrimonio.coletado) {
            patrimonio.dataColetaFormatada ?: patrimonio.dataColeta ?: "-"
        } else {
            "-"
        }
        table.addCell(createCell(dataColeta, bgColor))
    }
    
    private fun createCell(
        text: String,
        bgColor: com.itextpdf.kernel.colors.Color,
        alignment: TextAlignment = TextAlignment.CENTER
    ): Cell {
        return Cell()
            .add(Paragraph(text).setFontSize(TABLE_CONTENT_FONT_SIZE))
            .setBackgroundColor(bgColor)
            .setTextAlignment(alignment)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setPadding(4f)
    }
    
    private fun addSummary(document: Document, summary: PdfSummary, filter: ExportFilter) {
        document.add(Paragraph("").setMarginTop(20f))
        
        // Título do resumo
        val summaryTitle = Paragraph("RESUMO")
            .setFontSize(SUMMARY_FONT_SIZE + 2)
            .setBold()
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(5f)
        document.add(summaryTitle)
        
        // Linha separadora
        val separator = Paragraph("─".repeat(50))
            .setFontSize(8f)
            .setMarginBottom(5f)
        document.add(separator)
        
        // Conteúdo do resumo baseado no filtro
        when (filter) {
            ExportFilter.TODOS -> {
                document.add(createSummaryLine("Total de Itens:", summary.totalItems.toString()))
                document.add(createSummaryLine("Coletados:", "${summary.coletados} (${String.format("%.1f", summary.percentualColeta)}%)"))
                document.add(createSummaryLine("Não Coletados:", "${summary.naoColetados} (${String.format("%.1f", 100 - summary.percentualColeta)}%)"))
            }
            ExportFilter.COLETADOS -> {
                document.add(createSummaryLine("Total de Itens Coletados:", summary.totalItems.toString()))
            }
            ExportFilter.NAO_COLETADOS -> {
                document.add(createSummaryLine("Total de Itens Não Coletados:", summary.totalItems.toString()))
            }
        }
        
        // Percentual de coleta (sempre mostrar para TODOS)
        if (filter == ExportFilter.TODOS) {
            document.add(Paragraph("").setMarginTop(5f))
            val percentualText = "Percentual de Coleta: ${String.format("%.1f", summary.percentualColeta)}%"
            val percentualParagraph = Paragraph(percentualText)
                .setFontSize(SUMMARY_FONT_SIZE)
                .setBold()
                .setFontColor(HEADER_COLOR)
            document.add(percentualParagraph)
        }
    }
    
    private fun createSummaryLine(label: String, value: String): Paragraph {
        return Paragraph()
            .add(label)
            .add(" ")
            .add(value)
            .setFontSize(SUMMARY_FONT_SIZE)
    }
    
    /**
     * Event handler para adicionar footer com numeração de páginas
     */
    private class FooterEventHandler(private val pdfDoc: PdfDocument) : IEventHandler {
        override fun handleEvent(event: Event) {
            val docEvent = event as PdfDocumentEvent
            val page = docEvent.page
            val pageNumber = pdfDoc.getPageNumber(page)
            val totalPages = pdfDoc.numberOfPages
            
            val canvas = PdfCanvas(page)
            val pageSize = page.pageSize
            
            // Texto do footer
            val footerText = "Página $pageNumber de $totalPages"
            
            canvas.beginText()
            canvas.setFontAndSize(
                PdfFontFactory.createFont(),
                9f
            )
            canvas.moveText(
                (pageSize.width / 2 - 30).toDouble(),
                20.0
            )
            canvas.showText(footerText)
            canvas.endText()
            canvas.release()
        }
    }
}
