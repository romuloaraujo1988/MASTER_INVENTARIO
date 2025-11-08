package com.inventario.view;

import com.inventario.view.ui.ModernButtons;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
    
    public ConfiguracaoBancoDialog(Frame parent) {
        super(parent, "Configuração do Banco de Dados", true);
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
        checkSalvarSenha = new JCheckBox("Salvar senha (não recomendado)");
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
        
        painelBotoes.add(btnTestar);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
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
            
            // TODO: Salvar configurações em arquivo de propriedades
            // Properties props = new Properties();
            // props.setProperty("sgbd", (String) comboSGBD.getSelectedItem());
            // props.setProperty("servidor", campoServidor.getText());
            // props.setProperty("porta", campoPorta.getText());
            // props.setProperty("banco", campoBanco.getText());
            // props.setProperty("usuario", campoUsuario.getText());
            // if (checkSalvarSenha.isSelected()) {
            //     props.setProperty("senha", new String(campoSenha.getPassword()));
            // }
            
            confirmado = true;
            dispose();
            
            JOptionPane.showMessageDialog(getParent(), 
                "Configurações salvas com sucesso!\nReinicie a aplicação para aplicar as mudanças.",
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar configurações: " + e.getMessage(),
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarConfiguracoes() {
        // TODO: Carregar configurações salvas
        // Properties props = new Properties();
        // try (InputStream input = new FileInputStream("config.properties")) {
        //     props.load(input);
        //     comboSGBD.setSelectedItem(props.getProperty("sgbd", "MySQL"));
        //     campoServidor.setText(props.getProperty("servidor", "localhost"));
        //     campoPorta.setText(props.getProperty("porta", "3306"));
        //     campoBanco.setText(props.getProperty("banco", "inventario"));
        //     campoUsuario.setText(props.getProperty("usuario", "root"));
        //     if (props.containsKey("senha")) {
        //         campoSenha.setText(props.getProperty("senha"));
        //         checkSalvarSenha.setSelected(true);
        //     }
        // } catch (IOException e) {
        //     // Arquivo não existe, usar valores padrão
        // }
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
}