package com.inventario.view;

import com.inventario.view.ui.ModernButtons;
import com.inventario.config.DatabaseConfig;
import com.inventario.config.DatabaseConfigManager;
import com.inventario.util.DatabaseConnection;
import com.inventario.util.ConnectionManager;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

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
        JButton btnAjuda = ModernButtons.info("📖 Ajuda");
        
        btnAjuda.setToolTipText("Abrir guia de configuração PostgreSQL remoto");
        
        painelBotoes.add(btnAjuda);
        painelBotoes.add(btnTestar);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Evento para limpar configurações
        btnLimpar.addActionListener(e -> limparConfiguracoes());
        
        // Evento para abrir ajuda
        btnAjuda.addActionListener(e -> abrirGuiaAjuda());
        
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
            case "MySQL" -> campoPorta.setText("3306");
            case "PostgreSQL" -> campoPorta.setText("5432");
            case "SQL Server" -> campoPorta.setText("1433");
            case "Oracle" -> campoPorta.setText("1521");
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
                } catch (InterruptedException | ExecutionException e) {
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
                String mensagemErro = interpretarErroConexao(e);
                JOptionPane.showMessageDialog(this, 
                    mensagemErro,
                    "Erro de Conexão", 
                    JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
    }
    
    /**
     * Interpreta erros de conexão e retorna mensagem amigável
     */
    private String interpretarErroConexao(SQLException e) {
        String mensagem = e.getMessage().toLowerCase();
        
        // Erro de pg_hba.conf (acesso remoto não configurado)
        if (mensagem.contains("pg_hba.conf") || mensagem.contains("no pg_hba.conf entry")) {
            return """
                   \u274c Erro de Configura\u00e7\u00e3o do PostgreSQL
                   
                   O servidor PostgreSQL n\u00e3o est\u00e1 configurado para aceitar
                   conex\u00f5es remotas deste computador.
                   
                   Solu\u00e7\u00e3o:
                   1. No servidor PostgreSQL, edite o arquivo pg_hba.conf
                   2. Adicione uma linha permitindo seu IP:
                      host    all    all    """ + obterIPLocal() + "/32    md5\n" +
                   "3. Edite postgresql.conf e configure:\n" +
                   "   listen_addresses = '*'\n" +
                   "4. Reinicie o PostgreSQL\n" +
                   "5. Abra a porta 5432 no firewall\n\n" +
                   "Consulte o arquivo GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md\n" +
                   "para instruções detalhadas.\n\n" +
                   "Erro técnico: " + e.getMessage();
        }
        
        // Erro de conexão recusada (servidor não está rodando ou firewall)
        if (mensagem.contains("connection refused") || mensagem.contains("conexão recusada")) {
            return """
                   \u274c Conex\u00e3o Recusada
                   
                   N\u00e3o foi poss\u00edvel conectar ao servidor PostgreSQL.
                   
                   Poss\u00edveis causas:
                   \u2022 PostgreSQL n\u00e3o est\u00e1 rodando no servidor
                   \u2022 Firewall bloqueando a porta """ + campoPorta.getText() + "\n" +
                   "• Endereço ou porta incorretos\n\n" +
                   "Verifique:\n" +
                   "1. Se o PostgreSQL está rodando\n" +
                   "2. Se o firewall permite conexões na porta " + campoPorta.getText() + "\n" +
                   "3. Se o endereço está correto: " + campoServidor.getText() + "\n\n" +
                   "Erro técnico: " + e.getMessage();
        }
        
        // Erro de timeout (servidor não responde)
        if (mensagem.contains("timeout") || mensagem.contains("timed out")) {
            return """
                   \u274c Tempo Esgotado (Timeout)
                   
                   O servidor n\u00e3o respondeu dentro do tempo esperado.
                   
                   Poss\u00edveis causas:
                   \u2022 Servidor est\u00e1 muito lento ou sobrecarregado
                   \u2022 Problemas de rede
                   \u2022 Firewall bloqueando a conex\u00e3o
                   
                   Tente novamente ou verifique a conex\u00e3o de rede.
                   
                   Erro t\u00e9cnico: """ + e.getMessage();
        }
        
        // Erro de autenticação (usuário/senha incorretos)
        if (mensagem.contains("password authentication failed") || 
            mensagem.contains("autenticação") ||
            mensagem.contains("authentication")) {
            return """
                   \u274c Falha na Autentica\u00e7\u00e3o
                   
                   Usu\u00e1rio ou senha incorretos.
                   
                   Verifique:
                   \u2022 Usu\u00e1rio: """ + campoUsuario.getText() + "\n" +
                   "• Senha digitada\n" +
                   "• Se o usuário tem permissão no banco\n\n" +
                   "Erro técnico: " + e.getMessage();
        }
        
        // Erro de banco não existe
        if (mensagem.contains("database") && mensagem.contains("does not exist")) {
            return """
                   \u274c Banco de Dados N\u00e3o Encontrado
                   
                   O banco de dados '""" + campoBanco.getText() + "' não existe.\n\n" +
                   "Solução:\n" +
                   "1. Verifique se o nome está correto\n" +
                   "2. Crie o banco de dados no PostgreSQL:\n" +
                   "   CREATE DATABASE " + campoBanco.getText() + ";\n\n" +
                   "Erro técnico: " + e.getMessage();
        }
        
        // Erro genérico
        return """
               \u274c Erro ao Conectar com o Banco de Dados
               
               Detalhes do erro:
               """ + e.getMessage() + "\n\n" +
               "Verifique:\n" +
               "• Servidor: " + campoServidor.getText() + "\n" +
               "• Porta: " + campoPorta.getText() + "\n" +
               "• Banco: " + campoBanco.getText() + "\n" +
               "• Usuário: " + campoUsuario.getText() + "\n\n" +
               "Consulte o arquivo GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md\n" +
               "para mais informações.";
    }
    
    /**
     * Obtém o IP local da máquina (melhor esforço)
     */
    private String obterIPLocal() {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return "SEU_IP";
        }
    }
    
    private String construirURL() {
        String sgbd = (String) comboSGBD.getSelectedItem();
        String servidor = campoServidor.getText();
        String porta = campoPorta.getText();
        String banco = campoBanco.getText();
        
        switch (sgbd) {
            case "MySQL" -> {
                return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC",
                        servidor, porta, banco);
            }
            case "PostgreSQL" -> {
                return String.format("jdbc:postgresql://%s:%s/%s", servidor, porta, banco);
            }
            case "SQL Server" -> {
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s",
                        servidor, porta, banco);
            }
            case "Oracle" -> {
                return String.format("jdbc:oracle:thin:@%s:%s:%s", servidor, porta, banco);
            }
            default -> throw new IllegalArgumentException("SGBD não suportado: " + sgbd);
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
            
            // ✅ NOVO: Salvar também no arquivo configuracao_banco.json (prioridade máxima)
            boolean salvouJson = salvarConfiguracaoBancoJson(config);
            
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
                
                String msgJson = salvouJson ? "\n✅ Arquivo configuracao_banco.json atualizado!" : "";
                JOptionPane.showMessageDialog(getParent(), 
                    "Configurações salvas com sucesso!" + msgJson + "\n" +
                    "A conexão foi atualizada e está pronta para uso.\n\n" +
                    "Host: " + config.getHost() + ":" + config.getPort(),
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao salvar algumas configurações.",
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar configurações: " + e.getMessage(),
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Salva a configuração no arquivo configuracao_banco.json na raiz do projeto
     * Este arquivo tem PRIORIDADE MÁXIMA no DatabaseConnection
     */
    private boolean salvarConfiguracaoBancoJson(DatabaseConfig config) {
        try {
            File jsonFile = new File("configuracao_banco.json");
            
            // Construir JSON manualmente (sem dependência externa)
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("    \"postgresql\": {\n");
            json.append("        \"host\": \"").append(config.getHost()).append("\",\n");
            json.append("        \"database\": \"").append(config.getDatabase()).append("\",\n");
            json.append("        \"user\": \"").append(config.getUsername()).append("\",\n");
            json.append("        \"password\": \"").append(config.getPassword()).append("\",\n");
            json.append("        \"port\": ").append(config.getPort()).append(",\n");
            json.append("        \"schema\": \"public\"\n");
            json.append("    },\n");
            json.append("    \"sqlite\": {\n");
            json.append("        \"database\": \"inventario.db\",\n");
            json.append("        \"backup_dir\": \"backups/").append(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())).append("\"\n");
            json.append("    },\n");
            json.append("    \"mysql\": {\n");
            json.append("        \"host\": \"localhost\",\n");
            json.append("        \"database\": \"sispatrimonio\",\n");
            json.append("        \"user\": \"root\",\n");
            json.append("        \"password\": \"\",\n");
            json.append("        \"port\": 3306\n");
            json.append("    },\n");
            json.append("    \"debug\": true,\n");
            json.append("    \"log_queries\": false\n");
            json.append("}\n");
            
            // Escrever no arquivo
            try (FileWriter writer = new FileWriter(jsonFile)) {
                writer.write(json.toString());
            }
            
            System.out.println("✅ Arquivo configuracao_banco.json atualizado com sucesso!");
            System.out.println("   Host: " + config.getHost() + ":" + config.getPort());
            System.out.println("   Database: " + config.getDatabase());
            System.out.println("   User: " + config.getUsername());
            
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar configuracao_banco.json: " + e.getMessage());
            return false;
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
        int resposta = JOptionPane.showConfirmDialog(this, """
                                                           Tem certeza que deseja limpar todas as configura\u00e7\u00f5es salvas?
                                                           Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita.""",
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
                
            } catch (HeadlessException e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao limpar configurações: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
    
    /**
     * Abre o guia de ajuda para configuração PostgreSQL remoto
     */
    private void abrirGuiaAjuda() {
        try {
            File guia = new File("GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md");
            
            if (!guia.exists()) {
                JOptionPane.showMessageDialog(this, """
                                                    Arquivo de ajuda n\u00e3o encontrado.
                                                    
                                                    Procure pelo arquivo:
                                                    GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md
                                                    
                                                    na pasta raiz do sistema.""",
                    "Ajuda",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Tentar abrir com o aplicativo padrão
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(guia);
                    return;
                }
            }
            
            // Fallback: mostrar caminho do arquivo
            JOptionPane.showMessageDialog(this,
                "Abra o arquivo manualmente:\n\n" +
                guia.getAbsolutePath() + "\n\n" +
                "Este arquivo contém instruções detalhadas para\n" +
                "configurar o PostgreSQL para aceitar conexões remotas.",
                "Ajuda",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (HeadlessException | IOException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao abrir o guia de ajuda:\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}