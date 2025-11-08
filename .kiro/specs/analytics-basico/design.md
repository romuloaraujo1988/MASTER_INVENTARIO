# Design Document - Analytics Básico

## Overview

O módulo de Analytics Básico será implementado como uma camada adicional sobre o sistema existente, utilizando agregações SQL otimizadas e cache para performance. A arquitetura seguirá o padrão MVC existente no desktop e MVVM no mobile, com novos endpoints REST para servir dados agregados.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
├──────────────────────┬──────────────────────────────────────┤
│   Desktop (Swing)    │      Mobile (Android)                │
│   - DashboardFrame   │      - DashboardFragment             │
│   - ChartsPanel      │      - AnalyticsActivity             │
│   - ReportsFrame     │      - ChartsFragment                │
└──────────────────────┴──────────────────────────────────────┘
                              ↓ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│                      API Layer (Spring Boot)                 │
├─────────────────────────────────────────────────────────────┤
│   - AnalyticsController                                      │
│   - DashboardController                                      │
│   - ReportsController                                        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
├─────────────────────────────────────────────────────────────┤
│   - AnalyticsService (agregações e cálculos)                │
│   - CacheService (Redis/Caffeine)                           │
│   - ExportService (Excel, PDF, CSV)                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
├─────────────────────────────────────────────────────────────┤
│   - AnalyticsDAO (queries otimizadas)                       │
│   - Existing DAOs (Patrimonio, Coleta, etc)                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      Database (PostgreSQL)                   │
├─────────────────────────────────────────────────────────────┤
│   - Existing tables                                          │
│   - New materialized views for analytics                    │
│   - Indexes for performance                                  │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Backend Components

#### AnalyticsService
```java
@Service
public class AnalyticsService {
    
    // Dashboard Metrics
    public DashboardMetricsDTO getDashboardMetrics(Integer inventarioId);
    public List<ColetasPorDiaDTO> getColetasPorDia(Integer inventarioId, LocalDate inicio, LocalDate fim);
    public List<DistribuicaoSetorDTO> getDistribuicaoPorSetor(Integer inventarioId);
    
    // Performance
    public List<PerformanceColetorDTO> getPerformanceColetores(Integer inventarioId);
    public List<TopResponsaveisDTO> getTopResponsaveis(Integer inventarioId, int limit);
    
    // Divergências
    public DivergenciasAnalysisDTO getAnalyseDivergencias(Integer inventarioId);
    public List<DivergenciaDetalheDTO> getDivergenciasDetalhadas(Integer inventarioId);
    
    // Comparação
    public ComparacaoInventariosDTO compararInventarios(List<Integer> inventarioIds);
    
    // Valor
    public ValorPatrimonialDTO getAnalyseValor(Integer inventarioId);
}
```

#### AnalyticsController
```java
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    @GetMapping("/dashboard/{inventarioId}")
    public ResponseEntity<ApiResponse<DashboardMetricsDTO>> getDashboard(
        @PathVariable Integer inventarioId
    );
    
    @GetMapping("/coletas-evolucao")
    public ResponseEntity<ApiResponse<List<ColetasPorDiaDTO>>> getColetasEvolucao(
        @RequestParam Integer inventarioId,
        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fim
    );
    
    @GetMapping("/distribuicao-setor/{inventarioId}")
    public ResponseEntity<ApiResponse<List<DistribuicaoSetorDTO>>> getDistribuicaoSetor(
        @PathVariable Integer inventarioId
    );
    
    @GetMapping("/performance-coletores/{inventarioId}")
    public ResponseEntity<ApiResponse<List<PerformanceColetorDTO>>> getPerformanceColetores(
        @PathVariable Integer inventarioId
    );
    
    @GetMapping("/divergencias/{inventarioId}")
    public ResponseEntity<ApiResponse<DivergenciasAnalysisDTO>> getDivergencias(
        @PathVariable Integer inventarioId
    );
    
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportData(
        @RequestBody ExportRequest request
    );
}
```

#### CacheService
```java
@Service
public class CacheService {
    
    private final Cache<String, Object> cache;
    
    public <T> T getOrCompute(String key, Supplier<T> supplier, Duration ttl);
    public void invalidate(String key);
    public void invalidatePattern(String pattern);
}
```

### 2. Data Transfer Objects (DTOs)

#### DashboardMetricsDTO
```java
public class DashboardMetricsDTO {
    private Integer totalPatrimonios;
    private Integer patrimoniosColetados;
    private Double percentualConclusao;
    private Integer divergencias;
    private BigDecimal valorTotal;
    private Integer coletoresAtivos;
    
    // Variações em relação ao período anterior
    private Double variacaoColetas;
    private Double variacaoDivergencias;
}
```

#### ColetasPorDiaDTO
```java
public class ColetasPorDiaDTO {
    private LocalDate data;
    private Integer quantidade;
    private Integer coletoresAtivos;
}
```

#### DistribuicaoSetorDTO
```java
public class DistribuicaoSetorDTO {
    private Integer setorId;
    private String setorNome;
    private Integer quantidade;
    private Double percentual;
    private BigDecimal valorTotal;
    private Integer coletados;
    private Double percentualColetado;
}
```

#### PerformanceColetorDTO
```java
public class PerformanceColetorDTO {
    private Integer coletorId;
    private String nome;
    private Integer totalColetas;
    private Double mediaColetasPorDia;
    private Long tempoMedioPorColeta; // em segundos
    private Double taxaDivergencias;
    private LocalDateTime ultimaAtividade;
    private String status; // ATIVO, INATIVO, ALERTA
}
```

### 3. Database Optimizations

#### Materialized Views
```sql
-- View para métricas de dashboard
CREATE MATERIALIZED VIEW mv_dashboard_metrics AS
SELECT 
    i.id as inventario_id,
    COUNT(DISTINCT p.id) as total_patrimonios,
    COUNT(DISTINCT c.id) as patrimonios_coletados,
    COUNT(DISTINCT CASE WHEN c.divergencia = true THEN c.id END) as divergencias,
    SUM(p.valor) as valor_total,
    COUNT(DISTINCT c.id_coletor) as coletores_ativos
FROM tabela_inventario i
LEFT JOIN tabela_patrimonio p ON 1=1
LEFT JOIN tabela_coleta c ON c.id_inventario = i.id AND c.id_patrimonio = p.id
WHERE i.status = 'EM_ANDAMENTO'
GROUP BY i.id;

-- Refresh automático a cada 5 minutos
CREATE INDEX idx_mv_dashboard_inventario ON mv_dashboard_metrics(inventario_id);
```

#### Indexes
```sql
-- Índices para performance de queries analytics
CREATE INDEX idx_coleta_data_inventario ON tabela_coleta(id_inventario, data_coleta);
CREATE INDEX idx_coleta_coletor_data ON tabela_coleta(id_coletor, data_coleta);
CREATE INDEX idx_patrimonio_setor_valor ON tabela_patrimonio(id_setor, valor);
CREATE INDEX idx_coleta_divergencia ON tabela_coleta(id_inventario, divergencia) WHERE divergencia = true;
```

### 4. Frontend Components (Desktop)

#### DashboardFrame
```java
public class DashboardFrame extends JFrame {
    
    private MetricsPanel metricsPanel;
    private ChartsPanel chartsPanel;
    private FiltersPanel filtersPanel;
    
    public void loadDashboard(Integer inventarioId);
    public void refreshData();
    public void applyFilters(FilterCriteria filters);
}
```

#### ChartsPanel
```java
public class ChartsPanel extends JPanel {
    
    private JFreeChart lineChart; // Evolução de coletas
    private JFreeChart pieChart;  // Distribuição por setor
    private JFreeChart barChart;  // Performance de coletores
    
    public void updateLineChart(List<ColetasPorDiaDTO> data);
    public void updatePieChart(List<DistribuicaoSetorDTO> data);
    public void updateBarChart(List<PerformanceColetorDTO> data);
}
```

### 5. Mobile Components (Android)

#### AnalyticsActivity
```kotlin
class AnalyticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAnalyticsBinding
    private val viewModel: AnalyticsViewModel by viewModels()
    
    private fun setupCharts()
    private fun setupMetrics()
    private fun setupFilters()
}
```

#### AnalyticsViewModel
```kotlin
class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    
    private val _dashboardMetrics = MutableStateFlow<DashboardMetrics?>(null)
    val dashboardMetrics: StateFlow<DashboardMetrics?> = _dashboardMetrics
    
    private val _coletasEvolucao = MutableStateFlow<List<ColetasPorDia>>(emptyList())
    val coletasEvolucao: StateFlow<List<ColetasPorDia>> = _coletasEvolucao
    
    fun loadDashboard(inventarioId: Int)
    fun loadColetasEvolucao(inventarioId: Int, inicio: LocalDate, fim: LocalDate)
    fun refreshData()
}
```

## Data Models

### Cache Keys Pattern
```
analytics:dashboard:{inventarioId}
analytics:coletas:{inventarioId}:{inicio}:{fim}
analytics:distribuicao:{inventarioId}
analytics:performance:{inventarioId}
analytics:divergencias:{inventarioId}
```

### Cache TTL Strategy
- Dashboard metrics: 5 minutos
- Gráficos de evolução: 10 minutos
- Distribuição por setor: 15 minutos
- Performance de coletores: 5 minutos
- Divergências: 2 minutos (mais crítico)

## Error Handling

### Error Scenarios

1. **Dados não disponíveis**
   - Retornar estrutura vazia com mensagem
   - Não quebrar a UI

2. **Timeout de query**
   - Limite de 30 segundos
   - Retornar dados parciais se possível
   - Log para análise

3. **Cache miss**
   - Fallback para query direta
   - Rebuild cache assíncrono

4. **Exportação falha**
   - Retry automático (1x)
   - Notificar usuário
   - Log detalhado

### Error Response Format
```json
{
  "success": false,
  "message": "Erro ao carregar métricas",
  "error": {
    "code": "ANALYTICS_001",
    "details": "Timeout ao executar query de agregação"
  },
  "data": null
}
```

## Testing Strategy

### Unit Tests
- Testar cálculos de métricas
- Testar agregações
- Testar formatação de dados
- Testar cache hit/miss

### Integration Tests
- Testar endpoints REST
- Testar queries com dados reais
- Testar exportações
- Testar invalidação de cache

### Performance Tests
- Load test com 10.000 patrimônios
- Stress test com 50 usuários simultâneos
- Query performance < 500ms
- Dashboard load < 3 segundos

### UI Tests
- Testar renderização de gráficos
- Testar interações (drill-down, filtros)
- Testar responsividade
- Testar acessibilidade

## Security Considerations

### Authorization
```java
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
public DashboardMetricsDTO getDashboardMetrics(Integer inventarioId) {
    // Verificar se usuário tem acesso ao inventário
    if (!hasAccessToInventario(inventarioId)) {
        throw new AccessDeniedException("Sem permissão");
    }
    // ...
}
```

### Data Masking
- Valores monetários podem ser mascarados para perfis específicos
- Nomes de responsáveis podem ser anonimizados em exportações

### Audit Log
```java
@Audited
public byte[] exportData(ExportRequest request) {
    auditLog.log(
        "EXPORT_ANALYTICS",
        getCurrentUser(),
        request.getType(),
        request.getFilters()
    );
    // ...
}
```

## Performance Optimizations

### Query Optimization
1. Usar materialized views para agregações frequentes
2. Índices compostos para filtros comuns
3. EXPLAIN ANALYZE para identificar gargalos
4. Partition tables por data se necessário

### Caching Strategy
1. Cache em memória (Caffeine) para dados quentes
2. Redis para cache distribuído (futuro)
3. Invalidação inteligente baseada em eventos
4. Pre-warming de cache para horários de pico

### Frontend Optimization
1. Lazy loading de gráficos
2. Virtualização de listas longas
3. Debounce em filtros
4. Progressive rendering

## Deployment Considerations

### Database Migration
```sql
-- V1.3.0__analytics_tables.sql
CREATE MATERIALIZED VIEW mv_dashboard_metrics AS ...;
CREATE INDEX idx_coleta_data_inventario ON ...;
-- Refresh inicial
REFRESH MATERIALIZED VIEW mv_dashboard_metrics;
```

### Configuration
```properties
# application.properties
analytics.cache.ttl.dashboard=300
analytics.cache.ttl.charts=600
analytics.query.timeout=30
analytics.export.max-records=100000
```

### Monitoring
- Métricas de performance de queries
- Taxa de cache hit/miss
- Tempo de resposta de endpoints
- Erros e timeouts

## Future Enhancements

### Phase 2 (não neste spec)
- Dashboards customizáveis
- Alertas configuráveis
- Scheduled reports
- Real-time updates via WebSocket
- Machine Learning predictions
- Integration with BI tools

## Diagrams

### Sequence Diagram - Load Dashboard
```
User -> DashboardFrame: loadDashboard(inventarioId)
DashboardFrame -> AnalyticsService: getDashboardMetrics(inventarioId)
AnalyticsService -> CacheService: get("analytics:dashboard:{id}")
alt Cache Hit
    CacheService --> AnalyticsService: cached data
else Cache Miss
    AnalyticsService -> AnalyticsDAO: queryDashboardMetrics(inventarioId)
    AnalyticsDAO -> Database: SELECT ...
    Database --> AnalyticsDAO: result
    AnalyticsDAO --> AnalyticsService: data
    AnalyticsService -> CacheService: put("analytics:dashboard:{id}", data, 5min)
end
AnalyticsService --> DashboardFrame: DashboardMetricsDTO
DashboardFrame -> ChartsPanel: updateCharts(data)
ChartsPanel --> User: Display dashboard
```

### Component Diagram
```
┌─────────────────────────────────────────────────────────┐
│                    Desktop Application                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Dashboard    │  │ Charts       │  │ Reports      │ │
│  │ Frame        │  │ Panel        │  │ Frame        │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                         ↓ REST API
┌─────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Analytics    │→ │ Cache        │  │ Export       │ │
│  │ Service      │  │ Service      │  │ Service      │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│         ↓                                               │
│  ┌──────────────┐                                      │
│  │ Analytics    │                                      │
│  │ DAO          │                                      │
│  └──────────────┘                                      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Tables       │  │ Materialized │  │ Indexes      │ │
│  │              │  │ Views        │  │              │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```
