package com.inventario.sihcp.view.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

/**
 * Fábrica de botões modernos replicando o estilo usado em JLogin e MainFrame.
 * Uso:
 *   JButton salvar = ModernButtons.primary("Salvar");
 *   JButton cancelar = ModernButtons.danger("Cancelar");
 */
public final class ModernButtons {
    private ModernButtons() {}

    // Paleta padrão (similar ao JLogin/MainFrame)
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color MUTED_COLOR = new Color(108, 117, 125);

    public static JButton primary(String text) {
        return roundedSolid(text, PRIMARY_COLOR, Color.WHITE, 8, new Dimension(120, 40));
    }

    public static JButton secondary(String text) {
        return roundedSolid(text, SECONDARY_COLOR, Color.WHITE, 8, new Dimension(120, 40));
    }

    public static JButton danger(String text) {
        return roundedSolid(text, DANGER_COLOR, Color.WHITE, 8, new Dimension(120, 40));
    }

    public static JButton muted(String text) {
        return roundedSolid(text, MUTED_COLOR, Color.WHITE, 8, new Dimension(120, 40));
    }
    
    public static JButton info(String text) {
        return roundedSolid(text, new Color(23, 162, 184), Color.WHITE, 8, new Dimension(120, 40));
    }

    public static JButton smallPrimary(String text) {
        return roundedSolid(text, PRIMARY_COLOR, Color.WHITE, 6, new Dimension(120, 30));
    }

    /**
     * Botão com gradiente suave, usado em tiles do MainFrame.
     */
    public static JButton tile(String text, String iconEmoji) {
        JButton button = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isHovered = Boolean.TRUE.equals(getClientProperty("isHovered"));

                Color baseColor = PRIMARY_COLOR;
                Color hoverColor = new Color(41, 128, 185);
                Color topColor = isHovered ? hoverColor : baseColor;
                Color bottomColor = isHovered ? hoverColor.darker() : baseColor.darker();

                GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.dispose();

                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int centerX = getWidth() / 2;
                int startY = 25;

                if (iconEmoji != null && !iconEmoji.isEmpty()) {
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                    g2d.setColor(Color.WHITE);
                    FontMetrics fmIcon = g2d.getFontMetrics();
                    int iconWidth = fmIcon.stringWidth(iconEmoji);
                    int iconX = centerX - (iconWidth / 2);
                    g2d.drawString(iconEmoji, iconX, startY);
                    startY += 24;
                }

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.setColor(new Color(0, 0, 0, 80));
                FontMetrics fmText = g2d.getFontMetrics();
                int textWidth = fmText.stringWidth(text);
                int textX = centerX - (textWidth / 2);
                g2d.drawString(text, textX + 1, startY + 1);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, textX, startY);
                g2d.dispose();
            }
        };

        button.setPreferredSize(new Dimension(180, 170));
        button.setMinimumSize(new Dimension(180, 170));
        button.setMaximumSize(new Dimension(180, 170));
        baseSetup(button);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.putClientProperty("isHovered", true);
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.putClientProperty("isHovered", false);
                button.repaint();
            }
        });

        return button;
    }

    private static JButton roundedSolid(String text, Color bgColor, Color textColor, int radius, Dimension size) {
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

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2d.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setPreferredSize(size);
        baseSetup(button);
        return button;
    }

    private static void baseSetup(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}

