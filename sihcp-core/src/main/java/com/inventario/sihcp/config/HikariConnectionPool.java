package com.inventario.sihcp.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Configuração centralizada do connection pool usando HikariCP.
 * Singleton thread-safe para gerenciar conexões com o banco de dados.
 * 
 * RENOMEADO de DatabaseConfig para HikariConnectionPool para evitar conflito
 * com a classe DatabaseConfig (POJO) usada por DatabaseConfigManager.
 * 
 * @author Sistema de Inventário
 * @version 2.0
 */
public class HikariConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(HikariConnectionPool.class);
    private static volatile HikariConnectionPool instance;
    private static final Object lock = new Object();
    
    private HikariDataSource dataSource;
    
    // Configurações padrão
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 5000; // 5 segundos
    private static final long DEFAULT_IDLE_TIMEOUT = 300000; // 5 minutos
    private static final long DEFAULT_MAX_LIFETIME = 1800000; // 30 minutos
    private static final long DEFAULT_LEAK_DETECTION_THRESHOLD = 300000; // 5 minutos
    
    /**
     * Construtor privado para singleton.
     * Inicializa o HikariCP com configurações otimizadas.
     */
    private HikariConnectionPool() {
        try {
            initializeDataSource();
            logger.info("HikariConnectionPool inicializado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao inicializar HikariConnectionPool", e);
            throw new RuntimeException("Falha ao configurar connection pool", e);
        }
    }
    
    /**
     * Obtém a instância singleton do HikariConnectionPool.
     * Thread-safe usando double-checked locking.
     * 
     * @return instância única do HikariConnectionPool
     */
    public static HikariConnectionPool getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new HikariConnectionPool();
                }
            }
        }
        return instance;
    }

    /**
     * Reconfigura e reinicializa o pool com novos parâmetros.
     * Útil quando a configuração é carregada de fontes dinâmicas (como JSON).
     */
    public static void setup(String url, String user, String password) {
        synchronized (lock) {
            if (instance != null) {
                instance.close();
            }
            instance = new HikariConnectionPool(url, user, password);
        }
    }

    /**
     * Construtor para inicialização dinâmica.
     */
    private HikariConnectionPool(String url, String user, String password) {
        try {
            Properties props = new Properties();
            props.setProperty("db.url", url);
            props.setProperty("db.username", user);
            props.setProperty("db.password", password);
            initializeDataSource(props);
            logger.info("HikariConnectionPool inicializado dinamicamente com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao inicializar HikariConnectionPool dinamicamente", e);
            throw new RuntimeException("Falha ao configurar connection pool", e);
        }
    }
    
    /**
     * Inicializa o HikariDataSource com configurações do arquivo de propriedades.
     */
    private void initializeDataSource() {
        initializeDataSource(loadDatabaseProperties());
    }

    /**
     * Inicializa o HikariDataSource com propriedades específicas.
     */
    private void initializeDataSource(Properties props) {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
        }

        HikariConfig config = new HikariConfig();
        
        // Configurações de conexão
        config.setJdbcUrl(props.getProperty("db.url", "jdbc:postgresql://localhost:5432/sispatrimonio"));
        config.setUsername(props.getProperty("db.username", "postgres")); // Alterado padrão para postgres
        config.setPassword(props.getProperty("db.password", "Romulo@1919")); // Alterado padrão para o atual
        config.setDriverClassName("org.postgresql.Driver");
        
        // Configurações do pool
        config.setMaximumPoolSize(getIntProperty(props, "db.pool.maxSize", DEFAULT_MAX_POOL_SIZE));
        config.setMinimumIdle(getIntProperty(props, "db.pool.minIdle", DEFAULT_MIN_IDLE));
        config.setConnectionTimeout(getLongProperty(props, "db.pool.connectionTimeout", DEFAULT_CONNECTION_TIMEOUT));
        config.setIdleTimeout(getLongProperty(props, "db.pool.idleTimeout", DEFAULT_IDLE_TIMEOUT));
        config.setMaxLifetime(getLongProperty(props, "db.pool.maxLifetime", DEFAULT_MAX_LIFETIME));
        config.setLeakDetectionThreshold(getLongProperty(props, "db.pool.leakDetection", DEFAULT_LEAK_DETECTION_THRESHOLD));
        
        // Configurações de performance
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("InventarioHikariPool");
        
        // Propriedades adicionais do PostgreSQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        this.dataSource = new HikariDataSource(config);
        
        logger.info("HikariCP configurado: maxPoolSize={}, minIdle={}, connectionTimeout={}ms",
                config.getMaximumPoolSize(), config.getMinimumIdle(), config.getConnectionTimeout());
    }
    
    /**
     * Carrega propriedades do banco de dados do arquivo de configuração.
     * 
     * @return Properties com configurações do banco
     */
    private Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        // 1. Tentar carregar do arquivo .properties no home do usuário (legado)
        String configPath = System.getProperty("user.home") + "/configuracao_banco.properties";
        File propFile = new File(configPath);
        if (propFile.exists()) {
            try (FileReader reader = new FileReader(propFile)) {
                props.load(reader);
                logger.info("Propriedades carregadas de: {}", configPath);
            } catch (IOException e) {
                logger.warn("Erro ao ler arquivo de configuração em {}: {}", configPath, e.getMessage());
            }
        }
        
        // 2. Se não encontrou as propriedades básicas, tentar carregar do JSON (preferencial)
        if (props.getProperty("db.url") == null) {
            loadFromJsonConfig(props);
        }
        
        // 3. Aplicar valores padrão se ainda estiverem vazios
        if (props.getProperty("db.url") == null) {
            props.setProperty("db.url", "jdbc:postgresql://localhost:5432/sispatrimonio");
        }
        if (props.getProperty("db.username") == null) {
            props.setProperty("db.username", "postgres");
        }
        if (props.getProperty("db.password") == null) {
            props.setProperty("db.password", "Romulo@1919");
        }
        
        return props;
    }

    /**
     * Tenta carregar configurações de arquivos JSON.
     */
    private void loadFromJsonConfig(Properties props) {
        String[] paths = {"config/configuracao_banco.json", "configuracao_banco.json"};
        for (String path : paths) {
            File jsonFile = new File(path);
            if (jsonFile.exists()) {
                try {
                    logger.info("Tentando carregar configuração do JSON: {}", path);
                    String json = readSmallFile(jsonFile);
                    
                    String host = extractJsonValue(json, "host");
                    String database = extractJsonValue(json, "database");
                    String user = extractJsonValue(json, "user");
                    String password = extractJsonValue(json, "password");
                    String portStr = extractJsonValue(json, "port");
                    
                    if (host != null && database != null) {
                        int port = portStr != null ? Integer.parseInt(portStr.trim()) : 5432;
                        props.setProperty("db.url", String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
                        if (user != null) props.setProperty("db.username", user);
                        if (password != null) props.setProperty("db.password", password);
                        logger.info("Configuração carregada com sucesso do JSON: {}", path);
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao fazer parse do JSON {}: {}", path, e.getMessage());
                }
            }
        }
    }

    private String readSmallFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        }
        return sb.toString();
    }

    private String extractJsonValue(String json, String key) {
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
            while (valueStart < pgBlock.length() && Character.isWhitespace(pgBlock.charAt(valueStart))) valueStart++;
            if (pgBlock.charAt(valueStart) == '"') {
                int valueEnd = pgBlock.indexOf("\"", valueStart + 1);
                return pgBlock.substring(valueStart + 1, valueEnd);
            } else {
                int valueEnd = valueStart;
                while (valueEnd < pgBlock.length() && pgBlock.charAt(valueEnd) != ',' && pgBlock.charAt(valueEnd) != '}') valueEnd++;
                return pgBlock.substring(valueStart, valueEnd).trim();
            }
        } catch (Exception e) { return null; }
    }
    
    /**
     * Obtém propriedade inteira com valor padrão.
     */
    private int getIntProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Valor inválido para {}: {}, usando padrão: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * Obtém propriedade long com valor padrão.
     */
    private long getLongProperty(Properties props, String key, long defaultValue) {
        String value = props.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Valor inválido para {}: {}, usando padrão: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * Obtém o DataSource configurado.
     * 
     * @return DataSource do HikariCP
     */
    public DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("DataSource não está disponível");
        }
        return dataSource;
    }
    
    /**
     * Obtém uma conexão do pool.
     * Método estático para facilitar uso.
     * 
     * @return Connection do pool
     * @throws SQLException se houver erro ao obter conexão
     */
    public static Connection getConnection() throws SQLException {
        return getInstance().getDataSource().getConnection();
    }
    
    /**
     * Fecha o connection pool e libera recursos.
     * Deve ser chamado ao encerrar a aplicação.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Fechando connection pool...");
            dataSource.close();
            logger.info("Connection pool fechado com sucesso");
        }
    }
    
    /**
     * Obtém estatísticas do pool de conexões.
     * 
     * @return String com estatísticas formatadas
     */
    public String getPoolStats() {
        if (dataSource == null || dataSource.isClosed()) {
            return "DataSource não disponível";
        }
        
        return String.format("Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
    
    /**
     * Verifica se o pool está saudável.
     * 
     * @return true se o pool está operacional
     */
    public boolean isHealthy() {
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            return true;
        } catch (Exception e) {
            logger.error("Health check falhou", e);
            return false;
        }
    }
}
