# 📋 Plano de Implementação - Busca Rápida

## 🎯 Objetivo

Implementar funcionalidade de Busca Rápida seguindo **Clean Architecture + MVVM** para buscar patrimônios de forma eficiente, com suporte a filtros e busca por voz.

---

## 📐 Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                QuickSearchActivity (View)                    │
│  - UI e interação do usuário                                 │
│  - Observa StateFlow do ViewModel                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ observa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│            QuickSearchViewModel (ViewModel)                  │
│  - Gerencia SearchState (sealed class)                       │
│  - Debounce de busca (300ms)                                 │
│  - Coordena Use Cases                                        │
└──────────────────────┬──────────────────────────────────────┘
                       │ chama
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Use Cases (Domain Layer)                        │
│  - BuscarPatrimoniosUseCase                                  │
│  - FiltrarPatrimoniosUseCase                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│         PatrimonioRepository (Data Layer)                    │
│  - buscarPorTexto()                                          │
│  - buscarComFiltros()                                        │
└──────────────────────┬──────────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              PatrimonioDao (Room)                            │
│  - Query com LIKE para busca                                 │
│  - Índices para performance                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Fase 1: Domain Layer (Regras de Negócio)

### 1.1 Models

**Arquivo:** `domain/model/SearchFilter.kt`
```kotlin
enum class SearchFilter {
    ALL,           // Todos
    COLETADOS,     // Apenas coletados
    PENDENTES,     // Apenas pendentes
    DIVERGENCIAS   // Com divergências
}
```

**Arquivo:** `domain/model/SearchCriteria.kt`
```kotlin
data class SearchCriteria(
    val query: String,
    val filter: SearchFilter = SearchFilter.ALL,
    val salaId: Int? = null,
    val responsavelId: Int? = null,
    val estadoConservacao: String? = null
)
```

### 1.2 Repository Interface

**Arquivo:** `domain/repository/SearchRepository.kt`
```kotlin
interface SearchRepository {
    suspend fun buscarPatrimonios(criteria: SearchCriteria): Result<List<Patrimonio>>
    fun observarResultados(criteria: SearchCriteria): Flow<List<Patrimonio>>
}
```

### 1.3 Use Cases

**Arquivo:** `domain/usecase/BuscarPatrimoniosUseCase.kt`
```kotlin
class BuscarPatrimoniosUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(criteria: SearchCriteria): Result<List<Patrimonio>> {
        // Validações
        if (criteria.query.length < 2) {
            return Result.failure(Exception("Digite pelo menos 2 caracteres"))
        }
        
        return repository.buscarPatrimonios(criteria)
    }
}
```

---

## 📦 Fase 2: Data Layer (Acesso a Dados)

### 2.1 DAO Queries

**Arquivo:** `data/local/dao/PatrimonioDao.kt` (adicionar)
```kotlin
@Query("""
    SELECT * FROM patrimonio 
    WHERE 
        (numeroPatrimonio LIKE '%' || :query || '%' OR
         descricao LIKE '%' || :query || '%' OR
         marca LIKE '%' || :query || '%' OR
         modelo LIKE '%' || :query || '%' OR
         nomeSala LIKE '%' || :query || '%' OR
         nomeResponsavel LIKE '%' || :query || '%')
    AND (:coletado IS NULL OR coletado = :coletado)
    AND (:salaId IS NULL OR idSala = :salaId)
    ORDER BY numeroPatrimonio ASC
    LIMIT :limit
""")
suspend fun buscarPatrimonios(
    query: String,
    coletado: Boolean? = null,
    salaId: Int? = null,
    limit: Int = 100
): List<PatrimonioEntity>
```

### 2.2 Repository Implementation

**Arquivo:** `data/repository/SearchRepositoryImpl.kt`
```kotlin
class SearchRepositoryImpl @Inject constructor(
    private val patrimonioDao: PatrimonioDao,
    private val mapper: PatrimonioMapper
) : SearchRepository {
    
    override suspend fun buscarPatrimonios(
        criteria: SearchCriteria
    ): Result<List<Patrimonio>> {
        return try {
            val coletado = when (criteria.filter) {
                SearchFilter.COLETADOS -> true
                SearchFilter.PENDENTES -> false
                else -> null
            }
            
            val entities = patrimonioDao.buscarPatrimonios(
                query = criteria.query,
                coletado = coletado,
                salaId = criteria.salaId
            )
            
            val patrimonios = entities.map { mapper.toDomain(it) }
            Result.success(patrimonios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 📦 Fase 3: Presentation Layer (UI)

### 3.1 UI State

**Arquivo:** `presentation/search/SearchState.kt`
```kotlin
sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(
        val patrimonios: List<Patrimonio>,
        val query: String,
        val filter: SearchFilter
    ) : SearchState()
    data class Empty(val query: String) : SearchState()
    data class Error(val message: String) : SearchState()
}
```

### 3.2 ViewModel

**Arquivo:** `presentation/search/QuickSearchViewModel.kt`
```kotlin
@HiltViewModel
class QuickSearchViewModel @Inject constructor(
    private val buscarPatrimoniosUseCase: BuscarPatrimoniosUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state.asStateFlow()
    
    private var searchJob: Job? = null
    
    fun buscar(query: String, filter: SearchFilter = SearchFilter.ALL) {
        searchJob?.cancel()
        
        if (query.length < 2) {
            _state.value = SearchState.Idle
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _state.value = SearchState.Loading
            
            val criteria = SearchCriteria(query, filter)
            buscarPatrimoniosUseCase(criteria).fold(
                onSuccess = { patrimonios ->
                    _state.value = if (patrimonios.isEmpty()) {
                        SearchState.Empty(query)
                    } else {
                        SearchState.Success(patrimonios, query, filter)
                    }
                },
                onFailure = { error ->
                    _state.value = SearchState.Error(error.message ?: "Erro")
                }
            )
        }
    }
}
```

### 3.3 Activity

**Arquivo:** `presentation/search/QuickSearchActivity.kt`
```kotlin
@AndroidEntryPoint
class QuickSearchActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuickSearchBinding
    private val viewModel: QuickSearchViewModel by viewModels()
    private lateinit var adapter: PatrimonioAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupSearch()
        observeViewModel()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SearchState.Idle -> showIdle()
                    is SearchState.Loading -> showLoading()
                    is SearchState.Success -> showResults(state.patrimonios)
                    is SearchState.Empty -> showEmpty(state.query)
                    is SearchState.Error -> showError(state.message)
                }
            }
        }
    }
}
```

---

## 📦 Fase 4: Integração com Busca por Voz

### 4.1 VoiceSearchManager (já existe)

Usar o `VoiceSearchManager` existente para capturar voz e passar para o ViewModel.

---

## 🗂️ Estrutura de Arquivos

```
InventarioMobile/app/src/main/java/com/inventario/mobile/
├── domain/
│   ├── model/
│   │   ├── SearchFilter.kt          # ✨ NOVO
│   │   └── SearchCriteria.kt        # ✨ NOVO
│   ├── repository/
│   │   └── SearchRepository.kt      # ✨ NOVO
│   └── usecase/
│       └── BuscarPatrimoniosUseCase.kt  # ✨ NOVO
│
├── data/
│   ├── local/
│   │   └── dao/
│   │       └── PatrimonioDao.kt     # ✏️ ADICIONAR queries
│   └── repository/
│       └── SearchRepositoryImpl.kt  # ✨ NOVO
│
└── presentation/
    └── search/
        ├── SearchState.kt           # ✨ NOVO
        ├── QuickSearchViewModel.kt  # ✨ NOVO
        └── QuickSearchActivity.kt   # ✏️ REFATORAR
```

---

## ⏱️ Cronograma

### Sprint 1 (2-3 horas)
- [x] Criar models (SearchFilter, SearchCriteria)
- [x] Criar SearchRepository interface
- [x] Criar BuscarPatrimoniosUseCase
- [x] Adicionar queries no PatrimonioDao

### Sprint 2 (2-3 horas)
- [ ] Implementar SearchRepositoryImpl
- [ ] Criar SearchState
- [ ] Criar QuickSearchViewModel
- [ ] Configurar Hilt modules

### Sprint 3 (3-4 horas)
- [ ] Refatorar QuickSearchActivity
- [ ] Criar layout XML
- [ ] Implementar adapter
- [ ] Integrar busca por voz

### Sprint 4 (1-2 horas)
- [ ] Testes
- [ ] Ajustes de UI/UX
- [ ] Documentação

**Total:** 8-12 horas

---

## 🧪 Checklist de Testes

- [ ] Busca por número de patrimônio
- [ ] Busca por descrição
- [ ] Busca por sala
- [ ] Filtro "Coletados"
- [ ] Filtro "Pendentes"
- [ ] Busca por voz
- [ ] Debounce funcionando (300ms)
- [ ] Performance com 10.000+ patrimônios
- [ ] Modo offline

---

## 📊 Métricas de Sucesso

- **Performance:** Busca < 200ms
- **UX:** Resultados em tempo real
- **Precisão:** 95%+ de relevância
- **Cobertura:** Busca em todos os campos

---

**Status:** 📋 Planejamento completo  
**Próximo passo:** Iniciar Sprint 1
