package com.inventario.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTestQuery {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java DatabaseTestQuery \"<SQL_QUERY>\"");
            return;
        }
        
        String sql = args[0];
        System.out.println("Executando consulta: " + sql);
        System.out.println("=".repeat(80));
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            // Obter metadados das colunas
            int columnCount = rs.getMetaData().getColumnCount();
            
            // Imprimir cabeçalhos
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getMetaData().getColumnName(i));
                if (i < columnCount) System.out.print(" | ");
            }
            System.out.println();
            System.out.println("-".repeat(80));
            
            // Imprimir dados
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    System.out.print(value != null ? value.toString() : "NULL");
                    if (i < columnCount) System.out.print(" | ");
                }
                System.out.println();
            }
            
            System.out.println("-".repeat(80));
            System.out.println("Total de registros: " + rowCount);
            
        } catch (SQLException e) {
            System.err.println("Erro ao executar consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}