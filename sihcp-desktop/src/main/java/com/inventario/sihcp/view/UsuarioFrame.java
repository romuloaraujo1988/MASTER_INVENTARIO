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
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.inventario.sihcp.model.PerfilUsuario;
import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.util.DateFormatUtils;

/**
 * Frame para gerenciamento de usuários do sistema
 * Design moderno com cards e visual clean
 */
public class UsuarioFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Cores do tema moderno
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 245);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color INFO_COLOR = new Color(6, 182, 212);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TABLE_HEADER_BG = new Color(249, 250, 251);
    private static final Color TABLE_STRIPE = new Color(249, 250, 251);
    private static final Color SELECTION_COLOR = new Color(59, 130, 246, 30);

    // Componentes da interface
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private JTextField campoBusca;
    private JComboBox<String> comboFiltroTipo;
    private JButton btnNovo, btnEditar, btnExcluir;
    private JButton btnBloquear, btnDesbloquear, btnAlterarSenha;
    private JLabel lblTotalRegistros;

    // DAOs
    private final com.inventario.sihcp.dao.UsuarioDAO usuarioDAO;
    private final com.inventario.sihcp.dao.SetorDAO setorDAO;

    public UsuarioFrame() {
        this.usuarioDAO = new com.inventario.sihcp.dao.UsuarioDAO();
        this.setorDAO = new com.inventario.sihcp.dao.SetorDAO();
        initComponents();
        carregarUsuarios();
    }

    private void initComponents() {
        setTitle("Gerenciamento de Usuários - SIHCP");
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

        setSize(1400, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 550));
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Lado esquerdo - Título e subtítulo
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND_COLOR);

        JLabel lblTitulo = new JLabel("Gerenciamento de Usuários");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Gerencie os usuários e permissões do sistema");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(TEXT_SECONDARY);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitulo);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Lado direito - Botão Novo Usuário
        btnNovo = createModernButton("+ Novo Usuário", SUCCESS_COLOR, Color.WHITE);
        btnNovo.setPreferredSize(new Dimension(160, 44));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setBackground(BACKGROUND_COLOR);
        btnPanel.add(btnNovo);

        headerPanel.add(btnPanel, BorderLayout.EAST);

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

        // Painel de busca (esquerda)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        // Checkbox mostrar inativos
        // O checkbox 'Mostrar Inativos' foi removido pois agora todos são visíveis por
        // padrão

        // ComboBox de tipo de filtro
        comboFiltroTipo = new JComboBox<>(new String[] { "Todos", "Nome", "Login", "Email", "Perfil", "Setor" });
        comboFiltroTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboFiltroTipo.setPreferredSize(new Dimension(120, 40));
        comboFiltroTipo.setBackground(Color.WHITE);
        searchPanel.add(comboFiltroTipo);

        // Campo de busca com ícone
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setOpaque(false);
        searchFieldPanel.setPreferredSize(new Dimension(350, 40));

        campoBusca = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setColor(TEXT_SECONDARY);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2d.drawString("Buscar usuário...", 38, 25);
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

        searchFieldPanel.add(searchWrapper, BorderLayout.CENTER);
        searchPanel.add(searchFieldPanel);

        toolbar.add(searchPanel, BorderLayout.WEST);

        // Botões de ação (direita)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

        // Grupo de botões: Ações principais
        JPanel primaryActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        primaryActions.setOpaque(false);

        btnEditar = createModernButton("✏ Editar", PRIMARY_COLOR, Color.WHITE);
        btnEditar.setPreferredSize(new Dimension(100, 38));

        btnExcluir = createModernButton("🗑 Excluir", DANGER_COLOR, Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 38));

        // Grupo de botões: Segurança
        JPanel securityActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        securityActions.setOpaque(false);
        securityActions.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

        btnBloquear = createModernButton("🔒 Bloquear", WARNING_COLOR, Color.WHITE);
        btnBloquear.setPreferredSize(new Dimension(115, 38));

        btnDesbloquear = createModernButton("🔓 Desbloquear", SUCCESS_COLOR, Color.WHITE);
        btnDesbloquear.setPreferredSize(new Dimension(130, 38));

        btnAlterarSenha = createModernButton("🔑 Senha", INFO_COLOR, Color.WHITE);
        btnAlterarSenha.setPreferredSize(new Dimension(100, 38));

        primaryActions.add(btnEditar);
        primaryActions.add(btnExcluir);

        securityActions.add(btnBloquear);
        securityActions.add(btnDesbloquear);
        securityActions.add(btnAlterarSenha);

        actionsPanel.add(primaryActions);
        actionsPanel.add(securityActions);

        toolbar.add(actionsPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createTablePanel() {
        String[] colunas = { "ID", "Login", "Nome Completo", "Email", "Matrícula", "Perfil",
                "Setor", "Ativo", "Bloqueado", "Último Acesso", "Data Criação" };

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.setAutoCreateRowSorter(true);
        tabelaUsuarios.setRowHeight(48);
        tabelaUsuarios.setShowGrid(false);
        tabelaUsuarios.setIntercellSpacing(new Dimension(0, 0));
        tabelaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaUsuarios.setSelectionBackground(SELECTION_COLOR);
        tabelaUsuarios.setSelectionForeground(TEXT_PRIMARY);
        tabelaUsuarios.setFocusable(false);

        // Configurar larguras das colunas
        tabelaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        tabelaUsuarios.getColumnModel().getColumn(0).setMaxWidth(70);
        tabelaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100); // Login
        tabelaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(180); // Nome
        tabelaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(180); // Email
        tabelaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(100); // Matrícula
        tabelaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(90); // Perfil
        tabelaUsuarios.getColumnModel().getColumn(6).setPreferredWidth(180); // Setor
        tabelaUsuarios.getColumnModel().getColumn(7).setPreferredWidth(60); // Ativo
        tabelaUsuarios.getColumnModel().getColumn(8).setPreferredWidth(80); // Bloqueado
        tabelaUsuarios.getColumnModel().getColumn(9).setPreferredWidth(130); // Último Acesso
        tabelaUsuarios.getColumnModel().getColumn(10).setPreferredWidth(130); // Data Criação

        // Header da tabela
        JTableHeader header = tabelaUsuarios.getTableHeader();
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

                // Detectar se a linha representa um usuário inativo (coluna 7 = "Não")
                Object ativoVal = table.getModel().getValueAt(
                        table.convertRowIndexToModel(row), 7);
                boolean inativo = "Não".equals(ativoVal);

                if (!isSelected) {
                    c.setBackground(inativo
                            ? new Color(245, 245, 245) // cinza claro para inativos
                            : (row % 2 == 0 ? Color.WHITE : TABLE_STRIPE));
                }

                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                // Inativos aparecem em cinza com fonte itálica
                if (inativo && !isSelected) {
                    setForeground(new Color(160, 160, 160));
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    setForeground(TEXT_PRIMARY);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                // Centralizar ID
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                // Colorir Ativo/Bloqueado
                if (value != null && !inativo) {
                    String val = value.toString();
                    if (column == 7) { // Ativo
                        setForeground("Sim".equals(val) ? SUCCESS_COLOR : DANGER_COLOR);
                    } else if (column == 8) { // Bloqueado
                        setForeground("Sim".equals(val) ? DANGER_COLOR : SUCCESS_COLOR);
                    }
                }

                return c;
            }
        };

        for (int i = 0; i < tabelaUsuarios.getColumnCount(); i++) {
            tabelaUsuarios.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Header renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setBackground(TABLE_HEADER_BG);
                label.setForeground(TEXT_SECONDARY);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                label.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return label;
            }
        };

        for (int i = 0; i < tabelaUsuarios.getColumnCount(); i++) {
            tabelaUsuarios.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
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

        lblTotalRegistros = new JLabel("0 usuários encontrados");
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
        btnNovo.addActionListener(e -> novoUsuario());
        btnEditar.addActionListener(e -> editarUsuarioSelecionado());
        btnExcluir.addActionListener(e -> excluirUsuarioSelecionado());
        btnBloquear.addActionListener(e -> bloquearUsuarioSelecionado());
        btnDesbloquear.addActionListener(e -> desbloquearUsuarioSelecionado());
        btnAlterarSenha.addActionListener(e -> alterarSenhaUsuarioSelecionado());

        // Removido listener do chkMostrarInativos

        // Busca em tempo real ao digitar
        campoBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltro();
            }
        });

        // Duplo clique na tabela para editar
        tabelaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarUsuarioSelecionado();
                }
            }
        });

        // Atualizar estado dos botões quando seleção muda
        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarEstadoBotoes();
            }
        });
    }

    private void carregarUsuarios() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Usuario> usuarios = usuarioDAO.findAll();
                atualizarTabelaUsuarios(usuarios);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao carregar usuários: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void atualizarTabelaUsuarios(List<Usuario> usuarios) {
        modeloTabela.setRowCount(0);

        for (Usuario usuario : usuarios) {
            Object[] linha = {
                    usuario.getId(),
                    usuario.getLogin(),
                    usuario.getNomeCompleto(),
                    usuario.getEmail(),
                    usuario.getMatricula(),
                    usuario.getPerfil() != null ? usuario.getPerfil().name() : "",
                    usuario.getNomeSetor() != null ? usuario.getNomeSetor() : "",
                    usuario.getAtivo() ? "Sim" : "Não",
                    usuario.getBloqueado() ? "Sim" : "Não",
                    usuario.getDataUltimoAcesso() != null
                            ? DateFormatUtils.formatDateTime(usuario.getDataUltimoAcesso())
                            : "Nunca",
                    usuario.getDataCriacao() != null ? DateFormatUtils.formatDateTime(usuario.getDataCriacao()) : ""
            };
            modeloTabela.addRow(linha);
        }

        atualizarContador(usuarios.size());
        atualizarEstadoBotoes();
    }

    private void atualizarContador(int total) {
        String texto = total == 1 ? "1 usuário encontrado" : total + " usuários encontrados";
        lblTotalRegistros.setText(texto);
    }

    private void aplicarFiltro() {
        String filtro = campoBusca.getText().trim();
        String tipoFiltro = (String) comboFiltroTipo.getSelectedItem();

        if (filtro.isEmpty()) {
            carregarUsuarios();
            return;
        }

        try {
            // Respeita o checkbox "Mostrar inativos" ao filtrar
            List<Usuario> base = usuarioDAO.findAll();

            List<Usuario> usuariosFiltrados;

            switch (tipoFiltro) {
                case "Nome", "Login", "Email" -> usuariosFiltrados = base.stream()
                        .filter(u -> u.getNomeCompleto().toLowerCase().contains(filtro.toLowerCase()) ||
                                u.getLogin().toLowerCase().contains(filtro.toLowerCase()) ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(filtro.toLowerCase())))
                        .collect(java.util.stream.Collectors.toList());
                case "Perfil" -> {
                    try {
                        PerfilUsuario perfil = PerfilUsuario.valueOf(filtro.toUpperCase());
                        usuariosFiltrados = base.stream()
                                .filter(u -> u.getPerfil() == perfil)
                                .collect(java.util.stream.Collectors.toList());
                    } catch (IllegalArgumentException e) {
                        usuariosFiltrados = base.stream()
                                .filter(u -> u.getPerfil() != null &&
                                        u.getPerfil().name().toLowerCase().contains(filtro.toLowerCase()))
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
                case "Setor" -> usuariosFiltrados = base.stream()
                        .filter(u -> u.getNomeSetor() != null &&
                                u.getNomeSetor().toLowerCase().contains(filtro.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
                default -> usuariosFiltrados = base.stream()
                        .filter(u -> u.getNomeCompleto().toLowerCase().contains(filtro.toLowerCase()) ||
                                u.getLogin().toLowerCase().contains(filtro.toLowerCase()) ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(filtro.toLowerCase())) ||
                                (u.getNomeSetor() != null
                                        && u.getNomeSetor().toLowerCase().contains(filtro.toLowerCase())))
                        .collect(java.util.stream.Collectors.toList());
            }

            atualizarTabelaUsuarios(usuariosFiltrados);

        } catch (SQLException e) {
            System.err.println("Erro ao aplicar filtro: " + e.getMessage());
        }
    }

    private void novoUsuario() {
        UsuarioFormDialog dialog = new UsuarioFormDialog(this, null, usuarioDAO, setorDAO);
        dialog.setVisible(true);

        if (dialog.isUsuarioSalvo()) {
            carregarUsuarios();
        }
    }

    private void editarUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
            Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);

            Usuario usuario = usuarioDAO.findById(idUsuario);

            if (usuario != null) {
                UsuarioFormDialog dialog = new UsuarioFormDialog(this, usuario, usuarioDAO, setorDAO);
                dialog.setVisible(true);

                if (dialog.isUsuarioSalvo()) {
                    carregarUsuarios();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuário não encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados do usuário: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
        Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
        String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);
        String loginUsuario = (String) modeloTabela.getValueAt(modelRow, 1);

        try {
            boolean temVinculo = usuarioDAO.temVinculoComInventario(idUsuario);

            if (temVinculo) {
                // Usuário possui histórico em inventários — exclusão física proibida
                int opcao = JOptionPane.showConfirmDialog(this,
                        "O usuário '" + nomeUsuario + "' (login: " + loginUsuario + ")\n" +
                                "possui vínculos com inventários e NÃO pode ser excluído fisicamente,\n" +
                                "pois sua exclusão apagaria o histórico de participação.\n\n" +
                                "Deseja DESATIVAR o usuário em vez disso?\n" +
                                "(O usuário ficará inativo e não conseguirá acessar o sistema.)",
                        "Exclusão Não Permitida — Desativar?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (opcao == JOptionPane.YES_OPTION) {
                    usuarioDAO.delete(idUsuario); // soft-delete
                    JOptionPane.showMessageDialog(this,
                            "Usuário '" + nomeUsuario + "' foi desativado.",
                            "Usuário Desativado",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarUsuarios();
                }
                return;
            }

            // Usuário sem vínculo — permite exclusão física com dupla confirmação
            int opcao1 = JOptionPane.showConfirmDialog(this,
                    "O usuário '" + nomeUsuario + "' (login: " + loginUsuario + ")\n" +
                            "não possui vínculo com nenhum inventário.\n\n" +
                            "Deseja EXCLUIR PERMANENTEMENTE este usuário?\n\n" +
                            "Esta ação é IRREVERSÍVEL!",
                    "Confirmar Exclusão Física",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (opcao1 != JOptionPane.YES_OPTION) {
                return;
            }

            int opcao2 = JOptionPane.showConfirmDialog(this,
                    "ÚLTIMA CONFIRMAÇÃO\n\n" +
                            "Tem ABSOLUTA CERTEZA que deseja excluir permanentemente\n" +
                            "o usuário '" + nomeUsuario + "'?\n\n" +
                            "Esta ação NÃO PODE SER DESFEITA!",
                    "Confirmação Final",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);

            if (opcao2 == JOptionPane.YES_OPTION) {
                usuarioDAO.deleteFisico(idUsuario);
                JOptionPane.showMessageDialog(this,
                        "Usuário '" + nomeUsuario + "' foi excluído permanentemente.",
                        "Exclusão Concluída",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarUsuarios();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao excluir usuário: " + ex.getMessage(),
                    "Erro na Exclusão",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bloquearUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para bloquear.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
        Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
        String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);

        int opcao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja bloquear o usuário '" + nomeUsuario + "'?",
                "Confirmar Bloqueio", JOptionPane.YES_NO_OPTION);

        if (opcao == JOptionPane.YES_OPTION) {
            try {
                Usuario u = usuarioDAO.findById(idUsuario);
                if (u != null) {
                    u.setBloqueado(true);
                    usuarioDAO.update(u);
                    JOptionPane.showMessageDialog(this,
                            "Usuário bloqueado com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarUsuarios();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao bloquear usuário: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void desbloquearUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para desbloquear.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
        Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
        String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);

        int opcao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja desbloquear o usuário '" + nomeUsuario + "'?",
                "Confirmar Desbloqueio", JOptionPane.YES_NO_OPTION);

        if (opcao == JOptionPane.YES_OPTION) {
            try {
                Usuario u = usuarioDAO.findById(idUsuario);
                if (u != null) {
                    u.setBloqueado(false);
                    usuarioDAO.update(u);
                    JOptionPane.showMessageDialog(this,
                            "Usuário desbloqueado com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarUsuarios();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao desbloquear usuário: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void alterarSenhaUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário para alterar a senha.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabelaUsuarios.convertRowIndexToModel(linhaSelecionada);
        Integer idUsuario = (Integer) modeloTabela.getValueAt(modelRow, 0);
        String nomeUsuario = (String) modeloTabela.getValueAt(modelRow, 2);

        AlterarSenhaDialog dialog = new AlterarSenhaDialog(this, idUsuario, nomeUsuario);
        dialog.setVisible(true);

        if (dialog.isSenhaSalva()) {
            carregarUsuarios();
        }
    }

    private void atualizarEstadoBotoes() {
        boolean usuarioSelecionado = tabelaUsuarios.getSelectedRow() != -1;

        btnEditar.setEnabled(usuarioSelecionado);
        btnExcluir.setEnabled(usuarioSelecionado);
        btnBloquear.setEnabled(usuarioSelecionado);
        btnDesbloquear.setEnabled(usuarioSelecionado);
        btnAlterarSenha.setEnabled(usuarioSelecionado);
    }
}
