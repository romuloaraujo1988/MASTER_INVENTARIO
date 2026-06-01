# Requirements Document

## Introduction

Os ambientes nos campus do IFMT são heterogêneos: há salas com itens duplicados, itens sem etiqueta e patrimônios cujo QR Code está danificado, ausente ou ilegível. Hoje, quando o coletor está na tela de coleta rápida (scanner) e encontra um item problemático, ele precisa sair do fluxo de scan para escolher outro método de coleta, perdendo o contexto (principalmente a sala atual) e diminuindo a produtividade.

Esta feature adiciona atalhos de navegação na tela de coleta rápida (`ScannerActivity`) que expõem, de forma sutil e consistente com os elementos já existentes (chips de Sala/Estado, FAB e botões), o acesso direto aos demais métodos de coleta já implementados no app: coleta sem etiqueta (`ItemSemEtiquetaActivity`), coleta manual por número (`ManualCollectionActivity`) e coleta por descrição (`DescricaoSelectionActivity`). Os atalhos preservam o contexto da sala atual, tratam o caso de ausência de sala, pré-preenchem dados quando aplicável (a partir de um item escaneado) e funcionam em modo offline.

Esta feature **consolida e expõe** capacidades de navegação já presentes no app. Ela **não** cria novos métodos de coleta, **não** altera os Intents/extras existentes de forma incompatível e **não** altera URLs de endpoints de API funcionais.

## Glossary

- **Tela_Coleta_Rapida**: A `ScannerActivity` (pacote `com.inventario.mobile.presentation.scanner`), tela que escaneia QR Code / código de barras para coleta.
- **Menu_Atalhos**: O conjunto de controles de UI adicionados à Tela_Coleta_Rapida que permitem navegar para os demais métodos de coleta sem encerrar a sessão de scan de forma definitiva.
- **Atalho_Sem_Etiqueta**: Atalho que navega para a `ItemSemEtiquetaActivity` (coleta de itens sem etiqueta).
- **Atalho_Manual**: Atalho que navega para a `ManualCollectionActivity` (coleta manual por número de patrimônio).
- **Atalho_Descricao**: Atalho que navega para a `DescricaoSelectionActivity` (coleta por descrição).
- **Sala_Atual**: A sala de coleta selecionada, persistida no `PreferencesManager` via `getCurrentSalaId()` (Int; `0` significa nenhuma sala) e `getCurrentSalaNome()` (String; vazia significa nenhuma sala).
- **Inventario_Ativo**: O inventário em andamento, identificado por `PreferencesManager.getInventarioAtivoId()` (Int?; `null` significa nenhum inventário ativo).
- **Item_Escaneado**: O resultado do último scan disponível na Tela_Coleta_Rapida (`ScanResult`), contendo número de patrimônio e, quando disponível, a descrição do patrimônio.
- **PreferencesManager**: Componente de persistência local de preferências do app que armazena a Sala_Atual, o Inventario_Ativo e o estado fixo.
- **Modo_Offline**: Estado em que o dispositivo não possui conectividade de rede; o app opera sobre o banco local (Room) com sincronização posterior em lote.
- **Coletor**: Usuário do app responsável por realizar a coleta física dos patrimônios em campo (perfil COLETOR ou superior).

## Requirements

### Requirement 1: Exibir o menu de atalhos na tela de coleta rápida

**User Story:** Como Coletor, quero ver atalhos para os outros métodos de coleta na tela de coleta rápida, para acessá-los rapidamente quando o item escaneado é problemático, sem precisar voltar a telas anteriores.

#### Acceptance Criteria

1. WHEN a Tela_Coleta_Rapida termina de carregar, THE Tela_Coleta_Rapida SHALL exibir o Menu_Atalhos contendo o Atalho_Sem_Etiqueta, o Atalho_Manual e o Atalho_Descricao.
2. THE Menu_Atalhos SHALL apresentar cada atalho com um rótulo textual que identifica o método de coleta de destino.
3. WHILE a câmera está ativamente escaneando, THE Menu_Atalhos SHALL permanecer acessível ao Coletor sem encerrar a sessão de scan.
4. THE Menu_Atalhos SHALL manter visíveis os controles preexistentes da Tela_Coleta_Rapida (chip de Sala_Atual, chip de Estado e ações de coleta sem etiqueta e coletar similar).

### Requirement 2: Navegar para a coleta sem etiqueta preservando o contexto

**User Story:** Como Coletor, quero abrir a coleta sem etiqueta a partir da tela de coleta rápida, para registrar um item cujo QR Code está ausente ou ilegível sem perder a sala em que estou.

#### Acceptance Criteria

1. WHEN o Coletor aciona o Atalho_Sem_Etiqueta com uma Sala_Atual selecionada, THE Tela_Coleta_Rapida SHALL navegar para a `ItemSemEtiquetaActivity`.
2. WHEN o Coletor aciona o Atalho_Sem_Etiqueta, THE Tela_Coleta_Rapida SHALL preservar a Sala_Atual no PreferencesManager para que a `ItemSemEtiquetaActivity` exiba o nome da Sala_Atual.
3. IF a preservação da Sala_Atual no PreferencesManager falha WHEN o Coletor aciona o Atalho_Sem_Etiqueta, THEN THE Tela_Coleta_Rapida SHALL prosseguir com a navegação para a `ItemSemEtiquetaActivity`.
4. WHERE existe um Item_Escaneado disponível, WHEN o Coletor aciona o Atalho_Sem_Etiqueta, THE Tela_Coleta_Rapida SHALL passar a descrição do Item_Escaneado no extra `DESCRICAO_SUGERIDA` e o número do patrimônio no extra `NUMERO_PATRIMONIO_ORIGEM`.
5. WHERE não existe Item_Escaneado disponível, WHEN o Coletor aciona o Atalho_Sem_Etiqueta, THE Tela_Coleta_Rapida SHALL navegar para a `ItemSemEtiquetaActivity` sem os extras `DESCRICAO_SUGERIDA` e `NUMERO_PATRIMONIO_ORIGEM`.

### Requirement 3: Navegar para a coleta manual preservando o contexto

**User Story:** Como Coletor, quero abrir a coleta manual a partir da tela de coleta rápida, para digitar o número de um patrimônio cujo código não pode ser escaneado, mantendo a sala atual.

#### Acceptance Criteria

1. WHEN o Coletor aciona o Atalho_Manual com uma Sala_Atual selecionada, THE Tela_Coleta_Rapida SHALL navegar para a `ManualCollectionActivity`.
2. WHEN o Coletor aciona o Atalho_Manual, THE Tela_Coleta_Rapida SHALL passar o identificador da Sala_Atual no extra `extra_sala_id` no formato Long e o nome da Sala_Atual no extra `extra_sala_nome`.
3. THE Tela_Coleta_Rapida SHALL converter o identificador da Sala_Atual de Int para Long ao preencher o extra `extra_sala_id` do Atalho_Manual.

### Requirement 4: Navegar para a coleta por descrição preservando o contexto

**User Story:** Como Coletor, quero abrir a coleta por descrição a partir da tela de coleta rápida, para localizar um item duplicado ou sem etiqueta pela descrição, mantendo a sala atual.

#### Acceptance Criteria

1. WHEN o Coletor aciona o Atalho_Descricao com uma Sala_Atual selecionada, THE Tela_Coleta_Rapida SHALL navegar para a `DescricaoSelectionActivity`.
2. WHEN o Coletor aciona o Atalho_Descricao, THE Tela_Coleta_Rapida SHALL passar o identificador da Sala_Atual no extra `extra_sala_id` no formato Long e o nome da Sala_Atual no extra `extra_sala_nome`.
3. THE Tela_Coleta_Rapida SHALL converter o identificador da Sala_Atual de Int para Long ao preencher o extra `extra_sala_id` do Atalho_Descricao.
4. IF o preenchimento dos extras de Sala_Atual falha WHEN o Coletor aciona o Atalho_Descricao, THEN THE Tela_Coleta_Rapida SHALL prosseguir com a navegação para a `DescricaoSelectionActivity`.

### Requirement 5: Tratar a ausência de sala selecionada

**User Story:** Como Coletor, quero ser avisado quando não há sala selecionada ao usar um atalho, para que minhas coletas sejam sempre associadas à sala correta.

#### Acceptance Criteria

1. IF a Sala_Atual não está selecionada (`getCurrentSalaId()` retorna `0` ou `getCurrentSalaNome()` retorna texto vazio) WHEN o Coletor aciona qualquer atalho do Menu_Atalhos (Atalho_Sem_Etiqueta, Atalho_Manual ou Atalho_Descricao), THEN THE Tela_Coleta_Rapida SHALL exibir a mensagem "Selecione uma sala antes de continuar" e permanecer na Tela_Coleta_Rapida.
2. WHEN a Tela_Coleta_Rapida exibe a mensagem de ausência de Sala_Atual, THE Tela_Coleta_Rapida SHALL disponibilizar imediatamente ao Coletor a ação de selecionar uma sala junto à mensagem.

### Requirement 6: Consistência visual e não obstrução do fluxo de scan

**User Story:** Como Coletor, quero que os atalhos sejam discretos e consistentes com a interface existente, para que não atrapalhem a leitura dos códigos durante a coleta rápida.

#### Acceptance Criteria

1. THE Menu_Atalhos SHALL usar os mesmos componentes visuais e padrões de estilo já adotados na Tela_Coleta_Rapida para chips e botões de ação.
2. THE Menu_Atalhos SHALL posicionar os atalhos fora da área central de leitura do scanner.
3. WHILE o Coletor realiza um scan, THE Menu_Atalhos SHALL preservar a visibilidade da região de mira do scanner posicionando os atalhos fora da área central de leitura, sem sobrepô-la.

### Requirement 7: Acessibilidade e feedback ao usuário

**User Story:** Como Coletor que utiliza recursos de acessibilidade, quero que os atalhos tenham descrições e feedback claros, para identificá-los e operá-los com leitores de tela.

#### Acceptance Criteria

1. THE Menu_Atalhos SHALL fornecer um `contentDescription` para cada atalho descrevendo o método de coleta de destino.
2. WHEN o Coletor aciona um atalho do Menu_Atalhos, THE Tela_Coleta_Rapida SHALL fornecer feedback visual confirmando a ação acionada.
3. THE Menu_Atalhos SHALL apresentar alvos de toque com dimensão mínima de 48dp por 48dp para cada atalho.

### Requirement 8: Compatibilidade com modo offline

**User Story:** Como Coletor em área sem conectividade, quero usar os atalhos normalmente, para continuar coletando itens problemáticos mesmo sem rede.

#### Acceptance Criteria

1. WHILE o dispositivo está em Modo_Offline, WHEN o Coletor aciona qualquer atalho do Menu_Atalhos com uma Sala_Atual selecionada, THE Tela_Coleta_Rapida SHALL navegar para o método de coleta de destino correspondente.
2. THE Menu_Atalhos SHALL obter a Sala_Atual e o Inventario_Ativo a partir do PreferencesManager sem depender de conectividade de rede.

### Requirement 9: Preservação das integrações existentes

**User Story:** Como mantenedor do sistema, quero que os atalhos reutilizem os contratos de Intent e endpoints já existentes, para não quebrar funcionalidades em produção.

#### Acceptance Criteria

1. THE Menu_Atalhos SHALL reutilizar os nomes de extras já definidos pelas telas de destino (`extra_sala_id`, `extra_sala_nome`, `DESCRICAO_SUGERIDA`, `NUMERO_PATRIMONIO_ORIGEM`).
2. THE Tela_Coleta_Rapida SHALL preservar o comportamento existente do FAB de coleta sem etiqueta, do botão de coletar similar e do tratamento de duplicata após a adição do Menu_Atalhos.
3. THE Menu_Atalhos SHALL navegar entre telas utilizando os endpoints de API existentes, sem alterar URLs de endpoints funcionais.
