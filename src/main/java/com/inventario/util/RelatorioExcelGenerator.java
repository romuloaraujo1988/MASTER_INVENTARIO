package com.inventario.util;

import com.inventario.dao.RelatorioColetaDAO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import javax.swing.*;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Gerador de relatórios específicos em formato Excel
 */
public class RelatorioExcelGenerator {

    // Removido SimpleDateFormat - usando DateFormatUtils centralizado

    private RelatorioColetaDAO relatorioDAO;

    public RelatorioExcelGenerator() {
        this.relatorioDAO = new RelatorioColetaDAO();
    }

    /**
     * Gera relatório de itens encontrados
     */
    public boolean gerarRelatorioItensEncontrados(int idInventario, JComponent parent) {
        try {
            System.out
                    .println("Iniciando geração de relatório de itens encontrados para inventário ID: " + idInventario);

            List<Map<String, Object>> dadosBrutos = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);

            if (dadosBrutos == null || dadosBrutos.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Não há itens encontrados para exportar.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            System.out.println("Total de itens encontrados (brutos): " + dadosBrutos.size());

            // Debug: mostrar chaves disponíveis no primeiro registro
            if (!dadosBrutos.isEmpty()) {
                System.out.println("Chaves disponíveis no primeiro registro: " + dadosBrutos.get(0).keySet());
            }

            // SANITIZAR DADOS ANTES DE PROCESSAR
            System.out.println("Sanitizando dados para Excel...");
            List<Map<String, Object>> dados = DataSanitizer.sanitizarDados(dadosBrutos);
            System.out.println("Dados sanitizados: " + dados.size() + " registros válidos");

            String[] colunas = { "Patrimônio", "Descrição", "Marca", "Modelo", "Setor", "Responsável", "Estado",
                    "Local de Coleta", "Data Coleta", "Observações" };
            // Usar chaves mais consistentes baseadas no que realmente retorna do DAO
            String[] chaves = { "numero", "descricao", "marca", "modelo", "setor", "responsavel", "estado",
                    "localizacao", "data_coleta", "observacoes" };

            String nomeArquivo = "relatorio_itens_encontrados_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());

            return ExcelExporter.exportarRelatorioCustomizado(
                    dados, colunas, chaves,
                    "Relatório de Itens Encontrados",
                    nomeArquivo, parent);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao gerar relatório de itens encontrados: " + e.getMessage());
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage() + "\n\nVerifique o console para mais detalhes.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório de itens não encontrados
     */
    public boolean gerarRelatorioItensNaoEncontrados(int idInventario, JComponent parent) {
        try {
            List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioItensNaoEncontrados(idInventario);

            if (dados == null || dados.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Não há itens não encontrados para exportar.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Debug: mostrar chaves disponíveis
            if (!dados.isEmpty()) {
                System.out.println("Chaves disponíveis (não encontrados): " + dados.get(0).keySet());
            }

            String[] colunas = { "Patrimônio", "Descrição", "Marca", "Modelo", "Setor", "Responsável", "Situação",
                    "Última Localização", "Data Tentativa", "Motivo" };
            String[] chaves = { "numero", "descricao", "marca", "modelo", "setor", "responsavel", "situacao",
                    "ultima_localizacao", "data_tentativa", "motivo" };

            String nomeArquivo = "relatorio_itens_nao_encontrados_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());

            return ExcelExporter.exportarRelatorioCustomizado(
                    dados, colunas, chaves,
                    "Relatório de Itens Não Encontrados",
                    nomeArquivo, parent);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao gerar relatório de itens não encontrados: " + e.getMessage());
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage() + "\n\nVerifique o console para mais detalhes.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório de itens sem etiqueta
     */
    public boolean gerarRelatorioItensSemEtiqueta(int idInventario, JComponent parent) {
        try {
            List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioItensSemEtiqueta(idInventario);

            if (dados == null || dados.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Não há itens sem etiqueta para exportar.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Debug: mostrar chaves disponíveis
            if (!dados.isEmpty()) {
                System.out.println("Chaves disponíveis (sem etiqueta): " + dados.get(0).keySet());
            }

            String[] colunas = { "Descrição", "Marca", "Modelo", "Quantidade", "Local", "Responsável", "Estado",
                    "Data Coleta", "Observações" };
            String[] chaves = { "descricao", "marca", "modelo", "quantidade", "local", "responsavel", "estado",
                    "data_coleta", "observacoes" };

            String nomeArquivo = "relatorio_itens_sem_etiqueta_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());

            return ExcelExporter.exportarRelatorioCustomizado(
                    dados, colunas, chaves,
                    "Relatório de Itens Sem Plaqueta de Patrimônio",
                    nomeArquivo, parent);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao gerar relatório de itens sem etiqueta: " + e.getMessage());
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage() + "\n\nVerifique o console para mais detalhes.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório por responsável
     */
    public boolean gerarRelatorioPorResponsavel(int idInventario, String nomeResponsavel, JComponent parent) {
        try {
            List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioDetalhadoPorResponsavel(idInventario,
                    nomeResponsavel, null);

            String[] colunas = { "Patrimônio", "Descrição", "Marca", "Modelo", "Responsável", "Setor", "Status",
                    "Local de Coleta", "Data Coleta", "Observações" };
            String[] chaves = { "numero", "descricao", "marca", "modelo", "responsavel", "setor", "status_coleta",
                    "localizacao", "data_coleta", "observacao" };

            String nomeArquivo = "relatorio_responsavel_" + nomeResponsavel.replaceAll("\\s+", "_") + "_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());

            return ExcelExporter.exportarRelatorioCustomizado(
                    dados, colunas, chaves,
                    "Relatório por Responsável - " + nomeResponsavel,
                    nomeArquivo, parent);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório de itens não coletados
     */
    public boolean gerarRelatorioItensNaoColetados(int idInventario, JComponent parent) {
        try {
            List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioItensNaoColetados(idInventario);

            String[] colunas = { "Patrimônio", "Descrição", "Marca", "Modelo", "Setor", "Responsável", "Sala",
                    "Situação", "Valor" };
            String[] chaves = { "Número Patrimônio", "Descrição", "marca", "modelo", "Setor", "Responsável", "Sala",
                    "situacao", "Valor" };

            String nomeArquivo = "relatorio_itens_nao_coletados_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());

            return ExcelExporter.exportarRelatorioCustomizado(
                    dados, colunas, chaves,
                    "Relatório de Itens Não Coletados",
                    nomeArquivo, parent);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório de divergências
     */
    public boolean gerarRelatorioDivergencias(int idInventario, JComponent parent) {
        try {
            List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioDivergencias(idInventario);

            String[] colunas = { "Patrimônio", "Descrição", "Marca", "Modelo", "Responsável", "Localização Cadastrada",
                    "Localização Encontrada", "Estado Encontrado", "Motivo da Divergência", "Data Coleta",
                    "Observações" };
            String[] chaves = { "Número Patrimônio", "Descrição", "marca", "modelo", "Responsável",
                    "Localização Cadastrada", "Localização Encontrada", "Estado Encontrado", "Motivo da Divergência",
                    "Data Coleta", "Observações" };

            String nomeArquivo = "relatorio_divergencias_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());

            return ExcelExporter.exportarRelatorioCustomizado(
                    dados, colunas, chaves,
                    "Relatório de Divergências",
                    nomeArquivo, parent);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório de estatísticas gerais
     */
    public boolean gerarRelatorioEstatisticas(int idInventario, JComponent parent) {
        try {
            List<Map<String, Object>> dados = relatorioDAO.gerarEstatisticasGerais(idInventario);

            // Criar workbook personalizado para estatísticas
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Estatísticas");

            // Estilos
            CellStyle estiloTitulo = criarEstiloTitulo(workbook);
            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
            CellStyle estiloDados = criarEstiloDados(workbook);
            CellStyle estiloNumero = criarEstiloNumero(workbook);

            int linhaAtual = 0;

            // Título
            Row linhaTitulo = sheet.createRow(linhaAtual++);
            Cell celulaTitulo = linhaTitulo.createCell(0);
            celulaTitulo.setCellValue("Estatísticas Gerais do Inventário");
            celulaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Data
            linhaAtual++;
            Row linhaData = sheet.createRow(linhaAtual++);
            Cell celulaData = linhaData.createCell(0);
            celulaData.setCellValue("Gerado em: " + DateFormatUtils.formatDateTime(new Date()));

            linhaAtual++;

            // Estatísticas
            for (Map<String, Object> estatistica : dados) {
                Row linha = sheet.createRow(linhaAtual++);

                Cell celulaDescricao = linha.createCell(0);
                celulaDescricao
                        .setCellValue(sanitizarTextoParaExcel(estatistica.get("descricao").toString(), linhaAtual, 0));
                celulaDescricao.setCellStyle(estiloCabecalho);

                Cell celulaValor = linha.createCell(1);
                Object valor = estatistica.get("valor");
                if (valor instanceof Number) {
                    celulaValor.setCellValue(((Number) valor).doubleValue());
                } else {
                    celulaValor.setCellValue(sanitizarTextoParaExcel(valor.toString(), linhaAtual, 1));
                }
                celulaValor.setCellStyle(estiloNumero);

                Object percentual = estatistica.get("percentual");
                if (percentual != null) {
                    Cell celulaPercentual = linha.createCell(2);
                    celulaPercentual.setCellValue(sanitizarTextoParaExcel(percentual.toString(), linhaAtual, 2));
                    celulaPercentual.setCellStyle(estiloDados);
                }
            }

            // Auto-ajustar colunas
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Salvar
            String nomeArquivo = "estatisticas_inventario_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());
            String caminhoArquivo = System.getProperty("user.home") + "/Desktop/" + nomeArquivo + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(caminhoArquivo);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            JOptionPane.showMessageDialog(parent,
                    "Relatório de estatísticas exportado com sucesso!\nArquivo salvo em: " + caminhoArquivo,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Gera relatório geral consolidado
     */
    public boolean gerarRelatorioGeral(int idInventario, JComponent parent) {
        try {
            // Criar workbook com múltiplas abas
            Workbook workbook = new XSSFWorkbook();

            // Aba 1: Itens Encontrados
            List<Map<String, Object>> encontrados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
            criarAbaRelatorio(workbook, "Itens Encontrados", encontrados,
                    new String[] { "Patrimônio", "Descrição", "Marca", "Modelo", "Setor", "Responsável", "Estado",
                            "Local de Coleta", "Data Coleta", "Observações" },
                    new String[] { "patrimonio", "descricao", "marca", "modelo", "setor", "responsavel", "estado",
                            "localizacao_encontrada", "data_coleta", "observacoes" });

            // Aba 2: Itens Não Encontrados
            List<Map<String, Object>> naoEncontrados = relatorioDAO.gerarRelatorioItensNaoEncontrados(idInventario);
            criarAbaRelatorio(workbook, "Não Encontrados", naoEncontrados,
                    new String[] { "Patrimônio", "Descrição", "Marca", "Modelo", "Setor", "Responsável", "Situação",
                            "Última Localização", "Data Tentativa", "Motivo" },
                    new String[] { "patrimonio", "descricao", "marca", "modelo", "setor", "responsavel", "situacao",
                            "ultima_localizacao", "data_tentativa", "motivo" });

            // Aba 3: Sem Etiqueta
            List<Map<String, Object>> semEtiqueta = relatorioDAO.gerarRelatorioItensSemEtiqueta(idInventario);
            criarAbaRelatorio(workbook, "Sem Etiqueta", semEtiqueta,
                    new String[] { "Descrição", "Marca", "Modelo", "Quantidade", "Local", "Responsável", "Estado",
                            "Local de Coleta", "Data Coleta", "Observações" },
                    new String[] { "descricao", "marca", "modelo", "quantidade", "local", "responsavel", "estado",
                            "localizacao_encontrada", "data_coleta", "observacoes" });

            // Aba 4: Estatísticas
            List<Map<String, Object>> estatisticas = relatorioDAO.gerarEstatisticasGerais(idInventario);
            criarAbaEstatisticas(workbook, "Estatísticas", estatisticas);

            // Salvar
            String nomeArquivo = "relatorio_geral_inventario_"
                    + DateFormatUtils.formatForFilename(DateFormatUtils.nowAsDate());
            String caminhoArquivo = System.getProperty("user.home") + "/Desktop/" + nomeArquivo + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(caminhoArquivo);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            JOptionPane.showMessageDialog(parent,
                    "Relatório geral exportado com sucesso!\nArquivo salvo em: " + caminhoArquivo,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Cria uma aba de relatório no workbook
     */
    private void criarAbaRelatorio(Workbook workbook, String nomeAba, List<Map<String, Object>> dados,
            String[] colunas, String[] chaves) {
        Sheet sheet = workbook.createSheet(nomeAba);

        CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
        CellStyle estiloDados = criarEstiloDados(workbook);

        int linhaAtual = 0;

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

            for (int i = 0; i < colunas.length && i < chaves.length; i++) {
                Cell celula = linhaExcel.createCell(i);
                Object valor = linha.get(chaves[i]);

                if (valor != null) {
                    celula.setCellValue(sanitizarTextoParaExcel(valor.toString(), linhaAtual, i));
                } else {
                    celula.setCellValue("");
                }
                celula.setCellStyle(estiloDados);
            }
        }

        // Auto-ajustar colunas
        for (int i = 0; i < colunas.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Cria aba de estatísticas
     */
    private void criarAbaEstatisticas(Workbook workbook, String nomeAba, List<Map<String, Object>> dados) {
        Sheet sheet = workbook.createSheet(nomeAba);

        CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
        CellStyle estiloDados = criarEstiloDados(workbook);

        int linhaAtual = 0;

        // Cabeçalho
        Row linhaCabecalho = sheet.createRow(linhaAtual++);
        linhaCabecalho.createCell(0).setCellValue("Descrição");
        linhaCabecalho.createCell(1).setCellValue("Valor");
        linhaCabecalho.createCell(2).setCellValue("Percentual");

        for (Cell celula : linhaCabecalho) {
            celula.setCellStyle(estiloCabecalho);
        }

        // Dados
        for (Map<String, Object> linha : dados) {
            Row linhaExcel = sheet.createRow(linhaAtual++);

            linhaExcel.createCell(0)
                    .setCellValue(sanitizarTextoParaExcel(linha.get("descricao").toString(), linhaAtual, 0));
            linhaExcel.createCell(1)
                    .setCellValue(sanitizarTextoParaExcel(linha.get("valor").toString(), linhaAtual, 1));

            Object percentual = linha.get("percentual");
            if (percentual != null) {
                linhaExcel.createCell(2).setCellValue(sanitizarTextoParaExcel(percentual.toString(), linhaAtual, 2));
            }

            for (Cell celula : linhaExcel) {
                celula.setCellStyle(estiloDados);
            }
        }

        // Auto-ajustar colunas
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Métodos de estilo (reutilizados do ExcelExporter)
    private CellStyle criarEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        fonte.setFontHeightInPoints((short) 16);
        fonte.setColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFont(fonte);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private CellStyle criarEstiloCabecalho(Workbook workbook) {
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
        return estilo;
    }

    private CellStyle criarEstiloDados(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }

    private CellStyle criarEstiloNumero(Workbook workbook) {
        CellStyle estilo = criarEstiloDados(workbook);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }

    /**
     * Sanitiza texto para Excel, truncando se necessário
     */
    private String sanitizarTextoParaExcel(String texto, int linha, int coluna) {
        if (texto == null) {
            return "";
        }

        if (texto.length() > 32767) {
            System.out.println("⚠️ Texto truncado na linha " + linha + ", coluna " + coluna +
                    " (original: " + texto.length() + " chars → truncado para 32767)");
            return texto.substring(0, 32764) + "...";
        }

        return texto;
    }

}