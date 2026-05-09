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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.inventario.sihcp.model.Campus;
import com.inventario.sihcp.service.CampusService;

/**
 * Tela principal para gerenciamento de campus
 * Design moderno com cards e visual clean
 */
public class CampusFrame extends JFrame {
    
    // Cores do tema moderno
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 245);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TABLE_HEADER_BG = new Color(249, 250, 251);
    private static final Color TABLE_STRIPE = new Color(249, 250, 251);
    private static final Color SELECTION_COLOR = new Color(59, 130, 246, 30);
    
    private JTable tabelaCampus;
    private DefaultTableModel modeloTabela;
    private final CampusService campusService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnExcluir;
    private JLabel lblTotalRegistros;
    
    public CampusFrame() {
        this.campusService = new CampusService();
        initComponents();
        carregarCampus();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Campus - SIHCP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Definir ícone personalizado
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        
        // Painel principal com margem
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // Header com título e estatísticas
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Card principal com tabela
        JPanel cardPanel = createCardPanel();
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(1000, 500);
        setLocationRelativeTo(null);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        // Lado esquerdo - Título e subtítulo
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JLabel lblTitulo = new JLabel("Gerenciamento de Campus");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblSubtitulo = new JLabel("Gerencie os campus cadastrados no sistema");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(TEXT_SECONDARY);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitulo);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Lado direito - Botão Novo Campus
        btnNovo = createModernButton("+ Novo Campus", SUCCESS_COLOR, Color.WHITE);
        btnNovo.setPreferredSize(new Dimension(160, 44));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setBackground(BACKGROUND_COLOR);
        btnPanel.add(btnNovo);
        
        headerPanel.add(btnPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createCardPanel() {
        // Card com sombra simulada
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(BACKGROUND_COLOR);
        
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 16, 16));
                
                // Card
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 16, 16));
                
                // Borda
                g2d.setColor(BORDER_COLOR);
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 16, 16));
                
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Toolbar do card
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);
        
        // Tabela
        JPanel tablePanel = createTablePanel();
        card.add(tablePanel, BorderLayout.CENTER);
        
        // Footer com total de registros
        JPanel footer = createFooter();
        card.add(footer, BorderLayout.SOUTH);
        
        cardWrapper.add(card, BorderLayout.CENTER);
        return cardWrapper;
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Campo de busca com ícone
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
                    g2d.drawString("Buscar por nome, local ou diretor...", 38, 25);
                    g2d.dispose();
                }
            }
        };
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 38, 8, 12)
        ));
        campoBusca.setBackground(Color.WHITE);
        
        // Ícone de busca
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
        
        // Botões de ação
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);
        
        btnEditar = createModernButton("✏ Editar", PRIMARY_COLOR, Color.WHITE);
        btnEditar.setPreferredSize(new Dimension(110, 38));
        
        btnExcluir = createModernButton("🗑 Excluir", DANGER_COLOR, Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(110, 38));
        
        actionsPanel.add(btnEditar);
        actionsPanel.add(btnExcluir);
        
        toolbar.add(actionsPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    private JPanel createTablePanel() {
        String[] colunas = {"ID", "Nome", "Local", "CNPJ", "Diretor", "Telefone", "Email"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaCampus = new JTable(modeloTabela);
        tabelaCampus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCampus.setRowHeight(48);
        tabelaCampus.setShowGrid(false);
        tabelaCampus.setIntercellSpacing(new Dimension(0, 0));
        tabelaCampus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaCampus.setSelectionBackground(SELECTION_COLOR);
        tabelaCampus.setSelectionForeground(TEXT_PRIMARY);
        tabelaCampus.setFocusable(false);
        
        // Configurar larguras das colunas
        tabelaCampus.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tabelaCampus.getColumnModel().getColumn(0).setMaxWidth(80);
        tabelaCampus.getColumnModel().getColumn(1).setPreferredWidth(180);  // Nome
        tabelaCampus.getColumnModel().getColumn(2).setPreferredWidth(200);  // Local
        tabelaCampus.getColumnModel().getColumn(3).setPreferredWidth(140);  // CNPJ
        tabelaCampus.getColumnModel().getColumn(4).setPreferredWidth(160);  // Diretor
        tabelaCampus.getColumnModel().getColumn(5).setPreferredWidth(120);  // Telefone
        tabelaCampus.getColumnModel().getColumn(6).setPreferredWidth(200);  // Email
        
        // Header da tabela
        JTableHeader header = tabelaCampus.getTableHeader();
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
                
                // Centralizar ID
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < tabelaCampus.getColumnCount(); i++) {
            tabelaCampus.getColumnModel().getColumn(i).setCellRenderer(renderer);
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
        
        for (int i = 0; i < tabelaCampus.getColumnCount(); i++) {
            tabelaCampus.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(tabelaCampus);
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
        
        lblTotalRegistros = new JLabel("0 registros encontrados");
        lblTotalRegistros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotalRegistros.setForeground(TEXT_SECONDARY);
        
        footer.add(lblTotalRegistros, BorderLayout.WEST);
        
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
                
                // Desenhar texto
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
    
    private void configurarEventos() {
        btnNovo.addActionListener(e -> abrirFormularioCampus(null));
        btnEditar.addActionListener(e -> editarCampus());
        btnExcluir.addActionListener(e -> excluirCampus());
        
        // Busca em tempo real ao digitar
        campoBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { buscarCampus(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { buscarCampus(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscarCampus(); }
        });
        
        // Duplo clique na tabela para editar
        tabelaCampus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarCampus();
                }
            }
        });
    }
    
    private void carregarCampus() {
        modeloTabela.setRowCount(0);
        try {
            List<Campus> campusList = campusService.listarCampusAtivos();
            for (Campus campus : campusList) {
                modeloTabela.addRow(new Object[]{
                    campus.getId(),
                    campus.getNome(),
                    campus.getLocal(),
                    campus.getCnpj() != null ? campus.getCnpj() : "",
                    campus.getDiretor() != null ? campus.getDiretor() : "",
                    campus.getTelefone() != null ? campus.getTelefone() : "",
                    campus.getEmail() != null ? campus.getEmail() : ""
                });
            }
            atualizarContador(campusList.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar campus: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarContador(int total) {
        String texto = total == 1 ? "1 registro encontrado" : total + " registros encontrados";
        lblTotalRegistros.setText(texto);
    }
    
    private void abrirFormularioCampus(Campus campus) {
        CampusFormDialog dialog = new CampusFormDialog(this, campus);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarCampus();
        }
    }
    
    private void editarCampus() {
        int linhaSelecionada = tabelaCampus.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Campus campus = campusService.buscarCampusPorId(id);
                if (campus != null) {
                    abrirFormularioCampus(campus);
                } else {
                    JOptionPane.showMessageDialog(this, "Campus não encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HeadlessException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar campus: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um campus para editar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void excluirCampus() {
        int linhaSelecionada = tabelaCampus.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nomeCampus = (String) modeloTabela.getValueAt(linhaSelecionada, 1);
                
                // Verificar se há setores vinculados ao campus
                int qtdSetores = campusService.contarSetoresDoCampus(id);
                
                if (qtdSetores > 0) {
                    String mensagem = "Não é possível excluir o campus '" + nomeCampus + "' pois possui " +
                        qtdSetores + " setor(es) vinculado(s).\n\nRemova as vinculações antes de excluir o campus.";
                    JOptionPane.showMessageDialog(this, mensagem, "Exclusão não permitida", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirmacao = JOptionPane.showConfirmDialog(this, 
                    "Tem certeza que deseja excluir o campus '" + nomeCampus + "'?", 
                    "Confirmar Exclusão", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    if (campusService.excluirCampus(id)) {
                        JOptionPane.showMessageDialog(this, "Campus excluído com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarCampus();
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao excluir campus.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (HeadlessException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir campus: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um campus para excluir.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void buscarCampus() {
        String termo = campoBusca.getText().trim();
        modeloTabela.setRowCount(0);
        
        try {
            List<Campus> campusList;
            if (!termo.isEmpty()) {
                campusList = campusService.buscarCampusPorFiltro(termo, termo, termo);
            } else {
                campusList = campusService.listarCampusAtivos();
            }
            
            for (Campus campus : campusList) {
                modeloTabela.addRow(new Object[]{
                    campus.getId(),
                    campus.getNome(),
                    campus.getLocal(),
                    campus.getCnpj() != null ? campus.getCnpj() : "",
                    campus.getDiretor() != null ? campus.getDiretor() : "",
                    campus.getTelefone() != null ? campus.getTelefone() : "",
                    campus.getEmail() != null ? campus.getEmail() : ""
                });
            }
            
            atualizarContador(campusList.size());
        } catch (Exception e) {
            // Silenciar erros durante digitação
            System.err.println("Erro ao buscar campus: " + e.getMessage());
        }
    }
}
