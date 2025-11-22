# Correção: Erro ao Buscar Patrimônio no Modo Offline

## 🔍 Problema Identificado

Erro ao buscar patrimônio no modo offline:
```
[SQLITE_ERROR] SQL error or missing database (no such table: TABELA_PATRIMONIO)
```

### Causa Raiz

O método `PatrimonioDAO.buscarPorNumero()` usava sempre as tabelas do PostgreSQL (`TABELA_PATRIMONIO`, `TABELA_RESPONSAVEL`, `TABELA_SALA`), mesmo quando conectado ao SQLite offline.

No SQLite, as tabelas têm nomes diferentes:
- ❌ `TABELA_PATRIMONIO` → ✅ `local_patrimonio`
- ❌ `TABELA_RESPONSAVEL` → ✅ `local_responsavel`
- ❌ `TABELA_SALA` → ✅ `local_sala`

## ✅ Solução Aplicada

Adicionada detecção automática do tipo de banco e uso das tabelas corretas:

```java
// Detectar tipo de banco
boolean isSQLite = dbUrl.contains("jdbc:sqlite");

// Usar tabelas corretas
String sql;
if (isSQLite) {
    // SQLite: usar tabelas local_*
    sql = "SELECT p.*, r.nome as nome_responsavel, s.descricao as nome_sala " +
          "FROM local_patrimonio p " +
          "LEFT JOIN local_responsavel r ON p.id_responsavel = r.id " +
          "LEFT JOIN local_sala s ON p.id_sala = s.id " +
          "WHERE p.numero = ?";
} else {
    // PostgreSQL: usar TABELA_*
    sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
          "FROM TABELA_PATRIMONIO p " +
          "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
          "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
          "WHERE p.NUMERO = ?";
}
```

## 📝 Arquivos Alterados

1. ✅ `src/main/java/com/inventario/dao/PatrimonioDAO.java`
   - Método `buscarPorNumero()` - Detecção de banco e tabelas corretas

2. 🆕 `CORRECAO_BUSCA_OFFLINE_PATRIMONIO.md`
   - Este documento

## 🧪 Como Testar

### Passo 1: Recompilar

```bash
mvn clean compile
```

### Passo 2: Testar Busca Offline

1. Abrir aplicação
2. Ir para Coleta de Patrimônios v2
3. Selecionar uma sala
4. Digitar número de patrimônio (ex: 108019)
5. Clicar "Buscar Item"

### Resultado Esperado

```
DEBUG PatrimonioDAO: buscarPorNumero() chamado para número: 108019
DEBUG PatrimonioDAO: Usando banco de dados: jdbc:sqlite:data/inventario.db
DEBUG PatrimonioDAO: *** BUSCA OFFLINE (SQLite) ***
DEBUG PatrimonioDAO: Resultado da busca: ENCONTRADO
```

## ⚠️ Observação Importante

### Diferenças entre PostgreSQL e SQLite

| Aspecto | PostgreSQL | SQLite |
|---------|-----------|--------|
| Tabelas | `TABELA_*` | `local_*` |
| Colunas | MAIÚSCULAS | minúsculas |
| Case-sensitive | Sim | Não |

### Outros Métodos que Precisam Correção

Os seguintes métodos também usam `TABELA_PATRIMONIO` e precisarão da mesma correção se forem usados no modo offline:

- ✅ `buscarPorNumero()` - **CORRIGIDO**
- ⏳ `buscarPorTermo()` - Precisa correção
- ⏳ `buscarPorDescricao()` - Precisa correção
- ⏳ `buscarPorSala()` - Precisa correção
- ⏳ `buscarPorResponsavel()` - Precisa correção
- ⏳ `listarTodosComJoins()` - Precisa correção

## 🎯 Próximas Correções Necessárias

Se outros métodos forem usados no modo offline, aplicar o mesmo padrão:

```java
public List<Patrimonio> buscarPorDescricao(String descricao) throws SQLException {
    // Detectar tipo de banco
    boolean isSQLite = detectarSQLite();
    
    String sql;
    if (isSQLite) {
        sql = "SELECT p.*, r.nome as nome_responsavel, s.descricao as nome_sala " +
              "FROM local_patrimonio p " +
              "LEFT JOIN local_responsavel r ON p.id_responsavel = r.id " +
              "LEFT JOIN local_sala s ON p.id_sala = s.id " +
              "WHERE p.descricao LIKE ?";
    } else {
        sql = "SELECT p.*, r.NOME as nome_responsavel, s.DESCRICAO as nome_sala " +
              "FROM TABELA_PATRIMONIO p " +
              "LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID " +
              "LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA " +
              "WHERE p.DESCRICAO ILIKE ?";
    }
    
    return executeQuery(sql, "%" + descricao + "%");
}
```

## ✅ Resultado Final

Após a correção:

- ✅ **Busca de patrimônio funciona no modo offline**
- ✅ **Detecção automática do tipo de banco**
- ✅ **Tabelas corretas usadas automaticamente**
- ✅ **Logs detalhados para debug**

---

**Data da Correção:** 21/11/2025  
**Versão:** 2.0.6  
**Status:** ✅ CÓDIGO CORRIGIDO - PRONTO PARA TESTE
