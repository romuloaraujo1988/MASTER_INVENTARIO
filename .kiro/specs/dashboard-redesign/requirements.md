# Requirements Document

## Introduction

Esta funcionalidade é um **redesign da camada de apresentação** do Dashboard do aplicativo Android (`InventarioMobile`), com o objetivo de torná-lo mais **profissional** (visual corporativo, limpo e consistente). O foco é exclusivamente em **apresentação, hierarquia visual e consistência** — não há criação de nova lógica de negócio, novos endpoints ou alteração do contrato de dados.

Todos os dados necessários já estão disponíveis na camada de ViewModel (`DashboardViewModelClean`) por meio do modelo de domínio `DashboardStats` e do método já implementado `loadColetasEvolucao(inventarioId)`. O redesign aproveita esses dados existentes, fechando lacunas como o gráfico de evolução (cujos dados são calculados mas não exibidos) e o timestamp da última sincronização (atualmente apenas registrado em log).

O redesign abrange: redesenho dos 4 cartões de KPI para um estilo neutro e profissional, adição de um indicador proeminente de conclusão do inventário, área de cabeçalho com contexto (saudação, nome do inventário, última sincronização), gráfico de evolução de coletas, melhoria da hierarquia dos botões de ação, polimento de tipografia/espaçamento e melhorias de acessibilidade.

O trabalho deve **preservar integralmente** as funcionalidades existentes (pull-to-refresh, indicador offline, busca por voz com FAB e overlay, ações de navegação e o fluxo de fonte única de verdade dos KPIs) e seguir Clean Architecture + MVVM, onde o Fragment apenas renderiza.

## Glossary

- **Dashboard**: A tela principal do app Android implementada por `DashboardFragment` (`fragment_dashboard.xml`), que exibe o resumo do inventário e ações de coleta.
- **DashboardFragment**: Componente de apresentação (View) que renderiza o Dashboard; segue MVVM e não contém lógica de negócio.
- **DashboardViewModelClean**: ViewModel existente que expõe `fonteEstatisticas`, `uiState` e o método `loadColetasEvolucao(...)`.
- **DashboardStats**: Modelo de domínio já existente que expõe os campos: `totalPatrimonios`, `totalColetados`, `totalPendentes`, `divergencias`, `coletoresAtivos`, `percentualConclusao`, `inventarioId`, `inventarioNome`, `isOfflineData`, `timestampUltimaSincronizacao`.
- **FonteEstatisticas**: `StateFlow<DashboardStats>` exposto pelo ViewModel como fonte única de verdade (single source of truth) dos KPIs do Dashboard.
- **Cartao_KPI**: Elemento visual (MaterialCardView) que exibe uma métrica do inventário (Coletados, Pendentes, Divergências, Coletores).
- **Indicador_Conclusao**: Componente `LinearProgressIndicator` (Material) que exibe o percentual de conclusão do inventário derivado de `percentualConclusao`.
- **Area_Cabecalho**: Região no topo do Dashboard que exibe saudação, nome do inventário ativo (`inventarioNome`) e o timestamp da última sincronização (`timestampUltimaSincronizacao`).
- **Grafico_Evolucao**: Gráfico (MPAndroidChart) que exibe a evolução de coletas alimentado por `loadColetasEvolucao(...)`.
- **Indicador_Offline**: Componente existente (`layoutOfflineIndicator`) que sinaliza quando os dados exibidos são locais/offline.
- **FAB_Voz**: Floating Action Button (`fabVoiceSearch`) que inicia a busca por voz.
- **Overlay_Voz**: Sobreposição de tela cheia (`voiceListeningOverlay`) usada durante a escuta de voz.
- **Sistema_de_Espacamento_8dp**: Convenção de espaçamento em que margens e paddings são múltiplos de 8dp.
- **TextAppearance_Tema**: Estilos de tipografia definidos no tema do aplicativo (Material TextAppearance) usados de forma consistente.
- **Estado_Carregamento**: Representação visual (skeleton/placeholder) exibida enquanto os dados do Dashboard estão sendo carregados.
- **Estado_Vazio**: Representação visual exibida quando não há dados a apresentar.
- **Estado_Erro**: Representação visual exibida quando ocorre falha ao carregar dados (`tvError`).
- **WCAG_AA**: Nível AA das diretrizes WCAG de contraste de cor (texto normal ≥ 4.5:1; texto grande ≥ 3:1).
- **Alvo_de_Toque**: Área tocável de um elemento interativo, com dimensão mínima recomendada de 48dp x 48dp.

## Requirements

### Requirement 1: Redesenho dos Cartões de KPI

**User Story:** Como coletor de campo, quero que os cartões de KPI tenham uma aparência profissional e neutra, para que eu consiga ler as métricas com clareza e foco.

#### Acceptance Criteria

1. THE DashboardFragment SHALL renderizar o fundo de cada Cartao_KPI com uma cor sólida de superfície neutra do tema (branco ou a cor de superfície definida no tema), sem aplicar gradientes nem cores saturadas de preenchimento.
2. THE DashboardFragment SHALL aplicar no máximo uma cor de destaque por Cartao_KPI, restrita ao ícone e/ou ao valor numérico, mantendo os rótulos de texto e o fundo do cartão em tons neutros do tema.
3. THE DashboardFragment SHALL renderizar os 4 Cartao_KPI (Coletados, Pendentes, Divergências, Coletores) em uma grade de 2 colunas por 2 linhas, com largura e altura idênticas entre todos os 4 cartões.
4. THE DashboardFragment SHALL remover as sombras de texto (`shadowDx`, `shadowDy`, `shadowRadius`) dos números dos Cartao_KPI.
5. THE DashboardFragment SHALL exibir o valor numérico inteiro de cada campo da FonteEstatisticas em seu Cartao_KPI correspondente: `totalColetados` no cartão Coletados, `totalPendentes` no cartão Pendentes, `divergencias` no cartão Divergências e `coletoresAtivos` no cartão Coletores.
6. WHEN a FonteEstatisticas emitir um novo valor para um dos campos exibidos (`totalColetados`, `totalPendentes`, `divergencias`, `coletoresAtivos`), THE DashboardFragment SHALL atualizar o texto do Cartao_KPI correspondente para refletir o novo valor.

### Requirement 2: Indicador de Conclusão do Inventário

**User Story:** Como supervisor, quero ver de forma proeminente o percentual de conclusão do inventário, para que eu acompanhe o progresso rapidamente.

#### Acceptance Criteria

1. THE DashboardFragment SHALL exibir um Indicador_Conclusao do tipo `LinearProgressIndicator` (Material) no Dashboard, em modo determinado, com valor mínimo de 0 e valor máximo de 100.
2. THE DashboardFragment SHALL definir o valor do Indicador_Conclusao igual ao valor de `percentualConclusao` da FonteEstatisticas, arredondado ao número inteiro mais próximo e restrito ao intervalo de 0 a 100.
3. THE DashboardFragment SHALL exibir um rótulo textual associado ao Indicador_Conclusao no formato "X% concluído", onde X é o valor de `percentualConclusao` arredondado ao número inteiro mais próximo no intervalo de 0 a 100, sem casas decimais.
4. WHEN a FonteEstatisticas emitir um novo valor de `percentualConclusao`, THE DashboardFragment SHALL atualizar o valor do Indicador_Conclusao e o texto do rótulo correspondente em até 1 segundo após a emissão.
5. IF o valor de `percentualConclusao` for inferior a 0, superior a 100, nulo ou não numérico (NaN), THEN THE DashboardFragment SHALL limitar o valor exibido tanto no Indicador_Conclusao quanto no rótulo ao intervalo de 0 a 100, tratando valores inferiores a 0, nulos ou inválidos como 0 e valores superiores a 100 como 100.

### Requirement 3: Área de Cabeçalho com Contexto

**User Story:** Como coletor de campo, quero uma área de cabeçalho com saudação e o contexto do inventário ativo, para que eu saiba imediatamente em qual inventário estou trabalhando e quão atuais são os dados.

#### Acceptance Criteria

1. THE DashboardFragment SHALL exibir uma Area_Cabecalho contendo uma saudação textual não vazia ao usuário, com comprimento máximo de 60 caracteres.
2. THE DashboardFragment SHALL exibir o nome do inventário ativo a partir do campo `inventarioNome` da FonteEstatisticas na Area_Cabecalho, exibindo no máximo 80 caracteres e truncando com reticências (…) o texto que exceder esse limite.
3. WHEN o campo `timestampUltimaSincronizacao` da FonteEstatisticas possuir valor não nulo, THE DashboardFragment SHALL exibir o timestamp da última sincronização na Area_Cabecalho no formato "DD/MM/AAAA HH:MM" (dia/mês/ano com hora e minuto em relógio de 24 horas).
4. IF o campo `inventarioNome` for nulo e o campo `inventarioId` estiver disponível, THEN THE DashboardFragment SHALL exibir na Area_Cabecalho um texto substituto que identifique o inventário pelo valor de `inventarioId`.
5. IF o campo `timestampUltimaSincronizacao` for nulo, THEN THE DashboardFragment SHALL ocultar a área de timestamp da última sincronização na Area_Cabecalho.
6. IF os campos `inventarioNome` e `inventarioId` forem ambos nulos ou indisponíveis, THEN THE DashboardFragment SHALL exibir na Area_Cabecalho um texto substituto genérico indicando que não há inventário ativo identificável.

### Requirement 4: Gráfico de Evolução de Coletas

**User Story:** Como supervisor, quero visualizar a evolução das coletas ao longo do tempo, para que eu identifique tendências de progresso do inventário.

#### Acceptance Criteria

1. THE DashboardFragment SHALL exibir uma área de Grafico_Evolucao renderizada com a biblioteca MPAndroidChart.
2. WHEN o `uiState` emitir uma lista de evolução (`coletasEvolucao`) não vazia, THE DashboardFragment SHALL popular o Grafico_Evolucao com um ponto por item da lista, utilizando o campo `data` de cada EvolucaoColeta no eixo temporal e o campo `quantidade` como valor, e SHALL ocultar o Estado_Carregamento, o Estado_Vazio e o Estado_Erro na área do gráfico.
3. WHEN o pull-to-refresh for acionado, THE DashboardFragment SHALL solicitar a atualização dos dados do Grafico_Evolucao por meio de `loadColetasEvolucao(inventarioId)`.
4. WHILE o `uiState` indicar carregamento dos dados de evolução, THE DashboardFragment SHALL exibir um Estado_Carregamento na área do Grafico_Evolucao e SHALL não exibir o gráfico, o Estado_Vazio nem o Estado_Erro.
5. IF, após a conclusão do carregamento e sem erro, a lista `coletasEvolucao` estiver vazia, THEN THE DashboardFragment SHALL exibir um Estado_Vazio na área do Grafico_Evolucao em vez do gráfico.
6. IF o `uiState` indicar erro ao carregar os dados de evolução, THEN THE DashboardFragment SHALL exibir um Estado_Erro na área do Grafico_Evolucao com mensagem descritiva indicando a falha, SHALL não exibir o gráfico e SHALL preservar inalterados os dados de evolução previamente carregados em memória.

### Requirement 5: Hierarquia e Consistência dos Botões de Ação

**User Story:** Como coletor de campo, quero que a área de ações de coleta seja visualmente consistente e organizada, para que eu encontre e acione as ações com facilidade.

#### Acceptance Criteria

1. THE DashboardFragment SHALL exibir os quatro botões de ação de coleta (Scan Rápido, Coleta Manual, Coleta por Descrição, Visualizar Coletas) com valor de raio de canto idêntico entre os quatro botões.
2. THE DashboardFragment SHALL aplicar entre botões de ação adjacentes um espaçamento igual a um múltiplo positivo de 8dp em conformidade com o Sistema_de_Espacamento_8dp, mantendo o mesmo valor de espaçamento entre todos os pares de botões adjacentes.
3. THE DashboardFragment SHALL aplicar o mesmo estilo de tipografia TextAppearance_Tema aos rótulos dos quatro botões de ação.
4. THE DashboardFragment SHALL renderizar o botão da ação primária (Scan Rápido) com estilo preenchido (filled) e os três botões das ações secundárias (Coleta Manual, Coleta por Descrição, Visualizar Coletas) com um mesmo estilo não preenchido, distinto do estilo da ação primária.
5. WHEN um dos quatro botões de ação (Scan Rápido, Coleta Manual, Coleta por Descrição, Visualizar Coletas) for acionado, THE DashboardFragment SHALL executar a ação de navegação atualmente associada a esse botão, sem alterar o destino de navegação atual.

### Requirement 6: Polimento Visual e Estados de Tela

**User Story:** Como usuário do aplicativo, quero uma aparência consistente e feedback claro durante carregamento, vazio e erro, para que a experiência seja confiável e profissional.

#### Acceptance Criteria

1. THE DashboardFragment SHALL aplicar os estilos de tipografia do TextAppearance_Tema a todos os elementos textuais do Dashboard, sem definir tamanho, peso ou família de fonte diretamente fora do TextAppearance_Tema.
2. THE DashboardFragment SHALL aplicar a todos os elementos de layout do Dashboard margens e paddings cujos valores sejam múltiplos de 8dp, em conformidade com o Sistema_de_Espacamento_8dp.
3. WHILE o `uiState` indicar carregamento dos dados do Dashboard (`uiState.isLoading` verdadeiro), THE DashboardFragment SHALL exibir um Estado_Carregamento na forma de skeleton ou placeholder e SHALL não exibir o conteúdo populado do Dashboard, o Estado_Vazio nem o Estado_Erro.
4. IF, após a conclusão do carregamento (`uiState.isLoading` falso) e sem erro, a FonteEstatisticas não contiver um inventário identificável (campos `inventarioNome` e `inventarioId` ambos nulos), THEN THE DashboardFragment SHALL exibir um Estado_Vazio em vez do conteúdo populado e SHALL não exibir o Estado_Carregamento nem o Estado_Erro.
5. IF ocorrer erro ao carregar os dados do Dashboard, THEN THE DashboardFragment SHALL exibir um Estado_Erro por meio de `tvError` com mensagem indicando a falha, SHALL não exibir o conteúdo populado, o Estado_Carregamento nem o Estado_Vazio, e SHALL preservar inalterados os dados do Dashboard previamente carregados em memória.
6. THE DashboardFragment SHALL exibir, em qualquer momento, no máximo um dentre Estado_Carregamento, Estado_Vazio e Estado_Erro, exibindo o conteúdo populado do Dashboard apenas quando nenhum desses três estados estiver ativo.

### Requirement 7: Acessibilidade

**User Story:** Como usuário que depende de recursos de acessibilidade, quero descrições adequadas, alvos de toque amplos e contraste suficiente, para que eu consiga operar o Dashboard com conforto.

#### Acceptance Criteria

1. THE DashboardFragment SHALL fornecer `contentDescription` para os elementos visuais não textuais do Dashboard, incluindo ícones e o FAB_Voz.
2. THE DashboardFragment SHALL garantir que os elementos interativos do Dashboard possuam Alvo_de_Toque de no mínimo 48dp x 48dp.
3. THE DashboardFragment SHALL aplicar combinações de cor de texto e fundo que atendam ao contraste WCAG_AA onde for viável.

> Nota: A validação completa de conformidade WCAG requer teste manual com tecnologias assistivas e revisão de acessibilidade por especialista; este requisito cobre apenas as verificações automatizáveis/estruturais viáveis.

### Requirement 8: Preservação de Funcionalidades Existentes

**User Story:** Como usuário existente, quero que todas as funcionalidades atuais do Dashboard continuem funcionando após o redesign, para que nenhuma capacidade seja perdida.

#### Acceptance Criteria

1. WHEN o usuário acionar o pull-to-refresh, THE DashboardFragment SHALL disparar a atualização das estatísticas por meio do mecanismo de refresh existente do ViewModel.
2. WHILE os dados exibidos forem locais/offline, THE DashboardFragment SHALL exibir o Indicador_Offline com o comportamento atual baseado em `isOfflineData`.
3. WHEN o usuário acionar o FAB_Voz, THE DashboardFragment SHALL iniciar a busca por voz e exibir o Overlay_Voz.
4. WHEN o usuário cancelar a busca por voz, THE DashboardFragment SHALL ocultar o Overlay_Voz.
5. THE DashboardFragment SHALL obter todos os KPIs exibidos exclusivamente a partir da FonteEstatisticas, sem introduzir fontes de dados distintas para os mesmos valores.
6. THE DashboardFragment SHALL preservar as ações de navegação de Scan Rápido, Coleta Manual, Coleta por Descrição e Visualizar Coletas.

### Requirement 9: Restrições de Arquitetura e Contrato de Dados (Não-objetivos)

**User Story:** Como mantenedor do sistema, quero que o redesign respeite a arquitetura e os contratos existentes, para que a estabilidade do app e da API seja preservada.

#### Acceptance Criteria

1. THE DashboardFragment SHALL restringir-se à renderização da interface, sem conter lógica de negócio, em conformidade com Clean Architecture + MVVM.
2. THE Dashboard SHALL consumir os dados existentes do modelo DashboardStats sem alterar o contrato de dados desse modelo.
3. THE Dashboard SHALL preservar inalteradas as URLs dos endpoints da API mobile (prefixo `api/mobile/`), em conformidade com a regra de steering `endpoints-nao-alterar.md`.
4. THE Dashboard SHALL preservar o comportamento offline-first existente do aplicativo.
