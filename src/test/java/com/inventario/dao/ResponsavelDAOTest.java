package com.inventario.dao;

import com.inventario.model.Responsavel;
import java.util.List;

/**
 * Teste simples para verificar responsáveis
 */
public class ResponsavelDAOTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TESTE: Verificando Responsáveis");
        System.out.println("========================================");
        
        try {
            ResponsavelDAO dao = new ResponsavelDAO();
            
            // Teste 1: Buscar todos (com filtro ATIVO)
            System.out.println("\n1. Buscando responsáveis ativos (findAll):");
            List<Responsavel> ativos = dao.findAll();
            System.out.println("   Quantidade encontrada: " + ativos.size());
            
            if (ativos.isEmpty()) {
                System.out.println("   ⚠️ NENHUM responsável ativo encontrado!");
                
                // Teste 2: Verificar se existem responsáveis inativos
                System.out.println("\n2. Verificando responsáveis inativos:");
                String sqlInativos = "SELECT * FROM TABELA_RESPONSAVEL WHERE ATIVO = FALSE OR ATIVO IS NULL";
                List<Responsavel> inativos = dao.executeQuery(sqlInativos);
                System.out.println("   Quantidade de inativos: " + inativos.size());
                
                if (!inativos.isEmpty()) {
                    System.out.println("\n   📋 Responsáveis inativos encontrados:");
                    for (Responsavel r : inativos) {
                        System.out.println("      - ID: " + r.getId() + ", Nome: " + r.getNome() + ", Ativo: " + r.isAtivo());
                    }
                    
                    System.out.println("\n   💡 SOLUÇÃO: Execute o comando SQL:");
                    System.out.println("      UPDATE TABELA_RESPONSAVEL SET ATIVO = TRUE;");
                }
                
                // Teste 3: Verificar se a tabela está vazia
                System.out.println("\n3. Verificando se a tabela está vazia:");
                String sqlTotal = "SELECT * FROM TABELA_RESPONSAVEL";
                List<Responsavel> todos = dao.executeQuery(sqlTotal);
                System.out.println("   Total de registros na tabela: " + todos.size());
                
                if (todos.isEmpty()) {
                    System.out.println("\n   ⚠️ TABELA VAZIA!");
                    System.out.println("   💡 SOLUÇÃO: Insira responsáveis usando o script SQL:");
                    System.out.println("      INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, CARGO, ATIVO)");
                    System.out.println("      VALUES ('João Silva', '123.456.789-00', 'joao@ifmt.edu.br', 'Administrador', TRUE);");
                }
                
            } else {
                System.out.println("\n   ✅ Responsáveis ativos encontrados:");
                for (Responsavel r : ativos) {
                    System.out.println("      - ID: " + r.getId() + ", Nome: " + r.getNome() + ", Cargo: " + r.getCargo());
                }
            }
            
            System.out.println("\n========================================");
            System.out.println("TESTE CONCLUÍDO");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERRO ao executar teste:");
            e.printStackTrace();
        }
    }
}
