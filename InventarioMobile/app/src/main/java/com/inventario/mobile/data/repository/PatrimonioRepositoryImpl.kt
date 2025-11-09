package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.mapper.PatrimonioMapper
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementação do repositório de Patrimônio
 * Estratégia: Offline-first com sincronização
 */
class PatrimonioRepositoryImpl @Inject constructor(
    private val patrimonioDao: PatrimonioDao,
    private val patrimonioApi: PatrimonioApi,
    private val mapper: PatrimonioMapper
) : PatrimonioRepository {
    
    override fun getAllPatrimonios(): Flow<List<Patrimonio>> {
        return patrimonioDao.observarNaoColetados()
            .map { entities -> mapper.toDomainList(entities) }
    }
    
    override suspend fun getPatrimonioById(id: Long): Patrimonio? {
        return patrimonioDao.buscarPorId(id.toInt())?.let { mapper.toDomain(it) }
    }
    
    override suspend fun getPatrimonioByNumero(numeroPatrimonio: String): Patrimonio? {
        return buscarPorNumero(numeroPatrimonio)
    }
    
    override suspend fun getPatrimonioByQrCode(qrCode: String): Patrimonio? {
        // QR Code geralmente contém o número do patrimônio
        return buscarPorNumero(qrCode)
    }
    
    override suspend fun getPatrimoniosBySetor(setorId: Long): List<Patrimonio> {
        // TODO: Implementar quando necessário
        return emptyList()
    }
    
    override suspend fun getPatrimoniosBySala(salaId: Long): List<Patrimonio> {
        return patrimonioDao.buscarPorSalaNaoColetados(salaId.toInt())
            .map { mapper.toDomain(it) }
    }
    
    override suspend fun getPatrimoniosNaoSincronizados(): List<Patrimonio> {
        // TODO: Implementar quando necessário
        return emptyList()
    }
    
    override suspend fun searchPatrimonios(query: String): List<Patrimonio> {
        return buscarPorDescricaoNaoColetados(query)
    }
    
    override suspend fun insertPatrimonio(patrimonio: Patrimonio): Long {
        val entity = mapper.toEntity(patrimonio)
        patrimonioDao.inserir(entity)
        return patrimonio.id.toLong()
    }
    
    override suspend fun insertPatrimonios(patrimonios: List<Patrimonio>) {
        val entities = mapper.toEntityList(patrimonios)
        patrimonioDao.inserirTodos(entities)
    }
    
    override suspend fun updatePatrimonio(patrimonio: Patrimonio) {
        val entity = mapper.toEntity(patrimonio)
        patrimonioDao.inserir(entity) // Room usa REPLACE strategy
    }
    
    override suspend fun marcarComoSincronizado(id: Long, servidorId: Long) {
        // TODO: Implementar quando necessário
    }
    
    override suspend fun deletePatrimonio(patrimonio: Patrimonio) {
        // Não implementado - patrimônios não são deletados localmente
    }
    
    override suspend fun deletePatrimonioById(id: Long) {
        // Não implementado - patrimônios não são deletados localmente
    }
    
    override suspend fun getPatrimonioCount(): Int {
        return patrimonioDao.contarTodos()
    }
    
    override suspend fun getPatrimoniosNaoSincronizadosCount(): Int {
        return 0 // TODO: Implementar quando necessário
    }
    
    override suspend fun sincronizarPatrimonios(): Result<Unit> {
        return try {
            // Buscar patrimônios do servidor
            val response = patrimonioApi.buscarTodos()
            
            if (response.success) {
                // Converter DTOs para Domain
                val patrimonios = response.data ?: emptyList()
                
                // Salvar no banco local
                insertPatrimonios(patrimonios)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enviarPatrimoniosParaServidor(): Result<Unit> {
        // Não aplicável - patrimônios não são criados no app
        return Result.success(Unit)
    }
    
    // ========== Novos métodos Clean Architecture ==========
    
    override suspend fun buscarPorNumero(numero: String): Patrimonio? {
        return try {
            // 1. Tentar buscar do banco local primeiro (offline-first)
            val localResult = patrimonioDao.buscarPorNumero(numero)
            if (localResult != null) {
                return mapper.toDomain(localResult)
            }
            
            // 2. Se não encontrou localmente, buscar do servidor
            val response = patrimonioApi.buscarPorNumero(numero)
            if (response.success && response.data != null) {
                // Salvar no banco local para próximas consultas
                val entity = mapper.toEntity(response.data)
                patrimonioDao.inserir(entity)
                return response.data
            }
            
            null
        } catch (e: Exception) {
            // Em caso de erro, retornar resultado local se existir
            patrimonioDao.buscarPorNumero(numero)?.let { mapper.toDomain(it) }
        }
    }
    
    override suspend fun buscarDescricoesNaoColetadas(): List<String> {
        return try {
            // 1. Tentar buscar do servidor (dados mais atualizados)
            val response = patrimonioApi.buscarDescricoesNaoColetadas(null)
            if (response.success && response.data != null) {
                return response.data
            }
            
            // 2. Se falhar, buscar do banco local
            patrimonioDao.buscarDescricoesNaoColetadas()
        } catch (e: Exception) {
            // Em caso de erro de rede, usar dados locais
            patrimonioDao.buscarDescricoesNaoColetadas()
        }
    }
    
    override suspend fun buscarPorDescricaoNaoColetados(descricao: String): List<Patrimonio> {
        return try {
            // Offline-first: buscar do banco local
            val entities = patrimonioDao.buscarPorDescricaoNaoColetados(descricao)
            mapper.toDomainList(entities)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getAllPatrimoniosList(): List<Patrimonio> {
        return try {
            val entities = patrimonioDao.getAllPatrimoniosList()
            mapper.toDomainList(entities)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
