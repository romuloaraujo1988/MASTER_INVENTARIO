package com.inventario.util;

import java.sql.*;

/**
 * Remove componentes TECLADO e MOUSE dos computadores
 */
public class RemoverComponentesComputador {
    
    public static void main(String[] args) {
        System.out.println("Removendo TECLADO e MOUSE dos computadores...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM tabela_item_composto WHERE tipo_componente IN ('TECLADO', 'MOUSE')";
            
            try (Statement stmt = conn.createStatement()) {
                int deleted = stmt.executeUpdate(sql);
                System.out.println("✓ Removidos: " + deleted + " componentes (TECLADO e MOUSE)");
            }
            
            // Mostrar resumo atual
            String sqlResumo = """
                SELECT tipo_componente, COUNT(*) as qtd 
                FROM tabela_item_composto 
                GROUP BY tipo_componente 
                ORDER BY qtd DESC
                """;
            
            System.out.println("\nResumo atual:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlResumo)) {
                while (rs.next()) {
                    System.out.println("  " + rs.getString("tipo_componente") + ": " + rs.getInt("qtd"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
