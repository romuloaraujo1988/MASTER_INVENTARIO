package com.inventario.util.notification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Sistema de notificação toast não intrusivo
 * Exibe notificações no canto da tela sem bloquear a interface
 */
public class ToastNotification extends JWindow {
    
    public enum Type {
        SUCCESS("✅", new Color(40, 167, 69)),
        WARNING("⚠️", new Color(255, 193, 7)),
        ERROR("❌", new Color(220, 53, 69)),
        INFO("ℹ️", new Color(23, 162, 184));
        
        private final String icon;
        private final Color color;
        
        Type(String icon, Color color) {
            this.icon = icon;
            this.color = color;
        }
        
        public String getIcon() { return icon; }
        public Color getColor() { return color; }
    }
    
    private static final int TOAST_WIDTH = 350;
    private static final int TOAST_HEIGHT = 80;
    private static final int DISPLAY_TIME = 4000; // 4 segundos
    
    public ToastNotification(String message, Type type) {
        setupUI(message, type);
        positionToast();
        showToast();
    }
    
    private void setupUI(String message, Type type) {
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        
        // Painel principal com gradiente
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradiente suave
                GradientPaint gradient = new GradientPaint(
                    0, 0, type.getColor(),
                    0, getHeight(), type.getColor().darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        
        // Ícone
        JLabel iconLabel = new JLabel(type.getIcon());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));
        
        // Mensagem
        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 15));
        
        // Botão fechar
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.add(closeButton, BorderLayout.EAST);
        
        add(mainPanel);
        setSize(TOAST_WIDTH, TOAST_HEIGHT);
    }
    
    private void positionToast() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - TOAST_WIDTH - 20;
        int y = 50; // Canto superior direito
        setLocation(x, y);
    }
    
    private void showToast() {
        setVisible(true);
        
        // Auto-fechar após tempo determinado
        Timer timer = new Timer(DISPLAY_TIME, e -> fadeOut());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void fadeOut() {
        Timer fadeTimer = new Timer(50, new ActionListener() {
            float opacity = 1.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0) {
                    ((Timer) e.getSource()).stop();
                    dispose();
                } else {
                    setOpacity(opacity);
                }
            }
        });
        fadeTimer.start();
    }
    
    // Métodos estáticos para facilitar uso
    public static void showSuccess(String message) {
        SwingUtilities.invokeLater(() -> new ToastNotification(message, Type.SUCCESS));
    }
    
    public static void showWarning(String message) {
        SwingUtilities.invokeLater(() -> new ToastNotification(message, Type.WARNING));
    }
    
    public static void showError(String message) {
        SwingUtilities.invokeLater(() -> new ToastNotification(message, Type.ERROR));
    }
    
    public static void showInfo(String message) {
        SwingUtilities.invokeLater(() -> new ToastNotification(message, Type.INFO));
    }
}
