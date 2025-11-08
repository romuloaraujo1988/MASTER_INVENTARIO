package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobileSalaDTO;
import com.inventario.mobile.server.service.MobileSalaService;
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
 * Controlador REST para operações de sala mobile
 */
@RestController
@RequestMapping("/api/mobile/salas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileSalaController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSalaController.class);
    
    @Autowired
    private MobileSalaService salaService;
    
    /**
     * Listar todas as salas ativas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MobileSalaDTO>>> listarSalas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Listando salas para usuário: {}", username);
            
            List<MobileSalaDTO> salas = salaService.listarSalas();
            
            return ResponseEntity.ok(
                    ApiResponse.success(salas, 
                            String.format("%d sala(s) encontrada(s)", salas.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar salas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar salas", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar sala por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileSalaDTO>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando sala {} para usuário: {}", id, username);
            
            MobileSalaDTO sala = salaService.buscarPorId(id);
            
            if (sala != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(sala, "Sala encontrada"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sala não encontrada", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar sala por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar sala", "FETCH_ERROR"));
        }
    }
}
