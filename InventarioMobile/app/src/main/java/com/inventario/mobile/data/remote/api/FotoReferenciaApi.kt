package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.FotoReferenciaDTO
import com.inventario.mobile.data.remote.dto.PagedResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API para sincronização de fotos de referência
 * Endpoints para download de fotos por descrição
 * 
 * Suporta delta sync através do parâmetro ultimaAtualizacao.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
interface FotoReferenciaApi {
    
    /**
     * Busca fotos de referência com paginação e delta sync
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização (opcional, null = todas)
     * @param pagina Número da página (0-based)
     * @param tamanho Tamanho da página (max 50)
     * @param incluirImagem Se deve incluir a imagem Base64 (default: true)
     * @return Página de fotos de referência
     */
    @GET("api/mobile/fotos-referencia")
    suspend fun buscarFotos(
        @Query("ultimaAtualizacao") ultimaAtualizacao: Long? = null,
        @Query("pagina") pagina: Int = 0,
        @Query("tamanho") tamanho: Int = 20,
        @Query("incluirImagem") incluirImagem: Boolean = true
    ): ApiResponse<PagedResponse<FotoReferenciaDTO>>
    
    /**
     * Busca foto de referência por ID
     * 
     * @param id ID da foto
     * @return Foto de referência com imagem
     */
    @GET("api/mobile/fotos-referencia/{id}")
    suspend fun buscarPorId(
        @Path("id") id: Int
    ): ApiResponse<FotoReferenciaDTO>
    
    /**
     * Busca foto de referência por descrição normalizada
     * 
     * @param descricao Descrição normalizada
     * @return Foto de referência com imagem
     */
    @GET("api/mobile/fotos-referencia/descricao")
    suspend fun buscarPorDescricao(
        @Query("descricao") descricao: String
    ): ApiResponse<FotoReferenciaDTO>
    
    /**
     * Busca fotos de referência em lote por descrições
     * Otimizado para buscar múltiplas fotos de uma vez
     * 
     * @param descricoes Lista de descrições normalizadas
     * @return Lista de fotos de referência
     */
    @POST("api/mobile/fotos-referencia/batch")
    suspend fun buscarPorDescricoes(
        @Body descricoes: List<String>
    ): ApiResponse<List<FotoReferenciaDTO>>
    
    /**
     * Busca estatísticas das fotos de referência
     * 
     * @return Estatísticas (total, com foto, sem foto, tamanho)
     */
    @GET("api/mobile/fotos-referencia/stats")
    suspend fun buscarEstatisticas(): ApiResponse<Map<String, Any>>
    
    /**
     * Verifica se há atualizações de fotos desde o timestamp
     * Retorna apenas metadados (sem imagens) para verificação rápida
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização
     * @return Informações sobre atualizações disponíveis
     */
    @GET("api/mobile/fotos-referencia")
    suspend fun verificarAtualizacoes(
        @Query("ultimaAtualizacao") ultimaAtualizacao: Long,
        @Query("pagina") pagina: Int = 0,
        @Query("tamanho") tamanho: Int = 1,
        @Query("incluirImagem") incluirImagem: Boolean = false
    ): ApiResponse<PagedResponse<FotoReferenciaDTO>>
}
