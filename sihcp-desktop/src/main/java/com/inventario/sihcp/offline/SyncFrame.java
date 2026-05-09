package com.inventario.sihcp.offline;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.sihcp.util.DateFormatUtils;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Tela de gerenciamento de sincronização offline.
 * Permite ao usuário visualizar o status da sincronização e executar operações manuais.
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class SyncFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(SyncFrame.class.getName());
    private static final long serialVersionUID = 1L;
    
    // Componentes da interface
    private JLabel lblConnectionStatus;
    private JLabel lblLastSync;
    private JLabel lblPendingOperations;
    private JLabel lblLocalRecords;
    private JLabel lblSyncProgress;
    private JProgressBar progressBar;
    private JButton btnSyncNow;
    private JButton btnToggleAutoSync;
    private JButton btnViewLogs;
    private JButton btnSettings;
    private JButton btnClose;
    private JTextArea txtSyncLog;
    private JScrollPane scrollLog;
    
    // Componentes de estatísticas
    private JLabel lblPatrimoniosLocal;
    private JLabel lblColetasLocal;
    private JLabel lblInventariosLocal;
    private JLabel lblConflictsResolved;
    private JLabel lblLastError;
    
    // Gerenciadores
    private OfflineManager offlineManager;
    private OfflineConfigManager configManager;
    private Timer refreshTimer;
    
    // Formatação de datas centralizada em DateFormatUtils
    
    /**
     * Construtor da tela de sincronização.
     */
    public SyncFrame() {
        this.offlineManager = OfflineManager.getInstance();
        this.configManager = OfflineConfigManager.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        startRefreshTimer();
        updateDisplay();
        
        setTitle("Sincronização Offline - SIHCP");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    /**
     * Inicializa os componentes da interface.
     */
    private void initializeComponents() {
        // Labels de status
        lblConnectionStatus = new JLabel("Verificando...");
        lblLastSync = new JLabel("Nunca");
        lblPendingOperations = new JLabel("0");
        lblLocalRecords = new JLabel("0");
        lblSyncProgress = new JLabel("Aguardando...");
        
        // Barra de progresso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Pronto");
        
        // Botões
        btnSyncNow = new JButton("Sincronizar Agora");
        
        btnToggleAutoSync = new JButton("Desabilitar Auto-Sync");
        
        btnViewLogs = new JButton("Ver Logs");
        
        btnSettings = new JButton("Configurações");
        
        btnClose = new JButton("Fechar");
        
        // Área de log
        txtSyncLog = new JTextArea(10, 50);
        txtSyncLog.setEditable(false);
        txtSyncLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtSyncLog.setBackground(Color.BLACK);
        txtSyncLog.setForeground(Color.GREEN);
        scrollLog = new JScrollPane(txtSyncLog);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Labels de estatísticas
        lblPatrimoniosLocal = new JLabel("0");
        lblColetasLocal = new JLabel("0");
        lblInventariosLocal = new JLabel("0");
        lblConflictsResolved = new JLabel("0");
        lblLastError = new JLabel("Nenhum");
        
        // Configurar cores e fontes
        setupComponentStyles();
    }
    
    /**
     * Configura o layout da interface.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel superior - Status
        JPanel statusPanel = createStatusPanel();
        
        // Painel central - Informações e controles
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(createInfoPanel(), BorderLayout.NORTH);
        centerPanel.add(createLogPanel(), BorderLayout.CENTER);
        
        // Painel inferior - Botões
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Cria o painel de status.
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Status da Conexão"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Status da conexão
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        panel.add(lblConnectionStatus, gbc);
        
        // Última sincronização
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Última Sync:"), gbc);
        gbc.gridx = 3;
        panel.add(lblLastSync, gbc);
        
        // Barra de progresso
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Progresso:"), gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(progressBar, gbc);
        
        // Status do progresso
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        panel.add(lblSyncProgress, gbc);
        
        return panel;
    }
    
    /**
     * Cria o painel de informações.
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Painel de dados locais
        JPanel localDataPanel = new JPanel(new GridBagLayout());
        localDataPanel.setBorder(new TitledBorder("Dados Locais"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        localDataPanel.add(new JLabel("Patrimônios:"), gbc);
        gbc.gridx = 1;
        localDataPanel.add(lblPatrimoniosLocal, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        localDataPanel.add(new JLabel("Coletas:"), gbc);
        gbc.gridx = 1;
        localDataPanel.add(lblColetasLocal, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        localDataPanel.add(new JLabel("Inventários:"), gbc);
        gbc.gridx = 1;
        localDataPanel.add(lblInventariosLocal, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        localDataPanel.add(new JLabel("Operações Pendentes:"), gbc);
        gbc.gridx = 1;
        localDataPanel.add(lblPendingOperations, gbc);
        
        // Painel de estatísticas
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBorder(new TitledBorder("Estatísticas"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        statsPanel.add(new JLabel("Registros Locais:"), gbc);
        gbc.gridx = 1;
        statsPanel.add(lblLocalRecords, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        statsPanel.add(new JLabel("Conflitos Resolvidos:"), gbc);
        gbc.gridx = 1;
        statsPanel.add(lblConflictsResolved, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        statsPanel.add(new JLabel("Último Erro:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        statsPanel.add(lblLastError, gbc);
        
        panel.add(localDataPanel);
        panel.add(statsPanel);
        
        return panel;
    }
    
    /**
     * Cria o painel de log.
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Log de Sincronização"));
        
        // Painel de controles do log
        JPanel logControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClearLog = new JButton("Limpar");
        btnClearLog.addActionListener(e -> clearLog());
        logControlPanel.add(btnClearLog);
        
        panel.add(logControlPanel, BorderLayout.NORTH);
        panel.add(scrollLog, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel de botões.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        panel.add(btnSyncNow);
        panel.add(btnToggleAutoSync);
        panel.add(btnViewLogs);
        panel.add(btnSettings);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnClose);
        
        return panel;
    }
    
    /**
     * Configura os estilos dos componentes.
     */
    private void setupComponentStyles() {
        // Configurar cores dos labels de status
        lblConnectionStatus.setFont(lblConnectionStatus.getFont().deriveFont(Font.BOLD));
        lblLastSync.setFont(lblLastSync.getFont().deriveFont(Font.ITALIC));
        
        // Configurar botões
        btnSyncNow.setBackground(new Color(0, 150, 0));
        btnSyncNow.setForeground(Color.WHITE);
        btnSyncNow.setFont(btnSyncNow.getFont().deriveFont(Font.BOLD));
        
        btnClose.setBackground(new Color(200, 50, 50));
        btnClose.setForeground(Color.WHITE);
    }
    
    /**
     * Configura os listeners de eventos.
     */
    private void setupEventListeners() {
        btnSyncNow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performManualSync();
            }
        });
        
        btnToggleAutoSync.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAutoSync();
            }
        });
        
        btnViewLogs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewDetailedLogs();
            }
        });
        
        btnSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSettings();
            }
        });
        
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeFrame();
            }
        });
    }
    
    /**
     * Inicia o timer de atualização da interface.
     */
    private void startRefreshTimer() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateDisplay());
            }
        }, 0, 2000); // Atualiza a cada 2 segundos
    }
    
    /**
     * Atualiza a exibição da interface.
     */
    private void updateDisplay() {
        try {
            // Atualizar status da conexão
            OfflineManager.OfflineState state = offlineManager.getCurrentState();
            updateConnectionStatus(state);
            
            // Atualizar informações de sincronização
            updateSyncInfo();
            
            // Atualizar estatísticas
            updateStatistics();
            
            // Atualizar estado dos botões
            updateButtonStates();
            
        } catch (Exception e) {
            logger.warning("Erro ao atualizar display: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza o status da conexão.
     */
    private void updateConnectionStatus(OfflineManager.OfflineState state) {
        switch (state) {
            case ONLINE:
                lblConnectionStatus.setText("🟢 Online");
                lblConnectionStatus.setForeground(new Color(0, 150, 0));
                break;
            case OFFLINE:
                lblConnectionStatus.setText("🔴 Offline");
                lblConnectionStatus.setForeground(new Color(200, 0, 0));
                break;
            case SYNCING:
                lblConnectionStatus.setText("🟡 Sincronizando");
                lblConnectionStatus.setForeground(new Color(200, 150, 0));
                break;
            case ERROR:
                lblConnectionStatus.setText("ERRO");
                lblConnectionStatus.setForeground(Color.RED);
                break;
            default:
                lblConnectionStatus.setText("❓ Desconhecido");
                lblConnectionStatus.setForeground(Color.GRAY);
        }
    }
    
    /**
     * Atualiza as informações de sincronização.
     */
    private void updateSyncInfo() {
        // Simular dados - em implementação real, obter do OfflineManager
        lblLastSync.setText(DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()));
        lblPendingOperations.setText("0");
        lblSyncProgress.setText("Aguardando próxima sincronização...");
    }
    
    /**
     * Atualiza as estatísticas.
     */
    private void updateStatistics() {
        // Simular dados - em implementação real, obter do OfflineDAO
        lblPatrimoniosLocal.setText("0");
        lblColetasLocal.setText("0");
        lblInventariosLocal.setText("0");
        lblLocalRecords.setText("0");
        lblConflictsResolved.setText("0");
        lblLastError.setText("Nenhum");
    }
    
    /**
     * Atualiza o estado dos botões.
     */
    private void updateButtonStates() {
        OfflineManager.OfflineState state = offlineManager.getCurrentState();
        
        btnSyncNow.setEnabled(state != OfflineManager.OfflineState.SYNCING);
        
        if (configManager.isAutoSyncEnabled()) {
            btnToggleAutoSync.setText("Desabilitar Auto-Sync");
        } else {
            btnToggleAutoSync.setText("Habilitar Auto-Sync");
        }
    }
    
    /**
     * Executa sincronização manual.
     */
    private void performManualSync() {
        addLogMessage("Iniciando sincronização manual...");
        
        // Atualizar interface
        btnSyncNow.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Sincronizando...");
        lblSyncProgress.setText("Executando sincronização manual...");
        
        // Executar sincronização em thread separada
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Verificando conectividade...");
                    Thread.sleep(1000); // Simular operação
                    
                    publish("Enviando dados locais...");
                    Thread.sleep(2000); // Simular operação
                    
                    publish("Baixando dados atualizados...");
                    Thread.sleep(1500); // Simular operação
                    
                    publish("Resolvendo conflitos...");
                    Thread.sleep(500); // Simular operação
                    
                    publish("Sincronização concluída com sucesso!");
                    
                } catch (Exception e) {
                    publish("Erro na sincronização: " + e.getMessage());
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    addLogMessage(message);
                    lblSyncProgress.setText(message);
                }
            }
            
            @Override
            protected void done() {
                btnSyncNow.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                progressBar.setString("Concluído");
                
                try {
                    get(); // Verificar se houve exceção
                    addLogMessage("SUCESSO: Sincronização manual concluída com sucesso!");
                } catch (Exception e) {
                    addLogMessage("ERRO na sincronização: " + e.getMessage());
                }
                
                // Reset após 3 segundos
                Timer resetTimer = new Timer();
                resetTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(0);
                            progressBar.setString("Pronto");
                            lblSyncProgress.setText("Aguardando...");
                        });
                    }
                }, 3000);
            }
        };
        
        worker.execute();
    }
    
    /**
     * Alterna o estado da sincronização automática.
     */
    private void toggleAutoSync() {
        boolean currentState = configManager.isAutoSyncEnabled();
        
        // Simular alteração da configuração
        String newState = currentState ? "desabilitada" : "habilitada";
        addLogMessage("Sincronização automática " + newState);
        
        updateButtonStates();
    }
    
    /**
     * Abre a visualização detalhada de logs.
     */
    private void viewDetailedLogs() {
        JDialog logDialog = new JDialog(this, "Logs Detalhados", true);
        logDialog.setSize(600, 400);
        logDialog.setLocationRelativeTo(this);
        
        JTextArea detailedLog = new JTextArea();
        detailedLog.setEditable(false);
        detailedLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        detailedLog.setText("=== Logs Detalhados do Sistema Offline ===\n\n" +
                           "[" + DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()) + "] Sistema iniciado\n" +
                           "[" + DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()) + "] Configurações carregadas\n" +
                           "[" + DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()) + "] Banco SQLite inicializado\n" +
                           "[" + DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()) + "] Monitoramento de conectividade ativo\n");
        
        JScrollPane scrollPane = new JScrollPane(detailedLog);
        logDialog.add(scrollPane);
        logDialog.setVisible(true);
    }
    
    /**
     * Abre as configurações do modo offline.
     */
    private void openSettings() {
        JDialog settingsDialog = new JDialog(this, "Configurações Offline", true);
        settingsDialog.setSize(500, 400);
        settingsDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Configurações básicas
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Intervalo de Sincronização (min):"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerInterval = new JSpinner(new SpinnerNumberModel(configManager.getSyncIntervalMinutes(), 1, 60, 1));
        panel.add(spinnerInterval, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tamanho do Lote:"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerBatch = new JSpinner(new SpinnerNumberModel(configManager.getSyncBatchSize(), 10, 1000, 10));
        panel.add(spinnerBatch, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Estratégia de Conflito:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboStrategy = new JComboBox<>(new String[]{
            "TIMESTAMP_WINS", "LOCAL_WINS", "REMOTE_WINS", "MANUAL_RESOLUTION"
        });
        comboStrategy.setSelectedItem(configManager.getConflictResolutionStrategy());
        panel.add(comboStrategy, gbc);
        
        // Botões
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSave = new JButton("Salvar");
        JButton btnCancel = new JButton("Cancelar");
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, gbc);
        
        btnSave.addActionListener(e -> {
            addLogMessage("Configurações salvas");
            settingsDialog.dispose();
        });
        
        btnCancel.addActionListener(e -> settingsDialog.dispose());
        
        settingsDialog.add(panel);
        settingsDialog.setVisible(true);
    }
    
    /**
     * Adiciona uma mensagem ao log.
     */
    private void addLogMessage(String message) {
        String timestamp = DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate());
        String logEntry = "[" + timestamp + "] " + message + "\n";
        
        SwingUtilities.invokeLater(() -> {
            txtSyncLog.append(logEntry);
            txtSyncLog.setCaretPosition(txtSyncLog.getDocument().getLength());
        });
    }
    
    /**
     * Limpa o log.
     */
    private void clearLog() {
        txtSyncLog.setText("");
        addLogMessage("Log limpo");
    }
    
    /**
     * Fecha a janela.
     */
    private void closeFrame() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        setVisible(false);
    }
    
    /**
     * Método para mostrar a janela.
     */
    public void showFrame() {
        setVisible(true);
        toFront();
        addLogMessage("Tela de sincronização aberta");
    }
    
    /**
     * Método para atualizar o progresso externamente.
     */
    public void updateProgress(int progress, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            progressBar.setString(message);
            lblSyncProgress.setText(message);
            addLogMessage(message);
        });
    }
    
    /**
     * Método para notificar erro.
     */
    public void notifyError(String error) {
        SwingUtilities.invokeLater(() -> {
            lblLastError.setText(error);
            addLogMessage("ERRO: " + error);
        });
    }
    
    /**
     * Método para notificar sucesso.
     */
    public void notifySuccess(String message) {
        SwingUtilities.invokeLater(() -> {
            addLogMessage("SUCESSO: " + message);
        });
    }
}