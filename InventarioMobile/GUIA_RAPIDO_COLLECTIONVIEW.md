# Guia Rápido - CollectionViewActivity (Clean Architecture)

## 🎯 Resumo da Migração

`CollectionViewActivity` foi migrada para Clean Architecture usando:
- ✅ **Use Cases**: `BuscarColetasUseCase`, `ObterUsuarioAtualUseCase`
- ✅ **Repository**: `ColetaRepository` (ao invés de `InventarioRepository`)
- ✅ **ViewModel**: `CollectionViewViewModelClean` com `@HiltViewModel`
- ✅ **State**: `CollectionViewState` (sealed class)
- ✅ **DI**: Hilt com `@AndroidEntryPoint`

---

## 📁 Arquivos Criados/Modificados

### Novos Arquivos
```
domain/usecase/
├── BuscarColetasUseCase.kt          ✨ NOVO
└── ObterUsuarioAtualUseCase.kt      ✨ NOVO

presentation/state/
└── CollectionViewState.kt           ✨ NOVO

presentation/coleta/
└── CollectionViewViewModelClean.kt  ✨ NOVO
```

### Arquivos Modificados
```
presentation/coleta/
└── CollectionViewActivity.kt        ✏️ MIGRADO

data/local/dao/
└── ColetaDao.kt                     ✏️ +buscarTodas()

data/repository/
└── ColetaRepositoryImpl.kt          ✏️ +getColetasLocal()

di/
└── DatabaseModule.kt                ✏️ +LocalDataManager
```

---

## 🚀 Como Usar

### 1. Carregar Coletas

```kotlin
// Activity
viewModel.carregarColetas()

// ViewModel (automático via Use Case)
buscarColetasUseCase().fold(
    onSuccess = { coletas -> /* atualizar estado */ },
    onFailure = { error -> /* mostrar erro */ }
)
```

### 2. Aplicar Filtros

```kotlin
// Filtrar por usuário
viewModel.filtrarPorUsuario(FiltroUsuario.MINHAS)
viewModel.filtrarPorUsuario(FiltroUsuario.TODAS)

// Filtrar por status
viewModel.filtrarPorStatus(FiltroStatus.PENDENTES)
viewModel.filtrarPorStatus(FiltroStatus.SINCRONIZADOS)
viewModel.filtrarPorStatus(FiltroStatus.TODOS)

// Filtrar por sala
viewModel.filtrarPorSala("Sala 101")
viewModel.filtrarPorSala(null) // Todas as salas
```

### 3. Observar Estado

```kotlin
lifecycleScope.launchWhenStarted {
    viewModel.state.collect { state ->
        when (state) {
            is CollectionViewState.Idle -> {
                // Estado inicial
            }
            is CollectionViewState.Loading -> {
                // Mostrando loading
                showLoading()
            }
            is CollectionViewState.Success -> {
                // Dados carregados
                updateUI(state)
            }
            is CollectionViewState.Error -> {
                // Erro ao carregar
                showError(state.message)
            }
        }
    }
}
```

### 4. Buscar por Texto (Opcional)

```kotlin
// Busca em patrimônio, observações e sala
viewModel.buscar("Sala 101")
viewModel.buscar("") // Limpar busca
```

### 5. Remover Coleta (Opcional)

```kotlin
// Remove uma coleta pelo ID
viewModel.removerColeta(coletaId)
```

---

## 📊 Estrutura do Estado

```kotlin
sealed class CollectionViewState {
    object Idle : CollectionViewState()
    object Loading : CollectionViewState()
    
    data class Success(
        val coletas: List<Coleta>,           // Todas as coletas
        val filteredColetas: List<Coleta>,   // Coletas filtradas
        val salas: List<String>,             // Salas únicas
        val totalColetas: Int,               // Total filtrado
        val sincronizadas: Int,              // Sincronizadas filtradas
        val pendentes: Int                   // Pendentes filtradas
    ) : CollectionViewState()
    
    data class Error(val message: String) : CollectionViewState()
}
```

---

## 🔄 Fluxo de Dados

```
1. Activity chama viewModel.carregarColetas()
   ↓
2. ViewModel chama buscarColetasUseCase()
   ↓
3. Use Case chama coletaRepository.getColetasLocal()
   ↓
4. Repository busca do ColetaDao.buscarTodas()
   ↓
5. DAO retorna List<ColetaEntity>
   ↓
6. Mapper converte para List<Coleta>
   ↓
7. ViewModel atualiza state para Success
   ↓
8. Activity observa state e atualiza UI
```

---

## 🎨 Exemplo Completo

```kotlin
@AndroidEntryPoint
class CollectionViewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCollectionViewBinding
    private val viewModel: CollectionViewViewModelClean by viewModels()
    private val adapter = CollectionAdapter()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupFilters()
        observeViewModel()
        
        // Carregar coletas
        viewModel.carregarColetas()
    }
    
    private fun setupFilters() {
        // Filtro de usuário
        binding.chipGroupUser.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAllUsers.id -> 
                        viewModel.filtrarPorUsuario(FiltroUsuario.TODAS)
                    binding.chipMyCollections.id -> 
                        viewModel.filtrarPorUsuario(FiltroUsuario.MINHAS)
                }
            }
        }
        
        // Filtro de status
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAll.id -> 
                        viewModel.filtrarPorStatus(FiltroStatus.TODOS)
                    binding.chipSynced.id -> 
                        viewModel.filtrarPorStatus(FiltroStatus.SINCRONIZADOS)
                    binding.chipPending.id -> 
                        viewModel.filtrarPorStatus(FiltroStatus.PENDENTES)
                }
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is CollectionViewState.Loading -> showLoading()
                    is CollectionViewState.Success -> updateUI(state)
                    is CollectionViewState.Error -> showError(state.message)
                    else -> hideLoading()
                }
            }
        }
    }
    
    private fun updateUI(state: CollectionViewState.Success) {
        hideLoading()
        adapter.submitList(state.filteredColetas)
        binding.tvTotalColetas.text = state.totalColetas.toString()
        binding.tvPendingSync.text = state.pendentes.toString()
        
        if (state.salas.isNotEmpty() && binding.spinnerSalas.adapter == null) {
            setupSalaSpinner(state.salas)
        }
        
        binding.layoutEmptyState.visibility = 
            if (state.filteredColetas.isEmpty()) View.VISIBLE else View.GONE
    }
}
```

---

## ✅ Checklist de Teste

### Funcionalidades Principais (Implementadas e Conectadas)
- [ ] Carregar coletas ao abrir a tela
- [ ] Filtrar por "Todas" e "Minhas Coletas"
- [ ] Filtrar por "Todos", "Sincronizados" e "Pendentes"
- [ ] Filtrar por sala específica
- [ ] Verificar estatísticas (total, sincronizadas, pendentes)
- [ ] Verificar estado vazio quando não há coletas
- [ ] Verificar loading durante carregamento
- [ ] Verificar mensagem de erro em caso de falha

### Funcionalidades Extras (Implementadas mas Não Conectadas na UI)
- [ ] Busca por texto (precisa adicionar SearchView na UI)
- [ ] Remoção de coleta (precisa adicionar swipe ou botão na UI)

---

## 🐛 Troubleshooting

### Coletas não aparecem
- Verificar se há coletas no banco Room
- Verificar logs: `adb logcat | grep CollectionViewVMClean`
- Verificar se `ColetaDao.buscarTodas()` retorna dados

### Filtro "Minhas Coletas" não funciona
- Verificar se `usuarioAtualId` está sendo obtido
- Verificar se `Coleta.usuarioId` corresponde ao ID do usuário
- Verificar logs de filtros

### Estatísticas incorretas
- Verificar se filtros estão sendo aplicados corretamente
- Verificar cálculo de `sincronizadas` e `pendentes`

---

## 📚 Referências

- [MIGRACAO_COLLECTIONVIEW_CLEAN.md](./MIGRACAO_COLLECTIONVIEW_CLEAN.md) - Documentação completa
- [android-clean-migration-status.md](../.kiro/steering/android-clean-migration-status.md) - Status geral
- [clean-architecture.md](../.kiro/steering/clean-architecture.md) - Diretrizes

---

**Data**: 14/11/2025  
**Versão**: 1.0.0
