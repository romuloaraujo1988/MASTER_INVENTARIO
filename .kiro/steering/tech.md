# Technology Stack

## Build System

**Maven** - Primary build tool for Java components
- Version management via `pom.xml`
- Dependency resolution and packaging
- Multi-module project structure

## Core Technologies

### Desktop Application
- **Java 21** - Primary language (LTS version)
- **Swing** - Native GUI framework
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM framework

### Mobile API Backend
- **Spring Boot 3.2.0** - REST API framework
- **Spring Security** - Authentication and authorization
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **Spring WebSocket** - Real-time communication
- **Springdoc OpenAPI 2.0.2** - API documentation (Swagger)

### Android Mobile App
- **Kotlin** - Primary language
- **Android SDK** - Native Android development
- **Gradle** - Build system
- **Room Database** - Local SQLite persistence
- **Retrofit** - HTTP client for API calls
- **Coroutines & Flow** - Asynchronous programming

### Database
- **PostgreSQL 12+** - Primary production database
- **SQLite** - Offline mode and mobile local storage

### Key Libraries

#### Reporting & Export
- **Apache POI 5.4.0** - Excel generation (.xlsx)
- **iText 7.2.5** - PDF generation
- **JFreeChart 1.5.5** - Charts and graphs

#### QR Code
- **ZXing 3.5.2** - QR code generation and scanning

#### Utilities
- **Jackson 2.16.1** - JSON processing
- **Apache Commons** - Utility libraries (Lang3, IO, Collections4, Compress)
- **SLF4J + Log4j2** - Logging framework
- **BCrypt (Spring Security Crypto)** - Password hashing

## Common Commands

### Build & Compile

```bash
# Clean and compile
mvn clean compile

# Package application
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -P fat-jar
```

### Run Application

```bash
# Run desktop application
mvn exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"

# Run mobile API server
mvn exec:java -Dexec.mainClass="com.inventario.MobileApiApplication"

# Run with Spring profile
java -jar target/sistema-inventario-1.2.0.jar --spring.profiles.active=mobile
```

### Android Mobile App

```bash
# Build debug APK
cd InventarioMobile
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug

# Build release APK
.\gradlew.bat assembleRelease

# Clean build
.\gradlew.bat clean build
```

### Database Operations

```bash
# Execute SQL scripts
psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabelas_sispatrimonio.sql

# Backup database
pg_dump -h localhost -U inventario sispatrimonio > backup.sql

# Restore database
psql -h localhost -U inventario -d sispatrimonio < backup.sql
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Run with coverage
mvn clean test jacoco:report
```

### Dependency Management

```bash
# Display dependency tree
mvn dependency:tree

# Copy dependencies to lib folder
mvn dependency:copy-dependencies -DoutputDirectory=lib

# Check for updates
mvn versions:display-dependency-updates
```

## Configuration Files

- `pom.xml` - Maven project configuration
- `application.properties` - Spring Boot configuration
- `application-mobile.properties` - Mobile API specific config
- `configuracao_banco.json` - Database connection settings
- `build.gradle` - Android app build configuration
- `settings.gradle` - Android project settings

## Development Environment

### Required Tools
- JDK 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Android Studio (for mobile app)
- Git

### Recommended IDEs
- IntelliJ IDEA (Java development)
- Eclipse (alternative for Java)
- Android Studio (mobile app development)
- VS Code (general purpose)

## Port Configuration

- **8080** - Mobile API server (default)
- **8081** - Alternative mobile API port
- **5432** - PostgreSQL database (default)
- **WebSocket** - Dynamic port for real-time updates

## Profiles

- `default` - Desktop application mode
- `mobile` - Mobile API server mode
- `dev` - Development with debug logging
- `prod` - Production optimized settings
