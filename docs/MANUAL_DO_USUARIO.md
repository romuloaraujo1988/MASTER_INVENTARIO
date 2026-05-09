# Manual do Usuário - Sistema de Inventário IFMT

**Versão 1.0.0 - Manual de Produção**  
**Instituto Federal de Mato Grosso**  
**Data: Janeiro 2025**

---

## Sumário

1. [Introdução](#introdução)
2. [Requisitos do Sistema](#requisitos-do-sistema)
3. [Instalação e Configuração](#instalação-e-configuração)
4. [Primeiros Passos](#primeiros-passos)
5. [Funcionalidades do Sistema](#funcionalidades-do-sistema)
6. [Ergonomia de Software](#ergonomia-de-software)
7. [Guia de Uso por Módulo](#guia-de-uso-por-módulo)
8. [Recursos Avançados](#recursos-avançados)
9. [Solução de Problemas](#solução-de-problemas)
10. [Manutenção e Backup](#manutenção-e-backup)
11. [Suporte Técnico](#suporte-técnico)
12. [Glossário](#glossário)

---

## Introdução

O **Sistema de Inventário IFMT** é uma solução completa e robusta para gestão patrimonial desenvolvida especificamente para o Instituto Federal de Mato Grosso. O sistema oferece controle total sobre bens patrimoniais, facilitando processos de inventário, gestão de ativos e geração de relatórios com alta precisão e eficiência.

### Principais Características

- **🖥️ Interface Intuitiva**: Desenvolvido com tecnologia Swing para máxima compatibilidade e usabilidade
- **📊 Gestão Completa**: Controle integrado de usuários, patrimônio, inventários e relatórios
- **🔗 Integração SUAP**: Importação automática e sincronização com o sistema institucional
- **📊 Análise Avançada**: Recursos de análise preditiva e otimização
- **📈 Análise Preditiva**: Recursos avançados de IA para otimização de processos
- **📱 Modo Offline**: Funcionamento completo sem conexão à internet
- **🔒 Segurança**: Controle de acesso por níveis e auditoria completa
- **📋 QR Code**: Geração e leitura de códigos QR para identificação rápida

### Benefícios para a Instituição

- **Redução de Tempo**: Automatização de processos manuais
- **Maior Precisão**: Eliminação de erros humanos em inventários
- **Controle Total**: Rastreabilidade completa de todos os bens
- **Conformidade**: Atendimento às normas de controle patrimonial
- **Economia**: Otimização de recursos e redução de custos operacionais

---

## Requisitos do Sistema

### Requisitos Mínimos

#### Hardware
- **Processador**: Intel Core i3 ou AMD equivalente
- **Memória RAM**: 4 GB
- **Espaço em Disco**: 2 GB livres
- **Resolução**: 1024x768 pixels
- **Conexão**: Rede local para banco de dados

#### Software
- **Sistema Operacional**: Windows 10/11, Linux Ubuntu 18+, macOS 10.14+
- **Java**: JDK 21 ou superior
- **Banco de Dados**: PostgreSQL 12 ou superior
- **Navegador**: Para relatórios web (Chrome, Firefox, Edge)

### Requisitos Recomendados

#### Hardware
- **Processador**: Intel Core i5 ou AMD Ryzen 5
- **Memória RAM**: 8 GB ou superior
- **Espaço em Disco**: 10 GB livres
- **Resolução**: 1920x1080 pixels ou superior
- **SSD**: Para melhor performance

#### Rede
- **Velocidade**: 100 Mbps para múltiplos usuários
- **Latência**: Máximo 50ms para servidor de banco

---

## Instalação e Configuração

### Preparação do Ambiente

#### 1. Instalação do Java 21
```bash
# Windows (usando Chocolatey)
choco install openjdk21

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-21-jdk

# Verificar instalação
java --version
```

#### 2. Instalação do PostgreSQL
```bash
# Windows
# Baixar de: https://www.postgresql.org/download/windows/

# Linux (Ubuntu/Debian)
sudo apt install postgresql postgresql-contrib

# Configurar usuário
sudo -u postgres createuser --interactive
sudo -u postgres createdb sispatrimonio
```

### Instalação do Sistema

#### Opção 1: Instalação Automática (Recomendada)
```bash
# Windows
instalar_sistema.bat

# Linux/Mac
./instalar_sistema.sh
```

#### Opção 2: Instalação Manual
1. Extrair arquivos do sistema
2. Copiar dependências para pasta `lib/`
3. Configurar permissões de execução
4. Executar primeira configuração

### Configuração Inicial do Banco

#### Criação do Banco de Dados
```sql
-- Conectar como superusuário PostgreSQL
CREATE DATABASE sispatrimonio;
CREATE USER inventario WITH PASSWORD 'senha_segura';
GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO inventario;
```

#### Configuração de Segurança
```bash
# Editar pg_hba.conf
# Adicionar linha:
host    sispatrimonio    inventario    127.0.0.1/32    md5

# Reiniciar PostgreSQL
sudo systemctl restart postgresql
```

---

## Primeiros Passos

### Primeira Execução

#### 1. Inicialização do Sistema
```bash
# Windows
cd "C:\Sistema_Inventario"
executar_aplicacao.bat

# Linux/Mac
cd /opt/sistema_inventario
./executar_aplicacao.sh
```

#### 2. Configuração Inicial do Banco
Na primeira execução, o sistema solicitará:
- **Host do Banco**: localhost (padrão)
- **Porta**: 5432 (padrão PostgreSQL)
- **Nome do Banco**: sispatrimonio
- **Usuário**: inventario
- **Senha**: [definida na instalação]

#### 3. Teste de Conectividade
- Clique em "Testar Conexão"
- Aguarde confirmação de sucesso
- Clique em "Salvar Configuração"

### Acesso ao Sistema

#### Credenciais Padrão
- **Usuário Administrador**: `admin`
- **Senha Inicial**: `admin123`
- **⚠️ IMPORTANTE**: Altere a senha na primeira utilização

#### Processo de Login
1. Insira usuário e senha
2. Selecione o campus (se aplicável)
3. Clique em "Entrar"
4. Aguarde carregamento do sistema

### Interface Principal

#### Elementos da Tela
- **🏠 Menu Principal**: Acesso a todos os módulos
- **🔧 Barra de Ferramentas**: Ações rápidas e atalhos
- **📋 Área de Trabalho**: Formulários e relatórios
- **📊 Painel Lateral**: Informações rápidas e notificações
- **📍 Barra de Status**: Conexão, usuário e hora

#### Atalhos de Teclado
- **Ctrl + N**: Novo registro
- **Ctrl + S**: Salvar
- **Ctrl + F**: Buscar
- **F1**: Ajuda
- **F5**: Atualizar
- **Esc**: Cancelar/Fechar

### Módulos Disponíveis

A tela principal apresenta os módulos disponíveis:
- **Gerenciar Usuários**
- **Gerenciar Patrimônio**
- **Coletar Inventário**
- **Relatórios**
- **Importar Dados SUAP**
- **Dashboard**


### Configurações Iniciais Recomendadas

#### 1. Configuração de Campus
- Acesse: **Configurações → Campus**
- Cadastre todos os campus da instituição
- Defina campus padrão

#### 2. Configuração de Setores
- Acesse: **Configurações → Setores**
- Importe ou cadastre setores
- Defina hierarquia organizacional

#### 3. Configuração de Usuários
- Acesse: **Gestão → Usuários**
- Cadastre usuários do sistema
- Defina perfis de acesso
- Configure permissões por módulo

#### 4. Backup Inicial
- Configure rotina de backup automático
- Teste procedimento de restauração
- Defina local de armazenamento seguro

---

## Funcionalidades do Sistema

### 1. 👥 Gestão de Usuários

**Objetivo**: Controlar acesso e permissões no sistema com segurança avançada.

#### Funcionalidades Principais
- ✅ Cadastro completo de usuários
- 🔐 Autenticação segura com hash de senhas
- 👤 Definição de perfis de acesso personalizados
- 🛡️ Controle granular de permissões por módulo
- 📊 Histórico completo de atividades e auditoria
- 🚫 Bloqueio/desbloqueio automático de contas
- ⏰ Controle de sessões e timeout automático
- 📱 Suporte a autenticação de dois fatores (2FA)

#### Perfis de Acesso
| Perfil | Descrição | Permissões |
|--------|-----------|------------|
| **Administrador** | Acesso total ao sistema | Todas as funcionalidades |
| **Gestor** | Gestão e relatórios | Relatórios, configurações, usuários |
| **Operador** | Operações diárias | Coleta, cadastro, consultas |
| **Auditor** | Auditoria e controle | Relatórios, consultas, logs |
| **Consulta** | Apenas visualização | Consultas e relatórios básicos |

#### Recursos de Segurança
- 🔒 Criptografia de senhas com bcrypt
- 🕐 Expiração automática de senhas
- 📝 Log detalhado de todas as ações
- 🚨 Alertas de tentativas de acesso inválido
- 🔄 Política de rotação de senhas

### 2. 🏢 Gestão de Patrimônio

**Objetivo**: Controlar todos os bens patrimoniais com precisão e eficiência.

#### Funcionalidades Avançadas
- 📝 Cadastro completo com validação automática
- 🏷️ Geração automática de códigos QR únicos
- 📍 Controle preciso de localização com GPS
- 📚 Histórico completo de movimentações
- 🤖 Categorização automática com IA
- 📄 Descrição resumida inteligente
- 💰 Controle de depreciação automática
- 📸 Anexo de fotos e documentos
- 🔍 Busca avançada com filtros múltiplos

#### Campos Obrigatórios
| Campo | Tipo | Descrição |
|-------|------|----------|
| **Número Patrimonial** | Numérico | Identificador único do bem |
| **Descrição** | Texto | Descrição detalhada do item |
| **Valor Aquisição** | Monetário | Valor de compra original |
| **Data Aquisição** | Data | Data de entrada no patrimônio |
| **Estado Conservação** | Lista | Novo, Bom, Regular, Ruim, Péssimo |
| **Localização** | Hierárquica | Campus → Setor → Sala |
| **Responsável** | Pessoa | Servidor responsável pelo bem |

#### Recursos de IA
- 🧠 **Categorização Inteligente**: Classificação automática por descrição
- 📝 **Descrição Resumida**: Geração automática de resumos
- 📊 **Análise Preditiva**: Previsão de necessidades de manutenção
- 🔍 **Busca Semântica**: Busca por similaridade de conteúdo

#### Descrição Resumida Inteligente
O sistema possui funcionalidade avançada para gerar descrições resumidas:

**Exemplo**:
- **Descrição Original**: "CADEIRA GIRATÓRIA EXECUTIVA COM BRAÇOS, ENCOSTO ALTO, REVESTIMENTO EM COURO SINTÉTICO COR PRETA"
- **Descrição Resumida**: "CADEIRA EXECUTIVA BRAÇOS ENCOSTO"

**Como usar**:
1. Preencha a descrição completa
2. Clique no botão **"Gerar"** ao lado do campo "Descrição Resumida"
3. O sistema criará automaticamente um resumo otimizado
4. Edite manualmente se necessário

### 3. 📋 Coleta de Inventário

**Objetivo**: Facilitar e otimizar o processo de inventário físico.

#### Funcionalidades Completas
- 📅 Planejamento de inventários por período
- 🏢 Organização por campus, setores e salas
- 📱 Coleta via QR Code com app móvel
- 🔄 Modo offline para áreas sem conectividade
- ⚡ Sincronização automática em tempo real
- 👥 Controle de equipes e participantes
- 📊 Relatórios de progresso em tempo real
- 🎯 Metas e indicadores de performance

#### Processo Otimizado
1. **📋 Planejamento**
   - Definição de escopo e cronograma
   - Seleção de setores e responsáveis
   - Configuração de equipes

2. **🚀 Execução**
   - Distribuição de tablets/smartphones
   - Coleta via QR Code ou busca manual
   - Registro de divergências

3. **📊 Consolidação**
   - Sincronização automática de dados
   - Validação de informações
   - Geração de relatórios finais

#### Recursos Móveis
- 📱 App nativo para Android/iOS
- 📷 Leitura de QR Code com câmera
- 💾 Armazenamento local para modo offline
- 🔄 Sincronização automática quando online

### 4. 📊 Sistema de Relatórios

**Objetivo**: Gerar informações estratégicas e operacionais precisas.

#### Relatórios Gerenciais
- 📈 **Dashboard Executivo**: Indicadores estratégicos
- 💰 **Análise Financeira**: Valor total do patrimônio
- 📊 **Indicadores de Performance**: KPIs do inventário
- 🎯 **Relatório de Metas**: Acompanhamento de objetivos

#### Relatórios Operacionais
- 📋 **Inventário Completo**: Lista detalhada de bens
- 🏢 **Bens por Setor**: Organização departamental
- 👤 **Responsabilidade**: Bens por responsável
- 🔄 **Movimentações**: Histórico de transferências
- ⚠️ **Divergências**: Inconsistências encontradas
- 🔍 **Bens Não Localizados**: Itens em falta

#### Formatos e Exportação
| Formato | Uso | Características |
|---------|-----|----------------|
| **PDF** | Impressão/Arquivo | Layout profissional, assinatura digital |
| **Excel** | Análise | Planilhas dinâmicas, gráficos |
| **CSV** | Integração | Importação em outros sistemas |
| **JSON** | API | Integração com sistemas externos |

### 5. 🔗 Integração SUAP

**Objetivo**: Sincronizar dados com o sistema institucional oficial.

#### Funcionalidades de Integração
- 🔄 **Sincronização Automática**: Atualização programada
- 📥 **Importação Seletiva**: Escolha de dados específicos
- ✅ **Validação Inteligente**: Verificação de consistência
- 📝 **Log Detalhado**: Registro de todas as operações
- 🔧 **Mapeamento Flexível**: Configuração de campos
- 🚨 **Alertas de Conflito**: Notificação de divergências

#### Dados Sincronizados
- 🏢 **Estrutura Organizacional**: Campus, setores, salas
- 👥 **Servidores**: Dados pessoais e lotação
- 💼 **Patrimônio**: Bens cadastrados no SUAP
- 📊 **Movimentações**: Transferências e baixas

### 6. 📊 Dashboard Inteligente

**Objetivo**: Visão estratégica em tempo real com análise preditiva.

#### Indicadores Principais
- 📈 **Total de Bens**: Quantidade e valor total
- 🔄 **Inventários Ativos**: Status em tempo real
- ⚠️ **Alertas Críticos**: Bens não localizados
- 📊 **Performance**: Indicadores de eficiência
- 💰 **Análise Financeira**: Depreciação e valores

#### Recursos Avançados
- 📊 **Gráficos Interativos**: Visualização dinâmica
- 🎯 **Metas e KPIs**: Acompanhamento de objetivos
- 📱 **Responsivo**: Adaptação a diferentes telas
- 🔄 **Atualização Automática**: Dados em tempo real
- 📧 **Alertas por Email**: Notificações automáticas



---

## Ergonomia de Software

O Sistema de Inventário IFMT foi desenvolvido seguindo princípios modernos de ergonomia de software para garantir máxima usabilidade e eficiência.

### 1. Princípios de Design

#### **Simplicidade e Clareza**
- Interface limpa e organizada
- Navegação intuitiva entre módulos
- Terminologia consistente em todo o sistema
- Ícones padronizados e reconhecíveis

#### **Eficiência de Uso**
- Atalhos de teclado para funções frequentes
- Formulários com preenchimento automático
- Busca inteligente com filtros avançados
- Operações em lote para tarefas repetitivas

#### **Prevenção de Erros**
- Validação em tempo real de dados
- Confirmações para ações críticas
- Mensagens de erro claras e orientativas
- Backup automático de dados

#### **Flexibilidade**
- Personalização de interface por usuário
- Configuração de preferências
- Múltiplos formatos de relatórios
- Adaptação a diferentes resoluções de tela

### 2. Acessibilidade

#### **Suporte a Diferentes Usuários**
- Interface responsiva para diferentes tamanhos de tela
- Contraste adequado para facilitar leitura
- Fontes legíveis e redimensionáveis
- Suporte a navegação por teclado

#### **Multilíngue**
- Interface em português brasileiro
- Terminologia técnica padronizada
- Mensagens de ajuda contextuais

### 3. Performance e Responsividade

#### **Otimização de Performance**
- Carregamento rápido de telas
- Cache inteligente de dados frequentes
- Paginação automática para grandes volumes
- Indicadores visuais de progresso

#### **Feedback Visual**
- Estados visuais claros (carregando, sucesso, erro)
- Animações suaves para transições
- Cores padronizadas para diferentes tipos de informação
- Tooltips informativos

### 4. Consistência de Interface

#### **Padrões Visuais**
- Layout consistente em todas as telas
- Posicionamento padronizado de botões
- Cores e tipografia uniformes
- Comportamento previsível de elementos

#### **Navegação**
- Menu principal sempre visível
- Breadcrumbs para orientação
- Botões "Voltar" e "Cancelar" consistentes
- Atalhos contextuais relevantes

---

## Guia de Uso por Módulo

### Módulo: Gerenciar Patrimônio

#### Cadastrar Novo Patrimônio
1. Acesse **"Gerenciar Patrimônio"** no menu principal
2. Clique em **"Novo Patrimônio"**
3. Preencha os campos obrigatórios:
   - Número do patrimônio
   - Descrição completa
   - Categoria
   - Localização (Campus/Setor/Sala)
   - Responsável
4. Use o botão **"Gerar"** para criar descrição resumida
5. Adicione observações se necessário
6. Clique em **"Salvar"**

#### Buscar Patrimônio
1. Use a barra de busca no topo da tela
2. Digite número do patrimônio, descrição ou responsável
3. Use filtros avançados:
   - Por categoria
   - Por localização
   - Por período de cadastro
   - Por status

#### Gerar Código QR
1. Selecione o patrimônio desejado
2. Clique em **"Gerar QR Code"**
3. Escolha o formato de impressão
4. Imprima a etiqueta

### Módulo: Coleta de Inventário

#### Iniciar Nova Coleta
1. Acesse **"Coletar Inventário"**
2. Clique em **"Nova Coleta"**
3. Defina:
   - Período da coleta
   - Setores incluídos
   - Responsáveis
   - Tipo de inventário
4. Clique em **"Iniciar Coleta"**

#### Realizar Coleta em Campo
1. Acesse a coleta em andamento
2. Selecione a sala/setor
3. Para cada item:
   - Escaneie o QR Code ou digite o número
   - Confirme localização
   - Registre observações
   - Marque como "Encontrado" ou "Não Encontrado"
4. Finalize a sala quando concluída

### Módulo: Relatórios

#### Gerar Relatório de Inventário
1. Acesse **"Relatórios"**
2. Selecione **"Relatório de Inventário"**
3. Configure filtros:
   - Período
   - Localização
   - Categoria
   - Status
4. Escolha formato (PDF, Excel, CSV)
5. Clique em **"Gerar Relatório"**

### Módulo: Dashboard

#### Visualizar Indicadores
1. Acesse **"Dashboard"** no menu principal
2. Visualize os cards de indicadores:
   - Total de patrimônios
   - Inventários em andamento
   - Inconsistências encontradas
   - Performance do sistema
3. Use filtros de período para análise temporal
4. Clique nos gráficos para detalhamento

---

## Recursos Avançados

### 🔧 Configurações Avançadas

#### Personalização da Interface
- **🎨 Temas**: Claro, escuro, alto contraste
- **📏 Tamanho de Fonte**: Ajustável para acessibilidade
- **🖼️ Layout**: Configuração de painéis e barras
- **⌨️ Atalhos**: Personalização de teclas de atalho

#### Configurações de Sistema
- **🔄 Backup Automático**: Agendamento de backups
- **📧 Notificações**: Configuração de alertas por email
- **🔒 Segurança**: Políticas de senha e sessão
- **📊 Performance**: Otimização de consultas

### 📱 Aplicativo Móvel

#### Funcionalidades Mobile
- **📷 Scanner QR**: Leitura rápida de códigos
- **📍 GPS**: Localização automática de bens
- **💾 Modo Offline**: Sincronização posterior
- **📊 Relatórios**: Visualização em dispositivos móveis

#### Instalação
```bash
# Android
# Baixar APK do portal institucional
# Ou instalar via Google Play Store

# iOS
# Disponível na App Store
# Buscar por "IFMT Inventário"
```

### 🔗 Integrações

#### APIs Disponíveis
- **REST API**: Integração com sistemas externos
- **WebServices**: SOAP para sistemas legados
- **Webhooks**: Notificações em tempo real
- **GraphQL**: Consultas flexíveis

#### Exemplos de Integração
```json
// Consulta de patrimônio via API
GET /api/v1/patrimonio/123456
{
  "numero": "123456",
  "descricao": "Notebook Dell Inspiron",
  "valor": 2500.00,
  "localizacao": "Campus Cuiabá - TI - Sala 101"
}
```

---

## Solução de Problemas

### 🚨 Problemas Críticos

#### 1. Sistema Não Inicia
**Sintomas**:
- Erro ao executar aplicação
- Tela branca ou travamento

**Diagnóstico**:
```bash
# Verificar Java
java --version

# Verificar logs
tail -f logs/application.log

# Testar conexão banco
psql -h localhost -U inventario -d sispatrimonio
```

**Soluções**:
1. ✅ Verificar instalação do Java 21
2. 🔧 Reconfigurar banco de dados
3. 🔄 Reinstalar dependências
4. 📞 Contatar suporte técnico

#### 2. Erro de Conexão com Banco
**Sintomas**:
- "Erro ao conectar com o banco de dados"
- Timeout de conexão

**Verificações**:
```sql
-- Testar conexão
SELECT version();

-- Verificar usuário
SELECT current_user;

-- Verificar permissões
\du inventario
```

**Soluções**:
1. 🔍 Verificar status do PostgreSQL
2. 🔑 Confirmar credenciais de acesso
3. 🌐 Testar conectividade de rede
4. 🛡️ Verificar configurações de firewall
5. 📝 Revisar arquivo pg_hba.conf

### ⚠️ Problemas Operacionais

#### 3. Performance Lenta
**Sintomas**:
- Sistema lento para carregar
- Relatórios demoram para gerar
- Interface travando

**Otimizações**:
```sql
-- Reindexar tabelas
REINDEX DATABASE sispatrimonio;

-- Atualizar estatísticas
ANALYZE;

-- Verificar consultas lentas
SELECT query, mean_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

**Soluções**:
1. 💾 Verificar espaço em disco
2. 🧠 Aumentar memória RAM
3. 🗃️ Otimizar índices do banco
4. 🧹 Limpar cache do sistema
5. 📊 Analisar consultas SQL

#### 4. Problemas com QR Code
**Sintomas**:
- QR Code não é reconhecido
- Erro ao escanear códigos
- Leitura inconsistente

**Soluções**:
1. 🖨️ Verificar qualidade da impressão
2. 💡 Ajustar iluminação do ambiente
3. 🧽 Limpar lente da câmera
4. ⚙️ Calibrar leitor de código
5. 📱 Atualizar app móvel

### 📋 Procedimentos de Diagnóstico

#### Coleta de Informações
```bash
# Informações do sistema
systeminfo | findstr /C:"OS Name" /C:"Total Physical Memory"

# Status dos serviços
sc query postgresql

# Verificar portas
netstat -an | findstr :5432

# Logs recentes
Get-Content logs\application.log -Tail 50
```

#### Logs do Sistema
| Arquivo | Localização | Conteúdo |
|---------|-------------|----------|
| `application.log` | `logs/` | Log geral da aplicação |
| `database.log` | `logs/` | Operações do banco |
| `error.log` | `logs/` | Erros do sistema |
| `audit.log` | `logs/` | Auditoria de usuários |
| `performance.log` | `logs/` | Métricas de performance |

---

## Manutenção e Backup

### 💾 Estratégia de Backup

#### Backup Automático
```bash
# Script de backup diário
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/sispatrimonio"

# Backup do banco
pg_dump -h localhost -U inventario sispatrimonio > \
  "$BACKUP_DIR/db_backup_$DATE.sql"

# Backup dos arquivos
tar -czf "$BACKUP_DIR/files_backup_$DATE.tar.gz" \
  /opt/sistema_inventario/uploads/

# Limpeza de backups antigos (>30 dias)
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
```

#### Configuração de Backup
1. **📅 Frequência**: Diário às 02:00
2. **📍 Local**: Servidor de backup dedicado
3. **🔄 Retenção**: 30 dias locais, 1 ano remoto
4. **✅ Verificação**: Teste semanal de restauração

### 🔧 Manutenção Preventiva

#### Rotinas Semanais
- 🧹 Limpeza de logs antigos
- 📊 Análise de performance
- 🔍 Verificação de integridade
- 📈 Monitoramento de espaço

#### Rotinas Mensais
- 🗃️ Otimização do banco de dados
- 🔄 Atualização de índices
- 📋 Relatório de uso do sistema
- 🔒 Auditoria de segurança

#### Rotinas Anuais
- 🆙 Planejamento de atualizações
- 📊 Análise de capacidade
- 🔐 Renovação de certificados
- 📚 Revisão de documentação

---

## Suporte Técnico

### 📞 Canais de Atendimento

#### Suporte Nível 1 (Usuários)
- **📧 Email**: suporte.inventario@ifmt.edu.br
- **📱 WhatsApp**: (65) 99999-9999
- **🕐 Horário**: Segunda a Sexta, 8h às 17h
- **⏱️ SLA**: 4 horas para resposta

#### Suporte Nível 2 (Técnico)
- **🎫 Sistema de Tickets**: https://suporte.ifmt.edu.br
- **💬 Chat Online**: Disponível no sistema
- **📞 Telefone**: (65) 3616-4100 ramal 4500
- **⏱️ SLA**: 2 horas para problemas críticos

#### Suporte Nível 3 (Desenvolvimento)
- **📧 Email**: dev.inventario@ifmt.edu.br
- **🔧 GitHub**: Issues no repositório oficial
- **📅 Agendamento**: Reuniões técnicas
- **⏱️ SLA**: 24 horas para bugs críticos

### 📋 Informações para Suporte

#### Dados Obrigatórios
```
🖥️ Sistema Operacional: _______________
☕ Versão Java: _______________
🗄️ Versão PostgreSQL: _______________
📱 Versão do Sistema: _______________
👤 Usuário Logado: _______________
🏢 Campus/Setor: _______________
📅 Data/Hora do Problema: _______________
```

#### Descrição do Problema
1. **📝 Descrição Detalhada**: O que aconteceu?
2. **🔄 Passos para Reproduzir**: Como replicar?
3. **📊 Resultado Esperado**: O que deveria acontecer?
4. **📈 Resultado Atual**: O que está acontecendo?
5. **📎 Anexos**: Screenshots, logs, arquivos

### 🔄 Processo de Atualização

#### Verificação de Atualizações
```bash
# Verificar versão atual
java -jar sistema-inventario.jar --version

# Verificar atualizações disponíveis
curl -s https://releases.ifmt.edu.br/inventario/latest
```

#### Procedimento de Atualização
1. **💾 Backup Completo**: Sistema e banco de dados
2. **🧪 Teste em Homologação**: Validar nova versão
3. **📋 Planejamento**: Definir janela de manutenção
4. **🚀 Implantação**: Aplicar atualização
5. **✅ Validação**: Testes pós-atualização
6. **📚 Documentação**: Registrar alterações

---

## Glossário

### 📚 Termos Técnicos

**API (Application Programming Interface)**
: Interface para integração entre sistemas

**Backup**
: Cópia de segurança dos dados do sistema

**Dashboard**
: Painel de controle com indicadores visuais

**IA (Inteligência Artificial)**
: Tecnologia para automatização inteligente

**QR Code**
: Código de barras bidimensional para identificação

**SLA (Service Level Agreement)**
: Acordo de nível de serviço

**SUAP**
: Sistema Unificado de Administração Pública

### 🏢 Termos Institucionais

**Campus**
: Unidade física do IFMT

**Patrimônio**
: Bem permanente da instituição

**Setor**
: Divisão administrativa dentro do campus

**Inventário**
: Processo de verificação física dos bens

**Responsável**
: Servidor responsável pela guarda do bem

---

## 📄 Informações Legais

### Licença
Este sistema é propriedade do Instituto Federal de Mato Grosso (IFMT) e está licenciado sob os termos da legislação brasileira de software público.

### Conformidade
O sistema atende às seguintes normas:
- **Lei 4.320/64**: Normas de Direito Financeiro
- **Lei 8.666/93**: Licitações e Contratos
- **NBCASP**: Normas Brasileiras de Contabilidade Aplicadas ao Setor Público
- **LGPD**: Lei Geral de Proteção de Dados

### Suporte e Desenvolvimento
**Desenvolvido por**: Equipe de TI - IFMT  
**Versão**: 1.0.0  
**Data**: Janeiro 2025  
**Contato**: dev.inventario@ifmt.edu.br

---

*Este manual foi desenvolvido para garantir o uso eficiente e seguro do Sistema de Inventário IFMT. Para sugestões de melhoria ou dúvidas, entre em contato com a equipe de desenvolvimento através dos canais oficiais de suporte.*

**© 2025 Instituto Federal de Mato Grosso - Todos os direitos reservados**