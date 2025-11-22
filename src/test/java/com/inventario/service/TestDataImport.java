package com.inventario.service;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.SalaDAO;
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.UsuarioDAO;

/**
 * Teste simples para verificar se os DAOs estão retornando dados
 */
public class TestDataImport {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DE IMPORTAÇÃO ===\n");
        
        try {
            // Testar PatrimonioDAO
            System.out.println("1. Testando PatrimonioDAO...");
            PatrimonioDAO patrimonioDAO = new PatrimonioDAO();
            var patrimonios = patrimonioDAO.findAll("NUMERO");
            System.out.println("   ✅ Patrimônios encontrados: " + patrimonios.size());
            
            if (!patrimonios.isEmpty()) {
                var primeiro = patrimonios.get(0);
                System.out.println("   Exemplo: " + primeiro.getNumero() + " - " + primeiro.getDescricao());
            }
            
            // Testar SalaDAO
            System.out.println("\n2. Testando SalaDAO...");
            SalaDAO salaDAO = new SalaDAO();
            var salas = salaDAO.findAll("DESCRICAO");
            System.out.println("   ✅ Salas encontradas: " + salas.size());
            
            if (!salas.isEmpty()) {
                var primeira = salas.get(0);
                System.out.println("   Exemplo: " + primeira.getNumeroSala() + " - " + primeira.getDescricao());
            }
            
            // Testar ResponsavelDAO
            System.out.println("\n3. Testando ResponsavelDAO...");
            ResponsavelDAO responsavelDAO = new ResponsavelDAO();
            var responsaveis = responsavelDAO.findAll("NOME");
            System.out.println("   ✅ Responsáveis encontrados: " + responsaveis.size());
            
            if (!responsaveis.isEmpty()) {
                var primeiro = responsaveis.get(0);
                System.out.println("   Exemplo: " + primeiro.getNome() + " - " + primeiro.getCargo());
            }
            
            // Testar UsuarioDAO
            System.out.println("\n4. Testando UsuarioDAO...");
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            var usuarios = usuarioDAO.findAll("LOGIN");
            System.out.println("   ✅ Usuários encontrados: " + usuarios.size());
            
            if (!usuarios.isEmpty()) {
                var primeiro = usuarios.get(0);
                System.out.println("   Exemplo: " + primeiro.getLogin() + " - " + primeiro.getNomeCompleto());
            }
            
            System.out.println("\n=== TESTE CONCLUÍDO COM SUCESSO ===");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERRO NO TESTE:");
            e.printStackTrace();
        }
    }
}
