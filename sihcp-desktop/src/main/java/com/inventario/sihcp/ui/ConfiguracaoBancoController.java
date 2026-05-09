package com.inventario.sihcp.ui;

import com.inventario.sihcp.config.DatabaseConfig;
import com.inventario.sihcp.config.DatabaseConfigManager;
import com.inventario.sihcp.SistemaInventarioApplication;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;

/**
 * Controller para a tela de configuração do banco de dados
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ConfiguracaoBancoController extends JFrame {
    
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDatabase;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtSchema;
    private JCheckBox chkSsl;
    private JButton btnTestarConexao;
    private JButton btnSalvar;
    private JButton btnCancelar;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    
    private DatabaseConfigManager configManager;
    private SistemaInventarioApplication mainApp;
    
    public ConfiguracaoBancoController() {
        configManager = new DatabaseConfigManager();
        initializeComponents();
        setupUI();
        loadExistingConfiguration();
    }
    
    private void initializeComponents() {
        setTitle("Configuração do Banco de Dados");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(null);
        
        // Inicializar componentes
        txtHost = new JTextField(30);
        txtPort = new JTextField(15);
        txtDatabase = new JTextField(30);
        txtUsername = new JTextField(30);
        txtPassword = new JPasswordField(30);
        txtSchema = new JTextField(30);
        chkSsl = new JCheckBox("Usar SSL");
        btnTestarConexao = new JButton("Testar Conexão");
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        lblStatus = new JLabel("Preencha os dados de conexão");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
    }
    
    /**
     * Configura a interface do usuário
     */
    private void setupUI() {
        // Configurar validação de porta
        txtPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        
        // Criar layout principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Painel do formulário
        JPanel formPanel = createFormPanel();
        
        // Painel de status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(lblStatus);
        statusPanel.add(progressBar);
        
        // Painel de botões
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Configurar eventos dos botões
        btnTestarConexao.addActionListener(e -> testarConexao());
        btnSalvar.addActionListener(e -> salvarConfiguracao());
        btnCancelar.addActionListener(e -> cancelar());
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Host
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        panel.add(txtHost, gbc);
        
        // Porta
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPort, gbc);
        
        // Database
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Banco de Dados:"), gbc);
        gbc.gridx = 1;
        panel.add(txtDatabase, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);
        
        // Schema
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Schema:"), gbc);
        gbc.gridx = 1;
        panel.add(txtSchema, gbc);
        
        // SSL
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(chkSsl, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(btnTestarConexao);
        panel.add(btnSalvar);
        panel.add(btnCancelar);
        return panel;
    }
    
    /**
     * Carrega configuração existente se houver
     */
    private void loadExistingConfiguration() {
        DatabaseConfig config = configManager.getCurrentConfig();
        if (config != null) {
            txtHost.setText(config.getHost());
            txtPort.setText(String.valueOf(config.getPort()));
            txtDatabase.setText(config.getDatabase());
            txtUsername.setText(config.getUsername());
            txtPassword.setText(config.getPassword());
            txtSchema.setText(config.getSchema());
            chkSsl.setSelected(config.isSsl());
            
            lblStatus.setText("Configuração existente carregada");
        } else {
            // Valores padrão
            txtHost.setText("localhost");
            txtPort.setText("5432");
            txtDatabase.setText("sispatrimonio");
            txtSchema.setText("public");
            chkSsl.setSelected(false);
        }
    }
    
    /**
     * Testa a conexão com o banco de dados
     */
    private void testarConexao() {
        if (!validarCampos()) {
            return;
        }
        
        DatabaseConfig config = criarConfiguracao();
        
        // Desabilitar botões durante o teste
        setButtonsEnabled(false);
        progressBar.setVisible(true);
        lblStatus.setText("Testando conexão...");
        
        // Executar teste em thread separada usando SwingWorker
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return configManager.testConnection(config);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    
                    if (success) {
                        lblStatus.setText("Conexão estabelecida com sucesso!");
                        lblStatus.setForeground(Color.GREEN);
                        showAlert("Sucesso", "Conexão testada com sucesso!\nA conexão com o banco de dados foi estabelecida.", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lblStatus.setText("✗ Falha na conexão");
                        lblStatus.setForeground(Color.RED);
                        showAlert("Erro de Conexão", "Falha ao conectar com o banco de dados.\nVerifique os dados de conexão e tente novamente.", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    lblStatus.setText("✗ Erro no teste de conexão");
                    lblStatus.setForeground(Color.RED);
                    
                    System.err.println("Erro no teste de conexão: " + e.getMessage());
                    showAlert("Erro", "Erro no teste de conexão:\n" + e.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Salva a configuração
     */
    private void salvarConfiguracao() {
        if (!validarCampos()) {
            return;
        }
        
        DatabaseConfig config = criarConfiguracao();
        
        // Desabilitar botões durante o salvamento
        setButtonsEnabled(false);
        progressBar.setVisible(true);
        lblStatus.setText("Salvando configuração...");
        
        // Executar salvamento em thread separada
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return configManager.saveConfiguration(config);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    
                    if (success) {
                        lblStatus.setText("Configuração salva com sucesso!");
                        lblStatus.setForeground(Color.GREEN);
                        showAlert("Sucesso", "Configuração salva!\nA configuração foi salva com sucesso.", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Recarregar configuração na aplicação principal
                        if (mainApp != null) {
                            mainApp.reloadDatabaseConfig();
                            mainApp.showMainScreen();
                        }
                        dispose();
                    } else {
                        lblStatus.setText("✗ Erro ao salvar configuração");
                        lblStatus.setForeground(Color.RED);
                        showAlert("Erro", "Erro ao salvar configuração\nNão foi possível salvar a configuração.", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                    lblStatus.setText("✗ Erro ao salvar configuração");
                    lblStatus.setForeground(Color.RED);
                    
                    System.err.println("Erro ao salvar configuração: " + e.getMessage());
                    showAlert("Erro", "Erro ao salvar configuração:\n" + e.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Cancela a configuração
     */
    private void cancelar() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja cancelar? O sistema será fechado.",
            "Cancelar configuração?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            if (mainApp != null) {
                mainApp.shutdown();
            } else {
                System.exit(0);
            }
        }
    }
    
    /**
     * Valida os campos obrigatórios
     */
    private boolean validarCampos() {
        StringBuilder erros = new StringBuilder();
        
        if (txtHost.getText().trim().isEmpty()) {
            erros.append("- Host é obrigatório\n");
        }
        
        if (txtPort.getText().trim().isEmpty()) {
            erros.append("- Porta é obrigatória\n");
        } else {
            try {
                int port = Integer.parseInt(txtPort.getText().trim());
                if (port <= 0 || port > 65535) {
                    erros.append("- Porta deve estar entre 1 e 65535\n");
                }
            } catch (NumberFormatException e) {
                erros.append("- Porta deve ser um número válido\n");
            }
        }
        
        if (txtDatabase.getText().trim().isEmpty()) {
            erros.append("- Nome do banco é obrigatório\n");
        }
        
        if (txtUsername.getText().trim().isEmpty()) {
            erros.append("- Usuário é obrigatório\n");
        }
        
        if (erros.length() > 0) {
            showAlert("Campos Obrigatórios", "Preencha todos os campos obrigatórios:\n" + erros.toString(), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Cria objeto de configuração com os dados do formulário
     */
    private DatabaseConfig criarConfiguracao() {
        DatabaseConfig config = new DatabaseConfig();
        config.setHost(txtHost.getText().trim());
        config.setPort(Integer.parseInt(txtPort.getText().trim()));
        config.setDatabase(txtDatabase.getText().trim());
        config.setUsername(txtUsername.getText().trim());
        config.setPassword(new String(txtPassword.getPassword()));
        config.setSchema(txtSchema.getText().trim().isEmpty() ? "public" : txtSchema.getText().trim());
        config.setSsl(chkSsl.isSelected());
        
        return config;
    }
    
    /**
     * Habilita/desabilita botões
     */
    private void setButtonsEnabled(boolean enabled) {
        btnTestarConexao.setEnabled(enabled);
        btnSalvar.setEnabled(enabled);
        btnCancelar.setEnabled(enabled);
    }
    
    /**
     * Exibe um alerta
     */
    private void showAlert(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    /**
     * Define a aplicação principal
     */
    public void setMainApp(SistemaInventarioApplication mainApp) {
        this.mainApp = mainApp;
    }
    
    /**
     * Exibe a janela de configuração
     */
    public void showWindow() {
        setVisible(true);
    }
}