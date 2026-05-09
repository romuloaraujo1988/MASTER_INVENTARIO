package com.inventario.sihcp.dao;

import com.inventario.sihcp.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe base genérica para DAOs
 * Elimina código duplicado fornecendo operações CRUD padrão
 * 
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador (geralmente Integer ou Long)
 */
public abstract class BaseDAO<T, ID> {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    
    // ========== Métodos Abstratos (Subclasses devem implementar) ==========
    
    /**
     * Retorna o nome da tabela no banco de dados
     */
    protected abstract String getTableName();
    
    /**
     * Mapeia um ResultSet para uma entidade
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    /**
     * Define os parâmetros para INSERT
     */
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Define os parâmetros para UPDATE
     */
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Retorna o SQL para INSERT
     */
    protected abstract String getInsertSQL();
    
    /**
     * Retorna o SQL para UPDATE
     */
    protected abstract String getUpdateSQL();
    
    /**
     * Define o ID gerado após INSERT
     */
    protected abstract void setGeneratedId(T entity, int id);
    
    // ========== Operações CRUD Genéricas ==========
    
    /**
     * Insere uma nova entidade no banco de dados usando uma nova conexão.
     */
    public void insert(T entity) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            insert(entity, conn);
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    /**
     * Insere uma nova entidade usando uma conexão existente.
     * Útil para transações e processamento em lote.
     */
    public void insert(T entity, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, entity);
            stmt.executeUpdate();
            
            logger.info("Registro inserido com sucesso na tabela: {}", getTableName());
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    setGeneratedId(entity, rs.getInt(1));
                }
            }
        }
    }
    
    /**
     * Atualiza uma entidade existente usando uma nova conexão.
     */
    public void update(T entity) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            boolean autoCommit = conn.getAutoCommit();
            update(entity, conn);
            
            // Forçar commit se autoCommit estiver desligado
            if (!autoCommit) {
                conn.commit();
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    /**
     * Atualiza uma entidade existente usando uma conexão existente.
     */
    public void update(T entity, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(getUpdateSQL())) {
            setUpdateParameters(stmt, entity);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Registro atualizado com sucesso na tabela: {}", getTableName());
            }
        }
    }
    
    /**
     * Exclui uma entidade por ID
     */
    public void delete(ID id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE ID = ?";
        int rows = executeUpdate(sql, id);
        if (rows > 0) {
            logger.info("Registro deletado com sucesso (ID: {}) na tabela: {}", id, getTableName());
        }
    }
    
    /**
     * Busca uma entidade por ID usando uma nova conexão.
     */
    public T findById(ID id) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return findById(id, conn);
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    /**
     * Busca uma entidade por ID usando uma conexão existente.
     */
    public T findById(ID id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ID = ?";
        return executeQuerySingle(conn, sql, id);
    }
    
    /**
     * Lista todas as entidades
     */
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        return executeQuery(sql);
    }
    
    /**
     * Lista todas as entidades com ordenação
     */
    public List<T> findAll(String orderBy) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY " + orderBy;
        return executeQuery(sql);
    }
    
    /**
     * Conta o total de registros
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return 0;
    }
    
    /**
     * Verifica se existe uma entidade com o ID
     */
    public boolean exists(ID id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE ID = ?";
        
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return false;
    }
    
    // ========== Métodos Utilitários Protegidos ==========
    
    /**
     * Executa uma query e retorna lista de entidades
     */
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToEntity(rs));
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return results;
    }
    
    /**
     * Executa uma query e retorna lista de entidades usando uma conexão existente.
     */
    protected List<T> executeQuery(Connection conn, String sql, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
        }
        return results;
    }
    
    /**
     * Executa uma query e retorna uma única entidade usando uma nova conexão.
     */
    protected T executeQuerySingle(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return executeQuerySingle(conn, sql, params);
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    /**
     * Executa uma query e retorna uma única entidade usando uma conexão existente.
     */
    protected T executeQuerySingle(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Executa um UPDATE/DELETE e retorna número de linhas afetadas
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                return stmt.executeUpdate();
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }
    
    /**
     * Executa uma query e retorna um valor escalar (COUNT, SUM, etc)
     * 
     * CORREÇÃO: rs.getObject(1, Class) falha no PostgreSQL para conversões
     * de int8 → Integer. Usamos rs.getObject(1) e convertemos manualmente.
     */
    @SuppressWarnings("unchecked")
    protected <R> R executeScalar(String sql, Class<R> resultType, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Object value = rs.getObject(1);
                        if (value == null) return null;
                        return convertScalar(value, resultType);
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Converte um valor escalar retornado pelo JDBC para o tipo esperado.
     * Necessário porque o PostgreSQL retorna COUNT(*) como Long (int8),
     * mas muitos DAOs esperam Integer.
     */
    @SuppressWarnings("unchecked")
    private <R> R convertScalar(Object value, Class<R> resultType) {
        if (resultType.isInstance(value)) {
            return resultType.cast(value);
        }
        if (resultType == Integer.class) {
            if (value instanceof Number) return (R) Integer.valueOf(((Number) value).intValue());
        }
        if (resultType == Long.class) {
            if (value instanceof Number) return (R) Long.valueOf(((Number) value).longValue());
        }
        if (resultType == Double.class) {
            if (value instanceof Number) return (R) Double.valueOf(((Number) value).doubleValue());
        }
        if (resultType == Float.class) {
            if (value instanceof Number) return (R) Float.valueOf(((Number) value).floatValue());
        }
        if (resultType == Boolean.class) {
            if (value instanceof Boolean) return (R) value;
            if (value instanceof Number) return (R) Boolean.valueOf(((Number) value).intValue() != 0);
        }
        if (resultType == String.class) {
            return (R) value.toString();
        }
        // Fallback: deixar o driver tentar
        return resultType.cast(value);
    }
    
    /**
     * Define parâmetros no PreparedStatement
     */
    protected void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            
            if (param == null) {
                stmt.setNull(i + 1, Types.NULL);
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }
    
    /**
     * Executa uma operação em transação
     */
    protected <R> R executeInTransaction(TransactionCallback<R> callback) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            try {
                R result = callback.execute(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                ConnectionManager.closeConnection(conn);
            }
        }
    }
    
    /**
     * Interface funcional para callbacks de transação
     */
    @FunctionalInterface
    protected interface TransactionCallback<R> {
        R execute(Connection conn) throws SQLException;
    }
}
