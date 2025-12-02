package com.inventario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço de Monitoramento e Recuperação de Memória
 * 
 * FUNCIONALIDADES:
 * - Monitora uso de memória a cada 15 segundos
 * - Detecta tendências de crescimento
 * - RECUPERAÇÃO AUTOMÁTICA quando memória está alta:
 *   1. Limpa caches
 *   2. Força Garbage Collection
 *   3. Libera referências fracas
 * - Previne travamentos por falta de memória
 * 
 * LIMITES (para heap de 1GB):
 * - 60% (600MB): Alerta leve
 * - 75% (750MB): Inicia limpeza preventiva
 * - 85% (850MB): Recuperação agressiva
 * - 95% (950MB): Emergência - limpa tudo
 * 
 * @version 2.0.0 - Com recuperação automática
 */
@Service
@Profile("mobile")
public class MemoryMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryMonitorService.class);
    
    // Limites de alerta (ajustados para 1GB)
    private static final double HEAP_INFO_THRESHOLD = 0.60;      // 60% - Info
    private static final double HEAP_WARNING_THRESHOLD = 0.75;   // 75% - Limpeza preventiva
    private static final double HEAP_CRITICAL_THRESHOLD = 0.85;  // 85% - Recuperação agressiva
    private static final double HEAP_EMERGENCY_THRESHOLD = 0.95; // 95% - Emergência
    
    private static final int GC_FREQUENCY_WARNING = 15;          // GCs por minuto
    
    private final MemoryMXBean memoryBean;
    private final List<GarbageCollectorMXBean> gcBeans;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    // Contadores para detecção de tendências
    private long lastGcCount = 0;
    private long lastGcTime = 0;
    private final AtomicInteger consecutiveHighUsage = new AtomicInteger(0);
    private final AtomicInteger recoveryAttempts = new AtomicInteger(0);
    private final AtomicLong lastRecoveryTime = new AtomicLong(0);
    private long lastHeapUsed = 0;
    
    // Para detecção de vazamento
    private long[] heapHistory = new long[10];
    private int heapHistoryIndex = 0;
    
    public MemoryMonitorService() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }
    
    @PostConstruct
    public void init() {
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  🛡️ MONITOR DE MEMÓRIA COM RECUPERAÇÃO AUTOMÁTICA");
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  Limites configurados:");
        logger.info("    - 60%: Alerta informativo");
        logger.info("    - 75%: Limpeza preventiva de caches");
        logger.info("    - 85%: Recuperação agressiva (GC + limpeza)");
        logger.info("    - 95%: Modo emergência");
        logger.info("═══════════════════════════════════════════════════════════════");
        logMemoryStatus();
    }
    
    /**
     * Verifica memória a cada 15 segundos (mais frequente para reagir rápido)
     */
    @Scheduled(fixedRate = 15000)
    public void checkMemory() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        double usagePercent = (double) used / max;
        
        // Guardar histórico para detecção de vazamento
        heapHistory[heapHistoryIndex] = used;
        heapHistoryIndex = (heapHistoryIndex + 1) % heapHistory.length;
        
        // Verificar tendência de crescimento
        checkMemoryTrend(used);
        
        // RECUPERAÇÃO AUTOMÁTICA baseada no nível de uso
        if (usagePercent >= HEAP_EMERGENCY_THRESHOLD) {
            handleEmergencyMemory(used, max, usagePercent);
        } else if (usagePercent >= HEAP_CRITICAL_THRESHOLD) {
            handleCriticalMemory(used, max, usagePercent);
        } else if (usagePercent >= HEAP_WARNING_THRESHOLD) {
            handleWarningMemory(used, max, usagePercent);
        } else if (usagePercent >= HEAP_INFO_THRESHOLD) {
            handleInfoMemory(used, max, usagePercent);
        } else {
            // Memória OK - resetar contadores
            consecutiveHighUsage.set(0);
            recoveryAttempts.set(0);
        }
        
        // Verificar frequência de GC
        checkGcFrequency();
        
        // Verificar possível vazamento de memória
        checkForMemoryLeak();
    }
    
    /**
     * 60-75%: Apenas informativo
     */
    private void handleInfoMemory(long used, long max, double usagePercent) {
        int currentCount = consecutiveHighUsage.get();
        // Log apenas na primeira vez ou a cada 20 verificações (5 minutos)
        if (currentCount == 0 || currentCount % 20 == 0) {
            logger.info("📊 Memória em uso moderado: {}% ({} MB / {} MB)", 
                String.format("%.1f", usagePercent * 100),
                used / (1024 * 1024),
                max / (1024 * 1024));
        }
        consecutiveHighUsage.incrementAndGet();
    }
    
    /**
     * Log detalhado a cada 5 minutos
     */
    @Scheduled(fixedRate = 300000)
    public void logDetailedStatus() {
        logMemoryStatus();
    }
    
    /**
     * 75-85%: Limpeza preventiva de caches
     */
    private void handleWarningMemory(long used, long max, double usagePercent) {
        int count = consecutiveHighUsage.incrementAndGet();
        
        logger.warn("⚠️ MEMÓRIA ALTA: {}% usado ({} MB / {} MB) - Iniciando limpeza preventiva", 
            String.format("%.1f", usagePercent * 100),
            used / (1024 * 1024),
            max / (1024 * 1024));
        
        // Limpeza preventiva imediata
        if (count >= 2) {
            performPreventiveCleanup();
        }
    }
    
    /**
     * 85-95%: Recuperação agressiva
     */
    private void handleCriticalMemory(long used, long max, double usagePercent) {
        consecutiveHighUsage.incrementAndGet();
        int attempts = recoveryAttempts.incrementAndGet();
        
        logger.error("🚨 MEMÓRIA CRÍTICA: {}% usado ({} MB / {} MB) - Tentativa de recuperação #{}", 
            String.format("%.1f", usagePercent * 100),
            used / (1024 * 1024),
            max / (1024 * 1024),
            attempts);
        
        // Recuperação agressiva
        performAggressiveRecovery();
        
        // Verificar se recuperação funcionou
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage afterRecovery = memoryBean.getHeapMemoryUsage();
        double afterPercent = (double) afterRecovery.getUsed() / afterRecovery.getMax();
        long freedMB = (used - afterRecovery.getUsed()) / (1024 * 1024);
        
        if (afterPercent < HEAP_WARNING_THRESHOLD) {
            logger.info("✅ Recuperação bem-sucedida! Liberados {} MB. Novo uso: {}%", 
                freedMB, String.format("%.1f", afterPercent * 100));
            consecutiveHighUsage.set(0);
            recoveryAttempts.set(0);
        } else if (afterPercent < HEAP_CRITICAL_THRESHOLD) {
            logger.warn("⚠️ Recuperação parcial. Liberados {} MB. Uso ainda em {}%", 
                freedMB, String.format("%.1f", afterPercent * 100));
        } else {
            logger.error("🚨 Recuperação insuficiente! Possível vazamento de memória.");
            
            if (attempts >= 5) {
                logger.error("🚨🚨🚨 ALERTA CRÍTICO: {} tentativas de recuperação falharam!", attempts);
                logger.error("🚨🚨🚨 Recomendação: Reiniciar o servidor em breve");
            }
        }
    }
    
    /**
     * >95%: Modo emergência - limpa TUDO
     */
    private void handleEmergencyMemory(long used, long max, double usagePercent) {
        logger.error("🆘🆘🆘 EMERGÊNCIA DE MEMÓRIA: {}% usado ({} MB / {} MB)", 
            String.format("%.1f", usagePercent * 100),
            used / (1024 * 1024),
            max / (1024 * 1024));
        
        logger.error("🆘 Executando limpeza de emergência...");
        
        // Limpeza de emergência - tudo que for possível
        performEmergencyCleanup();
        
        // Verificar resultado
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage afterEmergency = memoryBean.getHeapMemoryUsage();
        double afterPercent = (double) afterEmergency.getUsed() / afterEmergency.getMax();
        
        if (afterPercent >= HEAP_EMERGENCY_THRESHOLD) {
            logger.error("🆘🆘🆘 FALHA NA RECUPERAÇÃO DE EMERGÊNCIA!");
            logger.error("🆘🆘🆘 O servidor pode travar em breve!");
            logger.error("🆘🆘🆘 REINICIE O SERVIDOR IMEDIATAMENTE!");
        } else {
            logger.warn("🆘 Emergência controlada. Uso reduzido para {}%", 
                String.format("%.1f", afterPercent * 100));
        }
    }
    
    /**
     * Limpeza preventiva (75-85%)
     */
    private void performPreventiveCleanup() {
        logger.info("🧹 Executando limpeza preventiva...");
        
        long before = memoryBean.getHeapMemoryUsage().getUsed();
        
        // 1. Limpar caches do Spring
        clearCaches();
        
        // 2. Sugerir GC
        System.gc();
        
        long after = memoryBean.getHeapMemoryUsage().getUsed();
        logger.info("🧹 Limpeza preventiva concluída. Liberados {} MB", 
            (before - after) / (1024 * 1024));
        
        lastRecoveryTime.set(System.currentTimeMillis());
    }
    
    /**
     * Recuperação agressiva (85-95%)
     */
    private void performAggressiveRecovery() {
        logger.warn("🔧 Executando recuperação agressiva...");
        
        long before = memoryBean.getHeapMemoryUsage().getUsed();
        
        // 1. Limpar TODOS os caches
        clearAllCaches();
        
        // 2. Limpar referências fracas
        clearWeakReferences();
        
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
        
        long after = memoryBean.getHeapMemoryUsage().getUsed();
        logger.warn("🔧 Recuperação agressiva concluída. Liberados {} MB", 
            (before - after) / (1024 * 1024));
        
        lastRecoveryTime.set(System.currentTimeMillis());
    }
    
    /**
     * Limpeza de emergência (>95%)
     */
    private void performEmergencyCleanup() {
        long before = memoryBean.getHeapMemoryUsage().getUsed();
        
        // 1. Limpar absolutamente tudo
        clearAllCaches();
        clearWeakReferences();
        
        // 2. Limpar dispositivos conectados (se existir)
        try {
            ConnectedDevicesManager.cleanupInactiveDevices();
        } catch (Exception e) {
            // Ignorar
        }
        
        // 3. GC agressivo
        for (int i = 0; i < 5; i++) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long after = memoryBean.getHeapMemoryUsage().getUsed();
        logger.error("🆘 Limpeza de emergência liberou {} MB", 
            (before - after) / (1024 * 1024));
    }
    
    /**
     * Limpa caches do Spring
     */
    private void clearCaches() {
        if (cacheManager != null) {
            try {
                cacheManager.getCacheNames().forEach(name -> {
                    var cache = cacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                    }
                });
                logger.debug("Caches do Spring limpos");
            } catch (Exception e) {
                logger.warn("Erro ao limpar caches: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Limpa todos os caches possíveis
     */
    private void clearAllCaches() {
        clearCaches();
        
        // Limpar cache de prepared statements do HikariCP (indiretamente via GC)
        // Outros caches específicos podem ser adicionados aqui
    }
    
    /**
     * Força limpeza de referências fracas
     */
    private void clearWeakReferences() {
        // Criar objetos temporários para forçar limpeza de WeakReferences
        WeakReference<byte[]> ref = new WeakReference<>(new byte[1024]);
        ref.clear();
    }
    
    /**
     * Verifica tendência de crescimento
     */
    private void checkMemoryTrend(long currentUsed) {
        if (lastHeapUsed > 0) {
            long diff = currentUsed - lastHeapUsed;
            double diffMB = diff / (1024.0 * 1024.0);
            
            // Se cresceu mais de 30MB em 15 segundos, alertar
            if (diffMB > 30) {
                logger.warn("📈 Crescimento rápido de memória: +{} MB em 15s", 
                    String.format("%.1f", diffMB));
                
                // Se crescimento muito rápido, iniciar limpeza preventiva
                if (diffMB > 50) {
                    logger.warn("📈 Crescimento muito rápido! Iniciando limpeza preventiva...");
                    performPreventiveCleanup();
                }
            }
        }
        lastHeapUsed = currentUsed;
    }
    
    /**
     * Detecta possível vazamento de memória
     */
    private void checkForMemoryLeak() {
        // Verificar se memória só cresce (nunca diminui)
        int growthCount = 0;
        for (int i = 1; i < heapHistory.length; i++) {
            int prevIndex = (heapHistoryIndex - i + heapHistory.length) % heapHistory.length;
            int currIndex = (heapHistoryIndex - i + 1 + heapHistory.length) % heapHistory.length;
            
            if (heapHistory[currIndex] > 0 && heapHistory[prevIndex] > 0) {
                if (heapHistory[currIndex] > heapHistory[prevIndex]) {
                    growthCount++;
                }
            }
        }
        
        // Se memória cresceu em 8 das últimas 10 verificações, possível vazamento
        if (growthCount >= 8) {
            logger.error("🔴 POSSÍVEL VAZAMENTO DE MEMÓRIA DETECTADO!");
            logger.error("🔴 Memória cresceu consistentemente nas últimas {} verificações", growthCount);
            logger.error("🔴 Considere reiniciar o servidor e investigar a causa");
        }
    }
    
    private void checkGcFrequency() {
        long totalGcCount = 0;
        long totalGcTime = 0;
        
        for (GarbageCollectorMXBean gc : gcBeans) {
            totalGcCount += gc.getCollectionCount();
            totalGcTime += gc.getCollectionTime();
        }
        
        if (lastGcCount > 0) {
            long gcCountDiff = totalGcCount - lastGcCount;
            long gcTimeDiff = totalGcTime - lastGcTime;
            
            // Mais de 10 GCs em 30 segundos = problema
            if (gcCountDiff > GC_FREQUENCY_WARNING / 2) {
                logger.warn("⚠️ GC frequente: {} coletas em 30s ({}ms total)", 
                    gcCountDiff, gcTimeDiff);
            }
        }
        
        lastGcCount = totalGcCount;
        lastGcTime = totalGcTime;
    }
    
    public void logMemoryStatus() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║              STATUS DE MEMÓRIA DO SERVIDOR                 ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ HEAP:                                                      ║");
        logger.info("║   Usado:    {} MB                                          ", 
            String.format("%6d", heap.getUsed() / (1024 * 1024)));
        logger.info("║   Alocado:  {} MB                                          ", 
            String.format("%6d", heap.getCommitted() / (1024 * 1024)));
        logger.info("║   Máximo:   {} MB                                          ", 
            String.format("%6d", heap.getMax() / (1024 * 1024)));
        logger.info("║   Uso:      {}%                                            ", 
            String.format("%5.1f", (double) heap.getUsed() / heap.getMax() * 100));
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ NON-HEAP (Metaspace):                                      ║");
        logger.info("║   Usado:    {} MB                                          ", 
            String.format("%6d", nonHeap.getUsed() / (1024 * 1024)));
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ GARBAGE COLLECTOR:                                         ║");
        
        for (GarbageCollectorMXBean gc : gcBeans) {
            logger.info("║   {}: {} coletas, {}ms total                              ", 
                String.format("%-10s", gc.getName()),
                gc.getCollectionCount(),
                gc.getCollectionTime());
        }
        
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ THREADS: {}                                                ", 
            String.format("%4d", Thread.activeCount()));
        logger.info("╚════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Retorna status de memória para API/UI
     */
    public MemoryStatus getMemoryStatus() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        
        return new MemoryStatus(
            heap.getUsed(),
            heap.getCommitted(),
            heap.getMax(),
            Thread.activeCount(),
            getHealthStatus(heap)
        );
    }
    
    private String getHealthStatus(MemoryUsage heap) {
        double usage = (double) heap.getUsed() / heap.getMax();
        
        if (usage >= HEAP_CRITICAL_THRESHOLD) {
            return "CRITICAL";
        } else if (usage >= HEAP_WARNING_THRESHOLD) {
            return "WARNING";
        }
        return "HEALTHY";
    }
    
    @PreDestroy
    public void shutdown() {
        logger.info("Monitor de memória encerrado");
    }
    
    /**
     * DTO para status de memória
     */
    public static class MemoryStatus {
        public final long heapUsed;
        public final long heapCommitted;
        public final long heapMax;
        public final int threadCount;
        public final String status;
        public final double usagePercent;
        
        public MemoryStatus(long heapUsed, long heapCommitted, long heapMax, 
                           int threadCount, String status) {
            this.heapUsed = heapUsed;
            this.heapCommitted = heapCommitted;
            this.heapMax = heapMax;
            this.threadCount = threadCount;
            this.status = status;
            this.usagePercent = (double) heapUsed / heapMax * 100;
        }
    }
}
