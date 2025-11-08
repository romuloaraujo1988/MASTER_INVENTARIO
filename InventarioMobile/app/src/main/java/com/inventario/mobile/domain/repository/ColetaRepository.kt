package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Coleta
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Coleta
 */
interface ColetaRepository {

    // Operações de consulta
    fun getAllColetas(): Flow<List<Coleta>>
    suspend fun getColetaById(id: Long): Coleta?
    suspend fun getColetasByPatrimonio(patrimonioId: Long): List<Coleta>
    suspend fun getColetasByUsuario(usuarioId: Long): List<Coleta>
    suspend fun getColetasNaoSincronizadas(): List<Coleta>

    // Operações de inserção
    suspend fun insertColeta(coleta: Coleta): Long
    suspend fun insertColetas(coletas: List<Coleta>)

    // Operações de atualização
    suspend fun updateColeta(coleta: Coleta)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deleteColeta(coleta: Coleta)

    // Operações de contagem
    suspend fun getColetasNaoSincronizadasCount(): Int

    // Sincronização
    suspend fun sincronizarColetas(): Result<Unit>
    suspend fun enviarColetasParaServidor(): Result<Unit>
}