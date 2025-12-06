package com.inventario.analytics.service;

import com.inventario.analytics.dao.AnalyticsDAO;
import com.inventario.analytics.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço principal de Analytics.
 * 
 * Coordena análises de divergências e métricas de tempo de coleta.
 */
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final AnalyticsDAO analyticsDAO;
    private final DivergenciaClassificadorService classificador;
    
    public AnalyticsService() {
        this.analyticsDAO = new AnalyticsDAO();
        this.classificador = new DivergenciaClassificadorService();
    }
    
    // ==================== KPIs ====================
    
    /**
     * Calcula KPIs do dashboard para um inventário.
     */
    public AnalyticsKPIs calcularKPIs(int idInventario) {
        Map<String, Object> dados = analyticsDAO.buscarKPIsGerais(idInventario);
        
        if (dados.isEmpty()) {
            return AnalyticsKPIs.empty();
        }
        
        return AnalyticsKPIs.builder()
                .totalColetas(getInt(dados, "total_coletas"))
                .totalDivergencias(getInt(dados, "total_divergencias"))
                .taxaDivergenciaGeral(getDouble(dados, "taxa_divergencia"))
                .tempoMedioColeta(getDouble(dados, "tempo_medio"))
                .coletasPorHora(getDouble(dados, "coletas_por_hora"))
                .coletoresAtivos(getInt(dados, "coletores_ativos"))
                .build();
    }
    
    // ==================== Divergências ====================
    
    /**
     * Analisa divergências de um inventário com classificação de gravidade.
     */
    public List<DivergenciaAnalytics> analisarDivergencias(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarDivergenciasComDetalhes(idInventario);
        
        return dados.stream()
                .map(this::mapearDivergencia)
                .collect(Collectors.toList());
    }
    
    /**
     * Agrupa divergências por tipo.
     */
    public Map<TipoDivergencia, Long> agruparDivergenciasPorTipo(int idInventario) {
        List<DivergenciaAnalytics> divergencias = analisarDivergencias(idInventario);
        
        return divergencias.stream()
                .collect(Collectors.groupingBy(
                        DivergenciaAnalytics::getTipo,
                        Collectors.counting()
                ));
    }
    
    /**
     * Agrupa divergências por gravidade.
     */
    public Map<GravidadeDivergencia, Long> agruparDivergenciasPorGravidade(int idInventario) {
        List<DivergenciaAnalytics> divergencias = analisarDivergencias(idInventario);
        
        return divergencias.stream()
                .collect(Collectors.groupingBy(
                        DivergenciaAnalytics::getGravidade,
                        Collectors.counting()
                ));
    }
    
    /**
     * Busca divergências agrupadas por dia para gráfico temporal.
     */
    public List<DivergenciaTemporal> buscarDivergenciasPorDia(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarDivergenciasPorPeriodo(idInventario, null, null);
        List<DivergenciaTemporal> resultado = new ArrayList<>();
        
        for (Map<String, Object> row : dados) {
            LocalDate data = (LocalDate) row.get("data");
            
            // Adiciona uma entrada para cada tipo de divergência
            int localizacao = getInt(row, "divergencias_localizacao");
            int estado = getInt(row, "divergencias_estado");
            int manual = getInt(row, "divergencias_manual");
            
            if (localizacao > 0) {
                resultado.add(new DivergenciaTemporal(data, TipoDivergencia.LOCALIZACAO, localizacao));
            }
            if (estado > 0) {
                resultado.add(new DivergenciaTemporal(data, TipoDivergencia.ESTADO, estado));
            }
            if (manual > 0) {
                resultado.add(new DivergenciaTemporal(data, TipoDivergencia.MANUAL, manual));
            }
        }
        
        return resultado;
    }
    
    // ==================== Métricas de Tempo ====================
    
    /**
     * Calcula métricas de tempo por coletor.
     */
    public List<MetricasColetorDTO> calcularMetricasPorColetor(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarMetricasTempoAgrupadas(idInventario, "COLETOR");
        List<MetricasColetorDTO> resultado = new ArrayList<>();
        
        int posicao = 1;
        for (Map<String, Object> row : dados) {
            int totalColetas = getInt(row, "total_coletas");
            int coletasComTempo = getInt(row, "coletas_com_tempo");
            
            MetricasColetorDTO dto = MetricasColetorDTO.builder()
                    .idColetor(getInt(row, "id_grupo"))
                    .nomeColetor(getString(row, "nome_grupo"))
                    .totalColetas(totalColetas)
                    .coletasComTempo(coletasComTempo)
                    .tempoMedio(getDouble(row, "tempo_medio"))
                    .tempoMinimo(getInt(row, "tempo_minimo"))
                    .tempoMaximo(getInt(row, "tempo_maximo"))
                    .desvioPadrao(getDouble(row, "desvio_padrao"))
                    .percentualComTempo(totalColetas > 0 ? (coletasComTempo * 100.0 / totalColetas) : 0)
                    .posicaoRanking(posicao++)
                    .build();
            
            resultado.add(dto);
        }
        
        return resultado;
    }
    
    /**
     * Calcula métricas por período do dia (Manhã/Tarde/Noite).
     */
    public Map<String, MetricasPeriodoDTO> calcularMetricasPorPeriodo(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarMetricasTempoAgrupadas(idInventario, "PERIODO");
        Map<String, MetricasPeriodoDTO> resultado = new LinkedHashMap<>();
        
        // Calcular média geral para identificar baixa produtividade
        double mediaGeral = dados.stream()
                .mapToDouble(row -> getDouble(row, "tempo_medio"))
                .filter(t -> t > 0)
                .average()
                .orElse(0);
        
        for (Map<String, Object> row : dados) {
            String periodo = getString(row, "id_grupo");
            double tempoMedio = getDouble(row, "tempo_medio");
            
            MetricasPeriodoDTO dto = MetricasPeriodoDTO.builder()
                    .periodo(periodo)
                    .totalColetas(getInt(row, "total_coletas"))
                    .tempoMedio(tempoMedio)
                    .coletasComTempo(getInt(row, "coletas_com_tempo"))
                    .baixaProdutividade(tempoMedio > mediaGeral * 1.5)
                    .build();
            
            resultado.put(periodo, dto);
        }
        
        return resultado;
    }
    
    /**
     * Calcula métricas por dia da semana.
     */
    public Map<String, MetricasPeriodoDTO> calcularMetricasPorDiaSemana(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarMetricasTempoAgrupadas(idInventario, "DIA_SEMANA");
        Map<String, MetricasPeriodoDTO> resultado = new LinkedHashMap<>();
        
        double mediaGeral = dados.stream()
                .mapToDouble(row -> getDouble(row, "tempo_medio"))
                .filter(t -> t > 0)
                .average()
                .orElse(0);
        
        for (Map<String, Object> row : dados) {
            String diaSemana = getString(row, "nome_grupo");
            double tempoMedio = getDouble(row, "tempo_medio");
            
            MetricasPeriodoDTO dto = MetricasPeriodoDTO.builder()
                    .periodo(diaSemana)
                    .totalColetas(getInt(row, "total_coletas"))
                    .tempoMedio(tempoMedio)
                    .coletasComTempo(getInt(row, "coletas_com_tempo"))
                    .baixaProdutividade(tempoMedio > mediaGeral * 1.5)
                    .build();
            
            resultado.put(diaSemana, dto);
        }
        
        return resultado;
    }
    
    // ==================== Rankings ====================
    
    /**
     * Gera ranking de setores por taxa de divergência.
     */
    public List<RankingSetorDTO> gerarRankingSetores(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarRankingSetores(idInventario);
        
        return dados.stream()
                .map(row -> RankingSetorDTO.builder()
                        .idSetor(getInt(row, "id_setor"))
                        .nomeSetor(getString(row, "nome_setor"))
                        .totalColetas(getInt(row, "total_coletas"))
                        .totalDivergencias(getInt(row, "total_divergencias"))
                        .taxaDivergencia(getDouble(row, "taxa_divergencia"))
                        .posicaoRanking(getInt(row, "posicao_ranking"))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Gera ranking de responsáveis por quantidade de divergências.
     */
    public List<RankingResponsavelDTO> gerarRankingResponsaveis(int idInventario) {
        List<Map<String, Object>> dados = analyticsDAO.buscarRankingResponsaveis(idInventario);
        
        return dados.stream()
                .map(row -> RankingResponsavelDTO.builder()
                        .idResponsavel(getInt(row, "id_responsavel"))
                        .nomeResponsavel(getString(row, "nome_responsavel"))
                        .totalPatrimonios(getInt(row, "total_patrimonios"))
                        .totalDivergencias(getInt(row, "total_divergencias"))
                        .posicaoRanking(getInt(row, "posicao_ranking"))
                        .build())
                .collect(Collectors.toList());
    }
    
    // ==================== Exportação ====================
    
    /**
     * Exporta dados de analytics para CSV.
     */
    public File exportarParaCSV(int idInventario, String diretorio) throws IOException {
        List<DivergenciaAnalytics> divergencias = analisarDivergencias(idInventario);
        
        String nomeArquivo = String.format("analytics_%d_%s.csv", 
                idInventario, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        File arquivo = new File(diretorio, nomeArquivo);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo))) {
            // Cabeçalho
            writer.println("Patrimonio,Descricao,Tipo,Gravidade,Valor Cadastrado,Valor Encontrado,Data Coleta,Setor,Responsavel,Coletor");
            
            // Dados
            for (DivergenciaAnalytics div : divergencias) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCsv(div.getNumeroPatrimonio()),
                        escapeCsv(div.getDescricaoPatrimonio()),
                        div.getTipo(),
                        div.getGravidade(),
                        escapeCsv(div.getValorCadastrado()),
                        escapeCsv(div.getValorEncontrado()),
                        div.getDataColeta() != null ? div.getDataColeta().format(ISO_FORMATTER) : "",
                        escapeCsv(div.getNomeSetor()),
                        escapeCsv(div.getNomeResponsavel()),
                        escapeCsv(div.getNomeColetor())
                );
            }
        }
        
        logger.info("Exportado CSV com {} registros para {}", divergencias.size(), arquivo.getAbsolutePath());
        return arquivo;
    }
    
    /**
     * Exporta dados de analytics para Excel com múltiplas abas.
     * Abas: Divergências, Métricas Tempo, Ranking Coletores, Ranking Setores
     */
    public File exportarParaExcel(int idInventario, String diretorio) throws IOException {
        String nomeArquivo = String.format("analytics_%d_%s.xlsx", 
                idInventario, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        File arquivo = new File(diretorio, nomeArquivo);
        
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            
            // Estilo para cabeçalhos
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            // Aba 1: Divergências
            criarAbaDivergencias(workbook, headerStyle, idInventario);
            
            // Aba 2: Métricas de Tempo
            criarAbaMetricasTempo(workbook, headerStyle, idInventario);
            
            // Aba 3: Ranking Coletores
            criarAbaRankingColetores(workbook, headerStyle, idInventario);
            
            // Aba 4: Ranking Setores
            criarAbaRankingSetores(workbook, headerStyle, idInventario);
            
            // Salvar arquivo
            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                workbook.write(fos);
            }
        }
        
        logger.info("Exportado Excel para {}", arquivo.getAbsolutePath());
        return arquivo;
    }
    
    private void criarAbaDivergencias(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, 
                                       org.apache.poi.ss.usermodel.CellStyle headerStyle, 
                                       int idInventario) {
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Divergências");
        
        // Cabeçalho
        String[] headers = {"Patrimônio", "Descrição", "Tipo", "Gravidade", "Valor Cadastrado", 
                           "Valor Encontrado", "Data Coleta", "Setor", "Responsável", "Coletor"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dados
        List<DivergenciaAnalytics> divergencias = analisarDivergencias(idInventario);
        int rowNum = 1;
        for (DivergenciaAnalytics div : divergencias) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(div.getNumeroPatrimonio() != null ? div.getNumeroPatrimonio() : "");
            row.createCell(1).setCellValue(div.getDescricaoPatrimonio() != null ? div.getDescricaoPatrimonio() : "");
            row.createCell(2).setCellValue(div.getTipo() != null ? div.getTipo().name() : "");
            row.createCell(3).setCellValue(div.getGravidade() != null ? div.getGravidade().name() : "");
            row.createCell(4).setCellValue(div.getValorCadastrado() != null ? div.getValorCadastrado() : "");
            row.createCell(5).setCellValue(div.getValorEncontrado() != null ? div.getValorEncontrado() : "");
            row.createCell(6).setCellValue(div.getDataColeta() != null ? div.getDataColeta().format(ISO_FORMATTER) : "");
            row.createCell(7).setCellValue(div.getNomeSetor() != null ? div.getNomeSetor() : "");
            row.createCell(8).setCellValue(div.getNomeResponsavel() != null ? div.getNomeResponsavel() : "");
            row.createCell(9).setCellValue(div.getNomeColetor() != null ? div.getNomeColetor() : "");
        }
        
        // Auto-ajustar colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void criarAbaMetricasTempo(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, 
                                        org.apache.poi.ss.usermodel.CellStyle headerStyle, 
                                        int idInventario) {
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Métricas Tempo");
        
        // Cabeçalho
        String[] headers = {"Período", "Total Coletas", "Coletas com Tempo", "Tempo Médio (s)", "Baixa Produtividade"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dados por período
        Map<String, MetricasPeriodoDTO> metricas = calcularMetricasPorPeriodo(idInventario);
        int rowNum = 1;
        for (MetricasPeriodoDTO dto : metricas.values()) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getPeriodo() != null ? dto.getPeriodo() : "");
            row.createCell(1).setCellValue(dto.getTotalColetas());
            row.createCell(2).setCellValue(dto.getColetasComTempo());
            row.createCell(3).setCellValue(dto.getTempoMedio());
            row.createCell(4).setCellValue(dto.isBaixaProdutividade() ? "Sim" : "Não");
        }
        
        // Auto-ajustar colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void criarAbaRankingColetores(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, 
                                           org.apache.poi.ss.usermodel.CellStyle headerStyle, 
                                           int idInventario) {
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Ranking Coletores");
        
        // Cabeçalho
        String[] headers = {"Posição", "Coletor", "Total Coletas", "Tempo Médio (s)", "Tempo Mín (s)", 
                           "Tempo Máx (s)", "Desvio Padrão", "% com Tempo"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dados
        List<MetricasColetorDTO> coletores = calcularMetricasPorColetor(idInventario);
        int rowNum = 1;
        for (MetricasColetorDTO dto : coletores) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getPosicaoRanking());
            row.createCell(1).setCellValue(dto.getNomeColetor() != null ? dto.getNomeColetor() : "");
            row.createCell(2).setCellValue(dto.getTotalColetas());
            row.createCell(3).setCellValue(dto.getTempoMedio());
            row.createCell(4).setCellValue(dto.getTempoMinimo());
            row.createCell(5).setCellValue(dto.getTempoMaximo());
            row.createCell(6).setCellValue(dto.getDesvioPadrao());
            row.createCell(7).setCellValue(dto.getPercentualComTempo());
        }
        
        // Auto-ajustar colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void criarAbaRankingSetores(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, 
                                         org.apache.poi.ss.usermodel.CellStyle headerStyle, 
                                         int idInventario) {
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Ranking Setores");
        
        // Cabeçalho
        String[] headers = {"Posição", "Setor", "Total Coletas", "Total Divergências", "Taxa Divergência (%)"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dados
        List<RankingSetorDTO> setores = gerarRankingSetores(idInventario);
        int rowNum = 1;
        for (RankingSetorDTO dto : setores) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getPosicaoRanking());
            row.createCell(1).setCellValue(dto.getNomeSetor() != null ? dto.getNomeSetor() : "");
            row.createCell(2).setCellValue(dto.getTotalColetas());
            row.createCell(3).setCellValue(dto.getTotalDivergencias());
            row.createCell(4).setCellValue(dto.getTaxaDivergencia());
        }
        
        // Auto-ajustar colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    // ==================== Métodos Auxiliares ====================
    
    private DivergenciaAnalytics mapearDivergencia(Map<String, Object> row) {
        String nomeSala = getString(row, "nome_sala");
        String localizacaoEncontrada = getString(row, "localizacao_encontrada");
        String estadoCadastrado = getString(row, "estado_cadastrado");
        String estadoEncontrado = getString(row, "estado_encontrado");
        boolean divergenciaManual = getBoolean(row, "divergencia");
        String motivo = getString(row, "motivo_divergencia");
        
        // Detectar tipo de divergência
        TipoDivergencia tipo;
        String valorCadastrado;
        String valorEncontrado;
        
        if (divergenciaManual && motivo != null && !motivo.isEmpty()) {
            tipo = TipoDivergencia.MANUAL;
            valorCadastrado = "";
            valorEncontrado = motivo;
        } else if (!Objects.equals(nomeSala, localizacaoEncontrada)) {
            tipo = TipoDivergencia.LOCALIZACAO;
            valorCadastrado = nomeSala;
            valorEncontrado = localizacaoEncontrada;
        } else if (!Objects.equals(estadoCadastrado, estadoEncontrado)) {
            tipo = TipoDivergencia.ESTADO;
            valorCadastrado = estadoCadastrado;
            valorEncontrado = estadoEncontrado;
        } else {
            tipo = TipoDivergencia.OUTRO;
            valorCadastrado = "";
            valorEncontrado = "";
        }
        
        // Classificar gravidade
        GravidadeDivergencia gravidade;
        switch (tipo) {
            case LOCALIZACAO:
                gravidade = classificador.classificarDivergenciaLocalizacao(
                        nomeSala, localizacaoEncontrada,
                        getInteger(row, "id_setor"), null
                );
                break;
            case ESTADO:
                gravidade = classificador.classificarDivergenciaEstado(estadoCadastrado, estadoEncontrado);
                break;
            case MANUAL:
                gravidade = GravidadeDivergencia.MEDIA;
                break;
            default:
                gravidade = GravidadeDivergencia.BAIXA;
        }
        
        java.sql.Timestamp ts = (java.sql.Timestamp) row.get("data_coleta");
        LocalDateTime dataColeta = ts != null ? ts.toLocalDateTime() : null;
        
        return DivergenciaAnalytics.builder()
                .idColeta(getInt(row, "id_coleta"))
                .numeroPatrimonio(getString(row, "numero_patrimonio"))
                .descricaoPatrimonio(getString(row, "descricao_patrimonio"))
                .tipo(tipo)
                .gravidade(gravidade)
                .valorCadastrado(valorCadastrado)
                .valorEncontrado(valorEncontrado)
                .motivo(motivo)
                .dataColeta(dataColeta)
                .idSetor(getInteger(row, "id_setor"))
                .nomeSetor(getString(row, "nome_setor"))
                .idResponsavel(getInteger(row, "id_responsavel"))
                .nomeResponsavel(getString(row, "nome_responsavel"))
                .idColetor(getInteger(row, "id_coletor"))
                .nomeColetor(getString(row, "nome_coletor"))
                .build();
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }
    
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
    
    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }
    
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return false;
    }
}
