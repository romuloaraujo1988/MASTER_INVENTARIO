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
 * Estratégia: 
 * 1. Verificar cache primeiro (100-1000x mais rápido)
 * 2. Se não estiver em cache, buscar do servidor
 * 3. Fallback para local se offline
 * 4. Cachear resultado para próximas buscas
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
     * Executa o caso de uso
     * 
     * @param query Termo de busca (mínimo 3 caracteres)
     * @param filtro Filtro de status (ALL, COLETADOS, PENDENTES, DIVERGENCIAS)
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        query: String,
        filtro: SearchFilter = SearchFilter.ALL
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
            
            Log.d(TAG, "Buscando patrimônios: query='$queryLimpa', filtro=$filtro, inventarioId=$inventarioId")
            
            // 1. VERIFICAR CACHE PRIMEIRO (100-1000x mais rápido)
            searchCache.get(cacheKey)?.let { resultadoCache ->
                Log.d(TAG, "✓ Cache hit! Retornando ${resultadoCache.size} resultados do cache")
                return Result.success(resultadoCache)
            }
            
            // 2. Tentar buscar do servidor
            val resultadoServidor = buscarDoServidor(queryLimpa, filtro, inventarioId)
            
            if (resultadoServidor.isSuccess) {
                val resultados = resultadoServidor.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ Busca no servidor bem-sucedida: ${resultados.size} resultados")
                
                // Cachear resultado do servidor
                searchCache.put(cacheKey, resultados)
                
                return resultadoServidor
            }
            
            // 3. Fallback para busca local
            Log.w(TAG, "Servidor indisponível, usando busca local")
            val resultadoLocal = buscarLocal(queryLimpa, filtro, inventarioId)
            
            // Cachear resultado local também
            resultadoLocal.onSuccess { resultados ->
                searchCache.put(cacheKey, resultados)
            }
            
            resultadoLocal
            
        } catch (e: CancellationException) {
            // Job foi cancelado (normal durante debounce/navegação)
            Log.d(TAG, "ℹ️ Busca cancelada - operação normal")
            throw e // Re-throw para não quebrar o fluxo de coroutines
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
