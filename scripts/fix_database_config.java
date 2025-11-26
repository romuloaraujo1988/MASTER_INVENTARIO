///usr/bin/env jbang "$0" "$@" ; exit $?
// Execute com: java scripts/fix-database-config.java

import java.io.*;
import java.util.Properties;

public class fix_database_config {
    public static void main(String[] args) throws Exception {
        String configDir = System.getenv("LOCALAPPDATA") + "\\SIHCP-Inventario";
        String configPath = configDir + "\\database-config.properties";
        
        // Criar diretório se não existir
        new File(configDir).mkdirs();
        
        // Criar arquivo de propriedades
        Properties props = new Properties();
        props.setProperty("host", "localhost");
        props.setProperty("port", "5432");
        props.setProperty("database", "sispatrimonio");
        props.setProperty("username", "postgres");
        props.setProperty("password", "Romulo@2020");
        
        try (FileOutputStream fos = new FileOutputStream(configPath)) {
            props.store(fos, "Database Configuration - Fixed");
        }
        
        System.out.println("Arquivo criado: " + configPath);
        
        // Verificar
        Properties verify = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            verify.load(fis);
        }
        
        System.out.println("Verificação:");
        System.out.println("  host: " + verify.getProperty("host"));
        System.out.println("  port: " + verify.getProperty("port"));
        System.out.println("  database: " + verify.getProperty("database"));
        System.out.println("  username: " + verify.getProperty("username"));
        System.out.println("  password: " + (verify.getProperty("password") != null ? "PRESENTE" : "AUSENTE"));
    }
}
