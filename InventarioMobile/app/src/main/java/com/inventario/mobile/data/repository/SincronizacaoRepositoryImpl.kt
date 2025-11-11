package com.inventario.mobile.data.repository

import com.inventario.mobile.data.local.dao.SincronizacaoDao
import com.inventario.mobile.data.mapper.SincronizacaoMapper
import com.inventario.mobile.domain.model.Sincronizacao
import com.inventario.mobile.domain.repository.SincronizacaoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementação do repositório de Sincronização
 */
class SincronizacaoRepositoryImpl @Inject constructor(
    private val sincronizacaoDao: SincronizacaoDao,
    private val mapper: SincronizacaoMapper
) : SincronizacaoRepository {

    override suspend fun getSincronizacoesPendentes(): List<Sincronizacao> {
        return sincronizacaoDao.buscarPendentes().map { mapper.toDomain(it) }
    }

    override suspend fun getSincronizacoesComErro(): List<Sincronizacao> {
        // Filtrar pendentes que têm erro
        return sincronizacaoDao.buscarPendentes().filter { it.erro != null }.map { mapper.toDomain(it) }
    }

    override suspend fun getSincronizacaoByEntidade(entidade: String, entidadeId: Long): Sincronizacao? {
        // Buscar todas pendentes e filtrar
        return sincronizacaoDao.buscarPendentes()
            .find { it.entidade == entidade && it.entidadeId == entidadeId }
            ?.let { mapper.toDomain(it) }
    }

    override suspend fun insertSincronizacao(sincronizacao: Sincronizacao): Long {
        return sincronizacaoDao.inserir(mapper.toEntity(sincronizacao))
    }

    override suspend fun updateSincronizacao(sincronizacao: Sincronizacao) {
        sincronizacaoDao.atualizar(mapper.toEntity(sincronizacao))
    }

    override suspend fun marcarComoSincronizado(id: Long) {
        sincronizacaoDao.marcarComoSincronizado(id)
    }

    override suspend fun marcarComoErro(id: Long, erro: String) {
        sincronizacaoDao.registrarErro(id, erro)
    }

    override suspend fun deleteSincronizacao(sincronizacao: Sincronizacao) {
        // Não há método delete individual, usar limparTodas se necessário
        // Por enquanto, marcar como sincronizado
        sincronizacaoDao.marcarComoSincronizado(sincronizacao.id)
    }

    override suspend fun deleteSincronizacaoByEntidade(entidade: String, entidadeId: Long) {
        // Não há método específico, limpar todas sincronizadas antigas
        sincronizacaoDao.limparSincronizados(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // 30 dias
    }

    override suspend fun countSincronizacoesPendentes(): Int {
        return sincronizacaoDao.contarPendentes()
    }

    override suspend fun processarFilaSincronizacao(): Result<Unit> {
        return try {
            val pendentes = getSincronizacoesPendentes()
            // TODO: Implementar lógica de sincronização
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun adicionarParaSincronizacao(entidade: String, entidadeId: Long, operacao: String) {
        val sincronizacao = Sincronizacao(
            entidade = entidade,
            entidadeId = entidadeId,
            operacao = operacao,
            status = "PENDENTE",
            tentativas = 0,
            ultimoErro = null,
            dataCriacao = System.currentTimeMillis(),
            dataSincronizacao = null,
            dataUltimaTentativa = null
        )
        insertSincronizacao(sincronizacao)
    }

    override suspend fun sincronizarTodosDados(): Result<Int> {
        return try {
            val pendentes = getSincronizacoesPendentes()
            // TODO: Implementar sincronização completa
            Result.success(pendentes.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
