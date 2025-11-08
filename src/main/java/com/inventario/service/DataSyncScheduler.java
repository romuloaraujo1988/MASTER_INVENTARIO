package com.inventario.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Agendador para sincronização automática de dados
 */
public class DataSyncScheduler {
    
    private final DataSyncService dataSyncService;
    private final ScheduledExecutorService scheduler;
    private boolean isRunning = false;
    
    public DataSyncScheduler() {
        this.dataSyncService = new DataSyncService();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Inicia a sincronização automática
     * @param intervalHours Intervalo em horas entre sincronizações
     */
    public void startAutoSync(int intervalHours) {
        if (isRunning) {
            System.out.println("[SCHEDULER] Sincronização automática já está em execução");
            return;
        }
        
        System.out.println("[SCHEDULER] Iniciando sincronização automática a cada " + intervalHours + " horas");
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[SCHEDULER] Executando sincronização automática...");
                dataSyncService.syncAllData();
                System.out.println("[SCHEDULER] Sincronização automática concluída com sucesso!");
                
            } catch (Exception e) {
                System.err.println("[SCHEDULER] Erro durante sincronização automática: " + e.getMessage());
                // Log do erro mas não interrompe o agendamento
            }
        }, 0, intervalHours, TimeUnit.HOURS);
        
        isRunning = true;
    }
    
    /**
     * Inicia sincronização automática com intervalo padrão de 2 horas
     */
    public void startAutoSync() {
        startAutoSync(2);
    }
    
    /**
     * Para a sincronização automática
     */
    public void stopAutoSync() {
        if (!isRunning) {
            System.out.println("[SCHEDULER] Sincronização automática não está em execução");
            return;
        }
        
        System.out.println("[SCHEDULER] Parando sincronização automática...");
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
        System.out.println("[SCHEDULER] Sincronização automática parada");
    }
    
    /**
     * Verifica se a sincronização automática está ativa
     */
    public boolean isAutoSyncRunning() {
        return isRunning && !scheduler.isShutdown();
    }
    
    /**
     * Executa uma sincronização manual imediata
     */
    public void syncNow() {
        try {
            System.out.println("[SCHEDULER] Executando sincronização manual...");
            dataSyncService.syncAllData();
            System.out.println("[SCHEDULER] Sincronização manual concluída!");
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER] Erro durante sincronização manual: " + e.getMessage());
            throw new RuntimeException("Falha na sincronização manual", e);
        }
    }
    
    /**
     * Obtém estatísticas atualizadas
     */
    public String getUpdatedStatistics() {
        try {
            return dataSyncService.generateUpdatedStatistics();
        } catch (Exception e) {
            System.err.println("[SCHEDULER] Erro ao gerar estatísticas: " + e.getMessage());
            return "Erro ao gerar estatísticas: " + e.getMessage();
        }
    }
    
    /**
     * Método para teste do agendador
     */
    public static void main(String[] args) {
        DataSyncScheduler scheduler = new DataSyncScheduler();
        
        try {
            System.out.println("=== TESTE DO AGENDADOR DE SINCRONIZAÇÃO ===");
            
            // Executar sincronização manual
            scheduler.syncNow();
            
            // Mostrar estatísticas
            System.out.println("\n=== ESTATÍSTICAS ATUALIZADAS ===");
            System.out.println(scheduler.getUpdatedStatistics());
            
            // Iniciar sincronização automática (para teste, usar intervalo menor)
            System.out.println("\n=== INICIANDO SINCRONIZAÇÃO AUTOMÁTICA ===");
            scheduler.startAutoSync(1); // 1 hora para teste
            
            // Aguardar um pouco para demonstrar
            Thread.sleep(5000);
            
            // Parar sincronização
            scheduler.stopAutoSync();
            
            System.out.println("\n✅ Teste do agendador concluído!");
            
        } catch (Exception e) {
            System.err.println("❌ Erro durante teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}