package com.inventario.mobile.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC
 * Registra interceptors e outras configurações web
 * 
 * Inclui configuração de timeout para operações assíncronas
 * para evitar erros quando o cliente cancela uma requisição.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private DeviceTrackingInterceptor deviceTrackingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar interceptor de rastreamento de dispositivos
        registry.addInterceptor(deviceTrackingInterceptor)
                .addPathPatterns("/api/mobile/**", "/inventario/api/mobile/**")
                .excludePathPatterns("/api/mobile/v1/connection/**"); // Não rastrear o próprio endpoint de monitoramento
    }
    
    /**
     * Configura suporte a operações assíncronas.
     * 
     * Define timeout de 5 minutos para operações longas como:
     * - Sincronização de patrimônios
     * - Upload de coletas em lote
     * - Geração de relatórios
     * 
     * Quando o cliente cancela a requisição, o servidor trata
     * graciosamente via GlobalExceptionHandler.
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Timeout de 5 minutos (300.000 ms) para operações assíncronas
        configurer.setDefaultTimeout(300000);
    }
}
