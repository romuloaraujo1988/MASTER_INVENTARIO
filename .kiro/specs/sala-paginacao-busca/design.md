# Design Document - Paginação e Busca de Salas

## Overview

Implementação de paginação com scroll infinito e busca em tempo real para a tela de seleção de salas, usando Paging 3, Room e Flow para otimizar performance e experiência do usuário.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────────────┐       ┌──────────────────────┐   │
│  │ SalaSelectionActivity│──────▶│ SalaViewModel        │   │
│  │ - SearchView         │       │ - searchQuery        │   │
│  │ - RecyclerView       │       │ - salaPagingData     │   │
│  │ - PagingDataAdapter  │       └──────────┬───────────┘   │
│  └──────────────────────┘                  │               │
└────────────────────────────────────────────┼───────────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────────────────┐       ┌──────────────────────┐   │
│  │ BuscarSalasUseCase   │       │ SalaRepository       │   │
│  │ - invoke(query)      │──────▶│ (interface)          │   │
│  └──────────────────────┘       └──────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  ┌──────────────────────┐       ┌──────────────────────┐   │
│  │ SalaRepositoryImpl   │       │ SalaPagingSource     │   │
│  │ - getPagingData()    │──────▶│ - load()             │   │
│  └──────────┬───────────┘       └──────────┬───────────┘   │
│             │                               │               │
│             ▼                               ▼               │
│  ┌──────────────────────┐       ┌──────────────────────┐   │
│  │ SalaDao (Room)       │       │ SalaApi (Retrofit)   │   │
│  │ - buscarPaginado()   │       │ - listarSalas()      │   │
│  │ - buscarPorNome()    │       │ - buscarPorNome()    │   │
│  └──────────────────────┘       └──────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. SalaPagingSource

**Responsabilidade**: Carregar salas do servidor com paginação

```kotlin
class SalaPagingSource(
    private val salaApi: SalaApi,
    private val query: String?
) : PagingSource<Int, Sala>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sala> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            
            val response = if (query.isNullOrBlank()) {
                salaApi.listarSalasPaginado(page, pageSize)
            } else {
                salaApi.buscarSalasPorNome(query, page, pageSize)
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                val salas = response.body()?.data ?: emptyList()
                val totalPages = response.body()?.totalPages ?: 0
                
                LoadResult.Page(
                    data = salas,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (page < totalPages - 1) page + 1 else null
                )
            } else {
                LoadResult.Error(Exception("Erro ao carregar salas"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Sala>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
```

### 2. SalaViewModel

**Responsabilidade**: Gerenciar estado da UI e paginação

```kotlin
@HiltViewModel
class SalaViewModel @Inject constructor(
    private val buscarSalasUseCase: BuscarSalasUseCase
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val salaPagingData: Flow<PagingData<Sala>> = searchQuery
        .debounce(300) // Debounce de 300ms
        .distinctUntilChanged()
        .flatMapLatest { query ->
            buscarSalasUseCase(query)
        }
        .cachedIn(viewModelScope)
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
    }
}
```

### 3. SalaPagingAdapter

**Responsabilidade**: Adapter para RecyclerView com paginação

```kotlin
class SalaPagingAdapter(
    private val onSalaClick: (Sala) -> Unit
) : PagingDataAdapter<Sala, SalaPagingAdapter.SalaViewHolder>(SALA_COMPARATOR) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaViewHolder {
        val binding = ItemSalaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalaViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SalaViewHolder, position: Int) {
        val sala = getItem(position)
        sala?.let { holder.bind(it) }
    }
    
    inner class SalaViewHolder(
        private val binding: ItemSalaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(sala: Sala) {
            binding.apply {
                tvSalaNome.text = sala.nome
                tvSalaDescricao.text = sala.descricao ?: "Sem descrição"
                
                root.setOnClickListener {
                    onSalaClick(sala)
                }
            }
        }
    }
    
    companion object {
        private val SALA_COMPARATOR = object : DiffUtil.ItemCallback<Sala>() {
            override fun areItemsTheSame(oldItem: Sala, newItem: Sala): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: Sala, newItem: Sala): Boolean {
                return oldItem == newItem
            }
        }
    }
}
```

### 4. SalaLoadStateAdapter

**Responsabilidade**: Exibir loading/erro no final da lista

```kotlin
class SalaLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<SalaLoadStateAdapter.LoadStateViewHolder>() {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    
    inner class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(loadState: LoadState) {
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                btnRetry.isVisible = loadState is LoadState.Error
                tvError.isVisible = loadState is LoadState.Error
                
                if (loadState is LoadState.Error) {
                    tvError.text = loadState.error.localizedMessage
                }
                
                btnRetry.setOnClickListener {
                    retry()
                }
            }
        }
    }
}
```

### 5. BuscarSalasUseCase

**Responsabilidade**: Lógica de negócio para buscar salas

```kotlin
class BuscarSalasUseCase @Inject constructor(
    private val salaRepository: SalaRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Sala>> {
        return salaRepository.getSalasPaginadas(query)
    }
}
```

### 6. SalaRepositoryImpl

**Responsabilidade**: Implementação do repositório com paginação

```kotlin
@Singleton
class SalaRepositoryImpl @Inject constructor(
    private val salaApi: SalaApi,
    private val salaDao: SalaDao
) : SalaRepository {
    
    override fun getSalasPaginadas(query: String): Flow<PagingData<Sala>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SalaPagingSource(salaApi, query.takeIf { it.isNotBlank() })
            }
        ).flow
    }
    
    override suspend fun buscarSalasLocal(query: String): List<Sala> {
        return if (query.isBlank()) {
            salaDao.buscarTodas()
        } else {
            salaDao.buscarPorNome("%$query%")
        }
    }
}
```

## Data Models

### Sala (Domain Model)

```kotlin
data class Sala(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val andar: String?,
    val bloco: String?,
    val ativa: Boolean
)
```

### SalaEntity (Room)

```kotlin
@Entity(tableName = "sala")
data class SalaEntity(
    @PrimaryKey
    val id: Int,
    val nome: String,
    val descricao: String?,
    val andar: String?,
    val bloco: String?,
    val ativa: Boolean,
    val sincronizado: Boolean = true,
    val dataAtualizacao: Long = System.currentTimeMillis()
)
```

### SalaDao

```kotlin
@Dao
interface SalaDao {
    
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC")
    suspend fun buscarTodas(): List<SalaEntity>
    
    @Query("SELECT * FROM sala WHERE ativa = 1 AND nome LIKE :query ORDER BY nome ASC")
    suspend fun buscarPorNome(query: String): List<SalaEntity>
    
    @Query("SELECT * FROM sala WHERE id = :id")
    suspend fun buscarPorId(id: Int): SalaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(sala: SalaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(salas: List<SalaEntity>)
    
    @Query("DELETE FROM sala")
    suspend fun limparTodas()
    
    @Query("SELECT COUNT(*) FROM sala WHERE ativa = 1")
    suspend fun contar(): Int
}
```

### API Response

```kotlin
data class PagedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int
)
```

### SalaApi

```kotlin
interface SalaApi {
    
    @GET("salas/paginado")
    suspend fun listarSalasPaginado(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PagedResponse<Sala>>
    
    @GET("salas/buscar")
    suspend fun buscarSalasPorNome(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PagedResponse<Sala>>
}
```

## UI Layout

### activity_sala_selection.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk-auto/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="Selecionar Sala" />
        
        <androidx.appcompat.widget.SearchView
            android:id="@+id/searchView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="8dp"
            app:queryHint="Buscar sala..."
            app:iconifiedByDefault="false" />
        
    </com.google.android.material.appbar.AppBarLayout>
    
    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
        
    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
    
    <TextView
        android:id="@+id/tvEmpty"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="Nenhuma sala encontrada"
        android:visibility="gone" />
    
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### item_sala.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk-auto/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="2dp"
    app:cardCornerRadius="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <TextView
            android:id="@+id/tvSalaNome"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="?attr/colorOnSurface" />
        
        <TextView
            android:id="@+id/tvSalaDescricao"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:textSize="14sp"
            android:textColor="?attr/colorOnSurfaceVariant" />
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

## Performance Optimizations

### 1. Debounce na Busca

```kotlin
searchQuery
    .debounce(300) // Aguarda 300ms após última digitação
    .distinctUntilChanged() // Ignora valores duplicados
    .flatMapLatest { query ->
        buscarSalasUseCase(query)
    }
```

### 2. Cache de Resultados

```kotlin
.cachedIn(viewModelScope) // Mantém dados em cache durante rotação
```

### 3. Prefetch

```kotlin
PagingConfig(
    pageSize = 20,
    prefetchDistance = 5, // Carrega próxima página quando faltam 5 itens
    enablePlaceholders = false
)
```

### 4. DiffUtil

```kotlin
// Atualiza apenas itens que mudaram
private val SALA_COMPARATOR = object : DiffUtil.ItemCallback<Sala>() {
    override fun areItemsTheSame(oldItem: Sala, newItem: Sala): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Sala, newItem: Sala): Boolean {
        return oldItem == newItem
    }
}
```

## Error Handling

```kotlin
sealed class SalaUiState {
    object Loading : SalaUiState()
    data class Success(val totalSalas: Int) : SalaUiState()
    data class Error(val message: String) : SalaUiState()
    object Empty : SalaUiState()
}
```

## Testing Strategy

### Unit Tests

```kotlin
@Test
fun `buscar salas com query vazia retorna todas salas`() = runTest {
    val useCase = BuscarSalasUseCase(repository)
    val result = useCase("").first()
    
    assertThat(result).isNotEmpty()
}

@Test
fun `buscar salas com query retorna salas filtradas`() = runTest {
    val useCase = BuscarSalasUseCase(repository)
    val result = useCase("Lab").first()
    
    assertThat(result).allMatch { it.nome.contains("Lab", ignoreCase = true) }
}
```

## Migration Path

1. ✅ Criar SalaPagingSource
2. ✅ Criar BuscarSalasUseCase
3. ✅ Atualizar SalaViewModel
4. ✅ Criar SalaPagingAdapter
5. ✅ Atualizar SalaSelectionActivity
6. ✅ Adicionar SearchView
7. ✅ Implementar LoadStateAdapter
8. ✅ Testar fluxo completo

## Dependencies

```gradle
// Paging 3
implementation "androidx.paging:paging-runtime-ktx:3.2.1"

// SwipeRefreshLayout
implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
```
