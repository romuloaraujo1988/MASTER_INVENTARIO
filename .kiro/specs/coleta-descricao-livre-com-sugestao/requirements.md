# Requirements Document

## Introduction

A funcionalidade atual de "coleta por descrição" do app SIHCP Android apresenta ao coletor uma lista fixa de descrições vindas do cadastro de patrimônios do sistema. Essas descrições são frequentemente técnicas ou genéricas (ex.: "ARMÁRIO DE MADEIRA 2 PORTAS COM CHAVE"), o que dificulta a associação com o item físico encontrado em campo e gera erros de coleta.

Esta feature adiciona, na tela de coleta de item sem etiqueta, um campo livre para o coletor descrever o item com as próprias palavras, complementado por uma opção ativável que exibe um autocomplete de descrições cadastradas — filtradas para mostrar apenas descrições de patrimônios ainda não coletados no inventário ativo. A descrição final salva é sempre o conteúdo do campo livre, seja ele digitado diretamente ou preenchido por uma sugestão (e potencialmente editado depois).

O escopo abrange: UX no app Android (tela, fluxo, interações), um novo endpoint no servidor para sugestões filtradas (sem alterar endpoints existentes), e o comportamento de cache offline. O modelo de dados da coleta não muda: a descrição continua sendo salva no campo `descricao_item_sem_etiqueta` da `TABELA_COLETA`. As funcionalidades de foto e relatório fotográfico existentes permanecem intactas.

## Glossary

- **App_Android**: Aplicativo Android SIHCP (módulo `InventarioMobile`) usado pelos coletores em campo.
- **Servidor_Mobile**: API REST Spring Boot que expõe endpoints `/api/mobile/**` para o App_Android.
- **Tela_Coleta_Sem_Etiqueta**: Tela do App_Android (`ItemSemEtiquetaActivity`) onde o coletor registra itens sem etiqueta física.
- **Campo_Descricao_Livre**: Componente de entrada de texto, sempre visível na Tela_Coleta_Sem_Etiqueta, onde o coletor digita a descrição do item.
- **Toggle_Sugestao**: Componente de UI (checkbox ou switch) que, quando ativado, exibe o Autocomplete_Sugestao.
- **Autocomplete_Sugestao**: Componente de UI que exibe uma lista filtrável de descrições sugeridas, condicionado ao Toggle_Sugestao estar ativado.
- **Descricao_Final**: Valor textual enviado ao Servidor_Mobile e persistido no campo `descricao_item_sem_etiqueta` da `TABELA_COLETA`.
- **Inventario_Ativo**: Inventário atualmente em andamento, identificado pelo endpoint `GET /api/mobile/inventario/ativo`.
- **Patrimonio_Nao_Coletado**: Patrimônio pertencente ao Inventario_Ativo que ainda não possui registro correspondente na `TABELA_COLETA` para esse inventário.
- **Endpoint_Sugestoes**: Novo endpoint REST no Servidor_Mobile que retorna descrições de Patrimonio_Nao_Coletado do Inventario_Ativo.
- **Cache_Sugestoes_Local**: Armazenamento local (Room) no App_Android contendo descrições baixadas do Endpoint_Sugestoes para uso offline.
- **Coletor**: Usuário autenticado do App_Android com perfil `COLETOR` (ou superior) realizando coleta em campo.
- **Termo_Busca**: String digitada pelo Coletor no Campo_Descricao_Livre usada para filtrar o Autocomplete_Sugestao quando o Toggle_Sugestao está ativado.

## Requirements

### Requirement 1: Campo de descrição livre sempre visível

**User Story:** Como Coletor, quero digitar livremente a descrição do item encontrado em campo, para que o registro reflita o que efetivamente vejo, sem depender de descrições técnicas pré-cadastradas.

#### Acceptance Criteria

1. WHEN a Tela_Coleta_Sem_Etiqueta é aberta, THE App_Android SHALL exibir o Campo_Descricao_Livre visível, editável, com conteúdo inicial vazio e com foco de entrada, em até 500 milissegundos após a abertura da tela.
2. WHILE o Coletor edita o Campo_Descricao_Livre, THE App_Android SHALL manter o texto digitado como valor corrente da Descricao_Final.
3. WHEN o Coletor confirma o registro da coleta, THE App_Android SHALL aplicar trim (remoção de espaços em branco nas extremidades) ao conteúdo do Campo_Descricao_Livre antes de enviá-lo como valor de `descricao_item_sem_etiqueta` ao Servidor_Mobile.
4. IF o conteúdo do Campo_Descricao_Livre após trim está vazio, contém apenas espaços em branco, ou possui menos de 3 caracteres no momento da confirmação, THEN THE App_Android SHALL bloquear o envio, exibir mensagem indicando que a descrição é obrigatória com no mínimo 3 caracteres, preservar o conteúdo digitado e manter o foco no Campo_Descricao_Livre.
5. WHILE o conteúdo do Campo_Descricao_Livre atinge 255 caracteres após trim, THE App_Android SHALL bloquear a digitação de novos caracteres e exibir indicador visual de limite atingido.
6. THE Campo_Descricao_Livre SHALL aceitar um mínimo de 3 caracteres inclusive e um máximo de 255 caracteres inclusive após remoção de espaços nas extremidades.
7. IF uma requisição de registro de coleta chega ao Servidor_Mobile com `descricao_item_sem_etiqueta` após trim com menos de 3 caracteres ou mais de 255 caracteres, THEN THE Servidor_Mobile SHALL rejeitar a requisição com status HTTP 400 e mensagem indicando o intervalo válido (3 a 255 caracteres).

### Requirement 2: Toggle para ativar sugestão de descrições existentes

**User Story:** Como Coletor, quero ter a opção de tentar casar o item encontrado com uma descrição já cadastrada no sistema, para manter consistência com o inventário quando possível, sem ser obrigado a usar uma sugestão.

#### Acceptance Criteria

1. WHEN a Tela_Coleta_Sem_Etiqueta é aberta, THE App_Android SHALL exibir o Toggle_Sugestao em estado desativado em até 500 milissegundos após a abertura da tela.
2. WHILE o Toggle_Sugestao está desativado, THE App_Android SHALL manter o Autocomplete_Sugestao oculto e NÃO executar consultas ao Endpoint_Sugestoes.
3. WHEN o Coletor ativa o Toggle_Sugestao, THE App_Android SHALL exibir o Autocomplete_Sugestao em até 500 milissegundos e preservar integralmente o conteúdo atual do Campo_Descricao_Livre.
4. WHEN o Coletor desativa o Toggle_Sugestao, THE App_Android SHALL ocultar o Autocomplete_Sugestao em até 500 milissegundos e preservar integralmente o conteúdo atual do Campo_Descricao_Livre.
5. IF ocorre falha ao alterar o estado visual do Toggle_Sugestao (exceção ou erro não recuperável), THEN THE App_Android SHALL reverter o componente ao estado anterior e exibir mensagem ao Coletor em até 1 segundo.

### Requirement 3: Autocomplete com descrições de patrimônios não coletados

**User Story:** Como Coletor, quando ativo a opção de sugestão, quero ver apenas descrições de patrimônios que ainda não foram coletados no inventário ativo, para evitar sugestões irrelevantes de itens já registrados.

#### Acceptance Criteria

1. WHEN o Toggle_Sugestao é ativado, THE App_Android SHALL solicitar ao Endpoint_Sugestoes a lista de descrições de Patrimonio_Nao_Coletado do Inventario_Ativo dentro de 2 segundos, limitada a no máximo 500 descrições distintas por requisição.
2. IF a requisição ao Endpoint_Sugestoes falha, expira após 10 segundos, ou retorna erro HTTP, THEN THE App_Android SHALL utilizar o Cache_Sugestoes_Local como fonte das descrições e exibir mensagem indicando uso de dados em cache.
3. WHILE o Toggle_Sugestao está ativado, THE Autocomplete_Sugestao SHALL exibir somente descrições retornadas pelo Endpoint_Sugestoes ou pelo Cache_Sugestoes_Local, limitando a exibição a no máximo 50 itens simultâneos ordenados alfabeticamente.
4. WHILE o Coletor digita no Campo_Descricao_Livre com o Toggle_Sugestao ativado e o Termo_Busca possui 1 ou mais caracteres, THE Autocomplete_Sugestao SHALL filtrar as descrições exibidas usando correspondência parcial case-insensitive e insensível a acentuação sobre o Termo_Busca.
5. WHEN o Coletor seleciona uma descrição no Autocomplete_Sugestao, THE App_Android SHALL preencher o Campo_Descricao_Livre com a descrição selecionada e manter o Campo_Descricao_Livre editável para modificações posteriores.
6. IF o filtro pelo Termo_Busca não retorna nenhuma descrição, THEN THE Autocomplete_Sugestao SHALL exibir mensagem textual indicando ausência de sugestões, THE App_Android SHALL manter o Campo_Descricao_Livre habilitado para entrada livre, e THE Autocomplete_Sugestao SHALL remover a mensagem quando o filtro voltar a produzir ao menos 1 resultado.
7. THE App_Android SHALL aplicar debounce de 300 milissegundos entre a última alteração no Termo_Busca e a reexecução do filtro no Autocomplete_Sugestao, descartando execuções pendentes quando novo caractere for digitado antes do término do intervalo.

### Requirement 4: Descrição final determinada pelo campo livre

**User Story:** Como Coletor, quero que o texto final salvo seja sempre aquilo que está no campo de descrição no momento de confirmar, mesmo que eu tenha selecionado uma sugestão antes e editado depois, para ter controle total sobre o que é registrado.

#### Acceptance Criteria

1. WHEN o Coletor confirma o registro da coleta, THE App_Android SHALL usar o conteúdo do Campo_Descricao_Livre após trim como Descricao_Final, independentemente do estado do Toggle_Sugestao e independentemente de a seleção atual ter vindo do Autocomplete_Sugestao.
2. WHEN o Coletor edita o Campo_Descricao_Livre após selecionar uma descrição no Autocomplete_Sugestao, THE App_Android SHALL descartar qualquer vínculo com a descrição previamente selecionada e manter o conteúdo editado como valor corrente da Descricao_Final.
3. WHEN uma coleta é confirmada com sucesso, THE App_Android SHALL persistir a Descricao_Final na `TABELA_COLETA`, coluna `descricao_item_sem_etiqueta`, sem alterar, adicionar ou remover valores de outras colunas do modelo de coleta.
4. IF a Descricao_Final ultrapassa 255 caracteres após trim no momento da confirmação, THEN THE App_Android SHALL bloquear o envio e exibir mensagem indicando o limite máximo de 255 caracteres.
5. IF a Descricao_Final após trim contém apenas espaços em branco ou está vazia no momento da confirmação, THEN THE App_Android SHALL bloquear o envio e exibir mensagem indicando que a descrição é obrigatória.

### Requirement 5: Novo endpoint de sugestões no servidor

**User Story:** Como integrador do Servidor_Mobile, quero um novo endpoint dedicado às sugestões filtradas, para atender o App_Android sem alterar endpoints existentes que outros clientes consomem.

#### Acceptance Criteria

1. THE Servidor_Mobile SHALL expor um novo endpoint HTTP GET sob o prefixo `/api/mobile/descricoes/` dedicado a sugestões de descrições de Patrimonio_Nao_Coletado do Inventario_Ativo.
2. THE Endpoint_Sugestoes SHALL retornar somente descrições distintas associadas a Patrimonio_Nao_Coletado do Inventario_Ativo no momento da requisição.
3. THE Endpoint_Sugestoes SHALL aceitar um parâmetro de consulta opcional representando o Termo_Busca com tamanho entre 0 e 100 caracteres após trim.
4. WHEN o Termo_Busca possui 1 ou mais caracteres após trim, THE Endpoint_Sugestoes SHALL filtrar o resultado por correspondência parcial case-insensitive e insensível a acentuação sobre a descrição.
5. WHEN o Termo_Busca está vazio ou não é informado, THE Endpoint_Sugestoes SHALL retornar todas as descrições de Patrimonio_Nao_Coletado respeitando paginação.
6. THE Endpoint_Sugestoes SHALL aceitar parâmetros de paginação `page` (inteiro maior ou igual a 0, padrão 0) e `size` (inteiro entre 1 e 100 inclusive, padrão 50).
7. IF o parâmetro `size` da requisição é ausente, não numérico, não inteiro, menor que 1 ou maior que 100, THEN THE Servidor_Mobile SHALL aplicar silenciosamente o valor padrão de 50 sem retornar erro.
8. IF nenhum Inventario_Ativo existir no momento da requisição, THEN THE Endpoint_Sugestoes SHALL retornar HTTP 200 com lista vazia, metadados de paginação e indicador booleano de ausência de inventário ativo.
9. THE Servidor_Mobile SHALL preservar o comportamento, URL, método HTTP, parâmetros, tipos e estrutura de resposta de todos os endpoints existentes sob `/api/mobile/descricoes/` (incluindo `/api/mobile/descricoes/nao-coletadas`) sem alterações de contrato.

### Requirement 6: Performance do endpoint de sugestões

**User Story:** Como Coletor em um inventário grande, quero que o autocomplete responda rápido mesmo com muitos patrimônios, para não atrapalhar o ritmo de coleta em campo.

#### Acceptance Criteria

1. WHEN o Endpoint_Sugestoes recebe requisições com o Inventario_Ativo contendo até 10.000 patrimônios e até 50% de cobertura de coleta, THE Servidor_Mobile SHALL responder com percentil 95 (p95) menor ou igual a 500 milissegundos e tempo máximo menor ou igual a 2000 milissegundos, medidos do recebimento completo da requisição ao envio completo da resposta sobre uma janela de pelo menos 100 requisições.
2. WHEN o Endpoint_Sugestoes recebe uma requisição paginada com `size` inteiro entre 1 e 100 inclusive, THE Servidor_Mobile SHALL retornar no máximo o número de itens definido pelo parâmetro `size`.
3. IF o parâmetro `size` da requisição é ausente, não numérico, não inteiro, menor que 1 ou maior que 100, THEN THE Servidor_Mobile SHALL aplicar silenciosamente o valor padrão de 50 sem rejeitar a requisição.
4. THE Endpoint_Sugestoes SHALL ordenar os resultados alfabeticamente pela descrição em ordem crescente, aplicando regras de locale pt-BR case-insensitive e insensível a acentuação, com desempate determinístico pelo número do patrimônio crescente, garantindo ordenação estável entre páginas.

### Requirement 7: Comportamento offline do autocomplete

**User Story:** Como Coletor em área sem cobertura de rede, quero que a funcionalidade degrade de forma previsível, para continuar coletando sem travar a tela.

#### Acceptance Criteria

1. WHEN o Endpoint_Sugestoes responde com sucesso, THE App_Android SHALL atualizar o Cache_Sugestoes_Local com as descrições recebidas associadas ao identificador do Inventario_Ativo em até 2 segundos.
2. WHILE o App_Android está sem conexão de rede e o Toggle_Sugestao é ativado, IF o Cache_Sugestoes_Local contém descrições para o Inventario_Ativo, THEN THE Autocomplete_Sugestao SHALL exibir no máximo 10 sugestões filtradas pelo Termo_Busca com correspondência case-insensitive e insensível a acentuação em até 500 milissegundos.
3. WHILE o App_Android está sem conexão de rede e o Toggle_Sugestao é ativado, IF o Cache_Sugestoes_Local está vazio para o Inventario_Ativo, THEN THE App_Android SHALL exibir mensagem informando indisponibilidade de sugestões offline.
4. WHILE o App_Android está sem conexão de rede, THE Campo_Descricao_Livre SHALL permanecer habilitado para entrada de texto com preservação integral do conteúdo digitado.
5. WHEN o Coletor confirma o registro da coleta sem conexão, THE App_Android SHALL persistir a coleta localmente conforme o fluxo offline-first já existente.
6. WHEN a coleta é sincronizada com o Servidor_Mobile após retorno da conexão, THE App_Android SHALL enviar a Descricao_Final exatamente como capturada no momento do registro local, sem qualquer transformação ou substituição por descrições obtidas posteriormente.
7. IF ocorre falha ao ler o Cache_Sugestoes_Local, THEN THE App_Android SHALL exibir mensagem informando indisponibilidade de sugestões e manter o Campo_Descricao_Livre habilitado, sem travar a Tela_Coleta_Sem_Etiqueta.

### Requirement 8: Atualização do cache após coleta

**User Story:** Como Coletor, quero que uma descrição deixe de aparecer como sugestão após eu coletar o patrimônio correspondente, para não ver sugestões já usadas no mesmo inventário.

#### Acceptance Criteria

1. WHEN o App_Android persiste localmente uma coleta vinculada a um patrimônio identificado, THE App_Android SHALL marcar o patrimônio como coletado no Cache_Sugestoes_Local em até 500 milissegundos, independentemente de a coleta já ter sido sincronizada com o Servidor_Mobile.
2. WHEN uma coleta é sincronizada com sucesso com o Servidor_Mobile, THE App_Android SHALL confirmar o estado coletado do patrimônio no Cache_Sugestoes_Local.
3. WHEN o Toggle_Sugestao é ativado, THE Autocomplete_Sugestao SHALL atualizar a lista exibida em até 300 milissegundos, removendo descrições cujo patrimônio associado esteja marcado como coletado no Cache_Sugestoes_Local.
4. WHERE uma mesma descrição está associada a múltiplos patrimônios no Inventario_Ativo, THE Autocomplete_Sugestao SHALL manter a descrição visível enquanto pelo menos um patrimônio com essa descrição permanecer não coletado.
5. WHEN o Coletor aciona sincronização manual, THE App_Android SHALL recarregar o Cache_Sugestoes_Local a partir do Endpoint_Sugestoes para o Inventario_Ativo com timeout de 10 segundos.
6. WHEN a sincronização em background conclui sem erros, THE App_Android SHALL recarregar o Cache_Sugestoes_Local a partir do Endpoint_Sugestoes para o Inventario_Ativo com timeout de 10 segundos.
7. IF o recarregamento do Cache_Sugestoes_Local falha por timeout ou erro de rede, THEN THE App_Android SHALL preservar o conteúdo anterior do Cache_Sugestoes_Local e exibir indicador de sincronização pendente.

### Requirement 9: Compatibilidade com funcionalidades existentes

**User Story:** Como responsável pelo sistema SIHCP, quero que a nova feature não quebre o modelo de dados, as fotos e os relatórios já existentes, para evitar impactos colaterais em produção.

#### Acceptance Criteria

1. THE App_Android SHALL persistir a Descricao_Final exclusivamente na coluna `descricao_item_sem_etiqueta` da `TABELA_COLETA`.
2. THE App_Android SHALL NÃO criar, renomear, remover ou alterar o tipo de qualquer coluna da `TABELA_COLETA` como parte desta feature.
3. THE Servidor_Mobile SHALL NÃO criar, renomear, remover ou alterar o tipo de qualquer coluna da `TABELA_COLETA` como parte desta feature.
4. WHEN o App_Android registra uma coleta, THE App_Android SHALL manter inalterados: diretório de armazenamento da foto, formato de arquivo da foto, vínculo foto-coleta, e os fluxos de captura, compressão e armazenamento da foto em relação ao comportamento anterior a esta feature.
5. WHEN o Servidor_Mobile gera o relatório fotográfico, THE Servidor_Mobile SHALL utilizar o valor de `descricao_item_sem_etiqueta` como descrição do item, tratando descrições digitadas livremente e descrições selecionadas por sugestão de forma idêntica.
6. IF uma coleta legada possui `descricao_item_sem_etiqueta` nulo ou vazio, THEN THE Servidor_Mobile SHALL exibir a descrição como string vazia ou placeholder padrão no relatório fotográfico sem lançar erro.
7. THE Servidor_Mobile SHALL preservar os endpoints existentes de coleta (`POST /api/mobile/coletas`, `POST /api/mobile/coletas/batch`) mantendo inalterados: método HTTP, URL, campos obrigatórios e opcionais, tipos dos campos e estrutura de resposta.
8. IF uma requisição de registro de coleta proveniente de versão anterior do App_Android (sem os novos campos introduzidos por esta feature) é recebida pelo Servidor_Mobile, THEN THE Servidor_Mobile SHALL aceitar a requisição e registrar a coleta normalmente.

### Requirement 10: Controle de acesso

**User Story:** Como administrador do sistema, quero que apenas usuários autorizados possam consultar descrições de sugestão, para manter o padrão de segurança do módulo mobile.

#### Acceptance Criteria

1. WHEN uma requisição ao Endpoint_Sugestoes apresenta token JWT válido no cabeçalho `Authorization` com esquema `Bearer` e o usuário autenticado possui perfil `COLETOR`, `SUPERVISOR` ou `ADMIN`, THE Servidor_Mobile SHALL responder com status HTTP 200 contendo a lista de descrições em até 2000 milissegundos.
2. IF uma requisição ao Endpoint_Sugestoes apresenta token JWT ausente, malformado, expirado ou com assinatura inválida, THEN THE Servidor_Mobile SHALL responder com status HTTP 401 em até 500 milissegundos contendo mensagem indicando falha de autenticação e sem retornar descrições.
3. IF uma requisição ao Endpoint_Sugestoes apresenta token JWT válido mas o usuário autenticado possui exclusivamente o perfil `CONSULTA`, THEN THE Servidor_Mobile SHALL responder com status HTTP 403 em até 500 milissegundos contendo mensagem indicando perfil insuficiente e sem retornar descrições.
