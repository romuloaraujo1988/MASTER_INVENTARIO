package com.inventario.sihcp.util.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.time.LocalDateTime;

/**
 * Gerenciador centralizado de notificações
 * Funciona como wrapper sobre JOptionPane existente
 * Adiciona funcionalidades sem quebrar código atual
 */
public class NotificationManager {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationManager.class);
    private static NotificationManager instance;
    private static boolean toastEnabled = true;
    private static boolean errorReportingEnabled = true;
    
    private NotificationManager() {}
    
    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Wrapper para JOptionPane.showMessageDialog que adiciona toast
     * COMPATÍVEL 100% com código existente
     */
    public static void showMessage(Component parent, String message, String title, int messageType) {
        // Comportamento original (não alterado)
        JOptionPane.showMessageDialog(parent, message, title, messageType);
        
        // Funcionalidade adicional (opcional)
        if (toastEnabled) {
            showToastForMessageType(message, messageType);
        }
        
        // Log estruturado
        logNotification(message, title, messageType);
        
        // Relatório de erro (se for erro)
        if (messageType == JOptionPane.ERROR_MESSAGE && errorReportingEnabled) {
            reportError(message, title, null);
        }
    }
    
    /**
     * Método melhorado para notificações com contexto
     */
    public static void showNotification(String message, NotificationType type, Component parent) {
        switch (type) {
            case SUCCESS:
                if (toastEnabled) ToastNotification.showSuccess(message);
                logger.info("SUCCESS: {}", message);
                break;
                
            case WARNING:
                if (toastEnabled) ToastNotification.showWarning(message);
                JOptionPane.showMessageDialog(parent, message, "Aviso", JOptionPane.WARNING_MESSAGE);
                logger.warn("WARNING: {}", message);
                break;
                
            case ERROR:
                if (toastEnabled) ToastNotification.showError(message);
                JOptionPane.showMessageDialog(parent, message, "Erro", JOptionPane.ERROR_MESSAGE);
                logger.error("ERROR: {}", message);
                if (errorReportingEnabled) reportError(message, "Erro", null);
                break;
                
            case INFO:
                if (toastEnabled) ToastNotification.showInfo(message);
                JOptionPane.showMessageDialog(parent, message, "Informação", JOptionPane.INFORMATION_MESSAGE);
                logger.info("INFO: {}", message);
                break;
        }
    }
    
    /**
     * Notificação de sucesso (apenas toast, não bloqueia)
     */
    public static void notifySuccess(String message) {
        if (toastEnabled) {
            ToastNotification.showSuccess(message);
        }
        logger.info("SUCCESS: {}", message);
    }
    
    /**
     * Notificação de erro (apenas toast, não bloqueia)
     */
    public static void notifyError(String message) {
        if (toastEnabled) {
            ToastNotification.showError(message);
        }
        logger.error("ERROR: {}", message);
    }
    
    /**
     * Notificação de aviso (apenas toast, não bloqueia)
     */
    public static void notifyWarning(String message) {
        if (toastEnabled) {
            ToastNotification.showWarning(message);
        }
        logger.warn("WARNING: {}", message);
    }
    
    /**
     * Notificação de informação (apenas toast, não bloqueia)
     */
    public static void notifyInfo(String message) {
        if (toastEnabled) {
            ToastNotification.showInfo(message);
        }
        logger.info("INFO: {}", message);
    }
    
    /**
     * Relatório de erro com contexto
     */
    public static void reportError(String message, String context, Throwable exception) {
        if (!errorReportingEnabled) return;
        
        try {
            ErrorReport report = new ErrorReport(
                message,
                context,
                exception,
                LocalDateTime.now(),
                System.getProperty("user.name"),
                System.getProperty("java.version")
            );
            
            // Salvar localmente
            ErrorReportingService.saveErrorReport(report);
            
            // Log detalhado
            logger.error("Error Report: {} | Context: {} | User: {}", 
                message, context, System.getProperty("user.name"), exception);
                
        } catch (Exception e) {
            logger.error("Falha ao gerar relatório de erro", e);
        }
    }
    
    private static void showToastForMessageType(String message, int messageType) {
        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE:
                ToastNotification.showError(message);
                break;
            case JOptionPane.WARNING_MESSAGE:
                ToastNotification.showWarning(message);
                break;
            case JOptionPane.INFORMATION_MESSAGE:
                ToastNotification.showInfo(message);
                break;
            default:
                ToastNotification.showInfo(message);
        }
    }
    
    private static void logNotification(String message, String title, int messageType) {
        String type = getMessageTypeName(messageType);
        logger.info("Notification [{}]: {} - {}", type, title, message);
    }
    
    private static String getMessageTypeName(int messageType) {
        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE: return "ERROR";
            case JOptionPane.WARNING_MESSAGE: return "WARNING";
            case JOptionPane.INFORMATION_MESSAGE: return "INFO";
            case JOptionPane.QUESTION_MESSAGE: return "QUESTION";
            default: return "UNKNOWN";
        }
    }
    
    // Configurações
    public static void setToastEnabled(boolean enabled) {
        toastEnabled = enabled;
        logger.info("Toast notifications {}", enabled ? "enabled" : "disabled");
    }
    
    public static void setErrorReportingEnabled(boolean enabled) {
        errorReportingEnabled = enabled;
        logger.info("Error reporting {}", enabled ? "enabled" : "disabled");
    }
    
    public static boolean isToastEnabled() {
        return toastEnabled;
    }
    
    public static boolean isErrorReportingEnabled() {
        return errorReportingEnabled;
    }
}
