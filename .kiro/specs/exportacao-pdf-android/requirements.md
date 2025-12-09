# Requirements Document

## Introduction

Este documento especifica os requisitos para a funcionalidade de exportação de dados em PDF no aplicativo Android de inventário. A funcionalidade permitirá que os usuários gerem relatórios em PDF dos patrimônios de uma sala específica, com filtros por status de coleta (Todos, Coletados, Não Coletados). O relatório incluirá um somatório dos itens ao final do documento.

## Glossary

- **Sistema**: Aplicativo Android de Inventário Mobile
- **Usuário**: Pessoa autenticada utilizando o aplicativo
- **Sala**: Localização física onde os patrimônios estão alocados
- **Patrimônio**: Item físico registrado no sistema de inventário
- **Coleta**: Ação de verificar fisicamente a existência de um patrimônio
- **PDF**: Portable Document Format, formato de arquivo para documentos
- **Filtro de Status**: Critério de seleção baseado no estado de coleta do patrimônio
- **Somatório**: Contagem total de itens incluídos no relatório

## Requirements

### Requirement 1

**User Story:** As a user, I want to access an export screen, so that I can generate PDF reports of assets.

#### Acceptance Criteria

1. WHEN the user navigates to the export screen THEN the System SHALL display a form with sala selection and filter options
2. WHEN the export screen loads THEN the System SHALL fetch and display the list of available salas from the local database
3. WHEN the user has not selected a sala THEN the System SHALL disable the export button and display a validation message
4. WHEN the export screen encounters an error loading salas THEN the System SHALL display an error message with retry option

### Requirement 2

**User Story:** As a user, I want to select a sala from a dropdown list, so that I can specify which location's assets to export.

#### Acceptance Criteria

1. WHEN the user taps the sala selector THEN the System SHALL display a searchable dropdown with all available salas
2. WHEN the user selects a sala THEN the System SHALL update the selection and enable the export button
3. WHEN the user searches for a sala by name THEN the System SHALL filter the dropdown list to show matching results
4. WHEN no salas match the search query THEN the System SHALL display a message indicating no results found

### Requirement 3

**User Story:** As a user, I want to filter assets by collection status, so that I can generate targeted reports.

#### Acceptance Criteria

1. WHEN the export screen loads THEN the System SHALL display three filter options: TODOS, COLETADOS, NÃO COLETADOS
2. WHEN the user selects the TODOS filter THEN the System SHALL include all patrimônios of the selected sala in the export
3. WHEN the user selects the COLETADOS filter THEN the System SHALL include only patrimônios marked as collected in the export
4. WHEN the user selects the NÃO COLETADOS filter THEN the System SHALL include only patrimônios not yet collected in the export
5. WHEN the screen loads THEN the System SHALL pre-select the TODOS filter as default

### Requirement 4

**User Story:** As a user, I want to generate a PDF report, so that I can have a printable document of the inventory.

#### Acceptance Criteria

1. WHEN the user taps the export button with valid selections THEN the System SHALL generate a PDF document with the filtered patrimônios
2. WHEN the PDF generation starts THEN the System SHALL display a loading indicator with progress message
3. WHEN the PDF generation completes successfully THEN the System SHALL save the file to the device storage and notify the user
4. WHEN the PDF generation fails THEN the System SHALL display an error message with the failure reason
5. WHEN no patrimônios match the filter criteria THEN the System SHALL display a message indicating no data to export

### Requirement 5

**User Story:** As a user, I want the PDF to contain detailed asset information, so that I can review the inventory data.

#### Acceptance Criteria

1. WHEN the PDF is generated THEN the System SHALL include a header with the sala name, filter applied, and generation date
2. WHEN the PDF is generated THEN the System SHALL include a table with columns: Número Patrimônio, Descrição, Estado, Status Coleta
3. WHEN a patrimônio has been collected THEN the System SHALL display the collection date and collector name in the PDF
4. WHEN the PDF is generated THEN the System SHALL format the document with readable fonts and proper spacing

### Requirement 6

**User Story:** As a user, I want to see a summary at the end of the PDF, so that I can quickly understand the totals.

#### Acceptance Criteria

1. WHEN the PDF is generated THEN the System SHALL include a summary section at the end of the document
2. WHEN the TODOS filter is applied THEN the System SHALL display total count, collected count, and not collected count in the summary
3. WHEN the COLETADOS filter is applied THEN the System SHALL display only the collected count in the summary
4. WHEN the NÃO COLETADOS filter is applied THEN the System SHALL display only the not collected count in the summary
5. WHEN the summary is generated THEN the System SHALL calculate and display the collection percentage for the sala

### Requirement 7

**User Story:** As a user, I want to share or open the generated PDF, so that I can distribute or print the report.

#### Acceptance Criteria

1. WHEN the PDF is generated successfully THEN the System SHALL display options to open or share the file
2. WHEN the user taps open THEN the System SHALL launch the default PDF viewer application
3. WHEN the user taps share THEN the System SHALL display the Android share sheet with available applications
4. WHEN no PDF viewer is installed THEN the System SHALL display a message suggesting to install a PDF reader

### Requirement 8

**User Story:** As a user, I want the PDF generation to work offline, so that I can export data without internet connection.

#### Acceptance Criteria

1. WHEN the device is offline THEN the System SHALL generate the PDF using locally cached data
2. WHEN generating PDF offline THEN the System SHALL include a watermark indicating the data may not be current
3. WHEN the device is offline THEN the System SHALL disable the share option and only allow local file access

### Requirement 9

**User Story:** As a user, I want the PDF to have a professional appearance, so that I can use it in formal contexts.

#### Acceptance Criteria

1. WHEN the PDF is generated THEN the System SHALL include the institution logo in the header
2. WHEN the PDF is generated THEN the System SHALL use consistent formatting with proper margins and page numbers
3. WHEN the PDF spans multiple pages THEN the System SHALL repeat the table header on each page
4. WHEN the PDF is generated THEN the System SHALL include a footer with page number and total pages

### Requirement 10

**User Story:** As a user, I want to generate a PDF with round-trip data integrity, so that the exported data accurately represents the source data.

#### Acceptance Criteria

1. WHEN the PDF is generated THEN the System SHALL serialize patrimônio data to PDF format preserving all field values
2. WHEN the PDF contains special characters THEN the System SHALL encode them correctly without data loss
3. WHEN the PDF is generated THEN the System SHALL validate that the item count in the summary matches the actual number of items in the document
