package com.inventario.mobile.data.repository

import android.content.Context
import android.util.Log
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.local.dao.SincronizacaoDao
import com.inventario.mobile.data.local.entity.PatrimonioEntity
import com.inventario.mobile.data.local.entity.SalaEntity
import com.inventario.mobile.data.local.entity.SincronizacaoEntity
import com.inventario.mobile.utils.NetworkUtils
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Repository para sincronização de dados entre servidor e banco local
 */
class SyncRepository(
    private val context: Context,
    private val patrimonioApi: PatrimonioApi,
    private val salaApi: SalaApi,
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao,
    private val sincronizacaoDao: SincronizacaoDao,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "SyncRepository"
        private const val SYNC_TYPE_FULL = "FULL"
        private const val SYNC_TYPE_INCREMENTAL = "INCREMENTAL"
    }
    
    /**
     * Resultado da sincronização
     */
    data class SyncResult(
        val success: Boolean,
        val message: String,
        val patrimoniosSincronizados: Int = 0,
        val salasSincronizadas: Int = 0,
        val tempoDecorrido: Long = 0,
        val error: Exception? = null
    )
    
    /**
     * Força sincronização completa do servidor para o banco local
     */
    suspend fun forceSyncFromServer(): Result<SyncResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "INICIANDO SINCRONIZAÇÃO FORÇADA")
            Log.d(TAG, "═══════════════════════════════════════")
            
            // Verificar conectividade
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return@withContext Result.failure(
                    Exception("Sem conexão com a internet. Conecte-se à rede e tente novamente.")
                )
            }
            
            var patrimoniosSincronizados = 0
            var salasSincronizadas = 0
            
            // 1. Sincronizar Salas
            Log.d(TAG, "1. Sincronizando salas...")
            try {
                val salasResponse = salaApi.listarSalas()
                
                if (salasResponse.isSuccessful && salasResponse.body()?.success == true) {
                    val salas = salasResponse.body()?.data ?: emptyList()
                    
                    // Limpar salas antigas
                    salaDao.limparTodas()
                    
                    // Converter Sala (data.model) para SalaEntity
                    val salasEntities = salas.map { sala ->
                        SalaEntity(
                            id = sala.id,
                            nome = sala.nome,
                            idSetor = null, // Sala não tem setorId
                            nomeSetor = null
                        )
                    }
                    
                    salaDao.inserirTodas(salasEntities)
                    salasSincronizadas = salasEntities.size
                    
                    Log.d(TAG, "✓ ${salasSincronizadas} salas sincronizadas")
                } else {
                    Log.w(TAG, "Falha ao sincronizar salas: ${salasResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sincronizar salas", e)
                // Continuar mesmo com erro nas salas
            }
            
            // 2. Sincronizar Patrimônios
            Log.d(TAG, "2. Sincronizando patrimônios...")
            try {
                val patrimoniosResponse = patrimonioApi.listarPatrimonios()
                
                if (patrimoniosResponse.isSuccessful && patrimoniosResponse.body()?.success == true) {
                    val patrimonios = patrimoniosResponse.body()?.data ?: emptyList()
                    
                    // Limpar patrimônios antigos
                    patrimonioDao.limparTodos()
                    
                    // Converter Patrimonio (data.model) para PatrimonioEntity
                    val patrimoniosEntities = patrimonios.map { patrimonio ->
                        PatrimonioEntity(
                            id = patrimonio.id.toInt(),
                            numero = patrimonio.numeroPatrimonio,
                            descricao = patrimonio.descricao,
                            idSala = patrimonio.salaId?.toInt(),
                            nomeSala = patrimonio.salaNome,
                            idResponsavel = patrimonio.responsavelId?.toInt(),
                            nomeResponsavel = patrimonio.responsavelNome,
                            status = patrimonio.estado ?: "ATIVO",
                            coletado = patrimonio.coletado
                        )
                    }
                    
                    patrimonioDao.inserirTodos(patrimoniosEntities)
                    patrimoniosSincronizados = patrimoniosEntities.size
                    
                    Log.d(TAG, "✓ ${patrimoniosSincronizados} patrimônios sincronizados")
                } else {
                    Log.w(TAG, "Falha ao sincronizar patrimônios: ${patrimoniosResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sincronizar patrimônios", e)
                throw e // Patrimônios são críticos
            }
            
            // 3. Registrar sincronização
            val tempoDecorrido = System.currentTimeMillis() - startTime
            val sincronizacao = SincronizacaoEntity(
                id = 0,
                entidade = "SYNC_FULL",
                entidadeId = 0,
                operacao = "DOWNLOAD",
                sincronizado = true,
                dataHora = System.currentTimeMillis(),
                erro = null
            )
            
            sincronizacaoDao.inserir(sincronizacao)
            preferencesManager.saveLastSyncTime(System.currentTimeMillis())
            
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO")
            Log.d(TAG, "Patrimônios: $patrimoniosSincronizados")
            Log.d(TAG, "Salas: $salasSincronizadas")
            Log.d(TAG, "Tempo: ${tempoDecorrido}ms")
            Log.d(TAG, "═══════════════════════════════════════")
            
            val result = SyncResult(
                success = true,
                message = "Sincronização completa realizada com sucesso",
                patrimoniosSincronizados = patrimoniosSincronizados,
                salasSincronizadas = salasSincronizadas,
                tempoDecorrido = tempoDecorrido
            )
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "ERRO NA SINCRONIZAÇÃO")
            Log.e(TAG, "Erro: ${e.message}", e)
            Log.e(TAG, "═══════════════════════════════════════")
            
            // Registrar falha
            val tempoDecorrido = System.currentTimeMillis() - startTime
            val sincronizacao = SincronizacaoEntity(
                id = 0,
                entidade = "SYNC_FULL",
                entidadeId = 0,
                operacao = "DOWNLOAD",
                sincronizado = false,
                dataHora = System.currentTimeMillis(),
                erro = e.message
            )
            
            try {
                sincronizacaoDao.inserir(sincronizacao)
            } catch (dbError: Exception) {
                Log.e(TAG, "Erro ao registrar falha de sincronização", dbError)
            }
            
            val result = SyncResult(
                success = false,
                message = "Erro na sincronização: ${e.message}",
                tempoDecorrido = tempoDecorrido,
                error = e
            )
            
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se há dados no banco local
     */
    suspend fun hasLocalData(): Boolean = withContext(Dispatchers.IO) {
        try {
            val patrimoniosCount = patrimonioDao.contarTodos()
            val salasCount = salaDao.contar()
            
            patrimoniosCount > 0 || salasCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar dados locais", e)
            false
        }
    }
    
    /**
     * Obtém estatísticas do banco local
     */
    suspend fun getLocalStats(): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            mapOf(
                "patrimonios" to patrimonioDao.contarTodos(),
                "salas" to salaDao.contar(),
                "coletados" to patrimonioDao.contarColetados(),
                "pendentes" to patrimonioDao.contarNaoColetados()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter estatísticas locais", e)
            emptyMap()
        }
    }
    
    /**
     * Obtém última sincronização
     */
    suspend fun getLastSync(): SincronizacaoEntity? = withContext(Dispatchers.IO) {
        try {
            sincronizacaoDao.getUltimaSincronizacao()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter última sincronização", e)
            null
        }
    }
    
    /**
     * Limpa todos os dados locais
     */
    suspend fun clearLocalData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Limpando dados locais...")
            
            patrimonioDao.limparTodos()
            salaDao.limparTodas()
            
            Log.d(TAG, "✓ Dados locais limpos")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar dados locais", e)
            Result.failure(e)
        }
    }
}
