# Estratégias de Escalabilidade - Servidor Mobile API

## 📊 Situação Atual

**Arquitetura:**
- Spring Boot standalone
- Conexão direta com PostgreSQL
- Sem cache distribuído
- Sem balanceamento de carga

**Limitações:**
- 1 instância = ~100-200 usuários simultâneos
- Sem redundância
- Sem distribuição de carga

---

## 🚀 Estratégias de Escalabilidade

### 1. Escalabilidade Vertical (Scale Up)

**Aumentar recursos da máquina atual**

#### Configurações JVM Otimizadas

```bash
# Aumentar heap memory
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -jar sistema-inventario.jar --spring.profiles.active=mobile
```

#### Configurações Spring Boot

```properties
# application-mobile.properties

# Thread Pool
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=20
server.tomcat.accept-count=100

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Compressão
server.compression.enabled=true
server.compression.mime-types=application/json,text/json
server.compression.min-response-size=1024
```

**Resultado:** 200-400 usuários simultâneos

---

### 2. Escalabilidade Horizontal (Scale Out)

**Múltiplas instâncias com Load Balancer**

#### Arquitetura Recomendada

```
                    ┌─────────────────┐
                    │  Load Balancer  │
                    │    (Nginx)      │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
    ┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
    │  Mobile API  │ │  Mobile API │ │  Mobile API │
    │  Instance 1  │ │  Instance 2 │ │  Instance 3 │
    │  :8080       │ │  :8081      │ │  :8082      │
    └───────┬──────┘ └──────┬──────┘ └──────┬──────┘
            │                │                │
            └────────────────┼────────────────┘
                             │
                    ┌────────▼────────┐
                    │   PostgreSQL    │
                    │   (Master)      │
                    └─────────────────┘
```

#### Configuração Nginx

```nginx
# /etc/nginx/nginx.conf

upstream mobile_api {
    least_conn;  # Algoritmo de balanceamento
    
    server localhost:8080 weight=1 max_fails=3 fail_timeout=30s;
    server localhost:8081 weight=1 max_fails=3 fail_timeout=30s;
    server localhost:8082 weight=1 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name api.inventario.ifmt.edu.br;
    
    # Compressão
    gzip on;
    gzip_types application/json text/json;
    gzip_min_length 1000;
    
    # Timeout
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;
    
    location /api/mobile/ {
        proxy_pass http://mobile_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Health check
        proxy_next_upstream error timeout http_502 http_503 http_504;
    }
}
```

#### Scripts de Deploy

```bash
# start-mobile-cluster.sh

#!/bin/bash

# Iniciar 3 instâncias
for port in 8080 8081 8082; do
    java -Xms1g -Xmx2g \
         -Dserver.port=$port \
         -jar sistema-inventario.jar \
         --spring.profiles.active=mobile &
    
    echo "Instância iniciada na porta $port"
done

echo "Cluster iniciado com 3 instâncias"
```

**Resultado:** 600-1200 usuários simultâneos

---

### 3. Cache Distribuído (Redis)

**Reduzir carga no banco de dados**

#### Adicionar Dependência

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### Configuração Redis

```properties
# application-mobile.properties

# Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.timeout=60000
spring.redis.jedis.pool.max-active=20
spring.redis.jedis.pool.max-idle=10
spring.redis.jedis.pool.min-idle=5

# Cache
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
```

#### Implementação

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

// No Service
@Service
public class MobileSalaService {
    
    @Cacheable(value = "salas", key = "#page + '_' + #size")
    public List<MobileSalaDTO> listarSalas(int page, int size) {
        // Buscar do banco apenas se não estiver em cache
    }
    
    @CacheEvict(value = "salas", allEntries = true)
    public void limparCache() {
        // Limpar cache quando houver alterações
    }
}
```

**Resultado:** 
- 80% menos queries no banco
- Resposta 10x mais rápida
- 1000+ usuários simultâneos

---

### 4. Database Replication (Read Replicas)

**Separar leitura e escrita**

```
                    ┌─────────────────┐
                    │  Mobile API     │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │ (Write)        │ (Read)         │
    ┌───────▼──────┐         │         ┌──────▼──────┐
    │  PostgreSQL  │         │         │ PostgreSQL  │
    │   Master     │─────────┼────────▶│  Replica 1  │
    │  (Write)     │ Replication       │   (Read)    │
    └──────────────┘         │         └─────────────┘
                             │
                             │         ┌─────────────┐
                             └────────▶│ PostgreSQL  │
                                       │  Replica 2  │
                                       │   (Read)    │
                                       └─────────────┘
```

#### Configuração Spring Boot

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://master:5432/inventario")
            .build();
    }
    
    @Bean
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://replica:5432/inventario")
            .build();
    }
    
    @Bean
    public DataSource routingDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("write", dataSource());
        targetDataSources.put("read", readDataSource());
        
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(dataSource());
        
        return routingDataSource;
    }
}

// Annotation customizada
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {
}

// Aspect para rotear queries
@Aspect
@Component
public class DataSourceRoutingAspect {
    
    @Around("@annotation(ReadOnly)")
    public Object routeToReadReplica(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSourceContextHolder.setDataSourceType("read");
        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}

// Uso
@Service
public class MobileSalaService {
    
    @ReadOnly
    public List<MobileSalaDTO> listarSalas() {
        // Vai para replica de leitura
    }
    
    public void salvarSala(Sala sala) {
        // Vai para master (escrita)
    }
}
```

**Resultado:** 
- Leituras não impactam escritas
- 2000+ usuários simultâneos

---

### 5. CDN para Assets Estáticos

**Reduzir carga no servidor**

```nginx
# Nginx como CDN local

server {
    listen 80;
    server_name cdn.inventario.ifmt.edu.br;
    
    location /static/ {
        alias /var/www/inventario/static/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

---

### 6. Rate Limiting

**Proteger contra sobrecarga**

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 req/s
    }
}

@RestController
public class MobileSalaController {
    
    @Autowired
    private RateLimiter rateLimiter;
    
    @GetMapping("/api/mobile/salas")
    public ResponseEntity<?> listarSalas() {
        if (!rateLimiter.tryAcquire()) {
            return ResponseEntity.status(429)
                .body("Too many requests");
        }
        
        // Processar requisição
    }
}
```

---

### 7. Async Processing

**Operações pesadas em background**

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class SyncService {
    
    @Async
    public CompletableFuture<Void> sincronizarDados() {
        // Processar em background
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## 📈 Comparação de Estratégias

| Estratégia | Custo | Complexidade | Usuários | Tempo Impl. |
|------------|-------|--------------|----------|-------------|
| **Vertical** | Baixo | Baixa | 200-400 | 1 dia |
| **Horizontal** | Médio | Média | 600-1200 | 3-5 dias |
| **Cache Redis** | Baixo | Média | 1000+ | 2-3 dias |
| **Read Replicas** | Alto | Alta | 2000+ | 5-7 dias |
| **CDN** | Baixo | Baixa | +20% | 1 dia |
| **Rate Limiting** | Baixo | Baixa | Proteção | 1 dia |
| **Async** | Baixo | Média | +30% | 2-3 dias |

---

## 🎯 Roadmap Recomendado

### Fase 1: Quick Wins (1 semana)
1. ✅ Otimizar JVM e Thread Pool
2. ✅ Adicionar compressão
3. ✅ Implementar Rate Limiting
4. ✅ Otimizar queries SQL (índices)

**Resultado:** 200 → 400 usuários

### Fase 2: Cache (2 semanas)
1. ✅ Instalar Redis
2. ✅ Implementar cache de salas
3. ✅ Implementar cache de patrimônios
4. ✅ Cache de autenticação (JWT)

**Resultado:** 400 → 1000 usuários

### Fase 3: Horizontal Scaling (3 semanas)
1. ✅ Configurar Nginx
2. ✅ Deploy de 3 instâncias
3. ✅ Health checks
4. ✅ Monitoramento

**Resultado:** 1000 → 1500 usuários

### Fase 4: Database Optimization (4 semanas)
1. ✅ Configurar Read Replicas
2. ✅ Implementar roteamento
3. ✅ Otimizar queries pesadas
4. ✅ Connection pooling

**Resultado:** 1500 → 2500 usuários

---

## 🔧 Ferramentas de Monitoramento

### Spring Boot Actuator

```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

### Prometheus + Grafana

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'mobile-api'
    static_configs:
      - targets: ['localhost:8080', 'localhost:8081', 'localhost:8082']
```

### Métricas Importantes

- **Throughput**: Requisições/segundo
- **Latency**: Tempo de resposta (p50, p95, p99)
- **Error Rate**: % de erros
- **CPU/Memory**: Uso de recursos
- **Database**: Connection pool, query time

---

## 💰 Estimativa de Custos (Mensal)

### Cenário 1: Vertical (400 usuários)
- Servidor: R$ 200/mês
- **Total: R$ 200/mês**

### Cenário 2: Horizontal + Redis (1000 usuários)
- 3 Servidores: R$ 600/mês
- Redis: R$ 100/mês
- Load Balancer: R$ 50/mês
- **Total: R$ 750/mês**

### Cenário 3: Full Stack (2500 usuários)
- 5 Servidores: R$ 1000/mês
- Redis Cluster: R$ 300/mês
- PostgreSQL Replicas: R$ 400/mês
- Load Balancer: R$ 100/mês
- CDN: R$ 50/mês
- **Total: R$ 1850/mês**

---

## 🚨 Alertas e SLA

### Definir SLAs

- **Disponibilidade**: 99.5% (3.6h downtime/mês)
- **Latência**: p95 < 500ms
- **Error Rate**: < 1%

### Alertas Críticos

```yaml
# alertmanager.yml
alerts:
  - name: HighErrorRate
    condition: error_rate > 5%
    action: notify_team
    
  - name: HighLatency
    condition: p95_latency > 1000ms
    action: scale_up
    
  - name: HighCPU
    condition: cpu_usage > 80%
    action: notify_ops
```

---

## 📚 Referências

- [Spring Boot Performance Tuning](https://spring.io/guides/gs/spring-boot/)
- [PostgreSQL Replication](https://www.postgresql.org/docs/current/high-availability.html)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)
- [Nginx Load Balancing](https://nginx.org/en/docs/http/load_balancing.html)

---

**Última atualização:** 13/11/2025  
**Versão:** 1.0  
**Autor:** Sistema de Inventário IFMT
