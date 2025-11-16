# Inconsistências - PatrimonioDAO.java

## 📋 Análise Completa

**Arquivo:** `src/main/java/com/inventario/dao/PatrimonioDAO.java`  
**Data:** 15/11/2025  
**Total de Erros:** 3 erros de compilação

---

## ❌ Problemas Identificados

### 1. **Método `getConnection()` Não Existe**

**Linhas com Erro:** 504, 540, 575

#### Problema: Uso de `getConnection()` que não existe na classe

**Código Atual (ERRADO):**
```java
// Linha 504 - buscarPorCodigoParcial()
try (Connection conn = getConnection();  // ERRO!
     PreparedStatement stmt = conn.prepareStatement(sql)) {

// Linha 540 - buscarPorDescricao()
try (Connection conn = getConnection();  // ERRO!
     PreparedStatement stmt = conn.prepareStatement(sql)) {

// Linha 575 - buscarDetalhesCompletos()
try (Connection conn = getConnection();  // ERRO!
     PreparedStatement stmt = conn.prepareStatement(sql)) {
```

---

### 2. **Análise da Causa Raiz**

#### PatrimonioDAO estende BaseDAO
```java
public class PatrimonioDAO extends BaseDAO<Patrimonio, Integer>
```

#### BaseDAO usa ConnectionManager
```java
// No BaseDAO
Connection conn = null;
try {
    conn = ConnectionManager.getConnection();
    // ...
} finally {
    ConnectionManager.closeConnection(conn);
}
```

#### Conclusão
- ❌ `getConnection()` **NÃO** é um método do BaseDAO
- ❌ `getConnection()` **NÃO** é um método do PatrimonioDAO
- ✅ Deve usar `ConnectionManager.getConnection()`

---

### 3. **Inconsistência no Próprio Arquivo**

O arquivo PatrimonioDAO tem **dois padrões diferentes**:

#### Padrão 1: Usa ConnectionManager (CORRETO) ✅
```java
// Linha 448 - buscarPatrimoniosColetados()
Connection conn = null;
try {
    conn = com.inventario.util.ConnectionManager.getConnection();
    // ...
} finally {
    com.inventario.util.ConnectionManager.closeConnection(conn);
}

// Linha 648 - buscarAvancada()
Connection conn = null;
try {
    conn = com.inventario.util.ConnectionManager.getConnection();
    // ...
} finally {
    com.inventario.util.ConnectionManager.closeConnection(conn);
}
```

#### Padrão 2: Usa getConnection() (ERRADO) ❌
```java
// Linha 504 - buscarPorCodigoParcial()
try (Connection conn = getConnection();  // ERRO!

// Linha 540 - buscarPorDescricao()
try (Connection conn = getConnection();  // ERRO!

// Linha 575 - buscarDetalhesCompletos()
try (Connection conn = getConnection();  // ERRO!
```

---

## 🔧 Correções Necessárias

### Correção 1: buscarPorCodigoParcial() (Linha 504)

**Antes:**
```java
List<Patrimonio> patrimonios = new ArrayList<>();

try (Connection conn = getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    
    stmt.setString(1, "%" + codigoParcial + "%");
    stmt.setInt(2, limit);
    
    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            patrimonios.add(mapResultSetToEntity(rs));
        }
    }
}

return patrimonios;
```

**Depois:**
```java
List<Patrimonio> patrimonios = new ArrayList<>();
Connection conn = null;

try {
    conn = com.inventario.util.ConnectionManager.getConnection();
    PreparedStatement stmt = conn.prepareStatement(sql);
    
    stmt.setString(1, "%" + codigoParcial + "%");
    stmt.setInt(2, limit);
    
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
        patrimonios.add(mapResultSetToEntity(rs));
    }
    
    rs.close();
    stmt.close();
    
} catch (SQLException e) {
    throw e;
} finally {
    com.inventario.util.ConnectionManager.closeConnection(conn);
}

return patrimonios;
```

---

### Correção 2: buscarPorDescricao() (Linha 540)

**Antes:**
```java
List<Patrimonio> patrimonios = new ArrayList<>();

try (Connection conn = getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    
    stmt.setString(1, "%" + descricao + "%");
    stmt.setInt(2, limit);
    
    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            patrimonios.add(mapResultSetToEntity(rs));
        }
    }
}

return patrimonios;
```

**Depois:**
```java
List<Patrimonio> patrimonios = new ArrayList<>();
Connection conn = null;

try {
    conn = com.inventario.util.ConnectionManager.getConnection();
    PreparedStatement stmt = conn.prepareStatement(sql);
    
    stmt.setString(1, "%" + descricao + "%");
    stmt.setInt(2, limit);
    
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
        patrimonios.add(mapResultSetToEntity(rs));
    }
    
    rs.close();
    stmt.close();
    
} catch (SQLException e) {
    throw e;
} finally {
    com.inventario.util.ConnectionManager.closeConnection(conn);
}

return patrimonios;
```

---

### Correção 3: buscarDetalhesCompletos() (Linha 575)

**Antes:**
```java
try (Connection conn = getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    
    stmt.setLong(1, patrimonioId);
    
    try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
            return mapResultSetToEntity(rs);
        }
    }
}

return null;
```

**Depois:**
```java
Connection conn = null;

try {
    conn = com.inventario.util.ConnectionManager.getConnection();
    PreparedStatement stmt = conn.prepareStatement(sql);
    
    stmt.setLong(1, patrimonioId);
    
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        Patrimonio patrimonio = mapResultSetToEntity(rs);
        rs.close();
        stmt.close();
        return patrimonio;
    }
    
    rs.close();
    stmt.close();
    
} catch (SQLException e) {
    throw e;
} finally {
    com.inventario.util.ConnectionManager.closeConnection(conn);
}

return null;
```

---

## 📊 Resumo dos Erros

| Método | Linha | Erro | Severidade |
|--------|-------|------|------------|
| buscarPorCodigoParcial() | 504 | getConnection() não existe | 🔴 Alta |
| buscarPorDescricao() | 540 | getConnection() não existe | 🔴 Alta |
| buscarDetalhesCompletos() | 575 | getConnection() não existe | 🔴 Alta |
| **TOTAL** | - | **3 erros** | - |

---

## 🎯 Padrão Correto a Seguir

### Template para Métodos com Conexão

```java
public List<Patrimonio> meuMetodo(String parametro) throws SQLException {
    List<Patrimonio> patrimonios = new ArrayList<>();
    Connection conn = null;
    
    try {
        // 1. Obter conexão
        conn = com.inventario.util.ConnectionManager.getConnection();
        
        // 2. Preparar statement
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, parametro);
        
        // 3. Executar query
        ResultSet rs = stmt.executeQuery();
        
        // 4. Processar resultados
        while (rs.next()) {
            patrimonios.add(mapResultSetToEntity(rs));
        }
        
        // 5. Fechar recursos
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        throw e;
    } finally {
        // 6. SEMPRE fechar conexão
        com.inventario.util.ConnectionManager.closeConnection(conn);
    }
    
    return patrimonios;
}
```

---

## ✅ Checklist de Correção

- [ ] Substituir `getConnection()` por `ConnectionManager.getConnection()` (linha 504)
- [ ] Substituir `getConnection()` por `ConnectionManager.getConnection()` (linha 540)
- [ ] Substituir `getConnection()` por `ConnectionManager.getConnection()` (linha 575)
- [ ] Adicionar `finally` com `ConnectionManager.closeConnection(conn)`
- [ ] Fechar ResultSet e PreparedStatement manualmente
- [ ] Testar compilação
- [ ] Testar métodos em runtime

---

## 🔍 Por Que Isso Aconteceu?

### Hipótese 1: Copy-Paste de Código Antigo
- Código pode ter sido copiado de uma versão antiga
- Versão antiga tinha método `getConnection()` na classe
- Refatoração para BaseDAO removeu o método

### Hipótese 2: Mistura de Padrões
- Alguns métodos usam ConnectionManager (correto)
- Outros métodos usam getConnection() (incorreto)
- Falta de padronização no código

### Hipótese 3: Geração Automática
- Código pode ter sido gerado por ferramenta
- Ferramenta assumiu existência de getConnection()
- Não foi validado após geração

---

## 📈 Impacto dos Erros

### Antes da Correção
- ❌ **3 métodos** não compilam
- ❌ Endpoints de consulta **não funcionam**
- ❌ App mobile **não consegue** buscar patrimônios
- ❌ Funcionalidade de consulta **quebrada**

### Depois da Correção
- ✅ **0 erros** de compilação
- ✅ Endpoints de consulta **funcionais**
- ✅ App mobile **consegue** buscar patrimônios
- ✅ Funcionalidade de consulta **operacional**

---

## 🎯 Recomendações

### Curto Prazo
1. ✅ Corrigir os 3 métodos com erro
2. ✅ Testar compilação
3. ✅ Testar endpoints

### Médio Prazo
1. Padronizar todos os métodos para usar ConnectionManager
2. Criar método helper `executeQueryWithConnection()` no BaseDAO
3. Refatorar métodos duplicados

### Longo Prazo
1. Migrar para Spring Data JPA (elimina DAOs manuais)
2. Usar @Repository e @Transactional
3. Eliminar gerenciamento manual de conexões

---

**Conclusão:** Os 3 erros são **idênticos** e causados pelo uso incorreto de `getConnection()`. A correção é simples: substituir por `ConnectionManager.getConnection()` e adicionar gerenciamento adequado de recursos.

**Tempo estimado de correção:** 10 minutos

