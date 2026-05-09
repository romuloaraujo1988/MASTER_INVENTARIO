package com.inventario.sihcp.view;

import com.inventario.sihcp.dto.FiltroHistoricoDTO;
import com.inventario.sihcp.dto.HistoricoColetaDTO;
import com.inventario.sihcp.dto.EstatisticasHistoricoDTO;
import com.inventario.sihcp.dto.FormatoExportacao;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.service.HistoricoColetaService;
import com.inventario.sihcp.ui.HistoricoTableModel;
import com.inventario.sihcp.ui.HistoricoTableCellRenderer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.inventario.sihcp.service.BusinessException;

/**
 * Painel principal para exibição do histórico de coletas de patrimônio.
 * 
 * Funcionalidades:
 * - Visualização de histórico em tabela
 * - Filtros por inventário, coletor e período
 * - Exportação para PDF e Excel
 * - Estatísticas do histórico
 * - Carregamento assíncrono
 * 
 * @author Sistema Inventário
 * @version 1.0
 */
public class HistoricoColetaPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // Services
    private final HistoricoColetaService historicoService;
    private final InventarioDAO inventarioDAO;
    
    // Dados
    private final Integer patrimonioId;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    
    // Componentes de filtro
    private JComboBox<ComboItem> comboInventario;
    private JComboBox<ComboItem> comboColetor;
    private JSpinner spinnerDataInicio;
    private JSpinner spinnerDataFim;
    private JButton btnAplicarFiltros;
    private JButton btnLimparFiltros;
    
    // Componentes de tabela
    private JTable tabelaHistorico;
    private HistoricoTableModel tableModel;
    private JScrollPane scrollPane;
    
    // Componentes de ações
    private JButton btnExportarPDF;
    private JButton btnExportarExcel;
    private JLabel lblEstatisticas;
    private JProgressBar progressBar;
    
    // Painel de cabeçalho
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    
    /**
     * Construtor
     * 
     * @param patrimonioId ID do patrimônio
     */
    public HistoricoColetaPanel(Integer patrimonioId) {
        this.patrimonioId = patrimonioId;
        this.historicoService = new HistoricoColetaService();
        this.inventarioDAO = new InventarioDAO();
        
        initComponents();
        setupLayout();
        carregarDadosIniciais();
    }
    
    /**
     * Construtor com dados do patrimônio
     * 
     * @param patrimonioId ID do patrimônio
     * @param numeroPatrimonio Número do patrimônio
     * @param descricaoPatrimonio Descrição do patrimônio
     */
    public HistoricoColetaPanel(Integer patrimonioId, String numeroPatrimonio, String descricaoPatrimonio) {
        this.patrimonioId = patrimonioId;
        this.numeroPatrimonio = numeroPatrimonio;
        this.descricaoPatrimonio = descricaoPatrimonio;
        this.historicoService = new HistoricoColetaService();
        this.inventarioDAO = new InventarioDAO();
        
        initComponents();
        setupLayout();
        carregarDadosIniciais();
    }
    
    /**
     * Inicializa os componentes
     */
    private void initComponents() {
        // Cabeçalho
        lblTitulo = new JLabel("Histórico de Coletas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 58, 64));
        
        lblSubtitulo = new JLabel();
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(108, 117, 125));
        atualizarSubtitulo();
        
        // Filtros
        comboInventario = new JComboBox<>();
        comboInventario.addItem(new ComboItem(null, "Todos os inventários"));
        
        comboColetor = new JComboBox<>();
        comboColetor.addItem(new ComboItem(null, "Todos os coletores"));
        
        // Spinners de data
        spinnerDataInicio = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerDataInicio, "dd/MM/yyyy");
        spinnerDataInicio.setEditor(editorInicio);
        spinnerDataInicio.setValue(null); // Sem filtro inicial
        
        spinnerDataFim = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFim = new JSpinner.DateEditor(spinnerDataFim, "dd/MM/yyyy");
        spinnerDataFim.setEditor(editorFim);
        spinnerDataFim.setValue(new Date());
        
        btnAplicarFiltros = new JButton("Aplicar Filtros");
        btnAplicarFiltros.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        btnAplicarFiltros.addActionListener(e -> aplicarFiltros());
        styleButton(btnAplicarFiltros, new Color(0, 123, 255));
        
        btnLimparFiltros = new JButton("Limpar");
        btnLimparFiltros.addActionListener(e -> limparFiltros());
        styleButton(btnLimparFiltros, new Color(108, 117, 125));
        
        // Tabela
        tableModel = new HistoricoTableModel();
        tabelaHistorico = new JTable(tableModel);
        tabelaHistorico.setDefaultRenderer(Object.class, new HistoricoTableCellRenderer());
        tabelaHistorico.setRowHeight(28);
        tabelaHistorico.setShowGrid(true);
        tabelaHistorico.setGridColor(new Color(222, 226, 230));
        tabelaHistorico.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabelaHistorico.getTableHeader().setBackground(new Color(248, 249, 250));
        tabelaHistorico.getTableHeader().setForeground(new Color(73, 80, 87));
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Configurar largura das colunas
        configurarLarguraColunas();
        
        scrollPane = new JScrollPane(tabelaHistorico);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        
        // Botões de ação
        btnExportarPDF = new JButton("Exportar PDF");
        btnExportarPDF.setIcon(UIManager.getIcon("FileView.fileIcon"));
        btnExportarPDF.addActionListener(e -> exportar(FormatoExportacao.PDF));
        styleButton(btnExportarPDF, new Color(220, 53, 69));
        
        btnExportarExcel = new JButton("Exportar Excel");
        btnExportarExcel.setIcon(UIManager.getIcon("FileView.computerIcon"));
        btnExportarExcel.addActionListener(e -> exportar(FormatoExportacao.EXCEL));
        styleButton(btnExportarExcel, new Color(40, 167, 69));
        
        lblEstatisticas = new JLabel("Carregando...");
        lblEstatisticas.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstatisticas.setForeground(new Color(108, 117, 125));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));
    }
    
    /**
     * Configura o layout do painel
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        
        // Painel superior: Cabeçalho
        add(criarPainelCabecalho(), BorderLayout.NORTH);
        
        // Painel central: Filtros + Tabela
        JPanel painelCentral = new JPanel(new BorderLayout(0, 10));
        painelCentral.setBackground(Color.WHITE);
        painelCentral.add(criarPainelFiltros(), BorderLayout.NORTH);
        painelCentral.add(scrollPane, BorderLayout.CENTER);
        add(painelCentral, BorderLayout.CENTER);
        
        // Painel inferior: Ações
        add(criarPainelAcoes(), BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel de cabeçalho
     */
    private JPanel criarPainelCabecalho() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        
        JPanel painelTitulos = new JPanel();
        painelTitulos.setLayout(new BoxLayout(painelTitulos, BoxLayout.Y_AXIS));
        painelTitulos.setBackground(Color.WHITE);
        painelTitulos.add(lblTitulo);
        painelTitulos.add(Box.createVerticalStrut(5));
        painelTitulos.add(lblSubtitulo);
        
        painel.add(painelTitulos, BorderLayout.WEST);
        
        return painel;
    }
    
    /**
     * Cria o painel de filtros
     */
    private JPanel criarPainelFiltros() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            "Filtros",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(73, 80, 87)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Linha 1: Inventário e Coletor
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        painel.add(new JLabel("Inventário:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        painel.add(comboInventario, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        painel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0;
        painel.add(new JLabel("Coletor:"), gbc);
        
        gbc.gridx = 4;
        gbc.weightx = 1;
        painel.add(comboColetor, gbc);
        
        // Linha 2: Período
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        painel.add(new JLabel("Data Início:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        painel.add(spinnerDataInicio, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        painel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0;
        painel.add(new JLabel("Data Fim:"), gbc);
        
        gbc.gridx = 4;
        gbc.weightx = 1;
        painel.add(spinnerDataFim, gbc);
        
        // Linha 3: Botões
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelBotoes.setBackground(Color.WHITE);
        painelBotoes.add(btnAplicarFiltros);
        painelBotoes.add(btnLimparFiltros);
        
        painel.add(painelBotoes, gbc);
        
        return painel;
    }
    
    /**
     * Cria o painel de ações
     */
    private JPanel criarPainelAcoes() {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(10, 0, 0, 0)
        ));
        
        // Painel esquerdo: Estatísticas
        JPanel painelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEsquerdo.setBackground(Color.WHITE);
        painelEsquerdo.add(lblEstatisticas);
        painelEsquerdo.add(Box.createHorizontalStrut(10));
        painelEsquerdo.add(progressBar);
        
        // Painel direito: Botões de exportação
        JPanel painelDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelDireito.setBackground(Color.WHITE);
        painelDireito.add(btnExportarPDF);
        painelDireito.add(btnExportarExcel);
        
        painel.add(painelEsquerdo, BorderLayout.WEST);
        painel.add(painelDireito, BorderLayout.EAST);
        
        return painel;
    }
    
    /**
     * Configura a largura das colunas
     */
    private void configurarLarguraColunas() {
        TableColumn column;
        
        // Data/Hora: 150px
        column = tabelaHistorico.getColumnModel().getColumn(HistoricoTableModel.COL_DATA);
        column.setPreferredWidth(150);
        column.setMinWidth(120);
        
        // Inventário: 150px
        column = tabelaHistorico.getColumnModel().getColumn(HistoricoTableModel.COL_INVENTARIO);
        column.setPreferredWidth(150);
        column.setMinWidth(100);
        
        // Coletor: 180px
        column = tabelaHistorico.getColumnModel().getColumn(HistoricoTableModel.COL_COLETOR);
        column.setPreferredWidth(180);
        column.setMinWidth(120);
        
        // Localização: 200px
        column = tabelaHistorico.getColumnModel().getColumn(HistoricoTableModel.COL_LOCALIZACAO);
        column.setPreferredWidth(200);
        column.setMinWidth(150);
        
        // Estado: 100px
        column = tabelaHistorico.getColumnModel().getColumn(HistoricoTableModel.COL_ESTADO);
        column.setPreferredWidth(100);
        column.setMinWidth(80);
        
        // Observações: 250px
        column = tabelaHistorico.getColumnModel().getColumn(HistoricoTableModel.COL_OBSERVACOES);
        column.setPreferredWidth(250);
        column.setMinWidth(150);
    }
    
    /**
     * Estiliza um botão
     */
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }
    
    /**
     * Atualiza o subtítulo com informações do patrimônio
     */
    private void atualizarSubtitulo() {
        if (numeroPatrimonio != null && descricaoPatrimonio != null) {
            lblSubtitulo.setText(String.format("Patrimônio: %s - %s", 
                numeroPatrimonio, descricaoPatrimonio));
        } else if (patrimonioId != null) {
            lblSubtitulo.setText(String.format("Patrimônio ID: %d", patrimonioId));
        } else {
            lblSubtitulo.setText("Patrimônio não identificado");
        }
    }
    
    /**
     * Carrega dados iniciais (inventários, coletores e histórico)
     */
    private void carregarDadosIniciais() {
        // Carregar em background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                carregarInventarios();
                carregarColetores();
                return null;
            }
            
            @Override
            protected void done() {
                // Após carregar combos, carregar histórico
                carregarHistorico();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Carrega inventários para o combo via InventarioDAO
     */
    private void carregarInventarios() {
        try {
            List<Inventario> inventarios = inventarioDAO.findAll();
            SwingUtilities.invokeLater(() -> {
                comboInventario.removeAllItems();
                comboInventario.addItem(new ComboItem(null, "Todos os inventários"));
                for (Inventario inv : inventarios) {
                    String label = inv.getNome();
                    if (inv.getAno() != null) {
                        label += " (" + inv.getAno() + ")";
                    }
                    comboInventario.addItem(new ComboItem(inv.getId(), label));
                }
            });
        } catch (Exception e) {
            // Falha silenciosa: o combo mantém apenas "Todos os inventários"
            // Logar sem interromper o carregamento do histórico
            System.err.println("Aviso: não foi possível carregar inventários para o filtro: " + e.getMessage());
        }
    }
    
    /**
     * Carrega coletores para o combo
     * Os coletores são extraídos do próprio histórico de coletas do patrimônio
     */
    private void carregarColetores() {
        // Combo de coletores permanece com "Todos os coletores" por padrão.
        // Futuramente pode ser populado a partir de HistoricoColetaDAO ou UsuarioDAO.
        SwingUtilities.invokeLater(() -> {
            comboColetor.removeAllItems();
            comboColetor.addItem(new ComboItem(null, "Todos os coletores"));
        });
    }
    
    /**
     * Carrega o histórico de coletas
     */
    private void carregarHistorico() {
        // Mostrar loading
        progressBar.setVisible(true);
        btnAplicarFiltros.setEnabled(false);
        btnExportarPDF.setEnabled(false);
        btnExportarExcel.setEnabled(false);
        lblEstatisticas.setText("Carregando histórico...");
        
        // Obter filtros atuais
        FiltroHistoricoDTO filtros = obterFiltrosAtuais();
        
        // Carregar em background
        SwingWorker<List<HistoricoColetaDTO>, Void> worker = 
            new SwingWorker<List<HistoricoColetaDTO>, Void>() {
                
                private EstatisticasHistoricoDTO estatisticas;
                
                @Override
                protected List<HistoricoColetaDTO> doInBackground() throws Exception {
                    // Buscar histórico
                    List<HistoricoColetaDTO> historico = 
                        historicoService.buscarHistorico(patrimonioId, filtros);
                    
                    // Buscar estatísticas
                    estatisticas = historicoService.buscarEstatisticas(patrimonioId);
                    
                    return historico;
                }
                
                @Override
                protected void done() {
                    try {
                        List<HistoricoColetaDTO> historico = get();
                        
                        // Atualizar tabela
                        tableModel.setData(historico);
                        
                        // Atualizar estatísticas
                        atualizarEstatisticas(estatisticas);
                        
                        // Habilitar botões
                        btnExportarPDF.setEnabled(!historico.isEmpty());
                        btnExportarExcel.setEnabled(!historico.isEmpty());
                        
                    } catch (InterruptedException | ExecutionException e) {
                        mostrarErro("Erro ao carregar histórico", e);
                        lblEstatisticas.setText("Erro ao carregar dados");
                    } finally {
                        progressBar.setVisible(false);
                        btnAplicarFiltros.setEnabled(true);
                    }
                }
            };
        
        worker.execute();
    }
    
    /**
     * Obtém os filtros atuais dos componentes
     */
    private FiltroHistoricoDTO obterFiltrosAtuais() {
        FiltroHistoricoDTO filtros = new FiltroHistoricoDTO();
        
        // Inventário
        ComboItem itemInventario = (ComboItem) comboInventario.getSelectedItem();
        if (itemInventario != null && itemInventario.getId() != null) {
            filtros.setInventarioId(itemInventario.getId());
        }
        
        // Coletor
        ComboItem itemColetor = (ComboItem) comboColetor.getSelectedItem();
        if (itemColetor != null && itemColetor.getId() != null) {
            filtros.setColetorId(itemColetor.getId());
        }
        
        // Data início
        Object valorInicio = spinnerDataInicio.getValue();
        if (valorInicio instanceof Date date) {
            filtros.setDataInicio(date);
        }
        
        // Data fim
        Object valorFim = spinnerDataFim.getValue();
        if (valorFim instanceof Date date) {
            filtros.setDataFim(date);
        }
        
        return filtros;
    }
    
    /**
     * Aplica os filtros e recarrega o histórico
     */
    private void aplicarFiltros() {
        // Validar datas
        Date dataInicio = null;
        Date dataFim = null;
        
        Object valorInicio = spinnerDataInicio.getValue();
        if (valorInicio instanceof Date date) {
            dataInicio = date;
        }
        
        Object valorFim = spinnerDataFim.getValue();
        if (valorFim instanceof Date date) {
            dataFim = date;
        }
        
        // Validar: data início <= data fim
        if (dataInicio != null && dataFim != null && dataInicio.after(dataFim)) {
            JOptionPane.showMessageDialog(
                this,
                "A data de início deve ser anterior ou igual à data de fim.",
                "Filtros Inválidos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Recarregar histórico
        carregarHistorico();
    }
    
    /**
     * Limpa os filtros e recarrega o histórico
     */
    private void limparFiltros() {
        comboInventario.setSelectedIndex(0);
        comboColetor.setSelectedIndex(0);
        spinnerDataInicio.setValue(null);
        spinnerDataFim.setValue(new Date());
        
        carregarHistorico();
    }
    
    /**
     * Atualiza as estatísticas exibidas
     */
    private void atualizarEstatisticas(EstatisticasHistoricoDTO stats) {
        if (stats == null) {
            lblEstatisticas.setText("Nenhuma estatística disponível");
            return;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total: %d coletas", stats.getTotalColetas()));
        
        if (stats.getPrimeiraColeta() != null && stats.getUltimaColeta() != null) {
            sb.append(String.format(" | Período: %s a %s",
                sdf.format(stats.getPrimeiraColeta()),
                sdf.format(stats.getUltimaColeta())
            ));
        }
        
        if (stats.getTotalMudancasLocalizacao() != null && stats.getTotalMudancasLocalizacao() > 0) {
            sb.append(String.format(" | Mudanças de local: %d", 
                stats.getTotalMudancasLocalizacao()));
        }
        
        if (stats.getTotalMudancasEstado() != null && stats.getTotalMudancasEstado() > 0) {
            sb.append(String.format(" | Mudanças de estado: %d", 
                stats.getTotalMudancasEstado()));
        }
        
        lblEstatisticas.setText(sb.toString());
    }
    
    /**
     * Exporta o histórico no formato especificado
     */
    private void exportar(FormatoExportacao formato) {
        // Verificar se há dados
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                this,
                "Não há dados para exportar.",
                "Exportação",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        // Escolher local para salvar
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório");
        
        String extensao = formato == FormatoExportacao.PDF ? ".pdf" : ".xlsx";
        String descricao = formato == FormatoExportacao.PDF ? "PDF" : "Excel";
        String nomeArquivo = String.format("historico_patrimonio_%d_%s%s",
            patrimonioId,
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
            extensao
        );
        
        fileChooser.setSelectedFile(new File(nomeArquivo));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            descricao + " (*" + extensao + ")", 
            extensao.substring(1)
        ));
        
        int resultado = fileChooser.showSaveDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            
            // Garantir extensão correta
            if (!arquivo.getName().toLowerCase().endsWith(extensao)) {
                arquivo = new File(arquivo.getAbsolutePath() + extensao);
            }
            
            // Exportar em background
            exportarArquivo(arquivo, formato);
        }
    }
    
    /**
     * Exporta o arquivo em background
     */
    private void exportarArquivo(File arquivo, FormatoExportacao formato) {
        // Mostrar loading
        progressBar.setVisible(true);
        btnExportarPDF.setEnabled(false);
        btnExportarExcel.setEnabled(false);
        lblEstatisticas.setText("Exportando...");
        
        // Obter filtros atuais
        FiltroHistoricoDTO filtros = obterFiltrosAtuais();
        
        SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                return historicoService.exportarHistorico(patrimonioId, formato, filtros);
            }
            
            @Override
            protected void done() {
                try {
                    byte[] dados = get();
                    
                    // Salvar arquivo
                    try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                        fos.write(dados);
                    }
                    
                    // Mostrar mensagem de sucesso
                    int opcao = JOptionPane.showConfirmDialog(
                        HistoricoColetaPanel.this,
                        String.format("Arquivo exportado com sucesso!\n\n%s\n\nDeseja abrir o arquivo?",
                            arquivo.getAbsolutePath()),
                        "Exportação Concluída",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Abrir arquivo se solicitado
                    if (opcao == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(arquivo);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(
                                HistoricoColetaPanel.this,
                                """
                                N\u00e3o foi poss\u00edvel abrir o arquivo automaticamente.
                                Abra manualmente: """ + arquivo.getAbsolutePath(),
                                "Aviso",
                                JOptionPane.WARNING_MESSAGE
                            );
                        }
                    }
                    
                    // Restaurar estatísticas
                    EstatisticasHistoricoDTO stats = historicoService.buscarEstatisticas(patrimonioId);
                    atualizarEstatisticas(stats);
                    
                } catch (BusinessException | HeadlessException | IOException | InterruptedException | ExecutionException e) {
                    mostrarErro("Erro ao exportar arquivo", e);
                    lblEstatisticas.setText("Erro na exportação");
                } finally {
                    progressBar.setVisible(false);
                    btnExportarPDF.setEnabled(true);
                    btnExportarExcel.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Mostra mensagem de erro
     */
    private void mostrarErro(String mensagem, Exception e) {
        String detalhes = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        
        JOptionPane.showMessageDialog(
            this,
            mensagem + "\n\nDetalhes: " + detalhes,
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Atualiza os dados do patrimônio
     */
    public void setDadosPatrimonio(String numeroPatrimonio, String descricaoPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
        this.descricaoPatrimonio = descricaoPatrimonio;
        atualizarSubtitulo();
    }
    
    /**
     * Recarrega o histórico (método público para uso externo)
     */
    public void recarregar() {
        carregarHistorico();
    }
    
    /**
     * Classe auxiliar para itens de combo
     */
    private static class ComboItem {
        private final Integer id;
        private final String descricao;
        
        public ComboItem(Integer id, String descricao) {
            this.id = id;
            this.descricao = descricao;
        }
        
        public Integer getId() {
            return id;
        }
        
        @Override
        public String toString() {
            return descricao;
        }
    }
}
