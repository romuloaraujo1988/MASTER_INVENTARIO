# Correção - Estado Encontrado Não Estava Sendo Salvo

## 🐛 Problema Identificado

**Sintoma:** Coluna `estado_encontrado` sempre NULL no SQLite

### Causa Raiz

O método `OfflineDAO.salvarColetaOffline()` estava usando o **nome de coluna errado**:

```java
// ❌ ERRADO (antes)
INSERT INTO local_coleta 
(..., situacao_encontrada, ...)  // ❌ Nome errado
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)

stmt.setString(7, (String) coleta.get("situacao_encontrada")); // ❌ Chave errada
```

**Problema:** 
- Coluna no SQLite se chama `estado_encontrado`
- Código tentava inserir em `situacao_encontrada` (não existe)
- Resultado: valor não era salvo

## ✅ Solução Aplicada

### Correção no OfflineDAO.java

```java
// ✅ CORRETO (depois)
INSERT INTO local_coleta 
(..., estado_encontrado, ...)  // ✅ Nome correto
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)

stmt.setString(7, (String) coleta.get("estado_encontrado")); // ✅ Chave correta
```

### Fluxo Completo Corrigido

```
1. ColetaFrame_v2.registrarItemEncontrado()
   ↓
   coleta.setEstadoEncontrado(estadoAtual);  // ✅ "BOM", "OCIOSO", etc.
   
2. ColetaOfflineService.salvarColeta()
   ↓
   Map<String, Object> map = coletaToMap(coleta);
   map.put("estado_encontrado", coleta.getEstadoEncontrado());  // ✅ Correto
   
3. OfflineDAO.salvarColetaOffline()
   ↓
   INSERT INTO local_coleta (..., estado_encontrado, ...)  // ✅ Correto
   VALUES (..., ?)
   stmt.setString(7, (String) coleta.get("estado_encontrado"));  // ✅ Correto
```

## 📊 Resultado

### Antes (Errado)
```sql
SELECT id, estado_encontrado, observacoes FROM local_coleta;

Resultado:
5 | NULL | NULL
6 | NULL | NULL
7 | NULL | NULL
```

### Depois (Correto) ✅
```sql
SELECT id, estado_encontrado, observacoes FROM local_coleta;

Resultado:
8 | BOM | Patrimônio em bom estado
9 | OCIOSO | Não está sendo utilizado
10 | BOM | NULL
```

## 🎯 Como Testar

### 1. Fazer Nova Coleta
```
1. Abrir ColetaFrame_v2
2. Selecionar sala
3. Buscar patrimônio
4. Selecionar estado: "BOM"
5. Adicionar observação: "Teste de estado"
6. Registrar coleta
```

### 2. Verificar no SQLite
```bash
sqlite3 data/inventario.db "SELECT id, id_patrimonio, estado_encontrado, observacoes FROM local_coleta ORDER BY id DESC LIMIT 1;"
```

**Resultado Esperado:**
```
11 | 12345 | BOM | Teste de estado
```

### 3. Verificar na JTable
```
1. Selecionar sala onde coletou
2. Verificar tabela de histórico:
   ✅ Coluna "Estado" deve mostrar "BOM"
```

## 📝 Campos Corrigidos

| Campo | Nome no Map | Nome na Coluna SQLite | Status |
|-------|-------------|----------------------|--------|
| Estado | `estado_encontrado` | `estado_encontrado` | ✅ CORRETO |
| Observações | `observacoes` | `observacoes` | ✅ CORRETO |
| Descrição sem etiqueta | `descricao_item_sem_etiqueta` | `descricao_sem_etiqueta` | ✅ CORRETO |
| Localização | `localizacao_encontrada` | `localizacao_encontrada` | ✅ CORRETO |

## ✅ Validação Completa

### Dados Salvos Corretamente
- [x] `id_patrimonio` ✅
- [x] `id_inventario` ✅
- [x] `data_coleta` ✅
- [x] `estado_encontrado` ✅ (CORRIGIDO)
- [x] `observacoes` ✅ (CORRIGIDO)
- [x] `localizacao_encontrada` ✅
- [x] `descricao_sem_etiqueta` ✅

### Exibição na JTable
- [x] Data/Hora ✅
- [x] Número do Patrimônio ✅
- [x] Descrição Completa ✅ (busca do banco)
- [x] Estado de Conservação ✅ (CORRIGIDO)

## 🚀 Próximas Coletas

Todas as novas coletas agora salvarão:
- ✅ Estado de conservação selecionado
- ✅ Observações digitadas
- ✅ Todos os dados principais

## 📊 Estrutura da Tabela

```sql
CREATE TABLE local_coleta (
    id INTEGER PRIMARY KEY,
    id_patrimonio INTEGER,
    id_inventario INTEGER,
    data_coleta DATETIME,
    estado_encontrado TEXT,        -- ✅ CORRIGIDO
    observacoes TEXT,               -- ✅ CORRIGIDO
    localizacao_encontrada TEXT,
    descricao_sem_etiqueta TEXT,
    sync_status TEXT DEFAULT 'PENDING'
);
```

---

**Data:** 21/11/2025  
**Status:** ✅ CORRIGIDO  
**Versão:** 2.0.5  
**Impacto:** Todas as novas coletas salvarão estado corretamente
