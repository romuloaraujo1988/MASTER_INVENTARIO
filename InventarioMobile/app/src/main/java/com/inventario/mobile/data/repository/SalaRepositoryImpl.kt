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
    
    // ========================================
    // Métodos para Inventário por Sala
    // ========================================
    
    override suspend fun buscarComProgresso(): Result<List<com.inventario.mobile.domain.model.SalaComProgresso>> {
        return try {
            android.util.Log.d(TAG, "Buscando salas com progresso do SERVIDOR...")
            
            // Tentar buscar do endpoint com progresso primeiro
            try {
                val response = salaApi.listarSalasComProgresso()
                if (response.isSuccessful && response.body()?.success == true) {
                    val dtos = response.body()?.data ?: emptyList()
                    android.util.Log.d(TAG, "✓ ${dtos.size} salas com progresso do servidor (endpoint com-progresso)")
                    
                    val salasComProgresso = dtos.map { dto ->
                        com.inventario.mobile.domain.model.SalaComProgresso(
                            id = dto.id,
                            nome = dto.nome,
                            numero = dto.numeroSala,
                            totalPatrimonios = dto.totalPatrimonios,
                            coletados = dto.coletados,
                            pendentes = dto.pendentes,
                            percentualColeta = dto.percentualColeta
                        )
                    }
                    return Result.success(salasComProgresso)
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Falha no endpoint com-progresso: ${e.message}")
            }
            
            // Fallback 1: Tentar endpoint simples de salas
            try {
                android.util.Log.d(TAG, "Tentando endpoint simples /api/mobile/salas...")
                val response = salaApi.listarSalas()
                if (response.isSuccessful && response.body()?.success == true) {
                    val salas = response.body()?.data ?: emptyList()
                    android.util.Log.d(TAG, "✓ ${salas.size} salas do servidor (endpoint simples)")
                    
                    // Converter para SalaComProgresso (sem estatísticas reais)
                    val salasComProgresso = salas.map { sala ->
                        com.inventario.mobile.domain.model.SalaComProgresso(
                            id = sala.id,
                            nome = sala.nome,
                            numero = sala.nome,
                            totalPatrimonios = 0,
                            coletados = 0,
                            pendentes = 0,
                            percentualColeta = 0f
                        )
                    }
                    return Result.success(salasComProgresso)
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Falha no endpoint simples: ${e.message}")
            }
            
            // Fallback 2: buscar do banco local
            android.util.Log.d(TAG, "Buscando salas com progresso do banco LOCAL...")
            val entities = salaDao.buscarComEstatisticas()
            val salasComProgresso = entities.map { entity ->
                com.inventario.mobile.data.mapper.SalaComProgressoMapper.toDomain(entity)
            }
            
            android.util.Log.d(TAG, "✓ ${salasComProgresso.size} salas com progresso do banco local")
            Result.success(salasComProgresso)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Erro ao buscar salas com progresso", e)
            Result.failure(e)
        }
    }
    
    override suspend fun buscarPorNomeOuNumero(query: String): Result<List<Sala>> {
        return try {
            android.util.Log.d(TAG, "Buscando salas por nome/número: $query")
            
            val entities = salaDao.buscarPorNomeOuNumero(query)
            val salas = entities.map { entity ->
                Sala(
                    id = entity.id.toLong(),
                    nome = entity.nome,
                    codigo = entity.nome,
                    descricao = null,
                    ativo = entity.ativa,
                    setorId = entity.idSetor?.toLong() ?: 0L
                )
            }
            
            android.util.Log.d(TAG, "✓ ${salas.size} salas encontradas")
            Result.success(salas)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Erro ao buscar salas por nome/número", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "SalaRepositoryImpl"
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
    }
}
