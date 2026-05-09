# Implementação: Login Biométrico com Renovação de Token

## 📋 Problema Identificado

Atualmente, quando o usuário faz login com biometria, o app apenas usa o **token antigo** salvo localmente, sem gerar um novo token válido no servidor.

### ❌ Problemas:
1. Token pode estar **expirado**
2. Não há **renovação de sessão** no servidor
3. Servidor não sabe que usuário fez login novamente
4. Requisições podem falhar com erro 401 (Unauthorized)

---

## ✅ Solução Implementada

### 1. Novo Use Case: `RenovarTokenComBiometriaUseCase`

**Localização:** `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/usecase/RenovarTokenComBiometriaUseCase.kt`

**Fluxo:**
```
1. Usuário autentica com biometria ✅
2. App pega refresh token salvo localmente
3. App chama /api/mobile/auth/refresh no servidor
4. Servidor valida refresh token
5. Servidor gera novo access token válido
6. App salva novo token localmente
7. App retorna dados do usuário atualizados
```

**Benefícios:**
- ✅ Token sempre válido após login biométrico
- ✅ Sessão renovada no servidor
- ✅ Segurança mantida (biometria + token válido)
- ✅ Fallback para offline se servidor inacessível

---

## 🔧 Implementação

### Passo 1: Adicionar AuthApi (✅ CRIADO)

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/api/AuthApi.kt`

```kotlin
interface AuthApi {
    @POST("api/mobile/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("api/mobile/auth/refresh")
    suspend fun refreshToken(@Query("refreshToken") refreshToken: String): Response<LoginResponse>
}
```

### Passo 2: Criar Use Case (✅ CRIADO)

**Arquivo:** `RenovarTokenComBiometriaUseCase.kt`

Já criado com toda a lógica de renovação de token.

### Passo 3: Atualizar LoginViewModel

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/login/LoginViewModel.kt`

**ANTES:**
```kotlin
fun loginWithBiometric() {
    viewModelScope.launch {
        // Apenas carrega dados locais (token antigo)
        val username = preferencesManager.getSavedUsername()
        val fullName = preferencesManager.getSavedUserFullName()
        val token = preferencesManager.getAccessToken()
        
        if (username != null && fullName != null && token != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccessful = true,
                offlineMode = true
            )
        }
    }
}
```

**DEPOIS:**
```kotlin
// Adicionar no construtor
private val renovarTokenComBiometriaUseCase: RenovarTokenComBiometriaUseCase

fun loginWithBiometric() {
    viewModelScope.launch {
        try {
            Log.d("LoginViewModel", "═══════════════════════════════════════════")
            Log.d("LoginViewModel", "INICIANDO LOGIN COM BIOMETRIA")
            
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Tentar renovar token no servidor
            renovarTokenComBiometriaUseCase().fold(
                onSuccess = { result ->
                    Log.d("LoginViewModel", "✅ LOGIN BIOMÉTRICO BEM-SUCEDIDO")
                    Log.d("LoginViewModel", "Usuário: ${result.fullName}")
                    Log.d("LoginViewModel", "Modo: ${if (result.isOnline) "ONLINE" else "OFFLINE"}")
                    Log.d("LoginViewModel", "Token renovado: ${result.isOnline}")
                    Log.d("LoginViewModel", "═══════════════════════════════════════════")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        offlineMode = !result.isOnline,
                        savedUserName = result.fullName
                    )
                },
                onFailure = { error ->
                    Log.e("LoginViewModel", "❌ FALHA NO LOGIN BIOMÉTRICO")
                    Log.e("LoginViewModel", "Erro: ${error.message}")
                    Log.e("LoginViewModel", "═══════════════════════════════════════════")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao fazer login com biometria"
                    )
                }
            )
        } catch (e: Exception) {
            Log.e("LoginViewModel", "❌ ERRO INESPERADO", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro inesperado: ${e.message}"
            )
        }
    }
}
```

### Passo 4: Adicionar Métodos no PreferencesManager

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/utils/PreferencesManager.kt`

Verificar se já existem estes métodos:

```kotlin
fun getRefreshToken(): String? {
    return sharedPreferences.getString("refresh_token", null)
}

fun saveRefreshToken(refreshToken: String) {
    sharedPreferences.edit()
        .putString("refresh_token", refreshToken)
        .apply()
}
```

Se não existirem, adicionar.

### Passo 5: Configurar Hilt (se necessário)

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/di/ApiModule.kt`

Adicionar provider para AuthApi:

```kotlin
@Provides
@Singleton
fun provideAuthApi(retrofit: Retrofit): AuthApi {
    return retrofit.create(AuthApi::class.java)
}
```

---

## 🧪 Como Testar

### Teste 1: Login Biométrico Online
```
1. Fazer login normal com usuário/senha
2. Habilitar biometria
3. Fazer logout
4. Fazer login com biometria
5. Verificar logs:
   - "RENOVANDO TOKEN COM BIOMETRIA"
   - "✅ Token renovado com sucesso!"
   - "Modo: ONLINE"
6. Fazer requisição ao servidor
7. Verificar que funciona (token válido)
```

### Teste 2: Login Biométrico Offline
```
1. Fazer login normal com usuário/senha
2. Habilitar biometria
3. Fazer logout
4. Desconectar internet
5. Fazer login com biometria
6. Verificar logs:
   - "Tentando fallback para login offline..."
   - "✓ Login offline com token antigo"
   - "Modo: OFFLINE"
7. App deve funcionar em modo offline
```

### Teste 3: Token Expirado
```
1. Fazer login normal
2. Habilitar biometria
3. Fazer logout
4. Esperar token expirar (ou simular)
5. Fazer login com biometria
6. Verificar que novo token é gerado
7. Requisições devem funcionar
```

---

## 📊 Comparação: Antes vs Depois

### ANTES
```
Biometria ✅
    ↓
Carrega token antigo do storage
    ↓
Login "bem-sucedido"
    ↓
Requisições FALHAM (token expirado) ❌
```

### DEPOIS
```
Biometria ✅
    ↓
Chama /api/mobile/auth/refresh
    ↓
Servidor valida refresh token
    ↓
Servidor gera novo access token ✅
    ↓
App salva novo token
    ↓
Login bem-sucedido
    ↓
Requisições FUNCIONAM (token válido) ✅
```

---

## 🔐 Segurança

### Tokens Salvos Localmente
- **Access Token**: Token de curta duração (ex: 24h)
- **Refresh Token**: Token de longa duração (ex: 7 dias)

### Fluxo de Segurança
1. **Login inicial**: Usuário/senha → Servidor gera access + refresh token
2. **Habilitar biometria**: Salva tokens localmente (criptografados)
3. **Login biométrico**: 
   - Biometria valida identidade do usuário
   - Refresh token renova access token no servidor
   - Novo access token é salvo
4. **Requisições**: Usa access token válido

### Benefícios de Segurança
- ✅ Biometria garante identidade do usuário
- ✅ Refresh token permite renovação sem senha
- ✅ Access token sempre válido
- ✅ Servidor controla sessões ativas
- ✅ Tokens podem ser revogados remotamente

---

## 📝 Checklist de Implementação

- [x] Criar `AuthApi.kt` com endpoint de refresh
- [x] Criar `RenovarTokenComBiometriaUseCase.kt`
- [ ] Adicionar Use Case no construtor do `LoginViewModel`
- [ ] Atualizar método `loginWithBiometric()` no `LoginViewModel`
- [ ] Verificar métodos de refresh token no `PreferencesManager`
- [ ] Configurar provider do `AuthApi` no Hilt (se necessário)
- [ ] Testar login biométrico online
- [ ] Testar login biométrico offline
- [ ] Testar renovação de token expirado
- [ ] Documentar mudanças no CHANGELOG

---

## 🚀 Próximos Passos

1. **Implementar no LoginViewModel** (próximo passo)
2. **Testar em dispositivo real**
3. **Adicionar renovação automática** (interceptor)
4. **Monitorar expiração de tokens**
5. **Adicionar métricas de uso**

---

**Implementado em:** 20/01/2026  
**Versão:** 2.16.0  
**Status:** ⚠️ Parcialmente implementado (Use Case criado, falta integrar no ViewModel)

