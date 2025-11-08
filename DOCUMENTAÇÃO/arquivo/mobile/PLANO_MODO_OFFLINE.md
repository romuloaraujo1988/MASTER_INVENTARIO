# Plano de Implementação - Modo Offline
## Sistema de Inventário IFMT

### 1. Visão Geral

O modo offline permitirá que o sistema funcione sem conexão com a internet, utilizando um banco de dados SQLite local para armazenar dados temporariamente e sincronizando com o PostgreSQL quando a conexão for restabelecida.

### 2. Arquitetura Proposta

#### 2.1 Componentes Principais

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Interface     │    │   Modo Online   │    │   Modo Offline  │
│   Usuário       │◄──►│   PostgreSQL    │    │   SQLite Local  │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                └────────────────────────┘
                                    Sincronização
```

#### 2.2 Fluxo de Dados

1. **Detecção de Conectividade**: Verificação automática da conexão
2. **Modo Automático**: Alternância transparente entre online/offline
3. **Armazenamento Local**: SQLite para dados temporários
4. **Sincronização**: Upload/download quando conexão disponível
5. **Resolução de Conflitos**: Estratégias para dados divergentes

### 3. Estrutura de Banco SQLite

#### 3.1 Tabelas Principais

```sql
-- Tabela de controle de sincronização
CREATE TABLE sync_control (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    record_id INTEGER NOT NULL,
    operation TEXT NOT NULL, -- INSERT, UPDATE, DELETE
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    synced BOOLEAN DEFAULT FALSE,
    conflict_resolved BOOLEAN DEFAULT FALSE,
    data_json TEXT -- Dados serializados
);

-- Tabelas espelho das principais entidades
CREATE TABLE local_patrimonio (
    id INTEGER PRIMARY KEY,
    numero TEXT,
    descricao TEXT,
    -- outros campos...
    sync_status TEXT DEFAULT 'PENDING', -- PENDING, SYNCED, CONFLICT
    last_modified DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE local_coleta (
    id INTEGER PRIMARY KEY,
    id_patrimonio INTEGER,
    id_inventario INTEGER,
    -- outros campos...
    sync_status TEXT DEFAULT 'PENDING',
    last_modified DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE local_inventario (
    id INTEGER PRIMARY KEY,
    nome TEXT,
    data_inicio DATE,
    -- outros campos...
    sync_status TEXT DEFAULT 'PENDING',
    last_modified DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.2 Metadados de Sincronização

```sql
CREATE TABLE sync_metadata (
    key TEXT PRIMARY KEY,
    value TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Exemplos de metadados
INSERT INTO sync_metadata VALUES 
('last_sync_timestamp', '2024-01-01 00:00:00', CURRENT_TIMESTAMP),
('sync_mode', 'AUTO', CURRENT_TIMESTAMP),
('conflict_resolution_strategy', 'SERVER_WINS', CURRENT_TIMESTAMP);
```

### 4. Classes Java a Implementar

#### 4.1 Gerenciamento de Conectividade

```java
// com.inventario.offline.ConnectivityManager
public class ConnectivityManager {
    private boolean isOnline;
    private List<ConnectivityListener> listeners;
    
    public boolean checkConnection()
    public void startMonitoring()
    public void stopMonitoring()
    public void addListener(ConnectivityListener listener)
}

// com.inventario.offline.ConnectivityListener
public interface ConnectivityListener {
    void onConnectionEstablished();
    void onConnectionLost();
}
```

#### 4.2 Banco SQLite Local

```java
// com.inventario.offline.SQLiteConnection
public class SQLiteConnection {
    private static final String DB_NAME = "inventario_offline.db";
    private Connection connection;
    
    public Connection getConnection()
    public void initializeDatabase()
    public void createTables()
}

// com.inventario.offline.OfflineDAO
public abstract class OfflineDAO<T> {
    protected SQLiteConnection sqliteConn;
    
    public abstract void saveLocally(T entity);
    public abstract List<T> getPendingSync();
    public abstract void markAsSynced(int id);
}
```

#### 4.3 Sincronização

```java
// com.inventario.offline.SyncManager
public class SyncManager {
    private ConnectivityManager connectivityManager;
    private List<SyncableDAO> syncableDAOs;
    
    public void startAutoSync()
    public void performManualSync()
    public SyncResult syncTable(String tableName)
    public void resolveConflicts()
}

// com.inventario.offline.SyncResult
public class SyncResult {
    private int recordsUploaded;
    private int recordsDownloaded;
    private int conflicts;
    private List<String> errors;
}
```

#### 4.4 Interface Unificada

```java
// com.inventario.offline.HybridDAO
public abstract class HybridDAO<T> {
    protected DatabaseConnection onlineConn;
    protected SQLiteConnection offlineConn;
    protected ConnectivityManager connectivityManager;
    
    public T save(T entity) {
        if (connectivityManager.isOnline()) {
            return saveOnline(entity);
        } else {
            return saveOffline(entity);
        }
    }
    
    protected abstract T saveOnline(T entity);
    protected abstract T saveOffline(T entity);
}
```

### 5. Estratégias de Sincronização

#### 5.1 Tipos de Sincronização

1. **Automática**: Executada quando conexão é detectada
2. **Manual**: Iniciada pelo usuário
3. **Agendada**: Em intervalos regulares
4. **Por Demanda**: Para operações específicas

#### 5.2 Resolução de Conflitos

```java
public enum ConflictResolutionStrategy {
    SERVER_WINS,     // Servidor sempre prevalece
    CLIENT_WINS,     // Cliente sempre prevalece
    TIMESTAMP_WINS,  // Mais recente prevalece
    MANUAL_RESOLVE   // Usuário decide
}
```

#### 5.3 Priorização de Dados

1. **Alta Prioridade**: Coletas, Inventários ativos
2. **Média Prioridade**: Patrimônios, Usuários
3. **Baixa Prioridade**: Logs, Configurações

### 6. Interface do Usuário

#### 6.1 Indicadores Visuais

```java
// Componente de status de conectividade
public class ConnectivityStatusPanel extends JPanel {
    private JLabel statusLabel;
    private JButton syncButton;
    private JProgressBar syncProgress;
    
    public void updateStatus(boolean online)
    public void showSyncProgress(int percentage)
    public void showSyncResult(SyncResult result)
}
```

#### 6.2 Tela de Sincronização

```java
// com.inventario.view.SyncFrame
public class SyncFrame extends JFrame {
    private JTable conflictsTable;
    private JButton resolveButton;
    private JButton syncNowButton;
    
    public void showConflicts(List<SyncConflict> conflicts)
    public void showSyncHistory()
}
```

### 7. Configurações

#### 7.1 Arquivo de Configuração

```properties
# offline.properties
offline.enabled=true
offline.db.path=./data/inventario_offline.db
offline.sync.auto=true
offline.sync.interval=300000
offline.conflict.strategy=TIMESTAMP_WINS
offline.max.pending.records=10000
```

#### 7.2 Classe de Configuração

```java
public class OfflineConfig {
    private boolean enabled;
    private String dbPath;
    private boolean autoSync;
    private long syncInterval;
    private ConflictResolutionStrategy conflictStrategy;
    
    public static OfflineConfig load()
    public void save()
}
```

### 8. Implementação por Fases

#### Fase 1 (1 semana): Infraestrutura Base
- [ ] Classe ConnectivityManager
- [ ] SQLiteConnection e estrutura básica
- [ ] Configurações offline
- [ ] Indicador visual de status

#### Fase 2 (1 semana): Sincronização Básica
- [ ] SyncManager básico
- [ ] HybridDAO para Coleta
- [ ] Sincronização manual
- [ ] Tela de sincronização

#### Fase 3 (1 semana): Funcionalidades Avançadas
- [ ] Sincronização automática
- [ ] Resolução de conflitos
- [ ] Otimizações de performance
- [ ] Logs e auditoria

#### Fase 4 (1 semana): Testes e Refinamentos
- [ ] Testes de conectividade
- [ ] Testes de sincronização
- [ ] Testes de conflitos
- [ ] Documentação

### 9. Considerações Técnicas

#### 9.1 Performance
- Índices otimizados no SQLite
- Sincronização em lotes
- Compressão de dados
- Cache inteligente

#### 9.2 Segurança
- Criptografia de dados locais
- Validação de integridade
- Autenticação para sincronização
- Logs de auditoria

#### 9.3 Robustez
- Recuperação de falhas
- Validação de dados
- Backup automático
- Rollback em caso de erro

### 10. Casos de Uso

#### 10.1 Cenário Típico
1. Usuário inicia coleta em local sem internet
2. Sistema detecta modo offline automaticamente
3. Dados são salvos no SQLite local
4. Usuário retorna a local com internet
5. Sistema sincroniza automaticamente
6. Conflitos são resolvidos conforme estratégia

#### 10.2 Cenário de Conflito
1. Mesmo patrimônio coletado offline e online
2. Sistema detecta conflito na sincronização
3. Aplica estratégia de resolução configurada
4. Registra resolução para auditoria

### 11. Métricas e Monitoramento

```java
public class SyncMetrics {
    private int totalSyncs;
    private int successfulSyncs;
    private int failedSyncs;
    private int conflictsResolved;
    private long averageSyncTime;
    
    public void recordSync(SyncResult result)
    public SyncReport generateReport()
}
```

### 12. Dependências Adicionais

```xml
<!-- SQLite JDBC Driver -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>

<!-- JSON para serialização -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

---

**Este plano fornece uma base sólida para implementar o modo offline no Sistema de Inventário IFMT, garantindo operação contínua mesmo sem conectividade e sincronização robusta quando a conexão for restabelecida.**