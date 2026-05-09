# Design Document - Histórico de Coletas de Patrimônio

## Overview

Este documento descreve o design da funcionalidade de histórico de coletas de patrimônio, que permitirá aos usuários visualizar, filtrar e exportar o histórico completo de verificações físicas de um patrimônio ao longo de múltiplos inventários.

**Escopo de Implementação:**
- **Prioridade 1 (Obrigatório)**: Sistema Desktop (Java Swing)
- **Prioridade 2 (Futuro)**: Aplicativo Mobile (Kotlin/Android)

A solução será implementada primeiramente no sistema desktop (Java Swing), seguindo os padrões arquiteturais existentes. A implementação mobile está documentada para referência futura, mas não faz parte do escopo inicial.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │  Desktop (Swing) │         │  Mobile (Android)│         │
│  │  - HistoricoFrame│         │  - HistoricoFrag │         │
│  │  - HistoricoPanel│         │  - HistoricoVM   │         │
│  └──────────────────┘         └──────────────────┘         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  HistoricoColetaService                               │  │
│  │  - buscarHistorico(patrimonioId, filtros)            │  │
│  │  - compararColetas(coletaId1, coletaId2)             │  │
│  │  - exportarHistorico(patrimonioId, formato)          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  HistoricoColetaDAO                                   │  │
│  │  - buscarColetasPorPatrimonio(patrimonioId)          │  │
│  │  - buscarColetasComFiltros(patrimonioId, filtros)    │  │
│  │  - buscarColetaPorId(coletaId)                        │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Database                                │
│  tabela_coleta (já existe)                                   │
│  + índices para otimização de queries                        │
└─────────────────────────────────────────────────────────────┘
```

### Mobile Architecture (Clean Architecture + MVVM)

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  HistoricoFragment                                    │  │
│  │  - RecyclerView com adapter                           │  │
│  │  - Filtros (inventário, período, coletor)            │  │
│  │  - Botões de exportação                               │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↓                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  HistoricoViewModel (@HiltViewModel)                  │  │
│  │  - StateFlow<HistoricoState>                          │  │
│  │  - carregarHistorico()                                │  │
│  │  - aplicarFiltros()                                   │  │
│  │  - exportar()                                         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Use Cases                                            │  │
│  │  - BuscarHistoricoColetaUseCase                       │  │
│  │  - FiltrarHistoricoUseCase                            │  │
│  │  - ExportarHistoricoUseCase                           │  │
│  │  - CompararColetasUseCase                             │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Models                                               │  │
│  │  - HistoricoColeta                                    │  │
│  │  - FiltroHistorico                                    │  │
│  │  - ComparacaoColeta                                   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  HistoricoRepository (interface)                      │  │
│  │  HistoricoRepositoryImpl                              │  │
│  │  - Local: Room DAO                                    │  │
│  │  - Remote: Retrofit API                               │  │
│  │  - Strategy: Offline-first                            │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### Backend Components (Java)

#### 1. HistoricoColetaService

```java
public class HistoricoColetaService {
    
    private final HistoricoColetaDAO historicoDAO;
    private final PatrimonioDAO patrimonioDAO;
    
    /**
     * Busca histórico completo de coletas de um patrimônio
     */
    public List<HistoricoColetaDTO> buscarHistorico(
        Integer patrimonioId,
        FiltroHistoricoDTO filtros
    ) throws ServiceException;
    
    /**
     * Compara duas coletas consecutivas e identifica mudanças
     */
    public ComparacaoColetaDTO compararColetas(
        Integer coletaId1,
        Integer coletaId2
    ) throws ServiceException;
    
    /**
     * Exporta histórico em formato especificado
     */
    public byte[] exportarHistorico(
        Integer patrimonioId,
        FormatoExportacao formato,
        FiltroHistoricoDTO filtros
    ) throws ServiceException;
    
    /**
     * Busca estatísticas do histórico
     */
    public EstatisticasHistoricoDTO buscarEstatisticas(
        Integer patrimonioId
    ) throws ServiceException;
}
```

#### 2. HistoricoColetaDAO

```java
public class HistoricoColetaDAO {
    
    /**
     * Busca todas as coletas de um patrimônio ordenadas por data
     */
    public List<Map<String, Object>> buscarColetasPorPatrimonio(
        Integer patrimonioId,
        int offset,
        int limit
    ) throws SQLException;
    
    /**
     * Busca coletas com filtros aplicados
     */
    public List<Map<String, Object>> buscarColetasComFiltros(
        Integer patrimonioId,
        Integer inventarioId,
        Integer coletorId,
        Date dataInicio,
        Date dataFim,
        int offset,
        int limit
    ) throws SQLException;
    
    /**
     * Conta total de coletas de um patrimônio
     */
    public int contarColetas(
        Integer patrimonioId,
        FiltroHistoricoDTO filtros
    ) throws SQLException;
    
    /**
     * Busca coleta anterior a uma data específica
     */
    public Map<String, Object> buscarColetaAnterior(
        Integer patrimonioId,
        Date dataReferencia
    ) throws SQLException;
}
```

### Mobile Components (Kotlin)

#### 1. Domain Models

```kotlin
/**
 * Modelo de domínio para histórico de coleta
 */
data class HistoricoColeta(
    val id: Int,
    val patrimonioId: Int,
    val numeroPatrimonio: String,
    val descricaoPatrimonio: String,
    val inventarioId: Int,
    val nomeInventario: String,
    val coletorId: Int,
    val nomeColetorCompleto: String,
    val dataColeta: Long,
    val localizacaoEncontrada: String?,
    val estadoEncontrado: String?,
    val observacoes: String?,
    val salaId: Int?,
    val nomeSala: String?,
    val setorId: Int?,
    val nomeSetor: String?
) {
    fun temMudancaLocalizacao(anterior: HistoricoColeta?): Boolean {
        return anterior != null && 
               localizacaoEncontrada != anterior.localizacaoEncontrada
    }
    
    fun temMudancaEstado(anterior: HistoricoColeta?): Boolean {
        return anterior != null && 
               estadoEncontrado != anterior.estadoEncontrado
    }
}

/**
 * Filtros para histórico
 */
data class FiltroHistorico(
    val inventarioId: Int? = null,
    val coletorId: Int? = null,
    val dataInicio: Long? = null,
    val dataFim: Long? = null
)

/**
 * Comparação entre duas coletas
 */
data class ComparacaoColeta(
    val coletaAtual: HistoricoColeta,
    val coletaAnterior: HistoricoColeta?,
    val mudancas: List<Mudanca>
) {
    data class Mudanca(
        val campo: String,
        val valorAnterior: String?,
        val valorAtual: String?,
        val tipo: TipoMudanca
    )
    
    enum class TipoMudanca {
        LOCALIZACAO,
        ESTADO,
        OBSERVACAO
    }
}
```

#### 2. Use Cases

```kotlin
/**
 * Use Case: Buscar histórico de coletas de um patrimônio
 */
class BuscarHistoricoColetaUseCase @Inject constructor(
    private val historicoRepository: HistoricoRepository
) {
    suspend operator fun invoke(
        patrimonioId: Int,
        filtros: FiltroHistorico? = null
    ): Result<List<HistoricoColeta>> {
        return try {
            // Validar entrada
            if (patrimonioId <= 0) {
                return Result.failure(
                    IllegalArgumentException("ID do patrimônio inválido")
                )
            }
            
            // Buscar histórico
            val historico = historicoRepository.buscarHistorico(
                patrimonioId,
                filtros
            )
            
            // Ordenar por data (mais recente primeiro)
            val historicoOrdenado = historico.sortedByDescending { 
                it.dataColeta 
            }
            
            Result.success(historicoOrdenado)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use Case: Comparar coletas consecutivas
 */
class CompararColetasUseCase @Inject constructor() {
    
    operator fun invoke(
        coletaAtual: HistoricoColeta,
        coletaAnterior: HistoricoColeta?
    ): ComparacaoColeta {
        
        val mudancas = mutableListOf<ComparacaoColeta.Mudanca>()
        
        // Verificar mudança de localização
        if (coletaAtual.temMudancaLocalizacao(coletaAnterior)) {
            mudancas.add(
                ComparacaoColeta.Mudanca(
                    campo = "Localização",
                    valorAnterior = coletaAnterior?.localizacaoEncontrada,
                    valorAtual = coletaAtual.localizacaoEncontrada,
                    tipo = ComparacaoColeta.TipoMudanca.LOCALIZACAO
                )
            )
        }
        
        // Verificar mudança de estado
        if (coletaAtual.temMudancaEstado(coletaAnterior)) {
            mudancas.add(
                ComparacaoColeta.Mudanca(
                    campo = "Estado",
                    valorAnterior = coletaAnterior?.estadoEncontrado,
                    valorAtual = coletaAtual.estadoEncontrado,
                    tipo = ComparacaoColeta.TipoMudanca.ESTADO
                )
            )
        }
        
        return ComparacaoColeta(
            coletaAtual = coletaAtual,
            coletaAnterior = coletaAnterior,
            mudancas = mudancas
        )
    }
}

/**
 * Use Case: Exportar histórico
 */
class ExportarHistoricoUseCase @Inject constructor(
    private val historicoRepository: HistoricoRepository,
    private val pdfGenerator: PdfGenerator,
    private val excelGenerator: ExcelGenerator
) {
    suspend operator fun invoke(
        patrimonioId: Int,
        formato: FormatoExportacao,
        filtros: FiltroHistorico? = null
    ): Result<File> {
        return try {
            // Buscar histórico
            val historico = historicoRepository.buscarHistorico(
                patrimonioId,
                filtros
            )
            
            if (historico.isEmpty()) {
                return Result.failure(
                    IllegalStateException("Nenhuma coleta encontrada")
                )
            }
            
            // Gerar arquivo conforme formato
            val arquivo = when (formato) {
                FormatoExportacao.PDF -> pdfGenerator.gerar(historico)
                FormatoExportacao.EXCEL -> excelGenerator.gerar(historico)
            }
            
            Result.success(arquivo)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 3. ViewModel

```kotlin
@HiltViewModel
class HistoricoViewModel @Inject constructor(
    private val buscarHistoricoUseCase: BuscarHistoricoColetaUseCase,
    private val compararColetasUseCase: CompararColetasUseCase,
    private val exportarHistoricoUseCase: ExportarHistoricoUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<HistoricoState>(HistoricoState.Idle)
    val state: StateFlow<HistoricoState> = _state.asStateFlow()
    
    private var historicoCompleto: List<HistoricoColeta> = emptyList()
    private var filtroAtual: FiltroHistorico? = null
    
    fun carregarHistorico(patrimonioId: Int) {
        viewModelScope.launch {
            _state.value = HistoricoState.Loading
            
            buscarHistoricoUseCase(patrimonioId, filtroAtual).fold(
                onSuccess = { historico ->
                    historicoCompleto = historico
                    
                    // Comparar coletas consecutivas
                    val historicoComComparacao = historico.mapIndexed { index, coleta ->
                        val anterior = historico.getOrNull(index + 1)
                        compararColetasUseCase(coleta, anterior)
                    }
                    
                    _state.value = HistoricoState.Success(
                        historico = historicoComComparacao,
                        totalColetas = historico.size
                    )
                },
                onFailure = { error ->
                    _state.value = HistoricoState.Error(
                        error.message ?: "Erro ao carregar histórico"
                    )
                }
            )
        }
    }
    
    fun aplicarFiltros(filtros: FiltroHistorico) {
        filtroAtual = filtros
        // Recarregar com novos filtros
        // (patrimonioId deve ser mantido no estado)
    }
    
    fun exportar(patrimonioId: Int, formato: FormatoExportacao) {
        viewModelScope.launch {
            _state.value = HistoricoState.Exporting
            
            exportarHistoricoUseCase(patrimonioId, formato, filtroAtual).fold(
                onSuccess = { arquivo ->
                    _state.value = HistoricoState.ExportSuccess(arquivo)
                },
                onFailure = { error ->
                    _state.value = HistoricoState.Error(
                        "Erro ao exportar: ${error.message}"
                    )
                }
            )
        }
    }
}

/**
 * Estados da UI
 */
sealed class HistoricoState {
    object Idle : HistoricoState()
    object Loading : HistoricoState()
    data class Success(
        val historico: List<ComparacaoColeta>,
        val totalColetas: Int
    ) : HistoricoState()
    object Exporting : HistoricoState()
    data class ExportSuccess(val arquivo: File) : HistoricoState()
    data class Error(val message: String) : HistoricoState()
}
```

### Desktop Components (Java Swing)

#### 1. HistoricoColetaPanel

```java
public class HistoricoColetaPanel extends JPanel {
    
    private final HistoricoColetaService historicoService;
    private final Integer patrimonioId;
    
    private JTable tabelaHistorico;
    private HistoricoTableModel tableModel;
    private JComboBox<String> comboInventario;
    private JComboBox<String> comboColetor;
    private JDateChooser dataInicio;
    private JDateChooser dataFim;
    private JButton btnExportarPDF;
    private JButton btnExportarExcel;
    private JButton btnLimparFiltros;
    
    public HistoricoColetaPanel(Integer patrimonioId) {
        this.patrimonioId = patrimonioId;
        this.historicoService = new HistoricoColetaService();
        
        initComponents();
        carregarHistorico();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel de filtros no topo
        add(criarPainelFiltros(), BorderLayout.NORTH);
        
        // Tabela no centro
        add(criarPainelTabela(), BorderLayout.CENTER);
        
        // Botões de ação no rodapé
        add(criarPainelAcoes(), BorderLayout.SOUTH);
    }
    
    private void carregarHistorico() {
        SwingWorker<List<HistoricoColetaDTO>, Void> worker = 
            new SwingWorker<>() {
                @Override
                protected List<HistoricoColetaDTO> doInBackground() {
                    FiltroHistoricoDTO filtros = obterFiltrosAtuais();
                    return historicoService.buscarHistorico(
                        patrimonioId, 
                        filtros
                    );
                }
                
                @Override
                protected void done() {
                    try {
                        List<HistoricoColetaDTO> historico = get();
                        tableModel.setData(historico);
                        aplicarDestaquesVisuais();
                    } catch (Exception e) {
                        mostrarErro("Erro ao carregar histórico", e);
                    }
                }
            };
        
        worker.execute();
    }
    
    private void aplicarDestaquesVisuais() {
        // Aplicar cores e ícones para mudanças
        tabelaHistorico.setDefaultRenderer(
            Object.class,
            new HistoricoTableCellRenderer()
        );
    }
}
```

## Data Models

### Database Schema (Existing)

```sql
-- Tabela já existe, apenas adicionar índices para otimização

-- Índice para buscar coletas por patrimônio (já existe)
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio 
ON tabela_coleta(id_patrimonio);

-- Índice para buscar coletas por inventário
CREATE INDEX IF NOT EXISTS idx_coleta_inventario 
ON tabela_coleta(id_inventario);

-- Índice para buscar coletas por data
CREATE INDEX IF NOT EXISTS idx_coleta_data 
ON tabela_coleta(data_coleta);

-- Índice composto para queries com filtros múltiplos
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio_inventario_data 
ON tabela_coleta(id_patrimonio, id_inventario, data_coleta DESC);
```

### DTOs

```java
// Backend DTO
public class HistoricoColetaDTO {
    private Integer id;
    private Integer patrimonioId;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private Integer inventarioId;
    private String nomeInventario;
    private Integer coletorId;
    private String nomeColetorCompleto;
    private Date dataColeta;
    private String localizacaoEncontrada;
    private String estadoEncontrado;
    private String observacoes;
    private Integer salaId;
    private String nomeSala;
    private Integer setorId;
    private String nomeSetor;
    
    // Campos de comparação
    private Boolean temMudancaLocalizacao;
    private Boolean temMudancaEstado;
    private String localizacaoAnterior;
    private String estadoAnterior;
}

public class FiltroHistoricoDTO {
    private Integer inventarioId;
    private Integer coletorId;
    private Date dataInicio;
    private Date dataFim;
    private Integer offset;
    private Integer limit;
}

public class EstatisticasHistoricoDTO {
    private Integer totalColetas;
    private Integer totalInventarios;
    private Date primeiraColeta;
    private Date ultimaColeta;
    private Integer totalMudancasLocalizacao;
    private Integer totalMudancasEstado;
}
```

## Correctness Properties

*Uma propriedade é uma característica ou comportamento que deve ser verdadeiro em todas as execuções válidas do sistema - essencialmente, uma declaração formal sobre o que o sistema deve fazer. Propriedades servem como ponte entre especificações legíveis por humanos e garantias de corretude verificáveis por máquina.*

### Property 1: Ordenação Cronológica do Histórico
*Para qualquer* patrimônio com múltiplas coletas, o histórico retornado deve estar ordenado por data de coleta em ordem decrescente (mais recente primeiro).

**Validates: Requirements 1.2**

### Property 2: Completude dos Dados de Coleta
*Para qualquer* coleta exibida no histórico, todos os campos obrigatórios devem estar presentes: data/hora, inventário, coletor, localização encontrada, estado do patrimônio, e se houver observações, elas devem estar incluídas.

**Validates: Requirements 1.3, 2.1, 2.2, 2.3, 2.4, 2.5**

### Property 3: Detecção de Mudanças Entre Coletas
*Para quaisquer* duas coletas consecutivas de um patrimônio, se houver diferença em localização ou estado, o sistema deve identificar e marcar a mudança com indicadores apropriados.

**Validates: Requirements 3.1, 3.2, 3.3**

### Property 4: Ausência de Indicadores Quando Não Há Mudanças
*Para quaisquer* duas coletas consecutivas com mesma localização e mesmo estado, os campos de indicação de mudança devem ser false ou null.

**Validates: Requirements 3.4**

### Property 5: Filtragem Correta do Histórico
*Para qualquer* conjunto de filtros válidos (inventário, período, coletor), o histórico retornado deve conter apenas coletas que atendem a todos os critérios especificados.

**Validates: Requirements 4.4**

### Property 6: Round-Trip de Filtros
*Para qualquer* histórico, aplicar filtros vazios ou nulos deve retornar o mesmo resultado que não aplicar filtros (histórico completo).

**Validates: Requirements 4.5**

### Property 7: Exportação Multi-Formato
*Para qualquer* histórico válido e formato especificado (PDF ou Excel), o sistema deve gerar um arquivo válido do formato solicitado contendo todos os dados do histórico.

**Validates: Requirements 5.1, 5.2, 5.3**

### Property 8: Completude da Exportação
*Para qualquer* histórico exportado, o arquivo gerado deve conter o mesmo número de coletas que o histórico original e incluir cabeçalho com dados do patrimônio (número e descrição).

**Validates: Requirements 5.3, 5.4**

### Property 9: Estratégia Offline-First no Mobile
*Para qualquer* requisição de histórico no mobile, o sistema deve tentar carregar do servidor primeiro e, em caso de falha de conexão, usar dados do cache local.

**Validates: Requirements 6.2, 6.3**

### Property 10: Paginação Consistente
*Para qualquer* patrimônio com mais de N coletas (onde N é o tamanho da página), a primeira requisição deve retornar exatamente N registros, e requisições subsequentes devem retornar até N registros adicionais sem duplicação.

**Validates: Requirements 1.5, 6.5, 8.1, 8.2**

### Property 11: Destaque da Coleta Mais Recente
*Para qualquer* histórico não vazio, a primeira coleta (mais recente) deve ter um indicador especial que a diferencia das demais.

**Validates: Requirements 7.1**

### Property 12: Detecção de Intervalos Longos
*Para quaisquer* duas coletas consecutivas com intervalo maior que X dias (configurável), o sistema deve marcar com alerta visual.

**Validates: Requirements 7.3**

### Property 13: Classificação de Mudanças de Estado
*Para quaisquer* duas coletas consecutivas com estados diferentes, o sistema deve classificar a mudança como melhora (estado melhor) ou piora (estado pior) e aplicar indicador visual apropriado.

**Validates: Requirements 7.4, 7.5**

### Property 14: Imutabilidade dos Dados Históricos
*Para qualquer* coleta no histórico, os dados exibidos devem ser idênticos aos dados originais registrados no momento da coleta (round-trip property).

**Validates: Requirements 9.3**

### Property 15: Auditoria de Acessos e Modificações
*Para qualquer* acesso ao histórico ou tentativa de modificação de coleta, o sistema deve registrar entrada no log de auditoria contendo timestamp, usuário e ação realizada.

**Validates: Requirements 9.4, 9.5**

### Property 16: Contexto do Patrimônio Sempre Presente
*Para qualquer* histórico exibido, os dados contextuais do patrimônio (número, descrição) devem estar presentes e visíveis.

**Validates: Requirements 10.3**

### Property 17: Contador de Coletas Recentes
*Para qualquer* patrimônio com coletas nos últimos 30 dias, o sistema deve exibir contador ou badge indicando a quantidade de coletas recentes.

**Validates: Requirements 10.5**

### Property 18: Cache Efetivo
*Para qualquer* histórico acessado repetidamente dentro de um período de cache (configurável), a segunda requisição deve ser atendida pelo cache sem consultar o banco de dados.

**Validates: Requirements 8.4**

### Property 19: Limite para Históricos Grandes
*Para qualquer* patrimônio com mais de 1000 coletas, o sistema deve limitar a visualização inicial e sugerir uso de filtros para refinar a busca.

**Validates: Requirements 8.5**

### Property 20: Integridade dos Dados de Auditoria
*Para qualquer* coleta registrada, o sistema deve armazenar timestamp preciso e ID do usuário coletor, e esses dados devem ser imutáveis após o registro.

**Validates: Requirements 9.1, 9.2**

## Error Handling

### Backend Error Handling

```java
public class HistoricoColetaService {
    
    public List<HistoricoColetaDTO> buscarHistorico(
        Integer patrimonioId,
        FiltroHistoricoDTO filtros
    ) throws ServiceException {
        
        // Validar entrada
        if (patrimonioId == null || patrimonioId <= 0) {
            throw new ServiceException(
                "ID do patrimônio inválido",
                ErrorCode.INVALID_INPUT
            );
        }
        
        try {
            // Verificar se patrimônio existe
            if (!patrimonioDAO.existe(patrimonioId)) {
                throw new ServiceException(
                    "Patrimônio não encontrado",
                    ErrorCode.NOT_FOUND
                );
            }
            
            // Buscar histórico
            List<Map<String, Object>> coletas = 
                historicoDAO.buscarColetasComFiltros(
                    patrimonioId,
                    filtros.getInventarioId(),
                    filtros.getColetorId(),
                    filtros.getDataInicio(),
                    filtros.getDataFim(),
                    filtros.getOffset(),
                    filtros.getLimit()
                );
            
            // Converter para DTO
            return coletas.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
                
        } catch (SQLException e) {
            logger.error("Erro ao buscar histórico", e);
            throw new ServiceException(
                "Erro ao buscar histórico de coletas",
                ErrorCode.DATABASE_ERROR,
                e
            );
        }
    }
}
```

### Mobile Error Handling

```kotlin
class BuscarHistoricoColetaUseCase @Inject constructor(
    private val historicoRepository: HistoricoRepository
) {
    suspend operator fun invoke(
        patrimonioId: Int,
        filtros: FiltroHistorico? = null
    ): Result<List<HistoricoColeta>> {
        return try {
            // Validar entrada
            if (patrimonioId <= 0) {
                return Result.failure(
                    IllegalArgumentException("ID do patrimônio inválido")
                )
            }
            
            // Buscar histórico (offline-first)
            val historico = try {
                // Tentar servidor primeiro
                historicoRepository.buscarHistoricoRemoto(
                    patrimonioId,
                    filtros
                )
            } catch (e: IOException) {
                // Fallback para cache local
                logger.warn("Servidor indisponível, usando cache", e)
                historicoRepository.buscarHistoricoLocal(
                    patrimonioId,
                    filtros
                )
            }
            
            // Validar resultado
            if (historico.isEmpty()) {
                logger.info("Nenhuma coleta encontrada para patrimônio $patrimonioId")
            }
            
            // Ordenar por data
            val historicoOrdenado = historico.sortedByDescending { 
                it.dataColeta 
            }
            
            Result.success(historicoOrdenado)
            
        } catch (e: IllegalArgumentException) {
            logger.error("Entrada inválida", e)
            Result.failure(e)
        } catch (e: Exception) {
            logger.error("Erro inesperado ao buscar histórico", e)
            Result.failure(
                RuntimeException("Erro ao buscar histórico", e)
            )
        }
    }
}
```

### Error Codes

```java
public enum ErrorCode {
    INVALID_INPUT("Entrada inválida"),
    NOT_FOUND("Recurso não encontrado"),
    DATABASE_ERROR("Erro no banco de dados"),
    EXPORT_ERROR("Erro ao exportar arquivo"),
    CACHE_ERROR("Erro no cache"),
    NETWORK_ERROR("Erro de conexão");
    
    private final String message;
    
    ErrorCode(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
```

## Testing Strategy

### Dual Testing Approach

O sistema utilizará **testes unitários** e **testes baseados em propriedades** de forma complementar:

- **Testes Unitários**: Verificam exemplos específicos, casos extremos e condições de erro
- **Testes de Propriedade**: Verificam propriedades universais através de múltiplas entradas geradas

### Unit Testing

#### Backend (JUnit 5)

```java
@ExtendWith(MockitoExtension.class)
class HistoricoColetaServiceTest {
    
    @Mock
    private HistoricoColetaDAO historicoDAO;
    
    @Mock
    private PatrimonioDAO patrimonioDAO;
    
    @InjectMocks
    private HistoricoColetaService service;
    
    @Test
    @DisplayName("Deve retornar histórico vazio quando patrimônio não tem coletas")
    void deveRetornarHistoricoVazio() throws Exception {
        // Given
        Integer patrimonioId = 1;
        when(patrimonioDAO.existe(patrimonioId)).thenReturn(true);
        when(historicoDAO.buscarColetasPorPatrimonio(
            eq(patrimonioId), anyInt(), anyInt()
        )).thenReturn(Collections.emptyList());
        
        // When
        List<HistoricoColetaDTO> resultado = 
            service.buscarHistorico(patrimonioId, new FiltroHistoricoDTO());
        
        // Then
        assertTrue(resultado.isEmpty());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando patrimônio não existe")
    void deveLancarExcecaoQuandoPatrimonioNaoExiste() {
        // Given
        Integer patrimonioId = 999;
        when(patrimonioDAO.existe(patrimonioId)).thenReturn(false);
        
        // When/Then
        assertThrows(ServiceException.class, () -> {
            service.buscarHistorico(patrimonioId, new FiltroHistoricoDTO());
        });
    }
    
    @Test
    @DisplayName("Deve identificar mudança de localização entre coletas")
    void deveIdentificarMudancaLocalizacao() throws Exception {
        // Given
        HistoricoColetaDTO coleta1 = criarColeta(1, "Sala 101");
        HistoricoColetaDTO coleta2 = criarColeta(2, "Sala 102");
        
        // When
        ComparacaoColetaDTO comparacao = 
            service.compararColetas(coleta1.getId(), coleta2.getId());
        
        // Then
        assertTrue(comparacao.getTemMudancaLocalizacao());
        assertEquals("Sala 101", comparacao.getLocalizacaoAnterior());
        assertEquals("Sala 102", comparacao.getLocalizacaoAtual());
    }
}
```

#### Mobile (JUnit 4 + Kotlin)

```kotlin
@RunWith(MockitoJUnitRunner::class)
class BuscarHistoricoColetaUseCaseTest {
    
    @Mock
    private lateinit var historicoRepository: HistoricoRepository
    
    private lateinit var useCase: BuscarHistoricoColetaUseCase
    
    @Before
    fun setup() {
        useCase = BuscarHistoricoColetaUseCase(historicoRepository)
    }
    
    @Test
    fun `deve retornar erro quando patrimonio id invalido`() = runTest {
        // Given
        val patrimonioId = -1
        
        // When
        val result = useCase(patrimonioId)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `deve retornar historico ordenado por data decrescente`() = runTest {
        // Given
        val patrimonioId = 1
        val coletas = listOf(
            criarColeta(id = 1, data = 1000L),
            criarColeta(id = 2, data = 3000L),
            criarColeta(id = 3, data = 2000L)
        )
        whenever(historicoRepository.buscarHistoricoRemoto(patrimonioId, null))
            .thenReturn(coletas)
        
        // When
        val result = useCase(patrimonioId)
        
        // Then
        assertTrue(result.isSuccess)
        val historico = result.getOrNull()!!
        assertEquals(3000L, historico[0].dataColeta) // Mais recente primeiro
        assertEquals(2000L, historico[1].dataColeta)
        assertEquals(1000L, historico[2].dataColeta)
    }
    
    @Test
    fun `deve usar cache quando servidor indisponivel`() = runTest {
        // Given
        val patrimonioId = 1
        val coletasCache = listOf(criarColeta(id = 1))
        whenever(historicoRepository.buscarHistoricoRemoto(patrimonioId, null))
            .thenThrow(IOException("Sem conexão"))
        whenever(historicoRepository.buscarHistoricoLocal(patrimonioId, null))
            .thenReturn(coletasCache)
        
        // When
        val result = useCase(patrimonioId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        verify(historicoRepository).buscarHistoricoLocal(patrimonioId, null)
    }
}
```

### Property-Based Testing

#### Configuração

- **Framework Backend**: JUnit-Quickcheck ou jqwik
- **Framework Mobile**: Kotest Property Testing
- **Iterações mínimas**: 100 por propriedade
- **Tag format**: `Feature: historico-coleta-patrimonio, Property {number}: {property_text}`

#### Backend Property Tests (jqwik)

```java
@PropertyTest
@Tag("Feature: historico-coleta-patrimonio, Property 1: Ordenação Cronológica")
void historicoSempreOrdenadoPorDataDecrescente(
    @ForAll @IntRange(min = 1, max = 100) int patrimonioId,
    @ForAll @Size(min = 2, max = 50) List<@From("coletaGenerator") Coleta> coletas
) {
    // Given: patrimônio com múltiplas coletas
    salvarColetas(patrimonioId, coletas);
    
    // When: buscar histórico
    List<HistoricoColetaDTO> historico = 
        service.buscarHistorico(patrimonioId, new FiltroHistoricoDTO());
    
    // Then: deve estar ordenado por data decrescente
    for (int i = 0; i < historico.size() - 1; i++) {
        Date dataAtual = historico.get(i).getDataColeta();
        Date dataProxima = historico.get(i + 1).getDataColeta();
        assertTrue(
            dataAtual.compareTo(dataProxima) >= 0,
            "Histórico não está ordenado corretamente"
        );
    }
}

@PropertyTest
@Tag("Feature: historico-coleta-patrimonio, Property 2: Completude dos Dados")
void todasColetasDevemTerCamposObrigatorios(
    @ForAll @IntRange(min = 1, max = 100) int patrimonioId,
    @ForAll @Size(min = 1, max = 20) List<@From("coletaGenerator") Coleta> coletas
) {
    // Given: patrimônio com coletas
    salvarColetas(patrimonioId, coletas);
    
    // When: buscar histórico
    List<HistoricoColetaDTO> historico = 
        service.buscarHistorico(patrimonioId, new FiltroHistoricoDTO());
    
    // Then: todas as coletas devem ter campos obrigatórios
    for (HistoricoColetaDTO coleta : historico) {
        assertNotNull(coleta.getDataColeta(), "Data não pode ser nula");
        assertNotNull(coleta.getInventarioId(), "Inventário não pode ser nulo");
        assertNotNull(coleta.getNomeInventario(), "Nome do inventário não pode ser nulo");
        assertNotNull(coleta.getColetorId(), "Coletor não pode ser nulo");
        assertNotNull(coleta.getNomeColetorCompleto(), "Nome do coletor não pode ser nulo");
        assertNotNull(coleta.getLocalizacaoEncontrada(), "Localização não pode ser nula");
        assertNotNull(coleta.getEstadoEncontrado(), "Estado não pode ser nulo");
        
        // Se tem observações, devem estar presentes
        if (coleta.getObservacoes() != null) {
            assertFalse(coleta.getObservacoes().trim().isEmpty());
        }
    }
}

@PropertyTest
@Tag("Feature: historico-coleta-patrimonio, Property 5: Filtragem Correta")
void filtrosDevemRetornarApenasColetasQueAtendemCriterios(
    @ForAll @IntRange(min = 1, max = 100) int patrimonioId,
    @ForAll @Size(min = 10, max = 50) List<@From("coletaGenerator") Coleta> coletas,
    @ForAll @IntRange(min = 1, max = 10) int inventarioIdFiltro
) {
    // Given: patrimônio com coletas de múltiplos inventários
    salvarColetas(patrimonioId, coletas);
    
    FiltroHistoricoDTO filtros = new FiltroHistoricoDTO();
    filtros.setInventarioId(inventarioIdFiltro);
    
    // When: buscar com filtro
    List<HistoricoColetaDTO> historico = 
        service.buscarHistorico(patrimonioId, filtros);
    
    // Then: todas as coletas devem ser do inventário filtrado
    for (HistoricoColetaDTO coleta : historico) {
        assertEquals(
            inventarioIdFiltro,
            coleta.getInventarioId(),
            "Coleta não atende ao filtro de inventário"
        );
    }
}
```

#### Mobile Property Tests (Kotest)

```kotlin
class HistoricoPropertyTest : StringSpec({
    
    "Property 1: Histórico sempre ordenado por data decrescente" {
        checkAll(100, Arb.int(1..100), Arb.list(Arb.historicoColeta(), 2..50)) { 
            patrimonioId, coletas ->
            
            // Given: repositório com coletas
            val repository = FakeHistoricoRepository(coletas)
            val useCase = BuscarHistoricoColetaUseCase(repository)
            
            // When: buscar histórico
            val result = useCase(patrimonioId)
            
            // Then: deve estar ordenado
            result.getOrNull()!!.zipWithNext().forEach { (atual, proxima) ->
                atual.dataColeta shouldBeGreaterThanOrEqualTo proxima.dataColeta
            }
        }
    }
    
    "Property 9: Estratégia offline-first" {
        checkAll(100, Arb.int(1..100), Arb.list(Arb.historicoColeta(), 1..20)) {
            patrimonioId, coletasCache ->
            
            // Given: servidor indisponível, cache com dados
            val repository = FakeHistoricoRepository(
                remoteThrows = IOException("Sem conexão"),
                localData = coletasCache
            )
            val useCase = BuscarHistoricoColetaUseCase(repository)
            
            // When: buscar histórico
            val result = useCase(patrimonioId)
            
            // Then: deve usar cache
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe coletasCache.sortedByDescending { it.dataColeta }
        }
    }
    
    "Property 10: Paginação consistente" {
        checkAll(100, Arb.int(1..100), Arb.list(Arb.historicoColeta(), 25..100)) {
            patrimonioId, todasColetas ->
            
            val pageSize = 20
            val repository = FakeHistoricoRepository(todasColetas)
            val useCase = BuscarHistoricoColetaUseCase(repository)
            
            // When: buscar primeira página
            val filtro1 = FiltroHistorico(offset = 0, limit = pageSize)
            val pagina1 = useCase(patrimonioId, filtro1).getOrNull()!!
            
            // Then: deve ter exatamente pageSize itens
            pagina1.size shouldBe pageSize
            
            // When: buscar segunda página
            val filtro2 = FiltroHistorico(offset = pageSize, limit = pageSize)
            val pagina2 = useCase(patrimonioId, filtro2).getOrNull()!!
            
            // Then: não deve ter duplicação
            val idsPage1 = pagina1.map { it.id }.toSet()
            val idsPage2 = pagina2.map { it.id }.toSet()
            idsPage1.intersect(idsPage2) shouldBe emptySet()
        }
    }
})

// Generators customizados
fun Arb.Companion.historicoColeta(): Arb<HistoricoColeta> = arbitrary {
    HistoricoColeta(
        id = Arb.int(1..10000).bind(),
        patrimonioId = Arb.int(1..100).bind(),
        numeroPatrimonio = Arb.string(5..10).bind(),
        descricaoPatrimonio = Arb.string(10..50).bind(),
        inventarioId = Arb.int(1..10).bind(),
        nomeInventario = Arb.string(10..30).bind(),
        coletorId = Arb.int(1..50).bind(),
        nomeColetorCompleto = Arb.string(10..40).bind(),
        dataColeta = Arb.long(1000000000L..2000000000L).bind(),
        localizacaoEncontrada = Arb.string(5..30).bind(),
        estadoEncontrado = Arb.of("BOM", "REGULAR", "RUIM").bind(),
        observacoes = Arb.string(0..100).orNull().bind(),
        salaId = Arb.int(1..100).orNull().bind(),
        nomeSala = Arb.string(5..20).orNull().bind(),
        setorId = Arb.int(1..50).orNull().bind(),
        nomeSetor = Arb.string(5..30).orNull().bind()
    )
}
```

### Integration Testing

```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class HistoricoIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var useCase: BuscarHistoricoColetaUseCase
    
    @Test
    fun fluxoCompletoHistorico() = runTest {
        // Given: patrimônio com coletas no banco
        val patrimonio = criarPatrimonio()
        val coletas = criarColetas(patrimonio.id, quantidade = 5)
        database.patrimonioDao().insert(patrimonio)
        database.coletaDao().insertAll(coletas)
        
        // When: buscar histórico
        val result = useCase(patrimonio.id)
        
        // Then: deve retornar todas as coletas ordenadas
        assertTrue(result.isSuccess)
        val historico = result.getOrNull()!!
        assertEquals(5, historico.size)
        
        // Verificar ordenação
        historico.zipWithNext().forEach { (atual, proxima) ->
            assertTrue(atual.dataColeta >= proxima.dataColeta)
        }
    }
}
```

## Performance Considerations

### Database Optimization

1. **Índices**: Criar índices compostos para queries frequentes
2. **Paginação**: Limitar resultados com OFFSET/LIMIT
3. **Lazy Loading**: Carregar dados adicionais sob demanda
4. **Connection Pooling**: Reutilizar conexões do banco

### Caching Strategy

```kotlin
class HistoricoRepositoryImpl @Inject constructor(
    private val localDataSource: HistoricoLocalDataSource,
    private val remoteDataSource: HistoricoRemoteDataSource,
    private val cacheManager: CacheManager
) : HistoricoRepository {
    
    override suspend fun buscarHistorico(
        patrimonioId: Int,
        filtros: FiltroHistorico?
    ): List<HistoricoColeta> {
        
        val cacheKey = "historico_$patrimonioId${filtros?.hashCode() ?: ""}"
        
        // Verificar cache
        cacheManager.get<List<HistoricoColeta>>(cacheKey)?.let {
            return it
        }
        
        // Buscar do servidor
        val historico = try {
            remoteDataSource.buscarHistorico(patrimonioId, filtros)
        } catch (e: IOException) {
            // Fallback para local
            localDataSource.buscarHistorico(patrimonioId, filtros)
        }
        
        // Salvar no cache (5 minutos)
        cacheManager.put(cacheKey, historico, duration = 5.minutes)
        
        return historico
    }
}
```

### Mobile Optimization

1. **RecyclerView**: Usar ViewHolder pattern e DiffUtil
2. **Image Loading**: Lazy loading de fotos com Glide/Coil
3. **Background Processing**: Usar Coroutines para operações pesadas
4. **Memory Management**: Limpar cache quando memória baixa

## Security Considerations

1. **Autenticação**: Verificar token JWT em todas as requisições
2. **Autorização**: Verificar permissões do usuário para acessar histórico
3. **Auditoria**: Registrar todos os acessos ao histórico
4. **Imutabilidade**: Impedir modificação de coletas antigas
5. **SQL Injection**: Usar prepared statements em todas as queries

## Deployment Considerations

### Backend

1. Executar scripts de criação de índices no banco
2. Configurar cache em memória (Redis ou similar)
3. Ajustar pool de conexões do banco
4. Configurar logs de auditoria

### Mobile

1. Migração do banco Room para incluir cache de histórico
2. Configurar WorkManager para limpeza periódica de cache
3. Testar em dispositivos com diferentes tamanhos de tela
4. Validar performance com históricos grandes (>1000 coletas)

