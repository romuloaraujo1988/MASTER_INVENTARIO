package com.inventario.view.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Fábrica de botões com estilo padronizado baseado no frame de login.
 * Centraliza a criação de botões para manter consistência visual em todo o
 * sistema.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ButtonStyleFactory {

    // Cores padronizadas do sistema (baseadas no JLogin)
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    public static final Color SECONDARY_COLOR = new Color(46, 204, 113);
    public static final Color DANGER_COLOR = new Color(231, 76, 60);
    public static final Color WARNING_COLOR = new Color(243, 156, 18);
    public static final Color INFO_COLOR = new Color(52, 152, 219);
    public static final Color LIGHT_COLOR = new Color(236, 240, 241);
    public static final Color DARK_COLOR = new Color(52, 73, 94);

    /**
     * Cria um botão primário (azul)
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, Color.WHITE);
    }

    /**
     * Cria um botão de sucesso (verde)
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR, Color.WHITE);
    }

    /**
     * Cria um botão de perigo (vermelho)
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR, Color.WHITE);
    }

    /**
     * Cria um botão de aviso (laranja)
     */
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, WARNING_COLOR, Color.WHITE);
    }

    /**
     * Cria um botão de informação (azul claro)
     */
    public static JButton createInfoButton(String text) {
        return createStyledButton(text, INFO_COLOR, Color.WHITE);
    }

    /**
     * Cria um botão secundário (cinza claro)
     */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, LIGHT_COLOR, DARK_COLOR);
    }

    /**
     * Cria um botão pequeno primário
     */
    public static JButton createSmallPrimaryButton(String text) {
        JButton button = createStyledButton(text, PRIMARY_COLOR, Color.WHITE);
        button.setPreferredSize(new Dimension(80, 30));
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        return button;
    }

    /**
     * Cria um botão pequeno de perigo
     */
    public static JButton createSmallDangerButton(String text) {
        JButton button = createStyledButton(text, DANGER_COLOR, Color.WHITE);
        button.setPreferredSize(new Dimension(80, 30));
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        return button;
    }

    /**
     * Cria um botão pequeno de sucesso
     */
    public static JButton createSmallSuccessButton(String text) {
        JButton button = createStyledButton(text, SECONDARY_COLOR, Color.WHITE);
        button.setPreferredSize(new Dimension(80, 30));
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        return button;
    }

    /**
     * Método principal para criar botões estilizados (baseado no JLogin)
     */
    public static JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(100, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    /**
     * Cria um botão com ícone e texto
     */
    public static JButton createIconButton(String text, String icon, Color bgColor, Color textColor) {
        JButton button = createStyledButton(text, bgColor, textColor);

        if (icon != null && !icon.isEmpty()) {
            button.setText(icon + " " + text);
        }

        return button;
    }

    /**
     * Cria um botão de menu principal (estilo do MainFrame)
     */
    public static JButton createMenuButton(String text, String icon) {
        JButton button = new JButton() {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Cor de fundo com gradiente
                Color baseColor = PRIMARY_COLOR;
                Color hoverColor = PRIMARY_COLOR.brighter();
                Color topColor = isHovered ? hoverColor : baseColor;
                Color bottomColor = isHovered ? hoverColor.darker() : baseColor.darker();

                GradientPaint gradient = new GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2d.dispose();

                // Desenhar ícone e texto manualmente
                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int centerX = getWidth() / 2;
                int startY = 25;

                // Desenhar ícone no topo
                if (icon != null && !icon.isEmpty()) {
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                    g2d.setColor(Color.WHITE);
                    FontMetrics fmIcon = g2d.getFontMetrics();
                    int iconWidth = fmIcon.stringWidth(icon);
                    int iconX = centerX - (iconWidth / 2);
                    g2d.drawString(icon, iconX, startY);
                    startY += 24;
                }

                // Desenhar texto abaixo do ícone com sombra
                g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

                // Sombra do texto
                g2d.setColor(new Color(0, 0, 0, 80));
                FontMetrics fmText = g2d.getFontMetrics();
                int textWidth = fmText.stringWidth(text);
                int textX = centerX - (textWidth / 2);
                g2d.drawString(text, textX + 1, startY + 1);

                // Texto principal
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, textX, startY);

                g2d.dispose();
            }
        };

        button.setPreferredSize(new Dimension(180, 170));
        button.setMinimumSize(new Dimension(180, 170));
        button.setMaximumSize(new Dimension(180, 170));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito hover
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, true);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }

            public void mouseExited(MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, false);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }
        });

        return button;
    }
}