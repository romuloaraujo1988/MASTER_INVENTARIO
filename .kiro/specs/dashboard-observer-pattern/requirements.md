# Requirements Document

## Introduction

Este documento especifica os requisitos para implementar o padrão Observer na dashboard da aplicação desktop do SIHCP. Atualmente, a dashboard (`DashboardColetaFrame`) utiliza polling com `Timer` para atualizar os dados a cada 30 segundos, o que é ineficiente e não reflete mudanças em tempo real. A implementação do padrão Observer permitirá que a dashboard seja notificada automaticamente quando eventos relevantes ocorrerem (novas coletas sincronizadas, patrimônios atualizados, etc.), proporcionando uma experiência mais dinâmica e responsiva.

## Glossary

- **Dashboard**: Tela principal de monitoramento que exibe estatísticas e gráficos do inventário em tempo real
- **Observer**: Padrão de projeto onde objetos (observers) se registram para receber notificações de mudanças em outro objeto (subject)
- **Subject/Observable**: Objeto que mantém uma lista de observers e os notifica quando seu estado muda
- **DashboardEvent**: Evento que representa uma mudança relevante para a dashboard (nova coleta, sincronização, etc.)
- **EventBus**: Componente central que gerencia a publicação e distribuição de eventos para os observers registrados
- **Polling**: Técnica de verificação periódica de mudanças (atual implementação com Timer de 30s)
- **Push Notification**: Técnica onde o sistema notifica ativamente os interessados quando há mudanças

## Requirements

### Requirement 1

**User Story:** As a system administrator, I want the dashboard to update automatically when new collections are synchronized, so that I can see real-time progress without waiting for the 30-second refresh cycle.

#### Acceptance Criteria

1. WHEN a collection is synchronized from the mobile app THEN the Dashboard SHALL update the collection statistics within 2 seconds
2. WHEN multiple collections are synchronized in batch THEN the Dashboard SHALL consolidate updates and refresh once after the batch completes
3. WHEN the Dashboard receives a collection event THEN the Dashboard SHALL update only the affected components (cards, charts) without full page refresh
4. WHEN the Dashboard is not visible (minimized or in background) THEN the Dashboard SHALL queue events and apply them when becoming visible again

### Requirement 2

**User Story:** As a developer, I want a centralized event bus for dashboard events, so that I can easily add new event types and observers without modifying existing code.

#### Acceptance Criteria

1. THE EventBus SHALL provide methods to register and unregister observers for specific event types
2. THE EventBus SHALL support multiple observers for the same event type
3. THE EventBus SHALL deliver events asynchronously to avoid blocking the event source
4. WHEN an observer throws an exception THEN the EventBus SHALL log the error and continue notifying other observers
5. THE EventBus SHALL use a singleton pattern to ensure a single instance across the application

### Requirement 3

**User Story:** As a user, I want the dashboard to show a visual indicator when data is being updated, so that I know the system is actively refreshing.

#### Acceptance Criteria

1. WHEN the Dashboard receives an update event THEN the Dashboard SHALL display a subtle animation or indicator showing data refresh
2. WHEN the update completes THEN the Dashboard SHALL hide the refresh indicator within 500 milliseconds
3. WHEN an update fails THEN the Dashboard SHALL display an error indicator with a retry option

### Requirement 4

**User Story:** As a system administrator, I want to see which events triggered dashboard updates, so that I can understand the system activity.

#### Acceptance Criteria

1. THE Dashboard SHALL maintain a log of the last 10 events that triggered updates
2. WHEN hovering over the status area THEN the Dashboard SHALL display a tooltip with recent event history
3. THE event log SHALL include event type, timestamp, and source information

### Requirement 5

**User Story:** As a developer, I want the event system to support different event types for different data changes, so that observers can subscribe only to relevant events.

#### Acceptance Criteria

1. THE system SHALL define distinct event types: COLETA_SINCRONIZADA, PATRIMONIO_ATUALIZADO, INVENTARIO_ALTERADO, SALA_ATUALIZADA, RESPONSAVEL_ATUALIZADO
2. WHEN publishing an event THEN the EventBus SHALL include event metadata (timestamp, source, affected entity IDs)
3. THE observers SHALL be able to filter events by type when registering
4. THE system SHALL provide a pretty-printer for event serialization to support round-trip testing

### Requirement 6

**User Story:** As a user, I want the option to pause automatic updates temporarily, so that I can analyze the current data without interruption.

#### Acceptance Criteria

1. THE Dashboard SHALL provide a toggle button to pause/resume automatic event-driven updates
2. WHILE updates are paused THEN the Dashboard SHALL display a "Paused" indicator
3. WHEN updates are resumed THEN the Dashboard SHALL immediately fetch and display the latest data
4. THE Dashboard SHALL automatically resume updates after 5 minutes of being paused

### Requirement 7

**User Story:** As a developer, I want the observer pattern to integrate with the existing service layer, so that events are published from the appropriate business logic locations.

#### Acceptance Criteria

1. WHEN MobileColetaService processes a new collection THEN the service SHALL publish a COLETA_SINCRONIZADA event
2. WHEN PatrimonioService updates a patrimony THEN the service SHALL publish a PATRIMONIO_ATUALIZADO event
3. WHEN InventarioService changes inventory status THEN the service SHALL publish an INVENTARIO_ALTERADO event
4. THE event publishing SHALL not affect the transaction outcome of the original operation
5. IF event publishing fails THEN the original operation SHALL still complete successfully

### Requirement 8

**User Story:** As a system administrator, I want the dashboard to gracefully handle high-frequency events, so that the UI remains responsive during bulk operations.

#### Acceptance Criteria

1. WHEN more than 5 events occur within 1 second THEN the Dashboard SHALL debounce updates and refresh once after the burst
2. THE Dashboard SHALL limit UI updates to a maximum of 2 per second during high-frequency event periods
3. WHEN debouncing is active THEN the Dashboard SHALL display a "Batch update in progress" indicator
