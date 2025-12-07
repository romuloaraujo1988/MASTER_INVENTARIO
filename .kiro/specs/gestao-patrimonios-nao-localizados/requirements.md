# Requirements Document

## Introduction

Este documento especifica os requisitos para o módulo de Gestão de Patrimônios Não Localizados, que implementa um fluxo completo e normatizado para tratamento de bens patrimoniais não encontrados durante inventários, em conformidade com a legislação federal brasileira (Decreto nº 9.373/2018, IN SEDGG/ME nº 205/1988).

O módulo visa estruturar o processo desde a detecção do desaparecimento até a eventual baixa patrimonial, garantindo rastreabilidade, responsabilização adequada e conformidade legal.

## Glossary

- **SIHCP**: Sistema de Histórico e Coleta Patrimonial
- **Patrimônio Não Localizado**: Bem patrimonial que não foi encontrado durante o inventário físico
- **Sindicância**: Processo administrativo para apurar responsabilidades
- **PAD**: Processo Administrativo Disciplinar
- **Termo de Responsabilidade**: Documento que vincula um servidor à guarda de bens
- **B.O.**: Boletim de Ocorrência policial
- **Baixa Patrimonial**: Exclusão definitiva do bem do acervo patrimonial
- **Ressarcimento**: Devolução ao erário do valor do bem extraviado
- **SIADS**: Sistema Integrado de Administração de Serviços

## Requirements

### Requirement 1

**User Story:** Como gestor de patrimônio, quero registrar formalmente a não localização de um bem durante o inventário, para iniciar o processo de investigação conforme a legislação.

#### Acceptance Criteria

1. WHEN um patrimônio é marcado como não localizado durante o inventário THEN the SIHCP SHALL criar automaticamente um registro de ocorrência com status "PENDENTE_INVESTIGACAO"
2. WHEN o registro de ocorrência é criado THEN the SIHCP SHALL notificar o responsável pelo bem via sistema e registrar a data/hora da notificação
3. WHEN o responsável é notificado THEN the SIHCP SHALL definir prazo de 5 dias úteis para manifestação inicial
4. WHEN o prazo de manifestação expira sem resposta THEN the SIHCP SHALL escalar automaticamente para a chefia imediata

### Requirement 2

**User Story:** Como gestor de patrimônio, quero acompanhar um workflow estruturado de investigação, para garantir que todas as etapas legais sejam cumpridas.

#### Acceptance Criteria

1. WHEN uma ocorrência de não localização é registrada THEN the SIHCP SHALL apresentar um checklist de etapas obrigatórias baseado na legislação
2. WHEN cada etapa do workflow é concluída THEN the SIHCP SHALL registrar data, responsável e documentos anexados
3. WHEN uma etapa possui prazo legal THEN the SIHCP SHALL alertar com antecedência de 3 dias e novamente no vencimento
4. WHEN todas as etapas obrigatórias são concluídas THEN the SIHCP SHALL habilitar a opção de finalização do processo

### Requirement 3

**User Story:** Como gestor de patrimônio, quero classificar o tipo de desaparecimento, para aplicar o procedimento correto conforme a legislação.

#### Acceptance Criteria

1. WHEN o usuário classifica o desaparecimento THEN the SIHCP SHALL oferecer as opções: FURTO_ROUBO, EXTRAVIO, CASO_FORTUITO, FORCA_MAIOR, NEGLIGENCIA
2. WHEN a classificação é FURTO_ROUBO THEN the SIHCP SHALL exigir anexação de Boletim de Ocorrência como documento obrigatório
3. WHEN a classificação é NEGLIGENCIA THEN the SIHCP SHALL exigir abertura de sindicância e identificação do responsável
4. WHEN a classificação é CASO_FORTUITO ou FORCA_MAIOR THEN the SIHCP SHALL exigir documentação comprobatória do evento

### Requirement 4

**User Story:** Como gestor de patrimônio, quero gerar documentos padronizados, para formalizar cada etapa do processo.

#### Acceptance Criteria

1. WHEN o usuário solicita geração de documento THEN the SIHCP SHALL oferecer modelos para: Comunicação de Desaparecimento, Termo de Responsabilidade, Relatório de Sindicância, Parecer Conclusivo
2. WHEN um documento é gerado THEN the SIHCP SHALL preencher automaticamente os dados do patrimônio, responsável e datas
3. WHEN um documento é gerado THEN the SIHCP SHALL permitir edição antes da finalização
4. WHEN um documento é finalizado THEN the SIHCP SHALL armazenar versão PDF e vincular ao processo

### Requirement 5

**User Story:** Como gestor de patrimônio, quero calcular o valor de ressarcimento quando aplicável, para garantir a correta reposição ao erário.

#### Acceptance Criteria

1. WHEN o processo conclui com responsabilidade apurada THEN the SIHCP SHALL calcular o valor atualizado do bem usando índice IPCA
2. WHEN o cálculo é realizado THEN the SIHCP SHALL apresentar: valor original, data de aquisição, índice de correção e valor atualizado
3. WHEN há depreciação aplicável THEN the SIHCP SHALL deduzir a depreciação acumulada do valor de ressarcimento
4. WHEN o valor é calculado THEN the SIHCP SHALL gerar Guia de Recolhimento da União (GRU) ou equivalente

### Requirement 6

**User Story:** Como gestor de patrimônio, quero definir a competência para autorização da baixa, para respeitar os limites de alçada conforme legislação.

#### Acceptance Criteria

1. WHEN o valor do bem é até R$ 80.000 THEN the SIHCP SHALL indicar competência do Ordenador de Despesas
2. WHEN o valor do bem é entre R$ 80.000 e R$ 800.000 THEN the SIHCP SHALL indicar competência do Dirigente Máximo
3. WHEN o valor do bem é acima de R$ 800.000 THEN the SIHCP SHALL indicar competência do Ministro de Estado
4. WHEN a autorização é registrada THEN the SIHCP SHALL exigir identificação da autoridade competente e data

### Requirement 7

**User Story:** Como gestor de patrimônio, quero visualizar um dashboard de processos em andamento, para acompanhar prazos e pendências.

#### Acceptance Criteria

1. WHEN o usuário acessa o dashboard THEN the SIHCP SHALL exibir contadores por status: Pendentes, Em Investigação, Aguardando Autorização, Concluídos
2. WHEN existem processos com prazo vencido THEN the SIHCP SHALL destacar em vermelho e ordenar por urgência
3. WHEN o usuário filtra por período THEN the SIHCP SHALL exibir apenas processos do período selecionado
4. WHEN o usuário clica em um processo THEN the SIHCP SHALL abrir o detalhamento completo

### Requirement 8

**User Story:** Como auditor, quero gerar relatórios consolidados de patrimônios não localizados, para análise gerencial e prestação de contas.

#### Acceptance Criteria

1. WHEN o usuário solicita relatório THEN the SIHCP SHALL oferecer filtros por: período, setor, responsável, classificação, status
2. WHEN o relatório é gerado THEN the SIHCP SHALL incluir: quantidade de ocorrências, valor total envolvido, tempo médio de resolução
3. WHEN o relatório é gerado THEN the SIHCP SHALL agrupar por classificação de desaparecimento
4. WHEN o relatório é exportado THEN the SIHCP SHALL disponibilizar formatos PDF e Excel

### Requirement 9

**User Story:** Como gestor de patrimônio, quero tentar reconciliar patrimônios não localizados com itens sem etiqueta, para recuperar bens antes de iniciar processo de baixa.

#### Acceptance Criteria

1. WHEN um patrimônio é marcado como não localizado THEN the SIHCP SHALL sugerir automaticamente itens sem etiqueta com descrição similar
2. WHEN a similaridade é maior que 70% THEN the SIHCP SHALL destacar como "alta probabilidade de correspondência"
3. WHEN o usuário confirma a reconciliação THEN the SIHCP SHALL atualizar a localização do patrimônio e encerrar a ocorrência
4. WHEN a reconciliação é confirmada THEN the SIHCP SHALL gerar termo de atualização de localização

### Requirement 10

**User Story:** Como gestor de patrimônio, quero registrar a conclusão do processo com a devida baixa patrimonial, para manter o acervo atualizado.

#### Acceptance Criteria

1. WHEN todas as etapas obrigatórias são concluídas THEN the SIHCP SHALL habilitar botão de "Concluir Processo"
2. WHEN o processo é concluído com baixa THEN the SIHCP SHALL atualizar o status do patrimônio para "BAIXADO" com motivo específico
3. WHEN o processo é concluído THEN the SIHCP SHALL gerar Termo de Baixa Patrimonial com todos os dados do processo
4. WHEN a baixa é registrada THEN the SIHCP SHALL manter histórico completo para auditoria por prazo mínimo de 10 anos
