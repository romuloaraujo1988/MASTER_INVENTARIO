# Design Document - Analytics de Divergências e Métricas

## Overview

Este documento descreve o design do módulo de Analytics Avançado para o Sistema de Inventário, focando em análise de divergências e métricas de tempo de coleta. O módulo será implementado como uma extensão do sistema desktop existente (Java Swing), reutilizando a infraestrutura de relatórios e serviços já disponíveis.

O design segue os padrões já estabelecidos no projeto:
- **Arquitetura em camadas**: View → Service → DAO
- **Padrão MVVM** para ViewModels onde aplicável
- **JFreeChart** para visualizações gráficas
- **Apache POI** para exportação Excel

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presentation Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │AnalyticsDash-   │  │DivergenciasAna- │  │MetricasTempoAna-│  │
│  │boardFrame       │  │lyticsFrame      │  │lyticsFrame      │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼─────────────────────┼─────────────────────┼─────────┘
            │                     │                     │
┌───────────┼─────────────────────┼─────────────────────┼─────────┐
│           ▼                     ▼                     ▼         │
│                       Service Layer                              │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                  AnalyticsService                            ││
│  │  - calcularKPIs()                                            ││
│  │  - analisarDivergencias()                                    ││
│  │  - calcularMetricasTempo()                                   ││
│  │  - gerarRankings()                                           ││
│  │  - exportarParaBI()                                          ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              DivergenciaClassificadorService                 ││
│  │  - classificarGravidade()                                    ││
│  │  - detectarTipoDivergencia()                                 ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
            │
┌───────────┼─────────────────────────────────────────────────────┐
│           ▼                                                      │
│                        Data Layer                                │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                   AnalyticsDAO                               ││
│  │  - buscarDivergenciasComDetalhes()                           ││
│  │  - buscarMetricasTempoAgrupadas()                            ││
│  │  - buscarDivergenciasPorPeriodo()                            ││
│  │  - buscarRankingSetores()                                    ││
│  │  - buscarRankingColetores()                                  ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. AnalyticsService

Serviço principal que coordena todas as análises.

```java
public class AnalyticsService {
    
    private final AnalyticsDAO analyticsDAO;
    private final DivergenciaClassificadorService classificador;
    
    // KPIs do Dashboard
    public AnalyticsKPIs calcularKPIs(int idInventario);
    
    // Análise de Divergências
    public List<DivergenciaAnalytics> analisarDivergencias(int idInventario);
    public Map<String, Long> agruparDivergenciasPorTipo(int idInventario);
    public Map<String, Long> agruparDivergenciasPorGravidade(int idInventario);
    public List<DivergenciaTemporal> buscarDivergenciasPorDia(int idInventario);
    
    // Métricas de Tempo
    public List<MetricasColetorDTO> calcularMetricasPorColetor(int idInventario);
    public Map<String, MetricasPeriodoDTO> calcularMetricasPorPeriodo(int idInventario);
    public Map<String, MetricasPeriodoDTO> calcularMetricasPorDiaSemana(int idInventario);
    
    // Rankings
    public List<RankingSetorDTO> gerarRankingSetores(int idInventario);
    public List<RankingResponsavelDTO> gerarRankingResponsaveis(int idInventario);
    
    // Exportação
    public File exportarParaCSV(int idInventario, String diretorio);
    public File exportarParaExcel(int idInventario, String diretorio);
}
```

### 2. DivergenciaClassificadorService

Serviço responsável pela classificação de gravidade das divergências.

```java
public class DivergenciaClassificadorService {
    
    public enum GravidadeDivergencia {
        CRITICA, ALTA, MEDIA, BAIXA
    }
    
    public enum TipoDivergencia {
        LOCALIZACAO, ESTADO, RESPONSAVEL, VALOR, MANUAL, OUTRO
    }
    
    // Classificação de Localização
    public GravidadeDivergencia classificarDivergenciaLocalizacao(
        String localCadastrado, 
        String localEncontrado,
        Integer setorCadastrado,
        Integer setorEncontrado
    );
    
    // Classificação de Estado
    public GravidadeDivergencia classificarDivergenciaEstado(
        String estadoCadastrado, 
        String estadoEncontrado
    );
    
    // Detecção automática de tipo
    public TipoDivergencia detectarTipo(Coleta coleta, Patrimonio patrimonio);
}
```

### 3. AnalyticsDAO

DAO para consultas analíticas otimizadas.

```java
public class AnalyticsDAO {
    
    // Divergências
    public List<Map<String, Object>> buscarDivergenciasComDetalhes(int idInventario);
    public List<Map<String, Object>> buscarDivergenciasPorPeriodo(
        int idInventario, 
        LocalDate inicio, 
        LocalDate fim
    );
    
    // Métricas de Tempo
    public List<Map<String, Object>> buscarMetricasTempoAgrupadas(
        int idInventario, 
        String agrupamento // "COLETOR", "PERIODO", "DIA_SEMANA"
    );
    
    // Rankings
    public List<Map<String, Object>> buscarRankingSetores(int idInventario);
    public List<Map<String, Object>> buscarRankingColetores(int idInventario);
    public List<Map<String, Object>> buscarRankingResponsaveis(int idInventario);
    
    // KPIs
    public Map<String, Object> buscarKPIsGerais(int idInventario);
}
```

## Data Models

### DTOs de Analytics

```java
// KPIs do Dashboard
public class AnalyticsKPIs {
    private double taxaDivergenciaGeral;      // Percentual
    private double tempoMedioColeta;          // Segundos
    private double coletasPorHora;            // Média
    private int coletoresAtivos;              // Contagem
    private int totalDivergencias;            // Contagem
    private int totalColetas;                 // Contagem
    
    // Comparação com período anterior
    private Double variacaoTaxaDivergencia;   // Positivo = piorou
    private Double variacaoTempoMedio;        // Positivo = piorou
}

// Divergência com análise
public class DivergenciaAnalytics {
    private int idColeta;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private TipoDivergencia tipo;
    private GravidadeDivergencia gravidade;
    private String valorCadastrado;
    private String valorEncontrado;
    private String motivo;
    private LocalDateTime dataColeta;
    private String nomeSetor;
    private String nomeResponsavel;
    private String nomeColetor;
}

// Métricas por Coletor
public class MetricasColetorDTO {
    private int idColetor;
    private String nomeColetor;
    private int totalColetas;
    private double tempoMedio;                // Segundos
    private int tempoMinimo;                  // Segundos
    private int tempoMaximo;                  // Segundos
    private double desvioPadrao;              // Segundos
    private int coletasComTempo;              // Coletas com métrica disponível
    private double percentualComTempo;        // % de coletas com tempo
}

// Métricas por Período
public class MetricasPeriodoDTO {
    private String periodo;                   // "MANHA", "TARDE", "NOITE" ou dia da semana
    private int totalColetas;
    private double tempoMedio;
    private boolean baixaProdutividade;       // tempo > 1.5x média geral
}

// Ranking de Setor
public class RankingSetorDTO {
    private int idSetor;
    private String nomeSetor;
    private int totalColetas;
    private int totalDivergencias;
    private double taxaDivergencia;           // Percentual
    private int posicaoRanking;
}

// Divergência Temporal (para gráfico)
public class DivergenciaTemporal {
    private LocalDate data;
    private TipoDivergencia tipo;
    private long quantidade;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Agrupamento de divergências preserva total
*For any* conjunto de divergências, a soma das contagens de todos os tipos de divergência deve ser igual ao total de divergências.
**Validates: Requirements 1.1**

### Property 2: Classificação de gravidade de localização é determinística
*For any* par de localizações (cadastrada, encontrada) com informação de setor, a gravidade classificada deve ser: ALTA se setores diferentes, MÉDIA se mesma setor mas sala diferente, BAIXA se apenas descrição difere.
**Validates: Requirements 1.2**

### Property 3: Classificação de gravidade de estado é determinística
*For any* par de estados de conservação (cadastrado, encontrado), a gravidade deve ser: CRÍTICA se diferença >= 2 níveis para pior, ALTA se diferença = 1 nível para pior, BAIXA se melhorou ou igual.
**Validates: Requirements 1.3**

### Property 4: Filtro de gravidade retorna apenas itens da gravidade selecionada
*For any* lista de divergências e gravidade selecionada, todos os itens retornados pelo filtro devem ter a gravidade igual à selecionada.
**Validates: Requirements 1.4**

### Property 5: Exportação Excel contém todos os campos obrigatórios
*For any* conjunto de divergências exportado, o arquivo Excel deve conter colunas para: tipo, gravidade, valor cadastrado, valor encontrado, data da coleta.
**Validates: Requirements 1.5**

### Property 6: Soma de divergências por dia igual ao total
*For any* análise temporal de divergências, a soma das quantidades de todos os dias deve ser igual ao total de divergências do período.
**Validates: Requirements 2.1**

### Property 7: Séries do gráfico contêm apenas divergências do tipo correspondente
*For any* série de dados do gráfico temporal, todos os pontos devem ser de divergências do tipo indicado pela série.
**Validates: Requirements 2.2**

### Property 8: Ranking de setores está ordenado por taxa decrescente
*For any* ranking de setores, cada setor deve ter taxa de divergência maior ou igual ao setor seguinte na lista.
**Validates: Requirements 3.1**

### Property 9: Taxa de divergência calculada corretamente
*For any* setor com coletas, a taxa de divergência deve ser igual a (divergências / coletas) * 100.
**Validates: Requirements 3.2**

### Property 10: Filtro por setor retorna apenas divergências do setor
*For any* setor selecionado, todas as divergências retornadas devem pertencer a patrimônios daquele setor.
**Validates: Requirements 3.4**

### Property 11: Métricas estatísticas calculadas corretamente
*For any* conjunto de tempos de coleta de um coletor, o tempo médio deve ser a média aritmética, o mínimo deve ser o menor valor, o máximo deve ser o maior valor.
**Validates: Requirements 4.1**

### Property 12: Filtro de outliers exclui valores fora do range
*For any* cálculo de tempo médio, coletas com tempo <= 0 ou tempo >= 600 segundos não devem ser incluídas.
**Validates: Requirements 4.2**

### Property 13: Ranking de coletores ordenado por tempo crescente
*For any* ranking de coletores, cada coletor deve ter tempo médio menor ou igual ao coletor seguinte na lista.
**Validates: Requirements 4.3**

### Property 14: Classificação de período baseada na hora
*For any* coleta com hora registrada, deve ser classificada como: MANHÃ se hora >= 6 e < 12, TARDE se hora >= 12 e < 18, NOITE caso contrário.
**Validates: Requirements 5.1**

### Property 15: Identificação de baixa produtividade
*For any* período com tempo médio > 1.5 * média geral, deve ser marcado como baixa produtividade.
**Validates: Requirements 5.4**

### Property 16: CSV contém todos os registros
*For any* exportação CSV, o número de linhas de dados deve ser igual ao número de registros no banco.
**Validates: Requirements 6.1**

### Property 17: Datas no CSV seguem ISO 8601
*For any* data no arquivo CSV exportado, deve estar no formato yyyy-MM-dd HH:mm:ss.
**Validates: Requirements 6.2**

### Property 18: Comparação de KPIs indica direção correta
*For any* par de valores de KPI (atual, anterior), a indicação deve ser "melhorou" se atual < anterior para métricas onde menor é melhor (tempo, divergências), e "piorou" caso contrário.
**Validates: Requirements 7.2**

## Error Handling

### Tratamento de Dados Ausentes

```java
// Coletas sem tempo registrado
if (coleta.getTempoColetaSegundos() == null || coleta.getTempoColetaSegundos() <= 0) {
    // Não incluir no cálculo de métricas de tempo
    // Incrementar contador de coletas sem métrica
}

// Divergências sem tipo identificável
if (tipoDivergencia == null) {
    tipoDivergencia = TipoDivergencia.OUTRO;
}

// Inventário sem coletas
if (totalColetas == 0) {
    // Retornar KPIs zerados com flag indicando ausência de dados
    return AnalyticsKPIs.empty();
}
```

### Tratamento de Erros de Exportação

```java
try {
    File arquivo = exportarParaExcel(idInventario, diretorio);
} catch (IOException e) {
    logger.error("Erro ao exportar Excel: {}", e.getMessage());
    throw new ExportacaoException("Não foi possível gerar o arquivo Excel", e);
} catch (OutOfMemoryError e) {
    logger.error("Memória insuficiente para exportação de {} registros", totalRegistros);
    throw new ExportacaoException("Dados muito grandes para exportação. Tente filtrar por período.", e);
}
```

## Testing Strategy

### Dual Testing Approach

O módulo será testado usando duas abordagens complementares:

1. **Unit Tests**: Verificam casos específicos e edge cases
2. **Property-Based Tests**: Verificam propriedades universais usando a biblioteca **jqwik** para Java

### Property-Based Testing Framework

- **Biblioteca**: jqwik 1.8.x (https://jqwik.net/)
- **Configuração**: Mínimo de 100 iterações por propriedade
- **Formato de anotação**: `// **Feature: analytics-divergencias-metricas, Property N: descrição**`

### Estrutura de Testes

```
src/test/java/com/inventario/analytics/
├── service/
│   ├── AnalyticsServiceTest.java           # Unit tests
│   ├── AnalyticsServicePropertyTest.java   # Property tests
│   ├── DivergenciaClassificadorTest.java   # Unit tests
│   └── DivergenciaClassificadorPropertyTest.java  # Property tests
├── dao/
│   └── AnalyticsDAOTest.java               # Integration tests
└── export/
    └── ExportacaoPropertyTest.java         # Property tests para exportação
```

### Generators para Property Tests

```java
@Provide
Arbitrary<DivergenciaAnalytics> divergencias() {
    return Combinators.combine(
        Arbitraries.integers().between(1, 1000),           // idColeta
        Arbitraries.strings().alpha().ofLength(10),        // numeroPatrimonio
        Arbitraries.of(TipoDivergencia.values()),          // tipo
        Arbitraries.of(GravidadeDivergencia.values())      // gravidade
    ).as(DivergenciaAnalytics::new);
}

@Provide
Arbitrary<Integer> temposColetaValidos() {
    return Arbitraries.integers().between(1, 599);  // Dentro do range válido
}

@Provide
Arbitrary<Integer> temposColetaComOutliers() {
    return Arbitraries.oneOf(
        Arbitraries.integers().between(-100, 0),    // Outliers negativos
        Arbitraries.integers().between(1, 599),     // Válidos
        Arbitraries.integers().between(600, 10000)  // Outliers altos
    );
}
```

### Exemplo de Property Test

```java
// **Feature: analytics-divergencias-metricas, Property 9: Taxa de divergência calculada corretamente**
@Property(tries = 100)
void taxaDivergenciaCalculadaCorretamente(
    @ForAll @IntRange(min = 1, max = 1000) int totalColetas,
    @ForAll @IntRange(min = 0, max = 1000) int totalDivergencias
) {
    Assume.that(totalDivergencias <= totalColetas);
    
    double taxaEsperada = (totalDivergencias * 100.0) / totalColetas;
    double taxaCalculada = analyticsService.calcularTaxaDivergencia(totalDivergencias, totalColetas);
    
    assertThat(taxaCalculada).isCloseTo(taxaEsperada, within(0.01));
}
```

