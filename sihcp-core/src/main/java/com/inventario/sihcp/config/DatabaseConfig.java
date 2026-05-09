package com.inventario.sihcp.config;

/**
 * POJO para armazenar configurações de conexão com banco de dados.
 * Usado por DatabaseConfigManager e DatabaseConnection.
 * 
 * @author Sistema de Inventário
 * @version 2.0
 */
public class DatabaseConfig {
    
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private boolean ssl;
    private int connectionTimeout;
    private String schema; // Schema do banco (opcional)
    
    /**
     * Construtor padrão
     */
    public DatabaseConfig() {
        this.host = "localhost";
        this.port = 5432;
        this.database = "";
        this.username = "";
        this.password = "";
        this.ssl = false;
        this.connectionTimeout = 10;
        this.schema = "public"; // Schema padrão do PostgreSQL
    }
    
    /**
     * Construtor com parâmetros
     */
    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.ssl = false;
        this.connectionTimeout = 10;
        this.schema = "public"; // Schema padrão do PostgreSQL
    }
    
    // Getters e Setters
    
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
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    /**
     * Valida se a configuração está completa
     */
    public boolean isValid() {
        return host != null && !host.isEmpty() &&
               database != null && !database.isEmpty() &&
               username != null && !username.isEmpty();
    }
    
    /**
     * Retorna string segura (sem senha)
     */
    public String toSafeString() {
        return String.format("DatabaseConfig{host='%s', port=%d, database='%s', username='%s', ssl=%b}",
                host, port, database, username, ssl);
    }
    
    @Override
    public String toString() {
        return toSafeString();
    }
}
