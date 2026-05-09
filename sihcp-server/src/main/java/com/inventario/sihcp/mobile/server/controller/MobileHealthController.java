package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.service.MemoryMonitorService;
import com.inventario.sihcp.service.MemoryMonitorService.MemoryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller para monitoramento de saúde do servidor
 * 
 * Endpoints:
 * - GET /api/mobile/server/memory - Status de memória
 * - GET /api/mobile/server/status - Status geral
 * - POST /api/mobile/server/gc - Forçar GC (admin only)
 * 
 * NOTA: Usa /server ao invés de /health para evitar conflito com Spring Actuator
 */
@RestController
@RequestMapping("/api/mobile/server")
public class MobileHealthController {

    @Autowired(required = false)
    private MemoryMonitorService memoryMonitor;

    /**
     * Retorna status detalhado de memória
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        // Memória básica
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        Map<String, Object> memory = new HashMap<>();
        memory.put("usedMB", usedMemory / (1024 * 1024));
        memory.put("freeMB", freeMemory / (1024 * 1024));
        memory.put("totalMB", totalMemory / (1024 * 1024));
        memory.put("maxMB", maxMemory / (1024 * 1024));
        memory.put("usagePercent", String.format("%.1f", (double) usedMemory / maxMemory * 100));
        
        response.put("memory", memory);
        
        // Status do monitor
        if (memoryMonitor != null) {
            MemoryStatus status = memoryMonitor.getMemoryStatus();
            response.put("status", status.status);
            response.put("threadCount", status.threadCount);
        } else {
            response.put("status", getSimpleStatus(usedMemory, maxMemory));
            response.put("threadCount", Thread.activeCount());
        }
        
        // Uptime
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = runtimeBean.getUptime();
        response.put("uptimeMinutes", TimeUnit.MILLISECONDS.toMinutes(uptimeMs));
        response.put("uptimeFormatted", formatUptime(uptimeMs));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna status geral do servidor
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        // Status geral
        String status;
        if (usagePercent >= 85) {
            status = "CRITICAL";
        } else if (usagePercent >= 70) {
            status = "WARNING";
        } else {
            status = "HEALTHY";
        }
        
        response.put("status", status);
        response.put("memoryUsagePercent", String.format("%.1f", usagePercent));
        response.put("memoryUsedMB", usedMemory / (1024 * 1024));
        response.put("memoryMaxMB", maxMemory / (1024 * 1024));
        response.put("threadCount", Thread.activeCount());
        response.put("availableProcessors", runtime.availableProcessors());
        
        // Uptime
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        response.put("uptimeFormatted", formatUptime(runtimeBean.getUptime()));
        
        // Versão
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("serverVersion", "2.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Força Garbage Collection (usar com cuidado)
     */
    @PostMapping("/gc")
    public ResponseEntity<Map<String, Object>> forceGarbageCollection() {
        Map<String, Object> response = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        // Antes do GC
        long beforeUsed = runtime.totalMemory() - runtime.freeMemory();
        
        // Forçar GC
        System.gc();
        
        // Aguardar um pouco
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Depois do GC
        long afterUsed = runtime.totalMemory() - runtime.freeMemory();
        long freed = beforeUsed - afterUsed;
        
        response.put("success", true);
        response.put("beforeMB", beforeUsed / (1024 * 1024));
        response.put("afterMB", afterUsed / (1024 * 1024));
        response.put("freedMB", freed / (1024 * 1024));
        response.put("message", String.format("GC executado. Liberados %d MB", freed / (1024 * 1024)));
        
        return ResponseEntity.ok(response);
    }

    private String getSimpleStatus(long used, long max) {
        double usage = (double) used / max;
        if (usage >= 0.85) return "CRITICAL";
        if (usage >= 0.70) return "WARNING";
        return "HEALTHY";
    }

    private String formatUptime(long uptimeMs) {
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
