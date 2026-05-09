package com.inventario.sihcp.service;

import com.inventario.sihcp.model.*;

// Apache POI imports for Excel
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// iText imports for PDF
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.DeviceRgb;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Service para exportação do relatório de itens compostos em múltiplos formatos.
 * Suporta Excel (.xlsx), PDF e CSV.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ExportacaoItemCompostoService {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    /**
     * Exporta o relatório para Excel (.xlsx)
     * Cria duas abas: "Resumo" com estatísticas e "Detalhamento" com todos os itens
     */
    public File exportarExcel(List<ItemCompostoResumo> itens, EstatisticasIntegridade stats, 
                              String caminhoArquivo) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Criar estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle completoStyle = createStatusStyle(workbook, new java.awt.Color(212, 239, 223));
            CellStyle incompletoStyle = createStatusStyle(workbook, new java.awt.Color(250, 219, 216));
            CellStyle parcialStyle = createStatusStyle(workbook, new java.awt.Color(252, 243, 207));
            
            // === Aba 1: Resumo ===
            Sheet resumoSheet = workbook.createSheet("Resumo");
            criarAbaResumo(resumoSheet, stats, headerStyle);
            
            // === Aba 2: Detalhamento ===
            Sheet detalhamentoSheet = workbook.createSheet("Detalhamento");
            criarAbaDetalhamento(detalhamentoSheet, itens, headerStyle, 
                    completoStyle, incompletoStyle, parcialStyle);
            
            // Salvar arquivo
            File arquivo = new File(caminhoArquivo);
            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                workbook.write(fos);
            }
            
            return arquivo;
        }
    }
    
    private void criarAbaResumo(Sheet sheet, EstatisticasIntegridade stats, CellStyle headerStyle) {
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Relatório de Integridade de Itens Compostos");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Linha em branco
        
        // Data de geração
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Data de Geração:");
        dateRow.createCell(1).setCellValue(DATE_FORMAT.format(new Date()));
        
        rowNum++; // Linha em branco
        
        // Estatísticas
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Estatística");
        headerRow.createCell(0).setCellStyle(headerStyle);
        headerRow.createCell(1).setCellValue("Valor");
        headerRow.createCell(1).setCellStyle(headerStyle);
        
        createStatRow(sheet, rowNum++, "Total de Conjuntos", String.valueOf(stats.getTotalConjuntos()));
        createStatRow(sheet, rowNum++, "Conjuntos Completos", String.valueOf(stats.getConjuntosCompletos()));
        createStatRow(sheet, rowNum++, "Conjuntos Incompletos", String.valueOf(stats.getConjuntosIncompletos()));
        createStatRow(sheet, rowNum++, "Conjuntos Parciais", String.valueOf(stats.getConjuntosParciais()));
        createStatRow(sheet, rowNum++, "Taxa de Integridade Geral", stats.getTaxaFormatada());
        createStatRow(sheet, rowNum++, "Situação", stats.isAlerta() ? "⚠️ ALERTA (< 80%)" : "✅ OK (>= 80%)");
        
        // Ajustar largura das colunas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createStatRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
    
    private void criarAbaDetalhamento(Sheet sheet, List<ItemCompostoResumo> itens, 
                                       CellStyle headerStyle, CellStyle completoStyle,
                                       CellStyle incompletoStyle, CellStyle parcialStyle) {
        int rowNum = 0;
        
        // Cabeçalho
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Nº Patrimônio", "Descrição", "Sala", "Responsável", 
                           "Esperados", "Encontrados", "Faltantes", "Taxa", "Status", "Componentes Faltantes"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dados
        for (ItemCompostoResumo item : itens) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(item.getNumeroPatrimonio());
            row.createCell(1).setCellValue(truncate(item.getDescricaoPatrimonio(), 50));
            row.createCell(2).setCellValue(item.getNomeSala());
            row.createCell(3).setCellValue(item.getNomeResponsavel());
            row.createCell(4).setCellValue(item.getComponentesEsperados());
            row.createCell(5).setCellValue(item.getComponentesEncontrados());
            row.createCell(6).setCellValue(item.getComponentesFaltantes());
            row.createCell(7).setCellValue(item.getTaxaIntegridadeFormatada());
            
            Cell statusCell = row.createCell(8);
            statusCell.setCellValue(item.getStatus().getDescricao());
            
            // Aplicar cor baseada no status
            CellStyle statusStyle = switch (item.getStatus()) {
                case COMPLETO -> completoStyle;
                case INCOMPLETO -> incompletoStyle;
                case PARCIAL -> parcialStyle;
                default -> null;
            };
            if (statusStyle != null) {
                statusCell.setCellStyle(statusStyle);
            }
            
            row.createCell(9).setCellValue(item.getComponentesFaltantesFormatado());
        }
        
        // Ajustar largura das colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    
    /**
     * Exporta o relatório para PDF
     */
    public File exportarPDF(List<ItemCompostoResumo> itens, EstatisticasIntegridade stats, 
                            String caminhoArquivo) throws IOException {
        
        File arquivo = new File(caminhoArquivo);
        
        try (PdfWriter writer = new PdfWriter(arquivo);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Cabeçalho institucional
            Paragraph header = new Paragraph("IFMT - Instituto Federal de Mato Grosso")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(header);
            
            Paragraph title = new Paragraph("Relatório de Integridade de Itens Compostos")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBold();
            document.add(title);
            
            Paragraph date = new Paragraph("Gerado em: " + DATE_FORMAT.format(new Date()))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            document.add(date);
            
            document.add(new Paragraph("\n"));
            
            // Estatísticas resumidas
            document.add(new Paragraph("Resumo Estatístico").setBold().setFontSize(11));
            
            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(60));
            
            addStatRow(statsTable, "Total de Conjuntos", String.valueOf(stats.getTotalConjuntos()));
            addStatRow(statsTable, "Completos", String.valueOf(stats.getConjuntosCompletos()));
            addStatRow(statsTable, "Incompletos", String.valueOf(stats.getConjuntosIncompletos()));
            addStatRow(statsTable, "Taxa Geral", stats.getTaxaFormatada());
            
            document.add(statsTable);
            document.add(new Paragraph("\n"));
            
            // Tabela de dados
            document.add(new Paragraph("Detalhamento por Patrimônio").setBold().setFontSize(11));
            
            Table dataTable = new Table(UnitValue.createPercentArray(new float[]{12, 25, 15, 10, 10, 10, 18}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setFontSize(8);
            
            // Cabeçalho da tabela
            String[] headers = {"Nº Patrimônio", "Descrição", "Sala", "Esperados", "Encontrados", "Taxa", "Status"};
            for (String h : headers) {
                dataTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(h).setBold())
                        .setBackgroundColor(new DeviceRgb(52, 152, 219))
                        .setFontColor(new DeviceRgb(255, 255, 255)));
            }
            
            // Dados
            for (ItemCompostoResumo item : itens) {
                dataTable.addCell(item.getNumeroPatrimonio());
                dataTable.addCell(truncate(item.getDescricaoPatrimonio(), 30));
                dataTable.addCell(truncate(item.getNomeSala(), 15));
                dataTable.addCell(String.valueOf(item.getComponentesEsperados()));
                dataTable.addCell(String.valueOf(item.getComponentesEncontrados()));
                dataTable.addCell(item.getTaxaIntegridadeFormatada());
                
                com.itextpdf.layout.element.Cell statusCell = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(item.getStatus().getDescricao()));
                DeviceRgb bgColor = switch (item.getStatus()) {
                    case COMPLETO -> new DeviceRgb(212, 239, 223);
                    case INCOMPLETO -> new DeviceRgb(250, 219, 216);
                    case PARCIAL -> new DeviceRgb(252, 243, 207);
                    default -> new DeviceRgb(255, 255, 255);
                };
                statusCell.setBackgroundColor(bgColor);
                dataTable.addCell(statusCell);
            }
            
            document.add(dataTable);
        }
        
        return arquivo;
    }
    
    private void addStatRow(Table table, String label, String value) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(label)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(value)));
    }
    
    /**
     * Exporta o relatório para CSV
     * Usa ponto-e-vírgula como separador e UTF-8 com BOM
     */
    public File exportarCSV(List<ItemCompostoResumo> itens, String caminhoArquivo) throws IOException {
        File arquivo = new File(caminhoArquivo);
        
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(arquivo), StandardCharsets.UTF_8)) {
            
            // BOM para UTF-8 (ajuda Excel a reconhecer encoding)
            writer.write('\ufeff');
            
            // Cabeçalho
            writer.write("Nº Patrimônio;Descrição;Sala;Responsável;Esperados;Encontrados;Faltantes;Taxa;Status;Componentes Faltantes\n");
            
            // Dados
            for (ItemCompostoResumo item : itens) {
                writer.write(String.format("%s;%s;%s;%s;%d;%d;%d;%s;%s;%s\n",
                        escapeCsv(item.getNumeroPatrimonio()),
                        escapeCsv(item.getDescricaoPatrimonio()),
                        escapeCsv(item.getNomeSala()),
                        escapeCsv(item.getNomeResponsavel()),
                        item.getComponentesEsperados(),
                        item.getComponentesEncontrados(),
                        item.getComponentesFaltantes(),
                        item.getTaxaIntegridadeFormatada(),
                        item.getStatus().getDescricao(),
                        escapeCsv(item.getComponentesFaltantesFormatado())
                ));
            }
        }
        
        return arquivo;
    }
    
    // === Métodos auxiliares ===
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private CellStyle createStatusStyle(Workbook workbook, java.awt.Color color) {
        CellStyle style = workbook.createCellStyle();
        // POI não suporta cores RGB diretamente em XSSF, usar IndexedColors aproximadas
        if (color.equals(new java.awt.Color(212, 239, 223))) {
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        } else if (color.equals(new java.awt.Color(250, 219, 216))) {
            style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        } else if (color.equals(new java.awt.Color(252, 243, 207))) {
            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        // Escapar aspas duplas e envolver em aspas se contiver separador
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
