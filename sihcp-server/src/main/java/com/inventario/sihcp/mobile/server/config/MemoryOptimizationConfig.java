package com.inventario.sihcp.mobile.server.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuração de otimização de memória para o servidor mobile
 * 
 * NOTA: Schedulers de limpeza movidos para MemoryCleanupService
 * para evitar DUPLICAÇÃO que causava overhead desnecessário.
 * 
 * Esta classe agora é apenas um placeholder para configurações futuras.
 * 
 * @author Sistema de Inventário
 * @version 2.0.0 - Schedulers removidos (duplicados com MemoryCleanupService)
 */
@Configuration
public class MemoryOptimizationConfig {
    
    // Schedulers REMOVIDOS - estavam duplicados com MemoryCleanupService
    // Isso causava overhead desnecessário e múltiplas chamadas de GC
    
    // A limpeza de memória agora é feita APENAS pelo MemoryCleanupService
}
