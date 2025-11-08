package com.inventario.util;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.model.Usuario;
import java.util.List;

/**
 * Utilitário para migrar todas as senhas do banco para BCrypt
 */
public class MigrarSenhasParaBCrypt {
    
    public static void main(String[] args) {
        try {
            UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
            
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("MIGRAÇÃO DE SENHAS PARA BCRYPT");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println();
            
            // Buscar todos os usuários
            List<Usuario> usuarios = usuarioDAO.listarUsuarios();
            
            System.out.println("Total de usuários encontrados: " + usuarios.size());
            System.out.println();
            
            int migrados = 0;
            int erros = 0;
            int jaBCrypt = 0;
            
            for (Usuario usuario : usuarios) {
                String senhaAtual = usuario.getSenhaHash();
                
                // Verificar se já está em BCrypt
                if (senhaAtual != null && (senhaAtual.startsWith("$2a$") || 
                    senhaAtual.startsWith("$2b$") || senhaAtual.startsWith("$2y$"))) {
                    System.out.println("✓ " + usuario.getLogin() + " - Já está em BCrypt");
                    jaBCrypt++;
                    continue;
                }
                
                // Para usuários com senha antiga, vamos resetar para uma senha padrão
                // O usuário deverá alterar no primeiro acesso
                String novaSenha = "senha123"; // Senha padrão temporária
                String novaSenhaHash = PasswordUtil.hashPassword(novaSenha);
                
                boolean sucesso = usuarioDAO.atualizarSenha(usuario.getId(), novaSenhaHash);
                
                if (sucesso) {
                    System.out.println("✓ " + usuario.getLogin() + " - Migrado (senha resetada para: " + novaSenha + ")");
                    migrados++;
                    
                    // Marcar como primeiro acesso para forçar troca de senha
                    usuario.setPrimeiroAcesso(true);
                    usuarioDAO.atualizarUsuario(usuario);
                } else {
                    System.err.println("✗ " + usuario.getLogin() + " - ERRO ao migrar");
                    erros++;
                }
            }
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("RESUMO DA MIGRAÇÃO");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("Total de usuários: " + usuarios.size());
            System.out.println("Já em BCrypt: " + jaBCrypt);
            System.out.println("Migrados: " + migrados);
            System.out.println("Erros: " + erros);
            System.out.println();
            
            if (migrados > 0) {
                System.out.println("⚠️  IMPORTANTE:");
                System.out.println("   Os usuários migrados tiveram suas senhas resetadas para: senha123");
                System.out.println("   Eles deverão alterar a senha no primeiro acesso.");
            }
            
        } catch (Exception e) {
            System.err.println("Erro durante migração: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
