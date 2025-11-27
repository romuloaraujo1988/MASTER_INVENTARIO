# Análise Crítica: Sincronização SQLite → PostgreSQL (Coletas)

## 🔍 Verificação Realizada via MCP PostgreSQL

```sql
-- Estrutura da tabela TABELA_COLETA no PostgreSQL
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'tabela_coleta';
```

---

## ⚠️ PROBLEMAS CRÍTICOS IDENTIFICADOS

### 1. **INCOMPATIBILIDADE DE NOMES DE COLUNAS** 🔴 CRÍTICO

#### SQLite usa:
- `id_participante` (coluna 3)
- `situacao_encontrada` (coluna 8)

#### PostgreSQL usa:
- `id_participante_inventario` ✅
- `estado_encontrado` ✅

#### Impacto:
O código de sincronização está lendo `id_participante` do SQLite mas tentando inserir em `ID_PARTICIPANTE_INVENTARIO` no PostgreSQL. **Isso funciona**, mas há inconsistência nos nomes.

---

### 2. **CAMPOS OBRIGATÓRIOS NO POSTGRESQL** 🔴 CRÍTICO

Campos **NOT NULL** no PostgreSQL:
- ✅ `id` (auto-increment)
- ✅ `id_inventario`
- ✅ `id_coletor`
- ✅ `id_participante_inventario` ⚠️ **CRÍTICO**

Se qualquer um desses campos vier como `NULL` ou `0`, a sincronização **FALHA**.

---

### 3. **CAMPOS FALTANTES NO SQLITE** ⚠️ IMPORTANTE

Campos que existem no PostgreSQL mas **NÃO** no SQLite:

| Campo PostgreSQL | Existe no SQLite? | Impacto |
|------------------|-------------------|---------|
| `status_coleta` | ❌ | Usa default 'COLETADO' |
| `divergencia` | ❌ | Usa default FALSE |
| `motivo_divergencia` | ❌ | NULL |
| `latitude` | ❌ | NULL |
| `longitude` | ❌ | NULL |
| `sem_etiqueta` | ✅ | OK |
| `descricao_item_sem_etiqueta` | ✅ | OK |
| `categoria_item_sem_etiqueta` | ❌ | NULL |
| `observacao` | ❌ | NULL (diferente de observacao_coleta) |
| `tempo_coleta_segundos` | ❌ | NULL |
| `metodo_coleta` | ❌ | NULL |
| `tipo_scan` | ❌ | NULL |

**Impacto:** Perda de dados analíticos importantes (métricas de performance, geolocalização, etc.)

---

### 4. **MAPEAMENTO DE CAMPOS NO CÓDIGO** ⚠️ IMPORTANTE

#### No DataSynchronizer.java (linha 415):

```java
// ❌ PROBLEMA: Lê "situacao_encontrada" do SQLite
stmt.setString(8, objectToString(dados.get("situacao_encontrada")));

// Mas insere em "ESTADO_ENCONTRADO" no PostgreSQL
// INSERT INTO TABELA_COLETA (..., ESTADO_ENCONTRADO, ...)
```

**Status:** Funciona, mas há inconsistência de nomenclatura.

---

### 5. **VALIDAÇÕES CRÍTICAS** ✅ CORRETO

O código valida corretamente (linha 373-387):

```java
// ✅ Valida ID_PATRIMONIO
if (idPatrimonio == null || idPatrimonio == 0) {
    LOGGER.warning("ID_PATRIMONIO inválido");
    return false;
}

// ✅ Valida ID_INVENTARIO
if (idInventario == null || idInventario == 0) {
    LOGGER.warning("ID_INVENTARIO inválido");
    return false;
}

// ✅ Valida ID_PARTICIPANTE
if (idParticipante == null || idParticipante == 0) {
    LOGGER.warning("ID_PARTICIPANTE inválido");
    return false;
}
```

**Status:** ✅ Correto após a correção do ColetaFrame_v2.java

---

### 6. **CONVERSÃO DE TIMESTAMP** ⚠️ IMPORTANTE

#### SQLite armazena:
```
data_coleta: "2025-11-25 11:32:28.542" (TEXT/DATETIME)
```

#### PostgreSQL espera:
```
data_coleta: TIMESTAMP WITHOUT TIME ZONE
```

#### Código atual (linha 397-406):
```java
try {
    dataColeta = DateFormatUtils.toTimestampSafe(dataColetaObj);
} catch (Exception e) {
    LOGGER.warning("Erro ao converter timestamp - usando data atual");
    dataColeta = new Timestamp(System.currentTimeMillis());
}
```

**Status:** ✅ Tem fallback, mas pode perder a data original se conversão falhar.

---

### 7. **CAMPOS NULLABLE QUE PODEM CAUSAR PROBLEMAS** ⚠️

#### `id_patrimonio` no PostgreSQL é NULLABLE

Isso permite coletas de "itens sem etiqueta", mas pode causar problemas se:
- Código espera sempre um ID válido
- Relatórios não tratam NULL
- Joins sem LEFT JOIN

**Recomendação:** Sempre usar `LEFT JOIN` ao relacionar com `tabela_patrimonio`.

---

## 🔧 CORREÇÕES NECESSÁRIAS

### 1. ✅ CORRIGIDO: ID_PARTICIPANTE = 0

**Problema:** `ColetaFrame_v2.java` não definia `idParticipanteInventario`  
**Solução:** Adicionada busca via `ParticipanteInventarioDAO.buscarIdParticipantePorUsuario()`  
**Status:** ✅ Implementado

---

### 2. 🔴 CRÍTICO: Adicionar Campos Faltantes no SQLite

Para melhorar a qualidade dos dados, adicionar ao SQLite:

```sql
ALTER TABLE local_coleta ADD COLUMN status_coleta TEXT DEFAULT 'COLETADO';
ALTER TABLE local_coleta ADD COLUMN divergencia BOOLEAN DEFAULT FALSE;
ALTER TABLE local_coleta ADD COLUMN motivo_divergencia TEXT;
ALTER TABLE local_coleta ADD COLUMN latitude REAL;
ALTER TABLE local_coleta ADD COLUMN longitude REAL;
ALTER TABLE local_coleta ADD COLUMN categoria_item_sem_etiqueta TEXT;
ALTER TABLE local_coleta ADD COLUMN tempo_coleta_segundos INTEGER;
ALTER TABLE local_coleta ADD COLUMN metodo_coleta TEXT;
ALTER TABLE local_coleta ADD COLUMN tipo_scan TEXT;
```

**Benefício:** Dados completos para análise de performance e qualidade.

---

### 3. ⚠️ IMPORTANTE: Melhorar Conversão de Timestamp

```java
// Adicionar log mais detalhado
try {
    dataColeta = DateFormatUtils.toTimestampSafe(dataColetaObj);
    LOGGER.info("✅ Timestamp convertido: " + dataColetaObj + " -> " + dataColeta);
} catch (Exception e) {
    LOGGER.severe("❌ ERRO ao converter timestamp: " + dataColetaObj);
    LOGGER.severe("Tipo do objeto: " + dataColetaObj.getClass().getName());
    LOGGER.severe("Usando data atual como fallback");
    dataColeta = new Timestamp(System.currentTimeMillis());
}
```

---

### 4. ⚠️ IMPORTANTE: Validar Referências Estrangeiras

Antes de sincronizar, verificar se IDs existem:

```java
// Verificar se patrimônio existe no PostgreSQL
if (idPatrimonio != null && idPatrimonio > 0) {
    String checkSql = "SELECT 1 FROM TABELA_PATRIMONIO WHERE ID = ?";
    try (PreparedStatement check = conn.prepareStatement(checkSql)) {
        check.setInt(1, idPatrimonio);
        try (ResultSet rs = check.executeQuery()) {
            if (!rs.next()) {
                LOGGER.warning("Patrimônio ID " + idPatrimonio + " não existe no PostgreSQL");
                return false;
            }
        }
    }
}

// Verificar se inventário existe
// Verificar se participante existe
```

**Benefício:** Evita erros de constraint violation.

---

### 5. 🔴 CRÍTICO: Tratar Coletas Duplicadas

```java
// Antes de inserir, verificar se já existe
String checkDuplicateSql = """
    SELECT ID FROM TABELA_COLETA 
    WHERE ID_INVENTARIO = ? 
    AND ID_PATRIMONIO = ? 
    AND ID_PARTICIPANTE_INVENTARIO = ?
""";

try (PreparedStatement check = conn.prepareStatement(checkDuplicateSql)) {
    check.setInt(1, idInventario);
    check.setInt(2, idPatrimonio);
    check.setInt(3, idParticipante);
    
    try (ResultSet rs = check.executeQuery()) {
        if (rs.next()) {
            LOGGER.warning("Coleta duplicada detectada - pulando");
            // Marcar como sincronizada no SQLite mesmo assim
            marcarComoSincronizado(recordId);
            return true;
        }
    }
}
```

**Benefício:** Evita duplicação de coletas no PostgreSQL.

---

## 📊 CHECKLIST DE VALIDAÇÃO PRÉ-SINCRONIZAÇÃO

Antes de sincronizar cada coleta, validar:

- [ ] `id_participante` > 0 ✅ Implementado
- [ ] `id_inventario` > 0 ✅ Implementado
- [ ] `id_patrimonio` > 0 ou NULL (se sem etiqueta) ✅ Implementado
- [ ] `data_coleta` é válida ✅ Implementado
- [ ] Patrimônio existe no PostgreSQL ⚠️ Não implementado
- [ ] Inventário existe no PostgreSQL ⚠️ Não implementado
- [ ] Participante existe no PostgreSQL ⚠️ Não implementado
- [ ] Coleta não é duplicada ⚠️ Não implementado

---

## 🧪 SCRIPT DE TESTE COMPLETO

```sql
-- 1. Verificar coletas pendentes no SQLite
SELECT COUNT(*) as pendentes 
FROM local_coleta 
WHERE sync_status = 'PENDING';

-- 2. Verificar se IDs são válidos
SELECT 
    id,
    id_participante,
    id_inventario,
    id_patrimonio,
    numero_patrimonio,
    CASE 
        WHEN id_participante = 0 THEN '❌ INVÁLIDO'
        WHEN id_participante IS NULL THEN '❌ NULL'
        ELSE '✅ OK'
    END as status_participante
FROM local_coleta 
WHERE sync_status = 'PENDING';

-- 3. Verificar timestamps
SELECT 
    id,
    data_coleta,
    typeof(data_coleta) as tipo,
    length(data_coleta) as tamanho
FROM local_coleta 
WHERE sync_status = 'PENDING'
LIMIT 5;
```

---

## 🎯 PRIORIDADES DE CORREÇÃO

### 🔴 CRÍTICO (Fazer AGORA)
1. ✅ Corrigir `id_participante = 0` → **FEITO**
2. ⚠️ Adicionar validação de duplicatas
3. ⚠️ Adicionar validação de referências estrangeiras

### ⚠️ IMPORTANTE (Fazer em breve)
4. Adicionar campos faltantes no SQLite
5. Melhorar logs de conversão de timestamp
6. Adicionar retry automático em falhas de rede

### 💡 MELHORIA (Fazer quando possível)
7. Adicionar métricas de performance (tempo_coleta, etc.)
8. Adicionar geolocalização (latitude/longitude)
9. Adicionar categoria para itens sem etiqueta

---

## 📝 NOTA SOBRE MCP

**Para verificar o banco PostgreSQL, sempre use o MCP:**

```sql
-- Verificar estrutura de tabela
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'tabela_coleta'
ORDER BY ordinal_position;

-- Verificar constraints
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'tabela_coleta';

-- Verificar foreign keys
SELECT 
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name = 'tabela_coleta' 
AND tc.constraint_type = 'FOREIGN KEY';
```

---

**Data da Análise:** 26/11/2025  
**Ferramenta:** MCP PostgreSQL  
**Status:** ✅ Análise Completa  
**Próxima Ação:** Implementar validações críticas
