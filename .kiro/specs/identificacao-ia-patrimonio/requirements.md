# Requirements Document

## Introduction

Este documento define os requisitos para a feature de **Identificação de Patrimônios por IA**, que utiliza Google ML Kit para analisar fotos de itens e sugerir descrições compatíveis com o sistema de inventário. A feature é integrada como **subopção da Coleta por Descrição**, permitindo que o coletor tire uma foto do item e receba sugestões de descrições que correspondem ao objeto identificado.

**Fluxo Principal:**
```
Coleta por Descrição → 📷 Identificar por Foto → 🤖 IA analisa → 🔍 Filtra descrições → ✅ Seleciona e coleta
```

## Glossary

- **ML_Kit**: Google ML Kit - biblioteca de machine learning on-device para Android
- **Image_Labeling**: Serviço do ML Kit que identifica objetos em imagens e retorna labels com níveis de confiança
- **Label**: Identificação retornada pela IA (ex: "Chair", "Computer Monitor")
- **Confiança**: Percentual de certeza da IA sobre a identificação (0-100%)
- **Descrição_Sistema**: Descrições de patrimônios já cadastradas no banco de dados local
- **Sugestão**: Descrição do sistema que corresponde aos labels identificados pela IA
- **Matching_Score**: Pontuação de correspondência entre labels da IA e descrições do sistema
- **Coleta_Assistida**: Processo de coleta onde a IA auxilia na identificação do item

## Requirements

### Requirement 1: Integração com Google ML Kit

**User Story:** As a developer, I want to integrate Google ML Kit Image Labeling, so that the app can identify objects in photos.

#### Acceptance Criteria

1. THE App SHALL include Google ML Kit Image Labeling dependency in the build configuration
2. WHEN the app is installed, THE ML_Kit model SHALL be downloaded automatically for offline use
3. THE Image_Labeling service SHALL process images without requiring internet connection
4. WHEN processing an image, THE ML_Kit SHALL return labels with confidence scores above 50%
5. THE ML_Kit integration SHALL support images from camera capture and gallery selection

---

### Requirement 2: Captura e Análise de Imagem

**User Story:** As a collector, I want to take a photo of an item and have the AI analyze it, so that I can quickly identify what the item is.

#### Acceptance Criteria

1. WHEN the user opens the AI identification screen, THE System SHALL display a camera preview
2. WHEN the user captures a photo, THE System SHALL immediately send it to ML_Kit for analysis
3. WHILE the image is being analyzed, THE System SHALL display a loading indicator with message "Analisando imagem..."
4. WHEN analysis completes, THE System SHALL display the top 5 labels with their confidence percentages
5. IF the analysis fails, THEN THE System SHALL display an error message and allow retry
6. WHEN displaying labels, THE System SHALL translate common English labels to Portuguese (ex: "Chair" → "Cadeira")

---

### Requirement 3: Matching com Descrições do Sistema

**User Story:** As a collector, I want the AI suggestions to match existing descriptions in the system, so that I can select the correct patrimony type.

#### Acceptance Criteria

1. WHEN labels are identified, THE System SHALL search for matching descriptions in the local database
2. THE Matching algorithm SHALL compare labels against all unique descriptions of uncollected patrimônios
3. WHEN calculating Matching_Score, THE System SHALL consider:
   - Exact word matches (highest weight)
   - Partial word matches (medium weight)
   - Category matches (lower weight)
4. THE System SHALL display up to 10 suggested descriptions ordered by Matching_Score
5. WHEN no matches are found, THE System SHALL display the raw AI labels as fallback suggestions
6. FOR EACH suggestion, THE System SHALL display the number of uncollected items with that description

---

### Requirement 4: Seleção e Coleta Assistida

**User Story:** As a collector, I want to select a suggested description and proceed to collection, so that I can quickly register the item.

#### Acceptance Criteria

1. WHEN the user taps a suggested description, THE System SHALL navigate to the collection confirmation screen
2. THE Collection confirmation screen SHALL display:
   - The captured photo
   - The selected description
   - The number of matching uncollected items
   - Estado de conservação selector
3. WHEN the user confirms, THE System SHALL register the collection with the selected description
4. IF the user wants to edit the description, THEN THE System SHALL allow manual text input
5. WHEN collection is successful, THE System SHALL play success sound and show confirmation message

---

### Requirement 5: Configurações de IA

**User Story:** As a user, I want to configure AI behavior, so that I can optimize it for my needs.

#### Acceptance Criteria

1. THE Settings screen SHALL include an "Identificação por IA" section
2. THE User SHALL be able to enable/disable AI identification feature
3. THE User SHALL be able to set minimum confidence threshold (default: 50%)
4. WHEN AI is disabled, THE System SHALL hide the AI identification FAB in DescricaoSelectionActivity

---

### Requirement 7: Integração como Subopção da Coleta por Descrição

**User Story:** As a collector, I want AI identification to be integrated within the description selection flow, so that I have a unified experience for collecting items without tags.

#### Acceptance Criteria

1. THE DescricaoSelectionActivity SHALL include a floating action button "📷 Identificar por Foto" at the bottom
2. WHEN the user taps the FAB, THE System SHALL open the camera for AI identification
3. AFTER AI analysis, THE System SHALL filter the description list to show only matching descriptions
4. THE Filtered descriptions SHALL be highlighted with "🤖 Sugerido pela IA" badge
5. IF no matches are found, THE System SHALL show all descriptions with AI labels as search hint
6. THE User SHALL be able to clear the AI filter and see all descriptions again
7. THE AI identification flow SHALL use the same sala context as the parent DescricaoSelectionActivity
8. WHEN a suggested description is selected, THE System SHALL proceed with the normal collection flow

---

### Requirement 6: Performance e Otimização

**User Story:** As a user, I want the AI identification to be fast and efficient, so that it doesn't slow down my work.

#### Acceptance Criteria

1. THE Image analysis SHALL complete within 3 seconds on average devices
2. THE System SHALL resize images to maximum 640x480 before analysis to optimize performance
3. THE ML_Kit model SHALL be cached locally after first download
4. THE System SHALL release ML_Kit resources when returning to the description list

---

### Requirement 7: Feedback Visual e UX

**User Story:** As a user, I want clear visual feedback during AI identification, so that I understand what's happening.

#### Acceptance Criteria

1. WHEN analyzing, THE System SHALL display animated scanning effect over the image
2. THE Confidence scores SHALL be displayed as colored indicators:
   - Green: 80-100% (alta confiança)
   - Yellow: 60-79% (média confiança)
   - Orange: 50-59% (baixa confiança)
3. THE Filtered descriptions SHALL display the AI matching indicator with category icon
4. WHEN no suggestions are found, THE System SHALL display helpful message suggesting manual search

---

### Requirement 8: Tratamento de Erros e Edge Cases

**User Story:** As a user, I want the system to handle errors gracefully, so that I can continue working even when AI fails.

#### Acceptance Criteria

1. IF ML_Kit model is not downloaded, THEN THE System SHALL show download prompt with progress
2. IF camera permission is denied, THEN THE System SHALL show explanation and settings link
3. IF image is too dark or blurry, THEN THE System SHALL suggest retaking the photo
4. IF device doesn't support ML_Kit, THEN THE System SHALL hide the AI FAB
5. WHEN offline and model not cached, THE System SHALL inform user and allow manual search
6. THE System SHALL log AI identification attempts for debugging purposes
