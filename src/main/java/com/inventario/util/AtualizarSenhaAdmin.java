package com.inventario.util;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.model.Usuario;

/**
 * Utilitário para atualizar a senha do usuário admin para BCrypt
 */
public class AtualizarSenhaAdmin {
    
    public static void main(String[] args) {
        try {
            UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
            
            // Buscar usuário admin
            Usuario admin = usuarioDAO.buscarUsuarioPorLogin("admin");
            
            if (admin == null) {
                System.out.println("Usuário admin não encontrado!");
                return;
            }
            
            System.out.println("Usuário admin encontrado: ID=" + admin.getId());
            System.out.println("Senha atual (hash): " + admin.getSenhaHash());
            
            // Gerar novo hash BCrypt para a senha "admin123"
            String novaSenhaHash = PasswordUtil.hashPassword("admin123");
            System.out.println("Novo hash BCrypt: " + novaSenhaHash);
            
            // Atualizar senha no banco
            boolean sucesso = usuarioDAO.atualizarSenha(admin.getId(), novaSenhaHash);
            
            if (sucesso) {
                System.out.println("✓ Senha do admin atualizada com sucesso!");
                System.out.println("  Login: admin");
                System.out.println("  Senha: admin123");
                System.out.println("  Hash: BCrypt");
            } else {
                System.err.println("✗ Erro ao atualizar senha do admin");
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar senha: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
