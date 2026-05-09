package com.inventario.sihcp.view;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.KeyEvent;

public class ModernDialog extends JDialog {

    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(52, 152, 219);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    public static final int INFO = 4;
    public static final int QUESTION = 5;

    private ModernDialog(Frame parent, String title, String message, int messageType, boolean showCancel) {
        super(parent, title, true);
        initComponents(message, messageType, showCancel);
    }

    private void initComponents(String message, int messageType, boolean showCancel) {
        setUndecorated(true);

        // Calcular tamanho baseado no conteúdo
        int width = Math.min(400, Math.max(320, message.length() * 6 + 80));
        int height = 200; // Mais compacto
        setSize(width, height);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CARD_COLOR);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(0, 0, 0, 0)));

        JPanel headerPanel = createHeaderPanel(messageType);
        JPanel contentPanel = createContentPanel(message, messageType);
        JPanel buttonPanel = createButtonPanel(messageType, showCancel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        getRootPane().registerKeyboardAction(
                e -> {
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createHeaderPanel(int messageType) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(getColorForType(messageType));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20)); // Mais compacto

        JLabel iconLabel = new JLabel(getIconForType(messageType));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); // Menor
        iconLabel.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15)); // Menor
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 12, 0, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createContentPanel(String message, int messageType) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 25, 15, 25)); // Mais compacto

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Menor
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

    private JPanel createButtonPanel(int messageType, boolean showCancel) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12)); // Mais compacto
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 15, 20)); // Mais compacto

        if (showCancel) {
            JButton btnCancel = createStyledButton("Cancelar", new Color(149, 165, 166), Color.WHITE);
            btnCancel.addActionListener(e -> {
                dispose();
            });
            buttonPanel.add(btnCancel);
        }

        String okText = messageType == QUESTION ? "Sim" : "OK";
        JButton btnOk = createStyledButton(okText, getColorForType(messageType), Color.WHITE);
        btnOk.addActionListener(e -> {
            dispose();
        });
        buttonPanel.add(btnOk);

        getRootPane().setDefaultButton(btnOk);

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

        button.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Menor
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(90, 32)); // Mais compacto
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private Color getColorForType(int messageType) {
        switch (messageType) {
            case SUCCESS:
                return SUCCESS_COLOR;
            case ERROR:
                return DANGER_COLOR;
            case WARNING:
                return WARNING_COLOR;
            case QUESTION:
                return PRIMARY_COLOR;
            case INFO:
            default:
                return INFO_COLOR;
        }
    }

    private String getIconForType(int messageType) {
        switch (messageType) {
            case SUCCESS:
                return "OK";
            case ERROR:
                return "X";
            case WARNING:
                return "!";
            case QUESTION:
                return "?";
            case INFO:
            default:
                return "ℹ";
        }
    }

    public static void showMessage(Component parent, String message, String title, int messageType) {
        int type = convertJOptionPaneType(messageType);
        ModernDialog dialog = new ModernDialog(getFrame(parent), title, message, type, false);
        dialog.setVisible(true);
    }

    private static int convertJOptionPaneType(int jOptionPaneType) {
        switch (jOptionPaneType) {
            case JOptionPane.ERROR_MESSAGE:
                return ERROR;
            case JOptionPane.WARNING_MESSAGE:
                return WARNING;
            case JOptionPane.INFORMATION_MESSAGE:
                return INFO;
            case JOptionPane.QUESTION_MESSAGE:
                return QUESTION;
            default:
                return INFO;
        }
    }

    private static Frame getFrame(Component parent) {
        if (parent instanceof Frame) {
            return (Frame) parent;
        }
        if (parent != null) {
            return (Frame) SwingUtilities.getWindowAncestor(parent);
        }
        return null;
    }
}
