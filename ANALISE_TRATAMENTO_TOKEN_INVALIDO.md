# 🔐 Análise: Tratamento de Token Inválido

## 📊 Comportamento Atual

### ❌ Problema Identificado

Atualmente, quando o token expira ou se torna inválido:

1. **RefreshTokenInterceptor** tenta renovar automaticamente
2. Se a renovação **falhar**, retorna erro 401
3. **NÃO há redirecionamento automático para tela de login**
4. Usuário fica "preso" com token inválido

### Fluxo Atual

```
Token Expira
    ↓
Requisição retorna 401
    ↓
RefreshTokenInterceptor tenta renovar
    ↓
Renovação FALHA (refresh token também inválido)
    ↓
Retorna 401 para o app
    ↓
❌ Usuário continua "logado" mas não consegue fazer nada
```

---

## 🐛 Problemas

### 1. Usuário Fica Preso
- Token inválido mas app não desloga
- Telas mostram erros genéricos
- Usuário não sabe que precisa fazer login novamente

### 2. Experiência Ruim
- Mensagens de erro confusas
- Não há feedback claro
- Usuário precisa fechar e abrir o app

### 3. Sem Tratamento Global
- Cada tela precisa tratar erro 401
- Código duplicado
- Inconsistência no tratamento

---

## ✅ Solução Proposta

### Implementar Tratamento Global de 401

```kotlin
// 1. Adicionar callback no RefreshTokenInterceptor
// 2. Quando renovação falhar, notificar app
// 3. App limpa sessão e redireciona para login
```

---

## 📦 Implementação

### Fase 1: Adicionar Callback no Interceptor

**Arquivo:** `RefreshTokenInterceptor.kt`

```kotlin
interface TokenExpiredListener {
    fun onTokenExpired()
}

class RefreshTokenInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    
    var tokenExpiredListener: TokenExpiredListener? = null
    
    private fun tryRefreshToken(chain: Interceptor.Chain): Boolean {
        // ... código existente ...
        
        if (!refreshSuccess) {
            // Token não pode ser renovado
            tokenExpiredListener?.onTokenExpired()
        }
        
        return refreshSuccess
    }
}
```

### Fase 2: Criar Gerenciador de Sessão

**Arquivo:** `SessionManager.kt` (NOVO)

```kotlin
@Singleton
class SessionManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Limpa sessão e redireciona para login
     */
    fun logout(showMessage: Boolean = true) {
        Log.d(TAG, "Logout: Limpando sessão...")
        
        // Limpar todos os dados de sessão
        preferencesManager.clearSavedUser()
        preferencesManager.clearInventarioAtivo()
        preferencesManager.clearSessionData()
        
        // Redirecionar para login
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                      Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        if (showMessage) {
            intent.putExtra("LOGOUT_MESSAGE", "Sessão expirada. Faça login novamente.")
        }
        
        context.startActivity(intent)
    }
    
    /**
     * Verifica se sessão está válida
     */
    fun isSessionValid(): Boolean {
        val hasToken = preferencesManager.getAccessToken() != null
        val isExpired = preferencesManager.isTokenExpired()
        return hasToken && !isExpired
    }
}
```

### Fase 3: Integrar no Application

**Arquivo:** `InventarioMobileApp.kt`

```kotlin
@HiltAndroidApp
class InventarioMobileApp : Application(), TokenExpiredListener {
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var refreshTokenInterceptor: RefreshTokenInterceptor
    
    override fun onCreate() {
        super.onCreate()
        
        // Registrar listener de token expirado
        refreshTokenInterceptor.tokenExpiredListener = this
    }
    
    override fun onTokenExpired() {
        Log.w(TAG, "Token expirado! Fazendo logout...")
        sessionManager.logout(showMessage = true)
    }
}
```

### Fase 4: Adicionar Verificação nas Activities

**Arquivo:** `BaseActivity.kt` (NOVO)

```kotlin
@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    override fun onResume() {
        super.onResume()
        
        // Verificar se sessão ainda é válida
        if (!sessionManager.isSessionValid()) {
            sessionManager.logout(showMessage = true)
        }
    }
}
```

---

## 🔄 Novo Fluxo

```
Token Expira
    ↓
Requisição retorna 401
    ↓
RefreshTokenInterceptor tenta renovar
    ↓
Renovação FALHA
    ↓
Chama tokenExpiredListener.onTokenExpired()
    ↓
SessionManager.logout()
    ↓
Limpa dados de sessão
    ↓
Redireciona para LoginActivity
    ↓
✅ Usuário vê mensagem: "Sessão expirada. Faça login novamente."
```

---

## 📱 Experiência do Usuário

### Antes (Atual)
```
1. Token expira
2. Usuário tenta fazer algo
3. Vê erro genérico: "Erro ao carregar dados"
4. Tenta novamente, mesmo erro
5. Fica confuso, não sabe o que fazer
6. Precisa fechar e abrir o app
```

### Depois (Proposto)
```
1. Token expira
2. Usuário tenta fazer algo
3. App detecta automaticamente
4. Mostra mensagem clara: "Sessão expirada"
5. Redireciona para tela de login
6. Usuário faz login novamente
7. ✅ Continua usando o app normalmente
```

---

## 🎯 Benefícios

1. **Experiência Clara**
   - Usuário sabe exatamente o que aconteceu
   - Mensagem amigável e informativa

2. **Automático**
   - Não precisa fechar o app
   - Redirecionamento automático

3. **Seguro**
   - Limpa todos os dados de sessão
   - Previne uso com token inválido

4. **Consistente**
   - Tratamento global em um único lugar
   - Todas as telas se beneficiam

---

## 📋 Checklist de Implementação

### Fase 1: Infraestrutura
- [ ] Criar interface `TokenExpiredListener`
- [ ] Adicionar callback no `RefreshTokenInterceptor`
- [ ] Criar `SessionManager`
- [ ] Adicionar método `clearSavedUser()` no `PreferencesManager`

### Fase 2: Integração
- [ ] Implementar listener no `Application`
- [ ] Criar `BaseActivity` com verificação
- [ ] Atualizar Activities principais para herdar de `BaseActivity`

### Fase 3: UI
- [ ] Adicionar mensagem de sessão expirada no `LoginActivity`
- [ ] Criar dialog de confirmação (opcional)
- [ ] Adicionar animação de transição

### Fase 4: Testes
- [ ] Testar com token expirado
- [ ] Testar com refresh token inválido
- [ ] Testar redirecionamento
- [ ] Testar limpeza de dados

---

## ⏱️ Estimativa

| Fase | Tempo |
|------|-------|
| Fase 1 | 1h |
| Fase 2 | 1h |
| Fase 3 | 30min |
| Fase 4 | 30min |
| **Total** | **3h** |

---

## 🚨 Casos de Uso

### Caso 1: Token Expira Durante Uso
```
Usuário está navegando no app
Token expira
Tenta fazer uma coleta
App detecta 401
Redireciona para login
Mostra: "Sessão expirada. Faça login novamente."
```

### Caso 2: Refresh Token Inválido
```
Access token expira
Interceptor tenta renovar
Refresh token também está inválido
Renovação falha
App limpa sessão
Redireciona para login
```

### Caso 3: App Aberto Após Muito Tempo
```
Usuário abre app após 1 semana
Token já expirou
BaseActivity detecta na verificação
Redireciona para login imediatamente
```

---

## 📊 Comparação

| Aspecto | Atual | Proposto |
|---------|-------|----------|
| **Detecção** | Manual em cada tela | Automática global |
| **Feedback** | Erro genérico | Mensagem clara |
| **Ação** | Usuário precisa fechar app | Redirecionamento automático |
| **Segurança** | Dados podem ficar | Limpeza completa |
| **UX** | Confusa | Clara e intuitiva |

---

**Status:** 📋 Análise completa  
**Recomendação:** ✅ Implementar solução proposta  
**Prioridade:** 🔴 Alta (afeta experiência do usuário)
