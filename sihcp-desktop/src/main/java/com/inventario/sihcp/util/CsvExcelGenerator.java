package com.inventario.sihcp.util;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Gerador de relatórios CSV (compatível com Excel) - ALTERNATIVA ao Apache POI
 * Esta classe contorna completamente os problemas do Apache POI
 */
public class CsvExcelGenerator {
    
    /**
     * Gera arquivo CSV que pode ser aberto no Excel
     */
    public static boolean gerarRelatorioCSV(List<Map<String, Object>> dados, 
                                           String[] colunas, 
                                           String[] chaves,
                                           String tituloRelatorio, 
                                           String nomeArquivo,
                                           JComponent parent) {
        
        if (dados == null || dados.isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Não há dados para exportar!", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            // Escolher local para salvar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos CSV (Excel)", "csv"));
            fileChooser.setSelectedFile(new File(nomeArquivo + ".csv"));
            
            int resultado = fileChooser.showSaveDialog(parent);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            
            String arquivo = fileChooser.getSelectedFile().getAbsolutePath();
            if (!arquivo.toLowerCase().endsWith(".csv")) {
                arquivo += ".csv";
            }
            
            System.out.println("Gerando CSV: " + arquivo);
            
            // Criar arquivo CSV
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
                
                // BOM para UTF-8 (para Excel reconhecer acentos)
                writer.print('\ufeff');
                
                // Título do relatório
                writer.println("\"" + tituloRelatorio + "\"");
                writer.println("\"Gerado em: " + DateFormatUtils.formatDateTime(DateFormatUtils.nowAsDate()) + "\"");
                writer.println(); // Linha em branco
                
                // Cabeçalhos
                for (int i = 0; i < colunas.length; i++) {
                    if (i > 0) writer.print(",");
                    writer.print("\"" + escaparCSV(colunas[i]) + "\"");
                }
                writer.println();
                
                // Dados
                for (Map<String, Object> linha : dados) {
                    for (int i = 0; i < colunas.length && i < chaves.length; i++) {
                        if (i > 0) writer.print(",");
                        
                        Object valor = linha.get(chaves[i]);
                        String valorStr = "";
                        
                        if (valor != null) {
                            valorStr = valor.toString();
                        }
                        
                        writer.print("\"" + escaparCSV(valorStr) + "\"");
                    }
                    writer.println();
                }
            }
            
            JOptionPane.showMessageDialog(parent, 
                "Relatório CSV gerado com sucesso!\n\n" +
                "Arquivo: " + arquivo + "\n\n" +
                "Este arquivo pode ser aberto no Excel, LibreOffice ou Google Sheets.",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, 
                "Erro ao gerar relatório CSV: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Escapa caracteres especiais para CSV
     */
    private static String escaparCSV(String valor) {
        if (valor == null) {
            return "";
        }
        
        // Escapar aspas duplas
        valor = valor.replace("\"", "\"\"");
        
        return valor;
    }
    
    /**
     * Gera relatório HTML que pode ser aberto no Excel
     */
    public static boolean gerarRelatorioHTML(List<Map<String, Object>> dados, 
                                            String[] colunas, 
                                            String[] chaves,
                                            String tituloRelatorio, 
                                            String nomeArquivo,
                                            JComponent parent) {
        
        if (dados == null || dados.isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Não há dados para exportar!", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            // Escolher local para salvar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos HTML (Excel)", "html"));
            fileChooser.setSelectedFile(new File(nomeArquivo + ".html"));
            
            int resultado = fileChooser.showSaveDialog(parent);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            
            String arquivo = fileChooser.getSelectedFile().getAbsolutePath();
            if (!arquivo.toLowerCase().endsWith(".html")) {
                arquivo += ".html";
            }
            
            System.out.println("Gerando HTML: " + arquivo);
            
            // Criar arquivo HTML
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
                
                // Cabeçalho HTML
                writer.println("<!DOCTYPE html>");
                writer.println("<html>");
                writer.println("<head>");
                writer.println("<meta charset='UTF-8'>");
                writer.println("<title>" + tituloRelatorio + "</title>");
                writer.println("<style>");
                writer.println("table { border-collapse: collapse; width: 100%; }");
                writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
                writer.println("th { background-color: #f2f2f2; font-weight: bold; }");
                writer.println("tr:nth-child(even) { background-color: #f9f9f9; }");
                writer.println("h1 { color: #333; }");
                writer.println("</style>");
                writer.println("</head>");
                writer.println("<body>");
                
                // Título
                writer.println("<h1>" + tituloRelatorio + "</h1>");
                writer.println("<p>Gerado em: " + DateFormatUtils.formatDateTime(DateFormatUtils.nowAsDate()) + "</p>");
                
                // Tabela
                writer.println("<table>");
                
                // Cabeçalhos
                writer.println("<thead><tr>");
                for (String coluna : colunas) {
                    writer.println("<th>" + escaparHTML(coluna) + "</th>");
                }
                writer.println("</tr></thead>");
                
                // Dados
                writer.println("<tbody>");
                for (Map<String, Object> linha : dados) {
                    writer.println("<tr>");
                    for (int i = 0; i < colunas.length && i < chaves.length; i++) {
                        Object valor = linha.get(chaves[i]);
                        String valorStr = valor != null ? valor.toString() : "";
                        writer.println("<td>" + escaparHTML(valorStr) + "</td>");
                    }
                    writer.println("</tr>");
                }
                writer.println("</tbody>");
                
                writer.println("</table>");
                writer.println("</body>");
                writer.println("</html>");
            }
            
            JOptionPane.showMessageDialog(parent, 
                "Relatório HTML gerado com sucesso!\n\n" +
                "Arquivo: " + arquivo + "\n\n" +
                "Este arquivo pode ser aberto no navegador ou importado no Excel.",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, 
                "Erro ao gerar relatório HTML: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Escapa caracteres especiais para HTML
     */
    private static String escaparHTML(String valor) {
        if (valor == null) {
            return "";
        }
        
        return valor.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Gera relatório em formato de texto simples
     */
    public static boolean gerarRelatorioTexto(List<Map<String, Object>> dados, 
                                             String[] colunas, 
                                             String[] chaves,
                                             String tituloRelatorio, 
                                             String nomeArquivo,
                                             JComponent parent) {
        
        if (dados == null || dados.isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "Não há dados para exportar!", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            // Escolher local para salvar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto", "txt"));
            fileChooser.setSelectedFile(new File(nomeArquivo + ".txt"));
            
            int resultado = fileChooser.showSaveDialog(parent);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            
            String arquivo = fileChooser.getSelectedFile().getAbsolutePath();
            if (!arquivo.toLowerCase().endsWith(".txt")) {
                arquivo += ".txt";
            }
            
            System.out.println("Gerando TXT: " + arquivo);
            
            // Criar arquivo de texto
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
                
                // Título
                writer.println(tituloRelatorio);
                writer.println("=".repeat(tituloRelatorio.length()));
                writer.println();
                writer.println("Gerado em: " + DateFormatUtils.formatDateTime(DateFormatUtils.nowAsDate()));
                writer.println();
                
                // Calcular larguras das colunas
                int[] larguras = new int[colunas.length];
                for (int i = 0; i < colunas.length; i++) {
                    larguras[i] = colunas[i].length();
                }
                
                // Verificar larguras dos dados
                for (Map<String, Object> linha : dados) {
                    for (int i = 0; i < colunas.length && i < chaves.length; i++) {
                        Object valor = linha.get(chaves[i]);
                        String valorStr = valor != null ? valor.toString() : "";
                        if (valorStr.length() > larguras[i]) {
                            larguras[i] = Math.min(valorStr.length(), 50); // Limitar a 50 caracteres
                        }
                    }
                }
                
                // Cabeçalhos
                for (int i = 0; i < colunas.length; i++) {
                    if (i > 0) writer.print(" | ");
                    writer.printf("%-" + larguras[i] + "s", colunas[i]);
                }
                writer.println();
                
                // Linha separadora
                for (int i = 0; i < colunas.length; i++) {
                    if (i > 0) writer.print("-+-");
                    writer.print("-".repeat(larguras[i]));
                }
                writer.println();
                
                // Dados
                for (Map<String, Object> linha : dados) {
                    for (int i = 0; i < colunas.length && i < chaves.length; i++) {
                        if (i > 0) writer.print(" | ");
                        Object valor = linha.get(chaves[i]);
                        String valorStr = valor != null ? valor.toString() : "";
                        if (valorStr.length() > larguras[i]) {
                            valorStr = valorStr.substring(0, larguras[i] - 3) + "...";
                        }
                        writer.printf("%-" + larguras[i] + "s", valorStr);
                    }
                    writer.println();
                }
            }
            
            JOptionPane.showMessageDialog(parent, 
                "Relatório de texto gerado com sucesso!\n\n" +
                "Arquivo: " + arquivo,
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, 
                "Erro ao gerar relatório de texto: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}