package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.local.entity.SalaEntity
import com.inventario.mobile.domain.repository.SalaRepository
import com.inventario.mobile.domain.model.Sala
import javax.inject.Inject

/**
 * Use Case para buscar salas disponíveis para exportação
 */
class BuscarSalasParaExportacaoUseCase @Inject constructor(
    private val salaRepository: SalaRepository,
    private val salaApi: SalaApi,
    private val salaDao: SalaDao
) {
    companion object {
        private const val TAG = "BuscarSalasExportUC"
    }
    
    /**
     * Busca todas as salas, primeiro do banco local, depois do servidor se necessário
     * 
     * @return Result com lista de salas ordenadas por nome
     */
    suspend operator fun invoke(): Result<List<Sala>> {
        return try {
            Log.d(TAG, "Buscando salas para exportação...")
            
            // 1. Tentar buscar do banco local primeiro
            var salas = salaRepository.buscarSalasLocal("")
            Log.d(TAG, "Salas no banco local: ${salas.size}")
            
            // 2. Se banco local estiver vazio, buscar do servidor
            if (salas.isEmpty()) {
                Log.d(TAG, "Banco local vazio, buscando do servidor...")
                salas = buscarDoServidor()
            }
            
            Log.d(TAG, "Total de salas encontradas: ${salas.size}")
            Result.success(salas.sortedBy { it.nome })
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar salas: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Busca salas do servidor e salva no banco local
     */
    private suspend fun buscarDoServidor(): List<Sala> {
        try {
            // Tentar endpoint de sync primeiro (retorna todas as salas)
            Log.d(TAG, "Tentando endpoint /api/mobile/sync/salas...")
            val syncResponse = salaApi.listarTodasSalas()
            if (syncResponse.isSuccessful && syncResponse.body()?.success == true) {
                val salasApi = syncResponse.body()?.data ?: emptyList()
                Log.d(TAG, "✓ ${salasApi.size} salas do endpoint sync")
                
                // Salvar no banco local
                salvarNoBancoLocal(salasApi)
                
                return salasApi.map { sala ->
                    Sala(
                        id = sala.id.toLong(),
                        nome = sala.nome,
                        codigo = sala.nome,
                        descricao = sala.descricao,
                        ativo = sala.ativa ?: true,
                        setorId = 0L
                    )
                }
            }
            
            // Fallback: endpoint simples
            Log.d(TAG, "Tentando endpoint /api/mobile/salas...")
            val response = salaApi.listarSalas()
            if (response.isSuccessful && response.body()?.success == true) {
                val salasApi = response.body()?.data ?: emptyList()
                Log.d(TAG, "✓ ${salasApi.size} salas do endpoint simples")
                
                // Salvar no banco local
                salvarNoBancoLocal(salasApi)
                
                return salasApi.map { sala ->
                    Sala(
                        id = sala.id.toLong(),
                        nome = sala.nome,
                        codigo = sala.nome,
                        descricao = sala.descricao,
                        ativo = sala.ativa ?: true,
                        setorId = 0L
                    )
                }
            }
            
            Log.w(TAG, "Nenhum endpoint retornou salas")
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar salas do servidor: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Salva salas no banco local para cache
     */
    private suspend fun salvarNoBancoLocal(salas: List<com.inventario.mobile.data.model.Sala>) {
        try {
            val entities = salas.map { sala ->
                SalaEntity(
                    id = sala.id,
                    nome = sala.nome,
                    idSetor = null,
                    nomeSetor = null,
                    ativa = sala.ativa ?: true,
                    dataUltimaAtualizacao = System.currentTimeMillis()
                )
            }
            salaDao.inserirTodas(entities)
            Log.d(TAG, "✓ ${entities.size} salas salvas no banco local")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar salas no banco local: ${e.message}", e)
        }
    }
    
    /**
     * Busca salas filtradas por nome
     * 
     * @param query Termo de busca
     * @return Result com lista de salas filtradas
     */
    suspend fun buscarPorNome(query: String): Result<List<Sala>> {
        return try {
            val salas = salaRepository.buscarSalasLocal(query)
            Result.success(salas.sortedBy { it.nome })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
