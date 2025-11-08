package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Sala
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Sala
 */
interface SalaRepository {

    // Operações de consulta
    fun getAllSalasAtivas(): Flow<List<Sala>>
    fun getAllSalas(): Flow<List<Sala>>
    suspend fun getSalaById(id: Long): Sala?
    suspend fun getSalasBySetor(setorId: Long): List<Sala>
    suspend fun getSalaByCodigo(codigo: String): Sala?
    suspend fun getSalasNaoSincronizadas(): List<Sala>

    // Operações de inserção
    suspend fun insertSala(sala: Sala): Long
    suspend fun insertSalas(salas: List<Sala>)

    // Operações de atualização
    suspend fun updateSala(sala: Sala)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deleteSala(sala: Sala)

    // Sincronização
    suspend fun sincronizarSalas(): Result<Unit>
    suspend fun enviarSalasParaServidor(): Result<Unit>
}