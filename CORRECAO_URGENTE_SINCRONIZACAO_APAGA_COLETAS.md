# 🚨 CORREÇÃO URGENTE: Sincronização Apaga Coletas do Banco

## 🔴 PROBLEMA CRÍTICO IDENTIFICADO

**Data:** 26/11/2025  
**Severidade:** 🔴 **CRÍTICA**  
**Impacto:** Coletas são apagadas do banco local após sincronização

---

## 📋 Descrição do Problema

### Sintomas:
1. ✅ Contador "Pendentes" mostra número correto
2. ❌ Ao clicar "Sincronizar Coletas" diz "Nenhuma coleta pendente"
3. ❌ Coletas desaparecem do banco local após sincronização
4. ❌ Usuário perde histórico de coletas

### Causa Raiz:
```kotlin
// ColetaRepositoryImpl.kt - LINHA ~460
private suspend fun sincronizarEmLote(...): Int {
    // ...
    if (response.success) {
        // ❌ ERRADO: APAGA as coletas do banco local!
        coletasPendentes.forEach { entity ->
            coletaDao.deletar(entity.id)  // ❌ NÃO DEVE APAGAR!
        }
    }
}
```

**O problema:** Após sincronizar com sucesso, o código **APAGA** as coletas ao invés de apenas **marcar como sincronizadas**!

---

## 🔍 Código Problemático

### Método sincronizarEmLote (Linha ~460)
```kotlin
if (response.success) {
    // ❌ ERRADO: Apaga coletas do banco local
    coletasPendentes.forEach { entity ->
        try {
            coletaDao.deletar(entity.id)  // ❌ PROBLEMA AQUI!
            android.util.Log.d("ColetaRepositoryImpl", "🗑️ Coleta ${entity.id} apagada do banco local (sincronizada)")
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao apagar coleta ${entity.id}", e)
        }
    }
    
    return sucesso
}
```

### Método sincronizarIndividualmente (Linha ~510)
```kotlin
if (response.success) {
    // ❌ ERRADO: Apaga coleta do banco local
    try {
        coletaDao.deletar(entity.id)  // ❌ PROBLEMA AQUI!
        android.util.Log.d("ColetaRepositoryImpl", "🗑️ Coleta ${entity.id} apagada do banco local (sincronizada)")
        sincronizadas++
    } catch (e: Exception) {
        android.util.Log.e("ColetaRepositoryImpl", "Erro ao apagar coleta ${entity.id}", e)
    }
}
```

---

## ✅ SOLUÇÃO CORRETA

### O Que Fazer:
**MARCAR como sincronizada** ao invés de **APAGAR**!

### Código Corrigido - sincronizarEmLote

```kotlin
/**
 * Sincroniza coletas em lote (mais eficiente)
 * v2.6: CORRIGIDO - Marca como sincronizada ao invés de apagar
 */
private suspend fun sincronizarEmLote(coletasPendentes: List<com.inventario.mobile.data.local.entity.ColetaEntity>): Int {
    // Converter entities para requests
    val requests = coletasPendentes.mapNotNull { entity ->
        try {
            val coleta = mapper.toDomain(entity)
            val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
            
            // ✅ Obter ID do inventário ativo
            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
            
            com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                idInventario = inventarioId,
                usuarioId = coleta.usuarioId.toInt(),
                idSala = patrimonio?.idSala,
                localizacaoEncontrada = coleta.localizacaoAtual,
                estadoEncontrado = coleta.status ?: "BOM",
                observacaoColeta = coleta.observacoes,
                dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                    .format(java.util.Date(coleta.dataColeta)),
                latitude = coleta.latitude,
                longitude = coleta.longitude,
                fotoPatrimonio = null,
                semEtiqueta = false,
                descricaoItemSemEtiqueta = null,
                categoriaItemSemEtiqueta = null,
                deviceId = android.os.Build.MODEL,
                appVersion = "1.2",
                divergencia = false,
                motivoDivergencia = null
            )
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "Erro ao converter coleta ${entity.id}", e)
            null
        }
    }
    
    if (requests.isEmpty()) {
        android.util.Log.w("ColetaRepositoryImpl", "⚠️ Nenhuma coleta válida para sincronizar")
        return 0
    }
    
    android.util.Log.d("ColetaRepositoryImpl", "📤 Enviando ${requests.size} coletas em lote...")
    
    // Enviar em lote
    val batchRequest = com.inventario.mobile.data.remote.dto.MobileColetaBatchRequest(requests)
    val response = coletaApi.registrarColetasEmLote(batchRequest)
    
    if (response.success) {
        // ✅ CORRETO: Marcar como sincronizada (NÃO apagar!)
        coletasPendentes.forEach { entity ->
            try {
                coletaDao.marcarSincronizada(entity.id)  // ✅ CORRETO!
                android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta ${entity.id} marcada como sincronizada")
            } catch (e: Exception) {
                android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao marcar coleta ${entity.id}", e)
            }
        }
        
        // Extrair quantidade de sucesso do response
        val resultado = response.data
        val sucesso = (resultado?.get("sucesso") as? Number)?.toInt() ?: coletasPendentes.size
        
        android.util.Log.d("ColetaRepositoryImpl", "✅ Batch sync: ${sucesso} coletas sincronizadas")
        return sucesso
    } else {
        android.util.Log.e("ColetaRepositoryImpl", "❌ Batch sync falhou: ${response.message}")
        throw Exception("Batch sync falhou: ${response.message}")
    }
}
```

### Código Corrigido - sincronizarIndividualmente

```kotlin
/**
 * Sincroniza coletas individualmente (fallback)
 * v2.6: CORRIGIDO - Marca como sincronizada ao invés de apagar
 */
private suspend fun sincronizarIndividualmente(coletasPendentes: List<com.inventario.mobile.data.local.entity.ColetaEntity>): Int {
    var sincronizadas = 0
    
    android.util.Log.d("ColetaRepositoryImpl", "📤 Sincronizando ${coletasPendentes.size} coletas individualmente (fallback)...")
    
    for (entity in coletasPendentes) {
        try {
            val coleta = mapper.toDomain(entity)
            val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
            
            // ✅ Obter ID do inventário ativo
            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
            
            // Converter para MobileColetaRequest
            val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
                idInventario = inventarioId,
                usuarioId = coleta.usuarioId.toInt(),
                idSala = patrimonio?.idSala,
                localizacaoEncontrada = coleta.localizacaoAtual,
                estadoEncontrado = coleta.status ?: "BOM",
                observacaoColeta = coleta.observacoes,
                dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                    .format(java.util.Date(coleta.dataColeta)),
                latitude = coleta.latitude,
                longitude = coleta.longitude,
                fotoPatrimonio = null,
                semEtiqueta = false,
                descricaoItemSemEtiqueta = null,
                categoriaItemSemEtiqueta = null,
                deviceId = android.os.Build.MODEL,
                appVersion = "1.2",
                divergencia = false,
                motivoDivergencia = null
            )
            
            android.util.Log.d("ColetaRepositoryImpl", "📤 Sincronizando coleta ${entity.id}...")
            val response = coletaApi.registrarColeta(request)
            
            if (response.success) {
                // ✅ CORRETO: Marcar como sincronizada (NÃO apagar!)
                try {
                    coletaDao.marcarSincronizada(entity.id)  // ✅ CORRETO!
                    android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta ${entity.id} marcada como sincronizada")
                    sincronizadas++
                } catch (e: Exception) {
                    android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao marcar coleta ${entity.id}", e)
                }
            } else {
                android.util.Log.w("ColetaRepositoryImpl", "⚠️ Coleta ${entity.id} falhou: ${response.message}")
                coletaDao.registrarErroSincronizacao(
                    entity.id,
                    response.message ?: "Erro desconhecido"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Erro ao sincronizar coleta ${entity.id}", e)
            coletaDao.registrarErroSincronizacao(
                entity.id,
                e.message ?: "Erro de conexão"
            )
        }
    }
    
    android.util.Log.d("ColetaRepositoryImpl", "✅ Sync individual: ${sincronizadas} coletas sincronizadas")
    return sincronizadas
}
```

---

## 📊 Comparação Antes/Depois

### Antes da Correção:
```
1. Usuário coleta 5 patrimônios offline
2. Banco local: 5 coletas (sincronizado=false)
3. Usuário sincroniza
4. Servidor recebe 5 coletas ✅
5. Banco local: 0 coletas ❌ (APAGADAS!)
6. Contador "Pendentes": 0
7. Contador "Coletados": 0 ❌
8. Histórico perdido ❌
```

### Depois da Correção:
```
1. Usuário coleta 5 patrimônios offline
2. Banco local: 5 coletas (sincronizado=false)
3. Usuário sincroniza
4. Servidor recebe 5 coletas ✅
5. Banco local: 5 coletas ✅ (sincronizado=true)
6. Contador "Pendentes": 0 ✅
7. Contador "Coletados": 5 ✅
8. Histórico preservado ✅
```

---

## 🔄 Impacto da Correção

### Benefícios:
- ✅ Coletas permanecem no banco local
- ✅ Histórico de coletas preservado
- ✅ Contador "Coletados" funciona corretamente
- ✅ Possibilidade de visualizar coletas sincronizadas
- ✅ Auditoria completa mantida

### Comportamento Correto:
```
Coleta no banco local:
- sincronizado = false → Pendente (aparece no contador "Pendentes")
- sincronizado = true  → Sincronizada (aparece no contador "Coletados")
```

---

## 🧪 Como Testar

### Teste 1: Sincronização Preserva Coletas
```
1. Coletar 5 patrimônios offline
2. Verificar banco: SELECT * FROM coleta WHERE sincronizado = 0
   → Deve retornar 5 coletas
3. Sincronizar
4. Verificar banco: SELECT * FROM coleta WHERE sincronizado = 1
   → Deve retornar 5 coletas ✅
5. Verificar banco: SELECT COUNT(*) FROM coleta
   → Deve retornar 5 (não 0!) ✅
```

### Teste 2: Contadores Corretos
```
1. Coletar 3 patrimônios offline
2. Tela Sincronização: Pendentes = 3, Coletados = 0
3. Sincronizar
4. Tela Sincronização: Pendentes = 0, Coletados = 3 ✅
```

### Teste 3: Histórico Preservado
```
1. Coletar 10 patrimônios
2. Sincronizar
3. Abrir "Minhas Coletas"
4. Verificar que 10 coletas aparecem ✅
```

---

## 📝 Logs Esperados

### Antes da Correção (ERRADO):
```
📤 Enviando 5 coletas em lote...
✅ Batch sync: 5 coletas sincronizadas
🗑️ Coleta 1 apagada do banco local  ❌
🗑️ Coleta 2 apagada do banco local  ❌
🗑️ Coleta 3 apagada do banco local  ❌
🗑️ Coleta 4 apagada do banco local  ❌
🗑️ Coleta 5 apagada do banco local  ❌
```

### Depois da Correção (CORRETO):
```
📤 Enviando 5 coletas em lote...
✅ Batch sync: 5 coletas sincronizadas
✅ Coleta 1 marcada como sincronizada  ✅
✅ Coleta 2 marcada como sincronizada  ✅
✅ Coleta 3 marcada como sincronizada  ✅
✅ Coleta 4 marcada como sincronizada  ✅
✅ Coleta 5 marcada como sincronizada  ✅
```

---

## ⚠️ POR QUE ESTAVA APAGANDO?

### Comentário no Código:
```kotlin
// v2.5: Apaga coletas sincronizadas do banco local para liberar espaço
```

**Justificativa (ERRADA):** Alguém pensou que apagar coletas liberaria espaço no banco.

**Problema:** 
- ❌ Perde histórico de coletas
- ❌ Impossibilita auditoria
- ❌ Contador "Coletados" fica zerado
- ❌ Usuário não vê o que já coletou

**Solução Correta:**
- ✅ Manter coletas no banco
- ✅ Marcar como sincronizadas
- ✅ Se precisar liberar espaço, criar rotina de limpeza de coletas ANTIGAS (ex: > 30 dias)

---

## 🔧 Limpeza Opcional (Futuro)

Se realmente precisar liberar espaço, criar método separado:

```kotlin
/**
 * Limpa coletas sincronizadas antigas (> 30 dias)
 * Chamado manualmente pelo usuário ou automaticamente
 */
suspend fun limparColetasAntigas(diasRetencao: Int = 30) {
    val timestampLimite = System.currentTimeMillis() - (diasRetencao * 24 * 60 * 60 * 1000L)
    val removidas = coletaDao.limparSincronizadasAntigas(timestampLimite)
    android.util.Log.d("ColetaRepositoryImpl", "🗑️ $removidas coletas antigas removidas (> $diasRetencao dias)")
}
```

---

## ✅ CHECKLIST DE CORREÇÃO

- [ ] Abrir `ColetaRepositoryImpl.kt`
- [ ] Localizar método `sincronizarEmLote()` (linha ~460)
- [ ] Substituir `coletaDao.deletar(entity.id)` por `coletaDao.marcarSincronizada(entity.id)`
- [ ] Localizar método `sincronizarIndividualmente()` (linha ~510)
- [ ] Substituir `coletaDao.deletar(entity.id)` por `coletaDao.marcarSincronizada(entity.id)`
- [ ] Atualizar logs de "apagada" para "marcada como sincronizada"
- [ ] Salvar arquivo
- [ ] Recompilar: `.\gradlew.bat assembleDebug`
- [ ] Reinstalar APK
- [ ] Testar sincronização
- [ ] Verificar que coletas permanecem no banco
- [ ] Verificar contadores corretos

---

**AÇÃO REQUERIDA:** Implementar correção IMEDIATAMENTE!

**Prioridade:** 🔴 CRÍTICA  
**Impacto:** Altíssimo (perda de dados)  
**Esforço:** Baixo (5 minutos)  
**Recomendação:** ✅ FAZER AGORA
