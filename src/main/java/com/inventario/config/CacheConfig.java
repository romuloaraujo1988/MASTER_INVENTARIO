package com.inventario.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de Cache de Alta Performance
 * Usa Caffeine para cache em memória otimizado
 */
@Configuration
@EnableCaching
@Profile({"mobile", "performance", "prod"})
public class CacheConfig {

    /**
     * Cache Manager com Caffeine
     * Caffeine é mais rápido que Guava e EhCache
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "patrimonios",
            "salas", 
            "responsaveis",
            "descricoes",
            "inventarios",
            "coletas",
            "usuarios",
            "estatisticas"
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuração do Caffeine
     * - maximumSize: Máximo de entradas no cache
     * - expireAfterWrite: Expira após 30 minutos de escrita
     * - expireAfterAccess: Expira após 1 hora sem acesso
     * - recordStats: Habilita estatísticas de cache
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .recordStats();
    }
}
