package com.inventario.analytics.view;

import com.inventario.analytics.model.AnalyticsKPIs;
import com.inventario.analytics.service.AnalyticsService;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Inventario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dashboard principal de Analytics.
 * 
 * Exibe KPIs principais e permite navegação para análises detalhadas.
 */
public class AnalyticsDashboardFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDashboardFrame.class);
    
    private final AnalyticsService analyticsService;
    private final InventarioDAO inventarioDAO;
    
    // Componentes de seleção
    private JComboBox<Inventario> comboInventario;
    
    // Cards de KPIs
    private KPICard cardTaxaDivergencia;
    private KPICard cardTempoMedio;
    private KPICard cardColetasPorHora;
    private KPICard cardColetoresAtivos;
    
    // Timer para atualização automática
    private Timer autoRefreshTimer;
    private static final int REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutos
    
    public AnalyticsDashboardFrame() {
        this.analyticsService = new AnalyticsService();
        this.inventarioDAO = new InventarioDAO();
        
        initComponents();
        carregarInventarios();
    }
    
    private void initComponents() {
        setTitle("Analytics - Dashboard de KPIs");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Painel superior com seleção de inventário
        mainPanel.add(criarPainelSuperior(), BorderLayout.NORTH);
        
        // Painel central com cards de KPIs
        mainPanel.add(criarPainelKPIs(), BorderLayout.CENTER);
        
        // Painel inferior com botões de navegação
        mainPanel.add(criarPainelNavegacao(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Configurar atualização automática
        configurarAutoRefresh();
    }
    
    private JPanel criarPainelSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        
        panel.add(new JLabel("Inventário:"));
        
        comboInventario = new JComboBox<>();
        comboInventario.setPreferredSize(new Dimension(300, 30));
        comboInventario.addActionListener(e -> atualizarKPIs());
        panel.add(comboInventario);
        
        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> atualizarKPIs());
        panel.add(btnAtualizar);
        
        JCheckBox chkAutoRefresh = new JCheckBox("Atualização automática (5 min)");
        chkAutoRefresh.setSelected(true);
        chkAutoRefresh.addActionListener(e -> {
            if (chkAutoRefresh.isSelected()) {
                autoRefreshTimer.start();
            } else {
                autoRefreshTimer.stop();
            }
        });
        panel.add(chkAutoRefresh);
        
        return panel;
    }

    private JPanel criarPainelKPIs() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Card 1: Taxa de Divergência
        cardTaxaDivergencia = new KPICard(
            "Taxa de Divergência",
            "0%",
            "Percentual de coletas com divergências",
            new Color(220, 53, 69), // Vermelho
            () -> abrirDivergenciasAnalytics()
        );
        panel.add(cardTaxaDivergencia);
        
        // Card 2: Tempo Médio de Coleta
        cardTempoMedio = new KPICard(
            "Tempo Médio de Coleta",
            "0s",
            "Tempo médio por coleta (segundos)",
            new Color(0, 123, 255), // Azul
            () -> abrirMetricasTempoAnalytics()
        );
        panel.add(cardTempoMedio);
        
        // Card 3: Coletas por Hora
        cardColetasPorHora = new KPICard(
            "Coletas por Hora",
            "0",
            "Média de coletas realizadas por hora",
            new Color(40, 167, 69), // Verde
            () -> abrirMetricasTempoAnalytics()
        );
        panel.add(cardColetasPorHora);
        
        // Card 4: Coletores Ativos
        cardColetoresAtivos = new KPICard(
            "Coletores Ativos",
            "0",
            "Número de coletores que realizaram coletas",
            new Color(255, 193, 7), // Amarelo
            () -> abrirMetricasTempoAnalytics()
        );
        panel.add(cardColetoresAtivos);
        
        return panel;
    }
    
    private JPanel criarPainelNavegacao() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);
        
        JButton btnDivergencias = new JButton("📊 Análise de Divergências");
        btnDivergencias.setPreferredSize(new Dimension(200, 40));
        btnDivergencias.addActionListener(e -> abrirDivergenciasAnalytics());
        panel.add(btnDivergencias);
        
        JButton btnMetricasTempo = new JButton("⏱️ Métricas de Tempo");
        btnMetricasTempo.setPreferredSize(new Dimension(200, 40));
        btnMetricasTempo.addActionListener(e -> abrirMetricasTempoAnalytics());
        panel.add(btnMetricasTempo);
        
        JButton btnExportar = new JButton("📥 Exportar Dados");
        btnExportar.setPreferredSize(new Dimension(200, 40));
        btnExportar.addActionListener(e -> exportarDados());
        panel.add(btnExportar);
        
        return panel;
    }
    
    private void configurarAutoRefresh() {
        autoRefreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> {
            logger.debug("Atualizando KPIs automaticamente...");
            atualizarKPIs();
        });
        autoRefreshTimer.start();
    }
    
    private void carregarInventarios() {
        try {
            List<Inventario> inventarios = inventarioDAO.findAll();
            comboInventario.removeAllItems();
            
            for (Inventario inv : inventarios) {
                comboInventario.addItem(inv);
            }
            
            // Selecionar inventário ativo se existir
            for (int i = 0; i < comboInventario.getItemCount(); i++) {
                Inventario inv = comboInventario.getItemAt(i);
                if (inv.isEmAndamento()) {
                    comboInventario.setSelectedIndex(i);
                    break;
                }
            }
            
            atualizarKPIs();
            
        } catch (Exception e) {
            logger.error("Erro ao carregar inventários: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar inventários: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarKPIs() {
        Inventario inventarioSelecionado = (Inventario) comboInventario.getSelectedItem();
        if (inventarioSelecionado == null) {
            return;
        }
        
        SwingWorker<AnalyticsKPIs, Void> worker = new SwingWorker<>() {
            @Override
            protected AnalyticsKPIs doInBackground() {
                return analyticsService.calcularKPIs(inventarioSelecionado.getId());
            }
            
            @Override
            protected void done() {
                try {
                    AnalyticsKPIs kpis = get();
                    atualizarCards(kpis);
                } catch (Exception e) {
                    logger.error("Erro ao atualizar KPIs: {}", e.getMessage(), e);
                }
            }
        };
        
        worker.execute();
    }
    
    private void atualizarCards(AnalyticsKPIs kpis) {
        // Taxa de Divergência
        String taxaFormatada = String.format("%.1f%%", kpis.getTaxaDivergenciaGeral());
        cardTaxaDivergencia.setValor(taxaFormatada);
        cardTaxaDivergencia.setVariacao(kpis.getVariacaoTaxaDivergencia(), true);
        
        // Tempo Médio
        double tempoMedio = kpis.getTempoMedioColeta();
        String tempoFormatado;
        if (tempoMedio < 60) {
            tempoFormatado = String.format("%.0fs", tempoMedio);
        } else {
            int minutos = (int) (tempoMedio / 60);
            int segundos = (int) (tempoMedio % 60);
            tempoFormatado = String.format("%dm %ds", minutos, segundos);
        }
        cardTempoMedio.setValor(tempoFormatado);
        cardTempoMedio.setVariacao(kpis.getVariacaoTempoMedio(), true);
        
        // Coletas por Hora
        cardColetasPorHora.setValor(String.format("%.1f", kpis.getColetasPorHora()));
        
        // Coletores Ativos
        cardColetoresAtivos.setValor(String.valueOf(kpis.getColetoresAtivos()));
    }
    
    private void abrirDivergenciasAnalytics() {
        Inventario inventarioSelecionado = (Inventario) comboInventario.getSelectedItem();
        if (inventarioSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário primeiro.");
            return;
        }
        
        DivergenciasAnalyticsFrame frame = new DivergenciasAnalyticsFrame(inventarioSelecionado.getId());
        frame.setVisible(true);
    }
    
    private void abrirMetricasTempoAnalytics() {
        Inventario inventarioSelecionado = (Inventario) comboInventario.getSelectedItem();
        if (inventarioSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário primeiro.");
            return;
        }
        
        MetricasTempoAnalyticsFrame frame = new MetricasTempoAnalyticsFrame(inventarioSelecionado.getId());
        frame.setVisible(true);
    }
    
    private void exportarDados() {
        Inventario inventarioSelecionado = (Inventario) comboInventario.getSelectedItem();
        if (inventarioSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário primeiro.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione o diretório para exportação");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String diretorio = fileChooser.getSelectedFile().getAbsolutePath();
            
            String[] opcoes = {"CSV", "Excel", "Ambos"};
            int escolha = JOptionPane.showOptionDialog(this,
                "Escolha o formato de exportação:",
                "Formato de Exportação",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[2]);
            
            if (escolha >= 0) {
                exportarEmBackground(inventarioSelecionado.getId(), diretorio, escolha);
            }
        }
    }
    
    private void exportarEmBackground(int idInventario, String diretorio, int formato) {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Exportando...");
        progressBar.setStringPainted(true);
        
        JDialog progressDialog = new JDialog(this, "Exportando", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 80);
        progressDialog.setLocationRelativeTo(this);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (formato == 0 || formato == 2) {
                    analyticsService.exportarParaCSV(idInventario, diretorio);
                }
                if (formato == 1 || formato == 2) {
                    analyticsService.exportarParaExcel(idInventario, diretorio);
                }
                return null;
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get();
                    JOptionPane.showMessageDialog(AnalyticsDashboardFrame.this,
                        "Exportação concluída com sucesso!\nArquivos salvos em: " + diretorio,
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    logger.error("Erro na exportação: {}", e.getMessage(), e);
                    JOptionPane.showMessageDialog(AnalyticsDashboardFrame.this,
                        "Erro na exportação: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    @Override
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        super.dispose();
    }
}
