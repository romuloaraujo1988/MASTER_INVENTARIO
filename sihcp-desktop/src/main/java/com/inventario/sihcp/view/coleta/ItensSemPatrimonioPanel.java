package com.inventario.sihcp.view.coleta;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.ParticipanteInventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.offline.ColetaOfflineService;
import com.inventario.sihcp.offline.OfflineManager;
import com.inventario.sihcp.util.SoundNotification;
import com.inventario.sihcp.view.ui.StyledButtonFactory;

/**
 * Painel da aba "Itens Sem Patrimônio": permite pesquisar descrições existentes,
 * registrar itens coletados sem etiqueta de patrimônio na sala atual, listar os
 * itens já registrados e removê-los (admin/supervisor).
 *
 * <p>Extraído de {@code ColetaFrame_v2} como parte da refatoração estrutural,
 * seguindo o mesmo padrão de {@link ItensPendentesPanel}. O painel é autocontido:
 * recebe os DAOs, serviços offline, o usuário logado e dois callbacks no
 * construtor (um {@link Supplier} que fornece a sala atual e um {@link Consumer}
 * para registrar workers no frame).</p>
 *
 * <p>O comportamento de negócio foi preservado integralmente em relação ao código
 * original do frame, inclusive os logs de DEBUG, mensagens, filtros por inventário
 * ativo e o caminho offline via {@code coletaOfflineService}/{@code offlineManager}.</p>
 */
public class ItensSemPatrimonioPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ItensSemPatrimonioPanel.class);

    private static final Color COR_FUNDO = new Color(248, 249, 250);
    private static final Color COR_PRIMARIA = new Color(52, 73, 94);
    private static final Color COR_TEXTO = new Color(44, 62, 80);

    // === Dependências injetadas ===
    private final PatrimonioDAO patrimonioDAO;
    private final ColetaDAO coletaDAO;
    private final InventarioDAO inventarioDAO;
    private final ColetaOfflineService coletaOfflineService;
    private final OfflineManager offlineManager;
    private final Usuario usuarioLogado;
    private final Supplier<Sala> salaSupplier;
    private final Consumer<SwingWorker<?, ?>> workerRegistrar;

    // === Componentes de busca por descrição ===
    private JTextField campoBuscaDescricao;
    private JButton btnBuscarDescricao;
    private JTable tabelaResultadosDescricao;
    private DefaultTableModel modeloTabelaResultados;

    // === Componentes de loading ===
    private JLabel lblLoadingDescricao;
    private JProgressBar progressBarDescricao;

    // === Componentes de agrupamento de itens sem etiqueta ===
    private JButton btnVerItensAgrupados;
    private JTable tabelaItensAgrupados;
    private DefaultTableModel modeloTabelaItensAgrupados;
    private JScrollPane scrollItensAgrupados;
    private JPanel panelItensAgrupados;
    private JDialog dialogItensAgrupados;

    // === Componentes do formulário / tabela de registrados ===
    private JTextField campoDescricaoSemPatrimonio;
    private JTextArea areaObservacoesSemPatrimonio;
    private JTextField campoLocalizacaoSemPatrimonio;
    private JTable tabelaSemPatrimonio;
    private DefaultTableModel modeloTabelaSemPatrimonio;
    private JLabel lblTotalItensSemPatrimonio;
    private JButton btnRegistrarSemPatrimonio;
    private JButton btnRemoverSemPatrimonio;
    private JButton btnExportarSemPatrimonio;

    /**
     * @param patrimonioDAO        DAO de patrimônios
     * @param coletaDAO            DAO de coletas
     * @param inventarioDAO        DAO de inventários
     * @param coletaOfflineService serviço de coleta com suporte offline
     * @param offlineManager       gerenciador de estado offline
     * @param usuarioLogado        usuário autenticado (para permissões)
     * @param salaSupplier         fornece a sala atualmente selecionada
     * @param workerRegistrar      callback para registrar o SwingWorker no frame
     *                             (permite cancelamento ao trocar de sala/fechar)
     */
    public ItensSemPatrimonioPanel(PatrimonioDAO patrimonioDAO,
                                   ColetaDAO coletaDAO,
                                   InventarioDAO inventarioDAO,
                                   ColetaOfflineService coletaOfflineService,
                                   OfflineManager offlineManager,
                                   Usuario usuarioLogado,
                                   Supplier<Sala> salaSupplier,
                                   Consumer<SwingWorker<?, ?>> workerRegistrar) {
        this.patrimonioDAO = patrimonioDAO;
        this.coletaDAO = coletaDAO;
        this.inventarioDAO = inventarioDAO;
        this.coletaOfflineService = coletaOfflineService;
        this.offlineManager = offlineManager;
        this.usuarioLogado = usuarioLogado;
        this.salaSupplier = salaSupplier;
        this.workerRegistrar = workerRegistrar;
        construirUI();
    }

    private void construirUI() {
        // Componentes de "itens agrupados" eram criados em initializeComponents do frame
        criarComponentesItensAgrupados();

        setLayout(new BorderLayout(8, 8));
        setBackground(COR_FUNDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== PAINEL SUPERIOR: PESQUISA DE DESCRIÇÕES ==========
        JPanel painelPesquisa = criarPainelPesquisaDescricoes(COR_PRIMARIA);

        // ========== PAINEL CENTRAL: SPLIT ENTRE RESULTADOS E FORMULÁRIO ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.55);

        // Painel esquerdo: Resultados da pesquisa
        JPanel painelResultados = criarPainelResultadosPesquisa(COR_TEXTO);

        // Painel direito: Formulário de registro
        JPanel painelFormulario = criarPainelFormularioRegistro(COR_TEXTO);

        splitPane.setLeftComponent(painelResultados);
        splitPane.setRightComponent(painelFormulario);

        // ========== PAINEL INFERIOR: TABELA DE ITENS REGISTRADOS ==========
        JPanel painelTabela = criarPainelTabelaItensRegistrados(COR_TEXTO);

        // ========== MONTAGEM FINAL ==========
        add(painelPesquisa, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(painelTabela, BorderLayout.SOUTH);
    }

    /**
     * Cria os componentes de agrupamento de itens sem etiqueta (tabela, scroll e
     * botão), que originalmente eram criados em {@code initializeComponents} do
     * frame. Também registra os listeners que ficavam em {@code setupEventListeners}.
     */
    private void criarComponentesItensAgrupados() {
        // Tabela para agrupamento de itens sem etiqueta
        String[] colunasItensAgrupados = { "Descrição", "Quantidade", "Categorias", "Primeira Coleta",
                "Última Coleta" };
        modeloTabelaItensAgrupados = new DefaultTableModel(colunasItensAgrupados, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaItensAgrupados = new JTable(modeloTabelaItensAgrupados);
        tabelaItensAgrupados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurações da tabela de itens agrupados
        tabelaItensAgrupados.getColumnModel().getColumn(0).setPreferredWidth(250); // Descrição
        tabelaItensAgrupados.getColumnModel().getColumn(1).setPreferredWidth(80); // Quantidade
        tabelaItensAgrupados.getColumnModel().getColumn(2).setPreferredWidth(150); // Categorias
        tabelaItensAgrupados.getColumnModel().getColumn(3).setPreferredWidth(120); // Primeira Coleta
        tabelaItensAgrupados.getColumnModel().getColumn(4).setPreferredWidth(120); // Última Coleta

        tabelaItensAgrupados.setRowHeight(24);
        tabelaItensAgrupados.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        tabelaItensAgrupados.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));

        scrollItensAgrupados = new JScrollPane(tabelaItensAgrupados);
        scrollItensAgrupados.setPreferredSize(new Dimension(750, 400));
        scrollItensAgrupados.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollItensAgrupados.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Botão para ver itens agrupados com design moderno
        btnVerItensAgrupados = StyledButtonFactory.create("📊 Ver Itens Sem Etiqueta Agrupados", new Color(52, 152, 219));
        btnVerItensAgrupados.setEnabled(false);
        btnVerItensAgrupados.addActionListener(e -> mostrarItensAgrupados());

        // Painel para itens agrupados (inicialmente oculto)
        panelItensAgrupados = new JPanel(new BorderLayout());
        panelItensAgrupados.setBorder(BorderFactory.createTitledBorder("Itens Sem Etiqueta Agrupados"));
        panelItensAgrupados.setVisible(false);

        // Listener para seleção na tabela de itens agrupados
        tabelaItensAgrupados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarDescricaoAgrupada();
            }
        });
    }

    /**
     * Cria o painel de pesquisa de descrições
     */
    private JPanel criarPainelPesquisaDescricoes(Color corPrimaria) {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        painel.setPreferredSize(new Dimension(0, 110)); // Aumentado para 110

        // Título
        JLabel lblTitulo = new JLabel("Pesquisar Descrições Existentes");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(corPrimaria);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // Painel de busca com GridBagLayout para melhor controle
        JPanel painelBusca = new JPanel(new GridBagLayout());
        painelBusca.setBackground(Color.WHITE);
        painelBusca.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPesquisa = new JLabel("Digite palavras-chave:");
        lblPesquisa.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        campoBuscaDescricao = new JTextField();
        campoBuscaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoBuscaDescricao.setPreferredSize(new Dimension(350, 30));
        campoBuscaDescricao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        btnBuscarDescricao = StyledButtonFactory.create("🔍 Pesquisar", new Color(108, 117, 125));
        btnBuscarDescricao.addActionListener(e -> buscarPorDescricao());

        // Listener para Enter no campo de busca
        campoBuscaDescricao.addActionListener(e -> buscarPorDescricao());

        // Adicionar componentes ao painel de busca
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        painelBusca.add(lblPesquisa, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelBusca.add(campoBuscaDescricao, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        painelBusca.add(btnBuscarDescricao, gbc);

        // Instruções - mais compactas
        JLabel lblInstrucao = new JLabel(
                "<html><i>💡 Digite parte da descrição e pressione Enter | 📍 Localização definida pela sala selecionada</i></html>");
        lblInstrucao.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        lblInstrucao.setForeground(new Color(120, 120, 120));
        lblInstrucao.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(painelBusca, BorderLayout.CENTER);
        painel.add(lblInstrucao, BorderLayout.SOUTH);

        return painel;
    }

    /**
     * Cria o painel de resultados da pesquisa
     */
    private JPanel criarPainelResultadosPesquisa(Color corTexto) {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Definir tamanho fixo para o painel
        painel.setPreferredSize(new Dimension(500, 400));
        painel.setMinimumSize(new Dimension(400, 300));

        // Título
        JLabel lblTitulo = new JLabel("📋 Descrições Encontradas");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lblTitulo.setForeground(corTexto);

        // Painel de loading (inicialmente oculto)
        JPanel painelLoading = new JPanel(new GridBagLayout());
        painelLoading.setBackground(Color.WHITE);
        painelLoading.setVisible(false);

        GridBagConstraints gbcLoading = new GridBagConstraints();
        gbcLoading.gridx = 0;
        gbcLoading.gridy = 0;
        gbcLoading.insets = new Insets(10, 10, 10, 10);

        // Label de loading com ícone animado
        lblLoadingDescricao = new JLabel("🔍 Pesquisando descrições...");
        lblLoadingDescricao.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        lblLoadingDescricao.setForeground(new Color(52, 152, 219));
        painelLoading.add(lblLoadingDescricao, gbcLoading);

        gbcLoading.gridy = 1;
        progressBarDescricao = new JProgressBar();
        progressBarDescricao.setIndeterminate(true);
        progressBarDescricao.setPreferredSize(new Dimension(300, 25));
        progressBarDescricao.setStringPainted(true);
        progressBarDescricao.setString("Aguarde...");
        progressBarDescricao.setForeground(new Color(52, 152, 219));
        painelLoading.add(progressBarDescricao, gbcLoading);

        // Tabela de resultados - apenas Descrição
        String[] colunas = { "Descrição" };
        modeloTabelaResultados = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaResultadosDescricao = new JTable(modeloTabelaResultados);
        tabelaResultadosDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabelaResultadosDescricao.setRowHeight(28);
        tabelaResultadosDescricao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaResultadosDescricao.setBackground(Color.WHITE);
        tabelaResultadosDescricao.setGridColor(new Color(230, 230, 230));
        tabelaResultadosDescricao.setShowGrid(true);

        // Configurar largura - coluna única ocupa todo o espaço
        tabelaResultadosDescricao.getColumnModel().getColumn(0).setPreferredWidth(450);

        // Cabeçalho
        tabelaResultadosDescricao.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabelaResultadosDescricao.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaResultadosDescricao.getTableHeader().setForeground(Color.WHITE);
        tabelaResultadosDescricao.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Listener para seleção na tabela
        tabelaResultadosDescricao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarPatrimonioDescricao();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabelaResultadosDescricao);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // Painel central que alterna entre loading e tabela
        JPanel painelCentral = new JPanel(new CardLayout());
        painelCentral.add(scrollPane, "tabela");
        painelCentral.add(painelLoading, "loading");

        // Instrução
        JLabel lblInstrucao = new JLabel("<html><i>Clique em uma descrição para usá-la no formulário →</i></html>");
        lblInstrucao.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblInstrucao.setForeground(new Color(120, 120, 120));

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(painelCentral, BorderLayout.CENTER);
        painel.add(lblInstrucao, BorderLayout.SOUTH);

        return painel;
    }

    /**
     * Cria o painel do formulário de registro
     */
    private JPanel criarPainelFormularioRegistro(Color corTexto) {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Título
        JLabel lblTitulo = new JLabel("✏️ Registrar Item Sem Patrimônio");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lblTitulo.setForeground(corTexto);

        // Formulário
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Campo Localização (movido para o topo)
        // NOTA: Este campo é preenchido automaticamente com a sala selecionada na aba
        // "Coleta Normal"
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblLocalizacao = new JLabel("Localização * (definida pela sala)");
        lblLocalizacao.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLocalizacao.setForeground(new Color(52, 73, 94));
        formulario.add(lblLocalizacao, gbc);

        gbc.gridy = 1;
        campoLocalizacaoSemPatrimonio = new JTextField();
        campoLocalizacaoSemPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoLocalizacaoSemPatrimonio.setPreferredSize(new Dimension(0, 30));
        campoLocalizacaoSemPatrimonio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        campoLocalizacaoSemPatrimonio.setEditable(false);
        campoLocalizacaoSemPatrimonio.setBackground(new Color(245, 245, 245));
        campoLocalizacaoSemPatrimonio
                .setToolTipText("A localização é definida pela sala selecionada na aba 'Coleta Normal'");
        formulario.add(campoLocalizacaoSemPatrimonio, gbc);

        // Campo Descrição
        gbc.gridy = 2;
        JLabel lblDescricao = new JLabel("Descrição do Item *");
        lblDescricao.setFont(new Font("Segoe UI", Font.BOLD, 11));
        formulario.add(lblDescricao, gbc);

        gbc.gridy = 3;
        campoDescricaoSemPatrimonio = new JTextField();
        campoDescricaoSemPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoDescricaoSemPatrimonio.setPreferredSize(new Dimension(0, 30));
        campoDescricaoSemPatrimonio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        formulario.add(campoDescricaoSemPatrimonio, gbc);

        // Campo Observações
        gbc.gridy = 4;
        JLabel lblObservacoes = new JLabel("Observações");
        lblObservacoes.setFont(new Font("Segoe UI", Font.BOLD, 11));
        formulario.add(lblObservacoes, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        areaObservacoesSemPatrimonio = new JTextArea(2, 20);
        areaObservacoesSemPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        areaObservacoesSemPatrimonio.setLineWrap(true);
        areaObservacoesSemPatrimonio.setWrapStyleWord(true);
        areaObservacoesSemPatrimonio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JScrollPane scrollObs = new JScrollPane(areaObservacoesSemPatrimonio);
        scrollObs.setBorder(null);
        formulario.add(scrollObs, gbc);

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(formulario, BorderLayout.CENTER);

        return painel;
    }

    /**
     * Cria o painel da tabela de itens registrados
     */
    private JPanel criarPainelTabelaItensRegistrados(Color corTexto) {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        painel.setPreferredSize(new Dimension(0, 220));

        // Cabeçalho com título, contador e botões
        JPanel painelCabecalho = new JPanel(new BorderLayout());
        painelCabecalho.setBackground(Color.WHITE);

        // Painel esquerdo com título e contador
        JPanel painelTituloContador = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelTituloContador.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("📊 Itens Sem Patrimônio Registrados");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lblTitulo.setForeground(corTexto);

        // Instanciar e configurar o label do contador
        lblTotalItensSemPatrimonio = new JLabel("Total: 0 item(ns)");
        lblTotalItensSemPatrimonio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalItensSemPatrimonio.setForeground(new Color(52, 152, 219));
        lblTotalItensSemPatrimonio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        painelTituloContador.add(lblTitulo);
        painelTituloContador.add(lblTotalItensSemPatrimonio);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelBotoes.setBackground(Color.WHITE);

        // Criar botões Registrar e Limpar
        btnRegistrarSemPatrimonio = StyledButtonFactory.create("✅ Registrar", new Color(46, 204, 113));
        btnRegistrarSemPatrimonio.setPreferredSize(new Dimension(120, 32));
        btnRegistrarSemPatrimonio.addActionListener(e -> registrarColetaComDescricaoSelecionada());

        JButton btnLimpar = StyledButtonFactory.create("🗑️ Limpar", new Color(108, 117, 125));
        btnLimpar.setPreferredSize(new Dimension(120, 32));
        btnLimpar.addActionListener(e -> limparFormularioSemPatrimonio());

        btnRemoverSemPatrimonio = StyledButtonFactory.create("❌ Remover", new Color(231, 76, 60));
        btnRemoverSemPatrimonio.setPreferredSize(new Dimension(120, 32));
        btnRemoverSemPatrimonio.setEnabled(false);
        btnRemoverSemPatrimonio.addActionListener(e -> removerItemSemPatrimonioSelecionado());

        // Ocultar botão remover para coletores (apenas admin e supervisor podem
        // remover)
        if (usuarioLogado != null) {
            String perfil = usuarioLogado.getPerfil().name();
            btnRemoverSemPatrimonio.setVisible("ADMIN".equals(perfil) || "SUPERVISOR".equals(perfil));
        } else {
            btnRemoverSemPatrimonio.setVisible(false);
        }

        JButton btnAtualizar = StyledButtonFactory.create("🔄 Atualizar", new Color(52, 152, 219));
        btnAtualizar.setPreferredSize(new Dimension(120, 32));
        btnAtualizar.addActionListener(e -> carregarTodosItensSemPatrimonio());

        // Adicionar botões na ordem: Registrar, Limpar, Remover (se visível), Atualizar
        painelBotoes.add(btnRegistrarSemPatrimonio);
        painelBotoes.add(btnLimpar);
        if (btnRemoverSemPatrimonio.isVisible()) {
            painelBotoes.add(btnRemoverSemPatrimonio);
        }
        painelBotoes.add(btnAtualizar);

        painelCabecalho.add(painelTituloContador, BorderLayout.WEST);
        painelCabecalho.add(painelBotoes, BorderLayout.EAST);

        // Tabela
        String[] colunas = { "Data/Hora", "Descrição", "Categoria", "Localização" };
        modeloTabelaSemPatrimonio = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaSemPatrimonio = new JTable(modeloTabelaSemPatrimonio);
        tabelaSemPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabelaSemPatrimonio.setRowHeight(30);
        tabelaSemPatrimonio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaSemPatrimonio.setBackground(Color.WHITE);
        tabelaSemPatrimonio.setGridColor(new Color(230, 230, 230));
        tabelaSemPatrimonio.setShowGrid(true);

        // Configurar larguras
        tabelaSemPatrimonio.getColumnModel().getColumn(0).setPreferredWidth(120);
        tabelaSemPatrimonio.getColumnModel().getColumn(1).setPreferredWidth(350);
        tabelaSemPatrimonio.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabelaSemPatrimonio.getColumnModel().getColumn(3).setPreferredWidth(200);

        // Cabeçalho
        tabelaSemPatrimonio.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabelaSemPatrimonio.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaSemPatrimonio.getTableHeader().setForeground(Color.WHITE);
        tabelaSemPatrimonio.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Listener para habilitar botão remover apenas quando uma linha for selecionada
        tabelaSemPatrimonio.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean temSelecao = tabelaSemPatrimonio.getSelectedRow() != -1;
                // Só habilitar se houver seleção E o botão estiver visível (admin/supervisor)
                btnRemoverSemPatrimonio.setEnabled(temSelecao && btnRemoverSemPatrimonio.isVisible());
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabelaSemPatrimonio);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        painel.add(painelCabecalho, BorderLayout.NORTH);
        painel.add(scrollPane, BorderLayout.CENTER);

        return painel;
    }

    // ===================== API pública (usada pelo frame) =====================

    /**
     * Define o texto do campo de localização (sala atual). Use {@code ""} para limpar.
     */
    public void setLocalizacao(String texto) {
        if (campoLocalizacaoSemPatrimonio != null) {
            campoLocalizacaoSemPatrimonio.setText(texto);
        }
    }

    /**
     * Carrega os itens sem patrimônio registrados na sala atual.
     */
    public void carregarItens() {
        carregarTodosItensSemPatrimonio();
    }

    /**
     * Carrega os itens sem etiqueta agrupados por descrição.
     */
    public void carregarAgrupados() {
        carregarItensAgrupados();
    }

    /**
     * Habilita/desabilita os componentes interativos do painel, replicando o
     * comportamento original de {@code desabilitarComponentesAbaItensSemPatrimonio}
     * (estado {@code false}) e do {@code case 2} de {@code habilitarComponentesPorAba}
     * (estado {@code true}).
     */
    public void setComponentesAtivos(boolean ativo) {
        if (campoDescricaoSemPatrimonio != null)
            campoDescricaoSemPatrimonio.setEnabled(ativo);
        if (campoLocalizacaoSemPatrimonio != null)
            campoLocalizacaoSemPatrimonio.setEnabled(ativo);
        if (areaObservacoesSemPatrimonio != null)
            areaObservacoesSemPatrimonio.setEnabled(ativo);
        if (btnRegistrarSemPatrimonio != null)
            btnRegistrarSemPatrimonio.setEnabled(ativo);
        if (campoBuscaDescricao != null)
            campoBuscaDescricao.setEnabled(ativo);
        if (btnBuscarDescricao != null)
            btnBuscarDescricao.setEnabled(ativo);
        if (tabelaResultadosDescricao != null)
            tabelaResultadosDescricao.setEnabled(ativo);
        if (btnVerItensAgrupados != null)
            btnVerItensAgrupados.setEnabled(ativo);
        if (tabelaSemPatrimonio != null)
            tabelaSemPatrimonio.setEnabled(ativo);
        if (btnRemoverSemPatrimonio != null)
            btnRemoverSemPatrimonio.setEnabled(ativo);
        if (btnExportarSemPatrimonio != null)
            btnExportarSemPatrimonio.setEnabled(ativo);
    }

    /**
     * Coloca o foco no campo de descrição.
     */
    public void focarDescricao() {
        SwingUtilities.invokeLater(() -> {
            if (campoDescricaoSemPatrimonio != null) {
                campoDescricaoSemPatrimonio.requestFocus();
            }
        });
    }

    // ===================== Lógica interna =====================

    /**
     * Carrega todos os itens sem patrimônio registrados na sala atual
     */
    private void carregarTodosItensSemPatrimonio() {
        System.out.println("=== DEBUG TIMESTAMP: Iniciando carregarTodosItensSemPatrimonio ===");

        Sala salaSelecionada = salaSupplier.get();
        if (salaSelecionada == null) {
            System.out.println("DEBUG TIMESTAMP: Nenhuma sala selecionada");
            JOptionPane.showMessageDialog(this,
                    "Selecione uma sala primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("DEBUG TIMESTAMP: Sala selecionada: " + salaSelecionada.getIdentificacaoCompleta());

        // Limpar tabela
        modeloTabelaSemPatrimonio.setRowCount(0);

        try {
            // Buscar itens sem etiqueta da sala (usando numeroSala para consistência com dados no banco).
            // IMPORTANTE: filtrar pelo inventário ativo para não trazer coletas de inventários antigos.
            System.out.println("DEBUG TIMESTAMP: Buscando itens sem etiqueta...");
            Inventario inventarioAtivoSemEtiq = inventarioDAO.buscarInventarioAtivo();
            Integer idInventarioSemEtiq = inventarioAtivoSemEtiq != null ? inventarioAtivoSemEtiq.getId() : null;
            List<Coleta> itensSemPatrimonio = coletaDAO.buscarColetasSemEtiquetaPorSala(
                    salaSelecionada.getIdSala(),
                    salaSelecionada.getNumeroSala(),
                    idInventarioSemEtiq);

            System.out.println("DEBUG TIMESTAMP: Encontrados " + itensSemPatrimonio.size() + " itens sem patrimônio");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            int contador = 0;
            for (Coleta coleta : itensSemPatrimonio) {
                contador++;
                System.out.println("\n--- DEBUG TIMESTAMP: Item sem patrimônio " + contador + " ---");

                // Log detalhado do timestamp
                java.sql.Timestamp dataColeta = coleta.getDataColeta();
                System.out.println("DEBUG TIMESTAMP: Data Coleta (raw): " + dataColeta);
                System.out.println("DEBUG TIMESTAMP: Data Coleta (class): " + (dataColeta != null ? dataColeta.getClass().getName() : "null"));

                String dataFormatada;
                try {
                    if (dataColeta != null) {
                        dataFormatada = sdf.format(dataColeta);
                        System.out.println("DEBUG TIMESTAMP: Data formatada: " + dataFormatada);
                    } else {
                        System.err.println("DEBUG TIMESTAMP: ERRO - dataColeta é null!");
                        dataFormatada = "DATA INVÁLIDA";
                    }
                } catch (Exception formatEx) {
                    System.err.println("DEBUG TIMESTAMP: ERRO ao formatar data: " + formatEx.getMessage());
                    dataFormatada = "ERRO FORMATO";
                }

                Object[] linha = {
                        dataFormatada,
                        coleta.getDescricaoItemSemEtiqueta(),
                        coleta.getCategoriaItemSemEtiqueta() != null ? coleta.getCategoriaItemSemEtiqueta() : "-",
                        coleta.getLocalizacaoEncontrada()
                };
                modeloTabelaSemPatrimonio.addRow(linha);
            }

            // Atualizar label de total
            if (lblTotalItensSemPatrimonio != null) {
                lblTotalItensSemPatrimonio.setText(
                        String.format("Total: %d item(ns) sem patrimônio registrado(s)", itensSemPatrimonio.size()));
            }

            System.out.println("=== DEBUG TIMESTAMP: carregarTodosItensSemPatrimonio concluído ===");

        } catch (SQLException | RuntimeException e) {
            LOG.error("ERRO em carregarTodosItensSemPatrimonio: {}", e.getMessage(), e);

            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar itens sem patrimônio: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Registra um item sem patrimônio usando a descrição selecionada ou digitada
     */
    private void registrarColetaComDescricaoSelecionada() {
        Sala salaSelecionada = salaSupplier.get();
        if (salaSelecionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma sala primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String descricao = campoDescricaoSemPatrimonio.getText().trim();
        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe a descrição do item.",
                    "Campo Obrigatório",
                    JOptionPane.WARNING_MESSAGE);
            campoDescricaoSemPatrimonio.requestFocus();
            return;
        }

        // Limpar descrição removendo contadores "(X pendente(s))" se existirem
        String descricaoLimpa = descricao;
        int indexParenteses = descricao.lastIndexOf(" (");
        if (indexParenteses > 0 && descricao.endsWith(")")) {
            String possivelContador = descricao.substring(indexParenteses);
            // Verificar se é realmente um contador (contém "pendente")
            if (possivelContador.toLowerCase().contains("pendente")) {
                descricaoLimpa = descricao.substring(0, indexParenteses).trim();
            }
        }

        String observacoes = areaObservacoesSemPatrimonio.getText().trim();

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

            // Verificar permissão do usuário
            if (usuarioLogado == null) {
                JOptionPane.showMessageDialog(this,
                        "Usuário não autenticado.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Modo offline: autorização simplificada
            // ✅ CORRIGIDO: Buscar ID do participante do inventário
            ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
            Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
                inventarioAtivo.getId(), 
                usuarioLogado.getId()
            );
            
            if (idParticipante == null || idParticipante == 0) {
                // Fallback: usar ID do usuário
                idParticipante = usuarioLogado.getId();
                System.out.println("DEBUG: AVISO - Participante não encontrado, usando ID do usuário como fallback: " + idParticipante);
            } else {
                System.out.println("DEBUG: ID participante encontrado: " + idParticipante);
            }

            // Criar objeto Coleta para item sem patrimônio
            Coleta coleta = new Coleta();
            coleta.setIdInventario(inventarioAtivo.getId());
            coleta.setIdPatrimonio(0); // Sem patrimônio
            coleta.setIdColetor(usuarioLogado.getId());
            coleta.setIdParticipanteInventario(idParticipante);
            
            // DEBUG: Log detalhado da criação do timestamp (Item sem patrimônio)
            long currentTimeMillis1 = System.currentTimeMillis();
            Timestamp novoTimestamp1 = new Timestamp(currentTimeMillis1);
            System.out.println("=== DEBUG TIMESTAMP: Criando timestamp para item SEM PATRIMÔNIO ===");
            System.out.println("DEBUG TIMESTAMP: currentTimeMillis: " + currentTimeMillis1);
            System.out.println("DEBUG TIMESTAMP: Timestamp criado: " + novoTimestamp1);
            System.out.println("DEBUG TIMESTAMP: Timestamp.toString(): " + novoTimestamp1.toString());
            
            coleta.setDataColeta(novoTimestamp1);
            System.out.println("DEBUG TIMESTAMP: Timestamp setado na coleta");
            
            coleta.setStatusColeta("COLETADO");
            coleta.setObservacaoColeta(observacoes);
            // ✅ CORRIGIDO: Usar numeroSala para consistência com dados existentes no banco
            coleta.setLocalizacaoAtual(salaSelecionada.getNumeroSala());
            coleta.setLocalizacaoEncontrada(salaSelecionada.getNumeroSala());
            coleta.setEstadoEncontrado("N/A");
            coleta.setDivergencia(false);
            coleta.setSemEtiqueta(true);
            coleta.setDescricaoItemSemEtiqueta(descricaoLimpa); // Usar descrição limpa sem contadores
            coleta.setCategoriaItemSemEtiqueta("OUTROS"); // Categoria padrão

            // Inserir no banco (usando serviço offline)
            coletaOfflineService.salvarColeta(coleta);

            System.out.println("DEBUG: Coleta sem etiqueta salva - Modo: " +
                    offlineManager.getCurrentState());

            // Reproduzir som de sucesso
            SoundNotification.playColetaSalvaSound();

            JOptionPane.showMessageDialog(this,
                    "Item sem patrimônio registrado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpar formulário
            limparFormularioSemPatrimonio();

            // Recarregar tabela
            carregarTodosItensSemPatrimonio();

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao registrar item: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "Erro ao registrar item: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpa o formulário de itens sem patrimônio
     */
    private void limparFormularioSemPatrimonio() {
        campoDescricaoSemPatrimonio.setText("");
        areaObservacoesSemPatrimonio.setText("");
        campoDescricaoSemPatrimonio.requestFocus();
    }

    /**
     * Remove o item sem patrimônio selecionado na tabela
     */
    private void removerItemSemPatrimonioSelecionado() {
        int linhaSelecionada = tabelaSemPatrimonio.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um item para remover.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar remoção
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja remover este item sem patrimônio?",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Sala salaSelecionada = salaSupplier.get();
            if (salaSelecionada == null) {
                return;
            }

            // Obter dados da linha selecionada
            String dataHora = (String) modeloTabelaSemPatrimonio.getValueAt(linhaSelecionada, 0);
            String descricao = (String) modeloTabelaSemPatrimonio.getValueAt(linhaSelecionada, 1);
            String localizacao = (String) modeloTabelaSemPatrimonio.getValueAt(linhaSelecionada, 3);

            // Buscar e excluir a coleta (usando numeroSala para consistência com dados no banco).
            // Filtrar pelo inventário ativo para garantir que a exclusão incida apenas
            // no inventário corrente e não apague registros históricos.
            Inventario inventarioAtivoExcl = inventarioDAO.buscarInventarioAtivo();
            Integer idInventarioExcl = inventarioAtivoExcl != null ? inventarioAtivoExcl.getId() : null;
            List<Coleta> itensSemPatrimonio = coletaDAO.buscarColetasSemEtiquetaPorSala(
                    salaSelecionada.getIdSala(),
                    salaSelecionada.getNumeroSala(),
                    idInventarioExcl);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            boolean encontrado = false;

            for (Coleta coleta : itensSemPatrimonio) {
                if (sdf.format(coleta.getDataColeta()).equals(dataHora)
                        && coleta.getDescricaoItemSemEtiqueta().equals(descricao)
                        && coleta.getLocalizacaoEncontrada().equals(localizacao)) {
                    coletaDAO.excluirColeta(coleta.getId());
                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                JOptionPane.showMessageDialog(this,
                        "Item removido com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);

                // Recarregar tabela
                carregarTodosItensSemPatrimonio();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Item não encontrado no banco de dados.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao remover item: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "Erro ao remover item: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Busca descrições de patrimônios, mostrando apenas os pendentes (não
     * coletados)
     * Facilita o trabalho do coletor ao exibir somente itens que ainda precisam ser
     * coletados
     */
    private void buscarPorDescricao() {
        String termoBusca = campoBuscaDescricao.getText().trim();

        if (termoBusca.isEmpty()) {
            lblLoadingDescricao.setText("⚠️ Digite uma descrição para buscar");
            lblLoadingDescricao.setForeground(new Color(241, 196, 15));
            mostrarLoading(true);

            Timer timer = new Timer(2000, e -> mostrarLoading(false));
            timer.setRepeats(false);
            timer.start();
            return;
        }

        // Mostrar loading
        mostrarLoading(true);
        lblLoadingDescricao.setText("🔍 Pesquisando descrições...");
        lblLoadingDescricao.setForeground(new Color(52, 152, 219));
        progressBarDescricao.setString("Buscando no banco de dados...");

        // Desabilitar botão durante busca
        btnBuscarDescricao.setEnabled(false);
        campoBuscaDescricao.setEnabled(false);

        // Executar busca em background
        SwingWorker<ResultadoBusca, Void> worker = new SwingWorker<ResultadoBusca, Void>() {
            @Override
            protected ResultadoBusca doInBackground() throws Exception {
                // Limpar resultados anteriores
                SwingUtilities.invokeLater(() -> modeloTabelaResultados.setRowCount(0));

                // Buscar patrimônios por descrição
                List<Patrimonio> patrimonios = patrimonioDAO.buscarPorDescricao(termoBusca);

                if (patrimonios == null || patrimonios.isEmpty()) {
                    return new ResultadoBusca(false, "Nenhuma descrição encontrada com o termo: \"" + termoBusca + "\"",
                            0, 0, 0);
                }

                // Buscar inventário ativo para filtrar pendentes
                Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                Integer idInventario = inventarioAtivo != null ? inventarioAtivo.getId() : null;

                // Filtrar apenas patrimônios pendentes (não coletados)
                List<Patrimonio> patrimoniosPendentes = new ArrayList<>();
                for (Patrimonio p : patrimonios) {
                    try {
                        boolean jaColetado = coletaDAO.coletaExiste(idInventario, p.getId());
                        if (!jaColetado) {
                            patrimoniosPendentes.add(p);
                        }
                    } catch (SQLException | RuntimeException e) {
                        // Em caso de erro, incluir o patrimônio
                        patrimoniosPendentes.add(p);
                    }
                }

                if (patrimoniosPendentes.isEmpty()) {
                    return new ResultadoBusca(false, "Todos os patrimônios com essa descrição já foram coletados!",
                            patrimonios.size(), 0, patrimonios.size());
                }

                // Agrupar por descrição única (apenas pendentes)
                java.util.Set<String> descricoesUnicas = new java.util.LinkedHashSet<>();
                java.util.Map<String, Integer> contagemPorDescricao = new java.util.HashMap<>();

                for (Patrimonio p : patrimoniosPendentes) {
                    String desc = p.getDescricao();
                    if (desc != null && !desc.trim().isEmpty()) {
                        descricoesUnicas.add(desc);
                        contagemPorDescricao.put(desc, contagemPorDescricao.getOrDefault(desc, 0) + 1);
                    }
                }

                // Adicionar à tabela com contagem de pendentes
                for (String descricao : descricoesUnicas) {
                    int qtdPendente = contagemPorDescricao.get(descricao);
                    String descricaoComContagem = String.format("%s (%d pendente%s)",
                            descricao, qtdPendente, qtdPendente > 1 ? "s" : "");
                    Object[] linha = { descricaoComContagem };
                    SwingUtilities.invokeLater(() -> modeloTabelaResultados.addRow(linha));
                }

                return new ResultadoBusca(true, "Busca concluída com sucesso!",
                        patrimonios.size(), patrimoniosPendentes.size(),
                        patrimonios.size() - patrimoniosPendentes.size());
            }

            @Override
            protected void done() {
                try {
                    ResultadoBusca resultado = get();

                    // Ocultar loading
                    mostrarLoading(false);

                    // Reabilitar componentes
                    btnBuscarDescricao.setEnabled(true);
                    campoBuscaDescricao.setEnabled(true);

                    // Mostrar resultado na label de loading temporariamente
                    if (resultado.sucesso) {
                        lblLoadingDescricao.setText(
                                String.format("✅ %d descrição(ões) encontrada(s) | %d pendente(s) | %d coletado(s)",
                                        modeloTabelaResultados.getRowCount(), resultado.totalPendentes,
                                        resultado.totalColetados));
                        lblLoadingDescricao.setForeground(new Color(46, 204, 113));
                    } else {
                        lblLoadingDescricao.setText("ℹ️ " + resultado.mensagem);
                        lblLoadingDescricao.setForeground(new Color(241, 196, 15));
                    }

                    // Mostrar mensagem temporariamente
                    mostrarLoading(true);
                    progressBarDescricao.setVisible(false);

                    Timer timer = new Timer(3000, e -> {
                        mostrarLoading(false);
                        progressBarDescricao.setVisible(true);
                    });
                    timer.setRepeats(false);
                    timer.start();

                } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                    mostrarLoading(false);
                    btnBuscarDescricao.setEnabled(true);
                    campoBuscaDescricao.setEnabled(true);

                    lblLoadingDescricao.setText("❌ Erro ao buscar: " + e.getMessage());
                    lblLoadingDescricao.setForeground(new Color(231, 76, 60));
                    mostrarLoading(true);
                    progressBarDescricao.setVisible(false);

                    Timer timer = new Timer(3000, ev -> {
                        mostrarLoading(false);
                        progressBarDescricao.setVisible(true);
                    });
                    timer.setRepeats(false);
                    timer.start();

                    LOG.error("Erro ao buscar por descrição", e);
                    Thread.currentThread().interrupt();
                }
            }
        };

        if (workerRegistrar != null) {
            workerRegistrar.accept(worker);
        }
        worker.execute();
    }

    /**
     * Mostra ou oculta o indicador de loading
     */
    private void mostrarLoading(boolean mostrar) {
        // Encontrar o painel central que contém o CardLayout
        Component[] components = tabelaResultadosDescricao.getParent().getParent().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel painelCentral) {
                if (painelCentral.getLayout() instanceof CardLayout layout) {
                    if (mostrar) {
                        layout.show(painelCentral, "loading");
                    } else {
                        layout.show(painelCentral, "tabela");
                    }
                    break;
                }
            }
        }
    }

    /**
     * Classe auxiliar para armazenar resultado da busca
     */
    private static class ResultadoBusca {
        boolean sucesso;
        String mensagem;
        int totalPendentes;
        int totalColetados;

        ResultadoBusca(boolean sucesso, String mensagem, int totalEncontrados, int totalPendentes, int totalColetados) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
            // totalEncontrados não é usado, apenas totalPendentes e totalColetados
            this.totalPendentes = totalPendentes;
            this.totalColetados = totalColetados;
        }
    }

    /**
     * Seleciona uma descrição da tabela de resultados e preenche o formulário
     */
    private void selecionarPatrimonioDescricao() {
        int linhaSelecionada = tabelaResultadosDescricao.getSelectedRow();

        if (linhaSelecionada == -1) {
            return;
        }

        try {
            String descricaoComContagem = (String) modeloTabelaResultados.getValueAt(linhaSelecionada, 0);

            // Extrair apenas a descrição, removendo a parte "(X pendente(s))"
            String descricaoLimpa = descricaoComContagem;
            int indexParenteses = descricaoComContagem.lastIndexOf(" (");
            if (indexParenteses > 0) {
                descricaoLimpa = descricaoComContagem.substring(0, indexParenteses).trim();
            }

            // Preencher campo de descrição do formulário com a descrição limpa
            campoDescricaoSemPatrimonio.setText(descricaoLimpa);

            // Focar no campo de observações
            areaObservacoesSemPatrimonio.requestFocus();

        } catch (Exception e) {
            LOG.error("Erro ao selecionar descrição: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "Erro ao selecionar descrição: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carrega itens sem etiqueta agrupados por descrição
     */
    private void carregarItensAgrupados() {
        try {
            // Limpar dados anteriores
            modeloTabelaItensAgrupados.setRowCount(0);

            // Buscar itens agrupados
            List<Object[]> itensAgrupados = coletaDAO.agruparItensSemEtiquetaPorDescricao();

            for (Object[] item : itensAgrupados) {
                // Estrutura retornada pelo DAO (4 colunas):
                // item[0] = descricao
                // item[1] = categoria
                // item[2] = quantidade
                // item[3] = ultima_coleta
                
                // Tabela espera 5 colunas: Descrição, Quantidade, Categorias, Primeira Coleta, Última Coleta
                Object[] linha = {
                        item[0] != null ? item[0] : "N/A",              // Descrição
                        item[2] != null ? item[2] : 0,                  // Quantidade
                        item[1] != null ? item[1] : "N/A",              // Categorias
                        "N/A",                                          // Primeira Coleta (não disponível no DAO)
                        item[3] != null ? item[3].toString() : "N/A"    // Última Coleta
                };
                modeloTabelaItensAgrupados.addRow(linha);
            }

            // Forçar atualização da tabela
            tabelaItensAgrupados.revalidate();
            tabelaItensAgrupados.repaint();

        } catch (SQLException | RuntimeException e) {
            LOG.error("Erro ao carregar itens agrupados: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao carregar itens agrupados: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mostra dialog com itens agrupados
     */
    private void mostrarItensAgrupados() {
        if (dialogItensAgrupados == null) {
            dialogItensAgrupados = new JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                    "Itens Sem Etiqueta Agrupados", true);
            dialogItensAgrupados.setLayout(new BorderLayout());

            JPanel panelDialog = new JPanel(new BorderLayout());
            panelDialog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel lblTitulo = new JLabel("<html><b>Itens sem etiqueta agrupados por descrição similar</b><br>" +
                    "<i>Clique em uma linha para usar a descrição no formulário</i></html>");
            lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            panelDialog.add(lblTitulo, BorderLayout.NORTH);
            panelDialog.add(scrollItensAgrupados, BorderLayout.CENTER);

            JButton btnFechar = new JButton("Fechar");
            btnFechar.addActionListener(e -> dialogItensAgrupados.setVisible(false));

            JPanel panelBotoesDialog = new JPanel(new FlowLayout());
            panelBotoesDialog.add(btnFechar);
            panelDialog.add(panelBotoesDialog, BorderLayout.SOUTH);

            dialogItensAgrupados.add(panelDialog);
            dialogItensAgrupados.setSize(800, 500);
            dialogItensAgrupados.setLocationRelativeTo(this);
        }

        // Recarregar dados antes de mostrar
        carregarItensAgrupados();
        dialogItensAgrupados.setVisible(true);
    }

    /**
     * Seleciona uma descrição agrupada e preenche o campo
     */
    private void selecionarDescricaoAgrupada() {
        int linhaSelecionada = tabelaItensAgrupados.getSelectedRow();

        if (linhaSelecionada == -1) {
            return;
        }

        String descricaoSelecionada = (String) modeloTabelaItensAgrupados.getValueAt(linhaSelecionada, 0);

        // MUDANÇA DE COMPORTAMENTO: o frame original preenchia o campo invisível
        // txtDescricaoSemEtiqueta (removido na refatoração). Agora preenchemos o campo
        // visível do formulário (campoDescricaoSemPatrimonio), igual ao que
        // selecionarPatrimonioDescricao já faz.
        campoDescricaoSemPatrimonio.setText(descricaoSelecionada);

        // Fechar dialog se estiver aberto
        if (dialogItensAgrupados != null && dialogItensAgrupados.isVisible()) {
            dialogItensAgrupados.setVisible(false);
        }

        JOptionPane.showMessageDialog(this, "Descrição selecionada: " + descricaoSelecionada,
                "Seleção", JOptionPane.INFORMATION_MESSAGE);
    }
}
