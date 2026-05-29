# ✅ Solução Híbrida: Coleta Excluída - Modo Online e Offline

## 📋 Problema Anterior

A correção v2.29.4 resolveu o problema online (coleta excluída no servidor podia ser recoletada), mas **quebrou o modo offline**: sem nenhuma verificação local, o app permitiria criar duplicatas reais quando offline.

## 🎯 Solução Híbrida v2.29.5

A nova solução considera o **estado de conectividade** para decidir como verificar duplicatas:

### Estratégia

| Estado | Verificação | Justificativa |
|--------|-------------|---------------|
| **🌐 ONLINE** | Servidor (já validado por `VerificarDuplicataColetaUseCase`) | Servidor é fonte de verdade |
| **📵 OFFLINE** | Banco local (Room) | Previne duplicatas reais no dispositivo |

### Fluxo Detalhado

```
┌─────────────────────────────────────────┐
│   Tentativa de Coleta                   │
└─────────────────┬───────────────────────┘
                  │
                  ▼
        ┌─────────────────────┐
        │  Online ou Offline? │
        └──┬───────────────┬──┘
           │               │
        ONLINE          OFFLINE
           │               │
           ▼               ▼
  ┌──────────────┐  ┌──────────────────┐
  │ Sem verif.   │  │ Verificar banco  │
  │ local        │  │ local (Room)     │
  │ (servidor    │  │                  │
  │  já validou) │  │                  │
  └──────┬───────┘  └────────┬─────────┘
         │                   │
         │                   ▼
         │          ┌────────────────────┐
         │          │ Coleta existe      │
         │          │ localmente?        │
         │          └────┬───────────┬───┘
         │              SIM         NÃO
         │               │           │
         │               ▼           │
         │     ┌─────────────────┐   │
         │     │ BLOQUEAR        │   │
         │     │ (modo offline)  │   │
         │     └─────────────────┘   │
         │                           │
         ▼                           ▼
    ┌─────────────────────────────────────┐
    │ REGISTRAR COLETA                    │
    └─────────────────────────────────────┘
```

## 🔧 Implementação Técnica

### 1. ColetaRepositoryImpl - Verificação Adaptativa

```kotlin
val isOnline = try {
    networkMonitor.isConnected()
} catch (e: Exception) {
    false  // Em caso de erro, assume offline (mais seguro)
}

if (isOnline) {
    // ONLINE: servidor é fonte de verdade
    Log.d(TAG, "✓ ONLINE - Verificação delegada ao servidor")
} else {
    // OFFLINE: verificar banco local
    Log.d(TAG, "📵 OFFLINE - Verificando duplicata localmente")
    
    insertMutex.withLock {
        val coletaExistente = coletaDao.buscarColetaExistente(
            coleta.patrimonioId.toInt(),
            inventarioId
        )
        
        if (coletaExistente != null) {
            return Result.failure(Exception(
                "⚠️ Patrimônio ${coleta.numeroPatrimonio} já foi coletado " +
                "em $dataFormatada por ${coletaExistente.nomeUsuario} (modo offline)"
            ))
        }
    }
}
```

### 2. SincronizarColetasDoServidorUseCase - Limpeza de Excluídas

Quando o app reconecta e sincroniza, **identifica coletas que foram excluídas no servidor** e as remove do banco local:

```kotlin
val idsServidor = coletasServidor.mapNotNull { it.id }.toSet()
val coletasParaRemover = coletasLocais.filter { local ->
    local.sincronizado &&                       // Apenas sincronizadas
    local.servidorId != null &&                 // Tem ID do servidor
    local.servidorId !in idsServidor            // Não existe mais no servidor
}

for (coletaRemover in coletasParaRemover) {
    coletaDao.deletarPorId(coletaRemover.id)
    Log.d(TAG, "🗑️ Coleta excluída no servidor removida localmente")
}
```

**Importante:** Coletas **NÃO sincronizadas** (pendentes locais) são preservadas. Apenas coletas que tinham ID do servidor e não existem mais lá são removidas.

## 🎯 Cenários Cobertos

### ✅ Cenário 1: Online - Coleta excluída no servidor
1. Usuário coleta item online
2. Item é excluído no servidor (via desktop ou outro app)
3. Usuário tenta coletar novamente
4. **App online**: `VerificarDuplicataColetaUseCase` consulta servidor → não duplicada
5. **Repository**: Não verifica localmente (online)
6. ✅ Coleta permitida

### ✅ Cenário 2: Offline - Tenta coletar item já coletado localmente
1. Usuário coleta item offline
2. Coleta fica pendente no banco local
3. Usuário tenta coletar mesmo item novamente
4. **Repository**: Verifica banco local (offline)
5. Encontra coleta pendente
6. ✅ Bloqueia com "já coletado em modo offline"

### ✅ Cenário 3: Offline - Tenta coletar item já sincronizado anteriormente
1. Usuário coletou item antes (online), está sincronizado
2. App fica offline
3. Usuário tenta coletar mesmo item novamente
4. **Repository**: Verifica banco local (offline)
5. Encontra coleta sincronizada
6. ✅ Bloqueia com "já coletado"

### ⚠️ Cenário 4: Offline - Item excluído no servidor durante offline
1. Usuário coletou item online (sincronizado)
2. App fica offline
3. **No servidor**: outro usuário exclui a coleta
4. Usuário tenta coletar mesmo item offline
5. **Repository**: Verifica banco local (offline)
6. Encontra coleta antiga (ainda no cache local)
7. ⚠️ **Bloqueia falsamente** (limitação aceita do offline)
8. **Resolução**: Quando reconectar, sincronização automática remove coleta antiga e item poderá ser recoletado

### ✅ Cenário 5: Reconexão - Limpeza automática
1. App estava offline, coleta foi excluída no servidor
2. App reconecta
3. `SincronizarColetasDoServidorUseCase` é executado
4. Identifica coletas locais que não existem mais no servidor
5. ✅ Remove do banco local automaticamente
6. Item pode ser recoletado normalmente

## 📊 Tabela Comparativa

| Versão | Online: item excluído | Offline: item já coletado | Offline: item recém excluído |
|--------|----------------------|---------------------------|------------------------------|
| **v2.29.3** | ❌ Bloqueava falsamente | ✅ Bloqueava | ❌ Bloqueava falsamente |
| **v2.29.4** | ✅ Permitia | ❌ Permitia duplicata | ✅ Permitia (errado) |
| **v2.29.5** | ✅ Permitia | ✅ Bloqueia | ⚠️ Bloqueia (limitação aceita) |

## 🔄 Comportamento de Reconexão

```
App Offline                     App Online
    │                              │
    │ Tenta coletar                │
    │ Verifica banco local         │
    │ Bloqueia falsamente          │
    │                              │
    │      [Reconecta]            │
    ├─────────────────────────────►│
    │                              │
    │                              │ Sincronização automática
    │                              │ Remove coletas excluídas
    │                              │ no servidor do banco local
    │                              │
    │      Tenta coletar           │
    │      (agora online)          │
    │      Servidor permite        │
    │      ✅ Coleta registrada    │
    │                              │
```

## 📦 Arquivos Modificados

1. **`ColetaRepositoryImpl.kt`**
   - Adicionado `NetworkMonitor` como dependência
   - Verificação adaptativa baseada em conectividade
   - Mantém mutex para coletas offline (evita race condition)

2. **`SincronizarColetasDoServidorUseCase.kt`**
   - Detecta coletas excluídas no servidor
   - Remove do banco local automaticamente
   - Adicionado campo `removidas` no `SyncResult`

3. **`ColetaDao.kt`**
   - Adicionado método `deletarPorId()` para limpeza

## 🧪 Como Testar

### Teste Online (Cenário Principal)
1. ✅ Online: coletar item
2. ✅ Online: excluir coleta (via desktop ou app)
3. ✅ Online: tentar coletar mesmo item → **DEVE PERMITIR**

### Teste Offline (Prevenção de Duplicatas)
1. ✅ Offline: coletar item A
2. ✅ Offline: tentar coletar item A novamente → **DEVE BLOQUEAR**

### Teste Reconexão (Limpeza Automática)
1. ✅ Online: coletar item B (sincroniza)
2. ✅ Desabilitar internet
3. ✅ Online (em outro dispositivo): excluir coleta de B
4. ✅ Reabilitar internet no app
5. ✅ Aguardar sincronização automática
6. ✅ Tentar coletar item B → **DEVE PERMITIR** (cache foi limpo)

## 📝 Melhorias Futuras

### Curto Prazo
- [ ] Endpoint do servidor que retorna IDs de coletas excluídas (delta sync)
- [ ] Notificação push quando coleta é excluída (real-time)

### Médio Prazo
- [ ] WebSocket para notificações em tempo real
- [ ] Soft delete no servidor (manter histórico de exclusões)

### Longo Prazo
- [ ] Resolução automática de conflitos
- [ ] Histórico de exclusões consultável

## ⚠️ Limitações Aceitas

1. **Offline + Item excluído no servidor**: bloqueio falso temporário até sincronização
2. **Tempo de propagação**: depende de quando o app sincroniza
3. **Coletas pendentes**: nunca são removidas automaticamente (apenas usuário pode excluir)

---

**Implementado em:** 22/05/2026  
**Versão:** 2.29.5  
**Build:** 82  
**Status:** ✅ PRODUÇÃO READY
