# Tarefas de Implementação — android-offline-sync-fixes

## Visão Geral

Correção de 5 bugs no fluxo de sincronização offline do app Android. Ordenadas do menor risco ao maior — cada tarefa é independente e pode ser verificada isoladamente.

---

- [x] 1. Bug 5 — Adicionar reset automático de erros recuperáveis em `sincronizarColetasPendentes()`
  - Adicionado bloco de reset no início de `sincronizarColetasPendentes()`, antes de `buscarPendentes()`
  - Coletas com 5+ tentativas e erros contendo "timeout", "conexão", "connection", "network", "rede" têm o erro limpo via `limparErroSincronizacao()` e são incluídas no ciclo atual
  - Erros permanentes ("Patrimônio não encontrado", "idInventario inválido") não são afetados
  - _Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/ColetaRepositoryImpl.kt`_

- [x] 2. Bug 4 — Remover fallback de inventário perigoso em `sincronizarEmLote()` e `sincronizarIndividualmente()`
  - Em `sincronizarEmLote()`: substituído `?: preferencesManager.getInventarioAtivoId() ?: 0` por rejeição explícita com `return@mapNotNull null` e registro de erro descritivo
  - Em `sincronizarIndividualmente()`: substituído pelo mesmo padrão com `continue` no loop `for`
  - Coletas com `idInventario > 0` continuam com comportamento idêntico ao anterior
  - _Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/ColetaRepositoryImpl.kt`_

- [x] 3. Bug 3 — Documentar e garantir uso correto do `idInventario` da entity em `sincronizarColetasPendentes()`
  - Adicionado comentário explicativo acima da chamada `sincronizarEmLote()` documentando que o `idInventario` vem sempre da entity, não do `PreferencesManager`
  - _Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/ColetaRepositoryImpl.kt`_

- [x] 4. Bug 1 — Corrigir tratamento de `idInventario = 0` no bloco de sincronização em background de `registrarColeta()`
  - Nos dois blocos de sync em background (principal e fallback de rede instável): substituído registro imediato de erro permanente por lógica condicional
  - Tentativas 1-2: `registrarErroSincronizacao(id, null)` — incrementa contador sem registrar erro, coleta fica pendente para retry
  - Tentativa 3+: registra erro permanente "Inventário ativo não configurado no app"
  - _Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/ColetaRepositoryImpl.kt`_

- [x] 5. Bug 2 — Implementar envio real ao servidor em `EnviarColetasPendentesUseCase`
  - Substituído `ApiService` por `ColetaApi` no construtor
  - Removido o `TODO` e o `marcarSincronizada()` falso
  - Implementado envio real via `coletaApi.registrarColeta(request)` com `MobileColetaRequest` completo
  - Coleta só é marcada como sincronizada se `response.success == true`
  - Erros registrados via `registrarErroSincronizacao()` em caso de falha
  - Coletas com `idInventario = 0` são rejeitadas com erro descritivo (sem fallback perigoso)
  - _Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/usecase/EnviarColetasPendentesUseCase.kt`_

- [x] 6. Verificar injeção de `ColetaApi` em `EnviarColetasPendentesUseCase` no módulo Hilt
  - `provideColetaApi(retrofit: Retrofit)` já existe em `ApiModule.kt` — nenhuma ação necessária
  - `EnviarColetasPendentesUseCase` usa `@Inject` diretamente — Hilt resolve automaticamente
  - _Arquivos: `InventarioMobile/app/src/main/java/com/inventario/mobile/di/ApiModule.kt`_

- [x] 7. Compilar o app Android e verificar que não há erros
  - `.\gradlew.bat assembleDebug` — **BUILD SUCCESSFUL** em 1m 7s
  - Apenas warnings pré-existentes (name shadowed, elvis redundante) — nenhum erro novo
