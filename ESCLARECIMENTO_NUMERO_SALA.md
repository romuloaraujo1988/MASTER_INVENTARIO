# Esclarecimento - Campo numero_sala

## ✅ Estrutura Correta das Tabelas

### TABELA_SALA (ou SALA)
**Contém informações da sala:**
```sql
CREATE TABLE TABELA_SALA (
    ID_SALA INTEGER PRIMARY KEY,
    NUMERO_SALA TEXT NOT NULL,      -- ✅ PERTENCE AQUI
    NOME_SALA TEXT,
    DESCRICAO TEXT,
    ANDAR TEXT,
    BLOCO TEXT,
    CAPACIDADE INTEGER,
    TIPO_SALA TEXT,
    ATIVA BOOLEAN
);
```

### TABELA_SALA_INVENTARIO
**Relaciona sala com inventário (muitos-para-muitos):**
```sql
CREATE TABLE TABELA_SALA_INVENTARIO (
    ID_SALA_INVENTARIO SERIAL PRIMARY KEY,  -- PostgreSQL
    ID_SALA INTEGER NOT NULL,                -- FK para TABELA_SALA
    ID_INVENTARIO INTEGER NOT NULL,          -- FK para TABELA_INVENTARIO
    -- ❌ NUMERO_SALA NÃO PERTENCE AQUI
    ID_PARTICIPANTE INTEGER,
    COLETA_FINALIZADA BOOLEAN,
    STATUS_COLETA TEXT,
    TOTAL_ITENS_COLETADOS INTEGER,
    ...
);
```

## 📊 Relacionamento Correto

```
TABELA_SALA (1) ←→ (N) TABELA_SALA_INVENTARIO (N) ←→ (1) TABELA_INVENTARIO
    ↓                           ↓                              ↓
NUMERO_SALA              ID_SALA (FK)                    ID_INVENTARIO (FK)
NOME_SALA                ID_INVENTARIO (FK)              NOME
DESCRICAO                STATUS_COLETA                   DATA_INICIO
...                      TOTAL_ITENS                     ...
```

## 🎯 Como Obter numero_sala

### Opção 1: JOIN
```sql
SELECT 
    si.ID_SALA_INVENTARIO,
    si.ID_SALA,
    s.NUMERO_SALA,           -- ✅ Buscar da tabela SALA
    s.NOME_SALA,
    si.STATUS_COLETA
FROM TABELA_SALA_INVENTARIO si
JOIN TABELA_SALA s ON si.ID_SALA = s.ID_SALA;
```

### Opção 2: Buscar Separadamente
```java
// 1. Buscar sala-inventário
SalaInventario si = salaInventarioDAO.buscarPorId(id);

// 2. Buscar sala para obter numero_sala
Sala sala = salaDAO.buscarPorId(si.getIdSala());
String numeroSala = sala.getNumeroSala();  // ✅ Correto
```

## ✅ Correção Aplicada Foi Correta

### Antes (Errado)
```java
// ❌ Tentava inserir NUMERO_SALA em TABELA_SALA_INVENTARIO
INSERT INTO TABELA_SALA_INVENTARIO 
(ID_SALA, NUMERO_SALA, ID_INVENTARIO, ...)  // ❌ NUMERO_SALA não existe
VALUES (?, ?, ?, ...)
```

### Depois (Correto)
```java
// ✅ Removido NUMERO_SALA (não pertence a esta tabela)
INSERT INTO TABELA_SALA_INVENTARIO 
(ID_SALA, ID_INVENTARIO, ...)  // ✅ Apenas FKs
VALUES (?, ?, ...)
```

## 📋 Onde numero_sala É Usado

### 1. Tabela SALA ✅
```sql
-- Armazenado aqui
INSERT INTO TABELA_SALA (ID_SALA, NUMERO_SALA, ...) VALUES (?, ?, ...);
```

### 2. Tabela COLETA ✅
```sql
-- Pode ser armazenado para histórico
INSERT INTO local_coleta (..., localizacao_encontrada, ...) 
VALUES (..., 'SALA 101', ...);
```

### 3. Queries com JOIN ✅
```sql
-- Obtido via JOIN
SELECT s.NUMERO_SALA, si.STATUS_COLETA
FROM TABELA_SALA_INVENTARIO si
JOIN TABELA_SALA s ON si.ID_SALA = s.ID_SALA;
```

## 🎯 Diferenças PostgreSQL vs SQLite

### PostgreSQL
```sql
-- TABELA_SALA_INVENTARIO
-- ❌ Não tem NUMERO_SALA
-- ✅ Usa JOIN para obter

-- TABELA_SALA
-- ✅ Tem NUMERO_SALA
```

### SQLite (Offline)
```sql
-- TABELA_SALA_INVENTARIO
-- ✅ Pode ter NUMERO_SALA (desnormalizado para performance offline)

-- local_sala
-- ✅ Tem nome (equivalente a numero_sala)
```

## ✅ Conclusão

**A correção aplicada estava correta:**
- ✅ `NUMERO_SALA` pertence à tabela `SALA`
- ✅ `TABELA_SALA_INVENTARIO` não deve ter `NUMERO_SALA` no PostgreSQL
- ✅ Usar JOIN ou busca separada para obter `numero_sala`
- ✅ SQLite pode ter desnormalização para performance offline

**Estrutura normalizada e correta!** ✅

---

**Data:** 21/11/2025  
**Status:** ✅ ESCLARECIDO  
**Conclusão:** Correção aplicada estava correta
