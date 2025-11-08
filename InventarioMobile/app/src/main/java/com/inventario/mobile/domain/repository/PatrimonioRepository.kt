package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Patrimonio
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Patrimônio
 */
interface PatrimonioRepository {

    // Operações de consulta
    fun getAllPatrimonios(): Flow<List<Patrimonio>>
    suspend fun getPatrimonioById(id: Long): Patrimonio?
    suspend fun getPatrimonioByNumero(numeroPatrimonio: String): Patrimonio?
    suspend fun getPatrimonioByQrCode(qrCode: String): Patrimonio?
    suspend fun getPatrimoniosBySetor(setorId: Long): List<Patrimonio>
    suspend fun getPatrimoniosBySala(salaId: Long): List<Patrimonio>
    suspend fun getPatrimoniosNaoSincronizados(): List<Patrimonio>
    suspend fun searchPatrimonios(query: String): List<Patrimonio>

    // Operações de inserção
    suspend fun insertPatrimonio(patrimonio: Patrimonio): Long
    suspend fun insertPatrimonios(patrimonios: List<Patrimonio>)

    // Operações de atualização
    suspend fun updatePatrimonio(patrimonio: Patrimonio)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deletePatrimonio(patrimonio: Patrimonio)
    suspend fun deletePatrimonioById(id: Long)

    // Operações de contagem
    suspend fun getPatrimonioCount(): Int
    suspend fun getPatrimoniosNaoSincronizadosCount(): Int

    // Sincronização
    suspend fun sincronizarPatrimonios(): Result<Unit>
    suspend fun enviarPatrimoniosParaServidor(): Result<Unit>
}