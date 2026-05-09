package com.inventario.sihcp.mobile.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.inventario.sihcp.config.DatabaseConfigManager;

/**
 * Configuração geral do servidor mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "mobile.server")
public class MobileServerConfig implements WebMvcConfigurer {
    
    private String version = "1.0.0";
    private boolean enabled = true;
    private int maxRequestSize = 10485760; // 10MB
    private int sessionTimeout = 3600; // 1 hora
    
    /**
     * Bean do DatabaseConfigManager para injeção nos controllers mobile.
     * Necessário porque DatabaseConfigManager não é um @Component
     * (usado também pelo app desktop sem Spring context).
     */
    @Bean
    public DatabaseConfigManager databaseConfigManager() {
        return new DatabaseConfigManager();
    }

    /**
     * Configuração do ObjectMapper para serialização JSON
     */
    @Bean
    public ObjectMapper mobileObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registrar módulo para suporte a Java 8 Time API
        mapper.registerModule(new JavaTimeModule());
        
        // Não incluir propriedades nulas na serialização
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Configurações adicionais
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }
    
    // Getters e Setters para propriedades de configuração
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getMaxRequestSize() {
        return maxRequestSize;
    }
    
    public void setMaxRequestSize(int maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }
    
    public int getSessionTimeout() {
        return sessionTimeout;
    }
    
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}