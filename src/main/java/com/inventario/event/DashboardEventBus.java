package com.inventario.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EventBus centralizado para eventos da dashboard.
 * Implementa o padrão Singleton thread-safe com double-checked locking.
 * 
 * Características:
 * - Entrega assíncrona de eventos
 * - Suporte a múltiplos observers por tipo de evento
 * - Rate limiting via EventDebouncer
 * - Isolamento de exceções entre observers
 * - Log de eventos recentes
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DashboardEventBus {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardEventBus.class);
    
    // Singleton instance
    private static volatile DashboardEventBus instance;
    
    // Observers organizados por tipo de evento
    private final Map<DashboardEventType, List<DashboardObserver>> observersByType;
    
    // Observers que querem todos os eventos
    private final List<DashboardObserver> globalObservers;
    
    // Executor para entrega assíncrona
    private final ExecutorService executorService;
    
    // Debouncer para rate limiting
    private final EventDebouncer debouncer;
    
    // Buffer circular para histórico de eventos
    private final CircularEventBuffer eventLog;
    
    // Flag para controle de shutdown
    private final AtomicBoolean isShutdown;
    
    // Lock para operações de registro
    private final Object registrationLock = new Object();
    
    /**
     * Construtor privado - use getInstance().
     */
    private DashboardEventBus() {
        this.observersByType = new ConcurrentHashMap<>();
        this.globalObservers = new CopyOnWriteArrayList<>();
        this.eventLog = new CircularEventBuffer(10);
        this.isShutdown = new AtomicBoolean(false);
        
        // Inicializar mapa com todos os tipos
        for (DashboardEventType type : DashboardEventType.values()) {
            observersByType.put(type, new CopyOnWriteArrayList<>());
        }
        
        // Executor com pool fixo para entrega assíncrona
        this.executorService = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "DashboardEventBus-Worker");
            t.setDaemon(true);
            return t;
        });
        
        // Configurar debouncer
        this.debouncer = new EventDebouncer();
        this.debouncer.setFlushCallback(this::deliverBatch);
        
        logger.info("DashboardEventBus inicializado");
    }
    
    /**
     * Retorna a instância única do EventBus.
     * Thread-safe com double-checked locking.
     * 
     * @return Instância do DashboardEventBus
     */
    public static DashboardEventBus getInstance() {
        if (instance == null) {
            synchronized (DashboardEventBus.class) {
                if (instance == null) {
                    instance = new DashboardEventBus();
                }
            }
        }
        return instance;
    }
    
    /**
     * Registra um observer para receber eventos.
     * O observer receberá apenas os tipos de eventos definidos em getSubscribedEventTypes().
     * 
     * @param observer Observer a ser registrado
     * @throws IllegalArgumentException se observer for null
     */
    public void register(DashboardObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        
        if (isShutdown.get()) {
            logger.warn("Tentativa de registrar observer após shutdown: {}", observer.getObserverName());
            return;
        }
        
        synchronized (registrationLock) {
            Set<DashboardEventType> subscribedTypes = observer.getSubscribedEventTypes();
            
            if (subscribedTypes == null || subscribedTypes.isEmpty() || 
                subscribedTypes.size() == DashboardEventType.values().length) {
                // Observer quer todos os eventos
                if (!globalObservers.contains(observer)) {
                    globalObservers.add(observer);
                    logger.debug("Observer registrado para todos os eventos: {}", observer.getObserverName());
                }
            } else {
                // Observer quer tipos específicos
                for (DashboardEventType type : subscribedTypes) {
                    List<DashboardObserver> observers = observersByType.get(type);
                    if (!observers.contains(observer)) {
                        observers.add(observer);
                    }
                }
                logger.debug("Observer registrado para tipos {}: {}", subscribedTypes, observer.getObserverName());
            }
        }
    }
    
    /**
     * Remove um observer do EventBus.
     * 
     * @param observer Observer a ser removido
     */
    public void unregister(DashboardObserver observer) {
        if (observer == null) {
            return;
        }
        
        synchronized (registrationLock) {
            globalObservers.remove(observer);
            
            for (List<DashboardObserver> observers : observersByType.values()) {
                observers.remove(observer);
            }
            
            logger.debug("Observer removido: {}", observer.getObserverName());
        }
    }
    
    /**
     * Publica um evento para todos os observers interessados.
     * A entrega é feita de forma assíncrona.
     * Este método retorna rapidamente (< 10ms).
     * 
     * @param event Evento a ser publicado
     * @throws IllegalArgumentException se event for null
     */
    public void publish(DashboardEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        if (isShutdown.get()) {
            logger.warn("Tentativa de publicar evento após shutdown: {}", event.getType());
            return;
        }
        
        // Adicionar ao log
        eventLog.add(event);
        
        logger.debug("Evento publicado: {}", event);
        
        // Verificar se deve fazer debounce
        if (debouncer.process(event)) {
            // Entregar imediatamente
            deliverAsync(event);
        }
        // Se retornou false, o evento foi enfileirado no debouncer
    }
    
    /**
     * Publica um evento de forma síncrona (bloqueia até entrega).
     * Use apenas quando necessário garantir entrega antes de continuar.
     * 
     * @param event Evento a ser publicado
     */
    public void publishSync(DashboardEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        if (isShutdown.get()) {
            return;
        }
        
        eventLog.add(event);
        deliverToObservers(event);
    }
    
    /**
     * Retorna o histórico de eventos recentes.
     * 
     * @return Lista dos últimos 10 eventos
     */
    public List<DashboardEvent> getEventHistory() {
        return eventLog.getAll();
    }
    
    /**
     * Retorna o buffer de eventos para acesso direto.
     * 
     * @return CircularEventBuffer com histórico
     */
    public CircularEventBuffer getEventLog() {
        return eventLog;
    }
    
    /**
     * Verifica se o debouncer está ativo.
     * 
     * @return true se há eventos sendo debounced
     */
    public boolean isDebouncing() {
        return debouncer.isDebouncing();
    }
    
    /**
     * Força o flush de eventos pendentes no debouncer.
     */
    public void flushPendingEvents() {
        debouncer.flush();
    }
    
    /**
     * Retorna o número de observers registrados.
     * 
     * @return Total de observers
     */
    public int getObserverCount() {
        int count = globalObservers.size();
        Set<DashboardObserver> unique = new HashSet<>(globalObservers);
        for (List<DashboardObserver> observers : observersByType.values()) {
            unique.addAll(observers);
        }
        return unique.size();
    }
    
    /**
     * Retorna o número de observers para um tipo específico.
     * 
     * @param type Tipo de evento
     * @return Número de observers interessados neste tipo
     */
    public int getObserverCount(DashboardEventType type) {
        int count = globalObservers.size();
        List<DashboardObserver> typeObservers = observersByType.get(type);
        if (typeObservers != null) {
            count += typeObservers.size();
        }
        return count;
    }
    
    /**
     * Encerra o EventBus e libera recursos.
     * Após shutdown, novos eventos e registros são ignorados.
     */
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Encerrando DashboardEventBus...");
            
            debouncer.shutdown();
            executorService.shutdown();
            
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            synchronized (registrationLock) {
                globalObservers.clear();
                for (List<DashboardObserver> observers : observersByType.values()) {
                    observers.clear();
                }
            }
            
            logger.info("DashboardEventBus encerrado");
        }
    }
    
    /**
     * Reseta a instância singleton (apenas para testes).
     */
    static void resetInstance() {
        synchronized (DashboardEventBus.class) {
            if (instance != null) {
                instance.shutdown();
                instance = null;
            }
        }
    }
    
    // ========== Métodos privados ==========
    
    private void deliverAsync(DashboardEvent event) {
        executorService.submit(() -> deliverToObservers(event));
    }
    
    private void deliverBatch(List<DashboardEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        executorService.submit(() -> {
            // Coletar observers únicos interessados
            Set<DashboardObserver> interestedObservers = new HashSet<>();
            
            for (DashboardEvent event : events) {
                interestedObservers.addAll(globalObservers);
                List<DashboardObserver> typeObservers = observersByType.get(event.getType());
                if (typeObservers != null) {
                    interestedObservers.addAll(typeObservers);
                }
            }
            
            // Entregar batch para cada observer
            for (DashboardObserver observer : interestedObservers) {
                try {
                    // Filtrar eventos relevantes para este observer
                    List<DashboardEvent> relevantEvents = new ArrayList<>();
                    for (DashboardEvent event : events) {
                        if (observer.isInterestedIn(event.getType())) {
                            relevantEvents.add(event);
                        }
                    }
                    
                    if (!relevantEvents.isEmpty()) {
                        observer.onDashboardEventBatch(relevantEvents);
                    }
                } catch (Exception e) {
                    logger.error("Erro ao entregar batch para observer {}: {}", 
                        observer.getObserverName(), e.getMessage(), e);
                }
            }
        });
    }
    
    private void deliverToObservers(DashboardEvent event) {
        DashboardEventType type = event.getType();
        
        // Entregar para observers globais
        for (DashboardObserver observer : globalObservers) {
            deliverToObserver(observer, event);
        }
        
        // Entregar para observers do tipo específico
        List<DashboardObserver> typeObservers = observersByType.get(type);
        if (typeObservers != null) {
            for (DashboardObserver observer : typeObservers) {
                // Evitar duplicação se já está nos globais
                if (!globalObservers.contains(observer)) {
                    deliverToObserver(observer, event);
                }
            }
        }
    }
    
    private void deliverToObserver(DashboardObserver observer, DashboardEvent event) {
        try {
            observer.onDashboardEvent(event);
        } catch (Exception e) {
            // Isolar exceção - não afetar outros observers
            logger.error("Erro ao entregar evento para observer {}: {}", 
                observer.getObserverName(), e.getMessage(), e);
        }
    }
}
