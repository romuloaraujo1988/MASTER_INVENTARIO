package com.inventario.analytics.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.inventario.analytics.model.DivergenciaAnalytics;
import com.inventario.analytics.model.DivergenciaTemporal;
import com.inventario.analytics.model.TipoDivergencia;
import com.inventario.analytics.service.AnalyticsService;

/**
 * Frame de análise detalhada de divergências.
 */
public class DivergenciasAnalyticsFrame extends JFrame {
        
    private final int idInventario;
    private final AnalyticsService analyticsService;
    
    // Componentes de filtro
    private JComboBox<String> comboTipo;
    private JComboBox<String> comboGravidade;
    
    // Tabela de divergências
    private JTable tabelaDivergencias;
    private DefaultTableModel modeloTabela;
    
    // Cards de resumo
    private JLabel labelTotalDivergencias;
    private JLabel labelLocalizacao;
    private JLabel labelEstado;
    private JLabel labelManual;
    
    // Dados
    private List<DivergenciaAnalytics> divergencias;
    
    public DivergenciasAnalyticsFrame(int idInventario) {
        this.idInventario = idInventario;
        this.analyticsService = new AnalyticsService();
        
        initComponents();
        carregarDados();
    }
    
    private void initComponents() {
        setTitle("Analytics - Análise de Divergências");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com cards de resumo
        mainPanel.add(criarPainelResumo(), BorderLayout.NORTH);
        
        // Painel central com tabela e gráfico
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(criarPainelTabela());
        splitPane.setRightComponent(criarPainelGrafico());
        splitPane.setDividerLocation(600);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Painel inferior com botões
        mainPanel.add(criarPainelBotoes(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel criarPainelResumo() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(new TitledBorder("Resumo de Divergências"));
        
        // Card Total
        JPanel cardTotal = criarCardResumo("Total", "0", new Color(108, 117, 125));
        labelTotalDivergencias = (JLabel) ((JPanel) cardTotal.getComponent(0)).getComponent(1);
        panel.add(cardTotal);
        
        // Card Localização
        JPanel cardLoc = criarCardResumo("Localização", "0", new Color(0, 123, 255));
        labelLocalizacao = (JLabel) ((JPanel) cardLoc.getComponent(0)).getComponent(1);
        panel.add(cardLoc);
        
        // Card Estado
        JPanel cardEstado = criarCardResumo("Estado", "0", new Color(255, 193, 7));
        labelEstado = (JLabel) ((JPanel) cardEstado.getComponent(0)).getComponent(1);
        panel.add(cardEstado);
        
        // Card Manual
        JPanel cardManual = criarCardResumo("Manual", "0", new Color(220, 53, 69));
        labelManual = (JLabel) ((JPanel) cardManual.getComponent(0)).getComponent(1);
        panel.add(cardManual);
        
        return panel;
    }
    
    private JPanel criarCardResumo(String titulo, String valor, Color cor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(cor, 2));
        
        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        conteudo.add(labelTitulo);
        
        JLabel labelValor = new JLabel(valor);
        labelValor.setFont(new Font("SansSerif", Font.BOLD, 24));
        labelValor.setForeground(cor);
        labelValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        conteudo.add(labelValor);
        
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Divergências"));
        
        // Filtros
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        painelFiltros.add(new JLabel("Tipo:"));
        comboTipo = new JComboBox<>(new String[]{"Todos", "LOCALIZACAO", "ESTADO", "MANUAL", "OUTRO"});
        comboTipo.addActionListener(e -> filtrarTabela());
        painelFiltros.add(comboTipo);
        
        painelFiltros.add(new JLabel("Gravidade:"));
        comboGravidade = new JComboBox<>(new String[]{"Todas", "CRITICA", "ALTA", "MEDIA", "BAIXA"});
        comboGravidade.addActionListener(e -> filtrarTabela());
        painelFiltros.add(comboGravidade);
        
        panel.add(painelFiltros, BorderLayout.NORTH);
        
        // Tabela
        String[] colunas = {"Patrimônio", "Descrição", "Tipo", "Gravidade", "Cadastrado", "Encontrado", "Setor", "Coletor"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaDivergencias = new JTable(modeloTabela);
        tabelaDivergencias.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaDivergencias.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tabelaDivergencias);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel criarPainelGrafico() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Evolução Temporal"));
        
        // Gráfico será criado quando os dados forem carregados
        return panel;
    }
    
    private JPanel criarPainelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnExportarCSV = new JButton("📥 Exportar CSV");
        btnExportarCSV.addActionListener(e -> exportarCSV());
        panel.add(btnExportarCSV);
        
        JButton btnExportarExcel = new JButton("📊 Exportar Excel");
        btnExportarExcel.addActionListener(e -> exportarExcel());
        panel.add(btnExportarExcel);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        panel.add(btnFechar);
        
        return panel;
    }
    
    private void carregarDados() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                divergencias = analyticsService.analisarDivergencias(idInventario);
                return null;
            }
            
            @Override
            protected void done() {
                atualizarResumo();
                preencherTabela();
                criarGraficoTemporal();
            }
        };
        worker.execute();
    }
    
    private void atualizarResumo() {
        Map<TipoDivergencia, Long> porTipo = analyticsService.agruparDivergenciasPorTipo(idInventario);
        
        int total = divergencias.size();
        long localizacao = porTipo.getOrDefault(TipoDivergencia.LOCALIZACAO, 0L);
        long estado = porTipo.getOrDefault(TipoDivergencia.ESTADO, 0L);
        long manual = porTipo.getOrDefault(TipoDivergencia.MANUAL, 0L);
        
        labelTotalDivergencias.setText(String.valueOf(total));
        labelLocalizacao.setText(String.valueOf(localizacao));
        labelEstado.setText(String.valueOf(estado));
        labelManual.setText(String.valueOf(manual));
    }
    
    private void preencherTabela() {
        modeloTabela.setRowCount(0);
        
        for (DivergenciaAnalytics div : divergencias) {
            modeloTabela.addRow(new Object[]{
                div.getNumeroPatrimonio(),
                div.getDescricaoPatrimonio(),
                div.getTipo(),
                div.getGravidade(),
                div.getValorCadastrado(),
                div.getValorEncontrado(),
                div.getNomeSetor(),
                div.getNomeColetor()
            });
        }
    }
    
    private void filtrarTabela() {
        String tipoSelecionado = (String) comboTipo.getSelectedItem();
        String gravidadeSelecionada = (String) comboGravidade.getSelectedItem();
        
        modeloTabela.setRowCount(0);
        
        for (DivergenciaAnalytics div : divergencias) {
            boolean passaTipo = "Todos".equals(tipoSelecionado) || 
                               div.getTipo().name().equals(tipoSelecionado);
            boolean passaGravidade = "Todas".equals(gravidadeSelecionada) || 
                                    div.getGravidade().name().equals(gravidadeSelecionada);
            
            if (passaTipo && passaGravidade) {
                modeloTabela.addRow(new Object[]{
                    div.getNumeroPatrimonio(),
                    div.getDescricaoPatrimonio(),
                    div.getTipo(),
                    div.getGravidade(),
                    div.getValorCadastrado(),
                    div.getValorEncontrado(),
                    div.getNomeSetor(),
                    div.getNomeColetor()
                });
            }
        }
    }
    
    private void criarGraficoTemporal() {
        List<DivergenciaTemporal> dadosTemporais = analyticsService.buscarDivergenciasPorDia(idInventario);
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        TimeSeries serieLocalizacao = new TimeSeries("Localização");
        TimeSeries serieEstado = new TimeSeries("Estado");
        TimeSeries serieManual = new TimeSeries("Manual");
        
        for (DivergenciaTemporal dt : dadosTemporais) {
            LocalDate data = dt.getData();
            Day day = new Day(data.getDayOfMonth(), data.getMonthValue(), data.getYear());
            
            switch (dt.getTipo()) {
                case LOCALIZACAO -> serieLocalizacao.addOrUpdate(day, dt.getQuantidade());
                case ESTADO -> serieEstado.addOrUpdate(day, dt.getQuantidade());
                case MANUAL -> serieManual.addOrUpdate(day, dt.getQuantidade());
                default -> { } // OUTRO type - no action needed
            }
        }
        
        dataset.addSeries(serieLocalizacao);
        dataset.addSeries(serieEstado);
        dataset.addSeries(serieManual);
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Divergências por Dia",
            "Data",
            "Quantidade",
            dataset,
            true,
            true,
            false
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 400));
        
        // Atualizar painel do gráfico
        JPanel painelGrafico = (JPanel) ((JSplitPane) getContentPane().getComponent(1)).getRightComponent();
        painelGrafico.removeAll();
        painelGrafico.add(chartPanel, BorderLayout.CENTER);
        painelGrafico.revalidate();
        painelGrafico.repaint();
    }
    
    private void exportarCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar CSV");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                analyticsService.exportarParaCSV(idInventario, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "CSV exportado com sucesso!");
            } catch (java.io.IOException | SecurityException e) {
                JOptionPane.showMessageDialog(this, "Erro ao exportar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportarExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Excel");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                analyticsService.exportarParaExcel(idInventario, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Excel exportado com sucesso!");
            } catch (java.io.IOException | SecurityException e) {
                JOptionPane.showMessageDialog(this, "Erro ao exportar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
