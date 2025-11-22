# ⚠️ Problema: Tabelas Antigas vs Novas

## ❌ Erro Identificado

```
Erro ao verificar dados locais: [SQLITE_ERROR] SQL error or missing database 
(no such table: usuario)
```

## 🔍 Causa Raiz

O sistema tem **DUAS classes de sincronização** com nomes de tabelas diferentes:

### 1. SyncPostgresToSQLite (ANTIGA - ❌ NÃO USAR)

**Tabelas que usa:**
- `USUARIO` (sem prefixo)
- `RESPONSAVEL` (sem prefixo)
- `PATRIMONIO` (sem prefixo)
- `TABELA_INVENTARIO` (nome antigo)
- `SALA` (sem prefixo)

**Problema:** Essas tabelas **NÃO EXISTEM** no banco SQLite atual!

### 2. SyncPostgresToSQLiteV2 (NOVA - ✅ USAR)

**Tabelas que usa:**
- `local_usuario` (com prefixo)
- `local_responsavel` (com prefixo)
- `local_patrimonio` (com prefixo)
- `local_inventario` (com prefixo)
- `local_sala` (com prefixo)

**Correto:** Essas tabelas **EXISTEM** e estão corretas!

---

## 📊 Comparação das Tabelas

| Tabela Antiga (❌) | Tabela Nova (✅) | Status |
|-------------------|------------------|--------|
| `USUARIO` | `local_usuario` | ✅ Existe |
| `RESPONSAVEL` | `local_responsavel` | ✅ Existe |
| `PATRIMONIO` | `local_patrimonio` | ✅ Existe |
| `TABELA_INVENTARIO` | `local_inventario` | ✅ Existe |
| `SALA` | `local_sala` | ✅ Existe |
| `PARTICIPANTE_INVENTARIO` | `local_participante_inventario` | ✅ Existe |

---

## ✅ Solução

### 1. Usar SEMPRE a Classe V2

**Scripts atualizados:**
- ✅ `sincronizar-postgresql-sqlite.bat` → Usa `SyncPostgresToSQLiteV2`
- ✅ `sincronizar-completo.bat` → Usa `SyncPostgresToSQLiteV2`

### 2. Verificar Qual Classe Está Sendo Usada

```bash
# Verificar no script
type sincronizar-postgresql-sqlite.bat | find "SyncPostgresToSQLite"
```

Deve mostrar:
```
-Dexec.mainClass="com.inventario.offline.SyncPostgresToSQLiteV2"
```

### 3. Remover ou Deprecar Classe Antiga

**Opção A:** Renomear para indicar que está obsoleta
```
SyncPostgresToSQLite.java → SyncPostgresToSQLite_OBSOLETO.java
```

**Opção B:** Adicionar aviso na classe antiga
```java
@Deprecated
public class SyncPostgresToSQLite {
    public static void main(String[] args) {
        System.err.println("ERRO: Esta classe está obsoleta!");
        System.err.println("Use: SyncPostgresToSQLiteV2");
        System.exit(1);
    }
}
```

---

## 🔧 Como Corrigir Banco Atual

Se você executou a classe antiga e o banco ficou com tabelas erradas:

### Passo 1: Verificar Tabelas Existentes
```cmd
sqlite3 data\inventario.db ".tables"
```

**Esperado (correto):**
```
local_coleta
local_inventario
local_participante_inventario
local_patrimonio
local_responsavel
local_sala
local_usuario
```

**Se aparecer (errado):**
```
USUARIO
RESPONSAVEL
PATRIMONIO
TABELA_INVENTARIO
SALA
```

### Passo 2: Recriar Banco Correto
```cmd
# Deletar banco antigo
del data\inventario.db

# Sincronizar com classe correta
sincronizar-postgresql-sqlite.bat

# Copiar para local do sistema
copiar-banco-sqlite.bat
```

---

## 📝 Checklist de Verificação

Após sincronização, verificar:

- [ ] Script usa `SyncPostgresToSQLiteV2` (não `SyncPostgresToSQLite`)
- [ ] Tabelas têm prefixo `local_` (ex: `local_usuario`)
- [ ] Comando `.tables` mostra tabelas corretas
- [ ] Dados foram sincronizados (11.428 patrimônios)
- [ ] Sistema desktop encontra o banco
- [ ] Não há erros "no such table"

---

## 🎯 Comandos de Verificação

### Verificar Tabelas no Banco
```cmd
sqlite3 data\inventario.db ".tables"
```

### Verificar Dados
```cmd
sqlite3 data\inventario.db "SELECT COUNT(*) FROM local_patrimonio;"
sqlite3 data\inventario.db "SELECT COUNT(*) FROM local_usuario;"
```

### Verificar Estrutura de Tabela
```cmd
sqlite3 data\inventario.db ".schema local_usuario"
```

---

## 🚨 Sinais de Problema

Se você ver estes erros, está usando a classe ERRADA:

```
❌ no such table: usuario
❌ no such table: USUARIO
❌ no such table: RESPONSAVEL
❌ no such table: PATRIMONIO
❌ no such table: TABELA_INVENTARIO
```

**Solução:** Use `SyncPostgresToSQLiteV2`!

---

## ✅ Sinais de Sucesso

Se a sincronização está correta, você verá:

```
✅ [1/6] Sincronizando Inventários...
✅    ✓ 1 inventário(s) sincronizado(s)
✅ [2/6] Sincronizando Salas...
✅    ✓ 108 sala(s) sincronizada(s)
✅ [3/6] Sincronizando Responsáveis...
✅    ✓ 91 responsável(is) sincronizado(s)
✅ [4/6] Sincronizando Patrimônios...
✅    ✓ 11428 patrimônio(s) sincronizado(s)
✅ [5/6] Sincronizando Usuários...
✅    ✓ 8 usuário(s) sincronizado(s)
✅ [6/6] Sincronizando Participantes...
✅    ✓ 5 participante(s) sincronizado(s)
```

---

## 📋 Ações Recomendadas

### Imediato
1. ✅ Verificar qual classe o script está usando
2. ✅ Garantir que é `SyncPostgresToSQLiteV2`
3. ✅ Resincronizar se necessário

### Curto Prazo
1. [ ] Deprecar `SyncPostgresToSQLite` antiga
2. [ ] Adicionar aviso de erro na classe antiga
3. [ ] Atualizar documentação

### Médio Prazo
1. [ ] Remover classe antiga completamente
2. [ ] Renomear V2 para nome principal
3. [ ] Adicionar testes automatizados

---

## 🎉 Resumo

**Problema:** Sistema usava classe antiga com nomes de tabelas errados  
**Solução:** Usar `SyncPostgresToSQLiteV2` que usa tabelas corretas (`local_*`)  
**Status:** ✅ Scripts atualizados e funcionando

---

**Versão:** 2.0.0  
**Data:** 21/11/2025  
**Status:** ✅ CORRIGIDO
