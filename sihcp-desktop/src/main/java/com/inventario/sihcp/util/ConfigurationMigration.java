package com.inventario.sihcp.util;

import com.inventario.sihcp.config.DatabaseConfig;
import com.inventario.sihcp.config.DatabaseConfigManager;

import javax.swing.*;
import java.awt.*;

/**
 * Utilitário para migração de configurações hardcoded para arquivo
 * 
 * Este utilitário ajuda a migrar senhas e configurações que estavam
 * hardcoded no código para o arquivo de configuração criptografado.
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class ConfigurationMigration {
    
    /**
     * Verifica se existe configuração válida, caso contrário solicita ao usuário
     * 
     * @param parentFrame Frame pai para exibir dialogs
     * @return true se configuração está válida ou foi criada
     */
    public static boolean ensureConfiguration(Frame parentFrame) {
        DatabaseConfigManager configManager = new DatabaseConfigManager();
        
        // Se já tem configuração válida, não precisa fazer nada
        if (configManager.hasConfiguration()) {
            DatabaseConfig config = configManager.getCurrentConfig();
            System.out.println("✓ Configuração válida encontrada: " + config.getHost() + ":" + config.getPort());
            return true;
        }
        
        // Não tem configuração, solicitar ao usuário
        System.out.println("⚠ Nenhuma configuração de banco encontrada");
        
        // Tentar migração automática de valores padrão
        boolean migrated = tryAutoMigration(configManager);
        
        if (migrated) {
            System.out.println("✓ Configuração migrada automaticamente");
            
            // Perguntar se quer revisar
            int resposta = JOptionPane.showConfirmDialog(
                parentFrame,
                "Configuração de banco foi criada automaticamente.\n" +
                "Deseja revisar as configurações?",
                "Configuração Criada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );
            
            if (resposta == JOptionPane.YES_OPTION) {
                ConfiguracaoBancoUtil.abrirDialogConfiguracao(parentFrame);
            }
            
            return true;
        }
        
        // Não conseguiu migrar, solicitar configuração manual
        int resposta = JOptionPane.showConfirmDialog(
            parentFrame,
            "Nenhuma configuração de banco de dados encontrada.\n\n" +
            "O sistema precisa de uma configuração válida para funcionar.\n" +
            "Deseja configurar agora?",
            "Configuração Necessária",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (resposta == JOptionPane.YES_OPTION) {
            return ConfiguracaoBancoUtil.abrirDialogConfiguracao(parentFrame);
        }
        
        return false;
    }
    
    /**
     * Tenta migração automática de valores padrão conhecidos
     * 
     * @param configManager Gerenciador de configurações
     * @return true se conseguiu migrar
     */
    private static boolean tryAutoMigration(DatabaseConfigManager configManager) {
        try {
            // Valores padrão comuns (sem senha por segurança)
            DatabaseConfig defaultConfig = new DatabaseConfig(
                "localhost",
                5432,
                "sispatrimonio",
                "postgres",
                "" // Senha vazia - usuário precisará configurar
            );
            
            // Testar se consegue conectar (improvável sem senha)
            if (configManager.testConnection(defaultConfig)) {
                System.out.println("✓ Conexão padrão funciona (sem senha)");
                configManager.saveConfiguration(defaultConfig);
                return true;
            }
            
            // Não conseguiu conectar, não salvar configuração inválida
            System.out.println("✗ Não foi possível conectar com configuração padrão");
            return false;
            
        } catch (Exception e) {
            System.err.println("✗ Erro na migração automática: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Migra uma configuração específica para o arquivo
     * 
     * @param host Host do banco
     * @param port Porta do banco
     * @param database Nome do banco
     * @param username Usuário
     * @param password Senha (será criptografada)
     * @return true se migrou com sucesso
     */
    public static boolean migrateConfiguration(String host, int port, String database, 
                                               String username, String password) {
        try {
            DatabaseConfig config = new DatabaseConfig(host, port, database, username, password);
            
            // Validar configuração
            if (!config.isValid()) {
                System.err.println("✗ Configuração inválida para migração");
                return false;
            }
            
            // Testar conexão
            DatabaseConfigManager configManager = new DatabaseConfigManager();
            if (!configManager.testConnection(config)) {
                System.err.println("✗ Não foi possível conectar com a configuração fornecida");
                return false;
            }
            
            // Salvar (senha será criptografada automaticamente)
            boolean saved = configManager.saveConfiguration(config);
            
            if (saved) {
                System.out.println("✓ Configuração migrada e salva com sucesso");
                System.out.println("  Host: " + host + ":" + port);
                System.out.println("  Database: " + database);
                System.out.println("  Username: " + username);
                System.out.println("  Senha: [CRIPTOGRAFADA]");
                
                // Atualizar DatabaseConnection
                DatabaseConnection.updateConfig(config);
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao migrar configuração: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove senhas hardcoded do código (apenas log de aviso)
     * 
     * @deprecated Este método é apenas informativo
     */
    @Deprecated
    public static void warnAboutHardcodedPasswords() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║  ⚠️  AVISO DE SEGURANÇA                                        ║\n" +
            "╠════════════════════════════════════════════════════════════════╣\n" +
            "║                                                                ║\n" +
            "║  Senhas hardcoded no código foram REMOVIDAS por segurança.    ║\n" +
            "║                                                                ║\n" +
            "║  Configure o banco de dados via:                              ║\n" +
            "║  1. Tela de Login → Botão '⚙ Configurar Banco'               ║\n" +
            "║  2. Ou programaticamente via ConfiguracaoBancoUtil            ║\n" +
            "║                                                                ║\n" +
            "║  A senha será salva CRIPTOGRAFADA com AES-256.                ║\n" +
            "║                                                                ║\n" +
            "╚════════════════════════════════════════════════════════════════╝\n"
        );
    }
    
    /**
     * Exemplo de uso para migração manual
     */
    public static void main(String[] args) {
        System.out.println("=== Utilitário de Migração de Configuração ===\n");
        
        // Exemplo 1: Verificar configuração atual
        DatabaseConfigManager configManager = new DatabaseConfigManager();
        
        if (configManager.hasConfiguration()) {
            DatabaseConfig config = configManager.getCurrentConfig();
            System.out.println("✓ Configuração existente:");
            System.out.println("  " + config.toSafeString());
        } else {
            System.out.println("✗ Nenhuma configuração encontrada");
        }
        
        // Exemplo 2: Migrar configuração (descomente e ajuste os valores)
        /*
        boolean migrated = migrateConfiguration(
            "localhost",     // host
            5432,           // port
            "sispatrimonio", // database
            "postgres",     // username
            "sua_senha_aqui" // password (será criptografada)
        );
        
        if (migrated) {
            System.out.println("\n✓ Migração concluída com sucesso!");
        } else {
            System.out.println("\n✗ Falha na migração");
        }
        */
        
        // Exemplo 3: Aviso sobre senhas hardcoded
        warnAboutHardcodedPasswords();
    }
}
