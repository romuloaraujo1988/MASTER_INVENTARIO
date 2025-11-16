package com.inventario.mobile.data.repository

import com.inventario.mobile.data.mapper.PatrimonioConsultaMapper
import com.inventario.mobile.data.mapper.PatrimonioDetalheMapper
import com.inventario.mobile.data.remote.api.PatrimonioConsultaApi
import com.inventario.mobile.domain.model.PatrimonioConsulta
import com.inventario.mobile.domain.model.PatrimonioDetalhe
import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementação do repositório de consulta de patrimônios
 * 
 * Regra: Implementação no Data, Interface no Domain
 * Estratégia: API-first (sempre busca do servidor)
 */
class PatrimonioConsultaRepositoryImpl @Inject constructor(
    private val api: PatrimonioConsultaApi,
    private val consultaMapper: PatrimonioConsultaMapper,
    private val detalheMapper: PatrimonioDetalheMapper
) : PatrimonioConsultaRepository {
    
    /**
     * Busca patrimônios por código parcial
     * 
     * @param codigo Código parcial (mínimo 2 caracteres)
     * @param limit Quantidade máxima de resultados
     * @return Result com lista de patrimônios ou erro
     */
    override suspend fun buscarPorCodigoParcial(
        codigo: String,
        limit: Int
    ): Result<List<PatrimonioConsulta>> = withContext(Dispatchers.IO) {
        try {
            val response = api.buscarPorCodigoParcial(codigo, limit)
            
            if (response.success && response.data != null) {
                val patrimonios = consultaMapper.toDomainList(response.data)
                Result.success(patrimonios)
            } else {
                Result.failure(Exception(response.message ?: "Erro ao buscar patrimônios"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de conexão: ${e.message}", e))
        }
    }
    
    /**
     * Busca patrimônios por descrição
     * 
     * @param descricao Descrição ou parte dela (mínimo 3 caracteres)
     * @param limit Quantidade máxima de resultados
     * @return Result com lista de patrimônios ou erro
     */
    override suspend fun buscarPorDescricao(
        descricao: String,
        limit: Int
    ): Result<List<PatrimonioConsulta>> = withContext(Dispatchers.IO) {
        try {
            val response = api.buscarPorDescricao(descricao, limit)
            
            if (response.success && response.data != null) {
                val patrimonios = consultaMapper.toDomainList(response.data)
                Result.success(patrimonios)
            } else {
                Result.failure(Exception(response.message ?: "Erro ao buscar patrimônios"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de conexão: ${e.message}", e))
        }
    }
    
    /**
     * Obtém detalhes completos de um patrimônio
     * 
     * @param patrimonioId ID do patrimônio
     * @return Result com detalhes completos ou erro
     */
    override suspend fun obterDetalhesCompletos(
        patrimonioId: Int
    ): Result<PatrimonioDetalhe> = withContext(Dispatchers.IO) {
        try {
            val response = api.obterDetalhesCompletos(patrimonioId.toLong())
            
            if (response.success && response.data != null) {
                val detalhe = detalheMapper.toDomain(response.data)
                Result.success(detalhe)
            } else {
                Result.failure(Exception(response.message ?: "Erro ao obter detalhes"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de conexão: ${e.message}", e))
        }
    }
    
    /**
     * Busca avançada com múltiplos critérios
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados
     * @return Result com lista de patrimônios ou erro
     */
    override suspend fun buscarAvancada(
        termo: String,
        salaId: Int?,
        responsavelId: Int?,
        limit: Int
    ): Result<List<PatrimonioConsulta>> = withContext(Dispatchers.IO) {
        try {
            val response = api.buscarAvancada(
                termo = termo,
                salaId = salaId,
                responsavelId = responsavelId,
                limit = limit
            )
            
            if (response.success && response.data != null) {
                val patrimonios = consultaMapper.toDomainList(response.data)
                Result.success(patrimonios)
            } else {
                Result.failure(Exception(response.message ?: "Erro na busca avançada"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de conexão: ${e.message}", e))
        }
    }
}
