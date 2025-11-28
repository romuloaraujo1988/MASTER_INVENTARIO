package com.inventario.mobile.server.controller;

import com.inventario.config.HikariConnectionPool;
import com.inventario.mobile.server.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para monitoramento de saúde do servidor
 * 
 * Endpoints:
 * - GET /api/mobile/health - Status geral
 * - GET /api/mobile/health/pool - Status do pool de conexões
 * - GET /api/mobile/health/memory - Status de memória
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/health")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    /**
     * Status geral do servidor
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Status do pool
            boolean poolHealthy = HikariConnectionPool.isHealthy();
            status.put("database", poolHealthy ? "UP" : "DOWN");
            status.put("poolStats", HikariConnectionPool.getPoolStats());
            
            // Status de memória
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            
            long usedMB = heapUsage.getUsed() / (1024 * 1024);
            long maxMB = heapUsage.getMax() / (1024 * 1024);
            double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            
            status.put("memoryUsedMB", usedMB);
            status.put("memoryMaxMB", maxMB);
            status.put("memoryUsagePercent", String.format("%.1f%%", usagePercent));
            
            // Status geral
            boolean healthy = poolHealthy && usagePercent < 90;
            status.put("status", healthy ? "UP" : "DEGRADED");
            status.put("timestamp", System.currentTimeMillis());
            
            String message = healthy ? "Servidor saudável" : "Servidor com problemas";
            
            logger.info("Health check: {} - Pool: {}, Memória: {}MB/{}MB ({}%)", 
                    status.get("status"), poolHealthy ? "OK" : "FAIL", usedMB, maxMB, 
                    String.format("%.1f", usagePercent));
            
            return ResponseEntity.ok(ApiResponse.success(status, message));
            
        } catch (Exception e) {
            logger.error("Erro no health check: {}", e.getMessage());
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Erro no health check", "HEALTH_ERROR"));
        }
    }
    
    /**
     * Status detalhado do pool de conexões
     */
    @GetMapping("/pool")
    public ResponseEntity<ApiResponse<Map<String, Object>>> poolStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("healthy", HikariConnectionPool.isHealthy());
            status.put("stats", HikariConnectionPool.getPoolStats());
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponse.success(status, "Status do pool"));
            
        } catch (Exception e) {
            logger.error("Erro ao obter status do pool: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Erro ao obter status do pool", "POOL_ERROR"));
        }
    }
    
    /**
     * Status detalhado de memória
     */
    @GetMapping("/memory")
    public ResponseEntity<ApiResponse<Map<String, Object>>> memoryStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // Heap Memory
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            Map<String, Object> heap = new HashMap<>();
            heap.put("usedMB", heapUsage.getUsed() / (1024 * 1024));
            heap.put("committedMB", heapUsage.getCommitted() / (1024 * 1024));
            heap.put("maxMB", heapUsage.getMax() / (1024 * 1024));
            heap.put("usagePercent", String.format("%.1f%%", 
                    (double) heapUsage.getUsed() / heapUsage.getMax() * 100));
            status.put("heap", heap);
            
            // Non-Heap Memory
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            Map<String, Object> nonHeap = new HashMap<>();
            nonHeap.put("usedMB", nonHeapUsage.getUsed() / (1024 * 1024));
            nonHeap.put("committedMB", nonHeapUsage.getCommitted() / (1024 * 1024));
            status.put("nonHeap", nonHeap);
            
            // Runtime info
            Runtime runtime = Runtime.getRuntime();
            status.put("availableProcessors", runtime.availableProcessors());
            status.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
            status.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
            
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponse.success(status, "Status de memória"));
            
        } catch (Exception e) {
            logger.error("Erro ao obter status de memória: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Erro ao obter status de memória", "MEMORY_ERROR"));
        }
    }
    
    /**
     * Endpoint de teste simples
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        logger.info("Endpoint de teste chamado");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "API Mobile funcionando corretamente");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Força garbage collection (usar com cuidado!)
     */
    @PostMapping("/gc")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forceGC() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Memória antes
            Runtime runtime = Runtime.getRuntime();
            long beforeMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            
            // Forçar GC
            System.gc();
            
            // Aguardar um pouco
            Thread.sleep(500);
            
            // Memória depois
            long afterMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long freedMB = beforeMB - afterMB;
            
            status.put("beforeMB", beforeMB);
            status.put("afterMB", afterMB);
            status.put("freedMB", freedMB);
            status.put("timestamp", System.currentTimeMillis());
            
            logger.info("GC forçado: {}MB -> {}MB (liberado: {}MB)", beforeMB, afterMB, freedMB);
            
            return ResponseEntity.ok(ApiResponse.success(status, 
                    String.format("GC executado - Liberado: %dMB", freedMB)));
            
        } catch (Exception e) {
            logger.error("Erro ao forçar GC: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Erro ao forçar GC", "GC_ERROR"));
        }
    }
}
