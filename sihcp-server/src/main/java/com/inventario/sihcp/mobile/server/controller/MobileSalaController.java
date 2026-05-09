package com.inventario.sihcp.mobile.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.MobileSalaComProgressoDTO;
import com.inventario.sihcp.mobile.server.dto.MobileSalaDTO;
import com.inventario.sihcp.mobile.server.service.MobileSalaService;
import com.inventario.sihcp.security.annotation.RequireConsulta;

/**
 * Controlador REST para operações de sala mobile
 * 
 * Segurança por Role:
 * - Todos os endpoints: Qualquer usuário autenticado
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/salas")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
@RequireConsulta // Todos os endpoints de sala são acessíveis a qualquer usuário autenticado
public class MobileSalaController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSalaController.class);
    
    @Autowired
    private MobileSalaService salaService;
    
    /**
     * Listar TODAS as salas ativas (sem paginação)
     * Performance: <500ms para ~100 salas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MobileSalaDTO>>> listarSalas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Listando TODAS as salas para usuário: {}", username);
            
            long startTime = System.currentTimeMillis();
            
            // Buscar TODAS as salas de uma vez (sem paginação)
            List<MobileSalaDTO> salas = salaService.listarTodasSalas();
            
            long endTime = System.currentTimeMillis();
            
            logger.info("✓ Retornando {} salas em {}ms", salas.size(), (endTime - startTime));
            
            return ResponseEntity.ok(
                    ApiResponse.success(salas, 
                            String.format("%d sala(s) encontrada(s)", salas.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar salas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar salas: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * SINCRONIZAÇÃO INCREMENTAL de salas.
     * Retorna apenas salas modificadas após a data informada.
     * 
     * Estratégia:
     * - Se lastSync = 0 ou null: retorna TODAS as salas (sync inicial)
     * - Se lastSync > 0: retorna apenas salas modificadas/criadas após essa data
     * - Inclui lista de IDs de salas removidas/inativadas
     * 
     * @param lastSync Timestamp da última sincronização (milissegundos)
     * @return Salas novas/modificadas + IDs removidos
     */
    @GetMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sincronizarSalas(
            @RequestParam(required = false, defaultValue = "0") Long lastSync) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Sincronização incremental de salas para usuário: {}, lastSync: {}", 
                username, lastSync);
            
            long startTime = System.currentTimeMillis();
            
            Map<String, Object> resultado = new HashMap<>();
            
            if (lastSync == null || lastSync == 0) {
                // SYNC INICIAL: retorna todas as salas
                List<MobileSalaDTO> todasSalas = salaService.listarTodasSalas();
                resultado.put("salas", todasSalas);
                resultado.put("removidas", List.of());
                resultado.put("syncType", "FULL");
                resultado.put("totalSalas", todasSalas.size());
                resultado.put("serverTime", System.currentTimeMillis());
                
                logger.info("✓ Sync FULL: {} salas em {}ms", 
                    todasSalas.size(), (System.currentTimeMillis() - startTime));
            } else {
                // SYNC INCREMENTAL: apenas mudanças
                List<MobileSalaDTO> salasModificadas = salaService.buscarSalasModificadasApos(lastSync);
                List<Integer> salasRemovidas = salaService.buscarSalasInativadasApos(lastSync);
                
                resultado.put("salas", salasModificadas);
                resultado.put("removidas", salasRemovidas);
                resultado.put("syncType", "INCREMENTAL");
                resultado.put("totalModificadas", salasModificadas.size());
                resultado.put("totalRemovidas", salasRemovidas.size());
                resultado.put("serverTime", System.currentTimeMillis());
                
                logger.info("✓ Sync INCREMENTAL: {} modificadas, {} removidas em {}ms", 
                    salasModificadas.size(), salasRemovidas.size(), 
                    (System.currentTimeMillis() - startTime));
            }
            
            return ResponseEntity.ok(ApiResponse.success(resultado, "Sincronização concluída"));
            
        } catch (Exception e) {
            logger.error("Erro na sincronização de salas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro na sincronização: " + e.getMessage(), "SYNC_ERROR"));
        }
    }
    
    /**
     * Listar salas com estatísticas de progresso de coleta.
     * Retorna total de patrimônios, coletados, pendentes e percentual.
     * 
     * @param inventarioId ID do inventário (opcional, usa ativo se não informado)
     */
    @GetMapping("/com-progresso")
    public ResponseEntity<ApiResponse<List<MobileSalaComProgressoDTO>>> listarSalasComProgresso(
            @RequestParam(required = false) Integer inventarioId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Listando salas com progresso para usuário: {}, inventário: {}", 
                username, inventarioId != null ? inventarioId : "ATIVO");
            
            long startTime = System.currentTimeMillis();
            
            List<MobileSalaComProgressoDTO> salas = salaService.listarSalasComProgresso(inventarioId);
            
            long endTime = System.currentTimeMillis();
            
            logger.info("✓ Retornando {} salas com progresso em {}ms", salas.size(), (endTime - startTime));
            
            return ResponseEntity.ok(
                    ApiResponse.success(salas, 
                            String.format("%d sala(s) com progresso", salas.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao listar salas com progresso", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar salas: " + e.getMessage(), "FETCH_ERROR"));
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
