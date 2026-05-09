# Documento de Requisitos

## Introdução

Esta feature simplifica a navegação do app SIHCP (Sistema de Histórico e Coleta Patrimonial) eliminando uma tela intermediária desnecessária no fluxo "Coletar por Descrição" e unificando duas telas de "Coletas" que causam confusão nos usuários de campo.

**Problema 1 — Tela intermediária sem valor:** O fluxo "Coletar por Descrição" passa por `EscolhaMetodoColetaActivity`, que exibe apenas um botão ("Busca Manual"). Essa tela foi projetada para oferecer múltiplos métodos de coleta (IA + Manual), mas nunca foi completada. Hoje ela é um passo extra sem utilidade.

**Problema 2 — Duas telas "Coletas" com o mesmo nome:** A Bottom Navigation abre `ColetasActivityClean` (busca/filtro de coletas) e o Navigation Drawer abre `CollectionViewActivity` (gerenciamento de coletas pendentes com reenvio/exclusão). Ambas usam o mesmo ícone e rótulos similares, gerando confusão nos coletores.

---

## Glossário

- **App**: O aplicativo Android SIHCP para coleta patrimonial em campo.
- **Coletor**: Usuário de campo que utiliza o App para registrar coletas de patrimônios.
- **Sala**: Ambiente físico onde os patrimônios estão localizados.
- **Coleta**: Registro de um patrimônio encontrado durante o inventário.
- **Coleta Pendente**: Coleta registrada localmente que ainda não foi sincronizada com o servidor.
- **Bottom_Navigation**: Barra de navegação inferior do App com quatro itens fixos.
- **Navigation_Drawer**: Menu lateral deslizante acessível pelo ícone de hambúrguer na toolbar.
- **SalaSelectionActivity**: Tela de seleção de sala, ponto de entrada para os fluxos de coleta.
- **EscolhaMetodoColetaActivity**: Tela intermediária atualmente presente no fluxo "Coletar por Descrição", a ser eliminada.
- **DescricaoSelectionActivity**: Tela de busca e seleção de descrição de patrimônio para coleta manual.
- **ColetasActivityClean**: Tela atual de visualização de coletas acessada pela Bottom_Navigation; possui busca, filtros por status, filtro por sala e visualização agrupada.
- **CollectionViewActivity**: Tela atual de gerenciamento de coletas acessada pelo Navigation_Drawer; possui filtros e menu de contexto (long-click) para reenviar ou excluir coletas pendentes.
- **Tela_Coletas_Unificada**: Nova tela resultante da fusão de `ColetasActivityClean` e `CollectionViewActivity`, que concentra todas as funcionalidades de ambas.
- **EXTRA_SALA_ID**: Parâmetro de Intent com o ID da sala selecionada.
- **EXTRA_SALA_NOME**: Parâmetro de Intent com o nome da sala selecionada.

---

## Requisitos

### Requisito 1: Eliminar a tela intermediária no fluxo "Coletar por Descrição"

**User Story:** Como coletor, quero selecionar uma sala e ir diretamente para a busca por descrição, para que eu não precise passar por uma tela intermediária sem utilidade.

#### Critérios de Aceitação

1. QUANDO um coletor seleciona uma sala com tipo de coleta `DESCRICAO` na `SalaSelectionActivity`, O `App` SHALL navegar diretamente para a `DescricaoSelectionActivity`, passando `EXTRA_SALA_ID` e `EXTRA_SALA_NOME`.
2. THE `App` SHALL remover a `EscolhaMetodoColetaActivity` do fluxo de navegação do tipo `DESCRICAO`.
3. WHEN a `DescricaoSelectionActivity` é aberta a partir da `SalaSelectionActivity`, THE `DescricaoSelectionActivity` SHALL exibir o nome da sala recebida no subtítulo da toolbar.
4. IF a `DescricaoSelectionActivity` é aberta sem `EXTRA_SALA_ID` válido, THEN THE `App` SHALL exibir uma mensagem de erro e encerrar a tela.
5. THE `App` SHALL manter o comportamento do botão "Voltar" na `DescricaoSelectionActivity`, retornando o coletor à `SalaSelectionActivity`.

---

### Requisito 2: Unificar as duas telas de "Coletas" em uma única tela

**User Story:** Como coletor, quero acessar todas as funcionalidades de coletas em um único lugar, para que eu não precise descobrir qual das duas telas "Coletas" usar para cada tarefa.

#### Critérios de Aceitação

1. THE `App` SHALL disponibilizar uma única `Tela_Coletas_Unificada` que substitui tanto a `ColetasActivityClean` quanto a `CollectionViewActivity`.
2. THE `Tela_Coletas_Unificada` SHALL exibir campo de busca por texto para filtrar coletas por número de patrimônio ou descrição.
3. THE `Tela_Coletas_Unificada` SHALL exibir chips de filtro por status: Todos, Coletados, Pendentes e Sem Etiqueta.
4. THE `Tela_Coletas_Unificada` SHALL exibir chip de filtro para mostrar apenas as coletas do usuário logado.
5. THE `Tela_Coletas_Unificada` SHALL exibir spinner de filtro por sala.
6. THE `Tela_Coletas_Unificada` SHALL exibir as coletas agrupadas por sala quando a opção de agrupamento estiver ativa.
7. WHEN um coletor realiza long-click em uma coleta com status pendente, THE `Tela_Coletas_Unificada` SHALL exibir um menu de contexto com as opções "Reenviar Coleta" e "Excluir Coleta".
8. WHEN um coletor confirma o reenvio de uma coleta pendente, THE `Tela_Coletas_Unificada` SHALL tentar sincronizar a coleta com o servidor e exibir o resultado da operação.
9. WHEN um coletor confirma a exclusão de uma coleta pendente, THE `Tela_Coletas_Unificada` SHALL excluir a coleta localmente e atualizar a lista.
10. WHEN um coletor realiza long-click em uma coleta já sincronizada, THE `Tela_Coletas_Unificada` SHALL exibir uma mensagem informando que a coleta já está sincronizada e não pode ser gerenciada.
11. THE `Tela_Coletas_Unificada` SHALL exibir contadores de total de coletas e coletas pendentes de sincronização.
12. THE `Tela_Coletas_Unificada` SHALL suportar pull-to-refresh para recarregar os dados.

---

### Requisito 3: Atualizar os pontos de entrada de navegação

**User Story:** Como coletor, quero que os rótulos e destinos de navegação sejam claros e consistentes, para que eu saiba exatamente para onde cada item de menu me leva.

#### Critérios de Aceitação

1. WHEN um coletor toca no item "Coletas" da `Bottom_Navigation`, THE `App` SHALL abrir a `Tela_Coletas_Unificada`.
2. WHEN um coletor toca no item "Minhas Coletas" do `Navigation_Drawer`, THE `App` SHALL abrir a `Tela_Coletas_Unificada`.
3. THE `App` SHALL remover a entrada duplicada de "Coletas" / "Minhas Coletas" do `Navigation_Drawer`, mantendo apenas uma entrada que aponte para a `Tela_Coletas_Unificada`.
4. THE `Tela_Coletas_Unificada` SHALL exibir o título "Coletas" na toolbar.
5. IF o `Navigation_Drawer` contiver entradas que apontem para `ColetasActivityClean` ou `CollectionViewActivity`, THEN THE `App` SHALL redirecionar essas entradas para a `Tela_Coletas_Unificada`.

---

### Requisito 4: Preservar todas as funcionalidades existentes

**User Story:** Como coletor, quero que nenhuma funcionalidade que eu já uso seja removida durante a simplificação, para que meu trabalho de campo não seja prejudicado.

#### Critérios de Aceitação

1. THE `App` SHALL manter o fluxo de coleta por QR Code (tipo `QRCODE`) sem alterações.
2. THE `App` SHALL manter o fluxo de coleta manual (tipo `MANUAL`) sem alterações.
3. THE `App` SHALL manter o fluxo de coleta por descrição (tipo `DESCRICAO`) com a navegação direta para `DescricaoSelectionActivity`, sem perda de funcionalidade de busca e seleção de patrimônio.
4. THE `App` SHALL manter a funcionalidade de "Sala Fixada" na `SalaSelectionActivity` para todos os tipos de coleta.
5. THE `App` SHALL manter a funcionalidade de "Modo Rápido" (fixar estado de conservação) na `SalaSelectionActivity` para todos os tipos de coleta.
6. WHEN a `Tela_Coletas_Unificada` é aberta, THE `App` SHALL carregar os dados de coletas do banco de dados local, garantindo funcionamento em modo offline.
7. THE `App` SHALL manter o filtro "Sem Etiqueta" (`SEM_ETIQUETA`) na `Tela_Coletas_Unificada`, que exibe coletas de itens sem número de patrimônio.
8. THE `App` SHALL manter a funcionalidade de visualização agrupada por sala na `Tela_Coletas_Unificada`.
