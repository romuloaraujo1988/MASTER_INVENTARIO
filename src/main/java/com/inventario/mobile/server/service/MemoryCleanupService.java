package com.inventario.mobile.server.service;

import com.inventario.service.ConnectedDevicesManager;
import com.inventario.service.DescricaoResumoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Serviço de Limpeza Periódica de Memória
 * 
 * PROBLEMA: Vazamento de memória causando uso de 3GB+
 * 
 * CAUSAS IDENTIFICADAS:
 * 1. Caches estáticos sem limite (DescricaoResumoService)
 * 2. Listeners não removidos (MobileServerManager)
 * 3. Dispositivos conectados acumulados (ConnectedDevicesManager)
 * 4. Objetos temporários não coletados pelo GC
 * 
 * SOLUÇÃO: Limpeza periódica agressiva de todos os caches
 * 
 * @version 1.0.0
 */
@Service
@Profile("mobile")
public class MemoryCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCleanupService.class);
    
    private final MemoryMXBean memoryBean;
    
    // Limite para limpeza agressiva (50% do heap - mais agressivo)
    // Com heap de 384MB, isso significa limpeza quando usar 192MB
    private static final double CLEANUP_THRESHOLD = 0.50;
    
    public MemoryCleanupService() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }
    
    @PostConstruct
    public void init() {
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  🧹 SERVIÇO DE LIMPEZA DE MEMÓRIA v2.0 - BAIXO CONSUMO");
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  Heap máximo: {}MB", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        logger.info("  Limpeza leve: a cada 2 minutos");
        logger.info("  Limpeza agressiva: a cada 5 minutos ou quando memória > 50%");
        logger.info("═══════════════════════════════════════════════════════════════");
    }
    
    /**
     * Limpeza leve a cada 2 minutos (mais frequente para heap pequeno)
     * - Limpa dispositivos inativos
     * - Sugere GC se memória > 40%
     */
    @Scheduled(fixedRate = 120000) // 2 minutos
    public void lightCleanup() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        double usagePercent = (double) heap.getUsed() / heap.getMax();
        
        // Limpar dispositivos inativos SEMPRE
        try {
            ConnectedDevicesManager.cleanupInactiveDevices();
        } catch (Exception e) {
            // Silencioso
        }
        
        // Se memória > 40%, sugerir GC (mais agressivo)
        if (usagePercent > 0.40) {
            System.gc();
            logger.debug("🧹 Limpeza leve - Memória: {}% ({} MB)", 
                String.format("%.1f", usagePercent * 100),
                heap.getUsed() / (1024 * 1024));
        }
    }
    
    /**
     * Limpeza agressiva a cada 5 minutos (mais frequente para heap pequeno)
     * - Limpa TODOS os caches estáticos
     * - Força GC múltiplas vezes
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void aggressiveCleanup() {
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long usedBefore = heapBefore.getUsed();
        double usagePercent = (double) usedBefore / heapBefore.getMax();
        
        logger.info("🧹 Limpeza agressiva programada - Memória: {}% ({} MB)", 
            String.format("%.1f", usagePercent * 100),
            usedBefore / (1024 * 1024));
        
        performAggressiveCleanup();
        
        // Verificar resultado
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long freedMB = (usedBefore - heapAfter.getUsed()) / (1024 * 1024);
        
        logger.info("🧹 Limpeza concluída - Liberados {} MB - Novo uso: {}% ({} MB)", 
            freedMB,
            String.format("%.1f", (double) heapAfter.getUsed() / heapAfter.getMax() * 100),
            heapAfter.getUsed() / (1024 * 1024));
    }
    
    /**
     * Verifica memória a cada 15 segundos e limpa se necessário (mais frequente)
     */
    @Scheduled(fixedRate = 15000) // 15 segundos
    public void checkAndCleanIfNeeded() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        double usagePercent = (double) heap.getUsed() / heap.getMax();
        
        if (usagePercent >= CLEANUP_THRESHOLD) {
            logger.warn("⚠️ Memória alta detectada: {}% - Iniciando limpeza de emergência", 
                String.format("%.1f", usagePercent * 100));
            performAggressiveCleanup();
        }
    }
    
    /**
     * Executa limpeza agressiva de todos os caches
     */
    private void performAggressiveCleanup() {
        // 1. Limpar cache de descrições resumidas
        try {
            DescricaoResumoService.limparCache();
            logger.debug("✓ Cache de descrições limpo");
        } catch (Exception e) {
            logger.debug("Erro ao limpar cache de descrições: {}", e.getMessage());
        }
        
        // 2. Limpar dispositivos conectados inativos
        try {
            ConnectedDevicesManager.cleanupInactiveDevices();
            logger.debug("✓ Dispositivos inativos removidos");
        } catch (Exception e) {
            logger.debug("Erro ao limpar dispositivos: {}", e.getMessage());
        }
        
        // 3. Forçar GC múltiplas vezes
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 4. GC final
        System.gc();
    }
    
    /**
     * Força limpeza imediata (pode ser chamado via API)
     */
    public void forceCleanup() {
        logger.info("🧹 Limpeza forçada solicitada");
        performAggressiveCleanup();
    }
    
    /**
     * Retorna estatísticas de memória
     */
    public java.util.Map<String, Object> getMemoryStats() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("heapUsedMB", heap.getUsed() / (1024 * 1024));
        stats.put("heapMaxMB", heap.getMax() / (1024 * 1024));
        stats.put("heapUsagePercent", String.format("%.1f", (double) heap.getUsed() / heap.getMax() * 100));
        stats.put("threads", Thread.activeCount());
        
        return stats;
    }
}
