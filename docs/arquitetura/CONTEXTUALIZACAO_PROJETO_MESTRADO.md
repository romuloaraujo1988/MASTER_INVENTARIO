# Inovação no Processo de Inventário do IFMT: Desenvolvimento de Solução Digital Integrada

## Contextualização do Projeto de Mestrado

---

## 1. Introdução

Este projeto de mestrado investiga a **inovação em processos organizacionais** no contexto da gestão patrimonial pública, tendo como objeto de estudo o processo de inventário do Instituto Federal de Mato Grosso (IFMT). 

A pesquisa parte da premissa de que a transformação digital em organizações públicas não se resume à adoção de tecnologias, mas requer fundamentalmente o **redesenho dos processos de negócio** que essas tecnologias irão suportar. Nesse sentido, o software desenvolvido neste trabalho é um **artefato resultante** da proposta de um novo processo — um meio para operacionalizar as melhorias identificadas, não um fim em si mesmo.

### 1.1 Delimitação do Objeto de Estudo

| Dimensão | Escopo |
|----------|--------|
| **Foco Principal** | Redesenho do processo de inventário patrimonial |
| **Abordagem** | Business Process Management (BPM) |
| **Artefato Tecnológico** | Produto derivado do novo processo |
| **Contexto** | Administração Pública Federal |
| **Instituição** | Instituto Federal de Mato Grosso (IFMT) |

---

## 2. Fundamentação Teórica

### 2.1 Gestão de Processos de Negócio (BPM)

A Gestão de Processos de Negócio (*Business Process Management* - BPM) é uma abordagem sistemática para identificar, desenhar, executar, documentar, medir, monitorar e controlar processos de negócio, visando alcançar resultados consistentes e alinhados aos objetivos estratégicos da organização (ABPMP, 2013).

Segundo Hammer e Champy (1993), a reengenharia de processos representa uma mudança de paradigma: ao invés de automatizar processos existentes com suas ineficiências, propõe-se **repensar fundamentalmente** como o trabalho é realizado.

#### Ciclo de Vida BPM

```
        ┌─────────────────┐
        │   PLANEJAMENTO  │
        │   E ESTRATÉGIA  │
        └────────┬────────┘
                 │
                 ▼
        ┌─────────────────┐
        │     ANÁLISE     │◄──────────────────┐
        │    (AS-IS)      │                   │
        └────────┬────────┘                   │
                 │                            │
                 ▼                            │
        ┌─────────────────┐                   │
        │    DESENHO      │                   │
        │    (TO-BE)      │                   │
        └────────┬────────┘                   │
                 │                            │
                 ▼                            │
        ┌─────────────────┐                   │
        │  IMPLEMENTAÇÃO  │                   │
        └────────┬────────┘                   │
                 │                            │
                 ▼                            │
        ┌─────────────────┐                   │
        │  MONITORAMENTO  │───────────────────┘
        │   E CONTROLE    │    (Melhoria Contínua)
        └─────────────────┘
```

### 2.2 Notação BPMN

A *Business Process Model and Notation* (BPMN) é o padrão internacional para modelagem de processos de negócio, mantido pela Object Management Group (OMG). Sua utilização permite:

- **Comunicação clara** entre áreas de negócio e tecnologia
- **Documentação padronizada** dos processos organizacionais
- **Identificação de gargalos** e oportunidades de melhoria
- **Base para automação** de processos

### 2.3 Transformação Digital na Administração Pública

A transformação digital no setor público vai além da simples informatização de procedimentos. Conforme Fountain (2001), trata-se de uma **reconfiguração das estruturas organizacionais** e dos processos de trabalho, mediada pela tecnologia.

No contexto brasileiro, o Decreto nº 10.332/2020 estabelece a Estratégia de Governo Digital, que preconiza:

- Digitalização de serviços públicos
- Desburocratização de processos
- Transparência e controle social
- Eficiência na gestão de recursos

### 2.4 Gestão Patrimonial na Administração Pública

A gestão de bens patrimoniais em órgãos públicos federais é regulamentada por um arcabouço normativo que inclui:

- **Instrução Normativa SEDAP nº 205/1988**: Normas para administração de material
- **Decreto nº 9.373/2018**: Alienação e destinação de bens móveis
- **Lei nº 4.320/1964**: Normas gerais de direito financeiro
- **Acórdãos do TCU**: Recomendações de controle patrimonial

O inventário patrimonial é um **processo crítico** para a conformidade legal e prestação de contas, exigindo precisão, rastreabilidade e tempestividade.

---

## 3. Diagnóstico: Análise do Processo Atual (AS-IS)

### 3.1 Mapeamento do Processo Tradicional

O processo de inventário patrimonial tradicionalmente praticado em instituições públicas federais apresenta características de um **modelo funcional fragmentado**, com múltiplas transferências de responsabilidade (*handoffs*) e dependência intensiva de atividades manuais.

#### Modelo BPMN - Processo AS-IS (Tradicional)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  PROCESSO DE INVENTÁRIO PATRIMONIAL - MODELO TRADICIONAL (AS-IS)                │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  POOL: COMISSÃO DE INVENTÁRIO                                                   │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                                                                            │ │
│  │  ○──►[Constituir    ]──►[Definir      ]──►[Elaborar     ]──►◇             │ │
│  │      [Comissão      ]   [Cronograma   ]   [Listagens    ]   │             │ │
│  │                                                              │             │ │
│  └──────────────────────────────────────────────────────────────┼─────────────┘ │
│                                                                  │               │
│  POOL: COLETORES DE CAMPO                                        │               │
│  ┌───────────────────────────────────────────────────────────────┼─────────────┐ │
│  │                                                               ▼             │ │
│  │  [Receber        ]──►[Visitar       ]──►[Anotar em    ]──►[Devolver     ] │ │
│  │  [Listagens      ]   [Salas         ]   [Papel        ]   [Formulários  ] │ │
│  │  [Impressas      ]   [Físicas       ]   [Dados        ]   [Preenchidos  ] │ │
│  │                                                                            │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                                                  │               │
│  POOL: EQUIPE ADMINISTRATIVA                                     │               │
│  ┌───────────────────────────────────────────────────────────────┼─────────────┐ │
│  │                                                               ▼             │ │
│  │  [Receber        ]──►[Digitar em    ]──►[Conferir     ]──►◇──►[Corrigir   ]│ │
│  │  [Formulários    ]   [Planilhas     ]   [Digitação    ]   │   [Erros     ]│ │
│  │                                                            │              ││ │
│  │                                                            ▼              ││ │
│  │  [Consolidar     ]◄──[Resolver      ]◄──[Identificar  ]◄──┘              ││ │
│  │  [Dados          ]   [Pendências    ]   [Divergências ]◄─────────────────┘│ │
│  │        │                                                                   │ │
│  │        ▼                                                                   │ │
│  │  [Gerar          ]──►[Enviar para   ]──►●                                 │ │
│  │  [Relatório      ]   [Órgãos        ]                                     │ │
│  │  [Final          ]   [Controle      ]                                     │ │
│  │                                                                            │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  LEGENDA: ○ Início  ● Fim  ◇ Gateway (Decisão)  [ ] Atividade                   │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Análise de Valor Agregado

Aplicando a técnica de Análise de Valor Agregado (AVA), as atividades do processo tradicional podem ser classificadas em:

| Tipo de Atividade | Exemplos | % do Tempo |
|-------------------|----------|------------|
| **Valor Agregado (VA)** | Verificação física do bem | ~15% |
| **Valor Agregado ao Negócio (VAN)** | Registro para conformidade legal | ~20% |
| **Sem Valor Agregado (SVA)** | Transcrição, conferência de digitação, espera | ~65% |

> **Conclusão**: Aproximadamente 65% do esforço do processo tradicional é consumido em atividades que não agregam valor ao resultado final.

### 3.3 Problemas Identificados

#### 3.3.1 Desperdícios (Ótica Lean)

| Desperdício | Manifestação no Processo |
|-------------|-------------------------|
| **Espera** | Formulários aguardando digitação; dados aguardando consolidação |
| **Transporte** | Movimentação física de documentos entre setores |
| **Processamento Excessivo** | Dupla conferência; retrabalho por erros de transcrição |
| **Defeitos** | Erros de digitação; dados inconsistentes |
| **Movimentação** | Deslocamentos desnecessários para buscar informações |
| **Estoque** | Acúmulo de formulários pendentes de processamento |

#### 3.3.2 Pontos Críticos (Gargalos)

1. **Transcrição Manual**: Etapa com maior incidência de erros e maior consumo de tempo
2. **Consolidação Sequencial**: Dependência de conclusão de todas as coletas para iniciar análise
3. **Resolução de Divergências**: Processo reativo, identificando problemas tardiamente
4. **Comunicação Fragmentada**: Informações dispersas entre múltiplos atores

### 3.4 Indicadores do Processo AS-IS

| Indicador | Valor Típico |
|-----------|--------------|
| Tempo de ciclo (lead time) | 4-8 semanas |
| Número de handoffs | 8-12 transferências |
| Taxa de retrabalho | Não mensurada (estimada alta) |
| Custo de papel/impressão | Significativo |
| Rastreabilidade | Parcial/Manual |

---

## 4. Proposta: Redesenho do Processo (TO-BE)

### 4.1 Princípios Norteadores do Redesenho

O redesenho do processo foi orientado pelos seguintes princípios de BPM:

1. **Eliminação de atividades sem valor**: Remover etapas de transcrição e conferência redundante
2. **Paralelização**: Permitir execução simultânea de atividades independentes
3. **Automação de tarefas repetitivas**: Delegar à tecnologia atividades mecânicas
4. **Controle na origem**: Validar dados no momento da coleta, não posteriormente
5. **Visibilidade em tempo real**: Permitir acompanhamento contínuo do progresso
6. **Empoderamento do executor**: Dar autonomia ao coletor para resolver questões simples

### 4.2 Modelo BPMN - Processo TO-BE (Otimizado)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  PROCESSO DE INVENTÁRIO PATRIMONIAL - MODELO OTIMIZADO (TO-BE)                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  POOL: GESTOR DO INVENTÁRIO                                                     │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                                                                            │ │
│  │  ○──►[Configurar    ]──►[Atribuir     ]──►[Monitorar    ]──►[Validar     ]│ │
│  │      [Inventário    ]   [Responsáveis ]   [Progresso    ]   [Conclusão   ]│ │
│  │      [Digital       ]   [por Área     ]   [em Tempo Real]   [e Fechar    ]│ │
│  │                              │                  ▲                         │ │
│  └──────────────────────────────┼──────────────────┼─────────────────────────┘ │
│                                 │                  │                            │
│                                 │    ┌─────────────┘ (Sincronização            │
│                                 │    │               Automática)               │
│  POOL: COLETOR DE CAMPO         │    │                                         │
│  ┌──────────────────────────────┼────┼───────────────────────────────────────┐ │
│  │                              ▼    │                                       │ │
│  │  [Receber        ]──►[Identificar  ]──►◇──►[Registrar    ]──►[Confirmar  ]│ │
│  │  [Atribuição     ]   [Bem          ]   │   [Coleta       ]   [Dados      ]│ │
│  │  [Digital        ]   [Automaticam. ]   │   [com Validação]   [           ]│ │
│  │                                        │         │                        │ │
│  │                                        │         │                        │ │
│  │                      ┌─────────────────┘         │                        │ │
│  │                      │ (Divergência?)            │                        │ │
│  │                      ▼                           │                        │ │
│  │                [Registrar    ]                   │                        │ │
│  │                [Divergência  ]───────────────────┘                        │ │
│  │                [com Evidência]                                            │ │
│  │                                                                           │ │
│  └───────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  SUBPROCESSO AUTOMATIZADO (Sistema)                                             │
│  ┌───────────────────────────────────────────────────────────────────────────┐  │
│  │  ═══►[Validar      ]══►[Consolidar  ]══►[Gerar        ]══►[Disponibilizar]│  │
│  │      [Dados em     ]   [Automaticam.]   [Relatórios   ]   [para Órgãos   ]│  │
│  │      [Tempo Real   ]   [             ]   [Automáticos  ]   [de Controle  ]│  │
│  └───────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  LEGENDA: ○ Início  ● Fim  ◇ Gateway  [ ] Atividade Manual  ═══ Atividade Auto │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 4.3 Mudanças Estruturais no Processo

| Aspecto | Processo AS-IS | Processo TO-BE |
|---------|----------------|----------------|
| **Fluxo de Informação** | Sequencial, com handoffs | Paralelo, com sincronização |
| **Registro de Dados** | Duplo (papel → digital) | Único (digital na origem) |
| **Validação** | Posterior (na consolidação) | Imediata (na coleta) |
| **Visibilidade** | Ao final do processo | Em tempo real |
| **Tratamento de Divergências** | Reativo | Proativo |
| **Documentação** | Física (papel) | Digital nativa |

### 4.4 Eliminação de Atividades Sem Valor

```
ATIVIDADES ELIMINADAS NO REDESENHO:

  ╔═══════════════════════════════════════════════════════════════╗
  ║  PROCESSO AS-IS                    PROCESSO TO-BE             ║
  ╠═══════════════════════════════════════════════════════════════╣
  ║                                                               ║
  ║  [Imprimir Listagens    ] ─────────► ELIMINADA               ║
  ║  [Anotar em Papel       ] ─────────► ELIMINADA               ║
  ║  [Digitar em Planilhas  ] ─────────► ELIMINADA               ║
  ║  [Conferir Digitação    ] ─────────► ELIMINADA               ║
  ║  [Corrigir Erros        ] ─────────► ELIMINADA               ║
  ║  [Consolidar Manualmente] ─────────► AUTOMATIZADA            ║
  ║  [Gerar Relatório Manual] ─────────► AUTOMATIZADA            ║
  ║                                                               ║
  ║  RESULTADO: 7 atividades eliminadas ou automatizadas         ║
  ║                                                               ║
  ╚═══════════════════════════════════════════════════════════════╝
```

---

## 5. O Artefato Tecnológico como Habilitador

### 5.1 Relação Processo × Tecnologia

É fundamental compreender que a tecnologia desenvolvida neste projeto é um **meio**, não um **fim**:

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                  │
│     PROCESSO OTIMIZADO              SOLUÇÃO TECNOLÓGICA         │
│     (Contribuição Principal)        (Artefato Derivado)         │
│                                                                  │
│     ┌───────────────────┐          ┌───────────────────┐        │
│     │                   │          │                   │        │
│     │  • Define REGRAS  │ ──────►  │  • Implementa as  │        │
│     │    de negócio     │ orienta  │    regras         │        │
│     │                   │          │                   │        │
│     │  • Estabelece     │          │  • Fornece        │        │
│     │    FLUXO de       │          │    interface      │        │
│     │    trabalho       │          │    para execução  │        │
│     │                   │          │                   │        │
│     │  • Define         │          │  • Coleta dados   │        │
│     │    INDICADORES    │          │    para medição   │        │
│     │                   │          │                   │        │
│     │  • Determina      │          │  • Automatiza     │        │
│     │    PAPÉIS e       │          │    tarefas        │        │
│     │    responsáveis   │          │    repetitivas    │        │
│     │                   │          │                   │        │
│     └───────────────────┘          └───────────────────┘        │
│                                                                  │
│     O PROCESSO PRECEDE E ORIENTA A TECNOLOGIA                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Funções do Artefato no Suporte ao Processo

| Necessidade do Processo | Função Habilitadora |
|------------------------|---------------------|
| Registro único na origem | Captura digital no ponto de coleta |
| Validação imediata | Verificação automática de regras de negócio |
| Rastreabilidade | Registro de autor, data, hora e localização |
| Visibilidade em tempo real | Consolidação automática e dashboards |
| Conformidade normativa | Geração de relatórios nos formatos exigidos |
| Continuidade operacional | Funcionamento em áreas sem conectividade |

---

## 6. Indicadores e Resultados Esperados

### 6.1 Indicadores de Desempenho do Processo

| Indicador | AS-IS | TO-BE (Meta) | Melhoria |
|-----------|-------|--------------|----------|
| **Tempo de Ciclo** | 4-8 semanas | 1-2 semanas | 75% redução |
| **Número de Handoffs** | 8-12 | 2-3 | 75% redução |
| **Atividades Manuais** | 12+ | 4 | 67% redução |
| **Uso de Papel** | Centenas de páginas | Zero | 100% eliminação |
| **Tempo para Consolidação** | Dias/semanas | Tempo real | ~100% redução |
| **Rastreabilidade** | Parcial | Total | Qualitativa |

### 6.2 Matriz de Valor

```
                        IMPACTO NO RESULTADO
                    Baixo                Alto
                ┌─────────────────┬─────────────────┐
         Alto   │                 │  ★ REDESENHO    │
   ESFORÇO      │   Evitar        │    DO PROCESSO  │
   DE           │                 │                 │
   MUDANÇA      ├─────────────────┼─────────────────┤
                │                 │  ★ AUTOMAÇÃO    │
         Baixo  │   Ignorar       │    DE TAREFAS   │
                │                 │                 │
                └─────────────────┴─────────────────┘

   ★ = Foco deste projeto
```

---

## 7. Considerações sobre Gestão da Mudança

### 7.1 Fatores Críticos de Sucesso

A implementação do novo processo requer atenção a fatores organizacionais além da tecnologia:

1. **Patrocínio da Alta Gestão**: Comprometimento institucional com a mudança
2. **Capacitação dos Atores**: Treinamento nos novos procedimentos e ferramentas
3. **Comunicação Clara**: Explicação dos benefícios e do novo fluxo de trabalho
4. **Gestão de Resistências**: Tratamento de objeções e receios dos envolvidos
5. **Piloto Controlado**: Implementação gradual com aprendizado iterativo

### 7.2 Modelo de Maturidade

O projeto posiciona a organização em uma trajetória de evolução na gestão de processos:

```
NÍVEIS DE MATURIDADE EM BPM (ABPMP)

  Nível 1        Nível 2        Nível 3        Nível 4        Nível 5
  INICIAL   ──►  GERENCIADO ──► PADRONIZADO ──► PREVISÍVEL ──► OTIMIZADO
     │              │               │               │              │
     │              │               │               │              │
  Processos     Processos       Processos       Processos      Melhoria
  ad hoc,       documentados,   padronizados,   medidos e      contínua
  caóticos      mas isolados    integrados      controlados    sistemática
     │              │               │               │              │
     │              │               │               │              │
     └──────────────┴───────────────┴───────────────┴──────────────┘
                              │
                              ▼
                    POSIÇÃO ATUAL DO IFMT
                    (Transição Nível 2 → 3)
```

---

## 8. Contribuições do Projeto

### 8.1 Contribuição Principal: Modelo de Processo Otimizado

A principal contribuição deste trabalho é a proposição de um **modelo de processo de inventário patrimonial** que pode ser:

- **Replicado** em outras instituições federais de ensino
- **Adaptado** para diferentes contextos de gestão patrimonial pública
- **Referenciado** como caso de redesenho de processos no setor público

### 8.2 Contribuição Metodológica

O projeto demonstra a aplicação prática de:

- Técnicas de mapeamento de processos (BPMN)
- Análise de valor agregado
- Identificação de desperdícios (Lean)
- Redesenho orientado a resultados

### 8.3 Contribuição Prática

O artefato tecnológico desenvolvido serve como:

- Ferramenta operacional para o novo processo
- Prova de conceito da viabilidade do modelo proposto
- Base para evolução e melhorias futuras

---

## 9. Conclusão

Este projeto de mestrado propõe uma **inovação no processo de inventário patrimonial** do IFMT, fundamentada em princípios de Gestão de Processos de Negócio (BPM) e orientada pela busca de eficiência operacional e conformidade normativa.

A abordagem adotada reconhece que a **transformação digital efetiva** não ocorre pela simples adoção de tecnologia, mas pelo **redesenho consciente dos processos** que a tecnologia irá suportar. O software desenvolvido é, portanto, um produto derivado — um instrumento que viabiliza a operacionalização do novo modelo de processo.

A contribuição central do trabalho está na demonstração de que processos burocráticos e ineficientes podem ser transformados em fluxos de trabalho ágeis, rastreáveis e conformes, desde que o redesenho seja conduzido com método e foco nos resultados organizacionais.

---

## Referências

ABPMP. **BPM CBOK: Guia para o Gerenciamento de Processos de Negócio**. 3ª ed. ABPMP Brasil, 2013.

BRASIL. Decreto nº 10.332, de 28 de abril de 2020. Institui a Estratégia de Governo Digital.

BRASIL. Instrução Normativa SEDAP nº 205, de 08 de abril de 1988.

DAVENPORT, Thomas H. **Process Innovation: Reengineering Work Through Information Technology**. Harvard Business School Press, 1993.

FOUNTAIN, Jane E. **Building the Virtual State: Information Technology and Institutional Change**. Brookings Institution Press, 2001.

HAMMER, Michael; CHAMPY, James. **Reengineering the Corporation: A Manifesto for Business Revolution**. Harper Business, 1993.

OMG. **Business Process Model and Notation (BPMN) Specification**. Version 2.0. Object Management Group, 2011.

WOMACK, James P.; JONES, Daniel T. **Lean Thinking: Banish Waste and Create Wealth in Your Corporation**. Free Press, 2003.

---

**Projeto:** Inovação no Processo de Inventário do IFMT: Desenvolvimento de Solução Digital Integrada  
**Instituição:** Instituto Federal de Mato Grosso (IFMT)  
**Programa:** Mestrado Profissional  
**Ano:** 2025
