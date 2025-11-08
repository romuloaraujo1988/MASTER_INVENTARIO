package com.inventario.mobile.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC
 * Registra interceptors e outras configurações web
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
}
