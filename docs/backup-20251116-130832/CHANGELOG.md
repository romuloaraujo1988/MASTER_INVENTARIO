# Changelog - SIHCP

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [2.0.0] - 2025-11-08

### 🎉 Versão Major - Refatoração Completa e Novas Funcionalidades

### ✨ Adicionado

#### Sistema de Versão e Rastreabilidade
- **VersionInfo**: Nova classe utilitária para rastreamento de versão via Git
  - Exibição de commit hash, branch e data do commit
  - Detecção de mudanças não commitadas
  - Verificação de commits à frente do remote
  - Integração nas telas "Sobre" do sistema

#### Impressão de Etiquetas
- **EtiquetaPrinter**: Sistema completo de impressão de etiquetas patrimoniais
- **EtiquetaConfig**: Configuração flexível de formatos e tamanhos
- **ImpressaoEtiquetasDialog**: Interface gráfica para configuração de impressão
  - Suporte a múltiplos formatos (Padrão, Compacto, Detalhado)
  - Tamanhos configuráveis (50x30mm, 60x40mm, 70x50mm, A4)
  - Geração de QR Codes nas etiquetas
  - Preview antes da impressão
  - Controle de número de cópias

#### Interface do Usuário
- **ButtonStyleFactory**: Fábrica de botões com estilos modernos padronizados
  - Botões Primary, Secondary, Success, Danger, Warning, Info
  - Estilos consistentes em todo o sistema
- **MainFrame**: Melhorias significativas no layout
  - Botões com tamanhos máximos controlados (não crescem desproporcionalmente)
  - Layout mais responsivo e profissional
  - Indicadores de status offline/online aprimorados

#### Funcionalidades de Inventário
- **InventarioFrame**: Implementação completa de controle de inventários
  - Cálculo de progresso baseado em coletas realizadas
  - Controle de status (Abrir, Cancelar, Encerrar, Excluir)
  - Validações de integridade (não permite excluir inventários com coletas)
  - Confirmação dupla para operações críticas
  - Integração com modo offline

#### Utilitários
- **DateFormatUtils**: Padronização de formatação de datas
  - Uso de `Locale.forLanguageTag("pt-BR")` (padrão moderno)
  - Thread-safe com ThreadLocal
  - Suporte a múltiplos formatos (data, datetime, API, filename)
- **MigrarSenhasParaBCrypt**: Atualização para usar métodos não-deprecated
  - Uso de `findAllIncludingInactive()` ao invés de `listarUsuarios()`
  - Uso de `update()` ao invés de `atualizarUsuario()`

### 🔧 Modificado

#### Arquitetura e Padrões
- Refatoração completa seguindo princípios SOLID
- Separação clara de responsabilidades (MVC)
- Uso consistente de padrões de projeto (Factory, Builder, DAO)
- Documentação aprimorada com JavaDoc

#### Banco de Dados
- DAOs refatorados herdando de BaseDAO
- Redução de código duplicado (~56% de redução)
- Queries otimizadas e parametrizadas
- Melhor tratamento de transações

#### Segurança
- Migração completa para BCrypt
- Remoção de hashes MD5/SHA-1 inseguros
- Validações de senha fortalecidas
- Controle de acesso por perfil aprimorado

#### Performance
- Lazy loading de informações Git
- Cache de formatadores de data (ThreadLocal)
- Otimização de queries de banco
- Redução de operações I/O desnecessárias

### 🐛 Corrigido

- Botões do MainFrame não crescem mais desproporcionalmente ao maximizar janela
- Correção de imports não utilizados em múltiplas classes
- Warnings de deprecation resolvidos
- Tratamento adequado de casos edge em cálculos de progresso
- Validações de integridade referencial antes de exclusões

### 🔒 Segurança

- Implementação completa de BCrypt para senhas
- Remoção de algoritmos de hash inseguros
- Validação de entrada em todos os formulários
- Proteção contra SQL Injection (prepared statements)
- Controle de acesso baseado em perfis

### 📚 Documentação

- Steering rules criadas (.kiro/steering/)
  - structure.md: Organização do projeto
  - tech.md: Stack tecnológica
  - product.md: Visão do produto
- EXEMPLO_VERSAO.md: Guia de uso do sistema de versionamento
- CHANGELOG.md: Histórico de mudanças
- JavaDoc aprimorado em classes críticas

### 🏗️ Infraestrutura

- Integração com Git para rastreamento de versão
- Sistema de build otimizado
- Configurações de ambiente padronizadas
- Logs estruturados e informativos

---

## [1.2.0] - 2025-10-XX

### Adicionado
- Sistema de coleta mobile
- API REST para aplicativo Android
- Modo offline básico
- Dashboard de coleta

### Modificado
- Melhorias na interface do usuário
- Otimizações de performance

---

## [1.1.0] - 2025-09-XX

### Adicionado
- Gerenciamento de salas e setores
- Relatórios básicos
- Importação de CSV do SUAP

---

## [1.0.0] - 2025-08-XX

### Adicionado
- Versão inicial do sistema
- Cadastro de patrimônios
- Gerenciamento de usuários
- Autenticação básica

---

## Tipos de Mudanças

- **✨ Adicionado**: para novas funcionalidades
- **🔧 Modificado**: para mudanças em funcionalidades existentes
- **❌ Removido**: para funcionalidades removidas
- **🐛 Corrigido**: para correção de bugs
- **🔒 Segurança**: para correções de vulnerabilidades
- **📚 Documentação**: para mudanças na documentação
- **🏗️ Infraestrutura**: para mudanças na infraestrutura

## Versionamento Semântico

Dado um número de versão MAJOR.MINOR.PATCH:

- **MAJOR**: mudanças incompatíveis na API
- **MINOR**: funcionalidades adicionadas de forma retrocompatível
- **PATCH**: correções de bugs retrocompatíveis

Versões pré-lançamento podem ser denotadas anexando um hífen e uma série de identificadores separados por pontos (ex: 2.0.0-alpha.1).
