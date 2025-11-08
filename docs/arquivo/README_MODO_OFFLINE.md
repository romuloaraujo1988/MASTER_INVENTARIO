# Modo Offline - Sistema de Inventário

## Visão Geral

O modo offline permite que o Sistema de Inventário funcione sem conexão com a internet, armazenando dados localmente em um banco SQLite e sincronizando automaticamente quando a conexão for restaurada.

## Funcionalidades

### 🔄 Sincronização Automática
- Detecção automática de conectividade
- Sincronização bidirecional entre SQLite local e PostgreSQL remoto
- Resolução inteligente de conflitos
- Sincronização agendada configurável

### 💾 Armazenamento Local
- Banco de dados SQLite para persistência offline
- Tabelas espelho das entidades principais
- Controle de metadados de sincronização
- Sistema de logs offline

### 🎯 Interface do Usuário
- Indicador visual de status de conexão
- Botão de sincronização rápida
- Tela dedicada para gerenciamento de sincronização
- Configurações de modo offline

## Configuração

### Arquivo de Configuração

O modo offline é configurado através do arquivo `src/main/resources/offline.properties`:

```properties
# Configurações Gerais
offline.enabled=true
offline.database.path=./data/offline.db

# Conectividade
connectivity.check.interval.seconds=30
connectivity.test.url=https://www.google.com
connectivity.timeout.seconds=5

# Sincronização
sync.auto.enabled=true
sync.interval.minutes=15
sync.conflict.resolution=TIMESTAMP_WINS
```

### Estratégias de Resolução de Conflitos

1. **TIMESTAMP_WINS**: O registro mais recente prevalece
2. **LOCAL_WINS**: Dados locais sempre prevalecem
3. **REMOTE_WINS**: Dados remotos sempre prevalecem
4. **MANUAL_RESOLUTION**: Usuário resolve conflitos manualmente

## Estrutura do Banco SQLite

### Tabelas de Controle

- **sync_control**: Controla status de sincronização por tabela
- **sync_metadata**: Metadados de sincronização por registro
- **offline_logs**: Logs do sistema offline

### Tabelas Espelho

- **local_patrimonio**: Cópia local da tabela patrimônio
- **local_coleta**: Cópia local da tabela coleta
- **local_inventario**: Cópia local da tabela inventário
- **local_participante_inventario**: Cópia local da tabela participante

### Campos de Controle

Cada tabela espelho possui campos adicionais:

- `is_local_only`: Indica se existe apenas localmente
- `local_created_at`: Data de criação local
- `local_updated_at`: Data de última atualização local
- `sync_status`: Status de sincronização
- `remote_id`: ID no banco remoto após sincronização

## Como Usar

### 1. Inicialização

```bash
# Executar script de criação das tabelas SQLite
sqlite3 ./data/offline.db < criar_tabelas_sqlite_offline.sql
```

### 2. Interface do Usuário

#### Indicador de Status
- **● Online** (Verde): Conectado e sincronizado
- **● Offline** (Vermelho): Sem conexão, dados salvos localmente
- **● Sincronizando...** (Laranja): Processo de sincronização em andamento
- **● Erro** (Magenta): Erro na conectividade ou sincronização

#### Botão de Sincronização Rápida
- Clique no botão **⟳** para forçar sincronização imediata
- Disponível apenas quando online

#### Menu Ferramentas
- **Sincronização Offline** (F9): Abre tela de gerenciamento
- **Configurar Modo Offline**: Abre configurações básicas

### 3. Tela de Sincronização

A tela de sincronização oferece:

- Status detalhado da conexão
- Informações da última sincronização
- Contadores de operações pendentes
- Estatísticas de dados locais
- Barra de progresso da sincronização
- Logs de sincronização
- Controles manuais de sincronização

## Fluxo de Trabalho Offline

### Cenário 1: Perda de Conexão
1. Sistema detecta perda de conectividade
2. Interface atualiza para modo offline
3. Dados são salvos no SQLite local
4. Operações continuam normalmente

### Cenário 2: Restauração de Conexão
1. Sistema detecta conectividade restaurada
2. Sincronização automática é iniciada (se habilitada)
3. Dados locais são enviados para o servidor
4. Dados remotos são baixados e mesclados
5. Conflitos são resolvidos conforme estratégia configurada

### Cenário 3: Sincronização Manual
1. Usuário clica no botão de sincronização
2. Processo de sincronização é executado
3. Progresso é exibido na interface
4. Resultado é reportado ao usuário

## Arquitetura Técnica

### Classes Principais

- **OfflineManager**: Gerenciador central do modo offline
- **ConnectivityManager**: Monitora conectividade de rede
- **DataSynchronizer**: Executa sincronização de dados
- **SQLiteConnection**: Gerencia conexão com SQLite
- **OfflineDAO**: Operações CRUD no banco local
- **OfflineConfigManager**: Gerencia configurações
- **SyncFrame**: Interface de gerenciamento de sincronização

### Fluxo de Sincronização

```
1. ConnectivityManager detecta mudança de status
2. OfflineManager é notificado
3. DataSynchronizer é acionado (se online)
4. Dados pendentes são identificados
5. Upload de dados locais para servidor
6. Download de dados atualizados do servidor
7. Resolução de conflitos
8. Atualização de metadados de sincronização
9. Notificação de conclusão
```

## Monitoramento e Logs

### Logs do Sistema
- Logs são armazenados na tabela `offline_logs`
- Níveis: DEBUG, INFO, WARN, ERROR
- Componentes rastreados: ConnectivityManager, DataSynchronizer, etc.

### Métricas de Performance
- Tempo de sincronização
- Quantidade de registros sincronizados
- Taxa de conflitos
- Frequência de perda de conectividade

## Troubleshooting

### Problemas Comuns

#### 1. Banco SQLite não criado
```bash
# Verificar se o diretório existe
mkdir -p ./data

# Executar script de criação
sqlite3 ./data/offline.db < criar_tabelas_sqlite_offline.sql
```

#### 2. Sincronização não funciona
- Verificar conectividade de rede
- Verificar configurações do banco PostgreSQL
- Consultar logs na tabela `offline_logs`

#### 3. Conflitos de dados
- Revisar estratégia de resolução de conflitos
- Usar resolução manual se necessário
- Verificar timestamps dos registros

### Comandos de Diagnóstico

```sql
-- Verificar status de sincronização
SELECT * FROM v_sync_stats;

-- Listar dados pendentes
SELECT * FROM v_pending_sync;

-- Verificar logs recentes
SELECT * FROM offline_logs 
ORDER BY created_at DESC 
LIMIT 50;

-- Estatísticas de conflitos
SELECT entity_type, COUNT(*) as conflicts
FROM sync_metadata 
WHERE sync_status = 'CONFLICT'
GROUP BY entity_type;
```

## Limitações

### Funcionais
- Sincronização apenas de entidades principais
- Resolução de conflitos limitada a estratégias predefinidas
- Não suporta sincronização de arquivos/imagens

### Técnicas
- Banco SQLite limitado a um usuário por vez
- Performance pode degradar com grandes volumes de dados
- Requer espaço em disco para armazenamento local

## Roadmap

### Versão Futura
- [ ] Sincronização de arquivos e imagens
- [ ] Suporte a múltiplos usuários offline
- [ ] Compressão de dados para otimizar transferência
- [ ] Sincronização incremental mais eficiente
- [ ] Interface web para monitoramento remoto
- [ ] Backup automático de dados offline

## Suporte

Para suporte técnico ou dúvidas sobre o modo offline:

1. Consulte os logs do sistema
2. Verifique as configurações em `offline.properties`
3. Execute comandos de diagnóstico SQL
4. Consulte a documentação técnica do projeto

---

**Nota**: O modo offline é uma funcionalidade avançada que requer configuração adequada do ambiente e compreensão dos conceitos de sincronização de dados.