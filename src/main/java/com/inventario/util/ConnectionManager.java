package com.inventario.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Gerenciador centralizado de conexões com banco de dados
 * Usa HikariCP para connection pooling eficiente
 * Elimina código de conexão espalhado
 */
public class ConnectionManager {
    
    private static HikariDataSource dataSource;
    private static boolean initialized = false;
    
    /**
     * Inicializa o pool de conexões
     */
    public static synchronized void initialize(String url, String username, String password) {
        if (initialized) {
            return;
        }
        
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            
            // Configurações de pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30 segundos
            config.setIdleTimeout(600000); // 10 minutos
            config.setMaxLifetime(1800000); // 30 minutos
            
            // Configurações de validação
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
            
            // Configurações de performance
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            initialized = true;
            
            System.out.println("✓ Connection pool inicializado com sucesso");
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao inicializar connection pool: " + e.getMessage());
            throw new RuntimeException("Falha ao inicializar pool de conexões", e);
        }
    }
    
    /**
     * Obtém uma conexão do pool
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized || dataSource == null) {
            // Fallback para DatabaseConnection se não inicializado
            return DatabaseConnection.getConnection();
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Erro ao obter conexão do pool: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Fecha uma conexão (retorna ao pool)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // HikariCP retorna ao pool automaticamente
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
    
    /**
     * Fecha o pool de conexões
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            initialized = false;
            System.out.println("✓ Connection pool fechado");
        }
    }
    
    /**
     * Verifica se o pool está inicializado
     */
    public static boolean isInitialized() {
        return initialized && dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * Obtém estatísticas do pool
     */
    public static String getPoolStats() {
        if (!isInitialized()) {
            return "Pool não inicializado";
        }
        
        return String.format(
            "Pool Stats - Ativas: %d | Idle: %d | Total: %d | Aguardando: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * Testa a conexão
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém o DataSource (para uso com frameworks)
     */
    public static DataSource getDataSource() {
        if (!isInitialized()) {
            throw new IllegalStateException("Connection pool não inicializado");
        }
        return dataSource;
    }
}
