# Melhorias de Performance - Seleção de Salas

## 📊 Situação Atual

✅ **Já Implementado:**
- Paginação (20 itens por vez)
- Cache em memória (5 minutos)
- Scroll infinito
- Pull-to-refresh

## 🚀 Melhorias Propostas

### 1. Cache Persistente com Room Database

**Problema:** Cache em memória é perdido ao fechar o app.

**Solução:** Salvar salas no banco local Room.

```kotlin
// Criar DAO para cache de salas
@Dao
interface SalaCacheDao {
    @Query("SELECT * FROM sala_cache ORDER BY nome ASC LIMIT :limit OFFSET :offset")
    suspend fun getSalasPaginadas(limit: Int, offset: Int): List<SalaCacheEntity>
    
    @Query("SELECT * FROM sala_cache WHERE nome LIKE '%' || :query || '%' OR codigo LIKE '%' || :query || '%' ORDER BY nome ASC")
    suspend fun buscarSalas(query: String): List<SalaCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(salas: List<SalaCacheEntity>)
    
    @Query("DELETE FROM sala_cache")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM sala_cache")
    suspend fun count(): Int
}

@Entity(tableName = "sala_cache")
data class SalaCacheEntity(
    @PrimaryKey val id: Long,
    val nome: String,
    val codigo: String?,
    val descricao: String?,
    val setorId: Long,
    val ativo: Boolean,
    @ColumnInfo(name = "cache_timestamp") val cacheTimestamp: Long = System.currentTimeMillis()
)
```

### 2. Estratégia Offline-First

```kotlin
class SalaRepository(
    private val salaCacheDao: SalaCacheDao,
    private val apiService: ApiService
) {
    suspend fun getSalasPaginadas(page: Int, pageSize: Int): List<Sala> {
        // 1. Tentar buscar do cache local primeiro
        val cachedSalas = salaCacheDao.getSalasPaginadas(
            limit = pageSize,
            offset = page * pageSize
        )
        
        if (cachedSalas.isNotEmpty() && isCacheValid()) {
            return cachedSalas.map { it.toDomain() }
        }
        
        // 2. Se cache vazio ou expirado, buscar da API
        return try {
            val response = apiService.getSalasPaginadas(page, pageSize)
            if (response.isSuccessful && response.body()?.success == true) {
                val salas = response.body()!!.data!!.map { it.toDomain() }
                
                // 3. Salvar no cache local
                if (page == 0) {
                    salaCacheDao.clearAll() // Limpar cache antigo
                }
                salaCacheDao.insertAll(salas.map { it.toEntity() })
                
                salas
            } else {
                // Se API falhar, retornar cache mesmo expirado
                cachedSalas.map { it.toDomain() }
            }
        } catch (e: Exception) {
            // Em caso de erro de rede, usar cache
            cachedSalas.map { it.toDomain() }
        }
    }
    
    private fun isCacheValid(): Boolean {
        // Cache válido por 1 hora
        return true // Implementar lógica de validação
    }
}
```

### 3. Busca Local (Evita Chamadas à API)

```kotlin
// No ViewModel
private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

fun search(query: String) {
    _searchQuery.value = query
    
    viewModelScope.launch {
        if (query.isBlank()) {
            // Mostrar todas as salas do cache
            loadSalas()
        } else {
            // Buscar localmente no cache
            val filtered = salaCacheDao.buscarSalas(query)
            _uiState.value = _uiState.value.copy(
                salas = filtered.map { it.toDomain() },
                isLoading = false
            )
        }
    }
}
```

### 4. Pré-carregamento Inteligente

```kotlin
// Carregar próxima página em background quando chegar a 50% da lista
private fun setupSmartPreloading() {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            
            // Pré-carregar quando chegar a 50% da lista
            if (lastVisibleItem >= totalItemCount / 2 && !isPreloading) {
                viewModel.preloadNextPage()
            }
        }
    })
}
```

### 5. Índices Otimizados no Backend

```sql
-- Adicionar índices para melhorar performance das queries
CREATE INDEX IF NOT EXISTS idx_sala_nome ON TABELA_SALA(NOME);
CREATE INDEX IF NOT EXISTS idx_sala_codigo ON TABELA_SALA(CODIGO);
CREATE INDEX IF NOT EXISTS idx_sala_ativo ON TABELA_SALA(ATIVO);
CREATE INDEX IF NOT EXISTS idx_sala_setor ON TABELA_SALA(ID_SETOR);

-- Índice composto para paginação
CREATE INDEX IF NOT EXISTS idx_sala_ativo_nome ON TABELA_SALA(ATIVO, NOME);
```

### 6. Compressão de Resposta da API

```kotlin
// No backend (Spring Boot)
@Configuration
class CompressionConfig {
    @Bean
    fun compressionCustomizer(): WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        return WebServerFactoryCustomizer { factory ->
            val compression = Compression()
            compression.enabled = true
            compression.mimeTypes = arrayOf("application/json", "text/json")
            compression.minResponseSize = DataSize.ofKilobytes(1)
            factory.compression = compression
        }
    }
}
```

### 7. Lazy Loading de Imagens (se houver)

```kotlin
// Usar Coil ou Glide com cache
Coil.imageLoader(context).apply {
    memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25) // 25% da memória disponível
            .build()
    }
    diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(50 * 1024 * 1024) // 50 MB
            .build()
    }
}
```

### 8. RecyclerView Otimizado

```kotlin
// No Adapter
class SalaAdapter : ListAdapter<Sala, SalaViewHolder>(SalaDiffCallback()) {
    
    init {
        setHasStableIds(true) // Melhor performance
    }
    
    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaViewHolder {
        // Usar ViewBinding para melhor performance
        val binding = ItemSalaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalaViewHolder(binding)
    }
}

// DiffUtil para atualizações eficientes
class SalaDiffCallback : DiffUtil.ItemCallback<Sala>() {
    override fun areItemsTheSame(oldItem: Sala, newItem: Sala): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Sala, newItem: Sala): Boolean {
        return oldItem == newItem
    }
}
```

## 📈 Resultados Esperados

| Métrica | Antes | Depois | Melhoria |
|---------|-------|--------|----------|
| Tempo de carregamento inicial | 2-3s | 0.5-1s | **70% mais rápido** |
| Carregamento offline | ❌ Não funciona | ✅ Instantâneo | **100% offline** |
| Uso de dados | Alto | Baixo | **-60% dados** |
| Scroll suave | Às vezes trava | Sempre suave | **+100% fluidez** |
| Busca | Requer API | Local instantânea | **Instantânea** |

## 🎯 Prioridades de Implementação

### Alta Prioridade (Implementar Agora)
1. ✅ Cache persistente com Room
2. ✅ Busca local
3. ✅ Índices no backend

### Média Prioridade
4. Pré-carregamento inteligente
5. Compressão de resposta

### Baixa Prioridade
6. Lazy loading de imagens (se aplicável)
7. Otimizações avançadas de RecyclerView

## 🔧 Como Implementar

### Passo 1: Adicionar Entity e DAO

```bash
# Criar arquivos:
# - data/local/entity/SalaCacheEntity.kt
# - data/local/dao/SalaCacheDao.kt
```

### Passo 2: Atualizar Database

```kotlin
@Database(
    entities = [
        // ... outras entities
        SalaCacheEntity::class
    ],
    version = 2 // Incrementar versão
)
```

### Passo 3: Criar Repository

```kotlin
// data/repository/SalaRepository.kt
```

### Passo 4: Atualizar ViewModel

```kotlin
// Usar repository ao invés de chamar API diretamente
```

### Passo 5: Adicionar Índices no Backend

```sql
-- Executar script SQL no PostgreSQL
```

## 📝 Notas

- O cache em memória atual (5 min) pode ser mantido como camada adicional
- Room cache deve ter validade de 1 hora
- Implementar sincronização em background com WorkManager
- Adicionar indicador visual quando estiver usando cache offline
