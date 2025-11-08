# Instruções de Implementação das Tabelas do Sistema

## 📋 Visão Geral
Este documento fornece instruções completas para implementar as tabelas do sistema de inventário de patrimônio IFMT.

## 🗂️ Arquivos Disponíveis

### Arquivos Principais
- `IMPLEMENTAR_TABELAS_COMPLETO.sql` - Script completo para PostgreSQL e SQLite
- `executar_criacao_tabelas.sql` - Script principal para PostgreSQL
- `criar_tabelas_sqlite_offline.sql` - Script para SQLite (modo offline)

### Scripts Individuais
- `script_tabela_setor.sql` - Tabela de setores
- `script_tabela_responsavel.sql` - Tabela de responsáveis
- `script_tabela_inventario.sql` - Tabela de inventários
- `script_tabela_patrimonio.sql` - Tabela de patrimônios
- `script_tabela_coleta.sql` - Tabela de coletas
- `criar_tabela_usuario.sql` - Tabela de usuários

## 🚀 Como Implementar

### 1. PostgreSQL (Sistema Principal)

#### Opção A: Script Completo
```bash
# Conectar ao PostgreSQL e executar o script completo
psql -h localhost -U postgres -d sispatrimonio -f IMPLEMENTAR_TABELAS_COMPLETO.sql
```

#### Opção B: Scripts Individuais
```bash
# Executar na ordem correta (importante por dependências)
psql -h localhost -U postgres -d sispatrimonio -f script_tabela_setor.sql
psql -h localhost -U postgres -d sispatrimonio -f script_tabela_responsavel.sql
psql -h localhost -U postgres -d sispatrimonio -f script_tabela_inventario.sql
psql -h localhost -U postgres -d sispatrimonio -f criar_tabela_usuario.sql
psql -h localhost -U postgres -d sispatrimonio -f executar_criacao_tabelas.sql
```

### 2. SQLite (Modo Offline)

#### Criar banco de dados offline
```bash
# Criar novo banco SQLite
sqlite3 inventario_offline.db < IMPLEMENTAR_TABELAS_COMPLETO.sql

# Ou usar o script específico
sqlite3 inventario_offline.db < criar_tabelas_sqlite_offline.sql
```

## 📊 Estrutura das Tabelas

### TABELA_SETOR
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID | SERIAL | Identificador único |
| NOME | VARCHAR(255) | Nome do setor |
| DESCRICAO | TEXT | Descrição detalhada |
| RESPONSAVEL_SETOR | VARCHAR(255) | Pessoa responsável |
| ATIVO | BOOLEAN | Status ativo/inativo |

### TABELA_RESPONSAVEL
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID | SERIAL | Identificador único |
| NOME | VARCHAR(255) | Nome completo |
| CPF | VARCHAR(14) | CPF único |
| EMAIL | VARCHAR(255) | Email de contato |
| TELEFONE | VARCHAR(20) | Telefone de contato |
| CARGO | VARCHAR(100) | Função/cargo |
| ID_SETOR | INTEGER | Setor de pertencimento |

### TABELA_SALA
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID_SALA | SERIAL | Identificador único |
| DESCRICAO | VARCHAR(255) | Descrição da sala |
| NUMERO_SALA | VARCHAR(20) | Número identificador |
| ANDAR | INTEGER | Andar do prédio |
| BLOCO | VARCHAR(10) | Bloco do prédio |
| ID_SETOR | INTEGER | Setor de pertencimento |
| CAPACIDADE | INTEGER | Capacidade da sala |
| AREA_M2 | DECIMAL(8,2) | Área em metros quadrados |

### TABELA_INVENTARIO
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID | SERIAL | Identificador único |
| NOME | VARCHAR(255) | Nome do inventário |
| ANO | INTEGER | Ano de referência |
| DATA_INICIO | DATE | Data de início |
| DATA_FIM | DATE | Data de conclusão |
| STATUS_INVENTARIO | VARCHAR(50) | Status (PLANEJADO, EM_ANDAMENTO, CONCLUIDO) |
| PERCENTUAL_CONCLUSAO | DECIMAL(5,2) | Percentual de conclusão |

### TABELA_PATRIMONIO
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID | SERIAL | Identificador único |
| NUMERO | VARCHAR(50) | Número único do patrimônio |
| DESCRICAO | TEXT | Descrição detalhada |
| MARCA | VARCHAR(100) | Marca do item |
| MODELO | VARCHAR(100) | Modelo do item |
| VALOR_AQUISICAO | DECIMAL(15,2) | Valor de aquisição |
| VALOR_DEPRECIADO | DECIMAL(15,2) | Valor depreciado |
| ID_RESPONSAVEL | INTEGER | Responsável pelo item |
| ID_SALA | INTEGER | Localização atual |
| ESTADO_CONSERVACAO | VARCHAR(50) | Estado de conservação |

### TABELA_COLETA
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID | SERIAL | Identificador único |
| ID_INVENTARIO | INTEGER | Referência ao inventário |
| ID_PATRIMONIO | INTEGER | Referência ao patrimônio |
| ID_USUARIO | INTEGER | Usuário que realizou a coleta |
| DATA_COLETA | TIMESTAMP | Data e hora da coleta |
| STATUS_COLETA | VARCHAR(50) | Status da coleta |
| DIVERGENCIA | BOOLEAN | Indica se houve divergência |
| LOCALIZACAO_ATUAL | VARCHAR(255) | Localização cadastrada |
| LOCALIZACAO_ENCONTRADA | VARCHAR(255) | Localização encontrada |

### TABELA_USUARIO
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ID | SERIAL | Identificador único |
| LOGIN | VARCHAR(50) | Login único |
| SENHA_HASH | VARCHAR(255) | Hash da senha |
| NOME_COMPLETO | VARCHAR(150) | Nome completo |
| EMAIL | VARCHAR(100) | Email único |
| PERFIL | VARCHAR(20) | ADMIN, SUPERVISOR, COLETOR, CONSULTA |
| ATIVO | CHAR(1) | Status ativo/inativo |

## 🔧 Configuração do Banco de Dados

### PostgreSQL
```sql
-- Criar banco de dados
CREATE DATABASE sispatrimonio;

-- Criar usuário (opcional)
CREATE USER inventario_user WITH PASSWORD 'senha_segura';
GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO inventario_user;
```

### SQLite
```bash
# Não requer configuração prévia
# O banco é criado automaticamente ao executar o script
```

## 📋 Verificação da Implementação

### Verificar tabelas criadas (PostgreSQL)
```sql
-- Listar todas as tabelas
\dt

-- Verificar estrutura de uma tabela específica
\d TABELA_PATRIMONIO

-- Contar registros em cada tabela
SELECT 'TABELA_SETOR' as tabela, COUNT(*) as total FROM TABELA_SETOR
UNION ALL
SELECT 'TABELA_RESPONSAVEL' as tabela, COUNT(*) as total FROM TABELA_RESPONSAVEL
UNION ALL
SELECT 'TABELA_SALA' as tabela, COUNT(*) as total FROM TABELA_SALA
UNION ALL
SELECT 'TABELA_INVENTARIO' as tabela, COUNT(*) as total FROM TABELA_INVENTARIO
UNION ALL
SELECT 'TABELA_PATRIMONIO' as tabela, COUNT(*) as total FROM TABELA_PATRIMONIO
UNION ALL
SELECT 'TABELA_COLETA' as tabela, COUNT(*) as total FROM TABELA_COLETA;
```

### Verificar tabelas criadas (SQLite)
```sql
-- Listar todas as tabelas
.tables

-- Verificar estrutura de uma tabela específica
.schema local_patrimonio

-- Contar registros em cada tabela
SELECT 'local_patrimonio' as tabela, COUNT(*) as total FROM local_patrimonio
UNION ALL
SELECT 'local_coleta' as tabela, COUNT(*) as total FROM local_coleta
UNION ALL
SELECT 'local_inventario' as tabela, COUNT(*) as total FROM local_inventario;
```

## ⚠️ Observações Importantes

### Ordem de Execução
1. **PostgreSQL**: As tabelas devem ser criadas na ordem correta devido às dependências de chaves estrangeiras
2. **SQLite**: As tabelas offline podem ser criadas em qualquer ordem

### Dados Iniciais
- Todos os scripts incluem dados iniciais para teste
- Usuário admin padrão: login `admin`, senha `admin123`

### Segurança
- As senhas são armazenadas com hash bcrypt
- Os usuários são criados com status ativo por padrão
- Os logs de sincronização são mantidos para auditoria

### Manutenção
- Índices são criados automaticamente para melhor performance
- Triggers atualizam timestamps automaticamente
- Views facilitam consultas complexas

## 🆘 Suporte

### Problemas Comuns
1. **Erro de permissão PostgreSQL**: Verifique as permissões do usuário
2. **Erro de dependência**: Execute os scripts na ordem correta
3. **SQLite não encontrado**: Instale o SQLite no sistema

### Comandos Úteis
```bash
# PostgreSQL - Verificar conexão
psql -h localhost -U postgres -d sispatrimonio -c "SELECT version();"

# SQLite - Verificar versão
sqlite3 --version

# PostgreSQL - Backup
pg_dump -h localhost -U postgres sispatrimonio > backup_inventario.sql

# SQLite - Backup
sqlite3 inventario_offline.db .dump > backup_inventario_offline.sql
```