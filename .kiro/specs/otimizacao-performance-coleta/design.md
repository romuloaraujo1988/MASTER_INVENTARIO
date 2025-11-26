# Design Document - Otimização de Performance na Coleta Patrimonial Desktop

## Overview

Este documento detalha o design técnico para otimização de performance do ColetaFrame_v2, focando em eliminar lentidões e proporcionar uma experiência ágil e responsiva. As otimizações abrangem desde a camada de interface (Swing) até o banco de dados (PostgreSQL), implementando padrões de concorrência, cache inteligente e queries otimizadas.

## Architecture

### Arquitetura Atual vs. Otimizada

```
┌─────────────────────────────────────────────────────────────────┐
│                    ANTES (Problemático)                          │
├─────────────────────────────────────────────────────────────────┤
│  EDT (Event Dispatch Thread)                                     │
│    ↓ BLOQUEIO                                                    │
│  Operação de Banco (SELECT * FROM patrimonio)                   │
│    ↓ BLOQUEIO                                                    │
│  Atualização da JTable (todas as linhas)                        │
│    ↓ BLOQUEIO                                                    │
│  UI Congelada (usuário espera)                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    DEPOIS (Otimizado)                            │
├─────────────────────────────────────────────────────────────────┤
│  EDT (Event Dispatch Thread)                                     │
│    ↓ NÃO BLOQUEIA                                               │
│  SwingWorker.execute()                                          │
│    ↓                                                            │
│  Background Thread                                               │
│    ├─ Verifica Cache                                            │
│    ├─ Query Otimizada (LIMIT 100, índices)                     │
│    ├─ Connection Pool                                            │
│    └─ Processa Dados                                            │
│         ↓                                                        │
│  SwingWorker.done()                                             │
│    ↓                                                            │
│  EDT: Atualiza UI (apenas dados visíveis)                      │
│    ↓                                                            │
│  UI Responsiva (usuário continua trabalhando)                   │
└─────────────────────────────────────────────────────────────────┘
```

### Camadas de Otimização

```
┌─────────────────────────────────────────────────────────────────┐
│                    Camada de Apresentação                        │
│  - ColetaFrame_v2 (Swing)                                       │
│  - SwingWorker para operações assíncronas                       │
│  - Debounce em campos de busca                                  │
│  - Feedback visual imediato                                     │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                    Camada de Cache                               │
│  - CacheManager (singleton)                                     │
│  - Cache de Salas (Map<Integer, Sala>)                         │
│  - Cache de Inventário Ativo                                    │
│  - TTL de 5 minutos                                             │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                    Camada de Acesso a Dados                      │
│  - PatrimonioDAO (otimizado)                                    │
│  - ColetaDAO (otimizado)                                        │
│  - PreparedStatements                                            │
│  - Queries com índices                                          │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                    Connection Pool                               │
│  - HikariCP                                                     │
│  - 5-10 conexões ativas                                         │
│  - Validação automática                                         │
│  - Timeout configurável                                         │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                    PostgreSQL                                    │
│  - Índices em colunas de busca                                  │
│  - VACUUM e ANALYZE periódicos                                  │
│  - Estatísticas atualizadas                                     │
└─────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. CacheManager (Novo Componente)

```java
/**
 * Gerenciador de cache em memória para dados frequentes
 * Singleton thread-safe
 */
public class CacheManager {
    private static final CacheManager INSTANCE = new CacheManager();
    private static final long TTL_MILLIS = 5 * 60 * 1000; // 5 minutos
    
    // Caches
    private Map<Integer, CachedData<Sala>> salasCache;
    private CachedData<Inventario> inventarioAtivoCache;
    private Map<Integer, CachedData<List<Patrimonio>>> patrimoniosPorSalaCache;
    
    // Métodos principais
    public static CacheManager getInstance();
    public Optional<Sala> getSala(int id);
    public void putSala(Sala sala);
    public Optional<Inventario> getInventarioAtivo();
    public void putInventarioAtivo(Inventario inventario);
    public Optional<List<Patrimonio>> getPatrimoniosPorSala(int salaId);
    public void putPatrimoniosPorSala(int salaId, List<Patrimonio> patrimonios);
    public void invalidate(String cacheKey);
    public void clear();
    
    // Classe interna para dados com timestamp
    private static class CachedData<T> {
        private final T data;
        private final long timestamp;
        
        public boolean isExpired();
        public T getData();
    }
}
```

### 2. OptimizedSwingWorker (Classe Base)

```java
/**
 * SwingWorker otimizado com tratamento de erros e logging
 */
public abstract class OptimizedSwingWorker<T, V> extends SwingWorker<T, V> {
    private final String operationName;
    private final long startTime;
    
    public OptimizedSwingWorker(String operationName);
    
    @Override
    protected final T doInBackground() throws Exception {
        try {
            return performOperation();
        } catch (Exception e) {
            logError(e);
            throw e;
        } finally {
            logPerformance();
        }
    }
    
    protected abstract T performOperation() throws Exception;
    
    @Override
    protected void done() {
        try {
            T result = get();
            onSuccess(result);
        } catch (Exception e) {
            onError(e);
        }
    }
    
    protected abstract void onSuccess(T result);
    protected abstract void onError(Exception e);
    
    private void logPerformance();
    private void logError(Exception e);
}
```

### 3. DebouncedSearchField (Componente Reutilizável)

```java
/**
 * JTextField com debounce automático
 */
public class DebouncedSearchField extends JTextField {
    private Timer debounceTimer;
    private final int delayMs;
    private SearchListener searchListener;
    
    public DebouncedSearchField(int delayMs);
    
    public void setSearchListener(SearchListener listener);
    
    @FunctionalInterface
    public interface SearchListener {
        void onSearch(String searchText);
    }
    
    private void setupDebounce();
    private void cancelPendingSearch();
}
```

### 4. PatrimonioDAO (Otimizado)

```java
/**
 * DAO otimizado com PreparedStatements e índices
 */
public class PatrimonioDAO {
    private final DataSource dataSource;
    
    // Queries otimizadas como constantes
    private static final String BUSCAR_POR_NUMERO = 
        "SELECT p.id, p.numero_patrimonio, p.descricao, p.id_sala, s.nome as sala_nome " +
        "FROM patrimonio p " +
        "LEFT JOIN sala s ON p.id_sala = s.id " +
        "WHERE p.numero_patrimonio = ? " +
        "LIMIT 1";
    
    private static final String BUSCAR_POR_SALA_PAGINADO = 
        "SELECT p.id, p.numero_patrimonio, p.descricao " +
        "FROM patrimonio p " +
        "WHERE p.id_sala = ? AND p.ativo = true " +
        "ORDER BY p.numero_patrimonio " +
        "LIMIT ? OFFSET ?";
    
    private static final String CONTAR_POR_SALA = 
        "SELECT COUNT(*) FROM patrimonio WHERE id_sala = ? AND ativo = true";
    
    // Métodos otimizados
    public Optional<Patrimonio> buscarPorNumero(String numero);
    public List<Patrimonio> buscarPorSalaPaginado(int salaId, int limit, int offset);
    public int contarPorSala(int salaId);
    public List<Patrimonio> buscarPorDescricao(String descricao, int limit);
}
```

### 5. ColetaFrame_v2 (Refatorado)

```java
/**
 * Frame principal otimizado
 */
public class ColetaFrame_v2 extends JFrame {
    // Componentes
    private DebouncedSearchField campoBusca;
    private JTable tabelaHistorico;
    private OptimizedTableModel modeloTabela;
    private JProgressBar progressBar;
    
    // Cache e workers
    private CacheManager cacheManager;
    private ExecutorService executorService;
    
    // Métodos principais
    private void setupComponents();
    private void setupDebouncedSearch();
    private void carregarSalasAsync();
    private void buscarPatrimonioAsync(String numero);
    private void registrarColetaAsync(Coleta coleta);
    private void atualizarTabelaAsync();
    
    // Feedback visual
    private void showLoading();
    private void hideLoading();
    private void showSuccess(String message);
    private void showError(String message);
}
```

## Data Models

### CachedData<T>

```java
public class CachedData<T> {
    private final T data;
    private final long timestamp;
    private final long ttlMillis;
    
    public CachedData(T data, long ttlMillis);
    
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > ttlMillis;
    }
    
    public T getData() {
        return data;
    }
    
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
}
```

### PerformanceMetrics

```java
public class PerformanceMetrics {
    private final String operationName;
    private final long startTime;
    private final long endTime;
    private final boolean success;
    private final String errorMessage;
    
    public PerformanceMetrics(String operationName, long startTime, long endTime, 
                             boolean success, String errorMessage);
    
    public long getDurationMs() {
        return endTime - startTime;
    }
    
    public boolean isSlowOperation() {
        return getDurationMs() > 500;
    }
    
    public String toLogString();
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: EDT Never Blocks

*For any* database operation or heavy computation, the Event Dispatch Thread should never be blocked, ensuring UI remains responsive.

**Validates: Requirements 1.5, 5.1, 5.2, 8.1**

**Test Strategy:**
- Monitor EDT with ThreadMXBean
- Assert no operation blocks EDT for more than 50ms
- Verify all DB operations use SwingWorker

### Property 2: Cache Hit Reduces Latency

*For any* cached data access, retrieval time should be less than 10ms, significantly faster than database query.

**Validates: Requirements 2.3, 3.2, 3.5**

**Test Strategy:**
- Measure cache hit time vs database query time
- Assert cache retrieval < 10ms
- Assert database query > 50ms
- Verify cache hit ratio > 80% for frequent data

### Property 3: Debounce Prevents Excessive Queries

*For any* rapid sequence of keystrokes in search field, only one query should execute after the debounce delay.

**Validates: Requirements 4.1, 4.5**

**Test Strategy:**
- Simulate rapid typing (10 characters in 100ms)
- Assert only 1 query executes after 300ms delay
- Verify previous queries are cancelled

### Property 4: Connection Pool Reuses Connections

*For any* sequence of database operations, connections should be reused from pool rather than creating new ones.

**Validates: Requirements 10.1, 10.2, 10.5**

**Test Strategy:**
- Monitor connection creation count
- Execute 100 operations
- Assert connection creation count < 10
- Verify connection reuse rate > 90%

### Property 5: Queries Use Indices

*For any* search by patrimonio number or sala, query execution plan should use index scan, not sequential scan.

**Validates: Requirements 6.1, 6.4**

**Test Strategy:**
- Execute EXPLAIN ANALYZE on queries
- Assert "Index Scan" appears in plan
- Assert "Seq Scan" does not appear
- Verify query time < 100ms

### Property 6: Table Pagination Limits Memory

*For any* table load operation, maximum number of rows loaded should not exceed configured limit (100 rows).

**Validates: Requirements 2.1, 7.1, 7.5**

**Test Strategy:**
- Load table with 10,000 available rows
- Assert DefaultTableModel contains exactly 100 rows
- Verify memory usage < 50MB for table data

### Property 7: SwingWorker Completes on EDT

*For any* SwingWorker operation, the done() method should execute on EDT, ensuring safe UI updates.

**Validates: Requirements 8.3, 8.4**

**Test Strategy:**
- Execute SwingWorker
- In done(), assert SwingUtilities.isEventDispatchThread() == true
- Verify UI updates occur without ConcurrentModificationException

### Property 8: Feedback Appears Immediately

*For any* user action (button click, search), visual feedback should appear within 100ms.

**Validates: Requirements 9.1, 9.2, 9.5**

**Test Strategy:**
- Measure time from button click to UI change
- Assert feedback time < 100ms
- Verify button disabled immediately
- Verify cursor changes to WAIT_CURSOR

### Property 9: Cache Invalidation on Update

*For any* data modification (insert, update, delete), related cache entries should be invalidated immediately.

**Validates: Requirements 3.2, 3.5**

**Test Strategy:**
- Cache sala data
- Update sala in database
- Assert cache returns fresh data on next access
- Verify stale data is never returned

### Property 10: Slow Operations Are Logged

*For any* operation exceeding 500ms, a performance log entry should be created with timing details.

**Validates: Requirements 12.1, 12.2, 12.4**

**Test Strategy:**
- Execute operation taking 600ms
- Assert log contains entry with operation name and duration
- Verify log includes timestamp and thread info

## Error Handling

### 1. Database Connection Failures

```java
try {
    // Operação de banco
} catch (SQLException e) {
    logger.error("Erro ao acessar banco de dados", e);
    
    // Tentar usar cache se disponível
    Optional<T> cachedData = cacheManager.get(key);
    if (cachedData.isPresent()) {
        logger.info("Usando dados do cache devido a falha de conexão");
        return cachedData.get();
    }
    
    // Mostrar erro ao usuário
    SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(this,
            "Erro ao conectar ao banco de dados. Verifique a conexão.",
            "Erro de Conexão",
            JOptionPane.ERROR_MESSAGE);
    });
}
```

### 2. SwingWorker Exceptions

```java
@Override
protected void done() {
    try {
        T result = get();
        onSuccess(result);
    } catch (InterruptedException e) {
        logger.warn("Operação foi cancelada", e);
        Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        logger.error("Erro durante execução", cause);
        onError(cause);
    } finally {
        hideLoading();
    }
}
```

### 3. Cache Expiration

```java
public Optional<T> get(String key) {
    CachedData<T> cached = cache.get(key);
    
    if (cached == null) {
        return Optional.empty();
    }
    
    if (cached.isExpired()) {
        cache.remove(key);
        logger.debug("Cache expirado para chave: {}", key);
        return Optional.empty();
    }
    
    return Optional.of(cached.getData());
}
```

### 4. EDT Violations

```java
// Verificar se está na EDT antes de atualizar UI
private void updateUI(Object data) {
    if (!SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater(() -> updateUI(data));
        return;
    }
    
    // Atualizar UI com segurança
    modeloTabela.setData(data);
}
```

## Testing Strategy

### Unit Tests

#### 1. CacheManager Tests

```java
@Test
public void testCacheHitReturnsCachedData() {
    CacheManager cache = CacheManager.getInstance();
    Sala sala = new Sala(1, "Sala 101");
    
    cache.putSala(sala);
    Optional<Sala> result = cache.getSala(1);
    
    assertTrue(result.isPresent());
    assertEquals("Sala 101", result.get().getNome());
}

@Test
public void testCacheExpiresAfterTTL() throws InterruptedException {
    CacheManager cache = CacheManager.getInstance();
    cache.setTTL(100); // 100ms para teste
    
    Sala sala = new Sala(1, "Sala 101");
    cache.putSala(sala);
    
    Thread.sleep(150);
    
    Optional<Sala> result = cache.getSala(1);
    assertFalse(result.isPresent());
}
```

#### 2. DebouncedSearchField Tests

```java
@Test
public void testDebounceDelaysExecution() throws InterruptedException {
    AtomicInteger searchCount = new AtomicInteger(0);
    DebouncedSearchField field = new DebouncedSearchField(300);
    
    field.setSearchListener(text -> searchCount.incrementAndGet());
    
    // Simular digitação rápida
    field.setText("a");
    field.setText("ab");
    field.setText("abc");
    
    // Aguardar menos que o delay
    Thread.sleep(200);
    assertEquals(0, searchCount.get());
    
    // Aguardar completar delay
    Thread.sleep(150);
    assertEquals(1, searchCount.get());
}
```

#### 3. OptimizedSwingWorker Tests

```java
@Test
public void testSwingWorkerLogsPerformance() {
    TestAppender appender = new TestAppender();
    Logger.getRootLogger().addAppender(appender);
    
    OptimizedSwingWorker<String, Void> worker = new OptimizedSwingWorker<>("test") {
        @Override
        protected String performOperation() {
            return "result";
        }
        
        @Override
        protected void onSuccess(String result) {}
        
        @Override
        protected void onError(Exception e) {}
    };
    
    worker.execute();
    worker.get(); // Aguardar conclusão
    
    assertTrue(appender.getMessages().stream()
        .anyMatch(msg -> msg.contains("test") && msg.contains("ms")));
}
```

### Integration Tests

#### 1. Database Query Performance

```java
@Test
public void testBuscarPorNumeroUsesIndex() {
    PatrimonioDAO dao = new PatrimonioDAO(dataSource);
    
    long startTime = System.currentTimeMillis();
    Optional<Patrimonio> result = dao.buscarPorNumero("12345");
    long duration = System.currentTimeMillis() - startTime;
    
    assertTrue(result.isPresent());
    assertTrue(duration < 100, "Query deve usar índice e completar em < 100ms");
}
```

#### 2. Connection Pool Reuse

```java
@Test
public void testConnectionPoolReusesConnections() {
    HikariDataSource ds = (HikariDataSource) dataSource;
    int initialConnections = ds.getHikariPoolMXBean().getActiveConnections();
    
    // Executar 50 operações
    for (int i = 0; i < 50; i++) {
        dao.buscarPorNumero("" + i);
    }
    
    int finalConnections = ds.getHikariPoolMXBean().getTotalConnections();
    
    assertTrue(finalConnections - initialConnections < 5, 
        "Pool deve reutilizar conexões");
}
```

### Property-Based Tests

#### 1. Cache Consistency

```java
@Property
public void cacheAlwaysReturnsValidData(@ForAll Sala sala) {
    CacheManager cache = CacheManager.getInstance();
    cache.putSala(sala);
    
    Optional<Sala> result = cache.getSala(sala.getId());
    
    assertTrue(result.isPresent());
    assertEquals(sala.getId(), result.get().getId());
    assertEquals(sala.getNome(), result.get().getNome());
}
```

#### 2. Debounce Prevents Excessive Calls

```java
@Property
public void debounceExecutesOnlyOnce(
    @ForAll @IntRange(min = 5, max = 20) int keystrokes) {
    
    AtomicInteger callCount = new AtomicInteger(0);
    DebouncedSearchField field = new DebouncedSearchField(300);
    field.setSearchListener(text -> callCount.incrementAndGet());
    
    // Simular múltiplas digitações
    for (int i = 0; i < keystrokes; i++) {
        field.setText("text" + i);
    }
    
    Thread.sleep(400);
    
    assertEquals(1, callCount.get());
}
```

## Performance Benchmarks

### Target Metrics

| Operação | Antes | Meta | Como Medir |
|----------|-------|------|------------|
| Abrir ColetaFrame_v2 | 2-3s | < 500ms | System.currentTimeMillis() |
| Buscar patrimônio por número | 500ms | < 100ms | Query execution time |
| Carregar tabela de coletas | 3-5s | < 500ms | SwingWorker duration |
| Selecionar sala no combo | 1-2s | < 200ms | ActionListener timing |
| Registrar coleta | 800ms | < 300ms | INSERT + UI update time |
| Busca com debounce | N/A | 300ms delay | Timer measurement |
| Cache hit | N/A | < 10ms | Map.get() timing |
| Connection pool get | N/A | < 50ms | DataSource.getConnection() |

### Monitoring

```java
public class PerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final long SLOW_THRESHOLD_MS = 500;
    
    public static <T> T measureOperation(String operationName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        try {
            T result = operation.get();
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > SLOW_THRESHOLD_MS) {
                logger.warn("Operação lenta detectada: {} levou {}ms", 
                    operationName, duration);
            } else {
                logger.debug("Operação {}: {}ms", operationName, duration);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Erro na operação {} após {}ms", operationName, duration, e);
            throw e;
        }
    }
}
```

## Implementation Notes

### 1. HikariCP Configuration

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost:5432/sispatrimonio");
config.setUsername("inventario");
config.setPassword("senha");
config.setMaximumPoolSize(10);
config.setMinimumIdle(5);
config.setConnectionTimeout(5000);
config.setIdleTimeout(300000);
config.setMaxLifetime(600000);
config.setLeakDetectionThreshold(60000);

HikariDataSource dataSource = new HikariDataSource(config);
```

### 2. Database Indices

```sql
-- Índice para busca por número de patrimônio
CREATE INDEX IF NOT EXISTS idx_patrimonio_numero 
ON patrimonio(numero_patrimonio);

-- Índice para busca por sala
CREATE INDEX IF NOT EXISTS idx_patrimonio_sala 
ON patrimonio(id_sala) WHERE ativo = true;

-- Índice para busca por descrição (full-text)
CREATE INDEX IF NOT EXISTS idx_patrimonio_descricao 
ON patrimonio USING gin(to_tsvector('portuguese', descricao));

-- Índice para coletas por inventário
CREATE INDEX IF NOT EXISTS idx_coleta_inventario 
ON coleta(id_inventario, data_coleta DESC);

-- Atualizar estatísticas
ANALYZE patrimonio;
ANALYZE coleta;
```

### 3. SwingWorker Template

```java
private void executarOperacaoAsync(String operationName, 
                                   Supplier<T> operation,
                                   Consumer<T> onSuccess) {
    new OptimizedSwingWorker<T, Void>(operationName) {
        @Override
        protected T performOperation() throws Exception {
            return operation.get();
        }
        
        @Override
        protected void onSuccess(T result) {
            onSuccess.accept(result);
        }
        
        @Override
        protected void onError(Exception e) {
            showError("Erro: " + e.getMessage());
        }
    }.execute();
}
```

### 4. Cache Warming on Startup

```java
private void warmupCache() {
    executorService.submit(() -> {
        try {
            // Pré-carregar inventário ativo
            Inventario inventario = inventarioDAO.buscarAtivo();
            cacheManager.putInventarioAtivo(inventario);
            
            // Pré-carregar salas
            List<Sala> salas = salaDAO.listarTodas();
            salas.forEach(cacheManager::putSala);
            
            logger.info("Cache aquecido com {} salas", salas.size());
        } catch (Exception e) {
            logger.error("Erro ao aquecer cache", e);
        }
    });
}
```
