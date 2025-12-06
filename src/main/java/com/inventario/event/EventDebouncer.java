package com.inventario.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Debouncer para eventos da dashboard.
 * Implementa rate limiting para evitar atualizações excessivas da UI
 * durante bursts de eventos.
 * 
 * Regras:
 * - Máximo de 2 atualizações de UI por segundo
 * - Se mais de 5 eventos em 1 segundo, consolida em uma única atualização
 * - Janela de debounce de 100ms para consolidar eventos próximos
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class EventDebouncer {
    
    private static final Logger logger = LoggerFactory.getLogger(EventDebouncer.class);
    
    /** Número máximo de atualizações de UI por segundo */
    public static final int MAX_UPDATES_PER_SECOND = 2;
    
    /** Threshold para ativar debouncing (eventos por segundo) */
    public static final int BURST_THRESHOLD = 5;
    
    /** Janela de debounce em milissegundos */
    public static final long DEBOUNCE_WINDOW_MS = 100;
    
    /** Intervalo mínimo entre atualizações em milissegundos */
    public static final long MIN_UPDATE_INTERVAL_MS = 500; // 2 updates/sec = 500ms interval
    
    private final int maxUpdatesPerSecond;
    private final int burstThreshold;
    private final long debounceWindowMs;
    
    private final Queue<DashboardEvent> pendingEvents;
    private final AtomicLong lastUpdateTime;
    private final AtomicInteger eventCountInWindow;
    private final AtomicLong windowStartTime;
    
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pendingFlush;
    private Consumer<List<DashboardEvent>> flushCallback;
    
    private volatile boolean isDebouncing;
    
    private final Object lock = new Object();
    
    /**
     * Cria um debouncer com configurações padrão.
     */
    public EventDebouncer() {
        this(MAX_UPDATES_PER_SECOND, BURST_THRESHOLD, DEBOUNCE_WINDOW_MS);
    }
    
    /**
     * Cria um debouncer com configurações customizadas.
     * 
     * @param maxUpdatesPerSecond Máximo de atualizações por segundo
     * @param burstThreshold Threshold para ativar debouncing
     * @param debounceWindowMs Janela de debounce em ms
     */
    public EventDebouncer(int maxUpdatesPerSecond, int burstThreshold, long debounceWindowMs) {
        this.maxUpdatesPerSecond = Math.max(1, maxUpdatesPerSecond);
        this.burstThreshold = Math.max(1, burstThreshold);
        this.debounceWindowMs = Math.max(10, debounceWindowMs);
        
        this.pendingEvents = new ConcurrentLinkedQueue<>();
        this.lastUpdateTime = new AtomicLong(0);
        this.eventCountInWindow = new AtomicInteger(0);
        this.windowStartTime = new AtomicLong(System.currentTimeMillis());
        this.isDebouncing = false;
        
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EventDebouncer-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Define o callback a ser chamado quando eventos são consolidados.
     * 
     * @param callback Consumer que recebe a lista de eventos consolidados
     */
    public void setFlushCallback(Consumer<List<DashboardEvent>> callback) {
        this.flushCallback = callback;
    }
    
    /**
     * Processa um evento, aplicando debouncing se necessário.
     * 
     * @param event Evento a ser processado
     * @return true se o evento deve ser entregue imediatamente, false se foi enfileirado
     */
    public boolean process(DashboardEvent event) {
        if (event == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        
        synchronized (lock) {
            // Atualizar contagem de eventos na janela
            updateEventCount(now);
            
            // Verificar se estamos em burst
            if (eventCountInWindow.get() >= burstThreshold) {
                // Ativar debouncing
                isDebouncing = true;
                pendingEvents.add(event);
                scheduleFlush();
                logger.debug("Evento enfileirado para debounce: {}", event.getType());
                return false;
            }
            
            // Verificar rate limiting
            long timeSinceLastUpdate = now - lastUpdateTime.get();
            long minInterval = 1000 / maxUpdatesPerSecond;
            
            if (timeSinceLastUpdate < minInterval) {
                // Rate limit atingido, enfileirar
                pendingEvents.add(event);
                scheduleFlush();
                logger.debug("Evento enfileirado por rate limit: {}", event.getType());
                return false;
            }
            
            // Pode entregar imediatamente
            lastUpdateTime.set(now);
            eventCountInWindow.incrementAndGet();
            return true;
        }
    }
    
    /**
     * Verifica se um evento deve ser debounced sem processá-lo.
     * 
     * @param event Evento a verificar
     * @return true se o evento seria debounced
     */
    public boolean shouldDebounce(DashboardEvent event) {
        if (event == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        
        synchronized (lock) {
            updateEventCount(now);
            
            // Verificar burst
            if (eventCountInWindow.get() >= burstThreshold) {
                return true;
            }
            
            // Verificar rate limit
            long timeSinceLastUpdate = now - lastUpdateTime.get();
            long minInterval = 1000 / maxUpdatesPerSecond;
            
            return timeSinceLastUpdate < minInterval;
        }
    }
    
    /**
     * Retorna os eventos consolidados pendentes e limpa a fila.
     * 
     * @return Lista de eventos pendentes
     */
    public List<DashboardEvent> getConsolidatedEvents() {
        synchronized (lock) {
            if (pendingEvents.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<DashboardEvent> events = new ArrayList<>();
            DashboardEvent event;
            while ((event = pendingEvents.poll()) != null) {
                events.add(event);
            }
            
            isDebouncing = false;
            return events;
        }
    }
    
    /**
     * Força o flush dos eventos pendentes.
     */
    public void flush() {
        synchronized (lock) {
            cancelPendingFlush();
            
            List<DashboardEvent> events = getConsolidatedEvents();
            if (!events.isEmpty() && flushCallback != null) {
                lastUpdateTime.set(System.currentTimeMillis());
                try {
                    flushCallback.accept(events);
                } catch (Exception e) {
                    logger.error("Erro ao executar flush callback", e);
                }
            }
        }
    }
    
    /**
     * Reseta o estado do debouncer.
     */
    public void reset() {
        synchronized (lock) {
            cancelPendingFlush();
            pendingEvents.clear();
            lastUpdateTime.set(0);
            eventCountInWindow.set(0);
            windowStartTime.set(System.currentTimeMillis());
            isDebouncing = false;
        }
    }
    
    /**
     * Verifica se o debouncer está atualmente em modo de debouncing.
     * 
     * @return true se há eventos sendo debounced
     */
    public boolean isDebouncing() {
        return isDebouncing;
    }
    
    /**
     * Retorna o número de eventos pendentes.
     * 
     * @return Número de eventos na fila
     */
    public int getPendingCount() {
        return pendingEvents.size();
    }
    
    /**
     * Retorna o número de eventos na janela atual.
     * 
     * @return Contagem de eventos
     */
    public int getEventCountInWindow() {
        return eventCountInWindow.get();
    }
    
    /**
     * Encerra o debouncer e libera recursos.
     */
    public void shutdown() {
        cancelPendingFlush();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // ========== Métodos privados ==========
    
    private void updateEventCount(long now) {
        // Resetar contagem se passou 1 segundo desde o início da janela
        if (now - windowStartTime.get() >= 1000) {
            eventCountInWindow.set(0);
            windowStartTime.set(now);
        }
    }
    
    private void scheduleFlush() {
        cancelPendingFlush();
        
        pendingFlush = scheduler.schedule(() -> {
            synchronized (lock) {
                List<DashboardEvent> events = getConsolidatedEvents();
                if (!events.isEmpty() && flushCallback != null) {
                    lastUpdateTime.set(System.currentTimeMillis());
                    try {
                        flushCallback.accept(events);
                        logger.debug("Flush executado com {} eventos", events.size());
                    } catch (Exception e) {
                        logger.error("Erro ao executar flush callback", e);
                    }
                }
            }
        }, debounceWindowMs, TimeUnit.MILLISECONDS);
    }
    
    private void cancelPendingFlush() {
        if (pendingFlush != null && !pendingFlush.isDone()) {
            pendingFlush.cancel(false);
        }
    }
    
    @Override
    public String toString() {
        return String.format("EventDebouncer{pending=%d, debouncing=%s, eventsInWindow=%d}",
            pendingEvents.size(), isDebouncing, eventCountInWindow.get());
    }
}
