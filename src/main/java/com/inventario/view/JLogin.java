package com.inventario.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.inventario.service.AutenticacaoServiceDB;
import com.inventario.model.Usuario;

/**
 * Tela de login modernizada e simplificada
 */
public class JLogin extends JFrame {

    // Cores modernas   
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    //private static final Color PLACEHOLDER_COLOR = new Color(149, 165, 166);

    // Componentes
    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private JButton btnLogin;
    private JButton btnCancelar;

    // Serviço de autenticação
    private AutenticacaoServiceDB autenticacaoService;
    private Usuario usuarioLogado;

    public JLogin() {
        // Inicializar serviço de autenticação
        try {
            autenticacaoService = new AutenticacaoServiceDB();
            // Criar usuário admin padrão se não existir
            autenticacaoService.criarUsuarioAdminPadrao();
        } catch (Exception e) {
            System.err.println("Erro ao inicializar serviço de autenticação: " + e.getMessage());
            e.printStackTrace();
        }

        initComponents();
        setupEventHandlers();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("SIHCP - Sistema de Histórico e Coleta Patrimonial - Login");
        setSize(450, 600);
        setResizable(false);
        
        // Definir ícone personalizado
        setIconImages(com.inventario.util.IconManager.getAppIconImages());

        // Painel principal com gradiente
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        0, getHeight(), SECONDARY_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Painel de cabeçalho
        JPanel headerPanel = createHeaderPanel();

        // Card de login
        JPanel loginCard = createLoginCard();

        // Painel de rodapé
        JPanel footerPanel = createFooterPanel();

        // Adicionar componentes ao painel principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(loginCard, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JLabel titleLabel = new JLabel("SIHCP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Sistema de Histórico e Coleta Patrimonial");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);

        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(titleContainer);
        return headerPanel;
    }

    private JPanel createLoginCard() {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(40, 40, 40, 40)));
        cardPanel.setMaximumSize(new Dimension(350, 300));

        // Container principal do card
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Painel dos campos
        JPanel fieldsPanel = createFieldsPanel();

        // Painel dos botões
        JPanel buttonsPanel = createButtonsPanel();

        container.add(fieldsPanel, BorderLayout.CENTER);
        container.add(buttonsPanel, BorderLayout.SOUTH);

        cardPanel.add(container);

        // Centralizar o card
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        centerPanel.add(cardPanel);

        return centerPanel;
    }

    private JPanel createFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        // Campo usuário
        JLabel userLabel = new JLabel("Usuário");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsuario = new JTextField();
        styleTextField(txtUsuario);
        txtUsuario.setPreferredSize(new Dimension(280, 40));
        txtUsuario.setMaximumSize(new Dimension(280, 40));
        txtUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo senha
        JLabel passwordLabel = new JLabel("Senha");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setBorder(new EmptyBorder(20, 0, 8, 0));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtSenha = new JPasswordField();
        styleTextField(txtSenha);
        txtSenha.setPreferredSize(new Dimension(280, 40));
        txtSenha.setMaximumSize(new Dimension(280, 40));
        txtSenha.setAlignmentX(Component.CENTER_ALIGNMENT);

        fieldsPanel.add(userLabel);
        fieldsPanel.add(txtUsuario);
        fieldsPanel.add(passwordLabel);
        fieldsPanel.add(txtSenha);

        return fieldsPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 30));
        buttonsPanel.setOpaque(false);

        btnLogin = createStyledButton("Entrar", PRIMARY_COLOR, Color.WHITE);
        btnCancelar = createStyledButton("Cancelar", DANGER_COLOR, Color.WHITE);

        // Container para centralizar melhor os botões
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonContainer.setOpaque(false);
        buttonContainer.add(btnCancelar);
        buttonContainer.add(btnLogin);

        buttonsPanel.add(buttonContainer);

        return buttonsPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Label de copyright
        JLabel footerLabel = new JLabel("© 2025 IFMT - Instituto Federal de Mato Grosso");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Botão de configuração
        JButton btnConfig = new JButton("⚙ Configurar Banco");
        btnConfig.setFont(new Font("Arial", Font.PLAIN, 11));
        btnConfig.setForeground(Color.WHITE);
        btnConfig.setBackground(new Color(52, 73, 94));
        btnConfig.setBorderPainted(false);
        btnConfig.setFocusPainted(false);
        btnConfig.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfig.setToolTipText("Configurar conexão com banco de dados");
        
        btnConfig.addActionListener(e -> abrirConfiguracaoBanco());
        
        // Adicionar hover effect
        btnConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnConfig.setBackground(new Color(44, 62, 80));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnConfig.setBackground(new Color(52, 73, 94));
            }
        });

        footerPanel.add(footerLabel, BorderLayout.CENTER);
        footerPanel.add(btnConfig, BorderLayout.EAST);
        
        return footerPanel;
    }
    
    /**
     * Abre o dialog de configuração do banco de dados
     */
    private void abrirConfiguracaoBanco() {
        com.inventario.util.ConfiguracaoBancoUtil.abrirDialogConfiguracao(this);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                new EmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);

        // Efeito de foco
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        new EmptyBorder(8, 12, 8, 12)));
            }

            @Override
            public void focusLost(FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                        new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
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

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(100, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void setupEventHandlers() {
        // Enter no campo de senha faz login
        txtSenha.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        // Enter no campo de usuário move para senha
        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtSenha.requestFocus();
                }
            }
        });

        // Ação do botão login
        btnLogin.addActionListener(e -> performLogin());

        // Ação do botão cancelar
        btnCancelar.addActionListener(e -> System.exit(0));
    }

    private void performLogin() {
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (usuario.isEmpty() || senha.isEmpty()) {
            showMessage("Por favor, preencha todos os campos.", "Campos obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Autenticar usando o banco de dados
            usuarioLogado = autenticacaoService.autenticar(usuario, senha);

            if (usuarioLogado != null) {
                showMessage("Login realizado com sucesso!\nBem-vindo, " + usuarioLogado.getNomeCompleto() + "!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // Fechar a janela de login
                dispose();

                // Abrir o sistema principal
                abrirSistemaPrincipal();
            } else {
                showMessage("Usuário ou senha incorretos.\nVerifique suas credenciais e tente novamente.",
                        "Erro de autenticação", JOptionPane.ERROR_MESSAGE);
                txtSenha.setText("");
                txtUsuario.requestFocus();
            }
        } catch (Exception e) {
            System.err.println("Erro durante autenticação: " + e.getMessage());
            e.printStackTrace();
            showMessage("Erro interno do sistema.\nTente novamente ou contate o administrador.",
                    "Erro do Sistema", JOptionPane.ERROR_MESSAGE);
            txtSenha.setText("");
            txtUsuario.requestFocus();
        }
    }

    private void abrirSistemaPrincipal() {
        try {
            SwingUtilities.invokeLater(() -> {
                // Verificar se o usuário é um coletor
                if (usuarioLogado.isColetor()) {
                    // Redirecionar coletores diretamente para o frame de coleta
                    try {
                        ColetaFrame_v2 coletaFrame = new ColetaFrame_v2(usuarioLogado);
                        coletaFrame.setVisible(true);
                    } catch (Exception e) {
                        System.err.println("Erro ao abrir o frame de coleta: " + e.getMessage());
                        e.printStackTrace();
                        showMessage("Erro ao abrir o sistema de coleta.", "Erro", JOptionPane.ERROR_MESSAGE);

                        // Fallback: abrir o sistema principal normal
                        MainFrame mainFrame = new MainFrame(usuarioLogado);
                        mainFrame.setVisible(true);
                    }
                } else {
                    // Para outros perfis, abrir o sistema principal normal
                    MainFrame mainFrame = new MainFrame(usuarioLogado);
                    mainFrame.setVisible(true);
                }
            });
        } catch (Exception e) {
            System.err.println("Erro ao abrir o sistema principal: " + e.getMessage());
            e.printStackTrace();
            showMessage("Erro ao abrir o sistema principal.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMessage(String message, String title, int messageType) {
        // Usar diálogo moderno customizado
        ModernDialog.showMessage(this, message, title, messageType);
    }

    public static void main(String args[]) {
        // Verificar se já existe uma instância do sistema em execução
        if (!com.inventario.util.SingleInstanceLock.tryLock()) {
            // Outra instância já está rodando
            com.inventario.util.SingleInstanceLock.showInstanceAlreadyRunningMessage();
            System.exit(0);
            return;
        }

        // Configurar look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Usar look and feel padrão se houver erro
        }

        SwingUtilities.invokeLater(() -> {
            new JLogin().setVisible(true);
        });
    }
}