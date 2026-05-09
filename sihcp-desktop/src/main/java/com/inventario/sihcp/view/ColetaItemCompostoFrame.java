package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.ItemCompostoDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Usuario;

/**
 * Frame para coleta de itens compostos durante o inventário
 * Permite registrar a coleta de cada componente individualmente
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ColetaItemCompostoFrame extends JFrame {
    
    private static final Logger LOG = LoggerFactory.getLogger(ColetaItemCompostoFrame.class);
    
    private Usuario usuarioLogado;
    private Inventario inventarioAtivo;
    private PatrimonioDAO patrimonioDAO;
    private InventarioDAO inventarioDAO;
    private ItemCompostoDAO itemCompostoDAO;
    
    // Componentes de busca
    private JTextField txtNumeroPatrimonio;
    private JTextField txtBuscaDescricao;
    private JButton btnBuscar;
    private JButton btnBuscarDescricao;
    private JButton btnLimpar;
    
    // Aba de listagem
    private JTabbedPane tabbedPane;
    private JTable tblItensCompostos;
    private DefaultTableModel modelItensCompostos;
    private JButton btnCarregarSelecionado;
    
    // Dados do item composto
    private JLabel lblPatrimonioId;
    private JLabel lblNumeroPatrimonio;  // Número do patrimônio em destaque
    private JLabel lblDescricao;
    private JLabel lblSala;
    private JLabel lblResponsavel;
    private JLabel lblStatusGeral;
    
    // Tabela de componentes
    private JTable tblComponentes;
    private DefaultTableModel modelComponentes;
    
    // Botões de ação
    private JButton btnRegistrarComponente;
    private JButton btnMarcarTodosEncontrados;
    private JButton btnFinalizarColeta;
    private JButton btnCancelar;
    
    // Dados
    private Patrimonio patrimonioAtual;
    private List<ComponenteColeta> componentesColeta;
    
    // Localização atual de coleta
    private JComboBox<String> cmbLocalizacaoAtual;
    private JTextField txtLocalizacaoManual;
    
    // Aba de histórico
    private JTable tblHistorico;
    private DefaultTableModel modelHistorico;
    private JButton btnAtualizarHistorico;
    private JLabel lblTotalHistorico;
    
    public ColetaItemCompostoFrame(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.patrimonioDAO = new PatrimonioDAO();
        this.inventarioDAO = new InventarioDAO();
        this.itemCompostoDAO = new ItemCompostoDAO();
        this.componentesColeta = new ArrayList<>();
        
        carregarInventarioAtivo();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("Coleta de Itens Compostos - Inventário");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        
        // ✅ Garantir foco no campo de busca ao abrir a janela
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtNumeroPatrimonio.requestFocusInWindow();
            }
        });
    }
    
    private void carregarInventarioAtivo() {
        try {
            // Buscar inventário ativo do banco de dados
            this.inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            
            if (this.inventarioAtivo == null) {
                JOptionPane.showMessageDialog(this, """
                                                    Nenhum invent\u00e1rio ativo encontrado.
                                                    \u00c9 necess\u00e1rio ter um invent\u00e1rio em andamento para realizar coletas.""",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                System.out.println("AVISO: Nenhum inventário ativo encontrado");
            } else {
                System.out.println("Inventário ativo carregado: " + inventarioAtivo.getNome() + 
                                 " (ID: " + inventarioAtivo.getId() + ")");
            }
            
        } catch (java.sql.SQLException | RuntimeException e) {
            LOG.error("Erro ao carregar inventário ativo: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar inventário ativo: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initializeComponents() {
        // Busca por número
        txtNumeroPatrimonio = new JTextField(15);
        txtNumeroPatrimonio.setFont(new Font("Arial", Font.PLAIN, 14));
        btnBuscar = createButton("🔍 Buscar", new Color(52, 152, 219));
        btnLimpar = createButton("🗑️ Limpar", new Color(149, 165, 166));
        
        // Busca por descrição
        txtBuscaDescricao = new JTextField(25);
        txtBuscaDescricao.setFont(new Font("Arial", Font.PLAIN, 14));
        btnBuscarDescricao = createButton("🔍 Filtrar", new Color(155, 89, 182));
        
        // Tabela de itens compostos cadastrados
        String[] colunasItens = {"Número", "Descrição", "Componentes", "Sala"};
        modelItensCompostos = new DefaultTableModel(colunasItens, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItensCompostos = new JTable(modelItensCompostos);
        tblItensCompostos.setFont(new Font("Arial", Font.PLAIN, 12));
        tblItensCompostos.setRowHeight(28);
        tblItensCompostos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblItensCompostos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblItensCompostos.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblItensCompostos.getColumnModel().getColumn(1).setPreferredWidth(400);
        tblItensCompostos.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblItensCompostos.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        btnCarregarSelecionado = createButton("📋 Carregar Selecionado", new Color(39, 174, 96));
        
        // Labels de dados
        lblPatrimonioId = createDataLabel("");
        lblNumeroPatrimonio = createDataLabel("");
        lblNumeroPatrimonio.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        lblNumeroPatrimonio.setForeground(new Color(52, 152, 219));
        lblDescricao = createDataLabel("");
        lblSala = createDataLabel("");
        lblResponsavel = createDataLabel("");
        lblStatusGeral = createDataLabel("");
        lblStatusGeral.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Seletor de localização atual
        cmbLocalizacaoAtual = new JComboBox<>();
        cmbLocalizacaoAtual.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbLocalizacaoAtual.setPreferredSize(new Dimension(200, 30));
        carregarSalasComboBox();
        
        txtLocalizacaoManual = new JTextField(15);
        txtLocalizacaoManual.setFont(new Font("Arial", Font.PLAIN, 12));
        txtLocalizacaoManual.setToolTipText("Digite um local manualmente se não estiver na lista");
        
        // Tabela de componentes - ADICIONADA COLUNA DE LOCALIZAÇÃO
        String[] colunas = {"Tipo", "Descrição", "Qtd. Esperada", "Qtd. Encontrada", "Local Encontrado", "Status", "Observação"};
        modelComponentes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblComponentes = new JTable(modelComponentes);
        tblComponentes.setFont(new Font("Arial", Font.PLAIN, 12));
        tblComponentes.setRowHeight(30);
        tblComponentes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblComponentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Botões de ação
        btnRegistrarComponente = createButton("✓ Registrar Componente", new Color(46, 204, 113));
        btnMarcarTodosEncontrados = createButton("✓ Marcar Todos no Local", new Color(52, 152, 219));
        btnFinalizarColeta = createButton("📋 Próximo Item", new Color(39, 174, 96));
        btnFinalizarColeta.setToolTipText("<html><b>Limpa o formulário para coletar outro item composto.</b><br>" +
            "Use após registrar todos os componentes deste patrimônio.<br>" +
            "⚠️ Componentes pendentes ficarão sem registro!</html>");
        btnCancelar = createButton("❌ Fechar", new Color(192, 57, 43));
        
        btnRegistrarComponente.setEnabled(false);
        btnMarcarTodosEncontrados.setEnabled(false);
        btnFinalizarColeta.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Painel superior - Info do inventário
        JPanel panelInventario = createPanelInventario();
        
        // TabbedPane com duas abas
        tabbedPane = new JTabbedPane();
        // Usar Segoe UI Emoji para suportar emojis nas abas
        tabbedPane.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        
        // Aba 1: Busca por Número
        JPanel panelBuscaNumero = createPanelBuscaPorNumero();
        tabbedPane.addTab("🔍 Buscar por Número", panelBuscaNumero);
        
        // Aba 2: Listar Todos
        JPanel panelListagem = createPanelListagem();
        tabbedPane.addTab("📋 Listar Itens Compostos", panelListagem);
        
        // Aba 3: Histórico de Coletas
        JPanel panelHistorico = createPanelHistorico();
        tabbedPane.addTab("📊 Histórico de Coletas", panelHistorico);
        
        // Painel inferior - Botões
        JPanel panelAcoes = createPanelAcoes();
        
        mainPanel.add(panelInventario, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(panelAcoes, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Carregar lista de itens compostos ao iniciar
        carregarListaItensCompostos();
    }
    
    private JPanel createPanelBuscaPorNumero() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com busca e localização
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 5));
        panelSuperior.setOpaque(false);
        
        // Painel de busca
        JPanel panelBusca = createPanelBusca();
        
        // Painel de localização atual
        JPanel panelLocalizacao = createPanelLocalizacaoAtual();
        
        panelSuperior.add(panelBusca, BorderLayout.NORTH);
        panelSuperior.add(panelLocalizacao, BorderLayout.SOUTH);
        
        // Painel central - Dados e componentes com JSplitPane para melhor distribuição
        JPanel panelDados = createPanelDados();
        JPanel panelComponentes = createPanelComponentes();
        
        // Painel central com GridBagLayout para melhor controle de espaço
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setOpaque(false);
        
        GridBagConstraints gbcCentral = new GridBagConstraints();
        gbcCentral.fill = GridBagConstraints.BOTH;
        gbcCentral.weightx = 1.0;
        gbcCentral.gridx = 0;
        gbcCentral.insets = new Insets(5, 0, 5, 0);
        
        // Painel de dados - altura fixa (não expande)
        gbcCentral.gridy = 0;
        gbcCentral.weighty = 0.0; // Não expande verticalmente
        panelDados.setPreferredSize(new Dimension(800, 110));
        panelDados.setMinimumSize(new Dimension(400, 100));
        panelCentral.add(panelDados, gbcCentral);
        
        // Painel de componentes - expande para ocupar todo espaço restante
        gbcCentral.gridy = 1;
        gbcCentral.weighty = 1.0; // Expande verticalmente
        panelComponentes.setMinimumSize(new Dimension(400, 200));
        panelCentral.add(panelComponentes, gbcCentral);
        
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(panelCentral, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel para selecionar a localização atual de coleta
     */
    private JPanel createPanelLocalizacaoAtual() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(new Color(155, 89, 182));
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel lblTitulo = new JLabel("📍 Local Atual de Coleta:");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblOu = new JLabel("ou");
        lblOu.setFont(new Font("Arial", Font.PLAIN, 12));
        lblOu.setForeground(Color.WHITE);
        
        JLabel lblDica = new JLabel("💡 Componentes registrados usarão este local automaticamente");
        lblDica.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        lblDica.setForeground(new Color(230, 230, 250));
        
        panel.add(lblTitulo);
        panel.add(cmbLocalizacaoAtual);
        panel.add(lblOu);
        panel.add(txtLocalizacaoManual);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(lblDica);
        
        return panel;
    }
    
    /**
     * Carrega as salas no ComboBox de localização
     */
    private void carregarSalasComboBox() {
        cmbLocalizacaoAtual.removeAllItems();
        cmbLocalizacaoAtual.addItem("-- Selecione uma sala --");
        
        try {
            com.inventario.sihcp.dao.SalaDAO salaDAO = new com.inventario.sihcp.dao.SalaDAO();
            List<com.inventario.sihcp.model.Sala> salas = salaDAO.listarSalas();
            
            for (com.inventario.sihcp.model.Sala sala : salas) {
                String item = sala.getDescricao();
                if (sala.getNumero() != null && !sala.getNumero().isEmpty()) {
                    item = sala.getNumero() + " - " + sala.getDescricao();
                }
                cmbLocalizacaoAtual.addItem(item);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Erro ao carregar salas: " + e.getMessage());
        }
    }
    
    /**
     * Obtém a localização atual selecionada
     */
    private String getLocalizacaoAtual() {
        // Prioridade: texto manual > combobox
        String manual = txtLocalizacaoManual.getText().trim();
        if (!manual.isEmpty()) {
            return manual;
        }
        
        int idx = cmbLocalizacaoAtual.getSelectedIndex();
        if (idx > 0) {
            return (String) cmbLocalizacaoAtual.getSelectedItem();
        }
        
        return "";
    }
    
    private JPanel createPanelListagem() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel de filtro
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltro.setBackground(Color.WHITE);
        panelFiltro.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblFiltro = new JLabel("🔍 Filtrar por descrição:");
        lblFiltro.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        
        panelFiltro.add(lblFiltro);
        panelFiltro.add(txtBuscaDescricao);
        panelFiltro.add(btnBuscarDescricao);
        
        JButton btnRecarregar = createButton("🔄 Recarregar", new Color(52, 152, 219));
        btnRecarregar.addActionListener(e -> carregarListaItensCompostos());
        panelFiltro.add(btnRecarregar);
        
        // Tabela de itens compostos
        JPanel panelTabela = new JPanel(new BorderLayout(10, 10));
        panelTabela.setBackground(Color.WHITE);
        panelTabela.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
            "Itens Compostos Cadastrados (794 patrimônios)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(155, 89, 182)
        ));
        
        JScrollPane scrollItens = new JScrollPane(tblItensCompostos);
        scrollItens.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        
        JPanel panelBotoesTabela = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotoesTabela.setBackground(Color.WHITE);
        panelBotoesTabela.add(btnCarregarSelecionado);
        
        JLabel lblDica = new JLabel("💡 Dica: Dê duplo clique em um item para carregar");
        lblDica.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        lblDica.setForeground(new Color(127, 140, 141));
        panelBotoesTabela.add(Box.createHorizontalStrut(20));
        panelBotoesTabela.add(lblDica);
        
        panelTabela.add(scrollItens, BorderLayout.CENTER);
        panelTabela.add(panelBotoesTabela, BorderLayout.SOUTH);
        
        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelTabela, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelInventario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(new Color(52, 152, 219));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("📋 Inventário: " + 
            (inventarioAtivo != null ? inventarioAtivo.getNome() : "Nenhum ativo"));
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblColetor = new JLabel("👤 Coletor: " + usuarioLogado.getNomeCompleto());
        lblColetor.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lblColetor.setForeground(Color.WHITE);
        
        panel.add(lblTitulo);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(lblColetor);
        
        return panel;
    }
    
    private JPanel createPanelBusca() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblTitulo = new JLabel("🔍 Buscar Item Composto:");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        
        panel.add(lblTitulo);
        panel.add(txtNumeroPatrimonio);
        panel.add(btnBuscar);
        panel.add(btnLimpar);
        
        return panel;
    }
    
    private JPanel createPanelDados() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Dados do Item Composto",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(52, 152, 219)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8); // Insets menores
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Linha 1: Número do Patrimônio (destaque), ID e Status
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblNumeroTitulo = createLabel("📦 Patrimônio:");
        lblNumeroTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        panel.add(lblNumeroTitulo, gbc);
        gbc.gridx = 1; gbc.weightx = 0.15;
        panel.add(lblNumeroPatrimonio, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(createLabel("ID:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.05;
        panel.add(lblPatrimonioId, gbc);
        
        gbc.gridx = 4; gbc.weightx = 0;
        panel.add(createLabel("Status:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.2;
        panel.add(lblStatusGeral, gbc);
        
        // Linha 2: Descrição
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(createLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 5; gbc.weightx = 1.0;
        panel.add(lblDescricao, gbc);
        gbc.gridwidth = 1;
        
        // Linha 3: Sala e Responsável
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(createLabel("Sala:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.4;
        panel.add(lblSala, gbc);
        gbc.gridwidth = 1;
        
        gbc.gridx = 3; gbc.weightx = 0;
        panel.add(createLabel("Responsável:"), gbc);
        gbc.gridx = 4; gbc.gridwidth = 2; gbc.weightx = 0.4;
        panel.add(lblResponsavel, gbc);
        
        return panel;
    }
    
    private JPanel createPanelComponentes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            "Componentes - Registro de Coleta",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(46, 204, 113)
        ));
        
        // ScrollPane para a tabela - ocupa todo espaço disponível
        JScrollPane scrollPane = new JScrollPane(tblComponentes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Configurar tabela para preencher viewport e mostrar linhas
        tblComponentes.setFillsViewportHeight(true);
        tblComponentes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBotoes.setBackground(Color.WHITE);
        panelBotoes.add(btnRegistrarComponente);
        panelBotoes.add(btnMarcarTodosEncontrados);
        
        // Dica sobre duplo clique
        JLabel lblDica = new JLabel("💡 Dica: Dê duplo clique em um componente para registrar");
        lblDica.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        lblDica.setForeground(new Color(127, 140, 141));
        panelBotoes.add(Box.createHorizontalStrut(20));
        panelBotoes.add(lblDica);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Cria o painel de histórico de coletas de itens compostos
     */
    private JPanel createPanelHistorico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com botão de atualizar e total
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        btnAtualizarHistorico = createButton("🔄 Atualizar", new Color(52, 152, 219));
        btnAtualizarHistorico.setPreferredSize(new Dimension(130, 35));
        btnAtualizarHistorico.addActionListener(e -> carregarHistoricoColetas());
        
        lblTotalHistorico = new JLabel("Total: 0 coletas");
        lblTotalHistorico.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        lblTotalHistorico.setForeground(new Color(52, 73, 94));
        
        panelSuperior.add(btnAtualizarHistorico);
        panelSuperior.add(Box.createHorizontalStrut(20));
        panelSuperior.add(lblTotalHistorico);
        
        // Tabela de histórico
        String[] colunas = {"Data/Hora", "Patrimônio", "Descrição", "Componente", "Qtd", "Status", "Coletor"};
        modelHistorico = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblHistorico = new JTable(modelHistorico);
        tblHistorico.setFont(new Font("Arial", Font.PLAIN, 12));
        tblHistorico.setRowHeight(25);
        tblHistorico.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistorico.setAutoCreateRowSorter(true);
        
        // Ajustar larguras das colunas
        tblHistorico.getColumnModel().getColumn(0).setPreferredWidth(130);  // Data/Hora
        tblHistorico.getColumnModel().getColumn(1).setPreferredWidth(80);   // Patrimônio
        tblHistorico.getColumnModel().getColumn(2).setPreferredWidth(200);  // Descrição
        tblHistorico.getColumnModel().getColumn(3).setPreferredWidth(150);  // Componente
        tblHistorico.getColumnModel().getColumn(4).setPreferredWidth(50);   // Qtd
        tblHistorico.getColumnModel().getColumn(5).setPreferredWidth(80);   // Status
        tblHistorico.getColumnModel().getColumn(6).setPreferredWidth(100);  // Coletor
        
        JScrollPane scrollPane = new JScrollPane(tblHistorico);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
            "Histórico de Coletas de Componentes",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(155, 89, 182)
        ));
        
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Carrega o histórico de coletas de componentes
     */
    private void carregarHistoricoColetas() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            modelHistorico.setRowCount(0);
            
            if (inventarioAtivo == null) {
                lblTotalHistorico.setText("Total: 0 coletas (sem inventário ativo)");
                return;
            }
            
            List<Map<String, Object>> historico = itemCompostoDAO.buscarHistoricoColetas(inventarioAtivo.getId());
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (Map<String, Object> item : historico) {
                java.sql.Timestamp dataColeta = (java.sql.Timestamp) item.get("dataColeta");
                String dataFormatada = dataColeta != null ? sdf.format(dataColeta) : "-";
                
                modelHistorico.addRow(new Object[]{
                    dataFormatada,
                    item.get("numeroPatrimonio"),
                    truncarTexto((String) item.get("descricaoPatrimonio"), 40),
                    item.get("tipoComponente"),
                    item.get("quantidadeEncontrada"),
                    item.get("statusComponente"),
                    item.get("nomeColetor")
                });
            }
            
            lblTotalHistorico.setText("📊 Total: " + historico.size() + " coletas");
            
        } catch (java.sql.SQLException | RuntimeException e) {
            LOG.error("Erro ao carregar histórico: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar histórico: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private JPanel createPanelAcoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        
        panel.add(btnFinalizarColeta);
        panel.add(btnCancelar);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Busca por número
        btnBuscar.addActionListener(e -> buscarItemComposto());
        txtNumeroPatrimonio.addActionListener(e -> buscarItemComposto());
        btnLimpar.addActionListener(e -> limparFormulario());
        
        // Busca por descrição
        btnBuscarDescricao.addActionListener(e -> filtrarPorDescricao());
        txtBuscaDescricao.addActionListener(e -> filtrarPorDescricao());
        
        // Tabela de itens compostos - duplo clique
        tblItensCompostos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    carregarItemSelecionado();
                }
            }
        });
        
        // Botão carregar selecionado
        btnCarregarSelecionado.addActionListener(e -> carregarItemSelecionado());
        
        // Componentes
        btnRegistrarComponente.addActionListener(e -> registrarComponente());
        btnMarcarTodosEncontrados.addActionListener(e -> marcarTodosEncontrados());
        btnFinalizarColeta.addActionListener(e -> finalizarColeta());
        btnCancelar.addActionListener(e -> dispose());
        
        tblComponentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnRegistrarComponente.setEnabled(tblComponentes.getSelectedRow() != -1);
            }
        });
        
        // Duplo clique na tabela de componentes para registrar
        tblComponentes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblComponentes.getSelectedRow() != -1) {
                    registrarComponente();
                }
            }
        });
    }
    
    /**
     * Carrega a lista de todos os itens compostos cadastrados
     */
    private void carregarListaItensCompostos() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            modelItensCompostos.setRowCount(0);
            
            List<Map<String, Object>> itens = itemCompostoDAO.listarItensCompostosComDetalhes();
            
            for (Map<String, Object> item : itens) {
                modelItensCompostos.addRow(new Object[]{
                    item.get("numero"),
                    truncarTexto((String) item.get("descricao"), 80),
                    item.get("totalComponentes"),
                    item.get("sala") != null ? item.get("sala") : "Sem sala"
                });
            }
            
            // Atualizar título com contagem
            if (tabbedPane.getTabCount() > 1) {
                tabbedPane.setTitleAt(1, "📋 Listar Itens Compostos (" + itens.size() + ")");
            }
            
        } catch (java.sql.SQLException | RuntimeException e) {
            LOG.error("Erro ao carregar lista de itens compostos: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar lista de itens compostos: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Filtra a lista de itens compostos por descrição
     */
    private void filtrarPorDescricao() {
        String filtro = txtBuscaDescricao.getText().trim();
        
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            modelItensCompostos.setRowCount(0);
            
            List<Map<String, Object>> itens;
            if (filtro.isEmpty()) {
                itens = itemCompostoDAO.listarItensCompostosComDetalhes();
            } else {
                itens = itemCompostoDAO.buscarItensCompostosPorDescricao(filtro);
            }
            
            for (Map<String, Object> item : itens) {
                modelItensCompostos.addRow(new Object[]{
                    item.get("numero"),
                    truncarTexto((String) item.get("descricao"), 80),
                    item.get("totalComponentes"),
                    item.get("sala") != null ? item.get("sala") : "Sem sala"
                });
            }
            
            // Atualizar título com contagem
            if (tabbedPane.getTabCount() > 1) {
                String titulo = filtro.isEmpty() 
                    ? "📋 Listar Itens Compostos (" + itens.size() + ")"
                    : "📋 Filtrado: " + itens.size() + " itens";
                tabbedPane.setTitleAt(1, titulo);
            }
            
        } catch (java.sql.SQLException | RuntimeException e) {
            LOG.error("Erro ao filtrar itens compostos: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Erro ao filtrar itens compostos: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Carrega o item composto selecionado na tabela
     */
    private void carregarItemSelecionado() {
        int selectedRow = tblItensCompostos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um item composto na lista.",
                "Seleção Necessária",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String numero = (String) modelItensCompostos.getValueAt(selectedRow, 0);
        
        // Mudar para aba de busca e carregar
        tabbedPane.setSelectedIndex(0);
        txtNumeroPatrimonio.setText(numero);
        buscarItemComposto();
    }
    
    /**
     * Trunca texto longo para exibição na tabela
     */
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        if (texto.length() <= maxLength) return texto;
        return texto.substring(0, maxLength - 3) + "...";
    }
    
    private void buscarItemComposto() {
        String numero = txtNumeroPatrimonio.getText().trim();
        
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite o número do patrimônio.",
                "Campo Obrigatório",
                JOptionPane.WARNING_MESSAGE);
            txtNumeroPatrimonio.requestFocus();
            return;
        }
        
        if (inventarioAtivo == null) {
            JOptionPane.showMessageDialog(this,
                "Nenhum inventário ativo. Não é possível realizar coleta.",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
            
            if (patrimonio == null) {
                JOptionPane.showMessageDialog(this,
                    "Patrimônio não encontrado: " + numero,
                    "Não Encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // ✅ Verificar se é item composto no banco
            if (!itemCompostoDAO.isItemComposto(patrimonio.getId())) {
                JOptionPane.showMessageDialog(this,
                    "O patrimônio " + numero + " não é um item composto.\n" +
                    "Esta tela é apenas para coleta de itens compostos.",
                    "Não é Item Composto",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            carregarItemComposto(patrimonio);
            
        } catch (java.sql.SQLException | RuntimeException e) {
            LOG.error("Erro ao buscar patrimônio: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Erro ao buscar patrimônio: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            // Manter foco no campo de busca em caso de erro
            txtNumeroPatrimonio.requestFocusInWindow();
        }
    }
    
    private void carregarItemComposto(Patrimonio patrimonio) {
        this.patrimonioAtual = patrimonio;
        
        // ✅ Exibir número do patrimônio em destaque
        lblNumeroPatrimonio.setText(patrimonio.getNumero());
        lblPatrimonioId.setText(String.valueOf(patrimonio.getId()));
        lblDescricao.setText(patrimonio.getDescricao());
        lblSala.setText(patrimonio.getNomeSala() != null ? patrimonio.getNomeSala() : "Sem sala");
        lblResponsavel.setText(patrimonio.getNomeResponsavel() != null ? 
            patrimonio.getNomeResponsavel() : "Sem responsável");
        
        // ✅ Limpar campo de busca e manter foco para próxima leitura
        txtNumeroPatrimonio.setText("");
        txtNumeroPatrimonio.requestFocusInWindow();
        
        // ✅ Carregar componentes do banco de dados
        componentesColeta.clear();
        modelComponentes.setRowCount(0);
        
        try {
            List<Map<String, Object>> componentes = itemCompostoDAO.buscarStatusColeta(
                patrimonio.getId(), 
                inventarioAtivo.getId()
            );
            
            if (componentes.isEmpty()) {
                JOptionPane.showMessageDialog(this, """
                                                    Este patrim\u00f4nio n\u00e3o possui componentes cadastrados.
                                                    Configure os componentes antes de realizar a coleta.""",
                    "Sem Componentes",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            for (Map<String, Object> comp : componentes) {
                ComponenteColeta componente = new ComponenteColeta(
                    (Integer) comp.get("id"),
                    (String) comp.get("tipo"),
                    (String) comp.get("descricao"),
                    (Integer) comp.get("quantidadeEsperada"),
                    (Integer) comp.get("quantidadeEncontrada"),
                    (String) comp.get("localizacaoEncontrada"),
                    (String) comp.get("status"),
                    (String) comp.get("observacao")
                );
                componentesColeta.add(componente);
            }
            
            atualizarTabelaComponentes();
            atualizarStatusGeral();
            
            btnRegistrarComponente.setEnabled(false);
            btnMarcarTodosEncontrados.setEnabled(true);
            btnFinalizarColeta.setEnabled(true);
            
        } catch (java.sql.SQLException | RuntimeException e) {
            LOG.error("Erro ao carregar componentes: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar componentes: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarTabelaComponentes() {
        modelComponentes.setRowCount(0);
        
        for (ComponenteColeta comp : componentesColeta) {
            String status = comp.getStatus();
            String localizacao = comp.getLocalizacaoEncontrada();
            if (localizacao == null || localizacao.isEmpty()) {
                localizacao = "-";
            }
            
            modelComponentes.addRow(new Object[]{
                comp.getTipo(),
                comp.getDescricao(),
                comp.getQuantidadeEsperada(),
                comp.getQuantidadeEncontrada(),
                localizacao,
                status,
                comp.getObservacao()
            });
        }
        
        // Ajustar larguras das colunas
        tblComponentes.getColumnModel().getColumn(0).setPreferredWidth(80);   // Tipo
        tblComponentes.getColumnModel().getColumn(1).setPreferredWidth(200);  // Descrição
        tblComponentes.getColumnModel().getColumn(2).setPreferredWidth(80);   // Qtd Esperada
        tblComponentes.getColumnModel().getColumn(3).setPreferredWidth(80);   // Qtd Encontrada
        tblComponentes.getColumnModel().getColumn(4).setPreferredWidth(150);  // Local
        tblComponentes.getColumnModel().getColumn(5).setPreferredWidth(80);   // Status
        tblComponentes.getColumnModel().getColumn(6).setPreferredWidth(150);  // Observação
    }
    
    private void atualizarStatusGeral() {
        int total = componentesColeta.size();
        int encontrados = 0;
        int parcial = 0;
        
        for (ComponenteColeta comp : componentesColeta) {
            if ("COMPLETO".equals(comp.getStatus())) {
                encontrados++;
            } else if ("PARCIAL".equals(comp.getStatus())) {
                parcial++;
            }
        }
        
        String status;
        Color cor;
        
        if (encontrados == total) {
            status = "✓ COMPLETO (" + encontrados + "/" + total + ")";
            cor = new Color(46, 204, 113);
        } else if (encontrados + parcial > 0) {
            status = "⚠ PARCIAL (" + (encontrados + parcial) + "/" + total + ")";
            cor = new Color(241, 196, 15);
        } else {
            status = "✗ PENDENTE (0/" + total + ")";
            cor = new Color(231, 76, 60);
        }
        
        lblStatusGeral.setText(status);
        lblStatusGeral.setForeground(cor);
    }
    
    private void registrarComponente() {
        int selectedRow = tblComponentes.getSelectedRow();
        if (selectedRow == -1) return;
        
        ComponenteColeta componente = componentesColeta.get(selectedRow);
        
        // Passar a localização atual selecionada para o dialog
        String localizacaoAtual = getLocalizacaoAtual();
        RegistroComponenteDialog dialog = new RegistroComponenteDialog(this, componente, localizacaoAtual);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            try {
                int quantidadeEncontrada = dialog.getQuantidadeEncontrada();
                String localizacaoEncontrada = dialog.getLocalizacaoEncontrada();
                String observacao = dialog.getObservacao();
                
                // ✅ Salvar no banco de dados
                itemCompostoDAO.registrarColetaComponente(
                    componente.getId(),
                    inventarioAtivo.getId(),
                    usuarioLogado.getId(),
                    quantidadeEncontrada,
                    observacao,
                    localizacaoEncontrada  // Novo parâmetro
                );
                
                // Atualizar objeto local
                componente.setQuantidadeEncontrada(quantidadeEncontrada);
                componente.setLocalizacaoEncontrada(localizacaoEncontrada);
                componente.setObservacao(observacao);
                
                // Atualizar status
                if (quantidadeEncontrada >= componente.getQuantidadeEsperada()) {
                    componente.setStatus("COMPLETO");
                } else if (quantidadeEncontrada > 0) {
                    componente.setStatus("PARCIAL");
                } else {
                    componente.setStatus("FALTANTE");
                }
                
                atualizarTabelaComponentes();
                atualizarStatusGeral();
                
                // ✅ Manter foco no campo de busca para próxima leitura
                txtNumeroPatrimonio.requestFocusInWindow();
                
            } catch (java.sql.SQLException | RuntimeException e) {
                LOG.error("Erro ao registrar componente: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(this,
                    "Erro ao registrar componente: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        // ✅ Manter foco no campo de busca mesmo se cancelar
        txtNumeroPatrimonio.requestFocusInWindow();
    }
    
    private void marcarTodosEncontrados() {
        String localizacaoAtual = getLocalizacaoAtual();
        
        String mensagem = """
                          Marcar todos os componentes como encontrados?
                          Isso registrar\u00e1 a quantidade esperada para cada componente.""";
        
        if (!localizacaoAtual.isEmpty()) {
            mensagem += "\n\n📍 Local: " + localizacaoAtual;
        } else {
            mensagem += "\n\n⚠️ Nenhum local selecionado. Deseja continuar?";
        }
        
        int opcao = JOptionPane.showConfirmDialog(this,
            mensagem,
            "Confirmar - Marcar Todos no Local Atual",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                for (ComponenteColeta comp : componentesColeta) {
                    // ✅ Salvar no banco de dados com localização
                    itemCompostoDAO.registrarColetaComponente(
                        comp.getId(),
                        inventarioAtivo.getId(),
                        usuarioLogado.getId(),
                        comp.getQuantidadeEsperada(),
                        "Marcado automaticamente como completo",
                        localizacaoAtual  // Usar localização atual
                    );
                    
                    // Atualizar objeto local
                    comp.setQuantidadeEncontrada(comp.getQuantidadeEsperada());
                    comp.setLocalizacaoEncontrada(localizacaoAtual);
                    comp.setStatus("COMPLETO");
                    comp.setObservacao("Marcado automaticamente como completo");
                }
                
                atualizarTabelaComponentes();
                atualizarStatusGeral();
                
                JOptionPane.showMessageDialog(this,
                    "Todos os componentes foram marcados como encontrados!\n" +
                    (localizacaoAtual.isEmpty() ? "" : "📍 Local: " + localizacaoAtual),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // ✅ Manter foco no campo de busca para próxima leitura
                txtNumeroPatrimonio.requestFocusInWindow();
                
            } catch (java.sql.SQLException | RuntimeException e) {
                LOG.error("Erro ao marcar componentes: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(this,
                    "Erro ao marcar componentes: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        // ✅ Manter foco no campo de busca
        txtNumeroPatrimonio.requestFocusInWindow();
    }
    
    private void finalizarColeta() {
        if (patrimonioAtual == null) {
            JOptionPane.showMessageDialog(this,
                "Nenhum item composto carregado.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Contar status dos componentes
        int total = componentesColeta.size();
        int completos = 0;
        int parciais = 0;
        int pendentes = 0;
        
        for (ComponenteColeta comp : componentesColeta) {
            switch (comp.getStatus()) {
                case "COMPLETO" -> completos++;
                case "PARCIAL" -> parciais++;
                default -> pendentes++;
            }
        }
        
        // Montar mensagem de resumo (sem descrição)
        StringBuilder resumo = new StringBuilder();
        resumo.append("📋 RESUMO DA COLETA\n");
        resumo.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        resumo.append("Patrimônio: ").append(patrimonioAtual.getNumero()).append("\n\n");
        resumo.append("Componentes:\n");
        resumo.append("  ✓ Completos: ").append(completos).append("/").append(total).append("\n");
        if (parciais > 0) {
            resumo.append("  ⚠ Parciais: ").append(parciais).append("\n");
        }
        if (pendentes > 0) {
            resumo.append("  ✗ Pendentes: ").append(pendentes).append("\n");
        }
        resumo.append("\n");
        
        if (pendentes > 0) {
            resumo.append("⚠️ Componentes pendentes não serão registrados.\n\n");
        }
        
        resumo.append("Deseja coletar outro item?");
        
        int opcao = JOptionPane.showConfirmDialog(this,
            resumo.toString(),
            "Próximo Item - Confirmar",
            JOptionPane.YES_NO_OPTION,
            pendentes > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            // Limpar formulário para próxima coleta
            limparFormulario();
            txtNumeroPatrimonio.requestFocus();
        }
    }
    
    private void limparFormulario() {
        patrimonioAtual = null;
        txtNumeroPatrimonio.setText("");
        lblNumeroPatrimonio.setText("");  // Limpar número do patrimônio em destaque
        lblPatrimonioId.setText("");
        lblDescricao.setText("");
        lblSala.setText("");
        lblResponsavel.setText("");
        lblStatusGeral.setText("");
        componentesColeta.clear();
        modelComponentes.setRowCount(0);
        btnRegistrarComponente.setEnabled(false);
        btnMarcarTodosEncontrados.setEnabled(false);
        btnFinalizarColeta.setEnabled(false);
        // Manter foco no campo de busca para próxima leitura
        txtNumeroPatrimonio.requestFocusInWindow();
    }
    
    // Métodos auxiliares
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }
    
    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        // Usar Segoe UI Emoji para suportar emojis nos botões
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 35));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    // Classe interna para representar um componente na coleta
    public static class ComponenteColeta {
        private int id;
        private String tipo;
        private String descricao;
        private int quantidadeEsperada;
        private int quantidadeEncontrada;
        private String localizacaoEncontrada; // Local onde o componente foi encontrado
        private String status; // PENDENTE, COMPLETO, PARCIAL, FALTANTE
        private String observacao;
        
        public ComponenteColeta(int id, String tipo, String descricao, 
                               int quantidadeEsperada, int quantidadeEncontrada,
                               String status, String observacao) {
            this(id, tipo, descricao, quantidadeEsperada, quantidadeEncontrada, null, status, observacao);
        }
        
        public ComponenteColeta(int id, String tipo, String descricao, 
                               int quantidadeEsperada, int quantidadeEncontrada,
                               String localizacaoEncontrada, String status, String observacao) {
            this.id = id;
            this.tipo = tipo;
            this.descricao = descricao;
            this.quantidadeEsperada = quantidadeEsperada;
            this.quantidadeEncontrada = quantidadeEncontrada;
            this.localizacaoEncontrada = localizacaoEncontrada;
            this.status = status;
            this.observacao = observacao;
        }
        
        // Getters e Setters
        public int getId() { return id; }
        public String getTipo() { return tipo; }
        public String getDescricao() { return descricao; }
        public int getQuantidadeEsperada() { return quantidadeEsperada; }
        public int getQuantidadeEncontrada() { return quantidadeEncontrada; }
        public void setQuantidadeEncontrada(int qtd) { this.quantidadeEncontrada = qtd; }
        public String getLocalizacaoEncontrada() { return localizacaoEncontrada; }
        public void setLocalizacaoEncontrada(String loc) { this.localizacaoEncontrada = loc; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getObservacao() { return observacao; }
        public void setObservacao(String obs) { this.observacao = obs; }
    }
}
