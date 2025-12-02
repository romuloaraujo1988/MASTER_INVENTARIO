package com.inventario.mobile.server.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega configurações do banco de dados do arquivo configuracao_banco.json
 * Isso permite que o servidor mobile use as mesmas credenciais do desktop
 * 
 * @author Sistema de Inventário
 * @version 1.1.0 - Usa SLF4J ao invés de System.out
 */
public class DatabaseConfigLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigLoader.class);
    
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        
        try {
            Map<String, Object> props = loadFromJsonFile();
            
            if (!props.isEmpty()) {
                MapPropertySource propertySource = new MapPropertySource("configuracao_banco_json", props);
                // Adiciona com alta prioridade (antes das properties do Spring)
                environment.getPropertySources().addFirst(propertySource);
                
                logger.info("✅ Servidor Mobile: Credenciais carregadas de configuracao_banco.json");
                logger.info("   Host: {}", props.get("spring.datasource.url"));
                logger.info("   User: {}", props.get("spring.datasource.username"));
            }
        } catch (Exception e) {
            logger.warn("Aviso: Não foi possível carregar configuracao_banco.json: {}", e.getMessage());
            logger.info("Usando configurações do application-mobile.properties");
        }
    }
    
    private Map<String, Object> loadFromJsonFile() {
        Map<String, Object> props = new HashMap<>();
        
        // Tentar vários caminhos possíveis
        String[] possiblePaths = {
            "configuracao_banco.json",           // Raiz do projeto
            "../configuracao_banco.json",        // Um nível acima
            "config/configuracao_banco.json",    // Pasta config
            System.getProperty("user.home") + "/.inventario/configuracao_banco.json" // Home do usuário
        };
        
        File jsonFile = null;
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists() && f.canRead()) {
                jsonFile = f;
                logger.debug("DatabaseConfigLoader: Encontrado arquivo em: {}", f.getAbsolutePath());
                break;
            }
        }
        
        if (jsonFile == null) {
            logger.debug("DatabaseConfigLoader: Arquivo configuracao_banco.json não encontrado");
            return props;
        }
        
        try {
            // Ler o arquivo
            StringBuilder content = new StringBuilder();
            try (FileReader reader = new FileReader(jsonFile)) {
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    content.append(buffer, 0, read);
                }
            }
            
            String json = content.toString();
            
            // Extrair valores do bloco postgresql
            String host = extractJsonValue(json, "host");
            String database = extractJsonValue(json, "database");
            String user = extractJsonValue(json, "user");
            String password = extractJsonValue(json, "password");
            String portStr = extractJsonValue(json, "port");
            int port = portStr != null ? Integer.parseInt(portStr.trim()) : 5432;
            
            if (host != null && database != null && user != null && password != null) {
                String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                props.put("spring.datasource.url", url);
                props.put("spring.datasource.username", user);
                props.put("spring.datasource.password", password);
                
                logger.debug("DatabaseConfigLoader: Configuração carregada com sucesso!");
            }
            
        } catch (Exception e) {
            logger.warn("DatabaseConfigLoader: Erro ao ler arquivo: {}", e.getMessage());
        }
        
        return props;
    }
    
    /**
     * Extrai um valor de uma string JSON de forma simples
     * Procura dentro do bloco "postgresql"
     */
    private String extractJsonValue(String json, String key) {
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
}
