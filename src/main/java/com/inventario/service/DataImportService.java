package com.inventario.service;

import com.inventario.dao.*;
import com.inventario.model.Inventario;
import com.inventario.offline.OfflineDAO;
import com.inventario.offline.OfflineModeManager;
import com.inventario.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Serviço para importação de dados do servidor para modo offline
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class DataImportService {
    
    private static final Logger LOGGER = Logger.getLogger(DataImportService.class.getName());
    
    private final InventarioDAO inventarioDAO;
    private final OfflineDAO offlineDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final SalaDAO salaDAO;
    private final ResponsavelDAO responsavelDAO;
    private final UsuarioDAO usuarioDAO;
    
    public DataImportService() {
        this.inventarioDAO = new InventarioDAO();
        this.offlineDAO = new OfflineDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.salaDAO = new SalaDAO();
        this.responsavelDAO = new ResponsavelDAO();
        this.usuarioDAO = new UsuarioDAO();
    }
    
    /**
     * Importa todos os dados necessários para modo offline
     */
    public ImportResult importarTodosDados(ProgressListener listener) {
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  MÉTODO importarTodosDados() FOI CHAMADO COM SUCESSO!     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Thread atual: " + Thread.currentThread().getName());
        System.out.println("Listener: " + (listener != null ? "OK" : "NULL"));
        System.out.println();
        
        ImportResult result = new ImportResult();
        result.startTime = LocalDateTime.now();
        
        try {
            System.out.println("========================================");
            System.out.println("=== INICIANDO IMPORTAÇÃO DE DADOS ===");
            System.out.println("========================================");
            LOGGER.info("Iniciando importação de dados para modo offline");
            
            // Teste imediato do listener
            if (listener != null) {
                System.out.println(">>> Testando listener.onProgress()...");
                listener.onProgress("Teste de conexão com listener", 0);
                System.out.println(">>> ✅ Listener funcionando!");
            } else {
                System.err.println(">>> ❌ ERRO: Listener é NULL!");
                throw new IllegalArgumentException("ProgressListener não pode ser null");
            }
            
            // 0. Testar conexão com PostgreSQL
            System.out.println("\n>>> TESTE: Verificando conexão com PostgreSQL...");
            listener.onProgress("Verificando conexão com servidor...", 0);
            try {
                java.sql.Connection testConn = DatabaseConnection.getConnection();
                if (testConn != null && !testConn.isClosed()) {
                    System.out.println(">>> ✅ Conexão PostgreSQL OK!");
                    System.out.println(">>>    Database: " + testConn.getMetaData().getDatabaseProductName());
                    System.out.println(">>>    URL: " + testConn.getMetaData().getURL());
                } else {
                    throw new SQLException("Conexão com PostgreSQL está fechada ou nula");
                }
            } catch (Exception e) {
                System.err.println(">>> ❌ ERRO: Sem conexão com PostgreSQL!");
                System.err.println(">>>    Mensagem: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Não foi possível conectar ao servidor PostgreSQL. Verifique se o servidor está rodando.", e);
            }
            
            // 1. Inicializar banco SQLite (criar tabelas se não existirem)
            listener.onProgress("Inicializando banco de dados local...", 2);
            System.out.println("\n>>> Inicializando banco SQLite...");
            try {
                com.inventario.offline.SQLiteConnection sqliteConn = com.inventario.offline.SQLiteConnection.getInstance();
                sqliteConn.initializeDatabase();
                System.out.println(">>> ✅ Banco SQLite inicializado!");
                LOGGER.info("Banco de dados SQLite inicializado com sucesso");
            } catch (SQLException e) {
                System.err.println(">>> ❌ ERRO ao inicializar SQLite: " + e.getMessage());
                e.printStackTrace();
                LOGGER.log(Level.SEVERE, "Erro ao inicializar banco SQLite", e);
                throw new RuntimeException("Falha ao inicializar banco de dados local: " + e.getMessage(), e);
            }
            
            // 1. Patrimônios
            System.out.println("\n>>> ETAPA 1: Importando patrimônios...");
            listener.onProgress("Importando patrimônios...", 5);
            
            try {
                result.patrimonios = importarPatrimonios(listener);
                System.out.println(">>> ✅ Patrimônios importados: " + result.patrimonios);
                listener.onProgress("Patrimônios importados: " + result.patrimonios, 30);
            } catch (Exception e) {
                System.err.println(">>> ❌ ERRO ao importar patrimônios: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            // 2. Salas
            listener.onProgress("Importando salas...", 30);
            result.salas = importarSalas(listener);
            listener.onProgress("Salas importadas: " + result.salas, 50);
            
            // 3. Responsáveis
            listener.onProgress("Importando responsáveis...", 50);
            result.responsaveis = importarResponsaveis(listener);
            listener.onProgress("Responsáveis importados: " + result.responsaveis, 70);
            
            // 4. Inventário Ativo
            listener.onProgress("Importando inventário ativo...", 70);
            result.inventario = importarInventarioAtivo(listener);
            listener.onProgress("Inventário importado", 85);
            
            // 5. Usuários (para login offline)
            listener.onProgress("Importando credenciais de usuários...", 85);
            result.usuarios = importarUsuarios(listener);
            listener.onProgress("Credenciais salvas", 95);
            
            // 6. Marcar como sincronizado
            listener.onProgress("Salvando metadados...", 95);
            offlineDAO.atualizarMetadado("last_sync_timestamp", 
                LocalDateTime.now().toString());
            
            // Marcar dados como sincronizados
            OfflineModeManager.getInstance().marcarDadosSincronizados();
            
            listener.onProgress("Concluído!", 100);
            
            result.success = true;
            result.endTime = LocalDateTime.now();
            
            System.out.println("========================================");
            System.out.println("=== IMPORTAÇÃO CONCLUÍDA COM SUCESSO ===");
            System.out.println("Patrimônios: " + result.patrimonios);
            System.out.println("Salas: " + result.salas);
            System.out.println("Responsáveis: " + result.responsaveis);
            System.out.println("Usuários: " + result.usuarios);
            System.out.println("========================================");
            
            LOGGER.info(String.format("Importação concluída: %d patrimônios, %d salas, %d responsáveis, %d usuários",
                result.patrimonios, result.salas, result.responsaveis, result.usuarios));
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.endTime = LocalDateTime.now();
            
            LOGGER.log(Level.SEVERE, "Erro na importação de dados", e);
            listener.onError(e.getMessage());
        }
        
        return result;
    }

    
    /**
     * Importa patrimônios do servidor para SQLite
     */
    private int importarPatrimonios(ProgressListener listener) throws SQLException {
        System.out.println("\n========================================");
        System.out.println(">>> INICIANDO IMPORTAÇÃO DE PATRIMÔNIOS");
        System.out.println("========================================");
        LOGGER.info("Importando patrimônios do servidor PostgreSQL");
        
        try {
            System.out.println(">>> Chamando patrimonioDAO.findAll()...");
            System.out.println(">>> PatrimonioDAO instance: " + patrimonioDAO);
            System.out.println(">>> PatrimonioDAO class: " + patrimonioDAO.getClass().getName());
            
            // Buscar todos os patrimônios do PostgreSQL
            var patrimonios = patrimonioDAO.findAll("NUMERO");
            
            System.out.println(">>> ✅ patrimonioDAO.findAll() RETORNOU!");
            System.out.println(">>> Total de patrimônios encontrados: " + patrimonios.size());
            LOGGER.info("Total de patrimônios encontrados: " + patrimonios.size());
            
            if (patrimonios.isEmpty()) {
                LOGGER.warning("Nenhum patrimônio encontrado no servidor!");
                return 0;
            }
            
            // Limpar tabela antes de importar
            System.out.println(">>> Limpando tabela local_patrimonio...");
            try (Connection conn = com.inventario.offline.SQLiteConnection.getInstance().getConnection()) {
                try (var stmt = conn.createStatement()) {
                    int deleted = stmt.executeUpdate("DELETE FROM local_patrimonio");
                    System.out.println(">>> ✅ Tabela limpa - " + deleted + " registros removidos");
                    LOGGER.info("Tabela local_patrimonio limpa - " + deleted + " registros removidos");
                    
                    // Verificar se tabela existe e tem estrutura correta
                    try (var rs = stmt.executeQuery("PRAGMA table_info(local_patrimonio)")) {
                        System.out.println(">>> Estrutura da tabela local_patrimonio:");
                        while (rs.next()) {
                            System.out.println(">>>   - " + rs.getString("name") + " (" + rs.getString("type") + ")");
                        }
                    }
                }
            }
            
            // Importar patrimônios
            int imported = 0;
            int total = patrimonios.size();
            
            System.out.println(">>> Iniciando loop de importação de " + total + " patrimônios...");
            
            for (var patrimonio : patrimonios) {
                try {
                    // Converter patrimônio para Map e salvar no SQLite
                    var patrimonioMap = new java.util.HashMap<String, Object>();
                    patrimonioMap.put("id", patrimonio.getId());
                    patrimonioMap.put("numero", patrimonio.getNumero()); // Campo correto
                    patrimonioMap.put("descricao", patrimonio.getDescricao());
                    patrimonioMap.put("descricao_resumida", patrimonio.getDescricaoResumida());
                    patrimonioMap.put("marca", patrimonio.getMarca());
                    patrimonioMap.put("modelo", patrimonio.getModelo());
                    patrimonioMap.put("numero_serie", patrimonio.getNumeroSerie());
                    patrimonioMap.put("situacao", patrimonio.getSituacao());
                    patrimonioMap.put("valor", patrimonio.getValorAquisicao());
                    patrimonioMap.put("data_aquisicao", patrimonio.getDataEntrada());
                    patrimonioMap.put("id_setor", null); // Não temos setor no modelo atual
                    patrimonioMap.put("id_sala", patrimonio.getIdSala());
                    patrimonioMap.put("observacoes", patrimonio.getObservacoes());
                    
                    // DEBUG: Log do primeiro patrimônio
                    if (imported == 0) {
                        System.out.println(">>> DEBUG: Primeiro patrimônio a ser salvo:");
                        System.out.println(">>>   ID: " + patrimonio.getId());
                        System.out.println(">>>   Número: " + patrimonio.getNumero());
                        System.out.println(">>>   Descrição: " + patrimonio.getDescricao());
                    }
                    
                    offlineDAO.salvarPatrimonio(patrimonioMap);
                    imported++;
                    
                    // Atualizar progresso a cada 100 patrimônios ou no final
                    if (imported % 100 == 0 || imported == total) {
                        int progress = 5 + (int) ((imported * 25.0) / total);
                        listener.onProgress("Importando patrimônios: " + imported + "/" + total, progress);
                        System.out.println(">>> Progresso: " + imported + "/" + total + " patrimônios");
                    }
                } catch (Exception e) {
                    System.err.println(">>> ❌ ERRO ao importar patrimônio ID " + patrimonio.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    LOGGER.log(Level.WARNING, "Erro ao importar patrimônio ID " + patrimonio.getId(), e);
                    // Continuar com os próximos
                }
            }
            
            System.out.println(">>> ✅ Patrimônios importados com sucesso: " + imported);
            LOGGER.info("Patrimônios importados com sucesso: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar patrimônios", e);
            throw new SQLException("Erro ao importar patrimônios: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa salas do servidor para SQLite
     */
    private int importarSalas(ProgressListener listener) throws SQLException {
        System.out.println("\n========================================");
        System.out.println(">>> INICIANDO IMPORTAÇÃO DE SALAS");
        System.out.println("========================================");
        LOGGER.info("Importando salas do servidor PostgreSQL");
        
        try {
            // Buscar inventário ativo para associar às salas
            System.out.println(">>> Buscando inventário ativo...");
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            
            if (inventarioAtivo == null) {
                System.err.println(">>> ⚠️ AVISO: Nenhum inventário ativo encontrado!");
                System.err.println(">>> Salas serão importadas sem associação a inventário");
                LOGGER.warning("Nenhum inventário ativo encontrado - salas sem associação");
            } else {
                System.out.println(">>> ✅ Inventário ativo encontrado: " + inventarioAtivo.getNome() + " (ID=" + inventarioAtivo.getId() + ")");
            }
            
            System.out.println(">>> Chamando salaDAO.listarTodasSalas()...");
            System.out.println(">>> SalaDAO instance: " + salaDAO);
            
            // Buscar TODAS as salas do PostgreSQL (ativas e inativas)
            var salas = salaDAO.listarTodasSalas();
            
            System.out.println(">>> ✅ salaDAO.listarTodasSalas() RETORNOU!");
            System.out.println(">>> Total de salas encontradas: " + salas.size());
            System.out.println(">>> (incluindo ativas e inativas)");
            LOGGER.info("Total de salas encontradas: " + salas.size());
            
            if (salas.isEmpty()) {
                System.err.println(">>> ⚠️ AVISO: Nenhuma sala encontrada no servidor!");
                LOGGER.warning("Nenhuma sala encontrada no servidor!");
                return 0;
            }
            
            // Limpar TODAS as tabelas de salas antes de importar
            System.out.println(">>> Limpando tabelas de salas...");
            try (Connection conn = offlineDAO.getConnection()) {
                try (var stmt = conn.createStatement()) {
                    // Limpar local_sala
                    int deleted1 = stmt.executeUpdate("DELETE FROM local_sala");
                    System.out.println(">>> ✅ Tabela local_sala limpa - " + deleted1 + " registros removidos");
                    
                    // Limpar SALA (compatibilidade)
                    int deleted2 = stmt.executeUpdate("DELETE FROM SALA");
                    System.out.println(">>> ✅ Tabela SALA limpa - " + deleted2 + " registros removidos");
                    
                    LOGGER.info("Tabelas de salas limpas");
                }
            }
            
            // Importar salas
            int imported = 0;
            int total = salas.size();
            
            System.out.println(">>> Iniciando loop de importação de " + total + " salas...");
            System.out.println(">>> DEBUG: Lista de salas tem " + salas.size() + " elementos");
            System.out.println(">>> DEBUG: Classe da lista: " + salas.getClass().getName());
            
            // DEBUG: Mostrar IDs de todas as salas
            System.out.println(">>> DEBUG: IDs das salas na lista:");
            for (int i = 0; i < Math.min(20, salas.size()); i++) {
                System.out.println(">>>   [" + i + "] ID=" + salas.get(i).getIdSala() + 
                                 ", Número=" + salas.get(i).getNumeroSala() +
                                 ", Ativo=" + salas.get(i).getAtivo());
            }
            if (salas.size() > 20) {
                System.out.println(">>>   ... e mais " + (salas.size() - 20) + " salas");
            }
            
            for (var sala : salas) {
                try {
                    // DEBUG: Log da primeira sala
                    if (imported == 0) {
                        System.out.println(">>> DEBUG: Primeira sala a ser salva:");
                        System.out.println(">>>   ID: " + sala.getIdSala());
                        System.out.println(">>>   Número: " + sala.getNumeroSala());
                        System.out.println(">>>   Descrição: " + sala.getDescricao());
                        System.out.println(">>>   Bloco: " + sala.getBloco());
                        System.out.println(">>>   Tipo: " + sala.getTipoSala());
                        System.out.println(">>>   Ativa: " + sala.getAtivo());
                    }
                    
                    // 1. Salvar em local_sala
                    var salaMap = new java.util.HashMap<String, Object>();
                    salaMap.put("id", sala.getIdSala());
                    salaMap.put("nome", sala.getNumeroSala()); // Nome da sala
                    salaMap.put("descricao", sala.getDescricao());
                    salaMap.put("bloco", sala.getBloco());
                    salaMap.put("andar", sala.getAndar());
                    salaMap.put("capacidade", sala.getCapacidade());
                    salaMap.put("tipo", sala.getTipoSala()); // Tipo da sala
                    salaMap.put("ativa", sala.getAtivo() != null ? sala.getAtivo() : true);
                    
                    offlineDAO.salvarSala(salaMap);
                    
                    // 2. Salvar também em SALA (compatibilidade)
                    try (Connection conn = com.inventario.offline.SQLiteConnection.getInstance().getConnection()) {
                        // Salvar em SALA
                        String sqlSala = """
                            INSERT OR REPLACE INTO SALA 
                            (ID_SALA, NUMERO_SALA, NOME_SALA, ANDAR, BLOCO, CAPACIDADE, TIPO_SALA, ATIVA)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                        
                        try (var stmt = conn.prepareStatement(sqlSala)) {
                            stmt.setInt(1, sala.getIdSala());
                            stmt.setString(2, sala.getNumeroSala());
                            stmt.setString(3, sala.getDescricao());
                            stmt.setString(4, sala.getAndar() != null ? String.valueOf(sala.getAndar()) : null);
                            stmt.setString(5, sala.getBloco() != null ? String.valueOf(sala.getBloco()) : null);
                            stmt.setObject(6, sala.getCapacidade());
                            stmt.setString(7, sala.getTipoSala());
                            stmt.setBoolean(8, sala.getAtivo() != null ? sala.getAtivo() : true);
                            stmt.executeUpdate();
                        }
                        
                        if (imported == 1) {
                            System.out.println(">>> ✅ Primeira sala salva em SALA");
                        }
                        
                    } catch (SQLException e) {
                        System.err.println(">>> ⚠️ Erro ao salvar em tabela SALA: " + e.getMessage());
                        e.printStackTrace();
                        // Não lançar exceção, apenas logar
                    }
                    
                    imported++;
                    
                    // Log de progresso
                    if (imported % 10 == 0 || imported == total) {
                        System.out.println(">>> Progresso: " + imported + "/" + total + " salas");
                    }
                    
                } catch (Exception e) {
                    System.err.println(">>> ❌ ERRO ao importar sala ID " + sala.getIdSala() + ":");
                    System.err.println(">>>   Número: " + sala.getNumeroSala());
                    System.err.println(">>>   Descrição: " + sala.getDescricao());
                    System.err.println(">>>   Bloco: " + sala.getBloco());
                    System.err.println(">>>   Andar: " + sala.getAndar());
                    System.err.println(">>>   Tipo: " + sala.getTipoSala());
                    System.err.println(">>>   Ativa: " + sala.getAtivo());
                    System.err.println(">>>   Erro: " + e.getMessage());
                    System.err.println(">>>   Tipo do erro: " + e.getClass().getName());
                    e.printStackTrace();
                    LOGGER.log(Level.WARNING, "Erro ao importar sala ID " + sala.getIdSala(), e);
                    // NÃO continuar silenciosamente - registrar erro mas continuar loop
                }
            }
            
            System.out.println(">>> ✅ Salas importadas com sucesso: " + imported);
            System.out.println(">>> DEBUG: Total esperado: " + total);
            System.out.println(">>> DEBUG: Total importado: " + imported);
            System.out.println(">>> DEBUG: Diferença: " + (total - imported));
            
            if (imported < total) {
                System.err.println(">>> ⚠️ AVISO: Nem todas as salas foram importadas!");
                System.err.println(">>>    Esperado: " + total);
                System.err.println(">>>    Importado: " + imported);
                System.err.println(">>>    Perdidas: " + (total - imported));
            }
            
            listener.onProgress("Salas importadas: " + imported, 50);
            
            LOGGER.info("Salas importadas com sucesso: " + imported);
            return imported;
            
        } catch (Exception e) {
            System.err.println(">>> ❌ ERRO ao importar salas: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao importar salas", e);
            throw new SQLException("Erro ao importar salas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa responsáveis do servidor para SQLite
     */
    private int importarResponsaveis(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando responsáveis do servidor PostgreSQL");
        
        try {
            // Buscar todos os responsáveis do PostgreSQL
            var responsaveis = responsavelDAO.findAll("NOME");
            
            LOGGER.info("Total de responsáveis encontrados: " + responsaveis.size());
            
            if (responsaveis.isEmpty()) {
                LOGGER.warning("Nenhum responsável encontrado no servidor!");
                return 0;
            }
            
            // Limpar tabela antes de importar
            System.out.println(">>> Limpando tabela local_responsavel...");
            try (Connection conn = offlineDAO.getConnection()) {
                try (var stmt = conn.createStatement()) {
                    int deleted = stmt.executeUpdate("DELETE FROM local_responsavel");
                    System.out.println(">>> ✅ Tabela local_responsavel limpa - " + deleted + " registros removidos");
                    LOGGER.info("Tabela local_responsavel limpa");
                }
            }
            
            // Importar responsáveis
            int imported = 0;
            for (var responsavel : responsaveis) {
                try {
                    var responsavelMap = new java.util.HashMap<String, Object>();
                    responsavelMap.put("id", responsavel.getId());
                    responsavelMap.put("nome", responsavel.getNome());
                    responsavelMap.put("cpf", responsavel.getCpf());
                    responsavelMap.put("matricula", null); // Campo não existe no modelo
                    responsavelMap.put("email", responsavel.getEmail());
                    responsavelMap.put("telefone", responsavel.getTelefone());
                    responsavelMap.put("cargo", responsavel.getCargo());
                    responsavelMap.put("setor", responsavel.getNomeSetor()); // Nome do setor
                    responsavelMap.put("ativo", responsavel.getAtivo() != null ? responsavel.getAtivo() : true);
                    
                    offlineDAO.salvarResponsavel(responsavelMap);
                    imported++;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erro ao importar responsável ID " + responsavel.getId(), e);
                }
            }
            
            listener.onProgress("Responsáveis importados: " + imported, 75);
            
            LOGGER.info("Responsáveis importados com sucesso: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar responsáveis", e);
            throw new SQLException("Erro ao importar responsáveis: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa inventário ativo do servidor para SQLite
     * Salva em AMBAS as tabelas (local_inventario e TABELA_INVENTARIO) para compatibilidade
     */
    private int importarInventarioAtivo(ProgressListener listener) throws SQLException {
        System.out.println("\n========================================");
        System.out.println(">>> INICIANDO IMPORTAÇÃO DE INVENTÁRIO");
        System.out.println("========================================");
        LOGGER.info("Importando inventário ativo do servidor PostgreSQL");
        
        try {
            // Buscar inventário ativo do PostgreSQL
            System.out.println(">>> Chamando inventarioDAO.buscarInventarioAtivo()...");
            Inventario inventario = inventarioDAO.buscarInventarioAtivo();
            
            if (inventario == null) {
                System.out.println(">>> ⚠️ Nenhum inventário ativo encontrado!");
                LOGGER.warning("Nenhum inventário ativo encontrado no servidor");
                listener.onProgress("⚠️ Nenhum inventário ativo encontrado", 85);
                return 0;
            }
            
            System.out.println(">>> ✅ Inventário encontrado: " + inventario.getNome());
            System.out.println(">>>    ID: " + inventario.getId());
            System.out.println(">>>    Status: " + inventario.getStatusInventario());
            
            // Limpar ambas as tabelas antes de importar
            System.out.println(">>> Limpando tabelas de inventário...");
            try (Connection conn = com.inventario.offline.SQLiteConnection.getInstance().getConnection()) {
                try (var stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM local_inventario");
                    stmt.executeUpdate("DELETE FROM TABELA_INVENTARIO");
                    System.out.println(">>> ✅ Tabelas limpas");
                }
            }
            
            // 1. Salvar em local_inventario (tabela com prefixo local_)
            System.out.println(">>> Salvando em local_inventario...");
            var inventarioMap = new java.util.HashMap<String, Object>();
            inventarioMap.put("id", inventario.getId());
            inventarioMap.put("nome", inventario.getNome());
            inventarioMap.put("descricao", inventario.getObservacao());
            inventarioMap.put("data_inicio", inventario.getDataInicio());
            inventarioMap.put("data_fim", inventario.getDataFim());
            inventarioMap.put("status", inventario.getStatusInventario());
            
            offlineDAO.salvarInventario(inventarioMap);
            System.out.println(">>> ✅ Salvo em local_inventario");
            
            // 2. Salvar também em TABELA_INVENTARIO (compatibilidade com InventarioDAO)
            System.out.println(">>> Salvando em TABELA_INVENTARIO...");
            try (Connection conn = com.inventario.offline.SQLiteConnection.getInstance().getConnection()) {
                String sql = """
                    INSERT INTO TABELA_INVENTARIO 
                    (ID, NOME, ANO, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO, 
                     RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (var stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, inventario.getId());
                    stmt.setString(2, inventario.getNome());
                    
                    // Extrair ano da data de início
                    Integer ano = null;
                    if (inventario.getDataInicio() != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(inventario.getDataInicio());
                        ano = cal.get(java.util.Calendar.YEAR);
                    }
                    stmt.setObject(3, ano);
                    
                    if (inventario.getDataInicio() != null) {
                        stmt.setDate(4, new java.sql.Date(inventario.getDataInicio().getTime()));
                    } else {
                        stmt.setNull(4, java.sql.Types.DATE);
                    }
                    
                    if (inventario.getDataFim() != null) {
                        stmt.setDate(5, new java.sql.Date(inventario.getDataFim().getTime()));
                    } else {
                        stmt.setNull(5, java.sql.Types.DATE);
                    }
                    
                    stmt.setString(6, inventario.getStatusInventario());
                    stmt.setString(7, inventario.getResponsavelInventario());
                    stmt.setBigDecimal(8, inventario.getPercentualConclusao());
                    
                    stmt.executeUpdate();
                    System.out.println(">>> ✅ Salvo em TABELA_INVENTARIO");
                }
            }
            
            System.out.println(">>> ✅ Inventário importado em AMBAS as tabelas!");
            LOGGER.info("Inventário ativo importado: " + inventario.getNome());
            listener.onProgress("Inventário importado: " + inventario.getNome(), 85);
            
            return 1;
            
        } catch (Exception e) {
            System.err.println(">>> ❌ ERRO ao importar inventário: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao importar inventário", e);
            throw new SQLException("Erro ao importar inventário: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importa usuários do servidor para SQLite (para login offline)
     */
    private int importarUsuarios(ProgressListener listener) throws SQLException {
        LOGGER.info("Importando usuários do servidor PostgreSQL");
        
        try {
            // Buscar todos os usuários do PostgreSQL
            var usuarios = usuarioDAO.findAll("LOGIN");
            
            // Filtrar apenas usuários ativos
            var usuariosAtivos = usuarios.stream()
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .toList();
            
            LOGGER.info("Total de usuários ativos encontrados: " + usuariosAtivos.size());
            
            if (usuariosAtivos.isEmpty()) {
                LOGGER.warning("Nenhum usuário ativo encontrado no servidor!");
                return 0;
            }
            
            // Limpar tabela antes de importar
            System.out.println(">>> Limpando tabela local_usuario...");
            try (Connection conn = offlineDAO.getConnection()) {
                try (var stmt = conn.createStatement()) {
                    int deleted = stmt.executeUpdate("DELETE FROM local_usuario");
                    System.out.println(">>> ✅ Tabela local_usuario limpa - " + deleted + " registros removidos");
                    LOGGER.info("Tabela local_usuario limpa");
                }
            }
            
            // Importar usuários
            int imported = 0;
            for (var usuario : usuariosAtivos) {
                try {
                    var usuarioMap = new java.util.HashMap<String, Object>();
                    usuarioMap.put("id", usuario.getId());
                    usuarioMap.put("login", usuario.getLogin());
                    usuarioMap.put("senha_hash", usuario.getSenhaHash()); // Hash da senha
                    usuarioMap.put("nome_completo", usuario.getNomeCompleto()); // ✅ CORRIGIDO: nome_completo
                    usuarioMap.put("email", usuario.getEmail());
                    usuarioMap.put("perfil", usuario.getPerfil() != null ? usuario.getPerfil().name() : "COLETOR");
                    usuarioMap.put("ativo", usuario.getAtivo() != null ? usuario.getAtivo() : true);
                    
                    // DEBUG: Log do primeiro usuário
                    if (imported == 0) {
                        System.out.println(">>> DEBUG: Primeiro usuário a ser salvo:");
                        System.out.println(">>>   ID: " + usuario.getId());
                        System.out.println(">>>   Login: " + usuario.getLogin());
                        System.out.println(">>>   Nome: " + usuario.getNomeCompleto());
                    }
                    
                    offlineDAO.salvarUsuario(usuarioMap);
                    imported++;
                } catch (Exception e) {
                    System.err.println(">>> ❌ ERRO ao importar usuário ID " + usuario.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    LOGGER.log(Level.WARNING, "Erro ao importar usuário ID " + usuario.getId(), e);
                }
            }
            
            listener.onProgress("Credenciais de " + imported + " usuários salvas", 90);
            
            LOGGER.info("Usuários importados com sucesso: " + imported);
            return imported;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar usuários", e);
            throw new SQLException("Erro ao importar usuários: " + e.getMessage(), e);
        }
    }
    
    // ==================== CLASSES AUXILIARES ====================
    
    /**
     * Resultado da importação
     */
    public static class ImportResult {
        public boolean success;
        public String errorMessage;
        public int patrimonios;
        public int salas;
        public int responsaveis;
        public int inventario;
        public int usuarios;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        
        public long getDurationMillis() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
    }
    
    /**
     * Interface para listener de progresso
     */
    public interface ProgressListener {
        void onProgress(String message, int progress);
        void onError(String error);
    }
}
