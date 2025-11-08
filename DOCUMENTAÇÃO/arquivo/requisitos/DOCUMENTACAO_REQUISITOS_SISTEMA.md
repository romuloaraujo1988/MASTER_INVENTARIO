# Documentação de Requisitos - Sistema de Inventário IFMT

## 1. Visão Geral do Sistema

O Sistema de Inventário do IFMT é uma aplicação desktop Java desenvolvida para auxiliar a comissão de inventário na coleta, gestão e acompanhamento de patrimônios da instituição. O sistema permitirá a digitalização do processo de inventário, proporcionando maior eficiência e controle sobre os bens patrimoniais através de uma interface desktop robusta e intuitiva.

## 2. Funcionalidades Principais

### 2.1 Coleta de Códigos de Barras/Numeração Única

#### Requisitos Funcionais:
- **RF001**: O sistema deve permitir a leitura de códigos de barras através de dispositivos móveis (smartphones/tablets)
- **RF002**: O sistema deve permitir a entrada manual de numeração única de patrimônios
- **RF003**: O sistema deve validar se o código/numeração existe na base de dados
- **RF004**: O sistema deve registrar automaticamente data, hora e usuário responsável pela coleta
- **RF005**: O sistema deve permitir adicionar observações durante a coleta
- **RF006**: O sistema deve funcionar com banco de dados local e permitir sincronização com servidor central
- **RF007**: O sistema deve permitir captura de fotos do patrimônio através de webcam integrada

#### Requisitos Técnicos:
- Interface desktop Java Swing/JavaFX
- Integração com webcam ou leitor de código de barras USB
- Banco de dados local (SQLite) ou remoto (PostgreSQL)
- Sincronização de dados entre estações de trabalho

### 2.2 Gerenciamento de Usuários

#### Requisitos Funcionais:
- **RF008**: O sistema deve permitir cadastro de usuários com diferentes perfis de acesso
- **RF009**: O sistema deve implementar autenticação segura (login/senha)
- **RF010**: O sistema deve ter os seguintes perfis de usuário:
  - **Administrador**: Acesso total ao sistema
  - **Coordenador**: Gestão de equipes e relatórios
  - **Coletor**: Coleta de dados e consultas básicas
  - **Consulta**: Apenas visualização de relatórios
- **RF011**: O sistema deve permitir ativação/desativação de usuários
- **RF012**: O sistema deve registrar log de atividades dos usuários

#### Funcionalidades por Perfil:

**Administrador:**
- Gerenciar todos os usuários
- Configurar parâmetros do sistema
- Acesso a todos os relatórios
- Importar dados do SUAP
- Gerenciar setores e salas

**Coordenador:**
- Gerenciar coletores da sua equipe
- Visualizar relatórios de progresso
- Atribuir responsabilidades por setor/sala
- Acompanhar metas de coleta

**Coletor:**
- Realizar coleta de patrimônios
- Consultar dados básicos de patrimônios
- Visualizar suas atribuições
- Registrar observações

**Consulta:**
- Visualizar relatórios gerais
- Consultar status do inventário

### 2.3 Gestão de Patrimônios (CRUD)

#### Requisitos Funcionais:
- **RF013**: O sistema deve permitir cadastrar novos patrimônios
- **RF014**: O sistema deve permitir consultar patrimônios por diversos filtros:
  - Código/numeração
  - Descrição
  - Setor
  - Sala
  - Responsável
  - Status de coleta
- **RF015**: O sistema deve permitir editar informações de patrimônios
- **RF016**: O sistema deve permitir inativar patrimônios (não excluir)
- **RF017**: O sistema deve manter histórico de alterações
- **RF018**: O sistema deve permitir upload de imagens dos patrimônios

#### Campos Obrigatórios do Patrimônio:
- Código/Numeração única
- Descrição
- Setor
- Sala
- Responsável
- Data de aquisição
- Valor
- Estado de conservação
- Status de coleta

### 2.4 Geração de Relatórios

#### Requisitos Funcionais:
- **RF019**: O sistema deve gerar relatórios em formato PDF e Excel
- **RF020**: O sistema deve permitir filtros personalizados nos relatórios
- **RF021**: O sistema deve gerar os seguintes relatórios:

#### Tipos de Relatórios:

**Relatório de Inventário Geral:**
- Lista completa de patrimônios
- Status de coleta (coletado/não coletado)
- Responsável pela coleta
- Data da coleta
- Observações

**Relatório por Setor:**
- Patrimônios agrupados por setor
- Percentual de conclusão por setor
- Responsáveis por setor

**Relatório por Sala:**
- Patrimônios por sala
- Status de coleta por sala
- Responsável pela sala

**Relatório por Responsável:**
- Patrimônios sob responsabilidade de cada servidor
- Status de localização
- Divergências encontradas

**Relatório de Progresso:**
- Percentual geral de conclusão
- Metas vs. realizado
- Projeção de conclusão

**Relatório de Divergências:**
- Patrimônios não localizados
- Patrimônios com problemas
- Observações da comissão

### 2.5 Importador de Dados SUAP

#### Requisitos Funcionais:
- **RF022**: O sistema deve importar arquivos Excel/CSV gerados pelo SUAP
- **RF023**: O sistema deve validar a estrutura do arquivo antes da importação
- **RF024**: O sistema deve permitir mapeamento de campos durante a importação
- **RF025**: O sistema deve gerar relatório de importação com sucessos e erros
- **RF026**: O sistema deve permitir importação incremental (apenas novos registros)
- **RF027**: O sistema deve fazer backup antes de cada importação

#### Estrutura Esperada do Arquivo SUAP:
- Código do Patrimônio
- Descrição
- Número de Série
- Setor
- Sala
- Responsável
- Data de Aquisição
- Valor
- Estado de Conservação

#### Processo de Importação:
1. Upload do arquivo
2. Validação da estrutura
3. Mapeamento de campos
4. Prévia dos dados
5. Confirmação da importação
6. Processamento
7. Relatório de resultado

### 2.6 Painel de Acompanhamento (Dashboard)

#### Requisitos Funcionais:
- **RF028**: O sistema deve apresentar dashboard com indicadores em tempo real
- **RF029**: O sistema deve permitir filtros por período, setor, responsável
- **RF030**: O sistema deve atualizar automaticamente os indicadores

#### Indicadores do Dashboard:

**Indicadores Gerais:**
- Total de patrimônios cadastrados
- Total de patrimônios coletados
- Percentual de conclusão geral
- Meta diária vs. realizado
- Projeção de conclusão

**Por Sala:**
- Lista de salas com status de conclusão
- Responsável por cada sala
- Última atualização
- Patrimônios pendentes por sala

**Por Responsável:**
- Lista de responsáveis
- Patrimônios sob sua responsabilidade
- Status de localização
- Divergências encontradas

**Por Setor:**
- Progresso por setor
- Equipes alocadas por setor
- Tempo estimado para conclusão

**Situação do Trabalho da Comissão:**
- Membros ativos
- Produtividade diária
- Áreas em andamento
- Problemas identificados

**Meta de Coleta Estimada:**
- Meta diária baseada no prazo total
- Progresso em relação à meta
- Ajuste de metas por período
- Alertas de atraso

## 3. Requisitos Não Funcionais

### 3.1 Performance
- **RNF001**: O sistema deve suportar até 100 usuários simultâneos
- **RNF002**: Tempo de resposta máximo de 3 segundos para consultas
- **RNF003**: Tempo de resposta máximo de 5 segundos para relatórios

### 3.2 Segurança
- **RNF004**: Todas as senhas devem ser criptografadas
- **RNF005**: O sistema deve implementar controle de sessão
- **RNF006**: Backup automático diário dos dados
- **RNF007**: Log de auditoria de todas as operações

### 3.3 Usabilidade
- **RNF008**: Interface desktop intuitiva e amigável
- **RNF009**: Navegação por menus e atalhos de teclado
- **RNF010**: Suporte a diferentes resoluções de monitor

### 3.4 Compatibilidade
- **RNF011**: Compatível com Windows 10/11, Linux e macOS
- **RNF012**: Suporte a leitores de código de barras USB
- **RNF013**: Leitura de códigos de barras em diferentes formatos (Code128, EAN, QR Code)

## 4. Arquitetura do Sistema

### 4.1 Tecnologias Recomendadas

**Aplicação Desktop:**
- Java 17+ (LTS)
- JavaFX ou Swing (interface gráfica)
- Hibernate/JPA (persistência)
- H2 Database (banco local) ou PostgreSQL (banco remoto)

**Bibliotecas Principais:**
- ZXing (leitura de códigos de barras)
- Apache POI (geração de relatórios Excel)
- iText (geração de relatórios PDF)
- JFreeChart (gráficos do dashboard)
- Webcam Capture (captura de imagens)

**Infraestrutura:**
- JVM instalada nas estações de trabalho
- Banco de dados PostgreSQL (servidor central)
- Sistema de backup automatizado

### 4.2 Estrutura de Banco de Dados

**Tabelas Principais:**
- usuarios
- patrimonio
- setor
- sala
- responsavel
- coleta
- coletor
- inventario
- log_auditoria

## 5. Cronograma de Implementação

### Fase 1 (4 semanas): Infraestrutura Base
- Configuração do projeto Java
- Estrutura do banco de dados
- Interface principal (JavaFX/Swing)
- Sistema de autenticação
- CRUD básico de patrimônios

### Fase 2 (3 semanas): Funcionalidades Core
- Módulo de coleta com leitor de código de barras
- Gestão de usuários e permissões
- Importador de arquivos SUAP (Excel/CSV)
- Captura de imagens via webcam

### Fase 3 (3 semanas): Relatórios e Dashboard
- Geração de relatórios (PDF/Excel)
- Dashboard com gráficos e indicadores
- Sistema de metas e acompanhamento
- Sincronização de dados

### Fase 4 (2 semanas): Testes e Distribuição
- Testes de integração
- Criação do instalador
- Documentação de instalação
- Treinamento dos usuários

## 6. Considerações de Implantação

### 6.1 Treinamento
- Treinamento para administradores (4 horas)
- Treinamento para coordenadores (2 horas)
- Treinamento para coletores (1 hora)

### 6.2 Instalação e Configuração
- Instalação do Java Runtime Environment (JRE)
- Instalação da aplicação desktop
- Configuração da conexão com banco de dados
- Importação inicial dos dados do SUAP
- Configuração inicial de usuários e permissões

### 6.3 Suporte
- Manual do usuário
- Documentação técnica
- Suporte durante o período de inventário

## 7. Benefícios Esperados

- **Eficiência**: Redução do tempo de coleta em 60%
- **Precisão**: Eliminação de erros de transcrição
- **Controle**: Acompanhamento em tempo real do progresso
- **Transparência**: Relatórios detalhados para a gestão
- **Portabilidade**: Aplicação desktop que pode ser executada em diferentes sistemas operacionais
- **Integração**: Sincronização automática com o SUAP

## 8. Riscos e Mitigações

### Riscos Identificados:
- **Conectividade**: Problemas de internet durante a coleta
  - *Mitigação*: Modo offline com sincronização posterior

- **Adoção**: Resistência dos usuários à nova tecnologia
  - *Mitigação*: Treinamento adequado e interface intuitiva

- **Dados**: Perda ou corrupção de dados
  - *Mitigação*: Backup automático e redundância

- **Performance**: Lentidão com muitos usuários simultâneos
  - *Mitigação*: Testes de carga e otimização

## 9. Conclusão

Este sistema proporcionará à comissão de inventário do IFMT uma ferramenta moderna e eficiente para realizar o inventário patrimonial, garantindo maior precisão, controle e agilidade no processo. A implementação seguirá as melhores práticas de desenvolvimento e segurança, assegurando um sistema robusto e confiável para a instituição.