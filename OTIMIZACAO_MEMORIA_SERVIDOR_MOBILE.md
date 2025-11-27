# Otimização de Memória - Servidor Mobile

## Problema Identificado
O servidor mobile estava consumindo ~4GB de memória para apenas ~15 usuários.

## Causas Identificadas

### 1. Logs Excessivos
- `RequestLoggingFilter` logava TODOS os headers de CADA requisição
- Logs em nível DEBUG/INFO para todas as classes

### 2. Configurações Duplicadas/Conflitantes
- Métricas habilitadas duas vezes no `application-mobile.properties`
- Pool de conexões muito grande (10 conexões para 15 usuários)

### 3. Threads Excessivas
- Tomcat com 50 threads máximas (desnecessário para 15 usuários)

### 4. Acúmulo em Memória
- `ConnectedDevicesManager` sem limite de dispositivos
- Sem limpeza periódica de dispositivos inativos

### 5. JVM Sem Limites
- Nenhum limite de memória definido nos scripts de inicialização

---

## Correções Aplicadas

### 1. `application-mobile.properties` (v3.0)
```properties
# Logging MÍNIMO
logging.level.root=WARN
logging.level.com.inventario=WARN
logging.level.org.springframework=ERROR

# HikariCP para 15 usuários
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2

# Tomcat para 15 usuários
server.tomcat.threads.max=20
server.tomcat.threads.min-spare=2

# Métricas DESABILITADAS
management.metrics.enable.all=false
```

### 2. `RequestLoggingFilter.java` (v3.0)
- Logging desabilitado por padrão
- Pode ser habilitado via `mobile.server.request-logging.enabled=true`
- Headers NÃO são mais logados

### 3. `DeviceTrackingInterceptor.java` (v3.0)
- Tracking desabilitado por padrão
- Pode ser habilitado via `mobile.server.device-tracking.enabled=true`

### 4. `ConnectedDevicesManager.java`
- Limite de 50 dispositivos em memória
- Limpeza automática a cada 100 operações
- Remoção de dispositivos inativos

### 5. Scripts de Inicialização Otimizados
- `start-mobile-server-otimizado.bat`
- `start-mobile-server-otimizado.ps1`

---

## Configurações JVM Recomendadas

```bash
# Para ~15 usuários
-Xms256m                              # Memória inicial: 256MB
-Xmx512m                              # Memória máxima: 512MB
-XX:+UseG1GC                          # G1 Garbage Collector
-XX:MaxGCPauseMillis=100              # Pausas curtas do GC
-XX:+UseStringDeduplication           # Economiza memória com strings
-XX:+ParallelRefProcEnabled           # Processamento paralelo
-XX:InitiatingHeapOccupancyPercent=45 # Inicia GC mais cedo
-XX:+DisableExplicitGC                # Desabilita System.gc()
-Djava.awt.headless=true              # Modo headless
```

---

## Comparação de Consumo

| Recurso | Antes | Depois | Redução |
|---------|-------|--------|---------|
| Memória JVM | ~4GB | ~512MB | **87%** |
| Pool Conexões | 10 | 5 | 50% |
| Threads Tomcat | 50 | 20 | 60% |
| Logs/requisição | ~20 linhas | 0-1 linha | **95%** |

---

## Como Usar

### Opção 1: Via Aplicação Desktop (RECOMENDADO)
Basta iniciar o servidor pelo painel da aplicação desktop.
As configurações de memória otimizadas já estão embutidas no `MobileServerManager.java`.

### Opção 2: Script BAT (Windows CMD)
```cmd
start-mobile-server-otimizado.bat
```

### Opção 3: Script PowerShell
```powershell
.\start-mobile-server-otimizado.ps1
```

### Opção 4: Maven Direto
```cmd
set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"
```

---

## Monitoramento

### Verificar Uso de Memória
```powershell
# Ver processos Java
Get-Process java | Select-Object Id, WorkingSet64, CPU

# Converter para MB
Get-Process java | Select-Object Id, @{N='MemoriaMB';E={[math]::Round($_.WorkingSet64/1MB,2)}}
```

### Verificar Conexões do Banco
```sql
SELECT count(*) FROM pg_stat_activity WHERE application_name LIKE '%inventario%';
```

---

## Propriedades Opcionais

Se precisar habilitar features desabilitadas:

```properties
# Habilitar logging de requisições (debug)
mobile.server.request-logging.enabled=true

# Habilitar tracking de dispositivos
mobile.server.device-tracking.enabled=true

# Aumentar pool se necessário (mais usuários)
spring.datasource.hikari.maximum-pool-size=10
```

---

## Resultado Esperado

Com as otimizações aplicadas, o servidor mobile deve:
- Consumir **~300-500MB** de memória (ao invés de 4GB)
- Suportar **15-20 usuários** simultâneos confortavelmente
- Ter **tempo de resposta** mais rápido (menos GC)
- **Não travar** por falta de memória

---

**Data:** 27/11/2025  
**Versão:** 3.0.0  
**Status:** ✅ Otimizações aplicadas
