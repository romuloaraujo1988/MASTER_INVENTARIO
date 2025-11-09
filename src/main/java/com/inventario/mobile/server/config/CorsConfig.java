package com.inventario.mobile.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing) para a API Mobile
 * Permite que o aplicativo Android acesse a API de qualquer origem
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ Configurando CORS para API Mobile");
        logger.info("║ Permitindo todas as origens para aplicativo Android");
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Permite qualquer origem (necessário para Android)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
                .allowCredentials(false) // Desabilitar credentials para permitir *
                .maxAge(3600); // Cache de preflight por 1 hora
    }
    
    /**
     * Configuração adicional de CORS para Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Criando CorsConfigurationSource para Spring Security");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir origens específicas para mobile (incluindo o IP configurado)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://192.168.10.107:8081",
            "https://192.168.10.107:8081",
            "http://localhost:8081",
            "https://localhost:8081",
            "http://10.0.2.2:8081",
            "https://10.0.2.2:8081"
        ));
        
        // Permitir todos os métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Permitir todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Expor headers importantes
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"
        ));
        
        // Desabilitar credentials para permitir allowedOriginPatterns("*")
        configuration.setAllowCredentials(false);
        
        // Cache de preflight por 1 hora
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        logger.info("CorsConfigurationSource configurado com sucesso");
        logger.info("- Origens permitidas: * (todas)");
        logger.info("- Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        logger.info("- Headers permitidos: * (todos)");
        logger.info("- Credenciais: permitidas");
        
        return source;
    }
}
