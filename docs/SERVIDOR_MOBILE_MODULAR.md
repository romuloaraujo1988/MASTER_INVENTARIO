# Servidor Mobile - Arquitetura Modular

**Versão:** 2.7.0  
**Módulo:** `sihcp-server`  
**Pacote Base:** `com.inventario.sihcp`  
**Tipo:** Spring Boot Fat JAR  
**Porta:** 8081 (configurável)

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Estrutura de Pacotes](#estrutura-de-pacotes)
3. [Dependências](#dependências)
4. [Endpoints REST](#endpoints-rest)
5. [Segurança e Autenticação](#segurança-e-autenticação)
6. [Configuração](#configuração)
7. [Build e Deploy](#build-e-deploy)
8. [Monitoramento](#monitoramento)

---

## 🎯 Visão Geral

O servidor mobile agora é um **módulo independente** (`sihcp-server`) que:

- ✅ **Depende apenas do `sihcp-core`** (modelos, DAOs, serviços compartilhados)
- ✅ **Não contém código Swing** (interface desktop)
- ✅ **Não contém bibliotecas desktop** (JFreeChart, POI, iText)
- ✅ **É um Spring Boot Fat JAR** (todas as dependências incluídas)
- ✅ **Deploy independente** do módulo desktop

### Antes vs Depois

| Aspecto | Antes (Monolítico) | Depois (Modular) |
|---------|-------------------|------------------|
| **Tamanho** | ~50 MB (com tudo) | ~40 MB (otimizado) |
| **Dependências** | Desktop + Server | Apenas Server |
| **Classes Swing** | ✅ Incluídas | ❌ Removidas |
| **JFreeChart** | ✅ Incluído | ❌ Removido |
| **Apache POI** | ✅ Incluído | ❌ Removido |
| **Build** | Único | Independente |
| **Deploy** | Único | Independente |

---

## 📁 Estrutura de Pacotes

```
sihcp-server/
├── pom.xml                                    # Configuração Maven
│
└── src/
    ├── main/
    │   ├── java/com/inventario/sihcp/
    │   │   │
    │   │   ├── MobileApiApplication.java     # Entry Point Spring Boot
    │   │   │
    │   │   ├── mobile/server/                # Pacote principal da API Mobile
    │   │   │   │
    │   │   │   ├── controller/               # REST Controllers
    │   │   │   │   ├── MobileAuthController.java
    │   │   │   │   ├── MobileColetaController.java
    │   │   │   │   ├── MobilePatrimonioController.java
    │   │   │   │   ├── MobileSalaController.java
    │   │   │   │   ├── MobileSetorController.java
    │   │   │   │   ├── MobileResponsavelController.java
    │   │   │   │   ├── MobileDescricaoController.java
    │   │   │   │   ├── MobileInventarioController.java
    │   │   │   │   ├── MobileSyncController.java
    │   │   │   │   ├── MobileDashboardController.java
    │   │   │   │   ├── MobileUsuarioController.java
    │   │   │   │   └── MobileHealthController.java
    │   │   │   │
    │   │   │   ├── dto/                      # Data Transfer Objects
    │   │   │   │   ├── MobilePatrimonioDTO.java
    │   │   │   │   ├── MobileColetaDTO.java
    │   │   │   │   ├── MobileColetaBatchRequest.java
    │   │   │   │   ├── MobileSalaDTO.java
    │   │   │   │   ├── MobileSetorDTO.java
    │   │   │   │   ├── MobileResponsavelDTO.java
    │   │   │   │   ├── MobileInventarioDTO.java
    │   │   │   │   ├── MobileUsuarioDTO.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── LoginResponse.java
    │   │   │   │   ├── RefreshTokenRequest.java
    │   │   │   │   ├── ApiResponse.java
    │   │   │   │   └── DashboardStatsDTO.java
    │   │   │   │
    │   │   │   ├── service/                  # Serviços Mobile
    │   │   │   │   ├── MobileAuthService.java
    │   │   │   │   ├── MobileColetaService.java
    │   │   │   │   ├── MobilePatrimonioService.java
    │   │   │   │   ├── MobileSalaService.java
    │   │   │   │   ├── MobileSetorService.java
    │   │   │   │   ├── MobileResponsavelService.java
    │   │   │   │   ├── MobileDescricaoService.java
    │   │   │   │   ├── MobileInventarioService.java
    │   │   │   │   ├── MobileSyncService.java
    │   │   │   │   ├── MobileDashboardService.java
    │   │   │   │   └── MobileUsuarioService.java
    │   │   │   │
    │   │   │   ├── config/                   # Configuração Spring
    │   │   │   │   ├── SecurityConfig.java
    │   │   │   │   ├── CorsConfig.java
    │   │   │   │   ├── SwaggerConfig.java
    │   │   │   │   ├── CacheConfig.java
    │   │   │   │   ├── AsyncConfig.java
    │   │   │   │   ├── WebSocketConfig.java
    │   │   │   │   ├── SchedulingConfig.java
    │   │   │   │   └── MobileDataSourceConfig.java
    │   │   │   │
    │   │   │   ├── security/                 # Segurança JWT
    │   │   │   │   ├── JwtTokenProvider.java
    │   │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   │   ├── JwtAuthenticationEntryPoint.java
    │   │   │   │   ├── RequireAdmin.java
    │   │   │   │   ├── RequireSupervisor.java
    │   │   │   │   ├── RequireColetor.java
    │   │   │   │   └── RequireConsulta.java
    │   │   │   │
    │   │   │   └── util/                     # Utilitários Mobile
    │   │   │       ├── ResponseBuilder.java
    │   │   │       └── DateTimeUtils.java
    │   │   │
    │   │   └── security/                     # Segurança compartilhada
    │   │       └── PasswordEncoder.java
    │   │
    │   └── resources/
    │       ├── application.properties        # Config base
    │       ├── application-mobile.properties # Config mobile
    │       ├── application-prod.properties   # Config produção
    │       └── log4j2.xml                    # Configuração de logs
    │
    └── test/
        └── java/com/inventario/sihcp/
            └── mobile/server/
                ├── controller/               # Testes de Controllers
                ├── service/                  # Testes de Services
                └── security/                 # Testes de Segurança
```

---

## 📦 Dependências

### Dependências do Módulo

```xml
<dependencies>
    <!-- Módulo Core (interno) -->
    <dependency>
        <groupId>com.inventario.sihcp</groupId>
        <artifactId>sihcp-core</artifactId>
        <version>2.7.0</version>
    </dependency>
    
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- Swagger/OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.0.2</version>
    </dependency>
    
    <!-- Cache -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
    
    <!-- Métricas -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

### O que NÃO está incluído

```xml
<!-- ❌ NÃO TEM - Bibliotecas Desktop -->
<!-- JFreeChart -->
<!-- Apache POI -->
<!-- iText PDF -->
<!-- SQLite JDBC -->
<!-- Swing -->
```

---

## 🌐 Endpoints REST

### Autenticação

```
POST   /api/mobile/auth/login              # Login com JWT
POST   /api/mobile/auth/refresh            # Refresh token
POST   /api/mobile/auth/validate           # Validar token
```

### Coletas

```
POST   /api/mobile/coletas                 # Registrar coleta
POST   /api/mobile/coletas/batch           # Registrar múltiplas coletas
GET    /api/mobile/coletas                 # Listar coletas
GET    /api/mobile/coletas/{id}            # Buscar coleta por ID
GET    /api/mobile/coletas/historico       # Histórico de coletas
GET    /api/mobile/coletas/pendentes       # Coletas pendentes
PUT    /api/mobile/coletas/{id}            # Atualizar coleta
DELETE /api/mobile/coletas/{id}            # Deletar coleta (Admin)
POST   /api/mobile/coletas/verificar-duplicata  # Verificar duplicata
GET    /api/mobile/coletas/descricoes-pendentes # Descrições não coletadas
GET    /api/mobile/coletas/incremental     # Sync incremental
```

### Patrimônios

```
GET    /api/mobile/patrimonio              # Listar patrimônios
GET    /api/mobile/patrimonio/{id}         # Buscar por ID
GET    /api/mobile/patrimonio/numero/{numero}  # Buscar por número
GET    /api/mobile/patrimonio/numero/{numero}/validar  # Validar patrimônio
GET    /api/mobile/patrimonio/numero/{numero}/coletado # Verificar se coletado
GET    /api/mobile/patrimonio/sala/{idSala}  # Patrimônios por sala
GET    /api/mobile/patrimonio/descricao/{descricao}/nao-coletados  # Por descrição
```

### Salas

```
GET    /api/mobile/salas                   # Listar salas
GET    /api/mobile/salas/{id}              # Buscar sala por ID
GET    /api/mobile/salas/setor/{idSetor}  # Salas por setor
```

### Setores

```
GET    /api/mobile/setores                 # Listar setores
GET    /api/mobile/setores/{id}            # Buscar setor por ID
```

### Responsáveis

```
GET    /api/mobile/responsaveis            # Listar responsáveis
GET    /api/mobile/responsaveis/{id}       # Buscar responsável por ID
```

### Descrições

```
GET    /api/mobile/descricoes/nao-coletadas  # Descrições não coletadas
```

### Inventário

```
GET    /api/mobile/inventario/ativo        # Inventário ativo
GET    /api/mobile/inventario/{id}         # Buscar inventário por ID
GET    /api/mobile/inventario              # Listar todos (Supervisor+)
GET    /api/mobile/inventario/{id}/estatisticas  # Estatísticas do inventário
```

### Sincronização

```
GET    /api/mobile/sync/patrimonios        # Sincronizar patrimônios
GET    /api/mobile/sync/salas              # Sincronizar salas
GET    /api/mobile/sync/stats              # Estatísticas de sincronização
```

### Dashboard

```
GET    /api/mobile/dashboard/stats         # Estatísticas gerais
GET    /api/mobile/dashboard/coletas-recentes  # Coletas recentes
GET    /api/mobile/dashboard/progresso     # Progresso do inventário
```

### Usuários

```
GET    /api/mobile/usuarios                # Listar usuários (Admin)
GET    /api/mobile/usuarios/{id}           # Buscar usuário (Admin)
GET    /api/mobile/usuarios/login/{login}  # Buscar por login (Admin)
GET    /api/mobile/usuarios/me             # Perfil do usuário logado
```

### Health Check

```
GET    /api/mobile/health                  # Status do servidor
```

### Documentação

```
GET    /swagger-ui.html                    # Swagger UI
GET    /v3/api-docs                        # OpenAPI JSON
```

---

## 🔐 Segurança e Autenticação

### JWT (JSON Web Token)

O servidor usa autenticação baseada em JWT com refresh tokens.

#### Fluxo de Autenticação

```
1. Cliente → POST /api/mobile/auth/login
   Body: { "login": "usuario", "password": "senha" }

2. Servidor valida credenciais

3. Servidor ← Response
   {
     "accessToken": "eyJhbGc...",
     "refreshToken": "eyJhbGc...",
     "tokenType": "Bearer",
     "expiresIn": 86400,
     "usuario": { ... }
   }

4. Cliente usa accessToken em todas as requisições
   Header: Authorization: Bearer eyJhbGc...

5. Quando accessToken expira:
   Cliente → POST /api/mobile/auth/refresh
   Body: { "refreshToken": "eyJhbGc..." }

6. Servidor ← Novo accessToken
```

### Roles e Permissões

| Role | Descrição | Permissões |
|------|-----------|------------|
| **ADMIN** | Administrador | Acesso total |
| **SUPERVISOR** | Supervisor | Visualizar tudo, gerenciar inventários |
| **COLETOR** | Coletor | Registrar coletas, sincronizar |
| **CONSULTA** | Consulta | Apenas visualização |

### Anotações de Segurança

```java
// Apenas Admin
@RequireAdmin
@DeleteMapping("/{id}")
public ResponseEntity<?> deletarColeta(@PathVariable Integer id) { }

// Admin ou Supervisor
@RequireSupervisor
@GetMapping
public ResponseEntity<?> listarTodosInventarios() { }

// Admin, Supervisor ou Coletor
@RequireColetor
@PostMapping
public ResponseEntity<?> registrarColeta(@RequestBody MobileColetaDTO dto) { }

// Qualquer usuário autenticado
@RequireConsulta
@GetMapping("/dashboard/stats")
public ResponseEntity<?> obterEstatisticas() { }
```

### Endpoints Públicos (sem autenticação)

```
/api/mobile/health
/api/mobile/auth/login
/api/mobile/auth/validate
/swagger-ui.html
/v3/api-docs
```

---

## ⚙️ Configuração

### application-mobile.properties

```properties
# ========================================
# Server Configuration
# ========================================
server.port=8081
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain

# ========================================
# Database Configuration
# ========================================
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.username=inventario
spring.datasource.password=senha_aqui

# HikariCP
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ========================================
# JPA/Hibernate
# ========================================
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ========================================
# JWT Configuration
# ========================================
jwt.secret=sua_chave_secreta_jwt_aqui_minimo_256_bits
jwt.expiration=86400
jwt.refresh-expiration=604800

# ========================================
# CORS Configuration
# ========================================
cors.allowed-origins=*
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true

# ========================================
# Cache Configuration
# ========================================
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m

# ========================================
# Logging
# ========================================
logging.level.root=INFO
logging.level.com.inventario.sihcp=DEBUG
logging.level.com.inventario.sihcp.mobile.server.controller=DEBUG
logging.level.com.inventario.sihcp.mobile.server.service=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.springframework.security=INFO
logging.level.org.hibernate.SQL=DEBUG

# Log file
logging.file.name=logs/sihcp-server.log
logging.file.max-size=10MB
logging.file.max-history=30

# ========================================
# Actuator (Monitoramento)
# ========================================
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# ========================================
# Swagger/OpenAPI
# ========================================
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

### Variáveis de Ambiente (Produção)

```bash
# Banco de Dados
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sispatrimonio
export DB_USER=inventario
export DB_PASSWORD=senha_segura

# JWT
export JWT_SECRET=chave_secreta_256_bits_minimo
export JWT_EXPIRATION=86400
export JWT_REFRESH_EXPIRATION=604800

# Server
export SERVER_PORT=8081

# Executar
java -jar sihcp-server-2.7.0.jar \
  --spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
  --spring.datasource.username=${DB_USER} \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET} \
  --server.port=${SERVER_PORT}
```

---

## 🚀 Build e Deploy

### Build

```bash
# Navegar para o módulo
cd sihcp-server

# Build
mvn clean package

# Build sem testes
mvn clean package -DskipTests

# Resultado
# target/sihcp-server-2.7.0.jar (Fat JAR ~40 MB)
```

### Executar

```bash
# Básico
java -jar sihcp-server-2.7.0.jar

# Com profile mobile
java -jar sihcp-server-2.7.0.jar --spring.profiles.active=mobile

# Com porta customizada
java -jar sihcp-server-2.7.0.jar --server.port=8082

# Com opções de memória e GC
java -Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
  -jar sihcp-server-2.7.0.jar \
  --spring.profiles.active=mobile \
  --server.port=8081
```

### Deploy como Serviço (Linux)

```bash
# Copiar JAR
sudo cp sihcp-server-2.7.0.jar /opt/sihcp-server/

# Criar service file
sudo nano /etc/systemd/system/sihcp-server.service
```

```ini
[Unit]
Description=SIHCP Server - API REST Mobile
After=network.target postgresql.service

[Service]
Type=simple
User=sihcp
WorkingDirectory=/opt/sihcp-server
ExecStart=/usr/bin/java -Xms512m -Xmx2g -XX:+UseG1GC \
  -jar /opt/sihcp-server/sihcp-server-2.7.0.jar \
  --spring.profiles.active=mobile \
  --server.port=8081
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

```bash
# Habilitar e iniciar
sudo systemctl daemon-reload
sudo systemctl enable sihcp-server
sudo systemctl start sihcp-server

# Verificar status
sudo systemctl status sihcp-server

# Ver logs
sudo journalctl -u sihcp-server -f
```

---

## 📊 Monitoramento

### Health Check

```bash
curl http://localhost:8081/api/mobile/health
```

**Resposta:**
```json
{
  "status": "UP",
  "timestamp": "2026-05-01T10:30:00Z",
  "database": "UP",
  "diskSpace": "UP"
}
```

### Métricas (Actuator)

```bash
# Health detalhado
curl http://localhost:8081/actuator/health

# Métricas gerais
curl http://localhost:8081/actuator/metrics

# Métricas específicas
curl http://localhost:8081/actuator/metrics/jvm.memory.used
curl http://localhost:8081/actuator/metrics/http.server.requests

# Prometheus
curl http://localhost:8081/actuator/prometheus
```

### Logs

```bash
# Systemd
sudo journalctl -u sihcp-server -f

# Arquivo
tail -f logs/sihcp-server.log

# Filtrar erros
sudo journalctl -u sihcp-server | grep ERROR
```

### Swagger UI

Abrir no navegador:
```
http://localhost:8081/swagger-ui.html
```

---

## 📈 Performance

### Otimizações Implementadas

- ✅ **Cache Caffeine** para consultas frequentes
- ✅ **Connection Pool HikariCP** otimizado
- ✅ **Compressão HTTP** habilitada
- ✅ **G1GC** para garbage collection eficiente
- ✅ **Batch Sync** para múltiplas coletas
- ✅ **Lazy Loading** em relacionamentos JPA

### Configurações Recomendadas

```bash
# Produção - Alta carga
java -Xms1g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=logs/heapdump.hprof \
  -jar sihcp-server-2.7.0.jar

# Desenvolvimento
java -Xms256m -Xmx1g \
  -jar sihcp-server-2.7.0.jar
```

---

## 🔧 Troubleshooting

### Problema: Porta já em uso

```bash
# Verificar o que está usando a porta
sudo lsof -i :8081

# Usar outra porta
java -jar sihcp-server-2.7.0.jar --server.port=8082
```

### Problema: Erro de conexão com banco

```bash
# Verificar PostgreSQL
sudo systemctl status postgresql

# Testar conexão
psql -h localhost -U inventario -d sispatrimonio

# Verificar configuração
cat ~/configuracao_banco.json
```

### Problema: OutOfMemoryError

```bash
# Aumentar heap
java -Xms1g -Xmx4g -jar sihcp-server-2.7.0.jar

# Analisar heap dump
jmap -dump:live,format=b,file=heap.bin <PID>
```

---

## 📋 Resumo

### O que mudou

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Módulo** | Monolítico | `sihcp-server` independente |
| **Pacote** | `com.inventario` | `com.inventario.sihcp` |
| **Dependências** | Desktop + Server | Apenas Server + Core |
| **Tamanho** | ~50 MB | ~40 MB |
| **Build** | Único | Independente |
| **Deploy** | Único | Independente |

### Benefícios

- ✅ **Menor tamanho** (~20% redução)
- ✅ **Sem dependências desnecessárias** (Swing, JFreeChart, POI)
- ✅ **Build mais rápido** (apenas server)
- ✅ **Deploy independente** (não afeta desktop)
- ✅ **Manutenção facilitada** (código isolado)
- ✅ **Melhor performance** (menos classes carregadas)

---

**Versão:** 2.7.0  
**Status:** ✅ Produção Ready  
**Última atualização:** 01/05/2026
