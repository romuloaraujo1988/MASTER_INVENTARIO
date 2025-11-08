package com.inventario.config;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gerenciador de configurações do banco de dados
 * 
 * Esta classe é responsável por carregar, salvar e testar
 * as configurações de conexão com o banco de dados PostgreSQL.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DatabaseConfigManager {
    
    private static final String CONFIG_FILE = "database-config.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".inventario";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILE;
    private DatabaseConfig currentConfig;
    
    public DatabaseConfigManager() {
        createConfigDirectoryIfNotExists();
        loadConfiguration();
    }
    
    /**
     * Cria o diretório de configuração se não existir
     */
    private void createConfigDirectoryIfNotExists() {
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (created) {
                System.out.println("Diretório de configuração criado: " + CONFIG_DIR);
            } else {
                System.err.println("Falha ao criar diretório de configuração: " + CONFIG_DIR);
            }
        }
    }
    
    /**
     * Verifica se existe configuração salva
     * 
     * @return true se existe configuração, false caso contrário
     */
    public boolean hasConfiguration() {
        return currentConfig != null && currentConfig.isValid();
    }
    
    /**
     * Carrega a configuração do arquivo
     */
    private void loadConfiguration() {
        File configFile = new File(CONFIG_PATH);
        
        if (configFile.exists()) {
            try {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                currentConfig = new DatabaseConfig(
                    props.getProperty("host", "localhost"),
                    Integer.parseInt(props.getProperty("port", "5432")),
                    props.getProperty("database", ""),
                    props.getProperty("username", ""),
                    props.getProperty("password", "")
                );
                System.out.println("Configuração de banco carregada com sucesso.");
            } catch (IOException | NumberFormatException e) {
                System.err.println("Erro ao carregar configuração de banco: " + e.getMessage());
                currentConfig = null;
            }
        } else {
            System.out.println("Arquivo de configuração não encontrado: " + CONFIG_PATH);
            currentConfig = null;
        }
    }
    
    /**
     * Salva a configuração no arquivo
     * 
     * @param config configuração a ser salva
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveConfiguration(DatabaseConfig config) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            Properties props = new Properties();
            props.setProperty("host", config.getHost());
            props.setProperty("port", String.valueOf(config.getPort()));
            props.setProperty("database", config.getDatabase());
            props.setProperty("username", config.getUsername());
            props.setProperty("password", config.getPassword());
            
            props.store(fos, "Database Configuration");
            this.currentConfig = config;
            System.out.println("Configuração de banco salva com sucesso.");
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração de banco: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Testa a conexão com o banco de dados
     * 
     * @return true se a conexão foi bem-sucedida, false caso contrário
     */
    public boolean testConnection() {
        if (currentConfig == null || !currentConfig.isValid()) {
            System.err.println("Configuração inválida para teste de conexão.");
            return false;
        }
        
        return testConnection(currentConfig);
    }
    
    /**
     * Testa a conexão com uma configuração específica
     * 
     * @param config configuração a ser testada
     * @return true se a conexão foi bem-sucedida, false caso contrário
     */
    public boolean testConnection(DatabaseConfig config) {
        if (config == null || !config.isValid()) {
            System.err.println("Configuração inválida para teste de conexão.");
            return false;
        }
        
        String url = String.format("jdbc:postgresql://%s:%d/%s", 
                                  config.getHost(), 
                                  config.getPort(), 
                                  config.getDatabase());
        
        try (Connection connection = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
            System.out.println("Teste de conexão bem-sucedido com: " + config.getHost());
            return true;
        } catch (SQLException e) {
            System.err.println("Falha no teste de conexão: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém a configuração atual
     * 
     * @return configuração atual ou null se não existe
     */
    public DatabaseConfig getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * Obtém a URL de conexão JDBC
     * 
     * @return URL de conexão ou null se configuração inválida
     */
    public String getJdbcUrl() {
        if (currentConfig == null || !currentConfig.isValid()) {
            return null;
        }
        
        return String.format("jdbc:postgresql://%s:%d/%s", 
                           currentConfig.getHost(), 
                           currentConfig.getPort(), 
                           currentConfig.getDatabase());
    }
    
    /**
     * Remove a configuração salva
     * 
     * @return true se removeu com sucesso, false caso contrário
     */
    public boolean removeConfiguration() {
        File configFile = new File(CONFIG_PATH);
        
        if (configFile.exists()) {
            boolean deleted = configFile.delete();
            if (deleted) {
                currentConfig = null;
                System.out.println("Configuração de banco removida com sucesso.");
                return true;
            } else {
                System.err.println("Falha ao remover configuração de banco.");
                return false;
            }
        }
        
        return true; // Arquivo não existe, consideramos como removido
    }
}