# Implementation Plan: Identificação de Patrimônios por IA

## Overview

Implementação da feature de identificação de patrimônios usando Google ML Kit Image Labeling, integrada como subopção da Coleta por Descrição. A implementação segue Clean Architecture + MVVM com Hilt para injeção de dependência.

## Tasks

- [x] 1. Configurar dependências e infraestrutura
  - [x] 1.1 Adicionar dependências do ML Kit e CameraX no build.gradle
    - Adicionar `com.google.mlkit:image-labeling:17.0.7`
    - Adicionar dependências CameraX para preview de câmera
    - _Requirements: 1.1_

  - [x] 1.2 Criar estrutura de pacotes para a feature
    - Criar pacotes: `domain/model`, `domain/usecase`, `data/ai`, `presentation/ai`
    - _Requirements: N/A (infraestrutura)_

- [x] 2. Implementar camada Domain
  - [x] 2.1 Criar modelo AILabel
    - Implementar data class com originalText, translatedText, confidence, confidenceLevel
    - Implementar enum ConfidenceLevel (HIGH, MEDIUM, LOW)
    - Implementar companion object com fromMLKitLabel()
    - _Requirements: 2.4, 7.2_

  - [x] 2.2 Criar modelo DescricaoSugerida
    - Implementar data class com descricao, quantidade, matchingScore, categoria, icone, matchedLabels
    - _Requirements: 3.4, 3.6_

  - [x] 2.3 Criar interface AIIdentificationRepository
    - Definir métodos: analyzeImage(), findMatchingDescriptions(), isMLKitAvailable(), isModelDownloaded(), downloadModel()
    - _Requirements: 1.2, 1.3, 3.1_

  - [x] 2.4 Criar AnalisarImagemUseCase
    - Implementar invoke() que chama repository.analyzeImage()
    - Validar bitmap não nulo
    - _Requirements: 2.2_

  - [x] 2.5 Criar BuscarDescricoesCorrespondentesUseCase
    - Implementar invoke() que busca descrições correspondentes aos labels
    - Ordenar por matchingScore descendente
    - Limitar a 10 resultados
    - _Requirements: 3.1, 3.4_

- [x] 3. Implementar camada Data
  - [x] 3.1 Criar LabelTranslator
    - Implementar mapa de traduções EN→PT para labels comuns
    - Implementar translate() que retorna tradução ou original se não encontrado
    - _Requirements: 2.6_

  - [x] 3.2 Escrever property test para LabelTranslator
    - **Property 3: Label Translation Consistency**
    - **Validates: Requirements 2.6**
    - ✅ Implementado em `LabelTranslatorPropertyTest.kt`

  - [x] 3.3 Criar DescricaoMatchingEngine
    - Implementar calculateMatchingScore() com pesos: exact=1.0, partial=0.5, category=0.3
    - Implementar hasPartialMatch() e hasCategoryMatch()
    - Normalizar score para [0, 1]
    - _Requirements: 3.3_

  - [x] 3.4 Escrever property test para DescricaoMatchingEngine
    - **Property 5: Matching Score Calculation**
    - **Validates: Requirements 3.3**
    - ✅ Implementado em `DescricaoMatchingEnginePropertyTest.kt`

  - [x] 3.5 Criar MLKitImageLabeler
    - Implementar wrapper para ImageLabeler do ML Kit
    - Implementar analyze() com filtro de confiança mínima
    - Implementar release() para liberar recursos
    - Implementar isAvailable() para verificar suporte do dispositivo
    - _Requirements: 1.1, 1.3, 1.4, 6.4_

  - [x] 3.6 Criar ImageResizer utility
    - Implementar resize para máximo 640x480 mantendo aspect ratio
    - _Requirements: 6.2_

  - [x] 3.7 Escrever property test para ImageResizer
    - **Property 9: Image Resize Before Analysis**
    - **Validates: Requirements 6.2**
    - ✅ Implementado em `ImageResizerPropertyTest.kt`

  - [x] 3.8 Criar AIIdentificationRepositoryImpl
    - Implementar analyzeImage() usando MLKitImageLabeler
    - Implementar findMatchingDescriptions() usando DescricaoMatchingEngine
    - Buscar descrições não coletadas do PatrimonioDao
    - _Requirements: 1.3, 3.1, 3.2_

- [ ] 4. Checkpoint - Verificar camadas Domain e Data
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implementar camada Presentation
  - [x] 5.1 Criar AIIdentificationState sealed class
    - Implementar estados: Idle, CameraReady, Capturing, Analyzing, LabelsIdentified, SuggestionsReady, Error, ModelDownloading
    - _Requirements: 2.3, 2.5_

  - [x] 5.2 Criar AIIdentificationViewModel
    - Implementar com @HiltViewModel
    - Injetar AnalisarImagemUseCase e BuscarDescricoesCorrespondentesUseCase
    - Implementar analyzeImage(), retryAnalysis(), clearState()
    - Gerenciar estados via StateFlow
    - _Requirements: 2.2, 2.3, 2.4, 2.5_

  - [x] 5.3 Criar layout activity_ai_identification.xml
    - Incluir PreviewView para câmera
    - Incluir botão de captura
    - Incluir área de resultados (labels e sugestões)
    - Incluir indicadores de loading e erro
    - _Requirements: 2.1, 2.3, 7.1_

  - [x] 5.4 Criar AILabelAdapter para RecyclerView
    - Exibir label traduzido com confiança
    - Aplicar cores baseadas em ConfidenceLevel (verde/amarelo/laranja)
    - _Requirements: 2.4, 7.2_

  - [x] 5.5 Escrever property test para cores de confiança
    - **Property 10: Confidence Color Coding**
    - **Validates: Requirements 7.2**
    - ✅ Implementado em `AILabelConfidenceColorPropertyTest.kt`

  - [x] 5.6 Criar DescricaoSugeridaAdapter para RecyclerView
    - Exibir descrição com badge "🤖 Sugerido pela IA"
    - Exibir quantidade de itens pendentes
    - Exibir ícone de categoria
    - _Requirements: 3.6, 7.3_

  - [x] 5.7 Criar AIIdentificationActivity
    - Implementar com @AndroidEntryPoint
    - Configurar CameraX para preview
    - Implementar captura de foto
    - Observar estados do ViewModel
    - Navegar de volta com resultado (descrição selecionada)
    - _Requirements: 2.1, 2.2, 4.1_

- [x] 6. Integrar com DescricaoSelectionActivity
  - [x] 6.1 Adicionar FAB "📷 Identificar por Foto" no layout
    - Posicionar no canto inferior direito
    - Usar ícone de câmera
    - _Requirements: 7.1_

  - [x] 6.2 Implementar lógica de visibilidade do FAB
    - Verificar se AI está habilitada nas configurações
    - Verificar se dispositivo suporta ML Kit
    - Esconder FAB se qualquer condição falhar
    - _Requirements: 5.4, 8.4_

  - [x] 6.3 Escrever property test para visibilidade do FAB
    - **Property 8: FAB Visibility Control**
    - **Validates: Requirements 5.4, 8.4**
    - ✅ Implementado em `FABVisibilityPropertyTest.kt`

  - [x] 6.4 Implementar navegação para AIIdentificationActivity
    - Passar contexto da sala (salaId, salaNome)
    - Receber resultado via ActivityResult
    - _Requirements: 7.7_

  - [x] 6.5 Implementar filtro de descrições baseado em sugestões da IA
    - Filtrar lista para mostrar apenas descrições sugeridas
    - Adicionar badge "🤖 Sugerido pela IA" nas descrições filtradas
    - Adicionar botão para limpar filtro
    - _Requirements: 7.3, 7.4, 7.6_

- [ ] 7. Checkpoint - Verificar integração
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Implementar configurações de IA
  - [ ] 8.1 Adicionar seção "Identificação por IA" nas configurações
    - Switch para habilitar/desabilitar
    - Slider para threshold de confiança (50-90%)
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ] 8.2 Criar AISettings data class e persistência
    - Salvar em SharedPreferences via PreferencesManager
    - _Requirements: 5.2, 5.3_

- [ ] 9. Implementar tratamento de erros
  - [ ] 9.1 Criar AIIdentificationError sealed class
    - Implementar tipos: ModelNotDownloaded, CameraPermissionDenied, DeviceNotSupported, ImageTooLowQuality, AnalysisFailed, OfflineAndNoModel
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [ ] 9.2 Implementar dialog de download do modelo
    - Mostrar progresso de download
    - Permitir cancelar
    - _Requirements: 8.1_

  - [ ] 9.3 Implementar verificação de qualidade de imagem
    - Detectar imagens muito escuras ou borradas
    - Sugerir nova captura
    - _Requirements: 8.3_

  - [ ] 9.4 Adicionar logging para debugging
    - Logar tentativas de identificação
    - Logar erros e exceções
    - _Requirements: 8.6_

- [x] 10. Configurar injeção de dependência
  - [x] 10.1 Criar AIModule para Hilt
    - Prover MLKitImageLabeler
    - Prover LabelTranslator
    - Prover DescricaoMatchingEngine
    - Bind AIIdentificationRepository → AIIdentificationRepositoryImpl
    - _Requirements: N/A (infraestrutura)_

- [ ] 11. Checkpoint final
  - Ensure all tests pass, ask the user if questions arise.
  - Verificar fluxo completo: FAB → Câmera → Análise → Sugestões → Coleta
  - Testar modo offline com modelo cacheado
  - Testar com AI desabilitada nas configurações

## Notes

- Todas as tasks são obrigatórias, incluindo property tests
- Cada task referencia os requisitos específicos para rastreabilidade
- Checkpoints garantem validação incremental
- Property tests validam propriedades de corretude universais
- Unit tests validam exemplos específicos e edge cases
