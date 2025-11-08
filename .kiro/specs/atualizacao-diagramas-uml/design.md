# Design Document - Atualização de Diagramas UML

## Overview

Este documento descreve o design para atualização completa dos diagramas UML do Sistema de Inventário. A atualização visa sincronizar a documentação arquitetural com o estado atual do código, removendo componentes não implementados (especialmente relacionados a IA) e adicionando todos os novos componentes desenvolvidos desde a última atualização.

A abordagem será sistemática: analisar cada arquivo `.puml` existente, identificar discrepâncias com o código real, e atualizar os diagramas para refletir a arquitetura implementada.

## Architecture

### Estrutura de Documentação UML

```
docs/uml/
├── README.md                           # Documentação principal (atualizar)
├── diagrama-entidades.puml             # Modelo de dados (atualizar)
├── diagrama-controllers-services.puml  # Arquitetura de software (reescrever)
├── diagrama-casos-uso.puml             # Funcionalidades (atualizar)
├── diagrama-sequencia.puml             # Fluxos de interação (reescrever)
├── diagrama-componentes.puml           # Arquitetura de alto nível (atualizar)
└── *.png                               # Imagens geradas (regenerar)
```

### Princípios de Design

1. **Fidelidade ao Código**: Diagramas devem refletir exatamente o que está implementado
2. **Clareza**: Evitar sobrecarga de informação, focar no essencial
3. **Modularidade**: Separar claramente Desktop App e Mobile API
4. **Manutenibilidade**: Estrutura que facilite futuras atualizações
5. **Completude**: Incluir todos os componentes principais sem omissões

## Components and Interfaces

### 1. Diagrama de Entidades (diagrama-entidades.puml)

#### Entidades Existentes a Manter
- Patrimonio (atualizar atributos)
- Setor, Responsavel, Sala, Campus
- Inventario, Coleta, Coletor
- Usuario, PerfilUsuario, QRCode

#### Novas Entidades a Adicionar

**ParticipanteInventario**
```plantuml
class ParticipanteInventario {
    -Integer id
    -Integer idInventario
    -Integer idUsuario
    -String nomeUsuario
    -String papel
    -LocalDateTime dataInclusao
    -Boolean ativo
    +getId(): Integer
    +setId(Integer): void
    +getIdInventario(): Integer
    +setIdInventario(Integer): void
    +getIdUsuario(): Integer
    +setIdUsuario(Integer): void
    +getPapel(): String
    +setPapel(String): void
}
```

**SalaInventario**
```plantuml
class SalaInventario {
    -Integer id
    -Integer idInventario
    -Integer idSala
    -String nomeSala
    -Integer totalPatrimonios
    -Integer patrimoniosColetados
    -BigDecimal percentualConclusao
    -LocalDateTime dataInclusao
    +getId(): Integer
    +setId(Integer): void
    +getIdInventario(): Integer
    +setIdInventario(Integer): void
    +getIdSala(): Integer
    +setIdSala(Integer): void
    +getTotalPatrimonios(): Integer
    +setTotalPatrimonios(Integer): void
}
```

**InventarioSetor**
```plantuml
class InventarioSetor {
    -Integer id
    -Integer idInventario
    -Integer idSetor
    -String nomeSetor
    -Integer totalPatrimonios
    -Integer patrimoniosColetados
    -BigDecimal percentualConclusao
    -LocalDateTime dataInclusao
    +getId(): Integer
    +setId(Integer): void
    +getIdInventario(): Integer
    +setIdInventario(Integer): void
    +getIdSetor(): Integer
    +setIdSetor(Integer): void
}
```

**MobileConnection**
```plantuml
class MobileConnection {
    -Long id
    -String deviceId
    -String deviceName
    -String deviceModel
    -String osVersion
    -String appVersion
    -String ipAddress
    -Integer idUsuario
    -String nomeUsuario
    -LocalDateTime firstConnection
    -LocalDateTime lastConnection
    -Boolean isActive
    -Integer totalColetas
    +getId(): Long
    +setId(Long): void
    +getDeviceId(): String
    +setDeviceId(String): void
    +getIdUsuario(): Integer
    +setIdUsuario(Integer): void
}
```

#### Relacionamentos a Adicionar
- Inventario 1:N ParticipanteInventario
- Usuario 1:N ParticipanteInventario
- Inventario 1:N SalaInventario
- Sala 1:N SalaInventario
- Inventario 1:N InventarioSetor
- Setor 1:N InventarioSetor
- Usuario 1:N MobileConnection

### 2. Diagrama de Controllers e Services (diagrama-controllers-services.puml)

Este diagrama precisa ser **completamente reescrito** para refletir a arquitetura real.

#### Package: Mobile Controllers

**Controllers a Adicionar:**
- MobileAuthController
- MobileColetaController
- MobileConnectionController
- MobileDashboardController
- MobileDescricaoController
- MobileHealthController
- MobilePatrimonioController
- MobileResponsavelController
- MobileSalaController
- MobileSetorController
- MobileSyncController
- MobileTestController
- MobileUsuarioController

**Controllers a Remover:**
- AIEnhancedChatbotController
- LegalDocumentController
- ChatbotController
- AIIntegrationController
- AIPerformanceController

#### Package: Core Services

**Services a Adicionar:**
- DashboardService
- DataSyncService
- DataSyncScheduler
- DescricaoResumoService
- MobileConnectionService
- MobileServerManager
- ParticipanteInventarioService
- SalaInventarioService
- ConnectedDevicesManager
- PatrimonioService
- InventarioService
- ColetaService
- RelatorioService
- ResponsavelService
- SalaService
- SetorService
- CampusService
- UsuarioService
- QRCodeService
- AutenticacaoService

**Services a Remover:**
- Todos os AI Services (ChatbotService, AIIntegrationService, AIDataAnalysisService, PatrimonioCategorizationService, PredictiveAnalysisService, LegalDocumentProcessor, LegalKnowledgeIntegrator, BusinessAnalyticsEngine, UnifiedConfigurationManager)

#### Package: Mobile Services

**Services a Adicionar:**
- MobileAuthService
- MobileColetaService
- MobileDashboardService
- MobilePatrimonioService
- MobileResponsavelService
- MobileSalaService
- MobileSyncService

#### Package: DAOs

**DAOs a Adicionar:**
- BaseDAO
- CampusDAO
- ColetaDAO
- ColetaDAORefactored
- ColetorDAO
- DashboardColetaDAO
- InventarioDAO
- InventarioDAORefactored
- InventarioSetorDAO
- ParticipanteInventarioDAO
- PatrimonioDAORefactored
- QRCodeDAO
- RelatorioColetaDAO
- ResponsavelDAORefactored
- SalaDAORefactored
- SalaInventarioDAO
- SetorDAORefactored
- UsuarioDAORefactored

### 3. Diagrama de Casos de Uso (diagrama-casos-uso.puml)

#### Casos de Uso a Adicionar

**Gestão de Inventário (expandir):**
- UC56: Gerenciar Participantes de Inventário
- UC57: Associar Salas ao Inventário
- UC58: Associar Setores ao Inventário
- UC59: Monitorar Progresso por Sala
- UC60: Monitorar Progresso por Setor

**Coleta Mobile (expandir):**
- UC61: Monitorar Conexões Mobile
- UC62: Visualizar Dispositivos Conectados
- UC63: Rastrear Coletas por Dispositivo
- UC64: Gerar Descrição Resumida Automática

**Dashboard e Analytics:**
- UC65: Visualizar Dashboard de Coleta
- UC66: Visualizar Estatísticas em Tempo Real
- UC67: Visualizar Coletas por Dia
- UC68: Visualizar Progresso por Coletor

#### Casos de Uso a Remover
- Todos os casos de uso relacionados a IA (UC35-UC40, UC43, UC46-UC50, UC54-UC55)
- Casos de uso de Chatbot (UC41-UC45)

### 4. Diagrama de Sequência (diagrama-sequencia.puml)

Reescrever completamente com fluxos reais implementados.

#### Fluxo 1: Autenticação Mobile com JWT

```
Coletor -> MobileAuthController: POST /api/mobile/auth/login
MobileAuthController -> MobileAuthService: autenticar(login, senha)
MobileAuthService -> UsuarioDAORefactored: buscarPorLogin(login)
UsuarioDAORefactored -> Database: SELECT * FROM usuario
Database --> UsuarioDAORefactored: Usuario
UsuarioDAORefactored --> MobileAuthService: Usuario
MobileAuthService -> PasswordUtil: verificarSenha(senha, hash)
PasswordUtil --> MobileAuthService: boolean
MobileAuthService -> JwtTokenProvider: gerarToken(usuario)
JwtTokenProvider --> MobileAuthService: String token
MobileAuthService -> MobileConnectionService: registrarConexao(deviceInfo, usuario)
MobileConnectionService --> MobileAuthService: void
MobileAuthService --> MobileAuthController: MobileLoginResponse
MobileAuthController --> Coletor: 200 OK + JWT Token
```

#### Fluxo 2: Coleta Mobile com Sincronização

```
Coletor -> MobileColetaController: POST /api/mobile/coleta
MobileColetaController -> JwtAuthenticationFilter: validarToken()
JwtAuthenticationFilter --> MobileColetaController: Usuario autenticado
MobileColetaController -> MobileColetaService: registrarColeta(request)
MobileColetaService -> PatrimonioDAORefactored: buscarPorNumero(numero)
PatrimonioDAORefactored --> MobileColetaService: Patrimonio
MobileColetaService -> ColetaDAORefactored: inserirColeta(coleta)
ColetaDAORefactored -> Database: INSERT INTO coleta
Database --> ColetaDAORefactored: id
ColetaDAORefactored --> MobileColetaService: Coleta
MobileColetaService -> DataSyncService: notificarNovaColeta(coleta)
DataSyncService -> WebSocket: broadcast(coleta)
WebSocket --> DashboardColetaFrame: atualizar()
MobileColetaService --> MobileColetaController: MobileColetaResponse
MobileColetaController --> Coletor: 201 Created
```

#### Fluxo 3: Dashboard em Tempo Real

```
Gestor -> DashboardColetaFrame: abrir()
DashboardColetaFrame -> DashboardService: obterEstatisticas(idInventario)
DashboardService -> DashboardColetaDAO: buscarEstatisticas(idInventario)
DashboardColetaDAO -> Database: SELECT estatísticas
Database --> DashboardColetaDAO: ResultSet
DashboardColetaDAO --> DashboardService: DashboardStatsDTO
DashboardService --> DashboardColetaFrame: DashboardStatsDTO
DashboardColetaFrame -> WebSocket: conectar()
WebSocket --> DashboardColetaFrame: conexão estabelecida
[Nova Coleta Registrada]
WebSocket -> DashboardColetaFrame: onMessage(coleta)
DashboardColetaFrame -> DashboardService: obterEstatisticas(idInventario)
DashboardService --> DashboardColetaFrame: DashboardStatsDTO atualizado
DashboardColetaFrame -> UI: atualizar componentes
```

#### Fluxo 4: Geração de Descrição Resumida

```
Operador -> PatrimonioFormDialog: digitar descrição
Operador -> PatrimonioFormDialog: clicar "Gerar Resumo"
PatrimonioFormDialog -> DescricaoResumoService: gerarResumo(descricao)
DescricaoResumoService -> DescricaoResumoService: extrairPalavrasChave(descricao)
DescricaoResumoService -> DescricaoResumoService: removerStopWords()
DescricaoResumoService -> DescricaoResumoService: ordenarPorRelevancia()
DescricaoResumoService -> DescricaoResumoService: construirResumo()
DescricaoResumoService --> PatrimonioFormDialog: String resumo
PatrimonioFormDialog -> UI: preencher campo resumo
PatrimonioFormDialog --> Operador: exibir mensagem sucesso
```

### 5. Diagrama de Componentes (diagrama-componentes.puml)

#### Estrutura de Alto Nível

```
┌─────────────────────────────────────────────────────────┐
│              Camada de Apresentação                      │
├─────────────────────────────────────────────────────────┤
│  Desktop Application (Swing)  │  Mobile API (REST)      │
│  - MainFrame                  │  - Mobile Controllers   │
│  - PatrimonioFrame            │  - JWT Authentication   │
│  - ColetaFrame_v2             │  - WebSocket Support    │
│  - DashboardColetaFrame       │                         │
│  - InventarioFrame            │  Android App (Kotlin)   │
│  - RelatorioFrame             │  - Retrofit Client      │
│                               │  - Room Database        │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              Camada de Controle                          │
├─────────────────────────────────────────────────────────┤
│  Security & Authentication    │  Configuration          │
│  - JwtTokenProvider           │  - SecurityConfig       │
│  - JwtAuthenticationFilter    │  - DatabaseConfig       │
│  - CustomUserDetailsService   │  - WebSocketConfig      │
│                               │  - NotificationConfig   │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              Camada de Negócio                           │
├─────────────────────────────────────────────────────────┤
│  Core Services                │  Mobile Services        │
│  - PatrimonioService          │  - MobileAuthService    │
│  - InventarioService          │  - MobileColetaService  │
│  - ColetaService              │  - MobileSyncService    │
│  - DashboardService           │  - MobileDashboardSvc   │
│  - RelatorioService           │                         │
│  - DescricaoResumoService     │  Sync & Connection      │
│  - QRCodeService              │  - DataSyncService      │
│                               │  - MobileConnectionSvc  │
│                               │  - ConnectedDevicesMgr  │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              Camada de Dados                             │
├─────────────────────────────────────────────────────────┤
│  Data Access Layer            │  Offline Support        │
│  - PatrimonioDAORefactored    │  - OfflineManager       │
│  - InventarioDAORefactored    │  - DataSynchronizer     │
│  - ColetaDAORefactored        │  - ConnectivityManager  │
│  - DashboardColetaDAO         │  - SQLiteConnection     │
│  - UsuarioDAORefactored       │                         │
│  - BaseDAO                    │                         │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              Camada de Persistência                      │
├─────────────────────────────────────────────────────────┤
│  PostgreSQL Database          │  SQLite Local Cache     │
│  - Tabelas principais         │  - Cache offline        │
│  - Views                      │  - Sincronização        │
│  - Triggers                   │                         │
│                               │  File System            │
│                               │  - QR Codes             │
│                               │  - Relatórios           │
│                               │  - Logs                 │
└─────────────────────────────────────────────────────────┘
```

#### Componentes a Remover
- Todos os componentes de IA
- Chatbot Interface
- AI Services
- External APIs (não implementadas)

## Data Models

### Estrutura de Arquivos PlantUML

Cada arquivo `.puml` seguirá a estrutura:

```plantuml
@startuml Título do Diagrama
!theme plain
title Sistema de Inventário - Subtítulo

' Definições de packages e classes

' Relacionamentos

' Notas explicativas (quando necessário)

@enduml
```

### Convenções de Nomenclatura

- **Classes**: PascalCase
- **Atributos**: camelCase com prefixo `-` (privado) ou `+` (público)
- **Métodos**: camelCase com `()` e tipo de retorno
- **Packages**: "Nome do Package" entre aspas
- **Relacionamentos**: Usar notação UML padrão (1:N, 1:1, etc.)

## Error Handling

### Validação de Diagramas

Antes de considerar a atualização completa:

1. **Validar sintaxe PlantUML**: Testar cada arquivo `.puml` no PlantUML Online
2. **Verificar completude**: Comparar com código fonte para garantir que nada foi omitido
3. **Validar relacionamentos**: Garantir que todos os relacionamentos fazem sentido
4. **Revisar nomenclatura**: Garantir consistência com código real

### Tratamento de Discrepâncias

Se encontrar discrepâncias entre código e design:

1. **Priorizar código**: O código é a fonte da verdade
2. **Documentar decisões**: Adicionar notas explicativas quando necessário
3. **Simplificar quando apropriado**: Não incluir todos os métodos, apenas os principais

## Testing Strategy

### Verificação de Qualidade

1. **Geração de Imagens**: Gerar PNG de cada diagrama para verificar renderização
2. **Revisão Visual**: Verificar se diagramas estão legíveis e bem organizados
3. **Validação de Conteúdo**: Comparar com código fonte
4. **Teste de Links**: Verificar se README referencia corretamente os arquivos

### Critérios de Aceitação

- [ ] Todos os arquivos `.puml` compilam sem erros
- [ ] Imagens PNG são geradas corretamente
- [ ] README está atualizado com descrições corretas
- [ ] Nenhum componente não implementado está presente
- [ ] Todos os componentes principais implementados estão presentes
- [ ] Relacionamentos estão corretos e completos
- [ ] Data de atualização e versão estão atualizadas

## Implementation Notes

### Ordem de Atualização

1. **diagrama-entidades.puml**: Adicionar novas entidades e relacionamentos
2. **diagrama-controllers-services.puml**: Reescrever completamente
3. **diagrama-casos-uso.puml**: Adicionar novos casos de uso e remover não implementados
4. **diagrama-sequencia.puml**: Reescrever com fluxos reais
5. **diagrama-componentes.puml**: Atualizar arquitetura de alto nível
6. **README.md**: Atualizar documentação completa

### Ferramentas Necessárias

- PlantUML (para geração de imagens)
- Editor de texto (para edição dos arquivos `.puml`)
- Navegador web (para validação no PlantUML Online)

### Considerações de Manutenibilidade

- Manter estrutura modular para facilitar futuras atualizações
- Usar comentários nos arquivos `.puml` para explicar seções
- Manter consistência de estilo entre todos os diagramas
- Documentar versão e data de atualização em cada arquivo
