# 📋 Planejamento - Tela de Patrimônios por Sala

## 🎯 Objetivo

Criar uma tela onde o usuário:
1. Seleciona uma sala
2. Vê lista de patrimônios daquela sala
3. Filtra por: Coletados / Não Coletados / Todos
4. Pode coletar diretamente da lista

---

## 🏗️ Arquitetura (Clean Architecture + MVVM)

### Camadas

```
┌─────────────────────────────────────────────────────────┐
│                    UI (Activity)                         │
│  PatrimoniosPorSalaActivity                             │
│  - Seleção de sala (Spinner/AutoComplete)              │
│  - Filtros (Chips: Todos/Coletados/Não Coletados)      │
│  - RecyclerView com lista de patrimônios                │
│  - Botão "Coletar" em cada item                         │
└──────────────────────┬──────────────────────────────────┘
                       │ observa StateFlow
                       ▼
┌─────────────────────────────────────────────────────────┐
│              ViewModel (Presentation)                    │
│  PatrimoniosPorSalaViewModel                            │
│  - State: PatrimoniosPorSalaState                       │
│  - Métodos: carregarSalas(), selecionarSala(),          │
│             filtrar(), coletar()                         │
└──────────────────────┬──────────────────────────────────┘
                       │ chama Use Cases
                       ▼
┌─────────────────────────────────────────────────────────┐
│                Use Cases (Domain)                        │
│  - BuscarPatrimoniosPorSalaUseCase                      │
│  - BuscarSalasComEstatisticasUseCase                    │
│  - RegistrarColetaUseCase (já existe)                   │
└──────────────────────┬──────────────────────────────────┘
                       │ usa Repository
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Repository (Data)                           │
│  - PatrimonioRepository (já existe)                     │
│  - SalaRepository (já existe)                           │
│  - ColetaRepository (já existe)                         │
└──────────────────────┬──────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Data Sources                                │
│  - Room Database (Local)                                │
│  - Retrofit APIs (Remote)                               │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Componentes a Criar

### 1. Domain Layer

#### Models
```kotlin
// domain/model/PatrimonioPorSala.kt
data class PatrimonioPorSala(
    val id: Int,
    val numero: String,
    val descricao: String,
    val sala: String,
    val responsavel: String?,
    val coletado: Boolean,
    val dataColeta: Long?,
    val coletadoPor: String?
)

// domain/model/SalaComEstatisticas.kt
data class SalaComEstatisticas(
    val id: Int,
    val nome: String,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualColetado: Float
)
```

#### Use Cases
```kotlin
// domain/usecase/BuscarPatrimoniosPorSalaUseCase.kt
class BuscarPatrimoniosPorSalaUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(
        salaId: Int,
        filtro: FiltroColeta = FiltroColeta.TODOS
    ): Result<List<PatrimonioPorSala>>
}

enum class FiltroColeta {
    TODOS,
    COLETADOS,
    NAO_COLETADOS
}

// domain/usecase/BuscarSalasComEstatisticasUseCase.kt
class BuscarSalasComEstatisticasUseCase @Inject constructor(
    private val salaRepository: SalaRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(): Result<List<SalaComEstatisticas>>
}
```

### 2. Data Layer

#### DAO (Adicionar métodos)
```kotlin
// data/local/dao/PatrimonioDao.kt
@Dao
interface PatrimonioDao {
    // Já existentes...
    
    // NOVOS MÉTODOS
    @Query("""
        SELECT * FROM patrimonio 
        WHERE idSala = :salaId 
        ORDER BY coletado ASC, numero ASC
    """)
    suspend fun buscarPorSala(salaId: Int): List<PatrimonioEntity>
    
    @Query("""
        SELECT * FROM patrimonio 
        WHERE idSala = :salaId AND coletado = :coletado
        ORDER BY numero ASC
    """)
    suspend fun buscarPorSalaComFiltro(
        salaId: Int, 
        coletado: Boolean
    ): List<PatrimonioEntity>
    
    @Query("""
        SELECT COUNT(*) FROM patrimonio 
        WHERE idSala = :salaId
    """)
    suspend fun contarPorSala(salaId: Int): Int
    
    @Query("""
        SELECT COUNT(*) FROM patrimonio 
        WHERE idSala = :salaId AND coletado = 1
    """)
    suspend fun contarColetadosPorSala(salaId: Int): Int
}

// data/local/dao/SalaDao.kt
@Dao
interface SalaDao {
    // Já existentes...
    
    // NOVO MÉTODO
    @Query("""
        SELECT s.*, 
               COUNT(p.id) as totalPatrimonios,
               SUM(CASE WHEN p.coletado = 1 THEN 1 ELSE 0 END) as coletados
        FROM sala s
        LEFT JOIN patrimonio p ON s.id = p.idSala
        GROUP BY s.id
        ORDER BY s.nome
    """)
    suspend fun buscarSalasComEstatisticas(): List<SalaComEstatisticasEntity>
}
```

#### Repository Implementation
```kotlin
// data/repository/PatrimonioRepositoryImpl.kt
// Adicionar métodos:

override suspend fun buscarPorSala(
    salaId: Int,
    filtro: FiltroColeta
): Result<List<PatrimonioPorSala>> {
    return try {
        val entities = when (filtro) {
            FiltroColeta.TODOS -> 
                patrimonioDao.buscarPorSala(salaId)
            FiltroColeta.COLETADOS -> 
                patrimonioDao.buscarPorSalaComFiltro(salaId, true)
            FiltroColeta.NAO_COLETADOS -> 
                patrimonioDao.buscarPorSalaComFiltro(salaId, false)
        }
        
        val patrimonios = entities.map { mapper.toDomain(it) }
        Result.success(patrimonios)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 3. Presentation Layer

#### State
```kotlin
// presentation/state/PatrimoniosPorSalaState.kt
sealed class PatrimoniosPorSalaState {
    object Idle : PatrimoniosPorSalaState()
    object Loading : PatrimoniosPorSalaState()
    
    data class SalasCarregadas(
        val salas: List<SalaComEstatisticas>
    ) : PatrimoniosPorSalaState()
    
    data class PatrimoniosCarregados(
        val sala: SalaComEstatisticas,
        val patrimonios: List<PatrimonioPorSala>,
        val filtroAtual: FiltroColeta
    ) : PatrimoniosPorSalaState()
    
    data class ColetaRealizada(
        val patrimonio: PatrimonioPorSala
    ) : PatrimoniosPorSalaState()
    
    data class Error(
        val message: String
    ) : PatrimoniosPorSalaState()
}
```

#### ViewModel
```kotlin
// presentation/patrimonio/PatrimoniosPorSalaViewModel.kt
@HiltViewModel
class PatrimoniosPorSalaViewModel @Inject constructor(
    private val buscarSalasComEstatisticasUseCase: BuscarSalasComEstatisticasUseCase,
    private val buscarPatrimoniosPorSalaUseCase: BuscarPatrimoniosPorSalaUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<PatrimoniosPorSalaState>(
        PatrimoniosPorSalaState.Idle
    )
    val state: StateFlow<PatrimoniosPorSalaState> = _state.asStateFlow()
    
    private var salaAtual: SalaComEstatisticas? = null
    private var filtroAtual: FiltroColeta = FiltroColeta.TODOS
    
    fun carregarSalas() {
        viewModelScope.launch {
            _state.value = PatrimoniosPorSalaState.Loading
            
            buscarSalasComEstatisticasUseCase().fold(
                onSuccess = { salas ->
                    _state.value = PatrimoniosPorSalaState.SalasCarregadas(salas)
                },
                onFailure = { error ->
                    _state.value = PatrimoniosPorSalaState.Error(
                        error.message ?: "Erro ao carregar salas"
                    )
                }
            )
        }
    }
    
    fun selecionarSala(sala: SalaComEstatisticas) {
        salaAtual = sala
        carregarPatrimonios()
    }
    
    fun aplicarFiltro(filtro: FiltroColeta) {
        filtroAtual = filtro
        carregarPatrimonios()
    }
    
    private fun carregarPatrimonios() {
        val sala = salaAtual ?: return
        
        viewModelScope.launch {
            _state.value = PatrimoniosPorSalaState.Loading
            
            buscarPatrimoniosPorSalaUseCase(sala.id, filtroAtual).fold(
                onSuccess = { patrimonios ->
                    _state.value = PatrimoniosPorSalaState.PatrimoniosCarregados(
                        sala = sala,
                        patrimonios = patrimonios,
                        filtroAtual = filtroAtual
                    )
                },
                onFailure = { error ->
                    _state.value = PatrimoniosPorSalaState.Error(
                        error.message ?: "Erro ao carregar patrimônios"
                    )
                }
            )
        }
    }
    
    fun coletar(patrimonio: PatrimonioPorSala) {
        viewModelScope.launch {
            // Criar coleta
            val coleta = Coleta(
                patrimonioId = patrimonio.id.toLong(),
                numeroPatrimonio = patrimonio.numero,
                // ... outros campos
            )
            
            registrarColetaUseCase(coleta).fold(
                onSuccess = {
                    _state.value = PatrimoniosPorSalaState.ColetaRealizada(patrimonio)
                    // Recarregar lista
                    carregarPatrimonios()
                },
                onFailure = { error ->
                    _state.value = PatrimoniosPorSalaState.Error(
                        error.message ?: "Erro ao coletar"
                    )
                }
            )
        }
    }
}
```

#### Activity
```kotlin
// presentation/patrimonio/PatrimoniosPorSalaActivity.kt
@AndroidEntryPoint
class PatrimoniosPorSalaActivity : AppCompatActivity() {
    
    private val viewModel: PatrimoniosPorSalaViewModel by viewModels()
    private lateinit var binding: ActivityPatrimoniosPorSalaBinding
    private lateinit var adapter: PatrimoniosPorSalaAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatrimoniosPorSalaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupObservers()
        
        viewModel.carregarSalas()
    }
    
    private fun setupUI() {
        // Configurar RecyclerView
        adapter = PatrimoniosPorSalaAdapter(
            onColetarClick = { patrimonio ->
                viewModel.coletar(patrimonio)
            }
        )
        binding.recyclerView.adapter = adapter
        
        // Configurar filtros
        binding.chipTodos.setOnClickListener {
            viewModel.aplicarFiltro(FiltroColeta.TODOS)
        }
        binding.chipColetados.setOnClickListener {
            viewModel.aplicarFiltro(FiltroColeta.COLETADOS)
        }
        binding.chipNaoColetados.setOnClickListener {
            viewModel.aplicarFiltro(FiltroColeta.NAO_COLETADOS)
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PatrimoniosPorSalaState.Idle -> {
                        // Estado inicial
                    }
                    is PatrimoniosPorSalaState.Loading -> {
                        showLoading()
                    }
                    is PatrimoniosPorSalaState.SalasCarregadas -> {
                        hideLoading()
                        setupSalaSelector(state.salas)
                    }
                    is PatrimoniosPorSalaState.PatrimoniosCarregados -> {
                        hideLoading()
                        updateUI(state)
                    }
                    is PatrimoniosPorSalaState.ColetaRealizada -> {
                        showSuccess("Patrimônio coletado com sucesso")
                    }
                    is PatrimoniosPorSalaState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
    }
}
```

---

## 📱 Layout (XML)

### activity_patrimonios_por_sala.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- AppBar -->
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="Patrimônios por Sala" />
        
    </com.google.android.material.appbar.AppBarLayout>
    
    <!-- Conteúdo -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            
            <!-- Seletor de Sala -->
            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/salaInputLayout"
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Selecione uma sala">
                
                <AutoCompleteTextView
                    android:id="@+id/salaAutoComplete"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="none" />
                
            </com.google.android.material.textfield.TextInputLayout>
            
            <!-- Estatísticas da Sala -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardEstatisticas"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:visibility="gone">
                
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:padding="16dp">
                    
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:gravity="center">
                        
                        <TextView
                            android:id="@+id/tvTotal"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="0"
                            android:textSize="24sp"
                            android:textStyle="bold" />
                        
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Total"
                            android:textSize="12sp" />
                        
                    </LinearLayout>
                    
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:gravity="center">
                        
                        <TextView
                            android:id="@+id/tvColetados"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="0"
                            android:textSize="24sp"
                            android:textStyle="bold"
                            android:textColor="@color/green" />
                        
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Coletados"
                            android:textSize="12sp" />
                        
                    </LinearLayout>
                    
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:gravity="center">
                        
                        <TextView
                            android:id="@+id/tvPendentes"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="0"
                            android:textSize="24sp"
                            android:textStyle="bold"
                            android:textColor="@color/orange" />
                        
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Pendentes"
                            android:textSize="12sp" />
                        
                    </LinearLayout>
                    
                </LinearLayout>
                
            </com.google.android.material.card.MaterialCardView>
            
            <!-- Filtros -->
            <com.google.android.material.chip.ChipGroup
                android:id="@+id/chipGroupFiltros"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                app:singleSelection="true"
                app:selectionRequired="true">
                
                <com.google.android.material.chip.Chip
                    android:id="@+id/chipTodos"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Todos"
                    android:checked="true" />
                
                <com.google.android.material.chip.Chip
                    android:id="@+id/chipColetados"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Coletados" />
                
                <com.google.android.material.chip.Chip
                    android:id="@+id/chipNaoColetados"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Não Coletados" />
                
            </com.google.android.material.chip.ChipGroup>
            
            <!-- Lista de Patrimônios -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerView"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
            
        </LinearLayout>
        
    </androidx.core.widget.NestedScrollView>
    
    <!-- Loading -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />
    
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

---

## 🔄 Fluxo de Dados

```
1. Usuário abre tela
   ↓
2. ViewModel.carregarSalas()
   ↓
3. BuscarSalasComEstatisticasUseCase
   ↓
4. SalaRepository + PatrimonioRepository
   ↓
5. Room Database (local) ou API (remoto)
   ↓
6. State.SalasCarregadas → UI atualiza dropdown
   ↓
7. Usuário seleciona sala
   ↓
8. ViewModel.selecionarSala(sala)
   ↓
9. BuscarPatrimoniosPorSalaUseCase
   ↓
10. PatrimonioRepository.buscarPorSala()
   ↓
11. Room Database (filtrado por sala)
   ↓
12. State.PatrimoniosCarregados → UI atualiza lista
   ↓
13. Usuário clica "Coletar"
   ↓
14. ViewModel.coletar(patrimonio)
   ↓
15. RegistrarColetaUseCase (já existe)
   ↓
16. ColetaRepository.registrarColeta()
   ↓
17. Salva local + tenta sync
   ↓
18. State.ColetaRealizada → UI mostra sucesso
   ↓
19. Recarrega lista atualizada
```

---

## ⚠️ Riscos e Mitigações

### Risco 1: Performance com Muitos Patrimônios
**Problema:** Sala com 1000+ patrimônios pode travar UI

**Mitigação:**
- Usar Paging 3 para paginação
- Lazy loading no RecyclerView
- Índices no banco de dados

### Risco 2: Sincronização Inconsistente
**Problema:** Dados locais desatualizados

**Mitigação:**
- Sempre buscar do banco local (offline-first)
- Botão "Atualizar" para forçar sync
- Indicador de última atualização

### Risco 3: Memória
**Problema:** Carregar todas as salas com estatísticas

**Mitigação:**
- Calcular estatísticas sob demanda
- Cache em memória com LRU
- Limpar cache ao sair da tela

### Risco 4: Conflitos de Estado
**Problema:** Múltiplas ações simultâneas

**Mitigação:**
- Usar StateFlow (thread-safe)
- Desabilitar botões durante loading
- Cancelar operações anteriores

---

## 📋 Checklist de Implementação

### Fase 1: Domain Layer (Sem risco)
- [ ] Criar models: `PatrimonioPorSala`, `SalaComEstatisticas`
- [ ] Criar enum: `FiltroColeta`
- [ ] Criar Use Cases: `BuscarPatrimoniosPorSalaUseCase`, `BuscarSalasComEstatisticasUseCase`
- [ ] Testes unitários dos Use Cases

### Fase 2: Data Layer (Baixo risco)
- [ ] Adicionar métodos no `PatrimonioDao`
- [ ] Adicionar métodos no `SalaDao`
- [ ] Atualizar `PatrimonioRepositoryImpl`
- [ ] Atualizar `SalaRepositoryImpl`
- [ ] Testes de integração dos DAOs

### Fase 3: Presentation Layer (Médio risco)
- [ ] Criar `PatrimoniosPorSalaState`
- [ ] Criar `PatrimoniosPorSalaViewModel`
- [ ] Testes unitários do ViewModel

### Fase 4: UI (Alto risco - testar bem)
- [ ] Criar layouts XML
- [ ] Criar `PatrimoniosPorSalaActivity`
- [ ] Criar `PatrimoniosPorSalaAdapter`
- [ ] Adicionar no AndroidManifest
- [ ] Adicionar no menu principal
- [ ] Testes de UI

### Fase 5: Integração e Testes
- [ ] Testar com 10 patrimônios
- [ ] Testar com 100 patrimônios
- [ ] Testar com 1000+ patrimônios
- [ ] Testar filtros
- [ ] Testar coleta
- [ ] Testar offline
- [ ] Testar sincronização

---

## 🧪 Plano de Testes

### Teste 1: Carregar Salas
```
1. Abrir tela
2. Verificar: Dropdown mostra todas as salas
3. Verificar: Estatísticas corretas (total, coletados, pendentes)
```

### Teste 2: Selecionar Sala
```
1. Selecionar sala com 50 patrimônios
2. Verificar: Lista carrega corretamente
3. Verificar: Estatísticas da sala aparecem
```

### Teste 3: Filtros
```
1. Selecionar sala
2. Clicar "Coletados"
3. Verificar: Apenas coletados aparecem
4. Clicar "Não Coletados"
5. Verificar: Apenas não coletados aparecem
6. Clicar "Todos"
7. Verificar: Todos aparecem
```

### Teste 4: Coletar
```
1. Selecionar sala
2. Filtrar "Não Coletados"
3. Clicar "Coletar" em um item
4. Verificar: Item some da lista
5. Verificar: Estatísticas atualizadas
6. Filtrar "Coletados"
7. Verificar: Item aparece na lista
```

### Teste 5: Performance
```
1. Selecionar sala com 1000+ patrimônios
2. Verificar: Carrega em < 2s
3. Verificar: Scroll suave
4. Verificar: Sem travamentos
```

---

## 📊 Estimativa de Tempo

| Fase | Tempo Estimado | Risco |
|------|----------------|-------|
| Fase 1: Domain | 2h | Baixo |
| Fase 2: Data | 3h | Baixo |
| Fase 3: Presentation | 2h | Médio |
| Fase 4: UI | 4h | Alto |
| Fase 5: Testes | 3h | Médio |
| **TOTAL** | **14h** | - |

---

## ✅ Critérios de Aceitação

- [ ] Usuário consegue selecionar sala
- [ ] Lista mostra patrimônios da sala
- [ ] Filtros funcionam corretamente
- [ ] Estatísticas estão corretas
- [ ] Coleta funciona da lista
- [ ] Performance aceitável (< 2s)
- [ ] Funciona offline
- [ ] Sincroniza corretamente
- [ ] Sem crashes
- [ ] Sem memory leaks

---

**Versão:** 1.0.0  
**Data:** 18/11/2025  
**Status:** 📋 PLANEJAMENTO COMPLETO  
**Próximo Passo:** Aprovação para iniciar Fase 1
