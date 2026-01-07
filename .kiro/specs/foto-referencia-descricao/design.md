# Design Document: Foto de Referência por Descrição

## Overview

Esta feature implementa um sistema de fotos de referência vinculadas a descrições de patrimônios, permitindo que coletores identifiquem visualmente os itens durante o inventário. O sistema é composto por:

1. **Módulo Desktop (Java Swing)**: Interface para cadastro e gerenciamento de fotos
2. **Camada de Persistência**: Armazenamento de fotos e metadados no PostgreSQL
3. **API Mobile (Spring Boot)**: Endpoints para sincronização de fotos
4. **Módulo Android (Kotlin)**: Exibição e cache local de fotos

A arquitetura segue o padrão offline-first, onde as fotos são sincronizadas e cacheadas localmente no dispositivo móvel para uso sem conexão.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           DESKTOP (Java Swing)                          │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────┐ │
│  │ FotoReferenciaFrame │  │ FotoReferenciaService│  │ ImageProcessor  │ │
│  │   (UI Cadastro)     │──│   (Business Logic)   │──│ (Resize/Compress)│ │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────┘ │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         BANCO DE DADOS (PostgreSQL)                      │
│  ┌─────────────────────────────────────────────────────────────────────┐│
│  │ tabela_foto_referencia                                               ││
│  │ - id, descricao_normalizada, imagem_blob, hash_imagem               ││
│  │ - data_cadastro, data_atualizacao, ativo, tamanho_bytes             ││
│  └─────────────────────────────────────────────────────────────────────┘│
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        API MOBILE (Spring Boot)                          │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │MobileFotoReferenciaCtrl │──│ MobileFotoReferenciaService         │  │
│  │  GET /fotos-referencia  │  │ - buscarFotosAtualizadas(timestamp) │  │
│  └─────────────────────────┘  └─────────────────────────────────────┘  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ANDROID (Kotlin)                               │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────┐ │
│  │ FotoReferenciaRepo  │──│ FotoReferenciaDao   │──│ Room Database   │ │
│  │ (Sync + Cache)      │  │ (Local Storage)     │  │ (SQLite)        │ │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────┘ │
│                                     │                                    │
│  ┌─────────────────────────────────┴────────────────────────────────┐  │
│  │                    UI Components                                   │  │
│  │  PatrimonioAdapter → exibe thumbnail ao lado de cada item         │  │
│  │  PatrimonioDetailFragment → exibe foto ampliada                   │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### Desktop Components

#### FotoReferenciaFrame (View)
```java
public class FotoReferenciaFrame extends JFrame {
    // Lista de descrições únicas com contagem de patrimônios
    private JTable tabelaDescricoes;
    // Preview da foto selecionada
    private JLabel labelPreviewFoto;
    // Botões de ação
    private JButton btnSelecionarImagem;
    private JButton btnSalvar;
    private JButton btnExcluir;
    // Campo de busca
    private JTextField campoBusca;
    
    // Métodos principais
    void carregarDescricoes();
    void filtrarDescricoes(String texto);
    void selecionarImagem();
    void salvarFotoReferencia();
    void excluirFotoReferencia();
}
```

#### FotoReferenciaService (Business Logic)
```java
public class FotoReferenciaService {
    // Buscar descrições únicas normalizadas
    List<DescricaoResumo> buscarDescricoesUnicas();
    
    // Salvar foto de referência
    void salvarFotoReferencia(String descricaoNormalizada, byte[] imagemOriginal);
    
    // Buscar foto por descrição
    Optional<FotoReferencia> buscarPorDescricao(String descricao);
    
    // Excluir foto (soft delete)
    void excluirFotoReferencia(int id);
    
    // Normalizar descrição
    String normalizarDescricao(String descricaoOriginal);
    
    // Estatísticas
    EstatisticasFoto obterEstatisticas();
}
```

#### ImageProcessor (Utility)
```java
public class ImageProcessor {
    // Redimensionar para thumbnail
    BufferedImage redimensionar(BufferedImage original, int maxWidth, int maxHeight);
    
    // Comprimir para JPEG
    byte[] comprimirJpeg(BufferedImage imagem, float qualidade);
    
    // Processar imagem completa (resize + compress)
    byte[] processarParaThumbnail(byte[] imagemOriginal);
    
    // Validar imagem
    boolean isImagemValida(byte[] dados);
    
    // Calcular hash
    String calcularHash(byte[] dados);
}
```

### API Components

#### MobileFotoReferenciaController
```java
@RestController
@RequestMapping("/api/mobile/fotos-referencia")
public class MobileFotoReferenciaController {
    
    @GetMapping
    ResponseEntity<ApiResponse<List<FotoReferenciaDTO>>> buscarFotos(
        @RequestParam(required = false) Long ultimaSincronizacao,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "50") int tamanho
    );
    
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<FotoReferenciaDTO>> buscarPorId(@PathVariable int id);
    
    @GetMapping("/descricao")
    ResponseEntity<ApiResponse<FotoReferenciaDTO>> buscarPorDescricao(
        @RequestParam String descricao
    );
}
```

#### FotoReferenciaDTO
```java
public class FotoReferenciaDTO {
    private int id;
    private String descricaoNormalizada;
    private String imagemBase64;  // Thumbnail em Base64
    private String hashImagem;
    private LocalDateTime dataAtualizacao;
    private boolean ativo;
}
```

### Android Components

#### FotoReferenciaEntity (Room)
```kotlin
@Entity(tableName = "foto_referencia")
data class FotoReferenciaEntity(
    @PrimaryKey val id: Int,
    val descricaoNormalizada: String,
    val imagemBlob: ByteArray,
    val hashImagem: String,
    val dataAtualizacao: Long,
    val ativo: Boolean
)
```

#### FotoReferenciaDao
```kotlin
@Dao
interface FotoReferenciaDao {
    @Query("SELECT * FROM foto_referencia WHERE ativo = 1")
    fun buscarTodas(): Flow<List<FotoReferenciaEntity>>
    
    @Query("SELECT * FROM foto_referencia WHERE descricaoNormalizada LIKE :descricao AND ativo = 1")
    suspend fun buscarPorDescricao(descricao: String): FotoReferenciaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(fotos: List<FotoReferenciaEntity>)
    
    @Query("UPDATE foto_referencia SET ativo = 0 WHERE id = :id")
    suspend fun desativar(id: Int)
    
    @Query("SELECT SUM(LENGTH(imagemBlob)) FROM foto_referencia WHERE ativo = 1")
    suspend fun calcularTamanhoTotal(): Long?
}
```

#### FotoReferenciaRepository
```kotlin
class FotoReferenciaRepositoryImpl(
    private val dao: FotoReferenciaDao,
    private val api: FotoReferenciaApi,
    private val prefs: PreferencesManager
) : FotoReferenciaRepository {
    
    suspend fun sincronizar(): Result<Int> {
        val ultimaSync = prefs.getUltimaSyncFotos()
        val fotos = api.buscarFotos(ultimaSync)
        dao.inserirTodas(fotos.map { it.toEntity() })
        prefs.setUltimaSyncFotos(System.currentTimeMillis())
        return Result.success(fotos.size)
    }
    
    suspend fun buscarPorDescricao(descricao: String): FotoReferencia? {
        val normalizada = normalizarDescricao(descricao)
        return dao.buscarPorDescricao("%$normalizada%")?.toDomain()
    }
}
```

## Data Models

### PostgreSQL Schema

```sql
CREATE TABLE tabela_foto_referencia (
    id SERIAL PRIMARY KEY,
    descricao_normalizada VARCHAR(500) NOT NULL UNIQUE,
    imagem_blob BYTEA NOT NULL,
    hash_imagem VARCHAR(64) NOT NULL,
    tamanho_bytes INTEGER NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    usuario_cadastro VARCHAR(100),
    
    CONSTRAINT chk_tamanho CHECK (tamanho_bytes <= 51200)  -- 50KB max
);

CREATE INDEX idx_foto_ref_descricao ON tabela_foto_referencia(descricao_normalizada);
CREATE INDEX idx_foto_ref_atualizacao ON tabela_foto_referencia(data_atualizacao);
CREATE INDEX idx_foto_ref_ativo ON tabela_foto_referencia(ativo);
```

### Domain Models

```java
// Java - Desktop/Backend
public class FotoReferencia {
    private int id;
    private String descricaoNormalizada;
    private byte[] imagemBlob;
    private String hashImagem;
    private int tamanhoBytes;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private boolean ativo;
    private String usuarioCadastro;
}

public class DescricaoResumo {
    private String descricaoNormalizada;
    private int quantidadePatrimonios;
    private boolean possuiFoto;
}
```

```kotlin
// Kotlin - Android
data class FotoReferencia(
    val id: Int,
    val descricaoNormalizada: String,
    val imagemBitmap: Bitmap?,
    val hashImagem: String,
    val dataAtualizacao: Long
)
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Validação de Entrada de Imagem

*For any* arquivo submetido para upload, se o arquivo não for uma imagem válida (JPG ou PNG) ou exceder 2MB, o sistema deve rejeitar o upload e retornar erro.

**Validates: Requirements 1.2, 1.6**

### Property 2: Processamento de Thumbnail

*For any* imagem válida processada pelo sistema, o thumbnail resultante deve ter dimensões máximas de 200x200 pixels e tamanho máximo de 50KB.

**Validates: Requirements 1.3, 2.4**

### Property 3: Completude de Metadados

*For any* foto de referência salva no sistema, todos os metadados obrigatórios devem estar presentes: descrição normalizada, hash da imagem, data de cadastro, tamanho em bytes.

**Validates: Requirements 2.2, 4.4**

### Property 4: Normalização de Descrições

*For any* descrição de patrimônio, a normalização deve remover números de patrimônio ANAC (padrão "PATRIMÔNIO ANAC XXXXXXX"), números de série, e preservar a descrição base do item.

**Validates: Requirements 3.2**

### Property 5: Filtro de Busca

*For any* texto de busca e lista de descrições, o resultado filtrado deve conter apenas descrições que incluem o texto buscado (case-insensitive).

**Validates: Requirements 1.7**

### Property 6: Paginação da API

*For any* requisição à API de fotos de referência, o número de fotos retornadas deve ser menor ou igual ao tamanho de página solicitado (máximo 50).

**Validates: Requirements 4.2**

### Property 7: Delta Sync

*For any* timestamp de última sincronização, a API deve retornar apenas fotos com data de atualização posterior ao timestamp fornecido.

**Validates: Requirements 4.3**

## Error Handling

### Desktop

| Erro | Causa | Tratamento |
|------|-------|------------|
| `ImagemInvalidaException` | Arquivo não é JPG/PNG | Exibir mensagem e rejeitar upload |
| `TamanhoExcedidoException` | Arquivo > 2MB | Exibir mensagem com limite permitido |
| `ProcessamentoException` | Falha ao redimensionar | Log de erro e mensagem genérica |
| `PersistenciaException` | Falha ao salvar no banco | Rollback e mensagem de erro |

### API Mobile

| HTTP Status | Causa | Response |
|-------------|-------|----------|
| 400 | Parâmetros inválidos | `{"success": false, "message": "Parâmetro inválido: ..."}` |
| 404 | Foto não encontrada | `{"success": false, "message": "Foto não encontrada"}` |
| 500 | Erro interno | `{"success": false, "message": "Erro interno do servidor"}` |

### Android

| Erro | Causa | Tratamento |
|------|-------|------------|
| `NetworkException` | Sem conexão | Usar cache local, agendar retry |
| `StorageException` | Disco cheio | Limpar fotos antigas, notificar usuário |
| `ParseException` | Resposta inválida | Log de erro, ignorar foto problemática |

## Testing Strategy

### Unit Tests

**Desktop:**
- `ImageProcessorTest`: Testar redimensionamento e compressão
- `FotoReferenciaServiceTest`: Testar normalização e validações
- `DescricaoNormalizadorTest`: Testar padrões de normalização

**API:**
- `MobileFotoReferenciaServiceTest`: Testar busca e paginação
- `FotoReferenciaControllerTest`: Testar endpoints REST

**Android:**
- `FotoReferenciaRepositoryTest`: Testar sincronização e cache
- `DescricaoNormalizadorTest`: Testar normalização no cliente

### Property-Based Tests

Cada propriedade de corretude será implementada como um teste baseado em propriedades usando:
- **Java**: jqwik ou QuickTheories
- **Kotlin**: Kotest Property Testing

Configuração mínima: 100 iterações por propriedade.

**Exemplo de teste para Property 2:**
```java
@Property(tries = 100)
void thumbnailDeveTerDimensoesCorretas(@ForAll @ImageData byte[] imagemValida) {
    byte[] thumbnail = imageProcessor.processarParaThumbnail(imagemValida);
    BufferedImage resultado = ImageIO.read(new ByteArrayInputStream(thumbnail));
    
    assertThat(resultado.getWidth()).isLessThanOrEqualTo(200);
    assertThat(resultado.getHeight()).isLessThanOrEqualTo(200);
    assertThat(thumbnail.length).isLessThanOrEqualTo(51200); // 50KB
}
```

### Integration Tests

- Testar fluxo completo: upload → processamento → persistência → API → sincronização Android
- Testar delta sync com diferentes timestamps
- Testar comportamento offline no Android
