# Design Document - Inventário com Abas e Filtro por Sala

## Overview

Esta funcionalidade adiciona uma nova aba na tela de Inventário do aplicativo Android, permitindo filtrar patrimônios por sala. A implementação segue Clean Architecture + MVVM, reutilizando componentes existentes e adicionando novos Use Cases e ViewModels específicos para a funcionalidade de filtro por sala.

A tela atual de Inventário será refatorada para usar TabLayout + ViewPager2, com duas abas:
1. **Por Responsável** - Funcionalidade existente (mantida)
2. **Por Sala** - Nova funcionalidade

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    InventarioActivity                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     TabLayout                            │    │
│  │  [Por Responsável]  [Por Sala]                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    ViewPager2                            │    │
│  │  ┌─────────────────┐  ┌─────────────────┐               │    │
│  │  │ ResponsavelFrag │  │   SalaFragment  │               │    │
│  │  │  (existente)    │  │     (novo)      │               │    │
│  │  └─────────────────┘  └─────────────────┘               │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presentation Layer                            │
│  - InventarioActivity (refatorada com TabLayout)                │
│  - InventarioPorResponsavelFragment (extraído do existente)     │
│  - InventarioPorSalaFragment (novo)                             │
│  - InventarioPorSalaViewModel (novo)                            │
│  - InventarioPorSalaState (novo)                                │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                      Domain Layer                                │
│  - BuscarPatrimoniosPorSalaUseCase (novo)                       │
│  - BuscarEstatisticasSalaUseCase (novo)                         │
│  - BuscarSalasComProgressoUseCase (novo)                        │
│  - SalaComProgresso (novo model)                                │
│  - EstatisticasSala (novo model)                                │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                       Data Layer                                 │
│  - SalaRepositoryImpl (atualizado)                              │
│  - PatrimonioRepositoryImpl (atualizado)                        │
│  - SalaDao (atualizado com novas queries)                       │
│  - PatrimonioDao (atualizado com novas queries)                 │
└─────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Domain Models (Novos)

```kotlin
// domain/model/SalaComProgresso.kt
data class SalaComProgresso(
    val id: Int,
    val nome: String,
    val numero: String?,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualColeta: Float
) {
    val isCompleta: Boolean get() = percentualColeta >= 100f
    val isVazia: Boolean get() = totalPatrimonios == 0
    val temPendentes: Boolean get() = pendentes > 0
}

// domain/model/EstatisticasSala.kt
data class EstatisticasSala(
    val salaId: Int,
    val salaNome: String,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualColeta: Float,
    val coletadosHoje: Int,
    val coletadosSemana: Int
)
```

### 2. Use Cases (Novos)

```kotlin
// domain/usecase/BuscarPatrimoniosPorSalaUseCase.kt
class BuscarPatrimoniosPorSalaUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(
        salaId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>>
}

// domain/usecase/BuscarEstatisticasSalaUseCase.kt
class BuscarEstatisticasSalaUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository,
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(salaId: Int): Result<EstatisticasSala>
}

// domain/usecase/BuscarSalasComProgressoUseCase.kt
class BuscarSalasComProgressoUseCase @Inject constructor(
    private val salaRepository: SalaRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(): Result<List<SalaComProgresso>>
}
```

### 3. Repository Interfaces (Atualizações)

```kotlin
// domain/repository/PatrimonioRepository.kt (adicionar)
interface PatrimonioRepository {
    // ... métodos existentes ...
    
    suspend fun buscarPorSala(
        salaId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>>
    
    suspend fun contarPorSala(salaId: Int): Result<Int>
    suspend fun contarColetadosPorSala(salaId: Int): Result<Int>
}

// domain/repository/SalaRepository.kt (adicionar)
interface SalaRepository {
    // ... métodos existentes ...
    
    suspend fun buscarComProgresso(): Result<List<SalaComProgresso>>
    suspend fun buscarPorNomeOuNumero(query: String): Result<List<Sala>>
}
```

### 4. DAO Queries (Novas)

```kotlin
// data/local/dao/PatrimonioDao.kt (adicionar)
@Query("""
    SELECT * FROM patrimonio 
    WHERE id_sala = :salaId 
    AND (:coletado IS NULL OR coletado = :coletado)
    ORDER BY numero_patrimonio
    LIMIT :pageSize OFFSET :offset
""")
suspend fun buscarPorSala(
    salaId: Int, 
    coletado: Boolean?, 
    pageSize: Int, 
    offset: Int
): List<PatrimonioEntity>

@Query("SELECT COUNT(*) FROM patrimonio WHERE id_sala = :salaId")
suspend fun contarPorSala(salaId: Int): Int

@Query("SELECT COUNT(*) FROM patrimonio WHERE id_sala = :salaId AND coletado = 1")
suspend fun contarColetadosPorSala(salaId: Int): Int

// data/local/dao/SalaDao.kt (adicionar)
@Query("""
    SELECT s.*, 
           COUNT(p.id) as total_patrimonios,
           SUM(CASE WHEN p.coletado = 1 THEN 1 ELSE 0 END) as coletados
    FROM sala s
    LEFT JOIN patrimonio p ON p.id_sala = s.id
    GROUP BY s.id
    ORDER BY s.nome
""")
suspend fun buscarComEstatisticas(): List<SalaComEstatisticasEntity>

@Query("""
    SELECT * FROM sala 
    WHERE nome LIKE '%' || :query || '%' 
    OR numero LIKE '%' || :query || '%'
    ORDER BY nome
""")
suspend fun buscarPorNomeOuNumero(query: String): List<SalaEntity>
```

### 5. ViewModel e State

```kotlin
// presentation/inventario/InventarioPorSalaViewModel.kt
@HiltViewModel
class InventarioPorSalaViewModel @Inject constructor(
    private val buscarPatrimoniosPorSalaUseCase: BuscarPatrimoniosPorSalaUseCase,
    private val buscarEstatisticasSalaUseCase: BuscarEstatisticasSalaUseCase,
    private val buscarSalasComProgressoUseCase: BuscarSalasComProgressoUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<InventarioPorSalaState>(InventarioPorSalaState.Idle)
    val state: StateFlow<InventarioPorSalaState> = _state.asStateFlow()
    
    fun carregarSalas()
    fun selecionarSala(salaId: Int)
    fun aplicarFiltro(coletado: Boolean?)
    fun buscarSala(query: String)
    fun carregarMaisPatrimonios()
}

// presentation/state/InventarioPorSalaState.kt
sealed class InventarioPorSalaState {
    object Idle : InventarioPorSalaState()
    object Loading : InventarioPorSalaState()
    
    data class SalasCarregadas(
        val salas: List<SalaComProgresso>,
        val salasFiltradas: List<SalaComProgresso> = salas
    ) : InventarioPorSalaState()
    
    data class SalaSelecionada(
        val sala: SalaComProgresso,
        val estatisticas: EstatisticasSala,
        val patrimonios: List<Patrimonio>,
        val filtroAtual: FiltroColeta = FiltroColeta.TODOS,
        val hasMorePages: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : InventarioPorSalaState()
    
    data class Error(val message: String) : InventarioPorSalaState()
}

enum class FiltroColeta { TODOS, COLETADOS, NAO_COLETADOS }
```

## Data Models

### Entity para Sala com Estatísticas

```kotlin
// data/local/entity/SalaComEstatisticasEntity.kt
data class SalaComEstatisticasEntity(
    @Embedded val sala: SalaEntity,
    @ColumnInfo(name = "total_patrimonios") val totalPatrimonios: Int,
    @ColumnInfo(name = "coletados") val coletados: Int
)
```

### Mapper

```kotlin
// data/mapper/SalaComProgressoMapper.kt
fun SalaComEstatisticasEntity.toDomain(): SalaComProgresso {
    val pendentes = totalPatrimonios - coletados
    val percentual = if (totalPatrimonios > 0) {
        (coletados.toFloat() / totalPatrimonios) * 100
    } else 0f
    
    return SalaComProgresso(
        id = sala.id,
        nome = sala.nome,
        numero = sala.numero,
        totalPatrimonios = totalPatrimonios,
        coletados = coletados,
        pendentes = pendentes,
        percentualColeta = percentual
    )
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Filter Correctness
*For any* room and filter selection (TODOS, COLETADOS, NAO_COLETADOS), all displayed assets should match the filter criteria: if COLETADOS, all assets have coletado=true; if NAO_COLETADOS, all assets have coletado=false; if TODOS, the count equals total assets in room.
**Validates: Requirements 3.2, 3.3, 3.4**

### Property 2: Statistics Calculation Consistency
*For any* room, the statistics should satisfy: total = coletados + pendentes, and percentual = (coletados / total) * 100 when total > 0, otherwise percentual = 0.
**Validates: Requirements 1.4, 2.1**

### Property 3: Color Mapping Correctness
*For any* collection percentage, the color should match the defined ranges: red for 0-25%, orange for 26-50%, yellow for 51-75%, green for 76-100%.
**Validates: Requirements 2.3, 6.2**

### Property 4: Room Search Filtering
*For any* search query, all displayed rooms should contain the query string in either their name or number (case-insensitive).
**Validates: Requirements 4.4**

### Property 5: Room Progress Display Consistency
*For any* room in the dropdown, the displayed progress text (e.g., "15/20 coletados") should match the actual collected and total counts from the database.
**Validates: Requirements 4.1**

### Property 6: Tab State Preservation
*For any* sequence of tab switches, the filter state (selected room, selected filter chip) of each tab should be preserved when returning to that tab.
**Validates: Requirements 1.5**

### Property 7: Offline Data Loading
*For any* offline state, the system should load room and asset data from the local database, and the loaded data should be consistent with the last synchronized state.
**Validates: Requirements 5.1**

### Property 8: Data Staleness Detection
*For any* local data with last sync timestamp older than 24 hours, the system should display a warning message.
**Validates: Requirements 5.4**

## Error Handling

### Network Errors
- Quando offline, carregar dados do banco local
- Exibir indicador de modo offline
- Permitir todas as operações de leitura

### Data Errors
- Se sala não encontrada, exibir mensagem de erro
- Se patrimônios não carregarem, exibir estado vazio com opção de retry
- Validar dados antes de exibir estatísticas

### UI Errors
- Tratar estados de loading para evitar múltiplos cliques
- Preservar estado durante rotação de tela
- Tratar lista vazia com mensagem apropriada

## Testing Strategy

### Unit Tests
- Testar cálculo de estatísticas (total, coletados, pendentes, percentual)
- Testar mapeamento de cores por percentual
- Testar filtro de busca de salas
- Testar Use Cases com mocks de repository

### Property-Based Tests (usando Kotest)
- **Property 1**: Gerar listas aleatórias de patrimônios com diferentes status de coleta, aplicar filtros e verificar que todos os itens retornados satisfazem o critério
- **Property 2**: Gerar estatísticas aleatórias e verificar que total = coletados + pendentes e percentual está correto
- **Property 3**: Gerar percentuais aleatórios (0-100) e verificar que a cor retornada está na faixa correta
- **Property 4**: Gerar queries e listas de salas aleatórias, verificar que todas as salas retornadas contêm a query
- **Property 5**: Gerar salas com contagens aleatórias e verificar que o texto de progresso está formatado corretamente

### Integration Tests
- Testar fluxo completo de seleção de sala e carregamento de patrimônios
- Testar persistência de estado entre abas
- Testar comportamento offline

### UI Tests
- Testar navegação entre abas
- Testar seleção de sala no dropdown
- Testar chips de filtro
- Testar scroll infinito na lista de patrimônios
