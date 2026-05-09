package com.inventario.sihcp.mobile.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing) para a API Mobile
 * Permite que o aplicativo Android acesse a API de qualquer origem
 * 
 * NOTA: O bean CorsConfigurationSource está definido exclusivamente em
 * MobileSecurityConfig.java para evitar conflito de beans duplicados.
 * 
 * @author Sistema de Inventário
 * @version 1.1.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ Configurando CORS para API Mobile");
        logger.info("║ Configurando CORS via WebMvcConfigurer (MVC layer)");
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Permite qualquer origem (necessário para Android)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
                .allowCredentials(false) // Desabilitar credentials para permitir *
                .maxAge(3600); // Cache de preflight por 1 hora
    }
}
