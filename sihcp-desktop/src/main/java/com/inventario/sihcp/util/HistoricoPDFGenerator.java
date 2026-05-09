package com.inventario.sihcp.util;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.dto.EstatisticasHistoricoDTO;
import com.inventario.sihcp.dto.HistoricoColetaDTO;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

/**
 * Gerador de relatórios PDF para histórico de coletas.
 * Utiliza iText 7 para criar documentos PDF formatados.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class HistoricoPDFGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoricoPDFGenerator.class);
    
    // Cores para destacar mudanças
    private static final DeviceRgb COR_MUDANCA_LOCALIZACAO = new DeviceRgb(255, 255, 200); // Amarelo claro
    private static final DeviceRgb COR_MUDANCA_ESTADO = new DeviceRgb(255, 200, 200); // Vermelho claro
    private static final DeviceRgb COR_CABECALHO = new DeviceRgb(70, 130, 180); // Azul aço
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Gera PDF com histórico de coletas de um patrimônio.
     * 
     * @param historico Lista de coletas ordenadas por data DESC
     * @param estatisticas Estatísticas calculadas do histórico
     * @return Bytes do arquivo PDF gerado
     * @throws Exception Se houver erro na geração
     */
    public byte[] gerarPDF(
            List<HistoricoColetaDTO> historico,
            EstatisticasHistoricoDTO estatisticas) throws Exception {
        
        logger.info("Iniciando geração de PDF com {} coletas", historico.size());
        
        if (historico.isEmpty()) {
            throw new IllegalArgumentException("Histórico vazio, não é possível gerar PDF");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Criar documento PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Configurar fontes
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Adicionar cabeçalho
            adicionarCabecalho(document, historico.get(0), fontBold, fontNormal);
            
            // Adicionar estatísticas
            adicionarEstatisticas(document, estatisticas, fontBold, fontNormal);
            
            // Adicionar tabela de histórico
            adicionarTabelaHistorico(document, historico, fontBold, fontNormal);
            
            // Adicionar rodapé
            adicionarRodape(document, fontNormal);
            
            // Fechar documento
            document.close();
            
            logger.info("PDF gerado com sucesso: {} bytes", baos.size());
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Erro ao gerar PDF", e);
            throw new Exception("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adiciona cabeçalho com informações do patrimônio.
     */
    private void adicionarCabecalho(
            Document document,
            HistoricoColetaDTO primeiraColeta,
            PdfFont fontBold,
            PdfFont fontNormal) {
        
        // Título
        Paragraph titulo = new Paragraph("HISTÓRICO DE COLETAS DE PATRIMÔNIO")
                .setFont(fontBold)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(titulo);
        
        // Informações do patrimônio
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);
        
        adicionarLinhaInfo(infoTable, "Número do Patrimônio:", 
                primeiraColeta.getNumeroPatrimonio(), fontBold, fontNormal);
        adicionarLinhaInfo(infoTable, "Descrição:", 
                primeiraColeta.getDescricaoPatrimonio(), fontBold, fontNormal);
        
        document.add(infoTable);
    }
    
    /**
     * Adiciona linha de informação na tabela.
     */
    private void adicionarLinhaInfo(
            Table table,
            String label,
            String valor,
            PdfFont fontBold,
            PdfFont fontNormal) {
        
        Cell cellLabel = new Cell()
                .add(new Paragraph(label).setFont(fontBold))
                .setBorder(Border.NO_BORDER)
                .setPadding(2);
        
        Cell cellValor = new Cell()
                .add(new Paragraph(valor != null ? valor : "N/A").setFont(fontNormal))
                .setBorder(Border.NO_BORDER)
                .setPadding(2);
        
        table.addCell(cellLabel);
        table.addCell(cellValor);
    }
    
    /**
     * Adiciona seção de estatísticas.
     */
    private void adicionarEstatisticas(
            Document document,
            EstatisticasHistoricoDTO stats,
            PdfFont fontBold,
            PdfFont fontNormal) {
        
        // Título da seção
        Paragraph tituloStats = new Paragraph("Estatísticas do Histórico")
                .setFont(fontBold)
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(tituloStats);
        
        // Tabela de estatísticas
        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);
        
        // Linha 1
        statsTable.addCell(criarCelulaEstatistica("Total de Coletas", 
                String.valueOf(stats.getTotalColetas()), fontBold, fontNormal));
        statsTable.addCell(criarCelulaEstatistica("Inventários", 
                String.valueOf(stats.getTotalInventarios()), fontBold, fontNormal));
        statsTable.addCell(criarCelulaEstatistica("Mudanças de Local", 
                String.valueOf(stats.getTotalMudancasLocalizacao()), fontBold, fontNormal));
        statsTable.addCell(criarCelulaEstatistica("Mudanças de Estado", 
                String.valueOf(stats.getTotalMudancasEstado()), fontBold, fontNormal));
        
        // Linha 2
        String primeiraColeta = stats.getPrimeiraColeta() != null ? 
                dateOnlyFormat.format(stats.getPrimeiraColeta()) : "N/A";
        String ultimaColeta = stats.getUltimaColeta() != null ? 
                dateOnlyFormat.format(stats.getUltimaColeta()) : "N/A";
        
        statsTable.addCell(criarCelulaEstatistica("Primeira Coleta", 
                primeiraColeta, fontBold, fontNormal));
        statsTable.addCell(criarCelulaEstatistica("Última Coleta", 
                ultimaColeta, fontBold, fontNormal));
        
        // Calcular e adicionar dias e média
        stats.calcularDiasEntrePrimeiraEUltima();
        stats.calcularMediaColetasPorInventario();
        
        String dias = stats.getDiasEntrePrimeiraEUltima() != null ? 
                stats.getDiasEntrePrimeiraEUltima() + " dias" : "N/A";
        String media = stats.getMediaColetasPorInventario() != null ? 
                String.format("%.2f", stats.getMediaColetasPorInventario()) : "N/A";
        
        statsTable.addCell(criarCelulaEstatistica("Período", dias, fontBold, fontNormal));
        statsTable.addCell(criarCelulaEstatistica("Média/Inventário", media, fontBold, fontNormal));
        
        document.add(statsTable);
    }
    
    /**
     * Cria célula de estatística formatada.
     */
    private Cell criarCelulaEstatistica(
            String label,
            String valor,
            PdfFont fontBold,
            PdfFont fontNormal) {
        
        Paragraph p = new Paragraph()
                .add(new Paragraph(label).setFont(fontBold).setFontSize(8))
                .add("\n")
                .add(new Paragraph(valor).setFont(fontNormal).setFontSize(10));
        
        return new Cell()
                .add(p)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }
    
    /**
     * Adiciona tabela com histórico de coletas.
     */
    private void adicionarTabelaHistorico(
            Document document,
            List<HistoricoColetaDTO> historico,
            PdfFont fontBold,
            PdfFont fontNormal) {
        
        // Título da seção
        Paragraph tituloHistorico = new Paragraph("Histórico de Coletas")
                .setFont(fontBold)
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(tituloHistorico);
        
        // Criar tabela
        float[] columnWidths = {12, 18, 15, 20, 15, 20};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setFontSize(8);
        
        // Cabeçalho da tabela
        String[] headers = {"Data", "Inventário", "Coletor", "Localização", "Estado", "Observações"};
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setFont(fontBold))
                    .setBackgroundColor(COR_CABECALHO)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
            table.addHeaderCell(cell);
        }
        
        // Adicionar linhas de dados
        for (HistoricoColetaDTO coleta : historico) {
            adicionarLinhaColeta(table, coleta, fontNormal);
        }
        
        document.add(table);
    }
    
    /**
     * Adiciona linha de coleta na tabela.
     */
    private void adicionarLinhaColeta(Table table, HistoricoColetaDTO coleta, PdfFont fontNormal) {
        // Data
        String dataStr = coleta.getDataColeta() != null ? 
                dateFormat.format(coleta.getDataColeta()) : "N/A";
        table.addCell(criarCelula(dataStr, fontNormal, null));
        
        // Inventário
        table.addCell(criarCelula(coleta.getNomeInventario(), fontNormal, null));
        
        // Coletor
        table.addCell(criarCelula(coleta.getNomeColetorCompleto(), fontNormal, null));
        
        // Localização (com destaque se mudou)
        DeviceRgb corLocalizacao = Boolean.TRUE.equals(coleta.getTemMudancaLocalizacao()) ? 
                COR_MUDANCA_LOCALIZACAO : null;
        String localizacao = coleta.getLocalizacaoEncontrada();
        if (Boolean.TRUE.equals(coleta.getTemMudancaLocalizacao()) && coleta.getLocalizacaoAnterior() != null) {
            localizacao += "\n(Anterior: " + coleta.getLocalizacaoAnterior() + ")";
        }
        table.addCell(criarCelula(localizacao, fontNormal, corLocalizacao));
        
        // Estado (com destaque se mudou)
        DeviceRgb corEstado = Boolean.TRUE.equals(coleta.getTemMudancaEstado()) ? 
                COR_MUDANCA_ESTADO : null;
        String estado = coleta.getEstadoEncontrado();
        if (Boolean.TRUE.equals(coleta.getTemMudancaEstado()) && coleta.getEstadoAnterior() != null) {
            estado += "\n(Anterior: " + coleta.getEstadoAnterior() + ")";
        }
        table.addCell(criarCelula(estado, fontNormal, corEstado));
        
        // Observações
        String obs = coleta.getObservacoes() != null && !coleta.getObservacoes().isEmpty() ? 
                coleta.getObservacoes() : "-";
        table.addCell(criarCelula(obs, fontNormal, null));
    }
    
    /**
     * Cria célula formatada.
     */
    private Cell criarCelula(String texto, PdfFont font, DeviceRgb backgroundColor) {
        Cell cell = new Cell()
                .add(new Paragraph(texto != null ? texto : "N/A").setFont(font))
                .setPadding(3);
        
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        
        return cell;
    }
    
    /**
     * Adiciona rodapé com data de geração.
     */
    private void adicionarRodape(Document document, PdfFont fontNormal) {
        String dataGeracao = dateFormat.format(new Date());
        
        Paragraph rodape = new Paragraph("Relatório gerado em: " + dataGeracao)
                .setFont(fontNormal)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(15);
        
        document.add(rodape);
    }
}
