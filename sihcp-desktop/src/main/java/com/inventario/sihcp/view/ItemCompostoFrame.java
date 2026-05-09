package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.inventario.sihcp.dao.ItemCompostoDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Usuario;

/**
 * Frame para gestão de itens compostos
 * Permite marcar patrimônios como compostos e gerenciar seus componentes
 * 
 * @author Sistema de Inventário
 * @version 1.1.0
 */
public class ItemCompostoFrame extends JFrame {
    
    private final Usuario usuarioLogado;
    private final PatrimonioDAO patrimonioDAO;
    private final ItemCompostoDAO itemCompostoDAO;
    
    // Componentes de busca
    private JTextField txtNumeroPatrimonio;
    private JButton btnBuscar;
    private JButton btnLimpar;
    
    // Dados do patrimônio
    private JLabel lblPatrimonioId;
    private JLabel lblDescricao;
    private JLabel lblSala;
    private JLabel lblResponsavel;
    private JLabel lblValor;
    private JCheckBox chkItemComposto;
    private JCheckBox chkDeteccaoAutomatica;
    
    // Tabela de componentes
    private JTable tblComponentes;
    private DefaultTableModel modelComponentes;
    private JButton btnAdicionarComponente;
    private JButton btnEditarComponente;
    private JButton btnRemoverComponente;
    private JButton btnDetectarPadroes;
    
    // Botões de ação
    private JButton btnSalvar;
    private JButton btnRemoverItemComposto;
    private JButton btnCancelar;
    
    // Componentes da aba de pesquisa
    private JTabbedPane tabbedPane;
    private JTextField txtPesquisaNumero;
    private JTextField txtPesquisaDescricao;
    private JButton btnPesquisar;
    private JButton btnLimparPesquisa;
    private JTable tblResultados;
    private DefaultTableModel modelResultados;
    private JButton btnAbrirItem;
    private JButton btnEditarItem;
    private JButton btnExcluirItem;
    private JButton btnExcluirSelecionados; // Novo botão para exclusão em lote (admin)
    private JLabel lblSelecaoInfo; // Label para mostrar quantidade selecionada
    
    // Dados
    private Patrimonio patrimonioAtual;
    private final List<ComponenteItem> componentes;
    
    public ItemCompostoFrame(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.patrimonioDAO = new PatrimonioDAO();
        this.itemCompostoDAO = new ItemCompostoDAO();
        this.componentes = new ArrayList<>();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("Gestão de Itens Compostos");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Definir ícone
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
    }
    
    private void initializeComponents() {
        // Painel de busca
        txtNumeroPatrimonio = new JTextField(20);
        txtNumeroPatrimonio.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnBuscar = createModernButton("Buscar", new Color(52, 152, 219));
        btnLimpar = createModernButton("Limpar", new Color(149, 165, 166));
        
        // Labels de dados do patrimônio
        lblPatrimonioId = createDataLabel("");
        lblDescricao = createDataLabel("");
        lblSala = createDataLabel("");
        lblResponsavel = createDataLabel("");
        lblValor = createDataLabel("");
        
        // Checkboxes
        chkItemComposto = new JCheckBox("Marcar como Item Composto");
        chkItemComposto.setFont(new Font("Arial", Font.BOLD, 13));
        chkItemComposto.setEnabled(false);
        
        chkDeteccaoAutomatica = new JCheckBox("Detecção Automática Ativada");
        chkDeteccaoAutomatica.setFont(new Font("Arial", Font.PLAIN, 12));
        chkDeteccaoAutomatica.setEnabled(false);
        
        // Tabela de componentes
        String[] colunas = {"Tipo", "Descrição", "Qtd. Esperada", "Ordem"};
        modelComponentes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblComponentes = new JTable(modelComponentes);
        tblComponentes.setFont(new Font("Arial", Font.PLAIN, 12));
        tblComponentes.setRowHeight(25);
        tblComponentes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblComponentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Botões de componentes
        btnAdicionarComponente = createModernButton("➕ Adicionar", new Color(46, 204, 113));
        btnEditarComponente = createModernButton("✏️ Editar", new Color(241, 196, 15));
        btnRemoverComponente = createModernButton("🗑️ Remover", new Color(231, 76, 60));
        btnDetectarPadroes = createModernButton("🔍 Detectar Padrões", new Color(155, 89, 182));
        
        btnAdicionarComponente.setEnabled(false);
        btnEditarComponente.setEnabled(false);
        btnRemoverComponente.setEnabled(false);
        btnDetectarPadroes.setEnabled(false);
        
        // Botões de ação
        btnSalvar = createModernButton("💾 Salvar", new Color(39, 174, 96));
        btnRemoverItemComposto = createModernButton("🗑️ Remover Item Composto", new Color(192, 57, 43));
        btnCancelar = createModernButton("❌ Cancelar", new Color(149, 165, 166));
        
        btnSalvar.setEnabled(false);
        btnRemoverItemComposto.setEnabled(false);
        btnRemoverItemComposto.setPreferredSize(new Dimension(180, 35));
        
        // Componentes da aba de pesquisa
        txtPesquisaNumero = new JTextField(15);
        txtPesquisaNumero.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtPesquisaDescricao = new JTextField(25);
        txtPesquisaDescricao.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnPesquisar = createModernButton("🔍 Pesquisar", new Color(52, 152, 219));
        btnLimparPesquisa = createModernButton("🧹 Limpar", new Color(149, 165, 166));
        
        // Tabela de resultados - Permitir múltipla seleção para admins
        String[] colunasResultados = {"ID", "Número", "Descrição", "Sala", "Componentes"};
        modelResultados = new DefaultTableModel(colunasResultados, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblResultados = new JTable(modelResultados);
        tblResultados.setFont(new Font("Arial", Font.PLAIN, 12));
        tblResultados.setRowHeight(25);
        tblResultados.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Permitir múltipla seleção apenas para administradores
        if (isUsuarioAdmin()) {
            tblResultados.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            tblResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        
        // Ajustar largura das colunas
        tblResultados.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tblResultados.getColumnModel().getColumn(1).setPreferredWidth(100); // Número
        tblResultados.getColumnModel().getColumn(2).setPreferredWidth(300); // Descrição
        tblResultados.getColumnModel().getColumn(3).setPreferredWidth(100); // Sala
        tblResultados.getColumnModel().getColumn(4).setPreferredWidth(80);  // Componentes
        
        // Botões de ação da pesquisa
        btnAbrirItem = createModernButton("📂 Abrir", new Color(52, 152, 219));
        btnEditarItem = createModernButton("✏️ Editar", new Color(241, 196, 15));
        btnExcluirItem = createModernButton("🗑️ Excluir", new Color(231, 76, 60));
        
        // Botão de exclusão em lote (apenas para administradores)
        btnExcluirSelecionados = createModernButton("🗑️ Excluir Selecionados", new Color(192, 57, 43));
        btnExcluirSelecionados.setPreferredSize(new Dimension(170, 35));
        btnExcluirSelecionados.setEnabled(false);
        btnExcluirSelecionados.setVisible(isUsuarioAdmin()); // Visível apenas para admin
        
        // Label de informação de seleção
        lblSelecaoInfo = new JLabel("");
        lblSelecaoInfo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblSelecaoInfo.setForeground(new Color(52, 152, 219));
        lblSelecaoInfo.setVisible(isUsuarioAdmin());
        
        btnAbrirItem.setEnabled(false);
        btnEditarItem.setEnabled(false);
        btnExcluirItem.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal com margem
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Criar TabbedPane
        tabbedPane = new JTabbedPane();
        // Usar Segoe UI Emoji para suportar emojis nas abas
        tabbedPane.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        
        // Aba 1 - Cadastro
        JPanel panelCadastro = createPanelCadastro();
        tabbedPane.addTab("📝 Cadastro", panelCadastro);
        
        // Aba 2 - Pesquisa
        JPanel panelPesquisa = createPanelPesquisa();
        tabbedPane.addTab("🔍 Pesquisa", panelPesquisa);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createPanelCadastro() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior - Busca
        JPanel panelBusca = createPanelBusca();
        
        // Painel central - Dados e Componentes
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setOpaque(false);
        
        JPanel panelDados = createPanelDados();
        JPanel panelComponentes = createPanelComponentes();
        
        panelCentral.add(panelDados, BorderLayout.NORTH);
        panelCentral.add(panelComponentes, BorderLayout.CENTER);
        
        // Painel inferior - Botões de ação
        JPanel panelAcoes = createPanelAcoes();
        
        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(panelCentral, BorderLayout.CENTER);
        panel.add(panelAcoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPanelPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblNumero = new JLabel("Número:");
        lblNumero.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setFont(new Font("Arial", Font.BOLD, 12));
        
        panelFiltros.add(lblNumero);
        panelFiltros.add(txtPesquisaNumero);
        panelFiltros.add(Box.createHorizontalStrut(20));
        panelFiltros.add(lblDescricao);
        panelFiltros.add(txtPesquisaDescricao);
        panelFiltros.add(Box.createHorizontalStrut(10));
        panelFiltros.add(btnPesquisar);
        panelFiltros.add(btnLimparPesquisa);
        
        // Painel da tabela
        JPanel panelTabela = new JPanel(new BorderLayout(10, 10));
        panelTabela.setBackground(Color.WHITE);
        panelTabela.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Itens Compostos Cadastrados",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(52, 152, 219)
        ));
        
        JScrollPane scrollPane = new JScrollPane(tblResultados);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        
        panelTabela.add(scrollPane, BorderLayout.CENTER);
        
        // Painel de botões de ação
        JPanel panelBotoesAcao = new JPanel(new BorderLayout());
        panelBotoesAcao.setBackground(Color.WHITE);
        
        // Painel esquerdo com botões individuais
        JPanel panelBotoesEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotoesEsquerda.setBackground(Color.WHITE);
        panelBotoesEsquerda.add(btnAbrirItem);
        panelBotoesEsquerda.add(btnEditarItem);
        panelBotoesEsquerda.add(btnExcluirItem);
        
        // Label de informação
        JLabel lblInfo = new JLabel("💡 Dica: Clique duas vezes em um item para abri-lo na aba de cadastro");
        lblInfo.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(127, 140, 141));
        panelBotoesEsquerda.add(Box.createHorizontalStrut(20));
        panelBotoesEsquerda.add(lblInfo);
        
        // Painel direito com botão de exclusão em lote (apenas admin)
        JPanel panelBotoesDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoesDireita.setBackground(Color.WHITE);
        panelBotoesDireita.add(lblSelecaoInfo);
        panelBotoesDireita.add(btnExcluirSelecionados);
        
        panelBotoesAcao.add(panelBotoesEsquerda, BorderLayout.WEST);
        panelBotoesAcao.add(panelBotoesDireita, BorderLayout.EAST);
        
        panelTabela.add(panelBotoesAcao, BorderLayout.SOUTH);
        
        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(panelTabela, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelBusca() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblTitulo = new JLabel("🔍 Buscar Patrimônio:");
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
            "Dados do Patrimônio",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(52, 152, 219)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Linha 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(createLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(lblPatrimonioId, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(createLabel("Valor:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        panel.add(lblValor, gbc);
        
        // Linha 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(createLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        panel.add(lblDescricao, gbc);
        gbc.gridwidth = 1;
        
        // Linha 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(createLabel("Sala:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(lblSala, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(createLabel("Responsável:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        panel.add(lblResponsavel, gbc);
        
        // Linha 4 - Checkboxes
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(chkItemComposto, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 2;
        panel.add(chkDeteccaoAutomatica, gbc);
        
        return panel;
    }
    
    private JPanel createPanelComponentes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            "Componentes do Item",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(46, 204, 113)
        ));
        
        // Tabela com scroll
        JScrollPane scrollPane = new JScrollPane(tblComponentes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        
        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotoes.setBackground(Color.WHITE);
        panelBotoes.add(btnAdicionarComponente);
        panelBotoes.add(btnEditarComponente);
        panelBotoes.add(btnRemoverComponente);
        panelBotoes.add(Box.createHorizontalStrut(20));
        panelBotoes.add(btnDetectarPadroes);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPanelAcoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        
        panel.add(btnRemoverItemComposto);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnSalvar);
        panel.add(btnCancelar);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Buscar patrimônio
        btnBuscar.addActionListener(e -> buscarPatrimonio());
        txtNumeroPatrimonio.addActionListener(e -> buscarPatrimonio());
        
        // Limpar formulário
        btnLimpar.addActionListener(e -> limparFormulario());
        
        // Checkbox item composto
        chkItemComposto.addActionListener(e -> {
            boolean isComposto = chkItemComposto.isSelected();
            habilitarComponentes(isComposto);
        });
        
        // Gerenciar componentes
        btnAdicionarComponente.addActionListener(e -> adicionarComponente());
        btnEditarComponente.addActionListener(e -> editarComponente());
        btnRemoverComponente.addActionListener(e -> removerComponente());
        btnDetectarPadroes.addActionListener(e -> detectarPadroes());
        
        // Seleção na tabela
        tblComponentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = tblComponentes.getSelectedRow() != -1;
                btnEditarComponente.setEnabled(hasSelection && chkItemComposto.isSelected());
                btnRemoverComponente.setEnabled(hasSelection && chkItemComposto.isSelected());
            }
        });
        
        // Salvar e cancelar
        btnSalvar.addActionListener(e -> salvarItemComposto());
        btnRemoverItemComposto.addActionListener(e -> removerItemComposto());
        btnCancelar.addActionListener(e -> dispose());
        
        // Fechar janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (formularioAlterado()) {
                    int opcao = JOptionPane.showConfirmDialog(
                        ItemCompostoFrame.this,
                        "Existem alterações não salvas. Deseja realmente sair?",
                        "Confirmar Saída",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                dispose();
            }
        });
        
        // Event listeners da aba de pesquisa
        btnPesquisar.addActionListener(e -> pesquisarItensCompostos());
        txtPesquisaNumero.addActionListener(e -> pesquisarItensCompostos());
        txtPesquisaDescricao.addActionListener(e -> pesquisarItensCompostos());
        btnLimparPesquisa.addActionListener(e -> limparPesquisa());
        
        // Seleção na tabela de resultados
        tblResultados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedRows = tblResultados.getSelectedRows();
                int selectionCount = selectedRows.length;
                boolean singleSelection = selectionCount == 1;
                
                // Botões de ação individual (apenas com seleção única)
                btnAbrirItem.setEnabled(singleSelection);
                btnEditarItem.setEnabled(singleSelection);
                btnExcluirItem.setEnabled(singleSelection);
                
                // Botão de exclusão em lote (apenas para admin com múltipla seleção)
                if (isUsuarioAdmin()) {
                    btnExcluirSelecionados.setEnabled(selectionCount > 1);
                    if (selectionCount > 1) {
                        lblSelecaoInfo.setText("📋 " + selectionCount + " itens selecionados");
                    } else {
                        lblSelecaoInfo.setText("");
                    }
                }
            }
        });
        
        // Duplo clique na tabela de resultados
        tblResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblResultados.getSelectedRow() != -1) {
                    abrirItemSelecionado();
                }
            }
        });
        
        // Botões de ação da pesquisa
        btnAbrirItem.addActionListener(e -> abrirItemSelecionado());
        btnEditarItem.addActionListener(e -> editarItemSelecionado());
        btnExcluirItem.addActionListener(e -> excluirItemSelecionado());
        btnExcluirSelecionados.addActionListener(e -> excluirItensSelecionados());
        
        // Carregar itens compostos ao abrir a aba de pesquisa
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) { // Aba de pesquisa
                carregarTodosItensCompostos();
            }
        });
    }
    
    private void buscarPatrimonio() {
        String numero = txtNumeroPatrimonio.getText().trim();
        
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite o número do patrimônio.",
                "Campo Obrigatório",
                JOptionPane.WARNING_MESSAGE);
            txtNumeroPatrimonio.requestFocus();
            return;
        }
        
        try {
            // Buscar patrimônio no banco
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
            
            if (patrimonio == null) {
                JOptionPane.showMessageDialog(this,
                    "Patrimônio não encontrado: " + numero,
                    "Não Encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Carregar dados do patrimônio (já verifica se é item composto e carrega componentes)
            carregarPatrimonio(patrimonio);
            
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao buscar patrimônio: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarPatrimonio(Patrimonio patrimonio) {
        this.patrimonioAtual = patrimonio;
        
        // Preencher labels
        lblPatrimonioId.setText(String.valueOf(patrimonio.getId()));
        lblDescricao.setText(patrimonio.getDescricao());
        lblSala.setText(patrimonio.getNomeSala() != null ? patrimonio.getNomeSala() : "Sem sala");
        lblResponsavel.setText(patrimonio.getNomeResponsavel() != null ? 
            patrimonio.getNomeResponsavel() : "Sem responsável");
        lblValor.setText(String.format("R$ %.2f", patrimonio.getValor() != null ? patrimonio.getValor() : BigDecimal.ZERO));
        
        // Habilitar controles
        chkItemComposto.setEnabled(true);
        chkDeteccaoAutomatica.setEnabled(true);
        btnSalvar.setEnabled(true);
        
        // Limpar componentes
        componentes.clear();
        modelComponentes.setRowCount(0);
        
        // Verificar se já é item composto e carregar componentes
        try {
            if (itemCompostoDAO.isItemComposto(patrimonio.getId())) {
                chkItemComposto.setSelected(true);
                habilitarComponentes(true);
                btnRemoverItemComposto.setEnabled(true); // Habilitar botão de remover
                
                // Carregar componentes existentes
                java.util.List<java.util.Map<String, Object>> comps = itemCompostoDAO.buscarComponentes(patrimonio.getId());
                for (java.util.Map<String, Object> comp : comps) {
                    ComponenteItem componente = new ComponenteItem(
                        (String) comp.get("tipo"),
                        (String) comp.get("descricao"),
                        (Integer) comp.get("quantidadeEsperada"),
                        componentes.size() + 1
                    );
                    componentes.add(componente);
                    
                    modelComponentes.addRow(new Object[]{
                        componente.getTipo(),
                        componente.getDescricao(),
                        componente.getQuantidadeEsperada(),
                        componente.getOrdem()
                    });
                }
                
                JOptionPane.showMessageDialog(this,
                    "Este patrimônio já é um item composto com " + comps.size() + " componente(s).",
                    "Item Composto Existente",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                btnRemoverItemComposto.setEnabled(false); // Desabilitar se não é item composto
            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("Erro ao verificar item composto: " + e.getMessage());
        }
    }
    
    private void habilitarComponentes(boolean habilitar) {
        btnAdicionarComponente.setEnabled(habilitar);
        btnDetectarPadroes.setEnabled(habilitar);
        
        if (!habilitar) {
            btnEditarComponente.setEnabled(false);
            btnRemoverComponente.setEnabled(false);
        }
    }
    
    private void adicionarComponente() {
        ComponenteDialog dialog = new ComponenteDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            ComponenteItem componente = dialog.getComponente();
            componentes.add(componente);
            
            // Adicionar na tabela
            modelComponentes.addRow(new Object[]{
                componente.getTipo(),
                componente.getDescricao(),
                componente.getQuantidadeEsperada(),
                componente.getOrdem()
            });
        }
    }
    
    private void editarComponente() {
        int selectedRow = tblComponentes.getSelectedRow();
        if (selectedRow == -1) return;
        
        ComponenteItem componente = componentes.get(selectedRow);
        ComponenteDialog dialog = new ComponenteDialog(this, componente);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            ComponenteItem componenteEditado = dialog.getComponente();
            componentes.set(selectedRow, componenteEditado);
            
            // Atualizar tabela
            modelComponentes.setValueAt(componenteEditado.getTipo(), selectedRow, 0);
            modelComponentes.setValueAt(componenteEditado.getDescricao(), selectedRow, 1);
            modelComponentes.setValueAt(componenteEditado.getQuantidadeEsperada(), selectedRow, 2);
            modelComponentes.setValueAt(componenteEditado.getOrdem(), selectedRow, 3);
        }
    }
    
    private void removerComponente() {
        int selectedRow = tblComponentes.getSelectedRow();
        if (selectedRow == -1) return;
        
        int opcao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente remover este componente?",
            "Confirmar Remoção",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            componentes.remove(selectedRow);
            modelComponentes.removeRow(selectedRow);
        }
    }
    
    private void detectarPadroes() {
        if (patrimonioAtual == null) return;
        
        String descricao = patrimonioAtual.getDescricao().toUpperCase();
        List<ComponenteItem> sugestoes = new ArrayList<>();
        
        // Detectar padrões baseados na descrição
        if (descricao.contains("COMPUTADOR") || descricao.contains("DESKTOP") || descricao.contains("PC")) {
            sugestoes.add(new ComponenteItem("GABINETE", "Gabinete/CPU", 1, 1));
            sugestoes.add(new ComponenteItem("MONITOR", "Monitor", 1, 2));
            sugestoes.add(new ComponenteItem("TECLADO", "Teclado", 1, 3));
            sugestoes.add(new ComponenteItem("MOUSE", "Mouse", 1, 4));
        } else if (descricao.contains("NOTEBOOK") || descricao.contains("LAPTOP")) {
            sugestoes.add(new ComponenteItem("NOTEBOOK", "Notebook", 1, 1));
            sugestoes.add(new ComponenteItem("CARREGADOR", "Carregador/Fonte", 1, 2));
            sugestoes.add(new ComponenteItem("MOUSE", "Mouse (opcional)", 1, 3));
        } else if (descricao.contains("IMPRESSORA") || descricao.contains("MULTIFUNCIONAL")) {
            sugestoes.add(new ComponenteItem("IMPRESSORA", "Impressora", 1, 1));
            sugestoes.add(new ComponenteItem("CABO_FORCA", "Cabo de Força", 1, 2));
            sugestoes.add(new ComponenteItem("CABO_USB", "Cabo USB", 1, 3));
        } else if (descricao.contains("PROJETOR") || descricao.contains("DATASHOW")) {
            sugestoes.add(new ComponenteItem("PROJETOR", "Projetor", 1, 1));
            sugestoes.add(new ComponenteItem("CONTROLE", "Controle Remoto", 1, 2));
            sugestoes.add(new ComponenteItem("CABO_FORCA", "Cabo de Força", 1, 3));
            sugestoes.add(new ComponenteItem("CABO_HDMI", "Cabo HDMI/VGA", 1, 4));
        } else if (descricao.contains("AR CONDICIONADO") || descricao.contains("SPLIT")) {
            sugestoes.add(new ComponenteItem("EVAPORADORA", "Unidade Evaporadora (interna)", 1, 1));
            sugestoes.add(new ComponenteItem("CONDENSADORA", "Unidade Condensadora (externa)", 1, 2));
            sugestoes.add(new ComponenteItem("CONTROLE", "Controle Remoto", 1, 3));
        } else if (descricao.contains("MESA") && descricao.contains("CADEIRA")) {
            sugestoes.add(new ComponenteItem("MESA", "Mesa", 1, 1));
            sugestoes.add(new ComponenteItem("CADEIRA", "Cadeira", 1, 2));
        } else if (descricao.contains("ESTAÇÃO DE TRABALHO")) {
            sugestoes.add(new ComponenteItem("MESA", "Mesa/Bancada", 1, 1));
            sugestoes.add(new ComponenteItem("GAVETEIRO", "Gaveteiro", 1, 2));
            sugestoes.add(new ComponenteItem("CADEIRA", "Cadeira", 1, 3));
        }
        
        if (sugestoes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Não foi possível detectar padrões automaticamente para:\n\"" + 
                patrimonioAtual.getDescricao() + "\"\n\n" +
                "Adicione os componentes manualmente.",
                "Nenhum Padrão Detectado",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Perguntar se deseja adicionar as sugestões
        StringBuilder msg = new StringBuilder();
        msg.append("Foram detectados os seguintes componentes sugeridos:\n\n");
        for (ComponenteItem comp : sugestoes) {
            msg.append("• ").append(comp.getTipo()).append(": ").append(comp.getDescricao()).append("\n");
        }
        msg.append("\nDeseja adicionar estes componentes?");
        
        int opcao = JOptionPane.showConfirmDialog(this,
            msg.toString(),
            "Componentes Detectados",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            for (ComponenteItem comp : sugestoes) {
                comp.setOrdem(componentes.size() + 1);
                componentes.add(comp);
                modelComponentes.addRow(new Object[]{
                    comp.getTipo(),
                    comp.getDescricao(),
                    comp.getQuantidadeEsperada(),
                    comp.getOrdem()
                });
            }
            JOptionPane.showMessageDialog(this,
                sugestoes.size() + " componente(s) adicionado(s) com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void salvarItemComposto() {
        if (patrimonioAtual == null) {
            JOptionPane.showMessageDialog(this,
                "Nenhum patrimônio selecionado.",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            boolean jaEraItemComposto = itemCompostoDAO.isItemComposto(patrimonioAtual.getId());
            
            // Se desmarcou o checkbox e já era item composto, perguntar se quer remover
            if (!chkItemComposto.isSelected() && jaEraItemComposto) {
                int opcao = JOptionPane.showConfirmDialog(this,
                    "O checkbox 'Item Composto' está desmarcado.\n\n" +
                    "Deseja remover este patrimônio dos itens compostos?\n" +
                    "(Todos os componentes serão excluídos)",
                    "Remover Item Composto?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (opcao == JOptionPane.YES_OPTION) {
                    removerItemComposto();
                    return;
                } else if (opcao == JOptionPane.CANCEL_OPTION) {
                    return;
                } else {
                    // NO - restaurar checkbox e não fazer nada
                    chkItemComposto.setSelected(true);
                    return;
                }
            }
            
            if (!chkItemComposto.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Marque a opção 'Item Composto' para salvar.",
                    "Validação",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (componentes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Adicione pelo menos um componente.",
                    "Validação",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            java.util.Map<String, Object> infoColetas = null;
            int componentesAntigos = 0;
            
            // Verificar se já existe e se há coletas no inventário ativo
            if (jaEraItemComposto) {
                // Verificar coletas existentes
                infoColetas = itemCompostoDAO.verificarColetasExistentes(patrimonioAtual.getId(), null);
                boolean temColetas = (Boolean) infoColetas.get("temColetas");
                componentesAntigos = (Integer) infoColetas.get("totalComponentes");
                int componentesColetados = (Integer) infoColetas.get("componentesColetados");
                String inventarioNome = (String) infoColetas.get("inventarioNome");
                
                // Verificar se está adicionando novos componentes (mais do que tinha antes)
                boolean adicionandoNovos = componentes.size() > componentesAntigos;
                int novosComponentes = componentes.size() - componentesAntigos;
                
                if (temColetas && adicionandoNovos) {
                    // Aviso especial: há coletas e está adicionando novos componentes
                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("⚠️ ATENÇÃO: Este item composto já possui coletas registradas!\n\n");
                    mensagem.append("📋 Inventário: ").append(inventarioNome).append("\n");
                    mensagem.append("📊 Componentes coletados: ").append(componentesColetados)
                            .append("/").append(componentesAntigos).append("\n\n");
                    mensagem.append("Você está adicionando ").append(novosComponentes)
                            .append(" novo(s) componente(s).\n\n");
                    mensagem.append("⚡ Os novos componentes precisarão ser coletados\n");
                    mensagem.append("   na tela 'Coleta de Itens Compostos' para que\n");
                    mensagem.append("   o status do item fique COMPLETO.\n\n");
                    mensagem.append("Deseja continuar?");
                    
                    int opcao = JOptionPane.showConfirmDialog(this,
                        mensagem.toString(),
                        "Aviso - Novos Componentes Pendentes de Coleta",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                } else if (temColetas) {
                    // Aviso: há coletas mas não está adicionando novos
                    int opcao = JOptionPane.showConfirmDialog(this,
                        """
                        Este patrim\u00f4nio j\u00e1 possui componentes cadastrados e coletas registradas.
                        Invent\u00e1rio: """ + inventarioNome + "\n" +
                        "Componentes coletados: " + componentesColetados + "/" + componentesAntigos + "\n\n" +
                        "Deseja substituir os componentes?\n" +
                        "⚠️ As coletas existentes serão mantidas para componentes equivalentes.",
                        "Confirmar Substituição",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                } else {
                    // Sem coletas, apenas confirmar substituição
                    int opcao = JOptionPane.showConfirmDialog(this, """
                                                                    Este patrim\u00f4nio j\u00e1 possui componentes cadastrados.
                                                                    Deseja substituir pelos novos componentes?""",
                        "Confirmar Substituição",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                // Remover componentes antigos
                java.util.List<java.util.Map<String, Object>> compsAntigos = 
                    itemCompostoDAO.buscarComponentes(patrimonioAtual.getId());
                for (java.util.Map<String, Object> comp : compsAntigos) {
                    itemCompostoDAO.removerComponente((Integer) comp.get("id"));
                }
            }
            
            // Salvar novos componentes no banco
            for (ComponenteItem comp : componentes) {
                itemCompostoDAO.adicionarComponente(
                    patrimonioAtual.getId(),
                    comp.getTipo(),
                    comp.getDescricao(),
                    comp.getQuantidadeEsperada(),
                    true, // obrigatório
                    null  // observação
                );
            }
            
            // Mensagem de sucesso com aviso sobre coleta se necessário
            StringBuilder msgSucesso = new StringBuilder();
            msgSucesso.append("Item composto salvo com sucesso!\n");
            msgSucesso.append("Patrimônio: ").append(patrimonioAtual.getNumero()).append("\n");
            msgSucesso.append("Componentes: ").append(componentes.size());
            
            if (jaEraItemComposto && infoColetas != null && (Boolean) infoColetas.get("temColetas")) {
                int novos = componentes.size() - componentesAntigos;
                if (novos > 0) {
                    msgSucesso.append("\n\n💡 Lembre-se: ").append(novos)
                              .append(" novo(s) componente(s) precisam ser coletados!");
                }
            }
            
            JOptionPane.showMessageDialog(this,
                msgSucesso.toString(),
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            
            limparFormulario();
            
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar item composto: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limparFormulario() {
        patrimonioAtual = null;
        txtNumeroPatrimonio.setText("");
        lblPatrimonioId.setText("");
        lblDescricao.setText("");
        lblSala.setText("");
        lblResponsavel.setText("");
        lblValor.setText("");
        chkItemComposto.setSelected(false);
        chkDeteccaoAutomatica.setSelected(false);
        chkItemComposto.setEnabled(false);
        chkDeteccaoAutomatica.setEnabled(false);
        componentes.clear();
        modelComponentes.setRowCount(0);
        habilitarComponentes(false);
        btnSalvar.setEnabled(false);
        btnRemoverItemComposto.setEnabled(false);
        txtNumeroPatrimonio.requestFocus();
    }
    
    /**
     * Remove o patrimônio dos itens compostos, excluindo todos os seus componentes
     */
    private void removerItemComposto() {
        if (patrimonioAtual == null) {
            JOptionPane.showMessageDialog(this,
                "Nenhum patrimônio selecionado.",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Verificar se realmente é um item composto
            if (!itemCompostoDAO.isItemComposto(patrimonioAtual.getId())) {
                JOptionPane.showMessageDialog(this,
                    "Este patrimônio não é um item composto.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Verificar se há coletas existentes
            java.util.Map<String, Object> infoColetas = itemCompostoDAO.verificarColetasExistentes(patrimonioAtual.getId(), null);
            boolean temColetas = (Boolean) infoColetas.get("temColetas");
            int totalComponentes = (Integer) infoColetas.get("totalComponentes");
            int componentesColetados = (Integer) infoColetas.get("componentesColetados");
            String inventarioNome = (String) infoColetas.get("inventarioNome");
            
            // Construir mensagem de confirmação
            StringBuilder mensagem = new StringBuilder();
            mensagem.append("⚠️ ATENÇÃO: Você está prestes a remover este patrimônio dos itens compostos!\n\n");
            mensagem.append("📦 Patrimônio: ").append(patrimonioAtual.getNumero()).append("\n");
            mensagem.append("📝 Descrição: ").append(patrimonioAtual.getDescricao()).append("\n");
            mensagem.append("🔧 Componentes cadastrados: ").append(totalComponentes).append("\n\n");
            
            if (temColetas) {
                mensagem.append("⚠️ AVISO IMPORTANTE:\n");
                mensagem.append("Este item possui coletas registradas no inventário '")
                        .append(inventarioNome).append("'!\n");
                mensagem.append("Componentes coletados: ").append(componentesColetados)
                        .append("/").append(totalComponentes).append("\n\n");
                mensagem.append("🗑️ As coletas de componentes também serão REMOVIDAS!\n\n");
            }
            
            mensagem.append("Esta ação irá:\n");
            mensagem.append("• Remover todos os ").append(totalComponentes).append(" componente(s) cadastrado(s)\n");
            if (temColetas) {
                mensagem.append("• Remover todas as coletas de componentes associadas\n");
            }
            mensagem.append("• O patrimônio voltará a ser um item simples\n\n");
            mensagem.append("Esta ação NÃO pode ser desfeita. Deseja continuar?");
            
            int opcao = JOptionPane.showConfirmDialog(this,
                mensagem.toString(),
                "Confirmar Remoção de Item Composto",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (opcao != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Executar remoção
            int removidos = itemCompostoDAO.removerItemComposto(patrimonioAtual.getId());
            
            // Mensagem de sucesso
            StringBuilder msgSucesso = new StringBuilder();
            msgSucesso.append("✅ Item composto removido com sucesso!\n\n");
            msgSucesso.append("Patrimônio: ").append(patrimonioAtual.getNumero()).append("\n");
            msgSucesso.append("Componentes removidos: ").append(removidos).append("\n\n");
            msgSucesso.append("O patrimônio agora é um item simples.");
            
            JOptionPane.showMessageDialog(this,
                msgSucesso.toString(),
                "Remoção Concluída",
                JOptionPane.INFORMATION_MESSAGE);
            
            limparFormulario();
            
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao remover item composto: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== MÉTODOS DA ABA DE PESQUISA ====================
    
    /**
     * Carrega todos os itens compostos na tabela de resultados
     */
    private void carregarTodosItensCompostos() {
        try {
            java.util.List<java.util.Map<String, Object>> itens = itemCompostoDAO.listarItensCompostosComDetalhes();
            preencherTabelaResultados(itens);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar itens compostos: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Pesquisa itens compostos por número ou descrição
     */
    private void pesquisarItensCompostos() {
        String numero = txtPesquisaNumero.getText().trim();
        String descricao = txtPesquisaDescricao.getText().trim();
        
        try {
            java.util.List<java.util.Map<String, Object>> itens;
            
            if (numero.isEmpty() && descricao.isEmpty()) {
                // Se ambos vazios, carregar todos
                itens = itemCompostoDAO.listarItensCompostosComDetalhes();
            } else {
                // Pesquisar com filtro
                String filtro = !numero.isEmpty() ? numero : descricao;
                itens = itemCompostoDAO.buscarItensCompostosPorDescricao(filtro);
            }
            
            preencherTabelaResultados(itens);
            
            if (itens.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Nenhum item composto encontrado com os filtros informados.",
                    "Pesquisa",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao pesquisar itens compostos: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Preenche a tabela de resultados com os itens encontrados
     */
    private void preencherTabelaResultados(java.util.List<java.util.Map<String, Object>> itens) {
        modelResultados.setRowCount(0);
        
        for (java.util.Map<String, Object> item : itens) {
            modelResultados.addRow(new Object[]{
                item.get("id"),
                item.get("numero"),
                item.get("descricao"),
                item.get("sala") != null ? item.get("sala") : "Sem sala",
                item.get("totalComponentes")
            });
        }
    }
    
    /**
     * Limpa os filtros de pesquisa e recarrega todos os itens
     */
    private void limparPesquisa() {
        txtPesquisaNumero.setText("");
        txtPesquisaDescricao.setText("");
        carregarTodosItensCompostos();
    }
    
    /**
     * Abre o item selecionado na aba de cadastro (modo visualização)
     */
    private void abrirItemSelecionado() {
        int selectedRow = tblResultados.getSelectedRow();
        if (selectedRow == -1) return;
        
        String numero = (String) modelResultados.getValueAt(selectedRow, 1);
        
        // Mudar para aba de cadastro
        tabbedPane.setSelectedIndex(0);
        
        // Preencher o campo de busca e buscar
        txtNumeroPatrimonio.setText(numero);
        SwingUtilities.invokeLater(this::buscarPatrimonio);
    }
    
    /**
     * Abre o item selecionado na aba de cadastro para edição
     */
    private void editarItemSelecionado() {
        abrirItemSelecionado(); // Mesmo comportamento de abrir
    }
    
    /**
     * Exclui o item composto selecionado após confirmação
     */
    private void excluirItemSelecionado() {
        int selectedRow = tblResultados.getSelectedRow();
        if (selectedRow == -1) return;
        
        int idPatrimonio = (Integer) modelResultados.getValueAt(selectedRow, 0);
        String numero = (String) modelResultados.getValueAt(selectedRow, 1);
        String descricao = (String) modelResultados.getValueAt(selectedRow, 2);
        int totalComponentes = (Integer) modelResultados.getValueAt(selectedRow, 4);
        
        try {
            // Verificar se há coletas existentes
            java.util.Map<String, Object> infoColetas = itemCompostoDAO.verificarColetasExistentes(idPatrimonio, null);
            boolean temColetas = (Boolean) infoColetas.get("temColetas");
            int componentesColetados = (Integer) infoColetas.get("componentesColetados");
            String inventarioNome = (String) infoColetas.get("inventarioNome");
            
            // Construir mensagem de confirmação
            StringBuilder mensagem = new StringBuilder();
            mensagem.append("⚠️ CONFIRMAR EXCLUSÃO\n\n");
            mensagem.append("Você está prestes a excluir o seguinte item composto:\n\n");
            mensagem.append("📦 Número: ").append(numero).append("\n");
            mensagem.append("📝 Descrição: ").append(descricao).append("\n");
            mensagem.append("🔧 Componentes: ").append(totalComponentes).append("\n\n");
            
            if (temColetas) {
                mensagem.append("⚠️ ATENÇÃO: Este item possui coletas registradas!\n");
                mensagem.append("📋 Inventário: ").append(inventarioNome).append("\n");
                mensagem.append("📊 Componentes coletados: ").append(componentesColetados)
                        .append("/").append(totalComponentes).append("\n\n");
                mensagem.append("🗑️ As coletas de componentes também serão REMOVIDAS!\n\n");
            }
            
            mensagem.append("Esta ação NÃO pode ser desfeita.\n");
            mensagem.append("Deseja realmente excluir?");
            
            int opcao = JOptionPane.showConfirmDialog(this,
                mensagem.toString(),
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (opcao != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Executar exclusão
            int removidos = itemCompostoDAO.removerItemComposto(idPatrimonio);
            
            // Mensagem de sucesso
            JOptionPane.showMessageDialog(this,
                "✅ Item composto excluído com sucesso!\n\n" +
                "Patrimônio: " + numero + "\n" +
                "Componentes removidos: " + removidos,
                "Exclusão Concluída",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Atualizar tabela
            modelResultados.removeRow(selectedRow);
            
            // Desabilitar botões
            btnAbrirItem.setEnabled(false);
            btnEditarItem.setEnabled(false);
            btnExcluirItem.setEnabled(false);
            
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao excluir item composto: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Exclui múltiplos itens compostos selecionados (apenas para administradores)
     * Itens com coletas existentes NÃO serão excluídos
     */
    private void excluirItensSelecionados() {
        // Verificar se é administrador
        if (!isUsuarioAdmin()) {
            JOptionPane.showMessageDialog(this,
                "Apenas administradores podem excluir múltiplos itens.",
                "Acesso Negado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int[] selectedRows = tblResultados.getSelectedRows();
        if (selectedRows.length < 2) {
            JOptionPane.showMessageDialog(this,
                "Selecione pelo menos 2 itens para exclusão em lote.",
                "Seleção Insuficiente",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Coletar informações dos itens selecionados
        List<Integer> idsParaExcluir = new ArrayList<>();
        List<String> itensComColeta = new ArrayList<>();
        List<String> itensSemColeta = new ArrayList<>();
        
        for (int row : selectedRows) {
            int idPatrimonio = (Integer) modelResultados.getValueAt(row, 0);
            String numero = (String) modelResultados.getValueAt(row, 1);
            
            try {
                java.util.Map<String, Object> infoColetas = itemCompostoDAO.verificarColetasExistentes(idPatrimonio, null);
                boolean temColetas = (Boolean) infoColetas.get("temColetas");
                
                if (temColetas) {
                    itensComColeta.add(numero);
                } else {
                    idsParaExcluir.add(idPatrimonio);
                    itensSemColeta.add(numero);
                }
            } catch (SQLException e) {
                System.err.println("Erro ao verificar coletas do patrimônio " + numero + ": " + e.getMessage());
            }
        }
        
        // Verificar se há itens que podem ser excluídos
        if (idsParaExcluir.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("❌ Nenhum item pode ser excluído!\n\n");
            msg.append("Todos os ").append(selectedRows.length).append(" itens selecionados possuem coletas registradas:\n\n");
            for (String numero : itensComColeta) {
                msg.append("• ").append(numero).append("\n");
            }
            msg.append("\n⚠️ Itens com coletas não podem ser excluídos em lote.");
            
            JOptionPane.showMessageDialog(this,
                msg.toString(),
                "Exclusão Não Permitida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Construir mensagem de confirmação
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("⚠️ EXCLUSÃO EM LOTE\n\n");
        mensagem.append("📊 Resumo da seleção:\n");
        mensagem.append("• Total selecionados: ").append(selectedRows.length).append("\n");
        mensagem.append("• Podem ser excluídos: ").append(idsParaExcluir.size()).append("\n");
        mensagem.append("• Com coletas (protegidos): ").append(itensComColeta.size()).append("\n\n");
        
        if (!itensComColeta.isEmpty()) {
            mensagem.append("🔒 Itens PROTEGIDOS (possuem coletas):\n");
            for (String numero : itensComColeta) {
                mensagem.append("   • ").append(numero).append("\n");
            }
            mensagem.append("\n");
        }
        
        mensagem.append("🗑️ Itens que serão EXCLUÍDOS:\n");
        for (String numero : itensSemColeta) {
            mensagem.append("   • ").append(numero).append("\n");
        }
        
        mensagem.append("\n⚠️ Esta ação NÃO pode ser desfeita!\n");
        mensagem.append("Deseja continuar com a exclusão?");
        
        int opcao = JOptionPane.showConfirmDialog(this,
            mensagem.toString(),
            "Confirmar Exclusão em Lote",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Executar exclusão
        int excluidos = 0;
        int erros = 0;
        List<String> errosDetalhes = new ArrayList<>();
        
        for (int i = 0; i < idsParaExcluir.size(); i++) {
            int idPatrimonio = idsParaExcluir.get(i);
            String numero = itensSemColeta.get(i);
            
            try {
                itemCompostoDAO.removerItemComposto(idPatrimonio);
                excluidos++;
            } catch (SQLException e) {
                erros++;
                errosDetalhes.add(numero + ": " + e.getMessage());
            }
        }
        
        // Mensagem de resultado
        StringBuilder resultado = new StringBuilder();
        if (excluidos > 0) {
            resultado.append("✅ Exclusão concluída!\n\n");
            resultado.append("📊 Resultado:\n");
            resultado.append("• Itens excluídos: ").append(excluidos).append("\n");
            resultado.append("• Itens protegidos: ").append(itensComColeta.size()).append("\n");
            
            if (erros > 0) {
                resultado.append("• Erros: ").append(erros).append("\n\n");
                resultado.append("⚠️ Detalhes dos erros:\n");
                for (String erro : errosDetalhes) {
                    resultado.append("   • ").append(erro).append("\n");
                }
            }
        } else {
            resultado.append("❌ Nenhum item foi excluído.\n\n");
            if (erros > 0) {
                resultado.append("Erros encontrados:\n");
                for (String erro : errosDetalhes) {
                    resultado.append("• ").append(erro).append("\n");
                }
            }
        }
        
        JOptionPane.showMessageDialog(this,
            resultado.toString(),
            excluidos > 0 ? "Exclusão Concluída" : "Erro na Exclusão",
            excluidos > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        
        // Atualizar tabela
        if (excluidos > 0) {
            carregarTodosItensCompostos();
            lblSelecaoInfo.setText("");
            btnExcluirSelecionados.setEnabled(false);
        }
    }
    
    /**
     * Verifica se o usuário logado é administrador
     */
    private boolean isUsuarioAdmin() {
        if (usuarioLogado == null) {
            return false;
        }
        return usuarioLogado.getPerfil() == com.inventario.sihcp.model.PerfilUsuario.ADMIN;
    }
    
    private boolean formularioAlterado() {
        return patrimonioAtual != null && 
               (chkItemComposto.isSelected() || !componentes.isEmpty());
    }
    
    // Métodos auxiliares para criar componentes
    
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
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        // Usar Segoe UI Emoji para suportar emojis nos botões
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));
        
        // Efeito hover
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
    
    // Classe interna para representar um componente
    public static class ComponenteItem {
        private String tipo;
        private String descricao;
        private int quantidadeEsperada;
        private int ordem;
        
        public ComponenteItem(String tipo, String descricao, int quantidadeEsperada, int ordem) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.quantidadeEsperada = quantidadeEsperada;
            this.ordem = ordem;
        }
        
        // Getters e Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public int getQuantidadeEsperada() { return quantidadeEsperada; }
        public void setQuantidadeEsperada(int quantidadeEsperada) { 
            this.quantidadeEsperada = quantidadeEsperada; 
        }
        
        public int getOrdem() { return ordem; }
        public void setOrdem(int ordem) { this.ordem = ordem; }
    }
}
