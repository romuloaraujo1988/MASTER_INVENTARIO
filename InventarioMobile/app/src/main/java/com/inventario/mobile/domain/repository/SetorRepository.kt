package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Setor
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Setor
 */
interface SetorRepository {

    // Operações de consulta
    fun getAllSetoresAtivos(): Flow<List<Setor>>
    fun getAllSetores(): Flow<List<Setor>>
    suspend fun getSetorById(id: Long): Setor?
    suspend fun getSetorByCodigo(codigo: String): Setor?
    suspend fun getSetoresNaoSincronizados(): List<Setor>

    // Operações de inserção
    suspend fun insertSetor(setor: Setor): Long
    suspend fun insertSetores(setores: List<Setor>)

    // Operações de atualização
    suspend fun updateSetor(setor: Setor)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deleteSetor(setor: Setor)

    // Sincronização
    suspend fun sincronizarSetores(): Result<Unit>
    suspend fun enviarSetoresParaServidor(): Result<Unit>
}