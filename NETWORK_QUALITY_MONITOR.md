# 📡 Monitor de Qualidade de Rede - Sincronização Inteligente

## 🎯 Problema Resolvido

**Antes:** App tentava sincronizar mesmo com rede instável, causando:
- ❌ Timeouts longos (30s+)
- ❌ App travando durante coleta
- ❌ Bateria desperdiçada
- ❌ Experiência ruim para usuário

**Depois:** App detecta qualidade da rede e decide automaticamente:
- ✅ Rede boa → Sincroniza imediatamente
- ✅ Rede instável → Salva local, sincroniza depois
- ✅ Sem rede → Modo offline completo
- ✅ Experiência fluida sempre

---

## 🏗️ Arquitetura

### NetworkQualityMonitor

Monitor contínuo que avalia qualidade da rede em tempo real baseado em:

1. **Tipo de Conexão** (40 pontos)
   - WiFi: 40 pontos
   - Ethernet: 40 pontos
   - Dados móveis: 25-35 pontos
   - Outros: 10 pontos

2. **Largura de Banda** (40 pontos)
   - ≥10 Mbps: 40 pontos (Excelente)
   - ≥5 Mbps: 30 pontos (Boa)
   - ≥2 Mbps: 20 pontos (Regular)
   - <2 Mbps: 10 pontos (Ruim)

3. **Histórico de Falhas** (20 pontos)
   - 0 falhas: 20 pontos
   - 1-2 falhas (< 1 min): 15 pontos
   - 3-5 falhas (< 5 min): 10 pontos
   - 5+ falhas: 0 pontos

**Score Total:** 0-100 pontos

---

## 📊 Níveis de Qualidade

### 🟢 EXCELENTE (80-100 pontos)
- **Características:** WiFi rápido, sem falhas recentes
- **Comportamento:** Sincroniza imediatamente
- **Timeout:** 10 segundos
- **Exemplo:** WiFi 50 Mbps, 0 falhas

### 🟢 BOA (60-79 pontos)
- **Características:** WiFi ou 4G/5G estável
- **Comportamento:** Sincroniza imediatamente
- **Timeout:** 15 segundos
- **Exemplo:** 4G com 8 Mbps, 1 falha recente

### 🟡 REGULAR (40-59 pontos)
- **Características:** 3G/4G com algumas falhas
- **Comportamento:** Salva local, tenta sync com timeout curto
- **Timeout:** 8 segundos
- **Exemplo:** 3G com 3 Mbps, 3 falhas

### 🟠 RUIM (20-39 pontos)
- **Características:** Conexão lenta ou muito instável
- **Comportamento:** Salva local, não tenta sync
- **Timeout:** 5 segundos (apenas para sync manual)
- **Exemplo:** 2G com 1 Mbps, 5+ falhas

### 🔴 MUITO_RUIM (1-19 pontos)
- **Características:** Conexão extremamente instável
- **Comportamento:** Modo offline completo
- **Timeout:** 3 segundos (apenas para sync manual)
- **Exemplo:** Sinal fraco, 10+ falhas

### ⚫ SEM_REDE (0 pontos)
- **Características:** Sem conexão
- **Comportamento:** Modo offline completo
- **Timeout:** 0 (não tenta)
- **Exemplo:** Modo avião, sem sinal

---

## 🔄 Fluxo de Decisão

```
USUÁRIO COLETA PATRIMÔNIO
    ↓
SALVAR NO ROOM DATABASE (SEMPRE)
    ↓
VERIFICAR QUALIDADE DA REDE
    ↓
    ├─ EXCELENTE/BOA (80-60)
    │   ↓
    │   TENTAR SINCRONIZAR IMEDIATAMENTE
    │   ├─ SUCESSO → Marca como sincronizado
    │   └─ FALHA → Registra falha, ajusta score
    │
    ├─ REGULAR (40-59)
    │   ↓
    │   SALVAR LOCAL (não tenta sync)
    │   ↓
    │   SYNC EM BACKGROUND (com timeout curto)
    │
    └─ RUIM/MUITO_RUIM/SEM_REDE (<40)
        ↓
        SALVAR LOCAL (não tenta sync)
        ↓
        AGUARDAR MELHORA DA REDE
```

---

## 💻 Implementação

### 1. NetworkQualityMonitor.kt

```kotlin
@Singleton
class NetworkQualityMonitor @Inject constructor(
    private val context: Context
) {
    val networkQuality: StateFlow<NetworkQuality>
    val shouldSyncImmediately: StateFlow<Boolean>
    
    fun shouldAttemptSync(): Boolean
    fun getRecommendedTimeout(): Long
    fun registerSyncSuccess()
    fun registerSyncFailure()
}
```

### 2. ColetaRepositoryImpl.kt

```kotlin
override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
    // 1. Salvar localmente SEMPRE
    val id = coletaDao.inserir(entity)
    
    // 2. Verificar qualidade da rede
    val shouldAttemptSync = networkQualityMonitor.shouldAttemptSync()
    
    if (shouldAttemptSync) {
        // Rede boa: tentar sincronizar
        try {
            val response = coletaApi.registrarColeta(request)
            if (response.success) {
                coletaDao.marcarSincronizada(id)
                networkQualityMonitor.registerSyncSuccess()
            }
        } catch (e: Exception) {
            networkQualityMonitor.registerSyncFailure()
        }
    } else {
        // Rede ruim: apenas salvar local
        Log.d(TAG, "Rede instável - Salvando apenas localmente")
    }
    
    return Result.success(coleta)
}
```

### 3. NetworkQualityIndicator.kt (UI)

```kotlin
class NetworkQualityIndicator : LinearLayout {
    fun setQuality(quality: NetworkQuality, pendingCount: Int)
}
```

---

## 📱 Experiência do Usuário

### Indicador Visual

```
🟢 EXCELENTE
"Excelente - Sincronizando automaticamente"
"Todas as coletas sincronizadas"

🟢 BOA
"Boa - Sincronizando automaticamente"
"2 coletas aguardando sincronização"

🟡 REGULAR
"Regular - Salvando localmente"
"5 coletas pendentes"

🟠 RUIM
"Ruim - Modo offline ativado"
"Rede instável - 10 coletas pendentes"

🔴 MUITO_RUIM
"Muito ruim - Modo offline ativado"
"15 coletas pendentes"

⚫ SEM_REDE
"Sem conexão - Modo offline"
"20 coletas pendentes"
```

### Feedback Durante Coleta

**Rede Boa:**
```
✓ Coleta salva e sincronizada
```

**Rede Instável:**
```
✓ Coleta salva localmente
⚠ Rede instável - Será sincronizada automaticamente
```

**Sem Rede:**
```
✓ Coleta salva localmente
ℹ Modo offline - 15 coletas aguardando sincronização
```

---

## 🎯 Benefícios

### Performance
- ⚡ **Sem timeouts longos:** Não tenta sync em rede ruim
- ⚡ **App responsivo:** Nunca trava esperando rede
- ⚡ **Bateria economizada:** Não desperdiça tentando sync impossível

### Confiabilidade
- 🔒 **Zero perda de dados:** Sempre salva local primeiro
- 🔒 **Decisão inteligente:** Baseada em dados reais da rede
- 🔒 **Auto-ajuste:** Aprende com falhas e sucessos

### Usabilidade
- 😊 **Experiência fluida:** Usuário não percebe problemas de rede
- 😊 **Feedback claro:** Sabe exatamente o que está acontecendo
- 😊 **Sem surpresas:** Comportamento previsível

---

## 🧪 Testes

### Teste 1: Rede Excelente
```
1. Conectar WiFi rápido (50+ Mbps)
2. Coletar patrimônio
3. Verificar: Sincronizado imediatamente
4. Verificar logs: "Qualidade: EXCELENTE"
```

### Teste 2: Rede Instável
```
1. Simular rede lenta (2-3 Mbps)
2. Causar 3 falhas de sync
3. Coletar patrimônio
4. Verificar: Salvo local, não tentou sync
5. Verificar logs: "Qualidade: REGULAR"
```

### Teste 3: Sem Rede
```
1. Modo avião
2. Coletar patrimônio
3. Verificar: Salvo local
4. Verificar logs: "Qualidade: SEM_REDE"
5. Ligar rede
6. Verificar: Sincroniza automaticamente
```

### Teste 4: Recuperação de Rede
```
1. Iniciar com rede ruim (5 falhas)
2. Verificar: Qualidade RUIM
3. Melhorar rede (WiFi)
4. Aguardar 5s
5. Verificar: Qualidade melhora para BOA
6. Próxima coleta sincroniza imediatamente
```

---

## 📊 Métricas Monitoradas

### Em Tempo Real
- Qualidade atual da rede
- Tipo de conexão (WiFi/Celular)
- Largura de banda
- Falhas consecutivas
- Tempo desde última falha

### Histórico
- Taxa de sucesso de sync por qualidade
- Tempo médio de sync por qualidade
- Distribuição de coletas por qualidade
- Economia de bateria (tentativas evitadas)

---

## 🔧 Configuração

### Ajustar Thresholds

```kotlin
// NetworkQualityMonitor.kt
companion object {
    // Ajustar conforme necessário
    private const val EXCELLENT_BANDWIDTH_MBPS = 10
    private const val GOOD_BANDWIDTH_MBPS = 5
    private const val FAIR_BANDWIDTH_MBPS = 2
}
```

### Ajustar Timeouts

```kotlin
fun getRecommendedTimeout(): Long {
    return when (_networkQuality.value) {
        NetworkQuality.EXCELENTE -> 10_000L   // Ajustar
        NetworkQuality.BOA -> 15_000L
        NetworkQuality.REGULAR -> 8_000L
        NetworkQuality.RUIM -> 5_000L
        // ...
    }
}
```

---

## 🚀 Próximas Melhorias

### Curto Prazo
- [ ] Histórico de qualidade (últimos 10 min)
- [ ] Predição de qualidade futura
- [ ] Notificação quando rede melhorar

### Médio Prazo
- [ ] Machine Learning para predição
- [ ] Ajuste automático de thresholds
- [ ] Análise de padrões de uso

### Longo Prazo
- [ ] Sincronização P2P em rede local
- [ ] Compressão adaptativa baseada em qualidade
- [ ] Priorização de coletas críticas

---

## 📝 Logs Importantes

```bash
# Ver qualidade da rede
adb logcat -s NetworkQualityMonitor:D

# Ver decisões de sync
adb logcat -s ColetaRepositoryImpl:D | grep "Qualidade"

# Ver sucessos/falhas
adb logcat -s NetworkQualityMonitor:D | grep "Sync"
```

**Exemplo de Log:**
```
NetworkQualityMonitor: Qualidade da rede: BOA (score: 75)
ColetaRepositoryImpl: Qualidade da rede: BOA - Tentar sync: true
ColetaRepositoryImpl: Enviando coleta para servidor (rede: BOA)
ColetaRepositoryImpl: ✓ Coleta sincronizada com sucesso
NetworkQualityMonitor: ✓ Sync bem-sucedido - Resetando contador de falhas
```

---

## ✅ Checklist de Implementação

- [x] NetworkQualityMonitor criado
- [x] Integração com ColetaRepositoryImpl
- [x] NetworkQualityIndicator (UI)
- [x] Documentação completa
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Validação em campo

---

**Versão:** 2.1.0  
**Data:** 18/11/2025  
**Status:** ✅ IMPLEMENTADO  
**Impacto:** 🚀 ALTO - Melhora significativa na experiência
