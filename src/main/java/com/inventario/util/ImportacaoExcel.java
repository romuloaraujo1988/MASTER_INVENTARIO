package com.inventario.util;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.SalaDAORefactored;
import com.inventario.dao.SetorDAORefactored;
import com.inventario.model.Patrimonio;
import com.inventario.model.Responsavel;
import com.inventario.model.Sala;
import com.inventario.model.Setor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;

/**
 * Classe responsável pela importação de dados Excel do SUAP
 * Processa arquivos .xlsx/.xls exportados do SUAP e importa os dados para o banco
 * Mapeia o cabeçalho do Excel para os índices esperados pela importação CSV.
 */
public class ImportacaoExcel {

    // DAOs para acesso aos dados
    private final PatrimonioDAO patrimonioDAO;
    private final ResponsavelDAO responsavelDAO;
    private final SalaDAORefactored salaDAO;
    private final SetorDAORefactored setorDAO;

    // Cache para otimização
    private final Map<String, Responsavel> cacheResponsaveis = new HashMap<>();
    private final Map<String, Sala> cacheSalas = new HashMap<>();
    private final Map<String, Setor> cacheSetores = new HashMap<>();

    // Callback para progresso
    private ImportacaoCSV.ProgressCallback progressCallback;

    // Contadores para relatório
    private int linhasProcessadas = 0;
    private int itensInseridos = 0;
    private int itensAtualizados = 0;
    private int erros = 0;
    private final List<String> listaErros = new ArrayList<>();

    public ImportacaoExcel() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.responsavelDAO = new ResponsavelDAO();
        this.salaDAO = new SalaDAORefactored();
        this.setorDAO = new SetorDAORefactored();
    }

    /**
     * Importa patrimônios do arquivo Excel (.xlsx/.xls)
     */
    public ImportacaoCSV.RelatorioImportacao importarPatrimonios(String caminhoArquivo, ImportacaoCSV.ProgressCallback callback) {
        this.progressCallback = callback;
        resetarContadores();

        if (progressCallback != null) {
            progressCallback.onInfo("Iniciando leitura do arquivo Excel...");
        }

        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                registrarErro("Arquivo Excel sem planilhas.");
                return gerarRelatorio();
            }

            int firstRowNum = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(firstRowNum);
            if (headerRow == null) {
                registrarErro("Cabeçalho não encontrado na planilha.");
                return gerarRelatorio();
            }

            Map<Integer, Integer> mappingConstanteParaColunaExcel = mapearCabecalho(headerRow);

            if (!mappingConstanteParaColunaExcel.containsKey(ImportacaoCSVConstants.COL_NUMERO)) {
                registrarErro("Cabeçalho não contém a coluna de Número do patrimônio.");
                return gerarRelatorio();
            }

            // Processar linhas de dados
            for (int r = firstRowNum + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                try {
                    String[] campos = extrairCamposDaLinha(row, mappingConstanteParaColunaExcel);
                    // Validação mínima
                    if (campos.length < 18) {
                        registrarErro("Linha " + (r + 1) + ": Número insuficiente de campos (" + campos.length + ")");
                        continue;
                    }

                    String numeroPatrimonio = campos[ImportacaoCSVConstants.COL_NUMERO] != null
                            ? campos[ImportacaoCSVConstants.COL_NUMERO].trim() : "";

                    if (numeroPatrimonio.isEmpty()) {
                        registrarErro("Linha " + (r + 1) + ": Número do patrimônio vazio");
                        continue;
                    }

                    Patrimonio patrimonioEncontrado = patrimonioDAO.buscarPorNumero(numeroPatrimonio);
                    if (patrimonioEncontrado != null) {
                        preencherPatrimonio(patrimonioEncontrado, campos);
                        try {
                            patrimonioDAO.update(patrimonioEncontrado);
                            itensAtualizados++;
                        } catch (SQLException e) {
                            registrarErro("Linha " + (r + 1) + ": Erro ao atualizar patrimônio " + numeroPatrimonio + " - " + e.getMessage());
                        }
                    } else {
                        Patrimonio novoPatrimonio = new Patrimonio();
                        preencherPatrimonio(novoPatrimonio, campos);
                        try {
                            patrimonioDAO.insert(novoPatrimonio);
                            itensInseridos++;
                        } catch (SQLException e) {
                            registrarErro("Linha " + (r + 1) + ": Erro ao inserir patrimônio " + numeroPatrimonio + " - " + e.getMessage());
                        }
                    }

                } catch (Exception e) {
                    registrarErro("Linha " + (r + 1) + ": Erro ao processar - " + e.getMessage());
                }

                linhasProcessadas++;

                if (linhasProcessadas % 50 == 0) {
                    String mensagem = String.format("Processadas %d linhas - Inseridos: %d, Atualizados: %d, Erros: %d",
                            linhasProcessadas, itensInseridos, itensAtualizados, erros);
                    if (progressCallback != null) {
                        progressCallback.onProgress(linhasProcessadas, mensagem);
                    }
                }
            }

            if (progressCallback != null) {
                progressCallback.onInfo("Processamento concluído. Gerando relatório...");
            }

        } catch (IOException e) {
            registrarErro("Erro ao ler arquivo Excel: " + e.getMessage());
        }

        return gerarRelatorio();
    }

    // ==================== Auxiliares ====================

    /**
     * Mapeia cabeçalhos do Excel para constantes de coluna esperadas.
     */
    private Map<Integer, Integer> mapearCabecalho(Row headerRow) {
        Map<Integer, Integer> mapping = new HashMap<>();

        Map<String, Integer> nomeNormalizadoParaConstante = new HashMap<>();
        nomeNormalizadoParaConstante.put("NUMERO", ImportacaoCSVConstants.COL_NUMERO);
        nomeNormalizadoParaConstante.put("STATUS", ImportacaoCSVConstants.COL_STATUS);
        nomeNormalizadoParaConstante.put("ED", ImportacaoCSVConstants.COL_ED);
        nomeNormalizadoParaConstante.put("DESCRICAO", ImportacaoCSVConstants.COL_DESCRICAO);
        nomeNormalizadoParaConstante.put("ROTULOS", ImportacaoCSVConstants.COL_ROTULOS);
        nomeNormalizadoParaConstante.put("CARGA ATUAL", ImportacaoCSVConstants.COL_CARGA_ATUAL);
        nomeNormalizadoParaConstante.put("SETOR RESPONSAVEL", ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("CAMPUS", ImportacaoCSVConstants.COL_CAMPUS);
        nomeNormalizadoParaConstante.put("VALOR AQUISICAO", ImportacaoCSVConstants.COL_VALOR_AQUISICAO);
        nomeNormalizadoParaConstante.put("VALOR DEPRECIADO", ImportacaoCSVConstants.COL_VALOR_DEPRECIADO);
        nomeNormalizadoParaConstante.put("NUMERO NOTA FISCAL", ImportacaoCSVConstants.COL_NUMERO_NOTA_FISCAL);
        nomeNormalizadoParaConstante.put("NUMERO SERIE", ImportacaoCSVConstants.COL_NUMERO_SERIE);
        nomeNormalizadoParaConstante.put("DATA ENTRADA", ImportacaoCSVConstants.COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA CARGA", ImportacaoCSVConstants.COL_DATA_CARGA);
        nomeNormalizadoParaConstante.put("FORNECEDOR", ImportacaoCSVConstants.COL_FORNECEDOR);
        nomeNormalizadoParaConstante.put("SALA", ImportacaoCSVConstants.COL_SALA);
        nomeNormalizadoParaConstante.put("ESTADO CONSERVACAO", ImportacaoCSVConstants.COL_ESTADO_CONSERVACAO);

        short lastCellNum = headerRow.getLastCellNum();
        for (int c = headerRow.getFirstCellNum(); c < lastCellNum; c++) {
            Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String valor = cell != null ? getCellString(cell).trim() : "";
            String normalizado = normalizarCabecalho(valor);
            if (nomeNormalizadoParaConstante.containsKey(normalizado)) {
                mapping.put(nomeNormalizadoParaConstante.get(normalizado), c);
            }
        }
        return mapping;
    }

    private String normalizarCabecalho(String s) {
        if (s == null) return "";
        String semAcento = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        semAcento = semAcento.replaceAll("[^A-Za-z0-9 ]", " ");
        semAcento = semAcento.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
        return semAcento;
    }

    /**
     * Extrai os campos da linha com base no mapeamento de colunas.
     * O array retornado segue o layout do CSV, com índices de 0..17 (0 não usado).
     */
    private String[] extrairCamposDaLinha(Row row, Map<Integer, Integer> mappingConstanteParaColunaExcel) {
        String[] campos = new String[18];
        campos[0] = ""; // índice 0 não utilizado

        for (Map.Entry<Integer, Integer> entry : mappingConstanteParaColunaExcel.entrySet()) {
            int constante = entry.getKey();
            int colExcel = entry.getValue();
            Cell cell = row.getCell(colExcel, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String valor = getCellString(cell);
            campos[constante] = valor;
        }

        // Garantir que campos obrigatórios existam (mesmo que vazios)
        for (int i = 1; i <= 17; i++) {
            if (campos[i] == null) campos[i] = "";
        }
        return campos;
    }

    /**
     * Converte uma célula do Excel para string, respeitando datas e números inteiros.
     */
    private String getCellString(Cell cell) {
        if (cell == null) return "";

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        switch (type) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return DateFormatUtils.formatDate(date); // dd/MM/yyyy
                } else {
                    double d = cell.getNumericCellValue();
                    long l = (long) d;
                    if (Double.compare(d, (double) l) == 0) {
                        return String.valueOf(l);
                    } else {
                        // Evitar notação científica
                        BigDecimal bd = new BigDecimal(Double.toString(d));
                        bd = bd.stripTrailingZeros();
                        return bd.toPlainString();
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case BLANK:
            default:
                return "";
        }
    }

    /**
     * Preenche um objeto Patrimonio com dados dos campos
     */
    private void preencherPatrimonio(Patrimonio patrimonio, String[] campos) {
        try {
            // Dados básicos
            patrimonio.setNumero(valor(campos, ImportacaoCSVConstants.COL_NUMERO));
            patrimonio.setStatus(valor(campos, ImportacaoCSVConstants.COL_STATUS));
            patrimonio.setDescricao(valor(campos, ImportacaoCSVConstants.COL_DESCRICAO));
            patrimonio.setRotulos(valor(campos, ImportacaoCSVConstants.COL_ROTULOS));

            // Valores monetários
            patrimonio.setValorAquisicao(parseValorMonetario(valor(campos, ImportacaoCSVConstants.COL_VALOR_AQUISICAO)));
            patrimonio.setValorDepreciado(parseValorMonetario(valor(campos, ImportacaoCSVConstants.COL_VALOR_DEPRECIADO)));

            // Outros campos
            patrimonio.setNumeroNotaFiscal(valor(campos, ImportacaoCSVConstants.COL_NUMERO_NOTA_FISCAL));
            patrimonio.setNumeroSerie(valor(campos, ImportacaoCSVConstants.COL_NUMERO_SERIE));
            patrimonio.setFornecedor(valor(campos, ImportacaoCSVConstants.COL_FORNECEDOR));
            patrimonio.setEstadoConservacao(valor(campos, ImportacaoCSVConstants.COL_ESTADO_CONSERVACAO));

            // Datas
            Date dataEntrada = DateFormatUtils.parseDate(valor(campos, ImportacaoCSVConstants.COL_DATA_ENTRADA));
            if (dataEntrada != null) {
                patrimonio.setDataEntrada(new java.sql.Date(dataEntrada.getTime()));
            }

            Date dataCarga = DateFormatUtils.parseDateTime(valor(campos, ImportacaoCSVConstants.COL_DATA_CARGA));
            if (dataCarga != null) {
                patrimonio.setDataCarga(new java.sql.Timestamp(dataCarga.getTime()));
            }

            // Responsável
            String nomeResponsavel = extrairNomeResponsavel(valor(campos, ImportacaoCSVConstants.COL_CARGA_ATUAL));
            String setorResponsavel = valor(campos, ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);

            if (!nomeResponsavel.isEmpty()) {
                Responsavel responsavel = obterOuCriarResponsavel(nomeResponsavel, setorResponsavel);
                if (responsavel != null) {
                    patrimonio.setIdResponsavel(responsavel.getId());
                }
            }

            // Sala
            String nomeSala = valor(campos, ImportacaoCSVConstants.COL_SALA);
            if (!nomeSala.isEmpty()) {
                Sala sala = obterOuCriarSala(nomeSala, setorResponsavel);
                if (sala != null) {
                    patrimonio.setIdSala(sala.getIdSala());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao preencher patrimônio: " + e.getMessage(), e);
        }
    }

    private String valor(String[] campos, int idx) {
        return (campos != null && idx >= 0 && idx < campos.length && campos[idx] != null) ? campos[idx].trim() : "";
    }

    private String extrairNomeResponsavel(String cargaAtual) {
        if (cargaAtual == null || cargaAtual.trim().isEmpty() || cargaAtual.equals("-")) {
            return "";
        }
        int indiceParen = cargaAtual.indexOf('(');
        if (indiceParen > 0) {
            return cargaAtual.substring(0, indiceParen).trim();
        }
        return cargaAtual.trim();
    }

    private Responsavel obterOuCriarResponsavel(String nomeCompleto, String nomeSetor) {
        if (cacheResponsaveis.containsKey(nomeCompleto)) {
            return cacheResponsaveis.get(nomeCompleto);
        }
        try {
            List<Responsavel> responsaveisEncontrados = responsavelDAO.buscarPorNome(nomeCompleto);
            if (!responsaveisEncontrados.isEmpty()) {
                Responsavel responsavel = responsaveisEncontrados.get(0);
                cacheResponsaveis.put(nomeCompleto, responsavel);
                return responsavel;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        Responsavel novoResponsavel = new Responsavel();
        novoResponsavel.setNome(nomeCompleto);
        novoResponsavel.setAtivo(true);
        if (nomeSetor != null && !nomeSetor.isEmpty()) {
            Setor setor = obterOuCriarSetor(nomeSetor);
            if (setor != null) {
                novoResponsavel.setIdSetor(setor.getId());
            }
        }
        try {
            responsavelDAO.insert(novoResponsavel);
            if (novoResponsavel.getId() > 0) {
                cacheResponsaveis.put(nomeCompleto, novoResponsavel);
                return novoResponsavel;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        return null;
    }

    private Sala obterOuCriarSala(String nomeSala, String nomeSetor) {
        if (cacheSalas.containsKey(nomeSala)) {
            return cacheSalas.get(nomeSala);
        }
        try {
            List<Sala> salasEncontradas = salaDAO.buscarPorFiltro(nomeSala, null, null, null, null, null, true);
            if (!salasEncontradas.isEmpty()) {
                Sala sala = salasEncontradas.get(0);
                cacheSalas.put(nomeSala, sala);
                return sala;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        Sala novaSala = new Sala();
        novaSala.setDescricao(nomeSala);
        novaSala.setNumeroSala(extrairNumeroSala(nomeSala));
        novaSala.setAtivo(true);
        if (nomeSetor != null && !nomeSetor.isEmpty()) {
            Setor setor = obterOuCriarSetor(nomeSetor);
            if (setor != null) {
                novaSala.setIdSetor(setor.getId());
            }
        }
        try {
            salaDAO.insert(novaSala);
            if (novaSala.getIdSala() > 0) {
                cacheSalas.put(nomeSala, novaSala);
                return novaSala;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        return null;
    }

    private String extrairNumeroSala(String nomeSala) {
        String[] partes = nomeSala.split("\\s+");
        for (String parte : partes) {
            if (parte.matches("\\d+")) {
                return parte;
            }
        }
        return nomeSala;
    }

    private Setor obterOuCriarSetor(String nomeSetor) {
        if (cacheSetores.containsKey(nomeSetor)) {
            return cacheSetores.get(nomeSetor);
        }
        try {
            // Buscar por nome exato primeiro
            Setor setorEncontrado = setorDAO.buscarPorNome(nomeSetor);
            if (setorEncontrado != null) {
                cacheSetores.put(nomeSetor, setorEncontrado);
                return setorEncontrado;
            }
            
            // Se não encontrou, buscar por termo similar
            List<Setor> setoresEncontrados = setorDAO.buscarPorTermo(nomeSetor);
            if (!setoresEncontrados.isEmpty()) {
                Setor setor = setoresEncontrados.get(0);
                cacheSetores.put(nomeSetor, setor);
                return setor;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        
        // Criar novo setor
        Setor novoSetor = new Setor();
        novoSetor.setNome(nomeSetor);
        novoSetor.setAtivo(true);
        try {
            setorDAO.insert(novoSetor);
            if (novoSetor.getId() > 0) {
                cacheSetores.put(nomeSetor, novoSetor);
                return novoSetor;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        return null;
    }

    private BigDecimal parseValorMonetario(String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.equals("-")) {
            return BigDecimal.ZERO;
        }
        try {
            String valorLimpo = valor.trim().replace(",", ".");
            return new BigDecimal(valorLimpo);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void resetarContadores() {
        linhasProcessadas = 0;
        itensInseridos = 0;
        itensAtualizados = 0;
        erros = 0;
        listaErros.clear();
        cacheResponsaveis.clear();
        cacheSalas.clear();
        cacheSetores.clear();
    }

    private void registrarErro(String erro) {
        listaErros.add(erro);
        erros++;
        if (progressCallback != null) {
            progressCallback.onError(erro);
        }
    }

    private ImportacaoCSV.RelatorioImportacao gerarRelatorio() {
        return new ImportacaoCSV.RelatorioImportacao(
                linhasProcessadas,
                itensInseridos,
                itensAtualizados,
                erros,
                new ArrayList<>(listaErros)
        );
    }

    /**
     * Constantes dos índices de coluna esperados, espelhando ImportacaoCSV.
     * Mantemos aqui para evitar acesso a membros privados.
     */
    public static class ImportacaoCSVConstants {
        public static final int COL_NUMERO = 1;
        public static final int COL_STATUS = 2;
        public static final int COL_ED = 3;
        public static final int COL_DESCRICAO = 4;
        public static final int COL_ROTULOS = 5;
        public static final int COL_CARGA_ATUAL = 6;
        public static final int COL_SETOR_RESPONSAVEL = 7;
        public static final int COL_CAMPUS = 8;
        public static final int COL_VALOR_AQUISICAO = 9;
        public static final int COL_VALOR_DEPRECIADO = 10;
        public static final int COL_NUMERO_NOTA_FISCAL = 11;
        public static final int COL_NUMERO_SERIE = 12;
        public static final int COL_DATA_ENTRADA = 13;
        public static final int COL_DATA_CARGA = 14;
        public static final int COL_FORNECEDOR = 15;
        public static final int COL_SALA = 16;
        public static final int COL_ESTADO_CONSERVACAO = 17;
    }
}

