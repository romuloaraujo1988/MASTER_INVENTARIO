package com.inventario.sihcp.view.coleta;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.ParticipanteInventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.SalaInventarioDAO;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.offline.ColetaOfflineService;
import com.inventario.sihcp.offline.OfflineManager;
import com.inventario.sihcp.util.SoundNotification;
import com.inventario.sihcp.view.ui.StyledButtonFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Painel da aba "Coleta Normal": busca de patrimônio (manual ou via leitor de
 * código de barras), registro de coletas na sala atual, histórico de coletas da
 * sala, e ações de finalizar/reabrir/remover coletas conforme o perfil.
 *
 * <p>Extraído de {@code ColetaFrame_v2} como parte da refatoração estrutural,
 * seguindo o mesmo padrão de {@link ItensPendentesPanel} e
 * {@link ItensSemPatrimonioPanel} (Opção A: o {@code comboSalas} permanece no
 * frame como coordenador). O painel é autocontido: recebe os DAOs, serviços
 * offline, o usuário logado e dois callbacks no construtor (um {@link Supplier}
 * que fornece a sala atual e um {@link Consumer} para registrar workers no
 * frame).</p>
 *
 * <p>O comportamento de negócio foi preservado integralmente em relação ao
 * código original do frame, inclusive os logs de DEBUG, mensagens, métricas de
 * tempo, sons ({@code SoundNotification}), caminho offline
 * ({@code coletaOfflineService}/{@code offlineManager}), filtros por inventário
 * ativo e regras de permissão (ADMIN/SUPERVISOR/CONSULTA).</p>
 */
public class ColetaNormalPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ColetaNormalPanel.class);

    // Limite de registros para performance
    private static final int LIMITE_HISTORICO = 50;  // Carregar apenas últimos 50 patrimônios
    private static final int LIMITE_TODAS_COLETAS = 500; // Limite para visualização de todas as coletas

    // === Dependências injetadas ===
    private final PatrimonioDAO patrimonioDAO;
    private final ColetaDAO coletaDAO;
    private final InventarioDAO inventarioDAO;
    private final SalaInventarioDAO salaInventarioDAO;
    private final ParticipanteInventarioDAO participanteInventarioDAO;
    private final ColetaOfflineService coletaOfflineService;
    private final OfflineManager offlineManager;
    private final Usuario usuarioLogado;
    private final Supplier<Sala> salaSupplier;
    private final Consumer<SwingWorker<?, ?>> workerRegistrar;

    // === Componentes ===
    private JTextField campoBusca;
    private JButton btnBuscar;
    private JTextArea campoObservacao;
    private JComboBox<String> comboEstado;
    private JButton btnColetar;
    private JButton btnFinalizarColeta; // Botão para finalizar coleta na sala
    private JButton btnReabrirColeta; // Botão para reabrir coleta da sala (apenas admin/supervisor)
    private JButton btnRemoverItem; // Botão para remover item (não disponível para coletor)
    private JButton btnVerTodasColetas; // Botão para ver todas as coletas do inventário
    private JLabel lblResumoSala;

    // Painel de informações do item pesquisado
    private JPanel panelInfoItem;
    private JLabel lblNumeroItem;
    private JLabel lblDescricaoItem;
    private JLabel lblLocalizacaoOriginal;
    private JLabel lblEstadoOriginal;
    private JLabel lblStatusItem;

    // Painéis principais do layout
    private JPanel panelFormulario;
    private JPanel panelBotoes;

    // Tabela de histórico de coleta - sempre visível
    private JTable tabelaHistorico;
    private DefaultTableModel modeloTabelaHistorico;
    private JLabel lblContagemColetas; // Label para exibir contagem de coletas na sala
    private JScrollPane scrollHistorico;

    private Patrimonio patrimonioSelecionado;
    private long ultimaDigitacao = 0;
    private StringBuilder bufferCodigoBarras = new StringBuilder();

    // Variáveis para métricas de tempo (Analytics)
    private long inicioColetaAtual = 0;      // Timestamp do início da coleta atual
    private long inicioScanAtual = 0;        // Timestamp do início do scan/busca
    private long fimScanAtual = 0;           // Timestamp do fim do scan/busca
    private String metodoColetaAtual = null; // MANUAL, SCANNER, BUSCA_DESCRICAO

    // Lista de componentes que devem ser desabilitados até a seleção da sala
    private java.util.List<Component> componentesParaDesabilitar = new ArrayList<>();

    // Modo de coleta automática com leitor de código de barras
    private JCheckBox chkColetaAutomatica;
    private boolean modoColetaAutomatica = false;

    // Timers para controle de animações visuais (evitar conflitos)
    private Timer timerAnimacaoAtual;
    private Timer timerTextoAtual;

    /**
     * @param patrimonioDAO             DAO de patrimônios
     * @param coletaDAO                 DAO de coletas
     * @param inventarioDAO             DAO de inventários
     * @param salaInventarioDAO         DAO de salas do inventário
     * @param participanteInventarioDAO DAO de participantes do inventário
     * @param coletaOfflineService      serviço de coleta com suporte offline
     * @param offlineManager            gerenciador de estado offline
     * @param usuarioLogado             usuário autenticado (para permissões)
     * @param salaSupplier              fornece a sala atualmente selecionada
     * @param workerRegistrar           callback para registrar o SwingWorker no frame
     *                                  (permite cancelamento ao trocar de sala/fechar)
     */
    public ColetaNormalPanel(
            PatrimonioDAO patrimonioDAO,
            ColetaDAO coletaDAO,
            InventarioDAO inventarioDAO,
            SalaInventarioDAO salaInventarioDAO,
            ParticipanteInventarioDAO participanteInventarioDAO,
            ColetaOfflineService coletaOfflineService,
            OfflineManager offlineManager,
            Usuario usuarioLogado,
            java.util.function.Supplier<Sala> salaSupplier,
            java.util.function.Consumer<javax.swing.SwingWorker<?, ?>> workerRegistrar) {
        this.patrimonioDAO = patrimonioDAO;
        this.coletaDAO = coletaDAO;
        this.inventarioDAO = inventarioDAO;
        this.salaInventarioDAO = salaInventarioDAO;
        this.participanteInventarioDAO = participanteInventarioDAO;
        this.coletaOfflineService = coletaOfflineService;
        this.offlineManager = offlineManager;
        this.usuarioLogado = usuarioLogado;
        this.salaSupplier = salaSupplier;
        this.workerRegistrar = workerRegistrar;
        construirUI();
        inicializar();
    }

    private void construirUI() {
        criarComponentes();
        montarLayout();
        registrarListeners();
    }

    private void criarComponentes() {
        // Definir cores do tema moderno
        Color corPrimaria = new Color(52, 73, 94); // Azul escuro elegante
        Color corAcento = new Color(46, 204, 113); // Verde moderno
        Color corTexto = new Color(44, 62, 80); // Texto escuro

        // Campo de busca de patrimônio com design moderno
        campoBusca = new JTextField(20);
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoBusca.setPreferredSize(new Dimension(250, 35));
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        campoBusca.setBackground(Color.WHITE);
        campoBusca.setForeground(corTexto);
        campoBusca.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarPatrimonio();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                detectarLeituraCodigoBarras();
            }
        });

        // Botão de busca com design moderno
        btnBuscar = StyledButtonFactory.create("🔍 Buscar", new Color(52, 152, 219));

        // Painel de informações do item pesquisado com design moderno
        panelInfoItem = new JPanel(new GridBagLayout());
        panelInfoItem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "Informações do Item Pesquisado",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(5, 15, 10, 15))); // Reduzido padding superior
        panelInfoItem.setBackground(Color.WHITE);
        panelInfoItem.setPreferredSize(new Dimension(400, 160)); // Tamanho compacto
        panelInfoItem.setMinimumSize(new Dimension(350, 140));
        panelInfoItem.setMaximumSize(new Dimension(500, 180));

        // Labels com design moderno
        lblNumeroItem = new JLabel("-");
        lblNumeroItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNumeroItem.setForeground(corPrimaria);

        lblDescricaoItem = new JLabel("-");
        lblDescricaoItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDescricaoItem.setForeground(corTexto);

        lblLocalizacaoOriginal = new JLabel("-");
        lblLocalizacaoOriginal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLocalizacaoOriginal.setForeground(corTexto);

        lblEstadoOriginal = new JLabel("-");
        lblEstadoOriginal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstadoOriginal.setForeground(corTexto);

        lblStatusItem = new JLabel("-");
        lblStatusItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatusItem.setForeground(corAcento);

        // Campo de observação com design moderno (reduzido para 1 linha)
        campoObservacao = new JTextArea(1, 30);
        campoObservacao.setLineWrap(true);
        campoObservacao.setWrapStyleWord(true);
        campoObservacao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoObservacao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        campoObservacao.setBackground(Color.WHITE);
        campoObservacao.setForeground(corTexto);

        // Combo de estado com design moderno
        String[] estados = { "BOM", "OCIOSO", "ANTIECONÔMICO", "RECUPERÁVEL", "IRRECUPERÁVEL" };
        comboEstado = new JComboBox<>(estados);
        comboEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboEstado.setPreferredSize(new Dimension(200, 35));
        comboEstado.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        comboEstado.setBackground(Color.WHITE);
        comboEstado.setForeground(corTexto);
        comboEstado.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                if (isSelected) {
                    setBackground(corAcento);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(corTexto);
                }
                return this;
            }
        });

        // Botão coletar com design moderno - DESTAQUE MAIOR
        btnColetar = StyledButtonFactory.create("✅ Registrar", new Color(46, 204, 113));
        btnColetar.setPreferredSize(new Dimension(200, 45)); // Aumentado para 200x45
        btnColetar.setMinimumSize(new Dimension(200, 45));
        btnColetar.setEnabled(false);
        btnColetar.setToolTipText("Clique para registrar o item encontrado ou pressione F4");

        // Adicionar animação de clique ao botão Registrar
        adicionarAnimacaoClique(btnColetar, new Color(46, 204, 113));

        // Adicionar atalho F4 para o botão registrar
        btnColetar.setMnemonic(KeyEvent.VK_F4);
        btnColetar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "registrar");
        btnColetar.getActionMap().put("registrar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnColetar.isEnabled()) {
                    animarBotaoRegistrar();
                    registrarItemEncontrado();
                }
            }
        });

        // Botão remover item com design moderno
        btnRemoverItem = StyledButtonFactory.create("🗑️ Remover", new Color(231, 76, 60));
        btnRemoverItem.setEnabled(false);
        btnRemoverItem.setToolTipText("Clique para remover o item selecionado (apenas Admin/Supervisor)");

        // Controle de visibilidade baseado no perfil do usuário
        // Apenas administradores e supervisores podem remover itens
        // Perfil CONSULTA não pode realizar nenhuma ação de modificação
        boolean podeRemover = usuarioLogado != null &&
                ("ADMIN".equals(usuarioLogado.getPerfil().name()) ||
                        "SUPERVISOR".equals(usuarioLogado.getPerfil().name()));
        System.out.println("DEBUG: btnRemoverItem.setVisible(" + podeRemover + ") - Usuario: " +
                (usuarioLogado != null ? usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getPerfil() + ")"
                        : "null"));
        btnRemoverItem.setVisible(podeRemover);

        // Verificar se é perfil CONSULTA para desabilitar botões de ação
        boolean isPerfilConsulta = usuarioLogado != null &&
                "CONSULTA".equals(usuarioLogado.getPerfil().name());
        if (isPerfilConsulta) {
            btnColetar.setEnabled(false);
            btnFinalizarColeta.setEnabled(false);
            btnRemoverItem.setEnabled(false);
            System.out.println("DEBUG: Perfil CONSULTA detectado - botões de ação desabilitados");
        }

        // Botão para finalizar coleta na sala com design moderno
        btnFinalizarColeta = StyledButtonFactory.create("🏁 Finalizar", new Color(40, 167, 69));
        btnFinalizarColeta.setEnabled(false);
        btnFinalizarColeta.setToolTipText("Marcar a coleta desta sala como finalizada");

        // Botão para reabrir coleta da sala (apenas admin/supervisor)
        btnReabrirColeta = StyledButtonFactory.create("⚠️ Reabrir", new Color(255, 193, 7));
        btnReabrirColeta.setPreferredSize(new Dimension(140, 35));
        btnReabrirColeta.setEnabled(false);
        btnReabrirColeta.setToolTipText("Reabrir uma sala finalizada para permitir novas coletas (Admin/Supervisor)");
        // Controlar visibilidade baseado no perfil do usuário
        // Apenas ADMIN e SUPERVISOR podem reabrir salas
        boolean podeReabrir = usuarioLogado != null &&
                ("ADMIN".equals(usuarioLogado.getPerfil().name()) ||
                        "SUPERVISOR".equals(usuarioLogado.getPerfil().name()));
        btnReabrirColeta.setVisible(podeReabrir);
        System.out.println("DEBUG: btnReabrirColeta.setVisible(" + podeReabrir + ") - Usuario: " +
                (usuarioLogado != null ? usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getPerfil() + ")"
                        : "null"));

        // Botão para ver todas as coletas do inventário
        btnVerTodasColetas = StyledButtonFactory.create("📋 Ver Todas", new Color(52, 152, 219));
        btnVerTodasColetas.setPreferredSize(new Dimension(140, 35));
        btnVerTodasColetas.setEnabled(true);
        btnVerTodasColetas.setToolTipText("Ver todas as coletas do inventário atual (todas as salas)");

        // Labels de resumo
        lblResumoSala = new JLabel("Selecione uma sala para iniciar a coleta");

        // Tabela de histórico de coleta - CONFIGURAÇÃO MELHORADA
        // NOTA: Coluna ID (índice 4) é oculta - usada internamente para exclusão
        String[] colunasHistorico = { "Data/Hora", "Patrimônio", "Descrição", "Estado", "ID" };
        modeloTabelaHistorico = new DefaultTableModel(colunasHistorico, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaHistorico = new JTable(modeloTabelaHistorico);
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurações detalhadas da tabela
        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(140); // Data/Hora
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(120); // Patrimônio
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(400); // Descrição (aumentada)
        tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(120); // Estado

        // Ocultar coluna ID (índice 4) - usada internamente para exclusão
        tabelaHistorico.getColumnModel().getColumn(4).setMinWidth(0);
        tabelaHistorico.getColumnModel().getColumn(4).setMaxWidth(0);
        tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth(0);

        tabelaHistorico.setRowHeight(30);
        tabelaHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaHistorico.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaHistorico.getTableHeader().setForeground(Color.WHITE);
        tabelaHistorico.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabelaHistorico.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tabelaHistorico.setGridColor(new Color(189, 195, 199));
        tabelaHistorico.setShowGrid(true);
        tabelaHistorico.setIntercellSpacing(new Dimension(1, 1));
        tabelaHistorico.setSelectionBackground(new Color(52, 152, 219));
        tabelaHistorico.setSelectionForeground(Color.WHITE);
        tabelaHistorico.setBackground(Color.WHITE);
        tabelaHistorico.setForeground(new Color(44, 62, 80));

        // Cores alternadas nas linhas com texto PRETO visível
        tabelaHistorico.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    // Linha selecionada: fundo azul, texto branco
                    c.setBackground(new Color(52, 152, 219));
                    c.setForeground(Color.WHITE);
                } else {
                    // Linha não selecionada: fundo alternado, texto PRETO
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                    c.setForeground(new Color(44, 62, 80)); // Texto escuro (quase preto)
                }
                return c;
            }
        });

        // Menu de contexto para excluir coleta (apenas admin)
        JPopupMenu popupMenuHistorico = new JPopupMenu();
        JMenuItem menuExcluirColeta = new JMenuItem("🗑️ Excluir Coleta");
        menuExcluirColeta.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        menuExcluirColeta.addActionListener(e -> excluirColetaSelecionada());
        popupMenuHistorico.add(menuExcluirColeta);

        // Adicionar listener de mouse para mostrar popup menu
        tabelaHistorico.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mostrarPopupSeNecessario(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                mostrarPopupSeNecessario(e);
            }

            private void mostrarPopupSeNecessario(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // Selecionar a linha clicada
                    int row = tabelaHistorico.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tabelaHistorico.getRowCount()) {
                        tabelaHistorico.setRowSelectionInterval(row, row);
                    }
                    // Mostrar menu apenas para admin
                    if (usuarioLogado != null && "ADMIN".equals(usuarioLogado.getPerfil().name())) {
                        popupMenuHistorico.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Criar JScrollPane para a tabela
        scrollHistorico = new JScrollPane(tabelaHistorico);
        // Altura para exibir 3 linhas: altura da linha (25) * 3 + cabeçalho (25) +
        // bordas (10)
        scrollHistorico.setPreferredSize(new Dimension(800, 180)); // Aumentado de 110 para 180
        scrollHistorico.setMinimumSize(new Dimension(600, 110));
        scrollHistorico.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollHistorico.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollHistorico.setBorder(BorderFactory.createLoweredBevelBorder());

        // Label de contagem de coletas na sala
        lblContagemColetas = new JLabel("📊 Total de itens coletados: 0");
        lblContagemColetas.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        lblContagemColetas.setForeground(new Color(52, 152, 219));
        lblContagemColetas.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Adicionar componentes à lista de desabilitação (EXCETO a tabela de histórico)
        componentesParaDesabilitar.add(campoBusca);
        componentesParaDesabilitar.add(btnBuscar);
        componentesParaDesabilitar.add(campoObservacao);
        componentesParaDesabilitar.add(comboEstado);
        componentesParaDesabilitar.add(btnColetar);
        componentesParaDesabilitar.add(btnRemoverItem);

        componentesParaDesabilitar.add(btnFinalizarColeta);

        // Adicionar botão reabrir à lista (será controlado separadamente)
        if (btnReabrirColeta.isVisible()) {
            componentesParaDesabilitar.add(btnReabrirColeta);
        }

        // Configurar estilo dos botões para manter aparência quando desabilitados
        configurarBotaoComEstiloDesabilitado(btnColetar, new Color(46, 204, 113), new Color(120, 220, 150));
        configurarBotaoComEstiloDesabilitado(btnRemoverItem, new Color(231, 76, 60), new Color(240, 140, 130));

        configurarBotaoComEstiloDesabilitado(btnFinalizarColeta, new Color(40, 167, 69), new Color(120, 200, 140));

        // Configurar estilo do botão reabrir (amarelo/laranja para ação de aviso)
        if (btnReabrirColeta.isVisible()) {
            configurarBotaoComEstiloDesabilitado(btnReabrirColeta, new Color(255, 193, 7), new Color(255, 220, 100));
        }

        // Desabilitar componentes inicialmente
        desabilitarComponentes();
    }

    private void montarLayout() {
        // Definir cores do tema moderno
        Color corFundo = new Color(248, 249, 250); // Fundo suave
        Color corTexto = new Color(44, 62, 80); // Texto escuro

        setLayout(new BorderLayout());
        setBackground(corFundo);

        // Painel ao NORTE com o resumo da sala (lblResumoSala)
        JPanel panelResumo = new JPanel(new BorderLayout());
        panelResumo.setBackground(Color.WHITE);
        panelResumo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        lblResumoSala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResumoSala.setForeground(new Color(100, 100, 100));
        panelResumo.add(lblResumoSala, BorderLayout.CENTER);

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

        // Montagem final do painel
        add(panelResumo, BorderLayout.NORTH);
        add(panelCentralPrincipal, BorderLayout.CENTER);
    }

    private void registrarListeners() {
        btnBuscar.addActionListener(e -> buscarPatrimonio());
        campoBusca.addActionListener(e -> buscarPatrimonio());
        btnColetar.addActionListener(e -> {
            animarBotaoRegistrar();
            registrarItemEncontrado();
        });
        btnRemoverItem.addActionListener(e -> removerItemSelecionado());

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

                // NOTA: btnFinalizarColeta e btnReabrirColeta são controlados em carregarParaSala()
                // baseado no status da sala (aberta/finalizada) e modo de operação (online/offline)
            }
        });
    }

    // ===================== API pública (usada pelo frame) =====================

    /**
     * Inicialização tardia do painel. Garante o estado inicial (componentes
     * desabilitados) e adiciona a dica dos leitores de código de barras.
     */
    public void inicializar() {
        adicionarDicaLeitores();
    }

    /**
     * Carrega os dados da sala informada na aba "Coleta Normal".
     *
     * <p>Contém a lógica do antigo {@code carregarDadosSala()} do frame, exceto:
     * o {@code cancelarWorkersAtivos()} inicial, o
     * {@code itensSemPatrimonioPanel.setLocalizacao(...)} e os blocos que carregam
     * as outras abas — que permanecem no frame coordenador.</p>
     *
     * @param sala     sala selecionada (pode ser {@code null})
     * @param abaAtiva {@code true} se a aba "Coleta Normal" está selecionada
     */
    public void carregarParaSala(Sala sala, boolean abaAtiva) {
        System.out.println("DEBUG: carregarDadosSala() chamado");

        Sala salaSelecionada = sala;
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

            // Resetar contagem de coletas e limpar tabela de histórico
            atualizarContagemColetas(0);
            modeloTabelaHistorico.setRowCount(0);

            return;
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
                setColetaNormalAtiva(abaAtiva);

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

    /**
     * Replica os ramos do antigo {@code habilitarComponentesPorAba} para os
     * componentes da aba "Coleta Normal": {@code ativa=true} equivale ao
     * {@code case 0}; {@code ativa=false} equivale aos {@code case 1/2}.
     */
    public void setColetaNormalAtiva(boolean ativa) {
        if (ativa) {
            campoBusca.setEnabled(true);
            btnBuscar.setEnabled(true);
            campoObservacao.setEnabled(true);
            comboEstado.setEnabled(true);
            btnColetar.setText("Registrar");
            btnColetar.setEnabled(patrimonioSelecionado != null);
            btnRemoverItem.setEnabled(patrimonioSelecionado != null && btnRemoverItem.isVisible());
        } else {
            campoBusca.setEnabled(false);
            btnBuscar.setEnabled(false);
            btnColetar.setEnabled(false);
            btnRemoverItem.setEnabled(false);
        }
    }

    // ===================== Lógica interna (movida do frame) =====================

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
            // Ele só é habilitado quando a sala está FINALIZADA (controlado em carregarParaSala)
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
     * Mostra todas as coletas do inventário atual em um diálogo separado.
     * Delegado para {@link com.inventario.sihcp.view.coleta.TodasColetasDialog}.
     */
    private void mostrarTodasColetas() {
        new com.inventario.sihcp.view.coleta.TodasColetasDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                inventarioDAO, coletaDAO, LIMITE_TODAS_COLETAS, workerRegistrar)
                .setVisible(true);
    }

    /**
     * Carrega histórico de coletas de forma ASSÍNCRONA.
     * - Modo offline: carrega do SQLite local
     * - Modo online: carrega do PostgreSQL em uma única consulta
     */
    private void carregarHistoricoColeta(String identificacaoSala) {
        Sala salaAtual = salaSupplier.get();
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
        if (workerRegistrar != null) {
            workerRegistrar.accept(worker);
        }
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

        if (workerRegistrar != null) {
            workerRegistrar.accept(worker);
        }
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
                        btnColetar.setEnabled(salaSupplier.get() != null && !result.jaColetado);

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
                        JOptionPane.showMessageDialog(ColetaNormalPanel.this,
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

        if (workerRegistrar != null) {
            workerRegistrar.accept(worker);
        }
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

        Sala salaAtual = salaSupplier.get();
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
            inicioColetaAtual = 0;
            inicioScanAtual = 0;
            fimScanAtual = 0;
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
        Sala salaAtual = salaSupplier.get();
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

            Sala salaAtual = salaSupplier.get();
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
        Sala salaAtual = salaSupplier.get();
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

        Sala salaAtual = salaSupplier.get();
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

        Sala salaAtual = salaSupplier.get();
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
        Sala salaAtual = salaSupplier.get();
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
        Sala salaAtual = salaSupplier.get();
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
        Sala salaAtual = salaSupplier.get();
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
}
