package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.service.MemoryCleanupService;
import com.inventario.service.MemoryMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para monitoramento e gerenciamento de memória
 * 
 * Endpoints:
 * - GET /api/mobile/memory/status - Status atual da memória
 * - POST /api/mobile/memory/cleanup - Força limpeza de memória
 * - POST /api/mobile/memory/gc - Força Garbage Collection
 * 
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/memory")
public class MobileMemoryController {

    private static final Logger logger = LoggerFactory.getLogger(MobileMemoryController.class);
    
    @Autowired(required = false)
    private MemoryCleanupService memoryCleanupService;
    
    @Autowired(required = false)
    private MemoryMonitorService memoryMonitorService;
    
    /**
     * Retorna status atual da memória
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMemoryStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            long usedMemory = totalMemory - freeMemory;
            
            response.put("success", true);
            response.put("heapUsedMB", usedMemory / (1024 * 1024));
            response.put("heapTotalMB", totalMemory / (1024 * 1024));
            response.put("heapMaxMB", maxMemory / (1024 * 1024));
            response.put("heapFreeMB", freeMemory / (1024 * 1024));
            response.put("heapUsagePercent", String.format("%.1f", (double) usedMemory / maxMemory * 100));
            response.put("threads", Thread.activeCount());
            
            // Status de saúde
            double usagePercent = (double) usedMemory / maxMemory;
            if (usagePercent >= 0.85) {
                response.put("status", "CRITICAL");
            } else if (usagePercent >= 0.70) {
                response.put("status", "WARNING");
            } else {
                response.put("status", "HEALTHY");
            }
            
            // Estatísticas adicionais do serviço de monitoramento
            if (memoryMonitorService != null) {
                MemoryMonitorService.MemoryStatus monitorStatus = memoryMonitorService.getMemoryStatus();
                response.put("monitorStatus", monitorStatus.status);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter status de memória: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Força limpeza de memória (caches + GC)
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> forceCleanup() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedBefore = runtime.totalMemory() - runtime.freeMemory();
            
            logger.info("🧹 Limpeza de memória solicitada via API");
            
            // Executar limpeza
            if (memoryCleanupService != null) {
                memoryCleanupService.forceCleanup();
            } else {
                // Fallback: apenas GC
                System.gc();
                Thread.sleep(500);
                System.gc();
            }
            
            // Aguardar GC
            Thread.sleep(1000);
            
            long usedAfter = runtime.totalMemory() - runtime.freeMemory();
            long freedMB = (usedBefore - usedAfter) / (1024 * 1024);
            
            response.put("success", true);
            response.put("message", "Limpeza de memória executada");
            response.put("freedMB", freedMB);
            response.put("usedBeforeMB", usedBefore / (1024 * 1024));
            response.put("usedAfterMB", usedAfter / (1024 * 1024));
            response.put("heapUsagePercent", String.format("%.1f", 
                (double) usedAfter / runtime.maxMemory() * 100));
            
            logger.info("✓ Limpeza concluída - Liberados {} MB", freedMB);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao executar limpeza: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Força apenas Garbage Collection
     */
    @PostMapping("/gc")
    public ResponseEntity<Map<String, Object>> forceGC() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedBefore = runtime.totalMemory() - runtime.freeMemory();
            
            logger.info("🗑️ GC forçado solicitado via API");
            
            // Forçar GC
            System.gc();
            Thread.sleep(500);
            System.gc();
            Thread.sleep(500);
            
            long usedAfter = runtime.totalMemory() - runtime.freeMemory();
            long freedMB = (usedBefore - usedAfter) / (1024 * 1024);
            
            response.put("success", true);
            response.put("message", "Garbage Collection executado");
            response.put("freedMB", freedMB);
            response.put("usedBeforeMB", usedBefore / (1024 * 1024));
            response.put("usedAfterMB", usedAfter / (1024 * 1024));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao executar GC: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
