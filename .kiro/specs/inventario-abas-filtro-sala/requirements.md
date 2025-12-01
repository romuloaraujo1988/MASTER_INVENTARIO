# Requirements Document

## Introduction

Este documento especifica os requisitos para a implementação de uma nova aba na tela de Inventário do aplicativo Android, permitindo a visualização de patrimônios filtrados por sala. Atualmente, a tela de inventário possui apenas filtro por responsável. A nova funcionalidade adicionará uma segunda aba para filtrar patrimônios por sala, exibindo estatísticas de coleta (encontrados vs. não coletados) para cada sala selecionada.

## Glossary

- **Inventário**: Processo de levantamento e verificação de patrimônios de uma instituição
- **Patrimônio**: Bem material pertencente à instituição, identificado por número único
- **Sala**: Localização física onde os patrimônios estão alocados
- **Coleta**: Ato de registrar que um patrimônio foi encontrado durante o inventário
- **Patrimônio Coletado/Encontrado**: Patrimônio que já foi verificado e registrado no inventário atual
- **Patrimônio Não Coletado/Pendente**: Patrimônio que ainda não foi verificado no inventário atual
- **TabLayout**: Componente Android para navegação entre abas
- **ViewPager2**: Componente Android para exibição de conteúdo paginado com suporte a swipe

## Requirements

### Requirement 1

**User Story:** As a inventory collector, I want to filter assets by room, so that I can see which assets have been collected and which are still pending in each room.

#### Acceptance Criteria

1. WHEN the user opens the Inventory screen THEN the System SHALL display a TabLayout with two tabs: "Por Responsável" and "Por Sala"
2. WHEN the user selects the "Por Sala" tab THEN the System SHALL display a dropdown/spinner with all available rooms
3. WHEN the user selects a room from the dropdown THEN the System SHALL load and display all assets assigned to that room
4. WHEN assets are displayed for a selected room THEN the System SHALL show a summary card with the count of collected assets and pending assets
5. WHEN the user switches between tabs THEN the System SHALL preserve the filter state of each tab independently

### Requirement 2

**User Story:** As a inventory collector, I want to see collection statistics for each room, so that I can track my progress and prioritize rooms with more pending assets.

#### Acceptance Criteria

1. WHEN a room is selected THEN the System SHALL display a statistics card showing: total assets, collected count, pending count, and collection percentage
2. WHEN the statistics are displayed THEN the System SHALL use visual indicators (colors/icons) to differentiate collected from pending counts
3. WHEN the collection percentage is below 50% THEN the System SHALL display the percentage in a warning color (orange/yellow)
4. WHEN the collection percentage is 100% THEN the System SHALL display a completion indicator (green checkmark)
5. WHEN no room is selected THEN the System SHALL display a message prompting the user to select a room

### Requirement 3

**User Story:** As a inventory collector, I want to filter the asset list by collection status within a room, so that I can focus on pending assets or review collected ones.

#### Acceptance Criteria

1. WHEN a room is selected THEN the System SHALL display filter chips: "Todos", "Coletados", "Não Coletados"
2. WHEN the user selects "Coletados" chip THEN the System SHALL display only assets that have been collected in the current inventory
3. WHEN the user selects "Não Coletados" chip THEN the System SHALL display only assets that have not been collected yet
4. WHEN the user selects "Todos" chip THEN the System SHALL display all assets in the selected room
5. WHEN the filter changes THEN the System SHALL update the statistics card to reflect the filtered count

### Requirement 4

**User Story:** As a inventory collector, I want the room list to show collection progress, so that I can quickly identify which rooms need attention.

#### Acceptance Criteria

1. WHEN the room dropdown is displayed THEN the System SHALL show each room name with its collection progress (e.g., "Sala 101 - 15/20 coletados")
2. WHEN a room has 100% collection THEN the System SHALL display a visual indicator (checkmark icon) next to the room name
3. WHEN a room has 0% collection THEN the System SHALL display a visual indicator (warning icon) next to the room name
4. WHEN the user types in the room search field THEN the System SHALL filter the room list by name or number

### Requirement 5

**User Story:** As a inventory collector, I want the interface to work offline, so that I can view room statistics even without internet connection.

#### Acceptance Criteria

1. WHEN the device is offline THEN the System SHALL load room and asset data from local database
2. WHEN the device is offline THEN the System SHALL display an offline indicator in the toolbar
3. WHEN the device reconnects THEN the System SHALL synchronize any pending changes automatically
4. WHEN local data is stale (older than 24 hours) THEN the System SHALL display a warning message suggesting synchronization

### Requirement 6

**User Story:** As a inventory collector, I want to see a visual representation of collection progress, so that I can quickly understand the overall status.

#### Acceptance Criteria

1. WHEN a room is selected THEN the System SHALL display a progress bar showing collection percentage
2. WHEN the progress bar is displayed THEN the System SHALL use color coding: red (0-25%), orange (26-50%), yellow (51-75%), green (76-100%)
3. WHEN the user taps on the progress bar THEN the System SHALL display a detailed breakdown of collection statistics
4. WHEN the statistics are displayed THEN the System SHALL include: total assets, collected today, collected this week, remaining
