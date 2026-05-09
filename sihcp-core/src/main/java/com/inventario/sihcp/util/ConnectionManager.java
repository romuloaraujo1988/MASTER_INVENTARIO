package com.inventario.sihcp.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gerenciador centralizado de conexões com banco de dados
 * Usa HikariCP para connection pooling eficiente
 * Elimina código de conexão espalhado
 * 
 * v2.0: Usa SLF4J ao invés de System.out
 */
public class ConnectionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    
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
            
            // Configurações de pool - OTIMIZADO para alta concorrência
            config.setMaximumPoolSize(30);  // Aumentado de 5 para 30 para suportar mais coletores simultâneos
            config.setMinimumIdle(5);       // Aumentado de 1 para 5 para ter conexões prontas
            config.setConnectionTimeout(30000); // 30 segundos
            config.setIdleTimeout(600000);  // 10 minutos
            config.setMaxLifetime(1800000); // 30 minutos
            
            // Configurações de validação
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(3000);
            
            // Configurações de performance - REDUZIDO cache
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "100");  // Era: 250
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024"); // Era: 2048
            
            dataSource = new HikariDataSource(config);
            initialized = true;
            
            logger.info("✓ Connection pool inicializado com sucesso");
            
        } catch (Exception e) {
            logger.error("✗ Erro ao inicializar connection pool: {}", e.getMessage());
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
            logger.error("Erro ao obter conexão do pool: {}", e.getMessage());
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
                logger.warn("Erro ao fechar conexão: {}", e.getMessage());
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
            logger.info("✓ Connection pool fechado");
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
            logger.warn("Erro ao testar conexão: {}", e.getMessage());
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
