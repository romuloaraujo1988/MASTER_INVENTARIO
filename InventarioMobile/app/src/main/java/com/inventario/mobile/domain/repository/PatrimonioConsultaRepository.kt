package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.PatrimonioConsulta
import com.inventario.mobile.domain.model.PatrimonioDetalhe

/**
 * Repository interface para consulta de patrimônios
 * Define O QUE fazer, não COMO fazer
 * 
 * Regra: Interface no Domain, Implementação no Data
 */
interface PatrimonioConsultaRepository {
    
    /**
     * Busca patrimônios por código parcial
     * 
     * @param codigo Código parcial (mínimo 2 caracteres)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Result com lista de patrimônios ou erro
     */
    suspend fun buscarPorCodigoParcial(
        codigo: String,
        limit: Int = 10
    ): Result<List<PatrimonioConsulta>>
    
    /**
     * Busca patrimônios por descrição
     * 
     * @param descricao Descrição ou parte dela (mínimo 3 caracteres)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Result com lista de patrimônios ou erro
     */
    suspend fun buscarPorDescricao(
        descricao: String,
        limit: Int = 10
    ): Result<List<PatrimonioConsulta>>
    
    /**
     * Obtém detalhes completos de um patrimônio
     * 
     * @param patrimonioId ID do patrimônio
     * @return Result com detalhes completos ou erro
     */
    suspend fun obterDetalhesCompletos(
        patrimonioId: Int
    ): Result<PatrimonioDetalhe>
    
    /**
     * Busca avançada com múltiplos critérios
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Result com lista de patrimônios ou erro
     */
    suspend fun buscarAvancada(
        termo: String,
        salaId: Int? = null,
        responsavelId: Int? = null,
        limit: Int = 10
    ): Result<List<PatrimonioConsulta>>
}
