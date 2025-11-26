# Design Document - Correção e Melhoria da Sincronização SQLite ↔ PostgreSQL

## Overview

Este documento descreve o design técnico para corrigir e melhorar o sistema crítico de sincronização de dados entre SQLite (modo offline) e PostgreSQL (servidor) no SIHCP. O design foca em garantir integridade de dados, atomicidade de transações, tratamento robusto de erros e rastreabilidade completa de todas as operações.

### Objetivos Principais

1. **Integridade de Dados**: Garantir que dados sincronizados sejam válidos e consistentes
2. **Atomicidade**: Operações de sincronização devem ser all-or-nothing
3. **Rastreabilidade**: Logs detalhados de todas as operações para auditoria
4. **Resiliência**: Recuperação automática de falhas temporárias
5. **Performance**: Sincronização incremental e eficiente
6. **Segurança**: Backups automáticos antes de modificações

## Architecture

### Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Sistema Desktop (Java Swing)              │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           SyncCoordinator (Orquestrador)              │  │
│  │  - Coordena todo o processo de sincronização          │  │
│  │  - Gerencia transações e rollback                     │  │
│  │  - Controla ordem de sincronização                    │  │
│  └────────┬─────────────────────────────────────────────┘  │
│           │                                                  │
│           ├──► ValidationService (Validação)                │
│           │    - Valida dados antes de sincronizar          │
│           │    - Verifica integridade referencial           │
│           │                                                  │
│           ├──► BackupService (Backup)                       │
│           │    - Cria backups automáticos                   │
│           │    - Gerencia restauração                       │
│           │                                                  │
│           ├──► ConflictResolver (Resolução de Conflitos)    │
│           │    - Detecta conflitos de dados                 │
│           │    - Permite resolução manual                   │
│           │                                                  │
│           ├──► RetryManager (Gerenciamento de Retry)        │
│           │    - Implementa backoff exponencial             │
│           │    - Controla tentativas de retry               │
│           │                                                  │
│           ├──► IntegrityChecker (Verificação de Integridade)│
│           │    - Verifica consistência pós-sincronização    │
│           │    - Calcula checksums                          │
│           │                                                  │
│           └──► AuditLogger (Logging de Auditoria)           │
│                - Registra todas as operações                │
│                - Mantém histórico de sincronizações         │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Data Access Layer                        │  │
│  │                                                        │  │
│  │  SQLiteManager          PostgreSQLManager             │  │
│  │  - CRUD SQLite          - CRUD PostgreSQL             │  │
│  │  - Transações           - Transações                  │  │
│  │  - Queries              - Queries                     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ HTTP/REST
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Servidor API (Spring Boot)                  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         SyncController (REST Endpoints)               │  │
│  │  POST /api/sync/coletas/batch                         │  │
│  │  POST /api/sync/validate                              │  │
│  │  GET  /api/sync/health                                │  │
│  └────────┬─────────────────────────────────────────────┘  │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         SyncService (Lógica de Negócio)               │  │
│  │  - Valida dados recebidos                             │  │
│  │  - Processa sincronização em lote                     │  │
│  │  - Gerencia transações                                │  │
│  └────────┬─────────────────────────────────────────────┘  │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Repository Layer (JPA)                        │  │
│  │  - ColetaRepository                                   │  │
│  │  - PatrimonioRepository                               │  │
│  │  - InventarioRepository                               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```


## Components and Interfaces

### 1. SyncCoordinator (Orquestrador Principal)

**Responsabilidade**: Coordenar todo o processo de sincronização, gerenciar transações e controlar ordem de execução.

```java
public class SyncCoordinator {
    private final ValidationService validationService;
    private final BackupService backupService;
    private final ConflictResolver conflictResolver;
    private final RetryManager retryManager;
    private final IntegrityChecker integrityChecker;
    private final AuditLogger auditLogger;
    private final SQLiteManager sqliteManager;
    private final PostgreSQLManager postgresManager;
    
    /**
     * Sincroniza coletas pendentes do SQLite para PostgreSQL
     * @param testMode Se true, apenas simula sem modificar dados
     * @return Resultado da sincronização com estatísticas
     */
    public SyncResult syncColetasPendentes(boolean testMode);
    
    /**
     * Baixa dados do PostgreSQL para SQLite
     * @return Resultado do download com estatísticas
     */
    public DownloadResult downloadDadosServidor();
    
    /**
     * Sincroniza dados específicos com resolução de conflitos
     * @param entityType Tipo de entidade (PATRIMONIO, SALA, RESPONSAVEL)
     * @return Resultado da sincronização
     */
    public SyncResult syncWithConflictResolution(EntityType entityType);
}
```

### 2. ValidationService (Serviço de Validação)

**Responsabilidade**: Validar dados antes de sincronizar para garantir integridade.

```java
public class ValidationService {
    /**
     * Valida coletas pendentes antes de sincronizar
     * @param coletas Lista de coletas a validar
     * @return Resultado da validação com lista de erros
     */
    public ValidationResult validateColetas(List<Coleta> coletas);
    
    /**
     * Valida integridade referencial
     * @param coleta Coleta a validar
     * @return true se todas as FKs são válidas
     */
    public boolean validateReferentialIntegrity(Coleta coleta);
    
    /**
     * Valida formato de timestamps
     * @param timestamp Timestamp a validar
     * @return true se formato é válido
     */
    public boolean validateTimestampFormat(String timestamp);
    
    /**
     * Valida campos obrigatórios
     * @param entity Entidade a validar
     * @return Lista de campos faltantes
     */
    public List<String> validateRequiredFields(Object entity);
}
```

### 3. BackupService (Serviço de Backup)

**Responsabilidade**: Criar e gerenciar backups automáticos do SQLite.

```java
public class BackupService {
    private static final int MAX_BACKUPS = 10;
    private final Path backupDirectory;
    
    /**
     * Cria backup do SQLite antes de sincronização
     * @return Path do arquivo de backup criado
     */
    public Path createBackup();
    
    /**
     * Restaura backup específico
     * @param backupPath Path do backup a restaurar
     * @return true se restauração foi bem-sucedida
     */
    public boolean restoreBackup(Path backupPath);
    
    /**
     * Lista backups disponíveis ordenados por data
     * @return Lista de backups
     */
    public List<BackupInfo> listBackups();
    
    /**
     * Remove backups antigos mantendo apenas os últimos MAX_BACKUPS
     */
    public void cleanOldBackups();
}
```

### 4. ConflictResolver (Resolvedor de Conflitos)

**Responsabilidade**: Detectar e resolver conflitos de dados entre SQLite e PostgreSQL.

```java
public class ConflictResolver {
    /**
     * Detecta conflitos comparando timestamps
     * @param sqliteEntity Entidade do SQLite
     * @param postgresEntity Entidade do PostgreSQL
     * @return Conflito detectado ou null
     */
    public Conflict detectConflict(Object sqliteEntity, Object postgresEntity);
    
    /**
     * Resolve conflito usando estratégia escolhida
     * @param conflict Conflito a resolver
     * @param strategy Estratégia (KEEP_SQLITE, KEEP_POSTGRES, MERGE)
     * @return Entidade resolvida
     */
    public Object resolveConflict(Conflict conflict, ResolutionStrategy strategy);
    
    /**
     * Exibe dialog para resolução manual
     * @param conflict Conflito a resolver
     * @return Estratégia escolhida pelo usuário
     */
    public ResolutionStrategy showConflictDialog(Conflict conflict);
}
```

### 5. RetryManager (Gerenciador de Retry)

**Responsabilidade**: Implementar retry inteligente com backoff exponencial.

```java
public class RetryManager {
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 5000;
    
    /**
     * Executa operação com retry automático
     * @param operation Operação a executar
     * @param retryPolicy Política de retry
     * @return Resultado da operação
     */
    public <T> T executeWithRetry(Callable<T> operation, RetryPolicy retryPolicy);
    
    /**
     * Calcula delay para próxima tentativa usando backoff exponencial
     * @param attemptNumber Número da tentativa atual
     * @return Delay em milissegundos
     */
    public long calculateBackoffDelay(int attemptNumber);
    
    /**
     * Determina se erro é recuperável e deve fazer retry
     * @param exception Exceção ocorrida
     * @return true se deve fazer retry
     */
    public boolean isRetryableError(Exception exception);
}
```

### 6. IntegrityChecker (Verificador de Integridade)

**Responsabilidade**: Verificar consistência dos dados após sincronização.

```java
public class IntegrityChecker {
    /**
     * Verifica integridade após sincronização
     * @param inventarioId ID do inventário sincronizado
     * @return Resultado da verificação
     */
    public IntegrityCheckResult checkPostSyncIntegrity(int inventarioId);
    
    /**
     * Compara totais entre SQLite e PostgreSQL
     * @param inventarioId ID do inventário
     * @return true se totais coincidem
     */
    public boolean compareTotals(int inventarioId);
    
    /**
     * Calcula checksum MD5 dos dados sincronizados
     * @param data Dados a calcular checksum
     * @return Checksum MD5
     */
    public String calculateChecksum(List<?> data);
    
    /**
     * Verifica se há coletas pendentes no SQLite
     * @return Quantidade de coletas pendentes
     */
    public int countPendingColetas();
}
```

### 7. AuditLogger (Logger de Auditoria)

**Responsabilidade**: Registrar todas as operações de sincronização para auditoria.

```java
public class AuditLogger {
    private static final int MAX_LOG_SIZE_MB = 50;
    private static final int LOG_RETENTION_DAYS = 30;
    
    /**
     * Registra início de sincronização
     * @param syncType Tipo de sincronização
     * @param user Usuário que iniciou
     */
    public void logSyncStart(SyncType syncType, String user);
    
    /**
     * Registra processamento de registro individual
     * @param entityType Tipo de entidade
     * @param entityId ID da entidade
     * @param result Resultado do processamento
     */
    public void logRecordProcessed(EntityType entityType, int entityId, ProcessResult result);
    
    /**
     * Registra erro durante sincronização
     * @param error Erro ocorrido
     * @param context Contexto do erro
     */
    public void logError(Exception error, String context);
    
    /**
     * Registra conclusão de sincronização
     * @param stats Estatísticas da sincronização
     */
    public void logSyncComplete(SyncStatistics stats);
    
    /**
     * Exporta logs para análise externa
     * @param outputPath Path do arquivo de saída
     */
    public void exportLogs(Path outputPath);
}
```


## Data Models

### SyncResult (Resultado de Sincronização)

```java
public class SyncResult {
    private boolean success;
    private int totalProcessed;
    private int totalSuccess;
    private int totalFailed;
    private long durationMs;
    private List<SyncError> errors;
    private String checksum;
    private LocalDateTime timestamp;
    
    // Getters, setters, builder
}
```

### ValidationResult (Resultado de Validação)

```java
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;
    private int totalValidated;
    
    public static class ValidationError {
        private String field;
        private String message;
        private Object invalidValue;
    }
}
```

### Conflict (Conflito de Dados)

```java
public class Conflict {
    private EntityType entityType;
    private int entityId;
    private Object sqliteVersion;
    private Object postgresVersion;
    private LocalDateTime sqliteModified;
    private LocalDateTime postgresModified;
    private List<String> conflictingFields;
}
```

### BackupInfo (Informação de Backup)

```java
public class BackupInfo {
    private Path filePath;
    private LocalDateTime createdAt;
    private long sizeBytes;
    private String checksum;
}
```

### IntegrityCheckResult (Resultado de Verificação de Integridade)

```java
public class IntegrityCheckResult {
    private boolean passed;
    private int sqliteCount;
    private int postgresCount;
    private int pendingCount;
    private String checksum;
    private List<String> issues;
}
```

### SyncStatistics (Estatísticas de Sincronização)

```java
public class SyncStatistics {
    private int responsaveisSynced;
    private int salasSynced;
    private int patrimoniosSynced;
    private int coletasSynced;
    private int conflictsResolved;
    private int retries;
    private long totalDurationMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

### RetryPolicy (Política de Retry)

```java
public class RetryPolicy {
    private int maxRetries;
    private long initialDelayMs;
    private double backoffMultiplier;
    private Set<Class<? extends Exception>> retryableExceptions;
    
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, 5000, 2.0, 
            Set.of(SocketTimeoutException.class, ConnectException.class));
    }
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Validação Completa de Dados

*For any* conjunto de coletas pendentes, todas as coletas devem ter patrimônio_id, inventario_id e coletor_id válidos antes de sincronização ser permitida.

**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Validação de Formato de Timestamps

*For any* timestamp em coletas pendentes, o formato deve ser yyyy-MM-dd HH:mm:ss e não pode estar no futuro.

**Validates: Requirements 1.4, 13.7**

### Property 3: Validação de Campos Obrigatórios

*For any* coleta pendente, nenhum campo obrigatório pode conter valor NULL.

**Validates: Requirements 1.5**

### Property 4: Atomicidade de Transação - Sucesso

*For any* coleta inserida com sucesso no PostgreSQL, a coleta deve estar marcada como sincronizada no SQLite.

**Validates: Requirements 2.2, 2.6**

### Property 5: Atomicidade de Transação - Falha

*For any* falha durante sincronização, todas as coletas devem permanecer como pendentes no SQLite e nenhuma deve estar no PostgreSQL.

**Validates: Requirements 2.3, 2.4**

### Property 6: Integridade Referencial - Patrimônio

*For any* coleta a ser sincronizada, o patrimônio referenciado deve existir no PostgreSQL antes da inserção.

**Validates: Requirements 3.1**

### Property 7: Integridade Referencial - Inventário

*For any* coleta a ser sincronizada, o inventário referenciado deve existir no PostgreSQL antes da inserção.

**Validates: Requirements 3.2**

### Property 8: Integridade Referencial - Coletor

*For any* coleta a ser sincronizada, o coletor referenciado deve existir no PostgreSQL antes da inserção.

**Validates: Requirements 3.3**

### Property 9: Ordem de Sincronização de Dependências

*For any* patrimônio que não existe no PostgreSQL mas existe no SQLite, o patrimônio deve ser sincronizado antes de suas coletas.

**Validates: Requirements 3.4, 3.5, 3.6**

### Property 10: Ordem Global de Sincronização

*For any* sincronização completa, a ordem deve ser sempre: responsáveis → salas → patrimônios → coletas.

**Validates: Requirements 3.7**

### Property 11: Resolução de Conflito - SQLite

*For any* conflito onde operador escolhe manter versão SQLite, o PostgreSQL deve ser atualizado com dados do SQLite.

**Validates: Requirements 4.4**

### Property 12: Resolução de Conflito - PostgreSQL

*For any* conflito onde operador escolhe manter versão PostgreSQL, o SQLite deve ser atualizado com dados do PostgreSQL.

**Validates: Requirements 4.5**

### Property 13: Retry com Backoff Exponencial

*For any* falha de conexão, o sistema deve tentar novamente até 3 vezes com delay crescente exponencialmente.

**Validates: Requirements 5.2**

### Property 14: Backup Antes de Modificação

*For any* sincronização iniciada, um backup do SQLite deve existir antes de qualquer modificação ser feita.

**Validates: Requirements 7.1**

### Property 15: Nomenclatura de Backup

*For any* backup criado, o nome deve seguir o formato inventario_backup_yyyyMMdd_HHmmss.db.

**Validates: Requirements 7.2**

### Property 16: Limite de Backups

*For any* momento no tempo, não deve haver mais de 10 backups armazenados.

**Validates: Requirements 7.4**

### Property 17: Restauração de Backup (Round Trip)

*For any* backup criado, restaurá-lo deve resultar no SQLite idêntico ao momento do backup.

**Validates: Requirements 7.6**

### Property 18: Consistência de Totais

*For any* sincronização bem-sucedida, o total de coletas sincronizadas no SQLite deve ser igual ao total no PostgreSQL para o mesmo inventário.

**Validates: Requirements 8.3**

### Property 19: Completude de Sincronização

*For any* sincronização bem-sucedida, não deve haver coletas pendentes no SQLite.

**Validates: Requirements 8.5**

### Property 20: Validação no Servidor - Campos Obrigatórios

*For any* requisição de sincronização recebida pelo servidor, todos os campos obrigatórios devem estar presentes.

**Validates: Requirements 9.1**

### Property 21: Validação no Servidor - Tipos de Dados

*For any* requisição de sincronização recebida pelo servidor, todos os tipos de dados devem estar corretos.

**Validates: Requirements 9.2**

### Property 22: Validação no Servidor - Foreign Keys

*For any* requisição de sincronização recebida pelo servidor, todas as foreign keys devem referenciar registros existentes.

**Validates: Requirements 9.3**

### Property 23: Consistência de Download

*For any* download de dados do PostgreSQL para SQLite, os dados no SQLite devem ser idênticos aos do PostgreSQL.

**Validates: Requirements 10.1, 10.2, 10.3**

### Property 24: Sincronização Incremental

*For any* sincronização incremental, apenas registros com sincronizado = false ou modificados desde última sincronização devem ser enviados.

**Validates: Requirements 12.3**

### Property 25: Atualização de Timestamp de Sincronização

*For any* registro sincronizado com sucesso, o campo data_ultima_sincronizacao deve ser atualizado no SQLite.

**Validates: Requirements 12.4**

### Property 26: Conversão de Timezone para UTC

*For any* timestamp sincronizado do SQLite para PostgreSQL, o valor deve estar em UTC.

**Validates: Requirements 13.1**

### Property 27: Formato ISO 8601

*For any* timestamp sincronizado, o formato deve ser ISO 8601 (yyyy-MM-dd'T'HH:mm:ss'Z').

**Validates: Requirements 13.2**

### Property 28: Interpretação UTC no Servidor

*For any* timestamp recebido pelo servidor, deve ser interpretado como UTC e armazenado no PostgreSQL.

**Validates: Requirements 13.3**

### Property 29: Conversão de Timezone para Local (Round Trip)

*For any* timestamp baixado do PostgreSQL, deve ser convertido de UTC para timezone local do usuário.

**Validates: Requirements 13.4**

### Property 30: Health Check Disponibilidade

*For any* momento em que o serviço de sincronização está operacional, o endpoint /api/sync/health deve retornar HTTP 200.

**Validates: Requirements 14.1, 14.5**


## Error Handling

### Estratégia de Tratamento de Erros

#### 1. Erros de Validação

**Tipo**: Erros detectados antes de iniciar sincronização
**Tratamento**:
- Coletar todos os erros de validação
- Exibir relatório detalhado ao usuário
- Impedir sincronização até correção
- Registrar em log de auditoria

**Exemplo**:
```java
try {
    ValidationResult result = validationService.validateColetas(coletas);
    if (!result.isValid()) {
        showValidationErrorDialog(result.getErrors());
        auditLogger.logValidationFailed(result);
        return SyncResult.validationFailed(result);
    }
} catch (ValidationException e) {
    auditLogger.logError(e, "Validation phase");
    throw new SyncException("Falha na validação", e);
}
```

#### 2. Erros de Rede

**Tipo**: Timeout, conexão recusada, DNS failure
**Tratamento**:
- Retry automático com backoff exponencial
- Máximo 3 tentativas
- Registrar cada tentativa em log
- Exibir progresso ao usuário

**Exemplo**:
```java
RetryPolicy policy = RetryPolicy.defaultPolicy();
try {
    return retryManager.executeWithRetry(() -> {
        return postgresManager.insertColeta(coleta);
    }, policy);
} catch (MaxRetriesExceededException e) {
    auditLogger.logError(e, "Network retry exhausted");
    return SyncResult.networkFailed(e);
}
```

#### 3. Erros de Constraint do Banco

**Tipo**: Violação de FK, unique constraint, check constraint
**Tratamento**:
- Registrar erro detalhado
- Pular registro problemático
- Continuar com próximos registros
- Incluir no relatório final

**Exemplo**:
```java
for (Coleta coleta : coletas) {
    try {
        postgresManager.insertColeta(coleta);
        stats.incrementSuccess();
    } catch (ConstraintViolationException e) {
        auditLogger.logError(e, "Constraint violation for coleta " + coleta.getId());
        stats.addError(new SyncError(coleta.getId(), e.getMessage()));
        // Continua com próxima coleta
    }
}
```

#### 4. Erros de Autenticação

**Tipo**: Token expirado, credenciais inválidas
**Tratamento**:
- Interromper sincronização imediatamente
- Solicitar reautenticação ao usuário
- Não fazer retry automático
- Registrar tentativa de acesso

**Exemplo**:
```java
try {
    postgresManager.authenticate();
} catch (AuthenticationException e) {
    auditLogger.logSecurityEvent("Authentication failed", user);
    showReauthenticationDialog();
    return SyncResult.authenticationFailed();
}
```

#### 5. Erros de Transação

**Tipo**: Deadlock, timeout de transação, rollback
**Tratamento**:
- Executar rollback completo
- Manter dados originais no SQLite
- Registrar estado da transação
- Oferecer tentar novamente

**Exemplo**:
```java
Connection conn = null;
try {
    conn = postgresManager.getConnection();
    conn.setAutoCommit(false);
    
    // Operações de sincronização
    syncColetas(conn, coletas);
    
    conn.commit();
    return SyncResult.success(stats);
    
} catch (SQLException e) {
    if (conn != null) {
        try {
            conn.rollback();
            auditLogger.logTransactionRollback(e);
        } catch (SQLException rollbackEx) {
            auditLogger.logError(rollbackEx, "Rollback failed");
        }
    }
    return SyncResult.transactionFailed(e);
} finally {
    if (conn != null) conn.close();
}
```

#### 6. Erros de Conflito

**Tipo**: Dados modificados em ambos os bancos
**Tratamento**:
- Detectar conflito comparando timestamps
- Exibir dialog de resolução
- Aguardar decisão do usuário
- Aplicar estratégia escolhida
- Registrar resolução

**Exemplo**:
```java
Conflict conflict = conflictResolver.detectConflict(sqliteEntity, postgresEntity);
if (conflict != null) {
    ResolutionStrategy strategy = conflictResolver.showConflictDialog(conflict);
    Object resolved = conflictResolver.resolveConflict(conflict, strategy);
    auditLogger.logConflictResolution(conflict, strategy);
    return resolved;
}
```

### Hierarquia de Exceções

```java
public class SyncException extends Exception {
    private final SyncErrorType errorType;
    private final String context;
}

public enum SyncErrorType {
    VALIDATION_ERROR,
    NETWORK_ERROR,
    CONSTRAINT_VIOLATION,
    AUTHENTICATION_ERROR,
    TRANSACTION_ERROR,
    CONFLICT_ERROR,
    INTEGRITY_ERROR,
    BACKUP_ERROR
}
```

### Recuperação de Erros

#### Estratégia de Recuperação Automática

1. **Erros Temporários** (rede, timeout):
   - Retry automático com backoff
   - Até 3 tentativas
   - Registrar cada tentativa

2. **Erros de Dados** (constraint, validação):
   - Pular registro problemático
   - Continuar com próximos
   - Incluir no relatório

3. **Erros Críticos** (autenticação, transação):
   - Interromper sincronização
   - Executar rollback
   - Solicitar intervenção do usuário

#### Estratégia de Recuperação Manual

1. **Backup e Restauração**:
   - Oferecer restaurar último backup
   - Exibir lista de backups disponíveis
   - Confirmar antes de restaurar

2. **Resolução de Conflitos**:
   - Exibir dados conflitantes lado a lado
   - Permitir escolha manual
   - Permitir mesclagem de campos

3. **Retry Manual**:
   - Botão "Tentar Novamente"
   - Exibir detalhes do erro
   - Permitir ajustar configurações


## Testing Strategy

### Abordagem Dual de Testes

Este projeto utilizará **testes unitários** e **testes baseados em propriedades (PBT)** de forma complementar:

- **Testes Unitários**: Verificam exemplos específicos, casos extremos e condições de erro
- **Testes de Propriedade**: Verificam propriedades universais que devem valer para todas as entradas
- **Juntos**: Fornecem cobertura abrangente - testes unitários capturam bugs concretos, testes de propriedade verificam corretude geral

### Framework de Property-Based Testing

**Framework Escolhido**: JUnit-Quickcheck (https://pholser.github.io/junit-quickcheck/)

**Justificativa**:
- Integração nativa com JUnit 5
- Suporte a geradores customizados
- Configuração de número de iterações
- Shrinking automático de falhas

**Configuração**:
```xml
<dependency>
    <groupId>com.pholser</groupId>
    <artifactId>junit-quickcheck-core</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.pholser</groupId>
    <artifactId>junit-quickcheck-generators</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
```

**Configuração de Iterações**: Cada teste de propriedade executará **mínimo 100 iterações** para garantir cobertura adequada.

### Testes de Propriedade

Cada teste de propriedade deve ser marcado com comentário explícito referenciando a propriedade do design:

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 1: Validação Completa de Dados
 */
@Property(trials = 100)
public void todasColetasDevemTerIdsValidos(@From(ColetaGenerator.class) List<Coleta> coletas) {
    // Teste da propriedade
}
```

#### Property Test 1: Validação Completa de Dados

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 1: Validação Completa de Dados
 */
@Property(trials = 100)
public void todasColetasDevemTerIdsValidos(@From(ColetaGenerator.class) List<Coleta> coletas) {
    ValidationService validator = new ValidationService();
    ValidationResult result = validator.validateColetas(coletas);
    
    // Se validação passou, todas as coletas devem ter IDs válidos
    if (result.isValid()) {
        for (Coleta coleta : coletas) {
            assertNotNull(coleta.getPatrimonioId());
            assertNotNull(coleta.getInventarioId());
            assertNotNull(coleta.getColetorId());
            assertTrue(coleta.getPatrimonioId() > 0);
            assertTrue(coleta.getInventarioId() > 0);
            assertTrue(coleta.getColetorId() > 0);
        }
    }
}
```

#### Property Test 2: Validação de Formato de Timestamps

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 2: Validação de Formato de Timestamps
 */
@Property(trials = 100)
public void timestampsDevemEstarEmFormatoCorreto(@From(ColetaGenerator.class) List<Coleta> coletas) {
    ValidationService validator = new ValidationService();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    
    for (Coleta coleta : coletas) {
        String timestamp = coleta.getDataColeta();
        
        // Deve estar em formato válido
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp, formatter));
        
        // Não deve estar no futuro
        LocalDateTime dataColeta = LocalDateTime.parse(timestamp, formatter);
        assertTrue(dataColeta.isBefore(now) || dataColeta.isEqual(now));
    }
}
```

#### Property Test 3: Atomicidade de Transação - Sucesso

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 4: Atomicidade de Transação - Sucesso
 */
@Property(trials = 100)
public void coletaSincronizadaDeveEstarMarcadaEmAmbosOsBancos(
    @From(ColetaGenerator.class) Coleta coleta) {
    
    SyncCoordinator coordinator = new SyncCoordinator();
    
    // Sincronizar coleta
    SyncResult result = coordinator.syncColeta(coleta);
    
    // Se sincronização foi bem-sucedida
    if (result.isSuccess()) {
        // Coleta deve estar no PostgreSQL
        assertTrue(postgresManager.coletaExists(coleta.getId()));
        
        // Coleta deve estar marcada como sincronizada no SQLite
        Coleta sqliteColeta = sqliteManager.findColetaById(coleta.getId());
        assertTrue(sqliteColeta.isSincronizada());
    }
}
```

#### Property Test 4: Atomicidade de Transação - Falha

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 5: Atomicidade de Transação - Falha
 */
@Property(trials = 100)
public void falhaDeveManter TodasColetasPendentes(
    @From(ColetaGenerator.class) List<Coleta> coletas) {
    
    SyncCoordinator coordinator = new SyncCoordinator();
    
    // Forçar falha na metade da sincronização
    coordinator.setFailureInjector(new FailureInjector(coletas.size() / 2));
    
    SyncResult result = coordinator.syncColetasPendentes(false);
    
    // Se sincronização falhou
    if (!result.isSuccess()) {
        // Nenhuma coleta deve estar no PostgreSQL
        for (Coleta coleta : coletas) {
            assertFalse(postgresManager.coletaExists(coleta.getId()));
        }
        
        // Todas as coletas devem estar pendentes no SQLite
        for (Coleta coleta : coletas) {
            Coleta sqliteColeta = sqliteManager.findColetaById(coleta.getId());
            assertFalse(sqliteColeta.isSincronizada());
        }
    }
}
```

#### Property Test 5: Integridade Referencial

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 6, 7, 8: Integridade Referencial
 */
@Property(trials = 100)
public void coletaDeveTerReferenciasValidasAntesDeInserir(
    @From(ColetaGenerator.class) Coleta coleta) {
    
    ValidationService validator = new ValidationService();
    
    // Se validação de integridade referencial passou
    if (validator.validateReferentialIntegrity(coleta)) {
        // Patrimônio deve existir no PostgreSQL
        assertTrue(postgresManager.patrimonioExists(coleta.getPatrimonioId()));
        
        // Inventário deve existir no PostgreSQL
        assertTrue(postgresManager.inventarioExists(coleta.getInventarioId()));
        
        // Coletor deve existir no PostgreSQL
        assertTrue(postgresManager.coletorExists(coleta.getColetorId()));
    }
}
```

#### Property Test 6: Ordem de Sincronização

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 10: Ordem Global de Sincronização
 */
@Property(trials = 100)
public void sincronizacaoDeveRespeitarOrdem() {
    SyncCoordinator coordinator = new SyncCoordinator();
    List<EntityType> syncOrder = new ArrayList<>();
    
    // Capturar ordem de sincronização
    coordinator.setSyncListener(entityType -> syncOrder.add(entityType));
    
    coordinator.syncAll();
    
    // Verificar ordem: RESPONSAVEL → SALA → PATRIMONIO → COLETA
    int responsavelIndex = syncOrder.indexOf(EntityType.RESPONSAVEL);
    int salaIndex = syncOrder.indexOf(EntityType.SALA);
    int patrimonioIndex = syncOrder.indexOf(EntityType.PATRIMONIO);
    int coletaIndex = syncOrder.indexOf(EntityType.COLETA);
    
    assertTrue(responsavelIndex < salaIndex);
    assertTrue(salaIndex < patrimonioIndex);
    assertTrue(patrimonioIndex < coletaIndex);
}
```

#### Property Test 7: Backup Antes de Modificação

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 14: Backup Antes de Modificação
 */
@Property(trials = 100)
public void backupDeveExistirAntesDeQualquerModificacao() {
    BackupService backupService = new BackupService();
    SyncCoordinator coordinator = new SyncCoordinator();
    
    // Limpar backups existentes
    backupService.cleanAllBackups();
    
    // Iniciar sincronização
    coordinator.syncColetasPendentes(false);
    
    // Deve haver pelo menos 1 backup
    List<BackupInfo> backups = backupService.listBackups();
    assertTrue(backups.size() >= 1);
    
    // Backup deve ter sido criado antes da sincronização
    BackupInfo latestBackup = backups.get(0);
    assertTrue(latestBackup.getCreatedAt().isBefore(LocalDateTime.now()));
}
```

#### Property Test 8: Limite de Backups

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 16: Limite de Backups
 */
@Property(trials = 100)
public void nuncaDeveHaverMaisDe10Backups() {
    BackupService backupService = new BackupService();
    
    // Criar 15 backups
    for (int i = 0; i < 15; i++) {
        backupService.createBackup();
    }
    
    // Limpar backups antigos
    backupService.cleanOldBackups();
    
    // Deve haver no máximo 10 backups
    List<BackupInfo> backups = backupService.listBackups();
    assertTrue(backups.size() <= 10);
}
```

#### Property Test 9: Restauração de Backup (Round Trip)

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 17: Restauração de Backup
 */
@Property(trials = 100)
public void restauracaoDeveRecuperarEstadoOriginal() {
    BackupService backupService = new BackupService();
    SQLiteManager sqliteManager = new SQLiteManager();
    
    // Calcular checksum do estado atual
    String originalChecksum = sqliteManager.calculateChecksum();
    
    // Criar backup
    Path backupPath = backupService.createBackup();
    
    // Modificar dados
    sqliteManager.insertRandomData();
    
    // Restaurar backup
    backupService.restoreBackup(backupPath);
    
    // Checksum deve ser igual ao original
    String restoredChecksum = sqliteManager.calculateChecksum();
    assertEquals(originalChecksum, restoredChecksum);
}
```

#### Property Test 10: Consistência de Totais

```java
/**
 * Feature: correcao-sincronizacao-sqlite-postgresql, Property 18: Consistência de Totais
 */
@Property(trials = 100)
public void totaisSQLiteEPostgresDevemCoincidirAposSincronizacao(
    @InRange(minInt = 1, maxInt = 100) int inventarioId) {
    
    SyncCoordinator coordinator = new SyncCoordinator();
    IntegrityChecker checker = new IntegrityChecker();
    
    // Sincronizar coletas
    SyncResult result = coordinator.syncColetasPendentes(false);
    
    // Se sincronização foi bem-sucedida
    if (result.isSuccess()) {
        // Totais devem coincidir
        int sqliteCount = sqliteManager.countColetasSincronizadas(inventarioId);
        int postgresCount = postgresManager.countColetas(inventarioId);
        
        assertEquals(sqliteCount, postgresCount);
    }
}
```

### Geradores Customizados

```java
public class ColetaGenerator extends Generator<Coleta> {
    public ColetaGenerator() {
        super(Coleta.class);
    }
    
    @Override
    public Coleta generate(SourceOfRandomness random, GenerationStatus status) {
        Coleta coleta = new Coleta();
        coleta.setId(random.nextInt(1, 10000));
        coleta.setPatrimonioId(random.nextInt(1, 1000));
        coleta.setInventarioId(random.nextInt(1, 10));
        coleta.setColetorId(random.nextInt(1, 50));
        coleta.setDataColeta(generateRandomTimestamp(random));
        coleta.setObservacoes(generateRandomString(random, 0, 200));
        coleta.setSincronizada(false);
        return coleta;
    }
    
    private String generateRandomTimestamp(SourceOfRandomness random) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(random.nextInt(1, 365));
        return past.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
```

### Testes Unitários

#### Teste de Validação de Campos Obrigatórios

```java
@Test
public void deveRejeitarColetaSemPatrimonioId() {
    ValidationService validator = new ValidationService();
    Coleta coleta = new Coleta();
    coleta.setInventarioId(1);
    coleta.setColetorId(1);
    // patrimonioId não definido
    
    ValidationResult result = validator.validateColetas(List.of(coleta));
    
    assertFalse(result.isValid());
    assertTrue(result.getErrors().stream()
        .anyMatch(e -> e.getField().equals("patrimonioId")));
}
```

#### Teste de Retry com Backoff

```java
@Test
public void deveAplicarBackoffExponencial() {
    RetryManager retryManager = new RetryManager();
    
    assertEquals(5000, retryManager.calculateBackoffDelay(1));
    assertEquals(10000, retryManager.calculateBackoffDelay(2));
    assertEquals(20000, retryManager.calculateBackoffDelay(3));
}
```

#### Teste de Resolução de Conflito

```java
@Test
public void deveResolverConflitoMantendoVersaoSQLite() {
    ConflictResolver resolver = new ConflictResolver();
    
    Patrimonio sqliteVersion = new Patrimonio();
    sqliteVersion.setDescricao("Versão SQLite");
    
    Patrimonio postgresVersion = new Patrimonio();
    postgresVersion.setDescricao("Versão PostgreSQL");
    
    Conflict conflict = new Conflict(EntityType.PATRIMONIO, 1, 
        sqliteVersion, postgresVersion);
    
    Object resolved = resolver.resolveConflict(conflict, 
        ResolutionStrategy.KEEP_SQLITE);
    
    assertEquals("Versão SQLite", ((Patrimonio) resolved).getDescricao());
}
```

### Testes de Integração

#### Teste de Sincronização Completa End-to-End

```java
@Test
@Transactional
public void deveSincronizarColetasComSucesso() {
    // Preparar dados no SQLite
    sqliteManager.insertColeta(createTestColeta(1));
    sqliteManager.insertColeta(createTestColeta(2));
    
    // Sincronizar
    SyncCoordinator coordinator = new SyncCoordinator();
    SyncResult result = coordinator.syncColetasPendentes(false);
    
    // Verificar resultado
    assertTrue(result.isSuccess());
    assertEquals(2, result.getTotalSuccess());
    
    // Verificar dados no PostgreSQL
    assertTrue(postgresManager.coletaExists(1));
    assertTrue(postgresManager.coletaExists(2));
}
```

### Cobertura de Testes

**Meta de Cobertura**:
- Cobertura de linha: > 80%
- Cobertura de branch: > 70%
- Propriedades críticas: 100% testadas

**Ferramentas**:
- JaCoCo para cobertura de código
- JUnit 5 para testes unitários
- JUnit-Quickcheck para testes de propriedade
- Mockito para mocks quando necessário

