package com.inventario.mobile.data.repository

import android.app.Application
import android.content.Context
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.data.remote.api.ApiClient

/**
 * InventarioRepository stub - Mantido para compatibilidade temporária
 * 
 * NOTA: Este é um stub temporário para manter compatibilidade com código legado.
 * Para novas funcionalidades, use os repositórios Clean Architecture específicos:
 * - SalaRepository para salas
 * - PatrimonioRepository para patrimônios
 * - ColetaRepository para coletas
 */
class InventarioRepository(
    private val apiService: ApiService,
    private val localDataManager: LocalDataManager,
    private val context: Context
) {
    
    // Construtor alternativo para aceitar Application
    constructor(application: Application) : this(
        ApiClient.getApiService(application.applicationContext),
        LocalDataManager.getInstance(application.applicationContext),
        application.applicationContext
    )
    
    companion object {
        @Volatile
        private var INSTANCE: InventarioRepository? = null
        
        fun getInstance(context: Context, apiService: ApiService): InventarioRepository {
            return INSTANCE ?: synchronized(this) {
                val localDataManager = LocalDataManager.getInstance(context)
                INSTANCE ?: InventarioRepository(apiService, localDataManager, context).also { INSTANCE = it }
            }
        }
        
        // Cache de estatísticas (válido por 30 segundos)
        private const val CACHE_DURATION_MS = 30_000L
    }
    
    // Cache em memória para estatísticas
    private var cachedStats: DashboardStats? = null
    private var cacheTimestamp: Long = 0L
    
    /**
     * Verifica se o cache ainda é válido
     */
    private fun isCacheValid(): Boolean {
        return cachedStats != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
    }
    
    /**
     * Invalida o cache de estatísticas
     * Deve ser chamado após registrar uma coleta
     */
    fun invalidarCacheEstatisticas() {
        android.util.Log.d("InventarioRepository", "Cache de estatísticas invalidado")
        cachedStats = null
        cacheTimestamp = 0L
    }
    
    // Métodos stub - retornam valores padrão
    suspend fun getAllPatrimoniosList(): List<Patrimonio> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando todos os patrimônios...")
            
            val response = apiService.getAllPatrimonios(page = 0, size = 10000)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val patrimonios = apiResponse.data.map { dto ->
                        Patrimonio(
                            id = dto.id,
                            numeroPatrimonio = dto.codigo,
                            descricao = dto.descricao,
                            marca = dto.marca,
                            modelo = dto.modelo,
                            numeroSerie = dto.numeroSerie,
                            estado = dto.estado,
                            valor = dto.valor,
                            setorId = dto.setorId,
                            setorNome = dto.setorNome,
                            salaId = dto.salaId,
                            salaNome = dto.salaNome,
                            responsavelId = dto.responsavelId,
                            responsavelNome = dto.responsavelNome,
                            coletado = dto.coletado,
                            dataColeta = dto.dataColeta,
                            observacoesColeta = null,
                            observacoes = dto.observacoes
                        )
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${patrimonios.size} patrimônios carregados")
                    patrimonios
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    emptyList()
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar patrimônios", e)
            emptyList()
        }
    }
    
    suspend fun getPatrimoniosColetados(): List<Patrimonio> = emptyList()
    
    suspend fun getPatrimoniosNaoColetados(): List<Patrimonio> = emptyList()
    
    suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> = Result.success(null)
    
    suspend fun coletarPatrimonio(
        patrimonio: Patrimonio,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> = Result.failure(Exception("Use ColetaRepository"))
    
    suspend fun coletarPatrimonioComSala(
        patrimonio: Patrimonio,
        salaNome: String,
        estadoEncontrado: String = "BOM",
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> = Result.failure(Exception("Use ColetaRepository"))
    
    suspend fun getColetas(): List<Coleta> = emptyList()
    
    suspend fun getColetasPaginadas(
        page: Int = 0,
        size: Int = 50,
        useCache: Boolean = true
    ): Result<PagedColetasResult> {
        // TODO: Implementar busca de coletas do banco Room
        // Por enquanto retorna lista vazia para não quebrar a compilação
        android.util.Log.w("InventarioRepository", "getColetasPaginadas() não implementado - retornando lista vazia")
        return Result.success(
            PagedColetasResult(emptyList(), 0, 0, 0, 0, false, false)
        )
    }
    
    suspend fun removeColeta(coletaId: Int): Result<Unit> = Result.success(Unit)
    
    suspend fun isPatrimonioColetado(patrimonioId: Long): Boolean = false
    
    suspend fun sincronizarTodosDados(): Int = 0
    
    suspend fun buscarDescricoesNaoColetadas(): List<String> = emptyList()
    
    suspend fun syncData(): Result<Unit> = Result.success(Unit)
    
    // Métodos stub adicionais para compatibilidade
    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            // Verificar cache primeiro
            if (isCacheValid()) {
                android.util.Log.d("InventarioRepository", "✓ Usando estatísticas do cache (${(System.currentTimeMillis() - cacheTimestamp) / 1000}s atrás)")
                return Result.success(cachedStats!!)
            }
            
            android.util.Log.d("InventarioRepository", "Carregando estatísticas do dashboard (otimizado)...")
            
            // Usar endpoint otimizado ao invés de buscar todos os patrimônios
            val response = apiService.getDashboardStats()
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    
                    val stats = DashboardStats(
                        totalPatrimonios = dto.totalPatrimonios,
                        coletados = dto.patrimoniosColetados,
                        naoColetados = dto.patrimoniosPendentes,
                        percentualColetado = dto.percentualConclusao,
                        coletoresAtivos = dto.coletoresAtivos,
                        divergencias = dto.divergencias,
                        valorTotal = dto.valorTotal
                    )
                    
                    android.util.Log.d("InventarioRepository", "✓ Estatísticas carregadas (otimizado):")
                    android.util.Log.d("InventarioRepository", "  Total: ${stats.totalPatrimonios}")
                    android.util.Log.d("InventarioRepository", "  Coletados: ${stats.coletados}")
                    android.util.Log.d("InventarioRepository", "  Não Coletados: ${stats.naoColetados}")
                    android.util.Log.d("InventarioRepository", "  Percentual: ${String.format("%.2f", stats.percentualColetado)}%")
                    android.util.Log.d("InventarioRepository", "  Coletores Ativos: ${stats.coletoresAtivos}")
                    android.util.Log.d("InventarioRepository", "  Divergências: ${stats.divergencias}")
                    
                    // Atualizar cache
                    cachedStats = stats
                    cacheTimestamp = System.currentTimeMillis()
                    
                    Result.success(stats)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    
                    // Fallback: calcular localmente se o endpoint falhar
                    android.util.Log.d("InventarioRepository", "Usando fallback: calculando estatísticas localmente...")
                    calcularEstatisticasLocalmente()
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                
                // Fallback: calcular localmente
                android.util.Log.d("InventarioRepository", "Usando fallback: calculando estatísticas localmente...")
                calcularEstatisticasLocalmente()
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao carregar estatísticas", e)
            
            // Fallback: tentar calcular localmente
            try {
                android.util.Log.d("InventarioRepository", "Usando fallback: calculando estatísticas localmente...")
                calcularEstatisticasLocalmente()
            } catch (fallbackError: Exception) {
                android.util.Log.e("InventarioRepository", "Erro no fallback", fallbackError)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Fallback: Calcula estatísticas localmente quando o endpoint otimizado falha
     * Usa cache do Room Database para melhor performance
     */
    private suspend fun calcularEstatisticasLocalmente(): Result<DashboardStats> {
        return try {
            // Tentar buscar do banco local primeiro (mais rápido)
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(context)
            val patrimonioDao = database.patrimonioDao()
            
            val total = patrimonioDao.contarTodos()
            val coletados = patrimonioDao.contarColetados()
            val naoColetados = patrimonioDao.contarNaoColetados()
            val percentual = if (total > 0) (coletados.toDouble() / total.toDouble()) * 100.0 else 0.0
            
            val stats = DashboardStats(
                totalPatrimonios = total,
                coletados = coletados,
                naoColetados = naoColetados,
                percentualColetado = percentual
            )
            
            android.util.Log.d("InventarioRepository", "✓ Estatísticas calculadas localmente:")
            android.util.Log.d("InventarioRepository", "  Total: $total")
            android.util.Log.d("InventarioRepository", "  Coletados: $coletados")
            android.util.Log.d("InventarioRepository", "  Não Coletados: $naoColetados")
            
            // Atualizar cache
            cachedStats = stats
            cacheTimestamp = System.currentTimeMillis()
            
            Result.success(stats)
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao calcular estatísticas localmente", e)
            Result.failure(e)
        }
    }
    
    suspend fun getPatrimoniosByResponsavel(
        responsavelId: Int,
        page: Int = 0,
        size: Int = 50,
        coletado: Boolean? = null
    ): Result<List<Patrimonio>> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando patrimônios do responsável $responsavelId (page: $page, size: $size, coletado: $coletado)")
            
            val response = apiService.getPatrimoniosByResponsavel(responsavelId, page, size, coletado)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val patrimonios = apiResponse.data.map { dto ->
                        Patrimonio(
                            id = dto.id,
                            numeroPatrimonio = dto.codigo,
                            descricao = dto.descricao,
                            marca = dto.marca,
                            modelo = dto.modelo,
                            numeroSerie = dto.numeroSerie,
                            estado = dto.estado,
                            valor = dto.valor,
                            setorId = dto.setorId,
                            setorNome = dto.setorNome,
                            salaId = dto.salaId,
                            salaNome = dto.salaNome,
                            responsavelId = dto.responsavelId,
                            responsavelNome = dto.responsavelNome,
                            coletado = dto.coletado,
                            dataColeta = dto.dataColeta,
                            observacoesColeta = null,
                            observacoes = dto.observacoes
                        )
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${patrimonios.size} patrimônios carregados do responsável $responsavelId")
                    Result.success(patrimonios)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    Result.success(emptyList())
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro ao buscar patrimônios: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar patrimônios do responsável", e)
            Result.failure(e)
        }
    }
    
    suspend fun getResponsaveis(): Result<List<com.inventario.mobile.data.model.Responsavel>> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando responsáveis...")
            
            val response = apiService.getResponsaveis()
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val responsaveis = apiResponse.data.map { dto ->
                        com.inventario.mobile.data.model.Responsavel.fromDto(dto)
                    }
                    
                    android.util.Log.d("InventarioRepository", "✓ ${responsaveis.size} responsáveis carregados")
                    Result.success(responsaveis)
                } else {
                    android.util.Log.w("InventarioRepository", "API retornou success=false ou data=null")
                    Result.success(emptyList())
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro ao buscar responsáveis: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar responsáveis", e)
            Result.failure(e)
        }
    }
    
    suspend fun getColetasPendentes(): List<Coleta> = emptyList()
    
    suspend fun getLastSyncTime(): Long = 0L
    
    suspend fun sincronizarDados(): Result<Int> = Result.success(0)
    
    /**
     * Busca evolução de coletas por dia (últimos N dias)
     * Usado para gráfico de linhas no dashboard
     */
    suspend fun getColetasEvolucao(dias: Int = 30): Result<List<com.inventario.mobile.data.remote.dto.ColetasPorDiaDto>> {
        return try {
            android.util.Log.d("InventarioRepository", "Buscando evolução de coletas (últimos $dias dias)...")
            
            val response = apiService.getColetasEvolucao(dias = dias)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    // Converter Map<String, Int> para List<ColetasPorDiaDto>
                    val evolucaoMap = apiResponse.data["evolucao"] as? Map<*, *>
                    
                    if (evolucaoMap != null) {
                        val evolucaoList = evolucaoMap.entries.map { entry ->
                            val dataFormatada = entry.key.toString()
                            val quantidade = (entry.value as? Number)?.toInt() ?: 0
                            
                            // Converter data formatada "dd/MM" para "2025-11-dd"
                            val ano = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            val partes = dataFormatada.split("/")
                            val dia = partes.getOrNull(0)?.padStart(2, '0') ?: "01"
                            val mes = partes.getOrNull(1)?.padStart(2, '0') ?: "01"
                            val dataISO = "$ano-$mes-$dia"
                            
                            com.inventario.mobile.data.remote.dto.ColetasPorDiaDto(
                                data = dataISO,
                                quantidade = quantidade,
                                coletoresAtivos = 0,
                                dataFormatada = dataFormatada
                            )
                        }.sortedBy { it.data }
                        
                        android.util.Log.d("InventarioRepository", "Evolução carregada: ${evolucaoList.size} dias")
                        Result.success(evolucaoList)
                    } else {
                        android.util.Log.w("InventarioRepository", "Dados de evolução não encontrados")
                        Result.success(emptyList())
                    }
                } else {
                    android.util.Log.e("InventarioRepository", "Resposta sem sucesso: ${apiResponse.message}")
                    Result.failure(Exception(apiResponse.message ?: "Erro ao buscar evolução"))
                }
            } else {
                android.util.Log.e("InventarioRepository", "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro ao buscar evolução: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("InventarioRepository", "Erro ao buscar evolução de coletas", e)
            Result.failure(e)
        }
    }
    
    // Métodos auxiliares
    fun getCurrentUser(): com.inventario.mobile.data.model.Usuario? = null
    
    private suspend fun obterInventarioAtivo(): Result<Int> = Result.success(1)
}

// Data classes para compatibilidade
data class DashboardStats(
    val totalPatrimonios: Int,
    val coletados: Int,
    val naoColetados: Int,
    val percentualColetado: Double,
    val coletoresAtivos: Int = 0,
    val divergencias: Int = 0,
    val valorTotal: Double = 0.0
)

// Data class para resultado paginado
data class PagedColetasResult(
    val coletas: List<Coleta>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
