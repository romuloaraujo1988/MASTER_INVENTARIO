# Documento de Design

## Visão Geral

Esta feature aplica 12 correções de segurança ao SIHCP sem quebrar funcionalidade existente. O escopo abrange o servidor Spring Boot (`sihcp-server`) e o app Android (`InventarioMobile`), combinando **mudanças de código** (filtros, configs, classes novas) com **mudanças operacionais** (variáveis de ambiente, rotação de segredos, rebuild do APK).

### Impacto por tipo de mudança

| Tipo de mudança | Componentes afetados | Reversibilidade |
|---|---|---|
| Configuração versionada (`application-mobile.properties`, `AndroidManifest.xml`, `network_security_config.xml`, `.gitignore`) | Servidor + App | Reversível via Git |
| Novas classes Java (`SecretsBootstrap`, `LoginRateLimiter`) | Servidor | Reversível via Git |
| Novas classes Kotlin (`SqlCipherKeyManager`) | App | Reversível via Git, **mas** rotaciona a passphrase do banco local — dispositivos existentes perderão o cache SQLCipher e precisarão re-sincronizar |
| Operacional — variáveis de ambiente (`DATASOURCE_PASSWORD`, `JWT_SECRET`) | Servidor em produção | Não versionado |
| Operacional — rotação de segredos expostos | PostgreSQL + Keystore | Irreversível (deve rotacionar senha do banco e, idealmente, gerar novo keystore) |
| Operacional — remoção do histórico Git (opcional) | Repositório | Irreversível |

### Estratégia de Deploy

A implementação é dividida em 3 ondas para minimizar risco:

**Onda 1 — Segredos e Git (bloqueia acesso novo, não derruba quem já tem token)**
1. Adicionar entradas no `.gitignore` e remover `keystore.properties` do índice (`git rm --cached`).
2. Criar `application-mobile-local.properties` (não versionado) com os valores atuais.
3. Substituir valores literais em `application-mobile.properties` por `${DATASOURCE_PASSWORD}` e `${JWT_SECRET}`.
4. Definir variáveis de ambiente no ambiente de produção.
5. Rotacionar a senha do PostgreSQL (`Romulo@1919` já está exposta em histórico Git).
6. Gerar novo `JWT_SECRET` (mínimo 32 bytes base64) — rotação força todos os usuários a re-login.
7. Adicionar `SecretsBootstrap` que valida as variáveis no `@PostConstruct` e aborta `SpringApplication.exit()` se estiverem ausentes.

**Onda 2 — Endurecimento do Servidor (sem quebra de contrato)**
8. Criar perfil `application-prod.properties` com `logging.level.com.inventario=INFO`, `api.mobile.rate-limit.enabled=true`, `api.mobile.cors.allowed-origins=...` explícito, `api.mobile.cors.allow-credentials=false`, `jwt.expiration=3600`.
9. Adicionar `LoginRateLimiter` via Bucket4j como `HandlerInterceptor` aplicado a `POST /api/mobile/auth/login`.
10. Remover `logger.info("Token: {}...")` em `MobileAuthController` e `MobileJwtAuthenticationFilter` — substituir por logs de `username` apenas.
11. Validar comprimento mínimo do `JWT_SECRET` (32+ chars) em `JwtUtil` ou `SecretsBootstrap`.

**Onda 3 — Endurecimento do App (requer novo APK)**
12. Criar `SqlCipherKeyManager` usando `MasterKey` + `EncryptedSharedPreferences`. Migração destrutiva: dispositivos apagam `inventario_offline_secure.db` e re-sincronizam.
13. Atualizar `network_security_config.xml`: `<base-config cleartextTrafficPermitted="false">`, remover `<certificates src="user" />` da base-config, manter apenas em `<domain-config>` de desenvolvimento.
14. `AndroidManifest.xml`: remover `android:usesCleartextTraffic="true"`, trocar `android:allowBackup="true"` por `"false"`, remover `SYSTEM_ALERT_WINDOW` e `USE_FULL_SCREEN_INTENT`.
15. Criar `proguard-rules.pro` com regras para Room, Retrofit, Hilt, SQLCipher, Gson. Ativar `minifyEnabled true`, `shrinkResources true`.
16. Preencher `data_extraction_rules.xml` com `<exclude>` para `database/` e `shared_prefs/secure_prefs.xml`.
17. Incrementar `versionCode` e `versionName` para rastrear a release de segurança.

---

## Arquitetura

Diagrama textual do mapa de correções:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          SERVIDOR (sihcp-server)                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  src/main/resources/                                                    │
│   ├── application-mobile.properties ─┐ [R1, R6, R8, R9]                 │
│   │   (placeholders ${...})          │                                  │
│   ├── application-mobile-local.properties (NOVO, NÃO versionado)        │
│   └── application-prod.properties (NOVO) ── [R7, R8, R9]                │
│                                                                         │
│  src/main/java/com/inventario/sihcp/                                    │
│   ├── security/                                                         │
│   │   └── JwtUtil.java ─────────── [R1.6] validação de secret length    │
│   ├── mobile/server/                                                    │
│   │   ├── config/                                                       │
│   │   │   ├── MobileSecurityConfig.java ── [R6] CORS já OK no código    │
│   │   │   ├── SecretsBootstrap.java (NOVO) ── [R1.3, R1.4, R1.6]        │
│   │   │   └── RateLimitConfig.java (NOVO)   ── [R7]                     │
│   │   ├── security/                                                     │
│   │   │   ├── LoginRateLimiter.java (NOVO)  ── [R7] HandlerInterceptor  │
│   │   │   └── MobileJwtAuthenticationFilter.java ── [R8.4] sanitizar    │
│   │   └── controller/                                                   │
│   │       └── MobileAuthController.java ── [R8.4] remover log de token  │
│   └── ...                                                               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                     APP ANDROID (InventarioMobile)                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  app/                                                                   │
│   ├── build.gradle ─────────── [R4] minifyEnabled, shrinkResources      │
│   ├── proguard-rules.pro (NOVO) ── [R4] regras de ofuscação             │
│   ├── keystore.properties ── [R2] remover do índice Git                 │
│   │                                                                     │
│   └── src/main/                                                         │
│       ├── AndroidManifest.xml ── [R5.5, R11.1, R12]                     │
│       ├── res/xml/                                                      │
│       │   ├── network_security_config.xml ── [R5, R10]                  │
│       │   ├── data_extraction_rules.xml ── [R11.2]                      │
│       │   └── backup_rules.xml ── [R11.2]                               │
│       │                                                                 │
│       └── java/com/inventario/mobile/                                   │
│           ├── security/                                                 │
│           │   ├── SecureStorage.kt (EXISTENTE)                          │
│           │   └── SqlCipherKeyManager.kt (NOVO) ── [R3]                 │
│           └── data/local/database/                                      │
│               └── AppDatabase.kt ─────── [R3] remover passphrase hard   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                          REPOSITÓRIO GIT                                │
├─────────────────────────────────────────────────────────────────────────┤
│  .gitignore (raiz) ─── [R1.5, R2.1, R2.4] adicionar .env, *-local.*     │
└─────────────────────────────────────────────────────────────────────────┘
```

### Fluxo da inicialização segura do servidor

```
┌──────────┐
│ mvn boot │
└────┬─────┘
     │
     ▼
┌────────────────────────────────────────────┐
│ Spring resolve ${DATASOURCE_PASSWORD}      │
│ Spring resolve ${JWT_SECRET}               │
└────┬───────────────────────────────────────┘
     │
     ▼
┌────────────────────────────────────────────┐
│ SecretsBootstrap.@PostConstruct            │
│  - assert DATASOURCE_PASSWORD != null/""   │
│  - assert JWT_SECRET != null/""            │
│  - assert JWT_SECRET.length() >= 32        │
└────┬───────────────────────────────┬───────┘
     │ falha                         │ ok
     ▼                               ▼
┌─────────────────────┐      ┌────────────────┐
│ log.error(...)      │      │ continua boot  │
│ System.exit(1)      │      │ do Spring      │
└─────────────────────┘      └────────────────┘
```

### Fluxo da passphrase SQLCipher no app

```
┌───────────────────┐
│ AppDatabase.get() │
└────────┬──────────┘
         │
         ▼
┌──────────────────────────────────┐
│ SqlCipherKeyManager.getOrCreate()│
└────────┬─────────────────────────┘
         │
         ▼
    EncryptedSharedPreferences
    (key=sqlcipher_passphrase)
         │
         ├── existe? ─── sim ──► retorna byte[32]
         │
         └── não ──► SecureRandom.nextBytes(32)
                    salva em EncryptedSharedPrefs
                    retorna byte[32]

O byte[32] é passado a SupportFactory do SQLCipher.
```

---

## Componentes e Interfaces

Cada correção documenta arquivo afetado, diff conceitual e, quando aplicável, nova classe.

### R1 — Credenciais do Servidor via Variáveis de Ambiente

**Arquivo:** `src/main/resources/application-mobile.properties`

ANTES:
```properties
spring.datasource.password=Romulo@1919
jwt.secret=inventario-mobile-secret-key-2024-very-long-secret-for-security-must-be-at-least-256-bits
```

DEPOIS:
```properties
spring.datasource.password=${DATASOURCE_PASSWORD:}
jwt.secret=${JWT_SECRET:}
# Configurações de desenvolvimento local devem ir em application-mobile-local.properties
# (arquivo NÃO versionado — ver .gitignore)
```

O sufixo `:` (vazio) faz o Spring injetar string vazia quando a variável não existir, permitindo que `SecretsBootstrap` capture a falha com mensagem clara em vez de um `IllegalArgumentException` críptico do Hibernate.

**Arquivo NOVO:** `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/config/SecretsBootstrap.java`

```java
@Configuration
public class SecretsBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SecretsBootstrap.class);
    private static final int JWT_SECRET_MIN_LENGTH = 32;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    private final ApplicationContext ctx;

    public SecretsBootstrap(ApplicationContext ctx) { this.ctx = ctx; }

    @PostConstruct
    public void validate() {
        List<String> errors = new ArrayList<>();
        if (datasourcePassword == null || datasourcePassword.isBlank()) {
            errors.add("Variável de ambiente DATASOURCE_PASSWORD não definida");
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            errors.add("Variável de ambiente JWT_SECRET não definida");
        } else if (jwtSecret.length() < JWT_SECRET_MIN_LENGTH) {
            errors.add("JWT_SECRET muito curto: " + jwtSecret.length()
                       + " chars (mínimo: " + JWT_SECRET_MIN_LENGTH + ")");
        }
        if (!errors.isEmpty()) {
            log.error("════════════════════════════════════════════════════════");
            log.error("FALHA DE BOOTSTRAP — CONFIGURAÇÃO DE SEGREDOS INCOMPLETA");
            errors.forEach(e -> log.error("  ✗ {}", e));
            log.error("════════════════════════════════════════════════════════");
            SpringApplication.exit(ctx, () -> 1);
            System.exit(1);
        }
        log.info("SecretsBootstrap OK — DATASOURCE_PASSWORD e JWT_SECRET carregados do ambiente");
    }
}
```

**Arquivo NOVO:** `application-mobile-local.properties` (não versionado)

```properties
# Cópia privada para desenvolvimento local.
# Nunca commitar. Ver .gitignore.
spring.datasource.password=Romulo@1919
jwt.secret=coloque-aqui-um-segredo-de-pelo-menos-32-caracteres-forte
```

O Spring Boot carrega automaticamente `application-{profile}.properties`. Para desenvolvedores locais, executar com `--spring.profiles.active=mobile,local` ou usar variáveis de ambiente diretamente.

### R1.5 + R2.1 + R2.4 — Endurecer o `.gitignore`

**Arquivo:** `.gitignore` (raiz)

ANTES (já cobre *.keystore, *.jks, keystore.properties):
```
# Keystore credentials (NUNCA versionar)
keystore.properties
*.keystore
*.jks
*.keystore.backup
```

DEPOIS (adicionar):
```
# Keystore credentials (NUNCA versionar)
keystore.properties
InventarioMobile/keystore.properties
*.keystore
*.jks
*.keystore.backup

# Secrets e configurações locais (NUNCA versionar)
.env
.env.*
application-*-local.properties
application-local.properties
src/main/resources/application-mobile-local.properties
secrets/
*.pem
*.p12
```

**Ação operacional (fora do diff):**
```bash
git rm --cached InventarioMobile/keystore.properties
git commit -m "security: remove keystore.properties from index"
```
O arquivo permanece em disco para builds locais, mas para de ser rastreado.

### R2.2 e R2.3 — Build de Release Falha sem `keystore.properties`

**Arquivo:** `InventarioMobile/app/build.gradle`

O bloco atual JÁ está correto (lança `RuntimeException` se o arquivo não existir). Validar que o código existente permanece:

```groovy
signingConfigs {
    release {
        if (keystorePropertiesFile.exists()) {
            storeFile rootProject.file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        } else {
            throw new RuntimeException("❌ keystore.properties não encontrado! ...")
        }
    }
}
```

Nenhuma mudança necessária aqui — apenas garantir que o arquivo deixe de ser versionado (R2.1).

### R3 — Passphrase do SQLCipher via Android Keystore

**Arquivo afetado:** `InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/database/AppDatabase.kt`

ANTES:
```kotlin
val passphrase = "inventario_secure_key".toCharArray()
val factory = net.sqlcipher.database.SupportFactory(
    net.sqlcipher.database.SQLiteDatabase.getBytes(passphrase)
)
```

DEPOIS:
```kotlin
val passphraseBytes = SqlCipherKeyManager.getInstance(context).getOrCreatePassphrase()
val factory = net.sqlcipher.database.SupportFactory(passphraseBytes)
```

**Arquivo NOVO:** `InventarioMobile/app/src/main/java/com/inventario/mobile/security/SqlCipherKeyManager.kt`

```kotlin
class SqlCipherKeyManager private constructor(context: Context) {

    companion object {
        private const val TAG = "SqlCipherKeyManager"
        private const val PREFS_NAME = "sqlcipher_key_prefs"
        private const val KEY_PASSPHRASE = "sqlcipher_passphrase_b64"
        private const val PASSPHRASE_BYTES = 32 // 256 bits

        @Volatile private var instance: SqlCipherKeyManager? = null
        fun getInstance(context: Context): SqlCipherKeyManager =
            instance ?: synchronized(this) {
                instance ?: SqlCipherKeyManager(context.applicationContext).also { instance = it }
            }
    }

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Retorna a passphrase do SQLCipher. Se não existir, gera uma aleatória
     * com 256 bits de entropia e persiste criptografada.
     *
     * @throws SqlCipherKeyException se o Android Keystore estiver indisponível
     */
    fun getOrCreatePassphrase(): ByteArray {
        val existing = prefs.getString(KEY_PASSPHRASE, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }
        val random = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(random, Base64.NO_WRAP))
            .apply()
        return random
    }

    class SqlCipherKeyException(message: String, cause: Throwable? = null)
        : RuntimeException(message, cause)
}
```

**Migração:** ao trocar de `"inventario_secure_key"` hardcoded para uma passphrase aleatória, o banco existente `inventario_offline_secure.db` não consegue ser aberto. A estratégia é a mesma já usada no `AppDatabase` (excluir banco antigo e reconstruir):

```kotlin
// No getInstance() do AppDatabase
val oldDbFile = context.getDatabasePath("inventario_offline_secure.db")
val firstRunMarker = context.getSharedPreferences("sqlcipher_migration", Context.MODE_PRIVATE)
if (!firstRunMarker.getBoolean("migrated_to_keystore_v1", false)) {
    if (oldDbFile.exists()) oldDbFile.delete()
    firstRunMarker.edit().putBoolean("migrated_to_keystore_v1", true).apply()
}
```

O impacto é que dispositivos atualizados precisam re-sincronizar com o servidor na primeira abertura. Coletas pendentes não sincronizadas seriam perdidas — portanto a release deve ser coordenada com uma sincronização forçada prévia.

### R4 — ProGuard/R8 no Release

**Arquivo:** `InventarioMobile/app/build.gradle`

ANTES:
```groovy
release {
    minifyEnabled false
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    signingConfig signingConfigs.release
}
```

DEPOIS:
```groovy
release {
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    signingConfig signingConfigs.release
}
```

**Arquivo NOVO:** `InventarioMobile/app/proguard-rules.pro`

```proguard
# ==============================================
# SIHCP Mobile — Regras de ProGuard/R8 (v2.21.0)
# ==============================================

# -------- Kotlin --------
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**

# -------- Coroutines --------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# -------- Hilt / Dagger --------
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.InstallIn <methods>;
}
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.HiltAndroidApp class *

# -------- Room --------
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# -------- Retrofit / OkHttp --------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# -------- Gson (se usado) --------
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -------- SQLCipher --------
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# -------- Security Crypto (EncryptedSharedPreferences) --------
-keep class androidx.security.crypto.** { *; }

# -------- DTOs e modelos de dados (preservar nomes para JSON) --------
-keep class com.inventario.mobile.data.remote.dto.** { *; }
-keep class com.inventario.mobile.data.local.entity.** { *; }
-keep class com.inventario.mobile.domain.model.** { *; }
-keepclassmembers class com.inventario.mobile.data.remote.dto.** { <fields>; }

# -------- WorkManager --------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# -------- Paging 3 --------
-keep class androidx.paging.** { *; }

# -------- Parcelize --------
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# -------- ZXing (QR Code) --------
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# -------- iText 7 (PDF) --------
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# -------- Glide --------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule

# -------- Depuração (remover em builds finais) --------
# Remover logs Log.d e Log.v em release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
```

### R5 e R10 — Network Security Config sem cleartext global

**Arquivo:** `InventarioMobile/app/src/main/res/xml/network_security_config.xml`

ANTES:
```xml
<base-config cleartextTrafficPermitted="true">
    <trust-anchors>
        <certificates src="system" />
        <certificates src="user" />
    </trust-anchors>
</base-config>
```

DEPOIS:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>

    <!-- Base-config: produção. HTTPS obrigatório, apenas CAs do sistema. -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Domain-config: APENAS desenvolvimento/rede interna IFMT.
         Cleartext + user CAs confinados a IPs específicos. -->
    <domain-config cleartextTrafficPermitted="true">
        <!-- Emulador Android / localhost -->
        <domain includeSubdomains="false">localhost</domain>
        <domain includeSubdomains="false">127.0.0.1</domain>
        <domain includeSubdomains="false">10.0.2.2</domain>

        <!-- Rede interna IFMT (ajustar conforme inventário de IPs) -->
        <domain includeSubdomains="false">10.14.250.228</domain>
        <domain includeSubdomains="false">10.14.250.236</domain>
        <domain includeSubdomains="false">10.14.250.238</domain>
        <domain includeSubdomains="false">192.168.10.107</domain>
        <domain includeSubdomains="false">192.168.11.136</domain>

        <trust-anchors>
            <certificates src="system" />
            <!-- user CAs apenas para debug. Remover em release final. -->
            <certificates src="user" />
        </trust-anchors>
    </domain-config>

</network-security-config>
```

> Observação: os IPs residenciais (`192.168.1.x`, `192.168.0.x`) foram removidos porque não pertencem à rede operacional do IFMT. Se desenvolvedores precisarem de IPs locais específicos, devem adicionar ao seu ambiente de debug via `network_security_config_debug.xml` em `src/debug/res/xml/`.

**Arquivo:** `InventarioMobile/app/src/main/AndroidManifest.xml`

ANTES (atributos do `<application>`):
```xml
android:allowBackup="true"
android:usesCleartextTraffic="true"
android:networkSecurityConfig="@xml/network_security_config"
```

DEPOIS:
```xml
android:allowBackup="false"
android:networkSecurityConfig="@xml/network_security_config"
```

O atributo `android:usesCleartextTraffic` é removido inteiramente — a decisão fica delegada ao `network_security_config.xml`, evitando configuração dupla e conflitante.

### R6 — CORS Explícito no Servidor

**Arquivo:** `src/main/resources/application-mobile.properties`

ANTES:
```properties
api.mobile.cors.allowed-origins=*
api.mobile.cors.allow-credentials=true
```

DEPOIS:
```properties
api.mobile.cors.allowed-origins=http://localhost:8081,https://localhost:8081,http://10.0.2.2:8081,http://192.168.10.107:8081,http://10.14.250.228:8081,http://10.14.250.236:8081,http://10.14.250.238:8081
api.mobile.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
api.mobile.cors.allowed-headers=Authorization,Content-Type,Accept,X-Requested-With
api.mobile.cors.allow-credentials=false
```

Observação: `MobileSecurityConfig.corsConfigurationSource()` já hardcoda a lista correta e `setAllowCredentials(false)`. Portanto, o arquivo de propriedades hoje não é lido pela configuração ativa — a correção é principalmente **alinhar os valores para evitar confusão** e para quando o código for evoluído para ler as propriedades.

**Arquivo:** `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/controller/MobileAuthController.java`

ANTES:
```java
@CrossOrigin(origins = "*", maxAge = 3600)
```

DEPOIS (remover a anotação — CORS é definido globalmente em `MobileSecurityConfig`):
```java
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource()
```

### R7 — Rate Limiting no Endpoint de Login

**Dependência nova em `sihcp-server/pom.xml`:**
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

**Arquivo NOVO:** `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/security/LoginRateLimiter.java`

```java
@Component
public class LoginRateLimiter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);
    private static final int CAPACITY = 10;
    private static final Duration REFILL_WINDOW = Duration.ofMinutes(1);
    private static final int EXCESS_ALERT_THRESHOLD = 3;
    private static final Duration EXCESS_ALERT_WINDOW = Duration.ofMinutes(5);

    @Value("${api.mobile.rate-limit.enabled:false}")
    private boolean enabled;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> excessEvents = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler)
            throws IOException {
        if (!enabled) return true;
        if (!isLoginRequest(req)) return true;

        String ip = resolveClientIp(req);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) return true;

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
        resp.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resp.setHeader("Retry-After", String.valueOf(waitSeconds));
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"rate_limited\",\"retryAfterSeconds\":" + waitSeconds + "}");
        registerExcessAndMaybeAlert(ip);
        return false;
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, REFILL_WINDOW));
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isLoginRequest(HttpServletRequest r) {
        return "POST".equalsIgnoreCase(r.getMethod())
            && r.getRequestURI().endsWith("/api/mobile/auth/login");
    }

    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    private void registerExcessAndMaybeAlert(String ip) {
        Deque<Instant> events = excessEvents.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        Instant now = Instant.now();
        events.addLast(now);
        while (!events.isEmpty() && events.peekFirst().isBefore(now.minus(EXCESS_ALERT_WINDOW))) {
            events.pollFirst();
        }
        if (events.size() >= EXCESS_ALERT_THRESHOLD) {
            log.warn("RATE_LIMIT_ALERT ip={} excess_events_last_5min={}", ip, events.size());
        }
    }
}
```

**Arquivo NOVO:** `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/config/RateLimitConfig.java`

```java
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final LoginRateLimiter loginRateLimiter;

    public RateLimitConfig(LoginRateLimiter loginRateLimiter) {
        this.loginRateLimiter = loginRateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginRateLimiter)
                .addPathPatterns("/api/mobile/auth/login");
    }
}
```

### R8 — Logs de Produção e Sanitização de Tokens

**Arquivo NOVO:** `src/main/resources/application-prod.properties`

```properties
# Perfil de produção — herda application-mobile.properties
logging.level.root=WARN
logging.level.com.inventario=INFO
logging.level.com.inventario.sihcp.mobile.server.controller=INFO
logging.level.com.inventario.sihcp.mobile.server.service=INFO
logging.level.com.inventario.sihcp.security=INFO
logging.level.org.springframework.security=WARN

# Rate limit em produção
api.mobile.rate-limit.enabled=true

# JWT mais curto em produção (override; dev mantém 172800)
jwt.expiration=${JWT_EXPIRATION:3600}
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:604800}

# CORS: pode ser sobrescrito via env
api.mobile.cors.allowed-origins=${API_MOBILE_CORS_ALLOWED_ORIGINS:http://localhost:8081}
api.mobile.cors.allow-credentials=false
```

Ativar com `--spring.profiles.active=mobile,prod` ou `SPRING_PROFILES_ACTIVE=mobile,prod`.

**Arquivo:** `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/controller/MobileAuthController.java`

ANTES (fragmento do método `login`):
```java
logger.info("Access Token: {}", loginResponse.getAccessToken() != null
    ? loginResponse.getAccessToken().substring(0, Math.min(20, ...)) + "..."
    : "null");
logger.info("Refresh Token: {}", loginResponse.getRefreshToken() != null ? "presente" : "null");
```

DEPOIS:
```java
logger.info("Login bem-sucedido: usuario={} expiresIn={}s",
            loginResponse.getUser().getUsername(),
            loginResponse.getExpiresIn());
// Não logar prefixos de tokens — mesmo truncados, facilitam correlação em logs
```

Aplicar a mesma sanitização em `refreshToken()` do mesmo controller.

**Arquivo:** `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/security/MobileJwtAuthenticationFilter.java`

ANTES:
```java
logger.info("Token extraído: {}", jwt != null ? "SIM (length=" + jwt.length() + ")" : "NÃO");
```

DEPOIS:
```java
if (log.isDebugEnabled()) {
    log.debug("Token extraído: presente={}", jwt != null);
}
```

Remover os demais `logger.info("=== MOBILE JWT FILTER ...")` e substituir por `log.debug`. Em produção (`level=INFO`), nada do filtro será logado rotineiramente.

### R9 — Redução do JWT Expiration e Resposta `token_expired`

**Arquivo:** `src/main/resources/application-mobile.properties`

ANTES:
```properties
jwt.expiration=172800
jwt.refresh-expiration=1209600
```

DEPOIS (dev):
```properties
jwt.expiration=${JWT_EXPIRATION:172800}
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:1209600}
```

Em produção, `application-prod.properties` sobrescreve para `3600` / `604800` (ver R8).

**Arquivo:** `sihcp-server/src/main/java/com/inventario/sihcp/security/JwtAuthenticationEntryPoint.java`

Confirmar que, ao disparar 401 por token expirado, o corpo de resposta contenha `{"error":"token_expired"}`. O diff conceitual depende da implementação atual (não lida neste design), mas o contrato exigido é:

```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json
{"error":"token_expired"}
```

Para tokens inválidos (assinatura quebrada, malformados), retornar `{"error":"invalid_token"}`. O app usa essa distinção para decidir entre chamar `/auth/refresh` (expirado) ou forçar logout (inválido).

### R10 — Já coberto em R5 (certificates src="user" removido da base-config).

### R11 — Desabilitar ADB Backup

**Arquivo:** `InventarioMobile/app/src/main/AndroidManifest.xml` (já diffado em R5)

```xml
android:allowBackup="false"
```

**Arquivo:** `InventarioMobile/app/src/main/res/xml/data_extraction_rules.xml`

ANTES:
```xml
<data-extraction-rules>
    <cloud-backup>
        <!-- TODO ... -->
    </cloud-backup>
</data-extraction-rules>
```

DEPOIS:
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="."/>
        <exclude domain="sharedpref" path="secure_prefs.xml"/>
        <exclude domain="sharedpref" path="sqlcipher_key_prefs.xml"/>
        <exclude domain="file" path="datastore/"/>
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="."/>
        <exclude domain="sharedpref" path="secure_prefs.xml"/>
        <exclude domain="sharedpref" path="sqlcipher_key_prefs.xml"/>
    </device-transfer>
</data-extraction-rules>
```

**Arquivo:** `InventarioMobile/app/src/main/res/xml/backup_rules.xml` (legado; Android < 12)

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="database" path="."/>
    <exclude domain="sharedpref" path="secure_prefs.xml"/>
    <exclude domain="sharedpref" path="sqlcipher_key_prefs.xml"/>
</full-backup-content>
```

Com `android:allowBackup="false"` esses arquivos se tornam redundantes, mas mantê-los preenchidos é defesa em profundidade caso alguém reverta a flag.

### R12 — Remoção de Permissões Desnecessárias

**Arquivo:** `InventarioMobile/app/src/main/AndroidManifest.xml`

REMOVER:
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

Antes de remover `USE_FULL_SCREEN_INTENT`, confirmar via `grep_search` que nenhuma notificação usa `setFullScreenIntent(...)`. Se houver uso ativo, manter apenas essa.

---

## Modelos de Dados

Nenhum modelo de dados novo é introduzido. As mudanças são em configuração e em três pontos de armazenamento seguro.

### Variáveis de ambiente esperadas (servidor)

| Variável | Obrigatória | Formato | Exemplo | Uso |
|---|---|---|---|---|
| `DATASOURCE_PASSWORD` | Sim | string, não vazia | `s3cr3t!P4ssw0rd` | Senha do PostgreSQL |
| `JWT_SECRET` | Sim | string, ≥ 32 chars | `base64:Oi5...32+bytes` | Chave HMAC do JWT |
| `JWT_EXPIRATION` | Não (default 3600 em prod) | inteiro (segundos) | `3600` | Validade do access token |
| `JWT_REFRESH_EXPIRATION` | Não (default 604800 em prod) | inteiro (segundos) | `604800` | Validade do refresh token |
| `API_MOBILE_CORS_ALLOWED_ORIGINS` | Não | CSV | `https://app.ifmt.edu.br` | CORS em prod |
| `SPRING_PROFILES_ACTIVE` | Sim (prod) | CSV | `mobile,prod` | Ativa `application-prod.properties` |

### Chaves de armazenamento seguro (app Android)

| Armazenamento | Chave | Conteúdo | Proteção |
|---|---|---|---|
| `EncryptedSharedPreferences("sqlcipher_key_prefs")` | `sqlcipher_passphrase_b64` | Base64 de 32 bytes aleatórios | `MasterKey` AES256-GCM via Android Keystore |
| `EncryptedSharedPreferences("secure_prefs")` | `jwt_token`, `refresh_token`, `user_id`, etc. | Tokens e dados de sessão | `MasterKey` AES256-GCM via Android Keystore |
| `SharedPreferences("sqlcipher_migration")` | `migrated_to_keystore_v1` | boolean | não sensível, apenas flag |

### Arquivos de configuração (propriedades relevantes a segurança)

| Arquivo | Versionado | Chaves |
|---|---|---|
| `application-mobile.properties` | Sim | `${DATASOURCE_PASSWORD}`, `${JWT_SECRET}`, CORS com lista explícita |
| `application-mobile-local.properties` | Não (`.gitignore`) | valores reais para dev |
| `application-prod.properties` | Sim | overrides de INFO, rate-limit, 3600s |
| `keystore.properties` | Não (`.gitignore`) | `storePassword`, `keyPassword`, `keyAlias`, `storeFile` |

---


## Propriedades de Correção

*Uma propriedade é uma característica ou comportamento que deve se manter verdadeiro em todas as execuções válidas do sistema — uma afirmação formal sobre o que o software deve fazer. Propriedades servem de ponte entre a especificação legível por humanos e garantias de correção verificáveis por máquina.*

As propriedades abaixo foram derivadas do prework, consolidando critérios redundantes.

### Propriedade 1: Ausência de segredos literais no arquivo versionado

*Para toda* linha no `src/main/resources/application-mobile.properties` que atribua uma chave em `{spring.datasource.password, jwt.secret}`, o valor atribuído DEVE ser a string vazia OU começar com `${` e terminar com `}`, nunca um literal arbitrário.

**Validates: Requisitos 1.1, 1.2**

### Propriedade 2: `SecretsBootstrap` valida a presença e o comprimento dos segredos

*Para qualquer* par `(datasourcePassword, jwtSecret)` de strings recebido como entrada, `SecretsBootstrap.validate()` DEVE abortar a inicialização se, e somente se, `datasourcePassword.isBlank() || jwtSecret.isBlank() || jwtSecret.length() < 32`; caso contrário DEVE permitir o boot.

**Validates: Requisitos 1.3, 1.4, 1.6**

### Propriedade 3: `.gitignore` cobre todos os padrões sensíveis

*Para qualquer* caminho gerado dentro do conjunto de padrões sensíveis `{.env, .env.*, application-*-local.properties, application-local.properties, keystore.properties, InventarioMobile/keystore.properties, *.jks, *.keystore}`, o arquivo `.gitignore` do projeto DEVE casar com esse caminho (segundo a semântica de globs do Git).

**Validates: Requisitos 1.5, 2.1, 2.4**

### Propriedade 4: Passphrase do SQLCipher é gerada, persistida e única por dispositivo

*Para qualquer* instância recém-criada de `SqlCipherKeyManager`:
- (a) `getOrCreatePassphrase()` DEVE retornar um array de exatamente 32 bytes (≥ 128 bits de entropia);
- (b) duas instâncias em `EncryptedSharedPreferences` inicialmente vazios DEVEM produzir passphrases distintas (probabilidade de colisão desprezível);
- (c) chamadas subsequentes no mesmo dispositivo DEVEM retornar a mesma passphrase (round-trip);
- (d) nenhum arquivo `.kt` sob `InventarioMobile/app/src/main` DEVE conter o literal `"inventario_secure_key"` ou passphrase hardcoded equivalente.

**Validates: Requisitos 3.1, 3.2, 3.3**

### Propriedade 5: Configuração de rede não permite cleartext nem user CAs em produção

*Para qualquer* parse do `network_security_config.xml` e do `AndroidManifest.xml`:
- (a) o nó `<base-config>` DEVE ter `cleartextTrafficPermitted="false"` e NÃO DEVE conter `<certificates src="user" />`;
- (b) o elemento `<application>` do manifesto NÃO DEVE conter o atributo `android:usesCleartextTraffic`;
- (c) para todo `<domain-config>` com `cleartextTrafficPermitted="true"`, cada `<domain>` listado DEVE pertencer a uma allowlist conhecida de desenvolvimento (`localhost`, `127.0.0.1`, `10.0.2.2`, ranges `10.14.250.x` e `192.168.1x.x` do IFMT).

**Validates: Requisitos 5.1, 5.3, 5.5, 10.1, 10.2, 10.3**

### Propriedade 6: ProGuard preserva bibliotecas reflexivas

*Para cada* biblioteca no conjunto `{androidx.room, retrofit2, dagger.hilt, net.sqlcipher, com.google.gson, androidx.work, androidx.security.crypto, com.inventario.mobile.data.remote.dto, com.inventario.mobile.data.local.entity, com.inventario.mobile.domain.model}`, o arquivo `proguard-rules.pro` DEVE conter pelo menos uma regra `-keep` (ou `-keepclassmembers`) cujo padrão de classe cobra o pacote dessa biblioteca.

**Validates: Requisito 4.3**

### Propriedade 7: Rate limit aplica exclusivamente ao login, com janela de 10/min

*Para qualquer* sequência arbitrária de `N` requisições dentro de uma janela de 60 segundos a partir de um IP único, com `api.mobile.rate-limit.enabled=true`:
- (a) se o path for `POST /api/mobile/auth/login`, as primeiras 10 requisições são processadas (retornam 200/401/403 conforme credenciais) e da 11ª em diante o servidor DEVE retornar HTTP 429 com cabeçalho `Retry-After` contendo inteiro positivo de segundos;
- (b) se o path for qualquer outro endpoint `/api/mobile/*`, nenhuma requisição DEVE receber 429 por motivo de rate limit, independentemente de `N`.

**Validates: Requisitos 7.2, 7.3, 7.5**

### Propriedade 8: Manifesto do app mantém permissões mínimas e desabilita backup

*Para qualquer* parse do `InventarioMobile/app/src/main/AndroidManifest.xml`:
- (a) `<application>` DEVE ter `android:allowBackup="false"`;
- (b) `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />` NÃO DEVE estar declarada;
- (c) `<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />` DEVE estar ausente sempre que o código-fonte não invocar `NotificationCompat.Builder.setFullScreenIntent(...)` em nenhum arquivo sob `InventarioMobile/app/src/main/java`.

**Validates: Requisitos 11.1, 12.1, 12.3**

### Propriedade 9: Logs não vazam tokens JWT

*Para qualquer* token JWT recebido em uma requisição processada por `MobileJwtAuthenticationFilter` ou `MobileAuthController` em um servidor com perfil `prod` ativo, as entradas de log emitidas em nível `INFO` ou superior NÃO DEVEM conter a string completa do token, nem um prefixo contíguo de 8 ou mais caracteres do token.

**Validates: Requisito 8.4**

---

## Tratamento de Erros

### Servidor

| Cenário | Comportamento esperado |
|---|---|
| `DATASOURCE_PASSWORD` ausente | `SecretsBootstrap` loga erro explícito e chama `SpringApplication.exit(ctx, () -> 1)` seguido de `System.exit(1)`. O Spring Boot não conclui a inicialização. |
| `JWT_SECRET` ausente | Idem acima. |
| `JWT_SECRET` com menos de 32 caracteres | `SecretsBootstrap` aborta com mensagem `"JWT_SECRET muito curto: X chars (mínimo: 32)"`. |
| `application-prod.properties` ausente ao ativar `prod` | Spring não encontra o arquivo, cai no perfil base `mobile` (log em `DEBUG`, rate limit desabilitado). Recomendação: criar arquivo vazio intencional ou falhar via `spring.config.import=required:classpath:application-prod.properties`. |
| IP atinge 10 tentativas em 1 min no `/auth/login` | Retorna 429 com `Retry-After`. A partir da 3ª vez em 5 min, o `LoginRateLimiter` emite `log.warn("RATE_LIMIT_ALERT ip={} ...")`. |
| Origem CORS não listada | Spring Security retorna 403 (ou remove header `Access-Control-Allow-Origin`, resultando no browser bloquear a resposta). |
| Token JWT expirado | 401 com corpo `{"error":"token_expired"}`. |
| Token JWT inválido (assinatura quebrada, malformado) | 401 com corpo `{"error":"invalid_token"}`. |

### App Android

| Cenário | Comportamento esperado |
|---|---|
| Android Keystore indisponível (dispositivo corrompido / rooted com policy violation) | `SqlCipherKeyManager` lança `SqlCipherKeyException`. `AppDatabase.getInstance()` propaga — a UI deve capturar e exibir mensagem de erro bloqueante; nenhum acesso ao banco é permitido. |
| Passphrase divergente do banco existente (migração mal sucedida) | O bloco de migração no `getInstance()` apaga o arquivo antigo e força a re-criação. Coletas pendentes não sincronizadas são perdidas — por isso a release deve ser precedida de sync forçado. |
| Build release sem `keystore.properties` | O `build.gradle` lança `RuntimeException` em tempo de configuração do Gradle, com mensagem orientando o desenvolvedor. Já implementado. |
| Rate limit atingido (429 do servidor) | Retrofit / OkHttp recebem 429; o `RefreshTokenInterceptor` NÃO trata 429 (apenas 401). A UI de login deve mostrar "Muitas tentativas, aguarde X segundos" lendo o cabeçalho `Retry-After`. |
| Tráfego cleartext para domínio fora da allowlist | Android OS lança `CleartextNotPermittedException` na camada de rede. App deve capturar e exibir mensagem clara: "Servidor não suportado — use HTTPS ou conecte-se à rede IFMT". |

### Cenários destrutivos (operacionais)

| Ação | Risco | Mitigação |
|---|---|---|
| Rotacionar `JWT_SECRET` em produção | Invalida todos os tokens ativos — todos os usuários são deslogados no próximo request | Comunicar janela de manutenção; documentar que os apps devem tentar re-login automaticamente |
| Rotacionar senha do PostgreSQL | Se servidor ativo não for reiniciado, HikariCP renova conexões com senha antiga e falha | Rotacionar senha + reiniciar servidor em sequência |
| Publicar nova release com `SqlCipherKeyManager` | Perda de banco local; coletas offline não sincronizadas são descartadas | Forçar sync nos apps antes do upgrade; exibir tela de "Atualização de segurança" explicando o re-download |
| Remover `keystore.properties` do índice Git (`git rm --cached`) | Commits antigos ainda contêm o arquivo no histórico | Rotacionar as senhas do keystore ou gerar novo keystore (gera nova assinatura e o app passa a ser tratado como novo na Play Store / dispositivos — aceitável para app interno) |

---

## Estratégia de Testes

A feature **combina PBT, example tests, smoke tests e integration tests**, porque mistura lógica testável (bootstrap, rate limiter, key manager) com configuração estática (XML, properties) e infra (ADB backup, build Gradle).

### Unit tests (JUnit 5 / Kotest — lógica)

Servidor (`sihcp-server/src/test/java`):
- `JwtUtilTest.rejectsSecretShorterThan32Chars()` — boundary explícito.
- `JwtAuthenticationEntryPointTest.returnsTokenExpiredForExpiredToken()` — valida o contrato do body.
- `MobileAuthControllerTest.doesNotLogTokenSubstrings()` — usa `LogCaptor` para validar Propriedade 9.

Android (`InventarioMobile/app/src/test/java`):
- `SqlCipherKeyManagerTest.throwsExceptionWhenKeystoreFails()` — mock `EncryptedSharedPreferences` para lançar `GeneralSecurityException`; verifica propagação.
- `LoginRateLimiterTest.disabledWhenFlagFalse()` — quando `enabled=false`, nunca retorna 429.

### Property-based tests (Kotest Property / jqwik)

As Propriedades 1 a 9 são implementadas cada uma como UM teste de propriedade. Configuração: mínimo 100 iterações por propriedade.

Tag format em cada teste:
`// Feature: correcoes-seguranca, Property {N}: {property_text}`

Servidor (jqwik em `sihcp-server`):
- `SecretsBootstrapPropertyTest` — Propriedade 2. `@Property` que gera pares `(password, secret)` usando `Arbitraries.strings().ofMaxLength(200)` e verifica o predicado `abort iff blank-or-short`.
- `ApplicationPropertiesPropertyTest` — Propriedade 1. Abre `application-mobile.properties` no `src/main/resources`, parseia com `java.util.Properties`, e para cada chave sensível aplica o predicado. (Propriedade sobre arquivo estático, mas executa como JUnit test.)
- `GitignorePropertyTest` — Propriedade 3. Usa uma biblioteca de gitignore matcher (ex. [gitignore-maven-plugin] ou simplesmente `org.eclipse.jgit.ignore.FastIgnoreRule`); gera paths dentro dos padrões sensíveis com `Arbitraries.of(...)` concatenados a sufixos aleatórios; valida match.
- `LoginRateLimiterPropertyTest` — Propriedade 7. Usa `Bucket4j` com `TimeMeter` controlável; gera `Arb.list(Arb.long(0..60000))` representando timestamps em ms, e para cada sequência verifica contagem de `isConsumed()==false`.
- `JwtLogSanitizationPropertyTest` — Propriedade 9. `LogCaptor` captura logs; gera tokens `Arb.string()` enviados ao filter; asserta que nenhum log contém substring de 8+ chars do token.

Android (Kotest Property em `InventarioMobile/app/src/test/java`):
- `SqlCipherKeyManagerPropertyTest` — Propriedade 4. Roda em JVM pura com `androidx.security.crypto` mockado via `mockk`; gera 100 instâncias, valida 32 bytes, distinção e round-trip.
- `NetworkSecurityConfigPropertyTest` — Propriedade 5. Parseia o XML com `DocumentBuilder`, aplica asserts; para a parte de allowlist, gera IPs com `Arb.string()` e confirma que só os conhecidos passam.
- `ProguardRulesPropertyTest` — Propriedade 6. Lê `proguard-rules.pro`, para cada biblioteca do array de libs esperadas, verifica regex match de `-keep.*<pacote>`.
- `AndroidManifestPropertyTest` — Propriedade 8. Parseia `AndroidManifest.xml`; grep recursivo nos `.kt` para confirmar ausência de `setFullScreenIntent`.

Cada teste de propriedade é anotado:

```kotlin
// Feature: correcoes-seguranca, Property 4: Passphrase do SQLCipher é gerada, persistida e única por dispositivo
@Test
fun passphraseHasExpectedEntropyAndIsStable() = runTest {
    checkAll(iterations = 100, Arb.int(1..10)) { seed ->
        // ...
    }
}
```

### Smoke tests (1 execução, estáticos)

Scripts em `scripts/security-smoke.ps1` ou como tarefa Maven/Gradle customizada:
- `grep` no `.` para confirmar que `"Romulo@1919"` não aparece em nenhum arquivo rastreado pelo Git.
- `grep` para `"inventario-mobile-secret-key-2024"` — a string atual do `jwt.secret` hardcoded.
- `grep` para `"inventario_secure_key"` nos `.kt`.
- `git ls-files | grep -E "^(InventarioMobile/)?keystore\\.properties$"` deve retornar vazio.
- `application-prod.properties` contém `logging.level.com.inventario=INFO` (grep literal).
- `build.gradle` do app tem `minifyEnabled true` no bloco `release`.
- `AndroidManifest.xml` não contém `SYSTEM_ALERT_WINDOW` nem `allowBackup="true"`.

### Integration tests

Servidor (Spring Boot `@SpringBootTest`):
- `LoginRateLimitIntegrationTest` — sobe contexto com perfil `prod`, usa `MockMvc` para bombardear `/api/mobile/auth/login` 15x em < 1 min, verifica que as requests 11-15 retornam 429 com `Retry-After`.
- `CorsIntegrationTest` — envia preflight OPTIONS com origem não listada, verifica ausência do header `Access-Control-Allow-Origin`.
- `TokenExpiryIntegrationTest` — gera token com `exp` no passado via `JwtUtil`, chama endpoint autenticado, verifica 401 + `{"error":"token_expired"}`.
- `StartupFailureIntegrationTest` — `@SpringBootTest` com propriedade explicitamente sem `JWT_SECRET`, espera `ApplicationContextException`.

App Android (instrumentation em `InventarioMobile/app/src/androidTest/`):
- `SqlCipherE2ETest` — cria `AppDatabase`, insere dados, fecha, reabre com mesmo `SqlCipherKeyManager`, lê — round-trip de ponta-a-ponta no emulador.
- `ReleaseBuildFunctionalTest` — CI roda `./gradlew assembleRelease` e instala o APK em emulador; smoke manual das telas principais (login, scan, sync).

### Execução em CI

Matriz de CI recomendada:
```
- mvn -pl sihcp-server test                      # unit + property server
- mvn -pl sihcp-server verify -P integration     # integration server
- cd InventarioMobile && ./gradlew testDebugUnitTest  # unit + property android
- cd InventarioMobile && ./gradlew assembleRelease    # valida ProGuard (R4.4)
- powershell ./scripts/security-smoke.ps1             # smoke de configurações
```

Falha em qualquer passo bloqueia o merge.

### Considerações de PBT aplicável

PBT é apropriado aqui porque:
- `SecretsBootstrap.validate()` é uma função pura de strings → predicado claro.
- `LoginRateLimiter` com clock mockado é função pura da sequência de eventos.
- Configurações em XML/properties podem ser parseadas e validadas contra um conjunto infinito de inputs sintéticos.
- `SqlCipherKeyManager` usa aleatoriedade — propriedade estatística (unicidade) vale PBT.

PBT NÃO é usado para:
- Ativar `minifyEnabled` — configuração estática, 1 teste basta.
- Remoção de permissões no manifesto — 1 assert cobre.
- Rotação de senha do PostgreSQL — operação manual.
- Remoção do histórico Git — operação manual, não testável por software.
