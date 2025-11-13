package com.inventario;

import com.inventario.config.DatabaseConfigManager;
import com.inventario.ui.ConfiguracaoBancoController;
import com.inventario.util.DatabaseConnection;
import com.inventario.util.SingleInstanceManager;
import com.inventario.view.JLogin;
import com.inventario.view.MainFrame;
import com.inventario.model.Usuario;

import javax.swing.*;

/**
 * Classe principal da aplicação Sistema de Inventário
 * Gerencia a inicialização e navegação entre telas
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class SistemaInventarioApplication {
    
    private DatabaseConfigManager configManager;
    private ConfiguracaoBancoController configController;
    private JLogin loginFrame;
    private MainFrame mainFrame;
    
    public SistemaInventarioApplication() {
        configManager = new DatabaseConfigManager();
        
        // Configurar Look and Feel
        setupLookAndFeel();
    }
    
    /**
     * Método principal para iniciar a aplicação
     */
    public static void main(String[] args) {
        // Verificar se já existe uma instância da aplicação em execução
        if (!SingleInstanceManager.isFirstInstance()) {
            System.out.println("Sistema de Inventário já está em execução.");
            SingleInstanceManager.showAlreadyRunningMessage();
            System.exit(0);
            return;
        }
        
        System.out.println("Iniciando Sistema de Inventário...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                SistemaInventarioApplication app = new SistemaInventarioApplication();
                app.start();
            } catch (Exception e) {
                System.err.println("Erro ao iniciar aplicação: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Erro ao iniciar a aplicação: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                
                // Liberar lock em caso de erro
                SingleInstanceManager.releaseLock();
                System.exit(1);
            }
        });
    }
    
    /**
     * Inicia a aplicação verificando se há configuração de banco
     */
    public void start() {
        // Verificar e migrar configuração se necessário
        if (!configManager.hasConfiguration()) {
            System.out.println("⚠ Nenhuma configuração de banco encontrada");
            System.out.println("  Configure via tela de login ou arquivo de configuração");
        }
        
        // Sempre mostrar tela de login (que tem botão de configuração)
        showLoginScreen();
    }
    
    /**
     * Exibe a tela de configuração do banco de dados
     */
    public void showConfigScreen() {
        if (configController == null) {
            configController = new ConfiguracaoBancoController();
            configController.setMainApp(this);
        }
        configController.showWindow();
    }
    
    /**
     * Exibe a tela de login do sistema
     */
    public void showLoginScreen() {
        showMainScreen(); // Alias para compatibilidade
    }
    
    /**
     * Exibe a tela de login do sistema
     */
    public void showMainScreen() {
        // Fechar tela de configuração se estiver aberta
        if (configController != null) {
            configController.dispose();
            configController = null;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Criar e exibir tela de login
                loginFrame = new JLogin();
                loginFrame.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("Erro ao abrir tela de login: " + e.getMessage());
                
                // Verificar se é erro de configuração
                if (e.getMessage() != null && e.getMessage().contains("configuração")) {
                    JOptionPane.showMessageDialog(null, 
                        "Erro: Nenhuma configuração de banco encontrada.\n\n" +
                        "Configure o banco de dados via:\n" +
                        "1. Botão '⚙ Configurar Banco' na tela de login\n" +
                        "2. Ou arquivo ~/.inventario/database-config.properties", 
                        "Configuração Necessária", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Erro ao abrir tela de login.\n" +
                        "Erro: " + e.getMessage(), 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
                // Se falha, volta para configuração
                showConfigScreen();
            }
        });
    }
    
    /**
     * Exibe a tela principal do sistema após login bem-sucedido
     */
    public void showDashboard(Usuario usuario) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Fechar tela de login se estiver aberta
                if (loginFrame != null) {
                    loginFrame.dispose();
                    loginFrame = null;
                }
                
                // Criar e exibir tela principal
                mainFrame = new MainFrame(usuario);
                mainFrame.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("Erro ao abrir dashboard: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Erro ao abrir dashboard: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Configura o Look and Feel da aplicação
     */
    private void setupLookAndFeel() {
        try {
            // Tenta usar o Look and Feel do sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Não foi possível definir Look and Feel: " + e.getMessage());
            // Continua com o padrão
        }
    }
    
    /**
     * Retorna o gerenciador de configuração
     */
    public DatabaseConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Testa a conexão com banco de dados
     */
    public boolean testDatabaseConnection() {
        return DatabaseConnection.testConnection();
    }
    
    /**
     * Recarrega a configuração do banco de dados
     */
    public void reloadDatabaseConfig() {
        DatabaseConnection.reloadConfig();
    }
    
    /**
     * Encerra a aplicação
     */
    public void shutdown() {
        try {
            System.out.println("Encerrando Sistema de Inventário...");
            
            // Fechar todas as janelas abertas
            if (mainFrame != null) {
                mainFrame.dispose();
                mainFrame = null;
            }
            
            if (loginFrame != null) {
                loginFrame.dispose();
                loginFrame = null;
            }
            
            if (configController != null) {
                configController.dispose();
                configController = null;
            }
            
            // As conexões serão fechadas automaticamente pelo pool de conexões
            
            // Liberar lock da instância única
            SingleInstanceManager.releaseLock();
            
        } catch (Exception e) {
            System.err.println("Erro ao encerrar aplicação: " + e.getMessage());
        } finally {
            // Garantir que o lock seja liberado mesmo em caso de erro
            SingleInstanceManager.releaseLock();
            System.exit(0);
        }
    }
}