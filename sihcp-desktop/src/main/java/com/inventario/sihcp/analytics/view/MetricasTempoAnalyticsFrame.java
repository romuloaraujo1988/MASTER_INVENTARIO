package com.inventario.sihcp.analytics.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.inventario.sihcp.analytics.model.MetricasColetorDTO;
import com.inventario.sihcp.analytics.model.MetricasPeriodoDTO;
import com.inventario.sihcp.analytics.service.AnalyticsService;

/**
 * Frame de análise de métricas de tempo de coleta.
 */
public class MetricasTempoAnalyticsFrame extends JFrame {
    
    //private static final Logger logger = LoggerFactory.getLogger(MetricasTempoAnalyticsFrame.class);
    
    private final int idInventario;
    private final AnalyticsService analyticsService;
    
    // Tabela de coletores
    private JTable tabelaColetores;
    private DefaultTableModel modeloTabela;
    
    // Labels de alerta
    private JLabel labelCobertura;
    
    public MetricasTempoAnalyticsFrame(int idInventario) {
        this.idInventario = idInventario;
        this.analyticsService = new AnalyticsService();
        
        initComponents();
        carregarDados();
    }
    
    private void initComponents() {
        setTitle("Analytics - Métricas de Tempo de Coleta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com alerta de cobertura
        mainPanel.add(criarPainelAlerta(), BorderLayout.NORTH);
        
        // Painel central com tabela e gráficos
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Ranking de Coletores", criarPainelColetores());
        tabbedPane.addTab("Por Período do Dia", criarPainelPeriodo());
        tabbedPane.addTab("Por Dia da Semana", criarPainelDiaSemana());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Painel inferior com botões
        mainPanel.add(criarPainelBotoes(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel criarPainelAlerta() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(255, 243, 205));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7)));
        
        JLabel iconAlerta = new JLabel("⚠️");
        iconAlerta.setFont(new Font("SansSerif", Font.PLAIN, 18));
        panel.add(iconAlerta);
        
        labelCobertura = new JLabel("Carregando cobertura de métricas...");
        labelCobertura.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(labelCobertura);
        
        return panel;
    }
    
    private JPanel criarPainelColetores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Tabela de coletores
        String[] colunas = {"Pos.", "Coletor", "Total", "Tempo Médio", "Mín", "Máx", "Desvio", "% Tempo"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaColetores = new JTable(modeloTabela);
        tabelaColetores.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaColetores.getTableHeader().setReorderingAllowed(false);
        
        // Renderer para destacar top 3 e bottom 3
        tabelaColetores.setDefaultRenderer(Object.class, new RankingCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(tabelaColetores);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel criarPainelPeriodo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Gráfico será adicionado quando os dados forem carregados
        return panel;
    }
    
    private JPanel criarPainelDiaSemana() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Gráfico será adicionado quando os dados forem carregados
        return panel;
    }
    
    private JPanel criarPainelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarDados());
        panel.add(btnAtualizar);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        panel.add(btnFechar);
        
        return panel;
    }
    
    private void carregarDados() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<MetricasColetorDTO> coletores;
            private Map<String, MetricasPeriodoDTO> periodos;
            private Map<String, MetricasPeriodoDTO> diasSemana;
            
            @Override
            protected Void doInBackground() {
                coletores = analyticsService.calcularMetricasPorColetor(idInventario);
                periodos = analyticsService.calcularMetricasPorPeriodo(idInventario);
                diasSemana = analyticsService.calcularMetricasPorDiaSemana(idInventario);
                return null;
            }
            
            @Override
            protected void done() {
                preencherTabelaColetores(coletores);
                criarGraficoPeriodo(periodos);
                criarGraficoDiaSemana(diasSemana);
                atualizarAlertaCobertura(coletores);
            }
        };
        worker.execute();
    }
    
    private void preencherTabelaColetores(List<MetricasColetorDTO> coletores) {
        modeloTabela.setRowCount(0);
        
        for (MetricasColetorDTO dto : coletores) {
            String tempoMedio = formatarTempo(dto.getTempoMedio());
            String tempoMin = formatarTempo(dto.getTempoMinimo());
            String tempoMax = formatarTempo(dto.getTempoMaximo());
            
            modeloTabela.addRow(new Object[]{
                dto.getPosicaoRanking(),
                dto.getNomeColetor(),
                dto.getTotalColetas(),
                tempoMedio,
                tempoMin,
                tempoMax,
                String.format("%.1f", dto.getDesvioPadrao()),
                String.format("%.1f%%", dto.getPercentualComTempo())
            });
        }
    }
    
    private String formatarTempo(double segundos) {
        if (segundos < 60) {
            return String.format("%.0fs", segundos);
        } else {
            int min = (int) (segundos / 60);
            int seg = (int) (segundos % 60);
            return String.format("%dm %ds", min, seg);
        }
    }

    private void criarGraficoPeriodo(Map<String, MetricasPeriodoDTO> periodos) {
        DefaultCategoryDataset datasetColetas = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetTempo = new DefaultCategoryDataset();
        
        for (MetricasPeriodoDTO dto : periodos.values()) {
            String periodo = dto.getPeriodo();
            datasetColetas.addValue(dto.getTotalColetas(), "Coletas", periodo);
            datasetTempo.addValue(dto.getTempoMedio(), "Tempo Médio (s)", periodo);
        }
        
        JFreeChart chartColetas = ChartFactory.createBarChart(
            "Coletas por Período",
            "Período",
            "Quantidade",
            datasetColetas,
            PlotOrientation.VERTICAL,
            true, true, false
        );
        
        JFreeChart chartTempo = ChartFactory.createBarChart(
            "Tempo Médio por Período",
            "Período",
            "Segundos",
            datasetTempo,
            PlotOrientation.VERTICAL,
            true, true, false
        );
        
        // Destacar períodos de baixa produtividade
        CategoryPlot plot = chartTempo.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        
        for (MetricasPeriodoDTO dto : periodos.values()) {
            if (dto.isBaixaProdutividade()) {
                renderer.setSeriesPaint(0, new Color(220, 53, 69));
            }
        }
        
        // Adicionar gráficos ao painel
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(1);
        JPanel painelPeriodo = (JPanel) tabbedPane.getComponentAt(1);
        painelPeriodo.removeAll();
        
        JPanel graficos = new JPanel(new GridLayout(1, 2, 10, 0));
        graficos.add(new ChartPanel(chartColetas));
        graficos.add(new ChartPanel(chartTempo));
        painelPeriodo.add(graficos, BorderLayout.CENTER);
        
        painelPeriodo.revalidate();
        painelPeriodo.repaint();
    }
    
    private void criarGraficoDiaSemana(Map<String, MetricasPeriodoDTO> diasSemana) {
        DefaultCategoryDataset datasetColetas = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetTempo = new DefaultCategoryDataset();
        
        // Ordenar dias da semana
        String[] ordemDias = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};
        
        for (String dia : ordemDias) {
            MetricasPeriodoDTO dto = diasSemana.get(dia);
            if (dto != null) {
                datasetColetas.addValue(dto.getTotalColetas(), "Coletas", dia);
                datasetTempo.addValue(dto.getTempoMedio(), "Tempo Médio (s)", dia);
            }
        }
        
        JFreeChart chartColetas = ChartFactory.createBarChart(
            "Coletas por Dia da Semana",
            "Dia",
            "Quantidade",
            datasetColetas,
            PlotOrientation.VERTICAL,
            true, true, false
        );
        
        JFreeChart chartTempo = ChartFactory.createBarChart(
            "Tempo Médio por Dia da Semana",
            "Dia",
            "Segundos",
            datasetTempo,
            PlotOrientation.VERTICAL,
            true, true, false
        );
        
        // Adicionar gráficos ao painel
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(1);
        JPanel painelDiaSemana = (JPanel) tabbedPane.getComponentAt(2);
        painelDiaSemana.removeAll();
        
        JPanel graficos = new JPanel(new GridLayout(1, 2, 10, 0));
        graficos.add(new ChartPanel(chartColetas));
        graficos.add(new ChartPanel(chartTempo));
        painelDiaSemana.add(graficos, BorderLayout.CENTER);
        
        painelDiaSemana.revalidate();
        painelDiaSemana.repaint();
    }
    
    private void atualizarAlertaCobertura(List<MetricasColetorDTO> coletores) {
        if (coletores.isEmpty()) {
            labelCobertura.setText("Nenhum dado de coleta disponível.");
            return;
        }
        
        int totalColetas = coletores.stream().mapToInt(MetricasColetorDTO::getTotalColetas).sum();
        int coletasComTempo = coletores.stream().mapToInt(MetricasColetorDTO::getColetasComTempo).sum();
        
        double percentual = totalColetas > 0 ? (coletasComTempo * 100.0 / totalColetas) : 0;
        
        String mensagem = String.format("Cobertura de métricas: %.1f%% das coletas têm tempo registrado (%d de %d)", 
                percentual, coletasComTempo, totalColetas);
        
        if (percentual < 50) {
            mensagem += " - ATENÇÃO: Cobertura baixa!";
            labelCobertura.getParent().setBackground(new Color(248, 215, 218));
            ((JPanel) labelCobertura.getParent()).setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69)));
        } else {
            labelCobertura.getParent().setBackground(new Color(212, 237, 218));
            ((JPanel) labelCobertura.getParent()).setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69)));
        }
        
        labelCobertura.setText(mensagem);
    }
    
    /**
     * Renderer para destacar top 3 (verde) e bottom 3 (vermelho) no ranking.
     */
    private class RankingCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                int totalRows = table.getRowCount();
                
                if (row < 3) {
                    // Top 3 - Verde
                    c.setBackground(new Color(212, 237, 218));
                } else if (row >= totalRows - 3 && totalRows > 6) {
                    // Bottom 3 - Vermelho
                    c.setBackground(new Color(248, 215, 218));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            
            return c;
        }
    }
}
