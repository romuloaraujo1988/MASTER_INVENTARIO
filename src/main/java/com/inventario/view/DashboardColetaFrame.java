package com.inventario.view;

import com.inventario.model.Inventario;
import com.inventario.service.DashboardService;
import com.inventario.view.ui.ButtonStyleFactory;
import com.inventario.event.DashboardEvent;
import com.inventario.event.DashboardEventBus;
import com.inventario.event.DashboardEventType;
import com.inventario.event.DashboardObserver;
import com.inventario.event.CircularEventBuffer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Dashboard gráfico em tempo real para monitoramento da coleta de inventário
 * Versão melhorada com interface em abas e gráficos mais interativos
 * 
 * REFATORADO: Implementa DashboardObserver para receber eventos em tempo real
 * ao invés de usar polling com Timer de 30 segundos.
 */
public class DashboardColetaFrame extends JFrame implements DashboardObserver {

    private final DashboardService dashboardService;
    private Timer timer;  // Mantido como fallback, mas com intervalo maior
    private int idInventarioAtivo = -1;
    private String nomeInventarioAtivo = "Nenhum inventário ativo";
    
    // Observer Pattern - Controle de eventos
    private final CircularEventBuffer eventLog = new CircularEventBuffer(10);
    private final Queue<DashboardEvent> pendingEvents = new ConcurrentLinkedQueue<>();
    private volatile boolean isPaused = false;
    private volatile boolean isVisible = true;
    private Timer autoResumeTimer;
    private static final int AUTO_RESUME_DELAY_MS = 5 * 60 * 1000; // 5 minutos
    
    // Indicadores visuais
    private JLabel lblUpdateIndicator;
    private JToggleButton btnPauseResume;

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

    // Aba Gráficos - Novos gráficos de pizza
    private ChartPanel painelGraficoProgressoResponsaveis;
    private ChartPanel painelGraficoProgressoSetores;

    // Aba Análise por Responsável
    private ChartPanel painelGraficoResponsaveis;
    private ChartPanel painelGraficoResponsaveisDetalhado;
    private JComboBox<String> comboResponsaveis;

    // Aba Análise por Setor
    private ChartPanel painelGraficoSetores;
    private ChartPanel painelGraficoSetoresDetalhado;
    private JComboBox<String> comboSetores;

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
        registrarNoEventBus();
        iniciarAtualizacaoAutomatica();
        atualizarDados();
        
        // Listener para detectar quando a janela é minimizada/restaurada
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                isVisible = false;
            }
            
            @Override
            public void windowDeiconified(WindowEvent e) {
                isVisible = true;
                processarEventosPendentes();
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                desregistrarDoEventBus();
            }
        });
    }
    
    // ==================== IMPLEMENTAÇÃO DO DASHBOARD OBSERVER ====================
    
    /**
     * Registra este frame no EventBus para receber eventos.
     */
    private void registrarNoEventBus() {
        DashboardEventBus.getInstance().register(this);
        System.out.println("DashboardColetaFrame registrado no EventBus");
    }
    
    /**
     * Remove este frame do EventBus ao fechar.
     */
    private void desregistrarDoEventBus() {
        DashboardEventBus.getInstance().unregister(this);
        System.out.println("DashboardColetaFrame desregistrado do EventBus");
    }
    
    @Override
    public void onDashboardEvent(DashboardEvent event) {
        // Adicionar ao log de eventos
        eventLog.add(event);
        
        // Se pausado ou não visível, enfileirar para processamento posterior
        if (isPaused || !isVisible) {
            pendingEvents.add(event);
            return;
        }
        
        // Processar evento na thread da UI
        SwingUtilities.invokeLater(() -> {
            mostrarIndicadorAtualizacao();
            atualizarDadosPorEvento(event);
            ocultarIndicadorAtualizacao();
        });
    }
    
    @Override
    public void onDashboardEventBatch(List<DashboardEvent> events) {
        // Adicionar todos ao log
        for (DashboardEvent event : events) {
            eventLog.add(event);
        }
        
        // Se pausado ou não visível, enfileirar
        if (isPaused || !isVisible) {
            pendingEvents.addAll(events);
            return;
        }
        
        // Processar batch - atualizar uma única vez
        SwingUtilities.invokeLater(() -> {
            mostrarIndicadorAtualizacao();
            atualizarDados(); // Atualização completa para batch
            ocultarIndicadorAtualizacao();
        });
    }
    
    @Override
    public Set<DashboardEventType> getSubscribedEventTypes() {
        // Interessado em eventos de coleta e inventário
        return EnumSet.of(
            DashboardEventType.COLETA_SINCRONIZADA,
            DashboardEventType.INVENTARIO_ALTERADO,
            DashboardEventType.PATRIMONIO_ATUALIZADO
        );
    }
    
    @Override
    public String getObserverName() {
        return "DashboardColetaFrame";
    }
    
    /**
     * Atualiza dados específicos baseado no tipo de evento.
     */
    private void atualizarDadosPorEvento(DashboardEvent event) {
        switch (event.getType()) {
            case COLETA_SINCRONIZADA:
                // Atualizar estatísticas de coleta
                atualizarEstatisticasColeta();
                break;
            case INVENTARIO_ALTERADO:
                // Recarregar inventário ativo
                obterInventarioAtivo();
                atualizarTitulosInterface();
                atualizarDados();
                break;
            case PATRIMONIO_ATUALIZADO:
                // Atualizar gráficos de patrimônio
                atualizarEstatisticasColeta();
                break;
            default:
                atualizarDados();
        }
    }
    
    /**
     * Atualiza apenas as estatísticas de coleta (mais leve que atualização completa).
     */
    private void atualizarEstatisticasColeta() {
        try {
            if (idInventarioAtivo <= 0) return;
            
            Map<String, Object> estatisticasObj = dashboardService.obterEstatisticasGerais(idInventarioAtivo);
            if (estatisticasObj != null) {
                Map<String, Integer> estatisticas = converterMapaEstatisticas(estatisticasObj);
                atualizarInformacoesGerais(estatisticas);
                atualizarGraficos(estatisticas);
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar estatísticas: " + e.getMessage());
        }
    }
    
    /**
     * Processa eventos que foram enfileirados enquanto a janela estava oculta/pausada.
     */
    private void processarEventosPendentes() {
        if (pendingEvents.isEmpty()) return;
        
        SwingUtilities.invokeLater(() -> {
            mostrarIndicadorAtualizacao();
            // Limpar fila e fazer uma única atualização
            pendingEvents.clear();
            atualizarDados();
            ocultarIndicadorAtualizacao();
        });
    }
    
    /**
     * Mostra indicador visual de que dados estão sendo atualizados.
     */
    private void mostrarIndicadorAtualizacao() {
        if (lblUpdateIndicator != null) {
            lblUpdateIndicator.setText("🔄 Atualizando...");
            lblUpdateIndicator.setVisible(true);
        }
    }
    
    /**
     * Oculta indicador de atualização após 500ms.
     */
    private void ocultarIndicadorAtualizacao() {
        Timer hideTimer = new Timer(500, e -> {
            if (lblUpdateIndicator != null) {
                lblUpdateIndicator.setVisible(false);
            }
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }
    
    /**
     * Pausa as atualizações automáticas.
     */
    private void pausarAtualizacoes() {
        isPaused = true;
        if (btnPauseResume != null) {
            btnPauseResume.setText("▶ Resumir");
            btnPauseResume.setSelected(true);
        }
        
        // Iniciar timer de auto-resume
        if (autoResumeTimer != null) {
            autoResumeTimer.stop();
        }
        autoResumeTimer = new Timer(AUTO_RESUME_DELAY_MS, e -> resumirAtualizacoes());
        autoResumeTimer.setRepeats(false);
        autoResumeTimer.start();
    }
    
    /**
     * Resume as atualizações automáticas.
     */
    private void resumirAtualizacoes() {
        isPaused = false;
        if (btnPauseResume != null) {
            btnPauseResume.setText("⏸ Pausar");
            btnPauseResume.setSelected(false);
        }
        
        // Cancelar timer de auto-resume
        if (autoResumeTimer != null) {
            autoResumeTimer.stop();
        }
        
        // Processar eventos pendentes e atualizar
        processarEventosPendentes();
        atualizarDados();
    }
    
    /**
     * Retorna o histórico de eventos para exibição em tooltip.
     */
    public String getEventHistoryTooltip() {
        return eventLog.toHistoryString();
    }

    private void initializeComponents() {
        setTitle("📊 Dashboard de Coleta - " + nomeInventarioAtivo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Criar painel de abas
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

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
        titulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        aba.add(titulo, BorderLayout.NORTH);

        // Painel central com gráficos principais em grid 2x2
        JPanel painelGraficos = new JPanel(new GridLayout(2, 2, 15, 15));
        painelGraficos.setBackground(new Color(248, 249, 250));

        // Gráfico de status da coleta
        painelGraficoStatus = new ChartPanel(criarGraficoPizzaStatus());
        painelGraficoStatus.setBorder(criarBordaModerna("📊 Status da Coleta"));
        painelGraficoStatus.setPreferredSize(new Dimension(500, 400));

        // Gráfico de etiquetas
        painelGraficoEtiquetas = new ChartPanel(criarGraficoPizzaEtiquetas());
        painelGraficoEtiquetas.setBorder(criarBordaModerna("🏷️ Situação das Etiquetas"));
        painelGraficoEtiquetas.setPreferredSize(new Dimension(500, 400));

        // Gráfico de progresso por responsável
        painelGraficoProgressoResponsaveis = new ChartPanel(criarGraficoPizzaProgressoResponsaveis());
        painelGraficoProgressoResponsaveis.setBorder(criarBordaModerna("👥 Progresso por Responsável"));
        painelGraficoProgressoResponsaveis.setPreferredSize(new Dimension(500, 400));

        // Gráfico de progresso por setor
        painelGraficoProgressoSetores = new ChartPanel(criarGraficoPizzaProgressoSetores());
        painelGraficoProgressoSetores.setBorder(criarBordaModerna("🏢 Progresso por Setor"));
        painelGraficoProgressoSetores.setPreferredSize(new Dimension(500, 400));

        painelGraficos.add(painelGraficoStatus);
        painelGraficos.add(painelGraficoEtiquetas);
        painelGraficos.add(painelGraficoProgressoResponsaveis);
        painelGraficos.add(painelGraficoProgressoSetores);

        aba.add(painelGraficos, BorderLayout.CENTER);

        return aba;
    }

    private JPanel criarAbaResponsaveis() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBorder(new EmptyBorder(15, 15, 15, 15));
        aba.setBackground(new Color(248, 249, 250));

        // Painel superior com título e controles
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(new Color(248, 249, 250));

        JLabel titulo = new JLabel("👥 Análise Detalhada por Responsável");
        titulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Painel de controles
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelControles.setBackground(new Color(248, 249, 250));

        JLabel labelResponsavel = new JLabel("Selecionar Responsável:");
        labelResponsavel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelResponsavel.setForeground(new Color(52, 73, 94));

        comboResponsaveis = new JComboBox<>();
        comboResponsaveis.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboResponsaveis.setPreferredSize(new Dimension(250, 25));
        comboResponsaveis.addActionListener(e -> atualizarGraficosResponsaveisFiltrado());

        JButton btnTodosResponsaveis = ButtonStyleFactory.createPrimaryButton("Todos");
        btnTodosResponsaveis.addActionListener(e -> {
            comboResponsaveis.setSelectedIndex(0);
            atualizarGraficosResponsaveisFiltrado();
        });

        painelControles.add(labelResponsavel);
        painelControles.add(Box.createHorizontalStrut(10));
        painelControles.add(comboResponsaveis);
        painelControles.add(Box.createHorizontalStrut(15));
        painelControles.add(btnTodosResponsaveis);

        painelSuperior.add(titulo, BorderLayout.NORTH);
        painelSuperior.add(painelControles, BorderLayout.SOUTH);

        aba.add(painelSuperior, BorderLayout.NORTH);

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

        // Painel superior com título e controles
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(new Color(248, 249, 250));

        JLabel titulo = new JLabel("🏢 Análise Detalhada por Setor");
        titulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 73, 94));
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Painel de controles
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelControles.setBackground(new Color(248, 249, 250));

        JLabel labelSetor = new JLabel("Selecionar Setor:");
        labelSetor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelSetor.setForeground(new Color(52, 73, 94));

        comboSetores = new JComboBox<>();
        comboSetores.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboSetores.setPreferredSize(new Dimension(250, 25));
        comboSetores.addActionListener(e -> atualizarGraficosSetoresFiltrado());

        JButton btnTodosSetores = ButtonStyleFactory.createPrimaryButton("Todos");
        btnTodosSetores.addActionListener(e -> {
            comboSetores.setSelectedIndex(0);
            atualizarGraficosSetoresFiltrado();
        });

        painelControles.add(labelSetor);
        painelControles.add(Box.createHorizontalStrut(10));
        painelControles.add(comboSetores);
        painelControles.add(Box.createHorizontalStrut(15));
        painelControles.add(btnTodosSetores);

        painelSuperior.add(titulo, BorderLayout.NORTH);
        painelSuperior.add(painelControles, BorderLayout.SOUTH);

        aba.add(painelSuperior, BorderLayout.NORTH);

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
        titulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
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

        JButton btnTodosUsuarios = ButtonStyleFactory.createPrimaryButton("Todos");
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
                "<html><div style='text-align: center; padding: 20px; font-family: Segoe UI Emoji;'>" +
                        "<div style='color: rgb(%d,%d,%d); font-size: 14px; font-weight: bold; margin-bottom: 10px;'>%s</div>"
                        +
                        "<div style='font-size: 28px; font-weight: bold; color: rgb(52,73,94);'>%s</div>" +
                        "</div></html>",
                cor.getRed(), cor.getGreen(), cor.getBlue(), titulo, valor));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setHorizontalAlignment(SwingConstants.CENTER);

        // Adicionar sombra sutil
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 20)),
                card.getBorder()));

        return card;
    }

    private JPanel criarPainelProgressoModerno() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 20)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15))));

        JLabel titulo = new JLabel("🎯 Progresso Geral");
        titulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
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
                                new Font("Segoe UI Emoji", Font.BOLD, 14),
                                new Color(52, 73, 94)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }

    private JPanel criarPainelControles() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painel.setBackground(new Color(52, 73, 94));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Botões modernos
        JButton btnAtualizar = ButtonStyleFactory.createPrimaryButton("Atualizar");
        btnAtualizar.addActionListener(e -> atualizarDados());

        JButton btnFechar = ButtonStyleFactory.createDangerButton("Fechar");
        btnFechar.addActionListener(e -> {
            pararAtualizacaoAutomatica();
            desregistrarDoEventBus();
            dispose();
        });
        
        // Botão Pause/Resume para atualizações automáticas
        btnPauseResume = new JToggleButton("⏸ Pausar");
        btnPauseResume.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btnPauseResume.setBackground(new Color(241, 196, 15));
        btnPauseResume.setForeground(Color.BLACK);
        btnPauseResume.setFocusPainted(false);
        btnPauseResume.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnPauseResume.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPauseResume.addActionListener(e -> {
            if (btnPauseResume.isSelected()) {
                pausarAtualizacoes();
            } else {
                resumirAtualizacoes();
            }
        });
        
        // Indicador de atualização
        lblUpdateIndicator = new JLabel("🔄 Atualizando...");
        lblUpdateIndicator.setForeground(new Color(46, 204, 113));
        lblUpdateIndicator.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblUpdateIndicator.setVisible(false);

        JLabel labelAtualizacao = new JLabel("Atualização em tempo real via eventos");
        labelAtualizacao.setForeground(Color.WHITE);
        labelAtualizacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelAtualizacao.setToolTipText(getEventHistoryTooltip());
        
        // Atualizar tooltip periodicamente
        Timer tooltipTimer = new Timer(5000, e -> labelAtualizacao.setToolTipText(getEventHistoryTooltip()));
        tooltipTimer.start();

        painel.add(btnAtualizar);
        painel.add(Box.createHorizontalStrut(10));
        painel.add(btnPauseResume);
        painel.add(Box.createHorizontalStrut(10));
        painel.add(lblUpdateIndicator);
        painel.add(Box.createHorizontalStrut(20));

        JButton btnStatusSalas = ButtonStyleFactory.createPrimaryButton("Status das Salas");
        btnStatusSalas.addActionListener(e -> {
            StatusSalasFrame statusFrame = new StatusSalasFrame();
            statusFrame.setVisible(true);
        });
        painel.add(btnStatusSalas);
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
                false);

        // Estilização moderna
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.setAntiAlias(true);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setCircular(true);
        plot.setStartAngle(290);

        // Gradientes de cores modernos
        plot.setSectionPaint("Coletados", criarGradiente(new Color(46, 204, 113), new Color(39, 174, 96)));
        plot.setSectionPaint("Não Inventariados", criarGradiente(new Color(231, 76, 60), new Color(192, 57, 43)));
        plot.setSectionPaint("Não Encontrados", criarGradiente(new Color(149, 165, 166), new Color(127, 140, 141)));

        // Labels personalizados com percentual e valores
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}\n{1} ({2})",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0.0%")));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 220));
        plot.setLabelOutlinePaint(new Color(200, 200, 200));
        plot.setLabelShadowPaint(new Color(0, 0, 0, 80));
        plot.setLabelPaint(new Color(52, 73, 94));
        plot.setLabelGap(0.02);

        // Sombra suave nas fatias
        plot.setShadowPaint(new Color(0, 0, 0, 60));
        plot.setShadowXOffset(5);
        plot.setShadowYOffset(5);

        // Bordas arredondadas nas seções
        plot.setSimpleLabels(false);
        plot.setInteriorGap(0.02);

        // Legenda elegante
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(248, 249, 250));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(new Color(220, 221, 225)));
            chart.getLegend().setPadding(new RectangleInsets(10, 10, 10, 10));
        }

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
                false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.setAntiAlias(true);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setCircular(true);
        plot.setStartAngle(290);

        // Gradientes vibrantes
        plot.setSectionPaint("Com Etiqueta", criarGradiente(new Color(46, 204, 113), new Color(39, 174, 96)));
        plot.setSectionPaint("Sem Etiqueta", criarGradiente(new Color(241, 196, 15), new Color(243, 156, 18)));

        // Labels personalizados
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}\n{1} ({2})",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0.0%")));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 220));
        plot.setLabelOutlinePaint(new Color(200, 200, 200));
        plot.setLabelShadowPaint(new Color(0, 0, 0, 80));
        plot.setLabelPaint(new Color(52, 73, 94));
        plot.setLabelGap(0.02);

        // Sombra suave
        plot.setShadowPaint(new Color(0, 0, 0, 60));
        plot.setShadowXOffset(5);
        plot.setShadowYOffset(5);

        // Bordas arredondadas
        plot.setSimpleLabels(false);
        plot.setInteriorGap(0.02);

        // Legenda elegante
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(248, 249, 250));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(new Color(220, 221, 225)));
            chart.getLegend().setPadding(new RectangleInsets(10, 10, 10, 10));
        }

        return chart;
    }

    private JFreeChart criarGraficoPizzaProgressoResponsaveis() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        try {
            if (idInventarioAtivo != -1) {
                java.util.List<Map<String, Object>> listaResponsaveis = dashboardService
                        .obterEstatisticasPorResponsavel(idInventarioAtivo);

                if (listaResponsaveis != null && !listaResponsaveis.isEmpty()) {
                    for (Map<String, Object> item : listaResponsaveis) {
                        String responsavel = (String) item.get("responsavel");
                        if (responsavel == null || responsavel.isEmpty()) {
                            responsavel = "Sem Responsável";
                        }

                        int coletados = ((Number) item.getOrDefault("itens_coletados", 0)).intValue();
                        if (coletados > 0) {
                            dataset.setValue(responsavel, coletados);
                        }
                    }
                } else {
                    dataset.setValue("Nenhum dado disponível", 1);
                }
            } else {
                dataset.setValue("Nenhum inventário ativo", 1);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados dos responsáveis: " + e.getMessage());
            e.printStackTrace();
            dataset.setValue("Erro ao carregar dados", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.setAntiAlias(true);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setCircular(true);
        plot.setStartAngle(290);

        // Labels personalizados com percentual
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}\n{1} ({2})",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0.0%")));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 220));
        plot.setLabelOutlinePaint(new Color(200, 200, 200));
        plot.setLabelShadowPaint(new Color(0, 0, 0, 80));
        plot.setLabelPaint(new Color(52, 73, 94));
        plot.setLabelGap(0.02);

        // Sombra suave
        plot.setShadowPaint(new Color(0, 0, 0, 60));
        plot.setShadowXOffset(5);
        plot.setShadowYOffset(5);

        // Bordas arredondadas
        plot.setSimpleLabels(false);
        plot.setInteriorGap(0.02);

        // Cores com gradientes variados
        Color[][] coresGradiente = {
                { new Color(52, 152, 219), new Color(41, 128, 185) }, // Azul
                { new Color(46, 204, 113), new Color(39, 174, 96) }, // Verde
                { new Color(155, 89, 182), new Color(142, 68, 173) }, // Roxo
                { new Color(241, 196, 15), new Color(243, 156, 18) }, // Amarelo
                { new Color(231, 76, 60), new Color(192, 57, 43) }, // Vermelho
                { new Color(26, 188, 156), new Color(22, 160, 133) }, // Turquesa
                { new Color(230, 126, 34), new Color(211, 84, 0) }, // Laranja
                { new Color(149, 165, 166), new Color(127, 140, 141) } // Cinza
        };

        int i = 0;
        for (Object key : dataset.getKeys()) {
            Color[] gradiente = coresGradiente[i % coresGradiente.length];
            plot.setSectionPaint((String) key, criarGradiente(gradiente[0], gradiente[1]));
            i++;
        }

        // Legenda elegante
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(248, 249, 250));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(new Color(220, 221, 225)));
            chart.getLegend().setPadding(new RectangleInsets(10, 10, 10, 10));
        }

        return chart;
    }

    private JFreeChart criarGraficoPizzaProgressoSetores() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        try {
            if (idInventarioAtivo != -1) {
                java.util.List<Map<String, Object>> listaSetores = dashboardService
                        .obterProgressoPorSetor(idInventarioAtivo);

                if (listaSetores != null && !listaSetores.isEmpty()) {
                    for (Map<String, Object> item : listaSetores) {
                        String setor = (String) item.get("setor");
                        if (setor == null || setor.isEmpty()) {
                            setor = "Sem Setor";
                        }

                        int coletados = ((Number) item.getOrDefault("itens_coletados", 0)).intValue();
                        if (coletados > 0) {
                            dataset.setValue(setor, coletados);
                        }
                    }
                } else {
                    dataset.setValue("Nenhum dado disponível", 1);
                }
            } else {
                dataset.setValue("Nenhum inventário ativo", 1);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados dos setores: " + e.getMessage());
            e.printStackTrace();
            dataset.setValue("Erro ao carregar dados", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.setAntiAlias(true);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setCircular(true);
        plot.setStartAngle(290);

        // Labels personalizados com percentual
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}\n{1} ({2})",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0.0%")));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 220));
        plot.setLabelOutlinePaint(new Color(200, 200, 200));
        plot.setLabelShadowPaint(new Color(0, 0, 0, 80));
        plot.setLabelPaint(new Color(52, 73, 94));
        plot.setLabelGap(0.02);

        // Sombra suave
        plot.setShadowPaint(new Color(0, 0, 0, 60));
        plot.setShadowXOffset(5);
        plot.setShadowYOffset(5);

        // Bordas arredondadas
        plot.setSimpleLabels(false);
        plot.setInteriorGap(0.02);

        // Cores com gradientes variados
        Color[][] coresGradiente = {
                { new Color(231, 76, 60), new Color(192, 57, 43) }, // Vermelho
                { new Color(52, 152, 219), new Color(41, 128, 185) }, // Azul
                { new Color(46, 204, 113), new Color(39, 174, 96) }, // Verde
                { new Color(241, 196, 15), new Color(243, 156, 18) }, // Amarelo
                { new Color(155, 89, 182), new Color(142, 68, 173) }, // Roxo
                { new Color(230, 126, 34), new Color(211, 84, 0) }, // Laranja
                { new Color(26, 188, 156), new Color(22, 160, 133) }, // Turquesa
                { new Color(149, 165, 166), new Color(127, 140, 141) } // Cinza
        };

        int i = 0;
        for (Object key : dataset.getKeys()) {
            Color[] gradiente = coresGradiente[i % coresGradiente.length];
            plot.setSectionPaint((String) key, criarGradiente(gradiente[0], gradiente[1]));
            i++;
        }

        // Legenda elegante
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(248, 249, 250));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(new Color(220, 221, 225)));
            chart.getLegend().setPadding(new RectangleInsets(10, 10, 10, 10));
        }

        return chart;
    }

    private JFreeChart criarGraficoBarrasResponsaveis() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Responsável",
                "Quantidade",
                dataset);

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
                dataset);

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
                dataset);

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
                dataset);

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
                Map<String, Integer> estatisticasUsuarios = dashboardService
                        .obterEstatisticasColetores(idInventarioAtivo);

                if (estatisticasUsuarios != null && !estatisticasUsuarios.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : estatisticasUsuarios.entrySet()) {
                        if (entry.getKey() != null && !entry.getKey().trim().isEmpty()) {
                            dataset.addValue(entry.getValue(), "Itens Coletados", entry.getKey());
                        }
                    }
                } else {
                    dataset.addValue(0, "Itens Coletados", "Nenhum dado disponível");
                }
            } else {
                dataset.addValue(0, "Itens Coletados", "Nenhum inventário ativo");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados dos usuários: " + e.getMessage());
            e.printStackTrace();
            // Dados de fallback
            dataset.addValue(0, "Itens Coletados", "Erro ao carregar dados");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Usuário",
                "Itens Coletados",
                dataset);

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
                Map<String, Map<String, Integer>> desempenhoDetalhado = dashboardService
                        .obterDesempenhoColetoresPorPeriodo(idInventarioAtivo);

                if (desempenhoDetalhado != null && !desempenhoDetalhado.isEmpty()) {
                    for (Map.Entry<String, Map<String, Integer>> usuarioEntry : desempenhoDetalhado.entrySet()) {
                        String usuario = usuarioEntry.getKey();
                        Map<String, Integer> dadosPorData = usuarioEntry.getValue();

                        if (dadosPorData != null && !dadosPorData.isEmpty()) {
                            for (Map.Entry<String, Integer> dataEntry : dadosPorData.entrySet()) {
                                String data = dataEntry.getKey();
                                Integer quantidade = dataEntry.getValue();
                                dataset.addValue(quantidade, usuario, data);
                            }
                        }
                    }
                } else {
                    dataset.addValue(0, "Nenhum dado", "Hoje");
                }
            } else {
                dataset.addValue(0, "Nenhum inventário", "Hoje");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar desempenho dos usuários: " + e.getMessage());
            e.printStackTrace();
            // Dados de fallback
            dataset.addValue(0, "Erro ao carregar", "Hoje");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Período",
                "Itens por Dia",
                dataset);

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
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.setAntiAlias(true);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(220, 221, 225));
        plot.setOutlineVisible(false);
        plot.setRangePannable(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // Usar GradientBarPainter para efeito de gradiente nas barras
        renderer.setBarPainter(new GradientBarPainter(0.1, 0.2, 0.3));
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.15);
        renderer.setShadowVisible(true);
        renderer.setShadowPaint(new Color(0, 0, 0, 50));
        renderer.setShadowXOffset(3);
        renderer.setShadowYOffset(3);

        // Labels nos valores das barras
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", new DecimalFormat("0.0")));
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 10));
        renderer.setDefaultItemLabelPaint(new Color(52, 73, 94));

        // Fontes modernas e elegantes
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 13));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 13));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setLabelPaint(new Color(52, 73, 94));
        plot.getRangeAxis().setLabelPaint(new Color(52, 73, 94));

        // Legenda elegante
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(248, 249, 250));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(new Color(220, 221, 225)));
            chart.getLegend().setPadding(new RectangleInsets(10, 10, 10, 10));
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
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
            if (idInventarioAtivo == -1) {
                return;
            }

            // Preservar seleção atual antes de atualizar
            String selecaoAtual = (String) comboUsuarios.getSelectedItem();
            String nomeUsuarioSelecionado = null;
            if (selecaoAtual != null && !"Todos os Usuários".equals(selecaoAtual)) {
                // Extrair nome do usuário (remover contagem entre parênteses)
                nomeUsuarioSelecionado = selecaoAtual.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();
            }

            // Atualizar lista de usuários no combobox
            Map<String, Integer> usuarios = dashboardService.obterEstatisticasColetores(idInventarioAtivo);

            comboUsuarios.removeAllItems();
            comboUsuarios.addItem("Todos os Usuários");

            int indiceParaSelecionar = 0; // Default: "Todos os Usuários"
            int indiceAtual = 1;

            // Adicionar usuários que fizeram coletas
            for (String usuario : usuarios.keySet()) {
                if (usuario != null && !usuario.trim().isEmpty()) {
                    comboUsuarios.addItem(usuario + " (" + usuarios.get(usuario) + " itens)");
                    
                    // Verificar se este era o item selecionado anteriormente
                    if (nomeUsuarioSelecionado != null && usuario.equals(nomeUsuarioSelecionado)) {
                        indiceParaSelecionar = indiceAtual;
                    }
                    indiceAtual++;
                }
            }

            // Restaurar seleção anterior
            if (indiceParaSelecionar < comboUsuarios.getItemCount()) {
                comboUsuarios.setSelectedIndex(indiceParaSelecionar);
            }

            atualizarGraficosUsuarios();
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados dos usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficosUsuarios() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            String usuarioSelecionado = (String) comboUsuarios.getSelectedItem();

            // Obter estatísticas dos coletores
            Map<String, Integer> estatisticasUsuarios = dashboardService.obterEstatisticasColetores(idInventarioAtivo);

            // Atualizar gráfico de barras dos usuários
            DefaultCategoryDataset datasetUsuarios = new DefaultCategoryDataset();

            if ("Todos os Usuários".equals(usuarioSelecionado)) {
                // Mostrar todos os usuários
                for (Map.Entry<String, Integer> entry : estatisticasUsuarios.entrySet()) {
                    if (entry.getKey() != null && !entry.getKey().trim().isEmpty()) {
                        datasetUsuarios.addValue(entry.getValue(), "Coletados", entry.getKey());
                    }
                }
            } else if (usuarioSelecionado != null) {
                // Extrair nome do usuário (remover contagem entre parênteses)
                String nomeUsuario = usuarioSelecionado.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();

                if (estatisticasUsuarios.containsKey(nomeUsuario)) {
                    // Mostrar apenas o usuário selecionado
                    datasetUsuarios.addValue(estatisticasUsuarios.get(nomeUsuario), "Coletados", nomeUsuario);
                }
            }

            JFreeChart novoGraficoUsuarios = ChartFactory.createBarChart(
                    null,
                    "Usuário",
                    "Itens Coletados",
                    datasetUsuarios);
            estilizarGraficoBarras(novoGraficoUsuarios);

            CategoryPlot plot = novoGraficoUsuarios.getCategoryPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(52, 152, 219));

            painelGraficoUsuarios.setChart(novoGraficoUsuarios);

            // Atualizar gráfico de desempenho detalhado
            DefaultCategoryDataset datasetDesempenho = new DefaultCategoryDataset();
            Map<String, Map<String, Integer>> desempenhoDetalhado = dashboardService
                    .obterDesempenhoColetoresPorPeriodo(idInventarioAtivo);

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
            } else if (usuarioSelecionado != null) {
                // Extrair nome do usuário (remover contagem entre parênteses)
                String nomeUsuario = usuarioSelecionado.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();

                if (desempenhoDetalhado.containsKey(nomeUsuario)) {
                    // Mostrar dados do usuário selecionado
                    Map<String, Integer> dadosUsuario = desempenhoDetalhado.get(nomeUsuario);
                    for (Map.Entry<String, Integer> entry : dadosUsuario.entrySet()) {
                        datasetDesempenho.addValue(entry.getValue(), nomeUsuario, entry.getKey());
                    }
                }
            }

            JFreeChart novoGraficoDesempenho = ChartFactory.createBarChart(
                    null,
                    "Período",
                    "Itens por Dia",
                    datasetDesempenho);
            estilizarGraficoBarras(novoGraficoDesempenho);

            CategoryPlot plotDesempenho = novoGraficoDesempenho.getCategoryPlot();
            BarRenderer rendererDesempenho = (BarRenderer) plotDesempenho.getRenderer();
            rendererDesempenho.setSeriesPaint(0, new Color(155, 89, 182));

            painelGraficoDesempenhoUsuarios.setChart(novoGraficoDesempenho);

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos dos usuários: " + e.getMessage());
            e.printStackTrace();
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
        atualizarCardModerno(labelTotalPatrimonios, "📦 Total de Patrimônios", String.valueOf(totalPatrimonios),
                new Color(52, 152, 219));
        atualizarCardModerno(labelItensColetados, "✅ Itens Coletados", String.valueOf(coletados),
                new Color(46, 204, 113));
        atualizarCardModerno(labelItensNaoEncontrados, "❌ Não Inventariados", String.valueOf(itensQueFaltam),
                new Color(231, 76, 60));
        atualizarCardModerno(labelItensSemEtiqueta, "📦 Sem Patrimônio", String.valueOf(semPatrimonio),
                new Color(241, 196, 15));
        atualizarCardModerno(labelPercentualConcluido, "📊 % Concluído", percentual + "%", new Color(155, 89, 182));

        // Atualizar barra de progresso
        barraProgresso.setValue(percentual);
        barraProgresso.setString(percentual + "% Concluído");
    }

    private void atualizarCardModerno(JLabel card, String titulo, String valor, Color cor) {
        card.setText(String.format(
                "<html><div style='text-align: center; padding: 20px; font-family: Segoe UI;'>" +
                        "<div style='color: rgb(%d,%d,%d); font-size: 14px; font-weight: bold; margin-bottom: 10px;'>%s</div>"
                        +
                        "<div style='font-size: 28px; font-weight: bold; color: rgb(52,73,94);'>%s</div>" +
                        "</div></html>",
                cor.getRed(), cor.getGreen(), cor.getBlue(), titulo, valor));
    }

    private void atualizarGraficos(Map<String, Integer> estatisticas) {
        // Atualizar gráfico de status
        DefaultPieDataset<String> datasetStatus = new DefaultPieDataset<>();
        datasetStatus.setValue("Coletados", estatisticas.get("itens_coletados"));
        datasetStatus.setValue("Não Inventariados", estatisticas.get("itens_nao_encontrados"));
        datasetStatus.setValue("Não Encontrados", estatisticas.get("itens_nao_coletados"));

        JFreeChart chartStatus = painelGraficoStatus.getChart();
        Plot plotStatus = chartStatus.getPlot();
        if (plotStatus instanceof PiePlot) {
            @SuppressWarnings("unchecked")
            PiePlot<String> piePlotStatus = (PiePlot<String>) plotStatus;
            piePlotStatus.setDataset(datasetStatus);
        }

        // Atualizar gráfico de etiquetas
        atualizarGraficoEtiquetas(estatisticas);

        // Atualizar novos gráficos de pizza
        atualizarGraficoPizzaProgressoResponsaveis();
        atualizarGraficoPizzaProgressoSetores();

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
        Plot plot = chart.getPlot();
        if (plot instanceof PiePlot) {
            @SuppressWarnings("unchecked")
            PiePlot<String> piePlot = (PiePlot<String>) plot;
            piePlot.setDataset(datasetEtiquetas);
        }
    }

    private void atualizarGraficoPizzaProgressoResponsaveis() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            java.util.List<Map<String, Object>> listaResponsaveis = dashboardService
                    .obterEstatisticasPorResponsavel(idInventarioAtivo);

            if (listaResponsaveis != null && !listaResponsaveis.isEmpty()) {
                for (Map<String, Object> item : listaResponsaveis) {
                    String responsavel = (String) item.get("responsavel");
                    if (responsavel == null || responsavel.isEmpty()) {
                        responsavel = "Sem Responsável";
                    }

                    int coletados = ((Number) item.getOrDefault("itens_coletados", 0)).intValue();
                    if (coletados > 0) {
                        dataset.setValue(responsavel, coletados);
                    }
                }
            }

            JFreeChart chart = painelGraficoProgressoResponsaveis.getChart();
            Plot plot = chart.getPlot();
            if (plot instanceof PiePlot) {
                @SuppressWarnings("unchecked")
                PiePlot<String> piePlot = (PiePlot<String>) plot;
                piePlot.setDataset(dataset);

                // Aplicar cores
                Color[] cores = {
                        new Color(52, 152, 219), // Azul
                        new Color(46, 204, 113), // Verde
                        new Color(155, 89, 182), // Roxo
                        new Color(241, 196, 15), // Amarelo
                        new Color(231, 76, 60), // Vermelho
                        new Color(26, 188, 156), // Turquesa
                        new Color(230, 126, 34), // Laranja
                        new Color(149, 165, 166) // Cinza
                };

                int i = 0;
                for (Object key : dataset.getKeys()) {
                    piePlot.setSectionPaint((String) key, cores[i % cores.length]);
                    i++;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráfico de pizza de responsáveis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficoPizzaProgressoSetores() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            java.util.List<Map<String, Object>> listaSetores = dashboardService
                    .obterProgressoPorSetor(idInventarioAtivo);

            if (listaSetores != null && !listaSetores.isEmpty()) {
                for (Map<String, Object> item : listaSetores) {
                    String setor = (String) item.get("setor");
                    if (setor == null || setor.isEmpty()) {
                        setor = "Sem Setor";
                    }

                    int coletados = ((Number) item.getOrDefault("itens_coletados", 0)).intValue();
                    if (coletados > 0) {
                        dataset.setValue(setor, coletados);
                    }
                }
            }

            JFreeChart chart = painelGraficoProgressoSetores.getChart();
            Plot plot = chart.getPlot();
            if (plot instanceof PiePlot) {
                @SuppressWarnings("unchecked")
                PiePlot<String> piePlot = (PiePlot<String>) plot;
                piePlot.setDataset(dataset);

                // Aplicar cores
                Color[] cores = {
                        new Color(231, 76, 60), // Vermelho
                        new Color(52, 152, 219), // Azul
                        new Color(46, 204, 113), // Verde
                        new Color(241, 196, 15), // Amarelo
                        new Color(155, 89, 182), // Roxo
                        new Color(230, 126, 34), // Laranja
                        new Color(26, 188, 156), // Turquesa
                        new Color(149, 165, 166) // Cinza
                };

                int i = 0;
                for (Object key : dataset.getKeys()) {
                    piePlot.setSectionPaint((String) key, cores[i % cores.length]);
                    i++;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráfico de pizza de setores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficosResponsaveis() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            // Preservar seleção atual antes de atualizar
            String selecaoAtual = (String) comboResponsaveis.getSelectedItem();
            String nomeResponsavelSelecionado = null;
            if (selecaoAtual != null && !"Todos os Responsáveis".equals(selecaoAtual)) {
                // Extrair nome do responsável (remover contagem entre parênteses)
                nomeResponsavelSelecionado = selecaoAtual.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();
            }

            // Converter List<Map> para Map<String, Map>
            java.util.List<Map<String, Object>> listaResponsaveis = dashboardService
                    .obterEstatisticasPorResponsavel(idInventarioAtivo);
            Map<String, Map<String, Integer>> dadosResponsaveis = converterListaParaMapaResponsaveis(listaResponsaveis);

            // Atualizar combobox de responsáveis
            comboResponsaveis.removeAllItems();
            comboResponsaveis.addItem("Todos os Responsáveis");

            int indiceParaSelecionar = 0; // Default: "Todos os Responsáveis"
            int indiceAtual = 1;

            for (String responsavel : dadosResponsaveis.keySet()) {
                if (responsavel != null && !responsavel.trim().isEmpty() && !"Sem Responsável".equals(responsavel)) {
                    Map<String, Integer> dados = dadosResponsaveis.get(responsavel);
                    int coletados = dados.get("itens_coletados");
                    comboResponsaveis.addItem(responsavel + " (" + coletados + " itens)");
                    
                    // Verificar se este era o item selecionado anteriormente
                    if (nomeResponsavelSelecionado != null && responsavel.equals(nomeResponsavelSelecionado)) {
                        indiceParaSelecionar = indiceAtual;
                    }
                    indiceAtual++;
                }
            }

            // Restaurar seleção anterior
            if (indiceParaSelecionar < comboResponsaveis.getItemCount()) {
                comboResponsaveis.setSelectedIndex(indiceParaSelecionar);
            }

            // Atualizar gráficos
            atualizarGraficosResponsaveisFiltrado();

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos de responsáveis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficosResponsaveisFiltrado() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            String responsavelSelecionado = (String) comboResponsaveis.getSelectedItem();

            // Obter dados dos responsáveis
            java.util.List<Map<String, Object>> listaResponsaveis = dashboardService
                    .obterEstatisticasPorResponsavel(idInventarioAtivo);
            Map<String, Map<String, Integer>> dadosResponsaveis = converterListaParaMapaResponsaveis(listaResponsaveis);

            // Gráfico principal
            DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
            DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();

            if ("Todos os Responsáveis".equals(responsavelSelecionado)) {
                // Mostrar todos os responsáveis
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
            } else if (responsavelSelecionado != null) {
                // Extrair nome do responsável (remover contagem entre parênteses)
                String nomeResponsavel = responsavelSelecionado.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();

                if (dadosResponsaveis.containsKey(nomeResponsavel)) {
                    // Mostrar apenas o responsável selecionado
                    Map<String, Integer> dados = dadosResponsaveis.get(nomeResponsavel);

                    dataset1.addValue(dados.get("total_patrimonios"), "Total", nomeResponsavel);
                    dataset1.addValue(dados.get("itens_coletados"), "Coletados", nomeResponsavel);

                    int total = dados.get("total_patrimonios");
                    int coletados = dados.get("itens_coletados");
                    int percentual = total > 0 ? (coletados * 100 / total) : 0;
                    dataset2.addValue(percentual, "% Concluído", nomeResponsavel);
                }
            }

            painelGraficoResponsaveis.getChart().getCategoryPlot().setDataset(dataset1);
            painelGraficoResponsaveisDetalhado.getChart().getCategoryPlot().setDataset(dataset2);

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos filtrados de responsáveis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficosSetores() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            // Preservar seleção atual antes de atualizar
            String selecaoAtual = (String) comboSetores.getSelectedItem();
            String nomeSetorSelecionado = null;
            if (selecaoAtual != null && !"Todos os Setores".equals(selecaoAtual)) {
                // Extrair nome do setor (remover contagem entre parênteses)
                nomeSetorSelecionado = selecaoAtual.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();
            }

            // Converter List<Map> para Map<String, Map>
            java.util.List<Map<String, Object>> listaSetores = dashboardService
                    .obterProgressoPorSetor(idInventarioAtivo);
            Map<String, Map<String, Integer>> dadosSetores = converterListaParaMapaSetores(listaSetores);

            // Atualizar combobox de setores
            comboSetores.removeAllItems();
            comboSetores.addItem("Todos os Setores");

            int indiceParaSelecionar = 0; // Default: "Todos os Setores"
            int indiceAtual = 1;

            for (String setor : dadosSetores.keySet()) {
                if (setor != null && !setor.trim().isEmpty() && !"Sem Setor".equals(setor)) {
                    Map<String, Integer> dados = dadosSetores.get(setor);
                    int coletados = dados.get("itens_coletados");
                    comboSetores.addItem(setor + " (" + coletados + " itens)");
                    
                    // Verificar se este era o item selecionado anteriormente
                    if (nomeSetorSelecionado != null && setor.equals(nomeSetorSelecionado)) {
                        indiceParaSelecionar = indiceAtual;
                    }
                    indiceAtual++;
                }
            }

            // Restaurar seleção anterior
            if (indiceParaSelecionar < comboSetores.getItemCount()) {
                comboSetores.setSelectedIndex(indiceParaSelecionar);
            }

            // Atualizar gráficos
            atualizarGraficosSetoresFiltrado();

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos de setores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficosSetoresFiltrado() {
        try {
            if (idInventarioAtivo == -1) {
                return;
            }

            String setorSelecionado = (String) comboSetores.getSelectedItem();

            // Obter dados dos setores
            java.util.List<Map<String, Object>> listaSetores = dashboardService
                    .obterProgressoPorSetor(idInventarioAtivo);
            Map<String, Map<String, Integer>> dadosSetores = converterListaParaMapaSetores(listaSetores);

            // Gráfico principal
            DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
            DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();

            if ("Todos os Setores".equals(setorSelecionado)) {
                // Mostrar todos os setores
                for (Map.Entry<String, Map<String, Integer>> entry : dadosSetores.entrySet()) {
                    String setor = entry.getKey();
                    Map<String, Integer> dados = entry.getValue();

                    dataset1.addValue(dados.get("percentual"), "% Concluído", setor);

                    dataset2.addValue(dados.get("total_patrimonios"), "Total", setor);
                    dataset2.addValue(dados.get("itens_coletados"), "Coletados", setor);
                }
            } else if (setorSelecionado != null) {
                // Extrair nome do setor (remover contagem entre parênteses)
                String nomeSetor = setorSelecionado.replaceAll("\\s*\\(.*\\)\\s*$", "").trim();

                if (dadosSetores.containsKey(nomeSetor)) {
                    // Mostrar apenas o setor selecionado
                    Map<String, Integer> dados = dadosSetores.get(nomeSetor);

                    dataset1.addValue(dados.get("percentual"), "% Concluído", nomeSetor);

                    dataset2.addValue(dados.get("total_patrimonios"), "Total", nomeSetor);
                    dataset2.addValue(dados.get("itens_coletados"), "Coletados", nomeSetor);
                }
            }

            painelGraficoSetores.getChart().getCategoryPlot().setDataset(dataset1);
            painelGraficoSetoresDetalhado.getChart().getCategoryPlot().setDataset(dataset2);

        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos filtrados de setores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos auxiliares

    private void iniciarAtualizacaoAutomatica() {
        // Timer como fallback - intervalo maior pois eventos são recebidos em tempo real
        // Mantido para garantir consistência caso eventos não sejam recebidos
        timer = new Timer(120000, new ActionListener() { // 2 minutos ao invés de 30 segundos
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused) {
                    atualizarDados();
                }
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
    private Map<String, Map<String, Integer>> converterListaParaMapaResponsaveis(
            java.util.List<Map<String, Object>> lista) {
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

    /**
     * Cria um gradiente de cores para os gráficos
     */
    private GradientPaint criarGradiente(Color cor1, Color cor2) {
        return new GradientPaint(0, 0, cor1, 0, 300, cor2);
    }

    @Override
    public void dispose() {
        pararAtualizacaoAutomatica();
        super.dispose();
    }
}