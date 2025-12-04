# Design Document - Relatório de Integridade de Itens Compostos

## Overview

Este documento descreve o design técnico do **Relatório de Integridade de Itens Compostos** para a aplicação desktop do Sistema de Inventário Patrimonial. O relatório permite visualizar e analisar a integridade dos conjuntos patrimoniais, identificando quais patrimônios compostos estão completos ou incompletos.

### Objetivos do Design

1. **Reutilizar Estrutura Existente**: Utilizar as tabelas `tabela_item_composto` e `tabela_coleta_componente` já existentes
2. **Seguir Padrões do Sistema**: Manter consistência com `RelatorioFrame` existente (MVVM)
3. **Performance**: Consultas otimizadas para os 1.588+ itens compostos existentes
4. **Usabilidade**: Interface intuitiva com filtros, ordenação e exportação
5. **Extensibilidade**: Facilitar adição de novos tipos de análises no futuro

### Princípios de Design

- **MVVM Pattern**: Separação clara entre View, ViewModel e Model
- **Offline-First**: Dados carregados do banco local
- **Lazy Loading**: Carregar detalhes apenas quando necessário
- **Responsive UI**: Feedback visual durante operações longas

---

## Architecture

### Camadas da Aplicação

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  - RelatorioItemCompostoFrame (JFrame principal)             │
│  - DetalheItemCompostoDialog (JDialog de detalhes)           │
│  - ExportacaoDialog (JDialog de exportação)                  │
└──────────────────────┬──────────────────────────────────────┘
                       │ observa StateFlow
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                           │
│  - RelatorioItemCompostoViewModel                            │
│  - RelatorioItemCompostoState (sealed class)                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  - RelatorioItemCompostoService                              │
│  - ExportacaoItemCompostoService                             │
└──────────────────────┬──────────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    DAO Layer                                 │
│  - ItemCompostoDAO (consultas de itens)                      │
│  - ComponenteColetaDAO (status de coleta)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ queries
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Database Layer                            │
│  - tabela_item_composto (1.588 registros)                    │
│  - tabela_coleta_componente (14 registros)                   │
│  - Views: vw_resumo_itens_compostos, etc.                    │
└─────────────────────────────────────────────────────────────┘
```

### Integração com Sistema Existente

```
MainFrame
    │
    └─── Menu "Relatórios"
            └─── "Relatório de Itens Compostos" → RelatorioItemCompostoFrame
```

---

## Components and Interfaces

### 1. Model Classes

#### ItemCompostoResumo.java
```java
public class ItemCompostoResumo {
    private Integer idPatrimonio;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private String nomeSala;
    private String nomeResponsavel;
    private int totalComponentes;
    private int componentesEsperados;
    private int componentesEncontrados;
    private int componentesFaltantes;
    private double taxaIntegridade;
    private StatusIntegridade status;
    private List<String> tiposFaltantes;
    
    // Métodos de negócio
    public boolean isCompleto() { return componentesFaltantes == 0; }
    public String getComponentesFaltantesFormatado() { 
        return String.join(", ", tiposFaltantes); 
    }
}
```

#### ComponenteDetalhe.java
```java
public class ComponenteDetalhe {
    private Integer id;
    private String tipo;
    private String descricao;
    private int quantidadeEsperada;
    private int quantidadeEncontrada;
    private StatusComponente status;
    private String observacao;
    private Date dataColeta;
    
    public int getQuantidadeFaltante() {
        return quantidadeEsperada - quantidadeEncontrada;
    }
}
```

#### EstatisticasIntegridade.java
```java
public class EstatisticasIntegridade {
    private int totalConjuntos;
    private int conjuntosCompletos;
    private int conjuntosIncompletos;
    private double taxaIntegridadeGeral;
    
    public boolean isAlerta() { return taxaIntegridadeGeral < 80.0; }
}
```

#### FiltroRelatorioItemComposto.java
```java
public class FiltroRelatorioItemComposto {
    private Integer idInventario;
    private Integer idSetor;
    private Integer idSala;
    private Integer idResponsavel;
    private StatusIntegridade statusIntegridade; // COMPLETO, INCOMPLETO, TODOS
    
    public boolean temFiltrosAtivos() {
        return idInventario != null || idSetor != null || 
               idSala != null || idResponsavel != null ||
               statusIntegridade != StatusIntegridade.TODOS;
    }
}
```

### 2. Enums

#### StatusIntegridade.java
```java
public enum StatusIntegridade {
    COMPLETO("Completo", new Color(76, 175, 80)),      // Verde
    INCOMPLETO("Incompleto", new Color(244, 67, 54)), // Vermelho
    PARCIAL("Parcial", new Color(255, 152, 0)),       // Laranja
    TODOS("Todos", Color.BLACK);
    
    private final String descricao;
    private final Color cor;
}
```

#### StatusComponente.java
```java
public enum StatusComponente {
    ENCONTRADO("Encontrado"),
    FALTANTE("Faltante"),
    PARCIAL("Parcial"),
    NAO_COLETADO("Não Coletado");
}
```

### 3. DAO Interfaces

#### ItemCompostoRelatorioDAO.java
```java
public interface ItemCompostoRelatorioDAO {
    // Consultas principais
    List<ItemCompostoResumo> buscarResumoComFiltros(FiltroRelatorioItemComposto filtro);
    EstatisticasIntegridade calcularEstatisticas(FiltroRelatorioItemComposto filtro);
    
    // Detalhamento
    List<ComponenteDetalhe> buscarComponentesPorPatrimonio(Integer idPatrimonio, Integer idInventario);
    
    // Dados para filtros
    List<Inventario> listarInventariosComItensCompostos();
    List<Setor> listarSetoresComItensCompostos();
    List<Sala> listarSalasComItensCompostos(Integer idSetor);
    List<Responsavel> listarResponsaveisComItensCompostos();
}
```

### 4. Service Classes

#### RelatorioItemCompostoService.java
```java
public class RelatorioItemCompostoService {
    private ItemCompostoRelatorioDAO dao;
    
    // Geração de relatório
    public RelatorioItemComposto gerarRelatorio(FiltroRelatorioItemComposto filtro);
    public EstatisticasIntegridade calcularEstatisticas(FiltroRelatorioItemComposto filtro);
    
    // Detalhamento
    public DetalheItemComposto obterDetalhes(Integer idPatrimonio, Integer idInventario);
    
    // Ordenação
    public List<ItemCompostoResumo> ordenar(List<ItemCompostoResumo> itens, 
                                             String coluna, boolean crescente);
}
```

#### ExportacaoItemCompostoService.java
```java
public class ExportacaoItemCompostoService {
    // Exportação
    public File exportarExcel(RelatorioItemComposto relatorio, String caminhoArquivo);
    public File exportarPDF(RelatorioItemComposto relatorio, String caminhoArquivo);
    public File exportarCSV(RelatorioItemComposto relatorio, String caminhoArquivo);
}
```

### 5. ViewModel

#### RelatorioItemCompostoViewModel.java
```java
public class RelatorioItemCompostoViewModel {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private RelatorioItemCompostoState state = RelatorioItemCompostoState.Idle.INSTANCE;
    
    private RelatorioItemCompostoService service;
    private ExportacaoItemCompostoService exportacaoService;
    
    // Ações
    public void carregarRelatorio(FiltroRelatorioItemComposto filtro);
    public void ordenarPor(String coluna);
    public void exportar(FormatoExportacao formato, String caminhoArquivo);
    public void carregarDetalhes(Integer idPatrimonio);
    
    // Dados para filtros
    public void carregarInventarios();
    public void carregarSetores();
    public void carregarSalas(Integer idSetor);
    public void carregarResponsaveis();
    
    // Observer pattern
    public void addPropertyChangeListener(PropertyChangeListener listener);
}
```

#### RelatorioItemCompostoState.java
```java
public sealed interface RelatorioItemCompostoState {
    record Idle() implements RelatorioItemCompostoState {}
    record Loading(String mensagem) implements RelatorioItemCompostoState {}
    record Success(List<ItemCompostoResumo> itens, 
                   EstatisticasIntegridade estatisticas) implements RelatorioItemCompostoState {}
    record DetalhesCarregados(DetalheItemComposto detalhe) implements RelatorioItemCompostoState {}
    record ExportacaoSucesso(String caminhoArquivo) implements RelatorioItemCompostoState {}
    record Error(String mensagem) implements RelatorioItemCompostoState {}
}
```

---

## Data Models

### Database Queries

#### Query Principal - Resumo de Itens Compostos
```sql
SELECT 
    p.id AS id_patrimonio,
    p.numero AS numero_patrimonio,
    p.descricao AS descricao_patrimonio,
    s.descricao AS nome_sala,
    r.nome AS nome_responsavel,
    COUNT(ic.id) AS total_componentes,
    SUM(ic.quantidade_esperada) AS componentes_esperados,
    COALESCE(SUM(cc.quantidade_encontrada), 0) AS componentes_encontrados,
    SUM(ic.quantidade_esperada) - COALESCE(SUM(cc.quantidade_encontrada), 0) AS componentes_faltantes,
    ROUND(
        (COALESCE(SUM(cc.quantidade_encontrada), 0)::DECIMAL / 
         NULLIF(SUM(ic.quantidade_esperada), 0)) * 100, 2
    ) AS taxa_integridade,
    CASE 
        WHEN COALESCE(SUM(cc.quantidade_encontrada), 0) >= SUM(ic.quantidade_esperada) THEN 'COMPLETO'
        WHEN COALESCE(SUM(cc.quantidade_encontrada), 0) > 0 THEN 'PARCIAL'
        ELSE 'INCOMPLETO'
    END AS status,
    STRING_AGG(
        CASE WHEN COALESCE(cc.quantidade_encontrada, 0) < ic.quantidade_esperada 
             THEN ic.tipo_componente END, 
        ', '
    ) AS tipos_faltantes
FROM tabela_patrimonio p
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_sala s ON p.id_sala = s.id
LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto 
    AND cc.id_inventario = :idInventario
WHERE 1=1
    AND (:idSetor IS NULL OR s.id_setor = :idSetor)
    AND (:idSala IS NULL OR p.id_sala = :idSala)
    AND (:idResponsavel IS NULL OR p.id_responsavel = :idResponsavel)
GROUP BY p.id, p.numero, p.descricao, s.descricao, r.nome
HAVING (:statusIntegridade = 'TODOS' 
    OR (:statusIntegridade = 'COMPLETO' AND COALESCE(SUM(cc.quantidade_encontrada), 0) >= SUM(ic.quantidade_esperada))
    OR (:statusIntegridade = 'INCOMPLETO' AND COALESCE(SUM(cc.quantidade_encontrada), 0) < SUM(ic.quantidade_esperada)))
ORDER BY p.numero;
```

#### Query de Estatísticas
```sql
SELECT 
    COUNT(DISTINCT p.id) AS total_conjuntos,
    COUNT(DISTINCT CASE 
        WHEN sub.componentes_encontrados >= sub.componentes_esperados THEN p.id 
    END) AS conjuntos_completos,
    COUNT(DISTINCT CASE 
        WHEN sub.componentes_encontrados < sub.componentes_esperados THEN p.id 
    END) AS conjuntos_incompletos,
    ROUND(
        AVG(sub.componentes_encontrados::DECIMAL / NULLIF(sub.componentes_esperados, 0)) * 100, 2
    ) AS taxa_integridade_geral
FROM tabela_patrimonio p
INNER JOIN (
    SELECT 
        ic.id_patrimonio_principal,
        SUM(ic.quantidade_esperada) AS componentes_esperados,
        COALESCE(SUM(cc.quantidade_encontrada), 0) AS componentes_encontrados
    FROM tabela_item_composto ic
    LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto 
        AND cc.id_inventario = :idInventario
    GROUP BY ic.id_patrimonio_principal
) sub ON p.id = sub.id_patrimonio_principal
WHERE 1=1
    AND (:idSetor IS NULL OR EXISTS (
        SELECT 1 FROM tabela_sala s WHERE s.id = p.id_sala AND s.id_setor = :idSetor
    ));
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Filtragem por inventário retorna apenas itens do inventário selecionado
*For any* inventário selecionado no filtro, todos os itens retornados no relatório devem ter coletas associadas apenas àquele inventário
**Validates: Requirements 2.2**

### Property 2: Filtragem por status retorna apenas itens com status correspondente
*For any* status de integridade selecionado (Completo/Incompleto), todos os itens retornados devem ter o status correspondente calculado corretamente
**Validates: Requirements 2.3**

### Property 3: Múltiplos filtros aplicam operação AND lógica
*For any* combinação de filtros aplicados simultaneamente, todos os itens retornados devem satisfazer TODOS os critérios de filtro
**Validates: Requirements 2.4**

### Property 4: Destaque visual corresponde ao status de integridade
*For any* item composto exibido na tabela, a cor de destaque da linha deve corresponder ao seu status: verde para completo, vermelho/laranja para incompleto
**Validates: Requirements 3.2, 3.3**

### Property 5: Lista de componentes faltantes é precisa
*For any* item composto com componentes faltantes, a coluna "Componentes Faltantes" deve listar exatamente os tipos de componentes onde quantidade_encontrada < quantidade_esperada
**Validates: Requirements 3.4**

### Property 6: Estatísticas são consistentes com dados filtrados
*For any* conjunto de filtros aplicados, as estatísticas exibidas (total, completos, incompletos, taxa) devem ser matematicamente consistentes com os dados visíveis na tabela
**Validates: Requirements 4.2**

### Property 7: Cor da taxa de integridade segue regra de 80%
*For any* taxa de integridade calculada, se taxa < 80% então cor deve ser vermelha, se taxa >= 80% então cor deve ser verde
**Validates: Requirements 4.3, 4.4**

### Property 8: Exportação Excel contém estrutura correta
*For any* exportação em formato Excel, o arquivo deve conter exatamente 2 abas nomeadas "Resumo" e "Detalhamento"
**Validates: Requirements 5.2**

### Property 9: Exportação CSV usa formato correto
*For any* exportação em formato CSV, o arquivo deve usar ponto-e-vírgula como separador e encoding UTF-8
**Validates: Requirements 5.4**

### Property 10: Ordenação mantém dados corretos
*For any* coluna ordenada, os dados devem estar ordenados corretamente (crescente ou decrescente) e a ordenação não deve alterar os filtros ativos
**Validates: Requirements 6.1, 6.2, 6.3**

### Property 11: Ordenação numérica para taxa de integridade
*For any* ordenação pela coluna "Taxa de Integridade", os valores devem ser ordenados numericamente (ex: 10% < 100%, não "100%" < "10%")
**Validates: Requirements 6.4**

### Property 12: Detalhamento exibe todos os componentes
*For any* item composto selecionado para detalhamento, o diálogo deve exibir todos os componentes cadastrados com seus campos completos (tipo, descrição, qtd esperada, qtd encontrada, status)
**Validates: Requirements 7.3**

### Property 13: Destaque de componentes faltantes no detalhamento
*For any* componente com quantidade_encontrada < quantidade_esperada, a linha deve ser destacada em vermelho no diálogo de detalhamento
**Validates: Requirements 7.4**

---

## Error Handling

### Validation Errors

#### FiltroInvalidoException
```java
public class FiltroInvalidoException extends Exception {
    // Erros possíveis:
    // - "Inventário não encontrado"
    // - "Setor não encontrado"
    // - "Combinação de filtros inválida"
}
```

#### ExportacaoException
```java
public class ExportacaoException extends Exception {
    private FormatoExportacao formato;
    private String caminhoArquivo;
    
    // Possíveis causas:
    // - Permissão negada para escrever arquivo
    // - Disco cheio
    // - Caminho inválido
    // - Erro ao gerar PDF/Excel
}
```

### Error Recovery Strategies

1. **Validação Preventiva**: Validar filtros antes de executar query
2. **Timeout de Query**: Limitar tempo de execução para queries longas
3. **Fallback de Exportação**: Se PDF falhar, sugerir Excel
4. **Mensagens Amigáveis**: Traduzir erros técnicos para linguagem do usuário
5. **Logging Detalhado**: Registrar todos os erros com stack trace

---

## Testing Strategy

### Unit Testing

#### Testes de Cálculo
- Calcular taxa de integridade com diferentes cenários
- Verificar classificação de status (completo/incompleto/parcial)
- Validar contagem de componentes faltantes

#### Testes de Filtragem
- Filtrar por inventário único
- Filtrar por múltiplos critérios
- Limpar filtros

#### Testes de Ordenação
- Ordenar por cada coluna
- Inverter ordenação
- Ordenação numérica vs alfabética

### Property-Based Testing

**Framework**: JUnit 5 + jqwik

#### Property Test 1: Consistência de Estatísticas
```java
@Property
void estatisticasConsistentesComDados(@ForAll FiltroRelatorioItemComposto filtro) {
    List<ItemCompostoResumo> itens = service.buscarComFiltros(filtro);
    EstatisticasIntegridade stats = service.calcularEstatisticas(filtro);
    
    // Total deve ser igual ao tamanho da lista
    assertEquals(itens.size(), stats.getTotalConjuntos());
    
    // Completos + Incompletos deve ser igual ao total
    assertEquals(stats.getTotalConjuntos(), 
                 stats.getConjuntosCompletos() + stats.getConjuntosIncompletos());
    
    // Contar manualmente e comparar
    long completos = itens.stream().filter(ItemCompostoResumo::isCompleto).count();
    assertEquals(completos, stats.getConjuntosCompletos());
}
```

#### Property Test 2: Filtragem AND Lógica
```java
@Property
void filtrosAplicamANDLogico(@ForAll FiltroRelatorioItemComposto filtro) {
    List<ItemCompostoResumo> resultados = service.buscarComFiltros(filtro);
    
    for (ItemCompostoResumo item : resultados) {
        if (filtro.getIdSetor() != null) {
            assertEquals(filtro.getIdSetor(), item.getIdSetor());
        }
        if (filtro.getIdSala() != null) {
            assertEquals(filtro.getIdSala(), item.getIdSala());
        }
        if (filtro.getStatusIntegridade() == StatusIntegridade.COMPLETO) {
            assertTrue(item.isCompleto());
        }
        if (filtro.getStatusIntegridade() == StatusIntegridade.INCOMPLETO) {
            assertFalse(item.isCompleto());
        }
    }
}
```

#### Property Test 3: Ordenação Correta
```java
@Property
void ordenacaoMantemOrdem(@ForAll List<ItemCompostoResumo> itens, 
                          @ForAll String coluna,
                          @ForAll boolean crescente) {
    List<ItemCompostoResumo> ordenados = service.ordenar(itens, coluna, crescente);
    
    // Verificar que está ordenado
    for (int i = 0; i < ordenados.size() - 1; i++) {
        Comparable val1 = getValorColuna(ordenados.get(i), coluna);
        Comparable val2 = getValorColuna(ordenados.get(i + 1), coluna);
        
        if (crescente) {
            assertTrue(val1.compareTo(val2) <= 0);
        } else {
            assertTrue(val1.compareTo(val2) >= 0);
        }
    }
}
```

#### Property Test 4: Estrutura Excel
```java
@Property
void excelTemEstruturaCor(@ForAll RelatorioItemComposto relatorio) {
    File arquivo = exportacaoService.exportarExcel(relatorio, tempFile);
    
    try (Workbook wb = WorkbookFactory.create(arquivo)) {
        assertEquals(2, wb.getNumberOfSheets());
        assertEquals("Resumo", wb.getSheetAt(0).getSheetName());
        assertEquals("Detalhamento", wb.getSheetAt(1).getSheetName());
    }
}
```

### Integration Testing

- Fluxo completo: abrir relatório → filtrar → ordenar → exportar
- Teste com dados reais (1.588 itens compostos)
- Teste de performance (< 3 segundos para carregar)

