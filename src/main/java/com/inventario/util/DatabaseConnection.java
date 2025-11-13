package com.inventario.util;

import com.inventario.config.DatabaseConfig;
import com.inventario.config.DatabaseConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe utilitária para gerenciar conexões com o banco de dados
 * 
 * Esta classe centraliza a lógica de conexão com o banco PostgreSQL,
 * utilizando as configurações definidas no DatabaseConfigManager.
 * 
 * @author Sistema de Inventário
 * @version 1.0
 */
public class DatabaseConnection {
    
    private static DatabaseConfigManager configManager;
    private static DatabaseConfig currentConfig;
    
    static {
        try {
            // Carrega o driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Inicializa o gerenciador de configurações
            configManager = new DatabaseConfigManager();
            currentConfig = configManager.getCurrentConfig();
            
            System.out.println("DatabaseConnection inicializado com sucesso.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL não encontrado: " + e.getMessage());
            throw new RuntimeException("Falha ao carregar driver PostgreSQL", e);
        } catch (Exception e) {
            System.err.println("Erro ao inicializar DatabaseConnection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtém uma conexão com o banco de dados
     * 
     * @return Connection ativa com o banco
     * @throws SQLException se houver erro na conexão
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Tenta usar a configuração carregada
            if (currentConfig != null && currentConfig.isValid()) {
                return createConnectionFromConfig(currentConfig);
            }
            
            // Sem configuração válida - não usar fallback com senha hardcoded
            System.err.println("╔════════════════════════════════════════════════════════════════╗");
            System.err.println("║  ⚠️  ERRO: Nenhuma configuração de banco encontrada           ║");
            System.err.println("╠════════════════════════════════════════════════════════════════╣");
            System.err.println("║  Configure o banco de dados via:                              ║");
            System.err.println("║  1. Tela de Login → Botão '⚙ Configurar Banco'               ║");
            System.err.println("║  2. Ou use ConfigurationMigration.migrateConfiguration()      ║");
            System.err.println("╚════════════════════════════════════════════════════════════════╝");
            
            throw new SQLException(
                "Nenhuma configuração de banco de dados encontrada. " +
                "Configure via interface ou arquivo ~/.inventario/database-config.properties"
            );
            
        } catch (SQLException e) {
            System.err.println("Erro ao obter conexão com banco de dados: " + e.getMessage());
            throw e;
        }
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
        
        // Configurações adicionais
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
            System.err.println("Falha no teste de conexão: " + e.getMessage());
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
            System.err.println("Falha no teste de conexão: " + e.getMessage());
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
            System.out.println("Configuração do banco atualizada.");
        }
    }
    
    /**
     * Obtém a configuração atual
     * 
     * @return Configuração atual do banco
     */
    public static DatabaseConfig getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * Recarrega a configuração do arquivo
     */
    public static void reloadConfig() {
        try {
            // Reinicializa o gerenciador para recarregar a configuração
            configManager = new DatabaseConfigManager();
            currentConfig = configManager.getCurrentConfig();
            System.out.println("Configuração recarregada.");
        } catch (Exception e) {
            System.err.println("Erro ao recarregar configuração: " + e.getMessage());
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
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
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