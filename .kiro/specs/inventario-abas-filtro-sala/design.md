# Design Document - Inventário com Abas e Filtro por Sala (Android)

## Overview

Este documento descreve o design para implementação de um sistema de abas na `InventarioActivity` do app Android, adicionando uma nova aba "Por Sala" que permite visualizar e filtrar patrimônios por localização física. A implementação segue Clean Architecture + MVVM conforme as diretrizes do projeto.

## Architecture

### Padrão Arquitetural
- **MVVM** (Model-View-ViewModel) com StateFlow
- **Clean Architecture** com separação em camadas (presentation, domain, data)
- **Hilt** para injeção de dependência

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                    InventarioActivity                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    TabLayout                             │    │
│  │  ┌──────────────────┐  ┌──────────────────┐             │    │
│  │  │ Por Responsável  │  │    Por Sala      │             │    │
│  │  └──────────────────┘  └──────────────────┘             │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    ViewPager2                            │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  ResponsavelFragment  │  SalaFragment           │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    InventarioViewModel                           │
│  - salasState: StateFlow<SalasUiState>                          │
│  - patrimoniosPorSalaState: StateFlow<PatrimoniosSalaState>     │
│  - loadSalas()                                                   │
│  - loadPatrimoniosBySala(salaId, coletado?)                     │
│  - filterPatrimonios(query)                                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Use Cases                                     │
│  - BuscarSalasUseCase                                           │
│  - BuscarPatrimoniosPorSalaUseCase                              │
│  - BuscarEstatisticasSalaUseCase                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Repository                                    │
│  - SalaRepository                                                │
│  - PatrimonioRepository                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Data Sources                                  │
│  - SalaApi (GET /api/mobile/salas)                              │
│  - PatrimonioApi (GET /api/mobile/patrimonio/sala/{salaId})     │
└─────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. UI Components

#### SalaFragment
```kotlin
@AndroidEntryPoint
class SalaFragment : Fragment() {
    private val viewModel: InventarioViewModel by activityViewModels()
    
    // UI Components
    - spinnerSala: AutoCompleteTextView
    - chipGroupStatus: ChipGroup (Todos, Coletados, Pendentes)
    - searchView: SearchView
    - recyclerView: RecyclerView
    - cardEstatisticas: CardView
    - tvTotalCount: TextView
    - emptyView: View
}
```

#### SalaAdapter (para dropdown)
```kotlin
class SalaDropdownAdapter(
    context: Context,
    salas: List<SalaComEstatisticas>
) : ArrayAdapter<SalaComEstatisticas> {
    // Exibe: "Sala 101 - 75% ✓" ou "Sala 102 - 30%"
}
```

#### PatrimonioSalaAdapter (para RecyclerView)
```kotlin
class PatrimonioSalaAdapter(
    private val onItemClick: (Patrimonio) -> Unit
) : ListAdapter<Patrimonio, PatrimonioSalaViewHolder>(PatrimonioDiffCallback()) {
    // Exibe patrimônios com indicador de status (verde/amarelo)
}
```

### 2. ViewModel States

```kotlin
// Estado das salas
sealed class SalasUiState {
    object Idle : SalasUiState()
    object Loading : SalasUiState()
    data class Success(
        val salas: List<SalaComEstatisticas>,
        val totalSalas: Int,
        val salasCompletas: Int,
        val salasEmProgresso: Int
    ) : SalasUiState()
    data class Error(val message: String) : SalasUiState()
}

// Estado dos patrimônios por sala
sealed class PatrimoniosSalaState {
    object Idle : PatrimoniosSalaState()
    object Loading : PatrimoniosSalaState()
    data class Success(
        val patrimonios: List<Patrimonio>,
        val patrimoniosFiltrados: List<Patrimonio>,
        val totalPatrimonios: Int,
        val coletados: Int,
        val pendentes: Int,
        val searchQuery: String,
        val filtroStatus: FiltroStatus
    ) : PatrimoniosSalaState()
    data class Error(val message: String) : PatrimoniosSalaState()
}

enum class FiltroStatus { TODOS, COLETADOS, PENDENTES }
```

### 3. Domain Models

```kotlin
data class SalaComEstatisticas(
    val id: Int,
    val nome: String,
    val numero: String?,
    val setorNome: String?,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualConcluido: Float,
    val isCompleta: Boolean
)
```

### 4. API Endpoints (já existentes)

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/api/mobile/salas` | GET | Lista todas as salas |
| `/api/mobile/patrimonio/sala/{salaId}` | GET | Patrimônios por sala |

### 5. Novo Endpoint Necessário

```java
// MobilePatrimonioController.java
@GetMapping("/sala/{salaId}/estatisticas")
public ResponseEntity<ApiResponse<SalaEstatisticasDTO>> buscarEstatisticasSala(
    @PathVariable Integer salaId,
    @RequestParam(required = false) Integer inventarioId
) {
    // Retorna: totalPatrimonios, coletados, pendentes, percentual
}
```

## Data Models

### API DTOs

```kotlin
// Resposta do endpoint de salas com estatísticas
data class SalaEstatisticasDTO(
    val salaId: Int,
    val salaNome: String,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualConcluido: Float
)
```

### Room Entities (para cache offline)

```kotlin
@Entity(tableName = "sala_estatisticas")
data class SalaEstatisticasEntity(
    @PrimaryKey val salaId: Int,
    val salaNome: String,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualConcluido: Float,
    val lastUpdated: Long
)
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Room selection loads correct assets
*For any* room selected from the dropdown, the displayed list should contain only assets where `salaId` equals the selected room's ID.
**Validates: Requirements 1.3**

### Property 2: Status indicator matches collection state
*For any* asset displayed in the list, if `coletado == true` then the status indicator should be green, otherwise it should be yellow/orange.
**Validates: Requirements 1.4, 1.5**

### Property 3: Coletados filter shows only collected
*For any* list of assets, when "Coletados" filter is selected, all displayed items should have `coletado == true`.
**Validates: Requirements 2.2**

### Property 4: Pendentes filter shows only pending
*For any* list of assets, when "Pendentes" filter is selected, all displayed items should have `coletado == false`.
**Validates: Requirements 2.3**

### Property 5: Count summary is accurate
*For any* filtered list, the count summary "Exibindo X de Y" should have X equal to the filtered list size and Y equal to the total unfiltered list size.
**Validates: Requirements 2.5**

### Property 6: Room statistics are correct
*For any* room, the percentage completed should equal `(coletados / totalPatrimonios) * 100`, and `isCompleta` should be true only when `coletados == totalPatrimonios`.
**Validates: Requirements 3.1, 3.2, 3.3**

### Property 7: Search filters by number or description
*For any* search query, all displayed items should contain the query string in either `numeroPatrimonio` or `descricao` (case-insensitive).
**Validates: Requirements 4.2**

### Property 8: Clear search restores full list
*For any* room, after clearing the search field, the displayed list should equal the original unfiltered list for that room.
**Validates: Requirements 4.3**

### Property 9: Tab state preservation
*For any* tab switch, returning to the previous tab should restore the same filter selections and search query that were active before switching.
**Validates: Requirements 5.1**

### Property 10: Inventory change resets filters
*For any* inventory change, all filters should be reset to default values (Todos, empty search, no room selected).
**Validates: Requirements 5.3**

## Error Handling

### Network Errors
- Exibir Snackbar com mensagem de erro e botão "Tentar novamente"
- Manter dados em cache se disponíveis
- Usar estratégia offline-first quando possível

### Empty States
- Sala sem patrimônios: "Esta sala não possui patrimônios cadastrados"
- Busca sem resultados: "Nenhum patrimônio encontrado"
- Erro de carregamento: "Erro ao carregar dados. Toque para tentar novamente"

### Loading States
- Shimmer effect durante carregamento inicial
- SwipeRefreshLayout para pull-to-refresh
- ProgressBar inline para carregamento de mais itens

## Testing Strategy

### Dual Testing Approach

#### Unit Tests
- Testar cálculo de estatísticas de sala
- Testar lógica de filtros (Todos, Coletados, Pendentes)
- Testar lógica de busca (case-insensitive, número ou descrição)
- Testar mapeamento de DTOs para domain models

#### Property-Based Tests (fast-check ou similar)
- **Property 1**: Room selection loads correct assets
- **Property 2**: Status indicator matches collection state
- **Property 3-4**: Filter correctness (Coletados/Pendentes)
- **Property 5**: Count summary accuracy
- **Property 6**: Room statistics calculation
- **Property 7-8**: Search functionality
- **Property 9-10**: State management

### Testing Framework
- **JUnit 5** para testes unitários
- **Kotest** com property-based testing para Kotlin
- **Mockk** para mocking de dependências
- **Turbine** para testar StateFlow

### Test Annotations
Cada property-based test deve incluir:
```kotlin
/**
 * **Feature: inventario-abas-filtro-sala, Property 1: Room selection loads correct assets**
 * **Validates: Requirements 1.3**
 */
@Test
fun `room selection should load only assets from selected room`() {
    // Property-based test implementation
}
```
