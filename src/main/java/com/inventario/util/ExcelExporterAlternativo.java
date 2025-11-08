package com.inventario.util;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Exportador Excel alternativo que não depende do Apache POI
 * Gera arquivos no formato CSV que podem ser abertos pelo Excel
 */
public class ExcelExporterAlternativo {

    /**
     * Exporta dados para formato CSV compatível com Excel
     */
    public static boolean exportarParaExcelCSV(List<Map<String, Object>> dados,
            String[] colunas,
            String[] chavesColunas,
            String tituloRelatorio,
            String nomeArquivo,
            JComponent parent) {

        System.out.println("\n=== EXPORTAÇÃO EXCEL ALTERNATIVA (CSV) ===");
        System.out.println("Dados recebidos: " + (dados != null ? dados.size() : "null"));

        if (dados == null || dados.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Não há dados para exportar!",
                        "Dados Vazios", JOptionPane.WARNING_MESSAGE);
            });
            return false;
        }

        // Escolher local para salvar - DEVE ser executado na EDT
        final String[] caminhoArquivoArray = new String[1];
        final boolean[] cancelado = new boolean[]{false};
        
        // Verificar se já estamos na EDT
        if (SwingUtilities.isEventDispatchThread()) {
            // Já estamos na EDT, executar diretamente
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel (CSV)", "csv"));
            fileChooser.setSelectedFile(new File(nomeArquivo + ".csv"));

            int resultado = fileChooser.showSaveDialog(parent);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                return false;
            }

            String caminho = fileChooser.getSelectedFile().getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".csv")) {
                caminho += ".csv";
            }
            caminhoArquivoArray[0] = caminho;
        } else {
            // Não estamos na EDT, usar invokeAndWait
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel (CSV)", "csv"));
                    fileChooser.setSelectedFile(new File(nomeArquivo + ".csv"));

                    int resultado = fileChooser.showSaveDialog(parent);
                    if (resultado != JFileChooser.APPROVE_OPTION) {
                        cancelado[0] = true;
                        return;
                    }

                    String caminho = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!caminho.toLowerCase().endsWith(".csv")) {
                        caminho += ".csv";
                    }
                    caminhoArquivoArray[0] = caminho;
                });
            } catch (Exception e) {
                System.err.println("Erro ao mostrar diálogo de salvar: " + e.getMessage());
                return false;
            }
            
            if (cancelado[0]) {
                return false;
            }
        }
        
        String caminhoArquivo = caminhoArquivoArray[0];

        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivo, java.nio.charset.StandardCharsets.UTF_8))) {
            
            System.out.println("Criando arquivo CSV: " + caminhoArquivo);

            // BOM para UTF-8 (para Excel reconhecer acentos corretamente)
            writer.print('\ufeff');

            // Título do relatório
            writer.println("\"" + tituloRelatorio + "\"");
            writer.println("\"Gerado em: " + DateFormatUtils.formatDateTime(DateFormatUtils.nowAsDate()) + "\"");
            writer.println(); // Linha em branco

            // Cabeçalhos
            for (int i = 0; i < colunas.length; i++) {
                if (i > 0) writer.print(";");
                writer.print("\"" + colunas[i] + "\"");
            }
            writer.println();

            // Dados
            int linhasProcessadas = 0;
            for (Map<String, Object> linha : dados) {
                for (int i = 0; i < chavesColunas.length; i++) {
                    if (i > 0) writer.print(";");
                    
                    Object valor = linha.get(chavesColunas[i]);
                    String valorTexto = (valor != null) ? valor.toString() : "";
                    
                    // Escapar aspas duplas e quebras de linha
                    valorTexto = valorTexto.replace("\"", "\"\"");
                    valorTexto = valorTexto.replace("\n", " ").replace("\r", " ");
                    
                    writer.print("\"" + valorTexto + "\"");
                }
                writer.println();
                linhasProcessadas++;
            }

            System.out.println("✅ Arquivo CSV criado com sucesso!");
            System.out.println("Linhas processadas: " + linhasProcessadas);

            final String caminhoFinal = caminhoArquivo;
            final int linhasFinal = linhasProcessadas;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Relatório exportado com sucesso!\n\n" +
                        "Arquivo: " + caminhoFinal + "\n" +
                        "Formato: CSV (compatível com Excel)\n" +
                        "Linhas: " + linhasFinal + "\n\n" +
                        "Para abrir no Excel:\n" +
                        "1. Abra o Excel\n" +
                        "2. Vá em Arquivo > Abrir\n" +
                        "3. Selecione o arquivo CSV\n" +
                        "4. Configure separador como ';' se necessário",
                        "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
            });

            return true;

        } catch (IOException e) {
            System.err.println("❌ Erro ao criar arquivo CSV: " + e.getMessage());
            e.printStackTrace();
            
            final String mensagemErro = e.getMessage();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Erro ao exportar arquivo:\n\n" + mensagemErro,
                        "Erro na Exportação", JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
    }

    /**
     * Exporta dados para formato HTML que pode ser aberto pelo Excel
     */
    public static boolean exportarParaExcelHTML(List<Map<String, Object>> dados,
            String[] colunas,
            String[] chavesColunas,
            String tituloRelatorio,
            String nomeArquivo,
            JComponent parent) {

        System.out.println("\n=== EXPORTAÇÃO EXCEL ALTERNATIVA (HTML) ===");

        if (dados == null || dados.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Não há dados para exportar!",
                        "Dados Vazios", JOptionPane.WARNING_MESSAGE);
            });
            return false;
        }

        // Escolher local para salvar - DEVE ser executado na EDT
        final String[] caminhoArquivoArray = new String[1];
        final boolean[] cancelado = new boolean[]{false};
        
        // Verificar se já estamos na EDT
        if (SwingUtilities.isEventDispatchThread()) {
            // Já estamos na EDT, executar diretamente
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel (HTML)", "html"));
            fileChooser.setSelectedFile(new File(nomeArquivo + ".html"));

            int resultado = fileChooser.showSaveDialog(parent);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                return false;
            }

            String caminho = fileChooser.getSelectedFile().getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".html")) {
                caminho += ".html";
            }
            caminhoArquivoArray[0] = caminho;
        } else {
            // Não estamos na EDT, usar invokeAndWait
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel (HTML)", "html"));
                    fileChooser.setSelectedFile(new File(nomeArquivo + ".html"));

                    int resultado = fileChooser.showSaveDialog(parent);
                    if (resultado != JFileChooser.APPROVE_OPTION) {
                        cancelado[0] = true;
                        return;
                    }

                    String caminho = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!caminho.toLowerCase().endsWith(".html")) {
                        caminho += ".html";
                    }
                    caminhoArquivoArray[0] = caminho;
                });
            } catch (Exception e) {
                System.err.println("Erro ao mostrar diálogo de salvar: " + e.getMessage());
                return false;
            }
            
            if (cancelado[0]) {
                return false;
            }
        }
        
        String caminhoArquivo = caminhoArquivoArray[0];

        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivo, java.nio.charset.StandardCharsets.UTF_8))) {
            
            // Cabeçalho HTML
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>" + tituloRelatorio + "</title>");
            writer.println("<style>");
            writer.println("table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; }");
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
                writer.println("<th>" + escapeHtml(coluna) + "</th>");
            }
            writer.println("</tr></thead>");

            // Dados
            writer.println("<tbody>");
            int linhasProcessadas = 0;
            for (Map<String, Object> linha : dados) {
                writer.println("<tr>");
                for (String chave : chavesColunas) {
                    Object valor = linha.get(chave);
                    String valorTexto = (valor != null) ? valor.toString() : "";
                    writer.println("<td>" + escapeHtml(valorTexto) + "</td>");
                }
                writer.println("</tr>");
                linhasProcessadas++;
            }
            writer.println("</tbody>");
            writer.println("</table>");

            writer.println("</body>");
            writer.println("</html>");

            System.out.println("✅ Arquivo HTML criado com sucesso!");
            System.out.println("Linhas processadas: " + linhasProcessadas);

            final String caminhoFinal = caminhoArquivo;
            final int linhasFinal = linhasProcessadas;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Relatório exportado com sucesso!\n\n" +
                        "Arquivo: " + caminhoFinal + "\n" +
                        "Formato: HTML (pode ser aberto pelo Excel)\n" +
                        "Linhas: " + linhasFinal + "\n\n" +
                        "Para abrir no Excel:\n" +
                        "1. Abra o Excel\n" +
                        "2. Vá em Arquivo > Abrir\n" +
                        "3. Selecione 'Todos os arquivos'\n" +
                        "4. Selecione o arquivo HTML",
                        "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
            });

            return true;

        } catch (IOException e) {
            System.err.println("❌ Erro ao criar arquivo HTML: " + e.getMessage());
            e.printStackTrace();
            
            final String mensagemErro = e.getMessage();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Erro ao exportar arquivo:\n\n" + mensagemErro,
                        "Erro na Exportação", JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
    }

    /**
     * Escapa caracteres HTML
     */
    private static String escapeHtml(String texto) {
        if (texto == null) return "";
        
        return texto.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}