package com.inventario.sihcp.view.helper;

import com.inventario.sihcp.cache.CacheManager;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Sala;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Helper para integração de cache no ColetaFrame_v2
 * 
 * Fornece métodos otimizados que verificam cache antes de consultar banco.
 * Reduz latência e melhora performance da interface.
 * 
 * @see Requirements 3.2, 3.3, 3.4
 */
public class ColetaFrameCacheHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(ColetaFrameCacheHelper.class);
    
    private final CacheManager cacheManager;
    
    /**
     * Construtor
     */
    public ColetaFrameCacheHelper() {
        this.cacheManager = CacheManager.getInstance();
    }
    
    /**
     * Obtém uma sala específica com cache
     * 
     * @param salaId ID da sala
     * @return Optional com sala se encontrada no cache
     */
    public Optional<Sala> obterSalaDoCache(int salaId) {
        long startTime = System.currentTimeMillis();
        
        Optional<Sala> cached = cacheManager.getSala(salaId);
        if (cached.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Sala {} carregada do cache em {}ms", salaId, duration);
        }
        
        return cached;
    }
    
    /**
     * Armazena uma sala no cache
     * 
     * @param sala Sala a ser cacheada
     */
    public void cachearSala(Sala sala) {
        if (sala != null) {
            cacheManager.putSala(sala);
            logger.debug("Sala {} armazenada no cache", sala.getId());
        }
    }
    
    /**
     * Obtém inventário ativo do cache
     * 
     * @return Optional com inventário se encontrado no cache
     */
    public Optional<Inventario> obterInventarioAtivoDoCache() {
        long startTime = System.currentTimeMillis();
        
        Optional<Inventario> cached = cacheManager.getInventarioAtivo();
        if (cached.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Inventário ativo carregado do cache em {}ms", duration);
        }
        
        return cached;
    }
    
    /**
     * Armazena inventário ativo no cache
     * 
     * @param inventario Inventário a ser cacheado
     */
    public void cachearInventarioAtivo(Inventario inventario) {
        if (inventario != null) {
            cacheManager.putInventarioAtivo(inventario);
            logger.debug("Inventário ativo {} armazenado no cache", inventario.getId());
        }
    }
    
    /**
     * Invalida cache após registrar coleta
     * Garante que dados sejam recarregados na próxima consulta
     * 
     * @param salaId ID da sala onde foi registrada a coleta
     */
    public void invalidarCacheAposColeta(int salaId) {
        logger.debug("Invalidando cache após coleta na sala {}", salaId);
        cacheManager.invalidatePatrimoniosSala(salaId);
        // Não invalidar sala pois dados da sala não mudam
    }
    
    /**
     * Limpa todo o cache
     * Útil ao trocar de inventário ou fazer refresh completo
     */
    public void limparCache() {
        logger.info("Limpando todo o cache");
        cacheManager.clear();
    }
    
    /**
     * Obtém estatísticas do cache
     * 
     * @return String com estatísticas formatadas
     */
    public String obterEstatisticasCache() {
        return cacheManager.getStats();
    }
    
    /**
     * Executa limpeza de entradas expiradas
     * Deve ser chamado periodicamente (ex: a cada 5 minutos)
     */
    public void executarLimpezaCache() {
        logger.debug("Executando limpeza de cache");
        cacheManager.cleanupExpired();
    }
}
