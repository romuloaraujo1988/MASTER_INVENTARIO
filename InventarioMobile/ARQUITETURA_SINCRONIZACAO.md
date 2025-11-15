# Arquitetura de Sincronização SQLite ↔ PostgreSQL

## 🏗️ Visão Geral

```
┌─────────────────────────────────────────────────────────────┐
│                    ANDROID APP (Kotlin)                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │   UI Layer      │         │   UI Layer      │           │
│  │  (Activities)   │◄────────┤  (ViewModels)   │           │
│  └─────────────────┘         └────────┬────────┘           │
│                                        │                     │
│                              ┌─────────▼────────┐           │
│                              │   Use Cases      │           │
│                              │  (Domain Layer)  │           │
│                              └─────────┬────────┘           │
│                                        │                     │
│                    ┌───────────────────┴───────────────┐   │
│                    │                                     │   │
│          ┌─────────▼────────┐              ┌───────────▼──┐│
│          │  Repository      │              │  Repository  ││
│          │  (Local)         │              │  (Remote)    ││
│          └─────────┬────────┘              └───────┬──────┘│
│                    │                               │        │
│          ┌─────────▼────────┐              ┌──────▼───────┐│
│          │  SQLite (Room)   │              │  Retrofit    ││
│          │  Offline Storage │              │  API Client  ││
│          └──────────────────┘              └──────┬───────┘│
│                                                    │        │
└────────────────────────────────────────────────────┼────────┘
                                                     │
                                                     │ HTTPS
                                                     │
┌────────────────────────────────────────────────────▼────────┐
│                    SERVIDOR (Java/Spring)                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │  REST API       │         │   Service       │           │
│  │  (Controllers)  │◄────────┤   Layer         │           │
│  └─────────────────┘         └────────┬────────┘           │
│                                        │                     │
│                              ┌─────────▼────────┐           │
│                              │   DAO Layer      │           │
│                              └─────────┬────────┘           │
│                                        │                     │
│                              ┌─────────▼────────┐           │
│                              │   PostgreSQL     │           │
│                              │   Database       │           │
│                              └──────────────────┘           │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Sincronização

### 1. Download (Servidor → App)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Servidor │────▶│   API    │────▶│  Mapper  │────▶│  SQLite  │
│PostgreSQL│     │ Response │     │  Entity  │     │  Local   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                                     │
     │  1. Buscar coletas                                 │
     │  2. Converter para DTO                             │
     │  3. Enviar via REST                                │
     │  4. Converter para Entity                          │
     │  5. Salvar no Room                                 │
     └─────────────────────────────────────────────────────┘
```

### 2. Upload (App → Servidor)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  SQLite  │────▶│  Mapper  │────▶│   API    │────▶│ Servidor │
│  Local   │     │   DTO    │     │ Request  │     │PostgreSQL│
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                                     │
     │  1. Buscar pendentes (sincronizado=false)          │
     │  2. Converter para DTO                             │
     │  3. Enviar via REST                                │
     │  4. Marcar como sincronizado                       │
     │  5. Atualizar servidorId                           │
     └─────────────────────────────────────────────────────┘
```

---

## 📦 Estrutura de Dados

### ColetaEntity (SQLite)
```kotlin
@Entity(tableName = "coleta")
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Dados do patrimônio
    val idPatrimonio: Int,
    val numeroPatrimonio: String,        // ✅ Preenchido na sync
    
    // Dados do inventário
    val idInventario: Int,
    
    // Dados da sala
    val idSala: Int?,
    val nomeSala: String?,               // ✅ Preenchido na sync
    
    // Dados do responsável
    val idResponsavel: Int?,
    val nomeResponsavel: String?,
    
    // Dados da coleta
    val observacao: String?,
    val estadoPatrimonio: String?,
    val latitude: Double?,
    val longitude: Double?,
    val dataColeta: Long,
    
    // Dados do usuário
    val idUsuario: Int,
    val nomeUsuario: String,             // ✅ Preenchido na sync
    
    // Controle de sincronização
    val sincronizado: Boolean = false,   // ✅ false = pendente
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    val servidorId: Long? = null         // ✅ ID no PostgreSQL
)
```

### MobileColetaResponseDto (API)
```kotlin
data class MobileColetaResponseDto(
    val id: Long?,
    val numeroPatrimonio: String?,
    val descricaoPatrimonio: String?,
    val idInventario: Int?,
    val nomeInventario: String?,
    val idSala: Int?,
    val nomeSala: String?,
    val localizacaoEncontrada: String?,
    val estadoEncontrado: String?,
    val observacoes: String?,
    val dataColeta: String?,
    val statusColeta: String?,
    val nomeColetor: String?,
    val usuarioId: Int,
    val patrimonioId: Int,
    val sincronizado: Boolean = true
)
```

---

## 🔀 Estratégias de Sincronização

### Estratégia 1: Offline-First (Recomendada)

```kotlin
suspend fun buscarColetas(): Result<List<Coleta>> {
    return try {
        // 1. Buscar do local primeiro (rápido)
        val coletasLocais = coletaDao.buscarTodas()
        
        // 2. Tentar atualizar do servidor em background
        if (networkChecker.isOnline()) {
            try {
                val coletasServidor = apiService.buscarColetas()
                sincronizarComLocal(coletasServidor)
            } catch (e: Exception) {
                // Falha silenciosa, continua com dados locais
            }
        }
        
        // 3. Retornar dados locais
        Result.success(coletasLocais)
        
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Vantagens**:
- ✅ App sempre funciona (mesmo offline)
- ✅ Resposta instantânea
- ✅ Sincronização transparente

**Desvantagens**:
- ⚠️ Dados podem estar desatualizados
- ⚠️ Requer sincronização periódica

---

### Estratégia 2: Server-First com Fallback

```kotlin
suspend fun buscarColetas(): Result<List<Coleta>> {
    return if (networkChecker.isOnline()) {
        try {
            // 1. Tentar buscar do servidor
            val coletasServidor = apiService.buscarColetas()
            
            // 2. Salvar no local
            salvarNoLocal(coletasServidor)
            
            // 3. Retornar dados do servidor
            Result.success(coletasServidor)
            
        } catch (e: Exception) {
            // 4. Fallback para local
            val coletasLocais = coletaDao.buscarTodas()
            Result.success(coletasLocais)
        }
    } else {
        // 5. Offline: buscar do local
        val coletasLocais = coletaDao.buscarTodas()
        Result.success(coletasLocais)
    }
}
```

**Vantagens**:
- ✅ Dados sempre atualizados quando online
- ✅ Fallback automático quando offline

**Desvantagens**:
- ⚠️ Mais lento (espera resposta do servidor)
- ⚠️ Requer conexão para dados atualizados

---

## 🔄 Ciclo de Vida da Coleta

```
┌─────────────────────────────────────────────────────────────┐
│                    CICLO DE VIDA DA COLETA                   │
└─────────────────────────────────────────────────────────────┘

1. CRIAÇÃO (Offline)
   ┌──────────────────┐
   │ Usuário coleta   │
   │ patrimônio       │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Salvar no SQLite │
   │ sincronizado=false│
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Mostrar na lista │
   │ (badge pendente) │
   └──────────────────┘

2. SINCRONIZAÇÃO (Online)
   ┌──────────────────┐
   │ Detectar conexão │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Enviar para API  │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Servidor salva   │
   │ Retorna ID       │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Atualizar SQLite │
   │ sincronizado=true│
   │ servidorId=123   │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Atualizar UI     │
   │ (badge removido) │
   └──────────────────┘

3. ATUALIZAÇÃO (Sync periódico)
   ┌──────────────────┐
   │ WorkManager      │
   │ (a cada 15 min)  │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Baixar do servidor│
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Comparar com local│
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Atualizar SQLite │
   └────────┬─────────┘
            │
            ▼
   ┌──────────────────┐
   │ Notificar UI     │
   └──────────────────┘
```

---

## 🛡️ Tratamento de Conflitos

### Cenário 1: Mesma Coleta Modificada

```
Local:    Coleta ID=1, observacao="Teste A", sincronizado=false
Servidor: Coleta ID=1, observacao="Teste B", timestamp=mais recente

Resolução: SERVIDOR GANHA
- Sobrescrever local com dados do servidor
- Perder modificações locais não sincronizadas
- Notificar usuário (opcional)
```

### Cenário 2: Coleta Deletada no Servidor

```
Local:    Coleta ID=1 existe
Servidor: Coleta ID=1 não existe

Resolução: DELETAR LOCAL
- Remover do SQLite
- Atualizar UI
```

### Cenário 3: Coleta Nova no Servidor

```
Local:    Coleta ID=2 não existe
Servidor: Coleta ID=2 existe

Resolução: ADICIONAR LOCAL
- Inserir no SQLite
- Atualizar UI
```

---

## 📊 Indicadores de Status

### Badge de Sincronização
```kotlin
// Na lista de coletas
when {
    coleta.sincronizado -> {
        // ✅ Ícone verde
        icon = R.drawable.ic_sync_success
        text = "Sincronizado"
    }
    coleta.tentativasSincronizacao > 0 -> {
        // ⚠️ Ícone amarelo
        icon = R.drawable.ic_sync_warning
        text = "Tentando sincronizar..."
    }
    else -> {
        // 🔄 Ícone cinza
        icon = R.drawable.ic_sync_pending
        text = "Pendente"
    }
}
```

### Barra de Status Global
```kotlin
// No topo do app
when (syncState) {
    is SyncState.Syncing -> {
        showBanner("Sincronizando...", color = BLUE)
    }
    is SyncState.Offline -> {
        showBanner("Modo offline", color = ORANGE)
    }
    is SyncState.Error -> {
        showBanner("Erro na sincronização", color = RED)
    }
}
```

---

## 🔧 Configurações de Sincronização

### Preferências do Usuário
```kotlin
data class SyncPreferences(
    val syncAutomatico: Boolean = true,
    val syncApenasWifi: Boolean = false,
    val intervaloSync: Int = 15, // minutos
    val notificarSync: Boolean = true,
    val manterHistorico: Int = 30 // dias
)
```

### Tela de Configurações
```
┌─────────────────────────────────────┐
│ Sincronização                       │
├─────────────────────────────────────┤
│ ☑ Sincronização automática          │
│ ☐ Apenas via Wi-Fi                  │
│                                      │
│ Intervalo: [15 minutos ▼]           │
│                                      │
│ ☑ Notificar ao sincronizar          │
│                                      │
│ Manter histórico: [30 dias ▼]       │
│                                      │
│ [Sincronizar Agora]                 │
│                                      │
│ Última sincronização:                │
│ 14/11/2025 17:45                    │
│                                      │
│ Coletas pendentes: 3                │
└─────────────────────────────────────┘
```

---

## 📝 Logs e Debug

### Logs de Sincronização
```kotlin
Log.d("Sync", "═══════════════════════════════")
Log.d("Sync", "INICIANDO SINCRONIZAÇÃO")
Log.d("Sync", "Coletas locais: ${coletasLocais.size}")
Log.d("Sync", "Coletas pendentes: ${coletasPendentes.size}")
Log.d("Sync", "═══════════════════════════════")

// Durante sync
Log.d("Sync", "Enviando coleta ${coleta.id}...")
Log.d("Sync", "✓ Coleta ${coleta.id} sincronizada")
Log.e("Sync", "✗ Erro ao sincronizar coleta ${coleta.id}: ${e.message}")

// Fim
Log.d("Sync", "═══════════════════════════════")
Log.d("Sync", "SINCRONIZAÇÃO CONCLUÍDA")
Log.d("Sync", "Sucesso: $sucesso")
Log.d("Sync", "Falhas: $falhas")
Log.d("Sync", "═══════════════════════════════")
```

---

## 🎯 Métricas de Sucesso

### KPIs
- ✅ Taxa de sincronização: > 95%
- ✅ Tempo médio de sync: < 5 segundos
- ✅ Coletas perdidas: 0
- ✅ Conflitos resolvidos: 100%

### Monitoramento
```kotlin
data class SyncMetrics(
    val totalSyncs: Int,
    val successfulSyncs: Int,
    val failedSyncs: Int,
    val averageSyncTime: Long,
    val coletasPendentes: Int,
    val ultimaSync: Long
)
```

---

**Versão**: 1.0.0  
**Data**: 14/11/2025  
**Status**: 📐 Arquitetura Definida
