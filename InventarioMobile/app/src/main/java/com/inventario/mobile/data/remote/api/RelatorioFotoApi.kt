package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * API Retrofit para o Relatório Fotográfico de Coletas.
 *
 * Endpoints do servidor (não alterar URLs — regra endpoints-nao-alterar.md):
 *   GET api/mobile/relatorios/fotos/{inventarioId}/info   → CONSULTA+  (Requirement 4.1)
 *   GET api/mobile/relatorios/fotos/{inventarioId}        → SUPERVISOR+ (Requirement 4.2)
 *
 * Todas as URLs usam o prefixo completo `api/mobile/` conforme exigido
 * pela regra de steering `endpoints-nao-alterar.md` (Requirement 4.4).
 */
interface RelatorioFotoApi {

    /**
     * Retorna contagens de fotos por tipo sem gerar o PDF.
     * Requer role CONSULTA ou superior.
     *
     * @param inventarioId ID do inventário ativo.
     * @return [ApiResponse] contendo um mapa com os campos:
     *   `totalFotos`, `semEtiqueta`, `patrimonio`, `divergencia`, `temFotos`.
     */
    @GET("api/mobile/relatorios/fotos/{inventarioId}/info")
    suspend fun buscarInfo(
        @Path("inventarioId") inventarioId: Int
    ): ApiResponse<Map<String, Any>>

    /**
     * Gera e retorna o PDF como stream binário.
     * Requer role SUPERVISOR ou superior.
     *
     * A anotação [@Streaming] evita carregar o PDF inteiro na memória
     * antes de gravar, prevenindo [OutOfMemoryError] em PDFs grandes.
     *
     * @param inventarioId ID do inventário ativo.
     * @param tipo Tipo de relatório fotográfico. Padrão: `"sem_etiqueta"`.
     * @return [Response] com o corpo binário do PDF.
     */
    @Streaming
    @GET("api/mobile/relatorios/fotos/{inventarioId}")
    suspend fun baixarPdf(
        @Path("inventarioId") inventarioId: Int,
        @Query("tipo") tipo: String = "sem_etiqueta"
    ): Response<ResponseBody>
}
