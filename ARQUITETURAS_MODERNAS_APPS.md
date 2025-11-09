# 🏗️ Melhores Arquiteturas de Software para Apps Modernos

## 🏆 Top 3 Arquiteturas Recomendadas (2025)

### 1. Clean Architecture + MVVM (Mais Recomendada)

#### Estrutura
```
app/
├── data/                    # Camada de Dados
│   ├── local/              # Room Database, SharedPreferences
│   ├── remote/             # Retrofit, API calls
│   ├── repository/         # Implementação dos repositórios
│   └── model/              # Entities, DTOs
│
├── domain/                  # Camada de Domínio (Regras de Negócio)
│   ├── model/              # Domain Models (entidades puras)
│   ├── repository/         # Interfaces dos repositórios
│   └── usecase/            # Use Cases (casos de uso)
│
└── presentation/            # Camada de Apresentação
    ├── ui/                 # Activities, Fragments
    ├── viewmodel/          # ViewModels
    └── mapper/             # Conversores UI ↔ Domain
```

#### Exemplo Prático

```kotlin
// Domain Layer - Use Case
class RegistrarColetaUseCase(
    private val coletaRepository: ColetaRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(numeroPatrimonio: String): Result<Coleta> {
        return try {
            // 1. Validar patrimônio
            val patrimonio = patrimonioRepository.buscarPorNumero(numeroPatrimonio)
                ?: return Result.failure(Exception("Patrimônio não encontrado"))
            
            // 2. Verificar se já foi coletado
            if (coletaRepository.jaFoiColetado(patrimonio.id)) {
                return Result.failure(Exception("Já coletado"))
            }
            
            // 3. Registrar coleta
            val coleta = Coleta(
                idPatrimonio = patrimonio.id,
                dataColeta = System.currentTimeMillis()
            )
            
            coletaRepository.registrar(coleta)
            Result.success(coleta)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Data Layer - Repository Implementation
class ColetaRepositoryImpl(
    private val remoteDataSource: ColetaRemoteDataSource,
    private val localDataSource: ColetaLocalDataSource
) : ColetaRepository {
    
    override suspend fun registrar(coleta: Coleta): Result<Coleta> {
        return try {
            // Salvar localmente primeiro
            localDataSource.inserir(coleta)
            
            // Tentar sincronizar
            if (isOnline()) {
                remoteDataSource.enviar(coleta)
                localDataSource.marcarSincronizada(coleta.id)
            }
            
            Result.success(coleta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Presentation Layer - ViewModel
class ColetaViewModel(
    private val registrarColetaUseCase: RegistrarColetaUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<ColetaState>(ColetaState.Idle)
    val state: StateFlow<ColetaState> = _state.asStateFlow()
    
    fun registrarColeta(numeroPatrimonio: String) {
        viewModelScope.launch {
            _state.value = ColetaState.Loading
            
            registrarColetaUseCase(numeroPatrimonio).fold(
                onSuccess = { coleta ->
                    _state.value = ColetaState.Success(coleta)
                },
                onFailure = { error ->
                    _state.value = ColetaState.Error(error.message ?: "Erro")
                }
            )
        }
    }
}

// UI Layer - Fragment
class ColetaFragment : Fragment() {
    
    private val viewModel: ColetaViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observar estado
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ColetaState.Loading -> showLoading()
                    is ColetaState.Success -> showSuccess(state.coleta)
                    is ColetaState.Error -> showError(state.message)
                    is ColetaState.Idle -> hideLoading()
                }
            }
        }
        
        // Ação do usuário
        binding.btnRegistrar.setOnClickListener {
            val numero = binding.edtNumero.text.toString()
            viewModel.registrarColeta(numero)
        }
    }
}
```

#### Vantagens
✅ **Separação clara de responsabilidades**
✅ **Testável** - Cada camada pode ser testada isoladamente
✅ **Manutenível** - Mudanças em uma camada não afetam outras
✅ **Escalável** - Fácil adicionar novas features
✅ **Independente de frameworks** - Domain não depende de Android

#### Quando Usar
- Apps médios a grandes
- Equipes com múltiplos desenvolvedores
- Projetos de longo prazo
- Apps que precisam de alta testabilidade

---

### 2. MVI (Model-View-Intent)

#### Conceito
Arquitetura unidirecional inspirada em Redux/Elm

```kotlin
// State - Estado único da tela
data class ColetaViewState(
    val isLoading: Boolean = false,
    val patrimonio: Patrimonio? = null,
    val coletas: List<Coleta> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

// Intent - Intenções do usuário
sealed class ColetaIntent {
    data class BuscarPatrimonio(val numero: String) : ColetaIntent()
    data class RegistrarColeta(val patrimonio: Patrimonio) : ColetaIntent()
    object LimparErro : ColetaIntent()
    object CarregarColetas : ColetaIntent()
}

// ViewModel com MVI
class ColetaViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(ColetaViewState())
    val state: StateFlow<ColetaViewState> = _state.asStateFlow()
    
    fun processIntent(intent: ColetaIntent) {
        when (intent) {
            is ColetaIntent.BuscarPatrimonio -> buscarPatrimonio(intent.numero)
            is ColetaIntent.RegistrarColeta -> registrarColeta(intent.patrimonio)
            is ColetaIntent.LimparErro -> limparErro()
            is ColetaIntent.CarregarColetas -> carregarColetas()
        }
    }
    
    private fun buscarPatrimonio(numero: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val patrimonio = repository.buscarPorNumero(numero)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        patrimonio = patrimonio
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private fun registrarColeta(patrimonio: Patrimonio) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                repository.registrarColeta(patrimonio)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Coleta registrada!",
                        patrimonio = null
                    )
                }
                // Recarregar lista
                carregarColetas()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

// Fragment
class ColetaFragment : Fragment() {
    
    private val viewModel: ColetaViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observar estado único
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                render(state)
            }
        }
        
        // Enviar intenções
        binding.btnBuscar.setOnClickListener {
            val numero = binding.edtNumero.text.toString()
            viewModel.processIntent(ColetaIntent.BuscarPatrimonio(numero))
        }
        
        binding.btnRegistrar.setOnClickListener {
            state.patrimonio?.let { patrimonio ->
                viewModel.processIntent(ColetaIntent.RegistrarColeta(patrimonio))
            }
        }
    }
    
    private fun render(state: ColetaViewState) {
        // Renderizar UI baseado no estado
        binding.progressBar.isVisible = state.isLoading
        
        state.patrimonio?.let { patrimonio ->
            binding.tvPatrimonio.text = patrimonio.descricao
            binding.btnRegistrar.isEnabled = true
        }
        
        state.error?.let { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }
        
        state.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerColetas.adapter = ColetaAdapter(state.coletas)
    }
}
```

#### Vantagens
✅ **Fluxo unidirecional** - Fácil rastrear mudanças
✅ **Estado previsível** - Um único estado por tela
✅ **Debugável** - Histórico de estados e intenções
✅ **Testável** - Estado é imutável

#### Quando Usar
- Apps com UI complexa
- Muitas interações do usuário
- Necessidade de time-travel debugging
- Equipes que gostam de programação funcional

---

### 3. Modular Architecture (Multi-Module)

#### Estrutura
```
app/
├── :app                    # App module (apenas composição)
├── :feature
│   ├── :coleta            # Feature de coleta
│   ├── :dashboard         # Feature de dashboard
│   └── :sync              # Feature de sincronização
├── :core
│   ├── :data              # Camada de dados compartilhada
│   ├── :domain            # Domain models compartilhados
│   ├── :ui                # Componentes UI reutilizáveis
│   └── :network           # Configuração de rede
└── :shared
    ├── :common            # Utilitários comuns
    └── :testing           # Utilitários de teste
```

#### build.gradle (feature module)
```kotlin
// :feature:coleta/build.gradle.kts
plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    // Dependências de core
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    
    // Dependências externas
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.navigation)
}
```

#### Vantagens
✅ **Build paralelo** - Compila módulos em paralelo (mais rápido)
✅ **Reutilização** - Módulos podem ser reutilizados
✅ **Isolamento** - Features isoladas
✅ **Equipes** - Cada equipe pode trabalhar em um módulo
✅ **Testes** - Testar módulos independentemente

#### Quando Usar
- Apps grandes (50k+ linhas)
- Múltiplas equipes
- Necessidade de build rápido
- Features independentes

---

## 📊 Comparação das Arquiteturas

| Aspecto | Clean + MVVM | MVI | Modular |
|---------|--------------|-----|---------|
| **Complexidade** | Média | Alta | Alta |
| **Curva de Aprendizado** | Média | Alta | Média |
| **Testabilidade** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Manutenibilidade** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Performance Build** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Escalabilidade** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Tamanho do App** | Pequeno-Grande | Médio-Grande | Grande |

---

## 🎯 Recomendação para Seu Projeto (SIHCP)

### Arquitetura Ideal: **Clean Architecture + MVVM + Modularização Leve**

```
InventarioMobile/
├── app/                           # App principal
├── feature/
│   ├── coleta/                   # Feature de coleta
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   ├── sync/                     # Feature de sincronização
│   └── dashboard/                # Feature de dashboard
└── core/
    ├── database/                 # Room Database
    ├── network/                  # Retrofit + API
    ├── common/                   # Utilitários
    └── ui/                       # Componentes UI
```

### Por Quê?

1. **Clean Architecture** - Separação clara, testável
2. **MVVM** - Padrão recomendado pelo Google
3. **Modularização Leve** - Apenas features principais
4. **Offline-First** - Repository pattern facilita

---

## 🛠️ Stack Tecnológico Recomendado (2025)

### Essenciais
```kotlin
// Injeção de Dependência
implementation("com.google.dagger:hilt-android:2.48")

// Coroutines + Flow
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// ViewModel + LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

// Navigation
implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")

// Room Database
implementation("androidx.room:room-runtime:2.6.0")
implementation("androidx.room:room-ktx:2.6.0")

// Retrofit + OkHttp
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Compose (UI Moderna)
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2")
```

---

## 💡 Boas Práticas Modernas

### 1. Single Source of Truth
```kotlin
// Repository é a única fonte de verdade
class PatrimonioRepository(
    private val api: PatrimonioApi,
    private val database: PatrimonioDao
) {
    // Sempre retorna do banco (source of truth)
    fun getPatrimonios(): Flow<List<Patrimonio>> {
        return database.getAll()
            .onStart { refreshFromNetwork() }
    }
    
    private suspend fun refreshFromNetwork() {
        try {
            val patrimonios = api.getPatrimonios()
            database.insertAll(patrimonios)
        } catch (e: Exception) {
            // Falha silenciosa, dados locais continuam disponíveis
        }
    }
}
```

### 2. Offline-First
```kotlin
// Sempre funciona offline
suspend fun registrarColeta(coleta: Coleta) {
    // 1. Salvar localmente
    database.insert(coleta)
    
    // 2. Tentar sincronizar (não bloqueia)
    workManager.enqueue(SyncWorker::class.java)
}
```

### 3. Reactive UI
```kotlin
// UI reage automaticamente a mudanças
viewModel.patrimonios.collectAsState { patrimonios ->
    LazyColumn {
        items(patrimonios) { patrimonio ->
            PatrimonioItem(patrimonio)
        }
    }
}
```

---

## 🚀 Migração Gradual

Se já tem código legado:

1. **Fase 1**: Adicionar ViewModels
2. **Fase 2**: Criar Repository pattern
3. **Fase 3**: Extrair Use Cases
4. **Fase 4**: Modularizar (opcional)

---

**Conclusão**: Para apps modernos em 2025, **Clean Architecture + MVVM** é o padrão ouro, com modularização para apps grandes.

**Versão**: 2.0.0  
**Data**: 08/11/2025
