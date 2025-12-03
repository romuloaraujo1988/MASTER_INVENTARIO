# Implementação de Paginação - App Android

## ✅ Arquivos Criados/Modificados

### DTOs
- `data/remote/dto/PagedResponse.kt` - DTO para resposta paginada
- `data/remote/dto/MobilePatrimonioDTO.kt` - DTO de patrimônio com conversão para domínio

### PagingSources
- `data/paging/PatrimonioPagingSource.kt` - Carrega patrimônios paginados
- `data/paging/PatrimonioPorSalaPagingSource.kt` - Carrega patrimônios por sala

### Use Cases
- `domain/usecase/BuscarPatrimoniosPaginadoUseCase.kt` - Use case com Paging 3

### Presentation
- `presentation/patrimonio/PatrimonioPagingAdapter.kt` - Adapter com PagingDataAdapter
- `presentation/patrimonio/PatrimonioLoadStateAdapter.kt` - Adapter para estados de loading
- `presentation/patrimonio/PatrimonioListViewModelPaging.kt` - ViewModel com Paging 3
- `presentation/patrimonio/PatrimonioListPagingFragment.kt` - Fragment de exemplo

### Layouts
- `res/layout/item_load_state.xml` - Layout para loading/erro no rodapé
- `res/layout/fragment_patrimonio_list_paging.xml` - Layout do fragment

### API
- `data/remote/api/PatrimonioApi.kt` - Novos endpoints paginados

---

## 🔄 Fluxo de Dados

```
┌─────────────────────────────────────────────────────────────┐
│                    Fragment/Activity                         │
│  - Observa patrimoniosPaging (Flow<PagingData>)             │
│  - Submete dados ao PagingDataAdapter                       │
└──────────────────────┬──────────────────────────────────────┘
                       │ collectLatest
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              PatrimonioListViewModelPaging                   │
│  - Configura Pager com PagingConfig                         │
│  - Aplica filtros (sala, coletado)                          │
│  - Carrega estatísticas separadamente                       │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│           BuscarPatrimoniosPaginadoUseCase                   │
│  - Cria Pager com PagingSource                              │
│  - Converte DTO → Domain                                    │
│  - Cache em viewModelScope                                  │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              PatrimonioPagingSource                          │
│  - Chama API paginada                                       │
│  - Retorna LoadResult.Page ou LoadResult.Error              │
│  - Gerencia prevKey/nextKey                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ chama
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    PatrimonioApi                             │
│  GET /api/mobile/patrimonio/paged?page=0&size=20            │
│  Retorna: PagedResponse<MobilePatrimonioDTO>                │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Endpoints Utilizados

### Patrimônios Paginados
```
GET /api/mobile/patrimonio/paged?page=0&size=20

Response:
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 11428,
    "totalPages": 572,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### Patrimônios por Sala
```
GET /api/mobile/patrimonio/sala/{salaId}?page=0&size=50&coletado=false

Response:
{
  "success": true,
  "data": [...]
}
```

### Estatísticas (Totais)
```
GET /api/mobile/dashboard/stats

Response:
{
  "success": true,
  "data": {
    "totalPatrimonios": 11428,
    "patrimoniosColetados": 524,
    "patrimoniosPendentes": 10904,
    "percentualConclusao": 4.58
  }
}
```

---

## 🎯 Como Usar

### No Fragment/Activity

```kotlin
@AndroidEntryPoint
class MinhaActivity : AppCompatActivity() {
    
    private val viewModel: PatrimonioListViewModelPaging by viewModels()
    private lateinit var adapter: PatrimonioPagingAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar adapter
        adapter = PatrimonioPagingAdapter { patrimonio ->
            // Click no item
        }
        
        recyclerView.adapter = adapter.withLoadStateFooter(
            footer = PatrimonioLoadStateAdapter { adapter.retry() }
        )
        
        // Observar dados paginados
        lifecycleScope.launch {
            viewModel.patrimoniosPaging.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        
        // Observar estatísticas (totais do servidor)
        lifecycleScope.launch {
            viewModel.estatisticas.collect { state ->
                when (state) {
                    is EstatisticasState.Success -> {
                        tvTotal.text = "Total: ${state.totalPatrimonios}"
                    }
                    // ...
                }
            }
        }
        
        // Carregar estatísticas
        viewModel.carregarEstatisticas()
    }
    
    // Aplicar filtros
    fun filtrarPorSala(salaId: Int) {
        viewModel.filtrarPorSala(salaId)
    }
    
    fun filtrarColetados() {
        viewModel.filtrarPorColetado(true)
    }
    
    fun filtrarPendentes() {
        viewModel.filtrarPorColetado(false)
    }
}
```

---

## ⚠️ Importante: Totais vs Lista

### ❌ ERRADO: Contar itens da lista
```kotlin
// NÃO FAÇA ISSO!
val total = adapter.itemCount // Conta apenas itens carregados
```

### ✅ CORRETO: Usar endpoint de estatísticas
```kotlin
// Use o endpoint de dashboard para totais
viewModel.carregarEstatisticas()

// Observe o estado
viewModel.estatisticas.collect { state ->
    if (state is EstatisticasState.Success) {
        val totalReal = state.totalPatrimonios // Total do servidor
    }
}
```

---

## 🔧 Configuração do Paging

```kotlin
PagingConfig(
    pageSize = 20,              // Itens por página
    enablePlaceholders = false, // Sem placeholders
    prefetchDistance = 10,      // Pré-carrega quando faltam 10 itens
    initialLoadSize = 40        // Carrega 2 páginas inicialmente
)
```

---

## 📱 Benefícios

1. **Economia de Memória**: Carrega apenas o necessário
2. **Economia de Dados**: Requisições menores
3. **UX Fluida**: Scroll infinito sem travamentos
4. **Retry Automático**: Tenta novamente em caso de erro
5. **Cache**: Páginas carregadas ficam em cache
6. **Filtros Reativos**: Mudança de filtro recarrega automaticamente

---

**Implementado em:** 02/12/2025
**Versão:** 1.0.0
