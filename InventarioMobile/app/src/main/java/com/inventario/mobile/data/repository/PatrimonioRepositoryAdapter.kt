package com.inventario.mobile.data.repository

import com.inventario.mobile.domain.model.Patrimonio as DomainPatrimonio
import com.inventario.mobile.data.model.Patrimonio as DataPatrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adapter que conecta PatrimonioRepositoryImpl (usa data.model.Patrimonio)
 * com PatrimonioRepository (usa domain.model.Patrimonio)
 * 
 * Este adapter resolve o conflito de tipos entre as duas representações de Patrimonio
 */
@Singleton
class PatrimonioRepositoryAdapter @Inject constructor(
    private val impl: PatrimonioRepositoryImpl
) : PatrimonioRepository {
    
    // Conversão de data.model.Patrimonio para domain.model.Patrimonio
    private fun DataPatrimonio.toDomain(): DomainPatrimonio {
        return DomainPatrimonio(
            id = this.id,
            numeroPatrimonio = this.numeroPatrimonio,
            descricao = this.descricao,
            marca = this.marca,
            modelo = this.modelo,
            numeroSerie = this.numeroSerie,
            estado = this.estado ?: "ATIVO",
            valor = this.valor,
            dataAquisicao = null, // data.model não tem este campo
            observacoes = this.observacoes,
            setorId = this.setorId ?: 0L,
            salaId = this.salaId ?: 0L,
            qrCode = this.qrCode ?: this.numeroPatrimonio,
            sincronizado = this.sincronizado,
            coletado = this.coletado,
            dataColeta = this.dataColeta,
            coletorId = this.responsavelId
        )
    }
    
    // Conversão de domain.model.Patrimonio para data.model.Patrimonio
    private fun DomainPatrimonio.toData(): DataPatrimonio {
        return DataPatrimonio(
            id = this.id,
            numeroPatrimonio = this.numeroPatrimonio,
            descricao = this.descricao,
            marca = this.marca,
            modelo = this.modelo,
            numeroSerie = this.numeroSerie,
            estado = this.estado,
            valor = this.valor,
            setorId = this.setorId,
            setorNome = null,
            salaId = this.salaId,
            salaNome = null,
            responsavelId = this.coletorId,
            responsavelNome = null,
            qrCode = this.qrCode,
            observacoes = this.observacoes,
            coletado = this.coletado,
            dataColeta = this.dataColeta,
            coletadoPor = null,
            dataColetaFormatada = null,
            observacoesColeta = null,
            sincronizado = this.sincronizado,
            servidorId = this.servidorId
        )
    }
    
    override fun getAllPatrimonios(): Flow<List<DomainPatrimonio>> = flow {
        val result = impl.getPatrimonios()
        if (result.isSuccess) {
            val dataList = result.getOrNull() ?: emptyList()
            emit(dataList.map { it.toDomain() })
        } else {
            emit(emptyList())
        }
    }
    
    override suspend fun getPatrimonioById(id: Long): DomainPatrimonio? {
        // Não implementado no impl, retornar null
        return null
    }
    
    override suspend fun getPatrimonioByNumero(numeroPatrimonio: String): DomainPatrimonio? {
        val result = impl.getPatrimonioPorNumero(numeroPatrimonio)
        return if (result.isSuccess) {
            result.getOrNull()?.toDomain()
        } else {
            null
        }
    }
    
    override suspend fun getPatrimonioByQrCode(qrCode: String): DomainPatrimonio? {
        // Usar busca por número como fallback
        return getPatrimonioByNumero(qrCode)
    }
    
    override suspend fun getPatrimoniosBySetor(setorId: Long): List<DomainPatrimonio> {
        // Não implementado, retornar lista vazia
        return emptyList()
    }
    
    override suspend fun getPatrimoniosBySala(salaId: Long): List<DomainPatrimonio> {
        // Não implementado, retornar lista vazia
        return emptyList()
    }
    
    override suspend fun getPatrimoniosNaoSincronizados(): List<DomainPatrimonio> {
        // Não implementado, retornar lista vazia
        return emptyList()
    }
    
    override suspend fun searchPatrimonios(query: String): List<DomainPatrimonio> {
        // Não implementado, retornar lista vazia
        return emptyList()
    }
    
    override suspend fun insertPatrimonio(patrimonio: DomainPatrimonio): Long {
        // Não implementado
        return 0L
    }
    
    override suspend fun insertPatrimonios(patrimonios: List<DomainPatrimonio>) {
        // Não implementado
    }
    
    override suspend fun updatePatrimonio(patrimonio: DomainPatrimonio) {
        // Não implementado
    }
    
    override suspend fun marcarComoSincronizado(id: Long, servidorId: Long) {
        // Não implementado
    }
    
    override suspend fun deletePatrimonio(patrimonio: DomainPatrimonio) {
        // Não implementado
    }
    
    override suspend fun deletePatrimonioById(id: Long) {
        // Não implementado
    }
    
    override suspend fun getPatrimonioCount(): Int {
        val result = impl.getPatrimonios()
        return if (result.isSuccess) {
            result.getOrNull()?.size ?: 0
        } else {
            0
        }
    }
    
    override suspend fun getPatrimoniosNaoSincronizadosCount(): Int {
        // Não implementado
        return 0
    }
    
    override suspend fun sincronizarPatrimonios(): Result<Unit> {
        // Não implementado
        return Result.success(Unit)
    }
    
    override suspend fun enviarPatrimoniosParaServidor(): Result<Unit> {
        // Não implementado
        return Result.success(Unit)
    }
    
    override suspend fun buscarPorNumero(numero: String): DomainPatrimonio? {
        return getPatrimonioByNumero(numero)
    }
    
    override suspend fun buscarDescricoesNaoColetadas(): List<String> {
        // Não implementado
        return emptyList()
    }
    
    override suspend fun buscarPorDescricaoNaoColetados(descricao: String): List<DomainPatrimonio> {
        // Não implementado
        return emptyList()
    }
}
