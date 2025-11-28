package com.inventario.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utilitário para executar scripts SQL
 */
public class ExecutarScriptSQL {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java ExecutarScriptSQL <arquivo.sql>");
            System.exit(1);
        }
        
        String arquivo = args[0];
        
        try {
            System.out.println("Lendo arquivo: " + arquivo);
            StringBuilder sql = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    // Ignorar comentários
                    if (linha.trim().startsWith("--") || linha.trim().isEmpty()) {
                        continue;
                    }
                    sql.append(linha).append("\n");
                }
            }
            
            System.out.println("Conectando ao banco de dados...");
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("Executando script SQL...");
                stmt.execute(sql.toString());
                
                System.out.println("✓ Script executado com sucesso!");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao executar script: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
