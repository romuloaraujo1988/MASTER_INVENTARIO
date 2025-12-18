# Diagramas UML - SIHCP

## Sistema de Histórico e Coleta Patrimonial

Documentação UML simplificada para uso em artigos acadêmicos e trabalhos científicos.

---

## 📋 Diagramas Disponíveis

| Diagrama | Arquivo | Descrição |
|----------|---------|-----------|
| Classes (Domínio) | `diagrama-classes-simplificado.puml` | Modelo de domínio com entidades principais |
| Casos de Uso | `diagrama-casos-uso-simplificado.puml` | Funcionalidades por perfil de usuário |
| Arquitetura | `diagrama-arquitetura-sistema.puml` | Visão geral dos componentes |
| Sequência (Coleta) | `diagrama-sequencia-coleta.puml` | Fluxo completo de coleta de patrimônio |
| Clean Architecture | `diagrama-clean-architecture-mobile.puml` | Arquitetura do app Android |
| Entidade-Relacionamento | `diagrama-er-simplificado.puml` | Modelo de dados do banco |
| Implantação | `diagrama-implantacao.puml` | Infraestrutura e deploy |

---

## 🎨 Como Visualizar

### Opção 1: PlantUML Online (Recomendado)

1. Acesse [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/)
2. Copie o conteúdo do arquivo `.puml`
3. Cole no editor
4. Clique em "Submit" para gerar a imagem

### Opção 2: VS Code

1. Instale a extensão **PlantUML**
2. Abra o arquivo `.puml`
3. Pressione `Alt+D` para visualizar

### Opção 3: IntelliJ IDEA

1. Instale o plugin **PlantUML Integration**
2. Abra o arquivo `.puml`
3. Use a aba de preview lateral

---

## 📤 Exportar para Imagem

### PNG (Alta Resolução)

```bash
# Usando PlantUML JAR
java -jar plantuml.jar -tpng diagrama-classes-simplificado.puml

# Usando Docker
docker run --rm -v $(pwd):/data plantuml/plantuml -tpng *.puml
```

### SVG (Vetorial - Ideal para Artigos)

```bash
java -jar plantuml.jar -tsvg diagrama-classes-simplificado.puml
```

### PDF

```bash
java -jar plantuml.jar -tpdf diagrama-classes-simplificado.puml
```

---

## 📊 Descrição dos Diagramas

### 1. Diagrama de Classes (Domínio)

Apresenta as entidades principais do sistema organizadas em pacotes:

- **Estrutura Organizacional**: Campus, Setor, Sala, Responsável
- **Gestão de Patrimônio**: Patrimônio, QRCode
- **Processo de Inventário**: Inventário, Coleta, Participante, SalaInventário
- **Controle de Acesso**: Usuário, PerfilUsuário

**Uso acadêmico**: Ideal para apresentar o modelo de domínio e relacionamentos entre entidades.

### 2. Diagrama de Casos de Uso

Mostra as funcionalidades do sistema organizadas por:

- **Atores**: Administrador, Supervisor, Coletor, Consulta
- **Pacotes**: Gestão de Patrimônio, Processo de Inventário, Coleta de Dados, Relatórios, Administração

**Uso acadêmico**: Demonstra os requisitos funcionais e perfis de acesso.

### 3. Diagrama de Arquitetura

Visão de alto nível com três camadas principais:

- **Aplicações Cliente**: Desktop (Swing) e Mobile (Android)
- **Servidor**: API REST (Spring Boot) e Serviços
- **Persistência**: PostgreSQL e SQLite

**Uso acadêmico**: Apresenta a arquitetura geral e tecnologias utilizadas.

### 4. Diagrama de Sequência (Coleta)

Fluxo detalhado do processo de coleta incluindo:

- Autenticação JWT
- Sincronização inicial
- Coleta online e offline
- Sincronização de pendências

**Uso acadêmico**: Demonstra o fluxo principal do sistema e tratamento offline.

### 5. Diagrama Clean Architecture (Mobile)

Arquitetura do aplicativo Android seguindo Clean Architecture + MVVM:

- **Presentation**: Activities, ViewModels, States
- **Domain**: Use Cases, Models, Repository Interfaces
- **Data**: Repository Impl, Room, Retrofit

**Uso acadêmico**: Apresenta padrões arquiteturais modernos para apps mobile.

### 6. Diagrama Entidade-Relacionamento

Modelo de dados do banco PostgreSQL com:

- Entidades e atributos
- Chaves primárias e estrangeiras
- Relacionamentos (1:N, N:M)

**Uso acadêmico**: Documenta a estrutura do banco de dados.

### 7. Diagrama de Implantação

Infraestrutura de deploy mostrando:

- Servidor de aplicação (JVM + PostgreSQL)
- Estação desktop
- Dispositivo mobile com SQLite local
- Conexões de rede

**Uso acadêmico**: Apresenta a infraestrutura necessária para o sistema.

---

## 🔧 Tecnologias do Sistema

| Componente | Tecnologia | Versão |
|------------|------------|--------|
| Backend | Java + Spring Boot | 21 / 3.2 |
| Desktop | Java Swing | 21 |
| Mobile | Kotlin + Android | 1.9 / SDK 34 |
| Banco Principal | PostgreSQL | 12+ |
| Banco Local | SQLite + Room | 2.6 |
| Autenticação | JWT | - |
| Build | Maven / Gradle | 3.6+ / 8.0+ |

---

## 📝 Citação

Para citar este sistema em trabalhos acadêmicos:

```
SIHCP - Sistema de Histórico e Coleta Patrimonial.
Instituto Federal de Mato Grosso (IFMT), 2025.
```

---

## 📁 Estrutura de Arquivos

```
docs/uml/academico/
├── README.md                           # Este arquivo
├── diagrama-classes-simplificado.puml  # Modelo de domínio
├── diagrama-casos-uso-simplificado.puml # Casos de uso
├── diagrama-arquitetura-sistema.puml   # Arquitetura geral
├── diagrama-sequencia-coleta.puml      # Fluxo de coleta
├── diagrama-clean-architecture-mobile.puml # Arquitetura mobile
├── diagrama-er-simplificado.puml       # Modelo ER
└── diagrama-implantacao.puml           # Infraestrutura
```

---

**Última atualização**: Dezembro 2025  
**Versão**: 2.0
