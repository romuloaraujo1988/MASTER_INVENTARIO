# 📊 Implementação das Tabelas do Sistema de Inventário

## 🎯 Objetivo
Este documento fornece instruções completas para implementar as tabelas do sistema de inventário de patrimônio do IFMT, suportando PostgreSQL, SQLite e MySQL.

## 📁 Arquivos Criados

| Arquivo | Descrição |
|---------|-----------|
| `IMPLEMENTAR_TABELAS_COMPLETO.sql` | Script SQL completo com todas as tabelas, views e dados iniciais |
| `INSTRUCOES_IMPLEMENTACAO_TABELAS.md` | Instruções detalhadas por banco de dados |
| `configuracao_banco.py` | Configurador de conexão com banco de dados |
| `implementar_tabelas.py` | Script para implementar tabelas automaticamente |
| `testar_implementacao.py` | Script para testar a implementação |

## 🚀 Como Implementar

### Opção 1: Implementação Automática (Recomendado)

#### 1. Configurar conexão
```bash
# Menu interativo de configuração
python configuracao_banco.py --menu
```

#### 2. Implementar tabelas
```bash
# PostgreSQL
python implementar_tabelas.py postgresql

# SQLite
python implementar_tabelas.py sqlite

# MySQL
python implementar_tabelas.py mysql
```

#### 3. Testar implementação
```bash
python testar_implementacao.py
```

### Opção 2: Implementação Manual

#### PostgreSQL
```bash
# Conectar ao PostgreSQL
psql -h localhost -U postgres -d sispatrimonio

# Executar script
\i IMPLEMENTAR_TABELAS_COMPLETO.sql
```

#### SQLite
```bash
# Criar banco SQLite
sqlite3 inventario_offline.db < IMPLEMENTAR_TABELAS_COMPLETO.sql
```

#### MySQL
```bash
# Conectar ao MySQL
mysql -h localhost -u root -p

-- Criar banco e executar script
CREATE DATABASE IF NOT EXISTS sispatrimonio;
USE sispatrimonio;
SOURCE IMPLEMENTAR_TABELAS_COMPLETO.sql;
```

## 📋 Estrutura das Tabelas

### Tabelas Principais
1. **TABELA_SETOR** - Gerenciamento de setores/departamentos
2. **TABELA_RESPONSAVEL** - Cadastro de responsáveis
3. **TABELA_SALA** - Localização física dos patrimônios
4. **TABELA_USUARIO** - Sistema de autenticação
5. **TABELA_INVENTARIO** - Controle de inventários
6. **TABELA_PATRIMONIO** - Cadastro de bens patrimoniais
7. **TABELA_COLETA** - Registro de coletas de inventário
8. **TABELA_COLETOR** - Dispositivos de coleta

### Tabelas Offline (SQLite)
- **local_patrimonio** - Cache local de patrimônios
- **local_coleta** - Coletas offline
- **local_inventario** - Inventários offline
- **sync_control** - Controle de sincronização
- **sync_metadata** - Metadados de sincronização

### Views
- **vw_patrimonios_completo** - Vista completa de patrimônios
- **vw_coletas_completo** - Vista completa de coletas
- **v_pending_sync** - Itens pendentes de sincronização
- **v_sync_stats** - Estatísticas de sincronização

## 🔧 Requisitos

### Python
```bash
pip install psycopg2-binary  # PostgreSQL
pip install mysql-connector-python  # MySQL
# SQLite já vem com Python
```

### Banco de Dados
- **PostgreSQL**: Versão 12 ou superior
- **MySQL**: Versão 8.0 ou superior
- **SQLite**: Versão 3.0 ou superior

## 📊 Dados Iniciais

O script inclui:
- 5 setores de exemplo
- 10 responsáveis de exemplo
- 20 patrimônios de exemplo
- 3 inventários de exemplo
- 1 usuário administrador padrão

## 🧪 Testes

### Testes de Conexão
```python
# Testar conexão PostgreSQL
python -c "import psycopg2; psycopg2.connect(host='localhost', database='sispatrimonio', user='postgres', password='postgres')"

# Testar conexão SQLite
python -c "import sqlite3; sqlite3.connect('inventario_offline.db')"
```

### Verificação de Tabelas
```sql
-- PostgreSQL
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- SQLite
SELECT name FROM sqlite_master WHERE type='table';

-- MySQL
SHOW TABLES;
```

## 🚨 Solução de Problemas

### Erro: "Tabela já existe"
- Use `DROP TABLE IF EXISTS` antes de criar
- Ou use `CREATE TABLE IF NOT EXISTS`

### Erro: Permissão negada
- PostgreSQL: Configure pg_hba.conf
- MySQL: Configure usuário com permissões adequadas

### Erro: Porta já em uso
- PostgreSQL: Porta 5432
- MySQL: Porta 3306

## 📋 Comandos Úteis

### PostgreSQL
```bash
# Iniciar PostgreSQL
sudo systemctl start postgresql

# Criar usuário
sudo -u postgres createuser -P sispatrimonio

# Criar banco
sudo -u postgres createdb -O sispatrimonio sispatrimonio
```

### MySQL
```bash
# Iniciar MySQL
sudo systemctl start mysql

# Criar usuário
mysql -u root -p
CREATE USER 'sispatrimonio'@'localhost' IDENTIFIED BY 'senha';
GRANT ALL PRIVILEGES ON sispatrimonio.* TO 'sispatrimonio'@'localhost';
```

### SQLite
```bash
# Criar banco
sqlite3 inventario_offline.db

# Backup
cp inventario_offline.db inventario_offline_backup_$(date +%Y%m%d_%H%M%S).db
```

## 🔄 Sincronização Offline

### Configuração
1. Execute o script para criar tabelas SQLite
2. Configure a sincronização no sistema
3. Teste a conexão offline

### Monitoramento
```bash
# Verificar itens pendentes
python testar_implementacao.py

# Ver logs de sincronização
SELECT * FROM offline_logs ORDER BY timestamp DESC;
```

## 📞 Suporte

Se encontrar problemas:
1. Verifique os logs de erro
2. Execute os testes de conexão
3. Confira as permissões do banco
4. Consulte a documentação específica do banco

## 📝 Próximos Passos

1. Implementar as tabelas usando um dos métodos acima
2. Executar os testes de verificação
3. Configurar o sistema para usar o banco de dados
4. Iniciar o cadastro de patrimônios

---

**Data de criação**: $(date +%d/%m/%Y)
**Versão**: 1.0.0
**Autor**: Sistema de Inventário IFMT