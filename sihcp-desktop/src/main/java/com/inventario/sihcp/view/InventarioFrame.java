package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.offline.ConnectivityListener;
import com.inventario.sihcp.offline.ConnectivityManager;
import com.inventario.sihcp.offline.OfflineConfigManager;
import com.inventario.sihcp.offline.OfflineManager;
import com.inventario.sihcp.offline.SyncFrame;
import com.inventario.sihcp.service.BusinessException;
import com.inventario.sihcp.service.ColetaService;
import com.inventario.sihcp.service.InventarioService;

/**
 * Tela principal para gerenciamento de inventários
 * Design moderno com cards e visual clean
 */
public class InventarioFrame extends JFrame implements ConnectivityListener {
    private static final Logger logger = LoggerFactory.getLogger(InventarioFrame.class);
    
    // Cores do tema moderno
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 245);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color INFO_COLOR = new Color(6, 182, 212);
    private static final Color PURPLE_COLOR = new Color(139, 92, 246);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TABLE_HEADER_BG = new Color(249, 250, 251);
    private static final Color TABLE_STRIPE = new Color(249, 250, 251);
    private static final Color SELECTION_COLOR = new Color(59, 130, 246, 30);
    
    private JTable tabelaInventario;
    private DefaultTableModel modeloTabela;
    private final InventarioService inventarioService;
    private final ColetaService coletaService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnVisualizar, btnFinalizar, btnRelatorio;
    private JButton btnAbrir, btnCancelar, btnExcluir;
    private JButton btnBuscarItem;
    private JLabel lblTotalRegistros;

    
    // Componentes do modo offline
    private JLabel lblOfflineStatus;
    private JButton btnSyncNow;
    private OfflineManager offlineManager;
    private Timer statusUpdateTimer;
    
    public InventarioFrame() {
        this.inventarioService = new InventarioService();
        this.coletaService = new ColetaService();
        inicializarComponentesOffline();
        initComponents();
        iniciarMonitoramentoStatusOffline();
        carregarInventarios();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Inventários - SIHCP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        
        // Criar barra de menu
        criarBarraMenu();
        
        // Painel principal com margem
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // Header com título e botão novo
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Card principal com tabela
        JPanel cardPanel = createCardPanel();
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(1300, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 550));
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        // Lado esquerdo - Título e subtítulo
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JLabel lblTitulo = new JLabel("Gerenciamento de Inventários");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblSubtitulo = new JLabel("Gerencie os inventários patrimoniais do sistema");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(TEXT_SECONDARY);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitulo);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Lado direito - Botão Novo e Status Offline
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        // Status offline
        lblOfflineStatus = new JLabel("🔄");
        lblOfflineStatus.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblOfflineStatus.setToolTipText("Status da conexão");
        rightPanel.add(lblOfflineStatus);
        
        btnSyncNow = createModernButton("🔄 Sync", INFO_COLOR, Color.WHITE);
        btnSyncNow.setPreferredSize(new Dimension(90, 40));
        btnSyncNow.setToolTipText("Sincronização rápida");
        rightPanel.add(btnSyncNow);
        
        btnNovo = createModernButton("+ Novo Inventário", SUCCESS_COLOR, Color.WHITE);
        btnNovo.setPreferredSize(new Dimension(170, 44));
        rightPanel.add(btnNovo);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createCardPanel() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(BACKGROUND_COLOR);
        
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 16, 16));
                
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 16, 16));
                
                g2d.setColor(BORDER_COLOR);
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 16, 16));
                
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        card.add(tablePanel, BorderLayout.CENTER);
        
        JPanel footer = createFooter();
        card.add(footer, BorderLayout.SOUTH);
        
        cardWrapper.add(card, BorderLayout.CENTER);
        return cardWrapper;
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Campo de busca (esquerda)
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(350, 40));
        
        campoBusca = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setColor(TEXT_SECONDARY);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2d.drawString("Buscar inventário...", 38, 25);
                    g2d.dispose();
                }
            }
        };
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoBusca.setBackground(Color.WHITE);
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        searchIcon.setForeground(TEXT_SECONDARY);
        
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(Color.WHITE);
        searchWrapper.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        searchWrapper.add(searchIcon, BorderLayout.WEST);
        searchWrapper.add(campoBusca, BorderLayout.CENTER);
        campoBusca.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 12));
        
        searchPanel.add(searchWrapper, BorderLayout.CENTER);
        toolbar.add(searchPanel, BorderLayout.WEST);
        
        // Botões de ação (direita)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        
        btnVisualizar = createModernButton("👁 Ver", PRIMARY_COLOR, Color.WHITE);
        btnVisualizar.setPreferredSize(new Dimension(85, 38));
        
        btnEditar = createModernButton("✏ Editar", WARNING_COLOR, Color.WHITE);
        btnEditar.setPreferredSize(new Dimension(100, 38));
        
        btnAbrir = createModernButton("🔓 Abrir", SUCCESS_COLOR, Color.WHITE);
        btnAbrir.setPreferredSize(new Dimension(95, 38));
        
        btnFinalizar = createModernButton("✅ Finalizar", INFO_COLOR, Color.WHITE);
        btnFinalizar.setPreferredSize(new Dimension(110, 38));
        
        btnCancelar = createModernButton("❌ Cancelar", WARNING_COLOR, Color.WHITE);
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        
        btnExcluir = createModernButton("🗑 Excluir", DANGER_COLOR, Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 38));
        
        btnRelatorio = createModernButton("📊 Relatório", PURPLE_COLOR, Color.WHITE);
        btnRelatorio.setPreferredSize(new Dimension(115, 38));
        
        btnBuscarItem = createModernButton("🔍 Buscar Item", new Color(99, 102, 241), Color.WHITE);
        btnBuscarItem.setPreferredSize(new Dimension(130, 38));
        btnBuscarItem.setToolTipText("Buscar situação de um patrimônio neste inventário e ver histórico");
        
        actionsPanel.add(btnVisualizar);
        actionsPanel.add(btnEditar);
        actionsPanel.add(btnAbrir);
        actionsPanel.add(btnFinalizar);
        actionsPanel.add(btnCancelar);
        actionsPanel.add(btnExcluir);

        
        toolbar.add(actionsPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    private JPanel createTablePanel() {
        String[] colunas = {"ID", "Descrição", "Data Início", "Data Fim", "Status", "Responsável", "Progresso"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaInventario = new JTable(modeloTabela);
        tabelaInventario.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaInventario.setRowHeight(48);
        tabelaInventario.setShowGrid(false);
        tabelaInventario.setIntercellSpacing(new Dimension(0, 0));
        tabelaInventario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaInventario.setSelectionBackground(SELECTION_COLOR);
        tabelaInventario.setSelectionForeground(TEXT_PRIMARY);
        tabelaInventario.setFocusable(false);
        
        // Configurar larguras das colunas
        tabelaInventario.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tabelaInventario.getColumnModel().getColumn(0).setMaxWidth(80);
        tabelaInventario.getColumnModel().getColumn(1).setPreferredWidth(250);  // Descrição
        tabelaInventario.getColumnModel().getColumn(2).setPreferredWidth(120);  // Data Início
        tabelaInventario.getColumnModel().getColumn(3).setPreferredWidth(120);  // Data Fim
        tabelaInventario.getColumnModel().getColumn(4).setPreferredWidth(120);  // Status
        tabelaInventario.getColumnModel().getColumn(5).setPreferredWidth(180);  // Responsável
        tabelaInventario.getColumnModel().getColumn(6).setPreferredWidth(100);  // Progresso
        
        // Header da tabela
        JTableHeader header = tabelaInventario.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        
        // Renderizador customizado
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_STRIPE);
                }
                
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                setForeground(TEXT_PRIMARY);
                
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                // Colorir status
                if (column == 4 && value != null) {
                    String status = value.toString().toUpperCase();
                    if (status.contains("ANDAMENTO") || status.contains("ABERTO")) {
                        setForeground(SUCCESS_COLOR);
                    } else if (status.contains("FINALIZADO") || status.contains("CONCLUIDO")) {
                        setForeground(PRIMARY_COLOR);
                    } else if (status.contains("CANCELADO")) {
                        setForeground(DANGER_COLOR);
                    } else if (status.contains("PLANEJADO")) {
                        setForeground(WARNING_COLOR);
                    }
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < tabelaInventario.getColumnCount(); i++) {
            tabelaInventario.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        // Header renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(TABLE_HEADER_BG);
                label.setForeground(TEXT_SECONDARY);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(0, 15, 0, 15)
                ));
                label.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return label;
            }
        };
        
        for (int i = 0; i < tabelaInventario.getColumnCount(); i++) {
            tabelaInventario.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(tabelaInventario);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        lblTotalRegistros = new JLabel("0 inventários encontrados");
        lblTotalRegistros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotalRegistros.setForeground(TEXT_SECONDARY);
        
        JLabel lblDica = new JLabel("💡 Dica: Clique duas vezes na linha para visualizar");
        lblDica.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblDica.setForeground(TEXT_SECONDARY);
        lblDica.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerActions.setOpaque(false);
        
        // Reutilizando os botões criados no toolbar, pois já foram instanciados
        if (btnBuscarItem != null) {
            btnBuscarItem.setPreferredSize(new Dimension(130, 36));
            footerActions.add(btnBuscarItem);
        }
        if (btnRelatorio != null) {
            btnRelatorio.setPreferredSize(new Dimension(115, 36));
            footerActions.add(btnRelatorio);
        }
        
        footer.add(lblTotalRegistros, BorderLayout.WEST);
        footer.add(lblDica, BorderLayout.CENTER);
        footer.add(footerActions, BorderLayout.EAST);
        
        return footer;
    }
    
    private JButton createModernButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bg = bgColor;
                if (!isEnabled()) {
                    bg = new Color(209, 213, 219);
                } else if (getModel().isPressed()) {
                    bg = bgColor.darker();
                } else if (isHovered) {
                    bg = brighter(bgColor, 0.1f);
                }
                
                g2d.setColor(bg);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2d.dispose();
                
                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setFont(getFont());
                g2d.setColor(isEnabled() ? textColor : new Color(156, 163, 175));
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                g2d.drawString(text, textX, textY);
                g2d.dispose();
            }
            
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }
        };
        
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        button.setForeground(textColor);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private Color brighter(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() + 255 * factor));
        int g = Math.min(255, (int) (color.getGreen() + 255 * factor));
        int b = Math.min(255, (int) (color.getBlue() + 255 * factor));
        return new Color(r, g, b);
    }
    
    private void criarBarraMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JMenuItem itemNovo = new JMenuItem("Novo Inventário");
        itemNovo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        itemNovo.addActionListener(e -> criarNovoInventario());
        
        JMenuItem itemAtualizar = new JMenuItem("Atualizar Lista");
        itemAtualizar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemAtualizar.addActionListener(e -> carregarInventarios());
        
        JMenuItem itemFechar = new JMenuItem("Fechar");
        itemFechar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        itemFechar.addActionListener(e -> dispose());
        
        menuArquivo.add(itemNovo);
        menuArquivo.addSeparator();
        menuArquivo.add(itemAtualizar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemFechar);
        
        // Menu Ações
        JMenu menuAcoes = new JMenu("Ações");
        menuAcoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JMenuItem itemVisualizar = new JMenuItem("Visualizar Detalhes");
        itemVisualizar.addActionListener(e -> visualizarInventario());
        
        JMenuItem itemEditar = new JMenuItem("Editar Inventário");
        itemEditar.addActionListener(e -> editarInventario());
        
        JMenuItem itemFinalizar = new JMenuItem("Finalizar Inventário");
        itemFinalizar.addActionListener(e -> finalizarInventario());
        
        JMenuItem itemCancelar = new JMenuItem("Cancelar Inventário");
        itemCancelar.addActionListener(e -> cancelarInventario());
        
        menuAcoes.add(itemVisualizar);
        menuAcoes.add(itemEditar);
        menuAcoes.addSeparator();
        menuAcoes.add(itemFinalizar);
        menuAcoes.add(itemCancelar);
        
        // Menu Sincronização
        JMenu menuSync = new JMenu("Sincronização");
        menuSync.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JMenuItem itemSyncRapida = new JMenuItem("Sincronização Rápida");
        itemSyncRapida.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        itemSyncRapida.addActionListener(e -> sincronizacaoRapida());
        
        JMenuItem itemSyncCompleta = new JMenuItem("Sincronização Completa");
        itemSyncCompleta.addActionListener(e -> abrirSyncFrame());
        
        JMenuItem itemConfigOffline = new JMenuItem("Configurar Modo Offline");
        itemConfigOffline.addActionListener(e -> configurarModoOffline());
        
        menuSync.add(itemSyncRapida);
        menuSync.add(itemSyncCompleta);
        menuSync.addSeparator();
        menuSync.add(itemConfigOffline);
        
        menuBar.add(menuArquivo);
        menuBar.add(menuAcoes);
        menuBar.add(menuSync);
        
        setJMenuBar(menuBar);
    }
    
    private void configurarEventos() {
        // Botões principais
        btnNovo.addActionListener(e -> criarNovoInventario());
        btnEditar.addActionListener(e -> editarInventario());
        btnVisualizar.addActionListener(e -> visualizarInventario());
        btnFinalizar.addActionListener(e -> finalizarInventario());
        btnAbrir.addActionListener(e -> abrirInventario());
        btnCancelar.addActionListener(e -> cancelarInventario());
        btnExcluir.addActionListener(e -> excluirInventario());
        btnRelatorio.addActionListener(e -> gerarRelatorio());
        btnBuscarItem.addActionListener(e -> buscarItemNoInventario());
        btnSyncNow.addActionListener(e -> sincronizacaoRapida());

        
        // Busca em tempo real com delay
        Timer searchTimer = new Timer(300, e -> buscarInventarios());
        searchTimer.setRepeats(false);
        
        campoBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                searchTimer.restart(); 
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                searchTimer.restart(); 
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                searchTimer.restart(); 
            }
        });
        
        // Duplo clique na tabela para visualizar
        tabelaInventario.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    visualizarInventario();
                }
            }
        });
    }
    
    private void carregarInventarios() {
        modeloTabela.setRowCount(0);
        try {
            List<Inventario> inventarios = inventarioService.listarTodos();
            for (Inventario inv : inventarios) {
                String progresso = calcularProgresso(inv);
                modeloTabela.addRow(new Object[]{
                    inv.getId(),
                    inv.getNome(),
                    formatarData(inv.getDataInicio()),
                    formatarData(inv.getDataFim()),
                    inv.getStatusInventario(),
                    inv.getResponsavelInventario() != null ? inv.getResponsavelInventario() : "",
                    progresso
                });
            }
            atualizarContador(inventarios.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar inventários: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarInventarios() {
        String termo = campoBusca.getText().trim().toLowerCase();
        modeloTabela.setRowCount(0);
        
        try {
            List<Inventario> inventarios = inventarioService.listarTodos();
            
            for (Inventario inv : inventarios) {
                // Filtrar por termo de busca
                if (!termo.isEmpty()) {
                    String nome = inv.getNome() != null ? inv.getNome().toLowerCase() : "";
                    String responsavel = inv.getResponsavelInventario() != null ? inv.getResponsavelInventario().toLowerCase() : "";
                    String status = inv.getStatusInventario() != null ? inv.getStatusInventario().toLowerCase() : "";
                    
                    if (!nome.contains(termo) && !responsavel.contains(termo) && !status.contains(termo)) {
                        continue;
                    }
                }
                
                String progresso = calcularProgresso(inv);
                modeloTabela.addRow(new Object[]{
                    inv.getId(),
                    inv.getNome(),
                    formatarData(inv.getDataInicio()),
                    formatarData(inv.getDataFim()),
                    inv.getStatusInventario(),
                    inv.getResponsavelInventario() != null ? inv.getResponsavelInventario() : "",
                    progresso
                });
            }
            atualizarContador(modeloTabela.getRowCount());
        } catch (Exception e) {
            logger.warn("Erro ao buscar inventários: {}", e.getMessage());
        }
    }
    
    private String calcularProgresso(Inventario inv) {
        try {
            Integer total = inv.getTotalPatrimonios();
            Integer coletados = inv.getPatrimoniosColetados();
            if (total != null && total > 0 && coletados != null) {
                int percentual = (coletados * 100) / total;
                return percentual + "%";
            }
        } catch (Exception e) {
            logger.warn("Erro ao calcular progresso: {}", e.getMessage());
        }
        return "0%";
    }
    
    private String formatarData(java.util.Date data) {
        if (data == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(data);
    }
    
    private void atualizarContador(int total) {
        String texto = total == 1 ? "1 inventário encontrado" : total + " inventários encontrados";
        lblTotalRegistros.setText(texto);
    }
    
    private Inventario getInventarioSelecionado() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada < 0) {
            return null;
        }
        try {
            Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
            return inventarioService.buscarPorId(id);
        } catch (Exception e) {
            logger.warn("Erro ao obter inventário selecionado: {}", e.getMessage());
            return null;
        }
    }
    
    // ==================== AÇÕES ====================
    
    private void criarNovoInventario() {
        InventarioFormDialog dialog = new InventarioFormDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarInventarios();
        }
    }
    
    private void editarInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para editar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String status = inv.getStatusInventario();
        if ("CONCLUIDO".equalsIgnoreCase(status) || "CANCELADO".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, 
                "Não é possível editar um inventário " + status.toLowerCase() + ".",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        InventarioFormDialog dialog = new InventarioFormDialog(this, inv);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarInventarios();
        }
    }
    
    private void visualizarInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para visualizar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        mostrarDetalhesInventario(inv);
    }
    
    private void mostrarDetalhesInventario(Inventario inv) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("         DETALHES DO INVENTÁRIO\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("ID: ").append(inv.getId()).append("\n");
        sb.append("Nome: ").append(inv.getNome()).append("\n");
        sb.append("Status: ").append(inv.getStatusInventario()).append("\n");
        sb.append("Data Início: ").append(formatarData(inv.getDataInicio())).append("\n");
        sb.append("Data Fim: ").append(formatarData(inv.getDataFim())).append("\n");
        sb.append("Responsável: ").append(inv.getResponsavelInventario() != null ? inv.getResponsavelInventario() : "N/A").append("\n");
        
        Integer total = inv.getTotalPatrimonios();
        Integer coletados = inv.getPatrimoniosColetados();
        int totalVal = total != null ? total : 0;
        int coletadosVal = coletados != null ? coletados : 0;
        int pendentes = totalVal - coletadosVal;
        int percentual = totalVal > 0 ? (coletadosVal * 100) / totalVal : 0;
        
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("              PROGRESSO\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("Total de Patrimônios: ").append(totalVal).append("\n");
        sb.append("Coletados: ").append(coletadosVal).append("\n");
        sb.append("Pendentes: ").append(pendentes).append("\n");
        sb.append("Progresso: ").append(percentual).append("%\n");
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Detalhes do Inventário #" + inv.getId(), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para abrir.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!"PLANEJADO".equalsIgnoreCase(inv.getStatusInventario())) {
            JOptionPane.showMessageDialog(this, 
                "Apenas inventários com status 'PLANEJADO' podem ser abertos.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja abrir o inventário '" + inv.getNome() + "'?\n\n" +
            "O status será alterado para 'EM ANDAMENTO'.",
            "Confirmar Abertura", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                inv.setStatusInventario(Inventario.STATUS_EM_ANDAMENTO);
                inventarioService.atualizar(inv);
                JOptionPane.showMessageDialog(this, "Inventário aberto com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarInventarios();
            } catch (BusinessException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void finalizarInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para finalizar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!Inventario.STATUS_EM_ANDAMENTO.equalsIgnoreCase(inv.getStatusInventario()) && !"ABERTO".equalsIgnoreCase(inv.getStatusInventario())) {
            JOptionPane.showMessageDialog(this, 
                "Apenas inventários em andamento podem ser finalizados.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja finalizar o inventário '" + inv.getNome() + "'?\n\n" +
            "Esta ação não pode ser desfeita.",
            "Confirmar Finalização", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                inventarioService.finalizarInventario(inv.getId());
                JOptionPane.showMessageDialog(this, "Inventário finalizado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarInventarios();
            } catch (BusinessException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cancelarInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para cancelar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (Inventario.STATUS_CONCLUIDO.equalsIgnoreCase(inv.getStatusInventario()) || Inventario.STATUS_CANCELADO.equalsIgnoreCase(inv.getStatusInventario())) {
            JOptionPane.showMessageDialog(this, 
                "Este inventário já está " + inv.getStatusInventario().toLowerCase() + ".",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja cancelar o inventário '" + inv.getNome() + "'?\n\n" +
            "Esta ação não pode ser desfeita.",
            "Confirmar Cancelamento", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                inventarioService.cancelarInventario(inv.getId());
                JOptionPane.showMessageDialog(this, "Inventário cancelado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarInventarios();
            } catch (BusinessException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void excluirInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para excluir.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int qtdColetas = coletaService.contarColetasPorInventario(inv.getId());
            if (qtdColetas > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Não é possível excluir o inventário pois possui " + qtdColetas + " coleta(s) registrada(s).",
                    "Exclusão não permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (HeadlessException e) {
            logger.warn("Erro ao verificar coletas: {}", e.getMessage());
        }
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja excluir o inventário '" + inv.getNome() + "'?\n\n" +
            "Esta ação não pode ser desfeita.",
            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                inventarioService.excluir(inv.getId());
                JOptionPane.showMessageDialog(this, "Inventário excluído com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarInventarios();
            } catch (BusinessException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gerarRelatorio() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para gerar relatório.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            RelatorioFrame relatorioFrame = new RelatorioFrame();
            relatorioFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir relatórios: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarItemNoInventario() {
        Inventario inv = getInventarioSelecionado();
        if (inv == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione um inventário para buscar itens.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BuscaItemInventarioDialog dialog = new BuscaItemInventarioDialog(this, inv);
        dialog.setVisible(true);
    }
    
    // ==================== MODO OFFLINE ====================

    
    private void inicializarComponentesOffline() {
        try {
            offlineManager = OfflineManager.getInstance();
            ConnectivityManager.getInstance().addListener(this);
        } catch (Exception e) {
            logger.warn("Erro ao inicializar componentes offline: {}", e.getMessage());
        }
    }
    
    private void iniciarMonitoramentoStatusOffline() {
        statusUpdateTimer = new Timer(30000, e -> atualizarStatusOffline());
        statusUpdateTimer.start();
        atualizarStatusOffline();
    }
    
    private void atualizarStatusOffline() {
        try {
            boolean online = ConnectivityManager.getInstance().isOnline();
            if (online) {
                lblOfflineStatus.setText("🟢 Online");
                lblOfflineStatus.setForeground(SUCCESS_COLOR);
            } else {
                lblOfflineStatus.setText("🔴 Offline");
                lblOfflineStatus.setForeground(DANGER_COLOR);
            }
        } catch (Exception e) {
            lblOfflineStatus.setText("⚪ Desconhecido");
            lblOfflineStatus.setForeground(TEXT_SECONDARY);
        }
    }
    
    private void sincronizacaoRapida() {
        try {
            if (!ConnectivityManager.getInstance().isOnline()) {
                JOptionPane.showMessageDialog(this, 
                    "Sem conexão com o servidor. Verifique sua rede.",
                    "Offline", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            lblOfflineStatus.setText("🔄 Sincronizando...");
            
            offlineManager.sincronizarDados();
            
            carregarInventarios();
            
            JOptionPane.showMessageDialog(this, "Sincronização concluída com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, "Erro na sincronização: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            atualizarStatusOffline();
        }
    }
    
    private void abrirSyncFrame() {
        SyncFrame syncFrame = new SyncFrame();
        syncFrame.setVisible(true);
    }
    
    private void configurarModoOffline() {
        try {
            OfflineConfigManager configManager = OfflineConfigManager.getInstance();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Configurações do Modo Offline\n\n");
            sb.append("Status: ").append(ConnectivityManager.getInstance().isOnline() ? "Online" : "Offline").append("\n");
            sb.append("Sincronização Automática: ").append(configManager.isSyncAutomatico() ? "Ativada" : "Desativada").append("\n");
            sb.append("Intervalo de Sync: ").append(configManager.getIntervaloSync()).append(" minutos\n");
            
            JOptionPane.showMessageDialog(this, sb.toString(),
                "Configurações Offline", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar configurações: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== CONNECTIVITY LISTENER ====================
    
    @Override
    public void onConnectionEstablished() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            lblOfflineStatus.setText("🟢 Online");
            lblOfflineStatus.setForeground(SUCCESS_COLOR);
            logger.info("Conexão estabelecida");
        });
    }
    
    @Override
    public void onConnectionLost() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            lblOfflineStatus.setText("🔴 Offline");
            lblOfflineStatus.setForeground(DANGER_COLOR);
            logger.info("Conexão perdida");
        });
    }
    
    @Override
    public void dispose() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.stop();
        }
        try {
            ConnectivityManager.getInstance().removeListener(this);
        } catch (Exception e) {
            logger.warn("Erro ao remover listener: {}", e.getMessage());
        }
        super.dispose();
    }
}
