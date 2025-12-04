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
            
            val strategy = strategyFactory.getRemoteDataSource()
            
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
            
            val strategy = strategyFactory.getLocalDataSource()
            
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
            val localStrategy = strategyFactory.getLocalDataSource()
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
    
    // ========================================
    // Métodos para Inventário por Sala
    // ========================================
    
    /**
     * Busca patrimônios por sala com filtro opcional de status de coleta e paginação.
     * Usa estratégia offline-first (banco local).
     * 
     * @param salaId ID da sala
     * @param coletado Filtro de status: null = todos, true = coletados, false = não coletados
     * @param page Número da página (0-indexed)
     * @param pageSize Quantidade de itens por página
     * @return Result com lista de patrimônios ou erro
     */
    suspend fun buscarPorSala(
        salaId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios da sala $salaId (coletado=$coletado, page=$page)")
            
            // Usar estratégia local para offline-first
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.buscarPorSala(salaId, coletado, page, pageSize)
            
            if (result.isSuccess) {
                val patrimonios = result.getOrDefault(emptyList())
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios encontrados na sala $salaId")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios da sala: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios da sala", e)
            Result.failure(e)
        }
    }
    
    /**
     * Conta total de patrimônios em uma sala.
     * 
     * @param salaId ID da sala
     * @return Result com total de patrimônios ou erro
     */
    suspend fun contarPorSala(salaId: Int): Result<Int> {
        return try {
            Log.d(TAG, "Contando patrimônios da sala $salaId")
            
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.contarPorSala(salaId)
            
            if (result.isSuccess) {
                Log.d(TAG, "✓ Total: ${result.getOrNull()} patrimônios na sala $salaId")
            } else {
                Log.e(TAG, "✗ Erro ao contar patrimônios: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao contar patrimônios da sala", e)
            Result.failure(e)
        }
    }
    
    /**
     * Conta patrimônios coletados em uma sala.
     * 
     * @param salaId ID da sala
     * @return Result com total de patrimônios coletados ou erro
     */
    suspend fun contarColetadosPorSala(salaId: Int): Result<Int> {
        return try {
            Log.d(TAG, "Contando patrimônios coletados da sala $salaId")
            
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.contarColetadosPorSala(salaId)
            
            if (result.isSuccess) {
                Log.d(TAG, "✓ Total coletados: ${result.getOrNull()} na sala $salaId")
            } else {
                Log.e(TAG, "✗ Erro ao contar coletados: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao contar patrimônios coletados", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // Métodos para Busca Rápida de Patrimônio
    // ========================================
    
    /**
     * Busca patrimônios por query (número, descrição ou nome da sala)
     * Usa estratégia offline-first (banco local)
     * 
     * @param query Termo de busca
     * @return Result com lista de patrimônios ou erro
     * @see Requirements 1.1
     */
    suspend fun buscarPorQuery(query: String): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios por query: $query")
            
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.buscarPorQuery(query)
            
            if (result.isSuccess) {
                val patrimonios = result.getOrDefault(emptyList())
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios encontrados para '$query'")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios por query", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios coletados por query
     * 
     * @param query Termo de busca
     * @return Result com lista de patrimônios coletados ou erro
     * @see Requirements 3.1
     */
    suspend fun buscarColetadosPorQuery(query: String): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios COLETADOS por query: $query")
            
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.buscarColetadosPorQuery(query)
            
            if (result.isSuccess) {
                val patrimonios = result.getOrDefault(emptyList())
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios coletados encontrados para '$query'")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios coletados: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios coletados", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios pendentes (não coletados) por query
     * 
     * @param query Termo de busca
     * @return Result com lista de patrimônios pendentes ou erro
     * @see Requirements 3.2
     */
    suspend fun buscarPendentesPorQuery(query: String): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios PENDENTES por query: $query")
            
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.buscarPendentesPorQuery(query)
            
            if (result.isSuccess) {
                val patrimonios = result.getOrDefault(emptyList())
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios pendentes encontrados para '$query'")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios pendentes: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios pendentes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios com divergência por query
     * 
     * @param query Termo de busca
     * @param inventarioId ID do inventário ativo
     * @return Result com lista de patrimônios com divergência ou erro
     * @see Requirements 3.3
     */
    suspend fun buscarDivergenciasPorQuery(query: String, inventarioId: Int): Result<List<Patrimonio>> {
        return try {
            Log.d(TAG, "Buscando patrimônios com DIVERGÊNCIA por query: $query")
            
            val localStrategy = strategyFactory.getLocalDataSource()
            _currentDataSource.value = DataSourceType.LOCAL
            
            val result = localStrategy.buscarDivergenciasPorQuery(query, inventarioId)
            
            if (result.isSuccess) {
                val patrimonios = result.getOrDefault(emptyList())
                Log.d(TAG, "✓ ${patrimonios.size} patrimônios com divergência encontrados para '$query'")
            } else {
                Log.e(TAG, "✗ Erro ao buscar patrimônios com divergência: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao buscar patrimônios com divergência", e)
            Result.failure(e)
        }
    }
}
