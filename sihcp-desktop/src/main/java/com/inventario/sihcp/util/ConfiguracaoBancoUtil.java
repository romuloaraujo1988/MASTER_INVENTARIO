package com.inventario.sihcp.util;

import com.inventario.sihcp.config.DatabaseConfig;
import com.inventario.sihcp.config.DatabaseConfigManager;
import com.inventario.sihcp.view.ConfiguracaoBancoDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Utilitário para gerenciar configurações de banco de dados
 * Fornece métodos estáticos para abrir o dialog e verificar configurações
 */
public class ConfiguracaoBancoUtil {
    
    private static DatabaseConfigManager configManager = new DatabaseConfigManager();
    
    /**
     * Abre o dialog de configuração do banco
     * 
     * @param parent Frame pai
     * @return true se o usuário confirmou as configurações
     */
    public static boolean abrirDialogConfiguracao(Frame parent) {
        ConfiguracaoBancoDialog dialog = new ConfiguracaoBancoDialog(parent);
        dialog.setVisible(true);
        return dialog.isConfirmado();
    }
    
    /**
     * Verifica se existe configuração válida
     * 
     * @return true se existe configuração válida
     */
    public static boolean temConfiguracaoValida() {
        return configManager.hasConfiguration();
    }
    
    /**
     * Obtém a configuração atual
     * 
     * @return DatabaseConfig ou null se não existe
     */
    public static DatabaseConfig getConfiguracaoAtual() {
        return configManager.getCurrentConfig();
    }
    
    /**
     * Testa a conexão com o banco
     * 
     * @return true se a conexão foi bem-sucedida
     */
    public static boolean testarConexao() {
        return configManager.testConnection();
    }
    
    /**
     * Obtém informações sobre a configuração atual
     * 
     * @return String com informações da configuração
     */
    public static String getInfoConfiguracao() {
        if (temConfiguracaoValida()) {
            DatabaseConfig config = configManager.getCurrentConfig();
            return String.format("Host: %s:%d | Database: %s | User: %s",
                config.getHost(),
                config.getPort(),
                config.getDatabase(),
                config.getUsername());
        }
        return "Nenhuma configuração encontrada";
    }
    
    /**
     * Verifica se a configuração está válida, caso contrário abre o dialog
     * 
     * @param parent Frame pai
     * @return true se a configuração está válida ou foi configurada
     */
    public static boolean verificarOuConfigurar(Frame parent) {
        if (!temConfiguracaoValida()) {
            int resposta = JOptionPane.showConfirmDialog(parent,
                "Nenhuma configuração de banco de dados encontrada.\n" +
                "Deseja configurar agora?",
                "Configuração Necessária",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (resposta == JOptionPane.YES_OPTION) {
                return abrirDialogConfiguracao(parent);
            }
            return false;
        }
        return true;
    }
    
    /**
     * Exibe informações da configuração atual em um dialog
     * 
     * @param parent Frame pai
     */
    public static void mostrarInfoConfiguracao(Frame parent) {
        String info = getInfoConfiguracao();
        String status = testarConexao() ? "✓ Conexão OK" : "✗ Falha na conexão";
        
        JOptionPane.showMessageDialog(parent,
            info + "\n" + status,
            "Informações da Configuração",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Recarrega a configuração do arquivo
     */
    public static void recarregarConfiguracao() {
        configManager = new DatabaseConfigManager();
        DatabaseConnection.reloadConfig();
    }
}
