# Plano de Revisão - App Android Inventário Mobile

## 📋 Objetivo

Realizar uma revisão completa do aplicativo Android para identificar e corrigir falhas, bugs, código incompleto, problemas de performance e melhorias de qualidade.

---

## 🔍 Áreas de Revisão

### 1. Código Incompleto e TODOs

**Problemas Identificados:**

#### 1.1 SmartSyncWorker - Sincronização não implementada
- **Arquivo**: `worker/SmartSyncWorker.kt`
- **Linha**: 128-130
- **Problema**: `TODO: Implementar lógica de sincronização real` - apenas simula sucesso
- **Impacto**: Alto - sincronização inteligente não funciona
- **Prioridade**: 🔴 CRÍTICA

#### 1.2 PhotoSyncWorker - Upload de fotos não implementado
- **Arquivo**: `worker/PhotoSyncWorker.kt`
- **Linha**: 109-111
- **Problema**: `TODO: Enviar para servidor via API` - código comentado
- **Impacto**: Alto - fotos não são sincronizadas
- **Prioridade**: 🔴 CRÍTICA

#### 1.3 ColetaActivity - ID do usuário hardcoded
- **Arquivo**: `ui/coleta/ColetaActivity.kt`
- **Linha**: 207-209
- **Problema**: `TODO: Obter ID do usuário logado` - usa placeholder `idUsuario = 1L`
- **Impacto**: Alto - coletas registradas com usuário errado
- **Prioridade**: 🔴 CRÍTICA

#### 1.4 AutoSyncManager - Sincronização automática não implementada
- **Arquivo**: `sync/AutoSyncManager.kt`
- **Linha**: 128-143
- **Problema**: `TODO: Implementar sincronização completa` - apenas loga
- **Impacto**: Médio - sincronização automática não funciona
- **Prioridade**: 🟡 ALTA

#### 1.5 DatabaseInitializer - Contagens não implementadas
- **Arquivo**: `utils/DatabaseInitializer.kt`
- **Linha**: 302-305
- **Problema**: `TODO: Implementar contagens reais` - retorna 0
- **Impacto**: Baixo - estatísticas incorretas
- **Prioridade**: 🟢 MÉDIA

---

### 2. Arquivos Desabilitados

**Problemas Identificados:**

#### 2.1 DispositivoRepository desabilitado
- **Arquivo**: `repository/DispositivoRepository.kt.disabled`
- **Problema**: Repositório desabilitado sem motivo documentado
- **Impacto**: Médio - funcionalidade de dispositivo não disponível
- **Prioridade**: 🟡 ALTA

#### 2.2 HeartbeatService desabilitado
- **Arquivo**: `service/HeartbeatService.kt.disabled`
- **Problema**: Service desabilitado sem motivo documentado
- **Impacto**: Baixo - monitoramento de conexão não funciona
- **Prioridade**: 🟢 MÉDIA

---

### 3. Duplicação de Código

**Problemas Identificados:**

#### 3.1 NetworkChecker duplicado
- **Arquivos**: 
  - `util/NetworkChecker.kt`
  - `utils/NetworkChecker.kt`
- **Problema**: Mesma classe em dois pacotes diferentes
- **Impacto**: Médio - confusão e manutenção duplicada
- **Prioridade**: 🟡 ALTA

#### 3.2 NetworkUtils duplicado
- **Arquivos**:
  - `util/NetworkUtils.kt`
  - `utils/NetworkUtils.kt`
- **Problema**: Mesma classe em dois pacotes diferentes
- **Impacto**: Médio - confusão e manutenção duplicada
- **Prioridade**: 🟡 ALTA

#### 3.3 SyncScheduler duplicado
- **Arquivos**:
  - `sync/SyncScheduler.kt`
  - `utils/SyncScheduler.kt`
- **Problema**: Mesma classe em dois pacotes diferentes
- **Impacto**: Médio - confusão e manutenção duplicada
- **Prioridade**: 🟡 ALTA

---

### 4. Problemas de Arquitetura

**Problemas Identificados:**

#### 4.1 Pacotes duplicados: util vs utils
- **Problema**: Dois pacotes com propósito similar (`util/` e `utils/`)
- **Impacto**: Médio - organização confusa
- **Prioridade**: 🟡 ALTA
- **Solução**: Consolidar tudo em `utils/` e remover `util/`

#### 4.2 Pasta notification vazia
- **Arquivo**: `notification/` (vazio)
- **Problema**: Pasta criada mas sem implementação
- **Impacto**: Baixo - código não utilizado
- **Prioridade**: 🟢 BAIXA
- **Solução**: Remover ou implementar notificações

#### 4.3 Múltiplos interceptors de rede
- **Arquivos**: 11 interceptors diferentes em `network/`
- **Problema**: Possível sobreposição de funcionalidades
- **Impacto**: Médio - performance e manutenção
- **Prioridade**: 🟡 ALTA
- **Ação**: Revisar e consolidar interceptors

---

### 5. Problemas de Segurança

**Problemas Identificados:**

#### 5.1 Credenciais hardcoded no build.gradle
- **Arquivo**: `app/build.gradle`
- **Linhas**: 35-38
- **Problema**: Senhas da keystore em texto plano
```groovy
storePassword "inventario2025"
keyPassword "inventario2025"
```
- **Impacto**: 🔴 CRÍTICO - vazamento de credenciais
- **Prioridade**: 🔴 CRÍTICA
- **Solução**: Mover para `gradle.properties` ou variáveis de ambiente

#### 5.2 Logs de debug com dados sensíveis
- **Arquivo**: `ui/splash/SplashActivity.kt`
- **Linha**: 138
- **Problema**: `Log.d(TAG, tokenManager.getTokenDebugInfo())` - loga tokens
- **Impacto**: Alto - exposição de tokens em logs
- **Prioridade**: 🔴 CRÍTICA
- **Solução**: Remover ou usar LogSanitizer

---

### 6. Problemas de Performance

**Problemas Identificados:**

#### 6.1 Múltiplos Workers de sincronização
- **Arquivos**: 7 workers diferentes
  - `BackupWorker.kt`
  - `ColetaSyncWorker.kt`
  - `DatabaseBackupWorker.kt`
  - `DatabaseCleanupWorker.kt`
  - `PhotoSyncWorker.kt`
  - `SmartSyncWorker.kt`
  - `SyncWorker.kt`
- **Problema**: Possível sobreposição e conflito
- **Impacto**: Médio - consumo de bateria e dados
- **Prioridade**: 🟡 ALTA
- **Ação**: Revisar e consolidar workers

#### 6.2 Cache não otimizado
- **Arquivo**: `presentation/viewmodel/QuickSearchViewModel.kt`
- **Problema**: Cache implementado mas sem métricas de hit/miss
- **Impacto**: Baixo - performance não monitorada
- **Prioridade**: 🟢 MÉDIA

---

### 7. Problemas de Usabilidade

**Problemas Identificados:**

#### 7.1 Mensagens de erro genéricas
- **Problema**: Muitas Activities usam mensagens genéricas
- **Impacto**: Médio - usuário não entende o problema
- **Prioridade**: 🟡 ALTA
- **Solução**: Implementar ErrorMapper consistente

#### 7.2 Falta de feedback visual
- **Problema**: Algumas operações não mostram loading
- **Impacto**: Médio - usuário não sabe se app travou
- **Prioridade**: 🟡 ALTA

---

### 8. Testes

**Problemas Identificados:**

#### 8.1 Cobertura de testes baixa
- **Problema**: Poucos testes unitários e de integração
- **Impacto**: Alto - bugs não detectados
- **Prioridade**: 🟡 ALTA
- **Ação**: Criar testes para componentes críticos

#### 8.2 Kotest configurado mas não utilizado
- **Problema**: Dependência adicionada mas sem testes
- **Impacto**: Baixo - recurso não aproveitado
- **Prioridade**: 🟢 BAIXA

---

### 9. Dependências

**Problemas Identificados:**

#### 9.1 Versões desatualizadas
- **Problema**: Algumas dependências podem estar desatualizadas
- **Impacto**: Médio - bugs e vulnerabilidades
- **Prioridade**: 🟡 ALTA
- **Ação**: Verificar atualizações disponíveis

#### 9.2 Dependências não utilizadas
- **Problema**: Possíveis dependências não utilizadas
- **Impacto**: Baixo - tamanho do APK
- **Prioridade**: 🟢 BAIXA

---

## 📊 Resumo de Prioridades

### 🔴 CRÍTICAS (Resolver Imediatamente)
1. SmartSyncWorker - sincronização não implementada
2. PhotoSyncWorker - upload de fotos não implementado
3. ColetaActivity - ID do usuário hardcoded
4. Credenciais hardcoded no build.gradle
5. Logs de debug com tokens

### 🟡 ALTAS (Resolver em 1-2 semanas)
1. AutoSyncManager - sincronização automática não implementada
2. Arquivos duplicados (NetworkChecker, NetworkUtils, SyncScheduler)
3. Consolidar pacotes util vs utils
4. Revisar e consolidar interceptors de rede
5. Revisar e consolidar workers de sincronização
6. DispositivoRepository desabilitado
7. Mensagens de erro genéricas
8. Cobertura de testes baixa

### 🟢 MÉDIAS (Resolver em 2-4 semanas)
1. DatabaseInitializer - contagens não implementadas
2. HeartbeatService desabilitado
3. Pasta notification vazia
4. Cache não otimizado
5. Dependências desatualizadas

### ⚪ BAIXAS (Backlog)
1. Kotest não utilizado
2. Dependências não utilizadas

---

## 🎯 Plano de Ação

### Fase 1: Correções Críticas (Semana 1)

#### Task 1.1: Implementar sincronização real no SmartSyncWorker
- Integrar com `SincronizarColetasPendentesUseCase`
- Implementar lógica de retry
- Adicionar logs detalhados
- Testar com dados reais

#### Task 1.2: Implementar upload de fotos no PhotoSyncWorker
- Criar endpoint de upload no backend (se não existir)
- Implementar chamada API no worker
- Adicionar compressão de imagens
- Testar com fotos reais

#### Task 1.3: Corrigir ID do usuário na ColetaActivity
- Obter ID do usuário do `PreferencesManager`
- Validar que usuário está logado
- Adicionar tratamento de erro se usuário não encontrado
- Testar fluxo completo de coleta

#### Task 1.4: Remover credenciais hardcoded
- Criar `keystore.properties` (adicionar ao .gitignore)
- Mover credenciais para arquivo de propriedades
- Atualizar build.gradle para ler de propriedades
- Documentar processo de configuração

#### Task 1.5: Remover logs de tokens
- Remover `tokenManager.getTokenDebugInfo()` de produção
- Usar `LogSanitizer` para logs de debug
- Revisar todos os logs de autenticação
- Adicionar lint rule para detectar logs sensíveis

---

### Fase 2: Limpeza de Código (Semana 2)

#### Task 2.1: Consolidar arquivos duplicados
- Mover tudo de `util/` para `utils/`
- Atualizar imports em todo o projeto
- Remover pasta `util/`
- Executar testes para validar

#### Task 2.2: Revisar interceptors de rede
- Documentar propósito de cada interceptor
- Identificar sobreposições
- Consolidar funcionalidades similares
- Otimizar ordem de execução

#### Task 2.3: Revisar workers de sincronização
- Documentar propósito de cada worker
- Identificar conflitos de agendamento
- Consolidar workers similares
- Otimizar frequência de execução

---

### Fase 3: Melhorias de Qualidade (Semana 3)

#### Task 3.1: Implementar sincronização automática
- Integrar AutoSyncManager com Use Cases
- Adicionar configuração de intervalo
- Implementar notificações de progresso
- Testar em diferentes cenários de rede

#### Task 3.2: Melhorar mensagens de erro
- Criar catálogo de mensagens de erro
- Implementar ErrorMapper consistente
- Adicionar sugestões de ação para cada erro
- Traduzir mensagens para português

#### Task 3.3: Adicionar testes críticos
- Testar ColetaActivity (fluxo completo)
- Testar sincronização de coletas
- Testar upload de fotos
- Testar autenticação e refresh token

---

### Fase 4: Otimizações (Semana 4)

#### Task 4.1: Otimizar cache
- Adicionar métricas de hit/miss
- Implementar estratégia de invalidação
- Configurar tamanho máximo
- Monitorar uso de memória

#### Task 4.2: Atualizar dependências
- Verificar atualizações disponíveis
- Testar compatibilidade
- Atualizar gradualmente
- Documentar breaking changes

#### Task 4.3: Remover código não utilizado
- Habilitar ou remover arquivos .disabled
- Remover pasta notification se não for usar
- Remover dependências não utilizadas
- Executar lint e corrigir warnings

---

## 🧪 Checklist de Validação

Após cada fase, validar:

- [ ] App compila sem erros
- [ ] App compila sem warnings críticos
- [ ] Testes unitários passam
- [ ] Testes de integração passam
- [ ] App funciona offline
- [ ] App funciona online
- [ ] Sincronização funciona
- [ ] Upload de fotos funciona
- [ ] Autenticação funciona
- [ ] Coleta funciona
- [ ] Não há memory leaks
- [ ] Performance está adequada
- [ ] Logs não expõem dados sensíveis
- [ ] Credenciais não estão hardcoded

---

## 📈 Métricas de Sucesso

### Antes da Revisão
- TODOs críticos: 5
- Arquivos duplicados: 6
- Credenciais hardcoded: 2
- Logs sensíveis: 1+
- Cobertura de testes: < 30%

### Após a Revisão (Meta)
- TODOs críticos: 0
- Arquivos duplicados: 0
- Credenciais hardcoded: 0
- Logs sensíveis: 0
- Cobertura de testes: > 60%

---

## 🚀 Como Executar

### Opção 1: Executar Fase por Fase
```
1. Revisar este documento
2. Criar spec de bugfix para cada fase
3. Executar tasks sequencialmente
4. Validar após cada fase
```

### Opção 2: Priorizar Críticos
```
1. Focar apenas em tasks 🔴 CRÍTICAS
2. Executar tasks 1.1 a 1.5
3. Validar e fazer release
4. Continuar com outras fases depois
```

### Opção 3: Criar Issues
```
1. Criar issue no GitHub para cada problema
2. Priorizar por impacto
3. Resolver gradualmente
4. Fechar issues conforme resolvido
```

---

## 📝 Notas Importantes

1. **Backup**: Fazer backup do código antes de iniciar
2. **Branch**: Criar branch separada para revisão
3. **Testes**: Executar testes após cada mudança
4. **Documentação**: Documentar decisões importantes
5. **Code Review**: Revisar mudanças antes de merge

---

**Criado em**: 27/03/2026  
**Versão**: 1.0.0  
**Status**: 📋 Planejamento Completo
