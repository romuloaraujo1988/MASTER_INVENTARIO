package com.inventario.mobile.server.controller;

import com.inventario.dao.InventarioDAO;
import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.model.Inventario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operações de inventário mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileInventarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileInventarioController.class);
    
    private final InventarioDAO inventarioDAO;
    
    public MobileInventarioController() {
        this.inventarioDAO = new InventarioDAO();
    }
    
    /**
     * Busca o inventário ativo (status EM_ANDAMENTO)
     * 
     * @return dados do inventário ativo
     */
    @GetMapping("/test/inventarios-ativos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obterInventarioAtivo() {
        try {
            logger.info("Buscando inventário ativo...");
            
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            
            Map<String, Object> response = new HashMap<>();
            
            if (inventarioAtivo != null) {
                logger.info("Inventário ativo encontrado: ID={}, Nome={}, Status={}", 
                        inventarioAtivo.getId(), inventarioAtivo.getNome(), inventarioAtivo.getStatusInventario());
                
                Map<String, Object> inventarioData = new HashMap<>();
                inventarioData.put("id", inventarioAtivo.getId());
                inventarioData.put("nome", inventarioAtivo.getNome());
                inventarioData.put("status", inventarioAtivo.getStatusInventario());
                inventarioData.put("dataInicio", inventarioAtivo.getDataInicio());
                inventarioData.put("dataFim", inventarioAtivo.getDataFim());
                inventarioData.put("percentualConclusao", inventarioAtivo.getPercentualConclusao());
                inventarioData.put("responsavel", inventarioAtivo.getResponsavelInventario());
                
                response.put("existeInventarioAtivo", true);
                response.put("inventarioAtivo", inventarioData);
                
                return ResponseEntity.ok(
                        ApiResponse.success(response, "Inventário ativo encontrado"));
            } else {
                logger.warn("Nenhum inventário ativo encontrado");
                
                response.put("existeInventarioAtivo", false);
                response.put("inventarioAtivo", null);
                
                return ResponseEntity.ok(
                        ApiResponse.success(response, "Nenhum inventário ativo no momento"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar inventário ativo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar inventário ativo: " + e.getMessage(), "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar inventário ativo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro inesperado: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
}
