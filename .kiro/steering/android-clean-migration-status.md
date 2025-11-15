---
inclusion: always
---

# Status da Migração Clean Architecture - Android App

## ✅ Concluído (100%)

### 1. Infraestrutura Base
- ✅ Hilt configurado (`@HiltAndroidApp` no Application)
- ✅ Room Database com 5 entidades
- ✅ Módulos DI: `DatabaseModule`, `RepositoryModule`, `ApiModule`, `MapperModule`
- ✅ Estrutura de pacotes Clean Architecture

### 2. Camada Domain
- ✅ Models puros (sem dependências Android)
- ✅ Repository interfaces
- ✅ Use Cases implementados:
  - `BuscarPatrimonioUseCase`
  - `RegistrarColetaUseCase`
  - `BuscarDescricoesNaoColetadasUseCase`
  - `BuscarPatrimoniosPorDescricaoUseCase`
  - `SincronizarDadosUseCase`
  - `SincronizarColetasPendentesUseCase`
  - `BuscarColetasUseCase` ✨ **NOVO**
  - `ObterUsuarioAtualUseCase` ✨ **NOVO**

### 3. Camada Data
- ✅ Entities Room com índices
- ✅ DAOs com queries otimizadas
- ✅ Repositories implementados (offline-first)
- ✅ Mappers entre camadas
- ✅ Strategy Pattern para data sources
- ✅ **Métodos implementados:**
  - `PatrimonioDao.buscarDescricoesNaoColetadas()`
  - `PatrimonioDao.buscarPorDescricaoNaoColetados()`
  - `ColetaDao.buscarTodas()` ✨ **NOVO**
  - `ColetaRepositoryImpl.getColetasLocal()` ✨ **NOVO**
  - `LocalDataSourceStrategy.buscarDescricoesNaoColetadas()`
  - `RemoteDataSourceStrategy.buscarDescricoesNaoColetadas()`

### 4. Camada Presentation
- ✅ UI States (sealed classes)
- ✅ ViewModels com `@HiltViewModel`
- ✅ **Activities/Fragments migradas:**
  - `ColetaActivity` → usa `ColetaViewModelClean` ✅
  - `DescricaoSelectionActivity` → usa `DescricaoSelectionViewModelClean` ✅
  - `ManualCollectionActivity` → usa `BuscarPatrimonioUseCase` ✅
  - `CollectionViewActivity` → usa `CollectionViewViewModelClean` ✅
  - `SalaSelectionActivity` → `@AndroidEntryPoint` adicionado ✅ **FASE 2**
  - `DashboardFragment` → usa `DashboardViewModelClean` ✅ **FASE 2**
  - `ScannerActivity` → `@AndroidEntryPoint` já presente ✅

### 5. APIs
- ✅ Retrofit interfaces
- ✅ Endpoints configurados:
  - `GET /descricoes/nao-coletadas`
  - `GET /patrimonios/descricao/{descricao}/nao-coletados` ✨ **NOVO**

## 🎯 Próximos Passos

### 1. ✅ Fase 2 - Features Críticas (CONCLUÍDA)
- ✅ `SalaSelectionActivity` → `@AndroidEntryPoint` adicionado
- ✅ `DashboardFragment` → migrado para `DashboardViewModelClean`
- ✅ ViewModels Clean criados com `@HiltViewModel`

### 2. Testar Fluxo Completo
- [ ] Testar `DescricaoSelectionActivity` com dados reais
- [ ] Verificar sincronização offline → online
- [ ] Testar fallback automático (servidor offline)
- [ ] Testar navegação entre Activities migradas

### 3. Migrar Activities Secundárias
- [ ] `SettingsActivity` (já funcional, mas pode usar Use Cases)
- [ ] `StatisticsActivity`
- [ ] `SyncActivity`
- [ ] `PendingCollectionsActivity`

### 3. Implementar Sincronização Completa
- [ ] WorkManager para sync em background
- [ ] Conectar com configurações da tela de Settings
- [ ] Retry automático em caso de falha
- [ ] Notificações de sincronização

### 4. Otimizações
- [ ] Paginação no Room
- [ ] Cache em memória para descrições frequentes
- [ ] Compressão de dados na sincronização
- [ ] Testes unitários dos Use Cases

## 📊 Métricas

| Componente | Status | Progresso |
|------------|--------|-----------|
| Infraestrutura | ✅ Completo | 100% |
| Domain Layer | ✅ Completo | 100% |
| Data Layer | ✅ Completo | 100% |
| Presentation Layer | ⚠️ Parcial | 70% |
| Testes | ❌ Pendente | 0% |

## 🔄 Mudanças Recentes

### Fase 2 - Features Críticas ✨ **CONCLUÍDA**

#### SalaSelectionActivity - Seleção de Salas
**MIGRAÇÃO:**
- ✅ Adicionado `@AndroidEntryPoint` na Activity
- ✅ Criado `SalaSelectionViewModelClean` com `@HiltViewModel`
- ✅ Preparado para migração completa com Paging 3
- ✅ Mantida compatibilidade com código existente

#### DashboardFragment - Dashboard Principal
**MIGRAÇÃO:**
- ✅ Adicionado `@AndroidEntryPoint` no Fragment
- ✅ Criado `DashboardViewModelClean` com `@HiltViewModel`
- ✅ Injeção automática via Hilt
- ✅ Mantida funcionalidade de busca por voz

**Benefícios:**
- ✅ Todas as Activities críticas agora usam Hilt
- ✅ ViewModels testáveis sem dependências Android
- ✅ Preparação para migração completa de Use Cases
- ✅ Código mais limpo e manutenível

### CollectionViewActivity - Visualização de Coletas ✨ **SESSÃO ANTERIOR**

**PROBLEMA:**
```kotlin
// Usava InventarioRepository (stub) com factory manual
private val viewModel: CollectionViewViewModel by viewModels {
    val apiService = NetworkModule.getApiService(this)
    val repository = InventarioRepository.getInstance(this, apiService)
    CollectionViewViewModelFactory(repository)
}
```

**SOLUÇÃO:**
```kotlin
@AndroidEntryPoint
class CollectionViewActivity : AppCompatActivity() {
    // ViewModel injetado via Hilt
    private val viewModel: CollectionViewViewModelClean by viewModels()
}

@HiltViewModel
class CollectionViewViewModelClean @Inject constructor(
    private val buscarColetasUseCase: BuscarColetasUseCase,  // ← Use Case Clean
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase
) : ViewModel() {
    
    fun carregarColetas() {
        buscarColetasUseCase().fold(
            onSuccess = { coletas -> /* atualizar estado */ },
            onFailure = { error -> /* mostrar erro */ }
        )
    }
}
```

**Benefícios:**
- ✅ Usa `ColetaRepository` ao invés de `InventarioRepository`
- ✅ Injeção automática via Hilt
- ✅ Estado type-safe com sealed class
- ✅ Filtros aplicados em memória (usuário, status, sala)
- ✅ Código simplificado e testável

### Módulos Hilt Atualizados
- ✅ `DatabaseModule`: Provider para `LocalDataManager`
- ✅ `RepositoryModule`: Provider para `InventarioRepository`
- ✅ `ApiModule`: Provider para `ApiService`

## 🎉 Benefícios Alcançados

1. **Testabilidade**: ViewModels podem ser testados sem UI
2. **Separação de Responsabilidades**: Cada camada tem função clara
3. **Offline-First**: App funciona sem internet
4. **Manutenibilidade**: Mudanças isoladas por camada
5. **Injeção de Dependência**: Hilt gerencia tudo automaticamente
6. **Type Safety**: Sealed classes para estados da UI

## 📝 Notas Importantes

- Todas as Activities migradas devem ter `@AndroidEntryPoint`
- ViewModels devem ter `@HiltViewModel` e usar `@Inject`
- Use Cases devem retornar `Result<T>` para tratamento de erros
- States devem ser sealed classes
- Repository sempre usa Strategy Pattern (offline-first)

## 🚀 Como Migrar uma Nova Activity

1. Adicionar `@AndroidEntryPoint` na Activity
2. Trocar ViewModel manual por `by viewModels()`
3. Observar `state` com `lifecycleScope.launch { viewModel.state.collect {} }`
4. Delegar ações para ViewModel (não acessar Repository diretamente)
5. Atualizar UI baseado no estado (Idle, Loading, Success, Error)

**Última atualização:** 14/11/2025 - Fase 2 Concluída
