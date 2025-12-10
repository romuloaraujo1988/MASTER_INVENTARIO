package com.inventario.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.SalaDAO;
import com.inventario.dao.SetorDAO;
import com.inventario.model.Patrimonio;
import com.inventario.model.Responsavel;
import com.inventario.model.Sala;
import com.inventario.model.Setor;

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
    
    // Formatadores de data
    // Removido SimpleDateFormat - usando DateFormatUtils centralizado
    
    // Contadores para relatório
    private int linhasProcessadas = 0;
    private int itensInseridos = 0;
    private int itensAtualizados = 0;
    private int erros = 0;
    private final List<String> listaErros = new ArrayList<>();
    
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
        
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            boolean primeiraLinha = true;
            
            while ((linha = reader.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    if (progressCallback != null) {
                        progressCallback.onInfo("Cabeçalho do arquivo lido. Iniciando processamento dos dados...");
                    }
                    continue; // Pular cabeçalho
                }
                
                processarLinha(linha);
                linhasProcessadas++;
                
                // Log de progresso a cada 50 linhas para melhor responsividade
                if (linhasProcessadas % 50 == 0) {
                    String mensagem = String.format("Processadas %d linhas - Inseridos: %d, Atualizados: %d, Erros: %d", 
                        linhasProcessadas, itensInseridos, itensAtualizados, erros);
                    
                    if (progressCallback != null) {
                        progressCallback.onProgress(linhasProcessadas, mensagem);
                    } else {
                        System.out.println(mensagem);
                    }
                }
            }
            
            if (progressCallback != null) {
                progressCallback.onInfo("Processamento concluído. Gerando relatório...");
            }
            
        } catch (IOException e) {
            String erro = "Erro ao ler arquivo CSV: " + e.getMessage();
            listaErros.add(erro);
            erros++;
            
            if (progressCallback != null) {
                progressCallback.onError(erro);
            } else {
                System.err.println(erro);
            }
        }
        
        return gerarRelatorio();
    }
    
    /**
     * Processa uma linha do CSV
     */
    private void processarLinha(String linha) {
        try {
            String[] campos = parsearLinha(linha);
            
            if (campos.length < 18) {
                String erro = "Linha " + (linhasProcessadas + 1) + ": Número insuficiente de campos (" + campos.length + ")";
                listaErros.add(erro);
                erros++;
                if (progressCallback != null) {
                    progressCallback.onError(erro);
                }
                return;
            }
            
            String numeroPatrimonio = campos[COL_NUMERO].trim();
            
            if (numeroPatrimonio.isEmpty()) {
                String erro = "Linha " + (linhasProcessadas + 1) + ": Número do patrimônio vazio";
                listaErros.add(erro);
                erros++;
                if (progressCallback != null) {
                    progressCallback.onError(erro);
                }
                return;
            }
            
            // Verificar se o patrimônio já existe
            Patrimonio patrimonioEncontrado = patrimonioDAO.buscarPorNumero(numeroPatrimonio);
            List<Patrimonio> patrimoniosExistentes = new ArrayList<>();
            if (patrimonioEncontrado != null) {
                patrimoniosExistentes.add(patrimonioEncontrado);
            }
            
            if (!patrimoniosExistentes.isEmpty()) {
                // Usar o primeiro patrimônio encontrado (busca exata por número)
                Patrimonio patrimonioExistente = patrimoniosExistentes.get(0);
                // Atualizar patrimônio existente
                preencherPatrimonio(patrimonioExistente, campos);
                try {
                    patrimonioDAO.update(patrimonioExistente);
                    itensAtualizados++;
                } catch (SQLException e) {
                    String erro = "Linha " + (linhasProcessadas + 1) + ": Erro ao atualizar patrimônio " + numeroPatrimonio + " - " + e.getMessage();
                    listaErros.add(erro);
                    erros++;
                    if (progressCallback != null) {
                        progressCallback.onError(erro);
                    }
                }
            } else {
                // Inserir novo patrimônio
                Patrimonio novoPatrimonio = new Patrimonio();
                preencherPatrimonio(novoPatrimonio, campos);
                
                try {
                    patrimonioDAO.insert(novoPatrimonio);
                    itensInseridos++;
                } catch (SQLException e) {
                    String erro = "Linha " + (linhasProcessadas + 1) + ": Erro ao inserir patrimônio " + numeroPatrimonio + " - " + e.getMessage();
                    listaErros.add(erro);
                    erros++;
                    if (progressCallback != null) {
                        progressCallback.onError(erro);
                    }
                }
            }
            
        } catch (SQLException e) {
            String erro = "Linha " + (linhasProcessadas + 1) + ": Erro ao processar - " + e.getMessage();
            listaErros.add(erro);
            erros++;
            if (progressCallback != null) {
                progressCallback.onError(erro);
            } else {
                System.err.println(erro);
            }
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
        return campos.toArray(new String[0]);
    }
    
    /**
     * Preenche um objeto Patrimonio com dados da linha CSV
     */
    private void preencherPatrimonio(Patrimonio patrimonio, String[] campos) {
        try {
            // Dados básicos
            patrimonio.setNumero(campos[COL_NUMERO].trim());
            patrimonio.setStatus(campos[COL_STATUS].trim());
            patrimonio.setEd(campos[COL_ED].trim());
            patrimonio.setDescricao(campos[COL_DESCRICAO].trim());
            patrimonio.setRotulos(campos[COL_ROTULOS].trim());
            
            // Valores monetários
            patrimonio.setValorAquisicao(parseValorMonetario(campos[COL_VALOR_AQUISICAO]));
            patrimonio.setValorDepreciado(parseValorMonetario(campos[COL_VALOR_DEPRECIADO]));
            
            // Outros campos
            patrimonio.setNumeroNotaFiscal(campos[COL_NUMERO_NOTA_FISCAL].trim());
            patrimonio.setNumeroSerie(campos[COL_NUMERO_SERIE].trim());
            patrimonio.setFornecedor(campos[COL_FORNECEDOR].trim());
            
            // Normalizar estado de conservação para os valores corretos do sistema
            String estadoOriginal = campos[COL_ESTADO_CONSERVACAO].trim();
            String estadoNormalizado = normalizarEstadoConservacao(estadoOriginal);
            patrimonio.setEstadoConservacao(estadoNormalizado);
            
            // Datas
            Date dataEntrada = parseData(campos[COL_DATA_ENTRADA]);
            if (dataEntrada != null) {
                patrimonio.setDataEntrada(new java.sql.Date(dataEntrada.getTime()));
            }
            
            Date dataCarga = parseData(campos[COL_DATA_CARGA]);
            if (dataCarga != null) {
                patrimonio.setDataCarga(new java.sql.Timestamp(dataCarga.getTime()));
            }
            
            // Responsável
            String nomeResponsavel = extrairNomeResponsavel(campos[COL_CARGA_ATUAL]);
            String setorResponsavel = campos[COL_SETOR_RESPONSAVEL].trim();
            
            if (!nomeResponsavel.isEmpty()) {
                Responsavel responsavel = obterOuCriarResponsavel(nomeResponsavel, setorResponsavel);
                if (responsavel != null) {
                    patrimonio.setIdResponsavel(responsavel.getId());
                }
            }
            
            // Sala
            String nomeSala = campos[COL_SALA].trim();
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
    
    /**
     * Obtém ou cria um responsável
     */
    private Responsavel obterOuCriarResponsavel(String nomeCompleto, String nomeSetor) {
        // Verificar cache
        if (cacheResponsaveis.containsKey(nomeCompleto)) {
            return cacheResponsaveis.get(nomeCompleto);
        }
        
        // Buscar no banco
        List<Responsavel> responsaveisEncontrados;
        try {
            responsaveisEncontrados = responsavelDAO.buscarPorNome(nomeCompleto);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar responsável: " + e.getMessage());
            return null;
        }
        
        if (!responsaveisEncontrados.isEmpty()) {
            Responsavel responsavel = responsaveisEncontrados.get(0);
            cacheResponsaveis.put(nomeCompleto, responsavel);
            return responsavel;
        }
        
        // Criar novo responsável
        Responsavel novoResponsavel = new Responsavel();
        novoResponsavel.setNome(nomeCompleto);
        novoResponsavel.setAtivo(true);
        
        // Obter ou criar setor
        if (!nomeSetor.isEmpty()) {
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
            System.err.println("Erro ao inserir responsável: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Obtém ou cria uma sala
     */
    private Sala obterOuCriarSala(String nomeSala, String nomeSetor) {
        // Verificar cache
        if (cacheSalas.containsKey(nomeSala)) {
            return cacheSalas.get(nomeSala);
        }
        
        // Buscar no banco
        List<Sala> salasEncontradas;
        try {
            salasEncontradas = salaDAO.buscarPorFiltro(nomeSala, null, null, null, null, null, true);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar sala: " + e.getMessage());
            return null;
        }
        
        if (!salasEncontradas.isEmpty()) {
            Sala sala = salasEncontradas.get(0);
            cacheSalas.put(nomeSala, sala);
            return sala;
        }
        
        // Criar nova sala
        Sala novaSala = new Sala();
        novaSala.setDescricao(nomeSala);
        novaSala.setNumeroSala(extrairNumeroSala(nomeSala));
        novaSala.setAtivo(true);
        
        // Obter ou criar setor
        if (!nomeSetor.isEmpty()) {
            Setor setor = obterOuCriarSetor(nomeSetor);
            if (setor != null) {
                novaSala.setIdSetor(setor.getId());
            }
        }
        
        try {
            salaDAO.insert(novaSala);
            // O ID foi setado pelo DAO
            cacheSalas.put(nomeSala, novaSala);
            return novaSala;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir sala: " + e.getMessage());
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
     * Obtém ou cria um setor
     */
    private Setor obterOuCriarSetor(String nomeSetor) {
        // Verificar cache
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
