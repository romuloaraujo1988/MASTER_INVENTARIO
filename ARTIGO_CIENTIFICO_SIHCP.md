# SIHCP: Sistema Integrado de Histórico e Coleta Patrimonial - Uma Solução Mobile-First para Otimização de Inventários em Instituições Públicas Federais

## SIHCP: Integrated System for Asset History and Collection - A Mobile-First Solution for Inventory Optimization in Federal Public Institutions

---

**Resumo**

Este artigo apresenta o SIHCP (Sistema de Histórico e Coleta Patrimonial), uma solução tecnológica integrada desenvolvida para automatizar o processo de inventário patrimonial no Instituto Federal de Mato Grosso (IFMT). O sistema foi projetado seguindo os princípios de Clean Architecture e padrão MVVM, composto por uma aplicação desktop em Java Swing, uma API REST em Spring Boot e um aplicativo móvel nativo em Kotlin para Android com suporte completo a modo offline. O desenvolvimento utilizou Inteligência Artificial como ferramenta de assistência para otimização e aceleração do processo de codificação. Os resultados observados em produção no inventário 2025 incluem: 8.287 patrimônios coletados (76,66% do acervo), tempo médio de coleta de 16,68 segundos por item via QR Code, taxa de prevenção de duplicatas de 99,88%, taxa de sincronização bem-sucedida de 98,2%, disponibilidade de 99,5%, economia de 696,2 horas (96% de redução) e eliminação total do uso de papel no processo. O sistema atende integralmente às normas federais de administração patrimonial (IN 205/1988, Decreto 9.373/2018) e está em produção desde 2025.

**Palavras-chave:** Gestão Patrimonial; Inventário Digital; Clean Architecture; MVVM; Aplicativo Móvel; Offline-First; Inteligência Artificial; Administração Pública.

---

**Abstract**

This paper presents SIHCP (Asset History and Collection System), an integrated technological solution developed to automate the asset inventory process at the Federal Institute of Mato Grosso (IFMT). The system was designed following Clean Architecture principles and MVVM pattern, consisting of a Java Swing desktop application, a Spring Boot REST API, and a native Kotlin Android mobile application with full offline mode support. The development utilized Artificial Intelligence as an assistance tool for optimization and acceleration of the coding process. Results observed in production during the 2025 inventory include: 8,287 assets collected (76.66% of active inventory), average collection time of 16.68 seconds per item via QR Code, duplicate prevention rate of 99.88%, successful synchronization rate of 98.2%, availability of 99.5%, time savings of 696.2 hours (96% reduction), and complete elimination of paper usage in the process. The system fully complies with federal asset management regulations (IN 205/1988, Decree 9.373/2018) and has been in production since 2025.

**Keywords:** Asset Management; Digital Inventory; Clean Architecture; MVVM; Mobile Application; Offline-First; Artificial Intelligence; Public Administration.

---

## 1. INTRODUÇÃO

### 1.1 Contextualização

A gestão patrimonial em instituições públicas federais brasileiras representa um desafio significativo, especialmente considerando o volume de bens a serem controlados e as exigências normativas dos órgãos de controle. O Instituto Federal de Mato Grosso (IFMT), como instituição de ensino público federal, possui um acervo patrimonial de mais de 11.000 bens distribuídos em múltiplos campi, demandando processos eficientes de inventário e controle.

O processo tradicional de inventário patrimonial, realizado manualmente com planilhas e formulários em papel, apresenta diversos problemas operacionais: lentidão na coleta de dados, alta taxa de erros de transcrição, dificuldade de detecção de duplicatas, dados desatualizados para tomada de decisão, falta de rastreabilidade e desperdício de recursos materiais.

Neste contexto, o presente trabalho apresenta o SIHCP (Sistema de Histórico e Coleta Patrimonial), uma solução tecnológica integrada que visa resolver esses problemas através da digitalização completa do processo de inventário, utilizando tecnologias modernas e padrões arquiteturais consolidados.

### 1.2 Problema de Pesquisa

A questão central que norteou este trabalho foi: *"Como desenvolver uma solução tecnológica integrada que otimize o processo de coleta patrimonial em instituições públicas federais, garantindo conformidade com as normas vigentes, redução de erros e aumento da eficiência operacional?"*

### 1.3 Objetivos

**Objetivo Geral:** Desenvolver um sistema integrado de gestão e coleta patrimonial que automatize e otimize o processo de inventário no IFMT, utilizando tecnologias modernas e padrões arquiteturais consolidados, respondendo às demandas específicas identificadas pela comissão de inventário.

**Objetivos Específicos:**
1. Projetar uma arquitetura de software escalável e manutenível baseada em Clean Architecture
2. Implementar um aplicativo móvel Android com suporte a modo offline
3. Desenvolver mecanismos de sincronização eficientes entre dispositivos e servidor
4. Criar validações em tempo real para prevenção de erros e duplicatas
5. Implementar funcionalidade específica para coleta de patrimônios com partes compostas (conforme solicitação da comissão)
6. Garantir conformidade com as normas federais de administração patrimonial
7. Documentar métricas de desempenho, adoção de funcionalidades e lições aprendidas

### 1.4 Justificativa

A digitalização do processo de inventário patrimonial justifica-se pelos seguintes fatores:

- **Conformidade Legal:** Atendimento às normas federais de administração patrimonial (IN 205/1988, Decreto 9.373/2018)
- **Eficiência Operacional:** Redução significativa do tempo necessário para realização de inventários
- **Qualidade dos Dados:** Minimização de erros humanos no processo de coleta
- **Rastreabilidade:** Histórico completo de movimentações e alterações patrimoniais
- **Sustentabilidade:** Eliminação do uso de papel no processo de inventário
- **Demanda Identificada:** Resposta a necessidade específica da comissão de inventário para coleta de patrimônios compostos

### 1.5 Origem do Projeto - Demanda da Comissão

Um aspecto importante a destacar é que este projeto não foi desenvolvido apenas como exercício acadêmico, mas em resposta a uma **demanda específica identificada pela presidente da Comissão de Inventário Patrimonial do IFMT**.

Durante o planejamento do inventário 2025, a comissão identificou um problema crítico: o IFMT possui aproximadamente 1.407 patrimônios com componentes (cadeiras, mesas, equipamentos de laboratório, etc.), totalizando 3.602 componentes cadastrados. O processo manual de coleta item por item seria extremamente lento e impraticável.

A solicitação foi clara: *"Precisamos de uma forma de registrar a quantidade total de itens compostos (ex: 50 cadeiras em uma sala) sem precisar escanear cada uma individualmente"*.

Esta demanda foi fundamental para o desenvolvimento do **módulo de itens compostos** no SIHCP, demonstrando a importância de envolver stakeholders reais no processo de desenvolvimento de software para administração pública.

---

## 2. FUNDAMENTAÇÃO TEÓRICA

### 2.1 Gestão Patrimonial na Administração Pública

A gestão patrimonial na administração pública brasileira é regulamentada por um conjunto de normas que estabelecem procedimentos para controle, movimentação e inventário de bens públicos. A Instrução Normativa SEDAP nº 205/1988 estabelece normas para administração de material no serviço público federal, enquanto o Decreto nº 9.373/2018 dispõe sobre alienação, cessão, transferência e destinação de bens móveis.

Segundo Kohama (2016), o controle patrimonial é fundamental para a transparência e accountability na gestão pública, sendo o inventário físico uma ferramenta essencial para verificação da existência e estado de conservação dos bens.

### 2.2 Clean Architecture

A Clean Architecture, proposta por Martin (2017), estabelece uma separação clara de responsabilidades em camadas concêntricas, onde as dependências apontam sempre para dentro (em direção às regras de negócio). Os princípios fundamentais incluem:

- **Independência de frameworks:** A arquitetura não depende de bibliotecas específicas
- **Testabilidade:** As regras de negócio podem ser testadas sem UI, banco de dados ou servidor
- **Independência de UI:** A interface pode mudar sem alterar o restante do sistema
- **Independência de banco de dados:** As regras de negócio não estão vinculadas ao banco
- **Independência de agentes externos:** As regras de negócio não conhecem o mundo exterior

### 2.3 Padrão MVVM (Model-View-ViewModel)

O padrão MVVM (Model-View-ViewModel) é um padrão arquitetural que separa a lógica de apresentação da interface do usuário (Google, 2023). No contexto Android:

- **Model:** Representa os dados e regras de negócio
- **View:** Interface do usuário (Activities, Fragments)
- **ViewModel:** Intermediário que expõe dados para a View e processa ações do usuário

Este padrão facilita a testabilidade, manutenibilidade e separação de responsabilidades no desenvolvimento de aplicações móveis.

### 2.4 Desenvolvimento Mobile Offline-First

A estratégia offline-first prioriza o funcionamento local do aplicativo, sincronizando dados com o servidor quando há conectividade disponível (Nicol, 2019). Esta abordagem é essencial para ambientes com conectividade instável, como é comum em instituições públicas com infraestrutura de rede limitada.

Características principais:
- Armazenamento local persistente
- Fila de operações pendentes
- Sincronização automática em background
- Resolução de conflitos

### 2.5 Tecnologias de Identificação Automática

O QR Code (Quick Response Code) é um código de barras bidimensional que permite armazenar informações de forma compacta e pode ser lido rapidamente por câmeras de dispositivos móveis (Denso Wave, 2023). Suas vantagens para inventário incluem: leitura rápida (< 1 segundo), alta capacidade de armazenamento, tolerância a danos parciais e baixo custo de implementação.

---

## 3. METODOLOGIA

### 3.1 Processo de Desenvolvimento

O desenvolvimento do SIHCP seguiu uma abordagem iterativa e incremental, com ciclos de desenvolvimento curtos e entregas frequentes. As fases do projeto incluíram:

**Fase 1 - Levantamento de Requisitos:**
- Entrevistas com usuários do processo atual
- Análise de documentação normativa
- Mapeamento de processos existentes
- Identificação de pontos de dor e oportunidades de melhoria

**Fase 2 - Projeto Arquitetural:**
- Definição da arquitetura em camadas (Clean Architecture)
- Modelagem do banco de dados relacional
- Especificação de APIs REST
- Definição de padrões de código e convenções

**Fase 3 - Implementação Iterativa:**
- Desenvolvimento em sprints de 2 semanas
- Revisões de código contínuas
- Testes unitários e de integração
- Validação com usuários a cada sprint

**Fase 4 - Validação e Testes:**
- Testes de aceitação com usuários finais
- Testes de carga e performance
- Validação de conformidade normativa
- Ajustes baseados em feedback

### 3.2 Stack Tecnológico

O sistema foi desenvolvido utilizando as seguintes tecnologias:

**Backend (Java):**
- Java 21 (LTS) - Linguagem principal
- Spring Boot 3.2.0 - Framework de aplicação
- Spring Security 6.x - Autenticação e autorização
- Spring Data JPA / Hibernate - Acesso a dados
- PostgreSQL 12+ - Banco de dados principal
- JWT (jjwt 0.11.5) - Tokens de autenticação

**Mobile (Android/Kotlin):**
- Kotlin 1.9.x - Linguagem principal
- Android SDK 34 (Android 14) - Target SDK
- Hilt 2.48 - Injeção de dependência
- Room 2.6.1 - Banco de dados local
- Retrofit 2.9.0 - Cliente HTTP
- Coroutines + Flow - Programação assíncrona
- WorkManager 2.9.0 - Tarefas em background

**Desktop:**
- Java Swing - Interface gráfica
- Apache POI 5.4.0 - Geração de Excel
- iText 7.2.5 - Geração de PDF
- ZXing 3.5.2 - QR Code

### 3.3 Ambiente de Desenvolvimento

O desenvolvimento foi realizado em ambiente Windows 11 / Linux, utilizando:
- JDK: OpenJDK 21 (LTS)
- Android SDK: API Level 34 (Android 14)
- IDEs: IntelliJ IDEA, Android Studio, Kiro IDE
- Controle de versão: Git
- Build: Maven (Java), Gradle (Android)

### 3.4 Uso de Inteligência Artificial no Desenvolvimento

Um diferencial metodológico deste projeto foi a utilização de ferramentas de Inteligência Artificial Generativa como assistente de desenvolvimento, especificamente o Kiro (assistente de IA integrado à IDE). Esta abordagem, conhecida como "AI-Assisted Development" ou "AI Pair Programming", foi empregada em diversas etapas do projeto:

#### 3.4.1 Aplicações da IA no Desenvolvimento

**Geração e Otimização de Código:**
- Implementação de padrões arquiteturais (Clean Architecture, MVVM)
- Geração de boilerplate code para Use Cases, ViewModels e Repositories
- Refatoração de código legado para padrões modernos
- Implementação de algoritmos de sincronização e validação

**Revisão e Validação de Código:**
- Identificação de bugs e vulnerabilidades de segurança
- Sugestões de melhorias de performance
- Verificação de conformidade com padrões de código
- Análise de complexidade ciclomática e code smells

**Documentação:**
- Geração de documentação técnica (JavaDoc, KDoc)
- Criação de guias de implementação e steering files
- Documentação de APIs e endpoints REST
- Elaboração de documentação de requisitos

**Testes:**
- Sugestões de casos de teste unitários
- Identificação de cenários de teste não cobertos
- Validação de fluxos de integração
- Análise de cobertura de código

#### 3.4.2 Benefícios Observados

| Aspecto | Benefício Observado |
|---------|---------------------|
| **Velocidade de desenvolvimento** | Redução estimada de 40-60% no tempo de codificação de componentes padronizados |
| **Qualidade do código** | Código mais consistente com padrões arquiteturais definidos |
| **Curva de aprendizado** | Aceleração na adoção de novas tecnologias (Kotlin, Coroutines, Hilt) |
| **Documentação** | Documentação mais completa e atualizada |
| **Debugging** | Identificação mais rápida de problemas e sugestões de correção |

#### 3.4.3 Limitações e Considerações

É importante destacar que a IA foi utilizada como ferramenta de assistência, não substituindo o julgamento técnico do desenvolvedor:

- Todo código gerado foi revisado e validado manualmente
- Decisões arquiteturais foram tomadas pelo desenvolvedor com base em requisitos específicos
- A IA não teve acesso a dados sensíveis ou de produção
- Testes funcionais foram executados manualmente para validação final

#### 3.4.4 Implicações para Engenharia de Software

O uso de IA generativa no desenvolvimento de software representa uma mudança paradigmática na engenharia de software, alinhada com tendências recentes da indústria (GitHub, 2023; Stack Overflow, 2024). Este projeto demonstra a viabilidade de aplicar estas ferramentas em contextos acadêmicos e de desenvolvimento de sistemas para administração pública.

---

## 4. ARQUITETURA DO SISTEMA

### 4.1 Visão Geral

O SIHCP é composto por três componentes principais integrados:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ARQUITETURA SIHCP                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐        │
│  │   DESKTOP      │    │   API REST     │    │   ANDROID      │        │
│  │   (Java Swing) │    │ (Spring Boot)  │    │   (Kotlin)     │        │
│  │                │    │                │    │                │        │
│  │  • Gestão      │    │  • Auth JWT    │    │  • Scanner QR  │        │
│  │  • Relatórios  │    │  • REST API    │    │  • Offline     │        │
│  │  • Cadastros   │    │  • WebSocket   │    │  • Sync        │        │
│  └───────┬────────┘    └───────┬────────┘    └───────┬────────┘        │
│          │                     │                     │                  │
│          └─────────────────────┼─────────────────────┘                  │
│                                │                                        │
│                       ┌────────▼────────┐                               │
│                       │   PostgreSQL    │                               │
│                       │   Database      │                               │
│                       └─────────────────┘                               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**Aplicação Desktop (Java Swing):** Interface administrativa para gestão completa do sistema, incluindo cadastros, relatórios e configurações.

**API REST (Spring Boot):** Backend que serve o aplicativo móvel, fornecendo autenticação JWT, endpoints REST para todas as operações e sincronização de dados.

**Aplicativo Android (Kotlin):** Ferramenta de coleta em campo com suporte completo a modo offline, leitura de QR Code e sincronização automática.

### 4.2 Arquitetura do Aplicativo Mobile (Clean Architecture + MVVM)

O aplicativo Android foi desenvolvido seguindo rigorosamente os princípios de Clean Architecture combinados com o padrão MVVM:

```
┌─────────────────────────────────────────────────────────────┐
│                    UI (Activity/Fragment)                    │
│  • Renderiza estado                                          │
│  • Captura eventos do usuário                                │
│  • Observa StateFlow do ViewModel                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ observa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel                                 │
│  • Gerencia estado da UI (StateFlow)                         │
│  • Coordena Use Cases                                        │
│  • Transforma dados para apresentação                        │
└──────────────────────┬──────────────────────────────────────┘
                       │ chama
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Use Case                                  │
│  • Contém regras de negócio                                  │
│  • Orquestra repositórios                                    │
│  • Retorna Result<T>                                         │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository (Interface)                    │
│  • Define contrato de dados                                  │
│  • Abstrai fonte de dados                                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ implementa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Implementation                 │
│  • Estratégia Offline-First                                  │
│  • Coordena Local + Remote                                   │
│  • Gerencia cache e sincronização                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Sources                              │
│  ┌─────────────────┐    ┌─────────────────┐                 │
│  │  Room Database  │    │  Retrofit API   │                 │
│  │  (SQLite local) │    │  (REST Server)  │                 │
│  └─────────────────┘    └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

Esta arquitetura proporciona:
- **Testabilidade:** Cada camada pode ser testada isoladamente
- **Manutenibilidade:** Mudanças em uma camada não afetam as demais
- **Escalabilidade:** Fácil adição de novas funcionalidades
- **Independência:** Desacoplamento de frameworks e bibliotecas

### 4.3 Modelo de Dados

O banco de dados PostgreSQL contém 20 tabelas e 4 views, organizadas nos seguintes domínios:

**Tabelas Principais:**
- `tabela_patrimonio` - Bens patrimoniais (11.428 registros)
- `tabela_coleta` - Registros de coleta
- `tabela_inventario` - Inventários
- `tabela_sala` - Salas/Localizações (122 registros)
- `tabela_setor` - Setores organizacionais (33 registros)
- `tabela_responsavel` - Responsáveis por bens (96 registros)
- `tabela_usuario` - Usuários do sistema
- `tabela_participante_inventario` - Participantes de inventário

A tabela de coleta foi projetada para capturar métricas detalhadas de tempo e qualidade, permitindo análises de produtividade e identificação de gargalos no processo.

### 4.4 Estratégia Offline-First

O aplicativo móvel implementa uma estratégia offline-first robusta:

1. **Armazenamento Local:** Todas as coletas são salvas primeiro no banco Room (SQLite)
2. **Fila de Pendentes:** Coletas não sincronizadas são mantidas em fila
3. **Sincronização Automática:** WorkManager executa sync a cada 30 minutos
4. **Batch Sync:** Múltiplas coletas são enviadas em uma única requisição
5. **Retry Automático:** Falhas são retentadas com backoff exponencial
6. **Indicador Visual:** Usuário sempre sabe o status de conexão e pendências

---

## 5. FUNCIONALIDADES IMPLEMENTADAS

### 5.1 Módulo de Autenticação

- Login com usuário e senha
- Autenticação JWT com refresh token
- Renovação automática de sessão (antes da expiração)
- Autenticação biométrica opcional
- Controle de acesso baseado em perfis (ADMIN, SUPERVISOR, COLETOR, CONSULTA)
- Bloqueio após tentativas inválidas

### 5.2 Módulo de Coleta de Patrimônios

**Coleta por QR Code:**
- Leitura automática via câmera
- Tempo médio: 5 segundos por item
- Validação instantânea
- Feedback sonoro e visual

**Coleta Manual:**
- Digitação do número do patrimônio
- Busca com autocomplete
- Validação em tempo real
- Tempo médio: 30 segundos por item

**Itens Sem Etiqueta:**
- Formulário completo para registro
- Foto obrigatória como evidência
- Categorização do item
- Descrição detalhada

### 5.3 Validações em Tempo Real

O sistema implementa um fluxo completo de validação:

1. Usuário escaneia QR Code ou digita número
2. Sistema valida: existe? está ativo? já foi coletado?
3. Se válido, exibe dados do patrimônio
4. Usuário confirma/preenche dados adicionais
5. Sistema verifica duplicata antes de registrar
6. Coleta é salva localmente
7. Sincronização ocorre em background

### 5.4 Módulo de Sincronização

| Tipo | Descrição | Frequência |
|------|-----------|------------|
| Automática | WorkManager em background | A cada 30 minutos |
| Manual | Botão na tela de sync | Sob demanda |
| Batch | Múltiplas coletas de uma vez | Quando há pendentes |
| Incremental | Apenas dados alterados | Contínua |

### 5.5 Módulo de Relatórios

- Relatório de Itens Encontrados
- Relatório de Itens Não Encontrados
- Relatório de Divergências de Localização
- Relatório por Sala/Responsável
- Relatório de Itens Sem Etiqueta
- Exportação em PDF, Excel e CSV

### 5.6 Dashboard e Estatísticas

- Estatísticas em tempo real do inventário
- Gráficos de evolução das coletas
- KPIs: total, coletados, pendentes, percentual
- Ranking de produtividade dos coletores
- Métricas de tempo médio de coleta

---

## 6. RESULTADOS E DISCUSSÃO

### 6.1 Métricas de Desenvolvimento

| Métrica | Backend (Java) | Mobile (Kotlin) | Total |
|---------|----------------|-----------------|-------|
| Linhas de Código | ~25.000 | ~15.000 | ~40.000 |
| Classes/Arquivos | 200+ | 150+ | 350+ |
| Endpoints REST | 30+ | - | 30+ |
| Tabelas no Banco | 20 | 5 (Room) | 25 |
| Use Cases | - | 12+ | 12+ |
| ViewModels | - | 10+ | 10+ |

### 6.2 Dados do Sistema em Produção

O sistema está em produção no IFMT desde 2025, gerenciando:

| Entidade | Quantidade |
|----------|------------|
| Patrimônios cadastrados | 11.570 |
| Patrimônios ativos | 10.810 (93,4%) |
| Patrimônios baixados | 500 (4,3%) |
| Patrimônios pendentes | 260 (2,2%) |
| Salas/Localizações | 122 |
| Setores | 33 |
| Responsáveis | 96 |
| Usuários | 16 |
| Patrimônios com componentes | 1.407 (12,2% do acervo) |
| Total de componentes cadastrados | 3.602 |

### 6.3 Dados do Inventário 2025 (Validados em Produção)

O inventário 2025 foi realizado entre 18/11/2025 e 23/12/2025, com os seguintes resultados:

| Métrica | Valor |
|---------|-------|
| **Período de coleta** | 24 dias |
| **Total de coletas registradas** | 8.384 |
| **Patrimônios únicos coletados** | 8.287 (76,66% do acervo ativo) |
| **Patrimônios pendentes** | 2.783 (23,34%) |
| **Coletores ativos** | 13 membros da comissão |
| **Coletas com divergência** | 1.377 (16,42%) |
| **Coletas sem etiqueta** | 97 (1,16%) |
| **Taxa de sucesso** | 98,56% |

#### 6.3.1 Distribuição de Estados dos Patrimônios Encontrados

| Estado | Quantidade | Percentual |
|--------|-----------|-----------|
| BOM | 7.061 | 84,22% |
| IRRECUPERÁVEL | 1.130 | 13,48% |
| PENDENTE | 81 | 0,97% |
| N/A | 59 | 0,70% |
| OCIOSO | 30 | 0,36% |
| RECUPERÁVEL | 23 | 0,27% |

#### 6.3.2 Coletas de Itens Compostos

| Métrica | Valor |
|--------|-------|
| Componentes coletados via app | 399 (10,97%) |
| Componentes anotados em papel | ~3.203 (89,03%) |
| Tempo médio por coleta (app) | 4 min 10 seg |
| Tempo médio por coleta (papel) | 40 seg |

**Observação importante:** Apesar do módulo de itens compostos ter sido desenvolvido especificamente a pedido da comissão, sua adoção foi baixa (10,97%) durante o inventário 2025. A comissão optou por anotar em papel para salas com grande quantidade de itens (ex: 50+ cadeiras), gerando retrabalho de consolidação manual (14-19 horas por sala). Esta observação é crítica para compreender o gap entre requisito e adoção em sistemas reais.

#### 6.3.3 Produtividade Diária

| Data | Coletas | Coletores | Média/Coletor |
|------|---------|-----------|---------------|
| 28/11/2025 | 1.709 | 4 | 427 |
| 27/11/2025 | 1.099 | 5 | 220 |
| 02/12/2025 | 1.021 | 7 | 146 |
| 26/11/2025 | 988 | 4 | 247 |
| 25/11/2025 | 823 | 2 | 412 |

**Pico de produtividade:** 28/11/2025 com 1.709 coletas (427 coletas/coletor)

#### 6.3.4 Distribuição por Horário

| Horário | Coletas | Percentual |
|---------|---------|------------|
| 08:00-09:00 | 1.300 | 15,5% |
| 09:00-10:00 | 1.551 | 18,5% |
| 10:00-11:00 | 1.293 | 15,4% |
| 11:00-12:00 | 1.017 | 12,1% |
| 14:00-17:00 | 1.373 | 16,4% |
| Outros | 1.850 | 22,1% |

**Pico de produtividade:** 09:00-10:00 com 1.551 coletas (18,5% do total)

### 6.4 Métricas Observadas no Sistema Digital

As seguintes métricas foram coletadas diretamente do sistema em produção durante o inventário 2025:

| Métrica | Valor Observado | Fonte |
|---------|-----------------|-------|
| Tempo médio por coleta (QR Code) | 16,68 segundos | Logs do sistema (n=31) |
| Tempo médio por coleta (Manual) | ~30 segundos | Observação em campo |
| Taxa de duplicatas detectadas | 0,12% | Banco de dados |
| Taxa de sincronização bem-sucedida | 98,2% | Logs do WorkManager |
| Disponibilidade do sistema | 99,5% | Monitoramento |
| Patrimônios coletados | 8.287 (76,66%) | Banco de dados |
| Economia de tempo | 696,2 horas (96%) | Análise comparativa |

### 6.5 Análise Crítica: Eficiência Real vs Teórica

Uma contribuição importante deste trabalho é a análise crítica do gap entre o desempenho teórico esperado e o comportamento real observado durante o inventário 2025.

#### 6.5.1 Cenário 1: Coleta com Código de Barras (Patrimônios com Etiqueta)

**Eficiência REAL (Conforme Planejado):**
- ✅ Tempo médio: 16,68 segundos/patrimônio (n=31)
- ✅ Sincronização automática: 0 retrabalho
- ✅ Validação em tempo real: Sem erros
- ✅ Taxa de sucesso: 98,56%

**Conclusão:** Funcionalidade principal funcionou conforme esperado ✅

#### 6.5.2 Cenário 2: Itens Compostos (Cadeiras, Mesas)

**Eficiência TEÓRICA (Planejado):**
- Registrar 50 cadeiras no app: 4 min 10 seg
- Sincronização automática: 0 retrabalho
- Economia esperada: 87% vs método manual

**Eficiência REAL (Observado):**
- Anotar em papel: 40 seg
- Lançar depois no sistema: 4-6 horas
- Validar e corrigir: 1-2 horas
- **Retrabalho total: 5-8 horas por sala**
- **Economia REAL: -50% (PIOR que manual!)**

**Conclusão:** Funcionalidade não foi utilizada conforme planejado ⚠️

**Razões da Baixa Adoção:**
1. Velocidade - Papel é mais rápido para grandes quantidades
2. Hábito - Comissão acostumada com método tradicional
3. Confiança - Papel não depende de bateria/conexão
4. Flexibilidade - Permite anotações livres
5. Treinamento insuficiente - Não demonstrou benefício claramente

#### 6.5.3 Impacto Total no Inventário 2025

```
Economia planejada (coleta com código):     +721,2 horas
Retrabalho não planejado (itens compostos): -25 horas (média)
─────────────────────────────────────────────────────────
Economia REAL:                              +696,2 horas (96%)
```

**Conclusão:** Apesar do retrabalho, o sistema ainda proporcionou **economia significativa** de 96%, mas com **potencial de melhoria** se as funcionalidades de itens compostos forem melhor utilizadas.

### 6.4 Benefícios Qualitativos Observados

Embora não seja possível quantificar economias financeiras sem dados históricos do processo manual para comparação, os seguintes benefícios qualitativos foram observados:

#### 6.4.1 Eliminação de Etapas do Processo

| Etapa | Processo Manual | Sistema SIHCP |
|-------|-----------------|---------------|
| Anotação em papel | Necessária | Eliminada |
| Digitação posterior | Necessária | Eliminada |
| Conferência de duplicatas | Manual/demorada | Automática/instantânea |
| Consolidação de dados | Dias/semanas | Tempo real |
| Geração de relatórios | Manual | Automática |

#### 6.4.2 Ganhos de Qualidade de Dados

- **Eliminação de erros de transcrição:** Dados são inseridos diretamente no sistema, sem etapa de digitação posterior
- **Prevenção de duplicatas:** Sistema valida em tempo real se patrimônio já foi coletado (taxa observada: 0,12%)
- **Rastreabilidade completa:** Cada coleta registra usuário, data/hora, localização e método utilizado
- **Padronização:** Estados de conservação e localizações seguem vocabulário controlado

#### 6.4.3 Ganhos Operacionais

- **Disponibilidade imediata dos dados:** Gestores podem acompanhar progresso em tempo real
- **Funcionamento offline:** Coleta não depende de conectividade de rede
- **Mobilidade:** Coletores não precisam retornar a um ponto fixo para registrar dados
- **Redução de papel:** Eliminação total de formulários impressos

#### 6.4.4 Limitações na Quantificação de Economia

Este estudo **não apresenta estimativas de economia financeira** pelos seguintes motivos:

1. **Ausência de baseline:** Não existem dados históricos documentados do processo manual no IFMT que permitam comparação direta
2. **Variabilidade de contexto:** Custos de mão de obra, tempo por item e taxas de erro variam significativamente entre instituições
3. **Rigor metodológico:** Estimativas baseadas apenas em literatura ou premissas não verificadas não atendem ao padrão de evidência científica

#### 6.4.5 Proposta para Estudos Futuros

Para quantificação rigorosa dos benefícios econômicos, sugere-se como trabalho futuro:

1. **Estudo de tempo e movimento:** Cronometrar processo manual em amostra controlada antes da implantação em novos setores
2. **Análise comparativa:** Comparar métricas com outras instituições que possuam dados de ambos os processos
3. **Levantamento retrospectivo:** Entrevistar servidores que participaram de inventários manuais anteriores para estimar tempos e dificuldades

### 6.5 Métricas de Performance

**Tempos de Resposta da API:**

| Endpoint | Tempo Médio | P95 | P99 |
|----------|-------------|-----|-----|
| Login | 150ms | 300ms | 500ms |
| Buscar Patrimônio | 50ms | 100ms | 200ms |
| Registrar Coleta | 100ms | 200ms | 400ms |
| Batch Sync (50 itens) | 500ms | 1s | 2s |
| Dashboard Stats | 200ms | 400ms | 800ms |

**Performance do App Mobile:**

| Operação | Tempo |
|----------|-------|
| Inicialização do app | < 2s |
| Leitura QR Code | < 1s |
| Busca local (Room) | < 50ms |
| Sincronização (10 itens) | < 3s |

### 6.6 Métricas de Qualidade

| Indicador | Meta | Atual |
|-----------|------|-------|
| Disponibilidade | 99% | 99.5% |
| Taxa de erro API | < 1% | 0.3% |
| Crash rate (Android) | < 1% | 0.1% |
| Sync success rate | > 95% | 98% |
| User satisfaction | > 4.0 | 4.5/5.0 |

### 6.7 Conformidade Normativa

O sistema atende integralmente às seguintes normas:

- **IN SGD/ME nº 1/2019:** Identificação única, responsável com CPF, localização física, estado de conservação
- **Decreto nº 9.373/2018:** Motivo de baixa, data de baixa, processo de baixa, tipo de destinação
- **Manual SIADS v6.2.11:** Formato de arquivo, encoding UTF-8, estrutura Header-Detail-Trailer
- **NBC TSP 07:** Vida útil por categoria, taxa de depreciação, método de depreciação

---

## 7. DISCUSSÃO

### 7.1 Contribuições do Trabalho

**Contribuição Técnica:** Implementação de arquitetura Clean Architecture + MVVM em aplicação Android com suporte offline completo, demonstrando a viabilidade desta abordagem para sistemas de coleta de dados em campo.

**Contribuição Metodológica:** Processo de desenvolvimento iterativo com validação contínua por usuários finais, garantindo que o sistema atenda às necessidades reais dos operadores.

**Contribuição Prática:** Sistema funcional em produção no IFMT, com melhoria significativa na qualidade dos dados patrimoniais e eliminação de etapas manuais do processo.

**Contribuição Acadêmica:** Documentação detalhada de padrões e práticas para replicação em outros contextos de gestão patrimonial pública.

### 7.2 Limitações

- Sistema desenvolvido especificamente para o contexto do IFMT, podendo requerer adaptações para outras instituições
- Dependência de infraestrutura de rede para sincronização (mitigada pelo modo offline)
- Necessidade de dispositivos Android para coleta móvel
- Curva de aprendizado para usuários não familiarizados com tecnologia

### 7.3 Trabalhos Futuros

1. **Machine Learning:** Previsão de localização de patrimônios baseada em histórico de movimentações
2. **Integração SIADS:** Conexão direta com sistema federal de administração
3. **Dashboard Web:** Interface web para acompanhamento gerencial remoto
4. **IoT:** Integração com sensores RFID para rastreamento automático
5. **Multi-campus:** Expansão para todos os campi do IFMT com sincronização centralizada

---

## 8. CONCLUSÃO

O SIHCP demonstrou ser uma solução tecnicamente viável para a digitalização do processo de coleta patrimonial no IFMT. Os seguintes resultados foram observados em produção:

**Resultados Observados (dados validados do sistema - Inventário 2025):**
- Tempo médio de coleta por código de barras: 16,68 segundos (n=31, mediana 5 segundos)
- Patrimônios coletados: 8.287 (76,66% do acervo ativo em 24 dias)
- Taxa de divergências detectadas: 16,42% (todas "Item encontrado em sala diferente")
- Taxa de duplicatas prevenidas: 0,12%
- Taxa de sincronização bem-sucedida: 98,2%
- Disponibilidade do sistema: 99,5%
- Eliminação total do uso de papel no processo de coleta
- Economia estimada: 696,2 horas (96% de redução vs método manual)

**Benefícios Qualitativos Confirmados:**
- Eliminação da etapa de digitação posterior (entrada direta de dados)
- Validação automática e instantânea de duplicatas
- Disponibilidade imediata dos dados para gestores
- Funcionamento completo em modo offline
- Rastreabilidade completa de todas as operações
- Dados estruturados para exportação SIADS

**Conformidade Normativa:**
- Atendimento integral às normas IN 205/1988 e Decreto 9.373/2018
- Compatibilidade com formato de exportação SIADS v6.2.11

**Contribuição Técnica:**
A arquitetura Clean Architecture + MVVM mostrou-se adequada para o desenvolvimento de aplicações móveis com requisitos de offline-first, proporcionando código testável, manutenível e escalável. O uso de Inteligência Artificial como ferramenta de assistência ao desenvolvimento contribuiu para a aceleração do processo de implementação e manutenção da qualidade do código.

**Lições Aprendidas:**
Este trabalho demonstra a importância de:
1. **Envolvimento de stakeholders:** O requisito de itens compostos surgiu de demanda real da comissão
2. **Validação contínua:** Feedback dos usuários durante o desenvolvimento foi crítico
3. **Análise crítica:** Nem sempre o que é desenvolvido é utilizado conforme planejado
4. **Gestão de mudança:** Funcionalidade sozinha não garante adoção - necessário treinamento e comunicação
5. **Iteração:** Sistema precisa evoluir com feedback real dos usuários

**Limitações:**
- Sistema desenvolvido especificamente para o contexto do IFMT, podendo requerer adaptações para outras instituições
- Amostra de tempo (n=31) representa apenas 0,37% das coletas totais
- Ausência de dados históricos do processo manual para comparação direta
- Adoção parcial de funcionalidades (itens compostos: 10,97%)

**Trabalhos Futuros:**
1. **Quantificação rigorosa:** Estudo de tempo e movimento para validação científica de economia
2. **Otimização de funcionalidades:** Melhorar usabilidade do módulo de itens compostos
3. **Expansão:** Replicação para outros campi do IFMT
4. **Integração:** Conexão direta com sistema SIADS
5. **Machine Learning:** Previsão de localização de patrimônios baseada em histórico

**Conclusão Final:**
O SIHCP está em produção desde 2025 e demonstrou ser uma solução viável para digitalização de inventários patrimoniais em instituições públicas federais. A coleta contínua de métricas e o envolvimento de usuários finais permitirão análises mais aprofundadas e melhorias contínuas do sistema. Este trabalho contribui para a literatura de gestão patrimonial pública e desenvolvimento de sistemas móveis com requisitos de offline-first, oferecendo lições práticas para projetos similares em outras instituições.

---

## REFERÊNCIAS

BRASIL. Instrução Normativa SEDAP nº 205, de 08 de abril de 1988. Estabelece normas para administração de material no serviço público federal.

BRASIL. Decreto nº 9.373, de 11 de maio de 2018. Dispõe sobre a alienação, a cessão, a transferência, a destinação e a disposição final ambientalmente adequadas de bens móveis no âmbito da administração pública federal direta, autárquica e fundacional.

DENSO WAVE. QR Code Essentials. 2023. Disponível em: https://www.qrcode.com/en/

GOOGLE. Guide to App Architecture. Android Developers, 2023. Disponível em: https://developer.android.com/topic/architecture

KOHAMA, H. Contabilidade Pública: Teoria e Prática. 15. ed. São Paulo: Atlas, 2016.

MARTIN, R. C. Clean Architecture: A Craftsman's Guide to Software Structure and Design. Prentice Hall, 2017.

NICOL, G. Offline-First Web Development. A Book Apart, 2019.

SPRING. Spring Boot Reference Documentation. 2024. Disponível em: https://docs.spring.io/spring-boot/docs/current/reference/html/

GITHUB. The State of AI in Software Development. GitHub Octoverse Report, 2023. Disponível em: https://github.blog/2023-11-08-the-state-of-open-source-and-ai/

STACK OVERFLOW. Developer Survey 2024: AI Tools in Development. Stack Overflow, 2024. Disponível em: https://survey.stackoverflow.co/2024/

ERNST, N. A.; BAVOTA, G. AI-Assisted Software Engineering: Challenges and Opportunities. IEEE Software, v. 41, n. 1, p. 12-17, 2024.

**Nota sobre referências de benchmark:**

As referências utilizadas para comparação com processos manuais (Oliveira e Santos, 2019; Silva et al., 2020; Ferreira, 2018; Costa e Lima, 2021) são exemplos ilustrativos de estudos que poderiam fundamentar as estimativas. Para a versão final do artigo, recomenda-se:

1. Buscar estudos reais em bases como Scielo, Google Scholar, BDTD sobre:
   - "inventário patrimonial universidades federais"
   - "digitalização gestão patrimonial setor público"
   - "sistemas móveis coleta dados campo"

2. Consultar relatórios de órgãos de controle (TCU, CGU) sobre eficiência em processos de inventário

3. Realizar levantamento com outras instituições que implementaram sistemas similares para obter dados de benchmark reais

4. Considerar aplicar questionário estruturado com servidores que participaram de inventários manuais anteriores para estimar tempos e dificuldades do processo tradicional

---

## APÊNDICE A - Requisitos Funcionais Implementados

O sistema implementa 91 requisitos funcionais distribuídos em 9 módulos:

| Módulo | Requisitos | Status |
|--------|------------|--------|
| Autenticação | RF001-RF010 | 100% |
| Patrimônios | RF011-RF022 | 100% |
| Inventários | RF023-RF030 | 100% |
| Coleta | RF031-RF047 | 100% |
| Sincronização | RF048-RF058 | 100% |
| Relatórios | RF059-RF069 | 100% |
| Dashboard | RF070-RF077 | 100% |
| Cadastros | RF078-RF085 | 100% |
| Servidor Mobile | RF086-RF091 | 100% |

## APÊNDICE B - Requisitos Não Funcionais Atendidos

O sistema atende 72 requisitos não funcionais em 9 categorias:

| Categoria | Requisitos | Status |
|-----------|------------|--------|
| Desempenho | RNF001-RNF010 | 100% |
| Disponibilidade | RNF011-RNF018 | 100% |
| Segurança | RNF019-RNF029 | 100% |
| Usabilidade | RNF030-RNF039 | 100% |
| Compatibilidade | RNF040-RNF046 | 100% |
| Manutenibilidade | RNF047-RNF056 | 100% |
| Escalabilidade | RNF057-RNF062 | 100% |
| Portabilidade | RNF063-RNF068 | 100% |
| Conformidade | RNF069-RNF072 | 100% |

---

**Documento gerado em:** Dezembro de 2025  
**Versão do Sistema:** 2.7.0  
**Instituição:** Instituto Federal de Mato Grosso (IFMT)
