package com.inventario.sihcp;

import com.inventario.sihcp.util.ConnectionManager;
import java.sql.Connection;
import java.sql.Statement;

public class FixSetor {
    public static void main(String[] args) {
        System.out.println("Applying schema changes to TABELA_SETOR...");
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            try {
                stmt.execute("ALTER TABLE TABELA_SETOR ADD COLUMN DESCRICAO TEXT");
                System.out.println("Column DESCRICAO added.");
            } catch (Exception e) {
                System.out.println("DESCRICAO might already exist: " + e.getMessage());
            }

            try {
                stmt.execute("ALTER TABLE TABELA_SETOR ADD COLUMN RESPONSAVEL_SETOR VARCHAR(255)");
                System.out.println("Column RESPONSAVEL_SETOR added.");
            } catch (Exception e) {
                System.out.println("RESPONSAVEL_SETOR might already exist: " + e.getMessage());
            }

            try {
                stmt.execute("ALTER TABLE TABELA_SETOR ADD COLUMN ATIVO BOOLEAN DEFAULT TRUE");
                System.out.println("Column ATIVO added.");
            } catch (Exception e) {
                System.out.println("ATIVO might already exist: " + e.getMessage());
            }

            System.out.println("Schema changes applied successfully!");
        } catch (Exception e) {
            System.err.println("Database connection error:");
            e.printStackTrace();
        }
    }
}
