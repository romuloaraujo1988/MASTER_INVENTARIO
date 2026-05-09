# Requirements Document

## Introduction

Este documento especifica os requisitos para prevenir a violação das regras de composição de itens no Sistema de Inventário Patrimonial.

### Contexto do Problema

O sistema possui dois tipos de coletas:
1. **Coleta Normal** - para itens individuais (patrimônios simples)
2. **Coleta Composta** - para itens compostos por múltiplos componentes (patrimônios em `tabela_item_composto`)

### Problema Atual

Atualmente, o sistema permite que itens compostos sejam coletados como itens normais, violando a regra de negócio. Por exemplo:
- Patrimônio 303752 (Conjunto Escolar) possui 2 componentes
- Foi coletado como item normal no coleta_id 6946
- Isso NÃO deveria ser permitido

### Impacto

- Perda de integridade dos dados de inventário
- Inconsistência entre o que foi coletado e o que foi planejado
- Dificuldade em rastrear componentes faltantes
- Relatórios incorretos sobre integridade de conjuntos

**Escopo:** Esta funcionalidade será implementada na aplicação desktop Java Swing, utilizando a estrutura de dados existente nas tabelas `tabela_item_composto` e `tabela_coleta_componente`.

## Glossary

- **Item Composto**: Patrimônio que consiste em dois ou mais itens físicos distintos registrados sob um único número de patrimônio
- **Item Principal**: O patrimônio registrado na TABELA_PATRIMONIO que possui componentes associados
- **Componente**: Item físico individual que faz parte de um item composto (ex: cadeira, mesa, monitor)
- **Coleta Normal**: Coleta de um patrimônio simples (não composto)
- **Coleta Composta**: Coleta de um item composto que registra todos os seus componentes
- **Sistema**: O Sistema de Inventário Patrimonial (SIHCP) - aplicação desktop
- **Validação de Composição**: Verificação se um patrimônio é composto antes de permitir coleta normal
- **Regra de Negócio**: Regra que proíbe coletar itens compostos como itens normais

## Requirements

### Requirement 1

**User Story:** As a coletor, I want that the system prevent me from collecting composite items as normal items, so that I maintain data integrity and follow the correct collection procedure.

#### Acceptance Criteria

1. WHEN a user attempts to collect an item that is marked as composite THEN THE Sistema SHALL validate that the item exists in TABELA_ITEM_COMPOSTO
2. WHEN an item is identified as composite THEN THE Sistema SHALL block the normal collection and display an error message explaining why
3. WHEN the error message is displayed THEN THE Sistema SHALL inform that the item is composite and must be collected using the composite collection flow
4. WHEN a user tries to collect a composite item as normal THEN THE Sistema SHALL NOT create any collection record
5. WHEN a user attempts to collect a composite item THEN THE Sistema SHALL provide clear guidance on the correct procedure

### Requirement 2

**User Story:** As a system administrator, I want to identify all composite items that were incorrectly collected as normal items, so that I can analyze and fix these data integrity violations.

#### Acceptance Criteria

1. WHEN a report of composite items collected as normal is generated THEN THE Sistema SHALL search for items that exist in TABELA_ITEM_COMPOSTO but also have a normal collection record
2. WHEN the report is generated THEN THE Sistema SHALL list all violations with: item number, description, location, collection date, and collector
3. WHEN violations are found THEN THE Sistema SHALL provide statistics: total violations, items affected, and percentage of composite items with violations
4. WHEN the report is exported THEN THE Sistema SHALL offer formats: Excel (.xlsx), PDF, and CSV
5. WHEN the report is displayed THEN THE Sistema SHALL highlight violations visually with color coding

### Requirement 3

**User Story:** As a system administrator, I want to have a way to fix existing violations where composite items were collected as normal, so that I can restore data integrity.

#### Acceptance Criteria

1. WHEN a violation is identified THEN THE Sistema SHALL provide an option to mark the normal collection as invalid
2. WHEN a normal collection is marked as invalid THEN THE Sistema SHALL preserve the collection data but flag it as "invalid due to composition rule violation"
3. WHEN a violation is fixed THEN THE Sistema SHALL update the report to reflect the correction
4. WHEN a correction is made THEN THE Sistema SHALL log the action with timestamp and user who made the change
5. WHEN multiple violations exist THEN THE Sistema SHALL allow bulk correction with filtering options

### Requirement 4

**User Story:** As a system administrator, I want the validation to be performed before any collection is saved, so that no violations can occur.

#### Acceptance Criteria

1. WHEN a collection is initiated for any item THEN THE Sistema SHALL check if the item exists in TABELA_ITEM_COMPOSTO
2. WHEN the validation check is performed THEN THE Sistema SHALL complete within 500ms to avoid user delay
3. WHEN an item is composite AND the user attempts normal collection THEN THE Sistema SHALL prevent the collection before database insertion
4. WHEN the validation fails THEN THE Sistema SHALL NOT attempt to save the collection to the database
5. WHEN validation is skipped or disabled THEN THE Sistema SHALL log the event for audit purposes

### Requirement 5

**User Story:** As a system administrator, I want the validation to work correctly in both online and offline modes, so that data integrity is maintained regardless of connection status.

#### Acceptance Criteria

1. WHEN validation is performed in offline mode THEN THE Sistema SHALL check TABELA_ITEM_COMPOSTO in the local SQLite database
2. WHEN validation is performed in online mode THEN THE Sistema SHALL check TABELA_ITEM_COMPOSTO in the PostgreSQL database
3. WHEN switching between online and offline modes THEN THE Sistema SHALL maintain consistent validation behavior
4. WHEN offline data is synchronized with the server THEN THE Sistema SHALL validate that no composite items were collected as normal during offline period
5. WHEN a violation is detected during sync THEN THE Sistema SHALL flag it for review by administrator

### Requirement 6

**User Story:** As a system administrator, I want to configure which users are subject to composition validation, so that I can control the scope of this rule.

#### Acceptance Criteria

1. WHEN a user profile is configured THEN THE Sistema SHALL allow setting whether composition validation applies to that profile
2. WHEN validation is enabled for a profile THEN THE Sistema SHALL enforce the rule for all collections by users with that profile
3. WHEN validation is disabled for a profile THEN THE Sistema SHALL allow collection of composite items as normal (with warning)
4. WHEN a user attempts collection THEN THE Sistema SHALL check their profile settings before applying validation
5. WHEN profile settings are changed THEN THE Sistema SHALL apply the new settings to future collections only

### Requirement 7

**User Story:** As a system administrator, I want to track when validation rules are bypassed, so that I can audit and investigate potential issues.

#### Acceptance Criteria

1. WHEN a user bypasses validation (with permission) THEN THE Sistema SHALL log the action with: timestamp, user, item details, and reason
2. WHEN validation is skipped for a collection THEN THE Sistema SHALL store a flag indicating validation was bypassed
3. WHEN audit logs are queried THEN THE Sistema SHALL filter by: date range, user, item, or bypass status
4. WHEN audit logs are exported THEN THE Sistema SHALL include all bypass events in the export
5. WHEN a violation is detected AFTER collection THEN THE Sistema SHALL flag it in the violation report

### Requirement 8

**User Story:** As a system administrator, I want clear error messages that explain why a collection was blocked, so that users understand the business rules.

#### Acceptance Criteria

1. WHEN a composite item collection is blocked THEN THE Sistema SHALL display a message in Portuguese
2. WHEN the error message is displayed THEN THE Sistema SHALL include: item number, item description, and number of components
3. WHEN the error message is displayed THEN THE Sistema SHALL explain that composite items require special collection procedure
4. WHEN the user clicks "More Information" THEN THE Sistema SHALL show additional details about the correct procedure
5. WHEN multiple items are being collected THEN THE Sistema SHALL indicate which specific item caused the block

### Requirement 9

**User Story:** As a system administrator, I want the validation to be fast and not impact collection performance, so that users can work efficiently.

#### Acceptance Criteria

1. WHEN validation is performed for a single item THEN THE Sistema SHALL complete within 200ms under normal conditions
2. WHEN validation is performed for 100 items in batch THEN THE Sistema SHALL complete within 2 seconds
3. WHEN database connection is slow THEN THE Sistema SHALL cache validation results for recently checked items
4. WHEN validation cache is available THEN THE Sistema SHALL use cached results instead of querying database
5. WHEN cache is invalidated THEN THE Sistema SHALL refresh validation data within 5 seconds

### Requirement 10

**User Story:** As a system administrator, I want to test the validation logic thoroughly, so that I can be confident it works correctly.

#### Acceptance Criteria

1. WHEN a normal item is collected THEN THE Sistema SHALL allow the collection without validation errors
2. WHEN a composite item is collected as normal THEN THE Sistema SHALL block the collection
3. WHEN a composite item is collected using the correct procedure THEN THE Sistema SHALL allow the collection
4. WHEN validation is disabled for a user profile THEN THE Sistema SHALL allow composite item collection with warning
5. WHEN a violation report is generated THEN THE Sistema SHALL accurately identify all violations
