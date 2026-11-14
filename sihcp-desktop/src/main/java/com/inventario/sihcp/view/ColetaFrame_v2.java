package com.inventario.sihcp.view;

// === SWING: Usar DAOs diretamente (sem Spring) ===
// import com.inventario.sihcp.dao.SalaDAO; // REMOVIDO - não é usado e causava problemas
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.SalaInventarioDAO;
import com.inventario.sihcp.dao.ParticipanteInventarioDAO;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.offline.ColetaOfflineService;
import com.inventario.sihcp.offline.OfflineManager;
import com.inventario.sihcp.util.SoundNotification;
import com.inventario.sihcp.view.ui.ModernComboBox;

import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.Component;
import java.awt.Cursor;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame para coleta de patrimônios - Versão 2
 * Interface melhorada com tabela de histórico sempre visível
 */
public class ColetaFrame_v2 extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(ColetaFrame_v2.class);
    private JComboBox<Sala> comboSalas;
    private JLabel lblInventarioAtual;

    // Aba "Itens Sem Patrimônio" - extraída para painel próprio
    private com.inventario.sihcp.view.coleta.ItensSemPatrimonioPanel itensSemPatrimonioPanel;

    // Componentes para estrutura de abas
    private JTabbedPane tabbedPane;
    private JPanel abaColetaNormal;

    // Aba "Itens Pendentes" - extraída para painel próprio
    private com.inventario.sihcp.view.coleta.ItensPendentesPanel itensPendentesPanel;

    private PatrimonioDAO patrimonioDAO;
    private ColetaDAO coletaDAO;
    private InventarioDAO inventarioDAO;
    private SalaInventarioDAO salaInventarioDAO;
    private ParticipanteInventarioDAO participanteInventarioDAO;

    // === OFFLINE: Serviços de modo offline ===
    private ColetaOfflineService coletaOfflineService;
    private OfflineManager offlineManager;

    private List<Sala> todasSalas = new ArrayList<>();
    private Usuario usuarioLogado; // Usuário logado para verificar permissões

    // OTIMIZAÇÃO: Tracking de SwingWorkers ativos para cancelamento ao trocar de sala
    private final List<SwingWorker<?, ?>> workersAtivos = new ArrayList<>();
    
    // === COMPONENTES DA ABA "COLETA NORMAL" ===
    
    // Componentes de busca
    private JTextField campoBusca;
    private JButton btnBuscar;
    
    // Botões de ação
    private JButton btnColetar;
    private JButton btnRemoverItem;
    private JButton btnFinalizarColeta;
    private JButton btnReabrirColeta;
    private JButton btnVerTodasColetas;
    
    // Tabela de histórico
    private JTable tabelaHistorico;
    private DefaultTableModel modeloTabelaHistorico;
    private JScrollPane scrollHistorico;
    
    // Panel de informações do item
    private JPanel panelInfoItem;
    private JLabel lblResumoSala;
    private JLabel lblNumeroItem;
    private JLabel lblDescricaoItem;
    private JLabel lblLocalizacaoOriginal;
    private JLabel lblEstadoOriginal;
    private JLabel lblStatusItem;
    
    // Formulário de coleta
    private JComboBox<String> comboEstado;
    private JTextArea campoObservacao;
    private JPanel panelFormulario;
    private JPanel panelBotoes;
    
    // Controles adicionais
    private JCheckBox chkColetaAutomatica;
    private boolean modoColetaAutomatica = false;
    private Patrimonio patrimonioSelecionado;
    private List<Component> componentesParaDesabilitar;
    private JLabel lblContagemColetas;
    
    // Variáveis de controle de tempo (para métricas)
    private Long inicioColetaAtual;
    private Long inicioScanAtual;
    private Long fimScanAtual;
    private String metodoColetaAtual; // "MANUAL" ou "LEITOR"
    
    // Buffer para leitura de código de barras
    private StringBuilder bufferCodigoBarras = new StringBuilder();
    private Long ultimaDigitacao;
    
    // Timers para animações visuais
    private Timer timerAnimacaoAtual;
    private Timer timerTextoAtual;

    public ColetaFrame_v2() {
        this(null, null); // Chama o construtor com usuário e mainFrame null para compatibilidade
    }

    public ColetaFrame_v2(Usuario usuarioLogado) {
        this(usuarioLogado, null); // Chama o construtor com mainFrame null
    }

    public ColetaFrame_v2(Usuario usuarioLogado, JFrame mainFrame) {
        this.usuarioLogado = usuarioLogado;
        // Debug: verificar se o usuário foi passado corretamente
        System.out.println("DEBUG: Usuario logado no ColetaFrame_v2: " +
                (usuarioLogado != null ? usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getPerfil() + ")"
                        : "null"));

        try {
            System.out.println("DEBUG: Iniciando initializeServices()...");
            try {
                initializeServices();
                System.out.println("DEBUG: initializeServices() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em initializeServices(): {}", e.getMessage(), e);
                throw e;
            }

            System.out.println("DEBUG: Iniciando initializeComponents()...");
            try {
                initializeComponents();
                System.out.println("DEBUG: initializeComponents() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em initializeComponents(): {}", e.getMessage(), e);
                throw e;
            }

            System.out.println("DEBUG: Iniciando setupLayout()...");
            try {
                setupLayout();
                System.out.println("DEBUG: setupLayout() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em setupLayout(): {}", e.getMessage(), e);
                throw e;
            }

            System.out.println("DEBUG: Iniciando setupEventListeners()...");
            try {
                setupEventListeners();
                System.out.println("DEBUG: setupEventListeners() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em setupEventListeners(): {}", e.getMessage(), e);
                throw e;
            }

            System.out.println("DEBUG: Iniciando carregarSalas()...");
            try {
                carregarSalas();
                System.out.println("DEBUG: carregarSalas() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em carregarSalas(): {}", e.getMessage(), e);
                throw e;
            }

            System.out.println("DEBUG: Iniciando carregarInventarioAtual()...");
            try {
                carregarInventarioAtual();
                System.out.println("DEBUG: carregarInventarioAtual() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em carregarInventarioAtual(): {}", e.getMessage(), e);
                throw e;
            }

            // Inicializar tabela vazia
            // A tabela será carregada quando uma sala for selecionada

            System.out.println("DEBUG: Configurando propriedades do frame...");
            setTitle("Gestão de Coletas Patrimoniais");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setExtendedState(JFrame.MAXIMIZED_BOTH);

            setLocationRelativeTo(null);

            System.out.println("DEBUG: ColetaFrame_v2 inicializado com sucesso!");

            // Ocultar MainFrame se foi fornecido
            if (mainFrame != null) {
                mainFrame.setVisible(false);
                System.out.println("DEBUG: MainFrame ocultado para foco na coleta");
            }

            // Adicionar listener para mostrar MainFrame quando fechar
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    // OTIMIZAÇÃO: Cancelar todos os workers ao fechar a janela
                    cancelarWorkersAtivos();
                    if (mainFrame != null) {
                        mainFrame.setVisible(true);
                        System.out.println("DEBUG: MainFrame restaurado após fechar ColetaFrame");
                    }
                }
            });

        } catch (Exception e) {
            LOG.error("ERRO durante inicialização do ColetaFrame_v2", e);
            throw new RuntimeException("Erro ao inicializar ColetaFrame_v2: " + e.getMessage(), e);
        }
    }

    private void initializeServices() {
        // REMOVIDO: new SalaDAO(); - causava erro de parsing de timestamp
        this.patrimonioDAO = new PatrimonioDAO();
        this.coletaDAO = new ColetaDAO();
        this.inventarioDAO = new InventarioDAO();
        this.salaInventarioDAO = new SalaInventarioDAO();
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();

        // === OFFLINE: Inicializar serviços de modo offline ===
        this.coletaOfflineService = ColetaOfflineService.getInstance();
        this.offlineManager = OfflineManager.getInstance();

        System.out.println("DEBUG: Serviços offline inicializados - Estado: " +
                offlineManager.getCurrentState());
    }

    private void initializeComponents() {
        // Combo de salas simples com barra de rolagem
        comboSalas = ModernComboBox.standard(new Sala[0]);
        comboSalas.setEditable(false); // Não editável - apenas seleção
        comboSalas.setPreferredSize(new Dimension(500, 35));
        comboSalas.setMaximumRowCount(20); // Mostrar até 20 salas por vez com scroll
        comboSalas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Sala sala) {
                    setText(sala.getIdentificacaoCompleta());
                } else if (value == null) {
                    setText("-- Selecione uma sala --");
                    setForeground(new Color(150, 150, 150));
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });

        // Labels de resumo
        lblInventarioAtual = new JLabel("Inventário: Carregando...");
        lblInventarioAtual.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblInventarioAtual.setForeground(new Color(0, 100, 0));
        
        // === CRIAR COMPONENTES DA ABA "COLETA NORMAL" ===
        
        // Componente de busca
        campoBusca = new JTextField(20);
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoBusca.setPreferredSize(new Dimension(300, 35));
        campoBusca.setToolTipText("Digite o número ou código do patrimônio");
        
        btnBuscar = createStyledButton("🔍 Buscar", new Color(52, 152, 219));
        btnBuscar.setPreferredSize(new Dimension(120, 35));
        
        // Botões de ação
        btnColetar = createStyledButton("✅ Registrar", new Color(40, 167, 69));
        btnColetar.setPreferredSize(new Dimension(200, 40));
        btnColetar.setEnabled(false);
        
        // Verificar permissões do usuário para remover itens
        boolean podeRemover = (usuarioLogado != null && 
            ("ADMIN".equals(usuarioLogado.getPerfil()) || "SUPERVISOR".equals(usuarioLogado.getPerfil())));
        
        btnRemoverItem = createStyledButton("🗑️ Remover", new Color(220, 53, 69));
        btnRemoverItem.setPreferredSize(new Dimension(150, 35));
        btnRemoverItem.setEnabled(false);
        btnRemoverItem.setVisible(podeRemover);
        btnRemoverItem.setToolTipText(podeRemover ? 
            "Remover item selecionado da coleta" : 
            "Apenas Admin/Supervisor podem remover itens");
        
        btnFinalizarColeta = createStyledButton("🏁 Finalizar Coleta", new Color(25, 135, 84));
        btnFinalizarColeta.setPreferredSize(new Dimension(180, 35));
        btnFinalizarColeta.setEnabled(false);
        
        // Verificar permissões do usuário para reabrir coletas
        boolean podeReabrir = (usuarioLogado != null && 
            ("ADMIN".equals(usuarioLogado.getPerfil()) || "SUPERVISOR".equals(usuarioLogado.getPerfil())));
        
        btnReabrirColeta = createStyledButton("🔄 Reabrir", new Color(255, 193, 7));
        btnReabrirColeta.setPreferredSize(new Dimension(150, 35));
        btnReabrirColeta.setEnabled(false);
        btnReabrirColeta.setVisible(podeReabrir);
        btnReabrirColeta.setToolTipText(podeReabrir ?
            "Reabrir coleta finalizada" :
            "Apenas Admin/Supervisor podem reabrir coletas");
        
        btnVerTodasColetas = createStyledButton("📋 Ver Todas", new Color(108, 117, 125));
        btnVerTodasColetas.setPreferredSize(new Dimension(150, 35));
        btnVerTodasColetas.setToolTipText("Ver todas as coletas registradas no sistema");
        
        // Tabela de histórico
        String[] colunas = {"Número", "Descrição", "Estado", "Data/Hora"};
        modeloTabelaHistorico = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaHistorico = new JTable(modeloTabelaHistorico);
        tabelaHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaHistorico.setRowHeight(25);
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        scrollHistorico = new JScrollPane(tabelaHistorico);
        scrollHistorico.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Panel de informações do item
        panelInfoItem = new JPanel(new GridBagLayout());
        panelInfoItem.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                "Informações do Item",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(44, 62, 80)),
            BorderFactory.createEmptyBorder(8, 12, 12, 12)));
        panelInfoItem.setBackground(Color.WHITE);
        panelInfoItem.setPreferredSize(new Dimension(0, 200));
        
        lblResumoSala = new JLabel("Selecione uma sala para iniciar a coleta");
        lblNumeroItem = new JLabel("-");
        lblDescricaoItem = new JLabel("-");
        lblLocalizacaoOriginal = new JLabel("-");
        lblEstadoOriginal = new JLabel("-");
        lblStatusItem = new JLabel("-");
        
        // Estilizar labels de informação
        lblNumeroItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDescricaoItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLocalizacaoOriginal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstadoOriginal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatusItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Formulário de coleta
        comboEstado = new JComboBox<>(new String[]{
            "BOM", "REGULAR", "RUIM", "PÉSSIMO", "NOVO"
        });
        comboEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboEstado.setPreferredSize(new Dimension(150, 30));
        comboEstado.setEnabled(false);
        
        campoObservacao = new JTextArea(2, 20);
        campoObservacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoObservacao.setLineWrap(true);
        campoObservacao.setWrapStyleWord(true);
        campoObservacao.setEnabled(false);
        
        // Label de contagem
        lblContagemColetas = new JLabel("Coletas nesta sala: 0");
        lblContagemColetas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblContagemColetas.setForeground(new Color(52, 152, 219));
        
        // Criar lista de componentes para habilitar/desabilitar
        componentesParaDesabilitar = new ArrayList<>();
        componentesParaDesabilitar.add(campoBusca);
        componentesParaDesabilitar.add(btnBuscar);
        componentesParaDesabilitar.add(btnColetar);
        componentesParaDesabilitar.add(comboEstado);
        componentesParaDesabilitar.add(campoObservacao);
        // Nota: btnRemoverItem e btnReabrirColeta são controlados separadamente
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Definir cores do tema moderno
        Color corFundo = new Color(248, 249, 250); // Fundo suave
        Color corTexto = new Color(44, 62, 80); // Texto escuro

        // Configurar fundo principal da janela
        getContentPane().setBackground(corFundo);

        // Criar estrutura de abas
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        // Usar fonte que suporta emojis (Segoe UI Emoji no Windows, ou fallback para Segoe UI Symbol)
        tabbedPane.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tabbedPane.setBackground(corFundo);
        tabbedPane.setForeground(corTexto);

        // Criar aba de Coleta Normal
        abaColetaNormal = new JPanel(new BorderLayout());
        abaColetaNormal.setBackground(corFundo);

        // Panel superior - seleção de sala e resumo com design moderno
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "Local de Coleta e Resumo",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)));
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setPreferredSize(new Dimension(0, 120));

        JPanel panelSelecaoSala = new JPanel(new GridBagLayout());
        panelSelecaoSala.setBackground(Color.WHITE);
        GridBagConstraints gbcSala = new GridBagConstraints();
        gbcSala.insets = new Insets(8, 8, 8, 8);
        gbcSala.anchor = GridBagConstraints.WEST;

        gbcSala.gridx = 0;
        gbcSala.gridy = 0;
        panelSelecaoSala.add(lblInventarioAtual, gbcSala);

        gbcSala.gridx = 0;
        gbcSala.gridy = 1;
        JLabel lblSalaAtual = new JLabel("Sala Atual:");
        lblSalaAtual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSalaAtual.setForeground(corTexto);
        panelSelecaoSala.add(lblSalaAtual, gbcSala);

        gbcSala.gridx = 1;
        gbcSala.gridy = 1;
        gbcSala.weightx = 1.0;
        gbcSala.fill = GridBagConstraints.HORIZONTAL;
        panelSelecaoSala.add(comboSalas, gbcSala);

        // Label de resumo abaixo do combo
        gbcSala.gridx = 0;
        gbcSala.gridy = 2;
        gbcSala.gridwidth = 2;
        gbcSala.weightx = 1.0;
        gbcSala.fill = GridBagConstraints.HORIZONTAL;
        gbcSala.insets = new Insets(5, 8, 5, 8);
        lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResumoSala.setForeground(new Color(100, 100, 100));
        panelSelecaoSala.add(lblResumoSala, gbcSala);

        panelSuperior.add(panelSelecaoSala, BorderLayout.CENTER);

        // Panel de busca com design moderno
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "Busca de Patrimônio",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)));
        panelBusca.setBackground(Color.WHITE);

        JLabel lblBusca = new JLabel("Buscar patrimônio (número ou código):");
        lblBusca.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBusca.setForeground(corTexto);
        panelBusca.add(lblBusca);
        panelBusca.add(campoBusca);
        panelBusca.add(btnBuscar);

        // Checkbox para modo de coleta automática com leitor
        chkColetaAutomatica = new JCheckBox("Coleta Automática (Leitor de Código)");
        chkColetaAutomatica.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkColetaAutomatica.setForeground(new Color(52, 152, 219));
        chkColetaAutomatica.setBackground(Color.WHITE);
        chkColetaAutomatica.setToolTipText("Ativa coleta automática ao escanear código de barras");
        chkColetaAutomatica.addActionListener(e -> {
            modoColetaAutomatica = chkColetaAutomatica.isSelected();
            if (modoColetaAutomatica) {
                campoBusca.requestFocusInWindow();
            }
        });
        panelBusca.add(chkColetaAutomatica);

        // Panel de histórico - SEMPRE VISÍVEL com design moderno
        JPanel panelHistorico = new JPanel(new BorderLayout());
        panelHistorico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "Histórico de Coleta da Sala",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)));
        panelHistorico.setBackground(Color.WHITE);

        // Panel para botões do histórico com design moderno
        JPanel panelBotoesHistorico = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoesHistorico.setBackground(Color.WHITE);

        // Adicionar botão Ver Todas as Coletas
        panelBotoesHistorico.add(btnVerTodasColetas);

        // Adicionar o botão remover se visível
        if (btnRemoverItem.isVisible()) {
            panelBotoesHistorico.add(btnRemoverItem);
        }

        panelBotoesHistorico.add(btnFinalizarColeta);

        // Adicionar o botão reabrir se visível (apenas admin/supervisor)
        if (btnReabrirColeta.isVisible()) {
            panelBotoesHistorico.add(btnReabrirColeta);
        }

        // Painel inferior com contagem e botões
        JPanel panelInferiorHistorico = new JPanel(new BorderLayout());
        panelInferiorHistorico.setBackground(Color.WHITE);
        panelInferiorHistorico.add(lblContagemColetas, BorderLayout.WEST);
        panelInferiorHistorico.add(panelBotoesHistorico, BorderLayout.EAST);

        panelHistorico.add(scrollHistorico, BorderLayout.CENTER);
        panelHistorico.add(panelInferiorHistorico, BorderLayout.SOUTH);

        // Panel direito - informações e ações com design moderno
        JPanel panelDireito = new JPanel(new BorderLayout());
        panelDireito.setPreferredSize(new Dimension(450, 0));
        panelDireito.setBackground(corFundo);
        panelDireito.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Configurar painel de informações do item
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // Espaçamento compacto
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNumeroLabel = new JLabel("Número:");
        lblNumeroLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNumeroLabel.setForeground(corTexto);
        panelInfoItem.add(lblNumeroLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelInfoItem.add(lblNumeroItem, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblDescricaoLabel = new JLabel("Descrição:");
        lblDescricaoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDescricaoLabel.setForeground(corTexto);
        panelInfoItem.add(lblDescricaoLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelInfoItem.add(lblDescricaoItem, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblLocalizacaoLabel = new JLabel("Localização Original:");
        lblLocalizacaoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLocalizacaoLabel.setForeground(corTexto);
        panelInfoItem.add(lblLocalizacaoLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelInfoItem.add(lblLocalizacaoOriginal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblEstadoOriginalLabel = new JLabel("Estado Original:");
        lblEstadoOriginalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoOriginalLabel.setForeground(corTexto);
        panelInfoItem.add(lblEstadoOriginalLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelInfoItem.add(lblEstadoOriginal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblStatusLabel = new JLabel("Status:");
        lblStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatusLabel.setForeground(corTexto);
        panelInfoItem.add(lblStatusLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelInfoItem.add(lblStatusItem, gbc);

        // Panel de ações de coleta com design moderno
        JPanel panelAcoes = new JPanel(new BorderLayout());
        panelAcoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "Ações de Coleta",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));
        panelAcoes.setBackground(Color.WHITE);
        panelAcoes.setPreferredSize(new Dimension(0, 180)); // Tamanho compacto

        panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8); // Reduzido espaçamento
        gbc.anchor = GridBagConstraints.WEST;

        // Checkbox para item sem etiqueta removido - há aba dedicada para isso
        // NOTA: O painel legado "panelSemEtiqueta" (invisível) foi removido junto com
        // a extração da aba "Itens Sem Patrimônio" para ItensSemPatrimonioPanel.

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        JLabel lblEstado = new JLabel("Estado Atual do Item:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstado.setForeground(corTexto);
        panelFormulario.add(lblEstado, gbc);
        gbc.gridx = 1;
        panelFormulario.add(comboEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblObservacoes.setForeground(corTexto);
        panelFormulario.add(lblObservacoes, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        // Campo de observação com altura fixa
        JScrollPane scrollObservacao = new JScrollPane(campoObservacao);
        scrollObservacao.setPreferredSize(new Dimension(200, 40));
        scrollObservacao.setMinimumSize(new Dimension(150, 35));
        panelFormulario.add(scrollObservacao, gbc);

        // Painel para os botões de ação
        panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelBotoes.setBackground(Color.WHITE);

        panelBotoes.add(btnColetar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Permitir que este componente ocupe espaço vertical restante
        gbc.anchor = GridBagConstraints.SOUTH; // Ancorar na parte inferior
        gbc.insets = new Insets(15, 8, 10, 8); // Mais espaço acima do botão
        panelFormulario.add(panelBotoes, gbc);

        panelAcoes.add(panelFormulario, BorderLayout.CENTER);

        // Panel central principal - layout lateral original
        JPanel panelCentralPrincipal = new JPanel(new BorderLayout());
        panelCentralPrincipal.setBackground(corFundo);

        // Panel esquerdo - busca e histórico
        JPanel panelEsquerdo = new JPanel(new BorderLayout());
        panelEsquerdo.setPreferredSize(new Dimension(600, 0));
        panelEsquerdo.setBackground(corFundo);
        panelEsquerdo.add(panelBusca, BorderLayout.NORTH);
        panelEsquerdo.add(panelHistorico, BorderLayout.CENTER);

        // Panel direito - informações do item e formulário de coleta
        // Usar BoxLayout para empilhar os painéis verticalmente
        panelDireito.setLayout(new BoxLayout(panelDireito, BoxLayout.Y_AXIS));
        panelDireito.add(panelInfoItem);
        panelDireito.add(Box.createVerticalStrut(10)); // Espaço entre painéis
        panelDireito.add(panelAcoes);

        panelCentralPrincipal.add(panelEsquerdo, BorderLayout.CENTER);
        panelCentralPrincipal.add(panelDireito, BorderLayout.EAST);

        // Adicionar componentes à aba de Coleta Normal
        abaColetaNormal.add(panelSuperior, BorderLayout.NORTH);
        abaColetaNormal.add(panelCentralPrincipal, BorderLayout.CENTER);

        // Criar aba para Itens Sem Patrimônio - painel próprio
        itensSemPatrimonioPanel = new com.inventario.sihcp.view.coleta.ItensSemPatrimonioPanel(
                patrimonioDAO,
                coletaDAO,
                inventarioDAO,
                coletaOfflineService,
                offlineManager,
                usuarioLogado,
                () -> (Sala) comboSalas.getSelectedItem(),
                this::registrarWorker);

        // Criar aba para Itens Pendentes (não coletados) - painel próprio
        itensPendentesPanel = new com.inventario.sihcp.view.coleta.ItensPendentesPanel(
                patrimonioDAO,
                inventarioDAO,
                this::irParaColeta,
                this::registrarWorker);

        // Adicionar abas ao TabbedPane
        tabbedPane.addTab("📋 Coleta Normal", abaColetaNormal);
        tabbedPane.addTab("⏳ Itens Pendentes", itensPendentesPanel);
        tabbedPane.addTab("🏷️ Itens Sem Patrimônio", itensSemPatrimonioPanel);

        // Adicionar listener para controlar habilitação de componentes por aba
        tabbedPane.addChangeListener(e -> {
            int abaSelecionada = tabbedPane.getSelectedIndex();
            habilitarComponentesPorAba(abaSelecionada);
        });

        // Adicionar TabbedPane à janela principal
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Navega para a aba de Coleta Normal e dispara a busca pelo número informado.
     * Usado como callback do {@link com.inventario.sihcp.view.coleta.ItensPendentesPanel}
     * quando o usuário dá duplo-clique em um item pendente.
     */
    private void irParaColeta(String numero) {
        tabbedPane.setSelectedIndex(0);
        campoBusca.setText(numero);
        campoBusca.requestFocus();
        buscarPatrimonio();
    }

    /**
     * Recarrega os itens pendentes da aba (delegado ao painel próprio).
     */
    private void carregarItensPendentes() {
        if (itensPendentesPanel != null) {
            itensPendentesPanel.carregarParaSala((Sala) comboSalas.getSelectedItem());
        }
    }

    private void setupEventListeners() {
        comboSalas.addActionListener(e -> {
            carregarDadosSala();
            // Se a aba de pendentes estiver selecionada, recarregar os pendentes
            if (tabbedPane != null && tabbedPane.getSelectedIndex() == 1) {
                carregarItensPendentes();
            }
        });
        btnBuscar.addActionListener(e -> buscarPatrimonio());
        campoBusca.addActionListener(e -> buscarPatrimonio());
        btnColetar.addActionListener(e -> {
            animarBotaoRegistrar();
            registrarItemEncontrado();
        });
        btnRemoverItem.addActionListener(e -> removerItemSelecionado());
        // chkSemEtiqueta.addActionListener removido - checkbox não existe mais

        // NOTA: Listeners de busca por descrição, itens agrupados e resultados foram
        // movidos para ItensSemPatrimonioPanel.

        // Listener para o botão de finalização da coleta
        btnFinalizarColeta.addActionListener(e -> finalizarColetaSala());

        // Listener para o botão de reabertura da coleta (apenas admin/supervisor)
        if (btnReabrirColeta.isVisible()) {
            btnReabrirColeta.addActionListener(e -> reabrirColetaSala());
        }

        // Listener para o botão de ver todas as coletas
        btnVerTodasColetas.addActionListener(e -> mostrarTodasColetas());

        // Listener para seleção na tabela de histórico
        // ✅ REGRA: btnRemover só habilita se:
        //    1. Há uma linha selecionada na tabela
        //    2. Usuário é Admin ou Supervisor
        //    3. Botão está visível (já foi verificado na criação)
        tabelaHistorico.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linhaSelecionada = tabelaHistorico.getSelectedRow();
                
                // Habilitar botão remover APENAS se:
                // - Há linha selecionada
                // - Botão está visível (usuário tem permissão)
                if (btnRemoverItem.isVisible()) {
                    btnRemoverItem.setEnabled(linhaSelecionada != -1);
                    
                    if (linhaSelecionada != -1) {
                        btnRemoverItem.setToolTipText("Remover item selecionado da coleta");
                    } else {
                        btnRemoverItem.setToolTipText("Selecione um item na tabela para remover");
                    }
                }
                
                // NOTA: btnFinalizarColeta e btnReabrirColeta são controlados em carregarDadosSala()
                // baseado no status da sala (aberta/finalizada) e modo de operação (online/offline)
            }
        });

        // Event listener para seleção na tabela de resultados movido para
        // ItensSemPatrimonioPanel.

        // NOTA: Listeners de tabelaItensAgrupados já registrados acima — NÃO duplicar
    }

    private void carregarInventarioAtual() {
        try {
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo != null) {
                lblInventarioAtual.setText("Inventário: " + inventarioAtivo.getNome());
            } else {
                lblInventarioAtual.setText("Inventário: Nenhum inventário ativo");
            }
        } catch (SQLException | RuntimeException e) {
            lblInventarioAtual.setText("Inventário: Erro ao carregar");
        }
    }

    // Métodos auxiliares (copiados da classe original)
    private void carregarSalas() {
        carregarSalasAsync();
    }

    /**
     * Carrega as salas de forma assíncrona em uma única consulta.
     * Mantém a UI responsiva (SwingWorker) sem carregamento em lotes.
     */
    private void carregarSalasAsync() {
        // Desabilitar combo e limpar
        comboSalas.setEnabled(false);
        comboSalas.removeAllItems();
        comboSalas.addItem(null); // Item placeholder
        todasSalas = new ArrayList<>();

        // Mudar cursor para indicar carregamento
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<Sala>, Void> worker = new SwingWorker<>() {
            private Inventario inventarioAtivo = null;
            private boolean usouFallback = false;

            @Override
            protected List<Sala> doInBackground() throws Exception {
                inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                if (inventarioAtivo == null) {
                    LOG.warn("Nenhum inventário ativo encontrado");
                    return new ArrayList<>();
                }

                LOG.info("Inventário ativo: {} (ID: {})", inventarioAtivo.getNome(), inventarioAtivo.getId());

                // Carregar todas as salas do inventário em uma única consulta
                List<Sala> salas = salaInventarioDAO.buscarSalasAbertasParaColeta(inventarioAtivo.getId());

                // Fallback: se não houver salas vinculadas, carregar todas as ativas
                if (salas.isEmpty()) {
                    LOG.info("Nenhuma sala vinculada ao inventário; usando fallback (todas as salas ativas)");
                    usouFallback = true;
                    salas = salaInventarioDAO.buscarTodasSalasAtivas();
                }

                return salas;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                comboSalas.setEnabled(true);

                try {
                    List<Sala> salas = get();
                    for (Sala sala : salas) {
                        todasSalas.add(sala);
                        comboSalas.addItem(sala);
                    }

                    LOG.info("Carregamento finalizado: {} salas carregadas", todasSalas.size());

                    if (inventarioAtivo == null) {
                        JOptionPane.showMessageDialog(ColetaFrame_v2.this,
                                "Nenhum inventário ativo encontrado.\nNão é possível realizar coletas.",
                                "Aviso", JOptionPane.WARNING_MESSAGE);
                    } else if (todasSalas.isEmpty()) {
                        JOptionPane.showMessageDialog(ColetaFrame_v2.this,
                                "Nenhuma sala ativa encontrada no sistema.\nCadastre salas antes de realizar coletas.",
                                "Aviso", JOptionPane.WARNING_MESSAGE);
                    } else if (usouFallback) {
                        JOptionPane.showMessageDialog(ColetaFrame_v2.this,
                                "Carregadas " + todasSalas.size() + " salas ativas.\n" +
                                "Nota: Estas salas não estão vinculadas ao inventário atual,\n" +
                                "mas você pode realizar coletas normalmente.",
                                "Informação", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    tratarErroCarregamentoSalas("Operação interrompida");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    tratarErroCarregamentoSalas(causa != null ? causa.getMessage() : e.getMessage());
                }
            }
        };

        registrarWorker(worker);
        worker.execute();
    }

    /**
     * Trata erros de carregamento de salas com opção de retry manual.
     */
    private void tratarErroCarregamentoSalas(String mensagemErro) {
        todasSalas = new ArrayList<>();
        comboSalas.setEnabled(true);
        LOG.error("Erro ao carregar salas: {}", mensagemErro);

        int opcao = JOptionPane.showOptionDialog(this,
                "Erro ao carregar salas:\n" + mensagemErro,
                "Erro ao Carregar Salas",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Tentar Novamente", "Cancelar"},
                "Tentar Novamente");

        if (opcao == JOptionPane.YES_OPTION) {
            carregarSalasAsync(); // Retry manual
        }
    }
    


    private void desabilitarComponentes() {
        for (Component comp : componentesParaDesabilitar) {
            comp.setEnabled(false);
        }
    }

    private void habilitarComponentes() {
        for (Component comp : componentesParaDesabilitar) {
            // ✅ EXCEÇÃO: btnRemoverItem NÃO deve ser habilitado aqui
            // Ele só é habilitado quando uma linha da tabela é selecionada
            if (comp == btnRemoverItem) {
                continue; // Pular btnRemoverItem
            }
            // ✅ EXCEÇÃO: btnReabrirColeta NÃO deve ser habilitado aqui
            // Ele só é habilitado quando a sala está FINALIZADA (controlado em carregarDadosSala)
            // REGRA: btnReabrir e btnFinalizar são MUTUAMENTE EXCLUSIVOS
            if (comp == btnReabrirColeta) {
                continue; // Pular btnReabrirColeta - controlado separadamente
            }
            comp.setEnabled(true);
        }
    }

    /**
     * Configura a aparência de um botão para manter cores quando desabilitado
     */
    private void configurarBotaoComEstiloDesabilitado(JButton botao, Color corHabilitada, Color corDesabilitada) {
        // Armazenar cores originais
        Color corOriginalHabilitada = corHabilitada;
        Color corOriginalDesabilitada = corDesabilitada;

        // Adicionar listener para mudanças de estado
        botao.addPropertyChangeListener("enabled", evt -> {
            if ((Boolean) evt.getNewValue()) {
                // Habilitado
                botao.setBackground(corOriginalHabilitada);
                botao.setForeground(Color.WHITE);
            } else {
                // Desabilitado
                botao.setBackground(corOriginalDesabilitada);
                botao.setForeground(new Color(180, 180, 180));
            }
        });

        // Aplicar estado inicial
        if (botao.isEnabled()) {
            botao.setBackground(corOriginalHabilitada);
            botao.setForeground(Color.WHITE);
        } else {
            botao.setBackground(corOriginalDesabilitada);
            botao.setForeground(new Color(180, 180, 180));
        }
    }

    /**
     * Habilita/desabilita componentes específicos baseado na aba selecionada
     * 
     * @param abaSelecionada índice da aba selecionada (0 = Coleta Normal, 1 = Itens Pendentes, 2 = Itens Sem Patrimônio)
     */
    private void habilitarComponentesPorAba(int abaSelecionada) {
        System.out.println("DEBUG: habilitarComponentesPorAba chamado com aba: " + abaSelecionada);
        
        // Desabilitar componentes de todas as abas primeiro
        desabilitarComponentesAbaItensSemPatrimonio();
        desabilitarComponentesAbaItensPendentes();
        
        switch (abaSelecionada) {
            case 0 -> {
                // Aba "Coleta Normal" selecionada
                // Habilitar componentes da coleta normal
                campoBusca.setEnabled(true);
                btnBuscar.setEnabled(true);
                System.out.println("DEBUG: Aba 0 - campoBusca e btnBuscar habilitados");
                campoObservacao.setEnabled(true);
                comboEstado.setEnabled(true);
                // Configurar botão coletar para modo normal
                btnColetar.setText("Registrar");
                btnColetar.setEnabled(patrimonioSelecionado != null);
                // Configurar botão remover para modo normal
                btnRemoverItem.setEnabled(patrimonioSelecionado != null && btnRemoverItem.isVisible());
            }
            case 1 -> {
                // Aba "Itens Pendentes" selecionada
                // Desabilitar componentes da coleta normal
                campoBusca.setEnabled(false);
                btnBuscar.setEnabled(false);
                btnColetar.setEnabled(false);
                btnRemoverItem.setEnabled(false);
                System.out.println("DEBUG: Aba 1 - Itens Pendentes selecionada");
                // Habilitar componentes do painel de pendentes
                if (itensPendentesPanel != null) {
                    itensPendentesPanel.setComponentesAtivos(true);
                }
                // Carregar itens pendentes automaticamente
                carregarItensPendentes();
            }
            case 2 -> {
                // Aba "Itens Sem Patrimônio" selecionada
                // Desabilitar componentes da coleta normal
                campoBusca.setEnabled(false);
                btnBuscar.setEnabled(false);
                btnColetar.setEnabled(false);
                btnRemoverItem.setEnabled(false);
                System.out.println("DEBUG: Aba 2 - Itens Sem Patrimônio selecionada");
                // Habilitar componentes específicos da aba "Itens Sem Patrimônio"
                if (itensSemPatrimonioPanel != null) {
                    itensSemPatrimonioPanel.setComponentesAtivos(true);
                    // Carregar itens agrupados automaticamente
                    itensSemPatrimonioPanel.carregarAgrupados();
                    // Carregar itens sem patrimônio da sala atual
                    itensSemPatrimonioPanel.carregarItens();
                    // Focar na descrição
                    itensSemPatrimonioPanel.focarDescricao();
                }
            }
            default -> {
            }
        }

        // Revalidar layout
        revalidate();
        repaint();
    }

    /**
     * Desabilita todos os componentes da aba "Itens Sem Patrimônio"
     */
    private void desabilitarComponentesAbaItensSemPatrimonio() {
        if (itensSemPatrimonioPanel != null) {
            itensSemPatrimonioPanel.setComponentesAtivos(false);
        }
    }

    /**
     * Desabilita todos os componentes da aba "Itens Pendentes"
     */
    private void desabilitarComponentesAbaItensPendentes() {
        if (itensPendentesPanel != null) {
            itensPendentesPanel.setComponentesAtivos(false);
        }
    }

    /**
     * OTIMIZAÇÃO: Cancela todos os SwingWorkers ativos para evitar acúmulo
     * de threads em background ao trocar de sala repetidamente.
     */
    private void cancelarWorkersAtivos() {
        int cancelados = 0;
        for (SwingWorker<?, ?> w : workersAtivos) {
            if (!w.isDone() && !w.isCancelled()) {
                w.cancel(true);
                cancelados++;
            }
        }
        workersAtivos.clear();
        if (cancelados > 0) {
            System.out.println("DEBUG: " + cancelados + " worker(s) cancelado(s) ao trocar de sala");
        }
    }

    /**
     * OTIMIZAÇÃO: Registra um SwingWorker para tracking e cancelamento futuro.
     */
    private <T, V> void registrarWorker(SwingWorker<T, V> worker) {
        // Limpar workers já finalizados para não acumular referências
        workersAtivos.removeIf(w -> w.isDone() || w.isCancelled());
        workersAtivos.add(worker);
    }

    private void carregarDadosSala() {
        System.out.println("DEBUG: carregarDadosSala() chamado");

        // OTIMIZAÇÃO: Cancelar workers anteriores ao trocar de sala
        cancelarWorkersAtivos();

        Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
        System.out.println("DEBUG: Sala selecionada: "
                + (salaSelecionada != null ? salaSelecionada.getIdentificacaoCompleta() : "null"));

        if (salaSelecionada == null) {
            lblResumoSala.setText("Selecione uma sala para iniciar a coleta");
            desabilitarComponentes();
            btnFinalizarColeta.setEnabled(false);
            
            // ✅ Desabilitar botões de ação quando não há sala selecionada
            btnRemoverItem.setEnabled(false);
            if (btnReabrirColeta.isVisible()) {
                btnReabrirColeta.setEnabled(false);
            }

            // Limpar campo de localização na aba de itens sem patrimônio
            if (itensSemPatrimonioPanel != null) {
                itensSemPatrimonioPanel.setLocalizacao("");
            }
            
            // Resetar contagem de coletas e limpar tabela de histórico
            atualizarContagemColetas(0);
            modeloTabelaHistorico.setRowCount(0);

            return;
        }

        // Atualizar campo de localização na aba de itens sem patrimônio IMEDIATAMENTE
        if (itensSemPatrimonioPanel != null) {
            itensSemPatrimonioPanel.setLocalizacao(salaSelecionada.getIdentificacaoCompleta());
            System.out.println(
                    "DEBUG: Campo de localização atualizado para: " + salaSelecionada.getIdentificacaoCompleta());
        }

        try {
            // Buscar inventário ativo
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            
            // Verificar se a sala está finalizada
            boolean salaFinalizada = false;
            int totalItensColetados = 0;
            
            if (inventarioAtivo != null) {
                salaFinalizada = salaInventarioDAO.isColetaFinalizada(
                        salaSelecionada.getIdSala(), inventarioAtivo.getId());
                
                // Contar itens coletados
                try {
                    int[] estatisticas = coletaDAO.contarColetasPorSala(
                            inventarioAtivo.getId(), salaSelecionada.getNumeroSala());
                    totalItensColetados = estatisticas[0];
                } catch (SQLException | RuntimeException e) {
                    System.err.println("Erro ao contar coletas: " + e.getMessage());
                }
            }

            // Carregar histórico usando o número da sala (consistente com dados no banco)
            carregarHistoricoColeta(salaSelecionada.getNumeroSala());

            // Carregar itens sem patrimônio se estiver na aba correspondente (índice 2)
            if (tabbedPane.getSelectedIndex() == 2) {
                if (itensSemPatrimonioPanel != null) {
                    itensSemPatrimonioPanel.carregarItens();
                }
            }
            // Carregar itens pendentes se estiver na aba correspondente (índice 1)
            if (tabbedPane.getSelectedIndex() == 1) {
                carregarItensPendentes();
            }

            // Verificar se está em modo offline - botões finalizar/reabrir só funcionam online
            boolean modoOffline = offlineManager.isOperatingOffline();
            
            System.out.println("DEBUG: carregarDadosSala() - salaFinalizada=" + salaFinalizada + 
                             ", modoOffline=" + modoOffline + 
                             ", btnReabrirColeta.isVisible()=" + btnReabrirColeta.isVisible());
            
            // ✅ REGRA CRÍTICA: btnReabrir e btnFinalizar são MUTUAMENTE EXCLUSIVOS
            // - Se sala FINALIZADA: btnFinalizar=false, btnReabrir=true (se online E admin/supervisor)
            // - Se sala ABERTA: btnFinalizar=true (se online), btnReabrir=false
            // - Em modo OFFLINE: ambos ficam desabilitados
            // - btnRemover: SEMPRE desabilitado até que uma linha seja selecionada na tabela
            
            if (salaFinalizada) {
                // ========== SALA FINALIZADA ==========
                lblResumoSala.setText(String.format("✅ Sala FINALIZADA: %s | %d itens coletados%s",
                        salaSelecionada.getIdentificacaoCompleta(), totalItensColetados,
                        modoOffline ? " [OFFLINE]" : ""));
                lblResumoSala.setForeground(new Color(40, 167, 69)); // Verde
                
                // Botão Finalizar: SEMPRE desabilitado (sala já está finalizada)
                btnFinalizarColeta.setText("✅ Finalizada");
                btnFinalizarColeta.setBackground(new Color(108, 117, 125)); // Cinza
                btnFinalizarColeta.setEnabled(false);
                btnFinalizarColeta.setToolTipText("Sala já finalizada");
                
                // Botão Reabrir: habilitado APENAS se:
                // 1. Botão está visível (usuário é admin/supervisor - verificado na criação)
                // 2. Sala está FINALIZADA (condição atual) ✅
                // 3. Modo ONLINE (requer conexão com servidor)
                // REGRA: btnReabrir só fica ativo quando btnFinalizar está INATIVO (sala finalizada)
                if (btnReabrirColeta.isVisible()) {
                    boolean podeReabrir = !modoOffline; // Online E sala finalizada (já estamos no bloco salaFinalizada=true)
                    btnReabrirColeta.setEnabled(podeReabrir);
                    btnReabrirColeta.setBackground(new Color(255, 193, 7)); // Amarelo/laranja
                    
                    if (modoOffline) {
                        btnReabrirColeta.setToolTipText("⚠️ Reabrir sala requer conexão com o servidor");
                    } else {
                        btnReabrirColeta.setToolTipText("Reabrir sala finalizada para permitir novas coletas");
                    }
                    
                    System.out.println("DEBUG: btnReabrir habilitado=" + podeReabrir + 
                                     " (modoOffline=" + modoOffline + ", salaFinalizada=true)");
                }
                
                // Botão Remover: SEMPRE desabilitado (nenhuma linha selecionada inicialmente)
                btnRemoverItem.setEnabled(false);
                btnRemoverItem.setToolTipText("Selecione um item na tabela para remover");
                
                // Desabilitar campos de coleta (sala finalizada)
                campoBusca.setEnabled(false);
                btnBuscar.setEnabled(false);
                btnColetar.setEnabled(false);
                campoObservacao.setEnabled(false);
                comboEstado.setEnabled(false);
                
                System.out.println("DEBUG: ✅ Sala FINALIZADA - btnFinalizar=false, btnReabrir=" + 
                                  (!modoOffline) + ", btnRemover=false (aguardando seleção)" + 
                                  (modoOffline ? " [OFFLINE]" : ""));
                
            } else {
                // ========== SALA ABERTA ==========
                lblResumoSala.setText(String.format("Coletando em: %s | %d itens coletados%s",
                        salaSelecionada.getIdentificacaoCompleta(), totalItensColetados,
                        modoOffline ? " [OFFLINE]" : ""));
                lblResumoSala.setForeground(new Color(100, 100, 100)); // Cinza padrão
                
                // Botão Finalizar: habilitado APENAS se online
                btnFinalizarColeta.setText("🏁 Finalizar");
                btnFinalizarColeta.setBackground(new Color(40, 167, 69)); // Verde
                btnFinalizarColeta.setEnabled(!modoOffline);
                
                if (modoOffline) {
                    btnFinalizarColeta.setToolTipText("⚠️ Finalizar sala requer conexão com o servidor");
                } else {
                    btnFinalizarColeta.setToolTipText("Marcar a coleta desta sala como finalizada");
                }
                
                // Botão Reabrir: SEMPRE desabilitado quando sala está ABERTA
                // REGRA: btnReabrir só fica ativo quando btnFinalizar está INATIVO (sala finalizada)
                // Como a sala está ABERTA, btnFinalizar está ATIVO, então btnReabrir deve estar INATIVO
                if (btnReabrirColeta.isVisible()) {
                    btnReabrirColeta.setEnabled(false); // SEMPRE false quando sala está aberta
                    btnReabrirColeta.setBackground(new Color(200, 160, 10)); // Cor mais escura para indicar inativo
                    btnReabrirColeta.setToolTipText("Sala já está aberta para coleta - não é necessário reabrir");
                    System.out.println("DEBUG: btnReabrir DESABILITADO - sala aberta (btnFinalizar está ativo)");
                }
                
                // Botão Remover: SEMPRE desabilitado inicialmente (nenhuma linha selecionada)
                btnRemoverItem.setEnabled(false);
                btnRemoverItem.setToolTipText("Selecione um item na tabela para remover");
                
                // Habilitar componentes de coleta (EXCETO btnRemover que depende de seleção)
                habilitarComponentes();
                
                // Habilitar componentes baseado na aba atualmente selecionada
                int abaSelecionada = tabbedPane.getSelectedIndex();
                habilitarComponentesPorAba(abaSelecionada);
                
                System.out.println("DEBUG: 🔓 Sala ABERTA - btnFinalizar=" + (!modoOffline) + 
                                  ", btnReabrir=false (sala aberta, btnFinalizar ativo), btnRemover=false (aguardando seleção)" + 
                                  (modoOffline ? " [OFFLINE]" : ""));
            }

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao carregar dados da sala: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados da sala: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Limite de registros para performance
    private static final int LIMITE_HISTORICO = 50;  // Carregar apenas últimos 50 patrimônios
    private static final int LIMITE_TODAS_COLETAS = 500; // Limite para visualização de todas as coletas
    
    /**
     * Mostra todas as coletas do inventário atual em um diálogo separado.
     * Delegado para {@link com.inventario.sihcp.view.coleta.TodasColetasDialog}.
     */
    private void mostrarTodasColetas() {
        new com.inventario.sihcp.view.coleta.TodasColetasDialog(
                this, inventarioDAO, coletaDAO, LIMITE_TODAS_COLETAS, this::registrarWorker)
                .setVisible(true);
    }
    
    /**
     * Carrega histórico de coletas de forma ASSÍNCRONA.
     * - Modo offline: carrega do SQLite local
     * - Modo online: carrega do PostgreSQL em uma única consulta
     */
    private void carregarHistoricoColeta(String identificacaoSala) {
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            return;
        }
        
        // Modo offline usa carregamento simples (SQLite é rápido)
        if (offlineManager.isOperatingOffline()) {
            carregarHistoricoOffline(identificacaoSala, salaAtual);
            return;
        }
        
        // Modo online: carregamento único
        carregarHistoricoOnline(identificacaoSala, salaAtual);
    }
    
    /**
     * Carrega histórico do SQLite local (modo offline - rápido).
     */
    private void carregarHistoricoOffline(String identificacaoSala, Sala salaAtual) {
        lblResumoSala.setText("⏳ Carregando histórico local...");
        tabelaHistorico.setEnabled(false);
        
        SwingWorker<List<Coleta>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Coleta> doInBackground() throws Exception {
                return coletaOfflineService.buscarColetasPorLocalizacao(identificacaoSala);
            }
            
            @Override
            protected void done() {
                try {
                    List<Coleta> coletas = get();
                    atualizarTabelaHistorico(coletas);
                    atualizarContagemColetas(coletas.size());
                    lblResumoSala.setText(String.format("Coletando em: %s (%d itens) [OFFLINE]", 
                        salaAtual.getIdentificacaoCompleta(), coletas.size()));
                    tabelaHistorico.setEnabled(true);
                } catch (InterruptedException | ExecutionException e) {
                    tratarErroHistorico(e.getMessage(), identificacaoSala);
                }
            }
        };
        registrarWorker(worker);
        worker.execute();
    }
    
    /**
     * Carrega o histórico do PostgreSQL em uma única consulta (até LIMITE_HISTORICO).
     * Mescla as coletas locais pendentes do serviço offline, preservando o filtro
     * por inventário ativo.
     */
    private void carregarHistoricoOnline(String identificacaoSala, Sala salaAtual) {
        modeloTabelaHistorico.setRowCount(0);
        tabelaHistorico.setEnabled(false);
        lblResumoSala.setText("⏳ Carregando histórico...");

        SwingWorker<List<Coleta>, Void> worker = new SwingWorker<>() {
            private int totalColetas = 0;

            @Override
            protected List<Coleta> doInBackground() throws Exception {
                // Resolver inventário ativo para filtrar as coletas do inventário corrente
                Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
                Integer idInventarioAtivo = inventarioAtivo != null ? inventarioAtivo.getId() : null;

                totalColetas = coletaDAO.contarColetasPorNumeroSala(identificacaoSala, idInventarioAtivo);

                // Buscar do servidor em uma única consulta (já ordenado por data DESC)
                List<Coleta> coletas = new ArrayList<>(
                        coletaDAO.buscarColetasPorNumeroSalaPaginado(
                                identificacaoSala, 0, LIMITE_HISTORICO, idInventarioAtivo));

                // Mesclar coletas locais pendentes (modo híbrido: online com pendências)
                try {
                    List<Coleta> coletasLocais = coletaOfflineService.buscarColetasPorLocalizacao(identificacaoSala);
                    for (Coleta coletaLocal : coletasLocais) {
                        boolean jaExiste = coletas.stream()
                                .anyMatch(c -> c.getNumeroPatrimonio() != null &&
                                        c.getNumeroPatrimonio().equals(coletaLocal.getNumeroPatrimonio()));
                        if (!jaExiste) {
                            coletas.add(0, coletaLocal); // pendentes no topo
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Erro ao mesclar coletas locais pendentes: {}", e.getMessage());
                }

                return coletas;
            }

            @Override
            protected void done() {
                try {
                    List<Coleta> coletas = get();
                    atualizarTabelaHistorico(coletas);
                    atualizarContagemColetas(coletas.size());

                    String sufixo = totalColetas > LIMITE_HISTORICO
                            ? " (mostrando " + LIMITE_HISTORICO + " de " + totalColetas + ")"
                            : "";
                    lblResumoSala.setText(String.format("Coletando em: %s (%d itens)%s",
                            salaAtual.getIdentificacaoCompleta(), coletas.size(), sufixo));
                    tabelaHistorico.setEnabled(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    tratarErroHistorico("Operação interrompida", identificacaoSala);
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    tratarErroHistorico(causa != null ? causa.getMessage() : e.getMessage(), identificacaoSala);
                }
            }
        };

        registrarWorker(worker);
        worker.execute();
    }
    
    /**
     * Insere uma nova coleta no INÍCIO da tabela (usado após registrar uma coleta).
     * Novas coletas devem aparecer no topo pois são as mais recentes.
     */
    private void inserirColetaNoInicio(Coleta coleta) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Colunas: { "Data/Hora", "Patrimônio", "Descrição", "Estado", "ID" }
        String data = coleta.getDataColeta() != null ? sdf.format(coleta.getDataColeta()) : "-";
        String numero = coleta.getNumeroPatrimonio() != null ? coleta.getNumeroPatrimonio() : "-";
        
        // ✅ OTIMIZADO: Usar descrição que já veio do JOIN no DAO (sem query adicional)
        // A descrição já vem preenchida pelo método criarColetaOtimizadaFromResultSet
        String descricao = coleta.getDescricaoPatrimonio();
        if (descricao == null || descricao.isEmpty()) {
            // Fallback: usar descrição de item sem etiqueta
            descricao = coleta.getDescricaoItemSemEtiqueta();
        }
        if (descricao == null || descricao.isEmpty()) {
            // Último fallback: usar hífen (NÃO fazer query adicional!)
            descricao = "-";
        }
        
        String estado = coleta.getEstadoEncontrado() != null ? coleta.getEstadoEncontrado() : "-";
        int id = coleta.getId();
        
        // Inserir na posição 0 (início da tabela)
        modeloTabelaHistorico.insertRow(0, new Object[]{data, numero, descricao, estado, id});
    }
    
    /**
     * Trata erros ao carregar histórico com opção de retry.
     */
    private void tratarErroHistorico(String mensagemErro, String identificacaoSala) {
        System.err.println("[OTIMIZADO] Erro ao carregar histórico: " + mensagemErro);
        lblResumoSala.setText("❌ Erro ao carregar histórico");
        tabelaHistorico.setEnabled(true);
        LOG.error("Erro ao carregar histórico: {}", mensagemErro);
        
        int opcao = JOptionPane.showOptionDialog(this,
                "Erro ao carregar histórico: " + mensagemErro + "\n\n" +
                "Isso pode ocorrer devido a conexão lenta via VPN.\n" +
                "Deseja tentar novamente?",
                "Erro de Conexão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Tentar Novamente", "Cancelar"},
                "Tentar Novamente");
        
        if (opcao == JOptionPane.YES_OPTION) {
            carregarHistoricoColeta(identificacaoSala); // Retry
        }
    }
    
    /**
     * Atualiza a tabela de histórico de forma otimizada
     * - Desabilita auto-resize durante carga
     * - Usa dados do modelo Coleta
     */
    private void atualizarTabelaHistorico(List<Coleta> coletas) {
        System.out.println("[OTIMIZADO] ========================================");
        System.out.println("[OTIMIZADO] atualizarTabelaHistorico chamado");
        System.out.println("[OTIMIZADO] Quantidade de coletas recebidas: " + (coletas != null ? coletas.size() : "null"));
        
        // Desabilitar auto-resize durante carga (performance)
        tabelaHistorico.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Limpar tabela
        int linhasAntes = modeloTabelaHistorico.getRowCount();
        modeloTabelaHistorico.setRowCount(0);
        System.out.println("[OTIMIZADO] Tabela limpa - linhas removidas: " + linhasAntes);
        
        // Formatador de data (criado UMA vez, fora do loop)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Adicionar linhas
        int contador = 0;
        if (coletas == null) {
            System.out.println("[OTIMIZADO] Lista de coletas é null, retornando");
            tabelaHistorico.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            return;
        }
        for (Coleta coleta : coletas) {
            if (coleta == null) continue;
            
            // FILTRO: Ignorar itens sem etiqueta - eles devem aparecer apenas na aba dedicada
            // Este filtro é uma proteção extra, pois o DAO já filtra no banco
            if (coleta.isSemEtiqueta()) {
                System.out.println("[OTIMIZADO] Ignorando item sem etiqueta: " + coleta.getDescricaoItemSemEtiqueta());
                continue;
            }
            
            contador++;
            String dataFormatada = coleta.getDataColeta() != null 
                ? sdf.format(coleta.getDataColeta()) 
                : "-";
            
            // Número do patrimônio (itens sem etiqueta já foram filtrados acima)
            String numeroPatrimonio = coleta.getNumeroPatrimonio() != null ? coleta.getNumeroPatrimonio() : "-";
            
            // ✅ OTIMIZADO: Usar descrição que já veio do JOIN no DAO (sem query adicional)
            // A descrição já vem preenchida pelo método criarColetaOtimizadaFromResultSet
            String descricao = coleta.getDescricaoPatrimonio();
            if (descricao == null || descricao.isEmpty()) {
                // Fallback: usar descrição de item sem etiqueta
                descricao = coleta.getDescricaoItemSemEtiqueta();
            }
            if (descricao == null || descricao.isEmpty()) {
                // Último fallback: usar hífen (NÃO fazer query adicional!)
                descricao = "-";
            }
            
            Object[] linha = {
                dataFormatada,
                numeroPatrimonio,
                descricao,
                coleta.getEstadoEncontrado() != null ? coleta.getEstadoEncontrado() : "-",
                coleta.getId() // ID oculto para facilitar exclusão
            };
            
            modeloTabelaHistorico.addRow(linha);
            
            if (contador <= 3) {
                System.out.println("[OTIMIZADO] Linha " + contador + " adicionada: " + 
                                 dataFormatada + " | " + numeroPatrimonio + " | " + 
                                 descricao + " | " + coleta.getEstadoEncontrado());
            }
        }
        
        // Restaurar auto-resize
        tabelaHistorico.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        int linhasDepois = modeloTabelaHistorico.getRowCount();
        System.out.println("[OTIMIZADO] Tabela atualizada - linhas adicionadas: " + linhasDepois);
        System.out.println("[OTIMIZADO] ========================================");
        
        // NOTA: A contagem é atualizada separadamente com o total REAL
        // (não usar coletas.size() pois é limitado a LIMITE_HISTORICO)
    }
    
    /**
     * Atualiza o label de contagem de coletas na sala
     * @param totalColetas número total de coletas na sala atual
     */
    private void atualizarContagemColetas(int totalColetas) {
        if (lblContagemColetas != null) {
            lblContagemColetas.setText(String.format("📊 Total de itens coletados: %d", totalColetas));
            
            // Mudar cor baseado na quantidade
            if (totalColetas == 0) {
                lblContagemColetas.setForeground(new Color(149, 165, 166)); // Cinza
            } else if (totalColetas < 10) {
                lblContagemColetas.setForeground(new Color(241, 196, 15)); // Amarelo
            } else {
                lblContagemColetas.setForeground(new Color(46, 204, 113)); // Verde
            }
        }
    }

    private void buscarPatrimonio() {
        String termoBusca = campoBusca.getText().trim();
        System.out.println("DEBUG: Termo de busca: '" + termoBusca + "'");

        // ✅ MÉTRICAS: Iniciar rastreamento de tempo da coleta
        if (inicioColetaAtual == 0) {
            inicioColetaAtual = System.currentTimeMillis();
        }
        inicioScanAtual = System.currentTimeMillis();
        metodoColetaAtual = "MANUAL"; // Busca manual por número

        // Verificar modo de operação
        System.out.println("DEBUG: Estado do OfflineManager: " + offlineManager.getCurrentState());
        System.out.println("DEBUG: Operando offline? " + offlineManager.isOperatingOffline());

        if (termoBusca.isEmpty()) {
            System.out.println("DEBUG: Termo de busca vazio, limpando informações");
            limparInformacoesItem();
            return;
        }

        // Usar SwingWorker para busca assíncrona (evita travamento com VPN)
        buscarPatrimonioAsync(termoBusca);
    }
    
    /**
     * Busca patrimônio de forma assíncrona usando SwingWorker.
     * Exibe indicador de loading e trata timeouts de conexão VPN.
     */
    private void buscarPatrimonioAsync(String termoBusca) {
        // Desabilitar botões durante busca
        btnBuscar.setEnabled(false);
        btnColetar.setEnabled(false);
        campoBusca.setEnabled(false);
        
        // Mostrar indicador de loading no painel de informações
        mostrarLoadingBusca(true);
        
        SwingWorker<BuscaPatrimonioResult, Void> worker = new SwingWorker<>() {
            @Override
            protected BuscaPatrimonioResult doInBackground() throws Exception {
                BuscaPatrimonioResult result = new BuscaPatrimonioResult();
                
                System.out.println("DEBUG: Chamando patrimonioDAO.buscarPorNumero() [ASYNC]...");
                result.patrimonio = patrimonioDAO.buscarPorNumero(termoBusca);
                System.out.println("DEBUG: Resultado da busca: " + (result.patrimonio != null ? "ENCONTRADO" : "NÃO ENCONTRADO"));
                
                if (result.patrimonio != null) {
                    // Verificar se já foi coletado
                    result.inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                    if (result.inventarioAtivo != null) {
                        result.jaColetado = coletaDAO.coletaExiste(result.inventarioAtivo.getId(), result.patrimonio.getId());
                    }
                }
                
                return result;
            }
            
            @Override
            protected void done() {
                // Esconder loading
                mostrarLoadingBusca(false);
                
                // Reabilitar componentes
                btnBuscar.setEnabled(true);
                campoBusca.setEnabled(true);
                
                try {
                    BuscaPatrimonioResult result = get();
                    
                    // ✅ MÉTRICAS: Registrar fim do scan
                    fimScanAtual = System.currentTimeMillis();
                    
                    if (result.patrimonio != null) {
                        exibirInformacoesItem(result.patrimonio);
                        patrimonioSelecionado = result.patrimonio;
                        
                        // Habilitar botão coletar apenas se não foi coletado e há sala selecionada
                        btnColetar.setEnabled(comboSalas.getSelectedItem() != null && !result.jaColetado);
                        
                        // Habilitar botão remover se patrimônio está selecionado e usuário tem permissão
                        boolean podeRemover = usuarioLogado != null &&
                                ("ADMIN".equals(usuarioLogado.getPerfil().name()) ||
                                        "SUPERVISOR".equals(usuarioLogado.getPerfil().name()));
                        btnRemoverItem.setEnabled(patrimonioSelecionado != null && podeRemover);
                        
                        // Reproduzir som apropriado
                        if (result.jaColetado) {
                            SoundNotification.playSound(SoundNotification.SoundType.WARNING);
                            mostrarFeedbackVisualAviso("Patrimônio já coletado!");
                        } else {
                            SoundNotification.playSound(SoundNotification.SoundType.INFO);
                        }
                    } else {
                        limparInformacoesItem();
                        SoundNotification.playSound(SoundNotification.SoundType.ERROR);
                        JOptionPane.showMessageDialog(ColetaFrame_v2.this,
                                "Patrimônio não encontrado: " + termoBusca,
                                "Item não encontrado", JOptionPane.WARNING_MESSAGE);
                    }
                    
                    // Manter foco no campo de pesquisa
                    SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    tratarErroBusca("Operação interrompida", termoBusca);
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    String mensagemErro = causa != null ? causa.getMessage() : e.getMessage();
                    tratarErroBusca(mensagemErro, termoBusca);
                }
            }
        };
        
        registrarWorker(worker);
        worker.execute();
    }
    
    /**
     * Classe auxiliar para resultado da busca de patrimônio.
     */
    private static class BuscaPatrimonioResult {
        Patrimonio patrimonio;
        Inventario inventarioAtivo;
        boolean jaColetado = false;
    }
    
    /**
     * Mostra/esconde indicador de loading durante busca.
     */
    private void mostrarLoadingBusca(boolean mostrar) {
        if (mostrar) {
            // Mostrar mensagem de loading no painel de informações
            lblNumeroItem.setText("Buscando...");
            lblDescricaoItem.setText("Aguarde, conectando ao banco de dados...");
            lblLocalizacaoOriginal.setText("-");
            lblEstadoOriginal.setText("-");
            lblStatusItem.setText("🔄 Carregando...");
            lblStatusItem.setForeground(new Color(52, 152, 219)); // Azul
            
            // Mudar cursor para loading
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            // Restaurar cursor normal
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Trata erros de busca com opção de retry.
     */
    private void tratarErroBusca(String mensagemErro, String termoBusca) {
        limparInformacoesItem();
        SoundNotification.playSound(SoundNotification.SoundType.ERROR);
        LOG.error("Erro ao buscar patrimônio: {}", mensagemErro);
        
        int opcao = JOptionPane.showOptionDialog(this,
                "Erro ao buscar patrimônio: " + mensagemErro + "\n\n" +
                "Isso pode ocorrer devido a conexão lenta via VPN.\n" +
                "Deseja tentar novamente?",
                "Erro de Conexão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Tentar Novamente", "Cancelar"},
                "Tentar Novamente");
        
        if (opcao == JOptionPane.YES_OPTION) {
            buscarPatrimonioAsync(termoBusca); // Retry
        }
        
        // Manter foco no campo de pesquisa
        SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());
    }

    private void exibirInformacoesItem(Patrimonio patrimonio) {
        lblNumeroItem.setText(patrimonio.getNumero());
        lblDescricaoItem.setText(patrimonio.getDescricao());
        lblLocalizacaoOriginal.setText(patrimonio.getNomeSala() != null ? patrimonio.getNomeSala() : "-");
        lblEstadoOriginal.setText(patrimonio.getEstadoConservacao() != null ? patrimonio.getEstadoConservacao() : "-");

        // Exibir status do patrimônio (ATIVO, BAIXADO, etc)
        String status = patrimonio.getStatus();
        if (status != null && !status.isEmpty()) {
            lblStatusItem.setText(status);
            // Colorir baseado no status
            switch (status) {
                case "ATIVO" -> lblStatusItem.setForeground(new Color(46, 204, 113)); // Verde
                case "BAIXADO", "INATIVO" -> lblStatusItem.setForeground(new Color(231, 76, 60)); // Vermelho
                default -> lblStatusItem.setForeground(new Color(241, 196, 15)); // Amarelo
            }
        } else {
            lblStatusItem.setText("-");
            lblStatusItem.setForeground(new Color(149, 165, 166)); // Cinza
        }

        // Preencher combo de estado com o estado atual
        if (patrimonio.getEstadoConservacao() != null) {
            comboEstado.setSelectedItem(patrimonio.getEstadoConservacao());
        } else {
            // Se não tem estado, selecionar "BOM" como padrão
            comboEstado.setSelectedItem("BOM");
        }
    }

    private void limparInformacoesItem() {
        lblNumeroItem.setText("-");
        lblDescricaoItem.setText("-");
        lblLocalizacaoOriginal.setText("-");
        lblEstadoOriginal.setText("-");
        lblStatusItem.setText("-");
        lblStatusItem.setForeground(new Color(149, 165, 166)); // Cinza
        patrimonioSelecionado = null;

        // Desabilitar botões quando não há patrimônio selecionado
        btnColetar.setEnabled(false);
        btnRemoverItem.setEnabled(false);
    }

    private void registrarItemEncontrado() {
        System.out.println("DEBUG: registrarItemEncontrado() iniciado");

        // Verificar se o usuário tem permissão para realizar coletas
        if (usuarioLogado == null) {
            System.out.println("DEBUG: Usuário não autenticado");
            JOptionPane.showMessageDialog(this,
                    "Usuário não autenticado. Faça login para realizar coletas.",
                    "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("DEBUG: Usuário autenticado: " + usuarioLogado.getNomeCompleto());

        // Buscar inventário ativo para verificar permissão
        try {
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo == null) {
                System.out.println("DEBUG: Nenhum inventário ativo encontrado");
                JOptionPane.showMessageDialog(this, "Nenhum inventário ativo encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("DEBUG: Inventário ativo: " + inventarioAtivo.getNome());

            // Verificar se o usuário tem autorização para realizar coletas
            Integer autorizacao = verificarAutorizacaoColeta(inventarioAtivo);

            if (autorizacao == null) {
                System.out.println("DEBUG: Usuário sem autorização");
                JOptionPane.showMessageDialog(this,
                        """
                        Acesso Negado!

                        Você não está habilitado para realizar coletas neste inventário.
                        Entre em contato com o supervisor do inventário para obter as permissões necessárias.""",
                        "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("DEBUG: Autorização obtida: " + autorizacao);
            // Nota: O ID do participante é calculado posteriormente no método,
            // quando a coleta é efetivamente criada (linha ~3065)
        } catch (SQLException | RuntimeException e) {
            System.err.println("DEBUG: Erro ao verificar permissões: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Erro ao verificar permissões: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            System.out.println("DEBUG: Nenhuma sala selecionada");
            JOptionPane.showMessageDialog(this, "Selecione uma sala primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("DEBUG: Sala selecionada: " + salaAtual.getIdentificacaoCompleta());

        // Validação: é necessário um patrimônio selecionado.
        // Itens sem etiqueta são tratados na aba dedicada "Itens Sem Patrimônio".
        if (patrimonioSelecionado == null) {
            System.out.println("DEBUG: Nenhum patrimônio selecionado");
            JOptionPane.showMessageDialog(this, "Busque um patrimônio primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        System.out.println("DEBUG: Patrimônio selecionado: " + patrimonioSelecionado.getNumero());

        try {
            String estadoAtual = (String) comboEstado.getSelectedItem();
            String observacoes = campoObservacao.getText().trim();

            // Criar objeto Coleta
            Coleta coleta = new Coleta();

            // Buscar inventário ativo
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo == null) {
                JOptionPane.showMessageDialog(this, "Nenhum inventário ativo encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            coleta.setIdInventario(inventarioAtivo.getId());
            // Modo offline: autorização simplificada
            if (usuarioLogado != null) {
                coleta.setIdColetor(usuarioLogado.getId());
                System.out.println("DEBUG: Modo offline - ID coletor: " + usuarioLogado.getId());
                
                // ✅ CORRIGIDO: Buscar ID do participante do inventário
                ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
                Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
                    inventarioAtivo.getId(), 
                    usuarioLogado.getId()
                );
                
                if (idParticipante != null && idParticipante > 0) {
                    coleta.setIdParticipanteInventario(idParticipante);
                    System.out.println("DEBUG: ID participante definido: " + idParticipante);
                } else {
                    // Fallback: usar ID do coletor
                    coleta.setIdParticipanteInventario(usuarioLogado.getId());
                    System.out.println("DEBUG: AVISO - Participante não encontrado, usando ID do coletor como fallback");
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuário não autenticado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // DEBUG: Log detalhado da criação do timestamp (Item normal)
            long currentTimeMillis2 = System.currentTimeMillis();
            Timestamp novoTimestamp2 = new Timestamp(currentTimeMillis2);
            System.out.println("=== DEBUG TIMESTAMP: Criando timestamp para item NORMAL ===");
            System.out.println("DEBUG TIMESTAMP: currentTimeMillis: " + currentTimeMillis2);
            System.out.println("DEBUG TIMESTAMP: Timestamp criado: " + novoTimestamp2);
            System.out.println("DEBUG TIMESTAMP: Timestamp.toString(): " + novoTimestamp2.toString());
            
            coleta.setDataColeta(novoTimestamp2);
            System.out.println("DEBUG TIMESTAMP: Timestamp setado na coleta");
            
            coleta.setStatusColeta("COLETADO");
            coleta.setObservacaoColeta(observacoes);
            // ✅ CORRIGIDO: Usar numeroSala para consistência com dados existentes no banco
            // O campo localizacao_encontrada no banco contém apenas o número/nome da sala (ex: "CAE")
            // e não a identificação completa (ex: "CAE - CAE(IFMT - PDL)")
            coleta.setLocalizacaoEncontrada(salaAtual.getNumeroSala());
            coleta.setEstadoEncontrado(estadoAtual);
            coleta.setDivergencia(false);
            
            // DEBUG: Confirmar que o estado e localização foram definidos
            System.out.println("DEBUG: Estado de conservação definido: " + estadoAtual);
            System.out.println("DEBUG: Estado na coleta: " + coleta.getEstadoEncontrado());
            System.out.println("DEBUG: Localização encontrada: " + coleta.getLocalizacaoEncontrada());
            System.out.println("DEBUG: Sala atual (numeroSala): " + salaAtual.getNumeroSala());

            // Configurar para patrimônio normal
            coleta.setIdPatrimonio(patrimonioSelecionado.getId());
            coleta.setNumeroPatrimonio(patrimonioSelecionado.getNumero()); // ✅ ADICIONAR número do patrimônio
            coleta.setDescricaoPatrimonio(patrimonioSelecionado.getDescricao()); // ✅ ADICIONAR descrição do patrimônio
            coleta.setSemEtiqueta(false);
            coleta.setLocalizacaoAtual(patrimonioSelecionado.getNomeSala());

            // Verificar se o patrimônio já foi coletado neste inventário
            if (coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonioSelecionado.getId())) {
                JOptionPane.showMessageDialog(this,
                        """
                        Este patrim\u00f4nio j\u00e1 foi coletado neste invent\u00e1rio.
                        N\u00famero: """ + patrimonioSelecionado.getNumero() + "\n" +
                                "Descrição: " + patrimonioSelecionado.getDescricao(),
                        "Patrimônio já coletado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Verificar divergência de localização
            if (!salaAtual.getIdentificacaoCompleta().equals(patrimonioSelecionado.getNomeSala())) {
                coleta.setDivergencia(true);
                coleta.setMotivoDivergencia("Item encontrado em sala diferente da registrada");
            }

            // ✅ MÉTRICAS: Calcular e definir métricas de tempo antes de salvar
            if (inicioColetaAtual > 0) {
                coleta.calcularMetricasTempo(inicioColetaAtual, 
                    inicioScanAtual > 0 ? inicioScanAtual : null, 
                    fimScanAtual > 0 ? fimScanAtual : null);
                coleta.setMetodoColeta(metodoColetaAtual != null ? metodoColetaAtual : "MANUAL");
                coleta.setTipoScan("MANUAL");
                coleta.setTentativasScan(1);
                coleta.setErrosScan(0);
                coleta.setQualidadeEtiqueta("BOA");
                
                System.out.println("DEBUG MÉTRICAS: Tempo total=" + coleta.getTempoColetaSegundos() + "s, " +
                    "Tempo scan=" + coleta.getTempoScanSegundos() + "s, " +
                    "Método=" + coleta.getMetodoColeta() + ", " +
                    "Período=" + coleta.getPeriodoColeta());
            }

            // Registrar no banco (usando serviço offline)
            System.out.println("DEBUG: Inserindo coleta no banco...");
            coletaOfflineService.salvarColeta(coleta);
            System.out.println("DEBUG: Coleta inserida com sucesso! Modo: " +
                    offlineManager.getCurrentState());
            
            // ✅ MÉTRICAS: Resetar variáveis de tempo para próxima coleta
            inicioColetaAtual = null;
            inicioScanAtual = null;
            fimScanAtual = null;
            metodoColetaAtual = null;

            // Reproduzir som de sucesso da COLETA (diferente do som de encontrar)
            SoundNotification.playColetaSalvaSound(); // Som SUCCESS (800Hz, 200ms)
            System.out.println("DEBUG: Som de coleta salva reproduzido");

            // Modo offline: não atualiza estatísticas em TABELA_SALA_INVENTARIO (não existe no SQLite)
            System.out.println("DEBUG: Modo offline - estatísticas não são atualizadas");

            // Inserir a nova coleta no INÍCIO da tabela (mais recente no topo)
            System.out.println("DEBUG: Inserindo coleta no início da tabela...");
            inserirColetaNoInicio(coleta);
            
            // Atualizar contagem
            atualizarContagemColetas(modeloTabelaHistorico.getRowCount());

            // Feedback visual de sucesso - piscar o painel de informações em verde
            mostrarFeedbackVisualSucesso();

            // Limpar formulário
            limparFormulario();
            System.out.println("DEBUG: Formulário limpo");

            // SEMPRE retornar foco ao campo de pesquisa
            SwingUtilities.invokeLater(() -> {
                campoBusca.requestFocusInWindow();
                campoBusca.selectAll(); // Selecionar todo o texto para facilitar nova digitação
            });

            LOG.debug("registrarItemEncontrado() concluído com sucesso!");

        } catch (SQLException | RuntimeException e) {
            LOG.error("ERRO ao registrar item: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao registrar item: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detectarLeituraCodigoBarras() {
        long agora = System.currentTimeMillis();
        String textoAtual = campoBusca.getText().trim();

        // Se passou muito tempo desde a última digitação, resetar buffer
        if (agora - ultimaDigitacao > 200) {
            bufferCodigoBarras.setLength(0);
        }

        ultimaDigitacao = agora;

        // Verificar se o texto tem 6 dígitos (códigos de patrimônio válidos)
        if (textoAtual.length() >= 6 && textoAtual.length() <= 6 && textoAtual.matches("\\d+")) {
            // Aguardar um pouco para ver se mais caracteres serão digitados
            Timer timer = new Timer(300, e -> {
                String textoFinal = campoBusca.getText().trim();
                // Se o texto ainda tem 6 dígitos, fazer a busca
                if (textoFinal.length() >= 6 && textoFinal.length() <= 6 && textoFinal.matches("\\d+")) {
                    if (modoColetaAutomatica) {
                        // Modo automático: buscar e coletar imediatamente
                        SwingUtilities.invokeLater(() -> buscarEColetarAutomaticamente());
                    } else {
                        // Modo normal: apenas buscar
                        SwingUtilities.invokeLater(() -> buscarPatrimonio());
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * Busca e coleta automaticamente um patrimônio (modo leitor de código de
     * barras)
     */
    private void buscarEColetarAutomaticamente() {
        String termoBusca = campoBusca.getText().trim();

        if (termoBusca.isEmpty()) {
            return;
        }

        // Verificar se há sala selecionada
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            SoundNotification.playSound(SoundNotification.SoundType.ERROR);
            mostrarFeedbackVisualErro("Selecione uma sala primeiro!");
            campoBusca.selectAll();
            return;
        }

        try {
            // Buscar patrimônio
            Patrimonio patrimonioEncontrado = patrimonioDAO.buscarPorNumero(termoBusca);

            if (patrimonioEncontrado == null) {
                SoundNotification.playSound(SoundNotification.SoundType.ERROR);
                mostrarFeedbackVisualErro("Patrimônio não encontrado: " + termoBusca);
                campoBusca.selectAll();
                return;
            }

            // Verificar se já foi coletado ANTES de exibir informações
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo != null
                    && coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonioEncontrado.getId())) {
                // Exibir informações mesmo se já coletado
                exibirInformacoesItem(patrimonioEncontrado);
                patrimonioSelecionado = patrimonioEncontrado;

                SoundNotification.playSound(SoundNotification.SoundType.WARNING);
                mostrarFeedbackVisualAviso("Patrimônio já coletado!");
                campoBusca.selectAll();
                return;
            }

            // Exibir informações
            exibirInformacoesItem(patrimonioEncontrado);
            patrimonioSelecionado = patrimonioEncontrado;

            // Habilitar botão coletar temporariamente para permitir o registro
            btnColetar.setEnabled(true);

            // Registrar automaticamente
            System.out.println("DEBUG: Modo automático - Registrando patrimônio: " + termoBusca);
            registrarItemEncontrado();

            // Limpar campo após registro bem-sucedido
            SwingUtilities.invokeLater(() -> {
                campoBusca.setText("");
                campoBusca.requestFocusInWindow();
            });

        } catch (SQLException | RuntimeException e) {
            SoundNotification.playSound(SoundNotification.SoundType.ERROR);
            mostrarFeedbackVisualErro("Erro: " + e.getMessage());
            LOG.error("ERRO no modo automático: {}", e.getMessage(), e);
            campoBusca.selectAll();
        }
    }

    private void adicionarDicaLeitores() {
        campoBusca.setToolTipText("Digite o número do patrimônio ou use um leitor de código de barras");
    }

    // Método alternarModoSemEtiqueta removido - a lógica agora é controlada pelas
    // abas

    /**
     * Limpa todos os campos do formulário
     */
    private void limparFormulario() {
        campoBusca.setText("");
        campoObservacao.setText("");
        // chkSemEtiqueta removido - checkbox não existe mais
        // alternarModoSemEtiqueta() não é mais necessário // Resetar modo
        limparInformacoesItem();

        // Garantir que o foco volte ao campo de pesquisa
        SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());
    }

    /**
     * Exclui a coleta selecionada na tabela de histórico (apenas admin)
     */
    private void excluirColetaSelecionada() {
        // Verificar se usuário é admin
        if (usuarioLogado == null || !"ADMIN".equals(usuarioLogado.getPerfil().name())) {
            JOptionPane.showMessageDialog(this, "Apenas administradores podem excluir coletas.",
                    "Acesso Negado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int linhaSelecionada = tabelaHistorico.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma coleta para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar exclusão
        String numeroPatrimonio = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 1);
        String descricao = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 2);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                """
                Tem certeza que deseja excluir a coleta do item:
                Patrim\u00f4nio: """ + numeroPatrimonio + "\n" +
                        "Descrição: " + descricao + "\n\n" +
                        "Esta ação não pode ser desfeita!",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Obter ID da coleta da coluna oculta (índice 4)
            Object idObj = modeloTabelaHistorico.getValueAt(linhaSelecionada, 4);
            if (idObj == null) {
                JOptionPane.showMessageDialog(this, "Erro: ID da coleta não encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int idColeta = (Integer) idObj;
            
            Sala salaAtual = (Sala) comboSalas.getSelectedItem();
            if (salaAtual == null) {
                JOptionPane.showMessageDialog(this, "Erro: Nenhuma sala selecionada.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Excluir coleta usando o ID diretamente (mais confiável)
            coletaDAO.excluirColeta(idColeta);
            
            JOptionPane.showMessageDialog(this, "Coleta excluída com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // Modo offline: não atualiza estatísticas (tabela não existe no SQLite)
            System.out.println("DEBUG: Coleta ID " + idColeta + " excluída com sucesso");

            // Remover apenas a linha da tabela (sem recarregar tudo)
            // Usar linhaSelecionada já declarada no início do método
            if (linhaSelecionada >= 0) {
                modeloTabelaHistorico.removeRow(linhaSelecionada);
                atualizarContagemColetas(modeloTabelaHistorico.getRowCount());
            }

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao excluir coleta: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao excluir coleta: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Remove o item selecionado da coleta atual (da tabela de histórico)
     */
    private void removerItemSelecionado() {
        // Verificar permissão do usuário
        if (usuarioLogado == null ||
                (!("ADMIN".equals(usuarioLogado.getPerfil().name()) ||
                        "SUPERVISOR".equals(usuarioLogado.getPerfil().name())))) {
            JOptionPane.showMessageDialog(this,
                    "Apenas Administradores e Supervisores podem remover itens da coleta.",
                    "Acesso Negado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar se há uma linha selecionada na tabela de histórico
        int linhaSelecionada = tabelaHistorico.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um item na tabela de histórico para remover.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar se há uma sala selecionada
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma sala primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obter dados da linha selecionada para exibição na confirmação
        String dataHora = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 0);
        String numeroPatrimonio = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 1);
        String descricao = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 2);

        // Confirmar remoção
        int confirmacao = JOptionPane.showConfirmDialog(this,
                """
                \u26a0\ufe0f Tem certeza que deseja remover este item da coleta?
                
                Patrim\u00f4nio: """ + numeroPatrimonio + "\n" +
                        "Descrição: " + descricao + "\n" +
                        "Data/Hora: " + dataHora + "\n\n" +
                        "Esta ação não pode ser desfeita!",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Obter ID da coleta da coluna oculta (índice 4)
            Object idObj = modeloTabelaHistorico.getValueAt(linhaSelecionada, 4);
            if (idObj == null) {
                JOptionPane.showMessageDialog(this,
                        "❌ Erro: ID da coleta não encontrado.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int idColeta = (Integer) idObj;
            
            // Remover a coleta usando o ID diretamente (mais confiável)
            coletaDAO.excluirColeta(idColeta);
            
            // Som de sucesso
            SoundNotification.playSound(SoundNotification.SoundType.SUCCESS);

            JOptionPane.showMessageDialog(this,
                    "✅ Item removido da coleta com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

            System.out.println("DEBUG: Coleta ID " + idColeta + " removida com sucesso");

            // Remover apenas a linha da tabela (sem recarregar tudo)
            modeloTabelaHistorico.removeRow(linhaSelecionada);
            atualizarContagemColetas(modeloTabelaHistorico.getRowCount());

            // Limpar seleção atual se o patrimônio removido era o selecionado
            if (patrimonioSelecionado != null &&
                    patrimonioSelecionado.getNumero().equals(numeroPatrimonio)) {
                limparInformacoesItem();
                campoBusca.setText("");
            }

            campoBusca.requestFocusInWindow();

        } catch (SQLException | RuntimeException e) {
            // Som de erro
            SoundNotification.playSound(SoundNotification.SoundType.ERROR);

            LOG.error("Erro ao remover item: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "❌ Erro ao remover item: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Finaliza a coleta na sala selecionada
     * Atualiza a tabela SALA_INVENTARIO com estatísticas e marca como finalizada
     * NOTA: Esta operação só funciona em modo ONLINE (requer PostgreSQL)
     */
    private void finalizarColetaSala() {
        // Verificar se está em modo online - operação requer PostgreSQL
        if (offlineManager.isOperatingOffline()) {
            JOptionPane.showMessageDialog(this, """
                                                \u26a0\ufe0f Opera\u00e7\u00e3o n\u00e3o dispon\u00edvel em modo OFFLINE!
                                                
                                                A finaliza\u00e7\u00e3o de salas requer conex\u00e3o com o servidor.
                                                Conecte-se ao servidor e tente novamente.""",
                    "Modo Offline",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma sala para finalizar a coleta.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Buscar inventário ativo
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo == null) {
                JOptionPane.showMessageDialog(this, "Nenhum inventário ativo encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar se a sala já está finalizada
            boolean jaFinalizada = salaInventarioDAO.isColetaFinalizada(salaAtual.getIdSala(), inventarioAtivo.getId());
            if (jaFinalizada) {
                JOptionPane.showMessageDialog(this, """
                                                    Esta sala j\u00e1 foi finalizada anteriormente.
                                                    Use o bot\u00e3o 'Reabrir' se precisar fazer altera\u00e7\u00f5es.""",
                        "Sala já Finalizada", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Contar itens coletados na sala
            int[] estatisticas = coletaDAO.contarColetasPorSala(inventarioAtivo.getId(), salaAtual.getNumeroSala());
            int totalItensColetados = estatisticas[0];
            int totalItensSemEtiqueta = estatisticas[1];

            // Solicitar observações opcionais
            String observacoes = JOptionPane.showInputDialog(this,
                    "Observações sobre a finalização da coleta (opcional):",
                    "Finalizar Coleta da Sala",
                    JOptionPane.QUESTION_MESSAGE);

            // Confirmar finalização com estatísticas
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    """
                    Tem certeza que deseja FINALIZAR a coleta da sala?

                    📍 Sala: %s
                    📊 Total de itens coletados: %d
                    🏷️ Itens sem etiqueta: %d

                    ⚠️ Após finalizar:
                    • A sala será marcada como CONCLUÍDA
                    • Não será possível adicionar novas coletas
                    • A sala não aparecerá mais no app mobile

                    Confirma a finalização?""".formatted(
                            salaAtual.getIdentificacaoCompleta(),
                            totalItensColetados,
                            totalItensSemEtiqueta),
                    "Confirmar Finalização",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            // Obter ID do participante (usuário logado)
            Integer idParticipante = null;
            if (usuarioLogado != null) {
                idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                        inventarioAtivo.getId(), usuarioLogado.getId());
                if (idParticipante == null) {
                    idParticipante = usuarioLogado.getId(); // Fallback para ID do usuário
                }
            }

            // Finalizar a coleta no banco de dados
            boolean sucesso = salaInventarioDAO.finalizarColeta(
                    salaAtual.getIdSala(),
                    inventarioAtivo.getId(),
                    idParticipante,
                    observacoes
            );

            // Atualizar estatísticas
            if (sucesso) {
                salaInventarioDAO.atualizarEstatisticas(
                        salaAtual.getIdSala(),
                        inventarioAtivo.getId(),
                        totalItensColetados,
                        totalItensSemEtiqueta
                );
            }

            if (sucesso) {
                // Reproduzir som de sucesso
                SoundNotification.playSound(SoundNotification.SoundType.SUCCESS);

                JOptionPane.showMessageDialog(this,
                        """
                        ✅ Coleta da sala FINALIZADA com sucesso!

                        📍 Sala: %s
                        📊 Total coletado: %d itens
                        🏷️ Sem etiqueta: %d itens

                        A sala foi marcada como concluída no inventário.""".formatted(
                                salaAtual.getIdentificacaoCompleta(),
                                totalItensColetados,
                                totalItensSemEtiqueta),
                        "Finalização Concluída", JOptionPane.INFORMATION_MESSAGE);

                // Atualizar interface - desabilitar campos
                btnFinalizarColeta.setText("✅ Finalizada");
                btnFinalizarColeta.setBackground(new Color(108, 117, 125)); // Cinza
                btnFinalizarColeta.setEnabled(false);

                // Habilitar botão reabrir se usuário tem permissão E está online
                // REGRA: btnReabrir só fica ativo quando sala está FINALIZADA e modo ONLINE
                if (btnReabrirColeta.isVisible()) {
                    btnReabrirColeta.setEnabled(!offlineManager.isOperatingOffline());
                    if (offlineManager.isOperatingOffline()) {
                        btnReabrirColeta.setToolTipText("⚠️ Reabrir sala requer conexão com o servidor");
                    } else {
                        btnReabrirColeta.setToolTipText("Reabrir sala finalizada para permitir novas coletas");
                    }
                }

                // Atualizar resumo da sala
                lblResumoSala.setText(String.format("✅ Sala FINALIZADA: %s | %d itens coletados",
                        salaAtual.getIdentificacaoCompleta(), totalItensColetados));
                lblResumoSala.setForeground(new Color(40, 167, 69)); // Verde

                // Desabilitar campos de coleta para esta sala
                campoBusca.setEnabled(false);
                btnBuscar.setEnabled(false);
                btnColetar.setEnabled(false);
                campoObservacao.setEnabled(false);
                comboEstado.setEnabled(false);

                System.out.println("DEBUG: Sala " + salaAtual.getIdentificacaoCompleta() + 
                                  " finalizada com " + totalItensColetados + " itens");

            } else {
                JOptionPane.showMessageDialog(this,
                        "Erro ao finalizar coleta da sala.\nVerifique os logs para mais detalhes.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao finalizar coleta: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao finalizar coleta: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reabre a coleta de uma sala finalizada
     * Apenas administradores e supervisores podem executar esta ação
     * NOTA: Esta operação só funciona em modo ONLINE (requer PostgreSQL)
     */
    private void reabrirColetaSala() {
        // Verificar se está em modo online - operação requer PostgreSQL
        if (offlineManager.isOperatingOffline()) {
            JOptionPane.showMessageDialog(this, """
                                                \u26a0\ufe0f Opera\u00e7\u00e3o n\u00e3o dispon\u00edvel em modo OFFLINE!
                                                
                                                A reabertura de salas requer conex\u00e3o com o servidor.
                                                Conecte-se ao servidor e tente novamente.""",
                    "Modo Offline",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // VALIDAÇÃO DE PERMISSÃO: Apenas ADMIN e SUPERVISOR podem reabrir salas
        if (usuarioLogado == null ||
                (!("ADMIN".equals(usuarioLogado.getPerfil().name()) ||
                        "SUPERVISOR".equals(usuarioLogado.getPerfil().name())))) {
            JOptionPane.showMessageDialog(this,
                    """
                    ⛔ Acesso Negado!

                    Apenas Administradores e Supervisores podem reabrir salas finalizadas.

                    Seu perfil atual: %s""".formatted(usuarioLogado != null ? usuarioLogado.getPerfil().name() : "Não identificado"),
                    "Permissão Insuficiente",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("DEBUG: Tentativa de reabrir sala negada - Usuário: " +
                    (usuarioLogado != null ? usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getPerfil() + ")" : "null"));
            return;
        }
        
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma sala para reabrir a coleta.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Buscar inventário ativo
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo == null) {
                JOptionPane.showMessageDialog(this,
                        "Nenhum inventário ativo encontrado.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar se a sala está realmente finalizada
            boolean estaFinalizada = salaInventarioDAO.isColetaFinalizada(salaAtual.getIdSala(), inventarioAtivo.getId());
            if (!estaFinalizada) {
                JOptionPane.showMessageDialog(this,
                        "Esta sala não está finalizada.\nNão é necessário reabrir.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirmar reabertura com popup de confirmação
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    """
                    \u26a0\ufe0f ATEN\u00c7\u00c3O: Deseja realmente REABRIR a coleta desta sala?
                    
                    \ud83d\udccd Sala: """ + salaAtual.getIdentificacaoCompleta() + "\n" +
                            "📋 Inventário: " + inventarioAtivo.getNome() + "\n\n" +
                            "Ao reabrir:\n" +
                            "• A sala voltará a aparecer no aplicativo mobile\n" +
                            "• Será possível adicionar novas coletas\n" +
                            "• O status será alterado para EM ANDAMENTO\n" +
                            "• As estatísticas serão mantidas\n\n" +
                            "Confirma a reabertura?",
                    "Confirmar Reabertura de Sala",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            // Reabrir a coleta no banco de dados
            boolean sucesso = salaInventarioDAO.reabrirColeta(salaAtual.getIdSala(), inventarioAtivo.getId());

            if (sucesso) {
                // Reproduzir som de sucesso
                SoundNotification.playSound(SoundNotification.SoundType.SUCCESS);

                JOptionPane.showMessageDialog(this,
                        """
                        ✅ Coleta da sala REABERTA com sucesso!

                        📍 Sala: %s

                        A sala voltará a aparecer no aplicativo mobile e
                        poderá receber novas coletas.""".formatted(salaAtual.getIdentificacaoCompleta()),
                        "Reabertura Concluída",
                        JOptionPane.INFORMATION_MESSAGE);

                // Atualizar interface - habilitar campos novamente
                btnFinalizarColeta.setText("🏁 Finalizar");
                btnFinalizarColeta.setBackground(new Color(40, 167, 69));
                btnFinalizarColeta.setEnabled(true);

                // Desabilitar botão reabrir (sala não está mais finalizada)
                if (btnReabrirColeta.isVisible()) {
                    btnReabrirColeta.setEnabled(false);
                }

                // Atualizar resumo da sala
                lblResumoSala.setText(String.format("Coletando em: %s",
                        salaAtual.getIdentificacaoCompleta()));
                lblResumoSala.setForeground(new Color(100, 100, 100)); // Cor padrão

                // Habilitar campos de coleta
                campoBusca.setEnabled(true);
                btnBuscar.setEnabled(true);
                campoObservacao.setEnabled(true);
                comboEstado.setEnabled(true);

                System.out.println("DEBUG: Sala " + salaAtual.getIdentificacaoCompleta() + " reaberta com sucesso");

            } else {
                JOptionPane.showMessageDialog(this,
                        "Erro ao reabrir coleta da sala.\nVerifique os logs para mais detalhes.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao reabrir coleta: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    """
                    ❌ Erro ao reabrir coleta:

                    %s""".formatted(e.getMessage()),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifica se o usuário tem permissão para realizar coletas
     * Modo offline simplificado: todos os usuários autenticados podem coletar
     * 
     * @param inventarioAtivo O inventário ativo
     * @return ID do usuário se autorizado, ou null se não autorizado
     */
    private Integer verificarAutorizacaoColeta(Inventario inventarioAtivo) {
        if (usuarioLogado == null) {
            return null;
        }
        
        if (inventarioAtivo == null) {
            System.out.println("DEBUG: Nenhum inventário ativo para verificar autorização");
            return null;
        }

        // Modo offline: todos os usuários autenticados têm acesso
        System.out.println("DEBUG: Modo offline - usuário autorizado: " + usuarioLogado.getNomeCompleto() + 
                          " para inventário: " + inventarioAtivo.getNome());
        return usuarioLogado.getId();
    }

    /**
     * Cancela animações visuais em andamento para evitar conflitos de cores
     */
    private void cancelarAnimacoesAnteriores() {
        if (timerAnimacaoAtual != null && timerAnimacaoAtual.isRunning()) {
            timerAnimacaoAtual.stop();
        }
        if (timerTextoAtual != null && timerTextoAtual.isRunning()) {
            timerTextoAtual.stop();
        }
        // Restaurar cores padrão imediatamente
        panelInfoItem.setBackground(Color.WHITE);
        lblResumoSala.setForeground(new Color(100, 100, 100));
        lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    /**
     * Adiciona animação de clique (pulse) a um botão
     * O botão "encolhe" brevemente e muda de cor quando pressionado para dar feedback visual
     */
    private void adicionarAnimacaoClique(JButton botao, Color corBase) {
        Color corPressionado = corBase.darker(); // Cor mais escura quando pressionado
        
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (botao.isEnabled()) {
                    // Efeito de "pressionar" - diminuir tamanho e escurecer cor
                    Dimension tamanhoOriginal = botao.getPreferredSize();
                    botao.setPreferredSize(new Dimension(
                        tamanhoOriginal.width - 4, 
                        tamanhoOriginal.height - 2));
                    botao.setBackground(corPressionado);
                    botao.revalidate();
                    botao.repaint();
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (botao.isEnabled()) {
                    // Restaurar tamanho e cor original
                    botao.setPreferredSize(new Dimension(200, 45));
                    botao.setBackground(corBase);
                    botao.revalidate();
                    botao.repaint();
                }
            }
        });
    }

    /**
     * Anima o botão Registrar com efeito de pulse (pulsar) e mudança de cor
     * Usado quando o botão é clicado ou ativado via atalho F4
     */
    private void animarBotaoRegistrar() {
        if (btnColetar == null || !btnColetar.isEnabled()) return;
        
        // Cores para animação
        final Color corOriginal = new Color(46, 204, 113); // Verde original
        final Color corPressionado = new Color(39, 174, 96); // Verde mais escuro
        final Color corDestaque = new Color(88, 214, 141); // Verde mais claro
        
        // Salvar tamanho original
        final Dimension tamanhoOriginal = new Dimension(200, 45);
        
        // Criar animação de pulse com mudança de cor
        Timer timerPulse = new Timer(60, null);
        final int[] fase = {0};
        
        timerPulse.addActionListener(e -> {
            switch (fase[0]) {
                case 0 -> {
                    // Encolher e escurecer
                    btnColetar.setPreferredSize(new Dimension(192, 41));
                    btnColetar.setBackground(corPressionado);
                    btnColetar.revalidate();
                    btnColetar.repaint();
                }
                case 1 -> {
                    // Expandir e clarear
                    btnColetar.setPreferredSize(new Dimension(208, 49));
                    btnColetar.setBackground(corDestaque);
                    btnColetar.revalidate();
                    btnColetar.repaint();
                }
                case 2 -> {
                    // Voltar ao tamanho e cor original
                    btnColetar.setPreferredSize(tamanhoOriginal);
                    btnColetar.setBackground(corOriginal);
                    btnColetar.revalidate();
                    btnColetar.repaint();
                    timerPulse.stop();
                }
            }
            fase[0]++;
        });
        
        timerPulse.start();
    }

    /**
     * Mostra feedback visual de sucesso ao registrar um item
     * Pisca o painel de informações em verde para indicar sucesso
     */
    private void mostrarFeedbackVisualSucesso() {
        // Cancelar animações anteriores para evitar conflito de cores
        cancelarAnimacoesAnteriores();

        // Salvar cor original
        Color corOriginal = Color.WHITE;
        Color corSucesso = new Color(46, 204, 113, 100); // Verde com transparência

        // Criar animação de piscar
        timerAnimacaoAtual = new Timer(100, null);
        final int[] contador = { 0 };

        timerAnimacaoAtual.addActionListener(e -> {
            if (contador[0] % 2 == 0) {
                panelInfoItem.setBackground(corSucesso);
                lblResumoSala.setForeground(new Color(0, 150, 0));
                lblResumoSala.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                panelInfoItem.setBackground(corOriginal);
                lblResumoSala.setForeground(new Color(100, 100, 100));
                lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }

            contador[0]++;

            // Parar após 3 piscadas (6 mudanças)
            if (contador[0] >= 6) {
                timerAnimacaoAtual.stop();
                panelInfoItem.setBackground(corOriginal);
                lblResumoSala.setForeground(new Color(100, 100, 100));
                lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }
        });

        timerAnimacaoAtual.start();

        // Mostrar mensagem temporária no label de resumo
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        String textoOriginal = salaAtual != null 
            ? String.format("Coletando em: %s", salaAtual.getIdentificacaoCompleta())
            : "Selecione uma sala para iniciar a coleta";
        lblResumoSala.setText("✅ Item registrado com sucesso!");
        lblResumoSala.setForeground(new Color(46, 204, 113));

        // Restaurar texto original após 2 segundos
        timerTextoAtual = new Timer(2000, e -> {
            lblResumoSala.setText(textoOriginal);
            lblResumoSala.setForeground(new Color(100, 100, 100));
        });
        timerTextoAtual.setRepeats(false);
        timerTextoAtual.start();
    }

    /**
     * Mostra feedback visual de erro
     * Pisca o painel em vermelho e mostra mensagem
     */
    private void mostrarFeedbackVisualErro(String mensagem) {
        // Cancelar animações anteriores para evitar conflito de cores
        cancelarAnimacoesAnteriores();

        // Salvar cor original
        Color corOriginal = Color.WHITE;
        Color corErro = new Color(231, 76, 60, 100); // Vermelho com transparência

        // Criar animação de piscar
        timerAnimacaoAtual = new Timer(100, null);
        final int[] contador = { 0 };

        timerAnimacaoAtual.addActionListener(e -> {
            if (contador[0] % 2 == 0) {
                panelInfoItem.setBackground(corErro);
            } else {
                panelInfoItem.setBackground(corOriginal);
            }

            contador[0]++;

            // Parar após 3 piscadas (6 mudanças)
            if (contador[0] >= 6) {
                timerAnimacaoAtual.stop();
                panelInfoItem.setBackground(corOriginal);
            }
        });

        timerAnimacaoAtual.start();

        // Mostrar mensagem temporária no label de resumo
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        String textoOriginal = salaAtual != null 
            ? String.format("Coletando em: %s", salaAtual.getIdentificacaoCompleta())
            : "Selecione uma sala para iniciar a coleta";
        lblResumoSala.setText("❌ " + mensagem);
        lblResumoSala.setForeground(new Color(231, 76, 60));
        lblResumoSala.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Restaurar texto original após 3 segundos
        timerTextoAtual = new Timer(3000, e -> {
            lblResumoSala.setText(textoOriginal);
            lblResumoSala.setForeground(new Color(100, 100, 100));
            lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        });
        timerTextoAtual.setRepeats(false);
        timerTextoAtual.start();
    }

    /**
     * Mostra feedback visual de aviso
     * Pisca o painel em amarelo e mostra mensagem
     */
    private void mostrarFeedbackVisualAviso(String mensagem) {
        // Cancelar animações anteriores para evitar conflito de cores
        cancelarAnimacoesAnteriores();

        // Salvar cor original
        Color corOriginal = Color.WHITE;
        Color corAviso = new Color(241, 196, 15, 100); // Amarelo com transparência

        // Criar animação de piscar
        timerAnimacaoAtual = new Timer(100, null);
        final int[] contador = { 0 };

        timerAnimacaoAtual.addActionListener(e -> {
            if (contador[0] % 2 == 0) {
                panelInfoItem.setBackground(corAviso);
            } else {
                panelInfoItem.setBackground(corOriginal);
            }

            contador[0]++;

            // Parar após 3 piscadas (6 mudanças)
            if (contador[0] >= 6) {
                timerAnimacaoAtual.stop();
                panelInfoItem.setBackground(corOriginal);
            }
        });

        timerAnimacaoAtual.start();

        // Mostrar mensagem temporária no label de resumo
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        String textoOriginal = salaAtual != null 
            ? String.format("Coletando em: %s", salaAtual.getIdentificacaoCompleta())
            : "Selecione uma sala para iniciar a coleta";
        lblResumoSala.setText("⚠️ " + mensagem);
        lblResumoSala.setForeground(new Color(241, 196, 15));
        lblResumoSala.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Restaurar texto original após 3 segundos
        timerTextoAtual = new Timer(3000, e -> {
            lblResumoSala.setText(textoOriginal);
            lblResumoSala.setForeground(new Color(100, 100, 100));
            lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        });
        timerTextoAtual.setRepeats(false);
        timerTextoAtual.start();
    }

    /**
     * Cria um botão estilizado no padrão do MainFrame.
     * Delega para {@link com.inventario.sihcp.view.ui.StyledButtonFactory}.
     *
     * @param text      Texto do botão (pode conter emojis)
     * @param baseColor Cor base do botão
     * @return JButton estilizado
     */
    private JButton createStyledButton(String text, Color baseColor) {
        return com.inventario.sihcp.view.ui.StyledButtonFactory.create(text, baseColor);
    }
}   
