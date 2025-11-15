package com.inventario.service;

import com.inventario.dao.UsuarioDAO;
import com.inventario.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações com Usuário
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class UsuarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    
    @Autowired
    private UsuarioDAO usuarioDAO;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Busca usuário por ID (Long)
     */
    public Usuario buscarPorId(Long id) {
        try {
            return usuarioDAO.findById(id.intValue());
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário por ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Busca usuário por ID (Integer)
     */
    public Usuario buscarPorId(Integer id) {
        try {
            return usuarioDAO.findById(id);
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário por ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Busca usuário por login/username
     */
    public Usuario buscarPorUsername(String username) {
        try {
            logger.info("UsuarioService.buscarPorUsername - Buscando: '{}'", username);
            Usuario usuario = usuarioDAO.buscarPorLogin(username);
            if (usuario != null) {
                logger.info("Usuário encontrado - ID: {}, Login: '{}', Nome: {}, Ativo: {}", 
                           usuario.getId(), usuario.getLogin(), usuario.getNomeCompleto(), usuario.getAtivo());
            } else {
                logger.warn("Usuário '{}' NÃO encontrado no banco de dados", username);
            }
            return usuario;
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário por login: {}", username, e);
            return null;
        }
    }
    
    /**
     * Busca usuário por email
     */
    public Usuario buscarPorEmail(String email) {
        try {
            return usuarioDAO.buscarPorEmail(email);
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário por email: {}", email, e);
            return null;
        }
    }
    
    /**
     * Salva ou atualiza um usuário
     * IMPORTANTE: Criptografa a senha com BCrypt antes de salvar
     */
    public void salvar(Usuario usuario) {
        try {
            // Criptografar senha se foi fornecida e não está vazia
            if (usuario.getSenhaHash() != null && !usuario.getSenhaHash().isEmpty()) {
                // Verificar se a senha já está em formato BCrypt
                if (!usuario.getSenhaHash().startsWith("$2a$") && 
                    !usuario.getSenhaHash().startsWith("$2b$") && 
                    !usuario.getSenhaHash().startsWith("$2y$")) {
                    // Senha em texto plano, precisa criptografar
                    String senhaEncriptada = passwordEncoder.encode(usuario.getSenhaHash());
                    usuario.setSenhaHash(senhaEncriptada);
                    logger.debug("Senha criptografada com BCrypt para usuário: {}", usuario.getLogin());
                } else {
                    logger.debug("Senha já está em formato BCrypt para usuário: {}", usuario.getLogin());
                }
            }
            
            if (usuario.getId() != null && usuario.getId() > 0) {
                usuarioDAO.update(usuario);
                logger.info("Usuário atualizado: {}", usuario.getLogin());
            } else {
                usuarioDAO.insert(usuario);
                logger.info("Usuário criado: {}", usuario.getLogin());
            }
        } catch (Exception e) {
            logger.error("Erro ao salvar usuário: {}", usuario.getLogin(), e);
            throw new RuntimeException("Erro ao salvar usuário", e);
        }
    }
    
    /**
     * Lista todos os usuários
     */
    public List<Usuario> listarTodos() {
        try {
            return usuarioDAO.findAll();
        } catch (Exception e) {
            logger.error("Erro ao listar usuários", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca usuários atualizados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Usuario> buscarAtualizadosApos(LocalDateTime lastSync) {
        try {
            // Por enquanto, retorna todos os usuários
            // TODO: Implementar filtro por data de atualização quando campo for adicionado
            return usuarioDAO.findAll();
        } catch (Exception e) {
            logger.error("Erro ao buscar usuários atualizados", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca usuários criados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Usuario> buscarCriadosApos(LocalDateTime lastSync) {
        try {
            // Por enquanto, retorna lista vazia
            // TODO: Implementar filtro por data de criação quando campo for adicionado
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Erro ao buscar usuários criados", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Exclui usuário
     */
    public void excluir(int id) {
        try {
            usuarioDAO.delete(id);
        } catch (Exception e) {
            logger.error("Erro ao excluir usuário: {}", id, e);
            throw new RuntimeException("Erro ao excluir usuário", e);
        }
    }
    
    /**
     * Autentica usuário
     * NOTA: Este método compara senha em texto plano (legado)
     * Para autenticação com BCrypt, use o Spring Security
     */
    public boolean autenticar(String login, String senha) {
        try {
            Usuario usuario = usuarioDAO.buscarPorLogin(login);
            if (usuario == null) {
                return false;
            }
            
            // Verificar se a senha está em formato BCrypt
            if (usuario.getSenhaHash().startsWith("$2a$") || 
                usuario.getSenhaHash().startsWith("$2b$") || 
                usuario.getSenhaHash().startsWith("$2y$")) {
                // Usar BCrypt para verificar
                return passwordEncoder.matches(senha, usuario.getSenhaHash());
            } else {
                // Comparação legada (texto plano)
                logger.warn("Usuário {} tem senha em texto plano. Considere atualizar.", login);
                return usuario.getSenhaHash().equals(senha);
            }
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário: {}", login, e);
            return false;
        }
    }
    
    /**
     * Altera a senha de um usuário
     * Criptografa automaticamente com BCrypt
     * 
     * @param usuarioId ID do usuário
     * @param novaSenha nova senha em texto plano
     * @return true se alterada com sucesso
     */
    public boolean alterarSenha(Integer usuarioId, String novaSenha) {
        try {
            Usuario usuario = usuarioDAO.findById(usuarioId);
            if (usuario == null) {
                logger.error("Usuário não encontrado: {}", usuarioId);
                return false;
            }
            
            // Criptografar nova senha
            String senhaEncriptada = passwordEncoder.encode(novaSenha);
            usuario.setSenhaHash(senhaEncriptada);
            
            // Atualizar no banco
            usuarioDAO.update(usuario);
            logger.info("Senha alterada com sucesso para usuário: {}", usuario.getLogin());
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao alterar senha do usuário: {}", usuarioId, e);
            return false;
        }
    }
    
    /**
     * Altera a senha de um usuário por login
     * Criptografa automaticamente com BCrypt
     * 
     * @param login login do usuário
     * @param novaSenha nova senha em texto plano
     * @return true se alterada com sucesso
     */
    public boolean alterarSenhaPorLogin(String login, String novaSenha) {
        try {
            Usuario usuario = usuarioDAO.buscarPorLogin(login);
            if (usuario == null) {
                logger.error("Usuário não encontrado: {}", login);
                return false;
            }
            
            return alterarSenha(usuario.getId(), novaSenha);
        } catch (Exception e) {
            logger.error("Erro ao alterar senha do usuário: {}", login, e);
            return false;
        }
    }
    
    /**
     * Atualiza senha do usuário (recebe hash já pronto)
     */
    public boolean atualizarSenha(Integer idUsuario, String senhaHash) {
        try {
            usuarioDAO.atualizarSenha(idUsuario, senhaHash);
            logger.info("Senha atualizada com sucesso para usuário ID: {}", idUsuario);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao atualizar senha do usuário ID: {}", idUsuario, e);
            return false;
        }
    }
}