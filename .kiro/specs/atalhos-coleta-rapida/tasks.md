# Implementation Plan: Atalhos de Coleta Rápida

## Overview

Esta feature adiciona um **Menu_Atalhos** à `ScannerActivity` (`com.inventario.mobile.presentation.scanner`) que expõe três atalhos de navegação para métodos de coleta já existentes:

- **Atalho_Sem_Etiqueta** → `ItemSemEtiquetaActivity` (`presentation/coleta`)
- **Atalho_Manual** → `ManualCollectionActivity` (`presentation/coleta`)
- **Atalho_Descricao** → `DescricaoSelectionActivity` (`presentation/descricao`)

O plano segue o `design.md` e separa a **decisão de navegação** (lógica pura e testável — `ColetaShortcutNavigator` + modelos) da **execução** (lado Android: `PreferencesManager`, `Intent`, `startActivity`, `Snackbar`), conforme o steering `clean-architecture.md`.

Escopo restrito ao app Android (Kotlin). **Nenhum método de coleta novo é criado**, nenhum contrato de Intent/extra existente é alterado de forma incompatível e nenhuma URL de endpoint funcional é tocada (steering `endpoints-nao-alterar.md`). Os contratos reutilizados foram confirmados no código:

- `PreferencesManager.getCurrentSalaId(): Int` / `getCurrentSalaNome(): String` / `setCurrentSalaId(Int)` / `setCurrentSalaNome(String)`
- `ScanResult.patrimonioCodigo: String` e `ScanResult.patrimonio?.descricao`
- Extras de destino: `extra_sala_id` (Long), `extra_sala_nome` (String), `DESCRICAO_SUGERIDA`, `NUMERO_PATRIMONIO_ORIGEM`

A infraestrutura de testes já está disponível no módulo (`build.gradle`): `io.kotest:kotest-property:5.8.0`, `kotest-runner-junit5`, `JUnitPlatform` habilitado, `mockk`, Espresso/JUnit Android.

## Tasks

- [x] 1. Criar o enum de atalhos e os modelos puros de navegação
  - Criar pacote `presentation/scanner/navigation/` no módulo app.
  - Criar `ColetaShortcut.kt` com o enum `ColetaShortcut { SEM_ETIQUETA, MANUAL, DESCRICAO }` (Kotlin puro, sem dependência de Android).
  - Criar `SalaContext.kt` (`data class SalaContext(val salaId: Int, val salaNome: String)`) com a propriedade computada `isValid = salaId > 0 && salaNome.isNotBlank()`.
  - Criar `ScannedItemRef.kt` (`data class ScannedItemRef(val numeroPatrimonio: String?, val descricao: String?)`) com `fun descricaoOuFallback(): String?` implementando: descrição não vazia → descrição; senão número não vazio → `"Patrimônio {numero}"`; senão `null`.
  - Criar `ShortcutNavigationPlan.kt` como `sealed class` com `RequireSalaSelection` (object), `NavigateToSemEtiqueta(salaParaPersistir, descricaoSugerida, numeroPatrimonioOrigem)`, `NavigateToManual(salaId: Long, salaNome: String)`, `NavigateToDescricao(salaId: Long, salaNome: String)`.
  - Garantir que nenhum arquivo importe pacotes `android.*`.
  - _Requirements: 2.4, 2.5, 3.2, 3.3, 4.2, 4.3, 5.1_

- [x] 2. Implementar o resolver puro de decisão de navegação
  - Criar `presentation/scanner/navigation/ColetaShortcutNavigator.kt` com `fun resolve(shortcut: ColetaShortcut, sala: SalaContext, itemEscaneado: ScannedItemRef?): ShortcutNavigationPlan`.
  - Se `!sala.isValid`, retornar `RequireSalaSelection` para qualquer atalho (independente de `itemEscaneado`).
  - `SEM_ETIQUETA` + sala válida → `NavigateToSemEtiqueta(salaParaPersistir = sala, descricaoSugerida = itemEscaneado?.descricaoOuFallback(), numeroPatrimonioOrigem = itemEscaneado?.numeroPatrimonio)`.
  - `MANUAL` + sala válida → `NavigateToManual(salaId = sala.salaId.toLong(), salaNome = sala.salaNome)`.
  - `DESCRICAO` + sala válida → `NavigateToDescricao(salaId = sala.salaId.toLong(), salaNome = sala.salaNome)`.
  - Manter a função determinística e sem efeitos colaterais (sem rede, Android ou `PreferencesManager`).
  - _Requirements: 2.1, 2.4, 2.5, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 5.1, 8.1_

- [x]* 3. Escrever os testes baseados em propriedades do `ColetaShortcutNavigator`
  - Criar `app/src/test/java/com/inventario/mobile/presentation/scanner/navigation/ColetaShortcutNavigatorPropertyTest.kt` usando Kotest Property (`io.kotest:kotest-property`) com no mínimo 100 iterações por propriedade (`PropTestConfig(iterations = 100)`).
  - Definir geradores: `SalaContext` válido (`salaId in 1..Int.MAX_VALUE`, `salaNome` não-branco incluindo acentos/unicode); `SalaContext` inválido (`salaId <= 0` **ou** `salaNome` em branco/só espaços/tabs); `ScannedItemRef` (combinações de `numeroPatrimonio` e `descricao` nulos/vazios/whitespace/texto) mais o caso `null`; gerador uniforme de `ColetaShortcut`.
  - Incluir em cada teste a tag `// Feature: atalhos-coleta-rapida, Property {n}: {texto}`.
  - **Property 1: Sala válida sempre mapeia cada atalho ao seu destino correspondente** — para sala válida e qualquer item, `resolve` retorna `NavigateToSemEtiqueta`/`NavigateToManual`/`NavigateToDescricao` conforme o atalho e nunca `RequireSalaSelection` (vale também offline, pois o resolver não usa rede). **Validates: Requirements 2.1, 3.1, 4.1, 8.1**
  - **Property 2: Preservação do contexto da sala e conversão Int→Long** — para `MANUAL`/`DESCRICAO`, `salaNome` idêntico e `salaId` (Long) `== sala.salaId.toLong()` com round-trip `salaId.toInt() == sala.salaId`; para `SEM_ETIQUETA`, `salaParaPersistir == sala`. **Validates: Requirements 2.2, 3.2, 3.3, 4.2, 4.3**
  - **Property 3: Extras dependentes do item escaneado, com fallback de descrição** — `SEM_ETIQUETA` com item: `numeroPatrimonioOrigem` = número e `descricaoSugerida` = descrição (ou `"Patrimônio {numero}"` quando descrição vazia/nula mas há número); sem item: ambos `null`. **Validates: Requirements 2.4, 2.5**
  - **Property 4: Ausência de sala bloqueia qualquer atalho** — para sala inválida e qualquer atalho/item, `resolve` retorna `RequireSalaSelection`. **Validates: Requirements 5.1**

- [x]* 4. Escrever testes unitários de borda dos modelos puros
  - Criar `app/src/test/java/com/inventario/mobile/presentation/scanner/navigation/ColetaShortcutModelsTest.kt` (JUnit + Kotest assertions).
  - `descricaoOuFallback()`: descrição presente → retorna descrição; descrição vazia/whitespace + número → `"Patrimônio {n}"`; ambos vazios/nulos → `null`.
  - `SalaContext.isValid`: tabela de casos (`id=0`, `id=-1`, nome `""`, nome `"  "`, par válido).
  - Caso "sem scan": `resolve(SEM_ETIQUETA, salaVálida, null)` → `NavigateToSemEtiqueta` com `descricaoSugerida == null` e `numeroPatrimonioOrigem == null`.
  - **Validates: Requirements 2.4, 2.5, 5.1**

- [x] 5. Adicionar os chips do Menu_Atalhos ao layout do scanner
  - Editar `app/src/main/res/layout/activity_scanner.xml` adicionando um `ChipGroup` (envolto em `HorizontalScrollView` se necessário) na faixa inferior de controles, fora da área central de mira, junto aos chips existentes (`chipSala`/`chipEstado`).
  - Adicionar três `com.google.android.material.chip.Chip` reutilizando o mesmo estilo/tema dos chips existentes:
    - `chipAtalhoSemEtiqueta` — texto "Sem etiqueta", `contentDescription` "Abrir coleta sem etiqueta".
    - `chipAtalhoManual` — texto "Manual", `contentDescription` "Abrir coleta manual por número".
    - `chipAtalhoDescricao` — texto "Descrição", `contentDescription` "Abrir coleta por descrição".
  - Definir `minWidth`/`minHeight` de 48dp por chip (alvo de toque mínimo).
  - Não remover nem reposicionar de forma a sobrepor a região de mira; preservar `chipSala`, `chipEstado`, o FAB de coleta sem etiqueta e o botão "Coletar similar".
  - _Requirements: 1.1, 1.2, 1.4, 6.1, 6.2, 6.3, 7.1, 7.3, 9.2_

- [x] 6. Integrar o resolver e os atalhos na `ScannerActivity`
  - Editar `app/src/main/java/com/inventario/mobile/presentation/scanner/ScannerActivity.kt`.
  - Instanciar `private val navigator = ColetaShortcutNavigator()`.
  - Registrar os listeners dos três chips em `setupButtonListeners()` chamando `acionarAtalho(ColetaShortcut.X)`, sem encerrar a sessão de scan.
  - Implementar `acionarAtalho(shortcut)`: montar `SalaContext` a partir de `preferencesManager.getCurrentSalaId()`/`getCurrentSalaNome()`; montar `ScannedItemRef?` a partir de `currentScanResult` (`patrimonioCodigo`, `patrimonio?.descricao`); chamar `navigator.resolve(...)` e despachar o plano.
  - `RequireSalaSelection` → `mostrarAvisoSalaAusente()` (não navega).
  - `NavigateToSemEtiqueta` → `executarSemEtiqueta(plano)`.
  - `NavigateToManual`/`NavigateToDescricao` → `executarComExtrasSala(target, salaId, salaNome)`.
  - _Requirements: 1.1, 1.3, 2.1, 3.1, 4.1, 5.1, 8.1, 8.2_

- [x] 7. Implementar a execução dos planos de navegação (lado Android)
  - Em `ScannerActivity`, implementar `executarSemEtiqueta(plano)`: persistir a sala via `runCatching { preferencesManager.setCurrentSalaId(...); setCurrentSalaNome(...) }` com `Log.w` em falha e **prosseguir** com a navegação; criar `Intent` para `ItemSemEtiquetaActivity` adicionando `DESCRICAO_SUGERIDA` e `NUMERO_PATRIMONIO_ORIGEM` apenas quando não nulos; chamar `startActivity`.
  - Implementar `executarComExtrasSala(target: Class<*>, salaId: Long, salaNome: String)`: criar `Intent` para o destino, preencher `extra_sala_id` (Long) e `extra_sala_nome` dentro de `runCatching` (falha não bloqueia a navegação) e chamar `startActivity`.
  - Implementar `mostrarAvisoSalaAusente()`: `Snackbar` com texto exato "Selecione uma sala antes de continuar", ação "Selecionar" abrindo a seleção de sala, e permanecer na tela.
  - Adicionar feedback visual de confirmação ao navegar (ex.: `Snackbar`/Toast curto), aproveitando o ripple dos chips.
  - Não alterar o comportamento existente do FAB sem etiqueta, do "Coletar similar" nem do tratamento de duplicata.
  - _Requirements: 2.2, 2.3, 2.4, 2.5, 3.2, 3.3, 4.2, 4.3, 4.4, 5.1, 5.2, 7.2, 8.2, 9.1, 9.2, 9.3_

- [x]* 8. Escrever testes instrumentados de navegação e UI (Espresso/Robolectric)
  - Criar testes em `app/src/androidTest/java/com/inventario/mobile/presentation/scanner/` validando o lado Android.
  - Navegação real: tocar cada chip com sala válida dispara `Intent` para a Activity correta com as chaves exatas (`extra_sala_id` Long, `extra_sala_nome`, e `DESCRICAO_SUGERIDA`/`NUMERO_PATRIMONIO_ORIGEM` quando aplicável). **Validates: Requirements 9.1**
  - Persistência (Atalho_Sem_Etiqueta): após o toque, `PreferencesManager` contém a sala; com `PreferencesManager` que lança exceção, `startActivity` ainda é chamado. **Validates: Requirements 2.2, 2.3**
  - Falha de extras (Manual/Descrição): simular falha de preenchimento e verificar que a navegação prossegue. **Validates: Requirements 4.4**
  - Sala ausente: `Snackbar` com texto exato + ação "Selecionar"; nenhuma navegação. **Validates: Requirements 5.1, 5.2**
  - Presença/estilo: os três chips são exibidos, rotulados, com `contentDescription` e alvo ≥ 48dp; `chipSala`, `chipEstado`, FAB e "Coletar similar" permanecem visíveis. **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 6.1, 6.2, 6.3, 7.1, 7.2, 7.3**
  - Regressão: fluxos de FAB sem etiqueta, "Coletar similar" e tratamento de duplicata permanecem inalterados. **Validates: Requirements 9.2**

- [x] 9. Revisão de compatibilidade e checkpoint final
  - Revisar que as chaves de extra usadas batem exatamente com as constantes/leituras das telas de destino (`ItemSemEtiquetaActivity`, `ManualCollectionActivity`, `DescricaoSelectionActivity`) e que nenhuma URL de endpoint funcional foi alterada (steering `endpoints-nao-alterar.md`).
  - Compilar o app (`.\gradlew.bat assembleDebug` em `InventarioMobile`) e rodar os testes unitários (`.\gradlew.bat testDebugUnitTest`); garantir que tudo passa.
  - Após gerar build, registrar no `BUILD_HISTORY.md` conforme o steering `build-tracking.md`.
  - Em caso de dúvidas, perguntar ao usuário.
  - _Requirements: 9.1, 9.2, 9.3_

## Notes

- Tarefas marcadas com `*` são opcionais (testes). São fortemente recomendadas para garantir as 4 propriedades de correção, mas podem ser puladas em um MVP.
- A lógica nova segue Clean Architecture: `ColetaShortcutNavigator` e os modelos são Kotlin puro (sem Android), e toda interação com `Intent`/`PreferencesManager`/UI fica na `ScannerActivity`.
- Property tests (Kotest Property, ≥100 iterações) cobrem P1–P4 do `design.md`; testes de exemplo e instrumentados cobrem casos de borda, UI e regressão.
- Nenhum extra/endpoint existente é alterado de forma incompatível; os atalhos reutilizam `extra_sala_id`, `extra_sala_nome`, `DESCRICAO_SUGERIDA`, `NUMERO_PATRIMONIO_ORIGEM`.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2", "5"] },
    { "id": 2, "tasks": ["3", "4", "6"] },
    { "id": 3, "tasks": ["7"] },
    { "id": 4, "tasks": ["8", "9"] }
  ]
}
```
