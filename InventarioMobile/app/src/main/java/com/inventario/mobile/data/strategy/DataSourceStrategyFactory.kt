package com.inventario.mobile.data.strategy

import android.content.Context
import android.util.Log
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.dao.SalaDao

/**
 * Factory Pattern - Cria a estratégia apropriada baseada na conectividade
 */
class DataSourceStrategyFactory(
    private val context: Context,
    private val patrimonioApi: PatrimonioApi,
    private val salaApi: SalaApi,
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao
) {
    
    companion object {
        private const val TAG = "DataSourceFactory"
    }
    
    private val remoteStrategy by lazy {
        RemoteDataSourceStrategy(context, patrimonioApi, salaApi)
    }
    
    private val localStrategy by lazy {
        LocalDataSourceStrategy(patrimonioDao, salaDao)
    }
    
    /**
     * Retorna a estratégia apropriada baseada na disponibilidade
     * Prioridade: Remote -> Local
     */
    suspend fun getStrategy(): DataSourceStrategy {
        Log.d(TAG, "Determinando estratégia de fonte de dados...")
        
        // Tentar remoto primeiro
        if (remoteStrategy.isAvailable()) {
            Log.d(TAG, "✓ Usando fonte REMOTA (servidor)")
            return remoteStrategy
        }
        
        // Fallback para local
        if (localStrategy.isAvailable()) {
            Log.d(TAG, "⚠ Usando fonte LOCAL (offline)")
            return localStrategy
        }
        
        // Se nenhum disponível, retorna local mesmo assim (vai falhar com mensagem apropriada)
        Log.w(TAG, "✗ Nenhuma fonte disponível, usando LOCAL (pode falhar)")
        return localStrategy
    }
    
    /**
     * Retorna estratégia remota (forçado)
     */
    fun getRemoteStrategy(): DataSourceStrategy = remoteStrategy
    
    /**
     * Retorna estratégia local (forçado)
     */
    fun getLocalStrategy(): DataSourceStrategy = localStrategy
    
    /**
     * Verifica qual estratégia está disponível
     */
    suspend fun getAvailableSourceType(): DataSourceType {
        return when {
            remoteStrategy.isAvailable() -> DataSourceType.REMOTE
            localStrategy.isAvailable() -> DataSourceType.LOCAL
            else -> DataSourceType.LOCAL // Default
        }
    }
}
