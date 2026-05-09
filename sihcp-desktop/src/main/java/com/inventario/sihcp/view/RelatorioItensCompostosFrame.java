package com.inventario.sihcp.view;

import com.inventario.sihcp.model.*;
import com.inventario.sihcp.presentation.state.RelatorioItemCompostoState;
import com.inventario.sihcp.presentation.viewmodel.RelatorioItemCompostoViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Frame para relatório de integridade de itens compostos.
 * Refatorado para usar padrão MVVM com RelatorioItemCompostoViewModel.
 * 
 * Exibe estatísticas, detalhes e permite exportação em Excel, PDF e CSV.
 * 
 * @author Sistema de Inventário
 * @version 2.0.0 - MVVM
 */
public class RelatorioItensCompostosFrame extends JFrame {
    
    // === MVVM - ViewModel ===
    private final RelatorioItemCompostoViewModel viewModel;
    
    // Componentes de filtro
    private JComboBox<ComboItem<Inventario>> cmbInventario;
    private JComboBox<ComboItem<Setor>> cmbSetor;
    private JComboBox<ComboItem<Sala>> cmbSala;
    private JComboBox<ComboItem<Responsavel>> cmbResponsavel;
    private JComboBox<String> cmbFiltroStatus;
    private JTextField txtFiltroPesquisa;
    private JButton btnFiltrar;
    private JButton btnLimparFiltro;
    
    // Estatísticas
    private JLabel lblTotalConjuntos;
    private JLabel lblConjuntosCompletos;
    private JLabel lblConjuntosIncompletos;
    private JLabel lblConjuntosParciais;
    private JLabel lblTaxaIntegridade;
    
    // Tabela principal
    private JTable tblRelatorio;
    private DefaultTableModel modelRelatorio;
    
    // Botões de ação
    private JButton btnExportarExcel;
    private JButton btnExportarPDF;
    private JButton btnExportarCSV;
    private JButton btnAtualizar;
    private JButton btnFechar;

    
    public RelatorioItensCompostosFrame() {
        this.viewModel = new RelatorioItemCompostoViewModel();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        observarViewModel();
        
        // Carregar dados iniciais
        carregarDadosIniciais();
        
        setTitle("Relatório de Integridade de Itens Compostos");
        setSize(1300, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
    }
    
    /**
     * === MVVM: Observer Pattern ===
     * Observa mudanças no ViewModel e atualiza a UI
     */
    private void observarViewModel() {
        viewModel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("state".equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(() -> {
                        atualizarUI((RelatorioItemCompostoState) evt.getNewValue());
                    });
                }
            }
        });
    }
    
    /**
     * === MVVM: Atualização Centralizada da UI ===
     */
    private void atualizarUI(RelatorioItemCompostoState state) {
        if (state instanceof RelatorioItemCompostoState.Idle) {
            setCursor(Cursor.getDefaultCursor());
            
        } else if (state instanceof RelatorioItemCompostoState.Loading) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
        } else if (state instanceof RelatorioItemCompostoState.Success) {
            setCursor(Cursor.getDefaultCursor());
            RelatorioItemCompostoState.Success success = (RelatorioItemCompostoState.Success) state;
            preencherTabela(success.getItens());
            atualizarEstatisticas(success.getEstatisticas());
            
        } else if (state instanceof RelatorioItemCompostoState.DetalhesCarregados) {
            setCursor(Cursor.getDefaultCursor());
            RelatorioItemCompostoState.DetalhesCarregados detalhes = 
                    (RelatorioItemCompostoState.DetalhesCarregados) state;
            abrirDialogoDetalhes(detalhes);
            
        } else if (state instanceof RelatorioItemCompostoState.ExportacaoSucesso) {
            setCursor(Cursor.getDefaultCursor());
            RelatorioItemCompostoState.ExportacaoSucesso exportacao = 
                    (RelatorioItemCompostoState.ExportacaoSucesso) state;
            mostrarSucessoExportacao(exportacao);
            
        } else if (state instanceof RelatorioItemCompostoState.InventariosCarregados) {
            preencherComboInventarios(((RelatorioItemCompostoState.InventariosCarregados) state).getInventarios());
            
        } else if (state instanceof RelatorioItemCompostoState.SetoresCarregados) {
            preencherComboSetores(((RelatorioItemCompostoState.SetoresCarregados) state).getSetores());
            
        } else if (state instanceof RelatorioItemCompostoState.SalasCarregadas) {
            preencherComboSalas(((RelatorioItemCompostoState.SalasCarregadas) state).getSalas());
            
        } else if (state instanceof RelatorioItemCompostoState.ResponsaveisCarregados) {
            preencherComboResponsaveis(((RelatorioItemCompostoState.ResponsaveisCarregados) state).getResponsaveis());
            
        } else if (state instanceof RelatorioItemCompostoState.Error) {
            setCursor(Cursor.getDefaultCursor());
            RelatorioItemCompostoState.Error error = (RelatorioItemCompostoState.Error) state;
            JOptionPane.showMessageDialog(this, error.getMensagem(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void carregarDadosIniciais() {
        // Carregar dados para os combos
        viewModel.carregarInventarios();
        viewModel.carregarSetores();
        viewModel.carregarResponsaveis();
    }
    
    private void initializeComponents() {
        // Filtros
        cmbInventario = new JComboBox<>();
        cmbSetor = new JComboBox<>();
        cmbSala = new JComboBox<>();
        cmbResponsavel = new JComboBox<>();
        cmbFiltroStatus = new JComboBox<>(new String[]{"Todos", "Completo", "Incompleto", "Parcial"});
        
        txtFiltroPesquisa = new JTextField(15);
        txtFiltroPesquisa.setToolTipText("Pesquisar por número ou descrição");
        
        btnFiltrar = createButton("🔍 Aplicar Filtros", new Color(52, 152, 219));
        btnLimparFiltro = createButton("🗑️ Limpar", new Color(149, 165, 166));
        
        // Labels de estatísticas
        lblTotalConjuntos = createStatLabel("0");
        lblConjuntosCompletos = createStatLabel("0");
        lblConjuntosIncompletos = createStatLabel("0");
        lblConjuntosParciais = createStatLabel("0");
        lblTaxaIntegridade = createStatLabel("0%");
        
        // Tabela
        String[] colunas = {
            "ID", "Nº Patrimônio", "Descrição", "Sala", "Responsável", 
            "Esperados", "Encontrados", "Faltantes", "Taxa", "Status", "Componentes Faltantes"
        };
        modelRelatorio = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6 || columnIndex == 7) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        tblRelatorio = new JTable(modelRelatorio);
        tblRelatorio.setFont(new Font("Arial", Font.PLAIN, 11));
        tblRelatorio.setRowHeight(25);
        tblRelatorio.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tblRelatorio.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblRelatorio.setFillsViewportHeight(true);
        tblRelatorio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Ocultar coluna ID
        tblRelatorio.getColumnModel().getColumn(0).setMinWidth(0);
        tblRelatorio.getColumnModel().getColumn(0).setMaxWidth(0);
        tblRelatorio.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        // Renderizador de cores para status
        tblRelatorio.getColumnModel().getColumn(9).setCellRenderer(new StatusCellRenderer());
        
        // Ajustar larguras
        tblRelatorio.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblRelatorio.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblRelatorio.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblRelatorio.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblRelatorio.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblRelatorio.getColumnModel().getColumn(6).setPreferredWidth(80);
        tblRelatorio.getColumnModel().getColumn(7).setPreferredWidth(70);
        tblRelatorio.getColumnModel().getColumn(8).setPreferredWidth(60);
        tblRelatorio.getColumnModel().getColumn(9).setPreferredWidth(80);
        tblRelatorio.getColumnModel().getColumn(10).setPreferredWidth(150);
        
        // Botões de ação
        btnExportarExcel = createButton("📊 Excel", new Color(39, 174, 96));
        btnExportarPDF = createButton("📄 PDF", new Color(231, 76, 60));
        btnExportarCSV = createButton("📋 CSV", new Color(155, 89, 182));
        btnAtualizar = createButton("🔄 Atualizar", new Color(52, 152, 219));
        btnFechar = createButton("❌ Fechar", new Color(127, 140, 141));
    }

    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Painel superior - Título
        JPanel panelTitulo = createPanelTitulo();
        
        // Painel de estatísticas
        JPanel panelEstatisticas = createPanelEstatisticas();
        
        // Painel de filtros
        JPanel panelFiltros = createPanelFiltros();
        
        // Painel superior combinado
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setOpaque(false);
        panelSuperior.add(panelTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelEstatisticas, BorderLayout.CENTER);
        panelSuperior.add(panelFiltros, BorderLayout.SOUTH);
        
        // Painel central - Tabela
        JPanel panelTabela = createPanelTabela();
        
        // Painel inferior - Botões
        JPanel panelBotoes = createPanelBotoes();
        
        mainPanel.add(panelSuperior, BorderLayout.NORTH);
        mainPanel.add(panelTabela, BorderLayout.CENTER);
        mainPanel.add(panelBotoes, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createPanelTitulo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(new Color(155, 89, 182));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("📊 Relatório de Integridade - Itens Compostos");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        
        panel.add(lblTitulo);
        
        return panel;
    }
    
    private JPanel createPanelEstatisticas() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        panel.add(createStatCard("📦 Total Conjuntos", lblTotalConjuntos, new Color(52, 152, 219)));
        panel.add(createStatCard("✅ Completos", lblConjuntosCompletos, new Color(46, 204, 113)));
        panel.add(createStatCard("❌ Incompletos", lblConjuntosIncompletos, new Color(231, 76, 60)));
        panel.add(createStatCard("⚠️ Parciais", lblConjuntosParciais, new Color(241, 196, 15)));
        panel.add(createStatCard("📈 Taxa Integridade", lblTaxaIntegridade, new Color(52, 73, 94)));
        
        return panel;
    }
    
    private JPanel createStatCard(String titulo, JLabel lblValor, Color cor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, cor));
        
        JLabel lblTitulo = new JLabel(titulo);
        // Usar Segoe UI Emoji para suportar emojis nos títulos dos cards
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(127, 140, 141));
        lblTitulo.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        lblValor.setFont(new Font("Arial", Font.BOLD, 24));
        lblValor.setForeground(cor);
        lblValor.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        
        return card;
    }

    
    private JPanel createPanelFiltros() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        
        // Linha 1: Filtros principais
        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        linha1.setOpaque(false);
        
        linha1.add(new JLabel("Inventário:"));
        cmbInventario.setPreferredSize(new Dimension(150, 25));
        linha1.add(cmbInventario);
        
        linha1.add(Box.createHorizontalStrut(5));
        linha1.add(new JLabel("Setor:"));
        cmbSetor.setPreferredSize(new Dimension(100, 25));
        linha1.add(cmbSetor);
        
        linha1.add(Box.createHorizontalStrut(5));
        linha1.add(new JLabel("Sala:"));
        cmbSala.setPreferredSize(new Dimension(100, 25));
        linha1.add(cmbSala);
        
        linha1.add(Box.createHorizontalStrut(5));
        linha1.add(new JLabel("Responsável:"));
        cmbResponsavel.setPreferredSize(new Dimension(120, 25));
        linha1.add(cmbResponsavel);
        
        linha1.add(Box.createHorizontalStrut(5));
        linha1.add(new JLabel("Status:"));
        cmbFiltroStatus.setPreferredSize(new Dimension(90, 25));
        linha1.add(cmbFiltroStatus);
        
        linha1.add(Box.createHorizontalStrut(5));
        linha1.add(new JLabel("Pesquisar:"));
        linha1.add(txtFiltroPesquisa);
        
        // Linha 2: Botões de filtro e ação
        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        linha2.setOpaque(false);
        
        linha2.add(btnFiltrar);
        linha2.add(btnLimparFiltro);
        linha2.add(Box.createHorizontalStrut(20));
        linha2.add(btnAtualizar);
        linha2.add(btnExportarExcel);
        linha2.add(btnExportarPDF);
        linha2.add(btnExportarCSV);
        linha2.add(Box.createHorizontalStrut(20));
        linha2.add(btnFechar);
        
        panel.add(linha1, BorderLayout.NORTH);
        panel.add(linha2, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPanelTabela() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Detalhes da Integridade (duplo clique para ver componentes)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(52, 152, 219)
        ));
        
        JScrollPane scrollPane = new JScrollPane(tblRelatorio);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelBotoes() {
        // Botões agora estão no painel de filtros (linha 2)
        // Este painel fica vazio para manter compatibilidade com o layout
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setBackground(new Color(236, 240, 241));
        panel.setPreferredSize(new Dimension(0, 10)); // Altura mínima
        return panel;
    }
    
    private void setupEventListeners() {
        // Filtros
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnLimparFiltro.addActionListener(e -> limparFiltros());
        txtFiltroPesquisa.addActionListener(e -> aplicarFiltros());
        
        // Setor -> Sala (cascata)
        cmbSetor.addActionListener(e -> {
            @SuppressWarnings("unchecked")
            ComboItem<Setor> selected = (ComboItem<Setor>) cmbSetor.getSelectedItem();
            if (selected != null && selected.getValue() != null) {
                viewModel.carregarSalas(selected.getValue().getId());
            } else {
                viewModel.carregarSalas(null);
            }
        });
        
        // Botões de ação
        btnAtualizar.addActionListener(e -> aplicarFiltros());
        btnExportarExcel.addActionListener(e -> exportarExcel());
        btnExportarPDF.addActionListener(e -> exportarPDF());
        btnExportarCSV.addActionListener(e -> exportarCSV());
        btnFechar.addActionListener(e -> dispose());
        
        // Duplo clique na tabela para detalhes
        tblRelatorio.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblRelatorio.getSelectedRow();
                    if (row >= 0) {
                        abrirDetalhes(row);
                    }
                }
            }
        });
        
        // Ordenação por clique no cabeçalho
        tblRelatorio.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = tblRelatorio.columnAtPoint(e.getPoint());
                String colName = tblRelatorio.getColumnName(col);
                viewModel.ordenarPor(colName);
            }
        });
    }

    
    // ========== MÉTODOS DE AÇÃO ==========
    
    @SuppressWarnings("unchecked")
    private void aplicarFiltros() {
        FiltroRelatorioItemComposto filtro = new FiltroRelatorioItemComposto();
        
        // Inventário
        ComboItem<Inventario> invItem = (ComboItem<Inventario>) cmbInventario.getSelectedItem();
        if (invItem != null && invItem.getValue() != null) {
            filtro.setIdInventario(invItem.getValue().getId());
        }
        
        // Setor
        ComboItem<Setor> setorItem = (ComboItem<Setor>) cmbSetor.getSelectedItem();
        if (setorItem != null && setorItem.getValue() != null) {
            filtro.setIdSetor(setorItem.getValue().getId());
        }
        
        // Sala
        ComboItem<Sala> salaItem = (ComboItem<Sala>) cmbSala.getSelectedItem();
        if (salaItem != null && salaItem.getValue() != null) {
            filtro.setIdSala(salaItem.getValue().getId());
        }
        
        // Responsável
        ComboItem<Responsavel> respItem = (ComboItem<Responsavel>) cmbResponsavel.getSelectedItem();
        if (respItem != null && respItem.getValue() != null) {
            filtro.setIdResponsavel(respItem.getValue().getId());
        }
        
        // Status
        String statusStr = (String) cmbFiltroStatus.getSelectedItem();
        if (statusStr != null && !"Todos".equals(statusStr)) {
            filtro.setStatusIntegridade(StatusIntegridade.fromDescricao(statusStr));
        }
        
        viewModel.carregarRelatorio(filtro);
    }
    
    private void limparFiltros() {
        cmbInventario.setSelectedIndex(0);
        cmbSetor.setSelectedIndex(0);
        cmbSala.removeAllItems();
        cmbSala.addItem(new ComboItem<>("Todas", null));
        cmbResponsavel.setSelectedIndex(0);
        cmbFiltroStatus.setSelectedIndex(0);
        txtFiltroPesquisa.setText("");
        
        modelRelatorio.setRowCount(0);
        atualizarEstatisticas(null);
    }
    
    private void abrirDetalhes(int row) {
        Integer idPatrimonio = (Integer) modelRelatorio.getValueAt(row, 0);
        List<ItemCompostoResumo> itens = viewModel.getItensAtuais();
        
        for (ItemCompostoResumo item : itens) {
            if (item.getIdPatrimonio().equals(idPatrimonio)) {
                viewModel.carregarDetalhes(item);
                break;
            }
        }
    }
    
    // ========== MÉTODOS DE EXPORTAÇÃO ==========
    
    private void exportarExcel() {
        String caminho = selecionarArquivoSalvar("xlsx", "Arquivos Excel (*.xlsx)");
        if (caminho != null) {
            viewModel.exportarExcel(caminho);
        }
    }
    
    private void exportarPDF() {
        String caminho = selecionarArquivoSalvar("pdf", "Arquivos PDF (*.pdf)");
        if (caminho != null) {
            viewModel.exportarPDF(caminho);
        }
    }
    
    private void exportarCSV() {
        String caminho = selecionarArquivoSalvar("csv", "Arquivos CSV (*.csv)");
        if (caminho != null) {
            viewModel.exportarCSV(caminho);
        }
    }
    
    private String selecionarArquivoSalvar(String extensao, String descricao) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório");
        
        // Nome sugerido com data/hora
        String nomeArquivo = "RelatorioItensCompostos_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + extensao;
        fileChooser.setSelectedFile(new File(nomeArquivo));
        
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(descricao, extensao));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String caminho = fileChooser.getSelectedFile().getAbsolutePath();
            if (!caminho.toLowerCase().endsWith("." + extensao)) {
                caminho += "." + extensao;
            }
            return caminho;
        }
        return null;
    }
    
    private void mostrarSucessoExportacao(RelatorioItemCompostoState.ExportacaoSucesso exportacao) {
        int opcao = JOptionPane.showConfirmDialog(this,
                "Arquivo exportado com sucesso!\n\n" + exportacao.getCaminhoArquivo() + 
                "\n\nDeseja abrir o arquivo?",
                "Exportação Concluída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(new File(exportacao.getCaminhoArquivo()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        "Não foi possível abrir o arquivo: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Voltar para estado de sucesso
        viewModel.voltarParaRelatorio();
    }

    
    // ========== MÉTODOS DE PREENCHIMENTO ==========
    
    private void preencherTabela(List<ItemCompostoResumo> itens) {
        modelRelatorio.setRowCount(0);
        
        for (ItemCompostoResumo item : itens) {
            modelRelatorio.addRow(new Object[]{
                item.getIdPatrimonio(),
                item.getNumeroPatrimonio(),
                truncarTexto(item.getDescricaoPatrimonio(), 40),
                item.getNomeSala(),
                item.getNomeResponsavel(),
                item.getComponentesEsperados(),
                item.getComponentesEncontrados(),
                item.getComponentesFaltantes(),
                item.getTaxaIntegridadeFormatada(),
                item.getStatus().getDescricao(),
                item.getComponentesFaltantesFormatado()
            });
        }
    }
    
    private void atualizarEstatisticas(EstatisticasIntegridade stats) {
        if (stats == null) {
            lblTotalConjuntos.setText("0");
            lblConjuntosCompletos.setText("0");
            lblConjuntosIncompletos.setText("0");
            lblConjuntosParciais.setText("0");
            lblTaxaIntegridade.setText("0%");
            lblTaxaIntegridade.setForeground(new Color(52, 73, 94));
            return;
        }
        
        lblTotalConjuntos.setText(String.valueOf(stats.getTotalConjuntos()));
        lblConjuntosCompletos.setText(String.valueOf(stats.getConjuntosCompletos()));
        lblConjuntosIncompletos.setText(String.valueOf(stats.getConjuntosIncompletos()));
        lblConjuntosParciais.setText(String.valueOf(stats.getConjuntosParciais()));
        lblTaxaIntegridade.setText(stats.getTaxaFormatada());
        
        // Cor condicional para taxa
        if (stats.isAlerta()) {
            lblTaxaIntegridade.setForeground(new Color(231, 76, 60)); // Vermelho
        } else {
            lblTaxaIntegridade.setForeground(new Color(46, 204, 113)); // Verde
        }
    }
    
    private void preencherComboInventarios(List<Inventario> inventarios) {
        cmbInventario.removeAllItems();
        cmbInventario.addItem(new ComboItem<>("Selecione...", null));
        for (Inventario inv : inventarios) {
            cmbInventario.addItem(new ComboItem<>(inv.getNome(), inv));
        }
        
        // Selecionar inventário ativo se houver
        for (Inventario inv : inventarios) {
            if ("EM_ANDAMENTO".equals(inv.getStatusInventario())) {
                for (int i = 0; i < cmbInventario.getItemCount(); i++) {
                    ComboItem<Inventario> item = cmbInventario.getItemAt(i);
                    if (item.getValue() != null && item.getValue().getId().equals(inv.getId())) {
                        cmbInventario.setSelectedIndex(i);
                        viewModel.setInventarioAtivoId(inv.getId());
                        break;
                    }
                }
                break;
            }
        }
    }
    
    private void preencherComboSetores(List<Setor> setores) {
        cmbSetor.removeAllItems();
        cmbSetor.addItem(new ComboItem<>("Todos", null));
        for (Setor setor : setores) {
            cmbSetor.addItem(new ComboItem<>(setor.getNome(), setor));
        }
    }
    
    private void preencherComboSalas(List<Sala> salas) {
        cmbSala.removeAllItems();
        cmbSala.addItem(new ComboItem<>("Todas", null));
        for (Sala sala : salas) {
            String label = sala.getNumero() != null ? sala.getNumero() + " - " + sala.getDescricao() : sala.getDescricao();
            cmbSala.addItem(new ComboItem<>(label, sala));
        }
    }
    
    private void preencherComboResponsaveis(List<Responsavel> responsaveis) {
        cmbResponsavel.removeAllItems();
        cmbResponsavel.addItem(new ComboItem<>("Todos", null));
        for (Responsavel resp : responsaveis) {
            cmbResponsavel.addItem(new ComboItem<>(resp.getNome(), resp));
        }
    }
    
    private void abrirDialogoDetalhes(RelatorioItemCompostoState.DetalhesCarregados detalhes) {
        DetalheItemCompostoDialog dialog = new DetalheItemCompostoDialog(
                this,
                detalhes.getNumeroPatrimonio(),
                detalhes.getDescricaoPatrimonio(),
                detalhes.getNomeSala(),
                detalhes.getNomeResponsavel(),
                detalhes.getComponentes()
        );
        dialog.setVisible(true);
        
        // Voltar para estado de sucesso após fechar
        viewModel.voltarParaRelatorio();
    }

    
    // ========== MÉTODOS AUXILIARES ==========
    
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        if (texto.length() <= maxLength) return texto;
        return texto.substring(0, maxLength - 3) + "...";
    }
    
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
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
        button.setPreferredSize(new Dimension(120, 32));
        
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
    
    // ========== CLASSES INTERNAS ==========
    
    /**
     * Wrapper para itens de ComboBox com valor associado
     */
    private static class ComboItem<T> {
        private final String label;
        private final T value;
        
        public ComboItem(String label, T value) {
            this.label = label;
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    /**
     * Renderizador de células para colorir status
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String status = value.toString();
                switch (status) {
                    case "Completo":
                        c.setBackground(new Color(212, 239, 223));
                        c.setForeground(new Color(39, 174, 96));
                        break;
                    case "Incompleto":
                        c.setBackground(new Color(250, 219, 216));
                        c.setForeground(new Color(231, 76, 60));
                        break;
                    case "Parcial":
                        c.setBackground(new Color(252, 243, 207));
                        c.setForeground(new Color(241, 196, 15));
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                }
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(CENTER);
            return c;
        }
    }
}
