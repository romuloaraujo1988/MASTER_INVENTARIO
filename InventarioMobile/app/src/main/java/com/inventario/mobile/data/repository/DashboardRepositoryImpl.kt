package com.inventario.mobile.data.repository

import android.util.Log
import com.inventario.mobile.data.mapper.DashboardMapper
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.domain.model.*
import com.inventario.mobile.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.catch
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
    private val preferencesManager: com.inventario.mobile.utils.PreferencesManager
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
                    Log.d(TAG, "Stats mapeados: ${stats.percentualConclusao}% (${stats.totalColetados}/${stats.totalPatrimonios})")
                    Log.d(TAG, "═══ SUCESSO ═══")
                    Result.success(stats)
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
        return try {
            Log.d(TAG, "═══ BUSCAR ESTATÍSTICAS LOCAIS (OFFLINE) ═══")
            Log.d(TAG, "Inventário ID: $inventarioId")
            
            // TODO: Implementar busca no banco local (Room)
            // Por enquanto, retornar estatísticas vazias
            Log.w(TAG, "⚠️ Busca local ainda não implementada - retornando dados vazios")
            
            val emptyStats = com.inventario.mobile.domain.model.DashboardStats(
                totalPatrimonios = 0,
                totalColetados = 0,
                totalPendentes = 0,
                percentualConclusao = 0.0,
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
            
            Log.d(TAG, "═══ RETORNANDO DADOS VAZIOS (OFFLINE) ═══")
            Result.success(emptyStats)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar estatísticas locais", e)
            Result.failure(e)
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
     * 🔄 MÉTODO HÍBRIDO INTELIGENTE - Servidor + Coletas Locais
     * 
     * Estratégia:
     * 1. Busca estatísticas base do servidor (total correto de patrimônios)
     * 2. Observa coletas locais em tempo real (todas, não apenas pendentes)
     * 3. Calcula: Total Coletados = Coletas no Servidor + Coletas Locais Não Sincronizadas
     * 
     * Benefícios:
     * - Total de patrimônios sempre correto (do servidor: 11428)
     * - Coletas locais somadas instantaneamente
     * - Atualização em tempo real sem esperar sincronização
     * - Não duplica coletas já sincronizadas
     * 
     * @param inventarioId ID do inventário (null = todos)
     * @return Flow que emite DashboardStats sempre que há mudanças
     */
    fun observarEstatisticasHibridas(inventarioId: Int?): kotlinx.coroutines.flow.Flow<com.inventario.mobile.domain.model.DashboardStats> {
        return kotlinx.coroutines.flow.flow {
            Log.d(TAG, "🔄 Iniciando observação híbrida de estatísticas")
            
            // 1. Buscar estatísticas base do servidor (uma única vez)
            val serverStatsResult = buscarEstatisticas(inventarioId)
            
            if (serverStatsResult.isFailure) {
                Log.w(TAG, "⚠️ Falha ao buscar do servidor, usando apenas dados locais")
                // Fallback para dados locais
                dashboardDao.observarEstatisticas(inventarioId).collect { dto ->
                    emit(mapDtoToDomain(dto, inventarioId))
                }
                return@flow
            }
            
            val serverStats = serverStatsResult.getOrNull()!!
            Log.d(TAG, "📊 Estatísticas base do servidor:")
            Log.d(TAG, "   Total Patrimônios: ${serverStats.totalPatrimonios}")
            Log.d(TAG, "   Coletados (servidor): ${serverStats.totalColetados}")
            Log.d(TAG, "   Pendentes (servidor): ${serverStats.totalPendentes}")
            
            // 2. Observar coletas locais em tempo real
            // Conta apenas coletas NÃO sincronizadas (sincronizado = false)
            val invId = inventarioId ?: preferencesManager.getInventarioAtivoId() ?: 0
            
            dashboardDao.observarTotalColetas(invId).collect { totalColetasLocais ->
                Log.d(TAG, "💾 Total de coletas locais NÃO sincronizadas: $totalColetasLocais")
                
                // 3. Calcular estatísticas híbridas
                // IMPORTANTE: O servidor já retorna o total de coletas sincronizadas
                // O DAO agora filtra apenas coletas NÃO sincronizadas (sincronizado = 0 ou NULL)
                // Então somamos: Servidor (sincronizadas) + Locais (não sincronizadas) = Total Real
                val totalColetadosAtualizado = serverStats.totalColetados + totalColetasLocais
                val totalPendentesAtualizado = serverStats.totalPatrimonios - totalColetadosAtualizado
                val percentualAtualizado = if (serverStats.totalPatrimonios > 0) {
                    (totalColetadosAtualizado * 100.0) / serverStats.totalPatrimonios
                } else {
                    0.0
                }
                
                Log.d(TAG, "🔄 Estatísticas híbridas calculadas:")
                Log.d(TAG, "   Total Patrimônios: ${serverStats.totalPatrimonios}")
                Log.d(TAG, "   Coletados: $totalColetadosAtualizado (servidor: ${serverStats.totalColetados} + locais: $totalColetasLocais)")
                Log.d(TAG, "   Pendentes: $totalPendentesAtualizado")
                Log.d(TAG, "   Percentual: ${String.format("%.2f", percentualAtualizado)}%")
                
                // 4. Emitir estatísticas atualizadas
                emit(serverStats.copy(
                    totalColetados = totalColetadosAtualizado,
                    totalPendentes = totalPendentesAtualizado,
                    percentualConclusao = percentualAtualizado
                ))
            }
        }
        .catch { e ->
            Log.e(TAG, "❌ Erro ao observar estatísticas híbridas", e)
            emit(createEmptyStats(inventarioId))
        }
    }
    
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
    
    /**
     * Cria estatísticas vazias para fallback
     */
    private fun createEmptyStats(inventarioId: Int?): com.inventario.mobile.domain.model.DashboardStats {
        return com.inventario.mobile.domain.model.DashboardStats(
            totalPatrimonios = 0,
            totalColetados = 0,
            totalPendentes = 0,
            percentualConclusao = 0.0,
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
