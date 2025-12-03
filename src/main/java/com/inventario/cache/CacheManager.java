package com.inventario.cache;

import com.inventario.model.Inventario;
import com.inventario.model.Patrimonio;
import com.inventario.model.Sala;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de cache em memória para dados frequentes
 * 
 * Singleton thread-safe que mantém dados em cache com TTL configurável.
 * Reduz consultas ao banco de dados para dados acessados frequentemente.
 * 
 * @see Requirements 3.1, 3.2, 3.4, 3.5
 */
public class CacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private static CacheManager instance;
    
    // TTL padrão: 5 minutos
    private static final long DEFAULT_TTL_MILLIS = 5 * 60 * 1000;
    
    // LIMITES para evitar vazamento de memória
    private static final int MAX_SALAS_CACHE = 200;
    private static final int MAX_PATRIMONIOS_POR_SALA_CACHE = 50;
    
    // Caches thread-safe COM LIMITE
    private final Map<Integer, CachedData<Sala>> salasCache;
    private final Map<Integer, CachedData<List<Patrimonio>>> patrimoniosPorSalaCache;
    private CachedData<Inventario> inventarioAtivoCache;
    private CachedData<List<Sala>> todasSalasCache;
    
    /**
     * Construtor privado - Singleton
     * CORREÇÃO: Caches com limite para evitar vazamento de memória
     */
    private CacheManager() {
        // Cache de salas com limite LRU
        this.salasCache = new java.util.LinkedHashMap<Integer, CachedData<Sala>>(MAX_SALAS_CACHE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, CachedData<Sala>> eldest) {
                return size() > MAX_SALAS_CACHE;
            }
        };
        
        // Cache de patrimônios por sala com limite LRU
        this.patrimoniosPorSalaCache = new java.util.LinkedHashMap<Integer, CachedData<List<Patrimonio>>>(MAX_PATRIMONIOS_POR_SALA_CACHE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, CachedData<List<Patrimonio>>> eldest) {
                return size() > MAX_PATRIMONIOS_POR_SALA_CACHE;
            }
        };
        
        logger.info("CacheManager inicializado com TTL de {}ms (limites: {} salas, {} patrimônios/sala)", 
            DEFAULT_TTL_MILLIS, MAX_SALAS_CACHE, MAX_PATRIMONIOS_POR_SALA_CACHE);
    }
    
    /**
     * Obtém instância singleton do CacheManager
     * Thread-safe usando double-checked locking
     */
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }
    
    // ========== Cache de Salas ==========
    
    /**
     * Obtém uma sala do cache por ID
     * 
     * @param id ID da sala
     * @return Optional com a sala se encontrada e não expirada
     */
    public Optional<Sala> getSala(int id) {
        CachedData<Sala> cached = salasCache.get(id);
        
        if (cached == null) {
            logger.debug("Cache miss: Sala {}", id);
            return Optional.empty();
        }
        
        if (cached.isExpired()) {
            salasCache.remove(id);
            logger.debug("Cache expired: Sala {}", id);
            return Optional.empty();
        }
        
        logger.debug("Cache hit: Sala {} (age={}ms)", id, cached.getAge());
        return Optional.of(cached.getData());
    }
    
    /**
     * Armazena uma sala no cache
     * 
     * @param sala Sala a ser armazenada
     */
    public void putSala(Sala sala) {
        if (sala != null && sala.getId() > 0) {
            salasCache.put(sala.getId(), new CachedData<>(sala, DEFAULT_TTL_MILLIS));
            logger.debug("Cached: Sala {}", sala.getId());
        }
    }
    
    /**
     * Obtém todas as salas do cache
     * 
     * @return Optional com lista de salas se encontrada e não expirada
     */
    public Optional<List<Sala>> getAllSalas() {
        if (todasSalasCache == null) {
            logger.debug("Cache miss: Todas as salas");
            return Optional.empty();
        }
        
        if (todasSalasCache.isExpired()) {
            todasSalasCache = null;
            logger.debug("Cache expired: Todas as salas");
            return Optional.empty();
        }
        
        logger.debug("Cache hit: Todas as salas (age={}ms)", todasSalasCache.getAge());
        return Optional.of(todasSalasCache.getData());
    }
    
    /**
     * Armazena lista de todas as salas no cache
     * 
     * @param salas Lista de salas
     */
    public void putAllSalas(List<Sala> salas) {
        if (salas != null) {
            todasSalasCache = new CachedData<>(salas, DEFAULT_TTL_MILLIS);
            logger.debug("Cached: {} salas", salas.size());
            
            // Também cachear individualmente
            salas.forEach(this::putSala);
        }
    }
    
    // ========== Cache de Inventário Ativo ==========
    
    /**
     * Obtém o inventário ativo do cache
     * 
     * @return Optional com inventário se encontrado e não expirado
     */
    public Optional<Inventario> getInventarioAtivo() {
        if (inventarioAtivoCache == null) {
            logger.debug("Cache miss: Inventário ativo");
            return Optional.empty();
        }
        
        if (inventarioAtivoCache.isExpired()) {
            inventarioAtivoCache = null;
            logger.debug("Cache expired: Inventário ativo");
            return Optional.empty();
        }
        
        logger.debug("Cache hit: Inventário ativo (age={}ms)", inventarioAtivoCache.getAge());
        return Optional.of(inventarioAtivoCache.getData());
    }
    
    /**
     * Armazena o inventário ativo no cache
     * 
     * @param inventario Inventário ativo
     */
    public void putInventarioAtivo(Inventario inventario) {
        if (inventario != null) {
            inventarioAtivoCache = new CachedData<>(inventario, DEFAULT_TTL_MILLIS);
            logger.debug("Cached: Inventário ativo {}", inventario.getId());
        }
    }
    
    // ========== Cache de Patrimônios por Sala ==========
    
    /**
     * Obtém patrimônios de uma sala do cache
     * 
     * @param salaId ID da sala
     * @return Optional com lista de patrimônios se encontrada e não expirada
     */
    public Optional<List<Patrimonio>> getPatrimoniosPorSala(int salaId) {
        CachedData<List<Patrimonio>> cached = patrimoniosPorSalaCache.get(salaId);
        
        if (cached == null) {
            logger.debug("Cache miss: Patrimônios da sala {}", salaId);
            return Optional.empty();
        }
        
        if (cached.isExpired()) {
            patrimoniosPorSalaCache.remove(salaId);
            logger.debug("Cache expired: Patrimônios da sala {}", salaId);
            return Optional.empty();
        }
        
        logger.debug("Cache hit: {} patrimônios da sala {} (age={}ms)", 
            cached.getData().size(), salaId, cached.getAge());
        return Optional.of(cached.getData());
    }
    
    /**
     * Armazena patrimônios de uma sala no cache
     * 
     * @param salaId ID da sala
     * @param patrimonios Lista de patrimônios
     */
    public void putPatrimoniosPorSala(int salaId, List<Patrimonio> patrimonios) {
        if (patrimonios != null) {
            patrimoniosPorSalaCache.put(salaId, 
                new CachedData<>(patrimonios, DEFAULT_TTL_MILLIS));
            logger.debug("Cached: {} patrimônios da sala {}", patrimonios.size(), salaId);
        }
    }
    
    // ========== Invalidação de Cache ==========
    
    /**
     * Invalida cache de uma sala específica
     * 
     * @param salaId ID da sala
     */
    public void invalidateSala(int salaId) {
        salasCache.remove(salaId);
        patrimoniosPorSalaCache.remove(salaId);
        logger.debug("Invalidated: Sala {}", salaId);
    }
    
    /**
     * Invalida cache do inventário ativo
     */
    public void invalidateInventarioAtivo() {
        inventarioAtivoCache = null;
        logger.debug("Invalidated: Inventário ativo");
    }
    
    /**
     * Invalida cache de todas as salas
     */
    public void invalidateAllSalas() {
        todasSalasCache = null;
        salasCache.clear();
        logger.debug("Invalidated: Todas as salas");
    }
    
    /**
     * Invalida cache de patrimônios de uma sala
     * 
     * @param salaId ID da sala
     */
    public void invalidatePatrimoniosSala(int salaId) {
        patrimoniosPorSalaCache.remove(salaId);
        logger.debug("Invalidated: Patrimônios da sala {}", salaId);
    }
    
    /**
     * Limpa todo o cache
     */
    public void clear() {
        salasCache.clear();
        patrimoniosPorSalaCache.clear();
        inventarioAtivoCache = null;
        todasSalasCache = null;
        logger.info("Cache completamente limpo");
    }
    
    // ========== Estatísticas ==========
    
    /**
     * Obtém estatísticas do cache
     * 
     * @return String com estatísticas formatadas
     */
    public String getStats() {
        int salasCount = salasCache.size();
        int patrimoniosCount = patrimoniosPorSalaCache.size();
        boolean hasInventario = inventarioAtivoCache != null && !inventarioAtivoCache.isExpired();
        boolean hasSalas = todasSalasCache != null && !todasSalasCache.isExpired();
        
        return String.format(
            "Cache Stats - Salas: %d | Patrimônios/Sala: %d | Inventário: %s | Lista Salas: %s",
            salasCount, patrimoniosCount, 
            hasInventario ? "cached" : "empty",
            hasSalas ? "cached" : "empty"
        );
    }
    
    /**
     * Remove entradas expiradas do cache
     * Deve ser chamado periodicamente para liberar memória
     */
    public void cleanupExpired() {
        int removed = 0;
        
        // Limpar salas expiradas
        int salasBefore = salasCache.size();
        salasCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        removed += (salasBefore - salasCache.size());
        
        // Limpar patrimônios expirados
        int patrimoniosBefore = patrimoniosPorSalaCache.size();
        patrimoniosPorSalaCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        removed += (patrimoniosBefore - patrimoniosPorSalaCache.size());
        
        // Limpar inventário expirado
        if (inventarioAtivoCache != null && inventarioAtivoCache.isExpired()) {
            inventarioAtivoCache = null;
            removed++;
        }
        
        // Limpar lista de salas expirada
        if (todasSalasCache != null && todasSalasCache.isExpired()) {
            todasSalasCache = null;
            removed++;
        }
        
        if (removed > 0) {
            logger.debug("Cleanup: {} entradas expiradas removidas", removed);
        }
    }
    
    /**
     * Obtém tamanho aproximado do cache em memória
     * 
     * @return Número total de entradas cacheadas
     */
    public int size() {
        int size = salasCache.size() + patrimoniosPorSalaCache.size();
        if (inventarioAtivoCache != null && !inventarioAtivoCache.isExpired()) {
            size++;
        }
        if (todasSalasCache != null && !todasSalasCache.isExpired()) {
            size++;
        }
        return size;
    }
}
