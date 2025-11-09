package com.inventario.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Gerenciador de ícones da aplicação
 * Fornece ícones personalizados para janelas e componentes
 */
public class IconManager {
    
    private static ImageIcon appIcon = null;
    private static Image appImage = null;
    
    /**
     * Obtém o ícone da aplicação
     */
    public static ImageIcon getAppIcon() {
        if (appIcon == null) {
            appIcon = createDefaultIcon();
        }
        return appIcon;
    }
    
    /**
     * Obtém a imagem do ícone da aplicação (para setIconImage)
     */
    public static Image getAppImage() {
        if (appImage == null) {
            appImage = getAppIcon().getImage();
        }
        return appImage;
    }
    
    /**
     * Define o ícone para uma janela
     */
    public static void setWindowIcon(Window window) {
        if (window != null) {
            window.setIconImage(getAppImage());
        }
    }
    
    /**
     * Define o ícone para um JFrame
     */
    public static void setFrameIcon(JFrame frame) {
        if (frame != null) {
            frame.setIconImage(getAppImage());
        }
    }
    
    /**
     * Define o ícone para um JDialog
     */
    public static void setDialogIcon(JDialog dialog) {
        if (dialog != null) {
            dialog.setIconImage(getAppImage());
        }
    }
    
    /**
     * Tenta carregar ícone de arquivo, se não existir cria um padrão
     */
    private static ImageIcon createDefaultIcon() {
        // Tentar carregar de recursos
        try {
            URL iconUrl = IconManager.class.getResource("/icons/app-icon.png");
            if (iconUrl != null) {
                return new ImageIcon(iconUrl);
            }
        } catch (Exception e) {
            // Ignorar e criar ícone padrão
        }
        
        // Criar ícone padrão programaticamente
        return createProgrammaticIcon();
    }
    
    /**
     * Cria um ícone padrão programaticamente
     * Ícone com as letras "SIHCP" em um círculo azul
     */
    private static ImageIcon createProgrammaticIcon() {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Ativar anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Desenhar círculo de fundo com gradiente
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(52, 152, 219),
            size, size, new Color(41, 128, 185)
        );
        g2d.setPaint(gradient);
        g2d.fillOval(2, 2, size - 4, size - 4);
        
        // Desenhar borda
        g2d.setColor(new Color(41, 128, 185));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(2, 2, size - 4, size - 4);
        
        // Desenhar texto "SI"
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "SI";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int x = (size - textWidth) / 2;
        int y = (size - textHeight) / 2 + textHeight - 2;
        g2d.drawString(text, x, y);
        
        // Desenhar subtexto "HC"
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        fm = g2d.getFontMetrics();
        String subtext = "HC";
        textWidth = fm.stringWidth(subtext);
        x = (size - textWidth) / 2;
        y = y + fm.getHeight() - 2;
        g2d.drawString(subtext, x, y);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    /**
     * Cria um ícone alternativo com símbolo de inventário
     */
    public static ImageIcon createInventoryIcon() {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fundo circular verde
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(46, 204, 113),
            size, size, new Color(39, 174, 96)
        );
        g2d.setPaint(gradient);
        g2d.fillOval(2, 2, size - 4, size - 4);
        
        // Desenhar símbolo de checklist
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Retângulo do clipboard
        g2d.drawRoundRect(18, 12, 28, 40, 4, 4);
        
        // Checkmarks
        int[] x1 = {22, 22, 22};
        int[] y1 = {22, 32, 42};
        int[] x2 = {26, 26, 26};
        int[] y2 = {26, 36, 46};
        int[] x3 = {34, 34, 34};
        int[] y3 = {18, 28, 38};
        
        for (int i = 0; i < 3; i++) {
            g2d.drawLine(x1[i], y1[i], x2[i], y2[i]);
            g2d.drawLine(x2[i], y2[i], x3[i], y3[i]);
        }
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    /**
     * Cria múltiplos tamanhos do ícone para diferentes resoluções
     */
    public static java.util.List<Image> getAppIconImages() {
        java.util.List<Image> icons = new java.util.ArrayList<>();
        
        // Adicionar ícones em diferentes tamanhos
        icons.add(createScaledIcon(16));
        icons.add(createScaledIcon(32));
        icons.add(createScaledIcon(48));
        icons.add(createScaledIcon(64));
        icons.add(createScaledIcon(128));
        
        return icons;
    }
    
    /**
     * Cria um ícone escalado para um tamanho específico
     */
    private static Image createScaledIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        // Desenhar círculo de fundo com gradiente
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(52, 152, 219),
            size, size, new Color(41, 128, 185)
        );
        g2d.setPaint(gradient);
        g2d.fillOval(1, 1, size - 2, size - 2);
        
        // Desenhar borda
        g2d.setColor(new Color(41, 128, 185));
        g2d.setStroke(new BasicStroke(Math.max(1, size / 32)));
        g2d.drawOval(1, 1, size - 2, size - 2);
        
        // Calcular tamanho da fonte baseado no tamanho do ícone
        int fontSize1 = size / 3;
        int fontSize2 = size / 5;
        
        // Desenhar texto "SI"
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize1));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "SI";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int x = (size - textWidth) / 2;
        int y = (size - textHeight) / 2 + textHeight - (size / 16);
        g2d.drawString(text, x, y);
        
        // Desenhar subtexto "HC"
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize2));
        fm = g2d.getFontMetrics();
        String subtext = "HC";
        textWidth = fm.stringWidth(subtext);
        x = (size - textWidth) / 2;
        y = y + fm.getHeight() - (size / 32);
        g2d.drawString(subtext, x, y);
        
        g2d.dispose();
        
        return image;
    }
    
    /**
     * Define ícones para todas as janelas da aplicação
     */
    public static void setIconForAllWindows() {
        // Definir ícones padrão para todas as janelas
        try {
            // Para sistemas que suportam múltiplos tamanhos
            java.util.List<Image> icons = getAppIconImages();
            
            // Aplicar a todas as janelas abertas
            for (Window window : Window.getWindows()) {
                if (window instanceof Frame) {
                    ((Frame) window).setIconImages(icons);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao definir ícones: " + e.getMessage());
        }
    }
}
