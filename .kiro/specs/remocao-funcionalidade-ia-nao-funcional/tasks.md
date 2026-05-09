# Implementation Plan - Remoção da Funcionalidade de IA

## Overview

Este plano de implementação detalha as tarefas para remover completamente a funcionalidade de identificação de patrimônios por IA do aplicativo Android. A remoção será executada em 11 fases sequenciais para minimizar riscos de quebra, começando pela desconexão de navegação e chamadas, seguida pela remoção dos componentes internos.

**IMPORTANTE**: As fases devem ser executadas NA ORDEM especificada. Não pule fases nem execute fora de ordem.

---

## Tasks

- [x] 1. Fase 1: Desconectar Navegação e Chamadas (CRÍTICO - Fazer Primeiro)
  
  - [x] 1.1 Desconectar DescricaoSelectionActivity
    - Remover import `AIIdentificationActivity`
    - Deletar `aiIdentificationLauncher` (linhas 76-93)
    - Deletar método `setupFabIA()` (linhas 131-145)
    - Deletar método `abrirIdentificacaoIA()` (linhas 148-154)
    - Remover chamada `setupFabIA()` do `onCreate()`
    - Comentar ou remover FAB do layout XML correspondente
    - _Requirements: 2.1, 2.4_
  
  - [x] 1.2 Desconectar EscolhaMetodoColetaActivity
    - Remover import `AIIdentificationActivity`
    - Deletar método `abrirIdentificacaoIA()` (linhas 134-141)
    - Remover tratamento de `REQUEST_CODE_AI` no `onActivityResult` (linhas 165-169)
    - Remover constante `REQUEST_CODE_AI` (se existir)
    - Comentar ou remover card de IA do layout XML correspondente
    - _Requirements: 2.2, 2.4_
  
  - [x] 1.3 Compilar e testar navegação
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Testar abertura de DescricaoSelectionActivity (não deve ter FAB de IA)
    - Testar abertura de EscolhaMetodoColetaActivity (não deve ter opção de IA)
    - _Requirements: 3.5, 3.7_

- [x] 2. Fase 2: Remover Presentation Layer
  
  - [x] 2.1 Deletar arquivos da camada de apresentação
    - Deletar `presentation/ai/AIIdentificationActivity.kt`
    - Deletar `presentation/ai/AIIdentificationViewModel.kt`
    - Deletar `presentation/ai/AIIdentificationState.kt`
    - Deletar `presentation/ai/AILabelAdapter.kt`
    - Deletar `presentation/ai/DescricaoSugeridaAdapter.kt`
    - Deletar `presentation/ai/BoundingBoxOverlay.kt`
    - Deletar pasta `presentation/ai/` se ficar vazia
    - _Requirements: 2.4_
  
  - [x] 2.2 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "AIIdentificationActivity" InventarioMobile/app/src/main/java/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.4_

- [x] 3. Fase 3: Remover Domain Layer
  
  - [x] 3.1 Deletar arquivos da camada de domínio
    - Deletar `domain/repository/AIIdentificationRepository.kt`
    - Deletar `domain/usecase/AnalisarImagemComBoundingBoxesUseCase.kt`
    - Deletar `domain/usecase/BuscarDescricoesCorrespondentesUseCase.kt`
    - Deletar `domain/model/AILabel.kt` (se existir)
    - _Requirements: 2.4_
  
  - [x] 3.2 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "AIIdentificationRepository\|AnalisarImagem\|BuscarDescricoesCorrespondentes" InventarioMobile/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.4_

- [x] 4. Fase 4: Remover Data Layer
  
  - [x] 4.1 Deletar arquivos da camada de dados
    - Deletar `data/repository/AIIdentificationRepositoryImpl.kt`
    - Deletar `data/local/analyzer/MLKitImageAnalyzer.kt`
    - Deletar `data/local/entity/AILabelEntity.kt` (se existir)
    - Deletar `data/local/dao/AILabelDao.kt` (se existir)
    - _Requirements: 2.4_
  
  - [x] 4.2 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "MLKitImageAnalyzer\|AILabelEntity\|AILabelDao" InventarioMobile/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.4_

- [x] 5. Fase 5: Remover DI Module
  
  - [x] 5.1 Deletar módulo de injeção de dependência
    - Verificar se existe `di/AIModule.kt`
    - Se existir, deletar arquivo completo
    - Se não existir, verificar módulos compartilhados (DatabaseModule, RepositoryModule, etc.)
    - Remover providers relacionados a IA de módulos compartilhados
    - _Requirements: 2.4_
  
  - [x] 5.2 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "AIModule\|provideAI\|bindAI" InventarioMobile/app/src/main/java/com/inventario/mobile/di/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.4_

- [x] 6. Fase 6: Remover Layouts e Recursos
  
  - [x] 6.1 Deletar layouts XML
    - Deletar `res/layout/activity_ai_identification.xml`
    - Deletar `res/layout/item_ai_label.xml`
    - Deletar `res/layout/item_descricao_sugerida.xml`
    - _Requirements: 2.6_
  
  - [x] 6.2 Remover recursos relacionados
    - Verificar e remover strings relacionadas a IA em `res/values/strings.xml`
    - Verificar e remover drawables/ícones específicos de IA
    - Verificar e remover cores específicas de IA em `res/values/colors.xml`
    - Verificar e remover dimensões específicas de IA em `res/values/dimens.xml`
    - _Requirements: 2.6_
  
  - [x] 6.3 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "ai_identification\|ai_label" InventarioMobile/app/src/main/res/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.6_

- [x] 7. Fase 7: Remover Testes
  
  - [x] 7.1 Deletar testes relacionados a IA
    - Deletar `test/java/com/inventario/mobile/presentation/ai/FABVisibilityPropertyTest.kt`
    - Deletar `test/java/com/inventario/mobile/presentation/ai/AILabelConfidenceColorPropertyTest.kt`
    - Verificar e deletar qualquer outro teste relacionado a IA
    - Deletar pasta de testes `test/.../ai/` se ficar vazia
    - _Requirements: 2.8_
  
  - [x] 7.2 Executar suite de testes
    - Executar `./gradlew test`
    - Verificar que todos os testes (não relacionados a IA) passam
    - Verificar que não há testes falhando devido à remoção
    - _Requirements: 3.8_

- [x] 8. Fase 8: Limpar Configurações
  
  - [x] 8.1 Limpar PreferencesManager
    - Abrir `PreferencesManager.kt`
    - Deletar método `isAIIdentificationEnabled()` (linhas 777-780)
    - Deletar método `setAIIdentificationEnabled()` (linhas 784-788)
    - Remover comentários relacionados a IA
    - _Requirements: 2.7_
  
  - [x] 8.2 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "isAIIdentificationEnabled\|setAIIdentificationEnabled" InventarioMobile/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.7_

- [x] 9. Fase 9: Atualizar AndroidManifest.xml
  
  - [x] 9.1 Remover registro da Activity
    - Abrir `AndroidManifest.xml`
    - Deletar bloco completo de registro da AIIdentificationActivity (linhas 164-169)
    - Remover comentário "AI Identification Activity"
    - _Requirements: 2.5_
  
  - [x] 9.2 Compilar e verificar
    - Executar `./gradlew clean build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep "AIIdentificationActivity" InventarioMobile/app/src/main/AndroidManifest.xml`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.5_

- [x] 10. Fase 10: Remover Dependências
  
  - [x] 10.1 Remover dependências ML Kit
    - Abrir `app/build.gradle`
    - Deletar linha 141: `implementation 'com.google.mlkit:image-labeling:17.0.7'`
    - Deletar linha 144: `implementation 'com.google.mlkit:object-detection:17.0.1'`
    - _Requirements: 2.3_
  
  - [x] 10.2 Verificar CameraX
    - Verificar se CameraX (linhas 147+) é usado em outras funcionalidades (QR Code)
    - Se usado APENAS para IA: remover dependências CameraX
    - Se usado em outras funcionalidades: MANTER dependências CameraX
    - Documentar decisão tomada
    - _Requirements: 2.3, 3.4_
  
  - [x] 10.3 Sync Gradle e verificar
    - Executar "Sync Project with Gradle Files" no Android Studio
    - Executar `./gradlew clean`
    - Executar `./gradlew build`
    - Verificar que não há erros de compilação
    - Buscar referências: `grep -r "ml-kit\|mlkit\|object-detection" InventarioMobile/app/build.gradle`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.3_

- [-] 11. Fase 11: Validação Final
  
  - [x] 11.1 Verificação de código completa
    - Buscar todas as referências: `grep -r "AIIdentification" InventarioMobile/`
    - Resultado esperado: 0 referências encontradas
    - Buscar referências a ML Kit: `grep -r "mlkit\|MLKit" InventarioMobile/app/src/`
    - Resultado esperado: 0 referências encontradas
    - _Requirements: 2.4_
  
  - [x] 11.2 Compilação limpa final
    - Executar `./gradlew clean`
    - Executar `./gradlew build`
    - Verificar que compilação é bem-sucedida sem erros
    - Verificar que não há warnings relacionados a componentes removidos
    - _Requirements: 3.5_
  
  - [x] 11.3 Verificar tamanho do APK
    - Compilar APK debug: `./gradlew assembleDebug`
    - Verificar tamanho: `ls -lh app/build/outputs/apk/debug/app-debug.apk`
    - Comparar com tamanho anterior (esperado: redução de 5-10 MB)
    - Documentar redução de tamanho
    - _Requirements: 2.3_
  
  - [ ] 11.4 Testes de preservação - Coleta Manual
    - Instalar APK em dispositivo de teste
    - Abrir DescricaoSelectionActivity
    - Verificar que lista de descrições carrega normalmente
    - Verificar que FAB de IA NÃO está presente
    - Selecionar uma descrição
    - Verificar que navegação para ManualCollectionActivity funciona
    - Registrar uma coleta
    - Verificar que coleta é salva localmente
    - _Requirements: 3.1, 3.3, 3.6_
  
  - [ ] 11.5 Testes de preservação - Coleta por QR Code
    - Abrir ScannerActivity
    - Escanear um QR Code válido
    - Verificar que patrimônio é identificado corretamente
    - Registrar coleta
    - Verificar que coleta é salva
    - _Requirements: 3.4, 3.6_
  
  - [ ] 11.6 Testes de preservação - Sincronização
    - Criar 3-5 coletas offline
    - Conectar à internet
    - Executar sincronização
    - Verificar que coletas são enviadas ao servidor com sucesso
    - Verificar que status de sincronização é atualizado
    - _Requirements: 3.6_
  
  - [ ] 11.7 Testes de preservação - Navegação
    - Navegar entre todas as telas principais do app
    - Verificar que não há crashes
    - Verificar que botões e menus funcionam normalmente
    - Verificar que transições de tela são suaves
    - _Requirements: 3.7_
  
  - [ ] 11.8 Testes de preservação - Dashboard
    - Abrir DashboardFragment
    - Verificar que estatísticas carregam corretamente
    - Verificar que gráficos são exibidos
    - Verificar que dados estão corretos
    - _Requirements: 3.2_
  
  - [ ] 11.9 Executar suite de testes automatizados
    - Executar `./gradlew test`
    - Verificar que todos os testes passam (100% de sucesso)
    - Verificar que não há novos testes falhando
    - Verificar que cobertura de código não diminuiu significativamente
    - _Requirements: 3.8_
  
  - [x] 11.10 Verificação final de arquivos
    - Verificar que pasta `presentation/ai/` não existe
    - Verificar que layouts de IA não existem em `res/layout/`
    - Verificar que testes de IA não existem em `test/`
    - Verificar que AndroidManifest não contém AIIdentificationActivity
    - _Requirements: 2.4, 2.5, 2.6, 2.8_

- [-] 12. Checkpoint Final - Documentação e Entrega
  
  - [x] 12.1 Documentar mudanças
    - Criar resumo das mudanças realizadas
    - Listar todos os arquivos deletados
    - Documentar redução de tamanho do APK
    - Documentar resultados dos testes de preservação
  
  - [ ] 12.2 Verificar métricas de sucesso
    - ✅ 0 referências a "AIIdentification" no código
    - ✅ 0 arquivos relacionados a IA presentes
    - ✅ 0 dependências ML Kit no build.gradle
    - ✅ Compilação limpa sem erros
    - ✅ Todos os testes de preservação passando
    - ✅ APK menor (redução de 5-10 MB)
    - ✅ App funciona normalmente sem crashes
  
  - [ ] 12.3 Perguntar ao usuário
    - Confirmar que todas as fases foram executadas com sucesso
    - Perguntar se há alguma dúvida ou problema encontrado
    - Confirmar que app está funcionando conforme esperado
    - Solicitar feedback sobre o processo de remoção

---

## Notes

### Ordem de Execução

**CRÍTICO**: As fases devem ser executadas EXATAMENTE nesta ordem:

1. Fase 1: Desconectar navegação (evita crashes)
2. Fase 2: Remover UI (remove componentes visuais)
3. Fase 3: Remover Domain (remove lógica de negócio)
4. Fase 4: Remover Data (remove acesso a dados)
5. Fase 5: Remover DI (remove injeção de dependências)
6. Fase 6: Remover Layouts (remove recursos visuais)
7. Fase 7: Remover Testes (remove testes obsoletos)
8. Fase 8: Limpar Configurações (remove preferências)
9. Fase 9: Atualizar Manifest (remove registro de Activity)
10. Fase 10: Remover Dependências (remove bibliotecas externas)
11. Fase 11: Validação Final (testes completos)

### Compilação Após Cada Fase

Após cada fase principal (1-10), é OBRIGATÓRIO:
1. Executar `./gradlew clean build`
2. Verificar que não há erros de compilação
3. Buscar referências aos componentes removidos
4. Confirmar que remoção foi bem-sucedida

### Backup

Antes de iniciar a Fase 1, é ALTAMENTE RECOMENDADO:
- Criar um commit Git com o estado atual
- Criar uma branch para a remoção: `git checkout -b remove-ai-feature`
- Documentar o commit inicial para possível rollback

### Testes de Preservação

Os testes de preservação (Fase 11.4-11.8) são CRÍTICOS para garantir que:
- Funcionalidades existentes não foram afetadas
- App continua funcionando normalmente
- Não há regressões introduzidas pela remoção

### Métricas de Sucesso

Ao final da Fase 11, TODAS as seguintes métricas devem ser atendidas:
- ✅ 0 referências a "AIIdentification" no código
- ✅ 0 arquivos relacionados a IA presentes
- ✅ 0 dependências ML Kit no build.gradle
- ✅ Compilação limpa sem erros
- ✅ Todos os testes de preservação passando
- ✅ APK menor (redução esperada: 5-10 MB)
- ✅ App funciona normalmente sem crashes

Se qualquer métrica não for atendida, revisar as fases anteriores e corrigir antes de prosseguir.
