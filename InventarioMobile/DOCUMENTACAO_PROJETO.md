# Documentação do Projeto InventarioMobile

## Visão Geral
O InventarioMobile é um aplicativo Android desenvolvido para gerenciamento de inventário de patrimônio, permitindo coleta de dados através de QR codes, sincronização com servidor e gestão offline.

## Arquitetura do Projeto

### Estrutura de Pastas
```
app/src/main/java/com/inventario/mobile/
├── data/
│   ├── local/
│   │   ├── dao/           # Data Access Objects
│   │   └── database/      # Configuração Room Database
│   ├── remote/            # APIs e serviços remotos
│   └── repository/        # Implementação de repositórios
├── domain/
│   └── model/             # Entidades do banco de dados
├── presentation/
│   ├── main/              # MainActivity
│   ├── login/             # Tela de login
│   ├── scanner/           # Scanner QR Code
│   ├── sala/              # Seleção de salas
│   ├── dashboard/         # Dashboard principal
│   └── viewmodel/         # ViewModels
├── ui/
│   └── coleta/            # Tela de coleta
└── utils/                 # Utilitários
```

## Tecnologias Utilizadas

### Principais Dependências
- **Room Database**: 2.6.1 (Persistência local)
- **Retrofit**: 2.9.0 (Comunicação HTTP)
- **Kotlin Coroutines**: 1.7.3 (Programação assíncrona)
- **Hilt**: 2.48 (Injeção de dependência)
- **Work Manager**: 2.9.0 (Tarefas em background)
- **ZXing**: 4.3.0 (Scanner QR Code)
- **Glide**: 4.16.0 (Carregamento de imagens)

### Configurações do Projeto
- **Compile SDK**: 34
- **Min SDK**: 21
- **Target SDK**: 34
- **Kotlin Version**: 2.2.0
- **Gradle**: 8.13.0

## Entidades do Banco de Dados

### 1. Patrimonio
```kotlin
@Entity(tableName = "patrimonio")
data class Patrimonio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numeroPatrimonio: String,
    val descricao: String,
    val qrCode: String?,
    val setorId: Long,
    val salaId: Long,
    val dataAquisicao: Long = System.currentTimeMillis(),
    val valor: Double = 0.0,
    val estado: String = "BOM",
    val observacoes: String = "",
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    val servidorId: Long? = null
)
```

### 2. Coleta
```kotlin
@Entity(
    tableName = "coleta",
    foreignKeys = [
        ForeignKey(entity = Patrimonio::class, parentColumns = ["id"], childColumns = ["patrimonioId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"])
    ]
)
data class Coleta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patrimonioId: Long,
    val usuarioId: Long,
    val dataColeta: Long = System.currentTimeMillis(),
    val localizacaoAtual: String,
    val observacoes: String = "",
    val fotoPath: String? = null,
    val status: String = "COLETADO",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    val servidorId: Long? = null
)
```

### 3. Usuario
```kotlin
@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String,
    val senha: String,
    val ativo: Boolean = true,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    val servidorId: Long? = null
)
```

### 4. Setor
```kotlin
@Entity(tableName = "setor")
data class Setor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val descricao: String = "",
    val ativo: Boolean = true,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    val servidorId: Long? = null
)
```

### 5. Sala
```kotlin
@Entity(
    tableName = "sala",
    foreignKeys = [ForeignKey(entity = Setor::class, parentColumns = ["id"], childColumns = ["setorId"])]
)
data class Sala(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val setorId: Long,
    val descricao: String = "",
    val ativo: Boolean = true,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    val servidorId: Long? = null
)
```

### 6. Sincronizacao
```kotlin
@Entity(tableName = "sincronizacao")
data class Sincronizacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tabela: String,
    val registroId: Long,
    val operacao: String, // CREATE, UPDATE, DELETE
    val dados: String, // JSON dos dados
    val tentativas: Int = 0,
    val maxTentativas: Int = 3,
    val sucesso: Boolean = false,
    val erro: String? = null,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataSincronizacao: Long? = null,
    val dataUltimaTentativa: Long? = null
)
```

## Activities Principais

### 1. SplashActivity
- **Função**: Tela inicial do aplicativo
- **Localização**: `com.inventario.mobile.ui.splash.SplashActivity`

### 2. MainActivity
- **Função**: Tela principal com navegação bottom navigation
- **Localização**: `com.inventario.mobile.presentation.main.MainActivity`
- **Fragments**: DashboardFragment

### 3. LoginActivity
- **Função**: Autenticação do usuário
- **Localização**: `com.inventario.mobile.presentation.login.LoginActivity`

### 4. ScannerActivity
- **Função**: Scanner de QR codes
- **Localização**: `com.inventario.mobile.presentation.scanner.ScannerActivity`

### 5. ColetaActivity
- **Função**: Registro de coletas de patrimônio
- **Localização**: `com.inventario.mobile.ui.coleta.ColetaActivity`

### 6. SalaSelectionActivity
- **Função**: Seleção de salas
- **Localização**: `com.inventario.mobile.presentation.sala.SalaSelectionActivity`

## Configuração do Banco de Dados

### Room Database
```kotlin
@Database(
    entities = [
        Patrimonio::class,
        Setor::class,
        Sala::class,
        Usuario::class,
        Coleta::class,
        Sincronizacao::class
    ],
    version = 1,
    exportSchema = false
)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun setorDao(): SetorDao
    abstract fun salaDao(): SalaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun sincronizacaoDao(): SincronizacaoDao
}
```

## Permissões Necessárias
- `INTERNET`: Comunicação com servidor
- `CAMERA`: Scanner QR Code
- `READ_EXTERNAL_STORAGE`: Leitura de arquivos
- `WRITE_EXTERNAL_STORAGE`: Escrita de arquivos
- `ACCESS_FINE_LOCATION`: Localização precisa
- `ACCESS_COARSE_LOCATION`: Localização aproximada
- `FOREGROUND_SERVICE`: Serviços em foreground
- `POST_NOTIFICATIONS`: Notificações

## Problemas Identificados e Status Atual

### ✅ Problemas Resolvidos
1. **Conversores de Data**: Removidos os conversores `Date` para `Long` - todas as entidades agora usam `Long` timestamps
2. **Imports de Date**: Removidos todos os imports `java.util.Date` das entidades
3. **Configuração Room**: TypeConverters comentados temporariamente

### ⚠️ Problemas Pendentes
1. **Compilação KAPT**: Ainda há falhas na compilação relacionadas ao processamento de anotações
2. **Estrutura de Pastas**: Inconsistência entre `presentation` e `ui` packages
3. **Fragments Incompletos**: Alguns fragments referenciados não estão implementados
4. **Configurações de Build**: Possíveis conflitos de versões

### 🔧 Melhorias Sugeridas
1. **Padronização de Packages**: Unificar estrutura entre `presentation` e `ui`
2. **Implementação de ViewModels**: Completar ViewModels para todas as telas
3. **Testes**: Adicionar testes unitários e de integração
4. **Documentação de API**: Documentar endpoints do servidor
5. **Tratamento de Erros**: Melhorar tratamento de erros e feedback ao usuário

## Próximos Passos
1. Resolver problemas de compilação KAPT
2. Implementar fragments faltantes
3. Completar funcionalidades de sincronização
4. Adicionar testes
5. Otimizar performance