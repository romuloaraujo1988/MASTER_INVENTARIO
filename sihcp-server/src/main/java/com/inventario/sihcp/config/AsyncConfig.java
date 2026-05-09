package com.inventario.sihcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração de processamento assíncrono para melhorar escalabilidade
 * 
 * OTIMIZADO v2.0: Reduzido número de threads para economizar memória
 * - Cada thread consome ~1MB de stack
 * - Configuração anterior criava 60+ threads = 60MB+ só de stack
 * - Nova configuração: máximo 15 threads = ~15MB de stack
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool para tarefas assíncronas gerais
     * OTIMIZADO: Reduzido de cores*4 para máximo 8 threads
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // OTIMIZADO: Valores fixos e conservadores
        executor.setCorePoolSize(2);      // Era: cores * 2 (até 24)
        executor.setMaxPoolSize(8);       // Era: cores * 4 (até 48)
        executor.setQueueCapacity(200);   // Era: 500
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setAllowCoreThreadTimeOut(true); // Permite encerrar threads ociosas
        
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool dedicado para sincronização de dados
     * OTIMIZADO: Reduzido de 10 para 4 threads máximo
     */
    @Bean(name = "syncExecutor")
    public Executor syncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);      // Era: 5
        executor.setMaxPoolSize(4);       // Era: 10
        executor.setQueueCapacity(50);    // Era: 100
        executor.setThreadNamePrefix("sync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool para geração de relatórios
     * OTIMIZADO: Reduzido de 5 para 2 threads máximo
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);      // Era: 2
        executor.setMaxPoolSize(2);       // Era: 5
        executor.setQueueCapacity(20);    // Era: 50
        executor.setThreadNamePrefix("report-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }
}
