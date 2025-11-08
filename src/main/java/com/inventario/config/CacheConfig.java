package com.inventario.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração de cache para melhorar performance e escalabilidade
 * 
 * Em produção, considere usar Redis para cache distribuído entre múltiplas instâncias
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache em memória para desenvolvimento e instância única
     * Para produção com múltiplas instâncias, usar Redis
     */
    @Bean
    @Profile("!production")
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "dashboard-stats",      // Estatísticas do dashboard
            "usuarios",             // Lista de usuários
            "patrimonios",          // Patrimônios (cache de curta duração)
            "coletas-recentes",     // Coletas recentes
            "salas",                // Lista de salas
            "inventarios"           // Lista de inventários
        );
        return cacheManager;
    }

    /**
     * Configuração para cache distribuído com Redis (produção)
     * 
     * Descomentar e configurar quando tiver Redis disponível:
     * 
     * @Bean
     * @Profile("production")
     * public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
     *     RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
     *         .entryTtl(Duration.ofMinutes(10))
     *         .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
     *         .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
     *         .disableCachingNullValues();
     *
     *     Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
     *     cacheConfigurations.put("dashboard-stats", config.entryTtl(Duration.ofMinutes(5)));
     *     cacheConfigurations.put("usuarios", config.entryTtl(Duration.ofMinutes(15)));
     *     cacheConfigurations.put("patrimonios", config.entryTtl(Duration.ofMinutes(2)));
     *     cacheConfigurations.put("coletas-recentes", config.entryTtl(Duration.ofMinutes(1)));
     *
     *     return RedisCacheManager.builder(connectionFactory)
     *         .cacheDefaults(config)
     *         .withInitialCacheConfigurations(cacheConfigurations)
     *         .build();
     * }
     */
}
