# 📄 Plano de Implementação - Paginação Desktop

## 🎯 Objetivo

Implementar paginação nas listagens do desktop para melhorar performance e experiência do usuário, especialmente em tabelas com muitos registros.

---

## 📊 Análise Atual

### Problema Identificado
```java
// ANTES - Carrega TODOS os registros de uma vez
List<Patrimonio> patrimonios = patrimonioService.listarTodos();
// Problema: 10.000+ registros = ~5-10 segundos de carregamento
```

### Impacto
- ⚠️ Lentidão em tabelas grandes (>1000 registros)
- ⚠️ Alto consumo de memória
- ⚠️ Interface trava durante carregamento
- ⚠️ Experiência ruim do usuário

### Frames Afetados (Prioridade)
1. 🔴 **PatrimonioFrame** - Crítico (pode ter 10.000+ registros)
2. 🔴 **ColetaFrame_v2** - Crítico (histórico grande)
3. 🟡 **UsuarioFrame** - Médio (centenas de registros)
4. 🟡 **ResponsavelFrame** - Médio (centenas de registros)
5. 🟡 **SalaFrame** - Médio (centenas de registros)
6. 🟢 **SetorFrame** - Baixo (dezenas de registros)
7. 🟢 **CampusFrame** - Baixo (poucos registros)
8. 🟢 **InventarioFrame** - Baixo (poucos registros)

---

## 🏗️ Arquitetura da Solução

### 1. Classe de Paginação (Reutilizável)

```java
package com.inventario.util;

import java.util.List;

/**
 * Classe para representar uma página de resultados
 * 
 * @param <T> Tipo dos elementos da página
 */
public class Page<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    
    public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }
    
    // Getters
    public List<T> getContent() { return content; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    
    // Métodos úteis
    public boolean hasNext() { return pageNumber < totalPages - 1; }
    public boolean hasPrevious() { return pageNumber > 0; }
    public boolean isFirst() { return pageNumber == 0; }
    public boolean isLast() { return pageNumber == totalPages - 1; }
}
```

### 2. Componente de Navegação (Swing)

```java
package com.inventario.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Componente de navegação de paginação
 * Exibe: [Primeira] [Anterior] [Página X de Y] [Próxima] [Última]
 */
public class PaginationPanel extends JPanel {
    private JButton btnFirst;
    private JButton btnPrevious;
    private JLabel lblPageInfo;
    private JButton btnNext;
    private JButton btnLast;
    private JComboBox<Integer> cboPageSize;
    
    private int currentPage = 0;
    private int totalPages = 0;
    private int pageSize = 50;
    
    public PaginationPanel() {
        initComponents();
        setupLayout();
    }
    
    private void initComponents() {
        btnFirst = new JButton("⏮ Primeira");
        btnPrevious = new JButton("◀ Anterior");
        lblPageInfo = new JLabel("Página 1 de 1");
        btnNext = new JButton("Próxima ▶");
        btnLast = new JButton("Última ⏭");
        
        // ComboBox para tamanho da página
        cboPageSize = new JComboBox<>(new Integer[]{10, 25, 50, 100, 200});
        cboPageSize.setSelectedItem(50);
        
        // Estilizar botões
        styleButton(btnFirst);
        styleButton(btnPrevious);
        styleButton(btnNext);
        styleButton(btnLast);
        
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setBackground(new Color(248, 249, 250));
        
        add(new JLabel("Itens por página:"));
        add(cboPageSize);
        add(Box.createHorizontalStrut(20));
        add(btnFirst);
        add(btnPrevious);
        add(lblPageInfo);
        add(btnNext);
        add(btnLast);
    }
    
    public void updatePageInfo(int currentPage, int totalPages, long totalElements) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        
        lblPageInfo.setText(String.format("Página %d de %d (%d itens)", 
            currentPage + 1, totalPages, totalElements));
        
        btnFirst.setEnabled(currentPage > 0);
        btnPrevious.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
        btnLast.setEnabled(currentPage < totalPages - 1);
    }
    
    // Métodos para adicionar listeners
    public void addFirstPageListener(ActionListener listener) {
        btnFirst.addActionListener(listener);
    }
    
    public void addPreviousPageListener(ActionListener listener) {
        btnPrevious.addActionListener(listener);
    }
    
    public void addNextPageListener(ActionListener listener) {
        btnNext.addActionListener(listener);
    }
    
    public void addLastPageListener(ActionListener listener) {
        btnLast.addActionListener(listener);
    }
    
    public void addPageSizeChangeListener(ActionListener listener) {
        cboPageSize.addActionListener(listener);
    }
    
    public int getPageSize() {
        return (Integer) cboPageSize.getSelectedItem();
    }
}
```

### 3. Atualização dos DAOs

```java
// PatrimonioDAORefactored.java

/**
 * Lista patrimônios com paginação
 * 
 * @param page Número da página (0-based)
 * @param size Tamanho da página
 * @return Página de patrimônios
 */
public Page<Patrimonio> findAllPaginated(int page, int size) throws SQLException {
    // 1. Contar total de registros
    String countSql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO";
    long totalElements = executeScalar(countSql, Long.class);
    
    // 2. Buscar registros da página
    int offset = page * size;
    String sql = "SELECT p.*, " +
                 "r.NOME as nome_responsavel, " +
                 "s.DESCRICAO as nome_sala " +
                 "FROM TABELA_PATRIMONIO p " +
                 "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                 "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID " +
                 "ORDER BY p.ID DESC " +
                 "LIMIT ? OFFSET ?";
    
    List<Patrimonio> content = executeQuery(sql, size, offset);
    
    return new Page<>(content, page, size, totalElements);
}

/**
 * Busca patrimônios por filtro com paginação
 */
public Page<Patrimonio> buscarPorFiltroPaginado(String filtro, int page, int size) 
        throws SQLException {
    // Implementação similar com WHERE clause
    String whereClause = "WHERE (UPPER(p.NUMERO) LIKE UPPER(?) OR " +
                        "UPPER(p.DESCRICAO) LIKE UPPER(?))";
    String filtroLike = "%" + filtro + "%";
    
    // Count
    String countSql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO p " + whereClause;
    long totalElements = executeScalar(countSql, Long.class, filtroLike, filtroLike);
    
    // Query
    int offset = page * size;
    String sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
                 "FROM TABELA_PATRIMONIO p " +
                 "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
                 "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID " +
                 whereClause + " " +
                 "ORDER BY p.ID DESC LIMIT ? OFFSET ?";
    
    List<Patrimonio> content = executeQuery(sql, filtroLike, filtroLike, size, offset);
    
    return new Page<>(content, page, size, totalElements);
}
```

### 4. Atualização dos Services

```java
// PatrimonioService.java

/**
 * Lista patrimônios com paginação
 */
public Page<Patrimonio> listarPaginado(int page, int size) {
    try {
        return patrimonioDAO.findAllPaginated(page, size);
    } catch (SQLException e) {
        logger.error("Erro ao listar patrimônios paginados", e);
        return new Page<>(new ArrayList<>(), page, size, 0);
    }
}

/**
 * Busca patrimônios por filtro com paginação
 */
public Page<Patrimonio> buscarPorFiltroPaginado(String filtro, int page, int size) {
    try {
        return patrimonioDAO.buscarPorFiltroPaginado(filtro, page, size);
    } catch (SQLException e) {
        logger.error("Erro ao buscar patrimônios paginados", e);
        return new Page<>(new ArrayList<>(), page, size, 0);
    }
}
```

### 5. Atualização do PatrimonioFrame

```java
public class PatrimonioFrame extends JFrame {
    // ... campos existentes ...
    
    private PaginationPanel paginationPanel;
    private int currentPage = 0;
    private int pageSize = 50;
    private String currentFilter = "";
    
    private void initComponents() {
        // ... código existente ...
        
        // Adicionar painel de paginação
        paginationPanel = new PaginationPanel();
        setupPaginationListeners();
    }
    
    private void setupPaginationListeners() {
        paginationPanel.addFirstPageListener(e -> goToPage(0));
        paginationPanel.addPreviousPageListener(e -> goToPage(currentPage - 1));
        paginationPanel.addNextPageListener(e -> goToPage(currentPage + 1));
        paginationPanel.addLastPageListener(e -> {
            Page<Patrimonio> page = carregarPatrimoniosPaginados(0, pageSize);
            goToPage(page.getTotalPages() - 1);
        });
        paginationPanel.addPageSizeChangeListener(e -> {
            pageSize = paginationPanel.getPageSize();
            goToPage(0); // Voltar para primeira página ao mudar tamanho
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // ... painéis existentes ...
        
        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.SOUTH); // NOVO
    }
    
    private void carregarPatrimonios() {
        currentFilter = "";
        goToPage(0);
    }
    
    private void buscarPatrimonios() {
        currentFilter = campoBusca.getText().trim();
        goToPage(0);
    }
    
    private void goToPage(int page) {
        currentPage = page;
        Page<Patrimonio> patrimonioPage = carregarPatrimoniosPaginados(page, pageSize);
        atualizarTabelaPatrimonios(patrimonioPage.getContent());
        paginationPanel.updatePageInfo(
            patrimonioPage.getPageNumber(),
            patrimonioPage.getTotalPages(),
            patrimonioPage.getTotalElements()
        );
    }
    
    private Page<Patrimonio> carregarPatrimoniosPaginados(int page, int size) {
        try {
            if (currentFilter.isEmpty()) {
                return patrimonioService.listarPaginado(page, size);
            } else {
                return patrimonioService.buscarPorFiltroPaginado(currentFilter, page, size);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar patrimônios: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return new Page<>(new ArrayList<>(), page, size, 0);
        }
    }
}
```

---

## 📋 Plano de Implementação

### Fase 1: Infraestrutura (1 dia)
**Objetivo**: Criar componentes reutilizáveis

- [ ] Criar classe `Page<T>` em `com.inventario.util`
- [ ] Criar `PaginationPanel` em `com.inventario.view.components`
- [ ] Testar componentes isoladamente
- [ ] Documentar uso dos componentes

**Arquivos**:
- `src/main/java/com/inventario/util/Page.java`
- `src/main/java/com/inventario/view/components/PaginationPanel.java`

### Fase 2: PatrimonioFrame (1 dia) - CRÍTICO
**Objetivo**: Implementar paginação no frame mais crítico

- [ ] Adicionar métodos paginados no `PatrimonioDAORefactored`
  - `findAllPaginated(page, size)`
  - `buscarPorFiltroPaginado(filtro, page, size)`
- [ ] Adicionar métodos no `PatrimonioService`
  - `listarPaginado(page, size)`
  - `buscarPorFiltroPaginado(filtro, page, size)`
- [ ] Refatorar `PatrimonioFrame`
  - Adicionar `PaginationPanel`
  - Implementar navegação de páginas
  - Atualizar métodos de carregamento
- [ ] Testar com dados reais (>1000 registros)

**Arquivos**:
- `src/main/java/com/inventario/dao/PatrimonioDAORefactored.java`
- `src/main/java/com/inventario/service/PatrimonioService.java`
- `src/main/java/com/inventario/view/PatrimonioFrame.java`

### Fase 3: ColetaFrame_v2 (1 dia) - CRÍTICO
**Objetivo**: Implementar paginação no histórico de coletas

- [ ] Adicionar métodos paginados no `ColetaDAO`
- [ ] Adicionar métodos no `ColetaService`
- [ ] Refatorar `ColetaFrame_v2`
- [ ] Testar navegação e filtros

**Arquivos**:
- `src/main/java/com/inventario/dao/ColetaDAO.java`
- `src/main/java/com/inventario/service/ColetaService.java`
- `src/main/java/com/inventario/view/ColetaFrame_v2.java`

### Fase 4: Frames Médios (1 dia)
**Objetivo**: Implementar paginação em frames com centenas de registros

- [ ] **UsuarioFrame**
  - Adicionar paginação no `UsuarioDAORefactored`
  - Atualizar `UsuarioService`
  - Refatorar frame
  
- [ ] **ResponsavelFrame**
  - Adicionar paginação no `ResponsavelDAORefactored`
  - Atualizar `ResponsavelService`
  - Refatorar frame
  
- [ ] **SalaFrame**
  - Adicionar paginação no `SalaDAORefactored`
  - Atualizar `SalaService`
  - Refatorar frame

**Arquivos**: 6 DAOs + 6 Services + 3 Frames

### Fase 5: Otimizações (0.5 dia)
**Objetivo**: Melhorar performance e UX

- [ ] Adicionar cache de páginas visitadas
- [ ] Implementar pré-carregamento da próxima página
- [ ] Adicionar indicador de carregamento
- [ ] Otimizar queries SQL (índices)
- [ ] Testar performance com dados grandes

### Fase 6: Documentação (0.5 dia)
**Objetivo**: Documentar implementação

- [ ] Atualizar documentação técnica
- [ ] Criar guia de uso da paginação
- [ ] Documentar padrões de implementação
- [ ] Atualizar ANALISE_CRITICA_DESKTOP.md

---

## 📊 Estimativa de Esforço

| Fase | Descrição | Tempo | Prioridade |
|------|-----------|-------|------------|
| 1 | Infraestrutura | 1 dia | 🔴 Alta |
| 2 | PatrimonioFrame | 1 dia | 🔴 Crítica |
| 3 | ColetaFrame_v2 | 1 dia | 🔴 Crítica |
| 4 | Frames Médios | 1 dia | 🟡 Média |
| 5 | Otimizações | 0.5 dia | 🟢 Baixa |
| 6 | Documentação | 0.5 dia | 🟡 Média |
| **Total** | | **5 dias** | |

---

## 🎯 Benefícios Esperados

### Performance
- ⚡ **Carregamento 10x mais rápido** (10s → 1s)
- 💾 **90% menos memória** (10.000 registros → 50 registros)
- 🚀 **Interface responsiva** (sem travamentos)

### Experiência do Usuário
- ✅ Navegação intuitiva entre páginas
- ✅ Controle do tamanho da página
- ✅ Informação clara (Página X de Y)
- ✅ Feedback visual de carregamento

### Escalabilidade
- ✅ Suporta 100.000+ registros sem problemas
- ✅ Performance consistente independente do volume
- ✅ Preparado para crescimento futuro

---

## 🧪 Testes Necessários

### Testes Funcionais
- [ ] Navegação entre páginas (primeira, anterior, próxima, última)
- [ ] Mudança de tamanho de página (10, 25, 50, 100, 200)
- [ ] Filtros com paginação
- [ ] Ordenação com paginação
- [ ] Edição/exclusão de registros paginados

### Testes de Performance
- [ ] Carregamento com 100 registros
- [ ] Carregamento com 1.000 registros
- [ ] Carregamento com 10.000 registros
- [ ] Carregamento com 100.000 registros
- [ ] Tempo de resposta < 1 segundo

### Testes de Usabilidade
- [ ] Botões habilitados/desabilitados corretamente
- [ ] Informação de página clara e precisa
- [ ] Transição suave entre páginas
- [ ] Feedback visual adequado

---

## 📝 Notas de Implementação

### Boas Práticas
1. **Sempre usar LIMIT/OFFSET** nas queries SQL
2. **Contar total antes de buscar** (2 queries: COUNT + SELECT)
3. **Validar parâmetros** (page >= 0, size > 0)
4. **Tratar páginas vazias** (retornar página vazia, não erro)
5. **Manter estado da paginação** (página atual, filtros)

### Otimizações SQL
```sql
-- Adicionar índices para melhorar performance
CREATE INDEX idx_patrimonio_id_desc ON TABELA_PATRIMONIO(ID DESC);
CREATE INDEX idx_patrimonio_numero ON TABELA_PATRIMONIO(NUMERO);
CREATE INDEX idx_patrimonio_descricao ON TABELA_PATRIMONIO(DESCRICAO);
```

### Padrão de Nomenclatura
- Métodos DAO: `findAllPaginated()`, `buscarPorFiltroPaginado()`
- Métodos Service: `listarPaginado()`, `buscarPorFiltroPaginado()`
- Classe de resultado: `Page<T>`
- Componente UI: `PaginationPanel`

---

## 🚀 Próximos Passos

1. ✅ Revisar e aprovar este plano
2. ⏳ Criar branch `feature/paginacao-desktop`
3. ⏳ Implementar Fase 1 (Infraestrutura)
4. ⏳ Implementar Fase 2 (PatrimonioFrame)
5. ⏳ Testar e validar
6. ⏳ Continuar com fases seguintes

---

**Criado em**: Novembro 2025  
**Autor**: Sistema de Inventário  
**Status**: 📋 Planejamento Completo - Aguardando Aprovação  
**Prioridade**: 🔴 Alta
