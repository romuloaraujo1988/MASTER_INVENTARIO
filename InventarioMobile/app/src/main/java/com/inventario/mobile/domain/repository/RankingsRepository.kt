package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.ProjecaoTermino
import com.inventario.mobile.domain.model.SetorPendente
import com.inventario.mobile.domain.model.TopColetor

/**
 * Repositório dos relatórios analíticos do dashboard
 * (Top Coletores / Setores Pendentes / Projeção de Término).
 *
 * Toda a lógica é remota — não há fallback offline. Falhas de rede
 * resultam em [Result.failure] e devem ser tratadas pela camada de
 * apresentação (a UI mostra um placeholder de erro).
 */
interface RankingsRepository {

    /**
     * Ranking dos coletores mais produtivos.
     *
     * @param inventarioId Inventário a consultar; `null` usa o ativo no servidor
     * @param limit        Quantidade máxima a retornar (default 10)
     */
    suspend fun buscarTopColetores(
        inventarioId: Int? = null,
        limit: Int = 10
    ): Result<List<TopColetor>>

    /**
     * Ranking dos setores com mais patrimônios pendentes.
     *
     * @param inventarioId Inventário a consultar; `null` usa o ativo no servidor
     * @param limit        Quantidade máxima a retornar (default 10)
     */
    suspend fun buscarSetoresPendentes(
        inventarioId: Int? = null,
        limit: Int = 10
    ): Result<List<SetorPendente>>

    /**
     * Projeção de término do inventário com base no ritmo médio de coleta.
     *
     * @param inventarioId Inventário a consultar; `null` usa o ativo no servidor
     */
    suspend fun calcularProjecaoTermino(
        inventarioId: Int? = null
    ): Result<ProjecaoTermino>
}
