package com.inventario.view;

// === SWING: Usar DAOs diretamente (sem Spring) ===
import com.inventario.dao.SalaDAORefactored;
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
import com.inventario.util.SoundNotification;
import com.inventario.view.ui.ModernButtons;
import com.inventario.view.ui.ModernComboBox;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Frame para coleta de patrimônios - Versão 2
 * Interface melhorada com tabela de histórico sempre visível
 */
public class ColetaFrame_v2 extends JFrame {
    private JComboBox<Sala> comboSalas;
    private JTextField campoBusca;
    private JTextArea campoObservacao;
    private JComboBox<String> comboEstado;
    private JButton btnColetar;
    private JButton btnBuscar;

    private JButton btnFinalizarColeta; // Botão para finalizar coleta na sala
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
    private JScrollPane scrollHistorico;

    // Componentes para estrutura de abas
    private JTabbedPane tabbedPane;
    private JPanel abaColetaNormal;
    private JPanel abaItensSemPatrimonio;

    // Campo de localização na aba de itens sem patrimônio
    private JTextField campoLocalizacaoSemPatrimonio;

    // Componentes removidos: busca simplificada para melhor usabilidade

    // Campo de descrição para coleta de itens sem patrimônio
    private JTextField campoDescricaoSemPatrimonio;

    // Novos componentes para a interface moderna de itens sem patrimônio
    private JComboBox<String> comboCategoriaSemPatrimonio;
    private JTextArea areaObservacoesSemPatrimonio;
    private JButton btnRegistrarSemPatrimonio;
    private JButton btnRemoverSemPatrimonio;
    private JButton btnExportarSemPatrimonio;
    private JTable tabelaSemPatrimonio;
    private DefaultTableModel modeloTabelaSemPatrimonio;
    private JLabel lblTotalItensSemPatrimonio;

    // Componentes para pesquisa de descrições únicas (não utilizados na nova interface)
    // Mantidos para compatibilidade, mas não são mais necessários
    //private JButton btnPesquisarDescricoes;
    //private JList<String> listaDescricoesSugestoes;
    //private DefaultListModel<String> modeloListaDescricoes;
    //private JPanel painelSugestoes;
    //private JLabel lblSugestoes;

    // === SWING: DAOs (sem Spring) ===
    private SalaDAORefactored salaDAO;
    private PatrimonioDAO patrimonioDAO;
    private ColetaDAO coletaDAO;
    private InventarioDAO inventarioDAO;
    private SalaInventarioDAO salaInventarioDAO;
    private ParticipanteInventarioDAO participanteInventarioDAO;

    private Patrimonio patrimonioSelecionado;
    private long ultimaDigitacao = 0;
    private StringBuilder bufferCodigoBarras = new StringBuilder();
    private List<Sala> todasSalas = new ArrayList<>();
    private Usuario usuarioLogado; // Usuário logado para verificar permissões
    private Timer filtroTimer; // Timer para controlar a filtragem das salas
    private boolean filtrandoSalas = false; // Flag para controlar se está filtrando salas

    // Lista de componentes que devem ser desabilitados até a seleção da sala
    private java.util.List<Component> componentesParaDesabilitar = new ArrayList<>();

    public ColetaFrame_v2() {
        this(null); // Chama o construtor com usuário null para compatibilidade
    }

    public ColetaFrame_v2(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        // Debug: verificar se o usuário foi passado corretamente
        System.out.println("DEBUG: Usuario logado no ColetaFrame_v2: " +
                (usuarioLogado != null ? usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getPerfil() + ")"
                        : "null"));

        try {
            System.out.println("DEBUG: Iniciando initializeServices()...");
            initializeServices();
            
            System.out.println("DEBUG: Iniciando initializeComponents()...");
            initializeComponents();
            
            System.out.println("DEBUG: Iniciando setupLayout()...");
            setupLayout();
            
            System.out.println("DEBUG: Iniciando setupEventListeners()...");
            setupEventListeners();
            
            System.out.println("DEBUG: Iniciando carregarSalas()...");
            carregarSalas();
            
            System.out.println("DEBUG: Iniciando carregarInventarioAtual()...");
            carregarInventarioAtual();
            
            System.out.println("DEBUG: Iniciando adicionarDicaLeitores()...");
            adicionarDicaLeitores();

            // Inicializar tabela vazia
            // A tabela será carregada quando uma sala for selecionada

            System.out.println("DEBUG: Configurando propriedades do frame...");
            setTitle("Coleta de Patrimônios v2 - Interface Melhorada");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setExtendedState(JFrame.MAXIMIZED_BOTH);

            setLocationRelativeTo(null);
            
            System.out.println("DEBUG: ColetaFrame_v2 inicializado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("ERRO durante inicialização do ColetaFrame_v2:");
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar ColetaFrame_v2: " + e.getMessage(), e);
        }
    }

    private void initializeServices() {
        // === SWING: Inicializar DAOs diretamente ===
        this.salaDAO = new SalaDAORefactored();
        this.patrimonioDAO = new PatrimonioDAO();
        this.coletaDAO = new ColetaDAO();
        this.inventarioDAO = new InventarioDAO();
        this.salaInventarioDAO = new SalaInventarioDAO();
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();
    }

    private void initializeComponents() {
        // Definir cores do tema moderno
        Color corPrimaria = new Color(52, 73, 94); // Azul escuro elegante
        // Color corSecundaria = new Color(236, 240, 241); // Cinza claro
        Color corAcento = new Color(46, 204, 113); // Verde moderno
        // Color corPerigo = new Color(231, 76, 60); // Vermelho moderno
        // Color corAviso = new Color(241, 196, 15); // Amarelo moderno
        Color corTexto = new Color(44, 62, 80); // Texto escuro

        // Combo de salas pesquisável com design moderno
        comboSalas = ModernComboBox.standard(new Sala[0]);
        comboSalas.setEditable(true);
        comboSalas.setPreferredSize(new Dimension(350, 35));
        comboSalas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Sala) {
                    Sala sala = (Sala) value;
                    setText(sala.getNumeroSala());
                }
                return this;
            }
        });

        // Configurar filtro para o combo pesquisável
        JTextField editorCombo = (JTextField) comboSalas.getEditor().getEditorComponent();
        editorCombo.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Permitir navegação normal com setas
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN ||
                        e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) {
                    return;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Só filtrar se não for tecla de navegação
                if (e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN &&
                        e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_TAB &&
                        e.getKeyCode() != KeyEvent.VK_ESCAPE) {

                    // Usar Timer para evitar filtros excessivos
                    if (filtroTimer != null) {
                        filtroTimer.stop();
                    }
                    filtroTimer = new Timer(300, evt -> filtrarSalas(editorCombo.getText()));
                    filtroTimer.setRepeats(false);
                    filtroTimer.start();
                }
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
        btnBuscar = ModernButtons.secondary("Buscar Item");

        // Painel de informações do item pesquisado com design moderno
        panelInfoItem = new JPanel(new GridBagLayout());
        panelInfoItem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "📋 Informações do Item Pesquisado",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)));
        panelInfoItem.setBackground(Color.WHITE);

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

        // Campo de observação com design moderno (reduzido para 2 linhas)
        campoObservacao = new JTextArea(2, 30);
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

        btnBuscarDescricao = ModernButtons.secondary("🔍 Buscar");
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
        btnVerItensAgrupados = ModernButtons.secondary("📊 Ver Itens Sem Etiqueta Agrupados");
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
                        "🔍 Buscar Item Similar por Descrição",
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

        // Botão coletar com design moderno
        btnColetar = ModernButtons.primary("Registrar");
        btnColetar.setEnabled(false);
        btnColetar.setToolTipText("Clique para registrar o item encontrado ou pressione F4");

        // Adicionar atalho F4 para o botão registrar
        btnColetar.setMnemonic(KeyEvent.VK_F4);
        btnColetar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "registrar");
        btnColetar.getActionMap().put("registrar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnColetar.isEnabled()) {
                    registrarItemEncontrado();
                }
            }
        });

        // Botão remover item com design moderno
        btnRemoverItem = ModernButtons.danger("🗑️ Remover Item");
        btnRemoverItem.setEnabled(false);
        btnRemoverItem.setToolTipText("Clique para remover o item selecionado");

        // Controle de visibilidade baseado no perfil do usuário
        // Permitir que todos os usuários logados vejam o botão de remover item
        boolean visivel = usuarioLogado != null;
        System.out.println("DEBUG: btnRemoverItem.setVisible(" + visivel + ") - Usuario: " +
                (usuarioLogado != null ? usuarioLogado.getNomeCompleto() : "null"));
        btnRemoverItem.setVisible(visivel);

        // Botão para finalizar coleta na sala com design moderno
        btnFinalizarColeta = ModernButtons.primary("🏁 Finalizar Coleta da Sala");
        btnFinalizarColeta.setEnabled(false);
        btnFinalizarColeta.setToolTipText("Marcar a coleta desta sala como finalizada");

        // Labels de resumo
        lblResumoSala = new JLabel("Selecione uma sala para iniciar a coleta");
        lblInventarioAtual = new JLabel("Inventário: Carregando...");
        lblInventarioAtual.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblInventarioAtual.setForeground(new Color(0, 100, 0));

        // Tabela de histórico de coleta - CONFIGURAÇÃO MELHORADA
        String[] colunasHistorico = { "Data/Hora", "Patrimônio", "Descrição", "Estado" };
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

        // Criar JScrollPane para a tabela
        scrollHistorico = new JScrollPane(tabelaHistorico);
        // Altura para exibir 3 linhas: altura da linha (25) * 3 + cabeçalho (25) +
        // bordas (10)
        scrollHistorico.setPreferredSize(new Dimension(800, 180)); // Aumentado de 110 para 180
        scrollHistorico.setMinimumSize(new Dimension(600, 110));
        scrollHistorico.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollHistorico.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollHistorico.setBorder(BorderFactory.createLoweredBevelBorder());

        // Adicionar componentes à lista de desabilitação (EXCETO a tabela de histórico)
        componentesParaDesabilitar.add(campoBusca);
        componentesParaDesabilitar.add(btnBuscar);
        componentesParaDesabilitar.add(campoObservacao);
        componentesParaDesabilitar.add(comboEstado);
        componentesParaDesabilitar.add(btnColetar);
        componentesParaDesabilitar.add(btnRemoverItem);

        componentesParaDesabilitar.add(btnFinalizarColeta);

        // Componentes da nova interface de itens sem patrimônio serão controlados por
        // habilitarComponentesPorAba
        // Não adicionamos à lista geral pois são específicos de cada aba

        // Configurar estilo dos botões para manter aparência quando desabilitados
        configurarBotaoComEstiloDesabilitado(btnColetar, new Color(46, 204, 113), new Color(120, 220, 150));
        configurarBotaoComEstiloDesabilitado(btnRemoverItem, new Color(231, 76, 60), new Color(240, 140, 130));

        configurarBotaoComEstiloDesabilitado(btnFinalizarColeta, new Color(40, 167, 69), new Color(120, 200, 140));

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
        Color corSecundaria = new Color(236, 240, 241); // Cinza claro
        Color corAcento = new Color(46, 204, 113); // Verde moderno
        Color corTexto = new Color(44, 62, 80); // Texto escuro

        // Configurar fundo principal da janela
        getContentPane().setBackground(corFundo);

        // Criar estrutura de abas
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
                        "🏢 Local de Coleta e Resumo",
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
        gbcSala.weightx = 0.3;
        gbcSala.fill = GridBagConstraints.HORIZONTAL;
        comboSalas.setPreferredSize(new Dimension(350, 25));
        panelSelecaoSala.add(comboSalas, gbcSala);

        gbcSala.gridx = 2;
        gbcSala.gridy = 1;
        gbcSala.weightx = 0.7;
        gbcSala.insets = new Insets(5, 20, 5, 5);
        panelSelecaoSala.add(lblResumoSala, gbcSala);

        panelSuperior.add(panelSelecaoSala, BorderLayout.CENTER);

        // Panel de busca com design moderno
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "🔍 Busca de Patrimônio",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)));
        panelBusca.setBackground(Color.WHITE);

        JLabel lblBusca = new JLabel("🔍 Buscar patrimônio (número ou código):");
        lblBusca.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBusca.setForeground(corTexto);
        panelBusca.add(lblBusca);
        panelBusca.add(campoBusca);
        panelBusca.add(btnBuscar);

        // Panel de histórico - SEMPRE VISÍVEL com design moderno
        JPanel panelHistorico = new JPanel(new BorderLayout());
        panelHistorico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "📊 Histórico de Coleta da Sala",
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


        panelHistorico.add(scrollHistorico, BorderLayout.CENTER);
        panelHistorico.add(panelBotoesHistorico, BorderLayout.SOUTH);

        // Panel direito - informações e ações com design moderno
        JPanel panelDireito = new JPanel(new BorderLayout());
        panelDireito.setPreferredSize(new Dimension(450, 0));
        panelDireito.setBackground(corFundo);
        panelDireito.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Configurar painel de informações do item
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNumeroLabel = new JLabel("Número:");
        lblNumeroLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNumeroLabel.setForeground(corTexto);
        panelInfoItem.add(lblNumeroLabel, gbc);
        gbc.gridx = 1;
        panelInfoItem.add(lblNumeroItem, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblDescricaoLabel = new JLabel("Descrição:");
        lblDescricaoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDescricaoLabel.setForeground(corTexto);
        panelInfoItem.add(lblDescricaoLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panelInfoItem.add(lblDescricaoItem, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblLocalizacaoLabel = new JLabel("Localização Original:");
        lblLocalizacaoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLocalizacaoLabel.setForeground(corTexto);
        panelInfoItem.add(lblLocalizacaoLabel, gbc);
        gbc.gridx = 1;
        panelInfoItem.add(lblLocalizacaoOriginal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblEstadoOriginalLabel = new JLabel("Estado Original:");
        lblEstadoOriginalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoOriginalLabel.setForeground(corTexto);
        panelInfoItem.add(lblEstadoOriginalLabel, gbc);
        gbc.gridx = 1;
        panelInfoItem.add(lblEstadoOriginal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblStatusLabel = new JLabel("Status:");
        lblStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatusLabel.setForeground(corTexto);
        panelInfoItem.add(lblStatusLabel, gbc);
        gbc.gridx = 1;
        panelInfoItem.add(lblStatusItem, gbc);

        // Panel de ações de coleta com design moderno
        JPanel panelAcoes = new JPanel(new BorderLayout());
        panelAcoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "⚡ Ações de Coleta",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        corTexto),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)));
        panelAcoes.setBackground(Color.WHITE);

        panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Checkbox para item sem etiqueta removido - há aba dedicada para isso

        // Painel para itens sem etiqueta (inicialmente oculto) com design moderno
        panelSemEtiqueta = new JPanel(new GridBagLayout());
        panelSemEtiqueta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                        "📦 Informações do Item Sem Etiqueta",
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
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblObservacoes.setForeground(corTexto);
        panelFormulario.add(lblObservacoes, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        panelFormulario.add(new JScrollPane(campoObservacao), gbc);

        // Painel para os botões de ação - movido para embaixo das observações
        panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelBotoes.setBackground(Color.WHITE);

        panelBotoes.add(btnColetar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panelFormulario.add(panelBotoes, gbc);
        System.out.println("DEBUG: panelBotoes adicionado ao panelFormulario. Bounds: " + panelBotoes.getBounds());

        // Forçar atualização do layout após adicionar o botão
        panelBotoes.revalidate();
        panelBotoes.repaint();
        panelFormulario.revalidate();
        panelFormulario.repaint();

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
        panelDireito.add(panelInfoItem, BorderLayout.CENTER);
        panelDireito.add(panelAcoes, BorderLayout.SOUTH);

        panelCentralPrincipal.add(panelEsquerdo, BorderLayout.CENTER);
        panelCentralPrincipal.add(panelDireito, BorderLayout.EAST);

        // Adicionar componentes à aba de Coleta Normal
        abaColetaNormal.add(panelSuperior, BorderLayout.NORTH);
        abaColetaNormal.add(panelCentralPrincipal, BorderLayout.CENTER);

        // Criar aba para Itens Sem Patrimônio
        abaItensSemPatrimonio = criarAbaItensSemPatrimonio(corFundo, corPrimaria, corSecundaria, corAcento, corTexto);

        // Adicionar abas ao TabbedPane
        tabbedPane.addTab("Coleta Normal", abaColetaNormal);
        tabbedPane.addTab("Itens Sem Patrimônio", abaItensSemPatrimonio);

        // Adicionar listener para controlar habilitação de componentes por aba
        tabbedPane.addChangeListener(e -> {
            int abaSelecionada = tabbedPane.getSelectedIndex();
            habilitarComponentesPorAba(abaSelecionada);
        });

        // Adicionar TabbedPane à janela principal
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel criarAbaItensSemPatrimonio(Color corFundo, Color corPrimaria, Color corSecundaria, Color corAcento,
            Color corTexto) {
        JPanel aba = new JPanel(new BorderLayout(8, 8));
        aba.setBackground(corFundo);
        aba.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== PAINEL SUPERIOR: PESQUISA DE DESCRIÇÕES ==========
        JPanel painelPesquisa = criarPainelPesquisaDescricoes(corFundo, corPrimaria, corTexto);

        // ========== PAINEL CENTRAL: SPLIT ENTRE RESULTADOS E FORMULÁRIO ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.55);

        // Painel esquerdo: Resultados da pesquisa
        JPanel painelResultados = criarPainelResultadosPesquisa(corFundo, corTexto);

        // Painel direito: Formulário de registro
        JPanel painelFormulario = criarPainelFormularioRegistro(corFundo, corAcento, corTexto);

        splitPane.setLeftComponent(painelResultados);
        splitPane.setRightComponent(painelFormulario);

        // ========== PAINEL INFERIOR: TABELA DE ITENS REGISTRADOS ==========
        JPanel painelTabela = criarPainelTabelaItensRegistrados(corFundo, corTexto);

        // ========== MONTAGEM FINAL ==========
        aba.add(painelPesquisa, BorderLayout.NORTH);
        aba.add(splitPane, BorderLayout.CENTER);
        aba.add(painelTabela, BorderLayout.SOUTH);

        return aba;
    }

    /**
     * Cria o painel de pesquisa de descrições
     */
    private JPanel criarPainelPesquisaDescricoes(Color corFundo, Color corPrimaria, Color corTexto) {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        painel.setPreferredSize(new Dimension(0, 110)); // Aumentado para 110

        // Título
        JLabel lblTitulo = new JLabel("🔍 Pesquisar Descrições Existentes");
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

        btnBuscarDescricao = ModernButtons.secondary("🔍 Pesquisar");
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
    private JPanel criarPainelResultadosPesquisa(Color corFundo, Color corTexto) {
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
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(corTexto);

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

        // Instrução
        JLabel lblInstrucao = new JLabel("<html><i>Clique em uma descrição para usá-la no formulário →</i></html>");
        lblInstrucao.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblInstrucao.setForeground(new Color(120, 120, 120));

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(scrollPane, BorderLayout.CENTER);
        painel.add(lblInstrucao, BorderLayout.SOUTH);

        return painel;
    }

    /**
     * Cria o painel do formulário de registro
     */
    private JPanel criarPainelFormularioRegistro(Color corFundo, Color corAcento, Color corTexto) {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Título
        JLabel lblTitulo = new JLabel("✏️ Registrar Item Sem Patrimônio");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        // NOTA: Este campo é preenchido automaticamente com a sala selecionada na aba "Coleta Normal"
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
        campoLocalizacaoSemPatrimonio.setToolTipText("A localização é definida pela sala selecionada na aba 'Coleta Normal'");
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
    private JPanel criarPainelTabelaItensRegistrados(Color corFundo, Color corTexto) {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        painel.setPreferredSize(new Dimension(0, 220));

        // Cabeçalho com título e botões
        JPanel painelCabecalho = new JPanel(new BorderLayout());
        painelCabecalho.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("📊 Itens Sem Patrimônio Registrados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(corTexto);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelBotoes.setBackground(Color.WHITE);

        // Criar botões Registrar e Limpar
        btnRegistrarSemPatrimonio = new JButton("✅ Registrar");
        btnRegistrarSemPatrimonio.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRegistrarSemPatrimonio.setPreferredSize(new Dimension(120, 32));
        btnRegistrarSemPatrimonio.setBackground(new Color(46, 204, 113));
        btnRegistrarSemPatrimonio.setForeground(Color.WHITE);
        btnRegistrarSemPatrimonio.setFocusPainted(false);
        btnRegistrarSemPatrimonio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrarSemPatrimonio.addActionListener(e -> registrarColetaComDescricaoSelecionada());

        JButton btnLimpar = new JButton("🗑️ Limpar");
        btnLimpar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLimpar.setPreferredSize(new Dimension(120, 32));
        btnLimpar.setBackground(new Color(149, 165, 166));
        btnLimpar.setForeground(Color.WHITE);
        btnLimpar.setFocusPainted(false);
        btnLimpar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpar.addActionListener(e -> limparFormularioSemPatrimonio());

        btnRemoverSemPatrimonio = new JButton("❌ Remover");
        btnRemoverSemPatrimonio.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRemoverSemPatrimonio.setPreferredSize(new Dimension(120, 32));
        btnRemoverSemPatrimonio.setBackground(new Color(231, 76, 60));
        btnRemoverSemPatrimonio.setForeground(Color.WHITE);
        btnRemoverSemPatrimonio.setFocusPainted(false);
        btnRemoverSemPatrimonio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemoverSemPatrimonio.setEnabled(false);
        btnRemoverSemPatrimonio.addActionListener(e -> removerItemSemPatrimonioSelecionado());
        
        // Ocultar botão remover para coletores (apenas admin e coordenador podem remover)
        if (usuarioLogado != null) {
            String perfil = usuarioLogado.getPerfil().name();
            btnRemoverSemPatrimonio.setVisible("ADMIN".equals(perfil) || "COORDENADOR".equals(perfil));
        } else {
            btnRemoverSemPatrimonio.setVisible(false);
        }

        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAtualizar.setPreferredSize(new Dimension(120, 32));
        btnAtualizar.setBackground(new Color(52, 152, 219));
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> carregarTodosItensSemPatrimonio());

        // Adicionar botões na ordem: Registrar, Limpar, Remover (se visível), Atualizar
        painelBotoes.add(btnRegistrarSemPatrimonio);
        painelBotoes.add(btnLimpar);
        if (btnRemoverSemPatrimonio.isVisible()) {
            painelBotoes.add(btnRemoverSemPatrimonio);
        }
        painelBotoes.add(btnAtualizar);

        painelCabecalho.add(lblTitulo, BorderLayout.WEST);
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
                // Só habilitar se houver seleção E o botão estiver visível (admin/coordenador)
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
    // mostrarSugestoes e ocultarSugestoes foram removidos pois não são mais necessários
    // na nova interface simplificada

    /**
     * Carrega todos os itens sem patrimônio registrados na sala atual
     */
    private void carregarTodosItensSemPatrimonio() {
        Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
        if (salaSelecionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma sala primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpar tabela
        modeloTabelaSemPatrimonio.setRowCount(0);

        try {
            // Buscar itens sem etiqueta da sala
            List<Coleta> itensSemPatrimonio = coletaDAO.buscarColetasSemEtiquetaPorSala(
                    salaSelecionada.getIdSala(),
                    salaSelecionada.getIdentificacaoCompleta());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (Coleta coleta : itensSemPatrimonio) {
                Object[] linha = {
                        sdf.format(coleta.getDataColeta()),
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

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar itens sem patrimônio: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

            // Verificar se o usuário tem autorização para realizar coletas
            Integer autorizacao = verificarAutorizacaoColeta(inventarioAtivo);

            if (autorizacao == null) {
                JOptionPane.showMessageDialog(this,
                        "Você não está habilitado para realizar coletas neste inventário.",
                        "Acesso Negado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Para admins, buscar ou criar um participante temporário se necessário
            Integer idParticipante = autorizacao;
            if (autorizacao == -1) {
                // Admin: tentar buscar participante existente, senão usar ID 0 para compatibilidade
                try {
                    idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                            inventarioAtivo.getId(), usuarioLogado.getId());
                    if (idParticipante == null) {
                        idParticipante = 0; // Valor especial para admin sem participação formal
                    }
                } catch (Exception e) {
                    idParticipante = 0; // Fallback para admin
                }
            }

            // Criar objeto Coleta para item sem patrimônio
            Coleta coleta = new Coleta();
            coleta.setIdInventario(inventarioAtivo.getId());
            coleta.setIdPatrimonio(0); // Sem patrimônio
            coleta.setIdColetor(usuarioLogado.getId());
            coleta.setIdParticipanteInventario(idParticipante);
            coleta.setDataColeta(new Timestamp(System.currentTimeMillis()));
            coleta.setStatusColeta("COLETADO");
            coleta.setObservacaoColeta(observacoes);
            coleta.setLocalizacaoAtual(salaSelecionada.getIdentificacaoCompleta());
            coleta.setLocalizacaoEncontrada(salaSelecionada.getIdentificacaoCompleta());
            coleta.setEstadoEncontrado("N/A");
            coleta.setDivergencia(false);
            coleta.setSemEtiqueta(true);
            coleta.setDescricaoItemSemEtiqueta(descricao);
            coleta.setCategoriaItemSemEtiqueta("OUTROS"); // Categoria padrão

            // Inserir no banco
            coletaDAO.inserirColeta(coleta);

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

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao registrar item: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

            // Buscar e excluir a coleta
            List<Coleta> itensSemPatrimonio = coletaDAO.buscarColetasSemEtiquetaPorSala(
                    salaSelecionada.getIdSala(),
                    salaSelecionada.getIdentificacaoCompleta());

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

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao remover item: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void setupEventListeners() {
        comboSalas.addActionListener(e -> {
            // Só executar se não estiver filtrando
            if (!filtrandoSalas) {
                carregarDadosSala();
            }
        });
        btnBuscar.addActionListener(e -> buscarPatrimonio());
        campoBusca.addActionListener(e -> buscarPatrimonio());
        btnColetar.addActionListener(e -> registrarItemEncontrado());
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

        // Listener para seleção na tabela de histórico (para habilitar/desabilitar
        // botões)
        tabelaHistorico.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Habilitar botão de finalização se há uma sala selecionada
                Sala salaSelecionada = (Sala) comboSalas.getSelectedItem();
                btnFinalizarColeta.setEnabled(salaSelecionada != null);
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
        } catch (Exception e) {
            lblInventarioAtual.setText("Inventário: Erro ao carregar");
        }
    }

    // Métodos auxiliares (copiados da classe original)
    private void carregarSalas() {
        try {
            todasSalas = salaDAO.listarSalas();
        } catch (Exception e) {
            todasSalas = new ArrayList<>();
            JOptionPane.showMessageDialog(this, "Erro ao carregar salas: " + e.getMessage());
        }
        comboSalas.removeAllItems();
        comboSalas.addItem(null);
        for (Sala sala : todasSalas) {
            comboSalas.addItem(sala);
        }

        if (todasSalas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma sala encontrada no sistema.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void desabilitarComponentes() {
        for (Component comp : componentesParaDesabilitar) {
            comp.setEnabled(false);
        }
    }

    private void habilitarComponentes() {
        for (Component comp : componentesParaDesabilitar) {
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
     * @param abaSelecionada índice da aba selecionada (0 = Coleta Normal, 1 = Itens
     *                       Sem Patrimônio)
     */
    private void habilitarComponentesPorAba(int abaSelecionada) {
        System.out.println("DEBUG: habilitarComponentesPorAba chamado com aba: " + abaSelecionada);
        if (abaSelecionada == 0) {
            // Aba "Coleta Normal" selecionada
            // Habilitar componentes da coleta normal
            campoBusca.setEnabled(true);
            btnBuscar.setEnabled(true);
            System.out.println("DEBUG: Aba 0 - campoBusca e btnBuscar habilitados");
            campoObservacao.setEnabled(true);
            comboEstado.setEnabled(true);

            // Desabilitar componentes específicos da aba "Itens Sem Patrimônio"
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

            // Configurar botão coletar para modo normal
            btnColetar.setText("Registrar");
            btnColetar.setEnabled(patrimonioSelecionado != null);

            // Configurar botão remover para modo normal
            btnRemoverItem.setEnabled(patrimonioSelecionado != null && btnRemoverItem.isVisible());

        } else if (abaSelecionada == 1) {
            // Aba "Itens Sem Patrimônio" selecionada
            // Desabilitar componentes da coleta normal
            campoBusca.setEnabled(false);
            btnBuscar.setEnabled(false);
            System.out.println("DEBUG: Aba 1 - campoBusca e btnBuscar desabilitados");

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
                    "DEBUG: Aba 1 - campoBuscaDescricao, comboCategoriasBusca e btnBuscarDescricao habilitados");
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

        // Revalidar layout
        revalidate();
        repaint();
    }

    private void filtrarSalas(String filtro) {
        if (todasSalas.isEmpty() || filtrandoSalas) {
            return;
        }

        filtrandoSalas = true;

        try {
            // Remover temporariamente o ActionListener para evitar loops
            ActionListener[] listeners = comboSalas.getActionListeners();
            for (ActionListener listener : listeners) {
                comboSalas.removeActionListener(listener);
            }

            comboSalas.removeAllItems();
            comboSalas.addItem(null);

            if (filtro == null || filtro.trim().isEmpty()) {
                for (Sala sala : todasSalas) {
                    comboSalas.addItem(sala);
                }
            } else {
                String filtroLower = filtro.toLowerCase();
                for (Sala sala : todasSalas) {
                    String identificacao = sala.getIdentificacaoCompleta().toLowerCase();
                    if (identificacao.contains(filtroLower)) {
                        comboSalas.addItem(sala);
                    }
                }
            }

            // Restaurar os ActionListeners
            for (ActionListener listener : listeners) {
                comboSalas.addActionListener(listener);
            }

            // Só mostrar popup se há filtro e resultados
            if (filtro != null && !filtro.trim().isEmpty() && comboSalas.getItemCount() > 1) {
                comboSalas.showPopup();
            }

        } finally {
            filtrandoSalas = false;
        }
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

            // Limpar campo de localização na aba de itens sem patrimônio
            if (campoLocalizacaoSemPatrimonio != null) {
                campoLocalizacaoSemPatrimonio.setText("");
            }

            return;
        }
        
        // Atualizar campo de localização na aba de itens sem patrimônio IMEDIATAMENTE
        if (campoLocalizacaoSemPatrimonio != null) {
            campoLocalizacaoSemPatrimonio.setText(salaSelecionada.getIdentificacaoCompleta());
            System.out.println("DEBUG: Campo de localização atualizado para: " + salaSelecionada.getIdentificacaoCompleta());
        }

        try {
            // Habilitar componentes quando uma sala válida é selecionada
            habilitarComponentes();

            carregarHistoricoColeta(salaSelecionada.getIdentificacaoCompleta());
            
            // Carregar itens sem patrimônio se estiver na aba correspondente
            if (tabbedPane.getSelectedIndex() == 1) {
                carregarTodosItensSemPatrimonio();
            }

            // Verificar se a coleta já foi finalizada
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            boolean coletaFinalizada = false;

            if (inventarioAtivo != null) {
                coletaFinalizada = salaInventarioDAO.isColetaFinalizada(salaSelecionada.getIdSala(),
                        inventarioAtivo.getId());
            }

            if (coletaFinalizada) {
                lblResumoSala.setText(
                        String.format("Coletando em: %s [FINALIZADA]", salaSelecionada.getIdentificacaoCompleta()));
                btnFinalizarColeta.setText("Reabrir Coleta da Sala");
                btnFinalizarColeta.setBackground(new Color(255, 193, 7));
            } else {
                lblResumoSala.setText(String.format("Coletando em: %s", salaSelecionada.getIdentificacaoCompleta()));
                btnFinalizarColeta.setText("Finalizar Coleta da Sala");
                btnFinalizarColeta.setBackground(new Color(40, 167, 69));

                // Iniciar coleta se ainda não foi iniciada (registra quem iniciou)
                if (usuarioLogado != null) {
                    // Verificar se o usuário tem autorização para realizar coletas
                    Integer autorizacao = verificarAutorizacaoColeta(inventarioAtivo);

                    if (autorizacao != null) {
                        // Para admins, buscar ou usar participante temporário se necessário
                        Integer idParticipante = autorizacao;
                        if (autorizacao == -1) {
                            // Admin: tentar buscar participante existente, senão usar ID 0 para compatibilidade
                            try {
                                idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                                        inventarioAtivo.getId(), usuarioLogado.getId());
                                if (idParticipante == null) {
                                    idParticipante = 0; // Valor especial para admin sem participação formal
                                }
                            } catch (Exception e) {
                                idParticipante = 0; // Fallback para admin
                            }
                        }

                        salaInventarioDAO.iniciarColeta(salaSelecionada.getIdSala(), inventarioAtivo.getId(),
                                idParticipante);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Usuário não é participante ativo deste inventário.",
                                "Acesso Negado", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }

            // Habilitar componentes baseado na aba atualmente selecionada
            int abaSelecionada = tabbedPane.getSelectedIndex();
            habilitarComponentesPorAba(abaSelecionada);
            btnFinalizarColeta.setEnabled(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados da sala: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarHistoricoColeta(String localizacaoEncontrada) {
        // Limpar dados anteriores
        modeloTabelaHistorico.setRowCount(0);

        try {
            // Buscar APENAS itens COM etiqueta coletados na localização especificada
            List<Coleta> coletasNaLocalizacao = coletaDAO.buscarColetasComEtiquetaPorLocalizacaoEncontrada(localizacaoEncontrada);

            for (Coleta coleta : coletasNaLocalizacao) {
                Object[] linha = {
                        coleta.getDataColetaFormatada(),
                        coleta.getNumeroPatrimonio(),
                        coleta.getDescricaoPatrimonio(),
                        coleta.getEstadoEncontrado()
                };
                modeloTabelaHistorico.addRow(linha);
            }

            // Forçar atualização da tabela
            tabelaHistorico.revalidate();
            tabelaHistorico.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPatrimonio() {
        System.out.println("DEBUG: buscarPatrimonio() chamado");
        String termoBusca = campoBusca.getText().trim();
        System.out.println("DEBUG: Termo de busca: '" + termoBusca + "'");

        if (termoBusca.isEmpty()) {
            System.out.println("DEBUG: Termo de busca vazio, limpando informações");
            limparInformacoesItem();
            return;
        }

        try {
            Patrimonio patrimonioEncontrado = patrimonioDAO.buscarPorNumero(termoBusca);
            Optional<Patrimonio> patrimonioOpt = Optional.ofNullable(patrimonioEncontrado);
            List<Patrimonio> patrimonios = new ArrayList<>();
            if (patrimonioOpt.isPresent()) {
                patrimonios.add(patrimonioOpt.get());
            }

            if (!patrimonios.isEmpty()) {
                Patrimonio patrimonio = patrimonios.stream()
                        .filter(p -> p.getNumero().equalsIgnoreCase(termoBusca))
                        .findFirst()
                        .orElse(patrimonios.get(0));

                exibirInformacoesItem(patrimonio);
                patrimonioSelecionado = patrimonio;
                btnColetar.setEnabled(comboSalas.getSelectedItem() != null);
                btnRemoverItem.setEnabled(patrimonioSelecionado != null && btnRemoverItem.isVisible());

                // Manter foco no campo de pesquisa
                SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());
            } else {
                limparInformacoesItem();
                JOptionPane.showMessageDialog(this,
                        "Patrimônio não encontrado: " + termoBusca,
                        "Item não encontrado", JOptionPane.WARNING_MESSAGE);

                // Manter foco no campo de pesquisa
                SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao buscar patrimônio: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exibirInformacoesItem(Patrimonio patrimonio) {
        lblNumeroItem.setText(patrimonio.getNumero());
        lblDescricaoItem.setText(patrimonio.getDescricao());
        lblLocalizacaoOriginal.setText(patrimonio.getNomeSala());
        lblEstadoOriginal.setText(patrimonio.getEstadoConservacao());

        comboEstado.setSelectedItem(patrimonio.getEstadoConservacao());
    }

    private void limparInformacoesItem() {
        lblNumeroItem.setText("-");
        lblDescricaoItem.setText("-");
        lblLocalizacaoOriginal.setText("-");
        lblEstadoOriginal.setText("-");
        patrimonioSelecionado = null;

        // Só desabilitar o botão se não estiver no modo sem etiqueta
        // Sempre buscar patrimônio já que não há mais checkbox (há aba dedicada para
        // itens sem etiqueta)
        if (true) {
            btnColetar.setEnabled(false);
            btnRemoverItem.setEnabled(false);
        }
    }

    /**
     * Busca descrições únicas de patrimônios para uso em itens sem patrimônio
     */
    /**
     * Busca descrições de patrimônios, mostrando apenas os pendentes (não coletados)
     * Facilita o trabalho do coletor ao exibir somente itens que ainda precisam ser coletados
     */
    private void buscarPorDescricao() {
        String termoBusca = campoBuscaDescricao.getText().trim();

        if (termoBusca.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Digite uma descrição para buscar.",
                    "Campo Vazio",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Limpar resultados anteriores
            modeloTabelaResultados.setRowCount(0);

            // Buscar patrimônios por descrição
            List<Patrimonio> patrimonios = patrimonioDAO.buscarPorDescricao(termoBusca);

            if (patrimonios == null || patrimonios.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Nenhuma descrição encontrada com o termo: \"" + termoBusca + "\"\n\n" +
                                "💡 Dica: Tente usar palavras-chave mais genéricas.",
                        "Sem Resultados",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
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
                } catch (Exception e) {
                    // Em caso de erro, incluir o patrimônio
                    patrimoniosPendentes.add(p);
                }
            }

            if (patrimoniosPendentes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "✅ Todos os patrimônios com essa descrição já foram coletados!\n\n" +
                                "Não há itens pendentes para: \"" + termoBusca + "\"",
                        "Coleta Completa",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
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
                modeloTabelaResultados.addRow(linha);
            }

            // Calcular estatísticas diretamente
            int totalEncontrados = patrimonios.size();
            int totalPendentes = patrimoniosPendentes.size();
            int totalColetados = totalEncontrados - totalPendentes;
            
            double percentualPendente = totalEncontrados > 0 ? (totalPendentes * 100.0 / totalEncontrados) : 0;
            double percentualColetado = totalEncontrados > 0 ? (totalColetados * 100.0 / totalEncontrados) : 0;

            JOptionPane.showMessageDialog(this,
                    String.format("✅ Encontradas %d descrição(ões) com itens pendentes!\n\n" +
                            "📊 Estatísticas:\n" +
                            "   • Total de patrimônios: %d\n" +
                            "   • Pendentes: %d (%.1f%%)\n" +
                            "   • Já coletados: %d (%.1f%%)\n\n" +
                            "💡 Clique em uma linha para usar a descrição no formulário.",
                            descricoesUnicas.size(),
                            totalEncontrados,
                            totalPendentes, percentualPendente,
                            totalColetados, percentualColetado),
                    "Busca Concluída - Apenas Pendentes",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao buscar descrições: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
            String descricaoSelecionada = (String) modeloTabelaResultados.getValueAt(linhaSelecionada, 0);

            // Preencher campo de descrição do formulário
            campoDescricaoSemPatrimonio.setText(descricaoSelecionada);

            // Focar no campo de observações
            areaObservacoesSemPatrimonio.requestFocus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao selecionar descrição: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void registrarItemEncontrado() {
        // Verificar se o usuário tem permissão para realizar coletas
        if (usuarioLogado == null) {
            JOptionPane.showMessageDialog(this,
                    "Usuário não autenticado. Faça login para realizar coletas.",
                    "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Buscar inventário ativo para verificar permissão
        try {
            Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
            if (inventarioAtivo == null) {
                JOptionPane.showMessageDialog(this, "Nenhum inventário ativo encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar se o usuário tem autorização para realizar coletas
            Integer autorizacao = verificarAutorizacaoColeta(inventarioAtivo);

            if (autorizacao == null) {
                JOptionPane.showMessageDialog(this,
                        "Acesso Negado!\n\n" +
                                "Você não está habilitado para realizar coletas neste inventário.\n" +
                                "Entre em contato com o coordenador do inventário para obter as permissões necessárias.",
                        "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Para admins, buscar ou criar um participante temporário se necessário
            Integer idParticipante = autorizacao;
            if (autorizacao == -1) {
                // Admin: tentar buscar participante existente, senão usar ID 0 para compatibilidade
                try {
                    idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                            inventarioAtivo.getId(), usuarioLogado.getId());
                    if (idParticipante == null) {
                        idParticipante = 0; // Valor especial para admin sem participação formal
                    }
                } catch (Exception e) {
                    idParticipante = 0; // Fallback para admin
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao verificar permissões: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma sala primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean itemSemEtiqueta = false; // Sempre false - itens sem etiqueta são tratados na aba dedicada

        // Validações específicas para cada tipo de item
        if (itemSemEtiqueta) {
            if (txtDescricaoSemEtiqueta.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe a descrição do item sem etiqueta.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                txtDescricaoSemEtiqueta.requestFocus();
                return;
            }
        } else {
            if (patrimonioSelecionado == null) {
                JOptionPane.showMessageDialog(this, "Busque um patrimônio primeiro ou marque 'Item sem etiqueta'.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
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
            // Verificar autorização e buscar ID do participante baseado no usuário logado
            if (usuarioLogado != null) {
                Integer autorizacao = verificarAutorizacaoColeta(inventarioAtivo);
                if (autorizacao == null) {
                    JOptionPane.showMessageDialog(this,
                            "Você não está habilitado para realizar coletas neste inventário.",
                            "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Integer idParticipante = autorizacao;
                if (autorizacao == -1) {
                    // Admin: tentar buscar participante existente, senão usar ID do usuário
                    try {
                        idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                                inventarioAtivo.getId(), usuarioLogado.getId());
                        if (idParticipante == null) {
                            idParticipante = usuarioLogado.getId(); // Usar ID do usuário admin
                        }
                    } catch (Exception e) {
                        idParticipante = usuarioLogado.getId(); // Fallback para admin
                    }
                }
                coleta.setIdColetor(usuarioLogado.getId());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuário não autenticado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            coleta.setDataColeta(new Timestamp(System.currentTimeMillis()));
            coleta.setStatusColeta("COLETADO");
            coleta.setObservacaoColeta(observacoes);
            coleta.setLocalizacaoEncontrada(salaAtual.getNumeroSala());
            coleta.setEstadoEncontrado(estadoAtual);
            coleta.setDivergencia(false);

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
                coleta.setSemEtiqueta(false);
                coleta.setLocalizacaoAtual(patrimonioSelecionado.getNomeSala());

                // Verificar se o patrimônio já foi coletado neste inventário
                if (coletaDAO.coletaExiste(inventarioAtivo.getId(), patrimonioSelecionado.getId())) {
                    JOptionPane.showMessageDialog(this,
                            "Este patrimônio já foi coletado neste inventário.\n" +
                                    "Número: " + patrimonioSelecionado.getNumero() + "\n" +
                                    "Descrição: " + patrimonioSelecionado.getDescricao(),
                            "Patrimônio já coletado", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Verificar divergência de localização
                if (!salaAtual.getNumeroSala().equals(patrimonioSelecionado.getNomeSala())) {
                    coleta.setDivergencia(true);
                    coleta.setMotivoDivergencia("Item encontrado em sala diferente da registrada");
                }
            }

            // Registrar no banco
            coletaDAO.inserirColeta(coleta);

            // Reproduzir som de sucesso
            SoundNotification.playColetaSalvaSound();

            // Atualizar estatísticas na tabela SALA_INVENTARIO
            try {
                if (inventarioAtivo != null) {
                    // Contar total de itens coletados na sala
                    List<Coleta> coletasNaSala = coletaDAO.buscarColetasPorSala(salaAtual.getIdSala());
                    int totalItens = coletasNaSala.size();
                    int itensSemEtiqueta = (int) coletasNaSala.stream().filter(Coleta::isSemEtiqueta).count();

                    // Atualizar estatísticas
                    salaInventarioDAO.atualizarEstatisticas(salaAtual.getIdSala(), inventarioAtivo.getId(), totalItens,
                            itensSemEtiqueta);
                }
            } catch (Exception e) {
                // Log do erro, mas não interrompe o fluxo principal
                System.err.println("Erro ao atualizar estatísticas: " + e.getMessage());
            }

            // Atualizar tabela de histórico
            carregarHistoricoColeta(salaAtual.getIdentificacaoCompleta());

            // Limpar formulário
            limparFormulario();

            // Manter foco no campo de pesquisa
            SwingUtilities.invokeLater(() -> campoBusca.requestFocusInWindow());

            String mensagem = itemSemEtiqueta ? "Item sem etiqueta registrado com sucesso!"
                    : "Patrimônio registrado com sucesso!";
            JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar item: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
                    SwingUtilities.invokeLater(() -> buscarPatrimonio());
                }
            });
            timer.setRepeats(false);
            timer.start();
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
                // item[0] = DESCRICAO_NORMALIZADA
                // item[1] = QUANTIDADE
                // item[2] = CATEGORIAS
                // item[3] = PRIMEIRA_COLETA
                // item[4] = ULTIMA_COLETA

                Object[] linha = {
                        item[0], // Descrição
                        item[1], // Quantidade
                        item[2] != null ? item[2] : "N/A", // Categorias
                        item[3] != null ? item[3].toString() : "N/A", // Primeira Coleta
                        item[4] != null ? item[4].toString() : "N/A" // Última Coleta
                };
                modeloTabelaItensAgrupados.addRow(linha);
            }

            // Forçar atualização da tabela
            tabelaItensAgrupados.revalidate();
            tabelaItensAgrupados.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar itens agrupados: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

            JPanel panelBotoes = new JPanel(new FlowLayout());
            panelBotoes.add(btnFechar);
            panelDialog.add(panelBotoes, BorderLayout.SOUTH);

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
                "Tem certeza que deseja excluir a coleta do item:\n" +
                        "Patrimônio: " + numeroPatrimonio + "\n" +
                        "Descrição: " + descricao + "\n\n" +
                        "Esta ação não pode ser desfeita!",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Buscar a coleta específica para exclusão
            // Como não temos ID da coleta na tabela, vamos buscar por critérios
            Sala salaAtual = (Sala) comboSalas.getSelectedItem();
            if (salaAtual == null) {
                JOptionPane.showMessageDialog(this, "Erro: Nenhuma sala selecionada.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String dataHora = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 0);
            String estado = (String) modeloTabelaHistorico.getValueAt(linhaSelecionada, 3);

            // Excluir coleta (sem observações)
            boolean sucesso = coletaDAO.excluirColeta(salaAtual.getIdSala(), numeroPatrimonio, dataHora, estado, "");

            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Coleta excluída com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // Atualizar estatísticas na tabela SALA_INVENTARIO
                try {
                    Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                    if (inventarioAtivo != null) {
                        // Contar total de itens coletados na sala após exclusão
                        List<Coleta> coletasNaSala = coletaDAO.buscarColetasPorSala(salaAtual.getIdSala());
                        int totalItens = coletasNaSala.size();
                        int itensSemEtiqueta = (int) coletasNaSala.stream().filter(Coleta::isSemEtiqueta).count();

                        // Atualizar estatísticas
                        salaInventarioDAO.atualizarEstatisticas(salaAtual.getIdSala(), inventarioAtivo.getId(),
                                totalItens, itensSemEtiqueta);
                    }
                } catch (Exception e) {
                    // Log do erro, mas não interrompe o fluxo principal
                    System.err.println("Erro ao atualizar estatísticas após exclusão: " + e.getMessage());
                }

                // Recarregar histórico
                carregarHistoricoColeta(salaAtual.getIdentificacaoCompleta());


            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir coleta. Verifique se a coleta ainda existe.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir coleta: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Remove o item selecionado da coleta atual
     */
    private void removerItemSelecionado() {
        // Verificar se há um patrimônio selecionado
        if (patrimonioSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um item para remover.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar se há uma sala selecionada
        Sala salaAtual = (Sala) comboSalas.getSelectedItem();
        if (salaAtual == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma sala para remover o item.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar remoção
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja remover o item da coleta?\n" +
                        "Patrimônio: " + patrimonioSelecionado.getNumero() + "\n" +
                        "Descrição: " + patrimonioSelecionado.getDescricao() + "\n\n" +
                        "Esta ação não pode ser desfeita!",
                "Confirmar Remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Buscar a coleta mais recente deste patrimônio na sala atual
            List<Coleta> coletasDoItem = coletaDAO.buscarPorPatrimonio(patrimonioSelecionado.getId());
            Coleta coletaParaRemover = null;

            // Para verificar se a coleta é da sala atual, precisamos comparar com o
            // patrimônio selecionado
            // que já está na sala atual
            for (Coleta coleta : coletasDoItem) {
                if (coleta.getIdPatrimonio() == patrimonioSelecionado.getId()) {
                    coletaParaRemover = coleta;
                    break;
                }
            }

            if (coletaParaRemover == null) {
                JOptionPane.showMessageDialog(this, "Este item não foi coletado nesta sala.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Remover a coleta
            boolean sucesso = coletaDAO.excluirColeta(
                    salaAtual.getIdSala(),
                    patrimonioSelecionado.getNumero(),
                    coletaParaRemover.getDataColetaFormatada(),
                    coletaParaRemover.getEstadoEncontrado(),
                    coletaParaRemover.getObservacaoColeta() != null ? coletaParaRemover.getObservacaoColeta() : "");

            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Item removido da coleta com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // Atualizar estatísticas na tabela SALA_INVENTARIO
                try {
                    Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                    if (inventarioAtivo != null) {
                        // Contar total de itens coletados na sala após remoção
                        List<Coleta> coletasNaSala = coletaDAO.buscarColetasPorSala(salaAtual.getIdSala());
                        int totalItens = coletasNaSala.size();
                        int itensSemEtiqueta = (int) coletasNaSala.stream().filter(Coleta::isSemEtiqueta).count();

                        // Atualizar estatísticas
                        salaInventarioDAO.atualizarEstatisticas(salaAtual.getIdSala(), inventarioAtivo.getId(),
                                totalItens, itensSemEtiqueta);
                    }
                } catch (Exception e) {
                    // Log do erro, mas não interrompe o fluxo principal
                    System.err.println("Erro ao atualizar estatísticas após remoção: " + e.getMessage());
                }

                // Recarregar histórico
                carregarHistoricoColeta(salaAtual.getIdentificacaoCompleta());

                // Limpar seleção atual
                limparInformacoesItem();
                campoBusca.setText("");
                campoBusca.requestFocusInWindow();

            } else {
                JOptionPane.showMessageDialog(this, "Erro ao remover item da coleta. Verifique se o item ainda existe.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao remover item: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Finaliza a coleta na sala selecionada
     */
    private void finalizarColetaSala() {
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

            // Verificar se a coleta já foi finalizada para este inventário
            if (salaInventarioDAO.isColetaFinalizada(salaAtual.getIdSala(), inventarioAtivo.getId())) {
                int opcao = JOptionPane.showConfirmDialog(this,
                        "A coleta desta sala já foi finalizada para o inventário atual.\n" +
                                "Deseja reabrir a coleta?",
                        "Coleta Já Finalizada",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (opcao == JOptionPane.YES_OPTION) {
                    // Reabrir coleta
                    salaInventarioDAO.reabrirColeta(salaAtual.getIdSala(), inventarioAtivo.getId());
                    JOptionPane.showMessageDialog(this, "Coleta da sala reaberta com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                    // Atualizar texto do botão
                    btnFinalizarColeta.setText("Finalizar Coleta da Sala");
                    btnFinalizarColeta.setBackground(new Color(40, 167, 69));

                    // Atualizar resumo da sala
                    lblResumoSala.setText(String.format("Coletando em: %s",
                            salaAtual.getIdentificacaoCompleta()));
                }
                return;
            }

            // Solicitar observações opcionais
            String observacoes = JOptionPane.showInputDialog(this,
                    "Observações sobre a finalização da coleta (opcional):",
                    "Finalizar Coleta da Sala",
                    JOptionPane.QUESTION_MESSAGE);

            // Confirmar finalização
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja finalizar a coleta da sala:\n" +
                            salaAtual.getIdentificacaoCompleta() + "\n\n" +
                            "Após finalizar, a sala será marcada como concluída para este inventário.",
                    "Confirmar Finalização",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            // Verificar autorização e finalizar coleta (passando o ID do participante)
            Integer idParticipante = null;
            if (usuarioLogado != null) {
                Integer autorizacao = verificarAutorizacaoColeta(inventarioAtivo);
                if (autorizacao == null) {
                    JOptionPane.showMessageDialog(this,
                            "Você não está habilitado para finalizar coletas neste inventário.",
                            "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                idParticipante = autorizacao;
                if (autorizacao == -1) {
                    // Admin: tentar buscar participante existente, senão usar ID do usuário
                    try {
                        idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                                inventarioAtivo.getId(), usuarioLogado.getId());
                        if (idParticipante == null) {
                            idParticipante = usuarioLogado.getId(); // Usar ID do usuário admin
                        }
                    } catch (Exception e) {
                        idParticipante = usuarioLogado.getId(); // Fallback para admin
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuário não autenticado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            salaInventarioDAO.finalizarColeta(salaAtual.getIdSala(), inventarioAtivo.getId(), idParticipante,
                    observacoes);

            JOptionPane.showMessageDialog(this,
                    "Coleta da sala finalizada com sucesso!\n" +
                            "Sala: " + salaAtual.getIdentificacaoCompleta(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // Atualizar interface
            btnFinalizarColeta.setText("Reabrir Coleta da Sala");
            btnFinalizarColeta.setBackground(new Color(255, 193, 7));

            // Atualizar resumo da sala
            lblResumoSala.setText(String.format("Coletando em: %s [FINALIZADA]",
                    salaAtual.getIdentificacaoCompleta()));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao finalizar coleta: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Determina a categoria de um item sem patrimônio baseado na sua descrição
     * 
     * @param descricao Descrição do item
     * @return Categoria determinada
     */
    private String determinarCategoria(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "NÃO CATEGORIZADO";
        }

        String desc = descricao.toLowerCase().trim();

        // Móveis
        if (desc.contains("mesa") || desc.contains("cadeira") || desc.contains("armário") ||
                desc.contains("estante") || desc.contains("arquivo") || desc.contains("gaveteiro") ||
                desc.contains("sofá") || desc.contains("poltrona") || desc.contains("banco") ||
                desc.contains("prateleira") || desc.contains("balcão") || desc.contains("escrivaninha")) {
            return "MÓVEIS";
        }

        // Equipamentos de Informática
        if (desc.contains("computador") || desc.contains("notebook") || desc.contains("monitor") ||
                desc.contains("teclado") || desc.contains("mouse") || desc.contains("impressora") ||
                desc.contains("scanner") || desc.contains("cpu") || desc.contains("tablet") ||
                desc.contains("servidor") || desc.contains("switch") || desc.contains("roteador") ||
                desc.contains("modem") || desc.contains("webcam") || desc.contains("hd") ||
                desc.contains("pendrive") || desc.contains("ssd")) {
            return "EQUIPAMENTOS DE INFORMÁTICA";
        }

        // Eletrônicos
        if (desc.contains("televisão") || desc.contains("tv") || desc.contains("projetor") ||
                desc.contains("som") || desc.contains("caixa de som") || desc.contains("microfone") ||
                desc.contains("amplificador") || desc.contains("rádio") || desc.contains("telefone") ||
                desc.contains("celular") || desc.contains("smartphone") || desc.contains("câmera") ||
                desc.contains("filmadora") || desc.contains("dvd") || desc.contains("blu-ray")) {
            return "ELETRÔNICOS";
        }

        // Eletrodomésticos
        if (desc.contains("geladeira") || desc.contains("freezer") || desc.contains("micro-ondas") ||
                desc.contains("microondas") || desc.contains("fogão") || desc.contains("forno") ||
                desc.contains("cafeteira") || desc.contains("bebedouro") || desc.contains("purificador") ||
                desc.contains("ventilador") || desc.contains("ar condicionado") || desc.contains("aquecedor")) {
            return "ELETRODOMÉSTICOS";
        }

        // Equipamentos de Laboratório
        if (desc.contains("microscópio") || desc.contains("balança") || desc.contains("estufa") ||
                desc.contains("autoclave") || desc.contains("centrífuga") || desc.contains("pipeta") ||
                desc.contains("béquer") || desc.contains("proveta") || desc.contains("bureta") ||
                desc.contains("reagente") || desc.contains("vidraria") || desc.contains("equipamento laboratorial")) {
            return "EQUIPAMENTOS DE LABORATÓRIO";
        }

        // Ferramentas
        if (desc.contains("furadeira") || desc.contains("parafusadeira") || desc.contains("martelo") ||
                desc.contains("chave") || desc.contains("alicate") || desc.contains("serra") ||
                desc.contains("broca") || desc.contains("ferramenta") || desc.contains("equipamento de manutenção")) {
            return "FERRAMENTAS";
        }

        // Materiais de Escritório
        if (desc.contains("papel") || desc.contains("caneta") || desc.contains("lápis") ||
                desc.contains("grampeador") || desc.contains("perfurador") || desc.contains("pasta") ||
                desc.contains("arquivo") || desc.contains("organizador") || desc.contains("material de escritório")) {
            return "MATERIAIS DE ESCRITÓRIO";
        }

        // Equipamentos de Segurança
        if (desc.contains("extintor") || desc.contains("câmera de segurança") || desc.contains("alarme") ||
                desc.contains("detector") || desc.contains("equipamento de segurança") || desc.contains("epi")) {
            return "EQUIPAMENTOS DE SEGURANÇA";
        }

        // Livros e Material Didático
        if (desc.contains("livro") || desc.contains("apostila") || desc.contains("manual") ||
                desc.contains("revista") || desc.contains("material didático") || desc.contains("bibliografia")) {
            return "LIVROS E MATERIAL DIDÁTICO";
        }

        // Categoria padrão para itens não identificados
        return "OUTROS";
    }

    /**
     * Verifica se o usuário tem permissão para realizar coletas
     * Administradores têm acesso total, outros usuários precisam ser participantes ativos
     * 
     * @param inventarioAtivo O inventário ativo
     * @return ID do participante se autorizado, ou -1 para admin com acesso total, ou null se não autorizado
     */
    private Integer verificarAutorizacaoColeta(Inventario inventarioAtivo) {
        if (usuarioLogado == null) {
            return null;
        }

        // Administradores têm acesso total a todas as funcionalidades
        if ("ADMIN".equals(usuarioLogado.getPerfil().name())) {
            System.out.println("DEBUG: Usuário ADMIN detectado - acesso total autorizado");
            return -1; // Valor especial para indicar acesso de admin
        }

        // Para outros usuários, verificar se são participantes ativos
        try {
            Integer idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                    inventarioAtivo.getId(), usuarioLogado.getId());
            
            System.out.println("DEBUG: Verificação de participante - ID: " + idParticipante);
            return idParticipante;
        } catch (Exception e) {
            System.err.println("Erro ao verificar autorização: " + e.getMessage());
            return null;
        }
    }

}






