package com.inventario.config;

import com.inventario.util.PasswordEncryption;

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
    
    private DatabaseConfig currentConfig;
    
    public DatabaseConfigManager() {
        // Tentar migrar configuração antiga (se existir)
        ConfigurationPaths.migrateOldConfiguration();
        
        createConfigDirectoryIfNotExists();
        loadConfiguration();
    }
    
    /**
     * Cria o diretório de configuração se não existir
     */
    private void createConfigDirectoryIfNotExists() {
        ConfigurationPaths.createConfigDirectoryIfNotExists();
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
        String configPath = ConfigurationPaths.getDatabaseConfigPath();
        File configFile = new File(configPath);
        
        System.out.println("DEBUG DatabaseConfigManager: Carregando configuração de: " + configPath);
        
        if (configFile.exists()) {
            try {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                
                String host = props.getProperty("host", "localhost");
                String port = props.getProperty("port", "5432");
                String database = props.getProperty("database", "");
                String username = props.getProperty("username", "");
                
                System.out.println("DEBUG DatabaseConfigManager: host=" + host + ", port=" + port + 
                                   ", database=" + database + ", username=" + username);
                
                // Carregar senha (descriptografar se necessário)
                String encryptedPassword = props.getProperty("password", "");
                String password = "";
                
                if (!encryptedPassword.isEmpty()) {
                    // Verificar se parece estar criptografada (Base64 com padding)
                    boolean pareceEncriptada = PasswordEncryption.isEncrypted(encryptedPassword);
                    
                    if (pareceEncriptada) {
                        try {
                            // Tentar descriptografar
                            password = PasswordEncryption.decrypt(encryptedPassword);
                            System.out.println("DEBUG DatabaseConfigManager: Senha descriptografada com sucesso (tamanho: " + password.length() + ")");
                        } catch (Exception e) {
                            // Falha na descriptografia - senha foi criptografada em outra máquina
                            System.out.println("DEBUG DatabaseConfigManager: Falha na descriptografia: " + e.getMessage());
                            System.err.println("╔════════════════════════════════════════════════════════════════╗");
                            System.err.println("║  ⚠️  AVISO: Falha ao descriptografar senha do banco           ║");
                            System.err.println("╠════════════════════════════════════════════════════════════════╣");
                            System.err.println("║  A senha foi criptografada em outra máquina ou houve          ║");
                            System.err.println("║  mudança no sistema. Reconfigure o banco de dados via:        ║");
                            System.err.println("║  Tela de Login → Botão '⚙ Configurar Banco'                  ║");
                            System.err.println("╚════════════════════════════════════════════════════════════════╝");
                            // Usar string vazia para forçar reconfiguração
                            password = "";
                        }
                    } else {
                        // Senha em texto plano (compatibilidade)
                        System.out.println("DEBUG DatabaseConfigManager: Senha em texto plano detectada");
                        password = encryptedPassword;
                    }
                }
                
                currentConfig = new DatabaseConfig(
                    host,
                    Integer.parseInt(port),
                    database,
                    username,
                    password
                );
                
                System.out.println("DEBUG DatabaseConfigManager: Configuração carregada - " + currentConfig.toSafeString());
                
                // Se a senha está vazia, tentar carregar do configuracao_banco.json
                if (password == null || password.isEmpty()) {
                    System.out.println("DEBUG DatabaseConfigManager: Senha vazia, tentando fallback para configuracao_banco.json");
                    tryLoadPasswordFromJsonConfig();
                }
                
                // Validar configuração
                if (!currentConfig.isValid()) {
                    System.err.println("DEBUG DatabaseConfigManager: Configuração inválida! Campos obrigatórios faltando.");
                }
                
            } catch (IOException | NumberFormatException e) {
                System.err.println("Erro ao carregar configuração de banco: " + e.getMessage());
                e.printStackTrace();
                currentConfig = null;
            }
        } else {
            System.out.println("Arquivo de configuração não encontrado: " + configPath);
            // Tentar carregar do configuracao_banco.json como fallback
            tryLoadFromJsonConfig();
        }
    }
    
    /**
     * Tenta carregar apenas a senha do configuracao_banco.json
     */
    private void tryLoadPasswordFromJsonConfig() {
        // Tentar múltiplos caminhos possíveis
        String[] possiblePaths = {
            "configuracao_banco.json",
            System.getProperty("user.dir") + File.separator + "configuracao_banco.json",
            System.getProperty("user.home") + File.separator + "Documents" + File.separator + "PROJETOS" + File.separator + "MASTER_INVENTARIO" + File.separator + "configuracao_banco.json"
        };
        
        File jsonFile = null;
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                jsonFile = f;
                System.out.println("DEBUG DatabaseConfigManager: configuracao_banco.json encontrado em: " + path);
                break;
            }
        }
        
        if (jsonFile == null || !jsonFile.exists()) {
            System.out.println("DEBUG DatabaseConfigManager: configuracao_banco.json não encontrado em nenhum caminho");
            System.out.println("DEBUG DatabaseConfigManager: Diretório atual: " + System.getProperty("user.dir"));
            return;
        }
        
        try {
            String content = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()));
            System.out.println("DEBUG DatabaseConfigManager: Lendo configuracao_banco.json...");
            
            // Parse simples do JSON para extrair a senha do PostgreSQL
            // Procurar por "password": "valor" dentro da seção postgresql
            String password = extractPasswordFromJson(content);
            
            if (password != null && !password.isEmpty()) {
                System.out.println("DEBUG DatabaseConfigManager: Senha carregada do configuracao_banco.json (tamanho: " + password.length() + ")");
                currentConfig.setPassword(password);
            } else {
                System.out.println("DEBUG DatabaseConfigManager: Senha não encontrada no configuracao_banco.json");
            }
        } catch (Exception e) {
            System.err.println("DEBUG DatabaseConfigManager: Erro ao ler configuracao_banco.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extrai a senha do PostgreSQL do JSON de forma robusta
     */
    private String extractPasswordFromJson(String json) {
        try {
            // Encontrar a seção postgresql
            int pgStart = json.indexOf("\"postgresql\"");
            if (pgStart < 0) return null;
            
            // Encontrar o início e fim da seção postgresql
            int braceStart = json.indexOf("{", pgStart);
            int braceEnd = json.indexOf("}", braceStart);
            if (braceStart < 0 || braceEnd < 0) return null;
            
            String pgSection = json.substring(braceStart, braceEnd + 1);
            
            // Procurar "password" dentro da seção
            int pwdIndex = pgSection.indexOf("\"password\"");
            if (pwdIndex < 0) return null;
            
            // Encontrar o valor após os dois pontos
            int colonIndex = pgSection.indexOf(":", pwdIndex);
            if (colonIndex < 0) return null;
            
            // Encontrar as aspas do valor
            int firstQuote = pgSection.indexOf("\"", colonIndex + 1);
            int secondQuote = pgSection.indexOf("\"", firstQuote + 1);
            
            if (firstQuote < 0 || secondQuote < 0) return null;
            
            return pgSection.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            System.err.println("DEBUG: Erro ao extrair senha do JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tenta carregar configuração completa do configuracao_banco.json
     */
    private void tryLoadFromJsonConfig() {
        File jsonFile = new File("configuracao_banco.json");
        if (!jsonFile.exists()) {
            System.out.println("DEBUG DatabaseConfigManager: configuracao_banco.json não encontrado");
            return;
        }
        
        try {
            String content = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()));
            
            // Parse simples do JSON para extrair configuração do PostgreSQL
            if (content.contains("\"postgresql\"")) {
                String host = extractJsonValue(content, "host", "postgresql");
                String database = extractJsonValue(content, "database", "postgresql");
                String user = extractJsonValue(content, "user", "postgresql");
                String password = extractJsonValue(content, "password", "postgresql");
                String portStr = extractJsonValue(content, "port", "postgresql");
                
                int port = 5432;
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    // usar padrão
                }
                
                if (host != null && database != null && user != null) {
                    currentConfig = new DatabaseConfig(host, port, database, user, password != null ? password : "");
                    System.out.println("DEBUG DatabaseConfigManager: Configuração carregada do configuracao_banco.json - " + currentConfig.toSafeString());
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG DatabaseConfigManager: Erro ao ler configuracao_banco.json: " + e.getMessage());
        }
    }
    
    /**
     * Extrai valor de um campo JSON de forma simples
     */
    private String extractJsonValue(String json, String field, String section) {
        try {
            int sectionStart = json.indexOf("\"" + section + "\"");
            if (sectionStart < 0) return null;
            
            int fieldStart = json.indexOf("\"" + field + "\"", sectionStart);
            if (fieldStart < 0) return null;
            
            // Verificar se o campo está dentro da seção (antes do próximo })
            int sectionEnd = json.indexOf("}", sectionStart);
            if (fieldStart > sectionEnd) return null;
            
            int valueStart = json.indexOf(":", fieldStart) + 1;
            int valueEnd = json.indexOf(",", valueStart);
            if (valueEnd < 0 || valueEnd > sectionEnd) {
                valueEnd = json.indexOf("}", valueStart);
            }
            
            String value = json.substring(valueStart, valueEnd).trim();
            // Remover aspas se for string
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            
            return value;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Salva a configuração no arquivo
     * 
     * @param config configuração a ser salva
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveConfiguration(DatabaseConfig config) {
        String configPath = ConfigurationPaths.getDatabaseConfigPath();
        try (FileOutputStream fos = new FileOutputStream(configPath)) {
            Properties props = new Properties();
            props.setProperty("host", config.getHost());
            props.setProperty("port", String.valueOf(config.getPort()));
            props.setProperty("database", config.getDatabase());
            props.setProperty("username", config.getUsername());
            
            // TEMPORÁRIO: Salvar senha em texto plano devido a problemas com criptografia
            // A criptografia baseada em máquina está falhando após mudanças no sistema
            String password = config.getPassword();
            if (password != null && !password.isEmpty()) {
                // Salvar em texto plano temporariamente
                props.setProperty("password", password);
                System.out.println("DEBUG: Senha salva em texto plano (criptografia desabilitada temporariamente)");
            } else {
                props.setProperty("password", "");
            }
            
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
        String configPath = ConfigurationPaths.getDatabaseConfigPath();
        File configFile = new File(configPath);
        
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