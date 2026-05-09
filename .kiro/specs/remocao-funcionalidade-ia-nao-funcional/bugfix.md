# Bugfix Requirements Document

## Introduction

Este documento descreve os requisitos para a remoção completa da funcionalidade de identificação de patrimônios por inteligência artificial (IA) usando ML Kit do aplicativo Android de inventário. A funcionalidade foi implementada mas não obteve sucesso na identificação de patrimônios por foto, causando problemas e confusão para os usuários. A remoção deve ser completa e cirúrgica, eliminando todos os componentes relacionados à IA sem afetar as funcionalidades existentes de coleta manual e por QR Code.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN o usuário acessa a tela de seleção de descrição (DescricaoSelectionActivity) THEN o sistema exibe um botão FAB "Identificar por Foto" que abre a funcionalidade de IA não funcional

1.2 WHEN o usuário acessa a tela de escolha de método de coleta (EscolhaMetodoColetaActivity) THEN o sistema exibe uma opção "Identificação por IA" que não funciona adequadamente

1.3 WHEN o usuário tenta usar a identificação por IA THEN o sistema abre a AIIdentificationActivity que não consegue identificar patrimônios corretamente

1.4 WHEN o aplicativo é compilado THEN o sistema inclui dependências do ML Kit e código relacionado à IA que não são mais necessários

1.5 WHEN o usuário navega pelas configurações THEN o sistema mantém preferências relacionadas à IA (isAIIdentificationEnabled) que não têm mais utilidade

1.6 WHEN o código é analisado THEN existem testes de propriedades (FABVisibilityPropertyTest, AILabelConfidenceColorPropertyTest) relacionados à funcionalidade de IA que devem ser removidos

1.7 WHEN o aplicativo é executado THEN existem layouts XML (activity_ai_identification.xml, item_ai_label.xml, item_descricao_sugerida.xml) e recursos relacionados à IA ocupando espaço desnecessário

### Expected Behavior (Correct)

2.1 WHEN o usuário acessa a tela de seleção de descrição (DescricaoSelectionActivity) THEN o sistema NÃO deve exibir o botão FAB "Identificar por Foto"

2.2 WHEN o usuário acessa a tela de escolha de método de coleta (EscolhaMetodoColetaActivity) THEN o sistema NÃO deve exibir a opção "Identificação por IA"

2.3 WHEN o aplicativo é compilado THEN o sistema NÃO deve incluir dependências do ML Kit nem código relacionado à IA

2.4 WHEN o código é analisado THEN NÃO devem existir referências à AIIdentificationActivity, AIIdentificationViewModel, AIIdentificationState ou componentes relacionados

2.5 WHEN o AndroidManifest é verificado THEN NÃO deve conter registro da AIIdentificationActivity

2.6 WHEN os layouts XML são verificados THEN NÃO devem existir arquivos relacionados à IA (activity_ai_identification.xml, item_ai_label.xml, item_descricao_sugerida.xml)

2.7 WHEN as preferências são verificadas THEN NÃO devem existir métodos relacionados à IA no PreferencesManager (isAIIdentificationEnabled, setAIIdentificationEnabled)

2.8 WHEN os testes são executados THEN NÃO devem existir testes relacionados à funcionalidade de IA

### Unchanged Behavior (Regression Prevention)

3.1 WHEN o usuário acessa a tela de seleção de descrição THEN o sistema deve CONTINUAR A exibir a lista de descrições disponíveis para seleção manual

3.2 WHEN o usuário acessa a tela de escolha de método de coleta THEN o sistema deve CONTINUAR A exibir as opções de coleta manual e por QR Code funcionando normalmente

3.3 WHEN o usuário seleciona uma descrição manualmente THEN o sistema deve CONTINUAR A processar a seleção e prosseguir com o fluxo de coleta

3.4 WHEN o usuário escaneia um QR Code THEN o sistema deve CONTINUAR A identificar o patrimônio e prosseguir com a coleta

3.5 WHEN o aplicativo é compilado THEN o sistema deve CONTINUAR A funcionar sem erros de compilação

3.6 WHEN o usuário realiza coletas THEN o sistema deve CONTINUAR A salvar as coletas localmente e sincronizar com o servidor

3.7 WHEN o usuário navega entre as telas do aplicativo THEN o sistema deve CONTINUAR A funcionar normalmente sem crashes ou erros

3.8 WHEN os testes existentes (não relacionados à IA) são executados THEN devem CONTINUAR A passar sem falhas
