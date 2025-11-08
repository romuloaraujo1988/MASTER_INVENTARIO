package com.inventario.mobile.data.repository

import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Implementação simples do repositório de Patrimônio
 * Para fins de demonstração e teste
 */
class PatrimonioRepositoryImpl : PatrimonioRepository {

    // Lista em memória para simular dados
    private val patrimonios = mutableListOf<Patrimonio>()

    override fun getAllPatrimonios(): Flow<List<Patrimonio>> {
        return flowOf(patrimonios.toList())
    }
    
    suspend fun getAllPatrimoniosList(): List<Patrimonio> {
        return patrimonios.toList()
    }

    override suspend fun getPatrimonioById(id: Long): Patrimonio? {
        return patrimonios.find { it.id == id }
    }

    override suspend fun getPatrimonioByNumero(numeroPatrimonio: String): Patrimonio? {
        return patrimonios.find { it.numeroPatrimonio == numeroPatrimonio }
    }

    override suspend fun getPatrimonioByQrCode(qrCode: String): Patrimonio? {
        return patrimonios.find { it.qrCode == qrCode }
    }

    override suspend fun getPatrimoniosBySetor(setorId: Long): List<Patrimonio> {
        return patrimonios.filter { it.setorId == setorId }
    }

    override suspend fun getPatrimoniosBySala(salaId: Long): List<Patrimonio> {
        return patrimonios.filter { it.salaId == salaId }
    }

    override suspend fun getPatrimoniosNaoSincronizados(): List<Patrimonio> {
        return patrimonios.filter { !it.sincronizado }
    }

    override suspend fun searchPatrimonios(query: String): List<Patrimonio> {
        return patrimonios.filter { 
            it.numeroPatrimonio.contains(query, ignoreCase = true) ||
            it.descricao.contains(query, ignoreCase = true) ||
            it.marca?.contains(query, ignoreCase = true) == true ||
            it.modelo?.contains(query, ignoreCase = true) == true
        }
    }

    override suspend fun insertPatrimonio(patrimonio: Patrimonio): Long {
        val newId = (patrimonios.maxOfOrNull { it.id } ?: 0) + 1
        val newPatrimonio = patrimonio.copy(id = newId)
        patrimonios.add(newPatrimonio)
        return newId
    }

    override suspend fun insertPatrimonios(patrimonios: List<Patrimonio>) {
        patrimonios.forEach { insertPatrimonio(it) }
    }

    override suspend fun updatePatrimonio(patrimonio: Patrimonio) {
        val index = patrimonios.indexOfFirst { it.id == patrimonio.id }
        if (index != -1) {
            patrimonios[index] = patrimonio
        }
    }

    override suspend fun marcarComoSincronizado(id: Long, servidorId: Long) {
        val patrimonio = patrimonios.find { it.id == id }
        patrimonio?.let {
            updatePatrimonio(it.copy(sincronizado = true, servidorId = servidorId))
        }
    }

    override suspend fun deletePatrimonio(patrimonio: Patrimonio) {
        patrimonios.removeIf { it.id == patrimonio.id }
    }

    override suspend fun deletePatrimonioById(id: Long) {
        patrimonios.removeIf { it.id == id }
    }

    override suspend fun getPatrimonioCount(): Int {
        return patrimonios.size
    }

    override suspend fun getPatrimoniosNaoSincronizadosCount(): Int {
        return patrimonios.count { !it.sincronizado }
    }

    override suspend fun sincronizarPatrimonios(): Result<Unit> {
        // Simular sincronização
        return Result.success(Unit)
    }

    override suspend fun enviarPatrimoniosParaServidor(): Result<Unit> {
        // Simular envio para servidor
        return Result.success(Unit)
    }


}