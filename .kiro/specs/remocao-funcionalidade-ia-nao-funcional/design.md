# Remoção da Funcionalidade de IA - Design Document

## Overview

Este documento detalha o design técnico para a remoção completa e cirúrgica da funcionalidade de identificação de patrimônios por inteligência artificial (IA) usando ML Kit do aplicativo Android de inventário. A funcionalidade não obteve sucesso na identificação de patrimônios por foto e está causando confusão para os usuários. A remoção será executada em uma ordem específica para minimizar riscos de quebra do aplicativo, começando pela desconexão das chamadas e navegação, seguida pela remoção dos componentes internos.

## Glossary

- **Bug_Condition (C)**: A condição que identifica o problema - quando componentes de IA não funcionais estão presentes no aplicativo
- **Property (P)**: O comportamento desejado - aplicativo sem qualquer vestígio de funcionalidade de IA
- **Preservation**: Funcionalidades de coleta manual e por QR Code que devem permanecer intactas
- **AIIdentificationActivity**: Activity principal da funcionalidade de IA que será removida
- **FAB (Floating Action Button)**: Botão flutuante "Identificar por Foto" na tela de seleção de descrição
- **ML Kit**: Biblioteca do Google para machine learning em dispositivos móveis
- **Clean Architecture**: Arquitetura em camadas (Presentation → Domain → Data) usada no projeto

## Bug Details

### Bug Condition

O bug manifesta-se quando o aplicativo contém código, dependências, layouts e recursos relacionados à funcionalidade de IA que não funciona adequadamente. A presença desses componentes causa confusão nos usuários, ocupa espaço desnecessário e aumenta a complexidade do código sem agregar valor.

**Formal Specification:**
```
FUNCTION isBugCondition(codebase)
  INPUT: codebase of type AndroidProject
  OUTPUT: boolean
  
  RETURN codebase.contains("AIIdentificationActivity")
         OR codebase.contains("AIIdentificationViewModel")
         OR codebase.contains("AIIdentificationState")
         OR codebase.contains("ml-kit dependencies")
         OR codebase.contains("ai_identification layouts")
         OR codebase.contains("isAIIdentificationEnabled")
         OR codebase.contains("FAB for AI")
         OR codebase.contains("AI option in EscolhaMetodoColetaActivity")
END FUNCTION
```

### Examples

**Exemplo 1: FAB de IA na DescricaoSelectionActivity**
- **Atual**: Usuário vê botão "Identificar por Foto" que abre funcionalidade não funcional
- **Esperado**: Botão não deve existir, usuário vê apenas lista de descrições

**Exemplo 2: Opção de IA na EscolhaMetodoColetaActivity**
- **Atual**: Usuário vê card "Identificação por IA" como opção de coleta
- **Esperado**: Card não deve existir, apenas opções de coleta manual e QR Code

**Exemplo 3: Dependências ML Kit no build.gradle**
- **Atual**: Projeto inclui `com.google.mlkit:image-labeling:17.0.7` e `object-detection:17.0.1`
- **Esperado**: Dependências removidas, tamanho do APK reduzido

**Exemplo 4: Testes de propriedades de IA**
- **Atual**: Existem `FABVisibilityPropertyTest` e `AILabelConfidenceColorPropertyTest`
- **Esperado**: Testes removidos, suite de testes mais limpa

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Coleta manual de patrimônios por seleção de descrição deve continuar funcionando
- Coleta por QR Code deve continuar funcionando normalmente
- Sincronização de coletas com servidor deve permanecer intacta
- Navegação entre telas do aplicativo deve funcionar sem crashes
- Salvamento local de coletas deve continuar operacional
- Todos os testes existentes (não relacionados à IA) devem continuar passando

**Scope:**
Todas as funcionalidades que NÃO envolvem identificação por IA devem ser completamente preservadas. Isso inclui:
- Fluxo de coleta manual (DescricaoSelectionActivity → ManualCollectionActivity)
- Fluxo de coleta por QR Code (ScannerActivity)
- Dashboard e estatísticas
- Sincronização offline/online
- Configurações do aplicativo (exceto preferências de IA)
- Exportação de relatórios

## Hypothesized Root Cause

Baseado na análise do código, os componentes de IA estão distribuídos em múltiplas camadas:

1. **Presentation Layer (6 arquivos)**:
   - `AIIdentificationActivity.kt` - Activity principal da IA
   - `AIIdentificationViewModel.kt` - ViewModel com lógica de estados
   - `AIIdentificationState.kt` - Sealed class de estados
   - `AILabelAdapter.kt` - Adapter para lista de labels
   - `DescricaoSugeridaAdapter.kt` - Adapter para sugestões
   - `BoundingBoxOverlay.kt` - View customizada para desenhar bounding boxes

2. **Domain Layer (2 arquivos)**:
   - `AIIdentificationRepository.kt` - Interface do repositório
   - Use Cases: `AnalisarImagemComBoundingBoxesUseCase.kt`, `BuscarDescricoesCorrespondentesUseCase.kt`

3. **Data Layer (5 arquivos)**:
   - `AIIdentificationRepositoryImpl.kt` - Implementação do repositório
   - `MLKitImageAnalyzer.kt` - Wrapper do ML Kit
   - `AILabel.kt` - Modelo de domínio
   - `AILabelEntity.kt` - Entity do Room (se existir)
   - `AILabelDao.kt` - DAO do Room (se existir)

4. **DI Module (1 arquivo)**:
   - `AIModule.kt` - Módulo Hilt para injeção de dependências

5. **Layouts XML (3+ arquivos)**:
   - `activity_ai_identification.xml` - Layout principal
   - `item_ai_label.xml` - Item de lista de labels
   - `item_descricao_sugerida.xml` - Item de sugestão

6. **Testes (2+ arquivos)**:
   - `FABVisibilityPropertyTest.kt`
   - `AILabelConfidenceColorPropertyTest.kt`

7. **Integrações em Activities Existentes**:
   - `DescricaoSelectionActivity.kt` - FAB e launcher
   - `EscolhaMetodoColetaActivity.kt` - Card de opção de IA

8. **Configurações**:
   - `PreferencesManager.kt` - Métodos `isAIIdentificationEnabled()` e `setAIIdentificationEnabled()`
   - `AndroidManifest.xml` - Registro da AIIdentificationActivity

9. **Dependências**:
   - `build.gradle` - ML Kit image-labeling e object-detection

## Correctness Properties

Property 1: Bug Condition - Remoção Completa de Componentes de IA

_For any_ verificação no codebase após a remoção, o sistema NÃO SHALL conter qualquer referência a AIIdentificationActivity, AIIdentificationViewModel, AIIdentificationState, dependências ML Kit, layouts de IA, preferências de IA, ou qualquer outro componente relacionado à funcionalidade de identificação por IA.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8**

Property 2: Preservation - Funcionalidades Existentes Intactas

_For any_ funcionalidade que NÃO envolve identificação por IA (coleta manual, QR Code, sincronização, dashboard), o sistema SHALL produzir exatamente o mesmo comportamento que produzia antes da remoção, preservando toda a funcionalidade de coleta, navegação, sincronização e visualização de dados.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8**

## Fix Implementation

### Changes Required

A remoção será executada em uma ordem específica para minimizar riscos:

#### Fase 1: Desconectar Navegação e Chamadas (CRÍTICO - Fazer Primeiro)

**Arquivo**: `DescricaoSelectionActivity.kt`

**Mudanças**:
1. **Remover import**: `import com.inventario.mobile.presentation.ai.AIIdentificationActivity`
2. **Remover launcher**: Deletar `aiIdentificationLauncher` (linhas 76-93)
3. **Remover método setupFabIA()**: Deletar método completo (linhas 131-145)
4. **Remover método abrirIdentificacaoIA()**: Deletar método completo (linhas 148-154)
5. **Remover chamada no onCreate()**: Remover linha que chama `setupFabIA()`
6. **Remover FAB do layout**: Comentar ou remover FAB no XML correspondente

**Arquivo**: `EscolhaMetodoColetaActivity.kt`

**Mudanças**:
1. **Remover import**: `import com.inventario.mobile.presentation.ai.AIIdentificationActivity`
2. **Remover método abrirIdentificacaoIA()**: Deletar método completo (linhas 134-141)
3. **Remover onActivityResult para IA**: Remover tratamento de `REQUEST_CODE_AI` (linhas 165-169)
4. **Remover card de IA do layout**: Comentar ou remover card no XML correspondente
5. **Remover constante REQUEST_CODE_AI**: Se existir

#### Fase 2: Remover Presentation Layer

**Arquivos a Deletar**:
1. `presentation/ai/AIIdentificationActivity.kt`
2. `presentation/ai/AIIdentificationViewModel.kt`
3. `presentation/ai/AIIdentificationState.kt`
4. `presentation/ai/AILabelAdapter.kt`
5. `presentation/ai/DescricaoSugeridaAdapter.kt`
6. `presentation/ai/BoundingBoxOverlay.kt`

**Nota**: Deletar pasta `presentation/ai/` se ficar vazia

#### Fase 3: Remover Domain Layer

**Arquivos a Deletar**:
1. `domain/repository/AIIdentificationRepository.kt`
2. `domain/usecase/AnalisarImagemComBoundingBoxesUseCase.kt`
3. `domain/usecase/BuscarDescricoesCorrespondentesUseCase.kt`
4. `domain/model/AILabel.kt` (se existir)

#### Fase 4: Remover Data Layer

**Arquivos a Deletar**:
1. `data/repository/AIIdentificationRepositoryImpl.kt`
2. `data/local/analyzer/MLKitImageAnalyzer.kt`
3. `data/local/entity/AILabelEntity.kt` (se existir)
4. `data/local/dao/AILabelDao.kt` (se existir)

#### Fase 5: Remover DI Module

**Arquivo a Deletar**:
1. `di/AIModule.kt` (se existir)

**Ou se estiver em módulo compartilhado**:
- Remover providers relacionados a IA de módulos existentes

#### Fase 6: Remover Layouts e Recursos

**Arquivos XML a Deletar**:
1. `res/layout/activity_ai_identification.xml`
2. `res/layout/item_ai_label.xml`
3. `res/layout/item_descricao_sugerida.xml`

**Recursos a Verificar e Remover**:
- Strings relacionadas a IA em `res/values/strings.xml`
- Drawables/ícones específicos de IA
- Cores específicas de IA em `res/values/colors.xml`
- Dimensões específicas de IA em `res/values/dimens.xml`

#### Fase 7: Remover Testes

**Arquivos a Deletar**:
1. `test/java/com/inventario/mobile/presentation/ai/FABVisibilityPropertyTest.kt`
2. `test/java/com/inventario/mobile/presentation/ai/AILabelConfidenceColorPropertyTest.kt`
3. Qualquer outro teste relacionado a IA

#### Fase 8: Limpar Configurações

**Arquivo**: `PreferencesManager.kt`

**Mudanças**:
1. **Remover método isAIIdentificationEnabled()**: Deletar método completo (linhas 777-780)
2. **Remover método setAIIdentificationEnabled()**: Deletar método completo (linhas 784-788)
3. **Remover comentários relacionados**: Limpar documentação sobre IA

#### Fase 9: Atualizar AndroidManifest.xml

**Arquivo**: `AndroidManifest.xml`

**Mudanças**:
1. **Remover registro da Activity**: Deletar bloco completo (linhas 164-169):
```xml
<!-- AI Identification Activity -->
<activity
    android:name=".presentation.ai.AIIdentificationActivity"
    android:exported="false"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.InventarioMobile.NoActionBar" />
```

#### Fase 10: Remover Dependências

**Arquivo**: `app/build.gradle`

**Mudanças**:
1. **Remover ML Kit Image Labeling**: Deletar linha 141:
```gradle
implementation 'com.google.mlkit:image-labeling:17.0.7'
```

2. **Remover ML Kit Object Detection**: Deletar linha 144:
```gradle
implementation 'com.google.mlkit:object-detection:17.0.1'
```

3. **Verificar CameraX**: Se CameraX (linhas 147+) for usado APENAS para IA, remover também. Se usado em outras funcionalidades (QR Code), manter.

#### Fase 11: Sync e Rebuild

**Ações**:
1. **Sync Gradle**: Executar "Sync Project with Gradle Files"
2. **Clean Build**: Executar `./gradlew clean`
3. **Rebuild**: Executar `./gradlew build`
4. **Verificar Erros**: Corrigir qualquer erro de compilação

## Testing Strategy

### Validation Approach

A estratégia de teste segue uma abordagem de três fases: primeiro, verificar que todos os componentes de IA foram removidos (exploratory bug condition checking), depois verificar que a remoção foi bem-sucedida (fix checking), e finalmente garantir que as funcionalidades existentes permanecem intactas (preservation checking).

### Exploratory Bug Condition Checking

**Goal**: Confirmar que todos os componentes de IA estão presentes ANTES da remoção. Isso estabelece a baseline e confirma que estamos removendo os componentes corretos.

**Test Plan**: Executar buscas no código e verificar a presença de arquivos, dependências e referências relacionadas à IA.

**Test Cases**:
1. **Busca por AIIdentification**: `grep -r "AIIdentification" InventarioMobile/` (deve encontrar múltiplas referências)
2. **Verificar Arquivos**: Confirmar existência de `AIIdentificationActivity.kt`, `AIIdentificationViewModel.kt`, etc.
3. **Verificar Dependências**: Confirmar presença de ML Kit no `build.gradle`
4. **Verificar Layouts**: Confirmar existência de `activity_ai_identification.xml`
5. **Verificar Manifest**: Confirmar registro da AIIdentificationActivity

**Expected Counterexamples**:
- Múltiplas referências a "AIIdentification" no código
- Arquivos de IA presentes em presentation/ai/
- Dependências ML Kit no build.gradle
- Layouts XML de IA em res/layout/

### Fix Checking

**Goal**: Verificar que para todos os componentes identificados na condição de bug, a remoção foi executada com sucesso.

**Pseudocode:**
```
FOR ALL component WHERE isBugCondition(component) DO
  result := verifyRemoval(component)
  ASSERT result.removed == true
  ASSERT result.noReferences == true
END FOR
```

**Test Cases**:

1. **Verificação de Código**:
```bash
# Não deve encontrar referências
grep -r "AIIdentification" InventarioMobile/app/src/main/java/
grep -r "isAIIdentificationEnabled" InventarioMobile/
grep -r "ml-kit\|mlkit\|object-detection" InventarioMobile/app/build.gradle
```

2. **Verificação de Arquivos**:
```bash
# Arquivos não devem existir
ls InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/ai/
ls InventarioMobile/app/src/main/res/layout/activity_ai_identification.xml
```

3. **Verificação de Manifest**:
```bash
# Não deve conter AIIdentificationActivity
grep "AIIdentificationActivity" InventarioMobile/app/src/main/AndroidManifest.xml
```

4. **Compilação Limpa**:
```bash
cd InventarioMobile
./gradlew clean build
# Deve compilar sem erros
```

5. **Tamanho do APK**:
```bash
# APK deve ser menor após remoção do ML Kit
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

### Preservation Checking

**Goal**: Verificar que para todas as funcionalidades que NÃO envolvem IA, o comportamento permanece exatamente o mesmo.

**Pseudocode:**
```
FOR ALL functionality WHERE NOT isBugCondition(functionality) DO
  ASSERT behavior_after_removal(functionality) = behavior_before_removal(functionality)
END FOR
```

**Testing Approach**: Testes manuais e automatizados para garantir que funcionalidades críticas continuam operacionais.

**Test Cases**:

1. **Coleta Manual**:
   - Abrir DescricaoSelectionActivity
   - Verificar que lista de descrições carrega
   - Selecionar uma descrição
   - Verificar que navegação para ManualCollectionActivity funciona
   - Registrar uma coleta
   - Verificar que coleta é salva localmente

2. **Coleta por QR Code**:
   - Abrir ScannerActivity
   - Escanear um QR Code válido
   - Verificar que patrimônio é identificado
   - Registrar coleta
   - Verificar que coleta é salva

3. **Sincronização**:
   - Criar coletas offline
   - Conectar à internet
   - Executar sincronização
   - Verificar que coletas são enviadas ao servidor

4. **Navegação**:
   - Navegar entre todas as telas do app
   - Verificar que não há crashes
   - Verificar que botões e menus funcionam

5. **Dashboard**:
   - Abrir DashboardFragment
   - Verificar que estatísticas carregam
   - Verificar que gráficos são exibidos

### Unit Tests

- Executar suite de testes existente (excluindo testes de IA)
- Verificar que todos os testes passam
- Verificar que cobertura de código não diminuiu significativamente

### Property-Based Tests

- Gerar múltiplas buscas no código para verificar ausência de referências a IA
- Testar compilação com diferentes configurações de build
- Verificar que APK não contém classes relacionadas a ML Kit

### Integration Tests

- Testar fluxo completo de coleta manual (início ao fim)
- Testar fluxo completo de coleta por QR Code
- Testar sincronização offline → online
- Testar navegação entre todas as telas principais
- Verificar que app inicia sem crashes
- Verificar que não há memory leaks relacionados a componentes removidos

### Regression Tests

- Executar todos os testes automatizados existentes
- Verificar que taxa de sucesso permanece 100%
- Verificar que tempo de execução não aumentou
- Verificar que não há novos warnings ou erros no logcat

## Ordem de Execução Segura

A ordem de remoção é crítica para evitar quebras:

1. **Primeiro**: Desconectar navegação (Fase 1) - Remove chamadas que causariam crashes
2. **Segundo**: Remover UI (Fase 2) - Remove componentes visuais
3. **Terceiro**: Remover lógica (Fases 3-5) - Remove camadas internas
4. **Quarto**: Remover recursos (Fases 6-7) - Remove arquivos estáticos
5. **Quinto**: Limpar configurações (Fases 8-9) - Remove configurações
6. **Sexto**: Remover dependências (Fase 10) - Remove bibliotecas externas
7. **Sétimo**: Validar (Fase 11) - Compila e testa

## Riscos e Mitigações

### Risco 1: Quebra de Compilação
**Mitigação**: Seguir ordem de remoção estrita, compilar após cada fase

### Risco 2: Crashes em Runtime
**Mitigação**: Remover navegação primeiro, testar app após cada fase

### Risco 3: Perda de Funcionalidade
**Mitigação**: Testes de preservação extensivos, backup do código antes de iniciar

### Risco 4: Dependências Compartilhadas
**Mitigação**: Verificar se CameraX é usado em outras funcionalidades antes de remover

## Métricas de Sucesso

- ✅ 0 referências a "AIIdentification" no código
- ✅ 0 arquivos relacionados a IA presentes
- ✅ 0 dependências ML Kit no build.gradle
- ✅ Compilação limpa sem erros
- ✅ Todos os testes de preservação passando
- ✅ APK menor (redução esperada: 5-10 MB)
- ✅ App funciona normalmente sem crashes
