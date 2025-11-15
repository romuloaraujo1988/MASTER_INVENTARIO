package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface da API para comunicação com o servidor
 */
interface ApiService {

    // Autenticação
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<MobileLoginResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshRequest: RefreshTokenRequest): Response<MobileLoginResponseDto>

    // Patrimônios
    @GET("patrimonio")
    suspend fun getPatrimonios(): Response<ApiResponse<List<MobilePatrimonioDto>>>

    @GET("patrimonio")
    suspend fun getAllPatrimonios(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<ApiResponse<List<MobilePatrimonioDto>>>

    @GET("patrimonio/{id}")
    suspend fun getPatrimonioById(@Path("id") id: Long): Response<ApiResponse<MobilePatrimonioDto>>

    @GET("patrimonio/numero/{numero}")
    suspend fun getPatrimonioByNumero(@Path("numero") numero: String): Response<ApiResponse<MobilePatrimonioDto>>

    @GET("patrimonio/qr/{qrCode}")
    suspend fun getPatrimonioByQrCode(@Path("qrCode") qrCode: String): Response<ApiResponse<MobilePatrimonioDto>>

    @GET("patrimonio/responsavel/{responsavelId}")
    suspend fun getPatrimoniosByResponsavel(
        @Path("responsavelId") responsavelId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("coletado") coletado: Boolean? = null
    ): Response<ApiResponse<List<MobilePatrimonioDto>>>

    @GET("patrimonio/responsavel/{responsavelId}/count")
    suspend fun countPatrimoniosByResponsavel(@Path("responsavelId") responsavelId: Int): Response<ApiResponse<Int>>

    @POST("patrimonio")
    suspend fun createPatrimonio(@Body patrimonio: PatrimonioDto): PatrimonioDto

    @PUT("patrimonio/{id}")
    suspend fun updatePatrimonio(@Path("id") id: Long, @Body patrimonio: PatrimonioDto): PatrimonioDto

    @DELETE("patrimonio/{id}")
    suspend fun deletePatrimonio(@Path("id") id: Long): Response<Unit>

    // Setores
    @GET("setores")
    suspend fun getSetores(): List<SetorDto>

    @GET("setores/{id}")
    suspend fun getSetorById(@Path("id") id: Long): SetorDto

    // Salas
    @GET("salas")
    suspend fun getSalas(): List<SalaDto>

    @GET("salas")
    suspend fun getSalasWithResponse(): Response<ApiResponse<List<SalaDto>>>
    
    @GET("salas")
    suspend fun getSalasPaginadas(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<List<SalaDto>>>

    @GET("salas/{id}")
    suspend fun getSalaById(@Path("id") id: Long): SalaDto

    @GET("salas/setor/{setorId}")
    suspend fun getSalasBySetor(@Path("setorId") setorId: Long): List<SalaDto>

    // Usuários
    @GET("usuarios")
    suspend fun getUsuarios(): List<UsuarioDto>

    @GET("usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Long): UsuarioDto

    // Coletas
    @GET("coletas/all")
    suspend fun getColetas(): Response<ApiResponse<List<ColetaDto>>>
    
    @GET("coletas/all")
    suspend fun buscarTodasColetasSemPaginacao(): Response<ApiResponse<List<MobileColetaResponseDto>>>
    
    @GET("coletas")
    suspend fun getColetasPaginadas(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PagedResponse<ColetaDto>>>

    @POST("coletas")
    suspend fun createColeta(@Body coleta: MobileColetaRequest): Response<ApiResponse<ColetaDto>>

    @PUT("coletas/{id}")
    suspend fun updateColeta(@Path("id") id: Long, @Body coleta: MobileColetaRequest): Response<ApiResponse<ColetaDto>>

    // Responsáveis
    @GET("responsaveis")
    suspend fun getResponsaveis(): Response<ApiResponse<List<ResponsavelDto>>>

    @GET("responsaveis/{id}")
    suspend fun getResponsavelById(@Path("id") id: Int): Response<ApiResponse<ResponsavelDto>>

    // Sincronização
    @GET("sync/status")
    suspend fun getSyncStatus(): SyncStatusResponse

    @POST("sync/upload")
    suspend fun uploadData(@Body syncData: SyncDataRequest): Response<SyncDataResponse>

    @GET("sync/download")
    suspend fun downloadData(@Query("lastSync") lastSync: String?): SyncDataResponse

    // Descrições
    @GET("descricoes")
    suspend fun getDescricoes(): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("descricoes/buscar")
    suspend fun searchDescricoes(@Query("termo") termo: String): Response<ApiResponse<List<Map<String, Any>>>>
    
    @GET("descricoes/nao-coletadas")
    suspend fun getDescricoesNaoColetadas(@Query("idInventario") idInventario: Int? = null): Response<ApiResponse<List<String>>>

    // Inventários
    @GET("test/inventarios-ativos")
    suspend fun obterInventarioAtivo(): Response<ApiResponse<Map<String, Any>>>
    
    // Dashboard
    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<ApiResponse<DashboardStatsDto>>
    
    @GET("dashboard/stats/{inventarioId}")
    suspend fun getDashboardStatsByInventario(@Path("inventarioId") inventarioId: Int): Response<ApiResponse<DashboardStatsDto>>
    
    @GET("dashboard/evolucao")
    suspend fun getColetasEvolucao(@Query("dias") dias: Int = 30): Response<ApiResponse<Map<String, Any>>>
    
    @GET("dashboard/top-itens")
    suspend fun getTopItens(@Query("limit") limit: Int = 10): Response<ApiResponse<Map<String, Any>>>
    
    @GET("dashboard/distribuicao-sala")
    suspend fun getDistribuicaoPorSala(@Query("limit") limit: Int = 10): Response<ApiResponse<Map<String, Any>>>
    
    @GET("dashboard/status")
    suspend fun getEstatisticasPorStatus(): Response<ApiResponse<Map<String, Any>>>
}
