# 🔧 Solução - ANR no Dashboard

## 🎯 Problema Identificado

**Sintoma:** App trava (ANR - Application Not Responding) ao abrir

**Causa Raiz:**
```
11-19 03:10:34.185 WindowManager: ANR in SplashActivity
Reason: Input dispatching timed out (Waited 5005ms)
```

**Análise dos Logs:**
1. Dashboard está carregando dados do servidor
2. Requisição HTTP está demorando muito (>5s)
3. Jobs sendo cancelados (`JobCancellationException`)
4. ANR acontece na SplashActivity/MainActivity

---

## 🔍 Causas Possíveis

### 1. Servidor Lento ou Fora do Ar
- Requisição para `getDashboardStats()` demora >5s
- Timeout padrão muito alto

### 2. Sem Tratamento de Timeout
- Não há timeout configurado no OkHttp
- App espera indefinidamente

### 3. Carregamento Síncrono
- Dashboard carrega na thread principal
- Bloqueia UI enquanto aguarda resposta

### 4. Sem Fallback Offline
- Não tenta buscar dados locais primeiro
- Depende 100% do servidor

---

## ✅ Soluções Implementadas

### Solução 1: Adicionar Timeouts no OkHttp

```kotlin
// NetworkModule.kt ou ApiModule.kt
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)      // Conexão: 10s
        .readTimeout(15, TimeUnit.SECONDS)         // Leitura: 15s
        .writeTimeout(15, TimeUnit.SECONDS)        // Escrita: 15s
        .callTimeout(30, TimeUnit.SECONDS)         // Total: 30s
        .addInterceptor(loggingInterceptor)
        .build()
}
```

### Solução 2: Offline-First no Dashboard

```kotlin
// DashboardRepositoryImpl.kt
override suspend fun buscarEstatisticas(inventarioId: Int?): Result<DashboardStats> {
    return try {
        // 1. Tentar buscar do servidor (com timeout)
        withTimeout(10_000) {  // 10 segundos máximo
            val response = apiService.getDashboardStats()
            
            if (response.isSuccessful && response.body() != null) {
                val stats = mapper.toDomain(response.body()!!.data!!)
                
                // Salvar no cache local
                cacheStats(stats)
                
                return@withTimeout Result.success(stats)
            }
        }
        
        // 2. Se falhar, buscar do cache local
        val cachedStats = getCachedStats()
        if (cachedStats != null) {
            Result.success(cachedStats)
        } else {
            Result.failure(Exception("Sem dados disponíveis"))
        }
        
    } catch (e: TimeoutCancellationException) {
        // Timeout: buscar do cache
        val cachedStats = getCachedStats()
        if (cachedStats != null) {
            Result.success(cachedStats)
        } else {
            Result.failure(Exception("Servidor não respondeu"))
        }
    } catch (e: Exception) {
        // Erro: buscar do cache
        val cachedStats = getCachedStats()
        if (cachedStats != null) {
            Result.success(cachedStats)
        } else {
            Result.failure(e)
        }
    }
}
```

### Solução 3: Loading State no ViewModel

```kotlin
// DashboardViewModelClean.kt
fun loadDashboardData(inventarioId: Int?) {
    viewModelScope.launch {
        // Mostrar loading
        _uiState.value = DashboardUiStateClean.Loading
        
        // Timeout de 10 segundos
        withTimeoutOrNull(10_000) {
            val result = buscarEstatisticasUseCase(inventarioId)
            
            result.fold(
                onSuccess = { stats ->
                    _uiState.value = DashboardUiStateClean.Success(stats)
                },
                onFailure = { error ->
                    _uiState.value = DashboardUiStateClean.Error(
                        error.message ?: "Erro ao carregar"
                    )
                }
            )
        } ?: run {
            // Timeout: mostrar erro
            _uiState.value = DashboardUiStateClean.Error(
                "Servidor não respondeu. Tente novamente."
            )
        }
    }
}
```

### Solução 4: Indicador de Loading

```kotlin
// DashboardFragment.kt
private fun updateUI(state: DashboardUiStateClean) {
    when (state) {
        is DashboardUiStateClean.Loading -> {
            binding.progressBar.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
        
        is DashboardUiStateClean.Success -> {
            binding.progressBar.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
            
            // Atualizar estatísticas
            updateStats(state.stats)
        }
        
        is DashboardUiStateClean.Error -> {
            binding.progressBar.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
            
            // Mostrar erro
            Toast.makeText(
                requireContext(),
                state.message,
                Toast.LENGTH_LONG
            ).show()
            
            // Mostrar dados em cache (se houver)
            showCachedDataIfAvailable()
        }
    }
}
```

---

## 🚀 Solução Rápida (Emergencial)

Se não puder recompilar agora, use estas soluções temporárias:

### 1. Aumentar Timeout do Emulador
```bash
# Aumentar timeout de ANR (apenas para testes)
adb shell settings put global anr_timeout 10000
```

### 2. Desabilitar Strict Mode
```bash
# Desabilitar strict mode
adb shell settings put global strict_mode_visual 0
```

### 3. Limpar Dados do App
```bash
# Limpar cache e dados
adb shell pm clear com.inventario.mobile.debug
```

### 4. Reinstalar APK
```bash
# Desinstalar
adb uninstall com.inventario.mobile.debug

# Reinstalar
adb install -r InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
```

---

## 📋 Checklist de Correções

### Imediato (Fazer Agora)
- [ ] Adicionar timeouts no OkHttpClient
- [ ] Adicionar `withTimeout` no ViewModel
- [ ] Mostrar loading enquanto carrega
- [ ] Tratar erro de timeout

### Curto Prazo (Esta Semana)
- [ ] Implementar cache local de estatísticas
- [ ] Offline-first no Dashboard
- [ ] Retry automático em falhas
- [ ] Indicador de "dados desatualizados"

### Médio Prazo (Próximo Mês)
- [ ] Pre-loading de dados no splash
- [ ] Cache inteligente com TTL
- [ ] Sincronização incremental
- [ ] Métricas de performance

---

## 🧪 Como Testar

### Teste 1: Servidor Lento
```
1. Simular rede lenta no emulador
2. Abrir app
3. Verificar: Loading aparece
4. Verificar: Timeout após 10s
5. Verificar: Mensagem de erro clara
```

### Teste 2: Servidor Fora
```
1. Desligar servidor backend
2. Abrir app
3. Verificar: Tenta carregar
4. Verificar: Mostra erro após timeout
5. Verificar: App não trava
```

### Teste 3: Offline Completo
```
1. Modo avião
2. Abrir app
3. Verificar: Mostra dados em cache
4. Verificar: Indicador "offline"
5. Verificar: App funciona normalmente
```

---

## 📊 Logs para Monitorar

```bash
# Ver ANRs
adb logcat -s "ANR:*" "ActivityManager:*"

# Ver timeouts
adb logcat -s "OkHttp:*" "Retrofit:*"

# Ver Dashboard
adb logcat -s "DashboardRepositoryImpl:*" "DashboardViewModelClean:*"

# Ver coroutines
adb logcat | grep -i "JobCancellationException"
```

---

## ✅ Resultado Esperado

**Antes:**
- ❌ App trava ao abrir
- ❌ ANR após 5 segundos
- ❌ Sem feedback para usuário
- ❌ Perde dados se fechar

**Depois:**
- ✅ Loading aparece imediatamente
- ✅ Timeout após 10s (configurável)
- ✅ Mensagem de erro clara
- ✅ Fallback para dados em cache
- ✅ App nunca trava

---

**Versão:** 1.0.0  
**Data:** 19/11/2025  
**Status:** 🔧 SOLUÇÃO DOCUMENTADA  
**Prioridade:** 🔴 CRÍTICA
