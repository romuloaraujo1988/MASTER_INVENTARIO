# Análise de Uso de Memória - Servidor Mobile

## 📊 Problemas Identificados

### 1. **MobileOfflineSyncService - CRÍTICO** ⚠️
**Arquivo:** `src/main/java/com/inventario/mobile/server/service/MobileOfflineSyncService.java`

**Problema:** O endpoint `/api/mobile/sync/offline-data` carrega TODOS os patrimônios em memória de uma vez.

```java
List<Patrimonio> patrimonios = patrimonioDAO.listarTodosComJoins();
```

**Impacto:** Com 10.809 patrimônios, isso pode consumir ~50-100MB por requisição.

**Solução Proposta:**
- Implementar paginação no endpoint de sync offline
- Ou usar streaming para enviar dados em chunks
- Limitar a 5.000 patrimônios por sync e fazer múltiplas requisições

---

### 2. **PatrimonioDAO.listarTodosComJoins()** ⚠️
**Arquivo:** `src/main/java/com/inventario/dao/PatrimonioDAO.java`

**Problema:** Carrega TODOS os patrimônios com JOINs em uma única query.

**Impacto:** ~10.809 objetos Patrimonio em memória simultaneamente.

**Solução Proposta:**
- Criar método `listarTodosComJoinsPaginado(int page, int size)`
- Usar LIMIT/OFFSET no SQL

---

### 3. **MobilePatrimonioService.listarPatrimonios()** ⚠️
**Arquivo:** `src/main/java/com/inventario/mobile/server/service/MobilePatrimonioService.java`

**Problema:** Mesmo com paginação no controller, o service pode carregar muitos dados.

**Solução:** Verificar se a paginação está sendo aplicada no SQL.

---

### 4. **Conversão de DTOs em Loop**
**Problema:** Cada conversão de Patrimonio para DTO cria novos objetos.

```java
List<PatrimonioOfflineDTO> patrimoniosDTO = patrimonios.stream()
    .map(this::converterPatrimonioParaDTO)
    .collect(Collectors.toList());
```

**Impacto:** Duplica o uso de memória temporariamente.

**Solução:** Processar em batches e liberar memória entre batches.

---

## 🔧 Soluções Recomendadas

### Solução 1: Paginação no Sync Offline (PRIORITÁRIA)

```java
@GetMapping("/offline-data")
public ResponseEntity<ApiResponse<MobileOfflineDataDTO>> buscarDadosOffline(
    @RequestParam(required = false) Integer inventarioId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "2000") int size  // Máximo 2000 por página
) {
    // Buscar patrimônios paginados
    List<Patrimonio> patrimonios = patrimonioDAO.listarTodosComJoinsPaginado(page, size);
    // ...
}
```

### Solução 2: Streaming de Dados

```java
@GetMapping(value = "/offline-data/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
public Flux<PatrimonioOfflineDTO> streamDadosOffline() {
    return Flux.fromIterable(patrimonioDAO.listarTodosComJoins())
        .map(this::converterPatrimonioParaDTO);
}
```

### Solução 3: Compressão GZIP (Já Implementada?)

Verificar se o Spring está configurado para comprimir respostas grandes:

```properties
server.compression.enabled=true
server.compression.min-response-size=1024
server.compression.mime-types=application/json
```

### Solução 4: Cache de Dados Estáticos

Patrimônios, salas e responsáveis raramente mudam. Usar cache:

```java
@Cacheable(value = "patrimonios", key = "#page + '-' + #size")
public List<PatrimonioOfflineDTO> buscarPatrimoniosPaginados(int page, int size) {
    // ...
}
```

### Solução 5: Limitar Tamanho de Resposta

```java
// Já implementado, mas pode ser mais agressivo
private static final int MAX_PATRIMONIOS_POR_SYNC = 5000;  // Reduzir de 10000
```

---

## 📈 Métricas Atuais

| Recurso | Quantidade | Memória Estimada |
|---------|------------|------------------|
| Patrimônios | 10.809 | ~50-100 MB |
| Salas | ~500 | ~5 MB |
| Responsáveis | ~200 | ~2 MB |
| **Total por Sync** | - | **~60-110 MB** |

---

## ✅ Ações Imediatas

1. [ ] Reduzir `MAX_PATRIMONIOS_POR_SYNC` de 10.000 para 5.000
2. [ ] Implementar paginação no endpoint `/offline-data`
3. [ ] Verificar se compressão GZIP está ativa
4. [ ] Adicionar logs de memória antes/depois de cada sync
5. [ ] Considerar usar streaming para grandes volumes

---

## 🔍 Monitoramento

O `MemoryOptimizationConfig.java` já monitora memória a cada 30s e faz limpeza quando ultrapassa 512MB. Isso é bom, mas não resolve o problema de pico de memória durante o sync.

**Recomendação:** Adicionar log de memória no início e fim de cada requisição de sync:

```java
@GetMapping("/offline-data")
public ResponseEntity<?> buscarDadosOffline(...) {
    Runtime runtime = Runtime.getRuntime();
    long memBefore = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    logger.info("📊 Memória ANTES do sync: {}MB", memBefore);
    
    // ... processar ...
    
    long memAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    logger.info("📊 Memória DEPOIS do sync: {}MB (delta: {}MB)", memAfter, memAfter - memBefore);
}
```

---

**Data:** 02/12/2025
**Versão:** 1.0
