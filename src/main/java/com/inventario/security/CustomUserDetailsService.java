package com.inventario.security;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço customizado para carregar detalhes do usuário
 * Integra Spring Security com o banco de dados local
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UsuarioDAORefactored usuarioDAO;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("=== CARREGANDO USUÁRIO PARA AUTENTICAÇÃO ===");
        logger.info("Username: {}", username);
        
        // Buscar usuário no banco de dados
        Usuario usuario;
        try {
            usuario = usuarioDAO.buscarPorLogin(username);
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário: {}", username, e);
            throw new UsernameNotFoundException("Erro ao buscar usuário: " + username);
        }
        logger.info("Resultado da busca: {}", usuario != null ? "Usuário encontrado" : "Usuário NÃO encontrado");
        
        if (usuario == null) {
            logger.error("Usuário não encontrado no banco: {}", username);
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        
        logger.info("Dados do usuário - ID: {}, Nome: {}, Ativo: {}, Bloqueado: {}, Perfil: {}", 
                   usuario.getId(), usuario.getNomeCompleto(), usuario.getAtivo(), 
                   usuario.getBloqueado(), usuario.getPerfil());
        
        // Verificar se o usuário está ativo
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            logger.error("Usuário inativo: {}", username);
            throw new UsernameNotFoundException("Usuário inativo: " + username);
        }
        
        // Verificar se o usuário está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            logger.error("Usuário bloqueado: {}", username);
            throw new UsernameNotFoundException("Usuário bloqueado: " + username);
        }
        
        // Criar lista de authorities baseada no perfil
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));
        
        logger.info("Authorities: {}", authorities);
        logger.info("Senha hash (primeiros 20 chars): {}", 
                   usuario.getSenhaHash() != null && usuario.getSenhaHash().length() > 20 
                   ? usuario.getSenhaHash().substring(0, 20) + "..." 
                   : usuario.getSenhaHash());
        
        // Retornar UserDetails do Spring Security
        UserDetails userDetails = User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(Boolean.TRUE.equals(usuario.getBloqueado()))
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(usuario.getAtivo()))
                .build();
        
        logger.info("=== USERDETAILS CRIADO COM SUCESSO ===");
        return userDetails;
    }
}
