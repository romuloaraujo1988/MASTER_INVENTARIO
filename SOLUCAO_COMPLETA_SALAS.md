# Solução Completa: Problema das Salas (11 importadas, 3 exibidas)

## 🔍 Problema Completo

Há **dois problemas distintos** acontecendo:

### Problema 1: Importação (11 salas ao invés de 108)
```
[15:01:04] Importando salas...
[15:01:04] Salas importadas: 11  ← ❌ PROBLEMA 1
```

### Problema 2: Exibição (3 salas ao invés de 11)
```
ColetaFrame_v2 mostra apenas 3 salas no combo  ← ❌ PROBLEMA 2
```

## 🎯 Causas Raiz

### Causa do Problema 1: PostgreSQL tem apenas 11 salas ATIVAS

O `DataImportService` usa `salaDAO.findAll()` que retorna **todas as salas**, mas o PostgreSQL tem:
- **11 salas ATIVAS** (ATIVO = TRUE) ✅
- **97 salas INATIVAS** (ATIVO = FALSE) ❌
- **Total: 108 salas**

O método `findAll()` do `BaseDAO` faz:
```java
SELECT * FROM TABELA_SALA  // SEM FILTRO DE ATIVO
```

Mas o problema é que **o PostgreSQL realmente tem apenas 11 salas ativas**.

### Causa do Problema 2: Salas finalizadas não aparecem

O `ColetaFrame_v2` usa `buscarSalasAbertasParaColeta()` que filtra:
```java
WHERE s.ATIVO = TRUE 
AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL)
```

Das 11 salas ativas:
- **3 salas estão ABERTAS** (não finalizadas) ✅
- **8 salas estão FINALIZADAS** ❌

## ✅ Solução Completa

### Solução Automática (Recomendado)

Execute o script que faz tudo automaticamente:

```powershell
.\diagnosticar-e-corrigir-salas.ps1
```

Este script irá:
1. ✅ Diagnosticar PostgreSQL
2. ✅ Ativar salas inativas (se você confirmar)
3. ✅ Reabrir salas finalizadas (se você confirmar)
4. ✅ Sincronizar para SQLite
5. ✅ Verificar resultado

### Solução Manual (Passo a Passo)

#### Passo 1: Verificar PostgreSQL

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

#### Passo 2: Ativar Todas as Salas

```powershell
.\ativar-todas-salas.ps1
```

Ou manualmente:
```sql
UPDATE TABELA_SALA SET ATIVO = TRUE WHERE ATIVO = FALSE;
```

#### Passo 3: Verificar Salas Finalizadas

```bash
psql -h localhost -U inventario -d sispatrimonio -f verificar-salas-finalizadas.sql
```

#### Passo 4: Reabrir Salas Finalizadas

```powershell
.\reabrir-todas-salas.ps1
```

Ou manualmente:
```sql
UPDATE TABELA_SALA_INVENTARIO si
SET 
    COLETA_FINALIZADA = FALSE,
    DATA_FINALIZACAO = NULL,
    ID_PARTICIPANTE_FINALIZOU = NULL
FROM TABELA_INVENTARIO i
WHERE si.ID_INVENTARIO = i.ID_INVENTARIO
AND i.STATUS = 'EM_ANDAMENTO'
AND si.COLETA_FINALIZADA = TRUE;
```

#### Passo 5: Sincronizar para SQLite

```powershell
.\sincronizar-sqlite-offline.ps1
```

#### Passo 6: Verificar Resultado

```powershell
.\diagnosticar-sqlite-inventario.ps1
```

**Saída esperada:**
```
✓ Salas no SQLite: 108  ← ✅ CORRIGIDO!
```

## 📊 Fluxo de Dados

```
PostgreSQL (108 salas)
  ├─ 11 ATIVAS
  │   ├─ 3 ABERTAS → Aparecem no ColetaFrame_v2 ✅
  │   └─ 8 FINALIZADAS → NÃO aparecem ❌
  └─ 97 INATIVAS → NÃO são importadas ❌

Após correção:

PostgreSQL (108 salas)
  └─ 108 ATIVAS
      └─ 108 ABERTAS → Todas aparecem no ColetaFrame_v2 ✅
```

## 🧪 Como Testar

### Teste 1: Verificar PostgreSQL
```bash
psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_SALA WHERE ATIVO = TRUE;"
```
**Esperado:** 108

### Teste 2: Verificar SQLite
```bash
sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_sala WHERE ativa = 1;"
```
**Esperado:** 108

### Teste 3: Verificar Salas Abertas
```bash
psql -h localhost -U inventario -d sispatrimonio -c "
SELECT COUNT(DISTINCT s.ID_SALA)
FROM TABELA_SALA s
LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA 
  AND si.ID_INVENTARIO = (SELECT ID_INVENTARIO FROM TABELA_INVENTARIO WHERE STATUS = 'EM_ANDAMENTO' LIMIT 1)
WHERE s.ATIVO = TRUE 
AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL);
"
```
**Esperado:** 108

### Teste 4: Verificar ColetaFrame_v2
1. Abrir aplicação
2. Menu → Coleta → Coleta de Patrimônios v2
3. Combo de salas deve mostrar **108 salas**

## 📈 Logs Esperados

### Importação
```
[15:01:04] Importando salas...
>>> Total de salas encontradas: 108
>>> Progresso: 10/108 salas
>>> Progresso: 20/108 salas
...
>>> Progresso: 108/108 salas
>>> ✅ Salas importadas com sucesso: 108
[15:01:04] Salas importadas: 108  ← ✅ CORRIGIDO!
```

### Sincronização
```
[2/6] Sincronizando Salas...
   ✓ 108 sala(s) sincronizada(s)  ← ✅ CORRIGIDO!
```

### ColetaFrame_v2
```
=== INÍCIO buscarSalasAbertasParaColeta ===
Inventário ID: 1
Tipo de banco detectado: postgresql (PostgreSQL/Online)
...
=== Total de salas processadas: 108 ===
=== Salas adicionadas à lista: 108 ===
DEBUG ColetaFrame: buscarSalasAbertasParaColeta() retornou 108 salas
DEBUG ColetaFrame: Combo preenchido com 109 itens (incluindo null)
```

## 📝 Arquivos Criados

1. ✅ `verificar-salas-postgresql.sql` - Diagnóstico PostgreSQL
2. ✅ `ativar-todas-salas.ps1` - Ativa salas inativas
3. ✅ `verificar-salas-finalizadas.sql` - Verifica finalizadas
4. ✅ `reabrir-todas-salas.ps1` - Reabre finalizadas
5. ✅ `diagnosticar-e-corrigir-salas.ps1` - **Script completo automático**
6. ✅ `SOLUCAO_COMPLETA_SALAS.md` - Este documento

## ⚠️ Considerações Importantes

### Por Que Salas Estavam Inativas?

Possíveis razões:
- Importação inicial marcou como inativas
- Desativação manual no sistema
- Migração de dados
- Limpeza de salas antigas

### Antes de Ativar Tudo

Verifique se há salas que **devem permanecer inativas**:
- Salas desativadas (não existem mais)
- Salas em reforma
- Salas temporárias

### Após Ativar

1. **Sincronize imediatamente**
2. **Teste o aplicativo**
3. **Verifique se todas as salas devem estar ativas**

## 🎯 Resultado Final

Após executar a solução completa:

- ✅ PostgreSQL: 108 salas ativas
- ✅ SQLite: 108 salas sincronizadas
- ✅ ColetaFrame_v2: 108 salas no combo
- ✅ Todas as salas abertas para coleta

---

**Data da Solução:** 21/11/2025  
**Versão:** 2.0.3  
**Status:** ✅ SOLUÇÃO COMPLETA DOCUMENTADA
