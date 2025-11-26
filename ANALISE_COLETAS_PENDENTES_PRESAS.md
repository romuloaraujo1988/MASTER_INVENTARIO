# Análise: Coletas Pendentes que Não Sincronizam

## 📋 Problema Reportado
Coleta ficou pendente e o sistema não reconhece nem consegue sincronizar.

## 🔍 Causas Identificadas

### 1. **Erros de Sincronização Não Eram Registrados**
**Problema**: Quando a sincronização assíncrona falhava, o erro era apenas logado mas **não salvo na coleta**.
```kotlin
// ANTES: Erro não era salvo
} catch (e: Exception) {
    android.util.Log.e("ColetaRepositoryImpl", "Erro...", e)
    // Coleta ficava "presa" sem informação do motivo!
}
```

**Correção**: Agora o erro é registrado no banco:
```kotlin
// DEPOIS: Erro é salvo na coleta
} catch (e: Exception) {
    coletaDao.registrarErroSincronizacao(id, e.message)
}
```

### 2. **Inventário Ativo Não Validado**
**Problema**: Se `inventarioId = 0`, a coleta era enviada ao servidor que rejeitava.

**Correção**: Validação antes de tentar sincronizar:
```kotlin
if (inventarioId <= 0) {
    val erro = "Inventário ativo não configurado no app"
    coletaDao.registrarErroSincronizacao(id, erro)
    return@withTimeout
}
```

### 3. **Timeout Silencioso**
**Problema**: Timeout na sincronização não registrava erro.

**Correção**: Timeout agora registra erro específico:
```kotlin
} catch (e: TimeoutCancellationException) {
    coletaDao.registrarErroSincronizacao(id, "Timeout na sincronização")
}
```

### 4. **Servidor Rejeita Coleta**
Possíveis motivos do servidor rejeitar:
- Usuário não é participante do inventário
- Patrimônio não encontrado no servidor
- Inventário não está "EM_ANDAMENTO"

## ✅ Correções Implementadas

### 1. Registro de Erros no Banco
Todos os erros de sincronização agora são salvos em `ColetaEntity.erroSincronizacao`.

### 2. Novas Queries de Diagnóstico
```kotlin
// Buscar coletas com erro
coletaDao.buscarPendentesComErro()

// Buscar coletas que nunca tentaram sincronizar
coletaDao.buscarPendentesSemErro()

// Buscar coletas com muitas tentativas
coletaDao.buscarColetasComMuitasTentativas(3)
```

### 3. Métodos para Gerenciar Coletas Pendentes
```kotlin
// Limpar erro para nova tentativa
coletaRepository.limparErroColeta(coletaId)

// Limpar todos os erros (reset geral)
coletaRepository.limparTodosErrosColetas()

// Remover coleta que não pode ser sincronizada
coletaRepository.removerColetaPendente(coletaId)
```

## 🛠️ Como Diagnosticar Coletas Presas

### Via Logs (ADB)
```bash
adb logcat -s ColetaRepositoryImpl:* | grep -E "❌|⚠|✗"
```

### Via Código (Debug)
```kotlin
// No ViewModel ou Activity
val coletasComErro = coletaRepository.getColetasPendentesComErro()
coletasComErro.forEach { coleta ->
    Log.d("DEBUG", "Coleta ${coleta.id}: ${coleta.erroSincronizacao}")
}
```

## 📊 Cenários de Coletas Presas

| Cenário | Erro Registrado | Solução |
|---------|-----------------|---------|
| Inventário não configurado | "Inventário ativo não configurado" | Configurar inventário no app |
| Usuário não participante | "Usuário não é participante" | Adicionar usuário ao inventário |
| Patrimônio não existe | "Patrimônio não encontrado" | Sincronizar dados do servidor |
| Timeout de rede | "Timeout na sincronização" | Tentar novamente com rede melhor |
| Servidor offline | "Erro de conexão" | Aguardar servidor disponível |

## 🔄 Fluxo de Recuperação

1. **Identificar coletas com erro**:
   ```kotlin
   val coletasComErro = coletaRepository.getColetasPendentesComErro()
   ```

2. **Analisar o erro**:
   - Se "Inventário não configurado" → Configurar inventário
   - Se "Usuário não participante" → Verificar cadastro
   - Se "Timeout" → Limpar erro e tentar novamente

3. **Limpar erro e retentar**:
   ```kotlin
   coletaRepository.limparErroColeta(coletaId)
   coletaRepository.sincronizarColetasPendentes()
   ```

4. **Se não resolver, remover coleta**:
   ```kotlin
   coletaRepository.removerColetaPendente(coletaId)
   ```

## 📱 Melhorias na UI (Implementadas)

### Tela de Coletas Pendentes (`PendingCollectionsActivity`)

1. ✅ **Mostrar erro na tela de Coletas Pendentes**
   - Campo vermelho com ícone de erro
   - Texto do erro visível para o usuário

2. ✅ **Botão "Tentar Novamente" por coleta**
   - Limpa o erro e tenta sincronizar novamente
   - Dialog de confirmação antes de tentar

3. ✅ **Botão "Remover" para coletas irrecuperáveis**
   - Já existia, mantido funcionando

4. ✅ **Indicador visual de coletas com erro**
   - Chip muda de "Pendente" para "Erro" (vermelho)
   - Background vermelho claro no card

### Novos Métodos no ViewModel

```kotlin
// Tentar sincronizar uma coleta específica
viewModel.retryCollection(coleta)

// Limpar erros de todas as coletas
viewModel.clearAllErrors()
```

## 📅 Data da Correção
26/11/2025

## 📁 Arquivos Modificados

### Backend (Registro de Erros)
- `ColetaRepositoryImpl.kt` - Registro de erros em todas as falhas de sync
- `ColetaDao.kt` - Novas queries de diagnóstico
- `ColetaRepository.kt` - Novos métodos na interface

### Modelos
- `Coleta.kt` (data/model) - Campo `erroSincronizacao` adicionado
- `Coleta.kt` (domain/model) - Campos `tentativasSincronizacao` e `erroSincronizacao`
- `ColetaMapper.kt` - Mapeamento dos novos campos

### UI
- `PendingCollectionsActivity.kt` - Dialog de retry
- `PendingCollectionsAdapter.kt` - Exibição de erro e botão retry
- `PendingCollectionsViewModel.kt` - Métodos `retryCollection()` e `clearAllErrors()`
- `item_pending_coleta.xml` - Layout com área de erro
- `bg_error_light.xml` - Background para área de erro

### Repositório Legacy
- `InventarioRepository.kt` - Método `getColetasPendentes()` implementado
