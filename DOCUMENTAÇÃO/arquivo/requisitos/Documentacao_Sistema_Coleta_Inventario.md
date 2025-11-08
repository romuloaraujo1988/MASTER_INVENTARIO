# Documentação do Sistema de Coleta de Inventário

## 1. Visão Geral do Sistema

### 1.1 Objetivo
O Sistema de Coleta de Inventário é uma aplicação desktop desenvolvida em Java com Spring Boot, destinada ao gerenciamento e coleta de dados patrimoniais. O sistema permite o controle eficiente de bens, responsáveis, localizações e processos de inventário.

### 1.2 Tecnologias Utilizadas
- **Linguagem**: Java 17+
- **Framework**: Spring Boot 3.x
- **Banco de Dados**: PostgreSQL
- **Interface**: JavaFX ou Swing
- **Build Tool**: Maven
- **ORM**: Spring Data JPA / Hibernate

## 2. Configuração do Ambiente

### 2.1 Banco de Dados PostgreSQL

**Configurações de Conexão**:
- **Nome do Banco**: `sispatrimonio`
- **Usuário**: `postgres` (ou usuário específico)
- **Senha**: `Romulo@2020`
- **Host**: `localhost`
- **Porta**: `5432`

**Script de Criação do Banco**:
```sql
CREATE DATABASE sispatrimonio
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Portuguese_Brazil.1252'
    LC_CTYPE = 'Portuguese_Brazil.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
```

### 2.2 Estrutura do Projeto Maven

```
sispatrimonio-coleta/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/
│   │   │       └── gov/
│   │   │           └── ifmt/
│   │   │               └── sispatrimonio/
│   │   │                   ├── SispatrimonioApplication.java
│   │   │                   ├── config/
│   │   │                   │   ├── DatabaseConfig.java
│   │   │                   │   └── JavaFXConfig.java
│   │   │                   ├── controller/
│   │   │                   │   ├── PatrimonioController.java
│   │   │                   │   ├── ColetaController.java
│   │   │                   │   └── InventarioController.java
│   │   │                   ├── model/
│   │   │                   │   ├── entity/
│   │   │                   │   ├── dto/
│   │   │                   │   └── enums/
│   │   │                   ├── repository/
│   │   │                   ├── service/
│   │   │                   ├── view/
│   │   │                   │   ├── fxml/
│   │   │                   │   └── controller/
│   │   │                   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── fxml/
│   │       ├── css/
│   │       └── images/
│   └── test/
├── pom.xml
└── README.md
```

## 3. Configuração do Spring Boot

### 3.1 Arquivo pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>br.gov.ifmt</groupId>
    <artifactId>sispatrimonio-coleta</artifactId>
    <version>1.0.0</version>
    <name>Sistema de Coleta de Inventário</name>
    <description>Sistema desktop para coleta de dados patrimoniais</description>
    
    <properties>
        <java.version>17</java.version>
        <javafx.version>21</javafx.version>
        <javafx.maven.plugin.version>0.0.8</javafx.maven.plugin.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>${javafx.maven.plugin.version}</version>
                <configuration>
                    <mainClass>br.gov.ifmt.sispatrimonio.SispatrimonioApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 Arquivo application.yml

```yaml
spring:
  application:
    name: sispatrimonio-coleta
  
  datasource:
    url: jdbc:postgresql://localhost:5432/sispatrimonio
    username: postgres
    password: Romulo@2020
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    
  sql:
    init:
      mode: always
      
logging:
  level:
    br.gov.ifmt.sispatrimonio: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    
app:
  config:
    backup:
      enabled: true
      interval: 24h
      path: ./backup/
    
    sync:
      enabled: true
      interval: 1h
      
    ui:
      theme: default
      language: pt_BR
```

## 4. Modelo de Dados (Entidades JPA)

### 4.1 Entidade Patrimonio

```java
@Entity
@Table(name = "patrimonio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patrimonio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", unique = true, nullable = false)
    private String numero;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusPatrimonio status;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "rotulos")
    private String rotulos;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsavel")
    private Responsavel responsavel;
    
    @Column(name = "valor_aquisicao", precision = 15, scale = 2)
    private BigDecimal valorAquisicao;
    
    @Column(name = "valor_depreciado", precision = 15, scale = 2)
    private BigDecimal valorDepreciado;
    
    @Column(name = "numero_nota_fiscal")
    private String numeroNotaFiscal;
    
    @Column(name = "numero_serie")
    private String numeroSerie;
    
    @Column(name = "data_entrada")
    private LocalDate dataEntrada;
    
    @Column(name = "data_carga")
    private LocalDateTime dataCarga;
    
    @Column(name = "fornecedor")
    private String fornecedor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sala")
    private Sala sala;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conservacao")
    private EstadoConservacao estadoConservacao;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 4.2 Entidade Coleta

```java
@Entity
@Table(name = "coleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coleta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inventario", nullable = false)
    private Inventario inventario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_patrimonio", nullable = false)
    private Patrimonio patrimonio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coletor", nullable = false)
    private Coletor coletor;
    
    @Column(name = "data_coleta", nullable = false)
    private LocalDateTime dataColeta;
    
    @Column(name = "status_encontrado", nullable = false)
    private Boolean statusEncontrado;
    
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
    
    @Column(name = "foto_path")
    private String fotoPath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_coleta")
    private StatusColeta statusColeta;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

## 5. Funcionalidades do Sistema

### 5.1 Módulo de Coleta

#### 5.1.1 Funcionalidades Principais
- **Busca de Patrimônio**: Por número, código de barras ou QR Code
- **Registro de Coleta**: Confirmação de localização e estado
- **Captura de Fotos**: Documentação visual do bem
- **Sincronização**: Upload/download de dados com servidor central
- **Relatórios**: Geração de relatórios de coleta

#### 5.1.2 Fluxo de Coleta
1. **Login do Coletor**
2. **Seleção do Inventário Ativo**
3. **Busca do Patrimônio**
4. **Verificação dos Dados**
5. **Registro da Coleta**
6. **Captura de Foto (opcional)**
7. **Sincronização**

### 5.2 Interface do Usuário

#### 5.2.1 Tela Principal
- Menu de navegação
- Status de conexão
- Contador de itens coletados
- Botão de sincronização

#### 5.2.2 Tela de Coleta
- Campo de busca (número/código)
- Informações do patrimônio
- Botões de ação (Encontrado/Não Encontrado)
- Campo de observações
- Botão de captura de foto

#### 5.2.3 Tela de Configurações
- Configurações de conexão
- Configurações de sincronização
- Configurações de backup
- Informações do sistema

## 6. Arquitetura do Sistema

### 6.1 Padrão MVC
- **Model**: Entidades JPA e DTOs
- **View**: Interfaces JavaFX (FXML)
- **Controller**: Controladores Spring e JavaFX

### 6.2 Camadas da Aplicação

#### 6.2.1 Camada de Apresentação (View)
- Interfaces JavaFX
- Controladores de tela
- Validações de entrada

#### 6.2.2 Camada de Negócio (Service)
- Regras de negócio
- Validações complexas
- Orquestração de operações

#### 6.2.3 Camada de Persistência (Repository)
- Acesso a dados
- Consultas customizadas
- Transações

### 6.3 Componentes Principais

#### 6.3.1 ColetaService
```java
@Service
@Transactional
public class ColetaService {
    
    public ColetaDTO registrarColeta(ColetaRequestDTO request) {
        // Validações
        // Busca do patrimônio
        // Registro da coleta
        // Atualização de status
    }
    
    public List<ColetaDTO> buscarColetasPorInventario(Long inventarioId) {
        // Busca com paginação
    }
    
    public void sincronizarColetas() {
        // Sincronização com servidor
    }
}
```

#### 6.3.2 PatrimonioService
```java
@Service
@Transactional(readOnly = true)
public class PatrimonioService {
    
    public PatrimonioDTO buscarPorNumero(String numero) {
        // Busca por número de tombamento
    }
    
    public PatrimonioDTO buscarPorCodigoBarras(String codigo) {
        // Busca por código de barras
    }
    
    public List<PatrimonioDTO> buscarPorFiltros(PatrimonioFiltroDTO filtro) {
        // Busca com filtros múltiplos
    }
}
```

## 7. Configurações de Segurança

### 7.1 Autenticação
- Login local com validação no banco
- Controle de sessão
- Timeout automático

### 7.2 Autorização
- Perfis de usuário (Coletor, Supervisor, Admin)
- Controle de acesso por funcionalidade
- Auditoria de ações

### 7.3 Proteção de Dados
- Criptografia de senhas (BCrypt)
- Validação de entrada
- Sanitização de dados

## 8. Configurações de Deploy

### 8.1 Empacotamento
- JAR executável com dependências
- Instalador para Windows (.msi)
- Script de inicialização

### 8.2 Requisitos do Sistema
- **SO**: Windows 10+ / Linux / macOS
- **Java**: JRE 17+
- **RAM**: 512MB mínimo, 1GB recomendado
- **Disco**: 200MB para aplicação + espaço para dados
- **Rede**: Conexão para sincronização

### 8.3 Configuração de Produção
```yaml
spring:
  profiles:
    active: production
    
  datasource:
    url: jdbc:postgresql://servidor-producao:5432/sispatrimonio
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

## 9. Testes

### 9.1 Testes Unitários
- Testes de serviços
- Testes de repositórios
- Testes de validações

### 9.2 Testes de Integração
- Testes de banco de dados
- Testes de sincronização
- Testes de interface

### 9.3 Testes de Performance
- Testes de carga
- Testes de sincronização
- Testes de memória

## 10. Monitoramento e Logs

### 10.1 Configuração de Logs
```yaml
logging:
  level:
    root: INFO
    br.gov.ifmt.sispatrimonio: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/sispatrimonio.log
    max-size: 10MB
    max-history: 30
```

### 10.2 Métricas
- Número de coletas por dia
- Tempo médio de coleta
- Taxa de sincronização
- Erros e exceções

## 11. Backup e Recuperação

### 11.1 Backup Automático
- Backup diário do banco local
- Sincronização com servidor central
- Compressão e criptografia

### 11.2 Recuperação
- Restauração de backup local
- Re-sincronização com servidor
- Recuperação de dados corrompidos

## 12. Roadmap de Desenvolvimento

### 12.1 Fase 1 - MVP (4 semanas)
- Configuração do projeto
- Entidades básicas
- Interface de coleta simples
- Funcionalidades core

### 12.2 Fase 2 - Melhorias (3 semanas)
- Interface aprimorada
- Sincronização
- Relatórios básicos
- Testes

### 12.3 Fase 3 - Avançado (3 semanas)
- Captura de fotos
- Geolocalização
- Relatórios avançados
- Deploy e documentação

---

**Versão**: 1.0  
**Data**: $(Get-Date -Format "dd/MM/yyyy")  
**Responsável**: Equipe de Desenvolvimento IFMT