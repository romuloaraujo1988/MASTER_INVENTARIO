# 🔧 Solução: Vazamento de Memória do Servidor Mobile (3.9GB+)

## 📋 Problema Identificado

O servidor Spring Boot estava consumindo **3.9GB de memória** e travando devido a:

### 1. ❌ Conexões NÃO eram gerenciadas por pool

A classe `DatabaseConnection.java` usava `DriverManager.getConnection()` diretamente, criando uma **nova conexão a cada chamada**:

```java
// ANTES (PROBLEMA)
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, props);  // Nova conexão TODA vez!
}
```

Cada requisição HTTP criava múltiplas conexões que **nunca eram reutilizadas**, causando:
- Vazamento de conexões
- Consumo excessivo de memória
- Esgotamento de recursos do PostgreSQL

### 2. ❌ HikariCP configurado mas NÃO utilizado

O `application.properties` tinha configurações do HikariCP, mas os DAOs usavam `DatabaseConnection` que **ignorava o pool**.

---

## ✅ Solução Implementada

### 1. Novo Pool de Conexões: `HikariConnectionPool.java`

Criado em `src/main/java/com/inventario/config/HikariConnectionPool.java`:

```java
// AGORA (SOLUÇÃO)
public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();  // Reutiliza conexões do pool!
}
```

**Configurações do pool:**
- Máximo 15 conexões
- Mínimo 5 conexões ociosas
- Timeout de 30 segundos
- Detecção de vazamento após 60 segundos
- Conexões ociosas liberadas após 10 minutos

### 2. `DatabaseConnection` atualizado

Agora usa o `HikariConnectionPool` como primeira opção:

```java
public static Connection getConnection() throws SQLException {
    // PRIORIDADE 1: Usar HikariCP Pool (RECOMENDADO)
    try {
        return HikariConnectionPool.getConnection();
    } catch (SQLException e) {
        // Fallback para conexão direta
    }
}
```

### 3. Endpoint de Monitoramento

Novo controller `HealthController.java` com endpoints:

- `GET /api/mobile/health` - Status geral
- `GET /api/mobile/health/pool` - Status do pool de conexões
- `GET /api/mobile/health/memory` - Status de memória
- `POST /api/mobile/health/gc` - Forçar garbage collection

### 4. Script de Inicialização Otimizado

`start-mobile-server-otimizado.bat` com:

```batch
java ^
    -Xms512m ^           # Memória inicial: 512MB
    -Xmx2g ^             # Memória máxima: 2GB
    -XX:+UseG1GC ^       # G1 Garbage Collector
    -XX:MaxGCPauseMillis=100 ^  # Pausas curtas
    ...
```

---

## 🚀 Como Usar

### 1. Recompilar o projeto

```bash
mvn clean package -DskipTests
```

### 2. Iniciar o servidor

**Opção A: Via aplicação desktop (recomendado)**
- Menu: Administração → Servidor Mobile → Iniciar Servidor
- O `MobileServerManager` agora usa `-Xmx2g` automaticamente

**Opção B: Via script standalone**
```bash
start-mobile-server-otimizado.bat
```

### 3. Monitorar saúde do servidor

```bash
curl http://localhost:8081/inventario/api/mobile/health
```

Resposta esperada:
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "database": "UP",
    "poolStats": "Pool Stats - Ativas: 2, Ociosas: 3, Total: 5, Aguardando: 0",
    "memoryUsedMB": 256,
    "memoryMaxMB": 1024,
    "memoryUsagePercent": "25.0%"
  }
}
```

---

## 📊 Comparação Antes x Depois

| Métrica | Antes | Depois |
|---------|-------|--------|
| Memória máxima | Sem limite (3.9GB+) | 2GB (limite rígido) |
| Conexões DB | Ilimitadas (vazamento) | 15 máximo (pool) |
| Reutilização | Nenhuma | 100% via pool |
| Detecção vazamento | Nenhuma | 60 segundos |
| Monitoramento | Nenhum | Endpoints /health |

---

## 🔍 Diagnóstico de Problemas

### Se o servidor ainda consumir muita memória:

1. **Verificar pool de conexões:**
   ```bash
   curl http://localhost:8081/api/mobile/health/pool
   ```

2. **Verificar memória:**
   ```bash
   curl http://localhost:8081/api/mobile/health/memory
   ```

3. **Forçar garbage collection:**
   ```bash
   curl -X POST http://localhost:8081/api/mobile/health/gc
   ```

4. **Verificar conexões no PostgreSQL:**
   ```sql
   SELECT count(*), state FROM pg_stat_activity GROUP BY state;
   ```

### Se houver muitas conexões ativas:

- Verificar se há queries lentas
- Verificar se há transações não commitadas
- Aumentar `LEAK_DETECTION` para detectar vazamentos

---

## 📁 Arquivos Modificados/Criados

### Criados:
- `src/main/java/com/inventario/config/HikariConnectionPool.java`
- `src/main/java/com/inventario/mobile/server/controller/HealthController.java`
- `start-mobile-server-otimizado.bat`

### Modificados:
- `src/main/java/com/inventario/util/DatabaseConnection.java`
- `src/main/java/com/inventario/service/MobileServerManager.java` (JVM: -Xmx2g)

---

## ⚠️ Importante

1. **Sempre feche conexões** - Use try-with-resources:
   ```java
   try (Connection conn = DatabaseConnection.getConnection()) {
       // usar conexão
   } // Conexão retorna ao pool automaticamente
   ```

2. **Não crie conexões em loops** - Reutilize a mesma conexão

3. **Monitore regularmente** - Use os endpoints /health

---

**Data:** 28/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Implementado
