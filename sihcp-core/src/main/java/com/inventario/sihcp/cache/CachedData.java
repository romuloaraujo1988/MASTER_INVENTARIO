package com.inventario.sihcp.cache;

/**
 * Wrapper para dados em cache com timestamp e TTL
 * 
 * Armazena dados junto com informações de tempo para controle de expiração.
 * Thread-safe e imutável.
 * 
 * @param <T> Tipo dos dados armazenados
 * @see Requirements 3.5
 */
public class CachedData<T> {
    
    private final T data;
    private final long timestamp;
    private final long ttlMillis;
    
    /**
     * Cria um novo CachedData com TTL especificado
     * 
     * @param data Dados a serem armazenados
     * @param ttlMillis Tempo de vida em milissegundos
     */
    public CachedData(T data, long ttlMillis) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.ttlMillis = ttlMillis;
    }
    
    /**
     * Verifica se os dados expiraram
     * 
     * @return true se o tempo de vida foi excedido
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > ttlMillis;
    }
    
    /**
     * Obtém os dados armazenados
     * 
     * @return Dados do cache
     */
    public T getData() {
        return data;
    }
    
    /**
     * Obtém a idade dos dados em milissegundos
     * 
     * @return Tempo decorrido desde a criação
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Obtém o timestamp de criação
     * 
     * @return Timestamp em milissegundos
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Obtém o TTL configurado
     * 
     * @return TTL em milissegundos
     */
    public long getTtlMillis() {
        return ttlMillis;
    }
    
    /**
     * Obtém o tempo restante até expiração
     * 
     * @return Milissegundos restantes, ou 0 se já expirou
     */
    public long getTimeToLive() {
        long remaining = ttlMillis - getAge();
        return Math.max(0, remaining);
    }
    
    /**
     * Verifica se os dados ainda são válidos (não expiraram)
     * 
     * @return true se ainda válidos
     */
    public boolean isValid() {
        return !isExpired();
    }
    
    @Override
    public String toString() {
        return String.format("CachedData{age=%dms, ttl=%dms, expired=%b}", 
            getAge(), ttlMillis, isExpired());
    }
}
