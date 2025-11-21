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
                // Criar tabela PDF com larguras personalizadas para 8 colunas
                int numColunas = modeloTabela.getColumnCount();
                
                // Definir larguras relativas das colunas (ajustadas para caber na página)
                float[] largurasColunas;
                if (numColunas == 8) {
                    // Larguras otimizadas para as 8 colunas do relatório
                    // [Número, Descrição, Sala, Estado, Setor/Local, Responsável, Situação, Valor]
                    largurasColunas = new float[]{8f, 20f, 12f, 10f, 12f, 15f, 10f, 10f};
                } else {
                    // Larguras iguais para outros casos
                    largurasColunas = new float[numColunas];
                    for (int i = 0; i < numColunas; i++) {
                        largurasColunas[i] = 1f;
                    }
                }
                
                Table tabela = new Table(UnitValue.createPercentArray(largurasColunas))
                        .useAllAvailableWidth();
                
                // Adicionar cabeçalhos com fonte menor
                for (int col = 0; col < numColunas; col++) {
                    Cell celulaCabecalho = new Cell()
                            .add(new Paragraph(modeloTabela.getColumnName(col)))
                            .setFont(boldFont)
                            .setFontSize(7)  // Reduzido de 10 para 7
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setPadding(3);  // Reduzir padding
                    tabela.addHeaderCell(celulaCabecalho);
                }
                
                // Adicionar dados com fonte menor
                for (int row = 0; row < modeloTabela.getRowCount(); row++) {
                    for (int col = 0; col < numColunas; col++) {
                        Object valor = modeloTabela.getValueAt(row, col);
                        String textoValor = valor != null ? valor.toString() : "";
                        
                        // Truncar textos muito longos para evitar quebra de página
                        if (textoValor.length() > 50 && col == 1) { // Descrição
                            textoValor = textoValor.substring(0, 47) + "...";
                        } else if (textoValor.length() > 30 && (col == 2 || col == 4 || col == 5)) { // Sala, Setor, Responsável
                            textoValor = textoValor.substring(0, 27) + "...";
                        }
                        
                        Cell celula = new Cell()
                                .add(new Paragraph(textoValor))
                                .setFont(font)
                                .setFontSize(6.5f)  // Reduzido de 9 para 6.5
                                .setTextAlignment(col == 7 ? TextAlignment.RIGHT : TextAlignment.LEFT)  // Valor alinhado à direita
                                .setPadding(2);  // Reduzir padding
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