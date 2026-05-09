package com.inventario.sihcp.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Buffer circular thread-safe para armazenar os últimos N eventos.
 * Quando o buffer está cheio, os eventos mais antigos são descartados.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class CircularEventBuffer {
    
    /** Capacidade padrão do buffer */
    public static final int DEFAULT_CAPACITY = 10;
    
    private final int capacity;
    private final DashboardEvent[] buffer;
    private int head;  // Próxima posição para escrita
    private int size;  // Número de elementos no buffer
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Cria um buffer com capacidade padrão (10 eventos).
     */
    public CircularEventBuffer() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Cria um buffer com capacidade especificada.
     * 
     * @param capacity Capacidade máxima do buffer (mínimo 1)
     * @throws IllegalArgumentException se capacity < 1
     */
    public CircularEventBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        this.capacity = capacity;
        this.buffer = new DashboardEvent[capacity];
        this.head = 0;
        this.size = 0;
    }
    
    /**
     * Adiciona um evento ao buffer.
     * Se o buffer estiver cheio, o evento mais antigo é descartado.
     * 
     * @param event Evento a ser adicionado
     * @throws IllegalArgumentException se event for null
     */
    public void add(DashboardEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            buffer[head] = event;
            head = (head + 1) % capacity;
            if (size < capacity) {
                size++;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Retorna todos os eventos no buffer, do mais antigo ao mais recente.
     * 
     * @return Lista imutável de eventos em ordem cronológica
     */
    public List<DashboardEvent> getAll() {
        lock.readLock().lock();
        try {
            if (size == 0) {
                return Collections.emptyList();
            }
            
            List<DashboardEvent> result = new ArrayList<>(size);
            
            // Calcular posição inicial (evento mais antigo)
            int start = (size < capacity) ? 0 : head;
            
            for (int i = 0; i < size; i++) {
                int index = (start + i) % capacity;
                result.add(buffer[index]);
            }
            
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna os eventos mais recentes, limitado ao número especificado.
     * 
     * @param count Número máximo de eventos a retornar
     * @return Lista imutável dos eventos mais recentes
     */
    public List<DashboardEvent> getRecent(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        
        lock.readLock().lock();
        try {
            if (size == 0) {
                return Collections.emptyList();
            }
            
            int actualCount = Math.min(count, size);
            List<DashboardEvent> result = new ArrayList<>(actualCount);
            
            // Começar do mais recente (head - 1) e ir para trás
            for (int i = 0; i < actualCount; i++) {
                int index = (head - 1 - i + capacity) % capacity;
                result.add(0, buffer[index]); // Inserir no início para manter ordem cronológica
            }
            
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna o evento mais recente.
     * 
     * @return Evento mais recente ou null se buffer vazio
     */
    public DashboardEvent getMostRecent() {
        lock.readLock().lock();
        try {
            if (size == 0) {
                return null;
            }
            int lastIndex = (head - 1 + capacity) % capacity;
            return buffer[lastIndex];
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna o evento mais antigo.
     * 
     * @return Evento mais antigo ou null se buffer vazio
     */
    public DashboardEvent getOldest() {
        lock.readLock().lock();
        try {
            if (size == 0) {
                return null;
            }
            int oldestIndex = (size < capacity) ? 0 : head;
            return buffer[oldestIndex];
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna o número de eventos no buffer.
     * 
     * @return Número de eventos (0 a capacity)
     */
    public int size() {
        lock.readLock().lock();
        try {
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna a capacidade máxima do buffer.
     * 
     * @return Capacidade máxima
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * Verifica se o buffer está vazio.
     * 
     * @return true se não há eventos no buffer
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return size == 0;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Verifica se o buffer está cheio.
     * 
     * @return true se o buffer atingiu capacidade máxima
     */
    public boolean isFull() {
        lock.readLock().lock();
        try {
            return size == capacity;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Limpa todos os eventos do buffer.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < capacity; i++) {
                buffer[i] = null;
            }
            head = 0;
            size = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Retorna eventos filtrados por tipo.
     * 
     * @param type Tipo de evento a filtrar
     * @return Lista de eventos do tipo especificado
     */
    public List<DashboardEvent> getByType(DashboardEventType type) {
        if (type == null) {
            return Collections.emptyList();
        }
        
        lock.readLock().lock();
        try {
            List<DashboardEvent> result = new ArrayList<>();
            List<DashboardEvent> all = getAll();
            for (DashboardEvent event : all) {
                if (event.getType() == type) {
                    result.add(event);
                }
            }
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna uma representação em texto do histórico de eventos.
     * Útil para exibição em tooltips.
     * 
     * @return String formatada com histórico de eventos
     */
    public String toHistoryString() {
        lock.readLock().lock();
        try {
            if (size == 0) {
                return "Nenhum evento registrado";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Últimos ").append(size).append(" eventos:\n");
            
            List<DashboardEvent> events = getAll();
            for (int i = events.size() - 1; i >= 0; i--) {
                DashboardEvent event = events.get(i);
                sb.append("• ").append(event.toSummary()).append("\n");
            }
            
            return sb.toString().trim();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        return String.format("CircularEventBuffer{capacity=%d, size=%d}", capacity, size);
    }
}
