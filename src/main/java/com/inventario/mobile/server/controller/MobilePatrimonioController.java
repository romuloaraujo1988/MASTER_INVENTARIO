package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.service.MobilePatrimonioService;
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
 * Controlador REST para operações de patrimônio mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/patrimonio")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobilePatrimonioController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobilePatrimonioController.class);
    
    @Autowired
    private MobilePatrimonioService patrimonioService;
    
    /**
     * Buscar patrimônio por QR Code
     * 
     * @param qrCode código QR do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> buscarPorQRCode(@PathVariable String qrCode) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônio por QR Code: {} para usuário: {}", qrCode, username);
            
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorQRCode(qrCode);
            
            if (patrimonio != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônio por QR Code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônio por número
     * 
     * @param numero número do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> buscarPorNumero(@PathVariable String numero) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônio por número: {} para usuário: {}", numero, username);
            
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorNumero(numero);
            
            if (patrimonio != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônio por número", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônios por sala
     * 
     * @param salaId ID da sala
     * @return lista de patrimônios
     */
    @GetMapping("/sala/{salaId}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorSala(@PathVariable Integer salaId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios da sala {} para usuário: {}", salaId, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorSala(salaId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por sala", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônios por setor
     * 
     * @param setorId ID do setor
     * @return lista de patrimônios
     */
    @GetMapping("/setor/{setorId}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorSetor(@PathVariable Integer setorId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios do setor {} para usuário: {}", setorId, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorSetor(setorId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por setor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Listar todos os patrimônios (com paginação)
     * 
     * @param page página (padrão: 0)
     * @param size tamanho da página (padrão: 50)
     * @return lista de patrimônios
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> listarPatrimonios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Listando patrimônios (page: {}, size: {}) para usuário: {}", page, size, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.listarPatrimonios(page, size);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) carregado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar patrimônios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônio por ID
     * 
     * @param id ID do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônio {} para usuário: {}", id, username);
            
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorId(id);
            
            if (patrimonio != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônio por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio", "FETCH_ERROR"));
        }
    }

    /**
     * Buscar patrimônios por responsável com paginação e filtro de coleta
     * 
     * @param idResponsavel ID do responsável
     * @param page página (padrão: 0)
     * @param size tamanho da página (padrão: 50)
     * @param coletado filtro de status de coleta (opcional: true=coletados, false=não coletados, null=todos)
     * @return lista de patrimônios
     */
    @GetMapping("/responsavel/{idResponsavel}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorResponsavel(
            @PathVariable Integer idResponsavel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Boolean coletado) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios do responsável {} (page: {}, size: {}, coletado: {}) para usuário: {}", 
                       idResponsavel, page, size, coletado, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorResponsavel(idResponsavel, page, size, coletado);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por responsável", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }

    /**
     * Contar total de patrimônios por responsável
     * 
     * @param idResponsavel ID do responsável
     * @return total de patrimônios
     */
    @GetMapping("/responsavel/{idResponsavel}/count")
    public ResponseEntity<ApiResponse<Integer>> contarPatrimoniosPorResponsavel(@PathVariable Integer idResponsavel) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Contando patrimônios do responsável {} para usuário: {}", idResponsavel, username);
            
            int total = patrimonioService.contarPatrimoniosPorResponsavel(idResponsavel);
            
            return ResponseEntity.ok(
                    ApiResponse.success(total, 
                            String.format("Total de %d patrimônio(s) encontrado(s)", total)));
            
        } catch (Exception e) {
            logger.error("Erro ao contar patrimônios por responsável", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao contar patrimônios", "COUNT_ERROR"));
        }
    }
}
