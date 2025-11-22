package com.inventario.test;

import com.inventario.dao.SalaDAO;
import com.inventario.model.Sala;
import com.inventario.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Teste para verificar por que apenas 11 salas são importadas
 */
public class TestSalaImport {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  TESTE DE IMPORTAÇÃO DE SALAS");
        System.out.println("========================================");
        System.out.println();
        
        try {
            // Teste 1: Contar registros direto no PostgreSQL
            System.out.println("TESTE 1: Contagem direta no PostgreSQL");
            System.out.println("---------------------------------------");
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Total absoluto
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM TABELA_SALA");
                if (rs1.next()) {
                    System.out.println("Total de registros na TABELA_SALA: " + rs1.getInt(1));
                }
                rs1.close();
                
                // Por status ATIVO
                ResultSet rs2 = stmt.executeQuery(
                    "SELECT ATIVO, COUNT(*) FROM TABELA_SALA GROUP BY ATIVO ORDER BY ATIVO DESC"
                );
                System.out.println("\nDistribuição por ATIVO:");
                while (rs2.next()) {
                    Boolean ativo = rs2.getBoolean(1);
                    int count = rs2.getInt(2);
                    System.out.println("  ATIVO = " + ativo + ": " + count + " sala(s)");
                }
                rs2.close();
                
                // Com LEFT JOIN (query do listarTodasSalas)
                ResultSet rs3 = stmt.executeQuery(
                    "SELECT COUNT(*) FROM TABELA_SALA s LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID"
                );
                if (rs3.next()) {
                    System.out.println("\nTotal com LEFT JOIN (query do listarTodasSalas): " + rs3.getInt(1));
                }
                rs3.close();
            }
            
            System.out.println();
            
            // Teste 2: Usar SalaDAO.findAll()
            System.out.println("TESTE 2: SalaDAO.findAll()");
            System.out.println("---------------------------------------");
            SalaDAO salaDAO = new SalaDAO();
            
            try {
                List<Sala> salasAll = salaDAO.findAll();
                System.out.println("findAll() retornou: " + salasAll.size() + " sala(s)");
                
                if (!salasAll.isEmpty()) {
                    System.out.println("\nPrimeiras 5 salas:");
                    for (int i = 0; i < Math.min(5, salasAll.size()); i++) {
                        Sala s = salasAll.get(i);
                        System.out.println("  " + (i+1) + ". ID=" + s.getIdSala() + 
                                         ", Número=" + s.getNumeroSala() + 
                                         ", Ativo=" + s.getAtivo());
                    }
                }
            } catch (Exception e) {
                System.err.println("ERRO ao executar findAll(): " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println();
            
            // Teste 3: Usar SalaDAO.listarTodasSalas()
            System.out.println("TESTE 3: SalaDAO.listarTodasSalas()");
            System.out.println("---------------------------------------");
            
            try {
                List<Sala> salasTodas = salaDAO.listarTodasSalas();
                System.out.println("listarTodasSalas() retornou: " + salasTodas.size() + " sala(s)");
                
                if (!salasTodas.isEmpty()) {
                    System.out.println("\nPrimeiras 5 salas:");
                    for (int i = 0; i < Math.min(5, salasTodas.size()); i++) {
                        Sala s = salasTodas.get(i);
                        System.out.println("  " + (i+1) + ". ID=" + s.getIdSala() + 
                                         ", Número=" + s.getNumeroSala() + 
                                         ", Ativo=" + s.getAtivo());
                    }
                }
            } catch (Exception e) {
                System.err.println("ERRO ao executar listarTodasSalas(): " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println();
            
            // Teste 4: Usar SalaDAO.listarSalas() (apenas ativas)
            System.out.println("TESTE 4: SalaDAO.listarSalas() (apenas ativas)");
            System.out.println("---------------------------------------");
            
            try {
                List<Sala> salasAtivas = salaDAO.listarSalas();
                System.out.println("listarSalas() retornou: " + salasAtivas.size() + " sala(s) ativa(s)");
                
                if (!salasAtivas.isEmpty()) {
                    System.out.println("\nPrimeiras 5 salas ativas:");
                    for (int i = 0; i < Math.min(5, salasAtivas.size()); i++) {
                        Sala s = salasAtivas.get(i);
                        System.out.println("  " + (i+1) + ". ID=" + s.getIdSala() + 
                                         ", Número=" + s.getNumeroSala() + 
                                         ", Ativo=" + s.getAtivo());
                    }
                }
            } catch (Exception e) {
                System.err.println("ERRO ao executar listarSalas(): " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println();
            System.out.println("========================================");
            System.out.println("  TESTE CONCLUÍDO");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("ERRO FATAL no teste:");
            e.printStackTrace();
        }
    }
}
