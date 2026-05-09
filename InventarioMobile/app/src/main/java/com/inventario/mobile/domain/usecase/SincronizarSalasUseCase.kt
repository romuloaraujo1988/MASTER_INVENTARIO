package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.local.entity.SalaEntity
import com.inventario.mobile.utils.PreferencesManager
import javax.inject.Inject

/**
 * Use Case para sincronização incremental de salas.
 * 
 * Estratégia:
 * - Se lastSync = 0: Sync FULL (baixa todas as salas)
 * - Se lastSync > 0: Sync INCREMENTAL (apenas mudanças)
 * 
 * Benefícios:
 * - Reduz tráfego de dados (apenas mudanças)
 * - Mais rápido (menos dados para processar)
 * - Mantém dados atualizados em tempo real
 */
class SincronizarSalasUseCase @Inject constructor(
    private val salaApi: SalaApi,
    private val salaDao: SalaDao,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "SincronizarSalasUseCase"
    }
    
    /**
     * Resultado da sincronização
     */
    data class SyncResult(
        val success: Boolean,
        val syncType: String,
        val salasAdicionadas: Int,
        val salasAtualizadas: Int,
        val salasRemovidas: Int,
        val tempoMs: Long,
        val mensagem: String
    )
    
    /**
     * Executa sincronização incremental de salas.
     * 
     * @param forceFullSync Se true, força sync completo ignorando lastSync
     * @return Resultado da sincronização
     */
    suspend operator fun invoke(forceFullSync: Boolean = false): Result<SyncResult> {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Obter lastSync (0 = sync inicial)
            val lastSync = if (forceFullSync) 0L else preferencesManager.getLastSalaSyncTimestamp()
            
            Log.d(TAG, "Iniciando sync de salas. lastSync=$lastSync, forceFullSync=$forceFullSync")
            
            // Chamar API
            val response = salaApi.sincronizarSalas(lastSync)
            
            if (!response.isSuccessful) {
                val errorMsg = "Erro HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, errorMsg)
                return Result.failure(Exception(errorMsg))
            }
            
            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success) {
                val errorMsg = apiResponse?.message ?: "Resposta vazia do servidor"
                Log.e(TAG, errorMsg)
                return Result.failure(Exception(errorMsg))
            }
            
            val syncData = apiResponse.data
            if (syncData == null) {
                Log.w(TAG, "Dados de sync vazios")
                return Result.success(SyncResult(
                    success = true,
                    syncType = "EMPTY",
                    salasAdicionadas = 0,
                    salasAtualizadas = 0,
                    salasRemovidas = 0,
                    tempoMs = System.currentTimeMillis() - startTime,
                    mensagem = "Nenhuma mudança encontrada"
                ))
            }
            
            // Processar resultado
            val syncType = syncData.syncType
            val salas = syncData.salas
            val removidas = syncData.removidas
            val serverTime = syncData.serverTime
            
            Log.d(TAG, "Sync $syncType: ${salas.size} salas, ${removidas.size} removidas")
            
            var adicionadas = 0
            var atualizadas = 0
            
            if (syncData.isFullSync()) {
                // SYNC FULL: Limpar e inserir todas
                Log.d(TAG, "Executando FULL sync - limpando banco local")
                salaDao.limparTodas()
                
                val entities = salas.map { sala ->
                    SalaEntity(
                        id = sala.id,
                        nome = sala.nome,
                        idSetor = sala.idSetor,
                        nomeSetor = sala.nomeSetor,
                        ativa = sala.ativa,
                        dataUltimaAtualizacao = serverTime
                    )
                }
                
                salaDao.inserirTodas(entities)
                adicionadas = entities.size
                
                Log.d(TAG, "✓ FULL sync: $adicionadas salas inseridas")
                
            } else {
                // SYNC INCREMENTAL: Upsert modificadas, deletar removidas
                Log.d(TAG, "Executando INCREMENTAL sync")
                
                // Upsert salas modificadas
                if (salas.isNotEmpty()) {
                    val entities = salas.map { sala ->
                        SalaEntity(
                            id = sala.id,
                            nome = sala.nome,
                            idSetor = sala.idSetor,
                            nomeSetor = sala.nomeSetor,
                            ativa = sala.ativa,
                            dataUltimaAtualizacao = serverTime
                        )
                    }
                    
                    salaDao.upsertTodas(entities)
                    atualizadas = entities.size
                    
                    Log.d(TAG, "✓ ${atualizadas} salas atualizadas/adicionadas")
                }
                
                // Deletar salas removidas
                if (removidas.isNotEmpty()) {
                    salaDao.deletarPorIds(removidas)
                    Log.d(TAG, "✓ ${removidas.size} salas removidas")
                }
            }
            
            // Salvar novo timestamp
            preferencesManager.setLastSalaSyncTimestamp(serverTime)
            
            val tempoMs = System.currentTimeMillis() - startTime
            
            val result = SyncResult(
                success = true,
                syncType = syncType,
                salasAdicionadas = adicionadas,
                salasAtualizadas = atualizadas,
                salasRemovidas = removidas.size,
                tempoMs = tempoMs,
                mensagem = if (syncData.isFullSync()) {
                    "Sync completo: $adicionadas salas em ${tempoMs}ms"
                } else {
                    "Sync incremental: $atualizadas atualizadas, ${removidas.size} removidas em ${tempoMs}ms"
                }
            )
            
            Log.d(TAG, "✓ Sync concluído: ${result.mensagem}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização de salas", e)
            Result.failure(e)
        }
    }
    
    /**
     * Força sincronização completa (ignora lastSync)
     */
    suspend fun forceFullSync(): Result<SyncResult> {
        return invoke(forceFullSync = true)
    }
    
    /**
     * Verifica se precisa sincronizar (baseado no tempo desde última sync)
     * 
     * @param maxAgeMinutes Tempo máximo em minutos desde última sync
     * @return true se precisa sincronizar
     */
    fun needsSync(maxAgeMinutes: Int = 30): Boolean {
        val lastSync = preferencesManager.getLastSalaSyncTimestamp()
        if (lastSync == 0L) return true
        
        val now = System.currentTimeMillis()
        val ageMinutes = (now - lastSync) / (60 * 1000)
        
        return ageMinutes >= maxAgeMinutes
    }
    
    /**
     * Obtém timestamp da última sincronização
     */
    fun getLastSyncTimestamp(): Long {
        return preferencesManager.getLastSalaSyncTimestamp()
    }
}
