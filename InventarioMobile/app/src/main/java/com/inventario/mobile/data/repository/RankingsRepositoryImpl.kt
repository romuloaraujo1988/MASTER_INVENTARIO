package com.inventario.mobile.data.repository

import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.domain.model.ProjecaoTermino
import com.inventario.mobile.domain.model.SetorPendente
import com.inventario.mobile.domain.model.TopColetor
import com.inventario.mobile.domain.repository.RankingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação remota dos relatórios analíticos.
 *
 * Os endpoints retornam `Map<String, Any>` (genérico do servidor) e este
 * repositório mapeia para os domain models de forma defensiva: campos
 * ausentes não derrubam a UI, apenas viram `null` ou zero.
 *
 * Conversão de números é feita via [Number.toInt]/[Number.toDouble]
 * porque o JSON pode chegar como `Int`, `Long` ou `Double` dependendo
 * do tamanho — fixar um tipo causaria `ClassCastException` em produção.
 */
@Singleton
class RankingsRepositoryImpl @Inject constructor(
    private val api: ApiService
) : RankingsRepository {

    override suspend fun buscarTopColetores(
        inventarioId: Int?,
        limit: Int
    ): Result<List<TopColetor>> = runCatching {
        val response = api.getTopColetores(inventarioId, limit)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()} ao buscar top coletores")
        }
        val payload = response.body()?.data
            ?: error("Resposta vazia do servidor")

        @Suppress("UNCHECKED_CAST")
        val raw = payload["topColetores"] as? List<Map<String, Any?>>
            ?: emptyList()

        raw.map { item ->
            TopColetor(
                idUsuario             = (item["idUsuario"]   as? Number)?.toInt() ?: 0,
                nome                  =  item["nome"]        as? String ?: "—",
                totalColetas          = (item["totalColetas"] as? Number)?.toInt() ?: 0,
                ultimaColetaMillis    = (item["ultimaColetaMillis"]    as? Number)?.toLong(),
                ultimaColetaFormatada =  item["ultimaColetaFormatada"] as? String,
                produtividadePorDia   = (item["produtividadePorDia"]    as? Number)?.toDouble()
            )
        }
    }

    override suspend fun buscarSetoresPendentes(
        inventarioId: Int?,
        limit: Int
    ): Result<List<SetorPendente>> = runCatching {
        val response = api.getSetoresPendentes(inventarioId, limit)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()} ao buscar setores pendentes")
        }
        val payload = response.body()?.data
            ?: error("Resposta vazia do servidor")

        @Suppress("UNCHECKED_CAST")
        val raw = payload["setoresPendentes"] as? List<Map<String, Any?>>
            ?: emptyList()

        raw.map { item ->
            SetorPendente(
                idSetor              = (item["idSetor"]            as? Number)?.toInt() ?: 0,
                nome                 =  item["nome"]               as? String ?: "—",
                totalPatrimonios     = (item["totalPatrimonios"]   as? Number)?.toInt() ?: 0,
                totalColetados       = (item["totalColetados"]     as? Number)?.toInt() ?: 0,
                totalPendentes       = (item["totalPendentes"]     as? Number)?.toInt() ?: 0,
                percentualConcluido  = (item["percentualConcluido"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }

    override suspend fun calcularProjecaoTermino(
        inventarioId: Int?
    ): Result<ProjecaoTermino> = runCatching {
        val response = api.getProjecaoTermino(inventarioId)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()} ao calcular projeção")
        }
        val payload = response.body()?.data
            ?: error("Resposta vazia do servidor")

        ProjecaoTermino(
            totalPatrimonios            = (payload["totalPatrimonios"]     as? Number)?.toInt() ?: 0,
            totalColetados              = (payload["totalColetados"]       as? Number)?.toInt() ?: 0,
            totalPendentes              = (payload["totalPendentes"]       as? Number)?.toInt() ?: 0,
            percentualConclusao         = (payload["percentualConclusao"]   as? Number)?.toDouble() ?: 0.0,
            ritmoColetasPorDia          = (payload["ritmoColetasPorDia"]    as? Number)?.toDouble() ?: 0.0,
            diasDecorridos              = (payload["diasDecorridos"]        as? Number)?.toLong(),
            diasRestantes               = (payload["diasRestantes"]         as? Number)?.toLong(),
            dataPrevistaFormatada       =  payload["dataPrevistaFormatada"]  as? String,
            dataPrevistaMillis          = (payload["dataPrevistaMillis"]     as? Number)?.toLong(),
            concluido                   =  payload["concluido"]              as? Boolean ?: false,
            dadosInsuficientes          =  payload["dadosInsuficientes"]     as? Boolean ?: false,
            dentroDoPrazo               =  payload["dentroDoPrazo"]          as? Boolean,
            diasDeFolgaOuAtraso         = (payload["diasDeFolgaOuAtraso"]    as? Number)?.toLong(),
            dataFimInventarioFormatada  =  payload["dataFimInventarioFormatada"] as? String
        )
    }
}
