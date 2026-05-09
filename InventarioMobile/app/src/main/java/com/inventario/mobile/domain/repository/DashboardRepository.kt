package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface para Dashboard
 * Define O QUE fazer, não COMO fazer
 * 
 * Regra: Interface no Domain, Implementação no Data
 */
interface DashboardRepository {
    
    /**
     * Busca estatísticas gerais do dashboard
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @return Result com DashboardStats ou erro
     */
    suspend fun buscarEstatisticas(inventarioId: Int? = null): Result<DashboardStats>
    
    /**
     * Busca estatísticas apenas do banco local (offline)
     * Usado como fallback quando servidor está inacessível
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @return Result com DashboardStats locais ou erro
     */
    suspend fun buscarEstatisticasLocais(inventarioId: Int? = null): Result<DashboardStats>
    
    /**
     * Busca evolução de coletas por dia
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param dias Quantidade de dias para buscar
     * @return Result com lista de EvolucaoColeta ou erro
     */
    suspend fun buscarEvolucaoColetas(
        inventarioId: Int? = null,
        dias: Int = 30
    ): Result<List<EvolucaoColeta>>
    
    /**
     * Busca top itens mais coletados
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param limit Quantidade máxima de itens
     * @return Result com lista de TopItem ou erro
     */
    suspend fun buscarTopItens(
        inventarioId: Int? = null,
        limit: Int = 10
    ): Result<List<TopItem>>
    
    /**
     * Busca distribuição de coletas por sala
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param limit Quantidade máxima de salas
     * @return Result com lista de DistribuicaoSala ou erro
     */
    suspend fun buscarDistribuicaoPorSala(
        inventarioId: Int? = null,
        limit: Int = 10
    ): Result<List<DistribuicaoSala>>
    
    /**
     * Busca estatísticas por status de coleta
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @return Result com lista de EstatisticaStatus ou erro
     */
    suspend fun buscarEstatisticasPorStatus(
        inventarioId: Int? = null
    ): Result<List<EstatisticaStatus>>

    /**
     * 🔄 Observa estatísticas híbridas (servidor + coletas locais) em tempo real.
     *
     * Estratégia:
     * 1. Busca estatísticas base do servidor (total correto de patrimônios).
     * 2. Observa coletas locais pendentes via Room Flow.
     * 3. Emite `DashboardStats` com `totalColetados = servidor + locais não sincronizadas`.
     *
     * Flow é atualizado automaticamente quando qualquer coleta é registrada/sincronizada,
     * sem necessidade de chamar refresh manualmente.
     *
     * Em caso de falha de rede no passo 1, faz fallback para dados do Room.
     *
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @return Flow que emite estatísticas atualizadas em tempo real
     */
    fun observarEstatisticasHibridas(inventarioId: Int? = null): Flow<DashboardStats>
}
