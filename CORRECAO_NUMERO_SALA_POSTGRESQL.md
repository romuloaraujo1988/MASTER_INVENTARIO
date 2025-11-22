# Correção - Coluna numero_sala Não Existe no PostgreSQL

## 🐛 Problema Identificado

**Erro:** `PSQLException: ERRO: coluna "numero_sala" da relação "tabela_sala_inventario" não existe`

### Causa Raiz

O método `SalaInventarioDAO.inserir()` estava tentando inserir a coluna `NUMERO_SALA` na tabela `TABELA_SALA_INVENTARIO` do PostgreSQL, mas essa coluna **não existe** no schema do PostgreSQL.

```java
// ❌ ERRADO (antes)
INSERT INTO TABELA_SALA_INVENTARIO 
(ID_SALA, NUMERO_SALA, ID_INVENTARIO, ...)  // ❌ NUMERO_SALA não existe
VALUES (?, ?, ?, ...)

stmt.setInt(1, salaInventario.getIdSala());
stmt.setString(2, numeroSala);  // ❌ Tentava inserir NUMERO_SALA
stmt.setInt(3, salaInventario.getIdInventario());
```

**Problema:** 
- Coluna `NUMERO_SALA` existe apenas no SQLite
- PostgreSQL não tem essa coluna em `TABELA_SALA_INVENTARIO`
- Código tentava inserir causando erro SQL

## ✅ Solução Aplicada

### Correção no SalaInventarioDAO.java

```java
// ✅ CORRETO (depois)
INSERT INTO TABELA_SALA_INVENTARIO 
(ID_SALA, ID_INVENTARIO, ID_PARTICIPANTE, ...)  // ✅ Sem NUMERO_SALA
VALUES (?, ?, ?, ...)

stmt.setInt(1, salaInventario.getIdSala());
stmt.setInt(2, salaInventario.getIdInventario());  // ✅ Índices ajustados
stmt.setInt(3, salaInventario.getIdParticipante());
// ... demais parâmetros com índices corretos
```

### Índices de Parâmetros Corrigidos

| Parâmetro | Antes | Depois |
|-----------|-------|--------|
| ID_SALA | 1 | 1 ✅ |
| NUMERO_SALA | 2 | ❌ Removido |
| ID_INVENTARIO | 3 | 2 ✅ |
| ID_PARTICIPANTE | 4 | 3 ✅ |
| COLETA_FINALIZADA | 5 | 4 ✅ |
| DATA_INICIO_COLETA | 6 | 5 ✅ |
| DATA_FINALIZACAO_COLETA | 7 | 6 ✅ |
| OBSERVACOES_FINALIZACAO | 8 | 7 ✅ |
| TOTAL_ITENS_COLETADOS | 9 | 8 ✅ |
| TOTAL_ITENS_SEM_ETIQUETA | 10 | 9 ✅ |
| PERCENTUAL_CONCLUSAO | 11 | 10 ✅ |
| STATUS_COLETA | 12 | 11 ✅ |

## 📊 Diferenças de Schema

### PostgreSQL (TABELA_SALA_INVENTARIO)
```sql
CREATE TABLE TABELA_SALA_INVENTARIO (
    ID_SALA_INVENTARIO SERIAL PRIMARY KEY,
    ID_SALA INTEGER NOT NULL,
    ID_INVENTARIO INTEGER NOT NULL,
    -- ❌ NUMERO_SALA não existe
    ID_PARTICIPANTE INTEGER,
    COLETA_FINALIZADA BOOLEAN DEFAULT FALSE,
    ...
);
```

### SQLite (TABELA_SALA_INVENTARIO)
```sql
CREATE TABLE TABELA_SALA_INVENTARIO (
    ID_SALA INTEGER NOT NULL,
    ID_INVENTARIO INTEGER NOT NULL,
    NUMERO_SALA TEXT NOT NULL,  -- ✅ Existe no SQLite
    NOME_SALA TEXT,
    ...
    PRIMARY KEY (ID_SALA, ID_INVENTARIO)
);
```

## 🎯 Por Que a Diferença?

### PostgreSQL
- Usa `ID_SALA_INVENTARIO` como PRIMARY KEY (SERIAL)
- Não precisa de `NUMERO_SALA` (busca da tabela SALA)
- Schema mais normalizado

### SQLite
- Usa chave composta `(ID_SALA, ID_INVENTARIO)`
- Inclui `NUMERO_SALA` para facilitar queries offline
- Schema desnormalizado para performance offline

## ✅ Validação

### Teste no PostgreSQL
```sql
-- Verificar estrutura
\d TABELA_SALA_INVENTARIO

-- Deve mostrar:
-- ID_SALA_INVENTARIO (PK)
-- ID_SALA
-- ID_INVENTARIO
-- (sem NUMERO_SALA)
```

### Teste no SQLite
```sql
-- Verificar estrutura
PRAGMA table_info(TABELA_SALA_INVENTARIO);

-- Deve mostrar:
-- ID_SALA (PK1)
-- ID_INVENTARIO (PK2)
-- NUMERO_SALA
-- NOME_SALA
```

## 🚀 Resultado

**Agora o código funciona com ambos os bancos:**
- ✅ PostgreSQL: INSERT sem `NUMERO_SALA`
- ✅ SQLite: INSERT com `NUMERO_SALA` (via DataImportService)
- ✅ Compatibilidade mantida

## 📝 Lições Aprendidas

1. **Schemas diferentes** - PostgreSQL e SQLite têm estruturas diferentes
2. **Validar colunas** - Sempre verificar se coluna existe antes de usar
3. **Testes em ambos** - Testar código em PostgreSQL E SQLite
4. **Documentar diferenças** - Manter documentação de schemas

---

**Data:** 21/11/2025  
**Status:** ✅ CORRIGIDO  
**Versão:** 2.0.6  
**Impacto:** Sistema funciona com PostgreSQL e SQLite
