# Correção de Vazamento de Memória - Servidor Mobile

**Data:** 02/12/2025  
**Problema:** Servidor consumindo ~1GB por usuário conectado  
**Solução:** Redução drástica de recursos e limpeza mais agressiva

---

## 🔴 Problema Identificado

O servidor mobile estava consumindo aproximadamente **1GB de memória por usuário** conectado, causando:
- Servidor com 3GB+ de uso com poucos usuários
- Lentidão e possíveis crashes
- Necessidade de reiniciar frequentemente

---

## ✅ Correções Aplicadas

### 1. JVM - Heap Drasticamente Reduzido

**Antes:**
```
-Xms256m -Xmx768m
```

**Depois:**
```
-Xms128m -Xmx384m
```

**Impacto:** Heap máximo reduzido de 768MB para 384MB (50% menos)

### 2. HikariCP - Pool de Conexões Reduzido

**Antes:**
```
maximum-pool-size=10
minimum-idle=3
```

**Depois:**
```
maximum-pool-size=5
minimum-idle=1
```

**Impacto:** Cada conexão consome ~5-10MB. Economia de ~50MB.

### 3. Tomcat - Threads Reduzidas

**Antes:**
```
threads.max=20
threads.min-spare=4
max-connections=50
```

**Depois:**
```
threads.max=10
threads.min-spare=2
max-connections=20
```

**Impacto:** Cada thread consome ~1MB de stack. Economia de ~10MB.

### 4. Sincronização - Limites Reduzidos

**Antes:**
```java
MAX_PATRIMONIOS = 10000
MAX_SALAS = 1000
MAX_RESPONSAVEIS = 500
```

**Depois:**
```java
MAX_PATRIMONIOS = 1000
MAX_SALAS = 200
MAX_RESPONSAVEIS = 100
```

**Impacto:** Evita carregar dados demais em memória durante sync.

### 5. Cache de Descrições Reduzido

**Antes:** 500 entradas  
**Depois:** 100 entradas

### 6. Dispositivos Conectados - Limite Reduzido

**Antes:** 50 dispositivos, timeout 5 min  
**Depois:** 20 dispositivos, timeout 3 min

### 7. Limpeza de Memória Mais Agressiva

- Limpeza leve: 5 min → **2 min**
- Limpeza agressiva: 15 min → **5 min**
- Verificação: 30 seg → **15 seg**
- Threshold: 70% → **50%**

### 8. Schedulers Duplicados Removidos

`MemoryOptimizationConfig` tinha schedulers duplicados com `MemoryCleanupService`. Removidos para evitar overhead.

### 9. Actuator/Métricas Desabilitadas

Métricas JVM, Tomcat e HikariCP desabilitadas para economizar memória.

---

## 📊 Comparativo de Consumo Esperado

| Recurso | Antes | Depois | Economia |
|---------|-------|--------|----------|
| Heap máximo | 768MB | 384MB | 384MB |
| Pool DB | 10 conn | 5 conn | ~50MB |
| Threads Tomcat | 20 | 10 | ~10MB |
| Cache descrições | 500 | 100 | ~5MB |
| **Total estimado** | **~1GB/usuário** | **~150-200MB total** | **~80%** |

---

## 🚀 Como Usar

### Opção 1: Script Otimizado
```batch
start-mobile-server-lowmem-v2.bat
```

### Opção 2: Via Aplicação Desktop
O servidor já usa as novas configurações automaticamente ao iniciar pelo menu.

### Opção 3: Manual
```batch
java -Xms128m -Xmx384m ^
  -XX:+UseG1GC ^
  -XX:InitiatingHeapOccupancyPercent=20 ^
  -Dspring.profiles.active=mobile ^
  -jar target\sistema-inventario-2.0.0.jar
```

---

## ⚠️ Limitações

Com as novas configurações:
- **Máximo 10 usuários simultâneos** (recomendado)
- **Sincronização limitada a 1000 patrimônios** por vez
- **20 dispositivos máximo** em memória

Para mais usuários, considere aumentar o heap gradualmente.

---

## 🔧 Monitoramento

O servidor agora loga uso de memória quando ultrapassa 50% do heap:
```
🧹 Limpeza leve - Memória: 45% (173 MB)
```

---

## 📝 Arquivos Modificados

1. `MobileServerManager.java` - JVM opts
2. `application-mobile.properties` - Configurações Spring
3. `MobileSyncService.java` - Limites de sync
4. `MemoryCleanupService.java` - Limpeza mais agressiva
5. `MemoryOptimizationConfig.java` - Schedulers removidos
6. `DescricaoResumoService.java` - Cache reduzido
7. `ConnectedDevicesManager.java` - Limites reduzidos
8. `start-mobile-server-lowmem-v2.bat` - Script novo

---

**Versão:** 5.1.0  
**Status:** ✅ Aplicado
