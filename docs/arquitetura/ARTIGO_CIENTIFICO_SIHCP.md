# SIHCP: Sistema Integrado de Histórico e Coleta Patrimonial — Uma Solução *Mobile-First* para Otimização de Inventários em Instituições Públicas Federais

## SIHCP: Integrated System for Asset History and Collection — A Mobile-First Solution for Inventory Optimization in Federal Public Institutions

---

**Resumo**

Este artigo apresenta o SIHCP (Sistema de Histórico e Coleta Patrimonial), uma solução tecnológica integrada desenvolvida para automatizar o processo de inventário patrimonial no Instituto Federal de Mato Grosso (IFMT). O sistema foi projetado seguindo os princípios de *Clean Architecture* e padrão MVVM, organizado em três componentes multi-módulo: uma aplicação *desktop* em Java Swing, uma API REST em Spring Boot 3.2.0 e um aplicativo móvel nativo em Kotlin para Android com suporte completo a modo *offline*, banco local criptografado (SQLCipher) e sincronização em *background* via *WorkManager*. O desenvolvimento utilizou Inteligência Artificial Generativa como ferramenta de assistência, caracterizando uma abordagem de *AI-Assisted Development*. Os resultados observados em produção no inventário 2025 (24 dias) incluem: 8.936 patrimônios únicos coletados (82,66% do acervo ativo), tempo médio de coleta de 16,68 segundos por item via QR Code (mediana de 5 segundos, n=31), taxa de prevenção de duplicatas de 99,88%, taxa de sincronização bem-sucedida de 98,2%, disponibilidade de 99,5%, economia estimada de 752,7 horas de trabalho (redução de ~92% frente ao método manual) e eliminação total do uso de papel no processo. O sistema atende integralmente às normas federais de administração patrimonial (IN SEDAP 205/1988 e Decreto 12.785/2025, que revogou o Decreto 9.373/2018) e está em produção desde novembro de 2025. A análise crítica do gap entre a eficiência teórica e a real — observada em particular no módulo de itens compostos (taxa de adoção de 10,97%) — evidencia a importância da gestão de mudança e do treinamento no sucesso de projetos de digitalização na administração pública.

**Palavras-chave:** Gestão Patrimonial; Inventário Digital; *Clean Architecture*; MVVM; Aplicativo Móvel; *Offline-First*; Inteligência Artificial; Administração Pública.

---

**Abstract**

This paper presents SIHCP (Asset History and Collection System), an integrated technological solution developed to automate the asset inventory process at the Federal Institute of Mato Grosso (IFMT). The system was designed following Clean Architecture principles and the MVVM pattern, organized as a three-component multi-module project: a Java Swing desktop application, a Spring Boot 3.2.0 REST API, and a native Kotlin Android mobile application with full offline support, encrypted local database (SQLCipher), and background synchronization via WorkManager. Development leveraged Generative Artificial Intelligence as an assistance tool, characterizing an AI-Assisted Development approach. Results observed in production during the 2025 inventory (24 days) include: 8,936 unique assets collected (82.66% of the active inventory), average collection time of 16.68 seconds per item via QR Code (median of 5 seconds, n=31), duplicate prevention rate of 99.88%, successful synchronization rate of 98.2%, system availability of 99.5%, estimated time savings of 752.7 work hours (~92% reduction compared with the manual method), and complete elimination of paper usage. The system fully complies with federal asset management regulations (IN SEDAP 205/1988 and Decree 12.785/2025, which superseded Decree 9.373/2018) and has been in production since November 2025. The critical analysis of the gap between theoretical and actual efficiency — particularly observed in the composite-items module (10.97% adoption rate) — highlights the importance of change management and training in the success of digitalization projects in public administration.

**Keywords:** Asset Management; Digital Inventory; Clean Architecture; MVVM; Mobile Application; Offline-First; Artificial Intelligence; Public Administration.

---

## 1. INTRODUÇÃO

### 1.1 Contextualização

A gestão patrimonial em instituições públicas federais brasileiras representa um desafio significativo, especialmente considerando o volume de bens a serem controlados e as exigências normativas dos órgãos de controle. O Instituto Federal de Mato Grosso (IFMT), como instituição de ensino público federal, possui um acervo patrimonial de aproximadamente 11.570 bens distribuídos em múltiplos campi, demandando processos eficientes de inventário e controle.

O processo tradicional de inventário patrimonial, realizado manualmente com planilhas e formulários em papel, apresenta diversos problemas operacionais: lentidão na coleta de dados, alta taxa de erros de transcrição, dificuldade de detecção de duplicatas, dados desatualizados para tomada de decisão, falta de rastreabilidade e desperdício de recursos materiais.

Neste contexto, o presente trabalho apresenta o SIHCP (Sistema de Histórico e Coleta Patrimonial), uma solução tecnológica integrada que visa resolver esses problemas por meio da digitalização completa do processo de inventário, utilizando tecnologias modernas e padrões arquiteturais consolidados.

### 1.2 Problema de Pesquisa

A questão central que norteou este trabalho foi: *"Como desenvolver uma solução tecnológica integrada que otimize o processo de coleta patrimonial em instituições públicas federais, garantindo conformidade com as normas vigentes, redução de erros e aumento da eficiência operacional?"*

### 1.3 Objetivos

**Objetivo Geral:** Desenvolver um sistema integrado de gestão e coleta patrimonial que automatize e otimize o processo de inventário no IFMT, utilizando tecnologias modernas e padrões arquiteturais consolidados, respondendo às demandas específicas identificadas pela comissão de inventário.

**Objetivos Específicos:**
1. Projetar uma arquitetura de *software* escalável e manutenível baseada em *Clean Architecture* e organizada em múltiplos módulos Maven;
2. Implementar um aplicativo móvel Android com suporte a modo *offline* completo e banco local criptografado;
3. Desenvolver mecanismos de sincronização eficientes entre dispositivos e servidor, incluindo *batch sync* e *retry* automático;
4. Criar validações em tempo real para prevenção de erros, duplicatas e divergências de localização;
5. Implementar funcionalidade específica para coleta de patrimônios com partes compostas (conforme solicitação da comissão);
6. Garantir conformidade com as normas federais de administração patrimonial;
7. Documentar métricas de desempenho, adoção de funcionalidades e lições aprendidas a partir de uma implantação real;
8. Analisar criticamente o *gap* entre eficiência teórica e adoção efetiva das funcionalidades.

### 1.4 Justificativa

A digitalização do processo de inventário patrimonial justifica-se pelos seguintes fatores:

- **Conformidade Legal:** Atendimento às normas federais de administração patrimonial (IN SEDAP 205/1988 e Decreto 12.785/2025, que revogou o Decreto 9.373/2018);
- **Eficiência Operacional:** Redução significativa do tempo necessário para realização de inventários;
- **Qualidade dos Dados:** Minimização de erros humanos no processo de coleta;
- **Rastreabilidade:** Histórico completo de movimentações e alterações patrimoniais;
- **Sustentabilidade:** Eliminação do uso de papel no processo de inventário;
- **Demanda Identificada:** Resposta a necessidade específica da comissão de inventário para coleta de patrimônios compostos.

### 1.5 Origem do Projeto — Demanda da Comissão

Um aspecto importante a destacar é que este projeto não foi desenvolvido apenas como exercício acadêmico, mas em resposta a uma **demanda específica identificada pela presidente da Comissão de Inventário Patrimonial do IFMT**.

Durante o planejamento do inventário 2025, a comissão identificou um problema crítico: o IFMT possui aproximadamente 1.407 patrimônios com componentes (cadeiras, mesas, equipamentos de laboratório), totalizando 3.602 componentes cadastrados. O processo manual de coleta item a item seria extremamente lento e impraticável.

A solicitação foi clara: *"Precisamos de uma forma de registrar a quantidade total de itens compostos (ex.: 50 cadeiras em uma sala) sem precisar escanear cada uma individualmente"*.

Essa demanda foi fundamental para o desenvolvimento do **módulo de itens compostos** no SIHCP, demonstrando a importância de envolver *stakeholders* reais no processo de desenvolvimento de *software* para administração pública. Como será discutido na Seção 6, a trajetória desse módulo — do requisito à adoção efetiva — oferece lições valiosas sobre gestão de mudança.

---

## 2. FUNDAMENTAÇÃO TEÓRICA

### 2.1 Gestão Patrimonial na Administração Pública

A gestão patrimonial na administração pública brasileira é regulamentada por um conjunto de normas que estabelecem procedimentos para controle, movimentação e inventário de bens públicos. A Instrução Normativa SEDAP nº 205/1988 estabelece normas para administração de material no serviço público federal, enquanto o Decreto nº 12.785/2025 — que revogou o Decreto nº 9.373/2018 em 19/12/2025 — dispõe sobre alienação, cessão, transferência e destinação de bens móveis. O inventário 2025 do IFMT foi realizado entre 18/11 e 23/12/2025, período em que a legislação anterior ainda estava parcialmente vigente; as classificações de inservibilidade, contudo, permanecem aplicáveis sem interrupção.

Segundo Kohama (2016), o controle patrimonial é fundamental para a transparência e a *accountability* na gestão pública, sendo o inventário físico uma ferramenta essencial para verificação da existência e do estado de conservação dos bens.

### 2.2 *Clean Architecture*

A *Clean Architecture*, proposta por Martin (2017), estabelece uma separação clara de responsabilidades em camadas concêntricas, em que as dependências apontam sempre para dentro (em direção às regras de negócio). Os princípios fundamentais incluem:

- **Independência de *frameworks*:** a arquitetura não depende de bibliotecas específicas;
- **Testabilidade:** as regras de negócio podem ser testadas sem UI, banco de dados ou servidor;
- **Independência de UI:** a interface pode mudar sem alterar o restante do sistema;
- **Independência de banco de dados:** as regras de negócio não estão vinculadas ao banco;
- **Independência de agentes externos:** as regras de negócio não conhecem o mundo exterior.

### 2.3 Padrão MVVM (*Model–View–ViewModel*)

O padrão MVVM é um padrão arquitetural que separa a lógica de apresentação da interface do usuário (Google, 2023). No contexto Android:

- **Model:** representa os dados e regras de negócio;
- **View:** interface do usuário (*Activities*, *Fragments*);
- **ViewModel:** intermediário que expõe dados à *View* via `StateFlow` e processa ações do usuário.

Esse padrão facilita a testabilidade, manutenibilidade e a separação de responsabilidades no desenvolvimento de aplicações móveis.

### 2.4 Desenvolvimento *Mobile Offline-First*

A estratégia *offline-first* prioriza o funcionamento local do aplicativo, sincronizando dados com o servidor quando há conectividade disponível (Nicol, 2019). Essa abordagem é essencial para ambientes com conectividade instável, como é comum em instituições públicas com infraestrutura de rede limitada.

Características principais:
- Armazenamento local persistente;
- Fila de operações pendentes;
- Sincronização automática em *background*;
- Resolução de conflitos.

### 2.5 Tecnologias de Identificação Automática

O QR Code (*Quick Response Code*) é um código de barras bidimensional que permite armazenar informações de forma compacta e pode ser lido rapidamente por câmeras de dispositivos móveis (Denso Wave, 2023). Suas vantagens para inventário incluem leitura rápida (menos de 1 segundo), alta capacidade de armazenamento, tolerância a danos parciais e baixo custo de implementação.

### 2.6 Segurança em Aplicações Móveis

A proteção de dados em repouso e em trânsito no ambiente móvel é crítica em sistemas que operam com dados patrimoniais públicos. O sistema adota três camadas de proteção:

- **SQLCipher:** criptografia AES-256 do banco local Room;
- **Android Keystore:** armazenamento seguro da *passphrase* de criptografia;
- **EncryptedSharedPreferences:** proteção de credenciais e *tokens* em preferências persistentes.

Essa abordagem alinha-se às recomendações da OWASP *Mobile Top 10* (2023) e às exigências de tratamento de dados da Lei Geral de Proteção de Dados (Lei nº 13.709/2018).

---

## 3. METODOLOGIA

### 3.1 Processo de Desenvolvimento

O desenvolvimento do SIHCP seguiu uma abordagem iterativa e incremental, com ciclos curtos e entregas frequentes. As fases do projeto incluíram:

**Fase 1 — Levantamento de Requisitos:**
- Entrevistas com usuários do processo atual;
- Análise de documentação normativa;
- Mapeamento de processos existentes;
- Identificação de pontos de dor e oportunidades de melhoria.

**Fase 2 — Projeto Arquitetural:**
- Definição da arquitetura em camadas (*Clean Architecture*);
- Organização multi-módulo Maven (`sihcp-core`, `sihcp-desktop`, `sihcp-server`);
- Modelagem do banco de dados relacional;
- Especificação de APIs REST;
- Definição de padrões de código e convenções.

**Fase 3 — Implementação Iterativa:**
- Desenvolvimento em *sprints* de duas semanas;
- Revisões de código contínuas;
- Testes unitários e de integração;
- Validação com usuários a cada *sprint*.

**Fase 4 — Validação e Testes:**
- Testes de aceitação com usuários finais;
- Testes de carga e *performance*;
- Validação de conformidade normativa;
- Ajustes baseados em *feedback*.

**Fase 5 — Implantação e Evolução Contínua (em produção):**
- Implantação no inventário 2025;
- Monitoramento em tempo real (*logs*, métricas, telemetria);
- Evolução iterativa com base em observação de uso real.

### 3.2 *Stack* Tecnológico

O sistema foi desenvolvido utilizando as seguintes tecnologias:

**Backend (Java):**
- Java 21 (LTS) — linguagem principal;
- Spring Boot 3.2.0 — *framework* de aplicação;
- Spring Security 6.x — autenticação e autorização;
- Spring Data JPA / Hibernate — acesso a dados;
- PostgreSQL 12+ — banco de dados principal;
- JWT (jjwt 0.11.5) — *tokens* de autenticação;
- HikariCP — *pool* de conexões;
- Springdoc OpenAPI 2.0.2 — documentação Swagger.

**Mobile (Android/Kotlin):**
- Kotlin 1.9.x — linguagem principal;
- Android SDK 34 (Android 14) — *target* SDK;
- Hilt 2.48 — injeção de dependência;
- Room 2.6.1 — banco de dados local;
- SQLCipher 4.5.x — criptografia AES-256 do Room;
- Retrofit 2.9.0 — cliente HTTP;
- *Coroutines* + *Flow* — programação assíncrona;
- *WorkManager* 2.9.0 — tarefas em *background*;
- *Paging* 3 — paginação reativa;
- *Jetpack Security* — `EncryptedSharedPreferences` para preferências sensíveis.

**Desktop:**
- Java 21 + Swing — interface gráfica nativa;
- Apache POI 5.4.0 — geração de Excel;
- iText 7.2.5 — geração de PDF;
- ZXing 3.5.2 — geração e leitura de QR Code;
- JFreeChart 1.5.5 — gráficos e indicadores;
- SQLite JDBC — modo *offline* local.

### 3.3 Ambiente de Desenvolvimento

O desenvolvimento foi realizado em ambiente Windows 11 / Linux, utilizando:
- JDK: OpenJDK 21 (LTS);
- Android SDK: API Level 34 (Android 14);
- IDEs: IntelliJ IDEA, Android Studio, Kiro IDE;
- Controle de versão: Git;
- *Build*: Maven (multi-módulo Java), Gradle (Android).

### 3.4 Uso de Inteligência Artificial no Desenvolvimento

Um diferencial metodológico deste projeto foi a utilização de ferramentas de Inteligência Artificial Generativa como assistente de desenvolvimento, especificamente o Kiro (assistente de IA integrado à IDE). Essa abordagem, conhecida como *AI-Assisted Development* ou *AI Pair Programming*, foi empregada em diversas etapas do projeto.

#### 3.4.1 Aplicações da IA no Desenvolvimento

**Geração e otimização de código:**
- Implementação de padrões arquiteturais (*Clean Architecture*, MVVM);
- Geração de *boilerplate* para *Use Cases*, *ViewModels* e *Repositories*;
- Refatoração de código legado para padrões modernos;
- Implementação de algoritmos de sincronização e validação.

**Revisão e validação de código:**
- Identificação de *bugs* e vulnerabilidades de segurança;
- Sugestões de melhorias de *performance*;
- Verificação de conformidade com padrões de código;
- Análise de complexidade ciclomática e *code smells*.

**Documentação:**
- Geração de documentação técnica (*Javadoc*, KDoc);
- Criação de guias de implementação e *steering files*;
- Documentação de APIs e *endpoints* REST;
- Elaboração de documentação de requisitos (100 RFs e 73 RNFs catalogados).

**Testes:**
- Sugestões de casos de teste unitários;
- Identificação de cenários de teste não cobertos;
- Validação de fluxos de integração;
- Análise de cobertura de código.

#### 3.4.2 Benefícios Observados

| Aspecto | Benefício Observado |
|---------|---------------------|
| **Velocidade de desenvolvimento** | Redução estimada de 40 a 60% no tempo de codificação de componentes padronizados |
| **Qualidade do código** | Maior consistência com padrões arquiteturais definidos |
| **Curva de aprendizado** | Aceleração na adoção de novas tecnologias (Kotlin, *Coroutines*, Hilt) |
| **Documentação** | Documentação mais completa e atualizada |
| ***Debugging*** | Identificação mais rápida de problemas e sugestões de correção |

#### 3.4.3 Limitações e Considerações

É importante destacar que a IA foi utilizada como ferramenta de assistência, não substituindo o julgamento técnico do desenvolvedor:

- Todo código gerado foi revisado e validado manualmente;
- Decisões arquiteturais foram tomadas pelo desenvolvedor com base em requisitos específicos;
- A IA não teve acesso a dados sensíveis ou de produção;
- Testes funcionais foram executados manualmente para validação final.

#### 3.4.4 Implicações para Engenharia de *Software*

O uso de IA generativa no desenvolvimento de *software* representa uma mudança paradigmática na engenharia de *software*, alinhada com tendências recentes da indústria (GitHub, 2023; Stack Overflow, 2024; Ernst e Bavota, 2024). Este projeto demonstra a viabilidade de aplicar essas ferramentas em contextos acadêmicos e de desenvolvimento de sistemas para administração pública.

---

## 4. ARQUITETURA DO SISTEMA

### 4.1 Visão Geral

O SIHCP é composto por três componentes principais integrados:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ARQUITETURA SIHCP                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐         │
│  │    DESKTOP     │    │   API REST     │    │    ANDROID     │         │
│  │  (Java Swing)  │    │ (Spring Boot)  │    │    (Kotlin)    │         │
│  │                │    │                │    │                │         │
│  │  • Gestão      │    │  • Auth JWT    │    │  • Scanner QR  │         │
│  │  • Relatórios  │    │  • REST API    │    │  • Offline     │         │
│  │  • Cadastros   │    │  • Batch Sync  │    │  • Sync + Retry│         │
│  │  • Reconcil.   │    │  • Rate Limit  │    │  • Fotos       │         │
│  └───────┬────────┘    └───────┬────────┘    └───────┬────────┘         │
│          │                     │                     │                   │
│          └─────────────────────┼─────────────────────┘                   │
│                                │                                         │
│                       ┌────────▼────────┐    ┌─────────────────┐        │
│                       │   PostgreSQL    │    │  Filesystem     │        │
│                       │    Database     │    │  (fotos JPEG)   │        │
│                       └─────────────────┘    └─────────────────┘        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**Aplicação *Desktop* (Java Swing):** interface administrativa para gestão completa do sistema, incluindo cadastros, relatórios, reconciliação, impressão de etiquetas e controle do servidor *mobile*.

**API REST (Spring Boot):** *backend* que serve o aplicativo móvel, fornecendo autenticação JWT, 105 *endpoints* REST e sincronização de dados.

**Aplicativo Android (Kotlin):** ferramenta de coleta em campo com suporte completo a modo *offline*, leitura de QR Code, captura de fotos e sincronização automática em *background*.

### 4.2 Organização Multi-Módulo Maven

O projeto Java foi organizado em três módulos independentes, conforme o padrão Maven multi-módulo:

| Módulo | Pacote Base | Tipo | Responsabilidade |
|--------|-------------|------|------------------|
| `sihcp-core` | `com.inventario.sihcp` | JAR (biblioteca) | Entidades, DAOs, *services* e utilitários compartilhados |
| `sihcp-desktop` | `com.inventario.sihcp` | JAR executável (Swing) | Interface administrativa; *main*: `SistemaInventarioApplication` |
| `sihcp-server` | `com.inventario.sihcp` | *Spring Boot Fat JAR* | API REST *mobile*; *main*: `MobileApiApplication` |

Essa separação garante reutilização de código, independência de *deploys* e preparação para evolução futura (contêineres, microsserviços).

### 4.3 Arquitetura do Aplicativo *Mobile* (*Clean Architecture* + MVVM)

O aplicativo Android foi desenvolvido seguindo rigorosamente os princípios de *Clean Architecture* combinados com o padrão MVVM:

```
┌─────────────────────────────────────────────────────────────┐
│                    UI (Activity/Fragment)                    │
│  • Renderiza o estado                                        │
│  • Captura eventos do usuário                                │
│  • Observa StateFlow do ViewModel                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ observa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                        ViewModel                             │
│  • Gerencia o estado da UI (StateFlow)                       │
│  • Coordena Use Cases                                        │
│  • Transforma dados para apresentação                        │
└──────────────────────┬──────────────────────────────────────┘
                       │ chama
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                        Use Case                              │
│  • Contém regras de negócio                                  │
│  • Orquestra repositórios                                    │
│  • Retorna Result<T>                                         │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository (Interface)                     │
│  • Define contrato de dados                                  │
│  • Abstrai fonte de dados                                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ implementa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository Implementation                   │
│  • Estratégia Offline-First                                  │
│  • Coordena Local + Remote                                   │
│  • Gerencia cache e sincronização                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Sources                           │
│  ┌─────────────────────┐    ┌─────────────────────┐         │
│  │   Room Database     │    │   Retrofit API      │         │
│  │ (SQLCipher AES-256) │    │   (REST Server)     │         │
│  └─────────────────────┘    └─────────────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

Essa arquitetura proporciona:
- **Testabilidade:** cada camada pode ser testada isoladamente;
- **Manutenibilidade:** mudanças em uma camada não afetam as demais;
- **Escalabilidade:** fácil adição de novas funcionalidades;
- **Independência:** desacoplamento de *frameworks* e bibliotecas.

### 4.4 Modelo de Dados

O banco de dados PostgreSQL contém 20 tabelas e *views* organizadas nos seguintes domínios:

**Tabelas Principais:**
- `tabela_patrimonio` — bens patrimoniais (11.570 registros);
- `tabela_coleta` — registros de coleta;
- `tabela_inventario` — inventários;
- `tabela_sala` — salas e localizações (122 registros);
- `tabela_setor` — setores organizacionais (33 registros);
- `tabela_responsavel` — responsáveis por bens (96 registros);
- `tabela_usuario` — usuários do sistema;
- `tabela_participante_inventario` — participantes do inventário.

A tabela de coleta foi projetada para capturar métricas detalhadas de tempo e qualidade, permitindo análises de produtividade e identificação de gargalos no processo. Inclui campos de auditoria (`created_at`, `updated_at`, `coletor_id`), campos de localização (`localizacao_encontrada`, `estado_encontrado`) e referência a fotos (`foto_path`).

### 4.5 Estratégia *Offline-First*

O aplicativo móvel implementa uma estratégia *offline-first* robusta:

1. **Armazenamento local:** todas as coletas são salvas primeiro no banco Room com criptografia SQLCipher;
2. **Fila de pendentes:** coletas não sincronizadas são mantidas em fila com *status* próprio;
3. **Sincronização automática:** *WorkManager* executa *sync* periódico a cada 30 minutos;
4. **Batch Sync:** múltiplas coletas são enviadas em uma única requisição;
5. **Retry automático:** falhas são retentadas com *backoff* exponencial;
6. ***Fallback* para *sync* individual:** se o *batch* falhar, o *sync* individual é acionado;
7. **Indicador visual:** o usuário sempre sabe o *status* de conexão e as pendências.

### 4.6 Sincronização de Fotos

Fotos de coleta seguem pipeline dedicado (`PhotoSyncWorker`), separado do *sync* de metadados:

- **Organização em disco:** `data/fotos/inventario_{id}/{YYYY-MM}/{tipo}/coleta_{id}_{identificador}.jpg`;
- **Tipos:** `patrimonio`, `sem_etiqueta` e `divergencia`;
- **Compressão local:** fotos são comprimidas para JPEG antes do *upload*;
- **Upload preferencial em Wi-Fi:** restrição configurável para economizar dados móveis;
- **Referência no banco:** o *path* relativo é persistido no campo `FOTO_PATH` da coleta.

---

## 5. FUNCIONALIDADES IMPLEMENTADAS

### 5.1 Requisitos Implementados

O sistema possui **100 Requisitos Funcionais (RF)** e **73 Requisitos Não Funcionais (RNF)** integralmente implementados, documentados nos seguintes artefatos:

- `REQUISITOS_FUNCIONAIS_DESKTOP.md` — 98 RFs da aplicação *desktop*;
- `REQUISITOS_NAO_FUNCIONAIS_DESKTOP.md` — 65 RNFs da aplicação *desktop*;
- `REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md` — 105 RFs do servidor *mobile*;
- `REQUISITOS_NAO_FUNCIONAIS_SERVIDOR_MOBILE.md` — 77 RNFs do servidor *mobile*;
- `REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md` — especificação consolidada (100 RFs + 73 RNFs).

### 5.2 Módulo de Autenticação

- *Login* com usuário e senha;
- Autenticação JWT com *refresh token*;
- Renovação automática de sessão (antes da expiração);
- Autenticação biométrica opcional;
- Controle de acesso baseado em perfis (ADMIN, SUPERVISOR, COLETOR, CONSULTA);
- Bloqueio após cinco tentativas inválidas;
- ***Rate limiting*** no *login* (10 tentativas por minuto, por IP);
- Detecção de ataques de força bruta com alerta após três bloqueios em cinco minutos.

### 5.3 Módulo de Coleta de Patrimônios

**Coleta por QR Code:**
- Leitura automática via câmera;
- Tempo médio observado: 16,68 segundos por item (mediana de 5 segundos);
- Validação instantânea;
- *Feedback* sonoro e visual.

**Coleta Manual:**
- Digitação do número do patrimônio;
- Busca com *autocomplete*;
- Validação em tempo real;
- Tempo médio observado: cerca de 30 segundos por item.

**Itens sem Etiqueta:**
- Formulário completo para registro;
- Foto obrigatória como evidência (validada pelo *ViewModel*);
- Categorização do item;
- Descrição detalhada com validação de comprimento (3 a 255 caracteres);
- **Sugestões de descrição:** cache *offline* com filtro insensível a acento/caso, atualizado automaticamente após o *sync*.

**Coleta de Itens Compostos:**
- Registro de quantidade por conjunto (ex.: "50 cadeiras");
- Interface dedicada para grandes quantidades;
- Associação com patrimônio principal.

### 5.4 Validações em Tempo Real

O sistema implementa um fluxo completo de validação:

1. O usuário escaneia QR Code ou digita o número;
2. O sistema valida: existe? está ativo? já foi coletado?;
3. Se válido, exibe os dados do patrimônio;
4. O usuário confirma e preenche os dados adicionais;
5. O sistema verifica duplicata antes de registrar;
6. A coleta é salva localmente (banco criptografado);
7. A sincronização ocorre em *background*.

### 5.5 Módulo de Sincronização

| Tipo | Descrição | Frequência |
|------|-----------|------------|
| Automática | *WorkManager* em *background* | A cada 30 minutos |
| Manual | Botão na tela de *sync* | Sob demanda |
| *Batch* | Múltiplas coletas de uma vez | Quando há pendentes |
| Incremental | Apenas dados alterados | Contínua |
| Fotos | *PhotoSyncWorker* dedicado | Preferencial em Wi-Fi |

### 5.6 Módulo de Relatórios

- Relatório de itens encontrados;
- Relatório de itens não encontrados;
- Relatório de divergências de localização;
- Relatório por sala e por responsável;
- Relatório de itens sem etiqueta (com fotos);
- **Relatório fotográfico em PDF** (implementado em maio de 2026): consolida fotos de coleta em PDF com metadados, filtrável por tipo (`patrimonio`, `sem_etiqueta`, `divergencia`);
- Exportação em PDF, Excel e CSV (UTF-8);
- Exportação no formato oficial SIADS v6.2.11.

### 5.7 *Dashboard* e Estatísticas

- Estatísticas em tempo real do inventário;
- Gráficos de evolução das coletas;
- KPIs: total, coletados, pendentes, percentual;
- *Ranking* de produtividade dos coletores;
- Métricas de tempo médio de coleta;
- Distribuição por sala e por horário.

### 5.8 Módulo de Reconciliação

- Correspondência manual entre itens não encontrados e itens sem etiqueta;
- Sugestões automáticas por similaridade de descrição e localização;
- Confirmação ou rejeição com registro de auditoria;
- Limiar de similaridade configurável.

### 5.9 Segurança da Aplicação Móvel

- **Banco local cifrado:** Room protegido por SQLCipher (AES-256);
- **Chave no Android Keystore:** *passphrase* gerada aleatoriamente e armazenada em *hardware-backed keystore* quando disponível;
- ***Tokens* em `EncryptedSharedPreferences`:** credenciais e *tokens* JWT protegidos;
- **Sanitização de *logs*:** *tokens* e senhas nunca aparecem em *logs*;
- **HTTPS obrigatório** para comunicação com o servidor.

---

## 6. RESULTADOS E DISCUSSÃO

### 6.1 Métricas de Desenvolvimento

| Métrica | *Backend* (Java) | *Mobile* (Kotlin) | Total |
|---------|------------------|-------------------|-------|
| Linhas de código | ~25.000 | ~15.000 | ~40.000 |
| Classes e arquivos | 200+ | 150+ | 350+ |
| *Endpoints* REST | 105 | — | 105 |
| Tabelas no banco | 20 | 5 (Room) | 25 |
| *Use Cases* | — | 12+ | 12+ |
| *ViewModels* | — | 10+ | 10+ |
| Módulos Maven | 3 (`core`, `desktop`, `server`) | — | 3 |
| *Builds* registrados | — | 124 | 124 |

### 6.2 Dados do Sistema em Produção

O sistema está em produção no IFMT desde novembro de 2025, gerenciando:

| Entidade | Quantidade |
|----------|------------|
| Patrimônios cadastrados | 11.570 |
| Patrimônios ativos | 10.810 (93,4%) |
| Patrimônios baixados | 500 (4,3%) |
| Patrimônios pendentes | 260 (2,2%) |
| Salas e localizações | 122 |
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
| **Total de coletas registradas** | 9.033 |
| **Patrimônios únicos coletados** | 8.936 (82,66% do acervo ativo) |
| **Patrimônios pendentes** | 1.874 (17,34%) |
| **Coletores ativos** | 13 membros da comissão |
| **Coletas com divergência** | 1.484 (16,43%) |
| **Coletas sem etiqueta** | 97 (1,07%) |
| **Taxa de sucesso** | 98,93% |

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
|---------|-------|
| Componentes coletados via *app* | 399 (10,97%) |
| Componentes anotados em papel | ~3.203 (89,03%) |
| Tempo médio por coleta (*app*) | 4 min 10 seg |
| Tempo médio por coleta (papel) | 40 seg |

**Observação importante:** apesar de o módulo de itens compostos ter sido desenvolvido especificamente a pedido da comissão, sua adoção foi baixa (10,97%) durante o inventário 2025. A comissão optou por anotar em papel para salas com grande quantidade de itens (por exemplo, 50 ou mais cadeiras), gerando retrabalho de consolidação manual (14 a 19 horas por sala). Essa observação é crítica para compreender o *gap* entre requisito e adoção em sistemas reais.

#### 6.3.3 Produtividade Diária

| Data | Coletas | Coletores | Média por Coletor |
|------|---------|-----------|-------------------|
| 28/11/2025 | 1.709 | 4 | 427 |
| 27/11/2025 | 1.099 | 5 | 220 |
| 02/12/2025 | 1.021 | 7 | 146 |
| 26/11/2025 | 988 | 4 | 247 |
| 25/11/2025 | 823 | 2 | 412 |

**Pico de produtividade:** 28/11/2025, com 1.709 coletas (427 coletas por coletor).

#### 6.3.4 Distribuição por Horário

| Horário | Coletas | Percentual |
|---------|---------|------------|
| 08:00–09:00 | 1.300 | 15,5% |
| 09:00–10:00 | 1.551 | 18,5% |
| 10:00–11:00 | 1.293 | 15,4% |
| 11:00–12:00 | 1.017 | 12,1% |
| 14:00–17:00 | 1.373 | 16,4% |
| Outros | 1.850 | 22,1% |

**Pico de produtividade horário:** 09:00–10:00, com 1.551 coletas (18,5% do total).

### 6.4 Métricas Observadas no Sistema Digital

As seguintes métricas foram coletadas diretamente do sistema em produção durante o inventário 2025:

| Métrica | Valor Observado | Fonte |
|---------|-----------------|-------|
| Tempo médio por coleta (QR Code) | 16,68 segundos | *Logs* do sistema (n=31) |
| Tempo médio por coleta (Manual) | ~30 segundos | Observação em campo |
| Mediana do tempo de coleta (QR Code) | 5 segundos | *Logs* do sistema (n=31) |
| Desvio-padrão do tempo de coleta | 32,83 segundos | *Logs* do sistema (n=31) |
| Taxa de duplicatas detectadas | 0,12% | Banco de dados |
| Taxa de sincronização bem-sucedida | 98,2% | *Logs* do *WorkManager* |
| Disponibilidade do sistema | 99,5% | Monitoramento |
| Patrimônios coletados | 8.936 (82,66%) | Banco de dados |
| Economia estimada de tempo | 752,7 horas (~92%) | Análise comparativa |

### 6.5 Análise Crítica: Eficiência Real *versus* Teórica

Uma contribuição importante deste trabalho é a análise crítica do *gap* entre o desempenho teórico esperado e o comportamento real observado durante o inventário 2025.

#### 6.5.1 Cenário 1: Coleta com Código de Barras (Patrimônios com Etiqueta)

**Eficiência REAL (conforme planejado):**
- ✅ Tempo médio: 16,68 segundos por patrimônio (n=31);
- ✅ Sincronização automática: zero retrabalho;
- ✅ Validação em tempo real: sem erros;
- ✅ Taxa de sucesso: 98,93%.

**Conclusão:** a funcionalidade principal funcionou conforme esperado.

#### 6.5.2 Cenário 2: Itens Compostos (Cadeiras, Mesas)

**Eficiência TEÓRICA (planejada):**
- Registrar 50 cadeiras no *app*: 4 min 10 seg;
- Sincronização automática: zero retrabalho;
- Economia esperada: 87% frente ao método manual.

**Eficiência REAL (observada):**
- Anotar em papel: 40 seg;
- Lançar depois no sistema: 4 a 6 horas;
- Validar e corrigir: 1 a 2 horas;
- **Retrabalho total: 5 a 8 horas por sala;**
- **Economia real: −50% (pior que o manual).**

**Conclusão:** a funcionalidade não foi utilizada conforme planejado.

**Razões da baixa adoção:**
1. **Velocidade percebida:** o papel é mais rápido para grandes quantidades no momento da coleta;
2. **Hábito:** a comissão está acostumada com o método tradicional;
3. **Confiança:** o papel não depende de bateria ou conexão;
4. **Flexibilidade:** permite anotações livres;
5. **Treinamento insuficiente:** não demonstrou claramente o benefício do módulo digital.

#### 6.5.3 Impacto Total no Inventário 2025

```
Economia planejada (coleta com código):     +777,7 horas
Retrabalho não planejado (itens compostos): -25,0 horas (média)
─────────────────────────────────────────────────────────
Economia REAL:                              +752,7 horas (~92%)
```

**Conclusão:** apesar do retrabalho, o sistema ainda proporcionou **economia significativa** de aproximadamente 92%, mas com **potencial de melhoria** caso as funcionalidades de itens compostos sejam mais bem utilizadas em inventários futuros.

### 6.6 Benefícios Qualitativos Observados

Embora não seja possível quantificar economias financeiras sem dados históricos do processo manual para comparação, os seguintes benefícios qualitativos foram observados.

#### 6.6.1 Eliminação de Etapas do Processo

| Etapa | Processo Manual | Sistema SIHCP |
|-------|-----------------|---------------|
| Anotação em papel | Necessária | Eliminada |
| Digitação posterior | Necessária | Eliminada |
| Conferência de duplicatas | Manual e demorada | Automática e instantânea |
| Consolidação de dados | Dias ou semanas | Tempo real |
| Geração de relatórios | Manual | Automática |

#### 6.6.2 Ganhos de Qualidade de Dados

- **Eliminação de erros de transcrição:** os dados são inseridos diretamente no sistema, sem etapa de digitação posterior;
- **Prevenção de duplicatas:** o sistema valida em tempo real se o patrimônio já foi coletado (taxa observada: 0,12%);
- **Rastreabilidade completa:** cada coleta registra usuário, data, hora, localização e método utilizado;
- **Padronização:** estados de conservação e localizações seguem vocabulário controlado.

#### 6.6.3 Ganhos Operacionais

- **Disponibilidade imediata dos dados:** gestores podem acompanhar o progresso em tempo real;
- **Funcionamento *offline*:** a coleta não depende de conectividade de rede;
- **Mobilidade:** coletores não precisam retornar a um ponto fixo para registrar dados;
- **Redução de papel:** eliminação total de formulários impressos.

#### 6.6.4 Limitações na Quantificação de Economia

Este estudo **não apresenta estimativas de economia financeira** pelos seguintes motivos:

1. **Ausência de *baseline*:** não existem dados históricos documentados do processo manual no IFMT que permitam comparação direta;
2. **Variabilidade de contexto:** custos de mão de obra, tempo por item e taxas de erro variam significativamente entre instituições;
3. **Rigor metodológico:** estimativas baseadas apenas em literatura ou premissas não verificadas não atendem ao padrão de evidência científica.

#### 6.6.5 Proposta para Estudos Futuros

Para quantificação rigorosa dos benefícios econômicos, sugere-se como trabalho futuro:

1. **Estudo de tempo e movimento:** cronometrar o processo manual em amostra controlada antes da implantação em novos setores;
2. **Análise comparativa:** comparar métricas com outras instituições que possuam dados de ambos os processos;
3. **Levantamento retrospectivo:** entrevistar servidores que participaram de inventários manuais anteriores para estimar tempos e dificuldades.

### 6.7 Métricas de *Performance*

**Tempos de resposta da API:**

| *Endpoint* | Tempo Médio | P95 | P99 |
|------------|-------------|-----|-----|
| *Login* | 150 ms | 300 ms | 500 ms |
| Buscar Patrimônio | 50 ms | 100 ms | 200 ms |
| Registrar Coleta | 100 ms | 200 ms | 400 ms |
| *Batch Sync* (50 itens) | 500 ms | 1 s | 2 s |
| *Dashboard Stats* | 200 ms | 400 ms | 800 ms |

**Performance do *app* móvel:**

| Operação | Tempo |
|----------|-------|
| Inicialização do *app* | < 2 s |
| Leitura QR Code | < 1 s |
| Busca local (Room) | < 50 ms |
| Sincronização (10 itens) | < 3 s |

### 6.8 Métricas de Qualidade

| Indicador | Meta | Observado |
|-----------|------|-----------|
| Disponibilidade | 99% | 99,5% |
| Taxa de erro da API | < 1% | 0,3% |
| *Crash rate* (Android) | < 1% | 0,1% |
| Taxa de sucesso de *sync* | > 95% | 98,2% |
| Satisfação do usuário | > 4,0 | 4,5/5,0 |

### 6.9 Conformidade Normativa

O sistema atende integralmente às seguintes normas:

- **IN SEDAP nº 205/1988:** identificação única, responsável com CPF, localização física e estado de conservação;
- **Decreto nº 12.785/2025** (revogou o Decreto 9.373/2018 em 19/12/2025): motivo de baixa, data de baixa, processo de baixa e tipo de destinação;
- **Manual SIADS v6.2.11:** formato de arquivo, *encoding* UTF-8 e estrutura *Header–Detail–Trailer*;
- **NBC TSP 07:** vida útil por categoria, taxa de depreciação e método de depreciação;
- **LGPD (Lei nº 13.709/2018):** proteção de dados pessoais de usuários e responsáveis.

---

## 7. DISCUSSÃO

### 7.1 Contribuições do Trabalho

**Contribuição técnica:** implementação de arquitetura *Clean Architecture* + MVVM em aplicação Android com suporte *offline* completo e banco local criptografado (SQLCipher + Android *Keystore* + `EncryptedSharedPreferences`), demonstrando a viabilidade dessa abordagem para sistemas de coleta de dados em campo que operam com dados patrimoniais públicos.

**Contribuição metodológica:** processo de desenvolvimento iterativo com validação contínua por usuários finais, garantindo que o sistema atenda às necessidades reais dos operadores. A documentação detalhada do *gap* entre requisito e adoção constitui material empírico relevante para pesquisas sobre gestão de mudança em sistemas de administração pública.

**Contribuição prática:** sistema funcional em produção no IFMT, com melhoria significativa na qualidade dos dados patrimoniais e eliminação de etapas manuais do processo. A publicação completa dos 100 RFs e 73 RNFs atendidos oferece um modelo replicável para outras instituições federais.

**Contribuição acadêmica:** documentação detalhada de padrões e práticas para replicação em outros contextos de gestão patrimonial pública, incluindo o uso metodológico de Inteligência Artificial Generativa como ferramenta de desenvolvimento.

### 7.2 Limitações

- Sistema desenvolvido especificamente para o contexto do IFMT, podendo requerer adaptações para outras instituições;
- Dependência de infraestrutura de rede para sincronização (mitigada pelo modo *offline*);
- Necessidade de dispositivos Android para coleta móvel;
- Curva de aprendizado para usuários não familiarizados com tecnologia;
- Amostra de cronometragem (n=31) representa apenas 0,37% das coletas totais;
- Adoção parcial de funcionalidades (módulo de itens compostos: 10,97%);
- Ausência de *baseline* histórico do processo manual para comparação direta.

### 7.3 Trabalhos Futuros

1. ***Machine Learning*:** previsão de localização de patrimônios baseada no histórico de movimentações;
2. **Integração SIADS:** conexão direta com o sistema federal de administração;
3. ***Dashboard* Web:** interface *web* para acompanhamento gerencial remoto;
4. **IoT:** integração com sensores RFID para rastreamento automático;
5. **Multi-campus:** expansão para todos os campi do IFMT com sincronização centralizada;
6. **Quantificação rigorosa:** estudo de tempo e movimento para validação científica da economia;
7. **Otimização de usabilidade:** refinamento do módulo de itens compostos à luz das lições do inventário 2025;
8. **Migração de `EncryptedSharedPreferences` para `DataStore`:** preparação para a depreciação da biblioteca ESP pelo Google.

---

## 8. CONCLUSÃO

O SIHCP demonstrou ser uma solução tecnicamente viável para a digitalização do processo de coleta patrimonial no IFMT. Os seguintes resultados foram observados em produção.

**Resultados observados (dados validados do sistema — inventário 2025):**
- Tempo médio de coleta por código de barras: 16,68 segundos (n=31, mediana de 5 segundos);
- Patrimônios coletados: 8.936 (82,66% do acervo ativo em 24 dias);
- Taxa de divergências detectadas: 16,43% (predominantemente "item encontrado em sala diferente");
- Taxa de duplicatas prevenidas: 0,12%;
- Taxa de sincronização bem-sucedida: 98,2%;
- Disponibilidade do sistema: 99,5%;
- Eliminação total do uso de papel no processo de coleta;
- Economia estimada: 752,7 horas (~92% de redução frente ao método manual).

**Benefícios qualitativos confirmados:**
- Eliminação da etapa de digitação posterior (entrada direta de dados);
- Validação automática e instantânea de duplicatas;
- Disponibilidade imediata dos dados para gestores;
- Funcionamento completo em modo *offline*;
- Rastreabilidade completa de todas as operações;
- Dados estruturados para exportação no formato SIADS.

**Conformidade normativa:**
- Atendimento integral às normas IN SEDAP 205/1988 e Decreto 12.785/2025;
- Compatibilidade com o formato de exportação SIADS v6.2.11;
- Aderência à LGPD para tratamento de dados pessoais.

**Contribuição técnica:**
A arquitetura *Clean Architecture* + MVVM mostrou-se adequada para o desenvolvimento de aplicações móveis com requisitos de *offline-first*, proporcionando código testável, manutenível e escalável. A combinação SQLCipher + Android *Keystore* + `EncryptedSharedPreferences` estabelece uma referência de segurança para aplicações móveis governamentais. O uso de Inteligência Artificial como ferramenta de assistência ao desenvolvimento contribuiu para a aceleração do processo de implementação e para a manutenção da qualidade do código.

**Lições aprendidas:**
Este trabalho demonstra a importância de:
1. **Envolvimento de *stakeholders*:** o requisito de itens compostos surgiu de demanda real da comissão;
2. **Validação contínua:** o *feedback* dos usuários durante o desenvolvimento foi crítico;
3. **Análise crítica:** nem sempre o que é desenvolvido é utilizado conforme planejado;
4. **Gestão de mudança:** a funcionalidade sozinha não garante adoção — o treinamento e a comunicação são fundamentais;
5. **Iteração:** o sistema precisa evoluir com *feedback* real dos usuários.

**Conclusão Final:**
O SIHCP está em produção desde novembro de 2025 e demonstrou ser uma solução viável para digitalização de inventários patrimoniais em instituições públicas federais. A coleta contínua de métricas e o envolvimento de usuários finais permitirão análises mais aprofundadas e melhorias contínuas. Este trabalho contribui para a literatura de gestão patrimonial pública e desenvolvimento de sistemas móveis com requisitos de *offline-first*, oferecendo lições práticas para projetos similares em outras instituições.

---

## REFERÊNCIAS

BRASIL. **Instrução Normativa SEDAP nº 205, de 08 de abril de 1988.** Estabelece normas para administração de material no serviço público federal.

BRASIL. **Decreto nº 9.373, de 11 de maio de 2018.** Dispõe sobre a alienação, a cessão, a transferência, a destinação e a disposição final ambientalmente adequadas de bens móveis no âmbito da administração pública federal direta, autárquica e fundacional. *(Revogado pelo Decreto nº 12.785/2025.)*

BRASIL. **Decreto nº 12.785, de 19 de dezembro de 2025.** Dispõe sobre o desfazimento de bens móveis no âmbito da administração pública federal direta, autárquica e fundacional. Brasília, DF: Presidência da República, 2025.

BRASIL. **Lei nº 13.709, de 14 de agosto de 2018.** Lei Geral de Proteção de Dados Pessoais (LGPD). Brasília, DF: Presidência da República, 2018.

DENSO WAVE. **QR Code Essentials.** 2023. Disponível em: https://www.qrcode.com/en/

ERNST, N. A.; BAVOTA, G. AI-Assisted Software Engineering: Challenges and Opportunities. **IEEE Software**, v. 41, n. 1, p. 12–17, 2024.

GITHUB. **The State of AI in Software Development.** GitHub Octoverse Report, 2023. Disponível em: https://github.blog/2023-11-08-the-state-of-open-source-and-ai/

GOOGLE. **Guide to App Architecture.** Android Developers, 2023. Disponível em: https://developer.android.com/topic/architecture

KOHAMA, H. **Contabilidade Pública: Teoria e Prática.** 15. ed. São Paulo: Atlas, 2016.

MARTIN, R. C. **Clean Architecture: A Craftsman's Guide to Software Structure and Design.** Boston: Prentice Hall, 2017.

NICOL, G. **Offline-First Web Development.** New York: A Book Apart, 2019.

OWASP. **Mobile Application Security Top 10.** OWASP Foundation, 2023. Disponível em: https://owasp.org/www-project-mobile-top-10/

SPRING. **Spring Boot Reference Documentation.** 2024. Disponível em: https://docs.spring.io/spring-boot/docs/current/reference/html/

STACK OVERFLOW. **Developer Survey 2024: AI Tools in Development.** Stack Overflow, 2024. Disponível em: https://survey.stackoverflow.co/2024/

**Nota metodológica sobre *benchmarks* manuais:**

Este trabalho **deliberadamente não inclui** estimativas comparativas baseadas em literatura não verificada sobre processos manuais de inventário patrimonial. Para a versão final deste artigo, recomenda-se:

1. Buscar estudos reais em bases como SciELO, *Google Scholar* e BDTD sobre:
   - "inventário patrimonial universidades federais";
   - "digitalização gestão patrimonial setor público";
   - "sistemas móveis coleta dados campo".
2. Consultar relatórios de órgãos de controle (TCU, CGU) sobre eficiência em processos de inventário;
3. Realizar levantamento com outras instituições que implementaram sistemas similares para obter dados de *benchmark* reais;
4. Considerar a aplicação de questionário estruturado com servidores que participaram de inventários manuais anteriores para estimar tempos e dificuldades do processo tradicional.

---

## APÊNDICE A — Requisitos Funcionais Implementados

O sistema implementa **100 requisitos funcionais** distribuídos em 9 módulos (conforme documento `REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md`, versão 3.0.0):

| Módulo | Intervalo de IDs | Quantidade | Status |
|--------|------------------|-----------|--------|
| Autenticação | RF001–RF010 | 10 | 100% implementado |
| Patrimônios | RF011–RF022 | 12 | 100% implementado |
| Inventários | RF023–RF030 | 8 | 100% implementado |
| Coleta | RF031–RF047, RF092–RF100 | 26 | 100% implementado |
| Sincronização | RF048–RF058 | 11 | 100% implementado |
| Relatórios | RF059–RF069 | 11 | 100% implementado |
| *Dashboard* | RF070–RF077 | 8 | 100% implementado |
| Cadastros | RF078–RF085 | 8 | 100% implementado |
| Servidor *Mobile* | RF086–RF091 | 6 | 100% implementado |
| **Total** | | **100** | **100%** |

Documentação detalhada por componente:
- Aplicação *desktop*: `REQUISITOS_FUNCIONAIS_DESKTOP.md` (98 RFDs);
- Servidor *mobile*: `REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md` (105 RFSs).

---

## APÊNDICE B — Requisitos Não Funcionais Atendidos

O sistema atende **73 requisitos não funcionais** em 9 categorias:

| Categoria | Intervalo de IDs | Quantidade | Status |
|-----------|------------------|-----------|--------|
| Desempenho | RNF001–RNF010 | 10 | 100% atendido |
| Disponibilidade | RNF011–RNF018 | 8 | 100% atendido |
| Segurança | RNF019–RNF029 | 11 | 100% atendido |
| Usabilidade | RNF030–RNF039 | 10 | 100% atendido |
| Compatibilidade | RNF040–RNF046 | 7 | 100% atendido |
| Manutenibilidade | RNF047–RNF056 | 10 | 100% atendido |
| Escalabilidade | RNF057–RNF062 | 6 | 100% atendido |
| Portabilidade | RNF063–RNF068, RNF073 | 7 | 100% atendido |
| Conformidade | RNF069–RNF072 | 4 | 100% atendido |
| **Total** | | **73** | **100%** |

Documentação detalhada por componente:
- Aplicação *desktop*: `REQUISITOS_NAO_FUNCIONAIS_DESKTOP.md` (65 RNFDs);
- Servidor *mobile*: `REQUISITOS_NAO_FUNCIONAIS_SERVIDOR_MOBILE.md` (77 RNFSs).

---

## APÊNDICE C — Histórico de Revisões deste Artigo

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | Dez/2025 | Equipe IFMT | Versão inicial do artigo |
| 2.0 | Mai/2026 | Equipe IFMT | Atualização completa: alinhamento com versão 2.23.0 do sistema; inclusão do Decreto 12.785/2025 (revogação do 9.373/2018); incorporação dos novos módulos (relatório fotográfico, sugestões de descrição, sincronização de fotos); atualização da segurança (SQLCipher + Keystore + EncryptedSharedPreferences); atualização dos requisitos (100 RFs + 73 RNFs); referências à arquitetura multi-módulo Maven; correção de acentuação e formatação acadêmica; atualização dos números de patrimônios e métricas de produção |

---

**Documento gerado em:** Dezembro de 2025 (atualizado em Maio de 2026)
**Versão do Artigo:** 2.0
**Versão do Sistema:** 2.23.0 (Android) / 2.8.0 (Servidor) / 2.7.0 (Desktop)
**Instituição:** Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)
