# 🛡️ Plano de Garantia de Persistência de Dados Críticos

## 🚨 CONTEXTO CRÍTICO

**Escala do Sistema:**
- 10.000+ patrimônios
- 100+ salas
- Múltiplos coletores simultâneos
- Ambiente offline-first

**Risco:** Perda de dados de coleta seria **CATASTRÓFICA**

---

## ✅ ANÁLISE DA IMPLEMENTAÇÃO ATUAL

### 1. Persistência Local (Room Database)

#### ✅ PONTOS FORTES

**Entity ColetaEntity - Campos Críticos Presentes:**
```kotlin
@Entity(tableName = "coleta")
data class ColetaEntity(
    // ✅ DADOS CRÍTICOS SALVOS:
    val idPatrimonio: Int,              // ✅ Qual patrimônio
    val numeroPatrimonio: String,       // ✅ Número identificador
    val idInventario: Int,              // ✅ Qual inventário
    val idSala: Int?,                   // ✅ Onde foi encontrado
    val nomeSala: String?,              // ✅ Nome da sala
    val idResponsavel: Int?,            // ✅ Responsável
    val nomeResponsavel: String?,       // ✅ Nome do responsável
    val estadoPatrimonio: String?,      // ✅ Estado de conservação
    val observacao: String?,            // ✅ Observações
    val dataColeta: Long,               // ✅ QUANDO foi coletado
    val idUsuario: Int,                 // ✅ QUEM coletou
    val nomeUsuario: String,            // ✅ Nome do coletor
    val latitude: Double?,              // ✅ Localização GPS
    val longitude: Double?,             // ✅ Localização GPS
    
    // ✅ CONTROLE DE SINCRONIZAÇÃO:
    val sincronizado: Boolean = false,
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    val servidorId: Long? = null
)
```

**Índices para Performance:**
```kotlin
indices = [
    Index(value = ["idPatrimonio"]),    // ✅ Busca rápida
    Index(value = ["idInventario"]),    // ✅ Filtro por inventário
    Index(value = ["sincronizado"]),    // ✅ Pendentes
    Index(value = ["dataColeta"])       // ✅ Ordenação temporal
]
```

**DAO com Operações Seguras:**
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)  // ✅ Não perde dados
suspend fun inserir(coleta: ColetaEntity): Long

@Query("SELECT * FROM coleta WHERE sincronizado = 0")
suspend fun buscarPendentes(): List<ColetaEntity>  // ✅ Recupera não sincronizadas
```

### 2. Estratégia Offline-First

#### ✅ IMPLEMENTAÇÃO CORRETA

```kotlin
override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
    // 1. ✅ SALVA LOCALMENTE PRIMEIRO (CRÍTICO!)
    val id = coletaDao.inserir(entity)
    
    // 2. ✅ Marca patrimônio como coletado
    patrimonioDao.marcarComoColetado(coleta.patrimonioId.toLong())
    
    // 3. ✅ Tenta sincronizar (mas não bloqueia se falhar)
    if (shouldAttemptSync) {
        try {
            coletaApi.registrarColeta(request)
            coletaDao.marcarSincronizada(id)
        } catch (e: Exception) {
            // ✅ FALHA NA SYNC NÃO PERDE DADOS!
            // Dados já estão salvos localmente
        }
    }
    
    // 4. ✅ Retorna sucesso (dados salvos)
    Result.success(coleta.copy(id = id))
}
```

**✅ GARANTIA:** Mesmo que servidor esteja offline, dados são salvos!

### 3. Sincronização em Background

#### ✅ WorkManager Configurado

```kotlin
// Sync periódico a cada 30 minutos
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    30, TimeUnit.MINUTES
)
.setConstraints(
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)  // ✅ Só com internet
        .setRequiresBatteryNotLow(true)                 // ✅ Preserva bateria
        .build()
)
.setBackoffCriteria(
    BackoffPolicy.EXPONENTIAL,                          // ✅ Retry inteligente
    15, TimeUnit.MINUTES
)
.build()
```

**✅ GARANTIA:** Coletas pendentes são sincronizadas automaticamente!

---

## ⚠️ PONTOS DE ATENÇÃO IDENTIFICADOS

### 1. ⚠️ Validação de Dados Antes de Salvar

**PROBLEMA POTENCIAL:**
```kotlin
// Se numeroPatrimonio vier vazio, pode causar problemas
val numeroPatrimonio = coleta.numeroPatrimonio ?: patrimonio?.numero ?: coleta.patrimonioId.toString()
```

**SOLUÇÃO RECOMENDADA:**
```kotlin
// Validar ANTES de salvar
if (numeroPatrimonio.isBlank()) {
    return Result.failure(Exception("Número do patrimônio é obrigatório"))
}

if (coleta.usuarioId <= 0) {
    return Result.failure(Exception("Usuário inválido"))
}

if (coleta.patrimonioId <= 0) {
    return Result.failure(Exception("Patrimônio inválido"))
}
```

### 2. ⚠️ Transações Atômicas

**PROBLEMA POTENCIAL:**
```kotlin
// Se falhar entre inserir e marcar como coletado?
coletaDao.inserir(entity)
patrimonioDao.marcarComoColetado(id)  // ← Pode falhar
```

**SOLUÇÃO RECOMENDADA:**
```kotlin
@Transaction
suspend fun registrarColetaComTransacao(coleta: ColetaEntity, patrimonioId: Long) {
    // Tudo ou nada
    coletaDao.inserir(coleta)
    patrimonioDao.marcarComoColetado(patrimonioId)
}
```

### 3. ⚠️ Backup Automático de Dados Não Sincronizados

**PROBLEMA POTENCIAL:**
- App desinstalado antes de sincronizar
- Dispositivo danificado
- Banco corrompido

**SOLUÇÃO RECOMENDADA:**
```kotlin
// Exportar coletas pendentes para arquivo
suspend fun exportarColetasPendentes(): File {
    val coletas = coletaDao.buscarPendentes()
    val json = Json.encodeToString(coletas)
    
    val file = File(context.getExternalFilesDir(null), "backup_coletas_${System.currentTimeMillis()}.json")
    file.writeText(json)
    
    return file
}
```

### 4. ⚠️ Detecção de Duplicatas

**PROBLEMA POTENCIAL:**
```kotlin
// Usuário pode coletar o mesmo patrimônio 2x por engano
// Atualmente não há validação forte
```

**SOLUÇÃO RECOMENDADA:**
```kotlin
@Query("""
    SELECT COUNT(*) FROM coleta 
    WHERE idPatrimonio = :idPatrimonio 
    AND idInventario = :idInventario
""")
suspend fun contarColetasPatrimonio(idPatrimonio: Int, idInventario: Int): Int

// Antes de inserir:
val jaColetado = coletaDao.contarColetasPatrimonio(patrimonioId, inventarioId) > 0
if (jaColetado) {
    // Avisar usuário ou bloquear
}
```

---

## 🛠️ MELHORIAS CRÍTICAS RECOMENDADAS

### Prioridade 1: CRÍTICA (Implementar AGORA)

#### 1.1 Validação Rigorosa Antes de Salvar

```kotlin
/**
 * Valida dados críticos antes de persistir
 */
private fun validarDadosCriticos(coleta: Coleta): Result<Unit> {
    return when {
        coleta.numeroPatrimonio.isNullOrBlank() -> 
            Result.failure(Exception("❌ Número do patrimônio é obrigatório"))
            
        coleta.patrimonioId <= 0 -> 
            Result.failure(Exception("❌ ID do patrimônio inválido"))
            
        coleta.usuarioId <= 0 -> 
            Result.failure(Exception("❌ Usuário não identificado"))
            
        coleta.dataColeta <= 0 -> 
            Result.failure(Exception("❌ Data de coleta inválida"))
            
        coleta.inventarioId <= 0 -> 
            Result.failure(Exception("❌ Inventário não identificado"))
            
        else -> Result.success(Unit)
    }
}

override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
    // ✅ VALIDAR PRIMEIRO
    validarDadosCriticos(coleta).getOrElse { 
        return Result.failure(it)
    }
    
    // Continuar com salvamento...
}
```

#### 1.2 Transações Atômicas

```kotlin
@Dao
interface ColetaDao {
    @Transaction
    suspend fun registrarColetaCompleta(
        coleta: ColetaEntity,
        patrimonioId: Int
    ) {
        inserir(coleta)
        // Atualizar patrimônio na mesma transação
        @Query("UPDATE patrimonio SET coletado = 1 WHERE id = :patrimonioId")
        marcarPatrimonioColetado(patrimonioId)
    }
}
```

#### 1.3 Log de Auditoria

```kotlin
@Entity(tableName = "log_coleta")
data class LogColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coletaId: Long,
    val acao: String,  // CRIADA, SINCRONIZADA, ERRO_SYNC, EXPORTADA
    val timestamp: Long,
    val detalhes: String?,
    val usuarioId: Int
)

// Registrar cada ação
suspend fun registrarLog(coletaId: Long, acao: String, detalhes: String?) {
    logDao.inserir(LogColetaEntity(
        coletaId = coletaId,
        acao = acao,
        timestamp = System.currentTimeMillis(),
        detalhes = detalhes,
        usuarioId = preferencesManager.getUserId()
    ))
}
```

### Prioridade 2: ALTA (Implementar esta semana)

#### 2.1 Backup Automático

```kotlin
class BackupManager @Inject constructor(
    private val coletaDao: ColetaDao,
    private val context: Context
) {
    /**
     * Exporta coletas pendentes para arquivo JSON
     */
    suspend fun backupColetasPendentes(): Result<File> {
        return try {
            val coletas = coletaDao.buscarPendentes()
            
            if (coletas.isEmpty()) {
                return Result.failure(Exception("Nenhuma coleta pendente"))
            }
            
            val json = Json.encodeToString(coletas)
            
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            backupDir.mkdirs()
            
            val file = File(backupDir, "coletas_${System.currentTimeMillis()}.json")
            file.writeText(json)
            
            Log.d("BackupManager", "✓ Backup criado: ${file.absolutePath}")
            Result.success(file)
            
        } catch (e: Exception) {
            Log.e("BackupManager", "✗ Erro ao criar backup", e)
            Result.failure(e)
        }
    }
    
    /**
     * Restaura coletas de arquivo de backup
     */
    suspend fun restaurarBackup(file: File): Result<Int> {
        return try {
            val json = file.readText()
            val coletas = Json.decodeFromString<List<ColetaEntity>>(json)
            
            var restauradas = 0
            coletas.forEach { coleta ->
                coletaDao.inserir(coleta)
                restauradas++
            }
            
            Log.d("BackupManager", "✓ $restauradas coletas restauradas")
            Result.success(restauradas)
            
        } catch (e: Exception) {
            Log.e("BackupManager", "✗ Erro ao restaurar backup", e)
            Result.failure(e)
        }
    }
}
```

#### 2.2 Detecção de Duplicatas

```kotlin
@Dao
interface ColetaDao {
    @Query("""
        SELECT * FROM coleta 
        WHERE idPatrimonio = :idPatrimonio 
        AND idInventario = :idInventario
        LIMIT 1
    """)
    suspend fun buscarColetaExistente(
        idPatrimonio: Int, 
        idInventario: Int
    ): ColetaEntity?
}

// No repositório:
override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
    // ✅ Verificar duplicata
    val coletaExistente = coletaDao.buscarColetaExistente(
        coleta.patrimonioId.toInt(),
        coleta.inventarioId
    )
    
    if (coletaExistente != null) {
        return Result.failure(Exception(
            "⚠️ Patrimônio já foi coletado em ${formatarData(coletaExistente.dataColeta)}"
        ))
    }
    
    // Continuar...
}
```

#### 2.3 Monitoramento de Saúde do Banco

```kotlin
class DatabaseHealthMonitor @Inject constructor(
    private val database: AppDatabase,
    private val coletaDao: ColetaDao
) {
    suspend fun verificarSaude(): DatabaseHealth {
        return try {
            val totalColetas = coletaDao.contarTodas()
            val pendentes = coletaDao.contarPendentes()
            val tamanhoDb = database.openHelper.writableDatabase.path?.let { 
                File(it).length() 
            } ?: 0L
            
            DatabaseHealth(
                totalColetas = totalColetas,
                coletasPendentes = pendentes,
                tamanhoBanco = tamanhoDb,
                saudavel = pendentes < 1000, // Alerta se > 1000 pendentes
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            DatabaseHealth(
                totalColetas = 0,
                coletasPendentes = 0,
                tamanhoBanco = 0,
                saudavel = false,
                erro = e.message,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}

data class DatabaseHealth(
    val totalColetas: Int,
    val coletasPendentes: Int,
    val tamanhoBanco: Long,
    val saudavel: Boolean,
    val erro: String? = null,
    val timestamp: Long
)
```

### Prioridade 3: MÉDIA (Implementar próxima semana)

#### 3.1 Compressão de Dados Antigos

```kotlin
/**
 * Compacta coletas sincronizadas antigas para liberar espaço
 */
suspend fun compactarColetasAntigas() {
    val umMesAtras = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
    val removidas = coletaDao.limparSincronizadasAntigas(umMesAtras)
    
    Log.d("DatabaseMaintenance", "✓ $removidas coletas antigas removidas")
}
```

#### 3.2 Exportação para CSV

```kotlin
suspend fun exportarColetasCSV(): File {
    val coletas = coletaDao.buscarTodas()
    
    val csv = buildString {
        appendLine("ID,Patrimonio,Inventario,Sala,Usuario,Data,Estado,Sincronizado")
        
        coletas.forEach { coleta ->
            appendLine("${coleta.id},${coleta.numeroPatrimonio},${coleta.idInventario}," +
                      "${coleta.nomeSala},${coleta.nomeUsuario},${coleta.dataColeta}," +
                      "${coleta.estadoPatrimonio},${coleta.sincronizado}")
        }
    }
    
    val file = File(context.getExternalFilesDir(null), "coletas_export.csv")
    file.writeText(csv)
    
    return file
}
```

---

## 📊 CHECKLIST DE GARANTIA DE DADOS

### Antes de Cada Coleta
- [ ] Validar usuário logado
- [ ] Validar inventário ativo
- [ ] Validar número do patrimônio
- [ ] Verificar duplicata

### Durante a Coleta
- [ ] Salvar localmente PRIMEIRO
- [ ] Usar transação atômica
- [ ] Registrar log de auditoria
- [ ] Capturar dados GPS (se disponível)

### Após a Coleta
- [ ] Confirmar salvamento local
- [ ] Tentar sincronizar (não bloquear)
- [ ] Atualizar contador de pendentes
- [ ] Notificar usuário do sucesso

### Sincronização
- [ ] Verificar conexão de rede
- [ ] Tentar batch sync primeiro
- [ ] Fallback para sync individual
- [ ] Registrar tentativas e erros
- [ ] Retry automático com backoff

### Manutenção
- [ ] Backup diário de pendentes
- [ ] Monitorar saúde do banco
- [ ] Limpar coletas antigas sincronizadas
- [ ] Verificar integridade dos dados

---

## 🚨 PLANO DE CONTINGÊNCIA

### Cenário 1: Banco Corrompido
```
1. Detectar corrupção (erro ao abrir banco)
2. Tentar recuperar de backup automático
3. Se falhar, criar novo banco
4. Notificar usuário da perda
5. Registrar incidente para análise
```

### Cenário 2: Sincronização Falhando Repetidamente
```
1. Após 5 tentativas, criar backup local
2. Notificar usuário para exportar dados
3. Enviar backup por email/WhatsApp
4. Continuar coletando (não bloquear)
5. Investigar causa da falha
```

### Cenário 3: Dispositivo Perdido/Danificado
```
1. Backup automático em nuvem (Google Drive)
2. Exportação manual periódica
3. Sincronização frequente (30 min)
4. Dados no servidor são fonte da verdade
```

### Cenário 4: App Desinstalado Acidentalmente
```
1. Backup em diretório externo (não apagado)
2. Ao reinstalar, oferecer restauração
3. Sincronizar com servidor
4. Recuperar dados não perdidos
```

---

## 📈 MÉTRICAS DE MONITORAMENTO

### Métricas Críticas (Monitorar em Tempo Real)

```kotlin
data class MetricasColeta(
    val totalColetadas: Int,
    val pendenteSincronizacao: Int,
    val taxaSucesso: Float,  // % sincronizadas com sucesso
    val tempoMedioSync: Long, // ms
    val errosUltimas24h: Int,
    val ultimaSync: Long,
    val saudeGeral: String  // OTIMA, BOA, ATENCAO, CRITICA
)
```

### Alertas Automáticos

```kotlin
fun verificarAlertas(metricas: MetricasColeta): List<Alerta> {
    val alertas = mutableListOf<Alerta>()
    
    // ⚠️ Muitas coletas pendentes
    if (metricas.pendenteSincronizacao > 500) {
        alertas.add(Alerta.CRITICO("${metricas.pendenteSincronizacao} coletas pendentes!"))
    }
    
    // ⚠️ Taxa de sucesso baixa
    if (metricas.taxaSucesso < 0.8f) {
        alertas.add(Alerta.ATENCAO("Taxa de sincronização: ${metricas.taxaSucesso * 100}%"))
    }
    
    // ⚠️ Sem sincronização há muito tempo
    val horasSemSync = (System.currentTimeMillis() - metricas.ultimaSync) / (1000 * 60 * 60)
    if (horasSemSync > 4) {
        alertas.add(Alerta.ATENCAO("Sem sincronização há $horasSemSync horas"))
    }
    
    return alertas
}
```

---

## ✅ RESUMO EXECUTIVO

### O que JÁ está PROTEGIDO ✅

1. ✅ **Persistência Local Garantida** - Room Database com REPLACE strategy
2. ✅ **Offline-First** - Salva localmente antes de tentar servidor
3. ✅ **Sync em Background** - WorkManager com retry automático
4. ✅ **Batch Sync** - Sincroniza múltiplas coletas de uma vez
5. ✅ **Índices de Performance** - Queries otimizadas
6. ✅ **Controle de Tentativas** - Registra erros de sincronização

### O que PRECISA ser IMPLEMENTADO ⚠️

1. ⚠️ **Validação Rigorosa** - Antes de salvar
2. ⚠️ **Transações Atômicas** - Tudo ou nada
3. ⚠️ **Backup Automático** - Exportar pendentes
4. ⚠️ **Detecção de Duplicatas** - Evitar coletas repetidas
5. ⚠️ **Log de Auditoria** - Rastreabilidade completa
6. ⚠️ **Monitoramento de Saúde** - Alertas proativos

### Nível de Risco Atual

```
🟢 BAIXO  - Dados locais protegidos
🟡 MÉDIO  - Sincronização pode falhar temporariamente
🔴 ALTO   - Sem backup automático de pendentes
```

### Recomendação Final

**IMPLEMENTAR IMEDIATAMENTE:**
1. Validação rigorosa (1 dia)
2. Transações atômicas (1 dia)
3. Backup automático (2 dias)

**Total: 4 dias de desenvolvimento para GARANTIA TOTAL**

---

**Criado em:** 20/11/2025  
**Prioridade:** 🔴 CRÍTICA  
**Status:** 📋 PLANO APROVADO - AGUARDANDO IMPLEMENTAÇÃO
