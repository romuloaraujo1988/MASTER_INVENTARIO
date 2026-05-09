package com.inventario.sihcp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.inventario.sihcp.util.ConnectionManager;

/**
 * Classe responsável pela importação de dados CSV do SUAP
 * Processa arquivos CSV exportados do sistema SUAP e importa os dados para o banco
 */
public class ImportacaoCSV {
    
    /**
     * Interface para callback de progresso da importação
     */
    public interface ProgressCallback {
        void onProgress(int linhasProcessadas, String mensagem);
        void onError(String mensagem);
        void onInfo(String mensagem);
    }
    
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
    private ProgressCallback progressCallback;
    
    // Índices das colunas no CSV (baseado no cabeçalho)
    private static final int COL_NUMERO = 1;
    private static final int COL_STATUS = 2;
    private static final int COL_ED = 3;
    private static final int COL_DESCRICAO = 4;
    private static final int COL_ROTULOS = 5;
    private static final int COL_CARGA_ATUAL = 6;
    private static final int COL_SETOR_RESPONSAVEL = 7;
    private static final int COL_CAMPUS = 8;
    private static final int COL_VALOR_AQUISICAO = 9;
    private static final int COL_VALOR_DEPRECIADO = 10;
    private static final int COL_NUMERO_NOTA_FISCAL = 11;
    private static final int COL_NUMERO_SERIE = 12;
    private static final int COL_DATA_ENTRADA = 13;
    private static final int COL_DATA_CARGA = 14;
    private static final int COL_FORNECEDOR = 15;
    private static final int COL_SALA = 16;
    private static final int COL_ESTADO_CONSERVACAO = 17;
    private static final int COL_MARCA = 18;
    private static final int COL_MODELO = 19;
    private static final int COL_CATEGORIA = 20;
    
    private static final int MAX_COLUMNS = 21;
    
    // Formatadores de data
    // Removido SimpleDateFormat - usando DateFormatUtils centralizado
    
    // Contadores para relatório
    private int linhasProcessadas = 0;
    private int itensInseridos = 0;
    private int itensAtualizados = 0;
    private int erros = 0;
    private final List<String> listaErros = new ArrayList<>();
    
    // OTIMIZAÇÃO: Cache de números de patrimônio existentes (numero → id)
    private Map<String, Integer> patrimoniosExistentes = new HashMap<>();
    
    // Mapeamento dinâmico de colunas
    private Map<Integer, Integer> mappingConstanteParaColunaCSV = new HashMap<>();
    
    /**
     * Construtor
     */
    public ImportacaoCSV() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.responsavelDAO = new ResponsavelDAO();
        this.salaDAO = new SalaDAO();
        this.setorDAO = new SetorDAO();
    }
    
    /**
     * Importa patrimônios do arquivo CSV
     */
    /**
     * Importa patrimônios do arquivo CSV sem callback de progresso
     */
    public RelatorioImportacao importarPatrimonios(String caminhoArquivo) {
        return importarPatrimonios(caminhoArquivo, null);
    }
    
    /**
     * Importa patrimônios do arquivo CSV com callback de progresso
     */
    public RelatorioImportacao importarPatrimonios(String caminhoArquivo, ProgressCallback callback) {
        this.progressCallback = callback;
        resetarContadores();
        
        if (progressCallback != null) {
            progressCallback.onInfo("Iniciando leitura do arquivo CSV...");
        }

        // OTIMIZAÇÃO: Pré-carregar dados em memória para evitar N queries por linha
        patrimoniosExistentes = preCarregarPatrimonios();
        preCarregarResponsaveis();
        preCarregarSalas();
        preCarregarSetores();

        if (progressCallback != null) {
            progressCallback.onInfo("Cache carregado: " + patrimoniosExistentes.size() + " patrimônios, "
                    + cacheResponsaveis.size() + " responsáveis, "
                    + cacheSalas.size() + " salas, "
                    + cacheSetores.size() + " setores em memória.");
        }
        
        try (Connection conn = ConnectionManager.getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {

            conn.setAutoCommit(false); // Transação única para todo o arquivo

            String linha;
            boolean primeiraLinha = true;

            while ((linha = reader.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    mapearCabecalho(linha);
                    if (progressCallback != null) {
                        progressCallback.onInfo("Cabeçalho mapeado. Iniciando processamento...");
                    }
                    continue;
                }

                processarLinha(linha, conn);
                linhasProcessadas++;

                // Commit em lote a cada 100 linhas
                if (linhasProcessadas % 100 == 0) {
                    conn.commit();
                    String mensagem = String.format("Processadas %d linhas - Inseridos: %d, Atualizados: %d, Erros: %d",
                            linhasProcessadas, itensInseridos, itensAtualizados, erros);
                    if (progressCallback != null) {
                        progressCallback.onProgress(linhasProcessadas, mensagem);
                    } else {
                        System.out.println(mensagem);
                    }
                }
            }

            conn.commit(); // Commit final

            if (progressCallback != null) {
                progressCallback.onInfo("Processamento concluído. Gerando relatório...");
            }

        } catch (IOException e) {
            String erro = "Erro ao ler arquivo CSV: " + e.getMessage();
            listaErros.add(erro);
            erros++;
            if (progressCallback != null) progressCallback.onError(erro);
            else System.err.println(erro);
        } catch (SQLException e) {
            String erro = "Erro de banco de dados na importação CSV: " + e.getMessage();
            listaErros.add(erro);
            erros++;
            if (progressCallback != null) progressCallback.onError(erro);
            else System.err.println(erro);
        }
        
        // Notificar Dashboard que dados foram alterados
        DashboardEvent event = DashboardEvent.builder(DashboardEventType.PATRIMONIO_ATUALIZADO)
                .source("ImportacaoCSV")
                .addMetadata("linhasProcessadas", linhasProcessadas)
                .addMetadata("itensAtualizados", itensAtualizados)
                .addMetadata("itensInseridos", itensInseridos)
                .build();
        DashboardEventBus.getInstance().publish(event);
        
        return gerarRelatorio();
    }
    
    /**
     * Processa uma linha do CSV usando a conexão transacional fornecida
     */
    private void processarLinha(String linha, Connection conn) {
        try {
            String[] campos = parsearLinha(linha);

            String[] camposExtraidos = extrairCamposPorMapeamento(campos);
            String numeroPatrimonio = camposExtraidos[COL_NUMERO].trim();

            if (numeroPatrimonio.isEmpty()) {
                String erro = "Linha " + (linhasProcessadas + 1) + ": Número do patrimônio vazio";
                listaErros.add(erro);
                erros++;
                if (progressCallback != null) progressCallback.onError(erro);
                return;
            }

            // Verificar se o patrimônio já existe usando cache em memória
            Integer idExistente = patrimoniosExistentes.get(numeroPatrimonio);

            if (idExistente != null) {
                // Atualizar patrimônio existente
                Patrimonio patrimonioExistente = null;
                try {
                    patrimonioExistente = patrimonioDAO.findById(idExistente, conn);
                } catch (SQLException e) {
                    String erro = "Linha " + (linhasProcessadas + 1) + ": Erro ao buscar id " + idExistente + " - " + e.getMessage();
                    listaErros.add(erro);
                    erros++;
                    if (progressCallback != null) progressCallback.onError(erro);
                    return;
                }

                if (patrimonioExistente != null) {
                    preencherPatrimonio(patrimonioExistente, camposExtraidos, conn);
                    try {
                        patrimonioDAO.update(patrimonioExistente, conn);
                        itensAtualizados++;
                    } catch (SQLException e) {
                        String erro = "Linha " + (linhasProcessadas + 1) + ": Erro ao atualizar '" + numeroPatrimonio + "' - " + e.getMessage();
                        listaErros.add(erro);
                        erros++;
                        if (progressCallback != null) progressCallback.onError(erro);
                    }
                }
            } else {
                // Inserir novo patrimônio
                Patrimonio novoPatrimonio = new Patrimonio();
                preencherPatrimonio(novoPatrimonio, camposExtraidos, conn);
                try {
                    patrimonioDAO.insert(novoPatrimonio, conn);
                    itensInseridos++;
                    if (novoPatrimonio.getId() > 0) {
                        patrimoniosExistentes.put(numeroPatrimonio, novoPatrimonio.getId());
                    }
                } catch (SQLException e) {
                    String erro = "Linha " + (linhasProcessadas + 1) + ": Erro ao inserir '" + numeroPatrimonio + "' - " + e.getMessage();
                    listaErros.add(erro);
                    erros++;
                    if (progressCallback != null) progressCallback.onError(erro);
                }
            }

        } catch (Exception e) {
            String erro = "Linha " + (linhasProcessadas + 1) + ": Erro inesperado - " + e.getMessage();
            listaErros.add(erro);
            erros++;
            if (progressCallback != null) progressCallback.onError(erro);
            else System.err.println(erro);
        }
    }
    
    /**
     * Parseia uma linha CSV considerando campos entre aspas
     */
    private String[] parsearLinha(String linha) {
        // Detectar delimitador predominante fora de aspas: vírgula ou ponto e vírgula
        boolean dentroAspas = false;
        int countVirgula = 0;
        int countPontoVirgula = 0;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                dentroAspas = !dentroAspas;
            } else if (!dentroAspas) {
                if (c == ',') countVirgula++;
                else if (c == ';') countPontoVirgula++;
            }
        }

        char delimitador = countPontoVirgula > countVirgula ? ';' : ',';

        // Parsear linha usando o delimitador escolhido, respeitando aspas
        List<String> campos = new ArrayList<>();
        StringBuilder campoAtual = new StringBuilder();
        dentroAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                // Alternar estado de aspas
                dentroAspas = !dentroAspas;
            } else if (c == delimitador && !dentroAspas) {
                campos.add(campoAtual.toString());
                campoAtual = new StringBuilder();
            } else {
                campoAtual.append(c);
            }
        }
        campos.add(campoAtual.toString());
        return campos.toArray(String[]::new);
    }

    /**
     * Mapeia cabeçalhos do CSV para as constantes de coluna
     */
    private void mapearCabecalho(String linhaHeader) {
        String[] headers = parsearLinha(linhaHeader);
        mappingConstanteParaColunaCSV.clear();

        Map<String, Integer> nomeNormalizadoParaConstante = new HashMap<>();
        // Inserir o mesmo mapa de sinônimos do ImportacaoExcel
        nomeNormalizadoParaConstante.put("NUMERO", COL_NUMERO);
        nomeNormalizadoParaConstante.put("PATRIMONIO", COL_NUMERO);
        nomeNormalizadoParaConstante.put("STATUS", COL_STATUS);
        nomeNormalizadoParaConstante.put("SITUACAO", COL_STATUS);
        nomeNormalizadoParaConstante.put("ED", COL_ED);
        nomeNormalizadoParaConstante.put("ELEMENTO DE DESPESA", COL_ED);
        nomeNormalizadoParaConstante.put("DESCRICAO", COL_DESCRICAO);
        nomeNormalizadoParaConstante.put("BEM", COL_DESCRICAO);
        nomeNormalizadoParaConstante.put("ROTULOS", COL_ROTULOS);
        nomeNormalizadoParaConstante.put("CARGA ATUAL", COL_CARGA_ATUAL);
        nomeNormalizadoParaConstante.put("SETOR", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("NOME DO SETOR", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("SIGLA DO SETOR", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("SETOR RESPONSAVEL", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("SETOR DO RESPONSAVEL", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("UNIDADE", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("DEPARTAMENTO", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("DEP", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("CENTRO DE CUSTO", COL_SETOR_RESPONSAVEL);
        nomeNormalizadoParaConstante.put("CAMPUS", COL_CAMPUS);
        nomeNormalizadoParaConstante.put("VALOR AQUISICAO", COL_VALOR_AQUISICAO);
        nomeNormalizadoParaConstante.put("VALOR DE AQUISICAO", COL_VALOR_AQUISICAO);
        nomeNormalizadoParaConstante.put("VALOR", COL_VALOR_AQUISICAO);
        
        // Novos campos: Marca, Modelo, Categoria
        nomeNormalizadoParaConstante.put("MARCA", COL_MARCA);
        nomeNormalizadoParaConstante.put("BRAND", COL_MARCA);
        nomeNormalizadoParaConstante.put("FABRICANTE", COL_MARCA);
        
        nomeNormalizadoParaConstante.put("MODELO", COL_MODELO);
        nomeNormalizadoParaConstante.put("MODEL", COL_MODELO);
        
        nomeNormalizadoParaConstante.put("CATEGORIA", COL_CATEGORIA);
        nomeNormalizadoParaConstante.put("CATEGORY", COL_CATEGORIA);
        nomeNormalizadoParaConstante.put("TIPO BEM", COL_CATEGORIA);
        nomeNormalizadoParaConstante.put("TIPO DO BEM", COL_CATEGORIA);
        
        // Sala e Estado
        nomeNormalizadoParaConstante.put("SALA", COL_SALA);
        nomeNormalizadoParaConstante.put("LOCAL", COL_SALA);
        nomeNormalizadoParaConstante.put("AMBIENTE", COL_SALA);
        nomeNormalizadoParaConstante.put("LOCALIZACAO", COL_SALA);
        
        nomeNormalizadoParaConstante.put("ESTADO CONSERVACAO", COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("ESTADO", COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("CONSERVACAO", COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("SITUACAO FISICA", COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("VALOR COMPRA", COL_VALOR_AQUISICAO);
        nomeNormalizadoParaConstante.put("VALOR DE COMPRA", COL_VALOR_AQUISICAO);
        nomeNormalizadoParaConstante.put("VALOR DEPRECIADO", COL_VALOR_DEPRECIADO);
        nomeNormalizadoParaConstante.put("NUMERO NOTA FISCAL", COL_NUMERO_NOTA_FISCAL);
        nomeNormalizadoParaConstante.put("NOTA FISCAL", COL_NUMERO_NOTA_FISCAL);
        nomeNormalizadoParaConstante.put("NF", COL_NUMERO_NOTA_FISCAL);
        nomeNormalizadoParaConstante.put("NUMERO SERIE", COL_NUMERO_SERIE);
        nomeNormalizadoParaConstante.put("NUMERO DE SERIE", COL_NUMERO_SERIE);
        nomeNormalizadoParaConstante.put("N SERIE", COL_NUMERO_SERIE);
        nomeNormalizadoParaConstante.put("SERIE", COL_NUMERO_SERIE);
        nomeNormalizadoParaConstante.put("DATA ENTRADA", COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA DE ENTRADA", COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA AQUISICAO", COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA DE AQUISICAO", COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("AQUISICAO", COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA DE AQUISICAO DO BEM", COL_DATA_ENTRADA);
        nomeNormalizadoParaConstante.put("DATA CARGA", COL_DATA_CARGA);
        nomeNormalizadoParaConstante.put("DATA DA CARGA", COL_DATA_CARGA);
        nomeNormalizadoParaConstante.put("FORNECEDOR", COL_FORNECEDOR);
        nomeNormalizadoParaConstante.put("SALA", COL_SALA);
        nomeNormalizadoParaConstante.put("ESTADO CONSERVACAO", COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("ESTADO", COL_ESTADO_CONSERVACAO);
        nomeNormalizadoParaConstante.put("CONSERVACAO", COL_ESTADO_CONSERVACAO);

        for (int i = 0; i < headers.length; i++) {
            String normalizado = normalizarCabecalho(headers[i]);
            if (nomeNormalizadoParaConstante.containsKey(normalizado)) {
                mappingConstanteParaColunaCSV.put(nomeNormalizadoParaConstante.get(normalizado), i);
            }
        }
    }

    private String normalizarCabecalho(String s) {
        if (s == null) return "";
        String semAcento = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        semAcento = semAcento.replaceAll("[^A-Za-z0-9 ]", " ");
        semAcento = semAcento.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
        return semAcento;
    }

    private String[] extrairCamposPorMapeamento(String[] camposOriginais) {
        String[] camposFinal = new String[MAX_COLUMNS];
        Arrays.fill(camposFinal, "");
        
        for (Map.Entry<Integer, Integer> entry : mappingConstanteParaColunaCSV.entrySet()) {
            int constante = entry.getKey();
            int idxOrigem = entry.getValue();
            if (idxOrigem >= 0 && idxOrigem < camposOriginais.length) {
                camposFinal[constante] = camposOriginais[idxOrigem];
            }
        }
        return camposFinal;
    }
    
    private String truncar(String valor, int maxLen) {
        if (valor == null) return "";
        if (valor.length() > maxLen) return valor.substring(0, maxLen);
        return valor;
    }
    
    /**
     * Preenche um objeto Patrimonio com dados da linha CSV
     */
    private void preencherPatrimonio(Patrimonio patrimonio, String[] campos, Connection conn) {
        try {
            // Dados básicos truncados para os tamanhos seguros do DB
            patrimonio.setNumero(truncar(campos[COL_NUMERO].trim(), 50));
            patrimonio.setStatus(truncar(campos[COL_STATUS].trim(), 50));
            patrimonio.setEd(truncar(campos[COL_ED].trim(), 20));
            patrimonio.setDescricao(campos[COL_DESCRICAO].trim()); // TEXT
            patrimonio.setRotulos(truncar(campos[COL_ROTULOS].trim(), 500));
            
            // Valores monetários
            patrimonio.setValorAquisicao(parseValorMonetario(campos[COL_VALOR_AQUISICAO]));
            patrimonio.setValorDepreciado(parseValorMonetario(campos[COL_VALOR_DEPRECIADO]));
            
            // Outros campos
            patrimonio.setNumeroNotaFiscal(truncar(campos[COL_NUMERO_NOTA_FISCAL].trim(), 100));
            patrimonio.setNumeroSerie(truncar(campos[COL_NUMERO_SERIE].trim(), 100));
            patrimonio.setFornecedor(truncar(campos[COL_FORNECEDOR].trim(), 255));
            
            // Normalizar estado de conservação para os valores corretos do sistema
            String estadoOriginal = campos[COL_ESTADO_CONSERVACAO].trim();
            String estadoNormalizado = normalizarEstadoConservacao(estadoOriginal);
            patrimonio.setEstadoConservacao(truncar(estadoNormalizado, 50));
            
            // Marca, Modelo e Categoria
            if (!campos[COL_MARCA].isEmpty()) {
                patrimonio.setMarca(truncar(campos[COL_MARCA].trim(), 255));
            }
            if (!campos[COL_MODELO].isEmpty()) {
                patrimonio.setModelo(truncar(campos[COL_MODELO].trim(), 255));
            }
            if (!campos[COL_CATEGORIA].isEmpty()) {
                patrimonio.setCategoria(truncar(campos[COL_CATEGORIA].trim(), 50));
            }
            
            // Datas (Parsing robusto para suportar formatos com e sem hora)
            String valDataEntrada = campos[COL_DATA_ENTRADA].trim();
            String valDataCarga = campos[COL_DATA_CARGA].trim();
            
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
            String valCargaAtual = campos[COL_CARGA_ATUAL];
            String nomeResponsavel = extrairNomeResponsavel(valCargaAtual);
            String setorResponsavel = campos[COL_SETOR_RESPONSAVEL].trim();
            
            // Fallback: se o setor estiver vazio na coluna de setor, tentar extrair do parênteses da Carga Atual
            if (setorResponsavel.isEmpty() && !valCargaAtual.isEmpty()) {
                setorResponsavel = extrairSetorDaCargaAtual(valCargaAtual);
            }
            
            if (!nomeResponsavel.isEmpty()) {
                Responsavel responsavel = obterOuCriarResponsavel(nomeResponsavel, setorResponsavel, conn);
                if (responsavel != null) {
                    patrimonio.setIdResponsavel(responsavel.getId());
                }
            }
            
            // Sala
            String nomeSala = campos[COL_SALA].trim();
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
    
    /**
     * Extrai o nome do responsável do campo "CARGA ATUAL"
     * Formato: "Nome do Responsavel(SIGLA SETOR)"
     */
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
            // Ignorar
        }
        return "";
    }
    
    /**
     * Obtém ou cria um responsável usando a conexão transacional
     */
    private Responsavel obterOuCriarResponsavel(String nomeCompleto, String nomeSetor, Connection conn) {
        if (cacheResponsaveis.containsKey(nomeCompleto)) {
            return cacheResponsaveis.get(nomeCompleto);
        }
        
        List<Responsavel> responsaveisEncontrados;
        try {
            responsaveisEncontrados = responsavelDAO.buscarPorNome(nomeCompleto, conn);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar responsável: " + e.getMessage());
            return null;
        }
        
        if (!responsaveisEncontrados.isEmpty()) {
            Responsavel responsavel = responsaveisEncontrados.get(0);
            cacheResponsaveis.put(nomeCompleto, responsavel);
            return responsavel;
        }
        
        Responsavel novoResponsavel = new Responsavel();
        novoResponsavel.setNome(nomeCompleto);
        novoResponsavel.setAtivo(true);
        
        if (!nomeSetor.isEmpty()) {
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
            System.err.println("Erro ao inserir responsável '" + nomeCompleto + "': " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Obtém ou cria uma sala usando a conexão transacional
     */
    private Sala obterOuCriarSala(String nomeSala, String nomeSetor, Connection conn) {
        if (cacheSalas.containsKey(nomeSala)) {
            return cacheSalas.get(nomeSala);
        }
        
        List<Sala> salasEncontradas;
        try {
            salasEncontradas = salaDAO.buscarPorFiltro(nomeSala, null, null, null, null, null, true, conn);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar sala: " + e.getMessage());
            return null;
        }
        
        if (!salasEncontradas.isEmpty()) {
            Sala sala = salasEncontradas.get(0);
            cacheSalas.put(nomeSala, sala);
            return sala;
        }
        
        Sala novaSala = new Sala();
        novaSala.setDescricao(nomeSala);
        novaSala.setNumeroSala(extrairNumeroSala(nomeSala));
        novaSala.setAtivo(true);
        
        if (!nomeSetor.isEmpty()) {
            Setor setor = obterOuCriarSetor(nomeSetor, conn);
            if (setor != null) {
                novaSala.setIdSetor(setor.getId());
            }
        }
        
        try {
            salaDAO.insert(novaSala, conn);
            cacheSalas.put(nomeSala, novaSala);
            return novaSala;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir sala '" + nomeSala + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrai o número da sala do nome
     */
    private String extrairNumeroSala(String nomeSala) {
        // Tentar extrair número do início do nome
        String[] partes = nomeSala.split("\\s+");
        for (String parte : partes) {
            if (parte.matches("\\d+")) {
                return parte;
            }
        }
        return nomeSala; // Retorna o nome completo se não encontrar número
    }
    
    /**
     * Obtém ou cria um setor usando a conexão transacional
     */
    private Setor obterOuCriarSetor(String nomeSetor, Connection conn) {
        if (cacheSetores.containsKey(nomeSetor)) {
            return cacheSetores.get(nomeSetor);
        }
        
        try {
            Setor setorEncontrado = setorDAO.buscarPorNome(nomeSetor, conn);
            if (setorEncontrado != null) {
                cacheSetores.put(nomeSetor, setorEncontrado);
                return setorEncontrado;
            }
            
            // NOTA: Removemos a busca por termo similar para evitar reaproveitamento incorreto
            // de setores que apenas contém parte do nome. Na importação, queremos nomes exatos.
        } catch (SQLException e) {
            System.err.println("Erro ao buscar setor '" + nomeSetor + "': " + e.getMessage());
        }
        
        Setor novoSetor = new Setor();
        novoSetor.setNome(truncar(nomeSetor, 255));
        novoSetor.setAtivo(true);
        
        try {
            setorDAO.insert(novoSetor, conn);
            if (novoSetor.getId() > 0) {
                cacheSetores.put(nomeSetor, novoSetor);
                return novoSetor;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir setor '" + nomeSetor + "': " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Converte string para BigDecimal (valor monetário)
     */
    private BigDecimal parseValorMonetario(String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.equals("-")) {
            return BigDecimal.ZERO;
        }
        
        try {
            // Remove espaços e substitui vírgula por ponto
            String valorLimpo = valor.trim().replace(",", ".");
            return new BigDecimal(valorLimpo);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Converte string para Date
     */
    private Date parseData(String data) {
        return DateFormatUtils.parseDate(data);
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
            case "BOM", "OTIMO", "ÓTIMO", "EXCELENTE" -> {
                return "BOM";
            }
            case "OCIOSO", "NAO UTILIZADO", "NÃO UTILIZADO", "SEM USO" -> {
                return "OCIOSO";
            }
            case "ANTIECONOMICO", "ANTIECONÔMICO", "NAO ECONOMICO", "NÃO ECONÔMICO" -> {
                return "ANTIECONÔMICO";
            }
            case "RECUPERAVEL", "RECUPERÁVEL", "PODE SER RECUPERADO", "CONSERTAVEL", "CONSERTÁVEL", "REGULAR" -> {
                return "RECUPERÁVEL";
            }
            case "IRRECUPERAVEL", "IRRECUPERÁVEL", "NAO RECUPERAVEL", "NÃO RECUPERÁVEL", "INSERVIVEL", "INSERVÍVEL", "RUIM", "PESSIMO", "PÉSSIMO", "SUCATA" -> {
                return "IRRECUPERÁVEL";
            }
            default -> {
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
        // BOM
        // OCIOSO
        // ANTIECONÔMICO
        // RECUPERÁVEL
        // IRRECUPERÁVEL
            }
    
    /**
     * Reseta os contadores
     */
    private void resetarContadores() {
        linhasProcessadas = 0;
        itensInseridos = 0;
        itensAtualizados = 0;
        erros = 0;
        listaErros.clear();
        cacheResponsaveis.clear();
        cacheSalas.clear();
        cacheSetores.clear();
        patrimoniosExistentes.clear();
    }

    // ==================== Pré-carregamento de cache ====================

    /**
     * OTIMIZAÇÃO: Pré-carrega todos os números de patrimônio em memória.
     * Elimina N SELECTs (um por linha do CSV) substituindo por 1 único SELECT.
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
    
    /**
     * Gera relatório da importação
     */
    private RelatorioImportacao gerarRelatorio() {
        return new RelatorioImportacao(
            linhasProcessadas,
            itensInseridos,
            itensAtualizados,
            erros,
            new ArrayList<>(listaErros)
        );
    }
    
    /**
     * Classe para encapsular o relatório de importação
     */
    public static class RelatorioImportacao {
        private final int linhasProcessadas;
        private final int itensInseridos;
        private final int itensAtualizados;
        private final int erros;
        private final List<String> listaErros;
        private double tempoExecucao;
        
        public RelatorioImportacao(int linhasProcessadas, int itensInseridos, 
                                 int itensAtualizados, int erros, List<String> listaErros) {
            this.linhasProcessadas = linhasProcessadas;
            this.itensInseridos = itensInseridos;
            this.itensAtualizados = itensAtualizados;
            this.erros = erros;
            this.listaErros = listaErros;
            this.tempoExecucao = 0.0;
        }
        
        // Getters
        public int getLinhasProcessadas() { return linhasProcessadas; }
        public int getItensInseridos() { return itensInseridos; }
        public int getItensAtualizados() { return itensAtualizados; }
        public int getErros() { return erros; }
        public List<String> getListaErros() { return listaErros; }
        public double getTempoExecucao() { return tempoExecucao; }
        
        public void setTempoExecucao(double tempoExecucao) {
            this.tempoExecucao = tempoExecucao;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RELATÓRIO DE IMPORTAÇÃO ===").append("\n");
            sb.append("Linhas processadas: ").append(linhasProcessadas).append("\n");
            sb.append("Itens inseridos: ").append(itensInseridos).append("\n");
            sb.append("Itens atualizados: ").append(itensAtualizados).append("\n");
            sb.append("Erros: ").append(erros).append("\n");
            
            if (!listaErros.isEmpty()) {
                sb.append("\n=== DETALHES DOS ERROS ===").append("\n");
                for (String erro : listaErros) {
                    sb.append("- ").append(erro).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}
