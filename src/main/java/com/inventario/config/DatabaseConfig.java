package com.inventario.config;

/**
 * Classe que representa a configuração de conexão com o banco de dados PostgreSQL
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
    private int maxPoolSize;
    
    /**
     * Construtor padrão
     */
    public DatabaseConfig() {
        // Valores padrão
        this.host = "localhost";
        this.port = 5432;
        this.database = "sispatrimonio";
        this.schema = "public";
        this.ssl = false;
        this.connectionTimeout = 30;
        this.maxPoolSize = 10;
    }
    
    /**
     * Construtor com parâmetros principais
     */
    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this();
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Valida se a configuração está completa
     * 
     * @return true se a configuração é válida, false caso contrário
     */
    public boolean isValid() {
        return host != null && !host.trim().isEmpty() &&
               port > 0 && port <= 65535 &&
               database != null && !database.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               password != null; // Senha pode ser vazia
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
    
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    @Override
    public String toString() {
        return String.format("DatabaseConfig{host='%s', port=%d, database='%s', username='%s', schema='%s', ssl=%s}",
                           host, port, database, username, schema, ssl);
    }
    
    /**
     * Cria uma cópia da configuração sem a senha (para logs)
     * 
     * @return string segura para log
     */
    public String toSafeString() {
        return String.format("DatabaseConfig{host='%s', port=%d, database='%s', username='%s', schema='%s', ssl=%s}",
                           host, port, database, username, schema, ssl);
    }
}