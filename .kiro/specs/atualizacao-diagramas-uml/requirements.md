# Requirements Document - Atualização de Diagramas UML

## Introduction

O Sistema de Inventário passou por diversas evoluções desde a última atualização dos diagramas UML (Janeiro 2025). Novas entidades, services, controllers e funcionalidades foram adicionadas, mas não estão refletidas na documentação UML atual. Esta atualização visa sincronizar os diagramas com o estado real do código, garantindo que a documentação arquitetural esteja precisa e atualizada.

## Glossary

- **Sistema de Inventário**: Aplicação completa para gestão de patrimônio institucional do IFMT
- **PlantUML**: Ferramenta de geração de diagramas UML a partir de código texto
- **Entidade**: Classe do modelo de domínio que representa dados persistidos
- **Service**: Classe da camada de negócio que implementa lógica de aplicação
- **Controller**: Classe da camada de apresentação que expõe endpoints REST
- **DAO**: Data Access Object - classe responsável por acesso a dados
- **Mobile API**: Backend REST que serve o aplicativo Android
- **Desktop Application**: Aplicação Swing para gestão administrativa

## Requirements

### Requirement 1: Atualizar Diagrama de Entidades

**User Story:** Como arquiteto de software, quero que o diagrama de entidades reflita todas as entidades atuais do sistema, para que eu possa entender corretamente o modelo de dados.

#### Acceptance Criteria

1. WHEN o diagrama de entidades é visualizado, THE Sistema SHALL incluir as entidades ParticipanteInventario, SalaInventario, InventarioSetor e MobileConnection que foram adicionadas ao sistema
2. WHEN o diagrama de entidades é visualizado, THE Sistema SHALL mostrar os atributos corretos e atualizados de cada entidade conforme implementado no código
3. WHEN o diagrama de entidades é visualizado, THE Sistema SHALL representar todos os relacionamentos entre entidades incluindo os novos relacionamentos de inventário
4. WHEN o diagrama de entidades é visualizado, THE Sistema SHALL incluir a entidade Coletor com seus atributos e relacionamentos corretos
5. WHERE a entidade possui atributos novos, THE Sistema SHALL adicionar esses atributos ao diagrama com seus tipos corretos

### Requirement 2: Atualizar Diagrama de Controllers e Services

**User Story:** Como desenvolvedor, quero que o diagrama de controllers e services mostre todos os componentes atuais da aplicação, para que eu possa entender a arquitetura de software implementada.

#### Acceptance Criteria

1. WHEN o diagrama de controllers é visualizado, THE Sistema SHALL incluir todos os Mobile Controllers implementados (MobileAuthController, MobileColetaController, MobileConnectionController, MobileDashboardController, MobileDescricaoController, MobileHealthController, MobilePatrimonioController, MobileResponsavelController, MobileSalaController, MobileSetorController, MobileSyncController, MobileTestController, MobileUsuarioController)
2. WHEN o diagrama de services é visualizado, THE Sistema SHALL incluir todos os services implementados incluindo DashboardService, DataSyncService, DataSyncScheduler, DescricaoResumoService, MobileConnectionService, MobileServerManager, ParticipanteInventarioService, SalaInventarioService, ConnectedDevicesManager
3. WHEN o diagrama de services é visualizado, THE Sistema SHALL incluir todos os Mobile Services (MobileAuthService, MobileColetaService, MobileDashboardService, MobilePatrimonioService, MobileResponsavelService, MobileSalaService, MobileSyncService)
4. WHEN o diagrama de DAOs é visualizado, THE Sistema SHALL incluir todos os DAOs refatorados e novos (BaseDAO, ColetaDAORefactored, DashboardColetaDAO, InventarioDAORefactored, InventarioSetorDAO, ParticipanteInventarioDAO, PatrimonioDAORefactored, RelatorioColetaDAO, ResponsavelDAORefactored, SalaDAORefactored, SalaInventarioDAO, SetorDAORefactored, UsuarioDAORefactored)
5. WHEN o diagrama é visualizado, THE Sistema SHALL mostrar os relacionamentos corretos entre Controllers, Services e DAOs

### Requirement 3: Atualizar Diagrama de Casos de Uso

**User Story:** Como analista de negócios, quero que o diagrama de casos de uso reflita todas as funcionalidades atuais do sistema, para que eu possa documentar corretamente os requisitos funcionais.

#### Acceptance Criteria

1. WHEN o diagrama de casos de uso é visualizado, THE Sistema SHALL incluir casos de uso para gestão de participantes de inventário
2. WHEN o diagrama de casos de uso é visualizado, THE Sistema SHALL incluir casos de uso para monitoramento de conexões mobile
3. WHEN o diagrama de casos de uso é visualizado, THE Sistema SHALL incluir casos de uso para sincronização de dados mobile
4. WHEN o diagrama de casos de uso é visualizado, THE Sistema SHALL incluir casos de uso para dashboard de coleta
5. WHEN o diagrama de casos de uso é visualizado, THE Sistema SHALL incluir casos de uso para geração automática de descrição resumida
6. WHERE funcionalidades de IA foram removidas ou não implementadas, THE Sistema SHALL remover esses casos de uso do diagrama

### Requirement 4: Atualizar Diagrama de Sequência

**User Story:** Como desenvolvedor, quero que os diagramas de sequência mostrem os fluxos reais implementados no sistema, para que eu possa entender as interações entre componentes.

#### Acceptance Criteria

1. WHEN o diagrama de sequência é visualizado, THE Sistema SHALL incluir fluxo de autenticação mobile com JWT
2. WHEN o diagrama de sequência é visualizado, THE Sistema SHALL incluir fluxo de coleta mobile com sincronização
3. WHEN o diagrama de sequência é visualizado, THE Sistema SHALL incluir fluxo de dashboard com estatísticas em tempo real
4. WHEN o diagrama de sequência é visualizado, THE Sistema SHALL incluir fluxo de geração de descrição resumida
5. WHERE fluxos de IA não foram implementados, THE Sistema SHALL remover esses diagramas de sequência

### Requirement 5: Atualizar Diagrama de Componentes

**User Story:** Como arquiteto de software, quero que o diagrama de componentes reflita a arquitetura real do sistema, para que eu possa documentar a estrutura de alto nível.

#### Acceptance Criteria

1. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL mostrar separação clara entre Desktop Application e Mobile API
2. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir componentes de sincronização de dados
3. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir componentes de monitoramento de conexões
4. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir componentes de dashboard e analytics
5. WHERE componentes de IA não foram implementados, THE Sistema SHALL remover esses componentes do diagrama

### Requirement 6: Atualizar README da Documentação UML

**User Story:** Como usuário da documentação, quero que o README reflita os diagramas atualizados, para que eu possa entender o que cada diagrama representa.

#### Acceptance Criteria

1. WHEN o README é lido, THE Sistema SHALL descrever corretamente todas as entidades presentes nos diagramas atualizados
2. WHEN o README é lido, THE Sistema SHALL descrever corretamente todos os controllers e services presentes nos diagramas atualizados
3. WHEN o README é lido, THE Sistema SHALL descrever corretamente todos os casos de uso presentes nos diagramas atualizados
4. WHEN o README é lido, THE Sistema SHALL descrever corretamente todos os fluxos de sequência presentes nos diagramas atualizados
5. WHEN o README é lido, THE Sistema SHALL atualizar a data de última atualização e versão dos diagramas

### Requirement 7: Remover Componentes Não Implementados

**User Story:** Como mantenedor da documentação, quero que os diagramas mostrem apenas componentes realmente implementados, para evitar confusão sobre o que existe no sistema.

#### Acceptance Criteria

1. WHEN os diagramas são visualizados, THE Sistema SHALL remover referências a AIEnhancedChatbotController que não existe no código
2. WHEN os diagramas são visualizados, THE Sistema SHALL remover referências a LegalDocumentController que não existe no código
3. WHEN os diagramas são visualizados, THE Sistema SHALL remover referências a AIIntegrationController que não existe no código
4. WHEN os diagramas são visualizados, THE Sistema SHALL remover referências a AIPerformanceController que não existe no código
5. WHEN os diagramas são visualizados, THE Sistema SHALL remover referências a todos os AI Services que não foram implementados (PatrimonioCategorizationService, PredictiveAnalysisService, LegalDocumentProcessor, LegalKnowledgeIntegrator, BusinessAnalyticsEngine, etc.)

### Requirement 8: Adicionar Componentes de Infraestrutura

**User Story:** Como desenvolvedor de infraestrutura, quero que os diagramas mostrem componentes de configuração e utilitários, para que eu possa entender a infraestrutura do sistema.

#### Acceptance Criteria

1. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir componentes de configuração (DatabaseConfig, SecurityConfig, WebSocketConfig, NotificationConfig)
2. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir componentes de segurança (JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService)
3. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir componentes offline (OfflineManager, DataSynchronizer, ConnectivityManager, SQLiteConnection)
4. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL incluir utilitários principais (ExcelExporter, RelatorioPDFGenerator, QRCodeService, ValidationUtils)
5. WHEN o diagrama de componentes é visualizado, THE Sistema SHALL mostrar as dependências entre componentes de infraestrutura e componentes de negócio
