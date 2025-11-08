package com.inventario.test;

import com.inventario.dao.ColetorDAO;
import com.inventario.model.Coletor;
import com.inventario.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Classe para testar a inserção e busca de coletores
 */
public class TesteColetores {
    
    public static void main(String[] args) {
        System.out.println("=== Teste de Coletores ===");
        
        // Inserir coletores de teste
        inserirColetoresTeste();
        
        // Listar coletores
        listarColetores();
    }
    
    private static void inserirColetoresTeste() {
        System.out.println("\nInserindo coletores de teste...");
        
        String sql = "INSERT INTO TABELA_COLETOR (NOME_COLETOR, EMAIL, MATRICULA, CARGO, ATIVO) VALUES (?, ?, ?, ?, ?) ON CONFLICT (EMAIL) DO NOTHING";
        
        String[][] coletores = {
            {"João Silva", "joao.silva@ifmt.edu.br", "COL001", "Técnico em Patrimônio"},
            {"Maria Santos", "maria.santos@ifmt.edu.br", "COL002", "Assistente Administrativo"},
            {"Pedro Oliveira", "pedro.oliveira@ifmt.edu.br", "COL003", "Técnico de Laboratório"},
            {"Ana Costa", "ana.costa@ifmt.edu.br", "COL004", "Técnico em Informática"},
            {"Carlos Ferreira", "carlos.ferreira@ifmt.edu.br", "COL005", "Bibliotecário"}
        };
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (String[] coletor : coletores) {
                stmt.setString(1, coletor[0]); // NOME_COLETOR
                stmt.setString(2, coletor[1]); // EMAIL
                stmt.setString(3, coletor[2]); // MATRICULA
                stmt.setString(4, coletor[3]); // CARGO
                stmt.setBoolean(5, true);      // ATIVO
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    System.out.println("Coletor inserido: " + coletor[0]);
                } else {
                    System.out.println("Coletor já existe: " + coletor[0]);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao inserir coletores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void listarColetores() {
        System.out.println("\nListando coletores ativos...");
        
        ColetorDAO coletorDAO = new ColetorDAO();
        List<Coletor> coletores = coletorDAO.listarColetores();
        
        if (coletores.isEmpty()) {
            System.out.println("Nenhum coletor encontrado!");
        } else {
            System.out.println("Total de coletores: " + coletores.size());
            for (Coletor coletor : coletores) {
                System.out.println("ID: " + coletor.getId() + ", Nome: " + coletor.getNomeColetor() + ", Email: " + coletor.getEmail());
            }
        }
    }
}