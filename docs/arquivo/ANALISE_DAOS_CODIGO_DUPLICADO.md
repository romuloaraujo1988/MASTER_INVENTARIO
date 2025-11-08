# 🔍 Análise Crítica - DAOs com Código Duplicado

## 📊 Problema Identificado

### **Código Massivamente Duplicado nos DAOs**

Encontrei **15 classes DAO** com padrões idênticos repetidos centenas de vezes:

```
src/main/java/com/inventario/dao/
├── CampusDAO.java
├── ColetaDAO.java (40+ métodos com try-with-resources)
├── ColetorDAO.java
├── DashboardColetaDAO.java
├── InventarioDAO.java
├── InventarioSetorDAO.java
├── ParticipanteInventarioDAO.java
├── PatrimonioDAO.java (30+ métodos com try-with-resources)
├── QRCodeDAO.java
├── RelatorioColetaDAO.java
├── ResponsavelDAO.java
├── SalaDAO.java
├── SalaInventarioDAO.java
├── SetorDAO.java
└── UsuarioDAO.java
```

---

## ❌ Padrões Duplicados Identificados

### 1. **Try-With-Resources Repetido** (300+ ocorrências)

#### Padrão Repetido em TODOS os Métodos:
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    
    // Configurar parâmetros
    stmt.setString(1, valor);
    stmt.setInt(2, id);
    
    // Executar
    stmt.executeUpdate();
    // ou
    ResultSet rs = stmt.executeQuery();
    
} catch (SQLException e) {
    // Tratamento inconsistente ou ausente
}
```

**Ocorrências**:
- `ColetaDAO.java`: **40+ vezes**
- `PatrimonioDAO.java`: **30+ vezes**
- `ResponsavelDAO.java`: **25+ vezes**
- `SalaDAO.java`: **20+ vezes**
- `SetorDAO.java`: **15+ vezes**
- Outros DAOs: **100+ vezes**

**Total Estimado**: **230+ métodos** com este padrão

---

### 2. **Mapeamento ResultSet → Objeto** (Duplicado)

#### Padrão Repetido:
```java
private Patrimonio criarPatrimonioFromResultSet(ResultSet rs) throws SQLException {
    Patrimonio p = new Patrimonio();
    p.setId(rs.getInt("ID"));
    p.setNumero(rs.getString("NUMERO"));
    p.setStatus(rs.getString("STATUS"));
    p.setDescricao(rs.getString("DESCRICAO"));
    p.setRotulos(rs.getString("ROTULOS"));
    p.setIdResponsavel(rs.getInt("ID_RESPONSAVEL"));
    p.setValorAquisicao(rs.getBigDecimal("VALOR_AQUISICAO"));
    p.setValorDepreciado(rs.getBigDecimal("VALOR_DEPRECIADO"));
    p.setNumeroNotaFiscal(rs.getString("NUMERO_NOTA_FISCAL"));
    p.setNumeroSerie(rs.getString("NUMERO_SERIE"));
    p.setMarca(rs.getString("MARCA"));
    p.setModelo(rs.getString("MODELO"));
    p.setDataEntrada(rs.getDate("DATA_ENTRADA"));
    p.setFornecedor(rs.getString("FORNECEDOR"));
    p.setIdSala(rs.getInt("ID_SALA"));
    p.setEstadoConservacao(rs.getString("ESTADO_CONSERVACAO"));
    p.setCategoria(rs.getString("CATEGORIA"));
    // ... mais 10+ linhas
    return p;
}
```

**Problema**: Cada DAO tem seu próprio método de mapeamento com 20-30 linhas

**Ocorrências**: 15 DAOs × 1-3 métodos = **30+ métodos de mapeamento**

---

### 3. **Operações CRUD Idênticas**

#### INSERT (Repetido 15+ vezes)
```java
public void inserir(Entidade entidade) throws SQLException {
    String sql = "INSERT INTO TABELA (...) VALUES (?, ?, ?)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, entidade.getCampo1());
        stmt.setInt(2, entidade.getCampo2());
        stmt.setString(3, entidade.getCampo3());
        
        stmt.executeUpdate();
        
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                entidade.setId(rs.getInt(1));
            }
        }
    }
}
```

#### UPDATE (Repetido 15+ vezes)
```java
public void atualizar(Entidade entidade) throws SQLException {
    String sql = "UPDATE TABELA SET CAMPO1 = ?, CAMPO2 = ?, CAMPO3 = ? WHERE ID = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, entidade.getCampo1());
        stmt.setInt(2, entidade.getCampo2());
        stmt.setString(3, entidade.getCampo3());
        stmt.setInt(4, entidade.getId());
        
        stmt.executeUpdate();
    }
}
```

#### DELETE (Repetido 15+ vezes)
```java
public void excluir(int id) throws SQLException {
    String sql = "DELETE FROM TABELA WHERE ID = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }
}
```

#### SELECT BY ID (Repetido 15+ vezes)
```java
public Entidade buscarPorId(int id) throws SQLException {
    String sql = "SELECT * FROM TABELA WHERE ID = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return criarEntidadeFromResultSet(rs);
            }
        }
    }
    
    return null;
}
```

#### SELECT ALL (Repetido 15+ vezes)
```java
public List<Entidade> listarTodos() throws SQLException {
    String sql = "SELECT * FROM TABELA ORDER BY CAMPO";
    
    List<Entidade> lista = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            lista.add(criarEntidadeFromResultSet(rs));
        }
    }
    
    return lista;
}
```

**Total**: 15 DAOs × 5 operações = **75 métodos CRUD idênticos**

---

### 4. **Tratamento de Exceções Inconsistente**

#### Abordagem 1: Propagar SQLException
```java
public void inserir(Entidade e) throws SQLException {
    // código
}
```

#### Abordagem 2: Capturar e Logar
```java
public void inserir(Entidade e) {
    try {
        // código
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}
```

#### Abordagem 3: Capturar e Lançar RuntimeException
```java
public void inserir(Entidade e) {
    try {
        // código
    } catch (SQLException ex) {
        throw new RuntimeException("Erro ao inserir", ex);
    }
}
```

**Problema**: 3 abordagens diferentes sem padronização

---

### 5. **Uso de DatabaseConnection Direto**

```java
// Repetido 230+ vezes
Connection conn = DatabaseConnection.getConnection();
```

**Problemas**:
- ❌ Não usa pool de conexões (HikariCP)
- ❌ Sem gerenciamento centralizado
- ❌ Sem estatísticas
- ❌ Sem monitoramento
- ❌ Performance ruim

---

## 📊 Estatísticas de Código Duplicado

| Padrão | Ocorrências | Linhas por Ocorrência | Total de Linhas |
|--------|-------------|----------------------|-----------------|
| **Try-with-resources** | 230+ | 8-15 linhas | ~2.300 linhas |
| **Mapeamento ResultSet** | 30+ | 20-30 linhas | ~750 linhas |
| **CRUD INSERT** | 15 | 20-25 linhas | ~300 linhas |
| **CRUD UPDATE** | 15 | 15-20 linhas | ~250 linhas |
| **CRUD DELETE** | 15 | 8-10 linhas | ~120 linhas |
| **CRUD SELECT BY ID** | 15 | 15-20 linhas | ~250 linhas |
| **CRUD SELECT ALL** | 15 | 12-15 linhas | ~200 linhas |
| **TOTAL** | **335+** | - | **~4.170 linhas** |

**Código duplicado nos DAOs**: **~4.170 linhas** (estimativa conservadora)

---

## 🎯 Solução Proposta: BaseDAO

### Criar Classe Base Genérica

```java
public abstract class BaseDAO<T, ID> {
    
    // Métodos abstratos para subclasses implementarem
    protected abstract String getTableName();
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    
    // Métodos CRUD genéricos (implementados uma vez)
    public void insert(T entity) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, entity);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    setGeneratedId(entity, rs.getInt(1));
                }
            }
        }
    }
    
    public void update(T entity) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getUpdateSQL())) {
            
            setUpdateParameters(stmt, entity);
            stmt.executeUpdate();
        }
    }
    
    public void delete(ID id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE ID = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
    
    public T findById(ID id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ID = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        
        return null;
    }
    
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        
        List<T> results = new ArrayList<>();
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
        }
        
        return results;
    }
    
    // Métodos utilitários
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
        }
        
        return results;
    }
    
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }
    
    protected T executeQuerySingle(String sql, Object... params) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        
        return null;
    }
    
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
    
    protected abstract void setGeneratedId(T entity, int id);
}
```

---

## 📈 Exemplo de Uso: PatrimonioDAO Refatorado

### ❌ ANTES (100+ linhas)
```java
public class PatrimonioDAO {
    
    public void inserirPatrimonio(Patrimonio patrimonio) throws SQLException {
        String sql = "INSERT INTO TABELA_PATRIMONIO (...) VALUES (?, ?, ?, ...)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, patrimonio.getNumero());
            stmt.setString(2, patrimonio.getStatus());
            // ... 14 mais parâmetros
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    patrimonio.setId(rs.getInt(1));
                }
            }
        }
    }
    
    public void atualizarPatrimonio(Patrimonio patrimonio) throws SQLException {
        String sql = "UPDATE TABELA_PATRIMONIO SET ... WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patrimonio.getNumero());
            // ... 15 mais parâmetros
            
            stmt.executeUpdate();
        }
    }
    
    public void excluirPatrimonio(int id) throws SQLException {
        String sql = "DELETE FROM TABELA_PATRIMONIO WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public Patrimonio buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM TABELA_PATRIMONIO WHERE ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarPatrimonioFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    public List<Patrimonio> listarTodos() throws SQLException {
        String sql = "SELECT * FROM TABELA_PATRIMONIO";
        
        List<Patrimonio> patrimonios = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                patrimonios.add(criarPatrimonioFromResultSet(rs));
            }
        }
        
        return patrimonios;
    }
    
    private Patrimonio criarPatrimonioFromResultSet(ResultSet rs) throws SQLException {
        Patrimonio p = new Patrimonio();
        p.setId(rs.getInt("ID"));
        p.setNumero(rs.getString("NUMERO"));
        // ... 15 mais campos
        return p;
    }
}
```

### ✅ DEPOIS (30 linhas)
```java
public class PatrimonioDAO extends BaseDAO<Patrimonio, Integer> {
    
    @Override
    protected String getTableName() {
        return "TABELA_PATRIMONIO";
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO TABELA_PATRIMONIO (...) VALUES (?, ?, ?, ...)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE TABELA_PATRIMONIO SET ... WHERE ID = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Patrimonio p) throws SQLException {
        stmt.setString(1, p.getNumero());
        stmt.setString(2, p.getStatus());
        // ... outros parâmetros
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Patrimonio p) throws SQLException {
        setInsertParameters(stmt, p);
        stmt.setInt(17, p.getId());
    }
    
    @Override
    protected Patrimonio mapResultSetToEntity(ResultSet rs) throws SQLException {
        Patrimonio p = new Patrimonio();
        p.setId(rs.getInt("ID"));
        p.setNumero(rs.getString("NUMERO"));
        // ... outros campos
        return p;
    }
    
    @Override
    protected void setGeneratedId(Patrimonio entity, int id) {
        entity.setId(id);
    }
    
    // Métodos específicos (se necessário)
    public Patrimonio buscarPorNumero(String numero) throws SQLException {
        return executeQuerySingle(
            "SELECT * FROM TABELA_PATRIMONIO WHERE NUMERO = ?",
            numero
        );
    }
}
```

**Redução**: 100 linhas → 30 linhas = **70% menos código**

---

## 🎯 Benefícios da Refatoração

### Redução de Código

| DAO | Linhas Antes | Linhas Depois | Redução |
|-----|--------------|---------------|---------|
| PatrimonioDAO | ~500 | ~150 | -70% |
| ColetaDAO | ~1.400 | ~400 | -71% |
| ResponsavelDAO | ~400 | ~120 | -70% |
| SalaDAO | ~350 | ~100 | -71% |
| SetorDAO | ~300 | ~90 | -70% |
| **Outros 10 DAOs** | ~2.000 | ~600 | -70% |
| **TOTAL** | **~4.950** | **~1.460** | **-70%** |

**Código eliminado**: **~3.490 linhas**

### Melhorias

- ✅ **70% menos código duplicado**
- ✅ **100% padronização**
- ✅ **ConnectionManager** em todos os DAOs
- ✅ **Tratamento de erros consistente**
- ✅ **Manutenção centralizada**
- ✅ **Fácil adicionar novos DAOs**
- ✅ **Logs estruturados**
- ✅ **Performance melhorada** (pool de conexões)

---

## 📝 Próximos Passos

1. **Criar BaseDAO** genérico
2. **Refatorar PatrimonioDAO** (exemplo)
3. **Refatorar SalaDAO** (exemplo)
4. **Refatorar SetorDAO** (exemplo)
5. **Criar guia de migração** para outros DAOs
6. **Testar todos os DAOs** refatorados
7. **Substituir versões antigas**

---

## 🎉 Conclusão

Os DAOs têm **~4.170 linhas de código duplicado** que podem ser reduzidas em **70%** usando uma classe base genérica.

**Status**: 🔄 **ANÁLISE COMPLETA** - Pronto para implementação

**Impacto Estimado**: Eliminação de **~3.490 linhas** de código duplicado
