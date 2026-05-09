package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.cache.SearchCache
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.data.repository.PatrimonioRepositoryImpl
import com.inventario.mobile.domain.model.PatrimonioComColeta
import com.inventario.mobile.domain.model.SearchFilter
import com.inventario.mobile.utils.NetworkUtils
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Use Case: Buscar patrimônios com filtros para busca rápida
 * 
 * Estratégia OTIMIZADA (Local First):
 * 1. Verificar cache primeiro (instantâneo)
 * 2. Buscar do BANCO LOCAL (muito rápido, ~10-50ms)
 * 3. Servidor apenas se forçado ou dados muito antigos
 * 
 * IMPORTANTE: Prioriza dados locais para velocidade máxima.
 * Dados são atualizados via sincronização periódica.
 * 
 * @see Requirements 1.1, 3.1, 3.2, 3.3
 * @see OTIMIZACAO_BUSCA_RAPIDA_ANDROID.md
 */
class BuscarPatrimoniosUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepositoryImpl,
    private val patrimonioApi: PatrimonioApi,
    private val preferencesManager: PreferencesManager,
    private val searchCache: SearchCache
) {
    companion object {
        private const val TAG = "BuscarPatrimoniosUseCase"
    }
    
    /**
     * Executa o caso de uso - PRIORIZA BANCO LOCAL
     * 
     * @param query Termo de busca (mínimo 3 caracteres)
     * @param filtro Filtro de status (ALL, COLETADOS, PENDENTES, DIVERGENCIAS)
     * @param forceServer Se true, força busca no servidor (ignora local)
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        query: String,
        filtro: SearchFilter = SearchFilter.ALL,
        forceServer: Boolean = false
    ): Result<List<PatrimonioComColeta>> {
        return try {
            // Validações de negócio
            if (query.isBlank()) {
                return Result.failure(Exception("Termo de busca não pode estar vazio"))
            }
            
            if (query.length < 3) {
                return Result.failure(Exception("Digite ao menos 3 caracteres para buscar"))
            }
            
            // Sanitizar entrada
            val queryLimpa = sanitizarQuery(query)
            val inventarioId = preferencesManager.getInventarioAtivoId()
            
            // Chave do cache: query + filtro + inventário
            val cacheKey = "$queryLimpa:$filtro:${inventarioId ?: 0}"
            
            Log.d(TAG, "🔍 Buscando patrimônios: query='$queryLimpa', filtro=$filtro")
            
            // 1. VERIFICAR CACHE PRIMEIRO (instantâneo)
            if (!forceServer) {
                searchCache.get(cacheKey)?.let { resultadoCache ->
                    Log.d(TAG, "⚡ Cache hit! ${resultadoCache.size} resultados em 0ms")
                    return Result.success(resultadoCache)
                }
            }
            
            // 2. BUSCAR DO BANCO LOCAL (muito rápido: ~10-50ms)
            if (!forceServer) {
                val startTime = System.currentTimeMillis()
                val resultadoLocal = buscarLocal(queryLimpa, filtro, inventarioId)
                val tempoLocal = System.currentTimeMillis() - startTime
                
                if (resultadoLocal.isSuccess) {
                    val resultados = resultadoLocal.getOrNull() ?: emptyList()
                    Log.d(TAG, "✅ Busca LOCAL: ${resultados.size} resultados em ${tempoLocal}ms")
                    
                    // Só retorna o resultado local se encontrou dados OU se não há internet
                    // (evita retornar lista vazia quando o banco local ainda não foi sincronizado)
                    if (resultados.isNotEmpty()) {
                        // Cachear resultado local
                        searchCache.put(cacheKey, resultados)
                        return resultadoLocal
                    }
                    
                    Log.d(TAG, "⚠️ Banco local vazio para '$queryLimpa' - tentando servidor...")
                }
            }
            
            // 3. FALLBACK: Buscar do servidor (se local falhou ou forceServer=true)
            Log.d(TAG, "🌐 Buscando do servidor...")
            val startTime = System.currentTimeMillis()
            val resultadoServidor = buscarDoServidor(queryLimpa, filtro, inventarioId)
            val tempoServidor = System.currentTimeMillis() - startTime
            
            if (resultadoServidor.isSuccess) {
                val resultados = resultadoServidor.getOrNull() ?: emptyList()
                Log.d(TAG, "✅ Busca SERVIDOR: ${resultados.size} resultados em ${tempoServidor}ms")
                
                // Cachear resultado do servidor
                searchCache.put(cacheKey, resultados)
            }
            
            resultadoServidor
            
        } catch (e: CancellationException) {
            Log.d(TAG, "ℹ️ Busca cancelada - operação normal")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios", e)
            Result.failure(Exception("Erro ao buscar patrimônios: ${e.message}", e))
        }
    }
    
    /**
     * Limpa o cache de busca
     * Deve ser chamado após sincronização ou quando dados mudam
     */
    fun limparCache() {
        searchCache.clear()
        Log.d(TAG, "✓ Cache de busca limpo")
    }
    
    /**
     * Força busca no servidor (ignora cache e banco local)
     * Útil quando usuário quer dados mais recentes
     */
    suspend fun buscarDoServidorForced(
        query: String,
        filtro: SearchFilter = SearchFilter.ALL
    ): Result<List<PatrimonioComColeta>> {
        return invoke(query, filtro, forceServer = true)
    }
    
    /**
     * Retorna estatísticas do cache para debug
     */
    fun getCacheStats(): String {
        return searchCache.getStats()
    }
    
    /**
     * Busca patrimônios do servidor
     */
    private suspend fun buscarDoServidor(
        query: String,
        filtro: SearchFilter,
        inventarioId: Int?
    ): Result<List<PatrimonioComColeta>> {
        return try {
            val filtroStr = filtro.name
            
            Log.d(TAG, "Chamando API: /buscar?query=$query&filtro=$filtroStr&inventarioId=$inventarioId")
            
            val response = patrimonioApi.buscarPorQuery(
                query = query,
                filtro = filtroStr,
                inventarioId = inventarioId,
                limit = 100
            )
            
            if (response.success && response.data != null) {
                val patrimonios = response.data.map { dto ->
                    PatrimonioComColeta(
                        id = dto.id ?: 0L,
                        numero = dto.codigo ?: "",
                        descricao = dto.descricao ?: "",
                        salaNome = dto.salaNome,
                        responsavelNome = dto.responsavelNome,
                        coletado = dto.coletado,
                        coletadoPor = dto.coletadoPor,
                        dataColeta = null, // TODO: converter dataColetaFormatada para Long
                        temDivergencia = dto.temDivergencia ?: false
                    )
                }
                Result.success(patrimonios)
            } else {
                Log.w(TAG, "Resposta do servidor sem sucesso: ${response.message}")
                Result.failure(Exception(response.message.ifBlank { "Erro na busca" }))
            }
        } catch (e: CancellationException) {
            // Re-throw para não quebrar o fluxo de coroutines
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar do servidor", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca patrimônios do banco local (fallback)
     */
    private suspend fun buscarLocal(
        query: String,
        filtro: SearchFilter,
        inventarioId: Int?
    ): Result<List<PatrimonioComColeta>> {
        Log.d(TAG, "Buscando localmente: query='$query', filtro=$filtro")
        
        val result = when (filtro) {
            SearchFilter.ALL -> patrimonioRepository.buscarPorQuery(query)
            SearchFilter.COLETADOS -> patrimonioRepository.buscarColetadosPorQuery(query)
            SearchFilter.PENDENTES -> patrimonioRepository.buscarPendentesPorQuery(query)
            SearchFilter.DIVERGENCIAS -> {
                patrimonioRepository.buscarDivergenciasPorQuery(query, inventarioId ?: 0)
            }
        }
        
        return result.map { patrimonios ->
            patrimonios.map { patrimonio ->
                mapToPatrimonioComColeta(patrimonio)
            }
        }
    }
    
    /**
     * Sanitiza o termo de busca
     */
    private fun sanitizarQuery(query: String): String {
        return query.trim()
            .replace(Regex("\\s+"), " ")
    }
    
    /**
     * Mapeia Patrimonio para PatrimonioComColeta
     */
    private fun mapToPatrimonioComColeta(patrimonio: Patrimonio): PatrimonioComColeta {
        val temDivergencia = patrimonio.coletado && 
            patrimonio.observacoesColeta?.contains("divergência", ignoreCase = true) == true
        
        return PatrimonioComColeta(
            id = patrimonio.id,
            numero = patrimonio.numeroPatrimonio,
            descricao = patrimonio.descricao,
            salaNome = patrimonio.salaNome,
            responsavelNome = patrimonio.responsavelNome,
            coletado = patrimonio.coletado,
            coletadoPor = patrimonio.coletadoPor,
            dataColeta = patrimonio.dataColeta?.toLongOrNull(),
            temDivergencia = temDivergencia
        )
    }
    
    /**
     * Valida se a query é válida para busca
     */
    fun isQueryValida(query: String): Boolean {
        return query.isNotBlank() && query.trim().length >= 3
    }
    
    /**
     * Retorna mensagem de validação
     */
    fun getMensagemValidacao(query: String): String? {
        return when {
            query.isBlank() -> "Digite um termo para buscar"
            query.trim().length < 3 -> "Digite ao menos 3 caracteres"
            else -> null
        }
    }
}
