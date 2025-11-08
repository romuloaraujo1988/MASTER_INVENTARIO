package com.inventario.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ModernConfirmDialog extends JDialog {

    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color CANCEL_COLOR = new Color(149, 165, 166);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    private int result = JOptionPane.NO_OPTION;

    public ModernConfirmDialog(Frame parent, String title, String message) {
        super(parent, title, true);
        initComponents(message);
    }

    private void initComponents(String message) {
        setUndecorated(true);

        int width = Math.min(450, Math.max(350, message.length() * 6 + 100));
        int height = 200;
        setSize(width, height);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CARD_COLOR);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(0, 0, 0, 0)));

        JPanel headerPanel = createHeaderPanel();
        JPanel contentPanel = createContentPanel(message);
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        getRootPane().registerKeyboardAction(
                e -> {
                    result = JOptionPane.NO_OPTION;
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel iconLabel = new JLabel("?");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 12, 0, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createContentPanel(String message) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setBackground(CARD_COLOR);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(null);
        messageArea.setFocusable(false);

        contentPanel.add(messageArea, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 15, 20));

        JButton btnNo = createStyledButton("Não", CANCEL_COLOR, Color.WHITE);
        btnNo.addActionListener(e -> {
            result = JOptionPane.NO_OPTION;
            dispose();
        });

        JButton btnYes = createStyledButton("Sim", PRIMARY_COLOR, Color.WHITE);
        btnYes.addActionListener(e -> {
            result = JOptionPane.YES_OPTION;
            dispose();
        });

        buttonPanel.add(btnNo);
        buttonPanel.add(btnYes);

        getRootPane().setDefaultButton(btnYes);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
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

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(90, 32));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public int getResult() {
        return result;
    }
}
