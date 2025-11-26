# 🐛 CORREÇÃO: Contador de Coletas Pendentes na Tela de Sincronização

## 🚨 PROBLEMA IDENTIFICADO

**Data:** 26/11/2025  
**Severidade:** 🔴 **ALTA**  
**Impacto:** Usuário não vê coletas pendentes de sincronização

---

## 📋 Descrição do Problema

### Sintomas:
1. ❌ Contador "Pendentes" mostra 0 mesmo com coletas não sincronizadas
2. ❌ Botão "Sincronizar Coletas" diz "Nenhuma coleta pendente" mas há coletas
3. ❌ Usuário não sabe se tem coletas para sincronizar

### Causa Raiz:
```kotlin
// SyncRepository.kt - LINHA 380
suspend fun getLocalStats(): Map<String, Int> {
    return try {
        val totalPatrimonios = patrimonioDao.contarTodos()
        val pendentes = patrimonioDao.contarNaoColetados()  // ❌ ERRADO!
        val coletados = totalPatrimonios - pendentes
        val salas = salaDao.contar()
        val responsaveis = responsavelDao.contar()
        
        mapOf(
            "patrimonios" to totalPatrimonios,
            "salas" to salas,
            "responsaveis" to responsaveis,
            "coletados" to coletados,
            "pendentes" to pendentes  // ❌ Conta patrimônios não coletados, não coletas pendentes!
        )
    } catch (e: Exception) {
        emptyMap()
    }
}
```

**O problema:** Está contando **patrimônios não coletados** ao invés de **coletas não sincronizadas**!

---

## ✅ SOLUÇÃO

### Arquivo: `SyncRepository.kt`

```kotlin
/**
 * Obtém estatísticas dos dados locais
 * v2.6: CORRIGIDO - Agora conta coletas pendentes de sincronização
 */
suspend fun getLocalStats(): Map<String, Int> {
    return try {
        val totalPatrimonios = patrimonioDao.contarTodos()
        val salas = salaDao.contar()
        val responsaveis = responsavelDao.contar()
        
        // ✅ CORRIGIDO: Contar coletas (não patrimônios)
        val totalColetas = coletaDao.contarTodas()
        val coletasSincronizadas = coletaDao.contarSincronizadas()
        val coletasPendentes = coletaDao.contarPendentes()  // ✅ CORRETO!
        
        android.util.Log.d("SyncRepository", "📊 Estatísticas locais:")
        android.util.Log.d("SyncRepository", "   📦 Patrimônios: $totalPatrimonios")
        android.util.Log.d("SyncRepository", "   🏢 Salas: $salas")
        android.util.Log.d("SyncRepository", "   👤 Responsáveis: $responsaveis")
        android.util.Log.d("SyncRepository", "   📋 Total Coletas: $totalColetas")
        android.util.Log.d("SyncRepository", "   ✅ Coletas Sincronizadas: $coletasSincronizadas")
        android.util.Log.d("SyncRepository", "   ⏳ Coletas Pendentes: $coletasPendentes")
        
        mapOf(
            "patrimonios" to totalPatrimonios,
            "salas" to salas,
            "responsaveis" to responsaveis,
            "coletados" to coletasSincronizadas,  // ✅ Coletas sincronizadas
            "pendentes" to coletasPendentes       // ✅ Coletas pendentes de sincronização
        )
    } catch (e: Exception) {
        android.util.Log.e("SyncRepository", "❌ Erro ao buscar estatísticas locais", e)
        emptyMap()
    }
}
```

---

## 🔍 VERIFICAÇÃO DO PROBLEMA

### Antes da Correção:
```
Cenário: Usuário coletou 5 patrimônios offline

Tela de Sincronização mostra:
📦 Patrimônios: 11428
🏢 Salas: 11
👤 Responsáveis: 45
✅ Coletados: 11428  ← ERRADO (deveria ser 0)
⏳ Pendentes: 0      ← ERRADO (deveria ser 5)

Ao clicar "Sincronizar Coletas":
❌ "Nenhuma coleta pendente para sincronizar"
```

### Depois da Correção:
```
Cenário: Usuário coletou 5 patrimônios offline

Tela de Sincronização mostra:
📦 Patrimônios: 11428
🏢 Salas: 11
👤 Responsáveis: 45
✅ Coletados: 0      ← CORRETO
⏳ Pendentes: 5      ← CORRETO

Ao clicar "Sincronizar Coletas":
✅ "5 coleta(s) sincronizada(s) com sucesso!"
```

---

## 🧪 COMO TESTAR

### Teste 1: Contador de Pendentes
```
1. Coletar 3 patrimônios offline
2. Abrir Menu → Sincronização
3. Verificar contador "Pendentes" = 3 ✅
4. Clicar "Sincronizar Coletas"
5. Aguardar sucesso
6. Verificar contador "Pendentes" = 0 ✅
```

### Teste 2: Logs
```bash
# Ver logs de estatísticas
adb logcat -s SyncRepository:D

# Deve mostrar:
📊 Estatísticas locais:
   📦 Patrimônios: 11428
   🏢 Salas: 11
   👤 Responsáveis: 45
   📋 Total Coletas: 5
   ✅ Coletas Sincronizadas: 0
   ⏳ Coletas Pendentes: 5
```

### Teste 3: Sincronização
```
1. Coletar 10 patrimônios offline
2. Verificar "Pendentes" = 10
3. Conectar internet
4. Clicar "Sincronizar Coletas"
5. Verificar mensagem: "10 coleta(s) sincronizada(s)"
6. Verificar "Pendentes" = 0
7. Verificar "Coletados" = 10
```

---

## 📝 CÓDIGO COMPLETO CORRIGIDO

### SyncRepository.kt (Método getLocalStats)

```kotlin
/**
 * Obtém estatísticas dos dados locais
 * 
 * v2.6: CORRIGIDO
 * - Agora conta coletas (não patrimônios)
 * - Diferencia coletas sincronizadas de pendentes
 * - Adiciona logs detalhados para diagnóstico
 * 
 * @return Map com estatísticas:
 *   - patrimonios: Total de patrimônios no banco local
 *   - salas: Total de salas no banco local
 *   - responsaveis: Total de responsáveis no banco local
 *   - coletados: Total de COLETAS sincronizadas
 *   - pendentes: Total de COLETAS pendentes de sincronização
 */
suspend fun getLocalStats(): Map<String, Int> {
    return try {
        android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
        android.util.Log.d("SyncRepository", "📊 BUSCANDO ESTATÍSTICAS LOCAIS")
        
        // Dados gerais
        val totalPatrimonios = patrimonioDao.contarTodos()
        val salas = salaDao.contar()
        val responsaveis = responsavelDao.contar()
        
        // Coletas (CORRIGIDO)
        val totalColetas = coletaDao.contarTodas()
        val coletasSincronizadas = coletaDao.contarSincronizadas()
        val coletasPendentes = coletaDao.contarPendentes()
        
        // Logs detalhados
        android.util.Log.d("SyncRepository", "📦 Patrimônios no banco: $totalPatrimonios")
        android.util.Log.d("SyncRepository", "🏢 Salas no banco: $salas")
        android.util.Log.d("SyncRepository", "👤 Responsáveis no banco: $responsaveis")
        android.util.Log.d("SyncRepository", "")
        android.util.Log.d("SyncRepository", "📋 COLETAS:")
        android.util.Log.d("SyncRepository", "   Total: $totalColetas")
        android.util.Log.d("SyncRepository", "   ✅ Sincronizadas: $coletasSincronizadas")
        android.util.Log.d("SyncRepository", "   ⏳ Pendentes: $coletasPendentes")
        
        // Validação
        if (coletasSincronizadas + coletasPendentes != totalColetas) {
            android.util.Log.w("SyncRepository", "⚠️ INCONSISTÊNCIA: Soma não bate!")
            android.util.Log.w("SyncRepository", "   $coletasSincronizadas + $coletasPendentes ≠ $totalColetas")
        }
        
        android.util.Log.d("SyncRepository", "═══════════════════════════════════════════")
        
        mapOf(
            "patrimonios" to totalPatrimonios,
            "salas" to salas,
            "responsaveis" to responsaveis,
            "coletados" to coletasSincronizadas,
            "pendentes" to coletasPendentes
        )
    } catch (e: Exception) {
        android.util.Log.e("SyncRepository", "❌ ERRO ao buscar estatísticas locais", e)
        emptyMap()
    }
}
```

---

## 🔄 IMPACTO DA CORREÇÃO

### Antes:
```
getLocalStats() retornava:
{
  "patrimonios": 11428,
  "salas": 11,
  "responsaveis": 45,
  "coletados": 11428,  ← ERRADO
  "pendentes": 0       ← ERRADO
}
```

### Depois:
```
getLocalStats() retorna:
{
  "patrimonios": 11428,
  "salas": 11,
  "responsaveis": 45,
  "coletados": 0,      ← CORRETO (coletas sincronizadas)
  "pendentes": 5       ← CORRETO (coletas pendentes)
}
```

---

## 📊 MÉTODOS DO ColetaDao USADOS

```kotlin
// ColetaDao.kt

@Query("SELECT COUNT(*) FROM coleta")
suspend fun contarTodas(): Int

@Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 1")
suspend fun contarSincronizadas(): Int

@Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
suspend fun contarPendentes(): Int
```

---

## ✅ CHECKLIST DE IMPLEMENTAÇÃO

- [ ] Abrir `SyncRepository.kt`
- [ ] Localizar método `getLocalStats()` (linha ~380)
- [ ] Substituir código pelo código corrigido
- [ ] Salvar arquivo
- [ ] Recompilar app: `.\gradlew.bat assembleDebug`
- [ ] Reinstalar APK: `adb install -r app-debug.apk`
- [ ] Testar contador de pendentes
- [ ] Verificar logs: `adb logcat -s SyncRepository:D`
- [ ] Testar sincronização
- [ ] Validar que contador zera após sync

---

## 🎯 RESULTADO ESPERADO

### Fluxo Completo:
```
1. Usuário coleta 5 patrimônios offline
   ↓
2. Abre tela de Sincronização
   ↓
3. Vê: "⏳ Pendentes: 5"
   ↓
4. Clica "Sincronizar Coletas"
   ↓
5. Vê: "5 coleta(s) sincronizada(s) com sucesso!"
   ↓
6. Contador atualiza: "⏳ Pendentes: 0"
   ↓
7. Contador atualiza: "✅ Coletados: 5"
```

---

## 🚨 ATENÇÃO

### Não Confundir:
- **Patrimônios não coletados** = Patrimônios que ainda não foram escaneados
- **Coletas pendentes** = Coletas já feitas mas não sincronizadas com servidor

### Exemplo:
```
Banco tem 11428 patrimônios
Usuário coletou 5 offline

Correto:
- Patrimônios não coletados: 11423
- Coletas pendentes: 5

Errado (antes da correção):
- Patrimônios não coletados: 11423
- Coletas pendentes: 0  ← ERRADO!
```

---

**AÇÃO REQUERIDA:** Implementar correção IMEDIATAMENTE!

**Prioridade:** 🔴 ALTA  
**Impacto:** Alto (funcionalidade crítica não funciona)  
**Esforço:** Baixo (5 minutos)  
**Recomendação:** ✅ FAZER AGORA
