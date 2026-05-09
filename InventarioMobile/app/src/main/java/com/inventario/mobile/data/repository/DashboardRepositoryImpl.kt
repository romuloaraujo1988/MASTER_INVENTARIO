package com.inventario.mobile.data.repository

import android.util.Log
import com.inventario.mobile.data.mapper.DashboardMapper
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.domain.model.*
import com.inventario.mobile.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Implementação do DashboardRepository
 * Coordena fontes de dados (remote-first + reativo)
 * 
 * v2.4: Adicionado suporte a Room Flow para reatividade automática
 * v2.5: Adicionado método híbrido (servidor + coletas locais)
 */
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: DashboardMapper,
    private val dashboardDao: com.inventario.mobile.data.local.dao.DashboardDao,
    private val preferencesManager: com.inventario.mobile.utils.PreferencesManager,
    private val patrimonioDao: com.inventario.mobile.data.local.dao.PatrimonioDao,
    private val coletaDao: com.inventario.mobile.data.local.dao.ColetaDao
) : DashboardRepository {
    
    companion object {
        private const val TAG = "DashboardRepositoryImpl"
    }
    
    override suspend fun buscarEstatisticas(inventarioId: Int?): Result<com.inventario.mobile.domain.model.DashboardStats> {
        return try {
            Log.d(TAG, "═══ BUSCAR ESTATÍSTICAS ═══")
            Log.d(TAG, "Inventário ID: $inventarioId")
            
            // Tratar 0 como null (usar inventário ativo do servidor)
            val effectiveInventarioId = if (inventarioId == null || inventarioId == 0) null else inventarioId
            
            val response = if (effectiveInventarioId != null) {
                Log.d(TAG, "Chamando: getDashboardStatsWithInventario($effectiveInventarioId)")
                apiService.getDashboardStatsWithInventario(effectiveInventarioId)
            } else {
                Log.d(TAG, "Chamando: getDashboardStats() (sem inventarioId)")
                apiService.getDashboardStats()
            }
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            Log.d(TAG, "Response Body: ${response.body()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "API Response Success: ${apiResponse.success}")
                Log.d(TAG, "API Response Message: ${apiResponse.message}")
                Log.d(TAG, "API Response Data: ${apiResponse.data}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val dto = apiResponse.data
                    Log.d(TAG, "DTO: totalPatrimonios=${dto.totalPatrimonios}, coletados=${dto.patrimoniosColetados}, pendentes=${dto.patrimoniosPendentes}")
                    
                    val stats = mapper.toDomain(dto)
                    val statsParaCache = stats.copy(
                        timestampUltimaSincronizacao = System.currentTimeMillis(),
                        isOfflineData = false
                    )
                    preferencesManager.saveCacheServerStats(statsParaCache)
                    Log.d(TAG, "💾 CacheServerStats persistido: timestamp=${statsParaCache.timestampUltimaSincronizacao}")
                    Log.d(TAG, "Stats mapeados: ${statsParaCache.percentualConclusao}% (${statsParaCache.totalColetados}/${statsParaCache.totalPatrimonios})")
                    Log.d(TAG, "═══ SUCESSO ═══")
                    Result.success(statsParaCache)
                } else {
                    val error = "API retornou erro: ${apiResponse.message}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } else {
                val error = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "═══ ERRO ═══", e)
            Log.e(TAG, "Tipo: ${e.javaClass.simpleName}")
            Log.e(TAG, "Mensagem: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    override suspend fun buscarEstatisticasLocais(inventarioId: Int?): Result<com.inventario.mobile.domain.model.DashboardStats> {
        val invId = inventarioId ?: preferencesManager.getInventarioAtivoId() ?: 0
        val cache = preferencesManager.getCacheServerStats()

        // Primeiro tentamos ler Room com proteção individual para cada query.
        // Se uma falhar, caímos no cache do servidor (que contém os últimos dados
        // bons conhecidos) em vez de retornar zeros.
        return runCatching {
            Log.d(TAG, "═══ BUSCAR ESTATÍSTICAS LOCAIS (OFFLINE) ═══")
            Log.d(TAG, "Inventário ID efetivo: $invId (parâmetro: $inventarioId)")
            Log.d(TAG, "CacheServerStats disponível: ${cache != null} (timestamp=${cache?.timestamp})")

            val totalPatrimonios = cache?.totalPatrimonios
                ?: runCatching { patrimonioDao.countAll() }
                    .getOrElse {
                        Log.w(TAG, "patrimonioDao.countAll() falhou, usando 0", it)
                        0
                    }

            val totalColetadosLocais = runCatching {
                coletaDao.buscarTodas(invId).distinctBy { it.idPatrimonio }.size
            }.getOrElse {
                Log.w(TAG, "coletaDao.buscarTodas($invId) falhou, usando cache do servidor", it)
                // Se o Room falhou, usamos o total do cache (é melhor que 0)
                cache?.totalColetados ?: 0
            }

            val totalPendentes = maxOf(0, totalPatrimonios - totalColetadosLocais)
            val percentualConclusao = if (totalPatrimonios > 0) {
                roundTo2((totalColetadosLocais * 100.0) / totalPatrimonios)
            } else {
                0.0
            }

            Log.d(TAG, "Stats locais calculados: total=$totalPatrimonios, coletados=$totalColetadosLocais, pendentes=$totalPendentes, percentual=$percentualConclusao")

            com.inventario.mobile.domain.model.DashboardStats(
                totalPatrimonios = totalPatrimonios,
                totalColetados = totalColetadosLocais,
                totalPendentes = totalPendentes,
                percentualConclusao = percentualConclusao,
                coletoresAtivos = cache?.coletoresAtivos ?: 0,
                divergencias = cache?.divergencias ?: 0,
                valorTotal = cache?.valorTotal ?: 0.0,
                inventarioId = if (invId > 0) invId else null,
                inventarioNome = cache?.inventarioNome,
                coletasHoje = 0,
                coletasSemana = 0,
                coletasMes = 0,
                tempoMedioColeta = 0.0,
                isOfflineData = true,
                timestampUltimaSincronizacao = cache?.timestamp
            )
        }.recoverCatching { e ->
            Log.e(TAG, "buscarEstatisticasLocais falhou — usando cache do servidor integralmente", e)
            // Se tudo falhou (até o construtor do DashboardStats), usa apenas o
            // CacheServerStats como fallback final. Só retorna empty() se o cache
            // também estiver vazio (primeira execução sem sync prévia).
            if (cache != null) {
                com.inventario.mobile.domain.model.DashboardStats(
                    totalPatrimonios = cache.totalPatrimonios,
                    totalColetados = cache.totalColetados,
                    totalPendentes = maxOf(0, cache.totalPatrimonios - cache.totalColetados),
                    percentualConclusao = if (cache.totalPatrimonios > 0)
                        roundTo2((cache.totalColetados * 100.0) / cache.totalPatrimonios)
                    else 0.0,
                    coletoresAtivos = cache.coletoresAtivos,
                    divergencias = cache.divergencias,
                    valorTotal = cache.valorTotal,
                    inventarioId = cache.inventarioId,
                    inventarioNome = cache.inventarioNome,
                    isOfflineData = true,
                    timestampUltimaSincronizacao = cache.timestamp
                )
            } else {
                com.inventario.mobile.domain.model.DashboardStats
                    .empty(inventarioId = if (invId > 0) invId else null, isOfflineData = true)
            }
        }
    }

    
    override suspend fun buscarEvolucaoColetas(
        inventarioId: Int?,
        dias: Int
    ): Result<List<EvolucaoColeta>> {
        return try {
            Log.d(TAG, "Buscando evolução ($dias dias)...")
            
            val response = apiService.getColetasEvolucao(dias = dias)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                
                if (apiResponse.success && apiResponse.data != null) {
                    val evolucaoMap = apiResponse.data["evolucao"] as? Map<*, *>
                    
                    if (evolucaoMap != null) {
                        val evolucaoList = mapper.evolucaoMapToDomain(evolucaoMap)
                        Log.d(TAG, "Evolução: ${evolucaoList.size} dias")
                        Result.success(evolucaoList)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Erro"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarTopItens(inventarioId: Int?, limit: Int): Result<List<TopItem>> {
        return try {
            Log.d(TAG, "═══ BUSCAR TOP ITENS ═══")
            Log.d(TAG, "Inventário ID: $inventarioId, Limit: $limit")
            
            val response = apiService.getTopItens(limit)
            
            Log.d(TAG, "Response Code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "API Response Success: ${apiResponse.success}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val data = apiResponse.data
                    
                    @Suppress("UNCHECKED_CAST")
                    val topItensMap = data["topItens"] as? Map<String, Any>
                    
                    if (topItensMap != null) {
                        val topItens = topItensMap.map { (descricao, quantidade) ->
                            val qtd = when (quantidade) {
                                is Number -> quantidade.toInt()
                                else -> 0
                            }
                            TopItem(
                                descricao = descricao,
                                quantidade = qtd,
                                percentual = 0.0
                            )
                        }.sortedByDescending { it.quantidade }
                        
                        Log.d(TAG, "Top itens: ${topItens.size} itens")
                        topItens.forEach { Log.d(TAG, "  ${it.descricao}: ${it.quantidade}") }
                        
                        Result.success(topItens)
                    } else {
                        Log.w(TAG, "topItens não encontrado no response")
                        Result.success(emptyList())
                    }
                } else {
                    val error = "API retornou erro: ${apiResponse.message}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } else {
                val error = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar top itens", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarDistribuicaoPorSala(inventarioId: Int?, limit: Int): Result<List<DistribuicaoSala>> {
        return try {
            Log.d(TAG, "═══ BUSCAR DISTRIBUIÇÃO POR SALA ═══")
            Log.d(TAG, "Inventário ID: $inventarioId, Limit: $limit")
            
            val response = apiService.getDistribuicaoPorSala(limit)
            
            Log.d(TAG, "Response Code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "API Response Success: ${apiResponse.success}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val data = apiResponse.data
                    
                    @Suppress("UNCHECKED_CAST")
                    val distribuicaoMap = data["distribuicaoPorSala"] as? Map<String, Any>
                    
                    if (distribuicaoMap != null) {
                        val distribuicao = distribuicaoMap.map { (sala, quantidade) ->
                            val qtd = when (quantidade) {
                                is Number -> quantidade.toInt()
                                else -> 0
                            }
                            DistribuicaoSala(
                                sala = sala,
                                quantidade = qtd,
                                percentual = 0.0
                            )
                        }.sortedByDescending { it.quantidade }
                        
                        Log.d(TAG, "Distribuição por sala: ${distribuicao.size} salas")
                        distribuicao.forEach { Log.d(TAG, "  ${it.sala}: ${it.quantidade}") }
                        
                        Result.success(distribuicao)
                    } else {
                        Log.w(TAG, "distribuicaoPorSala não encontrado no response")
                        Result.success(emptyList())
                    }
                } else {
                    val error = "API retornou erro: ${apiResponse.message}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } else {
                val error = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar distribuição por sala", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarEstatisticasPorStatus(inventarioId: Int?): Result<List<EstatisticaStatus>> {
        return try {
            Log.d(TAG, "═══ BUSCAR ESTATÍSTICAS POR STATUS ═══")
            Log.d(TAG, "Inventário ID: $inventarioId")
            
            val response = apiService.getEstatisticasPorStatus(inventarioId)
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "API Response Success: ${apiResponse.success}")
                Log.d(TAG, "API Response Data: ${apiResponse.data}")
                
                if (apiResponse.success && apiResponse.data != null) {
                    val data = apiResponse.data
                    
                    // Extrair statusDistribuicao do response
                    @Suppress("UNCHECKED_CAST")
                    val statusDistribuicao = data["statusDistribuicao"] as? Map<String, Any>
                    
                    if (statusDistribuicao != null) {
                        val estatisticas = statusDistribuicao.map { (status, quantidade) ->
                            val qtd = when (quantidade) {
                                is Number -> quantidade.toInt()
                                else -> 0
                            }
                            EstatisticaStatus(
                                status = status,
                                quantidade = qtd,
                                percentual = 0.0
                            )
                        }
                        
                        Log.d(TAG, "Estatísticas por status: ${estatisticas.size} estados")
                        estatisticas.forEach { Log.d(TAG, "  ${it.status}: ${it.quantidade}") }
                        
                        Result.success(estatisticas)
                    } else {
                        Log.w(TAG, "statusDistribuicao não encontrado no response")
                        Result.success(emptyList())
                    }
                } else {
                    val error = "API retornou erro: ${apiResponse.message}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } else {
                val error = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar estatísticas por status", e)
            Result.failure(e)
        }
    }
    
    /**
     * 🔄 MÉTODO HÍBRIDO INTELIGENTE - Servidor + Coletas Locais (Task 4.3)
     *
     * Pipeline reativo (Req 2.8, 2.9, 2.10, 2.11, 3.12, 3.13):
     *  1) Caminho feliz: `buscarEstatisticas` (servidor) + `emitAll(observarTotalColetas.map { ... })`.
     *  2) Falha do servidor: `catch` emite uma vez `buscarEstatisticasLocais` (fallback imediato — Req 2.9)
     *     e mantém reatividade local via `observarTotalColetas`.
     *  3) Em cada emissão, aplica `totalPendentes = max(0, totalPatrimonios - coletadosAtualizado)` (Req 3.13)
     *     e `percentualConclusao = roundTo2(...)` para consistência com `buscarEstatisticasLocais` (Req 3.2).
     *  4) No catch, todas as emissões têm `isOfflineData = true` (Req 3.5, 4.1).
     */
    override fun observarEstatisticasHibridas(inventarioId: Int?): Flow<com.inventario.mobile.domain.model.DashboardStats> {
        val invId = inventarioId ?: preferencesManager.getInventarioAtivoId() ?: 0
        Log.d(TAG, "🎯 observarEstatisticasHibridas iniciado (parametro=$inventarioId, efetivo=$invId)")
        return flow {
            // 1) Base do servidor (com persistência de cache). Se falhar, cai no catch.
            Log.d(TAG, "🎯 observarEstatisticasHibridas: chamando buscarEstatisticas($inventarioId)")
            val baseResult = buscarEstatisticas(inventarioId)
            Log.d(TAG, "🎯 observarEstatisticasHibridas: buscarEstatisticas retornou isSuccess=${baseResult.isSuccess}")
            val base = baseResult.getOrThrow()
            Log.d(TAG, "🎯 observarEstatisticasHibridas: base obtida — total=${base.totalPatrimonios} coletados=${base.totalColetados} pendentes=${base.totalPendentes}")

            // Emissão IMEDIATA da base do servidor (antes do combine com Room Flow).
            // Isso garante que o Fragment receba os KPIs do servidor mesmo quando o
            // Room Flow demora para emitir o primeiro valor.
            emit(base)
            Log.d(TAG, "🎯 observarEstatisticasHibridas: emitiu base imediata para upstream")

            // 2) Combina com Flow de coletas locais não-sincronizadas (Room reativo).
            // Envolvido com .catch { emit(0) } para que uma falha no Room (ex.:
            // SQLiteException por migration incompleta) NÃO derrube toda a
            // pipeline e force o .catch externo a zerar os dados do servidor.
            // Sem isso: servidor emite 77, Room falha, ViewModel.catch emite
            // DashboardStats.empty = 0 e sobrescreve os dados bons.
            emitAll(
                dashboardDao.observarTotalColetas(invId)
                    .catch { e ->
                        Log.w(TAG, "🎯 observarTotalColetas falhou, assumindo 0 coletas locais", e)
                        emit(0)
                    }
                    .map { totalLocais ->
                        Log.d(TAG, "🎯 observarTotalColetas emitiu $totalLocais coletas locais (invId=$invId)")
                        val coletadosAtualizado = base.totalColetados + totalLocais
                        val pendentesAtualizado = maxOf(0, base.totalPatrimonios - coletadosAtualizado)
                        val percentual = if (base.totalPatrimonios > 0)
                            (coletadosAtualizado * 100.0) / base.totalPatrimonios
                        else 0.0
                        base.copy(
                            totalColetados = coletadosAtualizado,
                            totalPendentes = pendentesAtualizado,
                            percentualConclusao = roundTo2(percentual)
                        )
                    }
            )
        }.catch { e ->
            Log.w(TAG, "🎯 observarEstatisticasHibridas: falha no servidor, caindo para fallback offline", e)
            // 3) Fallback: uma emissão imediata de estatísticas locais (Req 2.9)
            val local = buscarEstatisticasLocais(inventarioId)
                .getOrElse {
                    com.inventario.mobile.domain.model.DashboardStats.empty(
                        inventarioId = if (invId > 0) invId else null,
                        isOfflineData = true
                    )
                }
            emit(local.copy(isOfflineData = true))
            // 4) Continua reativo às mudanças locais mesmo offline
            emitAll(
                dashboardDao.observarTotalColetas(invId).map { totalLocais ->
                    val base = local
                    val coletadosAtualizado = base.totalColetados + totalLocais
                    val pendentesAtualizado = maxOf(0, base.totalPatrimonios - coletadosAtualizado)
                    base.copy(
                        totalColetados = coletadosAtualizado,
                        totalPendentes = pendentesAtualizado,
                        percentualConclusao = if (base.totalPatrimonios > 0)
                            roundTo2((coletadosAtualizado * 100.0) / base.totalPatrimonios)
                        else 0.0,
                        isOfflineData = true
                    )
                }
            )
        }
    }
    
    /**
     * Arredonda um Double para 2 casas decimais.
     * Usado por `buscarEstatisticasLocais` no cálculo de `percentualConclusao` (Req 3.2).
     */
    private fun roundTo2(value: Double): Double = Math.round(value * 100.0) / 100.0

    /**
     * Converte DTO do Room para modelo de domínio
     */
    private fun mapDtoToDomain(dto: com.inventario.mobile.data.local.dto.DashboardStatsDto, inventarioId: Int?): com.inventario.mobile.domain.model.DashboardStats {
        return com.inventario.mobile.domain.model.DashboardStats(
            totalPatrimonios = dto.totalPatrimonios,
            totalColetados = dto.totalColetados,
            totalPendentes = dto.totalPendentes,
            percentualConclusao = dto.percentualColetado.toDouble(),
            coletoresAtivos = 0,
            divergencias = 0,
            valorTotal = 0.0,
            inventarioId = inventarioId,
            inventarioNome = null,
            coletasHoje = 0,
            coletasSemana = 0,
            coletasMes = 0,
            tempoMedioColeta = 0.0,
            isOfflineData = true
        )
    }
}
