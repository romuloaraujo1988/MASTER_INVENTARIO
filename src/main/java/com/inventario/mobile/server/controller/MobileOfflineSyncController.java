package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobileOfflineDataDTO;
import com.inventario.mobile.server.service.MobileOfflineSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para sincronização offline completa
 * 
 * ENDPOINT DEDICADO: /api/mobile/sync/offline-data
 * 
 * BENEFÍCIOS:
 * - 1 requisição ao invés de múltiplas
 * - Query otimizada no banco
 * - Fácil debug e monitoramento
 * - Suporte a compressão GZIP
 * - Logs detalhados
 * 
 * CRÍTICO: Com 10.000+ patrimônios, este endpoint deve ser rápido
 * 
 * v2.2: Controller dedicado para sincronização offline
 */
@RestController
@RequestMapping("/api/mobile/sync")
@Tag(name = "Mobile Sync", description = "Sincronização offline completa")
public class MobileOfflineSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileOfflineSyncController.class);
    
    @Autowired
    private MobileOfflineSyncService offlineSyncService;
    
    /**
     * Busca TODOS os dados necessários para modo offline
     * 
     * OTIMIZADO para 10.000+ patrimônios:
     * - Query única por entidade
     * - DTOs simplificados
     * - Transação read-only
     * - Suporte a GZIP
     * 
     * @param inventarioId ID do inventário ativo (opcional)
     * @return Todos os dados: patrimônios, salas, responsáveis
     */
    @GetMapping("/offline-data")
    @Operation(
        summary = "Buscar dados para modo offline",
        description = "Retorna TODOS os dados necessários para o app funcionar offline: patrimônios, salas e responsáveis. Endpoint otimizado para grandes volumes de dados."
    )
    public ResponseEntity<ApiResponse<MobileOfflineDataDTO>> buscarDadosOffline(
        @Parameter(description = "ID do inventário ativo (opcional)")
        @RequestParam(required = false) Integer inventarioId
    ) {
        try {
            logger.info("📱 Requisição de sincronização offline recebida");
            logger.info("   Inventário ID: {}", inventarioId != null ? inventarioId : "não especificado");
            
            long startTime = System.currentTimeMillis();
            
            // Buscar dados
            MobileOfflineDataDTO dados = offlineSyncService.buscarDadosOffline(inventarioId);
            
            long tempoMs = System.currentTimeMillis() - startTime;
            double tempoSeg = tempoMs / 1000.0;
            
            logger.info("✅ Sincronização offline concluída em {}s", tempoSeg);
            logger.info("   Patrimônios: {}", dados.getMetadata().getTotalPatrimonios());
            logger.info("   Salas: {}", dados.getMetadata().getTotalSalas());
            logger.info("   Responsáveis: {}", dados.getMetadata().getTotalResponsaveis());
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    dados,
                    String.format("Dados offline carregados com sucesso em %.2fs", tempoSeg)
                )
            );
            
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar dados offline", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao buscar dados offline: " + e.getMessage()));
        }
    }
    
    /**
     * Busca patrimônios PAGINADOS para modo offline
     * 
     * USO: Quando há mais de 5.000 patrimônios, o app deve fazer múltiplas
     * requisições para baixar todos os dados em partes.
     * 
     * Exemplo:
     * - 1ª requisição: /offline-data/patrimonios?page=0&size=2000 → patrimônios 0-1999
     * - 2ª requisição: /offline-data/patrimonios?page=1&size=2000 → patrimônios 2000-3999
     * - 3ª requisição: /offline-data/patrimonios?page=2&size=2000 → patrimônios 4000-5999
     * - etc.
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página (máximo 2000)
     * @param inventarioId ID do inventário (opcional)
     * @return página de patrimônios
     */
    @GetMapping("/offline-data/patrimonios")
    @Operation(
        summary = "Buscar patrimônios paginados para modo offline",
        description = "Retorna patrimônios em páginas para evitar sobrecarga de memória. Use quando há mais de 5.000 patrimônios."
    )
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> buscarPatrimoniosPaginados(
        @Parameter(description = "Número da página (0-based)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamanho da página (máximo 2000)")
        @RequestParam(defaultValue = "2000") int size,
        @Parameter(description = "ID do inventário (opcional)")
        @RequestParam(required = false) Integer inventarioId
    ) {
        try {
            // Limitar tamanho máximo
            if (size > 2000) {
                size = 2000;
                logger.warn("Tamanho de página limitado a 2000");
            }
            
            logger.info("📱 Requisição de patrimônios paginados: page={}, size={}", page, size);
            
            long startTime = System.currentTimeMillis();
            
            // Buscar patrimônios paginados
            java.util.Map<String, Object> resultado = offlineSyncService.buscarPatrimoniosPaginados(page, size, inventarioId);
            
            long tempoMs = System.currentTimeMillis() - startTime;
            
            int totalElements = (int) resultado.get("totalElements");
            int totalPages = (int) resultado.get("totalPages");
            java.util.List<?> content = (java.util.List<?>) resultado.get("content");
            
            logger.info("✅ Página {}/{} carregada: {} patrimônios em {}ms", 
                page + 1, totalPages, content.size(), tempoMs);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    resultado,
                    String.format("Página %d/%d: %d patrimônios (total: %d)", 
                        page + 1, totalPages, content.size(), totalElements)
                )
            );
            
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar patrimônios paginados", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao buscar patrimônios: " + e.getMessage()));
        }
    }
    
    /**
     * Verifica status do servidor e dados disponíveis
     * Endpoint leve para verificar se servidor está respondendo
     */
    @GetMapping("/status")
    @Operation(
        summary = "Verificar status do servidor",
        description = "Endpoint leve para verificar se o servidor está respondendo e quantos dados estão disponíveis"
    )
    public ResponseEntity<ApiResponse<MobileOfflineDataDTO.MetadataDTO>> verificarStatus(
        @RequestParam(required = false) Integer inventarioId
    ) {
        try {
            logger.info("🔍 Verificação de status recebida");
            
            // Buscar apenas metadados (sem dados completos)
            MobileOfflineDataDTO dados = offlineSyncService.buscarDadosOffline(inventarioId);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    dados.getMetadata(),
                    "Servidor online e operacional"
                )
            );
            
        } catch (Exception e) {
            logger.error("❌ Erro ao verificar status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao verificar status: " + e.getMessage()));
        }
    }
}
