package com.inventario.mobile.data.realtime

import com.google.gson.annotations.SerializedName

/**
 * Evento recebido pelo feed WebSocket de coletas em tempo real.
 *
 * Espelha o `ColetaEventoDTO` do backend. Mantemos o mapeamento via
 * `@SerializedName` em vez de confiar no nome dos campos para tolerar
 * variações entre versões do servidor sem quebrar o app.
 *
 * Apenas os campos realmente exibidos no monitor ao vivo. Se precisar
 * de mais detalhes, o app pode chamar o GET tradicional usando o `id`.
 */
data class ColetaEvento(
    @SerializedName("tipo") val tipo: String = "COLETA_REGISTRADA",
    @SerializedName("id") val id: Long? = null,
    @SerializedName("inventarioId") val inventarioId: Int? = null,
    @SerializedName("numeroPatrimonio") val numeroPatrimonio: String? = null,
    @SerializedName("descricaoPatrimonio") val descricaoPatrimonio: String? = null,
    @SerializedName("nomeColetor") val nomeColetor: String? = null,
    @SerializedName("nomeSala") val nomeSala: String? = null,
    @SerializedName("estadoEncontrado") val estadoEncontrado: String? = null,
    @SerializedName("dataColeta") val dataColeta: String? = null,
    @SerializedName("semEtiqueta") val semEtiqueta: Boolean? = null,
    @SerializedName("descricaoItemSemEtiqueta") val descricaoItemSemEtiqueta: String? = null,
    @SerializedName("temFoto") val temFoto: Boolean? = null
)
