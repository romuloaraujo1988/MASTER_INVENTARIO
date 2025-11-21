# 📋 Implementação: Filtro por Sala na Visualização de Coletas

## 🎯 Objetivo

Permitir que a coordenadora visualize patrimônios **por sala**, mostrando:
- Sala cadastrada (original)
- Sala onde foi coletado (se diferente)
- Status (coletado/não coletado)
- Divergências de localização

---

## 📱 Funcionalidade Desejada

### Tela: Visualização de Coletas

**Filtros atuais:**
- ✅ Por usuário (Todas / Minhas coletas)
- ✅ Por status (Todos / Sincronizados / Pendentes)

**Novo filtro:**
- ➕ **Por sala** (Todas as salas / Sala específica)

### Exemplo de Uso:

```
Coordenadora quer ver:
1. Seleciona "Sala 101 - Laboratório"
2. Sistema mostra:
   - Patrimônios cadastrados na Sala 101
   - Se coletado: mostra onde foi encontrado
   - Se diferente: destaca divergência
```

---

## 🏗️ Arquitetura da Implementação

### 1. **Backend** (Java) ✅ JÁ EXISTE

Endpoint já disponível:
```
GET /api/mobile/coletas
```

Retorna coletas com:
- `localizacao_atual` (sala cadastrada)
- `localizacao_encontrada` (sala real)
- `divergencia` (boolean)

### 2. **Android - Camada de Dados**

#### 2.1. Atualizar `ColetaEntity` (Room)

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/entity/ColetaEntity.kt

@Entity(tableName = "coleta")
data class ColetaEntity(
    @PrimaryKey val id: Int,
    val idInventario: Int,
    val idPatrimonio: Int,
    val numeroPatrimonio: String,
    val descricaoPatrimonio: String,
    val dataColeta: Long,
    val statusColeta: String,
    val observacao: String?,
    
    // Campos de localização
    val localizacaoAtual: String?,        // Sala cadastrada
    val localizacaoEncontrada: String?,   // Sala onde foi coletado
    val divergencia: Boolean = false,     // Se há divergência
    
    val sincronizado: Boolean = false,
    val idUsuario: Int
)
```

#### 2.2. Atualizar `ColetaDao`

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/dao/ColetaDao.kt

@Dao
interface ColetaDao {
    
    // Métodos existentes...
    
    /**
     * Buscar coletas por sala cadastrada
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE localizacaoAtual LIKE '%' || :nomeSala || '%'
        ORDER BY dataColeta DESC
    """)
    suspend fun buscarPorSalaCadastrada(nomeSala: String): List<ColetaEntity>
    
    /**
     * Buscar coletas por sala encontrada
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE localizacaoEncontrada LIKE '%' || :nomeSala || '%'
        ORDER BY dataColeta DESC
    """)
    @suspend fun buscarPorSalaEncontrada(nomeSala: String): List<ColetaEntity>
    
    /**
     * Buscar todas as salas distintas (cadastradas)
     */
    @Query("""
        SELECT DISTINCT localizacaoAtual 
        FROM coleta 
        WHERE localizacaoAtual IS NOT NULL
        ORDER BY localizacaoAtual
    """)
    suspend fun buscarSalasDistintas(): List<String>
    
    /**
     * Buscar coletas com divergência de localização
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE divergencia = 1 
        AND localizacaoAtual != localizacaoEncontrada
        ORDER BY dataColeta DESC
    """)
    suspend fun buscarComDivergenciaLocal(): List<ColetaEntity>
}
```

### 3. **Android - Camada de Domínio**

#### 3.1. Criar Use Case

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/domain/usecase/BuscarColetasPorSalaUseCase.kt

package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.repository.ColetaRepository
import com.inventario.mobile.domain.model.Coleta
import javax.inject.Inject

/**
 * Use Case: Buscar coletas filtradas por sala
 * 
 * Regras de negócio:
 * - Se sala for null, retorna todas as coletas
 * - Busca por sala cadastrada (localizacaoAtual)
 * - Inclui informações de divergência
 */
class BuscarColetasPorSalaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    /**
     * Busca coletas por sala
     * 
     * @param nomeSala Nome da sala (null = todas)
     * @return Lista de coletas
     */
    suspend operator fun invoke(nomeSala: String?): Result<List<Coleta>> {
        return try {
            val coletas = if (nomeSala.isNullOrBlank()) {
                coletaRepository.buscarTodas()
            } else {
                coletaRepository.buscarPorSala(nomeSala)
            }
            Result.success(coletas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 4. **Android - Camada de Apresentação**

#### 4.1. Atualizar `CollectionViewState`

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/state/CollectionViewState.kt

sealed class CollectionViewState {
    object Idle : CollectionViewState()
    object Loading : CollectionViewState()
    
    data class Success(
        val allColetas: List<Coleta>,
        val filteredColetas: List<Coleta>,
        val totalColetas: Int,
        val pendentes: Int,
        val salas: List<String>,              // Lista de salas disponíveis
        val salaFiltrada: String? = null,     // Sala atualmente filtrada
        val divergenciasLocal: Int = 0        // Quantidade de divergências de local
    ) : CollectionViewState()
    
    data class Error(val message: String) : CollectionViewState()
}
```

#### 4.2. Atualizar `CollectionViewViewModelClean`

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/coleta/CollectionViewViewModelClean.kt

@HiltViewModel
class CollectionViewViewModelClean @Inject constructor(
    private val buscarColetasUseCase: BuscarColetasUseCase,
    private val buscarColetasPorSalaUseCase: BuscarColetasPorSalaUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<CollectionViewState>(CollectionViewState.Idle)
    val state: StateFlow<CollectionViewState> = _state.asStateFlow()
    
    private var todasColetas: List<Coleta> = emptyList()
    private var filtroUsuario: FiltroUsuario = FiltroUsuario.TODAS
    private var filtroStatus: FiltroStatus = FiltroStatus.TODOS
    private var filtroSala: String? = null  // NOVO
    
    fun carregarColetas() {
        viewModelScope.launch {
            _state.value = CollectionViewState.Loading
            
            val result = buscarColetasUseCase()
            
            if (result.isSuccess) {
                todasColetas = result.getOrNull() ?: emptyList()
                aplicarFiltros()
            } else {
                _state.value = CollectionViewState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao carregar coletas"
                )
            }
        }
    }
    
    /**
     * Filtrar por sala
     */
    fun filtrarPorSala(nomeSala: String?) {
        filtroSala = nomeSala
        aplicarFiltros()
    }
    
    private fun aplicarFiltros() {
        viewModelScope.launch {
            var coletasFiltradas = todasColetas
            
            // Filtro de usuário
            if (filtroUsuario == FiltroUsuario.MINHAS) {
                val usuarioAtual = obterUsuarioAtualUseCase()
                coletasFiltradas = coletasFiltradas.filter { 
                    it.idUsuario == usuarioAtual?.id 
                }
            }
            
            // Filtro de status
            coletasFiltradas = when (filtroStatus) {
                FiltroStatus.TODOS -> coletasFiltradas
                FiltroStatus.SINCRONIZADOS -> coletasFiltradas.filter { it.sincronizado }
                FiltroStatus.PENDENTES -> coletasFiltradas.filter { !it.sincronizado }
            }
            
            // Filtro de sala (NOVO)
            if (!filtroSala.isNullOrBlank()) {
                coletasFiltradas = coletasFiltradas.filter { coleta ->
                    coleta.localizacaoAtual?.contains(filtroSala!!, ignoreCase = true) == true
                }
            }
            
            // Extrair lista de salas únicas
            val salas = todasColetas
                .mapNotNull { it.localizacaoAtual }
                .distinct()
                .sorted()
            
            // Contar divergências de localização
            val divergenciasLocal = todasColetas.count { coleta ->
                coleta.divergencia && 
                coleta.localizacaoAtual != coleta.localizacaoEncontrada
            }
            
            _state.value = CollectionViewState.Success(
                allColetas = todasColetas,
                filteredColetas = coletasFiltradas,
                totalColetas = todasColetas.size,
                pendentes = todasColetas.count { !it.sincronizado },
                salas = salas,
                salaFiltrada = filtroSala,
                divergenciasLocal = divergenciasLocal
            )
        }
    }
    
    enum class FiltroUsuario { TODAS, MINHAS }
    enum class FiltroStatus { TODOS, SINCRONIZADOS, PENDENTES }
}
```

#### 4.3. Atualizar Layout XML

```xml
<!-- InventarioMobile/app/src/main/res/layout/activity_collection_view.xml -->

<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">
    
    <!-- Filtros existentes (Usuário e Status) -->
    
    <!-- NOVO: Filtro por Sala -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Filtrar por Sala"
        android:textStyle="bold"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="8dp"/>
    
    <Spinner
        android:id="@+id/spinnerSalas"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:background="@drawable/spinner_background"
        android:padding="12dp"/>
    
    <!-- Indicador de divergências -->
    <TextView
        android:id="@+id/tvDivergenciasLocal"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="⚠️ 0 divergências de localização"
        android:textColor="@color/warning"
        android:layout_marginTop="8dp"
        android:visibility="gone"/>
    
</LinearLayout>
```

#### 4.4. Atualizar `CollectionAdapter` (Item)

```xml
<!-- InventarioMobile/app/src/main/res/layout/item_coleta.xml -->

<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Patrimônio -->
        <TextView
            android:id="@+id/tvNumeroPatrimonio"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Patrimônio: 12345"
            android:textStyle="bold"
            android:textSize="16sp"/>
        
        <TextView
            android:id="@+id/tvDescricao"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Notebook Dell"
            android:layout_marginTop="4dp"/>
        
        <!-- Localização Cadastrada -->
        <TextView
            android:id="@+id/tvLocalizacaoCadastrada"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="📍 Cadastrado: Sala 101"
            android:layout_marginTop="8dp"
            android:textColor="@color/text_secondary"/>
        
        <!-- Localização Encontrada (se diferente) -->
        <TextView
            android:id="@+id/tvLocalizacaoEncontrada"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="✓ Encontrado: Sala 101"
            android:layout_marginTop="4dp"
            android:textColor="@color/success"
            android:visibility="gone"/>
        
        <!-- Divergência de Localização -->
        <TextView
            android:id="@+id/tvDivergencia"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="⚠️ DIVERGÊNCIA: Encontrado em Sala 205"
            android:layout_marginTop="4dp"
            android:textColor="@color/error"
            android:textStyle="bold"
            android:visibility="gone"/>
        
        <!-- Data e Status -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="8dp">
            
            <TextView
                android:id="@+id/tvDataColeta"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="14/11/2024 10:30"
                android:textSize="12sp"/>
            
            <TextView
                android:id="@+id/tvStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Sincronizado"
                android:textColor="@color/success"
                android:textSize="12sp"/>
        </LinearLayout>
        
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

## 📊 Relatório Desktop (Opcional)

### Criar Relatório de Patrimônios por Sala

```java
// src/main/java/com/inventario/dao/RelatorioColetaDAO.java

public List<Map<String, Object>> gerarRelatorioPatrimoniosPorSala(int idInventario, Integer idSala) {
    String sql = """
        SELECT 
            s.nome AS sala_cadastrada,
            p.numero_patrimonio,
            p.descricao,
            c.localizacao_encontrada AS sala_encontrada,
            CASE 
                WHEN c.id IS NULL THEN 'NÃO COLETADO'
                WHEN c.localizacao_atual = c.localizacao_encontrada THEN 'OK'
                ELSE 'DIVERGÊNCIA'
            END AS status_localizacao,
            c.data_coleta,
            u.nome_completo AS coletor
        FROM tabela_patrimonio p
        INNER JOIN tabela_sala s ON p.id_sala = s.id
        LEFT JOIN tabela_coleta c ON p.id = c.id_patrimonio AND c.id_inventario = ?
        LEFT JOIN tabela_usuario u ON c.id_participante_inventario = u.id
        WHERE 1=1
    """;
    
    if (idSala != null) {
        sql += " AND s.id = ?";
    }
    
    sql += " ORDER BY s.nome, p.numero_patrimonio";
    
    // Executar query...
}
```

---

## ✅ Checklist de Implementação

### Backend (Java)
- [x] Endpoint `/api/mobile/coletas` já retorna dados necessários
- [x] Campos `localizacao_atual` e `localizacao_encontrada` existem

### Android - Dados
- [ ] Atualizar `ColetaEntity` com campos de localização
- [ ] Adicionar métodos no `ColetaDao`
- [ ] Atualizar `ColetaRepository`

### Android - Domínio
- [ ] Criar `BuscarColetasPorSalaUseCase`
- [ ] Atualizar model `Coleta` com campos de localização

### Android - Apresentação
- [ ] Atualizar `CollectionViewState`
- [ ] Adicionar método `filtrarPorSala()` no ViewModel
- [ ] Atualizar layout com Spinner de salas
- [ ] Atualizar `CollectionAdapter` para mostrar divergências
- [ ] Atualizar `CollectionViewActivity`

### Testes
- [ ] Testar filtro por sala
- [ ] Testar exibição de divergências
- [ ] Testar combinação de filtros

---

## 🎯 Resultado Esperado

### Tela de Visualização:

```
┌─────────────────────────────────────────┐
│ Visualização de Coletas                 │
├─────────────────────────────────────────┤
│                                         │
│ Filtrar por Usuário:                    │
│ ○ Todas  ● Minhas Coletas              │
│                                         │
│ Filtrar por Status:                     │
│ ● Todos  ○ Sincronizados  ○ Pendentes  │
│                                         │
│ Filtrar por Sala:                       │
│ [Sala 101 - Lab. Informática ▼]        │
│                                         │
│ ⚠️ 3 divergências de localização        │
│                                         │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ Patrimônio: 12345                   │ │
│ │ Notebook Dell                       │ │
│ │ 📍 Cadastrado: Sala 101             │ │
│ │ ✓ Encontrado: Sala 101              │ │
│ │ 14/11/2024 10:30 | Sincronizado     │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Patrimônio: 12346                   │ │
│ │ Projetor Epson                      │ │
│ │ 📍 Cadastrado: Sala 101             │ │
│ │ ⚠️ DIVERGÊNCIA: Encontrado Sala 205 │ │
│ │ 14/11/2024 11:15 | Sincronizado     │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

**Prioridade:** 🔥 ALTA  
**Complexidade:** Média  
**Tempo Estimado:** 4-6 horas  
**Impacto:** Alto - Funcionalidade solicitada pela coordenadora

**Data:** 17/11/2025  
**Versão:** 2.0.0
