# Implementação de Paginação - Carregamento de Coletas

## 📋 Objetivo

Implementar paginação no carregamento de coletas para melhorar a performance do app Android, especialmente quando há muitas coletas registradas.

## 🚀 Melhorias Implementadas

### 1. Repository - Método Paginado

**Arquivo**: `InventarioRepository.kt`

```kotlin
suspend fun getColetasPaginadas(page: Int = 0, size: Int = 50): Result<PagedColetasResult> {
    // Busca coletas paginadas do servidor
    val response = apiService.getColetasPaginadas(page, size)
    
    // Mapeia resposta para modelo de domínio
    val pagedResult = PagedColetasResult(
        coletas = coletas,
        page = pagedData.page,
        size = pagedData.size,
        totalElements = pagedData.totalElements,
        totalPages = pagedData.totalPages,
        hasNext = !pagedData.last,
        hasPrevious = !pagedData.first
    )
    
    return Result.success(pagedResult)
}
```

**Características**:
- Tamanho de página padrão: 50 coletas
- Retorna informações de paginação (total, páginas, hasNext, etc.)
- Logs detalhados para debug

### 2. ViewModel - Carregamento Incremental

**Arquivo**: `CollectionViewViewModel.kt`

```kotlin
private var currentPage = 0
private val pageSize = 50
private var isLoadingMore = false
private var hasMorePages = true

fun loadColetas() {
    // Carrega primeira página
    currentPage = 0
    hasMorePages = true
    loadColetasPage(0)
}

fun loadNextPage() {
    // Carrega próxima página se disponível
    if (!isLoadingMore && hasMorePages) {
        isLoadingMore = true
        loadColetasPage(currentPage + 1)
    }
}

private suspend fun loadColetasPage(page: Int) {
    val result = repository.getColetasPaginadas(page, pageSize)
    
    // Combina com coletas existentes
    val todasColetas = if (page == 0) {
        pagedResult.coletas
    } else {
        _uiState.value.coletas + pagedResult.coletas
    }
    
    // Atualiza estado
    currentPage = pagedResult.page
    hasMorePages = pagedResult.hasNext
    isLoadingMore = false
}
```

**Características**:
- Carregamento incremental (acumula coletas)
- Controle de estado para evitar múltiplas requisições
- Extração de salas únicas de todas as coletas carregadas

### 3. Activity - Scroll Infinito

**Arquivo**: `CollectionViewActivity.kt`

```kotlin
recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        
        // Carregar mais quando estiver a 10 itens do fim
        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 10
            && firstVisibleItemPosition >= 0
            && totalItemCount > 0) {
            viewModel.loadNextPage()
        }
    }
})
```

**Características**:
- Carrega próxima página automaticamente
- Trigger: quando usuário está a 10 itens do fim
- Experiência fluida sem necessidade de botão "Carregar mais"

## 📊 Benefícios

### Performance
- ✅ Carregamento inicial mais rápido (50 coletas vs todas)
- ✅ Menor uso de memória
- ✅ Menor tráfego de rede inicial
- ✅ Resposta mais rápida da API

### UX
- ✅ App responde mais rápido
- ✅ Scroll infinito (sem interrupções)
- ✅ Spinner de salas carrega mais rápido
- ✅ Filtros aplicam mais rápido

### Escalabilidade
- ✅ Suporta milhares de coletas
- ✅ Performance consistente independente do volume
- ✅ Backend não sobrecarregado

## 🔄 Fluxo de Carregamento

### Primeira Carga
```
CollectionViewActivity.onCreate()
    ↓
CollectionViewViewModel.loadColetas()
    ↓ page = 0, size = 50
InventarioRepository.getColetasPaginadas()
    ↓
Backend: GET /api/mobile/coletas?page=0&size=50
    ↓ Retorna 50 coletas + metadados
ViewModel: atualiza uiState
    ↓ coletas = [50 itens]
    ↓ salas = [salas únicas extraídas]
Activity: renderiza lista
```

### Carregamento Incremental
```
Usuário rola até próximo do fim
    ↓
RecyclerView.OnScrollListener detecta
    ↓
CollectionViewViewModel.loadNextPage()
    ↓ page = 1, size = 50
InventarioRepository.getColetasPaginadas()
    ↓
Backend: GET /api/mobile/coletas?page=1&size=50
    ↓ Retorna mais 50 coletas
ViewModel: combina com existentes
    ↓ coletas = [100 itens]
    ↓ salas = [salas únicas atualizadas]
Activity: adiciona itens à lista
```

## 📝 Configurações

### Tamanho de Página
```kotlin
private val pageSize = 50  // Ajustável conforme necessidade
```

**Recomendações**:
- 20-50: Ideal para conexões lentas
- 50-100: Bom equilíbrio
- 100+: Para conexões rápidas e muitos dados

### Trigger de Carregamento
```kotlin
if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 10) {
    // Carregar mais
}
```

**Ajustes**:
- `-5`: Carrega mais cedo (mais agressivo)
- `-10`: Equilíbrio (padrão)
- `-20`: Carrega mais tarde (menos requisições)

## 🧪 Como Testar

1. **Abrir tela de Coletas**
2. **Verificar carregamento inicial** (deve ser rápido)
3. **Rolar a lista até o fim**
4. **Observar carregamento automático** de mais coletas
5. **Verificar spinner de salas** (deve ter todas as salas)
6. **Aplicar filtros** (deve funcionar com todas as coletas carregadas)

## 📊 Métricas Esperadas

### Antes (Sem Paginação)
- Tempo de carregamento inicial: 3-5s (1000+ coletas)
- Memória: ~50MB
- Tráfego inicial: ~2MB

### Depois (Com Paginação)
- Tempo de carregamento inicial: <1s (50 coletas)
- Memória: ~10MB inicial
- Tráfego inicial: ~200KB
- Carregamento incremental: ~200KB por página

## ⚠️ Observações

- O backend já suporta paginação no endpoint `/api/mobile/coletas`
- As salas são extraídas de todas as coletas carregadas (não apenas da página atual)
- Os filtros funcionam sobre todas as coletas carregadas até o momento
- Se o usuário aplicar filtro, pode ser necessário carregar mais páginas para ter dados suficientes

## ✅ Status

**CONCLUÍDO** - Paginação implementada e funcionando.

---

**Data**: 09/11/2025  
**Versão**: 2.0.0
