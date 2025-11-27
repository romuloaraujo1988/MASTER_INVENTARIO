package com.inventario.offline;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DAO para operações offline no banco SQLite local
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class OfflineDAO {
    
    private static final Logger LOGGER = Logger.getLogger(OfflineDAO.class.getName());
    // Formatação de datas centralizada em DateFormatUtils
    
    private final SQLiteConnection sqliteConnection;
    
    public OfflineDAO() {
        this.sqliteConnection = SQLiteConnection.getInstance();
    }
    
    // ==================== OPERAÇÕES DE PATRIMÔNIO ====================
    
    /**
     * Salva um patrimônio no banco local
     * @param patrimonio Dados do patrimônio
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarPatrimonio(Map<String, Object> patrimonio) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_patrimonio 
            (id, numero, descricao, descricao_resumida, marca, modelo, numero_serie, 
             situacao, valor, data_aquisicao, id_setor, id_sala, observacoes, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setPatrimonioParameters(stmt, patrimonio);
            stmt.setString(14, "SYNCED"); // Dados importados já estão sincronizados
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Retornar o ID que foi passado no Map
                Integer id = (Integer) patrimonio.get("id");
                if (id != null) {
                    LOGGER.fine("Patrimônio salvo offline - ID: " + id);
                    return id;
                }
            }
            
            throw new SQLException("Falha ao inserir patrimônio");
            
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERRO ao salvar patrimônio no SQLite:");
            System.err.println(">>>   ID: " + patrimonio.get("id"));
            System.err.println(">>>   Número: " + patrimonio.get("numero"));
            System.err.println(">>>   Erro: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao salvar patrimônio offline", e);
            throw e;
        }
    }
    
    /**
     * Atualiza um patrimônio no banco local
     * @param id ID do patrimônio
     * @param patrimonio Dados atualizados
     * @return true se atualizado com sucesso
     * @throws SQLException
     */
    public boolean atualizarPatrimonio(int id, Map<String, Object> patrimonio) throws SQLException {
        String sql = """
            UPDATE local_patrimonio SET 
            numero = ?, descricao = ?, descricao_resumida = ?, marca = ?, modelo = ?, 
            numero_serie = ?, situacao = ?, valor = ?, data_aquisicao = ?, 
            id_setor = ?, id_sala = ?, observacoes = ?, sync_status = 'PENDING',
            last_modified = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setPatrimonioParameters(stmt, patrimonio);
            stmt.setInt(13, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                registrarOperacaoSync("local_patrimonio", id, "UPDATE", patrimonio);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar patrimônio offline", e);
            throw e;
        }
    }
    
    /**
     * Lista patrimônios com filtros
     * @param filtros Mapa de filtros
     * @param limite Limite de registros
     * @param offset Offset para paginação
     * @return Lista de patrimônios
     * @throws SQLException
     */
    public List<Map<String, Object>> listarPatrimonios(Map<String, Object> filtros, int limite, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM local_patrimonio WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        
        // Aplica filtros
        if (filtros != null) {
            if (filtros.containsKey("numero")) {
                sql.append(" AND numero LIKE ?");
                parametros.add("%" + filtros.get("numero") + "%");
            }
            if (filtros.containsKey("descricao")) {
                sql.append(" AND descricao LIKE ?");
                parametros.add("%" + filtros.get("descricao") + "%");
            }
            if (filtros.containsKey("situacao")) {
                sql.append(" AND situacao = ?");
                parametros.add(filtros.get("situacao"));
            }
            if (filtros.containsKey("id_setor")) {
                sql.append(" AND id_setor = ?");
                parametros.add(filtros.get("id_setor"));
            }
        }
        
        sql.append(" ORDER BY numero LIMIT ? OFFSET ?");
        parametros.add(limite);
        parametros.add(offset);
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return resultSetToMapList(rs);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar patrimônios offline", e);
            throw e;
        }
    }
    
    // ==================== OPERAÇÕES DE COLETA ====================
    
    /**
     * Salva uma coleta no banco local
     * @param coleta Dados da coleta
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarColeta(Map<String, Object> coleta) throws SQLException {
        String sql = """
            INSERT INTO local_coleta 
            (id_patrimonio, id_inventario, id_participante, numero_patrimonio, 
             data_coleta, localizacao_atual, localizacao_encontrada, situacao_encontrada, 
             observacoes, foto_patrimonio, sem_etiqueta, descricao_sem_etiqueta, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setColetaParameters(stmt, coleta);
            stmt.setString(13, "PENDING");
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Para coletas, usar last_insert_rowid() do SQLite
                try (var stmtId = conn.createStatement();
                     var rs = stmtId.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        registrarOperacaoSync("local_coleta", id, "INSERT", coleta);
                        LOGGER.fine("Coleta salva offline - ID: " + id);
                        return id;
                    }
                }
            }
            
            throw new SQLException("Falha ao inserir coleta");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar coleta offline", e);
            throw e;
        }
    }
    
    /**
     * Lista coletas por inventário
     * @param idInventario ID do inventário
     * @param limite Limite de registros
     * @param offset Offset para paginação
     * @return Lista de coletas
     * @throws SQLException
     */
    public List<Map<String, Object>> listarColetasPorInventario(int idInventario, int limite, int offset) throws SQLException {
        String sql = """
            SELECT * FROM local_coleta 
            WHERE id_inventario = ? 
            ORDER BY data_coleta DESC 
            LIMIT ? OFFSET ?
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, limite);
            stmt.setInt(3, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return resultSetToMapList(rs);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar coletas offline", e);
            throw e;
        }
    }
    
    // ==================== OPERAÇÕES DE INVENTÁRIO ====================
    
    /**
     * Salva um inventário no banco local
     * @param inventario Dados do inventário
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarInventario(Map<String, Object> inventario) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_inventario 
            (id, nome, descricao, data_inicio, data_fim, status, id_responsavel, observacoes, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, inventario.get("id"));
            stmt.setString(2, (String) inventario.get("nome"));
            stmt.setString(3, (String) inventario.get("descricao"));
            // Conversão segura para java.sql.Date
            Object dataInicio = inventario.get("data_inicio");
            Object dataFim = inventario.get("data_fim");
            
            stmt.setDate(4, dataInicio != null ? new java.sql.Date(((java.util.Date) dataInicio).getTime()) : null);
            stmt.setDate(5, dataFim != null ? new java.sql.Date(((java.util.Date) dataFim).getTime()) : null);
            stmt.setString(6, (String) inventario.get("status"));
            stmt.setObject(7, inventario.get("id_responsavel"));
            stmt.setString(8, (String) inventario.get("observacoes"));
            stmt.setString(9, "SYNCED"); // Dados importados já estão sincronizados
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Integer id = (Integer) inventario.get("id");
                if (id != null) {
                    LOGGER.fine("Inventário salvo offline - ID: " + id);
                    return id;
                }
            }
            
            throw new SQLException("Falha ao inserir inventário");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar inventário offline", e);
            throw e;
        }
    }
    
    /**
     * Lista inventários ativos
     * @return Lista de inventários
     * @throws SQLException
     */
    public List<Map<String, Object>> listarInventariosAtivos() throws SQLException {
        String sql = "SELECT * FROM local_inventario WHERE status IN ('PLANEJAMENTO', 'EM_ANDAMENTO') ORDER BY data_inicio DESC";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return resultSetToMapList(rs);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar inventários offline", e);
            throw e;
        }
    }
    
    // ==================== OPERAÇÕES DE SINCRONIZAÇÃO ====================
    
    /**
     * Registra uma operação para sincronização
     * @param tabela Nome da tabela
     * @param recordId ID do registro
     * @param operacao Tipo de operação (INSERT, UPDATE, DELETE)
     * @param dados Dados do registro
     * @throws SQLException
     */
    public void registrarOperacaoSync(String tabela, int recordId, String operacao, Map<String, Object> dados) throws SQLException {
        String sql = """
            INSERT INTO sync_control (table_name, record_id, operation, data_json)
            VALUES (?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tabela);
            stmt.setInt(2, recordId);
            stmt.setString(3, operacao);
            stmt.setString(4, mapToJson(dados));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao registrar operação de sync", e);
            throw e;
        }
    }
    
    /**
     * Lista operações pendentes de sincronização
     * @param limite Limite de registros
     * @return Lista de operações pendentes
     * @throws SQLException
     */
    public List<Map<String, Object>> listarOperacoesPendentes(int limite) throws SQLException {
        String sql = """
            SELECT * FROM sync_control 
            WHERE synced = FALSE 
            ORDER BY timestamp ASC 
            LIMIT ?
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return resultSetToMapList(rs);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar operações pendentes", e);
            throw e;
        }
    }
    
    /**
     * Marca uma operação como sincronizada
     * @param syncId ID da operação de sync
     * @throws SQLException
     */
    public void marcarComoSincronizado(int syncId) throws SQLException {
        String sql = "UPDATE sync_control SET synced = TRUE WHERE id = ?";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, syncId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao marcar como sincronizado", e);
            throw e;
        }
    }
    
    /**
     * Registra erro de sincronização
     * @param syncId ID da operação de sync
     * @param erro Mensagem de erro
     * @throws SQLException
     */
    public void registrarErroSync(int syncId, String erro) throws SQLException {
        String sql = "UPDATE sync_control SET error_message = ? WHERE id = ?";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, erro);
            stmt.setInt(2, syncId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao registrar erro de sync", e);
            throw e;
        }
    }
    
    /**
     * Conta o total de coletas pendentes de sincronização
     * Verifica tanto a tabela sync_control quanto o campo sync_status da local_coleta
     * @return Número de coletas pendentes
     */
    public int contarColetasPendentes() {
        int total = 0;
        
        try (Connection conn = sqliteConnection.getConnection()) {
            // Método 1: Contar na tabela sync_control (operações pendentes)
            String sql1 = "SELECT COUNT(*) FROM sync_control WHERE synced = FALSE AND table_name = 'local_coleta'";
            try (PreparedStatement stmt = conn.prepareStatement(sql1);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }
            
            // Método 2: Se não encontrou na sync_control, verificar sync_status na local_coleta
            if (total == 0) {
                String sql2 = "SELECT COUNT(*) FROM local_coleta WHERE sync_status = 'PENDING' OR sync_status IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sql2);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }
            
            LOGGER.fine("Coletas pendentes de sincronização: " + total);
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao contar coletas pendentes: " + e.getMessage());
        }
        
        return total;
    }
    
    /**
     * Conta o total de operações pendentes de sincronização (todas as tabelas)
     * @return Número total de operações pendentes
     */
    public int contarOperacoesPendentes() {
        String sql = "SELECT COUNT(*) FROM sync_control WHERE synced = FALSE";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao contar operações pendentes: " + e.getMessage());
        }
        
        return 0;
    }
    
    // ==================== OPERAÇÕES DE METADADOS ====================
    
    /**
     * Atualiza metadado de sincronização
     * @param chave Chave do metadado
     * @param valor Valor do metadado
     * @throws SQLException
     */
    public void atualizarMetadado(String chave, String valor) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO sync_metadata (key, value, updated_at) 
            VALUES (?, ?, CURRENT_TIMESTAMP)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, chave);
            stmt.setString(2, valor);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            // Se a tabela não existir ainda, apenas logar e não lançar exceção
            if (e.getMessage().contains("no such table") || e.getMessage().contains("no column named")) {
                LOGGER.fine("Tabela sync_metadata ainda não existe - ignorando atualização de metadado: " + chave);
                return;
            }
            LOGGER.log(Level.WARNING, "Erro ao atualizar metadado: " + chave, e);
            throw e;
        }
    }
    
    /**
     * Obtém metadado de sincronização
     * @param chave Chave do metadado
     * @return Valor do metadado ou null se não encontrado
     */
    public String obterMetadado(String chave) {
        String sql = "SELECT value FROM sync_metadata WHERE key = ?";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, chave);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
                return null;
            }
            
        } catch (SQLException e) {
            // Se a tabela não existir ainda, retornar null silenciosamente
            if (e.getMessage().contains("no such table") || e.getMessage().contains("no column named")) {
                LOGGER.fine("Tabela sync_metadata ainda não existe - retornando null para chave: " + chave);
                return null;
            }
            LOGGER.log(Level.WARNING, "Erro ao obter metadado: " + chave, e);
            return null;
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Define parâmetros para patrimônio
     * @param stmt PreparedStatement
     * @param patrimonio Dados do patrimônio
     * @throws SQLException
     */
    private void setPatrimonioParameters(PreparedStatement stmt, Map<String, Object> patrimonio) throws SQLException {
        stmt.setObject(1, patrimonio.get("id"));
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
    }
    
    /**
     * Define parâmetros para coleta
     * @param stmt PreparedStatement
     * @param coleta Dados da coleta
     * @throws SQLException
     */
    private void setColetaParameters(PreparedStatement stmt, Map<String, Object> coleta) throws SQLException {
        stmt.setObject(1, coleta.get("id_patrimonio"));
        stmt.setObject(2, coleta.get("id_inventario"));
        stmt.setObject(3, coleta.get("id_participante"));
        stmt.setString(4, (String) coleta.get("numero_patrimonio"));
        stmt.setTimestamp(5, (Timestamp) coleta.get("data_coleta"));
        stmt.setString(6, (String) coleta.get("localizacao_atual"));
        stmt.setString(7, (String) coleta.get("localizacao_encontrada"));
        stmt.setString(8, (String) coleta.get("situacao_encontrada"));
        stmt.setString(9, (String) coleta.get("observacoes"));
        stmt.setString(10, (String) coleta.get("foto_patrimonio"));
        stmt.setBoolean(11, (Boolean) coleta.getOrDefault("sem_etiqueta", false));
        stmt.setString(12, (String) coleta.get("descricao_sem_etiqueta"));
    }
    
    /**
     * Converte ResultSet para lista de mapas
     * @param rs ResultSet
     * @return Lista de mapas
     * @throws SQLException
     */
    private List<Map<String, Object>> resultSetToMapList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnType = metaData.getColumnTypeName(i);
                
                // ✅ CORRIGIDO: Tratamento especial para timestamps do SQLite
                // Mas NÃO converter campos que são JSON ou TEXT
                if (columnType != null && 
                    (columnType.equalsIgnoreCase("DATETIME") || columnType.equalsIgnoreCase("TIMESTAMP")) &&
                    !columnName.toLowerCase().contains("json") &&
                    !columnName.toLowerCase().contains("data_json")) {
                    // Usar método seguro para ler timestamps
                    row.put(columnName, lerTimestampSeguro(rs, columnName));
                } else if (columnName.toLowerCase().contains("timestamp") || 
                           (columnName.toLowerCase().startsWith("data_") && 
                            !columnName.toLowerCase().contains("json"))) {
                    // Campos com nome sugestivo de data, mas verificar se não é JSON
                    try {
                        row.put(columnName, lerTimestampSeguro(rs, columnName));
                    } catch (Exception e) {
                        // Se falhar, usar como objeto normal
                        row.put(columnName, rs.getObject(i));
                    }
                } else {
                    row.put(columnName, rs.getObject(i));
                }
            }
            result.add(row);
        }
        
        return result;
    }
    
    /**
     * Converte mapa para JSON simples
     * @param map Mapa de dados
     * @return String JSON
     */
    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else {
                json.append(value.toString());
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    

    
    // ==================== OPERAÇÕES DE COLETA OFFLINE ====================
    
    /**
     * Salva uma coleta no banco offline (SQLite)
     * @param coleta Dados da coleta
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarColetaOffline(Map<String, Object> coleta) throws SQLException {
        System.out.println("=== DEBUG TIMESTAMP: OfflineDAO.salvarColetaOffline ===");
        
        String sql = """
            INSERT INTO local_coleta 
            (id_inventario, id_patrimonio, id_participante, numero_patrimonio,
             descricao_sem_etiqueta, localizacao_encontrada, estado_encontrado,
             observacoes, data_coleta, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) coleta.get("id_inventario"));
            stmt.setObject(2, coleta.get("id_patrimonio"));
            stmt.setObject(3, coleta.get("id_participante"));
            stmt.setString(4, (String) coleta.get("numero_patrimonio"));
            // ✅ CORRIGIDO: Aceita tanto "descricao_sem_etiqueta" quanto "descricao_item_sem_etiqueta"
            String descricaoSemEtiqueta = (String) coleta.get("descricao_sem_etiqueta");
            if (descricaoSemEtiqueta == null) {
                descricaoSemEtiqueta = (String) coleta.get("descricao_item_sem_etiqueta");
            }
            stmt.setString(5, descricaoSemEtiqueta);
            stmt.setString(6, (String) coleta.get("localizacao_encontrada"));
            stmt.setString(7, (String) coleta.get("situacao_encontrada")); // ✅ CORRIGIDO - campo correto do SQLite
            stmt.setString(8, (String) coleta.get("observacoes"));
            
            // DEBUG: Log detalhado do timestamp antes de setar no PreparedStatement
            Object dataColetaObj = coleta.get("data_coleta");
            System.out.println("DEBUG TIMESTAMP: Objeto data_coleta do Map: " + dataColetaObj);
            System.out.println("DEBUG TIMESTAMP: Classe do objeto: " + 
                (dataColetaObj != null ? dataColetaObj.getClass().getName() : "null"));
            
            if (dataColetaObj instanceof Timestamp) {
                Timestamp ts = (Timestamp) dataColetaObj;
                System.out.println("DEBUG TIMESTAMP: É um Timestamp válido");
                System.out.println("DEBUG TIMESTAMP: Timestamp.toString(): " + ts.toString());
                System.out.println("DEBUG TIMESTAMP: Timestamp.getTime(): " + ts.getTime());
                System.out.println("DEBUG TIMESTAMP: Timestamp.getNanos(): " + ts.getNanos());
                
                stmt.setTimestamp(9, ts);
                System.out.println("DEBUG TIMESTAMP: Timestamp setado no PreparedStatement (posição 9)");
            } else {
                System.err.println("DEBUG TIMESTAMP: ERRO - Objeto não é um Timestamp!");
                System.err.println("DEBUG TIMESTAMP: Tentando cast forçado...");
                stmt.setTimestamp(9, (Timestamp) dataColetaObj);
            }
            
            stmt.setString(10, (String) coleta.getOrDefault("sync_status", "PENDING")); // Corrigido
            
            System.out.println("DEBUG TIMESTAMP: Executando INSERT no SQLite...");
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG TIMESTAMP: INSERT executado - Linhas afetadas: " + rowsAffected);
            
            if (rowsAffected > 0) {
                // Usar last_insert_rowid() do SQLite
                try (var stmtId = conn.createStatement();
                     var rs = stmtId.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        System.out.println("DEBUG TIMESTAMP: Coleta salva com ID: " + id);
                        
                        // DEBUG: Ler de volta o registro para verificar o timestamp
                        String sqlVerifica = "SELECT data_coleta FROM local_coleta WHERE id = ?";
                        try (PreparedStatement stmtVerifica = conn.prepareStatement(sqlVerifica)) {
                            stmtVerifica.setInt(1, id);
                            try (ResultSet rsVerifica = stmtVerifica.executeQuery()) {
                                if (rsVerifica.next()) {
                                    Timestamp tsLido = rsVerifica.getTimestamp("data_coleta");
                                    System.out.println("DEBUG TIMESTAMP: Timestamp lido do banco: " + tsLido);
                                    System.out.println("DEBUG TIMESTAMP: Timestamp lido (class): " + 
                                        (tsLido != null ? tsLido.getClass().getName() : "null"));
                                    System.out.println("DEBUG TIMESTAMP: Timestamp lido (time): " + 
                                        (tsLido != null ? tsLido.getTime() : "null"));
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("DEBUG TIMESTAMP: Erro ao verificar timestamp salvo: " + e.getMessage());
                        }
                        
                        LOGGER.info("Coleta salva offline - ID: " + id);
                        return id;
                    }
                }
            }
            
            throw new SQLException("Falha ao salvar coleta offline");
        } catch (SQLException e) {
            System.err.println("=== DEBUG TIMESTAMP: SQLException em salvarColetaOffline ===");
            System.err.println("DEBUG TIMESTAMP: Mensagem: " + e.getMessage());
            System.err.println("DEBUG TIMESTAMP: SQLState: " + e.getSQLState());
            System.err.println("DEBUG TIMESTAMP: ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Busca coletas pendentes de sincronização
     * @return Lista de coletas pendentes
     * @throws SQLException
     */
    public List<Map<String, Object>> buscarColetasPendentes() throws SQLException {
        String sql = """
            SELECT * FROM local_coleta 
            WHERE sync_status = 'PENDING'
            ORDER BY data_coleta ASC
        """;
        
        List<Map<String, Object>> coletas = new ArrayList<>();
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> coleta = new HashMap<>();
                coleta.put("id", rs.getInt("id"));
                coleta.put("id_inventario", rs.getInt("id_inventario"));
                coleta.put("id_patrimonio", rs.getObject("id_patrimonio"));
                coleta.put("id_participante", rs.getObject("id_participante"));
                coleta.put("numero_patrimonio", rs.getString("numero_patrimonio"));
                // ✅ CORRIGIDO: Nome correto da coluna no SQLite é "descricao_sem_etiqueta"
                coleta.put("descricao_sem_etiqueta", rs.getString("descricao_sem_etiqueta"));
                coleta.put("localizacao_encontrada", rs.getString("localizacao_encontrada"));
                // ✅ CORRIGIDO: Nome correto da coluna no SQLite é "situacao_encontrada"
                coleta.put("situacao_encontrada", rs.getString("situacao_encontrada"));
                coleta.put("observacoes", rs.getString("observacoes"));
                
                // ✅ CORREÇÃO: Ler timestamp de forma segura do SQLite
                coleta.put("data_coleta", lerTimestampSeguro(rs, "data_coleta"));
                coletas.add(coleta);
            }
        }
        
        LOGGER.info("Encontradas " + coletas.size() + " coletas pendentes");
        return coletas;
    }
    
    /**
     * Marca uma coleta como sincronizada
     * @param idColeta ID da coleta
     * @throws SQLException
     */
    public void marcarColetaSincronizada(int idColeta) throws SQLException {
        String sql = """
            UPDATE local_coleta 
            SET sync_status = 'SYNCED',
                last_modified = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idColeta);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Coleta marcada como sincronizada - ID: " + idColeta);
            }
        }
    }
    
    /**
     * Verifica se uma coleta já existe
     * @param idInventario ID do inventário
     * @param idPatrimonio ID do patrimônio
     * @return true se existe
     * @throws SQLException
     */
    public boolean coletaExiste(int idInventario, int idPatrimonio) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM local_coleta 
            WHERE id_inventario = ? AND id_patrimonio = ?
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInventario);
            stmt.setInt(2, idPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    // ==================== MÉTODOS PÚBLICOS DE ACESSO ====================
    
    /**
     * Obtém a conexão SQLite para operações diretas
     * Útil para queries customizadas e operações avançadas
     * 
     * @return Conexão SQLite ativa
     * @throws SQLException Se houver erro ao obter conexão
     */
    public Connection getConnection() throws SQLException {
        return sqliteConnection.getConnection();
    }
    
    /**
     * Define um metadado do sistema
     * 
     * @param key Chave do metadado
     * @param value Valor do metadado
     * @return true se atualizado com sucesso
     */
    public boolean definirMetadado(String key, String value) {
        String sql = """
            INSERT INTO sync_metadata (key, value, updated_at) 
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(key) DO UPDATE SET 
                value = excluded.value,
                updated_at = CURRENT_TIMESTAMP
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, key);
            stmt.setString(2, value);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao definir metadado: " + key, e);
            return false;
        }
    }
    
    /**
     * Atualiza o status de sincronização de uma entidade
     * 
     * @param tableName Nome da tabela
     * @param recordId ID do registro
     * @param syncStatus Novo status (PENDING, SYNCED, CONFLICT)
     * @return true se atualizado com sucesso
     */
    public boolean atualizarStatusSync(String tableName, int recordId, String syncStatus) {
        String sql = String.format(
            "UPDATE %s SET sync_status = ?, last_modified = CURRENT_TIMESTAMP WHERE id = ?",
            tableName
        );
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, syncStatus);
            stmt.setInt(2, recordId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.fine(String.format("Status atualizado: %s #%d -> %s", tableName, recordId, syncStatus));
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao atualizar status de sync", e);
            return false;
        }
    }
    
    // ==================== OPERAÇÕES DE SALA ====================
    
    /**
     * Salva uma sala no banco local
     * @param sala Dados da sala
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarSala(Map<String, Object> sala) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_sala 
            (id, nome, descricao, bloco, andar, capacidade, tipo, ativa)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Tratamento seguro de valores NULL
            stmt.setObject(1, sala.get("id"));
            
            // Nome (pode ser NULL)
            Object nome = sala.get("nome");
            stmt.setString(2, nome != null ? String.valueOf(nome) : null);
            
            // Descrição (pode ser NULL)
            Object descricao = sala.get("descricao");
            stmt.setString(3, descricao != null ? String.valueOf(descricao) : null);
            
            // Bloco (pode ser NULL)
            Object bloco = sala.get("bloco");
            stmt.setString(4, bloco != null ? String.valueOf(bloco) : null);
            
            // Andar (pode ser NULL)
            Object andar = sala.get("andar");
            stmt.setString(5, andar != null ? String.valueOf(andar) : null);
            
            // Capacidade (pode ser NULL)
            stmt.setObject(6, sala.get("capacidade"));
            
            // Tipo (pode ser NULL)
            Object tipo = sala.get("tipo");
            stmt.setString(7, tipo != null ? String.valueOf(tipo) : null);
            
            // Ativa (padrão true se NULL)
            Object ativa = sala.get("ativa");
            if (ativa instanceof Boolean) {
                stmt.setBoolean(8, (Boolean) ativa);
            } else {
                stmt.setBoolean(8, true); // Padrão: ativa
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.fine("Sala salva offline - ID: " + sala.get("id"));
                return (Integer) sala.get("id");
            }
            
            throw new SQLException("Falha ao salvar sala offline");
            
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERRO ao salvar sala no SQLite:");
            System.err.println(">>>   ID: " + sala.get("id"));
            System.err.println(">>>   Nome: " + sala.get("nome"));
            System.err.println(">>>   Descrição: " + sala.get("descricao"));
            System.err.println(">>>   Bloco: " + sala.get("bloco"));
            System.err.println(">>>   Andar: " + sala.get("andar"));
            System.err.println(">>>   Tipo: " + sala.get("tipo"));
            System.err.println(">>>   Erro: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao salvar sala offline", e);
            throw e;
        }
    }
    
    /**
     * Lista todas as salas
     * @return Lista de salas
     * @throws SQLException
     */
    public List<Map<String, Object>> listarSalas() throws SQLException {
        String sql = "SELECT * FROM local_sala WHERE ativa = 1 ORDER BY nome";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return resultSetToMapList(rs);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar salas offline", e);
            throw e;
        }
    }
    
    // ==================== OPERAÇÕES DE RESPONSÁVEL ====================
    
    /**
     * Salva um responsável no banco local
     * @param responsavel Dados do responsável
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarResponsavel(Map<String, Object> responsavel) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_responsavel 
            (id, nome, cpf, matricula, email, telefone, cargo, setor, ativo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, responsavel.get("id"));
            stmt.setString(2, (String) responsavel.get("nome"));
            stmt.setString(3, (String) responsavel.get("cpf"));
            stmt.setString(4, (String) responsavel.get("matricula"));
            stmt.setString(5, (String) responsavel.get("email"));
            stmt.setString(6, (String) responsavel.get("telefone"));
            stmt.setString(7, (String) responsavel.get("cargo"));
            stmt.setString(8, (String) responsavel.get("setor"));
            stmt.setBoolean(9, (Boolean) responsavel.getOrDefault("ativo", true));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.fine("Responsável salvo offline - ID: " + responsavel.get("id"));
                return (Integer) responsavel.get("id");
            }
            
            throw new SQLException("Falha ao salvar responsável offline");
            
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERRO ao salvar responsável no SQLite:");
            System.err.println(">>>   ID: " + responsavel.get("id"));
            System.err.println(">>>   Nome: " + responsavel.get("nome"));
            System.err.println(">>>   Erro: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao salvar responsável offline", e);
            throw e;
        }
    }
    
    /**
     * Lista todos os responsáveis
     * @return Lista de responsáveis
     * @throws SQLException
     */
    public List<Map<String, Object>> listarResponsaveis() throws SQLException {
        String sql = "SELECT * FROM local_responsavel WHERE ativo = 1 ORDER BY nome";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return resultSetToMapList(rs);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar responsáveis offline", e);
            throw e;
        }
    }
    
    // ==================== OPERAÇÕES DE USUÁRIO ====================
    
    /**
     * Salva um usuário no banco local (para login offline)
     * @param usuario Dados do usuário
     * @return ID gerado
     * @throws SQLException
     */
    public int salvarUsuario(Map<String, Object> usuario) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO local_usuario 
            (id, login, senha_hash, nome_completo, email, perfil, ativo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, usuario.get("id"));
            stmt.setString(2, (String) usuario.get("login"));
            stmt.setString(3, (String) usuario.get("senha_hash"));
            // ✅ CORRIGIDO: Aceita tanto "nome" quanto "nome_completo"
            String nomeCompleto = (String) usuario.get("nome_completo");
            if (nomeCompleto == null) {
                nomeCompleto = (String) usuario.get("nome"); // Fallback para compatibilidade
            }
            stmt.setString(4, nomeCompleto);
            stmt.setString(5, (String) usuario.get("email"));
            stmt.setString(6, (String) usuario.get("perfil"));
            stmt.setBoolean(7, (Boolean) usuario.getOrDefault("ativo", true));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println(">>> ✅ Usuário salvo no SQLite - ID: " + usuario.get("id") + ", Login: " + usuario.get("login"));
                LOGGER.fine("Usuário salvo offline - ID: " + usuario.get("id"));
                return (Integer) usuario.get("id");
            }
            
            throw new SQLException("Falha ao salvar usuário offline");
            
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERRO ao salvar usuário no SQLite: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Erro ao salvar usuário offline", e);
            throw e;
        }
    }
    
    /**
     * Busca usuário por login (para autenticação offline)
     * @param login Login do usuário
     * @return Dados do usuário ou null se não encontrado
     * @throws SQLException
     */
    public Map<String, Object> buscarUsuarioPorLogin(String login) throws SQLException {
        String sql = "SELECT * FROM local_usuario WHERE login = ? AND ativo = 1";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("id", rs.getInt("id"));
                    usuario.put("login", rs.getString("login"));
                    usuario.put("senha_hash", rs.getString("senha_hash"));
                    usuario.put("nome", rs.getString("nome"));
                    usuario.put("email", rs.getString("email"));
                    usuario.put("perfil", rs.getString("perfil"));
                    usuario.put("ativo", rs.getBoolean("ativo"));
                    return usuario;
                }
                return null;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar usuário offline", e);
            throw e;
        }
    }
    
    // ==================== ESTATÍSTICAS ====================
    
    /**
     * Obtém estatísticas do banco offline
     * @return Mapa com estatísticas
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = sqliteConnection.getConnection()) {
            
            // Contar patrimônios
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_patrimonio")) {
                if (rs.next()) {
                    stats.put("total_patrimonios", rs.getInt(1));
                }
            }
            
            // Contar salas
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_sala")) {
                if (rs.next()) {
                    stats.put("total_salas", rs.getInt(1));
                }
            }
            
            // Contar responsáveis
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_responsavel")) {
                if (rs.next()) {
                    stats.put("total_responsaveis", rs.getInt(1));
                }
            }
            
            // Contar usuários
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_usuario")) {
                if (rs.next()) {
                    stats.put("total_usuarios", rs.getInt(1));
                }
            }
            
            // Contar coletas
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_coleta")) {
                if (rs.next()) {
                    stats.put("total_coletas", rs.getInt(1));
                }
            }
            
            // Contar coletas pendentes de sincronização
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_coleta WHERE sync_status = 'PENDING'")) {
                if (rs.next()) {
                    stats.put("coletas_pendentes", rs.getInt(1));
                }
            }
            
            // Contar inventários
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_inventario")) {
                if (rs.next()) {
                    stats.put("total_inventarios", rs.getInt(1));
                }
            }
            
            LOGGER.fine("Estatísticas obtidas: " + stats);
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao obter estatísticas", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    // ==================== BUSCA DE COLETAS POR LOCALIZAÇÃO ====================
    
    /**
     * Busca coletas por localização (para exibir histórico offline)
     * Busca tanto coletas pendentes quanto já sincronizadas
     * 
     * @param localizacao Localização encontrada (número/nome da sala)
     * @return Lista de coletas na localização
     * @throws SQLException
     */
    public List<Map<String, Object>> buscarColetasPorLocalizacao(String localizacao) throws SQLException {
        String sql = """
            SELECT lc.*, lp.descricao as descricao_patrimonio
            FROM local_coleta lc
            LEFT JOIN local_patrimonio lp ON lc.id_patrimonio = lp.id
            WHERE lc.localizacao_encontrada = ?
            ORDER BY lc.data_coleta DESC
            LIMIT 100
        """;
        
        List<Map<String, Object>> coletas = new ArrayList<>();
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, localizacao);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> coleta = new HashMap<>();
                    coleta.put("id", rs.getInt("id"));
                    coleta.put("id_inventario", rs.getInt("id_inventario"));
                    coleta.put("id_patrimonio", rs.getObject("id_patrimonio"));
                    coleta.put("id_participante", rs.getObject("id_participante"));
                    coleta.put("numero_patrimonio", rs.getString("numero_patrimonio"));
                    coleta.put("descricao_patrimonio", rs.getString("descricao_patrimonio"));
                    coleta.put("descricao_sem_etiqueta", rs.getString("descricao_sem_etiqueta"));
                    coleta.put("localizacao_encontrada", rs.getString("localizacao_encontrada"));
                    coleta.put("situacao_encontrada", rs.getString("situacao_encontrada"));
                    coleta.put("observacoes", rs.getString("observacoes"));
                    coleta.put("sync_status", rs.getString("sync_status"));
                    
                    // Verificar se é item sem etiqueta
                    Object idPatrimonio = rs.getObject("id_patrimonio");
                    boolean semEtiqueta = idPatrimonio == null || 
                                         (idPatrimonio instanceof Number && ((Number) idPatrimonio).intValue() == 0);
                    coleta.put("sem_etiqueta", semEtiqueta);
                    
                    // Ler timestamp de forma segura
                    coleta.put("data_coleta", lerTimestampSeguro(rs, "data_coleta"));
                    
                    coletas.add(coleta);
                }
            }
            
            LOGGER.info("Encontradas " + coletas.size() + " coletas para localização: " + localizacao);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar coletas por localização offline", e);
            throw e;
        }
        
        return coletas;
    }
    
    /**
     * Conta coletas por localização (para estatísticas)
     * 
     * @param localizacao Localização encontrada
     * @return Total de coletas na localização
     */
    public int contarColetasPorLocalizacao(String localizacao) {
        String sql = "SELECT COUNT(*) FROM local_coleta WHERE localizacao_encontrada = ?";
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, localizacao);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao contar coletas por localização: " + e.getMessage());
        }
        
        return 0;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Lê um timestamp do ResultSet de forma segura para SQLite
     * SQLite não tem tipo nativo de timestamp, então pode armazenar como:
     * - String no formato "yyyy-MM-dd HH:mm:ss"
     * - Long (milissegundos desde epoch)
     * - Ou formato ISO 8601
     * 
     * @param rs ResultSet
     * @param columnName Nome da coluna
     * @return Timestamp ou null se não conseguir converter
     */
    private Timestamp lerTimestampSeguro(ResultSet rs, String columnName) {
        try {
            // Primeiro, tentar ler como string (mais comum no SQLite)
            String strValue = rs.getString(columnName);
            
            if (strValue == null || strValue.isEmpty()) {
                return null;
            }
            
            // Tentar diferentes formatos
            
            // 1. Formato padrão JDBC: yyyy-mm-dd hh:mm:ss[.fffffffff]
            try {
                return Timestamp.valueOf(strValue);
            } catch (IllegalArgumentException e1) {
                // Não é formato padrão, tentar outros
            }
            
            // 2. Formato ISO 8601 com T: yyyy-MM-ddTHH:mm:ss
            if (strValue.contains("T")) {
                try {
                    String normalized = strValue.replace("T", " ");
                    // Remover timezone se existir
                    if (normalized.contains("+")) {
                        normalized = normalized.substring(0, normalized.indexOf("+"));
                    }
                    if (normalized.contains("Z")) {
                        normalized = normalized.replace("Z", "");
                    }
                    return Timestamp.valueOf(normalized);
                } catch (IllegalArgumentException e2) {
                    // Continuar tentando
                }
            }
            
            // 3. Tentar como número (milissegundos desde epoch)
            try {
                long millis = Long.parseLong(strValue);
                return new Timestamp(millis);
            } catch (NumberFormatException e3) {
                // Não é número
            }
            
            // 4. Formato brasileiro: dd/MM/yyyy HH:mm:ss
            if (strValue.contains("/")) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    java.util.Date date = sdf.parse(strValue);
                    return new Timestamp(date.getTime());
                } catch (java.text.ParseException e4) {
                    // Continuar tentando
                }
            }
            
            // 5. Último recurso: usar data atual
            LOGGER.warning("Não foi possível converter timestamp: " + strValue + " - usando data atual");
            return new Timestamp(System.currentTimeMillis());
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao ler timestamp da coluna " + columnName, e);
            return new Timestamp(System.currentTimeMillis());
        }
    }
}
