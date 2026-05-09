# Implementation Plan: Scanner — Coleta Sem Etiqueta (acesso permanente)

## Overview

Plano incremental para adicionar um `ExtendedFloatingActionButton` permanente à `ScannerActivity` que inicia o fluxo de coleta sem etiqueta (via `DescricaoSelectionActivity`), com validações de sala/inventário, sincronização com o bottom sheet, aplicação automática de estado fixo e feedback tátil/sonoro consistente com as demais coletas. Nenhum modelo de dados novo é introduzido; o design reaproveita `RegistrarColetaUseCase.registrarColetaPorDescricao(...)`, `PreferencesManager`, `SoundUtils` e `VibrationHelper`.

A implementação segue Clean Architecture + MVVM (regra de steering `clean-architecture.md`), Kotlin + Hilt, e respeita rigorosamente a regra `endpoints-nao-alterar.md` (esta feature não toca em nenhuma URL de API).

## Tasks

- [x] 1. Adicionar o Extended FAB ao layout da ScannerActivity
  - Editar `InventarioMobile/app/src/main/res/layout/activity_scanner.xml`
  - Adicionar `com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton` com `android:id="@+id/fabColetarSemEtiqueta"`, `android:text="Coletar similar"`, `app:icon="@drawable/ic_content_copy"`, `app:iconTint="@android:color/white"`, `app:backgroundTint="@color/teal_700"`, `android:textColor="@android:color/white"`, `android:contentDescription="Coletar item similar sem etiqueta"`
  - Posicionar com `android:layout_gravity="bottom|end"`, `android:layout_marginEnd="16dp"`, `android:layout_marginBottom="24dp"`, `app:elevation="6dp"`
  - Posicionar como filho direto do `CoordinatorLayout` raiz, após `layoutFixarEstado` e antes de `bottomSheet`, para garantir z-order correto e não obstruir a área central da câmera
  - Verificar ausência de sobreposição com `layoutFixarEstado` em telas pequenas (colunas opostas)
  - _Requirements: 1.1, 1.2, 1.4, 1.5_

- [x] 2. Implementar o handler de clique, validações e navegação na ScannerActivity
  - [x] 2.1 Adicionar binding e listener do FAB
    - Editar `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/scanner/ScannerActivity.kt`
    - Em `setupButtonListeners()` (ou método equivalente de setup), registrar `binding.fabColetarSemEtiqueta.setOnClickListener { handleColetaSemEtiquetaClick() }`
    - _Requirements: 1.1, 2.1_

  - [x] 2.2 Implementar `handleColetaSemEtiquetaClick()` com validações e Intent
    - Ler `salaId = preferencesManager.getCurrentSalaId()`, `salaNome = preferencesManager.getCurrentSalaNome()`, `inventarioId = preferencesManager.getInventarioAtivoId()`
    - Validação 1: se `salaId <= 0` OR `salaNome.isNullOrBlank()` → exibir `Toast.makeText(this, "Selecione uma sala antes de coletar", Toast.LENGTH_SHORT).show()` e retornar sem iniciar a Activity
    - Validação 2: se `inventarioId == null` OR `inventarioId <= 0` → exibir `Toast.makeText(this, "Não há inventário ativo. Não é possível registrar coleta.", Toast.LENGTH_LONG).show()` e retornar sem iniciar a Activity
    - Caso válido: criar `Intent(this, DescricaoSelectionActivity::class.java)` com `putExtra(DescricaoSelectionActivity.EXTRA_SALA_ID, salaId.toLong())` e `putExtra(DescricaoSelectionActivity.EXTRA_SALA_NOME, salaNome)`; chamar `startActivity(intent)`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.2_

  - [x]* 2.3 Escrever property test para validação de pré-condições do FAB
    - **Property 1: Pré-condição obrigatória para abrir o fluxo**
    - **Validates: Requirements 2.4, 2.5, 7.2**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/scanner/ScannerActivityValidationTest.kt`
    - Usar Kotest Property (`io.kotest:kotest-property`) com `checkAll` sobre `(salaId: Int, salaNome: String?, inventarioId: Int?)` gerando valores arbitrários incluindo negativos, zero, `Int.MIN_VALUE`, strings só de whitespace, strings vazias e nulas
    - Mockar `PreferencesManager` e verificar que, para qualquer entrada inválida, a função `handleColetaSemEtiquetaClick` NÃO chama `startActivity` e NÃO chama `registrarColetaPorDescricao`
    - Mínimo 100 iterações. Comentário: `// Feature: scanner-coleta-sem-etiqueta, Property 1: Pré-condição obrigatória`
    - _Requirements: 2.4, 2.5, 7.2_

  - [x]* 2.4 Escrever property test para repasse íntegro de contexto
    - **Property 2: Repasse íntegro do contexto de sala e inventário**
    - **Validates: Requirements 2.2, 2.3, 3.1, 3.2, 7.1**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/scanner/ScannerActivityNavigationTest.kt`
    - Gerar entradas válidas `(salaId > 0, salaNome não vazio, inventarioId > 0)` e verificar que o `Intent` capturado contém `EXTRA_SALA_ID == salaId.toLong()` e `EXTRA_SALA_NOME == salaNome`
    - Mínimo 100 iterações. Comentário: `// Feature: scanner-coleta-sem-etiqueta, Property 2: Repasse íntegro de contexto`
    - _Requirements: 2.2, 2.3, 3.1, 3.2, 7.1_

- [x] 3. Sincronizar visibilidade do FAB com o bottom sheet da ScannerActivity
  - Identificar os pontos em que `binding.bottomSheet.visibility = View.VISIBLE` (abertura) e `binding.bottomSheet.visibility = View.GONE` (fechamento/reset) são definidos em `ScannerActivity.kt`
  - Ao abrir o bottom sheet: adicionar `binding.fabColetarSemEtiqueta.hide()`
  - Ao fechar/resetar o bottom sheet (ex.: `resetScannerState()` ou handlers equivalentes): adicionar `binding.fabColetarSemEtiqueta.show()`
  - Garantir que o FAB não interfere no `buttonColetarSimilar` interno do bottom sheet (controles independentes)
  - _Requirements: 1.2, 1.3, 8.3_

- [x] 4. Atualizar contador e garantir reexibição do FAB em onResume da ScannerActivity
  - Criar (ou reutilizar) método privado `atualizarContadorColetas()` que lê `preferencesManager.getCollectionCount()` e atribui o valor convertido para `String` a `binding.textColetasCount.text`
  - Em `onResume()`, após `super.onResume()` e demais rotinas existentes, chamar `atualizarContadorColetas()` e `binding.fabColetarSemEtiqueta.show()`
  - _Requirements: 6.2, 6.3, 7.3_

- [x] 5. Checkpoint — Validar compilação e navegação básica
  - Executar `cd InventarioMobile && .\gradlew.bat assembleDebug`
  - Registrar a build em `BUILD_HISTORY.md` conforme regra `build-tracking.md` (número incrementado, versionName/versionCode de `app/build.gradle`, tamanho do APK, descrição "Adicionado FAB Coletar Similar na ScannerActivity")
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Aplicar estado de conservação fixo na DescricaoSelectionActivity
  - [x] 6.1 Refatorar a confirmação de coleta para respeitar `isEstadoFixoEnabled` / `getEstadoFixo`
    - Editar `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/descricao/DescricaoSelectionActivity.kt`
    - Localizar o método `showConfirmacaoColetaDialog(...)` (ou equivalente) e, no callback `setPositiveButton("Coletar")`, ler `estadoFixoHabilitado = preferencesManager.isEstadoFixoEnabled()` e `estadoFixo = preferencesManager.getEstadoFixo()`
    - Se `estadoFixoHabilitado == true` AND `!estadoFixo.isNullOrBlank()` → chamar `viewModel.registrarColetaPorDescricao(descricao, salaId.toInt(), salaNome, estadoFixo)` diretamente, sem abrir `EstadoPatrimonioDialog`
    - Caso contrário → chamar `showEstadoDialogParaDescricao(descricao)` (ou função equivalente que exibe `EstadoPatrimonioDialog`)
    - No retorno do dialog, usar o valor selecionado como `estadoConservacao` na chamada ao ViewModel
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x]* 6.2 Escrever property test para origem do estado de conservação
    - **Property 4: Origem do estado de conservação**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/descricao/DescricaoSelectionStatePropertyTest.kt`
    - Usar Kotest Property sobre `(estadoFixoEnabled: Boolean, estadoFixo: String?, estadoSelecionadoNoDialog: String)` e verificar que o parâmetro `estadoConservacao` passado ao ViewModel é igual a `estadoFixo` somente quando `estadoFixoEnabled == true && !estadoFixo.isNullOrBlank()`; caso contrário, é igual a `estadoSelecionadoNoDialog`
    - Mínimo 100 iterações. Comentário: `// Feature: scanner-coleta-sem-etiqueta, Property 4: Origem do estado`
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 7. Adicionar feedback tátil e garantir feedback sonoro na DescricaoSelectionActivity
  - [x] 7.1 Injetar `VibrationHelper` e disparar vibração condicional no sucesso
    - Em `DescricaoSelectionActivity.kt`, adicionar `@Inject lateinit var vibrationHelper: com.inventario.mobile.utils.VibrationHelper`
    - No branch `is ColetaState.Success` do observador de estado do ViewModel: após `hideLoading()`, chamar `SoundUtils.playSuccessSound()`; em seguida, se `preferencesManager.isVibrationOnCollectionEnabled() == true`, chamar `vibrationHelper.vibrateSuccess()`
    - Manter a exibição do `Toast` de sucesso e a recarga da lista via `loadDescricoes()` e `viewModel.limparColetaState()`
    - _Requirements: 5.1, 5.2, 5.3_

  - [x] 7.2 Confirmar que branch de falha NÃO emite feedback e NÃO finaliza a Activity
    - No branch `is ColetaState.Error`: garantir que `SoundUtils.playSuccessSound()` e `vibrationHelper.vibrateSuccess()` NÃO são chamados
    - Garantir que a Activity permanece ativa (nenhum `finish()` é invocado); apenas um Toast com `state.message` é exibido
    - _Requirements: 3.5, 5.4_

  - [x]* 7.3 Escrever property test para feedback pós-coleta condicional
    - **Property 5: Feedback pós-coleta condicional**
    - **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/descricao/DescricaoSelectionFeedbackTest.kt`
    - Gerar entradas `(result: Result<Coleta>, vibrationEnabled: Boolean)` e verificar: em `Result.success` → `SoundUtils.playSuccessSound()` chamado 1 vez E `VibrationHelper.vibrateSuccess()` chamado iff `vibrationEnabled`; em `Result.failure` → nenhum dos dois é chamado
    - Mínimo 100 iterações. Comentário: `// Feature: scanner-coleta-sem-etiqueta, Property 5: Feedback condicional`
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x]* 7.4 Escrever teste unitário para não-finalização em falha
    - **Property 6: Não-finalização em falha**
    - **Validates: Requirements 3.5**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/descricao/DescricaoSelectionErrorTest.kt`
    - Usar exemplos + PBT com diferentes tipos de `Exception` (IOException, SQLException, IllegalStateException, etc.) para verificar que `isFinishing == false` após `ColetaState.Error` ser emitido e que a mensagem de erro é exibida
    - _Requirements: 3.5_

- [x] 8. Garantir persistência correta da coleta sem etiqueta (verificação de integração)
  - Revisar `DescricaoSelectionViewModelClean.registrarColetaPorDescricao(...)` para confirmar (sem alterar estrutura) que:
    - O `inventarioId` usado é `preferencesManager.getInventarioAtivoId()` no momento da chamada
    - `userId` e `userName` são lidos de `preferencesManager`
    - A chamada ao `RegistrarColetaUseCase.registrarColetaPorDescricao(...)` repassa `descricao`, `salaId`, `salaNome`, `estadoConservacao` corretamente
  - Revisar `RegistrarColetaUseCase.registrarColetaPorDescricao(...)` para confirmar que a `Coleta` persistida tem `numeroPatrimonio=null`, `descricaoItemSemEtiqueta=descricao`, `semEtiqueta=true`, `inventarioId` vinculado ao inventário ativo, e status `"COLETADO"`
  - Se alguma dessas invariantes não estiver presente, abrir checkpoint com o usuário antes de modificar (evitar alterar endpoints/use case central)
  - _Requirements: 3.3, 7.1_

- [x]* 9. Escrever property test de persistência sem número de patrimônio
  - **Property 3: Persistência sem número de patrimônio**
  - **Validates: Requirements 3.3, 7.1**
  - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/domain/usecase/RegistrarColetaPorDescricaoPropertyTest.kt`
  - Usar Kotest Property sobre `(descricao: String não vazia, salaId: Int > 0, salaNome: String não vazio, estadoConservacao: String não vazio, inventarioId: Int > 0)` e verificar que a `Coleta` passada ao repositório mock tem `numeroPatrimonio == null`, `descricaoItemSemEtiqueta == descricao`, `semEtiqueta == true`, `inventarioId == preferencesManager.getInventarioAtivoId()`
  - Mínimo 100 iterações. Comentário: `// Feature: scanner-coleta-sem-etiqueta, Property 3: Persistência sem etiqueta`
  - _Requirements: 3.3, 7.1_

- [x] 10. Wire up final e retorno correto à ScannerActivity
  - [x] 10.1 Garantir atualização do contador ao retornar da DescricaoSelectionActivity
    - Validar que `preferencesManager.incrementCollectionCount()` é chamado no fluxo de sucesso (já centralizado no repositório/use case; não duplicar na Activity)
    - Validar que `ScannerActivity.onResume()` relê o contador via `atualizarContadorColetas()` e atualiza `binding.textColetasCount`
    - Validar que a `ScannerActivity` permanece operacional (câmera ativa) após o retorno; se não estiver, chamar o método existente de rearme do scanner (ex.: `resetScannerState()`)
    - _Requirements: 6.1, 6.2, 6.3, 7.3_

  - [x]* 10.2 Escrever property test para consistência do contador
    - **Property 7: Contador consistente ao retornar**
    - **Validates: Requirements 6.2, 7.3**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/scanner/ScannerActivityCounterTest.kt`
    - Gerar sequências arbitrárias de `count: Int >= 0` e verificar que `binding.textColetasCount.text.toString().toInt() == count` após `onResume` simulado
    - Mínimo 100 iterações. Comentário: `// Feature: scanner-coleta-sem-etiqueta, Property 7: Contador consistente`
    - _Requirements: 6.2, 7.3_

  - [x]* 10.3 Escrever teste de independência entre FAB e buttonColetarSimilar
    - **Property 8: Independência entre o FAB e o botão do bottom sheet**
    - **Validates: Requirements 8.1, 8.2, 8.3**
    - Criar `InventarioMobile/app/src/test/java/com/inventario/mobile/presentation/scanner/ScannerActivityIndependenceTest.kt`
    - Verificar, com ações arbitrárias (`hide`, `show`, `click`) sobre `fabColetarSemEtiqueta`, que `buttonColetarSimilar.visibility` e `buttonColetarSimilar.isEnabled` permanecem determinados exclusivamente pela lógica de abertura do bottom sheet, e vice-versa
    - Incluir 1 teste Espresso de instrumentação para confirmar que o `buttonColetarSimilar` mantém seu comportamento original após um scan + coleta
    - _Requirements: 8.1, 8.2, 8.3_

- [x] 11. Checkpoint final — Build, testes e registro
  - Executar `cd InventarioMobile && .\gradlew.bat test` (unit tests) e, se disponível no ambiente, os testes de instrumentação
  - Executar `cd InventarioMobile && .\gradlew.bat assembleDebug` e registrar a build em `BUILD_HISTORY.md` com a descrição "Feature scanner-coleta-sem-etiqueta completa: FAB + validações + estado fixo + feedback"
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido; cobrem principalmente os property tests e testes unitários/instrumentação.
- Cada task referencia requisitos específicos do `requirements.md` para rastreabilidade.
- Os property tests usam **Kotest Property** (já compatível com a stack Kotlin + Hilt do app) com mínimo de 100 iterações e tag `// Feature: scanner-coleta-sem-etiqueta, Property N: ...`.
- Checkpoints (tasks 5 e 11) garantem validação incremental via build Gradle e registro obrigatório em `BUILD_HISTORY.md` conforme regra `build-tracking.md`.
- Respeita `endpoints-nao-alterar.md`: nenhuma URL de API é modificada; o fluxo usa apenas repositórios locais e o `RegistrarColetaPorDescricaoUseCase` existente.
- Respeita `clean-architecture.md`: nenhuma lógica de negócio é adicionada à View; toda decisão passa por `PreferencesManager` (data) → ViewModel → Use Case → Repository.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1", "8"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["2.2", "3", "4"] },
    { "id": 3, "tasks": ["2.3", "2.4", "6.1", "9"] },
    { "id": 4, "tasks": ["6.2", "7.1"] },
    { "id": 5, "tasks": ["7.2", "10.1"] },
    { "id": 6, "tasks": ["7.3", "7.4", "10.2", "10.3"] }
  ]
}
```

Observações sobre o grafo:
- Wave 0: task 1 (layout XML) e task 8 (verificação/leitura do ViewModel e Use Case) são independentes entre si e dos demais.
- Wave 1: task 2.1 depende de 1 (binding precisa existir no layout) e prepara o listener para as tasks seguintes.
- Wave 2: tasks 2.2, 3 e 4 editam pontos distintos de `ScannerActivity.kt` mas compartilham o arquivo; executadas em sequência por wave, cada uma em sub-etapa de edição. Todas dependem de 2.1.
- Wave 3: property tests de navegação (2.3, 2.4), refactor de estado fixo (6.1) e property test de persistência (9) são independentes entre si.
- Wave 4: property test de estado (6.2) depende de 6.1; task 7.1 (feedback vibracional) depende do refactor de sucesso já presente em 6.1 (mesma Activity, mesmo branch `Success`).
- Wave 5: task 7.2 (verificação do branch Error) depende de 7.1; task 10.1 depende de 2.2/3/4 e do fluxo completo.
- Wave 6: testes finais dependem de todas as implementações anteriores.

## Conclusão

Este workflow (criação de design e planejamento) está concluído. Para iniciar a implementação, abra `tasks.md` e clique em **Start task** ao lado dos itens.
