package com.inventario.util;

import javax.swing.*;
import java.awt.*;

/**
 * Classe para visualizar os ícones do sistema
 * Execute o main para ver uma prévia dos ícones
 */
public class IconPreview {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Preview dos Ícones - SIHCP");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(10, 10));
            
            // Definir ícone na própria janela
            frame.setIconImages(IconManager.getAppIconImages());
            
            JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(new Color(240, 240, 240));
            
            // Adicionar ícones em diferentes tamanhos
            int[] sizes = {16, 32, 48, 64, 96, 128};
            for (int size : sizes) {
                JPanel iconPanel = createIconPanel(size);
                panel.add(iconPanel);
            }
            
            // Título
            JLabel title = new JLabel("Ícones do Sistema SIHCP", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 20));
            title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Informações
            JLabel info = new JLabel(
                "<html><center>" +
                "Ícone padrão: Círculo azul com 'SI HC'<br>" +
                "Usado em todas as janelas do sistema<br>" +
                "Múltiplos tamanhos para diferentes resoluções" +
                "</center></html>",
                SwingConstants.CENTER
            );
            info.setFont(new Font("Arial", Font.PLAIN, 12));
            info.setForeground(Color.GRAY);
            info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            frame.add(title, BorderLayout.NORTH);
            frame.add(panel, BorderLayout.CENTER);
            frame.add(info, BorderLayout.SOUTH);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    
    private static JPanel createIconPanel(int size) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Criar ícone do tamanho especificado
        Image scaledImage = IconManager.getAppImage().getScaledInstance(
            size, size, Image.SCALE_SMOOTH
        );
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Label com o tamanho
        JLabel sizeLabel = new JLabel(size + "x" + size + " px", SwingConstants.CENTER);
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(sizeLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Mostra comparação entre ícone padrão e alternativo
     */
    public static void showComparison() {
        JFrame frame = new JFrame("Comparação de Ícones");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2, 20, 20));
        frame.setIconImages(IconManager.getAppIconImages());
        
        // Painel do ícone padrão
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.setBorder(BorderFactory.createTitledBorder("Ícone Padrão (SIHCP)"));
        panel1.setBackground(Color.WHITE);
        JLabel icon1 = new JLabel(IconManager.getAppIcon());
        icon1.setHorizontalAlignment(SwingConstants.CENTER);
        panel1.add(icon1, BorderLayout.CENTER);
        
        // Painel do ícone alternativo
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.setBorder(BorderFactory.createTitledBorder("Ícone Alternativo (Inventário)"));
        panel2.setBackground(Color.WHITE);
        JLabel icon2 = new JLabel(IconManager.createInventoryIcon());
        icon2.setHorizontalAlignment(SwingConstants.CENTER);
        panel2.add(icon2, BorderLayout.CENTER);
        
        frame.add(panel1);
        frame.add(panel2);
        
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
