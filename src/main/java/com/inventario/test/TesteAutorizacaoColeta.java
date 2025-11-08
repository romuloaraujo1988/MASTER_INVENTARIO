package com.inventario.test;

import com.inventario.dao.ParticipanteInventarioDAO;
import com.inventario.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe para testar a autorização de coleta
 */
public class TesteAutorizacaoColeta {
    
    public static void main(String[] args) {
        System.out.println("=== Teste de Autorização de Coleta ===");
        
        // IDs para teste
        int idInventario = 2; // Inventário Anual 2025
        int idUsuario = 5;    // daniel.rezende
        
        System.out.println("Testando autorização para:");
        System.out.println("- ID Inventário: " + idInventario);
        System.out.println("- ID Usuário: " + idUsuario);
        System.out.println();
        
        // Teste 1: Verificar se o usuário é participante ativo
        testarParticipanteAtivo(idInventario, idUsuario);
        
        // Teste 2: Buscar ID do participante
        testarBuscarIdParticipante(idInventario, idUsuario);
        
        // Teste 3: Simular validação completa de coleta
        testarValidacaoCompleta(idInventario, idUsuario);
    }
    
    private static void testarParticipanteAtivo(int idInventario, int idUsuario) {
        System.out.println("1. Testando isParticipanteAtivo...");
        
        ParticipanteInventarioDAO dao = new ParticipanteInventarioDAO();
        boolean isAtivo = dao.isParticipanteAtivo(idInventario, idUsuario);
        
        System.out.println("   Resultado: " + (isAtivo ? "✅ AUTORIZADO" : "❌ NÃO AUTORIZADO"));
        System.out.println();
    }
    
    private static void testarBuscarIdParticipante(int idInventario, int idUsuario) {
        System.out.println("2. Testando buscarIdParticipantePorUsuario...");
        
        ParticipanteInventarioDAO dao = new ParticipanteInventarioDAO();
        Integer idParticipante = dao.buscarIdParticipantePorUsuario(idInventario, idUsuario);
        
        if (idParticipante != null) {
            System.out.println("   Resultado: ✅ ID Participante encontrado: " + idParticipante);
        } else {
            System.out.println("   Resultado: ❌ ID Participante NÃO encontrado");
        }
        System.out.println();
    }
    
    private static void testarValidacaoCompleta(int idInventario, int idUsuario) {
        System.out.println("3. Testando validação completa (simulando ColetaDAO)...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Simular a validação que é feita no ColetaDAO
            String sql = "SELECT COUNT(*) FROM TABELA_PARTICIPANTE_INVENTARIO " +
                        "WHERE ID_INVENTARIO = ? AND ID_USUARIO = ? AND ATIVO = TRUE";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idInventario);
                stmt.setInt(2, idUsuario);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        boolean autorizado = count > 0;
                        
                        System.out.println("   SQL executado: " + sql);
                        System.out.println("   Parâmetros: idInventario=" + idInventario + ", idUsuario=" + idUsuario);
                        System.out.println("   Count retornado: " + count);
                        System.out.println("   Resultado: " + (autorizado ? "✅ AUTORIZADO" : "❌ NÃO AUTORIZADO"));
                        
                        if (autorizado) {
                            System.out.println("   🎉 O usuário PODE realizar coletas neste inventário!");
                        } else {
                            System.out.println("   ⚠️  O usuário NÃO PODE realizar coletas neste inventário!");
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("   ❌ Erro na validação: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
}