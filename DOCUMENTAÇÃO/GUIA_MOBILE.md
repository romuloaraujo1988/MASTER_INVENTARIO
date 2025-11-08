# 📱 Guia Mobile - Sistema de Inventário

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura](#arquitetura)
3. [Backend - API Mobile](#backend---api-mobile)
4. [Android App](#android-app)
5. [Modo Offline](#modo-offline)
6. [Implementação](#implementação)
7. [Testes](#testes)
8. [Deploy](#deploy)
9. [Referências](#referências)

---

## 📊 Visão Geral

Sistema mobile completo para coleta de inventário com suporte a QR Code, modo offline e sincronização automática.

**Componentes**:
- Backend API REST (Spring Boot)
- App Android nativo (Kotlin)
- Banco local SQLite (Room)
- Sincronização bidirecional

**Versão**: 1.0.0  
**Data**: Novembro 2025  
**Status**: ✅ Implementado

---

## 🏗️ Arquitetura

### Diagrama Geral

```
┌─────────────────────────────────────────────────────────────┐
│                     App Android (Kotlin)                     │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐   │
│  │ Presentation│  │  Domain    │  │      Data          │   │
│  │   (MVVM)   │  │  (UseCases)│  │  (Repository)      │   │
│  └────────────┘  └────────────┘  └────────────────────┘   │
│         │              │                    │               │
│         └──────────────┴────────────────────┘               │
│                        │                                     │
└────────────────────────┼─────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Backend API REST (Spring Boot)                  │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐   │
│  │ Controllers│  │  Services  │  │       DAOs         │   │
│  │  (Mobile)  │  │  (Mobile)  │  │   (Database)       │   │
│  └────────────┘  └────────────┘  └────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              SQLite Local (Modo Offline)                     │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐   │
│  │ Patrimônios│  │Responsáveis│  │      Coletas       │   │
│  │   Cache    │  │   Cache    │  │     Pendentes      │   │
│  └────────────┘  └────────────┘  └────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de Dados

```
Online:
App → API REST → PostgreSQL → API REST → App

Offline:
App → SQLite Local → (aguarda conexão) → API REST → PostgreSQL
```

---

## 🔌 Backend - API Mobile

### Estrutura de Pacotes

```
src/main/java/com/inventario/
├── mobile/server/
│   ├── controller/          # REST Controllers
│   │   ├── MobileAuthController.java
│   │   ├── MobilePatrimonioController.java
│   │   ├── MobileColetaController.java
│   │   ├── MobileSyncController.java
│   │   ├── MobileResponsavelController.java
│   │   └── MobileSalaController.java
│   ├── service/             # Business Logic
│   │   ├── MobileAuthService.java
│   │   ├── MobilePatrimonioService.java
│   │   ├── MobileColetaService.java
│   │   ├── MobileSyncService.java
│   │   ├── MobileResponsavelService.java
│   │   └── MobileSalaService.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── MobilePatrimonioDTO.java
│   │   ├── MobileColetaDTO.java
│   │   ├── MobileResponsavelDTO.java
│   │   └── MobileSalaDTO.java
│   ├── config/              # Configurações
│   │   ├── SecurityConfig.java
│   │   └── JwtConfig.java
│   └── security/            # Segurança
│       ├── JwtAuthenticationFilter.java
│       └── JwtTokenProvider.java
```

### Endpoints Principais

#### Autenticação
```
POST   /api/mobile/auth/login       # Login com JWT
POST   /api/mobile/auth/refresh     # Refresh token
POST   /api/mobile/auth/logout      # Logout
```

#### Patrimônio
```
GET    /api/mobile/patrimonio                        # Listar (paginado)
GET    /api/mobile/patrimonio/{codigo}               # Buscar por código
GET    /api/mobile/patrimonio/qr/{qrCode}            # Buscar por QR
GET    /api/mobile/patrimonio/responsavel/{id}       # Por responsável
```

#### Coleta
```
POST   /api/mobile/coleta                  # Registrar coleta
POST   /api/mobile/coleta/batch             # Sincronizar lote
GET    /api/mobile/coleta/status/{codigo}   # Status de coleta
```

#### Sincronização
```
GET    /api/mobile/sync/patrimonio          # Sincronizar patrimônios
GET    /api/mobile/sync/responsaveis        # Sincronizar responsáveis
GET    /api/mobile/sync/salas               # Sincronizar salas
POST   /api/mobile/sync/connect             # Conectar dispositivo
POST   /api/mobile/sync/heartbeat           # Heartbeat
```

#### Responsáveis e Salas
```
GET    /api/mobile/responsaveis             # Listar responsáveis
GET    /api/mobile/responsaveis/{id}        # Buscar responsável
GET    /api/mobile/salas                    # Listar salas
GET    /api/mobile/salas/{id}               # Buscar sala
```

### Exemplo de Controller

```java
@RestController
@RequestMapping("/api/mobile/patrimonio")
public class MobilePatrimonioController {
    
    @Autowired
    private MobilePatrimonioService patrimonioService;
    
    @GetMapping("/responsavel/{idResponsavel}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorResponsavel(
            @PathVariable Integer idResponsavel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Boolean coletado) {
        
        List<MobilePatrimonioDTO> patrimonios = 
            patrimonioService.buscarPorResponsavel(idResponsavel, page, size, coletado);
        
        return ResponseEntity.ok(ApiResponse.success(patrimonios, "Patrimônios encontrados"));
    }
}
```

### Segurança JWT

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/mobile/auth/**").permitAll()
                .antMatchers("/api/mobile/**").authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 📱 Android App

### Tecnologias

- **Linguagem**: Kotlin
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Banco Local**: Room Database (SQLite)
- **Rede**: Retrofit + OkHttp
- **QR Code**: ZXing
- **Async**: Coroutines + Flow
- **DI**: Hilt (opcional)

### Estrutura do Projeto

```
app/src/main/java/com/inventario/mobile/
├── data/
│   ├── local/                    # Room Database
│   │   ├── dao/
│   │   │   ├── PatrimonioDao.kt
│   │   │   ├── ColetaDao.kt
│   │   │   ├── ResponsavelDao.kt
│   │   │   └── SalaDao.kt
│   │   ├── entity/
│   │   │   ├── PatrimonioEntity.kt
│   │   │   ├── ColetaEntity.kt
│   │   │   ├── ResponsavelEntity.kt
│   │   │   └── SalaEntity.kt
│   │   ├── InventarioDatabase.kt
│   │   └── OfflineDataManager.kt
│   ├── remote/                   # API
│   │   ├── api/
│   │   │   └── ApiService.kt
│   │   └── dto/
│   │       ├── PatrimonioDto.kt
│   │       ├── ColetaDto.kt
│   │       └── ResponsavelDto.kt
│   ├── repository/               # Repository Pattern
│   │   └── InventarioRepository.kt
│   └── model/                    # Domain Models
│       ├── Patrimonio.kt
│       ├── Coleta.kt
│       └── Responsavel.kt
├── presentation/                 # UI Layer
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── dashboard/
│   │   ├── DashboardFragment.kt
│   │   └── DashboardViewModel.kt
│   ├── scanner/
│   │   ├── ScannerActivity.kt
│   │   └── ScannerViewModel.kt
│   ├── coleta/
│   │   ├── ColetaActivity.kt
│   │   └── ColetaViewModel.kt
│   ├── inventario/
│   │   ├── InventarioActivity.kt
│   │   └── InventarioViewModel.kt
│   └── offline/
│       ├── OfflineSyncActivity.kt
│       └── OfflineSyncViewModel.kt
└── util/
    ├── PreferencesManager.kt
    ├── NetworkUtils.kt
    └── ErrorMapper.kt
```

### Dependências (build.gradle)

```gradle
dependencies {
    // Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // ViewModel e LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // QR Code
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.google.zxing:core:3.5.2'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### Exemplo de ViewModel

```kotlin
class InventarioViewModel(
    private val repository: InventarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()
    
    fun loadPatrimoniosByResponsavel(
        responsavelId: Int,
        coletado: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getPatrimoniosByResponsavel(
                responsavelId, 
                page = 0, 
                size = 20, 
                coletado = coletado
            )
            
            result.fold(
                onSuccess = { patrimonios ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        patrimonios = patrimonios
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }
}
```

### Exemplo de Repository

```kotlin
class InventarioRepository(
    private val apiService: ApiService,
    private val patrimonioDao: PatrimonioDao,
    private val offlineDataManager: OfflineDataManager
) {
    
    suspend fun getPatrimoniosByResponsavel(
        responsavelId: Int,
        page: Int,
        size: Int,
        coletado: Boolean?
    ): Result<List<Patrimonio>> {
        return withContext(Dispatchers.IO) {
            try {
                // Tentar buscar online
                if (NetworkUtils.isOnline()) {
                    val response = apiService.getPatrimoniosByResponsavel(
                        responsavelId, page, size, coletado
                    )
                    
                    if (response.isSuccessful) {
                        val patrimonios = response.body()?.data?.map { 
                            it.toDomain() 
                        } ?: emptyList()
                        Result.success(patrimonios)
                    } else {
                        Result.failure(Exception("Erro na requisição"))
                    }
                } else {
                    // Buscar offline
                    val patrimonios = offlineDataManager
                        .getPatrimoniosOfflineByResponsavel(responsavelId)
                    Result.success(patrimonios)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

---

## 💾 Modo Offline

### Funcionalidades

1. **Download de Dados**
   - Patrimônios (paginado)
   - Responsáveis
   - Salas
   - Inventários ativos

2. **Armazenamento Local**
   - SQLite via Room
   - Índices otimizados
   - Transações em lote

3. **Sincronização**
   - Upload de coletas pendentes
   - Retry automático
   - Resolução de conflitos

### Banco de Dados Local

```kotlin
@Database(
    entities = [
        PatrimonioEntity::class,
        ColetaEntity::class,
        ResponsavelEntity::class,
        SalaEntity::class
    ],
    version = 1
)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun responsavelDao(): ResponsavelDao
    abstract fun salaDao(): SalaDao
}
```

### Entidade Patrimônio

```kotlin
@Entity(
    tableName = "patrimonio_offline",
    indices = [
        Index(value = ["numero"]),
        Index(value = ["responsavelId"]),
        Index(value = ["jaColetado"])
    ]
)
data class PatrimonioEntity(
    @PrimaryKey val id: Int,
    val numero: String,
    val descricao: String,
    val responsavelId: Int?,
    val responsavelNome: String?,
    val salaId: Int?,
    val salaNome: String?,
    val jaColetado: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)
```

### OfflineDataManager

```kotlin
class OfflineDataManager(
    private val database: InventarioDatabase,
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun downloadAllData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Download responsáveis
                downloadResponsaveis()
                
                // Download salas
                downloadSalas()
                
                // Download patrimônios (paginado)
                downloadPatrimonios()
                
                // Atualizar timestamp
                preferencesManager.setLastOfflineSync(System.currentTimeMillis())
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private suspend fun downloadPatrimonios() {
        var page = 0
        var hasMore = true
        
        while (hasMore) {
            val response = apiService.getPatrimonios(page, 100)
            if (response.isSuccessful) {
                val patrimonios = response.body()?.data ?: emptyList()
                
                // Salvar em lote
                database.patrimonioDao().insertAll(
                    patrimonios.map { it.toEntity() }
                )
                
                hasMore = patrimonios.size == 100
                page++
            } else {
                hasMore = false
            }
        }
    }
}
```

### Tela de Sincronização

```kotlin
class OfflineSyncActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOfflineSyncBinding
    private lateinit var viewModel: OfflineSyncViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineSyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnDownloadData.setOnClickListener {
            viewModel.downloadOfflineData()
        }
        
        binding.btnSyncColetas.setOnClickListener {
            viewModel.syncPendingColetas()
        }
        
        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setOfflineModeEnabled(isChecked)
        }
    }
}
```

---

## 🛠️ Implementação

### Fase 1: Backend API (3-4 semanas)

**Semana 1-2**: Configuração Base
- [ ] Configurar Spring Security + JWT
- [ ] Criar DTOs mobile
- [ ] Implementar controllers de autenticação
- [ ] Configurar CORS

**Semana 3**: Endpoints Principais
- [ ] Controller de Patrimônio
- [ ] Controller de Coleta
- [ ] Controller de Sincronização

**Semana 4**: Endpoints Complementares
- [ ] Controller de Responsáveis
- [ ] Controller de Salas
- [ ] Testes e documentação

### Fase 2: Android App (4-5 semanas)

**Semana 1**: Setup
- [ ] Criar projeto Android
- [ ] Configurar dependências
- [ ] Estrutura de pacotes
- [ ] Configurar Room Database

**Semana 2**: Autenticação
- [ ] Tela de login
- [ ] Integração com API
- [ ] Armazenamento de token
- [ ] Navegação

**Semana 3**: Scanner e Coleta
- [ ] Integração ZXing
- [ ] Tela de scanner
- [ ] Tela de coleta
- [ ] Validações

**Semana 4**: Inventário e Listagens
- [ ] Tela de inventário
- [ ] Filtros e paginação
- [ ] Busca de patrimônios
- [ ] Estatísticas

**Semana 5**: Modo Offline
- [ ] OfflineDataManager
- [ ] Tela de sincronização
- [ ] Download de dados
- [ ] Upload de coletas

### Fase 3: Testes e Deploy (2-3 semanas)

**Semana 1**: Testes
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Testes de UI
- [ ] Correções de bugs

**Semana 2**: Deploy
- [ ] Configurar CI/CD
- [ ] Build de produção
- [ ] Testes em dispositivos reais
- [ ] Documentação

**Semana 3**: Distribuição
- [ ] Google Play Console
- [ ] Distribuição interna
- [ ] Monitoramento
- [ ] Suporte

**Total Estimado**: 9-12 semanas

---

## 🧪 Testes

### Backend

```java
@SpringBootTest
@AutoConfigureMockMvc
class MobilePatrimonioControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void buscarPorResponsavel_deveRetornarPatrimonios() throws Exception {
        mockMvc.perform(get("/api/mobile/patrimonio/responsavel/1")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }
}
```

### Android

```kotlin
@RunWith(AndroidJUnit4::class)
class InventarioRepositoryTest {
    
    private lateinit var database: InventarioDatabase
    private lateinit var repository: InventarioRepository
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, 
            InventarioDatabase::class.java
        ).build()
        
        repository = InventarioRepository(
            mockApiService,
            database.patrimonioDao(),
            mockOfflineDataManager
        )
    }
    
    @Test
    fun getPatrimonios_offline_deveRetornarDadosLocais() = runTest {
        // Given
        val patrimonios = listOf(
            PatrimonioEntity(1, "123", "Teste", null, null, null, null)
        )
        database.patrimonioDao().insertAll(patrimonios)
        
        // When
        val result = repository.getPatrimoniosOffline()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }
}
```

---

## 🚀 Deploy

### Backend

```bash
# Build
./mvnw clean package -DskipTests

# Run
java -jar target/sistema-inventario-1.2.0.jar --spring.profiles.active=mobile
```

### Android

```bash
# Debug
./gradlew assembleDebug
./gradlew installDebug

# Release
./gradlew assembleRelease

# APK gerado em:
# app/build/outputs/apk/release/app-release.apk
```

### Configuração de Produção

**Backend** (`application-mobile.properties`):
```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000
```

**Android** (`build.gradle`):
```gradle
buildTypes {
    release {
        buildConfigField "String", "API_BASE_URL", 
            '"https://seu-servidor.com/api/mobile/"'
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
    }
}
```

---

## 📚 Referências

### Documentação Técnica

- **Backend**: `src/main/java/com/inventario/mobile/server/`
- **Android**: `InventarioMobile/app/src/main/java/`
- **API Docs**: Swagger UI em `/swagger-ui.html`

### Arquivos Importantes

**Backend**:
- `MobilePatrimonioController.java`
- `MobileColetaController.java`
- `MobileSyncController.java`
- `SecurityConfig.java`

**Android**:
- `InventarioRepository.kt`
- `OfflineDataManager.kt`
- `InventarioDatabase.kt`
- `ApiService.kt`

### Endpoints Completos

Consulte a documentação Swagger para lista completa de endpoints e exemplos de request/response.

### Troubleshooting

**Problema**: App não conecta ao servidor  
**Solução**: Verificar `API_BASE_URL` e network_security_config.xml

**Problema**: Sincronização offline falha  
**Solução**: Verificar permissões de rede e espaço em disco

**Problema**: QR Code não escaneia  
**Solução**: Verificar permissão de câmera e iluminação

---

## 🎯 Conclusão

O sistema mobile está completamente implementado com:

- ✅ Backend API REST funcional
- ✅ App Android nativo
- ✅ Modo offline completo
- ✅ Sincronização bidirecional
- ✅ Scanner QR Code
- ✅ Autenticação JWT
- ✅ Paginação e filtros
- ✅ Testes implementados

**Pronto para produção** com suporte a operação online e offline.

---

**Última atualização**: 07/11/2025  
**Versão**: 1.0.0  
**Mantido por**: Equipe de Desenvolvimento SIHCP
