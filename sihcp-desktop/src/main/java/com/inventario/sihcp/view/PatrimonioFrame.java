package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.repository.impl.PatrimonioRepositoryImpl;
import com.inventario.sihcp.service.PatrimonioService;
import com.inventario.sihcp.view.ui.ButtonStyleFactory;

/**
 * Tela principal para gerenciamento de patrimônios
 * Design moderno com cards e visual clean
 */
public class PatrimonioFrame extends JFrame {

    // Cores do tema moderno
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 245);
    private static final Color CARD_COLOR = Color.WHITE;
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

    private JTable tabelaPatrimonio;
    private DefaultTableModel modeloTabela;
    private final PatrimonioService patrimonioService;
    private JTextField campoBusca;
    private JComboBox<String> comboTipoBusca;
    private JButton btnNovo, btnEditar, btnExcluir, btnImportar, btnImprimir;
    private JLabel lblTotalRegistros;

    // Variáveis para manter o estado da última busca
    private String ultimoTermoBusca = "";
    private String ultimoTipoBusca = "";
    private boolean ultimaBuscaFoiCarregarTodos = false;

    public PatrimonioFrame() {
        PatrimonioDAO patrimonioDAO = new PatrimonioDAO();
        PatrimonioRepositoryImpl patrimonioRepository = new PatrimonioRepositoryImpl(patrimonioDAO);
        this.patrimonioService = new PatrimonioService(patrimonioRepository);
        initComponents();
    }

    public PatrimonioFrame(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
        initComponents();
    }

    private void initComponents() {
        setTitle("Gerenciamento de Patrimônios - SIHCP");
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

        JLabel lblTitulo = new JLabel("Gerenciamento de Patrimônios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Gerencie os patrimônios cadastrados no sistema");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(TEXT_SECONDARY);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitulo);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Lado direito - Botão Novo Patrimônio
        btnNovo = ButtonStyleFactory.createSuccessButton("+ Novo Patrimônio");
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

        // Painel de busca (esquerda)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        // ComboBox de tipo de busca
        comboTipoBusca = new JComboBox<>(
                new String[] { "Todos os campos", "Número", "Descrição", "Responsável", "Sala" });
        comboTipoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboTipoBusca.setPreferredSize(new Dimension(150, 40));
        comboTipoBusca.setBackground(Color.WHITE);
        searchPanel.add(comboTipoBusca);

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
                    g2d.drawString("Buscar patrimônio...", 38, 25);
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

        searchFieldPanel.add(searchWrapper, BorderLayout.CENTER);
        searchPanel.add(searchFieldPanel);

        toolbar.add(searchPanel, BorderLayout.WEST);

        // Botões de ação (direita)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        btnEditar = ButtonStyleFactory.createWarningButton("✏ Editar");
        btnEditar.setPreferredSize(new Dimension(110, 38));

        btnExcluir = ButtonStyleFactory.createDangerButton("🗑 Excluir");
        btnExcluir.setPreferredSize(new Dimension(110, 38));

        btnImportar = ButtonStyleFactory.createInfoButton("📥 Importar CSV");
        btnImportar.setPreferredSize(new Dimension(140, 38));

        btnImprimir = ButtonStyleFactory.createPrimaryButton("🏷 Imprimir Etiquetas");
        btnImprimir.setPreferredSize(new Dimension(180, 38));

        actionsPanel.add(btnEditar);
        actionsPanel.add(btnExcluir);
        actionsPanel.add(btnImportar);
        actionsPanel.add(btnImprimir);

        toolbar.add(actionsPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createTablePanel() {
        String[] colunas = { "ID", "Número", "Descrição", "Marca", "Modelo", "Estado", "Situação", "Sala",
                "Responsável", "Data Entrada" };
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPatrimonio = new JTable(modeloTabela);
        tabelaPatrimonio.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabelaPatrimonio.setRowHeight(48);
        tabelaPatrimonio.setShowGrid(false);
        tabelaPatrimonio.setIntercellSpacing(new Dimension(0, 0));
        tabelaPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaPatrimonio.setSelectionBackground(SELECTION_COLOR);
        tabelaPatrimonio.setSelectionForeground(TEXT_PRIMARY);
        tabelaPatrimonio.setFocusable(false);

        // Configurar larguras das colunas
        tabelaPatrimonio.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        tabelaPatrimonio.getColumnModel().getColumn(0).setMaxWidth(80);
        tabelaPatrimonio.getColumnModel().getColumn(1).setPreferredWidth(100); // Número
        tabelaPatrimonio.getColumnModel().getColumn(2).setPreferredWidth(250); // Descrição
        tabelaPatrimonio.getColumnModel().getColumn(3).setPreferredWidth(100); // Marca
        tabelaPatrimonio.getColumnModel().getColumn(4).setPreferredWidth(100); // Modelo
        tabelaPatrimonio.getColumnModel().getColumn(5).setPreferredWidth(90); // Estado
        tabelaPatrimonio.getColumnModel().getColumn(6).setPreferredWidth(80); // Situação
        tabelaPatrimonio.getColumnModel().getColumn(7).setPreferredWidth(150); // Sala
        tabelaPatrimonio.getColumnModel().getColumn(8).setPreferredWidth(150); // Responsável

        // Header da tabela
        JTableHeader header = tabelaPatrimonio.getTableHeader();
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

                // Colorir situação
                if (column == 6 && value != null) {
                    String situacao = value.toString();
                    if ("ATIVO".equalsIgnoreCase(situacao)) {
                        setForeground(SUCCESS_COLOR);
                    } else if ("INATIVO".equalsIgnoreCase(situacao) || "BAIXADO".equalsIgnoreCase(situacao)) {
                        setForeground(DANGER_COLOR);
                    }
                }

                return c;
            }
        };

        for (int i = 0; i < tabelaPatrimonio.getColumnCount(); i++) {
            tabelaPatrimonio.getColumnModel().getColumn(i).setCellRenderer(renderer);
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
                        BorderFactory.createEmptyBorder(0, 15, 0, 15)));
                label.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return label;
            }
        };

        for (int i = 0; i < tabelaPatrimonio.getColumnCount(); i++) {
            tabelaPatrimonio.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tabelaPatrimonio);
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

        lblTotalRegistros = new JLabel("💡 Digite para buscar ou pressione Enter para carregar todos");
        lblTotalRegistros.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblTotalRegistros.setForeground(TEXT_SECONDARY);

        footer.add(lblTotalRegistros, BorderLayout.WEST);

        return footer;
    }

    private void configurarEventos() {
        btnNovo.addActionListener(e -> abrirFormularioPatrimonio(null));
        btnEditar.addActionListener(e -> editarPatrimonio());
        btnExcluir.addActionListener(e -> excluirPatrimonio());
        btnImportar.addActionListener(e -> importarCSV());

        btnImprimir.addActionListener(e -> imprimirEtiquetasSelecionadas());

        // Busca em tempo real ao digitar (com delay para performance)
        campoBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer;

            private void scheduleSearch() {
                if (timer != null) {
                    timer.stop();
                }
                timer = new javax.swing.Timer(300, evt -> buscarPatrimonios());
                timer.setRepeats(false);
                timer.start();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }
        });

        // Enter para buscar
        campoBusca.addActionListener(e -> {
            if (campoBusca.getText().trim().isEmpty()) {
                carregarPatrimonios();
            } else {
                buscarPatrimonios();
            }
        });

        // Duplo clique na tabela para editar
        tabelaPatrimonio.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarPatrimonio();
                }
            }
        });
    }

    private void imprimirEtiquetasSelecionadas() {
        try {
            int[] selectedRows = tabelaPatrimonio.getSelectedRows();
            List<Patrimonio> selecionados = new ArrayList<>();

            if (selectedRows.length > 0) {
                for (int row : selectedRows) {
                    int modelRow = tabelaPatrimonio.convertRowIndexToModel(row);
                    Object idObj = modeloTabela.getValueAt(modelRow, 0);
                    Integer id = null;
                    if (idObj instanceof Integer)
                        id = (Integer) idObj;
                    else if (idObj instanceof Number)
                        id = ((Number) idObj).intValue();

                    if (id != null) {
                        Optional<Patrimonio> p = patrimonioService.buscarPorId(id);
                        p.ifPresent(selecionados::add);
                    }
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Nenhum patrimônio selecionado. Deseja imprimir etiquetas de TODOS os registros visíveis na tabela?",
                        "Imprimir Tudo?", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    for (int i = 0; i < tabelaPatrimonio.getRowCount(); i++) {
                        int modelRow = tabelaPatrimonio.convertRowIndexToModel(i);
                        Object idObj = modeloTabela.getValueAt(modelRow, 0);
                        Integer id = null;
                        if (idObj instanceof Integer)
                            id = (Integer) idObj;
                        else if (idObj instanceof Number)
                            id = ((Number) idObj).intValue();

                        if (id != null) {
                            Optional<Patrimonio> p = patrimonioService.buscarPorId(id);
                            p.ifPresent(selecionados::add);
                        }
                    }
                } else {
                    return;
                }
            }

            if (!selecionados.isEmpty()) {
                ImpressaoEtiquetasDialog dialog = new ImpressaoEtiquetasDialog(this, selecionados);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum patrimônio válido encontrado para impressão.", "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erro ao abrir editor de etiquetas: " + ex.getMessage() + "\nVerifique os logs para mais detalhes.",
                    "Erro Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarPatrimonios() {
        try {
            modeloTabela.setRowCount(0);
            List<Patrimonio> patrimonios = patrimonioService.listarTodos();
            for (Patrimonio p : patrimonios) {
                modeloTabela.addRow(new Object[] {
                        p.getId(),
                        p.getNumero(),
                        p.getDescricao(),
                        p.getMarca(),
                        p.getModelo(),
                        p.getEstadoConservacao(),
                        p.getSituacao() != null ? p.getSituacao() : "ATIVO",
                        p.getNomeSala() != null ? p.getNomeSala() : "Não definida",
                        p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "Não definido",
                        p.getDataEntradaFormatada()
                });
            }
            ultimoTermoBusca = "";
            ultimoTipoBusca = "";
            ultimaBuscaFoiCarregarTodos = true;
            atualizarContador(patrimonios.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar patrimônios: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarContador(int total) {
        String texto = total == 1 ? "1 patrimônio encontrado" : total + " patrimônios encontrados";
        lblTotalRegistros.setText(texto);
    }

    private void abrirFormularioPatrimonio(Patrimonio patrimonio) {
        PatrimonioFormDialog dialog = new PatrimonioFormDialog(this, patrimonio);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            recarregarDadosAtuais();
        }
    }

    private void editarPatrimonio() {
        int linhaSelecionada = tabelaPatrimonio.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Optional<Patrimonio> patrimonioOpt = patrimonioService.buscarPorId(id);
                if (patrimonioOpt.isPresent()) {
                    abrirFormularioPatrimonio(patrimonioOpt.get());
                } else {
                    JOptionPane.showMessageDialog(this, "Patrimônio não encontrado.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HeadlessException e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao carregar patrimônio: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um patrimônio para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void excluirPatrimonio() {
        int linhaSelecionada = tabelaPatrimonio.getSelectedRow();
        if (linhaSelecionada >= 0) {
            String numeroPatrimonio = (String) modeloTabela.getValueAt(linhaSelecionada, 1);
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o patrimônio " + numeroPatrimonio + "?",
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                    patrimonioService.excluir(id);
                    JOptionPane.showMessageDialog(this, "Patrimônio excluído com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    recarregarDadosAtuais();
                } catch (HeadlessException e) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao excluir patrimônio: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um patrimônio para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void buscarPatrimonios() {
        String termo = campoBusca.getText().trim();
        if (termo.isEmpty()) {
            modeloTabela.setRowCount(0);
            lblTotalRegistros.setText("💡 Digite para buscar ou pressione Enter para carregar todos");
            return;
        }

        try {
            modeloTabela.setRowCount(0);
            List<Patrimonio> resultados;

            String tipoBusca = (String) comboTipoBusca.getSelectedItem();

            switch (tipoBusca) {
                case "Número":
                    Optional<Patrimonio> patrimonioEncontrado = patrimonioService.buscarPorNumero(termo);
                    resultados = new ArrayList<>();
                    patrimonioEncontrado.ifPresent(resultados::add);
                    break;
                case "Descrição":
                    resultados = buscarPorDescricao(termo);
                    break;
                case "Responsável":
                    resultados = buscarPorResponsavel(termo);
                    break;
                case "Sala":
                    resultados = buscarPorSala(termo);
                    break;
                case "Todos os campos":
                default:
                    resultados = patrimonioService.listarTodos().stream()
                            .filter(p -> (p.getNumero() != null
                                    && p.getNumero().toLowerCase().contains(termo.toLowerCase())) ||
                                    (p.getDescricao() != null
                                            && p.getDescricao().toLowerCase().contains(termo.toLowerCase()))
                                    ||
                                    (p.getMarca() != null && p.getMarca().toLowerCase().contains(termo.toLowerCase()))
                                    ||
                                    (p.getModelo() != null && p.getModelo().toLowerCase().contains(termo.toLowerCase()))
                                    ||
                                    (p.getNomeSala() != null
                                            && p.getNomeSala().toLowerCase().contains(termo.toLowerCase()))
                                    ||
                                    (p.getNomeResponsavel() != null
                                            && p.getNomeResponsavel().toLowerCase().contains(termo.toLowerCase())))
                            .collect(java.util.stream.Collectors.toList());
                    break;
            }

            ultimoTermoBusca = termo;
            ultimoTipoBusca = tipoBusca;
            ultimaBuscaFoiCarregarTodos = false;

            for (Patrimonio p : resultados) {
                modeloTabela.addRow(new Object[] {
                        p.getId(),
                        p.getNumero(),
                        p.getDescricao(),
                        p.getMarca(),
                        p.getModelo(),
                        p.getEstadoConservacao(),
                        p.getSituacao() != null ? p.getSituacao() : "ATIVO",
                        p.getNomeSala() != null ? p.getNomeSala() : "Não definida",
                        p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "Não definido",
                        p.getDataEntradaFormatada()
                });
            }

            atualizarContador(resultados.size());
        } catch (Exception e) {
            // Silenciar erros durante digitação
            System.err.println("Erro ao buscar patrimônios: " + e.getMessage());
        }
    }

    private List<Patrimonio> buscarPorDescricao(String descricao) throws Exception {
        return patrimonioService.listarTodos().stream()
                .filter(p -> p.getDescricao() != null
                        && p.getDescricao().toLowerCase().contains(descricao.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<Patrimonio> buscarPorResponsavel(String nomeResponsavel) throws Exception {
        return patrimonioService.listarTodos().stream()
                .filter(p -> p.getNomeResponsavel() != null
                        && p.getNomeResponsavel().toLowerCase().contains(nomeResponsavel.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<Patrimonio> buscarPorSala(String nomeSala) throws Exception {
        return patrimonioService.listarTodos().stream()
                .filter(p -> p.getNomeSala() != null && p.getNomeSala().toLowerCase().contains(nomeSala.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    private void importarCSV() {
        try {
            ImportacaoCSVFrame importacaoFrame = new ImportacaoCSVFrame();
            importacaoFrame.setVisible(true);

            importacaoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    recarregarDadosAtuais();
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao abrir importação CSV: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recarregarDadosAtuais() {
        if (ultimaBuscaFoiCarregarTodos) {
            carregarPatrimonios();
        } else if (!ultimoTermoBusca.isEmpty()) {
            String termoAtual = campoBusca.getText();
            String tipoAtual = (String) comboTipoBusca.getSelectedItem();

            campoBusca.setText(ultimoTermoBusca);
            comboTipoBusca.setSelectedItem(ultimoTipoBusca);

            buscarPatrimonios();

            campoBusca.setText(termoAtual);
            comboTipoBusca.setSelectedItem(tipoAtual);
        }
    }
}
