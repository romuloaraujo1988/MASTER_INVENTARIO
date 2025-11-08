# Requirements Document

## Introduction

Este documento especifica os requisitos para implementação das funcionalidades de conformidade com a LGPD (Lei Geral de Proteção de Dados) no Sistema de Inventário de Patrimônio do IFMT. O foco inicial é na criação da Política de Privacidade e implementação do Termo de Consentimento, que são requisitos críticos e urgentes identificados na análise de conformidade.

## Glossary

- **Sistema**: Sistema de Inventário de Patrimônio do IFMT
- **Titular**: Pessoa física a quem se referem os dados pessoais (usuários do sistema)
- **LGPD**: Lei Geral de Proteção de Dados (Lei nº 13.709/2018)
- **Política de Privacidade**: Documento que informa aos titulares como seus dados pessoais são coletados, usados e protegidos
- **Termo de Consentimento**: Manifestação livre, informada e inequívoca pela qual o titular concorda com o tratamento de seus dados pessoais
- **DPO**: Data Protection Officer (Encarregado de Proteção de Dados)
- **Consentimento**: Autorização expressa do titular para tratamento de seus dados pessoais

## Requirements

### Requirement 1

**User Story:** Como usuário do sistema, eu quero visualizar a Política de Privacidade antes de fazer login, para que eu possa entender como meus dados pessoais serão tratados.

#### Acceptance Criteria

1. WHEN o usuário acessa a tela de login, THE Sistema SHALL exibir um link "Política de Privacidade" visível e acessível
2. WHEN o usuário clica no link "Política de Privacidade", THE Sistema SHALL abrir uma janela modal ou nova tela exibindo o documento completo da Política de Privacidade
3. THE Política de Privacidade SHALL conter as seguintes seções obrigatórias:
   - Dados coletados (nome, email, matrícula, CPF de responsáveis)
   - Finalidade do tratamento
   - Base legal (execução de contrato e obrigação legal)
   - Prazo de retenção dos dados
   - Direitos dos titulares
   - Medidas de segurança implementadas
   - Contato do DPO/Encarregado
4. THE Sistema SHALL permitir que o usuário role o documento completo para leitura
5. THE Sistema SHALL exibir a data da última atualização da Política de Privacidade

### Requirement 2

**User Story:** Como usuário novo do sistema, eu quero fornecer meu consentimento explícito para o tratamento dos meus dados pessoais, para que o sistema esteja em conformidade com a LGPD.

#### Acceptance Criteria

1. WHEN um usuário realiza o primeiro login no sistema, THE Sistema SHALL exibir uma tela de consentimento antes de permitir acesso às funcionalidades
2. THE tela de consentimento SHALL exibir um resumo da Política de Privacidade com link para o documento completo
3. THE tela de consentimento SHALL conter um checkbox com o texto "Li e aceito a Política de Privacidade e autorizo o tratamento dos meus dados pessoais conforme descrito"
4. THE Sistema SHALL desabilitar o botão "Continuar" WHILE o checkbox não estiver marcado
5. WHEN o usuário marca o checkbox e clica em "Continuar", THE Sistema SHALL registrar o consentimento no banco de dados com data/hora e IP de origem
6. THE Sistema SHALL permitir acesso às funcionalidades WHEN o consentimento for registrado com sucesso
7. IF o usuário fechar a tela sem fornecer consentimento, THEN THE Sistema SHALL realizar logout automático e exibir mensagem informando que o consentimento é obrigatório

### Requirement 3

**User Story:** Como administrador do sistema, eu quero visualizar o histórico de consentimentos dos usuários, para que eu possa comprovar a conformidade com a LGPD em auditorias.

#### Acceptance Criteria

1. THE Sistema SHALL criar uma tabela TABELA_CONSENTIMENTO no banco de dados para armazenar os consentimentos
2. THE tabela SHALL conter os campos: ID, ID_USUARIO, TIPO_CONSENTIMENTO, DATA_CONSENTIMENTO, IP_ORIGEM, VERSAO_POLITICA, CONSENTIMENTO_ATIVO
3. WHEN um administrador acessa a funcionalidade de "Gestão de Consentimentos", THE Sistema SHALL exibir uma lista com todos os consentimentos registrados
4. THE lista SHALL permitir filtrar por usuário, data e status do consentimento
5. THE Sistema SHALL permitir exportar o relatório de consentimentos em formato PDF ou CSV

### Requirement 4

**User Story:** Como usuário do sistema, eu quero poder revogar meu consentimento a qualquer momento, para exercer meu direito garantido pela LGPD.

#### Acceptance Criteria

1. WHEN um usuário acessa seu perfil, THE Sistema SHALL exibir uma seção "Privacidade e Consentimento"
2. THE seção SHALL exibir o status atual do consentimento (ativo/revogado) e a data de concessão
3. THE Sistema SHALL exibir um botão "Revogar Consentimento" WHEN o consentimento estiver ativo
4. WHEN o usuário clica em "Revogar Consentimento", THE Sistema SHALL exibir uma mensagem de confirmação explicando as consequências (perda de acesso ao sistema)
5. IF o usuário confirma a revogação, THEN THE Sistema SHALL marcar o consentimento como revogado no banco de dados e realizar logout automático
6. THE Sistema SHALL enviar notificação ao DPO sobre a revogação do consentimento

### Requirement 5

**User Story:** Como DPO (Encarregado de Dados), eu quero ser notificado quando um usuário revogar seu consentimento, para que eu possa tomar as ações necessárias conforme a LGPD.

#### Acceptance Criteria

1. WHEN um usuário revoga seu consentimento, THE Sistema SHALL registrar a revogação com data/hora e motivo (se fornecido)
2. THE Sistema SHALL enviar email automático ao DPO informando sobre a revogação
3. THE email SHALL conter: nome do usuário, data da revogação, data do consentimento original
4. THE Sistema SHALL criar uma tarefa pendente para o DPO avaliar se há obrigação legal de manter os dados ou se devem ser excluídos

### Requirement 6

**User Story:** Como administrador do sistema, eu quero atualizar a Política de Privacidade quando necessário, para manter o documento sempre atualizado conforme mudanças no sistema ou na legislação.

#### Acceptance Criteria

1. THE Sistema SHALL permitir que administradores editem a Política de Privacidade através de uma interface administrativa
2. WHEN a Política de Privacidade é atualizada, THE Sistema SHALL incrementar o número da versão automaticamente
3. THE Sistema SHALL registrar a data da atualização e o usuário responsável
4. WHEN a Política de Privacidade é atualizada, THE Sistema SHALL marcar todos os consentimentos existentes como "requer nova aceitação"
5. WHEN um usuário com consentimento desatualizado faz login, THE Sistema SHALL exibir a nova Política de Privacidade e solicitar novo consentimento
6. THE Sistema SHALL manter histórico de todas as versões anteriores da Política de Privacidade

### Requirement 7

**User Story:** Como usuário do sistema, eu quero visualizar minha Política de Privacidade aceita a qualquer momento, para relembrar os termos que aceitei.

#### Acceptance Criteria

1. WHEN um usuário acessa seu perfil, THE Sistema SHALL exibir um link "Ver Política de Privacidade Aceita"
2. WHEN o usuário clica no link, THE Sistema SHALL exibir a versão exata da Política de Privacidade que foi aceita pelo usuário
3. THE Sistema SHALL exibir a data em que o usuário aceitou aquela versão
4. THE Sistema SHALL permitir que o usuário baixe uma cópia em PDF da Política de Privacidade aceita

### Requirement 8

**User Story:** Como desenvolvedor do sistema, eu quero que o consentimento seja validado em todas as operações que envolvem dados pessoais, para garantir conformidade contínua com a LGPD.

#### Acceptance Criteria

1. THE Sistema SHALL verificar se o usuário possui consentimento ativo WHEN o usuário tenta acessar qualquer funcionalidade após o login
2. IF o usuário não possui consentimento ativo, THEN THE Sistema SHALL redirecionar para a tela de consentimento
3. THE Sistema SHALL registrar em log todas as tentativas de acesso sem consentimento válido
4. THE Sistema SHALL bloquear operações de leitura/escrita de dados pessoais WHEN o consentimento não estiver ativo

### Requirement 9

**User Story:** Como auditor externo, eu quero verificar que o sistema está coletando consentimento adequadamente, para validar a conformidade com a LGPD.

#### Acceptance Criteria

1. THE Sistema SHALL gerar relatório de auditoria contendo:
   - Total de usuários cadastrados
   - Total de usuários com consentimento ativo
   - Total de usuários com consentimento revogado
   - Total de usuários sem consentimento
   - Histórico de atualizações da Política de Privacidade
2. THE relatório SHALL ser exportável em formato PDF com assinatura digital
3. THE Sistema SHALL registrar quem gerou o relatório e quando
4. THE relatório SHALL incluir evidências de conformidade (screenshots da tela de consentimento, cópia da Política de Privacidade)

### Requirement 10

**User Story:** Como usuário do sistema, eu quero que meu consentimento seja coletado de forma clara e específica, para que eu entenda exatamente o que estou autorizando.

#### Acceptance Criteria

1. THE tela de consentimento SHALL usar linguagem clara e acessível, evitando termos técnicos excessivos
2. THE Sistema SHALL separar diferentes tipos de consentimento (uso do sistema, comunicações, etc.) em checkboxes distintos
3. THE Sistema SHALL exibir ícones visuais indicando o que cada tipo de consentimento permite
4. THE Sistema SHALL permitir consentimento granular (usuário pode aceitar apenas o essencial e recusar comunicações opcionais)
5. THE Sistema SHALL destacar visualmente quais consentimentos são obrigatórios e quais são opcionais
