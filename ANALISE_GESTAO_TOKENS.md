# Análise: Gestão de Tokens e Redirecionamento para Login

## 📋 Situação Atual

### ✅ O Que Já Existe

1. **PreferencesManager** - Métodos de validação de token
   ```kotlin
   fun isTokenExpired(): Boolean
   fun isTokenExpiringSoon(): Boolean  
   fun getRefreshToken(): String?
   fun saveTokens(accessToken, refreshToken, expiresIn)
   ```

2. **RefreshTokenInterceptor** - Interceptor para renovação automática
   ```kotlin
   @Singleton
   class RefreshTokenInterceptor @Inject constructor(
       private val preferencesManager: PreferencesManager
   ) : Interceptor {
       // Verifica token expirando
       // Renova automaticamente em 401
       // Retry automático após renovação
   }
   ```

### ❌ O Que Está Faltando

1. **RefreshTokenInterceptor NÃO está sendo usado**
   - Não está adicionado ao OkHttpClient
   - Tokens não são renovados automaticamente
   
2. **Sem redirecionamento para login**
   - Quando token expira completamente
   - Quando refresh token também expira
   - Quando renovação falha

3. **Sem verificação na inicialização**
   - App não verifica token ao abrir
   - Pode tentar usar token expirado

---

## 🔧 Solução Completa

### Passo 1: Adicionar RefreshTokenInterceptor ao NetworkModule

**Problema:** Interceptor existe mas não é usado

**Solução:**

```kotlin
// NetworkModule.kt
private fun createOkHttpClient(...): OkHttpClient {
    
    // ✅ Criar RefreshTokenInterceptor
    val refreshTokenInterceptor = RefreshTokenInterceptor(preferencesManager)
    
    return OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(refreshTokenInterceptor)  // ✅ ADICIONAR AQUI
        .addInterceptor(timeoutFallbackInterceptor)
        .addInterceptor(rawResponseInterceptor)
        .addInterceptor(deviceInfoInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
}
```

### Passo 2: Melhorar RefreshTokenInterceptor

**Adicionar callback para falha de renovação:**

```kotlin
class RefreshTokenInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val onTokenRefreshFailed: (() -> Unit)? = null // ✅ Callback
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        // ... código existente ...
        
        if (response.code == 401) {
            val refreshSuccess = tryRefreshToken(chain)
            
            if (!refreshSuccess) {
                // ✅ Token não pode ser renovado - notificar
                onTokenRefreshFailed?.invoke()
                
                // Limpar dados de sessão
                preferencesManager.clearSavedUser()
                preferencesManager.clearSessionData()
            }
        }
        
        return response
    }
}
```

### Passo 3: Criar TokenManager

**Gerenciador centralizado de tokens:**

```kotlin
@Singleton
class TokenManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "TokenManager"
    }
    
    /**
     * Verifica se o token é válido
     */
    fun isTokenValid(): Boolean {
        val token = preferencesManager.getAccessToken()
        
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "Token não encontrado")
            return false
        }
        
        if (preferencesManager.isTokenExpired()) {
            Log.d(TAG, "Token expirado")
            return false
        }
        
        return true
    }
    
    /**
     * Verifica token e redireciona para login se inválido
     * @return true se token é válido, false se redirecionou para login
     */
    fun validateTokenOrRedirectToLogin(): Boolean {
        if (!isTokenValid()) {
            Log.w(TAG, "Token inválido, redirecionando para login...")
            redirectToLogin()
            return false
        }
        return true
    }
    
    /**
     * Redireciona para tela de login
     */
    fun redirectToLogin() {
        // Limpar dados de sessão
        preferencesManager.clearSavedUser()
        preferencesManager.clearSessionData()
        
        // Criar intent para LoginActivity
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("token_expired", true)
            putExtra("message", "Sua sessão expirou. Faça login novamente.")
        }
        
        context.startActivity(intent)
    }
    
    /**
     * Verifica se pode renovar token
     */
    fun canRefreshToken(): Boolean {
        val refreshToken = preferencesManager.getRefreshToken()
        return !refreshToken.isNullOrEmpty()
    }
}
```

### Passo 4: Verificar Token na Inicialização

**SplashActivity:**

```kotlin
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            delay(SPLASH_DELAY)
            
            // ✅ Verificar token antes de navegar
            if (preferencesManager.isLoggedIn()) {
                if (tokenManager.isTokenValid()) {
                    // Token válido, ir para MainActivity
                    navigateToMain()
                } else if (tokenManager.canRefreshToken()) {
                    // Tentar renovar token
                    tryRefreshTokenAndNavigate()
                } else {
                    // Token inválido e não pode renovar, ir para login
                    navigateToLogin()
                }
            } else {
                // Não está logado, ir para login
                navigateToLogin()
            }
        }
    }
}
```

### Passo 5: Verificar Token em Activities Críticas

**BaseActivity:**

```kotlin
abstract class BaseActivity : AppCompatActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    override fun onResume() {
        super.onResume()
        
        // ✅ Verificar token quando Activity volta ao foreground
        if (!tokenManager.validateTokenOrRedirectToLogin()) {
            // Token inválido, já redirecionou para login
            return
        }
    }
}
```

### Passo 6: Tratar Erro 401 Globalmente

**Criar UnauthorizedInterceptor:**

```kotlin
@Singleton
class UnauthorizedInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // Se receber 401 após tentativa de refresh
        if (response.code == 401) {
            Log.w("UnauthorizedInterceptor", "401 Unauthorized - Redirecionando para login")
            
            // Redirecionar para login
            tokenManager.redirectToLogin()
        }
        
        return response
    }
}
```

---

## 🎯 Fluxo Completo

### Fluxo 1: App Inicia

```
1. SplashActivity abre
   ↓
2. Verifica se está logado
   ↓
3. ✅ Verifica se token é válido
   ↓
4a. Token válido → MainActivity
4b. Token expirado mas tem refresh → Renova → MainActivity
4c. Token expirado sem refresh → LoginActivity
```

### Fluxo 2: Requisição HTTP

```
1. App faz requisição HTTP
   ↓
2. RefreshTokenInterceptor verifica token
   ↓
3a. Token válido → Continua
3b. Token expirando → Renova preventivamente → Continua
3c. Recebe 401 → Tenta renovar → Retry
3d. Renovação falha → Redireciona para login
```

### Fluxo 3: Activity Volta ao Foreground

```
1. Usuário volta para o app
   ↓
2. onResume() é chamado
   ↓
3. ✅ Verifica token
   ↓
4a. Token válido → Continua
4b. Token inválido → Redireciona para login
```

---

## 📊 Comparação Antes/Depois

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Verificação de Token** | ❌ Não verifica | ✅ Verifica sempre |
| **Renovação Automática** | ❌ Não funciona | ✅ Funciona |
| **Redirecionamento** | ❌ Não redireciona | ✅ Redireciona |
| **Verificação na Inicialização** | ❌ Não verifica | ✅ Verifica |
| **Tratamento de 401** | ❌ Apenas erro | ✅ Renova ou redireciona |
| **Experiência do Usuário** | ❌ Ruim (crashes) | ✅ Boa (transparente) |

---

## 🧪 Como Testar

### Teste 1: Token Expirando

```
1. Fazer login
2. Aguardar token estar próximo de expirar (< 5 min)
3. Fazer uma requisição
4. ✅ Deve renovar automaticamente
5. ✅ Requisição deve funcionar
```

### Teste 2: Token Expirado

```
1. Fazer login
2. Aguardar token expirar completamente
3. Fazer uma requisição
4. ✅ Deve tentar renovar
5a. Se renovação funcionar → Continua
5b. Se renovação falhar → Redireciona para login
```

### Teste 3: Refresh Token Expirado

```
1. Fazer login
2. Aguardar refresh token expirar (7 dias)
3. Abrir app
4. ✅ Deve redirecionar para login
5. ✅ Deve mostrar mensagem "Sessão expirou"
```

### Teste 4: App em Background

```
1. Fazer login
2. Deixar app em background por 1 dia
3. Voltar para o app
4. ✅ Deve verificar token
5a. Token válido → Continua
5b. Token expirado → Redireciona para login
```

---

## 🚀 Implementação Recomendada

### Prioridade ALTA

1. ✅ Adicionar RefreshTokenInterceptor ao OkHttpClient
2. ✅ Criar TokenManager
3. ✅ Verificar token no SplashActivity
4. ✅ Adicionar redirecionamento para login

### Prioridade MÉDIA

1. Criar BaseActivity com verificação de token
2. Adicionar UnauthorizedInterceptor
3. Melhorar mensagens de erro

### Prioridade BAIXA

1. Adicionar analytics de expiração de token
2. Notificar usuário antes de expirar
3. Adicionar testes automatizados

---

## 📝 Código Pronto para Implementar

Vou criar os arquivos necessários na próxima mensagem se você quiser que eu implemente isso.

---

## 🎯 Conclusão

### Situação Atual: ⚠️ PARCIAL

- ✅ Código existe (PreferencesManager, RefreshTokenInterceptor)
- ❌ Não está sendo usado
- ❌ Sem redirecionamento para login
- ❌ Sem verificação na inicialização

### Após Implementação: ✅ COMPLETO

- ✅ Renovação automática de token
- ✅ Redirecionamento para login quando necessário
- ✅ Verificação em todas as etapas
- ✅ Experiência transparente para usuário

---

**Quer que eu implemente essas melhorias agora?**

