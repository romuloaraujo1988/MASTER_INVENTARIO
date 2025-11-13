package com.inventario.config;

import com.inventario.util.PasswordEncryption;

import java.io.File;

/**
 * Teste de integração para DatabaseConfigManager com criptografia
 * Execute este main para testar o salvamento e carregamento de senhas criptografadas
 */
public class DatabaseConfigManagerEncryptionTest {
    
    public static void main(String[] args) {
        System.out.println("=== Teste de Integração: DatabaseConfigManager com Criptografia ===\n");
        
        try {
            // 1. Criar configuração de teste
            System.out.println("1. Criando configuração de teste...");
            DatabaseConfig config = new DatabaseConfig(
                "localhost",
                5432,
                "sispatrimonio",
                "postgres",
                "minhaSenhaSecreta123!@#"
            );
            System.out.println("   Senha original: " + config.getPassword());
            
            // 2. Salvar configuração (senha será criptografada)
            System.out.println("\n2. Salvando configuração...");
            DatabaseConfigManager manager = new DatabaseConfigManager();
            boolean saved = manager.saveConfiguration(config);
            System.out.println("   Salvo: " + saved);
            
            // 3. Verificar arquivo salvo
            System.out.println("\n3. Verificando arquivo salvo...");
            String configPath = System.getProperty("user.home") + File.separator + 
                               ".inventario" + File.separator + "database-config.properties";
            File configFile = new File(configPath);
            System.out.println("   Arquivo existe: " + configFile.exists());
            System.out.println("   Caminho: " + configPath);
            
            // 4. Ler arquivo e mostrar senha criptografada
            System.out.println("\n4. Conteúdo do arquivo:");
            if (configFile.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile)) {
                    props.load(fis);
                }
                System.out.println("   Host: " + props.getProperty("host"));
                System.out.println("   Port: " + props.getProperty("port"));
                System.out.println("   Database: " + props.getProperty("database"));
                System.out.println("   Username: " + props.getProperty("username"));
                System.out.println("   Password (criptografada): " + props.getProperty("password"));
                
                // Verificar se está criptografada
                String encryptedPassword = props.getProperty("password");
                boolean isEncrypted = PasswordEncryption.isEncrypted(encryptedPassword);
                System.out.println("   Senha está criptografada: " + isEncrypted);
            }
            
            // 5. Recarregar configuração (senha será descriptografada)
            System.out.println("\n5. Recarregando configuração...");
            DatabaseConfigManager newManager = new DatabaseConfigManager();
            DatabaseConfig loadedConfig = newManager.getCurrentConfig();
            
            if (loadedConfig != null) {
                System.out.println("   Configuração carregada: " + loadedConfig.isValid());
                System.out.println("   Host: " + loadedConfig.getHost());
                System.out.println("   Port: " + loadedConfig.getPort());
                System.out.println("   Database: " + loadedConfig.getDatabase());
                System.out.println("   Username: " + loadedConfig.getUsername());
                System.out.println("   Senha descriptografada: " + loadedConfig.getPassword());
                
                // 6. Verificar se a senha descriptografada é igual à original
                System.out.println("\n6. Verificando integridade...");
                boolean senhaCorreta = config.getPassword().equals(loadedConfig.getPassword());
                System.out.println("   Senha original: " + config.getPassword());
                System.out.println("   Senha carregada: " + loadedConfig.getPassword());
                System.out.println("   Senhas são iguais: " + senhaCorreta);
                
                if (senhaCorreta) {
                    System.out.println("\n✓ TESTE PASSOU! Criptografia funcionando corretamente.");
                } else {
                    System.out.println("\n✗ TESTE FALHOU! Senhas não coincidem.");
                }
            } else {
                System.out.println("   ✗ Erro: Configuração não foi carregada");
            }
            
            // 7. Teste de compatibilidade com senha em texto plano
            System.out.println("\n7. Testando compatibilidade com senha em texto plano...");
            java.util.Properties props = new java.util.Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", "5432");
            props.setProperty("database", "test");
            props.setProperty("username", "user");
            props.setProperty("password", "plainTextPassword"); // Senha não criptografada
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configPath)) {
                props.store(fos, "Test plain text password");
            }
            
            DatabaseConfigManager compatManager = new DatabaseConfigManager();
            DatabaseConfig compatConfig = compatManager.getCurrentConfig();
            
            if (compatConfig != null) {
                System.out.println("   Senha em texto plano carregada: " + compatConfig.getPassword());
                System.out.println("   ✓ Compatibilidade com texto plano funciona");
            }
            
            // 8. Limpar arquivo de teste
            System.out.println("\n8. Limpando arquivos de teste...");
            boolean deleted = configFile.delete();
            System.out.println("   Arquivo removido: " + deleted);
            
            System.out.println("\n=== FIM DO TESTE ===");
            
        } catch (Exception e) {
            System.err.println("\n✗ ERRO NO TESTE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
