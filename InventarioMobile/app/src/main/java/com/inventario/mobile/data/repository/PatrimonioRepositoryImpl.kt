package com.inventario.mobile.data.repository

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.observer.ConnectivityObserver
import com.inventario.mobile.data.observer.NetworkConnectivityObserver
import com.inventario.mobile.data.strategy.DataSourceStrategyFactory
import com.inventario.mobile.data.strategy.DataSourceType
import com.inventario.mobile.data.model.Patrimonio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Pattern com Strategy Pattern
 * Gerencia acesso a patrimônios com fallback automático entre fontes de dados
 */
@Singleton
class PatrimonioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val strategyFactory: DataSourceStrategyFactory
) {
    
    companion object {
        private const val TAG = "PatrimonioRepository"
    }
    
    // Observer de conectividade
    private val connectivityObserver: ConnectivityObserver = NetworkConnectivityObserver(context)
    
    // Estado atual da fonte de dados
    private val _currentDataSource = MutableStateFlow(DataSourceType.REMOTE)
    val currentDataSource: Flow<DataSourceType> = _currentDataSource.asStateFlow()
    
    /**
     * Observa mudanças de conectividade
     */
    fun observeConnectivity(): Flow<ConnectivityObserver.Status> {
        return connectivityObserver.observe()
    }
    
    /**
     * Busca todos os patrimônios
     * Usa estratégia apropriada automaticamente
     */
    suspend fun getPatrimonios(): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "BUSCANDO PATRIMÔNIOS")
            
            val strategy = strategyFactory.getStrategy()
            _currentDataSource.value = strategy.getSourceType()
            
            Log.d(TAG, "Fonte de dados: ${strategy.getSourceType()}")
            
            val result = strategy.getPatrimonios()
            
            if (result.isSuccess) {
                val patrimonios = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios obtidos")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios: ${result.exceptionOrNull()?.message}")
            }
            
            Log.d(TAG, "═══════════════════════════════════════")
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônio por número
     * Usa estratégia apropriada automaticamente
     */
    suspend fun getPatrimonioPorNumero(numero: String): Result<Patrimonio> {
        return try {
            Log.d(TAG, "Buscando patrimônio: $numero")
            
            val strategy = strategyFactory.getStrategy()
            _currentDataSource.value = strategy.getSourceType()
            
            Log.d(TAG, "Fonte de dados: ${strategy.getSourceType()}")
            
            val result = strategy.getPatrimonioPorNumero(numero)
            
            if (result.isSuccess) {
                Log.d(TAG, "✓ Patrimônio encontrado")
            } else {
                Log.w(TAG, "✗ Patrimônio não encontrado: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônio", e)
            Result.failure(e)
        }
    }
    
    /**
     * Força uso da fonte remota (servidor)
     */
    suspend fun getPatrimoniosFromRemote(): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Forçando busca REMOTA")
            
            val strategy = strategyFactory.getRemoteStrategy()
            
            if (!strategy.isAvailable()) {
                return Result.failure(Exception("Servidor não disponível. Verifique sua conexão."))
            }
            
            _currentDataSource.value = DataSourceType.REMOTE
            strategy.getPatrimonios()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar do servidor", e)
            Result.failure(e)
        }
    }
    
    /**
     * Força uso da fonte local (banco SQLite)
     */
    suspend fun getPatrimoniosFromLocal(): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Forçando busca LOCAL")
            
            val strategy = strategyFactory.getLocalStrategy()
            
            if (!strategy.isAvailable()) {
                return Result.failure(Exception("Banco local vazio. Sincronize os dados primeiro."))
            }
            
            _currentDataSource.value = DataSourceType.LOCAL
            strategy.getPatrimonios()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar do banco local", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verifica qual fonte de dados está disponível
     */
    suspend fun getAvailableDataSource(): DataSourceType {
        return strategyFactory.getAvailableSourceType()
    }
    
    /**
     * Busca descrições de patrimônios não coletados
     * Usa estratégia apropriada automaticamente
     */
    suspend fun buscarDescricoesNaoColetadas(): Result<List<String>> {
        return try {
            Log.d(TAG, "Buscando descrições não coletadas")
            
            val strategy = strategyFactory.getStrategy()
            _currentDataSource.value = strategy.getSourceType()
            
            Log.d(TAG, "Fonte de dados: ${strategy.getSourceType()}")
            
            val result = strategy.buscarDescricoesNaoColetadas()
            
            if (result.isSuccess) {
                val descricoes = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ ${descricoes.size} descrições encontradas")
            } else {
                Log.e(TAG, "✗ Erro ao buscar descrições: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar descrições", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios por descrição (não coletados)
     * Usa estratégia apropriada automaticamente
     */
    suspend fun buscarPorDescricaoNaoColetados(descricao: String): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios por descrição: $descricao")
            
            val strategy = strategyFactory.getStrategy()
            _currentDataSource.value = strategy.getSourceType()
            
            Log.d(TAG, "Fonte de dados: ${strategy.getSourceType()}")
            
            val result = strategy.buscarPorDescricaoNaoColetados(descricao)
            
            if (result.isSuccess) {
                val patrimonios = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios encontrados")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios por descrição", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônio por número
     * Usa estratégia apropriada automaticamente (offline-first)
     */
    suspend fun buscarPorNumero(numero: String): Patrimonio? {
        return try {
            Log.d(TAG, "Buscando patrimônio por número: $numero")
            
            val strategy = strategyFactory.getStrategy()
            _currentDataSource.value = strategy.getSourceType()
            
            Log.d(TAG, "Fonte de dados: ${strategy.getSourceType()}")
            
            val result = strategy.getPatrimonioPorNumero(numero)
            
            if (result.isSuccess) {
                val patrimonio = result.getOrNull()
                Log.d(TAG, "✓ Patrimônio encontrado: ${patrimonio?.descricao}")
                patrimonio
            } else {
                Log.w(TAG, "✗ Patrimônio não encontrado")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônio", e)
            null
        }
    }
    
    /**
     * Busca patrimônio APENAS no banco local (offline)
     * Usado como fallback quando servidor está inacessível
     */
    suspend fun buscarPorNumeroLocal(numero: String): Patrimonio? {
        return try {
            Log.d(TAG, "Buscando patrimônio LOCALMENTE: $numero")
            
            // Forçar uso da estratégia local
            val localStrategy = strategyFactory.getLocalStrategy()
            _currentDataSource.value = DataSourceType.LOCAL
            
            if (!localStrategy.isAvailable()) {
                Log.w(TAG, "⚠️ Banco local não disponível ou vazio")
                return null
            }
            
            val result = localStrategy.getPatrimonioPorNumero(numero)
            
            if (result.isSuccess) {
                val patrimonio = result.getOrNull()
                Log.d(TAG, "✓ Patrimônio encontrado no banco local: ${patrimonio?.descricao}")
                patrimonio
            } else {
                Log.w(TAG, "✗ Patrimônio não encontrado no banco local")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônio localmente", e)
            null
        }
    }
}
