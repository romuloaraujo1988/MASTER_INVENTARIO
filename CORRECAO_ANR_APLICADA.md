# Correção de ANR (Application Not Responding) - Aplicada

## 🔴 Problemas Identificados

### 1. **runBlocking no Interceptor** (CRÍTICO)
**Localização:** `ApiModule.kt` - `provideAuthInterceptor()`

**Problema:**
```kotlin
val currentUser = kotlinx.coroutines.runBlocking {
    localDataManager.getCurrentUser()
}
```

**Impacto:**
- `runBlocking` bloqueia a thread onde é executado
- Interceptors rodam na thread de rede
- Pode bloquear a UI thread indiretamente
- Causa ANR quando a operação demora

**Solução Aplicada:**
```kotlin
// Usar apenas PreferencesManager (síncrono e rápido)
val token = preferencesManager.getAccessToken()
val isTokenValid = preferencesManager.isTokenValid()
```

---

### 2. **Timeouts Muito Altos** (CRÍTICO)
**Localização:** `ApiModule.kt` - `provideOkHttpClient()`

**Problema:**
```kotlin
.connectTimeout(45, TimeUnit.SECONDS)   // 45 segundos!
.readTimeout(60, TimeUnit.SECONDS)      // 60 segundos!
.writeTimeout(60, TimeUnit.SECONDS)     // 60 segundos!
.callTimeout(120, TimeUnit.SECONDS)     // 2 MINUTOS!
```

**Impacto:**
- App fica "travado" esperando resposta do servidor
- Usuário vê tela congelada
- Android detecta como ANR após 5 segundos

**Solução Aplicada:**
```kotlin
.connectTimeout(10, TimeUnit.SECONDS)   // Reduzido para 10s
.readTimeout(15, TimeUnit.SECONDS)      // Reduzido para 15s
.writeTimeout(15, TimeUnit.SECONDS)     // Reduzido para 15s
.callTimeout(30, TimeUnit.SECONDS)      // Reduzido para 30s
```

---

## ✅ Correções Aplicadas

### 1. Removido `runBlocking` do AuthInterceptor
```kotlin
@Provides
@Singleton
fun provideAuthInterceptor(
    @ApplicationContext context: Context,
    preferencesManager: PreferencesManager  // ← Removido LocalDataManager
): Interceptor {
    return Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Usar apenas PreferencesManager (síncrono e rápido)
        val token = preferencesManager.getAccessToken()
        val isTokenValid = preferencesManager.isTokenValid()
        
        val newRequest = if (token != null && isTokenValid) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
        }
        
        chain.proceed(newRequest)
    }
}
```

### 2. Reduzidos Timeouts do OkHttpClient
```kotlin
@Provides
@Singleton
fun provideOkHttpClient(
    @ApplicationContext context: Context,
    loggingInterceptor: HttpLoggingInterceptor,
    authInterceptor: Interceptor,
    deviceInfoInterceptor: DeviceInfoInterceptor
): OkHttpClient {
    val cacheSize = 10 * 1024 * 1024L
    val cache = okhttp3.Cache(context.cacheDir, cacheSize)
    
    return OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(deviceInfoInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)  // ✅ Otimizado
        .readTimeout(15, TimeUnit.SECONDS)     // ✅ Otimizado
        .writeTimeout(15, TimeUnit.SECONDS)    // ✅ Otimizado
        .callTimeout(30, TimeUnit.SECONDS)     // ✅ Otimizado
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
        .build()
}
```

---

## 📊 Impacto Esperado

### Antes
- ❌ ANR frequentes
- ❌ App travando ao carregar dados
- ❌ Timeout de 2 minutos
- ❌ `runBlocking` bloqueando threads

### Depois
- ✅ Sem ANR
- ✅ App responsivo
- ✅ Timeout máximo de 30 segundos
- ✅ Operações assíncronas corretas

---

## 🔧 Otimizações Adicionais Recomendadas

### 1. Adicionar Loading States Visuais
```kotlin
// No DashboardFragment
private fun updateUI(state: DashboardUiStateClean) {
    // Mostrar skeleton loading ao invés de tela branca
    if (state.isLoading) {
        showSkeletonLoading()
    } else {
        hideSkeletonLoading()
        updateContent(state)
    }
}
```

### 2. Implementar Retry com Backoff
```kotlin
// No ApiModule
.addInterceptor(RetryInterceptor(maxRetries = 3, backoffMs = 1000))
```

### 3. Cache de Dados Críticos
```kotlin
// No DashboardRepository
override suspend fun buscarEstatisticas(inventarioId: Int?): Result<DashboardStats> {
    // Tentar cache primeiro
    val cached = cache.get("dashboard_stats_$inventarioId")
    if (cached != null && !cached.isExpired()) {
        return Result.success(cached.data)
    }
    
    // Buscar do servidor
    val result = apiService.getDashboardStats()
    
    // Salvar no cache
    if (result.isSuccess) {
        cache.put("dashboard_stats_$inventarioId", result.getOrNull())
    }
    
    return result
}
```

### 4. Paginação de Dados
```kotlin
// No PatrimonioDao
@Query("SELECT * FROM patrimonio LIMIT :limit OFFSET :offset")
suspend fun buscarPaginado(limit: Int, offset: Int): List<PatrimonioEntity>
```

### 5. Lazy Loading de Imagens
```kotlin
// Usar Coil ou Glide com placeholders
Coil.load(imageUrl) {
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
    crossfade(true)
}
```

---

## 🧪 Como Testar

### 1. Teste de Timeout
```
1. Desligar servidor backend
2. Abrir app
3. Tentar carregar dashboard
4. Verificar que erro aparece em ~30s (não 2 minutos)
5. App deve continuar responsivo
```

### 2. Teste de Rede Lenta
```
1. Usar Android Studio Device Manager
2. Configurar "Network Speed: EDGE (slow)"
3. Abrir app
4. Verificar que loading aparece
5. Verificar que app não trava
```

### 3. Teste de Múltiplas Requisições
```
1. Abrir dashboard
2. Fazer pull-to-refresh várias vezes
3. Navegar entre telas rapidamente
4. Verificar que não há ANR
```

### 4. Teste de Token Expirado
```
1. Fazer login
2. Esperar token expirar (ou forçar expiração)
3. Fazer requisição
4. Verificar que renovação funciona
5. Verificar que não há ANR
```

---

## 📝 Checklist de Validação

- [x] Removido `runBlocking` do AuthInterceptor
- [x] Reduzidos timeouts do OkHttpClient
- [ ] Testar app em dispositivo real
- [ ] Verificar logs de ANR (adb logcat)
- [ ] Testar com rede lenta
- [ ] Testar com servidor offline
- [ ] Validar que coletas funcionam
- [ ] Validar que sincronização funciona

---

## 🚀 Próximos Passos

1. **Rebuild do app:**
   ```bash
   cd InventarioMobile
   ./gradlew clean assembleDebug
   ```

2. **Instalar no dispositivo:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Monitorar logs:**
   ```bash
   adb logcat | grep -E "ANR|DashboardFragment|ApiModule"
   ```

4. **Testar fluxos críticos:**
   - Login
   - Carregar dashboard
   - Fazer coleta
   - Sincronizar dados

---

**Implementado em:** 17/11/2025
**Versão:** 2.0.1
**Status:** ✅ Correções aplicadas, aguardando testes

