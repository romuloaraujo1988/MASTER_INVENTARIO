# Implementation Tasks - Correção ID Usuário Coleta

## 1. Exploração do Bug (Bug Condition Exploration)

- [x] 1.1 Escrever property test de exploração do bug
  - Criar `ColetaIdUsuarioBugExplorationTest.kt`
  - Implementar teste que demonstra coletas sendo salvas com `id_usuario = 1L`
  - Testar com múltiplos usuários diferentes (IDs 3, 5, 7)
  - Verificar que TODAS as coletas têm `id_usuario = 1` independente do usuário logado
  - **Expected**: Teste DEVE FALHAR no código não corrigido (confirma bug existe)
  - **Property**: `∀ coleta WHERE usuarioLogado.id ≠ 1 → coleta.idUsuario = 1 (BUG)`
  - _Validates: Bug Condition Analysis_

- [x] 1.2 Verificar causa raiz no código
  - Abrir `ColetaActivity.kt` linha 207-209
  - Confirmar presença de `val idUsuario = 1L`
  - Verificar se `PreferencesManager` está injetado (esperado: NÃO)
  - Verificar se há validação de usuário logado (esperado: NÃO)
  - Documentar achados no teste
  - _Validates: Root Cause Hypothesis_

## 2. Implementação da Correção

- [x] 2.1 Injetar PreferencesManager no ColetaActivity
  - Abrir `InventarioMobile/app/src/main/java/com/inventario/mobile/ui/coleta/ColetaActivity.kt`
  - Adicionar campo: `@Inject lateinit var preferencesManager: PreferencesManager`
  - Verificar que `@AndroidEntryPoint` já está presente na classe
  - Compilar para garantir que injeção funciona
  - _Requirements: 2.1_

- [x] 2.2 Substituir ID hardcoded por chamada ao PreferencesManager
  - Localizar método `salvarColeta()` (linha ~207-209)
  - Remover linha: `val idUsuario = 1L // TODO: Obter ID do usuário logado`
  - Adicionar código:
    ```kotlin
    val idUsuario = preferencesManager.getUserId()
    ```
  - _Requirements: 2.1, 2.2_

- [x] 2.3 Adicionar validação de usuário logado
  - Após obter `idUsuario`, adicionar validação:
    ```kotlin
    if (idUsuario == null) {
        Toast.makeText(
            this, 
            "Usuário não identificado. Faça login novamente.", 
            Toast.LENGTH_LONG
        ).show()
        return
    }
    ```
  - _Requirements: 2.3_

- [x] 2.4 Adicionar logs de rastreamento
  - Antes de chamar `viewModel.registrarColeta()`, adicionar:
    ```kotlin
    Log.d(TAG, "Registrando coleta com ID do usuário: $idUsuario")
    ```
  - _Requirements: 2.2_

- [x] 2.5 Garantir que ID correto é passado para ViewModel
  - Verificar chamada a `viewModel.registrarColeta()`
  - Confirmar que parâmetro `idUsuario` recebe o valor obtido do PreferencesManager
  - Não alterar outros parâmetros (numeroPatrimonio, localizacaoAtual, observacoes, etc)
  - _Requirements: 2.2_

## 3. Testes de Validação da Correção (Fix Checking)

- [ ] 3.1 Escrever property test de fix checking
  - Criar `ColetaIdUsuarioFixCheckingTest.kt`
  - Implementar teste que verifica coletas são salvas com ID correto do usuário logado
  - Testar com múltiplos usuários (IDs 3, 5, 7, 10)
  - Verificar que cada coleta tem o `id_usuario` correspondente ao usuário logado
  - **Property**: `∀ coleta WHERE usuarioLogado.id = X → coleta.idUsuario = X`
  - _Validates: Requirements 2.1, 2.2, Property 1_

- [ ] 3.2 Testar validação de usuário não logado
  - Criar teste que simula `getUserId()` retornando `null`
  - Verificar que Toast de erro é mostrado
  - Verificar que coleta NÃO é registrada
  - Verificar que método retorna sem chamar ViewModel
  - _Validates: Requirements 2.3_

- [ ] 3.3 Testar com usuário ID 1 (edge case)
  - Fazer login com usuário que realmente tem ID 1
  - Registrar coleta
  - Verificar que `id_usuario = 1` (correto neste caso)
  - Garantir que não há falso positivo
  - _Validates: Requirements 2.2_

## 4. Testes de Preservação (Preservation Checking)

- [ ] 4.1 Testar salvamento local (offline)
  - Desconectar rede
  - Fazer login com usuário ID 5
  - Registrar coleta de patrimônio
  - Verificar que coleta é salva no banco Room local
  - Verificar que coleta é marcada como `sincronizada = false`
  - Verificar que `id_usuario = 5`
  - _Validates: Requirements 3.1, 3.2_

- [ ] 4.2 Testar sincronização de coletas pendentes
  - Com coletas pendentes no banco local
  - Reconectar rede
  - Aguardar sincronização automática
  - Verificar que coletas são enviadas ao servidor
  - Verificar que `sincronizada = true` após sucesso
  - Verificar que IDs de usuário são preservados
  - _Validates: Requirements 3.3_

- [ ] 4.3 Testar scan de QR Code
  - Fazer login com usuário ID 7
  - Abrir tela de coleta
  - Escanear QR Code de patrimônio
  - Verificar que número do patrimônio é preenchido automaticamente
  - Registrar coleta
  - Verificar que `id_usuario = 7`
  - _Validates: Requirements 3.6_

- [ ] 4.4 Testar seleção de sala
  - Fazer login com usuário ID 3
  - Selecionar sala "Sala 101" antes da coleta
  - Registrar coleta
  - Verificar que `localizacao = "Sala 101"`
  - Verificar que `id_usuario = 3`
  - _Validates: Requirements 3.7_

- [ ] 4.5 Testar mensagens de sucesso
  - Fazer login com usuário ID 5
  - Registrar coleta com sucesso
  - Verificar que Toast "✅ Coleta registrada com sucesso!" aparece
  - Verificar que retorna para tela anterior
  - _Validates: Requirements 3.4_

- [ ] 4.6 Testar botão cancelar
  - Fazer login com usuário ID 5
  - Preencher dados da coleta
  - Clicar em "Cancelar"
  - Verificar que dados são descartados
  - Verificar que retorna para tela anterior
  - Verificar que nenhuma coleta foi salva
  - _Validates: Requirements 3.5_

## 5. Testes de Integração

- [ ] 5.1 Testar fluxo completo com múltiplos usuários
  - Login usuário "Maria" (ID 5) → registrar 2 coletas → logout
  - Login usuário "João" (ID 3) → registrar 2 coletas → logout
  - Login usuário "Ana" (ID 7) → registrar 2 coletas
  - Verificar no banco: 2 coletas com `id_usuario = 5`, 2 com `id = 3`, 2 com `id = 7`
  - _Validates: Requirements 2.1, 2.2, Property 1_

- [ ] 5.2 Testar cenário offline → online
  - Login usuário ID 5
  - Desconectar rede
  - Registrar 3 coletas offline
  - Verificar banco local: 3 coletas com `id_usuario = 5`, `sincronizada = false`
  - Reconectar rede
  - Aguardar sincronização
  - Verificar servidor: 3 coletas com `id_usuario = 5`
  - _Validates: Requirements 3.1, 3.2, 3.3_

- [ ] 5.3 Testar tentativa de coleta sem login
  - Limpar dados do app (simular primeiro uso)
  - Abrir app sem fazer login
  - Tentar acessar tela de coleta
  - Verificar que sistema redireciona para login OU mostra erro
  - _Validates: Requirements 2.3, 2.4_

## 6. Validação Final

- [ ] 6.1 Executar teste de exploração novamente
  - Executar `ColetaIdUsuarioBugExplorationTest.kt` no código CORRIGIDO
  - **Expected**: Teste DEVE PASSAR agora (bug foi corrigido)
  - Se falhar, investigar e corrigir
  - _Validates: Bug Fix Complete_

- [ ] 6.2 Verificar logs de produção
  - Fazer login com usuário ID 5
  - Registrar coleta
  - Verificar logcat: deve aparecer "Registrando coleta com ID do usuário: 5"
  - Repetir com usuário ID 3
  - Verificar logcat: deve aparecer "Registrando coleta com ID do usuário: 3"
  - _Validates: Requirements 2.2_

- [ ] 6.3 Code review
  - Revisar todas as mudanças em `ColetaActivity.kt`
  - Verificar que APENAS o método `salvarColeta()` foi alterado
  - Verificar que nenhum outro comportamento foi modificado
  - Verificar que injeção do PreferencesManager está correta
  - _Validates: Preservation Requirements_

- [ ] 6.4 Documentar correção
  - Atualizar comentários no código
  - Remover TODO antigo
  - Adicionar comentário explicando obtenção do ID do usuário
  - Atualizar CHANGELOG.md com descrição da correção
  - _Validates: Documentation_

## 7. Deploy e Monitoramento

- [ ] 7.1 Build e teste em dispositivo real
  - Compilar APK debug
  - Instalar em dispositivo físico
  - Testar fluxo completo com 2 usuários diferentes
  - Verificar banco SQLite no dispositivo
  - _Validates: Real Device Testing_

- [ ] 7.2 Preparar release notes
  - Documentar bug corrigido
  - Explicar impacto: coletas agora têm ID correto do usuário
  - Avisar que coletas antigas com ID 1 não podem ser corrigidas automaticamente
  - _Validates: Communication_

## Notas Importantes

### Sobre Coletas Históricas
⚠️ **IMPORTANTE**: Coletas já registradas com `id_usuario = 1L` estão incorretas e NÃO podem ser corrigidas automaticamente, pois não há como determinar qual usuário realmente fez cada coleta. Estas coletas permanecerão com ID 1 no histórico.

### Sobre Testes
- Testes de exploração (1.1) DEVEM FALHAR no código não corrigido
- Testes de fix checking (3.1) DEVEM PASSAR no código corrigido
- Testes de preservação (4.x) DEVEM PASSAR em ambas as versões

### Sobre PreferencesManager
- `getUserId()` retorna `Int?` (nullable)
- Retorna `null` se usuário não está logado ou ID não foi salvo
- ID é salvo durante o login via `saveUserId(userId: Int)`
