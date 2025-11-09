package com.inventario.view;

import com.inventario.model.Inventario;
import com.inventario.service.DashboardService;
import com.inventario.view.ui.ButtonStyleFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Dashboard gráfico em tempo real para monitoramento da coleta de inventário
 * Versão melhorada com interface em abas e gráficos mais interativos
 */
public class DashboardColetaFrame extends JFrame {
    
    private final DashboardService dashboardService;
    private Timer timer;
    private int idInventarioAtivo = -1;
    private String nomeInventarioAtivo = "Nenhum inventário ativo";
    
    // Componentes principais
    private JTabbedPane tabbedPane;
    
    // Aba Visão Geral
    private ChartPanel painelGraficoStatus;
    private ChartPanel painelGraficoEtiquetas;
    private JLabel labelTotalPatrimonios;
    private JLabel labelItensColetados;
    private JLabel labelItensNaoEncontrados;
    private JLabel labelItensSemEtiqueta;
    private JLabel labelPercentualConcluido;
    private JProgressBar barraProgresso;
    
    // Aba Análise por Responsável
    private ChartPanel painelGraficoResponsaveis;
    private ChartPanel painelGraficoResponsaveisDetalhado;
    
    // Aba Análise por Setor
    private ChartPanel painelGraficoSetores;
    private ChartPanel painelGraficoSetoresDetalhado;
    
    // Aba Análise de Usuários
    private ChartPanel painelGraficoUsuarios;
    private ChartPanel painelGraficoDesempenhoUsuarios;
    private JComboBox<String> comboUsuarios;
    
    public DashboardColetaFrame() {
        // Instantiate service directly (no Spring context in Swing app)
        this.dashboardService = new DashboardService();
        obterInventarioAtivo();
        initializeComponents();
        aplicarTemaModerno();
        iniciarAtualizacaoAutomatica();
        atualizarDados();
    }
    
    private void initializeComponents() {
        setTitle("📊 Dashboard de Coleta - " + nomeInventarioAtivo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        
        // Criar painel de abas
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Adicionar abas
        tabbedPane.addTab("📈 Visão Geral", criarAbaVisaoGeral());
        tabbedPane.addTab("📊 Gráficos", criarAbaGraficos());
        tabbedPane.addTab("👥 Por Responsável", criarAbaResponsaveis());
        tabbedPane.addTab("🏢 Por Setor", criarAbaSetores());
        tabbedPane.addTab("👤 Usuários", criarAbaUsuarios());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Painel inferior com controles
        add(criarPainelControles(), BorderLayout.SOUTH);
    }
    
    private void aplicarTemaModerno() {
        // Cores modernas
        Color corFundo = new Color(248, 249, 250);
        Color corPrimaria = new Color(52, 152, 219);
        
        getContentPane().setBackground(corFundo);
        tabbedPane.setBackground(corFundo);
    }
    
    private JPanel criarAbaVisaoGeral() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBorder(new EmptyBorder(15, 15, 15, 15));
        aba.setBackground(new Color(248, 249, 250));
        
        // Painel com cards de informações
        JPanel painelCards = criarPainelCards();
        aba.add(painelCards, BorderLayout.CENTER);
        
        return aba;
    }
    
    private JPanel criarAbaGraficos() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBorder(new EmptyBorder(15, 15, 15, 15));
        aba.setBackground(new Color(248, 249, 250));
        
        // Título da aba
        JLabel titulo = new JLabel("📊 Gráficos Principais");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        aba.add(titulo, BorderLayout.NORTH);
        
        // Painel central com gráficos principais
        JPanel painelGraficos = new JPanel(new GridLayout(1, 2, 15, 15));
        painelGraficos.setBackground(new Color(248, 249, 250));
        
        // Gráfico de status da coleta
        painelGraficoStatus = new ChartPanel(criarGraficoPizzaStatus());
        painelGraficoStatus.setBorder(criarBordaModerna("📊 Status da Coleta"));
        painelGraficoStatus.setPreferredSize(new Dimension(500, 400));
        
        // Gráfico de etiquetas
        painelGraficoEtiquetas = new ChartPanel(criarGraficoPizzaEtiquetas());
        painelGraficoEtiquetas.setBorder(criarBordaModerna("🏷️ Situação das Etiquetas"));
        painelGraficoEtiquetas.setPreferredSize(new Dimension(500, 400));
        
        painelGraficos.add(painelGraficoStatus);
        painelGraficos.add(painelGraficoEtiquetas);
        
        aba.add(painelGraficos, BorderLayout.CENTER);
        
        return aba;
    }
    
    private JPanel criarAbaResponsaveis() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBorder(new EmptyBorder(15, 15, 15, 15));
        aba.setBackground(new Color(248, 249, 250));
        
        // Título da aba
        JLabel titulo = new JLabel("👥 Análise Detalhada por Responsável");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        aba.add(titulo, BorderLayout.NORTH);
        
        // Painel com gráficos de responsáveis
        JPanel painelGraficos = new JPanel(new GridLayout(2, 1, 10, 10));
        painelGraficos.setBackground(new Color(248, 249, 250));
        
        // Gráfico de barras - Progresso por responsável
        painelGraficoResponsaveis = new ChartPanel(criarGraficoBarrasResponsaveis());
        painelGraficoResponsaveis.setBorder(criarBordaModerna("📊 Progresso por Responsável"));
        painelGraficoResponsaveis.setPreferredSize(new Dimension(800, 300));
        
        // Gráfico detalhado - Comparativo
        painelGraficoResponsaveisDetalhado = new ChartPanel(criarGraficoComparativoResponsaveis());
        painelGraficoResponsaveisDetalhado.setBorder(criarBordaModerna("📈 Comparativo Detalhado"));
        painelGraficoResponsaveisDetalhado.setPreferredSize(new Dimension(800, 300));
        
        painelGraficos.add(painelGraficoResponsaveis);
        painelGraficos.add(painelGraficoResponsaveisDetalhado);
        
        aba.add(painelGraficos, BorderLayout.CENTER);
        
        return aba;
    }
    
    private JPanel criarAbaSetores() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBorder(new EmptyBorder(15, 15, 15, 15));
        aba.setBackground(new Color(248, 249, 250));
        
        // Título da aba
        JLabel titulo = new JLabel("🏢 Análise Detalhada por Setor");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        aba.add(titulo, BorderLayout.NORTH);
        
        // Painel com gráficos de setores
        JPanel painelGraficos = new JPanel(new GridLayout(2, 1, 10, 10));
        painelGraficos.setBackground(new Color(248, 249, 250));
        
        // Gráfico de barras - Progresso por setor
        painelGraficoSetores = new ChartPanel(criarGraficoBarrasSetores());
        painelGraficoSetores.setBorder(criarBordaModerna("📊 Progresso por Setor (%)"));
        painelGraficoSetores.setPreferredSize(new Dimension(800, 300));
        
        // Gráfico detalhado - Distribuição
        painelGraficoSetoresDetalhado = new ChartPanel(criarGraficoDistribuicaoSetores());
        painelGraficoSetoresDetalhado.setBorder(criarBordaModerna("📈 Distribuição de Patrimônios"));
        painelGraficoSetoresDetalhado.setPreferredSize(new Dimension(800, 300));
        
        painelGraficos.add(painelGraficoSetores);
        painelGraficos.add(painelGraficoSetoresDetalhado);
        
        aba.add(painelGraficos, BorderLayout.CENTER);
        
        return aba;
    }
    
    private JPanel criarAbaUsuarios() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBorder(new EmptyBorder(15, 15, 15, 15));
        aba.setBackground(new Color(248, 249, 250));
        
        // Painel superior com título e controles
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(new Color(248, 249, 250));
        
        JLabel titulo = new JLabel("👤 Análise de Desempenho dos Usuários");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Painel de controles
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelControles.setBackground(new Color(248, 249, 250));
        
        JLabel labelUsuario = new JLabel("Selecionar Usuário:");
        labelUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelUsuario.setForeground(new Color(52, 73, 94));
        
        comboUsuarios = new JComboBox<>();
        comboUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboUsuarios.setPreferredSize(new Dimension(200, 25));
        comboUsuarios.addActionListener(e -> atualizarGraficosUsuarios());
        
        JButton btnTodosUsuarios = ButtonStyleFactory.createPrimaryButton("📊 Todos os Usuários");
        btnTodosUsuarios.addActionListener(e -> {
            comboUsuarios.setSelectedIndex(0);
            atualizarGraficosUsuarios();
        });
        
        painelControles.add(labelUsuario);
        painelControles.add(Box.createHorizontalStrut(10));
        painelControles.add(comboUsuarios);
        painelControles.add(Box.createHorizontalStrut(15));
        painelControles.add(btnTodosUsuarios);
        
        painelSuperior.add(titulo, BorderLayout.NORTH);
        painelSuperior.add(painelControles, BorderLayout.SOUTH);
        
        aba.add(painelSuperior, BorderLayout.NORTH);
        
        // Painel com gráficos de usuários
        JPanel painelGraficos = new JPanel(new GridLayout(2, 1, 10, 10));
        painelGraficos.setBackground(new Color(248, 249, 250));
        
        // Gráfico de barras - Desempenho por usuário
        painelGraficoUsuarios = new ChartPanel(criarGraficoBarrasUsuarios());
        painelGraficoUsuarios.setBorder(criarBordaModerna("📊 Itens Coletados por Usuário"));
        painelGraficoUsuarios.setPreferredSize(new Dimension(800, 300));
        
        // Gráfico detalhado - Desempenho individual
        painelGraficoDesempenhoUsuarios = new ChartPanel(criarGraficoDesempenhoUsuarios());
        painelGraficoDesempenhoUsuarios.setBorder(criarBordaModerna("📈 Desempenho Detalhado"));
        painelGraficoDesempenhoUsuarios.setPreferredSize(new Dimension(800, 300));
        
        painelGraficos.add(painelGraficoUsuarios);
        painelGraficos.add(painelGraficoDesempenhoUsuarios);
        
        aba.add(painelGraficos, BorderLayout.CENTER);
        
        return aba;
    }
    
    private JPanel criarPainelCards() {
        JPanel painel = new JPanel(new GridLayout(2, 3, 15, 15));
        painel.setBackground(new Color(248, 249, 250));
        painel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Cards modernos com informações
        labelTotalPatrimonios = criarCardModerno("📦 Total de Patrimônios", "0", new Color(52, 152, 219));
        labelItensColetados = criarCardModerno("✅ Itens Coletados", "0", new Color(46, 204, 113));
        labelItensNaoEncontrados = criarCardModerno("❌ Não Inventariados", "0", new Color(231, 76, 60));
        labelItensSemEtiqueta = criarCardModerno("📦 Sem Patrimônio", "0", new Color(241, 196, 15));
        labelPercentualConcluido = criarCardModerno("📊 % Concluído", "0%", new Color(155, 89, 182));
        
        // Barra de progresso moderna
        JPanel painelProgresso = criarPainelProgressoModerno();
        
        painel.add(labelTotalPatrimonios);
        painel.add(labelItensColetados);
        painel.add(labelItensNaoEncontrados);
        painel.add(labelItensSemEtiqueta);
        painel.add(labelPercentualConcluido);
        painel.add(painelProgresso);
        
        return painel;
    }
    
    private JLabel criarCardModerno(String titulo, String valor, Color cor) {
        JLabel card = new JLabel();
        card.setText(String.format(
            "<html><div style='text-align: center; padding: 20px; font-family: Segoe UI;'>" +
            "<div style='color: rgb(%d,%d,%d); font-size: 14px; font-weight: bold; margin-bottom: 10px;'>%s</div>" +
            "<div style='font-size: 28px; font-weight: bold; color: rgb(52,73,94);'>%s</div>" +
            "</div></html>",
            cor.getRed(), cor.getGreen(), cor.getBlue(), titulo, valor
        ));
        
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Adicionar sombra sutil
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 20)),
            card.getBorder()
        ));
        
        return card;
    }
    
    private JPanel criarPainelProgressoModerno() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 20)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            )
        ));
        
        JLabel titulo = new JLabel("🎯 Progresso Geral");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(new Color(52, 152, 219));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("0% Concluído");
        barraProgresso.setFont(new Font("Segoe UI", Font.BOLD, 12));
        barraProgresso.setForeground(new Color(46, 204, 113));
        barraProgresso.setBackground(new Color(236, 240, 241));
        barraProgresso.setPreferredSize(new Dimension(200, 25));
        
        painel.add(titulo, BorderLayout.NORTH);
        painel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        painel.add(barraProgresso, BorderLayout.SOUTH);
        
        return painel;
    }
    
    private javax.swing.border.Border criarBordaModerna(String titulo) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 20)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
                    titulo,
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 14),
                    new Color(52, 73, 94)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
        );
    }
    
    private JPanel criarPainelControles() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painel.setBackground(new Color(52, 73, 94));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Botões modernos
        JButton btnAtualizar = ButtonStyleFactory.createPrimaryButton("🔄 Atualizar Agora");
        btnAtualizar.addActionListener(e -> atualizarDados());
        
        JButton btnFechar = ButtonStyleFactory.createDangerButton("❌ Fechar Dashboard");
        btnFechar.addActionListener(e -> {
            pararAtualizacaoAutomatica();
            dispose();
        });
        
        JLabel labelAtualizacao = new JLabel("⏱️ Atualização automática a cada 30 segundos");
        labelAtualizacao.setForeground(Color.WHITE);
        labelAtualizacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        painel.add(btnAtualizar);
        painel.add(Box.createHorizontalStrut(20));
        painel.add(labelAtualizacao);
        painel.add(Box.createHorizontalStrut(20));
        painel.add(btnFechar);
        
        return painel;
    }
    
    // Métodos para criar gráficos modernos e interativos
    
    private JFreeChart criarGraficoPizzaStatus() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Coletados", 0);
        dataset.setValue("Não Inventariados", 0);
        dataset.setValue("Não Encontrados", 0);
        
        JFreeChart chart = ChartFactory.createPieChart(
            null, // Sem título (já está na borda)
            dataset,
            true,
            true,
            false
        );
        
        // Estilização moderna
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Coletados", new Color(46, 204, 113));
        plot.setSectionPaint("Não Inventariados", new Color(231, 76, 60));
        plot.setSectionPaint("Não Encontrados", new Color(149, 165, 166));
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        
        return chart;
    }
    
    private JFreeChart criarGraficoPizzaEtiquetas() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Com Etiqueta", 0);
        dataset.setValue("Sem Etiqueta", 0);
        
        JFreeChart chart = ChartFactory.createPieChart(
            null,
            dataset,
            true,
            true,
            false
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Com Etiqueta", new Color(46, 204, 113));
        plot.setSectionPaint("Sem Etiqueta", new Color(241, 196, 15));
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        
        return chart;
    }
    
    private JFreeChart criarGraficoBarrasResponsaveis() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Responsável",
            "Quantidade",
            dataset
        );
        
        estilizarGraficoBarras(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219));
        renderer.setSeriesPaint(1, new Color(46, 204, 113));
        
        return chart;
    }
    
    private JFreeChart criarGraficoComparativoResponsaveis() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Responsável",
            "Percentual (%)",
            dataset
        );
        
        estilizarGraficoBarras(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(155, 89, 182));
        
        return chart;
    }
    
    private JFreeChart criarGraficoBarrasSetores() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Setor",
            "Percentual (%)",
            dataset
        );
        
        estilizarGraficoBarras(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(155, 89, 182));
        
        return chart;
    }
    
    private JFreeChart criarGraficoDistribuicaoSetores() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Setor",
            "Quantidade",
            dataset
        );
        
        estilizarGraficoBarras(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219));
        renderer.setSeriesPaint(1, new Color(46, 204, 113));
        
        return chart;
    }
    
    private JFreeChart criarGraficoBarrasUsuarios() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try {
            if (idInventarioAtivo != -1) {
                // TODO: Implementar obterEstatisticasColetores no DashboardService
                Map<String, Integer> estatisticasUsuarios = new java.util.HashMap<>();
                
                for (Map.Entry<String, Integer> entry : estatisticasUsuarios.entrySet()) {
                    dataset.addValue(entry.getValue(), "Itens Coletados", entry.getKey());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados dos usuários: " + e.getMessage());
            // Dados de fallback
            dataset.addValue(0, "Itens Coletados", "Sem dados");
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Usuário",
            "Itens Coletados",
            dataset
        );
        
        estilizarGraficoBarras(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219));
        renderer.setSeriesPaint(1, new Color(46, 204, 113));
        renderer.setSeriesPaint(2, new Color(231, 76, 60));
        
        return chart;
    }
    
    private JFreeChart criarGraficoDesempenhoUsuarios() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try {
            if (idInventarioAtivo != -1) {
                // TODO: Implementar obterDesempenhoColetoresPorPeriodo no DashboardService
                Map<String, Map<String, Integer>> desempenhoDetalhado = new java.util.HashMap<>();
                
                for (Map.Entry<String, Map<String, Integer>> usuarioEntry : desempenhoDetalhado.entrySet()) {
                    String usuario = usuarioEntry.getKey();
                    Map<String, Integer> dadosPorData = usuarioEntry.getValue();
                    
                    for (Map.Entry<String, Integer> dataEntry : dadosPorData.entrySet()) {
                        String data = dataEntry.getKey();
                        Integer quantidade = dataEntry.getValue();
                        dataset.addValue(quantidade, usuario, data);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar desempenho dos usuários: " + e.getMessage());
            // Dados de fallback
            dataset.addValue(0, "Sem dados", "Hoje");
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Período",
            "Itens por Dia",
            dataset
        );
        
        estilizarGraficoBarras(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        
        // Cores diferentes para cada usuário
        Color[] cores = {
            new Color(155, 89, 182),
            new Color(241, 196, 15),
            new Color(52, 152, 219),
            new Color(231, 76, 60),
            new Color(46, 204, 113)
        };
        
        for (int i = 0; i < dataset.getRowCount() && i < cores.length; i++) {
            renderer.setSeriesPaint(i, cores[i]);
        }
        
        return chart;
    }
    
    private void estilizarGraficoBarras(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(220, 221, 225));
        plot.setOutlineVisible(false);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.1);
        
        // Fontes modernas
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
    }
    
    // Métodos de atualização de dados
    
    private void atualizarDados() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (idInventarioAtivo == -1) {
                    obterInventarioAtivo();
                    if (idInventarioAtivo == -1) {
                        return;
                    }
                }
                
                atualizarTitulosInterface();
                
                Map<String, Object> estatisticasObj = dashboardService.obterEstatisticasGerais(idInventarioAtivo);
                Map<String, Integer> estatisticas = converterMapaEstatisticas(estatisticasObj);
                atualizarInformacoesGerais(estatisticas);
                atualizarGraficos(estatisticas);
                atualizarUsuarios();
                
            } catch (Exception e) {
                System.err.println("Erro ao atualizar dashboard: " + e.getMessage());
            }
        });
    }
    
    private void atualizarUsuarios() {
        try {
            // Atualizar lista de usuários no combobox
            // TODO: Implementar obterEstatisticasColetores no DashboardService
            Map<String, Integer> usuarios = new java.util.HashMap<>();
            
            comboUsuarios.removeAllItems();
            comboUsuarios.addItem("Todos os Usuários");
            
            for (String usuario : usuarios.keySet()) {
                comboUsuarios.addItem(usuario);
            }
            
            atualizarGraficosUsuarios();
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados dos usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void atualizarGraficosUsuarios() {
        try {
            String usuarioSelecionado = (String) comboUsuarios.getSelectedItem();
            
            // Atualizar gráfico de barras dos usuários
            DefaultCategoryDataset datasetUsuarios = new DefaultCategoryDataset();
            // TODO: Implementar obterEstatisticasColetores no DashboardService
            Map<String, Integer> estatisticasUsuarios = new java.util.HashMap<>();
            
            if ("Todos os Usuários".equals(usuarioSelecionado)) {
                // Mostrar todos os usuários
                for (Map.Entry<String, Integer> entry : estatisticasUsuarios.entrySet()) {
                    datasetUsuarios.addValue(entry.getValue(), "Coletados", entry.getKey());
                }
            } else if (usuarioSelecionado != null && estatisticasUsuarios.containsKey(usuarioSelecionado)) {
                // Mostrar apenas o usuário selecionado
                datasetUsuarios.addValue(estatisticasUsuarios.get(usuarioSelecionado), "Coletados", usuarioSelecionado);
            }
            
            JFreeChart novoGraficoUsuarios = ChartFactory.createBarChart(
                null,
                "Usuário",
                "Itens Coletados",
                datasetUsuarios
            );
            estilizarGraficoBarras(novoGraficoUsuarios);
            
            CategoryPlot plot = novoGraficoUsuarios.getCategoryPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(52, 152, 219));
            
            painelGraficoUsuarios.setChart(novoGraficoUsuarios);
            
            // Atualizar gráfico de desempenho detalhado
            DefaultCategoryDataset datasetDesempenho = new DefaultCategoryDataset();
            // TODO: Implementar obterDesempenhoColetoresPorPeriodo no DashboardService
            Map<String, Map<String, Integer>> desempenhoDetalhado = new java.util.HashMap<>();
            
            if ("Todos os Usuários".equals(usuarioSelecionado)) {
                // Agregar dados de todos os usuários por período
                Map<String, Integer> totalPorPeriodo = new java.util.HashMap<>();
                for (Map<String, Integer> usuarioData : desempenhoDetalhado.values()) {
                    for (Map.Entry<String, Integer> entry : usuarioData.entrySet()) {
                        totalPorPeriodo.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    }
                }
                for (Map.Entry<String, Integer> entry : totalPorPeriodo.entrySet()) {
                    datasetDesempenho.addValue(entry.getValue(), "Total", entry.getKey());
                }
            } else if (usuarioSelecionado != null && desempenhoDetalhado.containsKey(usuarioSelecionado)) {
                // Mostrar dados do usuário selecionado
                Map<String, Integer> dadosUsuario = desempenhoDetalhado.get(usuarioSelecionado);
                for (Map.Entry<String, Integer> entry : dadosUsuario.entrySet()) {
                    datasetDesempenho.addValue(entry.getValue(), usuarioSelecionado, entry.getKey());
                }
            }
            
            JFreeChart novoGraficoDesempenho = ChartFactory.createBarChart(
                null,
                "Período",
                "Itens por Dia",
                datasetDesempenho
            );
            estilizarGraficoBarras(novoGraficoDesempenho);
            
            CategoryPlot plotDesempenho = novoGraficoDesempenho.getCategoryPlot();
            BarRenderer rendererDesempenho = (BarRenderer) plotDesempenho.getRenderer();
            rendererDesempenho.setSeriesPaint(0, new Color(155, 89, 182));
            
            painelGraficoDesempenhoUsuarios.setChart(novoGraficoDesempenho);
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos dos usuários: " + e.getMessage());
        }
    }
    
    private void atualizarInformacoesGerais(Map<String, Integer> estatisticas) {
        int totalPatrimonios = estatisticas.get("total_patrimonios");
        int coletados = estatisticas.get("itens_coletados");
        int semPatrimonio = estatisticas.getOrDefault("itens_sem_patrimonio", 0);
        
        // Calcular percentual: itens sem patrimônio são considerados como "encontrados"
        // e subtraídos dos itens que faltam ser coletados
        int totalItensEncontrados = coletados + semPatrimonio;
        int percentual = totalPatrimonios > 0 ? (totalItensEncontrados * 100 / totalPatrimonios) : 0;
        
        // Calcular itens que realmente faltam: Total - (Coletados + Sem Patrimônio)
        int itensQueFaltam = totalPatrimonios - totalItensEncontrados;
        // Garantir que não seja negativo
        itensQueFaltam = Math.max(0, itensQueFaltam);
        
        // Atualizar cards
        atualizarCardModerno(labelTotalPatrimonios, "📦 Total de Patrimônios", String.valueOf(totalPatrimonios), new Color(52, 152, 219));
        atualizarCardModerno(labelItensColetados, "✅ Itens Coletados", String.valueOf(coletados), new Color(46, 204, 113));
        atualizarCardModerno(labelItensNaoEncontrados, "❌ Não Inventariados", String.valueOf(itensQueFaltam), new Color(231, 76, 60));
        atualizarCardModerno(labelItensSemEtiqueta, "📦 Sem Patrimônio", String.valueOf(semPatrimonio), new Color(241, 196, 15));
        atualizarCardModerno(labelPercentualConcluido, "📊 % Concluído", percentual + "%", new Color(155, 89, 182));
        
        // Atualizar barra de progresso
        barraProgresso.setValue(percentual);
        barraProgresso.setString(percentual + "% Concluído");
    }
    
    private void atualizarCardModerno(JLabel card, String titulo, String valor, Color cor) {
        card.setText(String.format(
            "<html><div style='text-align: center; padding: 20px; font-family: Segoe UI;'>" +
            "<div style='color: rgb(%d,%d,%d); font-size: 14px; font-weight: bold; margin-bottom: 10px;'>%s</div>" +
            "<div style='font-size: 28px; font-weight: bold; color: rgb(52,73,94);'>%s</div>" +
            "</div></html>",
            cor.getRed(), cor.getGreen(), cor.getBlue(), titulo, valor
        ));
    }
    
    private void atualizarGraficos(Map<String, Integer> estatisticas) {
        // Atualizar gráfico de status
        DefaultPieDataset<String> datasetStatus = new DefaultPieDataset<>();
        datasetStatus.setValue("Coletados", estatisticas.get("itens_coletados"));
        datasetStatus.setValue("Não Inventariados", estatisticas.get("itens_nao_encontrados"));
        datasetStatus.setValue("Não Encontrados", estatisticas.get("itens_nao_coletados"));
        
        JFreeChart chartStatus = painelGraficoStatus.getChart();
        ((PiePlot<String>) chartStatus.getPlot()).setDataset(datasetStatus);
        
        // Atualizar gráfico de etiquetas
        atualizarGraficoEtiquetas(estatisticas);
        
        // Atualizar gráficos de responsáveis e setores
        atualizarGraficosResponsaveis();
        atualizarGraficosSetores();
    }
    
    private void atualizarGraficoEtiquetas(Map<String, Integer> estatisticas) {
        DefaultPieDataset<String> datasetEtiquetas = new DefaultPieDataset<>();
        int comPatrimonio = estatisticas.get("itens_coletados");
        int semPatrimonio = estatisticas.getOrDefault("itens_sem_patrimonio", 0);
        datasetEtiquetas.setValue("Com Patrimônio", comPatrimonio);
        datasetEtiquetas.setValue("Sem Patrimônio", semPatrimonio);
        
        JFreeChart chart = painelGraficoEtiquetas.getChart();
        ((PiePlot<String>) chart.getPlot()).setDataset(datasetEtiquetas);
    }
    
    private void atualizarGraficosResponsaveis() {
        // Converter List<Map> para Map<String, Map>
        java.util.List<Map<String, Object>> listaResponsaveis = dashboardService.obterEstatisticasPorResponsavel(idInventarioAtivo);
        Map<String, Map<String, Integer>> dadosResponsaveis = converterListaParaMapaResponsaveis(listaResponsaveis);
        
        // Gráfico principal
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Map<String, Integer>> entry : dadosResponsaveis.entrySet()) {
            String responsavel = entry.getKey();
            Map<String, Integer> dados = entry.getValue();
            
            dataset1.addValue(dados.get("total_patrimonios"), "Total", responsavel);
            dataset1.addValue(dados.get("itens_coletados"), "Coletados", responsavel);
            
            int total = dados.get("total_patrimonios");
            int coletados = dados.get("itens_coletados");
            int percentual = total > 0 ? (coletados * 100 / total) : 0;
            dataset2.addValue(percentual, "% Concluído", responsavel);
        }
        
        painelGraficoResponsaveis.getChart().getCategoryPlot().setDataset(dataset1);
        painelGraficoResponsaveisDetalhado.getChart().getCategoryPlot().setDataset(dataset2);
    }
    
    private void atualizarGraficosSetores() {
        // Converter List<Map> para Map<String, Map>
        java.util.List<Map<String, Object>> listaSetores = dashboardService.obterProgressoPorSetor(idInventarioAtivo);
        Map<String, Map<String, Integer>> dadosSetores = converterListaParaMapaSetores(listaSetores);
        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Map<String, Integer>> entry : dadosSetores.entrySet()) {
            String setor = entry.getKey();
            Map<String, Integer> dados = entry.getValue();
            
            dataset1.addValue(dados.get("percentual"), "% Concluído", setor);
            
            dataset2.addValue(dados.get("total_patrimonios"), "Total", setor);
            dataset2.addValue(dados.get("itens_coletados"), "Coletados", setor);
        }
        
        painelGraficoSetores.getChart().getCategoryPlot().setDataset(dataset1);
        painelGraficoSetoresDetalhado.getChart().getCategoryPlot().setDataset(dataset2);
    }
    
    // Métodos auxiliares
    
    private void iniciarAtualizacaoAutomatica() {
        timer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarDados();
            }
        });
        timer.start();
    }
    
    private void pararAtualizacaoAutomatica() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    private void obterInventarioAtivo() {
        try {
            Inventario inventarioAtivo = dashboardService.obterInventarioAtivo();
            if (inventarioAtivo != null) {
                this.idInventarioAtivo = inventarioAtivo.getId();
                this.nomeInventarioAtivo = inventarioAtivo.getNome();
            } else {
                this.idInventarioAtivo = -1;
                this.nomeInventarioAtivo = "Nenhum inventário ativo";
                JOptionPane.showMessageDialog(this,
                    "Nenhum inventário ativo encontrado.\nO dashboard não poderá exibir dados.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            this.idInventarioAtivo = -1;
            this.nomeInventarioAtivo = "Erro ao carregar inventário";
            System.err.println("Erro ao obter inventário ativo: " + e.getMessage());
        }
    }
    
    private void atualizarTitulosInterface() {
        setTitle("📊 Dashboard de Coleta - " + nomeInventarioAtivo);
    }
    
    /**
     * Converte Map<String, Object> para Map<String, Integer>
     */
    private Map<String, Integer> converterMapaEstatisticas(Map<String, Object> mapa) {
        Map<String, Integer> resultado = new java.util.HashMap<>();
        
        for (Map.Entry<String, Object> entry : mapa.entrySet()) {
            Object valor = entry.getValue();
            if (valor instanceof Number) {
                resultado.put(entry.getKey(), ((Number) valor).intValue());
            } else {
                resultado.put(entry.getKey(), 0);
            }
        }
        
        return resultado;
    }
    
    /**
     * Converte lista de mapas de responsáveis para estrutura de dados adequada
     */
    private Map<String, Map<String, Integer>> converterListaParaMapaResponsaveis(java.util.List<Map<String, Object>> lista) {
        Map<String, Map<String, Integer>> resultado = new java.util.HashMap<>();
        
        for (Map<String, Object> item : lista) {
            String responsavel = (String) item.get("responsavel");
            if (responsavel == null || responsavel.isEmpty()) {
                responsavel = "Sem Responsável";
            }
            
            Map<String, Integer> dados = new java.util.HashMap<>();
            dados.put("total_patrimonios", ((Number) item.getOrDefault("total_patrimonios", 0)).intValue());
            dados.put("itens_coletados", ((Number) item.getOrDefault("itens_coletados", 0)).intValue());
            
            resultado.put(responsavel, dados);
        }
        
        return resultado;
    }
    
    /**
     * Converte lista de mapas de setores para estrutura de dados adequada
     */
    private Map<String, Map<String, Integer>> converterListaParaMapaSetores(java.util.List<Map<String, Object>> lista) {
        Map<String, Map<String, Integer>> resultado = new java.util.HashMap<>();
        
        for (Map<String, Object> item : lista) {
            String setor = (String) item.get("setor");
            if (setor == null || setor.isEmpty()) {
                setor = "Sem Setor";
            }
            
            Map<String, Integer> dados = new java.util.HashMap<>();
            dados.put("total_patrimonios", ((Number) item.getOrDefault("total_patrimonios", 0)).intValue());
            dados.put("itens_coletados", ((Number) item.getOrDefault("itens_coletados", 0)).intValue());
            dados.put("percentual", ((Number) item.getOrDefault("percentual", 0)).intValue());
            
            resultado.put(setor, dados);
        }
        
        return resultado;
    }
    
    @Override
    public void dispose() {
        pararAtualizacaoAutomatica();
        super.dispose();
    }
}