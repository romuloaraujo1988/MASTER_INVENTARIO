package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de sugestão de descrição de patrimônio não coletado.
 *
 * Espelha o contrato do record `SugestaoDescricaoDTO` exposto pelo servidor
 * no endpoint `GET /api/mobile/descricoes/sugestoes`. Traz os dados mínimos
 * para que o app associe uma sugestão a um patrimônio específico — o que é
 * indispensável para marcar o patrimônio como coletado no cache local após
 * a coleta (Req 8.4).
 *
 * Tipos são plain Kotlin / boxed nulos (`Int?`, `String?`) para máxima
 * tolerância a payloads inesperados na desserialização via Gson/Retrofit.
 *
 * Requirements: 5.1, 5.5, 5.6, 5.8, 8.4.
 *
 * @property idPatrimonio identificador do patrimônio associado à sugestão
 * @property numeroPatrimonio número do patrimônio (ex.: `IFMT-01234`)
 * @property descricao descrição cadastrada do patrimônio
 */
data class SugestaoDescricaoDto(
    @SerializedName("idPatrimonio")
    val idPatrimonio: Int?,

    @SerializedName("numeroPatrimonio")
    val numeroPatrimonio: String?,

    @SerializedName("descricao")
    val descricao: String?
)
