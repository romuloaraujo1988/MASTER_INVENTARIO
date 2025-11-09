package com.inventario.service;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.model.PerfilUsuario;
import com.inventario.model.Usuario;
import java.time.LocalDateTime;

/**
 * Serviço de autenticação integrado com banco de dados
 * Implementa hash de senhas e controle de tentativas
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
public class AutenticacaoServiceDB {
    
    private UsuarioDAORefactored usuarioDAO;
    private static final int MAX_TENTATIVAS_LOGIN = 5;
    
    public AutenticacaoServiceDB() {
        this.usuarioDAO = new UsuarioDAORefactored();
    }
    
    /**
     * Autentica um usuário no sistema
     * @param login Login do usuário
     * @param senha Senha em texto plano
     * @return Usuario autenticado ou null se falhou
     */
    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null || login.trim().isEmpty() || senha.trim().isEmpty()) {
            return null;
        }
        
        // Buscar usuário no banco
        Usuario usuario;
        try {
            usuario = usuarioDAO.buscarPorLogin(login.trim());
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
            return null;
        }
        
        if (usuario == null) {
            System.out.println("Usuário não encontrado: " + login);
            return null;
        }
        
        // Verificar se o usuário está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            System.out.println("Usuário bloqueado: " + login);
            return null;
        }
        
        // Verificar se o usuário está ativo
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            System.out.println("Usuário inativo: " + login);
            return null;
        }
        
        // Verificar a senha usando hash seguro
        boolean senhaCorreta = false;
        try {
            // Tentar verificação com hash seguro primeiro
            senhaCorreta = com.inventario.util.PasswordUtil.verifyPassword(senha, usuario.getSenhaHash());
            
            // Se falhar, tentar verificação simples (compatibilidade com senhas antigas)
            if (!senhaCorreta) {
                senhaCorreta = com.inventario.util.PasswordUtil.verifyPasswordSimple(senha, usuario.getSenhaHash());
            }
            
            // Se ainda falhar, tentar comparação direta (compatibilidade total)
            if (!senhaCorreta) {
                senhaCorreta = senha.equals(usuario.getSenhaHash());
            }
        } catch (Exception e) {
            System.err.println("Erro na verificação de senha: " + e.getMessage());
            // Fallback para comparação direta
            senhaCorreta = senha.equals(usuario.getSenhaHash());
        }
        
        if (!senhaCorreta) {
            // Incrementar tentativas de login
            int tentativas = (usuario.getTentativasLogin() != null ? usuario.getTentativasLogin() : 0) + 1;
            usuario.setTentativasLogin(tentativas);
            
            if (tentativas >= MAX_TENTATIVAS_LOGIN) {
                // Bloquear usuário
                try {
                    usuarioDAO.bloquearUsuario(usuario.getId());
                } catch (Exception e) {
                    System.err.println("Erro ao bloquear usuário: " + e.getMessage());
                }
                System.out.println("Usuário bloqueado por excesso de tentativas: " + login);
            } else {
                // Atualizar tentativas no banco
                try {
                    usuarioDAO.update(usuario);
                } catch (Exception e) {
                    System.err.println("Erro ao atualizar tentativas de login: " + e.getMessage());
                }
                System.out.println("Senha incorreta para usuário: " + login + 
                                 " (Tentativa " + tentativas + "/" + MAX_TENTATIVAS_LOGIN + ")");
            }
            return null;
        }
        
        // Autenticação bem-sucedida
        usuario.setTentativasLogin(0);
        usuario.setDataUltimoAcesso(LocalDateTime.now());
        usuario.setPrimeiroAcesso(false);
        
        // Atualizar dados no banco
        try {
            usuarioDAO.update(usuario);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados do usuário: " + e.getMessage());
        }
        
        System.out.println("Usuário autenticado com sucesso: " + login);
        return usuario;
    }
    
    /**
     * Gera hash SHA-256 da senha (TEMPORARIAMENTE DESABILITADO)
     * @param senha Senha em texto plano
     * @return Senha em texto plano (sem hash)
     */
    public String gerarHashSenha(String senha) {
        try {
            return com.inventario.util.PasswordUtil.hashPassword(senha);
        } catch (Exception e) {
            System.err.println("Erro ao gerar hash da senha: " + e.getMessage());
            // Fallback para hash simples em caso de erro
            return com.inventario.util.PasswordUtil.hashPasswordSimple(senha);
        }
    }
    
    /**
     * Verifica se a senha atende aos critérios de segurança
     * @param senha Senha a ser verificada
     * @return true se a senha é válida
     */
    public boolean validarSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            return false;
        }
        
        // Verificar se contém pelo menos uma letra e um número
        boolean temLetra = false;
        boolean temNumero = false;
        
        for (char c : senha.toCharArray()) {
            if (Character.isLetter(c)) {
                temLetra = true;
            }
            if (Character.isDigit(c)) {
                temNumero = true;
            }
        }
        
        return temLetra && temNumero;
    }
    
    /**
     * Altera a senha de um usuário
     * @param usuarioId ID do usuário
     * @param senhaAtual Senha atual
     * @param novaSenha Nova senha
     * @return true se alterada com sucesso
     */
    public boolean alterarSenha(int usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario;
        try {
            usuario = usuarioDAO.findById(usuarioId);
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
            return false;
        }
        
        if (usuario == null) {
            return false;
        }
        
        // Verificar senha atual usando hash seguro
        boolean senhaAtualCorreta = false;
        try {
            senhaAtualCorreta = com.inventario.util.PasswordUtil.verifyPassword(senhaAtual, usuario.getSenhaHash());
            
            // Fallback para verificação simples
            if (!senhaAtualCorreta) {
                senhaAtualCorreta = com.inventario.util.PasswordUtil.verifyPasswordSimple(senhaAtual, usuario.getSenhaHash());
            }
            
            // Fallback para comparação direta
            if (!senhaAtualCorreta) {
                senhaAtualCorreta = senhaAtual.equals(usuario.getSenhaHash());
            }
        } catch (Exception e) {
            senhaAtualCorreta = senhaAtual.equals(usuario.getSenhaHash());
        }
        
        if (!senhaAtualCorreta) {
            return false;
        }
        
        // Validar nova senha
        if (!validarSenha(novaSenha)) {
            return false;
        }
        
        // Gerar hash da nova senha
        String novaSenhaHash = gerarHashSenha(novaSenha);
        
        // Atualizar senha com hash
        try {
            return usuarioDAO.atualizarSenha(usuarioId, novaSenhaHash);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar senha: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Desbloqueia um usuário
     * @param usuarioId ID do usuário
     * @return true se desbloqueado com sucesso
     */
    public boolean desbloquearUsuario(int usuarioId) {
        try {
            return usuarioDAO.desbloquearUsuario(usuarioId);
        } catch (Exception e) {
            System.err.println("Erro ao desbloquear usuário: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cria um usuário administrador padrão se não existir
     */
    public void criarUsuarioAdminPadrao() {
        Usuario admin;
        try {
            admin = usuarioDAO.buscarPorLogin("admin");
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário admin: " + e.getMessage());
            return;
        }
        
        if (admin == null) {
            admin = new Usuario();
            admin.setLogin("admin");
            admin.setSenhaHash(gerarHashSenha("admin123")); // Senha com hash seguro
            admin.setNomeCompleto("Administrador do Sistema");
            admin.setEmail("admin@ifmt.edu.br");
            admin.setMatricula("0000000");
            admin.setPerfil(PerfilUsuario.ADMIN);
            admin.setAtivo(true);
            admin.setBloqueado(false);
            admin.setPrimeiroAcesso(true);
            
            try {
                usuarioDAO.insert(admin);
                System.out.println("Usuário administrador padrão criado: admin / admin123");
            } catch (Exception e) {
                System.err.println("Erro ao criar usuário administrador padrão: " + e.getMessage());
            }
        }
    }
}