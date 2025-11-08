# 🚨 Plano de Ação Imediato - Segurança Crítica

## 🎯 Objetivo
Implementar as medidas de segurança CRÍTICAS nas próximas 3-4 semanas para tornar o aplicativo seguro para uso em produção.

---

## 📅 SEMANA 1: Criptografia e Armazenamento Seguro

### Dia 1-2: EncryptedSharedPreferences
**Prioridade**: 🔴 CRÍTICA

```kotlin
// Implementar armazenamento seguro
class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        encryptedPrefs.edit().putString("jwt_token", token).apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString("jwt_token", null)
    }
    
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }
}
```

**Tarefas**:
- [ ] Adicionar dependência: `androidx.security:security-crypto:1.1.0-alpha06`
- [ ] Criar classe SecureStorage
- [ ] Migrar tokens JWT para armazenamento criptografado
- [ ] Migrar credenciais salvas (se houver)
- [ ] Testar em diferentes dispositivos

### Dia 3-4: Criptografia do Banco SQLite
**Prioridade**: 🔴 CRÍTICA

```kotlin
// Usar SQLCipher para criptografar banco
dependencies {
    implementation "net.zetetic:android-database-sqlcipher:4.5.4"
}

// Configurar Room com SQLCipher
Room.databaseBuilder(context, AppDatabase::class.java, "inventario.db")
    .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes("sua_chave_segura".toCharArray())))
    .build()
```

**Tarefas**:
- [ ] Adicionar SQLCipher ao projeto
- [ ] Gerar chave de criptografia segura
- [ ] Configurar Room com SQLCipher
- [ ] Migrar banco existente
- [ ] Testar performance

### Dia 5: Limpeza de Dados Sensíveis
**Prioridade**: 🔴 CRÍTICA

**Tarefas**:
- [ ] Remover logs de senhas/tokens
- [ ] Implementar ofuscação de dados em logs
- [ ] Limpar dados ao fazer logout
- [ ] Configurar android:allowBackup="false"

---

## 📅 SEMANA 2: Comunicação Segura

### Dia 1-2: Certificate Pinning
**Prioridade**: 🔴 CRÍTICA

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    
    <domain-config>
        <domain includeSubdomains="true">api.seuservidor.com</domain>
        <pin-set expiration="2025-12-31">
            <pin digest="SHA-256">HASH_DO_SEU_CERTIFICADO</pin>
            <pin digest="SHA-256">HASH_BACKUP</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

**Tarefas**:
- [ ] Criar network_security_config.xml
- [ ] Obter hash do certificado SSL do servidor
- [ ] Configurar certificate pinning
- [ ] Adicionar certificado backup
- [ ] Testar conexão
- [ ] Implementar tratamento de erro de pinning

### Dia 3-4: Validação de Certificado SSL
**Prioridade**: 🔴 CRÍTICA

```kotlin
// Configurar OkHttp com validação SSL
val client = OkHttpClient.Builder()
    .certificatePinner(
        CertificatePinner.Builder()
            .add("api.seuservidor.com", "sha256/HASH")
            .build()
    )
    .hostnameVerifier { hostname, session ->
        // Validação adicional
        hostname == "api.seuservidor.com"
    }
    .build()
```

**Tarefas**:
- [ ] Configurar OkHttp com CertificatePinner
- [ ] Implementar HostnameVerifier
- [ ] Forçar TLS 1.2+
- [ ] Testar com certificados inválidos
- [ ] Documentar processo

### Dia 5: Timeout e Retry Seguro
**Prioridade**: 🟡 IMPORTANTE

**Tarefas**:
- [ ] Configurar timeouts adequados (30s)
- [ ] Implementar retry com backoff exponencial
- [ ] Limitar tentativas de retry
- [ ] Cancelar requisições ao sair da tela

---

## 📅 SEMANA 3: Proteção de Código e Validação

### Dia 1-2: ProGuard/R8
**Prioridade**: 🔴 CRÍTICA

```groovy
// build.gradle (app)
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

```proguard
# proguard-rules.pro
-dontoptimize
-keepattributes *Annotation*

# Ofuscar classes sensíveis
-keep class com.inventario.mobile.data.model.** { *; }
-keep class com.inventario.mobile.security.** { *; }

# Remover logs de debug
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Manter classes do Retrofit
-keepattributes Signature
-keep class retrofit2.** { *; }
```

**Tarefas**:
- [ ] Habilitar minifyEnabled
- [ ] Configurar proguard-rules.pro
- [ ] Testar build release
- [ ] Verificar ofuscação com APK Analyzer
- [ ] Corrigir erros de runtime

### Dia 3-4: Validação de Entrada
**Prioridade**: 🔴 CRÍTICA

```kotlin
// Validador de entrada
object InputValidator {
    fun validatePatrimonioNumber(numero: String): Boolean {
        return numero.matches(Regex("^[0-9]{1,10}$"))
    }
    
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"']"), "")
            .take(255)
    }
    
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
```

**Tarefas**:
- [ ] Criar classe InputValidator
- [ ] Validar todos os campos de entrada
- [ ] Sanitizar dados antes de enviar para API
- [ ] Implementar validação no ViewModel
- [ ] Adicionar mensagens de erro claras

### Dia 5: Remover Hardcoded Secrets
**Prioridade**: 🔴 CRÍTICA

```kotlin
// NÃO FAZER:
val apiKey = "abc123xyz" // ❌

// FAZER:
val apiKey = BuildConfig.API_KEY // ✅
```

```groovy
// build.gradle
android {
    defaultConfig {
        buildConfigField "String", "API_KEY", "\"${System.getenv("API_KEY")}\""
    }
}
```

**Tarefas**:
- [ ] Identificar todos os secrets hardcoded
- [ ] Mover para BuildConfig
- [ ] Usar variáveis de ambiente
- [ ] Adicionar .env ao .gitignore
- [ ] Documentar configuração

---

## 📅 SEMANA 4: Autenticação e Sessão

### Dia 1-2: Política de Senha Forte
**Prioridade**: 🔴 CRÍTICA

```kotlin
object PasswordValidator {
    private const val MIN_LENGTH = 8
    
    fun validate(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < MIN_LENGTH) {
            errors.add("Mínimo $MIN_LENGTH caracteres")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("Pelo menos uma letra maiúscula")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Pelo menos um número")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Pelo menos um caractere especial")
        }
        
        return if (errors.isEmpty()) {
            PasswordValidationResult.Valid
        } else {
            PasswordValidationResult.Invalid(errors)
        }
    }
}
```

**Tarefas**:
- [ ] Implementar PasswordValidator
- [ ] Adicionar validação na tela de login
- [ ] Mostrar requisitos de senha
- [ ] Indicador de força de senha
- [ ] Forçar mudança de senha fraca

### Dia 3-4: Timeout de Sessão
**Prioridade**: 🔴 CRÍTICA

```kotlin
class SessionManager(private val context: Context) {
    private val TIMEOUT_MINUTES = 15L
    private var lastActivityTime = System.currentTimeMillis()
    
    fun updateActivity() {
        lastActivityTime = System.currentTimeMillis()
    }
    
    fun isSessionExpired(): Boolean {
        val elapsed = System.currentTimeMillis() - lastActivityTime
        return elapsed > TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES)
    }
    
    fun logout() {
        // Limpar tokens
        // Redirecionar para login
    }
}
```

**Tarefas**:
- [ ] Criar SessionManager
- [ ] Implementar timeout de 15 minutos
- [ ] Monitorar atividade do usuário
- [ ] Logout automático
- [ ] Salvar estado antes de logout

### Dia 5: Bloqueio após Tentativas Falhadas
**Prioridade**: 🔴 CRÍTICA

```kotlin
class LoginAttemptManager(context: Context) {
    private val prefs = context.getSharedPreferences("login_attempts", Context.MODE_PRIVATE)
    private val MAX_ATTEMPTS = 5
    private val LOCKOUT_DURATION = TimeUnit.MINUTES.toMillis(15)
    
    fun recordFailedAttempt() {
        val attempts = prefs.getInt("attempts", 0) + 1
        prefs.edit()
            .putInt("attempts", attempts)
            .putLong("last_attempt", System.currentTimeMillis())
            .apply()
    }
    
    fun isLocked(): Boolean {
        val attempts = prefs.getInt("attempts", 0)
        val lastAttempt = prefs.getLong("last_attempt", 0)
        
        if (attempts >= MAX_ATTEMPTS) {
            val elapsed = System.currentTimeMillis() - lastAttempt
            return elapsed < LOCKOUT_DURATION
        }
        return false
    }
    
    fun reset() {
        prefs.edit().clear().apply()
    }
}
```

**Tarefas**:
- [ ] Criar LoginAttemptManager
- [ ] Limitar a 5 tentativas
- [ ] Bloquear por 15 minutos
- [ ] Mostrar tempo restante
- [ ] Resetar após login bem-sucedido

---

## 📅 VERIFICAÇÃO E TESTES

### Checklist de Segurança
```
Criptografia:
- [ ] Tokens criptografados
- [ ] Banco de dados criptografado
- [ ] Sem dados sensíveis em logs
- [ ] Backup desabilitado ou criptografado

Comunicação:
- [ ] HTTPS em todas as requisições
- [ ] Certificate pinning ativo
- [ ] TLS 1.2+ forçado
- [ ] Timeouts configurados

Código:
- [ ] ProGuard/R8 ativo
- [ ] Sem secrets hardcoded
- [ ] Validação de entrada
- [ ] Logs de debug removidos

Autenticação:
- [ ] Política de senha forte
- [ ] Timeout de sessão
- [ ] Bloqueio após tentativas
- [ ] Logout seguro
```

### Testes de Segurança
```
- [ ] Teste de penetração básico
- [ ] Análise estática com MobSF
- [ ] Verificação de dependências vulneráveis
- [ ] Teste de MITM (Man-in-the-Middle)
- [ ] Teste de injeção SQL
- [ ] Teste de XSS (se houver WebView)
```

---

## 📊 MÉTRICAS DE SUCESSO

### Antes da Implementação
- 🔴 Nível de Segurança: BAIXO (30%)
- 🔴 Vulnerabilidades Críticas: 8-10
- 🔴 Conformidade OWASP: 40%

### Após Implementação (Meta)
- 🟢 Nível de Segurança: ALTO (85%)
- 🟢 Vulnerabilidades Críticas: 0-1
- 🟢 Conformidade OWASP: 85%

---

## 🚀 PRÓXIMOS PASSOS (Após 4 Semanas)

1. **Autenticação Biométrica** (1-2 semanas)
2. **Detecção de Root** (3-5 dias)
3. **2FA** (1 semana)
4. **Auditoria Completa** (1 semana)
5. **Certificação de Segurança** (2-3 semanas)

---

## 💰 ESTIMATIVA DE ESFORÇO

| Semana | Foco | Horas | Complexidade |
|--------|------|-------|--------------|
| 1 | Criptografia | 30-35h | Alta |
| 2 | Comunicação | 25-30h | Média |
| 3 | Código | 25-30h | Média |
| 4 | Autenticação | 30-35h | Alta |
| **Total** | | **110-130h** | |

**Equivalente**: 3-4 semanas de trabalho full-time

---

## ✅ CONCLUSÃO

Este plano cobre as **vulnerabilidades CRÍTICAS** que devem ser corrigidas antes de colocar o aplicativo em produção. Após essas 4 semanas, o app estará em um nível de segurança **ACEITÁVEL** para uso institucional.

**Status Atual**: 🔴 NÃO SEGURO PARA PRODUÇÃO
**Status Após Plano**: 🟡 SEGURO PARA PRODUÇÃO (com ressalvas)
**Status Ideal**: 🟢 TOTALMENTE SEGURO (após implementações adicionais)
