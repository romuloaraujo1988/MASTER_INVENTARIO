# Requirements Document

## Introduction

Este documento especifica os requisitos para a implementação de um sistema de abas na tela de visualização de inventário do app Android (`InventarioActivity`), permitindo múltiplas formas de consulta e filtro dos patrimônios coletados. A funcionalidade principal é adicionar uma nova aba com filtro por sala, complementando a aba existente de filtro por responsável, para facilitar o acompanhamento do progresso da coleta por localização física.

## Glossary

- **Inventário**: Processo de levantamento e verificação de patrimônios em um determinado período
- **Coleta**: Registro de verificação de um patrimônio durante o inventário
- **Sala**: Localização física onde os patrimônios estão alocados
- **Patrimônio**: Bem patrimonial da instituição identificado por número único
- **Status de Coleta**: Indica se um patrimônio foi coletado (verificado) ou está pendente
- **TabLayout**: Componente Material Design do Android que permite organizar conteúdo em abas
- **ViewPager2**: Componente Android para navegação entre fragmentos via swipe
- **InventarioActivity**: Tela de visualização de patrimônios do inventário no app Android
- **RecyclerView**: Componente Android para exibição de listas com reciclagem de views
- **ChipGroup**: Componente Material Design para filtros com chips selecionáveis

## Requirements

### Requirement 1

**User Story:** As a mobile inventory operator, I want to view collection progress by room on my Android device, so that I can identify which physical locations still need verification while in the field.

#### Acceptance Criteria

1. WHEN the user opens the InventarioActivity THEN the system SHALL display a TabLayout with two tabs: "Por Responsável" and "Por Sala"
2. WHEN the user selects the "Por Sala" tab THEN the system SHALL display a dropdown (AutoCompleteTextView) with all rooms from the current inventory
3. WHEN the user selects a room from the dropdown THEN the system SHALL display a RecyclerView showing all assets in that room with their collection status
4. WHEN displaying assets by room THEN the system SHALL show: Número Patrimônio, Descrição, Status indicator (green check for collected, yellow warning for pending)
5. WHEN a room has uncollected assets THEN the system SHALL display pending items with a yellow/orange background color in the list item

### Requirement 2

**User Story:** As a mobile inventory operator, I want to filter collected and pending assets separately on my phone, so that I can focus on items that still need verification.

#### Acceptance Criteria

1. WHEN viewing assets by room THEN the system SHALL provide a ChipGroup with filter options: "Todos", "Coletados", "Pendentes"
2. WHEN the user selects "Coletados" chip THEN the system SHALL display only assets that have been collected in the current inventory
3. WHEN the user selects "Pendentes" chip THEN the system SHALL display only assets that have not been collected yet
4. WHEN the filter changes THEN the system SHALL update the RecyclerView immediately using DiffUtil for smooth animations
5. WHEN displaying filtered results THEN the system SHALL show a count summary in a TextView: "Exibindo X de Y patrimônios"

### Requirement 3

**User Story:** As a mobile inventory manager, I want to see room-level statistics on my device, so that I can quickly assess collection progress per location.

#### Acceptance Criteria

1. WHEN the user views the "Por Sala" tab THEN the system SHALL display a summary CardView showing: total rooms, rooms completed (100%), rooms in progress
2. WHEN a room has all assets collected THEN the system SHALL display a green checkmark icon next to the room name in the dropdown
3. WHEN a room has partial collection THEN the system SHALL show the percentage completed (e.g., "Sala 101 - 75%") next to the room name
4. WHEN the user long-presses a room in the dropdown THEN the system SHALL display a Toast or Snackbar with: total assets, collected count, pending count

### Requirement 4

**User Story:** As a mobile inventory operator, I want to search for specific assets within a room on my phone, so that I can quickly locate items by number or description.

#### Acceptance Criteria

1. WHEN viewing the "Por Sala" tab THEN the system SHALL provide a SearchView in the toolbar or a TextInputLayout above the RecyclerView
2. WHEN the user types in the search field THEN the system SHALL filter the RecyclerView to show only assets matching the search term (by number or description)
3. WHEN the search field is cleared THEN the system SHALL restore the full list of assets for the selected room
4. WHEN no results match the search THEN the system SHALL display an empty state view with message "Nenhum patrimônio encontrado"

### Requirement 5

**User Story:** As a mobile user, I want the app to remember my tab selection, so that I can continue where I left off when returning to the screen.

#### Acceptance Criteria

1. WHEN the user switches between tabs THEN the system SHALL preserve the selected filters and search terms for each tab using ViewModel
2. WHEN the user navigates away and returns to InventarioActivity THEN the system SHALL restore the last active tab using SavedStateHandle
3. WHEN the user selects a different inventory THEN the system SHALL reset all filters to default values



