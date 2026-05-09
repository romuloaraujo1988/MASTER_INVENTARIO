package com.inventario.sihcp.service;

import com.inventario.sihcp.dao.UsuarioDAO;
import com.inventario.sihcp.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.SQLException;
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
    
    @Autowired(required = false)
    private UsuarioDAO usuarioDAO;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Construtor padrão para Spring
     */
    public UsuarioService() {
        // Spring irá injetar o UsuarioDAO via @Autowired
    }
    
    /**
     * Obtém ou cria instância do UsuarioDAO
     * Para aplicações Swing sem Spring
     */
    private UsuarioDAO getUsuarioDAO() {
        if (usuarioDAO == null) {
            logger.warn("UsuarioDAO não foi injetado pelo Spring, criando instância manual");
            usuarioDAO = new UsuarioDAO();
        }
        return usuarioDAO;
    }
    
    /**
     * Busca usuário por ID (Long)
     */
    public Usuario buscarPorId(Long id) {
        try {
            return getUsuarioDAO().findById(id.intValue());
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar usuário por ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Busca usuário por ID (Integer)
     */
    public Usuario buscarPorId(Integer id) {
        try {
            return getUsuarioDAO().findById(id);
        } catch (SQLException | RuntimeException e) {
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
            Usuario usuario = getUsuarioDAO().buscarPorLogin(username);
            if (usuario != null) {
                logger.info("Usuário encontrado - ID: {}, Login: '{}', Nome: {}, Ativo: {}", 
                           usuario.getId(), usuario.getLogin(), usuario.getNomeCompleto(), usuario.getAtivo());
            } else {
                logger.warn("Usuário '{}' NÃO encontrado no banco de dados", username);
            }
            return usuario;
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar usuário por login: {}", username, e);
            return null;
        }
    }
    
    /**
     * Busca usuário por email
     */
    public Usuario buscarPorEmail(String email) {
        try {
            return getUsuarioDAO().buscarPorEmail(email);
        } catch (SQLException | RuntimeException e) {
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
                getUsuarioDAO().update(usuario);
                logger.info("Usuário atualizado: {}", usuario.getLogin());
            } else {
                getUsuarioDAO().insert(usuario);
                logger.info("Usuário criado: {}", usuario.getLogin());
            }
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao salvar usuário: {}", usuario.getLogin(), e);
            throw new RuntimeException("Erro ao salvar usuário", e);
        }
    }
    
    /**
     * Lista todos os usuários (incluindo inativos)
     * CORRIGIDO: Agora lista TODOS os usuários, não apenas os ativos
     */
    public List<Usuario> listarTodos() {
        try {
            logger.info("========== LISTANDO TODOS OS USUÁRIOS ==========");
            logger.info("Tentando buscar usuários incluindo inativos...");
            
            List<Usuario> usuarios = getUsuarioDAO().findAllIncludingInactive();
            logger.info("Método findAllIncludingInactive() retornou {} usuários", usuarios.size());
            
            // Se não encontrou nenhum usuário, tentar apenas os ativos
            if (usuarios.isEmpty()) {
                logger.warn("Nenhum usuário encontrado com findAllIncludingInactive()");
                logger.info("Tentando buscar apenas usuários ativos...");
                usuarios = getUsuarioDAO().findAll();
                logger.info("Método findAll() retornou {} usuários", usuarios.size());
            }
            
            // Log detalhado dos usuários encontrados
            if (!usuarios.isEmpty()) {
                logger.info("Usuários encontrados:");
                for (Usuario u : usuarios) {
                    logger.info("  - ID: {}, Login: {}, Nome: {}, Ativo: {}", 
                        u.getId(), u.getLogin(), u.getNomeCompleto(), u.getAtivo());
                }
            } else {
                logger.error("NENHUM USUÁRIO ENCONTRADO NO BANCO DE DADOS!");
                logger.error("Verifique:");
                logger.error("  1. Se a tabela TABELA_USUARIO existe");
                logger.error("  2. Se há registros na tabela");
                logger.error("  3. Se a conexão com o banco está funcionando");
            }
            
            logger.info("========== FIM DA LISTAGEM ==========");
            return usuarios;
            
        } catch (SQLException | RuntimeException e) {
            logger.error("========== ERRO AO LISTAR USUÁRIOS ==========");
            logger.error("Tipo: {}", e.getClass().getName());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Stack trace:", e);
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
            return getUsuarioDAO().findAll();
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar usuários atualizados", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca usuários criados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Usuario> buscarCriadosApos(LocalDateTime lastSync) {
        // Por enquanto, retorna lista vazia
        // TODO: Implementar filtro por data de criação quando campo for adicionado
        return new ArrayList<>();
    }
    
    /**
     * Exclui usuário
     */
    public void excluir(int id) {
        try {
            getUsuarioDAO().delete(id);
        } catch (SQLException | RuntimeException e) {
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
            Usuario usuario = getUsuarioDAO().buscarPorLogin(login);
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
        } catch (SQLException | RuntimeException e) {
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
            Usuario usuario = getUsuarioDAO().findById(usuarioId);
            if (usuario == null) {
                logger.error("Usuário não encontrado: {}", usuarioId);
                return false;
            }
            
            // Criptografar nova senha
            String senhaEncriptada = passwordEncoder.encode(novaSenha);
            usuario.setSenhaHash(senhaEncriptada);
            
            // Atualizar no banco
            getUsuarioDAO().update(usuario);
            logger.info("Senha alterada com sucesso para usuário: {}", usuario.getLogin());
            
            return true;
        } catch (SQLException | RuntimeException e) {
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
            Usuario usuario = getUsuarioDAO().buscarPorLogin(login);
            if (usuario == null) {
                logger.error("Usuário não encontrado: {}", login);
                return false;
            }
            
            return alterarSenha(usuario.getId(), novaSenha);
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao alterar senha do usuário: {}", login, e);
            return false;
        }
    }
    
    /**
     * Atualiza senha do usuário (recebe hash já pronto)
     */
    public boolean atualizarSenha(Integer idUsuario, String senhaHash) {
        try {
            getUsuarioDAO().atualizarSenha(idUsuario, senhaHash);
            logger.info("Senha atualizada com sucesso para usuário ID: {}", idUsuario);
            return true;
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao atualizar senha do usuário ID: {}", idUsuario, e);
            return false;
        }
    }

    /**
     * Registra o acesso de um usuário (atualiza data do último acesso e reseta tentativas)
     * 
     * @param usuario usuário autenticado
     */
    public void registrarAcesso(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return;
        }
        
        try {
            usuario.registrarAcesso();
            usuario.marcarPrimeiroAcessoRealizado();
            getUsuarioDAO().update(usuario);
            logger.info("Acesso registrado para usuário: {} em {}", 
                        usuario.getLogin(), usuario.getDataUltimoAcesso());
        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao registrar acesso do usuário: {}", usuario.getLogin(), e);
            // Não lançar exceção para não interromper o fluxo de login
        }
    }
}