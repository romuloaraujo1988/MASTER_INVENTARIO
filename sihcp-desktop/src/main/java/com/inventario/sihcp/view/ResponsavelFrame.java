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

import com.inventario.sihcp.model.Responsavel;
import com.inventario.sihcp.service.BusinessException;
import com.inventario.sihcp.service.ResponsavelService;

/**
 * Tela principal para gerenciamento de responsáveis
 * Design moderno com cards e visual clean
 */
public class ResponsavelFrame extends JFrame {
    
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
    
    private JTable tabelaResponsavel;
    private DefaultTableModel modeloTabela;
    private final ResponsavelService responsavelService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnExcluir;
    private JLabel lblTotalRegistros;

    public ResponsavelFrame() {
        this.responsavelService = new ResponsavelService();
        initComponents();
        carregarResponsaveis();
    }
    
    public ResponsavelFrame(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
        initComponents();
        carregarResponsaveis();
    }

    private void initComponents() {
        setTitle("Gerenciamento de Responsáveis - SIHCP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Definir ícone personalizado
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        
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
        
        setSize(1100, 550);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 450));
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        // Lado esquerdo - Título e subtítulo
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JLabel lblTitulo = new JLabel("Gerenciamento de Responsáveis");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblSubtitulo = new JLabel("Gerencie os responsáveis pelos patrimônios");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(TEXT_SECONDARY);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitulo);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Lado direito - Botão Novo Responsável
        btnNovo = createModernButton("+ Novo Responsável", SUCCESS_COLOR, Color.WHITE);
        btnNovo.setPreferredSize(new Dimension(180, 44));
        
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
        searchPanel.setPreferredSize(new Dimension(400, 40));
        
        campoBusca = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setColor(TEXT_SECONDARY);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2d.drawString("Buscar por nome, CPF, email ou cargo...", 38, 25);
                    g2d.dispose();
                }
            }
        };
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        String[] colunas = {"ID", "Nome", "CPF", "Email", "Telefone", "Cargo", "Setor", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaResponsavel = new JTable(modeloTabela);
        tabelaResponsavel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaResponsavel.setRowHeight(48);
        tabelaResponsavel.setShowGrid(false);
        tabelaResponsavel.setIntercellSpacing(new Dimension(0, 0));
        tabelaResponsavel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaResponsavel.setSelectionBackground(SELECTION_COLOR);
        tabelaResponsavel.setSelectionForeground(TEXT_PRIMARY);
        tabelaResponsavel.setFocusable(false);
        
        // Configurar larguras das colunas
        tabelaResponsavel.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tabelaResponsavel.getColumnModel().getColumn(0).setMaxWidth(70);
        tabelaResponsavel.getColumnModel().getColumn(1).setPreferredWidth(180);  // Nome
        tabelaResponsavel.getColumnModel().getColumn(2).setPreferredWidth(120);  // CPF
        tabelaResponsavel.getColumnModel().getColumn(3).setPreferredWidth(180);  // Email
        tabelaResponsavel.getColumnModel().getColumn(4).setPreferredWidth(110);  // Telefone
        tabelaResponsavel.getColumnModel().getColumn(5).setPreferredWidth(120);  // Cargo
        tabelaResponsavel.getColumnModel().getColumn(6).setPreferredWidth(150);  // Setor
        tabelaResponsavel.getColumnModel().getColumn(7).setPreferredWidth(80);   // Status
        
        // Header da tabela
        JTableHeader header = tabelaResponsavel.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        
        // Renderizador customizado para células
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
                
                // Colorir status
                if (column == 7 && value != null) {
                    String status = value.toString();
                    if ("ATIVO".equalsIgnoreCase(status)) {
                        setForeground(SUCCESS_COLOR);
                    } else if ("INATIVO".equalsIgnoreCase(status)) {
                        setForeground(DANGER_COLOR);
                    }
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < tabelaResponsavel.getColumnCount(); i++) {
            tabelaResponsavel.getColumnModel().getColumn(i).setCellRenderer(renderer);
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
        
        for (int i = 0; i < tabelaResponsavel.getColumnCount(); i++) {
            tabelaResponsavel.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(tabelaResponsavel);
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
        btnNovo.addActionListener(e -> abrirFormularioResponsavel(null));
        btnEditar.addActionListener(e -> editarResponsavel());
        btnExcluir.addActionListener(e -> excluirResponsavel());
        
        // Busca em tempo real ao digitar
        campoBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { buscarResponsaveis(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { buscarResponsaveis(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscarResponsaveis(); }
        });
        
        // Duplo clique na tabela para editar
        tabelaResponsavel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarResponsavel();
                }
            }
        });
    }

    private void carregarResponsaveis() {
        try {
            modeloTabela.setRowCount(0);
            List<Responsavel> responsaveis = responsavelService.listarTodos();
            for (Responsavel responsavel : responsaveis) {
                modeloTabela.addRow(new Object[] {
                    responsavel.getId(),
                    responsavel.getNome(),
                    responsavel.getCpf(),
                    responsavel.getEmail(),
                    responsavel.getTelefone(),
                    responsavel.getCargo(),
                    responsavel.getNomeSetor(),
                    responsavel.getStatusAtivacao()
                });
            }
            atualizarContador(responsaveis.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar responsáveis: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarContador(int total) {
        String texto = total == 1 ? "1 registro encontrado" : total + " registros encontrados";
        lblTotalRegistros.setText(texto);
    }

    private void abrirFormularioResponsavel(Responsavel responsavel) {
        ResponsavelFormDialog dialog = new ResponsavelFormDialog(this, responsavel);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarResponsaveis();
        }
    }

    private void editarResponsavel() {
        int linhaSelecionada = tabelaResponsavel.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Responsavel responsavel = responsavelService.buscarPorId(id);
                if (responsavel != null) {
                    abrirFormularioResponsavel(responsavel);
                } else {
                    JOptionPane.showMessageDialog(this, "Responsável não encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao editar responsável: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um responsável para editar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void excluirResponsavel() {
        int linhaSelecionada = tabelaResponsavel.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nome = (String) modeloTabela.getValueAt(linhaSelecionada, 1);

                int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o responsável \"" + nome + "\"?",
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                if (confirmacao == JOptionPane.YES_OPTION) {
                    try {
                        responsavelService.excluir(id);
                        JOptionPane.showMessageDialog(this, 
                            "Responsável excluído com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarResponsaveis();
                    } catch (BusinessException e) {
                        JOptionPane.showMessageDialog(this, e.getMessage(),
                            "Não é Possível Excluir", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um responsável para excluir.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void buscarResponsaveis() {
        String termo = campoBusca.getText().trim();
        modeloTabela.setRowCount(0);
        
        try {
            List<Responsavel> resultados;
            if (!termo.isEmpty()) {
                resultados = responsavelService.buscarPorFiltro(termo);
            } else {
                resultados = responsavelService.listarTodos();
            }
            
            for (Responsavel responsavel : resultados) {
                modeloTabela.addRow(new Object[] {
                    responsavel.getId(),
                    responsavel.getNome(),
                    responsavel.getCpf(),
                    responsavel.getEmail(),
                    responsavel.getTelefone(),
                    responsavel.getCargo(),
                    responsavel.getNomeSetor(),
                    responsavel.getStatusAtivacao()
                });
            }
            
            atualizarContador(resultados.size());
        } catch (Exception e) {
            // Silenciar erros durante digitação
            System.err.println("Erro ao buscar responsáveis: " + e.getMessage());
        }
    }
}
