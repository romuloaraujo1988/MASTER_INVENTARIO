package com.inventario.sihcp.config;

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
 * 
 * OTIMIZADO v2.0: Reduzido tamanho do cache para economizar memória
 * - Antes: 10.000 entradas x 8 caches = 80.000 entradas potenciais
 * - Agora: 500 entradas x 4 caches essenciais = 2.000 entradas máximo
 * - Economia estimada: ~200MB de RAM
 */
@Configuration
@EnableCaching
@Profile({"mobile", "performance", "prod"})
public class CacheConfig {

    /**
     * Cache Manager com Caffeine
     * OTIMIZADO: Apenas caches essenciais para API mobile
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "patrimonios",    // Mais acessado
            "salas",          // Frequente
            "usuarios",       // Autenticação
            "estatisticas"    // Dashboard
            // REMOVIDOS: responsaveis, descricoes, inventarios, coletas
            // Esses dados mudam frequentemente, cache não é eficiente
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuração do Caffeine - OTIMIZADA para baixo consumo
     * - maximumSize: Reduzido de 10.000 para 500
     * - expireAfterWrite: Reduzido de 30 para 10 minutos
     * - expireAfterAccess: Reduzido de 1 hora para 15 minutos
     * - recordStats: DESABILITADO (consome memória)
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(500)                        // Era: 10000
            .expireAfterWrite(10, TimeUnit.MINUTES)  // Era: 30
            .expireAfterAccess(15, TimeUnit.MINUTES); // Era: 1 hora
            // .recordStats() REMOVIDO - consome memória
    }
}
