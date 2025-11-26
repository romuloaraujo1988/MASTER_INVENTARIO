package com.inventario.config;

import com.inventario.util.ConnectionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Configuração do Connection Pool usando HikariCP
 * 
 * Singleton thread-safe que gerencia o pool de conexões com PostgreSQL.
 * Otimizado para performance com reutilização de conexões.
 * Integra com ConnectionManager existente.
 * 
 * @see Requirements 10.1, 10.2, 10.5
 */
public class HikariConnectionPool {
    
    private static final Logger logger = LoggerFactory.getLogger(HikariConnectionPool.class);
    private static HikariConnectionPool instance;
    private HikariDataSource dataSource;
    
    // Configurações otimizadas do pool
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 5;
    private static final long CONNECTION_TIMEOUT = 5000; // 5 segundos
    private static final long IDLE_TIMEOUT = 300000; // 5 minutos
    private static final long MAX_LIFETIME = 600000; // 10 minutos
    private static final long LEAK_DETECTION_THRESHOLD = 60000; // 1 minuto
    
    /**
     * Construtor privado - Singleton
     */
    private HikariConnectionPool() {
        // Não inicializa automaticamente - usa ConnectionManager existente
    }
    
    /**
     * Obtém instância singleton do HikariConnectionPool
     * Thread-safe usando double-checked locking
     */
    public static synchronized HikariConnectionPool getInstance() {
        if (instance == null) {
            instance = new HikariConnectionPool();
        }
        return instance;
    }
    
    /**
     * Inicializa o HikariCP DataSource com configurações otimizadas
     * 
     * @param url URL JDBC
     * @param username Nome de usuário
     * @param password Senha
     */
    public synchronized void initialize(String url, String username, String password) {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("HikariCP já está inicializado");
            return;
        }
        
        try {
            logger.info("Inicializando HikariCP Connection Pool...");
            
            // Configurar HikariCP
            HikariConfig config = new HikariConfig();
            
            // Configurações de conexão
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            
            // Configurações do pool
            config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
            config.setMinimumIdle(MINIMUM_IDLE);
            config.setConnectionTimeout(CONNECTION_TIMEOUT);
            config.setIdleTimeout(IDLE_TIMEOUT);
            config.setMaxLifetime(MAX_LIFETIME);
            config.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
            
            // Configurações de performance
            config.setAutoCommit(true);
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("InventarioHikariPool");
            
            // Propriedades adicionais do PostgreSQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            // Criar DataSource
            dataSource = new HikariDataSource(config);
            
            logger.info("HikariCP Connection Pool inicializado com sucesso");
            logger.info("Pool configurado: maxPoolSize={}, minIdle={}, connectionTimeout={}ms",
                    MAXIMUM_POOL_SIZE, MINIMUM_IDLE, CONNECTION_TIMEOUT);
            
        } catch (Exception e) {
            logger.error("Erro ao inicializar HikariCP Connection Pool", e);
            throw new RuntimeException("Falha ao inicializar connection pool", e);
        }
    }
    
    /**
     * Obtém o DataSource configurado
     * 
     * @return DataSource do HikariCP
     */
    public DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("DataSource não está inicializado ou foi fechado");
        }
        return dataSource;
    }
    
    /**
     * Obtém estatísticas do pool de conexões
     * 
     * @return String com estatísticas formatadas
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "Pool não inicializado";
        }
        
        return String.format(
            "HikariCP Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * Fecha o DataSource e libera recursos
     * Deve ser chamado ao encerrar a aplicação
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Fechando HikariCP Connection Pool...");
            logger.info("Estatísticas finais: {}", getPoolStats());
            dataSource.close();
            logger.info("HikariCP Connection Pool fechado com sucesso");
        }
    }
    
    /**
     * Verifica se o pool está saudável
     * 
     * @return true se o pool está operacional
     */
    public boolean isHealthy() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                return false;
            }
            
            // Tentar obter uma conexão para verificar saúde
            try (var conn = dataSource.getConnection()) {
                return conn.isValid(1); // Timeout de 1 segundo
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar saúde do pool", e);
            return false;
        }
    }
    
    /**
     * Verifica se o pool está inicializado
     * 
     * @return true se inicializado
     */
    public boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * Registra shutdown hook para fechar pool ao encerrar aplicação
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HikariConnectionPool pool = HikariConnectionPool.getInstance();
            if (pool != null && pool.isInitialized()) {
                pool.close();
            }
            ConnectionManager.shutdown();
        }));
    }
}
