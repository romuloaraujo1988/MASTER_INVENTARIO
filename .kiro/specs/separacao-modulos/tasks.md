# Implementation Plan: Separação em Módulos Maven

## Overview

Migração incremental do projeto monolítico SIHCP para uma arquitetura Maven multi-módulo com três módulos: `sihcp-core` (código compartilhado), `sihcp-desktop` (aplicação Swing) e `sihcp-server` (API REST Spring Boot). A migração segue 6 fases com checkpoints de compilação entre cada fase para garantir estabilidade.

## Tasks

- [x] 1. Fase 1 — Estrutura de diretórios e Parent POM
  - [x] 1.1 Criar estrutura de diretórios dos três módulos
    - Criar diretórios `sihcp-core/src/main/java/com/inventario/`, `sihcp-desktop/src/main/java/com/inventario/` e `sihcp-server/src/main/java/com/inventario/`
    - Criar diretórios `sihcp-core/src/main/resources/`, `sihcp-desktop/src/main/resources/` e `sihcp-server/src/main/resources/`
    - Criar diretórios `sihcp-core/src/test/java/`, `sihcp-desktop/src/test/java/` e `sihcp-server/src/test/java/`
    - _Requirements: 1.1_

  - [x] 1.2 Converter pom.xml raiz para Parent POM
    - Alterar `<packaging>jar</packaging>` para `<packaging>pom</packaging>`
    - Adicionar seção `<modules>` declarando `sihcp-core`, `sihcp-desktop`, `sihcp-server` nesta ordem
    - Mover todas as dependências diretas para `<dependencyManagement>` (manter versões centralizadas)
    - Mover configurações de plugins para `<pluginManagement>`
    - Manter herança de `spring-boot-starter-parent` versão 3.2.0
    - Manter `<properties>` com java.version=17, encoding=UTF-8 e versões de libs
    - Remover a seção `<dependencies>` direta do parent (cada módulo declara as suas)
    - Manter profiles `mobile`, `fat-jar` e `thin-jar` no parent (serão ajustados depois)
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 1.3 Criar pom.xml do módulo sihcp-core
    - Declarar parent como `com.inventario:sistema-inventario:2.7.0`
    - Definir `<artifactId>sihcp-core</artifactId>` com `<packaging>jar</packaging>`
    - Adicionar dependências: `spring-boot-starter-data-jpa`, `spring-boot-starter-log4j2`, `postgresql` (runtime), `jackson-databind`, `commons-lang3`, `commons-io`, `commons-collections4`, `javax.annotation-api`, `jakarta.validation-api`, `slf4j-api`
    - Desabilitar `spring-boot-maven-plugin` com `<skip>true</skip>` (core não é executável)
    - Adicionar dependências de teste: `spring-boot-starter-test`, `junit-quickcheck-core`, `junit-quickcheck-generators`
    - _Requirements: 2.6, 2.7_

  - [x] 1.4 Criar pom.xml do módulo sihcp-desktop
    - Declarar parent como `com.inventario:sistema-inventario:2.7.0`
    - Definir `<artifactId>sihcp-desktop</artifactId>` com `<packaging>jar</packaging>`
    - Adicionar dependência `sihcp-core` versão `${project.version}`
    - Adicionar dependências desktop: `jfreechart`, `poi`, `poi-ooxml`, `poi-ooxml-full`, `poi-scratchpad`, `itext-kernel`, `itext-layout`, `itext-io`, `zxing-core`, `zxing-javase`, `sqlite-jdbc`, `commons-compress`, `commons-math3`, `SparseBitSet`, `xmlbeans`, `curvesapi`
    - Configurar `maven-jar-plugin` com main class `com.inventario.SistemaInventarioApplication` e classpath prefix `lib/`
    - Configurar `maven-dependency-plugin` para copiar dependências para `target/lib/`
    - Desabilitar `spring-boot-maven-plugin` com `<skip>true</skip>`
    - Manter compatibilidade com profile `thin-jar`
    - _Requirements: 3.5, 3.6, 3.7, 3.8, 6.3_

  - [x] 1.5 Criar pom.xml do módulo sihcp-server
    - Declarar parent como `com.inventario:sistema-inventario:2.7.0`
    - Definir `<artifactId>sihcp-server</artifactId>` com `<packaging>jar</packaging>`
    - Adicionar dependência `sihcp-core` versão `${project.version}`
    - Adicionar dependências server: `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-websocket`, `spring-boot-starter-validation`, `spring-boot-starter-cache`, `spring-boot-starter-actuator`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`, `springdoc-openapi-starter-webmvc-ui`, `caffeine`, `micrometer-registry-prometheus`, `micrometer-core`, `spring-boot-starter-data-redis` (optional), `jakarta.servlet-api`
    - Configurar `spring-boot-maven-plugin` com main class `com.inventario.MobileApiApplication` e `<skip>false</skip>` para gerar fat JAR
    - Configurar `maven-antrun-plugin` para gerar estrutura `target/mobile-server/` com scripts de inicialização e README
    - Manter compatibilidade com profile `mobile`
    - Adicionar dependências de teste: `spring-boot-starter-test`, `spring-security-test`
    - _Requirements: 4.4, 4.5, 4.6, 4.7, 6.2, 6.4_

- [x] 2. Checkpoint Fase 1 — Verificar compilação da estrutura
  - Executar `mvn clean compile` na raiz do projeto
  - Verificar que o Maven Reactor reconhece os três módulos na ordem correta (core → desktop → server)
  - Os módulos estarão vazios neste ponto, mas a estrutura de build deve funcionar sem erros
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 1.5_

- [x] 3. Fase 2 — Mover classes do core
  - [x] 3.1 Mover pacote model para sihcp-core
    - Mover todas as classes de `src/main/java/com/inventario/model/` para `sihcp-core/src/main/java/com/inventario/model/`
    - Inclui todas as entidades JPA: Patrimonio, Usuario, Coleta, Inventario, Sala, Setor, Responsavel, etc.
    - _Requirements: 2.1, 2.8_

  - [x] 3.2 Mover pacote dao para sihcp-core
    - Mover todas as classes de `src/main/java/com/inventario/dao/` para `sihcp-core/src/main/java/com/inventario/dao/`
    - Inclui BaseDAO, PatrimonioDAO, UsuarioDAO, ColetaDAO, etc.
    - _Requirements: 2.2_

  - [x] 3.3 Mover serviços compartilhados para sihcp-core
    - Mover para `sihcp-core/src/main/java/com/inventario/service/` os serviços compartilhados: PatrimonioService, ColetaService, UsuarioService, InventarioService, SalaService, SetorService, ResponsavelService, DashboardService, ParticipanteInventarioService, SalaInventarioService, CampusService, DescricaoResumoService, AutenticacaoService (interface), AutenticacaoServiceDB, AutenticacaoServiceAPI, UnifiedAuthService, ServiceFactory, BusinessException
    - NÃO mover serviços desktop-specific (RelatorioService, MobileServerManager, etc.) nem server-specific (DataSyncService, etc.)
    - _Requirements: 2.3_

  - [x] 3.4 Mover classes de configuração de banco para sihcp-core
    - Mover para `sihcp-core/src/main/java/com/inventario/config/`: DatabaseConfigManager, DatabaseConfig, DatabaseConfiguration, HikariConnectionPool, ConfigurationPaths, CampusConfig, ConfigGenerator, ConfigFileWatcher, DAOConfiguration, SetupConfig, NotificationConfig
    - NÃO mover SecurityConfig, MobileDataSourceConfig, CacheConfig, AsyncConfig, WebSocketConfig, SchedulingConfig (são do server)
    - NÃO mover MobileServerProcessManager (é do desktop)
    - _Requirements: 2.4, 7.1, 7.2, 7.5_

  - [x] 3.5 Mover utilitários compartilhados para sihcp-core
    - Mover para `sihcp-core/src/main/java/com/inventario/util/`: DatabaseConnection, ConnectionManager, ValidationUtils, DateFormatUtils, DataSanitizer, DescricaoNormalizador, PasswordEncryption, PasswordUtil, PasswordHashGenerator, VersionInfo, Page, PortManager, ExceptionHandler, ConfiguracaoBancoUtil, ConfigurationMigration
    - NÃO mover utilitários que dependem de Swing (IconManager, ImageUtils, DialogUtils, FormUtils, SoundNotification, etc.)
    - NÃO mover utilitários que dependem de POI/iText (ExcelExporter, RelatorioPDFGenerator, etc.)
    - _Requirements: 2.5_

  - [x] 3.6 Mover pacotes dto, exception, event, repository, cache e itemcomposto para sihcp-core
    - Mover `src/main/java/com/inventario/dto/` → `sihcp-core/src/main/java/com/inventario/dto/`
    - Mover `src/main/java/com/inventario/exception/` → `sihcp-core/src/main/java/com/inventario/exception/`
    - Mover `src/main/java/com/inventario/event/` → `sihcp-core/src/main/java/com/inventario/event/`
    - Mover `src/main/java/com/inventario/repository/` → `sihcp-core/src/main/java/com/inventario/repository/`
    - Mover `src/main/java/com/inventario/cache/` → `sihcp-core/src/main/java/com/inventario/cache/`
    - Mover `src/main/java/com/inventario/itemcomposto/` → `sihcp-core/src/main/java/com/inventario/itemcomposto/`
    - _Requirements: 2.1, 2.3_

- [x] 4. Checkpoint Fase 2 — Verificar compilação do core
  - Executar `mvn clean compile -pl sihcp-core` para compilar apenas o módulo core
  - Verificar que não há imports de `javax.swing.*`, `org.springframework.web.*` ou `org.springframework.security.*` no core
  - Corrigir quaisquer erros de compilação por referências a classes que ficaram fora do core
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 2.5, 2.6, 5.3, 5.4_

- [x] 5. Fase 3 — Mover classes do desktop
  - [x] 5.1 Mover pacotes de UI desktop para sihcp-desktop
    - Mover `src/main/java/com/inventario/view/` → `sihcp-desktop/src/main/java/com/inventario/view/`
    - Mover `src/main/java/com/inventario/ui/` → `sihcp-desktop/src/main/java/com/inventario/ui/`
    - Mover `src/main/java/com/inventario/chart/` → `sihcp-desktop/src/main/java/com/inventario/chart/`
    - Mover `src/main/java/com/inventario/print/` → `sihcp-desktop/src/main/java/com/inventario/print/`
    - Mover `src/main/java/com/inventario/offline/` → `sihcp-desktop/src/main/java/com/inventario/offline/`
    - Mover `src/main/java/com/inventario/analytics/` → `sihcp-desktop/src/main/java/com/inventario/analytics/`
    - Mover `src/main/java/com/inventario/presentation/` → `sihcp-desktop/src/main/java/com/inventario/presentation/`
    - Mover `src/main/java/com/inventario/siads/` → `sihcp-desktop/src/main/java/com/inventario/siads/`
    - _Requirements: 3.1, 3.2, 3.4_

  - [x] 5.2 Mover utilitários e serviços desktop-specific para sihcp-desktop
    - Mover para `sihcp-desktop/src/main/java/com/inventario/util/`: IconManager, ImageUtils, ImageProcessor, DialogUtils, FormUtils, SoundNotification, SingleInstanceManager, SingleInstanceLock, IconPreview, QrCodeGenerator, ExcelExporter, ExcelExporterAlternativo, CsvExcelGenerator, RelatorioExcelGenerator, RelatorioPDFGenerator, HistoricoExcelGenerator, HistoricoPDFGenerator, ImportacaoCSV, ImportacaoExcel, MobileApiClient, MobileServerProcessManager (util), CadastrarItensCompostosMassa, CriarTabelasCategoriaUtil, CriarTabelasItemComposto, DatabaseExecuteUpdate, DatabaseTestQuery, ExecutarScriptSQL
    - Mover para `sihcp-desktop/src/main/java/com/inventario/service/`: MobileServerManager, MobileConnectionService, ConnectedDevicesManager, DataImportService, RelatorioService, RelatorioItemCompostoService, ExportacaoItemCompostoService, MemoryMonitorService
    - Mover para `sihcp-desktop/src/main/java/com/inventario/config/`: MobileServerProcessManager (config)
    - _Requirements: 3.3, 3.6_

  - [x] 5.3 Mover entry points desktop para sihcp-desktop
    - Mover `src/main/java/com/inventario/SistemaInventarioApplication.java` → `sihcp-desktop/src/main/java/com/inventario/SistemaInventarioApplication.java`
    - Mover `src/main/java/com/inventario/TesteSalvar.java` → `sihcp-desktop/src/main/java/com/inventario/TesteSalvar.java`
    - _Requirements: 3.7_

- [x] 6. Checkpoint Fase 3 — Verificar compilação do desktop
  - Executar `mvn clean compile -pl sihcp-core,sihcp-desktop` para compilar core e desktop
  - Verificar que o desktop compila sem erros e que todas as referências ao core são resolvidas
  - Corrigir quaisquer imports quebrados ou classes faltantes
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 3.5, 3.8, 5.1_

- [x] 7. Fase 4 — Mover classes do server
  - [x] 7.1 Mover pacote mobile/server para sihcp-server
    - Mover `src/main/java/com/inventario/mobile/` → `sihcp-server/src/main/java/com/inventario/mobile/`
    - Inclui todos os subpacotes: server/controller, server/service, server/dto, server/config, server/security, server/util
    - _Requirements: 4.1_

  - [x] 7.2 Mover pacotes security e sync para sihcp-server
    - Mover `src/main/java/com/inventario/security/` → `sihcp-server/src/main/java/com/inventario/security/`
    - Mover `src/main/java/com/inventario/sync/` → `sihcp-server/src/main/java/com/inventario/sync/`
    - Inclui JwtTokenProvider, JwtUtil, JwtAuthenticationFilter, JwtAuthenticationEntryPoint, CustomUserDetailsService, anotações de segurança (@RequireAdmin, etc.)
    - _Requirements: 4.2_

  - [x] 7.3 Mover configurações e serviços server-specific para sihcp-server
    - Mover para `sihcp-server/src/main/java/com/inventario/config/`: SecurityConfig, MobileDataSourceConfig, CacheConfig, AsyncConfig, WebSocketConfig, SchedulingConfig
    - Mover para `sihcp-server/src/main/java/com/inventario/service/`: DataSyncService, DataSyncScheduler, SyncStatusManager, DispositivoMobileService, FotoReferenciaService, HistoricoColetaService
    - _Requirements: 4.3, 4.5_

  - [x] 7.4 Mover entry point server e atualizar ComponentScan
    - Mover `src/main/java/com/inventario/MobileApiApplication.java` → `sihcp-server/src/main/java/com/inventario/MobileApiApplication.java`
    - Atualizar `@ComponentScan` para incluir `com.inventario.config` e `com.inventario.sync` nos basePackages
    - Verificar que `@EntityScan(basePackages = "com.inventario.model")` está presente
    - _Requirements: 4.6, 4.8_

- [x] 8. Checkpoint Fase 4 — Verificar compilação do server
  - Executar `mvn clean compile -pl sihcp-core,sihcp-server` para compilar core e server
  - Verificar que o server compila sem erros e que todas as referências ao core são resolvidas
  - Corrigir quaisquer imports quebrados ou classes faltantes
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 4.4, 4.7, 5.2_

- [x] 9. Fase 5 — Verificação completa e limpeza
  - [x] 9.1 Limpar diretório src/main/java original
    - Remover todos os arquivos .java que foram movidos de `src/main/java/com/inventario/`
    - Manter o diretório `src/` vazio ou remover se não houver mais conteúdo
    - Verificar que nenhuma classe ficou órfã (não movida para nenhum módulo)
    - Mover pacote `controller/` e `test/` para o módulo apropriado se existirem classes relevantes
    - _Requirements: 1.1_

  - [x] 9.2 Executar build completo do projeto multi-módulo
    - Executar `mvn clean package -DskipTests` na raiz do projeto
    - Verificar que os três módulos compilam e empacotam na ordem correta: core → desktop → server
    - Verificar que `sihcp-core/target/` contém o JAR biblioteca (sem main class)
    - Verificar que `sihcp-desktop/target/` contém o JAR executável com main class `SistemaInventarioApplication`
    - Verificar que `sihcp-server/target/` contém o fat JAR Spring Boot com main class `MobileApiApplication`
    - _Requirements: 1.5, 2.7, 3.7, 4.6, 6.1_

  - [x] 9.3 Verificar isolamento de dependências entre módulos
    - Executar `mvn dependency:tree -pl sihcp-core` e verificar ausência de spring-boot-starter-web, spring-boot-starter-security, jfreechart, poi, itext, sqlite-jdbc, jjwt
    - Executar `mvn dependency:tree -pl sihcp-desktop` e verificar ausência de spring-boot-starter-web, spring-boot-starter-security, jjwt, springdoc-openapi
    - Executar `mvn dependency:tree -pl sihcp-server` e verificar ausência de jfreechart, poi, itext, sqlite-jdbc
    - _Requirements: 5.3, 5.4, 5.5_

  - [x] 9.4 Atualizar scripts de build existentes
    - Atualizar `build-pacote-implantacao.ps1` para referenciar os novos caminhos de artefatos
    - Atualizar `build-producao-completo.ps1` para referenciar os novos caminhos de artefatos
    - Verificar que o profile `mobile` gera o pacote de deploy do server em `sihcp-server/target/mobile-server/`
    - Verificar que o profile `thin-jar` gera o JAR desktop em `sihcp-desktop/target/` com `lib/`
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 10. Checkpoint Fase 5 — Verificação completa
  - Executar `mvn clean package` na raiz (com testes)
  - Verificar que todos os testes existentes passam
  - Verificar que `sihcp-server/target/mobile-server/` contém scripts de inicialização e README
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 6.1, 6.4, 6.5, 8.2, 8.4_

- [x] 11. Fase 6 — Mover resources
  - [x] 11.1 Mover resources do server
    - Copiar `src/main/resources/application.properties` → `sihcp-server/src/main/resources/application.properties`
    - Copiar `src/main/resources/application-mobile.properties` → `sihcp-server/src/main/resources/application-mobile.properties`
    - Copiar quaisquer outros arquivos de configuração Spring (application-*.properties, application-*.yml) para `sihcp-server/src/main/resources/`
    - _Requirements: 6.5_

  - [x] 11.2 Mover resources compartilhados para sihcp-core (se existirem)
    - Verificar se existem resources compartilhados (log4j2.xml, mensagens i18n, etc.)
    - Se existirem, copiar para `sihcp-core/src/main/resources/`
    - Se houver resources específicos do desktop, copiar para `sihcp-desktop/src/main/resources/`
    - _Requirements: 2.6_

  - [x] 11.3 Limpar resources originais
    - Remover os resources que foram copiados de `src/main/resources/` após confirmar que os módulos funcionam com os resources nos novos locais
    - _Requirements: 1.1_

- [x] 12. Checkpoint Final — Build e verificação completa
  - Executar `mvn clean package` na raiz do projeto
  - Verificar que todos os três módulos compilam, empacotam e passam testes
  - Verificar que o server inicia com `java -jar sihcp-server/target/sihcp-server-2.7.0.jar --spring.profiles.active=mobile`
  - Verificar que o endpoint `/api/mobile/health` responde com status 200
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 6.5, 8.2, 8.4, 8.5_

- [ ] 13. Testes de propriedade e isolamento
  - [ ]* 13.1 Write property test for Core Module Import Isolation
    - **Property 1: Core Module Import Isolation**
    - Escanear todos os arquivos .java em `sihcp-core/src/main/java/` e verificar que nenhum contém imports de `javax.swing.*`, `org.springframework.web.*` ou `org.springframework.security.*`
    - **Validates: Requirements 2.5, 5.3**

  - [ ]* 13.2 Write property test for Desktop Dependency Isolation
    - **Property 2: Desktop Dependency Isolation**
    - Parsear output de `mvn dependency:tree -pl sihcp-desktop` e verificar ausência de `spring-boot-starter-web`, `spring-boot-starter-security`, `jjwt-*`, `springdoc-openapi-*`
    - **Validates: Requirements 3.8**

  - [ ]* 13.3 Write property test for Server Dependency Isolation
    - **Property 3: Server Dependency Isolation**
    - Parsear output de `mvn dependency:tree -pl sihcp-server` e verificar ausência de `jfreechart`, `poi-*`, `itext-*`, `sqlite-jdbc`
    - **Validates: Requirements 4.7**

  - [ ]* 13.4 Write property test for DatabaseConfigManager Configuration Round-Trip
    - **Property 4: DatabaseConfigManager Configuration Round-Trip**
    - Usar junit-quickcheck com 100 trials para gerar configurações aleatórias (host, port 1-65535, database, username, password) e verificar round-trip via save/load
    - **Validates: Requirements 7.5**

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each phase has a checkpoint task to verify compilation before proceeding to the next phase
- A migração é incremental: cada fase move um conjunto de classes e verifica compilação
- O diretório `src/main/java/` original é limpo apenas na Fase 5, após todos os módulos estarem funcionando
- Os resources são movidos por último (Fase 6) para minimizar risco
- O `DatabaseConfigManager` permanece como POJO no core — sem refatoração para bean Spring
- O `@ComponentScan` do server precisa incluir `com.inventario.config` e `com.inventario.sync`
- Property tests validate universal correctness properties from the design document
- Cada tarefa referencia os requisitos específicos para rastreabilidade
