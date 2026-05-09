# Bugfix Requirements Document

## Introduction

O `SmartSyncWorker` foi criado para realizar sincronização inteligente de coletas pendentes em background, mas a implementação está incompleta. O método `performSync()` apenas retorna `true` (simulando sucesso) sem realizar nenhuma sincronização real. Isso causa uma falha crítica no sistema: coletas pendentes não são sincronizadas automaticamente, o usuário acredita que a sincronização está funcionando (pois o worker retorna sucesso), e os dados ficam apenas locais podendo ser perdidos.

**Impacto:** 🔴 CRÍTICO - Sincronização automática não funciona, dados podem ser perdidos se o app for desinstalado.

**Componentes Afetados:**
- `SmartSyncWorker.kt` (linha 127-131) - Worker com implementação incompleta
- `SincronizarColetasPendentesUseCase.kt` - Use Case já implementado que deve ser usado
- `ColetaRepositoryImpl.kt` - Repository com métodos de sincronização implementados

**Componentes Disponíveis:**
- `SincronizarColetasPendentesUseCase` - Use Case pronto para uso
- `ColetaRepositoryImpl.sincronizarColetasPendentes()` - Método implementado
- `ColetaRepositoryImpl.sincronizarEmLote()` - Batch sync implementado
- `ColetaRepositoryImpl.sincronizarIndividualmente()` - Fallback implementado

---

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN o `SmartSyncWorker` é executado pelo WorkManager THEN o sistema apenas simula sucesso sem sincronizar nenhuma coleta

1.2 WHEN existem coletas pendentes no banco local THEN o worker retorna `true` mas as coletas permanecem não sincronizadas

1.3 WHEN o método `performSync()` é chamado THEN o sistema apenas executa um delay de 1 segundo e retorna `true` sem chamar nenhum repository ou use case

1.4 WHEN o worker completa sua execução THEN o timestamp de última sincronização é atualizado mesmo que nenhuma coleta tenha sido sincronizada

1.5 WHEN o usuário verifica coletas pendentes após o worker executar THEN as coletas continuam marcadas como não sincronizadas no banco

### Expected Behavior (Correct)

2.1 WHEN o `SmartSyncWorker` é executado pelo WorkManager THEN o sistema SHALL chamar `SincronizarColetasPendentesUseCase` para sincronizar coletas pendentes

2.2 WHEN existem coletas pendentes no banco local THEN o worker SHALL tentar sincronizá-las usando batch sync primeiro, com fallback para sync individual

2.3 WHEN o método `performSync()` é chamado THEN o sistema SHALL executar a sincronização real através do use case e retornar o resultado verdadeiro

2.4 WHEN a sincronização é bem-sucedida THEN o worker SHALL registrar métricas de sucesso (quantidade de coletas sincronizadas) e atualizar o timestamp

2.5 WHEN a sincronização falha THEN o worker SHALL retornar `false` e registrar o erro para permitir retry automático

2.6 WHEN não há coletas pendentes THEN o worker SHALL retornar `true` imediatamente sem tentar sincronizar

2.7 WHEN o use case retorna erro THEN o worker SHALL capturar a exceção, registrar no log e retornar `false` para retry

### Unchanged Behavior (Regression Prevention)

3.1 WHEN o auto-sync está desabilitado nas preferências THEN o sistema SHALL CONTINUE TO pular a sincronização e retornar sucesso

3.2 WHEN as condições de rede não estão atendidas (sem internet, bateria baixa) THEN o sistema SHALL CONTINUE TO reagendar o worker sem tentar sincronizar

3.3 WHEN o worker excede 3 tentativas de retry THEN o sistema SHALL CONTINUE TO retornar falha e parar de tentar

3.4 WHEN o método `shouldSync()` retorna `false` THEN o sistema SHALL CONTINUE TO reagendar o worker sem executar sincronização

3.5 WHEN ocorre uma exceção durante a verificação de condições THEN o sistema SHALL CONTINUE TO capturar o erro e tentar retry se não excedeu o limite

---

## Bug Condition and Property

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type WorkerExecution
  OUTPUT: boolean
  
  // Retorna true quando o bug ocorre
  RETURN X.performSyncCalled = true 
         AND X.useCaseCalled = false 
         AND X.returnValue = true
END FUNCTION
```

**Explicação:** O bug ocorre quando o método `performSync()` é chamado, mas nenhum use case de sincronização é invocado, e mesmo assim o método retorna `true` (simulando sucesso).

### Property Specification - Fix Checking

```pascal
// Property: Fix Checking - Sincronização Real Implementada
FOR ALL X WHERE isBugCondition(X) DO
  result ← performSync'(X)
  ASSERT result.useCaseCalled = true
  ASSERT result.coletasSincronizadas >= 0
  ASSERT (result.coletasSincronizadas > 0) IMPLIES (result.returnValue = true)
  ASSERT (result.error != null) IMPLIES (result.returnValue = false)
END FOR
```

**Explicação:** Para todas as execuções onde o bug ocorria, após a correção:
- O use case DEVE ser chamado
- A quantidade de coletas sincronizadas DEVE ser retornada (>= 0)
- Se coletas foram sincronizadas, o método DEVE retornar `true`
- Se houve erro, o método DEVE retornar `false`

### Property Specification - Preservation Checking

```pascal
// Property: Preservation Checking - Comportamento Existente Preservado
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT performSync(X) = performSync'(X)
END FOR
```

**Explicação:** Para todas as execuções onde o bug NÃO ocorre (auto-sync desabilitado, condições não atendidas, etc.), o comportamento DEVE permanecer idêntico.

---

## Counterexample (Demonstração do Bug)

**Cenário:** Usuário coleta 5 patrimônios offline e espera sincronização automática

**Entrada:**
```kotlin
// Estado inicial
coletasPendentes = 5
autoSyncEnabled = true
hasNetwork = true
batteryLevel = 80%
```

**Comportamento Atual (Buggy):**
```kotlin
// SmartSyncWorker.performSync()
kotlinx.coroutines.delay(1000)  // Apenas simula
return true  // Retorna sucesso falso

// Resultado
coletasSincronizadas = 0  // ❌ Nenhuma coleta sincronizada
coletasPendentes = 5      // ❌ Continuam pendentes
lastSyncTime = updated    // ❌ Timestamp atualizado incorretamente
```

**Comportamento Esperado (Fixed):**
```kotlin
// SmartSyncWorker.performSync()
val result = sincronizarColetasPendentesUseCase()
return result.isSuccess && result.getOrNull() > 0

// Resultado
coletasSincronizadas = 5  // ✅ Todas sincronizadas
coletasPendentes = 0      // ✅ Nenhuma pendente
lastSyncTime = updated    // ✅ Timestamp correto
```

---

## Technical Context

### Arquitetura Atual

```
SmartSyncWorker (Worker)
    ↓ (deveria chamar)
SincronizarColetasPendentesUseCase (Use Case)
    ↓ (chama)
ColetaRepository (Interface)
    ↓ (implementado por)
ColetaRepositoryImpl (Repository)
    ↓ (usa)
ColetaDao (Room) + ColetaApi (Retrofit)
```

### Código Problemático

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/worker/SmartSyncWorker.kt`

**Linhas:** 127-131

```kotlin
private suspend fun performSync(): Boolean {
    return try {
        // TODO: Implementar lógica de sincronização real
        // Por enquanto, apenas simula sucesso
        
        Log.d(TAG, "Executando sincronização...")
        
        // Aqui você chamaria seu repository de sincronização
        // Exemplo:
        // val syncRepository = SyncRepository(...)
        // val result = syncRepository.syncAll()
        // return result.isSuccess
        
        // Simulação de sincronização
        kotlinx.coroutines.delay(1000)
        
        true
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao executar sincronização", e)
        false
    }
}
```

### Solução Proposta

**Injetar o Use Case via Hilt:**

```kotlin
@HiltWorker
class SmartSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase
) : CoroutineWorker(context, params)
```

**Implementar sincronização real:**

```kotlin
private suspend fun performSync(): Boolean {
    return try {
        Log.d(TAG, "Executando sincronização...")
        
        // Chamar use case de sincronização
        val result = sincronizarColetasPendentesUseCase()
        
        if (result.isSuccess) {
            val quantidade = result.getOrNull() ?: 0
            Log.d(TAG, "Sincronização concluída: $quantidade coletas sincronizadas")
            quantidade >= 0
        } else {
            val erro = result.exceptionOrNull()
            Log.e(TAG, "Erro na sincronização: ${erro?.message}", erro)
            false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao executar sincronização", e)
        false
    }
}
```

---

## Validation Criteria

### Critério 1: Use Case é Chamado
- ✅ Verificar logs: "Executando sincronização..."
- ✅ Verificar que `SincronizarColetasPendentesUseCase.invoke()` é chamado
- ✅ Verificar que não há mais delay simulado

### Critério 2: Coletas São Sincronizadas
- ✅ Criar 5 coletas pendentes no banco
- ✅ Executar worker
- ✅ Verificar que coletas foram marcadas como sincronizadas
- ✅ Verificar que `coletaDao.contarPendentes()` retorna 0

### Critério 3: Métricas São Registradas
- ✅ Verificar logs: "X coletas sincronizadas"
- ✅ Verificar que timestamp é atualizado apenas em caso de sucesso
- ✅ Verificar que erros são registrados corretamente

### Critério 4: Retry Funciona
- ✅ Simular erro de rede
- ✅ Verificar que worker retorna `Result.retry()`
- ✅ Verificar que worker tenta novamente até 3 vezes
- ✅ Verificar que após 3 falhas retorna `Result.failure()`

### Critério 5: Comportamento Preservado
- ✅ Desabilitar auto-sync → worker deve pular sincronização
- ✅ Sem rede → worker deve reagendar
- ✅ Bateria baixa → worker deve reagendar
- ✅ Sem coletas pendentes → worker deve retornar sucesso imediatamente
