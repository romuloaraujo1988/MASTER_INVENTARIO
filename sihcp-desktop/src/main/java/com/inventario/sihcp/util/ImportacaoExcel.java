package com.inventario.sihcp.util;

import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.ResponsavelDAO;
import com.inventario.sihcp.dao.SalaDAO;
import com.inventario.sihcp.dao.SetorDAO;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Responsavel;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.model.Setor;
import com.inventario.sihcp.event.DashboardEvent;
import com.inventario.sihcp.event.DashboardEventBus;
import com.inventario.sihcp.event.DashboardEventType;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
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
    private final SalaDAO salaDAO;
    private final SetorDAO setorDAO;

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
        this.salaDAO = new SalaDAO();
        this.setorDAO = new SetorDAO();
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

        // OTIMIZAÇÃO: Pré-carregar dados em memória para evitar N queries por linha
        Map<String, Integer> patrimoniosExistentes = preCarregarPatrimonios();
        preCarregarResponsaveis();
        preCarregarSalas();
        preCarregarSetores();

        if (progressCallback != null) {
            progressCallback.onInfo("Cache carregado: " + patrimoniosExistentes.size() + " patrimônios, "
                    + cacheResponsaveis.size() + " responsáveis, "
                    + cacheSalas.size() + " salas, "
                    + cacheSetores.size() + " setores em memória.");
        }

        Connection conn = null;
        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false); // Ativar modo manual para performance
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                registrarErro("Arquivo Excel sem planilhas.");
                workbook.close();
                return gerarRelatorio();
            }

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            Row headerRow = null;
            int headerRowIndex = -1;

            // Tentar localizar a linha de cabeçalho (procurar nas primeiras 10 linhas)
            for (int i = firstRowNum; i <= Math.min(firstRowNum + 10, lastRowNum); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                
                boolean encontrou = false;
                for (Cell c : r) {
                    if (c != null && c.getCellType() == CellType.STRING) {
                        String val = normalizarCabecalho(c.getStringCellValue());
                        if (val.contains("PATRIMONIO") || val.contains("NUMERO") || val.contains("DESCRICAO")) {
                            encontrou = true;
                            break;
                        }
                    }
                }
                
                if (encontrou) {
                    headerRow = r;
                    headerRowIndex = i;
                    break;
                }
            }

            if (headerRow == null) {
                registrarErro("Cabeçalho não encontrado nas primeiras 10 linhas da planilha.");
                workbook.close();
                return gerarRelatorio();
            }

            System.out.println(">>> 🎯 Linha de cabeçalho detectada: " + (headerRowIndex + 1));

            Map<Integer, Integer> mappingConstanteParaColunaExcel = mapearCabecalho(headerRow);

            if (!mappingConstanteParaColunaExcel.containsKey(ImportacaoCSVConstants.COL_NUMERO)) {
                registrarErro("Cabeçalho identificado mas coluna de Número do patrimônio não encontrada.");
                workbook.close();
                return gerarRelatorio();
            }

            // Processar linhas de dados
            int totalLinhas = sheet.getLastRowNum();
            for (int r = headerRowIndex + 1; r <= totalLinhas; r++) {
                // VERIFICAÇÃO DE CONEXÃO: Recuperar se cair durante o processo longo (ex: timeout)
                try {
                    if (conn == null || conn.isClosed() || !conn.isValid(5)) {
                        System.out.println(">>> ⚠️ Conexão perdida na linha " + (r + 1) + ". Tentando reconectar...");
                        if (progressCallback != null) progressCallback.onInfo("Conexão perdida. Reconectando...");
                        try { if (conn != null) conn.close(); } catch (Exception e) {}
                        conn = ConnectionManager.getConnection();
                        conn.setAutoCommit(false);
                    }
                } catch (SQLException e) {
                    registrarErro("Erro ao tentar reconectar na linha " + (r + 1) + ": " + e.getMessage());
                    if (progressCallback != null) progressCallback.onError("Falha crítica de conexão: " + e.getMessage());
                    break; // Se não consegue reconectar, para tudo
                }

                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                try {
                    String[] campos = extrairCamposDaLinha(row, mappingConstanteParaColunaExcel);
                    if (campos.length < 18) {
                        registrarErro("Linha " + (r + 1) + ": Número insuficiente de campos");
                        continue;
                    }

                    String numeroPatrimonio = campos[ImportacaoCSVConstants.COL_NUMERO] != null
                            ? campos[ImportacaoCSVConstants.COL_NUMERO].trim() : "";

                    if (numeroPatrimonio.isEmpty()) {
                        registrarErro("Linha " + (r + 1) + ": Número do patrimônio vazio");
                        continue;
                    }

                    Patrimonio patrimonioEncontrado = null;
                    Integer idExistente = patrimoniosExistentes.get(numeroPatrimonio);
                    
                    if (idExistente != null) {
                        try {
                            // OTIMIZAÇÃO: Usar findById básico em vez de buscarPorIdComJoins (que é lento)
                            patrimonioEncontrado = patrimonioDAO.findById(idExistente, conn);
                        } catch (SQLException e) {
                            registrarErro("Linha " + (r + 1) + ": Erro ao buscar id " + idExistente);
                        }
                    }

                    if (patrimonioEncontrado != null) {
                        preencherPatrimonio(patrimonioEncontrado, campos, conn);
                        try {
                            patrimonioDAO.update(patrimonioEncontrado, conn);
                            itensAtualizados++;
                        } catch (SQLException e) {
                            registrarErro("Linha " + (r + 1) + ": Erro ao atualizar " + numeroPatrimonio + " - " + e.getMessage());
                        }
                    } else {
                        Patrimonio novoPatrimonio = new Patrimonio();
                        preencherPatrimonio(novoPatrimonio, campos, conn);
                        try {
                            patrimonioDAO.insert(novoPatrimonio, conn);
                            itensInseridos++;
                            if (novoPatrimonio.getId() > 0) {
                                patrimoniosExistentes.put(numeroPatrimonio, novoPatrimonio.getId());
                            }
                        } catch (SQLException e) {
                            registrarErro("Linha " + (r + 1) + ": Erro ao inserir " + numeroPatrimonio + " - " + e.getMessage());
                        }
                    }

                } catch (Exception e) {
                    registrarErro("Linha " + (r + 1) + ": Erro - " + e.getMessage());
                }

                linhasProcessadas++;

                // COMMIT EM LOTE (A cada 100 linhas ou no final)
                if (linhasProcessadas % 100 == 0) {
                    conn.commit();
                    String mensagem = String.format("Processadas %d linhas - Inseridos: %d, Atualizados: %d, Erros: %d",
                            linhasProcessadas, itensInseridos, itensAtualizados, erros);
                    if (progressCallback != null) {
                        progressCallback.onProgress(linhasProcessadas, mensagem);
                    }
                }
            }

            conn.commit(); // Final commit
            workbook.close();
            
            System.out.println(">>> 🏁 Importação Concluída com Sucesso!");
            System.out.println(">>> 📊 RESUMO DE CRIAÇÃO:");
            System.out.println(">>>    - Setores novos: " + cacheSetores.size());
            System.out.println(">>>    - Responsáveis novos: " + cacheResponsaveis.size());
            System.out.println(">>>    - Salas novas: " + cacheSalas.size());
            System.out.println(">>>    - Patrimônios: " + linhasProcessadas + " processados (" + itensInseridos + " novos)");

            if (progressCallback != null) {
                progressCallback.onInfo("Processamento concluído com sucesso.");
            }

        } catch (Exception e) {
            registrarErro("Erro crítico na importação: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                ConnectionManager.closeConnection(conn);
            }
        }
        
        // Notificar Dashboard que dados foram alterados
        DashboardEvent event = DashboardEvent.builder(DashboardEventType.PATRIMONIO_ATUALIZADO)
                .source("ImportacaoExcel")
                .addMetadata("linhasProcessadas", linhasProcessadas)
                .addMetadata("itensAtualizados", itensAtualizados)
                .addMetadata("itensInseridos", itensInseridos)
                .build();
        DashboardEventBus.getInstance().publish(event);

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
        nomeNormalizadoParaConstante.put("PATRIMONIO", ImportacaoCSVConstants.COL_NUMERO);
        nomeNormalizadoParaConstante.put("STATUS", ImportacaoCSVConstants.COL_STATUS);
        nomeNormalizadoParaConstante.put("SITUACAO", ImportacaoCSVConstants.COL_STATUS);
        nomeNormalizadoParaConstante.put("ED", ImportacaoCSVConstants.COL_ED);
        nomeNormalizadoParaConstante.put("ELEMENTO DE DESPESA", ImportacaoCSVConstants.COL_ED);
        nomeNormalizadoParaConstante.put("DESCRICAO", ImportacaoCSVConstants.COL_DESCRICAO);
        nomeNormalizadoParaConstante.put("BEM", ImportacaoCSVConstants.COL_DESCRICAO);
        nomeNormalizadoParaConstante.put("ROTULOS", ImportacaoCSVConstants.COL_ROTULOS);
        nomeNormalizadoParaConstante.put("CARGA ATUAL", ImportacaoCSVConstants.COL_CARGA_ATUAL);
        
        // Setor
        nomeNormalizadoParaConstante.put("SETOR", ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("SETOR DO RESPONSAVEL", ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("NOME DO SETOR", ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("UNIDADE", ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("DEPARTAMENTO", ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
        
        // Sala
        nomeNormalizadoParaConstante.put("SALA", ImportacaoCSVConstants.COL_SALA);
        nomeNormalizadoParaConstante.put("LOCALIZACAO", ImportacaoCSVConstants.COL_SALA);
        nomeNormalizadoParaConstante.put("LOCAL", ImportacaoCSVConstants.COL_SALA);
        nomeNormalizadoParaConstante.put("AMBIENTE", ImportacaoCSVConstants.COL_SALA);
        
        nomeNormalizadoParaConstante.put("CAMPUS", ImportacaoCSVConstants.COL_CAMPUS);
        nomeNormalizadoParaConstante.put("CAMPUS DA CARGA", ImportacaoCSVConstants.COL_CAMPUS);
        nomeNormalizadoParaConstante.put("VALOR AQUISICAO", ImportacaoCSVConstants.COL_VALOR_AQUISICAO);
        nomeNormalizadoParaConstante.put("VALOR DEPRECIADO", ImportacaoCSVConstants.COL_VALOR_DEPRECIADO);
        nomeNormalizadoParaConstante.put("NUMERO NOTA FISCAL", ImportacaoCSVConstants.COL_NUMERO_NOTA_FISCAL);
        nomeNormalizadoParaConstante.put("NUMERO SERIE", ImportacaoCSVConstants.COL_NUMERO_SERIE);
        nomeNormalizadoParaConstante.put("MARCA", ImportacaoCSVConstants.COL_MARCA);
        nomeNormalizadoParaConstante.put("MODELO", ImportacaoCSVConstants.COL_MODELO);
        nomeNormalizadoParaConstante.put("FORNECEDOR", ImportacaoCSVConstants.COL_FORNECEDOR);
        nomeNormalizadoParaConstante.put("ESTADO CONSERVACAO", ImportacaoCSVConstants.COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("DATA DE AQUISICAO", ImportacaoCSVConstants.COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA DA CARGA", ImportacaoCSVConstants.COL_DATA_CARGA);

        logInfo("🔎 Mapeamento de Colunas Detectado:");
        for (int c = headerRow.getFirstCellNum(); c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell != null) {
                String valor = cell.getStringCellValue();
                String normalizado = normalizarCabecalho(valor);
                Integer constante = nomeNormalizadoParaConstante.get(normalizado);
                if (constante != null) {
                    mapping.put(constante, c);
                    logInfo("  ✅ Col " + c + ": '" + valor + "' -> Mapeado para " + normalizado);
                } else if (!valor.trim().isEmpty()) {
                    // Log hexadecimal para detectar caracteres ocultos
                    StringBuilder hex = new StringBuilder();
                    for (char ch : valor.toCharArray()) {
                        hex.append(String.format("\\u%04x", (int) ch));
                    }
                    logInfo("  ❓ Col " + c + ": '" + valor + "' [HEX: " + hex + "]");
                }
            }
        }
        
        return mapping;
    }

    private void logInfo(String mensagem) {
        System.out.println(">>> " + mensagem);
        if (progressCallback != null) {
            progressCallback.onInfo(mensagem);
        }
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
        String[] campos = new String[ImportacaoCSVConstants.COL_CATEGORIA + 1]; // Tamanho suficiente para todas as colunas
        campos[0] = ""; // índice 0 não utilizado

        for (Map.Entry<Integer, Integer> entry : mappingConstanteParaColunaExcel.entrySet()) {
            int constante = entry.getKey();
            int colExcel = entry.getValue();
            Cell cell = row.getCell(colExcel, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String valor = getCellString(cell);
            campos[constante] = valor;
        }

        // Garantir que campos obrigatórios existam (mesmo que vazios)
        for (int i = 0; i < campos.length; i++) {
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
                    // Usar formato completo para não perder informação de hora se houver
                    return DateFormatUtils.formatDateTimeFull(date);
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

    private String truncar(String valor, int maxLen) {
        if (valor == null) return "";
        if (valor.length() > maxLen) return valor.substring(0, maxLen);
        return valor;
    }

    /**
     * Preenche um objeto Patrimonio com dados dos campos
     */
    private void preencherPatrimonio(Patrimonio patrimonio, String[] campos, Connection conn) {
        try {
            // Dados básicos
            patrimonio.setNumero(truncar(valor(campos, ImportacaoCSVConstants.COL_NUMERO), 50));
            patrimonio.setStatus(truncar(valor(campos, ImportacaoCSVConstants.COL_STATUS), 50));
            patrimonio.setEd(truncar(valor(campos, ImportacaoCSVConstants.COL_ED), 20));
            patrimonio.setDescricao(valor(campos, ImportacaoCSVConstants.COL_DESCRICAO));
            patrimonio.setRotulos(truncar(valor(campos, ImportacaoCSVConstants.COL_ROTULOS), 500));

            // Valores monetários
            patrimonio.setValorAquisicao(parseValorMonetario(valor(campos, ImportacaoCSVConstants.COL_VALOR_AQUISICAO)));
            patrimonio.setValorDepreciado(parseValorMonetario(valor(campos, ImportacaoCSVConstants.COL_VALOR_DEPRECIADO)));

            // Outros campos
            patrimonio.setNumeroNotaFiscal(truncar(valor(campos, ImportacaoCSVConstants.COL_NUMERO_NOTA_FISCAL), 100));
            patrimonio.setNumeroSerie(truncar(valor(campos, ImportacaoCSVConstants.COL_NUMERO_SERIE), 100));
            patrimonio.setFornecedor(truncar(valor(campos, ImportacaoCSVConstants.COL_FORNECEDOR), 255));
            
            // Marca, Modelo e Categoria
            String marca = valor(campos, ImportacaoCSVConstants.COL_MARCA);
            if (!marca.isEmpty()) patrimonio.setMarca(truncar(marca, 255));
            String modelo = valor(campos, ImportacaoCSVConstants.COL_MODELO);
            if (!modelo.isEmpty()) patrimonio.setModelo(truncar(modelo, 255));
            String categoria = valor(campos, ImportacaoCSVConstants.COL_CATEGORIA);
            if (!categoria.isEmpty()) patrimonio.setCategoria(truncar(categoria, 50));

            // Normalizar estado de conservação para os valores corretos do sistema
            String estadoOriginal = valor(campos, ImportacaoCSVConstants.COL_ESTADO_CONSERVACAO);
            String estadoNormalizado = normalizarEstadoConservacao(estadoOriginal);
            patrimonio.setEstadoConservacao(truncar(estadoNormalizado, 50));

            // Datas
            // Datas (Parsing robusto para suportar formatos com e sem hora)
            String valDataEntrada = valor(campos, ImportacaoCSVConstants.COL_DATA_ENTRADA);
            String valDataCarga = valor(campos, ImportacaoCSVConstants.COL_DATA_CARGA);
            
            Date dataEntrada = null;
            if (!valDataEntrada.isEmpty() && !valDataEntrada.equals("-")) {
                dataEntrada = DateFormatUtils.parseAny(valDataEntrada);
            }
            
            Date dataCarga = null;
            if (!valDataCarga.isEmpty() && !valDataCarga.equals("-")) {
                dataCarga = DateFormatUtils.parseAny(valDataCarga);
            }
            
            // Regra de Negócio: Se não houver data de entrada, usar a data de carga do arquivo (Data de Aquisição)
            if (dataEntrada == null && dataCarga != null) {
                dataEntrada = dataCarga;
            }

            if (dataEntrada != null) {
                patrimonio.setDataEntrada(new java.sql.Date(dataEntrada.getTime()));
            }
            
            if (dataCarga != null) {
                patrimonio.setDataCarga(new java.sql.Timestamp(dataCarga.getTime()));
            }

            // Responsável
            String cargaAtual = valor(campos, ImportacaoCSVConstants.COL_CARGA_ATUAL);
            String nomeResponsavel = extrairNomeResponsavel(cargaAtual);
            String setorResponsavel = valor(campos, ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL);
            
            // Fallback: se o setor estiver vazio na coluna de setor, tentar extrair do parênteses da Carga Atual
            if (setorResponsavel.isEmpty() && !cargaAtual.isEmpty()) {
                setorResponsavel = extrairSetorDaCargaAtual(cargaAtual);
            }

            // LOG DE DIAGNÓSTICO (Apenas se o setor for identificado)
            if (!setorResponsavel.isEmpty()) {
                logInfo("📦 Patr: " + patrimonio.getNumero() + " | Resp: " + nomeResponsavel + " | Setor: " + setorResponsavel);
                // GARANTIR QUE O SETOR SEJA CRIADO, mesmo que não haja responsável
                obterOuCriarSetor(setorResponsavel, conn);
            }

            if (!nomeResponsavel.isEmpty()) {
                if (setorResponsavel.isEmpty()) {
                    logInfo("⚠️ Aviso: Responsável '" + nomeResponsavel + "' sem setor identificado.");
                }
                Responsavel responsavel = obterOuCriarResponsavel(nomeResponsavel, setorResponsavel, conn);
                if (responsavel != null) {
                    patrimonio.setIdResponsavel(responsavel.getId());
                }
            }

            // Sala
            String nomeSala = valor(campos, ImportacaoCSVConstants.COL_SALA);
            if (!nomeSala.isEmpty()) {
                Sala sala = obterOuCriarSala(nomeSala, setorResponsavel, conn);
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

    private String extrairSetorDaCargaAtual(String cargaAtual) {
        if (cargaAtual == null || !cargaAtual.contains("(") || !cargaAtual.contains(")")) {
            return "";
        }
        try {
            int start = cargaAtual.lastIndexOf('(') + 1;
            int end = cargaAtual.lastIndexOf(')');
            if (start < end) {
                return cargaAtual.substring(start, end).trim();
            }
        } catch (Exception e) {
            // Ignorar erros de parsing
        }
        return "";
    }

    private Responsavel obterOuCriarResponsavel(String nomeCompleto, String nomeSetor, Connection conn) {
        Responsavel responsavel = null;
        
        // 1. Tentar pegar do cache
        if (cacheResponsaveis.containsKey(nomeCompleto)) {
            responsavel = cacheResponsaveis.get(nomeCompleto);
        } else {
            // 2. Tentar buscar no banco
            try {
                List<Responsavel> responsaveisEncontrados = responsavelDAO.buscarPorNome(nomeCompleto, conn);
                if (!responsaveisEncontrados.isEmpty()) {
                    responsavel = responsaveisEncontrados.get(0);
                    cacheResponsaveis.put(nomeCompleto, responsavel);
                }
            } catch (SQLException e) {
                // Log error silently
            }
        }

        // 3. Se o responsável existe, verificar se o setor precisa ser atualizado
        if (responsavel != null) {
            if (nomeSetor != null && !nomeSetor.trim().isEmpty()) {
                Setor setor = obterOuCriarSetor(nomeSetor, conn);
                if (setor != null && responsavel.getIdSetor() != setor.getId()) {
                    logInfo("🔄 Atualizando setor do responsável '" + nomeCompleto + "' para '" + nomeSetor + "'");
                    responsavel.setIdSetor(setor.getId());
                    try {
                        responsavelDAO.update(responsavel, conn);
                    } catch (SQLException e) {
                        logInfo("⚠️ Erro ao atualizar setor do responsável: " + e.getMessage());
                    }
                }
            }
            return responsavel;
        }

        // 4. Se não existe, criar novo
        Responsavel novoResponsavel = new Responsavel();
        novoResponsavel.setNome(nomeCompleto);
        novoResponsavel.setAtivo(true);
        if (nomeSetor != null && !nomeSetor.isEmpty()) {
            Setor setor = obterOuCriarSetor(nomeSetor, conn);
            if (setor != null) {
                novoResponsavel.setIdSetor(setor.getId());
            }
        }
        try {
            responsavelDAO.insert(novoResponsavel, conn);
            if (novoResponsavel.getId() > 0) {
                cacheResponsaveis.put(nomeCompleto, novoResponsavel);
                return novoResponsavel;
            }
        } catch (SQLException e) {
            // Log error silently
        }
        return null;
    }

    private Sala obterOuCriarSala(String nomeSala, String nomeSetor, Connection conn) {
        if (cacheSalas.containsKey(nomeSala)) {
            return cacheSalas.get(nomeSala);
        }
        try {
            List<Sala> salasEncontradas = salaDAO.buscarPorFiltro(nomeSala, null, null, null, null, null, true, conn);
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
            Setor setor = obterOuCriarSetor(nomeSetor, conn);
            if (setor != null) {
                novaSala.setIdSetor(setor.getId());
            }
        }
        try {
            salaDAO.insert(novaSala, conn);
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
    private Setor obterOuCriarSetor(String nomeSetor, Connection conn) {
        if (nomeSetor == null || nomeSetor.trim().isEmpty()) {
            return null;
        }
        
        if (cacheSetores.containsKey(nomeSetor)) {
            return cacheSetores.get(nomeSetor);
        }
        try {
            // Buscar por nome exato primeiro
            Setor setorEncontrado = setorDAO.buscarPorNome(nomeSetor, conn);
            if (setorEncontrado != null) {
                System.out.println(">>> Setor encontrado: " + nomeSetor + " (ID: " + setorEncontrado.getId() + ")");
                cacheSetores.put(nomeSetor, setorEncontrado);
                return setorEncontrado;
            }
            
            // NOTA: Removemos a busca por termo similar para evitar reaproveitamento incorreto
            // de setores que apenas contém parte do nome. Na importação, queremos nomes exatos.
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERRO ao buscar setor '" + nomeSetor + "': " + e.getMessage());
        }
        
        // Criar novo setor
        System.out.println(">>> Tentando CRIAR NOVO setor: '" + nomeSetor + "'");
        Setor novoSetor = new Setor();
        novoSetor.setNome(nomeSetor);
        novoSetor.setAtivo(true);
        try {
            setorDAO.insert(novoSetor, conn);
            if (novoSetor.getId() > 0) {
                System.out.println(">>> ✅ Setor criado com SUCESSO: '" + nomeSetor + "' (ID: " + novoSetor.getId() + ")");
                cacheSetores.put(nomeSetor, novoSetor);
                return novoSetor;
            } else {
                System.err.println(">>> ❌ FALHA: Setor inserido mas ID não retornado para '" + nomeSetor + "'");
            }
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERRO CRÍTICO ao inserir setor '" + nomeSetor + "': " + e.getMessage());
            e.printStackTrace();
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
    
    /**
     * Normaliza o estado de conservação para os valores corretos do sistema
     * Converte variações do SUAP/Excel para os estados padronizados
     * 
     * Estados corretos: BOM, OCIOSO, ANTIECONÔMICO, RECUPERÁVEL, IRRECUPERÁVEL
     */
    private String normalizarEstadoConservacao(String estadoOriginal) {
        if (estadoOriginal == null || estadoOriginal.trim().isEmpty() || estadoOriginal.equals("-")) {
            return "BOM"; // Padrão quando não informado
        }
        
        String estado = estadoOriginal.trim().toUpperCase();
        
        // Remover acentos para facilitar comparação
        estado = estado.replace("Á", "A").replace("É", "E").replace("Í", "I")
                       .replace("Ó", "O").replace("Ú", "U").replace("Ã", "A")
                       .replace("Õ", "O").replace("Ç", "C");
        
        // Mapeamento de estados do SUAP para estados do sistema
        switch (estado) {
            // BOM
            case "BOM":
            case "OTIMO":
            case "ÓTIMO":
            case "EXCELENTE":
                return "BOM";
            
            // OCIOSO
            case "OCIOSO":
            case "NAO UTILIZADO":
            case "NÃO UTILIZADO":
            case "SEM USO":
                return "OCIOSO";
            
            // ANTIECONÔMICO
            case "ANTIECONOMICO":
            case "ANTIECONÔMICO":
            case "NAO ECONOMICO":
            case "NÃO ECONÔMICO":
                return "ANTIECONÔMICO";
            
            // RECUPERÁVEL
            case "RECUPERAVEL":
            case "RECUPERÁVEL":
            case "PODE SER RECUPERADO":
            case "CONSERTAVEL":
            case "CONSERTÁVEL":
            case "REGULAR":
                return "RECUPERÁVEL";
            
            // IRRECUPERÁVEL
            case "IRRECUPERAVEL":
            case "IRRECUPERÁVEL":
            case "NAO RECUPERAVEL":
            case "NÃO RECUPERÁVEL":
            case "INSERVIVEL":
            case "INSERVÍVEL":
            case "RUIM":
            case "PESSIMO":
            case "PÉSSIMO":
            case "SUCATA":
                return "IRRECUPERÁVEL";
            
            default:
                // Se não reconhecer, tentar mapear por palavras-chave
                if (estado.contains("BOM") || estado.contains("OTIMO")) {
                    return "BOM";
                } else if (estado.contains("OCIOSO")) {
                    return "OCIOSO";
                } else if (estado.contains("ANTIECONOMICO") || estado.contains("ECONOMICO")) {
                    return "ANTIECONÔMICO";
                } else if (estado.contains("RECUPERAVEL") || estado.contains("REGULAR") || estado.contains("CONSERT")) {
                    return "RECUPERÁVEL";
                } else if (estado.contains("IRRECUPERAVEL") || estado.contains("INSERVIVEL") || 
                          estado.contains("RUIM") || estado.contains("PESSIMO") || estado.contains("SUCATA")) {
                    return "IRRECUPERÁVEL";
                }
                
                // Se não conseguir mapear, usar BOM como padrão e registrar aviso
                if (progressCallback != null) {
                    progressCallback.onInfo("Estado não reconhecido: '" + estadoOriginal + "' - usando 'BOM' como padrão");
                }
                return "BOM";
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

    // ==================== Pré-carregamento de cache ====================

    /**
     * OTIMIZAÇÃO: Pré-carrega todos os números de patrimônio em memória.
     * Elimina N SELECTs (um por linha do Excel) substituindo por 1 único SELECT.
     */
    private Map<String, Integer> preCarregarPatrimonios() {
        try {
            return patrimonioDAO.buscarTodosNumerosComId();
        } catch (Exception e) {
            if (progressCallback != null) {
                progressCallback.onInfo("Aviso: não foi possível pré-carregar patrimônios. Usando busca individual.");
            }
            return new HashMap<>();
        }
    }

    /**
     * OTIMIZAÇÃO: Pré-carrega todos os responsáveis ativos no cache.
     */
    private void preCarregarResponsaveis() {
        try {
            List<Responsavel> todos = responsavelDAO.findAll();
            for (Responsavel r : todos) {
                if (r.getNome() != null && !r.getNome().isEmpty()) {
                    cacheResponsaveis.put(r.getNome(), r);
                }
            }
        } catch (Exception e) {
            if (progressCallback != null) {
                progressCallback.onInfo("Aviso: não foi possível pré-carregar responsáveis.");
            }
        }
    }

    /**
     * OTIMIZAÇÃO: Pré-carrega todas as salas ativas no cache.
     */
    private void preCarregarSalas() {
        try {
            List<Sala> todas = salaDAO.findAll();
            for (Sala s : todas) {
                String desc = s.getDescricao();
                if (desc != null && !desc.isEmpty()) {
                    cacheSalas.put(desc, s);
                }
            }
        } catch (Exception e) {
            if (progressCallback != null) {
                progressCallback.onInfo("Aviso: não foi possível pré-carregar salas.");
            }
        }
    }

    /**
     * OTIMIZAÇÃO: Pré-carrega todos os setores ativos no cache.
     */
    private void preCarregarSetores() {
        try {
            List<Setor> todos = setorDAO.findAll();
            for (Setor s : todos) {
                if (s.getNome() != null && !s.getNome().isEmpty()) {
                    cacheSetores.put(s.getNome(), s);
                }
            }
        } catch (Exception e) {
            if (progressCallback != null) {
                progressCallback.onInfo("Aviso: não foi possível pré-carregar setores.");
            }
        }
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
        public static final int COL_MARCA = 18;
        public static final int COL_MODELO = 19;
        public static final int COL_CATEGORIA = 20;
    }
}

