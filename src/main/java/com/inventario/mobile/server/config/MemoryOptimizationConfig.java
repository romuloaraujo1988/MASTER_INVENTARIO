package com.inventario.mobile.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuração de otimização de memória para o servidor mobile
 * 
 * PROBLEMA: Servidor salta de 200MB para 3GB quando dispositivos conectam
 * SOLUÇÃO: Limpar caches periodicamente e forçar GC
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Configuration
@EnableScheduling
public class MemoryOptimizationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MemoryOptimizationConfig.class);
    
    // Limite de memória em MB para acionar limpeza
    private static final long MEMORY_THRESHOLD_MB = 512;
    
    /**
     * Monitora e limpa memória a cada 30 segundos
     */
    @Scheduled(fixedRate = 30000)
    public void monitorMemory() {
        Runtime runtime = Runtime.getRuntime();
        
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        
        // Se uso de memória ultrapassar threshold, forçar limpeza
        if (usedMemory > MEMORY_THRESHOLD_MB) {
            logger.warn("⚠️ Memória alta detectada: {}MB / {}MB - Iniciando limpeza", usedMemory, maxMemory);
            
            // Sugerir GC
            System.gc();
            
            // Aguardar um pouco e verificar novamente
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long usedAfterGC = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            logger.info("✓ Memória após limpeza: {}MB (liberados {}MB)", usedAfterGC, usedMemory - usedAfterGC);
        }
    }
    
    /**
     * Limpeza agressiva a cada 5 minutos
     */
    @Scheduled(fixedRate = 300000)
    public void aggressiveCleanup() {
        Runtime runtime = Runtime.getRuntime();
        long beforeCleanup = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        
        logger.debug("Executando limpeza periódica de memória...");
        
        // Forçar GC
        System.gc();
        System.runFinalization();
        System.gc();
        
        long afterCleanup = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        
        if (beforeCleanup - afterCleanup > 10) {
            logger.info("✓ Limpeza periódica: {}MB → {}MB (liberados {}MB)", 
                    beforeCleanup, afterCleanup, beforeCleanup - afterCleanup);
        }
    }
    
    /**
     * Log de estatísticas de memória a cada minuto
     */
    @Scheduled(fixedRate = 60000)
    public void logMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        
        logger.debug("📊 Memória: Usado={}MB | Livre={}MB | Total={}MB | Max={}MB", 
                usedMemory, freeMemory, totalMemory, maxMemory);
    }
}
