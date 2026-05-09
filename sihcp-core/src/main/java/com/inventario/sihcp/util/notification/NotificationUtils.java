package com.inventario.sihcp.util.notification;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Classe utilitária para migração gradual do sistema de notificações
 * Mantém compatibilidade total com JOptionPane existente
 */
public class NotificationUtils {
    
    /**
     * Exibe mensagem de erro (compatível com JOptionPane)
     * Adiciona funcionalidades extras opcionalmente
     */
    public static void showErrorMessage(Component parent, String message) {
        // Comportamento atual (mantido)
        JOptionPane.showMessageDialog(parent, message, "Erro", JOptionPane.ERROR_MESSAGE);
        
        // Funcionalidade adicional (opcional)
        if (NotificationManager.isToastEnabled()) {
            NotificationManager.notifyError(message);
        }
        
        // Relatório de erro
        if (NotificationManager.isErrorReportingEnabled()) {
            NotificationManager.reportError(message, "Error Dialog", null);
        }
    }
    
    /**
     * Exibe mensagem de erro com exceção
     */
    public static void showErrorMessage(Component parent, String message, Throwable exception) {
        // Comportamento atual (mantido)
        String fullMessage = message + (exception != null ? "\n" + exception.getMessage() : "");
        JOptionPane.showMessageDialog(parent, fullMessage, "Erro", JOptionPane.ERROR_MESSAGE);
        
        // Funcionalidade adicional (opcional)
        if (NotificationManager.isToastEnabled()) {
            NotificationManager.notifyError(message);
        }
        
        // Relatório de erro com stack trace
        if (NotificationManager.isErrorReportingEnabled()) {
            NotificationManager.reportError(message, "Error Dialog", exception);
        }
    }
    
    /**
     * Exibe mensagem de aviso (compatível com JOptionPane)
     */
    public static void showWarningMessage(Component parent, String message) {
        // Comportamento atual (mantido)
        JOptionPane.showMessageDialog(parent, message, "Aviso", JOptionPane.WARNING_MESSAGE);
        
        // Funcionalidade adicional (opcional)
        if (NotificationManager.isToastEnabled()) {
            NotificationManager.notifyWarning(message);
        }
    }
    
    /**
     * Exibe mensagem de informação (compatível com JOptionPane)
     */
    public static void showInfoMessage(Component parent, String message) {
        // Comportamento atual (mantido)
        JOptionPane.showMessageDialog(parent, message, "Informação", JOptionPane.INFORMATION_MESSAGE);
        
        // Funcionalidade adicional (opcional)
        if (NotificationManager.isToastEnabled()) {
            NotificationManager.notifyInfo(message);
        }
    }
    
    /**
     * Exibe notificação de sucesso (novo - apenas toast, não bloqueia)
     */
    public static void showSuccessMessage(String message) {
        NotificationManager.notifySuccess(message);
    }
    
    /**
     * Exibe notificação de sucesso com dialog opcional
     */
    public static void showSuccessMessage(Component parent, String message, boolean showDialog) {
        if (showDialog) {
            JOptionPane.showMessageDialog(parent, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
        NotificationManager.notifySuccess(message);
    }
    
    /**
     * Exibe diálogo de confirmação (compatível com JOptionPane)
     */
    public static boolean showConfirmDialog(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            parent, 
            message, 
            title, 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Exibe diálogo de confirmação com opções customizadas
     */
    public static int showConfirmDialog(Component parent, String message, String title, 
                                       int optionType, int messageType) {
        return JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
    }
}
