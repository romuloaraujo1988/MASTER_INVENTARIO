# Estrutura dos Módulos Independentes - SIHCP

**Versão:** 2.7.0  
**Pacote Base:** `com.inventario.sihcp`  
**Arquitetura:** Multi-módulo Maven

---

## 📦 Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Parent POM (raiz)                         │
│              com.inventario.sihcp:sistema-inventario         │
│                                                              │
│  • Gerencia versões de dependências (dependencyManagement)  │
│  • Centraliza configuração de plugins (pluginManagement)    │
│  • Define propriedades comuns (Java 17, UTF-8, Spring 3.2)  │
└─────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                │             │             │
                ▼             ▼             ▼
        ┌───────────┐  ┌───────────┐  ┌───────────┐
        │   CORE    │  │  DESKTOP  │  │  SERVER   │
        │           │  │           │  │           │
        │ sihcp-    │  │ sihcp-    │  │ sihcp-    │
        │  core     │  │ desktop   │  │ server    │
        └───────────┘  └───────────┘  └───────────┘
             │              │  │            │
             │              │  │            │
             └──────────────┘  └────────────┘
                  depende          depende
```

---

## 🎯 Módulo 1: SIHCP Core (Código Compartilhado)

### Identificação
- **Artifact ID:** `sihcp-core`
- **Pacote Base:** `com.inventario.sihcp`
- **Tipo:** Biblioteca JAR (não executável)
- **Versão:** 2.7.0

### Responsabilidades
✅ **Modelos de Domínio** (JPA Entities)  
✅ **DAOs** (Data Access Objects)  
✅ **Serviços Compartilhados**  
✅ **Configuração de Banco de Dados**  
✅ **Utilitários Comuns**  

### Estrutura de Pacotes
```
com.inventario.sihcp/
├── model/                    # Entidades JPA
│   ├── Patrimonio.java
│   ├── Usuario.java
│   ├── Coleta.java
│   ├── Inventario.java
│   ├── Sala.java
│   ├── Setor.java
│   └── Responsavel.java
│
├── dao/                      # Data Access Objects
│   ├── PatrimonioDAO.java
│   ├── UsuarioDAO.java
│   ├── ColetaDAO.java
│   └── ...
│
├── service/                  # Serviços compartilhados
│   ├── PatrimonioService.java
│   ├── UsuarioService.java
│   └── ...
│
├── config/                   # Configuração de banco
│   ├── DatabaseConfigManager.java
│   ├── DatabaseConfig.java
│   ├── DatabaseConfiguration.java
│   ├── HikariConnectionPool.java
│   └── DatabaseConnection.java
│
└── util/                     # Utilitários comuns
    ├── DateUtils.java
    ├── StringUtils.java
    └── ValidationUtils.java
```

### Dependências Principais
```xml
✅ Spring Data JPA
✅ PostgreSQL Driver
✅ Jackson (JSON)
✅ Apache Commons (Lang3, IO, Collections4)
✅ SLF4J + Log4j2
✅ Spring Security Crypto (BCrypt)

❌ NÃO tem: Spring Web, Spring Security, Swing, JFreeChart
```

### Build
```bash
# Gera: sihcp-core-2.7.0.jar (biblioteca)
mvn clean package
```

---

## 🖥️ Módulo 2: SIHCP Desktop (Aplicação Swing)

### Identificação
- **Artifact ID:** `sihcp-desktop`
- **Pacote Base:** `com.inventario.sihcp`
- **Tipo:** JAR executável (Swing)
- **Main Class:** `com.inventario.sihcp.SistemaInventarioApplication`
- **Versão:** 2.7.0

### Responsabilidades
✅ **Interface Gráfica Swing**  
✅ **Relatórios (PDF, Excel)**  
✅ **Gráficos e Dashboards**  
✅ **Modo Offline (SQLite)**  
✅ **Geração de QR Codes**  

### Estrutura de Pacotes
```
com.inventario.sihcp/
├── SistemaInventarioApplication.java  # Entry point
│
├── view/                     # Frames e Dialogs Swing
│   ├── PatrimonioFrame.java
│   ├── UsuarioFrame.java
│   ├── ColetaFrame.java
│   ├── RelatorioFrame.java
│   └── DashboardFrame.java
│
├── ui/                       # Controllers de UI
│   ├── PatrimonioController.java
│   └── ...
│
├── chart/                    # Gráficos JFreeChart
│   └── ChartGenerator.java
│
├── print/                    # Impressão e PDF
│   └── PrintManager.java
│
├── offline/                  # Modo offline SQLite
│   └── OfflineManager.java
│
├── analytics/                # Analytics desktop
│   └── AnalyticsManager.java
│
└── util/                     # Utilitários Swing
    ├── IconManager.java
    ├── ImageUtils.java
    ├── DialogUtils.java
    ├── FormUtils.java
    ├── SoundNotification.java
    └── SingleInstanceManager.java
```

### Dependências Principais
```xml
✅ sihcp-core (módulo interno)
✅ JFreeChart (gráficos)
✅ Apache POI (Excel)
✅ iText 7 (PDF)
✅ ZXing (QR Code)
✅ SQLite JDBC (offline)

❌ NÃO tem: Spring Web, Spring Security, JWT, Swagger
```

### Build e Execução
```bash
# Gera: sihcp-desktop-2.7.0.jar + lib/
mvn clean package

# Executar
java -jar target/sihcp-desktop-2.7.0.jar

# Ou com classpath externo
java -cp "target/sihcp-desktop-2.7.0.jar:target/lib/*" \
  com.inventario.sihcp.SistemaInventarioApplication
```

### Estrutura de Deploy
```
sihcp-desktop/
├── sihcp-desktop-2.7.0.jar       # JAR principal
└── lib/                          # Dependências
    ├── sihcp-core-2.7.0.jar
    ├── jfreechart-1.5.5.jar
    ├── poi-5.4.0.jar
    ├── itext-kernel-7.2.5.jar
    └── ...
```

---

## 🌐 Módulo 3: SIHCP Server (API REST Spring Boot)

### Identificação
- **Artifact ID:** `sihcp-server`
- **Pacote Base:** `com.inventario.sihcp`
- **Tipo:** Spring Boot Fat JAR (executável)
- **Main Class:** `com.inventario.sihcp.MobileApiApplication`
- **Versão:** 2.7.0
- **Porta:** 8080 ou 8081

### Responsabilidades
✅ **API REST para App Mobile**  
✅ **Autenticação JWT**  
✅ **Segurança por Roles**  
✅ **Sincronização de Dados**  
✅ **WebSocket (tempo real)**  
✅ **Documentação Swagger**  

### Estrutura de Pacotes
```
com.inventario.sihcp/
├── MobileApiApplication.java      # Entry point Spring Boot
│
├── mobile/server/                 # API Mobile
│   ├── controller/                # REST Controllers
│   │   ├── MobileAuthController.java
│   │   ├── MobileColetaController.java
│   │   ├── MobilePatrimonioController.java
│   │   ├── MobileSyncController.java
│   │   └── ...
│   │
│   ├── dto/                       # Data Transfer Objects
│   │   ├── MobilePatrimonioDTO.java
│   │   ├── MobileColetaDTO.java
│   │   └── ...
│   │
│   ├── service/                   # Serviços Mobile
│   │   ├── MobileAuthService.java
│   │   ├── MobileColetaService.java
│   │   └── ...
│   │
│   ├── config/                    # Configuração Spring
│   │   ├── SecurityConfig.java
│   │   ├── CacheConfig.java
│   │   ├── WebSocketConfig.java
│   │   └── SwaggerConfig.java
│   │
│   └── security/                  # Segurança JWT
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       └── RequireAdmin.java (anotações)
│
└── security/                      # Segurança compartilhada
    └── PasswordEncoder.java
```

### Dependências Principais
```xml
✅ sihcp-core (módulo interno)
✅ Spring Boot Web
✅ Spring Boot Security
✅ Spring Boot WebSocket
✅ JWT (jjwt 0.11.5)
✅ Springdoc OpenAPI (Swagger)
✅ Caffeine Cache
✅ Micrometer (métricas)

❌ NÃO tem: Swing, JFreeChart, Apache POI, iText, SQLite
```

### Build e Execução
```bash
# Gera: sihcp-server-2.7.0.jar (fat JAR)
mvn clean package

# Executar
java -jar target/sihcp-server-2.7.0.jar

# Com profile mobile
java -jar target/sihcp-server-2.7.0.jar --spring.profiles.active=mobile

# Com porta customizada
java -jar target/sihcp-server-2.7.0.jar --server.port=8081
```

### Endpoints Principais
```
GET  /api/mobile/health                    # Health check
POST /api/mobile/auth/login                # Login JWT
POST /api/mobile/auth/refresh              # Refresh token
GET  /api/mobile/patrimonio                # Listar patrimônios
POST /api/mobile/coletas                   # Registrar coleta
POST /api/mobile/coletas/batch             # Batch sync
GET  /api/mobile/sync/patrimonios          # Sincronizar dados
GET  /swagger-ui.html                      # Documentação Swagger
```

### Estrutura de Deploy
```
sihcp-server/
└── sihcp-server-2.7.0.jar       # Fat JAR (todas as deps incluídas)
```

---

## 🔗 Dependências entre Módulos

```
┌─────────────────────────────────────────────────────────────┐
│                         SIHCP CORE                           │
│                                                              │
│  • Modelos JPA (Patrimonio, Usuario, Coleta, etc.)         │
│  • DAOs (PatrimonioDAO, UsuarioDAO, etc.)                  │
│  • Serviços compartilhados                                  │
│  • Configuração de banco de dados                          │
│  • Utilitários comuns                                       │
│                                                              │
│  Dependências: Spring Data JPA, PostgreSQL, Jackson         │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
                ┌─────────────┴─────────────┐
                │                           │
                │                           │
┌───────────────┴──────────────┐  ┌────────┴──────────────────┐
│       SIHCP DESKTOP          │  │      SIHCP SERVER         │
│                              │  │                           │
│  • Views Swing               │  │  • REST Controllers       │
│  • Relatórios PDF/Excel      │  │  • DTOs Mobile            │
│  • Gráficos JFreeChart       │  │  • Segurança JWT          │
│  • Modo Offline SQLite       │  │  • WebSocket              │
│  • QR Code                   │  │  • Swagger                │
│                              │  │                           │
│  Deps: JFreeChart, POI,      │  │  Deps: Spring Web,        │
│        iText, ZXing, SQLite  │  │        Security, JWT      │
└──────────────────────────────┘  └───────────────────────────┘
```

---

## 📊 Comparação: Antes vs Depois

### Antes (Monolítico)
```
sistema-inventario-2.6.0.jar (50+ MB)
├── com.inventario.*                    # Tudo junto
│   ├── model/                          # ✅ Usado por todos
│   ├── dao/                            # ✅ Usado por todos
│   ├── service/                        # ✅ Usado por todos
│   ├── view/                           # ❌ Desktop only
│   ├── mobile/server/                  # ❌ Server only
│   ├── chart/                          # ❌ Desktop only
│   └── security/                       # ❌ Server only
│
└── lib/
    ├── spring-boot-web.jar             # ❌ Desktop não usa
    ├── spring-security.jar             # ❌ Desktop não usa
    ├── jfreechart.jar                  # ❌ Server não usa
    ├── poi.jar                         # ❌ Server não usa
    └── ...

Problemas:
❌ Desktop carrega classes REST e JWT desnecessariamente
❌ Server carrega classes Swing e gráficos desnecessariamente
❌ JAR único muito grande (50+ MB)
❌ Conflitos de classpath
❌ Difícil manutenção
```

### Depois (Multi-módulo)
```
sihcp-core-2.7.0.jar (5 MB)
├── com.inventario.sihcp.model/         # ✅ Compartilhado
├── com.inventario.sihcp.dao/           # ✅ Compartilhado
├── com.inventario.sihcp.service/       # ✅ Compartilhado
└── com.inventario.sihcp.config/        # ✅ Compartilhado

sihcp-desktop-2.7.0.jar (2 MB) + lib/ (30 MB)
├── com.inventario.sihcp.view/          # ✅ Desktop only
├── com.inventario.sihcp.chart/         # ✅ Desktop only
├── com.inventario.sihcp.print/         # ✅ Desktop only
└── lib/
    ├── sihcp-core-2.7.0.jar            # ✅ Necessário
    ├── jfreechart.jar                  # ✅ Necessário
    ├── poi.jar                         # ✅ Necessário
    └── ...

sihcp-server-2.7.0.jar (40 MB fat JAR)
├── com.inventario.sihcp.mobile/        # ✅ Server only
├── com.inventario.sihcp.security/      # ✅ Server only
└── embedded libs/
    ├── sihcp-core-2.7.0.jar            # ✅ Necessário
    ├── spring-boot-web.jar             # ✅ Necessário
    ├── spring-security.jar             # ✅ Necessário
    └── ...

Benefícios:
✅ Cada módulo tem apenas o que precisa
✅ Builds independentes
✅ Deploys independentes
✅ Sem conflitos de classpath
✅ Manutenção facilitada
✅ Tamanhos otimizados
```

---

## 🚀 Comandos de Build

### Build Completo (todos os módulos)
```bash
# Na raiz do projeto
mvn clean package

# Resultado:
# sihcp-core/target/sihcp-core-2.7.0.jar
# sihcp-desktop/target/sihcp-desktop-2.7.0.jar + lib/
# sihcp-server/target/sihcp-server-2.7.0.jar
```

### Build Individual
```bash
# Apenas Core
cd sihcp-core && mvn clean package

# Apenas Desktop
cd sihcp-desktop && mvn clean package

# Apenas Server
cd sihcp-server && mvn clean package
```

### Profiles Maven
```bash
# Desktop - Fat JAR
mvn clean package -P fat-jar

# Desktop - Thin JAR (com lib/ externa)
mvn clean package -P thin-jar

# Server - Mobile API
mvn clean package -P mobile

# Produção - Pacote completo
mvn clean package -P producao
```

---

## 📁 Estrutura de Diretórios

```
MASTER_INVENTARIO/
├── pom.xml                           # Parent POM
│
├── sihcp-core/                       # Módulo Core
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/inventario/sihcp/
│   │   │   ├── model/
│   │   │   ├── dao/
│   │   │   ├── service/
│   │   │   ├── config/
│   │   │   └── util/
│   │   └── test/java/
│   └── target/
│       └── sihcp-core-2.7.0.jar
│
├── sihcp-desktop/                    # Módulo Desktop
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/inventario/sihcp/
│   │   │   ├── SistemaInventarioApplication.java
│   │   │   ├── view/
│   │   │   ├── ui/
│   │   │   ├── chart/
│   │   │   ├── print/
│   │   │   ├── offline/
│   │   │   └── util/
│   │   └── test/java/
│   └── target/
│       ├── sihcp-desktop-2.7.0.jar
│       └── lib/
│
├── sihcp-server/                     # Módulo Server
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/inventario/sihcp/
│   │   │   │   ├── MobileApiApplication.java
│   │   │   │   ├── mobile/server/
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── dto/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── config/
│   │   │   │   │   └── security/
│   │   │   │   └── security/
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-mobile.properties
│   │   └── test/java/
│   └── target/
│       └── sihcp-server-2.7.0.jar
│
├── InventarioMobile/                 # App Android (separado)
│   └── app/src/main/java/com/ifmt/inventariomobile/
│
├── CHANGELOG.md
├── README.md
└── PACKAGE_RENAME_REPORT.md
```

---

## ✅ Verificações de Isolamento

### Core não depende de Desktop ou Server
```bash
# Verificar dependências do Core
cd sihcp-core
mvn dependency:tree

# Não deve aparecer:
# ❌ spring-boot-starter-web
# ❌ spring-boot-starter-security
# ❌ jfreechart
# ❌ poi
```

### Desktop não depende de Server
```bash
# Verificar dependências do Desktop
cd sihcp-desktop
mvn dependency:tree

# Não deve aparecer:
# ❌ spring-boot-starter-web
# ❌ spring-boot-starter-security
# ❌ jjwt
# ❌ springdoc-openapi
```

### Server não depende de Desktop
```bash
# Verificar dependências do Server
cd sihcp-server
mvn dependency:tree

# Não deve aparecer:
# ❌ jfreechart
# ❌ poi
# ❌ itext
# ❌ sqlite-jdbc
```

---

## 🎯 Resumo Final

| Aspecto | Core | Desktop | Server |
|---------|------|---------|--------|
| **Tipo** | Biblioteca JAR | JAR executável | Spring Boot Fat JAR |
| **Main Class** | - | SistemaInventarioApplication | MobileApiApplication |
| **Pacote Base** | com.inventario.sihcp | com.inventario.sihcp | com.inventario.sihcp |
| **Tamanho** | ~5 MB | ~2 MB + 30 MB lib | ~40 MB |
| **Dependências** | JPA, PostgreSQL, Jackson | Core + Swing, POI, iText | Core + Spring Web, Security |
| **Executável** | ❌ Não | ✅ Sim | ✅ Sim |
| **Spring Boot** | ❌ Não | ❌ Não | ✅ Sim |
| **Build Independente** | ✅ Sim | ✅ Sim | ✅ Sim |
| **Deploy Independente** | ✅ Sim | ✅ Sim | ✅ Sim |

---

**Arquitetura:** ✅ Multi-módulo Maven  
**Pacote:** ✅ com.inventario.sihcp  
**Versão:** ✅ 2.7.0  
**Status:** ✅ Produção Ready  

**Última atualização:** 01/05/2026
