package com.inventario.sihcp.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utilitário centralizado para exibição de diálogos
 * Elimina duplicação de código de mensagens em múltiplos frames
 */
public class DialogUtils {
    
    /**
     * Exibe mensagem de sucesso
     */
    public static void showSuccess(Component parent, String message) {
        showSuccess(parent, message, "Sucesso");
    }
    
    public static void showSuccess(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Exibe mensagem de erro
     */
    public static void showError(Component parent, String message) {
        showError(parent, message, "Erro");
    }
    
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Exibe mensagem de erro com exceção
     */
    public static void showError(Component parent, String message, Exception e) {
        String fullMessage = message + "\n\nDetalhes: " + e.getMessage();
        showError(parent, fullMessage, "Erro");
        
        // Log do erro
        System.err.println("Erro: " + message);
        e.printStackTrace();
    }
    
    /**
     * Exibe mensagem de aviso
     */
    public static void showWarning(Component parent, String message) {
        showWarning(parent, message, "Aviso");
    }
    
    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Exibe mensagem informativa
     */
    public static void showInfo(Component parent, String message) {
        showInfo(parent, message, "Informação");
    }
    
    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Exibe diálogo de confirmação
     */
    public static boolean showConfirmation(Component parent, String message) {
        return showConfirmation(parent, message, "Confirmar");
    }
    
    public static boolean showConfirmation(Component parent, String message, String title) {
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
     * Exibe diálogo de confirmação com aviso
     */
    public static boolean showWarningConfirmation(Component parent, String message) {
        return showWarningConfirmation(parent, message, "Atenção");
    }
    
    public static boolean showWarningConfirmation(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Exibe diálogo de entrada de texto
     */
    public static String showInput(Component parent, String message) {
        return showInput(parent, message, "Entrada");
    }
    
    public static String showInput(Component parent, String message, String title) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
    }
    
    /**
     * Exibe diálogo de entrada de texto com valor padrão
     */
    public static String showInput(Component parent, String message, String title, String defaultValue) {
        return (String) JOptionPane.showInputDialog(
            parent,
            message,
            title,
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            defaultValue
        );
    }
    
    /**
     * Exibe diálogo de seleção
     */
    public static <T> T showSelection(Component parent, String message, String title, T[] options) {
        return showSelection(parent, message, title, options, options[0]);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T showSelection(Component parent, String message, String title, T[] options, T defaultOption) {
        return (T) JOptionPane.showInputDialog(
            parent,
            message,
            title,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            defaultOption
        );
    }
    
    /**
     * Exibe diálogo de progresso indeterminado
     */
    public static JDialog showProgress(Component parent, String message) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Processando...", true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        dialog.add(panel);
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        return dialog;
    }
    
    /**
     * Exibe toast notification (mensagem temporária)
     */
    public static void showToast(Component parent, String message, int durationMs) {
        JWindow toast = new JWindow();
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 50, 50, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panel.add(label);
        toast.add(panel);
        toast.pack();
        
        // Posicionar no canto inferior direito
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - toast.getWidth() - 20;
        int y = screenSize.height - toast.getHeight() - 60;
        toast.setLocation(x, y);
        
        toast.setVisible(true);
        
        // Auto-fechar após duração
        Timer timer = new Timer(durationMs, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Centraliza janela na tela
     */
    public static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;
        window.setLocation(x, y);
    }
    
    /**
     * Centraliza janela em relação a outra
     */
    public static void centerOnParent(Window window, Component parent) {
        window.setLocationRelativeTo(parent);
    }
}
