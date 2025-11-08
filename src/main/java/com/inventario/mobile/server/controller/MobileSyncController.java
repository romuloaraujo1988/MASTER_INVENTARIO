package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.dto.MobileSyncRequest;
import com.inventario.mobile.server.dto.MobileSyncResponse;
import com.inventario.mobile.server.service.MobileSyncService;
import com.inventario.mobile.server.service.MobilePatrimonioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para sincronização mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/sync")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSyncController.class);
    
    @Autowired
    private MobileSyncService mobileSyncService;
    
    /**
     * Endpoint principal de sincronização
     * 
     * @param syncRequest dados de sincronização
     * @return resposta da sincronização
     */
    @PostMapping("/data")
    public ResponseEntity<ApiResponse<MobileSyncResponse>> syncData(@Valid @RequestBody MobileSyncRequest syncRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Iniciando sincronização para usuário: {}", username);
            
            MobileSyncResponse syncResponse = mobileSyncService.syncData(syncRequest, username);
            
            return ResponseEntity.ok(
                ApiResponse.success(syncResponse, "Sincronização realizada com sucesso")
            );
            
        } catch (Exception e) {
            logger.error("Erro durante sincronização", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro na sincronização: " + e.getMessage(), "SYNC_ERROR"));
        }
    }
    
    /**
     * Endpoint para buscar patrimônios por setor
     * 
     * @param setorId ID do setor
     * @return lista de patrimônios
     */
    @GetMapping("/patrimonios/setor/{setorId}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> getPatrimoniosBySetor(@PathVariable Long setorId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios do setor {} para usuário: {}", setorId, username);
            
            // Implementar busca por setor
            // List<MobilePatrimonioDTO> patrimonios = mobileSyncService.buscarPorSetor(setorId);
            
            // Por enquanto retorna lista vazia
            return ResponseEntity.ok(
                ApiResponse.success(List.of(), "Patrimônios do setor carregados")
            );
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por setor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Endpoint para buscar patrimônios por sala
     * 
     * @param salaId ID da sala
     * @return lista de patrimônios
     */
    @GetMapping("/patrimonios/sala/{salaId}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> getPatrimoniosBySala(@PathVariable Long salaId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios da sala {} para usuário: {}", salaId, username);
            
            // Implementar busca por sala
            // List<MobilePatrimonioDTO> patrimonios = mobileSyncService.buscarPorSala(salaId);
            
            // Por enquanto retorna lista vazia
            return ResponseEntity.ok(
                ApiResponse.success(List.of(), "Patrimônios da sala carregados")
            );
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por sala", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Endpoint para buscar patrimônio por código
     * 
     * @param codigo código do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/patrimonio/{codigo}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> getPatrimonioByCodigo(@PathVariable String codigo) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônio {} para usuário: {}", codigo, username);
            
            // Buscar patrimônio por código usando MobilePatrimonioService
            MobilePatrimonioService patrimonioService = new MobilePatrimonioService();
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorNumero(codigo);
            
            if (patrimonio != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônio por código", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio", "FETCH_ERROR"));
        }
    }
    
    /**
     * Endpoint para atualizar status de coleta
     * 
     * @param patrimonioId ID do patrimônio
     * @param coletado status de coleta
     * @param observacoes observações
     * @return confirmação
     */
    @PutMapping("/patrimonio/{patrimonioId}/coleta")
    public ResponseEntity<ApiResponse<String>> updateColetaStatus(
            @PathVariable Long patrimonioId,
            @RequestParam Boolean coletado,
            @RequestParam(required = false) String observacoes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Atualizando status de coleta do patrimônio {} para usuário: {}", patrimonioId, username);
            
            // Implementar atualização de status
            // mobileSyncService.atualizarStatusColeta(patrimonioId, coletado, observacoes, username);
            
            return ResponseEntity.ok(
                ApiResponse.success("Status atualizado com sucesso", "Status de coleta atualizado")
            );
            
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de coleta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao atualizar status", "UPDATE_ERROR"));
        }
    }
}