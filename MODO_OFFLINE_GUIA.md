# 🔌 Guia do Modo Offline - Sistema de Inventário

## 📋 Visão Geral

O Sistema de Inventário possui um **modo offline completo** que permite trabalhar sem conexão com o banco de dados PostgreSQL. Todos os dados são armazenados localmente em um banco SQLite.

---

## 🚀 Como Ativar o Modo Offline

### Opção 1: Via Interface (Recomendado)

1. Abra o sistema
2. Na tela de login, clique no botão **"Modo Offline"**
3. O sistema criará automaticamente o banco SQLite se necessário
4. Faça login com as credenciais padrão:
   - **Usuário:** `admin`
   - **Senha:** `admin123`

### Opção 2: Criar Banco Manualmente

#### Windows (PowerShell - Recomendado)
```powershell
.\criar-banco-offline.ps1
```

#### Windows (CMD)
```cmd
criar-banco-offline.bat
```

---

## 📦 Requisitos

### SQLite3
O sistema precisa do `sqlite3.exe` para criar o banco de dados.

**Download:** https://www.sqlite.org/download.html

**Instalação:**
1. Baixe o arquivo `sqlite-tools-win32-x86-*.zip`
2. Extraia o `sqlite3.exe`
3. Coloque na pasta do projeto OU adicione ao PATH do Windows

---

## 🗄️ Estrutura do Banco Offline

### Localização
```
data/inventario.db
```

### Tabelas Principais

#### Tabelas de Controle Offline
- `sync_control` - Controle de sincronização
- `sync_metadata` - Metadados de sincronização
- `offline_logs` - Logs do sistema offline

#### Tabelas Espelho (Prefixo `local_`)
- `local_patrimonio` - Patrimônios locais
- `local_coleta` - Coletas locais
- `local_inventario` - Inventários locais
- `local_participante_inventario` - Participantes locais

#### Tabelas Compatíveis (Nomes PostgreSQL)
- `TABELA_INVENTARIO` - Inventários
- `SALA` - Salas
- `PATRIMONIO` - Patrimônios
- `RESPONSAVEL` - Responsáveis
- `COLETA` - Coletas
- `SALA_INVENTARIO` - Vínculo sala-inventário
- `PARTICIPANTE_INVENTARIO` - Participantes do inventário
- `USUARIO` - Usuários do sistema

---

## 👤 Dados Iniciais

### Usuário Padrão
- **Login:** `admin`
- **Senha:** `admin123`
- **Perfil:** Administrador
- **Email:** admin@ifmt.edu.br

### Inventário Padrão
- **Nome:** Inventário Offline 2024
- **Status:** EM_ANDAMENTO
- **Ano:** 2024

### Salas de Exemplo
1. Sala 101 - Sala de Aula (Bloco A, 1º Andar)
2. Sala 102 - Sala de Aula (Bloco A, 1º Andar)
3. Sala 201 - Laboratório de Informática (Bloco B, 2º Andar)

### Patrimônios de Exemplo
1. 000001 - Cadeira Giratória (Sala 101)
2. 000002 - Mesa de Escritório (Sala 101)
3. 000003 - Computador Desktop (Sala 201)
4. 000004 - Projetor Multimídia (Sala 102)
5. 000005 - Quadro Branco (Sala 101)

---

## 🔄 Sincronização

### Quando Voltar Online

1. Clique no botão **"Modo Online"** na tela de login
2. O sistema tentará conectar ao PostgreSQL
3. Se bem-sucedido, você poderá sincronizar os dados offline

### Sincronização Automática (Futuro)

O sistema está preparado para sincronização automática:
- Detecta mudanças locais
- Marca registros como `PENDING`
- Sincroniza quando conexão estiver disponível
- Resolve conflitos automaticamente

---

## ⚠️ Limitações do Modo Offline

### Funcionalidades Limitadas
- ❌ Não sincroniza automaticamente com servidor
- ❌ Não compartilha dados com outros usuários em tempo real
- ❌ Relatórios podem ter dados desatualizados

### Funcionalidades Disponíveis
- ✅ Cadastro de patrimônios
- ✅ Registro de coletas
- ✅ Gestão de salas
- ✅ Gestão de responsáveis
- ✅ Consultas e relatórios locais
- ✅ Todos os recursos da interface

---

## 🛠️ Solução de Problemas

### Erro: "SQLITE_ERROR: no such table"

**Causa:** Banco de dados não foi criado ou está corrompido

**Solução:**
```powershell
# Remover banco existente
Remove-Item data\inventario.db -Force

# Recriar banco
.\criar-banco-offline.ps1
```

### Erro: "sqlite3.exe não encontrado"

**Causa:** SQLite3 não está instalado

**Solução:**
1. Baixe de https://www.sqlite.org/download.html
2. Extraia `sqlite3.exe` na pasta do projeto
3. Execute novamente o script

### Erro: "Não foi possível conectar ao banco"

**Causa:** Arquivo do banco está bloqueado ou corrompido

**Solução:**
```powershell
# Fechar todas as instâncias do sistema
# Remover banco
Remove-Item data\inventario.db -Force

# Recriar
.\criar-banco-offline.ps1
```

---

## 📊 Verificar Banco de Dados

### Via PowerShell
```powershell
# Listar tabelas
sqlite3 data\inventario.db ".tables"

# Contar registros
sqlite3 data\inventario.db "SELECT COUNT(*) FROM USUARIO;"
sqlite3 data\inventario.db "SELECT COUNT(*) FROM PATRIMONIO;"
sqlite3 data\inventario.db "SELECT COUNT(*) FROM COLETA;"

# Ver estrutura de uma tabela
sqlite3 data\inventario.db ".schema TABELA_INVENTARIO"
```

### Via Interface Gráfica
Use ferramentas como:
- **DB Browser for SQLite** (https://sqlitebrowser.org/)
- **SQLiteStudio** (https://sqlitestudio.pl/)

---

## 🔐 Segurança

### Senhas
- Senhas são armazenadas com hash BCrypt
- Mesmo offline, as senhas estão protegidas

### Backup
Recomendamos fazer backup regular do arquivo:
```
data\inventario.db
```

---

## 📝 Logs

### Localização dos Logs
```
logs/sistema-inventario.log
```

### Logs Offline Específicos
Armazenados na tabela `offline_logs`:
```sql
SELECT * FROM offline_logs ORDER BY created_at DESC LIMIT 10;
```

---

## 🆘 Suporte

Em caso de problemas:
1. Verifique os logs em `logs/sistema-inventario.log`
2. Consulte a tabela `offline_logs` no banco SQLite
3. Entre em contato com o suporte técnico

---

**Última atualização:** 20/11/2024  
**Versão do Sistema:** 2.0  
**Modo Offline:** Totalmente funcional ✅
