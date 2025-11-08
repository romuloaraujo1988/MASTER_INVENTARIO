package com.inventario.config;

import com.inventario.util.notification.NotificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuração do sistema de notificações
 * Carrega configurações do application.properties
 */
@Configuration
public class NotificationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationConfig.class);
    
    @Value("${app.notification.toast.enabled:true}")
    private boolean toastEnabled;
    
    @Value("${app.notification.error-reporting.enabled:true}")
    private boolean errorReportingEnabled;
    
    @PostConstruct
    public void init() {
        NotificationManager.setToastEnabled(toastEnabled);
        NotificationManager.setErrorReportingEnabled(errorReportingEnabled);
        
        logger.info("Sistema de Notificação inicializado:");
        logger.info("  - Toast notifications: {}", toastEnabled ? "ATIVADO" : "DESATIVADO");
        logger.info("  - Error reporting: {}", errorReportingEnabled ? "ATIVADO" : "DESATIVADO");
    }
}
