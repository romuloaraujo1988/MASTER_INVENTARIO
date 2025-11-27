package com.inventario.mobile.data.strategy

import android.content.Context
import android.util.Log
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory Pattern - Cria a estratégia apropriada baseada na conectividade
 * v2.7: Respeita configuração de modo offline forçado
 */
@Singleton
class DataSourceStrategyFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val patrimonioApi: PatrimonioApi,
    private val salaApi: SalaApi,
    private val patrimonioDao: PatrimonioDao,
    private val salaDao: SalaDao
) {
    
    companion object {
        private const val TAG = "DataSourceFactory"
    }
    
    // PreferencesManager para verificar modo offline forçado
    private val preferencesManager by lazy { PreferencesManager(context) }
    
    private val remoteStrategy by lazy {
        RemoteDataSourceStrategy(context, patrimonioApi, salaApi)
    }
    
    private val localStrategy by lazy {
        LocalDataSourceStrategy(patrimonioDao, salaDao)
    }
    
    /**
     * Retorna a estratégia apropriada baseada na disponibilidade
     * v2.7: Respeita modo offline forçado
     * Prioridade: ForceOffline -> Remote -> Local
     */
    suspend fun getStrategy(): DataSourceStrategy {
        Log.d(TAG, "Determinando estratégia de fonte de dados...")
        
        // v2.7: Verificar se modo offline está forçado
        if (preferencesManager.isForceOfflineMode()) {
            Log.d(TAG, "🔒 MODO OFFLINE FORÇADO - Usando fonte LOCAL")
            if (localStrategy.isAvailable()) {
                return localStrategy
            } else {
                Log.w(TAG, "⚠️ Modo offline forçado mas banco local vazio!")
                return localStrategy // Retorna mesmo assim para mostrar erro apropriado
            }
        }
        
        // Tentar remoto primeiro (comportamento padrão)
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
     * v2.7: Respeita modo offline forçado
     */
    suspend fun getAvailableSourceType(): DataSourceType {
        // v2.7: Se modo offline forçado, sempre retorna LOCAL
        if (preferencesManager.isForceOfflineMode()) {
            Log.d(TAG, "🔒 Modo offline forçado - retornando LOCAL")
            return DataSourceType.LOCAL
        }
        
        return when {
            remoteStrategy.isAvailable() -> DataSourceType.REMOTE
            localStrategy.isAvailable() -> DataSourceType.LOCAL
            else -> DataSourceType.LOCAL // Default
        }
    }
    
    /**
     * Verifica se o modo offline está forçado
     */
    fun isForceOfflineMode(): Boolean {
        return preferencesManager.isForceOfflineMode()
    }
    
    /**
     * Define o modo offline forçado
     */
    fun setForceOfflineMode(enabled: Boolean) {
        preferencesManager.setForceOfflineMode(enabled)
        Log.d(TAG, "Modo offline forçado: ${if (enabled) "ATIVADO" else "DESATIVADO"}")
    }
}
