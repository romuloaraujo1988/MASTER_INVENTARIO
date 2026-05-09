package com.inventario.sihcp.view.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Fábrica de ComboBoxes modernos replicando o estilo usado no sistema.
 * <p>Uso:</p>
 * <pre>
 *   JComboBox&lt;String&gt; combo = ModernComboBox.primary(items);
 *   JComboBox&lt;String&gt; combo = ModernComboBox.secondary(items);
 * </pre>
 */
public final class ModernComboBox {
    private ModernComboBox() {}

    // Paleta padrão (similar ao ModernButtons)
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color MUTED_COLOR = new Color(108, 117, 125);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(206, 212, 218);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);

    public static <T> JComboBox<T> primary(T[] items) {
        return createStyledComboBox(items, PRIMARY_COLOR, Color.WHITE, new Dimension(200, 35));
    }

    public static <T> JComboBox<T> secondary(T[] items) {
        return createStyledComboBox(items, SECONDARY_COLOR, Color.WHITE, new Dimension(200, 35));
    }

    public static <T> JComboBox<T> danger(T[] items) {
        return createStyledComboBox(items, DANGER_COLOR, Color.WHITE, new Dimension(200, 35));
    }

    public static <T> JComboBox<T> muted(T[] items) {
        return createStyledComboBox(items, MUTED_COLOR, Color.WHITE, new Dimension(200, 35));
    }

    public static <T> JComboBox<T> standard(T[] items) {
        return createStyledComboBox(items, BACKGROUND_COLOR, TEXT_COLOR, new Dimension(200, 35));
    }

    public static <T> JComboBox<T> small(T[] items) {
        return createStyledComboBox(items, BACKGROUND_COLOR, TEXT_COLOR, new Dimension(150, 28));
    }

    private static <T> JComboBox<T> createStyledComboBox(T[] items, Color bgColor, Color textColor, Dimension size) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        
        comboBox.setUI(new ModernComboBoxUI(bgColor, textColor));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setForeground(textColor);
        comboBox.setBackground(bgColor);
        comboBox.setPreferredSize(size);
        comboBox.setMaximumSize(size);
        comboBox.setBorder(new EmptyBorder(5, 10, 5, 10));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Renderer personalizado para os itens
        comboBox.setRenderer(new ModernComboBoxRenderer(bgColor, textColor));
        
        return comboBox;
    }

    /**
     * UI personalizada para o ComboBox
     */
    private static class ModernComboBoxUI extends BasicComboBoxUI {
        private final Color bgColor;
        private final Color textColor;
        private boolean isHovered = false;

        public ModernComboBoxUI(Color bgColor, Color textColor) {
            this.bgColor = bgColor;
            this.textColor = textColor;
        }

        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                public void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Fundo do botão
                    g2d.setColor(isHovered ? bgColor.brighter() : bgColor);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
                    
                    // Desenhar seta
                    g2d.setColor(textColor);
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int[] xPoints = {centerX - 4, centerX + 4, centerX};
                    int[] yPoints = {centerY - 2, centerY - 2, centerY + 3};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    
                    g2d.dispose();
                }
            };
            
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color currentBg = isHovered ? bgColor.brighter() : bgColor;
            g2d.setColor(currentBg);
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            
            // Borda sutil
            g2d.setColor(BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 8, 8);
            
            g2d.dispose();
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected void configurePopup() {
                    super.configurePopup();
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                    setBackground(Color.WHITE);
                }
            };
        }

        @Override
        protected void installListeners() {
            super.installListeners();
            
            comboBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    comboBox.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    comboBox.repaint();
                }
            });
        }
    }

    /**
     * Renderer personalizado para os itens do ComboBox
     */
    private static class ModernComboBoxRenderer extends DefaultListCellRenderer {
        private final Color bgColor;
       
        public ModernComboBoxRenderer(Color bgColor, Color textColor) {
            this.bgColor = bgColor;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            
            if (isSelected) {
                setBackground(bgColor);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(TEXT_COLOR);
            }
            
            return this;
        }
    }
}