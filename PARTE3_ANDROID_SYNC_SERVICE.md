# 📱 Sistema de Sincronização Offline - Parte 3

## Android - Serviço de Sincronização

### SyncRepository.kt

```kotlin
class SyncRepository(
    private val api: SyncApiService,
    private val database: AppDatabase
) {
    private val patrimonioDao = database.patrimonioDao()
    private val salaDao = database.salaDao()
    private val responsavelDao = database.responsavelDao()
    private val metadataDao = database.syncMetadataDao()
    
    /**
     * Sincronização completa - baixa todos os dados
     */
    suspend fun sincronizacaoCompleta(): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                Log.i("SyncRepository", "Iniciando sincronização completa")
                
                // 1. Buscar dados da API
                val response = api.sincronizacaoCompleta()
                
                if (!response.success) {
                    return@withContext Result.failure(
                        Exception(response.message)
                    )
                }
                
                val data = response.data
                
                // 2. Limpar dados antigos
                patrimonioDao.deleteAll()
                salaDao.deleteAll()
                responsavelDao.deleteAll()
                
                // 3. Inserir novos dados
                val patrimonios = data.patrimonios.map { it.toEntity() }
                val salas = data.salas.map { it.toEntity() }
                val responsaveis = data.responsaveis.map { it.toEntity() }
                
                patrimonioDao.insertAll(patrimonios)
                salaDao.insertAll(salas)
                responsavelDao.insertAll(responsaveis)
                
                // 4. Salvar timestamp da sincronização
                metadataDao.insert(
                    SyncMetadataEntity(
                        chave = "ultima_sincronizacao",
                        valor = data.timestamp.toString()
                    )
                )
                
                Log.i("SyncRepository", "Sincronização completa: " +
                    "${patrimonios.size} patrimônios, " +
                    "${salas.size} salas, " +
                    "${responsaveis.size} responsáveis")
                
                Result.success(
                    SyncResult(
                        patrimonios = patrimonios.size,
                        salas = salas.size,
                        responsaveis = responsaveis.size,
                        timestamp = data.timestamp
                    )
                )
                
            } catch (e: Exception) {
                Log.e("SyncRepository", "Erro na sincronização", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sincronização incremental - apenas atualizações
     */
    suspend fun sincronizacaoIncremental(): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar timestamp da última sincronização
                val ultimaSync = metadataDao.get("ultima_sincronizacao")
                val timestamp = ultimaSync?.valor?.toLongOrNull()
                
                if (timestamp == null) {
                    // Se nunca sincronizou, fazer completa
                    return@withContext sincronizacaoCompleta()
                }
                
                // Verificar se há atualizações
                val checkResponse = api.verificarAtualizacoes(timestamp)
                
                if (!checkResponse.data.temAtualizacoes) {
                    Log.i("SyncRepository", "Nenhuma atualização disponível")
                    return@withContext Result.success(
                        SyncResult(0, 0, 0, System.currentTimeMillis())
                    )
                }
                
                // Buscar apenas patrimônios atualizados
                val response = api.sincronizarPatrimonios(timestamp)
                val patrimonios = response.data.patrimonios.map { it.toEntity() }
                
                patrimonioDao.insertAll(patrimonios)
                
                // Atualizar timestamp
                metadataDao.insert(
                    SyncMetadataEntity(
                        chave = "ultima_sincronizacao",
                        valor = response.data.timestamp.toString()
                    )
                )
                
                Log.i("SyncRepository", "Sincronização incremental: ${patrimonios.size} patrimônios")
                
                Result.success(
                    SyncResult(
                        patrimonios = patrimonios.size,
                        salas = 0,
                        responsaveis = 0,
                        timestamp = response.data.timestamp
                    )
                )
                
            } catch (e: Exception) {
                Log.e("SyncRepository", "Erro na sincronização incremental", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sincronizar coletas pendentes para o servidor
     */
    suspend fun sincronizarColetasPendentes(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val coletaDao = database.coletaOfflineDao()
                val pendentes = coletaDao.getPendentes()
                
                if (pendentes.isEmpty()) {
                    return@withContext Result.success(0)
                }
                
                var sincronizadas = 0
                
                for (coleta in pendentes) {
                    try {
                        val request = coleta.toRequest()
                        api.registrarColeta(request)
                        
                        coletaDao.marcarSincronizada(coleta.id)
                        sincronizadas++
                        
                    } catch (e: Exception) {
                        Log.e("SyncRepository", "Erro ao sincronizar coleta ${coleta.id}", e)
                        // Continua tentando as outras
                    }
                }
                
                // Limpar coletas já sincronizadas
                coletaDao.limparSincronizadas()
                
                Log.i("SyncRepository", "Sincronizadas $sincronizadas de ${pendentes.size} coletas")
                
                Result.success(sincronizadas)
                
            } catch (e: Exception) {
                Log.e("SyncRepository", "Erro ao sincronizar coletas", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obter estatísticas de sincronização
     */
    suspend fun obterEstatisticas(): SyncStats {
        return withContext(Dispatchers.IO) {
            val totalPatrimonios = patrimonioDao.count()
            val totalSalas = salaDao.getAll().size
            val totalResponsaveis = responsavelDao.getAll().size
            val coletasPendentes = database.coletaOfflineDao().countPendentes()
            
            val ultimaSync = metadataDao.get("ultima_sincronizacao")
            val timestamp = ultimaSync?.valor?.toLongOrNull() ?: 0L
            
            SyncStats(
                totalPatrimonios = totalPatrimonios,
                totalSalas = totalSalas,
                totalResponsaveis = totalResponsaveis,
                coletasPendentes = coletasPendentes,
                ultimaSincronizacao = timestamp
            )
        }
    }
}

// Data classes
data class SyncResult(
    val patrimonios: Int,
    val salas: Int,
    val responsaveis: Int,
    val timestamp: Long
)

data class SyncStats(
    val totalPatrimonios: Int,
    val totalSalas: Int,
    val totalResponsaveis: Int,
    val coletasPendentes: Int,
    val ultimaSincronizacao: Long
)
```

---

## Continua na Parte 4...
