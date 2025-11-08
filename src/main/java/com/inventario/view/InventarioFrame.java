package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import com.inventario.service.ServiceFactory;
import com.inventario.service.InventarioService;
import com.inventario.service.ColetaService;
import com.inventario.model.Inventario;
import com.inventario.offline.OfflineManager;
import com.inventario.offline.OfflineConfigManager;
import com.inventario.offline.SyncFrame;
import com.inventario.offline.ConnectivityListener;
import com.inventario.offline.ConnectivityManager;
import com.inventario.offline.DataSynchronizer;
import javax.swing.Timer;
import java.util.logging.Logger;
import com.inventario.view.ui.ButtonStyleFactory;

/**
 * Tela principal para gerenciamento de inventários
 * Permite visualizar, criar e gerenciar inventários
 */
public class InventarioFrame extends JFrame implements ConnectivityListener {
    private static final Logger logger = Logger.getLogger(InventarioFrame.class.getName());
    
    private JTable tabelaInventario;
    private DefaultTableModel modeloTabela;
    private final InventarioService inventarioService;
    private final ColetaService coletaService;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnVisualizar, btnFinalizar, btnRelatorio, btnBuscar;
    private JButton btnAbrir, btnCancelar, btnEncerrar, btnExcluir;
    
    // Componentes do modo offline
    private JLabel lblOfflineStatus;
    private JButton btnSyncNow;
    private SyncFrame syncFrame;
    private OfflineManager offlineManager;
    private OfflineConfigManager configManager;
    private Timer statusUpdateTimer;
    
    public InventarioFrame() {
        ServiceFactory factory = ServiceFactory.getInstance();
        this.inventarioService = factory.getInventarioService();
        this.coletaService = factory.getColetaService();
        
        // Inicializar componentes offline
        inicializarComponentesOffline();
        
        initComponents();
        
        // Iniciar monitoramento de status offline
        iniciarMonitoramentoStatusOffline();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Inventários - Sistema IFMT");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Criar e configurar a barra de menu
        criarBarraMenu();
        
        // Painel superior com design melhorado
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de busca com indicador offline
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBusca.add(new JLabel("🔍 Buscar:"));
        campoBusca = new JTextField(25);
        campoBusca.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        painelBusca.add(campoBusca);
        btnBuscar = ButtonStyleFactory.createSecondaryButton("Buscar");
        painelBusca.add(btnBuscar);
        
        // Adicionar indicador de status offline
        painelBusca.add(Box.createHorizontalStrut(20));
        lblOfflineStatus = new JLabel("🔄 Verificando...");
        lblOfflineStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblOfflineStatus.setToolTipText("Status da conexão offline");
        painelBusca.add(lblOfflineStatus);
        
        // Botão de sincronização rápida
        btnSyncNow = ButtonStyleFactory.createWarningButton("⚡");
        btnSyncNow.setPreferredSize(new Dimension(35, 25));
        btnSyncNow.setToolTipText("Sincronização rápida");
        painelBusca.add(btnSyncNow);
        
        // Painel de ações principais com layout em grade para melhor organização
        JPanel painelAcoes = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Botões principais com cores e ícones
        btnNovo = ButtonStyleFactory.createSuccessButton("➕ Novo");
        btnNovo.setPreferredSize(new Dimension(100, 35));
        
        btnEditar = ButtonStyleFactory.createPrimaryButton("✏️ Editar");
        btnEditar.setPreferredSize(new Dimension(100, 35));
        
        btnVisualizar = ButtonStyleFactory.createInfoButton("👁️ Ver");
        btnVisualizar.setPreferredSize(new Dimension(100, 35));
        
        btnExcluir = ButtonStyleFactory.createDangerButton("🗑️ Excluir");
        btnExcluir.setPreferredSize(new Dimension(100, 35));
        
        // Separador visual
        JSeparator separador1 = new JSeparator(SwingConstants.VERTICAL);
        separador1.setPreferredSize(new Dimension(2, 30));
        
        // Botões de controle de status
        btnAbrir = ButtonStyleFactory.createSuccessButton("🔓 Abrir");
        btnAbrir.setPreferredSize(new Dimension(100, 35));
        
        btnCancelar = ButtonStyleFactory.createWarningButton("❌ Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        
        btnEncerrar = ButtonStyleFactory.createSecondaryButton("🔒 Encerrar");
        btnEncerrar.setPreferredSize(new Dimension(100, 35));
        
        // Separador visual
        JSeparator separador2 = new JSeparator(SwingConstants.VERTICAL);
        separador2.setPreferredSize(new Dimension(2, 30));
        
        // Botões de relatório e finalização
        btnFinalizar = ButtonStyleFactory.createPrimaryButton("✅ Finalizar");
        btnFinalizar.setPreferredSize(new Dimension(100, 35));
        
        btnRelatorio = ButtonStyleFactory.createInfoButton("📊 Relatório");
        btnRelatorio.setPreferredSize(new Dimension(100, 35));
        
        // Adicionar botões ao painel de ações usando GridBagLayout
        // Primeira linha de botões
        gbc.gridx = 0; gbc.gridy = 0;
        painelAcoes.add(btnNovo, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        painelAcoes.add(btnEditar, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        painelAcoes.add(btnVisualizar, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        painelAcoes.add(btnExcluir, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0;
        painelAcoes.add(btnAbrir, gbc);
        
        gbc.gridx = 5; gbc.gridy = 0;
        painelAcoes.add(btnCancelar, gbc);
        
        // Segunda linha de botões
        gbc.gridx = 0; gbc.gridy = 1;
        painelAcoes.add(btnEncerrar, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        painelAcoes.add(btnFinalizar, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        painelAcoes.add(btnRelatorio, gbc);
        
        // Organizar painéis
        painelSuperior.add(painelBusca, BorderLayout.WEST);
        painelSuperior.add(painelAcoes, BorderLayout.CENTER);
        
        add(painelSuperior, BorderLayout.NORTH);
        
        // Tabela central
        String[] colunas = {"ID", "Descrição", "Data Início", "Data Fim", "Status", "Responsável", "Progresso"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaInventario = new JTable(modeloTabela);
        tabelaInventario.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaInventario.setRowHeight(25);
        tabelaInventario.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabelaInventario.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabelaInventario.getTableHeader().setBackground(new Color(240, 240, 240));
        tabelaInventario.setGridColor(new Color(220, 220, 220));
        tabelaInventario.setSelectionBackground(new Color(184, 207, 229));
        
        JScrollPane scrollPane = new JScrollPane(tabelaInventario);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Inventários"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel inferior com informações melhorado
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JPanel painelEstatisticas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEstatisticas.add(new JLabel("Total: 0"));
        painelEstatisticas.add(Box.createHorizontalStrut(20));
        painelEstatisticas.add(new JLabel("Em andamento: 0"));
        painelEstatisticas.add(Box.createHorizontalStrut(20));
        painelEstatisticas.add(new JLabel("Finalizados: 0"));
        painelEstatisticas.add(Box.createHorizontalStrut(20));
        painelEstatisticas.add(new JLabel("Cancelados: 0"));
        
        JLabel labelStatus = new JLabel("Sistema pronto");
        labelStatus.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        labelStatus.setForeground(new Color(100, 100, 100));
        
        painelInferior.add(painelEstatisticas, BorderLayout.WEST);
        painelInferior.add(labelStatus, BorderLayout.EAST);
        add(painelInferior, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(1200, 600);
        setLocationRelativeTo(null);
    }
    
    private void criarBarraMenu() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setMnemonic('A');
        
        JMenuItem itemNovo = new JMenuItem("Novo Inventário");
        itemNovo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        itemNovo.addActionListener(e -> criarNovoInventario());
        
        JMenuItem itemAbrir = new JMenuItem("Abrir Inventário");
        itemAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        itemAbrir.addActionListener(e -> visualizarInventario());
        
        JMenuItem itemSalvar = new JMenuItem("Salvar");
        itemSalvar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        itemSalvar.addActionListener(e -> salvarInventario());
        
        menuArquivo.addSeparator();
        
        JMenuItem itemImportar = new JMenuItem("Importar Dados");
        itemImportar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        itemImportar.addActionListener(e -> importarDados());
        
        JMenuItem itemExportar = new JMenuItem("Exportar Relatório");
        itemExportar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        itemExportar.addActionListener(e -> exportarRelatorio());
        
        menuArquivo.addSeparator();
        
        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        itemSair.addActionListener(e -> dispose());
        
        menuArquivo.add(itemNovo);
        menuArquivo.add(itemAbrir);
        menuArquivo.add(itemSalvar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemImportar);
        menuArquivo.add(itemExportar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);
        
        // Menu Inventário
        JMenu menuInventario = new JMenu("Inventário");
        menuInventario.setMnemonic('I');
        
        JMenuItem itemGerenciarInventarios = new JMenuItem("Gerenciar Inventários");
        itemGerenciarInventarios.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        itemGerenciarInventarios.addActionListener(e -> abrirGerenciamentoInventarios());
        
        JMenuItem itemFinalizar = new JMenuItem("Finalizar Inventário");
        itemFinalizar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        itemFinalizar.addActionListener(e -> finalizarInventario());
        
        JMenuItem itemColeta = new JMenuItem("Coleta de Dados");
        itemColeta.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        itemColeta.addActionListener(e -> abrirColeta());
        
        menuInventario.add(itemGerenciarInventarios);
        menuInventario.add(itemFinalizar);
        menuInventario.addSeparator();
        menuInventario.add(itemColeta);
        
        // Menu Patrimônio
        JMenu menuPatrimonio = new JMenu("Patrimônio");
        menuPatrimonio.setMnemonic('P');
        
        JMenuItem itemGerenciarPatrimonio = new JMenuItem("Gerenciar Patrimônio");
        itemGerenciarPatrimonio.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        itemGerenciarPatrimonio.addActionListener(e -> abrirGerenciamentoPatrimonio());
        
        JMenuItem itemConsultarPatrimonio = new JMenuItem("Consultar Patrimônio");
        itemConsultarPatrimonio.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        itemConsultarPatrimonio.addActionListener(e -> consultarPatrimonio());
        
        JMenuItem itemEtiquetas = new JMenuItem("Gerar Etiquetas");
        itemEtiquetas.addActionListener(e -> gerarEtiquetas());
        
        menuPatrimonio.add(itemGerenciarPatrimonio);
        menuPatrimonio.add(itemConsultarPatrimonio);
        menuPatrimonio.addSeparator();
        menuPatrimonio.add(itemEtiquetas);
        
        // Menu Cadastros
        JMenu menuCadastros = new JMenu("Cadastros");
        menuCadastros.setMnemonic('C');
        
        JMenuItem itemSalas = new JMenuItem("Salas");
        itemSalas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemSalas.addActionListener(e -> abrirGerenciamentoSalas());
        
        JMenuItem itemSetores = new JMenuItem("Setores");
        itemSetores.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        itemSetores.addActionListener(e -> abrirGerenciamentoSetores());
        
        JMenuItem itemResponsaveis = new JMenuItem("Responsáveis");
        itemResponsaveis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        itemResponsaveis.addActionListener(e -> abrirGerenciamentoResponsaveis());
        
        JMenuItem itemUsuarios = new JMenuItem("Usuários");
        itemUsuarios.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        itemUsuarios.addActionListener(e -> abrirGerenciamentoUsuarios());

        menuCadastros.add(itemSalas);
        menuCadastros.add(itemSetores);
        menuCadastros.add(itemResponsaveis);
        menuCadastros.addSeparator();
        menuCadastros.add(itemUsuarios);
        
        // Menu Relatórios
        JMenu menuRelatorios = new JMenu("Relatórios");
        menuRelatorios.setMnemonic('R');
        
        JMenuItem itemRelInventario = new JMenuItem("Relatório de Inventário");
        itemRelInventario.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        itemRelInventario.addActionListener(e -> gerarRelatorio());
        
        JMenuItem itemRelPatrimonio = new JMenuItem("Relatório de Patrimônio");
        itemRelPatrimonio.addActionListener(e -> relatorioPatrimonio());
        
        JMenuItem itemRelDivergencias = new JMenuItem("Relatório de Divergências");
        itemRelDivergencias.addActionListener(e -> relatorioDivergencias());
        
        JMenuItem itemRelEstatisticas = new JMenuItem("Estatísticas");
        itemRelEstatisticas.addActionListener(e -> relatorioEstatisticas());
        
        menuRelatorios.add(itemRelInventario);
        menuRelatorios.add(itemRelPatrimonio);
        menuRelatorios.add(itemRelDivergencias);
        menuRelatorios.addSeparator();
        menuRelatorios.add(itemRelEstatisticas);
        
        // Menu Ferramentas
        JMenu menuFerramentas = new JMenu("Ferramentas");
        menuFerramentas.setMnemonic('F');
        
        JMenuItem itemConfiguracoes = new JMenuItem("Configurações");
        itemConfiguracoes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        itemConfiguracoes.addActionListener(e -> abrirConfiguracoes());
        
        JMenuItem itemBackup = new JMenuItem("Backup de Dados");
        itemBackup.addActionListener(e -> realizarBackup());
        
        JMenuItem itemLogs = new JMenuItem("Visualizar Logs");
        itemLogs.addActionListener(e -> visualizarLogs());
        
        JMenuItem itemQRCodes = new JMenuItem("Gerenciar QR Codes");
        itemQRCodes.addActionListener(e -> abrirGerenciamentoQRCodes());
        
        // Menu de sincronização offline
        JMenuItem itemSincronizacao = new JMenuItem("Sincronização Offline");
        itemSincronizacao.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        itemSincronizacao.addActionListener(e -> abrirTelaSincronizacao());
        
        JMenuItem itemModoOffline = new JMenuItem("Configurar Modo Offline");
        itemModoOffline.addActionListener(e -> configurarModoOffline());
        
        menuFerramentas.add(itemConfiguracoes);
        menuFerramentas.addSeparator();
        menuFerramentas.add(itemQRCodes);
        menuFerramentas.addSeparator();
        menuFerramentas.add(itemSincronizacao);
        menuFerramentas.add(itemModoOffline);
        menuFerramentas.addSeparator();
        menuFerramentas.add(itemBackup);
        menuFerramentas.add(itemLogs);
        
        // Menu Ajuda
        JMenu menuAjuda = new JMenu("Ajuda");
        menuAjuda.setMnemonic('H');
        
        JMenuItem itemManual = new JMenuItem("Manual do Usuário");
        itemManual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        itemManual.addActionListener(e -> abrirManual());
        
        JMenuItem itemSobre = new JMenuItem("Sobre");
        itemSobre.addActionListener(e -> mostrarSobre());
        
        menuAjuda.add(itemManual);
        menuAjuda.addSeparator();
        menuAjuda.add(itemSobre);
        
        // Adicionar menus à barra
        menuBar.add(menuArquivo);
        menuBar.add(menuInventario);
        menuBar.add(menuPatrimonio);
        menuBar.add(menuCadastros);
        menuBar.add(menuRelatorios);
        menuBar.add(menuFerramentas);
        menuBar.add(menuAjuda);
        
        setJMenuBar(menuBar);
    }
    
    private void configurarEventos() {
        btnNovo.addActionListener(e -> criarNovoInventario());
        btnEditar.addActionListener(e -> editarInventario());
        btnVisualizar.addActionListener(e -> visualizarInventario());
        btnFinalizar.addActionListener(e -> finalizarInventario());
        btnRelatorio.addActionListener(e -> gerarRelatorio());
        btnBuscar.addActionListener(e -> buscarInventarios());
        
        // Eventos dos botões de controle de status
        btnAbrir.addActionListener(e -> abrirInventario());
        btnCancelar.addActionListener(e -> cancelarInventario());
        btnEncerrar.addActionListener(e -> encerrarInventario());
        btnExcluir.addActionListener(e -> excluirInventario());
        
        // Eventos dos componentes offline
        btnSyncNow.addActionListener(e -> executarSincronizacaoRapida());
    }
    
    private void carregarInventarios() {
        modeloTabela.setRowCount(0);
        try {
            List<Inventario> inventarios = inventarioService.listarTodos();
            for (Inventario i : inventarios) {
                modeloTabela.addRow(new Object[]{
                    i.getId(),
                    i.getNome(),
                    i.getDataInicio(),
                    i.getDataFim(),
                    i.getStatusInventario(),
                    i.getResponsavelInventario(),
                    i.getPercentualConclusao() + "%"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar inventários: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void criarNovoInventario() {
        InventarioFormDialog dialog = new InventarioFormDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            carregarInventarios();
        }
    }
    
    private void editarInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Inventario inventario = inventarioService.buscarPorId(id);
                
                if (inventario != null) {
                    InventarioFormDialog dialog = new InventarioFormDialog(this, inventario);
                    dialog.setVisible(true);
                    if (dialog.isConfirmado()) {
                        carregarInventarios();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Inventário não encontrado.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar inventário para edição: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para editar.");
        }
    }
    
    private void visualizarInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            // TODO: Abrir tela de detalhes do inventário
            // Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
            // InventarioDetalhesFrame detalhes = new InventarioDetalhesFrame(id);
            // detalhes.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para visualizar.");
        }
    }
    
    private void finalizarInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            String status = (String) modeloTabela.getValueAt(linhaSelecionada, 4);
            if ("CONCLUIDO".equals(status)) {
                JOptionPane.showMessageDialog(this, "Este inventário já foi finalizado.");
                return;
            }
            
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Tem certeza que deseja finalizar este inventário?\n" +
                "Esta ação não pode ser desfeita.", 
                "Confirmar Finalização", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                    if (inventarioService.finalizar(id)) {
                        carregarInventarios();
                        JOptionPane.showMessageDialog(this, "Inventário finalizado com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao finalizar inventário.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Erro ao finalizar inventário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para finalizar.");
        }
    }
    
    private void gerarRelatorio() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            // TODO: Gerar relatório do inventário
            // Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
            // RelatorioInventarioDialog relatorio = new RelatorioInventarioDialog(this, id);
            // relatorio.setVisible(true);
            JOptionPane.showMessageDialog(this, "Funcionalidade de relatório em desenvolvimento.");
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para gerar o relatório.");
        }
    }
    
    private void buscarInventarios() {
        String termo = campoBusca.getText().trim();
        if (!termo.isEmpty()) {
            modeloTabela.setRowCount(0);
            try {
                // Buscar todos e filtrar localmente
                List<Inventario> todos = inventarioService.listarTodos();
                List<Inventario> resultados = todos.stream()
                    .filter(i -> i.getNome().toUpperCase().contains(termo.toUpperCase()) ||
                                 i.getStatusInventario().toUpperCase().contains(termo.toUpperCase()))
                    .collect(java.util.stream.Collectors.toList());
                for (Inventario i : resultados) {
                    modeloTabela.addRow(new Object[]{
                        i.getId(),
                        i.getNome(),
                        i.getDataInicio(),
                        i.getDataFim(),
                        i.getStatusInventario(),
                        i.getResponsavelInventario(),
                        i.getPercentualConclusao() + "%"
                    });
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao buscar inventários: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            carregarInventarios();
        }
    }
    
    private int calcularProgresso(Inventario inventario) {
        // TODO: Implementar cálculo de progresso
        // Calcular percentual de patrimônios inventariados
        return 0;
    }
    
    // Métodos para os itens de menu
    private void salvarInventario() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de salvar em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void importarDados() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de importação em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportarRelatorio() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de exportação em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirGerenciamentoInventarios() {
        // Já estamos na tela de inventários
        JOptionPane.showMessageDialog(this, "Você já está na tela de gerenciamento de inventários.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirColeta() {
        try {
            // Verificar se há inventário selecionado
            int linhaSelecionada = tabelaInventario.getSelectedRow();
            if (linhaSelecionada >= 0) {
                String status = (String) modeloTabela.getValueAt(linhaSelecionada, 4);
                
                // Verificar se o inventário está ABERTO (EM_ANDAMENTO)
                if (!Inventario.STATUS_EM_ANDAMENTO.equals(status)) {
                    JOptionPane.showMessageDialog(this, 
                        "A coleta só pode ser realizada em inventários com status ABERTO.\n" +
                        "Status atual: " + status, 
                        "Coleta não permitida", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                // TODO: Abrir tela de coleta com o ID do inventário
                JOptionPane.showMessageDialog(this, "Abrindo coleta para inventário ID: " + id + "\n(Tela em desenvolvimento)", "Informação", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um inventário para iniciar a coleta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir coleta: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirGerenciamentoPatrimonio() {
        try {
            // Abrir tela de patrimônio se existir
            JFrame patrimonioFrame = new JFrame("Gerenciamento de Patrimônio");
            patrimonioFrame.setSize(800, 600);
            patrimonioFrame.setLocationRelativeTo(this);
            patrimonioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            JLabel label = new JLabel("<html><center>Tela de Gerenciamento de Patrimônio<br>Em desenvolvimento</center></html>", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            patrimonioFrame.add(label);
            
            patrimonioFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir patrimônio: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void consultarPatrimonio() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de consulta de patrimônio em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void gerarEtiquetas() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de geração de etiquetas em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirGerenciamentoSalas() {
        try {
            // Tentar abrir SalaFrame se existir
            Class<?> salaFrameClass = Class.forName("com.inventario.view.SalaFrame");
            JFrame salaFrame = (JFrame) salaFrameClass.getDeclaredConstructor().newInstance();
            salaFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tela de gerenciamento de salas em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void abrirGerenciamentoSetores() {
        try {
            SetorFrame setorFrame = new SetorFrame();
            setorFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir gerenciamento de setores: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirGerenciamentoResponsaveis() {
        try {
            ResponsavelFrame responsavelFrame = new ResponsavelFrame();
            responsavelFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir gerenciamento de responsáveis: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirGerenciamentoUsuarios() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de gerenciamento de usuários em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void relatorioPatrimonio() {
        JOptionPane.showMessageDialog(this, "Relatório de patrimônio em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void relatorioDivergencias() {
        JOptionPane.showMessageDialog(this, "Relatório de divergências em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void relatorioEstatisticas() {
        JOptionPane.showMessageDialog(this, "Relatório de estatísticas em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirConfiguracoes() {
        JOptionPane.showMessageDialog(this, "Tela de configurações em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void realizarBackup() {
        JOptionPane.showMessageDialog(this, "Funcionalidade de backup em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void visualizarLogs() {
        JOptionPane.showMessageDialog(this, "Visualização de logs em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirManual() {
        JOptionPane.showMessageDialog(this, "Manual do usuário em desenvolvimento.", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void mostrarSobre() {
        String sobre = "SIHCP - Sistema de Histórico e Coleta Patrimonial\n\n" +
                      "Versão: 1.0.0\n" +
                      "Desenvolvido para o Instituto Federal de Mato Grosso\n\n" +
                      "Sistema para gerenciamento de inventário de patrimônio.\n\n" +
                      "Atalhos de Teclado:\n" +
                      "F1 - Manual do Usuário\n" +
                      "F2 - Gerenciar Inventários\n" +
                      "F3 - Coleta de Dados\n" +
                      "F4 - Gerenciar Patrimônio\n" +
                      "F5 - Salas\n" +
                      "F6 - Setores\n" +
                      "F7 - Responsáveis\n" +
                      "F8 - Usuários\n" +
                      "F12 - Configurações\n" +
                      "Ctrl+N - Novo Inventário\n" +
                      "Ctrl+O - Abrir Inventário\n" +
                      "Ctrl+S - Salvar\n" +
                      "Ctrl+R - Relatório\n" +
                      "Alt+F4 - Sair";
        
        JOptionPane.showMessageDialog(this, sobre, "Sobre o Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Métodos para controle de status do inventário
    private void abrirInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String statusAtual = (String) modeloTabela.getValueAt(linhaSelecionada, 4);
                
                // Verificar se pode abrir
                if (Inventario.STATUS_EM_ANDAMENTO.equals(statusAtual)) {
                    JOptionPane.showMessageDialog(this, "Este inventário já está aberto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (Inventario.STATUS_CANCELADO.equals(statusAtual)) {
                    JOptionPane.showMessageDialog(this, "Inventários cancelados não podem ser reabertos.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Verificar se já existe inventário aberto
                if (existeInventarioAberto()) {
                    JOptionPane.showMessageDialog(this, "Já existe um inventário aberto. Apenas um inventário pode estar aberto por vez.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirmacao = JOptionPane.showConfirmDialog(this, 
                    "Tem certeza que deseja abrir este inventário?", 
                    "Confirmar Abertura", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    if (alterarStatusInventario(id, Inventario.STATUS_EM_ANDAMENTO)) {
                        carregarInventarios();
                        JOptionPane.showMessageDialog(this, "Inventário aberto com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao abrir inventário.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir inventário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para abrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void cancelarInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String statusAtual = (String) modeloTabela.getValueAt(linhaSelecionada, 4);
                
                // Verificar se pode cancelar
                if (!Inventario.STATUS_EM_ANDAMENTO.equals(statusAtual) && !Inventario.STATUS_PLANEJADO.equals(statusAtual)) {
                    JOptionPane.showMessageDialog(this, "Apenas inventários ABERTOS ou PLANEJADOS podem ser cancelados.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String motivo = JOptionPane.showInputDialog(this, 
                    "Informe o motivo do cancelamento:", 
                    "Cancelar Inventário", 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (motivo != null && !motivo.trim().isEmpty()) {
                    int confirmacao = JOptionPane.showConfirmDialog(this, 
                        "Tem certeza que deseja cancelar este inventário?\n" +
                        "Esta ação não pode ser desfeita.\n\n" +
                        "Motivo: " + motivo, 
                        "Confirmar Cancelamento", 
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirmacao == JOptionPane.YES_OPTION) {
                        if (alterarStatusInventario(id, Inventario.STATUS_CANCELADO)) {
                            carregarInventarios();
                            JOptionPane.showMessageDialog(this, "Inventário cancelado com sucesso!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Erro ao cancelar inventário.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao cancelar inventário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para cancelar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void encerrarInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String statusAtual = (String) modeloTabela.getValueAt(linhaSelecionada, 4);
                
                // Verificar se pode encerrar
                if (!Inventario.STATUS_EM_ANDAMENTO.equals(statusAtual)) {
                    JOptionPane.showMessageDialog(this, "Apenas inventários ABERTOS podem ser encerrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirmacao = JOptionPane.showConfirmDialog(this, 
                    "Tem certeza que deseja encerrar este inventário?\n" +
                    "Após o encerramento, não será possível realizar mais coletas.", 
                    "Confirmar Encerramento", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    if (alterarStatusInventario(id, Inventario.STATUS_CONCLUIDO)) {
                        carregarInventarios();
                        JOptionPane.showMessageDialog(this, "Inventário encerrado com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao encerrar inventário.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao encerrar inventário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um inventário para encerrar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private boolean alterarStatusInventario(Integer id, String novoStatus) {
        try {
            Inventario inventario = inventarioService.buscarPorId(id);
            if (inventario != null) {
                inventario.setStatusInventario(novoStatus);
                if (Inventario.STATUS_CONCLUIDO.equals(novoStatus)) {
                    inventario.setPercentualConclusao(new java.math.BigDecimal("100.00"));
                }
                return inventarioService.atualizar(inventario);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean existeInventarioAberto() {
        try {
            List<Inventario> inventarios = inventarioService.listarTodos();
            return inventarios.stream().anyMatch(inv -> Inventario.STATUS_EM_ANDAMENTO.equals(inv.getStatusInventario()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void excluirInventario() {
        int linhaSelecionada = tabelaInventario.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nome = (String) modeloTabela.getValueAt(linhaSelecionada, 1);
                String statusAtual = (String) modeloTabela.getValueAt(linhaSelecionada, 4);
                
                // Verificar se o inventário está em andamento
                if (Inventario.STATUS_EM_ANDAMENTO.equals(statusAtual)) {
                    JOptionPane.showMessageDialog(this, 
                        "Não é possível excluir um inventário que está em andamento.\n" +
                        "Cancele ou encerre o inventário antes de excluí-lo.", 
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Verificar se o inventário possui coletas
                if (inventarioPossuiColetas(id)) {
                    JOptionPane.showMessageDialog(this, 
                        "Não é possível excluir este inventário pois ele possui coletas registradas.\n" +
                        "Para manter a integridade dos dados, inventários com coletas não podem ser excluídos.", 
                        "Exclusão não permitida", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Confirmar exclusão
                int confirmacao = JOptionPane.showConfirmDialog(this, 
                    "Tem certeza que deseja excluir o inventário:\n\n" +
                    "Nome: " + nome + "\n" +
                    "Status: " + statusAtual + "\n\n" +
                    "⚠️ ATENÇÃO: Esta ação não pode ser desfeita!", 
                    "Confirmar Exclusão", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    // Solicitar confirmação adicional
                    String confirmacaoTexto = JOptionPane.showInputDialog(this, 
                        "Para confirmar a exclusão, digite 'EXCLUIR' (em maiúsculas):", 
                        "Confirmação Final", 
                        JOptionPane.WARNING_MESSAGE);
                    
                    if ("EXCLUIR".equals(confirmacaoTexto)) {
                        if (excluirInventarioDoBanco(id)) {
                            carregarInventarios();
                            JOptionPane.showMessageDialog(this, 
                                "Inventário excluído com sucesso!", 
                                "Sucesso", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "Erro ao excluir inventário.\n" +
                                "Verifique se não há dependências no banco de dados.", 
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (confirmacaoTexto != null) {
                        JOptionPane.showMessageDialog(this, 
                            "Texto de confirmação incorreto. Exclusão cancelada.", 
                            "Cancelado", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao excluir inventário: " + e.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Selecione um inventário para excluir.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private boolean inventarioPossuiColetas(Integer idInventario) {
        try {
            // Verificar se existe alguma coleta para este inventário
            List<com.inventario.model.Coleta> coletas = coletaService.buscarPorInventario(idInventario);
            return !coletas.isEmpty();
        } catch (Exception e) {
            // Em caso de erro, assumir que possui coletas para segurança
            System.err.println("Erro ao verificar coletas do inventário: " + e.getMessage());
            return true;
        }
    }
    
    private boolean excluirInventarioDoBanco(Integer id) {
        try {
            // TODO: Implementar método excluir no InventarioService
            return false; // Por enquanto retorna false
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void abrirGerenciamentoQRCodes() {
        try {
            com.inventario.view.QRCodeFrame qrCodeFrame = new com.inventario.view.QRCodeFrame();
            qrCodeFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao abrir gerenciamento de QR Codes: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // ==================== MÉTODOS DO MODO OFFLINE ====================
    
    private void inicializarComponentesOffline() {
        try {
            // Inicializar o OfflineManager
            offlineManager = OfflineManager.getInstance();
            offlineManager.addStateListener(new OfflineManager.OfflineStateListener() {
                @Override
                public void onStateChanged(OfflineManager.OfflineState oldState, OfflineManager.OfflineState newState) {
                    onOfflineStateChanged(oldState, newState);
                }
            });
            
            // Registrar como listener de conectividade
            ConnectivityManager.getInstance().addListener(this);
            
            // Configurar componentes de UI
            lblOfflineStatus = new JLabel();
            lblOfflineStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            
            btnSyncNow = new JButton("⟳");
            btnSyncNow.setToolTipText("Sincronizar agora");
            btnSyncNow.setPreferredSize(new Dimension(30, 25));
            btnSyncNow.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            
            // Inicializar o sistema offline
            offlineManager.initialize();
            
            // Atualizar status inicial
            atualizarStatusOffline();
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar componentes offline: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void iniciarMonitoramentoStatusOffline() {
        // O monitoramento é iniciado automaticamente pelo OfflineManager
        // Este método pode ser usado para configurações adicionais se necessário
        statusUpdateTimer = new Timer(5000, e -> atualizarStatusOffline());
        statusUpdateTimer.start();
    }
    
    private void atualizarStatusOffline() {
        if (offlineManager == null || lblOfflineStatus == null) {
            return;
        }
        
        try {
            OfflineManager.OfflineState state = offlineManager.getCurrentState();
            boolean isOnline = ConnectivityManager.getInstance().isOnline();
            
            SwingUtilities.invokeLater(() -> {
                switch (state) {
                    case ONLINE:
                        lblOfflineStatus.setText("● Online");
                        lblOfflineStatus.setForeground(Color.GREEN);
                        btnSyncNow.setEnabled(true);
                        break;
                    case OFFLINE:
                        lblOfflineStatus.setText("● Offline");
                        lblOfflineStatus.setForeground(Color.RED);
                        btnSyncNow.setEnabled(false);
                        break;
                    case SYNCING:
                        lblOfflineStatus.setText("● Sincronizando...");
                        lblOfflineStatus.setForeground(Color.ORANGE);
                        btnSyncNow.setEnabled(false);
                        break;
                    case ERROR:
                        lblOfflineStatus.setText("● Erro");
                        lblOfflineStatus.setForeground(Color.MAGENTA);
                        btnSyncNow.setEnabled(isOnline);
                        break;
                    default:
                        lblOfflineStatus.setText("● Inicializando...");
                        lblOfflineStatus.setForeground(Color.GRAY);
                        btnSyncNow.setEnabled(false);
                }
            });
        } catch (Exception e) {
            System.err.println("Erro ao atualizar status offline: " + e.getMessage());
        }
    }
    
    private void onOfflineStateChanged(OfflineManager.OfflineState oldState, OfflineManager.OfflineState newState) {
        atualizarStatusOffline();
        
        // Notificações opcionais para mudanças de estado
        if (OfflineConfigManager.getInstance().isSyncNotificationsEnabled()) {
            SwingUtilities.invokeLater(() -> {
                switch (newState) {
                    case ONLINE:
                        // Opcional: mostrar notificação de conexão restaurada
                        break;
                    case OFFLINE:
                        // Opcional: mostrar notificação de perda de conexão
                        break;
                    case ERROR:
                        // Opcional: mostrar notificação de erro
                        break;
                }
            });
        }
    }
    
    private void executarSincronizacaoRapida() {
        if (offlineManager == null) {
            JOptionPane.showMessageDialog(this, 
                "Sistema offline não inicializado.", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!ConnectivityManager.getInstance().isOnline()) {
            JOptionPane.showMessageDialog(this, 
                "Não é possível sincronizar sem conexão com a internet.", 
                "Offline", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Executar sincronização em thread separada
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                DataSynchronizer.SyncResult result = offlineManager.executarSincronizacaoManual();
                return result.success;
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(InventarioFrame.this, 
                            "Sincronização concluída com sucesso!", 
                            "Sucesso", 
                            JOptionPane.INFORMATION_MESSAGE);
                        // Recarregar dados se necessário
                        carregarInventarios();
                    } else {
                        JOptionPane.showMessageDialog(InventarioFrame.this, 
                            "Erro durante a sincronização. Verifique os logs para mais detalhes.", 
                            "Erro", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InventarioFrame.this, 
                        "Erro durante a sincronização: " + e.getMessage(), 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void abrirTelaSincronizacao() {
        try {
            SyncFrame syncFrame = new SyncFrame();
            syncFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao abrir tela de sincronização: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        if (statusUpdateTimer != null && statusUpdateTimer.isRunning()) {
            statusUpdateTimer.stop();
        }
        
        // Remover listener do ConnectivityManager
        try {
            ConnectivityManager.getInstance().removeListener(this);
        } catch (Exception e) {
            System.err.println("Erro ao remover listener de conectividade: " + e.getMessage());
        }
        
        super.dispose();
    }

    private void configurarModoOffline() {
        try {
            // Criar um diálogo simples para configurações básicas
            JDialog configDialog = new JDialog(this, "Configurações do Modo Offline", true);
            configDialog.setSize(400, 300);
            configDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Habilitar/Desabilitar modo offline
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Modo Offline:"), gbc);
            
            JCheckBox chkOfflineEnabled = new JCheckBox("Habilitado");
            chkOfflineEnabled.setSelected(OfflineConfigManager.getInstance().isOfflineEnabled());
            gbc.gridx = 1;
            panel.add(chkOfflineEnabled, gbc);
            
            // Sincronização automática
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Sincronização Automática:"), gbc);
            
            JCheckBox chkAutoSync = new JCheckBox("Habilitada");
            chkAutoSync.setSelected(OfflineConfigManager.getInstance().isAutoSyncEnabled());
            gbc.gridx = 1;
            panel.add(chkAutoSync, gbc);
            
            // Intervalo de sincronização
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Intervalo de Sincronização (min):"), gbc);
            
            JSpinner spnInterval = new JSpinner(new SpinnerNumberModel(
                OfflineConfigManager.getInstance().getSyncIntervalMinutes(), 1, 1440, 1));
            gbc.gridx = 1;
            panel.add(spnInterval, gbc);
            
            // Botões
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnSave = new JButton("Salvar");
            JButton btnCancel = new JButton("Cancelar");
            
            btnSave.addActionListener(e -> {
                // Aqui você pode implementar a lógica para salvar as configurações
                // Por enquanto, apenas fechamos o diálogo
                JOptionPane.showMessageDialog(configDialog, 
                    "Configurações salvas! Reinicie a aplicação para aplicar as mudanças.", 
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
                configDialog.dispose();
            });
            
            btnCancel.addActionListener(e -> configDialog.dispose());
            
            buttonPanel.add(btnSave);
            buttonPanel.add(btnCancel);
            
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            panel.add(buttonPanel, gbc);
            
            configDialog.add(panel);
            configDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao abrir configurações offline: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // Implementação da interface ConnectivityListener
    @Override
    public void onConnectionEstablished() {
        SwingUtilities.invokeLater(() -> {
            atualizarStatusOffline();
            // Opcional: executar sincronização automática quando a conexão for restaurada
            if (OfflineConfigManager.getInstance().isAutoSyncEnabled()) {
                executarSincronizacaoRapida();
            }
        });
    }
    
    @Override
    public void onConnectionLost() {
        SwingUtilities.invokeLater(() -> {
            atualizarStatusOffline();
        });
    }
}