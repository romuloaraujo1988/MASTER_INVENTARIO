package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.MobileInventarioDTO;
import com.inventario.sihcp.mobile.server.service.MobileInventarioService;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import com.inventario.sihcp.security.annotation.RequireSupervisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operações de inventário mobile
 * 
 * Segurança por Role:
 * - GET /ativo: Qualquer usuário autenticado
 * - GET /{id}: Qualquer usuário autenticado
 * - GET (listar): ADMIN ou SUPERVISOR
 * - GET /{id}/estatisticas: Qualquer usuário autenticado
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/inventario")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
public class MobileInventarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileInventarioController.class);
    
    @Autowired
    private MobileInventarioService inventarioService;
    
    /**
     * Busca o inventário ativo (em andamento)
     * GET /api/mobile/inventario/ativo
     * Requer role: Qualquer usuário autenticado
     */
    @GetMapping("/ativo")
    @RequireConsulta
    public ResponseEntity<ApiResponse<MobileInventarioDTO>> buscarInventarioAtivo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando inventário ativo para usuário: {}", username);
            
            MobileInventarioDTO inventario = inventarioService.buscarInventarioAtivo();
            
            if (inventario != null) {
                logger.info("✓ Inventário ativo encontrado: ID={}, Nome={}, Status={}", 
                        inventario.getId(), inventario.getNome(), inventario.getStatus());
                
                return ResponseEntity.ok(
                        ApiResponse.success(inventario, "Inventário ativo encontrado"));
            } else {
                logger.warn("Nenhum inventário ativo encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Nenhum inventário ativo encontrado", "NO_ACTIVE_INVENTORY"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário ativo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar inventário ativo: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca inventário por ID
     * GET /api/mobile/inventario/{id}
     * Requer role: Qualquer usuário autenticado
     */
    @GetMapping("/{id}")
    @RequireConsulta
    public ResponseEntity<ApiResponse<MobileInventarioDTO>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando inventário {} para usuário: {}", id, username);
            
            MobileInventarioDTO inventario = inventarioService.buscarPorId(id);
            
            if (inventario != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(inventario, "Inventário encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Inventário não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar inventário", "FETCH_ERROR"));
        }
    }
    
    /**
     * Lista todos os inventários
     * GET /api/mobile/inventario
     * Requer role: ADMIN ou SUPERVISOR
     */
    @GetMapping
    @RequireSupervisor
    public ResponseEntity<ApiResponse<List<MobileInventarioDTO>>> listarInventarios() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Listando inventários para usuário: {}", username);
            
            List<MobileInventarioDTO> inventarios = inventarioService.listarInventarios();
            
            return ResponseEntity.ok(
                    ApiResponse.success(inventarios, 
                            String.format("%d inventário(s) encontrado(s)", inventarios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar inventários", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar inventários", "FETCH_ERROR"));
        }
    }
    
    /**
     * Busca estatísticas do inventário
     * GET /api/mobile/inventario/{id}/estatisticas
     * Requer role: Qualquer usuário autenticado
     */
    @GetMapping("/{id}/estatisticas")
    @RequireConsulta
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarEstatisticas(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando estatísticas do inventário {} para usuário: {}", id, username);
            
            Map<String, Object> estatisticas = inventarioService.buscarEstatisticas(id);
            
            return ResponseEntity.ok(
                    ApiResponse.success(estatisticas, "Estatísticas carregadas"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Inventário não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar estatísticas", "FETCH_ERROR"));
        }
    }
}
