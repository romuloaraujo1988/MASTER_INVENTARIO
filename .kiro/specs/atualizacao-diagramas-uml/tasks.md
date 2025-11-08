# Implementation Plan - Atualização de Diagramas UML

- [x] 1. Atualizar Diagrama de Entidades



  - Ler o arquivo atual diagrama-entidades.puml para entender a estrutura
  - Adicionar as novas entidades: ParticipanteInventario, SalaInventario, InventarioSetor, MobileConnection com todos os atributos e métodos
  - Atualizar atributos das entidades existentes que foram modificados no código
  - Adicionar os novos relacionamentos entre entidades
  - Validar sintaxe PlantUML gerando preview ou testando online
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 2. Reescrever Diagrama de Controllers e Services
  - Ler o arquivo atual diagrama-controllers-services.puml
  - Remover todos os controllers de IA não implementados (AIEnhancedChatbotController, LegalDocumentController, AIIntegrationController, AIPerformanceController)
  - Remover todos os AI Services não implementados (ChatbotService, AIIntegrationService, AIDataAnalysisService, PatrimonioCategorizationService, PredictiveAnalysisService, LegalDocumentProcessor, LegalKnowledgeIntegrator, BusinessAnalyticsEngine, UnifiedConfigurationManager)
  - Adicionar package "Mobile Controllers" com todos os 13 controllers mobile (MobileAuthController, MobileColetaController, MobileConnectionController, MobileDashboardController, MobileDescricaoController, MobileHealthController, MobilePatrimonioController, MobileResponsavelController, MobileSalaController, MobileSetorController, MobileSyncController, MobileTestController, MobileUsuarioController)
  - Adicionar package "Core Services" com todos os services implementados (DashboardService, DataSyncService, DataSyncScheduler, DescricaoResumoService, MobileConnectionService, MobileServerManager, ParticipanteInventarioService, SalaInventarioService, ConnectedDevicesManager, PatrimonioService, InventarioService, ColetaService, RelatorioService, ResponsavelService, SalaService, SetorService, CampusService, UsuarioService, QRCodeService, AutenticacaoService)
  - Adicionar package "Mobile Services" com os 7 mobile services (MobileAuthService, MobileColetaService, MobileDashboardService, MobilePatrimonioService, MobileResponsavelService, MobileSalaService, MobileSyncService)
  - Atualizar package "DAOs" com todos os DAOs refatorados e novos (BaseDAO, CampusDAO, ColetaDAO, ColetaDAORefactored, ColetorDAO, DashboardColetaDAO, InventarioDAO, InventarioDAORefactored, InventarioSetorDAO, ParticipanteInventarioDAO, PatrimonioDAORefactored, QRCodeDAO, RelatorioColetaDAO, ResponsavelDAORefactored, SalaDAORefactored, SalaInventarioDAO, SetorDAORefactored, UsuarioDAORefactored)
  - Adicionar relacionamentos corretos entre Controllers -> Services -> DAOs
  - Validar sintaxe PlantUML
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 3. Atualizar Diagrama de Casos de Uso
  - Ler o arquivo atual diagrama-casos-uso.puml
  - Remover todos os casos de uso de IA não implementados (UC35-UC40, UC43, UC46-UC50, UC54-UC55)
  - Remover casos de uso de Chatbot (UC41-UC45)
  - Adicionar novos casos de uso para gestão de inventário: UC56 (Gerenciar Participantes), UC57 (Associar Salas), UC58 (Associar Setores), UC59 (Monitorar por Sala), UC60 (Monitorar por Setor)
  - Adicionar novos casos de uso para coleta mobile: UC61 (Monitorar Conexões), UC62 (Visualizar Dispositivos), UC63 (Rastrear Coletas), UC64 (Gerar Descrição Resumida)
  - Adicionar novos casos de uso para dashboard: UC65 (Dashboard de Coleta), UC66 (Estatísticas Tempo Real), UC67 (Coletas por Dia), UC68 (Progresso por Coletor)
  - Atualizar relacionamentos entre atores e casos de uso
  - Validar sintaxe PlantUML
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [ ] 4. Reescrever Diagramas de Sequência
  - Ler o arquivo atual diagrama-sequencia.puml
  - Remover todos os fluxos de IA não implementados
  - Criar Fluxo 1: Autenticação Mobile com JWT mostrando interação entre Coletor -> MobileAuthController -> MobileAuthService -> UsuarioDAORefactored -> JwtTokenProvider -> MobileConnectionService
  - Criar Fluxo 2: Coleta Mobile com Sincronização mostrando interação entre Coletor -> MobileColetaController -> MobileColetaService -> PatrimonioDAORefactored -> ColetaDAORefactored -> DataSyncService -> WebSocket -> DashboardColetaFrame
  - Criar Fluxo 3: Dashboard em Tempo Real mostrando interação entre Gestor -> DashboardColetaFrame -> DashboardService -> DashboardColetaDAO -> WebSocket com atualização em tempo real
  - Criar Fluxo 4: Geração de Descrição Resumida mostrando interação entre Operador -> PatrimonioFormDialog -> DescricaoResumoService com processamento de texto
  - Validar sintaxe PlantUML
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 5. Atualizar Diagrama de Componentes
  - Ler o arquivo atual diagrama-componentes.puml
  - Remover todos os componentes de IA não implementados
  - Atualizar Camada de Apresentação separando claramente Desktop Application (Swing) e Mobile API (REST) e Android App (Kotlin)
  - Atualizar Camada de Controle com componentes de Security (JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService) e Configuration (SecurityConfig, DatabaseConfig, WebSocketConfig, NotificationConfig)
  - Atualizar Camada de Negócio com Core Services, Mobile Services e componentes de Sync & Connection (DataSyncService, MobileConnectionService, ConnectedDevicesManager)
  - Atualizar Camada de Dados com Data Access Layer (DAOs refatorados) e Offline Support (OfflineManager, DataSynchronizer, ConnectivityManager, SQLiteConnection)
  - Atualizar Camada de Persistência com PostgreSQL Database, SQLite Local Cache e File System
  - Adicionar relacionamentos e dependências entre camadas
  - Validar sintaxe PlantUML
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 6. Atualizar README da Documentação UML
  - Ler o arquivo atual README.md
  - Atualizar seção "Diagrama de Classes - Entidades" com descrição das 4 novas entidades e seus relacionamentos
  - Reescrever seção "Diagrama de Classes - Controllers e Services" removendo componentes de IA e adicionando descrição de todos os Mobile Controllers, Core Services, Mobile Services e DAOs atualizados
  - Atualizar seção "Diagrama de Casos de Uso" removendo casos de uso de IA e adicionando descrição dos novos casos de uso de gestão de inventário, coleta mobile e dashboard
  - Reescrever seção "Diagramas de Sequência" com descrição dos 4 novos fluxos implementados
  - Atualizar seção "Diagrama de Componentes" com descrição da arquitetura real em camadas
  - Atualizar data de última atualização para a data atual
  - Atualizar versão para 2.0
  - Validar que todas as referências aos arquivos .puml estão corretas
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 7. Validar e Gerar Imagens dos Diagramas
  - Testar cada arquivo .puml no PlantUML Online Server para validar sintaxe
  - Gerar imagem PNG do diagrama-entidades.puml
  - Gerar imagem PNG do diagrama-controllers-services.puml
  - Gerar imagem PNG do diagrama-casos-uso.puml
  - Gerar imagem PNG do diagrama-sequencia.puml
  - Gerar imagem PNG do diagrama-componentes.puml
  - Verificar que todas as imagens estão legíveis e bem formatadas
  - Salvar imagens com nomes descritivos no diretório docs/uml/
  - _Requirements: Todos os requirements de validação_
