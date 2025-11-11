# Design Document - Correção de Erros de Compilação Android

## Overview

Este documento descreve a abordagem sistemática para corrigir todos os erros de compilação no aplicativo Android InventarioMobile. A estratégia é organizada em fases, priorizando correções que desbloqueiam outras correções subsequentes.

## Architecture

### Camadas Atuais

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  - Activities, Fragments, ViewModels                         │
│  - Observa estados, delega ações                             │
│  - PROBLEMAS: imports incorretos, métodos faltantes          │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                              │
│  - Models puros, Use Cases, Repository Interfaces            │
│  - PROBLEMAS: inconsistência entre Model/DTO/Entity          │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                                │
│  - Repositories, DAOs, Entities, DTOs, APIs                  │
│  - PROBLEMAS: mapeamentos incorretos, métodos faltantes      │
└─────────────────────────────────────────────────────────────┘
```

### Estratégia de Correção

**Abordagem Bottom-Up**: Corrigir da camada mais baixa (Data) para a mais alta (Presentation)

## Components and Interfaces

### 1. Data Access Objects (DAOs)

#### PatrimonioDao
```kotlin
interface PatrimonioDao {
    // Métodos de busca
    suspend fun buscarPorNumero(numero: String): PatrimonioEntity?
    suspend fun buscarPorId(id: Int): PatrimonioEntity?
    suspend fun getAllPatrimoniosList(): List<PatrimonioEntity>
    
    // Métodos de contagem
    suspend fun contarTodos(): Int
    suspend fun contarColetados(): Int
    suspend fun contarNaoColetados(): Int
    
    // Métodos de modificação
    suspend fun inserir(patrimonio: PatrimonioEntity)
    suspend fun inserirTodos(patrimonios: List<PatrimonioEntity>)
    suspend fun limparTodos()
}
```

#### SalaDao
```kotlin
interface SalaDao {
    // Métodos de busca
    suspend fun buscarTodas(): List<SalaEntity>
    suspend fun buscarPorId(id: Int): SalaEntity?
    
    // Métodos de contagem
    suspend fun contar(): Int
    
    // Métodos de modificação
    suspend fun inserir(sala: SalaEntity)
    suspend fun inserirTodas(salas: List<SalaEntity>)
    suspend fun limparTodas()
}
```

#### SincronizacaoDao (NOVO)
```kotlin
interface SincronizacaoDao {
    suspend fun buscarPendentes(): List<SincronizacaoEntity>
    suspend fun getUltimaSincronizacao(): SincronizacaoEntity?
    suspend fun inserir(sincronizacao: SincronizacaoEntity): Long
    suspend fun marcarComoSincronizado(id: Long)
    suspend fun limparTodas()
}
```

### 2. Entities (Room)

#### PatrimonioEntity
```kotlin
@Entity(tableName = "patrimonio")
data class PatrimonioEntity(
    @PrimaryKey val id: Int,
    val numero: String,
    val descricao: String,
    val idSala: Int?,
    val nomeSala: String?,
    val idResponsavel: Int?,
    val nomeResponsavel: String?,
    val status: String,
    val coletado: Boolean = false,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
```

#### SalaEntity
```kotlin
@Entity(tableName = "sala")
data class SalaEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val idSetor: Int?,
    val nomeSetor: String?,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
```

#### SincronizacaoEntity
```kotlin
@Entity(tableName = "sincronizacao")
data class SincronizacaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entidade: String,
    val entidadeId: Long,
    val operacao: String,
    val sincronizado: Boolean = false,
    val dataHora: Long,
    val erro: String? = null
)
```

### 3. DTOs (API)

#### MobilePatrimonioDto
```kotlin
data class MobilePatrimonioDto(
    val id: Long,
    val numero: String,
    val descricao: String,
    val idSala: Int?,
    val nomeSala: String?,
    val idResponsavel: Int?,
    val nomeResponsavel: String?,
    val status: String
)
```

#### SalaDto
```kotlin
data class SalaDto(
    val id: Int,
    val numeroSala: String?,
    val numero: String?,
    val descricao: String?,
    val idSetor: Int?,
    val setorId: Int?,
    val nomeSetor: String?
) {
    val nome: String get() = numeroSala ?: numero ?: descricao ?: "Sala $id"
    val setorIdFinal: Int get() = idSetor ?: setorId ?: 0
}
```

### 4. Mappers

#### PatrimonioMapper
```kotlin
object PatrimonioMapper {
    fun dtoToEntity(dto: MobilePatrimonioDto): PatrimonioEntity {
        return PatrimonioEntity(
            id = dto.id.toInt(),
            numero = dto.numero,
            descricao = dto.descricao,
            idSala = dto.idSala,
            nomeSala = dto.nomeSala,
            idResponsavel = dto.idResponsavel,
            nomeResponsavel = dto.nomeResponsavel,
            status = dto.status,
            coletado = false
        )
    }
    
    fun entityToModel(entity: PatrimonioEntity): Patrimonio {
        return Patrimonio(
            id = entity.id,
            numero = entity.numero,
            descricao = entity.descricao,
            idSala = entity.idSala,
            nomeSala = entity.nomeSala,
            idResponsavel = entity.idResponsavel,
            nomeResponsavel = entity.nomeResponsavel,
            status = entity.status
        )
    }
}
```

#### SalaMapper
```kotlin
object SalaMapper {
    fun dtoToEntity(dto: SalaDto): SalaEntity {
        return SalaEntity(
            id = dto.id,
            nome = dto.nome,
            idSetor = dto.setorIdFinal,
            nomeSetor = dto.nomeSetor
        )
    }
    
    fun entityToModel(entity: SalaEntity): Sala {
        return Sala(
            id = entity.id,
            nome = entity.nome,
            idSetor = entity.idSetor,
            nomeSetor = entity.nomeSetor
        )
    }
}
```

### 5. Repositories

#### SyncRepository
```kotlin
class SyncRepository(
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao,
    private val sincronizacaoDao: SincronizacaoDao,
    private val apiService: ApiService
) {
    suspend fun forceSyncFromServer(): Result<SyncResult> {
        // 1. Buscar dados da API
        val salasResponse = apiService.getSalasWithResponse()
        val patrimoniosResponse = apiService.getPatrimonios()
        
        // 2. Mapear DTOs para Entities
        val salasEntities = salasResponse.body()?.data?.map { 
            SalaMapper.dtoToEntity(it) 
        } ?: emptyList()
        
        val patrimoniosEntities = patrimoniosResponse.body()?.data?.map { 
            PatrimonioMapper.dtoToEntity(it) 
        } ?: emptyList()
        
        // 3. Limpar e inserir no banco local
        salaDao.limparTodas()
        salaDao.inserirTodas(salasEntities)
        
        patrimonioDao.limparTodos()
        patrimonioDao.inserirTodos(patrimoniosEntities)
        
        // 4. Registrar sincronização
        sincronizacaoDao.inserir(
            SincronizacaoEntity(
                entidade = "SYNC_FULL",
                entidadeId = 0,
                operacao = "DOWNLOAD",
                sincronizado = true,
                dataHora = System.currentTimeMillis()
            )
        )
        
        return Result.success(SyncResult(...))
    }
}
```

### 6. Strategies

#### LocalDataSourceStrategy
```kotlin
class LocalDataSourceStrategy(
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao
) : DataSourceStrategy {
    
    override suspend fun getPatrimonios(): List<Patrimonio> {
        val entities = patrimonioDao.getAllPatrimoniosList()
        return entities.map { PatrimonioMapper.entityToModel(it) }
    }
    
    override suspend fun getPatrimonioByNumero(numero: String): Patrimonio? {
        val entity = patrimonioDao.buscarPorNumero(numero)
        return entity?.let { PatrimonioMapper.entityToModel(it) }
    }
    
    override suspend fun getSalas(): List<Sala> {
        val entities = salaDao.buscarTodas()
        return entities.map { SalaMapper.entityToModel(it) }
    }
}
```

## Data Models

### Hierarquia de Modelos

```
API (Backend)
    ↓ (JSON)
MobilePatrimonioDto / SalaDto
    ↓ (Mapper.dtoToEntity)
PatrimonioEntity / SalaEntity (Room)
    ↓ (Mapper.entityToModel)
Patrimonio / Sala (Domain Model)
    ↓ (usado por)
Use Cases / ViewModels
```

### Fluxo de Dados

**Download (Sincronização)**
```
API → DTO → Entity → Banco Local
```

**Leitura (Offline-First)**
```
Banco Local → Entity → Model → ViewModel → UI
```

**Upload (Coleta)**
```
UI → ViewModel → Use Case → Repository → DTO → API
```

## Error Handling

### Estratégia de Tratamento de Erros

1. **Erros de Rede**: Usar fonte de dados local como fallback
2. **Erros de Banco**: Logar e retornar Result.failure()
3. **Erros de Mapeamento**: Validar dados antes de mapear
4. **Erros de Compilação**: Corrigir sistematicamente por categoria

### Categorias de Erros

#### Categoria 1: Unresolved Reference (Prioridade ALTA)
- Métodos de DAO não encontrados
- Classes não encontradas
- Imports incorretos

#### Categoria 2: Type Mismatch (Prioridade MÉDIA)
- Conversões Long ↔ Int
- Result<T> vs Result<List<T>>
- Nullable vs Non-nullable

#### Categoria 3: Missing Parameters (Prioridade MÉDIA)
- Construtores com parâmetros faltantes
- Campos obrigatórios não fornecidos

#### Categoria 4: Cannot Find Parameter (Prioridade BAIXA)
- Named parameters incorretos
- Campos renomeados

## Testing Strategy

### Validação de Correções

1. **Compilação**: `gradlew compileDebugKotlin`
2. **Build**: `gradlew assembleDebug`
3. **Testes Unitários**: Criar testes para mappers
4. **Testes de Integração**: Testar fluxo completo de sincronização

### Checklist de Validação

- [ ] Projeto compila sem erros
- [ ] Todos os DAOs têm métodos corretos
- [ ] Todos os mapeamentos estão consistentes
- [ ] Todas as classes utilitárias existem
- [ ] Todos os imports estão corretos
- [ ] Arquitetura Clean está preservada
- [ ] Não há código duplicado
- [ ] Documentação está atualizada

## Implementation Notes

### Ordem de Implementação

**Fase 1: Fundação (Data Layer)**
1. Corrigir DAOs
2. Alinhar Entities
3. Criar Mappers

**Fase 2: Integração (Repository Layer)**
4. Corrigir SyncRepository
5. Corrigir Strategies
6. Corrigir outros Repositories

**Fase 3: Apresentação (Presentation Layer)**
7. Corrigir ViewModels
8. Corrigir Activities
9. Corrigir Utilitários

**Fase 4: Validação**
10. Compilar e testar
11. Documentar mudanças
12. Atualizar steering rules

### Decisões Arquiteturais

1. **Usar Mappers Explícitos**: Evitar conversões inline, criar classes Mapper dedicadas
2. **Manter Clean Architecture**: Não violar dependências entre camadas
3. **Offline-First**: Priorizar fonte de dados local
4. **Fail-Safe**: Usar try-catch e Result<T> para operações que podem falhar
5. **Logging**: Adicionar logs detalhados para debug

### Limitações Conhecidas

1. **ApiService.create()**: Método não existe, precisa usar injeção de dependência ou factory
2. **DeviceInfoHelper**: Pode estar em pacote incorreto ou não implementado
3. **Múltiplos DTOs**: Alguns conceitos têm múltiplas representações (MobilePatrimonioDto vs PatrimonioDto)
4. **Campos Opcionais**: Alguns campos são nullable em DTO mas required em Entity
