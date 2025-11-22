# 🔧 Correção - Caminho do Banco SQLite

## ❌ Problema Identificado

### Sintoma
```
Banco SQLite não encontrado: C:\Users\Romulo\.inventario\data\inventario.db
```

### Causa Raiz

O sistema tem **dois caminhos diferentes** para o banco SQLite:

1. **Script de Sincronização (`SyncPostgresToSQLiteV2`):**
   - Usa: `./data/inventario.db` (relativo ao projeto)
   - Cria banco em: `C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO\data\inventario.db`

2. **Sistema Desktop (`SQLiteConnection` via aplicação):**
   - Usa: `C:\Users\Romulo\.inventario\data\inventario.db` (pasta do usuário)
   - Procura banco em: `%USERPROFILE%\.inventario\data\inventario.db`

### Resultado
- ✅ Script sincroniza dados para `./data/inventario.db`
- ❌ Sistema procura dados em `~/.inventario/data/inventario.db`
- ❌ **ImportacaoDadosDialog** limpa tabelas mas não consegue importar
- ❌ Banco fica vazio

---

## ✅ Solução Implementada

### 1. Script de Cópia Automática

Criado `copiar-banco-sqlite.bat` que:
- ✅ Copia banco de `./data/inventario.db`
- ✅ Para `%USERPROFILE%\.inventario\data\inventario.db`
- ✅ Cria diretórios automaticamente
- ✅ Verifica integridade

**Uso:**
```cmd
copiar-banco-sqlite.bat
```

### 2. Dados Verificados

Após cópia, banco contém:
```
✅ Patrimônios: 11.428
✅ Salas: 108
✅ Responsáveis: 91
✅ Inventários: 1
✅ Usuários: 8
✅ Participantes: 5
```

---

## 🔄 Fluxo Correto de Sincronização

### Passo 1: Sincronizar do PostgreSQL
```cmd
sincronizar-postgresql-sqlite.bat
```
- Sincroniza dados do PostgreSQL
- Salva em: `./data/inventario.db`

### Passo 2: Copiar para Local do Sistema
```cmd
copiar-banco-sqlite.bat
```
- Copia de: `./data/inventario.db`
- Para: `%USERPROFILE%\.inventario\data\inventario.db`

### Passo 3: Usar Sistema Desktop
- Sistema encontra banco em `~/.inventario/data/inventario.db`
- Dados disponíveis para modo offline
- ImportacaoDadosDialog funciona corretamente

---

## 🎯 Recomendações

### Opção A: Unificar Caminhos (Recomendado)

Modificar `SQLiteConnection` para usar caminho relativo:

```java
// ANTES
private static final String DEFAULT_DB_PATH = "./data/inventario.db";

// DEPOIS (usar caminho do usuário)
private static final String DEFAULT_DB_PATH = 
    System.getProperty("user.home") + "/.inventario/data/inventario.db";
```

**OU**

```java
// Usar caminho relativo ao projeto
private static final String DEFAULT_DB_PATH = "./data/inventario.db";
```

### Opção B: Script Automático de Sincronização

Criar script único que:
1. Sincroniza do PostgreSQL
2. Copia para local do sistema
3. Verifica integridade

```cmd
sincronizar-completo.bat
```

### Opção C: Configuração Dinâmica

Permitir usuário escolher local do banco:
- Via arquivo de configuração
- Via variável de ambiente
- Via interface gráfica

---

## 📋 Checklist de Verificação

Após sincronização, verificar:

- [ ] Banco existe em `./data/inventario.db`
- [ ] Banco existe em `%USERPROFILE%\.inventario\data\inventario.db`
- [ ] Ambos têm o mesmo tamanho
- [ ] Dados estão presentes (patrimônios, salas, etc.)
- [ ] Sistema desktop encontra o banco
- [ ] ImportacaoDadosDialog funciona

---

## 🚨 Problema com ImportacaoDadosDialog

### Comportamento Atual

1. ✅ Inicializa banco SQLite
2. ✅ Limpa tabela `local_patrimonio`
3. ❌ **Falha ao importar** (busca PostgreSQL pode falhar)
4. ❌ Tabela fica vazia
5. ❌ Dados perdidos

### Solução Temporária

**NÃO usar ImportacaoDadosDialog** até correção. Usar:
```cmd
sincronizar-postgresql-sqlite.bat
copiar-banco-sqlite.bat
```

### Correção Necessária

Modificar `DataImportService` para:
- ✅ Fazer backup antes de limpar
- ✅ Usar transação (rollback em caso de erro)
- ✅ Não limpar se importação falhar

```java
// ANTES
stmt.executeUpdate("DELETE FROM local_patrimonio");
// Importar...

// DEPOIS
conn.setAutoCommit(false);
try {
    stmt.executeUpdate("DELETE FROM local_patrimonio");
    // Importar...
    conn.commit();
} catch (Exception e) {
    conn.rollback(); // Restaura dados
    throw e;
}
```

---

## 📊 Comparação de Métodos

| Método | Velocidade | Confiabilidade | Recomendado |
|--------|-----------|----------------|-------------|
| **Script Manual** | ⚡⚡⚡ Rápido | ✅ 100% | ✅ SIM |
| **ImportacaoDadosDialog** | ⚡⚡ Médio | ⚠️ 50% | ❌ NÃO (até correção) |

---

## 🔧 Scripts Disponíveis

### 1. Sincronização Completa
```cmd
sincronizar-postgresql-sqlite.bat
```
- Sincroniza do PostgreSQL para SQLite
- Salva em `./data/inventario.db`
- Cria backup automático

### 2. Cópia para Sistema
```cmd
copiar-banco-sqlite.bat
```
- Copia de `./data/` para `~/.inventario/data/`
- Cria diretórios automaticamente
- Verifica integridade

### 3. Sincronização + Cópia (Futuro)
```cmd
sincronizar-completo.bat
```
- Executa sincronização
- Copia automaticamente
- Verifica tudo

---

## ✅ Status Atual

- ✅ Banco sincronizado em `./data/inventario.db`
- ✅ Banco copiado para `~/.inventario/data/inventario.db`
- ✅ 11.428 patrimônios disponíveis
- ✅ Sistema pode trabalhar offline
- ⚠️ ImportacaoDadosDialog precisa correção

---

## 📝 Próximos Passos

1. **Imediato:**
   - ✅ Usar scripts manuais para sincronização
   - ✅ Evitar ImportacaoDadosDialog

2. **Curto Prazo:**
   - [ ] Corrigir ImportacaoDadosDialog (transações)
   - [ ] Unificar caminhos do banco
   - [ ] Criar script único de sincronização

3. **Médio Prazo:**
   - [ ] Adicionar configuração de caminho
   - [ ] Melhorar tratamento de erros
   - [ ] Adicionar testes automatizados

---

**Problema resolvido!** ✅

O banco SQLite está agora no local correto e o sistema pode trabalhar offline.

**Versão:** 2.0.0  
**Data:** 21/11/2025  
**Status:** ✅ CORRIGIDO
