package com.inventario.service;

import com.inventario.model.Usuario;
import com.inventario.model.PerfilUsuario;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de autenticação e gerenciamento de usuários
 * Versão simplificada sem dependências externas
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class AutenticacaoService {

    // Simulação de banco de dados em memória - COM LIMITE para evitar vazamento
    private static final int MAX_USUARIOS_CACHE = 100;
    private static Map<String, Usuario> usuarios = new java.util.LinkedHashMap<String, Usuario>(MAX_USUARIOS_CACHE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Usuario> eldest) {
            // Nunca remover o admin
            if (size() > MAX_USUARIOS_CACHE && !"admin".equals(eldest.getKey())) {
                return true;
            }
            return false;
        }
    };
    private static final int MAX_TENTATIVAS_LOGIN = 5;
    
    // Constantes para usuário padrão
    private static final String ADMIN_USUARIO = "admin";
    private static final String ADMIN_SENHA = "admin123";
    
    static {
        criarUsuarioPadrao();
    }

    /**
     * Autentica um usuário
     * @param nomeUsuario Nome de usuário
     * @param senha Senha do usuário
     * @return Usuario autenticado ou null se falhou
     */
    public Usuario autenticar(String nomeUsuario, String senha) {
        if (nomeUsuario == null || senha == null || nomeUsuario.trim().isEmpty() || senha.trim().isEmpty()) {
            return null;
        }
        
        Usuario usuario = usuarios.get(nomeUsuario.trim());
        
        if (usuario == null) {
            System.out.println("Usuário não encontrado: " + nomeUsuario);
            return null;
        }
        
        // Verifica se o usuário está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            System.out.println("Usuário bloqueado: " + nomeUsuario);
            return null;
        }
        
        // Verifica se o usuário está ativo
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            System.out.println("Usuário inativo: " + nomeUsuario);
            return null;
        }
        
        // Verifica a senha
        if (!senha.equals(usuario.getSenhaHash())) {
            usuario.incrementarTentativasLogin();
            
            if (usuario.getTentativasLogin() >= MAX_TENTATIVAS_LOGIN) {
                usuario.bloquear();
                System.out.println("Usuário bloqueado por excesso de tentativas: " + nomeUsuario);
            } else {
                System.out.println("Senha incorreta para usuário: " + nomeUsuario + 
                                 " (Tentativa " + usuario.getTentativasLogin() + "/" + MAX_TENTATIVAS_LOGIN + ")");
            }
            return null;
        }
        
        // Autenticação bem-sucedida
        usuario.registrarAcesso();
        System.out.println("Usuário autenticado com sucesso: " + nomeUsuario);
        return usuario;
    }
    
    /**
     * Cria o usuário administrador padrão
     */
    private static void criarUsuarioPadrao() {
        Usuario admin = new Usuario(ADMIN_USUARIO, ADMIN_SENHA, "Administrador do Sistema",
                "admin@ifmt.edu.br", "00000000000", PerfilUsuario.ADMIN);
        admin.setId(1);
        
        usuarios.put(ADMIN_USUARIO, admin);
        System.out.println("Usuário padrão criado: " + ADMIN_USUARIO + " / " + ADMIN_SENHA);
    }
    
    /**
     * Desbloqueia um usuário
     * @param nomeUsuario Nome do usuário a ser desbloqueado
     * @return true se desbloqueado com sucesso
     */
    public boolean desbloquearUsuario(String nomeUsuario) {
        Usuario usuario = usuarios.get(nomeUsuario);
        if (usuario != null && Boolean.TRUE.equals(usuario.getBloqueado())) {
            usuario.desbloquear();
            System.out.println("Usuário desbloqueado: " + nomeUsuario);
            return true;
        }
        return false;
    }
    
    /**
     * Altera a senha de um usuário
     * @param nomeUsuario Nome do usuário
     * @param senhaAtual Senha atual
     * @param novaSenha Nova senha
     * @return true se alterada com sucesso
     */
    public boolean alterarSenha(String nomeUsuario, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarios.get(nomeUsuario);
        if (usuario != null && usuario.getSenhaHash().equals(senhaAtual)) {
            usuario.setSenhaHash(novaSenha);
            System.out.println("Senha alterada para usuário: " + nomeUsuario);
            return true;
        }
        return false;
    }
    
    /**
     * Cadastra um novo usuário
     * @param usuario Usuário a ser cadastrado
     * @return true se cadastrado com sucesso
     */
    public boolean cadastrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getLogin() == null || usuario.getLogin().trim().isEmpty()) {
            return false;
        }
        
        String nomeUsuario = usuario.getLogin().trim();
        if (usuarios.containsKey(nomeUsuario)) {
            System.out.println("Usuário já existe: " + nomeUsuario);
            return false;
        }
        
        usuarios.put(nomeUsuario, usuario);
        System.out.println("Usuário cadastrado: " + nomeUsuario);
        return true;
    }
    
    /**
     * Busca um usuário pelo nome de usuário
     * @param nomeUsuario Nome do usuário
     * @return Usuario encontrado ou null
     */
    public Usuario buscarUsuario(String nomeUsuario) {
        return usuarios.get(nomeUsuario);
    }
    
    /**
     * Lista todos os usuários cadastrados
     * @return Map com todos os usuários
     */
    public Map<String, Usuario> listarUsuarios() {
        return new HashMap<>(usuarios);
    }
    
    /**
     * Remove um usuário
     * @param nomeUsuario Nome do usuário a ser removido
     * @return true se removido com sucesso
     */
    public boolean removerUsuario(String nomeUsuario) {
        if (ADMIN_USUARIO.equals(nomeUsuario)) {
            System.out.println("Não é possível remover o usuário administrador padrão");
            return false;
        }
        
        Usuario removido = usuarios.remove(nomeUsuario);
        if (removido != null) {
            System.out.println("Usuário removido: " + nomeUsuario);
            return true;
        }
        return false;
    }
    
    /**
     * Ativa ou desativa um usuário
     * @param nomeUsuario Nome do usuário
     * @param ativo Status ativo
     * @return true se alterado com sucesso
     */
    public boolean alterarStatusUsuario(String nomeUsuario, boolean ativo) {
        Usuario usuario = usuarios.get(nomeUsuario);
        if (usuario != null) {
            usuario.setAtivo(ativo);
            System.out.println("Status do usuário " + nomeUsuario + " alterado para: " + (ativo ? "ativo" : "inativo"));
            return true;
        }
        return false;
    }
}