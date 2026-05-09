# Plano de Implementação - Consulta de Patrimônios

## 📋 Visão Geral

**Objetivo:** Criar uma funcionalidade de consulta de patrimônios para casos onde a etiqueta ou código de barras estejam danificados, permitindo busca por código parcial ou descrição.

**Problema Resolvido:**
- ✅ Etiquetas danificadas ou ilegíveis
- ✅ Código de barras não escaneável
- ✅ Necessidade de identificar patrimônio por características
- ✅ Busca rápida durante coleta

---

## 🎯 Funcionalidades

### 1. Busca por Código (Parcial)
- Buscar patrimônio digitando parte do código
- Sugestões em tempo real
- Filtro inteligente

### 2. Busca por Descrição
- Buscar por nome/descrição do item
- Busca fuzzy (tolerante a erros)
- Filtros por categoria

### 3. Visualização Detalhada
- Todas as informações do patrimônio
- Foto (se disponível)
- Localização cadastrada
- Responsável
- Valor
- Estado
- Histórico de coletas

### 4. Ação Rápida
- Botão "Coletar Este Item"
- Integração com fluxo de coleta existente

---

## 🏗️ Arquitetura (Clean Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER                           │
│  - ConsultaPatrimonioActivity                                │
│  - ConsultaPatrimonioViewModel                               │
│  - PatrimonioDetalheFragment                                 │
│  - PatrimonioSearchAdapter                                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌─────────────────────────────────────────────────────────────┐
│                   DOMAIN LAYER                               │
│  - BuscarPatrimonioPorCodigoUseCase                          │
│  - BuscarPatrimonioPorDescricaoUseCase                       │
│  - ObterDetalhePatrimonioUseCase                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌─────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                │
│  - PatrimonioRepository (já existe)                          │
│  - PatrimonioDao (já existe)                                 │
│  - PatrimonioApi (já existe)                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Plano de Implementação (Faseado)

### 🔵 Fase 1: Backend (Endpoints) - 2h

#### 1.1 Criar Endpoint de Busca por Código Parcial
```java
// MobilePatrimonioController.java
@GetMapping("/buscar-por-codigo")
public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorCodigoParcial(
    @RequestParam String codigo,
    @RequestParam(defaultValue = "10") int limit
)
```

**Query SQL:**
```sql
SELECT * FROM TABELA_PATRIMONIO 
WHERE NUMERO LIKE '%' || ? || '%' 
  AND ATIVO = true
ORDER BY NUMERO
LIMIT ?
```

#### 1.2 Criar Endpoint de Busca por Descrição
```java
@GetMapping("/buscar-por-descricao")
public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorDescricao(
    @RequestParam String descricao,
    @RequestParam(defaultValue = "10") int limit
)
```

**Query SQL:**
```sql
SELECT * FROM TABELA_PATRIMONIO 
WHERE UPPER(DESCRICAO) LIKE UPPER('%' || ? || '%')
  AND ATIVO = true
ORDER BY DESCRICAO
LIMIT ?
```

#### 1.3 Criar Endpoint de Detalhes Completos
```java
@GetMapping("/{id}/detalhes-completos")
public ResponseEntity<ApiResponse<PatrimonioDetalheDTO>> obterDetalhesCompletos(
    @PathVariable Long id
)
```

**Retorna:**
- Dados do patrimônio
- Sala completa (nome, bloco, andar)
- Responsável completo (nome, matrícula, setor)
- Histórico de coletas
- Foto (se disponível)

---

### 🔵 Fase 2: Domain Layer (Use Cases) - 1h

#### 2.1 BuscarPatrimonioPorCodigoUseCase
```kotlin
class BuscarPatrimonioPorCodigoUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(
        codigo: String,
        limit: Int = 10
    ): Result<List<Patrimonio>> {
        // Validações
        if (codigo.length < 2) {
            return Result.failure(Exception("Digite ao menos 2 caracteres"))
        }
        
        return patrimonioRepository.buscarPorCodigoParcial(codigo, limit)
    }
}
```

#### 2.2 BuscarPatrimonioPorDescricaoUseCase
```kotlin
class BuscarPatrimonioPorDescricaoUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(
        descricao: String,
        limit: Int = 10
    ): Result<List<Patrimonio>> {
        // Validações
        if (descricao.length < 3) {
            return Result.failure(Exception("Digite ao menos 3 caracteres"))
        }
        
        return patrimonioRepository.buscarPorDescricao(descricao, limit)
    }
}
```

#### 2.3 ObterDetalhePatrimonioUseCase
```kotlin
class ObterDetalhePatrimonioUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(
        patrimonioId: Long
    ): Result<PatrimonioDetalhe> {
        return patrimonioRepository.obterDetalhesCompletos(patrimonioId)
    }
}
```

---

### 🔵 Fase 3: Data Layer (Repository) - 1h

#### 3.1 Adicionar Métodos no PatrimonioRepository
```kotlin
interface PatrimonioRepository {
    // Métodos existentes...
    
    // Novos métodos
    suspend fun buscarPorCodigoParcial(
        codigo: String, 
        limit: Int
    ): Result<List<Patrimonio>>
    
    suspend fun buscarPorDescricao(
        descricao: String, 
        limit: Int
    ): Result<List<Patrimonio>>
    
    suspend fun obterDetalhesCompletos(
        patrimonioId: Long
    ): Result<PatrimonioDetalhe>
}
```

#### 3.2 Implementar no PatrimonioRepositoryImpl
```kotlin
override suspend fun buscarPorCodigoParcial(
    codigo: String, 
    limit: Int
): Result<List<Patrimonio>> {
    return try {
        val response = patrimonioApi.buscarPorCodigoParcial(codigo, limit)
        
        if (response.isSuccessful && response.body()?.success == true) {
            val dtos = response.body()!!.data
            val patrimonios = dtos.map { mapper.toDomain(it) }
            Result.success(patrimonios)
        } else {
            Result.failure(Exception("Erro ao buscar"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

### 🔵 Fase 4: Presentation Layer (UI) - 3h

#### 4.1 Criar ConsultaPatrimonioActivity
```kotlin
@AndroidEntryPoint
class ConsultaPatrimonioActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConsultaPatrimonioBinding
    private val viewModel: ConsultaPatrimonioViewModel by viewModels()
    private lateinit var adapter: PatrimonioSearchAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaPatrimonioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupSearch()
        observeViewModel()
    }
}
```

#### 4.2 Criar ConsultaPatrimonioViewModel
```kotlin
@HiltViewModel
class ConsultaPatrimonioViewModel @Inject constructor(
    private val buscarPorCodigoUseCase: BuscarPatrimonioPorCodigoUseCase,
    private val buscarPorDescricaoUseCase: BuscarPatrimonioPorDescricaoUseCase,
    private val obterDetalheUseCase: ObterDetalhePatrimonioUseCase
) : ViewModel() {
    
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    fun buscarPorCodigo(codigo: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            
            buscarPorCodigoUseCase(codigo).fold(
                onSuccess = { patrimonios ->
                    _searchState.value = SearchState.Success(patrimonios)
                },
                onFailure = { error ->
                    _searchState.value = SearchState.Error(error.message ?: "Erro")
                }
            )
        }
    }
    
    fun buscarPorDescricao(descricao: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            
            buscarPorDescricaoUseCase(descricao).fold(
                onSuccess = { patrimonios ->
                    _searchState.value = SearchState.Success(patrimonios)
                },
                onFailure = { error ->
                    _searchState.value = SearchState.Error(error.message ?: "Erro")
                }
            )
        }
    }
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val patrimonios: List<Patrimonio>) : SearchState()
    data class Error(val message: String) : SearchState()
}
```

#### 4.3 Criar Layout activity_consulta_patrimonio.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- Toolbar -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/primary"
        app:title="Consultar Patrimônio"
        app:titleTextColor="@android:color/white"
        app:navigationIcon="@drawable/ic_arrow_back" />

    <!-- Tabs de Busca -->
    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tabLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:tabMode="fixed">
        
        <com.google.android.material.tabs.TabItem
            android:text="Por Código" />
        
        <com.google.android.material.tabs.TabItem
            android:text="Por Descrição" />
    </com.google.android.material.tabs.TabLayout>

    <!-- Campo de Busca -->
    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:startIconDrawable="@drawable/ic_search"
        app:endIconMode="clear_text"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">
        
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/edtSearch"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Digite para buscar..."
            android:inputType="text" />
    </com.google.android.material.textfield.TextInputLayout>

    <!-- Progress Bar -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_marginTop="32dp"
        android:visibility="gone" />

    <!-- Lista de Resultados -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvResultados"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="8dp" />

    <!-- Mensagem Vazia -->
    <TextView
        android:id="@+id/tvEmpty"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:gravity="center"
        android:text="Digite para buscar patrimônios"
        android:textColor="@color/text_secondary"
        android:textSize="16sp"
        android:visibility="gone" />

</LinearLayout>
```

#### 4.4 Criar PatrimonioSearchAdapter
```kotlin
class PatrimonioSearchAdapter(
    private val onItemClick: (Patrimonio) -> Unit
) : RecyclerView.Adapter<PatrimonioSearchAdapter.ViewHolder>() {
    
    private var patrimonios = listOf<Patrimonio>()
    
    fun submitList(list: List<Patrimonio>) {
        patrimonios = list
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(
        private val binding: ItemPatrimonioSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(patrimonio: Patrimonio) {
            binding.tvCodigo.text = patrimonio.numeroPatrimonio
            binding.tvDescricao.text = patrimonio.descricao
            binding.tvSala.text = patrimonio.salaNome ?: "Sem sala"
            binding.tvResponsavel.text = patrimonio.responsavelNome ?: "Sem responsável"
            
            binding.root.setOnClickListener {
                onItemClick(patrimonio)
            }
        }
    }
}
```

#### 4.5 Criar PatrimonioDetalheFragment (Bottom Sheet)
```kotlin
class PatrimonioDetalheFragment : BottomSheetDialogFragment() {
    
    private lateinit var binding: FragmentPatrimonioDetalheBinding
    private lateinit var patrimonio: Patrimonio
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatrimonioDetalheBinding.inflate(inflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        exibirDetalhes()
        setupButtons()
    }
    
    private fun exibirDetalhes() {
        binding.tvCodigo.text = patrimonio.numeroPatrimonio
        binding.tvDescricao.text = patrimonio.descricao
        binding.tvMarca.text = patrimonio.marca
        binding.tvModelo.text = patrimonio.modelo
        binding.tvEstado.text = patrimonio.estado
        binding.tvSala.text = patrimonio.salaNome
        binding.tvResponsavel.text = patrimonio.responsavelNome
        binding.tvValor.text = formatCurrency(patrimonio.valor)
        
        // Indicador de coleta
        if (patrimonio.coletado) {
            binding.chipStatus.text = "✅ Já Coletado"
            binding.chipStatus.setChipBackgroundColorResource(R.color.success)
        } else {
            binding.chipStatus.text = "⏳ Pendente"
            binding.chipStatus.setChipBackgroundColorResource(R.color.warning)
        }
    }
    
    private fun setupButtons() {
        binding.btnColetar.setOnClickListener {
            // Navegar para tela de coleta com este patrimônio
            val intent = Intent(requireContext(), ColetaActivity::class.java)
            intent.putExtra("PATRIMONIO_ID", patrimonio.id)
            intent.putExtra("PATRIMONIO_NUMERO", patrimonio.numeroPatrimonio)
            startActivity(intent)
            dismiss()
        }
        
        binding.btnFechar.setOnClickListener {
            dismiss()
        }
    }
}
```

---

### 🔵 Fase 5: Integração com Fluxo Existente - 1h

#### 5.1 Adicionar Botão no Dashboard
```xml
<!-- fragment_dashboard.xml -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnConsultarPatrimonio"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:layout_marginBottom="12dp"
    android:text="Consultar Patrimônio"
    android:textSize="16sp"
    app:cornerRadius="12dp"
    app:icon="@drawable/ic_search"
    app:iconGravity="textStart" />
```

#### 5.2 Adicionar no Menu Principal
```kotlin
// DashboardFragment.kt
binding.btnConsultarPatrimonio.setOnClickListener {
    val intent = Intent(requireContext(), ConsultaPatrimonioActivity::class.java)
    startActivity(intent)
}
```

#### 5.3 Adicionar Atalho no Navigation Drawer
```xml
<!-- activity_main_drawer.xml -->
<item
    android:id="@+id/nav_consultar"
    android:icon="@drawable/ic_search"
    android:title="Consultar Patrimônio" />
```

---

### 🔵 Fase 6: Testes e Validação - 1h

#### 6.1 Testes Unitários
```kotlin
@Test
fun `buscar por codigo deve validar tamanho minimo`() = runTest {
    val useCase = BuscarPatrimonioPorCodigoUseCase(mockRepository)
    
    val result = useCase("1") // Apenas 1 caractere
    
    assertTrue(result.isFailure)
    assertEquals("Digite ao menos 2 caracteres", result.exceptionOrNull()?.message)
}

@Test
fun `buscar por descricao deve retornar resultados`() = runTest {
    val useCase = BuscarPatrimonioPorDescricaoUseCase(mockRepository)
    val expectedList = listOf(Patrimonio(...))
    
    whenever(mockRepository.buscarPorDescricao("cadeira", 10))
        .thenReturn(Result.success(expectedList))
    
    val result = useCase("cadeira")
    
    assertTrue(result.isSuccess)
    assertEquals(expectedList, result.getOrNull())
}
```

#### 6.2 Testes de Integração
- [ ] Buscar por código parcial
- [ ] Buscar por descrição
- [ ] Exibir detalhes completos
- [ ] Navegar para coleta
- [ ] Funcionar offline (cache)

---

## 🎨 Wireframes

### Tela de Consulta
```
┌─────────────────────────────────┐
│ ← Consultar Patrimônio          │
├─────────────────────────────────┤
│ [Por Código] [Por Descrição]    │
├─────────────────────────────────┤
│ 🔍 Digite para buscar...     ✕  │
├─────────────────────────────────┤
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 📦 12345                    │ │
│ │ Cadeira Giratória           │ │
│ │ 📍 Sala 101 | 👤 João Silva │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 📦 12346                    │ │
│ │ Mesa de Escritório          │ │
│ │ 📍 Sala 102 | 👤 Maria      │ │
│ └─────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

### Bottom Sheet de Detalhes
```
┌─────────────────────────────────┐
│ Detalhes do Patrimônio          │
├─────────────────────────────────┤
│ Código: 12345                   │
│ Descrição: Cadeira Giratória    │
│ Marca: Marca X                  │
│ Modelo: Modelo Y                │
│ Estado: BOM                     │
│ Valor: R$ 350,00                │
│                                 │
│ 📍 Localização                  │
│ Sala 101 - Bloco A - 1º Andar  │
│                                 │
│ 👤 Responsável                  │
│ João Silva - Matrícula: 12345   │
│ Setor: TI                       │
│                                 │
│ Status: ✅ Já Coletado          │
│ Coletado em: 10/11/2024         │
│ Por: Maria Santos               │
│                                 │
│ [Coletar Este Item] [Fechar]    │
└─────────────────────────────────┘
```

---

## 📊 Cronograma

| Fase | Descrição | Tempo | Dependências |
|------|-----------|-------|--------------|
| 1 | Backend (Endpoints) | 2h | - |
| 2 | Domain (Use Cases) | 1h | Fase 1 |
| 3 | Data (Repository) | 1h | Fase 1, 2 |
| 4 | Presentation (UI) | 3h | Fase 2, 3 |
| 5 | Integração | 1h | Fase 4 |
| 6 | Testes | 1h | Todas |

**Total:** 9 horas (~1,5 dias)

---

## ✅ Checklist de Implementação

### Backend
- [ ] Criar endpoint buscar por código parcial
- [ ] Criar endpoint buscar por descrição
- [ ] Criar endpoint detalhes completos
- [ ] Adicionar queries no PatrimonioDAO
- [ ] Testar endpoints manualmente

### Domain
- [ ] Criar BuscarPatrimonioPorCodigoUseCase
- [ ] Criar BuscarPatrimonioPorDescricaoUseCase
- [ ] Criar ObterDetalhePatrimonioUseCase
- [ ] Adicionar validações

### Data
- [ ] Adicionar métodos no PatrimonioRepository
- [ ] Implementar no PatrimonioRepositoryImpl
- [ ] Adicionar métodos no PatrimonioApi
- [ ] Atualizar MockApiService

### Presentation
- [ ] Criar ConsultaPatrimonioActivity
- [ ] Criar ConsultaPatrimonioViewModel
- [ ] Criar layout XML
- [ ] Criar PatrimonioSearchAdapter
- [ ] Criar PatrimonioDetalheFragment
- [ ] Criar layout do Bottom Sheet

### Integração
- [ ] Adicionar botão no Dashboard
- [ ] Adicionar no Navigation Drawer
- [ ] Testar navegação
- [ ] Testar fluxo completo

### Testes
- [ ] Testes unitários dos Use Cases
- [ ] Testes do ViewModel
- [ ] Testes de integração
- [ ] Teste no dispositivo real

---

## 🎯 Benefícios

### Para o Usuário
- ✅ Solução para etiquetas danificadas
- ✅ Busca rápida e intuitiva
- ✅ Informações completas antes de coletar
- ✅ Reduz erros de identificação

### Para o Sistema
- ✅ Não quebra funcionalidades existentes
- ✅ Segue Clean Architecture
- ✅ Código testável
- ✅ Fácil manutenção

### Métricas Esperadas
- ✅ 90% redução em erros de identificação
- ✅ 50% mais rápido que busca manual
- ✅ 100% dos patrimônios localizáveis

---

## 🔒 Garantias de Segurança

### Não Quebra o App
1. ✅ Nova Activity isolada
2. ✅ Não modifica código existente
3. ✅ Use Cases independentes
4. ✅ Repository estende funcionalidade
5. ✅ Testes antes de integrar

### Rollback Fácil
- Remover botão do Dashboard
- Desabilitar Activity no Manifest
- Código isolado, fácil de remover

---

## 📝 Próximos Passos

1. **Aprovar o plano**
2. **Implementar Fase 1 (Backend)**
3. **Testar endpoints**
4. **Implementar Fase 2-4 (Android)**
5. **Testar no dispositivo**
6. **Integrar no app principal**

---

**Status:** 📋 PLANO COMPLETO E PRONTO PARA IMPLEMENTAÇÃO  
**Risco:** 🟢 BAIXO (Implementação isolada)  
**Impacto:** 🟢 ALTO (Resolve problema real)

