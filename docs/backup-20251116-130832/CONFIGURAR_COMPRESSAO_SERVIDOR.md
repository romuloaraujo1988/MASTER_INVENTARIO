# Configurar Compressão GZIP no Servidor

## Spring Boot - Configuração

### 1. Adicionar em `application-mobile.properties`

```properties
# ===== COMPRESSÃO GZIP =====
# Habilitar compressão de respostas HTTP
server.compression.enabled=true

# Tipos MIME que serão comprimidos
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain,application/javascript,text/css

# Tamanho mínimo para comprimir (1 KB)
# Respostas menores que isso não serão comprimidas
server.compression.min-response-size=1024

# Nível de compressão (1-9, onde 9 é máximo)
# Recomendado: 6 (bom balanço entre compressão e CPU)
server.compression.level=6
```

### 2. Verificar se está funcionando

#### Teste com cURL:

```bash
# Requisição com Accept-Encoding: gzip
curl -H "Accept-Encoding: gzip" \
     -H "Authorization: Bearer SEU_TOKEN" \
     -i http://localhost:8081/inventario/api/mobile/dashboard/stats

# Deve retornar header:
# Content-Encoding: gzip
```

#### Teste no navegador:

1. Abrir DevTools (F12)
2. Ir para aba Network
3. Fazer requisição para API
4. Verificar headers da resposta:
   - `Content-Encoding: gzip` ✅
   - `Content-Length: [tamanho comprimido]`

### 3. Logs para Debug

Adicionar em `application-mobile.properties`:

```properties
# Logs de compressão
logging.level.org.springframework.boot.web.embedded.tomcat.TomcatWebServer=DEBUG
logging.level.org.apache.coyote.http11=DEBUG
```

Verificar nos logs:

```
DEBUG o.a.coyote.http11.Http11Processor : Compression: on
DEBUG o.a.coyote.http11.Http11Processor : Content-Encoding: gzip
```

## Benefícios da Compressão

### Exemplo Real:

```json
// Resposta sem compressão
{
  "success": true,
  "data": {
    "totalPatrimonios": 1500,
    "patrimoniosColetados": 450,
    "patrimoniosPendentes": 1050,
    // ... mais dados
  }
}

Tamanho: 2.5 KB
```

```
// Mesma resposta com GZIP
[dados binários comprimidos]

Tamanho: 0.5 KB (80% de redução!)
```

### Economia por Endpoint:

| Endpoint | Sem GZIP | Com GZIP | Economia |
|----------|----------|----------|----------|
| `/dashboard/stats` | 2 KB | 0.4 KB | 80% |
| `/coletas?page=0` | 50 KB | 10 KB | 80% |
| `/patrimonio` | 100 KB | 20 KB | 80% |
| `/coletas/batch` | 200 KB | 40 KB | 80% |

### Impacto em 1000 Usuários/Dia:

```
Sem GZIP:
- 1000 usuários × 10 requisições × 50 KB = 500 MB/dia
- 15 GB/mês
- 180 GB/ano

Com GZIP:
- 1000 usuários × 10 requisições × 10 KB = 100 MB/dia
- 3 GB/mês
- 36 GB/ano

ECONOMIA: 144 GB/ano (80%)
```

## Troubleshooting

### Problema: Compressão não está funcionando

#### Verificar 1: Tamanho da resposta
```
Se resposta < 1 KB, não será comprimida
Solução: Ajustar server.compression.min-response-size
```

#### Verificar 2: Tipo MIME
```
Se Content-Type não está na lista, não será comprimido
Solução: Adicionar tipo em server.compression.mime-types
```

#### Verificar 3: Cliente não suporta
```
Se cliente não envia Accept-Encoding: gzip, não será comprimido
Solução: Verificar CompressionInterceptor no app
```

#### Verificar 4: Proxy/Load Balancer
```
Proxy pode estar removendo compressão
Solução: Configurar proxy para preservar Content-Encoding
```

### Problema: Performance do servidor piorou

```
Compressão usa CPU
Solução: Reduzir nível de compressão
server.compression.level=4  # Ao invés de 6
```

### Problema: Alguns clientes não descomprimem

```
Clientes antigos podem não suportar GZIP
Solução: Servidor detecta automaticamente e não comprime
```

## Monitoramento

### Métricas Importantes:

1. **Taxa de Compressão**
```java
@Component
public class CompressionMetrics {
    private final MeterRegistry registry;
    
    public void recordCompression(long original, long compressed) {
        double ratio = (1.0 - (compressed / (double) original)) * 100;
        registry.gauge("compression.ratio", ratio);
    }
}
```

2. **Tempo de Compressão**
```java
long start = System.currentTimeMillis();
// Compressão acontece aqui
long duration = System.currentTimeMillis() - start;
log.debug("Compressão levou {}ms", duration);
```

3. **Uso de CPU**
```bash
# Monitorar CPU do servidor
top -p $(pgrep -f "MobileApiApplication")
```

## Configurações Avançadas

### Para Alta Performance:

```properties
# Compressão mais leve (menos CPU)
server.compression.level=4

# Comprimir apenas respostas grandes
server.compression.min-response-size=2048
```

### Para Máxima Economia de Banda:

```properties
# Compressão máxima (mais CPU)
server.compression.level=9

# Comprimir tudo
server.compression.min-response-size=512
```

### Para Balanceamento:

```properties
# Configuração recomendada
server.compression.level=6
server.compression.min-response-size=1024
```

## Testes de Carga

### Antes de Ativar em Produção:

```bash
# Teste com Apache Bench
ab -n 1000 -c 10 \
   -H "Accept-Encoding: gzip" \
   -H "Authorization: Bearer TOKEN" \
   http://localhost:8081/inventario/api/mobile/coletas

# Verificar:
# - Requests per second
# - Time per request
# - Transfer rate
```

### Comparar Com e Sem Compressão:

```bash
# Sem compressão
ab -n 1000 -c 10 http://localhost:8081/api/mobile/coletas

# Com compressão
ab -n 1000 -c 10 \
   -H "Accept-Encoding: gzip" \
   http://localhost:8081/api/mobile/coletas

# Comparar Transfer rate
```

## Checklist de Implementação

- [ ] Adicionar configurações em `application-mobile.properties`
- [ ] Reiniciar servidor
- [ ] Testar com cURL
- [ ] Verificar logs
- [ ] Testar no app Android
- [ ] Monitorar CPU do servidor
- [ ] Fazer teste de carga
- [ ] Documentar resultados
- [ ] Ativar em produção

## Conclusão

Compressão GZIP é uma otimização **essencial** que:

- ✅ Reduz uso de banda em 80%
- ✅ Melhora velocidade de download
- ✅ Economiza dados móveis dos usuários
- ✅ Fácil de implementar (apenas configuração)
- ✅ Suportado por todos os clientes modernos

**Recomendação**: Ativar SEMPRE em produção!
