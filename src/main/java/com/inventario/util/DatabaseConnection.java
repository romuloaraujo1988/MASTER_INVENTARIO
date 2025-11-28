package com.inventario.util;

import com.inventario.config.DatabaseConfig;
import com.inventario.config.DatabaseConfigManager;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe utilitária para gerenciar conexões com o banco de dados
 * 
 * Esta classe centraliza a lógica de conexão com o banco PostgreSQL,
 * utilizando as configurações definidas no DatabaseConfigManager.
 * PRIORIDADE: Arquivo configuracao_banco.json na raiz do projeto
 * 
 * @author Sistema de Inventário
 * @version 1.1
 */
public class DatabaseConnection {
    
    private static DatabaseConfigManager configManager;
    private static DatabaseConfig currentConfig;
    
    // Configuração direta do arquivo JSON na raiz (fallback prioritário)
    private static String jsonHost;
    private static int jsonPort;
    private static String jsonDatabase;
    private static String jsonUser;
    private static String jsonPassword;
    private static boolean jsonConfigLoaded = false;
    
    static {
        try {
            // Carrega o driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // PRIORIDADE 1: Tentar carregar do arquivo configuracao_banco.json na raiz
            loadJsonConfig();
            
            // PRIORIDADE 2: Se não carregou do JSON, usar DatabaseConfigManager
            if (!jsonConfigLoaded) {
                configManager = new DatabaseConfigManager();
                currentConfig = configManager.getCurrentConfig();
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL não encontrado: " + e.getMessage());
            throw new RuntimeException("Falha ao carregar driver PostgreSQL", e);
        } catch (Exception e) {
            System.err.println("Erro ao inicializar DatabaseConnection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Carrega configuração do arquivo configuracao_banco.json na raiz do projeto
     */
    private static void loadJsonConfig() {
        try {
            File jsonFile = new File("configuracao_banco.json");
            if (!jsonFile.exists()) {
                return;
            }
            
            // Ler o arquivo JSON manualmente (sem dependência externa)
            StringBuilder content = new StringBuilder();
            try (FileReader reader = new FileReader(jsonFile)) {
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    content.append(buffer, 0, read);
                }
            }
            
            String json = content.toString();
            
            // Parse simples do JSON para extrair valores do PostgreSQL
            jsonHost = extractJsonValue(json, "host");
            jsonDatabase = extractJsonValue(json, "database");
            jsonUser = extractJsonValue(json, "user");
            jsonPassword = extractJsonValue(json, "password");
            String portStr = extractJsonValue(json, "port");
            jsonPort = portStr != null ? Integer.parseInt(portStr.trim()) : 5432;
            
            if (jsonHost != null && jsonDatabase != null && jsonUser != null && jsonPassword != null) {
                jsonConfigLoaded = true;
            }
            
        } catch (Exception e) {
            // Ignorar erros silenciosamente - fallback para outras configurações
        }
    }
    
    /**
     * Extrai um valor de uma string JSON de forma simples
     * Procura dentro do bloco "postgresql"
     */
    private static String extractJsonValue(String json, String key) {
        try {
            // Encontrar o bloco postgresql
            int pgStart = json.indexOf("\"postgresql\"");
            if (pgStart == -1) return null;
            
            int blockStart = json.indexOf("{", pgStart);
            int blockEnd = json.indexOf("}", blockStart);
            if (blockStart == -1 || blockEnd == -1) return null;
            
            String pgBlock = json.substring(blockStart, blockEnd + 1);
            
            // Procurar a chave dentro do bloco
            String searchKey = "\"" + key + "\"";
            int keyIndex = pgBlock.indexOf(searchKey);
            if (keyIndex == -1) return null;
            
            int colonIndex = pgBlock.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;
            
            // Encontrar o valor após os dois pontos
            int valueStart = colonIndex + 1;
            while (valueStart < pgBlock.length() && 
                   (pgBlock.charAt(valueStart) == ' ' || pgBlock.charAt(valueStart) == '\t')) {
                valueStart++;
            }
            
            if (valueStart >= pgBlock.length()) return null;
            
            // Verificar se é string (começa com aspas) ou número
            if (pgBlock.charAt(valueStart) == '"') {
                int valueEnd = pgBlock.indexOf("\"", valueStart + 1);
                if (valueEnd == -1) return null;
                return pgBlock.substring(valueStart + 1, valueEnd);
            } else {
                // É um número ou outro valor
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
     * Obtém uma conexão com o banco de dados
     * 
     * ⚠️ IMPORTANTE: Agora usa HikariCP Connection Pool para evitar vazamento de conexões!
     * 
     * PRIORIDADE: 
     * 1. HikariCP Pool (RECOMENDADO - reutiliza conexões)
     * 2. Arquivo configuracao_banco.json na raiz (fallback)
     * 3. DatabaseConfigManager (fallback)
     * 4. SQLite apenas se explicitamente forçado offline
     * 
     * @return Connection ativa com o banco (PostgreSQL ou SQLite)
     * @throws SQLException se houver erro na conexão
     */
    public static Connection getConnection() throws SQLException {
        try {
            // PRIORIDADE 0: Verificar se modo offline foi EXPLICITAMENTE forçado
            try {
                com.inventario.offline.OfflineManager offlineManager = 
                    com.inventario.offline.OfflineManager.getInstance();
                
                if (offlineManager.isForcedOffline()) {
                    return getOfflineConnection();
                }
            } catch (Exception e) {
                // Ignorar erros do OfflineManager - continuar com PostgreSQL
            }
            
            // PRIORIDADE 1: Usar HikariCP Pool (RECOMENDADO)
            // O pool gerencia conexões automaticamente, evitando vazamentos
            try {
                return com.inventario.config.HikariConnectionPool.getConnection();
            } catch (SQLException e) {
                System.err.println("⚠️ Falha ao obter conexão do pool HikariCP: " + e.getMessage());
                System.err.println("   Tentando conexão direta como fallback...");
                // Continua para tentar conexão direta
            }
            
            // PRIORIDADE 2: Usar configuração do JSON na raiz (fallback)
            if (jsonConfigLoaded) {
                try {
                    System.out.println("⚠️ Usando conexão direta (sem pool) - pode causar vazamento de memória!");
                    return createConnectionFromJson();
                } catch (SQLException e) {
                    // Continua para tentar outras opções
                }
            }
            
            // PRIORIDADE 3: Se há configuração via DatabaseConfigManager
            if (currentConfig != null && currentConfig.isValid()) {
                try {
                    System.out.println("⚠️ Usando conexão direta (sem pool) - pode causar vazamento de memória!");
                    return createConnectionFromConfig(currentConfig);
                } catch (SQLException e) {
                    // Continua para tentar outras opções
                }
            }
            
            // PRIORIDADE 4: Tentar novamente com JSON se disponível
            if (jsonConfigLoaded) {
                return createConnectionFromJson();
            }
            
            // Sem configuração válida
            System.err.println("╔════════════════════════════════════════════════════════════════╗");
            System.err.println("║  ⚠️  ERRO: Nenhuma configuração de banco encontrada           ║");
            System.err.println("╠════════════════════════════════════════════════════════════════╣");
            System.err.println("║  Crie o arquivo configuracao_banco.json na raiz do projeto    ║");
            System.err.println("║  ou configure via Tela de Login → '⚙ Configurar Banco'       ║");
            System.err.println("╚════════════════════════════════════════════════════════════════╝");
            
            throw new SQLException(
                "Nenhuma configuração de banco de dados encontrada. " +
                "Crie configuracao_banco.json na raiz do projeto."
            );
            
        } catch (SQLException e) {
            System.err.println("Erro ao obter conexão com banco de dados: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Cria conexão usando configuração do arquivo JSON
     */
    private static Connection createConnectionFromJson() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s", jsonHost, jsonPort, jsonDatabase);
        
        Properties props = new Properties();
        props.setProperty("user", jsonUser);
        props.setProperty("password", jsonPassword);
        props.setProperty("connectTimeout", "10");
        props.setProperty("socketTimeout", "30");
        
        return DriverManager.getConnection(url, props);
    }
    
    /**
     * Obtém uma conexão com o banco SQLite offline
     * 
     * @return Connection ativa com SQLite
     * @throws SQLException se houver erro na conexão
     */
    private static Connection getOfflineConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            
            String dbPath = "data/inventario.db";
            String url = "jdbc:sqlite:" + dbPath;
            
            Connection conn = DriverManager.getConnection(url);
            
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não disponível", e);
        }
    }
    
    /**
     * Obtém uma conexão direta com PostgreSQL (ignora modo offline)
     * Usado para sincronização de dados quando o servidor está disponível
     * 
     * @return Connection ativa com PostgreSQL
     * @throws SQLException se houver erro na conexão ou configuração inválida
     */
    public static Connection getPostgreSQLConnection() throws SQLException {
        // PRIORIDADE 1: Usar configuração do JSON na raiz
        if (jsonConfigLoaded) {
            return createConnectionFromJson();
        }
        
        // PRIORIDADE 2: Usar DatabaseConfigManager
        if (currentConfig != null && currentConfig.isValid()) {
            return createConnectionFromConfig(currentConfig);
        }
        
        // Tentar recarregar do JSON
        loadJsonConfig();
        
        if (jsonConfigLoaded) {
            return createConnectionFromJson();
        }
        
        throw new SQLException("Configuração de banco PostgreSQL não disponível. " +
            "Verifique o arquivo configuracao_banco.json na raiz do projeto.");
    }
    
    /**
     * Cria uma conexão usando uma configuração específica
     * 
     * @param config Configuração do banco
     * @return Connection ativa
     * @throws SQLException se houver erro na conexão
     */
    private static Connection createConnectionFromConfig(DatabaseConfig config) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s", 
                                  config.getHost(), 
                                  config.getPort(), 
                                  config.getDatabase());
        
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        
        if (config.isSsl()) {
            props.setProperty("ssl", "true");
        }
        
        props.setProperty("connectTimeout", String.valueOf(config.getConnectionTimeout() * 1000));
        props.setProperty("socketTimeout", "30");
        props.setProperty("loginTimeout", String.valueOf(config.getConnectionTimeout()));
        
        return DriverManager.getConnection(url, props);
    }
    
    /**
     * Testa a conexão com o banco de dados
     * 
     * @return true se a conexão foi bem-sucedida
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Testa uma configuração específica
     * 
     * @param config Configuração a ser testada
     * @return true se a conexão foi bem-sucedida
     */
    public static boolean testConnection(DatabaseConfig config) {
        if (config == null || !config.isValid()) {
            return false;
        }
        
        try (Connection conn = createConnectionFromConfig(config)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Atualiza a configuração atual do banco
     * 
     * @param newConfig Nova configuração
     */
    public static void updateConfig(DatabaseConfig newConfig) {
        if (newConfig != null && newConfig.isValid()) {
            currentConfig = newConfig;
            if (configManager != null) {
                configManager.saveConfiguration(newConfig);
            }
        }
    }
    
    /**
     * Obtém a configuração atual
     * Retorna configuração do DatabaseConfigManager ou cria uma a partir do JSON
     * 
     * @return Configuração atual do banco (nunca null se JSON estiver configurado)
     */
    public static DatabaseConfig getCurrentConfig() {
        // Se já tem configuração do ConfigManager, retorna ela
        if (currentConfig != null && currentConfig.isValid()) {
            return currentConfig;
        }
        
        // Se configuração JSON está carregada, cria um DatabaseConfig a partir dela
        if (jsonConfigLoaded) {
            DatabaseConfig jsonBasedConfig = new DatabaseConfig();
            jsonBasedConfig.setHost(jsonHost);
            jsonBasedConfig.setPort(jsonPort);
            jsonBasedConfig.setDatabase(jsonDatabase);
            jsonBasedConfig.setUsername(jsonUser);
            jsonBasedConfig.setPassword(jsonPassword);
            jsonBasedConfig.setConnectionTimeout(10);
            return jsonBasedConfig;
        }
        
        return currentConfig;
    }
    
    /**
     * Verifica se há configuração válida disponível (JSON ou ConfigManager)
     * 
     * @return true se há configuração válida
     */
    public static boolean hasValidConfig() {
        return jsonConfigLoaded || (currentConfig != null && currentConfig.isValid());
    }
    
    /**
     * Recarrega a configuração do arquivo
     */
    public static void reloadConfig() {
        try {
            configManager = new DatabaseConfigManager();
            currentConfig = configManager.getCurrentConfig();
        } catch (Exception e) {
            // Ignorar erros silenciosamente
        }
    }
    
    /**
     * Fecha uma conexão de forma segura
     * 
     * @param connection Conexão a ser fechada
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                // Ignorar erros ao fechar
            }
        }
    }
    
    /**
     * Obtém informações sobre a conexão atual
     * 
     * @return String com informações da configuração
     */
    public static String getConnectionInfo() {
        if (currentConfig != null) {
            return String.format("Host: %s:%d | Database: %s | User: %s", 
                               currentConfig.getHost(),
                               currentConfig.getPort(),
                               currentConfig.getDatabase(),
                               currentConfig.getUsername());
        }
        return "Configuração não carregada - usando fallback";
    }
}