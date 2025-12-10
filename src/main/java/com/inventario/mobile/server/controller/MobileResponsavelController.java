package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobileResponsavelDTO;
import com.inventario.mobile.server.service.MobileResponsavelService;
import com.inventario.security.annotation.RequireConsulta;
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
 * Controlador REST para operações de responsável mobile
 * 
 * Segurança por Role:
 * - Todos os endpoints: Qualquer usuário autenticado
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/responsaveis")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequireConsulta // Todos os endpoints de responsável são acessíveis a qualquer usuário autenticado
public class MobileResponsavelController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileResponsavelController.class);
    
    @Autowired
    private MobileResponsavelService responsavelService;
    
    /**
     * Listar todos os responsáveis ativos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MobileResponsavelDTO>>> listarResponsaveis() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Listando responsáveis para usuário: {}", username);
            
            List<MobileResponsavelDTO> responsaveis = responsavelService.listarResponsaveis();
            
            return ResponseEntity.ok(
                    ApiResponse.success(responsaveis, 
                            String.format("%d responsável(is) encontrado(s)", responsaveis.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar responsáveis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar responsáveis", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar responsável por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileResponsavelDTO>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando responsável {} para usuário: {}", id, username);
            
            MobileResponsavelDTO responsavel = responsavelService.buscarPorId(id);
            
            if (responsavel != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(responsavel, "Responsável encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Responsável não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar responsável por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar responsável", "FETCH_ERROR"));
        }
    }
}
