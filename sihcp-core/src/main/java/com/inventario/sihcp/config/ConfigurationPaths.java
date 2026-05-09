package com.inventario.sihcp.config;

import java.io.File;

/**
 * Gerenciador de caminhos de configuração para diferentes ambientes
 * 
 * Define onde os arquivos de configuração serão salvos em:
 * - Desenvolvimento (máquina do desenvolvedor)
 * - Produção (servidor ou máquina do cliente)
 * - Portátil (junto com a aplicação)
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class ConfigurationPaths {
    
    /**
     * Modo de configuração
     */
    public enum ConfigMode {
        /**
         * Modo usuário: ~/.inventario/
         * Configuração específica por usuário do sistema operacional
         */
        USER_HOME,
        
        /**
         * Modo aplicação: ./config/
         * Configuração junto com a aplicação (portátil)
         */
        APP_DIR,
        
        /**
         * Modo sistema: /etc/inventario/ (Linux) ou C:\ProgramData\inventario\ (Windows)
         * Configuração compartilhada entre todos os usuários
         */
        SYSTEM,
        
        /**
         * Modo customizado: caminho definido por variável de ambiente
         * INVENTARIO_CONFIG_DIR
         */
        CUSTOM
    }
    
    // Modo atual (pode ser alterado via variável de ambiente ou propriedade do sistema)
    private static ConfigMode currentMode;
    
    // Nomes dos arquivos
    private static final String DATABASE_CONFIG_FILE = "database-config.properties";
    private static final String SGBD_CONFIG_FILE = "sgbd-config.properties";
    
    static {
        // Determinar modo baseado em variáveis de ambiente ou propriedades
        currentMode = detectConfigMode();
    }
    
    /**
     * Detecta o modo de configuração baseado em variáveis de ambiente
     */
    private static ConfigMode detectConfigMode() {
        // 1. Verificar variável de ambiente INVENTARIO_CONFIG_MODE
        String envMode = System.getenv("INVENTARIO_CONFIG_MODE");
        if (envMode != null) {
            try {
                return ConfigMode.valueOf(envMode.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Modo de configuração inválido: " + envMode);
            }
        }
        
        // 2. Verificar propriedade do sistema
        String sysMode = System.getProperty("inventario.config.mode");
        if (sysMode != null) {
            try {
                return ConfigMode.valueOf(sysMode.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Modo de configuração inválido: " + sysMode);
            }
        }
        
        // 3. Verificar se existe diretório config/ junto com a aplicação
        File appConfigDir = new File("config");
        if (appConfigDir.exists() && appConfigDir.isDirectory()) {
            System.out.println("Detectado diretório config/ - usando modo APP_DIR");
            return ConfigMode.APP_DIR;
        }
        
        // 4. Padrão: USER_HOME
        return ConfigMode.USER_HOME;
    }
    
    /**
     * Obtém o diretório de configuração baseado no modo atual
     */
    public static String getConfigDirectory() {
        switch (currentMode) {
            case USER_HOME:
                // Windows: C:\Users\[usuario]\AppData\Local\SIHCP-Inventario
                // Linux/Mac: ~/.config/sihcp-inventario (padrão XDG)
                if (isWindows()) {
                    String localAppData = System.getenv("LOCALAPPDATA");
                    if (localAppData != null && !localAppData.isEmpty()) {
                        return localAppData + File.separator + "SIHCP-Inventario";
                    }
                    // Fallback para Documents se LOCALAPPDATA não existir
                    return System.getProperty("user.home") + File.separator + "Documents" + 
                           File.separator + "SIHCP-Inventario";
                } else {
                    // Linux/Mac: seguir padrão XDG
                    String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
                    if (xdgConfigHome != null && !xdgConfigHome.isEmpty()) {
                        return xdgConfigHome + File.separator + "sihcp-inventario";
                    }
                    return System.getProperty("user.home") + File.separator + ".config" + 
                           File.separator + "sihcp-inventario";
                }
                
            case APP_DIR:
                return "config";
                
            case SYSTEM:
                if (isWindows()) {
                    return System.getenv("ProgramData") + File.separator + "SIHCP-Inventario";
                } else {
                    return "/etc/sihcp-inventario";
                }
                
            case CUSTOM:
                String customDir = System.getenv("INVENTARIO_CONFIG_DIR");
                if (customDir != null && !customDir.isEmpty()) {
                    return customDir;
                }
                // Fallback para USER_HOME se variável não definida
                if (isWindows()) {
                    String localAppData = System.getenv("LOCALAPPDATA");
                    if (localAppData != null) {
                        return localAppData + File.separator + "SIHCP-Inventario";
                    }
                    return System.getProperty("user.home") + File.separator + "Documents" + 
                           File.separator + "SIHCP-Inventario";
                } else {
                    return System.getProperty("user.home") + File.separator + ".config" + 
                           File.separator + "sihcp-inventario";
                }
                
            default:
                if (isWindows()) {
                    String localAppData = System.getenv("LOCALAPPDATA");
                    if (localAppData != null) {
                        return localAppData + File.separator + "SIHCP-Inventario";
                    }
                    return System.getProperty("user.home") + File.separator + "Documents" + 
                           File.separator + "SIHCP-Inventario";
                } else {
                    return System.getProperty("user.home") + File.separator + ".config" + 
                           File.separator + "sihcp-inventario";
                }
        }
    }
    
    /**
     * Obtém o caminho completo do arquivo de configuração do banco
     */
    public static String getDatabaseConfigPath() {
        return getConfigDirectory() + File.separator + DATABASE_CONFIG_FILE;
    }
    
    /**
     * Obtém o caminho completo do arquivo de configuração do SGBD
     */
    public static String getSGBDConfigPath() {
        return getConfigDirectory() + File.separator + SGBD_CONFIG_FILE;
    }
    
    /**
     * Define o modo de configuração manualmente
     */
    public static void setConfigMode(ConfigMode mode) {
        currentMode = mode;
        System.out.println("Modo de configuração alterado para: " + mode);
    }
    
    /**
     * Obtém o modo de configuração atual
     */
    public static ConfigMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Verifica se está rodando no Windows
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * Cria o diretório de configuração se não existir
     */
    public static boolean createConfigDirectoryIfNotExists() {
        File configDir = new File(getConfigDirectory());
        
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (created) {
                System.out.println("✓ Diretório de configuração criado: " + configDir.getAbsolutePath());
                return true;
            } else {
                System.err.println("✗ Falha ao criar diretório de configuração: " + configDir.getAbsolutePath());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Obtém informações sobre a configuração atual
     */
    public static String getConfigInfo() {
        StringBuilder info = new StringBuilder();
        info.append("╔════════════════════════════════════════════════════════════════╗\n");
        info.append("║  Configuração de Caminhos                                      ║\n");
        info.append("╠════════════════════════════════════════════════════════════════╣\n");
        info.append(String.format("║  Modo: %-56s ║\n", currentMode));
        info.append(String.format("║  Diretório: %-51s ║\n", truncate(getConfigDirectory(), 51)));
        info.append(String.format("║  Banco: %-54s ║\n", truncate(DATABASE_CONFIG_FILE, 54)));
        info.append(String.format("║  SGBD: %-55s ║\n", truncate(SGBD_CONFIG_FILE, 55)));
        info.append("╠════════════════════════════════════════════════════════════════╣\n");
        info.append("║  Variáveis de Ambiente:                                        ║\n");
        info.append(String.format("║  INVENTARIO_CONFIG_MODE: %-38s ║\n", 
            truncate(System.getenv("INVENTARIO_CONFIG_MODE"), 38)));
        info.append(String.format("║  INVENTARIO_CONFIG_DIR: %-39s ║\n", 
            truncate(System.getenv("INVENTARIO_CONFIG_DIR"), 39)));
        info.append("╚════════════════════════════════════════════════════════════════╝\n");
        
        return info.toString();
    }
    
    /**
     * Trunca string para caber no tamanho especificado
     */
    private static String truncate(String str, int maxLength) {
        if (str == null) return "null";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Migra configurações antigas da pasta .inventario para nova localização
     * 
     * @return true se migrou com sucesso ou não havia nada para migrar
     */
    public static boolean migrateOldConfiguration() {
        try {
            // Localização antiga
            String oldDir = System.getProperty("user.home") + File.separator + ".inventario";
            File oldDirectory = new File(oldDir);
            
            // Se não existe pasta antiga, não precisa migrar
            if (!oldDirectory.exists()) {
                return true;
            }
            
            System.out.println("⚠ Detectada configuração antiga em: " + oldDir);
            
            // Localização nova
            String newDir = getConfigDirectory();
            File newDirectory = new File(newDir);
            
            // Criar diretório novo se não existir
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
            
            // Migrar arquivos
            boolean migrated = false;
            
            // Migrar database-config.properties
            File oldDbConfig = new File(oldDir + File.separator + "database-config.properties");
            File newDbConfig = new File(newDir + File.separator + "database-config.properties");
            if (oldDbConfig.exists() && !newDbConfig.exists()) {
                copyFile(oldDbConfig, newDbConfig);
                System.out.println("✓ Migrado: database-config.properties");
                migrated = true;
            }
            
            // Migrar sgbd-config.properties
            File oldSgbdConfig = new File(oldDir + File.separator + "sgbd-config.properties");
            File newSgbdConfig = new File(newDir + File.separator + "sgbd-config.properties");
            if (oldSgbdConfig.exists() && !newSgbdConfig.exists()) {
                copyFile(oldSgbdConfig, newSgbdConfig);
                System.out.println("✓ Migrado: sgbd-config.properties");
                migrated = true;
            }
            
            if (migrated) {
                System.out.println("✓ Migração concluída!");
                System.out.println("  Nova localização: " + newDir);
                System.out.println("  Você pode deletar a pasta antiga: " + oldDir);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao migrar configuração: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Copia um arquivo
     */
    private static void copyFile(File source, File dest) throws Exception {
        java.nio.file.Files.copy(
            source.toPath(), 
            dest.toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );
    }
    
    /**
     * Exemplo de uso e teste
     */
    public static void main(String[] args) {
        System.out.println("=== Teste de Caminhos de Configuração ===\n");
        
        // Tentar migrar configuração antiga
        migrateOldConfiguration();
        System.out.println();
        
        // Mostrar configuração atual
        System.out.println(getConfigInfo());
        
        // Testar diferentes modos
        System.out.println("\n=== Testando Diferentes Modos ===\n");
        
        for (ConfigMode mode : ConfigMode.values()) {
            setConfigMode(mode);
            System.out.println("Modo: " + mode);
            System.out.println("  Diretório: " + getConfigDirectory());
            System.out.println("  Banco: " + getDatabaseConfigPath());
            System.out.println("  SGBD: " + getSGBDConfigPath());
            System.out.println();
        }
        
        // Restaurar modo original
        currentMode = detectConfigMode();
        System.out.println("Modo restaurado: " + currentMode);
    }
}
