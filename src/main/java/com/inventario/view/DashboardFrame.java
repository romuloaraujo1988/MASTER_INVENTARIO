package com.inventario.view;

import com.inventario.chart.InventarioChartService;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Frame de Dashboard com gráficos estatísticos do inventário
 */
public class DashboardFrame extends JFrame {

    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;
    
    private final InventarioChartService chartService;
    private Integer idInventarioAtivo;

    public DashboardFrame() {
        this.chartService = new InventarioChartService();
        
        initComponents();
        setLocationRelativeTo(null);
    }

    public DashboardFrame(Integer idInventario) {
        this.chartService = new InventarioChartService();
        this.idInventarioAtivo = idInventario;
        
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Dashboard - Sistema de Inventário");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Definir ícone
        setIconImages(com.inventario.util.IconManager.getAppIconImages());

        // Painel principal com scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        mainPanel.add(createHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(20));

        // Grid de gráficos
        mainPanel.add(createChartsGrid());

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel titleLabel = new JLabel("📊 Dashboard de Estatísticas");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));

        JLabel subtitleLabel = new JLabel("Visão geral do inventário");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setBackground(BACKGROUND_COLOR);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(5));
        titleContainer.add(subtitleLabel);

        headerPanel.add(titleContainer, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createChartsGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        gridPanel.setBackground(BACKGROUND_COLOR);

        // Linha 1: Status e Setores
        gridPanel.add(createChartCard("Status dos Patrimônios", 
            chartService.createPatrimonioStatusChart()));
        gridPanel.add(createChartCard("Patrimônios por Setor", 
            chartService.createPatrimoniosPorSetorChart()));

        // Linha 2: Progresso e Evolução (se houver inventário ativo)
        if (idInventarioAtivo != null) {
            gridPanel.add(createChartCard("Progresso da Coleta", 
                chartService.createProgressoColetaChart(idInventarioAtivo)));
            gridPanel.add(createChartCard("Evolução das Coletas", 
                chartService.createEvolucaoColetasChart(idInventarioAtivo)));

            // Linha 3: Top 10 (ocupa 2 colunas)
            JPanel top10Panel = createChartCard("Top 10 Itens Mais Coletados", 
                chartService.createTop10DescricoesChart(idInventarioAtivo));
            gridPanel.add(top10Panel);
            
            // Painel vazio para manter o grid
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(BACKGROUND_COLOR);
            gridPanel.add(emptyPanel);
        }

        return gridPanel;
    }

    private JPanel createChartCard(String title, JFreeChart chart) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Título do card
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Painel do gráfico
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setBorder(null);

        cardPanel.add(titleLabel, BorderLayout.NORTH);
        cardPanel.add(chartPanel, BorderLayout.CENTER);

        return cardPanel;
    }

    /**
     * Atualiza os gráficos com novo inventário
     */
    public void setInventarioAtivo(Integer idInventario) {
        this.idInventarioAtivo = idInventario;
        refreshCharts();
    }

    /**
     * Recarrega todos os gráficos
     */
    public void refreshCharts() {
        // Remover conteúdo atual
        getContentPane().removeAll();
        
        // Recriar componentes
        initComponents();
        
        // Atualizar UI
        revalidate();
        repaint();
    }

    // Método main para teste standalone
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame(1);
            frame.setVisible(true);
        });
    }
}
