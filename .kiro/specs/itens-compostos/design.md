# Design Document - Itens Compostos

## Overview

Este documento descreve o design técnico da funcionalidade de **Itens Compostos** para a aplicação desktop do Sistema de Inventário Patrimonial. A solução permite gerenciar patrimônios compostos por múltiplos componentes físicos e gerar relatórios detalhados sobre a integridade desses conjuntos.

### Objetivos do Design

1. **Preservar Estrutura Existente**: Adicionar funcionalidade sem modificar tabelas principais
2. **Performance**: Consultas otimizadas para relatórios com grandes volumes de dados
3. **Usabilidade**: Interface intuitiva integrada ao sistema existente
4. **Extensibilidade**: Facilitar adição de novos tipos de análises no futuro
5. **Compatibilidade**: Manter compatibilidade com app mobile (que ignora componentes)

### Princípios de Design

- **Minimal Invasiveness**: Novas tabelas sem alterar TABELA_PATRIMONIO
- **Backward Compatibility**: Sistema funciona normalmente para patrimônios não compostos
- **Data Integrity**: Constraints e validações garantem consistência
- **Reporting First**: Design otimizado para geração de relatórios

---

## Architecture

### Camadas da Aplicação

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  - ItemCompostoFrame (gestão de componentes)                 │
│  - RelatorioItemCompostoFrame (relatórios)                   │
│  - PatrimonioFrame (indicadores visuais)                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    Service Layer                             │
│  - ItemCompostoService (lógica de negócio)                   │
│  - RelatorioItemCompostoService (geração de relatórios)      │
│  - DeteccaoAutomaticaService (análise de padrões)            │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    DAO Layer                                 │
│  - ItemCompostoDAO (CRUD componentes)                        │
│  - ComponenteColetaDAO (status de coleta)                    │
│  - PadraoDeteccaoDAO (padrões configurados)                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    Database Layer                            │
│  - TABELA_ITEM_COMPOSTO                                      │
│  - TABELA_COMPONENTE                                         │
│  - TABELA_COMPONENTE_COLETA                                  │
│  - TABELA_PADRAO_DETECCAO                                    │
└─────────────────────────────────────────────────────────────┘
```

### Integração com Sistema Existente

```
MainFrame
    │
    ├─── Menu "Patrimônios"
    │       └─── "Gerenciar Itens Compostos" → ItemCompostoFrame
    │
    └─── Menu "Relatórios"
            └─── "Relatório de Itens Compostos" → RelatorioItemCompostoFrame

PatrimonioFrame (existente)
    └─── Adicionar coluna "Componentes" com indicador visual
```

---

## Components and Interfaces

### 1. Model Classes

#### ItemComposto.java
```java
public class ItemComposto {
    private Integer id;
    private Integer idPatrimonio;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private boolean deteccaoAutomatica;
    private Date dataCriacao;
    private Integer idUsuarioCriacao;
    private List<Componente> componentes;
    
    // Métodos de negócio
    public int getTotalComponentes();
    public boolean isCompleto(Integer idInventario);
    public double getTaxaIntegridade(Integer idInventario);
}
```

#### Componente.java
```java
public class Componente {
    private Integer id;
    private Integer idItemComposto;
    private String tipo;              // "CADEIRA", "MESA", "MONITOR", etc
    private String descricao;
    private int quantidadeEsperada;
    private int ordem;                // Para ordenação na exibição
    
    // Métodos de negócio
    public int getQuantidadeEncontrada(Integer idInventario);
    public int getQuantidadeFaltante(Integer idInventario);
    public boolean isCompleto(Integer idInventario);
}
```

#### ComponenteColeta.java
```java
public class ComponenteColeta {
    private Integer id;
    private Integer idComponente;
    private Integer idColeta;
    private Integer idInventario;
    private int quantidadeEncontrada;
    private String observacoes;
    private Date dataRegistro;
    private Integer idUsuarioRegistro;
}
```

#### PadraoDeteccao.java
```java
public class PadraoDeteccao {
    private Integer id;
    private String nome;
    private String descricaoPadrao;   // Regex ou palavras-chave
    private List<ComponentePadrao> componentesPadrao;
    private boolean ativo;
    private int prioridade;
}
```

### 2. DAO Interfaces

#### ItemCompostoDAO.java
```java
public interface ItemCompostoDAO {
    // CRUD básico
    ItemComposto inserir(ItemComposto item);
    ItemComposto atualizar(ItemComposto item);
    void excluir(Integer id);
    ItemComposto buscarPorId(Integer id);
    ItemComposto buscarPorPatrimonio(Integer idPatrimonio);
    
    // Consultas para relatórios
    List<ItemComposto> listarTodos();
    List<ItemComposto> listarPorInventario(Integer idInventario);
    List<ItemComposto> listarIncompletos(Integer idInventario);
    List<ItemComposto> listarComFiltros(FiltroRelatorio filtro);
    
    // Estatísticas
    int contarTotal();
    int contarCompletos(Integer idInventario);
    int contarIncompletos(Integer idInventario);
    double calcularTaxaIntegridadeGeral(Integer idInventario);
}
```

#### ComponenteDAO.java
```java
public interface ComponenteDAO {
    Componente inserir(Componente componente);
    Componente atualizar(Componente componente);
    void excluir(Integer id);
    List<Componente> listarPorItemComposto(Integer idItemComposto);
    
    // Estatísticas por tipo
    Map<String, EstatisticaComponente> obterEstatisticasPorTipo(Integer idInventario);
    List<String> listarTiposComponentes();
}
```

#### ComponenteColetaDAO.java
```java
public interface ComponenteColetaDAO {
    ComponenteColeta registrar(ComponenteColeta coleta);
    ComponenteColeta atualizar(ComponenteColeta coleta);
    List<ComponenteColeta> listarPorInventario(Integer idInventario);
    List<ComponenteColeta> listarPorComponente(Integer idComponente, Integer idInventario);
    
    // Consultas específicas
    int obterQuantidadeEncontrada(Integer idComponente, Integer idInventario);
    boolean isComponenteColetado(Integer idComponente, Integer idInventario);
}
```

### 3. Service Classes

#### ItemCompostoService.java
```java
public class ItemCompostoService {
    private ItemCompostoDAO itemCompostoDAO;
    private ComponenteDAO componenteDAO;
    private PatrimonioDAO patrimonioDAO;
    
    // Gestão de itens compostos
    public ItemComposto criarItemComposto(Integer idPatrimonio, List<Componente> componentes);
    public void adicionarComponente(Integer idItemComposto, Componente componente);
    public void removerComponente(Integer idComponente);
    public ItemComposto atualizarComponentes(Integer idItemComposto, List<Componente> componentes);
    
    // Validações
    public void validarItemComposto(ItemComposto item) throws ValidationException;
    public boolean patrimonioJaEhComposto(Integer idPatrimonio);
}
```

#### RelatorioItemCompostoService.java
```java
public class RelatorioItemCompostoService {
    private ItemCompostoDAO itemCompostoDAO;
    private ComponenteDAO componenteDAO;
    private ComponenteColetaDAO componenteColetaDAO;
    
    // Geração de relatórios
    public RelatorioItensCompostos gerarRelatorio(FiltroRelatorio filtro);
    public EstatisticasGerais calcularEstatisticasGerais(Integer idInventario);
    public Map<String, EstatisticaComponente> calcularEstatisticasPorTipo(Integer idInventario);
    
    // Análise comparativa
    public RelatorioComparativo compararInventarios(List<Integer> idsInventarios);
    public HistoricoIntegridade obterHistorico(Integer idItemComposto);
    
    // Exportação
    public File exportarExcel(RelatorioItensCompostos relatorio, String caminhoArquivo);
    public File exportarPDF(RelatorioItensCompostos relatorio, String caminhoArquivo);
    public File exportarCSV(RelatorioItensCompostos relatorio, String caminhoArquivo);
}
```

#### DeteccaoAutomaticaService.java
```java
public class DeteccaoAutomaticaService {
    private PadraoDeteccaoDAO padraoDAO;
    private ItemCompostoDAO itemCompostoDAO;
    
    // Detecção automática
    public List<SugestaoItemComposto> detectarItensCompostos(List<Patrimonio> patrimonios);
    public SugestaoItemComposto analisarDescricao(String descricao);
    public void aplicarSugestao(SugestaoItemComposto sugestao);
    
    // Gestão de padrões
    public PadraoDeteccao criarPadrao(PadraoDeteccao padrao);
    public void testarPadrao(PadraoDeteccao padrao);
    public int contarPatrimoniosAfetados(PadraoDeteccao padrao);
}
```

#### DeteccaoEmLoteService.java
```java
public class DeteccaoEmLoteService {
    private PatrimonioDAO patrimonioDAO;
    private ItemCompostoService itemCompostoService;
    private DeteccaoAutomaticaService deteccaoService;
    
    // Agrupamento por descrição
    public Map<String, DescricaoAgrupada> agruparPorDescricao();
    public DescricaoAgrupada obterDetalhesDescricao(String descricao);
    
    // Análise e sugestão
    public SugestaoComponentes analisarDescricao(String descricao);
    public List<Componente> extrairComponentesSugeridos(String descricao);
    
    // Aplicação em lote
    public ResultadoAplicacaoLote aplicarEmLote(
        String descricao, 
        List<Componente> componentes,
        ProgressCallback callback
    ) throws AplicacaoLoteException;
    
    // Validação
    public void validarAplicacaoLote(String descricao, List<Componente> componentes);
    public boolean existemPatrimoniosComDescricao(String descricao);
}

// Classes auxiliares
public class DescricaoAgrupada {
    private String descricao;
    private int quantidadePatrimonios;
    private List<Integer> idsPatrimonios;
    private boolean jaConfigurado;
    private SugestaoComponentes sugestao;
}

public class SugestaoComponentes {
    private String descricao;
    private List<Componente> componentesSugeridos;
    private String padraoDetectado;
    private double confianca;  // 0.0 a 1.0
}

public class ResultadoAplicacaoLote {
    private int patrimoniosConfigurados;
    private int componentesCriados;
    private long tempoExecucaoMs;
    private List<String> erros;
    private boolean sucesso;
}

public interface ProgressCallback {
    void onProgress(int atual, int total, String mensagem);
    boolean isCancelado();
}
```

#### DeteccaoEmLoteService.java
```java
public class DeteccaoEmLoteService {
    private PatrimonioDAO patrimonioDAO;
    private ItemCompostoDAO itemCompostoDAO;
    private ComponenteDAO componenteDAO;
    private DeteccaoAutomaticaService deteccaoService;
    
    // Agrupamento por descrição
    public Map<String, DescricaoAgrupada> agruparPorDescricao();
    public DescricaoAgrupada obterDetalhesPorDescricao(String descricao);
    
    // Análise e sugestão
    public SugestaoComponentes analisarDescricao(String descricao);
    
    // Aplicação em lote
    public ResultadoAplicacaoLote aplicarEmLote(
        String descricao, 
        List<Componente> componentes,
        ProgressCallback callback
    ) throws AplicacaoLoteException;
    
    // Cancelamento
    public void cancelarAplicacao();
}

// Classes auxiliares
public class DescricaoAgrupada {
    private String descricao;
    private int quantidadePatrimonios;
    private List<Integer> idsPatrimonios;
    private SugestaoComponentes sugestaoAutomatica;
}

public class SugestaoComponentes {
    private String descricao;
    private List<Componente> componentesSugeridos;
    private String padraoDetectado;
    private double confianca; // 0.0 a 1.0
}

public class ResultadoAplicacaoLote {
    private int patrimoniosConfigurados;
    private int componentesCriados;
    private long tempoExecucaoMs;
    private boolean cancelado;
    private List<String> erros;
}

public interface ProgressCallback {
    void onProgress(int atual, int total, String mensagem);
    boolean isCancelado();
}
```

---

## Data Models

### Database Schema

#### TABELA_ITEM_COMPOSTO
```sql
CREATE TABLE TABELA_ITEM_COMPOSTO (
    ID SERIAL PRIMARY KEY,
    ID_PATRIMONIO INTEGER NOT NULL UNIQUE,
    DETECCAO_AUTOMATICA BOOLEAN DEFAULT FALSE,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ID_USUARIO_CRIACAO INTEGER,
    OBSERVACOES TEXT,
    
    FOREIGN KEY (ID_PATRIMONIO) REFERENCES TABELA_PATRIMONIO(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_USUARIO_CRIACAO) REFERENCES TABELA_USUARIO(ID)
);

CREATE INDEX idx_item_composto_patrimonio ON TABELA_ITEM_COMPOSTO(ID_PATRIMONIO);
```

#### TABELA_COMPONENTE
```sql
CREATE TABLE TABELA_COMPONENTE (
    ID SERIAL PRIMARY KEY,
    ID_ITEM_COMPOSTO INTEGER NOT NULL,
    TIPO VARCHAR(100) NOT NULL,
    DESCRICAO VARCHAR(255) NOT NULL,
    QUANTIDADE_ESPERADA INTEGER NOT NULL DEFAULT 1,
    ORDEM INTEGER DEFAULT 0,
    
    FOREIGN KEY (ID_ITEM_COMPOSTO) REFERENCES TABELA_ITEM_COMPOSTO(ID) ON DELETE CASCADE,
    
    CONSTRAINT chk_quantidade_positiva CHECK (QUANTIDADE_ESPERADA > 0),
    CONSTRAINT chk_descricao_minima CHECK (LENGTH(DESCRICAO) >= 3)
);

CREATE INDEX idx_componente_item_composto ON TABELA_COMPONENTE(ID_ITEM_COMPOSTO);
CREATE INDEX idx_componente_tipo ON TABELA_COMPONENTE(TIPO);
```

#### TABELA_COMPONENTE_COLETA
```sql
CREATE TABLE TABELA_COMPONENTE_COLETA (
    ID SERIAL PRIMARY KEY,
    ID_COMPONENTE INTEGER NOT NULL,
    ID_COLETA INTEGER NOT NULL,
    ID_INVENTARIO INTEGER NOT NULL,
    QUANTIDADE_ENCONTRADA INTEGER NOT NULL DEFAULT 0,
    OBSERVACOES TEXT,
    DATA_REGISTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ID_USUARIO_REGISTRO INTEGER,
    
    FOREIGN KEY (ID_COMPONENTE) REFERENCES TABELA_COMPONENTE(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_COLETA) REFERENCES TABELA_COLETA(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    FOREIGN KEY (ID_USUARIO_REGISTRO) REFERENCES TABELA_USUARIO(ID),
    
    CONSTRAINT chk_quantidade_nao_negativa CHECK (QUANTIDADE_ENCONTRADA >= 0),
    CONSTRAINT uk_componente_coleta UNIQUE (ID_COMPONENTE, ID_COLETA)
);

CREATE INDEX idx_componente_coleta_componente ON TABELA_COMPONENTE_COLETA(ID_COMPONENTE);
CREATE INDEX idx_componente_coleta_inventario ON TABELA_COMPONENTE_COLETA(ID_INVENTARIO);
```

#### TABELA_PADRAO_DETECCAO
```sql
CREATE TABLE TABELA_PADRAO_DETECCAO (
    ID SERIAL PRIMARY KEY,
    NOME VARCHAR(100) NOT NULL UNIQUE,
    DESCRICAO_PADRAO TEXT NOT NULL,
    TIPO_PADRAO VARCHAR(50) DEFAULT 'PALAVRAS_CHAVE',  -- 'PALAVRAS_CHAVE' ou 'REGEX'
    ATIVO BOOLEAN DEFAULT TRUE,
    PRIORIDADE INTEGER DEFAULT 0,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ID_USUARIO_CRIACAO INTEGER,
    
    FOREIGN KEY (ID_USUARIO_CRIACAO) REFERENCES TABELA_USUARIO(ID)
);

CREATE INDEX idx_padrao_ativo ON TABELA_PADRAO_DETECCAO(ATIVO);
CREATE INDEX idx_padrao_prioridade ON TABELA_PADRAO_DETECCAO(PRIORIDADE DESC);
```

#### TABELA_COMPONENTE_PADRAO
```sql
CREATE TABLE TABELA_COMPONENTE_PADRAO (
    ID SERIAL PRIMARY KEY,
    ID_PADRAO_DETECCAO INTEGER NOT NULL,
    TIPO VARCHAR(100) NOT NULL,
    DESCRICAO VARCHAR(255) NOT NULL,
    QUANTIDADE_PADRAO INTEGER DEFAULT 1,
    ORDEM INTEGER DEFAULT 0,
    
    FOREIGN KEY (ID_PADRAO_DETECCAO) REFERENCES TABELA_PADRAO_DETECCAO(ID) ON DELETE CASCADE
);

CREATE INDEX idx_componente_padrao_deteccao ON TABELA_COMPONENTE_PADRAO(ID_PADRAO_DETECCAO);
```

### Views para Relatórios

#### VIEW_ITEM_COMPOSTO_RESUMO
```sql
CREATE OR REPLACE VIEW VIEW_ITEM_COMPOSTO_RESUMO AS
SELECT 
    ic.ID,
    ic.ID_PATRIMONIO,
    p.NUMERO AS NUMERO_PATRIMONIO,
    p.DESCRICAO AS DESCRICAO_PATRIMONIO,
    p.ID_SALA,
    s.NOME AS NOME_SALA,
    s.ID_SETOR,
    st.NOME AS NOME_SETOR,
    COUNT(c.ID) AS TOTAL_COMPONENTES,
    ic.DETECCAO_AUTOMATICA,
    ic.DATA_CRIACAO
FROM TABELA_ITEM_COMPOSTO ic
INNER JOIN TABELA_PATRIMONIO p ON ic.ID_PATRIMONIO = p.ID
LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA
LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID
LEFT JOIN TABELA_COMPONENTE c ON ic.ID = c.ID_ITEM_COMPOSTO
GROUP BY ic.ID, ic.ID_PATRIMONIO, p.NUMERO, p.DESCRICAO, p.ID_SALA, 
         s.NOME, s.ID_SETOR, st.NOME, ic.DETECCAO_AUTOMATICA, ic.DATA_CRIACAO;
```

#### VIEW_COMPONENTE_STATUS_INVENTARIO
```sql
CREATE OR REPLACE VIEW VIEW_COMPONENTE_STATUS_INVENTARIO AS
SELECT 
    c.ID AS ID_COMPONENTE,
    c.ID_ITEM_COMPOSTO,
    c.TIPO,
    c.DESCRICAO,
    c.QUANTIDADE_ESPERADA,
    i.ID AS ID_INVENTARIO,
    i.NOME AS NOME_INVENTARIO,
    COALESCE(SUM(cc.QUANTIDADE_ENCONTRADA), 0) AS QUANTIDADE_ENCONTRADA,
    c.QUANTIDADE_ESPERADA - COALESCE(SUM(cc.QUANTIDADE_ENCONTRADA), 0) AS QUANTIDADE_FALTANTE,
    CASE 
        WHEN COALESCE(SUM(cc.QUANTIDADE_ENCONTRADA), 0) >= c.QUANTIDADE_ESPERADA THEN 'COMPLETO'
        WHEN COALESCE(SUM(cc.QUANTIDADE_ENCONTRADA), 0) > 0 THEN 'PARCIAL'
        ELSE 'FALTANTE'
    END AS STATUS
FROM TABELA_COMPONENTE c
CROSS JOIN TABELA_INVENTARIO i
LEFT JOIN TABELA_COMPONENTE_COLETA cc ON c.ID = cc.ID_COMPONENTE AND i.ID = cc.ID_INVENTARIO
GROUP BY c.ID, c.ID_ITEM_COMPOSTO, c.TIPO, c.DESCRICAO, c.QUANTIDADE_ESPERADA, i.ID, i.NOME;
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Validação de descrição de componente
*For any* componente sendo adicionado a um item composto, se a descrição possui menos de 3 caracteres, então a validação deve falhar e o componente não deve ser persistido
**Validates: Requirements 1.3**

### Property 2: Persistência de componentes (Round Trip)
*For any* item composto com N componentes, após salvar no banco de dados e recarregar, o sistema deve retornar exatamente N componentes com os mesmos dados (tipo, descrição, quantidade)
**Validates: Requirements 1.5**

### Property 3: Completude de relatório
*For any* conjunto de itens compostos no banco de dados, o relatório gerado deve incluir todos os itens compostos sem exceção, com todos os campos obrigatórios preenchidos (número patrimônio, descrição, localização, componentes esperados, encontrados e faltantes)
**Validates: Requirements 2.3**

### Property 4: Consistência de estatísticas
*For any* relatório exibido, as estatísticas resumidas (total de conjuntos, completos, incompletos, taxa de integridade) devem ser matematicamente consistentes com os dados detalhados do relatório
**Validates: Requirements 2.5**

### Property 5: Estrutura de exportação Excel
*For any* exportação em formato Excel, o arquivo gerado deve conter exatamente 4 abas nomeadas: "Resumo Geral", "Conjuntos Completos", "Conjuntos Incompletos" e "Detalhamento por Componente"
**Validates: Requirements 3.2**

### Property 6: Completude de agrupamento por tipo
*For any* conjunto de componentes no sistema, o agrupamento por tipo deve incluir todos os tipos distintos presentes, sem omissões ou duplicações
**Validates: Requirements 4.1**

### Property 7: Invariante matemática de componentes
*For any* tipo de componente em um inventário, a seguinte invariante deve sempre ser verdadeira: total_esperado = total_encontrado + total_faltante
**Validates: Requirements 4.2**

### Property 8: Ordenação por taxa de ausência
*For any* lista de componentes agrupados por tipo, a lista deve estar ordenada de forma decrescente pela taxa de ausência (componentes com maior taxa de ausência aparecem primeiro)
**Validates: Requirements 4.3**

### Property 9: Filtragem correta por tipo de componente
*For any* tipo de componente selecionado no relatório estatístico, todos os patrimônios retornados no detalhamento devem possuir aquele tipo de componente com quantidade faltante > 0
**Validates: Requirements 4.4**

### Property 10: Análise automática de descrição
*For any* patrimônio cadastrado ou importado, o sistema deve executar a análise de detecção de padrões na descrição, independentemente do conteúdo
**Validates: Requirements 5.1**

### Property 11: Detecção de padrões conhecidos
*For any* descrição de patrimônio contendo palavras-chave configuradas nos padrões ativos (ex: "COM", "E", "+"), o sistema deve gerar uma sugestão de item composto
**Validates: Requirements 5.2**

### Property 12: Extração consistente de componentes
*For any* padrão detectado em uma descrição, os componentes extraídos automaticamente devem corresponder exatamente aos componentes definidos no padrão de detecção
**Validates: Requirements 5.3**

### Property 13: Completude de processamento em lote
*For any* execução de detecção em lote sobre N patrimônios, o sistema deve processar exatamente N patrimônios, sem omitir nenhum
**Validates: Requirements 5.5**

### Property 14: Validação de padrão de detecção
*For any* padrão de detecção sendo criado, se não possui descrição principal OU não possui lista de componentes, então a validação deve falhar e o padrão não deve ser persistido
**Validates: Requirements 6.2**

### Property 15: Aplicação automática de padrões
*For any* patrimônio importado após a configuração de um padrão ativo, se a descrição do patrimônio corresponde ao padrão, então o sistema deve aplicar automaticamente a detecção
**Validates: Requirements 6.3**

### Property 16: Contagem precisa de patrimônios afetados
*For any* padrão de detecção testado, o número de patrimônios afetados retornado deve ser exatamente igual ao número de patrimônios cuja descrição corresponde ao padrão
**Validates: Requirements 6.4**

### Property 17: Filtragem com AND lógico
*For any* conjunto de filtros aplicados simultaneamente (inventário, setor, sala, status), todos os registros retornados no relatório devem satisfazer TODOS os critérios de filtro (operação AND lógica)
**Validates: Requirements 7.2**

### Property 18: Consistência de contador de filtros
*For any* estado de filtros aplicados, o contador exibido de "registros filtrados vs total" deve refletir exatamente o número de registros visíveis na tabela
**Validates: Requirements 7.4**

### Property 19: Persistência de configuração de filtros (Round Trip)
*For any* configuração de filtros salva com um nome, ao recuperar e aplicar essa configuração, os filtros aplicados devem ser idênticos aos filtros originalmente salvos
**Validates: Requirements 7.5**

### Property 20: Indicador visual para todos os compostos
*For any* patrimônio marcado como composto (existe em TABELA_ITEM_COMPOSTO), a listagem de patrimônios deve exibir o ícone indicador correspondente
**Validates: Requirements 8.1**

### Property 21: Filtragem exclusiva de compostos
*For any* aplicação do filtro "apenas compostos", todos os patrimônios retornados devem ter um registro correspondente em TABELA_ITEM_COMPOSTO
**Validates: Requirements 8.3**

### Property 22: Contagem correta de componentes no badge
*For any* patrimônio composto visualizado, o badge de contagem deve exibir exatamente o número de registros em TABELA_COMPONENTE associados àquele item composto
**Validates: Requirements 8.4**

### Property 23: Alerta condicional para componentes faltantes
*For any* patrimônio composto em um inventário, se existe pelo menos um componente com quantidade_faltante > 0, então o ícone de alerta deve ser exibido
**Validates: Requirements 8.5**

### Property 24: Completude de comparação entre inventários
*For any* conjunto de inventários selecionados para comparação, a tabela comparativa deve incluir uma linha para cada item composto e uma coluna para cada inventário selecionado, com todas as taxas de integridade calculadas
**Validates: Requirements 9.2**

### Property 25: Detecção de piora na integridade
*For any* item composto presente em dois inventários consecutivos I1 e I2, se taxa_integridade(I2) < taxa_integridade(I1), então o item deve ser destacado visualmente como tendo piorado
**Validates: Requirements 9.3**

### Property 26: Completude de histórico temporal
*For any* item composto selecionado, a linha do tempo deve incluir um ponto para cada inventário onde aquele item foi coletado, ordenados cronologicamente
**Validates: Requirements 9.4**

### Property 27: Agrupamento correto por descrição
*For any* execução de agrupamento por descrição, todos os patrimônios com descrição exatamente igual devem ser agrupados juntos, sem omissões ou duplicações
**Validates: Requirements 10.1**

### Property 28: Aplicação em lote atômica
*For any* aplicação em lote de componentes, se a operação for cancelada ou falhar, então NENHUM patrimônio deve ser parcialmente configurado (ou todos são configurados ou nenhum é)
**Validates: Requirements 10.4**

### Property 29: Consistência de contagem em lote
*For any* descrição selecionada para aplicação em lote, o número de patrimônios configurados ao final deve ser exatamente igual ao número exibido antes da aplicação
**Validates: Requirements 10.6**

### Property 30: Progresso monotônico
*For any* aplicação em lote em execução, o progresso reportado deve ser sempre crescente (nunca diminuir) e estar entre 0% e 100%
**Validates: Requirements 10.5**

---

## Error Handling

### Validation Errors

#### ItemCompostoValidationException
```java
public class ItemCompostoValidationException extends Exception {
    private List<String> erros;
    
    // Erros possíveis:
    // - "Patrimônio não encontrado"
    // - "Patrimônio já é um item composto"
    // - "Nenhum componente definido"
    // - "Descrição de componente muito curta (mínimo 3 caracteres)"
    // - "Quantidade esperada deve ser maior que zero"
}
```

#### RelatorioException
```java
public class RelatorioException extends Exception {
    private String tipoErro;  // "FILTRO_INVALIDO", "EXPORTACAO_FALHOU", "SEM_DADOS"
    
    // Tratamento específico por tipo
}
```

### Database Errors

- **Constraint Violations**: Capturar e traduzir para mensagens amigáveis
- **Foreign Key Errors**: Validar existência de patrimônio antes de criar item composto
- **Unique Violations**: Verificar se patrimônio já é composto antes de inserir

### Export Errors

```java
public class ExportacaoException extends Exception {
    private String formato;  // "EXCEL", "PDF", "CSV"
    private String caminhoArquivo;
    
    // Possíveis causas:
    // - Permissão negada para escrever arquivo
    // - Disco cheio
    // - Caminho inválido
    // - Erro ao gerar gráficos (PDF)
}
```

### Error Recovery Strategies

1. **Validação Preventiva**: Validar dados antes de persistir
2. **Transações**: Usar transações para operações que modificam múltiplas tabelas
3. **Rollback Automático**: Em caso de erro, reverter todas as mudanças
4. **Logging Detalhado**: Registrar todos os erros com stack trace
5. **Mensagens Amigáveis**: Traduzir erros técnicos para linguagem do usuário

---

## Testing Strategy

### Unit Testing

#### Testes de Validação
- Validar descrição de componente com diferentes tamanhos
- Validar quantidade esperada (positiva, zero, negativa)
- Validar padrão de detecção sem descrição
- Validar padrão de detecção sem componentes

#### Testes de Cálculo
- Calcular taxa de integridade com diferentes cenários
- Calcular estatísticas por tipo de componente
- Verificar invariante matemática (esperado = encontrado + faltante)

#### Testes de Persistência
- Salvar e recuperar item composto com componentes
- Atualizar componentes existentes
- Excluir item composto (cascade para componentes)

### Property-Based Testing

**Framework**: JUnit 5 + jqwik (property-based testing para Java)

#### Property Test 1: Round Trip de Persistência
```java
@Property
void itemCompostoRoundTrip(@ForAll ItemComposto itemOriginal) {
    // Salvar
    ItemComposto itemSalvo = itemCompostoDAO.inserir(itemOriginal);
    
    // Recuperar
    ItemComposto itemRecuperado = itemCompostoDAO.buscarPorId(itemSalvo.getId());
    
    // Verificar
    assertEquals(itemOriginal.getComponentes().size(), 
                 itemRecuperado.getComponentes().size());
    // Verificar cada componente...
}
```

#### Property Test 2: Invariante Matemática
```java
@Property
void invarianteMatematicaComponentes(@ForAll Integer idInventario) {
    Map<String, EstatisticaComponente> stats = 
        componenteDAO.obterEstatisticasPorTipo(idInventario);
    
    for (EstatisticaComponente stat : stats.values()) {
        assertEquals(stat.getTotalEsperado(), 
                     stat.getTotalEncontrado() + stat.getTotalFaltante());
    }
}
```

#### Property Test 3: Filtragem AND Lógica
```java
@Property
void filtrosAplicamANDLogico(@ForAll FiltroRelatorio filtro) {
    List<ItemComposto> resultados = 
        itemCompostoDAO.listarComFiltros(filtro);
    
    for (ItemComposto item : resultados) {
        assertTrue(filtro.matches(item));  // Deve satisfazer TODOS os filtros
    }
}
```

#### Property Test 4: Ordenação por Taxa de Ausência
```java
@Property
void componentesOrdenadosPorTaxaAusencia(@ForAll Integer idInventario) {
    Map<String, EstatisticaComponente> stats = 
        componenteDAO.obterEstatisticasPorTipo(idInventario);
    
    List<EstatisticaComponente> lista = new ArrayList<>(stats.values());
    
    // Verificar ordenação decrescente
    for (int i = 0; i < lista.size() - 1; i++) {
        assertTrue(lista.get(i).getTaxaAusencia() >= 
                   lista.get(i + 1).getTaxaAusencia());
    }
}
```

#### Property Test 5: Detecção de Padrões
```java
@Property
void padraoDetectadoGeraComponentesCorretos(@ForAll PadraoDeteccao padrao, 
                                             @ForAll String descricao) {
    assumeTrue(padrao.matches(descricao));  // Apenas se descrição corresponde
    
    SugestaoItemComposto sugestao = 
        deteccaoService.analisarDescricao(descricao);
    
    assertNotNull(sugestao);
    assertEquals(padrao.getComponentesPadrao().size(), 
                 sugestao.getComponentesSugeridos().size());
}
```

### Integration Testing

#### Teste de Fluxo Completo
1. Criar item composto com 3 componentes
2. Registrar coleta parcial (2 componentes encontrados)
3. Gerar relatório
4. Verificar estatísticas
5. Exportar para Excel
6. Validar estrutura do arquivo

#### Teste de Detecção Automática
1. Configurar padrão "MESA COM CADEIRA"
2. Importar patrimônio com descrição "MESA COM 4 CADEIRAS"
3. Verificar sugestão gerada
4. Aplicar sugestão
5. Verificar item composto criado com 2 componentes

#### Teste de Análise Comparativa
1. Criar 2 inventários
2. Criar item composto
3. Registrar coleta completa no inventário 1
4. Registrar coleta parcial no inventário 2
5. Gerar análise comparativa
6. Verificar detecção de piora

### Performance Testing

#### Testes de Carga
- Relatório com 10.000 itens compostos
- Exportação Excel com 50.000 linhas
- Detecção em lote de 100.000 patrimônios
- Análise comparativa de 10 inventários

#### Métricas Esperadas
- Geração de relatório: < 3 segundos para 10k itens
- Exportação Excel: < 10 segundos para 50k linhas
- Detecção em lote: < 30 segundos para 100k patrimônios
- Queries de estatísticas: < 1 segundo

---

## UI Design

### 1. ItemCompostoFrame - Gestão de Componentes

#### Layout Principal
```
┌─────────────────────────────────────────────────────────────┐
│ Gerenciar Itens Compostos                              [X]  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│ Patrimônio: [12345 - MESA COM 4 CADEIRAS        ] [Buscar] │
│                                                              │
│ ☑ Marcar como Item Composto                                 │
│                                                              │
│ ┌─ Componentes ────────────────────────────────────────┐   │
│ │                                                        │   │
│ │ Tipo         Descrição           Qtd    [Ações]      │   │
│ │ ──────────────────────────────────────────────────    │   │
│ │ MESA         Mesa escolar         1     [✏️] [🗑️]    │   │
│ │ CADEIRA      Cadeira escolar      4     [✏️] [🗑️]    │   │
│ │                                                        │   │
│ │ [+ Adicionar Componente]                              │   │
│ │                                                        │   │
│ └────────────────────────────────────────────────────────┘   │
│                                                              │
│ Detecção Automática: ☑ Ativada                              │
│ [Detectar Padrões]  [Sugestões: 2 encontradas]              │
│                                                              │
│                          [Salvar] [Cancelar]                 │
└─────────────────────────────────────────────────────────────┘
```

#### Dialog: Adicionar/Editar Componente
```
┌─────────────────────────────────────────┐
│ Adicionar Componente              [X]   │
├─────────────────────────────────────────┤
│                                          │
│ Tipo: [CADEIRA          ▼]              │
│       (Sugestões: MESA, CADEIRA,        │
│        MONITOR, TECLADO, MOUSE...)      │
│                                          │
│ Descrição: [Cadeira escolar fixa  ]     │
│            (mínimo 3 caracteres)        │
│                                          │
│ Quantidade: [4]                          │
│                                          │
│                                          │
│              [Salvar] [Cancelar]         │
└─────────────────────────────────────────┘
```

### 2. RelatorioItemCompostoFrame - Relatórios

#### Layout Principal
```
┌──────────────────────────────────────────────────────────────────────┐
│ Relatório de Itens Compostos                                    [X]  │
├──────────────────────────────────────────────────────────────────────┤
│ ┌─ Filtros ──────────────────────────────────────────────────────┐  │
│ │ Inventário: [Inventário 2024 ▼]  Setor: [Todos ▼]            │  │
│ │ Sala: [Todas ▼]  Status: [Todos ▼] [Aplicar] [Limpar]        │  │
│ │ Configurações Salvas: [Minhas Configs ▼] [Salvar Como...]    │  │
│ └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│ ┌─ Estatísticas Gerais ──────────────────────────────────────────┐  │
│ │ Total de Conjuntos: 150  │  Completos: 120 (80%)              │  │
│ │ Incompletos: 30 (20%)    │  Taxa Geral: 85%                   │  │
│ └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│ Mostrando 30 de 150 registros                                       │
│                                                                      │
│ ┌────────────────────────────────────────────────────────────────┐  │
│ │ Nº Pat.  Descrição        Local    Esperado  Encontrado  Falta│  │
│ │ ──────────────────────────────────────────────────────────────│  │
│ │ 12345    Mesa c/ cadeiras Sala 101    5         5          0  │  │
│ │ 12346 ⚠️ Mesa c/ cadeiras Sala 102    5         3          2  │  │
│ │ 12347 ⚠️ Estação trabalho Lab 01      4         2          2  │  │
│ │ ...                                                            │  │
│ └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│ [📊 Estatísticas por Tipo] [📈 Análise Comparativa]                │
│ [📥 Exportar Excel] [📄 Exportar PDF] [📋 Exportar CSV]            │
│                                                          [Fechar]    │
└──────────────────────────────────────────────────────────────────────┘
```

#### Dialog: Estatísticas por Tipo de Componente
```
┌──────────────────────────────────────────────────────────────┐
│ Estatísticas por Tipo de Componente                    [X]  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ ┌────────────────────────────────────────────────────────┐  │
│ │ Tipo      Esperado  Encontrado  Faltante  Taxa Presença│  │
│ │ ────────────────────────────────────────────────────────│  │
│ │ CADEIRA      600        450        150        75%      │  │
│ │ MESA         150        140         10        93%      │  │
│ │ MONITOR      200        180         20        90%      │  │
│ │ TECLADO      200        195          5        97%      │  │
│ │ MOUSE        200        198          2        99%      │  │
│ └────────────────────────────────────────────────────────┘  │
│                                                              │
│ [Gráfico de Barras: Taxa de Presença por Tipo]              │
│ ┌────────────────────────────────────────────────────────┐  │
│ │ MOUSE     ████████████████████████████████████████ 99% │  │
│ │ TECLADO   ████████████████████████████████████████ 97% │  │
│ │ MESA      ████████████████████████████████████     93% │  │
│ │ MONITOR   ████████████████████████████████         90% │  │
│ │ CADEIRA   ██████████████████████████               75% │  │
│ └────────────────────────────────────────────────────────┘  │
│                                                              │
│ Clique em um tipo para ver detalhes dos patrimônios         │
│                                                              │
│                                    [Exportar] [Fechar]       │
└──────────────────────────────────────────────────────────────┘
```

#### Dialog: Análise Comparativa entre Inventários
```
┌──────────────────────────────────────────────────────────────────┐
│ Análise Comparativa de Inventários                         [X]  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Selecionar Inventários:                                         │
│ ☑ Inventário 2023  ☑ Inventário 2024  ☐ Inventário 2025       │
│                                                    [Comparar]    │
│                                                                  │
│ ┌────────────────────────────────────────────────────────────┐  │
│ │ Patrimônio        2023    2024    Tendência               │  │
│ │ ──────────────────────────────────────────────────────────│  │
│ │ 12345 Mesa c/ cad  100%   100%    ━━━━━━━━━━━━━━━━━━━━  │  │
│ │ 12346 Mesa c/ cad  100%    60% ⬇️  ━━━━━━━━━━━━━━━━━━━━  │  │
│ │ 12347 Estação trab  75%    50% ⬇️  ━━━━━━━━━━━━━━━━━━━━  │  │
│ │ 12348 Mesa c/ cad   80%   100% ⬆️  ━━━━━━━━━━━━━━━━━━━━  │  │
│ └────────────────────────────────────────────────────────────┘  │
│                                                                  │
│ Resumo:                                                          │
│ • 1 conjunto manteve integridade (33%)                          │
│ • 2 conjuntos pioraram (67%) ⚠️                                 │
│ • 1 conjunto melhorou (33%)                                     │
│                                                                  │
│ [Ver Linha do Tempo] [Exportar Análise] [Fechar]               │
└──────────────────────────────────────────────────────────────────┘
```

### 3. Integração com PatrimonioFrame

#### Modificações na Listagem de Patrimônios
```
┌────────────────────────────────────────────────────────────────┐
│ Nº Patrimônio  Descrição              Sala      Responsável   │
│ ──────────────────────────────────────────────────────────────│
│ 12345 🧩(5)    Mesa com cadeiras      Sala 101  João Silva    │
│ 12346 🧩⚠️(5)  Mesa com cadeiras      Sala 102  Maria Santos  │
│ 12347          Cadeira individual     Sala 103  João Silva    │
│ 12348 🧩(4)    Estação de trabalho    Lab 01    Pedro Costa   │
└────────────────────────────────────────────────────────────────┘

Legenda:
🧩 = Item Composto
(N) = Número de componentes
⚠️ = Possui componentes faltantes
```

#### Tooltip ao Passar Mouse sobre Indicador
```
┌─────────────────────────────────┐
│ Item Composto (5 componentes)   │
│ ─────────────────────────────── │
│ • Mesa escolar (1)              │
│ • Cadeira escolar (4)           │
│                                  │
│ Status: 2 componentes faltantes │
│ Clique para gerenciar           │
└─────────────────────────────────┘
```

### 5. DeteccaoEmLoteDialog - Aplicação em Lote

#### Layout Principal
```
┌──────────────────────────────────────────────────────────────────┐
│ Detecção em Lote de Itens Compostos                        [X]  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Agrupe patrimônios por descrição e configure componentes        │
│ em lote para economizar tempo.                                  │
│                                                                  │
│ ┌─ Descrições Encontradas ────────────────────────────────────┐ │
│ │ Descrição                         | Qtd | Status  | [Ação] │ │
│ │ ──────────────────────────────────────────────────────────  │ │
│ │ MESA COM 4 CADEIRAS               | 500 | Pendente|[Config]│ │
│ │ ESTAÇÃO DE TRABALHO COMPLETA      | 150 | Pendente|[Config]│ │
│ │ COMPUTADOR COM MONITOR            | 80  | Pendente|[Config]│ │
│ │ MESA COM 2 CADEIRAS               | 45  | Config. | [Ver] │ │
│ │ ARMÁRIO COM 4 GAVETAS             | 30  | Pendente|[Config]│ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ 📊 Resumo:                                                       │
│ • Total de descrições: 5                                        │
│ • Total de patrimônios: 805                                     │
│ • Já configurados: 45 (5.6%)                                    │
│ • Pendentes: 760 (94.4%)                                        │
│                                                                  │
│ [🔄 Atualizar Lista] [📋 Exportar Lista] [Fechar]              │
└──────────────────────────────────────────────────────────────────┘
```

#### Dialog: Configurar Descrição em Lote
```
┌──────────────────────────────────────────────────────────────────┐
│ Configurar: MESA COM 4 CADEIRAS (500 patrimônios)          [X]  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ 🤖 Componentes Detectados Automaticamente (Confiança: 95%)      │
│                                                                  │
│ ┌─ Componentes ────────────────────────────────────────────┐    │
│ │ Tipo      | Descrição          | Qtd | [Ações]          │    │
│ │ ──────────────────────────────────────────────────────   │    │
│ │ MESA      | Mesa escolar       | 1   | [✏️] [🗑️]       │    │
│ │ CADEIRA   | Cadeira escolar    | 4   | [✏️] [🗑️]       │    │
│ │                                                           │    │
│ │ [+ Adicionar Componente]                                 │    │
│ └──────────────────────────────────────────────────────────┘    │
│                                                                  │
│ 📋 Patrimônios que serão afetados:                              │
│ • 12345 - MESA COM 4 CADEIRAS (Sala 101)                        │
│ • 12346 - MESA COM 4 CADEIRAS (Sala 102)                        │
│ • 12347 - MESA COM 4 CADEIRAS (Sala 103)                        │
│ • ... e mais 497 patrimônios                                    │
│                                                                  │
│ ⚠️ Esta ação irá:                                                │
│ • Criar 500 itens compostos                                     │
│ • Criar 1.000 componentes (500 × 2)                             │
│ • Operação não pode ser desfeita facilmente                     │
│                                                                  │
│ [✅ Aplicar em 500 Itens] [Cancelar]                            │
└──────────────────────────────────────────────────────────────────┘
```

#### Dialog: Progresso de Aplicação
```
┌──────────────────────────────────────────────────────────────────┐
│ Aplicando Configuração em Lote...                          [X]  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Processando patrimônios com descrição:                          │
│ "MESA COM 4 CADEIRAS"                                            │
│                                                                  │
│ Progresso Geral:                                                 │
│ ████████████████████████████████░░░░░░░░ 80% (400/500)          │
│                                                                  │
│ 📊 Estatísticas:                                                 │
│ • Itens configurados: 400                                       │
│ • Componentes criados: 800                                      │
│ • Tempo decorrido: 12s                                          │
│ • Tempo estimado restante: 3s                                   │
│ • Velocidade: ~33 itens/segundo                                 │
│                                                                  │
│ 📝 Último processado:                                            │
│ Patrimônio 12745 - MESA COM 4 CADEIRAS (Sala 205)              │
│                                                                  │
│ [⏸️ Pausar] [❌ Cancelar]                                        │
└──────────────────────────────────────────────────────────────────┘
```

#### Dialog: Resultado da Aplicação
```
┌──────────────────────────────────────────────────────────────────┐
│ Aplicação Concluída com Sucesso!                           [X]  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ ✅ Configuração aplicada com sucesso!                            │
│                                                                  │
│ 📊 Resumo da Operação:                                           │
│                                                                  │
│ Descrição: MESA COM 4 CADEIRAS                                  │
│                                                                  │
│ ✓ Patrimônios configurados: 500                                 │
│ ✓ Componentes criados: 1.000                                    │
│   • 500 × MESA (quantidade: 1)                                  │
│   • 500 × CADEIRA (quantidade: 4)                               │
│                                                                  │
│ ⏱️ Tempo total: 15 segundos                                      │
│ 🚀 Velocidade média: 33 itens/segundo                            │
│                                                                  │
│ 💾 Todas as alterações foram salvas no banco de dados.          │
│                                                                  │
│ [📋 Ver Itens Configurados] [✅ Fechar]                          │
└──────────────────────────────────────────────────────────────────┘
```

### 4. Menu Principal - Novas Opções

```
MainFrame
│
├─ Patrimônios
│   ├─ Cadastrar Patrimônio
│   ├─ Listar Patrimônios
│   ├─ Importar Patrimônios
│   ├─ ─────────────────────
│   ├─ Gerenciar Itens Compostos  ← NOVO
│   └─ Detectar Itens Compostos   ← NOVO
│
├─ Relatórios
│   ├─ Relatório de Coletas
│   ├─ Relatório de Divergências
│   ├─ ─────────────────────
│   └─ Relatório de Itens Compostos  ← NOVO
│
└─ Configurações
    ├─ Usuários
    ├─ Perfis
    ├─ ─────────────────────
    └─ Padrões de Detecção  ← NOVO
```

---

## Implementation Notes

### Database Migration Script

```sql
-- Script de migração para adicionar funcionalidade de itens compostos
-- Executar em ordem

-- 1. Criar tabelas
\i sql/criar_tabelas_item_composto.sql

-- 2. Criar views
\i sql/criar_views_item_composto.sql

-- 3. Criar índices adicionais (se necessário)
CREATE INDEX IF NOT EXISTS idx_componente_tipo_upper 
ON TABELA_COMPONENTE(UPPER(TIPO));

-- 4. Inserir padrões de detecção padrão
INSERT INTO TABELA_PADRAO_DETECCAO (NOME, DESCRICAO_PADRAO, TIPO_PADRAO, PRIORIDADE)
VALUES 
('Mesa com Cadeiras', 'MESA.*COM.*CADEIRA', 'REGEX', 10),
('Estação de Trabalho', 'ESTACAO.*TRABALHO|WORKSTATION', 'REGEX', 9),
('Computador Completo', 'COMPUTADOR.*(MONITOR|TECLADO|MOUSE)', 'REGEX', 8);

-- 5. Inserir componentes padrão para cada padrão
-- (Exemplo para "Mesa com Cadeiras")
INSERT INTO TABELA_COMPONENTE_PADRAO (ID_PADRAO_DETECCAO, TIPO, DESCRICAO, QUANTIDADE_PADRAO, ORDEM)
SELECT 
    p.ID,
    'MESA',
    'Mesa escolar',
    1,
    1
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Mesa com Cadeiras';

INSERT INTO TABELA_COMPONENTE_PADRAO (ID_PADRAO_DETECCAO, TIPO, DESCRICAO, QUANTIDADE_PADRAO, ORDEM)
SELECT 
    p.ID,
    'CADEIRA',
    'Cadeira escolar',
    4,
    2
FROM TABELA_PADRAO_DETECCAO p
WHERE p.NOME = 'Mesa com Cadeiras';

-- 6. Commit
COMMIT;
```

### Performance Optimization

#### Índices Recomendados
```sql
-- Índices para queries de relatório
CREATE INDEX idx_componente_coleta_inventario_componente 
ON TABELA_COMPONENTE_COLETA(ID_INVENTARIO, ID_COMPONENTE);

-- Índice para busca por tipo (case-insensitive)
CREATE INDEX idx_componente_tipo_lower 
ON TABELA_COMPONENTE(LOWER(TIPO));

-- Índice composto para análise comparativa
CREATE INDEX idx_componente_coleta_comp_inv 
ON TABELA_COMPONENTE_COLETA(ID_COMPONENTE, ID_INVENTARIO, QUANTIDADE_ENCONTRADA);
```

#### Query Optimization

**Usar Prepared Statements** para todas as queries repetitivas:
```java
private static final String SQL_LISTAR_COMPONENTES = 
    "SELECT * FROM TABELA_COMPONENTE WHERE ID_ITEM_COMPOSTO = ?";

PreparedStatement pstmt = conn.prepareStatement(SQL_LISTAR_COMPONENTES);
```

**Usar Batch Inserts** para inserção de múltiplos componentes:
```java
PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_COMPONENTE);
for (Componente comp : componentes) {
    pstmt.setInt(1, comp.getIdItemComposto());
    pstmt.setString(2, comp.getTipo());
    // ... outros campos
    pstmt.addBatch();
}
pstmt.executeBatch();
```

**Usar Views Materializadas** para estatísticas complexas (se necessário):
```sql
CREATE MATERIALIZED VIEW mv_estatisticas_componentes AS
SELECT 
    c.TIPO,
    i.ID AS ID_INVENTARIO,
    SUM(c.QUANTIDADE_ESPERADA) AS TOTAL_ESPERADO,
    SUM(COALESCE(cc.QUANTIDADE_ENCONTRADA, 0)) AS TOTAL_ENCONTRADO
FROM TABELA_COMPONENTE c
CROSS JOIN TABELA_INVENTARIO i
LEFT JOIN TABELA_COMPONENTE_COLETA cc 
    ON c.ID = cc.ID_COMPONENTE AND i.ID = cc.ID_INVENTARIO
GROUP BY c.TIPO, i.ID;

-- Refresh periódico
REFRESH MATERIALIZED VIEW mv_estatisticas_componentes;
```

### Backward Compatibility

- Patrimônios sem registro em TABELA_ITEM_COMPOSTO são tratados como patrimônios simples
- Queries existentes não são afetadas (novas tabelas são independentes)
- App mobile continua funcionando normalmente (ignora componentes)
- Relatórios existentes não são modificados

### Security Considerations

- Apenas usuários com perfil ADMIN podem criar/editar itens compostos
- Apenas usuários com perfil ADMIN ou GESTOR podem acessar relatórios
- Validar permissões antes de executar operações de escrita
- Sanitizar inputs para prevenir SQL Injection (usar PreparedStatements)
- Validar tamanho de arquivos exportados (limite de 100MB)

---

## Deployment Plan

### Phase 1: Database Setup (1 dia)
1. Executar scripts de criação de tabelas
2. Criar views e índices
3. Inserir padrões de detecção padrão
4. Testar integridade referencial

### Phase 2: Backend Implementation (5 dias)
1. Implementar Models (ItemComposto, Componente, etc)
2. Implementar DAOs com testes unitários
3. Implementar Services com lógica de negócio
4. Implementar detecção automática
5. Implementar geração de relatórios

### Phase 3: UI Implementation (5 dias)
1. Criar ItemCompostoFrame
2. Criar RelatorioItemCompostoFrame
3. Integrar indicadores em PatrimonioFrame
4. Adicionar opções no menu principal
5. Implementar dialogs auxiliares

### Phase 4: Export Functionality (3 dias)
1. Implementar exportação Excel (Apache POI)
2. Implementar exportação PDF (iText)
3. Implementar exportação CSV
4. Adicionar geração de gráficos

### Phase 5: Testing & QA (3 dias)
1. Testes unitários (DAOs, Services)
2. Testes de integração (fluxos completos)
3. Testes de performance (grandes volumes)
4. Testes de usabilidade (coordenadora)

### Phase 6: Documentation & Training (2 dias)
1. Documentar funcionalidade
2. Criar manual do usuário
3. Treinar coordenadora e gestores
4. Preparar material de suporte

**Total Estimado: 19 dias úteis (~4 semanas)**

---

## Future Enhancements

### Short Term (3-6 meses)
- Notificações automáticas para conjuntos com alta taxa de ausência
- Dashboard com gráficos de integridade em tempo real
- Importação de componentes via planilha Excel
- Histórico de mudanças em componentes (audit trail)

### Medium Term (6-12 meses)
- Integração com app mobile (visualização de componentes durante coleta)
- Sugestões inteligentes baseadas em ML (padrões não óbvios)
- Relatórios agendados (envio automático por email)
- API REST para integração com outros sistemas

### Long Term (12+ meses)
- Gestão de manutenção preventiva baseada em componentes
- Previsão de necessidade de reposição (análise preditiva)
- Integração com sistema de compras (requisições automáticas)
- App mobile para registro de componentes faltantes em campo

---

**Documento de Design - Versão 1.0**  
**Data:** 27/11/2025  
**Autor:** Sistema de Inventário Patrimonial - Equipe de Desenvolvimento  
**Status:** Pronto para Revisão
