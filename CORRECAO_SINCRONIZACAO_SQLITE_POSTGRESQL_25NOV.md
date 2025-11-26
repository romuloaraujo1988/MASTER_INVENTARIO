# Correção: Sincronização SQLite → PostgreSQL

**Data:** 25/11/2025  
**Status:** ✅ CORRIGIDO E COMPILADO COM SUCESSO

## Problema Original

```
java.sql.SQLException: Configuração de banco PostgreSQL não disponível para sincronização
    at com.inventario.util.DatabaseConnection.getPostgreSQLConnection(DatabaseConnection.java:317)
```

O método `getPostgreSQLConnection()` não estava usando a configuração carregada do arquivo `configuracao_banco.json`.

## Sintoma

Ao tentar sincronizar dados do SQLite para o PostgreSQL (menu Sistema → Sincronizar), o sistema exibia erro de "Configuração de banco PostgreSQL não disponível".

## Causa Raiz

O `DatabaseConnection` carregava a configuração do JSON no bloco estático, mas o método `getPostgreSQLConnection()` verificava apenas `currentConfig` (do `DatabaseConfigManager`), ignorando a configuração JSON.

## Correções Aplicadas

### 1. `DatabaseConnection.getPostgreSQLConnection()` (PRINCIPAL)

**Antes:**
```java
public static Connection getPostgreSQLConnection() throws SQLException {
    if (currentConfig != null && currentConfig.isValid()) {
        return createConnectionFromConfig(currentConfig);
    }
    throw new SQLException("Configuração de banco PostgreSQL não disponível");
}
```

**Depois:**
```java
public static Connection getPostgreSQLConnection() throws SQLException {
    // PRIORIDADE 1: Usar configuração do JSON na raiz
    if (jsonConfigLoaded) {
        return createConnectionFromJson();
    }
    
    // PRIORIDADE 2: Usar DatabaseConfigManager
    if (currentConfig != null && currentConfig.isValid()) {
        return createConnectionFromConfig(currentConfig);
    }
    
    // Tentar recarregar do JSON
    loadJsonConfig();
    if (jsonConfigLoaded) {
        return createConnectionFromJson();
    }
    
    throw new SQLException("Configuração PostgreSQL não disponível");
}
```

### 2. `DatabaseConnection.getCurrentConfig()` (AUXILIAR)

Corrigido para retornar configuração válida baseada no JSON quando `currentConfig` é null:

```java
public static DatabaseConfig getCurrentConfig() {
    if (currentConfig != null && currentConfig.isValid()) {
        return currentConfig;
    }
    
    // Se JSON está carregado, cria DatabaseConfig a partir dele
    if (jsonConfigLoaded) {
        DatabaseConfig jsonBasedConfig = new DatabaseConfig();
        jsonBasedConfig.setHost(jsonHost);
        jsonBasedConfig.setPort(jsonPort);
        jsonBasedConfig.setDatabase(jsonDatabase);
        jsonBasedConfig.setUsername(jsonUser);
        jsonBasedConfig.setPassword(jsonPassword);
        return jsonBasedConfig;
    }
    
    return currentConfig;
}
```

### 3. Novo método `hasValidConfig()`

```java
public static boolean hasValidConfig() {
    return jsonConfigLoaded || (currentConfig != null && currentConfig.isValid());
}
```

## Fluxo de Sincronização Corrigido

```
1. MainFrame.executarSincronizacao()
   ↓
2. OfflineManager.executarSincronizacaoManual()
   ↓
3. DataSynchronizer.executarSincronizacaoManual()
   ↓
4. DataSynchronizer.sincronizarDadosLocais()
   ↓
5. DataSynchronizer.processarOperacaoUpload()
   ↓
6. DatabaseConnection.getPostgreSQLConnection() ← CORRIGIDO
   ↓
7. Conexão PostgreSQL estabelecida via JSON config
   ↓
8. Dados do SQLite enviados para PostgreSQL ✅
```

## Arquivos Modificados

- `src/main/java/com/inventario/util/DatabaseConnection.java`

## Como Testar

1. Certifique-se de que `configuracao_banco.json` existe na raiz do projeto
2. Execute a aplicação: `.\mvnw.cmd exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"`
3. Faça login
4. Vá em **Sistema → Sincronizar** ou clique no botão de sincronização
5. Verifique os logs para confirmar que a conexão PostgreSQL foi estabelecida

## Verificação

```sql
-- No PostgreSQL, verificar se dados foram sincronizados
SELECT COUNT(*) FROM tabela_coleta;
SELECT * FROM tabela_coleta ORDER BY id DESC LIMIT 5;
```

## Configuração Necessária

O arquivo `configuracao_banco.json` deve existir na raiz do projeto:

```json
{
    "postgresql": {
        "host": "localhost",
        "database": "sispatrimonio",
        "user": "postgres",
        "password": "sua_senha",
        "port": 5432
    }
}
```
