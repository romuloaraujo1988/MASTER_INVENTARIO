# Documentação UML - Sistema de Inventário

Este documento apresenta a documentação UML completa do Sistema de Inventário, incluindo diagramas de classes, casos de uso, sequência e componentes.

## Índice

1. [Visão Geral do Sistema](#visão-geral-do-sistema)
2. [Diagrama de Classes - Entidades](#diagrama-de-classes---entidades)
3. [Diagrama de Classes - Controllers e Services](#diagrama-de-classes---controllers-e-services)
4. [Diagrama de Casos de Uso](#diagrama-de-casos-de-uso)
5. [Diagramas de Sequência](#diagramas-de-sequência)
6. [Diagrama de Componentes](#diagrama-de-componentes)
7. [Como Visualizar os Diagramas](#como-visualizar-os-diagramas)

## Visão Geral do Sistema

O Sistema de Inventário é uma aplicação completa para gestão de patrimônio institucional que integra:

- **Gestão de Patrimônio**: Cadastro, categorização e controle de bens
- **Processo de Inventário**: Planejamento e execução de inventários
- **Coleta Mobile**: Aplicativo para coleta de dados com suporte offline
- **Inteligência Artificial**: Categorização automática e análises preditivas
- **Chatbot Inteligente**: Assistente virtual para consultas e suporte
- **Relatórios Avançados**: Geração de relatórios e análises

### Tecnologias Principais

- **Backend**: Java com Spring Framework
- **Frontend**: Interface web responsiva
- **Mobile**: Aplicativo Android/iOS com sincronização
- **Banco de Dados**: PostgreSQL (principal) + SQLite (cache local)
- **IA**: Integração com OpenAI e modelos customizados
- **Arquitetura**: MVC com camadas bem definidas

## Diagrama de Classes - Entidades

**Arquivo**: `diagrama-entidades.puml`

Este diagrama apresenta o modelo de dados do sistema, incluindo:

### Entidades Principais

- **Patrimonio**: Representa os bens da instituição
  - Atributos: id, numero, descricao, valorAquisicao, categoria, etc.
  - Relacionamentos: pertence a uma Sala, tem um Responsável

- **Setor**: Organização departamental
  - Atributos: id, nome, descricao, responsavelSetor, ativo
  - Relacionamentos: possui várias Salas

- **Responsavel**: Pessoas responsáveis pelos patrimônios
  - Atributos: id, nome, cpf, email, idSetor, ativo
  - Relacionamentos: pertence a um Setor

- **Inventario**: Processo de inventário
  - Atributos: id, nome, ano, dataInicio, statusInventario
  - Relacionamentos: possui várias Coletas

- **Coleta**: Registro de coleta de dados
  - Atributos: id, idInventario, idPatrimonio, dataColeta, statusColeta
  - Relacionamentos: pertence a um Inventário e um Patrimônio

### Relacionamentos

- Campus 1:N Setor
- Setor 1:N Sala
- Setor 1:N Responsavel
- Sala 1:N Patrimonio
- Responsavel 1:N Patrimonio
- Inventario 1:N Coleta
- Patrimonio 1:N Coleta
- Patrimonio 1:1 QRCode

## Diagrama de Classes - Controllers e Services

**Arquivo**: `diagrama-controllers-services.puml`

Este diagrama mostra a arquitetura de software do sistema:

### Controllers (Camada de Apresentação)

- **PatrimonioController**: Endpoints para gestão de patrimônio
- **InventarioController**: Endpoints para processo de inventário
- **ColetaController**: Endpoints para coleta de dados
- **RelatorioController**: Endpoints para geração de relatórios
- **ChatbotController**: Endpoints para interação com chatbot
- **AIController**: Endpoints para funcionalidades de IA

### Services (Camada de Negócio)

#### Core Services
- **PatrimonioService**: Regras de negócio para patrimônio
- **InventarioService**: Lógica do processo de inventário
- **ColetaService**: Gerenciamento de coleta de dados
- **RelatorioService**: Geração e processamento de relatórios

#### AI Services
- **AIIntegrationService**: Coordenação dos serviços de IA
- **ChatbotService**: Processamento de conversas
- **PatrimonioCategorizationService**: Categorização automática
- **PredictiveAnalysisService**: Análises preditivas

### DAO (Camada de Dados)

- **PatrimonioDAO**: Acesso a dados de patrimônio
- **InventarioDAO**: Acesso a dados de inventário
- **ColetaDAO**: Acesso a dados de coleta
- **UsuarioDAO**: Acesso a dados de usuário

## Diagrama de Casos de Uso

**Arquivo**: `diagrama-casos-uso.puml`

Este diagrama apresenta as funcionalidades do sistema organizadas por atores:

### Atores

- **Administrador**: Acesso completo ao sistema
- **Gestor**: Gerenciamento de inventários e relatórios
- **Operador**: Cadastro e manutenção de dados
- **Usuário Consulta**: Consultas e relatórios básicos
- **Coletor**: Coleta de dados via mobile
- **Sistema IA**: Processamento automático

### Pacotes de Casos de Uso

1. **Gestão de Usuários**: Login, cadastro, perfis
2. **Gestão de Patrimônio**: CRUD de patrimônios, categorização
3. **Gestão Organizacional**: Setores, salas, responsáveis
4. **Processo de Inventário**: Planejamento, execução, finalização
5. **Coleta de Dados**: Coleta mobile, sincronização
6. **Relatórios e Análises**: Geração, exportação, dashboards
7. **Inteligência Artificial**: Categorização, análises preditivas
8. **Chatbot e Assistência**: Consultas, suporte
9. **Documentos Legais**: Integração com legislação
10. **Configuração e Manutenção**: Configurações, backup

## Diagramas de Sequência

**Arquivo**: `diagrama-sequencia.puml`

Os diagramas de sequência mostram a interação entre componentes nos principais fluxos:

### Fluxo 1: Cadastro de Patrimônio com Categorização IA

Mostra como um patrimônio é cadastrado, categorizado automaticamente pela IA e recebe um QR Code:

1. Operador inicia cadastro
2. Sistema valida dados
3. Patrimônio é inserido no banco
4. IA categoriza automaticamente
5. QR Code é gerado
6. Resultado é retornado

### Fluxo 2: Processo de Coleta com Divergência

Demonstra a coleta de dados via mobile, incluindo cenários offline:

1. Coletor lê QR Code
2. Sistema busca patrimônio (online/offline)
3. Dados são apresentados
4. Coleta é registrada
5. Divergências são tratadas
6. Sincronização é realizada

### Fluxo 3: Interação com Chatbot IA

Mostra como o chatbot processa perguntas e gera respostas:

1. Usuário faz pergunta
2. Sistema detecta intenção
3. Parâmetros são extraídos
4. Consulta é executada
5. IA gera insights
6. Resposta é formatada

### Fluxo 4: Sincronização de Dados Offline

Demonstra como dados coletados offline são sincronizados:

1. Sistema verifica conectividade
2. Dados pendentes são identificados
3. Sincronização é executada
4. Cache local é atualizado
5. Relatório é gerado

## Diagrama de Componentes

**Arquivo**: `diagrama-componentes.puml`

Este diagrama apresenta a arquitetura em camadas do sistema:

### Camada de Apresentação
- **Interface Web**: Interface responsiva para usuários
- **Aplicativo Mobile**: App para coleta com suporte offline
- **Chatbot Interface**: Interface de conversação

### Camada de Controle
- **Controllers REST**: Endpoints da API
- **Security Filter**: Autenticação e autorização
- **Exception Handler**: Tratamento de erros

### Camada de Negócio
- **Core Services**: Serviços principais do sistema
- **AI Services**: Serviços de inteligência artificial
- **Utility Services**: Serviços utilitários

### Camada de Integração
- **External APIs**: Integração com APIs externas
- **Configuration Manager**: Gerenciamento de configurações

### Camada de Dados
- **Data Access Layer**: Acesso aos dados
- **Offline Manager**: Gerenciamento offline

### Camada de Persistência
- **PostgreSQL Database**: Banco principal
- **SQLite Local Cache**: Cache local
- **File System**: Armazenamento de arquivos

## Como Visualizar os Diagramas

### Opção 1: PlantUML Online

1. Acesse [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Copie o conteúdo de qualquer arquivo `.puml`
3. Cole no editor online
4. Visualize o diagrama gerado

### Opção 2: VS Code com Extensão PlantUML

1. Instale a extensão "PlantUML" no VS Code
2. Abra qualquer arquivo `.puml`
3. Use `Ctrl+Shift+P` → "PlantUML: Preview Current Diagram"

### Opção 3: IntelliJ IDEA

1. Instale o plugin "PlantUML Integration"
2. Abra qualquer arquivo `.puml`
3. Use a visualização integrada

### Opção 4: Exportar para Imagem

Para gerar imagens PNG/SVG:

```bash
# Instalar PlantUML
java -jar plantuml.jar diagrama-entidades.puml

# Ou usar Docker
docker run --rm -v $(pwd):/data plantuml/plantuml diagrama-entidades.puml
```

## Estrutura de Arquivos

```
docs/uml/
├── README.md                      # Este arquivo
├── diagrama-entidades.puml        # Diagrama de classes das entidades
├── diagrama-controllers-services.puml # Diagrama de classes dos controllers/services
├── diagrama-casos-uso.puml        # Diagrama de casos de uso
├── diagrama-sequencia.puml        # Diagramas de sequência
└── diagrama-componentes.puml      # Diagrama de componentes
```

## Considerações Técnicas

### Padrões Arquiteturais

- **MVC (Model-View-Controller)**: Separação clara de responsabilidades
- **DAO (Data Access Object)**: Abstração do acesso aos dados
- **Service Layer**: Encapsulamento da lógica de negócio
- **Dependency Injection**: Inversão de controle via Spring

### Qualidade e Manutenibilidade

- **Baixo Acoplamento**: Componentes independentes
- **Alta Coesão**: Responsabilidades bem definidas
- **Extensibilidade**: Fácil adição de novas funcionalidades
- **Testabilidade**: Arquitetura favorece testes unitários

### Escalabilidade

- **Arquitetura em Camadas**: Facilita escalabilidade horizontal
- **Cache Local**: Reduz carga no servidor
- **Processamento Assíncrono**: Para operações pesadas
- **API REST**: Permite integração com outros sistemas

---

**Última Atualização**: Janeiro 2025  
**Versão**: 1.0  
**Autor**: Sistema de Documentação Automática