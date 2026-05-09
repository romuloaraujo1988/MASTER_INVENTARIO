# Requirements Document

## Introduction

O SIHCP (Sistema de Histórico e Coleta Patrimonial) é atualmente um projeto Maven monolítico onde a aplicação Desktop (Java Swing) e o Servidor Mobile (Spring Boot REST API) compartilham o mesmo JAR e as mesmas classes. Isso causa acoplamento indesejado: o servidor carrega classes Swing que não utiliza, o desktop carrega controllers REST e filtros JWT que não precisa, e classes como `DatabaseConfigManager` têm problemas de ciclo de vida entre contextos Spring e não-Spring.

Este documento define os requisitos para separar o projeto em uma arquitetura multi-módulo Maven com três módulos: `sihcp-core` (código compartilhado), `sihcp-desktop` (aplicação Swing) e `sihcp-server` (API REST Spring Boot), permitindo builds e deploys independentes.

## Glossary

- **Build_System**: O sistema de build Maven responsável por compilar, empacotar e gerenciar dependências dos módulos do projeto.
- **Parent_POM**: O arquivo `pom.xml` raiz do projeto multi-módulo Maven que declara os módulos filhos e centraliza configurações comuns (versões de dependências, plugins, propriedades).
- **Core_Module**: O módulo Maven `sihcp-core` que contém modelos de domínio (JPA entities), DAOs, serviços compartilhados, utilitários comuns e configuração de banco de dados.
- **Desktop_Module**: O módulo Maven `sihcp-desktop` que contém a aplicação Java Swing, incluindo views, controllers de UI, utilitários gráficos e o entry point `SistemaInventarioApplication`.
- **Server_Module**: O módulo Maven `sihcp-server` que contém a API REST Spring Boot, incluindo controllers REST, DTOs mobile, segurança JWT, configuração Spring e o entry point `MobileApiApplication`.
- **Artefato**: O arquivo JAR gerado pela compilação de cada módulo Maven.
- **Dependência_Transitiva**: Uma dependência que é incluída indiretamente por ser dependência de outra dependência declarada.
- **Component_Scan**: O mecanismo do Spring Boot que detecta automaticamente beans e componentes anotados dentro de pacotes especificados.

## Requirements

### Requirement 1: Estrutura Multi-Módulo Maven

**User Story:** Como desenvolvedor, eu quero que o projeto seja organizado em módulos Maven independentes, para que cada componente possa ser compilado, testado e empacotado separadamente.

#### Acceptance Criteria

1. THE Build_System SHALL definir um Parent_POM na raiz do projeto que declare três módulos filhos: `sihcp-core`, `sihcp-desktop` e `sihcp-server`.
2. THE Parent_POM SHALL centralizar as versões de todas as dependências compartilhadas na seção `dependencyManagement`.
3. THE Parent_POM SHALL centralizar as configurações de plugins compartilhados na seção `pluginManagement`.
4. THE Parent_POM SHALL definir as propriedades comuns do projeto incluindo versão do Java (17), encoding (UTF-8) e versão do Spring Boot (3.2.0).
5. WHEN o comando `mvn clean package` for executado na raiz do projeto, THE Build_System SHALL compilar e empacotar os três módulos na ordem correta de dependência: core, desktop, server.

### Requirement 2: Módulo Core — Código Compartilhado

**User Story:** Como desenvolvedor, eu quero que todo o código compartilhado entre desktop e servidor esteja isolado em um módulo core, para que ambos os módulos dependam de uma única fonte de verdade para modelos, DAOs e serviços.

#### Acceptance Criteria

1. THE Core_Module SHALL conter todas as classes do pacote `com.inventario.model` (entidades JPA de domínio).
2. THE Core_Module SHALL conter todas as classes do pacote `com.inventario.dao` (Data Access Objects).
3. THE Core_Module SHALL conter as classes de serviço do pacote `com.inventario.service` que são utilizadas tanto pelo desktop quanto pelo servidor.
4. THE Core_Module SHALL conter as classes de configuração de banco de dados compartilhadas: `DatabaseConfigManager`, `DatabaseConfig`, `DatabaseConfiguration`, `HikariConnectionPool` e `DatabaseConnection`.
5. THE Core_Module SHALL conter as classes utilitárias compartilhadas do pacote `com.inventario.util` que não dependem de Swing nem de Spring Web.
6. THE Core_Module SHALL declarar como dependências apenas bibliotecas necessárias para a camada de dados e lógica de negócio: Spring Data JPA, PostgreSQL driver, Jackson, Apache Commons e SLF4J.
7. THE Core_Module SHALL gerar um Artefato JAR do tipo biblioteca (sem main class) que possa ser consumido como dependência pelos outros módulos.
8. THE Core_Module SHALL manter o pacote base `com.inventario` para preservar compatibilidade com as anotações JPA `@Entity` e `@Table` existentes.

### Requirement 3: Módulo Desktop — Aplicação Swing

**User Story:** Como desenvolvedor, eu quero que a aplicação desktop Swing seja isolada em seu próprio módulo, para que o JAR do desktop não contenha classes de API REST, segurança JWT ou controllers Spring Web.

#### Acceptance Criteria

1. THE Desktop_Module SHALL conter todas as classes do pacote `com.inventario.view` (frames e dialogs Swing).
2. THE Desktop_Module SHALL conter todas as classes do pacote `com.inventario.ui` (controllers de UI desktop).
3. THE Desktop_Module SHALL conter as classes utilitárias específicas do desktop que dependem de Swing: `IconManager`, `ImageUtils`, `DialogUtils`, `FormUtils`, `SoundNotification`, `SingleInstanceManager`.
4. THE Desktop_Module SHALL conter as classes dos pacotes `com.inventario.chart`, `com.inventario.print`, `com.inventario.offline`, `com.inventario.analytics` e `com.inventario.presentation` que são exclusivas do desktop.
5. THE Desktop_Module SHALL declarar o Core_Module como dependência.
6. THE Desktop_Module SHALL declarar como dependências adicionais apenas bibliotecas específicas do desktop: JFreeChart, Apache POI, iText PDF, ZXing e SQLite JDBC.
7. THE Desktop_Module SHALL gerar um Artefato JAR executável com `com.inventario.SistemaInventarioApplication` como main class.
8. THE Desktop_Module SHALL excluir do classpath todas as dependências de Spring Web, Spring Security, JWT e Swagger que são exclusivas do servidor.

### Requirement 4: Módulo Server — API REST Spring Boot

**User Story:** Como desenvolvedor, eu quero que o servidor da API mobile seja isolado em seu próprio módulo, para que o JAR do servidor não contenha classes Swing, gráficos ou utilitários de interface gráfica.

#### Acceptance Criteria

1. THE Server_Module SHALL conter todas as classes do pacote `com.inventario.mobile.server` (controllers, DTOs, services, config e security mobile).
2. THE Server_Module SHALL conter as classes de segurança do pacote `com.inventario.security` (JWT, filtros de autenticação, anotações de autorização).
3. THE Server_Module SHALL conter as classes de configuração específicas do Spring: `SecurityConfig`, `CacheConfig`, `AsyncConfig`, `WebSocketConfig`, `SchedulingConfig`.
4. THE Server_Module SHALL declarar o Core_Module como dependência.
5. THE Server_Module SHALL declarar como dependências adicionais apenas bibliotecas específicas do servidor: Spring Boot Starter Web, Spring Boot Starter Security, Spring Boot Starter WebSocket, JWT (jjwt), Springdoc OpenAPI, Micrometer e Caffeine Cache.
6. THE Server_Module SHALL gerar um Artefato JAR executável Spring Boot (fat JAR) com `com.inventario.MobileApiApplication` como main class.
7. THE Server_Module SHALL excluir do classpath todas as dependências de Swing, JFreeChart, Apache POI, iText PDF e SQLite JDBC que são exclusivas do desktop.
8. WHEN o Server_Module for iniciado, THE Component_Scan SHALL detectar beans apenas dos pacotes `com.inventario.mobile.server`, `com.inventario.service`, `com.inventario.dao`, `com.inventario.security` e `com.inventario.config` (classes compartilhadas do core).

### Requirement 5: Isolamento de Dependências entre Módulos

**User Story:** Como desenvolvedor, eu quero que cada módulo declare apenas as dependências que realmente utiliza, para que o tamanho dos artefatos seja reduzido e não haja conflitos de classpath.

#### Acceptance Criteria

1. THE Desktop_Module SHALL compilar e executar sem que as classes do pacote `com.inventario.mobile.server` estejam no classpath.
2. THE Server_Module SHALL compilar e executar sem que as classes dos pacotes `com.inventario.view`, `com.inventario.ui`, `com.inventario.chart`, `com.inventario.print` e `com.inventario.offline` estejam no classpath.
3. THE Core_Module SHALL compilar sem dependências de Spring Web (`spring-boot-starter-web`), Spring Security (`spring-boot-starter-security`) ou bibliotecas de UI (Swing, JFreeChart).
4. IF uma classe do Core_Module referenciar uma classe que pertence exclusivamente ao Desktop_Module ou ao Server_Module, THEN THE Build_System SHALL reportar um erro de compilação.
5. WHEN o comando `mvn dependency:tree` for executado em cada módulo, THE Build_System SHALL listar apenas as dependências declaradas para aquele módulo e suas Dependências_Transitivas legítimas.

### Requirement 6: Compatibilidade com Builds e Scripts Existentes

**User Story:** Como operador do sistema, eu quero que os scripts de build e deploy existentes continuem funcionando após a modularização, para que o processo de implantação não seja interrompido.

#### Acceptance Criteria

1. WHEN o comando `mvn clean package -DskipTests` for executado na raiz do projeto, THE Build_System SHALL gerar os artefatos de todos os três módulos no diretório `target` de cada módulo.
2. THE Server_Module SHALL manter compatibilidade com o profile Maven `mobile` existente para gerar o pacote de deploy do servidor.
3. THE Desktop_Module SHALL manter compatibilidade com o profile Maven `thin-jar` existente para gerar o JAR desktop com dependências externas na pasta `lib/`.
4. THE Server_Module SHALL manter a estrutura de saída `target/mobile-server/` com scripts de inicialização (`start-mobile-server.bat`, `start-mobile-server.sh`) e arquivo README.
5. WHEN o servidor for iniciado com `java -jar sihcp-server.jar --spring.profiles.active=mobile`, THE Server_Module SHALL carregar as configurações do profile mobile corretamente.

### Requirement 7: Preservação de Configuração de Banco de Dados

**User Story:** Como administrador do sistema, eu quero que a configuração de banco de dados continue funcionando da mesma forma após a modularização, para que não seja necessário reconfigurar ambientes existentes.

#### Acceptance Criteria

1. THE Core_Module SHALL conter a classe `DatabaseConfigManager` que lê a configuração de banco do arquivo `configuracao_banco.json` no diretório home do usuário.
2. THE Core_Module SHALL conter a classe `HikariConnectionPool` que gerencia o pool de conexões JDBC compartilhado.
3. WHEN o Desktop_Module iniciar, THE Core_Module SHALL fornecer conexões de banco de dados via `DatabaseConnection` sem depender do contexto Spring.
4. WHEN o Server_Module iniciar, THE Core_Module SHALL fornecer a configuração de DataSource via beans Spring (`MobileDataSourceConfig`, `DatabaseConfiguration`) que reutilizam a mesma configuração de `DatabaseConfigManager`.
5. THE Core_Module SHALL garantir que `DatabaseConfigManager` funcione tanto em contexto Spring (via `@Autowired`) quanto fora dele (via instanciação direta), resolvendo o problema atual de ciclo de vida.

### Requirement 8: Preservação de Funcionalidades e Contratos de API

**User Story:** Como usuário do sistema, eu quero que todas as funcionalidades existentes continuem operando normalmente após a modularização, para que a separação em módulos seja transparente.

#### Acceptance Criteria

1. THE Desktop_Module SHALL manter todas as telas Swing funcionais: login, dashboard, cadastros (patrimônio, sala, setor, responsável, usuário), coleta, relatórios, importação e configuração.
2. THE Server_Module SHALL manter todos os endpoints REST existentes com as mesmas URLs, métodos HTTP, request/response bodies e códigos de status.
3. THE Server_Module SHALL manter as anotações de segurança por roles (`@RequireAdmin`, `@RequireSupervisor`, `@RequireColetor`, `@RequireConsulta`) funcionando nos mesmos endpoints.
4. THE Server_Module SHALL manter o endpoint de health check em `/api/mobile/health` respondendo com status 200.
5. WHEN o aplicativo Android fizer requisições para a API, THE Server_Module SHALL responder de forma idêntica ao servidor monolítico atual, sem necessidade de alterações no app Android.
