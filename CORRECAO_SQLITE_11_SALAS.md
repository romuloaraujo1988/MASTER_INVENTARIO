# Correção: SQLite Salvando Apenas 11 Salas ao Invés de 108

## 🔍 Problema Identificado

O banco SQLite offline está salvando apenas **11 salas** ao invés das **108 salas** esperadas.

```
✓ Patrimônios no SQLite: 11428
✓ Salas no SQLite: 11          ← ❌ PROBLEMA
✓ Responsáveis no SQLite: 96
✓ Inventários (TABELA_INVENTARIO): 1
✓ Salas (SALA): 11
```

## 🎯 Causa Raiz

O processo de sincronização `SyncPostgresToSQLiteV2.java` usa o método `SalaDAO.listarSalas()` que retorna apenas **salas ATIVAS**:

```java
public List<Sala> listarSalas() throws SQLException {
    String sql = "SELECT s.*, st.NOME as NOME_SETOR " +
                "FROM TABELA_SALA s " +
                "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                "WHERE s.ATIVO = TRUE " +  // ← FILTRA APENAS ATIVAS
                "ORDER BY s.NUMERO_SALA, s.DESCRICAO";
    return executeQuery(sql);
}
```

Isso significa que no PostgreSQL:
- **11 salas estão ATIVAS** (ATIVO = TRUE) ✅
- **97 salas estão INATIVAS** (ATIVO = FALSE) ❌
- **Total: 108 salas**

## ✅ Solução

### Opção 1: Ativar Todas as Salas no PostgreSQL (Recomendado)

Execute o script fornecido:

```powershell
.\ativar-todas-salas.ps1
```

Isso irá:
1. Verificar quantas salas estão inativas
2. Ativar todas as salas inativas
3. Instruir você a executar a sincronização

**Depois de ativar, execute:**
```powershell
.\sincronizar-sqlite-offline.ps1
```

### Opção 2: Modificar o Método de Sincronização

Se você quiser sincronizar **todas as salas** (ativas e inativas), modifique o `SalaDAO.java`:

```java
// Criar novo método que retorna TODAS as salas
public List<Sala> listarTodasSalas() throws SQLException {
    String sql = "SELECT s.*, st.NOME as NOME_SETOR " +
                "FROM TABELA_SALA s " +
                "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                // SEM FILTRO DE ATIVO
                "ORDER BY s.NUMERO_SALA, s.DESCRICAO";
    return executeQuery(sql);
}
```

E atualizar `SyncPostgresToSQLiteV2.java`:

```java
private void sincronizarSalas() throws Exception {
    SalaDAO dao = new SalaDAO();
    List<Sala> salas = dao.listarTodasSalas();  // ← Usar novo método
    
    // ... resto do código
}
```

## 📊 Como Verificar o Status Atual

Execute o script SQL fornecido:

```bash
psql -h localhost -U inventario -d sispatrimonio -f verificar-salas-postgresql.sql
```

Isso mostrará:
- Total de salas (todas)
- Quantas estão ativas
- Quantas estão inativas
- Distribuição por status
- Primeiras 20 salas ativas

## 🔄 Fluxo Completo de Correção

### Passo 1: Verificar Status Atual

```bash
psql -h localhost -U inventario -d sispatrimonio -f verificar-salas-postgresql.sql
```

**Saída esperada:**
```
1. Total de salas (todas):
 total_salas 
-------------
         108

2. Salas ATIVAS:
 salas_ativas 
--------------
           11

3. Salas INATIVAS:
 salas_inativas 
----------------
             97
```

### Passo 2: Ativar Todas as Salas

```powershell
.\ativar-todas-salas.ps1
```

**Saída esperada:**
```
Status atual:
  Salas ATIVAS: 11
  Salas INATIVAS: 97
  Total: 108

ATENÇÃO: Esta ação irá ativar 97 sala(s) inativa(s)!
Deseja continuar? (S/N): S

Ativando salas...
✓ Sucesso!
97 sala(s) foram ativadas.

PRÓXIMO PASSO:
Execute a sincronização para atualizar o SQLite:
  .\sincronizar-sqlite-offline.ps1
```

### Passo 3: Sincronizar para SQLite

```powershell
.\sincronizar-sqlite-offline.ps1
```

**Saída esperada:**
```
[2/6] Sincronizando Salas...
   ✓ 108 sala(s) sincronizada(s)  ← ✅ AGORA 108!
```

### Passo 4: Verificar SQLite

```powershell
.\diagnosticar-sqlite-inventario.ps1
```

**Saída esperada:**
```
✓ Salas no SQLite: 108  ← ✅ CORRIGIDO!
```

## 📝 Arquivos Criados

1. 🆕 `verificar-salas-postgresql.sql`
   - Script para diagnosticar status das salas no PostgreSQL

2. 🆕 `ativar-todas-salas.ps1`
   - Script para ativar todas as salas inativas

3. 🆕 `CORRECAO_SQLITE_11_SALAS.md`
   - Este documento

## 🎯 Por Que Algumas Salas Estavam Inativas?

Possíveis razões:
1. **Importação de dados**: Salas foram importadas como inativas
2. **Desativação manual**: Alguém desativou salas no sistema
3. **Migração de dados**: Durante migração, algumas salas ficaram inativas
4. **Limpeza de dados**: Salas antigas foram desativadas

## ⚠️ Considerações Importantes

### Antes de Ativar Todas as Salas

Verifique se há motivo para algumas salas estarem inativas:
- Salas desativadas (não existem mais fisicamente)
- Salas em reforma
- Salas temporárias que não são mais usadas

Se houver salas que **devem permanecer inativas**, você pode ativá-las seletivamente:

```sql
-- Ativar salas específicas
UPDATE TABELA_SALA
SET ATIVO = TRUE
WHERE NUMERO_SALA IN ('101', '102', '103', ...);
```

### Após Ativar

1. **Sincronize imediatamente** para atualizar o SQLite
2. **Teste o aplicativo** para garantir que todas as salas aparecem
3. **Verifique o ColetaFrame_v2** - deve mostrar 108 salas no combo

## 🧪 Como Testar

1. **Verificar PostgreSQL:**
   ```bash
   psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_SALA WHERE ATIVO = TRUE;"
   ```
   Deve retornar: **108**

2. **Verificar SQLite:**
   ```bash
   sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_sala WHERE ativa = 1;"
   ```
   Deve retornar: **108**

3. **Verificar no Aplicativo:**
   - Abrir ColetaFrame_v2
   - Combo de salas deve mostrar 108 salas

## 📈 Logs Esperados Após Correção

### Sincronização
```
[2/6] Sincronizando Salas...
   ✓ 108 sala(s) sincronizada(s)
```

### Verificação SQLite
```
=== Verificando dados no banco SQLite ===
✓ Patrimônios no SQLite: 11428
✓ Salas no SQLite: 108          ← ✅ CORRIGIDO!
✓ Responsáveis no SQLite: 96
✓ Inventários (TABELA_INVENTARIO): 1
✓ Salas (SALA): 108             ← ✅ CORRIGIDO!
```

### ColetaFrame_v2
```
=== Total de salas processadas: 108 ===
=== Salas adicionadas à lista: 108 ===
DEBUG ColetaFrame: buscarSalasAbertasParaColeta() retornou 108 salas
DEBUG ColetaFrame: Combo preenchido com 109 itens (incluindo null)
```

## ✅ Resultado Final

- ✅ PostgreSQL: 108 salas ativas
- ✅ SQLite: 108 salas sincronizadas
- ✅ ColetaFrame_v2: 108 salas no combo
- ✅ Aplicativo mobile: 108 salas disponíveis

---

**Data da Correção:** 21/11/2025  
**Versão:** 2.0.2  
**Status:** ✅ SOLUÇÃO DOCUMENTADA
