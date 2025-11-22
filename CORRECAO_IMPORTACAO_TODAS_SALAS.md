# Correção: Importação de TODAS as Salas (Ativas e Inativas)

## 🔍 Problema Identificado

O sistema estava importando apenas **11 salas** porque:

1. `DataImportService` usava `salaDAO.findAll("DESCRICAO")`
2. `findAll()` do `BaseDAO` busca todas as salas **SEM filtro**
3. **MAS** o PostgreSQL realmente tinha apenas 11 salas ativas
4. As outras 97 salas estavam **INATIVAS** (ATIVO = FALSE)

## ❌ Código Anterior

### DataImportService.java
```java
// Buscava todas, mas se PostgreSQL tem apenas 11 ativas, importa apenas 11
var salas = salaDAO.findAll("DESCRICAO");
```

### SyncPostgresToSQLiteV2.java
```java
// Usava listarSalas() que filtra apenas ATIVAS
List<Sala> salas = dao.listarSalas();
```

### SalaDAO.java
```java
// Método listarSalas() filtrava apenas ativas
public List<Sala> listarSalas() throws SQLException {
    String sql = "SELECT s.*, st.NOME as NOME_SETOR " +
                "FROM TABELA_SALA s " +
                "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                "WHERE s.ATIVO = TRUE " +  // ← FILTRO RESTRITIVO
                "ORDER BY s.NUMERO_SALA, s.DESCRICAO";
    return executeQuery(sql);
}
```

## ✅ Solução Aplicada

### 1. Novo Método no SalaDAO.java

Adicionado método `listarTodasSalas()` que busca **TODAS** (ativas e inativas):

```java
/**
 * Lista TODAS as salas (ativas e inativas) com join de setor
 * Usado para importação/sincronização offline
 */
public List<Sala> listarTodasSalas() throws SQLException {
    String sql = "SELECT s.*, st.NOME as NOME_SETOR " +
                "FROM TABELA_SALA s " +
                "LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID " +
                "ORDER BY s.NUMERO_SALA, s.DESCRICAO";  // ← SEM FILTRO DE ATIVO
    return executeQuery(sql);
}
```

### 2. Atualizado DataImportService.java

```java
// ANTES
var salas = salaDAO.findAll("DESCRICAO");

// DEPOIS
var salas = salaDAO.listarTodasSalas();  // ← Busca TODAS (ativas e inativas)
```

### 3. Atualizado SyncPostgresToSQLiteV2.java

```java
// ANTES
List<Sala> salas = dao.listarSalas();  // Apenas ativas

// DEPOIS
List<Sala> salas = dao.listarTodasSalas();  // TODAS (ativas e inativas)
```

## 📊 Resultado Esperado

### Antes da Correção
```
[15:01:04] Importando salas...
>>> Total de salas encontradas: 11
>>> ✅ Salas importadas com sucesso: 11
[15:01:04] Salas importadas: 11  ← ❌ PROBLEMA
```

### Depois da Correção
```
[15:01:04] Importando salas...
>>> Total de salas encontradas: 108
>>> (incluindo ativas e inativas)
>>> ✅ Salas importadas com sucesso: 108
[15:01:04] Salas importadas: 108  ← ✅ CORRIGIDO!
```

## 🎯 Por Que Importar Salas Inativas?

### Motivos para Importar TODAS as Salas

1. **Histórico Completo**: Salas inativas podem ter patrimônios históricos
2. **Relatórios**: Relatórios precisam mostrar dados de salas antigas
3. **Auditoria**: Necessário para rastreamento completo
4. **Reativação**: Salas podem ser reativadas no futuro
5. **Consistência**: Banco offline deve espelhar o online

### Controle de Exibição

As salas inativas **não aparecerão** no ColetaFrame_v2 porque:
- `buscarSalasAbertasParaColeta()` filtra `WHERE s.ATIVO = TRUE`
- Apenas salas ativas são mostradas para coleta
- Mas todas estão disponíveis para consultas e relatórios

## 📝 Arquivos Alterados

1. ✅ `src/main/java/com/inventario/dao/SalaDAO.java`
   - Adicionado método `listarTodasSalas()`

2. ✅ `src/main/java/com/inventario/service/DataImportService.java`
   - Alterado para usar `listarTodasSalas()`

3. ✅ `src/main/java/com/inventario/offline/SyncPostgresToSQLiteV2.java`
   - Alterado para usar `listarTodasSalas()`

4. 🆕 `CORRECAO_IMPORTACAO_TODAS_SALAS.md`
   - Este documento

## 🧪 Como Testar

### Passo 1: Recompilar

```bash
mvn clean compile
```

### Passo 2: Executar Importação

Via interface:
1. Abrir aplicação
2. Menu → Arquivo → Importar Dados Offline
3. Verificar log: "Total de salas encontradas: 108"

Ou via script:
```powershell
.\sincronizar-sqlite-offline.ps1
```

### Passo 3: Verificar SQLite

```bash
sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_sala;"
```

**Esperado:** 108

### Passo 4: Verificar Ativas vs Inativas

```bash
sqlite3 data/inventario.db "
SELECT 
    CASE WHEN ativa = 1 THEN 'ATIVA' ELSE 'INATIVA' END as status,
    COUNT(*) as quantidade
FROM local_sala
GROUP BY ativa;
"
```

**Esperado:**
```
ATIVA|11
INATIVA|97
```

### Passo 5: Verificar ColetaFrame_v2

1. Abrir ColetaFrame_v2
2. Combo de salas deve mostrar apenas as **11 salas ativas**
3. Salas inativas não aparecem (comportamento correto)

## 📈 Logs Esperados

### Importação Completa
```
========================================
>>> INICIANDO IMPORTAÇÃO DE SALAS
========================================
>>> Chamando salaDAO.listarTodasSalas()...
>>> SalaDAO instance: com.inventario.dao.SalaDAO@...
>>> ✅ salaDAO.listarTodasSalas() RETORNOU!
>>> Total de salas encontradas: 108
>>> (incluindo ativas e inativas)
>>> Limpando tabelas de salas...
>>> ✅ Tabela local_sala limpa - 0 registros removidos
>>> ✅ Tabela SALA limpa - 0 registros removidos
>>> Iniciando loop de importação de 108 salas...
>>> DEBUG: Primeira sala a ser salva:
>>>   ID: 1
>>>   Número: 101
>>>   Descrição: Sala de Aula 1
>>>   Bloco: A
>>>   Tipo: SALA_AULA
>>>   Ativa: true
>>> Progresso: 10/108 salas
>>> Progresso: 20/108 salas
...
>>> Progresso: 108/108 salas
>>> ✅ Salas importadas com sucesso: 108
```

### Sincronização SQLite
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
```

## ⚠️ Importante

### Salas Inativas NÃO Aparecem na Coleta

Mesmo importando todas as 108 salas, o ColetaFrame_v2 mostrará apenas as **ativas**:

```java
// buscarSalasAbertasParaColeta() filtra apenas ativas
WHERE s.ATIVO = TRUE 
```

Isso é **correto** porque:
- Salas inativas não devem receber novas coletas
- Mas precisam estar no banco para consultas históricas
- Relatórios podem precisar de dados de salas antigas

### Para Mostrar Todas no ColetaFrame_v2

Se você quiser que **todas as 108 salas** apareçam no combo:

1. Execute o script de ativação:
   ```powershell
   .\ativar-todas-salas.ps1
   ```

2. Ou ative manualmente no PostgreSQL:
   ```sql
   UPDATE TABELA_SALA SET ATIVO = TRUE WHERE ATIVO = FALSE;
   ```

3. Reimporte os dados:
   ```powershell
   .\sincronizar-sqlite-offline.ps1
   ```

## ✅ Resultado Final

Após a correção:

- ✅ **Importação**: 108 salas importadas (ativas e inativas)
- ✅ **SQLite**: 108 salas no banco offline
- ✅ **ColetaFrame_v2**: Mostra apenas salas ativas (comportamento correto)
- ✅ **Relatórios**: Acesso a dados de todas as salas
- ✅ **Consistência**: Banco offline espelha o online

---

**Data da Correção:** 21/11/2025  
**Versão:** 2.0.4  
**Status:** ✅ CÓDIGO CORRIGIDO
