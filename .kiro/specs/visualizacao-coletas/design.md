# Design Document - Visualização de Coletas

## Overview

A funcionalidade de Visualização de Coletas permite aos usuários do aplicativo Android visualizar, filtrar e analisar todas as coletas realizadas durante o inventário patrimonial. O sistema oferece múltiplos filtros (usuário, sala, status), exibe informações detalhadas de cada coleta (descrição, localização, estado de conservação, sincronização) e fornece contadores dinâmicos para acompanhamento do progresso.

A implementação seguirá Clean Architecture + MVVM, com separação clara entre camadas de apresentação, domínio e dados, garantindo testabilidade e manutenibilidade.

## Architecture

### Camadas da Aplicação

```
┌─────────────────────────────────────────────────────────────┐
│                 Presentation Layer                           │
│  - ColetasActivity (View)                                    │
│  - ColetasViewModel (ViewModel)                              │
│  - ColetasState (UI State)                                   │
│  - ColetasAdapter (RecyclerView Adapter)                     │
└──────────────────────┬──────────────────────────────────────┘
                       │ observa StateFlow
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                              │
│  - BuscarColetasUseCase                                      │
│  - FiltrarColetasUseCase                                     │
│  - AgruparColetasPorSalaUseCase                              │
│  - ColetaRepository (interface)                              │
└──────────────────────┬──────────────────────────────────────┘
                       │ implementa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                               │
│  - ColetaRepositoryImpl                                      │
│  - ColetaDao (Room)                                          │
│  - ColetaEntity (Room)                                       │
│  - ColetaMapper                                              │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de Dados

1. **Carregamento Inicial**: Activity → ViewModel → UseCase → Repository → Room Database
2. **Aplicação de Filtros**: ViewModel aplica filtros em memória sobre dados carregados
3. **Atualização de UI**: StateFlow notifica Activity sobre mudanças de estado
4. **Agrupamento**: UseCase agrupa coletas por sala quando necessário

## Components and Interfaces

### 1. Presentation Layer

#### ColetasState (Sealed Class)
```kotlin
sealed class ColetasState {
    object Idle : ColetasState()
    object Loading : ColetasState()
    
    data class Success(
        val coletas: List<Coleta>,
        val totalColetados: Int,
        val totalPendentes: Int,
        val filtroUsuario: Boolean,
        val filtroSala: String?,
        val filtroStatus: StatusFiltro
    ) : ColetasState()
    
    data class Error(val message: String) : ColetasState()
}

enum class StatusFiltro {
    TODOS, COLETADOS, PENDENTES
}
```

#### ColetasViewModel
```kotlin
@HiltViewModel
class ColetasViewModel @Inject constructor(
    private val buscarColetasUseCase: BuscarColetasUseCase,
    private val filtrarColetasUseCase: FiltrarColetasUseCase,
    private val agruparColetasPorSalaUseCase: AgruparColetasPorSalaUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<ColetasState>(ColetasState.Idle)
    val state: StateFlow<ColetasState> = _state.asStateFlow()
    
    private var todasColetas: List<Coleta> = emptyList()
    private var filtroUsuarioAtivo = true
    private var filtroSalaAtivo: String? = null
    private var filtroStatusAtivo = StatusFiltro.TODOS
    
    fun carregarColetas()
    fun toggleFiltroUsuario()
    fun aplicarFiltroSala(sala: String?)
    fun aplicarFiltroStatus(status: StatusFiltro)
    fun agruparPorSala(): Map<String, List<Coleta>>
}
```

#### ColetasActivity
```kotlin
@AndroidEntryPoint
class ColetasActivity : AppCompatActivity() {
    private val viewModel: ColetasViewModel by viewModels()
    private lateinit var binding: ActivityColetasBinding
    private lateinit var adapter: ColetasAdapter
    
    override fun onCreate(savedInstanceState: Bundle?)
    private fun setupObservers()
    private fun setupFilters()
    private fun updateUI(state: ColetasState)
}
```

### 2. Domain Layer

#### Use Cases

**BuscarColetasUseCase**
```kotlin
class BuscarColetasUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(): Result<List<Coleta>> {
        return coletaRepository.buscarTodasColetas()
    }
}
```

**FiltrarColetasUseCase**
```kotlin
class FiltrarColetasUseCase @Inject constructor() {
    operator fun invoke(
        coletas: List<Coleta>,
        usuarioAtual: String?,
        filtroUsuario: Boolean,
        filtroSala: String?,
        filtroStatus: StatusFiltro
    ): List<Coleta> {
        var resultado = coletas
        
        if (filtroUsuario && usuarioAtual != null) {
            resultado = resultado.filter { it.coletadoPor == usuarioAtual }
        }
        
        if (filtroSala != null) {
            resultado = resultado.filter { it.nomeSala == filtroSala }
        }
        
        resultado = when (filtroStatus) {
            StatusFiltro.COLETADOS -> resultado.filter { it.sincronizado }
            StatusFiltro.PENDENTES -> resultado.filter { !it.sincronizado }
            StatusFiltro.TODOS -> resultado
        }
        
        return resultado
    }
}
```

**AgruparColetasPorSalaUseCase**
```kotlin
class AgruparColetasPorSalaUseCase @Inject constructor() {
    operator fun invoke(coletas: List<Coleta>): Map<String, List<Coleta>> {
        return coletas.groupBy { it.nomeSala ?: "Sala não definida" }
            .toSortedMap() // Ordenação alfabética
    }
}
```

#### Repository Interface

```kotlin
interface ColetaRepository {
    suspend fun buscarTodasColetas(): Result<List<Coleta>>
    suspend fun buscarColetasPorUsuario(usuarioId: Int): Result<List<Coleta>>
    suspend fun buscarColetasPorSala(salaId: Int): Result<List<Coleta>>
    suspend fun contarColetadas(): Int
    suspend fun contarPendentes(): Int
}
```

### 3. Data Layer

#### ColetaEntity (Room)
```kotlin
@Entity(tableName = "coletas")
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val idPatrimonio: Int,
    val numeroPatrimonio: String,
    val descricaoPatrimonio: String,
    val idInventario: Int,
    val idSala: Int?,
    val nomeSala: String?,
    val localizacaoEncontrada: String?,
    val estadoEncontrado: String,
    val observacao: String?,
    val dataColeta: Long,
    val idUsuario: Int,
    val nomeUsuario: String,
    val coletadoPor: String,
    val sincronizado: Boolean,
    val servidorId: Long?
)
```

#### ColetaDao (Room)
```kotlin
@Dao
interface ColetaDao {
    @Query("SELECT * FROM coletas ORDER BY dataColeta DESC")
    suspend fun buscarTodas(): List<ColetaEntity>
    
    @Query("SELECT * FROM coletas WHERE idUsuario = :usuarioId ORDER BY dataColeta DESC")
    suspend fun buscarPorUsuario(usuarioId: Int): List<ColetaEntity>
    
    @Query("SELECT * FROM coletas WHERE idSala = :salaId ORDER BY dataColeta DESC")
    suspend fun buscarPorSala(salaId: Int): List<ColetaEntity>
    
    @Query("SELECT COUNT(*) FROM coletas WHERE sincronizado = 1")
    suspend fun contarSincronizadas(): Int
    
    @Query("SELECT COUNT(*) FROM coletas WHERE sincronizado = 0")
    suspend fun contarPendentes(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(coleta: ColetaEntity): Long
    
    @Update
    suspend fun atualizar(coleta: ColetaEntity)
}
```

#### ColetaRepositoryImpl
```kotlin
class ColetaRepositoryImpl @Inject constructor(
    private val coletaDao: ColetaDao,
    private val mapper: ColetaMapper
) : ColetaRepository {
    
    override suspend fun buscarTodasColetas(): Result<List<Coleta>> {
        return try {
            val entities = coletaDao.buscarTodas()
            val coletas = entities.map { mapper.toDomain(it) }
            Result.success(coletas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun contarColetadas(): Int {
        return coletaDao.contarSincronizadas()
    }
    
    override suspend fun contarPendentes(): Int {
        return coletaDao.contarPendentes()
    }
}
```

## Data Models

### Domain Model - Coleta
```kotlin
data class Coleta(
    val id: Long,
    val patrimonioId: Int,
    val numeroPatrimonio: String,
    val descricaoPatrimonio: String,
    val nomeSala: String?,
    val localizacaoEncontrada: String?,
    val estadoEncontrado: String,
    val observacoes: String?,
    val dataColeta: Long,
    val coletadoPor: String,
    val sincronizado: Boolean
) {
    fun isColetado(): Boolean = sincronizado
    fun isPendente(): Boolean = !sincronizado
}
```

### Mapper
```kotlin
class ColetaMapper @Inject constructor() {
    fun toDomain(entity: ColetaEntity): Coleta {
        return Coleta(
            id = entity.id,
            patrimonioId = entity.idPatrimonio,
            numeroPatrimonio = entity.numeroPatrimonio,
            descricaoPatrimonio = entity.descricaoPatrimonio,
            nomeSala = entity.nomeSala,
            localizacaoEncontrada = entity.localizacaoEncontrada,
            estadoEncontrado = entity.estadoEncontrado,
            observacoes = entity.observacao,
            dataColeta = entity.dataColeta,
            coletadoPor = entity.coletadoPor,
            sincronizado = entity.sincronizado
        )
    }
    
    fun toEntity(coleta: Coleta): ColetaEntity {
        // Implementação inversa
    }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Exibição completa de coletas
*For any* conjunto de coletas carregadas, todas as coletas devem ser exibidas na lista com descrição, localização, estado e indicador de sincronização visíveis
**Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**

### Property 2: Filtro de usuário exclusivo
*For any* usuário logado, quando o filtro por usuário está ativo, apenas coletas realizadas por esse usuário devem aparecer na lista
**Validates: Requirements 2.1, 2.2**

### Property 3: Filtro de usuário desativado mostra todos
*For any* conjunto de coletas, quando o filtro por usuário está desativado, coletas de todos os usuários devem aparecer na lista
**Validates: Requirements 2.3**

### Property 4: Atualização imediata de filtros
*For any* mudança de filtro, o estado da lista deve ser atualizado imediatamente sem delay perceptível
**Validates: Requirements 2.4**

### Property 5: Contadores refletem filtros
*For any* conjunto de filtros aplicados, os contadores de totais devem corresponder exatamente ao número de itens na lista filtrada
**Validates: Requirements 2.5, 6.3**

### Property 6: Filtro de sala específica
*For any* sala selecionada, apenas coletas de patrimônios dessa sala devem aparecer na lista filtrada
**Validates: Requirements 3.1**

### Property 7: Sem filtro de sala mostra todas
*For any* conjunto de coletas, quando nenhuma sala está selecionada, coletas de todas as salas devem aparecer
**Validates: Requirements 3.2**

### Property 8: Múltiplos filtros simultâneos (AND lógico)
*For any* combinação de filtros ativos (usuário, sala, status), apenas coletas que atendem TODOS os critérios devem aparecer
**Validates: Requirements 3.4, 4.5, 8.1, 8.2, 8.3, 8.4**

### Property 9: Filtro de status coletados
*For any* conjunto de patrimônios, quando o filtro "coletados" está ativo, apenas patrimônios com coletado=true devem aparecer
**Validates: Requirements 4.1**

### Property 10: Filtro de status pendentes
*For any* conjunto de patrimônios, quando o filtro "pendentes" está ativo, apenas patrimônios com coletado=false devem aparecer
**Validates: Requirements 4.2**

### Property 11: Informações detalhadas completas
*For any* coleta exibida, todos os campos obrigatórios (descrição, localização, estado, coletor, sincronização) devem estar presentes e visíveis
**Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**

### Property 12: Contadores sempre corretos
*For any* estado da lista, os contadores de coletados e pendentes devem corresponder exatamente ao número de itens em cada categoria
**Validates: Requirements 6.1, 6.2, 6.4**

### Property 13: Estados de carregamento
*For any* operação de carregamento, o sistema deve transicionar de Idle → Loading → (Success | Error) sem pular estados
**Validates: Requirements 7.1, 7.2, 7.3**

### Property 14: Indicadores de sincronização
*For any* coleta exibida, o indicador visual de sincronização deve refletir corretamente o campo sincronizado (true/false)
**Validates: Requirements 9.1, 9.2, 9.3, 9.4**

### Property 15: Agrupamento por sala
*For any* conjunto de coletas agrupadas, coletas da mesma sala devem estar juntas e ordenadas alfabeticamente por nome da sala
**Validates: Requirements 10.1, 10.3, 10.5**

### Property 16: Contagem de itens por grupo
*For any* grupo de sala exibido, a contagem de itens deve corresponder exatamente ao número de coletas naquele grupo
**Validates: Requirements 10.4**

### Property 17: Remoção de filtros expande resultados
*For any* conjunto de filtros, remover um filtro deve resultar em lista igual ou maior que a anterior
**Validates: Requirements 8.5**

## Error Handling

### Estratégias de Tratamento de Erros

1. **Erro de Carregamento de Dados**
   - Capturar exceções do Room Database
   - Exibir mensagem clara ao usuário
   - Permitir retry manual
   - Log detalhado para debugging

2. **Lista Vazia**
   - Exibir empty state com mensagem apropriada
   - Diferenciar entre "sem coletas" e "sem coletas filtradas"
   - Sugerir ações (remover filtros, fazer coletas)

3. **Filtros Sem Resultados**
   - Exibir mensagem específica ("Nenhuma coleta encontrada com os filtros aplicados")
   - Mostrar filtros ativos
   - Botão para limpar filtros

4. **Erro de Sincronização**
   - Não bloquear visualização de coletas locais
   - Indicar claramente coletas não sincronizadas
   - Permitir retry de sincronização individual

### Validações

1. **Validação de Dados**
   - Verificar integridade dos dados do Room
   - Validar que campos obrigatórios não são nulos
   - Tratar dados inconsistentes graciosamente

2. **Validação de Filtros**
   - Verificar que usuário atual existe antes de filtrar
   - Validar que sala selecionada é válida
   - Tratar combinações de filtros vazias

## Testing Strategy

### Unit Tests

1. **ViewModel Tests**
   - Testar transições de estado (Idle → Loading → Success/Error)
   - Testar aplicação de filtros individuais
   - Testar combinação de múltiplos filtros
   - Testar cálculo de contadores
   - Testar agrupamento por sala

2. **Use Case Tests**
   - Testar FiltrarColetasUseCase com diferentes combinações
   - Testar AgruparColetasPorSalaUseCase com dados variados
   - Testar casos edge (lista vazia, sala null)

3. **Repository Tests**
   - Testar busca de coletas do Room
   - Testar contagem de coletadas/pendentes
   - Testar tratamento de erros do Room

4. **Mapper Tests**
   - Testar conversão Entity → Domain
   - Testar conversão Domain → Entity
   - Testar preservação de dados

### Property-Based Tests

O sistema utilizará **Kotest Property Testing** para validação das propriedades de correção.

**Configuração:**
```kotlin
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
}
```

**Requisitos dos Testes:**
- Cada teste de propriedade deve executar no mínimo 100 iterações
- Cada teste deve referenciar explicitamente a propriedade do design usando o formato: `**Feature: visualizacao-coletas, Property {número}: {texto}**`
- Cada propriedade de correção deve ser implementada por UM ÚNICO teste de propriedade

**Exemplo de Teste:**
```kotlin
class ColetasPropertyTest : StringSpec({
    
    "Property 2: Filtro de usuário exclusivo" {
        /**
         * Feature: visualizacao-coletas, Property 2: Filtro de usuário exclusivo
         * Validates: Requirements 2.1, 2.2
         */
        checkAll(100, Arb.list(Arb.coleta(), 1..100)) { coletas ->
            val usuarioAtual = coletas.random().coletadoPor
            val filtradas = FiltrarColetasUseCase()(
                coletas = coletas,
                usuarioAtual = usuarioAtual,
                filtroUsuario = true,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            filtradas.all { it.coletadoPor == usuarioAtual } shouldBe true
        }
    }
    
    "Property 8: Múltiplos filtros simultâneos (AND lógico)" {
        /**
         * Feature: visualizacao-coletas, Property 8: Múltiplos filtros simultâneos
         * Validates: Requirements 3.4, 4.5, 8.1, 8.2, 8.3, 8.4
         */
        checkAll(100, 
            Arb.list(Arb.coleta(), 1..100),
            Arb.string(1..20),
            Arb.string(1..20)
        ) { coletas, usuarioAtual, salaAtual ->
            val filtradas = FiltrarColetasUseCase()(
                coletas = coletas,
                usuarioAtual = usuarioAtual,
                filtroUsuario = true,
                filtroSala = salaAtual,
                filtroStatus = StatusFiltro.COLETADOS
            )
            
            filtradas.all { 
                it.coletadoPor == usuarioAtual && 
                it.nomeSala == salaAtual && 
                it.sincronizado 
            } shouldBe true
        }
    }
})
```

### Integration Tests

1. **Room Database Tests**
   - Testar queries do ColetaDao
   - Testar inserção e atualização de coletas
   - Testar contagens e agregações

2. **UI Tests (Espresso)**
   - Testar navegação para tela de coletas
   - Testar aplicação de filtros via UI
   - Testar exibição de informações detalhadas
   - Testar indicadores visuais de sincronização

### Test Generators (Arb)

```kotlin
fun Arb.Companion.coleta(): Arb<Coleta> = arbitrary {
    Coleta(
        id = Arb.long(1..10000).bind(),
        patrimonioId = Arb.int(1..1000).bind(),
        numeroPatrimonio = Arb.string(5..10).bind(),
        descricaoPatrimonio = Arb.string(10..50).bind(),
        nomeSala = Arb.string(5..20).orNull().bind(),
        localizacaoEncontrada = Arb.string(5..30).orNull().bind(),
        estadoEncontrado = Arb.of("BOM", "REGULAR", "RUIM").bind(),
        observacoes = Arb.string(0..100).orNull().bind(),
        dataColeta = Arb.long(1000000000000..2000000000000).bind(),
        coletadoPor = Arb.string(5..20).bind(),
        sincronizado = Arb.bool().bind()
    )
}
```

## Performance Considerations

### Otimizações

1. **Carregamento de Dados**
   - Usar Room Database com queries otimizadas
   - Índices nas colunas de filtro (idUsuario, idSala, sincronizado)
   - Carregar dados uma vez e filtrar em memória

2. **Filtragem**
   - Aplicar filtros em memória (mais rápido que queries repetidas)
   - Usar Kotlin sequences para operações lazy
   - Cache de resultados filtrados

3. **UI**
   - RecyclerView com DiffUtil para atualizações eficientes
   - ViewHolder pattern para reciclagem de views
   - Paginação se lista crescer muito (>1000 itens)

4. **StateFlow**
   - Usar StateFlow ao invés de LiveData (melhor performance)
   - Evitar emissões desnecessárias de estado
   - Usar distinctUntilChanged para evitar recomposições
