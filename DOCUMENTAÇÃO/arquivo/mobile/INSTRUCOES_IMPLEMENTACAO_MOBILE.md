# Instruções de Implementação - Aplicativo Mobile Android

## 1. Preparação do Ambiente

### 1.1 Requisitos do Sistema
- **Java Development Kit (JDK)**: 11 ou superior
- **Android Studio**: Versão mais recente (Arctic Fox ou superior)
- **Android SDK**: API Level 24+ (Android 7.0)
- **Gradle**: 7.0+
- **Git**: Para controle de versão

### 1.2 Configuração do Android Studio
1. Instalar Android Studio
2. Configurar SDK Manager:
   - Android SDK Platform 24+
   - Android SDK Build-Tools
   - Google Play Services
   - Android Support Repository

### 1.3 Configuração do Projeto
```bash
# Criar diretório do projeto mobile
mkdir inventario-mobile-android
cd inventario-mobile-android

# Inicializar repositório Git
git init
git remote add origin <URL_DO_REPOSITORIO>
```

## 2. Estrutura do Projeto Android

### 2.1 Criar Novo Projeto
1. Abrir Android Studio
2. "Create New Project"
3. Selecionar "Empty Activity"
4. Configurações:
   - **Name**: Inventário Mobile
   - **Package**: com.inventario.mobile
   - **Language**: Kotlin
   - **Minimum SDK**: API 24 (Android 7.0)

### 2.2 Configuração do build.gradle (Module: app)
```kotlin
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.inventario.mobile"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        
        // Configurações do banco local
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += ["room.schemaLocation": "$projectDir/schemas".toString()]
            }
        }
    }
    
    buildTypes {
        debug {
            debuggable true
            applicationIdSuffix ".debug"
            versionNameSuffix "-debug"
            buildConfigField "String", "API_BASE_URL", '"http://10.0.2.2:8080/api/mobile/"'
            buildConfigField "boolean", "DEBUG_MODE", "true"
        }
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            buildConfigField "String", "API_BASE_URL", '"https://seu-servidor.com/api/mobile/"'
            buildConfigField "boolean", "DEBUG_MODE", "false"
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        viewBinding true
        dataBinding true
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Navigation
    implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
    implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'
    
    // ViewModel e LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    
    // Retrofit para API
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // QR Code Scanner
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.google.zxing:core:3.5.2'
    
    // WorkManager para sincronização
    implementation 'androidx.work:work-runtime-ktx:2.9.0'
    
    // Permissions
    implementation 'pub.devrel:easypermissions:3.0.0'
    
    // Image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    
    // Security
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

### 2.3 Configuração do AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.inventario.mobile">
    
    <!-- Permissões -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
        android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <!-- Features -->
    <uses-feature android:name="android.hardware.camera" android:required="true" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
    
    <application
        android:name=".InventarioApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.InventarioMobile"
        android:usesCleartextTraffic="true"
        android:networkSecurityConfig="@xml/network_security_config">
        
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity
            android:name=".ui.LoginActivity"
            android:exported="false"
            android:screenOrientation="portrait" />
        
        <activity
            android:name=".ui.ScannerActivity"
            android:exported="false"
            android:screenOrientation="portrait" />
        
        <!-- WorkManager -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup" />
        </provider>
        
    </application>
</manifest>
```

## 3. Implementação das Classes Principais

### 3.1 Application Class
```kotlin
// app/src/main/java/com/inventario/mobile/InventarioApplication.kt
class InventarioApplication : Application() {
    
    val database by lazy { InventarioDatabase.getDatabase(this) }
    val repository by lazy { InventarioRepository(database.patrimonioDao(), database.coletaDao()) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar logging em debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Inicializar WorkManager para sincronização
        initializeWorkManager()
    }
    
    private fun initializeWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
```

### 3.2 Database Configuration
```kotlin
// app/src/main/java/com/inventario/mobile/data/local/InventarioDatabase.kt
@Database(
    entities = [
        PatrimonioEntity::class,
        ColetaEntity::class,
        SetorEntity::class,
        SalaEntity::class,
        UsuarioEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class InventarioDatabase : RoomDatabase() {
    
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun setorDao(): SetorDao
    abstract fun salaDao(): SalaDao
    abstract fun usuarioDao(): UsuarioDao
    
    companion object {
        @Volatile
        private var INSTANCE: InventarioDatabase? = null
        
        fun getDatabase(context: Context): InventarioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    "inventario_database"
                )
                .addMigrations()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 3.3 API Service
```kotlin
// app/src/main/java/com/inventario/mobile/data/remote/InventarioApiService.kt
interface InventarioApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<LoginResponse>>
    
    @GET("patrimonio/{codigo}")
    suspend fun buscarPatrimonio(@Path("codigo") codigo: String): Response<ApiResponse<PatrimonioDto>>
    
    @GET("patrimonio/qr/{qrCode}")
    suspend fun buscarPatrimonioPorQr(@Path("qrCode") qrCode: String): Response<ApiResponse<PatrimonioDto>>
    
    @POST("coleta")
    suspend fun registrarColeta(@Body request: ColetaRequest): Response<ApiResponse<ColetaResponse>>
    
    @POST("coleta/batch")
    suspend fun sincronizarColetas(@Body request: BatchColetaRequest): Response<ApiResponse<BatchColetaResponse>>
    
    @GET("sync/patrimonio")
    suspend fun sincronizarPatrimonios(
        @Query("setorId") setorId: Long?,
        @Query("lastSync") lastSync: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<ApiResponse<PagedResponse<PatrimonioDto>>>
    
    companion object {
        fun create(): InventarioApiService {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(InventarioApiService::class.java)
        }
    }
}
```

## 4. Implementação das Telas

### 4.1 Login Activity
```kotlin
// app/src/main/java/com/inventario/mobile/ui/LoginActivity.kt
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupViewModel() {
        val repository = (application as InventarioApplication).repository
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> showLoading(true)
                is LoginState.Success -> {
                    showLoading(false)
                    navigateToMain()
                }
                is LoginState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInput(username, password)) {
                viewModel.login(username, password)
            }
        }
    }
    
    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.etUsername.error = "Usuário é obrigatório"
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Senha é obrigatória"
            return false
        }
        return true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
```

### 4.2 Scanner Activity
```kotlin
// app/src/main/java/com/inventario/mobile/ui/ScannerActivity.kt
class ScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityScannerBinding
    private lateinit var viewModel: ScannerViewModel
    private var captureManager: CaptureManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupScanner()
        setupObservers()
    }
    
    private fun setupScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Posicione o QR Code dentro do quadro")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showError("Scan cancelado")
                finish()
            } else {
                viewModel.processQrCode(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    private fun setupObservers() {
        viewModel.scanState.observe(this) { state ->
            when (state) {
                is ScanState.Loading -> showLoading(true)
                is ScanState.Success -> {
                    showLoading(false)
                    showPatrimonioDetails(state.patrimonio)
                }
                is ScanState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }
}
```

## 5. Configuração de Segurança

### 5.1 Network Security Config
```xml
<!-- app/src/main/res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain> <!-- Emulador -->
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
    
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```

### 5.2 ProGuard Rules
```proguard
# app/proguard-rules.pro

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ZXing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Models
-keep class com.inventario.mobile.data.model.** { *; }
-keep class com.inventario.mobile.data.remote.dto.** { *; }
```

## 6. Testes

### 6.1 Configuração de Testes
```kotlin
// app/src/test/java/com/inventario/mobile/ExampleUnitTest.kt
class InventarioRepositoryTest {
    
    @Mock
    private lateinit var apiService: InventarioApiService
    
    @Mock
    private lateinit var patrimonioDao: PatrimonioDao
    
    private lateinit var repository: InventarioRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = InventarioRepository(patrimonioDao, apiService)
    }
    
    @Test
    fun `buscar patrimonio por codigo deve retornar sucesso`() = runTest {
        // Given
        val codigo = "123456"
        val patrimonioDto = PatrimonioDto(id = 1, codigo = codigo, descricao = "Teste")
        val response = Response.success(ApiResponse.success(patrimonioDto))
        
        `when`(apiService.buscarPatrimonio(codigo)).thenReturn(response)
        
        // When
        val result = repository.buscarPatrimonioPorCodigo(codigo)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(codigo, result.getOrNull()?.codigo)
    }
}
```

### 6.2 Testes Instrumentados
```kotlin
// app/src/androidTest/java/com/inventario/mobile/DatabaseTest.kt
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    
    private lateinit var database: InventarioDatabase
    private lateinit var patrimonioDao: PatrimonioDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, InventarioDatabase::class.java
        ).build()
        patrimonioDao = database.patrimonioDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndGetPatrimonio() = runTest {
        val patrimonio = PatrimonioEntity(
            id = 1,
            codigo = "123456",
            descricao = "Teste",
            setorId = 1
        )
        
        patrimonioDao.insert(patrimonio)
        
        val retrieved = patrimonioDao.getById(1)
        assertEquals(patrimonio.codigo, retrieved?.codigo)
    }
}
```

## 7. Build e Deploy

### 7.1 Configuração de Assinatura
```kotlin
// app/build.gradle
android {
    signingConfigs {
        release {
            storeFile file('../keystore/inventario-release.jks')
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // outras configurações...
        }
    }
}
```

### 7.2 Script de Build
```bash
#!/bin/bash
# scripts/build-release.sh

echo "Iniciando build de produção..."

# Limpar projeto
./gradlew clean

# Executar testes
echo "Executando testes..."
./gradlew test

if [ $? -ne 0 ]; then
    echo "Testes falharam. Build cancelado."
    exit 1
fi

# Build release
echo "Gerando APK de produção..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo "Build concluído com sucesso!"
    echo "APK gerado em: app/build/outputs/apk/release/"
else
    echo "Erro no build!"
    exit 1
fi
```

### 7.3 Configuração CI/CD (GitHub Actions)
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Build APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## 8. Distribuição

### 8.1 Google Play Console
1. Criar conta de desenvolvedor
2. Configurar aplicativo
3. Upload do APK/AAB
4. Configurar metadados
5. Configurar testes internos
6. Publicar versão

### 8.2 Distribuição Interna
```bash
# Script para distribuição interna
#!/bin/bash
# scripts/distribute-internal.sh

APK_PATH="app/build/outputs/apk/release/app-release.apk"
DIST_DIR="distribution"
VERSION=$(grep versionName app/build.gradle | awk '{print $2}' | tr -d '"')

mkdir -p $DIST_DIR

# Copiar APK com nome versionado
cp $APK_PATH "$DIST_DIR/inventario-mobile-v$VERSION.apk"

# Gerar QR Code para download
qrencode -o "$DIST_DIR/download-qr.png" "https://seu-servidor.com/download/inventario-mobile-v$VERSION.apk"

echo "APK distribuído em: $DIST_DIR/"
echo "Versão: $VERSION"
```

## 9. Monitoramento e Analytics

### 9.1 Firebase Crashlytics
```kotlin
// build.gradle (Project)
classpath 'com.google.gms:google-services:4.3.15'
classpath 'com.google.firebase:firebase-crashlytics-gradle:2.9.9'

// build.gradle (Module: app)
apply plugin: 'com.google.gms.google-services'
apply plugin: 'com.google.firebase.crashlytics'

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-crashlytics-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
}
```

### 9.2 Configuração de Logs
```kotlin
// app/src/main/java/com/inventario/mobile/util/Logger.kt
object Logger {
    
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        
        // Enviar para Crashlytics em produção
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().recordException(
                throwable ?: Exception(message)
            )
        }
    }
    
    fun logEvent(event: String, params: Bundle? = null) {
        FirebaseAnalytics.getInstance(context).logEvent(event, params)
    }
}
```

## 10. Manutenção e Atualizações

### 10.1 Versionamento
- **Major**: Mudanças incompatíveis
- **Minor**: Novas funcionalidades compatíveis
- **Patch**: Correções de bugs

### 10.2 Estratégia de Atualização
1. **Automática**: Via Google Play Store
2. **Forçada**: Para versões críticas
3. **Opcional**: Para melhorias menores

### 10.3 Rollback Plan
1. Manter versões anteriores disponíveis
2. Monitorar métricas pós-deploy
3. Procedimento de rollback rápido

---

**Documento criado em**: $(date)
**Versão**: 1.0
**Responsável**: Equipe de Desenvolvimento Mobile

## Próximos Passos

1. ✅ Configurar ambiente de desenvolvimento
2. ⏳ Implementar autenticação e login
3. ⏳ Desenvolver scanner QR Code
4. ⏳ Implementar sincronização offline
5. ⏳ Criar telas de coleta
6. ⏳ Implementar testes
7. ⏳ Configurar CI/CD
8. ⏳ Deploy e distribuição

**Estimativa Total**: 16-20 semanas