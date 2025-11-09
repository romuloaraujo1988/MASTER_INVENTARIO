package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.SincronizacaoEntity
import com.inventario.mobile.domain.model.Sincronizacao
import javax.inject.Inject

/**
 * Mapper para converter entre Entity e Domain Model de Sincronização
 */
class SincronizacaoMapper @Inject constructor() {

    fun toDomain(entity: SincronizacaoEntity): Sincronizacao {
        return Sincronizacao(
            id = entity.id,
            entidade = entity.entidade,
            entidadeId = entity.entidadeId,
            operacao = entity.operacao,
            status = if (entity.sincronizado) "SINCRONIZADO" else if (entity.erro != null) "ERRO" else "PENDENTE",
            tentativas = 0,
            ultimoErro = entity.erro,
            dataCriacao = entity.dataHora,
            dataSincronizacao = if (entity.sincronizado) entity.dataHora else null,
            dataUltimaTentativa = null
        )
    }

    fun toEntity(domain: Sincronizacao): SincronizacaoEntity {
        return SincronizacaoEntity(
            id = domain.id,
            entidade = domain.entidade,
            entidadeId = domain.entidadeId,
            operacao = domain.operacao,
            sincronizado = domain.status == "SINCRONIZADO",
            dataHora = domain.dataCriacao,
            erro = domain.ultimoErro
        )
    }

    fun toDomainList(entities: List<SincronizacaoEntity>): List<Sincronizacao> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<Sincronizacao>): List<SincronizacaoEntity> {
        return domains.map { toEntity(it) }
    }
}
