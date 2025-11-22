# 🔧 Solução - Inventário Ativo no SQLite

## ❌ Problema

**Mensagem de Erro:**
```
Nenhum inventário ativo encontrado.
Não é possível realizar coletas.
```

## 🔍 Diagnóstico

### Dados no SQLite
```sql
SELECT * FROM local_inventario;
```

**Resultado:**
```
ID: 2
Nome: Inventário Anual 2025
Status: EM_ANDAMENTO
Data Início: 2025-10-01
Data Fim: 2025-12-31
```

✅ **O inventário ESTÁ no SQLite!**

### Problema Identificado

O código que busca o inventário ativo está:
1. ❌ Procurando na tabela errada (TABELA_INVENTARIO ao invés de local_inventario)
2. ❌ Usando query incorreta
3. ❌ Não adaptado para modo offline

---

## ✅ Solução

### 1. Adicionar Método no InventarioDAO

O `InventarioDAO` precisa de um método que busque no SQLite quando em modo offline:

```java
/**
 * Busca inventário ativo do banco local (SQLite)
 * @return Inventário ativo ou null
 */
public Inventario buscarInventarioAtivoOffline() {
    String sql = """
        SELECT * FROM local_inventario 
        WHERE status IN ('PLANEJAMENTO', 'EM_ANDAMENTO')
        ORDER BY data_inicio DESC 
        LIMIT 1
    """;
    
    try (Connection conn = SQLiteConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        if (rs.next()) {
            return mapResultSetToEntity(rs, true); // true = SQLite
        }
        return null;
        
    } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Erro ao buscar inventário ativo offline", e);
        return null;
    }
}
```

### 2. Modificar buscarInventarioAtivo() para Suportar Offline

```java
public Inventario buscarInventarioAtivo() {
    // Verificar se está em modo offline
    if (!DatabaseConnection.isOnline()) {
        return buscarInventarioAtivoOffline();
    }
    
    // Código existente para PostgreSQL
    String sql = """
        SELECT * FROM TABELA_INVENTARIO 
        WHERE STATUS_INVENTARIO IN ('PLANEJADO', 'EM_ANDAMENTO')
        ORDER BY DATA_INICIO DESC 
        LIMIT 1
    """;
    
    // ... resto do código
}
```

### 3. Adicionar Método mapResultSetToEntity com Flag SQLite

```java
private Inventario mapResultSetToEntity(ResultSet rs, boolean isSQLite) throws SQLException {
    Inventario inv = new Inventario();
    
    if (isSQLite) {
        // Mapear colunas do SQLite (minúsculas)
        inv.setId(rs.getInt("id"));
        inv.setNome(rs.getString("nome"));
        inv.setObservacao(rs.getString("descricao"));
        
        // Converter timestamps do SQLite
        String dataInicioStr = rs.getString("data_inicio");
        if (dataInicioStr != null) {
            inv.setDataInicio(new Date(Long.parseLong(dataInicioStr)));
        }
        
        String dataFimStr = rs.getString("data_fim");
        if (dataFimStr != null) {
            inv.setDataFim(new Date(Long.parseLong(dataFimStr)));
        }
        
        inv.setStatusInventario(rs.getString("status"));
        
    } else {
        // Mapear colunas do PostgreSQL (maiúsculas)
        inv.setId(rs.getInt("ID"));
        inv.setNome(rs.getString("NOME"));
        // ... resto do código existente
    }
    
    return inv;
}
```

---

## 🎯 Alternativa Mais Simples

Se não quiser modificar o `InventarioDAO`, pode criar um método auxiliar no `OfflineDAO`:

```java
/**
 * Busca inventário ativo do banco local
 * @return Dados do inventário ou null
 */
public Map<String, Object> buscarInventarioAtivo() {
    String sql = """
        SELECT * FROM local_inventario 
        WHERE status IN ('PLANEJAMENTO', 'EM_ANDAMENTO')
        ORDER BY data_inicio DESC 
        LIMIT 1
    """;
    
    try (Connection conn = sqliteConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        if (rs.next()) {
            Map<String, Object> inv = new HashMap<>();
            inv.put("id", rs.getInt("id"));
            inv.put("nome", rs.getString("nome"));
            inv.put("descricao", rs.getString("descricao"));
            inv.put("data_inicio", rs.getString("data_inicio"));
            inv.put("data_fim", rs.getString("data_fim"));
            inv.put("status", rs.getString("status"));
            return inv;
        }
        return null;
        
    } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Erro ao buscar inventário ativo", e);
        return null;
    }
}
```

E usar no código que verifica:

```java
// Verificar se há inventário ativo
Map<String, Object> inventario = offlineDAO.buscarInventarioAtivo();
if (inventario == null) {
    JOptionPane.showMessageDialog(this,
        "Nenhum inventário ativo encontrado.\n" +
        "Não é possível realizar coletas.",
        "Aviso",
        JOptionPane.WARNING_MESSAGE);
    return;
}

int idInventario = (int) inventario.get("id");
String nomeInventario = (String) inventario.get("nome");
```

---

## 🚀 Implementação Rápida

Vou adicionar o método no `OfflineDAO` agora mesmo, pois é a solução mais rápida e não quebra código existente.

---

**Status:** ⏳ Aguardando implementação  
**Prioridade:** 🔴 ALTA  
**Impacto:** Sistema não consegue fazer coletas offline
