package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * API Retrofit para upload/download/remoção de fotos de coleta.
 *
 * Mapeia o endpoint `POST /api/mobile/fotos/upload` do backend
 * (`MobileFotoColetaController`) que salva a foto em disco e grava o
 * caminho relativo na coluna `FOTO_PATH` da `TABELA_COLETA`.
 *
 * BUGFIX F6 (07/05/2026): introduzido para corrigir o `PhotoSyncWorker`
 * que marcava fotos como sincronizadas sem efetivamente enviar ao servidor.
 *
 * Regras de steering (`endpoints-nao-alterar.md`): URLs usam o prefixo
 * `api/mobile/` e NÃO devem ser alteradas.
 */
interface FotoColetaApi {

    /**
     * Upload de foto via multipart/form-data.
     *
     * @param foto         parte "foto" do multipart, contendo o arquivo.
     * @param coletaId     `RequestParam("coletaId")` — ID da coleta no servidor.
     * @param inventarioId `RequestParam("inventarioId")` — ID do inventário.
     * @return [ApiResponse] com mapa contendo `coletaId`, `fotoPath`, `tamanhoKB`.
     */
    @Multipart
    @POST("api/mobile/fotos/upload")
    suspend fun uploadFoto(
        @Part foto: MultipartBody.Part,
        @Part("coletaId") coletaId: okhttp3.RequestBody,
        @Part("inventarioId") inventarioId: okhttp3.RequestBody
    ): ApiResponse<Map<String, Any>>

    /**
     * Remove a foto associada a uma coleta.
     */
    @DELETE("api/mobile/fotos/{coletaId}")
    suspend fun removerFoto(@Path("coletaId") coletaId: Int): ApiResponse<Map<String, Any>>
}
