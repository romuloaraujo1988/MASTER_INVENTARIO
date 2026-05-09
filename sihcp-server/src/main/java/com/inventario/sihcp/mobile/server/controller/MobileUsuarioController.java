package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.model.Usuario;
import com.inventario.sihcp.security.annotation.RequireAdmin;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import com.inventario.sihcp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador REST para operações de usuário mobile
 * 
 * Segurança por Role:
 * - GET /me: Qualquer usuário autenticado
 * - GET (listar): ADMIN apenas
 * - GET /{id}: ADMIN apenas
 * - GET /login/{login}: ADMIN apenas
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/usuarios")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
public class MobileUsuarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileUsuarioController.class);
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * Listar todos os usuários ativos
     * Requer role: ADMIN apenas
     * 
     * @return lista de usuários
     */
    @GetMapping
    @RequireAdmin
    public ResponseEntity<ApiResponse<List<Usuario>>> listarUsuarios() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Listando usuários para usuário: {}", username);
            
            List<Usuario> usuarios = usuarioService.listarTodos();
            
            // Remover senhas antes de enviar
            usuarios.forEach(u -> u.setSenhaHash(null));
            
            return ResponseEntity.ok(
                    ApiResponse.success(usuarios, 
                            String.format("%d usuário(s) encontrado(s)", usuarios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar usuários", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar usuários", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar usuário por ID
     * Requer role: ADMIN apenas
     * 
     * @param id ID do usuário
     * @return dados do usuário
     */
    @GetMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<ApiResponse<Usuario>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando usuário {} para usuário: {}", id, username);
            
            Usuario usuario = usuarioService.buscarPorId(id.longValue());
            
            if (usuario != null) {
                // Remover senha antes de enviar
                usuario.setSenhaHash(null);
                
                return ResponseEntity.ok(
                        ApiResponse.success(usuario, "Usuário encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuário não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar usuário", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar usuário por login
     * Requer role: ADMIN apenas
     * 
     * @param login login do usuário
     * @return dados do usuário
     */
    @GetMapping("/login/{login}")
    @RequireAdmin
    public ResponseEntity<ApiResponse<Usuario>> buscarPorLogin(@PathVariable String login) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando usuário por login {} para usuário: {}", login, username);
            
            Usuario usuario = usuarioService.buscarPorUsername(login);
            
            if (usuario != null) {
                // Remover senha antes de enviar
                usuario.setSenhaHash(null);
                
                return ResponseEntity.ok(
                        ApiResponse.success(usuario, "Usuário encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuário não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário por login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar usuário", "FETCH_ERROR"));
        }
    }
    
    /**
     * Obter perfil do usuário autenticado
     * Requer role: Qualquer usuário autenticado
     * 
     * @return dados do usuário logado
     */
    @GetMapping("/me")
    @RequireConsulta
    public ResponseEntity<ApiResponse<Usuario>> obterPerfilUsuario() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Obtendo perfil do usuário: {}", username);
            
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario != null) {
                // Remover senha antes de enviar
                usuario.setSenhaHash(null);
                
                return ResponseEntity.ok(
                        ApiResponse.success(usuario, "Perfil do usuário"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuário não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao obter perfil do usuário", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao obter perfil", "FETCH_ERROR"));
        }
    }
}
