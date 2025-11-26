package com.inventario.view;

import com.inventario.model.Usuario;
import com.inventario.offline.OfflineManager;
import com.inventario.offline.StatusBarPanel;
import com.inventario.offline.SyncStatusManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Timer;
import java.util.TimerTask;

// Imports para integração do servidor mobile
// import com.inventario.MobileApiApplication;
// import org.springframework.boot.builder.SpringApplicationBuilder;
// import org.springframework.context.ConfigurableApplicationContext;

/**
 * Frame principal da aplicação Sistema de Inventário
 * Exibe o menu principal e gerencia a navegação entre módulos
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MainFrame extends JFrame {

    private Usuario usuarioLogado;
    private JLabel lblUsuarioLogado;
    private JMenuBar menuBar;
    private JLabel lblStatusConexao;
    private JButton btnModoOffline;
    private JButton btnTentarOnline;
    private OfflineManager offlineManager;
    private StatusBarPanel statusBarPanel;
    private SyncStatusManager syncStatusManager;
    private Timer statusTimer;

    // Controle do servidor mobile
    private Process servidorMobileProcess;
    private boolean servidorMobileAtivo = false;
    private JLabel lblServidorMobile;
    // private ConfigurableApplicationContext mobileServerContext;

    public MainFrame(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.offlineManager = OfflineManager.getInstance();
        this.syncStatusManager = SyncStatusManager.getInstance();

        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupOfflineControls();
        setupSyncStatusListeners();

        setTitle("SIHCP - Sistema de Histórico e Coleta Patrimonial");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // Definir ícone personalizado
        setIconImages(com.inventario.util.IconManager.getAppIconImages());

        // Centralizar na tela
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // Criar menu bar simples
        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 240, 240));

        // Menu Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setFont(new Font("Arial", Font.BOLD, 14));
        JMenuItem itemAlterarSenha = new JMenuItem("Alterar Minha Senha");
        itemAlterarSenha.setFont(new Font("Arial", Font.PLAIN, 13));
        itemAlterarSenha.addActionListener(e -> alterarMinhaSenha());
        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.setFont(new Font("Arial", Font.PLAIN, 13));
        itemSair.addActionListener(e -> sairSistema());
        menuArquivo.add(itemAlterarSenha);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);

        // Verificar se é usuário CONSULTA - só tem acesso ao dashboard
        boolean isConsulta = usuarioLogado.getPerfil().name().equals("CONSULTA");
        boolean isAdmin = usuarioLogado.getPerfil().name().equals("ADMIN");

        // Menu Administração (apenas para administradores) - SEGUNDO na ordem
        JMenu menuAdmin = null;
        if (isAdmin) {
            menuAdmin = new JMenu("Administração");
            menuAdmin.setFont(new Font("Arial", Font.BOLD, 14));
            JMenuItem itemUsuarios = new JMenuItem("Gerenciar Usuários");
            itemUsuarios.setFont(new Font("Arial", Font.PLAIN, 13));
            itemUsuarios.addActionListener(e -> abrirGerenciamentoUsuarios());
            JMenuItem itemConfigBanco = new JMenuItem("Configuração do Banco");
            itemConfigBanco.setFont(new Font("Arial", Font.PLAIN, 13));
            itemConfigBanco.addActionListener(e -> abrirConfiguracaoBanco());
            JMenuItem itemServidorMobile = new JMenuItem("Servidor Mobile");
            itemServidorMobile.setFont(new Font("Arial", Font.PLAIN, 13));
            itemServidorMobile.addActionListener(e -> abrirGerenciamentoServidorMobile());

            menuAdmin.add(itemUsuarios);
            menuAdmin.add(itemConfigBanco);
            menuAdmin.addSeparator();
            menuAdmin.add(itemServidorMobile);
        }

        // Menu Inventário e Relatórios (não disponível para CONSULTA) - TERCEIRO na
        // ordem
        JMenu menuInventario = null;
        JMenu menuRelatorios = null;
        if (!isConsulta) {
            // Menu Inventário
            menuInventario = new JMenu("Inventário");
            menuInventario.setFont(new Font("Arial", Font.BOLD, 14));
            JMenuItem itemGerenciarInventarios = new JMenuItem("Gerenciar Inventários");
            itemGerenciarInventarios.setFont(new Font("Arial", Font.PLAIN, 13));
            itemGerenciarInventarios.addActionListener(e -> abrirGerenciamentoInventarios());
            JMenuItem itemSalas = new JMenuItem("Gerenciar Salas");
            itemSalas.setFont(new Font("Arial", Font.PLAIN, 13));
            itemSalas.addActionListener(e -> abrirGerenciamentoSalas());
            JMenuItem itemSetores = new JMenuItem("Gerenciar Setores");
            itemSetores.setFont(new Font("Arial", Font.PLAIN, 13));
            itemSetores.addActionListener(e -> abrirGerenciamentoSetores());
            JMenuItem itemCampus = new JMenuItem("Gerenciar Campus");
            itemCampus.setFont(new Font("Arial", Font.PLAIN, 13));
            itemCampus.addActionListener(e -> abrirGerenciamentoCampus());
            JMenuItem itemResponsaveis = new JMenuItem("Gerenciar Responsáveis");
            itemResponsaveis.setFont(new Font("Arial", Font.PLAIN, 13));
            itemResponsaveis.addActionListener(e -> abrirGerenciamentoResponsaveis());
            JMenuItem itemPatrimonio = new JMenuItem("Patrimônio");
            itemPatrimonio.setFont(new Font("Arial", Font.PLAIN, 13));
            itemPatrimonio.addActionListener(e -> abrirPatrimonio());
            JMenuItem itemColeta = new JMenuItem("Coleta de Dados");
            itemColeta.setFont(new Font("Arial", Font.PLAIN, 13));
            itemColeta.addActionListener(e -> abrirColeta());
            JMenuItem itemImportarCSV = new JMenuItem("Importar Excel do SUAP");
            itemImportarCSV.setFont(new Font("Arial", Font.PLAIN, 13));
            itemImportarCSV.addActionListener(e -> abrirImportacaoCSV());

            menuInventario.add(itemGerenciarInventarios);
            menuInventario.addSeparator();
            menuInventario.add(itemSalas);
            menuInventario.add(itemSetores);
            menuInventario.add(itemCampus);
            menuInventario.add(itemResponsaveis);
            menuInventario.add(itemPatrimonio);
            menuInventario.add(itemColeta);
            menuInventario.addSeparator();
            menuInventario.add(itemImportarCSV);

            // Menu Relatórios
            menuRelatorios = new JMenu("Relatórios");
            menuRelatorios.setFont(new Font("Arial", Font.BOLD, 14));
            JMenuItem itemRelInventario = new JMenuItem("Relatório de Inventário");
            itemRelInventario.setFont(new Font("Arial", Font.PLAIN, 13));
            itemRelInventario.addActionListener(e -> abrirRelatorioInventario());
            menuRelatorios.add(itemRelInventario);

            JMenuItem itemRelDivergencias = new JMenuItem("Relatório de Divergências");
            itemRelDivergencias.setFont(new Font("Arial", Font.PLAIN, 13));
            itemRelDivergencias.addActionListener(e -> abrirRelatorioDivergencias());
            menuRelatorios.add(itemRelDivergencias);

            menuRelatorios.addSeparator();

            // Item SIADS
            JMenuItem itemSiads = new JMenuItem("Exportar SIADS");
            itemSiads.setFont(new Font("Arial", Font.PLAIN, 13));
            itemSiads.setToolTipText("Exportar dados para o Sistema Integrado de Administração de Serviços");
            itemSiads.addActionListener(e -> abrirExportacaoSiads());
            menuRelatorios.add(itemSiads);
        }

        // Menu Dashboard (disponível para todos os perfis) - QUARTO na ordem
        JMenu menuDashboard = new JMenu("Dashboard");
        menuDashboard.setFont(new Font("Arial", Font.BOLD, 14));
        JMenuItem itemDashboardColeta = new JMenuItem("Dashboard de Coleta");
        itemDashboardColeta.setFont(new Font("Arial", Font.PLAIN, 13));
        itemDashboardColeta.addActionListener(e -> abrirDashboardColeta());

        JMenuItem itemStatusSalas = new JMenuItem("Status das Salas");
        itemStatusSalas.setFont(new Font("Arial", Font.PLAIN, 13));
        itemStatusSalas.addActionListener(e -> abrirStatusSalas());

        menuDashboard.add(itemDashboardColeta);
        menuDashboard.add(itemStatusSalas);

        // Menu Sistema (para controles offline/online)
        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.setFont(new Font("Arial", Font.BOLD, 14));

        JMenuItem itemImportarDados = new JMenuItem("Importar Dados Offline");
        itemImportarDados.setFont(new Font("Arial", Font.PLAIN, 13));
        itemImportarDados.addActionListener(e -> abrirImportacaoDadosOffline());

        JMenuItem itemForcarOffline = new JMenuItem("Forçar Modo Offline");
        itemForcarOffline.setFont(new Font("Arial", Font.PLAIN, 13));
        itemForcarOffline.addActionListener(e -> forcarModoOffline());

        JMenuItem itemTentarOnline = new JMenuItem("Tentar Conectar Online");
        itemTentarOnline.setFont(new Font("Arial", Font.PLAIN, 13));
        itemTentarOnline.addActionListener(e -> tentarModoOnline());

        JMenuItem itemStatusSistema = new JMenuItem("Status do Sistema");
        itemStatusSistema.setFont(new Font("Arial", Font.PLAIN, 13));
        itemStatusSistema.addActionListener(e -> mostrarStatusSistema());

        JMenuItem itemIniciarServidorMobile = new JMenuItem("Iniciar Servidor Mobile");
        itemIniciarServidorMobile.setFont(new Font("Arial", Font.PLAIN, 13));
        itemIniciarServidorMobile.addActionListener(e -> iniciarServidorMobile());

        JMenuItem itemPararServidorMobile = new JMenuItem("Parar Servidor Mobile");
        itemPararServidorMobile.setFont(new Font("Arial", Font.PLAIN, 13));
        itemPararServidorMobile.addActionListener(e -> pararServidorMobile());

        JMenuItem itemMonitorMobile = new JMenuItem("Monitor de Usuários Mobile");
        itemMonitorMobile.setFont(new Font("Arial", Font.PLAIN, 13));
        itemMonitorMobile.addActionListener(e -> abrirMonitorMobile());

        menuSistema.add(itemImportarDados);
        menuSistema.addSeparator();
        menuSistema.add(itemForcarOffline);
        menuSistema.add(itemTentarOnline);
        menuSistema.addSeparator();
        menuSistema.add(itemIniciarServidorMobile);
        menuSistema.add(itemPararServidorMobile);
        menuSistema.add(itemMonitorMobile);
        menuSistema.addSeparator();
        menuSistema.add(itemStatusSistema);

        // Menu Ajuda
        JMenu menuAjuda = new JMenu("Ajuda");
        menuAjuda.setFont(new Font("Arial", Font.BOLD, 14));
        JMenuItem itemSobre = new JMenuItem("Sobre");
        itemSobre.setFont(new Font("Arial", Font.PLAIN, 13));
        itemSobre.addActionListener(e -> mostrarSobre());
        menuAjuda.add(itemSobre);

        // Adicionar menus à barra na ordem: Arquivo, Administração, Inventário,
        // Relatórios, Dashboard, Sistema, Ajuda
        menuBar.add(menuArquivo);

        // Administração (apenas ADMIN)
        if (menuAdmin != null) {
            menuBar.add(menuAdmin);
        }

        // Inventário (não disponível para CONSULTA)
        if (menuInventario != null) {
            menuBar.add(menuInventario);
        }

        // Relatórios (não disponível para CONSULTA) - ANTES do Dashboard
        if (menuRelatorios != null) {
            menuBar.add(menuRelatorios);
        }

        // Dashboard (disponível para todos)
        menuBar.add(menuDashboard);

        // Sistema (disponível para todos)
        menuBar.add(menuSistema);

        // Ajuda (disponível para todos)
        menuBar.add(menuAjuda);

        // Label do usuário logado
        lblUsuarioLogado = new JLabel("Usuário: " + usuarioLogado.getNomeCompleto() +
                " (" + usuarioLogado.getPerfil().name() + ")");
        lblUsuarioLogado.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUsuarioLogado.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsuarioLogado.setForeground(new Color(50, 50, 50));

        setJMenuBar(menuBar);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Painel superior moderno com gradiente
        JPanel panelTop = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(52, 152, 219),
                        0, getHeight(), new Color(41, 128, 185));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelTop.setOpaque(false);
        panelTop.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Painel direito com usuário e status do servidor mobile
        JPanel panelDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelDireito.setOpaque(false);
        
        // Verificar se é administrador
        boolean isAdmin = usuarioLogado.getPerfil().name().equals("ADMIN");
        
        // Indicador do servidor mobile (apenas para ADMIN)
        if (isAdmin) {
            lblServidorMobile = new JLabel();
            // Usar fonte que suporta emoticons nativamente
            lblServidorMobile.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
            lblServidorMobile.setOpaque(true);
            lblServidorMobile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            atualizarIndicadorServidorMobile();
            panelDireito.add(lblServidorMobile);
        }
        
        // Ajustar cor do label do usuário para branco
        lblUsuarioLogado.setForeground(Color.WHITE);
        
        panelDireito.add(lblUsuarioLogado);
        panelTop.add(panelDireito, BorderLayout.EAST);

        // Painel central com dashboard
        JPanel panelCenter = createDashboardPanel();

        // Criar StatusBarPanel (substitui o painel inferior antigo)
        statusBarPanel = new StatusBarPanel();
        statusBarPanel.addSincronizarListener(e -> executarSincronizacao());

        add(panelTop, BorderLayout.NORTH);
        add(panelCenter, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // Título principal
        JLabel lblTitulo = new JLabel("SIHCP - Sistema de Histórico e Coleta Patrimonial");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(44, 62, 80));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        panel.add(lblTitulo, gbc);

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Gestão de Patrimônio");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(127, 140, 141));
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 30, 10); // Mais espaço abaixo do subtítulo
        panel.add(lblSubtitulo, gbc);

        // Verificar se é usuário CONSULTA
        boolean isConsulta = usuarioLogado.getPerfil().name().equals("CONSULTA");

        // Container para os botões
        JPanel buttonContainer = new JPanel();
        buttonContainer.setOpaque(false);

        if (isConsulta) {
            buttonContainer.setLayout(new GridBagLayout()); // Centralizar único botão
            JButton btnDashboard = createSimpleButton("Dashboard", "📈");
            btnDashboard.addActionListener(e -> abrirDashboardColeta());
            buttonContainer.add(btnDashboard);

            // Adicionar mensagem informativa
            JLabel lblInfo = new JLabel(
                    "<html><center>Você tem acesso apenas ao Dashboard.<br>Para mais funcionalidades, entre em contato com o administrador.</center></html>");
            lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
            lblInfo.setForeground(new Color(100, 100, 100));
            lblInfo.setHorizontalAlignment(SwingConstants.CENTER);

            gbc.gridy = 2;
            panel.add(buttonContainer, gbc);

            gbc.gridy = 3;
            gbc.insets = new Insets(20, 10, 10, 10);
            panel.add(lblInfo, gbc);

        } else {
            // Grid para os botões (3 colunas)
            GridLayout gridLayout = new GridLayout(0, 3, 20, 20);
            buttonContainer.setLayout(gridLayout);

            // Botão Inventários
            JButton btnInventarios = createSimpleButton("Inventários", "📋");
            btnInventarios.addActionListener(e -> abrirGerenciamentoInventarios());
            buttonContainer.add(btnInventarios);

            // Botão Salas
            JButton btnSalas = createSimpleButton("Salas", "🚪");
            btnSalas.addActionListener(e -> abrirGerenciamentoSalas());
            buttonContainer.add(btnSalas);

            // Botão Setores
            JButton btnSetores = createSimpleButton("Setores", "🏢");
            btnSetores.addActionListener(e -> abrirGerenciamentoSetores());
            buttonContainer.add(btnSetores);

            // Botão Campus
            JButton btnCampus = createSimpleButton("Campus", "🏫");
            btnCampus.addActionListener(e -> abrirGerenciamentoCampus());
            buttonContainer.add(btnCampus);

            // Botão Responsáveis
            JButton btnResponsaveis = createSimpleButton("Responsáveis", "👥");
            btnResponsaveis.addActionListener(e -> abrirGerenciamentoResponsaveis());
            buttonContainer.add(btnResponsaveis);

            // Botão Patrimônio
            JButton btnPatrimonio = createSimpleButton("Patrimônio", "📦");
            btnPatrimonio.addActionListener(e -> abrirPatrimonio());
            buttonContainer.add(btnPatrimonio);

            // Botão Coleta
            JButton btnColeta = createSimpleButton("Coleta", "📝");
            btnColeta.addActionListener(e -> abrirColeta());
            buttonContainer.add(btnColeta);

            // Botão Importar CSV
            JButton btnImportarCSV = createSimpleButton("Importar CSV/XLS", "📥");
            btnImportarCSV.addActionListener(e -> abrirImportacaoCSV());
            buttonContainer.add(btnImportarCSV);

            // Botão Dashboard de Coleta
            JButton btnDashboard = createSimpleButton("Dashboard", "📈");
            btnDashboard.addActionListener(e -> abrirDashboardColeta());
            buttonContainer.add(btnDashboard);

            // Botão Relatórios
            JButton btnRelatorios = createSimpleButton("Relatórios", "📊");
            btnRelatorios.addActionListener(e -> abrirRelatorioInventario());
            buttonContainer.add(btnRelatorios);

            // Botão Usuários (apenas para admin)
            if (usuarioLogado.getPerfil().name().equals("ADMIN")) {
                JButton btnUsuarios = createSimpleButton("Usuários", "👤");
                btnUsuarios.addActionListener(e -> abrirGerenciamentoUsuarios());
                buttonContainer.add(btnUsuarios);
            }

            gbc.gridy = 2;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE; // Não esticar o container
            gbc.anchor = GridBagConstraints.CENTER; // Centralizar
            panel.add(buttonContainer, gbc);
        }

        return panel;
    }

    private JButton createSimpleButton(String text, String icon) {
        JButton button = new JButton() {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Cor de fundo com gradiente
                Color baseColor = new Color(52, 152, 219);
                Color hoverColor = new Color(41, 128, 185);
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
                int startY = 25; // Começar ainda mais no topo

                // Desenhar ícone no topo
                if (icon != null && !icon.isEmpty()) {
                    // Usar fonte que suporta emoticons nativamente
                    g2d.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
                    g2d.setColor(Color.WHITE);
                    FontMetrics fmIcon = g2d.getFontMetrics();
                    int iconWidth = fmIcon.stringWidth(icon);
                    int iconX = centerX - (iconWidth / 2);
                    g2d.drawString(icon, iconX, startY);
                    startY += 24; // Espaço ainda menor após o ícone
                }

                // Desenhar texto abaixo do ícone com sombra para destaque
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));

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

        // Tamanhos controlados - preferido e máximo
        button.setPreferredSize(new Dimension(200, 150));
        button.setMinimumSize(new Dimension(150, 120));
        button.setMaximumSize(new Dimension(250, 180)); // Limita o crescimento máximo
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, true);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
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

    private JButton createModernDashboardButton(String titulo, String descricao, Color cor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradiente suave
                GradientPaint gradient = new GradientPaint(0, 0, cor, 0, getHeight(),
                        new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 180));
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));
            }
        };

        button.setLayout(new BorderLayout());
        button.setPreferredSize(new Dimension(220, 140));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Painel interno para o conteúdo
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblDescricao = new JLabel("<html><center>" + descricao + "</center></html>");
        lblDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDescricao.setForeground(new Color(255, 255, 255, 200));
        lblDescricao.setHorizontalAlignment(SwingConstants.CENTER);

        conteudo.add(lblTitulo, BorderLayout.CENTER);
        conteudo.add(lblDescricao, BorderLayout.SOUTH);
        button.add(conteudo, BorderLayout.CENTER);

        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setPreferredSize(new Dimension(225, 145));
                button.revalidate();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setPreferredSize(new Dimension(220, 140));
                button.revalidate();
            }
        });

        return button;
    }

    private void setupEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sairSistema();
            }
        });
    }

    private JPanel createBottomPanel() {
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBackground(new Color(44, 62, 80));
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Painel esquerdo - Status da conexão
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(44, 62, 80));

        lblStatusConexao = new JLabel("Sistema de Inventário - Inicializando...");
        lblStatusConexao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusConexao.setForeground(new Color(236, 240, 241));
        statusPanel.add(lblStatusConexao);

        // Painel direito - Controles offline/online
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(new Color(44, 62, 80));

        btnModoOffline = createModernSmallButton("Forçar Offline");
        btnModoOffline.addActionListener(e -> forcarModoOffline());

        btnTentarOnline = createModernSmallButton("Tentar Online");
        btnTentarOnline.addActionListener(e -> tentarModoOnline());

        controlPanel.add(btnModoOffline);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnTentarOnline);

        panelBottom.add(statusPanel, BorderLayout.WEST);
        panelBottom.add(controlPanel, BorderLayout.EAST);

        return panelBottom;
    }

    private JButton createModernSmallButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = new Color(52, 152, 219);
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

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 30));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void setupOfflineControls() {
        // Configurar listener para mudanças de estado offline
        offlineManager.addStateListener(new OfflineManager.OfflineStateListener() {
            @Override
            public void onStateChanged(OfflineManager.OfflineState oldState, OfflineManager.OfflineState newState) {
                SwingUtilities.invokeLater(() -> updateConnectionStatus(newState));
            }
        });

        // Iniciar timer para atualizar status periodicamente
        statusTimer = new Timer(true);
        statusTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Verifica se está em estado INITIALIZING e força transição se conectividade
                    // detectada
                    if (offlineManager.getCurrentState() == OfflineManager.OfflineState.INITIALIZING) {
                        offlineManager.forceOnlineState();
                    }
                    updateConnectionStatus(offlineManager.getCurrentState());
                });
            }
        }, 1000, 5000); // Atualiza a cada 5 segundos

        // Atualizar status inicial e forçar transição se necessário
        if (offlineManager.getCurrentState() == OfflineManager.OfflineState.INITIALIZING) {
            offlineManager.forceOnlineState();
        }
        updateConnectionStatus(offlineManager.getCurrentState());
    }

    private void updateConnectionStatus(OfflineManager.OfflineState state) {
        if (lblStatusConexao == null)
            return;

        // Adicionar indicador do servidor mobile ao status
        String statusMobile = servidorMobileAtivo ? " | Mobile: ATIVO" : "";

        switch (state) {
            case ONLINE:
                lblStatusConexao.setText("Sistema de Inventário - Online" + statusMobile);
                lblStatusConexao.setForeground(new Color(0, 128, 0));
                btnModoOffline.setEnabled(true);
                btnTentarOnline.setEnabled(false);
                break;
            case OFFLINE:
                lblStatusConexao.setText("Sistema de Inventário - Modo Offline" + statusMobile);
                lblStatusConexao.setForeground(new Color(255, 140, 0));
                btnModoOffline.setEnabled(false);
                btnTentarOnline.setEnabled(true);
                break;
            case SYNCING:
                lblStatusConexao.setText("Sistema de Inventário - Sincronizando..." + statusMobile);
                lblStatusConexao.setForeground(new Color(0, 100, 200));
                btnModoOffline.setEnabled(false);
                btnTentarOnline.setEnabled(false);
                break;
            case ERROR:
                lblStatusConexao.setText("Sistema de Inventário - Erro de Conexão" + statusMobile);
                lblStatusConexao.setForeground(new Color(200, 0, 0));
                btnModoOffline.setEnabled(true);
                btnTentarOnline.setEnabled(true);
                break;
            case INITIALIZING:
            default:
                lblStatusConexao.setText("Sistema de Inventário - Inicializando..." + statusMobile);
                lblStatusConexao.setForeground(new Color(100, 100, 100));
                btnModoOffline.setEnabled(false);
                btnTentarOnline.setEnabled(false);
                break;
        }
    }

    // Métodos de navegação
    private void abrirGerenciamentoInventarios() {
        try {
            InventarioFrame inventarioFrame = new InventarioFrame();
            inventarioFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de inventários: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirGerenciamentoSalas() {
        try {
            SalaFrame salaFrame = new SalaFrame();
            salaFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de salas: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirGerenciamentoSetores() {
        try {
            SetorFrame setorFrame = new SetorFrame();
            setorFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de setores: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirGerenciamentoCampus() {
        try {
            CampusFrame campusFrame = new CampusFrame();
            campusFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de campus: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirGerenciamentoResponsaveis() {
        try {
            ResponsavelFrame responsavelFrame = new ResponsavelFrame();
            responsavelFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de responsáveis: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirPatrimonio() {
        try {
            PatrimonioFrame patrimonioFrame = new PatrimonioFrame();
            patrimonioFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de patrimônio: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirColeta() {
        try {
            System.out.println("===========================================");
            System.out.println("DEBUG MainFrame: Iniciando abertura do ColetaFrame_v2...");
            System.out.println("DEBUG MainFrame: Usuário logado: " + 
                (usuarioLogado != null ? usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getPerfil() + ")" : "null"));
            System.out.println("===========================================");

            System.out.println("DEBUG MainFrame: Chamando construtor ColetaFrame_v2...");
            ColetaFrame_v2 coletaFrame = new ColetaFrame_v2(usuarioLogado, this);

            System.out.println("DEBUG MainFrame: ColetaFrame_v2 criado com sucesso!");
            System.out.println("DEBUG MainFrame: Tornando frame visível...");
            coletaFrame.setVisible(true);
            System.out.println("DEBUG MainFrame: ColetaFrame_v2 exibido com sucesso!");
            System.out.println("===========================================");

        } catch (Exception e) {
            System.err.println("===========================================");
            System.err.println("ERRO FATAL ao abrir ColetaFrame_v2:");
            System.err.println("Tipo: " + e.getClass().getName());
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println("===========================================");
            e.printStackTrace();

            // Mostrar stack trace completo no dialog
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Erro ao abrir módulo de coleta:\n\n");
            errorMsg.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n\n");
            errorMsg.append("Stack Trace:\n");

            for (StackTraceElement element : e.getStackTrace()) {
                errorMsg.append("  ").append(element.toString()).append("\n");
                if (errorMsg.length() > 1000) {
                    errorMsg.append("  ...\n");
                    break;
                }
            }

            if (e.getCause() != null) {
                errorMsg.append("\nCausa: ").append(e.getCause().getClass().getName())
                        .append(": ").append(e.getCause().getMessage());
            }

            JTextArea textArea = new JTextArea(errorMsg.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Erro ao Abrir Coleta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirImportacaoCSV() {
        try {
            // Passar o usuário logado para validação de perfil
            ImportacaoCSVFrame importacaoFrame = new ImportacaoCSVFrame(usuarioLogado);
            importacaoFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir módulo de importação CSV/XLS: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirRelatorioInventario() {
        try {
            RelatorioFrame relatorioFrame = new RelatorioFrame();
            relatorioFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir relatórios: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirRelatorioDivergencias() {
        try {
            RelatorioDivergenciasFrame divergenciasFrame = new RelatorioDivergenciasFrame();
            divergenciasFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir relatório de divergências: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirExportacaoSiads() {
        try {
            com.inventario.siads.view.SiadsExportDialog dialog = new com.inventario.siads.view.SiadsExportDialog(this);
            dialog.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir exportação SIADS: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void abrirDashboardColeta() {
        try {
            DashboardColetaFrame dashboardFrame = new DashboardColetaFrame();
            dashboardFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir dashboard de coleta: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirGerenciamentoUsuarios() {
        try {
            UsuarioFrame usuarioFrame = new UsuarioFrame();
            usuarioFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento de usuários: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirGerenciamentoServidorMobile() {
        try {
            MobileServerPanel.showDialog(this);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir gerenciamento do servidor mobile: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirMonitorMobile() {
        try {
            MobileMonitorFrameV2 monitorFrame = new MobileMonitorFrameV2();
            monitorFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir monitor de dispositivos mobile: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void abrirConfiguracaoBanco() {
        try {
            ConfiguracaoBancoDialog dialog = new ConfiguracaoBancoDialog(this);
            dialog.setVisible(true);
            ModernDialog.showMessage(this,
                    "Funcionalidade de configuração do banco em desenvolvimento.",
                    "Informação", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir configuração do banco: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarSobre() {
        // Usar JLabel com HTML para melhor formatação
        JLabel label = new JLabel(com.inventario.util.VersionInfo.getVersionInfoHtml());
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this,
                label,
                "Sobre o Sistema",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void forcarModoOffline() {
        try {
            // Verificar se existem dados locais antes de ativar modo offline
            if (!verificarDadosLocaisDisponiveis()) {
                int opcao = JOptionPane.showConfirmDialog(this,
                    "⚠️ ATENÇÃO: Não foram encontrados dados locais!\n\n" +
                    "Para trabalhar em modo offline, você precisa primeiro importar\n" +
                    "os dados do servidor para o banco de dados local.\n\n" +
                    "Deseja importar os dados agora?",
                    "Dados Locais Não Encontrados",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (opcao == JOptionPane.YES_OPTION) {
                    // Abrir dialog de importação
                    abrirImportacaoDadosOffline();
                }
                return;
            }
            
            int opcao = showModernConfirmDialog(
                    "Deseja forçar o sistema para modo offline?\n" +
                            "Isso desconectará o sistema da rede e operará apenas localmente.\n" +
                            "Todas as operações serão salvas localmente até a reconexão.",
                    "Confirmar Modo Offline");

            if (opcao == JOptionPane.YES_OPTION) {
                // Força modo offline usando o novo método
                offlineManager.forceOfflineMode();

                ModernDialog.showMessage(this,
                        "Sistema foi forçado para modo offline.\n" +
                                "Todas as operações serão salvas localmente.\n" +
                                "Use 'Tentar Conectar Online' para voltar ao modo online.",
                        "Modo Offline Ativado",
                        JOptionPane.INFORMATION_MESSAGE);

                // Atualiza status imediatamente
                updateConnectionStatus(offlineManager.getCurrentState());
            }
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao ativar modo offline: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tentarModoOnline() {
        try {
            // Verificar se há dados locais para sincronizar
            boolean temDadosLocais = verificarDadosLocaisDisponiveis();
            
            String mensagem = "Deseja tentar reconectar ao servidor?\n" +
                    "O sistema verificará a conectividade e sincronizará os dados.";
            
            if (!temDadosLocais) {
                mensagem = "Deseja tentar reconectar ao servidor?\n\n" +
                        "⚠️ Aviso: Não foram encontrados dados locais.\n" +
                        "Recomenda-se importar os dados após reconectar.";
            }
            
            int opcao = showModernConfirmDialog(mensagem, "Confirmar Reconexão");

            if (opcao == JOptionPane.YES_OPTION) {
                // Mostra progresso
                ModernDialog.showMessage(this,
                        "Tentando reconectar ao servidor...\n" +
                                "Por favor, aguarde.",
                        "Reconectando",
                        JOptionPane.INFORMATION_MESSAGE);

                // Executa reconexão em thread separada
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        // Usa o novo método de reconexão
                        return offlineManager.tryReconnect();
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean sucesso = get();

                            if (sucesso) {
                                ModernDialog.showMessage(MainFrame.this,
                                        "Reconexão bem-sucedida!\n" +
                                                "Sistema voltou ao modo online.\n" +
                                                "Dados foram sincronizados.",
                                        "Conectado",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                ModernDialog.showMessage(MainFrame.this,
                                        "Não foi possível reconectar.\n" +
                                                "Verifique sua conexão de rede e tente novamente.",
                                        "Falha na Reconexão",
                                        JOptionPane.WARNING_MESSAGE);
                            }

                            // Atualiza status da interface
                            updateConnectionStatus(offlineManager.getCurrentState());

                        } catch (Exception e) {
                            ModernDialog.showMessage(MainFrame.this,
                                    "Erro durante reconexão: " + e.getMessage(),
                                    "Erro",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

                worker.execute();
            }

        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao tentar reconectar: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarStatusSistema() {
        try {
            OfflineManager.OfflineState estado = offlineManager.getCurrentState();
            boolean modoOfflineHabilitado = offlineManager.isOfflineModeEnabled();
            boolean operandoOffline = offlineManager.isOperatingOffline();

            StringBuilder info = new StringBuilder();
            info.append("=== STATUS DO SISTEMA ===\n\n");
            info.append("Estado Atual: ").append(estado.name()).append("\n");
            info.append("Modo Offline Habilitado: ").append(modoOfflineHabilitado ? "Sim" : "Não").append("\n");
            info.append("Operando Offline: ").append(operandoOffline ? "Sim" : "Não").append("\n\n");

            info.append("=== DESCRIÇÃO DOS ESTADOS ===\n\n");
            info.append("ONLINE: Conectado e sincronizado\n");
            info.append("OFFLINE: Desconectado, operando localmente\n");
            info.append("SYNCING: Sincronizando dados\n");
            info.append("ERROR: Erro no sistema offline\n");
            info.append("INITIALIZING: Inicializando sistema\n\n");

            info.append("=== SERVIDOR MOBILE ===\n\n");
            info.append("Status: ").append(servidorMobileAtivo ? "ATIVO" : "INATIVO").append("\n");
            if (servidorMobileAtivo) {
                info.append("URL: http://localhost:8080\n");
                info.append("Perfil: mobile\n");
            }
            info.append("\n");

            info.append("=== INFORMAÇÕES ADICIONAIS ===\n\n");
            info.append("Usuário: ").append(usuarioLogado.getNomeCompleto()).append("\n");
            info.append("Perfil: ").append(usuarioLogado.getPerfil().name()).append("\n");

            JTextArea textArea = new JTextArea(info.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Status do Sistema",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao obter status do sistema: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sairSistema() {
        int opcao = showModernConfirmDialog(
                "Deseja realmente sair do sistema?",
                "Confirmar Saída");

        if (opcao == JOptionPane.YES_OPTION) {
            // Parar servidor mobile se estiver rodando
            if (servidorMobileAtivo && servidorMobileProcess != null) {
                try {
                    System.out.println("Parando servidor mobile antes de sair...");
                    servidorMobileProcess.destroyForcibly();
                    servidorMobileProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.err.println("Erro ao parar servidor mobile: " + e.getMessage());
                }
            }

            // Parar timer de status antes de sair
            if (statusTimer != null) {
                statusTimer.cancel();
            }

            System.exit(0);
        }
    }

    private int showModernConfirmDialog(String message, String title) {
        ModernConfirmDialog dialog = new ModernConfirmDialog(this, title, message);
        dialog.setVisible(true);
        return dialog.getResult();
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    // Métodos para controle do servidor mobile
    private void iniciarServidorMobile() {
        if (servidorMobileAtivo) {
            ModernDialog.showMessage(this,
                    "O servidor mobile já está em execução!",
                    "Servidor Mobile",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Mostrar dialog de progresso
        JDialog progressDialog = new JDialog(this, "Iniciando Servidor Mobile", true);
        JTextArea outputArea = new JTextArea(15, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        progressDialog.add(scrollPane);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(this);

        // Executar em thread separada
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    publish("Iniciando servidor mobile...\n\n");

                    // Verificar se a porta 8081 está disponível
                    publish("Verificando disponibilidade da porta 8081...\n");
                    if (!verificarPortaDisponivel(8081)) {
                        publish("✗ ERRO: Porta 8081 já está em uso!\n");
                        publish("Execute 'netstat -ano | findstr :8081' para ver qual processo está usando.\n");
                        return false;
                    }
                    publish("✓ Porta 8081 disponível\n\n");

                    // Iniciar o servidor mobile como processo externo
                    String javaHome = System.getProperty("java.home");
                    String javaBin = javaHome + "/bin/java.exe"; // Windows
                    if (!new java.io.File(javaBin).exists()) {
                        javaBin = javaHome + "/bin/java"; // Linux/Mac
                    }

                    String classpath = System.getProperty("java.class.path");

                    publish("Java: " + javaBin + "\n");
                    publish("Classpath: " + classpath.substring(0, Math.min(100, classpath.length())) + "...\n");
                    publish("\nConfiguração do Servidor:\n");
                    publish("  • Perfil: mobile\n");
                    publish("  • Porta: 8081\n");
                    publish("  • Endereço: 0.0.0.0 (todas as interfaces)\n");
                    publish("  • Context Path: /inventario\n\n");

                    ProcessBuilder processBuilder = new ProcessBuilder(
                            javaBin,
                            "-cp", classpath,
                            // Propriedades do sistema Java (ANTES da classe)
                            "-Dspring.profiles.active=mobile",
                            "-Dspring.config.name=application",
                            "-Dspring.config.location=classpath:/",
                            "-Dserver.port=8081",
                            "-Dserver.address=0.0.0.0",
                            "-Dserver.servlet.context-path=/inventario",
                            "-Dspring.jmx.enabled=false",
                            "-Dcom.sun.management.jmxremote=false",
                            // Classe principal
                            "com.inventario.MobileApiApplication",
                            // Argumentos do Spring Boot (DEPOIS da classe)
                            "--spring.profiles.active=mobile",
                            "--server.port=8081",
                            "--server.address=0.0.0.0");

                    processBuilder.redirectErrorStream(true);
                    servidorMobileProcess = processBuilder.start();

                    publish("Processo iniciado! PID: " + servidorMobileProcess.pid() + "\n");
                    publish("Aguardando inicialização do Spring Boot...\n\n");

                    // Ler saída do processo em thread separada
                    Thread outputReader = new Thread(() -> {
                        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(servidorMobileProcess.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                publish(line + "\n");
                                System.out.println("[MOBILE SERVER] " + line);

                                // Verificar se o servidor iniciou com sucesso
                                if (line.contains("Started MobileApiApplication") ||
                                        line.contains("Tomcat started on port")) {
                                    servidorMobileAtivo = true;
                                }
                            }
                        } catch (Exception e) {
                            publish("Erro ao ler saída: " + e.getMessage() + "\n");
                        }
                    });
                    outputReader.setDaemon(true);
                    outputReader.start();

                    // Aguardar até 30 segundos para o servidor iniciar
                    for (int i = 0; i < 30; i++) {
                        Thread.sleep(1000);
                        if (servidorMobileAtivo) {
                            publish("\n✓ Servidor mobile iniciado com sucesso!\n");
                            return true;
                        }
                        if (!servidorMobileProcess.isAlive()) {
                            publish("\n✗ Processo terminou inesperadamente!\n");
                            return false;
                        }
                    }

                    publish("\n⚠ Timeout: Servidor pode ainda estar iniciando...\n");
                    return true;

                } catch (Exception e) {
                    publish("\n✗ ERRO: " + e.getMessage() + "\n");
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    outputArea.append(chunk);
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean sucesso = get();

                    // Adicionar botão de fechar
                    JButton btnFechar = new JButton("Fechar");
                    btnFechar.addActionListener(e -> progressDialog.dispose());
                    JPanel buttonPanel = new JPanel();
                    buttonPanel.add(btnFechar);
                    progressDialog.add(buttonPanel, BorderLayout.SOUTH);
                    progressDialog.revalidate();

                    // Atualizar status visual
                    updateConnectionStatus(offlineManager.getCurrentState());
                    atualizarIndicadorServidorMobile();

                    if (sucesso) {
                        outputArea.append("\n=== SERVIDOR MOBILE ATIVO ===\n");

                        // Obter IP da rede local
                        String ipLocal = obterIPLocal();

                        outputArea.append("\nAcesso Local (neste computador):\n");
                        outputArea.append("  URL: http://localhost:8081/inventario\n");
                        outputArea.append("  Swagger: http://localhost:8081/inventario/swagger-ui.html\n");

                        if (ipLocal != null && !ipLocal.isEmpty()) {
                            outputArea.append("\nAcesso pela Rede (smartphone/tablet):\n");
                            outputArea.append("  URL: http://" + ipLocal + ":8081/inventario\n");
                            outputArea.append("  Swagger: http://" + ipLocal + ":8081/inventario/swagger-ui.html\n");
                            outputArea.append("\n💡 Use este endereço no aplicativo mobile!\n");
                        } else {
                            outputArea.append("\n⚠ Não foi possível detectar o IP da rede local.\n");
                            outputArea
                                    .append("Execute 'ipconfig' (Windows) ou 'ifconfig' (Linux/Mac) para descobrir.\n");
                        }

                        outputArea.append("\n📱 Certifique-se de que:\n");
                        outputArea.append("  • O smartphone está na mesma rede Wi-Fi\n");
                        outputArea.append("  • O firewall permite conexões na porta 8081\n");
                        outputArea.append("  • O servidor está escutando em 0.0.0.0 (todas as interfaces)\n");
                    } else {
                        servidorMobileAtivo = false;
                        outputArea.append("\n=== FALHA AO INICIAR ===\n");
                        outputArea.append("Verifique os logs acima para mais detalhes.\n");
                    }

                } catch (Exception e) {
                    servidorMobileAtivo = false;
                    outputArea.append("\nErro: " + e.getMessage() + "\n");
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private void pararServidorMobile() {
        if (!servidorMobileAtivo || servidorMobileProcess == null) {
            ModernDialog.showMessage(this,
                    "O servidor mobile não está em execução.",
                    "Servidor Mobile",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Encerrar o processo do servidor
            servidorMobileProcess.destroy();

            // Aguardar até 5 segundos para o processo terminar
            if (!servidorMobileProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                // Se não terminou, forçar
                servidorMobileProcess.destroyForcibly();
            }

            servidorMobileProcess = null;
            servidorMobileAtivo = false;

            // Atualizar status visual
            updateConnectionStatus(offlineManager.getCurrentState());
            atualizarIndicadorServidorMobile();

            ModernDialog.showMessage(this,
                    "Servidor mobile foi parado com sucesso.",
                    "Servidor Mobile",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao parar servidor mobile: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Verifica se o servidor mobile está respondendo
     */
    private boolean verificarServidorMobile() {
        try {
            java.net.URI uri = java.net.URI.create("http://localhost:8081/inventario/actuator/health");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se uma porta está disponível
     */
    private boolean verificarPortaDisponivel(int porta) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(porta)) {
            socket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém o endereço IP local da rede
     */
    private String obterIPLocal() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();

                // Ignorar interfaces desativadas e loopback
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();

                    // Pegar apenas IPv4 e ignorar loopback
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();

                        // Preferir IPs da rede local (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") ||
                                (ip.startsWith("172.") &&
                                        Integer.parseInt(ip.split("\\.")[1]) >= 16 &&
                                        Integer.parseInt(ip.split("\\.")[1]) <= 31)) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter IP local: " + e.getMessage());
        }
        return null;
    }



    /**
     * Abre o diálogo para o usuário alterar sua própria senha
     */
    private void alterarMinhaSenha() {
        try {
            AlterarSenhaDialog dialog = new AlterarSenhaDialog(
                    this,
                    usuarioLogado.getId(),
                    usuarioLogado.getNomeCompleto(),
                    true // Requer senha atual
            );
            dialog.setVisible(true);

            if (dialog.isSenhaSalva()) {
                ModernDialog.showMessage(this,
                        "Senha alterada com sucesso!\nSua nova senha já está ativa.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir diálogo de alteração de senha: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void abrirStatusSalas() {
        try {
            StatusSalasFrame statusFrame = new StatusSalasFrame();
            statusFrame.setVisible(true);
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir status das salas: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Configura listeners para mudanças de status de sincronização
     */
    private void setupSyncStatusListeners() {
        // Listener para mudanças no OfflineManager
        offlineManager.addStateListener(new OfflineManager.OfflineStateListener() {
            @Override
            public void onStateChanged(OfflineManager.OfflineState oldState, OfflineManager.OfflineState newState) {
                SwingUtilities.invokeLater(() -> {
                    statusBarPanel.atualizarStatus(newState);
                    
                    // Se mudou para ONLINE, atualizar última sincronização
                    if (newState == OfflineManager.OfflineState.ONLINE) {
                        statusBarPanel.atualizarUltimaSincronizacao(java.time.LocalDateTime.now());
                        syncStatusManager.atualizarUltimaSincronizacaoGeral();
                    }
                    
                    // Atualizar contador de coletas pendentes
                    atualizarContadorColetasPendentes();
                });
            }
        });
        
        // Listener para mudanças no SyncStatusManager
        syncStatusManager.addListener((totalPendentes, ultimaSincronizacao) -> {
            SwingUtilities.invokeLater(() -> {
                statusBarPanel.atualizarColetasPendentes(totalPendentes);
                if (ultimaSincronizacao != null) {
                    statusBarPanel.atualizarUltimaSincronizacao(ultimaSincronizacao);
                }
            });
        });
        
        // Atualizar status inicial
        OfflineManager.OfflineState estadoInicial = offlineManager.getCurrentState();
        statusBarPanel.atualizarStatus(estadoInicial);
        
        // Atualizar contador de coletas pendentes na inicialização
        atualizarContadorColetasPendentes();
    }
    
    /**
     * Atualiza o contador de coletas pendentes consultando o SQLite
     */
    private void atualizarContadorColetasPendentes() {
        try {
            com.inventario.offline.OfflineDAO offlineDAO = new com.inventario.offline.OfflineDAO();
            int coletasPendentes = offlineDAO.contarColetasPendentes();
            
            SwingUtilities.invokeLater(() -> {
                statusBarPanel.atualizarColetasPendentes(coletasPendentes);
            });
            
            System.out.println(">>> Coletas pendentes de sincronização: " + coletasPendentes);
            
        } catch (Exception e) {
            System.err.println(">>> Erro ao contar coletas pendentes: " + e.getMessage());
        }
    }
    
    /**
     * Abre o dialog de importação de dados offline
     */
    private void abrirImportacaoDadosOffline() {
        try {
            // Verificar se já está em modo offline
            if (offlineManager.isOperatingOffline()) {
                int opcao = JOptionPane.showConfirmDialog(this,
                    "O sistema já está em modo offline.\n" +
                    "Deseja reimportar os dados do servidor?",
                    "Confirmar Importação",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (opcao != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Verificar conectividade antes de importar
            if (!verificarConectividadeServidor()) {
                JOptionPane.showMessageDialog(this,
                    "Não foi possível conectar ao servidor PostgreSQL.\n" +
                    "Verifique sua conexão e tente novamente.",
                    "Erro de Conexão",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Abrir dialog de importação
            System.out.println(">>> DEBUG MainFrame: Criando ImportacaoDadosDialog...");
            ImportacaoDadosDialog dialog = new ImportacaoDadosDialog(this, usuarioLogado);
            System.out.println(">>> DEBUG MainFrame: Dialog criado, exibindo...");
            dialog.setVisible(true);
            System.out.println(">>> DEBUG MainFrame: Dialog fechado");
            
            // Atualizar status após importação
            syncStatusManager.atualizarUltimaSincronizacaoGeral();
            statusBarPanel.atualizarUltimaSincronizacao(java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            ModernDialog.showMessage(this,
                    "Erro ao abrir importação de dados: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Executa sincronização de dados
     */
    private void executarSincronizacao() {
        // Verificar se está online
        if (offlineManager.isOperatingOffline()) {
            JOptionPane.showMessageDialog(this,
                "Sistema está em modo offline.\n" +
                "Conecte-se ao servidor para sincronizar.",
                "Modo Offline",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Mostrar progresso
        statusBarPanel.mostrarSincronizandoProgresso();
        statusBarPanel.atualizarStatus(OfflineManager.OfflineState.SYNCING);
        
        // Executar sincronização em thread separada
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Executar sincronização real
                    com.inventario.offline.DataSynchronizer.SyncResult result = 
                        offlineManager.executarSincronizacaoManual();
                    
                    // Atualizar status de sincronização
                    if (result.success) {
                        syncStatusManager.atualizarUltimaSincronizacaoGeral();
                    }
                    
                    return result.success;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean sucesso = get();
                    
                    statusBarPanel.restaurarBotaoSincronizar();
                    
                    if (sucesso) {
                        statusBarPanel.atualizarStatus(OfflineManager.OfflineState.ONLINE);
                        statusBarPanel.atualizarUltimaSincronizacao(java.time.LocalDateTime.now());
                        statusBarPanel.atualizarColetasPendentes(0);
                        
                        JOptionPane.showMessageDialog(MainFrame.this,
                            "Sincronização concluída com sucesso!",
                            "Sincronização",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusBarPanel.atualizarStatus(OfflineManager.OfflineState.ERROR);
                        
                        JOptionPane.showMessageDialog(MainFrame.this,
                            "Erro durante a sincronização.\n" +
                            "Verifique sua conexão e tente novamente.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    statusBarPanel.restaurarBotaoSincronizar();
                    statusBarPanel.atualizarStatus(OfflineManager.OfflineState.ERROR);
                    
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Erro ao sincronizar: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Atualiza o indicador visual do servidor mobile
     * Visível apenas para administradores
     */
    private void atualizarIndicadorServidorMobile() {
        if (lblServidorMobile == null) return;
        
        SwingUtilities.invokeLater(() -> {
            // Garantir que a fonte suporte emojis nativamente
            lblServidorMobile.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
            
            if (servidorMobileAtivo) {
                lblServidorMobile.setText("📱 Mobile: ONLINE");
                lblServidorMobile.setForeground(Color.WHITE);
                lblServidorMobile.setBackground(new Color(46, 204, 113)); // Verde
                lblServidorMobile.setToolTipText("Servidor mobile ativo na porta 8081 - Clique em Sistema > Servidor Mobile para gerenciar");
            } else {
                lblServidorMobile.setText("📱 Mobile: OFFLINE");
                lblServidorMobile.setForeground(Color.WHITE);
                lblServidorMobile.setBackground(new Color(149, 165, 166)); // Cinza
                lblServidorMobile.setToolTipText("Servidor mobile inativo - Clique em Sistema > Iniciar Servidor Mobile");
            }
        });
    }
    
    /**
     * Verifica conectividade com o servidor PostgreSQL
     */
    private boolean verificarConectividadeServidor() {
        try {
            // Usar o ConnectivityManager do OfflineManager
            com.inventario.offline.ConnectivityManager connManager = 
                com.inventario.offline.ConnectivityManager.getInstance();
            return connManager.checkDatabaseConnection();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica se existem dados locais (SQLite) disponíveis para modo offline
     * Verifica a existência de dados essenciais: usuários, patrimônios, salas
     */
    private boolean verificarDadosLocaisDisponiveis() {
        System.out.println("\n========================================");
        System.out.println(">>> VERIFICANDO DADOS LOCAIS NO SQLITE");
        System.out.println("========================================");
        
        try {
            // Usar caminho correto do banco SQLite (relativo ao projeto)
            String dbPath = "data/inventario.db";
            
            java.io.File dbFile = new java.io.File(dbPath);
            
            // Verificar se o arquivo do banco existe
            if (!dbFile.exists()) {
                System.out.println(">>> ❌ Banco SQLite não encontrado: " + dbFile.getAbsolutePath());
                return false;
            }
            
            System.out.println(">>> ✅ Banco SQLite encontrado: " + dbFile.getAbsolutePath());
            
            // Verificar se o banco tem dados
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                
                int totalUsuarios = 0;
                int totalPatrimonios = 0;
                int totalSalas = 0;
                int totalInventarios = 0;
                
                // 1. Verificar INVENTÁRIO (CRÍTICO!)
                System.out.println(">>> Verificando inventários...");
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM TABELA_INVENTARIO")) {
                    if (rs.next()) {
                        totalInventarios = rs.getInt("total");
                        System.out.println(">>>    TABELA_INVENTARIO: " + totalInventarios + " registro(s)");
                    }
                } catch (java.sql.SQLException e) {
                    System.out.println(">>>    ⚠️ Tabela TABELA_INVENTARIO não encontrada, tentando local_inventario...");
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM local_inventario")) {
                        if (rs.next()) {
                            totalInventarios = rs.getInt("total");
                            System.out.println(">>>    local_inventario: " + totalInventarios + " registro(s)");
                        }
                    } catch (java.sql.SQLException e2) {
                        System.out.println(">>>    ❌ Nenhuma tabela de inventário encontrada!");
                    }
                }
                
                if (totalInventarios == 0) {
                    System.out.println(">>> ❌ CRÍTICO: Nenhum inventário encontrado no banco local!");
                    return false;
                }
                
                // 2. Verificar USUÁRIOS
                System.out.println(">>> Verificando usuários...");
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM USUARIO")) {
                    if (rs.next()) {
                        totalUsuarios = rs.getInt("total");
                        System.out.println(">>>    USUARIO: " + totalUsuarios + " registro(s)");
                    }
                } catch (java.sql.SQLException e) {
                    System.out.println(">>>    ⚠️ Tabela USUARIO não encontrada, tentando local_usuario...");
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM local_usuario")) {
                        if (rs.next()) {
                            totalUsuarios = rs.getInt("total");
                            System.out.println(">>>    local_usuario: " + totalUsuarios + " registro(s)");
                        }
                    } catch (java.sql.SQLException e2) {
                        System.out.println(">>>    ⚠️ Nenhuma tabela de usuário encontrada");
                    }
                }
                
                if (totalUsuarios == 0) {
                    System.out.println(">>> ❌ Nenhum usuário encontrado no banco local");
                    return false;
                }
                
                // 3. Verificar PATRIMÔNIOS
                System.out.println(">>> Verificando patrimônios...");
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM PATRIMONIO")) {
                    if (rs.next()) {
                        totalPatrimonios = rs.getInt("total");
                        System.out.println(">>>    PATRIMONIO: " + totalPatrimonios + " registro(s)");
                    }
                } catch (java.sql.SQLException e) {
                    System.out.println(">>>    ⚠️ Tabela PATRIMONIO não encontrada, tentando local_patrimonio...");
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM local_patrimonio")) {
                        if (rs.next()) {
                            totalPatrimonios = rs.getInt("total");
                            System.out.println(">>>    local_patrimonio: " + totalPatrimonios + " registro(s)");
                        }
                    } catch (java.sql.SQLException e2) {
                        System.out.println(">>>    ⚠️ Nenhuma tabela de patrimônio encontrada");
                    }
                }
                
                if (totalPatrimonios == 0) {
                    System.out.println(">>> ⚠️ Nenhum patrimônio encontrado no banco local");
                    // Não retornar false aqui - pode ser um inventário novo sem patrimônios ainda
                }
                
                // 4. Verificar SALAS
                System.out.println(">>> Verificando salas...");
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM SALA")) {
                    if (rs.next()) {
                        totalSalas = rs.getInt("total");
                        System.out.println(">>>    SALA: " + totalSalas + " registro(s)");
                    }
                } catch (java.sql.SQLException e) {
                    System.out.println(">>>    ⚠️ Tabela SALA não encontrada, tentando local_sala...");
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM local_sala")) {
                        if (rs.next()) {
                            totalSalas = rs.getInt("total");
                            System.out.println(">>>    local_sala: " + totalSalas + " registro(s)");
                        }
                    } catch (java.sql.SQLException e2) {
                        System.out.println(">>>    ⚠️ Nenhuma tabela de sala encontrada");
                    }
                }
                
                if (totalSalas == 0) {
                    System.out.println(">>> ❌ Nenhuma sala encontrada no banco local");
                    return false;
                }
                
                // Resumo
                System.out.println("\n>>> ✅ DADOS LOCAIS DISPONÍVEIS:");
                System.out.println(">>>    Inventários: " + totalInventarios);
                System.out.println(">>>    Usuários: " + totalUsuarios);
                System.out.println(">>>    Patrimônios: " + totalPatrimonios);
                System.out.println(">>>    Salas: " + totalSalas);
                System.out.println("========================================\n");
                
                return true;
                
            } catch (java.sql.SQLException e) {
                System.err.println(">>> ❌ Erro ao verificar dados locais: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            
        } catch (Exception e) {
            System.err.println(">>> ❌ Erro ao verificar disponibilidade de dados locais: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
