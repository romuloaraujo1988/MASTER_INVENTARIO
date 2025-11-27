# Design Document

## Overview

Este documento descreve a solução técnica para corrigir o problema de carregamento de coletas pendentes na tela `PendingCollectionsActivity`. A correção envolve substituir o uso incorreto de `MockApiService` pelo `ApiClient` real, corrigir a instanciação do contexto no acesso ao banco Room, e adicionar logs de diagnóstico.

## Architecture

A solução mantém a arquitetura existente (MVVM) mas corrige as dependências:

```
┌─────────────────────────────────────────────────────────────┐
│              PendingCollectionsActivity (View)               │
│  - Observa uiState do ViewModel                              │
│  - Renderiza lista de coletas                                │
└──────────────────────┬──────────────────────────────────────┘
                       │ cria via Factory
                       ▼
┌─────────────────────────────────────────────────────────────┐
│           PendingCollectionsViewModel (ViewModel)            │
│  - Gerencia PendingCollectionsUiState                        │
│  - Chama repository.getColetasPendentes()                    │
│  - Usa contexto correto para Room                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              InventarioRepository (Repository)               │
│  - Busca coletas do Room via ColetaDao                       │
│  - Converte ColetaEntity → Coleta                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Room Database (Data)                        │
│  - ColetaDao.buscarPendentes()                               │
│  - Query: WHERE sincronizado = 0                             │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. PendingCollectionsActivity (Modificação)

**Mudança:** Substituir `MockApiService()` por `ApiClient.getApiService(this)` e `LocalDataManager(this)` por `LocalDataManager.getInstance(this)`.

```kotlin
// ANTES (incorreto):
private val viewModel: PendingCollectionsViewModel by viewModels {
    PendingCollectionsViewModelFactory(
        InventarioRepository(MockApiService(), LocalDataManager(this), this)
    )
}

// DEPOIS (correto):
private val viewModel: PendingCollectionsViewModel by viewModels {
    PendingCollectionsViewModelFactory(
        InventarioRepository(
            ApiClient.getApiService(this),
            LocalDataManager.getInstance(this),
            this
        )
    )
}
```

### 2. PendingCollectionsViewModel (Modificação)

**Mudança:** Corrigir acesso ao Room Database usando contexto do repositório.

```kotlin
// ANTES (incorreto):
val database = InventarioDatabase.getDatabase(android.app.Application())

// DEPOIS (correto):
// Remover acesso direto ao database no ViewModel
// Usar métodos do repository que já tem o contexto correto
```

### 3. InventarioRepository (Verificação)

O método `getColetasPendentes()` já está implementado corretamente. Apenas adicionar logs mais detalhados.

## Data Models

### ColetaEntity (Existente)
```kotlin
@Entity(tableName = "coleta")
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idPatrimonio: Int,
    val numeroPatrimonio: String,
    val sincronizado: Boolean = false,  // ← Campo chave para filtro
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    // ... outros campos
)
```

### Coleta (Existente)
```kotlin
data class Coleta(
    val id: Int? = null,
    val patrimonioId: Int,
    val sincronizado: Boolean = false,
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    // ... outros campos
)
```



## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Filtro de coletas pendentes

*For any* conjunto de coletas no banco Room, quando a tela de coletas pendentes é aberta, apenas as coletas com `sincronizado = false` devem ser retornadas e exibidas na lista.

**Validates: Requirements 1.1, 1.2**

### Property 2: Estado vazio consistente

*For any* estado do banco onde não existem coletas com `sincronizado = false`, a UI deve exibir o estado vazio (layoutEmptyState visível, RecyclerView oculta).

**Validates: Requirements 1.3**

## Error Handling

### Erros de Banco de Dados
- Se o Room falhar ao buscar coletas, capturar exceção e exibir mensagem de erro
- Registrar log detalhado do erro para diagnóstico
- Retornar lista vazia para não quebrar a UI

### Erros de Contexto
- Se o contexto for inválido, usar fallback para Application context
- Registrar warning no log

## Testing Strategy

### Testes Manuais (Recomendados)
1. **Teste de carregamento**: Criar coletas offline, abrir tela e verificar se aparecem
2. **Teste de estado vazio**: Sincronizar todas as coletas, abrir tela e verificar estado vazio
3. **Teste de sincronização**: Clicar em "Sincronizar Todas" e verificar comportamento
4. **Teste de exclusão**: Excluir uma coleta e verificar que some da lista
5. **Teste de retry**: Clicar em "Tentar Novamente" em coleta com erro

### Verificação de Logs (ADB)
```bash
adb logcat -s PendingCollectionsVM:* InventarioRepository:* | grep -E "coletas|pendentes|erro"
```

### Checklist de Validação
- [ ] Coletas pendentes aparecem na lista
- [ ] Contador mostra quantidade correta
- [ ] Estado vazio aparece quando não há pendentes
- [ ] Botão "Sincronizar Todas" funciona
- [ ] Botão "Excluir" funciona
- [ ] Botão "Tentar Novamente" funciona
- [ ] Logs de diagnóstico aparecem no Logcat

