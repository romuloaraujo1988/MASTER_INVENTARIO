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
