package com.inventario.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária para exportação de relatórios em formato Excel (XLSX)
 */
public class ExcelExporter {

    // Formatação de datas centralizada através de DateFormatUtils

    /**
     * Exporta dados para arquivo Excel
     * 
     * @param dados           Lista de mapas contendo os dados
     * @param colunas         Array com os nomes das colunas
     * @param tituloRelatorio Título do relatório
     * @param nomeArquivo     Nome do arquivo (sem extensão)
     * @param parent          Componente pai para diálogos
     * @return true se a exportação foi bem-sucedida
     */
    public static boolean exportarParaExcel(List<Map<String, Object>> dados,
            String[] colunas,
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
            // Criar workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Relatório");

            // Criar estilos
            CellStyle estiloTitulo = criarEstiloTitulo(workbook);
            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
            CellStyle estiloDados = criarEstiloDados(workbook);
            CellStyle estiloData = criarEstiloData(workbook);
            CellStyle estiloNumero = criarEstiloNumero(workbook);

            int linhaAtual = 0;

            // Título do relatório
            Row linhaTitulo = sheet.createRow(linhaAtual++);
            Cell celulaTitulo = linhaTitulo.createCell(0);
            celulaTitulo.setCellValue(tituloRelatorio);
            celulaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colunas.length - 1));

            // Data de geração
            linhaAtual++; // Linha em branco
            Row linhaData = sheet.createRow(linhaAtual++);
            Cell celulaData = linhaData.createCell(0);
            celulaData.setCellValue("Gerado em: " + DateFormatUtils.formatDateTime(DateFormatUtils.nowAsDate()));
            celulaData.setCellStyle(estiloData);

            linhaAtual++; // Linha em branco

            // Cabeçalho
            Row linhaCabecalho = sheet.createRow(linhaAtual++);
            for (int i = 0; i < colunas.length; i++) {
                Cell celula = linhaCabecalho.createCell(i);
                celula.setCellValue(colunas[i]);
                celula.setCellStyle(estiloCabecalho);
            }

            // Dados
            for (Map<String, Object> linha : dados) {
                Row linhaExcel = sheet.createRow(linhaAtual++);

                for (int i = 0; i < colunas.length; i++) {
                    Cell celula = linhaExcel.createCell(i);
                    Object valor = obterValorPorIndice(linha, i);

                    if (valor != null) {
                        if (valor instanceof Number) {
                            celula.setCellValue(((Number) valor).doubleValue());
                            celula.setCellStyle(estiloNumero);
                        } else if (valor instanceof Date) {
                            celula.setCellValue((Date) valor);
                            celula.setCellStyle(estiloData);
                        } else {
                            celula.setCellValue(sanitizarTextoParaExcel(valor.toString()));
                            celula.setCellStyle(estiloDados);
                        }
                    } else {
                        celula.setCellValue("");
                        celula.setCellStyle(estiloDados);
                    }
                }
            }

            // Auto-ajustar largura das colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
                // Definir largura mínima e máxima
                int largura = sheet.getColumnWidth(i);
                if (largura < 2000) {
                    sheet.setColumnWidth(i, 2000);
                } else if (largura > 8000) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            // Salvar arquivo
            String caminhoArquivo = System.getProperty("user.home") + "/Desktop/" + nomeArquivo + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(caminhoArquivo);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            JOptionPane.showMessageDialog(parent,
                    "Relatório exportado com sucesso!\nArquivo salvo em: " + caminhoArquivo,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Erro ao exportar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Obtém valor do mapa baseado no índice da coluna
     */
    private static Object obterValorPorIndice(Map<String, Object> linha, int indice) {
        String[] chaves = { "numero", "descricao", "marca", "quantidade", "setor", "responsavel", "status", "valor" };

        if (indice < chaves.length) {
            Object valor = linha.get(chaves[indice]);
            if (valor == null) {
                // Tentar chaves alternativas
                switch (indice) {
                    case 0:
                        return linha.get("patrimonio");
                    case 2:
                        return linha.get("modelo");
                    case 4:
                        return linha.get("local");
                    case 6:
                        return linha.get("status_coleta");
                    default:
                        return null;
                }
            }
            return valor;
        }
        return null;
    }

    /**
     * Cria estilo para o título
     */
    private static CellStyle criarEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        fonte.setFontHeightInPoints((short) 16);
        fonte.setColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFont(fonte);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    /**
     * Cria estilo para o cabeçalho
     */
    private static CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        fonte.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fonte);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    /**
     * Cria estilo para dados
     */
    private static CellStyle criarEstiloDados(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    /**
     * Cria estilo para datas
     */
    private static CellStyle criarEstiloData(Workbook workbook) {
        CellStyle estilo = criarEstiloDados(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        estilo.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return estilo;
    }

    /**
     * Cria estilo para números
     */
    private static CellStyle criarEstiloNumero(Workbook workbook) {
        CellStyle estilo = criarEstiloDados(workbook);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }

    /**
     * Exporta relatório com dados customizados e validação robusta
     */
    public static boolean exportarRelatorioCustomizado(List<Map<String, Object>> dados,
            String[] colunas,
            String[] chavesColunas,
            String tituloRelatorio,
            String nomeArquivo,
            JComponent parent) {

        System.out.println("\n=== INICIANDO EXPORTAÇÃO EXCEL ===");
        System.out.println("Dados recebidos: " + (dados != null ? dados.size() : "null"));
        System.out.println("Colunas: " + (colunas != null ? colunas.length : "null"));
        System.out.println("Chaves: " + (chavesColunas != null ? java.util.Arrays.toString(chavesColunas) : "null"));

        if (dados == null || dados.isEmpty()) {
            System.out.println("❌ ERRO: Nenhum dado para exportar");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Não há dados para exportar!\n\n" +
                        "Verifique se o relatório foi gerado corretamente.",
                        "Dados Vazios", JOptionPane.WARNING_MESSAGE);
            });
            return false;
        }

        // Validar estrutura dos dados
        System.out.println("\n--- VALIDANDO ESTRUTURA DOS DADOS ---");
        Map<String, Object> primeiraLinha = dados.get(0);
        System.out.println("Chaves disponíveis na primeira linha: " + primeiraLinha.keySet());
        
        for (String chave : chavesColunas) {
            boolean temChave = primeiraLinha.containsKey(chave);
            Object valor = primeiraLinha.get(chave);
            System.out.println("Chave '" + chave + "': " + (temChave ? "✅ OK" : "❌ AUSENTE") + 
                             (valor != null ? " (valor: '" + valor.toString().substring(0, Math.min(30, valor.toString().length())) + "')" : " (null)"));
        }
        
        // Mostrar amostra de dados das primeiras 3 linhas
        System.out.println("\n--- AMOSTRA DE DADOS ---");
        for (int i = 0; i < Math.min(3, dados.size()); i++) {
            Map<String, Object> linha = dados.get(i);
            System.out.println("Linha " + (i + 1) + ":");
            for (String chave : chavesColunas) {
                Object valor = linha.get(chave);
                String valorStr = (valor != null) ? valor.toString() : "null";
                if (valorStr.length() > 50) valorStr = valorStr.substring(0, 47) + "...";
                System.out.println("  " + chave + ": " + valorStr);
            }
        }

        // Escolher local para salvar - DEVE ser executado na EDT
        final String[] caminhoArquivoArray = new String[1];
        final boolean[] cancelado = new boolean[]{false};
        
        // Verificar se já estamos na EDT
        if (SwingUtilities.isEventDispatchThread()) {
            // Já estamos na EDT, executar diretamente
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel", "xlsx"));
            fileChooser.setSelectedFile(new java.io.File(nomeArquivo + ".xlsx"));

            int resultado = fileChooser.showSaveDialog(parent);
            if (resultado != javax.swing.JFileChooser.APPROVE_OPTION) {
                System.out.println("Exportação cancelada pelo usuário");
                return false;
            }

            String caminho = fileChooser.getSelectedFile().getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".xlsx")) {
                caminho += ".xlsx";
            }
            caminhoArquivoArray[0] = caminho;
        } else {
            // Não estamos na EDT, usar invokeAndWait
            try {
                SwingUtilities.invokeAndWait(() -> {
                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel", "xlsx"));
                    fileChooser.setSelectedFile(new java.io.File(nomeArquivo + ".xlsx"));

                    int resultado = fileChooser.showSaveDialog(parent);
                    if (resultado != javax.swing.JFileChooser.APPROVE_OPTION) {
                        System.out.println("Exportação cancelada pelo usuário");
                        cancelado[0] = true;
                        return;
                    }

                    String caminho = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!caminho.toLowerCase().endsWith(".xlsx")) {
                        caminho += ".xlsx";
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
        System.out.println("Arquivo de destino: " + caminhoArquivo);

        Workbook workbook = null;
        FileOutputStream fileOut = null;

        try {
            System.out.println("\n=== CRIANDO WORKBOOK EXCEL ===");
            
            // Criar workbook
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Relatório");
            System.out.println("✅ Workbook e sheet criados");

            // Criar estilos
            CellStyle estiloTitulo = criarEstiloTitulo(workbook);
            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
            CellStyle estiloDados = criarEstiloDados(workbook);
            System.out.println("✅ Estilos criados");

            int linhaAtual = 0;

            // Título
            Row linhaTitulo = sheet.createRow(linhaAtual++);
            Cell celulaTitulo = linhaTitulo.createCell(0);
            celulaTitulo.setCellValue(tituloRelatorio);
            celulaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colunas.length - 1));

            // Data de geração
            linhaAtual++;
            Row linhaData = sheet.createRow(linhaAtual++);
            Cell celulaData = linhaData.createCell(0);
            celulaData.setCellValue("Gerado em: " + DateFormatUtils.formatDateTime(DateFormatUtils.nowAsDate()));

            linhaAtual++;

            // Cabeçalho
            System.out.println("Adicionando cabeçalhos...");
            Row linhaCabecalho = sheet.createRow(linhaAtual++);
            for (int i = 0; i < colunas.length; i++) {
                Cell celula = linhaCabecalho.createCell(i);
                celula.setCellValue(colunas[i]);
                celula.setCellStyle(estiloCabecalho);
            }

            // Dados
            System.out.println("Processando " + dados.size() + " linhas de dados...");
            int contadorLinhas = 0;
            int linhasComErro = 0;
            
            for (Map<String, Object> linha : dados) {
                try {
                    Row linhaExcel = sheet.createRow(linhaAtual++);
                    contadorLinhas++;

                    for (int i = 0; i < colunas.length && i < chavesColunas.length; i++) {
                        Cell celula = linhaExcel.createCell(i);
                        Object valor = linha.get(chavesColunas[i]);
                        
                        String valorTexto = (valor != null) ? valor.toString() : "";
                        
                        // Sanitizar e truncar se necessário
                        valorTexto = sanitizarTextoParaExcel(valorTexto);
                        
                        celula.setCellValue(valorTexto);
                        celula.setCellStyle(estiloDados);
                    }

                    if (contadorLinhas % 100 == 0) {
                        System.out.println("Processadas " + contadorLinhas + " linhas...");
                    }
                    
                } catch (Exception rowError) {
                    System.err.println("❌ Erro na linha " + contadorLinhas + ": " + rowError.getMessage());
                    linhasComErro++;
                }
            }

            System.out.println("Ajustando colunas...");
            // Auto-ajustar colunas
            for (int i = 0; i < colunas.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                    int largura = sheet.getColumnWidth(i);
                    if (largura < 2000) {
                        sheet.setColumnWidth(i, 2000);
                    } else if (largura > 10000) {
                        sheet.setColumnWidth(i, 10000);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Aviso: Não foi possível ajustar coluna " + i);
                }
            }

            System.out.println("Salvando arquivo...");
            // Salvar arquivo
            fileOut = new FileOutputStream(caminhoArquivo);
            workbook.write(fileOut);

            System.out.println("\n=== EXPORTAÇÃO CONCLUÍDA ===");
            System.out.println("✅ Arquivo salvo: " + caminhoArquivo);
            System.out.println("✅ Linhas processadas: " + contadorLinhas);
            System.out.println("❌ Linhas com erro: " + linhasComErro);
            System.out.println("============================\n");

            final String mensagemFinal = "Relatório exportado com sucesso!\n\n" +
                    "Arquivo: " + caminhoArquivo + "\n" +
                    "Linhas processadas: " + contadorLinhas +
                    (linhasComErro > 0 ? "\nLinhas com erro: " + linhasComErro : "");

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent, mensagemFinal, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            });

            return true;

        } catch (Exception e) {
            System.err.println("❌ ERRO CRÍTICO na exportação Excel: " + e.getMessage());
            e.printStackTrace();
            
            String mensagemErro = "Erro ao exportar relatório:\n\n" + e.getMessage();
            
            if (e.getMessage() != null) {
                if (e.getMessage().contains("32767")) {
                    mensagemErro += "\n\nAlguns dados são muito longos para o Excel.\n" +
                                   "Tente usar exportação CSV como alternativa.";
                } else if (e.getMessage().contains("FileNotFoundException")) {
                    mensagemErro += "\n\nVerifique se:\n" +
                                   "- Você tem permissão para escrever no local\n" +
                                   "- O arquivo não está aberto em outro programa";
                }
            }
            
            final String mensagemErroFinal = mensagemErro;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parent, mensagemErroFinal, "Erro na Exportação", JOptionPane.ERROR_MESSAGE);
            });
            return false;
            
        } finally {
            // Fechar recursos
            try {
                if (fileOut != null) {
                    fileOut.close();
                    System.out.println("FileOutputStream fechado");
                }
                if (workbook != null) {
                    workbook.close();
                    System.out.println("Workbook fechado");
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
    
    /**
     * Sanitiza texto para Excel de forma simples e eficaz
     */
    private static String sanitizarTextoParaExcel(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        
        // Remover caracteres de controle
        texto = texto.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // Normalizar quebras de linha
        texto = texto.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        
        // Remover múltiplos espaços
        texto = texto.replaceAll("\\s+", " ").trim();
        
        // Truncar se muito longo
        if (texto.length() > 32767) {
            texto = texto.substring(0, 32764) + "...";
        }
        
        return texto;
    }



}