# Realidade Aumentada com QR Code - Sistema de Inventário

## Visão Geral

Implementação de Realidade Aumentada (AR) usando QR Codes para visualização de informações de patrimônios de forma imersiva e interativa.

## Casos de Uso

### 1. Visualização de Informações 3D
- Escanear QR Code do patrimônio
- Ver modelo 3D flutuando sobre o objeto real
- Informações detalhadas em AR

### 2. Navegação Indoor
- QR Codes em pontos estratégicos
- Setas AR indicando localização de patrimônios
- Mapa AR do campus

### 3. Manutenção Assistida
- Instruções de manutenção em AR
- Destacar componentes específicos
- Histórico visual do patrimônio

### 4. Inventário Gamificado
- Coletar patrimônios em AR
- Badges e conquistas
- Ranking de coletores

## Arquiteturas Possíveis

### Arquitetura 1: AR Web (WebXR + AR.js)

```
┌─────────────┐
│  QR Code    │
│  Patrimônio │
│  12345      │
└──────┬──────┘
       │ Escaneia
       ▼
┌─────────────────────────────────┐
│  App Mobile (Android)           │
│  - Leitor QR Code               │
│  - Abre navegador               │
└──────┬──────────────────────────┘
       │ URL: https://inventario.ifmt.edu.br/ar/12345
       ▼
┌─────────────────────────────────┐
│  Servidor Web                   │
│  - Página HTML com AR.js        │
│  - Dados do patrimônio (JSON)   │
│  - Modelo 3D (opcional)         │
└──────┬──────────────────────────┘
       │ Carrega
       ▼
┌─────────────────────────────────┐
│  Navegador com AR               │
│  - AR.js / WebXR                │
│  - Renderiza 3D sobre câmera    │
│  - Exibe informações            │
└─────────────────────────────────┘
```

**Vantagens:**
- ✅ Não precisa atualizar app
- ✅ Funciona em iOS e Android
- ✅ Fácil de manter
- ✅ Baixo custo

**Desvantagens:**
- ❌ Performance inferior ao nativo
- ❌ Recursos AR limitados
- ❌ Precisa de conexão internet

### Arquitetura 2: AR Nativo (ARCore/ARKit)

```
┌─────────────┐
│  QR Code    │
│  Patrimônio │
│  12345      │
└──────┬──────┘
       │ Escaneia
       ▼
┌─────────────────────────────────┐
│  App Mobile (Android)           │
│  - Leitor QR Code integrado     │
│  - ARCore nativo                │
│  - Renderização 3D              │
└──────┬──────────────────────────┘
       │ Busca dados
       ▼
┌─────────────────────────────────┐
│  API REST                       │
│  GET /api/patrimonio/12345/ar   │
│  - Dados do patrimônio          │
│  - URL do modelo 3D             │
│  - Configurações AR             │
└─────────────────────────────────┘
```

**Vantagens:**
- ✅ Melhor performance
- ✅ Mais recursos AR
- ✅ Funciona offline (cache)
- ✅ Integração profunda

**Desvantagens:**
- ❌ Precisa desenvolver para iOS também
- ❌ Mais complexo
- ❌ Maior custo de desenvolvimento

### Arquitetura 3: Híbrida (Recomendada)

```
QR Code → App verifica suporte AR
           │
           ├─ Tem ARCore? → AR Nativo
           │
           └─ Não tem? → AR Web (fallback)
```

## Implementação Prática

### Fase 1: AR Web Básico (MVP)

#### 1.1 Estrutura de Arquivos
```
src/main/resources/static/ar/
├── index.html              # Página AR principal
├── ar-viewer.js           # Lógica AR
├── styles.css             # Estilos
└── models/                # Modelos 3D (opcional)
    └── generic-asset.glb
```

#### 1.2 Endpoint REST
```java
@RestController
@RequestMapping("/api/ar")
public class ARController {
    
    @GetMapping("/patrimonio/{numero}")
    public ResponseEntity<ARPatrimonioDTO> getARData(@PathVariable String numero) {
        // Retorna dados para AR
    }
}
```

#### 1.3 Página AR (HTML + AR.js)
```html
<!DOCTYPE html>
<html>
<head>
    <script src="https://aframe.io/releases/1.4.0/aframe.min.js"></script>
    <script src="https://cdn.jsdelivr.net/gh/AR-js-org/AR.js/aframe/build/aframe-ar.js"></script>
</head>
<body style="margin: 0; overflow: hidden;">
    <a-scene embedded arjs>
        <!-- Modelo 3D -->
        <a-box position="0 0.5 0" material="color: blue;"></a-box>
        
        <!-- Texto com informações -->
        <a-text value="Patrimônio: 12345" position="0 1 0" scale="0.5 0.5 0.5"></a-text>
        
        <!-- Câmera -->
        <a-entity camera></a-entity>
    </a-scene>
</body>
</html>
```

### Fase 2: AR Nativo (Android)

#### 2.1 Adicionar Dependências (build.gradle)
```gradle
dependencies {
    // ARCore
    implementation 'com.google.ar:core:1.40.0'
    
    // Sceneform (renderização 3D)
    implementation 'com.google.ar.sceneform:core:1.17.1'
    implementation 'com.google.ar.sceneform.ux:sceneform-ux:1.17.1'
}
```

#### 2.2 Activity AR
```kotlin
class ARPatrimonioActivity : AppCompatActivity() {
    
    private lateinit var arFragment: ArFragment
    private var patrimonioNumero: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_patrimonio)
        
        // Obter número do patrimônio do QR Code
        patrimonioNumero = intent.getStringExtra("PATRIMONIO_NUMERO")
        
        // Configurar AR
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        
        // Carregar dados do patrimônio
        carregarDadosPatrimonio()
        
        // Detectar plano e posicionar objeto
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            posicionarObjetoAR(hitResult)
        }
    }
    
    private fun carregarDadosPatrimonio() {
        // Buscar dados da API
        val api = RetrofitClient.getPatrimonioApi()
        api.getPatrimonioAR(patrimonioNumero!!).enqueue(object : Callback<ARPatrimonioDTO> {
            override fun onResponse(call: Call<ARPatrimonioDTO>, response: Response<ARPatrimonioDTO>) {
                if (response.isSuccessful) {
                    val dados = response.body()
                    // Preparar modelo 3D
                }
            }
            
            override fun onFailure(call: Call<ARPatrimonioDTO>, t: Throwable) {
                Toast.makeText(this@ARPatrimonioActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun posicionarObjetoAR(hitResult: HitResult) {
        // Criar âncora AR
        val anchor = hitResult.createAnchor()
        
        // Criar nó com modelo 3D
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)
        
        // Adicionar modelo 3D
        val modelNode = TransformableNode(arFragment.transformationSystem)
        modelNode.setParent(anchorNode)
        modelNode.renderable = modelRenderable
        modelNode.select()
        
        // Adicionar card de informações
        adicionarCardInformacoes(anchorNode)
    }
}
```

### Fase 3: Recursos Avançados

#### 3.1 Image Tracking (QR Code como Marcador)
```kotlin
// Configurar banco de imagens
val config = Config(session)
val imageDatabase = AugmentedImageDatabase(session)

// Adicionar QR Code como imagem rastreável
val qrCodeBitmap = loadQRCodeImage()
imageDatabase.addImage("patrimonio_12345", qrCodeBitmap)

config.augmentedImageDatabase = imageDatabase
session.configure(config)

// Detectar quando QR Code é encontrado
frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { image ->
    if (image.trackingState == TrackingState.TRACKING) {
        // QR Code detectado! Posicionar objeto 3D
        posicionarObjetoNoQRCode(image)
    }
}
```

#### 3.2 Navegação AR
```kotlin
// Desenhar caminho AR até o patrimônio
fun desenharCaminhoAR(origem: Pose, destino: Pose) {
    val pontos = calcularCaminho(origem, destino)
    
    pontos.forEach { ponto ->
        // Criar seta 3D
        val setaNode = Node()
        setaNode.renderable = setaRenderable
        setaNode.worldPosition = Vector3(ponto.x, 0.1f, ponto.z)
        
        // Adicionar à cena
        arFragment.arSceneView.scene.addChild(setaNode)
    }
}
```

## Modelos de Dados

### ARPatrimonioDTO
```java
public class ARPatrimonioDTO {
    private String numero;
    private String descricao;
    private String localizacao;
    private String modelo3dUrl;
    private String texturaUrl;
    private ARConfigDTO config;
    private List<ARInfoCardDTO> infoCards;
}

public class ARConfigDTO {
    private float escala;
    private float rotacao;
    private String animacao;
    private boolean permitirInteracao;
}

public class ARInfoCardDTO {
    private String titulo;
    private String conteudo;
    private String icone;
    private String cor;
}
```

## Exemplos de Uso

### Exemplo 1: Card de Informações Simples
```
┌─────────────────────────┐
│  📦 Patrimônio 12345    │
│  ─────────────────────  │
│  Computador Desktop     │
│  Dell OptiPlex 7090     │
│  ─────────────────────  │
│  📍 Sala 101 - TI       │
│  👤 João Silva          │
│  📅 Adquirido: 15/05/24 │
│  💰 R$ 3.500,00         │
└─────────────────────────┘
```

### Exemplo 2: Modelo 3D Interativo
```
     ╔═══════════╗
     ║  Monitor  ║
     ╚═══════════╝
          │
     ┌────┴────┐
     │   CPU   │  ← Rotacionar com gestos
     │  [Dell] │  ← Zoom com pinça
     └─────────┘  ← Tocar para ver detalhes
```

### Exemplo 3: Histórico Visual
```
Timeline AR:
━━━━━━━━━━━━━━━━━━━━━━━━━━
2024 ●────────────────────● Hoje
     │                    │
     ├─ Aquisição         ├─ Última manutenção
     ├─ Instalação        └─ Próxima revisão
     └─ Primeira coleta
```

## Requisitos Técnicos

### Servidor
- Spring Boot 3.2+
- Endpoint REST para dados AR
- Armazenamento de modelos 3D
- CDN para assets (opcional)

### Cliente (Android)
- Android 7.0+ (API 24+)
- ARCore compatible device
- Câmera com permissão
- 2GB RAM mínimo

### Navegador (AR Web)
- Chrome 79+ / Safari 13+
- WebXR ou WebGL support
- HTTPS obrigatório
- Acesso à câmera

## Roadmap de Implementação

### Sprint 1: Fundação (2 semanas)
- [ ] Criar endpoint REST para dados AR
- [ ] Implementar página AR web básica
- [ ] Testar com QR Codes existentes
- [ ] Documentar API

### Sprint 2: AR Nativo (3 semanas)
- [ ] Adicionar ARCore ao app Android
- [ ] Implementar detecção de planos
- [ ] Criar visualização 3D básica
- [ ] Integrar com API REST

### Sprint 3: Conteúdo (2 semanas)
- [ ] Criar modelos 3D genéricos
- [ ] Implementar cards de informação
- [ ] Adicionar animações
- [ ] Testes de usabilidade

### Sprint 4: Recursos Avançados (3 semanas)
- [ ] Image tracking com QR Code
- [ ] Navegação AR
- [ ] Modo offline
- [ ] Otimizações de performance

## Custos Estimados

| Item | Custo |
|------|-------|
| Desenvolvimento AR Web | 40h |
| Desenvolvimento AR Nativo | 80h |
| Modelos 3D (10 tipos) | R$ 5.000 |
| Testes e QA | 20h |
| Documentação | 10h |
| **Total** | **150h + R$ 5.000** |

## Referências

- **AR.js:** https://ar-js-org.github.io/AR.js-Docs/
- **ARCore:** https://developers.google.com/ar
- **WebXR:** https://immersiveweb.dev/
- **A-Frame:** https://aframe.io/
- **Sceneform:** https://github.com/google-ar/sceneform-android-sdk

## Conclusão

A implementação de AR com QR Codes é viável e pode agregar muito valor ao sistema de inventário. Recomendo começar com **AR Web** para validar o conceito e depois evoluir para **AR Nativo** conforme a demanda.

**Próximo passo sugerido:** Criar um protótipo AR Web para demonstração.
