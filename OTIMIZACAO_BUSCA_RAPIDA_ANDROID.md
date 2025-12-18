# Otimização de Busca Rápida - App Android

## 📊 Análise da Implementação Atual

### Estratégia Existente

A busca rápida no app Android segue este fluxo:

```
QuickSearchActivity (UI)
    ↓ (input com debounce 300ms)
QuickSearchViewModel
    ↓ (chama Use Case)
BuscarPatrimoniosUseCase
    ↓ (tenta servidor primeiro)
PatrimonioApi.buscarPorQuery()
    ↓ (se falhar, fallback local)
PatrimonioRepositoryImpl.buscarPorQuery()
    ↓ (executa query SQL)
PatrimonioDao.buscarPorQuery()
    ↓ (SQLite)
SELECT * FROM patrimonio 
WHERE numeroPatrimonio LIKE '%' || :query || '%' 
   OR descricao LIKE '%' || :query || '%'
   OR nomeSala LIKE '%' || :query || '%'
ORDER BY numeroPatrimonio ASC
LIMIT 100
```

### Características Atuais

✅ **Bom:**
- Debounce de 300ms reduz requisições
- Fallback automático para local se servidor offline
- Limite de 100 resultados
- Busca em 3 campos (número, descrição, sala)
- Filtros por status (coletados, pendentes, divergências)

⚠️ **Problemas:**
- Query usa `LIKE '%query%'` (full table scan)
- Sem índices específicos para busca
- Sem cache em memória
- Sem paginação (carrega tudo de uma vez)
- Sem busca por voz otimizada
- Sem sugestões/autocomplete

---

## 🚀 Estratégias de Otimização

### 1. **Índices de Busca Full-Text (FTS5)**

**Problema:** `LIKE '%query%'` faz full table scan  
**Solução:** Usar SQLite FTS5 (Full-Text Search)

#### Implementação:

```sql
-- Criar tabela virtual FTS5
CREATE VIRTUAL TABLE patrimonio_fts USING fts5(
    id UNINDEXED,
    numeroPatrimonio,
    descricao,
    nomeSala,
    content=patrimonio,
    content_rowid=id
);

-- Criar trigger para manter FTS sincronizado
CREATE TRIGGER patrimonio_ai AFTER INSERT ON patrimonio BEGIN
  INSERT INTO patrimonio_fts(rowid, numeroPatrimonio, descricao, nomeSala)
  VALUES (new.id, new.numeroPatrimonio, new.descricao, new.nomeSala);
END;

CREATE TRIGGER patrimonio_ad AFTER DELETE ON patrimonio BEGIN
  INSERT INTO patrimonio_fts(patrimonio_fts, rowid, numeroPatrimonio, descricao, nomeSala)
  VALUES('delete', old.id, old.numeroPatrimonio, old.descricao, old.nomeSala);
END;

CREATE TRIGGER patrimonio_au AFTER UPDATE ON patrimonio BEGIN
  INSERT INTO patrimonio_fts(patrimonio_fts, rowid, numeroPatrimonio, descricao, nomeSala)
  VALUES('delete', old.id, old.numeroPatrimonio, old.descricao, old.nomeSala);
  INSERT INTO patrimonio_fts(rowid, numeroPatrimonio, descricao, nomeSala)
  VALUES (new.id, new.numeroPatrimonio, new.descricao, new.nomeSala);
END;
```

#### Query otimizada:

```sql
-- Antes (LIKE - lento)
SELECT * FROM patrimonio 
WHERE numeroPatrimonio LIKE '%' || :query || '%' 
   OR descricao LIKE '%' || :query || '%'
   OR nomeSala LIKE '%' || :query || '%'
LIMIT 100

-- Depois (FTS5 - rápido)
SELECT p.* FROM patrimonio p
INNER JOIN patrimonio_fts fts ON p.id = fts.rowid
WHERE patrimonio_fts MATCH :query
LIMIT 100
```

**Ganho esperado:** 10-50x mais rápido em bases grandes

---

### 2. **Índices Tradicionais**

Para queries que não usam FTS, adicionar índices:

```sql
-- Índices para busca rápida
CREATE INDEX idx_patrimonio_numero ON patrimonio(numeroPatrimonio);
CREATE INDEX idx_patrimonio_descricao ON patrimonio(descricao);
CREATE INDEX idx_patrimonio_sala ON patrimonio(nomeSala);

-- Índices compostos para filtros comuns
CREATE INDEX idx_patrimonio_coletado_numero ON patrimonio(coletado, numeroPatrimonio);
CREATE INDEX idx_patrimonio_coletado_descricao ON patrimonio(coletado, descricao);
```

**Ganho esperado:** 2-5x mais rápido

---

### 3. **Cache em Memória (LRU)**

Cachear resultados recentes para evitar queries repetidas:

```kotlin
// data/cache/SearchCache.kt
class SearchCache(private val maxSize: Int = 50) {
    private val cache = LinkedHashMap<String, List<PatrimonioComColeta>>(
        maxSize, 0.75f, true // LRU order
    ) {
        override fun removeEldestEntry(eldest: Map.Entry<String, List<PatrimonioComColeta>>?): Boolean {
            return size > maxSize
        }
    }
    
    fun get(key: String): List<PatrimonioComColeta>? = cache[key]
    
    fun put(key: String, value: List<PatrimonioComColeta>) {
        cache[key] = value
    }
    
    fun clear() = cache.clear()
    
    fun size() = cache.size
}
```

#### Integração no Use Case:

```kotlin
class BuscarPatrimoniosUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepositoryImpl,
    private val searchCache: SearchCache  // ← Injetar cache
) {
    suspend operator fun invoke(
        query: String,
        filtro: SearchFilter = SearchFilter.ALL
    ): Result<List<PatrimonioComColeta>> {
        // Chave do cache: query + filtro
        val cacheKey = "$query:$filtro"
        
        // Verificar cache primeiro
        searchCache.get(cacheKey)?.let {
            Log.d(TAG, "✓ Cache hit: $cacheKey (${it.size} resultados)")
            return Result.success(it)
        }
        
        // Se não estiver em cache, buscar normalmente
        val result = buscarDoServidor(query, filtro, inventarioId)
            .recoverCatching { buscarLocal(query, filtro, inventarioId) }
        
        // Cachear resultado se bem-sucedido
        result.onSuccess { resultados ->
            searchCache.put(cacheKey, resultados)
            Log.d(TAG, "✓ Resultado cacheado: $cacheKey")
        }
        
        return result
    }
}
```

**Ganho esperado:** 100-1000x mais rápido (cache hit)

---

### 4. **Paginação com Paging 3**

Carregar resultados em lotes ao invés de tudo de uma vez:

```kotlin
// data/paging/PatrimonioSearchPagingSource.kt
class PatrimonioSearchPagingSource(
    private val query: String,
    private val filtro: SearchFilter,
    private val patrimonioDao: PatrimonioDao
) : PagingSource<Int, PatrimonioComColeta>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatrimonioComColeta> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            val offset = page * pageSize
            
            // Query com LIMIT e OFFSET
            val entities = when (filtro) {
                SearchFilter.ALL -> patrimonioDao.buscarPorQueryPaginado(query, pageSize, offset)
                SearchFilter.COLETADOS -> patrimonioDao.buscarColetadosPorQueryPaginado(query, pageSize, offset)
                SearchFilter.PENDENTES -> patrimonioDao.buscarPendentesPorQueryPaginado(query, pageSize, offset)
                SearchFilter.DIVERGENCIAS -> patrimonioDao.buscarDivergenciasPorQueryPaginado(query, pageSize, offset)
            }
            
            val patrimonios = entities.map { it.toPatrimonioComColeta() }
            
            LoadResult.Page(
                data = patrimonios,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (patrimonios.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, PatrimonioComColeta>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
```

#### Queries paginadas no DAO:

```kotlin
@Query("""
    SELECT * FROM patrimonio 
    WHERE numeroPatrimonio LIKE '%' || :query || '%' 
       OR descricao LIKE '%' || :query || '%'
       OR nomeSala LIKE '%' || :query || '%'
    ORDER BY numeroPatrimonio ASC
    LIMIT :limit OFFSET :offset
""")
suspend fun buscarPorQueryPaginado(
    query: String,
    limit: Int,
    offset: Int
): List<PatrimonioEntity>
```

**Ganho esperado:** Reduz memória em 50-80%, UI mais responsiva

---

### 5. **Busca por Voz Otimizada**

Usar reconhecimento de voz com cache de resultados:

```kotlin
// presentation/search/VoiceSearchOptimized.kt
class VoiceSearchOptimized @Inject constructor(
    private val speechRecognizer: SpeechRecognizer,
    private val searchCache: SearchCache
) {
    
    fun startVoiceSearch(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        }
        
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val topResult = matches[0]
                    
                    // Verificar cache antes de buscar
                    val cacheKey = "$topResult:ALL"
                    if (searchCache.get(cacheKey) != null) {
                        Log.d(TAG, "✓ Resultado de voz encontrado em cache")
                    }
                    
                    onResult(topResult)
                }
            }
            
            override fun onError(error: Int) {
                onError("Erro no reconhecimento: $error")
            }
            
            // ... outros callbacks
        })
        
        speechRecognizer.startListening(intent)
    }
}
```

**Ganho esperado:** Busca por voz 2-3x mais rápida com cache

---

### 6. **Autocomplete com Sugestões**

Mostrar sugestões enquanto o usuário digita:

```kotlin
// domain/usecase/BuscarSugestoesUseCase.kt
class BuscarSugestoesUseCase @Inject constructor(
    private val patrimonioDao: PatrimonioDao
) {
    suspend operator fun invoke(query: String, limit: Int = 10): Result<List<String>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }
            
            // Buscar sugestões únicas (números e descrições)
            val sugestoes = patrimonioDao.buscarSugestoes(query, limit)
            Result.success(sugestoes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// DAO
@Query("""
    SELECT DISTINCT numeroPatrimonio as sugestao FROM patrimonio
    WHERE numeroPatrimonio LIKE :query || '%'
    LIMIT :limit
    UNION
    SELECT DISTINCT descricao FROM patrimonio
    WHERE descricao LIKE :query || '%'
    LIMIT :limit
""")
suspend fun buscarSugestoes(query: String, limit: Int): List<String>
```

#### UI com sugestões:

```kotlin
// presentation/search/QuickSearchActivityOptimized.kt
private fun setupAutoComplete() {
    binding.etSearch.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val query = s.toString()
            
            if (query.length >= 2) {
                viewModel.buscarSugestoes(query)
            }
        }
    })
    
    lifecycleScope.launch {
        viewModel.sugestoes.collect { sugestoes ->
            mostrarSugestoes(sugestoes)
        }
    }
}

private fun mostrarSugestoes(sugestoes: List<String>) {
    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sugestoes)
    binding.etSearch.setAdapter(adapter)
    adapter.notifyDataSetChanged()
}
```

**Ganho esperado:** UX melhorada, usuário encontra resultado mais rápido

---

### 7. **Compressão de Dados**

Comprimir dados antes de enviar para o servidor:

```kotlin
// util/CompressionUtils.kt
object CompressionUtils {
    
    fun compressString(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(input.toByteArray())
        }
        return bos.toByteArray()
    }
    
    fun decompressString(input: ByteArray): String {
        val bis = ByteArrayInputStream(input)
        val gzip = GZIPInputStream(bis)
        return gzip.bufferedReader().use { it.readText() }
    }
}

// Usar no Retrofit
@GET("api/mobile/patrimonio/buscar")
suspend fun buscarPorQuery(
    @Query("query") query: String,
    @Query("filtro") filtro: String = "ALL",
    @Header("Accept-Encoding") encoding: String = "gzip"
): ApiResponse<List<MobilePatrimonioDTO>>
```

**Ganho esperado:** Reduz tráfego de rede em 70-80%

---

## 📋 Implementação Recomendada (Prioridade)

### Fase 1 (Imediato - Alto Impacto)
1. ✅ **Índices tradicionais** - Fácil, ganho 2-5x
2. ✅ **Cache em memória (LRU)** - Fácil, ganho 100-1000x (cache hit)
3. ✅ **Debounce aumentado** - Já existe (300ms), considerar 500ms

### Fase 2 (Curto Prazo - Médio Impacto)
4. **FTS5** - Médio, ganho 10-50x
5. **Paginação** - Médio, reduz memória 50-80%
6. **Autocomplete** - Médio, melhora UX

### Fase 3 (Longo Prazo - Otimizações)
7. **Busca por voz otimizada** - Complexo, ganho 2-3x
8. **Compressão de dados** - Complexo, reduz tráfego 70-80%

---

## 🔧 Implementação Passo a Passo

### Passo 1: Adicionar Índices (5 minutos)

```kotlin
// data/local/database/AppDatabase.kt
@Database(
    entities = [PatrimonioEntity::class, ...],
    version = 2  // ← Incrementar versão
)
abstract class AppDatabase : RoomDatabase() {
    
    companion object {
        // Migrations
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar índices
                database.execSQL("CREATE INDEX idx_patrimonio_numero ON patrimonio(numeroPatrimonio)")
                database.execSQL("CREATE INDEX idx_patrimonio_descricao ON patrimonio(descricao)")
                database.execSQL("CREATE INDEX idx_patrimonio_sala ON patrimonio(nomeSala)")
                database.execSQL("CREATE INDEX idx_patrimonio_coletado_numero ON patrimonio(coletado, numeroPatrimonio)")
            }
        }
    }
}
```

### Passo 2: Implementar Cache (10 minutos)

```kotlin
// di/CacheModule.kt
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    
    @Provides
    @Singleton
    fun provideSearchCache(): SearchCache {
        return SearchCache(maxSize = 50)
    }
}
```

### Passo 3: Integrar Cache no Use Case (5 minutos)

Adicionar `searchCache` ao `BuscarPatrimoniosUseCase` conforme exemplo acima.

---

## 📊 Comparação de Performance

| Estratégia | Tempo (ms) | Ganho | Complexidade |
|-----------|-----------|-------|--------------|
| Sem otimização | 500-2000 | 1x | - |
| + Índices | 100-400 | 5-10x | Baixa |
| + Cache | 1-10 | 100-1000x | Baixa |
| + FTS5 | 50-200 | 10-50x | Média |
| + Paginação | 50-100 | 5-10x | Média |
| + Autocomplete | 10-50 | 10-100x | Média |
| Tudo junto | 1-50 | 100-1000x | Alta |

---

## ✅ Checklist de Implementação

- [ ] Adicionar índices ao banco
- [ ] Implementar SearchCache
- [ ] Integrar cache no BuscarPatrimoniosUseCase
- [ ] Testar performance com 10k+ patrimônios
- [ ] Implementar FTS5 (opcional)
- [ ] Adicionar paginação (opcional)
- [ ] Implementar autocomplete (opcional)
- [ ] Documentar mudanças

---

## 🧪 Testes de Performance

```kotlin
// Teste de busca
@Test
fun testBuscaPerformance() = runTest {
    val startTime = System.currentTimeMillis()
    
    val result = buscarPatrimoniosUseCase("patrimonio", SearchFilter.ALL)
    
    val tempoMs = System.currentTimeMillis() - startTime
    
    assertTrue(tempoMs < 500, "Busca demorou ${tempoMs}ms (esperado < 500ms)")
    assertTrue(result.isSuccess)
}

// Teste de cache
@Test
fun testCacheHit() = runTest {
    val query = "patrimonio"
    
    // Primeira busca
    val start1 = System.currentTimeMillis()
    buscarPatrimoniosUseCase(query, SearchFilter.ALL)
    val tempo1 = System.currentTimeMillis() - start1
    
    // Segunda busca (deve estar em cache)
    val start2 = System.currentTimeMillis()
    buscarPatrimoniosUseCase(query, SearchFilter.ALL)
    val tempo2 = System.currentTimeMillis() - start2
    
    assertTrue(tempo2 < tempo1 / 10, "Cache não acelerou suficientemente")
}
```

---

## 📝 Conclusão

A busca rápida atual é funcional, mas pode ser **10-1000x mais rápida** com as otimizações propostas. As estratégias de **índices** e **cache** são as mais impactantes e fáceis de implementar.

**Recomendação:** Implementar Fase 1 (índices + cache) imediatamente para ganho rápido e visível.

