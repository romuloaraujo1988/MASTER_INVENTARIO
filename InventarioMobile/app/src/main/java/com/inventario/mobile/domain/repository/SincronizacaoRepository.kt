package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Sincronizacao
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Sincronização
 */
interface SincronizacaoRepository {

    // Operações de consulta
    suspend fun getSincronizacoesPendentes(): List<Sincronizacao>
    suspend fun getSincronizacoesComErro(): List<Sincronizacao>
    suspend fun getSincronizacaoByEntidade(entidade: String, entidadeId: Long): Sincronizacao?

    // Operações de inserção
    suspend fun insertSincronizacao(sincronizacao: Sincronizacao): Long

    // Operações de atualização
    suspend fun updateSincronizacao(sincronizacao: Sincronizacao)
    suspend fun marcarComoSincronizado(id: Long)
    suspend fun marcarComoErro(id: Long, erro: String)

    // Operações de exclusão
    suspend fun deleteSincronizacao(sincronizacao: Sincronizacao)
    suspend fun deleteSincronizacaoByEntidade(entidade: String, entidadeId: Long)

    // Operações de contagem
    suspend fun countSincronizacoesPendentes(): Int

    // Operações de sincronização
    suspend fun processarFilaSincronizacao(): Result<Unit>
    suspend fun adicionarParaSincronizacao(entidade: String, entidadeId: Long, operacao: String)
}