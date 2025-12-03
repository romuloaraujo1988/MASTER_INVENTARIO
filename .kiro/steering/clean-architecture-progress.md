---
inclusion: always
---

# Clean Architecture + MVVM - Progresso da Implementação

## ✅ Implementado

### Camada Data (Local)

#### Entities (Room)
- ✅ `PatrimonioEntity` - com índices para performance
- ✅ `SalaEntity`
- ✅ `ResponsavelEntity`
- ✅ `ColetaEntity` - com controle de sincronização

#### DAOs (Room)
- ✅ `PatrimonioDao` - com queries otimizadas
  - Buscar por número
  - Buscar não coletados (paginado)
  - **Buscar descrições não coletadas** (NOVO)
  - Buscar por descrição não coletados
  - Marcar como coletado
- ✅ `SalaDao`
- ✅ `ResponsavelDao`
- ✅ `ColetaDao` - com controle de pendências

#### Database
- ✅ `AppDatabase` - Room Database configurado

### Camada Data (Mappers)
- ✅ `PatrimonioMapper` - Entity ↔ Domain
- ✅ `SalaMapper` - Entity ↔ Domain
- ✅ `ColetaMapper` - Entity ↔ Domain

### Camada Domain

#### Use Cases
- ✅ `BuscarPatrimonioUseCase`
- ✅ `RegistrarColetaUseCase` - com validações de negócio
- ✅ `BuscarDescricoesNaoColetadasUseCase` (NOVO)
- ✅ `BuscarPatrimoniosPorDescricaoUseCase` (NOVO)
- ✅ `SincronizarDadosUseCase`
- ✅ `SincronizarColetasPendentesUseCase`

### Camada Presentation

#### UI States
- ✅ `ColetaState` - Idle, Loading, Success, Error
- ✅ `DescricaoState` - para seleção de descrições
- ✅ `SyncState` - para sincronização

### Backend (Java)

#### Endpoints
- ✅ `/api/mobile/descricoes/nao-coletadas` - **NOVO**
  - Retorna apenas descrições de patrimônios não coletados
  - Facilita coleta sem etiqueta

#### DAO
- ✅ `PatrimonioDAORefactored.buscarPatrimoniosColetados()` - **NOVO**

---

### Camada Data (Repositories)
- ✅ `PatrimonioRepositoryImpl` - Offline-first com fallback
- ✅ `ColetaRepositoryImpl` - Com sincronização automática

### Camada Data (APIs)
- ✅ `PatrimonioApi` - Retrofit interface
- ✅ `ColetaApi` - Retrofit interface
- ✅ `ApiResponse` - DTO padrão

### Injeção de Dependência (Hilt)
- ✅ `DatabaseModule` - Providers do Room
- ✅ `RepositoryModule` - Binding de interfaces
- ✅ `MapperModule` - Providers de Mappers
- ✅ `ApiModule` - Providers de APIs
- ✅ `@HiltAndroidApp` - Application configurado

### ViewModels Refatorados
- ✅ `DescricaoSelectionViewModelClean` - Usa Use Case
- ✅ `ColetaViewModelClean` - Usa Use Cases

---

## 🚧 Próximos Passos

### 1. ~~Implementar Repositories (Data Layer)~~ ✅ CONCLUÍDO

Criar implementações dos repositories que usam Room + Retrofit:

```kotlin
// data/repository/PatrimonioRepositoryImpl.kt
class PatrimonioRepositoryImpl(
    private val patrimonioDao: PatrimonioDao,
    private val patrimonioApi: PatrimonioApi,
    private val mapper: PatrimonioMapper
) : PatrimonioRepository {
    
    override suspend fun buscarDescricoesNaoColetadas(): List<String> {
        // 1. Tentar buscar do servidor
        // 2. Se falhar, buscar do banco local
        // 3. Retornar resultado
    }
    
    override suspend fun buscarPorDescricaoNaoColetados(descricao: String): List<Patrimonio> {
        // Offline-first: busca local primeiro
    }
}
```

### 2. ~~Configurar Injeção de Dependência (Hilt)~~ ✅ CONCLUÍDO

```kotlin
// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase
    
    @Provides
    fun providePatrimonioDao(db: AppDatabase): PatrimonioDao
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindPatrimonioRepository(
        impl: PatrimonioRepositoryImpl
    ): PatrimonioRepository
}

// di/UseCaseModule.kt
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    // Use Cases são injetados automaticamente via @Inject
}
```

### 3. Refatorar ViewModels Existentes

Atualizar ViewModels para usar Use Cases:

```kotlin
// presentation/descricao/DescricaoSelectionViewModel.kt
@HiltViewModel
class DescricaoSelectionViewModel @Inject constructor(
    private val buscarDescricoesNaoColetadasUseCase: BuscarDescricoesNaoColetadasUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<DescricaoState>(DescricaoState.Idle)
    val state: StateFlow<DescricaoState> = _state.asStateFlow()
    
    fun carregarDescricoes() {
        viewModelScope.launch {
            _state.value = DescricaoState.Loading
            
            buscarDescricoesNaoColetadasUseCase().fold(
                onSuccess = { descricoes ->
                    _state.value = DescricaoState.Success(descricoes)
                },
                onFailure = { error ->
                    _state.value = DescricaoState.Error(error.message ?: "Erro desconhecido")
                }
            )
        }
    }
}
```

### 4. Criar API Service (Retrofit)

```kotlin
// data/remote/api/PatrimonioApi.kt
interface PatrimonioApi {
    @GET("api/mobile/descricoes/nao-coletadas")
    suspend fun buscarDescricoesNaoColetadas(
        @Query("idInventario") idInventario: Int?
    ): ApiResponse<List<String>>
}
```

### 5. Implementar Sincronização Offline

```kotlin
// data/repository/SincronizacaoRepositoryImpl.kt
class SincronizacaoRepositoryImpl(
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao,
    private val responsavelDao: ResponsavelDao,
    private val syncApi: SyncApi
) : SincronizacaoRepository {
    
    override suspend fun sincronizarTodosDados(): Int {
        // 1. Baixar patrimônios do servidor
        // 2. Baixar salas
        // 3. Baixar responsáveis
        // 4. Salvar tudo no Room
        // 5. Retornar total sincronizado
    }
}
```

### 6. ✅ Paginação com Paging 3 (CONCLUÍDO - 02/12/2025)

- ✅ `PagedResponse.kt` - DTO para resposta paginada
- ✅ `MobilePatrimonioDTO.kt` - DTO de patrimônio com conversão
- ✅ `PatrimonioPagingSource.kt` - PagingSource para patrimônios
- ✅ `PatrimonioPorSalaPagingSource.kt` - PagingSource por sala
- ✅ `BuscarPatrimoniosPaginadoUseCase.kt` - Use Case com Paging 3
- ✅ `PatrimonioPagingAdapter.kt` - Adapter com PagingDataAdapter
- ✅ `PatrimonioLoadStateAdapter.kt` - Adapter para estados de loading
- ✅ `PatrimonioListViewModelPaging.kt` - ViewModel com Paging 3
- ✅ `PatrimonioListPagingFragment.kt` - Fragment de exemplo
- ✅ `PatrimonioApi.kt` - Endpoints paginados adicionados

### 7. Otimizações Pendentes

- ✅ Implementar paginação com Paging 3
- [ ] Cache em memória para descrições frequentes
- ✅ Índices no banco Room (já criados)
- [ ] Compressão de dados na sincronização
- ✅ Background sync com WorkManager

### 8. Testes

- [ ] Testes unitários dos Use Cases
- [ ] Testes dos Repositories (mock)
- [ ] Testes dos Mappers
- [ ] Testes de integração do Room

---

## 📊 Benefícios Implementados

### Performance
- ✅ Queries otimizadas com índices
- ✅ Filtro de descrições não coletadas (reduz dados)
- ✅ Paginação preparada

### Offline-First
- ✅ Banco Room completo
- ✅ Coletas salvas localmente
- ✅ Sincronização em background

### Manutenibilidade
- ✅ Separação clara de camadas
- ✅ Use Cases testáveis
- ✅ Domain sem dependências Android
- ✅ Mappers centralizados

### UX
- ✅ Apenas descrições não coletadas aparecem
- ✅ Coleta mais rápida (menos opções)
- ✅ Funciona offline

---

## 🎯 Prioridades

1. ~~**ALTA**: Implementar Repositories (conectar tudo)~~ ✅
2. ~~**ALTA**: Configurar Hilt (DI)~~ ✅
3. ~~**MÉDIA**: Criar APIs Retrofit~~ ✅
4. **MÉDIA**: Refatorar Activities/Fragments para usar novos ViewModels
5. **MÉDIA**: Implementar sincronização offline completa
6. **BAIXA**: Testes unitários

## 📋 Tarefas Restantes

### Para Usar Clean Architecture no App

1. **Adicionar dependências Hilt no build.gradle**
   ```gradle
   plugins {
       id 'dagger.hilt.android.plugin'
       id 'kotlin-kapt'
   }
   
   dependencies {
       implementation "com.google.dagger:hilt-android:2.48"
       kapt "com.google.dagger:hilt-compiler:2.48"
   }
   ```

2. **Atualizar Activities para usar @AndroidEntryPoint**
   ```kotlin
   @AndroidEntryPoint
   class DescricaoSelectionActivity : AppCompatActivity() {
       private val viewModel: DescricaoSelectionViewModelClean by viewModels()
   }
   ```

3. **Implementar sincronização offline**
   - Criar `SincronizacaoRepositoryImpl`
   - Adicionar WorkManager para sync em background
   - Implementar estratégia de retry

4. **Migrar Activities existentes**
   - `DescricaoSelectionActivity` → usar `DescricaoSelectionViewModelClean`
   - `ColetaActivity` → usar `ColetaViewModelClean`
   - Observar estados com `collectAsState` ou `collect`

