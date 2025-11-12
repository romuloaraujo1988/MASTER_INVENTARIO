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
            INSERT INTO local_patrimonio 
            (id, numero, descricao, descricao_resumida, marca, modelo, numero_serie, 
             situacao, valor, data_aquisicao, id_setor, id_sala, observacoes, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setPatrimonioParameters(stmt, patrimonio);
            stmt.setString(14, "PENDING");
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        registrarOperacaoSync("local_patrimonio", id, "INSERT", patrimonio);
                        return id;
                    }
                }
            }
            
            throw new SQLException("Falha ao inserir patrimônio");
            
        } catch (SQLException e) {
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
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setColetaParameters(stmt, coleta);
            stmt.setString(13, "PENDING");
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        registrarOperacaoSync("local_coleta", id, "INSERT", coleta);
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
            INSERT INTO local_inventario 
            (nome, descricao, data_inicio, data_fim, status, id_responsavel, observacoes, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = sqliteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, (String) inventario.get("nome"));
            stmt.setString(2, (String) inventario.get("descricao"));
            // Conversão segura para java.sql.Date
            Object dataInicio = inventario.get("data_inicio");
            Object dataFim = inventario.get("data_fim");
            
            stmt.setDate(3, dataInicio != null ? new java.sql.Date(((java.util.Date) dataInicio).getTime()) : null);
            stmt.setDate(4, dataFim != null ? new java.sql.Date(((java.util.Date) dataFim).getTime()) : null);
            stmt.setString(5, (String) inventario.get("status"));
            stmt.setObject(6, inventario.get("id_responsavel"));
            stmt.setString(7, (String) inventario.get("observacoes"));
            stmt.setString(8, "PENDING");
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        registrarOperacaoSync("local_inventario", id, "INSERT", inventario);
                        return id;
                    }
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
            LOGGER.log(Level.WARNING, "Erro ao atualizar metadado", e);
            throw e;
        }
    }
    
    /**
     * Obtém metadado de sincronização
     * @param chave Chave do metadado
     * @return Valor do metadado ou null se não encontrado
     * @throws SQLException
     */
    public String obterMetadado(String chave) throws SQLException {
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
            LOGGER.log(Level.WARNING, "Erro ao obter metadado", e);
            throw e;
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
                row.put(metaData.getColumnName(i), rs.getObject(i));
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
    
    /**
     * Obtém estatísticas do banco offline
     * @return Mapa com estatísticas
     * @throws SQLException
     */
    public Map<String, Object> obterEstatisticas() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = sqliteConnection.getConnection()) {
            
            // Conta registros por tabela
            String[] tabelas = {"local_patrimonio", "local_coleta", "local_inventario", "local_participante_inventario"};
            
            for (String tabela : tabelas) {
                try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tabela);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    if (rs.next()) {
                        stats.put(tabela + "_count", rs.getInt(1));
                    }
                }
            }
            
            // Conta operações pendentes
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM sync_control WHERE synced = FALSE");
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    stats.put("pending_sync_count", rs.getInt(1));
                }
            }
            
            // Última sincronização
            String lastSync = obterMetadado("last_sync_timestamp");
            stats.put("last_sync", lastSync);
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao obter estatísticas", e);
            throw e;
        }
        
        return stats;
    }
}