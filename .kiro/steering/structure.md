# Project Structure

## Root Organization

```
MASTER_INVENTÁRIO/
├── src/main/java/com/inventario/    # Java source code
├── InventarioMobile/                 # Android mobile app
├── sql/                              # Database scripts
├── lib/                              # External JAR dependencies
├── docs/                             # Documentation
├── DOCUMENTAÇÃO/                     # Portuguese documentation
├── logs/                             # Application logs
├── data/                             # SQLite offline databases
├── dist/                             # Distribution builds
└── target/                           # Maven build output
```

## Java Application Structure

### Package Organization

```
com.inventario/
├── SistemaInventarioApplication.java    # Desktop app entry point
├── MobileApiApplication.java            # Mobile API entry point
├── model/                               # Domain entities (JPA)
├── dao/                                 # Data Access Objects
├── service/                             # Business logic layer
├── view/                                # Swing UI components
├── controller/                          # (Currently unused)
├── config/                              # Configuration classes
├── security/                            # JWT and authentication
├── util/                                # Utility classes
├── offline/                             # Offline mode support
├── ui/                                  # UI controllers
├── dto/                                 # Data Transfer Objects
├── mobile/server/                       # Mobile API backend
│   ├── controller/                      # REST endpoints
│   ├── service/                         # Mobile business logic
│   ├── dto/                             # API request/response objects
│   ├── config/                          # Mobile-specific config
│   ├── security/                        # JWT filters
│   └── util/                            # Mobile utilities
└── test/                                # Test classes
```

## Key Architectural Patterns

### Layered Architecture

1. **Presentation Layer** (`view/`, `mobile/server/controller/`)
   - Swing frames and dialogs for desktop
   - REST controllers for mobile API
   - User input validation

2. **Service Layer** (`service/`, `mobile/server/service/`)
   - Business logic implementation
   - Transaction management
   - Cross-cutting concerns

3. **Data Access Layer** (`dao/`)
   - Database operations
   - Query execution
   - Connection management

4. **Domain Layer** (`model/`)
   - JPA entities
   - Business domain objects
   - Relationships and constraints

### Naming Conventions

#### Java Classes
- **Entities**: `Patrimonio`, `Usuario`, `Coleta`, `Inventario`
- **DAOs**: `PatrimonioDAO`, `UsuarioDAO` (suffix: DAO)
- **Services**: `PatrimonioService`, `AutenticacaoService` (suffix: Service)
- **Views**: `PatrimonioFrame`, `UsuarioFormDialog` (suffix: Frame/Dialog)
- **DTOs**: `MobilePatrimonioDTO`, `DashboardStatsDTO` (suffix: DTO)
- **Controllers**: `MobileAuthController`, `MobileColetaController` (prefix: Mobile, suffix: Controller)

#### Database
- **Tables**: lowercase with underscores (`patrimonio`, `usuario`, `coleta`)
- **Columns**: lowercase with underscores (`numero_patrimonio`, `data_cadastro`)
- **Foreign Keys**: `fk_<table>_<column>`
- **Indexes**: `idx_<table>_<column>`

## Android Mobile App Structure

```
InventarioMobile/app/src/main/
├── java/com/ifmt/inventariomobile/
│   ├── data/                        # Data layer
│   │   ├── local/                   # Room database
│   │   ├── remote/                  # Retrofit API
│   │   └── repository/              # Repository pattern
│   ├── domain/                      # Business logic
│   │   ├── model/                   # Domain models
│   │   └── usecase/                 # Use cases
│   ├── presentation/                # UI layer
│   │   ├── login/                   # Login screen
│   │   ├── main/                    # Main activity
│   │   ├── dashboard/               # Dashboard fragment
│   │   ├── coleta/                  # Collection screens
│   │   └── sala/                    # Room selection
│   └── util/                        # Utilities
├── res/                             # Android resources
│   ├── layout/                      # XML layouts
│   ├── drawable/                    # Images and icons
│   ├── values/                      # Strings, colors, themes
│   └── menu/                        # Menu definitions
└── AndroidManifest.xml              # App configuration
```

### Android Architecture Pattern

**MVVM (Model-View-ViewModel)**
- **Model**: Data classes and repositories
- **View**: Activities and Fragments
- **ViewModel**: LiveData/StateFlow for UI state

## Database Scripts Location

```
sql/
├── criar_tabelas_sispatrimonio.sql      # Main table creation
├── criar_tabela_usuario.sql             # User table
├── criar_tabelas_sistema_inventario.sql # Inventory tables
├── criar_tabelas_mobile.sql             # Mobile-specific tables
├── criar_tabelas_sqlite_offline.sql     # Offline mode tables
├── script_tabela_*.sql                  # Individual table scripts
├── adicionar_*.sql                      # Column additions
├── criar_view_*.sql                     # Database views
└── triggers_historico_patrimonio.sql    # Audit triggers
```

## Configuration Files

### Java Application
- `configuracao_banco.json` - Database connection (user home directory)
- `application.properties` - Spring Boot settings
- `application-mobile.properties` - Mobile API settings
- `pom.xml` - Maven dependencies and build

### Android App
- `build.gradle` (project level) - Gradle configuration
- `build.gradle` (app level) - App dependencies
- `local.properties` - SDK location
- `gradle.properties` - Build properties

## Documentation Structure

```
DOCUMENTAÇÃO/
├── MANUAL_DO_USUARIO.md                 # User manual
├── PLANO_IMPLEMENTACAO_*.md             # Implementation plans
├── INSTRUCOES_*.md                      # Instructions
└── FUNCIONALIDADE_*.md                  # Feature docs

docs/
├── uml/                                 # UML diagrams
├── COLETAFRAME_V2_GUIA_*.md            # Collection guides
└── SINGLE_INSTANCE.md                   # Technical docs
```

## Important Directories

### Runtime Directories
- `logs/` - Application logs (sistema-inventario.log)
- `data/` - SQLite databases for offline mode
- `target/` - Maven build artifacts (gitignored)
- `.kiro/` - Kiro AI assistant configuration

### Distribution
- `dist/` - Production builds and APKs
- `SISTEMA_INVENTARIO_PRODUCAO/` - Production package
- `lib/` - External JAR dependencies (not in Maven)

## Module Separation

### Desktop Application
- Entry: `SistemaInventarioApplication.java`
- UI: Swing-based (`view/` package)
- Database: Direct JDBC via DAOs

### Mobile API Server
- Entry: `MobileApiApplication.java`
- API: REST endpoints (`mobile/server/controller/`)
- Auth: JWT-based authentication
- Port: 8080 or 8081

### Android Mobile App
- Separate Gradle project in `InventarioMobile/`
- Communicates with Mobile API via REST
- Local SQLite for offline capability

## Code Organization Principles

1. **Separation of Concerns**: Clear boundaries between layers
2. **Single Responsibility**: Each class has one primary purpose
3. **Package by Feature**: Mobile API organized by feature area
4. **Package by Layer**: Desktop app organized by technical layer
5. **Dependency Direction**: Always toward more stable abstractions

## File Naming Patterns

- **SQL Scripts**: `<action>_<entity>.sql` (e.g., `criar_tabela_usuario.sql`)
- **Documentation**: `<TOPIC>_<DESCRIPTION>.md` (uppercase for main docs)
- **Batch Scripts**: `<action>-<target>.bat` or `.ps1`
- **Java Classes**: PascalCase with descriptive suffixes
- **Kotlin Files**: PascalCase matching class name
- **Resources**: snake_case for XML files
