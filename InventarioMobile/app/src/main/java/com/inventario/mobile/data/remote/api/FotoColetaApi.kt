package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * API Retrofit para upload/download/remoção de fotos de coleta.
 *
 * ## Endpoint de upload
 *
 * `POST /api/mobile/fotos/upload` — multipart/form-data com os campos:
 *
 * | Campo          | Tipo    | Descrição                                          |
 * |----------------|---------|----------------------------------------------------|
 * | foto           | file    | Arquivo JPEG da foto                               |
 * | coletaId       | int     | ID da coleta no servidor                           |
 * | inventarioId   | int     | ID do inventário                                   |
 * | tipo           | string  | Tipo: `patrimonio`, `sem_etiqueta` ou `divergencia`|
 * | identificador  | string  | Número do patrimônio ou `SE` para sem etiqueta     |
 *
 * O servidor organiza as fotos em:
 * ```
 * data/fotos/inventario_{id}/{YYYY-MM}/{tipo}/coleta_{coletaId}_{identificador}.jpg
 * ```
 *
 * Regras de steering (`endpoints-nao-alterar.md`): URLs usam o prefixo
 * `api/mobile/` e NÃO devem ser alteradas.
 *
 * @author Sistema de Inventário v2.22
 */
interface FotoColetaApi {

    /**
     * Upload de foto via multipart/form-data.
     *
     * @param foto          parte "foto" do multipart (arquivo JPEG).
     * @param coletaId      ID da coleta no servidor.
     * @param inventarioId  ID do inventário.
     * @param tipo          Tipo da coleta: `patrimonio`, `sem_etiqueta` ou `divergencia`.
     * @param identificador Número do patrimônio ou `SE` para itens sem etiqueta.
     * @return [ApiResponse] com mapa contendo `coletaId`, `fotoPath`, `tamanhoKB`.
     */
    @Multipart
    @POST("api/mobile/fotos/upload")
    suspend fun uploadFoto(
        @Part foto: MultipartBody.Part,
        @Part("coletaId") coletaId: RequestBody,
        @Part("inventarioId") inventarioId: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("identificador") identificador: RequestBody
    ): ApiResponse<Map<String, Any>>

    /**
     * Remove a foto associada a uma coleta.
     */
    @DELETE("api/mobile/fotos/{coletaId}")
    suspend fun removerFoto(@Path("coletaId") coletaId: Int): ApiResponse<Map<String, Any>>
}
