package com.inventario.mobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.paging.SalaPagingSource
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.SalaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de Sala
 * Usa Paging 3 para carregamento eficiente
 */
@Singleton
class SalaRepositoryImpl @Inject constructor(
    private val salaApi: SalaApi,
    private val salaDao: SalaDao
) : SalaRepository {
    
    override fun getSalasPaginadas(query: String): Flow<PagingData<Sala>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = {
                SalaPagingSource(
                    salaApi = salaApi,
                    query = query.takeIf { it.isNotBlank() }
                )
            }
        ).flow.map { pagingData ->
            // Converter data.model.Sala para domain.model.Sala
            pagingData.map { sala ->
                Sala(
                    id = sala.id.toLong(),
                    nome = sala.nome,
                    codigo = sala.nome, // Usar nome como código temporariamente
                    descricao = sala.descricao,
                    ativo = sala.ativa ?: true,
                    setorId = 0L // Valor padrão
                )
            }
        }
    }
    
    override suspend fun buscarSalasLocal(query: String): List<Sala> {
        val entities = if (query.isBlank()) {
            salaDao.buscarTodas()
        } else {
            salaDao.buscarPorNome("%$query%")
        }
        
        return entities.map { entity ->
            Sala(
                id = entity.id.toLong(),
                nome = entity.nome,
                codigo = entity.nome,
                descricao = null,
                ativo = entity.ativa,
                setorId = entity.idSetor?.toLong() ?: 0L
            )
        }
    }
    
    override suspend fun contarSalas(): Int {
        return salaDao.contar()
    }
    
    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
    }
}
