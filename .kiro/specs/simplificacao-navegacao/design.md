# Documento de Design — Simplificação de Navegação (simplificacao-navegacao)

## Visão Geral

Esta feature resolve dois problemas de UX no app Android SIHCP:

**Problema 1 — Tela intermediária sem valor:** O fluxo "Coletar por Descrição" passa por `EscolhaMetodoColetaActivity`, que exibe apenas um botão ("Busca Manual"). A tela foi projetada para oferecer múltiplos métodos (IA + Manual), mas nunca foi completada. A solução é fazer `SalaSelectionActivity` navegar diretamente para `DescricaoSelectionActivity` quando o tipo de coleta for `DESCRICAO`.

**Problema 2 — Duas telas "Coletas" com o mesmo nome:** `ColetasActivityClean` (Bottom Navigation) e `CollectionViewActivity` (Navigation Drawer) exibem coletas com conjuntos de funcionalidades diferentes, gerando confusão. A solução é criar uma única `ColetasUnificadaActivity` que reúne todas as funcionalidades de ambas, e atualizar os dois pontos de entrada para apontar para ela.

Nenhuma lógica de negócio nova é introduzida. A feature é essencialmente uma reorganização de navegação e uma fusão de telas existentes, seguindo os padrões já estabelecidos no projeto (Clean Architecture + MVVM + Hilt + StateFlow).

---

## Arquitetura

O projeto segue Clean Architecture com três camadas bem definidas. Esta feature toca principalmente a camada de **Presentation** e não requer alterações nas camadas de **Domain** ou **Data**, pois toda a lógica de negócio (filtros, agrupamento, reenvio, exclusão) já existe nos Use Cases e ViewModels existentes.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                           │
│                                                                     │
│  SalaSelectionActivity ──(DESCRICAO)──► DescricaoSelectionActivity  │
│       │                                                             │
│       ├──(QRCODE)──► ScannerActivity          [sem alteração]       │
│       └──(MANUAL)──► ManualCollectionActivity  [sem alteração]      │
│                                                                     │
│  MainActivity                                                       │
│    ├── BottomNavigation.nav_collections ──► ColetasUnificadaActivity│
│    └── NavigationDrawer.nav_coletas     ──► ColetasUnificadaActivity│
│                                                                     │
│  ColetasUnificadaActivity  ◄──── ColetasUnificadaViewModel          │
│    (fusão de ColetasActivityClean                                   │
│     + CollectionViewActivity)                                       │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ (Use Cases já existentes)
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                               │
│  BuscarColetasUseCase          FiltrarColetasUseCase                │
│  AgruparColetasPorSalaUseCase  ObterUsuarioAtualUseCase             │
│  BuscarSalasComColetasUseCase  ReenviarColetaUseCase                │
│  ExcluirColetaPendenteUseCase  BuscarColetasComFallbackUseCase      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           DATA LAYER                                │
│  ColetaDao (Room)              ColetaApi (Retrofit)                 │
│  ColetaRepositoryImpl          AppDatabase                          │
└─────────────────────────────────────────────────────────────────────┘
```

### Diagrama de Fluxo de Navegação (antes × depois)

```
ANTES:
  SalaSelectionActivity
    ├── QRCODE  → ScannerActivity
    ├── MANUAL  → ManualCollectionActivity
    └── DESCRICAO → EscolhaMetodoColetaActivity → DescricaoSelectionActivity

  BottomNavigation.Coletas → ColetasActivityClean
  NavigationDrawer.Coletas → CollectionViewActivity

DEPOIS:
  SalaSelectionActivity
    ├── QRCODE  → ScannerActivity                [sem alteração]
    ├── MANUAL  → ManualCollectionActivity        [sem alteração]
    └── DESCRICAO → DescricaoSelectionActivity    [bypass direto]

  BottomNavigation.Coletas → ColetasUnificadaActivity
  NavigationDrawer.Coletas → ColetasUnificadaActivity
```

---

## Componentes e Interfaces

### Componentes Modificados

#### 1. `SalaSelectionActivity` (modificação cirúrgica)

**Arquivo:** `presentation/sala/SalaSelectionActivity.kt`

**Mudança:** No método `navegarParaColeta()`, o branch `"DESCRICAO"` passa a criar um `Intent` diretamente para `DescricaoSelectionActivity`, usando as constantes de extra corretas (`EXTRA_SALA_ID` e `EXTRA_SALA_NOME` de `DescricaoSelectionActivity`).

```kotlin
// ANTES
"DESCRICAO" -> {
    val intent = Intent(this, EscolhaMetodoColetaActivity::class.java).apply {
        putExtra("SALA_ID", sala.id)
        putExtra("SALA_NOME", sala.nome)
    }
    startActivity(intent)
    finish()
}

// DEPOIS
"DESCRICAO" -> {
    val intent = Intent(this, DescricaoSelectionActivity::class.java).apply {
        putExtra(DescricaoSelectionActivity.EXTRA_SALA_ID, sala.id)
        putExtra(DescricaoSelectionActivity.EXTRA_SALA_NOME, sala.nome)
    }
    startActivity(intent)
    finish()
}
```

**Observação:** `DescricaoSelectionActivity` já recebe `EXTRA_SALA_ID` (Long) e `EXTRA_SALA_NOME` (String) e já exibe o nome da sala no subtítulo da toolbar — nenhuma alteração é necessária nessa activity.

#### 2. `MainActivity` (atualização dos pontos de entrada)

**Arquivo:** `presentation/main/MainActivity.kt`

**Mudança 1 — Bottom Navigation:** O branch `R.id.nav_collections` passa a lançar `ColetasUnificadaActivity` ao invés de `ColetasActivityClean`.

**Mudança 2 — Navigation Drawer:** O branch `R.id.nav_coletas` passa a lançar `ColetasUnificadaActivity` ao invés de `CollectionViewActivity`.

#### 3. `EscolhaMetodoColetaActivity` (removida do fluxo)

**Arquivo:** `presentation/coleta/EscolhaMetodoColetaActivity.kt`

A activity é removida do fluxo de navegação. O arquivo pode ser mantido como código morto (sem entrada no `AndroidManifest.xml` ou com a entrada removida) para preservar o histórico. A decisão de deletar o arquivo fisicamente é deixada para a fase de tarefas.

### Componentes Novos

#### 4. `ColetasUnificadaActivity` (nova — fusão)

**Arquivo:** `presentation/coletas/ColetasUnificadaActivity.kt`

**Responsabilidade:** Tela única que substitui `ColetasActivityClean` e `CollectionViewActivity`. Combina todas as funcionalidades de ambas:

| Funcionalidade | Origem |
|---|---|
| Campo de busca por texto | `ColetasActivityClean` |
| Chips de status (Todos/Coletados/Pendentes/Sem Etiqueta) | `ColetasActivityClean` |
| Chip "Minhas Coletas" | `CollectionViewActivity` |
| Spinner de filtro por sala | Ambas |
| Visualização agrupada por sala | `ColetasActivityClean` |
| Long-click → menu de contexto (Reenviar/Excluir) | `CollectionViewActivity` |
| Contadores (total / pendentes) | Ambas |
| Pull-to-refresh | Ambas |
| Suporte offline (Room) | Ambas |
| Filtro SEM_ETIQUETA | Ambas |

**Anotação:** `@AndroidEntryPoint` (Hilt)

**ViewModel:** `ColetasUnificadaViewModel` (via `by viewModels()`)

**Adapter:** Reutiliza `ColetasAdapter` existente (já suporta lista simples e agrupada) com adição do callback `onItemLongClick`.

#### 5. `ColetasUnificadaViewModel` (novo — fusão)

**Arquivo:** `presentation/coletas/ColetasUnificadaViewModel.kt`

**Responsabilidade:** Gerencia o estado da `ColetasUnificadaActivity`. É uma fusão de `ColetasViewModelClean` e `CollectionViewViewModelClean`, eliminando duplicações.

**Anotação:** `@HiltViewModel`

**Injeção:** Todos os Use Cases já existentes que eram usados pelos dois ViewModels anteriores.

```kotlin
@HiltViewModel
class ColetasUnificadaViewModel @Inject constructor(
    private val buscarColetasComFallbackUseCase: BuscarColetasComFallbackUseCase,
    private val filtrarColetasUseCase: FiltrarColetasUseCase,
    private val agruparColetasPorSalaUseCase: AgruparColetasPorSalaUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase,
    private val buscarSalasComColetasUseCase: BuscarSalasComColetasUseCase,
    private val reenviarColetaUseCase: ReenviarColetaUseCase,
    private val excluirColetaPendenteUseCase: ExcluirColetaPendenteUseCase,
    private val coletaMigration: ColetaMigration
) : ViewModel()
```

#### 6. `ColetasUnificadaState` (novo — estado da UI)

**Arquivo:** `presentation/coletas/ColetasUnificadaState.kt`

Sealed class que unifica os estados de `ColetasState` e `CollectionViewState`:

```kotlin
sealed class ColetasUnificadaState {
    object Idle : ColetasUnificadaState()
    object Loading : ColetasUnificadaState()
    data class Success(
        val coletas: List<Coleta>,
        val totalColetas: Int,
        val totalPendentes: Int,
        val filtroUsuario: Boolean,
        val filtroSala: String?,
        val filtroStatus: StatusFiltro,
        val salasDisponiveis: List<String>,
        val coletasAgrupadas: Map<String, List<Coleta>>,
        val visualizacaoAgrupada: Boolean
    ) : ColetasUnificadaState()
    data class Error(val message: String) : ColetasUnificadaState()
    data class ColetaReenviada(val message: String) : ColetasUnificadaState()
    data class ColetaExcluida(val message: String) : ColetasUnificadaState()
}
```

---

## Modelos de Dados

Esta feature não introduz novos modelos de dados. Os modelos existentes são reutilizados:

- **`Coleta`** (`data.model.Coleta`) — modelo de coleta com campos `sincronizado`, `semEtiqueta`, `usuarioId`, `localizacaoEncontrada`, `nomeSala`, `numeroPatrimonio`, `descricaoItemSemEtiqueta`.
- **`StatusFiltro`** (`domain.model.StatusFiltro`) — enum com `TODOS`, `COLETADOS`, `PENDENTES`, `SEM_ETIQUETA`.
- **`Sala`** (`domain.model.Sala`) — modelo de sala com `id`, `nome`.

### Mapeamento de Extras de Intent

| Extra | Tipo | Origem | Destino |
|---|---|---|---|
| `DescricaoSelectionActivity.EXTRA_SALA_ID` = `"extra_sala_id"` | `Long` | `SalaSelectionActivity` | `DescricaoSelectionActivity` |
| `DescricaoSelectionActivity.EXTRA_SALA_NOME` = `"extra_sala_nome"` | `String` | `SalaSelectionActivity` | `DescricaoSelectionActivity` |

**Nota importante:** `EscolhaMetodoColetaActivity` usava as chaves `"SALA_ID"` e `"SALA_NOME"` (sem prefixo), enquanto `DescricaoSelectionActivity` espera `"extra_sala_id"` e `"extra_sala_nome"`. A mudança em `SalaSelectionActivity` deve usar as constantes de `DescricaoSelectionActivity` para garantir compatibilidade.

---

## Propriedades de Correção

*Uma propriedade é uma característica ou comportamento que deve ser verdadeiro em todas as execuções válidas de um sistema — essencialmente, uma declaração formal sobre o que o sistema deve fazer. As propriedades servem como ponte entre especificações legíveis por humanos e garantias de correção verificáveis por máquina.*

### Propriedade 1: Roteamento de navegação por tipo de coleta

*Para qualquer* objeto `Sala` e qualquer valor de `coletaTipo` (`QRCODE`, `MANUAL`, `DESCRICAO`), o método `navegarParaColeta()` de `SalaSelectionActivity` deve produzir um `Intent` que aponta para a Activity correta:
- `QRCODE` → `ScannerActivity`
- `MANUAL` → `ManualCollectionActivity`
- `DESCRICAO` → `DescricaoSelectionActivity` (nunca `EscolhaMetodoColetaActivity`)

**Valida: Requisitos 1.1, 1.2, 4.1, 4.2**

---

### Propriedade 2: Extras corretos no Intent para tipo DESCRICAO

*Para qualquer* objeto `Sala` com `coletaTipo = DESCRICAO`, o `Intent` produzido por `navegarParaColeta()` deve conter:
- `EXTRA_SALA_ID` com valor igual a `sala.id`
- `EXTRA_SALA_NOME` com valor igual a `sala.nome`

**Valida: Requisito 1.1**

---

### Propriedade 3: Subtítulo da toolbar reflete o nome da sala

*Para qualquer* string não vazia `salaNome` passada via `EXTRA_SALA_NOME` para `DescricaoSelectionActivity`, o subtítulo da toolbar após `setupToolbar()` deve ser igual a `salaNome`.

**Valida: Requisito 1.3**

---

### Propriedade 4: Filtro por texto retorna apenas coletas correspondentes

*Para qualquer* string de busca `query` e qualquer lista de `Coleta`, o resultado do filtro por texto deve conter apenas coletas onde `numeroPatrimonio` ou `descricaoPatrimonio` contém `query` (comparação case-insensitive). Nenhuma coleta que não satisfaça o critério deve aparecer no resultado.

**Valida: Requisito 2.2**

---

### Propriedade 5: Filtro por status retorna apenas coletas do status correto

*Para qualquer* valor de `StatusFiltro` e qualquer lista de `Coleta`, o resultado do filtro deve conter apenas coletas que satisfazem o critério do status:
- `TODOS` → todas as coletas
- `COLETADOS` → apenas `sincronizado = true`
- `PENDENTES` → apenas `sincronizado = false`
- `SEM_ETIQUETA` → apenas `semEtiqueta = true` ou (`numeroPatrimonio` vazio e `descricaoItemSemEtiqueta` não vazio)

**Valida: Requisitos 2.3, 4.7**

---

### Propriedade 6: Filtro por usuário retorna apenas coletas do usuário

*Para qualquer* `userId` e qualquer lista de `Coleta`, quando o filtro de usuário está ativo, o resultado deve conter apenas coletas onde `usuarioId == userId`.

**Valida: Requisito 2.4**

---

### Propriedade 7: Filtro por sala retorna apenas coletas da sala

*Para qualquer* nome de sala `salaNome` e qualquer lista de `Coleta`, o resultado do filtro por sala deve conter apenas coletas onde `localizacaoEncontrada` ou `nomeSala` é igual a `salaNome` (comparação case-insensitive após trim).

**Valida: Requisito 2.5**

---

### Propriedade 8: Agrupamento por sala é uma partição válida

*Para qualquer* lista de `Coleta`, o resultado de `AgruparColetasPorSalaUseCase` deve ser uma partição válida: (1) toda coleta da lista de entrada aparece em exatamente um grupo; (2) todas as coletas dentro de um grupo compartilham o mesmo nome de sala.

**Valida: Requisitos 2.6, 4.8**

---

### Propriedade 9: Exclusão de coleta pendente remove-a da lista

*Para qualquer* coleta pendente (`sincronizado = false`) com `id` válido, após a execução bem-sucedida de `ExcluirColetaPendenteUseCase(coletaId)`, a coleta não deve aparecer na lista retornada por `BuscarColetasUseCase`.

**Valida: Requisito 2.9**

---

### Propriedade 10: Contadores refletem os dados reais da lista

*Para qualquer* lista de `Coleta` filtrada, `totalColetas` deve ser igual ao tamanho da lista e `totalPendentes` deve ser igual ao número de coletas onde `sincronizado = false`.

**Valida: Requisito 2.11**

---

## Tratamento de Erros

### Erro 1: `DescricaoSelectionActivity` aberta sem sala válida

**Condição:** `salaId <= 0` ou `salaNome` vazio.

**Comportamento atual (já implementado):** A activity exibe um `Toast` com "Erro: Nenhuma sala selecionada" e chama `finish()`.

**Impacto da feature:** Nenhum. O comportamento existente já cobre o caso. A mudança em `SalaSelectionActivity` garante que `EXTRA_SALA_ID` e `EXTRA_SALA_NOME` sejam sempre passados corretamente, tornando esse erro improvável em uso normal.

### Erro 2: Falha ao carregar coletas na `ColetasUnificadaActivity`

**Condição:** Erro de rede ou banco de dados ao buscar coletas.

**Comportamento:** `ColetasUnificadaViewModel` emite `ColetasUnificadaState.Error(message)`. A activity exibe o erro via `Toast` e mantém a UI em estado de erro com opção de retry via pull-to-refresh.

**Estratégia offline-first:** `BuscarColetasComFallbackUseCase` tenta o servidor primeiro e faz fallback para o banco Room local. O erro só é emitido se ambas as fontes falharem.

### Erro 3: Falha ao reenviar coleta pendente

**Condição:** Sem conexão ou erro do servidor ao tentar sincronizar.

**Comportamento:** `ReenviarColetaUseCase` retorna `Result.failure`. O ViewModel emite `ColetasUnificadaState.Error(message)`. A activity exibe o erro via `Toast`. A coleta permanece pendente para nova tentativa.

### Erro 4: Tentativa de excluir coleta já sincronizada

**Condição:** `ExcluirColetaPendenteUseCase` recebe o ID de uma coleta com `sincronizado = true`.

**Comportamento:** O Use Case retorna `Result.failure` com mensagem explicativa. A activity exibe o erro. (Na prática, o menu de contexto só é exibido para coletas pendentes — Requisito 2.10 — então esse caso é uma salvaguarda adicional.)

### Erro 5: Long-click em coleta sincronizada

**Condição:** Usuário faz long-click em coleta com `sincronizado = true`.

**Comportamento:** A activity exibe um `Toast` informando "Esta coleta já está sincronizada e não pode ser gerenciada." Nenhum menu de contexto é exibido.

---

## Estratégia de Testes

### Abordagem Dual

A estratégia combina testes unitários (exemplos específicos e casos de borda) com testes baseados em propriedades (cobertura universal de entradas).

### Testes Unitários

**Foco:** Comportamentos específicos, casos de borda e pontos de integração.

1. **`SalaSelectionActivityTest`**
   - Verifica que `navegarParaColeta()` com `DESCRICAO` não lança `EscolhaMetodoColetaActivity`
   - Verifica que o botão "Voltar" em `DescricaoSelectionActivity` retorna à `SalaSelectionActivity`
   - Verifica que "Sala Fixada" funciona para todos os tipos de coleta

2. **`DescricaoSelectionActivityTest`**
   - Verifica que `salaId <= 0` chama `finish()` e exibe Toast de erro
   - Verifica que `salaNome` vazio chama `finish()` e exibe Toast de erro

3. **`ColetasUnificadaActivityTest`**
   - Verifica que long-click em coleta sincronizada exibe mensagem "já sincronizada"
   - Verifica que long-click em coleta pendente exibe menu com "Reenviar" e "Excluir"
   - Verifica que pull-to-refresh chama `carregarColetas()`
   - Verifica que o título da toolbar é "Coletas"

4. **`MainActivityTest`**
   - Verifica que `nav_collections` (Bottom Navigation) lança `ColetasUnificadaActivity`
   - Verifica que `nav_coletas` (Navigation Drawer) lança `ColetasUnificadaActivity`

### Testes Baseados em Propriedades

**Biblioteca:** [Kotest Property Testing](https://kotest.io/docs/proptest/property-based-testing.html) (já compatível com Kotlin/Android; alternativa: `junit-quickcheck`)

**Configuração:** Mínimo de 100 iterações por propriedade.

**Tag de referência:** `Feature: simplificacao-navegacao, Property {N}: {texto}`

#### Propriedade 1 — Roteamento de navegação
```kotlin
// Feature: simplificacao-navegacao, Property 1: Roteamento de navegação por tipo de coleta
checkAll(Arb.sala(), Arb.coletaTipo()) { sala, tipo ->
    val intent = navegarParaColeta(sala, tipo)
    when (tipo) {
        "QRCODE"    -> intent.component?.className shouldBe ScannerActivity::class.qualifiedName
        "MANUAL"    -> intent.component?.className shouldBe ManualCollectionActivity::class.qualifiedName
        "DESCRICAO" -> {
            intent.component?.className shouldBe DescricaoSelectionActivity::class.qualifiedName
            intent.component?.className shouldNotBe EscolhaMetodoColetaActivity::class.qualifiedName
        }
    }
}
```

#### Propriedade 2 — Extras corretos para DESCRICAO
```kotlin
// Feature: simplificacao-navegacao, Property 2: Extras corretos no Intent para tipo DESCRICAO
checkAll(Arb.sala()) { sala ->
    val intent = navegarParaColeta(sala, "DESCRICAO")
    intent.getLongExtra(DescricaoSelectionActivity.EXTRA_SALA_ID, -1L) shouldBe sala.id
    intent.getStringExtra(DescricaoSelectionActivity.EXTRA_SALA_NOME) shouldBe sala.nome
}
```

#### Propriedade 3 — Subtítulo da toolbar
```kotlin
// Feature: simplificacao-navegacao, Property 3: Subtítulo da toolbar reflete o nome da sala
checkAll(Arb.string(1..100)) { salaNome ->
    val subtitle = computeToolbarSubtitle(salaNome)
    subtitle shouldBe salaNome
}
```

#### Propriedade 4 — Filtro por texto
```kotlin
// Feature: simplificacao-navegacao, Property 4: Filtro por texto retorna apenas coletas correspondentes
checkAll(Arb.list(Arb.coleta()), Arb.string(1..50)) { coletas, query ->
    val resultado = filtrarPorTexto(coletas, query)
    resultado.all { coleta ->
        (coleta.numeroPatrimonio?.contains(query, ignoreCase = true) == true) ||
        (coleta.descricaoPatrimonio?.contains(query, ignoreCase = true) == true)
    } shouldBe true
}
```

#### Propriedade 5 — Filtro por status
```kotlin
// Feature: simplificacao-navegacao, Property 5: Filtro por status retorna apenas coletas do status correto
checkAll(Arb.list(Arb.coleta()), Arb.enum<StatusFiltro>()) { coletas, status ->
    val resultado = filtrarPorStatus(coletas, status)
    when (status) {
        StatusFiltro.TODOS        -> resultado.size shouldBe coletas.size
        StatusFiltro.COLETADOS    -> resultado.all { it.sincronizado } shouldBe true
        StatusFiltro.PENDENTES    -> resultado.all { !it.sincronizado } shouldBe true
        StatusFiltro.SEM_ETIQUETA -> resultado.all { it.semEtiqueta || 
            (it.numeroPatrimonio.isNullOrBlank() && !it.descricaoItemSemEtiqueta.isNullOrBlank()) 
        } shouldBe true
    }
}
```

#### Propriedade 6 — Filtro por usuário
```kotlin
// Feature: simplificacao-navegacao, Property 6: Filtro por usuário retorna apenas coletas do usuário
checkAll(Arb.list(Arb.coleta()), Arb.int(1..9999)) { coletas, userId ->
    val resultado = filtrarPorUsuario(coletas, userId)
    resultado.all { it.usuarioId == userId } shouldBe true
}
```

#### Propriedade 7 — Filtro por sala
```kotlin
// Feature: simplificacao-navegacao, Property 7: Filtro por sala retorna apenas coletas da sala
checkAll(Arb.list(Arb.coleta()), Arb.string(1..100)) { coletas, salaNome ->
    val resultado = filtrarPorSala(coletas, salaNome)
    resultado.all { coleta ->
        val salaColeta = (coleta.localizacaoEncontrada ?: coleta.nomeSala)?.trim()
        salaColeta.equals(salaNome.trim(), ignoreCase = true)
    } shouldBe true
}
```

#### Propriedade 8 — Agrupamento é partição válida
```kotlin
// Feature: simplificacao-navegacao, Property 8: Agrupamento por sala é uma partição válida
checkAll(Arb.list(Arb.coleta())) { coletas ->
    val grupos = agruparPorSala(coletas)
    // Toda coleta aparece em exatamente um grupo
    val todasNosGrupos = grupos.values.flatten()
    todasNosGrupos.size shouldBe coletas.size
    todasNosGrupos.toSet() shouldBe coletas.toSet()
    // Todas as coletas de um grupo têm a mesma sala
    grupos.forEach { (sala, coletasDoGrupo) ->
        coletasDoGrupo.all { coleta ->
            (coleta.localizacaoEncontrada ?: coleta.nomeSala) == sala
        } shouldBe true
    }
}
```

#### Propriedade 9 — Exclusão remove da lista
```kotlin
// Feature: simplificacao-navegacao, Property 9: Exclusão de coleta pendente remove-a da lista
checkAll(Arb.coleta(sincronizado = false)) { coleta ->
    val repo = InMemoryColetaRepository(listOf(coleta))
    excluirColetaPendenteUseCase(coleta.id!!.toLong())
    val listaAposExclusao = buscarColetasUseCase()
    listaAposExclusao.getOrNull()?.none { it.id == coleta.id } shouldBe true
}
```

#### Propriedade 10 — Contadores refletem os dados reais
```kotlin
// Feature: simplificacao-navegacao, Property 10: Contadores refletem os dados reais da lista
checkAll(Arb.list(Arb.coleta())) { coletas ->
    val (total, pendentes) = calcularContadores(coletas)
    total shouldBe coletas.size
    pendentes shouldBe coletas.count { !it.sincronizado }
}
```

### Testes de Integração

1. **Carregamento offline:** Verificar que `ColetasUnificadaActivity` carrega coletas do Room quando sem conexão.
2. **Reenvio de coleta:** Verificar que `reenviarColeta()` chama o endpoint correto e atualiza o estado.
3. **Navegação completa DESCRICAO:** Teste end-to-end do fluxo `SalaSelectionActivity → DescricaoSelectionActivity` sem passar por `EscolhaMetodoColetaActivity`.

### Testes de Fumaça (Smoke Tests)

1. Verificar que `ColetasUnificadaActivity` está registrada no `AndroidManifest.xml`.
2. Verificar que o Navigation Drawer tem exatamente uma entrada apontando para `ColetasUnificadaActivity`.
3. Verificar que `EscolhaMetodoColetaActivity` não está mais acessível via navegação normal.
