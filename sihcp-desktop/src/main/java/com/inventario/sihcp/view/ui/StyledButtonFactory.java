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
 * Fábrica de botões estilizados (gradiente, cantos arredondados, suporte a emoji)
 * no padrão visual usado pelas telas de coleta.
 *
 * <p>Extraído de {@code ColetaFrame_v2.createStyledButton} para permitir reuso
 * entre os painéis de coleta sem duplicação. O comportamento visual é idêntico
 * ao original.</p>
 */
public final class StyledButtonFactory {

    private StyledButtonFactory() {
        // Utilitário - não instanciável
    }

    /**
     * Cria um botão estilizado.
     *
     * @param text      Texto do botão (pode conter emoji no início)
     * @param baseColor Cor base do botão
     * @return JButton estilizado
     */
    public static JButton create(String text, Color baseColor) {
        final boolean[] isHovered = {false}; // Mutable wrapper for hover state

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                Color hoverColor = baseColor.brighter();
                Color topColor = isHovered[0] ? hoverColor : baseColor;
                Color bottomColor = isHovered[0] ? hoverColor.darker() : baseColor.darker();

                if (!isEnabled()) {
                    topColor = new Color(180, 180, 180);
                    bottomColor = new Color(150, 150, 150);
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2d.dispose();

                // Desenhar texto com suporte a emojis
                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Separar emoji do texto
                String emoji = "";
                String textOnly = text;

                // Detectar emoji no início do texto
                if (text.length() > 0) {
                    int codePoint = text.codePointAt(0);
                    // Verificar se é um emoji (vários ranges Unicode)
                    boolean isEmoji = (codePoint >= 0x1F300 && codePoint <= 0x1F9FF) || // Emojis diversos
                            (codePoint >= 0x2600 && codePoint <= 0x26FF) || // Símbolos diversos
                            (codePoint >= 0x2700 && codePoint <= 0x27BF) || // Dingbats
                            (codePoint >= 0x231A && codePoint <= 0x23FF) || // Símbolos técnicos
                            (codePoint >= 0x2B50 && codePoint <= 0x2BFF); // Estrelas e outros

                    if (isEmoji) {
                        int emojiEnd = Character.charCount(codePoint);
                        emoji = text.substring(0, emojiEnd).trim();
                        if (emojiEnd < text.length()) {
                            textOnly = text.substring(emojiEnd).trim();
                        } else {
                            textOnly = "";
                        }
                    }
                }

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                if (!emoji.isEmpty()) {
                    // Renderizar emoji com fonte especial que suporta emoticons nativamente
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    FontMetrics fmEmoji = g2d.getFontMetrics();
                    int emojiWidth = fmEmoji.stringWidth(emoji);

                    // Renderizar texto ao lado do emoji
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fmText = g2d.getFontMetrics();
                    int textWidth = fmText.stringWidth(textOnly);

                    int totalWidth = emojiWidth + (textOnly.isEmpty() ? 0 : 4 + textWidth);
                    int startX = centerX - (totalWidth / 2);

                    // Desenhar emoji
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    g2d.setColor(isEnabled() ? Color.WHITE : new Color(200, 200, 200));
                    g2d.drawString(emoji, startX, centerY + fmEmoji.getAscent() / 2);

                    // Desenhar texto se existir
                    if (!textOnly.isEmpty()) {
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        int textX = startX + emojiWidth + 4;
                        int textY = centerY + fmText.getAscent() / 2;

                        // Sombra do texto
                        if (isEnabled()) {
                            g2d.setColor(new Color(0, 0, 0, 80));
                            g2d.drawString(textOnly, textX + 1, textY + 1);
                        }

                        // Texto principal
                        g2d.setColor(isEnabled() ? Color.WHITE : new Color(200, 200, 200));
                        g2d.drawString(textOnly, textX, textY);
                    }
                } else {
                    // Sem emoji, renderizar apenas texto centralizado
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textX = centerX - (textWidth / 2);
                    int textY = centerY + fm.getAscent() / 2;

                    // Sombra do texto
                    if (isEnabled()) {
                        g2d.setColor(new Color(0, 0, 0, 80));
                        g2d.drawString(text, textX + 1, textY + 1);
                    }

                    // Texto principal
                    g2d.setColor(isEnabled() ? Color.WHITE : new Color(200, 200, 200));
                    g2d.drawString(text, textX, textY);
                }

                g2d.dispose();
            }

            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                repaint();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 38));
        button.setMinimumSize(new Dimension(100, 38));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    isHovered[0] = true;
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                isHovered[0] = false;
                button.repaint();
            }
        });

        return button;
    }
}
