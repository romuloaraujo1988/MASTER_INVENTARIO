package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.mapper.ColetaMapper
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementação do repositório de Coleta
 * Estratégia: Offline-first com sincronização automática
 */
class ColetaRepositoryImpl @Inject constructor(
    private val coletaDao: ColetaDao,
    private val patrimonioDao: PatrimonioDao,
    private val coletaApi: ColetaApi,
    private val mapper: ColetaMapper
) : ColetaRepository {
    
    override fun getAllColetas(): Flow<List<Coleta>> {
        return coletaDao.observarPendentes()
            .map { entities -> mapper.toDomainList(entities) }
    }
    
    override suspend fun getColetaById(id: Long): Coleta? {
        // TODO: Implementar busca por ID
        return null
    }
    
    override suspend fun getColetasByPatrimonio(patrimonioId: Long): List<Coleta> {
        // TODO: Implementar quando necessário
        return emptyList()
    }
    
    override suspend fun getColetasByUsuario(usuarioId: Long): List<Coleta> {
        // TODO: Implementar quando necessário
        return emptyList()
    }
    
    override suspend fun getColetasNaoSincronizadas(): List<Coleta> {
        return coletaDao.buscarPendentes().map { mapper.toDomain(it) }
    }
    
    override suspend fun insertColeta(coleta: Coleta): Long {
        val entity = mapper.toEntity(coleta)
        return coletaDao.inserir(entity)
    }
    
    override suspend fun insertColetas(coletas: List<Coleta>) {
        coletas.forEach { insertColeta(it) }
    }
    
    override suspend fun updateColeta(coleta: Coleta) {
        val entity = mapper.toEntity(coleta)
        coletaDao.inserir(entity)
    }
    
    override suspend fun marcarComoSincronizado(id: Long, servidorId: Long) {
        coletaDao.marcarSincronizada(id)
    }
    
    override suspend fun deleteColeta(coleta: Coleta) {
        coletaDao.deletar(coleta.id.toLong())
    }
    
    override suspend fun getColetasNaoSincronizadasCount(): Int {
        return coletaDao.buscarPendentes().size
    }
    
    override suspend fun sincronizarColetas(): Result<Unit> {
        return sincronizarColetasPendentes().let { 
            if (it >= 0) Result.success(Unit) 
            else Result.failure(Exception("Erro na sincronização"))
        }
    }
    
    override suspend fun enviarColetasParaServidor(): Result<Unit> {
        return sincronizarColetas()
    }
    
    // ========== Novos métodos Clean Architecture ==========
    
    override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
        return try {
            // 1. Salvar localmente (offline-first)
            val entity = mapper.toEntity(coleta)
            val id = coletaDao.inserir(entity)
            
            // 2. Marcar patrimônio como coletado
            patrimonioDao.marcarComoColetado(coleta.patrimonioId.toInt())
            
            // 3. Tentar sincronizar imediatamente (não bloqueia)
            try {
                val response = coletaApi.registrarColeta(coleta)
                if (response.success) {
                    coletaDao.marcarSincronizada(id)
                }
            } catch (e: Exception) {
                // Falha na sincronização não impede o sucesso local
                // Será sincronizado depois
            }
            
            Result.success(coleta.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun jaFoiColetado(idPatrimonio: Int): Boolean {
        return try {
            val patrimonio = patrimonioDao.buscarPorId(idPatrimonio)
            patrimonio?.coletado == true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun sincronizarColetasPendentes(): Int {
        return try {
            val coletasPendentes = coletaDao.buscarPendentes()
            var sincronizadas = 0
            
            for (entity in coletasPendentes) {
                try {
                    val coleta = mapper.toDomain(entity)
                    val response = coletaApi.registrarColeta(coleta)
                    
                    if (response.success) {
                        coletaDao.marcarSincronizada(entity.id)
                        sincronizadas++
                    } else {
                        coletaDao.registrarErroSincronizacao(
                            entity.id,
                            response.message ?: "Erro desconhecido"
                        )
                    }
                } catch (e: Exception) {
                    coletaDao.registrarErroSincronizacao(
                        entity.id,
                        e.message ?: "Erro de conexão"
                    )
                }
            }
            
            sincronizadas
        } catch (e: Exception) {
            -1
        }
    }
    
    override suspend fun getColetasLocal(): List<Coleta> {
        return coletaDao.buscarPendentes().map { mapper.toDomain(it) }
    }
}
