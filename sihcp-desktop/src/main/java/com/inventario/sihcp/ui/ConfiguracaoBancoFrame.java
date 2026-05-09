package com.inventario.sihcp.ui;

import com.inventario.sihcp.config.DatabaseConfig;
import com.inventario.sihcp.config.DatabaseConfigManager;
import com.inventario.sihcp.SistemaInventarioApplication;
// Removido SLF4J para simplificar dependências

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame para configuração do banco de dados
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ConfiguracaoBancoFrame extends JFrame {
    
    // Logger removido para simplificar dependências
    
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
    
    public ConfiguracaoBancoFrame(SistemaInventarioApplication mainApp) {
        this.mainApp = mainApp;
        this.configManager = mainApp.getConfigManager();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadExistingConfiguration();
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Configuração do Banco de Dados - SIHCP");
        setSize(700, 580);
        setResizable(false);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Campos com tamanho maior para melhor visualização
        txtHost = new JTextField("localhost", 35);
        txtHost.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtPort = new JTextField("5432", 20);
        txtPort.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtDatabase = new JTextField(35);
        txtDatabase.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtUsername = new JTextField(35);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtPassword = new JPasswordField(35);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtSchema = new JTextField("public", 35);
        txtSchema.setFont(new Font("Arial", Font.PLAIN, 14));
        chkSsl = new JCheckBox("Usar SSL");
        chkSsl.setFont(new Font("Arial", Font.PLAIN, 13));
        
        btnTestarConexao = new JButton("Testar Conexão");
        btnTestarConexao.setFont(new Font("Arial", Font.BOLD, 13));
        btnTestarConexao.setPreferredSize(new Dimension(150, 38));
        
        btnSalvar = new JButton("Salvar");
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 13));
        btnSalvar.setPreferredSize(new Dimension(120, 38));
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 13));
        btnCancelar.setPreferredSize(new Dimension(120, 38));
        
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.BLUE);
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel titleLabel = new JLabel("Configuração do Banco de Dados PostgreSQL");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Painel de formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        Font labelFont = new Font("Arial", Font.BOLD, 13);
        
        // Host
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblHost = new JLabel("Host:");
        lblHost.setFont(labelFont);
        formPanel.add(lblHost, gbc);
        gbc.gridx = 1;
        formPanel.add(txtHost, gbc);
        
        // Port
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPort = new JLabel("Porta:");
        lblPort.setFont(labelFont);
        formPanel.add(lblPort, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPort, gbc);
        
        // Database
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblDatabase = new JLabel("Banco de Dados:");
        lblDatabase.setFont(labelFont);
        formPanel.add(lblDatabase, gbc);
        gbc.gridx = 1;
        formPanel.add(txtDatabase, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblUsername = new JLabel("Usuário:");
        lblUsername.setFont(labelFont);
        formPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblPassword = new JLabel("Senha:");
        lblPassword.setFont(labelFont);
        formPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);
        
        // Schema
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblSchema = new JLabel("Schema:");
        lblSchema.setFont(labelFont);
        formPanel.add(lblSchema, gbc);
        gbc.gridx = 1;
        formPanel.add(txtSchema, gbc);
        
        // SSL
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(chkSsl, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Painel de status e progresso
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(lblStatus, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.SOUTH);
        
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnTestarConexao);
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnCancelar);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        btnTestarConexao.addActionListener(this::testarConexao);
        btnSalvar.addActionListener(this::salvarConfiguracao);
        btnCancelar.addActionListener(this::cancelar);
        
        // Evento de fechamento da janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Se não há configuração válida, fechar o sistema
                DatabaseConfig config = configManager.getCurrentConfig();
                if (config == null || !configManager.testConnection(config)) {
                    int option = JOptionPane.showConfirmDialog(
                        ConfiguracaoBancoFrame.this,
                        "Não há configuração válida do banco de dados.\nDeseja sair do sistema?",
                        "Confirmar Saída",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        if (mainApp != null) {
                            mainApp.shutdown();
                        } else {
                            System.exit(0);
                        }
                    }
                } else {
                    mainApp.showMainScreen();
                }
            }
        });
    }
    
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
        }
    }
    
    private void testarConexao(ActionEvent e) {
        if (!validarCampos()) {
            return;
        }
        
        DatabaseConfig config = criarConfiguracao();
        
        // Executar teste em thread separada
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                setUITesting(true);
                return configManager.testConnection(config);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        lblStatus.setForeground(Color.GREEN);
                        lblStatus.setText("Conexão realizada com sucesso!");
                        btnSalvar.setEnabled(true);
                    } else {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("✗ Falha na conexão. Verifique os dados.");
                    }
                } catch (Exception ex) {
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("✗ Erro: " + ex.getMessage());
                    System.err.println("Erro ao testar conexão: " + ex.getMessage());
                } finally {
                    setUITesting(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void salvarConfiguracao(ActionEvent e) {
        if (!validarCampos()) {
            return;
        }
        
        DatabaseConfig config = criarConfiguracao();
        
        // Executar salvamento em thread separada
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    btnSalvar.setEnabled(false);
                    btnCancelar.setEnabled(false);
                    progressBar.setVisible(true);
                    lblStatus.setForeground(Color.BLUE);
                    lblStatus.setText("Salvando configuração...");
                });
                
                configManager.saveConfiguration(config);
                return true;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Verifica se houve exceção
                    
                    lblStatus.setForeground(Color.GREEN);
                    lblStatus.setText("Configuração salva com sucesso!");
                    
                    JOptionPane.showMessageDialog(
                        ConfiguracaoBancoFrame.this,
                        "Configuração salva com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Recarregar configuração e ir para tela principal
                    if (mainApp != null) {
                        mainApp.reloadDatabaseConfig();
                        mainApp.showMainScreen();
                    } else {
                        dispose();
                    }
                    
                } catch (Exception ex) {
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("✗ Erro ao salvar: " + ex.getMessage());
                    
                    JOptionPane.showMessageDialog(
                        ConfiguracaoBancoFrame.this,
                        "Erro ao salvar configuração: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                    );
                    System.err.println("Erro ao salvar configuração: " + ex.getMessage());
                } finally {
                    btnSalvar.setEnabled(true);
                    btnCancelar.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void cancelar(ActionEvent e) {
        // Verificar se há configuração válida
        DatabaseConfig config = configManager.getCurrentConfig();
        if (config != null && configManager.testConnection(config)) {
            mainApp.showMainScreen();
        } else {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Não há configuração válida do banco de dados.\nDeseja sair do sistema?",
                "Confirmar Saída",
                JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                if (mainApp != null) {
                    mainApp.shutdown();
                } else {
                    System.exit(0);
                }
            }
        }
    }
    
    private boolean validarCampos() {
        if (txtHost.getText().trim().isEmpty()) {
            showValidationError("Host é obrigatório");
            txtHost.requestFocus();
            return false;
        }
        
        if (txtPort.getText().trim().isEmpty()) {
            showValidationError("Porta é obrigatória");
            txtPort.requestFocus();
            return false;
        }
        
        try {
            Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            showValidationError("Porta deve ser um número válido");
            txtPort.requestFocus();
            return false;
        }
        
        if (txtDatabase.getText().trim().isEmpty()) {
            showValidationError("Nome do banco de dados é obrigatório");
            txtDatabase.requestFocus();
            return false;
        }
        
        if (txtUsername.getText().trim().isEmpty()) {
            showValidationError("Usuário é obrigatório");
            txtUsername.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Erro de Validação",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    private DatabaseConfig criarConfiguracao() {
        DatabaseConfig config = new DatabaseConfig();
        config.setHost(txtHost.getText().trim());
        config.setPort(Integer.parseInt(txtPort.getText().trim()));
        config.setDatabase(txtDatabase.getText().trim());
        config.setUsername(txtUsername.getText().trim());
        config.setPassword(new String(txtPassword.getPassword()));
        config.setSchema(txtSchema.getText().trim());
        config.setSsl(chkSsl.isSelected());
        return config;
    }
    
    private void setUITesting(boolean testing) {
        SwingUtilities.invokeLater(() -> {
            btnTestarConexao.setEnabled(!testing);
            btnSalvar.setEnabled(!testing);
            progressBar.setVisible(testing);
            
            if (testing) {
                lblStatus.setForeground(Color.BLUE);
                lblStatus.setText("Testando conexão...");
            }
        });
    }
}