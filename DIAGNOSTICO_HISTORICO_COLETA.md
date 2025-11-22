# Diagnóstico - Histórico de Coleta Não Carrega

## 🔍 Problema

A tabela de histórico de coletas não está sendo preenchida quando uma sala é selecionada.

## ✅ Verificações Realizadas

### 1. Banco de Dados SQLite

**Coletas existentes:**
```sql
SELECT COUNT(*) FROM local_coleta;
-- Resultado: 2 coletas
```

**Dados da coleta:**
```sql
SELECT c.id, c.id_patrimonio, p.id_sala 
FROM local_coleta c 
LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id;
-- Resultado: Coleta ID 2, Patrimônio 103, Sala 119
```

**Query completa funciona:**
```sql
SELECT c.*, p.numero as NUMERO_PATRIMONIO, p.descricao as DESCRICAO_PATRIMONIO 
FROM local_coleta c 
LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id 
LEFT JOIN local_inventario i ON c.id_inventario = i.id 
WHERE p.id_sala = 119 
ORDER BY c.data_coleta DESC;
-- Resultado: ✅ Retorna 1 coleta (ID 2, Patrimônio 107994)
```

### 2. Código Java

**Método:** `ColetaFrame_v2.carregarHistoricoColeta()`

**Fluxo:**
1. Limpa tabela: `modeloTabelaHistorico.setRowCount(0)`
2. Obtém sala selecionada: `comboSalas.getSelectedItem()`
3. Busca coletas: `coletaDAO.buscarColetasPorSala(salaAtual.getIdSala())`
4. Preenche tabela com resultados

**Método DAO:** `ColetaDAO.buscarColetasPorSala()`

**Query usada (SQLite):**
```java
sql = "SELECT c.*, p.numero as NUMERO_PATRIMONIO, p.descricao as DESCRICAO_PATRIMONIO, " +
      "NULL as NOME_COLETOR, i.nome as DESCRICAO_INVENTARIO " +
      "FROM " + getColetaTableName() + " c " +
      "LEFT JOIN " + getPatrimonioTableName() + " p ON c.id_patrimonio = p.id " +
      "LEFT JOIN " + getInventarioTableName() + " i ON c.id_inventario = i.id " +
      "WHERE p.id_sala = ? ORDER BY c.data_coleta DESC";
```

## 🎯 Possíveis Causas

### 1. ⚠️ Aplicação não foi reiniciada
**Sintoma:** Código antigo ainda em execução  
**Solução:** Fechar e reabrir aplicação

### 2. ⚠️ Sala selecionada tem ID diferente
**Sintoma:** Busca por sala ID X mas coletas estão na sala ID Y  
**Solução:** Verificar qual sala está sendo selecionada

### 3. ⚠️ Erro silencioso no código
**Sintoma:** Exception capturada mas não exibida  
**Solução:** Verificar logs do console

### 4. ⚠️ Método auxiliar retorna nome errado
**Sintoma:** `getColetaTableName()` retorna nome errado  
**Solução:** Verificar se detecta SQLite corretamente

## 🧪 Testes para Fazer

### Teste 1: Verificar Logs do Console

Ao selecionar uma sala, deve aparecer:
```
=== DEBUG TIMESTAMP: Iniciando carregarHistoricoColeta ===
DEBUG TIMESTAMP: Buscando coletas para sala ID: 119
DEBUG TIMESTAMP: Encontradas X coletas
```

**Se aparecer "Encontradas 0 coletas":**
- Problema na query ou no ID da sala

**Se não aparecer nada:**
- Aplicação não foi reiniciada ou erro antes de chamar o método

### Teste 2: Verificar ID da Sala Selecionada

Adicionar log temporário:
```java
Sala salaAtual = (Sala) comboSalas.getSelectedItem();
System.out.println("DEBUG: Sala selecionada ID: " + salaAtual.getIdSala());
System.out.println("DEBUG: Sala selecionada Nome: " + salaAtual.getDescricao());
```

### Teste 3: Testar Query Diretamente

```sql
-- Verificar qual sala tem coletas
SELECT DISTINCT p.id_sala, COUNT(*) as total_coletas
FROM local_coleta c
LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id
GROUP BY p.id_sala;
```

### Teste 4: Verificar Detecção de SQLite

Adicionar log no método:
```java
private boolean isSQLite() throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String dbUrl = conn.getMetaData().getURL();
        boolean result = dbUrl != null && dbUrl.contains("jdbc:sqlite");
        System.out.println("DEBUG: isSQLite() = " + result + " (URL: " + dbUrl + ")");
        return result;
    }
}
```

## 🔧 Soluções

### Solução 1: Reiniciar Aplicação (MAIS PROVÁVEL)

```powershell
# Matar todos os processos Java
Get-Process java | Stop-Process -Force

# Reabrir aplicação
```

### Solução 2: Verificar Sala Correta

1. Verificar qual sala tem coletas:
```sql
SELECT p.id_sala, s.descricao, COUNT(*) as total
FROM local_coleta c
LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN local_sala s ON p.id_sala = s.id
GROUP BY p.id_sala, s.descricao;
```

2. Selecionar essa sala no combo

### Solução 3: Adicionar Logs Detalhados

Modificar temporariamente `ColetaDAO.buscarColetasPorSala()`:

```java
public List<Coleta> buscarColetasPorSala(int idSala) throws SQLException {
    boolean sqlite = isSQLite();
    System.out.println("DEBUG: buscarColetasPorSala() - SQLite: " + sqlite);
    System.out.println("DEBUG: buscarColetasPorSala() - ID Sala: " + idSala);
    
    String sql;
    if (sqlite) {
        sql = "SELECT c.*, p.numero as NUMERO_PATRIMONIO, p.descricao as DESCRICAO_PATRIMONIO, " +
              "NULL as NOME_COLETOR, i.nome as DESCRICAO_INVENTARIO " +
              "FROM " + getColetaTableName() + " c " +
              "LEFT JOIN " + getPatrimonioTableName() + " p ON c.id_patrimonio = p.id " +
              "LEFT JOIN " + getInventarioTableName() + " i ON c.id_inventario = i.id " +
              "WHERE p.id_sala = ? ORDER BY c.data_coleta DESC";
    } else {
        // PostgreSQL query...
    }
    
    System.out.println("DEBUG: SQL: " + sql);
    
    List<Coleta> coletas = new ArrayList<>();
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, idSala);
        System.out.println("DEBUG: Executando query...");
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                coletas.add(criarColetaFromResultSet(rs));
            }
        }
    }
    
    System.out.println("DEBUG: Coletas encontradas: " + coletas.size());
    return coletas;
}
```

## 📊 Dados de Teste

### Sala com Coletas
- **ID:** 119
- **Nome:** CAE
- **Total de Coletas:** 1
- **Patrimônio:** 107994

### Coleta Registrada
- **ID:** 2
- **Patrimônio:** 103 (número 107994)
- **Sala:** 119 (CAE)
- **Data:** 2025-11-21 20:33:10
- **Status:** COLETADO

## ✅ Checklist de Verificação

- [ ] Aplicação foi reiniciada após compilação
- [ ] Sala correta foi selecionada (ID 119 - CAE)
- [ ] Logs aparecem no console
- [ ] Query SQL está correta
- [ ] Método `isSQLite()` retorna `true`
- [ ] Método `getColetaTableName()` retorna `local_coleta`
- [ ] Método `getPatrimonioTableName()` retorna `local_patrimonio`
- [ ] Método `getInventarioTableName()` retorna `local_inventario`

## 🎯 Próximo Passo

**REINICIAR A APLICAÇÃO** e testar novamente selecionando a sala **CAE (ID 119)**.

Se o problema persistir, adicionar os logs detalhados sugeridos acima.

---

**Data:** 21/11/2025  
**Status:** 🔍 DIAGNÓSTICO COMPLETO  
**Ação:** REINICIAR APLICAÇÃO E TESTAR

