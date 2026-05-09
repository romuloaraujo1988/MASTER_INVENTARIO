package com.inventario.sihcp.util;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.dto.EstatisticasHistoricoDTO;
import com.inventario.sihcp.dto.HistoricoColetaDTO;

/**
 * Gerador de relatórios Excel para histórico de coletas.
 * Utiliza Apache POI 5.x para criar planilhas Excel formatadas.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class HistoricoExcelGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoricoExcelGenerator.class);
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    // Estilos reutilizáveis
    private CellStyle styleTitulo;
    private CellStyle styleSubtitulo;
    private CellStyle styleCabecalho;
    private CellStyle styleDadoNormal;
    private CellStyle styleDadoCentro;
    private CellStyle styleMudancaLocalizacao;
    private CellStyle styleMudancaEstado;
    private CellStyle styleEstatistica;
    
    /**
     * Gera arquivo Excel com histórico de coletas de um patrimônio.
     * 
     * @param historico Lista de coletas ordenadas por data DESC
     * @param estatisticas Estatísticas calculadas do histórico
     * @return Bytes do arquivo Excel gerado
     * @throws Exception Se houver erro na geração
     */
    public byte[] gerarExcel(
            List<HistoricoColetaDTO> historico,
            EstatisticasHistoricoDTO estatisticas) throws Exception {
        
        logger.info("Iniciando geração de Excel com {} coletas", historico.size());
        
        if (historico.isEmpty()) {
            throw new IllegalArgumentException("Histórico vazio, não é possível gerar Excel");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Criar estilos
            criarEstilos(workbook);
            
            // Criar planilha
            Sheet sheet = workbook.createSheet("Histórico de Coletas");
            
            int rowNum = 0;
            
            // Adicionar cabeçalho
            rowNum = adicionarCabecalho(sheet, historico.get(0), rowNum);
            
            // Adicionar estatísticas
            rowNum = adicionarEstatisticas(sheet, estatisticas, rowNum);
            
            // Adicionar espaço
            rowNum += 2;
            
            // Adicionar tabela de histórico
            rowNum = adicionarTabelaHistorico(sheet, historico, rowNum);
            
            // Auto-ajustar largura das colunas
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
                // Adicionar margem extra
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Escrever para ByteArrayOutputStream
            workbook.write(baos);
            
            logger.info("Excel gerado com sucesso: {} bytes", baos.size());
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Erro ao gerar Excel", e);
            throw new Exception("Erro ao gerar Excel: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cria estilos reutilizáveis para o workbook.
     */
    private void criarEstilos(Workbook workbook) {
        // Fonte para título
        Font fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        
        // Fonte para subtítulo
        Font fontSubtitulo = workbook.createFont();
        fontSubtitulo.setBold(true);
        fontSubtitulo.setFontHeightInPoints((short) 11);
        
        // Fonte para cabeçalho
        Font fontCabecalho = workbook.createFont();
        fontCabecalho.setBold(true);
        fontCabecalho.setColor(IndexedColors.WHITE.getIndex());
        
        // Estilo título
        styleTitulo = workbook.createCellStyle();
        styleTitulo.setFont(fontTitulo);
        styleTitulo.setAlignment(HorizontalAlignment.CENTER);
        styleTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Estilo subtítulo
        styleSubtitulo = workbook.createCellStyle();
        styleSubtitulo.setFont(fontSubtitulo);
        
        // Estilo cabeçalho
        styleCabecalho = workbook.createCellStyle();
        styleCabecalho.setFont(fontCabecalho);
        styleCabecalho.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        styleCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleCabecalho.setAlignment(HorizontalAlignment.CENTER);
        styleCabecalho.setVerticalAlignment(VerticalAlignment.CENTER);
        styleCabecalho.setBorderBottom(BorderStyle.THIN);
        styleCabecalho.setBorderTop(BorderStyle.THIN);
        styleCabecalho.setBorderLeft(BorderStyle.THIN);
        styleCabecalho.setBorderRight(BorderStyle.THIN);
        
        // Estilo dado normal
        styleDadoNormal = workbook.createCellStyle();
        styleDadoNormal.setBorderBottom(BorderStyle.THIN);
        styleDadoNormal.setBorderTop(BorderStyle.THIN);
        styleDadoNormal.setBorderLeft(BorderStyle.THIN);
        styleDadoNormal.setBorderRight(BorderStyle.THIN);
        styleDadoNormal.setVerticalAlignment(VerticalAlignment.TOP);
        styleDadoNormal.setWrapText(true);
        
        // Estilo dado centro
        styleDadoCentro = workbook.createCellStyle();
        styleDadoCentro.cloneStyleFrom(styleDadoNormal);
        styleDadoCentro.setAlignment(HorizontalAlignment.CENTER);
        
        // Estilo mudança de localização (amarelo claro)
        styleMudancaLocalizacao = workbook.createCellStyle();
        styleMudancaLocalizacao.cloneStyleFrom(styleDadoNormal);
        styleMudancaLocalizacao.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        styleMudancaLocalizacao.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Estilo mudança de estado (vermelho claro)
        styleMudancaEstado = workbook.createCellStyle();
        styleMudancaEstado.cloneStyleFrom(styleDadoNormal);
        styleMudancaEstado.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        styleMudancaEstado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Estilo estatística
        styleEstatistica = workbook.createCellStyle();
        styleEstatistica.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        styleEstatistica.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleEstatistica.setAlignment(HorizontalAlignment.CENTER);
        styleEstatistica.setBorderBottom(BorderStyle.THIN);
        styleEstatistica.setBorderTop(BorderStyle.THIN);
        styleEstatistica.setBorderLeft(BorderStyle.THIN);
        styleEstatistica.setBorderRight(BorderStyle.THIN);
    }
    
    /**
     * Adiciona cabeçalho com informações do patrimônio.
     */
    private int adicionarCabecalho(Sheet sheet, HistoricoColetaDTO primeiraColeta, int rowNum) {
        // Título
        Row rowTitulo = sheet.createRow(rowNum++);
        Cell cellTitulo = rowTitulo.createCell(0);
        cellTitulo.setCellValue("HISTÓRICO DE COLETAS DE PATRIMÔNIO");
        cellTitulo.setCellStyle(styleTitulo);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));
        
        // Linha em branco
        rowNum++;
        
        // Informações do patrimônio
        Row rowInfo1 = sheet.createRow(rowNum++);
        Cell cellLabel1 = rowInfo1.createCell(0);
        cellLabel1.setCellValue("Número do Patrimônio:");
        cellLabel1.setCellStyle(styleSubtitulo);
        Cell cellValor1 = rowInfo1.createCell(1);
        cellValor1.setCellValue(primeiraColeta.getNumeroPatrimonio());
        
        Row rowInfo2 = sheet.createRow(rowNum++);
        Cell cellLabel2 = rowInfo2.createCell(0);
        cellLabel2.setCellValue("Descrição:");
        cellLabel2.setCellStyle(styleSubtitulo);
        Cell cellValor2 = rowInfo2.createCell(1);
        cellValor2.setCellValue(primeiraColeta.getDescricaoPatrimonio());
        
        return rowNum;
    }
    
    /**
     * Adiciona seção de estatísticas.
     */
    private int adicionarEstatisticas(Sheet sheet, EstatisticasHistoricoDTO stats, int rowNum) {
        // Linha em branco
        rowNum++;
        
        // Título da seção
        Row rowTituloStats = sheet.createRow(rowNum++);
        Cell cellTituloStats = rowTituloStats.createCell(0);
        cellTituloStats.setCellValue("Estatísticas do Histórico");
        cellTituloStats.setCellStyle(styleSubtitulo);
        
        // Calcular estatísticas
        stats.calcularDiasEntrePrimeiraEUltima();
        stats.calcularMediaColetasPorInventario();
        
        // Linha de cabeçalhos
        Row rowHeaderStats = sheet.createRow(rowNum++);
        String[] headersStats = {
            "Total Coletas", "Inventários", "Mudanças Local", "Mudanças Estado",
            "Primeira Coleta", "Última Coleta", "Período (dias)", "Média/Inventário"
        };
        for (int i = 0; i < headersStats.length; i++) {
            Cell cell = rowHeaderStats.createCell(i);
            cell.setCellValue(headersStats[i]);
            cell.setCellStyle(styleCabecalho);
        }
        
        // Linha de valores
        Row rowValoresStats = sheet.createRow(rowNum++);
        
        // Total de coletas
        Cell cellTotal = rowValoresStats.createCell(0);
        int totalColetas = 0;
        if (stats.getTotalColetas() != null) {
            totalColetas = stats.getTotalColetas();
        }
        cellTotal.setCellValue(totalColetas);
        cellTotal.setCellStyle(styleEstatistica);
        
        // Total de inventários
        Cell cellInv = rowValoresStats.createCell(1);
        int totalInventarios = 0;
        if (stats.getTotalInventarios() != null) {
            totalInventarios = stats.getTotalInventarios();
        }
        cellInv.setCellValue(totalInventarios);
        cellInv.setCellStyle(styleEstatistica);
        
        // Mudanças de localização
        Cell cellMudLoc = rowValoresStats.createCell(2);
        int totalMudancasLoc = 0;
        if (stats.getTotalMudancasLocalizacao() != null) {
            totalMudancasLoc = stats.getTotalMudancasLocalizacao();
        }
        cellMudLoc.setCellValue(totalMudancasLoc);
        cellMudLoc.setCellStyle(styleEstatistica);
        
        // Mudanças de estado
        Cell cellMudEst = rowValoresStats.createCell(3);
        int totalMudancasEst = 0;
        if (stats.getTotalMudancasEstado() != null) {
            totalMudancasEst = stats.getTotalMudancasEstado();
        }
        cellMudEst.setCellValue(totalMudancasEst);
        cellMudEst.setCellStyle(styleEstatistica);
        
        // Primeira coleta
        Cell cellPrimeira = rowValoresStats.createCell(4);
        cellPrimeira.setCellValue(stats.getPrimeiraColeta() != null ? 
                dateOnlyFormat.format(stats.getPrimeiraColeta()) : "N/A");
        cellPrimeira.setCellStyle(styleEstatistica);
        
        // Última coleta
        Cell cellUltima = rowValoresStats.createCell(5);
        cellUltima.setCellValue(stats.getUltimaColeta() != null ? 
                dateOnlyFormat.format(stats.getUltimaColeta()) : "N/A");
        cellUltima.setCellStyle(styleEstatistica);
        
        // Período em dias
        Cell cellDias = rowValoresStats.createCell(6);
        long diasEntrePrimeiraEUltima = 0L;
        if (stats.getDiasEntrePrimeiraEUltima() != null) {
            diasEntrePrimeiraEUltima = stats.getDiasEntrePrimeiraEUltima();
        }
        cellDias.setCellValue(diasEntrePrimeiraEUltima);
        cellDias.setCellStyle(styleEstatistica);
        
        // Média por inventário
        Cell cellMedia = rowValoresStats.createCell(7);
        if (stats.getMediaColetasPorInventario() != null) {
            cellMedia.setCellValue(String.format("%.2f", stats.getMediaColetasPorInventario()));
        } else {
            cellMedia.setCellValue("N/A");
        }
        cellMedia.setCellStyle(styleEstatistica);
        
        return rowNum;
    }
    
    /**
     * Adiciona tabela com histórico de coletas.
     */
    private int adicionarTabelaHistorico(Sheet sheet, List<HistoricoColetaDTO> historico, int rowNum) {
        // Título da seção
        Row rowTituloHist = sheet.createRow(rowNum++);
        Cell cellTituloHist = rowTituloHist.createCell(0);
        cellTituloHist.setCellValue("Histórico de Coletas");
        cellTituloHist.setCellStyle(styleSubtitulo);
        
        // Cabeçalho da tabela
        Row rowHeader = sheet.createRow(rowNum++);
        String[] headers = {
            "Data", "Inventário", "Coletor", "Localização", 
            "Estado", "Sala/Setor", "Observações"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styleCabecalho);
        }
        
        // Adicionar linhas de dados
        for (HistoricoColetaDTO coleta : historico) {
            Row row = sheet.createRow(rowNum++);
            adicionarLinhaColeta(row, coleta);
        }
        
        return rowNum;
    }
    
    /**
     * Adiciona linha de coleta na tabela.
     */
    private void adicionarLinhaColeta(Row row, HistoricoColetaDTO coleta) {
        int colNum = 0;
        
        // Data
        Cell cellData = row.createCell(colNum++);
        cellData.setCellValue(coleta.getDataColeta() != null ? 
                dateFormat.format(coleta.getDataColeta()) : "N/A");
        cellData.setCellStyle(styleDadoCentro);
        
        // Inventário
        Cell cellInv = row.createCell(colNum++);
        cellInv.setCellValue(coleta.getNomeInventario() != null ? 
                coleta.getNomeInventario() : "N/A");
        cellInv.setCellStyle(styleDadoNormal);
        
        // Coletor
        Cell cellColetor = row.createCell(colNum++);
        cellColetor.setCellValue(coleta.getNomeColetorCompleto() != null ? 
                coleta.getNomeColetorCompleto() : "N/A");
        cellColetor.setCellStyle(styleDadoNormal);
        
        // Localização (com destaque se mudou)
        Cell cellLoc = row.createCell(colNum++);
        String localizacao = coleta.getLocalizacaoEncontrada() != null ? 
                coleta.getLocalizacaoEncontrada() : "N/A";
        if (Boolean.TRUE.equals(coleta.getTemMudancaLocalizacao()) && 
                coleta.getLocalizacaoAnterior() != null) {
            localizacao += "\n(Anterior: " + coleta.getLocalizacaoAnterior() + ")";
        }
        cellLoc.setCellValue(localizacao);
        cellLoc.setCellStyle(Boolean.TRUE.equals(coleta.getTemMudancaLocalizacao()) ? 
                styleMudancaLocalizacao : styleDadoNormal);
        
        // Estado (com destaque se mudou)
        Cell cellEstado = row.createCell(colNum++);
        String estado = coleta.getEstadoEncontrado() != null ? 
                coleta.getEstadoEncontrado() : "N/A";
        if (Boolean.TRUE.equals(coleta.getTemMudancaEstado()) && 
                coleta.getEstadoAnterior() != null) {
            estado += "\n(Anterior: " + coleta.getEstadoAnterior() + ")";
        }
        cellEstado.setCellValue(estado);
        cellEstado.setCellStyle(Boolean.TRUE.equals(coleta.getTemMudancaEstado()) ? 
                styleMudancaEstado : styleDadoNormal);
        
        // Sala/Setor
        Cell cellSala = row.createCell(colNum++);
        String salaSetor = "";
        if (coleta.getNomeSala() != null) {
            salaSetor = coleta.getNomeSala();
            if (coleta.getNomeSetor() != null) {
                salaSetor += " / " + coleta.getNomeSetor();
            }
        } else if (coleta.getNomeSetor() != null) {
            salaSetor = coleta.getNomeSetor();
        } else {
            salaSetor = "N/A";
        }
        cellSala.setCellValue(salaSetor);
        cellSala.setCellStyle(styleDadoNormal);
        
        // Observações
        Cell cellObs = row.createCell(colNum++);
        String obs = coleta.getObservacoes() != null && !coleta.getObservacoes().isEmpty() ? 
                coleta.getObservacoes() : "-";
        cellObs.setCellValue(obs);
        cellObs.setCellStyle(styleDadoNormal);
    }
}
