package com.inventario.config;

/**
 * Classe de configuração do banco de dados
 * 
 * Armazena as configurações de conexão com o PostgreSQL.
 * Usada pelo DatabaseConfigManager para gerenciar conexões.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DatabaseConfig {
    
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String schema;
    private boolean ssl;
    private int connectionTimeout;
    
    /**
     * Construtor padrão
     */
    public DatabaseConfig() {
        this.host = "localhost";
        this.port = 5432;
        this.database = "";
        this.username = "";
        this.password = "";
        this.schema = "public";
        this.ssl = false;
        this.connectionTimeout = 30000;
    }
    
    /**
     * Construtor com parâmetros básicos
     */
    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.schema = "public";
        this.ssl = false;
        this.connectionTimeout = 30000;
    }
    
    /**
     * Verifica se a configuração é válida
     * 
     * @return true se todos os campos obrigatórios estão preenchidos
     */
    public boolean isValid() {
        return host != null && !host.isEmpty() &&
               port > 0 && port < 65536 &&
               database != null && !database.isEmpty() &&
               username != null && !username.isEmpty();
    }
    
    /**
     * Retorna uma string segura (sem senha) para logging
     */
    public String toSafeString() {
        return String.format("DatabaseConfig{host='%s', port=%d, database='%s', username='%s', ssl=%b}",
            host, port, database, username, ssl);
    }
    
    // ========== Getters e Setters ==========
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public boolean isSsl() {
        return ssl;
    }
    
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    @Override
    public String toString() {
        return toSafeString();
    }
}
