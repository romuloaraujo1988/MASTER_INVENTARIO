# Diagrama de Funcionalidades - Versão 2.23.0

## 🗺️ Mapa de Funcionalidades

```mermaid
graph TD
    TELA["🖥️ TELA DE INVENTÁRIO v2.23.0<br/>(Paging 3)"]
    
    TELA --> FILTROS["🏷️ FILTROS<br/>(ChipGroup)"]
    TELA --> STATS["📊 ESTATÍSTICAS<br/>(Servidor)"]
    TELA --> ACOES["⚡ AÇÕES<br/>(Toolbar)"]
    
    FILTROS --> TODOS["Todos"]
    FILTROS --> COLETADOS["Coletados"]
    FILTROS --> PENDENTES["Pendentes"]
    
    STATS --> TOTAL["Total<br/>Patrimônios"]
    STATS --> PERCENT["Percentual<br/>Conclusão"]
    
    ACOES --> SCANNER["📷 Scanner<br/>QR Code"]
    ACOES --> EXPORTAR["📤 Exportar<br/>PDF/CSV/Excel"]
    ACOES --> REFRESH["🔄 Atualizar"]
    
    FILTROS --> LISTA["📋 LISTA COM PAGING 3<br/>(Scroll Infinito)"]
    
    LISTA --> CARD["🃏 ITEM CARD"]
    
    CARD --> INFO["ℹ️ Informações Básicas<br/>Número, Descrição,<br/>Marca, Modelo, Status"]
    CARD --> LOCAL["📍 Localização<br/>Setor, Sala,<br/>Responsável"]
    CARD --> COLETA["✅ Info Coleta<br/>Coletado por, Data,<br/>Local encontrado, Estado"]
    CARD --> FOTO["📷 Foto Referência<br/>(se disponível)"]

    style TELA fill:#1976D2,color:#fff
    style FILTROS fill:#7B1FA2,color:#fff
    style STATS fill:#388E3C,color:#fff
    style ACOES fill:#F57C00,color:#fff
    style LISTA fill:#0097A7,color:#fff
    style CARD fill:#455A64,color:#fff
```

---

## 🏗️ Arquitetura da Tela (Clean Architecture + MVVM)

```mermaid
graph TB
    subgraph Presentation["📱 Presentation Layer"]
        FRAG["PatrimonioListPagingFragment<br/>@AndroidEntryPoint"]
        ADAPTER["PatrimonioPagingAdapter<br/>PagingDataAdapter"]
        LOAD_ADAPTER["PatrimonioLoadStateAdapter<br/>Footer: Loading/Erro/Retry"]
    end
    
    subgraph ViewModel["🧠 ViewModel Layer"]
        VM["PatrimonioListViewModelPaging<br/>@HiltViewModel"]
        STATE["EstatisticasState<br/>(sealed class)"]
        PAGING["Flow&lt;PagingData&lt;Patrimonio&gt;&gt;<br/>cachedIn(viewModelScope)"]
    end
    
    subgraph Domain["🎯 Domain Layer"]
        UC1["BuscarPatrimoniosPaginadoUseCase"]
        UC2["BuscarEstatisticasDashboardUseCase"]
    end
    
    subgraph Data["💾 Data Layer"]
        PS1["PatrimonioPagingSource<br/>(Remote)"]
        PS2["PatrimonioPorSalaPagingSource<br/>(Remote por sala)"]
        API["PatrimonioApi<br/>(Retrofit)"]
        DAO["PatrimonioDao<br/>(Room - offline)"]
    end
    
    FRAG -->|"by viewModels()"| VM
    FRAG -->|"collectLatest"| ADAPTER
    ADAPTER --- LOAD_ADAPTER
    VM --> PAGING
    VM --> STATE
    VM -->|"@Inject"| UC1
    VM -->|"@Inject"| UC2
    UC1 --> PS1
    UC1 --> PS2
    UC2 --> API
    PS1 --> API
    PS2 --> API
    API -.->|"fallback"| DAO

    style Presentation fill:#E3F2FD
    style ViewModel fill:#FFF3E0
    style Domain fill:#E8F5E9
    style Data fill:#FCE4EC
```

---

## 🔄 Fluxos de Interação

### Fluxo Principal: Paginação Automática

```mermaid
sequenceDiagram
    participant U as 👤 Usuário
    participant F as Fragment
    participant VM as ViewModel
    participant UC as UseCase
    participant PS as PagingSource
    participant API as API Server

    F->>VM: collectLatest(patrimoniosPaging)
    VM->>UC: BuscarPatrimoniosPaginadoUseCase()
    UC->>PS: Pager(PagingConfig)
    PS->>API: GET /patrimonio?page=0&size=20
    API-->>PS: PagedResponse (20 itens)
    PS-->>VM: PagingData<Patrimonio>
    VM-->>F: emit PagingData
    F->>F: adapter.submitData(pagingData)
    
    Note over U,F: Usuário faz scroll...
    
    U->>F: Scroll até o final
    F->>PS: load(nextPage)
    PS->>API: GET /patrimonio?page=1&size=20
    API-->>PS: PagedResponse (20 itens)
    PS-->>F: Append automático
    F->>F: Lista atualiza com novos itens
```

### Fluxo de Filtros Reativos

```mermaid
sequenceDiagram
    participant U as 👤 Usuário
    participant F as Fragment
    participant VM as ViewModel
    participant PS as PagingSource

    U->>F: Toca chip "Pendentes"
    F->>VM: filtrarPorColetado(false)
    VM->>VM: _filtroColetado.value = false
    Note over VM: flatMapLatest detecta mudança
    VM->>PS: Nova PagingSource com filtro
    PS-->>VM: PagingData filtrada
    VM-->>F: emit nova PagingData
    F->>F: adapter.submitData() - Lista atualiza
```

### Fluxo de Exportação

```mermaid
sequenceDiagram
    participant U as 👤 Usuário
    participant EF as ExportFragment
    participant EVM as ExportViewModel
    participant UC as GerarRelatorioUseCase
    participant FS as FileSystem

    U->>EF: Seleciona formato (PDF)
    U->>EF: Aplica filtro (Coletados)
    U->>EF: Clica "Gerar"
    EF->>EVM: gerarRelatorio(PDF, COLETADOS)
    EVM->>UC: invoke(formato, filtro, sala)
    UC->>FS: Gera arquivo no storage
    FS-->>UC: ExportResult(path, size)
    UC-->>EVM: Result.success
    EVM-->>EF: ExportState.Success
    EF->>U: Opções: Abrir / Compartilhar
```

---

## 🔀 Máquina de Estados

```mermaid
stateDiagram-v2
    [*] --> Idle: Fragment criado
    Idle --> Loading: carregarPatrimonios()
    Loading --> Success: Dados recebidos
    Loading --> Error: Falha na requisição
    Success --> LoadingMore: Scroll infinito
    LoadingMore --> Success: Mais dados recebidos
    LoadingMore --> Error: Falha ao carregar mais
    Error --> Loading: retry()
    Success --> Loading: refresh() / filtro mudou
    
    state Success {
        [*] --> ListaComDados
        ListaComDados --> ListaVazia: filtro sem resultados
        ListaVazia --> ListaComDados: filtro alterado
    }
```

### Estados da UI (EstatisticasState)

```mermaid
stateDiagram-v2
    [*] --> EstatLoading: carregarEstatisticas()
    EstatLoading --> EstatSuccess: API respondeu
    EstatLoading --> EstatError: Falha
    EstatSuccess --> EstatLoading: refresh
    EstatError --> EstatLoading: retry
    
    state EstatSuccess {
        totalPatrimonios: int
        totalColetados: int
        totalPendentes: int
        percentualConclusao: double
    }
```

---

## 🔗 Navegação entre Telas

```mermaid
graph LR
    LOGIN["🔐 Login"] --> DASH["📊 Dashboard"]
    DASH --> INV["📋 Inventário<br/>(Paging 3)"]
    DASH --> STATS["📈 Estatísticas"]
    
    INV --> SCANNER["📷 Scanner<br/>QR Code"]
    INV --> FILTROS["🏷️ Filtros<br/>(Chips)"]
    INV --> DETALHES["🔍 Detalhes<br/>(Dialog)"]
    INV --> EXPORT["📤 Exportar<br/>PDF/CSV/Excel"]
    INV --> FOTO_AMP["🖼️ Foto Ampliar<br/>(Dialog)"]
    
    SCANNER --> COLETA["✅ Coleta<br/>(Registro)"]
    DETALHES --> SCANNER
    
    COLETA --> SYNC["🔄 Sincronização"]

    style LOGIN fill:#F44336,color:#fff
    style DASH fill:#2196F3,color:#fff
    style INV fill:#4CAF50,color:#fff
    style SCANNER fill:#FF9800,color:#fff
    style COLETA fill:#9C27B0,color:#fff
    style EXPORT fill:#00BCD4,color:#fff
    style SYNC fill:#607D8B,color:#fff
```

---

## 📦 Diagrama de Componentes

```mermaid
graph TB
    subgraph UI["🖥️ UI Components"]
        FRAG_PAGING["PatrimonioListPagingFragment"]
        FRAG_LEGACY["PatrimonioListFragment<br/>(legado)"]
        FRAG_EXPORT["ExportFragment"]
    end
    
    subgraph Adapters["🔌 Adapters"]
        PAGING_ADAPTER["PatrimonioPagingAdapter<br/>(PagingDataAdapter)"]
        LOAD_STATE["PatrimonioLoadStateAdapter<br/>(LoadStateAdapter)"]
        PATRIMONIO_ADAPTER["PatrimonioAdapter<br/>(ListAdapter + Fotos)"]
        BASIC_ADAPTER["PatrimonioAdapter<br/>(ListAdapter básico)"]
    end
    
    subgraph ViewModels["🧠 ViewModels"]
        VM_PAGING["PatrimonioListViewModelPaging"]
        VM_LEGACY["PatrimonioListViewModel"]
        VM_EXPORT["ExportViewModel"]
    end
    
    subgraph UseCases["🎯 Use Cases"]
        UC_PAGING["BuscarPatrimoniosPaginadoUseCase"]
        UC_STATS["BuscarEstatisticasDashboardUseCase"]
        UC_EXPORT["GerarRelatorioUseCase"]
        UC_SALAS["BuscarSalasParaExportacaoUseCase"]
    end
    
    FRAG_PAGING --> PAGING_ADAPTER
    FRAG_PAGING --> LOAD_STATE
    FRAG_PAGING --> VM_PAGING
    FRAG_LEGACY --> PATRIMONIO_ADAPTER
    FRAG_LEGACY --> VM_LEGACY
    FRAG_EXPORT --> VM_EXPORT
    
    VM_PAGING --> UC_PAGING
    VM_PAGING --> UC_STATS
    VM_EXPORT --> UC_EXPORT
    VM_EXPORT --> UC_SALAS

    style UI fill:#E3F2FD
    style Adapters fill:#FFF8E1
    style ViewModels fill:#FFF3E0
    style UseCases fill:#E8F5E9
```

---

## 📊 Fluxo de Dados (Data Flow)

```mermaid
flowchart LR
    subgraph Android["📱 Android App"]
        UI["UI<br/>(Fragment)"]
        VM["ViewModel<br/>(StateFlow)"]
        UC["Use Case"]
        REPO["Repository"]
        ROOM["Room DB<br/>(SQLite)"]
    end
    
    subgraph Server["🖥️ Servidor"]
        CTRL["Controller<br/>(REST)"]
        SVC["Service"]
        PG["PostgreSQL"]
    end
    
    UI -->|"observa"| VM
    VM -->|"chama"| UC
    UC -->|"usa"| REPO
    REPO -->|"offline"| ROOM
    REPO -->|"online"| CTRL
    CTRL --> SVC
    SVC --> PG
    PG -->|"PagedResponse"| SVC
    SVC --> CTRL
    CTRL -->|"JSON"| REPO
    REPO -->|"PagingData"| UC
    UC --> VM
    VM -->|"StateFlow"| UI

    style Android fill:#E8F5E9
    style Server fill:#FCE4EC
```

---

## 🎨 Hierarquia Visual

```
┌─────────────────────────────────────────────────────────────┐
│ ← Inventário                              🔍 📊 ⚙️          │ ← Toolbar
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Total: 1,234 | Coletados: 890 | Pendentes: 344 | 72%│   │ ← Estatísticas
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌────────┐ ┌──────────┐ ┌──────────┐                     │
│  │● Todos │ │ Coletados│ │ Pendentes│                      │ ← Chips Filtro
│  └────────┘ └──────────┘ └──────────┘                     │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 123456                              [COLETADO]        │ │
│  │ Computador Desktop Dell                               │ │
│  │ Dell | OptiPlex 7090                                  │ │
│  │ 👤 Responsável: Maria Silva                           │ │ ← Item Card
│  │ 🏢 Setor: TI  🚪 Sala: 101                           │ │
│  │ ┌─────────────────────────────────────────────────┐   │ │
│  │ │ Coletado por: João  |  Data: 15/03/2026 10:30  │   │ │ ← Info Coleta
│  │ │ Local: Sala 101     |  Estado: BOM             │   │ │
│  │ └─────────────────────────────────────────────────┘   │ │
│  │ ┌──────┐                                              │ │
│  │ │ 📷   │ ← Foto de Referência (se disponível)        │ │
│  │ └──────┘                                              │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 123457                              [PENDENTE]        │ │
│  │ Monitor LCD 24"                                       │ │
│  │ LG | 24MK430H                                         │ │ ← Item Card
│  │ 👤 Responsável: Carlos Souza                          │ │
│  │ 🏢 Setor: TI  🚪 Sala: 102                           │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │         ⟳ Carregando mais...                          │ │ ← LoadState Footer
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔀 Estados da Tela

```mermaid
graph TD
    subgraph Estados["Estados da UI"]
        E1["1️⃣ Carregamento Inicial<br/>ProgressBar visível"]
        E2["2️⃣ Lista com Dados<br/>Items + Estatísticas"]
        E3["3️⃣ Filtro Aplicado<br/>Chip ativo + lista filtrada"]
        E4["4️⃣ Lista Vazia<br/>Ícone + mensagem"]
        E5["5️⃣ Erro com Retry<br/>Mensagem + botão"]
        E6["6️⃣ Carregando Mais<br/>Footer com spinner"]
    end
    
    E1 -->|"sucesso"| E2
    E1 -->|"falha"| E5
    E2 -->|"chip tocado"| E3
    E3 -->|"sem resultados"| E4
    E2 -->|"scroll"| E6
    E6 -->|"sucesso"| E2
    E6 -->|"falha"| E5
    E5 -->|"retry"| E1
    E4 -->|"outro filtro"| E3

    style E1 fill:#FFF9C4
    style E2 fill:#C8E6C9
    style E3 fill:#E1BEE7
    style E4 fill:#FFECB3
    style E5 fill:#FFCDD2
    style E6 fill:#B3E5FC
```

---

## 🎯 Pontos de Interação

```mermaid
graph TD
    TOOLBAR["🔧 Toolbar"]
    TOOLBAR --> VOLTAR["1️⃣ ← Voltar"]
    TOOLBAR --> BUSCAR["2️⃣ 🔍 Buscar"]
    TOOLBAR --> ORDENAR["3️⃣ 📊 Ordenar"]
    TOOLBAR --> CONFIG["4️⃣ ⚙️ Config"]
    
    HEADER["📊 Header"]
    HEADER --> ESTAT["5️⃣ Estatísticas do servidor<br/>Total | Coletados | Pendentes | %"]
    
    CHIPS["🏷️ Chips"]
    CHIPS --> CHIP_ALL["6️⃣ Todos"]
    CHIPS --> CHIP_COL["6️⃣ Coletados"]
    CHIPS --> CHIP_PEND["6️⃣ Pendentes"]
    
    LISTA["📋 Lista"]
    LISTA --> ITEM["7️⃣ Item clicável → Detalhes"]
    LISTA --> FOTO_CLICK["8️⃣ Foto → Ampliar"]
    LISTA --> SCROLL["9️⃣ Scroll infinito (Paging 3)"]
    LISTA --> RETRY["🔟 Retry em erro"]

    style TOOLBAR fill:#1976D2,color:#fff
    style HEADER fill:#388E3C,color:#fff
    style CHIPS fill:#7B1FA2,color:#fff
    style LISTA fill:#F57C00,color:#fff
```

---

## 📊 Matriz de Funcionalidades

| Funcionalidade | Entrada | Processamento | Saída |
|----------------|---------|---------------|-------|
| **Paginação** | Scroll automático | PagingSource → API | Próxima página carregada |
| **Filtro Status** | Chip selecionado | flatMapLatest no ViewModel | Nova PagingData |
| **Filtro Sala** | salaId passado | PatrimonioPorSalaPagingSource | Lista filtrada por sala |
| **Estatísticas** | onViewCreated | BuscarEstatisticasDashboardUseCase | Total/Coletados/Pendentes/% |
| **Detalhes** | Toque no item | onItemClick callback | Diálogo/Navegação |
| **Foto Referência** | Item renderizado | FotoReferenciaHelper (cache) | Imagem ou card oculto |
| **Retry** | Toque no botão | pagingAdapter.retry() | Recarrega página com erro |
| **Refresh** | viewModel.refresh() | _refreshTrigger.emit() | Recarrega do início |
| **Exportar** | ExportFragment | GerarRelatorioUseCase | PDF/Excel(TSV)/CSV |

---

## 📦 Componentes Implementados

### Fragments
| Componente | Tipo | Descrição |
|------------|------|-----------|
| `PatrimonioListPagingFragment` | Fragment | Lista com Paging 3 (recomendado) |
| `PatrimonioListFragment` | Fragment | Lista com scroll infinito manual (legado) |
| `ExportFragment` | Fragment | Exportação multi-formato |

### ViewModels
| Componente | Tipo | Descrição |
|------------|------|-----------|
| `PatrimonioListViewModelPaging` | @HiltViewModel | Paging 3 + filtros reativos |
| `PatrimonioListViewModel` | @HiltViewModel | Paginação manual (legado) |
| `ExportViewModel` | @HiltViewModel | Geração de relatórios |

### Adapters
| Componente | Tipo | Descrição |
|------------|------|-----------|
| `PatrimonioPagingAdapter` | PagingDataAdapter | Paging 3 automático |
| `PatrimonioLoadStateAdapter` | LoadStateAdapter | Footer loading/erro/retry |
| `PatrimonioAdapter` (inventario) | ListAdapter | Com foto de referência |
| `PatrimonioAdapter` (adapter) | ListAdapter | Básico com info coleta |

### Use Cases
| Componente | Descrição |
|------------|-----------|
| `BuscarPatrimoniosPaginadoUseCase` | Paging 3 (todos e por sala) |
| `BuscarEstatisticasDashboardUseCase` | Totais do servidor |
| `GerarRelatorioUseCase` | Exportação PDF/CSV/Excel |
| `BuscarSalasParaExportacaoUseCase` | Salas para filtro de export |

---

## 🔄 Ciclo de Vida do Filtro Reativo

```mermaid
flowchart TD
    A["Usuário toca Chip"] --> B["Fragment chama<br/>viewModel.filtrarPorColetado()"]
    B --> C["_filtroColetado.value = novo valor"]
    C --> D["combine() detecta mudança"]
    D --> E["flatMapLatest cria<br/>nova PagingSource"]
    E --> F["PagingSource busca<br/>dados filtrados da API"]
    F --> G["PagingData emitida"]
    G --> H["cachedIn(viewModelScope)"]
    H --> I["Fragment collectLatest"]
    I --> J["adapter.submitData()"]
    J --> K["RecyclerView atualiza<br/>com animação DiffUtil"]

    style A fill:#E1BEE7
    style E fill:#C8E6C9
    style K fill:#B3E5FC
```

---

## 🎨 Paleta de Cores

```mermaid
pie title Distribuição de Status
    "Coletados (verde)" : 72
    "Pendentes (laranja)" : 28
```

| Cor | Hex | Uso |
|-----|-----|-----|
| 🟢 success | `#4CAF50` | COLETADO |
| 🟠 warning | `#FF9800` | PENDENTE |
| 🔵 primary | `#2196F3` | Número patrimônio |
| ⚫ secondary | `#757575` | Texto secundário |
| 🟢 bg_success | `status_background_success` | Fundo status coletado |
| 🟠 bg_warning | `status_background_warning` | Fundo status pendente |

---

## 📐 Dimensões e Espaçamentos

```
Card de Item:
┌─────────────────────────────────────┐
│ ↕ 16dp padding                      │
│ ← 16dp → [Conteúdo] ← 16dp →       │
│ ↕ 16dp padding                      │
└─────────────────────────────────────┘
↕ 8dp margin

Ícones:
• Toolbar: 24dp x 24dp
• Card: 16dp x 16dp
• Foto Referência: 80dp x 80dp (thumbnail)

Textos:
• Número: 18sp (bold, azul)
• Descrição: 16sp
• Marca/Modelo: 14sp
• Setor/Sala: 12sp
• Status: 12sp (bold, cor contextual)
• Info Coleta: 11sp (cinza)

Chips:
• Altura: 32dp
• Padding horizontal: 12dp
• Espaçamento entre chips: 8dp
```

---

## ⚡ Melhorias v2.23.0 vs v1.3.0

```mermaid
graph LR
    subgraph v1["v1.3.0 (Antigo)"]
        A1["Paginação Manual<br/>InfiniteScrollListener"]
        A2["Filtros por Diálogo"]
        A3["Contagem Local"]
        A4["Sem Fotos"]
        A5["Sem Info Coleta"]
        A6["Retry Manual"]
        A7["ViewModel Direto"]
        A8["Sem Offline"]
    end
    
    subgraph v2["v2.23.0 (Atual)"]
        B1["Paging 3<br/>Automático"]
        B2["Chips Reativos<br/>flatMapLatest"]
        B3["Endpoint Servidor"]
        B4["FotoReferenciaHelper<br/>+ Cache"]
        B5["Coletado por, Data,<br/>Local, Estado"]
        B6["LoadStateAdapter<br/>Automático"]
        B7["Clean Architecture<br/>+ Hilt"]
        B8["Room + Offline-first"]
    end
    
    A1 -.->|"migrado"| B1
    A2 -.->|"migrado"| B2
    A3 -.->|"migrado"| B3
    A4 -.->|"novo"| B4
    A5 -.->|"novo"| B5
    A6 -.->|"migrado"| B6
    A7 -.->|"migrado"| B7
    A8 -.->|"novo"| B8

    style v1 fill:#FFCDD2
    style v2 fill:#C8E6C9
```

| Aspecto | v1.3.0 | v2.23.0 |
|---------|--------|---------|
| **Paginação** | Manual (InfiniteScrollListener) | Paging 3 (automático) |
| **Filtros** | Diálogo de ordenação | Chips reativos (flatMapLatest) |
| **Estatísticas** | Contagem local | Endpoint do servidor |
| **Fotos** | Não suportado | FotoReferenciaHelper com cache |
| **Info Coleta** | Não exibido | Coletado por, data, local, estado |
| **Retry** | Manual | Automático (LoadStateAdapter) |
| **Arquitetura** | ViewModel direto | Clean Architecture + Hilt |
| **Offline** | Não suportado | Room + offline-first |
| **Exportação** | Não suportado | PDF, Excel (TSV), CSV |
| **DI** | Manual/Factory | Hilt (@AndroidEntryPoint) |

---

## 🧪 Testes Recomendados

### Teste 1: Paginação
```
1. Abrir tela de inventário
2. Verificar que primeira página carrega (20 itens)
3. Scroll até o final
4. Verificar que LoadState footer aparece
5. Verificar que próxima página carrega automaticamente
6. Repetir até última página
```

### Teste 2: Filtros
```
1. Tocar chip "Coletados"
2. Verificar que apenas itens coletados aparecem
3. Tocar chip "Pendentes"
4. Verificar que apenas itens pendentes aparecem
5. Tocar chip "Todos"
6. Verificar que todos os itens aparecem
```

### Teste 3: Erro e Retry
```
1. Desconectar internet
2. Scroll para carregar mais
3. Verificar que footer de erro aparece
4. Reconectar internet
5. Tocar "Tentar novamente"
6. Verificar que dados carregam
```

### Teste 4: Foto de Referência
```
1. Abrir lista com PatrimonioAdapter (inventario)
2. Verificar que fotos carregam assincronamente
3. Tocar na foto
4. Verificar que amplia
5. Scroll rápido (verificar cancelamento de jobs)
```

### Teste 5: Exportação
```
1. Navegar para ExportFragment
2. Selecionar formato (PDF/Excel/CSV)
3. Aplicar filtro (Todos/Coletados/Pendentes)
4. Gerar relatório
5. Verificar arquivo gerado
6. Compartilhar/Abrir
```

---

**Versão**: 2.23.0  
**Data**: 09/05/2026  
**Tipo**: Diagrama Visual + Arquitetura + Mermaid  
**Status**: ✅ Atualizado com implementação real
