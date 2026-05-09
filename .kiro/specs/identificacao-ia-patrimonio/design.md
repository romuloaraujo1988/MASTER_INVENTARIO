# Design Document: Identificação de Patrimônios por IA

## Overview

Esta feature integra Google ML Kit Image Labeling ao app Android para identificar objetos em fotos e sugerir descrições correspondentes do sistema de inventário. A funcionalidade é acessada como **subopção da Coleta por Descrição**, através de um FAB na `DescricaoSelectionActivity`.

### Fluxo Principal

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DescricaoSelectionActivity                            │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Lista de Descrições Não Coletadas                               │   │
│  │  ┌─────────────────────────────────────────────────────────┐    │   │
│  │  │ 🪑 Cadeira Giratória (5)                                 │    │   │
│  │  │ 💻 Computador Desktop (3)                                │    │   │
│  │  │ 🖥️ Monitor LCD (8)                                       │    │   │
│  │  └─────────────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│                                              ┌──────────────────────┐   │
│                                              │ 📷 Identificar       │   │
│                                              │    por Foto          │   │
│                                              └──────────────────────┘   │
│                                                      FAB               │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ Tap FAB
┌─────────────────────────────────────────────────────────────────────────┐
│                    AIIdentificationActivity                              │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    Camera Preview                                │   │
│  │                                                                  │   │
│  │                         📷                                       │   │
│  │                                                                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│                    ┌──────────────────────┐                            │
│                    │   Capturar Foto      │                            │
│                    └──────────────────────┘                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ Capture
┌─────────────────────────────────────────────────────────────────────────┐
│                    Análise ML Kit                                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    [Imagem Capturada]                            │   │
│  │                                                                  │   │
│  │              🔄 Analisando imagem...                             │   │
│  │                                                                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ Analysis Complete
┌─────────────────────────────────────────────────────────────────────────┐
│                    Resultados da IA                                      │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Labels Identificados:                                           │   │
│  │  ┌─────────────────────────────────────────────────────────┐    │   │
│  │  │ 🟢 Cadeira (92%)                                         │    │   │
│  │  │ 🟡 Móvel (68%)                                           │    │   │
│  │  │ 🟠 Escritório (55%)                                      │    │   │
│  │  └─────────────────────────────────────────────────────────┘    │   │
│  │                                                                  │   │
│  │  Descrições Sugeridas:                                          │   │
│  │  ┌─────────────────────────────────────────────────────────┐    │   │
│  │  │ 🤖 Cadeira Giratória (5 pendentes)                       │    │   │
│  │  │ 🤖 Cadeira Fixa (3 pendentes)                            │    │   │
│  │  │ 🤖 Cadeira Estofada (2 pendentes)                        │    │   │
│  │  └─────────────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌────────────────┐              ┌────────────────┐                    │
│  │  Nova Foto     │              │  Usar Sugestão │                    │
│  └────────────────┘              └────────────────┘                    │
└─────────────────────────────────────────────────────────────────────────┘
```

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                               │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  AIIdentificationActivity                                        │   │
│  │  AIIdentificationViewModel                                       │   │
│  │  AIIdentificationState (sealed class)                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           Domain Layer                                   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  AnalisarImagemUseCase                                           │   │
│  │  BuscarDescricoesCorrespondentesUseCase                          │   │
│  │  CalcularMatchingScoreUseCase                                    │   │
│  │  AIIdentificationRepository (interface)                          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            Data Layer                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  AIIdentificationRepositoryImpl                                  │   │
│  │  MLKitImageLabeler (wrapper)                                     │   │
│  │  LabelTranslator                                                 │   │
│  │  DescricaoMatchingEngine                                         │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Domain Layer

#### AIIdentificationRepository Interface

```kotlin
interface AIIdentificationRepository {
    suspend fun analyzeImage(bitmap: Bitmap): Result<List<AILabel>>
    suspend fun findMatchingDescriptions(labels: List<AILabel>): Result<List<DescricaoSugerida>>
    fun isMLKitAvailable(): Boolean
    fun isModelDownloaded(): Boolean
    suspend fun downloadModel(): Result<Unit>
}
```

#### Use Cases

```kotlin
class AnalisarImagemUseCase @Inject constructor(
    private val repository: AIIdentificationRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<List<AILabel>>
}

class BuscarDescricoesCorrespondentesUseCase @Inject constructor(
    private val repository: AIIdentificationRepository,
    private val patrimonioRepository: PatrimonioRepository
) {
    suspend operator fun invoke(labels: List<AILabel>): Result<List<DescricaoSugerida>>
}
```

### 2. Data Layer

#### MLKitImageLabeler

```kotlin
class MLKitImageLabeler @Inject constructor(
    private val context: Context
) {
    private var labeler: ImageLabeler? = null
    
    suspend fun analyze(bitmap: Bitmap, minConfidence: Float = 0.5f): List<AILabel>
    fun release()
    fun isAvailable(): Boolean
}
```

#### LabelTranslator

```kotlin
object LabelTranslator {
    private val translations = mapOf(
        "chair" to "Cadeira",
        "desk" to "Mesa",
        "computer" to "Computador",
        "monitor" to "Monitor",
        "keyboard" to "Teclado",
        "printer" to "Impressora",
        "cabinet" to "Armário",
        "shelf" to "Estante",
        // ... mais traduções
    )
    
    fun translate(englishLabel: String): String
}
```

#### DescricaoMatchingEngine

```kotlin
class DescricaoMatchingEngine @Inject constructor() {
    
    fun calculateMatchingScore(
        labels: List<AILabel>,
        descricao: String
    ): Float {
        var score = 0f
        
        for (label in labels) {
            // Exact match (highest weight)
            if (descricao.contains(label.translatedText, ignoreCase = true)) {
                score += label.confidence * 1.0f
            }
            // Partial match (medium weight)
            else if (hasPartialMatch(label.translatedText, descricao)) {
                score += label.confidence * 0.5f
            }
            // Category match (lower weight)
            else if (hasCategoryMatch(label.translatedText, descricao)) {
                score += label.confidence * 0.3f
            }
        }
        
        return score.coerceIn(0f, 1f)
    }
}
```

### 3. Presentation Layer

#### AIIdentificationState

```kotlin
sealed class AIIdentificationState {
    object Idle : AIIdentificationState()
    object CameraReady : AIIdentificationState()
    object Capturing : AIIdentificationState()
    data class Analyzing(val message: String = "Analisando imagem...") : AIIdentificationState()
    data class LabelsIdentified(
        val labels: List<AILabel>,
        val capturedImage: Bitmap
    ) : AIIdentificationState()
    data class SuggestionsReady(
        val labels: List<AILabel>,
        val suggestions: List<DescricaoSugerida>,
        val capturedImage: Bitmap
    ) : AIIdentificationState()
    data class Error(val message: String, val canRetry: Boolean = true) : AIIdentificationState()
    object ModelDownloading : AIIdentificationState()
}
```

#### AIIdentificationViewModel

```kotlin
@HiltViewModel
class AIIdentificationViewModel @Inject constructor(
    private val analisarImagemUseCase: AnalisarImagemUseCase,
    private val buscarDescricoesUseCase: BuscarDescricoesCorrespondentesUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<AIIdentificationState>(AIIdentificationState.Idle)
    val state: StateFlow<AIIdentificationState> = _state.asStateFlow()
    
    fun analyzeImage(bitmap: Bitmap)
    fun retryAnalysis()
    fun clearState()
}
```

## Data Models

### AILabel

```kotlin
data class AILabel(
    val originalText: String,      // Label original em inglês
    val translatedText: String,    // Label traduzido para português
    val confidence: Float,         // 0.0 a 1.0
    val confidenceLevel: ConfidenceLevel
) {
    enum class ConfidenceLevel {
        HIGH,    // 80-100%
        MEDIUM,  // 60-79%
        LOW      // 50-59%
    }
    
    companion object {
        fun fromMLKitLabel(label: ImageLabel): AILabel {
            val translated = LabelTranslator.translate(label.text)
            val level = when {
                label.confidence >= 0.8f -> ConfidenceLevel.HIGH
                label.confidence >= 0.6f -> ConfidenceLevel.MEDIUM
                else -> ConfidenceLevel.LOW
            }
            return AILabel(
                originalText = label.text,
                translatedText = translated,
                confidence = label.confidence,
                confidenceLevel = level
            )
        }
    }
}
```

### DescricaoSugerida

```kotlin
data class DescricaoSugerida(
    val descricao: String,
    val quantidade: Int,           // Quantidade de itens não coletados
    val matchingScore: Float,      // 0.0 a 1.0
    val categoria: String,
    val icone: String,
    val matchedLabels: List<String> // Labels que geraram o match
)
```

### AISettings

```kotlin
data class AISettings(
    val enabled: Boolean = true,
    val minConfidenceThreshold: Float = 0.5f,
    val maxSuggestions: Int = 10
)
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Offline Processing Capability

*For any* valid image bitmap, the ML Kit Image Labeling service SHALL process it and return labels without requiring an active internet connection (when model is cached).

**Validates: Requirements 1.3**

### Property 2: Label Confidence Filtering

*For any* set of labels returned by the analysis, ALL labels SHALL have confidence scores >= the configured minimum threshold (default 50%), AND at most 5 labels SHALL be displayed to the user.

**Validates: Requirements 1.4, 2.4**

### Property 3: Label Translation Consistency

*For any* English label in the translation dictionary, calling `LabelTranslator.translate()` SHALL return the corresponding Portuguese translation, AND for unknown labels, SHALL return the original English text.

**Validates: Requirements 2.6**

### Property 4: Matching Scope Completeness

*For any* set of AI labels, the matching algorithm SHALL compare against ALL unique descriptions of uncollected patrimônios in the local database, not a subset.

**Validates: Requirements 3.2**

### Property 5: Matching Score Calculation

*For any* label and description pair, the matching score SHALL be calculated considering: exact word matches (weight 1.0), partial word matches (weight 0.5), and category matches (weight 0.3), with the final score normalized to [0, 1].

**Validates: Requirements 3.3**

### Property 6: Results Ordering and Limit

*For any* set of matching descriptions, the results SHALL be ordered by descending matching score, AND the count SHALL be at most 10 items.

**Validates: Requirements 3.4**

### Property 7: Suggestion Count Display

*For any* suggested description displayed to the user, it SHALL include the count of uncollected items with that exact description.

**Validates: Requirements 3.6**

### Property 8: FAB Visibility Control

*For any* state where AI identification is disabled in settings OR the device doesn't support ML Kit, the AI identification FAB SHALL NOT be visible in DescricaoSelectionActivity.

**Validates: Requirements 5.4, 8.4**

### Property 9: Image Resize Before Analysis

*For any* image sent to ML Kit for analysis, its dimensions SHALL be at most 640x480 pixels (resized if larger while maintaining aspect ratio).

**Validates: Requirements 6.2**

### Property 10: Confidence Color Coding

*For any* displayed confidence score, the color indicator SHALL be: Green for 80-100%, Yellow for 60-79%, Orange for 50-59%.

**Validates: Requirements 7.2**

## Error Handling

### Error Types

```kotlin
sealed class AIIdentificationError : Exception() {
    object ModelNotDownloaded : AIIdentificationError()
    object CameraPermissionDenied : AIIdentificationError()
    object DeviceNotSupported : AIIdentificationError()
    object ImageTooLowQuality : AIIdentificationError()
    object AnalysisFailed : AIIdentificationError()
    object OfflineAndNoModel : AIIdentificationError()
    data class Unknown(override val message: String) : AIIdentificationError()
}
```

### Error Handling Strategy

| Error | User Message | Action |
|-------|--------------|--------|
| ModelNotDownloaded | "Modelo de IA não baixado. Deseja baixar agora?" | Show download dialog |
| CameraPermissionDenied | "Permissão de câmera necessária" | Show settings link |
| DeviceNotSupported | (FAB hidden) | N/A |
| ImageTooLowQuality | "Imagem muito escura ou borrada. Tente novamente." | Allow retry |
| AnalysisFailed | "Falha na análise. Tente novamente." | Allow retry |
| OfflineAndNoModel | "Sem conexão e modelo não disponível. Use busca manual." | Navigate to manual search |

## Testing Strategy

### Unit Tests

1. **LabelTranslator Tests**
   - Test known translations return correct Portuguese
   - Test unknown labels return original text
   - Test case insensitivity

2. **DescricaoMatchingEngine Tests**
   - Test exact match scoring
   - Test partial match scoring
   - Test category match scoring
   - Test score normalization

3. **AILabel Tests**
   - Test confidence level classification
   - Test fromMLKitLabel conversion

### Property-Based Tests

Each correctness property will be implemented as a property-based test using a PBT library (e.g., Kotest Property Testing).

**Configuration:**
- Minimum 100 iterations per property test
- Tag format: **Feature: identificacao-ia-patrimonio, Property {number}: {property_text}**

### Integration Tests

1. **ML Kit Integration**
   - Test image analysis with sample images
   - Test offline mode with cached model
   - Test model download flow

2. **End-to-End Flow**
   - Test complete flow from FAB tap to suggestion selection
   - Test filter application in DescricaoSelectionActivity

### UI Tests

1. **AIIdentificationActivity**
   - Test camera preview display
   - Test loading state display
   - Test results display
   - Test error state display

2. **DescricaoSelectionActivity Integration**
   - Test FAB visibility based on settings
   - Test filter application after AI analysis

## Dependencies

### Build Configuration (build.gradle)

```gradle
dependencies {
    // ML Kit Image Labeling
    implementation 'com.google.mlkit:image-labeling:17.0.7'
    
    // CameraX for camera preview
    implementation "androidx.camera:camera-camera2:1.3.0"
    implementation "androidx.camera:camera-lifecycle:1.3.0"
    implementation "androidx.camera:camera-view:1.3.0"
}
```

## File Structure

```
com.inventario.mobile/
├── domain/
│   ├── model/
│   │   ├── AILabel.kt
│   │   └── DescricaoSugerida.kt
│   ├── repository/
│   │   └── AIIdentificationRepository.kt
│   └── usecase/
│       ├── AnalisarImagemUseCase.kt
│       └── BuscarDescricoesCorrespondentesUseCase.kt
├── data/
│   ├── ai/
│   │   ├── MLKitImageLabeler.kt
│   │   ├── LabelTranslator.kt
│   │   └── DescricaoMatchingEngine.kt
│   └── repository/
│       └── AIIdentificationRepositoryImpl.kt
├── presentation/
│   ├── ai/
│   │   ├── AIIdentificationActivity.kt
│   │   ├── AIIdentificationViewModel.kt
│   │   ├── AIIdentificationState.kt
│   │   └── AILabelAdapter.kt
│   └── descricao/
│       └── DescricaoSelectionActivity.kt (modified)
└── di/
    └── AIModule.kt
```
