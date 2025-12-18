package com.inventario.view;

// === SWING: Usar DAOs diretamente (sem Spring) ===
// import com.inventario.dao.SalaDAO; // REMOVIDO - não é usado e causava problemas
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.SalaInventarioDAO;
import com.inventario.dao.ParticipanteInventarioDAO;
import com.inventario.model.Coleta;
import com.inventario.model.Inventario;
import com.inventario.model.Patrimonio;
import com.inventario.model.Sala;
import com.inventario.model.Usuario;
import com.inventario.offline.ColetaOfflineService;
import com.inventario.offline.OfflineManager;
import com.inventario.util.SoundNotification;
import com.inventario.view.ui.ModernComboBox;

import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
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
    private JTextField campoBusca;
    private JTextArea campoObservacao;
    private JComboBox<String> comboEstado;
    private JButton btnColetar;
    private JButton btnBuscar;

    private JButton btnFinalizarColeta; // Botão para finalizar coleta na sala
    private JButton btnReabrirColeta; // Botão para reabrir coleta da sala (apenas admin/supervisor)
    private JButton btnRemoverItem; // Botão para remover item (não disponível para coletor)
    private JLabel lblResumoSala;
    private JLabel lblInventarioAtual;

    // Painel de informações do item pesquisado
    private JPanel panelInfoItem;
    private JLabel lblNumeroItem;
    private JLabel lblDescricaoItem;
    private JLabel lblLocalizacaoOriginal;
    private JLabel lblEstadoOriginal;
    private JLabel lblStatusItem;

    // Componentes para itens sem etiqueta (removidos - agora há aba dedicada)
    private JTextArea txtDescricaoSemEtiqueta;
    private JComboBox<String> comboCategoriaSemEtiqueta;
    private JPanel panelSemEtiqueta;

    // Componentes para busca por descrição
    private JTextField campoBuscaDescricao;
    private JButton btnBuscarDescricao;
    private JComboBox<String> comboCategoriasBusca;
    private JTable tabelaResultadosDescricao;
    private DefaultTableModel modeloTabelaResultados;
    private JScrollPane scrollResultados;
    private JPanel panelBuscaDescricao;
    private Patrimonio patrimonioSelecionadoDescricao;

    // Componentes para indicador de loading
    private JLabel lblLoadingDescricao;
    private JProgressBar progressBarDescricao;

    // Componentes para agrupamento de itens sem etiqueta
    private JButton btnVerItensAgrupados;
    private JTable tabelaItensAgrupados;
    private DefaultTableModel modeloTabelaItensAgrupados;
    private JScrollPane scrollItensAgrupados;
    private JPanel panelItensAgrupados;
    private JDialog dialogItensAgrupados;

    // Painéis principais do layout
    private JPanel panelFormulario;
    private JPanel panelBotoes;

    // Tabela de histórico de coleta - sempre visível
    private JTable tabelaHistorico;
    private DefaultTableModel modeloTabelaHistorico;
    private JLabel lblContagemColetas; // Label para exibir contagem de coletas na sala
    private JScrollPane scrollHistorico;

    // Componentes para estrutura de abas
    private JTabbedPane tabbedPane;
    private JPanel abaColetaNormal;
    private JPanel abaItensSemPatrimonio;
    private JPanel abaItensPendentes; // Nova aba para itens não coletados

    // Componentes da aba "Itens Pendentes"
    private JTable tabelaPendentes;
    private DefaultTableModel modeloTabelaPendentes;
    private JLabel lblTotalPendentes;
    private JLabel lblProgressoColeta;
    private JTextField campoBuscaPendentes;
    private JButton btnExportarPendentes;
    private JProgressBar progressBarColeta;

    // Campo de localização na aba de itens sem patrimônio
    private JTextField campoLocalizacaoSemPatrimonio;

    // Componentes removidos: busca simplificada para melhor usabilidade

    // Campo de descrição para coleta de itens sem patrimônio
    private JTextField campoDescricaoSemPatrimonio;

    // Novos componentes para a interface moderna de itens sem patrimônio
    private JTextArea areaObservacoesSemPatrimonio;
    private JButton btnRegistrarSemPatrimonio;
    private JButton btnRemoverSemPatrimonio;
    private JButton btnExportarSemPatrimonio;
    private JTable tabelaSemPatrimonio;
    private DefaultTableModel modeloTabelaSemPatrimonio;
    private JLabel lblTotalItensSemPatrimonio;

    // Componentes para pesquisa de descrições únicas (não utilizados na nova
    // interface)
    // Mantidos para compatibilidade, mas não são mais necessários
    // private JButton btnPesquisarDescricoes;
    // private JList<String> listaDescricoesSugestoes;
    // private DefaultListModel<String> modeloListaDescricoes;
    // private JPanel painelSugestoes;
    // private JLabel lblSugestoes;

    private PatrimonioDAO patrimonioDAO;
    private ColetaDAO coletaDAO;
    private InventarioDAO inventarioDAO;
    private SalaInventarioDAO salaInventarioDAO;
    private ParticipanteInventarioDAO participanteInventarioDAO;

    // === OFFLINE: Serviços de modo offline ===
    private ColetaOfflineService coletaOfflineService;
    private OfflineManager offlineManager;

    private Patrimonio patrimonioSelecionado;
    private long ultimaDigitacao = 0;
    private StringBuilder bufferCodigoBarras = new StringBuilder();
    private List<Sala> todasSalas = new ArrayList<>();
    private Usuario usuarioLogado; // Usuário logado para verificar permissões
    
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

            System.out.println("DEBUG: Iniciando adicionarDicaLeitores()...");
            try {
                adicionarDicaLeitores();
                System.out.println("DEBUG: adicionarDicaLeitores() concluído com sucesso");
            } catch (Exception e) {
                LOG.error("ERRO FATAL em adicionarDicaLeitores(): {}", e.getMessage(), e);
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
        // Definir cores do tema moderno
        Color corPrimaria = new Color(52, 73, 94); // Azul escuro elegante
        // Color corSecundaria = new Color(236, 240, 241); // Cinza claro
        Color corAcento = new Color(46, 204, 113); // Verde moderno
        // Color corPerigo = new Color(231, 76, 60); // Vermelho moderno
        // Color corAviso = new Color(241, 196, 15); // Amarelo moderno
        Color corTexto = new Color(44, 62, 80); // Texto escuro

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
        btnBuscar = createStyledButton("🔍 Buscar", new Color(52, 152, 219));

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

        // Componentes para itens sem etiqueta com design moderno (checkbox removido -
        // há aba dedicada)

        txtDescricaoSemEtiqueta = new JTextArea(3, 25);
        txtDescricaoSemEtiqueta.setLineWrap(true);
        txtDescricaoSemEtiqueta.setWrapStyleWord(true);
        txtDescricaoSemEtiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescricaoSemEtiqueta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        txtDescricaoSemEtiqueta.setBackground(new Color(248, 249, 250));
        txtDescricaoSemEtiqueta.setForeground(new Color(108, 117, 125));
        txtDescricaoSemEtiqueta.setEnabled(false);

        String[] categorias = { "MÓVEIS", "EQUIPAMENTOS", "ELETRÔNICOS", "UTENSÍLIOS", "OUTROS" };
        comboCategoriaSemEtiqueta = new JComboBox<>(categorias);
        comboCategoriaSemEtiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboCategoriaSemEtiqueta.setPreferredSize(new Dimension(200, 35));
        comboCategoriaSemEtiqueta.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        comboCategoriaSemEtiqueta.setBackground(new Color(248, 249, 250));
        comboCategoriaSemEtiqueta.setForeground(new Color(108, 117, 125));
        comboCategoriaSemEtiqueta.setEnabled(false);

        // Componentes para busca por descrição com design moderno
        campoBuscaDescricao = new JTextField(20);
        campoBuscaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoBuscaDescricao.setPreferredSize(new Dimension(200, 32));
        campoBuscaDescricao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        campoBuscaDescricao.setBackground(new Color(248, 249, 250));
        campoBuscaDescricao.setForeground(new Color(108, 117, 125));
        campoBuscaDescricao.setEnabled(false);

        // Combobox de categorias para filtrar busca
        String[] categoriasBusca = { "TODAS", "MÓVEIS", "EQUIPAMENTOS", "ELETRÔNICOS", "UTENSÍLIOS", "OUTROS" };
        comboCategoriasBusca = new JComboBox<>(categoriasBusca);
        comboCategoriasBusca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboCategoriasBusca.setPreferredSize(new Dimension(140, 32));
        comboCategoriasBusca.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        comboCategoriasBusca.setBackground(new Color(248, 249, 250));
        comboCategoriasBusca.setForeground(new Color(108, 117, 125));
        comboCategoriasBusca.setEnabled(false);

        btnBuscarDescricao = createStyledButton("🔍 Buscar", new Color(108, 117, 125));
        btnBuscarDescricao.setEnabled(false);

        // Tabela de resultados da busca por descrição
        String[] colunasResultados = { "Número", "Descrição", "Marca/Modelo", "Localização" };
        modeloTabelaResultados = new DefaultTableModel(colunasResultados, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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
        btnVerItensAgrupados = createStyledButton("📊 Ver Itens Sem Etiqueta Agrupados", new Color(52, 152, 219));
        btnVerItensAgrupados.setEnabled(false);

        // Painel para itens agrupados (inicialmente oculto)
        panelItensAgrupados = new JPanel(new BorderLayout());
        panelItensAgrupados.setBorder(BorderFactory.createTitledBorder("Itens Sem Etiqueta Agrupados"));
        panelItensAgrupados.setVisible(false);

        tabelaResultadosDescricao = new JTable(modeloTabelaResultados);
        tabelaResultadosDescricao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaResultadosDescricao.setEnabled(false);

        // Configurações da tabela de resultados
        tabelaResultadosDescricao.getColumnModel().getColumn(0).setPreferredWidth(100); // Número
        tabelaResultadosDescricao.getColumnModel().getColumn(1).setPreferredWidth(250); // Descrição
        tabelaResultadosDescricao.getColumnModel().getColumn(2).setPreferredWidth(150); // Marca/Modelo
        tabelaResultadosDescricao.getColumnModel().getColumn(3).setPreferredWidth(120); // Localização

        tabelaResultadosDescricao.setRowHeight(28);
        tabelaResultadosDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaResultadosDescricao.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaResultadosDescricao.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaResultadosDescricao.getTableHeader().setForeground(Color.WHITE);
        tabelaResultadosDescricao.setGridColor(new Color(189, 195, 199));
        tabelaResultadosDescricao.setSelectionBackground(new Color(52, 152, 219));
        tabelaResultadosDescricao.setSelectionForeground(Color.WHITE);
        tabelaResultadosDescricao.setBackground(Color.WHITE);
        tabelaResultadosDescricao.setForeground(new Color(44, 62, 80));

        scrollResultados = new JScrollPane(tabelaResultadosDescricao);
        scrollResultados.setPreferredSize(new Dimension(400, 100)); // Diminuído de 150 para 100
        scrollResultados.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollResultados.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Painel de busca por descrição (inicialmente oculto) com design moderno
        panelBuscaDescricao = new JPanel(new BorderLayout());
        panelBuscaDescricao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 10, 5, 10),
                        "Buscar Item Similar por Descrição",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(52, 152, 219))));
        panelBuscaDescricao.setBackground(Color.WHITE);
        panelBuscaDescricao.setVisible(false);

        // Painel superior com campo de busca organizado
        JPanel panelCampoBusca = new JPanel(new BorderLayout());

        // Painel esquerdo com campo de busca
        JPanel panelCampoEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCampoEsquerdo.add(new JLabel("Categoria:"));
        panelCampoEsquerdo.add(comboCategoriasBusca);
        panelCampoEsquerdo.add(new JLabel("Descrição:"));
        panelCampoEsquerdo.add(campoBuscaDescricao);
        panelCampoEsquerdo.add(btnBuscarDescricao);

        // Painel direito com botão de itens agrupados
        JPanel panelCampoDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelCampoDireito.add(btnVerItensAgrupados);

        panelCampoBusca.add(panelCampoEsquerdo, BorderLayout.WEST);
        panelCampoBusca.add(panelCampoDireito, BorderLayout.EAST);

        panelBuscaDescricao.add(panelCampoBusca, BorderLayout.NORTH);
        panelBuscaDescricao.add(scrollResultados, BorderLayout.CENTER);

        JLabel lblInstrucao = new JLabel(
                "<html><i>Busque por palavras-chave da descrição para encontrar itens similares no banco de dados</i></html>");
        lblInstrucao.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        panelBuscaDescricao.add(lblInstrucao, BorderLayout.SOUTH);

        // Botão coletar com design moderno - DESTAQUE MAIOR
        btnColetar = createStyledButton("✅ Registrar", new Color(46, 204, 113));
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
        btnRemoverItem = createStyledButton("🗑️ Remover", new Color(231, 76, 60));
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
        btnFinalizarColeta = createStyledButton("🏁 Finalizar", new Color(40, 167, 69));
        btnFinalizarColeta.setEnabled(false);
        btnFinalizarColeta.setToolTipText("Marcar a coleta desta sala como finalizada");

        // Botão para reabrir coleta da sala (apenas admin/supervisor)
        btnReabrirColeta = createStyledButton("⚠️ Reabrir", new Color(255, 193, 7));
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

        // Labels de resumo
        lblResumoSala = new JLabel("Selecione uma sala para iniciar a coleta");
        lblInventarioAtual = new JLabel("Inventário: Carregando...");
        lblInventarioAtual.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblInventarioAtual.setForeground(new Color(0, 100, 0));

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

        // Cores alternadas nas linhas
        tabelaHistorico.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
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

        // Componentes da nova interface de itens sem patrimônio serão controlados por
        // habilitarComponentesPorAba
        // Não adicionamos à lista geral pois são específicos de cada aba

        // Configurar estilo dos botões para manter aparência quando desabilitados
        configurarBotaoComEstiloDesabilitado(btnColetar, new Color(46, 204, 113), new Color(120, 220, 150));
        configurarBotaoComEstiloDesabilitado(btnRemoverItem, new Color(231, 76, 60), new Color(240, 140, 130));

        configurarBotaoComEstiloDesabilitado(btnFinalizarColeta, new Color(40, 167, 69), new Color(120, 200, 140));

        // Configurar estilo do botão reabrir (amarelo/laranja para ação de aviso)
        if (btnReabrirColeta.isVisible()) {
            configurarBotaoComEstiloDesabilitado(btnReabrirColeta, new Color(255, 193, 7), new Color(255, 220, 100));
        }

        // Configurar estilo dos novos botões da aba de itens sem patrimônio (serão
        // criados depois)
        SwingUtilities.invokeLater(() -> {
            if (btnRegistrarSemPatrimonio != null) {
                configurarBotaoComEstiloDesabilitado(btnRegistrarSemPatrimonio, new Color(46, 204, 113),
                        new Color(120, 220, 150));
            }
            if (btnBuscarDescricao != null) {
                configurarBotaoComEstiloDesabilitado(btnBuscarDescricao, new Color(108, 117, 125),
                        new Color(160, 160, 160));
            }
            if (btnVerItensAgrupados != null) {
                configurarBotaoComEstiloDesabilitado(btnVerItensAgrupados, new Color(52, 152, 219),
                        new Color(120, 180, 230));
            }
            if (btnRemoverSemPatrimonio != null) {
                configurarBotaoComEstiloDesabilitado(btnRemoverSemPatrimonio, new Color(231, 76, 60),
                        new Color(240, 140, 130));
            }
            if (btnExportarSemPatrimonio != null) {
                configurarBotaoComEstiloDesabilitado(btnExportarSemPatrimonio, new Color(52, 152, 219),
                        new Color(120, 180, 230));
            }
        });

        // Desabilitar componentes inicialmente
        desabilitarComponentes();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Definir cores do tema moderno
        Color corFundo = new Color(248, 249, 250); // Fundo suave
        Color corPrimaria = new Color(52, 73, 94); // Azul escuro elegante
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

        // Painel para itens sem etiqueta (inicialmente oculto) com design moderno
        panelSemEtiqueta = new JPanel(new GridBagLayout());
        panelSemEtiqueta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "Informações do Item Sem Etiqueta",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        corTexto),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));
        panelSemEtiqueta.setBackground(Color.WHITE);
        panelSemEtiqueta.setVisible(false);

        GridBagConstraints gbcSem = new GridBagConstraints();
        gbcSem.insets = new Insets(3, 3, 3, 3);
        gbcSem.anchor = GridBagConstraints.WEST;

        gbcSem.gridx = 0;
        gbcSem.gridy = 0;
        panelSemEtiqueta.add(new JLabel("Categoria:"), gbcSem);
        gbcSem.gridx = 1;
        gbcSem.fill = GridBagConstraints.HORIZONTAL;
        gbcSem.weightx = 1.0;
        panelSemEtiqueta.add(comboCategoriaSemEtiqueta, gbcSem);

        gbcSem.gridx = 0;
        gbcSem.gridy = 1;
        gbcSem.fill = GridBagConstraints.NONE;
        gbcSem.weightx = 0;
        panelSemEtiqueta.add(new JLabel("Descrição:"), gbcSem);
        gbcSem.gridx = 1;
        gbcSem.fill = GridBagConstraints.BOTH;
        gbcSem.weightx = 1.0;
        gbcSem.weighty = 0.3;
        panelSemEtiqueta.add(new JScrollPane(txtDescricaoSemEtiqueta), gbcSem);

        // Adicionar painel de busca por descrição
        gbcSem.gridx = 0;
        gbcSem.gridy = 2;
        gbcSem.gridwidth = 2;
        gbcSem.fill = GridBagConstraints.BOTH;
        gbcSem.weightx = 1.0;
        gbcSem.weighty = 0.7;
        panelSemEtiqueta.add(panelBuscaDescricao, gbcSem);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        panelFormulario.add(panelSemEtiqueta, gbc);

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

        // Criar aba para Itens Sem Patrimônio
        abaItensSemPatrimonio = criarAbaItensSemPatrimonio(corFundo, corPrimaria, corTexto);

        // Criar aba para Itens Pendentes (não coletados)
        abaItensPendentes = criarAbaItensPendentes(corFundo, corPrimaria, corTexto);

        // Adicionar abas ao TabbedPane
        tabbedPane.addTab("📋 Coleta Normal", abaColetaNormal);
        tabbedPane.addTab("⏳ Itens Pendentes", abaItensPendentes);
        tabbedPane.addTab("🏷️ Itens Sem Patrimônio", abaItensSemPatrimonio);

        // Adicionar listener para controlar habilitação de componentes por aba
        tabbedPane.addChangeListener(e -> {
            int abaSelecionada = tabbedPane.getSelectedIndex();
            habilitarComponentesPorAba(abaSelecionada);
        });

        // Adicionar TabbedPane à janela principal
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel criarAbaItensSemPatrimonio(Color corFundo, Color corPrimaria, Color corTexto) {
        JPanel aba = new JPanel(new BorderLayout(8, 8));
        aba.setBackground(corFundo);
        aba.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== PAINEL SUPERIOR: PESQUISA DE DESCRIÇÕES ==========
        JPanel painelPesquisa = criarPainelPesquisaDescricoes(corPrimaria);

        // ========== PAINEL CENTRAL: SPLIT ENTRE RESULTADOS E FORMULÁRIO ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.55);

        // Painel esquerdo: Resultados da pesquisa
        JPanel painelResultados = criarPainelResultadosPesquisa(corTexto);

        // Painel direito: Formulário de registro
        JPanel painelFormulario = criarPainelFormularioRegistro(corTexto);

        splitPane.setLeftComponent(painelResultados);
        splitPane.setRightComponent(painelFormulario);

        // ========== PAINEL INFERIOR: TABELA DE ITENS REGISTRADOS ==========
        JPanel painelTabela = criarPainelTabelaItensRegistrados(corTexto);

        // ========== MONTAGEM FINAL ==========
        aba.add(painelPesquisa, BorderLayout.NORTH);
        aba.add(splitPane, BorderLayout.CENTER);
        aba.add(painelTabela, BorderLayout.SOUTH);

        return aba;
    }

    /**
     * Cria a aba de Itens Pendentes (não coletados) na sala selecionada
     * Mostra todos os patrimônios da sala que ainda não foram coletados no inventário atual
     */
    private JPanel criarAbaItensPendentes(Color corFundo, Color corPrimaria, Color corTexto) {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBackground(corFundo);
        aba.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ========== PAINEL SUPERIOR: INFORMAÇÕES E PROGRESSO ==========
        JPanel painelSuperior = new JPanel(new BorderLayout(10, 10));
        painelSuperior.setBackground(Color.WHITE);
        painelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Painel esquerdo: Título e informações
        JPanel painelInfo = new JPanel(new GridLayout(3, 1, 5, 5));
        painelInfo.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("📋 Itens Pendentes de Coleta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(corPrimaria);

        lblTotalPendentes = new JLabel("Selecione uma sala para ver os itens pendentes");
        lblTotalPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalPendentes.setForeground(new Color(108, 117, 125));

        lblProgressoColeta = new JLabel("Progresso: -");
        lblProgressoColeta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgressoColeta.setForeground(new Color(46, 204, 113));

        painelInfo.add(lblTitulo);
        painelInfo.add(lblTotalPendentes);
        painelInfo.add(lblProgressoColeta);

        // Painel direito: Barra de progresso
        JPanel painelProgresso = new JPanel(new BorderLayout(5, 5));
        painelProgresso.setBackground(Color.WHITE);
        painelProgresso.setPreferredSize(new Dimension(300, 60));

        progressBarColeta = new JProgressBar(0, 100);
        progressBarColeta.setStringPainted(true);
        progressBarColeta.setString("0%");
        progressBarColeta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBarColeta.setForeground(new Color(46, 204, 113));
        progressBarColeta.setBackground(new Color(236, 240, 241));
        progressBarColeta.setPreferredSize(new Dimension(280, 25));

        JLabel lblBarraProgresso = new JLabel("Progresso da Coleta na Sala:");
        lblBarraProgresso.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBarraProgresso.setForeground(new Color(108, 117, 125));

        painelProgresso.add(lblBarraProgresso, BorderLayout.NORTH);
        painelProgresso.add(progressBarColeta, BorderLayout.CENTER);

        painelSuperior.add(painelInfo, BorderLayout.WEST);
        painelSuperior.add(painelProgresso, BorderLayout.EAST);

        // ========== PAINEL DE BUSCA ==========
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelBusca.setBackground(Color.WHITE);
        painelBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel lblBusca = new JLabel("🔍 Filtrar:");
        lblBusca.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));

        campoBuscaPendentes = new JTextField(25);
        campoBuscaPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoBuscaPendentes.setPreferredSize(new Dimension(250, 30));
        campoBuscaPendentes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        campoBuscaPendentes.setToolTipText("Digite número ou descrição para filtrar");

        // Listener para filtrar em tempo real
        campoBuscaPendentes.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabelaPendentes(campoBuscaPendentes.getText());
            }
        });

        btnExportarPendentes = createStyledButton("📥 Exportar Lista", new Color(52, 152, 219));
        btnExportarPendentes.setEnabled(false);
        btnExportarPendentes.addActionListener(e -> exportarListaPendentes());

        JButton btnAtualizarPendentes = createStyledButton("🔄 Atualizar", new Color(46, 204, 113));
        btnAtualizarPendentes.addActionListener(e -> carregarItensPendentes());

        painelBusca.add(lblBusca);
        painelBusca.add(campoBuscaPendentes);
        painelBusca.add(Box.createHorizontalStrut(20));
        painelBusca.add(btnAtualizarPendentes);
        painelBusca.add(btnExportarPendentes);

        // Combinar painéis superiores
        JPanel painelTopoCompleto = new JPanel(new BorderLayout(0, 10));
        painelTopoCompleto.setBackground(corFundo);
        painelTopoCompleto.add(painelSuperior, BorderLayout.NORTH);
        painelTopoCompleto.add(painelBusca, BorderLayout.SOUTH);

        // ========== TABELA DE ITENS PENDENTES ==========
        String[] colunasPendentes = {"Número", "Descrição", "Localização Original", "Responsável", "Estado"};
        modeloTabelaPendentes = new DefaultTableModel(colunasPendentes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPendentes = new JTable(modeloTabelaPendentes);
        tabelaPendentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPendentes.setRowHeight(28);
        tabelaPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaPendentes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaPendentes.getTableHeader().setBackground(corPrimaria);
        tabelaPendentes.getTableHeader().setForeground(Color.WHITE);
        tabelaPendentes.setGridColor(new Color(220, 220, 220));
        tabelaPendentes.setSelectionBackground(new Color(52, 152, 219));
        tabelaPendentes.setSelectionForeground(Color.WHITE);
        tabelaPendentes.setBackground(Color.WHITE);

        // Configurar larguras das colunas
        tabelaPendentes.getColumnModel().getColumn(0).setPreferredWidth(100);  // Número
        tabelaPendentes.getColumnModel().getColumn(1).setPreferredWidth(300);  // Descrição
        tabelaPendentes.getColumnModel().getColumn(2).setPreferredWidth(150);  // Localização
        tabelaPendentes.getColumnModel().getColumn(3).setPreferredWidth(150);  // Responsável
        tabelaPendentes.getColumnModel().getColumn(4).setPreferredWidth(100);  // Estado

        // Duplo-clique para ir ao campo de busca com o número preenchido
        tabelaPendentes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabelaPendentes.getSelectedRow();
                    if (row >= 0) {
                        String numero = (String) modeloTabelaPendentes.getValueAt(row, 0);
                        // Ir para aba de coleta normal e preencher o campo de busca
                        tabbedPane.setSelectedIndex(0);
                        campoBusca.setText(numero);
                        campoBusca.requestFocus();
                        // Executar busca automaticamente
                        buscarPatrimonio();
                    }
                }
            }
        });

        JScrollPane scrollPendentes = new JScrollPane(tabelaPendentes);
        scrollPendentes.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPendentes.setBackground(Color.WHITE);

        // ========== PAINEL INFERIOR: DICA ==========
        JPanel painelDica = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelDica.setBackground(new Color(255, 248, 220));
        painelDica.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));

        JLabel lblDica = new JLabel("💡 Dica: Dê duplo-clique em um item para ir diretamente à tela de coleta com o número preenchido");
        lblDica.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDica.setForeground(new Color(133, 100, 4));
        painelDica.add(lblDica);

        // ========== MONTAGEM FINAL ==========
        aba.add(painelTopoCompleto, BorderLayout.NORTH);
        aba.add(scrollPendentes, BorderLayout.CENTER);
        aba.add(painelDica, BorderLayout.SOUTH);

        return aba;
    }

    /**
     * Carrega os itens pendentes (não coletados) da sala selecionada
     */
    private void carregarItensPendentes() {
        Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
        if (salaSelecionada == null) {
            lblTotalPendentes.setText("⚠️ Selecione uma sala para ver os itens pendentes");
            lblProgressoColeta.setText("Progresso: -");
            progressBarColeta.setValue(0);
            progressBarColeta.setString("0%");
            modeloTabelaPendentes.setRowCount(0);
            btnExportarPendentes.setEnabled(false);
            return;
        }

        // Mostrar loading
        lblTotalPendentes.setText("⏳ Carregando itens pendentes...");
        tabelaPendentes.setEnabled(false);

        // Executar em background
        SwingWorker<PendentesResult, Void> worker = new SwingWorker<>() {
            @Override
            protected PendentesResult doInBackground() throws Exception {
                int idSala = salaSelecionada.getId();
                
                // Buscar todos os patrimônios da sala
                List<Patrimonio> todosPatrimonios = patrimonioDAO.buscarPorSala(idSala);
                
                // Buscar IDs dos patrimônios já coletados no inventário atual
                Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
                List<Integer> idsColetados = new ArrayList<>();
                if (inventarioAtivo != null) {
                    idsColetados = patrimonioDAO.buscarPatrimoniosColetados(inventarioAtivo.getId());
                }
                
                // Filtrar apenas os não coletados
                List<Integer> finalIdsColetados = idsColetados;
                List<Patrimonio> pendentes = todosPatrimonios.stream()
                        .filter(p -> !finalIdsColetados.contains(p.getId()))
                        .toList();
                
                int totalSala = todosPatrimonios.size();
                int totalColetados = totalSala - pendentes.size();
                int totalPendentes = pendentes.size();
                double percentual = totalSala > 0 ? (totalColetados * 100.0 / totalSala) : 0;
                
                return new PendentesResult(pendentes, totalSala, totalColetados, totalPendentes, percentual);
            }

            @Override
            protected void done() {
                try {
                    PendentesResult resultado = get();
                    
                    // Atualizar labels
                    lblTotalPendentes.setText(String.format("📊 %d itens pendentes de %d na sala %s",
                            resultado.totalPendentes, resultado.totalSala, salaSelecionada.getNumeroSala()));
                    
                    lblProgressoColeta.setText(String.format("✅ Coletados: %d de %d (%.1f%%)",
                            resultado.totalColetados, resultado.totalSala, resultado.percentual));
                    
                    // Atualizar barra de progresso
                    progressBarColeta.setValue((int) resultado.percentual);
                    progressBarColeta.setString(String.format("%.1f%%", resultado.percentual));
                    
                    // Mudar cor da barra baseado no progresso
                    if (resultado.percentual >= 100) {
                        progressBarColeta.setForeground(new Color(46, 204, 113)); // Verde
                    } else if (resultado.percentual >= 50) {
                        progressBarColeta.setForeground(new Color(52, 152, 219)); // Azul
                    } else if (resultado.percentual >= 25) {
                        progressBarColeta.setForeground(new Color(241, 196, 15)); // Amarelo
                    } else {
                        progressBarColeta.setForeground(new Color(231, 76, 60)); // Vermelho
                    }
                    
                    // Preencher tabela
                    modeloTabelaPendentes.setRowCount(0);
                    for (Patrimonio p : resultado.pendentes) {
                        Object[] linha = {
                            p.getNumero(),
                            p.getDescricao() != null ? p.getDescricao() : "-",
                            p.getNomeSala() != null ? p.getNomeSala() : "-",
                            p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "-",
                            p.getEstadoConservacao() != null ? p.getEstadoConservacao() : "-"
                        };
                        modeloTabelaPendentes.addRow(linha);
                    }
                    
                    tabelaPendentes.setEnabled(true);
                    btnExportarPendentes.setEnabled(resultado.totalPendentes > 0);
                    
                    System.out.println("[PENDENTES] Carregados " + resultado.totalPendentes + 
                                     " itens pendentes para sala " + salaSelecionada.getNumeroSala());
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    tratarErroPendentes("Operação interrompida");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    String mensagemErro = causa != null ? causa.getMessage() : e.getMessage();
                    tratarErroPendentes(mensagemErro);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Trata erros ao carregar itens pendentes com opção de retry.
     */
    private void tratarErroPendentes(String mensagemErro) {
        lblTotalPendentes.setText("❌ Erro ao carregar itens pendentes");
        lblProgressoColeta.setText("Progresso: -");
        progressBarColeta.setValue(0);
        tabelaPendentes.setEnabled(true);
        LOG.error("Erro ao carregar itens pendentes: {}", mensagemErro);
        
        int opcao = JOptionPane.showOptionDialog(this,
                "Erro ao carregar itens pendentes: " + mensagemErro + "\n\n" +
                "Isso pode ocorrer devido a conexão lenta via VPN.\n" +
                "Deseja tentar novamente?",
                "Erro de Conexão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Tentar Novamente", "Cancelar"},
                "Tentar Novamente");
        
        if (opcao == JOptionPane.YES_OPTION) {
            carregarItensPendentes(); // Retry
        }
    }

    /**
     * Classe auxiliar para resultado da busca de pendentes
     */
    private static class PendentesResult {
        final List<Patrimonio> pendentes;
        final int totalSala;
        final int totalColetados;
        final int totalPendentes;
        final double percentual;

        PendentesResult(List<Patrimonio> pendentes, int totalSala, int totalColetados, 
                       int totalPendentes, double percentual) {
            this.pendentes = pendentes;
            this.totalSala = totalSala;
            this.totalColetados = totalColetados;
            this.totalPendentes = totalPendentes;
            this.percentual = percentual;
        }
    }

    /**
     * Filtra a tabela de pendentes baseado no texto digitado
     */
    private void filtrarTabelaPendentes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            // Recarregar todos os itens
            carregarItensPendentes();
            return;
        }
        
        String filtroLower = filtro.toLowerCase().trim();
        
        // Filtrar linhas visíveis (não recarrega do banco)
        // Para isso, precisamos manter uma lista original
        // Por simplicidade, vamos apenas destacar as linhas que correspondem
        for (int i = 0; i < tabelaPendentes.getRowCount(); i++) {
            String numero = String.valueOf(modeloTabelaPendentes.getValueAt(i, 0)).toLowerCase();
            String descricao = String.valueOf(modeloTabelaPendentes.getValueAt(i, 1)).toLowerCase();
            
            if (numero.contains(filtroLower) || descricao.contains(filtroLower)) {
                tabelaPendentes.setRowSelectionInterval(i, i);
                tabelaPendentes.scrollRectToVisible(tabelaPendentes.getCellRect(i, 0, true));
                return;
            }
        }
    }

    /**
     * Exporta a lista de itens pendentes para arquivo
     */
    private void exportarListaPendentes() {
        if (modeloTabelaPendentes.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Não há itens pendentes para exportar.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
        String nomeSala = salaSelecionada != null ? salaSelecionada.getNumeroSala() : "sala";
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Lista de Pendentes");
        fileChooser.setSelectedFile(new java.io.File("pendentes_" + nomeSala + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                // Cabeçalho
                writer.println("Número;Descrição;Localização;Responsável;Estado");
                
                // Dados
                for (int i = 0; i < modeloTabelaPendentes.getRowCount(); i++) {
                    StringBuilder linha = new StringBuilder();
                    for (int j = 0; j < modeloTabelaPendentes.getColumnCount(); j++) {
                        if (j > 0) linha.append(";");
                        Object valor = modeloTabelaPendentes.getValueAt(i, j);
                        linha.append(valor != null ? valor.toString() : "");
                    }
                    writer.println(linha);
                }
                
                JOptionPane.showMessageDialog(this,
                    "Lista exportada com sucesso!\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao exportar: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
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

        btnBuscarDescricao = createStyledButton("🔍 Pesquisar", new Color(108, 117, 125));
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
        btnRegistrarSemPatrimonio = createStyledButton("✅ Registrar", new Color(46, 204, 113));
        btnRegistrarSemPatrimonio.setPreferredSize(new Dimension(120, 32));
        btnRegistrarSemPatrimonio.addActionListener(e -> registrarColetaComDescricaoSelecionada());

        JButton btnLimpar = createStyledButton("🗑️ Limpar", new Color(108, 117, 125));
        btnLimpar.setPreferredSize(new Dimension(120, 32));
        btnLimpar.addActionListener(e -> limparFormularioSemPatrimonio());

        btnRemoverSemPatrimonio = createStyledButton("❌ Remover", new Color(231, 76, 60));
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

        JButton btnAtualizar = createStyledButton("🔄 Atualizar", new Color(52, 152, 219));
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

    // Métodos configurarEventListenersSemPatrimonio, pesquisarDescricoesUnicas,
    // mostrarSugestoes e ocultarSugestoes foram removidos pois não são mais
    // necessários
    // na nova interface simplificada

    /**
     * Carrega todos os itens sem patrimônio registrados na sala atual
     */
    private void carregarTodosItensSemPatrimonio() {
        System.out.println("=== DEBUG TIMESTAMP: Iniciando carregarTodosItensSemPatrimonio ===");
        
        Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
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
            // Buscar itens sem etiqueta da sala (usando numeroSala para consistência com dados no banco)
            System.out.println("DEBUG TIMESTAMP: Buscando itens sem etiqueta...");
            List<Coleta> itensSemPatrimonio = coletaDAO.buscarColetasSemEtiquetaPorSala(
                    salaSelecionada.getIdSala(),
                    salaSelecionada.getNumeroSala());

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
        Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
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
            Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
            if (salaSelecionada == null) {
                return;
            }

            // Obter dados da linha selecionada
            String dataHora = (String) modeloTabelaSemPatrimonio.getValueAt(linhaSelecionada, 0);
            String descricao = (String) modeloTabelaSemPatrimonio.getValueAt(linhaSelecionada, 1);
            String localizacao = (String) modeloTabelaSemPatrimonio.getValueAt(linhaSelecionada, 3);

            // Buscar e excluir a coleta (usando numeroSala para consistência com dados no banco)
            List<Coleta> itensSemPatrimonio = coletaDAO.buscarColetasSemEtiquetaPorSala(
                    salaSelecionada.getIdSala(),
                    salaSelecionada.getNumeroSala());

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

        // Event listeners para busca por descrição
        btnBuscarDescricao.addActionListener(e -> buscarPorDescricao());
        campoBuscaDescricao.addActionListener(e -> buscarPorDescricao());

        // Event listeners de busca removidos para simplificar interface

        // Listener para ver itens agrupados
        btnVerItensAgrupados.addActionListener(e -> mostrarItensAgrupados());

        // Listener para seleção na tabela de itens agrupados
        tabelaItensAgrupados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarDescricaoAgrupada();
            }
        });

        // Listener para o botão de finalização da coleta
        btnFinalizarColeta.addActionListener(e -> finalizarColetaSala());

        // Listener para o botão de reabertura da coleta (apenas admin/supervisor)
        if (btnReabrirColeta.isVisible()) {
            btnReabrirColeta.addActionListener(e -> reabrirColetaSala());
        }

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

        // Event listener para seleção na tabela de resultados
        tabelaResultadosDescricao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarPatrimonioDescricao();
            }
        });

        // Listener para seleção na tabela de itens agrupados
        tabelaItensAgrupados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarDescricaoAgrupada();
            }
        });
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
        // Usar SwingWorker para carregamento assíncrono (evita travamento com VPN)
        carregarSalasAsync();
    }
    
    /**
     * Carrega salas de forma assíncrona e PROGRESSIVA usando SwingWorker.
     * Carrega em lotes pequenos para funcionar bem com VPN lenta.
     * Atualiza o combo conforme os dados chegam.
     */
    private void carregarSalasAsync() {
        carregarSalasProgressivo();
    }
    
    /**
     * Carrega salas de forma PROGRESSIVA (em lotes).
     * Ideal para conexões VPN com ping alto.
     * AJUSTADO: Lotes de 5 salas para VPN muito lenta.
     */
    private void carregarSalasProgressivo() {
        final int TAMANHO_LOTE = 5; // Carregar apenas 5 salas por vez (VPN lenta)
        final int DELAY_ENTRE_LOTES = 200; // 200ms entre lotes
        
        // Desabilitar combo e limpar
        comboSalas.setEnabled(false);
        comboSalas.removeAllItems();
        comboSalas.addItem(null); // Item placeholder
        todasSalas = new ArrayList<>();
        
        // Mudar cursor para indicar carregamento
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Criar dialog de loading com progresso real
        JDialog loadingDialog = new JDialog(this, "Carregando salas...", false);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setLayout(new BorderLayout(10, 10));
        loadingDialog.setSize(420, 160);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblMensagem = new JLabel("<html><center>Carregando salas progressivamente...<br>" +
            "<small>Conexão VPN lenta - carregando em lotes de " + TAMANHO_LOTE + "</small></center></html>");
        lblMensagem.setHorizontalAlignment(SwingConstants.CENTER);
        lblMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel lblProgresso = new JLabel("Conectando...");
        lblProgresso.setHorizontalAlignment(SwingConstants.CENTER);
        lblProgresso.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblProgresso.setForeground(new Color(0, 100, 0));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Iniciando...");
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(lblMensagem, BorderLayout.NORTH);
        centerPanel.add(lblProgresso, BorderLayout.CENTER);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        loadingDialog.add(panel);
        
        loadingDialog.setVisible(true);
        
        // Worker que carrega em lotes e publica progresso
        SwingWorker<Void, Sala> worker = new SwingWorker<>() {
            private Inventario inventarioAtivo = null;
            private int totalSalas = 0;
            private int salasCarregadas = 0;
            private boolean usouFallback = false;
            private String erroFinal = null;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    LOG.info("Iniciando carregamento progressivo de salas [Thread: {}]", Thread.currentThread().getName());
                    
                    // 1. Buscar inventário ativo (query rápida)
                    SwingUtilities.invokeLater(() -> {
                        lblProgresso.setText("Buscando inventário ativo...");
                        progressBar.setIndeterminate(true);
                    });
                    
                    inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                    
                    if (inventarioAtivo == null) {
                        LOG.warn("Nenhum inventário ativo encontrado");
                        return null;
                    }
                    
                    LOG.info("Inventário ativo: {} (ID: {})", inventarioAtivo.getNome(), inventarioAtivo.getId());
                    
                    // 2. Contar total de salas (query rápida)
                    SwingUtilities.invokeLater(() -> lblProgresso.setText("Contando salas..."));
                    
                    totalSalas = salaInventarioDAO.contarSalasAtivas(inventarioAtivo.getId());
                    LOG.info("Total de salas a carregar: {}", totalSalas);
                    
                    if (totalSalas == 0) {
                        LOG.warn("Nenhuma sala encontrada");
                        return null;
                    }
                    
                    // Configurar barra de progresso
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(false);
                        progressBar.setMaximum(totalSalas);
                        progressBar.setValue(0);
                        progressBar.setString("0 / " + totalSalas + " salas");
                    });
                    
                    // 3. Carregar salas em lotes
                    int offset = 0;
                    int tentativasErro = 0;
                    final int MAX_TENTATIVAS_LOTE = 3;
                    
                    while (offset < totalSalas && tentativasErro < MAX_TENTATIVAS_LOTE) {
                        final int loteAtual = (offset / TAMANHO_LOTE) + 1;
                        final int totalLotes = (int) Math.ceil((double) totalSalas / TAMANHO_LOTE);
                        
                        SwingUtilities.invokeLater(() -> 
                            lblProgresso.setText("Carregando lote " + loteAtual + " de " + totalLotes + "...")
                        );
                        
                        try {
                            List<Sala> lote = salaInventarioDAO.buscarSalasAtivasPaginado(
                                inventarioAtivo.getId(), offset, TAMANHO_LOTE
                            );
                            
                            if (lote.isEmpty()) {
                                LOG.debug("Lote vazio retornado, finalizando");
                                break;
                            }
                            
                            // Publicar cada sala para atualizar UI progressivamente
                            for (Sala sala : lote) {
                                publish(sala);
                                salasCarregadas++;
                            }
                            
                            offset += lote.size();
                            tentativasErro = 0; // Reset contador de erros
                            
                            LOG.debug("Lote {} carregado: {} salas (total: {}/{})", 
                                loteAtual, lote.size(), salasCarregadas, totalSalas);
                            
                            // Pausa entre lotes para não sobrecarregar conexão VPN lenta
                            if (offset < totalSalas) {
                                Thread.sleep(DELAY_ENTRE_LOTES);
                            }
                            
                        } catch (Exception e) {
                            tentativasErro++;
                            LOG.warn("Erro no lote {} (tentativa {}/{}): {}", 
                                loteAtual, tentativasErro, MAX_TENTATIVAS_LOTE, e.getMessage());
                            
                            if (tentativasErro < MAX_TENTATIVAS_LOTE) {
                                // Aguardar antes de retry
                                SwingUtilities.invokeLater(() -> 
                                    lblProgresso.setText("Erro de conexão. Tentando novamente em 3s...")
                                );
                                Thread.sleep(3000);
                            } else {
                                erroFinal = "Falha após " + MAX_TENTATIVAS_LOTE + " tentativas: " + e.getMessage();
                                LOG.error("Falha definitiva no carregamento: {}", erroFinal);
                            }
                        }
                    }
                    
                    // Se não carregou nenhuma sala, tentar fallback
                    if (salasCarregadas == 0 && erroFinal == null) {
                        LOG.info("Tentando fallback: buscar todas as salas ativas");
                        usouFallback = true;
                        
                        SwingUtilities.invokeLater(() -> {
                            lblProgresso.setText("Carregando todas as salas ativas...");
                            progressBar.setIndeterminate(true);
                        });
                        
                        List<Sala> todasAtivas = salaInventarioDAO.buscarTodasSalasAtivas();
                        for (Sala sala : todasAtivas) {
                            publish(sala);
                            salasCarregadas++;
                        }
                    }
                    
                } catch (Exception e) {
                    LOG.error("Erro crítico no carregamento progressivo: {}", e.getMessage(), e);
                    erroFinal = e.getMessage();
                }
                
                return null;
            }
            
            @Override
            protected void process(List<Sala> chunks) {
                // Atualizar combo com as salas que chegaram
                for (Sala sala : chunks) {
                    todasSalas.add(sala);
                    comboSalas.addItem(sala);
                }
                
                // Atualizar progresso
                int carregadas = todasSalas.size();
                int total = totalSalas > 0 ? totalSalas : carregadas;
                int percentual = total > 0 ? (carregadas * 100 / total) : 0;
                
                progressBar.setValue(carregadas);
                progressBar.setString(carregadas + " / " + total + " salas (" + percentual + "%)");
                
                // Habilitar combo assim que tiver pelo menos uma sala
                if (carregadas == 1) {
                    comboSalas.setEnabled(true);
                    lblProgresso.setText("✓ Salas disponíveis! Carregando mais...");
                }
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                setCursor(Cursor.getDefaultCursor());
                comboSalas.setEnabled(true);
                
                LOG.info("Carregamento finalizado: {} salas carregadas", todasSalas.size());
                
                // Mostrar mensagens apropriadas
                if (inventarioAtivo == null) {
                    JOptionPane.showMessageDialog(ColetaFrame_v2.this,
                            "Nenhum inventário ativo encontrado.\nNão é possível realizar coletas.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                } else if (erroFinal != null && todasSalas.isEmpty()) {
                    tratarErroCarregamentoComRetry(erroFinal, 2, 3);
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
                } else if (erroFinal != null) {
                    // Carregou parcialmente
                    JOptionPane.showMessageDialog(ColetaFrame_v2.this, 
                            "Carregamento parcial: " + todasSalas.size() + " salas.\n" +
                            "Algumas salas podem não ter sido carregadas devido a problemas de conexão.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Verifica se o erro é relacionado a problemas de rede/conexão.
     */
    private boolean isErroDeRede(Throwable causa) {
        if (causa == null) return false;
        
        String mensagem = causa.getMessage();
        if (mensagem == null) mensagem = "";
        mensagem = mensagem.toLowerCase();
        
        // Verificar tipos de exceção de rede
        if (causa instanceof java.net.SocketException ||
            causa instanceof java.net.SocketTimeoutException ||
            causa instanceof java.io.IOException ||
            causa instanceof java.sql.SQLTransientConnectionException) {
            return true;
        }
        
        // Verificar mensagens comuns de erro de rede
        return mensagem.contains("connection reset") ||
               mensagem.contains("connection refused") ||
               mensagem.contains("connection timed out") ||
               mensagem.contains("socket") ||
               mensagem.contains("timeout") ||
               mensagem.contains("i/o error") ||
               mensagem.contains("network") ||
               mensagem.contains("08006") || // SQLSTATE para connection failure
               mensagem.contains("08001") || // SQLSTATE para unable to connect
               mensagem.contains("sending to the backend");
    }
    
    /**
     * Trata erros de carregamento com opção de retry manual.
     * @param mensagemErro Mensagem de erro
     * @param tentativaAtual Número da tentativa atual
     * @param maxTentativas Número máximo de tentativas
     */
    private void tratarErroCarregamentoComRetry(String mensagemErro, int tentativaAtual, int maxTentativas) {
        todasSalas = new ArrayList<>();
        comboSalas.setEnabled(true);
        LOG.error("Erro ao carregar salas (tentativa {}/{}): {}", tentativaAtual + 1, maxTentativas, mensagemErro);
        
        String infoTentativas = tentativaAtual > 0 
            ? "\n\nForam realizadas " + (tentativaAtual + 1) + " tentativas automáticas."
            : "";
        
        String dicasVPN = "\n\nDicas para conexão VPN:\n" +
            "• Verifique se a VPN está conectada\n" +
            "• Tente reconectar a VPN\n" +
            "• Aguarde alguns segundos e tente novamente";
        
        int opcao = JOptionPane.showOptionDialog(this,
                "Erro ao carregar salas:\n" + mensagemErro + infoTentativas + dicasVPN,
                "Erro de Conexão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Tentar Novamente", "Cancelar"},
                "Tentar Novamente");
        
        if (opcao == JOptionPane.YES_OPTION) {
            carregarSalasAsync(); // Retry manual (reinicia contagem)
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
                // Habilitar componentes específicos da aba "Itens Pendentes"
                if (campoBuscaPendentes != null)
                    campoBuscaPendentes.setEnabled(true);
                if (tabelaPendentes != null)
                    tabelaPendentes.setEnabled(true);
                if (btnExportarPendentes != null)
                    btnExportarPendentes.setEnabled(modeloTabelaPendentes != null && modeloTabelaPendentes.getRowCount() > 0);
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
                if (campoDescricaoSemPatrimonio != null)
                    campoDescricaoSemPatrimonio.setEnabled(true);
                if (campoLocalizacaoSemPatrimonio != null)
                    campoLocalizacaoSemPatrimonio.setEnabled(true);
                if (areaObservacoesSemPatrimonio != null)
                    areaObservacoesSemPatrimonio.setEnabled(true);
                if (btnRegistrarSemPatrimonio != null)
                    btnRegistrarSemPatrimonio.setEnabled(true);
                if (campoBuscaDescricao != null)
                    campoBuscaDescricao.setEnabled(true);
                if (comboCategoriasBusca != null)
                    comboCategoriasBusca.setEnabled(true);
                if (btnBuscarDescricao != null)
                    btnBuscarDescricao.setEnabled(true);
                System.out.println(
                        "DEBUG: Aba 2 - campoBuscaDescricao, comboCategoriasBusca e btnBuscarDescricao habilitados");
                if (tabelaResultadosDescricao != null)
                    tabelaResultadosDescricao.setEnabled(true);
                if (btnVerItensAgrupados != null)
                    btnVerItensAgrupados.setEnabled(true);
                if (tabelaSemPatrimonio != null)
                    tabelaSemPatrimonio.setEnabled(true);
                if (btnRemoverSemPatrimonio != null)
                    btnRemoverSemPatrimonio.setEnabled(true);
                if (btnExportarSemPatrimonio != null)
                    btnExportarSemPatrimonio.setEnabled(true);
                // Carregar itens agrupados automaticamente
                carregarItensAgrupados();
                // Carregar itens sem patrimônio da sala atual
                carregarTodosItensSemPatrimonio();
                // Focar na descrição
                SwingUtilities.invokeLater(() -> {
                    if (campoDescricaoSemPatrimonio != null) {
                        campoDescricaoSemPatrimonio.requestFocus();
                    }
                });
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
        if (campoDescricaoSemPatrimonio != null)
            campoDescricaoSemPatrimonio.setEnabled(false);
        if (campoLocalizacaoSemPatrimonio != null)
            campoLocalizacaoSemPatrimonio.setEnabled(false);
        if (areaObservacoesSemPatrimonio != null)
            areaObservacoesSemPatrimonio.setEnabled(false);
        if (btnRegistrarSemPatrimonio != null)
            btnRegistrarSemPatrimonio.setEnabled(false);
        if (campoBuscaDescricao != null)
            campoBuscaDescricao.setEnabled(false);
        if (comboCategoriasBusca != null)
            comboCategoriasBusca.setEnabled(false);
        if (btnBuscarDescricao != null)
            btnBuscarDescricao.setEnabled(false);
        if (tabelaResultadosDescricao != null)
            tabelaResultadosDescricao.setEnabled(false);
        if (btnVerItensAgrupados != null)
            btnVerItensAgrupados.setEnabled(false);
        if (tabelaSemPatrimonio != null)
            tabelaSemPatrimonio.setEnabled(false);
        if (btnRemoverSemPatrimonio != null)
            btnRemoverSemPatrimonio.setEnabled(false);
        if (btnExportarSemPatrimonio != null)
            btnExportarSemPatrimonio.setEnabled(false);
    }

    /**
     * Desabilita todos os componentes da aba "Itens Pendentes"
     */
    private void desabilitarComponentesAbaItensPendentes() {
        if (campoBuscaPendentes != null)
            campoBuscaPendentes.setEnabled(false);
        if (tabelaPendentes != null)
            tabelaPendentes.setEnabled(false);
        if (btnExportarPendentes != null)
            btnExportarPendentes.setEnabled(false);
    }

    private void carregarDadosSala() {
        System.out.println("DEBUG: carregarDadosSala() chamado");
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
            if (campoLocalizacaoSemPatrimonio != null) {
                campoLocalizacaoSemPatrimonio.setText("");
            }
            
            // Resetar contagem de coletas e limpar tabela de histórico
            atualizarContagemColetas(0);
            modeloTabelaHistorico.setRowCount(0);

            return;
        }

        // Atualizar campo de localização na aba de itens sem patrimônio IMEDIATAMENTE
        if (campoLocalizacaoSemPatrimonio != null) {
            campoLocalizacaoSemPatrimonio.setText(salaSelecionada.getIdentificacaoCompleta());
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
                carregarTodosItensSemPatrimonio();
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
                // 2. Sala está FINALIZADA (condição atual)
                // 3. Modo ONLINE (requer conexão com servidor)
                if (btnReabrirColeta.isVisible()) {
                    boolean podeReabrir = !modoOffline && salaFinalizada; // Online E sala finalizada
                    btnReabrirColeta.setEnabled(podeReabrir);
                    btnReabrirColeta.setBackground(new Color(255, 193, 7)); // Amarelo/laranja
                    
                    if (modoOffline) {
                        btnReabrirColeta.setToolTipText("⚠️ Reabrir sala requer conexão com o servidor");
                    } else if (salaFinalizada) {
                        btnReabrirColeta.setToolTipText("Reabrir sala finalizada para permitir novas coletas");
                    } else {
                        btnReabrirColeta.setToolTipText("Sala já está aberta para coleta");
                    }
                    
                    System.out.println("DEBUG: btnReabrir habilitado=" + podeReabrir + 
                                     " (modoOffline=" + modoOffline + ", salaFinalizada=" + salaFinalizada + ")");
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
                                  (!modoOffline && salaFinalizada) + ", btnRemover=false (aguardando seleção)" + 
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
                
                // Botão Reabrir: SEMPRE desabilitado (sala está ABERTA, não finalizada)
                // Só fica ativo quando sala está FINALIZADA
                if (btnReabrirColeta.isVisible()) {
                    btnReabrirColeta.setEnabled(false);
                    btnReabrirColeta.setBackground(new Color(255, 193, 7)); // Restaurar cor original
                    btnReabrirColeta.setToolTipText("Sala já está aberta para coleta");
                    System.out.println("DEBUG: btnReabrir desabilitado - sala aberta (salaFinalizada=false)");
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
                                  ", btnReabrir=false (sala aberta), btnRemover=false (aguardando seleção)" + 
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
    private static final int LIMITE_HISTORICO = 100;
    
    /**
     * Carrega histórico de coletas de forma PROGRESSIVA e ASSÍNCRONA.
     * - Carrega em lotes pequenos para funcionar bem com VPN lenta
     * - Atualiza tabela conforme os dados chegam
     * - Mostra progresso real ao usuário
     */
    private void carregarHistoricoColeta(String identificacaoSala) {
        System.out.println("[PROGRESSIVO] ========================================");
        System.out.println("[PROGRESSIVO] Carregando histórico para sala: '" + identificacaoSala + "'");
        System.out.println("[PROGRESSIVO] Modo offline: " + offlineManager.isOperatingOffline());
        System.out.println("[PROGRESSIVO] ========================================");
        
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            System.out.println("[PROGRESSIVO] ERRO: Nenhuma sala selecionada!");
            return;
        }
        
        // Modo offline usa carregamento simples (SQLite é rápido)
        if (offlineManager.isOperatingOffline()) {
            carregarHistoricoOffline(identificacaoSala, salaAtual);
            return;
        }
        
        // Modo online usa carregamento progressivo
        carregarHistoricoProgressivo(identificacaoSala, salaAtual);
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
                } catch (Exception e) {
                    tratarErroHistorico(e.getMessage(), identificacaoSala);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Carrega histórico do PostgreSQL de forma PROGRESSIVA (em lotes).
     * Ideal para conexões VPN com ping alto.
     * AJUSTADO: Lotes de 1 coleta + query simplificada para VPN MUITO lenta.
     * Exibe dialog de progresso com barra visual.
     */
    private void carregarHistoricoProgressivo(String identificacaoSala, Sala salaAtual) {
        final int TAMANHO_LOTE = 1; // Carregar 1 coleta por vez (VPN extremamente lenta)
        final int DELAY_ENTRE_LOTES = 300; // 300ms entre lotes
        final int DELAY_APOS_ERRO = 5000; // 5s após erro antes de retry
        
        // Limpar tabela
        modeloTabelaHistorico.setRowCount(0);
        tabelaHistorico.setEnabled(false);
        
        // Criar dialog de progresso (NÃO MODAL para não bloquear)
        JDialog progressDialog = new JDialog(this, "Carregando coletas...", false);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.setSize(450, 180);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitulo = new JLabel("<html><center><b>Carregando histórico de coletas</b><br>" +
            "<small>Sala: " + salaAtual.getIdentificacaoCompleta() + "</small></center></html>");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel lblStatus = new JLabel("Conectando ao banco de dados...");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(0, 100, 0));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Iniciando...");
        progressBar.setPreferredSize(new Dimension(400, 25));
        
        JLabel lblDetalhes = new JLabel("<html><small>Conexão VPN lenta - carregando em lotes de " + TAMANHO_LOTE + "</small></html>");
        lblDetalhes.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetalhes.setForeground(Color.GRAY);
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 8));
        centerPanel.add(lblTitulo, BorderLayout.NORTH);
        centerPanel.add(lblStatus, BorderLayout.CENTER);
        centerPanel.add(lblDetalhes, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(panel);
        
        // Mostrar dialog
        progressDialog.setVisible(true);
        
        // Lista para acumular coletas
        final List<Coleta> todasColetas = new ArrayList<>();
        
        SwingWorker<Void, Coleta> worker = new SwingWorker<>() {
            private int totalColetas = 0;
            private String erroFinal = null;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 1. Contar total (query rápida)
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Contando coletas...");
                        progressBar.setIndeterminate(true);
                    });
                    
                    totalColetas = coletaDAO.contarColetasPorNumeroSala(identificacaoSala);
                    System.out.println("[PROGRESSIVO] Total de coletas a carregar: " + totalColetas);
                    
                    if (totalColetas == 0) {
                        // Verificar coletas locais pendentes
                        try {
                            List<Coleta> coletasLocais = coletaOfflineService.buscarColetasPorLocalizacao(identificacaoSala);
                            if (!coletasLocais.isEmpty()) {
                                System.out.println("[PROGRESSIVO] Encontradas " + coletasLocais.size() + " coletas locais pendentes");
                                SwingUtilities.invokeLater(() -> {
                                    lblStatus.setText("Carregando coletas locais...");
                                    progressBar.setIndeterminate(false);
                                    progressBar.setMaximum(coletasLocais.size());
                                });
                                int i = 0;
                                for (Coleta c : coletasLocais) {
                                    publish(c);
                                    final int progresso = ++i;
                                    SwingUtilities.invokeLater(() -> {
                                        progressBar.setValue(progresso);
                                        progressBar.setString(progresso + " / " + coletasLocais.size());
                                    });
                                }
                                totalColetas = coletasLocais.size();
                            }
                        } catch (Exception e) {
                            System.err.println("[PROGRESSIVO] Erro ao buscar coletas locais: " + e.getMessage());
                        }
                        return null;
                    }
                    
                    // Configurar barra de progresso
                    final int limiteFinal = Math.min(totalColetas, LIMITE_HISTORICO);
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(false);
                        progressBar.setMaximum(limiteFinal);
                        progressBar.setValue(0);
                        progressBar.setString("0 / " + limiteFinal + " coletas");
                        lblStatus.setText("Carregando coletas do servidor...");
                    });
                    
                    // 2. Carregar em lotes PEQUENOS (5 por vez para VPN lenta)
                    int offset = 0;
                    int tentativasErro = 0;
                    final int MAX_TENTATIVAS = 5;
                    
                    System.out.println("[PROGRESSIVO] Carregando em lotes de " + TAMANHO_LOTE + " (limite: " + limiteFinal + ")");
                    
                    while (offset < limiteFinal && tentativasErro < MAX_TENTATIVAS) {
                        final int loteAtual = (offset / TAMANHO_LOTE) + 1;
                        final int totalLotes = (int) Math.ceil((double) limiteFinal / TAMANHO_LOTE);
                        final int offsetAtual = offset;
                        
                        SwingUtilities.invokeLater(() -> {
                            lblStatus.setText("Carregando lote " + loteAtual + " de " + totalLotes + "...");
                            int percentual = limiteFinal > 0 ? (offsetAtual * 100 / limiteFinal) : 0;
                            lblDetalhes.setText("<html><small>" + percentual + "% concluído</small></html>");
                        });
                        
                        try {
                            List<Coleta> lote = coletaDAO.buscarColetasPorNumeroSalaPaginado(
                                identificacaoSala, offset, TAMANHO_LOTE
                            );
                            
                            if (lote.isEmpty()) {
                                System.out.println("[PROGRESSIVO] Lote vazio, finalizando");
                                break;
                            }
                            
                            // Publicar cada coleta para atualizar tabela progressivamente
                            for (Coleta coleta : lote) {
                                publish(coleta);
                            }
                            
                            offset += lote.size();
                            tentativasErro = 0;
                            
                            // Atualizar barra de progresso
                            final int progressoAtual = offset;
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setValue(progressoAtual);
                                progressBar.setString(progressoAtual + " / " + limiteFinal + " coletas");
                            });
                            
                            System.out.println("[PROGRESSIVO] Lote " + loteAtual + " OK: " + lote.size() + " coletas (total: " + offset + ")");
                            
                            // Pausa entre lotes para não sobrecarregar VPN
                            if (offset < limiteFinal) {
                                Thread.sleep(DELAY_ENTRE_LOTES);
                            }
                            
                        } catch (Exception e) {
                            tentativasErro++;
                            System.err.println("[PROGRESSIVO] ERRO lote " + loteAtual + " (tentativa " + tentativasErro + "/" + MAX_TENTATIVAS + "): " + e.getMessage());
                            
                            if (tentativasErro < MAX_TENTATIVAS) {
                                final int tentativa = tentativasErro;
                                SwingUtilities.invokeLater(() -> {
                                    lblStatus.setText("⚠️ Erro de conexão. Aguardando...");
                                    lblStatus.setForeground(new Color(200, 100, 0));
                                    lblDetalhes.setText("<html><small>Tentativa " + tentativa + " de " + MAX_TENTATIVAS + " - Reconectando em 3s...</small></html>");
                                });
                                Thread.sleep(DELAY_APOS_ERRO);
                                SwingUtilities.invokeLater(() -> {
                                    lblStatus.setForeground(new Color(0, 100, 0));
                                });
                            } else {
                                erroFinal = "Falha após " + MAX_TENTATIVAS + " tentativas";
                                LOG.error("Falha definitiva ao carregar coletas: {}", e.getMessage());
                            }
                        }
                    }
                    
                    // 3. Adicionar coletas locais pendentes (se houver)
                    try {
                        SwingUtilities.invokeLater(() -> lblStatus.setText("Verificando coletas locais..."));
                        
                        List<Coleta> coletasLocais = coletaOfflineService.buscarColetasPorLocalizacao(identificacaoSala);
                        if (!coletasLocais.isEmpty()) {
                            System.out.println("[PROGRESSIVO] Adicionando " + coletasLocais.size() + " coletas locais pendentes");
                            for (Coleta coletaLocal : coletasLocais) {
                                boolean jaExiste = todasColetas.stream()
                                    .anyMatch(c -> c.getNumeroPatrimonio() != null && 
                                                  c.getNumeroPatrimonio().equals(coletaLocal.getNumeroPatrimonio()));
                                if (!jaExiste) {
                                    publish(coletaLocal);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[PROGRESSIVO] Erro ao buscar coletas locais: " + e.getMessage());
                    }
                    
                } catch (Exception e) {
                    erroFinal = e.getMessage();
                    LOG.error("Erro ao carregar histórico progressivo: {}", e.getMessage(), e);
                }
                
                return null;
            }
            
            @Override
            protected void process(List<Coleta> chunks) {
                // Adicionar coletas à tabela conforme chegam
                for (Coleta coleta : chunks) {
                    todasColetas.add(coleta);
                    adicionarColetaNaTabela(coleta);
                }
                
                // Atualizar contagem
                atualizarContagemColetas(todasColetas.size());
            }
            
            @Override
            protected void done() {
                // Fechar dialog de progresso
                progressDialog.dispose();
                
                tabelaHistorico.setEnabled(true);
                
                if (erroFinal != null && todasColetas.isEmpty()) {
                    tratarErroHistorico(erroFinal, identificacaoSala);
                } else {
                    String sufixo = totalColetas > LIMITE_HISTORICO 
                        ? " (mostrando " + LIMITE_HISTORICO + " de " + totalColetas + ")" 
                        : "";
                    lblResumoSala.setText(String.format("✓ Coletando em: %s (%d itens)%s", 
                        salaAtual.getIdentificacaoCompleta(), todasColetas.size(), sufixo));
                    
                    if (erroFinal != null) {
                        // Carregou parcialmente
                        JOptionPane.showMessageDialog(ColetaFrame_v2.this,
                            "Carregamento parcial: " + todasColetas.size() + " coletas.\n" +
                            "Algumas coletas podem não ter sido carregadas devido a problemas de conexão.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Adiciona uma coleta na tabela de histórico.
     */
    /**
     * Adiciona uma coleta no FINAL da tabela (usado no carregamento progressivo).
     * As coletas já vêm ordenadas por data DESC do banco, então adicionar no final mantém a ordem.
     */
    private void adicionarColetaNaTabela(Coleta coleta) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Colunas: { "Data/Hora", "Patrimônio", "Descrição", "Estado", "ID" }
        String data = coleta.getDataColeta() != null ? sdf.format(coleta.getDataColeta()) : "-";
        String numero = coleta.getNumeroPatrimonio() != null ? coleta.getNumeroPatrimonio() : "-";
        String descricao = coleta.getDescricaoPatrimonio() != null ? coleta.getDescricaoPatrimonio() : 
                          (coleta.getDescricaoItemSemEtiqueta() != null ? coleta.getDescricaoItemSemEtiqueta() : "-");
        String estado = coleta.getEstadoEncontrado() != null ? coleta.getEstadoEncontrado() : "-";
        int id = coleta.getId();
        
        // Adicionar no final (carregamento progressivo - dados já vêm ordenados)
        modeloTabelaHistorico.addRow(new Object[]{data, numero, descricao, estado, id});
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
        String descricao = coleta.getDescricaoPatrimonio() != null ? coleta.getDescricaoPatrimonio() : 
                          (coleta.getDescricaoItemSemEtiqueta() != null ? coleta.getDescricaoItemSemEtiqueta() : "-");
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
            
            // Descrição do patrimônio (itens sem etiqueta já foram filtrados acima)
            String descricao = coleta.getDescricaoPatrimonio() != null ? coleta.getDescricaoPatrimonio() : "-";
            
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

    /**
     * Busca descrições únicas de patrimônios para uso em itens sem patrimônio
     */
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

    // Método buscarItensSemPatrimonio removido para simplificar interface

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

        boolean itemSemEtiqueta = false; // Sempre false - itens sem etiqueta são tratados na aba dedicada

        // Validações específicas para cada tipo de item
        if (itemSemEtiqueta) {
            if (txtDescricaoSemEtiqueta.getText().trim().isEmpty()) {
                System.out.println("DEBUG: Descrição vazia para item sem etiqueta");
                JOptionPane.showMessageDialog(this, "Informe a descrição do item sem etiqueta.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                txtDescricaoSemEtiqueta.requestFocus();
                return;
            }
        } else {
            if (patrimonioSelecionado == null) {
                System.out.println("DEBUG: Nenhum patrimônio selecionado");
                JOptionPane.showMessageDialog(this, "Busque um patrimônio primeiro ou marque 'Item sem etiqueta'.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            System.out.println("DEBUG: Patrimônio selecionado: " + patrimonioSelecionado.getNumero());
        }

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

            if (itemSemEtiqueta) {
                // Configurar para item sem etiqueta
                if (patrimonioSelecionadoDescricao != null) {
                    // Item sem etiqueta vinculado a patrimônio existente
                    coleta.setIdPatrimonio(patrimonioSelecionadoDescricao.getId());
                    coleta.setSemEtiqueta(true);
                    coleta.setDescricaoItemSemEtiqueta(txtDescricaoSemEtiqueta.getText().trim());
                    coleta.setCategoriaItemSemEtiqueta((String) comboCategoriaSemEtiqueta.getSelectedItem());
                    coleta.setLocalizacaoAtual(patrimonioSelecionadoDescricao.getNomeSala());

                    // Verificar divergência de localização
                    if (!salaAtual.getNumeroSala().equals(patrimonioSelecionadoDescricao.getNomeSala())) {
                        coleta.setDivergencia(true);
                        coleta.setMotivoDivergencia("Item sem etiqueta encontrado em sala diferente da registrada");
                    }
                } else {
                    // Item sem etiqueta não vinculado
                    coleta.setIdPatrimonio(0); // ID especial para itens sem etiqueta
                    coleta.setSemEtiqueta(true);
                    coleta.setDescricaoItemSemEtiqueta(txtDescricaoSemEtiqueta.getText().trim());
                    coleta.setCategoriaItemSemEtiqueta((String) comboCategoriaSemEtiqueta.getSelectedItem());
                }
            } else {
                // Configurar para patrimônio normal
                coleta.setIdPatrimonio(patrimonioSelecionado.getId());
                coleta.setNumeroPatrimonio(patrimonioSelecionado.getNumero()); // ✅ ADICIONAR número do patrimônio
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
        txtDescricaoSemEtiqueta.setText("");
        comboCategoriaSemEtiqueta.setSelectedIndex(0);
        campoBuscaDescricao.setText("");
        modeloTabelaResultados.setRowCount(0);
        patrimonioSelecionadoDescricao = null;
        // chkSemEtiqueta removido - checkbox não existe mais
        // alternarModoSemEtiqueta() não é mais necessário // Resetar modo
        limparInformacoesItem();

        // Garantir que o foco volte ao campo de pesquisa
        SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());
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
            dialogItensAgrupados = new JDialog(this, "Itens Sem Etiqueta Agrupados", true);
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

        // Preencher campo de descrição
        txtDescricaoSemEtiqueta.setText(descricaoSelecionada);

        // Fechar dialog se estiver aberto
        if (dialogItensAgrupados != null && dialogItensAgrupados.isVisible()) {
            dialogItensAgrupados.setVisible(false);
        }

        JOptionPane.showMessageDialog(this, "Descrição selecionada: " + descricaoSelecionada,
                "Seleção", JOptionPane.INFORMATION_MESSAGE);
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
            int linhaSelecionada = tabelaHistorico.getSelectedRow();
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
     * Cria um botão estilizado no padrão do MainFrame
     * 
     * @param text      Texto do botão (pode conter emojis)
     * @param baseColor Cor base do botão
     * @return JButton estilizado
     */
    private JButton createStyledButton(String text, Color baseColor) {
        final boolean[] isHovered = {false}; // Mutable wrapper for hover state
        
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                Color hoverColor = baseColor.brighter();
                Color topColor = isHovered[0] ? hoverColor : baseColor;
                Color bottomColor = isHovered[0] ? hoverColor.darker() : baseColor.darker();

                if (!isEnabled()) {
                    topColor = new Color(180, 180, 180);
                    bottomColor = new Color(150, 150, 150);
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2d.dispose();

                // Desenhar texto com suporte a emojis
                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Separar emoji do texto
                String emoji = "";
                String textOnly = text;

                // Detectar emoji no início do texto
                if (text.length() > 0) {
                    int codePoint = text.codePointAt(0);
                    // Verificar se é um emoji (vários ranges Unicode)
                    boolean isEmoji = (codePoint >= 0x1F300 && codePoint <= 0x1F9FF) || // Emojis diversos
                            (codePoint >= 0x2600 && codePoint <= 0x26FF) || // Símbolos diversos
                            (codePoint >= 0x2700 && codePoint <= 0x27BF) || // Dingbats
                            (codePoint >= 0x231A && codePoint <= 0x23FF) || // Símbolos técnicos
                            (codePoint >= 0x2B50 && codePoint <= 0x2BFF); // Estrelas e outros

                    if (isEmoji) {
                        int emojiEnd = Character.charCount(codePoint);
                        emoji = text.substring(0, emojiEnd).trim();
                        if (emojiEnd < text.length()) {
                            textOnly = text.substring(emojiEnd).trim();
                        } else {
                            textOnly = "";
                        }
                    }
                }

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                if (!emoji.isEmpty()) {
                    // Renderizar emoji com fonte especial que suporta emoticons nativamente
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    FontMetrics fmEmoji = g2d.getFontMetrics();
                    int emojiWidth = fmEmoji.stringWidth(emoji);

                    // Renderizar texto ao lado do emoji
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fmText = g2d.getFontMetrics();
                    int textWidth = fmText.stringWidth(textOnly);

                    int totalWidth = emojiWidth + (textOnly.isEmpty() ? 0 : 4 + textWidth);
                    int startX = centerX - (totalWidth / 2);

                    // Desenhar emoji
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    g2d.setColor(isEnabled() ? Color.WHITE : new Color(200, 200, 200));
                    g2d.drawString(emoji, startX, centerY + fmEmoji.getAscent() / 2);

                    // Desenhar texto se existir
                    if (!textOnly.isEmpty()) {
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        int textX = startX + emojiWidth + 4;
                        int textY = centerY + fmText.getAscent() / 2;

                        // Sombra do texto
                        if (isEnabled()) {
                            g2d.setColor(new Color(0, 0, 0, 80));
                            g2d.drawString(textOnly, textX + 1, textY + 1);
                        }

                        // Texto principal
                        g2d.setColor(isEnabled() ? Color.WHITE : new Color(200, 200, 200));
                        g2d.drawString(textOnly, textX, textY);
                    }
                } else {
                    // Sem emoji, renderizar apenas texto centralizado
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textX = centerX - (textWidth / 2);
                    int textY = centerY + fm.getAscent() / 2;

                    // Sombra do texto
                    if (isEnabled()) {
                        g2d.setColor(new Color(0, 0, 0, 80));
                        g2d.drawString(text, textX + 1, textY + 1);
                    }

                    // Texto principal
                    g2d.setColor(isEnabled() ? Color.WHITE : new Color(200, 200, 200));
                    g2d.drawString(text, textX, textY);
                }

                g2d.dispose();
            }

            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                repaint();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 38)); // Altura aumentada de 35 para 38
        button.setMinimumSize(new Dimension(100, 38));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false); // Garantir transparência
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    isHovered[0] = true;
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                isHovered[0] = false;
                button.repaint();
            }
        });

        return button;
    }
}   
