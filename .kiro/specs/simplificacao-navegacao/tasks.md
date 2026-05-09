# Plano de Implementação: Simplificação de Navegação (simplificacao-navegacao)

## Visão Geral

Reorganização de navegação no app Android SIHCP com duas mudanças cirúrgicas:
1. Eliminar `EscolhaMetodoColetaActivity` do fluxo `DESCRICAO`, navegando diretamente para `DescricaoSelectionActivity`.
2. Criar `ColetasUnificadaActivity` + `ColetasUnificadaViewModel` que fundem `ColetasActivityClean` e `CollectionViewActivity`, e atualizar os dois pontos de entrada (`MainActivity`) para apontar para ela.

Nenhuma lógica de negócio nova é introduzida. Toda a camada Domain e Data permanece inalterada.

---

## Tarefas

- [x] 1. Corrigir navegação DESCRICAO em `SalaSelectionActivity`
  - [x] 1.1 Alterar o branch `"DESCRICAO"` no método `navegarParaColeta()` para criar um `Intent` diretamente para `DescricaoSelectionActivity`, usando `DescricaoSelectionActivity.EXTRA_SALA_ID` (Long) e `DescricaoSelectionActivity.EXTRA_SALA_NOME` (String) em vez das chaves antigas `"SALA_ID"` / `"SALA_NOME"` e do destino `EscolhaMetodoColetaActivity`
    - Arquivo: `presentation/sala/SalaSelectionActivity.kt`
    - Manter os branches `QRCODE` e `MANUAL` sem alteração
    - _Requisitos: 1.1, 1.2, 4.1, 4.2_

  - [ ]* 1.2 Escrever teste de propriedade para roteamento de navegação (Propriedade 1)
    - **Propriedade 1: Roteamento de navegação por tipo de coleta**
    - Para qualquer `Sala` e qualquer `coletaTipo`, verificar que `navegarParaColeta()` produz `Intent` apontando para a Activity correta; em especial, `DESCRICAO` nunca deve apontar para `EscolhaMetodoColetaActivity`
    - Usar `Arb.sala()` e `Arb.coletaTipo()` com Kotest Property Testing (mínimo 100 iterações)
    - **Valida: Requisitos 1.1, 1.2, 4.1, 4.2**

  - [ ]* 1.3 Escrever teste de propriedade para extras do Intent DESCRICAO (Propriedade 2)
    - **Propriedade 2: Extras corretos no Intent para tipo DESCRICAO**
    - Para qualquer `Sala`, verificar que o `Intent` gerado contém `EXTRA_SALA_ID == sala.id` e `EXTRA_SALA_NOME == sala.nome`
    - **Valida: Requisito 1.1**

  - [ ]* 1.4 Escrever teste de propriedade para subtítulo da toolbar (Propriedade 3)
    - **Propriedade 3: Subtítulo da toolbar reflete o nome da sala**
    - Para qualquer string não vazia `salaNome`, verificar que `setupToolbar(salaNome)` em `DescricaoSelectionActivity` produz subtítulo igual a `salaNome`
    - **Valida: Requisito 1.3**

  - [ ]* 1.5 Escrever testes unitários para `SalaSelectionActivity` e `DescricaoSelectionActivity`
    - Verificar que `DESCRICAO` não lança `EscolhaMetodoColetaActivity`
    - Verificar que `salaId <= 0` chama `finish()` e exibe Toast de erro
    - Verificar que `salaNome` vazio chama `finish()` e exibe Toast de erro
    - _Requisitos: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Checkpoint — Verificar navegação DESCRICAO
  - Garantir que todos os testes da tarefa 1 passam. Tirar dúvidas com o usuário se necessário.

- [x] 3. Criar `ColetasUnificadaState`
  - [x] 3.1 Criar o arquivo `presentation/coletas/ColetasUnificadaState.kt` com a sealed class que unifica os estados de `ColetasState` e `CollectionViewState`
    - Estados: `Idle`, `Loading`, `Success(coletas, totalColetas, totalPendentes, filtroUsuario, filtroSala, filtroStatus, salasDisponiveis, coletasAgrupadas, visualizacaoAgrupada)`, `Error(message)`, `ColetaReenviada(message)`, `ColetaExcluida(message)`
    - _Requisitos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12_

- [x] 4. Criar `ColetasUnificadaViewModel`
  - [x] 4.1 Criar o arquivo `presentation/coletas/ColetasUnificadaViewModel.kt` anotado com `@HiltViewModel`
    - Injetar: `BuscarColetasComFallbackUseCase`, `FiltrarColetasUseCase`, `AgruparColetasPorSalaUseCase`, `ObterUsuarioAtualUseCase`, `BuscarSalasComColetasUseCase`, `ReenviarColetaUseCase`, `ExcluirColetaPendenteUseCase`, `ColetaMigration`
    - Implementar: `carregarColetas()`, `filtrarPorTexto(query)`, `filtrarPorStatus(status)`, `filtrarPorUsuario(ativo)`, `filtrarPorSala(sala)`, `reenviarColeta(coletaId)`, `excluirColeta(coletaId)`, `toggleVisualizacaoAgrupada()`
    - Emitir `ColetasUnificadaState` via `StateFlow`
    - _Requisitos: 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12, 4.6, 4.7, 4.8_

  - [ ]* 4.2 Escrever teste de propriedade para filtro por texto (Propriedade 4)
    - **Propriedade 4: Filtro por texto retorna apenas coletas correspondentes**
    - Para qualquer lista de `Coleta` e qualquer `query`, verificar que o resultado contém apenas coletas onde `numeroPatrimonio` ou `descricaoPatrimonio` contém `query` (case-insensitive)
    - **Valida: Requisito 2.2**

  - [ ]* 4.3 Escrever teste de propriedade para filtro por status (Propriedade 5)
    - **Propriedade 5: Filtro por status retorna apenas coletas do status correto**
    - Para qualquer `StatusFiltro` e qualquer lista de `Coleta`, verificar que o resultado satisfaz o critério do status (`TODOS`, `COLETADOS`, `PENDENTES`, `SEM_ETIQUETA`)
    - **Valida: Requisitos 2.3, 4.7**

  - [ ]* 4.4 Escrever teste de propriedade para filtro por usuário (Propriedade 6)
    - **Propriedade 6: Filtro por usuário retorna apenas coletas do usuário**
    - Para qualquer `userId` e qualquer lista de `Coleta`, verificar que o resultado contém apenas coletas onde `usuarioId == userId`
    - **Valida: Requisito 2.4**

  - [ ]* 4.5 Escrever teste de propriedade para filtro por sala (Propriedade 7)
    - **Propriedade 7: Filtro por sala retorna apenas coletas da sala**
    - Para qualquer `salaNome` e qualquer lista de `Coleta`, verificar que o resultado contém apenas coletas onde `localizacaoEncontrada` ou `nomeSala` é igual a `salaNome` (case-insensitive após trim)
    - **Valida: Requisito 2.5**

  - [ ]* 4.6 Escrever teste de propriedade para agrupamento por sala (Propriedade 8)
    - **Propriedade 8: Agrupamento por sala é uma partição válida**
    - Para qualquer lista de `Coleta`, verificar que `AgruparColetasPorSalaUseCase` produz uma partição válida: toda coleta aparece em exatamente um grupo e todas as coletas de um grupo compartilham o mesmo nome de sala
    - **Valida: Requisitos 2.6, 4.8**

  - [ ]* 4.7 Escrever teste de propriedade para exclusão de coleta pendente (Propriedade 9)
    - **Propriedade 9: Exclusão de coleta pendente remove-a da lista**
    - Para qualquer coleta pendente (`sincronizado = false`), verificar que após `ExcluirColetaPendenteUseCase(coletaId)` a coleta não aparece na lista retornada por `BuscarColetasUseCase`
    - Usar `InMemoryColetaRepository` para isolar o teste
    - **Valida: Requisito 2.9**

  - [ ]* 4.8 Escrever teste de propriedade para contadores (Propriedade 10)
    - **Propriedade 10: Contadores refletem os dados reais da lista**
    - Para qualquer lista de `Coleta`, verificar que `totalColetas == coletas.size` e `totalPendentes == coletas.count { !it.sincronizado }`
    - **Valida: Requisito 2.11**

- [x] 5. Criar `ColetasUnificadaActivity`
  - [x] 5.1 Criar o arquivo `presentation/coletas/ColetasUnificadaActivity.kt` anotado com `@AndroidEntryPoint`
    - Vincular ao `ColetasUnificadaViewModel` via `by viewModels()`
    - Implementar: campo de busca por texto, chips de status (Todos/Coletados/Pendentes/Sem Etiqueta), chip "Minhas Coletas", spinner de filtro por sala, toggle de visualização agrupada, pull-to-refresh, contadores (total / pendentes), toolbar com título "Coletas"
    - Reutilizar `ColetasAdapter` existente com adição do callback `onItemLongClick`
    - Long-click em coleta pendente → menu de contexto com "Reenviar Coleta" e "Excluir Coleta" (com diálogo de confirmação)
    - Long-click em coleta sincronizada → Toast "Esta coleta já está sincronizada e não pode ser gerenciada."
    - Observar `ColetasUnificadaState` via `lifecycleScope.launch { viewModel.state.collect {} }`
    - _Requisitos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12, 3.4, 4.6, 4.7, 4.8_

  - [x] 5.2 Criar o layout XML `res/layout/activity_coletas_unificada.xml`
    - Incluir: `Toolbar`, `SearchView` ou `EditText` de busca, `ChipGroup` para status, chip "Minhas Coletas", `Spinner` de salas, `RecyclerView`, `SwipeRefreshLayout`, `TextView` de contadores
    - _Requisitos: 2.2, 2.3, 2.4, 2.5, 2.11, 2.12_

  - [x] 5.3 Registrar `ColetasUnificadaActivity` no `AndroidManifest.xml`
    - _Requisitos: 2.1, 3.1, 3.2_

  - [ ]* 5.4 Escrever testes unitários para `ColetasUnificadaActivity`
    - Verificar que long-click em coleta sincronizada exibe mensagem "já sincronizada"
    - Verificar que long-click em coleta pendente exibe menu com "Reenviar" e "Excluir"
    - Verificar que pull-to-refresh chama `carregarColetas()`
    - Verificar que o título da toolbar é "Coletas"
    - _Requisitos: 2.7, 2.10, 2.12, 3.4_

- [x] 6. Atualizar `MainActivity` para usar `ColetasUnificadaActivity`
  - [x] 6.1 No branch `R.id.nav_collections` (Bottom Navigation), substituir `ColetasActivityClean` por `ColetasUnificadaActivity`
    - Arquivo: `presentation/main/MainActivity.kt`
    - _Requisitos: 3.1, 3.5_

  - [x] 6.2 No branch `R.id.nav_coletas` (Navigation Drawer), substituir `CollectionViewActivity` por `ColetasUnificadaActivity`
    - Arquivo: `presentation/main/MainActivity.kt`
    - Garantir que não haja entradas duplicadas no Navigation Drawer apontando para as telas antigas
    - _Requisitos: 3.2, 3.3, 3.5_

  - [ ]* 6.3 Escrever testes unitários para `MainActivity`
    - Verificar que `nav_collections` (Bottom Navigation) lança `ColetasUnificadaActivity`
    - Verificar que `nav_coletas` (Navigation Drawer) lança `ColetasUnificadaActivity`
    - _Requisitos: 3.1, 3.2, 3.3_

- [x] 7. Remover `EscolhaMetodoColetaActivity` do fluxo ativo
  - [x] 7.1 Remover (ou comentar) a entrada de `EscolhaMetodoColetaActivity` no `AndroidManifest.xml` para que a activity não seja mais acessível via navegação normal
    - O arquivo `.kt` pode ser mantido como código morto para preservar histórico
    - _Requisitos: 1.2_

- [x] 8. Checkpoint final — Garantir que todos os testes passam
  - Executar a suite completa de testes unitários e de propriedade
  - Verificar que `ColetasUnificadaActivity` está registrada no `AndroidManifest.xml`
  - Verificar que `EscolhaMetodoColetaActivity` não está mais acessível via navegação normal
  - Verificar que o Navigation Drawer tem exatamente uma entrada apontando para `ColetasUnificadaActivity`
  - Tirar dúvidas com o usuário se necessário.

---

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- Cada tarefa referencia os requisitos específicos para rastreabilidade
- Os testes de propriedade usam [Kotest Property Testing](https://kotest.io/docs/proptest/property-based-testing.html) com mínimo de 100 iterações por propriedade
- Testes unitários de Activity usam Robolectric ou Espresso conforme o padrão já adotado no projeto
- A camada Domain (Use Cases) e a camada Data (Room, Retrofit) não são alteradas por esta feature
- `ColetasAdapter` existente é reutilizado; apenas o callback `onItemLongClick` é adicionado

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "3.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.4", "1.5", "4.1"] },
    { "id": 2, "tasks": ["4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "5.1", "5.2"] },
    { "id": 3, "tasks": ["5.3", "5.4"] },
    { "id": 4, "tasks": ["6.1", "6.2"] },
    { "id": 5, "tasks": ["6.3", "7.1"] }
  ]
}
```
