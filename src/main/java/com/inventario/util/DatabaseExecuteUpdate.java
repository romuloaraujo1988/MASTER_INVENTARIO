package com.inventario.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseExecuteUpdate {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java DatabaseExecuteUpdate \"<SQL_UPDATE_COMMAND>\"");
            return;
        }
        
        String sql = args[0];
        System.out.println("Executando comando: " + sql);
        System.out.println("=".repeat(80));
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int rowsAffected = stmt.executeUpdate();
            
            System.out.println("Comando executado com sucesso!");
            System.out.println("Linhas afetadas: " + rowsAffected);
            
        } catch (SQLException e) {
            System.err.println("Erro ao executar comando: " + e.getMessage());
            e.printStackTrace();
        }
    }
}