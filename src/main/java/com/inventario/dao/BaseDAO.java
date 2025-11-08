package com.inventario.dao;

import com.inventario.util.ConnectionManager;

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
     * Insere uma nova entidade no banco de dados
     */
    public void insert(T entity) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
                setInsertParameters(stmt, entity);
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        setGeneratedId(entity, rs.getInt(1));
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }
    
    /**
     * Atualiza uma entidade existente
     */
    public void update(T entity) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(getUpdateSQL())) {
                setUpdateParameters(stmt, entity);
                stmt.executeUpdate();
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }
    
    /**
     * Exclui uma entidade por ID
     */
    public void delete(ID id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE ID = ?";
        executeUpdate(sql, id);
    }
    
    /**
     * Busca uma entidade por ID
     */
    public T findById(ID id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ID = ?";
        return executeQuerySingle(sql, id);
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
     * Executa uma query e retorna uma única entidade
     */
    protected T executeQuerySingle(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs);
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
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
     */
    protected <R> R executeScalar(String sql, Class<R> resultType, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getObject(1, resultType);
                    }
                }
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
        
        return null;
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
