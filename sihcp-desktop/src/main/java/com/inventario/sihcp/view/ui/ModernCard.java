package com.inventario.sihcp.view.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Componente de Card Moderno para o Dashboard
 * Com sombras, bordas arredondadas e design premium.
 */
public class ModernCard extends JPanel {

    private final String title;
    private String value;
    private final Color accentColor;
    private final String icon;

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblIcon;

    public ModernCard(String title, String value, Color accentColor, String icon) {
        this.title = title;
        this.value = value;
        this.accentColor = accentColor;
        this.icon = icon;

        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(250, 120));

        // Label do Ícone
        lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblIcon.setBounds(20, 20, 50, 50);
        add(lblIcon);

        // Label do Título
        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(107, 114, 128));
        lblTitle.setBounds(80, 25, 160, 20);
        add(lblTitle);

        // Label do Valor
        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(new Color(17, 24, 39));
        lblValue.setBounds(80, 45, 160, 40);
        add(lblValue);
    }

    public void setValue(String newValue) {
        this.value = newValue;
        lblValue.setText(newValue);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Desenhar Sombra
        g2.setColor(new Color(0, 0, 0, 15));
        g2.fill(new RoundRectangle2D.Float(5, 5, w - 10, h - 10, 20, 20));

        // Desenhar Fundo do Card
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 5, h - 5, 20, 20));

        // Desenhar Borda de Sotaque (Accent) na lateral esquerda
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 6, h - 5, 20, 20);
        g2.fillRect(3, 0, 10, h - 5); // Complemento para a borda não ficar redonda demais na esquerda

        // Desenhar Borda sutil
        g2.setColor(new Color(229, 231, 235));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 5, h - 5, 20, 20));

        g2.dispose();
    }
}
