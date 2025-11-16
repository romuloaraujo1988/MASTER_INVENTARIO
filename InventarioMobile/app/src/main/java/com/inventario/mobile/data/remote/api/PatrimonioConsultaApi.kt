package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.PatrimonioConsultaDTO
import com.inventario.mobile.data.remote.dto.PatrimonioDetalheDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Retrofit para consulta de patrimônios
 * Endpoints do MobileConsultaController
 */
interface PatrimonioConsultaApi {
    
    /**
     * Busca patrimônios por código parcial
     * 
     * @param codigo Código parcial (mínimo 2 caracteres)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Lista de patrimônios encontrados
     */
    @GET("api/mobile/consulta/buscar-por-codigo")
    suspend fun buscarPorCodigoParcial(
        @Query("codigo") codigo: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PatrimonioConsultaDTO>>
    
    /**
     * Busca patrimônios por descrição
     * 
     * @param descricao Descrição ou parte dela (mínimo 3 caracteres)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Lista de patrimônios encontrados
     */
    @GET("api/mobile/consulta/buscar-por-descricao")
    suspend fun buscarPorDescricao(
        @Query("descricao") descricao: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PatrimonioConsultaDTO>>
    
    /**
     * Obtém detalhes completos de um patrimônio
     * 
     * @param patrimonioId ID do patrimônio
     * @return Detalhes completos do patrimônio
     */
    @GET("api/mobile/consulta/patrimonio/{id}/detalhes")
    suspend fun obterDetalhesCompletos(
        @Path("id") patrimonioId: Long
    ): ApiResponse<PatrimonioDetalheDTO>
    
    /**
     * Busca avançada com múltiplos critérios
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Lista de patrimônios encontrados
     */
    @GET("api/mobile/consulta/buscar-avancada")
    suspend fun buscarAvancada(
        @Query("termo") termo: String,
        @Query("salaId") salaId: Int? = null,
        @Query("responsavelId") responsavelId: Int? = null,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PatrimonioConsultaDTO>>
    
    /**
     * Health check do serviço de consulta
     * 
     * @return Status do serviço
     */
    @GET("api/mobile/consulta/health")
    suspend fun healthCheck(): ApiResponse<String>
}
