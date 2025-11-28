package com.inventario.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool de conexões HikariCP centralizado
 * 
 * PROBLEMA RESOLVIDO: Antes, cada chamada a DatabaseConnection.getConnection()
 * criava uma NOVA conexão via DriverManager, causando:
 * - Vazamento de conexões
 * - Consumo excessivo de memória (3.9GB+)
 * - Travamento do servidor
 * 
 * SOLUÇÃO: Pool de conexões com reutilização automática
 * - Máximo 15 conexões (configurável)
 * - Conexões ociosas são liberadas após 10 minutos
 * - Detecção de vazamento de conexões
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class HikariConnectionPool {
    
    private static final Logger logger = LoggerFactory.getLogger(HikariConnectionPool.class);
    
    private static volatile HikariDataSource dataSource;
    private static final Object lock = new Object();
    
    // Configurações do pool (alinhadas com application-mobile.properties)
    private static final int MAXIMUM_POOL_SIZE = 15;      // Máximo de conexões
    private static final int MINIMUM_IDLE = 5;            // Mínimo de conexões ociosas
    private static final long CONNECTION_TIMEOUT = 10000; // 10 segundos para obter conexão
    private static final long IDLE_TIMEOUT = 300000;      // 5 minutos para conexão ociosa
    private static final long MAX_LIFETIME = 900000;      // 15 minutos de vida máxima
    private static final long LEAK_DETECTION = 30000;     // Detectar vazamento após 30s
    
    private HikariConnectionPool() {
        // Singleton
    }
    
    /**
     * Obtém uma conexão do pool
     * 
     * @return Connection do pool (DEVE ser fechada após uso!)
     * @throws SQLException se não conseguir obter conexão
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initializePool();
                }
            }
        }
        
        try {
            Connection conn = dataSource.getConnection();
            
            // Log a cada 100 conexões para não poluir o log
            int active = dataSource.getHikariPoolMXBean().getActiveConnections();
            int idle = dataSource.getHikariPoolMXBean().getIdleConnections();
            int total = dataSource.getHikariPoolMXBean().getTotalConnections();
            
            // Log detalhado apenas em debug ou quando há muitas conexões ativas
            if (active > 10) {
                logger.warn("⚠️ Pool com muitas conexões ativas: {}/{} (ociosas: {})", active, total, idle);
            } else {
                logger.debug("✓ HikariCP: Ativas={}, Ociosas={}, Total={}", active, idle, total);
            }
            
            return conn;
        } catch (SQLException e) {
            logger.error("❌ Erro ao obter conexão do pool: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Inicializa o pool de conexões
     */
    private static void initializePool() throws SQLException {
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("🚀 Inicializando HikariCP Connection Pool");
        logger.info("═══════════════════════════════════════════════════════════════");
        
        try {
            // Carregar configurações do banco
            String[] config = loadDatabaseConfig();
            String host = config[0];
            int port = Integer.parseInt(config[1]);
            String database = config[2];
            String user = config[3];
            String password = config[4];
            
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            HikariConfig hikariConfig = new HikariConfig();
            
            // Configurações de conexão
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(password);
            hikariConfig.setDriverClassName("org.postgresql.Driver");
            
            // Configurações do pool
            hikariConfig.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
            hikariConfig.setMinimumIdle(MINIMUM_IDLE);
            hikariConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
            hikariConfig.setIdleTimeout(IDLE_TIMEOUT);
            hikariConfig.setMaxLifetime(MAX_LIFETIME);
            hikariConfig.setLeakDetectionThreshold(LEAK_DETECTION);
            
            // Nome do pool para identificação nos logs
            hikariConfig.setPoolName("InventarioPool");
            
            // Configurações de performance
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            
            // Configurações de timeout do PostgreSQL
            hikariConfig.addDataSourceProperty("socketTimeout", "30");
            hikariConfig.addDataSourceProperty("connectTimeout", "10");
            
            // Criar o DataSource
            dataSource = new HikariDataSource(hikariConfig);
            
            // Testar conexão
            try (Connection testConn = dataSource.getConnection()) {
                logger.info("✅ Pool inicializado com sucesso!");
                logger.info("   URL: {}", jdbcUrl);
                logger.info("   Pool Size: {} (min: {})", MAXIMUM_POOL_SIZE, MINIMUM_IDLE);
                logger.info("   Leak Detection: {}ms", LEAK_DETECTION);
            }
            
            logger.info("═══════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            logger.error("❌ Falha ao inicializar pool de conexões: {}", e.getMessage(), e);
            throw new SQLException("Falha ao inicializar pool de conexões", e);
        }
    }
    
    /**
     * Carrega configurações do banco do arquivo JSON
     */
    private static String[] loadDatabaseConfig() throws Exception {
        File jsonFile = new File("configuracao_banco.json");
        
        if (!jsonFile.exists()) {
            throw new Exception("Arquivo configuracao_banco.json não encontrado");
        }
        
        StringBuilder content = new StringBuilder();
        try (FileReader reader = new FileReader(jsonFile)) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
        }
        
        String json = content.toString();
        
        String host = extractJsonValue(json, "host");
        String port = extractJsonValue(json, "port");
        String database = extractJsonValue(json, "database");
        String user = extractJsonValue(json, "user");
        String password = extractJsonValue(json, "password");
        
        if (host == null || database == null || user == null || password == null) {
            throw new Exception("Configuração de banco incompleta no JSON");
        }
        
        return new String[] { host, port != null ? port : "5432", database, user, password };
    }
    
    /**
     * Extrai valor do JSON (parser simples)
     */
    private static String extractJsonValue(String json, String key) {
        try {
            int pgStart = json.indexOf("\"postgresql\"");
            if (pgStart == -1) return null;
            
            int blockStart = json.indexOf("{", pgStart);
            int blockEnd = json.indexOf("}", blockStart);
            if (blockStart == -1 || blockEnd == -1) return null;
            
            String pgBlock = json.substring(blockStart, blockEnd + 1);
            
            String searchKey = "\"" + key + "\"";
            int keyIndex = pgBlock.indexOf(searchKey);
            if (keyIndex == -1) return null;
            
            int colonIndex = pgBlock.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;
            
            int valueStart = colonIndex + 1;
            while (valueStart < pgBlock.length() && 
                   (pgBlock.charAt(valueStart) == ' ' || pgBlock.charAt(valueStart) == '\t')) {
                valueStart++;
            }
            
            if (valueStart >= pgBlock.length()) return null;
            
            if (pgBlock.charAt(valueStart) == '"') {
                int valueEnd = pgBlock.indexOf("\"", valueStart + 1);
                if (valueEnd == -1) return null;
                return pgBlock.substring(valueStart + 1, valueEnd);
            } else {
                int valueEnd = valueStart;
                while (valueEnd < pgBlock.length() && 
                       pgBlock.charAt(valueEnd) != ',' && 
                       pgBlock.charAt(valueEnd) != '}' &&
                       pgBlock.charAt(valueEnd) != '\n') {
                    valueEnd++;
                }
                return pgBlock.substring(valueStart, valueEnd).trim();
            }
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Retorna estatísticas do pool
     */
    public static String getPoolStats() {
        if (dataSource == null || dataSource.isClosed()) {
            return "Pool não inicializado";
        }
        
        return String.format(
            "Pool Stats - Ativas: %d, Ociosas: %d, Total: %d, Aguardando: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * Fecha o pool de conexões (usar apenas no shutdown da aplicação)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("🛑 Fechando pool de conexões...");
            dataSource.close();
            logger.info("✅ Pool fechado com sucesso");
        }
    }
    
    /**
     * Verifica se o pool está saudável
     */
    public static boolean isHealthy() {
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.warn("Pool não está saudável: {}", e.getMessage());
            return false;
        }
    }
}
