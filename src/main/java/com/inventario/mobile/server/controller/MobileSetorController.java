package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.model.Setor;
import com.inventario.dao.SetorDAO;
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
 * Controlador REST para operações de setor mobile
 * 
 * Segurança por Role:
 * - Todos os endpoints: Qualquer usuário autenticado
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/setores")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequireConsulta // Todos os endpoints de setor são acessíveis a qualquer usuário autenticado
public class MobileSetorController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSetorController.class);
    
    @Autowired
    private SetorDAO setorDAO;
    
    /**
     * Listar todos os setores ativos
     * 
     * @return lista de setores
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Setor>>> listarSetores() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Listando setores para usuário: {}", username);
            
            List<Setor> setores = setorDAO.findAll("NOME");
            
            return ResponseEntity.ok(
                    ApiResponse.success(setores, 
                            String.format("%d setor(es) encontrado(s)", setores.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar setores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar setores", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar setor por ID
     * 
     * @param id ID do setor
     * @return dados do setor
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Setor>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando setor {} para usuário: {}", id, username);
            
            Setor setor = setorDAO.findById(id);
            
            if (setor != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(setor, "Setor encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Setor não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar setor por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar setor", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar setores por campus
     * 
     * @param campusId ID do campus
     * @return lista de setores
     */
    @GetMapping("/campus/{campusId}")
    public ResponseEntity<ApiResponse<List<Setor>>> buscarPorCampus(@PathVariable Integer campusId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando setores do campus {} para usuário: {}", campusId, username);
            
            List<Setor> setores = setorDAO.listarSetoresPorCampus(campusId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(setores, 
                            String.format("%d setor(es) encontrado(s)", setores.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar setores por campus", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar setores", "FETCH_ERROR"));
        }
    }
}
