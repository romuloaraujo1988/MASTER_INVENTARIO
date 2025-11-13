package com.inventario.view;

import com.inventario.view.ui.ModernButtons;
import com.inventario.config.DatabaseConfig;
import com.inventario.config.DatabaseConfigManager;
import com.inventario.util.DatabaseConnection;
import com.inventario.util.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Dialog para configuração da conexão com o banco de dados
 */
public class ConfiguracaoBancoDialog extends JDialog {
    
    private boolean confirmado = false;
    
    // Campos do formulário
    private JTextField campoServidor;
    private JTextField campoPorta;
    private JTextField campoBanco;
    private JTextField campoUsuario;
    private JPasswordField campoSenha;
    private JComboBox<String> comboSGBD;
    private JCheckBox checkSalvarSenha;
    
    private JButton btnTestar, btnSalvar, btnCancelar;
    private JLabel labelStatus;
    
    // Gerenciador de configurações
    private DatabaseConfigManager configManager;
    
    public ConfiguracaoBancoDialog(Frame parent) {
        super(parent, "Configuração do Banco de Dados", true);
        this.configManager = new DatabaseConfigManager();
        initComponents();
        carregarConfiguracoes();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // SGBD
        gbc.gridx = 0; gbc.gridy = 0;
        painelPrincipal.add(new JLabel("SGBD:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        comboSGBD = new JComboBox<>(new String[]{"MySQL", "PostgreSQL", "SQL Server", "Oracle"});
        comboSGBD.addActionListener(e -> atualizarPortaPadrao());
        painelPrincipal.add(comboSGBD, gbc);
        
        // Servidor
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelPrincipal.add(new JLabel("Servidor:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoServidor = new JTextField("localhost", 20);
        painelPrincipal.add(campoServidor, gbc);
        
        // Porta
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelPrincipal.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoPorta = new JTextField("3306", 20);
        painelPrincipal.add(campoPorta, gbc);
        
        // Nome do Banco
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelPrincipal.add(new JLabel("Nome do Banco:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoBanco = new JTextField("inventario", 20);
        painelPrincipal.add(campoBanco, gbc);
        
        // Usuário
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelPrincipal.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoUsuario = new JTextField("root", 20);
        painelPrincipal.add(campoUsuario, gbc);
        
        // Senha
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelPrincipal.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoSenha = new JPasswordField(20);
        painelPrincipal.add(campoSenha, gbc);
        
        // Checkbox salvar senha
        gbc.gridx = 1; gbc.gridy = 6;
        checkSalvarSenha = new JCheckBox("Salvar senha (criptografada)");
        checkSalvarSenha.setToolTipText("A senha será salva criptografada usando AES-256");
        painelPrincipal.add(checkSalvarSenha, gbc);
        
        // Status da conexão
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        labelStatus = new JLabel("Status: Não testado");
        labelStatus.setBorder(BorderFactory.createLoweredBevelBorder());
        labelStatus.setOpaque(true);
        labelStatus.setBackground(Color.LIGHT_GRAY);
        painelPrincipal.add(labelStatus, gbc);
        
        add(painelPrincipal, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        btnTestar = ModernButtons.secondary("Testar Conexão");
        btnSalvar = ModernButtons.primary("Salvar");
        btnCancelar = ModernButtons.muted("Cancelar");
        JButton btnLimpar = ModernButtons.danger("Limpar Config");
        
        painelBotoes.add(btnTestar);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Evento para limpar configurações
        btnLimpar.addActionListener(e -> limparConfiguracoes());
        
        // Configurar eventos
        configurarEventos();
        
        setSize(450, 350);
        setLocationRelativeTo(getParent());
    }
    
    private void configurarEventos() {
        btnTestar.addActionListener(e -> testarConexao());
        btnSalvar.addActionListener(e -> salvarConfiguracao());
        btnCancelar.addActionListener(e -> dispose());
    }
    
    private void atualizarPortaPadrao() {
        String sgbd = (String) comboSGBD.getSelectedItem();
        switch (sgbd) {
            case "MySQL":
                campoPorta.setText("3306");
                break;
            case "PostgreSQL":
                campoPorta.setText("5432");
                break;
            case "SQL Server":
                campoPorta.setText("1433");
                break;
            case "Oracle":
                campoPorta.setText("1521");
                break;
        }
    }
    
    private void testarConexao() {
        btnTestar.setEnabled(false);
        labelStatus.setText("Status: Testando conexão...");
        labelStatus.setBackground(Color.YELLOW);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return testarConexaoBanco();
            }
            
            @Override
            protected void done() {
                try {
                    boolean sucesso = get();
                    if (sucesso) {
                        labelStatus.setText("Status: Conexão bem-sucedida!");
                        labelStatus.setBackground(Color.GREEN);
                        btnSalvar.setEnabled(true);
                    } else {
                        labelStatus.setText("Status: Falha na conexão");
                        labelStatus.setBackground(Color.RED);
                    }
                } catch (Exception e) {
                    labelStatus.setText("Status: Erro - " + e.getMessage());
                    labelStatus.setBackground(Color.RED);
                }
                btnTestar.setEnabled(true);
            }
        };
        worker.execute();
    }
    
    private boolean testarConexaoBanco() {
        try {
            String url = construirURL();
            String usuario = campoUsuario.getText();
            String senha = new String(campoSenha.getPassword());
            
            Connection conn = DriverManager.getConnection(url, usuario, senha);
            conn.close();
            return true;
            
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao conectar com o banco de dados:\n" + e.getMessage(),
                    "Erro de Conexão", 
                    JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
    }
    
    private String construirURL() {
        String sgbd = (String) comboSGBD.getSelectedItem();
        String servidor = campoServidor.getText();
        String porta = campoPorta.getText();
        String banco = campoBanco.getText();
        
        switch (sgbd) {
            case "MySQL":
                return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC", 
                                    servidor, porta, banco);
            case "PostgreSQL":
                return String.format("jdbc:postgresql://%s:%s/%s", servidor, porta, banco);
            case "SQL Server":
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s", 
                                    servidor, porta, banco);
            case "Oracle":
                return String.format("jdbc:oracle:thin:@%s:%s:%s", servidor, porta, banco);
            default:
                throw new IllegalArgumentException("SGBD não suportado: " + sgbd);
        }
    }
    
    private void salvarConfiguracao() {
        try {
            // Validar campos obrigatórios
            if (campoServidor.getText().trim().isEmpty() ||
                campoPorta.getText().trim().isEmpty() ||
                campoBanco.getText().trim().isEmpty() ||
                campoUsuario.getText().trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(this, 
                    "Todos os campos são obrigatórios.",
                    "Validação", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validar porta
            int porta;
            try {
                porta = Integer.parseInt(campoPorta.getText().trim());
                if (porta <= 0 || porta > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Porta inválida. Digite um número entre 1 e 65535.",
                    "Validação", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Criar configuração do banco
            DatabaseConfig config = new DatabaseConfig(
                campoServidor.getText().trim(),
                porta,
                campoBanco.getText().trim(),
                campoUsuario.getText().trim(),
                new String(campoSenha.getPassword())
            );
            
            // Salvar configuração do banco (PostgreSQL)
            boolean salvouBanco = configManager.saveConfiguration(config);
            
            // Salvar configuração do SGBD
            boolean salvouSGBD = salvarConfiguracaoSGBD();
            
            if (salvouBanco && salvouSGBD) {
                confirmado = true;
                
                // Atualizar DatabaseConnection
                DatabaseConnection.updateConfig(config);
                
                // Reinicializar ConnectionManager se necessário
                if (ConnectionManager.isInitialized()) {
                    ConnectionManager.shutdown();
                }
                
                String jdbcUrl = construirURL();
                ConnectionManager.initialize(jdbcUrl, config.getUsername(), config.getPassword());
                
                dispose();
                
                JOptionPane.showMessageDialog(getParent(), 
                    "Configurações salvas com sucesso!\n" +
                    "A conexão foi atualizada e está pronta para uso.",
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao salvar algumas configurações.",
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar configurações: " + e.getMessage(),
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Salva configurações específicas do SGBD
     */
    private boolean salvarConfiguracaoSGBD() {
        try {
            // Criar diretório se não existir
            com.inventario.config.ConfigurationPaths.createConfigDirectoryIfNotExists();
            
            String sgbdConfigPath = com.inventario.config.ConfigurationPaths.getSGBDConfigPath();
            
            Properties props = new Properties();
            props.setProperty("sgbd", (String) comboSGBD.getSelectedItem());
            props.setProperty("salvar_senha", String.valueOf(checkSalvarSenha.isSelected()));
            
            try (FileOutputStream fos = new FileOutputStream(sgbdConfigPath)) {
                props.store(fos, "SGBD Configuration");
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração do SGBD: " + e.getMessage());
            return false;
        }
    }
    
    private void carregarConfiguracoes() {
        try {
            // Carregar configuração do banco (PostgreSQL por padrão)
            DatabaseConfig config = configManager.getCurrentConfig();
            
            if (config != null && config.isValid()) {
                campoServidor.setText(config.getHost());
                campoPorta.setText(String.valueOf(config.getPort()));
                campoBanco.setText(config.getDatabase());
                campoUsuario.setText(config.getUsername());
                
                // Carregar senha se foi salva
                if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                    campoSenha.setText(config.getPassword());
                }
            }
            
            // Carregar configuração do SGBD
            carregarConfiguracaoSGBD();
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar configurações: " + e.getMessage());
            // Usar valores padrão já definidos nos campos
        }
    }
    
    /**
     * Carrega configurações específicas do SGBD
     */
    private void carregarConfiguracaoSGBD() {
        String sgbdConfigPath = com.inventario.config.ConfigurationPaths.getSGBDConfigPath();
        File configFile = new File(sgbdConfigPath);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(fis);
                
                String sgbd = props.getProperty("sgbd", "PostgreSQL");
                comboSGBD.setSelectedItem(sgbd);
                
                boolean salvarSenha = Boolean.parseBoolean(props.getProperty("salvar_senha", "false"));
                checkSalvarSenha.setSelected(salvarSenha);
                
            } catch (IOException e) {
                System.err.println("Erro ao carregar configuração do SGBD: " + e.getMessage());
                // Usar PostgreSQL como padrão
                comboSGBD.setSelectedItem("PostgreSQL");
            }
        } else {
            // Usar PostgreSQL como padrão
            comboSGBD.setSelectedItem("PostgreSQL");
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    // Getters para as configurações
    public String getSGBD() {
        return (String) comboSGBD.getSelectedItem();
    }
    
    public String getServidor() {
        return campoServidor.getText();
    }
    
    public String getPorta() {
        return campoPorta.getText();
    }
    
    public String getBanco() {
        return campoBanco.getText();
    }
    
    public String getUsuarioBanco() {
        return campoUsuario.getText();
    }
    
    public String getSenhaBanco() {
        return new String(campoSenha.getPassword());
    }
    
    public boolean isSalvarSenha() {
        return checkSalvarSenha.isSelected();
    }
    
    /**
     * Limpa as configurações salvas
     */
    private void limparConfiguracoes() {
        int resposta = JOptionPane.showConfirmDialog(this,
            "Tem certeza que deseja limpar todas as configurações salvas?\n" +
            "Esta ação não pode ser desfeita.",
            "Confirmar Limpeza",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (resposta == JOptionPane.YES_OPTION) {
            try {
                // Remover configuração do banco
                boolean removeuBanco = configManager.removeConfiguration();
                
                // Remover configuração do SGBD
                String sgbdConfigPath = com.inventario.config.ConfigurationPaths.getSGBDConfigPath();
                File sgbdConfig = new File(sgbdConfigPath);
                boolean removeuSGBD = true;
                if (sgbdConfig.exists()) {
                    removeuSGBD = sgbdConfig.delete();
                }
                
                if (removeuBanco && removeuSGBD) {
                    // Limpar campos
                    campoServidor.setText("localhost");
                    campoPorta.setText("5432");
                    campoBanco.setText("");
                    campoUsuario.setText("");
                    campoSenha.setText("");
                    comboSGBD.setSelectedItem("PostgreSQL");
                    checkSalvarSenha.setSelected(false);
                    labelStatus.setText("Status: Configurações limpas");
                    labelStatus.setBackground(Color.LIGHT_GRAY);
                    
                    JOptionPane.showMessageDialog(this,
                        "Configurações limpas com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erro ao limpar algumas configurações.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao limpar configurações: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Obtém informações sobre a configuração atual
     */
    public String getConfigInfo() {
        if (configManager.hasConfiguration()) {
            DatabaseConfig config = configManager.getCurrentConfig();
            return String.format("Conectado: %s:%d/%s (Usuário: %s)",
                config.getHost(),
                config.getPort(),
                config.getDatabase(),
                config.getUsername());
        }
        return "Nenhuma configuração salva";
    }
}