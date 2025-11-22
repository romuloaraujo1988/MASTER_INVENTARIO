# 📋 Instruções de Sincronização SQLite

## 🎯 Objetivo

Sincronizar dados do PostgreSQL para o banco SQLite offline, permitindo que o sistema funcione sem conexão com o servidor.

---

## 🚀 Como Executar a Sincronização

### Opção 1: Script Batch (Windows)

```bash
sincronizar-sqlite-offline.bat
```

### Opção 2: Script PowerShell

```powershell
.\sincronizar-sqlite-offline.ps1
```

### Opção 3: Maven Direto

```bash
mvn exec:java -Dexec.mainClass="com.inventario.offline.SyncPostgresToSQLite"
```

---

## 📊 O que é Sincronizado

A sincronização copia as seguintes tabelas do PostgreSQL para o SQLite:

1. **TABELA_INVENTARIO** - Inventários ativos e históricos
2. **SALA** - Todas as salas cadastradas
3. **RESPONSAVEL** - Responsáveis pelos patrimônios
4. **PATRIMONIO** - Todos os patrimônios cadastrados
5. **USUARIO** - Usuários do sistema
6. **PARTICIPANTE_INVENTARIO** - Participantes dos inventários
7. **SALA_INVENTARIO** - Vinculação de salas aos inventários

---

## ✅ Pré-requisitos

### 1. PostgreSQL Acessível
- O servidor PostgreSQL deve estar rodando
- As credenciais em `configuracao_banco.json` devem estar corretas

### 2. Banco SQLite
- Será criado automaticamente em `data/inventario.db`
- Se já existir, será atualizado

### 3. Maven Configurado
- Maven deve estar no PATH
- Dependências devem estar instaladas

---

## 📝 Processo de Sincronização

```
[1/7] Sincronizando Inventários...
   ✓ X inventário(s) sincronizado(s)

[2/7] Sincronizando Salas...
   ✓ X sala(s) sincronizada(s)

[3/7] Sincronizando Responsáveis...
   ✓ X responsável(is) sincronizado(s)

[4/7] Sincronizando Patrimônios...
   ✓ X patrimônio(s) sincronizado(s)

[5/7] Sincronizando Usuários...
   ✓ X usuário(s) sincronizado(s)

[6/7] Sincronizando Participantes...
   ✓ X participante(s) sincronizado(s)

[7/7] Sincronizando Salas do Inventário...
   ✓ X sala(s) de inventário sincronizada(s)

✅ Todas as tabelas sincronizadas!
```

---

## 🔍 Verificar Sincronização

### Via SQLite CLI

```bash
sqlite3 data/inventario.db

# Verificar inventários
SELECT * FROM TABELA_INVENTARIO;

# Verificar salas
SELECT COUNT(*) FROM SALA;

# Verificar patrimônios
SELECT COUNT(*) FROM PATRIMONIO;

# Sair
.quit
```

### Via Aplicação

1. Abrir o sistema
2. Desconectar da rede (modo offline)
3. Tentar realizar uma coleta
4. Se funcionar, a sincronização está OK

---

## ⚠️ Problemas Comuns

### Erro: "PostgreSQL não acessível"

**Solução:**
```bash
# Verificar se o PostgreSQL está rodando
psql -h localhost -U inventario -d sispatrimonio -c "SELECT 1"

# Verificar configuracao_banco.json
cat %USERPROFILE%\.inventario\configuracao_banco.json
```

### Erro: "Tabela não existe"

**Solução:**
```bash
# Recriar banco SQLite
del data\inventario.db
sqlite3 data\inventario.db < sql\criar_tabelas_sqlite_offline.sql
```

### Erro: "Error parsing time stamp"

**Solução:**
- Este erro foi corrigido nas últimas atualizações
- Certifique-se de estar usando a versão mais recente do código
- Os timestamps agora são tratados de forma robusta

---

## 🔄 Quando Sincronizar

### Sincronização Obrigatória

- **Primeira vez** que usar o sistema
- **Após importar** novos patrimônios
- **Após criar** novo inventário
- **Antes de trabalhar offline**

### Sincronização Recomendada

- **Diariamente** se houver mudanças frequentes
- **Semanalmente** para manutenção
- **Após grandes alterações** no banco

---

## 📂 Estrutura de Arquivos

```
MASTER_INVENTARIO/
├── data/
│   └── inventario.db              # Banco SQLite offline
├── sql/
│   └── criar_tabelas_sqlite_offline.sql  # Script de criação
├── src/main/java/com/inventario/offline/
│   └── SyncPostgresToSQLite.java  # Classe de sincronização
├── sincronizar-sqlite-offline.bat # Script Windows
└── sincronizar-sqlite-offline.ps1 # Script PowerShell
```

---

## 🛠️ Manutenção

### Limpar Banco SQLite

```bash
# Deletar banco antigo
del data\inventario.db

# Recriar do zero
sqlite3 data\inventario.db < sql\criar_tabelas_sqlite_offline.sql

# Sincronizar novamente
sincronizar-sqlite-offline.bat
```

### Verificar Integridade

```sql
-- No SQLite
PRAGMA integrity_check;

-- Verificar tamanho
SELECT 
    'TABELA_INVENTARIO' as tabela, COUNT(*) as registros 
FROM TABELA_INVENTARIO
UNION ALL
SELECT 'SALA', COUNT(*) FROM SALA
UNION ALL
SELECT 'PATRIMONIO', COUNT(*) FROM PATRIMONIO
UNION ALL
SELECT 'USUARIO', COUNT(*) FROM USUARIO;
```

---

## 📊 Logs e Debug

### Habilitar Logs Detalhados

Editar `SyncPostgresToSQLite.java`:

```java
// Adicionar no início de cada método
System.out.println("DEBUG: Iniciando sincronização de [TABELA]");
System.out.println("DEBUG: Encontrados " + lista.size() + " registros");
```

### Verificar Logs do Sistema

```bash
# Logs do Maven
mvn exec:java ... > sync.log 2>&1

# Ver logs
type sync.log
```

---

## ✅ Checklist de Validação

Após sincronizar, verificar:

- [ ] Arquivo `data/inventario.db` existe
- [ ] Tamanho do arquivo > 0 bytes
- [ ] Tabelas contêm dados
- [ ] Inventário ativo está presente
- [ ] Salas estão listadas
- [ ] Patrimônios estão disponíveis
- [ ] Sistema funciona offline

---

## 🆘 Suporte

Se encontrar problemas:

1. Verificar logs de erro
2. Confirmar que PostgreSQL está acessível
3. Verificar permissões de arquivo
4. Recriar banco SQLite do zero
5. Consultar documentação adicional

---

**Última atualização:** 21/11/2024  
**Versão:** 1.0.0  
**Status:** ✅ Pronto para uso
