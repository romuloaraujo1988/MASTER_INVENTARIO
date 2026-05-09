# Requirements Document

## Introduction

Esta feature adiciona à tela de coleta rápida (ScannerActivity) um acesso permanente ao fluxo de coleta de item sem etiqueta (coleta por descrição), permitindo que o coletor registre itens que não possuem número de patrimônio a qualquer momento, sem necessidade de escanear outro patrimônio primeiro.

Hoje a ScannerActivity já possui um botão "Coletar Similar" (`buttonColetarSimilar`), porém esse botão:
- Fica dentro do bottom sheet com dados do patrimônio escaneado
- Só aparece após um scan bem-sucedido e uma coleta
- Usa a descrição do último patrimônio coletado como referência

Esta feature introduz um novo ponto de acesso sempre visível na ScannerActivity que leva o usuário à tela `DescricaoSelectionActivity` (já existente), onde ele escolhe livremente a descrição do item a registrar. Após o registro, o usuário retorna à ScannerActivity para continuar coletando normalmente.

A funcionalidade deve respeitar o contexto atual do coletor (sala selecionada, estado de conservação fixado) e aplicar o mesmo feedback tátil e sonoro usado nas demais coletas.

## Glossary

- **Scanner_Screen**: Tela `ScannerActivity` usada para coleta rápida via câmera e QR Code.
- **Coleta_Sem_Etiqueta_Button**: Novo botão permanente adicionado à Scanner_Screen que inicia o fluxo de coleta sem etiqueta a qualquer momento.
- **Buttom_Sheet_Coletar_Similar**: Botão existente `buttonColetarSimilar` dentro do bottom sheet do scanner, que continua funcionando como hoje (não é removido por esta feature).
- **Descricao_Selection_Screen**: Tela `DescricaoSelectionActivity` existente que lista descrições de patrimônios não coletados e permite registrar uma coleta por descrição.
- **Sala_Atual**: Par `(salaId, salaNome)` persistido em `PreferencesManager` via `getCurrentSalaId()` / `getCurrentSalaNome()`.
- **Estado_Fixo**: Estado de conservação pré-selecionado pelo usuário via `PreferencesManager.isEstadoFixoEnabled()` e `PreferencesManager.getEstadoFixo()`, aplicado automaticamente às coletas.
- **Registrar_Coleta_Por_Descricao_Use_Case**: Use case `RegistrarColetaPorDescricaoUseCase` (e método `RegistrarColetaUseCase.registrarColetaPorDescricao`) já existente, que persiste uma coleta sem `numeroPatrimonio`.
- **Feedback_Coleta**: Conjunto de feedbacks executados após uma coleta bem-sucedida, composto por vibração (`VibrationHelper.vibrateSuccess()`, quando `isVibrationOnCollectionEnabled()`) e som de sucesso (`SoundUtils.playSuccessSound()`).
- **Inventario_Ativo**: Inventário em andamento persistido em `PreferencesManager.getInventarioAtivoId()`.

## Requirements

### Requirement 1: Acesso permanente ao fluxo de coleta sem etiqueta

**User Story:** Como coletor usando a tela de coleta rápida, quero um botão sempre visível para iniciar a coleta de um item sem etiqueta, para que eu possa registrar esses itens a qualquer momento sem precisar escanear um patrimônio antes.

#### Acceptance Criteria

1. THE Scanner_Screen SHALL exibir o Coleta_Sem_Etiqueta_Button de forma visível desde o momento em que a tela termina o `onCreate`.
2. WHILE o bottom sheet de dados do patrimônio está oculto, THE Scanner_Screen SHALL manter o Coleta_Sem_Etiqueta_Button visível e habilitado.
3. WHILE o bottom sheet de dados do patrimônio está aberto, THE Scanner_Screen SHALL manter o Coleta_Sem_Etiqueta_Button acessível ao usuário sem obstrução pelos demais elementos do bottom sheet.
4. THE Scanner_Screen SHALL posicionar o Coleta_Sem_Etiqueta_Button de forma que não sobreponha a área de leitura central da câmera necessária para o scan de QR Code.
5. THE Coleta_Sem_Etiqueta_Button SHALL ser rotulado com o texto "Coletar similar".

### Requirement 2: Abertura da tela de seleção de descrição

**User Story:** Como coletor, quero que ao tocar no botão de coleta sem etiqueta eu seja levado à tela que lista as descrições dos patrimônios não coletados, para que eu possa escolher qual item estou coletando.

#### Acceptance Criteria

1. WHEN o usuário toca no Coleta_Sem_Etiqueta_Button, THE Scanner_Screen SHALL iniciar a Descricao_Selection_Screen via Intent.
2. WHEN o usuário toca no Coleta_Sem_Etiqueta_Button, THE Scanner_Screen SHALL incluir na Intent o extra `EXTRA_SALA_ID` com o valor de `PreferencesManager.getCurrentSalaId()`.
3. WHEN o usuário toca no Coleta_Sem_Etiqueta_Button, THE Scanner_Screen SHALL incluir na Intent o extra `EXTRA_SALA_NOME` com o valor de `PreferencesManager.getCurrentSalaNome()`.
4. IF `PreferencesManager.getCurrentSalaId()` retorna valor menor ou igual a 0, THEN THE Scanner_Screen SHALL exibir mensagem informando que uma sala precisa estar selecionada e SHALL não iniciar a Descricao_Selection_Screen.
5. IF `PreferencesManager.getCurrentSalaNome()` retorna string vazia ou nula, THEN THE Scanner_Screen SHALL exibir mensagem informando que uma sala precisa estar selecionada e SHALL não iniciar a Descricao_Selection_Screen.

### Requirement 3: Registro da coleta sem etiqueta

**User Story:** Como coletor, quero que, após escolher a descrição na tela de seleção, o sistema registre a coleta do item sem etiqueta vinculada à sala atual, para que o item fique contabilizado no inventário ativo.

#### Acceptance Criteria

1. WHEN o usuário confirma o registro de uma coleta por descrição na Descricao_Selection_Screen iniciada a partir do Coleta_Sem_Etiqueta_Button, THE Descricao_Selection_Screen SHALL chamar Registrar_Coleta_Por_Descricao_Use_Case com `salaId` igual ao recebido no extra `EXTRA_SALA_ID`.
2. WHEN o usuário confirma o registro de uma coleta por descrição na Descricao_Selection_Screen iniciada a partir do Coleta_Sem_Etiqueta_Button, THE Descricao_Selection_Screen SHALL chamar Registrar_Coleta_Por_Descricao_Use_Case com `salaNome` igual ao recebido no extra `EXTRA_SALA_NOME`.
3. WHEN Registrar_Coleta_Por_Descricao_Use_Case é chamado no fluxo desta feature, THE Registrar_Coleta_Por_Descricao_Use_Case SHALL persistir a coleta sem `numeroPatrimonio` e com a `descricao` selecionada pelo usuário.
4. WHEN Registrar_Coleta_Por_Descricao_Use_Case conclui com sucesso, THE Descricao_Selection_Screen SHALL indicar ao usuário que a coleta foi registrada.
5. IF Registrar_Coleta_Por_Descricao_Use_Case retorna falha, THEN THE Descricao_Selection_Screen SHALL exibir a mensagem de erro retornada e SHALL não finalizar a tela automaticamente.

### Requirement 4: Aplicação do estado de conservação

**User Story:** Como coletor, quero que o estado de conservação fixado seja aplicado automaticamente à coleta sem etiqueta iniciada pelo scanner, para que eu não precise selecionar o estado a cada item quando já escolhi fixá-lo.

#### Acceptance Criteria

1. WHEN o fluxo de coleta sem etiqueta é iniciado pelo Coleta_Sem_Etiqueta_Button AND `PreferencesManager.isEstadoFixoEnabled()` retorna `true` AND `PreferencesManager.getEstadoFixo()` retorna valor não vazio, THE Descricao_Selection_Screen SHALL usar o valor de `PreferencesManager.getEstadoFixo()` como `estadoConservacao` ao chamar Registrar_Coleta_Por_Descricao_Use_Case sem solicitar seleção de estado ao usuário.
2. WHEN o fluxo de coleta sem etiqueta é iniciado pelo Coleta_Sem_Etiqueta_Button AND `PreferencesManager.isEstadoFixoEnabled()` retorna `false`, THE Descricao_Selection_Screen SHALL exibir `EstadoPatrimonioDialog` antes de chamar Registrar_Coleta_Por_Descricao_Use_Case.
3. WHEN o fluxo de coleta sem etiqueta é iniciado pelo Coleta_Sem_Etiqueta_Button AND `PreferencesManager.isEstadoFixoEnabled()` retorna `true` AND `PreferencesManager.getEstadoFixo()` retorna valor vazio ou nulo, THE Descricao_Selection_Screen SHALL exibir `EstadoPatrimonioDialog` antes de chamar Registrar_Coleta_Por_Descricao_Use_Case.
4. WHEN o usuário seleciona um estado em `EstadoPatrimonioDialog` aberto a partir deste fluxo, THE Descricao_Selection_Screen SHALL usar o valor selecionado como `estadoConservacao` ao chamar Registrar_Coleta_Por_Descricao_Use_Case.

### Requirement 5: Feedback tátil e sonoro após coleta

**User Story:** Como coletor, quero receber o mesmo feedback tátil e sonoro que recebo em uma coleta por scan quando registro uma coleta sem etiqueta iniciada pelo botão do scanner, para que eu tenha confirmação imediata de que a operação foi bem-sucedida.

#### Acceptance Criteria

1. WHEN Registrar_Coleta_Por_Descricao_Use_Case conclui com sucesso no fluxo desta feature, THE Descricao_Selection_Screen SHALL executar `SoundUtils.playSuccessSound()`.
2. WHEN Registrar_Coleta_Por_Descricao_Use_Case conclui com sucesso no fluxo desta feature AND `PreferencesManager.isVibrationOnCollectionEnabled()` retorna `true`, THE Descricao_Selection_Screen SHALL executar `VibrationHelper.vibrateSuccess()`.
3. WHEN Registrar_Coleta_Por_Descricao_Use_Case conclui com sucesso no fluxo desta feature AND `PreferencesManager.isVibrationOnCollectionEnabled()` retorna `false`, THE Descricao_Selection_Screen SHALL não executar `VibrationHelper.vibrateSuccess()`.
4. IF Registrar_Coleta_Por_Descricao_Use_Case retorna falha, THEN THE Descricao_Selection_Screen SHALL não executar `SoundUtils.playSuccessSound()` nem `VibrationHelper.vibrateSuccess()`.

### Requirement 6: Retorno à tela de scanner

**User Story:** Como coletor, quero voltar à tela de coleta rápida após registrar um item sem etiqueta, para que eu possa continuar escaneando patrimônios sem reabrir manualmente o scanner.

#### Acceptance Criteria

1. WHEN o usuário aciona o botão de navegação para cima (up navigation) ou o botão voltar do sistema a partir da Descricao_Selection_Screen iniciada pelo Coleta_Sem_Etiqueta_Button, THE Descricao_Selection_Screen SHALL finalizar retornando o controle à Scanner_Screen de origem.
2. WHEN o controle retorna à Scanner_Screen após o fluxo de coleta sem etiqueta, THE Scanner_Screen SHALL atualizar o contador exibido em `textColetasCount` para refletir o número atual de coletas da sessão obtido via `PreferencesManager.getCollectionCount()`.
3. WHEN o controle retorna à Scanner_Screen após o fluxo de coleta sem etiqueta, THE Scanner_Screen SHALL permanecer operacional para novos scans de QR Code sem exigir reinício manual pelo usuário.

### Requirement 7: Contabilização no inventário ativo

**User Story:** Como supervisor do inventário, quero que as coletas sem etiqueta registradas pelo scanner sejam contabilizadas no inventário ativo corrente, para que os relatórios e estatísticas reflitam corretamente essas coletas.

#### Acceptance Criteria

1. WHEN Registrar_Coleta_Por_Descricao_Use_Case persiste uma coleta originada pelo Coleta_Sem_Etiqueta_Button, THE Registrar_Coleta_Por_Descricao_Use_Case SHALL associar a coleta ao inventário cujo ID é retornado por `PreferencesManager.getInventarioAtivoId()`.
2. IF `PreferencesManager.getInventarioAtivoId()` retorna valor nulo ou menor ou igual a 0 no momento do clique no Coleta_Sem_Etiqueta_Button, THEN THE Scanner_Screen SHALL exibir mensagem informando que não há inventário ativo e SHALL não iniciar a Descricao_Selection_Screen.
3. WHEN uma coleta sem etiqueta é registrada com sucesso no fluxo desta feature, THE Scanner_Screen SHALL incrementar o contador da sessão via `PreferencesManager.incrementCollectionCount()` ao retornar.

### Requirement 8: Preservação do comportamento existente de "Coletar Similar"

**User Story:** Como coletor, quero que o botão "Coletar Similar" existente no bottom sheet continue funcionando como hoje, para que meu fluxo atual de coletar itens similares após um scan não seja afetado por esta nova feature.

#### Acceptance Criteria

1. THE Scanner_Screen SHALL manter o Buttom_Sheet_Coletar_Similar no layout do bottom sheet com o mesmo identificador `buttonColetarSimilar`.
2. WHEN o bottom sheet é aberto após uma coleta bem-sucedida via scan, THE Scanner_Screen SHALL continuar exibindo o Buttom_Sheet_Coletar_Similar com o comportamento atual de coletar item similar ao último coletado.
3. THE Scanner_Screen SHALL tratar o Coleta_Sem_Etiqueta_Button e o Buttom_Sheet_Coletar_Similar como controles independentes, sem que a ativação de um altere o estado do outro.
