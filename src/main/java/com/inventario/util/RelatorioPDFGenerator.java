package com.inventario.util;

import javax.swing.table.TableModel;
// Imports do iText7
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;

/**
 * Classe utilitária para gerar relatórios em PDF usando iText7
 */
public class RelatorioPDFGenerator {
    
    /**
     * Gera um relatório PDF a partir dos dados de uma JTable
     * 
     * @param caminhoArquivo Caminho onde o PDF será salvo
     * @param tituloRelatorio Título do relatório
     * @param modeloTabela Modelo da tabela com os dados
     * @param tipoRelatorio Tipo do relatório sendo gerado
     * @throws Exception Se houver erro na geração do PDF
     */
    public static void gerarRelatorioPDF(String caminhoArquivo, String tituloRelatorio, 
                                       TableModel modeloTabela, String tipoRelatorio) throws Exception {
        
        try {
            // Criar o documento PDF
            PdfWriter writer = new PdfWriter(caminhoArquivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Configurar fontes
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            
            // Cabeçalho do relatório
            Paragraph titulo = new Paragraph("SIHCP - SISTEMA DE HISTÓRICO E COLETA PATRIMONIAL")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(titulo);
            
            Paragraph subtitulo = new Paragraph(tituloRelatorio)
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(subtitulo);
            
            // Data de geração
            Paragraph dataGeracao = new Paragraph("Data de Geração: " + DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(dataGeracao);
            
            // Verificar se há dados na tabela
            if (modeloTabela.getRowCount() == 0) {
                Paragraph semDados = new Paragraph("Nenhum dado encontrado para este relatório.")
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(semDados);
            } else {
                // Criar tabela PDF
                int numColunas = modeloTabela.getColumnCount();
                Table tabela = new Table(UnitValue.createPercentArray(numColunas))
                        .useAllAvailableWidth();
                
                // Adicionar cabeçalhos
                for (int col = 0; col < numColunas; col++) {
                    Cell celulaCabecalho = new Cell()
                            .add(new Paragraph(modeloTabela.getColumnName(col)))
                            .setFont(boldFont)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY);
                    tabela.addHeaderCell(celulaCabecalho);
                }
                
                // Adicionar dados
                for (int row = 0; row < modeloTabela.getRowCount(); row++) {
                    for (int col = 0; col < numColunas; col++) {
                        Object valor = modeloTabela.getValueAt(row, col);
                        String textoValor = valor != null ? valor.toString() : "";
                        
                        Cell celula = new Cell()
                                .add(new Paragraph(textoValor))
                                .setFont(font)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.LEFT);
                        tabela.addCell(celula);
                    }
                }
                
                document.add(tabela);
                
                // Resumo estatístico
                Paragraph resumo = new Paragraph("\nResumo: Total de " + modeloTabela.getRowCount() + " itens")
                        .setFont(font)
                        .setFontSize(10)
                        .setMarginTop(20);
                document.add(resumo);
            }
            
            // Rodapé
            Paragraph rodape = new Paragraph("\nRelatório gerado pelo SIHCP - Sistema de Histórico e Coleta Patrimonial")
                    .setFont(font)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(rodape);
            
            // Fechar documento
            document.close();
            
        } catch (Exception e) {
            throw new Exception("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gera um relatório PDF simples com texto
     * 
     * @param caminhoArquivo Caminho onde o PDF será salvo
     * @param titulo Título do relatório
     * @param conteudo Conteúdo do relatório
     * @throws Exception Se houver erro na geração do PDF
     */
    public static void gerarRelatorioPDFTexto(String caminhoArquivo, String titulo, String conteudo) throws Exception {
        try {
            PdfWriter writer = new PdfWriter(caminhoArquivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            
            // Cabeçalho
            Paragraph cabecalho = new Paragraph("SISTEMA DE INVENTÁRIO IFMT")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(cabecalho);
            
            // Título
            Paragraph tituloP = new Paragraph(titulo)
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(tituloP);
            
            // Data de geração
            Paragraph dataGeracao = new Paragraph("Data de Geração: " + DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(dataGeracao);
            
            // Conteúdo
            Paragraph conteudoP = new Paragraph(conteudo)
                    .setFont(font)
                    .setFontSize(12);
            document.add(conteudoP);
            
            // Rodapé
            Paragraph rodape = new Paragraph("\nRelatório gerado pelo Sistema de Inventário IFMT")
                    .setFont(font)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(rodape);
            
            document.close();
            
        } catch (Exception e) {
            throw new Exception("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
}