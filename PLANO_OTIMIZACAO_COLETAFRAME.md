# Plano de Otimização - ColetaFrame_v2

## ✅ IMPLEMENTADO (25/11/2025)

### Otimizações Aplicadas:

1. **Query Otimizada com JOIN** - `ColetaDAO.buscarHistoricoOtimizado()`
   - UMA única query com JOIN ao invés de N+1
   - Retorna dados prontos para exibição (ColetaResumo DTO)
   - Suporte SQLite e PostgreSQL

2. **Carregamento Assíncrono** - `SwingWorker`
   - Executa query em background thread
   - UI não trava durante carregamento
   - Feedback visual "Carregando..."

3. **Limite de Registros** - `LIMITE_HISTORICO = 100`
   - Carrega no máximo 100 registros por vez
   - Evita sobrecarga de memória e renderização

4. **Otimização de Renderização**
   - Desabilita auto-resize durante carga
   - SimpleDateFormat criado UMA vez (fora do loop)

### Arquivos Modificados:
- `src/main/java/com/inventario/dto/ColetaResumo.java` (NOVO)
- `src/main/java/com/inventario/dao/ColetaDAO.java` (método adicionado)
- `src/main/java/com/inventario/view/ColetaFrame_v2.java` (refatorado)

### Impacto:
| Métrica | Antes | Depois |
|---------|-------|--------|
| Queries | N+1 (101 para 100 coletas) | 1 |
| Travamento UI | Sim | Não |
| Tempo estimado | 5-10s | < 500ms |

---

## 🔴 Problemas Identificados (Histórico)

### 1. N+1 Queries no Histórico
```java
// PROBLEMA: Para cada coleta, faz uma query separada
for (Coleta coleta : coletas) {
    Patrimonio patrimonio = patrimonioDAO.buscarPorIdComJoins(coleta.getIdPatrimonio());
    // ...
}
```
**Impacto:** 100 coletas = 101 queries (1 para listar + 100 para patrimônios)

### 2. Operações de Banco na EDT
```java
private void carregarHistoricoColeta(String identificacaoSala) {
    // Executa na thread principal - TRAVA A UI
    List<Coleta> coletas = coletaDAO.buscarColetasPorNumeroSala(identificacaoSala);
}
```

### 3. Sem Limite de Registros
- Carrega TODAS as coletas da sala
- Tabela renderiza centenas de linhas

### 4. Formatação de Data Repetitiva
```java
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
// Criado dentro do loop - ineficiente
```

---

## ✅ Soluções Propostas

### Fase 1: Query Otimizada com JOIN (Prioridade ALTA)

**Criar novo método no ColetaDAO:**
```java
/**
 * Busca coletas com dados do patrimônio em uma única query
 * Evita N+1 queries
 */
public List<ColetaComPatrimonio> buscarColetasComPatrimonioPorSala(String numeroSala, int limite) {
    String sql = """
        SELECT 
            c.id_coleta,
            c.data_coleta,
            c.estado_encontrado,
            c.sem_etiqueta,
            c.descricao_item_sem_etiqueta,
            p.numero_patrimonio,
            p.descricao
        FROM coleta c
        LEFT JOIN patrimonio p ON c.id_patrimonio = p.id_patrimonio
        WHERE c.localizacao_encontrada = ?
        ORDER BY c.data_coleta DESC
        LIMIT ?
    """;
    // Retorna tudo em UMA query
}
```

### Fase 2: Carregamento Assíncrono (Prioridade ALTA)

**Usar SwingWorker para não travar UI:**
```java
private void carregarHistoricoColetaAsync(String identificacaoSala) {
    // Mostrar loading
    lblResumoSala.setText("Carregando histórico...");
    
    SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Object[]> doInBackground() throws Exception {
            // Executa em background thread
            return coletaDAO.buscarColetasComPatrimonioPorSala(identificacaoSala, 50);
        }
        
        @Override
        protected void done() {
            try {
                List<Object[]> dados = get();
                // Atualiza tabela na EDT
                atualizarTabelaHistorico(dados);
            } catch (Exception e) {
                mostrarErro(e.getMessage());
            }
        }
    };
    worker.execute();
}
```

### Fase 3: Paginação da Tabela (Prioridade MÉDIA)

**Limitar registros exibidos:**
```java
private static final int REGISTROS_POR_PAGINA = 50;
private int paginaAtual = 0;

// Adicionar botões "Anterior" e "Próximo"
// Carregar apenas 50 registros por vez
```

### Fase 4: Cache de Patrimônios (Prioridade MÉDIA)

**Cache em memória para evitar queries repetidas:**
```java
private Map<Integer, Patrimonio> cachePatrimonios = new HashMap<>();

private Patrimonio buscarPatrimonioComCache(int id) {
    return cachePatrimonios.computeIfAbsent(id, 
        patrimonioDAO::buscarPorIdComJoins);
}

// Limpar cache ao trocar de sala
private void limparCache() {
    cachePatrimonios.clear();
}
```

### Fase 5: Otimização de Renderização (Prioridade BAIXA)

**Desabilitar auto-resize durante carga:**
```java
private void atualizarTabelaHistorico(List<Object[]> dados) {
    tabelaHistorico.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    modeloTabelaHistorico.setRowCount(0);
    
    for (Object[] linha : dados) {
        modeloTabelaHistorico.addRow(linha);
    }
    
    tabelaHistorico.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
}
```

---

## 📊 Impacto Esperado

| Métrica | Antes | Depois |
|---------|-------|--------|
| Queries por sala | N+1 (101 para 100 coletas) | 1 |
| Tempo de carga | 5-10s | < 500ms |
| Travamento UI | Sim | Não |
| Memória | Alta (todos registros) | Controlada (50 por página) |

---

## 🔧 Ordem de Implementação

1. **[URGENTE]** Query otimizada com JOIN
2. **[URGENTE]** SwingWorker para async
3. **[IMPORTANTE]** Limite de 50 registros
4. **[MELHORIA]** Paginação completa
5. **[MELHORIA]** Cache de patrimônios

---

## 📝 Código de Implementação

### DTO para resultado otimizado:
```java
public class ColetaResumo {
    private long id;
    private Timestamp dataColeta;
    private String numeroPatrimonio;
    private String descricao;
    private String estadoEncontrado;
    private boolean semEtiqueta;
    private String descricaoSemEtiqueta;
    
    // Getters/Setters
}
```

### Método otimizado no DAO:
```java
public List<ColetaResumo> buscarHistoricoOtimizado(String numeroSala, int limite) {
    String sql = """
        SELECT 
            c.id_coleta,
            c.data_coleta,
            COALESCE(p.numero_patrimonio, 'SEM ETIQUETA') as numero,
            COALESCE(p.descricao, c.descricao_item_sem_etiqueta, '-') as descricao,
            COALESCE(c.estado_encontrado, '-') as estado,
            c.sem_etiqueta
        FROM coleta c
        LEFT JOIN patrimonio p ON c.id_patrimonio = p.id_patrimonio
        WHERE c.localizacao_encontrada LIKE ?
        ORDER BY c.data_coleta DESC
        LIMIT ?
    """;
    
    List<ColetaResumo> resultado = new ArrayList<>();
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, "%" + numeroSala + "%");
        stmt.setInt(2, limite);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            ColetaResumo resumo = new ColetaResumo();
            resumo.setId(rs.getLong("id_coleta"));
            resumo.setDataColeta(rs.getTimestamp("data_coleta"));
            resumo.setNumeroPatrimonio(rs.getString("numero"));
            resumo.setDescricao(rs.getString("descricao"));
            resumo.setEstadoEncontrado(rs.getString("estado"));
            resumo.setSemEtiqueta(rs.getBoolean("sem_etiqueta"));
            resultado.add(resumo);
        }
    }
    return resultado;
}
```

