package com.inventario.mobile.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.inventario.mobile.data.paging.PatrimonioPagingSource
import com.inventario.mobile.data.paging.PatrimonioPorSalaPagingSource
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.domain.model.Patrimonio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use Case para buscar patrimônios com paginação
 * 
 * Usa Paging 3 para carregar dados sob demanda do servidor,
 * evitando carregar todos os patrimônios de uma vez.
 * 
 * Benefícios:
 * - Carrega apenas o necessário (economia de memória e dados)
 * - Scroll infinito automático
 * - Retry automático em caso de erro
 * - Cache em memória das páginas carregadas
 */
class BuscarPatrimoniosPaginadoUseCase @Inject constructor(
    private val patrimonioApi: PatrimonioApi
) {
    
    /**
     * Busca todos os patrimônios com paginação
     * 
     * @param pageSize quantidade de itens por página (padrão: 20)
     * @return Flow de PagingData com patrimônios
     */
    operator fun invoke(pageSize: Int = 20): Flow<PagingData<Patrimonio>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                prefetchDistance = pageSize / 2, // Pré-carrega quando faltam metade dos itens
                initialLoadSize = pageSize * 2   // Carrega 2 páginas inicialmente
            ),
            pagingSourceFactory = { PatrimonioPagingSource(patrimonioApi, pageSize) }
        ).flow.map { pagingData ->
            pagingData.map { dto -> dto.toDomain() }
        }
    }
    
    /**
     * Busca patrimônios de uma sala específica com paginação
     * 
     * @param salaId ID da sala
     * @param coletado filtro: true=coletados, false=não coletados, null=todos
     * @param inventarioId ID do inventário (opcional)
     * @param pageSize quantidade de itens por página
     * @return Flow de PagingData com patrimônios
     */
    fun porSala(
        salaId: Int,
        coletado: Boolean? = null,
        inventarioId: Int? = null,
        pageSize: Int = 50
    ): Flow<PagingData<Patrimonio>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                prefetchDistance = pageSize / 2
            ),
            pagingSourceFactory = { 
                PatrimonioPorSalaPagingSource(
                    patrimonioApi = patrimonioApi,
                    salaId = salaId,
                    coletado = coletado,
                    inventarioId = inventarioId,
                    pageSize = pageSize
                ) 
            }
        ).flow.map { pagingData ->
            pagingData.map { dto -> dto.toDomain() }
        }
    }
    
    companion object {
        private const val TAG = "BuscarPatrimoniosPaginadoUseCase"
    }
}
