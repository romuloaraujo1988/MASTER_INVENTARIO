package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.dto.MobileSalaDTO;
import com.inventario.mobile.server.service.MobilePatrimonioService;
import com.inventario.mobile.server.service.MobileSalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para sincronização de dados offline
 * Endpoints otimizados para download inicial de dados
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/mobile/sync")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileSyncController.class);
    
    @Autowired
    private MobilePatrimonioService patrimonioService;
    
    @Autowired
    private MobileSalaService salaService;
    
    /**
     * Endpoint otimizado para sincronização de patrimônios com paginação
     * GET /api/mobile/sync/patrimonios?page=0&size=100
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página (padrão: 100)
     * @return lista de patrimônios da página
     */
    @GetMapping("/patrimonios")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> sincronizarPatrimonios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("═══════════════════════════════════════════");
            logger.info("SYNC: Patrimônios solicitados");
            logger.info("Usuário: {}", username);
            logger.info("Page: {}, Size: {}", page, size);
            logger.info("═══════════════════════════════════════════");
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.listarPatrimonios(page, size);
            
            logger.info("✓ Retornando {} patrimônios (página {})", patrimonios.size(), page);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) na página %d", patrimonios.size(), page)));
            
        } catch (Exception e) {
            logger.error("✗ Erro ao sincronizar patrimônios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao sincronizar patrimônios: " + e.getMessage(), "SYNC_ERROR"));
        }
    }
    
    /**
     * Endpoint otimizado para sincronização de TODAS as salas
     * GET /api/mobile/sync/salas
     * 
     * Retorna todas as salas de uma vez (são apenas ~108)
     * 
     * @return lista completa de salas
     */
    @GetMapping("/salas")
    public ResponseEntity<ApiResponse<List<MobileSalaDTO>>> sincronizarSalas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("═══════════════════════════════════════════");
            logger.info("SYNC: Todas as salas solicitadas");
            logger.info("Usuário: {}", username);
            logger.info("═══════════════════════════════════════════");
            
            List<MobileSalaDTO> salas = salaService.listarTodasSalas();
            
            logger.info("✓ Retornando {} salas (todas)", salas.size());
            
            return ResponseEntity.ok(
                    ApiResponse.success(salas, 
                            String.format("%d sala(s) encontrada(s)", salas.size())));
            
        } catch (Exception e) {
            logger.error("✗ Erro ao sincronizar salas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao sincronizar salas: " + e.getMessage(), "SYNC_ERROR"));
        }
    }
    
    /**
     * Endpoint para obter estatísticas de sincronização
     * GET /api/mobile/sync/stats
     * 
     * @return estatísticas dos dados disponíveis
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obterEstatisticas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Obtendo estatísticas de sincronização para usuário: {}", username);
            
            Map<String, Object> stats = new HashMap<>();
            
            // Contar salas
            List<MobileSalaDTO> salas = salaService.listarTodasSalas();
            stats.put("totalSalas", salas.size());
            
            // Informações sobre patrimônios (primeira página para estimar)
            List<MobilePatrimonioDTO> primeirasPagina = patrimonioService.listarPatrimonios(0, 100);
            stats.put("patrimoniosPorPagina", 100);
            stats.put("primeiraPagemPatrimonios", primeirasPagina.size());
            
            // Estimativa de páginas (se primeira página tem 100, há mais)
            if (primeirasPagina.size() == 100) {
                stats.put("estimativaPaginas", "100+");
            } else {
                stats.put("estimativaPaginas", 1);
            }
            
            logger.info("Estatísticas: {} salas, {} patrimônios na primeira página", 
                    salas.size(), primeirasPagina.size());
            
            return ResponseEntity.ok(
                    ApiResponse.success(stats, "Estatísticas obtidas com sucesso"));
            
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao obter estatísticas: " + e.getMessage(), "STATS_ERROR"));
        }
    }
}
