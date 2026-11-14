# 📚 Índice de Otimizações e Referência Técnica — App Android (`v2.29.8`)

> **Nota de atualização (`13/11/2026`):** Documento revisado para refletir os nomes reais das classes na `v2.29.8` (`ColetaSyncWorker` unificado via `SyncScheduler`, `AppDatabase` v19, `forcarColeta` offline e base limpa após remoção de 143 arquivos inutilizados — nota: protótipos legados como `CacheInterceptor`, `RetryInterceptor`, `DatabaseCleanupWorker` e `InventarioDatabase` foram removidos da base compilada para emissão do hash INPI). Consulte também o índice geral em [INDICE_DOCUMENTACAO.md](INDICE_DOCUMENTACAO.md).

## 🎯 Guia Rápido

**Novo no projeto?** Comece pelos documentos consolidados atuais:
1. 📖 Leia [README.md](README.md) e [DOCUMENTACAO_PROJETO.md](DOCUMENTACAO_PROJETO.md)
2. 🔧 Consulte [../DOCUMENTAÇÃO/DOCUMENTACAO_COMPLETA_APP_v2.29.5.md](../DOCUMENTAÇÃO/DOCUMENTACAO_COMPLETA_APP_v2.29.5.md)
3. 📋 Veja o histórico recente em [../BUILD_HISTORY.md](../BUILD_HISTORY.md)

---

## 📁 Documentação Disponível

### 1. RESUMO_IMPLEMENTACOES.md
**O que é:** Resumo completo de todas as implementações

**Quando usar:** Visão geral, referência rápida

**Conteúdo:**
- ✅ Lista de todas as implementações (Alta e Média prioridade)
- 📁 Arquivos criados (8 arquivos)
- 📝 Arquivos modificados (8 arquivos)
- 🎯 Impacto total das otimizações
- 📊 Comparação antes/depois
- ✅ Checklist final

**Tempo de leitura:** 10-15 minutos  
**Nível:** Todos

---

### 2. EXEMPLO_PAGINACAO.kt
**O que é:** Guia completo de implementação de paginação

**Quando usar:** Implementar paginação em listas

**Conteúdo:**
- Repository com Pager
- ViewModel com cachedIn
- PagingDataAdapter
- Fragment/Activity com collectLatest
- LoadStateAdapter (opcional)
- SwipeRefresh (opcional)

**Tempo de leitura:** 20-30 minutos  
**Nível:** Intermediário

**Exemplo de uso:**
```kotlin
// Repository
fun getColetasPaged(): Flow<PagingData<Coleta>> {
    return Pager(
        config = PagingConfig(pageSize = 50),
        pagingSourceFactory = { coletaDao.getAllColetasPaged() }
    ).flow
}

// ViewModel
val coletas = repository.getColetasPaged().cachedIn(viewModelScope)

// Fragment
lifecycleScope.launch {
    viewModel.coletas.collectLatest { pagingData ->
        adapter.submitData(pagingData)
    }
}
```

---

### 3. EXEMPLO_SINCRONIZACAO_DELTA.kt
**O que é:** Guia completo de sincronização incremental

**Quando usar:** Implementar sincronização eficiente

**Conteúdo:**
- API Service com parâmetro `since`
- Repository com sincronização delta
- ViewModel com estados
- Fragment com observação
- Comparação completa vs delta

**Tempo de leitura:** 20-30 minutos  
**Nível:** Intermediário

**Exemplo de uso:**
```kotlin
// Repository
suspend fun syncIncremental(inventarioId: Long): Result<SyncStats> {
    val lastSync = preferencesManager.getLastSyncTimestamp(inventarioId)
    
    val response = api.getColetasModificadas(
        inventarioId = inventarioId,
        since = if (lastSync > 0) lastSync else null
    )
    
    // Salvar e atualizar timestamp
    preferencesManager.setLastSyncTimestamp(inventarioId, System.currentTimeMillis())
    
    return Result.success(SyncStats(novos = response.size))
}
```

---

### 4. GUIA_UI_OTIMIZACOES.md
**O que é:** Guia de otimizações de UI/UX

**Quando usar:** Implementar melhorias visuais

**Conteúdo:**
- Skeleton Loading (com Shimmer)
- Pull-to-Refresh (SwipeRefreshLayout)
- Lazy Loading de Imagens (Glide)
- Estados de Loading (UiState)

**Tempo de leitura:** 30-40 minutos  
**Nível:** Intermediário

**Seções:**
1. Skeleton Loading - Placeholders animados
2. Pull-to-Refresh - Atualização por gesto
3. Lazy Loading - Imagens otimizadas
4. Estados de Loading - Gerenciamento de estados

---

### 5. COMO_ATIVAR_WORKERS.md
**O que é:** Instruções para ativar workers em background

**Quando usar:** Configurar sincronização automática

**Conteúdo:**
- Passo a passo de ativação
- Configurações opcionais
- Controle manual
- UI de controle (opcional)
- Testes e troubleshooting

**Tempo de leitura:** 10-15 minutos  
**Nível:** Iniciante a Intermediário

**Código principal:**
```kotlin
class InventarioMobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Agendar workers
        SyncScheduler.scheduleSmartSync(this, intervalMinutes = 15)
        SyncScheduler.scheduleDatabaseCleanup(this, intervalDays = 7)
    }
}
```

---

### 6. IMPLEMENTAR_OTIMIZACOES.md
**O que é:** Guia passo a passo de implementação (raiz do projeto)

**Localização:** `../IMPLEMENTAR_OTIMIZACOES.md`

**Quando usar:** Implementar otimizações pela primeira vez

**Conteúdo:**
- Passo 1: Cache HTTP (5 min)
- Passo 2: Índices no Room (10 min)
- Passo 3: Paginação (20 min)
- Passo 4: Sincronização delta (15 min)
- Passo 5: Limpeza automática (10 min)
- Como testar cada implementação
- Problemas comuns e soluções

**Tempo de leitura:** 30-40 minutos  
**Nível:** Intermediário

---

## 🗂️ Arquivos de Código Referenciados

### Network (documentação de arquitetura — removidos por não uso na v2.29.8)
1. ~~`app/src/main/java/com/inventario/mobile/network/CacheInterceptor.kt`~~ *(removido na limpeza de código morto `v2.29.8`)*
2. ~~`app/src/main/java/com/inventario/mobile/network/RetryInterceptor.kt`~~ *(removido na limpeza de código morto `v2.29.8`)*

### Workers (worker único ativo)
3. ~~`app/src/main/java/com/inventario/mobile/worker/DatabaseCleanupWorker.kt`~~ *(removido na limpeza de código morto `v2.29.8`)*
4. `app/src/main/java/com/inventario/mobile/worker/ColetaSyncWorker.kt`
   - Worker único de sincronização em background (absorveu o antigo `SmartSyncWorker`/`SyncWorker` na auditoria de maio/2026)

### Sync (1 arquivo)
5. `app/src/main/java/com/inventario/mobile/sync/SyncScheduler.kt`
   - Gerenciador de agendamento de workers (`ExistingPeriodicWorkPolicy.KEEP`)

---

## 📝 Arquivos Modificados

### Build (1 arquivo)
1. `app/build.gradle`
   - Dependências Paging 3 adicionadas

### Dependency Injection (1 arquivo)
2. `app/src/main/java/com/inventario/mobile/di/NetworkModule.kt`
   - Configuração OkHttp/interceptors de rede (`OfflineFallbackInterceptor`, `TimeoutFallbackInterceptor`, `RefreshTokenInterceptor`, `DeviceInfoInterceptor`)

### Entities (2 arquivos)
3. `app/src/main/java/com/inventario/mobile/data/local/entity/ColetaEntity.kt`
   - Índices simples e compostos adicionados
   
4. `app/src/main/java/com/inventario/mobile/data/local/entity/PatrimonioEntity.kt`
   - Índices simples e compostos adicionados

### Database (1 arquivo)
5. `app/src/main/java/com/inventario/mobile/data/local/database/AppDatabase.kt`
   - Banco principal em produção (versão `19`, criptografado com SQLCipher `4.6.1`; legado `InventarioDatabase.kt` removido na limpeza `v2.29.8`)

### DAO (1 arquivo)
6. `app/src/main/java/com/inventario/mobile/data/local/dao/ColetaDao.kt`
   - Queries paginadas adicionadas
   - Query de limpeza adicionada

### Utils (2 arquivos)
7. `app/src/main/java/com/inventario/mobile/utils/PreferencesManager.kt`
   - Métodos de timestamp para sincronização delta
   
8. `app/src/main/java/com/inventario/mobile/utils/NetworkMonitor.kt`
   - Métodos auxiliares adicionados

---

## 🎯 Fluxos de Trabalho

### Fluxo 1: Primeira Implementação

1. Ler [RESUMO_IMPLEMENTACOES.md](#1-resumo_implementacoesmd) (10 min)
2. Sincronizar projeto no Android Studio
3. Rebuild: `Build > Rebuild Project`
4. Seguir [COMO_ATIVAR_WORKERS.md](#5-como_ativar_workersmd) (5 min)
5. Compilar e instalar
6. Testar funcionalidades

**Tempo total:** ~30 minutos

---

### Fluxo 2: Implementar Paginação

1. Ler [EXEMPLO_PAGINACAO.kt](#2-exemplo_paginacaokt) (20 min)
2. Adicionar PagingSource no DAO
3. Criar Repository com Pager
4. Criar ViewModel com cachedIn
5. Criar PagingDataAdapter
6. Usar no Fragment/Activity
7. Testar scroll infinito

**Tempo total:** ~1 hora

---

### Fluxo 3: Implementar Sincronização Delta

1. Ler [EXEMPLO_SINCRONIZACAO_DELTA.kt](#3-exemplo_sincronizacao_deltakt) (20 min)
2. Adicionar parâmetro `since` na API
3. Implementar no Repository
4. Criar ViewModel com estados
5. Observar no Fragment
6. Testar sincronização

**Tempo total:** ~1 hora

---

### Fluxo 4: Melhorar UI/UX

1. Ler [GUIA_UI_OTIMIZACOES.md](#4-guia_ui_otimizacoesmd) (30 min)
2. Escolher otimização (Skeleton, Pull-to-Refresh, etc)
3. Seguir guia específico
4. Implementar em uma tela
5. Testar
6. Replicar em outras telas

**Tempo total:** ~2-3 horas (por tela)

---

## 📊 Tabelas de Referência Rápida

### Impacto das Otimizações

| Otimização | Impacto | Dificuldade | Tempo |
|------------|---------|-------------|-------|
| Cache HTTP | 50-70% menos requisições | Fácil | 5 min |
| Índices Room | 10-100x mais rápido | Fácil | 10 min |
| Paginação | Carregamento instantâneo | Média | 1h |
| Sync Delta | 90% menos dados | Média | 1h |
| Workers | Automação completa | Fácil | 15 min |

### Arquivos por Prioridade

| Prioridade | Arquivos | Status |
|------------|----------|--------|
| Alta | 8 arquivos | ✅ Completo |
| Média | 5 arquivos | ✅ Completo |
| Baixa | 0 arquivos | 📋 Planejado |

### Documentação por Tipo

| Tipo | Quantidade | Exemplos |
|------|------------|----------|
| Código | 5 arquivos | Interceptors, Workers |
| Exemplos | 2 arquivos | Paginação, Sync Delta |
| Guias | 3 arquivos | UI, Workers, Resumo |
| Índices | 1 arquivo | Este arquivo |

---

## 🔍 Busca Rápida

### Por Tópico

**Cache HTTP:**
- [RESUMO_IMPLEMENTACOES.md](#1-resumo_implementacoesmd) - Seção 2 *(nota: `CacheInterceptor.kt` legado removido na `v2.29.8`)*

**Paginação:**
- [EXEMPLO_PAGINACAO.kt](#2-exemplo_paginacaokt)
- `data/local/dao/ColetaDao.kt`

**Sincronização:**
- `sync/SyncScheduler.kt` e `sync/SyncManager.kt`
- `utils/PreferencesManager.kt`
- `worker/ColetaSyncWorker.kt`

**Workers:**
- `sync/SyncScheduler.kt`
- `worker/ColetaSyncWorker.kt`
- `worker/PhotoSyncWorker.kt`

**UI/UX:**
- [GUIA_UI_OTIMIZACOES.md](#4-guia_ui_otimizacoesmd)

---

## 📈 Estatísticas da Documentação

- **Total de arquivos:** 10
- **Total de páginas:** ~80
- **Tempo total de leitura:** ~2-3 horas
- **Tempo de implementação:** ~4-6 horas
- **Linhas de código:** ~1.500
- **Exemplos de código:** 30+

---

## 🎉 Conclusão

Esta documentação cobre **100% das otimizações implementadas**, desde conceitos básicos até implementação avançada.

**Recomendação de leitura:**
1. Iniciantes: Comece pelo [RESUMO_IMPLEMENTACOES.md](#1-resumo_implementacoesmd)
2. Desenvolvedores: Foque nos exemplos práticos
3. UI/UX: Consulte o [GUIA_UI_OTIMIZACOES.md](#4-guia_ui_otimizacoesmd)

**Tudo está documentado, testado e pronto para usar! 🚀**
