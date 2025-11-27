package com.inventario.offline;

import com.inventario.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.util.DateFormatUtils;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gerenciador de sincronização de dados entre SQLite local e PostgreSQL remoto
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class DataSynchronizer {
    
    private static final Logger LOGGER = Logger.getLogger(DataSynchronizer.class.getName());
    // Formatação de datas centralizada em DateFormatUtils
    
    private final OfflineDAO offlineDAO;
    private final ConnectivityManager connectivityManager;
    private final ScheduledExecutorService scheduler;
    
    private boolean autoSyncEnabled = true;
    private int syncIntervalMinutes = 5;
    private ScheduledFuture<?> autoSyncTask;
    
    // Estratégias de resolução de conflitos
    public enum ConflictResolution {
        TIMESTAMP_WINS,    // Registro mais recente vence
        LOCAL_WINS,        // Dados locais têm prioridade
        REMOTE_WINS,       // Dados remotos têm prioridade
        MANUAL_RESOLUTION  // Requer intervenção manual
    }
    
    private ConflictResolution conflictStrategy = ConflictResolution.TIMESTAMP_WINS;
    
    public DataSynchronizer() {
        this.offlineDAO = new OfflineDAO();
        this.connectivityManager = ConnectivityManager.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // Registra listener para mudanças de conectividade
        connectivityManager.addListener(new ConnectivityListener() {
            @Override
            public void onConnectionEstablished() {
                LOGGER.info("Conexão estabelecida - iniciando sincronização automática");
                if (autoSyncEnabled) {
                    scheduleAutoSync();
                }
            }
            
            @Override
            public void onConnectionLost() {
                LOGGER.info("Conexão perdida - pausando sincronização automática");
                stopAutoSync();
            }
        });
    }
    
    // ==================== CONTROLE DE SINCRONIZAÇÃO ====================
    
    /**
     * Inicia a sincronização automática
     */
    public void startAutoSync() {
        autoSyncEnabled = true;
        if (connectivityManager.isOnline()) {
            scheduleAutoSync();
        }
        LOGGER.info("Sincronização automática habilitada");
    }
    
    /**
     * Para a sincronização automática
     */
    public void stopAutoSync() {
        autoSyncEnabled = false;
        if (autoSyncTask != null && !autoSyncTask.isCancelled()) {
            autoSyncTask.cancel(false);
        }
        LOGGER.info("Sincronização automática desabilitada");
    }
    
    /**
     * Agenda a sincronização automática
     */
    private void scheduleAutoSync() {
        if (autoSyncTask != null && !autoSyncTask.isCancelled()) {
            autoSyncTask.cancel(false);
        }
        
        autoSyncTask = scheduler.scheduleAtFixedRate(
            this::executarSincronizacaoCompleta,
            0, // Delay inicial
            syncIntervalMinutes,
            TimeUnit.MINUTES
        );
        
        LOGGER.info("Sincronização automática agendada para cada " + syncIntervalMinutes + " minutos");
    }
    
    /**
     * Executa sincronização manual
     * @return Resultado da sincronização
     */
    public SyncResult executarSincronizacaoManual() {
        LOGGER.info("Iniciando sincronização manual");
        return executarSincronizacao(true);
    }
    
    /**
     * Executa sincronização completa (automática)
     */
    private void executarSincronizacaoCompleta() {
        try {
            executarSincronizacao(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro na sincronização automática", e);
        }
    }
    
    /**
     * Executa o processo de sincronização
     * @param manual Se é sincronização manual
     * @return Resultado da sincronização
     */
    private SyncResult executarSincronizacao(boolean manual) {
        SyncResult result = new SyncResult();
        result.startTime = LocalDateTime.now();
        result.isManual = manual;
        
        if (!connectivityManager.isOnline()) {
            result.success = false;
            result.errorMessage = "Sem conexão com o servidor";
            LOGGER.warning("Tentativa de sincronização sem conexão");
            return result;
        }
        
        try {
            LOGGER.info("Iniciando processo de sincronização");
            
            // 1. Sincroniza dados locais para o servidor
            result.uploadedRecords = sincronizarDadosLocais();
            
            // 2. Baixa dados atualizados do servidor
            result.downloadedRecords = baixarDadosServidor();
            
            // 3. Atualiza timestamp da última sincronização (formato ISO para compatibilidade)
            offlineDAO.atualizarMetadado("last_sync_timestamp", 
                DateFormatUtils.formatForApi(LocalDateTime.now()));
            
            result.success = true;
            result.endTime = LocalDateTime.now();
            
            LOGGER.info(String.format("Sincronização concluída: %d enviados, %d recebidos", 
                result.uploadedRecords, result.downloadedRecords));
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.endTime = LocalDateTime.now();
            
            // Log como WARNING ao invés de SEVERE para não alarmar quando banco não está configurado
            LOGGER.log(Level.WARNING, "Erro durante sincronização (banco pode não estar configurado): " + e.getMessage());
        }
        
        return result;
    }
    
    // ==================== SINCRONIZAÇÃO DE UPLOAD ====================
    
    /**
     * Sincroniza dados locais para o servidor
     * @return Número de registros enviados
     * @throws SQLException
     */
    private int sincronizarDadosLocais() throws SQLException {
        int totalEnviados = 0;
        
        // Obtém operações pendentes em lotes
        List<Map<String, Object>> operacoesPendentes;
        int batchSize = 50;
        
        do {
            operacoesPendentes = offlineDAO.listarOperacoesPendentes(batchSize);
            
            for (Map<String, Object> operacao : operacoesPendentes) {
                try {
                    if (processarOperacaoUpload(operacao)) {
                        totalEnviados++;
                        offlineDAO.marcarComoSincronizado((Integer) operacao.get("id"));
                    }
                } catch (Exception e) {
                    String erro = "Erro ao processar operação: " + e.getMessage();
                    offlineDAO.registrarErroSync((Integer) operacao.get("id"), erro);
                    LOGGER.log(Level.WARNING, erro, e);
                }
            }
            
        } while (operacoesPendentes.size() == batchSize);
        
        return totalEnviados;
    }
    
    /**
     * Processa uma operação de upload
     * @param operacao Dados da operação
     * @return true se processada com sucesso
     * @throws SQLException
     */
    private boolean processarOperacaoUpload(Map<String, Object> operacao) throws SQLException {
        String tabela = (String) operacao.get("table_name");
        String tipoOperacao = (String) operacao.get("operation");
        int recordId = (Integer) operacao.get("record_id");
        String dataJson = (String) operacao.get("data_json");
        
        Map<String, Object> dados = jsonToMap(dataJson);
        
        // ✅ CORRIGIDO: Usar conexão PostgreSQL direta para upload (não SQLite)
        // ✅ MELHORADO: Verificar conectividade antes de tentar conexão
        if (!connectivityManager.isOnline()) {
            LOGGER.warning("Servidor offline - adiando upload para " + tabela);
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getPostgreSQLConnection();
            
            switch (tabela) {
                case "local_patrimonio":
                    return processarPatrimonioUpload(conn, tipoOperacao, recordId, dados);
                    
                case "local_coleta":
                    return processarColetaUpload(conn, tipoOperacao, recordId, dados);
                    
                case "local_inventario":
                    return processarInventarioUpload(conn, tipoOperacao, recordId, dados);
                    
                default:
                    LOGGER.warning("Tabela não suportada para upload: " + tabela);
                    return false;
            }
            
        } catch (SQLException e) {
            // Verificar se é erro de conexão (servidor indisponível)
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("connection") || msg.contains("timeout") || 
                msg.contains("refused") || msg.contains("não usual") ||
                msg.contains("network") || msg.contains("host")) {
                // Erro de conexão - marcar como offline e não propagar exceção
                LOGGER.warning("Servidor PostgreSQL indisponível - operação será tentada novamente: " + e.getMessage());
                connectivityManager.markOffline();
                return false;
            }
            // Outros erros SQL devem ser propagados
            LOGGER.log(Level.SEVERE, "Erro ao processar upload para " + tabela, e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Ignorar erro ao fechar conexão
                }
            }
        }
    }
    
    /**
     * Processa upload de patrimônio
     * @param conn Conexão com PostgreSQL
     * @param operacao Tipo de operação
     * @param recordId ID do registro
     * @param dados Dados do patrimônio
     * @return true se processado com sucesso
     * @throws SQLException
     */
    private boolean processarPatrimonioUpload(Connection conn, String operacao, int recordId, Map<String, Object> dados) throws SQLException {
        
        switch (operacao) {
            case "INSERT":
                String insertSql = """
                    INSERT INTO patrimonio 
                    (numero, descricao, descricao_resumida, marca, modelo, numero_serie, 
                     situacao, valor, data_aquisicao, id_setor, id_sala, observacoes)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    setPatrimonioParameters(stmt, dados);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Atualiza ID local com ID remoto se necessário
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int remoteId = rs.getInt(1);
                                atualizarIdRemoto("local_patrimonio", recordId, remoteId);
                            }
                        }
                        return true;
                    }
                }
                break;
                
            case "UPDATE":
                String updateSql = """
                    UPDATE patrimonio SET 
                    numero = ?, descricao = ?, descricao_resumida = ?, marca = ?, modelo = ?, 
                    numero_serie = ?, situacao = ?, valor = ?, data_aquisicao = ?, 
                    id_setor = ?, id_sala = ?, observacoes = ?
                    WHERE id = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    setPatrimonioParameters(stmt, dados);
                    stmt.setInt(13, recordId);
                    
                    return stmt.executeUpdate() > 0;
                }
                
            case "DELETE":
                String deleteSql = "DELETE FROM patrimonio WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                    stmt.setInt(1, recordId);
                    return stmt.executeUpdate() > 0;
                }
        }
        
        return false;
    }
    
    /**
     * Processa upload de coleta
     * @param conn Conexão com PostgreSQL
     * @param operacao Tipo de operação
     * @param recordId ID do registro
     * @param dados Dados da coleta
     * @return true se processado com sucesso
     * @throws SQLException
     */
    private boolean processarColetaUpload(Connection conn, String operacao, int recordId, Map<String, Object> dados) throws SQLException {
        
        switch (operacao) {
            case "INSERT":
                // ✅ CORRIGIDO: INSERT com todos os campos obrigatórios incluindo ID_PARTICIPANTE_INVENTARIO
                String insertSql = """
                    INSERT INTO TABELA_COLETA 
                    (ID_PATRIMONIO, ID_INVENTARIO, ID_COLETOR, ID_PARTICIPANTE_INVENTARIO,
                     DATA_COLETA, LOCALIZACAO_ATUAL, LOCALIZACAO_ENCONTRADA, ESTADO_ENCONTRADO, 
                     OBSERVACAO_COLETA, FOTO_PATRIMONIO)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    // ✅ DEBUG: Log dos dados antes de inserir
                    LOGGER.info("Dados para INSERT coleta: " + dados);
                    
                    // Obter IDs com validação
                    Object idPatrimonio = dados.get("id_patrimonio");
                    Object idInventario = dados.get("id_inventario");
                    Object idParticipante = dados.get("id_participante");
                    
                    // Validar IDs obrigatórios
                    if (idPatrimonio == null || (idPatrimonio instanceof Number && ((Number)idPatrimonio).intValue() == 0)) {
                        LOGGER.warning("ID_PATRIMONIO inválido: " + idPatrimonio + " - pulando registro");
                        return false;
                    }
                    
                    if (idInventario == null || (idInventario instanceof Number && ((Number)idInventario).intValue() == 0)) {
                        LOGGER.warning("ID_INVENTARIO inválido: " + idInventario + " - pulando registro");
                        return false;
                    }
                    
                    if (idParticipante == null || (idParticipante instanceof Number && ((Number)idParticipante).intValue() == 0)) {
                        LOGGER.warning("ID_PARTICIPANTE inválido: " + idParticipante + " - pulando registro");
                        return false;
                    }
                    
                    // ✅ NOVO: Verificar se coleta já existe (evitar duplicatas)
                    String checkDuplicateSql = """
                        SELECT ID FROM TABELA_COLETA 
                        WHERE ID_INVENTARIO = ? 
                        AND ID_PATRIMONIO = ? 
                        AND ID_PARTICIPANTE_INVENTARIO = ?
                        LIMIT 1
                    """;
                    
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSql)) {
                        checkStmt.setObject(1, idInventario);
                        checkStmt.setObject(2, idPatrimonio);
                        checkStmt.setObject(3, idParticipante);
                        
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                int existingId = rs.getInt("ID");
                                LOGGER.warning("⚠️ Coleta duplicada detectada! ID existente: " + existingId + 
                                             " (Patrimônio: " + idPatrimonio + ", Inventário: " + idInventario + ")");
                                // Marcar como sincronizada mesmo assim para não tentar novamente
                                atualizarIdRemoto("local_coleta", recordId, existingId);
                                return true;
                            }
                        }
                    }
                    
                    // Parâmetro 1: ID_PATRIMONIO
                    stmt.setObject(1, idPatrimonio);
                    // Parâmetro 2: ID_INVENTARIO
                    stmt.setObject(2, idInventario);
                    // Parâmetro 3: ID_COLETOR (mesmo valor que id_participante)
                    stmt.setObject(3, idParticipante);
                    // Parâmetro 4: ID_PARTICIPANTE_INVENTARIO (OBRIGATÓRIO - NOT NULL)
                    stmt.setObject(4, idParticipante);
                    
                    // ✅ CORRIGIDO: Tratamento robusto de timestamp para sincronização
                    Object dataColetaObj = dados.get("data_coleta");
                    Timestamp dataColeta = null;
                    try {
                        dataColeta = com.inventario.util.DateFormatUtils.toTimestampSafe(dataColetaObj);
                        LOGGER.info("Timestamp convertido: " + dataColetaObj + " -> " + dataColeta);
                    } catch (Exception e) {
                        LOGGER.warning("Erro ao converter timestamp '" + dataColetaObj + "': " + e.getMessage() + " - usando data atual");
                        dataColeta = new Timestamp(System.currentTimeMillis());
                    }
                    // Parâmetro 5: DATA_COLETA
                    stmt.setTimestamp(5, dataColeta);
                    
                    // Parâmetro 6: LOCALIZACAO_ATUAL
                    stmt.setString(6, objectToString(dados.get("localizacao_atual")));
                    // Parâmetro 7: LOCALIZACAO_ENCONTRADA
                    stmt.setString(7, objectToString(dados.get("localizacao_encontrada")));
                    // Parâmetro 8: ESTADO_ENCONTRADO
                    stmt.setString(8, objectToString(dados.get("situacao_encontrada")));
                    // Parâmetro 9: OBSERVACAO_COLETA
                    stmt.setString(9, objectToString(dados.get("observacoes")));
                    // Parâmetro 10: FOTO_PATRIMONIO
                    stmt.setString(10, objectToString(dados.get("foto_patrimonio")));
                    
                    LOGGER.info("Executando INSERT com: patrimonio=" + idPatrimonio + 
                               ", inventario=" + idInventario + 
                               ", participante=" + idParticipante);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int remoteId = rs.getInt(1);
                                atualizarIdRemoto("local_coleta", recordId, remoteId);
                                LOGGER.info("Coleta sincronizada com sucesso! ID remoto: " + remoteId);
                            }
                        }
                        return true;
                    }
                }
                break;
                
            case "UPDATE":
                String updateSql = """
                    UPDATE coleta SET 
                    id_patrimonio = ?, id_inventario = ?, id_participante = ?, numero_patrimonio = ?, 
                    data_coleta = ?, localizacao_atual = ?, localizacao_encontrada = ?, situacao_encontrada = ?, 
                    observacoes = ?, foto_patrimonio = ?, sem_etiqueta = ?, descricao_sem_etiqueta = ?
                    WHERE id = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    setColetaParameters(stmt, dados);
                    stmt.setInt(13, recordId);
                    
                    return stmt.executeUpdate() > 0;
                }
        }
        
        return false;
    }
    
    /**
     * Processa upload de inventário
     * @param conn Conexão com PostgreSQL
     * @param operacao Tipo de operação
     * @param recordId ID do registro
     * @param dados Dados do inventário
     * @return true se processado com sucesso
     * @throws SQLException
     */
    private boolean processarInventarioUpload(Connection conn, String operacao, int recordId, Map<String, Object> dados) throws SQLException {
        // CORREÇÃO: Nome correto da tabela no PostgreSQL é TABELA_INVENTARIO
        switch (operacao) {
            case "INSERT":
                String insertSql = """
                    INSERT INTO TABELA_INVENTARIO 
                    (NOME, OBSERVACAO, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO, RESPONSAVEL_INVENTARIO)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, (String) dados.get("nome"));
                    stmt.setString(2, (String) dados.get("descricao"));
                    
                    // Conversão segura para java.sql.Date
                    Object dataInicio = dados.get("data_inicio");
                    if (dataInicio instanceof java.util.Date) {
                        stmt.setDate(3, new java.sql.Date(((java.util.Date) dataInicio).getTime()));
                    } else if (dataInicio instanceof java.sql.Date) {
                        stmt.setDate(3, (java.sql.Date) dataInicio);
                    } else {
                        stmt.setDate(3, null);
                    }
                    
                    Object dataFim = dados.get("data_fim");
                    if (dataFim instanceof java.util.Date) {
                        stmt.setDate(4, new java.sql.Date(((java.util.Date) dataFim).getTime()));
                    } else if (dataFim instanceof java.sql.Date) {
                        stmt.setDate(4, (java.sql.Date) dataFim);
                    } else {
                        stmt.setDate(4, null);
                    }
                    
                    stmt.setString(5, (String) dados.get("status"));
                    stmt.setString(6, (String) dados.get("responsavel"));
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int remoteId = rs.getInt(1);
                                atualizarIdRemoto("local_inventario", recordId, remoteId);
                            }
                        }
                        return true;
                    }
                }
                break;
                
            case "UPDATE":
                String updateSql = """
                    UPDATE TABELA_INVENTARIO SET 
                    NOME = ?, OBSERVACAO = ?, DATA_INICIO = ?, DATA_FIM = ?, 
                    STATUS_INVENTARIO = ?, RESPONSAVEL_INVENTARIO = ?
                    WHERE ID = ?
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, (String) dados.get("nome"));
                    stmt.setString(2, (String) dados.get("descricao"));
                    
                    // Conversão segura para java.sql.Date
                    Object dataInicio = dados.get("data_inicio");
                    if (dataInicio instanceof java.util.Date) {
                        stmt.setDate(3, new java.sql.Date(((java.util.Date) dataInicio).getTime()));
                    } else if (dataInicio instanceof java.sql.Date) {
                        stmt.setDate(3, (java.sql.Date) dataInicio);
                    } else {
                        stmt.setDate(3, null);
                    }
                    
                    Object dataFim = dados.get("data_fim");
                    if (dataFim instanceof java.util.Date) {
                        stmt.setDate(4, new java.sql.Date(((java.util.Date) dataFim).getTime()));
                    } else if (dataFim instanceof java.sql.Date) {
                        stmt.setDate(4, (java.sql.Date) dataFim);
                    } else {
                        stmt.setDate(4, null);
                    }
                    
                    stmt.setString(5, (String) dados.get("status"));
                    stmt.setString(6, (String) dados.get("responsavel"));
                    stmt.setInt(7, recordId);
                    
                    return stmt.executeUpdate() > 0;
                }
        }
        
        return false;
    }
    
    // ==================== SINCRONIZAÇÃO DE DOWNLOAD ====================
    
    /**
     * Executa importação inicial completa de dados essenciais
     * Este método deve ser chamado quando o sistema entra em modo offline
     * para garantir que todos os dados necessários estejam disponíveis localmente
     * @return Resultado da importação
     */
    public SyncResult executarImportacaoInicial() {
        LOGGER.info("Iniciando importação inicial de dados essenciais");
        SyncResult result = new SyncResult();
        result.startTime = LocalDateTime.now();
        result.isManual = true;
        
        if (!connectivityManager.isOnline()) {
            result.success = false;
            result.errorMessage = "Sem conexão com o servidor para importação inicial";
            LOGGER.warning("Tentativa de importação inicial sem conexão");
            return result;
        }
        
        try {
            // Limpa dados locais existentes para importação completa
            limparDadosLocais();
            
            // Importa dados essenciais
            result.downloadedRecords += importarDadosEssenciais();
            
            // Marca como importação inicial concluída (formato ISO para compatibilidade)
            offlineDAO.atualizarMetadado("initial_import_completed", "true");
            offlineDAO.atualizarMetadado("initial_import_timestamp", 
                DateFormatUtils.formatForApi(LocalDateTime.now()));
            
            result.success = true;
            result.endTime = LocalDateTime.now();
            
            LOGGER.info(String.format("Importação inicial concluída: %d registros importados", 
                result.downloadedRecords));
                
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            result.endTime = LocalDateTime.now();
            
            LOGGER.log(Level.SEVERE, "Erro durante importação inicial", e);
        }
        
        return result;
    }
    
    /**
     * Importa todos os dados essenciais do servidor
     * @return Número total de registros importados
     * @throws SQLException
     */
    private int importarDadosEssenciais() throws SQLException {
        int totalImportados = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Importa dados de referência primeiro (ordem importante)
            totalImportados += importarCampus(conn);
            totalImportados += importarSetores(conn);
            totalImportados += importarSalas(conn);
            totalImportados += importarUsuarios(conn);
            
            // Importa dados principais
            totalImportados += importarPatrimoniosCompleto(conn);
            totalImportados += importarInventariosCompleto(conn);
            
            // ✅ CRÍTICO: Importar participantes ANTES das coletas
            totalImportados += importarParticipantesInventario(conn);
            
            totalImportados += importarColetasCompleto(conn);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao importar dados essenciais", e);
            throw e;
        }
        
        return totalImportados;
    }
    
    /**
     * Limpa dados locais para importação completa
     * @throws SQLException
     */
    private void limparDadosLocais() throws SQLException {
        LOGGER.info("Limpando dados locais para importação completa");
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection()) {
            // Desabilita foreign keys temporariamente
            try (PreparedStatement stmt = conn.prepareStatement("PRAGMA foreign_keys = OFF")) {
                stmt.execute();
            }
            
            // Limpa tabelas na ordem correta (dependências)
            String[] tabelas = {
                "local_coleta",
                "local_patrimonio", 
                "local_inventario",
                "local_participante_inventario",
                "local_sala",
                "local_setor",
                "local_campus",
                "local_usuario"
            };
            
            for (String tabela : tabelas) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tabela)) {
                    int deleted = stmt.executeUpdate();
                    LOGGER.info(String.format("Removidos %d registros de %s", deleted, tabela));
                }
            }
            
            // Reabilita foreign keys
            try (PreparedStatement stmt = conn.prepareStatement("PRAGMA foreign_keys = ON")) {
                stmt.execute();
            }
            
            conn.commit();
        }
    }
    
    /**
     * Importa todos os campus
     * @param conn Conexão com PostgreSQL
     * @return Número de campus importados
     * @throws SQLException
     */
    private int importarCampus(Connection conn) throws SQLException {
        String sql = "SELECT * FROM campus ORDER BY id";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirCampusLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importados %d campus", count));
        return count;
    }
    
    /**
     * Importa todos os setores
     * @param conn Conexão com PostgreSQL
     * @return Número de setores importados
     * @throws SQLException
     */
    private int importarSetores(Connection conn) throws SQLException {
        String sql = "SELECT * FROM setor ORDER BY id";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirSetorLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importados %d setores", count));
        return count;
    }
    
    /**
     * Importa todas as salas
     * @param conn Conexão com PostgreSQL
     * @return Número de salas importadas
     * @throws SQLException
     */
    private int importarSalas(Connection conn) throws SQLException {
        String sql = "SELECT * FROM sala ORDER BY id";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirSalaLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importadas %d salas", count));
        return count;
    }
    
    /**
     * Importa todos os usuários
     * @param conn Conexão com PostgreSQL
     * @return Número de usuários importados
     * @throws SQLException
     */
    private int importarUsuarios(Connection conn) throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY id";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirUsuarioLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importados %d usuários", count));
        return count;
    }
    
    /**
     * Importa todos os patrimônios
     * @param conn Conexão com PostgreSQL
     * @return Número de patrimônios importados
     * @throws SQLException
     */
    private int importarPatrimoniosCompleto(Connection conn) throws SQLException {
        String sql = "SELECT * FROM patrimonio ORDER BY id";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirPatrimonioLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importados %d patrimônios", count));
        return count;
    }
    
    /**
     * Importa todos os inventários
     * @param conn Conexão com PostgreSQL
     * @return Número de inventários importados
     * @throws SQLException
     */
    private int importarInventariosCompleto(Connection conn) throws SQLException {
        // CORREÇÃO: Nome correto da tabela no PostgreSQL é TABELA_INVENTARIO
        String sql = "SELECT * FROM TABELA_INVENTARIO ORDER BY ID";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirInventarioLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importados %d inventários", count));
        return count;
    }
    
    /**
     * ✅ NOVO: Importa participantes do inventário
     * @param conn Conexão com PostgreSQL
     * @return Número de participantes importados
     * @throws SQLException
     */
    private int importarParticipantesInventario(Connection conn) throws SQLException {
        String sql = """
            SELECT 
                ID_PARTICIPANTE as id_participante,
                ID_INVENTARIO as id_inventario,
                ID_USUARIO as id_usuario,
                PAPEL as papel,
                ATIVO as ativo,
                DATA_INCLUSAO as data_inclusao
            FROM TABELA_PARTICIPANTE_INVENTARIO 
            WHERE ATIVO = TRUE
            ORDER BY ID_PARTICIPANTE
        """;
        
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> participante = new HashMap<>();
                participante.put("id_participante", rs.getInt("id_participante"));
                participante.put("id_inventario", rs.getInt("id_inventario"));
                participante.put("id_usuario", rs.getInt("id_usuario"));
                participante.put("papel", rs.getString("papel"));
                participante.put("ativo", rs.getBoolean("ativo"));
                participante.put("data_inclusao", rs.getTimestamp("data_inclusao"));
                
                inserirParticipanteLocal(participante);
                count++;
            }
        }
        
        LOGGER.info(String.format("✅ Importados %d participantes do inventário", count));
        return count;
    }
    
    /**
     * Importa todas as coletas
     * @param conn Conexão com PostgreSQL
     * @return Número de coletas importadas
     * @throws SQLException
     */
    private int importarColetasCompleto(Connection conn) throws SQLException {
        String sql = "SELECT * FROM coleta ORDER BY id";
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                inserirColetaLocal(resultSetToMap(rs));
                count++;
            }
        }
        
        LOGGER.info(String.format("Importadas %d coletas", count));
        return count;
    }
    
    /**
     * Baixa dados atualizados do servidor
     * @return Número de registros baixados
     * @throws SQLException
     */
    private int baixarDadosServidor() throws SQLException {
        int totalBaixados = 0;
        
        // CORREÇÃO: Parsing robusto do timestamp da última sincronização
        String lastSyncStr = offlineDAO.obterMetadado("last_sync_timestamp");
        Timestamp lastSync;
        try {
            if (lastSyncStr != null && !lastSyncStr.isEmpty()) {
                // Tentar converter usando DateFormatUtils (suporta múltiplos formatos)
                lastSync = DateFormatUtils.toTimestampSafe(lastSyncStr);
                if (lastSync == null) {
                    // Fallback: tentar formato padrão do Timestamp
                    lastSync = Timestamp.valueOf(lastSyncStr);
                }
            } else {
                lastSync = Timestamp.valueOf("1970-01-01 00:00:00");
            }
        } catch (Exception e) {
            LOGGER.warning("Erro ao parsear timestamp '" + lastSyncStr + "': " + e.getMessage() + " - usando data padrão");
            lastSync = Timestamp.valueOf("1970-01-01 00:00:00");
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Verificar se a conexão está válida
            if (conn == null || conn.isClosed()) {
                LOGGER.warning("Conexão com banco de dados não disponível. Sincronização ignorada.");
                return 0;
            }
            
            // Baixa patrimônios atualizados
            totalBaixados += baixarPatrimonios(conn, lastSync);
            
            // Baixa inventários atualizados
            totalBaixados += baixarInventarios(conn, lastSync);
            
            // Baixa coletas atualizadas (se necessário)
            // totalBaixados += baixarColetas(conn, lastSync);
            
        } catch (SQLException e) {
            // Log do erro mas não propaga exceção para não quebrar o sistema
            LOGGER.log(Level.WARNING, "Erro ao baixar dados do servidor (banco pode não estar configurado): " + e.getMessage());
            // Não lança exceção para permitir que o sistema continue funcionando
            return 0;
        }
        
        return totalBaixados;
    }
    
    /**
     * Baixa patrimônios atualizados
     * @param conn Conexão com PostgreSQL
     * @param lastSync Timestamp da última sincronização
     * @return Número de patrimônios baixados
     * @throws SQLException
     */
    private int baixarPatrimonios(Connection conn, Timestamp lastSync) throws SQLException {
        // Verificar se a tabela existe antes de tentar consultar
        if (!tabelaExiste(conn, "tabela_patrimonio")) {
            LOGGER.warning("Tabela 'tabela_patrimonio' não existe no banco de dados. Sincronização ignorada.");
            return 0;
        }
        
        // Como a tabela não tem data_ultima_alteracao, usar data_carga como alternativa
        // ou sincronizar todos os registros (pode ser otimizado futuramente)
        String sql = """
            SELECT * FROM tabela_patrimonio 
            WHERE data_carga > ? OR data_carga IS NULL
            ORDER BY id
            LIMIT 1000
        """;
        
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, lastSync);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> patrimonio = resultSetToMap(rs);
                    
                    // Verifica se já existe localmente
                    if (existePatrimonioLocal(patrimonio)) {
                        atualizarPatrimonioLocal(patrimonio);
                    } else {
                        inserirPatrimonioLocal(patrimonio);
                    }
                    
                    count++;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao baixar patrimônios: " + e.getMessage());
            // Não propaga exceção para não quebrar o sistema
            return 0;
        }
        
        return count;
    }
    
    /**
     * Verifica se uma tabela existe no banco de dados
     * @param conn Conexão com o banco
     * @param tableName Nome da tabela
     * @return true se a tabela existe
     */
    private boolean tabelaExiste(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao verificar existência da tabela: " + tableName, e);
            return false;
        }
    }
    
    /**
     * Baixa inventários atualizados
     * @param conn Conexão com PostgreSQL
     * @param lastSync Timestamp da última sincronização
     * @return Número de inventários baixados
     * @throws SQLException
     */
    private int baixarInventarios(Connection conn, Timestamp lastSync) throws SQLException {
        // CORREÇÃO: Nome correto da tabela no PostgreSQL é TABELA_INVENTARIO
        // A tabela usa DATA_ULTIMA_ATUALIZACAO ao invés de data_ultima_alteracao
        String sql = """
            SELECT * FROM TABELA_INVENTARIO 
            WHERE DATA_ULTIMA_ATUALIZACAO > ? OR DATA_ULTIMA_ATUALIZACAO IS NULL
            ORDER BY ID
            LIMIT 1000
        """;
        
        int count = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, lastSync);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> inventario = resultSetToMap(rs);
                    
                    // Verifica se já existe localmente
                    if (existeInventarioLocal(inventario)) {
                        atualizarInventarioLocal(inventario);
                    } else {
                        inserirInventarioLocal(inventario);
                    }
                    
                    count++;
                }
            }
        } catch (SQLException e) {
            // Log do erro mas não propaga para não quebrar sincronização
            LOGGER.log(Level.WARNING, "Erro ao baixar inventários (tabela pode não existir): " + e.getMessage());
            return 0;
        }
        
        return count;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Define parâmetros para patrimônio
     * @param stmt PreparedStatement
     * @param dados Dados do patrimônio
     * @throws SQLException
     */
    private void setPatrimonioParameters(PreparedStatement stmt, Map<String, Object> dados) throws SQLException {
        stmt.setString(1, (String) dados.get("numero"));
        stmt.setString(2, (String) dados.get("descricao"));
        stmt.setString(3, (String) dados.get("descricao_resumida"));
        stmt.setString(4, (String) dados.get("marca"));
        stmt.setString(5, (String) dados.get("modelo"));
        stmt.setString(6, (String) dados.get("numero_serie"));
        stmt.setString(7, (String) dados.get("situacao"));
        stmt.setObject(8, dados.get("valor"));
        stmt.setObject(9, dados.get("data_aquisicao"));
        stmt.setObject(10, dados.get("id_setor"));
        stmt.setObject(11, dados.get("id_sala"));
        stmt.setString(12, (String) dados.get("observacoes"));
    }
    
    /**
     * Define parâmetros para coleta
     * @param stmt PreparedStatement
     * @param dados Dados da coleta
     * @throws SQLException
     */
    private void setColetaParameters(PreparedStatement stmt, Map<String, Object> dados) throws SQLException {
        stmt.setObject(1, dados.get("id_patrimonio"));
        stmt.setObject(2, dados.get("id_inventario"));
        stmt.setObject(3, dados.get("id_participante"));
        // ✅ CORRIGIDO: Conversão segura para String (pode vir como Integer)
        stmt.setString(4, objectToString(dados.get("numero_patrimonio")));
        // ✅ CORRIGIDO: Usar método seguro para timestamp
        stmt.setTimestamp(5, com.inventario.util.DateFormatUtils.toTimestampSafe(dados.get("data_coleta")));
        stmt.setString(6, objectToString(dados.get("localizacao_atual")));
        stmt.setString(7, objectToString(dados.get("localizacao_encontrada")));
        stmt.setString(8, objectToString(dados.get("situacao_encontrada")));
        stmt.setString(9, objectToString(dados.get("observacoes")));
        stmt.setString(10, objectToString(dados.get("foto_patrimonio")));
        stmt.setBoolean(11, (Boolean) dados.getOrDefault("sem_etiqueta", false));
        stmt.setString(12, objectToString(dados.get("descricao_sem_etiqueta")));
    }
    
    /**
     * Converte qualquer objeto para String de forma segura
     */
    private String objectToString(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }
    
    /**
     * Atualiza ID remoto no registro local
     * @param tabela Nome da tabela
     * @param localId ID local
     * @param remoteId ID remoto
     * @throws SQLException
     */
    private void atualizarIdRemoto(String tabela, int localId, int remoteId) throws SQLException {
        // ✅ CORRIGIDO: Marcar como sincronizado na sync_control E na tabela local
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection()) {
            // 1. Marcar na sync_control
            String sql1 = "UPDATE sync_control SET synced = TRUE WHERE table_name = ? AND record_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setString(1, tabela);
                stmt.setInt(2, localId);
                
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    LOGGER.info(String.format("✅ Sincronização marcada na sync_control: %s local=%d -> remoto=%d", 
                        tabela, localId, remoteId));
                }
            }
            
            // 2. Atualizar sync_status na tabela local (se for coleta)
            if ("local_coleta".equals(tabela)) {
                String sql2 = "UPDATE local_coleta SET sync_status = 'SYNCED' WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setInt(1, localId);
                    
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        LOGGER.info(String.format("✅ sync_status atualizado: local_coleta id=%d", localId));
                    }
                }
            }
        }
    }
    
    /**
     * Verifica se patrimônio existe localmente
     * @param patrimonio Dados do patrimônio
     * @return true se existe
     * @throws SQLException
     */
    private boolean existePatrimonioLocal(Map<String, Object> patrimonio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM local_patrimonio WHERE remote_id = ?";
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, patrimonio.get("id"));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Atualiza patrimônio local
     * @param patrimonio Dados do patrimônio
     * @throws SQLException
     */
    private void atualizarPatrimonioLocal(Map<String, Object> patrimonio) throws SQLException {
        String sql = """
            UPDATE local_patrimonio SET 
            numero = ?, descricao = ?, descricao_resumida = ?, marca = ?, modelo = ?, 
            numero_serie = ?, situacao = ?, valor = ?, data_aquisicao = ?, 
            id_setor = ?, id_sala = ?, observacoes = ?, data_ultima_alteracao = ?
            WHERE remote_id = ?
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, (String) patrimonio.get("numero"));
            stmt.setString(2, (String) patrimonio.get("descricao"));
            stmt.setString(3, (String) patrimonio.get("descricao_resumida"));
            stmt.setString(4, (String) patrimonio.get("marca"));
            stmt.setString(5, (String) patrimonio.get("modelo"));
            stmt.setString(6, (String) patrimonio.get("numero_serie"));
            stmt.setString(7, (String) patrimonio.get("situacao"));
            stmt.setObject(8, patrimonio.get("valor"));
            stmt.setObject(9, patrimonio.get("data_aquisicao"));
            stmt.setObject(10, patrimonio.get("id_setor"));
            stmt.setObject(11, patrimonio.get("id_sala"));
            stmt.setString(12, (String) patrimonio.get("observacoes"));
            stmt.setTimestamp(13, (Timestamp) patrimonio.get("data_ultima_alteracao"));
            stmt.setObject(14, patrimonio.get("id"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insere patrimônio local
     * @param patrimonio Dados do patrimônio
     * @throws SQLException
     */
    private void inserirPatrimonioLocal(Map<String, Object> patrimonio) throws SQLException {
        String sql = """
            INSERT INTO local_patrimonio 
            (remote_id, numero, descricao, descricao_resumida, marca, modelo, numero_serie, 
             situacao, valor, data_aquisicao, id_setor, id_sala, observacoes, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, patrimonio.get("id"));
            // Ajustar índices para setPatrimonioParameters (começando do índice 2)
            stmt.setString(2, (String) patrimonio.get("numero"));
            stmt.setString(3, (String) patrimonio.get("descricao"));
            stmt.setString(4, (String) patrimonio.get("descricao_resumida"));
            stmt.setString(5, (String) patrimonio.get("marca"));
            stmt.setString(6, (String) patrimonio.get("modelo"));
            stmt.setString(7, (String) patrimonio.get("numero_serie"));
            stmt.setString(8, (String) patrimonio.get("situacao"));
            stmt.setObject(9, patrimonio.get("valor"));
            stmt.setObject(10, patrimonio.get("data_aquisicao"));
            stmt.setObject(11, patrimonio.get("id_setor"));
            stmt.setObject(12, patrimonio.get("id_sala"));
            stmt.setString(13, (String) patrimonio.get("observacoes"));
            stmt.setTimestamp(14, (Timestamp) patrimonio.get("data_ultima_alteracao"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Verifica se inventário existe localmente
     * @param inventario Dados do inventário
     * @return true se existe
     * @throws SQLException
     */
    private boolean existeInventarioLocal(Map<String, Object> inventario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM local_inventario WHERE remote_id = ?";
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, inventario.get("id"));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Atualiza inventário local
     * @param inventario Dados do inventário
     * @throws SQLException
     */
    private void atualizarInventarioLocal(Map<String, Object> inventario) throws SQLException {
        String sql = """
            UPDATE local_inventario SET 
            nome = ?, descricao = ?, data_inicio = ?, data_fim = ?, 
            status = ?, id_responsavel = ?, observacoes = ?, data_ultima_alteracao = ?
            WHERE remote_id = ?
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, (String) inventario.get("nome"));
            stmt.setString(2, (String) inventario.get("descricao"));
            
            // Conversão segura para java.sql.Date
            Object dataInicio = inventario.get("data_inicio");
            if (dataInicio instanceof java.util.Date) {
                stmt.setDate(3, new java.sql.Date(((java.util.Date) dataInicio).getTime()));
            } else if (dataInicio instanceof java.sql.Date) {
                stmt.setDate(3, (java.sql.Date) dataInicio);
            } else {
                stmt.setDate(3, null);
            }
            
            Object dataFim = inventario.get("data_fim");
            if (dataFim instanceof java.util.Date) {
                stmt.setDate(4, new java.sql.Date(((java.util.Date) dataFim).getTime()));
            } else if (dataFim instanceof java.sql.Date) {
                stmt.setDate(4, (java.sql.Date) dataFim);
            } else {
                stmt.setDate(4, null);
            }
            
            stmt.setString(5, (String) inventario.get("status"));
            stmt.setObject(6, inventario.get("id_responsavel"));
            stmt.setString(7, (String) inventario.get("observacoes"));
            stmt.setTimestamp(8, (Timestamp) inventario.get("data_ultima_alteracao"));
            stmt.setObject(9, inventario.get("id"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insere inventário local
     * @param inventario Dados do inventário
     * @throws SQLException
     */
    private void inserirInventarioLocal(Map<String, Object> inventario) throws SQLException {
        String sql = """
            INSERT INTO local_inventario 
            (remote_id, nome, descricao, data_inicio, data_fim, status, id_responsavel, observacoes, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, inventario.get("id"));
            stmt.setString(2, (String) inventario.get("nome"));
            stmt.setString(3, (String) inventario.get("descricao"));
            
            // Conversão segura para java.sql.Date
            Object dataInicio = inventario.get("data_inicio");
            if (dataInicio instanceof java.util.Date) {
                stmt.setDate(4, new java.sql.Date(((java.util.Date) dataInicio).getTime()));
            } else if (dataInicio instanceof java.sql.Date) {
                stmt.setDate(4, (java.sql.Date) dataInicio);
            } else {
                stmt.setDate(4, null);
            }
            
            Object dataFim = inventario.get("data_fim");
            if (dataFim instanceof java.util.Date) {
                stmt.setDate(5, new java.sql.Date(((java.util.Date) dataFim).getTime()));
            } else if (dataFim instanceof java.sql.Date) {
                stmt.setDate(5, (java.sql.Date) dataFim);
            } else {
                stmt.setDate(5, null);
            }
            
            stmt.setString(6, (String) inventario.get("status"));
            stmt.setObject(7, inventario.get("id_responsavel"));
            stmt.setString(8, (String) inventario.get("observacoes"));
            stmt.setTimestamp(9, (Timestamp) inventario.get("data_ultima_alteracao"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * ✅ NOVO: Insere participante do inventário no banco local
     * @param participante Dados do participante
     * @throws SQLException
     */
    private void inserirParticipanteLocal(Map<String, Object> participante) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_participante_inventario 
            (id_participante, id_inventario, id_usuario, papel, ativo, data_inclusao)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) participante.get("id_participante"));
            stmt.setInt(2, (Integer) participante.get("id_inventario"));
            stmt.setInt(3, (Integer) participante.get("id_usuario"));
            stmt.setString(4, (String) participante.get("papel"));
            stmt.setBoolean(5, (Boolean) participante.getOrDefault("ativo", true));
            
            // Conversão segura de timestamp
            Object dataInclusao = participante.get("data_inclusao");
            if (dataInclusao instanceof Timestamp) {
                stmt.setTimestamp(6, (Timestamp) dataInclusao);
            } else if (dataInclusao instanceof java.util.Date) {
                stmt.setTimestamp(6, new Timestamp(((java.util.Date) dataInclusao).getTime()));
            } else {
                stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }
            
            stmt.executeUpdate();
            
            LOGGER.fine(String.format("Participante inserido: ID=%d, Inventário=%d, Usuário=%d", 
                participante.get("id_participante"), 
                participante.get("id_inventario"),
                participante.get("id_usuario")));
        }
    }
    
    /**
     * Insere campus no banco local
     * @param campus Dados do campus
     * @throws SQLException
     */
    private void inserirCampusLocal(Map<String, Object> campus) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_campus 
            (remote_id, nome, endereco, cidade, estado, cep, telefone, email, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, campus.get("id"));
            stmt.setString(2, (String) campus.get("nome"));
            stmt.setString(3, (String) campus.get("endereco"));
            stmt.setString(4, (String) campus.get("cidade"));
            stmt.setString(5, (String) campus.get("estado"));
            stmt.setString(6, (String) campus.get("cep"));
            stmt.setString(7, (String) campus.get("telefone"));
            stmt.setString(8, (String) campus.get("email"));
            stmt.setTimestamp(9, (Timestamp) campus.get("data_ultima_alteracao"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insere setor no banco local
     * @param setor Dados do setor
     * @throws SQLException
     */
    private void inserirSetorLocal(Map<String, Object> setor) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_setor 
            (remote_id, nome, descricao, id_campus, responsavel, telefone, email, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, setor.get("id"));
            stmt.setString(2, (String) setor.get("nome"));
            stmt.setString(3, (String) setor.get("descricao"));
            stmt.setObject(4, setor.get("id_campus"));
            stmt.setString(5, (String) setor.get("responsavel"));
            stmt.setString(6, (String) setor.get("telefone"));
            stmt.setString(7, (String) setor.get("email"));
            stmt.setTimestamp(8, (Timestamp) setor.get("data_ultima_alteracao"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insere sala no banco local
     * @param sala Dados da sala
     * @throws SQLException
     */
    private void inserirSalaLocal(Map<String, Object> sala) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_sala 
            (remote_id, numero, nome, descricao, id_setor, capacidade, tipo, observacoes, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, sala.get("id"));
            stmt.setString(2, (String) sala.get("numero"));
            stmt.setString(3, (String) sala.get("nome"));
            stmt.setString(4, (String) sala.get("descricao"));
            stmt.setObject(5, sala.get("id_setor"));
            stmt.setObject(6, sala.get("capacidade"));
            stmt.setString(7, (String) sala.get("tipo"));
            stmt.setString(8, (String) sala.get("observacoes"));
            stmt.setTimestamp(9, (Timestamp) sala.get("data_ultima_alteracao"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insere usuário no banco local
     * @param usuario Dados do usuário
     * @throws SQLException
     */
    private void inserirUsuarioLocal(Map<String, Object> usuario) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_usuario 
            (remote_id, nome, email, login, perfil, ativo, id_setor, telefone, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, usuario.get("id"));
            stmt.setString(2, (String) usuario.get("nome"));
            stmt.setString(3, (String) usuario.get("email"));
            stmt.setString(4, (String) usuario.get("login"));
            stmt.setString(5, (String) usuario.get("perfil"));
            stmt.setBoolean(6, (Boolean) usuario.get("ativo"));
            stmt.setObject(7, usuario.get("id_setor"));
            stmt.setString(8, (String) usuario.get("telefone"));
            stmt.setTimestamp(9, (Timestamp) usuario.get("data_ultima_alteracao"));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insere coleta no banco local
     * @param coleta Dados da coleta
     * @throws SQLException
     */
    private void inserirColetaLocal(Map<String, Object> coleta) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_coleta 
            (remote_id, id_patrimonio, id_inventario, id_usuario, data_coleta, status, observacoes, localizacao_atual, data_ultima_alteracao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, coleta.get("id"));
            stmt.setObject(2, coleta.get("id_patrimonio"));
            stmt.setObject(3, coleta.get("id_inventario"));
            stmt.setObject(4, coleta.get("id_usuario"));
            
            // Conversão segura para java.sql.Timestamp
            Object dataColeta = coleta.get("data_coleta");
            if (dataColeta instanceof java.util.Date) {
                stmt.setTimestamp(5, new Timestamp(((java.util.Date) dataColeta).getTime()));
            } else if (dataColeta instanceof Timestamp) {
                stmt.setTimestamp(5, (Timestamp) dataColeta);
            } else if (dataColeta instanceof String) {
                try {
                    stmt.setTimestamp(5, Timestamp.valueOf((String) dataColeta));
                } catch (IllegalArgumentException e) {
                    stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                }
            } else {
                stmt.setTimestamp(5, null);
            }
            
            stmt.setString(6, (String) coleta.get("status"));
            stmt.setString(7, (String) coleta.get("observacoes"));
            stmt.setString(8, (String) coleta.get("localizacao_atual"));
            
            // Conversão segura para data_ultima_alteracao
            Object dataUltimaAlteracao = coleta.get("data_ultima_alteracao");
            if (dataUltimaAlteracao instanceof Timestamp) {
                stmt.setTimestamp(9, (Timestamp) dataUltimaAlteracao);
            } else if (dataUltimaAlteracao instanceof java.util.Date) {
                stmt.setTimestamp(9, new Timestamp(((java.util.Date) dataUltimaAlteracao).getTime()));
            } else if (dataUltimaAlteracao instanceof String) {
                try {
                    stmt.setTimestamp(9, Timestamp.valueOf((String) dataUltimaAlteracao));
                } catch (IllegalArgumentException e) {
                    stmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
                }
            } else {
                stmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Verifica a integridade dos dados importados
     * @return Relatório de integridade
     * @throws SQLException
     */
    public IntegrityReport verificarIntegridadeDados() throws SQLException {
        IntegrityReport report = new IntegrityReport();
        
        try (Connection conn = SQLiteConnection.getInstance().getConnection()) {
            
            // Verifica se as tabelas essenciais têm dados
            report.campusCount = contarRegistros(conn, "local_campus");
            report.setoresCount = contarRegistros(conn, "local_setor");
            report.salasCount = contarRegistros(conn, "local_sala");
            report.usuariosCount = contarRegistros(conn, "local_usuario");
            report.patrimoniosCount = contarRegistros(conn, "local_patrimonio");
            report.inventariosCount = contarRegistros(conn, "local_inventario");
            report.coletasCount = contarRegistros(conn, "local_coleta");
            
            // Verifica integridade referencial
            report.setoresOrfaos = verificarSetoresSemCampus(conn);
            report.salasOrfaos = verificarSalasSemSetor(conn);
            report.usuariosOrfaos = verificarUsuariosSemSetor(conn);
            report.patrimoniosOrfaos = verificarPatrimoniosSemSala(conn);
            report.coletasOrfaos = verificarColetasSemReferencias(conn);
            
            // Calcula score de integridade
            report.calculateIntegrityScore();
            
            LOGGER.info(String.format("Verificação de integridade concluída. Score: %.2f%%", report.integrityScore));
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar integridade dos dados", e);
            throw e;
        }
        
        return report;
    }
    
    /**
     * Conta registros em uma tabela
     * @param conn Conexão SQLite
     * @param tableName Nome da tabela
     * @return Número de registros
     * @throws SQLException
     */
    private int contarRegistros(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Verifica setores sem campus válido
     * @param conn Conexão SQLite
     * @return Número de setores órfãos
     * @throws SQLException
     */
    private int verificarSetoresSemCampus(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM local_setor s 
            LEFT JOIN local_campus c ON s.id_campus = c.remote_id 
            WHERE c.remote_id IS NULL AND s.id_campus IS NOT NULL
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Verifica salas sem setor válido
     * @param conn Conexão SQLite
     * @return Número de salas órfãs
     * @throws SQLException
     */
    private int verificarSalasSemSetor(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM local_sala s 
            LEFT JOIN local_setor st ON s.id_setor = st.remote_id 
            WHERE st.remote_id IS NULL AND s.id_setor IS NOT NULL
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Verifica usuários sem setor válido
     * @param conn Conexão SQLite
     * @return Número de usuários órfãos
     * @throws SQLException
     */
    private int verificarUsuariosSemSetor(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM local_usuario u 
            LEFT JOIN local_setor s ON u.id_setor = s.remote_id 
            WHERE s.remote_id IS NULL AND u.id_setor IS NOT NULL
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Verifica patrimônios sem sala válida
     * @param conn Conexão SQLite
     * @return Número de patrimônios órfãos
     * @throws SQLException
     */
    private int verificarPatrimoniosSemSala(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM local_patrimonio p 
            LEFT JOIN local_sala s ON p.id_sala = s.remote_id 
            WHERE s.remote_id IS NULL AND p.id_sala IS NOT NULL
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Verifica coletas sem referências válidas
     * @param conn Conexão SQLite
     * @return Número de coletas órfãs
     * @throws SQLException
     */
    private int verificarColetasSemReferencias(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM local_coleta c 
            LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.remote_id 
            LEFT JOIN local_inventario i ON c.id_inventario = i.remote_id 
            LEFT JOIN local_usuario u ON c.id_usuario = u.remote_id 
            WHERE (p.remote_id IS NULL AND c.id_patrimonio IS NOT NULL) 
               OR (i.remote_id IS NULL AND c.id_inventario IS NOT NULL) 
               OR (u.remote_id IS NULL AND c.id_usuario IS NOT NULL)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Classe para relatório de integridade
     */
    public static class IntegrityReport {
        public int campusCount = 0;
        public int setoresCount = 0;
        public int salasCount = 0;
        public int usuariosCount = 0;
        public int patrimoniosCount = 0;
        public int inventariosCount = 0;
        public int coletasCount = 0;
        
        public int setoresOrfaos = 0;
        public int salasOrfaos = 0;
        public int usuariosOrfaos = 0;
        public int patrimoniosOrfaos = 0;
        public int coletasOrfaos = 0;
        
        public double integrityScore = 0.0;
        
        public void calculateIntegrityScore() {
            int totalRecords = campusCount + setoresCount + salasCount + usuariosCount + patrimoniosCount + inventariosCount + coletasCount;
            int totalOrphans = setoresOrfaos + salasOrfaos + usuariosOrfaos + patrimoniosOrfaos + coletasOrfaos;
            
            if (totalRecords == 0) {
                integrityScore = 0.0;
            } else {
                // Score baseado na presença de dados essenciais e integridade referencial
                double dataScore = (campusCount > 0 && setoresCount > 0 && salasCount > 0 && usuariosCount > 0) ? 50.0 : 0.0;
                double referentialScore = totalRecords > 0 ? ((double)(totalRecords - totalOrphans) / totalRecords) * 50.0 : 0.0;
                integrityScore = dataScore + referentialScore;
            }
        }
        
        @Override
        public String toString() {
            return String.format(
                "Relatório de Integridade:\n" +
                "- Campus: %d\n" +
                "- Setores: %d (%d órfãos)\n" +
                "- Salas: %d (%d órfãs)\n" +
                "- Usuários: %d (%d órfãos)\n" +
                "- Patrimônios: %d (%d órfãos)\n" +
                "- Inventários: %d\n" +
                "- Coletas: %d (%d órfãs)\n" +
                "- Score de Integridade: %.2f%%",
                campusCount, setoresCount, setoresOrfaos, salasCount, salasOrfaos,
                usuariosCount, usuariosOrfaos, patrimoniosCount, patrimoniosOrfaos,
                inventariosCount, coletasCount, coletasOrfaos, integrityScore
            );
        }
    }
    
    /**
     * Converte ResultSet para Map
     * @param rs ResultSet
     * @return Map com dados
     * @throws SQLException
     */
    private Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            map.put(metaData.getColumnName(i), rs.getObject(i));
        }
        
        return map;
    }
    
    /**
     * Converte JSON simples para Map
     * @param json String JSON
     * @return Map com dados
     */
    private Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        
        if (json == null || json.trim().isEmpty() || json.equals("{}")) {
            return map;
        }
        
        try {
            // Remove chaves e espaços
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
            }
            
            // Divide por vírgulas (parser simples)
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("[\"']", "");
                    String value = keyValue[1].trim().replaceAll("[\"']", "");
                    
                    // Tenta converter para tipos apropriados
                    if ("null".equals(value)) {
                        map.put(key, null);
                    } else if ("true".equals(value) || "false".equals(value)) {
                        map.put(key, Boolean.parseBoolean(value));
                    } else if (value.matches("-?\\d+")) {
                        map.put(key, Integer.parseInt(value));
                    } else if (value.matches("-?\\d+\\.\\d+")) {
                        map.put(key, Double.parseDouble(value));
                    } else {
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao fazer parse do JSON: " + json, e);
        }
        
        return map;
    }
    
    // ==================== CONFIGURAÇÕES ====================
    
    /**
     * Define intervalo de sincronização automática
     * @param minutes Intervalo em minutos
     */
    public void setSyncInterval(int minutes) {
        this.syncIntervalMinutes = minutes;
        if (autoSyncEnabled && connectivityManager.isOnline()) {
            scheduleAutoSync(); // Reagenda com novo intervalo
        }
    }
    
    /**
     * Define estratégia de resolução de conflitos
     * @param strategy Estratégia
     */
    public void setConflictResolution(ConflictResolution strategy) {
        this.conflictStrategy = strategy;
        LOGGER.info("Estratégia de conflito alterada para: " + strategy);
    }
    
    /**
     * Obtém status da sincronização
     * @return Status atual
     */
    public SyncStatus getStatus() {
        SyncStatus status = new SyncStatus();
        status.autoSyncEnabled = autoSyncEnabled;
        status.connected = connectivityManager.isOnline();
        status.syncInterval = syncIntervalMinutes;
        status.conflictStrategy = conflictStrategy;
        
        try {
            status.pendingOperations = offlineDAO.listarOperacoesPendentes(1000).size();
            status.lastSyncTime = offlineDAO.obterMetadado("last_sync_timestamp");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao obter status", e);
        }
        
        return status;
    }
    
    /**
     * Finaliza o sincronizador
     */
    public void shutdown() {
        LOGGER.info("Finalizando DataSynchronizer");
        stopAutoSync();
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // ==================== CLASSES AUXILIARES ====================
    
    /**
     * Resultado de uma operação de sincronização
     */
    public static class SyncResult {
        public boolean success;
        public String errorMessage;
        public int uploadedRecords;
        public int downloadedRecords;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public boolean isManual;
        
        public long getDurationMillis() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
        
        @Override
        public String toString() {
            return String.format("SyncResult{success=%s, uploaded=%d, downloaded=%d, duration=%dms, manual=%s}", 
                success, uploadedRecords, downloadedRecords, getDurationMillis(), isManual);
        }
    }
    
    /**
     * Status atual da sincronização
     */
    public static class SyncStatus {
        public boolean autoSyncEnabled;
        public boolean connected;
        public int syncInterval;
        public ConflictResolution conflictStrategy;
        public int pendingOperations;
        public String lastSyncTime;
        
        @Override
        public String toString() {
            return String.format("SyncStatus{auto=%s, connected=%s, interval=%dm, pending=%d, lastSync=%s}", 
                autoSyncEnabled, connected, syncInterval, pendingOperations, lastSyncTime);
        }
    }
}