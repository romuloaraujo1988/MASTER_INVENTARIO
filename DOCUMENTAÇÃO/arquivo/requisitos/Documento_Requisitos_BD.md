# Documento de Requisitos do Banco de Dados - Sistema de Inventário

## 1. Visão Geral

Este documento especifica os requisitos para o banco de dados do Sistema de Inventário, destinado ao controle e gerenciamento de patrimônio institucional.

## 2. Objetivo

O sistema tem como objetivo gerenciar o inventário de bens patrimoniais, controlando informações sobre itens, responsáveis, localização e processos de coleta de dados.

## 3. Estrutura do Banco de Dados

### 3.1 Tabelas Principais

#### 3.1.1 TABELA_PATRIMONIO
**Descrição**: Armazena informações detalhadas sobre os bens patrimoniais.

**Campos**:
- `ID` (Chave Primária) - Identificador único do patrimônio
- `NUMERO` - Número de tombamento do bem
- `STATUS` - Status atual do bem (Ativo, Inativo, etc.)
- `DESCRICAO` - Descrição detalhada do item
- `RÓTULOS` - Tags ou categorias do bem
- `ID_RESPONSAVEL` (Chave Estrangeira) - Referência ao responsável
- `VALOR_AQUISIÇÃO` - Valor de compra do bem
- `VALOR_DEPRECIADO` - Valor atual após depreciação
- `NUMERO_NOTA_FISCAL` - Número da nota fiscal de compra
- `NÚMERO_SÉRIE` - Número de série do equipamento
- `DATA_ENTRADA` - Data de entrada no patrimônio
- `DATA_CARGA` - Data de carregamento no sistema
- `FORNECEDOR` - Nome do fornecedor
- `ID_SALA` (Chave Estrangeira) - Referência à localização
- `ESTADO_CONSERVAÇÃO` - Estado de conservação do bem

#### 3.1.2 TABELA_RESPONSAVEL
**Descrição**: Cadastro dos responsáveis pelos bens patrimoniais.

**Campos**:
- `ID` (Chave Primária) - Identificador único do responsável
- `NOME` - Nome completo do responsável

#### 3.1.3 TABELA_SALA
**Descrição**: Cadastro das salas/localizações onde os bens estão alocados.

**Campos**:
- `ID_SALA` (Chave Primária) - Identificador único da sala
- `DESCRICAO` - Descrição/nome da sala

#### 3.1.4 TABELA_SETOR
**Descrição**: Cadastro dos setores da instituição.

**Campos**:
- `ID` (Chave Primária) - Identificador único do setor
- `NOME` - Nome do setor

#### 3.1.5 TABELA_INVENTARIO
**Descrição**: Controle dos processos de inventário realizados.

**Campos**:
- `ID` (Chave Primária) - Identificador único do inventário
- `NOME` - Nome/título do inventário
- `ANO` - Ano de realização do inventário
- `DATA_INICIO` - Data de início do processo
- `DATA_FIM` - Data de término do processo
- `OBSERVACAO` - Observações gerais sobre o inventário

#### 3.1.6 TABELA_COLETOR
**Descrição**: Cadastro dos coletores responsáveis pela coleta de dados.

**Campos**:
- `ID` (Chave Primária) - Identificador único do coletor
- `NOME_COLETOR` - Nome do coletor

#### 3.1.7 TABELA_COLETA
**Descrição**: Registro das atividades de coleta de dados do inventário.

**Campos**:
- Estrutura a ser definida conforme necessidades específicas

#### 3.1.8 TABELA_USUARIO
**Descrição**: Controle de usuários do sistema com diferentes perfis de acesso e autenticação.

**Campos**:
- `ID` (Chave Primária) - Identificador único do usuário
- `LOGIN` - Nome de usuário para acesso (único)
- `SENHA_HASH` - Hash da senha criptografada
- `NOME_COMPLETO` - Nome completo do usuário
- `EMAIL` - Endereço de e-mail (único)
- `CPF` - Número do CPF (único, opcional)
- `PERFIL` - Perfil de acesso (ADMIN, SUPERVISOR, COLETOR, CONSULTA)
- `ID_SETOR` (Chave Estrangeira) - Setor de lotação do usuário
- `ATIVO` - Indica se o usuário está ativo (S/N)
- `DATA_CRIACAO` - Data de criação do usuário
- `DATA_ULTIMO_ACESSO` - Data e hora do último acesso
- `TENTATIVAS_LOGIN` - Contador de tentativas de login falhadas
- `BLOQUEADO` - Indica se o usuário está bloqueado (S/N)
- `DATA_BLOQUEIO` - Data do bloqueio (se aplicável)
- `DATA_EXPIRACAO_SENHA` - Data de expiração da senha
- `PRIMEIRO_ACESSO` - Indica se é o primeiro acesso (S/N)
- `OBSERVACOES` - Observações sobre o usuário

## 4. Relacionamentos

### 4.1 Relacionamentos Identificados

1. **PATRIMONIO → RESPONSAVEL**
   - Tipo: N:1 (Muitos patrimônios para um responsável)
   - Chave: `ID_RESPONSAVEL`

2. **PATRIMONIO → SALA**
   - Tipo: N:1 (Muitos patrimônios para uma sala)
   - Chave: `ID_SALA`
   - Descrição: Localização física do patrimônio

3. **USUARIO → SETOR**
   - Tipo: N:1 (Muitos usuários para um setor)
   - Chave: `ID_SETOR`
   - Descrição: Define o setor de lotação do usuário

### 4.2 Relacionamentos Sugeridos

1. **SALA → SETOR**
   - Tipo: N:1 (Muitas salas para um setor)
   - Justificativa: Organização hierárquica da estrutura física

2. **COLETA → INVENTARIO**
   - Tipo: N:1 (Muitas coletas para um inventário)
   - Justificativa: Controle das atividades por processo de inventário

3. **COLETA → COLETOR**
   - Tipo: N:1 (Muitas coletas para um coletor)
   - Justificativa: Rastreabilidade das atividades por responsável

4. **COLETA → PATRIMONIO**
   - Tipo: N:1 (Muitas coletas para um patrimônio)
   - Justificativa: Histórico de verificações por bem

## 5. Regras de Negócio

### 5.1 Regras Gerais
- Todos os registros devem ter campos de auditoria (data de criação, última modificação)
- Campos obrigatórios não podem ser nulos
- Relacionamentos devem manter integridade referencial

### 5.2 Regras Específicas por Tabela

#### TABELA_PATRIMONIO
- Número do patrimônio deve ser único
- Status deve ser controlado (ATIVO, INATIVO, BAIXADO)
- Valor de aquisição deve ser maior que zero
- Data de entrada não pode ser futura
- Todo patrimônio deve ter um responsável designado
- Todo patrimônio deve estar alocado em uma sala
- O número de tombamento deve ser único
- Valores monetários devem ser positivos

#### TABELA_RESPONSAVEL
- Nome do responsável é obrigatório
- Não permitir exclusão se houver patrimônios vinculados
- Um responsável pode ter múltiplos patrimônios sob sua responsabilidade

#### TABELA_SALA
- Descrição da sala é obrigatória
- Não permitir exclusão se houver patrimônios vinculados
- Toda sala deve ter uma descrição clara
- Salas devem estar vinculadas a setores para organização hierárquica

#### TABELA_INVENTARIO
- Data de fim deve ser posterior à data de início
- Não pode haver sobreposição de períodos de inventário ativo
- Todo inventário deve ter pelo menos um coletor designado

#### TABELA_USUARIO
- Login deve ser único e ter no mínimo 3 caracteres
- Email deve ser único e ter formato válido
- Senha deve atender critérios de segurança (mínimo 8 caracteres, maiúscula, minúscula, número)
- Perfil deve ser um dos valores: ADMIN, SUPERVISOR, COLETOR, CONSULTA
- Usuário é bloqueado automaticamente após 5 tentativas de login falhadas
- Senha expira a cada 90 dias
- Primeiro acesso obriga alteração de senha
- Não permitir exclusão de usuário, apenas inativação
- Usuário inativo não pode fazer login
- Administrador não pode ser inativado se for o único no sistema

## 6. Requisitos Técnicos

### 6.1 Tipos de Dados Sugeridos
- `ID`: INTEGER AUTO_INCREMENT
- `NUMERO`: VARCHAR(50) UNIQUE
- `STATUS`: VARCHAR(20)
- `DESCRICAO`: TEXT
- `VALOR_AQUISIÇÃO`: DECIMAL(15,2)
- `VALOR_DEPRECIADO`: DECIMAL(15,2)
- `DATA_ENTRADA`: DATE
- `DATA_CARGA`: DATETIME
- `NOME`: VARCHAR(255)
- `ANO`: INTEGER

### 6.2 Índices Recomendados
- Índice único em `PATRIMONIO.NUMERO`
- Índice em `PATRIMONIO.ID_RESPONSAVEL`
- Índice em `PATRIMONIO.ID_SALA`
- Índice em `INVENTARIO.ANO`
- Índice composto em `COLETA(ID_INVENTARIO, ID_PATRIMONIO)`

### 6.3 Constraints
- Chaves estrangeiras com CASCADE em UPDATE e RESTRICT em DELETE
- CHECK constraints para valores monetários positivos
- CHECK constraints para datas válidas

## 7. Considerações de Segurança

- Implementar auditoria para alterações em dados patrimoniais
- Controle de acesso baseado em perfis de usuário
- Backup regular dos dados
- Log de todas as operações de inventário

## 8. Melhorias Futuras

### 8.1 Estrutura da TABELA_COLETA
Recomenda-se definir a estrutura completa incluindo:
- `ID` - Identificador único
- `ID_INVENTARIO` - Referência ao inventário
- `ID_PATRIMONIO` - Referência ao patrimônio verificado
- `ID_COLETOR` - Referência ao coletor
- `DATA_COLETA` - Data/hora da verificação
- `STATUS_ENCONTRADO` - Se o bem foi encontrado
- `OBSERVACOES` - Observações da coleta
- `FOTO` - Referência a foto do bem (opcional)

### 8.2 Tabelas Adicionais

#### 8.2.1 TABELA_HISTORICO_PATRIMONIO
**Descrição**: Registra todas as alterações realizadas nos dados patrimoniais para auditoria e rastreabilidade.

**Campos**:
- `ID` (Chave Primária) - Identificador único do registro de histórico
- `ID_PATRIMONIO` (Chave Estrangeira) - Referência ao patrimônio alterado
- `CAMPO_ALTERADO` - Nome do campo que foi modificado
- `VALOR_ANTERIOR` - Valor antes da alteração
- `VALOR_NOVO` - Valor após a alteração
- `DATA_ALTERACAO` - Data e hora da modificação
- `USUARIO_ALTERACAO` - Usuário responsável pela alteração
- `MOTIVO_ALTERACAO` - Justificativa da mudança
- `IP_ORIGEM` - Endereço IP de onde foi feita a alteração

**Relacionamentos**:
- N:1 com PATRIMONIO (ID_PATRIMONIO)

**Regras de Negócio**:
- Registro automático para todas as alterações em PATRIMONIO
- Dados não podem ser alterados após inserção
- Retenção mínima de 5 anos

#### 8.2.2 TABELA_CATEGORIA_PATRIMONIO
**Descrição**: Classificação hierárquica dos tipos de bens patrimoniais.

**Campos**:
- `ID` (Chave Primária) - Identificador único da categoria
- `CODIGO` - Código alfanumérico da categoria
- `NOME` - Nome da categoria
- `DESCRICAO` - Descrição detalhada da categoria
- `ID_CATEGORIA_PAI` (Chave Estrangeira) - Referência à categoria superior (hierarquia)
- `VIDA_UTIL_ANOS` - Vida útil padrão em anos para depreciação
- `TAXA_DEPRECIACAO` - Taxa anual de depreciação (%)
- `ATIVO` - Indica se a categoria está ativa
- `DATA_CRIACAO` - Data de criação da categoria

**Relacionamentos**:
- 1:N com PATRIMONIO (através de novo campo ID_CATEGORIA)
- N:1 consigo mesma (ID_CATEGORIA_PAI) para hierarquia

**Regras de Negócio**:
- Código deve ser único
- Não permitir exclusão se houver patrimônios vinculados
- Hierarquia máxima de 5 níveis

#### 8.2.3 TABELA_MANUTENCAO
**Descrição**: Controle de manutenções preventivas e corretivas dos bens.

**Campos**:
- `ID` (Chave Primária) - Identificador único da manutenção
- `ID_PATRIMONIO` (Chave Estrangeira) - Referência ao patrimônio
- `TIPO_MANUTENCAO` - Tipo (Preventiva, Corretiva, Emergencial)
- `DATA_SOLICITACAO` - Data da solicitação
- `DATA_INICIO` - Data de início da manutenção
- `DATA_CONCLUSAO` - Data de conclusão
- `DESCRICAO_PROBLEMA` - Descrição do problema identificado
- `DESCRICAO_SERVICO` - Descrição do serviço realizado
- `EMPRESA_RESPONSAVEL` - Empresa que realizou a manutenção
- `TECNICO_RESPONSAVEL` - Nome do técnico responsável
- `VALOR_SERVICO` - Custo da manutenção
- `VALOR_PECAS` - Custo das peças utilizadas
- `STATUS` - Status (Solicitada, Em Andamento, Concluída, Cancelada)
- `OBSERVACOES` - Observações gerais
- `NUMERO_OS` - Número da ordem de serviço
- `GARANTIA_DIAS` - Prazo de garantia do serviço em dias

**Relacionamentos**:
- N:1 com PATRIMONIO (ID_PATRIMONIO)

**Regras de Negócio**:
- Data de conclusão deve ser posterior à data de início
- Valores devem ser positivos
- Status deve seguir fluxo definido
- Manutenções preventivas devem ser agendadas conforme cronograma

#### 8.2.4 TABELA_TRANSFERENCIA
**Descrição**: Controle de movimentações e transferências de bens entre locais e responsáveis.

**Campos**:
- `ID` (Chave Primária) - Identificador único da transferência
- `ID_PATRIMONIO` (Chave Estrangeira) - Referência ao patrimônio
- `ID_RESPONSAVEL_ORIGEM` (Chave Estrangeira) - Responsável anterior
- `ID_RESPONSAVEL_DESTINO` (Chave Estrangeira) - Novo responsável
- `ID_SALA_ORIGEM` (Chave Estrangeira) - Localização anterior
- `ID_SALA_DESTINO` (Chave Estrangeira) - Nova localização
- `DATA_SOLICITACAO` - Data da solicitação de transferência
- `DATA_TRANSFERENCIA` - Data efetiva da transferência
- `MOTIVO` - Motivo da transferência
- `OBSERVACOES` - Observações sobre a transferência
- `STATUS` - Status (Solicitada, Aprovada, Realizada, Cancelada)
- `USUARIO_SOLICITANTE` - Usuário que solicitou a transferência
- `USUARIO_APROVADOR` - Usuário que aprovou a transferência
- `DOCUMENTO_ANEXO` - Referência a documento de autorização

**Relacionamentos**:
- N:1 com PATRIMONIO (ID_PATRIMONIO)
- N:1 com RESPONSAVEL (ID_RESPONSAVEL_ORIGEM)
- N:1 com RESPONSAVEL (ID_RESPONSAVEL_DESTINO)
- N:1 com SALA (ID_SALA_ORIGEM)
- N:1 com SALA (ID_SALA_DESTINO)

**Regras de Negócio**:
- Pelo menos um dos destinos (responsável ou sala) deve ser diferente da origem
- Data de transferência deve ser posterior à data de solicitação
- Transferências devem ser aprovadas antes da execução
- Atualização automática dos dados em PATRIMONIO após confirmação

#### 8.2.5 TABELA_USUARIO
**Descrição**: Controle de usuários do sistema com diferentes perfis de acesso.

**Campos**:
- `ID` (Chave Primária) - Identificador único do usuário
- `LOGIN` - Nome de usuário para acesso
- `SENHA_HASH` - Hash da senha (nunca armazenar senha em texto)
- `NOME_COMPLETO` - Nome completo do usuário
- `EMAIL` - Endereço de e-mail
- `PERFIL` - Perfil de acesso (Admin, Operador, Consulta)
- `ATIVO` - Indica se o usuário está ativo
- `DATA_CRIACAO` - Data de criação do usuário
- `DATA_ULTIMO_ACESSO` - Data do último acesso ao sistema
- `TENTATIVAS_LOGIN` - Contador de tentativas de login falhadas
- `BLOQUEADO` - Indica se o usuário está bloqueado
- `DATA_EXPIRACAO_SENHA` - Data de expiração da senha

**Regras de Negócio**:
- Login deve ser único
- E-mail deve ser único
- Senha deve seguir política de segurança
- Bloqueio automático após 5 tentativas falhadas
- Expiração de senha a cada 90 dias

#### 8.2.6 TABELA_DEPRECIACAO
**Descrição**: Controle automático da depreciação dos bens patrimoniais.

**Campos**:
- `ID` (Chave Primária) - Identificador único do registro
- `ID_PATRIMONIO` (Chave Estrangeira) - Referência ao patrimônio
- `ANO_REFERENCIA` - Ano de referência da depreciação
- `MES_REFERENCIA` - Mês de referência da depreciação
- `VALOR_INICIAL` - Valor inicial do bem no período
- `TAXA_DEPRECIACAO` - Taxa aplicada no período
- `VALOR_DEPRECIACAO` - Valor da depreciação no período
- `VALOR_FINAL` - Valor após depreciação
- `DATA_CALCULO` - Data do cálculo
- `METODO_DEPRECIACAO` - Método utilizado (Linear, Acelerada, etc.)

**Relacionamentos**:
- N:1 com PATRIMONIO (ID_PATRIMONIO)

**Regras de Negócio**:
- Cálculo automático mensal
- Não permitir depreciação além do valor residual
- Histórico completo para auditoria contábil

#### 8.2.7 TABELA_ANEXO
**Descrição**: Armazenamento de referências a documentos e imagens relacionados aos bens.

**Campos**:
- `ID` (Chave Primária) - Identificador único do anexo
- `ID_PATRIMONIO` (Chave Estrangeira) - Referência ao patrimônio
- `TIPO_ANEXO` - Tipo (Foto, Nota Fiscal, Manual, Garantia, etc.)
- `NOME_ARQUIVO` - Nome original do arquivo
- `CAMINHO_ARQUIVO` - Caminho de armazenamento
- `TAMANHO_ARQUIVO` - Tamanho em bytes
- `TIPO_MIME` - Tipo MIME do arquivo
- `DATA_UPLOAD` - Data do upload
- `USUARIO_UPLOAD` - Usuário que fez o upload
- `DESCRICAO` - Descrição do anexo
- `HASH_ARQUIVO` - Hash para verificação de integridade

**Relacionamentos**:
- N:1 com PATRIMONIO (ID_PATRIMONIO)

**Regras de Negócio**:
- Limite de tamanho por arquivo: 10MB
- Tipos permitidos: PDF, JPG, PNG, DOC, DOCX
- Verificação de integridade obrigatória
- Backup automático dos arquivos

## 9. Conclusão

Este documento estabelece a base para implementação do banco de dados do Sistema de Inventário, garantindo integridade, rastreabilidade e eficiência no controle patrimonial da instituição.

---
**Versão**: 1.0  
**Data**: $(Get-Date -Format "dd/MM/yyyy")  
**Responsável**: Sistema de Inventário IFMT