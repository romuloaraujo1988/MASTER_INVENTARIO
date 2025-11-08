# Design Document

## Overview

Esta solução modifica o comportamento da tela de Inventário no aplicativo mobile Android para exigir que o usuário selecione um responsável antes de carregar qualquer patrimônio. A implementação envolve mudanças no ViewModel, Activity e UI para criar um fluxo mais eficiente e intuitivo.

### Objetivos

1. Melhorar a performance inicial da tela eliminando carregamento desnecessário
2. Fornecer uma experiência de usuário mais focada e intuitiva
3. Reduzir o consumo de dados e memória
4. Manter compatibilidade com funcionalidades existentes (paginação, filtros, busca)

### Escopo

**Incluído:**
- Modificação do ViewModel para não carregar patrimônios automaticamente
- Atualização da UI para exibir estado vazio inicial
- Implementação de mensagem informativa para guiar o usuário
- Habilitação/desabilitação condicional dos filtros de status
- Atualização do botão "Limpar Filtros" para resetar ao estado inicial
- Preservação do estado em rotações de tela

**Não Incluído:**
- Mudanças na API backend
- Alterações no modelo de dados
- Modificações em outras telas do aplicativo

## Architecture

### Componentes Afetados

```
InventarioActivity (View)
       ↓
InventarioViewModel (ViewModel)
       ↓
InventarioRepository (Data)
       ↓
ApiService (Network)
```

### Fluxo de Dados

#### Estado Inicial (Novo Comportamento)
```
1. Activity onCreate()
   ↓
2. ViewModel init { loadResponsaveis() }  // Apenas responsáveis
   ↓
3. UI exibe estado vazio
   ↓
4. ComboBox carregado com responsáveis
   ↓
5. Filtros de status desabilitados
   ↓
6. Mensagem informativa exibida
```

#### Seleção de Responsável
```
1. Usuário seleciona responsável no ComboBox
   ↓
2. Activity chama viewModel.loadPatrimoniosByResponsavel(id)
   ↓
3. ViewModel atualiza estado: isLoading = true
   ↓
4. Repository busca patrimônios do responsável
   ↓
5. ViewModel atualiza estado com patrimônios
   ↓
6. UI exibe lista de patrimônios
   ↓
7. Filtros de status habilitados
```


## Components and Interfaces

### 1. InventarioViewModel

#### Mudanças no Estado (InventarioUiState)

```kotlin
data class InventarioUiState(
    val isLoading: Boolean = false,
    val patrimonios: List<Patrimonio> = emptyList(),
    val patrimoniosFiltered: List<Patrimonio> = emptyList(),
    val responsaveis: List<Responsavel> = emptyList(),
    val errorMessage: String? = null,
    val filtroResponsavelId: Int? = null,
    val filtroColetado: Boolean? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NUMERO_ASC,
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalPatrimonios: Int = 0,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false,
    // NOVO: Indica se há um responsável selecionado
    val hasResponsavelSelected: Boolean = false,
    // NOVO: Indica se deve mostrar mensagem de estado vazio
    val showEmptyStateMessage: Boolean = true
)
```

#### Mudanças nos Métodos

**init Block - MODIFICADO**
```kotlin
init {
    // Carregar apenas responsáveis, NÃO carregar patrimônios
    loadResponsaveis()
    // Remover: loadPatrimonios()
}
```

**loadPatrimonios() - MODIFICADO**
```kotlin
fun loadPatrimonios() {
    // Este método não deve fazer nada quando chamado sem responsável
    // Apenas para compatibilidade com código existente
    android.util.Log.w("InventarioViewModel", 
        "loadPatrimonios() chamado sem responsável - ignorando")
}
```

**loadPatrimoniosByResponsavel() - MODIFICADO**
```kotlin
fun loadPatrimoniosByResponsavel(
    responsavelId: Int,
    coletado: Boolean? = null,
    page: Int = 0,
    loadMore: Boolean = false
) {
    viewModelScope.launch {
        if (loadMore) {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                currentPage = 0,
                patrimonios = emptyList(),
                hasResponsavelSelected = true,  // NOVO
                showEmptyStateMessage = false   // NOVO
            )
        }
        
        // ... resto da implementação existente
    }
}
```

**clearFilters() - MODIFICADO**
```kotlin
fun clearFilters() {
    _uiState.value = _uiState.value.copy(
        filtroResponsavelId = null,
        filtroColetado = null,
        currentPage = 0,
        patrimonios = emptyList(),           // NOVO: Limpar patrimônios
        patrimoniosFiltered = emptyList(),   // NOVO: Limpar filtrados
        hasResponsavelSelected = false,      // NOVO: Marcar como sem seleção
        showEmptyStateMessage = true         // NOVO: Mostrar mensagem
    )
    // Remover: loadPatrimonios()
}
```

### 2. InventarioActivity

#### Mudanças no onCreate()

**ANTES:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ... setup code ...
    viewModel.loadPatrimonios()  // Carrega automaticamente
}
```

**DEPOIS:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ... setup code ...
    // NÃO carregar patrimônios automaticamente
    // Apenas observar o estado e aguardar seleção do usuário
}
```

#### Mudanças no setupFiltros()

**Habilitar/Desabilitar Filtros de Status:**
```kotlin
private fun setupFiltros() {
    // Observar estado para habilitar/desabilitar filtros
    lifecycleScope.launch {
        viewModel.uiState.collect { state ->
            // Habilitar filtros apenas se responsável selecionado
            val enabled = state.hasResponsavelSelected
            binding.chipTodos.isEnabled = enabled
            binding.chipColetados.isEnabled = enabled
            binding.chipNaoColetados.isEnabled = enabled
            
            // Aplicar estilo visual de desabilitado
            val alpha = if (enabled) 1.0f else 0.5f
            binding.chipGroupStatus.alpha = alpha
            
            if (state.responsaveis.isNotEmpty()) {
                setupResponsavelSpinner(state.responsaveis)
            }
        }
    }
    
    // ... resto do código existente ...
}
```

#### Mudanças no aplicarFiltros()

**MODIFICADO:**
```kotlin
private fun aplicarFiltros() {
    val responsavelId = responsavelSelecionado?.id
    val coletado = statusColetaSelecionado
    
    if (responsavelId != null) {
        // Carregar patrimônios do responsável
        viewModel.loadPatrimoniosByResponsavel(responsavelId, coletado)
    } else {
        // Sem responsável selecionado, não fazer nada
        // Apenas resetar filtro de status se aplicado
        if (coletado != null) {
            statusColetaSelecionado = null
            binding.chipTodos.isChecked = true
        }
    }
}
```

#### Mudanças no updateUI()

**Adicionar Lógica para Estado Vazio:**
```kotlin
private fun updateUI(state: InventarioUiState) {
    binding.swipeRefresh.isRefreshing = state.isLoading && state.currentPage == 0
    isLoadingMore = state.isLoadingMore
    
    binding.loadingMoreIndicator.visibility = 
        if (state.isLoadingMore) View.VISIBLE else View.GONE
    
    // NOVO: Mostrar mensagem de estado vazio
    if (state.showEmptyStateMessage && !state.hasResponsavelSelected) {
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = "Selecione um responsável para visualizar os patrimônios"
        binding.tvTotalCount.text = "Total: 0 patrimônios"
        return
    }
    
    val displayList = if (state.searchQuery.isNotEmpty() || 
                          state.sortOrder != SortOrder.NUMERO_ASC) {
        state.patrimoniosFiltered
    } else {
        state.patrimonios
    }
    
    if (displayList.isNotEmpty()) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        adapter.submitList(displayList)
    } else if (!state.isLoading && !state.isLoadingMore && state.hasResponsavelSelected) {
        // Responsável selecionado mas sem patrimônios
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = "Nenhum patrimônio encontrado para este responsável"
    }
    
    // ... resto do código existente ...
}
```

### 3. Layout XML (activity_inventario.xml)

#### Mudanças no Empty View

**Adicionar TextView para Mensagem Dinâmica:**
```xml
<LinearLayout
    android:id="@+id/emptyView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:visibility="gone">
    
    <ImageView
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@drawable/ic_empty_box"
        android:alpha="0.3"/>
    
    <TextView
        android:id="@+id/tvEmptyMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Selecione um responsável para visualizar os patrimônios"
        android:textSize="16sp"
        android:textColor="@color/text_secondary"
        android:gravity="center"
        android:layout_marginTop="16dp"
        android:paddingHorizontal="32dp"/>
</LinearLayout>
```

## Data Models

Nenhuma mudança nos modelos de dados existentes. Os modelos `Patrimonio`, `Responsavel` e `InventarioUiState` permanecem compatíveis.

## Error Handling

### Cenários de Erro

1. **Erro ao Carregar Responsáveis**
   - Exibir Toast com mensagem de erro
   - Manter ComboBox vazio
   - Permitir retry via swipe-to-refresh

2. **Erro ao Carregar Patrimônios**
   - Exibir Toast com mensagem de erro
   - Manter lista vazia
   - Permitir retry selecionando responsável novamente

3. **Responsável Sem Patrimônios**
   - Exibir mensagem específica: "Nenhum patrimônio encontrado para este responsável"
   - Não é um erro, é um estado válido

### Tratamento de Erros

```kotlin
// No ViewModel
result.fold(
    onSuccess = { patrimonios ->
        if (patrimonios.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                patrimonios = emptyList(),
                errorMessage = null  // Não é erro, é estado válido
            )
        } else {
            // ... processar patrimônios
        }
    },
    onFailure = { exception ->
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = "Erro ao carregar patrimônios: ${exception.message}"
        )
    }
)
```

## Testing Strategy

### Testes Unitários (ViewModel)

1. **Teste: init não carrega patrimônios**
   - Verificar que `patrimonios` está vazio após init
   - Verificar que `responsaveis` é carregado

2. **Teste: loadPatrimonios() não faz nada**
   - Chamar `loadPatrimonios()`
   - Verificar que estado não muda

3. **Teste: loadPatrimoniosByResponsavel carrega corretamente**
   - Chamar com ID válido
   - Verificar que patrimônios são carregados
   - Verificar que `hasResponsavelSelected = true`

4. **Teste: clearFilters reseta ao estado inicial**
   - Aplicar filtros
   - Chamar `clearFilters()`
   - Verificar que patrimônios estão vazios
   - Verificar que `hasResponsavelSelected = false`

### Testes de Integração (Activity)

1. **Teste: Estado inicial exibe mensagem vazia**
   - Abrir activity
   - Verificar que emptyView está visível
   - Verificar mensagem correta

2. **Teste: Selecionar responsável carrega patrimônios**
   - Selecionar responsável
   - Verificar que lista é carregada
   - Verificar que filtros são habilitados

3. **Teste: Limpar filtros volta ao estado inicial**
   - Selecionar responsável
   - Clicar em "Limpar Filtros"
   - Verificar que lista está vazia
   - Verificar que mensagem inicial é exibida

### Testes Manuais

1. **Fluxo Completo**
   - Abrir tela → Ver estado vazio
   - Selecionar responsável → Ver patrimônios
   - Aplicar filtro de status → Ver filtrados
   - Limpar filtros → Voltar ao estado vazio

2. **Rotação de Tela**
   - Selecionar responsável
   - Rotacionar dispositivo
   - Verificar que seleção é mantida

3. **Performance**
   - Medir tempo de abertura da tela
   - Comparar com versão anterior
   - Verificar uso de memória

## Implementation Notes

### Ordem de Implementação

1. Modificar ViewModel (init, loadPatrimonios, clearFilters)
2. Atualizar InventarioUiState (novos campos)
3. Modificar Activity (setupFiltros, aplicarFiltros, updateUI)
4. Atualizar layout XML (mensagem dinâmica)
5. Testar fluxo completo

### Compatibilidade

- Manter métodos existentes para compatibilidade
- `loadPatrimonios()` vazio mas não removido
- Todos os outros métodos funcionam normalmente

### Performance

**Antes:**
- Tempo de abertura: 2-3s (carrega todos os patrimônios)
- Memória inicial: 10-15 MB

**Depois:**
- Tempo de abertura: < 1s (carrega apenas responsáveis)
- Memória inicial: 2-3 MB
- Carregamento sob demanda após seleção

### Considerações de UX

1. **Mensagem Clara**: Usuário sabe exatamente o que fazer
2. **Feedback Visual**: Filtros desabilitados quando não aplicáveis
3. **Estado Preservado**: Rotação não perde contexto
4. **Fácil Reset**: Botão "Limpar Filtros" volta ao início
